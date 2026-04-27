let container;

function ensureContainer() {
  if (!container) {
    container = document.createElement('div');
    container.className = 'fixed top-4 right-4 z-[60] space-y-2';
    document.body.appendChild(container);
  }
}

export function notify(message, type = 'success') {
  ensureContainer();

  const item = document.createElement('div');
  const colorMap = {
    success: 'border-emerald-200 bg-emerald-50 text-emerald-800',
    error: 'border-rose-200 bg-rose-50 text-rose-800',
    info: 'border-sky-200 bg-sky-50 text-sky-800',
  };

  item.className = `${colorMap[type] || colorMap.info} border px-4 py-3 rounded-xl shadow-lg text-sm min-w-72 transition duration-200`;
  item.textContent = message;
  container.appendChild(item);

  setTimeout(() => {
    item.classList.add('opacity-0', 'translate-y-1');
    setTimeout(() => item.remove(), 250);
  }, 2600);
}

export function confirmModal({ title, description, onConfirm }) {
  const overlay = document.createElement('div');
  overlay.className =
    'fixed inset-0 bg-slate-900/50 z-50 flex items-center justify-center p-4';

  overlay.innerHTML = `
    <div class="w-full max-w-md card-base p-6">
      <h3 class="text-lg font-semibold text-slate-900 mb-2">${title}</h3>
      <p class="text-sm text-slate-600 mb-6">${description}</p>
      <div class="flex justify-end gap-3">
        <button data-close class="btn-secondary">Отмена</button>
        <button data-confirm class="btn-danger">Удалить</button>
      </div>
    </div>
  `;

  overlay.querySelector('[data-close]').addEventListener('click', () => overlay.remove());
  overlay.querySelector('[data-confirm]').addEventListener('click', async () => {
    await onConfirm();
    overlay.remove();
  });
  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) {
      overlay.remove();
    }
  });

  document.body.appendChild(overlay);
}

export function infoModal({ title, body }) {
  const overlay = document.createElement('div');
  overlay.className =
    'fixed inset-0 bg-slate-900/50 z-50 flex items-center justify-center p-4';

  overlay.innerHTML = `
    <div class="w-full max-w-lg card-base p-6 max-h-[85vh] overflow-y-auto">
      <h3 class="text-lg font-semibold text-slate-900 mb-4">${title}</h3>
      <div class="text-sm text-slate-700 prose prose-sm">${body}</div>
      <div class="mt-6 flex justify-end">
        <button data-close class="btn-primary">Закрыть</button>
      </div>
    </div>
  `;

  overlay.querySelector('[data-close]').addEventListener('click', () => overlay.remove());
  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) {
      overlay.remove();
    }
  });

  document.body.appendChild(overlay);
}
