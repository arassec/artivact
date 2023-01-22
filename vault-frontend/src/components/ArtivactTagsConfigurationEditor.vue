<template>
  <div v-if="tagsConfiguration != null && locales != null">
    <template v-for="(tagEntry, index) of tagsConfiguration.tags" :key="index">

      <q-card class="q-mb-lg">
        <q-card-section class="bg-primary text-white">
          <div class="text-h6">{{ tagEntry.value }}</div>
        </q-card-section>

        <q-card-section>
          <artivact-translatable-item-editor :locales="locales" :item="tagEntry" label="Prefix"
                                             :show-separator="false"/>
        </q-card-section>

        <q-card-section>
          <q-input outlined v-model="tagEntry.url" label="URL"/>
        </q-card-section>
      </q-card>

    </template>

    <q-btn label="Add Tag" @click="addTag" color="primary"/>

  </div>

</template>

<script setup lang="ts">

import {PropType, toRef} from 'vue';
import {TagsConfiguration, TranslatedTag} from 'components/models';
import ArtivactTranslatableItemEditor from 'components/ArtivactTranslatableItemEditor.vue';

const props = defineProps({
  tagsConfiguration: {
    required: false,
    type: Object as PropType<TagsConfiguration | null>
  },
  locales: {
    required: true,
    type: Object as PropType<string[]>
  }
});

const tagsConfigurationRef = toRef(props, 'tagsConfiguration');

function addTag() {
  if (tagsConfigurationRef.value) {
    let tag: TranslatedTag = {
      id: '',
      value: 'New Tag',
      translations: {},
      restrictions: [],
      translatedValue: '',
      url: ''
    };
    tagsConfigurationRef.value.tags.push(tag);
  }
}


</script>

<style scoped>

</style>
