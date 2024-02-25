<template>
  <div v-if="progressMonitorRef">
    <artivact-dialog :dialog-model="dialogModelRef && progressMonitorRef?.error == null" :hide-buttons="true">
      <template v-slot:header>
        {{ $t('ArtivactOperationInProgressDialog.heading') }}
      </template>

      <template v-slot:body>
        <q-card-section>
          <q-spinner size="2em" class="q-mr-md"/>
          {{ progressMonitorRef?.progress }}
        </q-card-section>
      </template>
    </artivact-dialog>

    <artivact-dialog :dialog-model="dialogModelRef && progressMonitorRef?.error != null"
                     :hide-buttons="true" :show-close-button="true" :error="true"
                     @close-dialog="$emit('close-dialog')">
      <template v-slot:header>
        {{ $t('ArtivactOperationInProgressDialog.failedHeading') }}
      </template>

      <template v-slot:body>
        <q-card-section>
          {{ progressMonitorRef?.progress }}
          <q-separator class="q-mt-lg q-mb-xs"/>
          <q-expansion-item :v-model="showDetailsRef" :label="$t('ArtivactOperationInProgressDialog.details')">
            <q-scroll-area style="height: 25em; max-width: 35em;">
              <pre>{{ progressMonitorRef?.error }}</pre>
            </q-scroll-area>
          </q-expansion-item>
        </q-card-section>
      </template>
    </artivact-dialog>
  </div>
</template>

<script setup lang="ts">

import {PropType, ref, toRef} from 'vue';
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

defineEmits(['close-dialog'])

const dialogModelRef = toRef(props, 'dialogModel');
const showDetailsRef = ref(false);

</script>

<style scoped>

</style>
