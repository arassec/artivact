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
        <q-space />

        <q-card :class="infoBoxStyle">
          <q-card-section class="text-white" v-if="widgetDataRef.heading">
            <div class="av-text-h2">{{ translate(widgetDataRef.heading) }}</div>
            <template v-if="widgetDataRef.content">{{
              translate(widgetDataRef.content)
            }}</template>
          </q-card-section>
        </q-card>

        <q-space />
      </artivact-content>
    </template>

    <template v-slot:widget-editor-preview>
      <artivact-content>
        <q-space />

        <q-card :class="infoBoxStyle">
          <q-card-section class="text-white" v-if="widgetDataRef.heading.value">
            <div class="av-text-h2" v-if="localeStore.selectedLocale === null">
              {{ widgetDataRef.heading.value }}
            </div>
            <div
              class="av-text-h2"
              v-if="
                localeStore.selectedLocale !== null &&
                widgetDataRef.heading.translations[localeStore.selectedLocale]
              "
            >
              {{
                widgetDataRef.heading.translations[localeStore.selectedLocale]
              }}
            </div>
            <div
              v-if="
                localeStore.selectedLocale !== null &&
                !widgetDataRef.heading.translations[localeStore.selectedLocale]
              "
              class="av-text-h2 text-red"
            >
              {{ widgetDataRef.heading.value }}
            </div>
            <label v-if="localeStore.selectedLocale === null">{{
              widgetDataRef.content.value
            }}</label>
            <label
              v-if="
                localeStore.selectedLocale !== null &&
                widgetDataRef.heading.translations[localeStore.selectedLocale]
              "
            >
              {{
                widgetDataRef.content.translations[localeStore.selectedLocale]
              }}
            </label>
            <label
              v-if="
                localeStore.selectedLocale !== null &&
                !widgetDataRef.heading.translations[localeStore.selectedLocale]
              "
              class="text-red"
              >{{ widgetDataRef.content.value }}</label
            >
          </q-card-section>
        </q-card>

        <q-space />
      </artivact-content>
    </template>

    <template v-slot:widget-editor-content>
      <artivact-content>
        <div class="column full-width">
          <artivact-restricted-translatable-item-editor
            :locales="localeStore.locales"
            :translatable-string="widgetDataRef.heading"
            :label="$t('InfoBoxWidget.label.heading')"
            :show-separator="false"
            class="q-mb-sm"
          />
          <artivact-restricted-translatable-item-editor
            :locales="localeStore.locales"
            :translatable-string="widgetDataRef.content"
            :label="$t('InfoBoxWidget.label.content')"
            :textarea="true"
            :show-separator="false"
          />
          <q-select
            outlined
            v-model="widgetDataRef.boxType"
            :options="infoBoxTypes"
            :label="$t('InfoBoxWidget.label.outlined')"
          />
        </div>
      </artivact-content>
    </template>
  </widget-template>
</template>

<script setup lang="ts">
import WidgetTemplate from 'components/widgets/WidgetTemplate.vue';
import {InfoBoxWidgetData} from 'components/widgets/widget-models';
import {computed, PropType, toRef} from 'vue';
import {useLocaleStore} from 'stores/locale';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';
import ArtivactContent from 'components/ArtivactContent.vue';
import {translate} from '../utils';

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
    type: Object as PropType<InfoBoxWidgetData>,
  },
});

const localeStore = useLocaleStore();

const widgetDataRef = toRef(props, 'widgetData');

const infoBoxTypes = ['INFO', 'WARN', 'ALERT'];

const infoBoxStyle = computed(() => {
  if (widgetDataRef.value.boxType === 'INFO') {
    return 'bg-positive';
  } else if (widgetDataRef.value.boxType === 'WARN') {
    return 'bg-warning';
  } else {
    return 'bg-negative';
  }
});
</script>

<style scoped></style>
