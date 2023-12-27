<template>
  <div>
    <div
      :style="
        'min-width: 1em; min-height: 1em; background-color: ' + colorValue
      "
    ></div>
    <q-input
      v-model="colorRef"
      outlined
      :rules="['anyColor']"
      :readonly="true"
      :label="colorKey"
    >
      <template v-slot:append>
        <q-icon name="colorize" class="cursor-pointer">
          <q-popup-proxy cover transition-show="scale" transition-hide="scale">
            <q-color
              v-model="colorRef"
              @change="$emit('color-changed', keyRef, colorRef)"
            />
          </q-popup-proxy>
        </q-icon>
      </template>
    </q-input>
  </div>
</template>

<script setup lang="ts">
import {ref} from 'vue';

const props = defineProps({
  colorKey: {
    required: true,
    type: String,
  },
  colorValue: {
    required: true,
    type: String,
  },
});

const keyRef = ref(props.colorKey);
const colorRef = ref(props.colorValue);
</script>

<style scoped></style>
