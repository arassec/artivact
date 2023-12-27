<template>
  <div v-if="licenseConfiguration != null && locales != null">
    <div class="q-mb-lg">
      Configures the license string displayed on the artivacts details-page. The
      license is composed of a prefix, the actual license and a suffix. If an
      URL is configured, the license will be rendered as link to the provided
      URL.
      <br class="q-mb-sm" />
      The final license text might look like: "Media files are provided under
      'CC BY-SA' license."
    </div>

    <q-card class="q-mb-lg">
      <q-card-section class="bg-primary text-white">
        <div class="text-h6">License</div>
      </q-card-section>

      <q-card-section>
        <label
          >The first part of the license text, e.g. 'Media files are provided
          under'.</label
        >
        <artivact-restricted-translatable-item-editor
          :locales="locales"
          :translatable-string="licenseConfiguration.prefix"
          label="Prefix"
          :show-separator="false"
        />
      </q-card-section>

      <q-card-section>
        <label>The name of the license, e.g. 'MIT' or 'CC BY-SA'.</label>
        <artivact-restricted-translatable-item-editor
          :locales="locales"
          :translatable-string="licenseConfiguration.licenseLabel"
          label="License"
          :show-separator="false"
        />
      </q-card-section>

      <q-card-section>
        <label>The last part of the license text, e.g. 'license'.</label>
        <artivact-restricted-translatable-item-editor
          :locales="locales"
          :translatable-string="licenseConfiguration.suffix"
          label="Suffix"
          :show-separator="false"
        />
      </q-card-section>

      <q-card-section>
        <label
          >If provided, this URL will be used to make the license text into a
          link.</label
        >
        <q-input
          outlined
          v-model="licenseConfigurationRef.licenseUrl"
          label="License-URL"
        />
      </q-card-section>
    </q-card>
  </div>
</template>

<script setup lang="ts">
import {PropType, toRef} from 'vue';
import {LicenseConfiguration} from 'components/models';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';

const props = defineProps({
  licenseConfiguration: {
    required: false,
    type: Object as PropType<LicenseConfiguration | null>,
  },
  locales: {
    required: true,
    type: Object as PropType<string[]>,
  },
});

const licenseConfigurationRef = toRef(props, 'licenseConfiguration');
</script>

<style scoped></style>
