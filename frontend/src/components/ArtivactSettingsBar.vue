<template>
  <div class="row gt-sm" data-test="artivact-settings-bar">

    <!-- LOCALE SELECTION -->
    <q-btn
      data-test="locale-selection-button"
      flat
      color="white"
      icon="language"
      v-if="userdataStore.isUserOrAdmin && localeStore.locales.length > 0">
      <q-tooltip v-if="!localeMenuOpen">{{ $t('ArtivactSettingsBar.tooltip.locales') }}</q-tooltip>
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
            class="menu-entry">
            <q-item-section>
              <label class="menu-label">{{ $t('ArtivactSettingsBar.default') }}</label>
            </q-item-section>
          </q-item>
          <template v-for="locale in localeStore.locales" :key="locale">
            <q-item
              :data-test="'artivact-locale-selection-' + locale"
              clickable
              v-close-popup
              @click="changeLocale(locale)"
              class="menu-entry">
              <q-item-section>
                <label class="menu-label">{{ locale }}</label>
              </q-item-section>
            </q-item>
          </template>
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
      <q-tooltip v-if="!itemMenuOpen">{{ $t('ArtivactSettingsBar.itemSettings') }}</q-tooltip>
      <q-menu
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
              {{ $t('ArtivactSettingsBar.createItem') }}</label>
            </q-item-section>
          </q-item>
          <q-item
            data-test="import-items-button"
            clickable
            v-close-popup
            @click="gotoItemImportPage"
            class="menu-entry"
            v-if="userdataStore.isAdmin"
          >
            <q-item-section
            ><label class="menu-label">
              <q-icon
                name="import_export"
                size="xs"
                color="primary"
                class="q-mr-sm"
              ></q-icon>
              {{ $t('ArtivactSettingsBar.importItems') }}</label>
            </q-item-section>
          </q-item>
        </q-list>
      </q-menu>
    </q-btn>

    <!-- SYSTEM SETTINGS -->
    <q-btn data-test="system-settings-button" flat color="white" icon="settings" v-if="userdataStore.isAdmin">
      <q-tooltip v-if="!systemMenuOpen">{{ $t('ArtivactSettingsBar.tooltip.systemSettings') }}</q-tooltip>
      <q-menu
        anchor="bottom middle"
        self="top middle"
        @before-show="systemMenuOpen = true"
        @before-hide="systemMenuOpen = false"
      >
        <q-list>
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
              {{ $t('Common.items.properties') }}</label>
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
              {{ $t('Common.items.tags') }}</label>
            </q-item-section>
          </q-item>
          <q-item
            data-test="artivact-system-settings-license"
            clickable
            v-close-popup
            @click="gotoLicenseConfigurationPage"
            v-if="userdataStore.isAdmin && profilesStore.isServerModeEnabled"
            class="menu-entry"
          >
            <q-item-section
            ><label class="menu-label">
              <q-icon
                name="policy"
                size="xs"
                color="primary"
                class="q-mr-sm"
              ></q-icon>
              {{ $t('ArtivactSettingsBar.license') }}</label>
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
              {{ $t('ArtivactSettingsBar.peripherals') }}</label>
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
              {{ $t('ArtivactSettingsBar.exchange') }}</label>
            </q-item-section>
          </q-item>
          <q-item
            data-test="artivact-system-settings-search"
            clickable
            v-close-popup
            @click="gotoSearchConfigurationPage"
            v-if="userdataStore.isAdmin"
            class="menu-entry"
          >
            <q-item-section
            ><label class="menu-label">
              <q-icon
                name="search"
                size="xs"
                color="primary"
                class="q-mr-sm"
              ></q-icon>
              {{ $t('ArtivactSettingsBar.search') }}</label>
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
      <q-tooltip v-if="!accountsMenuOpen">{{ $t('ArtivactSettingsBar.tooltip.account') }}</q-tooltip>
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
              {{ $t('Common.items.account') }}</label>
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
              {{ $t('Common.items.accounts') }}</label>
            </q-item-section>
          </q-item>
        </q-list>
      </q-menu>
    </q-btn>

    <!-- DOCUMENTATION -->
    <a data-test="documentation-button"
       href="/artivact/index.html" target="_blank">
      <q-btn flat color="white" icon="help" v-if="userdataStore.authenticated">
        <q-tooltip>{{ $t('ArtivactSettingsBar.tooltip.documentation') }}</q-tooltip>
      </q-btn>
    </a>
  </div>

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
                ><label class="menu-label">{{ $t('ArtivactSettingsBar.default') }}</label></q-item-section
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
                v-if="userdataStore.isUserOrAdmin">
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
                clickable
                v-close-popup
                @click="gotoItemImportPage"
                class="menu-entry"
                v-if="userdataStore.isAdmin"
              >
                <q-item-section
                ><label class="menu-label">
                  <q-icon
                    name="import_export"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  ></q-icon>
                  {{ $t('ArtivactSettingsBar.importItems') }}</label
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
              <q-icon name="article" size="sm" class="q-mr-sm"/>
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
                @click="gotoLicenseConfigurationPage"
                v-if="userdataStore.isAdmin && profilesStore.isServerModeEnabled"
                class="menu-entry"
              >
                <q-item-section
                ><label class="menu-label">
                  <q-icon
                    name="policy"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  ></q-icon>
                  {{ $t('ArtivactSettingsBar.license') }}</label
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
              <q-item
                clickable
                v-close-popup
                @click="gotoExchangeConfigurationPage"
                v-if="userdataStore.isAdmin && profilesStore.isDesktopModeEnabled"
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
            </q-list>
          </q-menu>
        </q-item>

        <!-- ACCOUNT SETTINGS -->
        <q-item clickable class="menu-entry" v-if="userdataStore.authenticated && profilesStore.isServerModeEnabled">
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
          <q-item clickable v-close-popup
                  class="menu-entry" v-if="userdataStore.authenticated">
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
import {useUserdataStore} from 'stores/userdata';
import {useLocaleStore} from 'stores/locale';
import {api} from 'boot/axios';
import {useRouter} from 'vue-router';
import {useQuasar} from 'quasar';
import {useI18n} from 'vue-i18n';
import {useProfilesStore} from 'stores/profiles';

const {locale} = useI18n({useScope: 'global'});

const quasar = useQuasar();
const route = useRouter();
const i18n = useI18n();

const userdataStore = useUserdataStore();
const localeStore = useLocaleStore();
const profilesStore = useProfilesStore();

const accountsMenuOpen = ref(false);
const itemMenuOpen = ref(false);
const systemMenuOpen = ref(false);
const localeMenuOpen = ref(false);

function gotoPropertiesConfigurationPage() {
  route.push('/administration/configuration/properties');
}

function gotoTagsConfigurationPage() {
  route.push('/administration/configuration/tags');
}

function gotoLicenseConfigurationPage() {
  route.push('/administration/configuration/license');
}

function gotoAppearanceConfigurationPage() {
  route.push('/administration/configuration/appearance');
}

function gotoPeripheralsConfigurationPage() {
  route.push('/administration/configuration/peripherals');
}

function gotoExchangeConfigurationPage() {
  route.push('/administration/configuration/exchange');
}

function gotoSearchConfigurationPage() {
  route.push('/administration/configuration/search');
}

function gotoAccountPage() {
  route.push('/account');
}

function gotoAccountsPage() {
  route.push('/administration/accounts');
}

function gotoItemImportPage() {
  route.push('/administration/item/import');
}

function createItem() {
  api
    .post('/api/item')
    .then((response) => {
      route.push('/administration/configuration/item/' + response.data);
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.creating.success', {item: i18n.t('Common.items.item')}),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.creating.failed', {item: i18n.t('Common.items.item')}),
        icon: 'report_problem',
      });
    });
}

function changeLocale(selectedLocale: string | null) {
  localeStore.setSelectedLocale(selectedLocale);
  if (selectedLocale !== null) {
    locale.value = selectedLocale;
  } else {
    locale.value = navigator.language.split('-')[0];
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
