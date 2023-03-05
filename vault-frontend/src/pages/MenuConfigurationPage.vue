<template>
  <ArtivactContent>
    <div class="col">
      <h1 class="av-text-h1">Menu Configuration</h1>
      <div class="q-mb-lg">The navigation bar's central main menu is configured on this page. Each menu can have several
        entries that point to pages. The menus themselves can be ordered by drag & drop.
      </div>

      <artivact-menu-configuration-editor :locales="localesRef" :menu-configuration="menuConfiguration"
                                          v-if="menuConfiguration"/>

      <q-separator class="q-mt-md q-mb-md"/>

      <q-btn label="Save" color="primary" class="float-right q-mb-lg" @click="saveMenu()"/>
    </div>
  </ArtivactContent>
</template>

<script>
import {useQuasar} from 'quasar';
import {onMounted, ref} from 'vue';
import {api} from 'boot/axios';
import ArtivactContent from 'components/ArtivactContent.vue';
import ArtivactMenuConfigurationEditor from 'components/ArtivactMenuConfigurationEditor.vue';
import {useMenuStore} from 'stores/menu';

export default {
  name: 'PropertiesConfigurationEditPage',
  components: {ArtivactMenuConfigurationEditor, ArtivactContent},
  setup() {
    const $q = useQuasar()
    const menuConfigurationRef = ref(null)
    const localesRef = ref([]);
    const menuStore = useMenuStore();

    let json = {};

    function loadMenuConfiguration() {
      api.get('/api/administration/menu')
        .then((response) => {
          json = response.data;
          menuConfigurationRef.value = json;
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
      loadMenuConfiguration();
    })

    return {
      menuConfiguration: ref(menuConfigurationRef),
      localesRef,

      saveMenu() {
        api.post('/api/administration/menu', json)
          .then(() => {
            api.get('/api/configuration/menu')
              .then((response) => {
                menuStore.setAvailableMenus(response.data);
              })
              .catch(() => {
                $q.notify({
                  color: 'negative',
                  position: 'bottom',
                  message: 'Loading menus failed',
                  icon: 'report_problem'
                })
              })
            $q.notify({
              color: 'positive',
              position: 'bottom',
              message: 'Menu saved',
              icon: 'check'
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
