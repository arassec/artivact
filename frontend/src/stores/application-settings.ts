import { defineStore } from 'pinia';
import { ApplicationSettings, ColorTheme, License, Profiles } from 'components/artivact-models';

export const useApplicationSettingsStore = defineStore('applicationSettings', {
  state: () => ({
    data: {
      settings: {} as ApplicationSettings
    }
  }),

  getters: {
    settings(state): ApplicationSettings {
      return state.data.settings;
    },
    applicationTitle(state): string {
      return state.data.settings.applicationTitle;
    },
    availableLocales(state): string[] {
      return state.data.settings.availableLocales;
    },
    applicationLocale(state): string {
      return state.data.settings.applicationLocale;
    },
    colorTheme(state): ColorTheme {
      return state.data.settings.colorTheme;
    },
    license(state): License {
      return state.data.settings.license;
    },
    profiles(state): Profiles {
      return state.data.settings.profiles;
    },
    availableRoles(state): string[] {
      return state.data.settings.availableRoles;
    },
    syncAvailable(state): boolean {
      return state.data.settings.syncAvailable;
    }
  },

  actions: {
    setSettings(settings: ApplicationSettings) {
      this.data.settings = settings;
    }
  }
});
