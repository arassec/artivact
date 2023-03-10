<template>
  <div v-if="tagsConfiguration != null && locales != null">
    <q-list bordered class="rounded-borders q-mb-lg">
      <q-expansion-item v-for="(tagEntry, index) of tagsConfiguration.tags" :key="index"
                        group="categories" header-class="bg-primary text-white" class="list-entry"
                        expand-separator expand-icon-class="text-white">
        <template v-slot:header>
          <q-item-section class="tag-label">
            {{ tagEntry.value }}
          </q-item-section>

          <q-item-section side>
            <q-btn round dense flat class="float-right" color="white"
                   icon="delete" size="md" @click="deleteTag(index)"></q-btn>
          </q-item-section>
        </template>

        <q-card>
          <q-card-section>
            <artivact-translatable-item-editor :locales="locales" :item="tagEntry" label="Prefix"
                                               :show-separator="false"/>
            <q-input outlined v-model="tagEntry.url" label="URL"/>
          </q-card-section>
        </q-card>
      </q-expansion-item>

    </q-list>

    <div class="row">
      <q-space></q-space>
      <q-btn label="Add Tag" @click="addTag" color="primary"/>
    </div>
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

function deleteTag(index: number) {
  if (tagsConfigurationRef.value && tagsConfigurationRef.value?.tags.length > index) {
    tagsConfigurationRef.value?.tags.splice(index, 1);
  }
}

</script>

<style scoped>

.list-entry {
  border-bottom: 1px solid white;
}

.tag-label {
  font-size: large;
}

</style>
