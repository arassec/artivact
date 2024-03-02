<template>
  <ArtivactContent>
    <div class="full-width">
      <h1 class="av-text-h1">{{ $t('SearchConfigurationPage.heading') }}</h1>

      <label>{{ $t('SearchConfigurationPage.label') }}</label>

      <br/>

      <q-btn
        :label="$t('SearchConfigurationPage.btnLabel')"
        color="primary"
        class="float-right q-mb-lg"
        @click="recreateSearchIndex()"
      />

    </div>

    <!-- LONG-RUNNING OPERATION -->
    <artivact-operation-in-progress-dialog :progress-monitor-ref="progressMonitorRef"
                                           :dialog-model="showOperationInProgressModalRef"
                                           @close-dialog="showOperationInProgressModalRef = false"/>

  </ArtivactContent>
</template>

<script setup lang="ts">

import ArtivactContent from 'components/ArtivactContent.vue';
import {api} from 'boot/axios';
import {useQuasar} from 'quasar';
import {useI18n} from 'vue-i18n';
import ArtivactOperationInProgressDialog from 'components/ArtivactOperationInProgressDialog.vue';
import {ref} from 'vue';
import {OperationProgress} from 'components/models';

const quasar = useQuasar();
const i18n = useI18n();

const showOperationInProgressModalRef = ref(false);
const progressMonitorRef = ref<OperationProgress>();

function recreateSearchIndex() {
  api
    .post('/api/search/index/recreate')
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
        message: i18n.t('SearchConfigurationPage.messages.indexCreationFailed'),
        icon: 'report_problem',
      });
    });
}

function updateOperationProgress() {
  api
    .get('/api/search/progress')
    .then((response) => {
      if (response.data) {
        progressMonitorRef.value = response.data;
        if (!progressMonitorRef.value?.error) {
          setTimeout(() => updateOperationProgress(), 1000);
        }
      } else {
        progressMonitorRef.value = undefined;
        showOperationInProgressModalRef.value = false;
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: i18n.t('SearchConfigurationPage.messages.indexCreated'),
          icon: 'done',
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('SearchConfigurationPage.messages.indexCreationFailed'),
        icon: 'report_problem',
      });
    });
}

</script>

<style scoped>

</style>
