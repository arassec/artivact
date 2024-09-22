<template>
  <artivact-widget-template
    :move-down-enabled="moveDownEnabled"
    :move-up-enabled="moveUpEnabled"
    :in-edit-mode="inEditMode"
    v-if="widgetDataRef"
    :restrictions="widgetDataRef.restrictions"
    :navigation-title="widgetDataRef.navigationTitle"
  >
    <template v-slot:widget-content>
      <div class="gt-sm">
        <div v-if="widgetDataRef.backgroundImage">
          <q-parallax
            :src="
              '/api/page/widget/' +
              widgetDataRef.id +
              '/' +
              widgetDataRef.backgroundImage +
              '?imageSize=PAGE_TITLE'
            "
            class="darkened-parallax-container title-image"
          >
            <artivact-content v-if="widgetDataRef.title">
              <h1 class="page-title gt-sm">
                {{ translate(widgetDataRef.title) }}
              </h1>
            </artivact-content>
          </q-parallax>
        </div>
        <artivact-content
          v-if="widgetDataRef.title && !widgetDataRef.backgroundImage"
        >
          <h1 class="av-label-h1 gt-sm">
            {{ translate(widgetDataRef.title) }}
          </h1>
        </artivact-content>
      </div>
      <div class="lt-md" v-if="widgetDataRef">
        <div v-if="widgetDataRef && widgetDataRef.backgroundImage">
          <q-img
            :src="
              '/api/page/widget/' +
              widgetDataRef.id +
              '/' +
              widgetDataRef.backgroundImage +
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
        <artivact-content
          v-if="widgetDataRef.title && !widgetDataRef.backgroundImage"
        >
          <h2 class="page-title av-text-h2 lt-md text-primary">
            {{ translate(widgetDataRef.title) }}
          </h2>
        </artivact-content>
      </div>
    </template>

    <template v-slot:widget-editor-preview>
      <div class="gt-sm">
        <div v-if="widgetDataRef.backgroundImage">
          <q-parallax
            :src="
              '/api/page/widget/' +
              widgetDataRef.id +
              '/' +
              widgetDataRef.backgroundImage +
              '?imageSize=PAGE_TITLE'
            "
            class="darkened-parallax-container"
          >
            <artivact-content v-if="widgetDataRef.title">
              <h1
                class="page-title gt-sm"
                v-if="localeStore.selectedLocale === null"
              >
                {{ widgetDataRef.title.value }}
              </h1>
              <h1
                class="page-title gt-sm"
                v-if="
                  localeStore.selectedLocale !== null &&
                  widgetDataRef.title.translations[localeStore.selectedLocale]
                "
              >
                {{
                  widgetDataRef.title.translations[localeStore.selectedLocale]
                }}
              </h1>
              <h1
                class="page-title gt-sm text-red"
                v-if="
                  localeStore.selectedLocale !== null &&
                  !widgetDataRef.title.translations[localeStore.selectedLocale]
                "
              >
                {{ widgetDataRef.title.value }}
              </h1>
            </artivact-content>
          </q-parallax>
        </div>
        <artivact-content
          v-if="widgetDataRef.title && !widgetDataRef.backgroundImage"
        >
          <h1 class="av-label-h1" v-if="localeStore.selectedLocale === null">
            {{ widgetDataRef.title.value }}
          </h1>
          <h1
            class="av-label-h1"
            v-if="
              localeStore.selectedLocale !== null &&
              widgetDataRef.title.translations[localeStore.selectedLocale]
            "
          >
            {{ widgetDataRef.title.translations[localeStore.selectedLocale] }}
          </h1>
          <h1
            class="av-label-h1 text-red"
            v-if="
              localeStore.selectedLocale !== null &&
              !widgetDataRef.title.translations[localeStore.selectedLocale]
            "
          >
            {{ widgetDataRef.title.value }}
          </h1>
        </artivact-content>
      </div>
      <div class="lt-md" v-if="widgetDataRef">
        <div v-if="widgetDataRef && widgetDataRef.backgroundImage">
          <q-img
            :src="
              '/api/page/widget/' +
              widgetDataRef.id +
              '/' +
              widgetDataRef.backgroundImage +
              '?imageSize=PAGE_TITLE'
            "
          >
            <div class="absolute-full flex flex-center page-title-small">
              <h2
                class="page-title av-text-h2 text-white lt-md"
                v-if="widgetDataRef.title"
              >
                {{ translate(widgetDataRef.title) }}
              </h2>
            </div>
          </q-img>
        </div>
        <artivact-content
          v-if="widgetDataRef.title && !widgetDataRef.backgroundImage"
        >
          <h2
            class="page-title av-text-h2 lt-md text-primary"
            v-if="localeStore.selectedLocale === null"
          >
            {{ widgetDataRef.title.value }}
          </h2>
          <h2
            class="page-title av-text-h2 lt-md text-primary"
            v-if="
              localeStore.selectedLocale !== null &&
              widgetDataRef.title.translations[localeStore.selectedLocale]
            "
          >
            {{ widgetDataRef.title.translations[localeStore.selectedLocale] }}
          </h2>
          <h2
            class="page-title av-text-h2 lt-md text-red"
            v-if="
              localeStore.selectedLocale !== null &&
              !widgetDataRef.title.translations[localeStore.selectedLocale]
            "
          >
            {{ widgetDataRef.title.value }}
          </h2>
        </artivact-content>
      </div>
    </template>

    <template v-slot:widget-editor-content>
      <artivact-content>
        <div class="column full-width">
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
      </artivact-content>
    </template>
  </artivact-widget-template>
</template>

<script setup lang="ts">
import {PropType, toRef} from 'vue';
import {PageTitleWidgetData} from 'components/widgets/artivact-widget-models';
import ArtivactContent from 'components/ArtivactContent.vue';
import {QUploader} from 'quasar';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';
import {useLocaleStore} from 'stores/locale';
import {translate} from '../artivact-utils';
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
  pageId: {
    required: true,
    type: String,
  },
  widgetData: {
    required: true,
    type: Object as PropType<PageTitleWidgetData>,
  },
});

const localeStore = useLocaleStore();

const pageIdRef = toRef(props, 'pageId');
const widgetDataRef = toRef(props, 'widgetData');
</script>

<style scoped>
.page-title {
  font-weight: bold;
  color: white;
  filter: none;
}

.page-title-heading-small {
  font-size: 10vw;
  font-weight: bold;
  color: white;
  filter: none;
}

.page-title-small {
  background: rgba(0, 0, 0, 0.25);
}

</style>
