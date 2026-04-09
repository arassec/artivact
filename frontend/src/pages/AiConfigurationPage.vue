<template>
  <artivact-content>
    <div class="full-width">
      <h1 class="av-text-h1">{{ $t('AiConfigurationPage.heading') }}</h1>

      <h2 class="av-text-h2">
        {{ $t('AiConfigurationPage.configuration.heading') }}
      </h2>

      <div class="q-mb-lg">
        {{ $t('AiConfigurationPage.configuration.description') }}
      </div>

      <div v-if="aiConfigurationRef">
        <q-toggle
          class="q-mb-lg"
          v-model="aiConfigurationRef.enabled"
          :label="$t('AiConfigurationPage.fields.enabled')"
        />

        <q-input
          outlined
          class="q-mb-lg"
          :label="$t('AiConfigurationPage.fields.apiKey')"
          v-model="aiConfigurationRef.apiKey"
        />

        <q-input
          outlined
          class="q-mb-lg"
          type="textarea"
          :label="$t('AiConfigurationPage.fields.generalContext')"
          v-model="aiConfigurationRef.generalContext"
        />

        <q-input
          outlined
          class="q-mb-lg"
          type="textarea"
          :label="$t('AiConfigurationPage.fields.translationPrompt')"
          v-model="aiConfigurationRef.translationPrompt"
        />

        <q-input
          outlined
          class="q-mb-lg"
          type="textarea"
          :label="$t('AiConfigurationPage.fields.ttsPrompt')"
          v-model="aiConfigurationRef.ttsPrompt"
        />

        <q-input
          outlined
          class="q-mb-lg"
          :label="$t('AiConfigurationPage.fields.ttsVoice')"
          v-model="aiConfigurationRef.ttsVoice"
        />

        <div class="full-width">
          <q-btn
            :label="$t('Common.save')"
            color="primary"
            class="q-mb-lg float-right"
            @click="saveAiConfiguration()"
          />
        </div>
      </div>
    </div>
  </artivact-content>
</template>

<script setup lang="ts">
import ArtivactContent from '../components/ArtivactContent.vue';
import {useQuasar} from 'quasar';
import {onMounted, ref, Ref} from 'vue';
import {AiConfiguration} from '../components/artivact-models';
import {api} from '../boot/axios';
import {useI18n} from 'vue-i18n';

const quasar = useQuasar();
const i18n = useI18n();

const aiConfigurationRef: Ref<AiConfiguration | null> = ref(null);

function loadAiConfiguration() {
  api
    .get('/api/configuration/ai')
    .then((response) => {
      aiConfigurationRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', {
          item: i18n.t('Common.items.configuration.ai'),
        }),
        icon: 'report_problem',
      });
    });
}

function saveAiConfiguration() {
  api
    .post('/api/configuration/ai', aiConfigurationRef.value)
    .then(() => {
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.success', {
          item: i18n.t('Common.items.configuration.ai'),
        }),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', {
          item: i18n.t('Common.items.configuration.ai'),
        }),
        icon: 'report_problem',
      });
    });
}

onMounted(() => {
  loadAiConfiguration();
});
</script>

<style scoped></style>
