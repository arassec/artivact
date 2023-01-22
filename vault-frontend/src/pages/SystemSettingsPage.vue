<template>
  <q-page class="row justify-center">
    <div class="row configuration-area">
      <div class="col items-center">

        <div>
          <h1 class="av-text-h1">License Configuration</h1>
          <artivact-license-configuration-editor :license-configuration="licenseConfigurationRef"
                                                 :locales="localesRef"/>
          <q-btn label="Save License" color="primary" class="q-mb-lg"
                 @click="saveLicenseConfiguration()"/>
        </div>
        <q-separator/>

        <h1 class="av-text-h1">Filesystem Administration</h1>
        <label>Scans the data directory for new Artivacts.</label>
        <q-btn label="Scan" color="primary" class="float-right q-mb-lg" @click="scanArtivacts()"/>

      </div>
    </div>
  </q-page>
</template>

<!--suppress JSUnusedGlobalSymbols -->
<script setup lang="ts">

import {api} from 'boot/axios';
import {useQuasar} from 'quasar';
import {onMounted, Ref, ref} from 'vue';
import ArtivactLicenseConfigurationEditor from 'components/ArtivactLicenseConfigurationEditor.vue';
import {LicenseConfiguration} from 'components/models';

const $q = useQuasar();

function scanArtivacts() {
  api.post('/api/administration/scan')
    .then(() => {
      $q.notify({
        color: 'positive',
        position: 'top',
        message: 'Scan successful',
        icon: 'report'
      })
    })
    .catch(() => {
      $q.notify({
        color: 'negative',
        position: 'top',
        message: 'Scan failed',
        icon: 'report_problem'
      })
    })
}

const localesRef = ref([]);
const licenseConfigurationRef: Ref<LicenseConfiguration | null> = ref(null)

let json = {} as LicenseConfiguration;

function loadLicenseConfiguration() {
  api.get('/api/administration/license')
    .then((response) => {
      json = response.data;
      licenseConfigurationRef.value = json;
    })
    .catch(() => {
      $q.notify({
        color: 'negative',
        position: 'top',
        message: 'License configuration loading failed',
        icon: 'report_problem'
      })
    })
}

function loadLocales() {
  api.get('/api/configuration/locale')
    .then((response) => {
      localesRef.value = response.data
    })
    .catch(() => {
      $q.notify({
        color: 'negative',
        position: 'top',
        message: 'Loading locales failed',
        icon: 'report_problem'
      })
    })
}

function saveLicenseConfiguration() {
  api.post('/api/administration/license', json)
    .then(() => {
      $q.notify({
        color: 'positive',
        position: 'top',
        message: 'License configuration saved',
        icon: 'report'
      })
    })
    .catch(() => {
      $q.notify({
        color: 'negative',
        position: 'top',
        message: 'Saving failed',
        icon: 'report_problem'
      })
    })
}

onMounted(() => {
  loadLocales();
  loadLicenseConfiguration();
})

</script>

<style scoped>

</style>
