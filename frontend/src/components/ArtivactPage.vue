<template>

  <div v-if="pageContentRef && pageId" :class="inEditMode ? 'page' : ''">

    <q-menu :context-menu="true" v-if="inEditMode">
      <q-list>
        <q-item
          clickable
          v-close-popup
          @click="showAddWidgetDialogRef = true">
          <q-item-section>
            <label>
              <q-icon
                name="add"
                size="xs"
                color="primary"
                class="q-mr-sm"
              />
              {{ $t('ArtivactPage.label.addWidget') }}</label
            >
          </q-item-section>
        </q-item>
      </q-list>
    </q-menu>

    <div class="col items-center sticky gt-md" v-if="userdataStore.isUserOrAdmin && pageId !== 'INDEX' && !inEditMode">
      <div class="absolute-top-right q-ma-md">
        <q-btn id="sticky-item"
               :disable="!pageContentRef.editable"
               data-test="edit-page-button"
               round
               color="primary"
               icon="edit"
               class="edit-page-button float-right"
               @click="inEditMode = true"
        >
          <q-tooltip>{{ $t('ArtivactPage.tooltip.edit') }}</q-tooltip>
        </q-btn>
      </div>
    </div>

    <div class="col items-center sticky gt-md" v-if="inEditMode">
      <div class="absolute-top-left q-ma-md">
        <q-btn
          data-test="close-button"
          round
          color="primary"
          icon="close"
          class="q-mr-sm main-nav-button"
          @click="inEditMode = false">
          <q-tooltip>{{ $t('Common.cancel') }}</q-tooltip>
        </q-btn>
      </div>

      <div class="absolute-top-right q-ma-md">
        <q-btn
          data-test="index-page-button"
          color="primary"
          :icon="pageContentRef.indexPage ? 'check_circle' : 'circle'"
          class="main-nav-button rounded-borders q-mr-sm"
          @click="pageContentRef.indexPage = !pageContentRef.indexPage"
          :label="$t('ArtivactPage.label.indexPage')">
          <q-tooltip>{{
              pageContentRef.indexPage ? $t('ArtivactPage.tooltip.indexPageYes') : $t('ArtivactPage.tooltip.indexPageNo')
            }}
          </q-tooltip>
        </q-btn>
        <q-btn
          data-test="save-button"
          round
          color="primary"
          icon="save"
          class="main-nav-button"
          @click="savePage(false)">
          <q-tooltip>{{ $t('Common.save') }}</q-tooltip>
        </q-btn>
      </div>
    </div>

    <artivact-content v-if="pageContentRef.widgets.length == 0 && pageId === 'INDEX'">
      <label>
        {{ $t('ArtivactPage.label.noIndexPage') }}
      </label>
    </artivact-content>

    <template
      v-for="(widgetData, index) of pageContentRef.widgets"
      v-bind:key="widgetData.id"
    >
      <artivact-page-title-widget
        :id="widgetData.id"
        :class="inEditMode ? 'widget' : ''"
        v-if="widgetData.type === 'PAGE_TITLE'"
        :widget-data="widgetData as PageTitleWidgetData"
        :in-edit-mode="inEditMode"
        :showEditor="inEditMode && pageStore.latestWidgetIndex === index"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @add-widget-above="addWidgetAboveRef = widgetData.id; showAddWidgetDialogRef = true;"
        @add-widget-below="addWidgetBelowRef = widgetData.id; showAddWidgetDialogRef = true;"
        @delete-widget="deleteWidget(index)"
        :page-id="pageId"
        v-on:image-added="fileAdded($event, widgetData.id)"
      />

      <artivact-text-widget
        :id="widgetData.id"
        :class="inEditMode ? 'widget' : ''"
        v-if="widgetData.type === 'TEXT'"
        :widget-data="widgetData as TextWidgetData"
        :in-edit-mode="inEditMode"
        :showEditor="inEditMode && pageStore.latestWidgetIndex === index"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @add-widget-above="addWidgetAboveRef = widgetData.id; showAddWidgetDialogRef = true;"
        @add-widget-below="addWidgetBelowRef = widgetData.id; showAddWidgetDialogRef = true;"
        @delete-widget="deleteWidget(index)"
      />

      <artivact-item-search-widget
        :id="widgetData.id"
        :class="inEditMode ? 'widget' : ''"
        v-if="widgetData.type === 'ITEM_SEARCH'"
        :widget-data="widgetData as ItemSearchWidget"
        :in-edit-mode="inEditMode"
        :showEditor="inEditMode && pageStore.latestWidgetIndex === index"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @add-widget-above="addWidgetAboveRef = widgetData.id; showAddWidgetDialogRef = true;"
        @add-widget-below="addWidgetBelowRef = widgetData.id; showAddWidgetDialogRef = true;"
        @delete-widget="deleteWidget(index)"
      />

      <artivact-info-box-widget
        :id="widgetData.id"
        :class="inEditMode ? 'widget' : ''"
        v-if="widgetData.type === 'INFO_BOX'"
        :widget-data="widgetData as InfoBoxWidgetData"
        :in-edit-mode="inEditMode"
        :showEditor="inEditMode && pageStore.latestWidgetIndex === index"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @add-widget-above="addWidgetAboveRef = widgetData.id; showAddWidgetDialogRef = true;"
        @add-widget-below="addWidgetBelowRef = widgetData.id; showAddWidgetDialogRef = true;"
        @delete-widget="deleteWidget(index)"
      />

      <artivact-avatar-widget
        :id="widgetData.id"
        :class="inEditMode ? 'widget' : ''"
        v-if="widgetData.type === 'AVATAR'"
        :widget-data="widgetData as AvatarWidgetData"
        :in-edit-mode="inEditMode"
        :showEditor="inEditMode && pageStore.latestWidgetIndex === index"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @add-widget-above="addWidgetAboveRef = widgetData.id; showAddWidgetDialogRef = true;"
        @add-widget-below="addWidgetBelowRef = widgetData.id; showAddWidgetDialogRef = true;"
        @delete-widget="deleteWidget(index)"
        :page-id="pageId"
        v-on:image-added="fileAdded($event, widgetData.id)"
      />

      <artivact-image-gallery-widget
        :id="widgetData.id"
        :class="inEditMode ? 'widget' : ''"
        v-if="widgetData.type === 'IMAGE_GALLERY'"
        :widget-data="widgetData as ImageGalleryWidgetData"
        :page-id="pageId"
        :in-edit-mode="inEditMode"
        :showEditor="inEditMode && pageStore.latestWidgetIndex === index"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @add-widget-above="addWidgetAboveRef = widgetData.id; showAddWidgetDialogRef = true;"
        @add-widget-below="addWidgetBelowRef = widgetData.id; showAddWidgetDialogRef = true;"
        @delete-widget="deleteWidget(index)"
        @image-added="fileAdded($event, widgetData.id)"
        @image-deleted="fileDeleted($event, widgetData.id)"
      />
    </template>

    <artivact-dialog :data-test="'add-widget-modal'" :dialog-model="showAddWidgetDialogRef"
                     v-if="userdataStore.isUserOrAdmin && inEditMode">
      <template v-slot:header>
        {{ $t('ArtivactPage.dialog.heading') }}
      </template>

      <template v-slot:body>
        <q-card-section>
          <div class="q-mb-lg">
            {{ $t('ArtivactPage.dialog.description') }}
          </div>
          <q-select
            data-test="add-widget-selection"
            outlined
            v-model="selectedWidgetTypeRef"
            :options="availableWidgetTypes"
            :option-label="(option) => $t(option)"
            :label="$t('ArtivactPage.dialog.type')"
          >
            <template v-slot:option="scope">
              <q-item :data-test="'add-widget-selection-' + scope.opt" v-bind="scope.itemProps">
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
        <q-btn data-test="add-widget-modal-approve" :label="$t('ArtivactPage.label.addWidget')" color="primary"
               @click="addWidget()"/>
      </template>
    </artivact-dialog>

  </div>
</template>

<script setup lang="ts">
import {onMounted, PropType, ref, toRef} from 'vue';
import {PageContent, TranslatableString, Widget} from './artivact-models';
import {useUserdataStore} from '../stores/userdata';
import {useQuasar} from 'quasar';
import {
  AvatarWidgetData,
  ImageGalleryWidgetData,
  ImageGalleryWidgetTextPosition,
  InfoBoxWidgetData,
  ItemSearchWidget,
  PageTitleWidgetData,
  TextWidgetData
} from './widgets/artivact-widget-models';
import {moveDown, moveUp} from './artivact-utils';
import {api} from '../boot/axios';
import {useI18n} from 'vue-i18n';
import ArtivactDialog from './ArtivactDialog.vue';
import ArtivactAvatarWidget from './widgets/ArtivactAvatarWidget.vue';
import ArtivactInfoBoxWidget from './widgets/ArtivactInfoBoxWidget.vue';
import ArtivactTextWidget from './widgets/ArtivactTextWidget.vue';
import ArtivactPageTitleWidget from './widgets/ArtivactPageTitleWidget.vue';
import ArtivactItemSearchWidget from './widgets/ArtivactItemSearchWidget.vue';
import ArtivactContent from './ArtivactContent.vue';
import {usePageStore} from '../stores/page';
import ArtivactImageGalleryWidget from "./widgets/ArtivactImageGalleryWidget.vue";

const props = defineProps({
  pageId: {
    required: true,
    type: String,
  },
  pageContent: {
    required: true,
    type: Object as PropType<PageContent>,
  },
});

const emit = defineEmits<{
  (e: 'update-page-content', pageContent: PageContent): void;
  (e: 'file-added', widgetId: string, property: string): void;
  (e: 'file-deleted', widgetId: string, property: string, filename: string): void;
}>();

const quasar = useQuasar();
const i18n = useI18n();

const userdataStore = useUserdataStore();
const pageStore = usePageStore();

const pageContentRef = toRef(props, 'pageContent');
const pageIdRef = toRef(props, 'pageId');

const showAddWidgetDialogRef = ref(false);
const selectedWidgetTypeRef = ref('PAGE_TITLE');
const inEditMode = ref(false);

const addWidgetAboveRef = ref('');
const addWidgetBelowRef = ref('');

const availableWidgetTypes = [
  'PAGE_TITLE',
  'TEXT',
  'ITEM_SEARCH',
  'INFO_BOX',
  'AVATAR',
  'IMAGE_GALLERY'
];

function addWidget() {

  let index = pageContentRef.value?.widgets.length;
  if (addWidgetAboveRef.value !== '') {
    index = pageContentRef.value?.widgets.findIndex((element) => element.id === addWidgetAboveRef.value);
  } else if (addWidgetBelowRef.value !== '') {
    index = pageContentRef.value?.widgets.findIndex((element) => element.id === addWidgetBelowRef.value) + 1;
  }

  addWidgetAboveRef.value = '';
  addWidgetBelowRef.value = '';

  if (selectedWidgetTypeRef.value === 'PAGE_TITLE') {
    pageContentRef.value?.widgets.splice(index, 0, {
      type: 'PAGE_TITLE',
      id: '',
      restrictions: [] as string[],
      navigationTitle: {
        value: ''
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
        value: ''
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
        value: ''
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
        value: ''
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
        value: ''
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
        value: ''
      } as TranslatableString,
      heading: {
        value: i18n.t('ArtivactPage.label.text'),
      } as TranslatableString,
      content: {
        value: i18n.t('ArtivactPage.label.text'),
      } as TranslatableString,
      images: [],
      fullscreenAllowed: true,
      textPosition: ImageGalleryWidgetTextPosition.TOP
    } as ImageGalleryWidgetData);
  }
  showAddWidgetDialogRef.value = false;
  savePage(false);

  pageStore.setLatestWidgetIndex(index);
}

function deleteWidget(index: number) {
  pageContentRef.value?.widgets.splice(index, 1);
}

function moveWidgetUp(array: [Widget], index: number) {
  moveUp(array, index);
}

function moveWidgetDown(array: [Widget], index: number) {
  moveDown(array, index);
}

function savePage(leaveEditMode: boolean) {
  api
    .post('/api/page/' + pageIdRef.value, pageContentRef.value)
    .then((response) => {
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.success', {item: i18n.t('Common.items.page')}),
        icon: 'check',
      });
      emit('update-page-content', response.data);
      if (leaveEditMode) {
        inEditMode.value = false;
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failed', {item: i18n.t('Common.items.page')}),
        icon: 'report_problem',
      });
    });
}

function fileAdded(propertyName: string, widgetId: string) {
  emit('file-added', widgetId, propertyName);
}

function fileDeleted(parameters: string[], widgetId: string) {
  emit('file-deleted', widgetId, parameters[0], parameters[1]);
}

onMounted(() => {
  if (pageStore.isNewPageCreated) {
    pageStore.setNewPageCreated(false);
    inEditMode.value = true
    showAddWidgetDialogRef.value = true
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
  background-color: #EAEAEA;
}

.page {
  height: 100%;
  width: 100%;
  position: absolute;
}

.page:hover {
  cursor: pointer;
}

</style>
