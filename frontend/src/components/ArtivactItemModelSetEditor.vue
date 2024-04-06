<template>
  <q-btn
    data-test="item-creation-create-model-button"
    text-color="primary"
    class="q-mr-md"
    round
    dense
    color="accent"
    icon="add_circle"
    @click="createModel">
    <q-tooltip>{{ $t('ItemModelSetEditor.tooltip.create') }}</q-tooltip>
  </q-btn>
  <q-btn
    text-color="primary"
    class="q-mr-md"
    round
    dense
    color="accent"
    icon="folder"
    @click="openModelsDir">
    <q-tooltip>{{ $t('ItemModelSetEditor.tooltip.open') }}</q-tooltip>
  </q-btn>

  <div class="row">
    <q-card v-for="(modelSet, index) in creationModelSets" :key="index" class="model-set-card q-mr-md q-mt-md">
      <q-img :src="'logos/' + modelSet.modelSetImage" class="model-set-card-img" fit="none">
        <div class="absolute-bottom">
          <div class="text-h6">{{ modelSet.comment }}</div>
        </div>
      </q-img>
      <q-card-actions>
        <q-btn
          icon="search"
          round
          dense
          flat
          size="md"
          color="primary"
          @click="showModelSetDetails(modelSet, index)">
          <q-tooltip>{{ $t('ItemModelSetEditor.tooltip.details') }}</q-tooltip>
        </q-btn>
        <q-btn
          icon="folder"
          round
          dense
          flat
          size="md"
          color="primary"
          @click="openModelDir(index)">
          <q-tooltip>{{ $t('ItemModelSetEditor.tooltip.openModel') }}</q-tooltip>
        </q-btn>
        <q-btn
          icon="edit"
          round
          dense
          flat
          size="md"
          color="primary"
          @click="editModel(index)">
          <q-tooltip>{{ $t('ItemModelSetEditor.tooltip.edit') }}</q-tooltip>
        </q-btn>
        <q-space/>
        <q-btn
          icon="delete"
          round
          dense
          flat
          size="md"
          color="primary"
          @click="showDeleteModelSetConfirm(index)">
          <q-tooltip>{{ $t('ItemModelSetEditor.tooltip.delete') }}</q-tooltip>
        </q-btn>
      </q-card-actions>
    </q-card>
  </div>

  <!-- MODEL-SET DETAILS -->
  <artivact-dialog :dialog-model="showModelSetDetailsModalRef"
                   v-if="showModelSetDetailsModalRef && modelSetFiles"
                   :hide-buttons="true"
                   :show-close-button="true"
                   :min-width="50"
                   @close-dialog="showModelSetDetailsModalRef = false">
    <template v-slot:header>
      {{ $t('ItemModelSetEditor.dialog.details.heading') }}
    </template>

    <template v-slot:body>
      <q-card-section>
        <div class="row">
          <q-card v-for="(file, index) in modelSetFiles" :key="index" class="image-set-card q-mr-md q-mb-md">
            <q-img :src="'logos/' + file.url" class="model-set-card-img">
              <div class="absolute-bottom">
                <div class="text-h6">{{ file.fileName }}</div>
              </div>
            </q-img>
            <q-card-actions>
              <div class="row full-width">
                <q-btn
                  v-if="file.transferable"
                  icon="move_up"
                  rounded
                  dense
                  flat
                  size="md"
                  color="primary"
                  @click="transferModel(file, selectedModelSetIndex)">
                  <q-tooltip>{{ $t('ItemModelSetEditor.dialog.details.transfer') }}</q-tooltip>
                </q-btn>
                <q-space/>
              </div>
            </q-card-actions>
          </q-card>
        </div>
      </q-card-section>
    </template>
  </artivact-dialog>

  <!-- LONG-RUNNING OPERATION -->
  <artivact-operation-in-progress-dialog :dialog-model="showOperationInProgressModalRef"
                                         :progress-monitor-ref="progressMonitorRef"
                                          @close-dialog="showOperationInProgressModalRef = false"/>

  <!-- DELETE CONFIRMATION DIALOG -->
  <artivact-dialog :dialog-model="confirmDeleteRef" :warn="true">
    <template v-slot:header>
      {{ $t('ItemModelSetEditor.dialog.delete.heading') }}
    </template>

    <template v-slot:body>
      <q-card-section>
        {{ $t('ItemModelSetEditor.dialog.delete.description') }}
      </q-card-section>
    </template>

    <template v-slot:cancel>
      <q-btn :label="$t('Common.cancel')" color="primary" @click="confirmDeleteRef = false"/>
    </template>

    <template v-slot:approve>
      <q-btn
        :label="$t('ItemModelSetEditor.dialog.delete.approve')"
        color="primary"
        @click="deleteModelSet"
      />
    </template>
  </artivact-dialog>

</template>

<script setup lang="ts">

import {PropType, ref} from 'vue';
import {Asset, ModelSet, OperationProgress} from 'components/artivact-models';
import {api} from 'boot/axios';
import {useQuasar} from 'quasar';
import ArtivactDialog from 'components/ArtivactDialog.vue';
import ArtivactOperationInProgressDialog from 'components/ArtivactOperationInProgressDialog.vue';
import {useI18n} from 'vue-i18n';

const quasar = useQuasar();
const i18n = useI18n();

const props = defineProps({
  itemId: {
    required: true,
    type: String,
  },
  creationModelSets: {
    required: true,
    type: Array as PropType<Array<ModelSet>>,
  }
});

const emit = defineEmits<{
  (e: 'delete-model-set-file', modelSetIndex: number, file: Asset): void;
  (e: 'update-item'): void;
}>();

const progressMonitorRef = ref<OperationProgress>();
const showOperationInProgressModalRef = ref(false);

const showModelSetDetailsModalRef = ref(false);
let modelSetFiles: Asset[];
let selectedModelSetIndex: number;

const confirmDeleteRef = ref(false);

function showModelSetDetails(modelSet: ModelSet, modelSetIndex: number) {
  selectedModelSetIndex = modelSetIndex;
  api
    .get('/api/item/' + props.itemId + '/media-creation/model-set-files/' + modelSetIndex)
    .then((response) => {
      if (response) {
        modelSetFiles = response.data;
        showModelSetDetailsModalRef.value = true;
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemModelSetEditor.messages.loadingFailed'),
        icon: 'report_problem',
      });
    });
}

function createModel() {
  api
    .post('/api/item/' + props.itemId + '/media-creation/create-model-set')
    .then((response) => {
      if (response) {
        showOperationInProgressModalRef.value = true;
        progressMonitorRef.value = response.data;
        updateOperationProgress();
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemModelSetEditor.messages.creationFailed'),
        icon: 'report_problem',
      });
    });
}

function openModelsDir() {
  api
    .put('/api/item/' + props.itemId + '/media-creation/open-models-dir')
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemModelSetEditor.messages.openFailed'),
        icon: 'report_problem',
      });
    });
}

function editModel(index: number) {
  api
    .post('/api/item/' + props.itemId + '/media-creation/edit-model/' + index)
    .then((response) => {
      if (response) {
        showOperationInProgressModalRef.value = true;
        progressMonitorRef.value = response.data;
        updateOperationProgress();
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemModelSetEditor.messages.editingFailed'),
        icon: 'report_problem',
      });
    });
}

function openModelDir(index: number) {
  api
    .put('/api/item/' + props.itemId + '/media-creation/open-model-dir/' + index)
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemModelSetEditor.messages.openFailed'),
        icon: 'report_problem',
      });
    });
}

function transferModel(file: Asset, modelSetIndex: number) {
  api
    .put('/api/item/' + props.itemId + '/media-creation/transfer-model/' + modelSetIndex, file)
    .then((response) => {
      if (response) {
        emit('update-item');
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: i18n.t('ItemModelSetEditor.messages.transferSuccess'),
          icon: 'check',
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemModelSetEditor.messages.transferFailed'),
        icon: 'report_problem',
      });
    });
}

function showDeleteModelSetConfirm(modelSetIndex: number) {
  selectedModelSetIndex = modelSetIndex;
  confirmDeleteRef.value = true;
}

function deleteModelSet() {
  confirmDeleteRef.value = false;
  api
    .delete('/api/item/' + props.itemId + '/media-creation/model-set/' + selectedModelSetIndex)
    .then((response) => {
      if (response) {
        emit('update-item');
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: i18n.t('ItemModelSetEditor.messages.deleteSuccess'),
          icon: 'check',
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemModelSetEditor.messages.deleteFailed'),
        icon: 'report_problem',
      });
    });
}

function updateOperationProgress() {
  api
    .get('/api/item/' + props.itemId + '/media-creation/progress')
    .then((response) => {
      if (response.data) {
        progressMonitorRef.value = response.data;
        if (!progressMonitorRef.value?.error) {
          setTimeout(() => updateOperationProgress(), 1000);
        }
      } else {
        progressMonitorRef.value = undefined;
        showOperationInProgressModalRef.value = false;
        emit('update-item');
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemModelSetEditor.messages.operationFailed'),
        icon: 'report_problem',
      });
    });
}

</script>

<style scoped>
.model-set-card {
  width: 200px;
}

.model-set-card-img {
  width: 200px;
  height: 200px;
}

</style>
