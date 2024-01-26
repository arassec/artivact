<template>
  <artivact-content>
    <div class="full-width">
      <h1 class="av-text-h1">Exchange Configuration</h1>
      <div class="q-mb-lg">
        On this page you can configure exchange parameters.
      </div>
      <artivact-exchange-configuration-editor :exchange-configuration="exchangeConfigurationRef"
                                              v-if="exchangeConfigurationRef"/>
      <q-btn
        label="Save Configuration"
        color="primary"
        class="q-mb-lg float-right"
        @click="saveExchangeConfiguration()"
      />
    </div>
  </artivact-content>
</template>

<script setup lang="ts">
import ArtivactContent from 'components/ArtivactContent.vue';
import ArtivactExchangeConfigurationEditor from 'components/ArtivactExchangeConfigurationEditor.vue';
import {useQuasar} from 'quasar';
import {onMounted, ref, Ref} from 'vue';
import {ExchangeConfiguration} from 'components/models';
import {api} from 'boot/axios';

const quasar = useQuasar();
const exchangeConfigurationRef: Ref<ExchangeConfiguration | null> =
  ref(null);

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
        message: 'Exchange configuration loading failed',
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
        message: 'Exchange configuration saved',
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
  loadExchangeConfiguration();
});

</script>

<style scoped>

</style>
