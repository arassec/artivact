<template>
  <ArtivactContent>
    <div>
      <h1 class="av-text-h1">Properties Configuration</h1>
      <div class="q-mb-lg">
        Here the properties of items can be configured. They are organized in
        categories, which can be ordered by drag & drop. The categories, with
        their respective properties, are shown at the bottom of the item
        details page.
      </div>

      <div
        class="q-mb-md"
        v-if="
          !propertiesConfigurationRef ||
          !propertiesConfigurationRef.categories ||
          propertiesConfigurationRef.categories.length == 0
        "
      >
        There are currently no properties defined.
      </div>

      <artivact-properties-configuration-editor
        v-if="propertiesConfigurationRef"
        :properties-configuration="propertiesConfigurationRef"
        :locales="localeStore.locales"
      />

      <q-separator class="q-mt-md q-mb-md" />

      <q-btn
        label="Save"
        color="primary"
        class="float-right q-mb-lg"
        @click="saveProperties()"
      />
    </div>

    <div>
      <h1 class="av-text-h1">Properties Import/Export</h1>

      <div class="q-mb-md">
        You can export the current properties configuration with the button below. A JSON file containing the
        current configuration will be created.
        <q-form :action="'/api/exchange/properties/export'" method="get">
          <q-btn
            icon="download"
            label="Export Properties"
            color="primary"
            type="submit"
            class="q-mt-md"
          />
        </q-form>
      </div>

      <div>
        You can upload a previously created properties export here and OVERWRITE the current properties with it.
        <q-uploader
          :url="'/api/exchange/properties/import'"
          label="Import Properties"
          class="q-mt-md q-mb-md"
          accept=".artivact.properties-configuration.json"
          field-name="file"
          :no-thumbnails="true"
          @finish="propertiesUploaded"
        />
      </div>
    </div>
  </ArtivactContent>
</template>

<script setup lang="ts">
import {useQuasar} from 'quasar';
import {onMounted, ref} from 'vue';
import {api} from 'boot/axios';
import ArtivactContent from 'components/ArtivactContent.vue';
import {useLocaleStore} from 'stores/locale';
import ArtivactPropertiesConfigurationEditor from 'components/ArtivactPropertiesConfigurationEditor.vue';

const quasar = useQuasar();

const localeStore = useLocaleStore();

const propertiesConfigurationRef = ref();

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

function saveProperties() {
  api
    .post('/api/configuration/property', propertiesConfigurationRef.value)
    .then(() => {
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: 'Properties saved',
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Saving failed',
        icon: 'report_problem',
      });
    });
}

function propertiesUploaded() {
  loadPropertyConfiguration();
  quasar.notify({
    color: 'positive',
    position: 'bottom',
    message: 'Properties uploaded',
    icon: 'check',
  });
}

onMounted(() => {
  loadPropertyConfiguration();
});
</script>

<style scoped></style>
