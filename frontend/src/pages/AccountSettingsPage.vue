<template>
  <ArtivactContent>
    <div class="col">
      <div>
        <h1 class="av-text-h1">{{$t('AccountSettingsPage.heading')}}</h1>
        <div class="q-mb-lg">{{$t('AccountSettingsPage.description')}}</div>

        <q-card class="q-mb-lg">
          <q-card-section class="bg-primary text-white">
            <div class="text-h6">{{$t('AccountSettingsPage.card.heading')}}</div>
          </q-card-section>

          <q-card-section>
            <q-input
              outlined
              v-model="usernameRef"
              class="col-5"
              type="text"
              name="username"
              :label="$t('Common.username')"
            />
          </q-card-section>

          <q-card-section>
            <q-input
              outlined
              v-model="passwordRef"
              class="col-5"
              type="password"
              name="password"
              :label="$t('Common.password')"
              :error="passwordValidationFailedRef"
            />
          </q-card-section>

          <q-card-section>
            <q-input
              outlined
              v-model="passwordRepeatRef"
              class="col-5"
              type="password"
              name="password"
              :label="$t('Common.passwordRepeat')"
              :error="passwordValidationFailedRef"
            />
          </q-card-section>

          <q-card-section>
            <q-input
              outlined
              v-model="emailRef"
              class="col-5"
              type="text"
              :label="$t('Common.email')"
              label="E-Mail"
            />
          </q-card-section>

          <q-card-section>
            <q-input
              outlined
              v-model="apiTokenRef"
              class="col-5"
              type="text"
              name="apiToken"
              :label="$t('AccountSettingsPage.apiToken')"
            />
          </q-card-section>
        </q-card>

        <q-btn
          :label="$t('Common.save')"
          color="primary"
          class="float-right q-mb-lg"
          @click="saveAccount()"
        />
      </div>
    </div>
  </ArtivactContent>
</template>

<script setup lang="ts">
import {useQuasar} from 'quasar';
import {api} from 'boot/axios';
import {onMounted, ref} from 'vue';
import {Account} from 'components/models';
import ArtivactContent from 'components/ArtivactContent.vue';
import {useI18n} from 'vue-i18n';

const i18n = useI18n();

const accountRef = ref({} as Account);

const usernameRef = ref('');
const passwordRef = ref('');
const passwordRepeatRef = ref('');
const passwordValidationFailedRef = ref(false);
const emailRef = ref('');
const apiTokenRef = ref('');

const quasar = useQuasar();

function loadAccount() {
  const quasar = useQuasar();

  api
    .get('/api/account/own')
    .then((result) => {
      let account = result.data as Account;
      accountRef.value = account;
      usernameRef.value = account.username;
      emailRef.value = account.email;
      apiTokenRef.value = account.apiToken;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.account')}),
        icon: 'report_problem',
      });
    });
}

function saveAccount() {
  if (passwordRef.value !== passwordRepeatRef.value) {
    passwordValidationFailedRef.value = true;
    return;
  } else {
    passwordValidationFailedRef.value = false;
  }

  let account: Account = {
    id: accountRef.value.id,
    version: accountRef.value.version,
    username: usernameRef.value,
    password: passwordRef.value,
    email: emailRef.value,
    apiToken: apiTokenRef.value,
    user: undefined,
    admin: undefined,
  };

  api
    .put('/api/account/own', account)
    .then((result) => {
      let account = result.data as Account;
      accountRef.value = account;
      usernameRef.value = account.username;
      emailRef.value = account.email;
      apiTokenRef.value = account.apiToken;
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.success', { item: i18n.t('Common.items.account')}),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', { item: i18n.t('Common.items.account')}),
        icon: 'report_problem',
      });
    });
}

onMounted(() => {
  loadAccount();
});
</script>

<style scoped></style>
