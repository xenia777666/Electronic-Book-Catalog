const listeners = new Set();

export const state = {
  route: 'books',
  loading: false,
  refs: {
    publishers: [],
    authors: [],
    genres: [],
    books: [],
  },
  data: {
    books: [],
    publishers: [],
    authors: [],
    genres: [],
    reviews: [],
  },
  meta: {
    books: {
      totalElements: 0,
      totalPages: 1,
      page: 0,
      size: 10,
      pageable: true,
    },
    publishers: {
      totalElements: 0,
      totalPages: 1,
      page: 0,
      size: 10,
      pageable: false,
    },
    authors: {
      totalElements: 0,
      totalPages: 1,
      page: 0,
      size: 10,
      pageable: false,
    },
    genres: {
      totalElements: 0,
      totalPages: 1,
      page: 0,
      size: 10,
      pageable: false,
    },
    reviews: {
      totalElements: 0,
      totalPages: 1,
      page: 0,
      size: 10,
      pageable: false,
    },
  },
  ui: {
    filters: {},
    sort: { key: 'publisher', dir: 'asc' },
    selectedIds: new Set(),
    editingId: null,
    drawerOpen: false,
    formErrors: {},
    /** Черновик полей после ошибки валидации/сервера — не сбрасывать ввод. */
    formDraft: null,
    booksServerFilter: false,
    bookFilter: {
      author: '',
      genre: '',
      title: '',
      minPrice: '',
      maxPrice: '',
      minRating: '',
    },
    bookFilterErrors: {},
  },
};

export function subscribe(listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function setState(mutator) {
  mutator(state);
  listeners.forEach((listener) => listener(state));
}

export function resetUiState() {
  setState((s) => {
    s.ui.filters = {};
    s.ui.sort =
      s.route === 'books'
        ? { key: 'publisher', dir: 'asc' }
        : { key: 'id', dir: 'desc' };
    s.ui.selectedIds = new Set();
    s.ui.editingId = null;
    s.ui.drawerOpen = false;
    s.ui.formErrors = {};
    s.ui.formDraft = null;
    s.ui.booksServerFilter = false;
    s.ui.bookFilter = {
      author: '',
      genre: '',
      title: '',
      minPrice: '',
      maxPrice: '',
      minRating: '',
    };
    s.ui.bookFilterErrors = {};
  });
}
