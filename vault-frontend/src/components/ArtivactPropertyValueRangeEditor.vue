<template>
  <div class="q-ml-sm">
    <label class="q-mr-xs q-mt-xs text-grey vertical-middle badge-container-label">Range of values:</label>
    <q-badge class="q-mr-xs vertical-middle" color="secondary" v-for="(value, key) in valueRangeProp" :key="key">
      {{ value.value }}
      <q-btn class="q-ml-xs" rounded dense flat color="primary" size="xs" icon="edit" @click="editValue(value)"/>
      <q-btn rounded dense flat color="primary" size="xs" icon="close" @click="deleteValue(key)"/>
    </q-badge>
    <q-btn class="vertical-middle" round dense unelevated color="secondary" size="xs" icon="add" @click="addValue"/>
    <q-dialog v-model="addValueRef" persistent transition-show="scale" transition-hide="scale" class=" justify-center">
      <q-card class="value-range-dialog">
        <q-card-section>
          Add Value
        </q-card-section>
        <q-card-section class="column-lg q-ma-md items-center">
          <artivact-translatable-item-editor :item="valueRef" label="Value" :locales="locales"/>
        </q-card-section>
        <q-card-actions>
          <q-btn flat label="Cancel" v-close-popup/>
          <q-space/>
          <q-btn flat label="Save" v-close-popup @click="saveValue"/>
        </q-card-actions>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup lang="ts">
import {PropType, ref, toRef} from 'vue';
import {TranslatableItem} from 'components/models';
import ArtivactTranslatableItemEditor from 'components/ArtivactTranslatableItemEditor.vue';

const props = defineProps({
  valueRange: {
    required: true,
    type: Object as PropType<TranslatableItem[]>
  },
  locales: {
    required: true,
    type: Object as PropType<string[]>
  }
});
const valueRangeProp = toRef(props, 'valueRange');

const value: TranslatableItem = {
  id: '',
  value: 'New Value',
  translations: {},
  restrictions: []
}

let addValueRef = ref(false);
const valueRef = ref(value);

let editedItem: TranslatableItem | null = null;

function addValue() {
  editedItem = null;
  valueRef.value = {
    id: '',
    value: 'New Value',
    translations: {},
    restrictions: []
  }
  addValueRef.value = true;
}

function editValue(item: TranslatableItem) {
  editedItem = item;
  valueRef.value = {
    id: item.id,
    value: item.value,
    translations: item.translations,
    restrictions: item.restrictions
  }
  addValueRef.value = true;
}

function saveValue() {
  if (editedItem !== null) {
    editedItem.value = valueRef.value.value;
    editedItem.translations = valueRef.value.translations;
    editedItem.restrictions = valueRef.value.restrictions;
  } else {
    let item: TranslatableItem = {
      id: valueRef.value.id,
      value: valueRef.value.value,
      translations: valueRef.value.translations,
      restrictions: valueRef.value.restrictions
    }
    valueRangeProp.value.push(item);
  }
}

function deleteValue(index: number) {
  valueRangeProp.value.splice(index, 1);
}

</script>

<style scoped>
.badge-container-label {
  display: inline-block;
}

.value-range-dialog {
  min-width: 350px;
}
</style>
