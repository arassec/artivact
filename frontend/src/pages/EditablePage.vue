<template>
  <artivact-page
    v-if="pageContentRef && pageIdRef"
    :page-id="pageIdRef"
    :page-content="pageContentRef"
    :in-edit-mode="inEditModeRef"
    @update-page-content="savePageIfRequired"
    @save-widget-before-upload="saveWidgetBeforeUpload"
    @file-added="fileAdded"
    @file-deleted="fileDeleted"
    @enter-edit-mode="loadPage(pageIdRef, true)"
    @exit-edit-mode="exitEditMode(pageIdRef)"
    @reset-wip="showResetWipDialogRef = true"
    @publish-wip="showPublishWipDialogRef = true"
  />

  <artivact-dialog :dialog-model="showResetWipDialogRef" :warn="true">
    <template v-slot:header>
      {{ $t('EditablePage.dialog.resetWip.heading') }}
    </template>

    <template v-slot:body>
      <q-card-section>
        <div class="q-mb-lg">
          {{ $t('EditablePage.dialog.resetWip.content') }}
        </div>
      </q-card-section>
    </template>

    <template v-slot:cancel>
      <q-btn
        :label="$t('Common.cancel')"
        color="primary"
        @click="showResetWipDialogRef = false"
      />
    </template>

    <template v-slot:approve>
      <q-btn
        data-test="add-widget-modal-approve"
        :label="$t('EditablePage.dialog.resetWip.approve')"
        color="primary"
        @click="resetWip()"
      />
    </template>
  </artivact-dialog>

  <artivact-dialog :dialog-model="showPublishWipDialogRef">
    <template v-slot:header>
      {{ $t('EditablePage.dialog.publishWip.heading') }}
    </template>

    <template v-slot:body>
      <q-card-section>
        <div class="q-mb-lg">
          {{ $t('EditablePage.dialog.publishWip.content') }}
        </div>
      </q-card-section>
    </template>

    <template v-slot:cancel>
      <q-btn
        :label="$t('Common.cancel')"
        color="primary"
        @click="showPublishWipDialogRef = false"
      />
    </template>

    <template v-slot:approve>
      <q-btn
        data-test="add-widget-modal-approve"
        :label="$t('EditablePage.dialog.publishWip.approve')"
        color="primary"
        @click="publishWip(true)"
      />
    </template>
  </artivact-dialog>
</template>

<script setup lang="ts">
import {useMeta, useQuasar} from 'quasar';
import {useRoute} from 'vue-router';
import {api} from '../boot/axios';
import {nextTick, onMounted, ref} from 'vue';
import ArtivactPage from '../components/ArtivactPage.vue';
import {Widget} from '../components/artivact-models';
import {useBreadcrumbsStore} from '../stores/breadcrumbs';
import {useI18n} from 'vue-i18n';
import ArtivactDialog from '../components/ArtivactDialog.vue';
import {useProfilesStore} from '../stores/profiles';
import {useWizzardStore} from "../stores/wizzard";
import {driver} from "driver.js";
import 'driver.js/dist/driver.css';

const quasar = useQuasar();
const route = useRoute();
const i18n = useI18n();

const breadcrumbsStore = useBreadcrumbsStore();
const profilesStore = useProfilesStore();
const wizzardStore = useWizzardStore();

const pageContentRef = ref();
const pageIdRef = ref('');

const inEditModeRef = ref(false);

const showResetWipDialogRef = ref(false);
const showPublishWipDialogRef = ref(false);

const metaTitleRef = ref(null);
const metaDescriptionRef = ref(null);
const metaAuthorRef = ref(null);
const metaKeywordsRef = ref(null);

let originalPageContentJson: string;
let publishedPageContentJson: string;

const driverObj = driver({
  showProgress: true,
  progressText: '{{current}} / {{total}}',
  nextBtnText: i18n.t('Common.next'),
  prevBtnText: i18n.t('Common.previous'),
  doneBtnText: i18n.t('Common.done'),
  steps: [
    {
      element: '[data-test="artivact-menu-bar"]',
      popover: {
        title: i18n.t('EditablePage.tour.menuBarTitle'),
        description: i18n.t('EditablePage.tour.menuBarDescription').replace(/\n/g, '<br/>'),
        side: "bottom",
        align: 'center'
      }
    },
    {
      element: '[data-test="menu-Welcome"]',
      popover: {
        title: i18n.t('EditablePage.tour.menuWelcomeTitle'),
        description: i18n.t('EditablePage.tour.menuWelcomeDescription').replace(/\n/g, '<br/>'),
        side: "bottom",
        align: 'start'
      }
    },
    {
      element: '[data-test="add-menu-button"]',
      popover: {
        title: i18n.t('EditablePage.tour.addMenuTitle'),
        description: i18n.t('EditablePage.tour.addMenuDescription').replace(/\n/g, '<br/>'),
        side: "bottom",
        align: 'start'
      }
    },
    {
      element: '[data-test="item-settings-button"]',
      popover: {
        title: i18n.t('EditablePage.tour.itemSettingsTitle'),
        description: i18n.t('EditablePage.tour.itemSettingsDescription').replace(/\n/g, '<br/>'),
        side: "bottom",
        align: 'start'
      }
    },
    {
      element: '[data-test="system-settings-button"]',
      popover: {
        title: i18n.t('EditablePage.tour.systemSettingsTitle'),
        description: i18n.t('EditablePage.tour.systemSettingsDescription').replace(/\n/g, '<br/>'),
        side: "bottom",
        align: 'start'
      }
    },
    {
      element: '[data-test="edit-page-button"]',
      popover: {
        title: i18n.t('EditablePage.tour.editPageTitle'),
        description: i18n.t('EditablePage.tour.editPageDescription').replace(/\n/g, '<br/>'),
        side: "bottom",
        align: 'start'
      }
    },
    {
      element: '[data-test="button-Prepare 3D Scanning"]',
      popover: {
        title: i18n.t('EditablePage.tour.setupScanningTitle'),
        description: i18n.t('EditablePage.tour.setupScanningDescription').replace(/\n/g, '<br/>'),
        side: "bottom",
        align: 'start'
      }
    },
    {
      element: '[data-test="button-Scan Item"]',
      popover: {
        title: i18n.t('EditablePage.tour.scanItemTitle'),
        description: i18n.t('EditablePage.tour.scanItemDescription').replace(/\n/g, '<br/>'),
        side: "bottom",
        align: 'start'
      }
    },
    {
      element: '[data-test="item-search-widget-results"]',
      popover: {
        title: i18n.t('EditablePage.tour.searchResultsTitle'),
        description: i18n.t('EditablePage.tour.searchResultsDescription').replace(/\n/g, '<br/>'),
        side: "top",
        align: 'center'
      }
    },
    {
      element: '[data-test="documentation-button"]',
      popover: {
        title: i18n.t('EditablePage.tour.documentationTitle'),
        description: i18n.t('EditablePage.tour.documentationDescription').replace(/\n/g, '<br/>'),
        side: "bottom",
        align: 'start'
      }
    },
  ]
});

async function exitEditMode(pageId: string) {
  if (profilesStore.isE2eModeEnabled || profilesStore.isDesktopModeEnabled) {
    let currentPageContentJson = JSON.stringify(pageContentRef.value);
    if (currentPageContentJson !== publishedPageContentJson) {
      await publishWip(false);
    }
  }
  loadPage(pageId, false);
}

async function loadPage(pageId: string | string[], wip: boolean) {
  pageIdRef.value = Array.isArray(pageId) ? pageId[0] : pageId;
  let url = '/api/page';

  if (!pageId) {
    return;
  }

  url += '/' + pageId;

  if (wip) {
    url += '/wip';
  }

  api
    .get(url)
    .then(async (response) => {
      metaTitleRef.value = response.data.metaData.title.translatedValue;
      metaDescriptionRef.value =
        response.data.metaData.description.translatedValue;
      metaAuthorRef.value = response.data.metaData.author;
      metaKeywordsRef.value = response.data.metaData.keywords.translatedValue;

      pageContentRef.value = response.data;
      originalPageContentJson = JSON.stringify(response.data);
      if (!wip) {
        publishedPageContentJson = JSON.stringify(response.data);
      }
      inEditModeRef.value = wip;

      await nextTick();

      if (wizzardStore.startTour) {
        wizzardStore.setStartTour(false);
        driverObj.drive();
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', {
          item: i18n.t('Common.items.page'),
        }),
        icon: 'report_problem',
      });
    });
  // Reset breadcrumbs if required:
  let lastBreadcrumb =
    breadcrumbsStore.breadcrumbs[breadcrumbsStore.breadcrumbs.length - 1];
  if (lastBreadcrumb && lastBreadcrumb.target !== pageId) {
    breadcrumbsStore.removeLastBreadcrumb();
  }
}

function fileAdded(widgetId: string, propertyName: string) {
  loadPage(pageIdRef.value, true);
}

function fileDeleted(widgetId: string, propertyName: string, filename: string) {
  api
    .delete(
      '/api/page/' + pageIdRef.value + '/widget/' + widgetId + '/' + filename,
    )
    .then((response) => {
      let index = -1;
      for (let i = 0; i < pageContentRef.value.widgets.length; i++) {
        if (pageContentRef.value.widgets[i].id === widgetId) {
          index = i;
        }
      }
      response.data.widgets.forEach((widget: Widget) => {
        if (widget.id === widgetId) {
          // eslint-disable-next-line
          pageContentRef.value.widgets[index][propertyName] = (widget as any)[
            propertyName
            ];
        }
      });
      originalPageContentJson = JSON.stringify(pageContentRef.value);
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', {
          item: i18n.t('Common.items.page'),
        }),
        icon: 'report_problem',
      });
    });
}

async function savePageIfRequired({resolve, reject} = {} as any) {
  let currentPageContentJson = JSON.stringify(pageContentRef.value);
  if (currentPageContentJson !== originalPageContentJson) {
    api
      .post('/api/page/' + pageIdRef.value, pageContentRef.value)
      .then((response) => {
        pageContentRef.value = response.data;
        originalPageContentJson = JSON.stringify(response.data);
        if (resolve) {
          resolve();
        }
      })
      .catch(() => {
        quasar.notify({
          color: 'negative',
          position: 'bottom',
          message: i18n.t('Common.messages.saving.failed', {
            item: i18n.t('Common.items.page'),
          }),
          icon: 'report_problem',
        });
        if (reject) {
          reject();
        }
      });
  } else {
    if (resolve) {
      resolve();
    }
  }
}

function resetWip() {
  api
    .post('/api/page/reset-wip/' + pageIdRef.value)
    .then((response) => {
      pageContentRef.value = response.data;
      originalPageContentJson = JSON.stringify(response.data);
      showResetWipDialogRef.value = false;
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('EditablePage.resetWip.success'),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('EditablePage.resetWip.failed'),
        icon: 'report_problem',
      });
    });
}

async function saveWidgetBeforeUpload({resolve, reject}) {
  await savePageIfRequired({resolve, reject});
}

async function publishWip(notifySuccess: boolean) {
  return api
    .post('/api/page/publish-wip/' + pageIdRef.value)
    .then((response) => {
      pageContentRef.value = response.data;
      originalPageContentJson = JSON.stringify(response.data);
      showPublishWipDialogRef.value = false;
      inEditModeRef.value = false;
      if (notifySuccess) {
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: i18n.t('EditablePage.publishWip.success'),
          icon: 'check',
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('EditablePage.publishWip.failed'),
        icon: 'report_problem',
      });
    });
}

useMeta(() => {
  return {
    title: metaTitleRef.value,
    meta: {
      description: {
        name: 'description',
        content: metaDescriptionRef.value,
      },
      author: {
        name: 'author',
        content: metaAuthorRef.value,
      },
      keywords: {
        name: 'keywords',
        content: metaKeywordsRef.value,
      },
    },
  };
});

onMounted(() => {
  loadPage(route.params.pageId, false);
});
</script>

<style scoped>
</style>
