<template>
  <div class="row">
    <div v-for="(imageMedia, index) in imagesRef" :key="index" class="image-media q-mb-xl q-mr-sm">
        <q-img :src="imageMedia.url + '?imageSize=CARD'" class="image-media"/>
        <q-btn v-if="index !== 0" round color="primary" size="xs" icon="chevron_left" class="image-media-button"
               @click="moveLeft(index)"/>
        <q-btn v-if="index !== images.length - 1" class="float-right image-media-button" round color="primary" size="xs"
               icon="chevron_right" @click="moveRight(index)"/>
    </div>
  </div>
</template>

<script setup lang="ts">

import {PropType, toRef} from 'vue';
import {MediaEntry} from 'components/models';

const props = defineProps({
  images: {
    required: true,
    type: Object as PropType<MediaEntry[]>,
    default: [] as MediaEntry[]
  }
});

const imagesRef = toRef(props, 'images');

function moveLeft(index: number) {
  let element = imagesRef.value[index];
  imagesRef.value.splice(index, 1);
  imagesRef.value.splice((index - 1), 0, element);
}

function moveRight(index: number) {
  let element = imagesRef.value[index];
  imagesRef.value.splice(index, 1);
  imagesRef.value.splice((index + 1), 0, element);
}

</script>

<style scoped>
.image-media {
  width: 200px;
  height: 150px;
}

.image-media-button {
  margin: .1rem;
}
</style>
