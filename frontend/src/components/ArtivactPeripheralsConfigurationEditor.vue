<template>
  <div v-if="adapterConfigurationRef">
    <div class="q-mb-lg">
      Configures the peripherals for 3D model creation of the Artivact application. Fallback-Options can be used if the
      peripheral or external software can not be used at all.
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
            Turntable Configuration
          </q-item-section>
        </template>
        <q-card>
          <q-card-section>
            <div class="q-mb-md">
              Automatic rotation of captured items via turntables can be configured here. Currently only the open source
              Artivact turntable is supported. If you use a turntable manually you can configure the fallback option and
              set a delay to give you time to rotate the turntable by hand.
            </div>
            <q-select
              class="q-mb-md"
              outlined
              emit-value
              v-model="adapterConfigurationRef.turntableAdapterImplementation"
              :options="availableTurntableOptions"
              :option-label="opt => opt.label ? $t(opt.label) : $t(opt)"
              label="Turntable to use"
            />
            <q-input
              v-if="adapterConfigurationRef.turntableAdapterImplementation.toString() === AdapterImplementation[AdapterImplementation.FALLBACK_TURNTABLE_ADAPTER]"
              outlined
              type="number"
              label="Turntable delay in milliseconds"
              v-model.number="adapterConfigurationRef.configValues['FALLBACK_TURNTABLE_ADAPTER']"
            />
            <q-input
              v-if="adapterConfigurationRef.turntableAdapterImplementation.toString() === AdapterImplementation[AdapterImplementation.ARTIVACT_TURNTABLE_ADAPTER]"
              outlined
              type="number"
              label="Turntable delay in milliseconds"
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
            Camera Configuration
          </q-item-section>
        </template>
        <q-card>
          <q-card-section>
            <div class="q-mb-md">
              Images are captured with third party applications, which can be configured here. On Windows,
              DigiCamControl is supported. On Linux, gphoto2 must be used.
            </div>
            <q-select
              class="q-mb-md"
              outlined
              emit-value
              v-model="adapterConfigurationRef.cameraAdapterImplementation"
              :options="availableCameraOptions"
              :option-label="opt => opt.label ? $t(opt.label) : $t(opt)"
              label="Photo-Capture Software to use"
            />
            <q-input
              v-if="adapterConfigurationRef.cameraAdapterImplementation.toString() === AdapterImplementation[AdapterImplementation.DIGI_CAM_CONTROL_CAMERA_ADAPTER]"
              outlined
              label="DigiCamControl Executable"
              v-model="adapterConfigurationRef.configValues['DIGI_CAM_CONTROL_CAMERA_ADAPTER']"
            />
            <q-input
              v-if="adapterConfigurationRef.cameraAdapterImplementation.toString() === AdapterImplementation[AdapterImplementation.DIGI_CAM_CONTROL_REMOTE_CAMERA_ADAPTER]"
              outlined
              label="DigiCamControl Webserver URL"
              v-model="adapterConfigurationRef.configValues['DIGI_CAM_CONTROL_REMOTE_CAMERA_ADAPTER']"
            />
            <q-input
              v-if="adapterConfigurationRef.cameraAdapterImplementation.toString() === AdapterImplementation[AdapterImplementation.GPHOTO_TWO_CAMERA_ADAPTER]"
              outlined
              label="gphoto Executable"
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
            Background Removal
          </q-item-section>
        </template>
        <q-card>
          <q-card-section>
            <div class="q-mb-md">
              Automatic background removal of captured images is implemented using the open source tool 'rembg' by
              Daniel Gatis (https://github.com/danielgatis/rembg). You can e.g. provide it with docker by running
              <pre>docker run -d -p 5000:5000 --name=rembg --restart=always danielgatis/rembg s</pre>
            </div>
            <q-select
              class="q-mb-md"
              outlined
              emit-value
              v-model="adapterConfigurationRef.backgroundRemovalAdapterImplementation"
              :options="availableBackgroundRemovalOptions"
              :option-label="opt => opt.label ? $t(opt.label) : $t(opt)"
              label="Background-Removal Software to use"
            />
            <q-input
              v-if="adapterConfigurationRef.backgroundRemovalAdapterImplementation.toString() === AdapterImplementation[AdapterImplementation.REMBG_REMOTE_BACKGROUND_REMOVAL_ADAPTER]"
              outlined
              label="rembg Webserver URL"
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
            3D Model-Creator
          </q-item-section>
        </template>
        <q-card>
          <q-card-section>
            <div class="q-mb-md">
              For 3D model creation currently "Metashape" and "Meshroom" are supported.
            </div>
            <q-select
              class="q-mb-md"
              outlined
              emit-value
              v-model="adapterConfigurationRef.modelCreatorImplementation"
              :options="availableModelCreatorOptions"
              :option-label="opt => opt.label ? $t(opt.label) : $t(opt)"
              label="Photogrammetry Software to use"
            />
            <q-input
              v-if="adapterConfigurationRef.modelCreatorImplementation.toString() === AdapterImplementation[AdapterImplementation.MESHROOM_MODEL_CREATOR_ADAPTER]"
              outlined
              label="Meshroom Executable"
              v-model="adapterConfigurationRef.configValues['MESHROOM_MODEL_CREATOR_ADAPTER']"
            />
            <q-input
              v-if="adapterConfigurationRef.modelCreatorImplementation.toString() === AdapterImplementation[AdapterImplementation.METASHAPE_MODEL_CREATOR_ADAPTER]"
              outlined
              label="Metashape Executable"
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
            3D Model-Editor
          </q-item-section>
        </template>
        <q-card>
          <q-card-section>
            <div class="q-mb-md">
              For editing created 3D models, Blender3D can be configured here.
            </div>
            <q-select
              class="q-mb-md"
              outlined
              emit-value
              v-model="adapterConfigurationRef.modelEditorImplementation"
              :options="availableModelEditorOptions"
              :option-label="opt => opt.label ? $t(opt.label) : $t(opt)"
              label="3D Model Editor to use"
            />
            <q-input
              v-if="adapterConfigurationRef.modelEditorImplementation.toString() === AdapterImplementation[AdapterImplementation.BLENDER_MODEL_EDITOR_ADAPTER]"
              outlined
              label="Blender3D Executable"
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

  for (let i=0; i < availableOptions.length; i++) {
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
