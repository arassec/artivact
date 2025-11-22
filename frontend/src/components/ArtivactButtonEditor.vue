<template>
  <div class="row" v-if="config">
    <div class="col q-mr-md">
      <q-input
        type="text"
        outlined
        v-model="config.targetUrl"
        class="q-mb-md full-width"
        :label="$t('ArtivactButtonEditor.label.targetUrl')"
      />
      <q-input
        type="text"
        outlined
        v-model="config.iconLeft"
        class="q-mb-md full-width"
        :label="$t('ArtivactButtonEditor.label.iconLeft')"
      />
      <q-input
        type="text"
        outlined
        v-model="config.textColor"
        class="q-mb-md full-width"
        :label="$t('ArtivactButtonEditor.label.textColor')"
      />
      <q-input
        type="number"
        outlined
        v-model="config.size"
        class="q-mb-md full-width"
        :label="$t('ArtivactButtonEditor.label.size')"
      />
    </div>
    <div class="col">
      <artivact-restricted-translatable-item-editor
        :locales="localeStore.locales"
        :translatable-string="config.label"
        :label="$t('ArtivactButtonEditor.label.label')"
        :show-separator="false"
      />
      <q-input
        type="text"
        outlined
        v-model="config.iconRight"
        class="q-mb-md full-width"
        :label="$t('ArtivactButtonEditor.label.iconRight')"
      />
      <q-input
        type="text"
        outlined
        v-model="config.buttonColor"
        class="q-mb-md full-width"
        :label="$t('ArtivactButtonEditor.label.buttonColor')"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {ButtonConfig} from './artivact-models';
import ArtivactRestrictedTranslatableItemEditor from './ArtivactRestrictedTranslatableItemEditor.vue';
import {useLocaleStore} from '../stores/locale';

const config = defineModel<ButtonConfig>('config', {required: true});

const localeStore = useLocaleStore();
</script>

<style scoped></style>
