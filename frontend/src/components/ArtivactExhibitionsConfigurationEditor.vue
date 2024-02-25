<template>
  <div v-if="exhibitionsRef">
    <div class="q-mb-lg">
      {{ $t('ArtivactExhibitionsConfigurationEditor.description') }}
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
                   @click="openDeleteExhibitionConfirmModal(exhibition)">
              <q-tooltip>{{ $t('ArtivactExhibitionsConfigurationEditor.button.delete') }}</q-tooltip>
            </q-btn>
            <q-btn round flat icon="edit" @click="createOrEditExhibition(exhibition)">
              <q-tooltip>{{ $t('ArtivactExhibitionsConfigurationEditor.button.edit') }}</q-tooltip>
            </q-btn>
          </div>
        </div>
      </q-expansion-item>
    </q-list>

    <!-- CREATE/EDIT EXHIBITION -->
    <artivact-dialog :dialog-model="showExhibitionConfigurationModalRef">
      <template v-slot:header>
        {{ selectedExhibitionRef.exhibitionId === null ? 'Add' : 'Edit' }} Exhibition
      </template>

      <template v-slot:body>
        <q-card-section>
          <artivact-restricted-translatable-item-editor
            :locales="localeStore.locales"
            :translatable-string="selectedExhibitionRef.title"
            :label="$t('ArtivactExhibitionsConfigurationEditor.dialog.title')"
            :show-separator="false"
          />
        </q-card-section>
        <q-card-section>
          <artivact-restricted-translatable-item-editor
            :locales="localeStore.locales"
            :translatable-string="selectedExhibitionRef.description"
            :label="$t('ArtivactExhibitionsConfigurationEditor.dialog.description')"
            :show-separator="false"
            :textarea="true"
          />
        </q-card-section>
        <q-card-section>
          <div class="q-card--bordered q-pa-sm q-mb-md">
            <div class="q-mb-sm">{{ $t('ArtivactExhibitionsConfigurationEditor.dialog.pages') }}</div>
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
      </template>

      <template v-slot:cancel>
        <q-btn
          :label="$t('Common.cancel')"
          color="primary"
          @click="showExhibitionConfigurationModalRef = false"
        />
      </template>

      <template v-slot:approve>
        <q-btn
          :label="$t('Common.save')"
          color="primary"
          @click="saveOrUpdateExhibitionSummary(selectedExhibitionRef)"
        />
      </template>
    </artivact-dialog>

    <!-- DELETE CONFIRMATION DIALOG -->
    <artivact-dialog :dialog-model="showDeleteExhibitionConfirmModalRef" :warn="true">
      <template v-slot:header>
        {{ $t('ArtivactExhibitionsConfigurationEditor.dialog.deleteExhibition') }}
      </template>

      <template v-slot:body>
        <q-card-section class="full-heights">
          {{ $t('ArtivactExhibitionsConfigurationEditor.dialog.deleteDescription') }}
        </q-card-section>
      </template>

      <template v-slot:cancel>
        <q-btn :label="$t('Common.cancel')" color="primary"
               @click="showDeleteExhibitionConfirmModalRef = false"/>
      </template>

      <template v-slot:approve>
        <q-btn
          :label="$t('ArtivactExhibitionsConfigurationEditor.dialog.deleteLabel')"
          color="primary"
          @click="deleteExhibition"
        />
      </template>
    </artivact-dialog>

    <!-- LONG-RUNNING OPERATION -->
    <artivact-operation-in-progress-dialog
      :progress-monitor-ref="progressMonitorRef" :dialog-model="showOperationInProgressModalRef"/>

    <div class="row">
      <q-space></q-space>
      <q-btn :label="$t('ArtivactExhibitionsConfigurationEditor.add')" @click="createOrEditExhibition(null)" color="primary"/>
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
import ArtivactDialog from 'components/ArtivactDialog.vue';
import ArtivactOperationInProgressDialog from 'components/ArtivactOperationInProgressDialog.vue';
import {useI18n} from 'vue-i18n';

const quasar = useQuasar();
const i18n = useI18n();
const localeStore = useLocaleStore();
const menuStore = useMenuStore();

const exhibitionsRef = ref([] as ExhibitionSummary[]);
const selectedExhibitionRef = ref({} as ExhibitionSummary);

const showExhibitionConfigurationModalRef = ref(false);
const showDeleteExhibitionConfirmModalRef = ref(false);

const menuTreeRef = ref([] as MenuTreeNode[])

const showOperationInProgressModalRef = ref(false);
const progressMonitorRef = ref<OperationProgress>({
  progress: i18n.t('ArtivactExhibitionsConfigurationEditor.dialog.progress'),
  error: ''
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
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.exhibition') }),
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
        message: i18n.t('Common.messages.saving.success', { item: i18n.t('Common.items.exhibition') }),
        icon: 'report',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', { item: i18n.t('Common.items.exhibition') }),
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
        message: i18n.t('Common.messages.deleting.success', { item: i18n.t('Common.items.exhibition') }),
        icon: 'report',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.deleting.failed', { item: i18n.t('Common.items.exhibition') }),
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
