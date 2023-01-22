<template>
  <div v-if="licenseConfiguration != null && locales != null">
    <q-card class="q-mb-lg">
      <q-card-section class="bg-primary text-white">
        <div class="text-h6">Configures the license String displayed on the artivacts details-page.</div>
      </q-card-section>

      <q-card-section>
        <artivact-translatable-item-editor :locales="locales" :item="licenseConfiguration.prefix" label="Prefix"
                                           :show-separator="false"/>
      </q-card-section>

      <q-card-section>
        <artivact-translatable-item-editor :locales="locales" :item="licenseConfiguration.licenseLabel" label="License"
                                           :show-separator="false"/>
      </q-card-section>

      <q-card-section>
        <artivact-translatable-item-editor :locales="locales" :item="licenseConfiguration.suffix" label="Suffix"
                                           :show-separator="false"/>
      </q-card-section>

      <q-card-section>
        <q-input outlined v-model="licenseConfigurationProp.licenseUrl" label="License-URL"/>
      </q-card-section>
    </q-card>
  </div>
</template>

<script setup lang="ts">


import {PropType, toRef} from 'vue';
import ArtivactTranslatableItemEditor from 'components/ArtivactTranslatableItemEditor.vue';
import {LicenseConfiguration} from 'components/models';

const props = defineProps({
  licenseConfiguration: {
    required: false,
    type: Object as PropType<LicenseConfiguration | null>
  },
  locales: {
    required: true,
    type: Object as PropType<string[]>
  }
});

const licenseConfigurationProp = toRef(props, 'licenseConfiguration');

</script>

<style scoped>

</style>
