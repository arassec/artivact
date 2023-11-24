<template>
  <ArtivactContent v-if="itemDataDetailsRef">
    <div>
      <q-breadcrumbs>
        <template
          v-for="(breadcrumb, index) in breadcrumbsStore.breadcrumbs"
          v-bind:key="index"
        >
          <q-breadcrumbs-el
            :label="breadcrumb.label"
            :to="'/page/' + breadcrumb.target"
            v-if="breadcrumb.label && breadcrumb.target"
            style="text-decoration: underline"
          />
          <q-breadcrumbs-el :label="breadcrumb.label" v-else />
        </template>
      </q-breadcrumbs>
    </div>

    <div class="col-12">
      <div class="col items-center">
        <router-link
          :to="'/administration/configuration/item/' + itemDataDetailsRef.id"
          v-if="userdataStore.authenticated"
        >
          <q-btn
            round
            color="primary"
            icon="edit"
            class="absolute-top-right q-ma-md edit-page-button"
          />
        </router-link>

        <div class="gt-xs">
          <label class="av-label-h1" v-if="itemDataDetailsRef.title">{{
            translate(itemDataDetailsRef.title)
          }}</label>
          <div v-else class="q-mt-lg"></div>
        </div>
        <div class="lt-sm">
          <label class="av-label-h2" v-if="itemDataDetailsRef.title">{{
            translate(itemDataDetailsRef.title)
          }}</label>
          <div v-else class="q-mt-lg"></div>
        </div>

        <item-media-carousel
          :show-images="!openModelRef"
          v-if="
            itemDataDetailsRef.images.length > 0 ||
            itemDataDetailsRef.models.length > 0
          "
          :item-details="itemDataDetailsRef"
        />
      </div>
    </div>

    <div
      class="col-12 q-mt-lg row"
      v-if="itemDataDetailsRef && propertiesDataRef"
    >
      <div
        class="col property-category"
        v-for="(category, index) in propertiesDataRef"
        :key="index"
      >
        <artivact-property-category-viewer
          :category="category"
          :properties="itemDataDetailsRef.properties"
        />
      </div>
    </div>
  </ArtivactContent>
</template>

<script setup lang="ts">
import { useQuasar } from 'quasar';
import { api } from 'boot/axios';
import { useRoute } from 'vue-router';
import { onMounted, ref } from 'vue';
import { useUserdataStore } from 'stores/userdata';
import ArtivactContent from 'components/ArtivactContent.vue';
import ItemMediaCarousel from 'components/ItemMediaCarousel.vue';
import { useBreadcrumbsStore } from 'stores/breadcrumbs';
import { translate } from 'components/utils';
import ArtivactPropertyCategoryViewer from 'components/ArtivactPropertyCategoryViewer.vue';

const quasar = useQuasar();
const route = useRoute();

const userdataStore = useUserdataStore();
const breadcrumbsStore = useBreadcrumbsStore();

const itemDataDetailsRef = ref();
const propertiesDataRef = ref();

const openModelRef = ref(false);

function loadData(itemId: string | string[]) {
  api
    .get('/api/item/' + itemId)
    .then((response) => {
      itemDataDetailsRef.value = response.data;
      if (itemDataDetailsRef.value.title) {
        breadcrumbsStore.addBreadcrumb({
          label: translate(itemDataDetailsRef.value.title),
          target: null,
        });
      } else {
        breadcrumbsStore.addBreadcrumb({
          label: itemDataDetailsRef.value.id,
          target: null,
        });
      }
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

function loadPropertiesData() {
  api
    .get('/api/configuration/public/property')
    .then((response) => {
      propertiesDataRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Loading properties failed',
        icon: 'report_problem',
      });
    });
}

onMounted(() => {
  loadData(route.params.itemId);
  loadPropertiesData();
  if (route.query.model === 'true') {
    openModelRef.value = true;
  }
});
</script>

<style scoped>
.property-category {
  min-width: 20rem;
}
</style>
