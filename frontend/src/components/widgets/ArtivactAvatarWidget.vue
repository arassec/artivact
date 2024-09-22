<template>
  <artivact-widget-template
    :move-down-enabled="moveDownEnabled"
    :move-up-enabled="moveUpEnabled"
    :in-edit-mode="inEditMode"
    v-if="widgetDataRef"
    :restrictions="widgetDataRef.restrictions"
    :navigation-title="widgetDataRef.navigationTitle"
  >
    <template v-slot:widget-content>
      <artivact-content>
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
      </artivact-content>
    </template>

    <template v-slot:widget-editor-preview>
      <artivact-content>
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
            <template v-if="widgetData.avatarSubtext">
              <div
                v-if="localeStore.selectedLocale === null"
                class="av-text-h2"
              >
                {{ widgetData.avatarSubtext.value }}
              </div>
              <div
                v-if="
                  localeStore.selectedLocale !== null &&
                  widgetData.avatarSubtext.translations[
                    localeStore.selectedLocale
                  ]
                "
                class="av-text-h2"
              >
                {{
                  widgetData.avatarSubtext.translations[
                    localeStore.selectedLocale
                  ]
                }}
              </div>
              <div
                v-if="
                  localeStore.selectedLocale !== null &&
                  !widgetData.avatarSubtext.translations[
                    localeStore.selectedLocale
                  ]
                "
                class="av-text-h2 text-red"
              >
                {{ widgetData.avatarSubtext.value }}
              </div>
            </template>
            <q-space />
          </div>
        </div>
      </artivact-content>
    </template>

    <template v-slot:widget-editor-content>
      <artivact-content>
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
      </artivact-content>
    </template>
  </artivact-widget-template>
</template>

<script setup lang="ts">
import {PropType, toRef} from 'vue';
import {AvatarWidgetData} from 'components/widgets/artivact-widget-models';
import ArtivactContent from 'components/ArtivactContent.vue';
import {QUploader} from 'quasar';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';
import {useLocaleStore} from 'stores/locale';
import {translate} from '../artivact-utils';
import ArtivactWidgetTemplate from 'components/widgets/ArtivactWidgetTemplate.vue';

const props = defineProps({
  inEditMode: {
    required: true,
    type: Boolean,
  },
  moveUpEnabled: {
    required: true,
    type: Boolean,
  },
  moveDownEnabled: {
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

const localeStore = useLocaleStore();

const pageIdRef = toRef(props, 'pageId');
const widgetDataRef = toRef(props, 'widgetData');
</script>

<style scoped></style>
