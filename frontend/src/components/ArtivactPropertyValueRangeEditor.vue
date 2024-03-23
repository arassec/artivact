<template>
  <div class="q-ml-sm row">
    <div class="editor-label q-mt-xs">
      <label class="q-mr-xs text-grey vertical-middle badge-container-label">
        {{ $t('ArtivactPropertyValueRangeEditor.label') }}
      </label>
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
          @click="editValue(value)">
          <q-tooltip>{{ $t('ArtivactPropertyValueRangeEditor.tooltip.edit') }}</q-tooltip>
        </q-btn>
        <q-btn
          rounded
          dense
          flat
          color="accent"
          size="xs"
          icon="close"
          @click="deleteValue(key)">
          <q-tooltip>{{ $t('ArtivactPropertyValueRangeEditor.tooltip.delete') }}</q-tooltip>
        </q-btn>
      </q-badge>
      <q-btn
        class="vertical-middle"
        round
        dense
        unelevated
        color="secondary"
        size="xs"
        icon="add"
        @click="addValue">
        <q-tooltip>{{ $t('ArtivactPropertyValueRangeEditor.tooltip.add') }}</q-tooltip>
      </q-btn>
    </div>

    <artivact-dialog :dialog-model="addValueRef">
      <template v-slot:header>
        {{ $t('ArtivactPropertyValueRangeEditor.dialog.heading') }}
      </template>

      <template v-slot:body>
        <q-card-section>
          <artivact-restricted-translatable-item-editor
            :translatable-string="valueRef"
            :restricted-item="valueRef"
            :label="$t('ArtivactPropertyValueRangeEditor.dialog.label')"
            :locales="locales"
            :show-separator="false"
          />
        </q-card-section>
      </template>

      <template v-slot:cancel>
        <q-btn color="primary" :label="$t('Common.cancel')" @click="addValueRef = false"/>
      </template>

      <template v-slot:approve>
        <q-btn color="primary" :label="$t('Common.save')" @click="saveValue" />
      </template>
    </artivact-dialog>

  </div>
</template>

<script setup lang="ts">
import {PropType, ref, toRef} from 'vue';
import {BaseTranslatableRestrictedObject} from 'components/artivact-models';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';
import {translate} from './artivact-utils';
import ArtivactDialog from 'components/ArtivactDialog.vue';
import {useI18n} from 'vue-i18n';

const i18n = useI18n();

const props = defineProps({
  valueRange: {
    required: true,
    type: Object as PropType<BaseTranslatableRestrictedObject[]>,
  },
  locales: {
    required: true,
    type: Object as PropType<string[]>,
  },
});
const valueRangeProp = toRef(props, 'valueRange');

const value: BaseTranslatableRestrictedObject = {
  id: '',
  value: i18n.t('ArtivactPropertyValueRangeEditor.newValue'),
  translatedValue: '',
  translations: {},
  restrictions: [],
};

let addValueRef = ref(false);
const valueRef = ref(value);

let editedItem: BaseTranslatableRestrictedObject | null = null;

function addValue() {
  editedItem = null;
  valueRef.value = {
    id: '',
    value: i18n.t('ArtivactPropertyValueRangeEditor.newValue'),
    translatedValue: '',
    translations: {},
    restrictions: [],
  };
  addValueRef.value = true;
}

function editValue(item: BaseTranslatableRestrictedObject) {
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
    let item: BaseTranslatableRestrictedObject = {
      id: valueRef.value.id,
      value: valueRef.value.value,
      translatedValue: '',
      translations: valueRef.value.translations,
      restrictions: valueRef.value.restrictions,
    };
    valueRangeProp.value.push(item);
  }
  addValueRef.value = false;
}

function deleteValue(index: number) {
  valueRangeProp.value.splice(index, 1);
}
</script>

<style scoped>
.badge-container-label {
  display: inline-block;
}
</style>
