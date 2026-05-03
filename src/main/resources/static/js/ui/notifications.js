let container;

function ensureContainer() {
  if (!container) {
    container = document.createElement('div');
    container.className = 'fixed top-4 right-4 z-[60] flex max-w-sm flex-col items-end space-y-2';
    document.body.appendChild(container);
  }
}

export function notify(message, type = 'success') {
  ensureContainer();

  const item = document.createElement('div');
  const colorMap = {
    success:
      'border-emerald-200/90 bg-white text-emerald-900 shadow-md shadow-zinc-900/10 ring-1 ring-emerald-500/15',
    error:
      'border-rose-200/90 bg-white text-rose-900 shadow-md shadow-zinc-900/10 ring-1 ring-rose-500/15',
    info: 'border-zinc-200 bg-white text-zinc-800 shadow-md shadow-zinc-900/10 ring-1 ring-violet-500/10',
  };

  item.className = `${colorMap[type] || colorMap.info} border px-4 py-3 rounded-xl text-sm min-w-72 transition duration-200`;
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
    'fixed inset-0 z-50 flex items-center justify-center bg-zinc-900/45 p-4 backdrop-blur-[2px]';

  overlay.innerHTML = `
    <div class="w-full max-w-md card-base p-6 shadow-xl shadow-zinc-900/15">
      <h3 class="mb-2 text-lg font-semibold text-zinc-900">${title}</h3>
      <p class="mb-6 text-sm text-zinc-600">${description}</p>
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
    'fixed inset-0 z-50 flex items-center justify-center bg-zinc-900/45 p-4 backdrop-blur-[2px]';

  overlay.innerHTML = `
    <div class="card-base max-h-[85vh] w-full max-w-lg overflow-y-auto p-6 shadow-xl shadow-zinc-900/15">
      <h3 class="mb-4 text-lg font-semibold text-zinc-900">${title}</h3>
      <div class="text-sm leading-relaxed text-zinc-700">${body}</div>
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
