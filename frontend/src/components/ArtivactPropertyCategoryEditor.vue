<template>
  <div>
    <h2 class="av-text-h2">{{ translate(categoryRef) }}</h2>
    <artivact-property-editor
      v-for="(property, index) in categoryRef.properties"
      :property="property"
      :key="index"
      :properties="properties"
    />
  </div>
</template>

<script setup lang="ts">
import {PropType, toRef} from 'vue';
import ArtivactPropertyEditor from 'components/ArtivactPropertyEditor.vue';
import {PropertyCategory, TranslatableString} from 'components/artivact-models';
import {translate} from './artivact-utils';

const props = defineProps({
  category: {
    required: true,
    type: Object as PropType<PropertyCategory>,
  },
  properties: {
    required: true,
    type: Object as PropType<Record<string, TranslatableString>>,
  },
});
const categoryRef = toRef(props, 'category');
</script>

<style scoped></style>
