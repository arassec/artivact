<template>
  <ArtivactContent>
      <artivact-card v-for="cardData of data" :key="cardData.title"
                     :artivact-card-data="cardData">
      </artivact-card>
  </ArtivactContent>
</template>

<script lang="ts">

import {defineComponent, ref} from 'vue';
import {useQuasar} from 'quasar';
import {api} from 'boot/axios';
import ArtivactCard from 'components/ArtivactCard.vue';
import ArtivactContent from 'components/ArtivactContent.vue';

export default defineComponent({
  name: 'IndexPage',
  components: {ArtivactContent, ArtivactCard},
  setup() {
    const $q = useQuasar()
    const data = ref([])

    function loadData() {
      api.get('/api/artivact/card')
        .then((response) => {
          data.value = response.data
        })
        .catch(() => {
          $q.notify({
            color: 'negative',
            position: 'bottom',
            message: 'Loading failed',
            icon: 'report_problem'
          })
        })
    }

    return {data, loadData}
  },
  mounted() {
    this.loadData();
  }
});
</script>
