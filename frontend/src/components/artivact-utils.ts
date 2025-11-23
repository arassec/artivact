import {TranslatableString} from './artivact-models';
import {useLocaleStore} from '../stores/locale';
import {useUserdataStore} from '../stores/userdata';

const localeStore = useLocaleStore();
const userdataStore = useUserdataStore();

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
  if (translatableString
    && translatableString.translations
    && localeStore.selectedLocale !== null
    && localeStore.selectedLocale in translatableString.translations
    && translatableString.translations[localeStore.selectedLocale]) {
    return translatableString.translations[localeStore.selectedLocale];
  }
  if (localeStore.selectedLocale === null && userdataStore.isUserOrAdmin) {
    return translatableString.value;
  }
  if (translatableString && translatableString.translatedValue) {
    return translatableString.translatedValue;
  }
  return translatableString.value;
}
