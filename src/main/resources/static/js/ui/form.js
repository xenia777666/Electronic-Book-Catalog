import {
  ISBN_BACKEND_PATTERN,
  normalizeIsbn,
  escapeFormText,
  escapeFormAttr,
  YEAR_BOOK_MIN,
  YEAR_BOOK_MAX,
  BIRTH_YEAR_MIN,
  parseLocalDateOnly,
  maxBirthDateIso,
} from '../utils/helpers.js';
import { entityLabelWithIcon } from './entityMeta.js';

const CREATE_ACTION_LABEL = 'Создать запись';

const PHONE_PATTERN = /^\+?[0-9\s\-()]{10,20}$/;
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/** Тот же вид ошибки, что у полей сущностей — для фильтра книг и форм. */
export function renderInlineFieldError(message) {
  if (!message) {
    return '';
  }
  return `<p class="mt-1.5 text-xs font-medium leading-snug text-rose-600" role="alert">${escapeFormText(message)}</p>`;
}

function fieldErrorHtml(message) {
  return renderInlineFieldError(message);
}

const BOOK_FILTER_TEXT_MAX = 200;

function parseOptionalDecimal(raw) {
  const t = String(raw ?? '').trim();
  if (!t) {
    return { empty: true, n: null };
  }
  const n = Number(t.replace(',', '.'));
  if (!Number.isFinite(n)) {
    return { empty: false, n: null, invalid: true };
  }
  return { empty: false, n, invalid: false };
}

/** Валидация полей панели поиска книг (как у форм сущностей). */
export function validateBookSearchFilters(f) {
  /** @type {Record<string, string>} */
  const errors = {};

  for (const key of ['author', 'genre', 'title']) {
    const t = (f[key] || '').trim();
    if (t.length > BOOK_FILTER_TEXT_MAX) {
      errors[key] = `Не больше ${BOOK_FILTER_TEXT_MAX} символов`;
    }
  }

  const minP = parseOptionalDecimal(f.minPrice);
  const maxP = parseOptionalDecimal(f.maxPrice);

  if (!minP.empty) {
    if (minP.invalid) {
      errors.minPrice = 'Укажите число, например 100 или 99.90';
    } else if (minP.n < 0) {
      errors.minPrice = 'Цена не может быть отрицательной';
    }
  }
  if (!maxP.empty) {
    if (maxP.invalid) {
      errors.maxPrice = 'Укажите число, например 500 или 499.99';
    } else if (maxP.n < 0) {
      errors.maxPrice = 'Цена не может быть отрицательной';
    }
  }
  if (!errors.minPrice && !errors.maxPrice && !minP.empty && !maxP.empty && minP.n > maxP.n) {
    errors.maxPrice = '«Цена до» не может быть меньше «Цена от»';
  }

  const rt = String(f.minRating ?? '').trim();
  if (rt) {
    const r = Number(rt.replace(',', '.'));
    if (!Number.isFinite(r)) {
      errors.minRating = 'Укажите число, например 3 или 4.5';
    } else if (r < 0 || r > 5) {
      errors.minRating = 'Рейтинг: от 0 до 5';
    }
  }

  return errors;
}

/**
 * Объединяет строку из БД с черновиком из формы (после ошибки валидации/сервера).
 * @param {object|null|undefined} row
 * @param {Record<string, unknown>|null|undefined} draft
 * @param {string} entity
 */
export function mergeEditingRowWithDraft(row, draft, entity) {
  if (!draft || typeof draft !== 'object') {
    return row ?? null;
  }
  const base = row && typeof row === 'object' ? { ...row } : {};
  for (const key of Object.keys(draft)) {
    base[key] = draft[key];
  }
  if (entity === 'books') {
    if (Array.isArray(draft.authorIds)) {
      base.authors = draft.authorIds.map((id) => ({ id: Number(id) }));
    }
    if (Array.isArray(draft.genreIds)) {
      base.genres = draft.genreIds.map((id) => ({ id: Number(id) }));
    }
  }
  return base;
}

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
        `<option ${String(value) === String(item.id) ? 'selected' : ''} value="${escapeFormAttr(String(item.id))}">${escapeFormText(getRefLabel(field, item))}</option>`,
    )
    .join('');
}

export function validateForm(entity, values) {
  const errors = {};

  if (entity === 'books') {
    const isbnNorm = normalizeIsbn(values.isbn);
    if (!isbnNorm) {
      errors.isbn = 'Укажите ISBN';
    } else if (!ISBN_BACKEND_PATTERN.test(isbnNorm)) {
      errors.isbn =
        'Нужен ISBN-10 или ISBN-13 (цифры; в конце ISBN-10 допустима X). Дефисы можно — при сохранении уберутся. Пример: 9785699180312';
    }
    const title = (values.title || '').trim();
    if (!title) {
      errors.title = 'Название обязательно';
    } else if (title.length < 2 || title.length > 255) {
      errors.title = 'Название: от 2 до 255 символов';
    }
    const price = Number(values.price);
    if (values.price === '' || values.price === null || !Number.isFinite(price) || price <= 0) {
      errors.price = 'Укажите цену числом больше 0';
    }
    if (!values.publisherId) {
      errors.publisherId = 'Выберите издателя из списка';
    }
    const authorIds = values.authorIds;
    if (!Array.isArray(authorIds) || authorIds.length < 1) {
      errors.authorIds = 'Отметьте хотя бы одного автора';
    }
    if (values.publicationYear != null && values.publicationYear !== '') {
      const y = Number(values.publicationYear);
      if (
        !Number.isInteger(y) ||
        y < YEAR_BOOK_MIN ||
        y > YEAR_BOOK_MAX
      ) {
        errors.publicationYear = `Год издания: целое число от ${YEAR_BOOK_MIN} до ${YEAR_BOOK_MAX} или оставьте поле пустым`;
      }
    }
  }

  if (entity === 'publishers') {
    const name = (values.name || '').trim();
    if (!name) {
      errors.name = 'Название обязательно';
    } else if (name.length < 2 || name.length > 255) {
      errors.name = 'Название: от 2 до 255 символов';
    }
    const phone = (values.phone || '').trim();
    if (phone && !PHONE_PATTERN.test(phone)) {
      errors.phone =
        'Телефон: 10–20 символов — цифры, пробелы, +, -, скобки. Пример: +7 (999) 123-45-67';
    }
    const email = (values.email || '').trim();
    if (email && !EMAIL_PATTERN.test(email)) {
      errors.email = 'Неверный формат email. Пример: press@example.com';
    }
  }

  if (entity === 'authors') {
    const name = (values.name || '').trim();
    if (!name) {
      errors.name = 'Имя обязательно';
    } else if (name.length < 2 || name.length > 255) {
      errors.name = 'Имя: от 2 до 255 символов';
    }
    if (values.birthDate != null && values.birthDate !== '') {
      const dt = parseLocalDateOnly(values.birthDate);
      if (!dt) {
        errors.birthDate = 'Некорректная дата';
      } else {
        const y = dt.getFullYear();
        if (y < BIRTH_YEAR_MIN) {
          errors.birthDate = `Год не раньше ${BIRTH_YEAR_MIN}`;
        } else if (y > YEAR_BOOK_MAX) {
          errors.birthDate = `Год не позднее ${YEAR_BOOK_MAX}`;
        } else {
          const today = new Date();
          const t0 = new Date(
            today.getFullYear(),
            today.getMonth(),
            today.getDate(),
          );
          if (dt.getTime() > t0.getTime()) {
            errors.birthDate = 'Дата рождения не может быть в будущем';
          }
        }
      }
    }
  }

  if (entity === 'genres') {
    const name = (values.name || '').trim();
    if (!name) {
      errors.name = 'Название обязательно';
    } else if (name.length < 2 || name.length > 100) {
      errors.name = 'Название: от 2 до 100 символов';
    }
  }

  if (entity === 'reviews') {
    if (!values.bookId) {
      errors.bookId = 'Выберите книгу из списка';
    }
    const r = Number(values.rating);
    if (values.rating === '' || values.rating === null || !Number.isInteger(r) || r < 1 || r > 5) {
      errors.rating = 'Оценка: целое число от 1 до 5';
    }
    const reviewer = (values.reviewerName || '').trim();
    if (reviewer.length === 1) {
      errors.reviewerName = 'Имя читателя: минимум 2 символа или оставьте поле пустым';
    } else if (reviewer.length > 100) {
      errors.reviewerName = 'Имя читателя: не больше 100 символов';
    }
    const comment = values.comment != null ? String(values.comment) : '';
    if (comment.length > 2000) {
      errors.comment = 'Комментарий: не больше 2000 символов';
    }
  }

  return errors;
}

function renderField(field, value, refs, data, error, row) {
  const baseInputClass = `input-base mt-1.5 w-full ${error ? 'border-rose-400 focus:border-rose-500 focus:ring-rose-200' : ''}`;
  const err = fieldErrorHtml(error);

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
          `<label class="flex items-center gap-2 py-1 text-sm text-zinc-700"><input data-multiref="1" type="checkbox" name="${field.key}" value="${escapeFormAttr(String(item.id))}" class="h-4 w-4 rounded border-zinc-300 text-violet-600" ${selected.has(item.id) ? 'checked' : ''}/> ${escapeFormText(getRefLabel({ ref: field.ref }, item))}</label>`,
      )
      .join('');
    return `
      <fieldset class="space-y-1 rounded-xl border bg-zinc-50/80 p-3 ${error ? 'border-rose-300 ring-1 ring-rose-100' : 'border-zinc-200'}">
        <legend class="px-1 text-sm font-medium text-zinc-800">${escapeFormText(field.label)}${field.required ? ' *' : ''}</legend>
        <div class="max-h-40 space-y-0.5 overflow-y-auto">${boxes}</div>
        ${err}
      </fieldset>`;
  }

  if (field.type === 'select' && field.options) {
    return `
      <label class="block text-sm font-medium text-zinc-800">
        ${escapeFormText(field.label)}
        <select name="${field.key}" class="${baseInputClass}">
          <option value="">Выберите...</option>
          ${field.options
            .map(
              (opt) =>
                `<option ${String(value) === String(opt.value) ? 'selected' : ''} value="${escapeFormAttr(String(opt.value))}">${escapeFormText(opt.label)}</option>`,
            )
            .join('')}
        </select>
        ${err}
      </label>`;
  }

  if (field.type === 'ref') {
    return `
      <label class="block text-sm font-medium text-zinc-800">
        ${escapeFormText(field.label)}
        <select name="${field.key}" class="${baseInputClass}">
          <option value="">${field.allowEmpty ? 'Не выбрано' : 'Выберите...'}</option>
          ${optionsForRef(field, refs, data, value)}
        </select>
        ${err}
      </label>`;
  }

  if (field.type === 'textarea') {
    return `
      <label class="block text-sm font-medium text-zinc-800">
        ${escapeFormText(field.label)}
        <textarea name="${field.key}" rows="3" class="${baseInputClass}">${escapeFormText(value ?? '')}</textarea>
        ${err}
      </label>`;
  }

  if (field.type === 'date') {
    const valAttr = escapeFormAttr(value ?? '');
    const minD = escapeFormAttr(field.min ?? `${BIRTH_YEAR_MIN}-01-01`);
    const maxD = escapeFormAttr(field.max ?? maxBirthDateIso());
    return `
      <label class="block text-sm font-medium text-zinc-800">
        ${escapeFormText(field.label)}
        <input type="date" name="${field.key}" class="${baseInputClass}" min="${minD}" max="${maxD}" value="${valAttr}" />
        ${err}
      </label>`;
  }

  const valAttr = escapeFormAttr(value ?? '');
  const inputType = field.type || 'text';
  const numMin =
    inputType === 'number' && field.min != null
      ? ` min="${escapeFormAttr(String(field.min))}"`
      : '';
  const numMax =
    inputType === 'number' && field.max != null
      ? ` max="${escapeFormAttr(String(field.max))}"`
      : '';
  return `
    <label class="block text-sm font-medium text-zinc-800">
      ${escapeFormText(field.label)}
      <input type="${escapeFormAttr(inputType)}" name="${field.key}" value="${valAttr}" class="${baseInputClass}"${numMin}${numMax} />
      ${err}
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
  const formTopError = fieldErrorHtml(formErrors._form);
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
  <form id="entity-form" class="space-y-5" novalidate>
      ${formTopError ? `<div class="rounded-lg border border-rose-200 bg-rose-50/80 px-3 py-2">${formTopError}</div>` : ''}
      <div>
        <h3 class="text-lg font-semibold tracking-tight text-zinc-900">${row?.id != null ? 'Редактирование' : 'Создание'}: ${entityKey ? entityLabelWithIcon(entityKey, config.title) : escapeFormText(config.title ?? '')}</h3>
      </div>
      <div class="space-y-4">${fieldHtml}</div>
      <div class="flex gap-2 border-t border-zinc-200 pt-4">
        <button class="btn-primary" type="submit">${row?.id != null ? 'Сохранить' : CREATE_ACTION_LABEL}</button>
        <button class="btn-secondary" type="button" data-cancel-edit>Отмена</button>
      </div>
   </form>`;
}

export function renderDrawer({ title, subtitle = '', body, open }) {
  return `
    <div class="fixed inset-0 z-40 flex items-center justify-center p-4 sm:p-6 ${open ? '' : 'pointer-events-none'}" data-drawer-root>
      <div class="absolute inset-0 bg-zinc-900/45 backdrop-blur-[2px] transition-opacity duration-200 ${open ? 'opacity-100' : 'opacity-0'}" data-drawer-close aria-hidden="true"></div>
      <div class="relative z-10 flex max-h-[min(90vh,880px)] w-full max-w-xl flex-col overflow-hidden rounded-2xl border border-zinc-200/90 bg-white shadow-2xl shadow-zinc-900/15 ring-1 ring-black/5 transition-all duration-200 ease-out ${open ? 'translate-y-0 scale-100 opacity-100' : 'pointer-events-none translate-y-2 scale-95 opacity-0'}">
        <div class="flex shrink-0 items-start justify-between gap-4 border-b border-zinc-200/90 bg-gradient-to-r from-violet-700 via-violet-600 to-indigo-800 px-6 py-4 md:px-7 md:py-5">
          <div class="min-w-0 pr-2">
            <h2 class="text-lg font-semibold tracking-tight text-white md:text-xl">${escapeFormText(title)}</h2>
            ${subtitle ? `<p class="mt-1 text-sm text-violet-100/90">${escapeFormText(subtitle)}</p>` : ''}
          </div>
          <button class="shrink-0 rounded-lg border border-white/25 bg-white/10 px-3 py-2 text-sm font-medium text-white shadow-sm backdrop-blur-sm transition hover:bg-white/20 focus:outline-none focus:ring-2 focus:ring-white/40" type="button" data-drawer-close>Закрыть</button>
        </div>
        <div class="min-h-0 flex-1 overflow-y-auto bg-zinc-50/40 px-6 py-6 md:px-7 md:py-7">${body}</div>
      </div>
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
