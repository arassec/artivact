<template>
  <div>
    <div class="q-mb-lg gt-xs" :class="marginRight ? 'q-pr-lg' : 'q-pl-lg'">
      <h2 class="av-text-h2">{{ translate(categoryRef) }}</h2>
      <q-separator class="q-mb-sm"/>
      <div
        class="row q-mb-xs"
        v-for="(property, index) in categoryRef.properties"
        :key="index">
        <div class="col text-weight-bold">
          {{ translate(property) }}
        </div>
        <div class="col">
          {{ getPropertyValue(property) }}
        </div>
      </div>
    </div>
    <div class="q-mb-lg lt-sm">
      <h2 class="av-text-h2">{{ translate(categoryRef) }}</h2>
      <q-separator class="q-mb-sm"/>
      <div
        class="row q-mb-xs"
        v-for="(property, index) in categoryRef.properties"
        :key="index">
        <div class="col text-weight-bold">
          {{ translate(property) }}
        </div>
        <div class="col">
          {{ getPropertyValue(property) }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {PropType, Ref, toRef} from 'vue';
import {
  Property, PropertyCategory
} from 'components/artivact-models';
import {translate} from 'components/artivact-utils';

const props = defineProps({
  category: {
    required: true,
    type: Object as PropType<PropertyCategory>,
  },
  properties: {
    required: true,
    type: Object as PropType<Record<string, string>>,
  },
  marginRight: {
    required: true,
    type: Boolean
  }
});

const categoryRef: Ref<PropertyCategory> = toRef(props, 'category');
const propertiesRef: Ref<Record<string, string>> = toRef(props, 'properties');

function getPropertyValue(property: Property) {
  if (property.valueRange.length === 0) {
    return propertiesRef.value[property.id];
  } else {
    let result = '';
    property.valueRange.forEach((valueRangeEntry) => {
      if (valueRangeEntry.value === propertiesRef.value[property.id]) {
        result = translate(valueRangeEntry);
      }
    });
    return result;
  }
}
</script>

<style scoped></style>
