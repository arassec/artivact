<template>
  <artivact-content>
    <div class="full-width">
      <h1 class="av-text-h1">{{$t('ExchangeConfigurationPage.heading')}}</h1>

      <h2 class="av-text-h2">{{ $t('ExchangeConfigurationPage.exchange.heading') }}</h2>
      <div class="q-mb-lg">
        {{ $t('ExchangeConfigurationPage.exchange.description') }}
      </div>
      <artivact-exchange-configuration-editor :exchange-configuration="exchangeConfigurationRef"
                                              v-if="exchangeConfigurationRef"/>
      <q-btn
        :label="$t('Common.save')"
        color="primary"
        class="q-mb-lg float-right"
        @click="saveExchangeConfiguration()"
      />
    </div>

    <div class="full-width">
      <q-btn
        :label="$t('ExchangeConfigurationPage.syncAllUp.button')"
        color="primary"
        class="q-mb-lg float-right"
        @click="synchronizeAllUp()"
      />
    </div>

    <div class="full-width">
      <h2 class="av-text-h2">{{ $t('ExchangeConfigurationPage.contentExport.heading') }}</h2>
      <div class="q-mb-lg">
        {{ $t('ExchangeConfigurationPage.contentExport.description') }}
      </div>
      <artivact-content-export-configuration-editor :content-exports="contentExportsRef"
                                                    v-if="contentExportsRef"
                                                    v-on:delete-content-export="confirmDeleteContentExport"/>
    </div>

    <!-- DELETE CONFIRMATION DIALOG -->
    <artivact-dialog :dialog-model="showDeleteContentExportModalRef" :warn="true">
      <template v-slot:header>
        {{ $t('ExchangeConfigurationPage.dialog.delete.heading') }}
      </template>

      <template v-slot:body>
        <q-card-section>
          {{ $t('ExchangeConfigurationPage.dialog.delete.description') }}
        </q-card-section>
      </template>

      <template v-slot:cancel>
        <q-btn :label="$t('Common.cancel')" color="primary" @click="showDeleteContentExportModalRef = false"/>
      </template>

      <template v-slot:approve>
        <q-btn
          :label="$t('ExchangeConfigurationPage.dialog.delete.approve')"
          color="primary"
          @click="deleteContentExport"
        />
      </template>
    </artivact-dialog>

    <!-- LONG-RUNNING OPERATION -->
    <artivact-operation-in-progress-dialog :progress-monitor-ref="progressMonitorRef"
                                           :dialog-model="showOperationInProgressModalRef"
                                           @close-dialog="showOperationInProgressModalRef = false"/>

  </artivact-content>
</template>

<script setup lang="ts">
import ArtivactContent from 'components/ArtivactContent.vue';
import ArtivactExchangeConfigurationEditor from 'components/ArtivactExchangeConfigurationEditor.vue';
import {useQuasar} from 'quasar';
import {onMounted, ref, Ref} from 'vue';
import {ContentExport, ExchangeConfiguration, OperationProgress} from 'components/artivact-models';
import {api} from 'boot/axios';
import {useI18n} from 'vue-i18n';
import ArtivactContentExportConfigurationEditor from 'components/ArtivactContentExportConfigurationEditor.vue';
import ArtivactDialog from 'components/ArtivactDialog.vue';
import ArtivactOperationInProgressDialog from 'components/ArtivactOperationInProgressDialog.vue';

const quasar = useQuasar();
const i18n = useI18n();

const exchangeConfigurationRef: Ref<ExchangeConfiguration | null> = ref(null);
const contentExportsRef: Ref<Array<ContentExport> | null> = ref(null);

const selectedContentExportRef: Ref<ContentExport | null> = ref(null);

const showDeleteContentExportModalRef = ref(false);

const progressMonitorRef = ref<OperationProgress>();
const showOperationInProgressModalRef = ref(false);

function loadExchangeConfiguration() {
  api
    .get('/api/configuration/exchange')
    .then((response) => {
      exchangeConfigurationRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.configuration.exchange')}),
        icon: 'report_problem',
      });
    });
}

function synchronizeAllUp() {
  api
    .post('/api/export/remote/item')
    .then((response) => {
      showOperationInProgressModalRef.value = true;
      progressMonitorRef.value = response.data;
      updateOperationProgress();
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ExchangeConfigurationPage.messages.sync.failed'),
        icon: 'report_problem',
      });
    });
}

function updateOperationProgress() {
  api
    .get('/api/export/progress')
    .then((response) => {
      if (response.data) {
        progressMonitorRef.value = response.data;
        setTimeout(() => updateOperationProgress(), 1000);
      } else {
        progressMonitorRef.value = undefined;
        showOperationInProgressModalRef.value = false;
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: i18n.t('ExchangeConfigurationPage.messages.sync.success'),
          icon: 'check',
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ExchangeConfigurationPage.messages.sync.failed'),
        icon: 'report_problem',
      });
    });
}

function loadContentExports() {
  api
    .get('/api/export/content')
    .then((response) => {
      contentExportsRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', {item: i18n.t('Common.items.configuration.exports')}),
        icon: 'report_problem',
      });
    });
}

function saveExchangeConfiguration() {
  api
    .post('/api/configuration/exchange', exchangeConfigurationRef.value)
    .then(() => {
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.success', { item: i18n.t('Common.items.configuration.exchange')}),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', { item: i18n.t('Common.items.configuration.exchange')}),
        icon: 'report_problem',
      });
    });
}

function confirmDeleteContentExport(contentExport: ContentExport) {
  selectedContentExportRef.value = contentExport;
  showDeleteContentExportModalRef.value = true;
}

function deleteContentExport() {
  if (!selectedContentExportRef.value?.id) {
    return;
  }
  api
    .delete('/api/export/content/' + selectedContentExportRef.value?.id)
    .then(() => {
      showDeleteContentExportModalRef.value = false;
      loadContentExports();
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.deleting.success', {item: i18n.t('Common.items.export')}),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.deleting.failed', {item: i18n.t('Common.items.export')}),
        icon: 'report_problem',
      });
    });
}

onMounted(() => {
  loadExchangeConfiguration();
  loadContentExports();
});

</script>

<style scoped>

</style>
