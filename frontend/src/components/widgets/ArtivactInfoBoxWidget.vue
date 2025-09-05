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
    </template>

    <template v-slot:widget-editor>
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
    </template>
  </artivact-widget-template>
</template>

<script setup lang="ts">
import { InfoBoxWidgetData } from './artivact-widget-models';
import { computed, PropType, ref, toRef } from 'vue';
import { useLocaleStore } from '../../stores/locale';
import ArtivactRestrictedTranslatableItemEditor from '../../components/ArtivactRestrictedTranslatableItemEditor.vue';
import { translate } from '../artivact-utils';
import ArtivactWidgetTemplate from '../../components/widgets/ArtivactWidgetTemplate.vue';

const props = defineProps({
  inEditMode: {
    required: true,
    type: Boolean,
  },
  widgetData: {
    required: true,
    type: Object as PropType<InfoBoxWidgetData>,
  },
});

const editingRef = ref(false);

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
