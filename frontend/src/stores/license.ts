import { defineStore } from 'pinia';
import {LicenseConfiguration} from 'components/models';

export const useLicenseStore = defineStore('license', {
  state: () => ({
    licenseConfiguration: {} as LicenseConfiguration
  }),

  getters: {
    license (state) {
      return state.licenseConfiguration;
    }
  },

  actions: {
    setLicense (licenseConfiguration: LicenseConfiguration) {
      this.licenseConfiguration = licenseConfiguration;
    }
  }
});
