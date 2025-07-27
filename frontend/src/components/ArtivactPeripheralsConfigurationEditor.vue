<template>
  <div v-if="peripheralConfigurationRef">
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
              v-model="peripheralConfigurationRef.turntablePeripheralImplementation"
              :options="availableTurntableOptions"
              :option-label="opt => opt.label ? $t(opt.label) : $t(opt)"
              :label="$t('ArtivactPeripheralsConfigurationEditor.turntable.label')"
            />
            <q-input
              v-if="peripheralConfigurationRef.turntablePeripheralImplementation.toString() === PeripheralImplementation[PeripheralImplementation.DEFAULT_TURNTABLE_PERIPHERAL]"
              outlined
              type="number"
              :label="$t('ArtivactPeripheralsConfigurationEditor.turntable.delay')"
              v-model.number="peripheralConfigurationRef.configValues['DEFAULT_TURNTABLE_PERIPHERAL']"
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
              v-model="peripheralConfigurationRef.cameraPeripheralImplementation"
              :options="availableCameraOptions"
              :option-label="opt => opt.label ? $t(opt.label) : $t(opt)"
              :label="$t('ArtivactPeripheralsConfigurationEditor.camera.label')"
            />
            <q-input
              v-if="peripheralConfigurationRef.cameraPeripheralImplementation.toString() === PeripheralImplementation[PeripheralImplementation.DIGI_CAM_CONTROL_CAMERA_PERIPHERAL]"
              outlined
              :label="$t('ArtivactPeripheralsConfigurationEditor.camera.digiCamControlExe')"
              v-model="peripheralConfigurationRef.configValues['DIGI_CAM_CONTROL_CAMERA_PERIPHERAL']"
            />
            <q-input
              v-if="peripheralConfigurationRef.cameraPeripheralImplementation.toString() === PeripheralImplementation[PeripheralImplementation.GPHOTO_TWO_CAMERA_PERIPHERAL]"
              outlined
              :label="$t('ArtivactPeripheralsConfigurationEditor.camera.gphotoExe')"
              v-model="peripheralConfigurationRef.configValues['GPHOTO_TWO_CAMERA_PERIPHERAL']"
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
              <pre>{{ $t('ArtivactPeripheralsConfigurationEditor.background.defaultConfiguration') }}</pre>
            </div>
            <q-select
              class="q-mb-md"
              outlined
              emit-value
              v-model="peripheralConfigurationRef.imageManipulationPeripheralImplementation"
              :options="availableImageManipulationOptions"
              :option-label="opt => opt.label ? $t(opt.label) : $t(opt)"
              :label="$t('ArtivactPeripheralsConfigurationEditor.background.label')"
            />
            <q-input
              v-if="peripheralConfigurationRef.imageManipulationPeripheralImplementation.toString() === PeripheralImplementation[PeripheralImplementation.DEFAULT_IMAGE_MANIPULATION_PERIPHERAL]"
              outlined
              :label="$t('ArtivactPeripheralsConfigurationEditor.background.default')"
              v-model="peripheralConfigurationRef.configValues['DEFAULT_IMAGE_MANIPULATION_PERIPHERAL']"
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
              v-model="peripheralConfigurationRef.modelCreatorPeripheralImplementation"
              :options="availableModelCreatorOptions"
              :option-label="opt => opt.label ? $t(opt.label) : $t(opt)"
              :label="$t('ArtivactPeripheralsConfigurationEditor.creator.label')"
            />
            <q-input
              v-if="peripheralConfigurationRef.modelCreatorPeripheralImplementation.toString() === PeripheralImplementation[PeripheralImplementation.MESHROOM_MODEL_CREATOR_PERIPHERAL]"
              outlined
              :label="$t('ArtivactPeripheralsConfigurationEditor.creator.meshroom')"
              v-model="peripheralConfigurationRef.configValues['MESHROOM_MODEL_CREATOR_PERIPHERAL']"
            />
            <q-input
              v-if="peripheralConfigurationRef.modelCreatorPeripheralImplementation.toString() === PeripheralImplementation[PeripheralImplementation.METASHAPE_MODEL_CREATOR_PERIPHERAL]"
              outlined
              :label="$t('ArtivactPeripheralsConfigurationEditor.creator.metashape')"
              v-model="peripheralConfigurationRef.configValues['METASHAPE_MODEL_CREATOR_PERIPHERAL']"
            />
            <q-input
              v-if="peripheralConfigurationRef.modelCreatorPeripheralImplementation.toString() === PeripheralImplementation[PeripheralImplementation.REALITY_SCAN_MODEL_CREATOR_PERIPHERAL]"
              outlined
              :label="$t('ArtivactPeripheralsConfigurationEditor.creator.RealityScan')"
              v-model="peripheralConfigurationRef.configValues['REALITY_SCAN_MODEL_CREATOR_PERIPHERAL']"
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
              v-model="peripheralConfigurationRef.modelEditorPeripheralImplementation"
              :options="availableModelEditorOptions"
              :option-label="opt => opt.label ? $t(opt.label) : $t(opt)"
              :label="$t('ArtivactPeripheralsConfigurationEditor.editor.label')"
            />
            <q-input
              v-if="peripheralConfigurationRef.modelEditorPeripheralImplementation.toString() === PeripheralImplementation[PeripheralImplementation.BLENDER_MODEL_EDITOR_PERIPHERAL]"
              outlined
              :label="$t('ArtivactPeripheralsConfigurationEditor.editor.blender')"
              v-model="peripheralConfigurationRef.configValues['BLENDER_MODEL_EDITOR_PERIPHERAL']"
            />
          </q-card-section>
        </q-card>
      </q-expansion-item>
    </q-list>
  </div>
</template>

<script setup lang="ts">
import {PropType, toRef} from 'vue';
import {PeripheralConfiguration, PeripheralImplementation, SelectboxModel} from 'components/artivact-models';

const props = defineProps({
  peripheralConfiguration: {
    required: true,
    type: Object as PropType<PeripheralConfiguration | null>,
  },
});

const peripheralConfigurationRef = toRef(props, 'peripheralConfiguration');

function isDisabled(availableOptions: PeripheralImplementation[] | undefined, option: PeripheralImplementation): boolean {
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
    label: PeripheralImplementation[PeripheralImplementation.DEFAULT_TURNTABLE_PERIPHERAL],
    value: PeripheralImplementation[PeripheralImplementation.DEFAULT_TURNTABLE_PERIPHERAL],
    disable: isDisabled(peripheralConfigurationRef.value?.availableTurntablePeripheralImplementations, PeripheralImplementation.DEFAULT_TURNTABLE_PERIPHERAL)
  }
];

const availableCameraOptions: SelectboxModel[] = [
  {
    label: PeripheralImplementation[PeripheralImplementation.DEFAULT_CAMERA_PERIPHERAL],
    value: PeripheralImplementation[PeripheralImplementation.DEFAULT_CAMERA_PERIPHERAL],
    disable: isDisabled(peripheralConfigurationRef.value?.availableCameraPeripheralImplementations, PeripheralImplementation.DEFAULT_CAMERA_PERIPHERAL)
  },
  {
    label: PeripheralImplementation[PeripheralImplementation.DIGI_CAM_CONTROL_CAMERA_PERIPHERAL],
    value: PeripheralImplementation[PeripheralImplementation.DIGI_CAM_CONTROL_CAMERA_PERIPHERAL],
    disable: isDisabled(peripheralConfigurationRef.value?.availableCameraPeripheralImplementations, PeripheralImplementation.DIGI_CAM_CONTROL_CAMERA_PERIPHERAL)
  },
  {
    label: PeripheralImplementation[PeripheralImplementation.GPHOTO_TWO_CAMERA_PERIPHERAL],
    value: PeripheralImplementation[PeripheralImplementation.GPHOTO_TWO_CAMERA_PERIPHERAL],
    disable: isDisabled(peripheralConfigurationRef.value?.availableCameraPeripheralImplementations, PeripheralImplementation.GPHOTO_TWO_CAMERA_PERIPHERAL)
  }
];

const availableImageManipulationOptions: SelectboxModel[] = [
  {
    label: PeripheralImplementation[PeripheralImplementation.DEFAULT_IMAGE_MANIPULATION_PERIPHERAL],
    value: PeripheralImplementation[PeripheralImplementation.DEFAULT_IMAGE_MANIPULATION_PERIPHERAL],
    disable: isDisabled(peripheralConfigurationRef.value?.availableBackgroundRemovalPeripheralImplementations, PeripheralImplementation.DEFAULT_IMAGE_MANIPULATION_PERIPHERAL)
  }
];

const availableModelCreatorOptions: SelectboxModel[] = [
  {
    label: PeripheralImplementation[PeripheralImplementation.FALLBACK_MODEL_CREATOR_PERIPHERAL],
    value: PeripheralImplementation[PeripheralImplementation.FALLBACK_MODEL_CREATOR_PERIPHERAL],
    disable: isDisabled(peripheralConfigurationRef.value?.availableModelCreatorPeripheralImplementations, PeripheralImplementation.FALLBACK_MODEL_CREATOR_PERIPHERAL)
  },
  {
    label: PeripheralImplementation[PeripheralImplementation.MESHROOM_MODEL_CREATOR_PERIPHERAL],
    value: PeripheralImplementation[PeripheralImplementation.MESHROOM_MODEL_CREATOR_PERIPHERAL],
    disable: isDisabled(peripheralConfigurationRef.value?.availableModelCreatorPeripheralImplementations, PeripheralImplementation.MESHROOM_MODEL_CREATOR_PERIPHERAL)
  },
  {
    label: PeripheralImplementation[PeripheralImplementation.METASHAPE_MODEL_CREATOR_PERIPHERAL],
    value: PeripheralImplementation[PeripheralImplementation.METASHAPE_MODEL_CREATOR_PERIPHERAL],
    disable: isDisabled(peripheralConfigurationRef.value?.availableModelCreatorPeripheralImplementations, PeripheralImplementation.METASHAPE_MODEL_CREATOR_PERIPHERAL)
  },
  {
    label: PeripheralImplementation[PeripheralImplementation.REALITY_SCAN_MODEL_CREATOR_PERIPHERAL],
    value: PeripheralImplementation[PeripheralImplementation.REALITY_SCAN_MODEL_CREATOR_PERIPHERAL],
    disable: isDisabled(peripheralConfigurationRef.value?.availableModelCreatorPeripheralImplementations, PeripheralImplementation.REALITY_SCAN_MODEL_CREATOR_PERIPHERAL)
  }
];

const availableModelEditorOptions: SelectboxModel[] = [
  {
    label: PeripheralImplementation[PeripheralImplementation.FALLBACK_MODEL_EDITOR_PERIPHERAL],
    value: PeripheralImplementation[PeripheralImplementation.FALLBACK_MODEL_EDITOR_PERIPHERAL],
    disable: isDisabled(peripheralConfigurationRef.value?.availableModelEditorPeripheralImplementations, PeripheralImplementation.FALLBACK_MODEL_EDITOR_PERIPHERAL)
  },
  {
    label: PeripheralImplementation[PeripheralImplementation.BLENDER_MODEL_EDITOR_PERIPHERAL],
    value: PeripheralImplementation[PeripheralImplementation.BLENDER_MODEL_EDITOR_PERIPHERAL],
    disable: isDisabled(peripheralConfigurationRef.value?.availableModelEditorPeripheralImplementations, PeripheralImplementation.BLENDER_MODEL_EDITOR_PERIPHERAL)
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
