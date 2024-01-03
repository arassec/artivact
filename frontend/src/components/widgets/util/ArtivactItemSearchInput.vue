<template>
  <div class="column full-width q-mb-md">
    <q-input
      outlined
      v-model="widgetDataRef.maxResults"
      label="Max Results"
      type="number"
      class="q-mb-md"
    />

    <q-input
      outlined
      v-model="widgetDataRef.searchTerm"
      label="Search Term"
      :error="searchTermRequired && widgetDataRef.searchTerm.length === 0"
      error-message="Please enter a search query."
    />

    <div class="full-width">
      <label>Syntax for searching property values in fulltext search: "PROPERTY_ID=[SEARCH_TERM]"</label>
    </div>

    <div class="full-width q-mb-md">
      <label>Syntax for searching property values only: PROPERTY_ID:"[SEARCH_TERM]"</label>
    </div>

    <div class="full-width">
      <q-btn-dropdown label="Add Tag" small dense flat>
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

      <q-btn-dropdown class="q-ml-md" label="Add Property" small dense flat>
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
        :disable="widgetDataRef.searchTerm.length === 0"
        @click="$emit('refresh-search-results')"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, PropType, ref, Ref, toRef} from 'vue';
import {SearchBasedWidgetData} from 'components/widgets/widget-models';
import {api} from 'boot/axios';
import {PropertiesConfiguration, TagsConfiguration} from 'components/models';
import {useQuasar} from 'quasar';

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
        message: 'Loading tags failed',
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
        message: 'Loading failed',
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
