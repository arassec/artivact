import { defineStore } from 'pinia';
import {BreadcrumbData} from 'components/models';

export const useBreadcrumbsStore = defineStore('breadcrumbs', {
  state: () => ({
    breadcrumbsData: [] as BreadcrumbData[]
  }),

  getters: {
    breadcrumbs (state) {
      return state.breadcrumbsData;
    }
  },

  actions: {
    addBreadcrumb (breadcrumbData: BreadcrumbData) {
      if (this.breadcrumbsData[this.breadcrumbsData.length -1] !== breadcrumbData) {
        this.breadcrumbsData.push(breadcrumbData);
      }
    },
    resetBreadcrumbs () {
      this.breadcrumbsData.length = 0;
    },
    removeLastBreadcrumb() {
      this.breadcrumbsData.pop();
    }
  }
});
