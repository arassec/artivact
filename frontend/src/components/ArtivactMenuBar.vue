<template>
  <div data-test="artivact-menu-bar" class="row gt-sm">

    <template v-for="(menu, index) in menuStore.menus" :key="menu.id">
      <!-- SINGLE MENU WITH PAGE -->
      <!-- Only menu with defined target, no entries defined: -->
      <q-btn
        :data-test="'menu-' + menu.value"
        v-if="menu.menuEntries.length === 0 && menu.targetPageId"
        no-caps
        flat
        color="white"
        :label="translate(menu)"
        class="q-mr-md"
        @click="gotoPage(menu.targetPageId, menu.translatedValue, null, null)"
      >
        <q-tooltip v-if="userdataStore.isAdmin">{{ $t('ArtivactMenuBar.tooltip.edit') }}</q-tooltip>
        <q-menu
          :data-test="'menu-context-menu-' + menu.value"
          v-model="showMenuRef[menu.id]"
          :context-menu="true"
          v-if="userdataStore.isAdmin"
        >
          <q-list>
            <q-item
              :data-test="'menu-edit-button-' + menu.value"
              clickable
              v-close-popup
              @click="editMenu(menu)"
              class="menu-entry"
            >
              <q-item-section>
                <label class="menu-label">
                  <q-icon
                    name="edit"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('ArtivactMenuBar.label.edit') }}</label
                >
              </q-item-section>
            </q-item>
            <q-item
              :data-test="'menu-delete-button-' + menu.value"
              clickable
              v-close-popup
              @click="showDeleteMenuConfirmation(menu)"
              class="menu-entry"
            >
              <q-item-section>
                <label class="menu-label">
                  <q-icon
                    name="delete"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('ArtivactMenuBar.label.delete') }}</label
                >
              </q-item-section>
            </q-item>
            <q-item
              :href="'/api/menu/' + menu.id + '/export'"
              :active="true"
              :data-test="'menu-export-button-' + menu.value"
              clickable
              v-close-popup
              class="menu-entry"
            >
              <q-item-section>
                <label class="menu-label">
                  <q-icon
                    name="file_download"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('ArtivactMenuBar.label.export') }}</label
                >
              </q-item-section>
            </q-item>
            <q-item
              :data-test="'menu-move-left-button-' + menu.value"
              v-if="index > 0"
              clickable
              v-close-popup
              @click="moveMenuUp(menuStore.menus, index)"
              class="menu-entry"
            >
              <q-item-section>
                <label class="menu-label">
                  <q-icon
                    name="chevron_left"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('ArtivactMenuBar.label.left') }}</label
                >
              </q-item-section>
            </q-item>
            <q-item
              :data-test="'menu-move-right-button-' + menu.value"
              v-if="index < menuStore.menus.length - 1 && userdataStore.isAdmin"
              clickable
              v-close-popup
              @click="moveMenuDown(menuStore.menus, index)"
              class="menu-entry"
            >
              <q-item-section>
                <label class="menu-label">
                  <q-icon
                    name="chevron_right"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('ArtivactMenuBar.label.right') }}</label
                >
              </q-item-section>
            </q-item>
          </q-list>
        </q-menu>
      </q-btn>

      <!-- MENU WITH SUB-ENTRIES -->
      <!-- Menu with entries or no entries and no page-->
      <q-btn
        :data-test="'menu-' + menu.value"
        class="q-mr-md"
        v-if="
          menu.menuEntries.length > 0 ||
          (menu.menuEntries.length == 0 &&
            !menu.targetPageId &&
            userdataStore.isAdmin)
        "
        no-caps
        flat
        :color="menu.menuEntries.length == 0 && !menu.targetPageId ? 'negative' : 'white'"
        :label="translate(menu)"
      >
        <q-tooltip v-if="userdataStore.isAdmin && !profilesStore.isE2eModeEnabled">
          {{ $t('ArtivactMenuBar.tooltip.edit') }}
        </q-tooltip
        >
        <q-menu :data-test="'menu-menu-' + menu.value"
                anchor="bottom middle" self="top middle">
          <q-list>
            <template
              v-for="(menuEntry, menuEntryIndex) in menu.menuEntries"
              :key="menuEntryIndex"
            >
              <q-item
                :data-test="'menu-entry-' + menuEntry.value"
                clickable
                v-close-popup
                class="menu-entry"
                @click="
                  gotoPage(
                    menuEntry.targetPageId,
                    menuEntry.translatedValue,
                    null,
                    menu.translatedValue
                  )
                "
              >
                <q-item-section>
                  <div class="row">
                    <label class="menu-label">{{ translate(menuEntry) }}</label>
                    <q-space></q-space>
                    <q-menu
                      :data-test="'menu-entry-context-menu-' + menuEntry.value"
                      v-model="showMenuRef[menuEntry.id]"
                      :context-menu="true"
                      v-if="userdataStore.isAdmin"
                    >
                      <q-list>
                        <q-item
                          :data-test="'menu-entry-edit-button-' + menuEntry.value"
                          clickable
                          v-close-popup
                          @click.capture.stop="editMenu(menuEntry)"
                          class="menu-entry"
                        >
                          <q-item-section>
                            <label class="menu-label">
                              <q-icon
                                name="edit"
                                size="xs"
                                color="primary"
                                class="q-mr-sm"
                              />
                              {{ $t('ArtivactMenuBar.label.editEntry') }}</label
                            >
                          </q-item-section>
                        </q-item>
                        <q-item
                          clickable
                          v-close-popup
                          @click.capture.stop="
                            showDeleteMenuConfirmation(menuEntry)
                          "
                          class="menu-entry"
                        >
                          <q-item-section>
                            <label class="menu-label">
                              <q-icon
                                name="delete"
                                size="xs"
                                color="primary"
                                class="q-mr-sm"
                              />
                              {{ $t('ArtivactMenuBar.label.deleteEntry') }}</label
                            >
                          </q-item-section>
                        </q-item>
                        <q-item
                          :href="'/api/menu/' + menuEntry.id + '/export'"
                          :data-test="'menu-export-button-' + menu.value"
                          clickable
                          v-close-popup
                          :active="true"
                          class="menu-entry"
                        >
                          <q-item-section>
                            <label class="menu-label">
                              <q-icon
                                name="file_download"
                                size="xs"
                                color="primary"
                                class="q-mr-sm"
                              />
                              {{ $t('ArtivactMenuBar.label.exportEntry') }}</label
                            >
                          </q-item-section>
                        </q-item>
                        <q-item
                          v-if="menuEntryIndex > 0"
                          clickable
                          v-close-popup
                          @click="moveMenuUp(menu.menuEntries, menuEntryIndex)"
                          class="menu-entry"
                        >
                          <q-item-section>
                            <label class="menu-label">
                              <q-icon
                                name="expand_less"
                                size="xs"
                                color="primary"
                                class="q-mr-sm"
                              />
                              {{ $t('ArtivactMenuBar.label.up') }}</label
                            >
                          </q-item-section>
                        </q-item>
                        <q-item
                          v-if="
                            menuEntryIndex < menu.menuEntries.length - 1 &&
                            userdataStore.isAdmin
                          "
                          clickable
                          v-close-popup
                          @click="
                            moveMenuDown(menu.menuEntries, menuEntryIndex)
                          "
                          class="menu-entry"
                        >
                          <q-item-section>
                            <label class="menu-label">
                              <q-icon
                                name="expand_more"
                                size="xs"
                                color="primary"
                                class="q-mr-sm"
                              />
                              {{ $t('ArtivactMenuBar.label.down') }}</label
                            >
                          </q-item-section>
                        </q-item>
                      </q-list>
                    </q-menu>
                  </div>
                </q-item-section>
              </q-item>
            </template>
          </q-list>
        </q-menu>
        <q-menu
          :data-test="'menu-context-menu-' + menu.value"
          v-model="showMenuRef[menu.id]"
          v-if="userdataStore.isAdmin"
          :context-menu="true"
        >
          <q-list>
            <q-item
              :data-test="'menu-edit-button-' + menu.value"
              clickable
              v-close-popup
              @click="editMenu(menu)"
              class="menu-entry"
            >
              <q-item-section>
                <label class="menu-label">
                  <q-icon
                    name="edit"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('ArtivactMenuBar.label.edit') }}</label
                >
              </q-item-section>
            </q-item>
            <q-item
              :data-test="'menu-delete-button-' + menu.value"
              clickable
              v-close-popup
              @click="showDeleteMenuConfirmation(menu)"
              class="menu-entry"
            >
              <q-item-section>
                <label class="menu-label">
                  <q-icon
                    name="delete"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('ArtivactMenuBar.label.delete') }}</label
                >
              </q-item-section>
            </q-item>
            <q-item
              :data-test="'menu-add-page-button-' + menu.value"
              clickable
              v-close-popup
              @click="addPage(menu)"
              class="menu-entry"
              v-if="
                !menu.targetPageId &&
                menu.menuEntries.length == 0 &&
                userdataStore.isAdmin
              "
            >
              <q-item-section
              ><label class="menu-label">
                <q-icon
                  name="add"
                  size="xs"
                  color="primary"
                  class="q-mr-sm"
                ></q-icon>
                {{ $t('ArtivactMenuBar.label.add') }}</label
              >
              </q-item-section>
            </q-item>
            <q-item
              :data-test="'menu-add-entry-button-' + menu.value"
              clickable
              v-close-popup
              @click="addMenuEntry(menu)"
              class="menu-entry"
              v-if="!menu.targetPageId && userdataStore.isAdmin"
            >
              <q-item-section
              ><label class="menu-label">
                <q-icon
                  name="add"
                  size="xs"
                  color="primary"
                  class="q-mr-sm"
                ></q-icon>
                {{ $t('ArtivactMenuBar.label.addEntry') }}</label
              >
              </q-item-section>
            </q-item>
            <q-item
              :href="'/api/menu/' + menu.id + '/export'"
              :active="true"
              :data-test="'menu-export-button-' + menu.value"
              clickable
              v-close-popup
              class="menu-entry"
            >
              <q-item-section>
                <label class="menu-label">
                  <q-icon
                    name="file_download"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('ArtivactMenuBar.label.export') }}</label
                >
              </q-item-section>
            </q-item>
            <q-item
              :data-test="'menu-move-left-button-' + menu.value"
              v-if="index > 0"
              clickable
              v-close-popup
              @click="moveMenuUp(menuStore.menus, index)"
              class="menu-entry"
            >
              <q-item-section>
                <label class="menu-label">
                  <q-icon
                    name="chevron_left"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('ArtivactMenuBar.label.left') }}</label
                >
              </q-item-section>
            </q-item>
            <q-item
              :data-test="'menu-move-right-button-' + menu.value"
              v-if="index < menuStore.menus.length - 1 && userdataStore.isAdmin"
              clickable
              v-close-popup
              @click="moveMenuDown(menuStore.menus, index)"
              class="menu-entry"
            >
              <q-item-section>
                <label class="menu-label">
                  <q-icon
                    name="chevron_right"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('ArtivactMenuBar.label.right') }}</label
                >
              </q-item-section>
            </q-item>
          </q-list>
        </q-menu>
      </q-btn>
    </template>

    <!-- MENU MANAGEMENT -->
    <!-- Menu with entries to create or import menus -->
    <q-btn flat v-if="userdataStore.isAdmin">
      <q-icon name="add" color="white"></q-icon>
      <q-menu>
        <q-list>
          <q-item data-test="create-menu-button"
                  @click="createMenu"
                  clickable
                  v-close-popup
                  class="menu-entry">
            <q-item-section>
              <label class="menu-label">
                <q-icon
                  name="add"
                  size="xs"
                  color="primary"
                  class="q-mr-sm"
                />
                {{ $t('ArtivactMenuBar.label.createMenu') }}</label
              >
            </q-item-section>
          </q-item>
          <q-item data-test="import-menu-button"
                  @click="showImportMenuModal = true"
                  clickable
                  v-close-popup
                  class="menu-entry">
            <q-item-section>
              <label class="menu-label">
                <q-icon
                  name="upload"
                  size="xs"
                  color="primary"
                  class="q-mr-sm"
                />
                {{ $t('ArtivactMenuBar.label.importMenu') }}</label
              >
            </q-item-section>
          </q-item>
        </q-list>
      </q-menu>
    </q-btn>

    <!-- IMPORT MENU MODAL -->
    <artivact-dialog :data-test="'import-menu-modal'" :dialog-model="showImportMenuModal">
      <template v-slot:header>
        <div class="text-h6">
          {{ $t('ArtivactMenuBar.dialog.import') }}
        </div>
      </template>
      <template v-slot:body>
        <q-card-section>
          <div class="q-mb-lg">
            {{ $t('ArtivactMenuBar.dialog.importDescription') }}
          </div>
          <div class="row">
            <q-uploader :label="$t('ArtivactMenuBar.dialog.importFileUpload')"
                        accept=".artivact.menu.zip"
                        field-name="file"
                        :no-thumbnails="true"
                        class="col"
                        :url="'/api/import/menu'"
                        @uploaded="menuImported()"
            />
          </div>
        </q-card-section>
      </template>
      <template v-slot:cancel>
        <q-btn
          :label="$t('Common.cancel')"
          color="primary"
          @click="showImportMenuModal = false"
        />
      </template>
    </artivact-dialog>

    <!-- ADD/EDIT MENU MODAL -->
    <artivact-dialog :data-test="'add-menu-modal'" :dialog-model="showMenuModal">
      <template v-slot:header>
        <div class="text-h6" v-if="!menuRef.id && !menuRef.parentId">
          {{ $t('ArtivactMenuBar.dialog.add') }}
        </div>
        <div class="text-h6" v-if="menuRef.id && !menuRef.parentId">
          {{ $t('ArtivactMenuBar.dialog.edit') }}
        </div>
        <div class="text-h6" v-if="!menuRef.id && menuRef.parentId">
          {{ $t('ArtivactMenuBar.dialog.addEntry') }}
        </div>
        <div class="text-h6" v-if="menuRef.id && menuRef.parentId">
          {{ $t('ArtivactMenuBar.dialog.editEntry') }}
        </div>
      </template>

      <template v-slot:body>
        <q-card-section>
          <div class="q-mb-xs" v-if="!menu.parentId">
            {{ $t('ArtivactMenuBar.dialog.description') }}
          </div>
          <div class="q-mb-xs" v-if="menu.parentId">
            {{ $t('ArtivactMenuBar.dialog.descriptionEntry') }}
          </div>
          <artivact-restricted-translatable-item-editor
            :dataTest="'add-menu-modal-menu-name'"
            :locales="localeStore.locales"
            :translatable-string="menuRef"
            :restricted-item="menuRef"
            :label="$t('ArtivactMenuBar.label.menu')"
            :show-separator="false"
          />
        </q-card-section>
      </template>

      <template v-slot:cancel>
        <q-btn
          :label="$t('Common.cancel')"
          color="primary"
          @click="showMenuModal = false"
        />
      </template>

      <template v-slot:approve>
        <q-btn
          data-test="add-menu-modal-approve-button"
          :label="$t('Common.save')"
          color="primary"
          @click="saveMenu(menuRef)"
        />
      </template>
    </artivact-dialog>

    <!-- DELETE MENU MODAL -->
    <artivact-dialog :dialog-model="confirmDeleteRef" :warn="true">
      <template v-slot:header>
        <h3 class="av-text-h3" v-if="!menuRef.parentId">{{ $t('ArtivactMenuBar.dialog.delete') }}</h3>
        <h3 class="av-text-h3" v-if="menuRef.parentId">{{ $t('ArtivactMenuBar.dialog.deleteEntry') }}</h3>
      </template>

      <template v-slot:body>
        <q-card-section>
          {{ $t('ArtivactMenuBar.dialog.deleteDescription') }}
        </q-card-section>
      </template>

      <template v-slot:cancel>
        <q-btn :label="$t('Common.cancel')" color="primary" @click="confirmDeleteRef = false" />
      </template>

      <template v-slot:approve>
        <q-btn
          data-test="delete-menu-approve-button"
          :label="menuRef.parentId ? $t('ArtivactMenuBar.dialog.deleteApproveEntry') : $t('ArtivactMenuBar.dialog.deleteApprove')"
          color="primary"
          @click="deleteMenu"
        />
      </template>
    </artivact-dialog>

  </div>

  <!-- MENUS FOR SMALL RESOLUTIONS / MOBILE RESOLUTIONS -->
  <q-btn flat icon="menu" class="lt-md" v-if="menuStore.menus.length > 0">
    <q-menu anchor="bottom middle" self="top middle">
      <q-list>
        <q-item
          clickable
          v-for="menu in menuStore.menus"
          :key="menu.id"
          class="menu-entry"
        >
          <!-- Only menu with defined target, no entries defined: -->
          <q-item-section
            v-if="menu.menuEntries.length === 0 && menu.targetPageId"
            @click="
              gotoPage(menu.targetPageId, menu.translatedValue, null, null)
            "
          >
            {{ menu.translatedValue }}
          </q-item-section>

          <!-- Menu with entries -->
          <q-item-section v-if="menu.menuEntries.length > 0">
            <label class="menu-label">
              {{ menu.translatedValue }}
              <q-icon name="chevron_right" size="sm"></q-icon>
            </label>
          </q-item-section>
          <q-menu
            v-if="menu.menuEntries.length > 0"
            anchor="top end"
            self="top start"
          >
            <q-list clickable v-close-popup class="menu-entry">
              <template
                v-for="(menuEntry, menuEntryIndex) in menu.menuEntries"
                :key="menuEntryIndex"
              >
                <router-link
                  :to="'/page/' + menuEntry.targetPageId"
                  class="menu-entry-link"
                >
                  <q-item clickable class="menu-entry" :key="menuEntryIndex">
                    <q-item-section>
                      {{ menuEntry.translatedValue }}
                    </q-item-section>
                  </q-item>
                </router-link>
              </template>
            </q-list>
          </q-menu>
        </q-item>
      </q-list>
    </q-menu>
  </q-btn>

</template>

<script setup lang="ts">
import { useMenuStore } from 'stores/menu';
import { api } from 'boot/axios';
import { QUploader, useQuasar } from 'quasar';
import { useRouter } from 'vue-router';
import { useUserdataStore } from 'stores/userdata';
import { ref } from 'vue';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';
import { useLocaleStore } from 'stores/locale';
import { moveDown, moveUp, translate } from 'components/artivact-utils';
import { Menu, TranslatableString } from 'components/artivact-models';
import { useBreadcrumbsStore } from 'stores/breadcrumbs';
import ArtivactDialog from 'components/ArtivactDialog.vue';
import { useI18n } from 'vue-i18n';
import { useProfilesStore } from 'stores/profiles';

const quasar = useQuasar();
const router = useRouter();
const i18n = useI18n();

const menuStore = useMenuStore();
const userdataStore = useUserdataStore();
const localeStore = useLocaleStore();
const breadcrumbsStore = useBreadcrumbsStore();
const profilesStore = useProfilesStore();

const showMenuRef = ref({} as Record<string, boolean>);

const showMenuModal = ref(false);
const menu: Menu = createEmptyMenuRef();
const menuRef = ref(menu);

const confirmDeleteRef = ref(false);

const showImportMenuModal = ref(false);

function createEmptyMenuRef(): Menu {
  return {
    id: '',
    value: '',
    translatedValue: '',
    restrictions: [],
    translations: {} as Record<string, string>,
    parentId: null,
    menuEntries: [],
    targetPageId: '',
    exportTitle: {} as TranslatableString,
    exportDescription: {} as TranslatableString
  };
}

function createMenu() {
  menuRef.value = createEmptyMenuRef();
  showMenuModal.value = true;
}

function saveMenu(menu: Menu) {
  let menuToSave = menu;
  let gotoPage = false;

  // Check if a menu _entry_ is added:
  if (menu.parentId !== null) {
    menuStore.menus.forEach((existingMenu) => {
      if (existingMenu.id === menu.parentId) {
        menuToSave = existingMenu;
        if (!menu.id) {
          // new menu entry:
          menuToSave.menuEntries.push(menu);
          gotoPage = true;
        } else {
          // update existing menu entry:
          menuToSave.menuEntries.forEach((existingMenuEntry) => {
            if (existingMenuEntry.id === menu.id) {
              existingMenuEntry.value = menu.value;
              existingMenuEntry.translations = menu.translations;
              existingMenuEntry.restrictions = menu.restrictions;
            }
          });
        }
      }
    });
  }

  api
    .post('/api/menu', menuToSave)
    .then((response) => {
      menuRef.value = createEmptyMenuRef();
      menuStore.setAvailableMenus(response.data);
      showMenuModal.value = false;
      if (gotoPage) {
        menuStore.menus.forEach((existingMenu) => {
          if (existingMenu.id === menu.parentId) {
            existingMenu.menuEntries.forEach(existingMenuEntry => {
              router.push('/page/' + existingMenuEntry.targetPageId);
            });
          }
        });
      }
      if (!profilesStore.isE2eModeEnabled) {
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: i18n.t('Common.messages.saving.success', { item: i18n.t('Common.items.menu') }),
          icon: 'check'
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', { item: i18n.t('Common.items.menu') }),
        icon: 'report_problem'
      });
    });
}

function editMenu(menu: Menu) {
  menuRef.value = menu;
  showMenuModal.value = true;
}

function showDeleteMenuConfirmation(menu: Menu) {
  menuRef.value = menu;
  confirmDeleteRef.value = true;
}

function deleteMenu() {
  api
    .delete('/api/menu/' + menuRef.value.id)
    .then((response) => {
      menuRef.value = createEmptyMenuRef();
      menuStore.setAvailableMenus(response.data);
      confirmDeleteRef.value = false;
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.deleting.success', { item: i18n.t('Common.items.menu') }),
        icon: 'check'
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.deleting.failed', { item: i18n.t('Common.items.menu') }),
        icon: 'report_problem'
      });
    });
}

function addMenuEntry(parentMenu: Menu) {
  menuRef.value = createEmptyMenuRef();
  menuRef.value.parentId = parentMenu.id;
  showMenuModal.value = true;
}

function addPage(menu: Menu) {
  api
    .post('/api/menu/' + menu.id + '/page')
    .then((response) => {
      menuStore.setAvailableMenus(response.data);
      menuStore.availableMenus.forEach(storedMenu => {
        if (menu.id === storedMenu.id) {
          router.push('/page/' + storedMenu.targetPageId);
        }
      });
      if (!profilesStore.isE2eModeEnabled) {
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: i18n.t('Common.messages.creating.success', { item: i18n.t('Common.items.page') }),
          icon: 'check'
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.creating.failed', { item: i18n.t('Common.items.page') }),
        icon: 'report_problem'
      });
    });
}

function moveMenuUp(array: [Menu], index: number) {
  hideMenus();
  moveUp(array, index);
  api
    .post('/api/menu/all', menuStore.menus)
    .then((response) => {
      menuStore.setAvailableMenus(response.data);
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ArtivactMenuBar.messages.movingFailed'),
        icon: 'report_problem'
      });
    });
}

function moveMenuDown(array: [Menu], index: number) {
  hideMenus();
  moveDown(array, index);
  api
    .post('/api/menu/all', menuStore.menus)
    .then((response) => {
      menuStore.setAvailableMenus(response.data);
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ArtivactMenuBar.messages.movingFailed'),
        icon: 'report_problem'
      });
    });
}

function gotoPage(
  pageId: string,
  pageLabel: string,
  parentPageId: string | null,
  parentPageLabel: string | null
) {
  breadcrumbsStore.resetBreadcrumbs();

  if (parentPageId && parentPageLabel) {
    breadcrumbsStore.addBreadcrumb({
      label: parentPageLabel,
      target: parentPageId
    });
  } else if (parentPageLabel) {
    breadcrumbsStore.addBreadcrumb({
      label: parentPageLabel,
      target: null
    });
  }

  breadcrumbsStore.addBreadcrumb({
    label: pageLabel,
    target: pageId
  });

  router.push('/page/' + pageId);
}

function hideMenus() {
  for (let prop in showMenuRef.value) {
    showMenuRef.value[prop] = false;
  }
}

function menuImported() {
  showImportMenuModal.value = false;
  api
    .get('/api/menu')
    .then((response) => {
      menuStore.setAvailableMenus(response.data);
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.creating.success', { item: i18n.t('Common.items.menus') }),
        icon: 'check'
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.menus') }),
        icon: 'report_problem',
      });
    });
}

</script>

<style scoped>
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
