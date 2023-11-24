import { defineStore } from 'pinia';
import {Menu} from 'components/models';

export const useMenuStore = defineStore('menu', {
  state: () => ({
    availableMenus: [] as Menu[]
  }),

  getters: {
    menus (state) {
      return state.availableMenus;
    }
  },

  actions: {
    setAvailableMenus (availableMenus: Menu[]) {
      this.availableMenus.length = 0;
      availableMenus.forEach(availableMenu => {
        this.availableMenus.push(availableMenu);
      })
    }
  }
});
