<template>
  <template v-if="!disabled">
    <router-link :to="'/item/' + artivactCardData.itemId" @click="addAnchorToBreadcrumbs">
      <q-card class="bg-info card">
        <q-img v-if="artivactCardData.imageUrl"
               :src="artivactCardData.imageUrl + '?imageSize=ITEM_CARD'"
               class="card-image"
        >
          <div class="absolute-bottom text-h6" v-if="artivactCardData.title">
            {{
              artivactCardData.title ? translate(artivactCardData.title) : "?"
            }}
          </div>
        </q-img>
        <div class="absolute-bottom text-h6 q-ml-sm" v-if="!artivactCardData.imageUrl && artivactCardData.title">
          {{
            artivactCardData.title ? translate(artivactCardData.title) : "?"
          }}
        </div>
        <router-link :to="'/item/' + artivactCardData.itemId + '?model=true'">
          <q-btn
            v-if="artivactCardData.hasModel"
            dense
            round
            color="primary"
            icon="3d_rotation"
            class="absolute model-indicator"
            size="lg"
          />
        </router-link>
      </q-card>
    </router-link>
  </template>
  <template v-else>
    <q-card class="bg-info card">
      <q-img v-if="artivactCardData.imageUrl"
             :src="artivactCardData.imageUrl + '?imageSize=ITEM_CARD'"
             class="card-image"
      >
        <div class="absolute-bottom text-h6" v-if="artivactCardData.title">
          {{ artivactCardData.title ? translate(artivactCardData.title) : "?" }}
        </div>
      </q-img>
      <div class="absolute-bottom text-h6" v-if="!artivactCardData.imageUrl && artivactCardData.title">
        {{ artivactCardData.title ? translate(artivactCardData.title) : "?" }}
      </div>
    </q-card>
  </template>
</template>

<script setup lang="ts">
import {ItemCardData} from './artivact-models';
import {PropType} from 'vue';
import {translate} from './artivact-utils';
import {useBreadcrumbsStore} from "../stores/breadcrumbs";

const props = defineProps({
  artivactCardData: {
    type: Object as PropType<ItemCardData>,
    required: true,
  },
  disabled: {
    type: Boolean,
    required: false,
    default: false,
  },
  widgetId: {
    type: String,
    required: false,
    default: false
  }
});

const breadcrumbsStore = useBreadcrumbsStore();

function addAnchorToBreadcrumbs() {
  if (props.widgetId) {
    breadcrumbsStore.addAnchor(props.widgetId)
  }
}

</script>

<style lang="sass" scoped>

.card
  width: 300px
  height: 200px

.card-image
  width: 300px
  height: 200px

.model-indicator
  top: 120px
  right: 12px
  transform: translateY(15%)
</style>
