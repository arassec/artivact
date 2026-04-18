<template>
  <div :class="!restrictedItemRef ? 'q-mb-md' : ''">
    <!-- Input field -->
    <q-input
      :data-test="dataTest"
      v-if="translatableStringRef && localeStore.selectedLocale === null"
      outlined
      v-model="translatableStringRef.value"
      :label="label"
      :type="textarea ? 'textarea' : 'text'"
      :autogrow="textarea"
      class="no-scroll"
      :disable="disable"
    />
    <div
      class="row full-width"
      v-if="translatableStringRef && localeStore.selectedLocale !== null"
    >
      <q-input
        :data-test="dataTest"
        outlined
        v-model="translatableStringRef.translations[localeStore.selectedLocale]"
        :label="label"
        :type="textarea ? 'textarea' : 'text'"
        :autogrow="textarea"
        class="no-scroll column col-grow"
        :disable="disable"
      />
      <div class="q-ml-sm" v-if="applicationSettingsStore.translationEnabled">
        <q-btn
          flat
          dense
          rounded
          icon="auto_awesome"
          :disable="disable"
          @click="translateText"
        >
          <q-tooltip>{{
              $t('ArtivactRestrictedTranslatableItemEditor.tooltip.translate')
            }}
          </q-tooltip>
        </q-btn>
      </div>
      <q-input
        v-if="showStandardTextRef"
        outlined
        v-model="translatableStringRef.value"
        :label="label"
        :type="textarea ? 'textarea' : 'text'"
        :autogrow="textarea"
        class="no-scroll column col-grow q-ml-md"
        transition-show="slide-left"
        transition-hide="slide-right"
      />
      <div class="q-ml-sm">
        <q-btn
          flat
          dense
          rounded
          icon="language"
          @click="showStandardTextRef = !showStandardTextRef"
        />
      </div>
    </div>

    <q-btn
      rounded
      dense
      flat
      color="primary"
      size="md"
      icon="expand_more"
      @click="
        showDetailsRef = true;
        $emit('show-details');
      "
      v-if="restrictedItemRef && showRestrictions && !showDetailsRef"
    >
      <q-tooltip>{{
          $t('ArtivactRestrictedTranslatableItemEditor.tooltip.more')
        }}
      </q-tooltip>
    </q-btn>
    <q-btn
      rounded
      dense
      flat
      color="primary"
      size="md"
      icon="expand_less"
      @click="
        showDetailsRef = false;
        $emit('hide-details');
      "
      v-if="restrictedItem && showDetailsRef"
    >
      <q-tooltip>{{
          $t('ArtivactRestrictedTranslatableItemEditor.tooltip.less')
        }}
      </q-tooltip>
    </q-btn>

    <div v-if="restrictedItem" v-show="showDetailsRef">
      <!-- Restrictions -->
      <artivact-restrictions-editor
        v-if="restrictedItemRef"
        :in-details-view="true"
        :restrictions="restrictedItemRef.restrictions"
        @delete-restriction="deleteRestriction"
        @add-restriction="addRestriction"
      />

      <!-- Slot for additional editors -->
      <slot></slot>

      <div v-if="!showSeparator" class="q-mb-md"/>
    </div>
    <q-separator class="q-mt-xs q-mb-lg" v-if="showSeparator"/>
  </div>
</template>

<script setup lang="ts">
import {PropType, ref, toRef} from 'vue';
import ArtivactRestrictionsEditor from '../components/ArtivactRestrictionsEditor.vue';
import {BaseRestrictedObject, TranslatableString} from './artivact-models';
import {useLocaleStore} from '../stores/locale';
import {useApplicationSettingsStore} from '../stores/application-settings';
import {api} from '../boot/axios';
import {useQuasar} from 'quasar';
import {useI18n} from 'vue-i18n';

const props = defineProps({
  label: {
    required: true,
    type: String,
    default: '',
  },
  textarea: {
    required: false,
    type: Boolean,
    default: false,
  },
  showSeparator: {
    required: false,
    type: Boolean,
    default: true,
  },
  translatableString: {
    required: false,
    type: Object as PropType<TranslatableString>,
  },
  restrictedItem: {
    required: false,
    type: Object as PropType<BaseRestrictedObject>,
  },
  dataTest: {
    required: false,
    type: String,
  },
  disable: {
    required: false,
    type: Boolean,
    default: false,
  },
  showRestrictions: {
    required: false,
    type: Boolean,
    default: true,
  }
});

const localeStore = useLocaleStore();
const applicationSettingsStore = useApplicationSettingsStore();
const quasar = useQuasar();
const i18n = useI18n();

const translatableStringRef = toRef(props, 'translatableString');
const restrictedItemRef = toRef(props, 'restrictedItem');
const showDetailsRef = ref(false);

const showStandardTextRef = ref(false);

function addRestriction(value: string) {
  if (restrictedItemRef.value) {
    restrictedItemRef.value.restrictions.push(value);
  }
}

function deleteRestriction(value: string) {
  if (restrictedItemRef.value) {
    restrictedItemRef.value.restrictions.forEach((item, index) => {
      if (item === value && restrictedItemRef.value) {
        restrictedItemRef.value.restrictions.splice(index, 1);
      }
    });
  }
}

function translateText() {
  if (!translatableStringRef.value || !localeStore.selectedLocale) {
    return;
  }

  const fallbackValue = translatableStringRef.value.value;
  const selectedLocaleValue = translatableStringRef.value.translations[localeStore.selectedLocale];

  // If both fallback and selected locale values are empty, do nothing.
  if (!fallbackValue && !selectedLocaleValue) {
    return;
  }

  // Determine the translation direction before the async call so that the
  // response handler uses a stable flag regardless of any user edits in between.
  const translateToSelectedLocale = !!fallbackValue;

  let sourceText: string;
  let targetLocale: string;

  if (translateToSelectedLocale) {
    // Fallback locale value is present: translate fallback → selected locale.
    sourceText = fallbackValue;
    targetLocale = localeStore.selectedLocale;
  } else {
    // Fallback locale is empty but selected locale has a value (guaranteed by
    // the early return above): invert direction — translate selected locale →
    // application default locale.
    sourceText = selectedLocaleValue;
    targetLocale = applicationSettingsStore.defaultLocale;
  }

  quasar.loading.show();
  api
    .post('/api/configuration/ai/translate/' + targetLocale, sourceText, {
      headers: {'Content-Type': 'text/plain'}
    })
    .then((response) => {
      if (translatableStringRef.value && localeStore.selectedLocale) {
        if (translateToSelectedLocale) {
          // Assign translated text to the selected locale translation.
          translatableStringRef.value.translations[localeStore.selectedLocale] = response.data;
        } else {
          // Assign translated text to the fallback locale value.
          translatableStringRef.value.value = response.data;
        }
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ArtivactRestrictedTranslatableItemEditor.messages.translationFailed'),
        icon: 'report_problem',
      });
    })
    .finally(() => {
      quasar.loading.hide();
    });
}
</script>

<style scoped></style>
