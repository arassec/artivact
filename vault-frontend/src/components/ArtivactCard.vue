<template>
  <q-card class="bg-info q-ma-md">
    <q-img :src="artivactCardData.imageUrl + '?imageSize=CARD'" class="card-image">
      <div class="absolute-bottom text-h6">
        {{ artivactCardData.title ? artivactCardData.title : '?' }}
      </div>
    </q-img>
    <router-link :to="'/artivact/' + artivactCardData.artivactId">
      <q-btn
        dense
        round
        color="primary"
        icon="arrow_forward"
        class="absolute"
        size="lg"
        style="top: 120px; right: 12px; transform: translateY(15%);"
      />
    </router-link>
  </q-card>
</template>

<script lang="ts">
import {ArtivactCardData} from './models';
import {PropType} from 'vue';

export default {
  name: 'ArtivactCard',
  props: {
    artivactCardData: {type: Object as PropType<ArtivactCardData>, required: true}
  }
}
</script>

<style lang="sass" scoped>

.card-image
  width: 300px
  height: 200px

</style>
