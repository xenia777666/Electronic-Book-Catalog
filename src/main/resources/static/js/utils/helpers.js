export function byId(items, id) {
  if (!items || id == null) {
    return undefined;
  }
  return items.find((item) => item.id === id);
}

export function average(values) {
  if (!values.length) {
    return 0;
  }
  return values.reduce((a, b) => a + b, 0) / values.length;
}

export function formatDate(value) {
  if (!value) {
    return '—';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return new Intl.DateTimeFormat('ru-RU', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(date);
}

export function namesJoin(list, key = 'name') {
  if (!list?.length) {
    return '—';
  }
  return list.map((x) => x[key]).join(', ');
}

/** Как на сервере BookDto: только цифры и X на контрольной позиции, без дефисов. */
export const ISBN_BACKEND_PATTERN = /^(97[8-9])?\d{9}[\dX]$/;

export function normalizeIsbn(value) {
  return String(value ?? '')
    .replace(/[\s-]/g, '')
    .toUpperCase();
}

/** Согласовано с BookDto и требованиями к UI. */
export const YEAR_BOOK_MIN = 1455;
export const YEAR_BOOK_MAX = 2026;

/** Нижняя граница года в дате рождения (произвольно разумная). */
export const BIRTH_YEAR_MIN = 1000;

/**
 * @param {unknown} value строка YYYY-MM-DD
 * @returns {Date|null} локальная полночь этой даты или null
 */
export function parseLocalDateOnly(value) {
  if (value == null || value === '') {
    return null;
  }
  const s = String(value).trim();
  const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(s);
  if (!m) {
    return null;
  }
  const y = Number(m[1]);
  const mo = Number(m[2]) - 1;
  const d = Number(m[3]);
  const dt = new Date(y, mo, d);
  if (dt.getFullYear() !== y || dt.getMonth() !== mo || dt.getDate() !== d) {
    return null;
  }
  return dt;
}

function toIsoDateLocal(d) {
  const y = d.getFullYear();
  const mo = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${mo}-${day}`;
}

/** Верхняя граница для input type=date: не позже сегодня и не позже 31.12.YEAR_BOOK_MAX. */
export function maxBirthDateIso() {
  const today = new Date();
  const t0 = new Date(today.getFullYear(), today.getMonth(), today.getDate());
  const cap = new Date(YEAR_BOOK_MAX, 11, 31);
  const maxD = t0.getTime() <= cap.getTime() ? t0 : cap;
  return toIsoDateLocal(maxD);
}

/** Текст в HTML (textarea, ошибки, подписи). */
export function escapeFormText(s) {
  return String(s ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

/** Значение в HTML-атрибуте в кавычках. */
export function escapeFormAttr(s) {
  return String(s ?? '')
    .replace(/&/g, '&amp;')
    .replace(/"/g, '&quot;');
}
