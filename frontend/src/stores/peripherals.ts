import { defineStore } from 'pinia';
import { PeripheralsConfiguration } from '../components/artivact-models';

export const usePeripheralsConfigStore = defineStore('peripheralsConfig', {
  state: () => ({
    config: null as PeripheralsConfiguration,
  }),

  getters: {
    peripheralsConfig(state) {
      return state.config;
    },
    isTurnTableSet(state) {
      return (
        state.config &&
        state.config.turntablePeripheralConfigs &&
        state.config.turntablePeripheralConfigs.length > 0
      );
    },
    favouriteTurnTable(state) {
      if (
        state.config &&
        state.config.turntablePeripheralConfigs &&
        state.config.turntablePeripheralConfigs.length > 0
      ) {
        return (
          state.config.turntablePeripheralConfigs.find(
            (config) => config.favourite,
          )?.id ?? state.config.turntablePeripheralConfigs[0].id
        );
      }
      return null;
    },
    isCameraSet(state) {
      return (
        state.config &&
        state.config.cameraPeripheralConfigs &&
        state.config.cameraPeripheralConfigs.length > 0
      );
    },
    favouriteCamera(state) {
      if (
        state.config &&
        state.config.cameraPeripheralConfigs &&
        state.config.cameraPeripheralConfigs.length > 0
      ) {
        return (
          state.config.cameraPeripheralConfigs.find(
            (config) => config.favourite,
          )?.id ?? state.config.turntablePeripheralConfigs[0].id
        );
      }
      return null;
    },
    isImageBackgroundRemovalSet(state) {
      return (
        state.config &&
        state.config.imageBackgroundRemovalPeripheralConfigs &&
        state.config.imageBackgroundRemovalPeripheralConfigs.length > 0
      );
    },
    favouriteImageBackgroundRemoval(state) {
      if (
        state.config &&
        state.config.imageBackgroundRemovalPeripheralConfigs &&
        state.config.imageBackgroundRemovalPeripheralConfigs.length > 0
      ) {
        return (
          state.config.imageBackgroundRemovalPeripheralConfigs.find(
            (config) => config.favourite,
          )?.id ?? state.config.imageBackgroundRemovalPeripheralConfigs[0].id
        );
      }
      return null;
    },
    isModelCreatorSet(state) {
      return (
        state.config &&
        state.config.modelCreatorPeripheralConfigs &&
        state.config.modelCreatorPeripheralConfigs.length > 0
      );
    },
    favouriteModelCreator(state) {
      if (
        state.config &&
        state.config.modelCreatorPeripheralConfigs &&
        state.config.modelCreatorPeripheralConfigs.length > 0
      ) {
        return (
          state.config.modelCreatorPeripheralConfigs.find(
            (config) => config.favourite,
          )?.id ?? state.config.modelCreatorPeripheralConfigs[0].id
        );
      }
      return null;
    },
    isModelEditorSet(state) {
      return (
        state.config &&
        state.config.modelEditorPeripheralConfigs &&
        state.config.modelEditorPeripheralConfigs.length > 0
      );
    },
    favouriteModelEditor(state) {
      if (
        state.config &&
        state.config.modelEditorPeripheralConfigs &&
        state.config.modelEditorPeripheralConfigs.length > 0
      ) {
        return (
          state.config.modelEditorPeripheralConfigs.find(
            (config) => config.favourite,
          )?.id ?? state.config.modelEditorPeripheralConfigs[0].id
        );
      }
      return null;
    },
  },

  actions: {
    setPeripheralsConfig(peripheralConfiguration: PeripheralsConfiguration) {
      this.config = peripheralConfiguration;
    },
  },
});
