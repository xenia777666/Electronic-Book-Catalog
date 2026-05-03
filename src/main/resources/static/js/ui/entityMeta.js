import { escapeFormText } from '../utils/helpers.js';

/** Иконки-контуры в духе разделов меню (книга, типография, автор, жанр, отзыв). */const ICONS = {
  books:
    '<path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/><path d="M8 7h8"/><path d="M8 11h6"/>',
  publishers:
    '<path d="M3 21h18"/><path d="M5 21V8l7-3 7 3v13"/><path d="M9 21v-6h2v6"/><path d="M13 21v-4h2v4"/>',
  authors:
    '<circle cx="12" cy="8" r="4"/><path d="M5.5 21a6.5 6.5 0 0 1 13 0"/>',
  genres:
    '<path d="M12 2H2v9.586a1 1 0 0 0 .293.707l9 9a1 1 0 0 0 1.414 0l6.586-6.586a1 1 0 0 0 0-1.414L12 2Z"/><circle cx="7.5" cy="7.5" r="1" fill="currentColor" stroke="none"/>',
  reviews:
    '<path d="M12 2l2.6 7.4h7.8l-6.3 4.6 2.4 7-6.5-4.7-6.5 4.7 2.4-7L3.6 9.4h7.8L12 2z"/>',
};

export function entityIcon(entityKey) {
  const paths = ICONS[entityKey] || ICONS.books;
  return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" class="entity-inline-icon" aria-hidden="true">${paths}</svg>`;
}

export function entityLabelWithIcon(entityKey, label) {
  return `<span class="entity-label-with-icon align-middle"><span class="text-violet-600">${entityIcon(entityKey)}</span><span>${escapeFormText(label)}</span></span>`;
}
