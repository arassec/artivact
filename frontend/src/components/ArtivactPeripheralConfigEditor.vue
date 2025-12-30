<template>
  <artivact-dialog
    :dialog-model="dialogModel"
    v-if="peripheralConfigRef && peripheralConfigRef.peripheralImplementation"
  >
    <template v-slot:header>
      <div>
        <slot name="header"/>
        <q-btn
          class="float-right"
          round
          dense
          flat
          icon="auto_fix_high"
          v-if="
            peripheralConfigRef.peripheralImplementation.toString() ===
            PeripheralImplementation[
              PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL
            ]
          "
        >
          <q-menu>
            <q-list>
              <q-item
                clickable
                v-close-popup
                @click="fillGphotoTwo()"
                v-if="Platform.is.linux"
              >
                <q-item-section>gphoto2</q-item-section>
              </q-item>
              <q-item
                clickable
                v-close-popup
                @click="fillDigiCamControl()"
                v-if="Platform.is.win"
              >
                <q-item-section>DigiCamControl</q-item-section>
              </q-item>
            </q-list>
          </q-menu>
        </q-btn>
        <q-btn
          class="float-right"
          round
          dense
          flat
          icon="auto_fix_high"
          v-if="
            peripheralConfigRef.peripheralImplementation.toString() ===
            PeripheralImplementation[
              PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL
            ]
          "
        >
          <q-menu>
            <q-list>
              <q-item clickable v-close-popup @click="fillSilueta()">
                <q-item-section>Silueta</q-item-section>
              </q-item>
            </q-list>
          </q-menu>
        </q-btn>
        <q-btn
          class="float-right"
          round
          dense
          flat
          icon="auto_fix_high"
          v-if="
            peripheralConfigRef.peripheralImplementation.toString() ===
            PeripheralImplementation[
              PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL
            ]
          "
        >
          <q-menu>
            <q-list>
              <q-item clickable v-close-popup @click="fillMeshroom(false)">
                <q-item-section>Meshroom 2025.1</q-item-section>
              </q-item>
              <q-item clickable v-close-popup @click="fillMeshroom(true)">
                <q-item-section>Meshroom 2025.1 (Headless)</q-item-section>
              </q-item>
              <q-item clickable v-close-popup @click="fillMetashape()">
                <q-item-section>Metashape 2.2</q-item-section>
              </q-item>
              <q-item
                clickable
                v-close-popup
                @click="fillRealityScan(false)"
                v-if="Platform.is.win"
              >
                <q-item-section>RealityScan</q-item-section>
              </q-item>
              <q-item
                clickable
                v-close-popup
                @click="fillRealityScan(true)"
                v-if="Platform.is.win"
              >
                <q-item-section>RealityScan (Headless)</q-item-section>
              </q-item>
            </q-list>
          </q-menu>
        </q-btn>
        <q-btn
          class="float-right"
          round
          dense
          flat
          icon="auto_fix_high"
          v-if="
            peripheralConfigRef.peripheralImplementation.toString() ===
            PeripheralImplementation[
              PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL
            ]
          "
        >
          <q-menu>
            <q-list>
              <q-item
                clickable
                v-close-popup
                @click="fillBlender()"
                v-if="Platform.is.linux"
              >
                <q-item-section>Blender 3D</q-item-section>
              </q-item>
            </q-list>
          </q-menu>
        </q-btn>
      </div>
    </template>

    <template v-slot:body>
      <q-card-section>
        <!-- GENERAL CONFIGURATION -->
        <q-input
          :error="peripheralConfigRef.label.length == 0"
          outlined
          :label="$t('ArtivactPeripheralConfigEditor.label.label')"
          v-model="peripheralConfigRef.label"
        />

        <q-select
          :disable="availableOptionsRef.length == 1"
          class="q-mb-md"
          outlined
          emit-value
          v-model="peripheralConfigRef.peripheralImplementation"
          :options="availableOptions"
          :option-label="(opt) => (opt.label ? $t(opt.label) : $t(opt))"
          :label="$t('ArtivactPeripheralConfigEditor.label.implementation')"
        />

        <q-separator class="q-mb-md"></q-separator>

        <!-- TURNTABLE SPECIFIC -->
        <div
          v-if="
            peripheralConfigRef.peripheralImplementation.toString() ===
            PeripheralImplementation[
              PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL
            ]
          "
        >
          <div class="q-mb-sm">
            {{
              $t('ArtivactPeripheralConfigEditor.turntable.delayDescription')
            }}
          </div>
          <q-input
            outlined
            type="number"
            :label="$t('ArtivactPeripheralConfigEditor.turntable.delay')"
            v-model.number="
              (peripheralConfigRef as ArduinoTurntablePeripheralConfig)
                .delayInMilliseconds
            "
          />
        </div>

        <!-- ONNX BACKGROUND REMOVAL SPECIFIC -->
        <div
          class="full-width"
          v-if="
            peripheralConfigRef.peripheralImplementation.toString() ===
            PeripheralImplementation[
              PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL
            ]
          "
        >
          <div class="q-mb-sm q-ml-xs">
            {{
              $t(
                'ArtivactPeripheralConfigEditor.background.argumentsDescription',
              ).replaceAll('$_projectDir_$', '{projectDir}')
            }}
          </div>

          <q-input
            class="q-mb-md"
            outlined
            :label="$t('ArtivactPeripheralConfigEditor.onnx.onnxModelFile')"
            v-model="
              (peripheralConfigRef as OnnxBackgroundRemovalPeripheralConfig)
                .onnxModelFile
            "
          />
          <q-input
            class="q-mb-md"
            outlined
            :label="
              $t('ArtivactPeripheralConfigEditor.onnx.inputParameterName')
            "
            v-model="
              (peripheralConfigRef as OnnxBackgroundRemovalPeripheralConfig)
                .inputParameterName
            "
          />
          <q-input
            class="q-mb-md"
            outlined
            :label="$t('ArtivactPeripheralConfigEditor.onnx.imageWidth')"
            type="number"
            v-model.number="
              (peripheralConfigRef as OnnxBackgroundRemovalPeripheralConfig)
                .imageWidth
            "
          />
          <q-input
            class="q-mb-md"
            outlined
            :label="$t('ArtivactPeripheralConfigEditor.onnx.imageHeight')"
            type="number"
            v-model.number="
              (peripheralConfigRef as OnnxBackgroundRemovalPeripheralConfig)
                .imageHeight
            "
          />
          <q-input
            class="q-mb-md"
            outlined
            :label="$t('ArtivactPeripheralConfigEditor.onnx.numThreads')"
            type="number"
            v-model.number="
              (peripheralConfigRef as OnnxBackgroundRemovalPeripheralConfig)
                .numThreads
            "
          />
        </div>

        <!-- PTP CAMERA SPECIFIC -->
        <div
          v-if="
            peripheralConfigRef.peripheralImplementation.toString() ===
            PeripheralImplementation[
              PeripheralImplementation.PTP_CAMERA_PERIPHERAL
            ]
          "
        >
          <div class="q-mb-sm">
            {{ $t('ArtivactPeripheralConfigEditor.camera.delayDescription') }}
          </div>
          <q-input
            outlined
            type="number"
            :label="$t('ArtivactPeripheralConfigEditor.camera.delay')"
            v-model.number="
              (peripheralConfigRef as PtpCameraPeripheralConfig)
                .delayInMilliseconds
            "
          />
        </div>

        <!-- EXTERNAL PROGRAM SPECIFIC -->
        <div
          class="full-width"
          v-if="
            peripheralConfigRef.peripheralImplementation.toString() ===
              PeripheralImplementation[
                PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL
              ] ||
            peripheralConfigRef.peripheralImplementation.toString() ===
              PeripheralImplementation[
                PeripheralImplementation
                  .EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL
              ] ||
            peripheralConfigRef.peripheralImplementation.toString() ===
              PeripheralImplementation[
                PeripheralImplementation
                  .EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL
              ]
          "
        >
          <div
            v-if="
              peripheralConfigRef.peripheralImplementation.toString() ===
              PeripheralImplementation[
                PeripheralImplementation
                  .EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL
              ]
            "
          >
            <div class="q-mb-sm q-ml-xs">
              {{
                $t(
                  'ArtivactPeripheralConfigEditor.modelCreator.description',
                ).replaceAll('$_projectDir_$', '{projectDir}')
              }}
            </div>

            <q-checkbox
              class="q-mb-sm"
              :label="
                $t(
                  'ArtivactPeripheralConfigEditor.modelCreator.openInputDirInOs',
                )
              "
              v-model="
                (peripheralConfigRef as ModelCreatorPeripheralConfig)
                  .openInputDirInOs
              "
            />
            <q-input
              class="q-mb-md"
              outlined
              :label="
                $t('ArtivactPeripheralConfigEditor.modelCreator.resultDir')
              "
              v-model="
                (peripheralConfigRef as ModelCreatorPeripheralConfig).resultDir
              "
            />
          </div>

          <div
            class="full-width"
            v-if="
              peripheralConfigRef.peripheralImplementation.toString() ===
              PeripheralImplementation[
                PeripheralImplementation
                  .EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL
              ]
            "
          >
            <div class="q-mb-sm q-ml-xs">
              {{
                $t('ArtivactPeripheralConfigEditor.modelEditor.description')
                  .replaceAll('$_projectDir_$', '{projectDir}')
                  .replaceAll('$_modelDir_$', '{modelDir}')
              }}
            </div>
          </div>

          <q-input
            class="q-mb-md"
            outlined
            :label="
              $t('ArtivactPeripheralConfigEditor.externalProgram.command')
            "
            v-model="
              (peripheralConfigRef as ExternalProgramPeripheralConfig).command
            "
          />

          <div
            class="q-mb-sm q-ml-xs"
            v-if="
              peripheralConfigRef.peripheralImplementation.toString() ===
              PeripheralImplementation[
                PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL
              ]
            "
          >
            {{
              $t(
                'ArtivactPeripheralConfigEditor.camera.argumentsDescription',
              ).replace('$_targetFile_$', '{targetFile}')
            }}
          </div>

          <q-input
            type="textarea"
            class="q-mb-md"
            outlined
            :label="
              $t('ArtivactPeripheralConfigEditor.externalProgram.arguments')
            "
            v-model="
              (peripheralConfigRef as ExternalProgramPeripheralConfig).arguments
            "
          />
        </div>
      </q-card-section>
    </template>

    <template v-slot:cancel>
      <q-btn
        :disable="!formValid"
        :label="$t('Common.test')"
        color="primary"
        @click="testPeripheralConfiguration()"
      />
      <div
        class="q-ml-sm"
        v-if="
          !peripheralStatusRef &&
          defaultPeripheralStatusRef &&
          defaultPeripheralStatusRef != 'AVAILABLE'
        "
      >
        <q-icon name="bolt" size="sm" color="negative"></q-icon>
        {{ $t('PeripheralStatus.' + defaultPeripheralStatusRef) }}
      </div>
      <div
        class="q-ml-sm"
        v-if="
          !peripheralStatusRef &&
          defaultPeripheralStatusRef &&
          defaultPeripheralStatusRef == 'AVAILABLE'
        "
      >
        <q-icon name="check" size="sm" color="positive"></q-icon>
      </div>
      <div
        class="q-ml-sm"
        v-if="peripheralStatusRef && peripheralStatusRef != 'AVAILABLE'"
      >
        <q-icon name="bolt" size="sm" color="negative"></q-icon>
        {{ $t('PeripheralStatus.' + peripheralStatusRef) }}
      </div>
      <div
        class="q-ml-sm"
        v-if="peripheralStatusRef && peripheralStatusRef == 'AVAILABLE'"
      >
        <q-icon name="check" size="sm" color="positive"></q-icon>
      </div>
    </template>
    <template v-slot:approve>
      <q-btn
        v-if="showCancelButton"
        :label="$t('Common.cancel')"
        color="primary"
        @click="$emit('cancel')"
      />
      <q-btn
        :disable="!formValid"
        :label="$t('Common.apply')"
        color="primary"
        @click="$emit('save')"
      />
    </template>
  </artivact-dialog>
</template>

<script setup lang="ts">
import {computed, onMounted, PropType, ref, toRef} from 'vue';
import {
  ArduinoTurntablePeripheralConfig,
  ExternalProgramPeripheralConfig,
  ModelCreatorPeripheralConfig,
  OnnxBackgroundRemovalPeripheralConfig,
  PeripheralConfig,
  PeripheralImplementation,
  PtpCameraPeripheralConfig,
  SelectboxModel,
} from './artivact-models';
import ArtivactDialog from './ArtivactDialog.vue';
import {Platform} from 'quasar';
import {api} from '../boot/axios';

const props = defineProps({
  dialogModel: {
    required: true,
  },
  showCancelButton: {
    required: true,
    default: true,
  },
  peripheralConfig: {
    required: false,
    type: Object as PropType<PeripheralConfig | null>,
  },
  availableOptions: {
    required: true,
    type: Object as PropType<SelectboxModel[]>,
  },
  peripheralStatus: {
    required: false,
    type: String,
  },
});

const peripheralConfigRef = toRef(props, 'peripheralConfig');
const availableOptionsRef = toRef(props, 'availableOptions');
const defaultPeripheralStatusRef = toRef(props, 'peripheralStatus');

const peripheralStatusRef = ref(null);

function testPeripheralConfiguration() {
  api
    .post('/api/configuration/peripheral/test', peripheralConfigRef.value)
    .then((response) => {
      peripheralStatusRef.value = response.data;
    });
}

function fillGphotoTwo() {
  let config = peripheralConfigRef.value as ExternalProgramPeripheralConfig;
  config.command = '/usr/bin/gphoto2';
  config.arguments = '--filename {targetFile}\n--capture-image-and-download';
}

function fillDigiCamControl() {
  let config = peripheralConfigRef.value as ExternalProgramPeripheralConfig;
  config.command = 'C:/Program Files (x86)/digiCamControl/CameraControlCmd.exe';
  config.arguments = '/filename {targetFile}\n/capture';
}

function fillSilueta() {
  let config =
    peripheralConfigRef.value as OnnxBackgroundRemovalPeripheralConfig;
  config.onnxModelFile = '{projectDir}/utils/onnx/silueta.onnx';
  config.inputParameterName = 'input.1';
  config.imageWidth = 320;
  config.imageHeight = 320;
  config.numThreads = 5;
}

function fillMeshroom(headless: boolean) {
  let config = peripheralConfigRef.value as ModelCreatorPeripheralConfig;
  config.openInputDirInOs = false;
  config.resultDir = '{projectDir}/temp/export/';
  if (headless) {
    if (Platform.is.win) {
      config.command = 'C:/Users/<USER>/Tools/Meshroom/meshroom_batch.exe';
    } else if (Platform.is.linux) {
      config.command = '/home/<USER>/Tools/Meshroom/meshroom_batch';
    }
    config.arguments =
      '-i {projectDir}/temp/\n-p photogrammetry\n-o {projectDir}/temp/export/';
  } else {
    if (Platform.is.win) {
      config.command = 'C:/Users/<USER>/Tools/Meshroom/Meshroom.exe';
    } else if (Platform.is.linux) {
      config.command = '/home/<USER>/Tools/Meshroom/Meshroom';
    }
    config.arguments = '-i {projectDir}/temp/\n-p photogrammetry';
  }
}

function fillMetashape() {
  let config = peripheralConfigRef.value as ModelCreatorPeripheralConfig;
  config.openInputDirInOs = true;
  config.resultDir = '{projectDir}/temp/export/';
  if (Platform.is.win) {
    config.command = 'C:/Program Files/Agisoft/Metashape/metashape.exe';
  } else if (Platform.is.linux) {
    config.command = '/home/<USER>/Tools/metashape/metashape.sh';
  }
  config.arguments = '';
}

function fillRealityScan(headless: boolean) {
  let config = peripheralConfigRef.value as ModelCreatorPeripheralConfig;
  config.openInputDirInOs = false;
  config.resultDir = '{projectDir}/temp/export/';
  if (Platform.is.win) {
    config.command =
      'C:/Program Files/Capturing Reality/RealityScan/RealityScan.exe';
    config.arguments =
      '-addFolder {projectDir}/temp/\n-save {projectDir}/temp/MyProject.rcproj';
    if (headless) {
      config.arguments +=
        '\n-headless\n-align\n-setReconstructionRegionAuto\n-calculateNormalModel' +
        '\n-simplify 200000\n-smooth\n-calculateTexture' +
        '\n-exportSelectedModel {projectDir}/temp/export/RealityScanExport.obj' +
        '\n-quit';
    }
  }
}

function fillBlender() {
  let config = peripheralConfigRef.value as ExternalProgramPeripheralConfig;
  if (Platform.is.win) {
    config.command = 'C:/Users/<USER>/Tools/Blender/blender.exe';
  } else if (Platform.is.linux) {
    config.command = '/home/<USER>/Tools/blender/blender';
  }
  config.arguments =
    '--python {projectDir}/utils/Blender/blender-artivact-import.py\n-- {modelDir}';
}

const formValid = computed(() => {
  if (peripheralConfigRef.value) {
    if (peripheralConfigRef.value.label.length == 0) {
      return false;
    }
    if (
      peripheralConfigRef.value.peripheralImplementation.toString() ===
      PeripheralImplementation[
        PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL
          ]
    ) {
      let config =
        peripheralConfigRef.value as OnnxBackgroundRemovalPeripheralConfig;
      if (!config.onnxModelFile || config.onnxModelFile.length === 0) {
        return false;
      }
      if (
        !config.inputParameterName ||
        config.inputParameterName.length === 0
      ) {
        return false;
      }
      if (!config.imageWidth) {
        return false;
      }
      if (!config.imageHeight) {
        return false;
      }
      if (!config.numThreads) {
        return false;
      }
    }
    if (
      peripheralConfigRef.value.peripheralImplementation.toString() ===
        PeripheralImplementation[
            PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL
        ] ||
      peripheralConfigRef.value.peripheralImplementation.toString() ===
        PeripheralImplementation[
            PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL
        ] ||
      peripheralConfigRef.value.peripheralImplementation.toString() ===
        PeripheralImplementation[
            PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL
        ]
    ) {
      let config = peripheralConfigRef.value as ExternalProgramPeripheralConfig;
      if (!config.command || config.command.trim().length === 0) {
        return false;
      }
    }
  }
  return true;
});

onMounted(() => {
  peripheralStatusRef.value = null;
});
</script>

<style scoped></style>
