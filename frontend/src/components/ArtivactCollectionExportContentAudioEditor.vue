<template>
  <div v-if="contentAudioRef" class="full-width q-mb-md">
    <div class="row items-center full-width">
      <q-file
        v-model="fileRef"
        outlined
        dense
        accept=".mp3"
        :label="label"
        class="col"
        @update:model-value="uploadContentAudio"
      >
        <template v-slot:prepend>
          <q-icon name="audiotrack"/>
        </template>
      </q-file>
      <q-btn
        v-if="applicationSettingsStore.ttsEnabled"
        round
        dense
        flat
        color="primary"
        icon="auto_awesome"
        class="q-ml-sm"
        :loading="generatingRef"
        @click="generateContentAudio"
      >
        <q-tooltip>{{ $t('ContentAudioEditor.tooltip.generateAudio') }}</q-tooltip>
      </q-btn>
      <q-btn
        v-if="hasContentAudio"
        round
        dense
        flat
        color="primary"
        icon="delete"
        class="q-ml-sm"
        @click="deleteContentAudio"
      >
        <q-tooltip>{{ deleteLabel }}</q-tooltip>
      </q-btn>
      <q-btn
        v-if="hasContentAudio"
        round
        dense
        flat
        color="primary"
        :icon="showPlayerRef ? 'stop' : 'play_arrow'"
        class="q-ml-sm"
        @click="togglePlayer"
      >
        <q-tooltip>{{ $t('ContentAudioEditor.tooltip.playAudio') }}</q-tooltip>
      </q-btn>
    </div>
    <div v-if="showPlayerRef && audioUrlRef" class="q-mt-sm">
      <audio controls :src="audioUrlRef" class="full-width"></audio>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, PropType, ref, toRef} from 'vue';
import {TranslatableString} from './artivact-models';
import {useLocaleStore} from '../stores/locale';
import {useApplicationSettingsStore} from '../stores/application-settings';
import {api} from '../boot/axios';
import {useQuasar} from 'quasar';
import {useI18n} from 'vue-i18n';

const props = defineProps({
  collectionExportId: {
    required: true,
    type: String,
  },
  contentAudio: {
    required: true,
    type: Object as PropType<TranslatableString>,
  },
  label: {
    required: true,
    type: String,
  },
  deleteLabel: {
    required: true,
    type: String,
  },
});

const emit = defineEmits<{
  (e: 'save-before-upload', payload: { resolve: (value?: unknown) => void; reject: (reason?: unknown) => void }): void;
  (e: 'audio-changed'): void;
}>();

const localeStore = useLocaleStore();
const applicationSettingsStore = useApplicationSettingsStore();
const quasar = useQuasar();
const i18n = useI18n();
const contentAudioRef = toRef(props, 'contentAudio');
const fileRef = ref(null as File | null);
const generatingRef = ref(false);
const showPlayerRef = ref(false);
const audioUrlRef = ref('');

const hasContentAudio = computed(() => {
  if (!contentAudioRef.value) {
    return false;
  }
  const locale = localeStore.selectedLocale;
  if (locale) {
    return !!contentAudioRef.value.translations[locale];
  }
  return !!contentAudioRef.value.value;
});

function getCurrentFilename(): string | null {
  if (!contentAudioRef.value) {
    return null;
  }
  const locale = localeStore.selectedLocale;
  if (locale) {
    return contentAudioRef.value.translations[locale] || null;
  }
  return contentAudioRef.value.value || null;
}

function togglePlayer() {
  showPlayerRef.value = !showPlayerRef.value;
  if (showPlayerRef.value) {
    const filename = getCurrentFilename();
    if (filename) {
      audioUrlRef.value = `/api/collection/export/${props.collectionExportId}/content-audio/${filename}?t=${Date.now()}`;
    }
  } else {
    audioUrlRef.value = '';
  }
}

async function uploadContentAudio(file: File | null) {
  if (!file) {
    return;
  }

  await new Promise((resolve, reject) => {
    emit('save-before-upload', {resolve, reject});
  });

  const locale = localeStore.selectedLocale || '';

  const formData = new FormData();
  formData.append('file', file, file.name);

  await api.post(
    `/api/collection/export/${props.collectionExportId}/content-audio?locale=${locale}`,
    formData
  );

  const audioFilename = getExpectedFilename(locale);
  setContentAudioFilename(locale, audioFilename);
  fileRef.value = null;
  emit('audio-changed');
}

async function generateContentAudio() {
  generatingRef.value = true;
  quasar.loading.show();

  await new Promise((resolve, reject) => {
    emit('save-before-upload', {resolve, reject});
  });

  const locale = localeStore.selectedLocale || '';

  api
    .post(
      `/api/collection/export/${props.collectionExportId}/generate-audio?locale=${locale}`
    )
    .then((response) => {
      setContentAudioFilename(locale, response.data);
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('ContentAudioEditor.messages.generateSuccess'),
        icon: 'check',
        badgeColor: 'positive',
      });
      emit('audio-changed');
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ContentAudioEditor.messages.generateFailed'),
        icon: 'report_problem',
      });
    })
    .finally(() => {
      generatingRef.value = false;
      quasar.loading.hide();
    });
}

async function deleteContentAudio() {
  const locale = localeStore.selectedLocale || '';

  await api.delete(
    `/api/collection/export/${props.collectionExportId}/content-audio?locale=${locale}`
  );

  clearContentAudioFilename(locale);
  emit('audio-changed');
}

function getExpectedFilename(locale: string): string {
  if (locale) {
    return `${props.collectionExportId}-${locale}.mp3`;
  }
  return `${props.collectionExportId}.mp3`;
}

function setContentAudioFilename(locale: string, filename: string) {
  if (locale) {
    contentAudioRef.value.translations[locale] = filename;
  } else {
    contentAudioRef.value.value = filename;
  }
}

function clearContentAudioFilename(locale: string) {
  if (locale) {
    delete contentAudioRef.value.translations[locale];
  } else {
    contentAudioRef.value.value = '';
  }
}
</script>

<style scoped></style>
