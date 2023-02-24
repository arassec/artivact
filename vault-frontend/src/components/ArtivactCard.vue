<template>
  <router-link :to="'/artivact/' + artivactCardData.artivactId">
    <q-card class="bg-info q-ma-md">
      <q-img :src="artivactCardData.imageUrl + '?imageSize=CARD'" class="card-image">
        <div class="absolute-bottom text-h6" v-if="artivactCardData.title">
          {{ artivactCardData.title ? artivactCardData.title : '?' }}
        </div>
      </q-img>
    </q-card>
  </router-link>
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
