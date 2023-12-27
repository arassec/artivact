<template>
  <div v-if="exhibitionsRef">
    <div class="q-mb-lg">
      Configures exhibitions of items. Exhibitions are published as ZIP files and can be downloaded and experienced
      in an Artivact viewer application.
    </div>

    <q-list bordered class="rounded-borders q-mb-lg">
      <q-expansion-item v-for="(exhibition, index) in exhibitionsRef" v-bind:key="index"
                        header-class="bg-primary text-white"
                        class="list-entry"
                        expand-separator
                        expand-icon-class="text-white">
        <template v-slot:header>
          <q-item-section class="list-entry-label">
            {{ translate(exhibition.title) }}
          </q-item-section>
        </template>
        <div class="row full-width">
          <q-card class="q-ma-md">
            <q-card-section>
              {{ translate(exhibition.description) }}
            </q-card-section>
          </q-card>
          <q-space/>
          <div class="q-mt-md q-mr-md">
            <q-btn class="q-mr-xs" round flat icon="delete"
                   @click="openDeleteExhibitionConfirmModal(exhibition)"></q-btn>
            <q-btn round flat icon="edit" @click="createOrEditExhibition(exhibition)"></q-btn>
          </div>
        </div>
      </q-expansion-item>
    </q-list>

    <!-- DELETE CONFIRMATION DIALOG -->
    <q-dialog v-model="showDeleteExhibitionConfirmModalRef" persistent>
      <q-card>
        <q-card-section class="row items-center">
          <q-icon
            name="warning"
            size="md"
            color="warning"
            class="q-mr-md"
          ></q-icon>
          <h3 class="av-text-h3">Delete Exhibition?</h3>
          <div class="q-ml-sm">
            Are you sure you want to delete this Exhibition and all its files?
            This action cannot be undone!
          </div>
        </q-card-section>
        <q-card-section>
          <q-btn flat label="Cancel" color="primary" v-close-popup/>
          <q-btn
            flat
            label="Delete Exhibition"
            color="primary"
            v-close-popup
            @click="deleteExhibition"
            class="float-right"
          />
        </q-card-section>
      </q-card>
    </q-dialog>

    <!-- ADD / UPDATE EXHIBITION -->
    <q-dialog v-model="showExhibitionConfigurationModalRef" persistent>
      <q-card class="q-mb-lg artivact-modal-content">
        <q-card-section class="bg-primary text-white">
          <div class="text-h6">{{ selectedExhibitionRef.exhibitionId === null ? 'Add' : 'Edit' }} Exhibition</div>
        </q-card-section>

        <q-card-section>
          <artivact-restricted-translatable-item-editor
            :locales="localeStore.locales"
            :translatable-string="selectedExhibitionRef.title"
            label="Title"
            :show-separator="false"
          />
        </q-card-section>
        <q-card-section>
          <artivact-restricted-translatable-item-editor
            :locales="localeStore.locales"
            :translatable-string="selectedExhibitionRef.description"
            label="Description"
            :show-separator="false"
            :textarea="true"
          />
        </q-card-section>
        <q-card-section>
          <div class="q-card--bordered q-pa-sm q-mb-md">
            <div class="q-mb-sm">Create Exhibition from Page(s)</div>
            <q-tree text-color="primary" control-color="primary"
                    :nodes="menuStore.menus"
                    node-key="id"
                    label-key="translatedValue"
                    children-key="menuEntries"
                    :dense="true"
                    v-model:ticked="selectedExhibitionRef.menuIds"
                    tick-strategy="leaf-filtered"
            />
          </div>
        </q-card-section>

        <q-card-section>
          <q-btn
            label="Cancel"
            color="primary"
            @click="showExhibitionConfigurationModalRef = false"
          />
          <q-btn
            label="Save"
            color="primary"
            class="float-right"
            @click="saveOrUpdateExhibitionSummary(selectedExhibitionRef)"
          />
        </q-card-section>
      </q-card>
    </q-dialog>

    <!-- LONG-RUNNING OPERATION -->
    <q-dialog v-model="showOperationInProgressModalRef" persistent>
      <q-card class="q-mb-lg image-set-modal">
        <q-card-section class="bg-primary text-white">
          <div class="text-h6">Operation in Progress</div>
        </q-card-section>
        <q-card-section>
          <q-spinner size="2em" class="q-mr-md"/> {{ progressMonitorRef?.progress }}
        </q-card-section>
      </q-card>
    </q-dialog>

    <div class="row">
      <q-space></q-space>
      <q-btn label="Add Exhibition" @click="createOrEditExhibition(null)" color="primary"/>
    </div>
  </div>
</template>

<script setup lang="ts">


import {api} from 'boot/axios';
import {onMounted, ref} from 'vue';
import {useQuasar} from 'quasar';
import {ExhibitionSummary, MenuTreeNode, OperationProgress} from 'components/models';
import {translate} from './utils';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';
import {useLocaleStore} from 'stores/locale';
import {useMenuStore} from 'stores/menu';

const quasar = useQuasar();
const localeStore = useLocaleStore();
const menuStore = useMenuStore();

const exhibitionsRef = ref([] as ExhibitionSummary[]);
const selectedExhibitionRef = ref({} as ExhibitionSummary);

const showExhibitionConfigurationModalRef = ref(false);
const showDeleteExhibitionConfirmModalRef = ref(false);

const menuTreeRef = ref([] as MenuTreeNode[])

const showOperationInProgressModalRef = ref(false);
const progressMonitorRef = ref<OperationProgress>({
  progress: 'Creating exhibition file...'
});

function createMenuTree() {
  let result = [] as MenuTreeNode[];
  menuStore.menus.forEach(menu => {
    let entry = {
      disabled: false,
      selectable: menu.menuEntries.length === 0,
    } as MenuTreeNode;
    result.push({...entry, ...menu});
  })
  menuTreeRef.value = result;
}

function loadExhibitionSummaries() {
  api
    .get('/api/exhibition')
    .then((response) => {
      exhibitionsRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Loading exhibitions failed',
        icon: 'report_problem',
      });
    });
}

function saveOrUpdateExhibitionSummary(exhibition: ExhibitionSummary) {
  showOperationInProgressModalRef.value = true;
  showExhibitionConfigurationModalRef.value = false;
  api
    .post('/api/exhibition', exhibition)
    .then(() => {
      loadExhibitionSummaries();
      showOperationInProgressModalRef.value = false;
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: 'Exhibition saved',
        icon: 'report',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Saving exhibition failed',
        icon: 'report_problem',
      });
    });
}

function openDeleteExhibitionConfirmModal(exhibition: ExhibitionSummary) {
  if (!exhibition) {
    return;
  }
  selectedExhibitionRef.value = exhibition;
  showDeleteExhibitionConfirmModalRef.value = true;
}

function deleteExhibition() {
  if (!selectedExhibitionRef.value.exhibitionId) {
    return;
  }
  api
    .delete('/api/exhibition/' + selectedExhibitionRef.value.exhibitionId)
    .then(() => {
      loadExhibitionSummaries();
      showDeleteExhibitionConfirmModalRef.value = false;
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: 'Exhibition deleted',
        icon: 'report',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Deleting exhibition failed',
        icon: 'report_problem',
      });
    });
}

function createOrEditExhibition(exhibition: ExhibitionSummary | null) {
  if (!exhibition) {
    selectedExhibitionRef.value = {
      exhibitionId: null,
      title: {
        value: '',
        translatedValue: '',
        translations: {}
      },
      description: {
        value: '',
        translatedValue: '',
        translations: {}
      },
      menuIds: []
    };
  } else {
    selectedExhibitionRef.value = exhibition;
  }
  createMenuTree();
  showExhibitionConfigurationModalRef.value = true;
}

onMounted(() => {
  loadExhibitionSummaries();
})
</script>

<style scoped>
.list-entry {
  border-bottom: 1px solid white;
}
</style>
