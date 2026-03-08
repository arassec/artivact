<template>
  <ArtivactContent>
    <div class="full-width">
      <h1 class="av-text-h1">{{ $t('MaintenancePage.heading') }}</h1>

      <q-list bordered class="rounded-borders q-mb-lg">
        <q-expansion-item
          data-test="maintenance-search-index"
          group="maintenance"
          header-class="bg-primary text-white"
          class="list-entry"
          expand-separator
          expand-icon-class="text-white"
        >
          <template v-slot:header>
            <q-item-section class="list-entry-label">
              {{ $t('MaintenancePage.searchIndex.heading') }}
            </q-item-section>
          </template>
          <q-card class="q-mb-lg">
            <q-card-section>
              <div class="q-mb-md">
                {{ $t('MaintenancePage.searchIndex.description') }}
              </div>
              <q-btn
                data-test="maintenance-recreate-search-index"
                :label="$t('MaintenancePage.searchIndex.recreateButton')"
                color="primary"
                @click="recreateSearchIndex()"
              />
            </q-card-section>
          </q-card>
        </q-expansion-item>
      </q-list>
    </div>

    <!-- LONG-RUNNING OPERATION -->
    <artivact-operation-in-progress-dialog
      v-if="showOperationInProgressModalRef"
      :dialog-model="showOperationInProgressModalRef"
      @close-dialog="showOperationInProgressModalRef = false"
      :success-message="'MaintenancePage.messages.recreateIndex.success'"
      :error-message="'MaintenancePage.messages.recreateIndex.failed'"
    />
  </ArtivactContent>
</template>

<script setup lang="ts">
import ArtivactContent from 'components/ArtivactContent.vue';
import ArtivactOperationInProgressDialog from 'components/ArtivactOperationInProgressDialog.vue';
import {api} from 'boot/axios';
import {useQuasar} from 'quasar';
import {ref} from 'vue';
import {useI18n} from 'vue-i18n';

const quasar = useQuasar();
const i18n = useI18n();

const showOperationInProgressModalRef = ref(false);

function recreateSearchIndex() {
  api
    .post('/api/maintenance/search-index/recreate')
    .then(() => {
      showOperationInProgressModalRef.value = true;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('MaintenancePage.messages.recreateIndex.failed'),
        icon: 'report_problem',
      });
    });
}
</script>

<style scoped></style>
