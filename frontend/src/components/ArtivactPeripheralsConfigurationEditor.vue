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
              {{
                $t(
                  'ArtivactPeripheralsConfigurationEditor.turntable.description',
                )
              }}
            </div>
            <artivact-peripheral-config-overview
              v-for="(
                peripheralConfig, index
              ) in peripheralConfigurationRef.turntablePeripheralConfigs"
              :peripheral-config="peripheralConfig"
              @delete-config="deleteTurntableConfig(index)"
              @edit-config="editTurntableConfig(index)"
            ></artivact-peripheral-config-overview>
            <div class="row q-mt-md">
              <div class="col"></div>
              <q-btn
                round
                dense
                @click="addTurntableConfig()"
                icon="add"
                color="primary"
              />
            </div>
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
              {{
                $t('ArtivactPeripheralsConfigurationEditor.camera.description')
              }}
            </div>
            <artivact-peripheral-config-overview
              v-for="(
                peripheralConfig, index
              ) in peripheralConfigurationRef.cameraPeripheralConfigs"
              :peripheral-config="peripheralConfig"
              @delete-config="deleteCameraConfig(index)"
              @edit-config="editCameraConfig(index)"
            ></artivact-peripheral-config-overview>
            <div class="row q-mt-md">
              <div class="col"></div>
              <q-btn
                round
                dense
                @click="addCameraConfig()"
                icon="add"
                color="primary"
              />
            </div>
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
            {{
              $t('ArtivactPeripheralsConfigurationEditor.background.heading')
            }}
          </q-item-section>
        </template>
        <q-card>
          <q-card-section>
            <div class="q-mb-md">
              {{
                $t(
                  'ArtivactPeripheralsConfigurationEditor.background.description',
                )
              }}
            </div>
            <artivact-peripheral-config-overview
              v-for="(
                peripheralConfig, index
              ) in peripheralConfigurationRef.imageBackgroundRemovalPeripheralConfigs"
              :peripheral-config="peripheralConfig"
              @delete-config="deleteBackgroundRemovalConfig(index)"
              @edit-config="editBackgroundRemovalConfig(index)"
            ></artivact-peripheral-config-overview>
            <div class="row q-mt-md">
              <div class="col"></div>
              <q-btn
                round
                dense
                @click="addBackgroundRemovalConfig()"
                icon="add"
                color="primary"
              />
            </div>
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
              {{
                $t('ArtivactPeripheralsConfigurationEditor.creator.description')
              }}
            </div>
            <artivact-peripheral-config-overview
              v-for="(
                peripheralConfig, index
              ) in peripheralConfigurationRef.modelCreatorPeripheralConfigs"
              :peripheral-config="peripheralConfig"
              @delete-config="deleteModelCreatorConfig(index)"
              @edit-config="editModelCreatorConfig(index)"
            ></artivact-peripheral-config-overview>
            <div class="row q-mt-md">
              <div class="col"></div>
              <q-btn
                round
                dense
                @click="addModelCreatorConfig()"
                icon="add"
                color="primary"
              />
            </div>
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
              {{
                $t('ArtivactPeripheralsConfigurationEditor.editor.description')
              }}
            </div>
            <artivact-peripheral-config-overview
              v-for="(
                peripheralConfig, index
              ) in peripheralConfigurationRef.modelEditorPeripheralConfigs"
              :peripheral-config="peripheralConfig"
              @delete-config="deleteModelEditorConfig(index)"
              @edit-config="editModelEditorConfig(index)"
            ></artivact-peripheral-config-overview>
            <div class="row q-mt-md">
              <div class="col"></div>
              <q-btn
                round
                dense
                @click="addModelEditorConfig()"
                icon="add"
                color="primary"
              />
            </div>
          </q-card-section>
        </q-card>
      </q-expansion-item>
    </q-list>
  </div>

  <artivact-peripheral-config-editor
    :dialog-model="showTurntablePeripheralConfigEditorRef"
    :peripheral-config="curPeripheralConfigRef"
    :available-options="availableTurntableOptions"
    :show-cancel-button="curPeripheralConfigIndexRef == null"
    @cancel="showTurntablePeripheralConfigEditorRef = false"
    @save="saveTurntableConfig()"
  >
    <template v-slot:header>
      {{ $t('ArtivactPeripheralConfigEditor.heading.turntable') }}
    </template>
  </artivact-peripheral-config-editor>

  <artivact-peripheral-config-editor
    :dialog-model="showCameraPeripheralConfigEditorRef"
    :peripheral-config="curPeripheralConfigRef"
    :available-options="availableCameraOptions"
    :show-cancel-button="curPeripheralConfigIndexRef == null"
    @cancel="showCameraPeripheralConfigEditorRef = false"
    @save="saveCameraConfig()"
  >
    <template v-slot:header>
      {{ $t('ArtivactPeripheralConfigEditor.heading.camera') }}
    </template>
  </artivact-peripheral-config-editor>

  <artivact-peripheral-config-editor
    :dialog-model="showBackgroundRemovalPeripheralConfigEditorRef"
    :peripheral-config="curPeripheralConfigRef"
    :available-options="availableBackgroundRemovalOptions"
    :show-cancel-button="curPeripheralConfigIndexRef == null"
    @cancel="showBackgroundRemovalPeripheralConfigEditorRef = false"
    @save="saveBackgroundRemovalConfig()"
  >
    <template v-slot:header>
      {{ $t('ArtivactPeripheralConfigEditor.heading.backgroundRemoval') }}
    </template>
  </artivact-peripheral-config-editor>

  <artivact-peripheral-config-editor
    :dialog-model="showModelCreatorPeripheralConfigEditorRef"
    :peripheral-config="curPeripheralConfigRef"
    :available-options="availableModelCreatorOptions"
    :show-cancel-button="curPeripheralConfigIndexRef == null"
    @cancel="showModelCreatorPeripheralConfigEditorRef = false"
    @save="saveModelCreatorConfig()"
  >
    <template v-slot:header>
      {{ $t('ArtivactPeripheralConfigEditor.heading.modelCreator') }}
    </template>
  </artivact-peripheral-config-editor>

  <artivact-peripheral-config-editor
    :dialog-model="showModelEditorPeripheralConfigEditorRef"
    :peripheral-config="curPeripheralConfigRef"
    :available-options="availableModelEditorOptions"
    :show-cancel-button="curPeripheralConfigIndexRef == null"
    @cancel="showModelEditorPeripheralConfigEditorRef = false"
    @save="saveModelEditorConfig()"
  >
    <template v-slot:header>
      {{ $t('ArtivactPeripheralConfigEditor.heading.modelEditor') }}
    </template>
  </artivact-peripheral-config-editor>
</template>

<script setup lang="ts">
import { PropType, ref, toRef } from 'vue';
import {
  PeripheralConfig,
  PeripheralImplementation,
  PeripheralsConfiguration,
  SelectboxModel,
} from './artivact-models';
import ArtivactPeripheralConfigOverview from './ArtivactPeripheralConfigOverview.vue';
import ArtivactPeripheralConfigEditor from './ArtivactPeripheralConfigEditor.vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  peripheralConfiguration: {
    required: true,
    type: Object as PropType<PeripheralsConfiguration | null>,
  },
});

const i18n = useI18n();

const peripheralConfigurationRef = toRef(props, 'peripheralConfiguration');
const curPeripheralConfigRef = ref(null);
const curPeripheralConfigIndexRef = ref(null);

const showTurntablePeripheralConfigEditorRef = ref(false);
const showCameraPeripheralConfigEditorRef = ref(false);
const showBackgroundRemovalPeripheralConfigEditorRef = ref(false);
const showModelCreatorPeripheralConfigEditorRef = ref(false);
const showModelEditorPeripheralConfigEditorRef = ref(false);

function addTurntableConfig() {
  curPeripheralConfigRef.value = {
    label: i18n.t('ArtivactPeripheralConfigEditor.label.default'),
  } as PeripheralConfig;
  curPeripheralConfigRef.value.peripheralImplementation =
    PeripheralImplementation[availableTurntableOptions[0].value];
  curPeripheralConfigIndexRef.value = null;
  showTurntablePeripheralConfigEditorRef.value = true;
}

function deleteTurntableConfig(index: number) {
  peripheralConfigurationRef.value.turntablePeripheralConfigs.splice(index, 1);
}

function editTurntableConfig(index: number) {
  curPeripheralConfigRef.value =
    peripheralConfigurationRef.value.turntablePeripheralConfigs[index];
  curPeripheralConfigIndexRef.value = index;
  showTurntablePeripheralConfigEditorRef.value = true;
}

function saveTurntableConfig() {
  if (curPeripheralConfigIndexRef.value == null) {
    peripheralConfigurationRef.value.turntablePeripheralConfigs.push(
      curPeripheralConfigRef.value,
    );
  } else {
    peripheralConfigurationRef.value.turntablePeripheralConfigs[
      curPeripheralConfigIndexRef.value
    ] = curPeripheralConfigRef.value;
  }
  showTurntablePeripheralConfigEditorRef.value = false;
  curPeripheralConfigRef.value = null;
}

function addCameraConfig() {
  curPeripheralConfigRef.value = {
    label: i18n.t('ArtivactPeripheralConfigEditor.label.default'),
  } as PeripheralConfig;
  curPeripheralConfigRef.value.peripheralImplementation =
    PeripheralImplementation[availableCameraOptions[0].value];
  curPeripheralConfigIndexRef.value = null;
  showCameraPeripheralConfigEditorRef.value = true;
}

function deleteCameraConfig(index: number) {
  peripheralConfigurationRef.value.cameraPeripheralConfigs.splice(index, 1);
}

function editCameraConfig(index: number) {
  curPeripheralConfigRef.value =
    peripheralConfigurationRef.value.cameraPeripheralConfigs[index];
  curPeripheralConfigIndexRef.value = index;
  showCameraPeripheralConfigEditorRef.value = true;
}

function saveCameraConfig() {
  if (curPeripheralConfigIndexRef.value == null) {
    peripheralConfigurationRef.value.cameraPeripheralConfigs.push(
      curPeripheralConfigRef.value,
    );
  } else {
    peripheralConfigurationRef.value.cameraPeripheralConfigs[
      curPeripheralConfigIndexRef.value
    ] = curPeripheralConfigRef.value;
  }
  showCameraPeripheralConfigEditorRef.value = false;
  curPeripheralConfigRef.value = null;
}

function addBackgroundRemovalConfig() {
  curPeripheralConfigRef.value = {
    label: i18n.t('ArtivactPeripheralConfigEditor.label.default'),
  } as PeripheralConfig;
  curPeripheralConfigRef.value.peripheralImplementation =
    PeripheralImplementation[availableBackgroundRemovalOptions[0].value];
  curPeripheralConfigIndexRef.value = null;
  showBackgroundRemovalPeripheralConfigEditorRef.value = true;
}

function deleteBackgroundRemovalConfig(index: number) {
  peripheralConfigurationRef.value.imageBackgroundRemovalPeripheralConfigs.splice(
    index,
    1,
  );
}

function editBackgroundRemovalConfig(index: number) {
  curPeripheralConfigRef.value =
    peripheralConfigurationRef.value.imageBackgroundRemovalPeripheralConfigs[
      index
    ];
  curPeripheralConfigIndexRef.value = index;
  showBackgroundRemovalPeripheralConfigEditorRef.value = true;
}

function saveBackgroundRemovalConfig() {
  if (curPeripheralConfigIndexRef.value == null) {
    peripheralConfigurationRef.value.imageBackgroundRemovalPeripheralConfigs.push(
      curPeripheralConfigRef.value,
    );
  } else {
    peripheralConfigurationRef.value.imageBackgroundRemovalPeripheralConfigs[
      curPeripheralConfigIndexRef.value
    ] = curPeripheralConfigRef.value;
  }
  showBackgroundRemovalPeripheralConfigEditorRef.value = false;
  curPeripheralConfigRef.value = null;
}

function addModelCreatorConfig() {
  curPeripheralConfigRef.value = {
    label: i18n.t('ArtivactPeripheralConfigEditor.label.default'),
  } as PeripheralConfig;
  curPeripheralConfigRef.value.peripheralImplementation =
    PeripheralImplementation[availableModelCreatorOptions[0].value];
  curPeripheralConfigIndexRef.value = null;
  showModelCreatorPeripheralConfigEditorRef.value = true;
}

function deleteModelCreatorConfig(index: number) {
  peripheralConfigurationRef.value.modelCreatorPeripheralConfigs.splice(
    index,
    1,
  );
}

function editModelCreatorConfig(index: number) {
  curPeripheralConfigRef.value =
    peripheralConfigurationRef.value.modelCreatorPeripheralConfigs[index];
  curPeripheralConfigIndexRef.value = index;
  showModelCreatorPeripheralConfigEditorRef.value = true;
}

function saveModelCreatorConfig() {
  if (curPeripheralConfigIndexRef.value == null) {
    peripheralConfigurationRef.value.modelCreatorPeripheralConfigs.push(
      curPeripheralConfigRef.value,
    );
  } else {
    peripheralConfigurationRef.value.modelCreatorPeripheralConfigs[
      curPeripheralConfigIndexRef.value
    ] = curPeripheralConfigRef.value;
  }
  showModelCreatorPeripheralConfigEditorRef.value = false;
  curPeripheralConfigRef.value = null;
}

function addModelEditorConfig() {
  curPeripheralConfigRef.value = {
    label: i18n.t('ArtivactPeripheralConfigEditor.label.default'),
  } as PeripheralConfig;
  curPeripheralConfigRef.value.peripheralImplementation =
    PeripheralImplementation[availableModelEditorOptions[0].value];
  curPeripheralConfigIndexRef.value = null;
  showModelEditorPeripheralConfigEditorRef.value = true;
}

function deleteModelEditorConfig(index: number) {
  peripheralConfigurationRef.value.modelEditorPeripheralConfigs.splice(
    index,
    1,
  );
}

function editModelEditorConfig(index: number) {
  curPeripheralConfigRef.value =
    peripheralConfigurationRef.value.modelEditorPeripheralConfigs[index];
  curPeripheralConfigIndexRef.value = index;
  showModelEditorPeripheralConfigEditorRef.value = true;
}

function saveModelEditorConfig() {
  if (curPeripheralConfigIndexRef.value == null) {
    peripheralConfigurationRef.value.modelEditorPeripheralConfigs.push(
      curPeripheralConfigRef.value,
    );
  } else {
    peripheralConfigurationRef.value.modelEditorPeripheralConfigs[
      curPeripheralConfigIndexRef.value
    ] = curPeripheralConfigRef.value;
  }
  showModelEditorPeripheralConfigEditorRef.value = false;
  curPeripheralConfigRef.value = null;
}

function isDisabled(
  availableOptions: PeripheralImplementation[] | undefined,
  option: PeripheralImplementation,
): boolean {
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
    label:
      PeripheralImplementation[
        PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL
      ],
    value:
      PeripheralImplementation[
        PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL
      ],
    disable: isDisabled(
      peripheralConfigurationRef.value
        ?.availableTurntablePeripheralImplementations,
      PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL,
    ),
  },
];

const availableCameraOptions: SelectboxModel[] = [
  {
    label:
      PeripheralImplementation[PeripheralImplementation.PTP_CAMERA_PERIPHERAL],
    value:
      PeripheralImplementation[PeripheralImplementation.PTP_CAMERA_PERIPHERAL],
    disable: isDisabled(
      peripheralConfigurationRef.value
        ?.availableCameraPeripheralImplementations,
      PeripheralImplementation.PTP_CAMERA_PERIPHERAL,
    ),
  },
  {
    label:
      PeripheralImplementation[
        PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL
      ],
    value:
      PeripheralImplementation[
        PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL
      ],
    disable: isDisabled(
      peripheralConfigurationRef.value
        ?.availableCameraPeripheralImplementations,
      PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL,
    ),
  },
];

const availableBackgroundRemovalOptions: SelectboxModel[] = [
  {
    label:
      PeripheralImplementation[
        PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL
      ],
    value:
      PeripheralImplementation[
        PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL
      ],
    disable: isDisabled(
      peripheralConfigurationRef.value
        ?.availableImageBackgroundRemovalPeripheralImplementations,
      PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL,
    ),
  },
];

const availableModelCreatorOptions: SelectboxModel[] = [
  {
    label:
      PeripheralImplementation[
        PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL
      ],
    value:
      PeripheralImplementation[
        PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL
      ],
    disable: isDisabled(
      peripheralConfigurationRef.value
        ?.availableModelCreatorPeripheralImplementations,
      PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL,
    ),
  },
];

const availableModelEditorOptions: SelectboxModel[] = [
  {
    label:
      PeripheralImplementation[
        PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL
      ],
    value:
      PeripheralImplementation[
        PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL
      ],
    disable: isDisabled(
      peripheralConfigurationRef.value
        ?.availableModelEditorPeripheralImplementations,
      PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL,
    ),
  },
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
