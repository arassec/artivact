<template>
  <div :class="!restrictedItemRef ? 'q-mb-md' : ''">

    <!-- Input field -->
    <q-input
      :data-test="dataTest"
      v-if="translatableStringRef && localeStore.selectedLocale === null"
      outlined
      v-model="translatableStringRef.value"
      :label="label"
      :type="textarea ? 'textarea' : 'text'"
      :autogrow="textarea"
      class="no-scroll"
      :disable="disable"
    />
    <div class="row full-width" v-if="translatableStringRef && localeStore.selectedLocale !== null">
      <q-input
        :data-test="dataTest"
        outlined
        v-model="translatableStringRef.translations[localeStore.selectedLocale]"
        :label="label"
        :type="textarea ? 'textarea' : 'text'"
        :autogrow="textarea"
        class="no-scroll column col-grow"
        :disable="disable"
      />
      <q-input
        v-if="showStandardTextRef"
        outlined
        v-model="translatableStringRef.value"
        :label="label"
        :type="textarea ? 'textarea' : 'text'"
        :autogrow="textarea"
        class="no-scroll column col-grow q-ml-md"
        :disable="true"
        transition-show="slide-left"
        transition-hide="slide-right"
      />
      <div class="q-ml-sm">
        <q-btn
          flat
          dense
          rounded
          icon="language"
          @click="showStandardTextRef = !showStandardTextRef"
        />
      </div>
    </div>

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
      v-if="restrictedItemRef && !showDetailsRef">
      <q-tooltip>{{ $t('ArtivactRestrictedTranslatableItemEditor.tooltip.more') }}</q-tooltip>
    </q-btn>
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
      v-if="restrictedItem && showDetailsRef">
      <q-tooltip>{{ $t('ArtivactRestrictedTranslatableItemEditor.tooltip.less') }}</q-tooltip>
    </q-btn>

    <div v-if="restrictedItem" v-show="showDetailsRef">
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

      <div v-if="!showSeparator" class="q-mb-md"/>
    </div>
    <q-separator class="q-mt-xs q-mb-lg" v-if="showSeparator"/>
  </div>
</template>

<script setup lang="ts">
import { PropType, ref, toRef } from 'vue';
import ArtivactRestrictionsEditor from 'components/ArtivactRestrictionsEditor.vue';
import { BaseRestrictedObject, TranslatableString } from 'components/artivact-models';
import { useLocaleStore } from 'stores/locale';

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
    type: Object as PropType<BaseRestrictedObject>,
  },
  dataTest: {
    required: false,
    type: String
  },
  disable: {
    required: false,
    type: Boolean,
    default: false
  }
});

const localeStore = useLocaleStore();

const translatableStringRef = toRef(props, 'translatableString');
const restrictedItemRef = toRef(props, 'restrictedItem');
const showDetailsRef = ref(false);

const showStandardTextRef = ref(true);

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
