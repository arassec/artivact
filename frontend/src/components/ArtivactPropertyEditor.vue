<template>
  <div v-if="propertyRef">
    <q-input
      class="q-mb-md"
      outlined
      v-model="enteredValueRef"
      :label="translate(propertyRef)"
      @change="updateValue"
      v-if="propertyRef.valueRange.length === 0"
      clearable
      @clear="deleteValue"
    />
    <q-select
      class="q-mb-md"
      outlined
      v-model="selectedValueRef"
      :options="availableOptions"
      :label="translate(propertyRef)"
      @update:model-value="updateValue"
      v-if="propertyRef.valueRange.length !== 0"
      clearable
      @clear="deleteValue"
    />
  </div>
</template>

<script setup lang="ts">
import {computed, PropType, ref, toRef} from 'vue';
import {Property, SelectboxModel} from 'components/models';
import {translate} from 'components/utils';

const props = defineProps({
  property: {
    required: true,
    type: Object as PropType<Property>,
  },
  properties: {
    required: true,
    type: Object as PropType<Record<string, string>>,
  },
});
const propertyRef = toRef(props, 'property');
const propertiesRef = toRef(props, 'properties');
const optionsRef = ref(calcOptions());

const enteredValueRef = ref(propertiesRef.value[propertyRef.value.id]);
const selectedValueRef = ref(
  optionsRef.value.find((option) => {
    return option.value === propertiesRef.value[propertyRef.value.id];
  })
);

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
  let options: SelectboxModel[] = [];
  propertyRef.value.valueRange.forEach((translatedItem) => {
    options.push({
      label: translate(translatedItem),
      value: translatedItem.value,
    });
  });
  return options;
}

const availableOptions = computed(() => {
  let options: SelectboxModel[] = [];
  propertyRef.value.valueRange.forEach((translatedItem) => {
    options.push({
      label: translate(translatedItem),
      value: translatedItem.value,
    });
  });
  return options;
});
</script>

<style scoped></style>
