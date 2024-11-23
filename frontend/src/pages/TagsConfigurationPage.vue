<template>
  <ArtivactContent>
    <div class="full-width">
      <h1 class="av-text-h1">{{ $t('TagsConfigurationPage.heading') }}</h1>

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
          {{ $t('TagsConfigurationPage.configuration.description') }}
        </div>

        <div
          v-if="!tagsConfigurationRef || tagsConfigurationRef.tags.length == 0"
          class="q-mb-md"
        >
          {{ $t('TagsConfigurationPage.configuration.noTagsDefined') }}
        </div>

        <artivact-tags-configuration-editor
          :tags-configuration="tagsConfigurationRef"
          :locales="applicationSettings.availableLocales"
        />

        <q-separator class="q-mt-md q-mb-md"/>

        <q-btn
          :label="$t('Common.save')"
          color="primary"
          class="float-right q-mb-lg"
          @click="saveTagsConfiguration()"
        />
      </div>
    </div>

    <div v-if="tab == 'export'">
      <h2 class="av-text-h2">{{ $t('TagsConfigurationPage.export.heading') }}</h2>

      <div class="q-mb-md">
        {{ $t('TagsConfigurationPage.export.description') }}
        <q-form :action="'/api/export/tags'" method="get">
          <q-btn
            icon="download"
            :label="$t('TagsConfigurationPage.export.button')"
            color="primary"
            type="submit"
            class="q-mt-md"
          />
        </q-form>
      </div>
    </div>

    <div v-if="tab == 'import'">
      <h2 class="av-text-h2">{{ $t('TagsConfigurationPage.import.heading') }}</h2>

      <div>
        {{ $t('TagsConfigurationPage.import.description') }}
        <q-uploader
          :url="'/api/import/tags'"
          :label="$t('TagsConfigurationPage.import.button')"
          class="q-mt-md q-mb-md"
          accept=".artivact.tags-configuration.json"
          field-name="file"
          :no-thumbnails="true"
          @finish="tagsUploaded"
        />
      </div>
    </div>

  </ArtivactContent>
</template>

<script setup lang="ts">
import {api} from 'boot/axios';
import {useQuasar} from 'quasar';
import {onMounted, Ref, ref} from 'vue';
import {TagsConfiguration} from 'components/artivact-models';
import ArtivactTagsConfigurationEditor from 'components/ArtivactTagsConfigurationEditor.vue';
import ArtivactContent from 'components/ArtivactContent.vue';
import {useI18n} from 'vue-i18n';
import {useApplicationSettingsStore} from "stores/application-settings";

const quasar = useQuasar();
const i18n = useI18n();

const applicationSettings = useApplicationSettingsStore();

const tagsConfigurationRef: Ref<TagsConfiguration | null> = ref(null);

const tab = ref('configuration');

let json = {} as TagsConfiguration;


function loadTagsConfiguration() {
  api
    .get('/api/configuration/tags')
    .then((response) => {
      json = response.data;
      tagsConfigurationRef.value = json;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', {item: i18n.t('Common.items.configuration.tags')}),
        icon: 'report_problem',
      });
    });
}

function saveTagsConfiguration() {
  api
    .post('/api/configuration/tags', json)
    .then(() => {
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.success', {item: i18n.t('Common.items.configuration.tags')}),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', {item: i18n.t('Common.items.configuration.tags')}),
        icon: 'report_problem',
      });
    });
}

function tagsUploaded() {
  loadTagsConfiguration();
  quasar.notify({
    color: 'positive',
    position: 'bottom',
    message: i18n.t('TagsConfigurationPage.messages.uploadSuccess'),
    icon: 'check',
  });
}

onMounted(() => {
  loadTagsConfiguration();
});
</script>

<style scoped></style>
