<template>
  <artivact-dialog :dialog-model="dialogModelRef" :hide-buttons="true" v-if="progressMonitorRef">
    <template v-slot:header>
      Operation in Progress
    </template>

    <template v-slot:body>
      <q-card-section>
        <q-spinner size="2em" class="q-mr-md"/>
        {{ progressMonitorRef?.progress }}
      </q-card-section>
    </template>
  </artivact-dialog>
</template>

<script setup lang="ts">

import {PropType, toRef} from 'vue';
import ArtivactDialog from 'components/ArtivactDialog.vue';
import {OperationProgress} from 'components/models';

const props = defineProps({
  dialogModel: {
    required: true,
  },
  progressMonitorRef: {
    required: false,
    type: Object as PropType<OperationProgress>
  }
});

const dialogModelRef = toRef(props, 'dialogModel');

</script>

<style scoped>

</style>
