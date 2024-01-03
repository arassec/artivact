<template>
  <artivact-page
    v-if="pageContentRef && pageIdRef && !editModeRef"
    :page-id="pageIdRef"
    :page-content="pageContentRef"
    v-on:update-page-content="updatePageContent"
    v-on:file-added="fileAdded"
  />
</template>

<script setup lang="ts">
import { useQuasar } from 'quasar';
import { useRoute } from 'vue-router';
import { api } from 'boot/axios';
import { onMounted, ref } from 'vue';
import ArtivactPage from 'components/ArtivactPage.vue';
import { PageContent, Widget } from 'components/models';
import { useBreadcrumbsStore } from 'stores/breadcrumbs';
import {useMenuStore} from 'stores/menu';

const quasar = useQuasar();
const route = useRoute();

const breadcrumbsStore = useBreadcrumbsStore();
const menuStore = useMenuStore();

const pageContentRef = ref();
const pageIdRef = ref('');

const editModeRef = ref(false);

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
        message: 'Loading failed',
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
        message: 'Loading failed',
        icon: 'report_problem',
      });
    });
}

onMounted(() => {
  loadPage(route.params.pageId);
});
</script>

<style scoped></style>
