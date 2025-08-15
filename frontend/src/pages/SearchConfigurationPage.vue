<template>
  <ArtivactContent>
    <div class="full-width">
      <h1 class="av-text-h1">{{ $t('SearchConfigurationPage.heading') }}</h1>

      {{ $t('SearchConfigurationPage.label') }}

      <div class="q-mb-lg" />

      <q-btn
        :label="$t('SearchConfigurationPage.btnLabel')"
        color="primary"
        class="float-right q-mb-lg"
        @click="recreateSearchIndex()"
      />
    </div>

    <!-- LONG-RUNNING OPERATION -->
    <artivact-operation-in-progress-dialog
      v-if="showOperationInProgressModalRef == true"
      :dialog-model="showOperationInProgressModalRef"
      @close-dialog="showOperationInProgressModalRef = false"
      :success-message="'SearchConfigurationPage.messages.indexCreated'"
      :error-message="'SearchConfigurationPage.messages.indexCreationFailed'"
    />
  </ArtivactContent>
</template>

<script setup lang="ts">
import ArtivactContent from '../components/ArtivactContent.vue';
import { api } from '../boot/axios';
import { useQuasar } from 'quasar';
import { useI18n } from 'vue-i18n';
import ArtivactOperationInProgressDialog from '../components/ArtivactOperationInProgressDialog.vue';
import { ref } from 'vue';

const quasar = useQuasar();
const i18n = useI18n();

const showOperationInProgressModalRef = ref(false);

function recreateSearchIndex() {
  api
    .post('/api/search/index/recreate')
    .then((response) => {
      if (response) {
        showOperationInProgressModalRef.value = true;
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

<style scoped></style>
