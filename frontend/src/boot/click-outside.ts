// src/boot/click-outside.ts
import { boot } from 'quasar/wrappers';
import type { App, DirectiveBinding } from 'vue';

interface ClickOutsideElement {
  el: HTMLElement;
  handler: (event: Event) => void;
}

// globale Liste aller registrierten Elemente
const elements: ClickOutsideElement[] = [];

// globaler Listener
function onClick(event: Event) {
  const target = event.target as HTMLElement;
  if (
    target.closest('.move-widget-button') ||
    target.closest('.q-menu') ||
    target.closest('.q-dialog') ||
    target.closest('.q-popover')
  ) {
    return;
  }
  elements.forEach(({ el, handler }) => {
    if (!el.contains(target)) {
      handler(event);
    }
  });
}

export const clickOutsideDirective = {
  beforeMount(el: HTMLElement, binding: DirectiveBinding) {
    if (typeof binding.value !== 'function') {
      console.warn('v-click-outside: binding.value is not a function');
      return;
    }

    // pro Instanz ein eigener Handler
    const instanceHandler = binding.value;

    elements.push({ el, handler: instanceHandler });

    // Listener einmalig auf document registrieren
    if (elements.length === 1) {
      document.addEventListener('click', onClick, true);
      document.addEventListener('touchstart', onClick, true);
    }
  },

  unmounted(el: HTMLElement) {
    const index = elements.findIndex((item) => item.el === el);
    if (index !== -1) {
      elements.splice(index, 1);
    }

    // Wenn kein Element mehr registriert, Listener entfernen
    if (elements.length === 0) {
      document.removeEventListener('click', onClick, true);
      document.removeEventListener('touchstart', onClick, true);
    }
  },
};

// Quasar Boot-File
export default boot(({ app }: { app: App }) => {
  app.directive('click-outside', clickOutsideDirective);
});
