<template>
  <ArtivactContent>

    <div class="full-width">
      <h1 class="av-text-h1">{{ $t('ItemImportPage.heading') }}</h1>

      <div>
        {{ $t('ItemImportPage.description.upload') }}
        <q-uploader
          :url="'/api/import/item'"
          :label="$t('ItemImportPage.button.upload')"
          class="q-mt-md q-mb-md"
          accept="ITEM.artivact.content.zip"
          field-name="file"
          :no-thumbnails="true"
          @finish="itemUploaded"
        />
      </div>
    </div>

  </ArtivactContent>
</template>

<script setup lang="ts">
import {useQuasar} from 'quasar';
import ArtivactContent from 'components/ArtivactContent.vue';
import {useI18n} from 'vue-i18n';

const quasar = useQuasar();
const i18n = useI18n();

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
