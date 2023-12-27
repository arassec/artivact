<template>
  <template v-if="itemDataRef">
    <q-toolbar class="sticky-toolbar gt-sm">
      <router-link :to="'/item/' + itemDataRef.id">
        <q-btn label="Cancel" color="primary" @click="cancel"/>
      </router-link>
      <q-btn
        label="Delete"
        @click="confirmDeleteRef = true"
        color="primary"
        class="q-ml-sm"
      />
      <q-space/>
      <q-btn label="Save" @click="saveItem" color="primary" :disable="tabRef == 'creation'"/>
    </q-toolbar>

    <q-toolbar class="sticky-toolbar lt-md">
      <router-link :to="'/item/' + itemDataRef.id">
        <q-btn icon="cancel" @click="cancel" flat dense round size="lg"/>
      </router-link>
      <q-btn
        icon="delete"
        @click="confirmDeleteRef = true"
        flat
        dense
        round
        size="lg"
        class="q-ml-sm"
      />
      <q-space/>
      <q-btn icon="save" @click="saveItem" flat dense round size="lg" :disable="tabRef == 'creation'"/>
    </q-toolbar>
  </template>

  <ArtivactContent v-if="itemDataRef && userdataStore.isUserOrAdmin">
    <div class="col-12">
      <div class="col items-center">

        <q-tabs v-model="tabRef" class="q-mb-lg">
          <q-tab name="base" icon="text_snippet" label="Base Data" class="nav-tab"/>
          <q-tab name="media" icon="image" label="Media" class="nav-tab"/>
          <q-tab name="properties" icon="library_books" label="Properties" class="nav-tab"/>
          <q-tab name="creation" icon="auto_awesome" label="Creation" class="nav-tab"
                 v-if="desktopStore.isDesktopModeEnabled"/>
        </q-tabs>

        <!-- BASE DATA -->
        <div v-show="tabRef == 'base'">
          <artivact-restrictions-editor
            :restrictions="itemDataRef.restrictions"
            @delete-restriction="removeRestriction"
            @add-restriction="addRestriction"
            class="q-mb-sm"
          />

          <q-separator
            :class="
            tagsDataRef && tagsDataRef.tags.length > 0 ? 'q-mb-sm' : 'q-mb-lg'
          "
          />

          <div
            class="q-mb-sm row"
            v-if="tagsDataRef"
            v-show="tagsDataRef.tags.length > 0">
            <div class="editor-label">
              <label class="q-mr-xs q-mt-xs vertical-middle">Tags:</label>
            </div>

            <div>
              <q-badge
                class="q-mr-xs vertical-middle"
                color="secondary"
                v-for="(tag, index) in itemDataRef.tags"
                :key="index"
              >{{ tag.translatedValue }}
                <q-btn
                  rounded
                  dense
                  flat
                  color="primary"
                  size="xs"
                  icon="close"
                  @click="removeTag(tag)"
                />
              </q-badge>
              <q-btn
                class="vertical-middle"
                round
                dense
                unelevated
                color="secondary"
                size="xs"
                icon="add"
                @click="addTag"
              />
            </div>
            <q-dialog
              v-model="addTagRef"
              persistent
              transition-show="scale"
              transition-hide="scale"
              class="justify-center"
            >
              <q-card class="dialog-card">
                <q-card-section> Add Tag</q-card-section>
                <q-card-section class="column-lg q-ma-md items-center">
                  <q-select
                    v-model="tagValueRef"
                    autofocus
                    :options="tagsDataRef.tags"
                    option-value="id"
                    option-label="translatedValue"
                    label="Tag"
                  />
                </q-card-section>
                <q-card-actions>
                  <q-btn flat label="Cancel" v-close-popup/>
                  <q-space/>
                  <q-btn
                    flat
                    label="Save"
                    v-close-popup
                    @click="saveSelectedTag"
                  />
                </q-card-actions>
              </q-card>
            </q-dialog>
          </div>
          <q-separator
            v-if="tagsDataRef"
            v-show="tagsDataRef.tags.length > 0"
            class="q-mb-lg"
          />

          <artivact-restricted-translatable-item-editor
            :translatable-string="itemDataRef.title"
            :locales="localeStore.locales"
            label="Title"
            :show-separator="false"
          />
          <artivact-restricted-translatable-item-editor
            :translatable-string="itemDataRef.description"
            :locales="localeStore.locales"
            label="Description"
            :textarea="true"
            :show-separator="false"
          />
        </div>

        <!-- MEDIA -->
        <div v-show="tabRef == 'media'">
          <h2 class="av-text-h2">Images</h2>
          <div>
            <item-image-editor
              :images="itemDataRef.images"
              :models="itemDataRef.models"
              :item-id="itemDataRef.id"
              @uploaded="loadItemMediaData(itemDataRef.id)"
            />
          </div>
          <h2 class="av-text-h2">3D Models</h2>
          <div class="row">
            <q-uploader
              :url="'/api/item/' + itemDataRef.id + '/model'"
              label="Add Models"
              multiple
              class="uploader q-mb-md col-12"
              accept=".glb"
              field-name="file"
              :no-thumbnails="true"
              @uploaded="loadItemMediaData(itemDataRef.id)"
            ></q-uploader>
            <draggable
              :list="itemDataRef.models"
              item-key="fileName"
              group="models"
              class="row"
            >
              <template #item="{ element }">
                <q-card class="model-card q-mr-md">
                  <q-btn
                    icon="delete"
                    class="absolute-top-right"
                    rounded
                    dense
                    color="primary"
                    @click="deleteModel(element)"
                  ></q-btn>
                  <q-card-section class="absolute-center text-h5">
                    {{ element.fileName }}
                  </q-card-section>
                </q-card>
              </template>
            </draggable>
          </div>
        </div>

        <!-- PROPERTIES -->
        <div v-show="tabRef == 'properties'">
          <div v-if="propertiesDataRef && itemDataRef">
            <artivact-property-category-editor
              v-for="(category, index) in propertiesDataRef"
              :category="category"
              :key="index"
              :properties="itemDataRef.properties"
            />
          </div>
          <label v-if="!propertiesDataRef || propertiesDataRef.length === 0">
            There are currently no property definitions for items available.
            Go to the configuration page and add properties.</label>
        </div>

        <!-- MEDIA-CREATION -->
        <div v-if="desktopStore.isDesktopModeEnabled" v-show="tabRef == 'creation'">
          <h2 class="av-text-h2">Images</h2>
          <div class="q-mb-lg">
            <item-image-set-editor :item-id="savedItemId" :creation-image-sets="itemDataRef.creationImageSets"
                                   @delete-image="(imageSet, asset) => deleteImageFromImageSet(imageSet, asset)"
                                   @update-item="loadItemData(itemDataRef.id)"/>
          </div>

          <h2 class="av-text-h2">3D Models</h2>
            <item-model-set-editor  :item-id="savedItemId" :creation-model-sets="itemDataRef.creationModelSets"
                                    @update-item="loadItemData(itemDataRef.id)"/>
        </div>

      </div>
    </div>

    <q-dialog v-model="confirmDeleteRef" persistent>
      <q-card>
        <q-card-section class="row items-center">
          <q-icon
            name="warning"
            size="md"
            color="warning"
            class="q-mr-md"
          ></q-icon>
          <h3 class="av-text-h3">Delete Artivact?</h3>
          <div class="q-ml-sm">
            Are you sure you want to delete this Artivact and all its files?
            This action cannot be undone!
          </div>
        </q-card-section>

        <q-card-section>
          <q-btn flat label="Cancel" color="primary" v-close-popup/>
          <q-btn
            flat
            label="Delete Artivact"
            color="primary"
            v-close-popup
            @click="deleteItem"
            class="float-right"
          />
        </q-card-section>
      </q-card>
    </q-dialog>
  </ArtivactContent>
</template>

<script setup lang="ts">
// noinspection ES6UnusedImports
import draggable from 'vuedraggable';
import {useQuasar} from 'quasar';
import {onMounted, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {api} from 'boot/axios';
import ArtivactContent from 'components/ArtivactContent.vue';
import ArtivactRestrictionsEditor from 'components/ArtivactRestrictionsEditor.vue';
import {useLocaleStore} from 'stores/locale';
import {Asset, ImageSet, Tag} from 'components/models';
import ArtivactPropertyCategoryEditor from 'components/ArtivactPropertyCategoryEditor.vue';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';
import {useBreadcrumbsStore} from 'stores/breadcrumbs';
import {useUserdataStore} from 'stores/userdata';
import {useDesktopStore} from 'stores/desktop';
import ItemImageEditor from 'components/ItemImageEditor.vue';
import ItemImageSetEditor from 'components/ItemImageSetEditor.vue';
import ItemModelSetEditor from 'components/ItemModelSetEditor.vue';

const quasar = useQuasar();
const route = useRoute();
const router = useRouter();

const localeStore = useLocaleStore();
const breadcrumbsStore = useBreadcrumbsStore();
const userdataStore = useUserdataStore();
const desktopStore = useDesktopStore();

const itemDataRef = ref();
const propertiesDataRef = ref();
const tagsDataRef = ref();

const confirmDeleteRef = ref(false);

let tabRef = ref('base');

let addTagRef = ref(false);
const tagValueRef = ref(null);

let savedItemId;

function addTag() {
  tagValueRef.value = null;
  addTagRef.value = true;
}

function loadItemData(itemId: string | string[]) {
  savedItemId = itemId;
  api
    .get('/api/item/' + itemId)
    .then((response) => {
      itemDataRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Loading artivact failed',
        icon: 'report_problem',
      });
    });
}

function saveSelectedTag() {
  itemDataRef.value?.tags.push(tagValueRef.value);
}

function loadItemMediaData(itemId: string | string[]) {
  api
    .get('/api/item/' + itemId)
    .then((response) => {
      itemDataRef.value.images = response.data.images;
      itemDataRef.value.models = response.data.models;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Loading artivact failed',
        icon: 'report_problem',
      });
    });
}

function loadPropertiesData() {
  api
    .get('/api/configuration/public/property')
    .then((response) => {
      propertiesDataRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Loading properties failed',
        icon: 'report_problem',
      });
    });
}

function loadTagsData() {
  api
    .get('/api/configuration/public/tag')
    .then((response) => {
      tagsDataRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Loading tags failed',
        icon: 'report_problem',
      });
    });
}

function removeTag(tag: Tag) {
  itemDataRef.value.tags = (itemDataRef.value?.tags as Tag[]).filter(
    (item) => item !== tag
  );
}

function removeRestriction(role: string) {
  const index = itemDataRef.value.restrictions.indexOf(role);
  if (index > -1) {
    itemDataRef.value.restrictions.splice(index, 1);
  }
}

function addRestriction(role: string) {
  itemDataRef.value.restrictions.push(role);
}

function deleteModel(element: Asset) {
  itemDataRef.value?.models.splice(
    itemDataRef.value?.models.indexOf(element),
    1
  );
}

function deleteImageFromImageSet(imageSet: ImageSet, asset: Asset) {
  itemDataRef.value?.creationImageSets[itemDataRef.value?.creationImageSets.indexOf(imageSet)].images.splice(
    itemDataRef.value?.creationImageSets[itemDataRef.value?.creationImageSets.indexOf(imageSet)].images.indexOf(asset), 1);
}

function saveItem() {
  let item = itemDataRef.value;
  api
    .put('/api/item', item)
    .then(() => {
      breadcrumbsStore.removeLastBreadcrumb();
      router.push('/item/' + item.id);
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: 'Item saved',
        icon: 'done',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Saving failed',
        icon: 'report_problem',
      });
    });
}

function deleteItem() {
  let item = itemDataRef.value;
  api
    .delete('/api/item/' + item.id)
    .then(() => {
      breadcrumbsStore.removeLastBreadcrumb();
      router.push('/');
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: 'Item deleted',
        icon: 'done',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Item deletion failed',
        icon: 'report_problem',
      });
    });
}

function cancel() {
  breadcrumbsStore.removeLastBreadcrumb();
}

onMounted(() => {
  loadPropertiesData();
  loadTagsData();
  loadItemData(route.params.itemId);
});
</script>

<style scoped>

.nav-tab {
  min-width: 8em;
}

.dialog-card {
  min-width: 25em;
}

.model-card {
  width: 200px;
  height: 150px;
}

.sticky-toolbar {
  position: sticky;
  top: 44px;
  background-color: white;
  z-index: 15;
  border-bottom: 1px solid var(--q-primary);
}
</style>
