<template>
  <ArtivactContent>
    <div class="col">
      <q-form @submit="onSubmit">
        <div class="row justify-center">
          <h1 class="av-text-h1 col-5">Login</h1>
        </div>
        <div class="row justify-center">
          <q-input autofocus v-model="usernameRef" class="col-5" type="text" name="username" label="Username"></q-input>
        </div>
        <div class="row justify-center">
          <q-input v-model="passwordRef" class="col-5" type="password" name="password" label="Password"></q-input>
        </div>
        <div class="row justify-center q-mt-lg">
          <div class="col-5">
            <q-btn type="submit" class="float-right" outline label="Login"/>
          </div>
        </div>
      </q-form>
    </div>
  </ArtivactContent>
</template>

<script setup lang="ts">

import {api} from 'boot/axios';
import {ref} from 'vue';
import {useQuasar} from 'quasar';
import {useUserdataStore} from 'stores/userdata';
import {useRouter} from 'vue-router';
import ArtivactContent from 'components/ArtivactContent.vue';

const $q = useQuasar()
const $r = useRouter()

const usernameRef = ref('');
const passwordRef = ref('');

const store = useUserdataStore();

function onSubmit() {
  let postdata = new URLSearchParams();
  postdata.append('username', usernameRef.value);
  postdata.append('password', passwordRef.value);

  api.post('/api/auth/login', postdata)
    .then(() => {
      api.get('/api/configuration/user')
        .then((response) => {
          store.setUserdata(response.data);
          $q.notify({
            color: 'positive',
            position: 'bottom',
            message: 'Login successful',
            icon: 'report'
          })
          $r.push('/');
        })
        .catch(() => {
          $q.notify({
            color: 'negative',
            position: 'bottom',
            message: 'Loading UserData failed',
            icon: 'report_problem'
          })
        });
    })
    .catch(() => {
      $q.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Login failed',
        icon: 'report_problem'
      })
    })
}
</script>

<style scoped>

</style>
