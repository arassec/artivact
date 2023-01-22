<template>
  <div>
    <!-- Input field -->
    <q-input outlined v-model="itemProp.value" :label="label" :type="textarea ? 'textarea' : ''"></q-input>
    <!-- Translations -->
    <div class="q-mt-xs">
      <label class="q-mr-xs q-mt-xs text-grey vertical-middle badge-container-label">Translations:</label>
      <q-badge class="q-mr-xs vertical-middle" color="secondary" v-for="(value, key) in itemProp.translations" :key="key">{{ key }}: {{ value }}
        <q-btn class="q-ml-xs" rounded dense flat color="primary" size="xs" icon="edit" @click="editTranslation(key, value)"/>
        <q-btn rounded dense flat color="primary" size="xs" icon="close" @click="deleteTranslation(key)"/>
      </q-badge>
      <q-btn class="vertical-middle" round dense unelevated color="secondary" size="xs" icon="add" @click="addTranslation"/>
      <q-dialog v-model="addTranslationRef" persistent transition-show="scale" transition-hide="scale" class=" justify-center">
        <q-card>
          <q-card-section>
            Add Translation
          </q-card-section>
          <q-card-section class="column-lg q-ma-md items-center">
            <q-select v-model="translationValueRef.key" autofocus :options="locales" label="Locale"/>
            <q-input v-model="translationValueRef.value" label="Translation" :type="textarea ? 'textarea' : ''"/>
          </q-card-section>
          <q-card-actions>
            <q-btn flat label="Cancel" v-close-popup/>
            <q-space />
            <q-btn flat label="Save" v-close-popup @click="saveTranslation"/>
          </q-card-actions>
        </q-card>
      </q-dialog>
    </div>
    <!-- Restrictions -->
    <div>
      <label class="q-mr-xs q-mt-xs text-grey vertical-middle badge-container-label">Restrictions:</label>
      <q-badge class="q-mr-xs vertical-middle" color="secondary" v-for="value in itemProp.restrictions" :key="value">{{ value }}
        <q-btn class="q-ml-xs" rounded dense flat color="primary" size="xs" icon="edit" @click="editRestriction(value)"/>
        <q-btn rounded dense flat color="primary" size="xs" icon="close" @click="deleteRestriction(value)"/>
      </q-badge>
      <q-btn class=" vertical-middle" round dense unelevated color="secondary" size="xs" icon="add" @click="addRestriction"/>
      <q-dialog v-model="addRestrictionRef" persistent transition-show="scale" transition-hide="scale" class=" justify-center">
        <q-card>
          <q-card-section>
            Add Restriction
          </q-card-section>
          <q-card-section class="column-lg q-ma-md items-center">
            <q-input v-model="restrictionValueRef" label="Restriction"/>
          </q-card-section>
          <q-card-actions>
            <q-btn flat label="Cancel" v-close-popup/>
            <q-space />
            <q-btn flat label="Save" v-close-popup @click="saveRestriction"/>
          </q-card-actions>
        </q-card>
      </q-dialog>
    </div>
    <q-separator class="q-mt-xs q-mb-lg" v-if="showSeparator"/>
  </div>
</template>

<script setup lang="ts">
import {TranslatableItem} from 'components/models';
import {PropType, ref, toRef} from 'vue';

const props = defineProps({
  label: {
    required: true,
    type: String,
    default: ''
  },
  textarea: {
    required: false,
    type: Boolean,
    default: false
  },
  showSeparator: {
    required: false,
    type: Boolean,
    default: true
  },
  item: {
    required: true,
    type: Object as PropType<TranslatableItem>
  },
  locales: {
    required: true,
    type: Object as PropType<string[]>
  }
});
const itemProp = toRef(props, 'item');

const translation = {
  key: '',
  value: ''
}

function addTranslation() {
  translationValueRef.value = {
    key: '',
    value: ''
  }
  addTranslationRef.value = true;
}

function editTranslation(existingKey: string, existingValue: string) {
  translationValueRef.value = {
    key: existingKey,
    value: existingValue
  }
  addTranslationRef.value = true;
}

function saveTranslation() {
  itemProp.value.translations[translationValueRef.value.key] = translationValueRef.value.value;
}

function deleteTranslation(key: string) {
  delete itemProp.value.translations[key];
}

function addRestriction() {
  restrictionValueRef.value = '';
  oldRestrictionValueRef.value = '';
  addRestrictionRef.value = true;
}

function editRestriction(existingValue: string) {
  oldRestrictionValueRef.value = existingValue;
  restrictionValueRef.value = existingValue;
  addRestrictionRef.value = true;
}

function saveRestriction() {
  if (oldRestrictionValueRef.value !== '') {
    itemProp.value.restrictions.forEach((item, index) => {
      if (item === oldRestrictionValueRef.value) {
        itemProp.value.restrictions.splice(index, 1, restrictionValueRef.value);
      }
    });
  } else {
    itemProp.value.restrictions.push(restrictionValueRef.value);
  }
}

function deleteRestriction(value: string) {
  itemProp.value.restrictions.forEach((item, index) => {
    if (item === value) {
      itemProp.value.restrictions.splice(index, 1);
    }
  });
}

let addTranslationRef = ref(false);
let addRestrictionRef = ref(false);

const translationValueRef = ref(translation);
const restrictionValueRef = ref('');
const oldRestrictionValueRef = ref('');
</script>

<style scoped>
.badge-container-label {
  min-width: 6em;
  display:inline-block;
}
</style>
