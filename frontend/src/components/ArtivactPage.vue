<template>
  <div v-if="pageContentRef && pageId">
    <q-btn
      round
      color="primary"
      icon="edit"
      class="absolute-top-right q-ma-md edit-page-button"
      @click="inEditMode = true"
      v-if="userdataStore.isUserOrAdmin && pageId !== 'INDEX' && !inEditMode"
    />

    <q-toolbar
      v-if="userdataStore.isUserOrAdmin && inEditMode"
      class="sticky-toolbar gt-sm"
    >
      <q-btn label="Cancel" color="primary" @click="inEditMode = false" />
      <q-checkbox
        v-model="pageContentRef.indexPage"
        class="col-5 q-ml-lg"
        name="user"
        label="Is this the index page?"
      />
      <q-space />
      <q-btn
        label="Add Widget"
        @click="showAddWidgetDialogRef = true"
        color="primary"
        class="q-mr-lg"
      />
      <q-btn label="Save" @click="savePage(true)" color="primary" />
    </q-toolbar>

    <q-toolbar
      v-if="userdataStore.isUserOrAdmin && inEditMode"
      class="sticky-toolbar lt-md"
    >
      <q-btn
        icon="cancel"
        @click="inEditMode = false"
        flat
        dense
        round
        size="lg"
      />
      <q-checkbox
        v-model="pageContentRef.indexPage"
        class="col-5 q-ml-lg"
        name="user"
        label="Index page?"
      />
      <q-space />
      <q-btn
        icon="add"
        @click="showAddWidgetDialogRef = true"
        flat
        dense
        round
        size="lg"
        class="q-mr-lg"
      />
      <q-btn icon="save" @click="savePage(true)" flat dense round size="lg" />
    </q-toolbar>

    <template
      v-for="(widgetData, index) of pageContentRef.widgets"
      v-bind:key="widgetData.id"
    >
      <page-title-widget
        v-if="widgetData.type === 'PAGE_TITLE'"
        :widget-data="widgetData as PageTitleWidgetData"
        :in-edit-mode="inEditMode"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @delete-widget="deleteWidget(index)"
        :page-id="pageId"
        v-on:image-added="fileAdded($event, widgetData.id)"
      />

      <text-widget
        v-if="widgetData.type === 'TEXT'"
        :widget-data="widgetData as TextWidgetData"
        :in-edit-mode="inEditMode"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @delete-widget="deleteWidget(index)"
      />

      <image-text-widget
        v-if="widgetData.type === 'IMAGE_TEXT'"
        :widget-data="widgetData as ImageTextWidgetData"
        :in-edit-mode="inEditMode"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @delete-widget="deleteWidget(index)"
        :page-id="pageId"
        v-on:image-added="fileAdded($event, widgetData.id)"
      />

      <search-widget
        v-if="widgetData.type === 'SEARCH'"
        :widget-data="widgetData as SearchWidgetData"
        :in-edit-mode="inEditMode"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @delete-widget="deleteWidget(index)"
      />

      <item-carousel-widget
        v-if="widgetData.type === 'ITEM_CAROUSEL'"
        :widget-data="widgetData as ItemCarouselWidgetData"
        :in-edit-mode="inEditMode"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @delete-widget="deleteWidget(index)"
      />

      <info-box-widget
        v-if="widgetData.type === 'INFO_BOX'"
        :widget-data="widgetData as InfoBoxWidgetData"
        :in-edit-mode="inEditMode"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @delete-widget="deleteWidget(index)"
      />

      <avatar-widget
        v-if="widgetData.type === 'AVATAR'"
        :widget-data="widgetData as AvatarWidgetData"
        :in-edit-mode="inEditMode"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @delete-widget="deleteWidget(index)"
        :page-id="pageId"
        v-on:image-added="fileAdded($event, widgetData.id)"
      />

      <space-widget
        v-if="widgetData.type === 'SPACE'"
        :widget-data="widgetData as SpaceWidgetData"
        :in-edit-mode="inEditMode"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @delete-widget="deleteWidget(index)"
      />
    </template>

    <q-dialog
      v-model="showAddWidgetDialogRef"
      persistent
      v-if="userdataStore.isUserOrAdmin && inEditMode"
    >
      <q-card>
        <q-card-section class="bg-primary text-white">
          <div class="text-h6">Widget Selection</div>
        </q-card-section>

        <q-card-section>
          <div class="q-mb-lg">
            {{ $t("addWidgetWarn") }}
          </div>
          <q-select
            outlined
            v-model="selectedWidgetTypeRef"
            :options="availableWidgetTypes"
            :option-label="(option) => $t(option)"
            :label="$t('widgetType')"
          />
          <div class="q-mt-md" style="overflow-wrap: break-word">
            {{ $t(selectedWidgetTypeRef + "_DESCRIPTION") }}
          </div>
        </q-card-section>

        <q-card-actions>
          <q-btn
            label="Cancel"
            color="primary"
            @click="showAddWidgetDialogRef = false"
          />
          <q-space />
          <q-btn label="Add" color="primary" @click="addWidget()" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup lang="ts">
import { PropType, ref, toRef } from 'vue';
import { PageContent, TranslatableString, Widget } from 'components/models';
import { useUserdataStore } from 'stores/userdata';
import TextWidget from 'components/widgets/TextWidget.vue';
import PageTitleWidget from 'components/widgets/PageTitleWidget.vue';
import SearchWidget from 'components/widgets/SearchWidget.vue';
import ItemCarouselWidget from 'components/widgets/ItemCarouselWidget.vue';
import { useQuasar } from 'quasar';
import {
  AvatarWidgetData,
  ImageTextWidgetData,
  InfoBoxWidgetData,
  ItemCarouselWidgetData,
  PageTitleWidgetData,
  SearchWidgetData,
  SpaceWidgetData,
  TextWidgetData,
} from 'components/widgets/widget-models';
import { moveDown, moveUp } from 'components/utils';
import { api } from 'boot/axios';
import InfoBoxWidget from 'components/widgets/InfoBoxWidget.vue';
import AvatarWidget from 'components/widgets/AvatarWidget.vue';
import SpaceWidget from 'components/widgets/SpaceWidget.vue';
import ImageTextWidget from 'components/widgets/ImageTextWidget.vue';

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
}>();

const quasar = useQuasar();

const userdataStore = useUserdataStore();

const pageContentRef = toRef(props, 'pageContent');
const pageIdRef = toRef(props, 'pageId');

const showAddWidgetDialogRef = ref(false);
const selectedWidgetTypeRef = ref('PAGE_TITLE');
const inEditMode = ref(false);

const availableWidgetTypes = [
  'PAGE_TITLE',
  'TEXT',
  'IMAGE_TEXT',
  'SEARCH',
  'ITEM_CAROUSEL',
  'INFO_BOX',
  'AVATAR',
  'SPACE',
];

function addWidget() {
  if (selectedWidgetTypeRef.value === 'PAGE_TITLE') {
    pageContentRef.value?.widgets.push({
      type: 'PAGE_TITLE',
      id: '',
      restrictions: [] as string[],
      title: {
        value: 'Page Title',
      } as TranslatableString,
      backgroundImage: '',
    } as PageTitleWidgetData);
  } else if (selectedWidgetTypeRef.value === 'TEXT') {
    pageContentRef.value?.widgets.push({
      type: 'TEXT',
      id: '',
      restrictions: [] as string[],
      heading: {
        value: 'Text Title',
      } as TranslatableString,
      content: {
        value: 'Text content',
      } as TranslatableString,
    } as TextWidgetData);
  } else if (selectedWidgetTypeRef.value === 'IMAGE_TEXT') {
    pageContentRef.value?.widgets.push({
      type: 'IMAGE_TEXT',
      id: '',
      restrictions: [] as string[],
      text: {
        value: 'Text',
      } as TranslatableString,
    } as ImageTextWidgetData);
  } else if (selectedWidgetTypeRef.value === 'SEARCH') {
    pageContentRef.value?.widgets.push({
      type: 'SEARCH',
      id: '',
      restrictions: [] as string[],
      searchTerm: '',
      maxResults: 100,
    } as SearchWidgetData);
  } else if (selectedWidgetTypeRef.value === 'ITEM_CAROUSEL') {
    pageContentRef.value?.widgets.push({
      type: 'ITEM_CAROUSEL',
      id: '',
      restrictions: [] as string[],
      searchTerm: '',
      maxResults: 9,
    } as ItemCarouselWidgetData);
  } else if (selectedWidgetTypeRef.value === 'INFO_BOX') {
    pageContentRef.value?.widgets.push({
      type: 'INFO_BOX',
      id: '',
      restrictions: [] as string[],
      heading: {
        value: 'Info-Box Title',
      } as TranslatableString,
      content: {
        value: 'Info-Box content',
      } as TranslatableString,
      boxType: 'INFO',
    } as InfoBoxWidgetData);
  } else if (selectedWidgetTypeRef.value === 'AVATAR') {
    pageContentRef.value?.widgets.push({
      type: 'AVATAR',
      id: '',
      restrictions: [] as string[],
      avatarImage: '',
      avatarSubtext: {
        value: 'Avatar Subtext',
      } as TranslatableString,
    } as AvatarWidgetData);
  } else if (selectedWidgetTypeRef.value === 'SPACE') {
    pageContentRef.value?.widgets.push({
      type: 'SPACE',
      id: '',
      restrictions: [] as string[],
      size: 2,
    } as SpaceWidgetData);
  }
  showAddWidgetDialogRef.value = false;
  savePage(false);
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
        message: 'Page saved',
        icon: 'done',
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
        message: 'Saving failed',
        icon: 'report_problem',
      });
    });
}

function fileAdded(propertyName: string, widgetId: string) {
  emit('file-added', widgetId, propertyName);
}
</script>

<style scoped>
.edit-page-button {
  z-index: 2;
}

.sticky-toolbar {
  position: sticky;
  top: 44px;
  background-color: white;
  z-index: 15;
  border-bottom: 1px solid var(--q-primary);
}
</style>
