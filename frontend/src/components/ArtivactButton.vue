<template>
  <a :href="config.targetUrl">
    <q-btn
      class="q-mb-md"
      no-caps
      :icon="
        config.iconLeft && config.iconLeft.length > 0
          ? config.iconLeft
          : undefined
      "
      :label="translate(config.label)"
      :icon-right="
        config.iconRight && config.iconRight.length > 0
          ? config.iconRight
          : undefined
      "
      :style="'font-size: ' + config.size + 'rem'"
      :color="config.buttonColor"
      :text-color="config.textColor"
    />
  </a>
</template>

<script setup lang="ts">
import { PropType } from 'vue';
import { ButtonConfig } from './artivact-models';
import { translate } from './artivact-utils';

defineProps({
  config: {
    required: false,
    type: Object as PropType<ButtonConfig>,
    default: {
      targetUrl: '',
      iconLeft: '',
      iconRight: '',
      label: '',
      size: 1,
      buttonColor: 'primary',
      textColor: 'white',
    },
  },
});
</script>

<style scoped></style>
