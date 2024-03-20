<template>
  <ArtivactContent>
    <div>
      <h1 class="av-text-h1">{{ $t('TagsConfigurationPage.heading') }}</h1>
      <div class="q-mb-lg">
        {{ $t('TagsConfigurationPage.description') }}
      </div>

      <div
        v-if="!tagsConfigurationRef || tagsConfigurationRef.tags.length == 0"
        class="q-mb-md"
      >
        {{ $t('TagsConfigurationPage.noTagsDefined') }}
      </div>

      <artivact-tags-configuration-editor
        :tags-configuration="tagsConfigurationRef"
        :locales="localesRef"
      />

      <q-separator class="q-mt-md q-mb-md" />

      <q-btn
        :label="$t('Common.save')"
        color="primary"
        class="float-right q-mb-lg"
        @click="saveTagsConfiguration()"
      />
    </div>

    <div>
      <h1 class="av-text-h1">{{ $t('TagsConfigurationPage.importexport.heading') }}</h1>

      <div class="q-mb-md">
        {{ $t('TagsConfigurationPage.importexport.export') }}
        <q-form :action="'/api/exchange/tags/export'" method="get">
          <q-btn
            icon="download"
            :label="$t('TagsConfigurationPage.importexport.button.export')"
            color="primary"
            type="submit"
            class="q-mt-md"
          />
        </q-form>
      </div>

      <div>
        {{ $t('TagsConfigurationPage.importexport.import') }}
        <q-uploader
          :url="'/api/exchange/tags/import'"
          :label="$t('TagsConfigurationPage.importexport.button.import')"
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
import { api } from 'boot/axios';
import { useQuasar } from 'quasar';
import { onMounted, Ref, ref } from 'vue';
import { TagsConfiguration } from 'components/artivact-models';
import ArtivactTagsConfigurationEditor from 'components/ArtivactTagsConfigurationEditor.vue';
import ArtivactContent from 'components/ArtivactContent.vue';
import {useI18n} from 'vue-i18n';

const quasar = useQuasar();
const i18n = useI18n();

const localesRef = ref([]);
const tagsConfigurationRef: Ref<TagsConfiguration | null> = ref(null);

let json = {} as TagsConfiguration;

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
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.locales')}),
        icon: 'report_problem',
      });
    });
}

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
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.configuration.tags')}),
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
        message: i18n.t('Common.messages.saving.success', { item: i18n.t('Common.items.configuration.tags')}),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', { item: i18n.t('Common.items.configuration.tags')}),
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
  loadLocales();
  loadTagsConfiguration();
});
</script>

<style scoped></style>
