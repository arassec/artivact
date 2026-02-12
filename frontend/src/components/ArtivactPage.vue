<template>
  <div v-if="pageContentRef && pageId" :class="inEditModeRef ? 'page' : ''">
    <div
      class="col items-center sticky gt-sm"
      v-if="userdataStore.isUserOrAdmin && pageId !== 'INDEX' && !inEditModeRef"
    >
      <div class="absolute-top-right q-ma-md">
        <q-btn
          id="sticky-item"
          :disable="!pageContentRef.editable"
          data-test="edit-page-button"
          round
          color="primary"
          icon="edit"
          class="edit-page-button float-right"
          @click="$emit('enter-edit-mode')"
        >
          <q-tooltip>{{ $t('ArtivactPage.tooltip.edit') }}</q-tooltip>
        </q-btn>
      </div>
    </div>

    <div class="col items-center sticky gt-sm" v-if="inEditModeRef">
      <div class="absolute-top-right q-ma-md">
        <q-btn
          v-if="
            !profilesStore.isDesktopModeEnabled ||
            profilesStore.isE2eModeEnabled
          "
          data-test="reset-wip-button"
          round
          color="primary"
          icon="undo"
          class="q-mr-sm main-nav-button"
          @click="$emit('reset-wip')"
        >
          <q-tooltip>{{ $t('ArtivactPage.tooltip.resetWip') }}</q-tooltip>
        </q-btn>
        <q-btn
          v-if="
            !profilesStore.isDesktopModeEnabled ||
            profilesStore.isE2eModeEnabled
          "
          data-test="publish-wip-button"
          round
          color="primary"
          icon="publish"
          class="q-mr-sm main-nav-button"
          @click="$emit('publish-wip')"
        >
          <q-tooltip>{{ $t('ArtivactPage.tooltip.publishWip') }}</q-tooltip>
        </q-btn>
        <q-btn
          v-if="
            !profilesStore.isDesktopModeEnabled ||
            profilesStore.isE2eModeEnabled
          "
          data-test="edit-metadata-button"
          round
          color="primary"
          icon="tag"
          class="q-mr-sm main-nav-button"
          @click="showEditMetadataDialogRef = true"
        >
          <q-tooltip>{{ $t('ArtivactPage.tooltip.editMetadata') }}</q-tooltip>
        </q-btn>
        <q-btn
          data-test="close-button"
          round
          color="primary"
          icon="close"
          class="main-nav-button"
          @click="$emit('exit-edit-mode')"
        >
          <q-tooltip>{{ $t('Common.close') }}</q-tooltip>
        </q-btn>
      </div>
    </div>

    <div
      class="full-width row justify-center"
      v-if="inEditModeRef && pageContentRef.widgets.length == 0"
    >
      <q-btn
        class="q-mt-md"
        icon="add"
        dense
        round
        @click="showAddWidgetDialogRef = true"
      ></q-btn>
    </div>

    <nav
      v-if="showSideNavigation"
      class="side-navigation gt-sm"
      aria-label="Page navigation"
      :style="sideNavigationStyle"
    >
      <ul class="side-navigation-list">
        <li
          v-for="item in navigationItems"
          :key="item.id"
          class="side-navigation-item"
        >
          <a
            :href="'#' + NAV_ANCHOR_PREFIX + item.id"
            @click.prevent="scrollToWidget(item.id)"
          >
            {{ item.label }}
          </a>
        </li>
      </ul>
    </nav>

    <Draggable
      v-model="pageContentRef.widgets"
      item-key="id"
      group="widgets"
      handle=".move-widget-button"
      :disabled="!inEditModeRef"
      @dragend="$emit('update-page-content')"
    >
      <template #item="{ element, index }">
        <div class="bg-accent widget-anchor" :id="NAV_ANCHOR_PREFIX + element.id">
          <artivact-page-title-widget
            v-if="element.type === 'PAGE_TITLE'"
            group="widgets"
            :id="element.id"
            :class="inEditModeRef ? 'widget' : ''"
            :widget-data="element as PageTitleWidgetData"
            :in-edit-mode="inEditModeRef"
            @save-widget-before-upload="saveWidgetBeforeUpload"
            @add-widget-below="
              addWidgetBelowRef = element.id;
              showAddWidgetDialogRef = true;
            "
            @delete-widget="() => deleteWidget(index)"
            :page-id="pageId"
            v-on:image-added="fileAdded($event, element.id)"
            @stop-editing="$emit('update-page-content')"
          />

          <artivact-text-widget
            v-else-if="element.type === 'TEXT'"
            group="widgets"
            :id="element.id"
            :class="inEditModeRef ? 'widget' : ''"
            :widget-data="element as TextWidgetData"
            :in-edit-mode="inEditModeRef"
            @add-widget-below="
              addWidgetBelowRef = element.id;
              showAddWidgetDialogRef = true;
            "
            @delete-widget="() => deleteWidget(index)"
            @stop-editing="$emit('update-page-content')"
          />

          <artivact-item-search-widget
            v-else-if="element.type === 'ITEM_SEARCH'"
            group="widgets"
            :id="element.id"
            :class="inEditModeRef ? 'widget' : ''"
            :widget-data="element as ItemSearchWidget"
            :in-edit-mode="inEditModeRef"
            @add-widget-below="
              addWidgetBelowRef = element.id;
              showAddWidgetDialogRef = true;
            "
            @delete-widget="() => deleteWidget(index)"
            @stop-editing="$emit('update-page-content')"
          />

          <artivact-info-box-widget
            v-else-if="element.type === 'INFO_BOX'"
            group="widgets"
            :id="element.id"
            :class="inEditModeRef ? 'widget' : ''"
            :widget-data="element as InfoBoxWidgetData"
            :in-edit-mode="inEditModeRef"
            @add-widget-below="
              addWidgetBelowRef = element.id;
              showAddWidgetDialogRef = true;
            "
            @delete-widget="() => deleteWidget(index)"
            @stop-editing="$emit('update-page-content')"
          />

          <artivact-avatar-widget
            v-else-if="element.type === 'AVATAR'"
            group="widgets"
            :id="element.id"
            :class="inEditModeRef ? 'widget' : ''"
            :widget-data="element as AvatarWidgetData"
            :in-edit-mode="inEditModeRef"
            @save-widget-before-upload="saveWidgetBeforeUpload"
            @add-widget-below="
              addWidgetBelowRef = element.id;
              showAddWidgetDialogRef = true;
            "
            @delete-widget="() => deleteWidget(index)"
            :page-id="pageId"
            v-on:image-added="fileAdded($event, element.id)"
            @stop-editing="$emit('update-page-content')"
          />

          <artivact-image-gallery-widget
            v-else-if="element.type === 'IMAGE_GALLERY'"
            group="widgets"
            :id="element.id"
            :class="inEditModeRef ? 'widget' : ''"
            :widget-data="element as ImageGalleryWidgetData"
            :page-id="pageId"
            :in-edit-mode="inEditModeRef"
            @save-widget-before-upload="saveWidgetBeforeUpload"
            @add-widget-below="
              addWidgetBelowRef = element.id;
              showAddWidgetDialogRef = true;
            "
            @delete-widget="() => deleteWidget(index)"

            @image-added="fileAdded($event, element.id)"
            @image-deleted="fileDeleted($event, element.id)"
            @stop-editing="$emit('update-page-content')"
          />

          <artivact-buttons-widget
            v-else-if="element.type === 'BUTTONS'"
            group="widgets"
            :id="element.id"
            :class="inEditModeRef ? 'widget' : ''"
            :widget-data="element as ButtonsWidgetData"
            :in-edit-mode="inEditModeRef"
            @add-widget-below="
              addWidgetBelowRef = element.id;
              showAddWidgetDialogRef = true;
            "
            @delete-widget="() => deleteWidget(index)"
            @stop-editing="$emit('update-page-content')"
          />
        </div>
      </template>
    </Draggable>

    <artivact-dialog
      :data-test="'add-widget-modal'"
      :dialog-model="showAddWidgetDialogRef"
      v-if="userdataStore.isUserOrAdmin && inEditModeRef"
    >
      <template v-slot:header>
        {{ $t('ArtivactPage.dialog.addWidget.heading') }}
      </template>

      <template v-slot:body>
        <q-card-section>
          <div class="q-mb-lg">
            {{ $t('ArtivactPage.dialog.addWidget.description') }}
          </div>
          <q-select
            data-test="add-widget-selection"
            outlined
            v-model="selectedWidgetTypeRef"
            :options="availableWidgetTypes"
            :option-label="(option) => $t(option)"
            :label="$t('ArtivactPage.dialog.addWidget.type')"
          >
            <template v-slot:option="scope">
              <q-item
                :data-test="'add-widget-selection-' + scope.opt"
                v-bind="scope.itemProps"
              >
                <q-item-section>
                  <q-item-label>{{ $t(scope.opt) }}</q-item-label>
                </q-item-section>
              </q-item>
            </template>
          </q-select>
          <div class="q-mt-md" style="overflow-wrap: break-word">
            {{ $t(selectedWidgetTypeRef + '_DESCRIPTION') }}
          </div>
        </q-card-section>
      </template>

      <template v-slot:cancel>
        <q-btn
          :label="$t('Common.cancel')"
          color="primary"
          @click="showAddWidgetDialogRef = false"
        />
      </template>

      <template v-slot:approve>
        <q-btn
          data-test="add-widget-modal-approve"
          :label="$t('ArtivactPage.label.addWidget')"
          color="primary"
          @click="addWidget()"
        />
      </template>
    </artivact-dialog>

    <artivact-dialog :dialog-model="showDeleteWidgetDialogRef" :warn="true">
      <template v-slot:header>
        {{ $t('ArtivactPage.dialog.deleteWidget.heading') }}
      </template>

      <template v-slot:body>
        <q-card-section>
          <div class="q-mb-lg">
            {{ $t('ArtivactPage.dialog.deleteWidget.description') }}
          </div>
        </q-card-section>
      </template>

      <template v-slot:cancel>
        <q-btn
          :label="$t('Common.cancel')"
          color="primary"
          @click="showDeleteWidgetDialogRef = false"
        />
      </template>

      <template v-slot:approve>
        <q-btn
          data-test="add-widget-modal-approve"
          :label="$t('ArtivactPage.label.deleteWidget')"
          color="primary"
          @click="deleteWidgetConfirmed()"
        />
      </template>
    </artivact-dialog>

    <artivact-dialog :dialog-model="showEditMetadataDialogRef">
      <template v-slot:header>
        {{ $t('ArtivactPage.dialog.editMetadata.heading') }}
      </template>

      <template v-slot:body>
        <q-card-section>
          <div class="q-mb-lg">
            {{ $t('ArtivactPage.dialog.editMetadata.description') }}
          </div>
          <div>
            {{ $t('ArtivactPage.dialog.editMetadata.desc.title') }}
            <artivact-restricted-translatable-item-editor
              :label="$t('ArtivactPage.dialog.editMetadata.label.title')"
              :translatable-string="pageContentRef.metaData.title"
              :show-separator="false"
            />
          </div>
          <div>
            {{ $t('ArtivactPage.dialog.editMetadata.desc.description') }}
            <artivact-restricted-translatable-item-editor
              :label="$t('ArtivactPage.dialog.editMetadata.label.description')"
              :translatable-string="pageContentRef.metaData.description"
              :show-separator="false"
            />
          </div>
          <div class="q-mb-md">
            {{ $t('ArtivactPage.dialog.editMetadata.desc.author') }}
            <q-input
              v-model="pageContentRef.metaData.author"
              :label="$t('ArtivactPage.dialog.editMetadata.label.author')"
              outlined
            />
          </div>
          <div>
            {{ $t('ArtivactPage.dialog.editMetadata.desc.keywords') }}
            <artivact-restricted-translatable-item-editor
              :label="$t('ArtivactPage.dialog.editMetadata.label.keywords')"
              :translatable-string="pageContentRef.metaData.keywords"
              :show-separator="false"
            />
          </div>
        </q-card-section>
      </template>

      <template v-slot:cancel>
        <q-btn
          :label="$t('Common.cancel')"
          color="primary"
          @click="showEditMetadataDialogRef = false"
        />
      </template>

      <template v-slot:approve>
        <q-btn
          data-test="add-widget-modal-approve"
          :label="$t('Common.save')"
          color="primary"
          @click="updateMetadata()"
        />
      </template>
    </artivact-dialog>
  </div>
</template>

<script setup lang="ts">
import Draggable from 'vuedraggable';
import {computed, nextTick, onBeforeUnmount, onMounted, PropType, ref, toRef} from 'vue';
import {ButtonConfig, PageContent, TranslatableString,} from './artivact-models';
import {translate} from './artivact-utils';
import {useUserdataStore} from '../stores/userdata';
import {
  AvatarWidgetData,
  ButtonsWidgetData,
  ImageGalleryWidgetData,
  ImageGalleryWidgetTextPosition,
  InfoBoxWidgetData,
  ItemSearchWidget,
  PageTitleWidgetData,
  TextWidgetData,
} from './widgets/artivact-widget-models';
import {useI18n} from 'vue-i18n';
import ArtivactDialog from './ArtivactDialog.vue';
import ArtivactAvatarWidget from './widgets/ArtivactAvatarWidget.vue';
import ArtivactInfoBoxWidget from './widgets/ArtivactInfoBoxWidget.vue';
import ArtivactTextWidget from './widgets/ArtivactTextWidget.vue';
import ArtivactPageTitleWidget from './widgets/ArtivactPageTitleWidget.vue';
import ArtivactItemSearchWidget from './widgets/ArtivactItemSearchWidget.vue';
import {usePageStore} from '../stores/page';
import ArtivactImageGalleryWidget from './widgets/ArtivactImageGalleryWidget.vue';
import ArtivactButtonsWidget from './widgets/ArtivactButtonsWidget.vue';
import {useProfilesStore} from '../stores/profiles';
import ArtivactRestrictedTranslatableItemEditor from './ArtivactRestrictedTranslatableItemEditor.vue';

const props = defineProps({
  pageId: {
    required: true,
    type: String,
  },
  pageContent: {
    required: true,
    type: Object as PropType<PageContent>,
  },
  inEditMode: {
    required: true,
    type: Boolean,
  },
});

const emit = defineEmits<{
  (e: 'file-added', widgetId: string, property: string): void;
  (e: 'save-widget-before-upload', payload: {
    resolve: () => void;
    reject: () => void;
  }): void;
  (
    e: 'file-deleted',
    widgetId: string,
    property: string,
    filename: string,
  ): void;
  (e: 'update-page-content'): void;
  (e: 'enter-edit-mode'): void;
  (e: 'exit-edit-mode'): void;
  (e: 'reset-wip'): void;
  (e: 'publish-wip'): void;
}>();

const i18n = useI18n();

const userdataStore = useUserdataStore();
const pageStore = usePageStore();
const profilesStore = useProfilesStore();

const pageContentRef = toRef(props, 'pageContent');
const inEditModeRef = toRef(props, 'inEditMode');

const showAddWidgetDialogRef = ref(false);
const selectedWidgetTypeRef = ref('PAGE_TITLE');
const addWidgetBelowRef = ref('');

const showDeleteWidgetDialogRef = ref(false);
const deleteWidgetRef = ref(-1);

const showEditMetadataDialogRef = ref(false);

const NAV_ANCHOR_PREFIX = 'nav-';
const HEADER_HEIGHT_PX = 64;

const navigationItems = computed(() => {
  if (!pageContentRef.value?.widgets) return [];
  return pageContentRef.value.widgets
    .reduce((items: { id: string; label: string }[], widget) => {
      const label = translate(widget.navigationTitle);
      if (label && label.trim() !== '') {
        items.push({ id: widget.id, label });
      }
      return items;
    }, []);
});

const showSideNavigation = computed(() => {
  return !inEditModeRef.value && navigationItems.value.length > 1;
});

function scrollToWidget(id: string) {
  const element = document.getElementById(NAV_ANCHOR_PREFIX + id);
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' });
  }
}

const hasLeadingPageTitleWidget = computed(() => {
  return pageContentRef.value?.widgets?.length > 0
    && pageContentRef.value.widgets[0].type === 'PAGE_TITLE';
});

const pageTitleBottomRef = ref(0);

const sideNavigationStyle = computed(() => {
  if (hasLeadingPageTitleWidget.value) {
    const effectiveTop = Math.max(pageTitleBottomRef.value, HEADER_HEIGHT_PX);
    return {
      top: effectiveTop + 'px',
      maxHeight: `calc(100vh - ${effectiveTop}px)`,
    };
  }
  return {};
});

let scrollRafId = 0;

function onScroll() {
  if (!scrollRafId) {
    scrollRafId = requestAnimationFrame(() => {
      updatePageTitleBottom();
      scrollRafId = 0;
    });
  }
}

function updatePageTitleBottom() {
  if (hasLeadingPageTitleWidget.value && pageContentRef.value?.widgets?.length > 0) {
    const firstWidgetId = pageContentRef.value.widgets[0].id;
    const el = document.getElementById(NAV_ANCHOR_PREFIX + firstWidgetId);
    if (el) {
      pageTitleBottomRef.value = Math.max(el.getBoundingClientRect().bottom, 0);
    }
  }
}

const availableWidgetTypes = [
  'PAGE_TITLE',
  'TEXT',
  'ITEM_SEARCH',
  'INFO_BOX',
  'AVATAR',
  'IMAGE_GALLERY',
  'BUTTONS',
].sort((a, b) => {
  const labelA = i18n.t(a).toString();
  const labelB = i18n.t(b).toString();
  return labelA.localeCompare(labelB);
});

function addWidget() {
  let index = pageContentRef.value?.widgets.length;
  if (addWidgetBelowRef.value !== '') {
    index =
      pageContentRef.value?.widgets.findIndex(
        (element) => element.id === addWidgetBelowRef.value,
      ) + 1;
  }

  addWidgetBelowRef.value = '';

  if (selectedWidgetTypeRef.value === 'PAGE_TITLE') {
    pageContentRef.value?.widgets.splice(index, 0, {
      type: 'PAGE_TITLE',
      id: '',
      restrictions: [] as string[],
      navigationTitle: {
        value: '',
      } as TranslatableString,
      title: {
        value: i18n.t('ArtivactPage.label.pageTitle'),
      } as TranslatableString,
      backgroundImage: '',
    } as PageTitleWidgetData);
  } else if (selectedWidgetTypeRef.value === 'TEXT') {
    pageContentRef.value?.widgets.splice(index, 0, {
      type: 'TEXT',
      id: '',
      restrictions: [] as string[],
      navigationTitle: {
        value: '',
      } as TranslatableString,
      heading: {
        value: i18n.t('ArtivactPage.label.textTitle'),
      } as TranslatableString,
      content: {
        value: i18n.t('ArtivactPage.label.textContent'),
      } as TranslatableString,
    } as TextWidgetData);
  } else if (selectedWidgetTypeRef.value === 'ITEM_SEARCH') {
    pageContentRef.value?.widgets.splice(index, 0, {
      type: 'ITEM_SEARCH',
      id: '',
      restrictions: [] as string[],
      navigationTitle: {
        value: '',
      } as TranslatableString,
      heading: {
        value: i18n.t('ArtivactPage.label.text'),
      } as TranslatableString,
      content: {
        value: i18n.t('ArtivactPage.label.text'),
      } as TranslatableString,
      searchTerm: '',
      pageSize: 9,
      maxResults: 100,
    } as ItemSearchWidget);
  } else if (selectedWidgetTypeRef.value === 'INFO_BOX') {
    pageContentRef.value?.widgets.splice(index, 0, {
      type: 'INFO_BOX',
      id: '',
      restrictions: [] as string[],
      navigationTitle: {
        value: '',
      } as TranslatableString,
      heading: {
        value: i18n.t('ArtivactPage.label.infoBoxTitle'),
      } as TranslatableString,
      content: {
        value: i18n.t('ArtivactPage.label.infoBoxContent'),
      } as TranslatableString,
      boxType: 'INFO',
    } as InfoBoxWidgetData);
  } else if (selectedWidgetTypeRef.value === 'AVATAR') {
    pageContentRef.value?.widgets.splice(index, 0, {
      type: 'AVATAR',
      id: '',
      restrictions: [] as string[],
      navigationTitle: {
        value: '',
      } as TranslatableString,
      avatarImage: '',
      avatarSubtext: {
        value: i18n.t('ArtivactPage.label.avatarSubtext'),
      } as TranslatableString,
    } as AvatarWidgetData);
  } else if (selectedWidgetTypeRef.value === 'IMAGE_GALLERY') {
    pageContentRef.value?.widgets.splice(index, 0, {
      type: 'IMAGE_GALLERY',
      id: '',
      restrictions: [] as string[],
      navigationTitle: {
        value: '',
      } as TranslatableString,
      heading: {
        value: i18n.t('ArtivactPage.label.text'),
      } as TranslatableString,
      content: {
        value: i18n.t('ArtivactPage.label.text'),
      } as TranslatableString,
      images: [],
      fullscreenAllowed: true,
      textPosition: ImageGalleryWidgetTextPosition.TOP,
    } as ImageGalleryWidgetData);
  } else if (selectedWidgetTypeRef.value === 'BUTTONS') {
    pageContentRef.value?.widgets.splice(index, 0, {
      type: 'BUTTONS',
      id: '',
      restrictions: [] as string[],
      navigationTitle: {
        value: '',
      } as TranslatableString,
      columns: 1,
      buttonConfigs: [
        {
          targetUrl: '',
          iconLeft: '',
          label: {
            value: 'Button',
          } as TranslatableString,
          iconRight: '',
          size: 1,
          buttonColor: 'primary',
          textColor: 'white',
        } as ButtonConfig,
      ],
    } as ButtonsWidgetData);
  }
  showAddWidgetDialogRef.value = false;

  pageStore.setLatestWidgetIndex(index);

  emit('update-page-content');
}

function deleteWidget(index: number) {
  deleteWidgetRef.value = index;
  showDeleteWidgetDialogRef.value = true;
}

function deleteWidgetConfirmed() {
  pageContentRef.value?.widgets.splice(deleteWidgetRef.value, 1);
  deleteWidgetRef.value = -1;
  showDeleteWidgetDialogRef.value = false;
  emit('update-page-content');
}

function fileAdded(propertyName: string, widgetId: string) {
  emit('file-added', widgetId, propertyName);
}

function fileDeleted(parameters: string[], widgetId: string) {
  emit('file-deleted', widgetId, parameters[0], parameters[1]);
}

function updateMetadata() {
  showEditMetadataDialogRef.value = false;
  emit('update-page-content');
}

async function saveWidgetBeforeUpload({resolve, reject}) {
  emit('save-widget-before-upload', {resolve, reject})
}

onMounted(async () => {
  if (pageStore.isNewPageCreated) {
    pageStore.setNewPageCreated(false);
    showAddWidgetDialogRef.value = true;
    emit('enter-edit-mode');
  }

  await nextTick();

  window.addEventListener('scroll', onScroll, { passive: true });
  updatePageTitleBottom();

  const hash = window.location.hash;
  if (hash) {
    const element = document.getElementById(hash.substring(1));
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  }
});

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll);
  if (scrollRafId) {
    cancelAnimationFrame(scrollRafId);
  }
});
</script>

<style scoped>
.main-nav-button {
  z-index: 2;
}

.edit-page-button {
  z-index: 2;
}

.sticky {
  position: sticky;
  top: 3.5em;
  z-index: 2;
}

.widget:hover {
  background-color: #eaeaea;
}

.page {
  height: 100%;
  width: 100%;
  position: absolute;
}

.side-navigation {
  position: fixed;
  top: 4em;
  left: 0;
  padding: 1em;
  max-height: calc(100vh - 4em);
  overflow-y: auto;
  z-index: 1;
  width: 180px;
}

.side-navigation-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.side-navigation-item {
  margin-bottom: 0.5em;
}

.side-navigation-item a {
  text-decoration: none;
  color: var(--q-primary);
}

.side-navigation-item a:hover {
  text-decoration: underline;
}

.widget-anchor {
  scroll-margin-top: 4em;
}

</style>
