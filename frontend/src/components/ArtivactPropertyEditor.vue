<template>
  <div v-if="propertyRef">
    <artivact-restricted-translatable-item-editor :label="translate(propertyRef)"
                                                  :translatable-string="enteredValueRef"
                                                  v-if="propertyRef.valueRange.length === 0"
                                                  :show-separator="false"/>
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
import {computed, PropType, Ref, ref, toRef, watch} from 'vue';
import {Property, SelectboxModel, TranslatableString} from './artivact-models';
import {translate} from './artivact-utils';
import ArtivactRestrictedTranslatableItemEditor from '../components/ArtivactRestrictedTranslatableItemEditor.vue';

const props = defineProps({
  property: {
    required: true,
    type: Object as PropType<Property>,
  },
  properties: {
    required: true,
    type: Object as PropType<Record<string, TranslatableString>>,
  },
});

const propertyRef = toRef(props, 'property');
const propertiesRef = toRef(props, 'properties');
const optionsRef = ref(calcOptions());

const enteredValueRef: Ref<TranslatableString> = ref(propertiesRef.value[propertyRef.value.id]);
const selectedValueRef = ref(
  optionsRef.value.find((option) => {
    return option.value === (propertiesRef.value[propertyRef.value.id] ? propertiesRef.value[propertyRef.value.id].value : undefined);
  })
);

function updateValue() {
  if (selectedValueRef.value) {
    if (!propertiesRef.value[propertyRef.value.id]) {
      propertiesRef.value[propertyRef.value.id] = {
        value: '',
        translatedValue: '',
        translations: {}
      }
    }
    propertiesRef.value[propertyRef.value.id].value = selectedValueRef.value.value;
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
      disable: false
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
      disable: false
    });
  });
  return options;
});

watch(propertiesRef, () => {
  enteredValueRef.value = propertiesRef.value[propertyRef.value.id];
  selectedValueRef.value = availableOptions.value.find((option) => {
    return option.value === (propertiesRef.value[propertyRef.value.id] ? propertiesRef.value[propertyRef.value.id].value : undefined);
  });
});

</script>

<style scoped></style>
