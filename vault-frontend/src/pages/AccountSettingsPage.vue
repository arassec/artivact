<template>
  <q-page class="q-ma-lg">
    <div class="row justify-center">
      <h1 class="av-text-h1">Account Settings</h1>
    </div>
    <div class="row justify-center q-mb-md">
      <q-input outlined v-model="usernameRef" class="col-5" type="text" name="username" label="Username"/>
    </div>
    <div class="row justify-center">
      <q-input outlined v-model="passwordRef" class="col-5" type="password" name="password" label="Password"
               :error="passwordValidationFailedRef"/>
    </div>
    <div class="row justify-center">
      <q-input outlined v-model="passwordRepeatRef" class="col-5" type="password" name="password" label="Password (repeat)"
               :error="passwordValidationFailedRef"/>
    </div>
    <div class="row justify-center q-mt-lg">
      <div class="col-5">
        <q-btn label="Save" color="primary" class="float-right q-mb-lg" @click="saveAccount()"/>
      </div>
    </div>

  </q-page>
</template>

<script setup lang="ts">

import {useQuasar} from 'quasar';
import {api} from 'boot/axios';
import {onMounted, ref} from 'vue';
import {Account} from 'components/models';

const $q = useQuasar();
const accountRef = ref({} as Account);

const usernameRef = ref('');
const passwordRef = ref('');
const passwordRepeatRef = ref('');
const passwordValidationFailedRef = ref(false);

function loadAccount() {
  api.get('/api/configuration/account')
    .then((result) => {
      let account = result.data as Account;
      accountRef.value = account;
      usernameRef.value = account.username;
    })
    .catch(() => {
      $q.notify({
        color: 'negative',
        position: 'top',
        message: 'Loading failed',
        icon: 'report_problem'
      })
    })
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
    password: passwordRef.value
  }

  api.post('/api/configuration/account', account)
    .then(() => {
      $q.notify({
        color: 'positive',
        position: 'top',
        message: 'Account saved',
        icon: 'report'
      })
    })
    .catch(() => {
      $q.notify({
        color: 'negative',
        position: 'top',
        message: 'Saving failed',
        icon: 'report_problem'
      })
    });
}

onMounted(() => {
  loadAccount();
})
</script>

<style scoped>

</style>
