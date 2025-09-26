import { createI18n } from 'vue-i18n';
import messages from 'src/i18n';
import { useLocaleStore } from 'src/stores/locale';

export default ({ app }) => {
  const i18n = createI18n({
    legacy: false,
    globalInjection: true,
    locale: 'en', // Default
    messages,
  });

  const localeStore = useLocaleStore();
  if (typeof navigator !== 'undefined') {
    i18n.global.locale.value = (
      navigator.languages?.[0] ||
      navigator.language ||
      null
    ).split('-')[0];
    localeStore.setSelectedLocale(i18n.global.locale.value);
  }

  app.use(i18n);
};
