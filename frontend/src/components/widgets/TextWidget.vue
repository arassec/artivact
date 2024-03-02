<template>
  <widget-template
    :move-down-enabled="moveDownEnabled"
    :move-up-enabled="moveUpEnabled"
    :in-edit-mode="inEditMode"
    v-if="widgetDataRef"
    :restrictions="widgetDataRef.restrictions"
  >
    <template v-slot:widget-content>
      <artivact-content>
        <div class="column">
          <h1 class="av-label-h1" v-if="widgetDataRef.heading">
            {{ translate(widgetDataRef.heading) }}
          </h1>
          <div v-html="format(translate(widgetDataRef.content))" />
        </div>
      </artivact-content>
    </template>

    <template v-slot:widget-editor-preview>
      <artivact-content>
        <div class="column">
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
            v-html="format(widgetDataRef.content.value)"
          />
          <div
            v-if="
              localeStore.selectedLocale !== null &&
              widgetDataRef.content.translations[localeStore.selectedLocale]
            "
            v-html="
              format(
                widgetDataRef.content.translations[localeStore.selectedLocale]
              )
            "
          />
          <div
            v-if="
              localeStore.selectedLocale !== null &&
              !widgetDataRef.content.translations[localeStore.selectedLocale]
            "
            class="text-red"
            v-html="format(widgetDataRef.content.value)"
          />
        </div>
      </artivact-content>
    </template>

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
        </div>
      </artivact-content>
    </template>
  </widget-template>
</template>

<script setup lang="ts">
import { PropType, toRef } from 'vue';
import { TextWidgetData } from 'components/widgets/widget-models';
import ArtivactContent from 'components/ArtivactContent.vue';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';
import { useLocaleStore } from 'stores/locale';
import WidgetTemplate from 'components/widgets/WidgetTemplate.vue';
import MarkdownIt from 'markdown-it';
import { translate } from '../utils';

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
    type: Object as PropType<TextWidgetData>,
  },
});

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
