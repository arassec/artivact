<template>
  <div v-if="licenseConfiguration != null && locales != null">
    <div class="q-mb-lg">
      {{ $t('ArtivactLicenseConfigurationEditor.description') }}
      <br class="q-mb-sm" />
      {{ $t('ArtivactLicenseConfigurationEditor.descriptionSuffix') }}
    </div>

    <q-card class="q-mb-lg">
      <q-card-section class="bg-primary text-white">
        <div class="text-h6">{{ $t('ArtivactLicenseConfigurationEditor.card.heading') }}</div>
      </q-card-section>

      <q-card-section>
        <label>{{ $t('ArtivactLicenseConfigurationEditor.card.prefixDescription') }}</label>
        <artivact-restricted-translatable-item-editor
          :locales="locales"
          :translatable-string="licenseConfiguration.prefix"
          :label="$t('ArtivactLicenseConfigurationEditor.card.prefixLabel')"
          :show-separator="false"
        />
      </q-card-section>

      <q-card-section>
        <label>{{ $t('ArtivactLicenseConfigurationEditor.card.licenseDescription') }}</label>
        <artivact-restricted-translatable-item-editor
          :locales="locales"
          :translatable-string="licenseConfiguration.licenseLabel"
          :label="$t('ArtivactLicenseConfigurationEditor.card.licenseLabel')"
          :show-separator="false"
        />
      </q-card-section>

      <q-card-section>
        <label>{{ $t('ArtivactLicenseConfigurationEditor.card.suffixDescription') }}</label>
        <artivact-restricted-translatable-item-editor
          :locales="locales"
          :translatable-string="licenseConfiguration.suffix"
          :label="$t('ArtivactLicenseConfigurationEditor.card.suffixLabel')"
          :show-separator="false"
        />
      </q-card-section>

      <q-card-section>
        <label>{{ $t('ArtivactLicenseConfigurationEditor.card.urlDescription') }}</label>
        <q-input
          outlined
          v-model="licenseConfigurationRef.licenseUrl"
          :label="$t('ArtivactLicenseConfigurationEditor.card.urlLabel')"
        />
      </q-card-section>
    </q-card>
  </div>
</template>

<script setup lang="ts">
import {PropType, toRef} from 'vue';
import {LicenseConfiguration} from 'components/artivact-models';
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
