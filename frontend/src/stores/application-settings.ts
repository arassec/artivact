import {defineStore} from 'pinia';
import {ApplicationSettings, ColorTheme, License, Profiles} from '../components/artivact-models';

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
    defaultLocale(state): string {
      return state.data.settings.defaultLocale;
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
    },
    translationEnabled(state): boolean {
      return state.data.settings.translationEnabled;
    },
    ttsEnabled(state): boolean {
      return state.data.settings.ttsEnabled;
    }
  },

  actions: {
    setSettings(settings: ApplicationSettings) {
      this.data.settings = settings;
    }
  }
});
