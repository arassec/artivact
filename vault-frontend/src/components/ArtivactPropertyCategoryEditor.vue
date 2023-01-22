<template>
  <div>
    <h2 class="av-text-h2">{{ categoryProp.translatedValue }}</h2>
    <artivact-property-editor v-for="(property, index) in categoryProp.properties" :property="property"
                              :key="index" :properties="properties"/>
  </div>
</template>

<script setup lang="ts">
import {PropType, toRef} from 'vue';
import {TranslatedPropertyCategory} from 'components/models';
import ArtivactPropertyEditor from 'components/ArtivactPropertyEditor.vue';

const props = defineProps({
  category: {
    required: true,
    type: Object as PropType<TranslatedPropertyCategory>
  },
  properties: {
    required: true,
    type: Object as PropType<Record<string, string>>
  }
});
const categoryProp = toRef(props, 'category');

</script>

<style scoped>

</style>
