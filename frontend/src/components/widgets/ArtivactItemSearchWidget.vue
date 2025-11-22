<template>
  <artivact-widget-template
    v-if="widgetDataRef"
    :editing="editingRef"
    :in-edit-mode="inEditMode"
    :restrictions="widgetDataRef.restrictions"
    :navigation-title="widgetDataRef.navigationTitle"
    @start-editing="editingRef = true"
    @stop-editing="
      editingRef = false;
      search(0);
    "
  >
    <template v-slot:widget-content>
      <template v-if="!expertMode">
        <div class="full-width">
          <div v-if="widgetDataRef && widgetDataRef.heading" class="full-width">
            <h1
              class="av-label-h1"
              v-if="widgetDataRef.heading.translatedValue"
            >
              {{ translate(widgetDataRef.heading) }}
            </h1>
            <div
              v-if="widgetDataRef.content"
              v-html="format(translate(widgetDataRef.content))"
            />
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
                  @click="
                    searchTermRef = '';
                    searchResultRef.data.length = 0;
                  "
                />
              </template>
            </q-input>
          </div>
        </div>
      </template>
      <template v-else>
        <artivact-item-search-input
          :widget-data="widgetDataRef"
          @refresh-search-results="search(0)"
        />
      </template>

      <div
        class="full-width"
        v-if="
          searchResultRef &&
          searchResultRef.data &&
          searchResultRef.data.length > 0
        "
        data-test="item-search-widget-results">
        <div class="row">
          <artivact-item-card
            :widget-id="widgetData.id"
            :artivact-card-data="resultEntry"
            :disabled="inEditMode"
            v-for="(resultEntry, index) in searchResultRef.data"
            v-bind:key="index"
            class="q-ma-md"
          />
        </div>
        <div class="flex flex-center" v-if="searchResultRef.data.length > 0">
          <q-pagination
            v-if="searchResultRef.totalPages > 1"
            class="lt-sm"
            size="2em"
            v-model="searchResultRef.pageNumber"
            :max="searchResultRef.totalPages"
            input
            @update:model-value="search(searchResultRef.pageNumber - 1)"
          />
          <q-pagination
            v-if="searchResultRef.totalPages > 1"
            class="gt-xs"
            v-model="searchResultRef.pageNumber"
            :max="searchResultRef.totalPages"
            input
            @update:model-value="search(searchResultRef.pageNumber - 1)"
          />
        </div>
      </div>
    </template>

    <template v-slot:widget-editor>
      <artivact-restricted-translatable-item-editor
        :locales="localeStore.locales"
        :label="$t('ItemSearchWidget.label.heading')"
        :translatable-string="widgetDataRef.heading"
        :show-separator="false"
        class="full-width"
      />
      <artivact-restricted-translatable-item-editor
        :locales="localeStore.locales"
        :label="$t('ItemSearchWidget.label.content')"
        :translatable-string="widgetDataRef.content"
        :show-separator="false"
        :textarea="true"
        class="full-width"
      />
      <q-input
        type="number"
        outlined
        v-model="widgetDataRef.pageSize"
        class="q-mb-md full-width"
        :label="$t('ItemSearchWidget.label.pageSize')"
      />
      <artivact-item-search-input
        :widget-data="widgetDataPreviewRef"
        @refresh-search-results="searchPreview()"
      />

      <q-separator/>

      <div
        class="full-width"
        v-if="
          searchResultRef &&
          searchResultRef.data &&
          searchResultRef.data.length > 0
        "
      >
        <div class="row">
          <artivact-item-card
            :widget-id="widgetData.id"
            :artivact-card-data="resultEntry"
            :disabled="inEditMode"
            v-for="(resultEntry, index) in searchResultRef.data"
            v-bind:key="index"
            class="q-ma-md"
          />
        </div>
        <div class="flex flex-center" v-if="searchResultRef.data.length > 0">
          <q-pagination
            v-if="searchResultRef.totalPages > 1"
            class="lt-sm"
            size="2em"
            v-model="searchResultRef.pageNumber"
            :max="searchResultRef.totalPages"
            input
            @update:model-value="search(searchResultRef.pageNumber - 1)"
            @click.stop
          />
          <q-pagination
            v-if="searchResultRef.totalPages > 1"
            class="gt-xs"
            v-model="searchResultRef.pageNumber"
            :max="searchResultRef.totalPages"
            input
            @update:model-value="search(searchResultRef.pageNumber - 1)"
            @click.stop
          />
        </div>
      </div>
    </template>
  </artivact-widget-template>
</template>

<script setup lang="ts">
import {onMounted, PropType, ref, toRef} from 'vue';
import {ItemSearchWidget} from './artivact-widget-models';
import {SearchResult} from '../artivact-models';
import {api} from '../../boot/axios';
import {useQuasar} from 'quasar';
import {useWidgetdataStore} from '../../stores/widgetdata';
import ArtivactItemSearchInput from '../../components/widgets/util/ArtivactItemSearchInput.vue';
import {useI18n} from 'vue-i18n';
import ArtivactItemCard from '../../components/ArtivactItemCard.vue';
import ArtivactWidgetTemplate from '../../components/widgets/ArtivactWidgetTemplate.vue';
import ArtivactRestrictedTranslatableItemEditor from '../../components/ArtivactRestrictedTranslatableItemEditor.vue';
import {useLocaleStore} from '../../stores/locale';
import {translate} from '../artivact-utils';
import MarkdownIt from 'markdown-it';

const props = defineProps({
  inEditMode: {
    required: true,
    type: Boolean,
  },
  widgetData: {
    required: true,
    type: Object as PropType<ItemSearchWidget>,
  },
  expertMode: {
    required: false,
    type: Boolean,
    default: false,
  },
});

const editingRef = ref(false);

const quasar = useQuasar();
const i18n = useI18n();

const localeStore = useLocaleStore();

const widgetDataRef = toRef(props, 'widgetData');
const widgetDataPreviewRef = toRef({
  searchTerm: '',
} as ItemSearchWidget);

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
    widgetDataRef.value.maxResults = widgetDataPreviewRef.value.maxResults;
    widgetDataRef.value.searchTerm = widgetDataPreviewRef.value.searchTerm;
    search(0);
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
      '&pageSize=' +
      pageSize,
    )
    .then((response) => {
      searchResultRef.value = response.data;
      searchResultRef.value.pageNumber += 1;
      if (
        searchResultRef.value.data.length === 0 &&
        !widgetDataRef.value?.searchTerm
      ) {
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
  if (widgetDataRef.value?.maxResults) {
    widgetDataPreviewRef.value.maxResults = widgetDataRef.value?.maxResults;
  }
  if (widgetDataRef.value?.searchTerm) {
    widgetDataPreviewRef.value.searchTerm = widgetDataRef.value.searchTerm;
    search(widgetDataStore.getPage(widgetDataRef.value?.id));
  }
});
</script>

<style scoped></style>
