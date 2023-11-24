<template>
  <model-viewer
    id="av-model-viewer"
    :src="modelUrl"
    camera-controls
    class="fit"
    :reveal="reveal"
    v-if="$q.platform.is.mobile && modelUrl"
  >
    <div
      id="button-load"
      slot="poster"
      class="download-div row justify-center items-center"
    >
      <q-btn icon="download" class="column" @click="reveal = 'auto'">{{
        $t("downloadModelButtonLabel")
      }}</q-btn>
    </div>
  </model-viewer>
  <model-viewer
    id="av-model-viewer"
    :src="modelUrl"
    camera-controls
    class="fit"
    v-if="!$q.platform.is.mobile && modelUrl"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue';

defineProps({
  modelUrl: {
    required: true,
    type: String,
  },
});

let reveal = ref('manual');
</script>

<style scoped>
.download-div {
  height: 100%;
}
</style>
