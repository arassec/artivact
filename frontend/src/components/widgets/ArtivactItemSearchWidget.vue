<template>
  <artivact-widget-template
    :move-down-enabled="moveDownEnabled"
    :move-up-enabled="moveUpEnabled"
    :in-edit-mode="inEditMode"
    v-if="widgetDataRef"
    :restrictions="widgetDataRef.restrictions"
    @editor-dialog-closed="search(0)"
  >
    <template v-slot:widget-content>
      <artivact-content>
        <div class="col">
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
        <label
          class="text-red"
          v-if="
            widgetDataRef &&
            widgetDataRef.searchTerm &&
            (!searchResultRef ||
              !searchResultRef.data ||
              searchResultRef.data.length === 0)
          ">
          {{ $t('SearchWidget.label.noSearchResults') }}
        </label>

        <div class="col">
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
              searchResultRef.data.length > 0
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
        <q-input type="number" outlined v-model="widgetDataRef.pageSize" class="q-mb-md full-width"
                 :label="$t('asdf')"/>
        <artivact-item-search-input
          :widget-data="widgetDataRef"
          @refresh-search-results="search(0)"
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

const widgetDataRef = toRef(props, 'widgetData');

const widgetDataStore = useWidgetdataStore();

const searchResultRef = ref({} as SearchResult);
const searchTermRef = ref('');

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
          message: i18n.t('SearchWidget.messages.noSearchResults'),
          icon: 'report_problem',
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('SearchWidget.messages.searchFailed'),
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
