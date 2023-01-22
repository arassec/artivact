<template>
  <q-page class="row justify-center">
    <div class="row configuration-area">
      <div class="col items-center">

        <h1 class="av-text-h1">Tags Configuration</h1>

        <artivact-tags-configuration-editor :tags-configuration="tagsConfigurationRef" :locales="localesRef"/>

        <q-separator class="q-mt-md q-mb-md"/>

        <q-btn label="Save" color="primary" class="float-right q-mb-lg" @click="saveTagsConfiguration()"/>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import {api} from 'boot/axios';
import {useQuasar} from 'quasar';
import {onMounted, Ref, ref} from 'vue';
import {TagsConfiguration} from 'components/models';
import ArtivactTagsConfigurationEditor from 'components/ArtivactTagsConfigurationEditor.vue';

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
        position: 'top',
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
        position: 'top',
        message: 'Loading failed',
        icon: 'report_problem'
      })
    })
}

const $q = useQuasar();
function saveTagsConfiguration() {
  api.post('/api/administration/tags', json)
    .then(() => {
      $q.notify({
        color: 'positive',
        position: 'top',
        message: 'Tags saved',
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
  loadTagsConfiguration();
})

</script>

<style scoped>
.configuration-area {
  width: 75%;
}
</style>
