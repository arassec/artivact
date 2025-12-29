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
              <h1 class="page-title full-width">
                {{ translate(widgetDataRef.title) }}
              </h1>
              <h2 class="page-subtitle full-width" v-if="widgetDataRef.subtitle">
                {{ translate(widgetDataRef.subtitle) }}
              </h2>
              <div class="row">
                <div v-for="(buttonConfig, index) in widgetDataRef.buttonConfigs" :key="index" class="q-mr-md">
                  <artivact-button class="page-button" :config="buttonConfig" :disabled="inEditMode"/>
                </div>
              </div>
            </artivact-content>
          </q-parallax>
        </div>
        <template v-if="widgetDataRef.title && !widgetDataRef.backgroundImage">
          <artivact-content>
            <h1 class="av-label-h1 gt-sm full-width text-center">
              {{ translate(widgetDataRef.title) }}
            </h1>
            <h2 v-if="widgetDataRef.subtitle" class="full-width text-center">
              {{ translate(widgetDataRef.subtitle) }}
            </h2>
            <div v-for="(buttonConfig, index) in widgetDataRef.buttonConfigs" :key="index"
                 class="text-center full-width q-mt-sm">
              <artivact-button class="page-button" :config="buttonConfig" :disabled="inEditMode"/>
            </div>
          </artivact-content>
        </template>
      </div>
      <div class="lt-md full-bleed" v-if="widgetDataRef">
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
            class="page-title-image-small"
            fit="cover"
          >
            <div class="absolute-full flex flex-center page-title-small">
              <h2
                class="page-title-heading-small full-width text-center"
                v-if="widgetDataRef.title"
              >
                {{ translate(widgetDataRef.title) }}
              </h2>
              <h3
                class="page-subtitle-heading-small full-width text-center"
                v-if="widgetDataRef.subtitle"
              >
                {{ translate(widgetDataRef.subtitle) }}
              </h3>
              <div v-for="(buttonConfig, index) in widgetDataRef.buttonConfigs" :key="index" class="q-mr-sm q-mb-sm ">
                <artivact-button class="page-button" :config="buttonConfig" :disabled="inEditMode"/>
              </div>
            </div>
          </q-img>
        </div>
        <template v-if="widgetDataRef.title && !widgetDataRef.backgroundImage">
          <h2 class="page-title full-width text-center text-primary">
            {{ translate(widgetDataRef.title) }}
          </h2>
          <h3
            class="page-subtitle-heading-small full-width text-center text-primary"
            v-if="widgetDataRef.subtitle"
          >
            {{ translate(widgetDataRef.subtitle) }}
          </h3>
          <div v-for="(buttonConfig, index) in widgetDataRef.buttonConfigs" :key="index" class="q-mr-sm q-mb-sm ">
            <artivact-button class="page-button" :config="buttonConfig" :disabled="inEditMode"/>
          </div>
        </template>
      </div>
    </template>

    <template v-slot:widget-editor>
      <div class="column full-width page-title-editor">
        <q-uploader
          :label="$t('PageTitleWidget.label.bgImage')"
          :auto-upload="true"
          :multiple="false"
          field-name="file"
          :no-thumbnails="true"
          class="q-mb-md"
          :url="'/api/page/' + pageIdRef + '/widget/' + widgetDataRef.id"
          @uploaded="$emit('image-added', 'backgroundImage')"
          @start="saveWidgetBeforeUpload"
          ref="imageUploader"
        >
        </q-uploader>
        <artivact-restricted-translatable-item-editor
          :locales="localeStore.locales"
          :translatable-string="widgetDataRef.title"
          :label="$t('PageTitleWidget.label.title')"
          :show-separator="false"
        />
        <artivact-restricted-translatable-item-editor
          :locales="localeStore.locales"
          :translatable-string="widgetDataRef.subtitle"
          :label="$t('PageTitleWidget.label.subtitle')"
          :show-separator="false"
        />
        <div class="column full-width">
          <p class="text-grey-14 q-mb-sm text-black">{{ $t('PageTitleWidget.label.buttons') }}</p>
          <q-list bordered class="rounded-borders q-mb-lg">
            <q-expansion-item
              header-class="bg-primary text-white"
              class="list-entry"
              expand-separator
              expand-icon-class="text-white"
              v-for="(buttonConfig, index) in widgetDataRef.buttonConfigs"
              :key="index"
            >
              <template v-slot:header>
                <q-item-section class="list-entry-label">
                  {{ translate(buttonConfig.label) }}
                </q-item-section>

                <q-item-section side>
                  <q-btn
                    round
                    dense
                    flat
                    class="float-right"
                    color="white"
                    icon="delete"
                    size="md"
                    @click.stop="deleteButton(index)"
                  >
                  </q-btn>
                </q-item-section>
              </template>

              <artivact-button-editor :config="buttonConfig" class="q-mt-md"/>
            </q-expansion-item>
          </q-list>

          <div>
            <q-btn
              class="float-right"
              rounded
              dense
              color="primary"
              icon="add"
              @click="addButtonConfig()"
            />
          </div>
        </div>
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
import ArtivactButton from "../ArtivactButton.vue";
import ArtivactButtonEditor from "../ArtivactButtonEditor.vue";
import {ButtonConfig, TranslatableString} from "../artivact-models";

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

const emit = defineEmits<{
  (e: 'image-added', payload: string): void;
  (e: 'save-widget-before-upload', payload: { resolve; reject; }): void;
}>();

const editingRef = ref(false);

const localeStore = useLocaleStore();

const pageIdRef = toRef(props, 'pageId');
const widgetDataRef = toRef(props, 'widgetData');

function saveWidgetBeforeUpload() {
  return new Promise((resolve, reject) => {
    emit('save-widget-before-upload', {resolve, reject})
  });
}

function addButtonConfig() {
  widgetDataRef.value.buttonConfigs.push({
    targetUrl: '',
    iconLeft: '',
    label: {
      value: 'Button',
    } as TranslatableString,
    iconRight: '',
    size: 1,
    buttonColor: 'primary',
    textColor: 'white',
  } as ButtonConfig);
}

function deleteButton(index) {
  widgetDataRef.value.buttonConfigs.splice(index, 1);
}

</script>

<style scoped>
.page-title {
  font-weight: bold;
  font-size: 6rem;
  line-height: 6.5rem;
  color: white;
  margin-top: 0;
  margin-bottom: 0.1em;
}

.page-subtitle {
  font-weight: bold;
  font-size: 2rem;
  line-height: 2.5rem;
  color: white;
  filter: none;
  margin-bottom: 0.5em;
}

.page-button {
  filter: none;
}

.page-title-heading-small {
  font-weight: bold;
  font-size: 2rem;
  line-height: 2.5rem;
  color: white;
  filter: none;
  margin-top: 0;
  margin-bottom: 0.1em;
}

.page-subtitle-heading-small {
  font-size: 1rem;
  line-height: 1.5rem;
  color: white;
  filter: none;
  margin-bottom: 0.5em;
}

.page-title-small {
  background: rgba(0, 0, 0, 0.25);
}

.page-title-image-small {
  min-height: 12em;
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
