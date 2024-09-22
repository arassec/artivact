<template>
  <div class="column full-width">
    <q-input
      outlined
      v-model="widgetDataRef.maxResults"
      :label="$t('ArtivactItemSearchInput.label.maxResults')"
      type="number"
      class="q-mb-md"
    />

    <q-input
      type="textarea"
      outlined
      v-model="widgetDataRef.searchTerm"
      :label="$t('ArtivactItemSearchInput.label.term')"
      :error="searchTermRequired && widgetDataRef.searchTerm.length === 0"
      :error-message="$t('ArtivactItemSearchInput.error')"
    />

    <div class="full-width">
      <label>{{ $t('ArtivactItemSearchInput.label.fulltext') }}</label>
    </div>

    <div class="full-width q-mb-md">
      <label>{{ $t('ArtivactItemSearchInput.label.property') }}</label>
    </div>

    <div class="full-width">
      <q-btn-dropdown :label="$t('ArtivactItemSearchInput.label.addTag')" small dense flat>
        <q-list>
          <q-item
            v-for="tag in tagsConfigurationRef.tags"
            :key="tag.id"
            clickable
            v-close-popup
            @click="appendToSearch(tag.id, tag.value)"
          >
            <q-item-section>
              <q-item-label>{{ tag.value }}</q-item-label>
            </q-item-section>
          </q-item>
        </q-list>
      </q-btn-dropdown>

      <q-btn-dropdown class="q-ml-md" :label="$t('ArtivactItemSearchInput.label.addProperty')" small dense flat>
        <q-list>
          <template v-for="category in propertiesConfigurationRef.categories">
            <q-item
              v-for="property in category.properties"
              :key="property.id"
              clickable
              v-close-popup
              @click="appendToSearch(property.id, property.value)"
            >
              <q-item-section>
                <q-item-label>{{ property.value }}</q-item-label>
              </q-item-section>
            </q-item>
          </template>
        </q-list>
      </q-btn-dropdown>

      <q-btn
        rounded
        flat
        dense
        icon="refresh"
        class="float-right"
        @click="$emit('refresh-search-results')"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, PropType, ref, Ref, toRef} from 'vue';
import {SearchBasedWidgetData} from 'components/widgets/artivact-widget-models';
import {api} from 'boot/axios';
import {PropertiesConfiguration, TagsConfiguration} from 'components/artivact-models';
import {useQuasar} from 'quasar';
import {useI18n} from 'vue-i18n';

const props = defineProps({
  widgetData: {
    required: true,
    type: Object as PropType<SearchBasedWidgetData>,
  },
  searchTermRequired: {
    required: false,
    type: Boolean,
    default: false,
  },
});

const quasar = useQuasar();
const i18n = useI18n();

const widgetDataRef = toRef(props, 'widgetData');

const tagsConfigurationRef: Ref<TagsConfiguration | null> = ref(null);

const propertiesConfigurationRef: Ref<PropertiesConfiguration | null> =
  ref(null);

function loadTagsConfiguration() {
  api
    .get('/api/configuration/tags')
    .then((response) => {
      tagsConfigurationRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.tags') }),
        icon: 'report_problem',
      });
    });
}

function loadPropertyConfiguration() {
  api
    .get('/api/configuration/property')
    .then((response) => {
      propertiesConfigurationRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.properties') }),
        icon: 'report_problem',
      });
    });
}

function appendToSearch(key: string, value: string) {
  if (widgetDataRef.value) {
    widgetDataRef.value.searchTerm =
      widgetDataRef.value.searchTerm + ' ' + key + ':"' + value + '"';
  }
}

onMounted(() => {
  loadTagsConfiguration();
  loadPropertyConfiguration();
});
</script>

<style scoped></style>
