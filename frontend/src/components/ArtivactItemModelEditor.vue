<template>
  <div>
    <q-btn
      v-if="profilesStore.isDesktopModeEnabled"
      text-color="primary"
      class="q-mr-md"
      round
      dense
      color="accent"
      icon="folder"
      @click="openModelsDir">
      <q-tooltip>{{ $t('ArtivactModelEditor.tooltip.open') }}</q-tooltip>
    </q-btn>
    <q-btn
      text-color="primary"
      round
      dense
      color="accent"
      icon="upload_file"
      @click="showUploadFilesModalRef = true">
      <q-tooltip>{{ $t('ArtivactModelEditor.tooltip.upload') }}</q-tooltip>
    </q-btn>

    <div class="row">
      <draggable
        :list="modelsRef"
        handle=".model-move-icon"
        item-key="fileName"
        group="models"
        class="row">
        <template #item="{ element }">
          <q-card class="model-card q-mr-md q-mt-md">
            <q-img src="logos/gltf-logo.png" class="model-card-img" fit="none">
              <div class="absolute-bottom">
                <div class="text-h6">{{ element.fileName }}</div>
              </div>
            </q-img>
            <q-card-actions>
              <q-icon
                name="drag_indicator"
                class="model-move-icon"
                size="md">
                <q-tooltip>{{ $t('ArtivactModelEditor.tooltip.move') }}</q-tooltip>
              </q-icon>
              <q-space/>
              <q-btn
                icon="delete"
                round
                dense
                flat
                size="md"
                color="primary"
                @click="showDeleteModelSetConfirm(element)">
                <q-tooltip>{{ $t('ArtivactModelEditor.tooltip.delete') }}</q-tooltip>
              </q-btn>
            </q-card-actions>
          </q-card>
        </template>
      </draggable>

      <!-- UPLOAD FILES -->
      <artivact-dialog :dialog-model="showUploadFilesModalRef" :hide-buttons="true"
                       :show-close-button="true" @close-dialog="showUploadFilesModalRef = false">
        <template v-slot:header>
          {{ $t('ArtivactModelEditor.dialog.upload.heading') }}
        </template>

        <template v-slot:body>
          <q-card-section>
            <q-uploader
              :url="'/api/item/' + itemId + '/model'"
              :label="$t('ArtivactModelEditor.dialog.upload.label')"
              multiple
              class="uploader q-mb-md full-width"
              accept=".glb"
              field-name="file"
              :no-thumbnails="true"
              @finish="showUploadFilesModalRef = false; $emit('uploaded')"
            />
          </q-card-section>
        </template>
      </artivact-dialog>

      <!-- DELETE CONFIRMATION DIALOG -->
      <artivact-dialog :dialog-model="confirmDeleteRef" :warn="true">
        <template v-slot:header>
          {{ $t('ArtivactModelEditor.dialog.delete.heading') }}
        </template>

        <template v-slot:body>
          <q-card-section>
            {{ $t('ArtivactModelEditor.dialog.delete.description') }}
          </q-card-section>
        </template>

        <template v-slot:cancel>
          <q-btn :label="$t('Common.cancel')" color="primary" @click="confirmDeleteRef = false"/>
        </template>

        <template v-slot:approve>
          <q-btn
            :label="$t('ArtivactModelEditor.dialog.delete.approve')"
            color="primary"
            @click="deleteModel"
          />
        </template>
      </artivact-dialog>

    </div>
  </div>
</template>

<script setup lang="ts">
// noinspection ES6UnusedImports
import draggable from 'vuedraggable';
import {PropType, ref, toRef} from 'vue';
import {Asset} from 'components/artivact-models';
import {api} from 'boot/axios';
import {useQuasar} from 'quasar';
import {useI18n} from 'vue-i18n';
import ArtivactDialog from 'components/ArtivactDialog.vue';
import {useProfilesStore} from 'stores/profiles';

const quasar = useQuasar();
const i18n = useI18n();

const profilesStore = useProfilesStore();

const props = defineProps({
  itemId: {
    required: true,
    type: String,
  },
  models: {
    required: true,
    type: Object as PropType<Asset[]>,
    default: [] as Asset[],
  },
});

const modelsRef = toRef(props, 'models');

const showUploadFilesModalRef = ref(false);

const confirmDeleteRef = ref(false);
let selectedModelIndex: number;

function openModelsDir() {
  api
    .put('/api/item/' + props.itemId + '/media-creation/open-models-dir')
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ArtivactModelEditor.messages.openFailed'),
        icon: 'report_problem',
      });
    });
}

function showDeleteModelSetConfirm(element: Asset) {
  selectedModelIndex = modelsRef.value?.indexOf(element)
  confirmDeleteRef.value = true;
}

function deleteModel() {
  modelsRef.value?.splice(selectedModelIndex, 1);
  confirmDeleteRef.value = false;
}

</script>

<style scoped>
.model-card {
  width: 200px;
}

.model-card-img {
  width: 200px;
  height: 200px;
}

.model-move-icon:hover {
  cursor: grab;
}

</style>
