<template>
  <artivact-content v-if="noIndexPageRef">
    <label>
      {{ $t('ArtivactPage.label.noIndexPage') }}
    </label>
  </artivact-content>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {api} from '../boot/axios';
import {useRouter} from 'vue-router';
import ArtivactContent from '../components/ArtivactContent.vue';
import {useQuasar} from 'quasar';
import {useI18n} from 'vue-i18n';

const quasar = useQuasar();
const router = useRouter();
const i18n = useI18n();

const noIndexPageRef = ref(false);

function redirectToIndexPage() {
  api
    .get('/api/page')
    .then((response) => {
      if (response.data !== '') {
        router.push('/page/' + response.data);
      } else {
        noIndexPageRef.value = true;
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('IndexPage.messages.noIndexPage'),
        icon: 'report_problem',
      });
    });
}

onMounted(() => {
  redirectToIndexPage();
});
</script>

<style scoped></style>
