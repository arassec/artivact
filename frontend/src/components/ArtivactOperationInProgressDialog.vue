<template>
  <div v-if="progressMonitorRef">
    <artivact-dialog
      :dialog-model="dialogModelRef && progressMonitorRef?.error == null"
      :hide-buttons="true"
    >
      <template v-slot:header>
        {{ $t('ArtivactOperationInProgressDialog.heading') }}
      </template>

      <template v-slot:body>
        <q-card-section>
          <q-spinner size="2em" class="q-mr-md"/>
          {{ $t(progressMonitorRef?.key) }}
          <template v-if="progressMonitorRef.targetAmount">
            {{
              ' (' +
              progressMonitorRef.currentAmount +
              '/' +
              progressMonitorRef.targetAmount +
              ')'
            }}
          </template>
        </q-card-section>
      </template>
    </artivact-dialog>

    <artivact-dialog
      :dialog-model="dialogModelRef && progressMonitorRef?.error != null"
      :hide-buttons="true"
      :show-close-button="true"
      :error="true"
      @close-dialog="$emit('close-dialog')"
    >
      <template v-slot:header>
        {{ $t('ArtivactOperationInProgressDialog.failedHeading') }}
      </template>

      <template v-slot:body>
        <q-card-section>
          {{ $t(progressMonitorRef?.key) }}
          <q-separator class="q-mt-lg q-mb-xs"/>
          <q-expansion-item
            :v-model="showDetailsRef"
            :label="$t('ArtivactOperationInProgressDialog.details')"
          >
            <q-scroll-area style="height: 25em; max-width: 35em">
              <pre>{{ progressMonitorRef?.error }}</pre>
            </q-scroll-area>
          </q-expansion-item>
        </q-card-section>
      </template>
    </artivact-dialog>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref, toRef} from 'vue';
import {useQuasar} from 'quasar';
import ArtivactDialog from '../components/ArtivactDialog.vue';
import {OperationProgress} from './artivact-models';
import {api} from '../boot/axios';
import {useI18n} from 'vue-i18n';

const props = defineProps({
  dialogModel: {
    required: true,
  },
  successMessage: {
    required: false,
    type: String,
    default: 'ArtivactOperationInProgressDialog.successMessage',
  },
  errorMessage: {
    required: false,
    type: String,
    default: 'ArtivactOperationInProgressDialog.errorMessage',
  },
});

const emit = defineEmits(['close-dialog']);

const quasar = useQuasar();
const i18n = useI18n();

const dialogModelRef = toRef(props, 'dialogModel');
const showDetailsRef = ref(false);
const progressMonitorRef = ref(null as OperationProgress);

const successMessageRef = toRef(props, 'successMessage');
const errorMessageRef = toRef(props, 'errorMessage');

function updateOperationProgress() {
  api
    .get('/api/operation/progress')
    .then((response) => {
      if (response.data) {
        progressMonitorRef.value = response.data;
        if (!progressMonitorRef.value?.error) {
          setTimeout(() => updateOperationProgress(), 1000);
        }
      } else {
        emit('close-dialog');
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: i18n.t(successMessageRef.value),
          icon: 'done',
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t(errorMessageRef.value),
        icon: 'report_problem',
      });
    });
}

onMounted(() => {
  updateOperationProgress();
});
</script>

<style scoped></style>
