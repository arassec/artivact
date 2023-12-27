<template>
  <div v-if="appearanceConfigurationRef">
    <div class="q-mb-lg">
      Configures the appearance of the Artivact application.
    </div>

    <q-list bordered class="rounded-borders q-mb-lg">
      <q-expansion-item
        header-class="bg-primary text-white"
        class="list-entry"
        expand-separator
        expand-icon-class="text-white"
      >
        <template v-slot:header>
          <q-item-section class="list-entry-label">
            Application Title
          </q-item-section>
        </template>
        <q-card class="q-mb-lg">
          <q-card-section>
            <div class="q-mb-md">
              The title is displayed in the browser tab on every page.
            </div>
            <q-input
              outlined
              label="Application Title"
              v-model="appearanceConfigurationRef.applicationTitle"
            ></q-input>
          </q-card-section>
        </q-card>
      </q-expansion-item>

      <q-expansion-item
        header-class="bg-primary text-white"
        class="list-entry"
        expand-separator
        expand-icon-class="text-white"
      >
        <template v-slot:header>
          <q-item-section class="list-entry-label">
            Supported Locales
          </q-item-section>
        </template>
        <q-card class="q-mb-lg">
          <q-card-section>
            <div class="q-mb-md">
              Comma-separated list of locales supported by this installation.
              E.g. 'de,nl,ja'. Those locales can be maintained in addition to
              the default locale by choosing it from the settings menu and
              editing translatable strings e.g. on pages.
            </div>
            <q-input
              outlined
              label="Locales"
              v-model="appearanceConfigurationRef.availableLocales"
            ></q-input>
          </q-card-section>
        </q-card>
      </q-expansion-item>

      <q-expansion-item
        header-class="bg-primary text-white"
        class="list-entry"
        expand-separator
        expand-icon-class="text-white"
      >
        <template v-slot:header>
          <q-item-section class="list-entry-label">
            Color Theme
          </q-item-section>
        </template>
        <q-card class="q-mb-lg">
          <q-card-section>
            <div class="q-mb-md">
              The color theme can be customized with the following settings.
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
        header-class="bg-primary text-white"
        class="list-entry"
        expand-separator
        expand-icon-class="text-white"
      >
        <template v-slot:header>
          <q-item-section class="list-entry-label"> Favicon </q-item-section>
        </template>
        <q-card class="q-mb-lg">
          <q-card-section>
            <div class="q-mb-md">
              A custom Favicon can be uploaded here in sizes of 16x16 and 32x32
              pixels.
              <strong
                >The uploaded file is directly installed as favicon!</strong
              >
              Due to browser caching the newly uploaded favicon might not
              directly be visible in your browser tab.
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
import {AppearanceConfiguration} from 'components/models';
import ArtivactThemeColorEditor from 'components/ArtivactThemeColorEditor.vue';
import {QUploader, useQuasar} from 'quasar';

const props = defineProps({
  appearanceConfiguration: {
    required: false,
    type: Object as PropType<AppearanceConfiguration | null>,
  },
});

const quasar = useQuasar();

const appearanceConfigurationRef = toRef(props, 'appearanceConfiguration');

function uploadComplete() {
  quasar.notify({
    color: 'positive',
    position: 'bottom',
    message: 'Favicon saved',
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
