<template>
  <div v-if="appearanceConfigurationRef">
    <div class="q-mb-lg">
      {{ $t('ArtivactAppearanceConfigurationEditor.description') }}
    </div>

    <q-list bordered class="rounded-borders q-mb-lg">
      <q-expansion-item
        group="appearanceConfig"
        v-if="!desktopStore.isDesktopModeEnabled"
        header-class="bg-primary text-white"
        class="list-entry"
        expand-separator
        expand-icon-class="text-white"
      >
        <template v-slot:header>
          <q-item-section class="list-entry-label">
            {{ $t('ArtivactAppearanceConfigurationEditor.list.title.heading') }}
          </q-item-section>
        </template>
        <q-card class="q-mb-lg">
          <q-card-section>
            <div class="q-mb-md">
              {{ $t('ArtivactAppearanceConfigurationEditor.list.title.description') }}
            </div>
            <q-input
              outlined
              :label="$t('ArtivactAppearanceConfigurationEditor.list.title.label')"
              v-model="appearanceConfigurationRef.applicationTitle"
            ></q-input>
          </q-card-section>
        </q-card>
      </q-expansion-item>

      <q-expansion-item
        group="appearanceConfig"
        header-class="bg-primary text-white"
        class="list-entry"
        expand-separator
        expand-icon-class="text-white"
      >
        <template v-slot:header>
          <q-item-section class="list-entry-label">
            {{ $t('ArtivactAppearanceConfigurationEditor.list.locales.heading') }}
          </q-item-section>
        </template>
        <q-card class="q-mb-lg">
          <q-card-section>
            <div class="q-mb-md">
              {{ $t('ArtivactAppearanceConfigurationEditor.list.locales.description') }}
            </div>
            <q-input
              outlined
              :label="$t('ArtivactAppearanceConfigurationEditor.list.locales.label')"
              v-model="appearanceConfigurationRef.availableLocales"
            ></q-input>
          </q-card-section>
        </q-card>
      </q-expansion-item>

      <q-expansion-item
        group="appearanceConfig"
        header-class="bg-primary text-white"
        class="list-entry"
        expand-separator
        expand-icon-class="text-white"
        v-if="desktopStore.isDesktopModeEnabled"
      >
        <template v-slot:header>
          <q-item-section class="list-entry-label">
            {{ $t('ArtivactAppearanceConfigurationEditor.list.applicationLocale.heading') }}
          </q-item-section>
        </template>
        <q-card class="q-mb-lg">
          <q-card-section>
            <div class="q-mb-md">
              {{ $t('ArtivactAppearanceConfigurationEditor.list.applicationLocale.description') }}
            </div>
            <q-select
              outlined
              emit-value
              v-model="appearanceConfigurationRef.applicationLocale"
              :options="availableApplicationLocaleOptions"/>
          </q-card-section>
        </q-card>
      </q-expansion-item>

      <q-expansion-item
        group="appearanceConfig"
        header-class="bg-primary text-white"
        class="list-entry"
        expand-separator
        expand-icon-class="text-white"
      >
        <template v-slot:header>
          <q-item-section class="list-entry-label">
            {{ $t('ArtivactAppearanceConfigurationEditor.list.colors.heading') }}
          </q-item-section>
        </template>
        <q-card class="q-mb-lg">
          <q-card-section>
            <div class="q-mb-md">
              {{ $t('ArtivactAppearanceConfigurationEditor.list.colors.description') }}
            </div>

            <artivact-theme-color-editor
              :colorKey="'primary'"
              :colorValue="appearanceConfigurationRef.colorTheme.primary"
              @color-changed="
                (colorKey, colorValue) =>
                  $emit('color-changed', colorKey, colorValue)
              "
            />
            <artivact-theme-color-editor
              :colorKey="'secondary'"
              :colorValue="appearanceConfigurationRef.colorTheme.secondary"
              @color-changed="
                (colorKey, colorValue) =>
                  $emit('color-changed', colorKey, colorValue)
              "
            />
            <artivact-theme-color-editor
              :colorKey="'accent'"
              :colorValue="appearanceConfigurationRef.colorTheme.accent"
              @color-changed="
                (colorKey, colorValue) =>
                  $emit('color-changed', colorKey, colorValue)
              "
            />
            <artivact-theme-color-editor
              :colorKey="'dark'"
              :colorValue="appearanceConfigurationRef.colorTheme.dark"
              @color-changed="
                (colorKey, colorValue) =>
                  $emit('color-changed', colorKey, colorValue)
              "
            />
            <artivact-theme-color-editor
              :colorKey="'positive'"
              :colorValue="appearanceConfigurationRef.colorTheme.positive"
              @color-changed="
                (colorKey, colorValue) =>
                  $emit('color-changed', colorKey, colorValue)
              "
            />
            <artivact-theme-color-editor
              :colorKey="'negative'"
              :colorValue="appearanceConfigurationRef.colorTheme.negative"
              @color-changed="
                (colorKey, colorValue) =>
                  $emit('color-changed', colorKey, colorValue)
              "
            />
            <artivact-theme-color-editor
              :colorKey="'info'"
              :colorValue="appearanceConfigurationRef.colorTheme.info"
              @color-changed="
                (colorKey, colorValue) =>
                  $emit('color-changed', colorKey, colorValue)
              "
            />
            <artivact-theme-color-editor
              :colorKey="'warning'"
              :colorValue="appearanceConfigurationRef.colorTheme.warning"
              @color-changed="
                (colorKey, colorValue) =>
                  $emit('color-changed', colorKey, colorValue)
              "
            />
          </q-card-section>
        </q-card>
      </q-expansion-item>

      <q-expansion-item
        group="appearanceConfig"
        v-if="!desktopStore.isDesktopModeEnabled"
        header-class="bg-primary text-white"
        class="list-entry"
        expand-separator
        expand-icon-class="text-white"
      >
        <template v-slot:header>
          <q-item-section class="list-entry-label">
            {{ $t('ArtivactAppearanceConfigurationEditor.list.favicon.heading') }}
          </q-item-section>
        </template>
        <q-card class="q-mb-lg">
          <q-card-section>
            <div class="q-mb-md">
              {{ $t('ArtivactAppearanceConfigurationEditor.list.favicon.descriptionPrefix') }}
              <strong>{{ $t('ArtivactAppearanceConfigurationEditor.list.favicon.description') }}</strong>
              {{ $t('ArtivactAppearanceConfigurationEditor.list.favicon.descriptionSuffix') }}
            </div>
            <div class="row">
              <q-uploader
                label="16x16 Pixel Favicon"
                :multiple="false"
                accept="ico"
                max-file-size="5120"
                auto-upload
                field-name="file"
                :no-thumbnails="true"
                class="q-mr-lg"
                :url="'/api/configuration/favicon/small'"
                ref="imageUploader"
                @uploaded="
                  uploadComplete();
                  $emit('favicon-uploaded');
                "
              >
              </q-uploader>
              <q-uploader
                label="32x32 Pixel Favicon"
                :multiple="false"
                accept="ico"
                max-file-size="10240"
                auto-upload
                field-name="file"
                :no-thumbnails="true"
                :url="'/api/configuration/favicon/large'"
                ref="imageUploader"
                @uploaded="
                  uploadComplete();
                  $emit('favicon-uploaded');
                "
              >
              </q-uploader>
            </div>
          </q-card-section>
        </q-card>
      </q-expansion-item>
    </q-list>
  </div>
</template>

<script setup lang="ts">
import {PropType, toRef} from 'vue';
import {AdapterImplementation, AppearanceConfiguration, SelectboxModel} from 'components/models';
import ArtivactThemeColorEditor from 'components/ArtivactThemeColorEditor.vue';
import {QUploader, useQuasar} from 'quasar';
import {useDesktopStore} from 'stores/desktop';
import {useI18n} from 'vue-i18n';

const props = defineProps({
  appearanceConfiguration: {
    required: false,
    type: Object as PropType<AppearanceConfiguration | null>,
  },
});

const quasar = useQuasar();
const i18n = useI18n();

const desktopStore = useDesktopStore();

const appearanceConfigurationRef = toRef(props, 'appearanceConfiguration');

const availableApplicationLocaleOptions: SelectboxModel[] = [
  {
    label: i18n.t('ArtivactAppearanceConfigurationEditor.list.applicationLocale.system'),
    value: '',
    disable: false
  },
  {
    label: i18n.t('ArtivactAppearanceConfigurationEditor.list.applicationLocale.en'),
    value: 'en',
    disable: false
  },
  {
    label: i18n.t('ArtivactAppearanceConfigurationEditor.list.applicationLocale.de'),
    value: 'de',
    disable: false
  }
];

function uploadComplete() {
  quasar.notify({
    color: 'positive',
    position: 'bottom',
    message: i18n.t('ArtivactAppearanceConfigurationEditor.list.favicon.saved'),
    icon: 'check',
  });
}
</script>

<style scoped>
.list-entry {
  border-bottom: 1px solid white;
}

.list-entry-label {
  font-size: large;
}
</style>
