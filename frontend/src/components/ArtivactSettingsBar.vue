<template>
  <div class="row gt-sm">
    <!-- LOCALE SELECTION -->
    <q-btn
      flat
      color="white"
      icon="language"
      v-if="userdataStore.isUserOrAdmin && localeStore.locales.length > 0"
    >
      <q-tooltip v-if="!localeMenuOpen">Locale Selection</q-tooltip>
      <q-menu
        anchor="bottom middle"
        self="top middle"
        @before-show="localeMenuOpen = true"
        @before-hide="localeMenuOpen = false"
      >
        <q-list>
          <q-item
            clickable
            v-close-popup
            @click="changeLocale(null)"
            class="menu-entry"
          >
            <q-item-section
            ><label class="menu-label">Default</label></q-item-section
            >
          </q-item>
          <template v-for="locale in localeStore.locales" :key="locale">
            <q-item
              clickable
              v-close-popup
              @click="changeLocale(locale)"
              class="menu-entry"
            >
              <q-item-section
              ><label class="menu-label">{{ locale }}</label></q-item-section
              >
            </q-item>
          </template>
        </q-list>
      </q-menu>
    </q-btn>

    <!-- EXHIBITION CONFIGURATION -->
    <router-link to="/administration/configuration/exhibitions">
      <q-btn flat color="white" icon="collections" v-if="userdataStore.isUserOrAdmin">
        <q-tooltip>Exhibitions</q-tooltip>
      </q-btn>
    </router-link>

    <!-- SYSTEM SETTINGS -->
    <q-btn flat color="white" icon="settings" v-if="userdataStore.isAdmin">
      <q-tooltip v-if="!systemMenuOpen">System Settings</q-tooltip>
      <q-menu
        anchor="bottom middle"
        self="top middle"
        @before-show="systemMenuOpen = true"
        @before-hide="systemMenuOpen = false"
      >
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
              Properties</label
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
              Tags</label
            >
            </q-item-section>
          </q-item>
          <q-item
            clickable
            v-close-popup
            @click="gotoLicenseConfigurationPage"
            v-if="userdataStore.isAdmin && !desktopStore.isDesktopModeEnabled"
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
              License</label
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
              Appearance</label
            >
            </q-item-section>
          </q-item>
          <q-item
            clickable
            v-close-popup
            @click="gotoPeripheralsConfigurationPage"
            v-if="userdataStore.isAdmin && desktopStore.isDesktopModeEnabled"
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
              Peripherals</label
            >
            </q-item-section>
          </q-item>
        </q-list>
      </q-menu>
    </q-btn>

    <!-- ITEM SETTINGS-->
    <q-btn
      flat
      color="white"
      icon="view_in_ar"
      v-if="userdataStore.isUserOrAdmin"
    >
      <q-tooltip v-if="!itemMenuOpen">Item Settings</q-tooltip>
      <q-menu
        anchor="bottom middle"
        self="top middle"
        @before-show="itemMenuOpen = true"
        @before-hide="itemMenuOpen = false"
      >
        <q-list>
          <q-item
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
              Create Item</label
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
              Import Items</label
            >
            </q-item-section>
          </q-item>
        </q-list>
      </q-menu>
    </q-btn>

    <!-- ACCOUNT SETTINGS -->
    <q-btn
      flat
      color="white"
      icon="manage_accounts"
      v-if="userdataStore.authenticated && !desktopStore.isDesktopModeEnabled"
    >
      <q-tooltip v-if="!accountsMenuOpen">Account Settings</q-tooltip>
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
              Account</label
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
              Accounts</label
            >
            </q-item-section>
          </q-item>
        </q-list>
      </q-menu>
    </q-btn>
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
              <label>Locale</label>
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
                ><label class="menu-label">Default</label></q-item-section
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

        <!-- EXHIBITION CONFIGURATION -->
        <q-item clickable v-close-popup @click="gotoExhibitionsConfigurationPage"
                class="menu-entry" v-if="userdataStore.isUserOrAdmin">
          <q-item-section>
            <label class="menu-label">
              <q-icon name="collections" size="sm" class="q-mr-sm"/>
              <label>Exhib.</label>
            </label>
          </q-item-section>
        </q-item>

        <!-- SYSTEM SETTINGS -->
        <q-item clickable class="menu-entry" v-if="userdataStore.isAdmin">
          <q-item-section>
            <label class="menu-label">
              <q-icon name="article" size="sm" class="q-mr-sm"/>
              <label>System</label>
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
                  Properties</label
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
                  Tags</label
                >
                </q-item-section>
              </q-item>
              <q-item
                clickable
                v-close-popup
                @click="gotoLicenseConfigurationPage"
                v-if="userdataStore.isAdmin && !desktopStore.isDesktopModeEnabled"
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
                  License</label
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
                  Appearance</label
                >
                </q-item-section>
              </q-item>
            </q-list>
          </q-menu>
        </q-item>

        <!-- ITEM SETTINGS -->
        <q-item clickable class="menu-entry" v-if="userdataStore.isUserOrAdmin">
          <q-item-section>
            <label class="menu-label">
              <q-icon name="add" size="sm" class="q-mr-sm"/>
              <label>Items</label>
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
                <q-item-section
                ><label class="menu-label">
                  <q-icon
                    name="add"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  ></q-icon>
                  Create Item</label
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
                  Import Items</label
                >
                </q-item-section>
              </q-item>
            </q-list>
          </q-menu>
        </q-item>

        <!-- ACCOUNT SETTINGS -->
        <q-item clickable class="menu-entry" v-if="userdataStore.authenticated && !desktopStore.isDesktopModeEnabled">
          <q-item-section>
            <label class="menu-label">
              <q-icon name="manage_accounts" size="sm" class="q-mr-sm"/>
              <label>Account</label>
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
                  Account
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
                  Accounts
                </label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-menu>
        </q-item>
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
import {useDesktopStore} from 'stores/desktop';

const {locale} = useI18n({useScope: 'global'});

const quasar = useQuasar();
const route = useRouter();

const userdataStore = useUserdataStore();
const localeStore = useLocaleStore();
const desktopStore = useDesktopStore();

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

function gotoExhibitionsConfigurationPage() {
  route.push('/administration/configuration/exhibitions');
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
        message: 'Item created',
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Creating item failed',
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
</style>
