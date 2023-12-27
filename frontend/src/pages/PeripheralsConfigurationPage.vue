<template>
  <ArtivactContent>
    <div class="full-width">
      <h1 class="av-text-h1">Peripherals Configuration</h1>
      <artivact-peripherals-configuration-editor :adapter-configuration="adapterConfigurationRef" v-if="adapterConfigurationRef"/>
      <q-btn
        label="Save Configuration"
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

const quasar = useQuasar();
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
        message: 'Peripherals configuration loading failed',
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
        message: 'Peripherals configuration saved',
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Saving failed',
        icon: 'report_problem',
      });
    });
}

onMounted(() => {
  loadAdapterConfiguration();
});
</script>

<style scoped></style>
