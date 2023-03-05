import { defineStore } from 'pinia';
import {TranslatedMenu} from 'components/models';

export const useMenuStore = defineStore('menu', {
  state: () => ({
    availableMenus: [] as TranslatedMenu[]
  }),

  getters: {
    menus (state) {
      return state.availableMenus;
    }
  },

  actions: {
    setAvailableMenus (availableMenus: TranslatedMenu[]) {
      this.availableMenus.length = 0;
      availableMenus.forEach(availableMenu => {
        this.availableMenus.push(availableMenu);
      })
    }
  }
});
