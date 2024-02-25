<template>
  <div v-if="adapterConfigurationRef">
    <div class="q-mb-lg">
      {{ $t('ArtivactPeripheralsConfigurationEditor.description') }}
    </div>

    <q-list bordered class="rounded-borders q-mb-lg">
      <q-expansion-item
        group="peripherals"
        header-class="bg-primary text-white"
        class="list-entry"
        expand-separator
        expand-icon-class="text-white"
      >
        <template v-slot:header>
          <q-item-section class="list-entry-label">
            {{ $t('ArtivactPeripheralsConfigurationEditor.turntable.heading') }}
          </q-item-section>
        </template>
        <q-card>
          <q-card-section>
            <div class="q-mb-md">
              {{ $t('ArtivactPeripheralsConfigurationEditor.turntable.description') }}
            </div>
            <q-select
              class="q-mb-md"
              outlined
              emit-value
              v-model="adapterConfigurationRef.turntableAdapterImplementation"
              :options="availableTurntableOptions"
              :option-label="opt => opt.label ? $t(opt.label) : $t(opt)"
              :label="$t('ArtivactPeripheralsConfigurationEditor.turntable.label')"
            />
            <q-input
              v-if="adapterConfigurationRef.turntableAdapterImplementation.toString() === AdapterImplementation[AdapterImplementation.FALLBACK_TURNTABLE_ADAPTER]"
              outlined
              type="number"
              :label="$t('ArtivactPeripheralsConfigurationEditor.turntable.delay')"
              v-model.number="adapterConfigurationRef.configValues['FALLBACK_TURNTABLE_ADAPTER']"
            />
            <q-input
              v-if="adapterConfigurationRef.turntableAdapterImplementation.toString() === AdapterImplementation[AdapterImplementation.ARTIVACT_TURNTABLE_ADAPTER]"
              outlined
              type="number"
              :label="$t('ArtivactPeripheralsConfigurationEditor.turntable.delay')"
              v-model.number="adapterConfigurationRef.configValues['ARTIVACT_TURNTABLE_ADAPTER']"
            />
          </q-card-section>
        </q-card>
      </q-expansion-item>

      <q-expansion-item
        group="peripherals"
        header-class="bg-primary text-white"
        class="list-entry"
        expand-separator
        expand-icon-class="text-white"
      >
        <template v-slot:header>
          <q-item-section class="list-entry-label">
            {{ $t('ArtivactPeripheralsConfigurationEditor.camera.heading') }}
          </q-item-section>
        </template>
        <q-card>
          <q-card-section>
            <div class="q-mb-md">
              {{ $t('ArtivactPeripheralsConfigurationEditor.camera.description') }}
            </div>
            <q-select
              class="q-mb-md"
              outlined
              emit-value
              v-model="adapterConfigurationRef.cameraAdapterImplementation"
              :options="availableCameraOptions"
              :option-label="opt => opt.label ? $t(opt.label) : $t(opt)"
              :label="$t('ArtivactPeripheralsConfigurationEditor.camera.label')"
            />
            <q-input
              v-if="adapterConfigurationRef.cameraAdapterImplementation.toString() === AdapterImplementation[AdapterImplementation.DIGI_CAM_CONTROL_CAMERA_ADAPTER]"
              outlined
              :label="$t('ArtivactPeripheralsConfigurationEditor.camera.digiCamControlExe')"
              v-model="adapterConfigurationRef.configValues['DIGI_CAM_CONTROL_CAMERA_ADAPTER']"
            />
            <q-input
              v-if="adapterConfigurationRef.cameraAdapterImplementation.toString() === AdapterImplementation[AdapterImplementation.DIGI_CAM_CONTROL_REMOTE_CAMERA_ADAPTER]"
              outlined
              label="$t('ArtivactPeripheralsConfigurationEditor.camera.digiCamControlUrl')"
              v-model="adapterConfigurationRef.configValues['DIGI_CAM_CONTROL_REMOTE_CAMERA_ADAPTER']"
            />
            <q-input
              v-if="adapterConfigurationRef.cameraAdapterImplementation.toString() === AdapterImplementation[AdapterImplementation.GPHOTO_TWO_CAMERA_ADAPTER]"
              outlined
              label="$t('ArtivactPeripheralsConfigurationEditor.camera.gphotoExe')"
              v-model="adapterConfigurationRef.configValues['GPHOTO_TWO_CAMERA_ADAPTER']"
            />
          </q-card-section>
        </q-card>
      </q-expansion-item>

      <q-expansion-item
        group="peripherals"
        header-class="bg-primary text-white"
        class="list-entry"
        expand-separator
        expand-icon-class="text-white"
      >
        <template v-slot:header>
          <q-item-section class="list-entry-label">
            {{ $t('ArtivactPeripheralsConfigurationEditor.background.heading') }}
          </q-item-section>
        </template>
        <q-card>
          <q-card-section>
            <div class="q-mb-md">
              {{ $t('ArtivactPeripheralsConfigurationEditor.background.description') }}
              <pre>{{ $t('ArtivactPeripheralsConfigurationEditor.background.dockerCmd') }}</pre>
            </div>
            <q-select
              class="q-mb-md"
              outlined
              emit-value
              v-model="adapterConfigurationRef.backgroundRemovalAdapterImplementation"
              :options="availableBackgroundRemovalOptions"
              :option-label="opt => opt.label ? $t(opt.label) : $t(opt)"
              :label="$t('ArtivactPeripheralsConfigurationEditor.background.label')"
            />
            <q-input
              v-if="adapterConfigurationRef.backgroundRemovalAdapterImplementation.toString() === AdapterImplementation[AdapterImplementation.REMBG_REMOTE_BACKGROUND_REMOVAL_ADAPTER]"
              outlined
              :label="$t('ArtivactPeripheralsConfigurationEditor.background.rembg')"
              v-model="adapterConfigurationRef.configValues['REMBG_REMOTE_BACKGROUND_REMOVAL_ADAPTER']"
            />
          </q-card-section>
        </q-card>
      </q-expansion-item>

      <q-expansion-item
        group="peripherals"
        header-class="bg-primary text-white"
        class="list-entry"
        expand-separator
        expand-icon-class="text-white"
      >
        <template v-slot:header>
          <q-item-section class="list-entry-label">
            {{ $t('ArtivactPeripheralsConfigurationEditor.creator.heading') }}
          </q-item-section>
        </template>
        <q-card>
          <q-card-section>
            <div class="q-mb-md">
              {{ $t('ArtivactPeripheralsConfigurationEditor.creator.description') }}
            </div>
            <q-select
              class="q-mb-md"
              outlined
              emit-value
              v-model="adapterConfigurationRef.modelCreatorImplementation"
              :options="availableModelCreatorOptions"
              :option-label="opt => opt.label ? $t(opt.label) : $t(opt)"
              :label="$t('ArtivactPeripheralsConfigurationEditor.creator.label')"
            />
            <q-input
              v-if="adapterConfigurationRef.modelCreatorImplementation.toString() === AdapterImplementation[AdapterImplementation.MESHROOM_MODEL_CREATOR_ADAPTER]"
              outlined
              :label="$t('ArtivactPeripheralsConfigurationEditor.creator.meshroom')"
              v-model="adapterConfigurationRef.configValues['MESHROOM_MODEL_CREATOR_ADAPTER']"
            />
            <q-input
              v-if="adapterConfigurationRef.modelCreatorImplementation.toString() === AdapterImplementation[AdapterImplementation.METASHAPE_MODEL_CREATOR_ADAPTER]"
              outlined
              :label="$t('ArtivactPeripheralsConfigurationEditor.creator.metashape')"
              v-model="adapterConfigurationRef.configValues['METASHAPE_MODEL_CREATOR_ADAPTER']"
            />
          </q-card-section>
        </q-card>
      </q-expansion-item>

      <q-expansion-item
        group="peripherals"
        header-class="bg-primary text-white"
        class="list-entry"
        expand-separator
        expand-icon-class="text-white"
      >
        <template v-slot:header>
          <q-item-section class="list-entry-label">
            {{ $t('ArtivactPeripheralsConfigurationEditor.editor.heading') }}
          </q-item-section>
        </template>
        <q-card>
          <q-card-section>
            <div class="q-mb-md">
              {{ $t('ArtivactPeripheralsConfigurationEditor.editor.description') }}
            </div>
            <q-select
              class="q-mb-md"
              outlined
              emit-value
              v-model="adapterConfigurationRef.modelEditorImplementation"
              :options="availableModelEditorOptions"
              :option-label="opt => opt.label ? $t(opt.label) : $t(opt)"
              :label="$t('ArtivactPeripheralsConfigurationEditor.editor.label')"
            />
            <q-input
              v-if="adapterConfigurationRef.modelEditorImplementation.toString() === AdapterImplementation[AdapterImplementation.BLENDER_MODEL_EDITOR_ADAPTER]"
              outlined
              :label="$t('ArtivactPeripheralsConfigurationEditor.editor.blender')"
              v-model="adapterConfigurationRef.configValues['BLENDER_MODEL_EDITOR_ADAPTER']"
            />
          </q-card-section>
        </q-card>
      </q-expansion-item>
    </q-list>
  </div>
</template>

<script setup lang="ts">
import {PropType, toRef} from 'vue';
import {AdapterConfiguration, AdapterImplementation, SelectboxModel} from 'components/models';

const props = defineProps({
  adapterConfiguration: {
    required: true,
    type: Object as PropType<AdapterConfiguration | null>,
  },
});

const adapterConfigurationRef = toRef(props, 'adapterConfiguration');

function isDisabled(availableOptions: AdapterImplementation[] | undefined, option: AdapterImplementation): boolean {
  if (!availableOptions) {
    return true;
  }

  for (let i = 0; i < availableOptions.length; i++) {
    if (availableOptions[i] === option) {
      return false;
    }
  }

  return true;
}

const availableTurntableOptions: SelectboxModel[] = [
  {
    label: AdapterImplementation[AdapterImplementation.FALLBACK_TURNTABLE_ADAPTER],
    value: AdapterImplementation[AdapterImplementation.FALLBACK_TURNTABLE_ADAPTER],
    disable: isDisabled(adapterConfigurationRef.value?.availableTurntableAdapterImplementations, AdapterImplementation.FALLBACK_TURNTABLE_ADAPTER)
  },
  {
    label: AdapterImplementation[AdapterImplementation.ARTIVACT_TURNTABLE_ADAPTER],
    value: AdapterImplementation[AdapterImplementation.ARTIVACT_TURNTABLE_ADAPTER],
    disable: isDisabled(adapterConfigurationRef.value?.availableTurntableAdapterImplementations, AdapterImplementation.ARTIVACT_TURNTABLE_ADAPTER)
  }
];

const availableCameraOptions: SelectboxModel[] = [
  {
    label: AdapterImplementation[AdapterImplementation.FALLBACK_CAMERA_ADAPTER],
    value: AdapterImplementation[AdapterImplementation.FALLBACK_CAMERA_ADAPTER],
    disable: isDisabled(adapterConfigurationRef.value?.availableCameraAdapterImplementations, AdapterImplementation.FALLBACK_CAMERA_ADAPTER)
  },
  {
    label: AdapterImplementation[AdapterImplementation.DIGI_CAM_CONTROL_CAMERA_ADAPTER],
    value: AdapterImplementation[AdapterImplementation.DIGI_CAM_CONTROL_CAMERA_ADAPTER],
    disable: isDisabled(adapterConfigurationRef.value?.availableCameraAdapterImplementations, AdapterImplementation.DIGI_CAM_CONTROL_CAMERA_ADAPTER)
  },
  {
    label: AdapterImplementation[AdapterImplementation.DIGI_CAM_CONTROL_REMOTE_CAMERA_ADAPTER],
    value: AdapterImplementation[AdapterImplementation.DIGI_CAM_CONTROL_REMOTE_CAMERA_ADAPTER],
    disable: isDisabled(adapterConfigurationRef.value?.availableCameraAdapterImplementations, AdapterImplementation.DIGI_CAM_CONTROL_REMOTE_CAMERA_ADAPTER)
  },
  {
    label: AdapterImplementation[AdapterImplementation.GPHOTO_TWO_CAMERA_ADAPTER],
    value: AdapterImplementation[AdapterImplementation.GPHOTO_TWO_CAMERA_ADAPTER],
    disable: isDisabled(adapterConfigurationRef.value?.availableCameraAdapterImplementations, AdapterImplementation.GPHOTO_TWO_CAMERA_ADAPTER)
  }
];

const availableBackgroundRemovalOptions: SelectboxModel[] = [
  {
    label: AdapterImplementation[AdapterImplementation.FALLBACK_BACKGROUND_REMOVAL_ADAPTER],
    value: AdapterImplementation[AdapterImplementation.FALLBACK_BACKGROUND_REMOVAL_ADAPTER],
    disable: isDisabled(adapterConfigurationRef.value?.availableBackgroundRemovalAdapterImplementations, AdapterImplementation.FALLBACK_BACKGROUND_REMOVAL_ADAPTER)
  },
  {
    label: AdapterImplementation[AdapterImplementation.REMBG_REMOTE_BACKGROUND_REMOVAL_ADAPTER],
    value: AdapterImplementation[AdapterImplementation.REMBG_REMOTE_BACKGROUND_REMOVAL_ADAPTER],
    disable: isDisabled(adapterConfigurationRef.value?.availableBackgroundRemovalAdapterImplementations, AdapterImplementation.REMBG_REMOTE_BACKGROUND_REMOVAL_ADAPTER)
  }
];

const availableModelCreatorOptions: SelectboxModel[] = [
  {
    label: AdapterImplementation[AdapterImplementation.FALLBACK_MODEL_CREATOR_ADAPTER],
    value: AdapterImplementation[AdapterImplementation.FALLBACK_MODEL_CREATOR_ADAPTER],
    disable: isDisabled(adapterConfigurationRef.value?.availableModelCreatorAdapterImplementations, AdapterImplementation.FALLBACK_MODEL_CREATOR_ADAPTER)
  },
  {
    label: AdapterImplementation[AdapterImplementation.MESHROOM_MODEL_CREATOR_ADAPTER],
    value: AdapterImplementation[AdapterImplementation.MESHROOM_MODEL_CREATOR_ADAPTER],
    disable: isDisabled(adapterConfigurationRef.value?.availableModelCreatorAdapterImplementations, AdapterImplementation.MESHROOM_MODEL_CREATOR_ADAPTER)
  },
  {
    label: AdapterImplementation[AdapterImplementation.METASHAPE_MODEL_CREATOR_ADAPTER],
    value: AdapterImplementation[AdapterImplementation.METASHAPE_MODEL_CREATOR_ADAPTER],
    disable: isDisabled(adapterConfigurationRef.value?.availableModelCreatorAdapterImplementations, AdapterImplementation.METASHAPE_MODEL_CREATOR_ADAPTER)
  }
];

const availableModelEditorOptions: SelectboxModel[] = [
  {
    label: AdapterImplementation[AdapterImplementation.FALLBACK_MODEL_EDITOR_ADAPTER],
    value: AdapterImplementation[AdapterImplementation.FALLBACK_MODEL_EDITOR_ADAPTER],
    disable: isDisabled(adapterConfigurationRef.value?.availableModelEditorAdapterImplementations, AdapterImplementation.FALLBACK_MODEL_EDITOR_ADAPTER)
  },
  {
    label: AdapterImplementation[AdapterImplementation.BLENDER_MODEL_EDITOR_ADAPTER],
    value: AdapterImplementation[AdapterImplementation.BLENDER_MODEL_EDITOR_ADAPTER],
    disable: isDisabled(adapterConfigurationRef.value?.availableModelEditorAdapterImplementations, AdapterImplementation.BLENDER_MODEL_EDITOR_ADAPTER)
  }
];

</script>

<style scoped>
.list-entry {
  border-bottom: 1px solid white;
}

.list-entry-label {
  font-size: large;
}
</style>
