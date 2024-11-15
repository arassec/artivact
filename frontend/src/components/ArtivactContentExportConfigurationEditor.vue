<template>
  <div v-if="contentExportsRef">
    <q-list bordered class="rounded-borders q-mb-lg">
      <q-expansion-item
        v-for="standardExportInfo in contentExportsRef"
        v-bind:key="standardExportInfo.id"
        group="exchange"
        header-class="bg-primary text-white"
        class="list-entry"
        expand-separator
        expand-icon-class="text-white"
      >
        <template v-slot:header>
          <q-item-section class="list-entry-label">
            {{ standardExportInfo.title?.translatedValue ? standardExportInfo.title?.translatedValue : standardExportInfo.id }}
          </q-item-section>
          <q-item-section side>
            <q-btn
              round
              dense
              flat
              class="float-right"
              color="white"
              icon="delete"
              size="md"
              @click="$emit('delete-content-export', standardExportInfo)">
              <q-tooltip>{{ $t('ArtivactContentExportConfigurationEditor.tooltip.deleteContentExport') }}</q-tooltip>
            </q-btn>
          </q-item-section>
        </template>

        <q-card>
          <q-card-section>
            {{
              $t('ArtivactContentExportConfigurationEditor.label.lastModified') + new Date(standardExportInfo.lastModified)
            }}
          </q-card-section>
        </q-card>
        <q-card>
          <q-card-section>
            {{ standardExportInfo.description?.translatedValue }}
          </q-card-section>
        </q-card>

      </q-expansion-item>
    </q-list>
  </div>
</template>

<script setup lang="ts">

import {PropType, toRef} from 'vue';
import {ContentExport} from 'components/artivact-models';

const props = defineProps({
  contentExports: {
    required: true,
    type: Array as PropType<Array<ContentExport>>
  },
});

const contentExportsRef = toRef(props, 'contentExports');

</script>

<style scoped>
.list-entry {
  border-bottom: 1px solid white;
}
</style>
