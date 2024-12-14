<template>
  <div v-if="collectionExportsRef">

    <q-list bordered class="rounded-borders q-mb-lg">
      <draggable
        :list="collectionExportsRef"
        item-key="id"
        group="collectionExports"
        handle=".collection-export-move-icon"
        @dragend="$emit('save-sort-order')"
      >
        <template #item="{ element }">
          <q-expansion-item
            data-test="collection-export-expansion-item"
            :label="element.title.translatedValue"
            group="collectionExports"
            header-class="bg-primary text-white"
            class="collection-export"
            expand-separator
            expand-icon-class="text-white"
          >
            <template v-slot:header>
              <q-item-section avatar>
                <div class="text-white q-gutter-md">
                  <q-icon
                    name="drag_indicator"
                    class="collection-export-move-icon"
                    size="md"
                  />
                </div>
              </q-item-section>

              <q-item-section class="collection-export-label">
                <div>
                  {{ translate(element.title) }}
                  <q-icon v-if="element.restrictions.length > 0"
                          class="q-ml-sm"
                          name="lock"
                          size="xs">
                    <q-tooltip>{{ $t('ArtivactCollectionExportEditor.tooltip.restricted') }}</q-tooltip>
                  </q-icon>
                  <q-icon v-if="element.distributionOnly"
                          class="q-ml-sm"
                          name="public"
                          size="xs">
                    <q-tooltip>{{ $t('ArtivactCollectionExportEditor.tooltip.distributionOnly') }}</q-tooltip>
                  </q-icon>
                </div>
              </q-item-section>

              <q-item-section side>
                <q-form :action="'/api/collection/export/' + element.id + '/export-file'" method="get">
                  <q-btn
                    v-if="element.filePresent"
                    round
                    dense
                    type="submit"
                    flat
                    color="white"
                    icon="download"
                    size="md"
                    @click.stop="$emit('download-collection-export-file', element)">
                    <q-tooltip>{{ $t('ArtivactCollectionExportEditor.tooltip.download') }}</q-tooltip>
                  </q-btn>
                  <q-btn
                    :disable="element.distributionOnly"
                    round
                    dense
                    flat
                    color="white"
                    :icon="element.filePresent ? 'restore_page' : 'note_add'"
                    size="md"
                    @click.stop="$emit('build-collection-export-file', element)">
                    <q-tooltip>
                      {{ element.filePresent ? $t('ArtivactCollectionExportEditor.tooltip.build') : $t('ArtivactCollectionExportEditor.tooltip.buildNew')
                      }}
                    </q-tooltip>
                  </q-btn>
                  <q-btn
                    round
                    dense
                    flat
                    color="white"
                    icon="edit"
                    size="md"
                    @click.stop="editExistingExportConfiguration(element)">
                    <q-tooltip>{{ $t('ArtivactCollectionExportEditor.tooltip.edit') }}</q-tooltip>
                  </q-btn>
                  <q-btn
                    round
                    dense
                    flat
                    color="white"
                    icon="delete"
                    size="md"
                    @click.stop="deleteCollectionExport(element)">
                    <q-tooltip>{{ $t('ArtivactCollectionExportEditor.tooltip.delete') }}</q-tooltip>
                  </q-btn>
                </q-form>
              </q-item-section>
            </template>

            <q-card>
              <q-card-section>
                <div class="row">
                  <div class="col">
                    <p v-if="!element.filePresent" class="bg-warning text-white q-pa-sm q-mb-sm">
                      {{ $t('ArtivactCollectionExportEditor.label.exportFileMissing') }}
                    </p>
                    <h3 class="av-label-h2">
                      {{ element.description.translatedValue }}
                    </h3>
                    <p v-if="element.filePresent">
                      {{
                        $t('ArtivactCollectionExportEditor.label.lastModified') + new Date(element.fileLastModified)
                      }}
                    </p>
                    <div>
                      <q-uploader v-if="!element.distributionOnly"
                                  :label="$t('ArtivactCollectionExportEditor.label.coverPicture')"
                                  field-name="file"
                                  :multiple="false"
                                  class="col"
                                  :url="'/api/collection/export/' + element.id + '/cover-picture'"
                                  @finish="element.coverPictureExtension = null; $emit('cover-picture-uploaded')"
                      />
                    </div>
                  </div>
                  <div class="col q-pa-lg">
                    <div v-if="element.coverPictureExtension">
                      <q-img
                        :src="'/api/collection/export/' + element.id + '/cover-picture/' + Math.random()">
                        <q-btn
                          class="absolute-top-right q-ma-sm all-pointer-events"
                          dense
                          round
                          color="primary"
                          text-color="white"
                          icon="delete"
                          size="md"
                          @click.stop="$emit('delete-cover-picture', element)">
                          <q-tooltip>{{ $t('ArtivactCollectionExportEditor.tooltip.deleteCoverPicture') }}</q-tooltip>
                        </q-btn>
                        <div class="absolute-bottom av-text-h2 text-center">
                          {{ element.title.translatedValue }}
                        </div>
                      </q-img>
                    </div>
                    <p v-else>
                      {{ $t('ArtivactCollectionExportEditor.label.noCoverPicture') }}
                    </p>
                  </div>
                </div>
              </q-card-section>
            </q-card>

          </q-expansion-item>
        </template>
      </draggable>
    </q-list>

    <div class="row">
      <q-space></q-space>
      <q-btn :label="$t('ArtivactCollectionExportEditor.button.create')" @click="configureNewExportConfiguration()"
             color="primary" />
    </div>

    <!-- CREATE / EDIT EXPORT CONFIGURATION DIALOG -->
    <artivact-dialog :dialog-model="showCollectionExportConfigurationDialogRef">
      <template v-slot:header>
        {{ createNewExportConfigurationRef ? $t('ArtivactCollectionExportEditor.dialog.create.heading') : $t('ArtivactCollectionExportEditor.dialog.edit.heading')
        }}
      </template>

      <template v-slot:body>
        <q-card-section>
          <artivact-restrictions-editor :restrictions="collectionExportRef.restrictions"
                                        :in-details-view="false"
                                        class="q-mb-md"
                                        @delete-restriction="removeRestriction"
                                        @add-restriction="addRestriction"
          />
          <q-select
            :disable="collectionExportRef.distributionOnly"
            class="q-mb-md"
            outlined
            emit-value
            v-model="collectionExportRef.sourceId"
            :options="availableSourceIdOptionsRef"
            :label="$t('ArtivactCollectionExportEditor.label.sourceId')"
          />
          <q-separator class="q-mb-lg" />
          <artivact-restricted-translatable-item-editor :label="$t('ArtivactCollectionExportEditor.label.title')"
                                                        :translatable-string="collectionExportRef.title"
                                                        :data-test="'create-collection-export-title'"
                                                        :show-separator="false"
                                                        :disable="collectionExportRef.distributionOnly"
          />
          <artivact-restricted-translatable-item-editor :label="$t('ArtivactCollectionExportEditor.label.description')"
                                                        :translatable-string="collectionExportRef.description"
                                                        :data-test="'create-collection-export-description'"
                                                        :show-separator="false"
                                                        :textarea="true"
                                                        :disable="collectionExportRef.distributionOnly"
          />
          <div>
            <q-checkbox
              :disable="collectionExportRef.distributionOnly"
              v-model="collectionExportRef.exportConfiguration.optimizeSize"
              name="optimizeSize"
              :label="$t('ArtivactCollectionExportEditor.label.optimizeSize')"
            />
          </div>
          <div>
            <q-checkbox
              :disable="collectionExportRef.distributionOnly"
              v-model="collectionExportRef.exportConfiguration.applyRestrictions"
              name="applyRestrictions"
              :label="$t('ArtivactCollectionExportEditor.label.applyRestrictions')"
            />
          </div>
        </q-card-section>
      </template>

      <template v-slot:cancel>
        <q-btn color="primary" :label="$t('Common.cancel')"
               @click="showCollectionExportConfigurationDialogRef = false" />
      </template>

      <template v-slot:approve>
        <q-btn color="primary"
               :label="createNewExportConfigurationRef ? $t('ArtivactCollectionExportEditor.dialog.create.approve') : $t('ArtivactCollectionExportEditor.dialog.edit.approve')"
               @click="saveExportConfiguration()" />
      </template>
    </artivact-dialog>

  </div>

</template>

<script setup lang="ts">
import draggable from 'vuedraggable';
import { PropType, Ref, ref, toRef } from 'vue';
import { CollectionExport, ContentSource, SelectboxModel, TranslatableString } from 'components/artivact-models';
import ArtivactDialog from 'components/ArtivactDialog.vue';
import ArtivactRestrictionsEditor from 'components/ArtivactRestrictionsEditor.vue';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';
import { useMenuStore } from 'stores/menu';
import { translate } from 'components/artivact-utils';
import { QUploader } from 'quasar';

const props = defineProps({
  collectionExports: {
    required: true,
    type: Array as PropType<Array<CollectionExport>>
  }
});

const emit = defineEmits<{
  (e: 'save-collection-export', collectionExport: CollectionExport): void;
  (e: 'delete-collection-export', collectionExport: CollectionExport): void;
  (e: 'save-sort-order'): void;
  (e: 'build-collection-export-file', collectionExport: CollectionExport): void;
  (e: 'download-collection-export-file', collectionExport: CollectionExport): void;
  (e: 'cover-picture-uploaded'): void;
  (e: 'delete-cover-picture', collectionExport: CollectionExport): void;
}>();

const menuStore = useMenuStore();

const collectionExportsRef = toRef(props.collectionExports);

const availableSourceIdOptionsRef: Ref<SelectboxModel[]> = ref([] as SelectboxModel[]);

const collectionExportRef: Ref<CollectionExport | null> = ref(null);
const showCollectionExportConfigurationDialogRef = ref(false);
const createNewExportConfigurationRef = ref(false);

function configureNewExportConfiguration() {
  collectionExportRef.value = {
    id: '',
    restrictions: [],
    title: {
      value: ''
    } as TranslatableString,
    description: {
      value: ''
    } as TranslatableString,
    contentSource: ContentSource.MENU,
    sourceId: '',
    exportConfiguration: {
      applyRestrictions: false,
      excludeItems: false,
      optimizeSize: false
    },
    fileLastModified: 0,
    filePresent: false,
    fileSize: 0,
    coverPictureExtension: null,
    distributionOnly: false
  } as CollectionExport;
  createSourceIdOptions();
  createNewExportConfigurationRef.value = true;
  showCollectionExportConfigurationDialogRef.value = true;
}

function editExistingExportConfiguration(collectionExport: CollectionExport) {
  collectionExportRef.value = collectionExport;
  createSourceIdOptions();
  createNewExportConfigurationRef.value = false;
  showCollectionExportConfigurationDialogRef.value = true;
}

function createSourceIdOptions() {
  availableSourceIdOptionsRef.value = [] as SelectboxModel[];
  menuStore.menus.forEach((menu) => {
    availableSourceIdOptionsRef.value.push({
      label: menu.translatedValue,
      value: menu.id,
      disable: false
    });
    if (menu.menuEntries.length > 0) {
      menu.menuEntries.forEach((menuEntry) => {
        availableSourceIdOptionsRef.value.push({
          label: menuEntry.translatedValue,
          value: menuEntry.id,
          disable: false
        });
      });
    }
  });
}

function saveExportConfiguration() {
  showCollectionExportConfigurationDialogRef.value = false;
  emit('save-collection-export', collectionExportRef.value as CollectionExport);
}

function deleteCollectionExport(collectionExport: CollectionExport) {
  emit('delete-collection-export', collectionExport);
}

function removeRestriction(role: string) {
  if (collectionExportRef.value) {
    const index = collectionExportRef.value.restrictions.indexOf(role);
    if (index > -1) {
      collectionExportRef.value?.restrictions.splice(index, 1);
    }
  }
}

function addRestriction(role: string) {
  collectionExportRef.value?.restrictions.push(role);
}

</script>

<style scoped>
.collection-export {
  border-bottom: 1px solid white;
}

.collection-export-label {
  font-size: large;
}
</style>
