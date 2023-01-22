<template>
  <q-page class="row items-center justify-evenly">

    <artivact-card v-for="cardData of data" :key="cardData.title"
                  :artivact-card-data="cardData">
    </artivact-card>

  </q-page>
</template>

<script lang="ts">

import {defineComponent, ref} from 'vue';
import {useQuasar} from 'quasar';
import {api} from 'boot/axios';
import ArtivactCard from 'components/ArtivactCard.vue';

export default defineComponent({
  name: 'IndexPage',
  components: {ArtivactCard},
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
            position: 'top',
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
