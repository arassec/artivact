<template>
  <artivact-content>
    <div class="full-width">
      <h1 class="av-text-h1">{{ $t('ExchangeConfigurationPage.heading') }}</h1>

      <q-tabs v-model="tab">
        <q-tab name="configuration" icon="build" :label="$t('ExchangeConfigurationPage.tabs.configuration')">
        </q-tab>
        <q-tab name="export" icon="backup" :label="$t('ExchangeConfigurationPage.tabs.export')">
        </q-tab>
        <q-tab name="import" icon="download" :label="$t('ExchangeConfigurationPage.tabs.import')">
        </q-tab>
      </q-tabs>

      <div v-if="tab == 'configuration'">
        <h2 class="av-text-h2">{{ $t('ExchangeConfigurationPage.configuration.heading') }}</h2>
        <div>
          <div class="q-mb-lg">
            {{ $t('ExchangeConfigurationPage.configuration.description') }}
          </div>
          <artivact-exchange-configuration-editor :exchange-configuration="exchangeConfigurationRef"
                                                  v-if="exchangeConfigurationRef" />
          <div class="full-width">
            <q-btn
              :label="$t('Common.save')"
              color="primary"
              class="q-mb-lg float-right"
              @click="saveExchangeConfiguration()"
            />
          </div>
        </div>
      </div>

      <div v-if="tab == 'export'">
        <div class="full-width">

          <h2 class="av-text-h2">{{ $t('ExchangeConfigurationPage.syncAllUp.heading') }}</h2>

          <div class="full-width">
            <div class="q-mb-lg">
              {{ $t('ExchangeConfigurationPage.syncAllUp.description') }}
            </div>
          </div>
          <div class="full-width sync-all-up-button">
            <q-btn
              :label="$t('ExchangeConfigurationPage.syncAllUp.button')"
              color="primary"
              class="q-mb-lg float-right"
              @click="synchronizeAllUp()"
            />
          </div>

          <q-separator class="full-width q-mb-md" />

          <h2 class="av-text-h2">{{ $t('ExchangeConfigurationPage.contentExport.heading') }}</h2>
          <div class="q-mb-lg">
            {{ $t('ExchangeConfigurationPage.contentExport.description') }}
          </div>
          <artivact-content-export-configuration-editor :content-exports="contentExportsRef"
                                                        v-if="contentExportsRef"
                                                        v-on:delete-content-export="confirmDeleteContentExport" />
        </div>
      </div>

      <div v-if="tab == 'import'" class="full-width">
        <h2 class="av-text-h2">{{ $t('ExchangeConfigurationPage.contentImport.heading') }}</h2>
        <div class="q-mb-lg">
          {{ $t('ExchangeConfigurationPage.contentImport.description') }}
        </div>
        <q-uploader
          :url="'/api/import/content'"
          :label="$t('ExchangeConfigurationPage.contentImport.button')"
          class="q-mt-md q-mb-md"
          accept="artivact.content.zip"
          field-name="file"
          :no-thumbnails="true"
          @uploaded="contentUploaded"
          @failed="contentUploadFailed"
        />
      </div>
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
        <q-btn :label="$t('Common.cancel')" color="primary" @click="showDeleteContentExportModalRef = false" />
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
                                           @close-dialog="showOperationInProgressModalRef = false" />


  </artivact-content>
</template>

<script setup lang="ts">
import ArtivactContent from 'components/ArtivactContent.vue';
import ArtivactExchangeConfigurationEditor from 'components/ArtivactExchangeConfigurationEditor.vue';
import { useQuasar } from 'quasar';
import { onMounted, ref, Ref } from 'vue';
import { ContentExport, ExchangeConfiguration, OperationProgress } from 'components/artivact-models';
import { api } from 'boot/axios';
import { useI18n } from 'vue-i18n';
import ArtivactContentExportConfigurationEditor from 'components/ArtivactContentExportConfigurationEditor.vue';
import ArtivactDialog from 'components/ArtivactDialog.vue';
import ArtivactOperationInProgressDialog from 'components/ArtivactOperationInProgressDialog.vue';
import { useMenuStore } from 'stores/menu';

const quasar = useQuasar();
const i18n = useI18n();

const menuStore = useMenuStore();

const exchangeConfigurationRef: Ref<ExchangeConfiguration | null> = ref(null);
const contentExportsRef: Ref<Array<ContentExport> | null> = ref(null);

const selectedContentExportRef: Ref<ContentExport | null> = ref(null);

const showDeleteContentExportModalRef = ref(false);

const progressMonitorRef = ref<OperationProgress>();
const showOperationInProgressModalRef = ref(false);

const tab = ref('configuration');

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
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.configuration.exchange') }),
        icon: 'report_problem'
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
        icon: 'report_problem'
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
          icon: 'check'
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ExchangeConfigurationPage.messages.sync.failed'),
        icon: 'report_problem'
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
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.configuration.exports') }),
        icon: 'report_problem'
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
        message: i18n.t('Common.messages.saving.success', { item: i18n.t('Common.items.configuration.exchange') }),
        icon: 'check'
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', { item: i18n.t('Common.items.configuration.exchange') }),
        icon: 'report_problem'
      });
    });
}

function confirmDeleteContentExport(standardExportInfo: ContentExport) {
  selectedContentExportRef.value = standardExportInfo;
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
        message: i18n.t('Common.messages.deleting.success', { item: i18n.t('Common.items.export') }),
        icon: 'check'
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.deleting.failed', { item: i18n.t('Common.items.export') }),
        icon: 'report_problem'
      });
    });
}

function contentUploaded() {
  api
    .get('/api/import/progress')
    .then((response) => {
      if (response.data) {
        showOperationInProgressModalRef.value = true;
        progressMonitorRef.value = response.data;
        updateImportOperationProgress();
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ExchangeConfigurationPage.messages.import.failed'),
        icon: 'report_problem'
      });
    });
}

function updateImportOperationProgress() {
  api
    .get('/api/import/progress')
    .then((response) => {
      if (response.data) {
        progressMonitorRef.value = response.data;
        setTimeout(() => updateImportOperationProgress(), 1000);
      } else {
        progressMonitorRef.value = undefined;
        showOperationInProgressModalRef.value = false;
        api
          .get('/api/menu')
          .then((response) => {
            menuStore.setAvailableMenus(response.data);
          });
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: i18n.t('ExchangeConfigurationPage.messages.import.success'),
          icon: 'check'
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ExchangeConfigurationPage.messages.import.failed'),
        icon: 'report_problem'
      });
    });
}

function contentUploadFailed() {
  quasar.notify({
    color: 'negative',
    position: 'bottom',
    message: i18n.t('ExchangeConfigurationPage.messages.import.failed'),
    icon: 'report_problem'
  });
}

onMounted(() => {
  loadExchangeConfiguration();
  loadContentExports();
});

</script>

<style scoped>
.sync-all-up-button {
  min-height: 4em;
}
</style>
