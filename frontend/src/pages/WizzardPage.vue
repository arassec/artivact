<template>
  <div>...</div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useWizzardStore } from '../stores/wizzard';
import { api } from '../boot/axios';
import { useQuasar } from 'quasar';
import { useI18n } from 'vue-i18n';

const route = useRoute();
const router = useRouter();
const quasar = useQuasar();
const i18n = useI18n();

const wizzardStore = useWizzardStore();

onMounted(() => {
  let action = route.params.action;
  if (action === 'scanItem') {
    wizzardStore.setScanItem(true);
    api
      .post('/api/item')
      .then((response) => {
        router.push('/administration/configuration/item/' + response.data);
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: i18n.t('Common.messages.creating.success', {
            item: i18n.t('Common.items.item'),
          }),
          icon: 'check',
        });
      })
      .catch(() => {
        quasar.notify({
          color: 'negative',
          position: 'bottom',
          message: i18n.t('Common.messages.creating.failed', {
            item: i18n.t('Common.items.item'),
          }),
          icon: 'report_problem',
        });
      });
  } else if (action === 'scanPeripherals') {
    wizzardStore.setScanPeripherals(true);
    router.push('/administration/configuration/peripherals');
  } else {
    router.push('/');
  }
});
</script>

<style scoped></style>
