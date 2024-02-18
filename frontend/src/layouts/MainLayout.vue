<template>
  <q-layout view="hHh lpR fFf">
    <q-header elevated class="bg-primary page-header" height-hint="98">
      <q-toolbar class="q-pl-xs page-toolbar">
        <q-toolbar-title class="q-mt-xs q-mb-xs">
          <artivact-menu-bar />
        </q-toolbar-title>

        <label v-if="localeStore.selectedLocale !== null" class="q-mr-sm">
          {{ localeStore.selectedLocale }}
        </label>

        <artivact-settings-bar />

        <router-link
          to="/user-login"
          v-if="!userdataStore.authenticated && !desktopStore.isDesktopModeEnabled"
          class="menu-entry-link q-mt-xs q-mb-xs"
        >
          <q-btn flat color="white" icon="login" size="md">
            <q-tooltip>{{$t("MainLayout.login")}}</q-tooltip>
          </q-btn>
        </router-link>

        <router-link
          to="/"
          v-if="userdataStore.authenticated && !desktopStore.isDesktopModeEnabled"
          class="menu-entry-link"
        >
          <q-btn
            no-caps
            flat
            color="white"
            icon-right="logout"
            size="md"
            @click="logout"
          >
            <label class="q-mr-sm cursor-pointer">
              {{ userdataStore.username }}
            </label>
            <q-tooltip>{{$t("MainLayout.logout")}}</q-tooltip>
          </q-btn>
        </router-link>
      </q-toolbar>
    </q-header>

    <q-page-container>
      <q-page class="bg-accent">
        <router-view :key="$route.fullPath" />
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {setCssVar, useQuasar} from 'quasar';
import {api} from 'boot/axios';
import {useUserdataStore} from 'stores/userdata';
import {useLocaleStore} from 'stores/locale';
import {useLicenseStore} from 'stores/license';
import {useMenuStore} from 'stores/menu';
import {useRoleStore} from 'stores/role';
import ArtivactMenuBar from 'components/ArtivactMenuBar.vue';
import ArtivactSettingsBar from 'components/ArtivactSettingsBar.vue';
import {useDesktopStore} from 'stores/desktop';

const quasar = useQuasar();

const data = ref('');

const userdataStore = useUserdataStore();
const localeStore = useLocaleStore();
const menuStore = useMenuStore();
const licenseStore = useLicenseStore();
const roleStore = useRoleStore();
const desktopStore = useDesktopStore();

function loadColorTheme() {
  api
    .get('/api/configuration/public/colortheme')
    .then((response) => {
      let colorTheme = response.data;
      setCssVar('primary', colorTheme.primary);
      setCssVar('secondary', colorTheme.secondary);
      setCssVar('accent', colorTheme.accent);
      setCssVar('dark', colorTheme.dark);
      setCssVar('positive', colorTheme.positive);
      setCssVar('negative', colorTheme.negative);
      setCssVar('info', colorTheme.info);
      setCssVar('warning', colorTheme.warning);
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Loading color theme failed',
        icon: 'report_problem',
      });
    });
}

function loadLocales() {
  api
    .get('/api/configuration/public/locale')
    .then((response) => {
      localeStore.setAvailableLocales(response.data);
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Loading locales failed',
        icon: 'report_problem',
      });
    });
}

function loadRoles() {
  api
    .get('/api/configuration/public/role')
    .then((response) => {
      roleStore.setAvailableRoles(response.data);
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Loading roles failed',
        icon: 'report_problem',
      });
    });
}

function loadMenus() {
  api
    .get('/api/configuration/public/menu')
    .then((response) => {
      menuStore.setAvailableMenus(response.data);
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Loading menus failed',
        icon: 'report_problem',
      });
    });
}

function loadTitle() {
  api
    .get('/api/configuration/public/title')
    .then((response) => {
      data.value = response.data;
      document.title = data.value;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Loading title failed',
        icon: 'report_problem',
      });
    });
}

function loadUserData() {
  api
    .get('/api/configuration/public/user')
    .then((response) => {
      userdataStore.setUserdata(response.data);
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Loading UserData failed',
        icon: 'report_problem',
      });
    });
}

function loadLicense() {
  api
    .get('/api/configuration/public/license')
    .then((response) => {
      licenseStore.setLicense(response.data);
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Loading License failed',
        icon: 'report_problem',
      });
    });
}

function logout() {
  api
    .get('/api/auth/logout')
    .then(() => {
      loadUserData();
      loadMenus();
      localeStore.setSelectedLocale(null);
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Logout failed',
        icon: 'report_problem',
      });
    });
}

onMounted(() => {
  loadColorTheme();
  loadLocales();
  loadRoles();
  loadTitle();
  loadUserData();
  loadLicense();
  loadMenus();
});
</script>

<style scoped>
.page-header {
  opacity: 90%;
  z-index: 4;
}

.page-toolbar {
  min-height: 10px;
}

.menu-entry-link {
  text-decoration: none;
  color: var(--q-primary);
}

.cursor-pointer {
  cursor: pointer;
}

</style>
