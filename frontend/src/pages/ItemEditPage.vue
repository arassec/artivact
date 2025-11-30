<template>
  <div v-if="itemDataRef && userdataStore.isUserOrAdmin">
    <div class="col items-center sticky gt-sm">
      <div class="absolute-top-right q-ma-md">
        <q-btn
          data-test="close-button"
          round
          color="primary"
          icon="close"
          class="main-nav-button"
          @click="exitEditMode"
        >
          <q-tooltip>{{ $t('ItemEditPage.button.tooltip.close') }}</q-tooltip>
        </q-btn>
      </div>
    </div>

    <ArtivactContent>
      <div class="col-12">
        <div class="col q-mt-xl lt-md"/>
        <!-- Space on mobile resolution -->

        <div data-test="edit-item-contents" class="col items-center">
          <q-tabs data-test="edit-item-tabs" v-model="tabRef" class="q-mb-lg">
            <q-tab
              data-test="edit-item-base-tab"
              name="base"
              icon="text_snippet"
              :label="$t('ItemEditPage.tab.base')"
              class="nav-tab"
              @click="saveItemIfNecessary(false)"
            />
            <q-tab
              data-test="edit-item-media-tab"
              name="media"
              icon="image"
              :label="$t('ItemEditPage.tab.media')"
              class="nav-tab"
              @click="saveItemIfNecessary(false)"
            />
            <q-tab
              data-test="edit-item-properties-tab"
              name="properties"
              icon="library_books"
              :label="$t('ItemEditPage.tab.properties')"
              class="nav-tab"
              @click="saveItemIfNecessary(false)"
            />
            <q-tab
              data-test="edit-item-creation-tab"
              name="creation"
              icon="3d_rotation"
              :label="$t('ItemEditPage.tab.creation')"
              class="nav-tab"
              @click="saveItemIfNecessary(false)"
              v-if="profilesStore.isDesktopModeEnabled"
            />
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
                tagsDataRef && tagsDataRef.tags.length > 0
                  ? 'q-mb-sm'
                  : 'q-mb-lg'
              "
            />

            <div
              class="q-mb-sm row"
              v-if="tagsDataRef"
              v-show="tagsDataRef.tags.length > 0"
            >
              <div class="editor-label">
                <label class="q-mr-xs q-mt-xs vertical-middle">{{
                    $t('ItemEditPage.label.tags')
                  }}</label>
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
                  >
                    <q-tooltip>{{
                        $t('ItemEditPage.button.tooltip.removeTag')
                      }}
                    </q-tooltip>
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
                  @click="addTag"
                >
                  <q-tooltip>{{
                      $t('ItemEditPage.button.tooltip.addTag')
                    }}
                  </q-tooltip>
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
                  <q-btn
                    color="primary"
                    :label="$t('Common.cancel')"
                    @click="addTagRef = false"
                  />
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
            <artivact-item-image-editor
              :images="itemDataRef.images"
              :item-id="itemDataRef.id"
              @uploaded="loadItemMediaData(itemDataRef.id)"
            />
            <artivact-item-model-editor
              :models="itemDataRef.models"
              :item-id="itemDataRef.id"
              @uploaded="loadItemMediaData(itemDataRef.id)"
            />
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

          <!-- 3D SCAN -->
          <div
            v-if="profilesStore.isDesktopModeEnabled"
            v-show="tabRef == 'creation'"
          >
            <div class="q-mb-xl" v-if="itemDataRef">
              <artivact-item-image-set-editor
                ref="imageSetEditorRef"
                :item-id="savedItemId"
                :creation-image-sets="itemDataRef.creationImageSets"
                @delete-image="saveItemIfNecessary(false)"
                @update-item="updateItemMediaData(itemDataRef.id)"
                @save-item="saveItem(false)"
              />
            </div>
            <artivact-item-model-set-editor
              :item-id="savedItemId"
              :creation-model-sets="itemDataRef.creationModelSets"
              @update-item="updateItemMediaData(itemDataRef.id)"
            />
          </div>
        </div>
      </div>
    </ArtivactContent>
  </div>
</template>

<script setup lang="ts">
import {useQuasar} from 'quasar';
import {computed, onMounted, ref} from 'vue';
import {onBeforeRouteLeave, useRoute, useRouter} from 'vue-router';
import {api} from '../boot/axios';
import ArtivactContent from '../components/ArtivactContent.vue';
import ArtivactRestrictionsEditor from '../components/ArtivactRestrictionsEditor.vue';
import {useLocaleStore} from '../stores/locale';
import {ItemDetails, Tag, TagsConfiguration,} from '../components/artivact-models';
import ArtivactPropertyCategoryEditor from '../components/ArtivactPropertyCategoryEditor.vue';
import ArtivactRestrictedTranslatableItemEditor from '../components/ArtivactRestrictedTranslatableItemEditor.vue';
import {useBreadcrumbsStore} from '../stores/breadcrumbs';
import {useUserdataStore} from '../stores/userdata';
import ArtivactDialog from '../components/ArtivactDialog.vue';
import {useI18n} from 'vue-i18n';
import ArtivactItemModelEditor from '../components/ArtivactItemModelEditor.vue';
import ArtivactItemImageEditor from '../components/ArtivactItemImageEditor.vue';
import ArtivactItemImageSetEditor from '../components/ArtivactItemImageSetEditor.vue';
import ArtivactItemModelSetEditor from '../components/ArtivactItemModelSetEditor.vue';
import {useProfilesStore} from '../stores/profiles';
import {useWizzardStore} from '../stores/wizzard';
import {usePeripheralsConfigStore} from '../stores/peripherals';

const quasar = useQuasar();
const route = useRoute();
const router = useRouter();
const i18n = useI18n();

const localeStore = useLocaleStore();
const breadcrumbsStore = useBreadcrumbsStore();
const userdataStore = useUserdataStore();
const profilesStore = useProfilesStore();
const wizzardStore = useWizzardStore();
const peripheralsConfigStore = usePeripheralsConfigStore();

const itemDataRef = ref<ItemDetails>();
const propertiesDataRef = ref();
const tagsDataRef = ref<TagsConfiguration>();

let tabRef = ref('base');

let addTagRef = ref(false);
const tagValueRef = ref(null);

let savedItemId: string;

const imageSetEditorRef = ref<InstanceType<
  typeof ArtivactItemImageSetEditor
> | null>(null);

let originalItemJson: string;

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
  });
});

function addTag() {
  tagValueRef.value = null;
  addTagRef.value = true;
}

function loadItemData(itemId: string | string[]) {
  if (typeof itemId === 'string') {
    savedItemId = itemId;
  } else {
    savedItemId = itemId[0];
  }
  api
    .get('/api/item/' + itemId)
    .then((response) => {
      originalItemJson = JSON.stringify(response.data);
      itemDataRef.value = response.data;
      if (wizzardStore.scanItem) {
        wizzardStore.setScanItem(false);
        tabRef.value = 'creation';
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', {
          item: i18n.t('Common.items.item'),
        }),
        icon: 'report_problem',
      });
    });
}

function updateItemMediaData(itemId: string) {
  api
    .get('/api/item/' + itemId)
    .then((response) => {
      originalItemJson = JSON.stringify(response.data);
      itemDataRef.value.images.splice(0, itemDataRef.value.images.length, ...response.data.images);
      itemDataRef.value.models.splice(0, itemDataRef.value.models.length, ...response.data.models);
      itemDataRef.value.creationImageSets.splice(0, itemDataRef.value.creationImageSets.length, ...response.data.creationImageSets);
      itemDataRef.value.creationModelSets.splice(0, itemDataRef.value.creationModelSets.length, ...response.data.creationModelSets);
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', {
          item: i18n.t('Common.items.item'),
        }),
        icon: 'report_problem',
      });
    });
}

function saveSelectedTag() {
  if (tagValueRef.value) {
    itemDataRef.value?.tags.push(tagValueRef.value);
    addTagRef.value = false;
  }
}

function loadItemMediaData(itemId: string | string[]) {
  api
    .get('/api/item/' + itemId)
    .then((response) => {
      if (itemDataRef.value) {
        itemDataRef.value.images = response.data.images;
        itemDataRef.value.models = response.data.models;
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', {
          item: i18n.t('Common.items.item'),
        }),
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
        message: i18n.t('Common.messages.loading.failed', {
          item: i18n.t('Common.items.properties'),
        }),
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
        message: i18n.t('Common.messages.loading.failed', {
          item: i18n.t('Common.items.tags'),
        }),
        icon: 'report_problem',
      });
    });
}

function loadPeripheralConfiguration() {
  if (profilesStore.isDesktopModeEnabled || profilesStore.isE2eModeEnabled) {
    api
      .get('/api/configuration/peripheral')
      .then((response) => {
        peripheralsConfigStore.setPeripheralsConfig(response.data);
      })
      .catch(() => {
        quasar.notify({
          color: 'negative',
          position: 'bottom',
          message: i18n.t('Common.messages.loading.failed', {
            item: i18n.t('Common.items.configuration.peripherals'),
          }),
          icon: 'report_problem',
        });
      });
  }
}

function removeTag(tag: Tag) {
  if (itemDataRef.value) {
    itemDataRef.value.tags = (itemDataRef.value?.tags as Tag[]).filter(
      (item) => item !== tag,
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

function saveItemIfNecessary(exitEditMode: boolean) {
  let currentPageContentJson = JSON.stringify(itemDataRef.value);
  if (currentPageContentJson !== originalItemJson) {
    saveItem(exitEditMode);
  } else if (exitEditMode) {
    router.push('/item/' + itemDataRef.value.id);
  }
}

function saveItem(exitEditMode: boolean) {
  let item = itemDataRef.value;
  api
    .put('/api/item', item)
    .then(() => {
      originalItemJson = JSON.stringify(itemDataRef.value);
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.success', {
          item: i18n.t('Common.items.item'),
        }),
        icon: 'done',
      });
      if (exitEditMode) {
        router.push('/item/' + item.id);
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', {
          item: i18n.t('Common.items.item'),
        }),
        icon: 'report_problem',
      });
    });
}

function exitEditMode() {
  breadcrumbsStore.removeLastBreadcrumb();
  saveItemIfNecessary(true);
}

onBeforeRouteLeave((to, from, next) => {
  saveItemIfNecessary(false);
  next();
});

onMounted(() => {
  loadPeripheralConfiguration();
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

.sticky {
  position: sticky;
  top: 3.5em;
  z-index: 2;
}
</style>
