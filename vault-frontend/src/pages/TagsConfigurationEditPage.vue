<template>
  <ArtivactContent>
    <div class="col">
      <h1 class="av-text-h1">Tags Configuration</h1>

      <artivact-tags-configuration-editor :tags-configuration="tagsConfigurationRef" :locales="localesRef"/>

      <q-separator class="q-mt-md q-mb-md"/>

      <q-btn label="Save" color="primary" class="float-right q-mb-lg" @click="saveTagsConfiguration()"/>
    </div>
  </ArtivactContent>
</template>

<script setup lang="ts">
import {api} from 'boot/axios';
import {useQuasar} from 'quasar';
import {onMounted, Ref, ref} from 'vue';
import {TagsConfiguration} from 'components/models';
import ArtivactTagsConfigurationEditor from 'components/ArtivactTagsConfigurationEditor.vue';
import ArtivactContent from 'components/ArtivactContent.vue';

const localesRef = ref([]);
const tagsConfigurationRef: Ref<TagsConfiguration | null> = ref(null)

let json = {} as TagsConfiguration;

function loadLocales() {
  let $q = useQuasar();
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

function loadTagsConfiguration() {
  let $q = useQuasar();
  api.get('/api/administration/tags')
    .then((response) => {
      json = response.data;
      tagsConfigurationRef.value = json;
    })
    .catch(() => {
      $q.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Loading failed',
        icon: 'report_problem'
      })
    })
}

function saveTagsConfiguration() {
  const $q = useQuasar();

  api.post('/api/administration/tags', json)
    .then(() => {
      $q.notify({
        color: 'positive',
        position: 'bottom',
        message: 'Tags saved',
        icon: 'report'
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
  loadTagsConfiguration();
})

</script>

<style scoped>
</style>
