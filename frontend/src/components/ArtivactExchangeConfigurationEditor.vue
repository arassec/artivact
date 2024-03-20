<template>
  <div v-if="exchangeConfigurationRef">
    <q-list bordered class="rounded-borders q-mb-lg">
      <q-expansion-item
        group="exchange"
        header-class="bg-primary text-white"
        class="list-entry"
        expand-separator
        expand-icon-class="text-white"
      >
        <template v-slot:header>
          <q-item-section class="list-entry-label">
            {{ $t('ArtivactExchangeConfigurationEditor.synchronization.heading') }}
          </q-item-section>
        </template>
        <q-card>
          <q-card-section>
            <div class="q-mb-md">
              {{ $t('ArtivactExchangeConfigurationEditor.synchronization.server.description') }}
            </div>
            <q-input
              outlined
              :label="$t('ArtivactExchangeConfigurationEditor.synchronization.server.label')"
              v-model="exchangeConfigurationRef.remoteServer"
            />
          </q-card-section>
          <q-card-section>
            <div class="q-mb-md">
              {{ $t('ArtivactExchangeConfigurationEditor.synchronization.token.description') }}
            </div>
            <q-input
              outlined
              :label="$t('ArtivactExchangeConfigurationEditor.synchronization.token.label')"
              v-model="exchangeConfigurationRef.apiToken"
            />
          </q-card-section>
        </q-card>
      </q-expansion-item>
    </q-list>
  </div>
</template>

<script setup lang="ts">

import {ExchangeConfiguration} from 'components/artivact-models';
import {PropType, toRef} from 'vue';

const props = defineProps({
  exchangeConfiguration: {
    required: true,
    type: Object as PropType<ExchangeConfiguration | null>,
  },
});

const exchangeConfigurationRef = toRef(props, 'exchangeConfiguration');

</script>

<style scoped>

</style>
