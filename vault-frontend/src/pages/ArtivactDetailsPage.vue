<template>
  <ArtivactContent v-if="artivactDetails">

    <div class="col-12">
      <div class="col items-center">
        <router-link :to="'/administration/configuration/artivact/' + artivactDetails.id"
                     v-if="userdataStore.authenticated">
          <q-btn round color="primary" icon="edit" class="q-mr-md"/>
        </router-link>
        <label class="av-label-h1">{{ artivactDetails.title.translatedValue }}</label>

        <artivact-media-carousel
          v-if="artivactDetails.images.length > 0 || artivactDetails.models.length > 0"
          :artivact-details="artivactDetails"/>

      </div>
    </div>

    <div class="col-12 q-mt-lg row">
        <template v-if="artivactDetails && propertiesDataRef">
          <div class="col property-category" v-for="(category, index) in propertiesDataRef" :key="index">
            <artivact-property-category-viewer :category="category" :properties="artivactDetails.properties"/>
          </div>
        </template>
    </div>
  </ArtivactContent>
</template>

<script>
import {useQuasar} from 'quasar';
import {api} from 'boot/axios';
import {useRoute} from 'vue-router';
import {onMounted, ref} from 'vue';
import ArtivactMediaCarousel from '../components/ArtivactMediaCarousel';
import ArtivactPropertyCategoryViewer from '../components/ArtivactPropertyCategoryViewer';
import {useUserdataStore} from 'stores/userdata';
import ArtivactContent from 'components/ArtivactContent.vue';

export default {
  name: 'DetailsPage',
  components: {ArtivactContent, ArtivactPropertyCategoryViewer, ArtivactMediaCarousel},
  setup() {
    const $q = useQuasar()

    const data = ref(null)
    const route = useRoute();
    const fullscreen = ref(false)
    const propertiesData = ref(null);

    const userdataStore = useUserdataStore();

    function loadData(artivactId) {
      api.get('/api/artivact/' + artivactId)
        .then((response) => {
          data.value = response.data
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

    onMounted(() => {
      loadData(route.params.artivactId)
      loadPropertiesData();
    })

    return {
      artivactDetails: ref(data),
      propertiesDataRef: ref(propertiesData),
      userdataStore,
      fullscreen
    }
  }
}
</script>

<style scoped>
.property-category {
  min-width: 20rem;
}
</style>
