<template>
  <div :class="inDetailsViewProp ? 'q-ml-sm' : ''" v-if="restrictionsProp">
    <label class="q-mr-xs q-mt-xs vertical-middle badge-container-label" :class="inDetailsViewProp ? ' text-grey' : ''">Restrictions:</label>
    <q-badge class="q-mr-xs vertical-middle" color="secondary" v-for="value in restrictionsProp" :key="value">
      {{ value }}
      <q-btn rounded dense flat color="primary" size="xs" icon="close" @click="$emit('delete-restriction', value)"/>
    </q-badge>
    <q-btn class=" vertical-middle" round dense unelevated color="secondary" size="xs" icon="add"
           @click="openAddRestrictionsDialog()"/>
    <q-dialog v-model="showAddRestrictionDialog" persistent transition-show="scale" transition-hide="scale"
              class=" justify-center">
      <q-card>
        <q-card-section>
          Add Restriction
        </q-card-section>
        <q-card-section class="column-lg q-ma-md items-center">
          <q-select v-model="selectedRestriction" autofocus :options="rolesRef" label="Role"/>
        </q-card-section>
        <q-card-actions>
          <q-btn flat label="Cancel" v-close-popup/>
          <q-space/>
          <q-btn flat label="Save" v-close-popup @click="$emit('add-restriction', selectedRestriction)"/>
        </q-card-actions>
      </q-card>
    </q-dialog>
  </div>

</template>

<script setup lang="ts">

import {ref, toRef} from 'vue';
import {useRoleStore} from 'stores/role';

const props = defineProps({
  restrictions: {
    required: true,
    type: []
  },
  inDetailsView: {
    required: true,
    type: Boolean,
    default: false
  }
});
const restrictionsProp = toRef(props, 'restrictions');
const inDetailsViewProp = toRef(props, 'inDetailsView');

const roleStore = useRoleStore();

const rolesRef = ref(roleStore.availableRoles)

const showAddRestrictionDialog = ref(false);
const selectedRestriction = ref('');

function openAddRestrictionsDialog() {
  rolesRef.value = roleStore.availableRoles;
  restrictionsProp.value?.forEach((restriction) => {
    const index = rolesRef.value.indexOf(restriction);
    if (index > -1) {
      rolesRef.value.splice(index, 1);
    }
  })
  showAddRestrictionDialog.value = true;
}
</script>

<style scoped>

</style>
