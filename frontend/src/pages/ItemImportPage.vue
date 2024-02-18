<template>
  <ArtivactContent>

    <div class="full-width">
      <h1 class="av-text-h1">{{ $t('ItemImportPage.heading') }}</h1>
      <div class="q-mb-lg">
        {{ $t('ItemImportPage.description.scan') }}
        <q-btn
          :label="$t('ItemImportPage.button.scan')"
          color="primary"
          class="float-right q-mb-lg"
          @click="scanItems()"
        />
      </div>

      <div>
        {{ $t('ItemImportPage.description.upload') }}
        <q-uploader
          :url="'/api/exchange/item/import'"
          :label="$t('ItemImportPage.button.upload')"
          class="q-mt-md q-mb-md"
          accept=".artivact.item.zip"
          field-name="file"
          :no-thumbnails="true"
          @finish="itemUploaded"
        />
      </div>
    </div>

    <div>
      <h1 class="av-text-h1">Create Search Index</h1>
      <label>(Re-)Creates the search index.</label>

      <q-btn
        label="(Re-)Create Search Index"
        color="primary"
        class="float-right q-mb-lg"
        @click="recreateSearchIndex()"
      />
    </div>

  </ArtivactContent>
</template>

<script setup lang="ts">
import {api} from 'boot/axios';
import {useQuasar} from 'quasar';
import ArtivactContent from 'components/ArtivactContent.vue';
import {useI18n} from 'vue-i18n';

const quasar = useQuasar();
const i18n = useI18n();

function scanItems() {
  api
    .post('/api/import')
    .then(() => {
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('ItemImportPage.messages.scanSuccessful'),
        icon: 'done',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemImportPage.messages.scanFailed'),
        icon: 'report_problem',
      });
    });
}

function itemUploaded() {
  quasar.notify({
    color: 'positive',
    position: 'bottom',
    message: i18n.t('ItemImportPage.messages.itemUploaded'),
    icon: 'check',
  });
}

function recreateSearchIndex() {
  api
    .post('/api/search/index/recreate')
    .then(() => {
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: 'Index created successfully',
        icon: 'done',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Index creation failed',
        icon: 'report_problem',
      });
    });
}
</script>

<style scoped></style>
