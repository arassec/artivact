<template>
  <widget-template
    :move-down-enabled="moveDownEnabled"
    :move-up-enabled="moveUpEnabled"
    :in-edit-mode="inEditMode"
    v-if="widgetDataRef"
    :restrictions="widgetDataRef.restrictions"
  >
    <template v-slot:widget-content>
      <div class="gt-sm">
        <artivact-content>
          <div class="row full-width">
            <div class="col q-mr-lg">
              <q-carousel
                v-model="slide"
                v-model:fullscreen="fullscreen"
                class="shadow-2 rounded-borders"
              >
                <q-carousel-slide
                  draggable="false"
                  name="image"
                  :img-src="
                    '/api/page/widget/' +
                    widgetDataRef.id +
                    '/' +
                    widgetDataRef.image +
                    '?imageSize=CAROUSEL'
                  "
                >
                  <q-btn
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
            <div
              v-if="widgetDataRef.text"
              class="col"
              v-html="format(translate(widgetDataRef.text))"
            />
          </div>
        </artivact-content>
      </div>
      <div class="lt-md">
        <artivact-content>
          <q-img
            v-if="widgetDataRef.image"
            class="q-mb-md"
            :src="
              '/api/page/widget/' +
              widgetDataRef.id +
              '/' +
              widgetDataRef.image +
              '?imageSize=CAROUSEL'
            "
          >
          </q-img>
          <div
            v-if="widgetDataRef.text"
            v-html="format(translate(widgetDataRef.text))"
          />
        </artivact-content>
      </div>
    </template>

    <template v-slot:widget-editor-preview>
      <div class="gt-sm">
        <artivact-content>
          <div class="row full-width">
            <div class="col shadow-2 q-mr-lg">
              <q-carousel
                v-model="slide"
                v-model:fullscreen="fullscreen"
                class="shadow-2 rounded-borders"
              >
                <q-carousel-slide
                  draggable="false"
                  name="image"
                  :img-src="
                    '/api/page/widget/' +
                    widgetDataRef.id +
                    '/' +
                    widgetDataRef.image +
                    '?imageSize=CAROUSEL'
                  "
                >
                  <q-btn
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
            <template v-if="widgetDataRef.text">
              <div
                v-if="
                  localeStore.selectedLocale !== null &&
                  widgetDataRef.text.translations[localeStore.selectedLocale]
                "
                class="col"
                v-html="
                  format(
                    widgetDataRef.text.translations[localeStore.selectedLocale]
                  )
                "
              />
              <div
                v-if="
                  localeStore.selectedLocale !== null &&
                  !widgetDataRef.text.translations[localeStore.selectedLocale]
                "
                class="col text-red"
                v-html="format(widgetDataRef.text.value)"
              />
              <div
                v-if="localeStore.selectedLocale === null"
                class="col"
                v-html="format(widgetDataRef.text.value)"
              />
            </template>
          </div>
        </artivact-content>
      </div>
      <div class="lt-md">
        <artivact-content>
          <q-img
            v-if="widgetDataRef.image"
            class="q-mb-md"
            :src="
              '/api/page/widget/' +
              widgetDataRef.id +
              '/' +
              widgetDataRef.image +
              '?imageSize=CAROUSEL'
            "
          >
          </q-img>
          <template v-if="widgetDataRef.text">
            <div
              v-if="
                localeStore.selectedLocale !== null &&
                widgetDataRef.text.translations[localeStore.selectedLocale]
              "
              v-html="
                format(
                  widgetDataRef.text.translations[localeStore.selectedLocale]
                )
              "
            />
            <div
              v-if="
                localeStore.selectedLocale !== null &&
                !widgetDataRef.text.translations[localeStore.selectedLocale]
              "
              class="text-red"
              v-html="format(widgetDataRef.text.value)"
            />
            <div
              v-if="localeStore.selectedLocale === null"
              v-html="format(widgetDataRef.text.value)"
            />
          </template>
        </artivact-content>
      </div>
    </template>

    <template v-slot:widget-editor-content>
      <artivact-content>
        <div class="column full-width">
          <q-uploader
            label="Image"
            auto-upload
            field-name="file"
            :no-thumbnails="true"
            class="q-mb-md"
            :url="'/api/page/' + pageIdRef + '/widget/' + widgetDataRef.id"
            @uploaded="$emit('image-added', 'image')"
            ref="imageUploader"
          >
          </q-uploader>
          <artivact-restricted-translatable-item-editor
            :locales="localeStore.locales"
            :translatable-string="widgetDataRef.text"
            :textarea="true"
            label="Text"
            :show-separator="false"
          />
        </div>
      </artivact-content>
    </template>
  </widget-template>
</template>

<script setup lang="ts">
import {PropType, ref, toRef} from 'vue';
import {ImageTextWidgetData} from 'components/widgets/widget-models';
import ArtivactContent from 'components/ArtivactContent.vue';
import WidgetTemplate from 'components/widgets/WidgetTemplate.vue';
import {QUploader} from 'quasar';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';
import {useLocaleStore} from 'stores/locale';
import MarkdownIt from 'markdown-it';
import {translate} from 'components/utils';

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
    type: Object as PropType<ImageTextWidgetData>,
  },
});

const localeStore = useLocaleStore();

const pageIdRef = toRef(props, 'pageId');
const widgetDataRef = toRef(props, 'widgetData');

const fullscreen = ref(false);
const slide = ref('image');

function format(text: string) {
  if (!text) {
    return;
  }
  let md = new MarkdownIt();
  return md.render(text);
}
</script>

<style scoped></style>
