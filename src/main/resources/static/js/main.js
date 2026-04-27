import { api } from './api.js';
import { resetUiState, setState, state, subscribe } from './state.js';
import { formatDate, namesJoin, byId } from './utils/helpers.js';
import {
  formDataToObject,
  renderDrawer,
  renderEntityForm,
  validateForm,
} from './ui/form.js';
import { navItems, renderNav } from './ui/nav.js';
import { confirmModal, notify, infoModal } from './ui/notifications.js';
import { applyQuery, paginate, renderTable } from './ui/table.js';

const app = document.getElementById('app');
const SAVE_SUCCESS_MESSAGE = 'Сохранение выполнено';
const DELETE_SUCCESS_MESSAGE = 'Удаление выполнено';
const CREATE_ACTION_LABEL = 'Создать запись';

const entityConfigs = {
  books: {
    title: 'Книги',
    endpoint: 'books',
    columns: [
      { key: 'title', label: 'Название' },
      { key: 'isbn', label: 'ISBN' },
      {
        key: 'price',
        label: 'Цена',
        render: (row) => (row.price != null ? String(row.price) : '—'),
      },
      {
        key: 'publisher',
        label: 'Издатель',
        render: (row) => row.publisher?.name ?? '—',
      },
      {
        key: 'authors',
        label: 'Авторы',
        render: (row) => namesJoin(row.authors),
      },
      {
        key: 'genres',
        label: 'Жанры',
        render: (row) => namesJoin(row.genres),
      },
    ],
    filters: [
      {
        key: 'publisherId',
        label: 'Издатель',
        options: (refs) =>
          refs.publishers.map((p) => ({ value: p.id, label: p.name })),
      },
    ],
    fields: [
      { key: 'isbn', label: 'ISBN', required: true },
      { key: 'title', label: 'Название', required: true },
      { key: 'description', label: 'Описание', type: 'textarea' },
      { key: 'publicationYear', label: 'Год издания', type: 'number' },
      {
        key: 'price',
        label: 'Цена',
        type: 'number',
        required: true,
      },
      {
        key: 'publisherId',
        label: 'Издатель',
        type: 'ref',
        ref: 'publishers',
        required: true,
      },
      {
        key: 'authorIds',
        label: 'Авторы',
        type: 'multiref',
        ref: 'authors',
        required: true,
        storageKey: 'authors',
      },
      {
        key: 'genreIds',
        label: 'Жанры',
        type: 'multiref',
        ref: 'genres',
        required: false,
        storageKey: 'genres',
      },
    ],
    payload: (v) => ({
      isbn: v.isbn,
      title: v.title,
      description: v.description || undefined,
      publicationYear: v.publicationYear
        ? Number(v.publicationYear)
        : undefined,
      price: Number(v.price),
      publisherId: Number(v.publisherId),
      authorIds: v.authorIds,
      genreIds: Array.isArray(v.genreIds) ? v.genreIds : [],
    }),
  },
  publishers: {
    title: 'Издатели',
    endpoint: 'publishers',
    columns: [
      { key: 'name', label: 'Название' },
      { key: 'email', label: 'Email' },
      { key: 'phone', label: 'Телефон' },
    ],
    filters: [],
    fields: [
      { key: 'name', label: 'Название', required: true },
      { key: 'address', label: 'Адрес', type: 'textarea' },
      { key: 'phone', label: 'Телефон' },
      { key: 'email', label: 'Email', type: 'email' },
    ],
    payload: (v) => v,
  },
  authors: {
    title: 'Авторы',
    endpoint: 'authors',
    columns: [
      { key: 'name', label: 'Имя' },
      { key: 'biography', label: 'Биография' },
      {
        key: 'birthDate',
        label: 'Дата рождения',
        render: (row) => formatDate(row.birthDate),
      },
    ],
    filters: [],
    fields: [
      { key: 'name', label: 'Имя', required: true },
      { key: 'biography', label: 'Биография', type: 'textarea' },
      { key: 'birthDate', label: 'Дата рождения', type: 'date' },
    ],
    payload: (v) => v,
  },
  genres: {
    title: 'Жанры',
    endpoint: 'genres',
    columns: [
      { key: 'name', label: 'Название' },
      { key: 'description', label: 'Описание' },
    ],
    filters: [],
    fields: [
      { key: 'name', label: 'Название', required: true },
      { key: 'description', label: 'Описание', type: 'textarea' },
    ],
    payload: (v) => v,
  },
  reviews: {
    title: 'Отзывы',
    endpoint: 'reviews',
    columns: [
      {
        key: 'bookId',
        label: 'Книга',
        render: (row, refs) =>
          byId(refs.books, row.bookId)?.title ?? row.bookId ?? '—',
      },
      { key: 'reviewerName', label: 'Читатель' },
      { key: 'rating', label: 'Оценка' },
      {
        key: 'comment',
        label: 'Комментарий',
        render: (row) =>
          row.comment
            ? String(row.comment).slice(0, 80) +
              (row.comment.length > 80 ? '…' : '')
            : '—',
      },
    ],
    filters: [],
    fields: [
      {
        key: 'bookId',
        label: 'Книга',
        type: 'ref',
        ref: 'books',
        required: true,
      },
      { key: 'reviewerName', label: 'Имя читателя' },
      {
        key: 'rating',
        label: 'Оценка (1–5)',
        type: 'number',
        required: true,
      },
      { key: 'comment', label: 'Комментарий', type: 'textarea' },
    ],
    payload: (v) => ({
      ...v,
      bookId: Number(v.bookId),
      rating: Number(v.rating),
    }),
  },
};

function parseRoute() {
  const hash = window.location.hash.replace('#/', '') || 'books';
  const key = navItems.find((item) => item.key === hash)?.key;
  return key || 'books';
}

function openEntityDrawer(editingId = null) {
  setState((s) => {
    s.ui.editingId = editingId;
    s.ui.drawerOpen = true;
    s.ui.formErrors = {};
  });
}

function closeDrawer() {
  setState((s) => {
    s.ui.drawerOpen = false;
    s.ui.editingId = null;
    s.ui.formErrors = {};
  });
}

function normalizeBookRow(b) {
  return {
    ...b,
    publisherId: b.publisher?.id ?? null,
  };
}

async function loadRefs(force = false) {
  if (
    !force &&
    state.refs.publishers.length &&
    state.refs.authors.length &&
    state.refs.genres.length
  ) {
    return;
  }
  const [pubs, auths, gens, booksRes] = await Promise.all([
    api.publishers.list(),
    api.authors.list(),
    api.genres.list(),
    api.books.list({ page: 0, size: 500 }),
  ]);
  setState((s) => {
    s.refs.publishers = pubs.items;
    s.refs.authors = auths.items;
    s.refs.genres = gens.items;
    s.refs.books = booksRes.items.map(normalizeBookRow);
  });
}

async function loadEntity(entity, force = false) {
  if (entity === 'books' && state.ui.booksServerFilter) {
    return;
  }
  if (
    !force &&
    state.data[entity].length > 0 &&
    entity !== 'books' &&
    state.meta[entity].pageable === false
  ) {
    return;
  }

  try {
    if (entity === 'books') {
      const meta = state.meta.books;
      const result = await api.books.list({
        page: meta.page,
        size: meta.size,
        sort: 'title,asc',
      });
      setState((s) => {
        s.data.books = result.items.map(normalizeBookRow);
        s.meta.books = {
          ...s.meta.books,
          ...result.meta,
          pageable: true,
        };
      });
      return;
    }

    const endpoint = entityConfigs[entity]?.endpoint;
    const result = await api[endpoint].list();
    setState((s) => {
      s.data[entity] = result.items;
      s.meta[entity] = {
        ...s.meta[entity],
        totalElements: result.items.length,
        totalPages: Math.max(
          1,
          Math.ceil(result.items.length / s.meta[entity].size),
        ),
        pageable: false,
      };
    });
  } catch (error) {
    notify(error.message, 'error');
  }
}

function getVisibleRows(entity) {
  const config = entityConfigs[entity];
  const meta = state.meta[entity];
  const queried = applyQuery(
    state.data[entity],
    config,
    state.ui,
    state.refs,
    state.data,
  );
  if (meta.pageable) {
    return {
      rows: queried,
      allFiltered: queried,
      meta: { ...meta, totalElements: meta.totalElements },
    };
  }
  const paged = paginate(queried, meta.page, meta.size);
  return {
    rows: paged,
    allFiltered: queried,
    meta: {
      ...meta,
      totalElements: queried.length,
      totalPages: Math.max(1, Math.ceil(queried.length / meta.size)),
    },
  };
}

function escAttr(v) {
  return String(v ?? '').replace(/&/g, '&amp;').replace(/"/g, '&quot;');
}

function booksServerFilterPanel() {
  const f = state.ui.bookFilter;
  return `<div class="card-base p-4 mb-4">
    <div class="mb-3 text-xs font-semibold uppercase tracking-wide text-zinc-500">Книги — поиск API</div>
    <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
      <label class="text-sm text-zinc-700">Автор<input class="input-base mt-1" data-bf="author" value="${escAttr(f.author)}" /></label>
      <label class="text-sm text-zinc-700">Жанр<input class="input-base mt-1" data-bf="genre" value="${escAttr(f.genre)}" /></label>
      <label class="text-sm text-zinc-700">Издатель<input class="input-base mt-1" data-bf="publisher" value="${escAttr(f.publisher)}" /></label>
      <label class="text-sm text-zinc-700">Цена от<input class="input-base mt-1" data-bf="minPrice" value="${escAttr(f.minPrice)}" /></label>
      <label class="text-sm text-zinc-700">Цена до<input class="input-base mt-1" data-bf="maxPrice" value="${escAttr(f.maxPrice)}" /></label>
      <label class="text-sm text-zinc-700">Рейтинг от<input class="input-base mt-1" data-bf="minRating" value="${escAttr(f.minRating)}" /></label>
    </div>
    <div class="mt-4 flex flex-wrap gap-2">
      <button type="button" class="btn-primary" data-books-search>Найти</button>
      <button type="button" class="btn-secondary" data-books-search-reset>Сброс</button>
    </div>
  </div>`;
}

function shellTemplate(content) {
  return `
    <div class="flex min-h-screen flex-col bg-zinc-950">
      <header class="shrink-0 border-b border-zinc-800 bg-zinc-900 px-4 py-3 md:px-6">
        <div class="mx-auto flex max-w-[1700px] items-baseline gap-3">
          <h1 class="text-base font-bold tracking-tight text-amber-400 md:text-lg">Каталог</h1>
        </div>
      </header>
      <main class="mx-auto flex min-h-0 w-full max-w-[1700px] flex-1 flex-col md:flex-row">
        <aside class="shrink-0 border-zinc-800 bg-zinc-900 px-3 py-3 md:w-56 md:border-r md:py-5">
          <div id="shell-nav"></div>
        </aside>
        <section class="min-h-0 min-w-0 flex-1 overflow-y-auto bg-zinc-100 p-4 md:p-8">
          <div id="shell-content">${content}</div>
        </section>
      </main>
    </div>`;
}

function ensureShell(content = '') {
  if (!document.getElementById('shell-content')) {
    app.innerHTML = shellTemplate(content);
    return;
  }
  const navRoot = document.getElementById('shell-nav');
  const contentRoot = document.getElementById('shell-content');
  if (navRoot) {
    navRoot.innerHTML = renderNav(state.route);
  }
  if (contentRoot) {
    contentRoot.innerHTML = content;
  }
}

function renderEntityDetails(entity, row) {
  if (!row) {
    return '<p class="text-zinc-500">Нет данных.</p>';
  }
  if (entity === 'books') {
    const revs = row.reviews?.length
      ? `<ul class="list-disc pl-5 mt-1">${row.reviews
          .map(
            (r) =>
              `<li>${r.rating}★ ${r.reviewerName || ''}: ${r.comment || '—'}</li>`,
          )
          .join('')}</ul>`
      : '<p class="text-zinc-500">Нет отзывов</p>';
    return `<div class="space-y-2 text-sm">
      <p><span class="text-zinc-500">ISBN</span> ${row.isbn}</p>
      <p><span class="text-zinc-500">Название</span> ${row.title}</p>
      <p><span class="text-zinc-500">Издатель</span> ${row.publisher?.name ?? '—'}</p>
      <p><span class="text-zinc-500">Авторы</span> ${namesJoin(row.authors)}</p>
      <p><span class="text-zinc-500">Жанры</span> ${namesJoin(row.genres)}</p>
      <div><span class="text-zinc-500">Отзывы</span>${revs}</div>
    </div>`;
  }
  if (entity === 'reviews') {
    return `<pre class="whitespace-pre-wrap text-xs bg-slate-50 p-3 rounded-lg">${JSON.stringify(row, null, 2)}</pre>`;
  }
  return `<pre class="whitespace-pre-wrap text-xs bg-slate-50 p-3 rounded-lg">${JSON.stringify(row, null, 2)}</pre>`;
}

function render() {
  const config = entityConfigs[state.route];
  const { rows, allFiltered, meta } = getVisibleRows(state.route);
  const editingRow = byId(state.data[state.route], state.ui.editingId);
  const canEdit = true;
  const canDelete = true;

  const drawerBody = renderEntityForm(
    config,
    state.refs,
    state.data,
    editingRow,
    state.route,
    state.ui.formErrors,
  );

  const drawerTitle = `${state.ui.editingId ? 'Изменить' : 'Создать'} · ${config.title}`;

  const bookExtras =
    state.route === 'books' ? booksServerFilterPanel() : '';

  const content = `
    <section class="space-y-4">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h2 class="text-xl font-semibold tracking-tight text-zinc-900">${config.title}</h2>
        ${canEdit ? `<button type="button" data-create-entity class="btn-primary shrink-0" aria-label="${CREATE_ACTION_LABEL}">${CREATE_ACTION_LABEL}</button>` : ''}
      </div>
      ${bookExtras}
      ${renderTable({
        entity: state.route,
        config,
        rows,
        refs: state.refs,
        data: state.data,
        ui: state.ui,
        meta,
        pageableFromApi:
          state.route === 'books' && !state.ui.booksServerFilter,
        canEdit,
        canDelete,
        loading: false,
      })}
      ${renderDrawer({
        title: drawerTitle,
        body: drawerBody,
        open: state.ui.drawerOpen,
      })}
    </section>`;

  ensureShell(content);
  const navRoot = document.getElementById('shell-nav');
  if (navRoot) {
    navRoot.innerHTML = renderNav(state.route);
  }
  bindEntityHandlers(config, allFiltered, rows, meta);
  bindBooksFilterHandlers();
}

function bindBooksFilterHandlers() {
  document.querySelectorAll('[data-bf]').forEach((el) => {
    el.addEventListener('input', (e) => {
      const key = e.target.getAttribute('data-bf');
      if (!key) {
        return;
      }
      state.ui.bookFilter[key] = e.target.value;
    });
  });
  document.querySelector('[data-books-search]')?.addEventListener('click', async () => {
    try {
      const f = state.ui.bookFilter;
      const { items } = await api.books.search({
        author: f.author || undefined,
        genre: f.genre || undefined,
        publisher: f.publisher || undefined,
        minPrice: f.minPrice || undefined,
        maxPrice: f.maxPrice || undefined,
        minRating: f.minRating || undefined,
      });
      setState((s) => {
        s.ui.booksServerFilter = true;
        s.data.books = items.map(normalizeBookRow);
        s.meta.books.pageable = false;
        s.meta.books.page = 0;
        s.meta.books.totalElements = items.length;
        s.meta.books.totalPages = Math.max(
          1,
          Math.ceil(items.length / s.meta.books.size),
        );
      });
      notify(`Найдено: ${items.length}`, 'success');
    } catch (e) {
      notify(e.message, 'error');
    }
  });
  document
    .querySelector('[data-books-search-reset]')
    ?.addEventListener('click', async () => {
      setState((s) => {
        s.ui.booksServerFilter = false;
        s.ui.bookFilter = {
          author: '',
          genre: '',
          publisher: '',
          minPrice: '',
          maxPrice: '',
          minRating: '',
        };
        s.meta.books.page = 0;
        s.meta.books.pageable = true;
      });
      await loadEntity('books', true);
      notify('Список обновлён', 'info');
    });
}

function bindEntityHandlers(config, allFiltered, displayedRows, meta) {
  document.querySelectorAll('[data-drawer-close]').forEach((el) =>
    el.addEventListener('click', closeDrawer),
  );
  document
    .querySelectorAll('[data-create-entity]')
    .forEach((el) =>
      el.addEventListener('click', () => openEntityDrawer(null)),
    );

  document.getElementById('entity-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const raw = formDataToObject(e.target);
    const errors = validateForm(state.route, raw);
    if (Object.keys(errors).length) {
      setState((s) => {
        s.ui.formErrors = errors;
      });
      return;
    }

    const payload = config.payload(raw);
    const route = state.route;
    const previousData = [...state.data[route]];

    try {
      if (state.ui.editingId) {
        const id = state.ui.editingId;
        const saved = await api[route].update(id, payload);
        setState((s) => {
          const norm =
            route === 'books' ? normalizeBookRow(saved) : saved;
          const idx = s.data[route].findIndex((item) => item.id === id);
          if (idx > -1) {
            s.data[route][idx] = norm;
          }
        });
        notify(SAVE_SUCCESS_MESSAGE, 'success');
      } else {
        const created = await api[route].create(payload);
        setState((s) => {
          const norm =
            route === 'books' ? normalizeBookRow(created) : created;
          s.data[route].unshift(norm);
          if (!s.meta[route].pageable) {
            s.meta[route].totalElements += 1;
          }
        });
        notify(SAVE_SUCCESS_MESSAGE, 'success');
      }
      closeDrawer();
      if (route === 'books' || route === 'publishers') {
        await loadRefs(true);
      }
    } catch (error) {
      setState((s) => {
        s.data[route] = previousData;
      });
      notify(error.message, 'error');
    }
  });

  document
    .querySelector('[data-cancel-edit]')
    ?.addEventListener('click', closeDrawer);

  document.querySelectorAll('[data-view-id]').forEach((element) => {
    element.addEventListener('click', (e) => {
      const rowId = Number(e.currentTarget.dataset.viewId);
      const row = byId(state.data[state.route], rowId);
      infoModal({
        title: `Просмотр: ${config.title}`,
        body: renderEntityDetails(state.route, row),
      });
    });
  });

  document.querySelectorAll('[data-filter-key]').forEach((element) => {
    element.addEventListener('change', (e) => {
      const key = e.currentTarget.dataset.filterKey;
      if (!key) {
        return;
      }
      setState((s) => {
        s.ui.filters[key] = e.currentTarget.value;
        s.meta[s.route].page = 0;
        s.ui.selectedIds = new Set();
      });
    });
  });

  document.querySelectorAll('[data-sort]').forEach((element) => {
    element.addEventListener('click', (e) => {
      const key = e.currentTarget.dataset.sort;
      if (!key) {
        return;
      }
      setState((s) => {
        const same = s.ui.sort.key === key;
        s.ui.sort = {
          key,
          dir: same && s.ui.sort.dir === 'asc' ? 'desc' : 'asc',
        };
      });
    });
  });

  document.querySelectorAll('[data-edit-id]').forEach((element) => {
    element.addEventListener('click', (e) =>
      openEntityDrawer(Number(e.currentTarget.dataset.editId)),
    );
  });

  document.querySelectorAll('[data-delete-id]').forEach((element) => {
    element.addEventListener('click', (e) => {
      const id = Number(e.currentTarget.dataset.deleteId);
      const route = state.route;
      confirmModal({
        title: 'Подтвердите удаление',
        description: 'Эту операцию нельзя отменить.',
        onConfirm: async () => {
          const previous = [...state.data[route]];
          setState((s) => {
            s.data[route] = s.data[route].filter((item) => item.id !== id);
            s.ui.selectedIds.delete(id);
          });
          try {
            await api[route].remove(id);
            notify(DELETE_SUCCESS_MESSAGE, 'success');
            await loadRefs(true);
            if (route === 'books' && state.meta.books.pageable) {
              await loadEntity('books', true);
            }
          } catch (error) {
            setState((s) => {
              s.data[route] = previous;
            });
            notify(error.message, 'error');
          }
        },
      });
    });
  });

  document.querySelectorAll('[data-select-id]').forEach((element) => {
    element.addEventListener('change', (e) => {
      const id = Number(e.currentTarget.dataset.selectId);
      setState((s) => {
        if (e.currentTarget.checked) {
          s.ui.selectedIds.add(id);
        } else {
          s.ui.selectedIds.delete(id);
        }
      });
    });
  });

  document.getElementById('select-all')?.addEventListener('change', (e) => {
    setState((s) => {
      if (e.target.checked) {
        displayedRows.forEach((row) => s.ui.selectedIds.add(row.id));
      } else {
        displayedRows.forEach((row) => s.ui.selectedIds.delete(row.id));
      }
    });
  });

  document.getElementById('bulk-delete')?.addEventListener('click', () => {
    if (!state.ui.selectedIds.size) {
      notify('Сначала отметьте записи', 'info');
      return;
    }
    const route = state.route;
    confirmModal({
      title: 'Массовое удаление',
      description: `Будут удалены записи: ${state.ui.selectedIds.size}`,
      onConfirm: async () => {
        const ids = [...state.ui.selectedIds];
        const previous = [...state.data[route]];
        setState((s) => {
          s.data[route] = s.data[route].filter(
            (item) => !s.ui.selectedIds.has(item.id),
          );
          s.ui.selectedIds = new Set();
        });
        const results = await Promise.allSettled(
          ids.map((id) => api[route].remove(id)),
        );
        const failed = results.filter((r) => r.status === 'rejected').length;
        if (failed) {
          setState((s) => {
            s.data[route] = previous;
          });
          notify(`Удаление прервано, ошибок: ${failed}`, 'error');
        } else {
          notify('Выбранные записи удалены', 'success');
          await loadRefs(true);
          if (route === 'books' && state.meta.books.pageable) {
            await loadEntity('books', true);
          }
        }
      },
    });
  });

  document.querySelectorAll('[data-page-action]').forEach((element) => {
    element.addEventListener('click', async (e) => {
      const action = e.currentTarget.dataset.pageAction;
      const canNext = meta.page + 1 < meta.totalPages;
      const route = state.route;
      setState((s) => {
        if (action === 'prev' && s.meta[route].page > 0) {
          s.meta[route].page -= 1;
        }
        if (action === 'next' && canNext) {
          s.meta[route].page += 1;
        }
      });
      if (route === 'books' && state.meta.books.pageable && !state.ui.booksServerFilter) {
        await loadEntity('books', true);
      }
    });
  });
}

async function routeChanged() {
  const route = parseRoute();
  setState((s) => {
    s.route = route;
  });

  try {
    await loadRefs();
    if (entityConfigs[route]) {
      await loadEntity(route, true);
    }
  } catch (error) {
    notify(error.message, 'error');
  }

  resetUiState();
}

subscribe(render);
window.addEventListener('hashchange', () => routeChanged());

if (!window.location.hash) {
  window.location.hash = '#/books';
} else {
  routeChanged();
}

render();
