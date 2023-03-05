<template>
  <div>
    <q-list bordered class="rounded-borders q-mb-lg">
      <draggable :list="menuConfigurationProp.menus" item-key="id" group="menus" handle=".menu-move-icon">
        <template #item="{ element }">
          <q-expansion-item :label="element.value" group="menus" header-class="bg-primary text-white"
                            class="menu" expand-separator expand-icon-class="text-white">

            <template v-slot:header>
              <q-item-section avatar>
                <div class="text-white q-gutter-md">
                  <q-icon name="drag_indicator" class="menu-move-icon" size="lg"></q-icon>
                </div>
              </q-item-section>

              <q-item-section class="menu-label">
                  {{ element.value }}
              </q-item-section>

              <q-item-section side>
                <q-btn round dense flat class="float-right" color="white"
                       icon="delete" size="md" @click="deleteMenu(element)"></q-btn>
              </q-item-section>
            </template>

            <q-card class="q-mb-lg">

              <q-separator/>

              <q-card-section>
                <artivact-translatable-item-editor :item="element" :locales="locales" label="Menu"
                                                   :show-separator="false"/>
                <q-input outlined v-model="element.target" label="Target"></q-input>
              </q-card-section>

              <q-separator/>

              <q-card-section>
                <h2 class="av-text-h2">Menu Entries</h2>

                <div v-if="element.menuEntries.length === 0">There are no menu entries defined.</div>

                <div v-for="(menuEntry, index) in element.menuEntries" :key="index">
                  <q-btn round dense flat class="float-right q-ml-sm"
                         icon="delete" size="md" @click="deleteMenuEntry(element, index)"></q-btn>
                  <q-btn round dense flat class="float-right q-ml-sm" v-if="index > 0"
                         icon="arrow_upward" size="md" @click="moveUp(element, index)"></q-btn>
                  <q-btn round dense flat class="float-right q-ml-sm" v-if="index < (element.menuEntries.length -1)"
                         icon="arrow_downward" size="md" @click="moveDown(element, index)"></q-btn>

                  <artivact-translatable-item-editor :item="menuEntry" :locales="locales" label="Menu Entry"
                                                     :show-separator="false" class="q-mb-xs"/>

                  <q-input outlined v-model="menuEntry.target" label="Target" class="q-mb-lg"></q-input>

                  <q-separator class="q-mb-lg"/>
                </div>
              </q-card-section>

              <q-card-section>
                <div class="row">
                  <q-space></q-space>
                  <q-btn label="Add Entry" @click="addMenuEntry(element)" color="secondary"/>
                </div>
              </q-card-section>
            </q-card>
          </q-expansion-item>
        </template>
      </draggable>
    </q-list>

    <div class="row">
      <q-space></q-space>
      <q-btn label="Add Menu" @click="addMenu" color="primary"/>
    </div>

  </div>
</template>

<script setup lang="ts">
// noinspection ES6UnusedImports
import draggable from 'vuedraggable';
import {PropType, toRef} from 'vue';
import {Menu, MenuConfiguration, MenuEntry} from 'components/models';
import ArtivactTranslatableItemEditor from 'components/ArtivactTranslatableItemEditor.vue';

const props = defineProps({
  menuConfiguration: {
    required: true,
    type: Object as PropType<MenuConfiguration>
  },
  locales: {
    required: true,
    type: Object as PropType<string[]>
  }
});
const menuConfigurationProp = toRef(props, 'menuConfiguration');

function addMenu() {
  let menu: Menu = {
    id: '',
    value: 'New Menu',
    translations: {},
    restrictions: [],
    menuEntries: []
  };
  menuConfigurationProp.value?.menus.push(menu);
}

function addMenuEntry(menu: Menu) {
  let menuEntry: MenuEntry = {
    id: '',
    value: 'New Entry',
    translations: {},
    restrictions: [],
    page: ''
  }
  menu.menuEntries.push(menuEntry);
}

function deleteMenu(menu: Menu) {
  menuConfigurationProp.value.menus
    .splice(menuConfigurationProp.value.menus.indexOf(menu), 1);
}

function deleteMenuEntry(menu: Menu, index: number) {
  menu.menuEntries.splice(index, 1);
}

function moveUp(menu: Menu, index: number) {
  if (index > 0) {
    let el = menu.menuEntries[index];
    menu.menuEntries[index] = menu.menuEntries[index - 1];
    menu.menuEntries[index - 1] = el;
  }
}

function moveDown(menu: Menu, index: number) {
  if (index !== -1 && index < menu.menuEntries.length - 1) {
    let el = menu.menuEntries[index];
    menu.menuEntries[index] = menu.menuEntries[index + 1];
    menu.menuEntries[index + 1] = el;
  }
}

</script>

<style scoped>
.menu {
  border-bottom: 1px solid white;
}

.menu-label {
  font-size: large;
}
</style>
