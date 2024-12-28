import { defineStore } from 'pinia';

export const usePageStore = defineStore('page', {
  state: () => ({
    newPageCreated: false,
    latestWidgetIndex: -1
  }),

  getters: {
    isNewPageCreated(state) {
      return state.newPageCreated;
    },
    getLatestWidgetIndex(state) {
      return state.latestWidgetIndex
    }
  },

  actions: {
    setNewPageCreated(created: boolean) {
      this.newPageCreated = created;
    },
    setLatestWidgetIndex(index: number) {
      this.latestWidgetIndex = index;
    }
  }
});
