<template>
  <div :class="!offerDetails() ? 'q-mb-md' : ''">
    <!-- Default value if locale is selected -->
    <label v-if="translatableStringRef && localeStore.selectedLocale !== null">
      Default: {{ translatableStringRef.value }}
    </label>
    <!-- Input field -->
    <q-input
      v-if="translatableStringRef && localeStore.selectedLocale === null"
      outlined
      v-model="translatableStringRef.value"
      :label="label"
      :type="textarea ? 'textarea' : ''"
    ></q-input>
    <q-input
      v-if="translatableStringRef && localeStore.selectedLocale !== null"
      outlined
      v-model="translatableStringRef.translations[localeStore.selectedLocale]"
      :label="label"
      :type="textarea ? 'textarea' : ''"
    ></q-input>

    <q-btn
      rounded
      dense
      flat
      color="primary"
      size="md"
      icon="expand_more"
      @click="
        showDetailsRef = true;
        $emit('show-details');
      "
      v-if="offerDetails() && !showDetailsRef"
    />
    <q-btn
      rounded
      dense
      flat
      color="primary"
      size="md"
      icon="expand_less"
      @click="
        showDetailsRef = false;
        $emit('hide-details');
      "
      v-if="offerDetails() && showDetailsRef"
    />

    <div v-if="offerDetails()" v-show="showDetailsRef">
      <!-- Restrictions -->
      <artivact-restrictions-editor
        v-if="restrictedItemRef"
        :in-details-view="true"
        :restrictions="restrictedItemRef.restrictions"
        @delete-restriction="deleteRestriction"
        @add-restriction="addRestriction"
      />

      <!-- Slot for additional editors -->
      <slot></slot>

      <div v-if="!showSeparator" class="q-mb-md" />
    </div>
    <q-separator class="q-mt-xs q-mb-lg" v-if="showSeparator" />
  </div>
</template>

<script setup lang="ts">
import {PropType, ref, toRef, useSlots} from 'vue';
import ArtivactRestrictionsEditor from 'components/ArtivactRestrictionsEditor.vue';
import {BaseRestrictedItem, TranslatableString} from 'components/models';
import {useLocaleStore} from 'stores/locale';

const props = defineProps({
  label: {
    required: true,
    type: String,
    default: '',
  },
  textarea: {
    required: false,
    type: Boolean,
    default: false,
  },
  showSeparator: {
    required: false,
    type: Boolean,
    default: true,
  },
  translatableString: {
    required: false,
    type: Object as PropType<TranslatableString>,
  },
  restrictedItem: {
    required: false,
    type: Object as PropType<BaseRestrictedItem>,
  },
  locales: {
    required: true,
    type: Object as PropType<string[]>,
  },
});

const localeStore = useLocaleStore();
const slots = useSlots();

const translatableStringRef = toRef(props, 'translatableString');
const restrictedItemRef = toRef(props, 'restrictedItem');
const showDetailsRef = ref(false);

function offerDetails() {
  return !!slots['default'] && restrictedItemRef;
}

function addRestriction(value: string) {
  if (restrictedItemRef.value) {
    restrictedItemRef.value.restrictions.push(value);
  }
}

function deleteRestriction(value: string) {
  if (restrictedItemRef.value) {
    restrictedItemRef.value.restrictions.forEach((item, index) => {
      if (item === value && restrictedItemRef.value) {
        restrictedItemRef.value.restrictions.splice(index, 1);
      }
    });
  }
}
</script>

<style scoped></style>
