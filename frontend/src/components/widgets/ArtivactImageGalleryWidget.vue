<template>
  <artivact-widget-template
    :move-down-enabled="moveDownEnabled"
    :move-up-enabled="moveUpEnabled"
    :in-edit-mode="inEditMode"
    v-if="widgetDataRef"
    :restrictions="widgetDataRef.restrictions"
    :navigation-title="widgetDataRef.navigationTitle"
  >

    <!-- WIDGET CONTENT -->
    <template v-slot:widget-content>
      <artivact-content>
        <div class="full-width" :class="getContainerClasses()">
          <div :class="getContentClasses()" data-test="image-gallery-widget-editor-heading">
            <h1 class="av-label-h1" v-if="widgetDataRef.heading">
              {{ translate(widgetDataRef.heading) }}
            </h1>
            <div v-html="translate(widgetDataRef.content)"/>
          </div>
          <div class="q-mt-lg" :class="getCarouselClasses()"
               v-if="widgetDataRef.images && widgetDataRef.images.length > 0">
            <q-carousel
              v-model="slide"
              v-model:fullscreen="fullscreen"
              class="shadow-2 rounded-borders"
              transition-prev="slide-right"
              transition-next="slide-left"
              animated
              arrows
              control-color="secondary"
              :thumbnails="widgetDataRef.images.length > 1"
            >
              <q-carousel-slide
                v-for="filename in widgetDataRef.images"
                draggable="false"
                :name="filename"
                :img-src="'/api/page/widget/' + widgetDataRef.id + '/' + filename + '?imageSize=DETAIL'"
              >
                <q-btn
                  v-if="widgetDataRef.fullscreenAllowed"
                  round
                  dense
                  flat
                  class="absolute-top-right fullscreen-button"
                  color="white"
                  :icon="fullscreen ? 'fullscreen_exit' : 'fullscreen'"
                  @click="fullscreen = !fullscreen"
                />
              </q-carousel-slide>
            </q-carousel>
          </div>
        </div>
      </artivact-content>
    </template>


    <!-- WIDGET EDITOR PREVIEW -->
    <template v-slot:widget-editor-preview>
      <artivact-content>
        <div class="full-width" :class="getContainerClasses()">
          <div :class="getContentClasses()">
            <template v-if="widgetDataRef.heading">
              <h1 class="av-label-h1" v-if="localeStore.selectedLocale === null">
                {{ widgetDataRef.heading.value }}
              </h1>
              <h1
                class="av-label-h1"
                v-if="
                localeStore.selectedLocale !== null &&
                widgetDataRef.heading.translations[localeStore.selectedLocale]
              "
              >
                {{
                  widgetDataRef.heading.translations[localeStore.selectedLocale]
                }}
              </h1>
              <h1
                class="av-label-h1 text-red"
                v-if="
                localeStore.selectedLocale !== null &&
                !widgetDataRef.heading.translations[localeStore.selectedLocale]
              "
              >
                {{ widgetDataRef.heading.value }}
              </h1>
            </template>

            <div
              v-if="localeStore.selectedLocale === null"
              v-html="widgetDataRef.content.value"
            />

            <div
              v-if="
              localeStore.selectedLocale !== null &&
              widgetDataRef.content.translations[localeStore.selectedLocale]
            "
              v-html="widgetDataRef.content.translations[localeStore.selectedLocale]"
            />

            <div
              v-if="
              localeStore.selectedLocale !== null &&
              !widgetDataRef.content.translations[localeStore.selectedLocale]
            "
              class="text-red"
              v-html="widgetDataRef.content.value"
            />
          </div>

          <div class="q-mt-lg" :class="getCarouselClasses()"
               v-if="widgetDataRef.images && widgetDataRef.images.length > 0">
            <q-carousel
              v-model="slide"
              v-model:fullscreen="fullscreen"
              class="shadow-2 rounded-borders"
              transition-prev="slide-right"
              transition-next="slide-left"
              animated
              thumbnails
              arrows
              control-color="secondary"
              :thumbnails="widgetDataRef.images.length > 1"
            >
              <q-carousel-slide
                v-for="filename in widgetDataRef.images"
                draggable="false"
                :name="filename"
                :img-src="'/api/page/widget/' + widgetDataRef.id + '/' + filename + '?imageSize=DETAIL'"
              >
                <q-btn
                  round
                  dense
                  class="absolute-top-left q-ma-sm"
                  color="primary"
                  icon="delete"
                  @click="deleteImage(filename)"
                />
                <q-btn
                  v-if="widgetDataRef.fullscreenAllowed"
                  round
                  dense
                  flat
                  class="absolute-top-right fullscreen-button"
                  color="white"
                  :icon="fullscreen ? 'fullscreen_exit' : 'fullscreen'"
                  @click="fullscreen = !fullscreen"
                />
              </q-carousel-slide>
            </q-carousel>
          </div>
        </div>
      </artivact-content>
    </template>


    <!-- WIDGET EDITOR CONTENT -->
    <template v-slot:widget-editor-content>
      <artivact-content>
        <div class="column full-width">
          <artivact-restricted-translatable-item-editor
            :locales="localeStore.locales"
            :translatable-string="widgetDataRef.heading"
            :label="$t('TextWidget.label.heading')"
            :show-separator="false"
            class="q-mb-sm"
          />
          <artivact-restricted-translatable-item-editor
            :locales="localeStore.locales"
            :translatable-string="widgetDataRef.content"
            :label="$t('TextWidget.label.content')"
            :textarea="true"
            :show-separator="false"
          />
          <q-select
            class="q-mb-md"
            outlined
            v-model="widgetDataRef.textPosition"
            :options="Object.values(ImageGalleryWidgetTextPosition)"
            label="TEXT POSITION"
          />
          <div class="row">
            <q-uploader
              :label="$t('ImageGalleryWidget.label.images')"
              field-name="file"
              :no-thumbnails="true"
              class="q-mb-md col"
              :multiple="true"
              :url="'/api/page/' + pageIdRef + '/widget/' + widgetDataRef.id"
              @uploaded="$emit('image-added', 'images')"
              ref="imageUploader"
            >
            </q-uploader>
            <q-checkbox v-model="widgetDataRef.fullscreenAllowed" class="q-ml-md col full-height"
                        :label="$t('ImageGalleryWidget.label.fullscreenAllowed')">
            </q-checkbox>
          </div>
        </div>
      </artivact-content>
    </template>

  </artivact-widget-template>

</template>

<script setup lang="ts">
import ArtivactWidgetTemplate from './ArtivactWidgetTemplate.vue';
import {ImageGalleryWidgetData, ImageGalleryWidgetTextPosition} from './artivact-widget-models';
import ArtivactContent from '../ArtivactContent.vue';
import {PropType, ref, toRef} from "vue";
import {translate} from "../artivact-utils";
import {useLocaleStore} from "../../stores/locale";
import ArtivactRestrictedTranslatableItemEditor from "../ArtivactRestrictedTranslatableItemEditor.vue";
import {QUploader, useQuasar} from "quasar";

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
  pageId: {
    required: true,
    type: String,
  },
  widgetData: {
    required: true,
    type: Object as PropType<ImageGalleryWidgetData>,
  },
});

const emit = defineEmits<{
  (e: 'image-added', property: string): void;
  (e: 'image-deleted', property: string, filename: string): void;
}>();

const quasar = useQuasar();
const localeStore = useLocaleStore();

const pageIdRef = toRef(props, 'pageId');
const widgetDataRef = toRef(props, 'widgetData');

const fullscreen = ref(false);
const slide = ref(widgetDataRef.value.images[0]);

function deleteImage(filename: string) {
  if (widgetDataRef.value.images.length > 0) {
    emit('image-deleted', ['images', filename]);
    widgetDataRef.value.images.forEach((image) => {
      if (image !== filename) {
        slide.value = image;
      }
    });
  }
}

function getContainerClasses(): string {
  if (quasar.screen.gt.sm) {
    if (widgetDataRef.value.textPosition === ImageGalleryWidgetTextPosition.LEFT) {
      return 'row';
    } else if (widgetDataRef.value.textPosition === ImageGalleryWidgetTextPosition.RIGHT) {
      return 'row reverse';
    }
  }
  // Text-Position 'TOP' or undefined:
  return 'column';
}

function getCarouselClasses(): string {
  if (quasar.screen.gt.sm) {
    if (widgetDataRef.value.textPosition === ImageGalleryWidgetTextPosition.LEFT) {
      return 'col-6';
    } else if (widgetDataRef.value.textPosition === ImageGalleryWidgetTextPosition.RIGHT) {
      return 'col-6';
    }
  }
  // Text-Position 'TOP' or undefined:
  return 'col-grow';
}

function getContentClasses(): string {
  if (quasar.screen.gt.sm) {
    if (widgetDataRef.value.textPosition === ImageGalleryWidgetTextPosition.LEFT) {
      return 'col-5 q-mr-md';
    } else if (widgetDataRef.value.textPosition === ImageGalleryWidgetTextPosition.RIGHT) {
      return 'col-5 q-ml-md';
    }
  }
  // Text-Position 'TOP' or undefined:
  return 'col-grow';
}

</script>


<style scoped>

</style>
