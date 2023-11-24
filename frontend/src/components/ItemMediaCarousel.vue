<template>
  <div v-if="itemDetails">
    <div class="row">
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
        class="artivact-carousel bg-info shadow-2 rounded-borders col-xs-12 col-sm-8 col-md-8 col-lg-8 col-xl-8"
      >
        <q-carousel-slide
          draggable="false"
          v-for="(mediaEntry, index) of itemDetailsRef.images"
          :key="index"
          :name="index"
          :img-src="mediaEntry.url + '?imageSize=CAROUSEL'"
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

      <div
        class="artivact-carousel bg-info shadow-2 rounded-borders col-xs-12 col-sm-8 col-md-8 col-lg-8 col-xl-8"
        v-if="itemDetailsRef.models.length > 0"
        v-show="!showImagesRef"
      >
        <item-model-viewer :model-url="getModelUrl()" />
      </div>

      <div class="col-4 xs-hide">
        <div class="q-ml-md column full-height">
          <div class="av-label-h2 col-2">
            Details
            <q-separator class="q-mr-xl" />
          </div>

          <div style="margin-top: -1rem" class="q-mb-md">
            <q-badge
              class="q-mr-xs vertical-middle"
              color="secondary"
              v-for="(tag, index) in itemDetailsRef.tags"
              :key="index"
            >
              <template v-if="tag.url">
                <a :href="tag.url" class="tag-link">{{
                  tag.translatedValue
                }}</a>
              </template>
              <template v-else>
                {{ tag.translatedValue }}
              </template>
            </q-badge>
          </div>

          <div class="col-grow">
            <label v-if="itemDetailsRef.description.translatedValue">
              {{ itemDetailsRef.description.translatedValue }}
            </label>
          </div>
          <q-form
            :action="'/api/item/' + itemDetailsRef.id + '/media'"
            method="get"
            class="q-mt-lg col-shrink"
          >
            <div
              v-if="
                licenseStore.licenseConfiguration &&
                licenseStore.licenseConfiguration.licenseLabel &&
                licenseStore.licenseConfiguration.licenseLabel.translatedValue
              "
            >
              {{ licenseStore.licenseConfiguration.prefix.translatedValue }}
              <a :href="licenseStore.licenseConfiguration.licenseUrl">{{
                licenseStore.licenseConfiguration.licenseLabel.translatedValue
              }}</a>
              {{ licenseStore.licenseConfiguration.suffix.translatedValue }}
            </div>
            <q-btn type="submit" icon="download" color="primary" class="q-mt-xs"
              >{{ $t("downloadMediaFilesButtonLabel") }}
            </q-btn>
          </q-form>
        </div>
      </div>
    </div>

    <div class="row q-mt-sm" v-if="itemDetailsRef.models.length > 0">
      <div class="col-xs-12 col-sm-8 col-md-8 col-lg-8 col-xl-8">
        <div class="row justify-center items-center">
          <q-btn
            icon="image"
            round
            dense
            color="primary"
            @click="showImagesRef = true"
            class="q-mr-sm"
          />
          <q-btn
            icon="3d_rotation"
            round
            dense
            color="primary"
            @click="showImagesRef = false"
            v-if="itemDetailsRef.models.length > 0"
          />
        </div>
      </div>
    </div>

    <div class="xs">
      <div class="q-mt-lg">
        <q-badge
          class="q-mr-xs vertical-middle"
          color="secondary"
          v-for="(tag, index) in itemDetailsRef.tags"
          :key="index"
          >{{ tag.translatedValue }}
        </q-badge>
      </div>
      <div v-if="itemDetailsRef.description.translatedValue" class="q-mt-lg">
        <label>
          {{ itemDetailsRef.description.translatedValue }}
        </label>
      </div>
      <q-form
        :action="'/api/item/' + itemDetailsRef.id + '/media'"
        method="get"
        class="q-mt-lg"
      >
        <div
          v-if="
            licenseStore.licenseConfiguration &&
            licenseStore.licenseConfiguration.licenseLabel &&
            licenseStore.licenseConfiguration.licenseLabel.translatedValue
          "
        >
          {{ licenseStore.licenseConfiguration.prefix.translatedValue }}
          <a :href="licenseStore.licenseConfiguration.licenseUrl">{{
            licenseStore.licenseConfiguration.licenseLabel.translatedValue
          }}</a>
          {{ licenseStore.licenseConfiguration.suffix.translatedValue }}
        </div>
        <q-btn type="submit" icon="download" color="primary" class="q-mt-xs"
          >{{ $t("downloadMediaFilesButtonLabel") }}
        </q-btn>
      </q-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { PropType, ref, toRef } from 'vue';
import { ItemDetails } from 'components/models';
import { useLicenseStore } from 'stores/license';
import ItemModelViewer from 'components/ItemModelViewer.vue';

const licenseStore = useLicenseStore();

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

const itemDetailsRef = toRef(props, 'itemDetails');

const fullscreen = ref(false);

let slide = ref(0);
let showImagesRef = toRef(props, 'showImages');

function getModelUrl(): string {
  if (itemDetailsRef.value.models.length > 0) {
    return itemDetailsRef.value.models[0].url;
  }
  return '';
}
</script>

<style scoped>
.fullscreen-button {
  z-index: 2;
  margin: 15px 15px 0 0;
}

.artivact-carousel {
  min-height: 500px;
}

.tag-link {
  text-decoration: none;
  color: white;
}
</style>
