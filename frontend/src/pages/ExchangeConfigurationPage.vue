<template>
  <artivact-content>
    <div class="full-width">
      <h1 class="av-text-h1">{{$t('ExchangeConfigurationPage.heading')}}</h1>
      <div class="q-mb-lg">
        {{$t('ExchangeConfigurationPage.description')}}
      </div>
      <artivact-exchange-configuration-editor :exchange-configuration="exchangeConfigurationRef"
                                              v-if="exchangeConfigurationRef"/>
      <q-btn
        :label="$t('Common.save')"
        color="primary"
        class="q-mb-lg float-right"
        @click="saveExchangeConfiguration()"
      />
    </div>
  </artivact-content>
</template>

<script setup lang="ts">
import ArtivactContent from 'components/ArtivactContent.vue';
import ArtivactExchangeConfigurationEditor from 'components/ArtivactExchangeConfigurationEditor.vue';
import {useQuasar} from 'quasar';
import {onMounted, ref, Ref} from 'vue';
import {ExchangeConfiguration} from 'components/artivact-models';
import {api} from 'boot/axios';
import {useI18n} from 'vue-i18n';

const quasar = useQuasar();
const i18n = useI18n();

const exchangeConfigurationRef: Ref<ExchangeConfiguration | null> =
  ref(null);

function loadExchangeConfiguration() {
  api
    .get('/api/configuration/exchange')
    .then((response) => {
      exchangeConfigurationRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.configuration.exchange')}),
        icon: 'report_problem',
      });
    });
}

function saveExchangeConfiguration() {
  api
    .post('/api/configuration/exchange', exchangeConfigurationRef.value)
    .then(() => {
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.success', { item: i18n.t('Common.items.configuration.exchange')}),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', { item: i18n.t('Common.items.configuration.exchange')}),
        icon: 'report_problem',
      });
    });
}

onMounted(() => {
  loadExchangeConfiguration();
});

</script>

<style scoped>

</style>
