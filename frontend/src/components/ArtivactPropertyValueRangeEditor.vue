<template>
  <div class="q-ml-sm row">
    <div class="editor-label q-mt-xs">
      <label class="q-mr-xs text-grey vertical-middle badge-container-label"
        >Range of values:</label
      >
    </div>

    <div>
      <q-badge
        class="q-mr-xs q-mt-xs vertical-middle"
        color="secondary"
        v-for="(value, key) in valueRangeProp"
        :key="key"
      >
        {{ translate(value) }}
        <q-btn
          class="q-ml-xs"
          rounded
          dense
          flat
          color="accent"
          size="xs"
          icon="edit"
          @click="editValue(value)"
        />
        <q-btn
          rounded
          dense
          flat
          color="accent"
          size="xs"
          icon="close"
          @click="deleteValue(key)"
        />
      </q-badge>
      <q-btn
        class="vertical-middle"
        round
        dense
        unelevated
        color="secondary"
        size="xs"
        icon="add"
        @click="addValue"
      />
    </div>

    <q-dialog
      v-model="addValueRef"
      persistent
      transition-show="scale"
      transition-hide="scale"
      class="justify-center"
    >
      <q-card class="value-range-dialog">
        <q-card-section> Configure Value </q-card-section>
        <q-card-section class="column-lg q-ma-md items-center">
          <artivact-restricted-translatable-item-editor
            :translatable-string="valueRef"
            :restricted-item="valueRef"
            label="Value"
            :locales="locales"
            :show-separator="false"
          />
        </q-card-section>
        <q-card-actions>
          <q-btn flat label="Cancel" v-close-popup />
          <q-space />
          <q-btn flat label="Save" v-close-popup @click="saveValue" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup lang="ts">
import {PropType, ref, toRef} from 'vue';
import {BaseTranslatableRestrictedItem} from 'components/models';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';
import {translate} from './utils';

const props = defineProps({
  valueRange: {
    required: true,
    type: Object as PropType<BaseTranslatableRestrictedItem[]>,
  },
  locales: {
    required: true,
    type: Object as PropType<string[]>,
  },
});
const valueRangeProp = toRef(props, 'valueRange');

const value: BaseTranslatableRestrictedItem = {
  id: '',
  value: 'New Value',
  translatedValue: '',
  translations: {},
  restrictions: [],
};

let addValueRef = ref(false);
const valueRef = ref(value);

let editedItem: BaseTranslatableRestrictedItem | null = null;

function addValue() {
  editedItem = null;
  valueRef.value = {
    id: '',
    value: 'New Value',
    translatedValue: '',
    translations: {},
    restrictions: [],
  };
  addValueRef.value = true;
}

function editValue(item: BaseTranslatableRestrictedItem) {
  editedItem = item;
  valueRef.value = {
    id: item.id,
    value: item.value,
    translatedValue: '',
    translations: item.translations,
    restrictions: item.restrictions,
  };
  addValueRef.value = true;
}

function saveValue() {
  if (editedItem !== null) {
    editedItem.value = valueRef.value.value;
    editedItem.translations = valueRef.value.translations;
    editedItem.restrictions = valueRef.value.restrictions;
  } else {
    let item: BaseTranslatableRestrictedItem = {
      id: valueRef.value.id,
      value: valueRef.value.value,
      translatedValue: '',
      translations: valueRef.value.translations,
      restrictions: valueRef.value.restrictions,
    };
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
