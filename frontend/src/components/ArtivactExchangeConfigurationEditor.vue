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
            Synchronization Configuration
          </q-item-section>
        </template>
        <q-card>
          <q-card-section>
            <div class="q-mb-md">
              URL of the remote Artivact server you want to use to synchronize items.
            </div>
            <q-input
              outlined
              label="Remote Artivact Server"
              v-model="exchangeConfigurationRef.remoteServer"
            />
          </q-card-section>
          <q-card-section>
            <div class="q-mb-md">
              API token to use for synchronization. The token configured here must also be configured for a user
              account on the remote server. This user will be used on the remote side for authentication and authorization.
            </div>
            <q-input
              outlined
              label="API Token"
              v-model="exchangeConfigurationRef.apiToken"
            />
          </q-card-section>
        </q-card>
      </q-expansion-item>
    </q-list>
  </div>
</template>

<script setup lang="ts">

import {ExchangeConfiguration} from 'components/models';
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
