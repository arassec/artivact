<template>
  <ArtivactContent>
    <div class="full-width">
      <h1 class="av-text-h1">{{ $t('PeripheralsConfigurationPage.heading') }}</h1>
      <artivact-peripherals-configuration-editor :adapter-configuration="adapterConfigurationRef" v-if="adapterConfigurationRef"/>
      <q-btn
        :label="$t('Common.save')"
        color="primary"
        class="q-mb-lg float-right"
        @click="saveAdapterConfiguration()"
      />
    </div>
  </ArtivactContent>
</template>

<script setup lang="ts">
import ArtivactContent from 'components/ArtivactContent.vue';
import {api} from 'boot/axios';
import {useQuasar} from 'quasar';
import {onMounted, ref, Ref} from 'vue';
import {AdapterConfiguration} from 'components/models';
import {useLocaleStore} from 'stores/locale';
import ArtivactPeripheralsConfigurationEditor from 'components/ArtivactPeripheralsConfigurationEditor.vue';
import {useI18n} from 'vue-i18n';

const quasar = useQuasar();
const i18n = useI18n();

const adapterConfigurationRef: Ref<AdapterConfiguration | null> =
  ref(null);

const localeStore = useLocaleStore();

function loadAdapterConfiguration() {
  api
    .get('/api/configuration/adapter')
    .then((response) => {
      adapterConfigurationRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.configuration.peripherals') }),
        icon: 'report_problem',
      });
    });
}

function saveAdapterConfiguration() {
  api
    .post('/api/configuration/adapter', adapterConfigurationRef.value)
    .then(() => {
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.success', { item: i18n.t('Common.items.configuration.peripherals') }),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', { item: i18n.t('Common.items.configuration.peripherals') }),
        icon: 'report_problem',
      });
    });
}

onMounted(() => {
  loadAdapterConfiguration();
});
</script>

<style scoped></style>
