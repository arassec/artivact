<template>
  <q-dialog v-model="dialogModelRef" persistent>
    <q-card class="widget-editor-modal-card">
      <q-card-section class="text-white bg-primary">
        <div class="row">
          <div class="text-h6">
            Widget Editor
          </div>
        </div>
      </q-card-section>

      <q-card-section>
          <slot name="editor-preview"/>
      </q-card-section>

      <q-separator class="q-ml-sm q-mr-sm"/>

      <q-card-section>
          <slot name="editor-content"/>
      </q-card-section>

      <q-separator class="q-ml-sm q-mr-sm"/>

      <q-card-actions>
        <q-space/>
        <slot name="approve"/>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">

import {toRef} from 'vue';
import ArtivactContent from 'components/ArtivactContent.vue';

const props = defineProps({
  dialogModel: {
    required: true,
  }
});

const dialogModelRef = toRef(props, 'dialogModel');

defineEmits(['close-dialog'])

</script>

<style scoped>
.widget-editor-modal-card {
  max-width: 75rem;
  min-width: 75rem;
}
</style>
