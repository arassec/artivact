<template>
  <div v-if="contentAudioRef" class="row items-center full-width q-mb-md">
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
      v-if="hasContentAudio"
      round
      dense
      flat
      color="negative"
      icon="delete"
      class="q-ml-sm"
      @click="deleteContentAudio"
    >
      <q-tooltip>{{ deleteLabel }}</q-tooltip>
    </q-btn>
    <q-icon
      v-if="hasContentAudio"
      name="check_circle"
      color="positive"
      size="sm"
      class="q-ml-sm"
    />
  </div>
</template>

<script setup lang="ts">
import {computed, PropType, ref, toRef} from 'vue';
import {TranslatableString} from '../artivact-models';
import {useLocaleStore} from '../../stores/locale';
import {api} from '../../boot/axios';

const props = defineProps({
  pageId: {
    required: true,
    type: String,
  },
  widgetId: {
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
  (e: 'save-widget-before-upload', payload: { resolve: (value?: unknown) => void; reject: (reason?: unknown) => void }): void;
}>();

const localeStore = useLocaleStore();
const contentAudioRef = toRef(props, 'contentAudio');
const fileRef = ref(null as File | null);

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

function getContentAudioFilename(): string {
  const locale = localeStore.selectedLocale;
  if (locale) {
    return `content-audio-${locale}.mp3`;
  }
  return 'content-audio.mp3';
}

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

function setContentAudioFilename(filename: string) {
  const locale = localeStore.selectedLocale;
  if (locale) {
    contentAudioRef.value.translations[locale] = filename;
  } else {
    contentAudioRef.value.value = filename;
  }
}

function clearContentAudioFilename() {
  const locale = localeStore.selectedLocale;
  if (locale) {
    delete contentAudioRef.value.translations[locale];
  } else {
    contentAudioRef.value.value = '';
  }
}

async function uploadContentAudio(file: File | null) {
  if (!file) {
    return;
  }

  const targetFilename = getContentAudioFilename();

  // Save the widget before uploading
  await new Promise((resolve, reject) => {
    emit('save-widget-before-upload', {resolve, reject});
  });

  const formData = new FormData();
  formData.append('file', file, targetFilename);

  await api.post(
    `/api/page/${props.pageId}/widget/${props.widgetId}/content-audio`,
    formData
  );

  setContentAudioFilename(targetFilename);
  fileRef.value = null;
}

async function deleteContentAudio() {
  const filename = getCurrentFilename();
  if (!filename) {
    return;
  }

  await api.delete(
    `/api/page/${props.pageId}/widget/${props.widgetId}/content-audio/${filename}`
  );

  clearContentAudioFilename();
}
</script>

<style scoped></style>
