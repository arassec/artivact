<template>
  <ArtivactContent>
    <div class="full-width" data-test="peripherals-configuration">
      <h1 class="av-text-h1">
        {{ $t('PeripheralsConfigurationPage.heading') }}
      </h1>
      <artivact-peripherals-configuration-editor
        :peripheral-configuration="peripheralConfigurationRef"
        :peripheral-status-overview="peripheralStatusOverviewRef"
      />
      <q-btn
        :label="$t('PeripheralsConfigurationPage.scanPeripherals')"
        color="primary"
        @click="scanPeripherals()"
      />
      <q-btn
        color="primary"
        flat
        dense
        round
        class="q-ml-sm"
        icon="question_mark"
        @click="showScanDescriptionRef = true"
      />
      <q-btn
        :label="$t('Common.save')"
        color="primary"
        class="q-mb-lg float-right"
        @click="savePeripheralConfiguration()"
      />
    </div>

    <artivact-dialog :dialog-model="showScanDescriptionRef"
                     :show-close-button="true"
                     :hide-buttons="true"
                     :min-width="50"
                     @close-dialog="showScanDescriptionRef = false">
      <template #header>
        {{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.heading') }}
      </template>

      <template #body>
        <q-card-section>
          {{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.description') }}
        </q-card-section>
        <q-card-section>
          <b>{{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.turntables.heading') }}</b>
          <ul>
            <li>
              {{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.turntables.arduino') }}
            </li>
          </ul>
        </q-card-section>
        <q-card-section>
          <b>{{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.cameras.heading') }}</b>
          <ul>
            <li>
              {{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.cameras.ptp') }}
            </li>
            <li v-if="Platform.is.linux">
              {{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.cameras.gphoto') }}
            </li>
            <li v-if="Platform.is.win">
              {{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.cameras.digiCamControl') }}
            </li>
          </ul>
        </q-card-section>
        <q-card-section>
          <b>{{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.imageBackgroundRemoval.heading') }}</b>
          <ul>
            <li>
              {{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.imageBackgroundRemoval.default') }}
            </li>
          </ul>
        </q-card-section>
        <q-card-section>
          <b>{{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.modelCreation.heading') }}</b>
          <ul>
            <li>
              {{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.modelCreation.meshroom') }}
            </li>
            <li v-if="Platform.is.linux">
              {{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.modelCreation.metashapeLinux') }}
            </li>
            <li v-if="Platform.is.win">
              {{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.modelCreation.metashapeWindows') }}
            </li>
            <li v-if="Platform.is.win">
              {{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.modelCreation.realityScanWindows') }}
            </li>
          </ul>
        </q-card-section>
        <q-card-section>
          <b>{{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.modelEditing.heading') }}</b>
          <ul>
            <li v-if="Platform.is.linux">
              {{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.modelEditing.blenderLinux') }}
            </li>
            <li v-if="Platform.is.win">
              {{ $t('PeripheralsConfigurationPage.dialogs.scanDescription.modelEditing.blenderWindows') }}
            </li>
          </ul>
        </q-card-section>
      </template>

    </artivact-dialog>

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
        <q-btn :label="$t('Common.ok')" color="primary" @click="leavePage"/>
      </template>
    </artivact-dialog>

    <!-- LONG-RUNNING OPERATION -->
    <artivact-operation-in-progress-dialog
      v-if="showOperationInProgressModalRef == true"
      :dialog-model="showOperationInProgressModalRef"
      @close-dialog="scanFinished()"
      :success-message="'PeripheralsConfigurationPage.messages.scan.success'"
      :error-message="'PeripheralsConfigurationPage.messages.scan.failed'"
    />
  </ArtivactContent>
</template>

<script setup lang="ts">
import ArtivactContent from '../components/ArtivactContent.vue';
import {api} from '../boot/axios';
import {Platform, useQuasar} from 'quasar';
import {onMounted, ref, Ref} from 'vue';
import ArtivactPeripheralsConfigurationEditor from '../components/ArtivactPeripheralsConfigurationEditor.vue';
import {useI18n} from 'vue-i18n';
import {PeripheralsConfiguration} from '../components/artivact-models';
import {usePeripheralsConfigStore} from '../stores/peripherals';
import {onBeforeRouteLeave, useRouter} from 'vue-router';
import ArtivactDialog from '../components/ArtivactDialog.vue';
import ArtivactOperationInProgressDialog from '../components/ArtivactOperationInProgressDialog.vue';
import {useWizzardStore} from '../stores/wizzard';

const quasar = useQuasar();
const i18n = useI18n();
const router = useRouter();

const wizzardStore = useWizzardStore();
const peripheralsConfigStore = usePeripheralsConfigStore();

const peripheralConfigurationRef: Ref<PeripheralsConfiguration | null> =
  ref(null);

const originalConfigRef = ref('');
const toRouteRef = ref(null);
const showUnsavedChangesWarningRef = ref(false);
const checkUnsavedChangesRef = ref(true);

const peripheralStatusOverviewRef = ref(null);

const showOperationInProgressModalRef = ref(false);

const showScanDescriptionRef = ref(false);

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

function scanPeripherals() {
  if (!peripheralConfigurationRef.value) {
    api
        .post('/api/configuration/peripheral/scan')
        .then(() => {
          showOperationInProgressModalRef.value = true;
        })
        .catch(() => {
          quasar.notify({
            color: 'negative',
            position: 'bottom',
            message: i18n.t('PeripheralsConfigurationPage.messages.scan.failed'),
            icon: 'report_problem',
          });
        });
  } else {
    api
        .post('/api/configuration/peripheral', peripheralConfigurationRef.value)
        .then(() => {
          api
              .post('/api/configuration/peripheral/scan')
              .then(() => {
                showOperationInProgressModalRef.value = true;
              })
              .catch(() => {
                quasar.notify({
                  color: 'negative',
                  position: 'bottom',
                  message: i18n.t('PeripheralsConfigurationPage.messages.scan.failed'),
                  icon: 'report_problem',
                });
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
}

function scanFinished() {
  showOperationInProgressModalRef.value = false;
  loadPeripheralConfiguration();
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
  if (wizzardStore.scanPeripherals) {
    wizzardStore.setScanPeripherals(false);
    scanPeripherals();
  } else {
    loadPeripheralConfiguration();
  }
});
</script>

<style scoped></style>
