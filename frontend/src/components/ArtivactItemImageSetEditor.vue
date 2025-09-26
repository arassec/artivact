<template>
  <h2 class="av-text-h2">
    {{ $t('Common.items.images') }}
    <q-btn
      data-test="item-creation-image-button"
      text-color="primary"
      round
      dense
      flat
      color="accent"
      icon="image"
      @click="captureSinglePhoto()"
    >
      <q-tooltip>{{
        $t('ItemImageSetEditor.tooltip.directCapture')
      }}</q-tooltip>
    </q-btn>
    <q-btn
      data-test="item-creation-camera-button"
      text-color="primary"
      round
      dense
      flat
      color="accent"
      icon="camera"
      @click="showCapturePhotosModalRef = true"
    >
      <q-tooltip>{{ $t('ItemImageSetEditor.tooltip.capture') }}</q-tooltip>
    </q-btn>
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
            text-color="primary"
            class="q-mr-md"
            round
            dense
            color="accent"
            icon="folder"
            @click="openImagesDir()"
          >
            <q-tooltip>{{ $t('ItemImageSetEditor.tooltip.open') }}</q-tooltip>
          </q-btn>
          <q-btn
            text-color="primary"
            round
            dense
            color="accent"
            icon="upload_file"
            @click="showUploadFilesModalRef = true"
          >
            <q-tooltip>{{ $t('ItemImageSetEditor.tooltip.upload') }}</q-tooltip>
          </q-btn>
        </div>
      </q-menu>
    </q-btn>
  </h2>

  <div class="row">
    <q-card
      v-for="(imageSet, index) in creationImageSets"
      :key="index"
      class="image-set-card q-mr-md q-mt-md"
    >
      <q-img
        :src="
          imageSet.images[0]
            ? imageSet.images[0].url + '?imageSize=ITEM_CARD'
            : ''
        "
        class="image-set-card-img"
      >
        <div class="absolute-bottom">
          <div class="text-h6">
            {{ imageSet.images.length }}
            {{ imageSet.images.length == 1 ? 'Image' : 'Images' }}
          </div>
        </div>
      </q-img>
      <q-card-section class="full-width q-pl-none q-pr-none q-pb-none">
        <div class="full-width q-ma-none">
          <q-toggle
            flat
            v-model="imageSet.modelInput"
            @click="toggleModelInput(index)"
            :disable="imageSet.images.length === 0"
            >Model input</q-toggle
          >
          <q-separator class="q-mt-sm q-mb-sm" />
        </div>
      </q-card-section>
      <q-card-actions>
        <q-btn
          :disable="imageSet.images.length === 0"
          icon="search"
          round
          dense
          flat
          size="md"
          color="primary"
          @click="showImageDetails(imageSet)"
        >
          <q-tooltip>{{ $t('ItemImageSetEditor.tooltip.details') }}</q-tooltip>
        </q-btn>
        <q-btn
          :disable="imageSet.images.length === 0"
          icon="content_cut"
          round
          dense
          flat
          size="md"
          color="primary"
          @click="removeBackgrounds(index)"
          v-if="!imageSet.backgroundRemoved"
        >
          <q-tooltip>{{
            $t('ItemImageSetEditor.tooltip.backgrounds')
          }}</q-tooltip>
        </q-btn>
        <q-space />
        <q-btn
          icon="delete"
          round
          dense
          flat
          size="md"
          color="primary"
          @click="showDeleteImageSetConfirm(index)"
        >
          <q-tooltip>{{ $t('ItemImageSetEditor.tooltip.delete') }}</q-tooltip>
        </q-btn>
      </q-card-actions>
    </q-card>
  </div>

  <!-- TRANSFER SINGLE PHOTO TO MEDIA -->
  <artivact-dialog :dialog-model="showTransferPhotoToMediaModalRef">
    <template v-slot:header>
      {{ $t('ItemImageSetEditor.transferPhotoToMedia') }}
    </template>

    <template v-slot:body>
      <q-card-section>
        <q-img
          :src="
            '/api/item/' +
            itemId +
            '/image/' +
            tempImageFilenameRef +
            '?imageSize=DETAIL'
          "
        ></q-img>
      </q-card-section>
    </template>

    <template v-slot:cancel>
      <q-btn
        color="primary"
        :label="$t('Common.cancel')"
        @click="deleteTempImage()"
      />
    </template>

    <template v-slot:approve>
      <q-btn
        color="primary"
        :label="$t('ItemImageSetEditor.label.add')"
        @click="addTempImageToMedia()"
      />
    </template>
  </artivact-dialog>

  <!-- SINGLE PHOTO CAPTURE IN PROGRESS -->
  <artivact-dialog
    :dialog-model="showCaptureSinglePhotoModalRef"
    :hide-buttons="true"
  >
    <template v-slot:header>
      {{ $t('ItemImageSetEditor.dialog.captureSinglePhotoInProgress.heading') }}
    </template>

    <template v-slot:body>
      <q-card-section>
        <div class="row">
          <div>
            <q-spinner size="2em" class="q-mr-md col" />
          </div>
          <div>
            {{
              $t(
                'ItemImageSetEditor.dialog.captureSinglePhotoInProgress.description',
              )
            }}
          </div>
        </div>
      </q-card-section>
    </template>
  </artivact-dialog>

  <!-- CAPTURE PHOTOS -->
  <artivact-dialog :dialog-model="showCapturePhotosModalRef">
    <template v-slot:header>
      {{ $t('ItemImageSetEditor.captureParameters') }}
    </template>

    <template v-slot:body>
      <q-card-section>
        <q-input
          outlined
          v-model="capturePhotosParamsRef.numPhotos"
          class="col-5 q-mb-sm"
          type="text"
          name="numPhotos"
          :label="$t('ItemImageSetEditor.label.numPhotos')"
        />
        <q-checkbox
          v-model="capturePhotosParamsRef.useTurnTable"
          class="full-width q-mb-sm"
          name="useTurntable"
          :label="$t('ItemImageSetEditor.label.turntable')"
        />
        <q-checkbox
          v-model="capturePhotosParamsRef.removeBackgrounds"
          class="full-width"
          name="removeBackgrounds"
          :label="$t('ItemImageSetEditor.label.backgrounds')"
        />
      </q-card-section>
    </template>

    <template v-slot:cancel>
      <q-btn
        color="primary"
        :label="$t('Common.cancel')"
        @click="showCapturePhotosModalRef = false"
      />
    </template>

    <template v-slot:approve>
      <q-btn
        color="primary"
        :label="$t('ItemImageSetEditor.startCapturing')"
        icon="camera"
        @click="capturePhotos"
      />
    </template>
  </artivact-dialog>

  <!-- UPLOAD FILES -->
  <artivact-dialog
    :dialog-model="showUploadFilesModalRef"
    :hide-buttons="true"
    :show-close-button="true"
    @close-dialog="showUploadFilesModalRef = false"
  >
    <template v-slot:header>
      {{ $t('ItemImageSetEditor.dialog.upload.heading') }}
    </template>

    <template v-slot:body>
      <q-card-section>
        <q-uploader
          :url="'/api/item/' + itemId + '/image?uploadOnly=true'"
          :label="$t('ItemImageSetEditor.dialog.upload.label')"
          multiple
          class="full-width q-mt-md q-mb-md"
          accept=".jpg, image/*"
          field-name="file"
          :no-thumbnails="true"
          @finish="createImageSet"
        />
      </q-card-section>
    </template>
  </artivact-dialog>

  <!-- IMAGE-SET DETAILS -->
  <artivact-dialog
    :dialog-model="showImageSetDetailsModalRef"
    v-if="showImageSetDetailsModalRef && selectedImageSet"
    :hide-buttons="true"
    :show-close-button="true"
    :min-width="50"
    @close-dialog="showImageSetDetailsModalRef = false"
  >
    <template v-slot:header>
      {{ $t('ItemImageSetEditor.dialog.details.heading') }}
    </template>

    <template v-slot:body>
      <q-card-section>
        <div class="row q-ml-md">
          <q-card
            v-for="(image, index) in imagePageRef"
            :key="index"
            class="image-set-card q-mr-md q-mb-md"
          >
            <q-img :src="image.url + '?imageSize=ITEM_CARD'">
              <div class="absolute-bottom">
                {{ image.fileName }}
              </div>
            </q-img>
            <q-card-actions>
              <div class="row full-width">
                <q-btn
                  :disable="!image.transferable"
                  icon="move_up"
                  round
                  dense
                  flat
                  size="md"
                  color="primary"
                  @click="transferImageToMedia(image)"
                >
                  <q-tooltip>{{
                    $t('ItemImageSetEditor.dialog.details.transfer')
                  }}</q-tooltip>
                </q-btn>
                <q-space />
                <q-btn
                  icon="delete"
                  round
                  dense
                  flat
                  size="md"
                  color="primary"
                  @click="deleteImageFromImageSet(image)"
                >
                  <q-tooltip>{{
                    $t('ItemImageSetEditor.dialog.details.deleteImage')
                  }}</q-tooltip>
                </q-btn>
              </div>
            </q-card-actions>
          </q-card>
        </div>
        <div class="row justify-center items-center">
          <q-pagination
            class="gt-xs"
            v-model="pageData.cur"
            :max="pageData.max"
            input
            @update:model-value="updateImagesPage(pageData.cur - 1)"
          />
        </div>
      </q-card-section>
    </template>
  </artivact-dialog>

  <!-- LONG-RUNNING OPERATION -->
  <artivact-operation-in-progress-dialog
    v-if="showOperationInProgressModalRef == true"
    :dialog-model="showOperationInProgressModalRef"
    @close-dialog="operationFinished()"
  />

  <!-- DELETE CONFIRMATION DIALOG -->
  <artivact-dialog :dialog-model="confirmDeleteRef" :warn="true">
    <template v-slot:header>
      {{ $t('ItemImageSetEditor.dialog.delete.heading') }}
    </template>

    <template v-slot:body>
      <q-card-section>
        {{ $t('ItemImageSetEditor.dialog.delete.description') }}
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
        :label="$t('ItemImageSetEditor.dialog.delete.approve')"
        color="primary"
        @click="deleteImageSet"
      />
    </template>
  </artivact-dialog>
</template>

<script setup lang="ts">
import { api } from '../boot/axios';
import { Asset, CapturePhotosParams, ImageSet } from './artivact-models';
import { useQuasar } from 'quasar';
import { PropType, ref } from 'vue';
import ArtivactDialog from '../components/ArtivactDialog.vue';
import ArtivactOperationInProgressDialog from '../components/ArtivactOperationInProgressDialog.vue';
import { useI18n } from 'vue-i18n';

const quasar = useQuasar();
const i18n = useI18n();

const props = defineProps({
  itemId: {
    required: true,
    type: String,
  },
  creationImageSets: {
    required: true,
    type: Array as PropType<Array<ImageSet>>,
  },
});

const emit = defineEmits<{
  (e: 'delete-image-set', imageSet: ImageSet): void;
  (e: 'delete-image'): void;
  (e: 'update-item'): void;
  (e: 'save-item'): void;
}>();

const showCapturePhotosModalRef = ref(false);
const capturePhotosParamsRef = ref({
  numPhotos: 36,
  useTurnTable: true,
  removeBackgrounds: true,
} as CapturePhotosParams);

const showImageSetDetailsModalRef = ref(false);
let selectedImageSet: ImageSet;

const showUploadFilesModalRef = ref(false);
const showOperationInProgressModalRef = ref(false);
const showCaptureSinglePhotoModalRef = ref(false);
const showTransferPhotoToMediaModalRef = ref(false);

const tempImageFilenameRef = ref(null);

const confirmDeleteRef = ref(false);
let selectedImageSetIndex = -1;

const pageData = {
  cur: 1,
  max: 0,
  min: 0,
};
const imagePageRef = ref([] as Asset[]);

let currentPage = 0;

function updateImagesPage(page: number) {
  if (page >= 0) {
    currentPage = page;
  }
  let start = currentPage * 9;
  let end = start + 9;
  if (end > selectedImageSet.images.length) {
    end = selectedImageSet.images.length;
  }
  imagePageRef.value = [];
  for (let i = start; i < end; i++) {
    imagePageRef.value.push(selectedImageSet.images[i]);
  }
}

function deleteImageFromImageSet(asset: Asset) {
  selectedImageSet.images.splice(selectedImageSet.images.indexOf(asset), 1);
  updateImagesPage(-1);
  emit('delete-image');
}

function showImageDetails(imageSet: ImageSet) {
  selectedImageSet = imageSet;
  pageData.max =
    imageSet.images.length / 9 + (imageSet.images.length % 9 > 0 ? 1 : 0);
  pageData.cur = 1;
  updateImagesPage(0);
  showImageSetDetailsModalRef.value = true;
}

function captureSinglePhoto() {
  showCaptureSinglePhotoModalRef.value = true;
  api
    .post(
      '/api/item/' + props.itemId + '/media-creation/capture-image',
      capturePhotosParamsRef.value,
    )
    .then((response) => {
      if (response) {
        tempImageFilenameRef.value = response.data;
        showCaptureSinglePhotoModalRef.value = false;
        showTransferPhotoToMediaModalRef.value = true;
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemImageSetEditor.messages.capturingFailed'),
        icon: 'report_problem',
      });
    });
}

function addTempImageToMedia() {
  showTransferPhotoToMediaModalRef.value = false;
  transferImageToMedia({
    fileName: tempImageFilenameRef.value,
    url: '',
    transferable: true,
  } as Asset);
}

function deleteTempImage() {
  showTransferPhotoToMediaModalRef.value = false;
  emit('save-item'); // Removes the not referenced, newly captured image!
}

function capturePhotos() {
  api
    .post(
      '/api/item/' + props.itemId + '/media-creation/capture-images',
      capturePhotosParamsRef.value,
    )
    .then((response) => {
      if (response) {
        showCapturePhotosModalRef.value = false;
        showOperationInProgressModalRef.value = true;
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemImageSetEditor.messages.capturingFailed'),
        icon: 'report_problem',
      });
    });
}

function removeBackgrounds(imageSetIndex: number) {
  api
    .post(
      '/api/item/' +
        props.itemId +
        '/media-creation/remove-backgrounds?imageSetIndex=' +
        imageSetIndex,
    )
    .then((response) => {
      if (response) {
        showOperationInProgressModalRef.value = true;
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemImageSetEditor.messages.backgroundFailed'),
        icon: 'report_problem',
      });
    });
}

function createImageSet() {
  api
    .post('/api/item/' + props.itemId + '/media-creation/create-image-set')
    .then((response) => {
      if (response) {
        showUploadFilesModalRef.value = false;
        showOperationInProgressModalRef.value = true;
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemImageSetEditor.messages.imageSetFailed'),
        icon: 'report_problem',
      });
    });
}

function openImagesDir() {
  api
    .put('/api/item/' + props.itemId + '/media-creation/open-images-dir')
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemImageSetEditor.messages.openingFailed'),
        icon: 'report_problem',
      });
    });
}

function transferImageToMedia(image: Asset) {
  api
    .put('/api/item/' + props.itemId + '/media-creation/transfer-image', image)
    .then((response) => {
      if (response) {
        emit('update-item');
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: i18n.t('ItemImageSetEditor.messages.transferred'),
          icon: 'check',
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemImageSetEditor.messages.transferFailed'),
        icon: 'report_problem',
      });
    });
}

function showDeleteImageSetConfirm(imageSetIndex: number) {
  selectedImageSetIndex = imageSetIndex;
  confirmDeleteRef.value = true;
}

function deleteImageSet() {
  confirmDeleteRef.value = false;
  api
    .delete(
      '/api/item/' +
        props.itemId +
        '/media-creation/image-set/' +
        selectedImageSetIndex,
    )
    .then((response) => {
      if (response) {
        emit('update-item');
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: i18n.t('ItemImageSetEditor.messages.imageSetDeleted'),
          icon: 'check',
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemImageSetEditor.messages.imageSetDeletionFailed'),
        icon: 'report_problem',
      });
    });
}

function toggleModelInput(imageSetIndex: number) {
  api
    .put(
      '/api/item/' +
        props.itemId +
        '/media-creation/image-set/' +
        imageSetIndex +
        '/toggle-model-input',
    )
    .then((response) => {
      if (response) {
        emit('update-item');
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: i18n.t('ItemImageSetEditor.messages.operationSuccess'),
          icon: 'check',
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('ItemImageSetEditor.messages.operationFailed'),
        icon: 'report_problem',
      });
    });
}

function operationFinished() {
  emit('update-item');
  showOperationInProgressModalRef.value = false;
}
</script>

<style scoped>
.image-set-card {
  width: 200px;
}

.image-set-card-img {
  width: 200px;
  height: 200px;
}
</style>
