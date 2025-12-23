import { reactive } from 'vue';

// 全局 toast 列表
export const toasts = reactive([]);
let idSeed = 1;

export function pushToast({ type = 'info', text = '', duration = 3000 }) {
  const id = idSeed++;
  const item = { id, type, text, leaving: false };
  toasts.push(item);
  if (duration > 0) {
    setTimeout(() => dismissToast(id), duration);
  }
  return id;
}

export function dismissToast(id) {
  const idx = toasts.findIndex(t => t.id === id);
  if (idx !== -1) {
    const ref = toasts[idx];
    ref.leaving = true;
    setTimeout(() => {
      const again = toasts.findIndex(t => t.id === id);
      if (again !== -1) toasts.splice(again, 1);
    }, 240);
  }
}

export const toast = {
  info: (text, duration = 3000) => pushToast({ type: 'info', text, duration }),
  success: (text, duration = 3000) => pushToast({ type: 'success', text, duration }),
  warning: (text, duration = 3500) => pushToast({ type: 'warning', text, duration }),
  error: (text, duration = 4000) => pushToast({ type: 'error', text, duration }),
};
