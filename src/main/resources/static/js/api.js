const BASE_HEADERS = { 'Content-Type': 'application/json' };

function buildUrl(path, params = {}) {
  const url = new URL(path, window.location.origin);
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, String(value));
    }
  });
  return `${url.pathname}${url.search}`;
}

export async function request(path, options = {}) {
  const headers = { ...BASE_HEADERS, ...(options.headers || {}) };
  let response;
  try {
    response = await fetch(path, { ...options, headers });
  } catch (error) {
    if (error.name === 'TypeError') {
      throw new Error(
        'Проблема с сетью. Проверьте соединение и попробуйте снова.',
      );
    }
    throw error;
  }

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();
  const payload = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const msg =
      payload?.message ||
      payload?.error ||
      (payload?.errors &&
        typeof payload.errors === 'object' &&
        Object.values(payload.errors).join(', ')) ||
      `Ошибка ${response.status}`;
    throw new Error(msg);
  }

  return payload;
}

function pageMeta(payload, params) {
  return {
    totalElements: payload?.totalElements ?? 0,
    totalPages: payload?.totalPages ?? 1,
    page: payload?.number ?? 0,
    size: payload?.size ?? params.size ?? 10,
    pageable: true,
  };
}

export const api = {
  books: {
    async list(params = {}) {
      const payload = await request(buildUrl('/api/books', params));
      const meta = pageMeta(payload, params);
      return {
        items: payload?.content ?? [],
        meta: { ...meta, pageable: true },
      };
    },
    async search(filters = {}) {
      const qs = new URLSearchParams();
      if (filters.author) {
        qs.set('author', filters.author);
      }
      if (filters.genre) {
        qs.set('genre', filters.genre);
      }
      if (filters.title) {
        qs.set('title', filters.title);
      }
      if (filters.minPrice) {
        qs.set('minPrice', filters.minPrice);
      }
      if (filters.maxPrice) {
        qs.set('maxPrice', filters.maxPrice);
      }
      if (filters.minRating) {
        qs.set('minRating', filters.minRating);
      }
      const path = `/api/books/search/complex${qs.toString() ? `?${qs}` : ''}`;
      const items = (await request(path)) ?? [];
      return { items, meta: { pageable: false } };
    },
    create: (body) =>
      request('/api/books', { method: 'POST', body: JSON.stringify(body) }),
    update: (id, body) =>
      request(`/api/books/${id}`, {
        method: 'PUT',
        body: JSON.stringify(body),
      }),
    remove: (id) => request(`/api/books/${id}`, { method: 'DELETE' }),
    get: (id) => request(`/api/books/${id}`),
  },
  publishers: {
    async list() {
      const payload = await request('/api/publishers');
      return { items: payload ?? [], meta: { pageable: false } };
    },
    create: (body) =>
      request('/api/publishers', { method: 'POST', body: JSON.stringify(body) }),
    update: (id, body) =>
      request(`/api/publishers/${id}`, {
        method: 'PUT',
        body: JSON.stringify(body),
      }),
    remove: (id) =>
      request(`/api/publishers/${id}`, { method: 'DELETE' }),
  },
  authors: {
    async list(params = {}) {
      const payload = await request(buildUrl('/api/authors', params));
      if (Array.isArray(payload)) {
        return { items: payload, meta: { pageable: false } };
      }
      return {
        items: payload?.content ?? [],
        meta: pageMeta(payload, params),
      };
    },
    async search(name) {
      const payload = await request(
        buildUrl('/api/authors/search', { name }),
      );
      return { items: payload ?? [], meta: { pageable: false } };
    },
    create: (body) =>
      request('/api/authors', { method: 'POST', body: JSON.stringify(body) }),
    update: (id, body) =>
      request(`/api/authors/${id}`, {
        method: 'PUT',
        body: JSON.stringify(body),
      }),
    remove: (id) => request(`/api/authors/${id}`, { method: 'DELETE' }),
  },
  genres: {
    async list() {
      const payload = await request('/api/genres');
      return { items: payload ?? [], meta: { pageable: false } };
    },
    create: (body) =>
      request('/api/genres', { method: 'POST', body: JSON.stringify(body) }),
    update: (id, body) =>
      request(`/api/genres/${id}`, {
        method: 'PUT',
        body: JSON.stringify(body),
      }),
    remove: (id) => request(`/api/genres/${id}`, { method: 'DELETE' }),
  },
  reviews: {
    async list() {
      const payload = await request('/api/reviews');
      return { items: payload ?? [], meta: { pageable: false } };
    },
    create: (body) =>
      request('/api/reviews', { method: 'POST', body: JSON.stringify(body) }),
    update: (id, body) =>
      request(`/api/reviews/${id}`, {
        method: 'PUT',
        body: JSON.stringify(body),
      }),
    remove: (id) => request(`/api/reviews/${id}`, { method: 'DELETE' }),
  },
};
