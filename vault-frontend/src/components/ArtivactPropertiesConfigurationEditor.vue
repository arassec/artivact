<template>
  <div>

    <q-card v-for="(category, categoryKey) in propertiesConfigurationProp.categories" :key="categoryKey"
            class="q-mb-lg">

      <q-card-section class="bg-primary text-white">
        <div class="text-h6">{{ category.value }}</div>
      </q-card-section>

      <q-separator/>

      <q-card-section>
        <artivact-translatable-item-editor :item="category" :locales="locales" label="Category"
                                           :show-separator="false"/>
      </q-card-section>

      <q-separator/>

      <q-card-section>
        <h3 class="av-text-h3">Properties</h3>
        <div v-for="(property, propertyKey) in category.properties" :key="propertyKey">
          <artivact-translatable-item-editor :item="property" :locales="locales" label="Property"
                                             :show-separator="false"/>
          <artivact-property-value-range-editor :value-range="property.valueRange" :locales="locales"/>
        </div>
      </q-card-section>

      <q-separator/>

      <q-card-section>
        <q-btn label="Add Property" @click="addProperty(category)" color="primary"/>
      </q-card-section>
    </q-card>

    <q-btn label="Add Category" @click="addCategory" color="primary"/>
  </div>
</template>

<script setup lang="ts">
import {PropType, toRef} from 'vue';
import {PropertiesConfiguration, Property, PropertyCategory} from 'components/models';
import ArtivactTranslatableItemEditor from 'components/ArtivactTranslatableItemEditor.vue';
import ArtivactPropertyValueRangeEditor from 'components/ArtivactPropertyValueRangeEditor.vue';

const props = defineProps({
  propertiesConfiguration: {
    required: true,
    type: Object as PropType<PropertiesConfiguration>
  },
  locales: {
    required: true,
    type: Object as PropType<string[]>
  }
});
const propertiesConfigurationProp = toRef(props, 'propertiesConfiguration');

function addCategory() {
  let category: PropertyCategory = {
    id: '',
    value: 'New Category',
    translations: {},
    restrictions: [],
    properties: []
  };
  propertiesConfigurationProp.value.categories.push(category);
}

function addProperty(category: PropertyCategory) {
  let property: Property = {
    id: '',
    value: 'New Property',
    translations: {},
    restrictions: [],
    valueRange: []
  }
  category.properties.push(property);
}

</script>

<style scoped>

</style>
