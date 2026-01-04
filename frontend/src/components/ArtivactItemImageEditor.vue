<template>
  <h2 class="av-text-h2">
    {{ $t('Common.items.images') }}
    <q-btn
      text-color="primary"
      round
      dense
      flat
      color="accent"
      icon="more_vert"
    >
      <q-menu
        anchor="top right"
        self="top left"
        auto-close
        transition-show="jump-right"
        transition-hide="jump-left"
      >
        <div class="row no-wrap q-pa-sm">
          <q-btn
            data-test="item-media-images-folder-button"
            v-if="profilesStore.isDesktopModeEnabled"
            text-color="primary"
            class="q-mr-md"
            round
            dense
            color="accent"
            icon="folder"
            @click="openImagesDir"
          >
            <q-tooltip>{{ $t('ItemImageEditor.tooltip.open') }}</q-tooltip>
          </q-btn>
          <q-btn
            data-test="item-media-images-upload-button"
            text-color="primary"
            round
            dense
            color="accent"
            icon="upload_file"
            @click="showUploadFilesModalRef = true"
          >
            <q-tooltip>{{ $t('ItemImageEditor.tooltip.upload') }}</q-tooltip>
          </q-btn>
        </div>
      </q-menu>
    </q-btn>
  </h2>

  <div class="q-mb-xl">
    <div class="row">
      <draggable
        :list="imagesRef"
        handle=".iamge-move-icon"
        item-key="fileName"
        group="images"
        class="row"
      >
        <template #item="{ element }">
          <q-card class="image-card q-mr-md q-mt-md">
            <q-img
              :src="element.url + '?imageSize=ITEM_CARD'"
              class="image-card-img"
              fit="none"
            >
              <div class="absolute-bottom">
                <div class="text-h6">{{ element.fileName }}</div>
              </div>
            </q-img>
            <q-card-actions>
              <q-icon name="drag_indicator" class="iamge-move-icon" size="md">
                <q-tooltip>{{ $t('ItemImageEditor.tooltip.move') }}</q-tooltip>
              </q-icon>
              <q-space/>
              <q-btn
                icon="delete"
                round
                dense
                flat
                size="md"
                color="primary"
                @click="showDeleteModelSetConfirm(element)"
              >
                <q-tooltip>{{
                    $t('ItemImageEditor.tooltip.delete')
                  }}
                </q-tooltip>
              </q-btn>
            </q-card-actions>
          </q-card>
        </template>
      </draggable>
    </div>

    <!-- UPLOAD FILES -->
    <artivact-dialog
      :dialog-model="showUploadFilesModalRef"
      :hide-buttons="true"
      :show-close-button="true"
      @close-dialog="showUploadFilesModalRef = false"
    >
      <template v-slot:header>
        {{ $t('ItemImageEditor.dialog.upload.heading') }}
      </template>

      <template v-slot:body>
        <q-card-section>
          <q-uploader
            :url="'/api/item/' + itemId + '/image'"
            :label="$t('ItemImageEditor.dialog.upload.label')"
            multiple
            auto-upload
            class="uploader q-mb-md full-width"
            accept=".jpg, image/*"
            field-name="file"
            :no-thumbnails="true"
            @finish="
              showUploadFilesModalRef = false;
              $emit('uploaded');
            "
          />
        </q-card-section>
      </template>
    </artivact-dialog>

    <!-- DELETE CONFIRMATION DIALOG -->
    <artivact-dialog :dialog-model="confirmDeleteRef" :warn="true">
      <template v-slot:header>
        {{ $t('ItemImageEditor.dialog.delete.heading') }}
      </template>

      <template v-slot:body>
        <q-card-section>
          {{ $t('ItemImageEditor.dialog.delete.description') }}
        </q-card-section>
      </template>

      <template v-slot:cancel>
        <q-btn
          :label="$t('Common.cancel')"
          color="primary"
          @click="confirmDeleteRef = false"
        />
      </template>

      <template v-slot:approve>
        <q-btn
          :label="$t('ItemImageEditor.dialog.delete.approve')"
          color="primary"
          @click="deleteImage"
        />
      </template>
    </artivact-dialog>
  </div>
</template>

<script setup lang="ts">
// noinspection ES6UnusedImports
import draggable from 'vuedraggable';
import {PropType, ref, toRef} from 'vue';
import {Asset} from './artivact-models';
import {useQuasar} from 'quasar';
import {useI18n} from 'vue-i18n';
import {api} from '../boot/axios';
import ArtivactDialog from '../components/ArtivactDialog.vue';
import {useProfilesStore} from '../stores/profiles';
import {useUserdataStore} from "../stores/userdata";

const quasar = useQuasar();
const i18n = useI18n();

const profilesStore = useProfilesStore();
const userDataStore = useUserdataStore();

const props = defineProps({
  itemId: {
    required: true,
    type: String,
  },
  images: {
    required: true,
    type: Object as PropType<Asset[]>,
    default: [] as Asset[],
  },
});

defineEmits(['uploaded']);

const imagesRef = toRef(props, 'images');

const showUploadFilesModalRef = ref(false);

const confirmDeleteRef = ref(false);
let selectedImageIndex: number;

function openImagesDir() {
  api
    .put('/api/item/' + props.itemId + '/media-creation/open-images-dir')
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemImageEditor.messages.openFailed'),
        icon: 'report_problem',
      });
    });
}

function showDeleteModelSetConfirm(element: Asset) {
  selectedImageIndex = imagesRef.value?.indexOf(element);
  confirmDeleteRef.value = true;
}

function deleteImage() {
  imagesRef.value.splice(selectedImageIndex, 1);
  confirmDeleteRef.value = false;
}
</script>

<style scoped>
.image-card {
  width: 200px;
}

.image-card-img {
  width: 200px;
  height: 200px;
}
</style>
