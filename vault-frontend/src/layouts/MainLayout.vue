<template>
  <q-layout view="hHh lpR fFf">

    <q-header elevated class="bg-primary text-white" height-hint="98">
      <q-toolbar class="q-pl-xs">

        <div>
          <q-btn flat round color="white" icon="menu" size="md">
            <q-menu>
              <q-list>
                <q-item clickable v-close-popup @click="home">
                  <q-item-section avatar>
                    <q-icon name="home" size="xs" color="primary"></q-icon>
                  </q-item-section>
                  <q-item-section>Home</q-item-section>
                </q-item>
                <q-separator/>
                <q-item clickable v-close-popup v-if="!userdataStore.authenticated" @click="login">
                  <q-item-section avatar>
                    <q-icon name="login" size="xs" color="primary"></q-icon>
                  </q-item-section>
                  <q-item-section>Login</q-item-section>
                </q-item>
                <q-item clickable v-close-popup v-if="userdataStore.authenticated" @click="logout">
                  <q-item-section avatar>
                    <q-icon name="logout" size="xs" color="primary"></q-icon>
                  </q-item-section>
                  <q-item-section>Logout</q-item-section>
                </q-item>
                <q-item clickable v-close-popup v-if="userdataStore.authenticated" @click="accountSettings">
                  <q-item-section avatar>
                    <q-icon name="person" size="xs" color="primary"></q-icon>
                  </q-item-section>
                  <q-item-section>Account</q-item-section>
                </q-item>

                <q-item clickable v-if="userdataStore.isAdmin">
                  <q-item-section avatar>
                    <q-icon name="build" size="xs" color="primary"></q-icon>
                  </q-item-section>
                  <q-item-section>Administration</q-item-section>
                  <q-item-section side>
                    <q-icon name="keyboard_arrow_right"/>
                  </q-item-section>
                  <q-menu anchor="top end" self="top start">
                    <q-list>
                      <q-item clickable v-close-popup @click="propertiesAdministration">
                        <q-item-section avatar>
                          <q-icon name="article" size="xs" color="primary"></q-icon>
                        </q-item-section>
                        <q-item-section>Artivact Properties</q-item-section>
                      </q-item>
                      <q-item clickable v-close-popup @click="tagsAdministration">
                        <q-item-section avatar>
                          <q-icon name="label" size="xs" color="primary"></q-icon>
                        </q-item-section>
                        <q-item-section>Artivact Tags</q-item-section>
                      </q-item>
                      <q-item clickable v-close-popup @click="systemSettings">
                        <q-item-section avatar>
                          <q-icon name="settings" size="xs" color="primary"></q-icon>
                        </q-item-section>
                        <q-item-section>System Settings</q-item-section>
                      </q-item>
                    </q-list>
                  </q-menu>
                </q-item>

              </q-list>
            </q-menu>
          </q-btn>

          <label class="q-ml-md">
            {{ userdataStore.username }}
          </label>
        </div>

        <q-toolbar-title class="absolute-center">
          <router-link to="/" class="silent-link">
            {{ data }}
          </router-link>
        </q-toolbar-title>

      </q-toolbar>

      <!--
      <q-tabs align="center">
        <q-route-tab to="/" label="Home"/>
        <q-route-tab to="/account" label="Account" v-if="userdataStore.authenticated"/>
      </q-tabs>
      -->
    </q-header>

    <q-page-container class="col" style="max-width: 73.75rem; margin-right: auto; margin-left: auto;">
      <router-view/>
    </q-page-container>

  </q-layout>
</template>

<script lang="ts">

import {defineComponent, ref} from 'vue';
import {useQuasar} from 'quasar';
import {api} from 'boot/axios';
import {useUserdataStore} from 'stores/userdata';
import {useRouter} from 'vue-router';
import {useLocaleStore} from 'stores/locale';
import {useLicenseStore} from 'stores/license';

export default defineComponent({
  name: 'MainLayout',
  setup() {
    const $q = useQuasar();
    const $r = useRouter();

    const data = ref('')

    const userdataStore = useUserdataStore();
    const localeStore = useLocaleStore();
    const licenseStore = useLicenseStore();

    function loadLocales() {
      api.get('/api/configuration/locale')
        .then((response) => {
          localeStore.setAvailableLocales(response.data);
        })
        .catch(() => {
          $q.notify({
            color: 'negative',
            position: 'top',
            message: 'Loading locales failed',
            icon: 'report_problem'
          })
        })
    }

    function loadTitle() {
      api.get('/api/configuration/title')
        .then((response) => {
          data.value = response.data;
          document.title = data.value;
        })
        .catch(() => {
          $q.notify({
            color: 'negative',
            position: 'top',
            message: 'Loading title failed',
            icon: 'report_problem'
          })
        })
    }

    function loadUserData() {
      api.get('/api/configuration/user')
        .then((response) => {
          userdataStore.setUserdata(response.data);
        })
        .catch(() => {
          $q.notify({
            color: 'negative',
            position: 'top',
            message: 'Loading UserData failed',
            icon: 'report_problem'
          })
        });
    }

    function loadLicense() {
      api.get('/api/configuration/license/translated')
        .then((response) => {
          licenseStore.setLicense(response.data);
        })
        .catch(() => {
          $q.notify({
            color: 'negative',
            position: 'top',
            message: 'Loading License failed',
            icon: 'report_problem'
          })
        });
    }

    function home() {
      $r.push('/')
    }

    function login() {
      $r.push('/user-login')
    }

    function accountSettings() {
      $r.push('/account')
    }

    function propertiesAdministration() {
      $r.push('/administration/configuration/properties/edit')
    }

    function tagsAdministration() {
      $r.push('/administration/configuration/tags/edit')
    }

    function systemSettings() {
      $r.push('/administration/system')
    }

    function logout() {
      api.get('/api/auth/logout')
        .then(() => {
          api.get('/api/configuration/user')
            .then((response) => {
              userdataStore.setUserdata(response.data);
              $q.notify({
                color: 'positive',
                position: 'top',
                message: 'Logout successful',
                icon: 'report'
              })
              $r.push('/');
            })
            .catch(() => {
              $q.notify({
                color: 'negative',
                position: 'top',
                message: 'Loading UserData failed',
                icon: 'report_problem'
              })
            });
        })
        .catch(() => {
          $q.notify({
            color: 'negative',
            position: 'top',
            message: 'Logout failed',
            icon: 'report_problem'
          })
        })
    }

    return {
      data,
      userdataStore,
      loadTitle,
      loadLocales,
      loadUserData,
      loadLicense,
      home,
      login,
      logout,
      accountSettings,
      propertiesAdministration,
      tagsAdministration,
      systemSettings
    }
  },
  mounted() {
    this.loadLocales();
    this.loadTitle();
    this.loadUserData();
    this.loadLicense();
  }
});

</script>
