<template>
  <div v-if="exchangeConfigurationRef">

    <div class="q-mb-md">
      {{ $t('ArtivactExchangeConfigurationEditor.synchronization.server.description') }}
    </div>
    <q-input
      outlined
      :label="$t('ArtivactExchangeConfigurationEditor.synchronization.server.label')"
      v-model="exchangeConfigurationRef.remoteServer"
    />

    <div class="q-mb-md q-mt-lg">
      {{ $t('ArtivactExchangeConfigurationEditor.synchronization.token.description') }}
    </div>
    <q-input class="q-mb-lg"
      outlined
      :label="$t('ArtivactExchangeConfigurationEditor.synchronization.token.label')"
      v-model="exchangeConfigurationRef.apiToken"
    />

  </div>
</template>

<script setup lang="ts">

import { ExchangeConfiguration } from 'components/artivact-models';
import { PropType, toRef } from 'vue';

const props = defineProps({
  exchangeConfiguration: {
    required: true,
    type: Object as PropType<ExchangeConfiguration | null>
  }
});

const exchangeConfigurationRef = toRef(props, 'exchangeConfiguration');

</script>

<style scoped>

</style>
