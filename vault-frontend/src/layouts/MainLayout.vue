<template>
  <q-layout view="hHh lpR fFf">

    <q-header elevated class="bg-primary text-white" height-hint="98">
      <q-toolbar class="q-pl-xs">

        <q-toolbar-title>

          <router-link to="/" class="silent-link">
            <q-btn flat>
              {{ data }}
            </q-btn>
          </router-link>

        </q-toolbar-title>

          <q-btn flat color="white" class="q-mt-xs" icon="settings" v-if="userdataStore.isAdmin">
            <q-tooltip v-if="!systemMenuOpen">System Settings</q-tooltip>
            <q-menu anchor="bottom middle" self="top middle" @before-show="systemMenuOpen = true"
                    @before-hide="systemMenuOpen = false">
              <q-list>
                <q-item clickable v-close-popup @click="systemSettings" class="menu-entry">
                  <q-item-section><label class="menu-label">
                    <q-icon name="plagiarism" size="xs" color="primary" class="q-mr-sm"></q-icon>
                    Filesystem Scan</label>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-menu>
          </q-btn>

          <q-btn flat color="white" class="q-mt-xs" icon="view_in_ar" v-if="userdataStore.authenticated">
            <q-tooltip v-if="!artivactMenuOpen">Artivact Settings</q-tooltip>
            <q-menu anchor="bottom middle" self="top middle" @before-show="artivactMenuOpen = true"
                    @before-hide="artivactMenuOpen = false">
              <q-list>
                <q-item clickable v-close-popup @click="createArtivact" class="menu-entry">
                  <q-item-section><label class="menu-label">
                    <q-icon name="add" size="xs" color="primary" class="q-mr-sm"></q-icon>
                    Create Artivact</label>
                  </q-item-section>
                </q-item>
                <q-item clickable v-close-popup @click="propertiesAdministration" v-if="userdataStore.isAdmin"
                        class="menu-entry">
                  <q-item-section><label class="menu-label">
                    <q-icon name="article" size="xs" color="primary" class="q-mr-sm"></q-icon>
                    Properties</label>
                  </q-item-section>
                </q-item>
                <q-item clickable v-close-popup @click="tagsAdministration" v-if="userdataStore.isAdmin"
                        class="menu-entry">
                  <q-item-section><label class="menu-label">
                    <q-icon name="label" size="xs" color="primary" class="q-mr-sm"></q-icon>
                    Tags</label>
                  </q-item-section>
                </q-item>
                <q-item clickable v-close-popup @click="licenseAdministration" v-if="userdataStore.isAdmin"
                        class="menu-entry">
                  <q-item-section><label class="menu-label">
                    <q-icon name="policy" size="xs" color="primary" class="q-mr-sm"></q-icon>
                    License</label>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-menu>
          </q-btn>

          <q-btn flat color="white" class="q-mt-xs" icon="manage_accounts" v-if="userdataStore.authenticated">
            <q-tooltip v-if="!accountsMenuOpen">Account Settings</q-tooltip>
            <q-menu anchor="bottom middle" self="top middle" @before-show="accountsMenuOpen = true"
                    @before-hide="accountsMenuOpen = false">
              <q-list>
                <q-item clickable v-close-popup @click="accountSettings" class="menu-entry">
                  <q-item-section><label class="menu-label">
                    <q-icon name="person" size="xs" color="primary" class="q-mr-sm"></q-icon>
                    Account</label>
                  </q-item-section>
                </q-item>
                <q-item clickable v-close-popup @click="accountsConfiguration" v-if="userdataStore.isAdmin"
                        class="menu-entry">
                  <q-item-section><label class="menu-label">
                    <q-icon name="group" size="xs" color="primary" class="q-mr-sm"></q-icon>
                    Accounts</label>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-menu>
          </q-btn>

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

      </q-toolbar>
    </q-header>

    <q-page-container>
      <q-page>
        <router-view/>
      </q-page>
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

    const artivactMenuOpen = ref(false);
    const systemMenuOpen = ref(false);
    const accountsMenuOpen = ref(false);

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

    function licenseAdministration() {
      $r.push('/administration/configuration/license/edit')
    }

    function createArtivact() {
      api.post('/api/artivact/new')
        .then((response) => {
          $r.push('/administration/configuration/artivact/' + response.data);
          $q.notify({
            color: 'positive',
            position: 'bottom',
            message: 'Artivact created',
            icon: 'check'
          })

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
      $r.push('/administration/filesystem/scan')
    }

    function accountSettings() {
      $r.push('/account')
    }

    function accountsConfiguration() {
      $r.push('/administration/accounts')
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
      artivactMenuOpen,
      systemMenuOpen,
      accountsMenuOpen,
      loadTitle,
      loadLocales,
      loadUserData,
      loadLicense,
      logout,
      propertiesAdministration,
      tagsAdministration,
      licenseAdministration,
      createArtivact,
      systemSettings,
      accountSettings,
      accountsConfiguration,
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

<style scoped>
.menu-label:hover {
  cursor: pointer;
}

.menu-entry:hover {
  color: white;
  background: var(--q-secondary);
}
</style>
