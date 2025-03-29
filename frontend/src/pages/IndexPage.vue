<template>
  Index-Page
</template>

<script setup>
import {onMounted} from "vue";
import {api} from "../boot/axios";
import i18n from "../i18n";
import {useRouter} from "vue-router";

const router = useRouter();

function redirectToIndexPage() {
  api
    .get('/api/page')
    .then((response) => {
      router.push('/page/' + response.data);
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

<style scoped>

</style>
