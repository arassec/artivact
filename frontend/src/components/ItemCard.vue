<template>
  <template v-if="!disabled">
    <router-link :to="'/item/' + artivactCardData.itemId">
      <q-card class="bg-info card">
        <q-img
          :src="artivactCardData.imageUrl + '?imageSize=ITEM_CARD'"
          class="card-image"
        >
          <div class="absolute-bottom text-h6" v-if="artivactCardData.title">
            {{
              artivactCardData.title ? translate(artivactCardData.title) : "?"
            }}
          </div>
        </q-img>
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
      <q-img
        :src="artivactCardData.imageUrl + '?imageSize=ITEM_CARD'"
        class="card-image"
      >
        <div class="absolute-bottom text-h6" v-if="artivactCardData.title">
          {{ artivactCardData.title ? translate(artivactCardData.title) : "?" }}
        </div>
      </q-img>
    </q-card>
  </template>
</template>

<script setup lang="ts">
import { ItemCardData } from './models';
import { PropType } from 'vue';
import { translate } from './utils';

defineProps({
  artivactCardData: {
    type: Object as PropType<ItemCardData>,
    required: true,
  },
  disabled: {
    type: Boolean,
    required: false,
    default: false,
  },
});
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
