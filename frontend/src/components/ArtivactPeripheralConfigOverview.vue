<template>
  <div class="full-width bg-primary q-mb-sm row rounded-borders">
    <div class="col text-white q-ma-sm config-label">
      <q-icon name="star" v-if="peripheralConfigRef.favourite" />
      <q-icon name="star_outline" v-if="!peripheralConfigRef.favourite" />
      {{ peripheralConfigRef.label }}
    </div>
    <div>
      <q-icon
        v-if="peripheralStatusRef && peripheralStatusRef == 'AVAILABLE'"
        name="done"
        size="sm"
        color="positive"
        class="q-ml-xs"
      >
        <q-tooltip>{{
          $t('PeripheralStatus.' + peripheralStatusRef)
        }}</q-tooltip>
      </q-icon>
      <q-icon
        v-if="peripheralStatusRef && peripheralStatusRef != 'AVAILABLE'"
        name="bolt"
        size="sm"
        color="negative"
        class="q-ml-xs"
      >
        <q-tooltip>{{
          $t('PeripheralStatus.' + peripheralStatusRef)
        }}</q-tooltip>
      </q-icon>
      <q-btn
        class="q-ma-xs"
        round
        dense
        flat
        @click="$emit('edit-config')"
        icon="edit"
        color="white"
      />
      <q-btn
        class="q-ma-xs"
        round
        dense
        flat
        @click="$emit('delete-config')"
        icon="delete"
        color="white"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { PropType, toRef } from 'vue';
import { PeripheralConfig } from './artivact-models';

const props = defineProps({
  peripheralConfig: {
    required: true,
    type: Object as PropType<PeripheralConfig | null>,
  },
  peripheralStatus: {
    required: false,
    type: String,
  },
});

const peripheralConfigRef = toRef(props, 'peripheralConfig');
const peripheralStatusRef = toRef(props, 'peripheralStatus');
</script>

<style scoped>
.config-label {
  font-size: 16px;
}
</style>
