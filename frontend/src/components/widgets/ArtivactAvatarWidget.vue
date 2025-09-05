<template>
  <artivact-widget-template
    v-if="widgetDataRef"
    :editing="editingRef"
    :in-edit-mode="inEditMode"
    :restrictions="widgetDataRef.restrictions"
    :navigation-title="widgetDataRef.navigationTitle"
    @start-editing="editingRef = true"
    @stop-editing="editingRef = false"
  >
    <template v-slot:widget-content>
      <div class="column full-width">
        <div class="row">
          <q-space />
          <q-avatar
            size="256px"
            v-if="widgetData.avatarImage !== ''"
            class="shadow-5 q-mb-sm"
            color="primary"
          >
            <img
              alt="avatar-image"
              :src="
                '/api/page/widget/' +
                widgetDataRef.id +
                '/' +
                widgetDataRef.avatarImage +
                '?imageSize=ITEM_CARD'
              "
            />
          </q-avatar>
          <q-space />
        </div>
        <div class="row">
          <q-space />
          <div v-if="widgetData.avatarSubtext" class="av-text-h2">
            {{ translate(widgetData.avatarSubtext) }}
          </div>
          <q-space />
        </div>
      </div>
    </template>

    <template v-slot:widget-editor>
      <div class="column full-width">
        <q-uploader
          :label="$t('AvatarWidget.label.image')"
          auto-upload
          field-name="file"
          :no-thumbnails="true"
          class="q-mb-md"
          :url="'/api/page/' + pageIdRef + '/widget/' + widgetDataRef.id"
          @uploaded="$emit('image-added', 'avatarImage')"
          ref="imageUploader"
        >
        </q-uploader>
        <artivact-restricted-translatable-item-editor
          :locales="localeStore.locales"
          :translatable-string="widgetDataRef.avatarSubtext"
          :label="$t('AvatarWidget.label.subtext')"
          :show-separator="false"
        />
      </div>
    </template>
  </artivact-widget-template>
</template>

<script setup lang="ts">
import { PropType, ref, toRef } from 'vue';
import { AvatarWidgetData } from './artivact-widget-models';
import { QUploader } from 'quasar';
import ArtivactRestrictedTranslatableItemEditor from '../../components/ArtivactRestrictedTranslatableItemEditor.vue';
import { useLocaleStore } from '../../stores/locale';
import { translate } from '../artivact-utils';
import ArtivactWidgetTemplate from '../../components/widgets/ArtivactWidgetTemplate.vue';

const props = defineProps({
  inEditMode: {
    required: true,
    type: Boolean,
  },
  pageId: {
    required: true,
    type: String,
  },
  widgetData: {
    required: true,
    type: Object as PropType<AvatarWidgetData>,
  },
});

const editingRef = ref(false);

const localeStore = useLocaleStore();

const pageIdRef = toRef(props, 'pageId');
const widgetDataRef = toRef(props, 'widgetData');
</script>

<style scoped></style>
