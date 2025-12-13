<template>
  <div class="row gt-sm" data-test="artivact-settings-bar">

    <!-- LOCALE SELECTION -->
    <q-btn
      data-test="locale-selection-button"
      flat
      color="white"
      icon="language"
      v-if="userdataStore.isUserOrAdmin && localeStore.locales.length > 0"
    >
      <q-tooltip v-if="!localeMenuOpen">{{
          $t('ArtivactSettingsBar.tooltip.locales')
        }}
      </q-tooltip>
      <q-menu
        anchor="bottom middle"
        self="top middle"
        @before-show="localeMenuOpen = true"
        @before-hide="localeMenuOpen = false"
      >
        <q-list>
          <q-item
            data-test="artivact-locale-selection-default"
            clickable
            v-close-popup
            @click="changeLocale(null)"
            class="menu-entry"
          >
            <q-item-section>
              <label class="menu-label">{{
                  $t('ArtivactSettingsBar.default')
                }}</label>
            </q-item-section>
          </q-item>
          <template v-for="locale in localeStore.locales" :key="locale">
            <q-item
              :data-test="'artivact-locale-selection-' + locale"
              clickable
              v-close-popup
              @click="changeLocale(locale)"
              class="menu-entry"
            >
              <q-item-section>
                <label class="menu-label">{{ locale }}</label>
              </q-item-section>
            </q-item>
          </template>
        </q-list>
      </q-menu>
    </q-btn>

    <!-- FAVORITES MENU -->
    <q-btn
      v-if="userdataStore.authenticated && favoritesStore.favoritesList.length > 0"
      data-test="favorites-menu-button"
      flat
      color="white"
      icon="star"
      size="md"
      class="q-mr-sm"
    >
      <q-tooltip>{{ $t('MainLayout.favorites') }}</q-tooltip>
      <q-menu
        anchor="bottom middle"
        self="top middle"
      >
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
                <img alt="" :src="favorite.thumbnailUrl + '?imageSize=FAVORITE'"/>
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

    <!-- ITEM SETTINGS-->
    <q-btn
      data-test="item-settings-button"
      flat
      color="white"
      icon="view_in_ar"
      v-if="userdataStore.isUserOrAdmin"
    >
      <q-tooltip v-if="!itemMenuOpen">{{
          $t('ArtivactSettingsBar.itemSettings')
        }}
      </q-tooltip>
      <q-menu
        data-test="item-settings-menu"
        anchor="bottom middle"
        self="top middle"
        @before-show="itemMenuOpen = true"
        @before-hide="itemMenuOpen = false"
      >
        <q-list>
          <q-item
            data-test="create-item-button"
            clickable
            v-close-popup
            @click="createItem"
            class="menu-entry"
            v-if="userdataStore.isUserOrAdmin"
          >
            <q-item-section
            ><label class="menu-label">
              <q-icon
                name="add"
                size="xs"
                color="primary"
                class="q-mr-sm"
              ></q-icon>
              {{ $t('ArtivactSettingsBar.createItem') }}</label
            >
            </q-item-section>
          </q-item>
          <q-item
            data-test="scan-item-button"
            clickable
            v-close-popup
            @click="createItemAndStartScanning"
            class="menu-entry"
            v-if="
              userdataStore.isUserOrAdmin && profilesStore.isDesktopModeEnabled
            "
          >
            <q-item-section
            ><label class="menu-label">
              <q-icon
                name="3d_rotation"
                size="xs"
                color="primary"
                class="q-mr-sm"
              ></q-icon>
              {{ $t('ArtivactSettingsBar.scanItem') }}</label
            >
            </q-item-section>
          </q-item>
          <q-item
            data-test="import-item-button"
            @click="showImportItemModal = true"
            clickable
            v-close-popup
            class="menu-entry"
            v-if="userdataStore.isUserOrAdmin"
          >
            <q-item-section>
              <label class="menu-label">
                <q-icon
                  name="upload"
                  size="xs"
                  color="primary"
                  class="q-mr-sm"
                />
                {{ $t('ArtivactSettingsBar.importItem') }}</label
              >
            </q-item-section>
          </q-item>
          <q-item
            data-test="batch-process-button"
            clickable
            v-close-popup
            @click="gotoBatchPage()"
            class="menu-entry"
            v-if="userdataStore.isAdmin"
          >
            <q-item-section
            ><label class="menu-label">
              <q-icon
                name="double_arrow"
                size="xs"
                color="primary"
                class="q-mr-sm"
              ></q-icon>
              {{ $t('ArtivactSettingsBar.batch') }}</label
            >
            </q-item-section>
          </q-item>
        </q-list>
      </q-menu>
    </q-btn>

    <!-- SYSTEM SETTINGS -->
    <q-btn
      data-test="system-settings-button"
      flat
      color="white"
      icon="settings"
      v-if="userdataStore.isAdmin"
    >
      <q-tooltip v-if="!systemMenuOpen">{{
          $t('ArtivactSettingsBar.tooltip.systemSettings')
        }}
      </q-tooltip>
      <q-menu
        anchor="bottom middle"
        self="top middle"
        @before-show="systemMenuOpen = true"
        @before-hide="systemMenuOpen = false"
      >
        <q-list data-test="system-settings-menu">
          <q-item
            data-test="artivact-system-settings-properties"
            clickable
            v-close-popup
            @click="gotoPropertiesConfigurationPage"
            v-if="userdataStore.isAdmin"
            class="menu-entry"
          >
            <q-item-section
            ><label class="menu-label">
              <q-icon
                name="article"
                size="xs"
                color="primary"
                class="q-mr-sm"
              ></q-icon>
              {{ $t('Common.items.properties') }}</label
            >
            </q-item-section>
          </q-item>
          <q-item
            data-test="artivact-system-settings-tags"
            clickable
            v-close-popup
            @click="gotoTagsConfigurationPage"
            v-if="userdataStore.isAdmin"
            class="menu-entry"
          >
            <q-item-section
            ><label class="menu-label">
              <q-icon
                name="label"
                size="xs"
                color="primary"
                class="q-mr-sm"
              ></q-icon>
              {{ $t('Common.items.tags') }}</label
            >
            </q-item-section>
          </q-item>
          <q-item
            data-test="artivact-system-settings-exchange"
            clickable
            v-close-popup
            @click="gotoExchangeConfigurationPage"
            v-if="userdataStore.isAdmin"
            class="menu-entry"
          >
            <q-item-section
            ><label class="menu-label">
              <q-icon
                name="import_export"
                size="xs"
                color="primary"
                class="q-mr-sm"
              ></q-icon>
              {{ $t('ArtivactSettingsBar.exchange') }}</label
            >
            </q-item-section>
          </q-item>
          <q-item
            data-test="artivact-system-settings-appearance"
            clickable
            v-close-popup
            @click="gotoAppearanceConfigurationPage"
            v-if="userdataStore.isAdmin"
            class="menu-entry"
          >
            <q-item-section
            ><label class="menu-label">
              <q-icon
                name="palette"
                size="xs"
                color="primary"
                class="q-mr-sm"
              ></q-icon>
              {{ $t('ArtivactSettingsBar.appearance') }}</label
            >
            </q-item-section>
          </q-item>
          <q-item
            data-test="artivact-system-settings-peripherals"
            clickable
            v-close-popup
            @click="gotoPeripheralsConfigurationPage"
            v-if="userdataStore.isAdmin && profilesStore.isDesktopModeEnabled"
            class="menu-entry"
          >
            <q-item-section
            ><label class="menu-label">
              <q-icon
                name="electrical_services"
                size="xs"
                color="primary"
                class="q-mr-sm"
              ></q-icon>
              {{ $t('ArtivactSettingsBar.peripherals') }}</label
            >
            </q-item-section>
          </q-item>
        </q-list>
      </q-menu>
    </q-btn>

    <!-- ACCOUNT SETTINGS -->
    <q-btn
      data-test="account-settings-button"
      flat
      color="white"
      icon="manage_accounts"
      v-if="userdataStore.authenticated && profilesStore.isServerModeEnabled"
    >
      <q-tooltip v-if="!accountsMenuOpen">{{
          $t('ArtivactSettingsBar.tooltip.account')
        }}
      </q-tooltip>
      <q-menu
        anchor="bottom middle"
        self="top middle"
        @before-show="accountsMenuOpen = true"
        @before-hide="accountsMenuOpen = false"
      >
        <q-list>
          <q-item
            clickable
            v-close-popup
            @click="gotoAccountPage"
            class="menu-entry"
          >
            <q-item-section
            ><label class="menu-label">
              <q-icon
                name="person"
                size="xs"
                color="primary"
                class="q-mr-sm"
              ></q-icon>
              {{ $t('Common.items.account') }}</label
            >
            </q-item-section>
          </q-item>
          <q-item
            clickable
            v-close-popup
            @click="gotoAccountsPage"
            v-if="userdataStore.isAdmin"
            class="menu-entry"
          >
            <q-item-section
            ><label class="menu-label">
              <q-icon
                name="group"
                size="xs"
                color="primary"
                class="q-mr-sm"
              ></q-icon>
              {{ $t('Common.items.accounts') }}</label
            >
            </q-item-section>
          </q-item>
        </q-list>
      </q-menu>
    </q-btn>

    <!-- DOCUMENTATION -->
    <a
      data-test="documentation-button"
      href="/artivact/index.html"
      target="_blank"
    >
      <q-btn flat color="white" icon="help" v-if="userdataStore.authenticated">
        <q-tooltip>{{
            $t('ArtivactSettingsBar.tooltip.documentation')
          }}
        </q-tooltip>
      </q-btn>
    </a>

    <!-- IMPORT ITEM MODAL -->
    <artivact-dialog
      :data-test="'import-menu-modal'"
      :dialog-model="showImportItemModal"
    >
      <template v-slot:header>
        <div class="text-h6">
          {{ $t('ArtivactSettingsBar.dialog.import') }}
        </div>
      </template>
      <template v-slot:body>
        <q-card-section>
          <div class="q-mb-lg">
            {{ $t('ArtivactSettingsBar.dialog.importDescription') }}
          </div>
          <div class="row">
            <q-uploader
              :label="$t('ArtivactSettingsBar.dialog.importFileUpload')"
              accept=".artivact.item.zip"
              field-name="file"
              :no-thumbnails="true"
              auto-upload
              class="col"
              :url="'/api/item/import'"
              @uploaded="itemImported()"
            />
          </div>
        </q-card-section>
      </template>
      <template v-slot:cancel>
        <q-btn
          :label="$t('Common.cancel')"
          color="primary"
          @click="showImportItemModal = false"
        />
      </template>
    </artivact-dialog>
  </div>

  <!-- MENUS FOR SMALLER / MOBILE RESOLUTIONS -->
  <q-btn flat icon="more_vert" class="lt-md" v-if="userdataStore.authenticated">
    <q-menu anchor="bottom right" self="top middle">
      <q-list>
        <!-- LOCALE SELECTION -->
        <q-item
          clickable
          class="menu-entry"
          v-if="userdataStore.isUserOrAdmin && localeStore.locales.length > 0"
        >
          <q-item-section>
            <label class="menu-label">
              <q-icon name="language" size="sm" class="q-mr-sm"/>
              <label>{{ $t('ArtivactSettingsBar.locale') }}</label>
            </label>
          </q-item-section>
          <q-menu anchor="top end" self="top start">
            <q-list>
              <q-item
                clickable
                v-close-popup
                @click="localeStore.setSelectedLocale(null)"
                class="menu-entry"
              >
                <q-item-section
                ><label class="menu-label">{{
                    $t('ArtivactSettingsBar.default')
                  }}</label></q-item-section
                >
              </q-item>
              <template v-for="locale in localeStore.locales" :key="locale">
                <q-item
                  clickable
                  v-close-popup
                  @click="localeStore.setSelectedLocale(locale)"
                  class="menu-entry"
                >
                  <q-item-section
                  ><label class="menu-label">{{
                      locale
                    }}</label></q-item-section
                  >
                </q-item>
              </template>
            </q-list>
          </q-menu>
        </q-item>

        <!-- ITEM SETTINGS -->
        <q-item clickable class="menu-entry" v-if="userdataStore.isUserOrAdmin">
          <q-item-section>
            <label class="menu-label">
              <q-icon name="view_in_ar" size="sm" class="q-mr-sm"/>
              <label>{{ $t('Common.items.items') }}</label>
            </label>
          </q-item-section>
          <q-menu anchor="top end" self="top start">
            <q-list>
              <q-item
                clickable
                v-close-popup
                @click="createItem"
                class="menu-entry"
                v-if="userdataStore.isUserOrAdmin"
              >
                <q-item-section>
                  <label class="menu-label">
                    <q-icon
                      name="add"
                      size="xs"
                      color="primary"
                      class="q-mr-sm"
                    ></q-icon>
                    {{ $t('ArtivactSettingsBar.createItem') }}</label
                  >
                </q-item-section>
              </q-item>
              <q-item
                data-test="batch-process-button"
                clickable
                v-close-popup
                @click="gotoBatchPage()"
                class="menu-entry"
                v-if="userdataStore.isUserOrAdmin"
              >
                <q-item-section
                ><label class="menu-label">
                  <q-icon
                    name="double_arrow"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  ></q-icon>
                  {{ $t('ArtivactSettingsBar.batch') }}</label
                >
                </q-item-section>
              </q-item>
            </q-list>
          </q-menu>
        </q-item>

        <!-- SYSTEM SETTINGS -->
        <q-item clickable class="menu-entry" v-if="userdataStore.isAdmin">
          <q-item-section>
            <label class="menu-label">
              <q-icon name="settings" size="sm" class="q-mr-sm"/>
              <label>{{ $t('ArtivactSettingsBar.system') }}</label>
            </label>
          </q-item-section>
          <q-menu anchor="top end" self="top start">
            <q-list>
              <q-item
                clickable
                v-close-popup
                @click="gotoPropertiesConfigurationPage"
                v-if="userdataStore.isAdmin"
                class="menu-entry"
              >
                <q-item-section
                ><label class="menu-label">
                  <q-icon
                    name="article"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  ></q-icon>
                  {{ $t('Common.items.properties') }}</label
                >
                </q-item-section>
              </q-item>
              <q-item
                clickable
                v-close-popup
                @click="gotoTagsConfigurationPage"
                v-if="userdataStore.isAdmin"
                class="menu-entry"
              >
                <q-item-section
                ><label class="menu-label">
                  <q-icon
                    name="label"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  ></q-icon>
                  {{ $t('Common.items.tags') }}</label
                >
                </q-item-section>
              </q-item>
              <q-item
                clickable
                v-close-popup
                @click="gotoExchangeConfigurationPage"
                v-if="
                  userdataStore.isAdmin && profilesStore.isDesktopModeEnabled
                "
                class="menu-entry"
              >
                <q-item-section
                ><label class="menu-label">
                  <q-icon
                    name="import_export"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  ></q-icon>
                  {{ $t('ArtivactSettingsBar.exchange') }}</label
                >
                </q-item-section>
              </q-item>
              <q-item
                clickable
                v-close-popup
                @click="gotoAppearanceConfigurationPage"
                v-if="userdataStore.isAdmin"
                class="menu-entry"
              >
                <q-item-section
                ><label class="menu-label">
                  <q-icon
                    name="palette"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  ></q-icon>
                  {{ $t('ArtivactSettingsBar.appearance') }}</label
                >
                </q-item-section>
              </q-item>
            </q-list>
          </q-menu>
        </q-item>

        <!-- ACCOUNT SETTINGS -->
        <q-item
          clickable
          class="menu-entry"
          v-if="
            userdataStore.authenticated && profilesStore.isServerModeEnabled
          "
        >
          <q-item-section>
            <label class="menu-label">
              <q-icon name="manage_accounts" size="sm" class="q-mr-sm"/>
              <label>{{ $t('Common.items.accounts') }}</label>
            </label>
          </q-item-section>
          <q-menu anchor="top end" self="top start">
            <q-list>
              <q-item
                clickable
                v-close-popup
                @click="gotoAccountPage"
                class="menu-entry"
              >
                <q-item-section
                ><label class="menu-label">
                  <q-icon
                    name="person"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('Common.items.account') }}
                </label>
                </q-item-section>
              </q-item>
              <q-item
                clickable
                v-close-popup
                @click="gotoAccountsPage"
                v-if="userdataStore.isAdmin"
                class="menu-entry"
              >
                <q-item-section
                ><label class="menu-label">
                  <q-icon
                    name="group"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('Common.items.accounts') }}
                </label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-menu>
        </q-item>

        <!-- DOCUMENTATION -->
        <a href="/artivact/index.html" target="_blank" class="nav-link">
          <q-item
            clickable
            v-close-popup
            class="menu-entry"
            v-if="userdataStore.authenticated"
          >
            <q-item-section>
              <label class="menu-label">
                <q-icon name="help" size="sm" class="q-mr-sm"/>
                <label>{{ $t('ArtivactSettingsBar.documentation') }}</label>
              </label>
            </q-item-section>
          </q-item>
        </a>
      </q-list>
    </q-menu>
  </q-btn>
</template>

<script setup lang="ts">
import {ref} from 'vue';
import {useUserdataStore} from '../stores/userdata';
import {useLocaleStore} from '../stores/locale';
import {api} from '../boot/axios';
import {useRouter} from 'vue-router';
import {QUploader, useQuasar} from 'quasar';
import {useI18n} from 'vue-i18n';
import {useProfilesStore} from '../stores/profiles';
import ArtivactDialog from './ArtivactDialog.vue';
import {useFavoritesStore} from "../stores/favorites";
import {useBreadcrumbsStore} from "../stores/breadcrumbs";

const {locale} = useI18n({useScope: 'global'});

const quasar = useQuasar();
const router = useRouter();
const i18n = useI18n();

const userdataStore = useUserdataStore();
const localeStore = useLocaleStore();
const profilesStore = useProfilesStore();
const favoritesStore = useFavoritesStore();
const breadcrumbsStore = useBreadcrumbsStore();

const accountsMenuOpen = ref(false);
const itemMenuOpen = ref(false);
const systemMenuOpen = ref(false);
const localeMenuOpen = ref(false);

const showImportItemModal = ref(false);

function gotoPropertiesConfigurationPage() {
  router.push('/administration/configuration/properties');
}

function gotoTagsConfigurationPage() {
  router.push('/administration/configuration/tags');
}

function gotoAppearanceConfigurationPage() {
  router.push('/administration/configuration/appearance');
}

function gotoPeripheralsConfigurationPage() {
  router.push('/administration/configuration/peripherals');
}

function gotoExchangeConfigurationPage() {
  router.push('/administration/configuration/exchange');
}

function gotoAccountPage() {
  router.push('/account');
}

function gotoAccountsPage() {
  router.push('/administration/accounts');
}

function gotoBatchPage() {
  router.push('/administration/batch');
}

function createItem() {
  api
    .post('/api/item')
    .then((response) => {
      router.push('/administration/configuration/item/' + response.data);
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.creating.success', {
          item: i18n.t('Common.items.item'),
        }),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.creating.failed', {
          item: i18n.t('Common.items.item'),
        }),
        icon: 'report_problem',
      });
    });
}

function createItemAndStartScanning() {
  router.push('/wizzard/scanItem');
}

function changeLocale(selectedLocale: string | null) {
  localeStore.setSelectedLocale(selectedLocale);
  if (selectedLocale !== null) {
    locale.value = selectedLocale;
  } else {
    locale.value = navigator.language.split('-')[0];
  }
}

function itemImported() {
  showImportItemModal.value = false;
  quasar.notify({
    color: 'positive',
    position: 'bottom',
    message: i18n.t('Common.messages.creating.success', {
      item: i18n.t('Common.items.item'),
    }),
    icon: 'check',
  });
}

function navigateToItem(itemId: string) {
  breadcrumbsStore.resetBreadcrumbs();
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

</script>

<style scoped>
.menu-label:hover {
  cursor: pointer;
}

.menu-entry {
  min-width: 125px;
}

.menu-entry:hover {
  color: white;
  background: var(--q-secondary);
}

.nav-link {
  text-decoration: none;
}
</style>
