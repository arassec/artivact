<template>
  <ArtivactContent>
      <div class="col">

        <h1 class="av-text-h1">Filesystem Administration</h1>
        <label>Scans the data directory for new or updated Artivacts.</label>

        <q-btn label="Scan" color="primary" class="float-right q-mb-lg" @click="scanArtivacts()"/>

      </div>
  </ArtivactContent>
</template>

<script setup lang="ts">

import {api} from 'boot/axios';
import {useQuasar} from 'quasar';
import ArtivactContent from 'components/ArtivactContent.vue';

function scanArtivacts() {
  const $q = useQuasar();

  api.post('/api/administration/scan')
    .then(() => {
      $q.notify({
        color: 'positive',
        position: 'bottom',
        message: 'Scan successful',
        icon: 'report'
      })
    })
    .catch(() => {
      $q.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Scan failed',
        icon: 'report_problem'
      })
    })
}

</script>

<style scoped>
</style>
