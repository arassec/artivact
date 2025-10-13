<template>
  <q-dialog v-model="dialogModelRef" persistent>
    <!-- DESKTOP RESOLUTION -->
    <q-card
      :data-test="dataTest"
      class="q-mb-lg gt-sm"
      :style="'min-width: ' + minWidth + 'em;'"
    >
      <q-card-section
        class="text-white"
        :class="error ? 'bg-negative' : warn ? 'bg-warning' : 'bg-primary'"
      >
        <div class="row">
          <div class="text-h6" :class="showCloseButton ? '' : 'full-width'">
            <slot name="header" />
          </div>
          <q-space />
          <q-btn
            v-if="showCloseButton"
            icon="close"
            flat
            round
            dense
            @click="$emit('close-dialog')"
          />
        </div>
      </q-card-section>

      <div class="fit">
        <slot name="body" />
      </div>

      <q-separator class="q-mr-sm q-ml-sm" v-if="!hideButtons" />

      <q-card-actions v-if="!hideButtons">
        <slot name="cancel" />
        <q-space />
        <slot name="approve" />
      </q-card-actions>
    </q-card>

    <!-- MOBILE RESOLUTION -->
    <q-card class="q-mb-lg lt-md">
      <q-card-section
        class="text-white"
        :class="error ? 'bg-negative' : warn ? 'bg-warning' : 'bg-primary'"
      >
        <div class="row">
          <div class="text-h6">
            <slot name="header" />
          </div>
          <q-space />
          <q-btn
            v-if="showCloseButton"
            icon="close"
            flat
            round
            dense
            @click="$emit('close-dialog')"
          />
        </div>
      </q-card-section>

      <div class="fit">
        <slot name="body" />
      </div>

      <q-separator class="q-mr-sm q-ml-sm" v-if="!hideButtons" />

      <q-card-actions v-if="!hideButtons">
        <slot name="cancel" />
        <q-space />
        <slot name="approve" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { toRef } from 'vue';

const props = defineProps({
  dialogModel: {
    required: true,
    type: Boolean,
  },
  hideButtons: {
    required: false,
  },
  showCloseButton: {
    required: false,
  },
  warn: {
    required: false,
  },
  error: {
    required: false,
  },
  minWidth: {
    required: false,
    default: 35,
  },
  dataTest: {
    required: false,
    default: 'artivact-modal',
    type: String,
  },
});

const dialogModelRef = toRef(props, 'dialogModel');

defineEmits(['close-dialog']);
</script>

<style scoped></style>
