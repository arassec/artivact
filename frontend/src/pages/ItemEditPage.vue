<template>
  <ArtivactContent v-if="itemDataRef && userdataStore.isUserOrAdmin">
    <div class="col-12">

      <div class="col items-center">
        <div class="absolute-top-left q-ma-md">
          <router-link :to="'/item/' + itemDataRef.id">
            <q-btn
              round
              color="primary"
              icon="close"
              class="main-nav-button"
              @click="cancel">
              <q-tooltip>{{$t('ItemEditPage.button.tooltip.close')}}</q-tooltip>
            </q-btn>
          </router-link>
        </div>

        <div class="absolute-top-right q-ma-md">
          <q-form :action="'/api/exchange/item/' + itemDataRef.id + '/export'" method="get">
            <q-btn
              round
              color="primary"
              icon="download"
              type="submit"
              class="q-mr-sm main-nav-button">
            <q-tooltip>{{$t('ItemEditPage.button.tooltip.download')}}</q-tooltip>
            </q-btn>
            <q-btn :disable="tabRef == 'creation'"
                   round
                   color="primary"
                   icon="save"
                   class="main-nav-button"
                   @click="saveItem">
              <q-tooltip>{{ $t('ItemEditPage.button.tooltip.save')}}</q-tooltip>
            </q-btn>
          </q-form>
        </div>

      </div>

      <div class="col q-mt-xl lt-md"/> <!-- Space on mobile resolution -->

      <div class="col items-center">

        <q-tabs v-model="tabRef" class="q-mb-lg">
          <q-tab name="base" icon="text_snippet" :label="$t('ItemEditPage.tab.base')" class="nav-tab"/>
          <q-tab name="media" icon="image" :label="$t('ItemEditPage.tab.media')" class="nav-tab"/>
          <q-tab name="properties" icon="library_books" :label="$t('ItemEditPage.tab.properties')" class="nav-tab"/>
          <q-tab name="creation" icon="auto_awesome" :label="$t('ItemEditPage.tab.creation')" class="nav-tab"
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

          <q-separator :class="tagsDataRef && tagsDataRef.tags.length > 0 ? 'q-mb-sm' : 'q-mb-lg'"/>

          <div
            class="q-mb-sm row"
            v-if="tagsDataRef"
            v-show="tagsDataRef.tags.length > 0">
            <div class="editor-label">
              <label class="q-mr-xs q-mt-xs vertical-middle">{{$t('ItemEditPage.label.tags')}}</label>
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
                  @click="removeTag(tag)">
                  <q-tooltip>{{$t('ItemEditPage.button.tooltip.removeTag')}}</q-tooltip>
                </q-btn>
              </q-badge>
              <q-btn
                v-if="itemDataRef.tags.length < tagsDataRef.tags.length"
                class="vertical-middle"
                round
                dense
                unelevated
                color="secondary"
                size="xs"
                icon="add"
                @click="addTag">
                <q-tooltip>{{ $t('ItemEditPage.button.tooltip.addTag')}}</q-tooltip>
              </q-btn>
            </div>

            <artivact-dialog :dialog-model="addTagRef">
              <template v-slot:header>
                {{ $t('ItemEditPage.dialog.addTag.heading') }}
              </template>

              <template v-slot:body>
                <q-card-section>
                  <q-select
                    v-model="tagValueRef"
                    autofocus
                    :options="availableTags"
                    option-value="id"
                    option-label="translatedValue"
                    :label="$t('Common.items.tag')"
                  />
                </q-card-section>
              </template>

              <template v-slot:cancel>
                <q-btn color="primary" :label="$t('Common.cancel')" @click="addTagRef = false"/>
              </template>

              <template v-slot:approve>
                <q-btn
                  color="primary"
                  :label="$t('Common.save')"
                  @click="saveSelectedTag"
                />
              </template>
            </artivact-dialog>
          </div>

          <q-separator
            v-if="tagsDataRef"
            v-show="tagsDataRef.tags.length > 0"
            class="q-mb-lg"
          />

          <artivact-restricted-translatable-item-editor
            :translatable-string="itemDataRef.title"
            :locales="localeStore.locales"
            :label="$t('ItemEditPage.editor.title')"
            :show-separator="false"
          />
          <artivact-restricted-translatable-item-editor
            :translatable-string="itemDataRef.description"
            :locales="localeStore.locales"
            :label="$t('ItemEditPage.editor.description')"
            :textarea="true"
            :show-separator="false"
          />
        </div>

        <!-- MEDIA -->
        <div v-show="tabRef == 'media'">
          <h2 class="av-text-h2">{{ $t('ItemEditPage.label.images') }}</h2>
          <div>
            <item-image-editor
              :images="itemDataRef.images"
              :item-id="itemDataRef.id"
              @uploaded="loadItemMediaData(itemDataRef.id)"
            />
          </div>
          <h2 class="av-text-h2">{{ $t('ItemEditPage.label.models') }}</h2>
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
                    class="absolute-top-right q-ma-sm"
                    rounded
                    dense
                    color="primary"
                    @click="deleteModel(element)">
                    <q-tooltip>{{ $t('ItemEditPage.button.tooltip.deleteModel') }}</q-tooltip>
                  </q-btn>
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
            {{ $t('ItemEditPage.label.noProperties') }}
          </label>
        </div>

        <!-- MEDIA-CREATION -->
        <div v-if="desktopStore.isDesktopModeEnabled" v-show="tabRef == 'creation'">
          <h2 class="av-text-h2">{{ $t('ItemEditPage.label.images') }}</h2>
          <div class="q-mb-lg">
            <item-image-set-editor :item-id="savedItemId" :creation-image-sets="itemDataRef.creationImageSets"
                                   @delete-image="(imageSet, asset) => deleteImageFromImageSet(imageSet, asset)"
                                   @update-item="loadItemData(itemDataRef.id)"/>
          </div>

          <h2 class="av-text-h2">{{ $t('ItemEditPage.label.models') }}</h2>
          <item-model-set-editor :item-id="savedItemId" :creation-model-sets="itemDataRef.creationModelSets"
                                 @update-item="loadItemData(itemDataRef.id)"/>
        </div>

      </div>
    </div>

  </ArtivactContent>
</template>

<script setup lang="ts">
// noinspection ES6UnusedImports
import draggable from 'vuedraggable';
import {useQuasar} from 'quasar';
import {computed, onMounted, ref} from 'vue';
import {useRoute} from 'vue-router';
import {api} from 'boot/axios';
import ArtivactContent from 'components/ArtivactContent.vue';
import ArtivactRestrictionsEditor from 'components/ArtivactRestrictionsEditor.vue';
import {useLocaleStore} from 'stores/locale';
import {Asset, ImageSet, ItemDetails, Tag, TagsConfiguration} from 'components/models';
import ArtivactPropertyCategoryEditor from 'components/ArtivactPropertyCategoryEditor.vue';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';
import {useBreadcrumbsStore} from 'stores/breadcrumbs';
import {useUserdataStore} from 'stores/userdata';
import {useDesktopStore} from 'stores/desktop';
import ItemImageEditor from 'components/ItemImageEditor.vue';
import ItemImageSetEditor from 'components/ItemImageSetEditor.vue';
import ItemModelSetEditor from 'components/ItemModelSetEditor.vue';
import ArtivactDialog from 'components/ArtivactDialog.vue';
import {useI18n} from 'vue-i18n';

const quasar = useQuasar();
const route = useRoute();
const i18n = useI18n();

const localeStore = useLocaleStore();
const breadcrumbsStore = useBreadcrumbsStore();
const userdataStore = useUserdataStore();
const desktopStore = useDesktopStore();

const itemDataRef = ref<ItemDetails>();
const propertiesDataRef = ref();
const tagsDataRef = ref<TagsConfiguration>();

let tabRef = ref('base');

let addTagRef = ref(false);
const tagValueRef = ref(null);

let savedItemId;

const availableTags = computed(() => {
  return tagsDataRef.value?.tags.filter((tag: Tag) => {
    if (itemDataRef.value) {
      for (let i = 0; i < itemDataRef.value.tags.length; i++) {
        let usedTag = itemDataRef.value.tags[i];
        if (tag.id == usedTag.id) {
          return false;
        }
      }
    }
    return true;
  })
})

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
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.item') }),
        icon: 'report_problem',
      });
    });
}

function saveSelectedTag() {
  itemDataRef.value?.tags.push(tagValueRef.value);
  addTagRef.value = false;
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
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.item') }),
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
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.properties') }),
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
        message: i18n.t('Common.messages.loading.failed', { item: i18n.t('Common.items.tags') }),
        icon: 'report_problem',
      });
    });
}

function removeTag(tag: Tag) {
  if (itemDataRef.value) {
    itemDataRef.value.tags = (itemDataRef.value?.tags as Tag[]).filter(
      (item) => item !== tag
    );
  }
}

function removeRestriction(role: string) {
  if (itemDataRef.value) {
    const index = itemDataRef.value.restrictions.indexOf(role);
    if (index > -1) {
      itemDataRef.value?.restrictions.splice(index, 1);
    }
  }
}

function addRestriction(role: string) {
  itemDataRef.value?.restrictions.push(role);
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
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.success', { item: i18n.t('Common.items.item') }),
        icon: 'done',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', { item: i18n.t('Common.items.item') }),
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
.main-nav-button {
  z-index: 2;
}

.nav-tab {
  min-width: 8em;
}

.model-card {
  width: 200px;
  height: 150px;
}

</style>
