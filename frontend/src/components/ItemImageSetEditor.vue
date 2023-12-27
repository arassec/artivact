<template>

  <q-btn
    text-color="primary"
    class="q-mr-md"
    round
    dense
    color="accent"
    icon="camera"
    @click="showCapturePhotosModalRef = true"
  />
  <q-btn
    text-color="primary"
    class="q-mr-md"
    round
    dense
    color="accent"
    icon="folder"
    @click="openImagesDir()"
  />
  <q-btn
    text-color="primary"
    round
    dense
    color="accent"
    icon="upload_file"
    @click="showUploadFilesModalRef = true"
  />

  <div class="row">
    <q-card v-for="(imageSet, index) in creationImageSets" :key="index" class="image-set-card q-mr-md q-mt-md">
      <q-img :src="imageSet.images[0] ? imageSet.images[0].url + '?imageSize=ITEM_CARD' : ''"
             class="image-set-card-img">
        <div class="absolute-bottom">
          <div class="text-h6">{{ imageSet.images.length }} {{ imageSet.images.length == 1 ? 'Image' : 'Images' }}</div>
        </div>
      </q-img>
      <q-card-section class="full-width q-pl-none q-pr-none q-pb-none">
        <div class="full-width q-ma-none">
          <q-toggle flat v-model="imageSet.modelInput" @click="toggleModelInput(index)">Model input</q-toggle>
          <q-toggle flat v-model="imageSet.backgroundRemoved" :disable="true">Background removed</q-toggle>
          <q-separator class="q-mt-sm q-mb-sm"/>
        </div>
      </q-card-section>
      <q-card-actions>
        <q-btn
          icon="search"
          round
          dense
          flat
          size="md"
          color="primary"
          @click="showImageDetails(imageSet)"
        />
        <q-btn
          icon="content_cut"
          round
          dense
          flat
          size="md"
          color="primary"
          @click="removeBackgrounds(index)"
          v-if="!imageSet.backgroundRemoved"
        />
        <q-space/>
        <q-btn
          icon="delete"
          round
          dense
          flat
          size="md"
          color="primary"
          @click="showDeleteImageSetConfirm(index)"
        />
      </q-card-actions>
    </q-card>
  </div>

  <!-- CAPTURE PHOTOS -->
  <q-dialog v-model="showCapturePhotosModalRef" persistent>
    <q-card class="artivact-fixed-modal-content">
      <q-card-section class="bg-primary text-white">
        <div class="row">
          <div class="text-h6">Photo-Capture Parameters</div>
          <q-space/>
          <q-btn
            icon="close"
            flat
            round
            dense
            @click="showCapturePhotosModalRef = false"
          />
        </div>
      </q-card-section>
      <q-card-section>
        <q-input
          outlined
          v-model="capturePhotosParamsRef.numPhotos"
          class="col-5"
          type="text"
          name="numPhotos"
          label="Number of Photos"
        />
      </q-card-section>
      <q-card-section>
        <q-checkbox
          v-model="capturePhotosParamsRef.useTurnTable"
          class="col-5"
          name="useTurntable"
          label="Use Turntable?"
        />
        <q-input
          outlined
          v-model="capturePhotosParamsRef.turnTableDelay"
          class="col-5"
          type="text"
          name="turntableDelay"
          label="Turntable Delay"
        />
      </q-card-section>
      <q-card-section>
        <q-checkbox
          v-model="capturePhotosParamsRef.removeBackgrounds"
          class="col-5"
          name="removeBackgrounds"
          label="Remove Image Backgrounds?"
        />
      </q-card-section>
      <q-card-actions>
        <div class="row full-width">
          <q-space/>
          <q-btn label="Start Capturing" icon="camera" @click="capturePhotos"/>
        </div>
      </q-card-actions>
    </q-card>
  </q-dialog>

  <!-- UPLOAD FILES -->
  <q-dialog v-model="showUploadFilesModalRef" persistent>
    <q-card class="artivact-fixed-modal-content">
      <q-card-section class="bg-primary text-white">
        <div class="row">
          <div class="text-h6">Upload Files to new Image-Set</div>
          <q-space/>
          <q-btn
            icon="close"
            flat
            round
            dense
            @click="showUploadFilesModalRef = false"
          />
        </div>
      </q-card-section>
      <q-card-section>
        <q-uploader
          :url="'/api/item/' + itemId + '/image?uploadOnly=true'"
          label="Add Images"
          multiple
          class="uploader q-mb-md col-12"
          accept=".jpg, image/*"
          field-name="file"
          :no-thumbnails="true"
          @finish="createImageSet"
        />
      </q-card-section>
    </q-card>
  </q-dialog>

  <!-- IMAGE-SET DETAILS -->
  <q-dialog v-model="showImageSetDetailsModalRef" persistent v-if="showImageSetDetailsModalRef && selectedImageSet">
    <q-card class="q-mb-lg image-set-modal">
      <q-card-section class="bg-primary text-white">
        <div class="row">
          <div class="text-h6">Image-Set Details</div>
          <q-space/>
          <q-btn
            icon="close"
            flat
            round
            dense
            @click="showImageSetDetailsModalRef = false"
          />
        </div>
      </q-card-section>
      <q-card-section>
        <div class="row">
          <q-card v-for="(image, index) in selectedImageSet.images" :key="index" class="image-set-card q-mr-md q-mb-md">
            <q-img :src="image.url + '?imageSize=ITEM_CARD'">
              <div class="absolute-bottom">
                <div class="text-h6">{{ image.fileName }}</div>
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
                />
                <q-space/>
                <q-btn
                  icon="delete"
                  round
                  dense
                  flat
                  size="md"
                  color="primary"
                  @click="$emit('delete-image', selectedImageSet, image)"
                />
              </div>
            </q-card-actions>
          </q-card>
        </div>
      </q-card-section>
    </q-card>
  </q-dialog>

  <!-- LONG-RUNNING OPERATION -->
  <q-dialog v-model="showOperationInProgressModalRef" persistent>
    <q-card class="q-mb-lg image-set-modal">
      <q-card-section class="bg-primary text-white">
        <div class="text-h6">Operation in Progress</div>
      </q-card-section>
      <q-card-section>
        {{ progressMonitorRef?.progress }}
      </q-card-section>
    </q-card>
  </q-dialog>

  <!-- DELETE CONFIRMATION DIALOG -->
  <q-dialog v-model="confirmDeleteRef" persistent>
    <q-card>
      <q-card-section class="row items-center">
        <q-icon
          name="warning"
          size="md"
          color="warning"
          class="q-mr-md"
        ></q-icon>
        <h3 class="av-text-h3">Delete Image-Set?</h3>
        <div class="q-ml-sm">
          Are you sure you want to delete this Image-Set and all its files?
          This action cannot be undone!
        </div>
      </q-card-section>
      <q-card-section>
        <q-btn flat label="Cancel" color="primary" v-close-popup/>
        <q-btn
          flat
          label="Delete Image-Set"
          color="primary"
          v-close-popup
          @click="deleteImageSet"
          class="float-right"
        />
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">

import {api} from 'boot/axios';
import {Asset, CapturePhotosParams, ImageSet, OperationProgress} from 'components/models';
import {useQuasar} from 'quasar';
import {PropType, ref} from 'vue';

const quasar = useQuasar();

const props = defineProps({
  itemId: {
    required: true,
    type: String,
  },
  creationImageSets: {
    required: true,
    type: Array as PropType<Array<ImageSet>>,
  }
});

const emit = defineEmits<{
  (e: 'delete-image-set', imageSet: ImageSet): void;
  (e: 'delete-image', imageSet: ImageSet, asset: Asset): void;
  (e: 'update-item'): void;
}>();

const showCapturePhotosModalRef = ref(false);
const capturePhotosParamsRef = ref({
  numPhotos: 36,
  useTurnTable: true,
  turnTableDelay: 100,
  removeBackgrounds: true
} as CapturePhotosParams);

const showImageSetDetailsModalRef = ref(false);
let selectedImageSet;

const showUploadFilesModalRef = ref(false);
const showOperationInProgressModalRef = ref(false);

const progressMonitorRef = ref<OperationProgress>();

const confirmDeleteRef = ref(false);
let selectedImageSetIndex = -1;

function showImageDetails(imageSet: ImageSet) {
  selectedImageSet = imageSet;
  showImageSetDetailsModalRef.value = true;
}

function capturePhotos() {
  api
    .post('/api/media-creation/' + props.itemId + '/capture-photos', capturePhotosParamsRef.value)
    .then((response) => {
      if (response) {
        showCapturePhotosModalRef.value = false;
        showOperationInProgressModalRef.value = true;
        progressMonitorRef.value = response.data;
        updateOperationProgress();
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Capturing photos failed!',
        icon: 'report_problem',
      });
    });
}

function removeBackgrounds(imageSetIndex: number) {
  api
    .post('/api/media-creation/' + props.itemId + '/remove-backgrounds?imageSetIndex=' + imageSetIndex)
    .then((response) => {
      if (response) {
        showOperationInProgressModalRef.value = true;
        progressMonitorRef.value = response.data;
        updateOperationProgress();
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Background removal failed!',
        icon: 'report_problem',
      });
    });
}

function createImageSet() {
  api
    .post('/api/media-creation/' + props.itemId + '/create-image-set')
    .then((response) => {
      if (response) {
        showUploadFilesModalRef.value = false;
        showOperationInProgressModalRef.value = true;
        progressMonitorRef.value = response.data;
        updateOperationProgress();
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Background removal failed!',
        icon: 'report_problem',
      });
    });
}

function openImagesDir() {
  api
    .put('/api/media-creation/' + props.itemId + '/open-images-dir')
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Opening directory failed!',
        icon: 'report_problem',
      });
    });
}

function transferImageToMedia(image: Asset) {
  api
    .put('/api/media-creation/' + props.itemId + '/transfer-image', image)
    .then((response) => {
      if (response) {
        emit('update-item');
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: 'Image transferred!',
          icon: 'check',
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Transfer failed!',
        icon: 'report_problem',
      });
    });
}

function showDeleteImageSetConfirm(imageSetIndex: number) {
  selectedImageSetIndex = imageSetIndex;
  confirmDeleteRef.value = true;
}

function deleteImageSet() {
  api
    .delete('/api/media-creation/' + props.itemId + '/image-set/' + selectedImageSetIndex)
    .then((response) => {
      if (response) {
        emit('update-item');
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: 'Image set deleted!',
          icon: 'check',
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Deletion failed!',
        icon: 'report_problem',
      });
    });
}

function toggleModelInput(imageSetIndex: number) {
  api
    .put('/api/media-creation/' + props.itemId + '/image-set/' + imageSetIndex + '/toggle-model-input')
    .then((response) => {
      if (response) {
        emit('update-item');
        quasar.notify({
          color: 'positive',
          position: 'bottom',
          message: 'Item saved!',
          icon: 'check',
        });
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Operation failed!',
        icon: 'report_problem',
      });
    });
}

function updateOperationProgress() {
  api
    .get('/api/media-creation/progress')
    .then((response) => {
      if (response.data) {
        progressMonitorRef.value = response.data;
        setTimeout(() => updateOperationProgress(), 1000);
      } else {
        progressMonitorRef.value = undefined;
        showOperationInProgressModalRef.value = false;
        emit('update-item');
      }
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Background removal failed!',
        icon: 'report_problem',
      });
    });
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

.image-set-modal {
  min-width: 70em;
}

</style>
