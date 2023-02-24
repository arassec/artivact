<template>
  <q-layout view="hHh lpR fFf">

    <q-header elevated class="bg-primary text-white" height-hint="98">
      <q-toolbar class="q-pl-xs">

        <div class="fixed-right">

          <q-btn flat color="white" class="q-mt-xs" icon="view_in_ar" v-if="userdataStore.authenticated">
            <q-tooltip>Artivact Configuration</q-tooltip>
            <q-menu anchor="bottom middle" self="top middle">
              <q-list>
                <q-item clickable v-close-popup @click="propertiesAdministration" v-if="userdataStore.isAdmin">
                  <q-item-section avatar>
                    <q-icon name="article" size="xs" color="primary"></q-icon>
                  </q-item-section>
                  <q-item-section>Properties</q-item-section>
                </q-item>
                <q-item clickable v-close-popup @click="tagsAdministration" v-if="userdataStore.isAdmin">
                  <q-item-section avatar>
                    <q-icon name="label" size="xs" color="primary"></q-icon>
                  </q-item-section>
                  <q-item-section>Tags</q-item-section>
                </q-item>
                <q-item clickable v-close-popup @click="createArtivact">
                  <q-item-section avatar>
                    <q-icon name="add" size="xs" color="primary"></q-icon>
                  </q-item-section>
                  <q-item-section>Add new</q-item-section>
                </q-item>
              </q-list>
            </q-menu>
          </q-btn>

          <q-btn flat color="white" class="q-mt-xs" icon="settings" v-if="userdataStore.isAdmin">
            <q-tooltip>System Settings</q-tooltip>
            <q-menu anchor="bottom middle" self="top middle">
              <q-list>
                <q-item clickable v-close-popup @click="systemSettings">
                  <q-item-section avatar>
                    <q-icon name="settings" size="xs" color="primary"></q-icon>
                  </q-item-section>
                  <q-item-section>System Settings</q-item-section>
                </q-item>
              </q-list>
            </q-menu>
          </q-btn>

          <router-link to="/account" class="silent-link" v-if="userdataStore.authenticated">
            <q-btn icon="manage_accounts" flat class="q-mt-xs">
              <q-tooltip>Account Settings</q-tooltip>
            </q-btn>
          </router-link>

          <router-link to="/user-login" class="silent-link" v-if="!userdataStore.authenticated">
            <q-btn flat color="white" icon="login" size="md" class="q-mr-xs q-mt-xs">
              <q-tooltip>Login</q-tooltip>
            </q-btn>
          </router-link>

          <router-link to="/" class="silent-link" v-if="userdataStore.authenticated">
            <q-btn flat color="white" icon-right="logout" size="md" class="q-mt-xs q-mr-xs" @click="logout">
              <label class="q-mr-sm">
                {{ userdataStore.username }}
              </label>
              <q-tooltip>Logout</q-tooltip>
            </q-btn>
          </router-link>

        </div>

        <router-link to="/" class="silent-link">
          <q-btn flat>
            {{ data }}
          </q-btn>
        </router-link>

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
            position: 'bottom',
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
            position: 'bottom',
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
            position: 'bottom',
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
            position: 'bottom',
            message: 'Loading License failed',
            icon: 'report_problem'
          })
        });
    }

    function propertiesAdministration() {
      $r.push('/administration/configuration/properties/edit')
    }

    function tagsAdministration() {
      $r.push('/administration/configuration/tags/edit')
    }

    function createArtivact() {
      api.post('/api/artivact/new')
        .then((response) => {
          $r.push('/administration/configuration/artivact/' + response.data)
        })
        .catch(() => {
          $q.notify({
            color: 'negative',
            position: 'bottom',
            message: 'Creating artivact failed',
            icon: 'report_problem'
          })
        });
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
                position: 'bottom',
                message: 'Logout successful',
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
      logout,
      propertiesAdministration,
      tagsAdministration,
      createArtivact,
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
