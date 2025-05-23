<template>
  <ArtivactContent>
    <div class="col">
      <q-form @submit="onSubmit">
        <div class="row justify-center">
          <h1 class="av-text-h1 col-5">{{ $t('LoginPage.login') }}</h1>
        </div>
        <div class="row justify-center">
          <q-input
            data-test="username-input"
            :autofocus="true"
            v-model="usernameRef"
            class="col-5"
            type="text"
            name="username"
            :label="$t('Common.username')"
          ></q-input>
        </div>
        <div class="row justify-center">
          <q-input
            data-test="password-input"
            v-model="passwordRef"
            class="col-5"
            type="password"
            name="password"
            :label="$t('Common.password')"
          ></q-input>
        </div>
        <div class="row justify-center q-mt-lg">
          <div class="col-5">
            <q-btn data-test="submit-login-button" type="submit" class="float-right" outline :label="$t('LoginPage.login')" />
          </div>
        </div>
      </q-form>
    </div>
  </ArtivactContent>
</template>

<script setup lang="ts">
import { api } from 'boot/axios';
import { ref } from 'vue';
import { useQuasar } from 'quasar';
import { useUserdataStore } from 'stores/userdata';
import { useRouter } from 'vue-router';
import ArtivactContent from 'components/ArtivactContent.vue';
import { useMenuStore } from 'stores/menu';
import { useI18n } from 'vue-i18n';

const quasar = useQuasar();
const route = useRouter();
const i18n = useI18n();

const usernameRef = ref('');
const passwordRef = ref('');

const store = useUserdataStore();
const menuStore = useMenuStore();

function loadMenus() {
  api
    .get('/api/menu')
    .then((response) => {
      menuStore.setAvailableMenus(response.data);
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('LoginPage.messages.loadingMenusFailed'),
      });
    });
}

function onSubmit() {
  let postdata = new URLSearchParams();
  postdata.append('username', usernameRef.value);
  postdata.append('password', passwordRef.value);

  api
    .post('/api/auth/login', postdata)
    .then(() => {
      loadMenus();
      api
        .get('/api/configuration/public/user')
        .then((response) => {
          store.setUserdata(response.data);
          quasar.notify({
            color: 'positive',
            position: 'bottom',
            message: i18n.t('LoginPage.messages.loginSuccessful'),
            icon: 'check',
            attrs: {
              'data-test': 'artivact-notify-success'
            },
            actions: [
              { icon: 'close', color: 'white', round: true, flat: true, handler: () => { /* ... */ } }
            ]
          });
          route.push('/');
        })
        .catch(() => {
          quasar.notify({
            color: 'negative',
            position: 'bottom',
            message: i18n.t('LoginPage.messages.loadingUserdataFailed'),
            icon: 'report_problem',
          });
        });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('LoginPage.messages.loginFailed'),
        icon: 'report_problem',
      });
    });
}
</script>

<style scoped></style>
