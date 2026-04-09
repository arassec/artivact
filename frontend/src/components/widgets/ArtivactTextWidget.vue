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
      <div data-test="text-widget-editor-display">
        <h1 class="av-label-h1" v-if="widgetDataRef.heading">
          {{ translate(widgetDataRef.heading) }}
        </h1>
        <div class="formatted-text" v-html="formatMarkdown(translate(widgetDataRef.content))"/>
      </div>
    </template>

    <template v-slot:widget-editor>
      <div data-test="text-widget-editor-edit" class="column full-width">
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
        <div class="row items-center q-mt-md">
          <q-uploader
            :label="$t('TextWidget.label.contentAudio')"
            :auto-upload="true"
            :multiple="false"
            field-name="file"
            accept=".mp3"
            :no-thumbnails="true"
            class="q-mb-md col"
            :url="'/api/page/' + pageIdRef + '/widget/' + widgetDataRef.id"
            @uploaded="$emit('file-added', 'contentAudio')"
            @start="saveWidgetBeforeUpload"
            ref="audioUploader"
          />
          <q-btn
            v-if="widgetDataRef.contentAudio"
            round
            dense
            flat
            color="negative"
            icon="delete"
            class="q-ml-sm q-mb-md"
            @click="deleteAudio()"
          />
        </div>
      </div>
    </template>
  </artivact-widget-template>
</template>

<script setup lang="ts">
import {PropType, ref, toRef} from 'vue';
import {TextWidgetData} from './artivact-widget-models';
import ArtivactRestrictedTranslatableItemEditor from '../../components/ArtivactRestrictedTranslatableItemEditor.vue';
import {useLocaleStore} from '../../stores/locale';
import {formatMarkdown, translate} from '../artivact-utils';
import ArtivactWidgetTemplate from '../../components/widgets/ArtivactWidgetTemplate.vue';

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
    type: Object as PropType<TextWidgetData>,
  },
});

const emit = defineEmits<{
  (e: 'file-added', property: string): void;
  (e: 'file-deleted', parameters: string[]): void;
  (e: 'save-widget-before-upload', payload: { resolve; reject; }): void;
}>();

const editingRef = ref(false);

const localeStore = useLocaleStore();

const pageIdRef = toRef(props, 'pageId');
const widgetDataRef = toRef(props, 'widgetData');

const audioUploader = ref(null);

function deleteAudio() {
  if (widgetDataRef.value.contentAudio) {
    emit('file-deleted', ['contentAudio', widgetDataRef.value.contentAudio]);
  }
}

function saveWidgetBeforeUpload() {
  return new Promise((resolve, reject) => {
    emit('save-widget-before-upload', {resolve, reject})
  });
}

</script>

<style scoped>

.formatted-text {
  margin-bottom: -15px;
}

</style>
