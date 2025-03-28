import { defineStore } from 'pinia';
import {BreadcrumbData} from '../components/artivact-models';

export const useBreadcrumbsStore = defineStore('breadcrumbs', {
  state: () => ({
    breadcrumbsData: [] as BreadcrumbData[],
  }),

  getters: {
    breadcrumbs (state) {
      return state.breadcrumbsData;
    },
  },

  actions: {
    addAnchor(anchor: string) {
      if (this.breadcrumbsData.length > 0) {
        this.breadcrumbsData[this.breadcrumbsData.length -1].anchor = '#' + anchor
      }
    },
    addBreadcrumb (breadcrumbData: BreadcrumbData) {
      if (this.breadcrumbsData[this.breadcrumbsData.length -1] !== breadcrumbData) {
        this.breadcrumbsData.push(breadcrumbData);
      }
    },
    resetBreadcrumbs () {
      this.breadcrumbsData.length = 0;
      this.anchor = '';
    },
    removeLastBreadcrumb() {
      this.breadcrumbsData.pop();
    }
  }
});
