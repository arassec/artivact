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

        <div class="row q-mb-md">
          {{
            $t(
              'AiConfigurationPage.test.translationPrompt',
            ).replaceAll('$_locale_$', '{locale}')
          }}
        </div>

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
          :label="$t('AiConfigurationPage.fields.ttsVoice')"
          v-model="aiConfigurationRef.ttsVoice"
        />

        <div class="row q-mb-lg">
          <div class="full-width">
            <q-btn
              :label="$t('Common.save')"
              color="primary"
              class="q-mb-lg float-right"
              @click="saveAiConfiguration()"
            />
          </div>
        </div>

        <div class="q-mb-lg">
          {{ $t('AiConfigurationPage.test.description') }}
        </div>

        <div class="row items-center q-mb-lg">
          <div class="col">
            <q-input
              outlined
              :label="$t('AiConfigurationPage.test.textInput')"
              v-model="testTextRef"
            />
          </div>
          <div class="col-auto q-ml-md">
            <q-toggle
              v-model="testTtsModeRef"
              :false-value="false"
              :true-value="true"
              :label="testTtsModeRef ? $t('AiConfigurationPage.test.ttsMode') : $t('AiConfigurationPage.test.translationMode')"
            />
          </div>
          <div class="col-auto q-ml-md">
            <q-btn
              round
              icon="smart_toy"
              color="primary"
              @click="runTest()"
            />
          </div>
        </div>

        <div v-if="translationResultRef" class="q-mb-lg">
          <q-field
            outlined
            :label="$t('AiConfigurationPage.test.translationResult')"
            stack-label
          >
            <template v-slot:control>
              <div class="self-center full-width">
                {{ translationResultRef }}
              </div>
            </template>
          </q-field>
        </div>

        <div v-if="audioUrlRef" class="q-mb-lg">
          <audio controls :src="audioUrlRef" class="full-width"></audio>
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
import {useLocaleStore} from '../stores/locale';

const quasar = useQuasar();
const i18n = useI18n();
const localeStore = useLocaleStore();

const aiConfigurationRef: Ref<AiConfiguration | null> = ref(null);
const testTextRef: Ref<string> = ref('');
const testTtsModeRef: Ref<boolean> = ref(false);
const translationResultRef: Ref<string> = ref('');
const audioUrlRef: Ref<string> = ref('');

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
        badgeColor: 'positive',
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

function runTest() {
  const locale = localeStore.selectedLocale || 'en';

  // Save the configuration first so the test uses the current values.
  api
    .post('/api/configuration/ai', aiConfigurationRef.value)
    .then(() => {
      if (testTtsModeRef.value) {
        runTtsTest();
      } else {
        runTranslationTest(locale);
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('AiConfigurationPage.test.testFailed'),
        icon: 'report_problem',
      });
    });
}

function runTranslationTest(locale: string) {
  translationResultRef.value = '';
  audioUrlRef.value = '';
  api
    .post('/api/configuration/ai/translate/' + locale, testTextRef.value, {
      headers: {'Content-Type': 'text/plain'},
    })
    .then((response) => {
      translationResultRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('AiConfigurationPage.test.testFailed'),
        icon: 'report_problem',
      });
    });
}

function runTtsTest() {
  translationResultRef.value = '';
  audioUrlRef.value = '';
  api
    .post('/api/configuration/ai/test/tts', testTextRef.value, {
      headers: {'Content-Type': 'text/plain'},
    })
    .then(() => {
      audioUrlRef.value =
        '/api/configuration/ai/test/tts/audio?t=' + Date.now();
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('AiConfigurationPage.test.testFailed'),
        icon: 'report_problem',
      });
    });
}

onMounted(() => {
  loadAiConfiguration();
});
</script>

<style scoped></style>
