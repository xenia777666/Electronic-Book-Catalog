import { entityIcon } from './entityMeta.js';

export const navItems = [
  { key: 'dashboard', label: 'Панель' },
  { key: 'books', label: 'Книги' },
  { key: 'publishers', label: 'Издатели' },
  { key: 'authors', label: 'Авторы' },
  { key: 'genres', label: 'Жанры' },
  { key: 'reviews', label: 'Отзывы' },
];

export function renderNav(active) {
  return `
    <nav class="sticky top-6 flex flex-col gap-1.5">
      <p class="px-3 pb-1 text-xs font-semibold uppercase tracking-wider text-slate-400">Навигация</p>
      ${navItems
        .map((item) => {
          const activeClass =
            item.key === active
              ? 'bg-indigo-600/95 text-white shadow-sm ring-1 ring-indigo-600/20'
              : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100/80';
          return `<a
            href="#/${item.key}"
            title="${item.label}"
            class="group flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500/60 ${activeClass}"
          >
            <span class="shrink-0 ${item.key === active ? 'text-white' : 'text-slate-400 group-hover:text-slate-700'}">${entityIcon(item.key)}</span>
            <span>${item.label}</span>
          </a>`;
        })
        .join('')}
    </nav>
  `;
}
