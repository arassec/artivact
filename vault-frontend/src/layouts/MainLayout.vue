<template>
  <q-layout view="hHh lpR fFf">

    <q-header elevated class="bg-primary text-white" height-hint="98">
      <q-toolbar class="q-pl-xs page-toolbar">

        <q-toolbar-title class="q-mt-xs q-mb-xs">

          <template v-for="menu in menuStore.menus" :key="menu.id">
            <!-- Only menu with target, no entries defined: -->
            <router-link :to="menu.target" v-if="menu.translatedMenuEntries.length === 0 && menu.target" class="menu-entry-link">
              <q-btn no-caps flat color="white" :label="menu.translatedValue"></q-btn>
            </router-link>

            <!-- Menu with entries -->
            <q-btn no-caps flat color="white" :label="menu.translatedValue"
                   v-if="menu.translatedMenuEntries.length > 0">
              <q-menu anchor="bottom middle" self="top middle">
                <q-list>
                  <template v-for="(menuEntry, menuEntryIndex) in menu.translatedMenuEntries" :key="menuEntryIndex">
                    <router-link :to="menuEntry.target" class="menu-entry-link">
                      <q-item clickable v-close-popup class="menu-entry">
                        <q-item-section>{{ menuEntry.translatedValue }}</q-item-section>
                      </q-item>
                    </router-link>
                  </template>
                </q-list>
              </q-menu>
            </q-btn>
          </template>

        </q-toolbar-title>

        <q-btn flat color="white" icon="settings" v-if="userdataStore.isAdmin">
          <q-tooltip v-if="!systemMenuOpen">System Settings</q-tooltip>
          <q-menu anchor="bottom middle" self="top middle" @before-show="systemMenuOpen = true"
                  @before-hide="systemMenuOpen = false">
            <q-list>
              <q-item clickable v-close-popup @click="menuSettings" class="menu-entry">
                <q-item-section><label class="menu-label">
                  <q-icon name="menu" size="xs" color="primary" class="q-mr-sm"></q-icon>
                  Main Menu</label>
                </q-item-section>
              </q-item>
              <q-item clickable v-close-popup @click="systemSettings" class="menu-entry">
                <q-item-section><label class="menu-label">
                  <q-icon name="plagiarism" size="xs" color="primary" class="q-mr-sm"></q-icon>
                  Filesystem Scan</label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-menu>
        </q-btn>

        <q-btn flat color="white" icon="view_in_ar" v-if="userdataStore.isUserOrAdmin">
          <q-tooltip v-if="!artivactMenuOpen">Artivact Settings</q-tooltip>
          <q-menu anchor="bottom middle" self="top middle" @before-show="artivactMenuOpen = true"
                  @before-hide="artivactMenuOpen = false">
            <q-list>
              <q-item clickable v-close-popup @click="createArtivact" class="menu-entry"
                      v-if="userdataStore.isUserOrAdmin">
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

        <q-btn flat color="white" icon="manage_accounts" v-if="userdataStore.authenticated">
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

        <router-link to="/user-login" v-if="!userdataStore.authenticated" class="menu-entry-link">
          <q-btn flat color="white" icon="login" size="md">
            <q-tooltip>Login</q-tooltip>
          </q-btn>
        </router-link>

        <router-link to="/" v-if="userdataStore.authenticated" class="menu-entry-link">
          <q-btn no-caps flat color="white" icon-right="logout" size="md" @click="logout">
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
import {useMenuStore} from 'stores/menu';

export default defineComponent({
  name: 'MainLayout',
  setup() {
    const $q = useQuasar();
    const $r = useRouter();

    const data = ref('')

    const userdataStore = useUserdataStore();
    const localeStore = useLocaleStore();
    const menuStore = useMenuStore();
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

    function loadMenus() {
      api.get('/api/configuration/menu')
        .then((response) => {
          menuStore.setAvailableMenus(response.data);
        })
        .catch(() => {
          $q.notify({
            color: 'negative',
            position: 'bottom',
            message: 'Loading menus failed',
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
      api.get('/api/configuration/license')
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

    function menuSettings() {
      $r.push('/administration/configuration/menu/edit')
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
          loadUserData();
          loadMenus();
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
      menuStore,
      artivactMenuOpen,
      systemMenuOpen,
      accountsMenuOpen,
      loadTitle,
      loadLocales,
      loadMenus,
      loadUserData,
      loadLicense,
      logout,
      propertiesAdministration,
      tagsAdministration,
      licenseAdministration,
      createArtivact,
      systemSettings,
      menuSettings,
      accountSettings,
      accountsConfiguration,
    }
  },
  mounted() {
    this.loadLocales();
    this.loadTitle();
    this.loadUserData();
    this.loadLicense();
    this.loadMenus();
  }
});

</script>

<style scoped>
.page-toolbar {
  min-height: 10px;
}

.menu-label:hover {
  cursor: pointer;
}

.menu-entry-link {
  text-decoration: none;
  color: var(--q-primary);
}

.menu-entry:hover {
  color: white;
  background: var(--q-secondary);
}
</style>
