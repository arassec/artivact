<template>
  <ArtivactContent>
    <div class="full-width" data-test="peripherals-configuration">
      <h1 class="av-text-h1">
        {{ $t('PeripheralsConfigurationPage.heading') }}
      </h1>
      <artivact-peripherals-configuration-editor
        :peripheral-configuration="peripheralConfigurationRef"
        v-if="peripheralConfigurationRef"
      />
      <q-btn
        :label="$t('Common.save')"
        color="primary"
        class="q-mb-lg float-right"
        @click="savePeripheralConfiguration()"
      />
    </div>
  </ArtivactContent>
</template>

<script setup lang="ts">
import ArtivactContent from '../components/ArtivactContent.vue';
import { api } from 'boot/axios';
import { useQuasar } from 'quasar';
import { onMounted, ref, Ref } from 'vue';
import ArtivactPeripheralsConfigurationEditor from '../components/ArtivactPeripheralsConfigurationEditor.vue';
import { useI18n } from 'vue-i18n';
import { PeripheralConfiguration } from '../components/artivact-models';

const quasar = useQuasar();
const i18n = useI18n();

const peripheralConfigurationRef: Ref<PeripheralConfiguration | null> =
  ref(null);

function loadPeripheralConfiguration() {
  api
    .get('/api/configuration/peripheral')
    .then((response) => {
      peripheralConfigurationRef.value = response.data;
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

function savePeripheralConfiguration() {
  api
    .post('/api/configuration/peripheral', peripheralConfigurationRef.value)
    .then(() => {
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

onMounted(() => {
  loadPeripheralConfiguration();
});
</script>

<style scoped></style>
