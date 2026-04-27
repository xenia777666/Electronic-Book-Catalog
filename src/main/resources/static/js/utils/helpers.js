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
