import { entityLabelWithIcon } from './entityMeta.js';

const CREATE_ACTION_LABEL = 'Создать запись';

function getRefLabel(field, item) {
  if (field.ref === 'publishers') {
    return item.name;
  }
  if (field.ref === 'authors') {
    return item.name;
  }
  if (field.ref === 'genres') {
    return item.name;
  }
  if (field.ref === 'books') {
    return item.title || item.name || item.id;
  }
  return item.id;
}

function optionsForRef(field, refs, _data, value) {
  const source = refs[field.ref] || [];
  return source
    .map(
      (item) =>
        `<option ${String(value) === String(item.id) ? 'selected' : ''} value="${item.id}">${getRefLabel(field, item)}</option>`,
    )
    .join('');
}

export function validateForm(entity, values) {
  const errors = {};

  if (entity === 'books') {
    if (!(values.isbn || '').trim()) {
      errors.isbn = 'ISBN обязателен';
    }
    if (!(values.title || '').trim()) {
      errors.title = 'Название обязательно';
    }
    const price = Number(values.price);
    if (!Number.isFinite(price) || price <= 0) {
      errors.price = 'Цена должна быть больше 0';
    }
    if (!values.publisherId) {
      errors.publisherId = 'Выберите издателя';
    }
    const authorIds = values.authorIds;
    if (!Array.isArray(authorIds) || authorIds.length < 1) {
      errors.authorIds = 'Нужен хотя бы один автор';
    }
  }

  if (entity === 'publishers') {
    if (!(values.name || '').trim()) {
      errors.name = 'Название обязательно';
    }
  }

  if (entity === 'authors') {
    if (!(values.name || '').trim()) {
      errors.name = 'Имя обязательно';
    }
  }

  if (entity === 'genres') {
    if (!(values.name || '').trim()) {
      errors.name = 'Название обязательно';
    }
  }

  if (entity === 'reviews') {
    const r = Number(values.rating);
    if (!Number.isInteger(r) || r < 1 || r > 5) {
      errors.rating = 'Оценка от 1 до 5';
    }
    if (!values.bookId) {
      errors.bookId = 'Выберите книгу';
    }
  }

  return errors;
}

function renderField(field, value, refs, data, error, row) {
  const baseInputClass = `input-base mt-1.5 w-full ${error ? 'border-rose-300 focus:border-rose-400 focus:ring-rose-200' : ''}`;

  if (field.type === 'multiref') {
    const source = refs[field.ref] || [];
    const raw = row?.[field.storageKey] || row?.[field.key];
    let selected = new Set();
    if (Array.isArray(raw)) {
      selected = new Set(
        raw.map((x) => (typeof x === 'object' ? x.id : x)).filter(Boolean),
      );
    }
    const boxes = source
      .map(
        (item) =>
          `<label class="flex items-center gap-2 text-sm text-slate-700 py-1"><input data-multiref="1" type="checkbox" name="${field.key}" value="${item.id}" class="h-4 w-4 rounded border-slate-300 text-indigo-600" ${selected.has(item.id) ? 'checked' : ''}/> ${getRefLabel({ ref: field.ref }, item)}</label>`,
      )
      .join('');
    return `
      <fieldset class="border border-slate-200 rounded-xl p-3 space-y-1">
        <legend class="text-sm font-medium text-slate-700 px-1">${field.label}${field.required ? ' *' : ''}</legend>
        <div class="max-h-40 overflow-y-auto space-y-0.5">${boxes}</div>
        ${error ? `<p class="mt-1 text-xs text-rose-600">${error}</p>` : ''}
      </fieldset>`;
  }

  if (field.type === 'select' && field.options) {
    return `
      <label class="block text-sm font-medium text-slate-700">
        ${field.label}
        <select name="${field.key}" class="${baseInputClass}" ${field.required ? 'required' : ''}>
          <option value="">Выберите...</option>
          ${field.options
            .map(
              (opt) =>
                `<option ${String(value) === String(opt.value) ? 'selected' : ''} value="${opt.value}">${opt.label}</option>`,
            )
            .join('')}
        </select>
        ${error ? `<p class="mt-1 text-xs text-rose-600">${error}</p>` : ''}
      </label>`;
  }

  if (field.type === 'ref') {
    return `
      <label class="block text-sm font-medium text-slate-700">
        ${field.label}
        <select name="${field.key}" class="${baseInputClass}" ${field.required ? 'required' : ''}>
          <option value="">${field.allowEmpty ? 'Не выбрано' : 'Выберите...'}</option>
          ${optionsForRef(field, refs, data, value)}
        </select>
        ${error ? `<p class="mt-1 text-xs text-rose-600">${error}</p>` : ''}
      </label>`;
  }

  if (field.type === 'textarea') {
    return `
      <label class="block text-sm font-medium text-slate-700">
        ${field.label}
        <textarea name="${field.key}" rows="3" class="${baseInputClass}">${value ?? ''}</textarea>
        ${error ? `<p class="mt-1 text-xs text-rose-600">${error}</p>` : ''}
      </label>`;
  }

  return `
    <label class="block text-sm font-medium text-slate-700">
      ${field.label}
      <input type="${field.type || 'text'}" name="${field.key}" value="${value ?? ''}" class="${baseInputClass}" ${field.required ? 'required' : ''} />
      ${error ? `<p class="mt-1 text-xs text-rose-600">${error}</p>` : ''}
    </label>`;
}

export function renderEntityForm(
  config,
  refs,
  data,
  row = null,
  entityKey = null,
  formErrors = {},
) {
  const fieldHtml = config.fields
    .map((field) => {
      let val = row?.[field.key];
      if (field.storageKey && row?.[field.storageKey]) {
        val = row[field.storageKey];
      }
      val = val ?? field.defaultValue ?? '';
      return renderField(field, val, refs, data, formErrors[field.key], row);
    })
    .join('');

  return `
  <form id="entity-form" class="space-y-5">
      <div>
        <h3 class="font-semibold tracking-tight text-slate-900 text-lg">${row ? 'Редактирование' : 'Создание'}: ${entityKey ? entityLabelWithIcon(entityKey, config.title) : config.title}</h3>
      </div>
      <div class="space-y-4">${fieldHtml}</div>
      <div class="flex gap-2 border-t border-slate-100 pt-4">
        <button class="btn-primary" type="submit">${row ? 'Сохранить' : CREATE_ACTION_LABEL}</button>
        <button class="btn-secondary" type="button" data-cancel-edit>Отмена</button>
      </div>
   </form>`;
}

export function renderDrawer({ title, subtitle = '', body, open }) {
  return `
    <div class="fixed inset-0 z-40 ${open ? '' : 'pointer-events-none'}" data-drawer-root>
      <div class="absolute inset-0 bg-slate-900/40 transition-opacity ${open ? 'opacity-100' : 'opacity-0'}" data-drawer-close></div>
      <aside class="absolute right-0 top-0 h-full w-full max-w-xl bg-white border-l border-slate-200 shadow-2xl transition-transform duration-200 ${open ? 'translate-x-0' : 'translate-x-full'}">
        <div class="p-6 md:p-7 border-b border-slate-200 flex items-start justify-between gap-4">
          <div>
            <h2 class="text-xl font-semibold text-slate-900">${title}</h2>
            ${subtitle ? `<p class="text-sm text-slate-500 mt-1">${subtitle}</p>` : ''}
          </div>
          <button class="btn-secondary" type="button" data-drawer-close>Закрыть</button>
        </div>
        <div class="p-6 md:p-7 overflow-y-auto h-[calc(100%-89px)]">${body}</div>
      </aside>
    </div>`;
}

export function formDataToObject(form) {
  const fd = new FormData(form);
  const multiref = new Set();
  /** @type {HTMLFormElement} */
  const f = form;
  f.querySelectorAll('[data-multiref="1"]').forEach((el) => {
    multiref.add(el.name);
  });
  /** @type {Record<string, unknown>} */
  const data = {};
  multiref.forEach((key) => {
    const vals = fd.getAll(key).filter(Boolean);
    data[key] = vals.map((v) => Number(v));
  });
  fd.forEach((value, key) => {
    if (multiref.has(key)) {
      return;
    }
    data[key] = value === '' ? null : value;
  });
  return data;
}
