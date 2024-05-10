<template>
  <div v-if="pageContentRef && pageId">

    <div class="col items-center sticky gt-md" v-if="userdataStore.isUserOrAdmin && pageId !== 'INDEX' && !inEditMode">
      <div class="absolute-top-right q-ma-md">
      <q-btn id="sticky-item"
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
        <q-btn
          data-test="index-page-button"
          round
          color="primary"
          :icon="pageContentRef.indexPage ? 'check_circle' : 'circle'"
          class="main-nav-button"
          @click="pageContentRef.indexPage = !pageContentRef.indexPage">
          <q-tooltip>{{
              pageContentRef.indexPage ? $t('ArtivactPage.tooltip.indexPageYes') : $t('ArtivactPage.tooltip.indexPageNo')
            }}
          </q-tooltip>
        </q-btn>
      </div>

      <div class="absolute-top-right q-ma-md">
        <q-btn
          data-test="add-widget-button"
          round
          color="primary"
          icon="add"
          type="submit"
          class="q-mr-sm main-nav-button"
          @click="showAddWidgetDialogRef = true">
          <q-tooltip>{{ $t('ArtivactPage.label.addWidget') }}</q-tooltip>
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
        :class="inEditMode ? 'widget' : ''"
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

      <artivact-text-widget
        :class="inEditMode ? 'widget' : ''"
        v-if="widgetData.type === 'TEXT'"
        :widget-data="widgetData as TextWidgetData"
        :in-edit-mode="inEditMode"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @delete-widget="deleteWidget(index)"
      />

      <artivact-image-text-widget
        :class="inEditMode ? 'widget' : ''"
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

      <artivact-item-search-widget
        :class="inEditMode ? 'widget' : ''"
        v-if="widgetData.type === 'ITEM_SEARCH'"
        :widget-data="widgetData as SearchBasedWidgetData"
        :in-edit-mode="inEditMode"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @delete-widget="deleteWidget(index)"
      />

      <artivact-item-carousel-widget
        :class="inEditMode ? 'widget' : ''"
        v-if="widgetData.type === 'ITEM_CAROUSEL'"
        :widget-data="widgetData as SearchBasedWidgetData"
        :in-edit-mode="inEditMode"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @delete-widget="deleteWidget(index)"
      />

      <artivact-info-box-widget
        :class="inEditMode ? 'widget' : ''"
        v-if="widgetData.type === 'INFO_BOX'"
        :widget-data="widgetData as InfoBoxWidgetData"
        :in-edit-mode="inEditMode"
        :move-up-enabled="index > 0"
        :move-down-enabled="index < pageContentRef.widgets.length - 1"
        @move-widget-up="moveWidgetUp(pageContentRef.widgets, index)"
        @move-widget-down="moveWidgetDown(pageContentRef.widgets, index)"
        @delete-widget="deleteWidget(index)"
      />

      <artivact-avatar-widget
        :class="inEditMode ? 'widget' : ''"
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

      <artivact-space-widget
        :class="inEditMode ? 'widget' : ''"
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
import {PropType, ref, toRef} from 'vue';
import {PageContent, TranslatableString, Widget} from 'components/artivact-models';
import {useUserdataStore} from 'stores/userdata';
import {useQuasar} from 'quasar';
import {
  AvatarWidgetData,
  ImageTextWidgetData,
  InfoBoxWidgetData,
  PageTitleWidgetData,
  SearchBasedWidgetData,
  SpaceWidgetData,
  TextWidgetData,
} from 'components/widgets/artivact-widget-models';
import {moveDown, moveUp} from 'components/artivact-utils';
import {api} from 'boot/axios';
import {useI18n} from 'vue-i18n';
import ArtivactDialog from 'components/ArtivactDialog.vue';
import ArtivactAvatarWidget from 'components/widgets/ArtivactAvatarWidget.vue';
import ArtivactImageTextWidget from 'components/widgets/ArtivactImageTextWidget.vue';
import ArtivactInfoBoxWidget from 'components/widgets/ArtivactInfoBoxWidget.vue';
import ArtivactSpaceWidget from 'components/widgets/ArtivactSpaceWidget.vue';
import ArtivactItemCarouselWidget from 'components/widgets/ArtivactItemCarouselWidget.vue';
import ArtivactTextWidget from 'components/widgets/ArtivactTextWidget.vue';
import ArtivactPageTitleWidget from 'components/widgets/ArtivactPageTitleWidget.vue';
import ArtivactItemSearchWidget from 'components/widgets/ArtivactItemSearchWidget.vue';
import ArtivactContent from 'components/ArtivactContent.vue';

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
const i18n = useI18n();

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
  'ITEM_SEARCH',
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
        value: i18n.t('ArtivactPage.label.pageTitle'),
      } as TranslatableString,
      backgroundImage: '',
    } as PageTitleWidgetData);
  } else if (selectedWidgetTypeRef.value === 'TEXT') {
    pageContentRef.value?.widgets.push({
      type: 'TEXT',
      id: '',
      restrictions: [] as string[],
      heading: {
        value: i18n.t('ArtivactPage.label.textTitle'),
      } as TranslatableString,
      content: {
        value: i18n.t('ArtivactPage.label.textContent'),
      } as TranslatableString,
    } as TextWidgetData);
  } else if (selectedWidgetTypeRef.value === 'IMAGE_TEXT') {
    pageContentRef.value?.widgets.push({
      type: 'IMAGE_TEXT',
      id: '',
      restrictions: [] as string[],
      text: {
        value: i18n.t('ArtivactPage.label.text'),
      } as TranslatableString,
    } as ImageTextWidgetData);
  } else if (selectedWidgetTypeRef.value === 'ITEM_SEARCH') {
    pageContentRef.value?.widgets.push({
      type: 'ITEM_SEARCH',
      id: '',
      restrictions: [] as string[],
      searchTerm: '',
      maxResults: 100,
    } as SearchBasedWidgetData);
  } else if (selectedWidgetTypeRef.value === 'ITEM_CAROUSEL') {
    pageContentRef.value?.widgets.push({
      type: 'ITEM_CAROUSEL',
      id: '',
      restrictions: [] as string[],
      searchTerm: '*',
      maxResults: 9,
    } as SearchBasedWidgetData);
  } else if (selectedWidgetTypeRef.value === 'INFO_BOX') {
    pageContentRef.value?.widgets.push({
      type: 'INFO_BOX',
      id: '',
      restrictions: [] as string[],
      heading: {
        value: i18n.t('ArtivactPage.label.infoBoxTitle'),
      } as TranslatableString,
      content: {
        value: i18n.t('ArtivactPage.label.infoBoxContent'),
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
        value: i18n.t('ArtivactPage.label.avatarSubtext'),
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

</style>
