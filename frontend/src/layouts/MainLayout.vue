<template>
  <q-layout view="hHh lpR fFf" data-test="artivact-main-layout">
    <q-header
      :elevated="!profilesStore.isE2eModeEnabled"
      class="bg-primary page-header"
      height-hint="98"
      data-test="artivact-menu-bar"
    >
      <q-toolbar class="q-pl-xs page-toolbar">
        <q-toolbar-title class="q-mt-xs q-mb-xs">
          <artivact-menu-bar/>
        </q-toolbar-title>

        <label v-if="localeStore.selectedLocale !== null" class="q-mr-sm">
          {{ localeStore.selectedLocale }}
        </label>

        <artivact-settings-bar/>

        <!-- FAVORITES MENU -->
        <q-btn
          v-if="userdataStore.authenticated"
          data-test="favorites-menu-button"
          flat
          color="white"
          icon="star"
          size="md"
          class="q-mr-sm"
        >
          <q-tooltip>{{ $t('MainLayout.favorites') }}</q-tooltip>
          <q-menu>
            <q-list style="min-width: 250px">
              <div v-if="favoritesStore.favoritesList.length === 0" class="q-pa-md text-center text-grey">
                {{ $t('MainLayout.noFavorites') }}
              </div>
              <q-item
                v-for="favorite in favoritesStore.favoritesList"
                :key="favorite.itemId"
                clickable
                @click="navigateToItem(favorite.itemId)"
              >
                <q-item-section avatar v-if="favorite.thumbnailUrl">
                  <q-avatar rounded>
                    <img alt="favorite-thumb" :src="favorite.thumbnailUrl + '?imageSize=FAVORITE'"/>
                  </q-avatar>
                </q-item-section>
                <q-item-section>
                  <q-item-label>{{ favorite.title }}</q-item-label>
                </q-item-section>
                <q-item-section side>
                  <q-btn
                    flat
                    round
                    dense
                    icon="close"
                    size="sm"
                    @click.stop="removeFavorite(favorite.itemId)"
                  >
                    <q-tooltip>{{ $t('MainLayout.removeFavorite') }}</q-tooltip>
                  </q-btn>
                </q-item-section>
              </q-item>
            </q-list>
          </q-menu>
        </q-btn>

        <router-link
          to="/user-login"
          v-if="
            !userdataStore.authenticated && profilesStore.isServerModeEnabled
          "
          class="menu-entry-link q-mt-xs q-mb-xs"
        >
          <q-btn
            data-test="login-button"
            flat
            color="white"
            icon="login"
            size="md"
          >
            <q-tooltip>{{ $t('MainLayout.login') }}</q-tooltip>
          </q-btn>
        </router-link>

        <router-link
          to="/"
          v-if="
            userdataStore.authenticated && profilesStore.isServerModeEnabled
          "
          class="menu-entry-link"
        >
          <q-btn
            data-test="logout-button"
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
            <q-tooltip>{{ $t('MainLayout.logout') }}</q-tooltip>
          </q-btn>
        </router-link>
      </q-toolbar>
    </q-header>

    <q-page-container>
      <q-page class="bg-accent" data-test="artivact-main-page">
        <router-view :key="$route.fullPath"/>
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import {onMounted} from 'vue';
import {useQuasar} from 'quasar';
import {api} from '../boot/axios';
import {useUserdataStore} from '../stores/userdata';
import {useLocaleStore} from '../stores/locale';
import {useMenuStore} from '../stores/menu';
import ArtivactMenuBar from '../components/ArtivactMenuBar.vue';
import ArtivactSettingsBar from '../components/ArtivactSettingsBar.vue';
import {useI18n} from 'vue-i18n';
import {useProfilesStore} from '../stores/profiles';
import {useApplicationSettingsStore} from '../stores/application-settings';
import {useFavoritesStore} from '../stores/favorites';
import {useRouter} from 'vue-router';

const quasar = useQuasar();
const i18n = useI18n();
const router = useRouter();

const userdataStore = useUserdataStore();
const applicationSettingsStore = useApplicationSettingsStore();
const localeStore = useLocaleStore();
const menuStore = useMenuStore();
const profilesStore = useProfilesStore();
const favoritesStore = useFavoritesStore();

function loadApplicationLocale() {
  if (applicationSettingsStore.applicationLocale) {
    i18n.locale.value = applicationSettingsStore.applicationLocale;
  }
}

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
        message: i18n.t('Common.messages.loading.failed', {
          item: i18n.t('Common.items.menus'),
        }),
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
        message: i18n.t('MainLayout.messages.userDataFailed'),
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
      favoritesStore.favorites = [];
      favoritesStore.loaded = false;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('MainLayout.messages.logoutFailed'),
        icon: 'report_problem',
      });
    });
}

function navigateToItem(itemId: string) {
  router.push(`/item/${itemId}`);
}

async function removeFavorite(itemId: string) {
  try {
    await favoritesStore.unmarkAsFavorite(itemId);
    quasar.notify({
      color: 'positive',
      position: 'bottom',
      message: i18n.t('MainLayout.messages.favoriteRemoved'),
      icon: 'done',
    });
  } catch (error) {
    quasar.notify({
      color: 'negative',
      position: 'bottom',
      message: i18n.t('MainLayout.messages.favoriteRemoveFailed'),
      icon: 'report_problem',
    });
  }
}

onMounted(() => {
  loadApplicationLocale();
  loadMenus();
  if (userdataStore.authenticated) {
    favoritesStore.loadFavorites();
  }
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
