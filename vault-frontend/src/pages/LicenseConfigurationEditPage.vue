<template>
  <ArtivactContent>
      <div>
        <h1 class="av-text-h1">License Configuration</h1>
        <artivact-license-configuration-editor :license-configuration="licenseConfigurationRef"
                                               :locales="localesRef"/>
        <q-btn label="Save License" color="primary" class="q-mb-lg float-right"
               @click="saveLicenseConfiguration()"/>
      </div>
  </ArtivactContent>
</template>

<script setup lang="ts">

import {useQuasar} from 'quasar';
import {api} from 'boot/axios';
import {onMounted, Ref, ref} from 'vue';
import {LicenseConfiguration} from 'components/models';
import ArtivactLicenseConfigurationEditor from 'components/ArtivactLicenseConfigurationEditor.vue';
import ArtivactContent from 'components/ArtivactContent.vue';

// noinspection JSUnusedGlobalSymbols
const $q = useQuasar();

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
        position: 'bottom',
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
        position: 'bottom',
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
        position: 'bottom',
        message: 'License configuration saved',
        icon: 'check'
      })
    })
    .catch(() => {
      $q.notify({
        color: 'negative',
        position: 'bottom',
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
