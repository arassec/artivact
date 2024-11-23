<template>
  <ArtivactContent>
    <div class="full-width">
      <h1 class="av-text-h1">{{ $t('PropertiesConfigurationPage.heading') }}</h1>

      <q-tabs v-model="tab">
        <q-tab name="configuration" icon="build" :label="$t('PropertiesConfigurationPage.tabs.configuration')">
        </q-tab>
        <q-tab name="export" icon="backup" :label="$t('PropertiesConfigurationPage.tabs.export')">
        </q-tab>
        <q-tab name="import" icon="download" :label="$t('PropertiesConfigurationPage.tabs.import')">
        </q-tab>
      </q-tabs>

      <div v-if="tab == 'configuration'">
        <h2 class="av-text-h2">{{ $t('PropertiesConfigurationPage.configuration.heading') }}</h2>

        <div class="q-mb-lg">
          {{ $t('PropertiesConfigurationPage.configuration.description') }}
        </div>

        <div
          class="q-mb-md"
          v-if="
          !propertiesConfigurationRef ||
          !propertiesConfigurationRef.categories ||
          propertiesConfigurationRef.categories.length == 0
        "
        >
          {{ $t('PropertiesConfigurationPage.configuration.noPropertiesDefined') }}
        </div>

        <artivact-properties-configuration-editor
          v-if="propertiesConfigurationRef"
          :properties-configuration="propertiesConfigurationRef"
          :locales="localeStore.locales"
        />

        <q-separator class="q-mt-md q-mb-md"/>

        <q-btn
          data-test="save-properties-button"
          :label="$t('Common.save')"
          color="primary"
          class="float-right q-mb-lg"
          @click="saveProperties()"
        />
      </div>

      <div v-if="tab == 'export'">
        <h2 class="av-text-h2">{{ $t('PropertiesConfigurationPage.export.heading') }}</h2>

        <div class="q-mb-md">
          {{ $t('PropertiesConfigurationPage.export.description') }}
          <q-form :action="'/api/export/properties'" method="get">
            <q-btn
              icon="download"
              :label="$t('PropertiesConfigurationPage.export.button')"
              color="primary"
              type="submit"
              class="q-mt-md"
            />
          </q-form>
        </div>
      </div>

      <div v-if="tab == 'import'">
        <h2 class="av-text-h2">{{ $t('PropertiesConfigurationPage.import.heading') }}</h2>

        {{ $t('PropertiesConfigurationPage.import.description') }}
        <q-uploader
          :url="'/api/import/properties'"
          :label="$t('PropertiesConfigurationPage.import.button')"
          class="q-mt-md q-mb-md"
          accept=".artivact.properties-configuration.json"
          field-name="file"
          :no-thumbnails="true"
          @finish="propertiesUploaded"
        />
      </div>

    </div>
  </ArtivactContent>
</template>

<script setup lang="ts">
import {useQuasar} from 'quasar';
import {onMounted, ref} from 'vue';
import {api} from 'boot/axios';
import ArtivactContent from 'components/ArtivactContent.vue';
import {useLocaleStore} from 'stores/locale';
import ArtivactPropertiesConfigurationEditor from 'components/ArtivactPropertiesConfigurationEditor.vue';
import {useI18n} from 'vue-i18n';

const quasar = useQuasar();
const i18n = useI18n();

const localeStore = useLocaleStore();

const propertiesConfigurationRef = ref();

const tab = ref('configuration');


function loadPropertyConfiguration() {
  api
    .get('/api/configuration/property')
    .then((response) => {
      propertiesConfigurationRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', {item: i18n.t('Common.items.configuration.properties')}),
        icon: 'report_problem',
      });
    });
}

function saveProperties() {
  api
    .post('/api/configuration/property', propertiesConfigurationRef.value)
    .then(() => {
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.success', {item: i18n.t('Common.items.configuration.properties')}),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', {item: i18n.t('Common.items.configuration.properties')}),
        icon: 'report_problem',
      });
    });
}

function propertiesUploaded() {
  loadPropertyConfiguration();
  quasar.notify({
    color: 'positive',
    position: 'bottom',
    message: i18n.t('PropertiesConfigurationPage.messages.uploadSuccess'),
    icon: 'check',
  });
}

onMounted(() => {
  loadPropertyConfiguration();
});
</script>

<style scoped></style>
