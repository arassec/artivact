<template>
  <div v-if="tagsConfiguration && locales">
    <q-list bordered class="rounded-borders q-mb-lg">
      <q-expansion-item
        group="tags"
        v-for="(tagEntry, index) of tagsConfiguration.tags"
        :key="index"
        header-class="bg-primary text-white"
        class="list-entry"
        expand-separator
        expand-icon-class="text-white"
      >
        <template v-slot:header>
          <q-item-section class="tag-label">
            {{ tagEntry.value }}
          </q-item-section>

          <q-item-section side>
            <q-btn
              round
              dense
              flat
              class="float-right"
              color="white"
              icon="delete"
              size="md"
              @click="deleteTag(index)">
              <q-tooltip>{{ $t('ArtivactTagsConfigurationEditor.tooltip.delete') }}</q-tooltip>
            </q-btn>
          </q-item-section>
        </template>

        <q-card>
          <q-card-section>
            <artivact-restricted-translatable-item-editor
              :locales="locales"
              :translatable-string="tagEntry"
              :restricted-item="tagEntry"
              :label="$t('Common.items.tag')"
              :show-separator="false"
            />
            <q-input
              outlined
              v-model="tagEntry.url"
              :label="$t('ArtivactTagsConfigurationEditor.url')"
              class="q-mb-sm"
            />
            <q-checkbox
              label="Default tag for new items?"
              v-model="tagEntry.defaultTag"
            ></q-checkbox>
          </q-card-section>
        </q-card>
      </q-expansion-item>
    </q-list>

    <div class="row">
      <q-space></q-space>
      <q-btn :label="$t('ArtivactTagsConfigurationEditor.addTag')" @click="addTag" color="primary" />
    </div>
  </div>
</template>

<script setup lang="ts">
import {PropType, toRef} from 'vue';
import {Tag, TagsConfiguration} from 'components/models';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';
import {useI18n} from 'vue-i18n';

const i18n = useI18n();

const props = defineProps({
  tagsConfiguration: {
    required: false,
    type: Object as PropType<TagsConfiguration | null>,
  },
  locales: {
    required: true,
    type: Object as PropType<string[]>,
  },
});

const tagsConfigurationRef = toRef(props, 'tagsConfiguration');

function addTag() {
  if (tagsConfigurationRef.value) {
    let tag: Tag = {
      id: '',
      value: i18n.t('ArtivactTagsConfigurationEditor.newTag'),
      translations: {},
      restrictions: [],
      translatedValue: '',
      url: '',
      defaultTag: false,
    };
    tagsConfigurationRef.value.tags.push(tag);
  }
}

function deleteTag(index: number) {
  if (
    tagsConfigurationRef.value &&
    tagsConfigurationRef.value?.tags.length > index
  ) {
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
