<template>
  <ArtivactContent>

    <div class="full-width">
      <h1 class="av-text-h1">{{ $t('ItemImportPage.heading') }}</h1>

      <q-separator class="q-mb-lg"/>

      <div class="column">
        <div class="q-mb-lg">
          {{ $t('ItemImportPage.description.scan') }}
        </div>
        <div>
          <q-btn
            :label="$t('ItemImportPage.button.scan')"
            color="primary"
            class="q-mb-lg"
            @click="scanItems()"
          />
        </div>
      </div>

      <q-separator class="q-mb-lg"/>

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
    .post('/api/exchange/item/import/filesystem')
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

</script>

<style scoped></style>
