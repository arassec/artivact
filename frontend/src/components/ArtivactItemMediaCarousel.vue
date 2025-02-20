<template>
  <div v-if="itemDetails" class="row">
    <q-carousel
      v-show="showImagesRef"
      v-model="slide"
      v-model:fullscreen="fullscreen"
      animated
      infinite
      padding
      navigation
      control-color="white"
      control-type="flat"
      class="artivact-carousel bg-info shadow-2 rounded-borders col-12"
    >
      <q-carousel-slide
        draggable="false"
        v-for="(mediaEntry, index) of itemDetailsRef.images"
        :key="index"
        :name="index"
        :img-src="mediaEntry.url + '?imageSize=DETAIL'"
      >
        <q-btn
          round
          dense
          flat
          class="absolute-top-right fullscreen-button"
          color="white"
          :icon="fullscreen ? 'fullscreen_exit' : 'fullscreen'"
          @click="fullscreen = !fullscreen"
        />
      </q-carousel-slide>
    </q-carousel>

    <q-carousel
      class="artivact-carousel bg-info shadow-2 rounded-borders col-12"
      v-if="itemDetailsRef.models.length > 0"
      v-show="!showImagesRef"
      v-model="modelSlide"
      v-model:fullscreen="fullscreen"
      animated
      infinite
      padding
      navigation
      control-color="white"
      control-type="flat"
    >
      <q-carousel-slide
        draggable="false"
        v-for="(model, index) of itemDetailsRef.models"
        :key="index"
        :name="index">
        <artivact-item-model-viewer :model-url="model.url"/>
        <q-btn
          round
          dense
          flat
          class="absolute-top-right fullscreen-button"
          color="white"
          :icon="fullscreen ? 'fullscreen_exit' : 'fullscreen'"
          @click="fullscreen = !fullscreen"
        />
      </q-carousel-slide>
    </q-carousel>

    <div class="row q-mt-sm q-mb-sm col-12 justify-center items-center"
         v-if="itemDetailsRef.images.length > 0 || itemDetailsRef.models.length > 0">
      <div v-if="applicationSettings.license &&
              applicationSettings.license.licenseLabel &&
              applicationSettings.license.licenseLabel.translatedValue">
        {{ applicationSettings.license.prefix.translatedValue }}
        <a :href="applicationSettings.license.licenseUrl">{{
            applicationSettings.license.licenseLabel.translatedValue
          }}</a>
        {{ applicationSettings.license.suffix.translatedValue }}
      </div>
    </div>

    <div class="row col-12 justify-center items-center"
         v-if="itemDetailsRef.images.length > 0 || itemDetailsRef.models.length > 0">
      <q-btn
        v-if="itemDetailsRef.images.length > 0"
        icon="image"
        round
        dense
        color="primary"
        @click="showImagesRef = true"
        class="q-mr-sm">
        <q-tooltip>{{ $t('ItemMediaCarousel.tooltip.image') }}</q-tooltip>
      </q-btn>
      <q-btn
        v-if="itemDetailsRef.models.length > 0"
        icon="3d_rotation"
        round
        dense
        color="primary"
        @click="showImagesRef = false"
        class="q-mr-sm">
        <q-tooltip>{{ $t('ItemMediaCarousel.tooltip.model') }}</q-tooltip>
      </q-btn>
    </div>

  </div>
</template>

<script setup lang="ts">
import { PropType, ref, toRef } from 'vue';
import { ItemDetails } from 'components/artivact-models';
import ArtivactItemModelViewer from 'components/ArtivactItemModelViewer.vue';
import { useApplicationSettingsStore } from 'stores/application-settings';

const props = defineProps({
  itemDetails: {
    required: true,
    type: Object as PropType<ItemDetails>,
    default: {} as ItemDetails,
  },
  showImages: {
    required: false,
    type: Boolean,
    default: true,
  },
});

const applicationSettings = useApplicationSettingsStore();

const itemDetailsRef = toRef(props, 'itemDetails');

const fullscreen = ref(false);

let slide = ref(0);
let modelSlide = ref(0);
let showImages = props.showImages;
let showImagesRef = toRef(showImages);

</script>

<style scoped>
.fullscreen-button {
  z-index: 2;
  margin: 15px 15px 0 0;
}

.artivact-carousel {
  min-height: 500px;
}

</style>
