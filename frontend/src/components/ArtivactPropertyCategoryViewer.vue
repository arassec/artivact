<template>
  <div>
    <h2 class="av-text-h2">{{ translate(categoryRef) }}</h2>
    <q-separator class="q-mr-xl q-mb-sm" />
    <div
      class="row q-mb-xs"
      v-for="(property, index) in categoryRef.properties"
      :key="index"
    >
      <div class="col text-weight-bold">
        {{ translate(property) }}
      </div>
      <div class="col">
        {{ getPropertyValue(property) }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { PropType, toRef } from 'vue';
import {
  TranslatedProperty,
  TranslatedPropertyCategory,
} from 'components/models';
import { translate } from 'components/utils';

const props = defineProps({
  category: {
    required: true,
    type: Object as PropType<TranslatedPropertyCategory>,
  },
  properties: {
    required: true,
    type: Object as PropType<Record<string, string>>,
  },
});
const categoryRef = toRef(props, 'category');
const propertiesRef = toRef(props, 'properties');

function getPropertyValue(property: TranslatedProperty) {
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
