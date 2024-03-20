import { defineStore } from 'pinia';
import {LicenseConfiguration} from 'components/artivact-models';

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
