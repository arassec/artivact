<template>
  <ArtivactContent>
    <div>
      <h1 class="av-text-h1">{{ $t('LicenseConfigurationPage.heading') }}</h1>
      <artivact-license-configuration-editor
        :license-configuration="licenseConfigurationRef"
        :locales="localesRef"
      />
      <q-btn
        :label="$t('Common.save')"
        color="primary"
        class="q-mb-lg float-right"
        @click="saveLicenseConfiguration()"
      />
    </div>
  </ArtivactContent>
</template>

<script setup lang="ts">
import { useQuasar } from 'quasar';
import { api } from 'boot/axios';
import { onMounted, Ref, ref } from 'vue';
import { LicenseConfiguration } from 'components/models';
import ArtivactLicenseConfigurationEditor from 'components/ArtivactLicenseConfigurationEditor.vue';
import ArtivactContent from 'components/ArtivactContent.vue';
import {useI18n} from 'vue-i18n';

const quasar = useQuasar();
const i18n = useI18n();

const localesRef = ref([]);
const licenseConfigurationRef: Ref<LicenseConfiguration | null> = ref(null);

let json = {} as LicenseConfiguration;

function loadLicenseConfiguration() {
  api
    .get('/api/configuration/license')
    .then((response) => {
      json = response.data;
      licenseConfigurationRef.value = json;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.configuration.license') }),
        icon: 'report_problem',
      });
    });
}

function loadLocales() {
  api
    .get('/api/configuration/public/locale')
    .then((response) => {
      localesRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.locales') }),
        icon: 'report_problem',
      });
    });
}

function saveLicenseConfiguration() {
  api
    .post('/api/configuration/license', json)
    .then(() => {
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.success', { item: i18n.t('Common.items.configuration.license') }),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', { item: i18n.t('Common.items.configuration.license') }),
        icon: 'report_problem',
      });
    });
}

onMounted(() => {
  loadLocales();
  loadLicenseConfiguration();
});
</script>

<style scoped></style>
