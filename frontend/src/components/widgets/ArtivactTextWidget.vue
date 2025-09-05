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
        <div v-html="format(translate(widgetDataRef.content))" />
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
      </div>
    </template>
  </artivact-widget-template>
</template>

<script setup lang="ts">
import { PropType, ref, toRef } from 'vue';
import { TextWidgetData } from './artivact-widget-models';
import ArtivactRestrictedTranslatableItemEditor from '../../components/ArtivactRestrictedTranslatableItemEditor.vue';
import { useLocaleStore } from '../../stores/locale';
import MarkdownIt from 'markdown-it';
import { translate } from '../artivact-utils';
import ArtivactWidgetTemplate from '../../components/widgets/ArtivactWidgetTemplate.vue';

const props = defineProps({
  inEditMode: {
    required: true,
    type: Boolean,
  },
  widgetData: {
    required: true,
    type: Object as PropType<TextWidgetData>,
  },
});

const editingRef = ref(false);

const localeStore = useLocaleStore();

const widgetDataRef = toRef(props, 'widgetData');

function format(text: string) {
  if (!text) {
    return;
  }
  let md = new MarkdownIt();
  return md.render(text);
}
</script>

<style scoped></style>
