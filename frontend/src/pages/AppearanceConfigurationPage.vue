<template>
  <ArtivactContent>
    <div class="full-width">
      <h1 class="av-text-h1">{{$t('AppearanceConfigurationPage.heading')}}</h1>
      <artivact-appearance-configuration-editor
        :appearance-configuration="appearanceConfigurationRef"
        @color-changed="updateColor"
        @favicon-uploaded="loadAppearanceConfiguration"
      />
      <q-btn
        :label="$t('save')"
        color="primary"
        class="q-mb-lg float-right"
        @click="saveAppearanceConfiguration()"
      />
    </div>
  </ArtivactContent>
</template>

<script setup lang="ts">
import ArtivactContent from 'components/ArtivactContent.vue';
import { api } from 'boot/axios';
import { setCssVar, useQuasar } from 'quasar';
import { onMounted, ref, Ref } from 'vue';
import { AppearanceConfiguration } from 'components/models';
import ArtivactAppearanceConfigurationEditor from 'components/ArtivactAppearanceConfigurationEditor.vue';
import { useLocaleStore } from 'stores/locale';
import {useI18n} from 'vue-i18n';

const quasar = useQuasar();
const i18n = useI18n();

const appearanceConfigurationRef: Ref<AppearanceConfiguration | null> =
  ref(null);

const localeStore = useLocaleStore();

function loadAppearanceConfiguration() {
  api
    .get('/api/configuration/appearance')
    .then((response) => {
      appearanceConfigurationRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.configuration.appearance')}),
        icon: 'report_problem',
      });
    });
}

function saveAppearanceConfiguration() {
  api
    .post('/api/configuration/appearance', appearanceConfigurationRef.value)
    .then(() => {
      if (appearanceConfigurationRef.value) {
        document.title = appearanceConfigurationRef.value?.applicationTitle;

        if (appearanceConfigurationRef.value?.applicationLocale) {
          i18n.locale.value = appearanceConfigurationRef.value?.applicationLocale;
        }

        api
          .get('/api/configuration/public/locale')
          .then((response) => {
            localeStore.setAvailableLocales(response.data);
          })
          .catch(() => {
            quasar.notify({
              color: 'negative',
              position: 'bottom',
              message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.locales')}),
              icon: 'report_problem',
            });
          });

        setCssVar(
          'primary',
          appearanceConfigurationRef.value.colorTheme.primary
        );
        setCssVar(
          'secondary',
          appearanceConfigurationRef.value.colorTheme.secondary
        );
        setCssVar('accent', appearanceConfigurationRef.value.colorTheme.accent);
        setCssVar('dark', appearanceConfigurationRef.value.colorTheme.dark);
        setCssVar(
          'positive',
          appearanceConfigurationRef.value.colorTheme.positive
        );
        setCssVar(
          'negative',
          appearanceConfigurationRef.value.colorTheme.negative
        );
        setCssVar('info', appearanceConfigurationRef.value.colorTheme.info);
        setCssVar(
          'warning',
          appearanceConfigurationRef.value.colorTheme.warning
        );
      }

      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.success', { item: i18n.t('Common.items.configuration.appearance')}),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', { item: i18n.t('Common.items.configuration.appearance')}),
        icon: 'report_problem',
      });
    });
}

function updateColor(colorKey: string, colorValue: string) {
  if (appearanceConfigurationRef.value?.colorTheme) {
    if (colorKey === 'primary') {
      appearanceConfigurationRef.value.colorTheme.primary = colorValue;
    }
    if (colorKey === 'secondary') {
      appearanceConfigurationRef.value.colorTheme.secondary = colorValue;
    }
    if (colorKey === 'accent') {
      appearanceConfigurationRef.value.colorTheme.accent = colorValue;
    }
    if (colorKey === 'dark') {
      appearanceConfigurationRef.value.colorTheme.dark = colorValue;
    }
    if (colorKey === 'positive') {
      appearanceConfigurationRef.value.colorTheme.positive = colorValue;
    }
    if (colorKey === 'negative') {
      appearanceConfigurationRef.value.colorTheme.negative = colorValue;
    }
    if (colorKey === 'info') {
      appearanceConfigurationRef.value.colorTheme.info = colorValue;
    }
    if (colorKey === 'warning') {
      appearanceConfigurationRef.value.colorTheme.warning = colorValue;
    }
    setCssVar(colorKey, colorValue);
  }
}

onMounted(() => {
  loadAppearanceConfiguration();
});
</script>

<style scoped></style>
