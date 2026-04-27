const ICONS = {
  books:
    '<path d="M4 19.5V5.75A2.75 2.75 0 0 1 6.75 3H18"/><path d="M6 20h12"/><path d="M6 3v17"/><path d="M10 7h6"/><path d="M10 11h6"/>',
  publishers:
    '<path d="M3 7h18"/><path d="M6 7v12"/><path d="M18 7v12"/><path d="M6 19h12"/><rect x="8" y="3" width="8" height="4" rx="1"/>',
  authors:
    '<circle cx="12" cy="7" r="4"/><path d="M5.5 20a6.5 6.5 0 0 1 13 0"/>',
  genres:
    '<path d="M4 20h16"/><path d="M6 16l4-8 4 5 4-9"/><circle cx="6" cy="16" r="1"/><circle cx="10" cy="8" r="1"/><circle cx="14" cy="13" r="1"/><circle cx="18" cy="4" r="1"/>',
  reviews:
    '<path d="M12 20l-3.5-2.1A8 8 0 1 1 20 11"/><path d="M12 20v-8"/>',
};

export function entityIcon(entityKey) {
  const paths = ICONS[entityKey] || ICONS.books;
  return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" class="entity-inline-icon" aria-hidden="true">${paths}</svg>`;
}

export function entityLabelWithIcon(entityKey, label) {
  return `<span class="entity-label-with-icon align-middle">${entityIcon(entityKey)}<span>${label}</span></span>`;
}
