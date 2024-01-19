<template>
  <ArtivactContent v-if="itemDataDetailsRef">

    <!-- BREADCRUMBS -->
    <div v-if="breadcrumbsStore.breadcrumbs.length > 1" class="q-mb-md">
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
          <q-breadcrumbs-el :label="breadcrumb.label" v-else/>
        </template>
      </q-breadcrumbs>
    </div>

    <div class="col-12">
      <div class="col items-center">

        <!-- EDIT ITEM BUTTON -->
        <router-link
          :to="'/administration/configuration/item/' + itemDataDetailsRef.id"
          v-if="userdataStore.authenticated">
          <q-btn
            round
            color="primary"
            icon="edit"
            class="absolute-top-right q-ma-md edit-page-button"
          />
        </router-link>

        <!-- TITLE -->
        <div v-if="itemDataDetailsRef.title.translatedValue" class="q-mb-sm">
          <div class="gt-xs">
            <label class="title">{{
                translate(itemDataDetailsRef.title)
              }}</label>
          </div>
          <div class="lt-sm">
            <label class="title-small">{{
                translate(itemDataDetailsRef.title)
              }}</label>
          </div>
          <q-separator/>
        </div>

        <!-- TAGS -->
        <div class="q-mb-md" v-if="itemDataDetailsRef.tags">
          <q-badge
            class="q-mr-xs vertical-middle"
            color="secondary"
            v-for="(tag, index) in itemDataDetailsRef.tags"
            :key="index"
          >
            <template v-if="tag.url && !desktopStore.isDesktopModeEnabled">
              <a :href="tag.url" class="tag-link">{{
                  tag.translatedValue
                }}</a>
            </template>
            <template v-else>
              {{ tag.translatedValue }}
            </template>
          </q-badge>
        </div>

        <!-- DESCRIPTION -->
        <div class="q-mb-md" v-if="itemDataDetailsRef.description.translatedValue">
          <label>
            {{ itemDataDetailsRef.description.translatedValue }}
          </label>
        </div>

        <!-- MEDIA-CAROUSEL -->
        <item-media-carousel :show-images="!openModelRef"
          v-if=" itemDataDetailsRef.images.length > 0 ||
            itemDataDetailsRef.models.length > 0"
          :item-details="itemDataDetailsRef"
        />
      </div>
    </div>

    <!-- PROPERTIES -->
    <div v-if="itemDataDetailsRef && propertiesDataRef" class="col-12 q-mt-lg row">
      <div v-for="(category, index) in propertiesDataRef" :key="index" class="col-6 property-category">
        <artivact-property-category-viewer
          :margin-right="(index % 2) == 0"
          :category="category"
          :properties="itemDataDetailsRef.properties"
        />
      </div>
    </div>
  </ArtivactContent>
</template>

<script setup lang="ts">
import {useQuasar} from 'quasar';
import {api} from 'boot/axios';
import {useRoute} from 'vue-router';
import {onMounted, ref} from 'vue';
import {useUserdataStore} from 'stores/userdata';
import ArtivactContent from 'components/ArtivactContent.vue';
import ItemMediaCarousel from 'components/ItemMediaCarousel.vue';
import {useBreadcrumbsStore} from 'stores/breadcrumbs';
import {translate} from 'components/utils';
import ArtivactPropertyCategoryViewer from 'components/ArtivactPropertyCategoryViewer.vue';
import {useDesktopStore} from 'stores/desktop';

const quasar = useQuasar();
const route = useRoute();

const userdataStore = useUserdataStore();
const breadcrumbsStore = useBreadcrumbsStore();
const desktopStore = useDesktopStore();

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

.title {
  font-size: 3rem;
  font-weight: 300;
  line-height: 4rem;
  margin: 0;
  vertical-align: middle;
}

.title-small {
  font-size: 1.5rem;
  font-weight: 300;
  line-height: 2rem;
  letter-spacing: -0.00833em;
  margin: 0;
  vertical-align: middle;
}

.tag-link {
  text-decoration: none;
  color: white;
}
</style>
