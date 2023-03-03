<template>
  <ArtivactContent>
    <div class="col">
      <h1 class="av-text-h1">Properties Configuration</h1>

      <artivact-properties-configuration-editor :properties-configuration="propertiesConfiguration"
                                                :locales="localesRef" v-if="propertiesConfiguration"/>

      <q-separator class="q-mt-md q-mb-md"/>

      <q-btn label="Save" color="primary" class="float-right q-mb-lg" @click="saveProperties()"/>
    </div>
  </ArtivactContent>
</template>

<script>
import ArtivactPropertiesConfigurationEditor from '../components/ArtivactPropertiesConfigurationEditor';
import {useQuasar} from 'quasar';
import {onMounted, ref} from 'vue';
import {api} from 'boot/axios';
import ArtivactContent from 'components/ArtivactContent.vue';

export default {
  name: 'PropertiesConfigurationEditPage',
  components: {ArtivactContent, ArtivactPropertiesConfigurationEditor},
  setup() {
    const $q = useQuasar()
    const propertiesConfigurationRef = ref(null)

    const localesRef = ref([]);

    let json = {};

    function loadPropertyConfiguration() {
      api.get('/api/administration/property')
        .then((response) => {
          json = response.data;
          propertiesConfigurationRef.value = json;
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

    function loadLocales() {
      api.get('/api/configuration/locale')
        .then((response) => {
          localesRef.value = response.data
        })
        .catch(() => {
          $q.notify({
            color: 'negative',
            position: 'bottom',
            message: 'Loading locales failed',
            icon: 'report_problem'
          })
        })
    }

    onMounted(() => {
      loadLocales();
      loadPropertyConfiguration();
    })

    return {
      propertiesConfiguration: ref(propertiesConfigurationRef),
      localesRef,

      saveProperties() {
        api.post('/api/administration/property', json)
          .then(() => {
            $q.notify({
              color: 'positive',
              position: 'bottom',
              message: 'Configuration saved',
              icon: 'report'
            })
          })
          .catch(() => {
            $q.notify({
              color: 'negative',
              position: 'bottom',
              message: 'Saving failed',
              icon: 'report_problem'
            })
          })
      }
    }
  }
}
</script>

<style scoped>
</style>
