<template>
  <h2 class="av-text-h2">
    {{ $t('Common.items.models') }}
    <q-btn
      :disable="!peripheralsConfigStore.isModelCreatorSet"
      data-test="item-creation-create-model-button"
      text-color="primary"
      round
      dense
      flat
      color="accent"
      icon="add_circle"
      @click="createModel()"
    >
      <q-tooltip>{{ $t('ItemModelSetEditor.tooltip.create') }}</q-tooltip>
    </q-btn>
    <q-btn
      text-color="primary"
      round
      dense
      flat
      color="accent"
      icon="more_vert"
    >
      <q-menu
        anchor="top right"
        self="top left"
        auto-close
        transition-show="jump-right"
        transition-hide="jump-left"
      >
        <div class="row no-wrap q-pa-sm">
          <q-btn
            text-color="primary"
            round
            dense
            color="accent"
            icon="playlist_add"
            @click="showCreateModel()"
            class="q-mr-md"
            :disable="peripheralsConfigStore.peripheralsConfig.modelCreatorPeripheralConfigs.length < 2"
          >
            <q-tooltip>{{ $t('ItemModelSetEditor.tooltip.createSelection') }}</q-tooltip>
          </q-btn>
          <q-btn
            text-color="primary"
            round
            dense
            color="accent"
            icon="folder"
            @click="openModelsDir"
          >
            <q-tooltip>{{ $t('ItemModelSetEditor.tooltip.open') }}</q-tooltip>
          </q-btn>
        </div>
      </q-menu>
    </q-btn>
  </h2>

  <div class="row">
    <q-card
      v-for="(modelSet, index) in creationModelSets"
      :key="index"
      class="model-set-card q-mr-md q-mt-md"
    >
      <q-img
        :src="'logos/' + modelSet.modelSetImage"
        class="model-set-card-img"
        fit="none"
      >
        <div class="absolute-bottom">
          <div class="text-h6">{{ modelSet.comment }}</div>
        </div>
      </q-img>
      <q-card-actions>
        <div>
          <q-btn
            :disable="!peripheralsConfigStore.isModelEditorSet"
            icon="edit"
            round
            dense
            flat
            size="md"
            color="primary"
            @click="editModelDirectly(index)"
          >
            <q-tooltip>{{ $t('ItemModelSetEditor.tooltip.edit') }}</q-tooltip>
          </q-btn>
        </div>
        <q-space/>
        <q-btn
          text-color="primary"
          round
          dense
          flat
          color="accent"
          icon="more_vert"
        >
          <q-menu
            anchor="top right"
            self="top left"
            auto-close
            transition-show="jump-right"
            transition-hide="jump-left"
          >
            <div class="row no-wrap q-pa-sm">
              <q-btn
                icon="edit_note"
                round
                dense
                flat
                size="md"
                color="primary"
                @click="showEditModel(index)"
              >
                <q-tooltip>{{ $t('ItemModelSetEditor.tooltip.editSelection') }}</q-tooltip>
              </q-btn>
              <q-btn
                icon="folder"
                round
                dense
                flat
                size="md"
                color="primary"
                @click="openModelDir(index)"
              >
                <q-tooltip>{{
                    $t('ItemModelSetEditor.tooltip.openModel')
                  }}
                </q-tooltip>
              </q-btn>
              <q-btn
                icon="search"
                round
                dense
                flat
                size="md"
                color="primary"
                @click="showModelSetDetails(index)"
              >
                <q-tooltip>{{ $t('ItemModelSetEditor.tooltip.details') }}</q-tooltip>
              </q-btn>
              <q-btn
                icon="delete"
                round
                dense
                flat
                size="md"
                color="primary"
                @click="showDeleteModelSetConfirm(index)"
              >
                <q-tooltip>{{ $t('ItemModelSetEditor.tooltip.delete') }}</q-tooltip>
              </q-btn>
            </div>
          </q-menu>
        </q-btn>
      </q-card-actions>
    </q-card>
  </div>

  <!-- CREATE MODEL PARAMETERS DIALOG -->
  <artivact-dialog :dialog-model="showCreateModelParamsModalRef">
    <template v-slot:header>
      {{ $t('ItemImageSetEditor.dialog.createModel.heading') }}
    </template>

    <template v-slot:body>
      <q-card-section>
        <q-select
          :disable="availableModelCreatorRef.length == 1"
          class="q-mb-md"
          outlined
          v-model="selectedModelCreatorRef"
          :options="availableModelCreatorRef"
          :option-label="(opt) => (opt.label ? opt.label : opt)"
          :label="$t('ItemImageSetEditor.label.selectModelCreator')"
        />
      </q-card-section>
    </template>

    <template v-slot:cancel>
      <q-btn
        color="primary"
        :label="$t('Common.cancel')"
        @click="showCreateModelParamsModalRef = false"
      />
    </template>

    <template v-slot:approve>
      <q-btn
        color="primary"
        :label="$t('ItemImageSetEditor.dialog.createModel.approve')"
        @click="createModel()"
      />
    </template>
  </artivact-dialog>

  <!-- EDIT MODEL PARAMETERS DIALOG -->
  <artivact-dialog :dialog-model="showEditModelParamsModalRef">
    <template v-slot:header>
      {{ $t('ItemImageSetEditor.dialog.editModel.heading') }}
    </template>

    <template v-slot:body>
      <q-card-section>
        <q-select
          :disable="availableModelEditorRef.length == 1"
          class="q-mb-md"
          outlined
          v-model="selectedModelEditorRef"
          :options="availableModelEditorRef"
          :option-label="(opt) => (opt.label ? opt.label : opt)"
          :label="$t('ItemImageSetEditor.label.selectModelEditor')"
        />
      </q-card-section>
    </template>

    <template v-slot:cancel>
      <q-btn
        color="primary"
        :label="$t('Common.cancel')"
        @click="showEditModelParamsModalRef = false"
      />
    </template>

    <template v-slot:approve>
      <q-btn
        color="primary"
        :label="$t('ItemImageSetEditor.dialog.editModel.approve')"
        @click="editModel()"
      />
    </template>
  </artivact-dialog>

  <!-- MODEL-SET DETAILS -->
  <artivact-dialog
    :dialog-model="showModelSetDetailsModalRef"
    v-if="showModelSetDetailsModalRef && modelSetFiles"
    :hide-buttons="true"
    :show-close-button="true"
    :min-width="50"
    @close-dialog="showModelSetDetailsModalRef = false"
  >
    <template v-slot:header>
      {{ $t('ItemModelSetEditor.dialog.details.heading') }}
    </template>

    <template v-slot:body>
      <q-card-section>
        <div class="row">
          <q-card
            v-for="(file, index) in modelSetFiles"
            :key="index"
            class="image-set-card q-mr-md q-mb-md"
          >
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
                  @click="transferModel(file, selectedModelSetIndexRef)"
                >
                  <q-tooltip>{{
                      $t('ItemModelSetEditor.dialog.details.transfer')
                    }}
                  </q-tooltip>
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
  <artivact-operation-in-progress-dialog
    v-if="showOperationInProgressModalRef == true"
    :dialog-model="showOperationInProgressModalRef"
    @close-dialog="operationFinished()"
  />

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
      <q-btn
        :label="$t('Common.cancel')"
        color="primary"
        @click="confirmDeleteRef = false"
      />
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
import {onMounted, PropType, Ref, ref} from 'vue';
import {Asset, CreateModelParams, ModelSet, SelectboxModel,} from './artivact-models';
import {api} from '../boot/axios';
import {useQuasar} from 'quasar';
import ArtivactDialog from '../components/ArtivactDialog.vue';
import ArtivactOperationInProgressDialog from '../components/ArtivactOperationInProgressDialog.vue';
import {useI18n} from 'vue-i18n';
import {usePeripheralsConfigStore} from '../stores/peripherals';

const props = defineProps({
  itemId: {
    required: true,
    type: String,
  },
  creationModelSets: {
    required: true,
    type: Array as PropType<Array<ModelSet>>,
  },
});

const emit = defineEmits<{
  (e: 'delete-model-set-file', modelSetIndex: number, file: Asset): void;
  (e: 'update-item'): void;
}>();

const quasar = useQuasar();
const i18n = useI18n();

const peripheralsConfigStore = usePeripheralsConfigStore();

const availableModelCreatorRef: Ref<SelectboxModel[]> = ref(
  [] as SelectboxModel[],
);
const selectedModelCreatorRef = ref(null);
const availableModelEditorRef: Ref<SelectboxModel[]> = ref(
  [] as SelectboxModel[],
);
const selectedModelEditorRef = ref(null);

const showOperationInProgressModalRef = ref(false);
const showCreateModelParamsModalRef = ref(false);

const selectedModelSetIndexRef = ref(null);

const showEditModelParamsModalRef = ref(false);
const showModelSetDetailsModalRef = ref(false);
let modelSetFiles: Asset[];

const confirmDeleteRef = ref(false);

const createModelParams = ref({
  modelCreatorPeripheralConfigId: null,
} as CreateModelParams);

function showModelSetDetails(modelSetIndex: number) {
  selectedModelSetIndexRef.value = modelSetIndex;
  api
    .get(
      '/api/item/' +
      props.itemId +
      '/media-creation/model-set-files/' +
      modelSetIndex,
    )
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

function showCreateModel() {
  if (availableModelCreatorRef.value.length > 1) {
    showCreateModelParamsModalRef.value = true;
  } else {
    createModel();
  }
}

function createModel() {
  showCreateModelParamsModalRef.value = false;
  createModelParams.value.modelCreatorPeripheralConfigId =
    selectedModelCreatorRef.value.value;
  api
    .post(
      '/api/item/' + props.itemId + '/media-creation/create-model-set',
      createModelParams.value,
    )
    .then((response) => {
      if (response) {
        showOperationInProgressModalRef.value = true;
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

function showEditModel(index: number) {
  selectedModelSetIndexRef.value = index;
  if (availableModelEditorRef.value.length > 1) {
    showEditModelParamsModalRef.value = true;
  } else {
    editModel();
  }
}


function editModelDirectly(index: number) {
  selectedModelSetIndexRef.value = index;
  editModel();
}

function editModel() {
  showEditModelParamsModalRef.value = false;
  api
    .post(
      '/api/item/' +
      props.itemId +
      '/media-creation/edit-model/' +
      selectedModelSetIndexRef.value +
      '?modelEditorPeripheralConfigId=' +
      selectedModelEditorRef.value.value,
    )
    .then((response) => {
      if (response) {
        showOperationInProgressModalRef.value = true;
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
    .put(
      '/api/item/' + props.itemId + '/media-creation/open-model-dir/' + index,
    )
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
    .put(
      '/api/item/' +
      props.itemId +
      '/media-creation/transfer-model/' +
      modelSetIndex,
      file,
    )
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
  selectedModelSetIndexRef.value = modelSetIndex;
  confirmDeleteRef.value = true;
}

function deleteModelSet() {
  confirmDeleteRef.value = false;
  api
    .delete(
      '/api/item/' +
      props.itemId +
      '/media-creation/model-set/' +
      selectedModelSetIndexRef.value,
    )
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

function operationFinished() {
  emit('update-item');
  showOperationInProgressModalRef.value = false;
}

function createPeripheralsOptions() {
  availableModelCreatorRef.value = [] as SelectboxModel[];
  if (peripheralsConfigStore.isModelCreatorSet) {
    peripheralsConfigStore.config.modelCreatorPeripheralConfigs.forEach(
      (config) => {
        availableModelCreatorRef.value.push({
          label: config.label,
          value: config.id,
          disable: false,
        });
      },
    );
    selectedModelCreatorRef.value =
      availableModelCreatorRef.value.find(
        (opt) => opt.value === peripheralsConfigStore.favouriteModelCreator,
      ) ?? availableModelCreatorRef.value[0];
  }

  availableModelEditorRef.value = [] as SelectboxModel[];
  if (peripheralsConfigStore.isModelEditorSet) {
    peripheralsConfigStore.config.modelEditorPeripheralConfigs.forEach(
      (config) => {
        availableModelEditorRef.value.push({
          label: config.label,
          value: config.id,
          disable: false,
        });
      },
    );
    selectedModelEditorRef.value =
      availableModelEditorRef.value.find(
        (opt) => opt.value === peripheralsConfigStore.favouriteModelEditor,
      ) ?? availableModelEditorRef.value[0];
  }
}

onMounted(() => {
  createPeripheralsOptions();
});
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
