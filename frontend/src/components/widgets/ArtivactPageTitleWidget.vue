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
    <template v-slot:widget-content>
      <div class="gt-sm full-bleed">
        <div v-if="widgetDataRef.backgroundImage">
          <q-parallax
            :src="
              '/api/page/widget/' +
              widgetDataRef.id +
              '/' +
              widgetDataRef.backgroundImage +
              (inEditMode ? '/wip' : '') +
              '?imageSize=PAGE_TITLE'
            "
            class="av-darkened-parallax-container"
          >
            <artivact-content>
              <h1 class="page-title gt-sm">
                {{ translate(widgetDataRef.title) }}
              </h1>
            </artivact-content>
          </q-parallax>
        </div>
        <template v-if="widgetDataRef.title && !widgetDataRef.backgroundImage">
          <artivact-content>
            <h1 class="av-label-h1 gt-sm">
              {{ translate(widgetDataRef.title) }}
            </h1>
          </artivact-content>
        </template>
      </div>
      <div class="lt-md full-width" v-if="widgetDataRef">
        <div v-if="widgetDataRef && widgetDataRef.backgroundImage">
          <q-img
            :src="
              '/api/page/widget/' +
              widgetDataRef.id +
              '/' +
              widgetDataRef.backgroundImage +
              (inEditMode ? '/wip' : '') +
              '?imageSize=PAGE_TITLE'
            "
          >
            <div class="absolute-full flex flex-center page-title-small">
              <h2
                class="page-title-heading-small av-text-h2 text-white lt-md"
                v-if="widgetDataRef.title"
              >
                {{ translate(widgetDataRef.title) }}
              </h2>
            </div>
          </q-img>
        </div>
        <template v-if="widgetDataRef.title && !widgetDataRef.backgroundImage">
          <h2 class="page-title av-text-h2 lt-md text-primary">
            {{ translate(widgetDataRef.title) }}
          </h2>
        </template>
      </div>
    </template>

    <template v-slot:widget-editor>
      <div class="column full-width page-title-editor">
        <q-uploader
          :label="$t('PageTitleWidget.label.bgImage')"
          auto-upload
          field-name="file"
          :no-thumbnails="true"
          class="q-mb-md"
          :url="'/api/page/' + pageIdRef + '/widget/' + widgetDataRef.id"
          @uploaded="$emit('image-added', 'backgroundImage')"
          ref="imageUploader"
        >
        </q-uploader>
        <artivact-restricted-translatable-item-editor
          :locales="localeStore.locales"
          :translatable-string="widgetDataRef.title"
          :label="$t('PageTitleWidget.label.title')"
          :show-separator="false"
        />
      </div>
    </template>
  </artivact-widget-template>
</template>

<script setup lang="ts">
import {PropType, ref, toRef} from 'vue';
import {PageTitleWidgetData} from './artivact-widget-models';
import {QUploader} from 'quasar';
import ArtivactRestrictedTranslatableItemEditor from '../../components/ArtivactRestrictedTranslatableItemEditor.vue';
import {useLocaleStore} from '../../stores/locale';
import {translate} from '../artivact-utils';
import ArtivactWidgetTemplate from '../../components/widgets/ArtivactWidgetTemplate.vue';
import ArtivactContent from '../ArtivactContent.vue';

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
    type: Object as PropType<PageTitleWidgetData>,
  },
});

const editingRef = ref(false);

const localeStore = useLocaleStore();

const pageIdRef = toRef(props, 'pageId');
const widgetDataRef = toRef(props, 'widgetData');
</script>

<style scoped>
.page-title {
  font-weight: bold;
  font-size: 6rem;
  line-height: 6.5rem;
  color: white;
  filter: none;
}

.page-title-heading-small {
  font-weight: bold;
  font-size: 2rem;
  line-height: 2.5rem;
  color: white;
  filter: none;
}

.page-title-small {
  background: rgba(0, 0, 0, 0.25);
}

.full-bleed {
  margin-top: -16px;
  position: relative;
  left: 50%;
  right: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  width: 100vw;
}

.page-title-editor {
  min-height: 26em;
}
</style>
