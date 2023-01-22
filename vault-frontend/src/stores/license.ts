import { defineStore } from 'pinia';
import {TranslatedLicense} from 'components/models';

export const useLicenseStore = defineStore('license', {
  state: () => ({
    translatedLicense: {} as TranslatedLicense
  }),

  getters: {
    license (state) {
      return state.translatedLicense;
    }
  },

  actions: {
    setLicense (translatedLicense: TranslatedLicense) {
      this.translatedLicense = translatedLicense;
    }
  }
});
