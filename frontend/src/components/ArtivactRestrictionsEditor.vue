<template>
  <div
    :class="inDetailsViewProp ? 'q-ml-sm row' : 'row'"
    v-if="restrictionsProp"
  >
    <div class="editor-label q-mt-xs">
      <p
        class="q-mr-xs vertical-middle badge-container-label editor-label"
        :class="inDetailsViewProp ? ' text-grey' : ''"
      >
        {{ $t('ArtivactRestrictionsEditor.restrictions') }}
      </p>
    </div>

    <div class="q-ml-sm">
      <q-toggle
        :label="$t('ROLE_ADMIN')"
        v-model="adminRef"
        size="xs"
        class="first-role q-mr-sm"
        @click="
          adminRef
            ? $emit('add-restriction', 'ROLE_ADMIN')
            : $emit('delete-restriction', 'ROLE_ADMIN')
        "
      />
      <q-toggle
        :label="$t('ROLE_USER')"
        v-model="userRef"
        size="xs"
        @click="
          userRef
            ? $emit('add-restriction', 'ROLE_USER')
            : $emit('delete-restriction', 'ROLE_USER')
        "
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {PropType, ref, toRef} from 'vue';

const props = defineProps({
  restrictions: {
    required: true,
    type: Array as PropType<string[]>,
  },
  inDetailsView: {
    required: false,
    type: Boolean,
    default: false,
  },
});
const restrictionsProp = toRef(props, 'restrictions');
const inDetailsViewProp = toRef(props, 'inDetailsView');

const adminRef = ref(restrictionsProp.value.includes('ROLE_ADMIN'));
const userRef = ref(restrictionsProp.value.includes('ROLE_USER'));
</script>

<style scoped>
.first-role {
  margin-left: -0.5em;
}

.badge-container-label {
  min-width: 6em;
  display: inline-block;
}
</style>
