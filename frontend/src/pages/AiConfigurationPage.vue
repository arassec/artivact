<template>
  <artivact-content>
    <div class="full-width">
      <h1 class="av-text-h1">{{ $t('AiConfigurationPage.heading') }}</h1>

      <div class="q-mb-xl">
        {{ $t('AiConfigurationPage.configuration.description') }}
      </div>

      <div v-if="aiConfigurationRef">
        <q-card flat bordered class="q-mb-xl">
          <q-card-section>
            <h2 class="av-text-h2 q-mb-md">
              {{ $t('AiConfigurationPage.translation.heading') }}
            </h2>

            <div class="q-mb-lg">
              {{ $t('AiConfigurationPage.translation.description') }}
            </div>

            <q-select
              outlined
              emit-value
              map-options
              class="q-mb-lg"
              v-model="aiConfigurationRef.translationModel"
              :options="translationModelOptions"
              :label="$t('AiConfigurationPage.fields.translationModel')"
            />

            <q-input
              outlined
              class="q-mb-lg"
              :label="$t('AiConfigurationPage.fields.translationApiKey')"
              v-model="aiConfigurationRef.translationApiKey"
            />

            <div class="q-mb-md">
              {{ $t('AiConfigurationPage.translation.promptInfo') }}
            </div>

            <artivact-restricted-translatable-item-editor
              class="q-mb-lg"
              :label="$t('AiConfigurationPage.fields.translationPrompt')"
              :translatable-string="aiConfigurationRef.translationPrompt"
              :textarea="true"
              :show-restrictions="false"
              :show-separator="false"
            />
          </q-card-section>

          <q-separator />

          <q-card-section>
            <div class="text-subtitle1 q-mb-md">
              {{ $t('AiConfigurationPage.translation.testHeading') }}
            </div>

            <div class="q-mb-lg">
              {{ $t('AiConfigurationPage.translation.testDescription') }}
            </div>

            <q-input
              outlined
              class="q-mb-md"
              :label="$t('AiConfigurationPage.test.textInput')"
              v-model="translationTestTextRef"
            />

            <div class="row justify-end">
              <q-btn
                color="primary"
                icon="translate"
                :label="$t('AiConfigurationPage.translation.testAction')"
                @click="runTranslationTest()"
              />
            </div>

            <div v-if="translationResultRef" class="q-mt-lg">
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
          </q-card-section>
        </q-card>

        <q-card flat bordered class="q-mb-xl">
          <q-card-section>
            <h2 class="av-text-h2 q-mb-md">
              {{ $t('AiConfigurationPage.tts.heading') }}
            </h2>

            <div class="q-mb-lg">
              {{ $t('AiConfigurationPage.tts.description') }}
            </div>

            <q-select
              outlined
              emit-value
              map-options
              class="q-mb-lg"
              v-model="aiConfigurationRef.ttsModel"
              :options="ttsModelOptions"
              :label="$t('AiConfigurationPage.fields.ttsModel')"
            />

            <q-input
              outlined
              class="q-mb-lg"
              :label="$t('AiConfigurationPage.fields.ttsApiKey')"
              v-model="aiConfigurationRef.ttsApiKey"
            />

            <artivact-restricted-translatable-item-editor
              class="q-mb-lg"
              :label="$t('AiConfigurationPage.fields.ttsVoice')"
              :translatable-string="aiConfigurationRef.ttsVoice"
              :show-restrictions="false"
              :show-separator="false"
            />
          </q-card-section>

          <q-separator />

          <q-card-section>
            <div class="text-subtitle1 q-mb-md">
              {{ $t('AiConfigurationPage.tts.testHeading') }}
            </div>

            <div class="q-mb-lg">
              {{ $t('AiConfigurationPage.tts.testDescription') }}
            </div>

            <q-input
              outlined
              class="q-mb-md"
              :label="$t('AiConfigurationPage.test.textInput')"
              v-model="ttsTestTextRef"
            />

            <div class="row justify-end">
              <q-btn
                color="primary"
                icon="smart_toy"
                :label="$t('AiConfigurationPage.tts.testAction')"
                @click="runTtsTest()"
              />
            </div>

            <div v-if="audioUrlRef" class="q-mt-lg">
              <audio controls :src="audioUrlRef" class="full-width"></audio>
            </div>
          </q-card-section>
        </q-card>

        <div class="row">
          <div class="full-width">
            <q-btn
              :label="$t('Common.save')"
              color="primary"
              class="float-right"
              @click="saveAiConfiguration()"
            />
          </div>
        </div>
      </div>
    </div>
  </artivact-content>
</template>

<script setup lang="ts">
import ArtivactContent from '../components/ArtivactContent.vue';
import ArtivactRestrictedTranslatableItemEditor from '../components/ArtivactRestrictedTranslatableItemEditor.vue';
import {useQuasar} from 'quasar';
import {onMounted, ref, Ref} from 'vue';
import {
  AiConfiguration,
  AiModel,
  SelectboxModel,
} from '../components/artivact-models';
import {api} from '../boot/axios';
import {useI18n} from 'vue-i18n';
import {useLocaleStore} from '../stores/locale';

const quasar = useQuasar();
const i18n = useI18n();
const localeStore = useLocaleStore();

const translationModelOptions: SelectboxModel[] = [
  {
    label: AiModel.OpenAI,
    value: AiModel.OpenAI,
    disable: false,
  },
];

const ttsModelOptions: SelectboxModel[] = [
  {
    label: AiModel.OpenAI,
    value: AiModel.OpenAI,
    disable: false,
  },
  {
    label: AiModel.Elevenlabs,
    value: AiModel.Elevenlabs,
    disable: false,
  },
];

const aiConfigurationRef: Ref<AiConfiguration | null> = ref(null);
const translationTestTextRef: Ref<string> = ref('');
const ttsTestTextRef: Ref<string> = ref('');
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

function persistAiConfiguration(showNotification: boolean): Promise<void> {
  return api
    .post('/api/configuration/ai', aiConfigurationRef.value)
    .then(() => {
      if (showNotification) {
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: i18n.t('Common.messages.saving.success', {
            item: i18n.t('Common.items.configuration.ai'),
          }),
          icon: 'check',
          badgeColor: 'positive',
        });
      }
    })
    .catch((error) => {
      if (showNotification) {
        quasar.notify({
          color: 'negative',
          position: 'bottom',
          message: i18n.t('Common.messages.saving.failed', {
            item: i18n.t('Common.items.configuration.ai'),
          }),
          icon: 'report_problem',
        });
      }
      throw error;
    });
}

function saveAiConfiguration() {
  persistAiConfiguration(true).catch(() => {
    // Notifications are handled in persistAiConfiguration.
  });
}

async function runTranslationTest() {
  const locale = localeStore.selectedLocale || 'en';

  translationResultRef.value = '';
  audioUrlRef.value = '';
  quasar.loading.show();

  try {
    await persistAiConfiguration(false);
    const response = await api.post(
      '/api/configuration/ai/translate/' + locale,
      translationTestTextRef.value,
      {
        headers: {'Content-Type': 'text/plain'},
      },
    );
    translationResultRef.value = response.data;
  } catch {
    quasar.notify({
      color: 'negative',
      position: 'bottom',
      message: i18n.t('AiConfigurationPage.test.testFailed'),
      icon: 'report_problem',
    });
  } finally {
    quasar.loading.hide();
  }
}

async function runTtsTest() {
  translationResultRef.value = '';
  audioUrlRef.value = '';
  quasar.loading.show();

  try {
    await persistAiConfiguration(false);
    await api.post('/api/configuration/ai/test/tts', ttsTestTextRef.value, {
      headers: {'Content-Type': 'text/plain'},
    });
    audioUrlRef.value = '/api/configuration/ai/test/tts/audio?t=' + Date.now();
  } catch {
    quasar.notify({
      color: 'negative',
      position: 'bottom',
      message: i18n.t('AiConfigurationPage.test.testFailed'),
      icon: 'report_problem',
    });
  } finally {
    quasar.loading.hide();
  }
}

onMounted(() => {
  loadAiConfiguration();
});
</script>

<style scoped></style>
