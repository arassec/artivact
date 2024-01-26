<template>
  <div class="row gt-sm">
    <template v-for="(menu, index) in menuStore.menus" :key="menu.id">
      <!-- Only menu with defined target, no entries defined: -->
      <q-btn
        v-if="menu.menuEntries.length === 0 && menu.targetPageId"
        no-caps
        flat
        color="white"
        :label="translate(menu)"
        class="q-mr-md"
        @click="gotoPage(menu.targetPageId, menu.translatedValue, null, null)"
      >
        <q-tooltip v-if="userdataStore.isAdmin">Right click to edit menu</q-tooltip>
        <q-menu
          v-model="showMenuRef[menu.id]"
          :context-menu="true"
          v-if="userdataStore.isAdmin"
        >
          <q-list>
            <q-item
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
                  Edit Menu</label
                >
              </q-item-section>
            </q-item>
            <q-item
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
                  Delete Menu</label
                >
              </q-item-section>
            </q-item>
            <q-item
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
                  Move Left</label
                >
              </q-item-section>
            </q-item>
            <q-item
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
                  Move Right</label
                >
              </q-item-section>
            </q-item>
          </q-list>
        </q-menu>
      </q-btn>

      <!-- Menu with entries -->
      <q-btn
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
        <q-tooltip v-if="userdataStore.isAdmin"
        >Right click to edit menu
        </q-tooltip
        >
        <q-menu anchor="bottom middle" self="top middle">
          <q-list>
            <template
              v-for="(menuEntry, menuEntryIndex) in menu.menuEntries"
              :key="menuEntryIndex"
            >
              <q-item
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
                      v-model="showMenuRef[menuEntry.id]"
                      :context-menu="true"
                      v-if="userdataStore.isAdmin"
                    >
                      <q-list>
                        <q-item
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
                              Edit Menu Entry</label
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
                              Delete Menu Entry</label
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
                              Move Up</label
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
                              Move Down</label
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
          v-model="showMenuRef[menu.id]"
          v-if="userdataStore.isAdmin"
          :context-menu="true"
        >
          <q-list>
            <q-item
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
                Add Page</label
              >
              </q-item-section>
            </q-item>
            <q-item
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
                Add Entry</label
              >
              </q-item-section>
            </q-item>
            <q-item
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
                  Edit Menu</label
                >
              </q-item-section>
            </q-item>
            <q-item
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
                  Delete Menu</label
                >
              </q-item-section>
            </q-item>
            <q-item
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
                  Move Left</label
                >
              </q-item-section>
            </q-item>
            <q-item
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
                  Move Right</label
                >
              </q-item-section>
            </q-item>
          </q-list>
        </q-menu>
      </q-btn>
    </template>

    <q-btn flat v-if="userdataStore.isAdmin" @click="addMenu">
      <q-icon name="add" color="white"></q-icon>
    </q-btn>

    <artivact-dialog :dialog-model="showMenuModal">
      <template v-slot:header>
        <div class="text-h6" v-if="!menuRef.id && !menuRef.parentId">
          Add Menu
        </div>
        <div class="text-h6" v-if="menuRef.id && !menuRef.parentId">
          Edit Menu
        </div>
        <div class="text-h6" v-if="!menuRef.id && menuRef.parentId">
          Add Menu Entry
        </div>
        <div class="text-h6" v-if="menuRef.id && menuRef.parentId">
          Edit Menu Entry
        </div>
      </template>

      <template v-slot:body>
        <q-card-section>
          <div class="q-mb-xs" v-if="!menu.parentId">
            Enter the menu's name.
          </div>
          <div class="q-mb-xs" v-if="menu.parentId">
            Enter the menu entry's name.
          </div>
          <artivact-restricted-translatable-item-editor
            :locales="localeStore.locales"
            :translatable-string="menuRef"
            :restricted-item="menuRef"
            label="Menu"
            :show-separator="false"
          />
        </q-card-section>
      </template>

      <template v-slot:cancel>
        <q-btn
          label="Cancel"
          color="primary"
          @click="showMenuModal = false"
        />
      </template>

      <template v-slot:approve>
        <q-btn
          label="Save"
          color="primary"
          @click="saveMenu(menuRef)"
        />
      </template>
    </artivact-dialog>

    <artivact-dialog :dialog-model="confirmDeleteRef" :warn="true">
      <template v-slot:header>
        <h3 class="av-text-h3" v-if="!menuRef.parentId">Delete Menu?</h3>
        <h3 class="av-text-h3" v-if="menuRef.parentId">Delete Menu Entry?</h3>
      </template>

      <template v-slot:body>
        <q-card-section>
          Are you sure you want to delete this menu including its page and
          menu entries? This action cannot be undone!
        </q-card-section>
      </template>

      <template v-slot:cancel>
        <q-btn label="Cancel" color="primary" @click="confirmDeleteRef = false"/>
      </template>

      <template v-slot:approve>
        <q-btn
          :label="menuRef.parentId ? 'Delete Menu Entry' : 'Delete Menu'"
          color="primary"
          @click="deleteMenu"
        />
      </template>
    </artivact-dialog>

  </div>

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
import {useMenuStore} from 'stores/menu';
import {api} from 'boot/axios';
import {useQuasar} from 'quasar';
import {useRouter} from 'vue-router';
import {useUserdataStore} from 'stores/userdata';
import {ref} from 'vue';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';
import {useLocaleStore} from 'stores/locale';
import {moveDown, moveUp, translate} from 'components/utils';
import {Menu} from 'components/models';
import {useBreadcrumbsStore} from 'stores/breadcrumbs';
import ArtivactDialog from 'components/ArtivactDialog.vue';

const quasar = useQuasar();
const router = useRouter();

const menuStore = useMenuStore();
const userdataStore = useUserdataStore();
const localeStore = useLocaleStore();
const breadcrumbsStore = useBreadcrumbsStore();

const showMenuRef = ref({} as Record<string, boolean>);

const showMenuModal = ref(false);
const menu: Menu = createEmptyMenuRef();
const menuRef = ref(menu);

const confirmDeleteRef = ref(false);

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
  };
}

function addMenu() {
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
    .post('/api/configuration/menu', menuToSave)
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
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: 'Menu saved',
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Saving Menu failed',
        icon: 'report_problem',
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
    .delete('/api/configuration/menu/' + menuRef.value.id)
    .then((response) => {
      menuRef.value = createEmptyMenuRef();
      menuStore.setAvailableMenus(response.data);
      confirmDeleteRef.value = false;
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: 'Menu deleted',
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Deleting Menu failed',
        icon: 'report_problem',
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
    .post('/api/configuration/menu/' + menu.id + '/page')
    .then((response) => {
      menuStore.setAvailableMenus(response.data);
      menuStore.availableMenus.forEach(storedMenu => {
        if (menu.id === storedMenu.id) {
          router.push('/page/' + storedMenu.targetPageId)
        }
      })
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: 'Page created',
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Page creation failed',
        icon: 'report_problem',
      });
    });
}

function moveMenuUp(array: [Menu], index: number) {
  hideMenus();
  moveUp(array, index);
  api
    .post('/api/configuration/menu/all', menuStore.menus)
    .then((response) => {
      menuStore.setAvailableMenus(response.data);
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Moving failed',
        icon: 'report_problem',
      });
    });
}

function moveMenuDown(array: [Menu], index: number) {
  hideMenus();
  moveDown(array, index);
  api
    .post('/api/configuration/menu/all', menuStore.menus)
    .then((response) => {
      menuStore.setAvailableMenus(response.data);
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Moving failed',
        icon: 'report_problem',
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
      target: parentPageId,
    });
  } else if (parentPageLabel) {
    breadcrumbsStore.addBreadcrumb({
      label: parentPageLabel,
      target: null,
    });
  }

  breadcrumbsStore.addBreadcrumb({
    label: pageLabel,
    target: pageId,
  });

  router.push('/page/' + pageId);
}

function hideMenus() {
  for (let prop in showMenuRef.value) {
    showMenuRef.value[prop] = false;
  }
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
