<template>
  <ArtivactContent>
    <div class="col">
      <h1 class="av-text-h1">Item Import</h1>
      <label>Scans the data directory for new or updated items.</label>

      <q-btn
        label="Scan"
        color="primary"
        class="float-right q-mb-lg"
        @click="scanItems()"
      />

      <h1 class="av-text-h1">Create Search Index</h1>
      <label>(Re-)Creates the search index.</label>

      <q-btn
        label="(Re-)Create Search Index"
        color="primary"
        class="float-right q-mb-lg"
        @click="recreateSearchIndex()"
      />
    </div>
  </ArtivactContent>
</template>

<script setup lang="ts">
import { api } from 'boot/axios';
import { useQuasar } from 'quasar';
import ArtivactContent from 'components/ArtivactContent.vue';

const quasar = useQuasar();

function scanItems() {
  api
    .post('/api/import')
    .then(() => {
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: 'Scan successful',
        icon: 'done',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Scan failed',
        icon: 'report_problem',
      });
    });
}

function recreateSearchIndex() {
  api
    .post('/api/search/index/recreate')
    .then(() => {
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: 'Index created successfully',
        icon: 'done',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Index creation failed',
        icon: 'report_problem',
      });
    });
}
</script>

<style scoped></style>
