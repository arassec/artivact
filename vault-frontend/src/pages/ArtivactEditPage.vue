<template>
  <ArtivactContent v-if="artivactDataRef">

    <div class="col-12">
      <div class="col items-center">
        <h1 class="av-text-h1">Base Data</h1>

        <artivact-restrictions-editor :restrictions="artivactDataRef.restrictions"
          @delete-restriction="removeRestriction" @add-restriction="addRestriction" class="q-mb-md"/>
        <q-separator class="q-mb-md"/>

        <div class="q-mb-sm" v-if="tagsDataRef" v-show="tagsDataRef.tags.length > 0">
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
        <h2 class="av-text-h2">Images</h2>
        <div>
          <artivact-image-editor :images="artivactDataRef.images" :models="artivactDataRef.models"
                                 :artivact-id="artivactDataRef.id" @uploaded="loadArtivactMediaData(artivactDataRef.id)"/>
        </div>
        <h2 class="av-text-h2">3D Models</h2>
        <div class="row">
          <q-uploader :url="'/api/artivact/media/' + artivactDataRef.id + '/model/upload'" label="Add Models" multiple
                      class="uploader q-mb-md col-12"
                      accept=".glb" field-name="file"
                      :no-thumbnails="true"
                      @uploaded="loadArtivactMediaData(artivactDataRef.id)"
          ></q-uploader>
          <draggable :list="artivactDataRef.models" item-key="fileName" group="models" class="row">
            <template #item="{ element }">
              <q-card class="model-card q-mr-md">
                <q-btn icon="delete" class="absolute-top-right" dense round flat
                       @click="deleteModel(element)"></q-btn>
                <q-card-section class="absolute-center text-h5">
                  {{ element.fileName }}
                </q-card-section>
              </q-card>
            </template>
          </draggable>
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
          <q-btn label="Delete" @click="confirmDeleteRef = true" color="primary" class="q-ml-sm"/>
          <q-btn label="Save" @click="save" color="primary" class="float-right"/>
        </div>

      </div>
    </div>

    <q-dialog v-model="confirmDeleteRef" persistent>
      <q-card>
        <q-card-section class="row items-center">
          <q-icon name="warning" size="md" color="warning" class="q-mr-md"></q-icon><h3 class="av-text-h3">Delete Artivact?</h3>
          <div class="q-ml-sm"> Are you sure you want to delete this Artivact and all its files? This action cannot be undone!</div>
        </q-card-section>

        <q-card-section>
          <q-btn flat label="Cancel" color="primary" v-close-popup/>
          <q-btn flat label="Delete Artivact" color="primary" v-close-popup @click="deleteArtivact" class="float-right"/>
        </q-card-section>
      </q-card>
    </q-dialog>

  </ArtivactContent>
</template>

<script>
import draggable from 'vuedraggable';
import {useQuasar} from 'quasar';
import {onMounted, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {api} from 'boot/axios';
import ArtivactTranslatableItemEditor from '../components/ArtivactTranslatableItemEditor';
import ArtivactPropertyCategoryEditor from '../components/ArtivactPropertyCategoryEditor';
import ArtivactImageEditor from '../components/ArtivactImageEditor';
import ArtivactContent from 'components/ArtivactContent.vue';
import ArtivactRestrictionsEditor from 'components/ArtivactRestrictionsEditor.vue';

export default {
  name: 'EditPage',
  components: {
    ArtivactRestrictionsEditor,
    ArtivactContent,
    draggable, ArtivactImageEditor, ArtivactPropertyCategoryEditor, ArtivactTranslatableItemEditor},
  setup() {
    const $q = useQuasar()
    const $r = useRouter();

    const artivactData = ref(null)
    const propertiesData = ref(null)
    const tagsDataRef = ref(null)
    const route = useRoute();

    const localesRef = ref([]);

    const confirmDeleteRef = ref(false);

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
            position: 'bottom',
            message: 'Loading artivact failed',
            icon: 'report_problem'
          })
        })
    }

    function loadArtivactMediaData(artivactId) {
      api.get('/api/artivact/' + artivactId)
        .then((response) => {
          artivactData.value.images = response.data.images;
          artivactData.value.models = response.data.models;
        })
        .catch(() => {
          $q.notify({
            color: 'negative',
            position: 'bottom',
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
            position: 'bottom',
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
            position: 'bottom',
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
            position: 'bottom',
            message: 'Loading tags failed',
            icon: 'report_problem'
          })
        })
    }

    function saveSelectedTag() {
      artivactData.value?.tags.push(tagValueRef.value);
    }

    function removeTag(tag) {
      artivactData.value.tags = artivactData.value?.tags.filter(item => item !== tag);
    }

    function removeRestriction(role) {
      const index = artivactData.value.restrictions.indexOf(role);
      if (index > -1) {
        artivactData.value.restrictions.splice(index, 1);
      }
    }

    function addRestriction(role) {
      artivactData.value.restrictions.push(role);
    }

    function deleteModel(element) {
      artivactData.value?.models.splice(artivactData.value?.models.indexOf(element), 1);
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
      confirmDeleteRef,

      loadArtivactMediaData,
      addTag,
      saveSelectedTag,
      removeTag,
      deleteModel,
      addRestriction,
      removeRestriction,

      save() {
        let artivact = artivactData.value;
        api.post('/api/artivact', artivact)
          .then(() => {
            $r.push('/artivact/' + artivact.id)
            $q.notify({
              color: 'positive',
              position: 'bottom',
              message: 'Configuration saved',
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
      },

      deleteArtivact() {
        let artivact = artivactData.value;
        api.delete('/api/artivact/' + artivact.id)
          .then(() => {
            $r.push('/')
            $q.notify({
              color: 'positive',
              position: 'bottom',
              message: 'Artivact deleted',
              icon: 'report'
            })
          })
          .catch(() => {
            $q.notify({
              color: 'negative',
              position: 'bottom',
              message: 'Artivact deletion failed',
              icon: 'report_problem'
            })
          })
      }

    }
  }
}
</script>

<style scoped>

.dialog-card {
  min-width: 25em;
}

.model-card {
  width: 200px;
  height: 150px;
}

</style>
