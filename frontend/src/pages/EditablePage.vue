<template>

  <artivact-page
    v-if="pageContentRef && pageIdRef && !editModeRef"
    :page-id="pageIdRef"
    :page-content="pageContentRef"
    v-on:update-page-content="updatePageContent"
    v-on:file-added="fileAdded"
  />

  <artivact-dialog :dialog-model="showUnsavedChangesExistDialog" :warn="true">
    <template v-slot:header>
      {{ $t('EditablePage.dialog.heading') }}
    </template>

    <template v-slot:body>
      <q-card-section>
        {{ $t('EditablePage.dialog.content') }}
      </q-card-section>
    </template>

    <template v-slot:cancel>
      <q-btn :label="$t('Common.cancel')" color="primary" @click="showUnsavedChangesExistDialog = false"/>
    </template>

    <template v-slot:approve>
      <q-btn
        :label="$t('EditablePage.dialog.approve')"
        color="primary"
        @click="proceed"
      />
    </template>
  </artivact-dialog>
</template>

<script setup lang="ts">
import {useQuasar} from 'quasar';
import {onBeforeRouteLeave, onBeforeRouteUpdate, RouteLocationNormalized, useRoute, useRouter} from 'vue-router';
import {api} from 'boot/axios';
import {onMounted, ref} from 'vue';
import ArtivactPage from 'components/ArtivactPage.vue';
import {PageContent, Widget} from 'components/artivact-models';
import {useBreadcrumbsStore} from 'stores/breadcrumbs';
import {useMenuStore} from 'stores/menu';
import {useI18n} from 'vue-i18n';
import ArtivactDialog from 'components/ArtivactDialog.vue';

const quasar = useQuasar();
const route = useRoute();
const router = useRouter();
const i18n = useI18n();

const breadcrumbsStore = useBreadcrumbsStore();
const menuStore = useMenuStore();

const pageContentRef = ref();
const pageIdRef = ref('');

const editModeRef = ref(false);

let originalPageContentJson: string;
let nextRoute: RouteLocationNormalized;
const showUnsavedChangesExistDialog = ref(false);

function loadPage(pageId: string | string[]) {
  pageIdRef.value = Array.isArray(pageId) ? pageId[0] : pageId;
  let url = '/api/page';
  if (pageId) {
    url += '/' + pageId;
  } else {
    pageIdRef.value = 'INDEX';
  }
  api
    .get(url)
    .then((response) => {
      pageContentRef.value = response.data;
      originalPageContentJson = JSON.stringify(response.data);
      // With only one page yet, "index page" should be checked by default:
      let menus = menuStore.availableMenus;
      if (menus.length == 1) {
        let menu = menus[0];
        if (menu?.targetPageId || menu?.menuEntries.length == 1) {
          pageContentRef.value.indexPage = true;
        }
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.page')}),
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

function updatePageContent(pageContent: PageContent) {
  pageContentRef.value = pageContent;
  originalPageContentJson = JSON.stringify(pageContent);
}

function fileAdded(widgetId: string, propertyName: string) {
  api
    .get('/api/page/' + pageIdRef.value)
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
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.page')}),
        icon: 'report_problem',
      });
    });
}

function proceed() {
  showUnsavedChangesExistDialog.value = false;
  originalPageContentJson = JSON.stringify(pageContentRef.value);
  router.push(nextRoute);
}

onBeforeRouteLeave((to) => {
  let currentPageContentJson = JSON.stringify(pageContentRef.value);
  if (currentPageContentJson !== originalPageContentJson) {
    nextRoute = to;
    showUnsavedChangesExistDialog.value = true;
    return false;
  }
})

onBeforeRouteUpdate((to) => {
  let currentPageContentJson = JSON.stringify(pageContentRef.value);
  if (currentPageContentJson !== originalPageContentJson) {
    nextRoute = to;
    showUnsavedChangesExistDialog.value = true;
    return false;
  }
})

onMounted(() => {
  loadPage(route.params.pageId);
});

</script>

<style scoped></style>
