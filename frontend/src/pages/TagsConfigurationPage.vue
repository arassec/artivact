<template>
  <ArtivactContent>
    <div class="col">
      <h1 class="av-text-h1">Tags Configuration</h1>
      <div class="q-mb-lg">
        Tags can be used to categorize items beyond their properties. They
        should be considered meta-data and not be used to replace properties.
      </div>

      <div
        v-if="!tagsConfigurationRef || tagsConfigurationRef.tags.length == 0"
        class="q-mb-md"
      >
        There are currently no tags defined.
      </div>

      <artivact-tags-configuration-editor
        :tags-configuration="tagsConfigurationRef"
        :locales="localesRef"
      />

      <q-separator class="q-mt-md q-mb-md" />

      <q-btn
        label="Save"
        color="primary"
        class="float-right q-mb-lg"
        @click="saveTagsConfiguration()"
      />
    </div>
  </ArtivactContent>
</template>

<script setup lang="ts">
import { api } from 'boot/axios';
import { useQuasar } from 'quasar';
import { onMounted, Ref, ref } from 'vue';
import { TagsConfiguration } from 'components/models';
import ArtivactTagsConfigurationEditor from 'components/ArtivactTagsConfigurationEditor.vue';
import ArtivactContent from 'components/ArtivactContent.vue';

const quasar = useQuasar();

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
        message: 'Loading locales failed',
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
        message: 'Loading failed',
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
        message: 'Tags saved',
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Saving failed',
        icon: 'report_problem',
      });
    });
}

onMounted(() => {
  loadLocales();
  loadTagsConfiguration();
});
</script>

<style scoped></style>
