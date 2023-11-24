<template>
  <div class="row">
    <q-uploader
      :url="'/api/item/' + itemId + '/image'"
      label="Add Images"
      multiple
      class="uploader q-mb-md col-12"
      accept=".jpg, image/*"
      field-name="file"
      :no-thumbnails="true"
      @uploaded="$emit('uploaded')"
    />
    <draggable :list="imagesRef" item-key="fileName" group="images">
      <template #item="{ element }">
        <span class="q-mr-md">
          <q-img
            :src="element.url + '?imageSize=ITEM_CARD'"
            class="image-media q-mr-sm q-mb-sm"
          ></q-img>
          <q-btn
            icon="delete"
            class="image-media-delete-btn"
            rounded
            dense
            color="primary"
            @click="deleteImage(element)"
          ></q-btn>
        </span>
      </template>
    </draggable>
  </div>
</template>

<script setup lang="ts">
// noinspection ES6UnusedImports
import draggable from 'vuedraggable';
import { PropType, toRef } from 'vue';
import { MediaEntry } from 'components/models';

const props = defineProps({
  itemId: {
    required: true,
    type: String,
  },
  images: {
    required: true,
    type: Object as PropType<MediaEntry[]>,
    default: [] as MediaEntry[],
  },
});

const imagesRef = toRef(props, 'images');

function deleteImage(element: MediaEntry) {
  imagesRef.value.splice(imagesRef.value?.indexOf(element), 1);
}
</script>

<style scoped>
.image-media {
  width: 200px;
  height: 150px;
  border: 1px solid lightgrey;
}

.image-media :hover {
  cursor: move;
  border: 1px solid darkgrey;
}

.image-media > button :hover {
  cursor: pointer;
}

.image-media-delete-btn {
  margin-left: -45px;
  margin-top: -120px;
}
</style>
