import { api } from './api.js';
import { resetUiState, setState, state, subscribe } from './state.js';
import {
  formatDate,
  namesJoin,
  byId,
  normalizeIsbn,
  escapeFormText,
  escapeFormAttr,
  YEAR_BOOK_MIN,
  YEAR_BOOK_MAX,
} from './utils/helpers.js';
import {
  formDataToObject,
  mergeEditingRowWithDraft,
  renderDrawer,
  renderEntityForm,
  renderInlineFieldError,
  validateBookSearchFilters,
  validateForm,
} from './ui/form.js';
import { navItems, renderNav } from './ui/nav.js';
import { confirmModal, notify } from './ui/notifications.js';
import { applyQuery, paginate, renderTable } from './ui/table.js';

function getAppRoot() {
  return document.getElementById('app');
}

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
        sortValue: (row) => row.publisher?.name ?? '',
      },
      {
        key: 'authors',
        label: 'Авторы',
        render: (row) => namesJoin(row.authors),
        sortValue: (row) => namesJoin(row.authors),
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
      {
        key: 'isbn',
        label: 'ISBN',
        required: true,
        hint: 'Десяти(тринадцати-)значное целое число.',
      },
      {
        key: 'title',
        label: 'Название',
        required: true,
        hint: 'От 2 до 255 символов.',
      },
      { key: 'description', label: 'Описание', type: 'textarea' },
      {
        key: 'publicationYear',
        label: 'Год издания',
        type: 'number',
        min: YEAR_BOOK_MIN,
        max: YEAR_BOOK_MAX,
        hint: `Целое число от ${YEAR_BOOK_MIN} до ${YEAR_BOOK_MAX}`,
      },
      {
        key: 'price',
        label: 'Цена',
        type: 'number',
        required: true,
        hint: 'Число больше нуля.',
      },
      {
        key: 'publisherId',
        label: 'Издатель',
        type: 'ref',
        ref: 'publishers',
        required: true,
        hint: 'Обязательный выбор из списка.',
      },
      {
        key: 'authorIds',
        label: 'Авторы',
        type: 'multiref',
        ref: 'authors',
        required: true,
        storageKey: 'authors',
        hint: 'Отметьте хотя бы одного автора.',
      },
      {
        key: 'genreIds',
        label: 'Жанры',
        type: 'multiref',
        ref: 'genres',
        required: false,
        storageKey: 'genres',
        hint: 'По желанию, можно несколько.',
      },
    ],
    payload: (v) => ({
      isbn: normalizeIsbn(v.isbn),
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
      {
        key: 'name',
        label: 'Название',
        required: true,
        hint: 'От 2 до 255 символов.',
      },
      { key: 'address', label: 'Адрес', type: 'textarea' },
      {
        key: 'phone',
        label: 'Телефон',
        hint: 'Необязательно. 10-20 символов: цифры, пробелы, +, -, скобки.',
      },
      {
        key: 'email',
        label: 'Email',
        type: 'email',
        hint: 'Необязательно. Формат: имя@домен.зона.ы',
      },
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
      {
        key: 'name',
        label: 'Имя',
        required: true,
        hint: 'От 2 до 255 символов.',
      },
      { key: 'biography', label: 'Биография', type: 'textarea' },
      {
        key: 'birthDate',
        label: 'Дата рождения',
        type: 'date',
        hint: `Не позже сегодняшнего дня; год не позднее ${YEAR_BOOK_MAX}`,
      },
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
      {
        key: 'name',
        label: 'Название',
        required: true,
        hint: 'От 2 до 100 символов.',
      },
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
        hint: 'Выберите книгу из списка.',
      },
      {
        key: 'reviewerName',
        label: 'Имя читателя',
        hint: 'Необязательно. От 2 до 100 символов.',
      },
      {
        key: 'rating',
        label: 'Оценка (1–5)',
        type: 'number',
        required: true,
        hint: 'Целое число от 1 до 5.',
      },
      {
        key: 'comment',
        label: 'Комментарий',
        type: 'textarea',
        hint: 'Не длиннее 2000 символов.',
      },
    ],
    payload: (v) => ({
      bookId: Number(v.bookId),
      rating: Number(v.rating),
      reviewerName: (v.reviewerName || '').trim() || undefined,
      comment: (v.comment || '').trim() || undefined,
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
    s.ui.formDraft = null;
  });
}

function closeDrawer() {
  setState((s) => {
    s.ui.drawerOpen = false;
    s.ui.editingId = null;
    s.ui.formErrors = {};
    s.ui.formDraft = null;
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
        sort: 'publisher.name,asc',
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

function bookFilterInputClass(key, errors) {
  const err = errors[key];
  return `input-base mt-1.5 w-full${err ? ' border-rose-400 focus:border-rose-500 focus:ring-rose-200' : ''}`;
}

function booksServerFilterPanel() {
  const f = state.ui.bookFilter;
  const err = state.ui.bookFilterErrors || {};
  const topBlock = err._form
    ? `<div class="mb-4 rounded-lg border border-rose-200 bg-rose-50/80 px-3 py-2">${renderInlineFieldError(err._form)}</div>`
    : '';

  const field = (key, label) => `
    <label class="block text-sm font-medium text-zinc-800">
      ${escapeFormText(label)}
      <input type="text" name="${key}" class="${bookFilterInputClass(key, err)}" data-bf="${key}" value="${escapeFormAttr(f[key] ?? '')}" autocomplete="off" />
      ${renderInlineFieldError(err[key])}
    </label>`;

  return `<div class="card-base p-4 mb-4">
    <div id="books-filter-panel" class="space-y-4">
      ${topBlock}
      <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        ${field('author', 'Автор')}
        ${field('genre', 'Жанр')}
        ${field('title', 'Название')}
        ${field('minPrice', 'Цена от')}
        ${field('maxPrice', 'Цена до')}
        ${field('minRating', 'Рейтинг от')}
      </div>
      <div class="flex flex-wrap gap-2 border-t border-zinc-200 pt-4">
        <button type="button" class="btn-primary" data-books-search>Найти</button>
        <button type="button" class="btn-secondary" data-books-search-reset>Сброс</button>
      </div>
    </div>
  </div>`;
}

function shellTemplate(content) {
  return `
    <div class="flex min-h-screen flex-col bg-zinc-100">
      <header class="shrink-0 border-b border-zinc-800/80 bg-zinc-900 px-4 py-3.5 md:px-6">
        <div class="mx-auto flex max-w-[1700px] flex-col gap-0.5 sm:flex-row sm:items-end sm:justify-between sm:gap-4">
          <h1 class="text-lg font-semibold tracking-tight text-white md:text-xl">Каталог</h1>
          <p class="text-xs font-medium text-zinc-400 sm:text-sm">Электронный каталог книг</p>
        </div>
      </header>
      <main class="mx-auto flex min-h-0 w-full max-w-[1700px] flex-1 flex-col md:flex-row">
        <aside class="shrink-0 border-zinc-200 bg-zinc-900 px-3 py-3 md:w-56 md:border-r md:border-zinc-800 md:py-6">
          <div id="shell-nav"></div>
        </aside>
        <section class="min-h-0 min-w-0 flex-1 overflow-y-auto bg-gradient-to-b from-zinc-100 to-zinc-200/80 p-4 md:p-8">
          <div id="shell-content" class="mx-auto max-w-6xl">${content}</div>
        </section>
      </main>
    </div>`;
}

function ensureShell(content = '') {
  const app = getAppRoot();
  if (!app) {
    return;
  }
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

function render() {
  const routeKey =
    state.route in entityConfigs ? state.route : 'books';
  const config = entityConfigs[routeKey];
  const { rows, allFiltered, meta } = getVisibleRows(routeKey);
  const editingRow = byId(state.data[routeKey], state.ui.editingId);
  const formRow = mergeEditingRowWithDraft(
    editingRow,
    state.ui.formDraft,
    routeKey,
  );
  const canEdit = true;
  const canDelete = true;

  const drawerBody = renderEntityForm(
    config,
    state.refs,
    state.data,
    formRow,
    routeKey,
    state.ui.formErrors,
  );

  const drawerTitle = `${state.ui.editingId ? 'Изменить' : 'Создать'} · ${config.title}`;

  const bookExtras =
    routeKey === 'books' ? booksServerFilterPanel() : '';

  const content = `
    <section class="space-y-5">
      <div class="flex flex-col gap-3 border-b border-zinc-200/90 pb-4 sm:flex-row sm:items-center sm:justify-between">
        <h2 class="text-xl font-semibold tracking-tight text-zinc-900 md:text-2xl">${config.title}</h2>
        ${canEdit ? `<button type="button" data-create-entity class="btn-primary shrink-0" aria-label="${CREATE_ACTION_LABEL}">${CREATE_ACTION_LABEL}</button>` : ''}
      </div>
      ${bookExtras}
      ${renderTable({
        entity: routeKey,
        config,
        rows,
        refs: state.refs,
        data: state.data,
        ui: state.ui,
        meta,
        pageableFromApi:
          routeKey === 'books' && !state.ui.booksServerFilter,
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
    navRoot.innerHTML = renderNav(routeKey);
  }
  bindEntityHandlers(config, allFiltered, rows, meta);
  bindBooksFilterHandlers();
}

function renderWithErrorBoundary() {
  try {
    render();
  } catch (err) {
    console.error(err);
    const box = getAppRoot();
    if (!box) {
      return;
    }
    const msg = err instanceof Error ? err.message : String(err);
    const stack = err instanceof Error && err.stack ? err.stack : '';
    box.innerHTML = `<div class="mx-auto max-w-xl p-6 font-sans text-zinc-800">
      <h1 class="text-lg font-semibold text-rose-700">Не удалось отрисовать интерфейс</h1>
      <p class="mt-2 text-sm text-zinc-600">Обновите страницу (Ctrl+F5). Откройте консоль (F12) для подробностей.</p>
      <pre class="mt-4 overflow-auto rounded-lg bg-zinc-100 p-3 text-xs text-rose-900">${escapeFormText(msg)}\n${escapeFormText(stack)}</pre>
    </div>`;
  }
}

function bindBooksFilterHandlers() {
  document.querySelectorAll('[data-bf]').forEach((el) => {
    el.addEventListener('input', (e) => {
      const input = e.target;
      const key = input.dataset.bf;
      if (!key) {
        return;
      }
      state.ui.bookFilter[key] = input.value;
      const errs = state.ui.bookFilterErrors;
      if (errs && Object.keys(errs).length > 0) {
        const selStart = input.selectionStart;
        const selEnd = input.selectionEnd;
        setState((s) => {
          const next = { ...s.ui.bookFilterErrors };
          delete next[key];
          delete next._form;
          s.ui.bookFilterErrors = next;
        });
        requestAnimationFrame(() => {
          const again = document.querySelector(`input[data-bf="${key}"]`);
          if (again) {
            again.focus();
            try {
              if (selStart != null && selEnd != null) {
                again.setSelectionRange(selStart, selEnd);
              }
            } catch {
              /* ignore */
            }
          }
        });
      }
    });
  });
  document.querySelector('[data-books-search]')?.addEventListener('click', async () => {
    const f = state.ui.bookFilter;
    const validation = validateBookSearchFilters(f);
    if (Object.keys(validation).length) {
      setState((s) => {
        s.ui.bookFilterErrors = validation;
      });
      return;
    }

    setState((s) => {
      s.ui.bookFilterErrors = {};
    });

    try {
      const { items } = await api.books.search({
        author: f.author || undefined,
        genre: f.genre || undefined,
        title: f.title || undefined,
        minPrice: f.minPrice || undefined,
        maxPrice: f.maxPrice || undefined,
        minRating: f.minRating || undefined,
      });
      setState((s) => {
        s.ui.booksServerFilter = true;
        s.ui.bookFilterErrors = {};
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
      const msg = e?.message != null ? String(e.message) : 'Ошибка поиска';
      setState((s) => {
        s.ui.bookFilterErrors = { _form: msg };
      });
      notify(msg, 'error');
    }
  });
  document
    .querySelector('[data-books-search-reset]')
    ?.addEventListener('click', async () => {
      setState((s) => {
        s.ui.booksServerFilter = false;
        s.ui.bookFilterErrors = {};
        s.ui.bookFilter = {
          author: '',
          genre: '',
          title: '',
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
        s.ui.formDraft = raw;
      });
      return;
    }

    setState((s) => {
      s.ui.formErrors = {};
    });

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
          s.meta[route].totalElements += 1;
          if (s.meta[route].pageable && s.meta[route].size > 0) {
            s.meta[route].totalPages = Math.max(
              1,
              Math.ceil(s.meta[route].totalElements / s.meta[route].size),
            );
          }
        });
        notify(SAVE_SUCCESS_MESSAGE, 'success');
      }
      closeDrawer();
      if (route === 'books' || route === 'publishers') {
        await loadRefs(true);
      }
    } catch (error) {
      const msg = error?.message != null ? String(error.message) : 'Не удалось сохранить';
      setState((s) => {
        s.data[route] = previousData;
        s.ui.formDraft = raw;
        s.ui.formErrors = { _form: msg };
      });
      notify(msg, 'error');
    }
  });

  document
    .querySelector('[data-cancel-edit]')
    ?.addEventListener('click', closeDrawer);

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

subscribe(renderWithErrorBoundary);
window.addEventListener('hashchange', () => routeChanged());

if (!window.location.hash) {
  window.location.hash = '#/books';
} else {
  routeChanged();
}

renderWithErrorBoundary();
