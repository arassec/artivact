import {defineStore} from 'pinia';

export const useLocaleStore = defineStore('locale', {
  state: () => ({
    localeData: {
      selectedLocale: null as string | null,
      availableLocales: [] as string[]
    }
  }),

  getters: {
    selectedLocale(state) {
      return state.localeData.selectedLocale;
    },
    locales(state) {
      return state.localeData.availableLocales;
    }
  },

  actions: {
    setSelectedLocale(selectedLocale: string | null) {
      this.localeData.selectedLocale = selectedLocale;
    },
    setAvailableLocales(availableLocales: string[]) {
      this.localeData.availableLocales.length = 0;
      availableLocales.forEach(availableLocale => {
        this.localeData.availableLocales.push(availableLocale);
      })
    }
  }
});
