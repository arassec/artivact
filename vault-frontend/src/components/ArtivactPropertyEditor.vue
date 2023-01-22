<template>
  <div>
    <q-input class="q-mb-md" outlined v-model="enteredValueRef" :label="propertyRef.translatedValue"
             @change="updateValue" v-if="propertyRef.valueRange.length === 0" clearable @clear="deleteValue"/>
    <q-select class="q-mb-md" outlined v-model="selectedValueRef" :options="calcOptions()" :label="propertyRef.translatedValue"
             @update:model-value="updateValue" v-if="propertyRef.valueRange.length !== 0" clearable
             @clear="deleteValue"/>
  </div>
</template>

<script setup lang="ts">

import {PropType, ref, toRef} from 'vue';
import {SelectboxModel, TranslatedProperty} from 'components/models';

const props = defineProps({
  property: {
    required: true,
    type: Object as PropType<TranslatedProperty>
  },
  properties: {
    required: true,
    type: Object as PropType<Record<string, string>>
  }
});
const propertyRef = toRef(props, 'property');
const propertiesRef = toRef(props, 'properties');
const optionsRef = ref(calcOptions());

const enteredValueRef = ref(propertiesRef.value[propertyRef.value.id]);
const selectedValueRef = ref(optionsRef.value.find((option) => {
  return option.value === propertiesRef.value[propertyRef.value.id];
}));

function updateValue() {
  if (propertyRef.value.valueRange.length === 0) {
    propertiesRef.value[propertyRef.value.id] = enteredValueRef.value;
  } else if (selectedValueRef.value) {
    propertiesRef.value[propertyRef.value.id] = selectedValueRef.value.value;
  }
}

function deleteValue() {
  delete propertiesRef.value[propertyRef.value.id];
}

function calcOptions() {
  let options : SelectboxModel[] = [];
  propertyRef.value.valueRange.forEach((translatedItem) => {
    options.push({
      label: translatedItem.translatedValue,
      value: translatedItem.value
    })
  })
  return options;
}

</script>

<style scoped>

</style>
