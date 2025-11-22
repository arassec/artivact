<template>
  <artivact-widget-template
    v-if="widgetDataRef"
    :editing="editingRef"
    :in-edit-mode="inEditMode"
    :restrictions="widgetDataRef.restrictions"
    :navigation-title="widgetDataRef.navigationTitle"
    @start-editing="editingRef = true"
    @stop-editing="editingRef = false"
  >
    <!-- WIDGET CONTENT -->
    <template v-slot:widget-content>
      <div class="full-width" :class="getContainerClasses()">
        <div
          :class="getContentClasses()"
          data-test="image-gallery-widget-editor-heading"
        >
          <h1 class="av-label-h1" v-if="widgetDataRef.heading">
            {{ translate(widgetDataRef.heading) }}
          </h1>
          <div v-html="format(translate(widgetDataRef.content))"/>
        </div>
        <div
          :class="getCarouselClasses()"
          v-if="widgetDataRef.images && widgetDataRef.images.length > 0"
        >
          <q-carousel
            v-model="slide"
            v-model:fullscreen="fullscreen"
            :class="!widgetDataRef.hideBorder ? 'shadow-1 rounded-borders' : ''"
            transition-prev="slide-right"
            transition-next="slide-left"
            :height="widgetDataRef.iconMode && !fullscreen ? '200px' : '500px'"
            :style="
              widgetDataRef.iconMode && !fullscreen ? 'width: 200px;' : ''
            "
            control-color="primary"
            control-type="regular"
            control-text-color="white"
            swipeable
            animated
            padding
            arrows
            :navigation="widgetDataRef.images.length > 1"
            infinite
            @click.stop
          >
            <q-carousel-slide
              style="padding: 0"
              v-for="(filename, index) in widgetDataRef.images"
              draggable="false"
              :name="index"
              dense
              arrows
              :class="widgetDataRef.iconMode ? 'av-icon' : ''"
            >
              <q-img
                v-if="!fullscreen"
                class="fit"
                :src="
                  '/api/page/widget/' +
                  widgetDataRef.id +
                  '/' +
                  filename +
                  (inEditMode ? '/wip' : '') +
                  '?imageSize=DETAIL'
                "
                fit="cover"
              />
              <q-img
                class="fit"
                v-if="fullscreen"
                :src="
                  '/api/page/widget/' +
                  widgetDataRef.id +
                  '/' +
                  filename +
                  (inEditMode ? '/wip' : '')
                "
                fit="scale-down"
              />
              <q-btn
                v-if="widgetDataRef.fullscreenAllowed"
                round
                dense
                class="absolute-top-right fullscreen-button q-ma-md"
                color="primary"
                :icon="fullscreen ? 'fullscreen_exit' : 'fullscreen'"
                @click="fullscreen = !fullscreen"
              />
            </q-carousel-slide>
          </q-carousel>
        </div>
      </div>
    </template>

    <template v-slot:widget-editor>
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
        <div class="q-gutter-sm q-mb-md">
          <q-radio
            v-model="widgetDataRef.textPosition"
            val="TOP"
            :label="$t('ImageGalleryWidget.label.textPositionTop')"
          ></q-radio>
          <q-radio
            v-model="widgetDataRef.textPosition"
            val="LEFT"
            :label="$t('ImageGalleryWidget.label.textPositionLeft')"
          ></q-radio>
          <q-radio
            v-model="widgetDataRef.textPosition"
            val="RIGHT"
            :label="$t('ImageGalleryWidget.label.textPositionRight')"
          ></q-radio>
        </div>
        <div class="row">
          <q-uploader
            :label="$t('ImageGalleryWidget.label.images')"
            :auto-upload="true"
            :multiple="true"
            :max-concurrent-uploads="1"
            field-name="file"
            :no-thumbnails="true"
            class="q-mb-md col"
            :url="'/api/page/' + pageIdRef + '/widget/' + widgetDataRef.id"
            @uploaded="$emit('image-added', 'images')"
            @start="saveWidgetBeforeUpload"
            ref="imageUploader"
          >
          </q-uploader>
          <div class="column">
            <q-checkbox
              v-model="widgetDataRef.fullscreenAllowed"
              class="q-ml-md col full-height"
              :label="$t('ImageGalleryWidget.label.fullscreenAllowed')"
            >
            </q-checkbox>
            <q-checkbox
              v-model="widgetDataRef.iconMode"
              class="q-ml-md col full-height"
              :label="$t('ImageGalleryWidget.label.iconMode')"
            >
            </q-checkbox>
            <q-checkbox
              v-model="widgetDataRef.hideBorder"
              class="q-ml-md col full-height"
              :label="$t('ImageGalleryWidget.label.hideBorder')"
            >
            </q-checkbox>
          </div>
        </div>

        <q-separator class="full-width q-mt-md"/>

        <div class="image-preview-container">
          <Draggable
            v-model="widgetDataRef.images"
            item-key="index"
            group="widget-image-preview"
          >
            <template #item="{ element }">
              <div class="image-preview">
                <q-btn
                  round
                  dense
                  class="q-ma-sm delete-image-button"
                  color="primary"
                  icon="delete"
                  @click="deleteImage(element)"
                />
                <q-img
                  group="widget-image-preview"
                  :src="
                    '/api/page/widget/' +
                    widgetDataRef.id +
                    '/' +
                    element +
                    (inEditMode ? '/wip' : '') +
                    '?imageSize=ITEM_CARD'
                  "
                ></q-img>
              </div>
            </template>
          </Draggable>
        </div>
      </div>
    </template>
  </artivact-widget-template>
</template>

<script setup lang="ts">
import ArtivactWidgetTemplate from './ArtivactWidgetTemplate.vue';
import {ImageGalleryWidgetData, ImageGalleryWidgetTextPosition,} from './artivact-widget-models';
import {PropType, ref, toRef} from 'vue';
import {translate} from '../artivact-utils';
import {useLocaleStore} from '../../stores/locale';
import ArtivactRestrictedTranslatableItemEditor from '../ArtivactRestrictedTranslatableItemEditor.vue';
import {QUploader, useQuasar} from 'quasar';
import MarkdownIt from 'markdown-it';
import Draggable from 'vuedraggable';

const props = defineProps({
  inEditMode: {
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
  (e: 'image-deleted', parameters: string[]): void;
  (e: 'save-widget-before-upload', payload: { resolve; reject; }): void;
}>();

const quasar = useQuasar();
const localeStore = useLocaleStore();

const pageIdRef = toRef(props, 'pageId');
const widgetDataRef = toRef(props, 'widgetData');

const fullscreen = ref(false);
const slide = ref(0);

const editingRef = ref(false);

function deleteImage(filename: string) {
  emit('image-deleted', ['images', filename]);
  if (widgetDataRef.value.images.length > 0) {
    slide.value = 0;
  }
}

function getContainerClasses(): string {
  if (quasar.screen.gt.sm) {
    if (
      widgetDataRef.value.textPosition === ImageGalleryWidgetTextPosition.LEFT
    ) {
      return 'row';
    } else if (
      widgetDataRef.value.textPosition === ImageGalleryWidgetTextPosition.RIGHT
    ) {
      return 'row reverse';
    }
  }
  // Text-Position 'TOP' or undefined:
  return 'column';
}

function getCarouselClasses(): string {
  let classes = '';
  if (widgetDataRef.value.iconMode) {
    classes = 'row justify-center';
  }
  if (quasar.screen.gt.sm) {
    if (
      widgetDataRef.value.textPosition === ImageGalleryWidgetTextPosition.LEFT
    ) {
      classes += ' q-pa-md q-mt-md';
    } else if (
      widgetDataRef.value.textPosition === ImageGalleryWidgetTextPosition.RIGHT
    ) {
      classes += ' q-pa-md q-mt-md';
    }
    if (widgetDataRef.value.iconMode) {
      classes += ' col-3';
    } else {
      classes += ' col-6';
    }
    return classes;
  }
  // Text-Position 'TOP' or undefined:
  classes += ' col-grow';
  return classes;
}

function getContentClasses(): string {
  if (
    quasar.screen.gt.sm &&
    widgetDataRef.value.textPosition !== ImageGalleryWidgetTextPosition.TOP
  ) {
    if (widgetDataRef.value.iconMode) {
      return 'col-9';
    } else {
      return 'col-6';
    }
  }
  // Text-Position 'TOP' or undefined:
  return 'col-grow';
}

function format(text: string) {
  if (!text) {
    return;
  }
  let md = new MarkdownIt();
  return md.render(text);
}

function saveWidgetBeforeUpload() {
  return new Promise((resolve, reject) => {
    emit('save-widget-before-upload', {resolve, reject})
  });
}

</script>

<style scoped>
.image-preview {
  width: 240px;
}

.image-preview:hover {
  cursor: move;
}

.image-preview-container > div {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.delete-image-button {
  position: relative;
  top: 50px;
  z-index: 2;
}

.q-carousel:hover {
  cursor: pointer;
}
</style>
