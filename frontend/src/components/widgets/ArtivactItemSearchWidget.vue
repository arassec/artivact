<template>
  <artivact-widget-template
    :move-down-enabled="moveDownEnabled"
    :move-up-enabled="moveUpEnabled"
    :in-edit-mode="inEditMode"
    v-if="widgetDataRef"
    :restrictions="widgetDataRef.restrictions"
    @editor-dialog-closed="search(0)"
    :navigation-title="widgetDataRef.navigationTitle"
  >
    <template v-slot:widget-content>
      <artivact-content>
        <div class="col">
          <div v-if="widgetDataRef && widgetDataRef.heading">
            <h1 class="av-label-h1" v-if="widgetDataRef.heading.translatedValue">
              {{ translate(widgetDataRef.heading) }}
            </h1>
            <div v-if="widgetDataRef.content" v-html="format(translate(widgetDataRef.content))"/>
          </div>
          <div v-if="widgetDataRef && !widgetDataRef.searchTerm">
            <q-input
              v-model="searchTermRef"
              input-class="text-right"
              @keydown.enter="search(0)"
              class="q-mb-lg"
            >
              <template v-slot:append>
                <q-icon v-if="searchTermRef === ''" name="search"/>
                <q-icon
                  v-else
                  name="clear"
                  class="cursor-pointer"
                  @click="searchTermRef = ''; searchResultRef.data.length = 0"
                />
              </template>
            </q-input>
          </div>

          <div
            v-if="
              searchResultRef &&
              searchResultRef.data &&
              searchResultRef.data.length > 0
            "
          >
            <div class="row">
              <artivact-item-card
                :artivact-card-data="resultEntry"
                v-for="(resultEntry, index) in searchResultRef.data"
                v-bind:key="index"
                class="q-ma-md"
              />
            </div>
            <div
              class="flex flex-center"
              v-if="searchResultRef.data.length > 0"
            >
              <q-pagination
                class="lt-sm"
                size="2em"
                v-model="searchResultRef.pageNumber"
                :max="searchResultRef.totalPages"
                input
                @update:model-value="search(searchResultRef.pageNumber - 1)"
              />
              <q-pagination
                class="gt-xs"
                v-model="searchResultRef.pageNumber"
                :max="searchResultRef.totalPages"
                input
                @update:model-value="search(searchResultRef.pageNumber - 1)"
              />
            </div>
          </div>
        </div>
      </artivact-content>
    </template>

    <template v-slot:widget-editor-preview>
      <artivact-content>
        <div class="col">
          <div v-if="widgetDataRef && widgetDataRef.heading">
            <h1 class="av-label-h1" v-if="widgetDataRef.heading.translatedValue">
              {{ translate(widgetDataRef.heading) }}
            </h1>
            <div v-if="widgetDataRef.content" v-html="format(translate(widgetDataRef.content))"/>
          </div>

          <label
            class="text-red"
            v-if="
            widgetDataRef &&
            widgetDataRef.searchTerm &&
            (!searchResultRef ||
              !searchResultRef.data ||
              searchResultRef.data.length === 0)
          ">
            {{ $t('ItemSearchWidget.label.noSearchResults') }}
          </label>

          <div v-if="widgetDataRef && !widgetDataRef.searchTerm">
            <q-input
              v-model="searchTermRef"
              input-class="text-right"
              @keydown.enter="search(0)"
              class="q-mb-lg"
            >
              <template v-slot:append>
                <q-icon v-if="searchTermRef === ''" name="search"/>
                <q-icon
                  v-else
                  name="clear"
                  class="cursor-pointer"
                  @click="searchTermRef = ''"
                />
              </template>
            </q-input>
          </div>

          <div
            v-if="
              searchResultRef &&
              searchResultRef.data &&
              searchResultRef.data.length > 0 &&
              widgetDataRef.searchTerm
            "
          >
            <div class="row">
              <artivact-item-card
                :artivact-card-data="resultEntry"
                v-for="(resultEntry, index) in searchResultRef.data"
                v-bind:key="index"
                class="q-ma-md"
                :disabled="true"
              />
            </div>
            <div
              class="flex flex-center"
              v-if="searchResultRef.data.length > 0"
            >
              <q-pagination
                v-model="searchResultRef.pageNumber"
                :max="searchResultRef.totalPages"
                input
                @update:model-value="search(searchResultRef.pageNumber - 1)"
              />
            </div>
          </div>
        </div>
      </artivact-content>
    </template>

    <template v-slot:widget-editor-content>
      <artivact-content>
        <artivact-restricted-translatable-item-editor
          :locales="localeStore.locales"
          :label="$t('ItemSearchWidget.label.heading')"
          :translatable-string="widgetDataRef.heading"
          :show-separator="false"
          class="full-width"/>
        <artivact-restricted-translatable-item-editor
          :locales="localeStore.locales"
          :label="$t('ItemSearchWidget.label.content')"
          :translatable-string="widgetDataRef.content"
          :show-separator="false"
          :textarea="true"
          class="full-width"/>
        <q-input type="number" outlined v-model="widgetDataRef.pageSize" class="q-mb-md full-width"
                 :label="$t('ItemSearchWidget.label.pageSize')"/>
        <artivact-item-search-input
          :widget-data="widgetDataPreviewRef"
          @refresh-search-results="searchPreview()"
        />
      </artivact-content>
    </template>
  </artivact-widget-template>
</template>

<script setup lang="ts">
import ArtivactContent from 'components/ArtivactContent.vue';
import {onMounted, PropType, ref, toRef} from 'vue';
import {ItemSearchWidget} from 'components/widgets/artivact-widget-models';
import {SearchResult} from 'components/artivact-models';
import {api} from 'boot/axios';
import {useQuasar} from 'quasar';
import {useWidgetdataStore} from 'stores/widgetdata';
import ArtivactItemSearchInput from 'components/widgets/util/ArtivactItemSearchInput.vue';
import {useI18n} from 'vue-i18n';
import ArtivactItemCard from 'components/ArtivactItemCard.vue';
import ArtivactWidgetTemplate from 'components/widgets/ArtivactWidgetTemplate.vue';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';
import {useLocaleStore} from 'stores/locale';
import {translate} from 'components/artivact-utils';
import MarkdownIt from 'markdown-it';

const props = defineProps({
  inEditMode: {
    required: true,
    type: Boolean,
  },
  moveUpEnabled: {
    required: true,
    type: Boolean,
  },
  moveDownEnabled: {
    required: true,
    type: Boolean,
  },
  widgetData: {
    required: true,
    type: Object as PropType<ItemSearchWidget>,
  },
});

const quasar = useQuasar();
const i18n = useI18n();

const localeStore = useLocaleStore();

const widgetDataRef = toRef(props, 'widgetData');
const widgetDataPreviewRef = toRef({
  searchTerm: ''
} as ItemSearchWidget)

const widgetDataStore = useWidgetdataStore();

const searchResultRef = ref({} as SearchResult);
const searchTermRef = ref('');

function format(text: string) {
  if (!text) {
    return;
  }
  let md = new MarkdownIt();
  return md.render(text);
}

function searchPreview() {
  if (widgetDataRef.value !== undefined) {
    widgetDataRef.value.searchTerm = widgetDataPreviewRef.value.searchTerm
    search(0)
  }
}

function search(page: number) {
  widgetDataStore.setPage(widgetDataRef.value?.id, page);
  let searchQuery = searchTermRef.value;
  if (widgetDataRef.value?.searchTerm) {
    searchQuery = widgetDataRef.value?.searchTerm;
  }
  if (!searchQuery) {
    searchResultRef.value = {} as SearchResult;
    return;
  }
  let maxSearchResults = widgetDataRef.value?.maxResults;
  if (maxSearchResults <= 0) {
    maxSearchResults = 100;
  }
  let pageSize = widgetDataRef.value?.pageSize;
  if (pageSize <= 0) {
    pageSize = 9;
  }
  api
    .get(
      '/api/search?query=' +
      searchQuery +
      '&pageNo=' +
      page +
      '&maxResults=' +
      maxSearchResults +
      '&pageSize=' + pageSize
    )
    .then((response) => {
      searchResultRef.value = response.data;
      searchResultRef.value.pageNumber += 1;
      if (searchResultRef.value.data.length === 0 && !widgetDataRef.value?.searchTerm) {
        quasar.notify({
          color: 'warning',
          position: 'bottom',
          message: i18n.t('ItemSearchWidget.messages.noSearchResults'),
          icon: 'report_problem',
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemSearchWidget.messages.searchFailed'),
        icon: 'report_problem',
      });
    });
}

onMounted(() => {
  if (widgetDataRef.value?.searchTerm) {
    search(widgetDataStore.getPage(widgetDataRef.value?.id));
  }
});
</script>

<style scoped></style>
