import { entityIcon } from './entityMeta.js';

export const navItems = [
  { key: 'books', label: 'Книги' },
  { key: 'publishers', label: 'Издатели' },
  { key: 'authors', label: 'Авторы' },
  { key: 'genres', label: 'Жанры' },
  { key: 'reviews', label: 'Отзывы' },
];

export function renderNav(active) {
  return `
    <nav class="flex flex-row gap-1 overflow-x-auto pb-1 md:flex-col md:overflow-visible md:pb-0">
      ${navItems
        .map((item) => {
          const activeClass =
            item.key === active
              ? 'bg-amber-500 text-zinc-950 shadow-sm'
              : 'text-zinc-400 hover:bg-zinc-800 hover:text-zinc-100';
          return `<a
            href="#/${item.key}"
            class="flex shrink-0 items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium transition-colors md:shrink ${activeClass}"
          >
            <span class="shrink-0 opacity-80">${entityIcon(item.key)}</span>
            <span>${item.label}</span>
          </a>`;
        })
        .join('')}
    </nav>
  `;
}
