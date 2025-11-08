<template>
  <ArtivactContent>
    <div class="full-width" data-test="peripherals-configuration">
      <h1 class="av-text-h1">
        {{ $t('PeripheralsConfigurationPage.heading') }}
      </h1>
      <artivact-peripherals-configuration-editor
        v-if="peripheralConfigurationRef && peripheralStatusOverviewRef"
        :peripheral-configuration="peripheralConfigurationRef"
        :peripheral-status-overview="peripheralStatusOverviewRef"
      />
      <q-btn
        :label="$t('Common.save')"
        color="primary"
        class="q-mb-lg float-right"
        @click="savePeripheralConfiguration()"
      />
    </div>

    <artivact-dialog :dialog-model="showUnsavedChangesWarningRef" :warn="true">
      <template #header>
        {{ $t('Common.dialogs.unsavedChanges.heading') }}
      </template>

      <template #body>
        <q-card-section>
          {{ $t('Common.dialogs.unsavedChanges.body') }}
        </q-card-section>
      </template>

      <template #cancel>
        <q-btn
          :label="$t('Common.cancel')"
          color="primary"
          @click="showUnsavedChangesWarningRef = false"
        />
      </template>

      <template #approve>
        <q-btn :label="$t('Common.ok')" color="primary" @click="leavePage" />
      </template>
    </artivact-dialog>
  </ArtivactContent>
</template>

<script setup lang="ts">
import ArtivactContent from '../components/ArtivactContent.vue';
import { api } from '../boot/axios';
import { useQuasar } from 'quasar';
import { onMounted, ref, Ref } from 'vue';
import ArtivactPeripheralsConfigurationEditor from '../components/ArtivactPeripheralsConfigurationEditor.vue';
import { useI18n } from 'vue-i18n';
import { PeripheralsConfiguration } from '../components/artivact-models';
import { usePeripheralsConfigStore } from '../stores/peripherals';
import { onBeforeRouteLeave, useRouter } from 'vue-router';
import ArtivactDialog from '../components/ArtivactDialog.vue';

const quasar = useQuasar();
const i18n = useI18n();
const router = useRouter();

const peripheralsConfigStore = usePeripheralsConfigStore();

const peripheralConfigurationRef: Ref<PeripheralsConfiguration | null> =
  ref(null);

const originalConfigRef = ref('');
const toRouteRef = ref(null);
const showUnsavedChangesWarningRef = ref(false);
const checkUnsavedChangesRef = ref(true);

const peripheralStatusOverviewRef = ref(null);

function loadPeripheralConfiguration() {
  api
    .get('/api/configuration/peripheral')
    .then((response) => {
      peripheralConfigurationRef.value = response.data;
      originalConfigRef.value = JSON.stringify(response.data);
      peripheralsConfigStore.setPeripheralsConfig(
        peripheralConfigurationRef.value,
      );
      testPeripheralConfiguration();
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', {
          item: i18n.t('Common.items.configuration.peripherals'),
        }),
        icon: 'report_problem',
      });
    });
}

function testPeripheralConfiguration() {
  api
    .post(
      '/api/configuration/peripheral/test-all',
      peripheralConfigurationRef.value,
    )
    .then((response) => {
      peripheralStatusOverviewRef.value = response.data;
    });
}

function savePeripheralConfiguration() {
  api
    .post('/api/configuration/peripheral', peripheralConfigurationRef.value)
    .then((response) => {
      peripheralConfigurationRef.value = response.data;
      originalConfigRef.value = JSON.stringify(response.data);
      peripheralsConfigStore.setPeripheralsConfig(
        peripheralConfigurationRef.value,
      );
      testPeripheralConfiguration();
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.success', {
          item: i18n.t('Common.items.configuration.peripherals'),
        }),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', {
          item: i18n.t('Common.items.configuration.peripherals'),
        }),
        icon: 'report_problem',
      });
    });
}

function leavePage() {
  checkUnsavedChangesRef.value = false;
  router.push('/');
}

onBeforeRouteLeave((to, from, next) => {
  let currentConfig = JSON.stringify(peripheralConfigurationRef.value);
  if (
    checkUnsavedChangesRef.value &&
    currentConfig !== originalConfigRef.value
  ) {
    showUnsavedChangesWarningRef.value = true;
    toRouteRef.value = to;
  } else {
    next();
  }
});

onMounted(() => {
  loadPeripheralConfiguration();
});
</script>

<style scoped></style>
