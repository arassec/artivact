import { defineStore } from 'pinia';

export const useRoleStore = defineStore('role', {
  state: () => ({
    availableRoles: [] as string[]
  }),

  getters: {
    roles (state) {
      return state.availableRoles;
    }
  },

  actions: {
    setAvailableRoles (availableRoles: string[]) {
      this.availableRoles.length = 0;
      availableRoles.forEach(availableRole => {
        this.availableRoles.push(availableRole);
      })
    }
  }
});
