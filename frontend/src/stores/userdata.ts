import {defineStore} from 'pinia';
import {UserData} from 'components/artivact-models';

export const useUserdataStore = defineStore('userdata', {
  state: () => ({
    userdata: {
      authenticated: false,
      roles: [],
      username: ''
    } as UserData
  }),

  getters: {
    authenticated(state) {
      return state.userdata.authenticated;
    },
    roles(state) {
      return state.userdata.roles;
    },
    username(state) {
      return state.userdata.username;
    },
    isAdmin(state) {
      let roleFound = false;
      state.userdata.roles.forEach(role => {
        if (role === 'ROLE_ADMIN') {
          roleFound = true;
        }
      })
      return roleFound;
    },
    isUser(state) {
      let roleFound = false;
      state.userdata.roles.forEach(role => {
        if (role === 'ROLE_USER') {
          roleFound = true;
        }
      })
      return roleFound;
    },
    isUserOrAdmin(state) {
      let roleFound = false;
      state.userdata.roles.forEach(role => {
        if (role === 'ROLE_USER' || role === 'ROLE_ADMIN') {
          roleFound = true;
        }
      })
      return roleFound;
    }
  },

  actions: {
    setUserdata(userdata: UserData) {
      this.userdata.authenticated = userdata.authenticated;
      this.userdata.roles = userdata.roles;
      this.userdata.username = userdata.username;
    }
  }
});
