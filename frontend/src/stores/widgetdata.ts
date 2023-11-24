import {defineStore} from 'pinia';
import {WidgetPageContainer} from 'components/models';

export const useWidgetdataStore = defineStore('widgetdata', {
  state: () => ({
    widgetdata: {
      pages: {} as WidgetPageContainer
    }
  }),

  getters: {
    getPage: (state) => (widgetId: string) => {
      if (state.widgetdata.pages[widgetId]) {
        return state.widgetdata.pages[widgetId];
      }
      return 0;
    }
  },

  actions: {
    setPage(widgetId: string, page: number) {
      this.widgetdata.pages[widgetId] = page;
    }
  }

});
