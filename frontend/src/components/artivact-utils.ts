import {TranslatableString} from './artivact-models';
import {useLocaleStore} from '../stores/locale';
import {useUserdataStore} from '../stores/userdata';
import {useApplicationSettingsStore} from '../stores/application-settings';
import MarkdownIt from 'markdown-it';

const localeStore = useLocaleStore();
const userdataStore = useUserdataStore();
const applicationSettingsStore = useApplicationSettingsStore();

function normalizeLocale(locale: string | null | undefined) {
  if (!locale) {
    return null;
  }
  return locale.replace('-', '_');
}

function getTranslationForLocale(
  translatableString: TranslatableString,
  locale: string | null | undefined,
) {
  if (!translatableString?.translations || !locale) {
    return null;
  }

  const normalizedLocale = normalizeLocale(locale);
  if (!normalizedLocale) {
    return null;
  }

  const exactMatches = [locale, normalizedLocale, normalizedLocale.replace('_', '-')];
  for (const matchingLocale of exactMatches) {
    if (
      matchingLocale in translatableString.translations
      && translatableString.translations[matchingLocale]
    ) {
      return translatableString.translations[matchingLocale];
    }
  }

  const languageOnlyLocale = normalizedLocale.split('_')[0];
  if (
    languageOnlyLocale in translatableString.translations
    && translatableString.translations[languageOnlyLocale]
  ) {
    return translatableString.translations[languageOnlyLocale];
  }

  for (const [translationLocale, translationValue] of Object.entries(translatableString.translations)) {
    const normalizedTranslationLocale = normalizeLocale(translationLocale);
    if (
      normalizedTranslationLocale
      && normalizedLocale.startsWith(normalizedTranslationLocale + '_')
      && translationValue
    ) {
      return translationValue;
    }
  }

  return null;
}

export function moveUp(array: [unknown], index: number) {
  if (index > 0 && array.length >= index) {
    const el = array[index];
    array[index] = array[index - 1];
    array[index - 1] = el;
  }
}

export function moveDown(array: [unknown], index: number) {
  if (index !== -1 && index < array.length - 1) {
    const el = array[index];
    array[index] = array[index + 1];
    array[index + 1] = el;
  }

}

export function translate(translatableString: TranslatableString) {
  if (!translatableString) {
    return '';
  }

  const selectedLocaleTranslation = getTranslationForLocale(
    translatableString,
    localeStore.selectedLocale,
  );
  if (selectedLocaleTranslation) {
    return selectedLocaleTranslation;
  }

  if (localeStore.selectedLocale === null && userdataStore.isUserOrAdmin) {
    return translatableString.value;
  }

  if (translatableString && translatableString.translatedValue) {
    return translatableString.translatedValue;
  }

  const defaultLocaleTranslation = getTranslationForLocale(
    translatableString,
    applicationSettingsStore.defaultLocale,
  );
  if (defaultLocaleTranslation) {
    return defaultLocaleTranslation;
  }

  return translatableString.value;
}

export function formatMarkdown(text: string) {
  if (!text) {
    return;
  }
  let md = new MarkdownIt();
  return md.render(text);
}
