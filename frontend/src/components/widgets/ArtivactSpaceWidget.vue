<template>
  <artivact-widget-template
    :move-down-enabled="moveDownEnabled"
    :move-up-enabled="moveUpEnabled"
    :in-edit-mode="inEditMode"
    v-if="widgetDataRef"
    :restrictions="widgetDataRef.restrictions"
  >
    <template v-slot:widget-content>
      <artivact-content
        v-if="widgetDataRef"
        :style="'padding-top: ' + widgetDataRef.size + 'em;'"
      >
      </artivact-content>
    </template>

    <template v-slot:widget-editor-preview>
      <artivact-content
        v-if="widgetDataRef"
        :style="'padding-top: ' + widgetDataRef.size + 'em;'"
      >
      </artivact-content>
    </template>

    <template v-slot:widget-editor-content>
      <artivact-content>
        <div class="column full-width">
          <q-input
            outlined
            v-model="widgetDataRef.size"
            :label="$t('SpaceWidget.label.spaceUnits')"
            type="number"
          />
        </div>
      </artivact-content>
    </template>
  </artivact-widget-template>
</template>

<script setup lang="ts">
import ArtivactContent from 'components/ArtivactContent.vue';
import {SpaceWidgetData} from 'components/widgets/artivact-widget-models';
import {PropType, toRef} from 'vue';
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
  widgetData: {
    required: true,
    type: Object as PropType<SpaceWidgetData>,
  },
});

const widgetDataRef = toRef(props, 'widgetData');
</script>

<style scoped></style>
