<template>
  <q-btn
    text-color="primary"
    class="q-mr-md"
    round
    dense
    color="accent"
    icon="add_circle"
    @click="createModel"
  />
  <q-btn
    text-color="primary"
    class="q-mr-md"
    round
    dense
    color="accent"
    icon="folder"
    @click="openModelsDir"
  />

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
          @click="showModelSetDetails(modelSet, index)"
        />
        <q-btn
          icon="folder"
          round
          dense
          flat
          size="md"
          color="primary"
          @click="openModelDir(index)"
        />
        <q-btn
          icon="edit"
          round
          dense
          flat
          size="md"
          color="primary"
          @click="editModel(index)"
        />
        <q-space/>
        <q-btn
          icon="delete"
          round
          dense
          flat
          size="md"
          color="primary"
          @click="showDeleteModelSetConfirm(index)"
        ></q-btn>
      </q-card-actions>
    </q-card>
  </div>

  <!-- MODEL-SET DETAILS -->
  <q-dialog v-model="showModelSetDetailsModalRef" persistent v-if="showModelSetDetailsModalRef && modelSetFiles">
    <q-card class="q-mb-lg image-set-modal">
      <q-card-section class="bg-primary text-white">
        <div class="row">
          <div class="text-h6">Model-Set Details</div>
          <q-space/>
          <q-btn
            icon="close"
            flat
            rounded
            dense
            @click="showModelSetDetailsModalRef = false"
          />
        </div>
      </q-card-section>
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
                  @click="transferModel(file, selectedModelSetIndex)"
                />
                <q-space/>
              </div>
            </q-card-actions>
          </q-card>
        </div>
      </q-card-section>
    </q-card>
  </q-dialog>

  <!-- LONG-RUNNING OPERATION -->
  <q-dialog v-model="showOperationInProgressModalRef" persistent>
    <q-card class="q-mb-lg image-set-modal">
      <q-card-section class="bg-primary text-white">
        <div class="text-h6">Operation in Progress</div>
      </q-card-section>
      <q-card-section>
        {{ progressMonitorRef?.progress }}
      </q-card-section>
    </q-card>
  </q-dialog>

  <!-- DELETE CONFIRMATION DIALOG -->
  <q-dialog v-model="confirmDeleteRef" persistent>
    <q-card>
      <q-card-section class="row items-center">
        <q-icon
          name="warning"
          size="md"
          color="warning"
          class="q-mr-md"
        ></q-icon>
        <h3 class="av-text-h3">Delete Model-Set?</h3>
        <div class="q-ml-sm">
          Are you sure you want to delete this Model-Set and all its files?
          This action cannot be undone!
        </div>
      </q-card-section>
      <q-card-section>
        <q-btn flat label="Cancel" color="primary" v-close-popup/>
        <q-btn
          flat
          label="Delete Model-Set"
          color="primary"
          v-close-popup
          @click="deleteModelSet"
          class="float-right"
        />
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">

import {PropType, ref} from 'vue';
import {Asset, ModelSet, OperationProgress} from 'components/models';
import {api} from 'boot/axios';
import {useQuasar} from 'quasar';

const quasar = useQuasar();

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
    .get('/api/media-creation/' + props.itemId + '/model-set-files/' + modelSetIndex)
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
        message: 'Loading failed!',
        icon: 'report_problem',
      });
    });
}

function createModel() {
  api
    .post('/api/media-creation/' + props.itemId + '/create-model-set/TODO_PIPELINE_SELECTION')
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
        message: 'Model creation failed!',
        icon: 'report_problem',
      });
    });
}

function openModelsDir() {
  api
    .put('/api/media-creation/' + props.itemId + '/open-models-dir')
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Opening directory failed!',
        icon: 'report_problem',
      });
    });
}

function editModel(index: number) {
  api
    .post('/api/media-creation/' + props.itemId + '/edit-model/' + index)
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
        message: 'Editing model failed!',
        icon: 'report_problem',
      });
    });
}

function openModelDir(index: number) {
  api
    .put('/api/media-creation/' + props.itemId + '/open-model-dir/' + index)
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Opening model directory failed!',
        icon: 'report_problem',
      });
    });
}

function transferModel(file: Asset, modelSetIndex: number) {
  api
    .put('/api/media-creation/' + props.itemId + '/transfer-model/' + modelSetIndex, file)
    .then((response) => {
      if (response) {
        emit('update-item');
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: 'Model transferred!',
          icon: 'check',
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Transfer failed!',
        icon: 'report_problem',
      });
    });
}

function showDeleteModelSetConfirm(modelSetIndex: number) {
  selectedModelSetIndex = modelSetIndex;
  confirmDeleteRef.value = true;
}

function deleteModelSet() {
  api
    .delete('/api/media-creation/' + props.itemId + '/model-set/' + selectedModelSetIndex)
    .then((response) => {
      if (response) {
        emit('update-item');
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: 'Model set deleted!',
          icon: 'check',
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Deletion failed!',
        icon: 'report_problem',
      });
    });
}

function updateOperationProgress() {
  api
    .get('/api/media-creation/progress')
    .then((response) => {
      if (response.data) {
        progressMonitorRef.value = response.data;
        setTimeout(() => updateOperationProgress(), 1000);
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
        message: 'Background removal failed!',
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

.image-set-modal {
  min-width: 70em;
}

</style>
