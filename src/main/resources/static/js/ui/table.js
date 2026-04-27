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

  if (ui.search) {
    const needle = ui.search.toLowerCase();
    result = result.filter((item) =>
      config.columns.some((col) => {
        const cell = col.render
          ? col.render(item, refs, data)
          : item[col.key];
        return String(cell ?? '').toLowerCase().includes(needle);
      }),
    );
  }

  Object.entries(ui.filters || {}).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== '') {
      result = result.filter(
        (item) => String(item[key] ?? '') === String(value),
      );
    }
  });

  if (ui.sort?.key) {
    const { key, dir } = ui.sort;
    result.sort((a, b) => {
      const left = a[key] ?? '';
      const right = b[key] ?? '';
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
  return Array.from({ length: 6 }, (_, rowIndex) => `<tr class="border-b border-slate-100">${Array.from(
    { length: cols },
    () =>
      `<td class="px-6 py-4"><div class="h-4 rounded bg-slate-200/80 animate-pulse ${rowIndex % 2 === 0 ? 'w-full' : 'w-4/5'}"></div></td>`,
  ).join('')}</tr>`).join('');
}

function emptyState(config, canCreate, hasQuery) {
  return `
    <div class="flex min-h-[360px] flex-col items-center justify-center px-6 py-10 text-center">
      <div class="mb-3 inline-flex h-12 w-12 items-center justify-center rounded-xl bg-slate-100 text-slate-500">
        <span class="text-xl">📄</span>
      </div>
      <h3 class="text-base font-semibold text-slate-900">${hasQuery ? 'Ничего не найдено' : 'Пока нет записей'}</h3>
      <p class="mt-1 max-w-sm text-sm text-slate-500">${hasQuery ? 'Попробуйте очистить поиск или фильтры.' : 'Добавьте первую запись, чтобы начать работу с разделом.'}</p>
      ${canCreate ? `<button data-create-entity class="btn-primary mt-5" aria-label="${CREATE_ACTION_LABEL}">${CREATE_ACTION_LABEL}</button>` : ''}
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
      return `<th data-sort="${col.key}" class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500 cursor-pointer hover:text-slate-900">${col.label} <span class="text-slate-400">${arrow}</span></th>`;
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
              `<td class="px-6 py-4 align-middle text-sm text-slate-700">${renderCell(entity, column, row, refs, data)}</td>`,
          )
          .join('');
        return `<tr data-table-row class="border-b border-slate-100 hover:bg-indigo-50/40 transition-colors ${index % 2 ? 'bg-slate-50/20' : ''}">
                ${canDelete ? `<td class="px-6 py-4 align-middle"><input data-select-id="${row.id}" type="checkbox" class="h-5 w-5 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500" ${isSelected ? 'checked' : ''} /></td>` : ''}
            ${cells}
            ${canEdit || canDelete ? `<td class="px-6 py-4 align-middle"><div class="flex items-center justify-end gap-2 whitespace-nowrap">
              ${canEdit ? `<button data-view-id="${row.id}" class="btn-secondary text-xs">Просмотр</button>` : ''}
              ${canEdit ? `<button data-edit-id="${row.id}" class="btn-secondary text-xs">Редактировать</button>` : ''}
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
      ? `<div class="grid gap-3 sm:grid-cols-2 lg:col-span-5">
            ${config.filters
              .map(
                (filter) =>
                  `<select data-filter-key="${filter.key}" class="input-base"><option value="">${filter.label}: все</option>${filter.options(refs, data).map((opt) => `<option ${String(ui.filters[filter.key] || '') === String(opt.value) ? 'selected' : ''} value="${opt.value}">${opt.label}</option>`).join('')}</select>`,
              )
              .join('')}
          </div>`
      : `<div class="lg:col-span-5"></div>`;

  return `
                <section class="card-base overflow-hidden">
                <div class="border-b border-slate-200 bg-white px-5 py-5 sm:px-6">
               <div class="grid gap-3 lg:grid-cols-12 lg:items-center">
                <label class="relative block lg:col-span-5">
                <span class="pointer-events-none absolute inset-y-0 left-3 inline-flex items-center text-slate-400">⌕</span>
            <input id="search-input" value="${ui.search || ''}" placeholder="Поиск по таблице" class="input-base pl-9" />
        </label>
            ${filterBlock}
           <div class="lg:col-span-2 lg:justify-self-end">
            ${canDelete ? `<button id="bulk-delete" class="btn-danger w-full lg:w-auto" ${ui.selectedIds.size ? '' : 'disabled'}>Удалить выбранные (${ui.selectedIds.size})</button>` : ''}
          </div>
        </div>
        </div>
                ${!hasRows && !loading ? `<div class="px-5 py-6 sm:px-6">${emptyState(config, canEdit, Boolean(ui.search) || Object.values(ui.filters || {}).some(Boolean))}</div>` : `
      <div class="overflow-x-auto">
        <table class="min-w-full table-auto">
          <thead class="border-b border-slate-200 bg-slate-50">
            <tr>
              ${canDelete ? `<th class="px-6 py-4 text-left"><input id="select-all" type="checkbox" class="h-5 w-5 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500" ${hasRows && rows.every((r) => ui.selectedIds.has(r.id)) ? 'checked' : ''}/></th>` : ''}
              ${headers}
              ${canEdit || canDelete ? '<th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">Действия</th>' : ''}
            </tr>
          </thead>
          <tbody>${body}</tbody>
        </table>
      </div>`}
                <div class="flex flex-col gap-3 border-t border-slate-200 bg-white px-5 py-4 text-sm text-slate-600 sm:flex-row sm:items-center sm:justify-between sm:px-6">
                    <span>Всего: ${meta.totalElements || 0}</span>
                    <div class="flex items-center gap-2">
                        <button data-page-action="prev" class="btn-secondary" ${meta.page <= 0 ? 'disabled' : ''}>Назад</button>
                        <span class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-1.5">Стр. ${meta.page + 1} / ${pages}</span>
                        <button data-page-action="next" class="btn-secondary" ${meta.page + 1 >= pages ? 'disabled' : ''}>Вперёд</button>
                    </div>
                </div>
            </section>`;
}
