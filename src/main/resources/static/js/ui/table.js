import { namesJoin } from '../utils/helpers.js';

const CREATE_ACTION_LABEL = 'Создать запись';

function renderCell(entity, column, row, refs, data) {
  if (column.render) {
    return column.render(row, refs, data);
  }
  const value = row[column.key];
  return value ?? '—';
}

export function applyQuery(items, config, ui, refs = {}, data = {}) {
  let result = [...items];

  Object.entries(ui.filters || {}).forEach(([key, value]) => {
    if (value === null || value === undefined || value === '') {
      return;
    }
    const filterDef = config.filters?.find((f) => f.key === key);
    if (filterDef?.match) {
      result = result.filter((item) => filterDef.match(item, value));
      return;
    }
    result = result.filter(
      (item) => String(item[key] ?? '') === String(value),
    );
  });

  if (ui.sort?.key) {
    const { key, dir } = ui.sort;
    const sortColumn = config.columns?.find((c) => c.key === key);
    const pick = (row) =>
      typeof sortColumn?.sortValue === 'function'
        ? sortColumn.sortValue(row)
        : (row[key] ?? '');
    result.sort((a, b) => {
      const left = pick(a);
      const right = pick(b);
      const cmp = String(left).localeCompare(String(right), 'ru', {
        numeric: true,
      });
      return dir === 'asc' ? cmp : -cmp;
    });
  }

  return result;
}

export function paginate(items, page = 0, size = 10) {
  const start = page * size;
  return items.slice(start, start + size);
}

function renderSkeleton(cols) {
  return Array.from({ length: 6 }, (_, rowIndex) => `<tr class="border-b border-zinc-100/80">${Array.from(
    { length: cols },
    () =>
      `<td class="px-4 py-3"><div class="h-3.5 rounded-md bg-zinc-200/90 animate-pulse ${rowIndex % 2 === 0 ? 'w-full' : 'w-4/5'}"></div></td>`,
  ).join('')}</tr>`).join('');
}

function emptyState(config, canCreate, hasFilters) {
  return `
    <div class="flex min-h-[220px] flex-col items-center justify-center px-6 py-10 text-center">
      <h3 class="text-sm font-medium text-zinc-600">${hasFilters ? 'Нет данных' : 'Нет записей'}</h3>
      ${canCreate ? `<button data-create-entity class="btn-primary mt-4" aria-label="${CREATE_ACTION_LABEL}">${CREATE_ACTION_LABEL}</button>` : ''}
    </div>`;
}

export function renderTable({
  entity,
  config,
  rows,
  refs,
  data,
  ui,
  meta,
  pageableFromApi,
  canEdit = true,
  canDelete = true,
  loading = false,
  renderLeadingActions,
}) {
  const hasRows = rows.length > 0;
  const columnCount =
    config.columns.length +
    (canEdit || canDelete ? 1 : 0) +
    (canDelete ? 1 : 0);
  const headers = config.columns
    .map((col) => {
      const active = ui.sort.key === col.key;
      const arrow = active ? (ui.sort.dir === 'asc' ? '↑' : '↓') : '↕';
      return `<th data-sort="${col.key}" class="px-4 py-3 text-left align-middle text-xs font-semibold uppercase tracking-wider text-zinc-500 cursor-pointer select-none hover:text-violet-700 whitespace-nowrap"><span class="inline-flex items-center gap-1.5 leading-none"><span>${col.label}</span><span class="font-normal text-zinc-400" aria-hidden="true">${arrow}</span></span></th>`;
    })
    .join('');

  let body = '';
  if (loading) {
    body = renderSkeleton(columnCount);
  } else {
    body = rows
      .map((row, index) => {
        const isSelected = ui.selectedIds.has(row.id);
        const cells = config.columns
          .map(
            (column) =>
              `<td class="px-4 py-3 align-middle text-sm text-zinc-800">${renderCell(entity, column, row, refs, data)}</td>`,
          )
          .join('');
        const leading =
          typeof renderLeadingActions === 'function'
            ? renderLeadingActions(row)
            : '';
        return `<tr data-table-row class="border-b border-zinc-100/90 transition-colors hover:bg-violet-50/60 ${index % 2 ? 'bg-zinc-50/70' : 'bg-white'}">
                ${canDelete ? `<td class="px-4 py-3 align-middle"><input data-select-id="${row.id}" type="checkbox" class="h-4 w-4 rounded border-zinc-300 text-violet-600 focus:ring-violet-500/35" ${isSelected ? 'checked' : ''} /></td>` : ''}
            ${cells}
            ${canEdit || canDelete ? `<td class="px-4 py-3 align-middle"><div class="flex items-center justify-end gap-2 whitespace-nowrap">
              ${leading}
              ${canEdit ? `<button data-edit-id="${row.id}" class="btn-secondary text-xs">Изменить</button>` : ''}
              ${canDelete ? `<button data-delete-id="${row.id}" class="btn-danger text-xs">Удалить</button>` : ''}
            </div></td>` : ''}
        </tr>`;
      })
      .join('');
  }

  const pages = pageableFromApi
    ? meta.totalPages
    : Math.max(1, Math.ceil((meta.totalElements || 0) / meta.size));

  const filterBlock =
    config.filters?.length > 0
      ? `<div class="flex flex-wrap items-end gap-3">
            ${config.filters
              .map(
                (filter) =>
                  `<label class="min-w-[160px] flex-1 text-xs font-medium text-zinc-600">${filter.label}<select data-filter-key="${filter.key}" class="input-base mt-1"><option value="">Все</option>${filter.options(refs, data).map((opt) => `<option ${String(ui.filters[filter.key] || '') === String(opt.value) ? 'selected' : ''} value="${opt.value}">${opt.label}</option>`).join('')}</select></label>`,
              )
              .join('')}
          </div>`
      : '';

  const hasToolbar = Boolean(filterBlock) || canDelete;
  const toolbar = hasToolbar
    ? `<div class="flex flex-col gap-3 border-b border-zinc-100 bg-zinc-50/80 px-4 py-4 sm:flex-row sm:flex-wrap sm:items-end sm:justify-between">
          ${filterBlock || '<span class="min-w-0 flex-1"></span>'}
          ${canDelete ? `<button type="button" id="bulk-delete" class="btn-danger shrink-0 self-start sm:self-end" ${ui.selectedIds.size ? '' : 'disabled'}>Удалить (${ui.selectedIds.size})</button>` : ''}
        </div>`
    : '';

  return `
    <section class="card-base overflow-hidden">
      ${toolbar}
      ${!hasRows && !loading ? `<div class="px-4 py-6">${emptyState(config, canEdit, Object.values(ui.filters || {}).some(Boolean))}</div>` : `
      <div class="overflow-x-auto">
        <table class="min-w-full table-auto">
          <thead class="border-b border-zinc-200 bg-zinc-50/90">
            <tr>
              ${canDelete ? `<th class="px-4 py-3 text-left"><input id="select-all" type="checkbox" class="h-4 w-4 rounded border-zinc-300 text-violet-600 focus:ring-violet-500/35" ${hasRows && rows.every((r) => ui.selectedIds.has(r.id)) ? 'checked' : ''}/></th>` : ''}
              ${headers}
              ${canEdit || canDelete ? '<th class="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wider text-zinc-500"></th>' : ''}
            </tr>
          </thead>
          <tbody>${body}</tbody>
        </table>
      </div>`}
      <div class="flex flex-col gap-2 border-t border-zinc-100 bg-zinc-50/90 px-4 py-3 text-sm text-zinc-600 sm:flex-row sm:items-center sm:justify-between">
        <span class="tabular-nums text-zinc-500">${meta.totalElements || 0} шт.</span>
        <div class="flex items-center gap-2">
          <button type="button" data-page-action="prev" class="btn-secondary" ${meta.page <= 0 ? 'disabled' : ''}>←</button>
          <span class="rounded-lg border border-zinc-200 bg-white px-2.5 py-1 text-sm tabular-nums text-zinc-700 shadow-sm">${meta.page + 1} / ${pages}</span>
          <button type="button" data-page-action="next" class="btn-secondary" ${meta.page + 1 >= pages ? 'disabled' : ''}>→</button>
        </div>
      </div>
    </section>`;
}
