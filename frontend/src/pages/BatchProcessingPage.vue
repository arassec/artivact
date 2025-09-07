<template>
  <ArtivactContent>
    <div class="full-width q-mb-lg">
      <h1 class="av-text-h1">{{ $t('BatchProcessingPage.heading') }}</h1>

      <h2 class="av-text-h2">
        {{ $t('BatchProcessingPage.parameters.task') }}
      </h2>
      <div class="full-width q-mb-md">
        {{ $t('BatchProcessingPage.parameters.taskDescription') }}
      </div>
      <q-select
        class="param-select"
        data-test="batch-processing-task-selection"
        outlined
        emit-value
        dense
        v-model="batchProcessingParamersRef.task"
        :options="Object.values(BatchProcessingTask)"
        :option-label="(task: BatchProcessingTask) => $t(task)"
      >
      </q-select>

      <template
        v-if="
          batchProcessingParamersRef.task !== BatchProcessingTask.DELETE_ITEM &&
          batchProcessingParamersRef.task !==
            BatchProcessingTask.UPLOAD_MODIFIED_ITEM &&
          batchProcessingParamersRef.task !==
            BatchProcessingTask.UPDATE_SEARCH_INDEX
        "
      >
        <div class="q-mt-lg">
          <h2 class="av-text-h2">
            {{ $t('BatchProcessingPage.parameters.targetId') }}
          </h2>
          <div class="full-width q-mb-md">
            {{ $t('BatchProcessingPage.parameters.targetIdDescription') }}
          </div>
          <q-select
            class="param-select"
            data-test="batch-processing-tag-selection"
            outlined
            dense
            v-model="selectedTagRef"
            :options="tagsConfigurationRef.tags"
            :option-label="(tag: Tag) => tag.translatedValue"
          >
          </q-select>
        </div>
      </template>
    </div>

    <h2 class="av-text-h2">
      {{ $t('BatchProcessingPage.parameters.searchTerm') }}
    </h2>
    <div class="full-width">
      {{ $t('BatchProcessingPage.parameters.searchTermDescription') }}
    </div>
  </ArtivactContent>

  <artivact-item-search-widget
    v-if="
      batchProcessingParamersRef.task !==
      BatchProcessingTask.UPLOAD_MODIFIED_ITEM
    "
    :widget-data="searchWidgetDataRef"
    :move-down-enabled="false"
    :move-up-enabled="false"
    :in-edit-mode="false"
    :expert-mode="true"
  />
  <artivact-content v-else>
    <q-input
      outlined
      v-model="uploadModifiedMaxResultsRef"
      :label="$t('ArtivactItemSearchInput.label.maxResults')"
      type="number"
      class="q-mb-md full-width"
    />
  </artivact-content>

  <artivact-content>
    <q-separator />

    <div class="full-width q-mt-lg">
      <q-btn
        :label="$t('BatchProcessingPage.startButton')"
        color="primary"
        @click="showConfirmBatchProcessingModalRef = true"
        class="float-right"
      />
    </div>

    <!-- BATCH CONFIRMATION DIALOG -->
    <artivact-dialog
      :dialog-model="showConfirmBatchProcessingModalRef"
      :warn="true"
    >
      <template v-slot:header>
        {{ $t('BatchProcessingPage.dialog.process.heading') }}
      </template>

      <template v-slot:body>
        <q-card-section>
          {{ $t('BatchProcessingPage.dialog.process.description') }}
        </q-card-section>
      </template>

      <template v-slot:cancel>
        <q-btn
          :label="$t('Common.cancel')"
          color="primary"
          @click="showConfirmBatchProcessingModalRef = false"
        />
      </template>

      <template v-slot:approve>
        <q-btn
          :label="$t('BatchProcessingPage.dialog.process.approve')"
          color="primary"
          @click="process()"
        />
      </template>
    </artivact-dialog>

    <!-- LONG-RUNNING OPERATION -->
    <artivact-operation-in-progress-dialog
      v-if="showOperationInProgressModalRef == true"
      :dialog-model="showOperationInProgressModalRef"
      @close-dialog="showOperationInProgressModalRef = false"
      :success-message="'BatchProcessingPage.messages.process.success'"
      :error-message="'BatchProcessingPage.messages.process.failed'"
    />
  </artivact-content>
</template>

<script setup lang="ts">
import ArtivactContent from '../components/ArtivactContent.vue';
import ArtivactOperationInProgressDialog from '../components/ArtivactOperationInProgressDialog.vue';
import ArtivactDialog from '../components/ArtivactDialog.vue';
import { onMounted, Ref, ref } from 'vue';
import {
  BaseTranslatableRestrictedObject,
  BatchProcessingParameters,
  BatchProcessingTask,
  Tag,
  TagsConfiguration,
  TranslatableString,
} from '../components/artivact-models';
import { api } from '../boot/axios';
import { useQuasar } from 'quasar';
import { useI18n } from 'vue-i18n';
import ArtivactItemSearchWidget from '../components/widgets/ArtivactItemSearchWidget.vue';
import { ItemSearchWidget } from '../components/widgets/artivact-widget-models';

const quasar = useQuasar();
const i18n = useI18n();

const batchProcessingParamersRef = ref({
  task: BatchProcessingTask.DELETE_ITEM,
  searchTerm: '',
  maxItems: 0,
  targetId: '',
} as BatchProcessingParameters);

const showConfirmBatchProcessingModalRef = ref(false);
const showOperationInProgressModalRef = ref(false);

const uploadModifiedMaxResultsRef = ref(10000);

const tagsConfigurationRef: Ref<TagsConfiguration | null> = ref(null);
const selectedTagRef: Ref<BaseTranslatableRestrictedObject> = ref({
  id: '',
  translatedValue: '',
  value: '',
  restrictions: [],
  translations: {},
});

const searchWidgetDataRef = ref({
  type: 'ITEM_SEARCH',
  id: '',
  restrictions: [] as string[],
  navigationTitle: {
    value: '',
  } as TranslatableString,
  heading: {
    value: '',
  } as TranslatableString,
  content: {
    value: '',
  } as TranslatableString,
  searchTerm: '',
  pageSize: 9,
  maxResults: 10000,
} as ItemSearchWidget);

function loadTagsConfiguration() {
  api
    .get('/api/configuration/public/tag')
    .then((response) => {
      tagsConfigurationRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', {
          item: i18n.t('Common.items.tags'),
        }),
        icon: 'report_problem',
      });
    });
}

function process() {
  batchProcessingParamersRef.value.maxItems =
    searchWidgetDataRef.value.maxResults;
  if (
    batchProcessingParamersRef.value.task ===
    BatchProcessingTask.UPLOAD_MODIFIED_ITEM
  ) {
    batchProcessingParamersRef.value.maxItems =
      uploadModifiedMaxResultsRef.value;
  }
  if (searchWidgetDataRef.value.searchTerm) {
    batchProcessingParamersRef.value.searchTerm =
      searchWidgetDataRef.value.searchTerm;
  }
  if (selectedTagRef.value) {
    batchProcessingParamersRef.value.targetId = selectedTagRef.value.id;
  }
  api
    .post('/api/batch/process', batchProcessingParamersRef.value)
    .then(() => {
      showConfirmBatchProcessingModalRef.value = false;
      showOperationInProgressModalRef.value = true;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('BatchProcessingPage.messages.process.failed'),
        icon: 'report_problem',
      });
    });
}

onMounted(() => {
  loadTagsConfiguration();
});
</script>

<style scoped>
.param-select {
  min-width: 25em;
}
</style>
