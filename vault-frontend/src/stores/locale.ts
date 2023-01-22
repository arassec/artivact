import { defineStore } from 'pinia';

export const useLocaleStore = defineStore('locale', {
  state: () => ({
    availableLocales: [] as string[]
  }),

  getters: {
    locales (state) {
      return state.availableLocales;
    }
  },

  actions: {
    setAvailableLocales (availableLocales: string[]) {
      this.availableLocales.length = 0;
      availableLocales.forEach(availableLocale => {
        this.availableLocales.push(availableLocale);
      })
    }
  }
});
