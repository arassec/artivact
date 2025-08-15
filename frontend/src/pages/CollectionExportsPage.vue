<template>
  <ArtivactContent>
    <div class="full-width">
      <h1 class="av-text-h1">{{ $t('CollectionExportsPage.heading') }}</h1>

      <q-tabs v-model="tab">
        <q-tab
          name="configuration"
          icon="build"
          :label="$t('CollectionExportsPage.tabs.configuration')"
        >
        </q-tab>
        <q-tab
          name="import"
          icon="upload"
          :label="$t('CollectionExportsPage.tabs.import')"
        >
        </q-tab>
      </q-tabs>

      <!-- CONFIGURATION -->
      <div v-if="tab == 'configuration'">
        <h2 class="av-text-h2">
          {{ $t('CollectionExportsPage.configuration.heading') }}
        </h2>

        <div class="q-mb-lg">
          {{ $t('CollectionExportsPage.configuration.description') }}
        </div>

        <artivact-collection-export-editor
          :collection-exports="collectionExportsRef"
          @save-collection-export="saveCollectionExport"
          @delete-collection-export="showConfirmDelete"
          @save-sort-order="saveSortOrder"
          @build-collection-export-file="showBuildDialog"
          @cover-picture-uploaded="loadCollectionExports"
          @delete-cover-picture="deleteCoverPicture"
        />
      </div>

      <!-- IMPORT -->
      <div v-if="tab == 'import'">
        <h2 class="av-text-h2">
          {{ $t('CollectionExportsPage.import.heading') }}
        </h2>

        <div class="q-mb-lg">
          {{ $t('CollectionExportsPage.import.description') }}
        </div>

        <div class="row">
          <div class="col">
            <h3 class="av-text-h3">
              {{ $t('CollectionExportsPage.import.completeImport') }}
            </h3>
            <q-uploader
              :label="$t('CollectionExportsPage.import.exportFile')"
              field-name="file"
              :multiple="false"
              accept=".artivact.collection.zip"
              url="/api/import/collection"
              @finish="pollOperationProgress()"
            />
          </div>
          <div class="col">
            <h3 class="av-text-h3">
              {{ $t('CollectionExportsPage.import.forDistributionImport') }}
            </h3>
            <q-uploader
              :label="$t('CollectionExportsPage.import.exportFile')"
              field-name="file"
              :multiple="false"
              accept=".artivact.collection.zip"
              url="/api/import/collection/for-distribution"
              @finish="pollOperationProgress()"
            />
          </div>
        </div>
      </div>

      <!-- LONG-RUNNING OPERATION -->
      <artivact-operation-in-progress-dialog
        v-if="showOperationInProgressModalRef == true"
        :dialog-model="showOperationInProgressModalRef"
        @close-dialog="finishOperation()"
      />

      <!-- DELETE CONFIRMATION DIALOG -->
      <artivact-dialog :dialog-model="confirmDeleteRef" :warn="true">
        <template v-slot:header>
          {{ $t('CollectionExportsPage.dialog.delete.heading') }}
        </template>

        <template v-slot:body>
          <q-card-section>
            {{ $t('CollectionExportsPage.dialog.delete.description') }}
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
            :label="$t('CollectionExportsPage.dialog.delete.approve')"
            color="primary"
            @click="deleteCollectionExport"
          />
        </template>
      </artivact-dialog>

      <!-- BUILD EXPORT FILE CONFIRMATION DIALOG -->
      <artivact-dialog :dialog-model="confirmBuildRef">
        <template v-slot:header>
          {{ $t('CollectionExportsPage.dialog.build.heading') }}
        </template>

        <template v-slot:body>
          <q-card-section>
            {{ $t('CollectionExportsPage.dialog.build.description') }}
          </q-card-section>
        </template>

        <template v-slot:cancel>
          <q-btn
            :label="$t('Common.cancel')"
            color="primary"
            @click="confirmBuildRef = false"
          />
        </template>

        <template v-slot:approve>
          <q-btn
            :label="$t('CollectionExportsPage.dialog.build.approve')"
            color="primary"
            @click="buildCollectionExportFile"
          />
        </template>
      </artivact-dialog>
    </div>
  </ArtivactContent>
</template>

<script setup lang="ts">
import { QUploader, useQuasar } from 'quasar';
import ArtivactContent from '../components/ArtivactContent.vue';
import { useI18n } from 'vue-i18n';
import { onMounted, Ref, ref } from 'vue';
import { CollectionExport } from '../components/artivact-models';
import { api } from '../boot/axios';
import ArtivactCollectionExportEditor from '../components/ArtivactCollectionExportEditor.vue';
import ArtivactOperationInProgressDialog from '../components/ArtivactOperationInProgressDialog.vue';
import ArtivactDialog from '../components/ArtivactDialog.vue';
import { useMenuStore } from '../stores/menu';

const quasar = useQuasar();
const i18n = useI18n();

const menuStore = useMenuStore();

const tab = ref('configuration');

const showOperationInProgressModalRef = ref(false);
const reloadMenusRef = ref(false);

const confirmDeleteRef = ref(false);
const collectionExportToDelete: Ref<CollectionExport | null> = ref(null);

const confirmBuildRef = ref(false);
const collectionExportToBuild: Ref<CollectionExport | null> = ref(null);

const collectionExportsRef = ref([] as CollectionExport[]);

function loadCollectionExports() {
  api
    .get('/api/collection/export')
    .then((response) => {
      refreshCollectionExports(response.data);
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', {
          item: i18n.t('Common.items.collectionExports'),
        }),
        icon: 'report_problem',
      });
    });
}

function saveCollectionExport(collectionExport: CollectionExport) {
  api
    .post('/api/collection/export', collectionExport)
    .then((response) => {
      refreshCollectionExports(response.data);
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.success', {
          item: i18n.t('Common.items.collectionExport'),
        }),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', {
          item: i18n.t('Common.items.collectionExport'),
        }),
        icon: 'report_problem',
      });
    });
}

function showBuildDialog(collectionExport: CollectionExport) {
  collectionExportToBuild.value = collectionExport;
  confirmBuildRef.value = true;
}

function buildCollectionExportFile() {
  if (collectionExportToBuild.value == null) {
    return;
  }
  api
    .post(
      '/api/collection/export/' + collectionExportToBuild.value.id + '/build',
    )
    .then((response) => {
      if (response) {
        confirmBuildRef.value = false;
        reloadMenusRef.value = false;
        showOperationInProgressModalRef.value = true;
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('CollectionExportsPage.messages.buildExportFileFailed'),
        icon: 'report_problem',
      });
    });
}

function showConfirmDelete(collectionExport: CollectionExport) {
  collectionExportToDelete.value = collectionExport;
  confirmDeleteRef.value = true;
}

function deleteCollectionExport() {
  if (collectionExportToDelete.value == null) {
    return;
  }
  api
    .delete('/api/collection/export/' + collectionExportToDelete.value.id)
    .then((response) => {
      refreshCollectionExports(response.data);
      confirmDeleteRef.value = false;
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.deleting.success', {
          item: i18n.t('Common.items.collectionExport'),
        }),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.deleting.failed', {
          item: i18n.t('Common.items.collectionExport'),
        }),
        icon: 'report_problem',
      });
    });
}

function saveSortOrder() {
  api
    .post('/api/collection/export/sort', collectionExportsRef.value)
    .then((response) => {
      refreshCollectionExports(response.data);
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.success', {
          item: i18n.t('Common.items.collectionExports'),
        }),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', {
          item: i18n.t('Common.items.collectionExports'),
        }),
        icon: 'report_problem',
      });
    });
}

function deleteCoverPicture(collectionExport: CollectionExport) {
  api
    .delete('/api/collection/export/' + collectionExport.id + '/cover-picture')
    .then((response) => {
      refreshCollectionExports(response.data);
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.success', {
          item: i18n.t('Common.items.collectionExport'),
        }),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', {
          item: i18n.t('Common.items.collectionExport'),
        }),
        icon: 'report_problem',
      });
    });
}

function refreshCollectionExports(collectionExports: CollectionExport[]) {
  collectionExportsRef.value.length = 0;
  collectionExports.forEach((collectionExport: CollectionExport) => {
    collectionExportsRef.value.push(collectionExport);
  });
}

function pollOperationProgress() {
  showOperationInProgressModalRef.value = true;
  reloadMenusRef.value = true;
}

function finishOperation() {
  showOperationInProgressModalRef.value = false;
  loadCollectionExports();
  if (reloadMenusRef.value == true) {
    api.get('/api/menu').then((response) => {
      menuStore.setAvailableMenus(response.data);
    });
    quasar.notify({
      color: 'positive',
      position: 'bottom',
      message: i18n.t('CollectionExportsPage.messages.importSuccess'),
      icon: 'check',
    });
  }
}

onMounted(() => {
  loadCollectionExports();
});
</script>

<style scoped></style>
