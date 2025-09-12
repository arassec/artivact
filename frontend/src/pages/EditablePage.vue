<template>
  <artivact-page
    v-if="pageContentRef && pageIdRef"
    :page-id="pageIdRef"
    :page-content="pageContentRef"
    :in-edit-mode="inEditModeRef"
    v-on:update-page-content="savePageIfRequired"
    v-on:file-added="fileAdded"
    v-on:file-deleted="fileDeleted"
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
import { useQuasar } from 'quasar';
import { useRoute } from 'vue-router';
import { api } from '../boot/axios';
import { onMounted, ref } from 'vue';
import ArtivactPage from '../components/ArtivactPage.vue';
import { Widget } from '../components/artivact-models';
import { useBreadcrumbsStore } from '../stores/breadcrumbs';
import { useI18n } from 'vue-i18n';
import ArtivactDialog from '../components/ArtivactDialog.vue';
import { useProfilesStore } from '../stores/profiles';

const quasar = useQuasar();
const route = useRoute();
const i18n = useI18n();

const breadcrumbsStore = useBreadcrumbsStore();
const profilesStore = useProfilesStore();

const pageContentRef = ref();
const pageIdRef = ref('');

const inEditModeRef = ref(false);

const showResetWipDialogRef = ref(false);
const showPublishWipDialogRef = ref(false);

let originalPageContentJson: string;
let publishedPageContentJson: string;

async function exitEditMode(pageId: string) {
  if (profilesStore.isE2eModeEnabled || profilesStore.isDesktopModeEnabled) {
    let currentPageContentJson = JSON.stringify(pageContentRef.value);
    if (currentPageContentJson !== publishedPageContentJson) {
      await publishWip(false);
    }
  }
  loadPage(pageId, false);
}

function loadPage(pageId: string | string[], wip: boolean) {
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
    .then((response) => {
      pageContentRef.value = response.data;
      originalPageContentJson = JSON.stringify(response.data);
      if (!wip) {
        publishedPageContentJson = JSON.stringify(response.data);
      }
      inEditModeRef.value = wip;
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
  let url = '/api/page/' + pageIdRef.value;
  if (inEditModeRef.value) {
    url += '/wip';
  }

  api
    .get(url)
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

function savePageIfRequired() {
  let currentPageContentJson = JSON.stringify(pageContentRef.value);
  if (currentPageContentJson !== originalPageContentJson) {
    api
      .post('/api/page/' + pageIdRef.value, pageContentRef.value)
      .then((response) => {
        pageContentRef.value = response.data;
        originalPageContentJson = JSON.stringify(response.data);
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
      });
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

onMounted(() => {
  loadPage(route.params.pageId, false);
});
</script>

<style scoped></style>
