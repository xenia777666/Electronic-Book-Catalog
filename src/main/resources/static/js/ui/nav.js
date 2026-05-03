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
              ? 'bg-violet-600 text-white shadow-md shadow-black/20'
              : 'text-zinc-400 hover:bg-zinc-800/80 hover:text-white';
          return `<a
            href="#/${item.key}"
            class="flex shrink-0 items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium transition-colors md:shrink ${activeClass}"
          >
            <span class="shrink-0 ${item.key === active ? 'text-white' : 'text-violet-400'}">${entityIcon(item.key)}</span>
            <span>${item.label}</span>
          </a>`;
        })
        .join('')}
    </nav>
  `;
}
