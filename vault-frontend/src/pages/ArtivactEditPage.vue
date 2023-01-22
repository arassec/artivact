<template>
  <q-page v-if="artivactDataRef" class="q-ma-lg">

    <div class="row configuration-area">
      <div class="col items-center">
        <h1 class="av-text-h1">Base Data</h1>
        <div class="q-mb-sm">
          <label class="q-mr-xs q-mt-xs vertical-middle">Tags:</label>
          <q-badge class="q-mr-xs vertical-middle" color="secondary" v-for="(tag, index) in artivactDataRef.tags"
                   :key="index">{{ tag.translatedValue }}
            <q-btn rounded dense flat color="primary" size="xs" icon="close" @click="removeTag(tag)"/>
          </q-badge>
          <q-btn class="vertical-middle" round dense unelevated color="secondary" size="xs" icon="add" @click="addTag"/>
          <q-dialog v-model="addTagRef" persistent transition-show="scale" transition-hide="scale"
                    class="justify-center">
            <q-card class="dialog-card">
              <q-card-section>
                Add Tag
              </q-card-section>
              <q-card-section class="column-lg q-ma-md items-center">
                <q-select v-model="tagValueRef" autofocus :options="tagsDataRef.tags" option-value="id"
                          option-label="translatedValue" label="Tag"/>
              </q-card-section>
              <q-card-actions>
                <q-btn flat label="Cancel" v-close-popup/>
                <q-space/>
                <q-btn flat label="Save" v-close-popup @click="saveSelectedTag"/>
              </q-card-actions>
            </q-card>
          </q-dialog>
        </div>
        <q-separator class="q-mb-lg"/>

        <artivact-translatable-item-editor :item="artivactDataRef.title" :locales="localesRef" label="Title"/>
        <artivact-translatable-item-editor :item="artivactDataRef.description" :locales="localesRef" label="Description"
                                           :textarea="true"/>

        <h1 class="av-text-h1">Media</h1>
        <div>
          <artivact-media-editor :images="artivactDataRef.images" :models="artivactDataRef.models"/>
        </div>

        <h1 class="av-text-h1">Properties</h1>
        <div v-if="propertiesDataRef && artivactDataRef">
          <artivact-property-category-editor v-for="(category, index) in propertiesDataRef" :category="category"
                                             :key="index" :properties="artivactDataRef.properties"/>
        </div>

        <q-separator class="q-mt-md q-mb-md"/>

        <div>
          <router-link :to="'/artivact/' + artivactDataRef.id">
            <q-btn label="Cancel" color="primary"/>
          </router-link>
          <router-link :to="'/artivact/' + artivactDataRef.id">
            <q-btn label="Save" @click="save" color="primary" class="float-right"/>
          </router-link>
        </div>

      </div>
    </div>
  </q-page>
</template>

<script>
import {useQuasar} from 'quasar';
import {onMounted, ref} from 'vue';
import {useRoute} from 'vue-router';
import {api} from 'boot/axios';
import ArtivactTranslatableItemEditor from '../components/ArtivactTranslatableItemEditor';
import ArtivactPropertyCategoryEditor from '../components/ArtivactPropertyCategoryEditor';
import ArtivactMediaEditor from '../components/ArtivactMediaEditor';

export default {
  name: 'EditPage',
  components: {ArtivactMediaEditor, ArtivactPropertyCategoryEditor, ArtivactTranslatableItemEditor},
  setup() {
    const $q = useQuasar()

    const artivactData = ref(null)
    const propertiesData = ref(null)
    const tagsDataRef = ref(null)
    const route = useRoute();

    const localesRef = ref([]);

    function addTag() {
      tagValueRef.value = null;
      addTagRef.value = true;
    }

    function loadArtivactData(artivactId) {
      api.get('/api/artivact/' + artivactId)
        .then((response) => {
          artivactData.value = response.data
        })
        .catch(() => {
          $q.notify({
            color: 'negative',
            position: 'top',
            message: 'Loading artivact failed',
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

    function loadPropertiesData() {
      api.get('/api/configuration/property/translated')
        .then((response) => {
          propertiesData.value = response.data
        })
        .catch(() => {
          $q.notify({
            color: 'negative',
            position: 'top',
            message: 'Loading properties failed',
            icon: 'report_problem'
          })
        })
    }

    function loadTagsData() {
      api.get('/api/configuration/tags/translated')
        .then((response) => {
          tagsDataRef.value = response.data
        })
        .catch(() => {
          $q.notify({
            color: 'negative',
            position: 'top',
            message: 'Loading tags failed',
            icon: 'report_problem'
          })
        })
    }

    function saveSelectedTag() {
      artivactData.value.tags.push(tagValueRef.value);
    }

    function removeTag(tag) {
      artivactData.value.tags = artivactData.value.tags.filter(item => item !== tag);
    }

    onMounted(() => {
      loadPropertiesData();
      loadTagsData();
      loadLocales();
      loadArtivactData(route.params.artivactId)
    })

    let addTagRef = ref(false);
    const tagValueRef = ref(null);

    return {
      artivactDataRef: ref(artivactData),
      propertiesDataRef: ref(propertiesData),

      addTagRef,
      tagValueRef,
      tagsDataRef,
      localesRef,

      addTag,
      saveSelectedTag,
      removeTag,

      save() {
        let artivact = artivactData.value;
        api.post('/api/artivact', artivact)
          .then(() => {
            $q.notify({
              color: 'positive',
              position: 'top',
              message: 'Configuration saved',
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
      },

    }
  }
}
</script>

<style scoped>
.configuration-area {
  width: 75%;
}

.dialog-card {
  min-width: 25em;
}
</style>
