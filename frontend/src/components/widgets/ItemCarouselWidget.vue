<template>
  <widget-template
    :move-down-enabled="moveDownEnabled"
    :move-up-enabled="moveUpEnabled"
    :in-edit-mode="inEditMode"
    v-if="widgetDataRef"
    :restrictions="widgetDataRef.restrictions"
    @editor-dialog-closed="search()"
  >
    <template v-slot:widget-content>
      <artivact-content
        v-if="
          searchResultRef &&
          searchResultRef.data &&
          searchResultRef.data.length > 0
        "
        class="gt-sm"
      >
        <q-carousel
          v-model="slideRef"
          transition-prev="slide-right"
          transition-next="slide-left"
          swipeable
          animated
          control-color="primary"
          navigation
          height="300px"
          class="shadow-2 rounded-borders full-width"
        >
          <template v-for="slide in calculateSlides(3)" v-bind:key="slide">
            <q-carousel-slide :name="slide" class="row no-wrap">
              <item-card
                class="q-ma-sm"
                v-if="searchResultRef.data[slide * 3]"
                :artivact-card-data="searchResultRef.data[slide * 3]"
              />
              <q-space />
              <item-card
                class="q-ma-sm"
                v-if="searchResultRef.data[slide * 3 + 1]"
                :artivact-card-data="searchResultRef.data[slide * 3 + 1]"
              />
              <q-space />
              <item-card
                class="q-ma-sm"
                v-if="searchResultRef.data[slide * 3 + 2]"
                :artivact-card-data="searchResultRef.data[slide * 3 + 2]"
              />
            </q-carousel-slide>
          </template>
        </q-carousel>
      </artivact-content>

      <artivact-content
        v-if="
          searchResultRef &&
          searchResultRef.data &&
          searchResultRef.data.length > 0
        "
        class="lt-md"
      >
        <q-carousel
          v-model="slideRef"
          transition-prev="slide-right"
          transition-next="slide-left"
          swipeable
          animated
          control-color="primary"
          navigation
          height="300px"
          class="shadow-2 rounded-borders full-width"
        >
          <template v-for="slide in calculateSlides(1)" v-bind:key="slide">
            <q-carousel-slide :name="slide" class="row no-wrap">
              <item-card
                class="q-ma-sm"
                v-if="searchResultRef.data[slide]"
                :artivact-card-data="searchResultRef.data[slide]"
              />
            </q-carousel-slide>
          </template>
        </q-carousel>
      </artivact-content>
    </template>

    <template v-slot:widget-editor-preview>
      <label
        class="text-red"
        v-if="
          !searchResultRef ||
          !searchResultRef.data ||
          searchResultRef.data.length === 0
        "
      >
        <artivact-content> No search results available! </artivact-content>
      </label>
      <artivact-content
        v-if="
          searchResultRef &&
          searchResultRef.data &&
          searchResultRef.data.length > 0
        "
        class="gt-sm"
      >
        <q-carousel
          v-model="slideRef"
          transition-prev="slide-right"
          transition-next="slide-left"
          swipeable
          animated
          control-color="primary"
          navigation
          height="300px"
          class="shadow-2 rounded-borders full-width"
        >
          <template v-for="slide in calculateSlides(3)" v-bind:key="slide">
            <q-carousel-slide :name="slide" class="row no-wrap">
              <item-card
                class="q-ma-sm"
                v-if="searchResultRef.data[slide * 3]"
                :artivact-card-data="searchResultRef.data[slide * 3]"
                :disabled="true"
              />
              <q-space />
              <item-card
                class="q-ma-sm"
                v-if="searchResultRef.data[slide * 3 + 1]"
                :artivact-card-data="searchResultRef.data[slide * 3 + 1]"
                :disabled="true"
              />
              <q-space />
              <item-card
                class="q-ma-sm"
                v-if="searchResultRef.data[slide * 3 + 2]"
                :artivact-card-data="searchResultRef.data[slide * 3 + 2]"
                :disabled="true"
              />
            </q-carousel-slide>
          </template>
        </q-carousel>
      </artivact-content>

      <artivact-content
        v-if="
          searchResultRef &&
          searchResultRef.data &&
          searchResultRef.data.length > 0
        "
        class="lt-md"
      >
        <q-carousel
          v-model="slideRef"
          transition-prev="slide-right"
          transition-next="slide-left"
          swipeable
          animated
          control-color="primary"
          navigation
          height="300px"
          class="shadow-2 rounded-borders full-width"
        >
          <template v-for="slide in calculateSlides(1)" v-bind:key="slide">
            <q-carousel-slide :name="slide" class="row no-wrap">
              <item-card
                class="q-ma-sm"
                v-if="searchResultRef.data[slide]"
                :artivact-card-data="searchResultRef.data[slide]"
                :disabled="true"
              />
            </q-carousel-slide>
          </template>
        </q-carousel>
      </artivact-content>
    </template>

    <template v-slot:widget-editor-content>
      <artivact-content>
        <div class="column full-width">
          <artivact-item-search-input
            :widget-data="widgetDataRef"
            :search-term-required="true"
            @refresh-search-results="search()"
          />
        </div>
      </artivact-content>
    </template>
  </widget-template>
</template>

<script setup lang="ts">
import ArtivactContent from 'components/ArtivactContent.vue';
import { onMounted, PropType, ref, toRef } from 'vue';
import { SearchBasedWidgetData } from 'components/widgets/widget-models';
import { useQuasar } from 'quasar';
import { SearchResult } from 'components/models';
import { api } from 'boot/axios';
import ItemCard from 'components/ItemCard.vue';
import WidgetTemplate from 'components/widgets/WidgetTemplate.vue';
import ArtivactItemSearchInput from 'components/widgets/util/ArtivactItemSearchInput.vue';

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
    type: Object as PropType<SearchBasedWidgetData>,
  },
});

const widgetDataRef = toRef(props, 'widgetData');

const quasar = useQuasar();

const slideRef = ref(0);
const searchResultRef = ref({} as SearchResult);

function search() {
  if (!widgetDataRef.value?.searchTerm) {
    return;
  }
  api
    .get(
      '/api/search?query=' +
        widgetDataRef.value?.searchTerm +
        '&pageNo=0&pageSize=' +
        widgetDataRef.value?.maxResults +
        '&maxResults=' +
        widgetDataRef.value?.maxResults
    )
    .then((response) => {
      searchResultRef.value = response.data;
      searchResultRef.value.pageNumber += 1;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Search failed',
        icon: 'report_problem',
      });
    });
}

function calculateSlides(numPerPage: number): number[] {
  if (searchResultRef.value && searchResultRef.value.data.length > 0) {
    let result = Math.floor(searchResultRef.value.data.length / numPerPage);
    if (result > 0) {
      return Array.from(Array(result).keys());
    }
  }
  return [0];
}

onMounted(() => {
  search();
});
</script>

<style scoped></style>
