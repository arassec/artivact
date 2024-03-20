<template>
  <artivact-widget-template
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
          class="flat full-width bg-accent"
        >
          <template v-for="slide in calculateSlides(3)" v-bind:key="slide">
            <q-carousel-slide :name="slide" class="row no-wrap">
              <artivact-item-card
                class="q-ma-sm"
                v-if="searchResultRef.data[slide * 3]"
                :artivact-card-data="searchResultRef.data[slide * 3]"
              />
              <q-space/>
              <artivact-item-card
                class="q-ma-sm"
                v-if="searchResultRef.data[slide * 3 + 1]"
                :artivact-card-data="searchResultRef.data[slide * 3 + 1]"
              />
              <q-space/>
              <artivact-item-card
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
          class="flat full-width bg-accent"
        >
          <template v-for="slide in calculateSlides(1)" v-bind:key="slide">
            <q-carousel-slide :name="slide" class="row no-wrap">
              <div class="row full-width">
                <q-space/>
                <artivact-item-card
                  class="q-ma-sm "
                  v-if="searchResultRef.data[slide]"
                  :artivact-card-data="searchResultRef.data[slide]"
                />
                <q-space/>
              </div>
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
        <artivact-content> No search results available!</artivact-content>
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
          class="flat full-width bg-accent"
        >
          <template v-for="slide in calculateSlides(3)" v-bind:key="slide">
            <q-carousel-slide :name="slide" class="row no-wrap">
              <artivact-item-card
                class="q-ma-sm"
                v-if="searchResultRef.data[slide * 3]"
                :artivact-card-data="searchResultRef.data[slide * 3]"
                :disabled="true"
              />
              <q-space/>
              <artivact-item-card
                class="q-ma-sm"
                v-if="searchResultRef.data[slide * 3 + 1]"
                :artivact-card-data="searchResultRef.data[slide * 3 + 1]"
                :disabled="true"
              />
              <q-space/>
              <artivact-item-card
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
          class="flat full-width bg-accent"
        >
          <template v-for="slide in calculateSlides(1)" v-bind:key="slide">
            <q-carousel-slide :name="slide" class="row no-wrap">
              <artivact-item-card
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
  </artivact-widget-template>
</template>

<script setup lang="ts">
import ArtivactContent from 'components/ArtivactContent.vue';
import {onMounted, PropType, ref, toRef} from 'vue';
import {SearchBasedWidgetData} from 'components/widgets/artivact-widget-models';
import {useQuasar} from 'quasar';
import {SearchResult} from 'components/artivact-models';
import {api} from 'boot/axios';
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
    type: Object as PropType<SearchBasedWidgetData>,
  },
});

const quasar = useQuasar();
const i18n = useI18n();

const widgetDataRef = toRef(props, 'widgetData');

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
        message: i18n.t('ItemCarouselWidget.messages.searchFailed'),
        icon: 'report_problem',
      });
    });
}

function calculateSlides(numPerPage: number): number[] {
  if (searchResultRef.value && searchResultRef.value.data.length > 0) {
    let result = Math.ceil(searchResultRef.value.data.length / numPerPage);
    if (result > 0) {
      console.log('RESULT: ' + result);
      console.log('KEYS: ' + Array.from(Array(result).keys()));
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
