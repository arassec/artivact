<template>
  <ArtivactContent>
    <div>
      <h1 class="av-text-h1">{{ $t('PropertiesConfigurationPage.heading') }}</h1>
      <div class="q-mb-lg">
        {{ $t('PropertiesConfigurationPage.description') }}
      </div>

      <div
        class="q-mb-md"
        v-if="
          !propertiesConfigurationRef ||
          !propertiesConfigurationRef.categories ||
          propertiesConfigurationRef.categories.length == 0
        "
      >
        {{ $t('PropertiesConfigurationPage.noPropertiesDefined') }}
      </div>

      <artivact-properties-configuration-editor
        v-if="propertiesConfigurationRef"
        :properties-configuration="propertiesConfigurationRef"
        :locales="localeStore.locales"
      />

      <q-separator class="q-mt-md q-mb-md" />

      <q-btn
        data-test="save-properties-button"
        :label="$t('Common.save')"
        color="primary"
        class="float-right q-mb-lg"
        @click="saveProperties()"
      />
    </div>

    <div>
      <h1 class="av-text-h1">{{ $t('PropertiesConfigurationPage.importexport.heading') }}</h1>

      <div class="q-mb-md">
        {{ $t('PropertiesConfigurationPage.importexport.export') }}
        <q-form :action="'/api/exchange/properties/export'" method="get">
          <q-btn
            icon="download"
            :label="$t('PropertiesConfigurationPage.importexport.button.export')"
            color="primary"
            type="submit"
            class="q-mt-md"
          />
        </q-form>
      </div>

      <div>
        {{ $t('PropertiesConfigurationPage.importexport.import') }}
        <q-uploader
          :url="'/api/exchange/properties/import'"
          :label="$t('PropertiesConfigurationPage.importexport.button.import')"
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
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.configuration.properties')}),
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
        message: i18n.t('Common.messages.saving.success', { item: i18n.t('Common.items.configuration.properties')}),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', { item: i18n.t('Common.items.configuration.properties')}),
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
