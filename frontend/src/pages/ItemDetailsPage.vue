<template>
  <div v-if="itemDataDetailsRef">

    <div class="col items-center sticky gt-md">
      <div class="absolute-top-left q-ma-md" v-if="userdataStore.authenticated">
        <q-btn
          round
          color="primary"
          icon="delete"
          class="main-nav-button"
          @click="confirmDeleteRef = true">
          <q-tooltip>{{ $t('ItemDetailsPage.button.tooltip.delete') }}</q-tooltip>
        </q-btn>
      </div>

      <div class="absolute-top-right q-ma-md" v-if="userdataStore.authenticated">
        <!-- DOWNLOAD BUTTON -->
        <q-form :action="'/api/item/' + itemDataDetailsRef.id + '/export'" method="get">
          <q-btn
            data-test="download-button"
            round
            color="primary"
            icon="download"
            type="submit"
            class="q-mr-sm main-nav-button">
            <q-tooltip>{{ $t('ItemDetailsPage.button.tooltip.download') }}</q-tooltip>
          </q-btn>
          <!-- SYNC UP BUTTON -->
          <q-btn
            round
            color="primary"
            icon="cloud_upload"
            class="q-mr-sm main-nav-button"
            @click="synchronizeUp()">
            <q-tooltip>{{ $t('ItemDetailsPage.button.tooltip.sync') }}</q-tooltip>
          </q-btn>
          <!-- EDIT ITEM BUTTON -->
          <router-link :to="'/administration/configuration/item/' + itemDataDetailsRef.id">
            <q-btn
              round
              color="primary"
              icon="edit"
              class="main-nav-button">
              <q-tooltip>{{ $t('ItemDetailsPage.button.tooltip.edit') }}</q-tooltip>
            </q-btn>
          </router-link>
        </q-form>
      </div>
    </div>

    <ArtivactContent>

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


        <div class="col q-mt-xl lt-md"/> <!-- Space on mobile resolution -->

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
            <template v-if="tag.url && profilesStore.isServerModeEnabled">
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
        <artivact-item-media-carousel :show-images="!openModelRef"
                                      v-if=" itemDataDetailsRef.images.length > 0 ||
            itemDataDetailsRef.models.length > 0"
                                      :item-details="itemDataDetailsRef"
        />
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

      <!-- LONG-RUNNING OPERATION -->
      <artivact-operation-in-progress-dialog :progress-monitor-ref="progressMonitorRef"
                                             :dialog-model="showOperationInProgressModalRef"
                                             @close-dialog="showOperationInProgressModalRef = false"/>


      <!-- DELETE CONFIRMATION -->
      <artivact-dialog :dialog-model="confirmDeleteRef" :warn="true">
        <template v-slot:header>
          {{ $t('ItemDetailsPage.dialog.delete.heading') }}
        </template>

        <template v-slot:body>
          <q-card-section>
            {{ $t('ItemDetailsPage.dialog.delete.description') }}
          </q-card-section>
        </template>

        <template v-slot:cancel>
          <q-btn :label="$t('Common.cancel')" color="primary" @click="confirmDeleteRef = false"/>
        </template>

        <template v-slot:approve>
          <q-btn
            :label="$t('ItemDetailsPage.dialog.delete.button')"
            color="primary"
            @click="deleteItem"
          />
        </template>
      </artivact-dialog>

    </ArtivactContent>
  </div>

</template>

<script setup lang="ts">
import { useQuasar } from 'quasar';
import { api } from 'boot/axios';
import { useRoute, useRouter } from 'vue-router';
import { onMounted, ref } from 'vue';
import { useUserdataStore } from 'stores/userdata';
import ArtivactContent from 'components/ArtivactContent.vue';
import { useBreadcrumbsStore } from 'stores/breadcrumbs';
import { translate } from 'components/artivact-utils';
import ArtivactPropertyCategoryViewer from 'components/ArtivactPropertyCategoryViewer.vue';
import ArtivactOperationInProgressDialog from 'components/ArtivactOperationInProgressDialog.vue';
import { OperationProgress } from 'components/artivact-models';
import ArtivactDialog from 'components/ArtivactDialog.vue';
import { useI18n } from 'vue-i18n';
import ArtivactItemMediaCarousel from 'components/ArtivactItemMediaCarousel.vue';
import { useProfilesStore } from 'stores/profiles';

const quasar = useQuasar();
const route = useRoute();
const router = useRouter();
const i18n = useI18n();

const userdataStore = useUserdataStore();
const breadcrumbsStore = useBreadcrumbsStore();
const profilesStore = useProfilesStore();

const itemDataDetailsRef = ref();
const propertiesDataRef = ref();

const openModelRef = ref(false);

const progressMonitorRef = ref<OperationProgress>();
const showOperationInProgressModalRef = ref(false);

const confirmDeleteRef = ref(false);

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
        message: i18n.t('Common.messages.loading.failed', {item: i18n.t('Common.items.item')}),
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
        message: i18n.t('Common.messages.loading.failed', {item: i18n.t('Common.items.properties')}),
        icon: 'report_problem',
      });
    });
}

function synchronizeUp() {
  api
    .post('/api/export/remote/item/' + itemDataDetailsRef.value.id)
    .then((response) => {
      showOperationInProgressModalRef.value = true;
      progressMonitorRef.value = response.data;
      updateOperationProgress();
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemDetailsPage.messages.sync.failed'),
        icon: 'report_problem',
      });
    });
}

function updateOperationProgress() {
  api
    .get('/api/export/progress')
    .then((response) => {
      if (response.data) {
        progressMonitorRef.value = response.data;
        setTimeout(() => updateOperationProgress(), 1000);
      } else {
        progressMonitorRef.value = undefined;
        showOperationInProgressModalRef.value = false;
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: i18n.t('ItemDetailsPage.messages.sync.success'),
          icon: 'check',
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemDetailsPage.messages.sync.failed'),
        icon: 'report_problem',
      });
    });
}

function deleteItem() {
  let item = itemDataDetailsRef.value;
  confirmDeleteRef.value = false;
  api
    .delete('/api/item/' + item.id)
    .then(() => {
      breadcrumbsStore.removeLastBreadcrumb();
      router.push('/');
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.deleting.success', {item: i18n.t('Common.items.item')}),
        icon: 'done',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.deleting.failed', {item: i18n.t('Common.items.item')}),
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
.main-nav-button {
  z-index: 2;
}

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

.sticky {
  position: sticky;
  top: 3.5em;
  z-index: 2;
}

</style>
