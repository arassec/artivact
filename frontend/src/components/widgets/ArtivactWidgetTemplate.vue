<template>
  <div class="row artivact-widget">

    <!-- Content -->
    <div class="col-grow" v-if="!inEditMode">
      <slot name="widget-content"></slot>
    </div>

    <!-- Editor -->
    <div v-if="inEditMode" class="col-grow">
      <div class="q-mb-lg">

        <div class="edit-widget-button-container">
          <q-btn
            rounded
            dense
            color="primary"
            size="md"
            icon="edit"
            class="edit-widget-button"
            @click="showDetailsRef = true">
            <q-tooltip>{{ $t('WidgetTemplate.tooltip.edit') }}</q-tooltip>
          </q-btn>
          <q-btn
            rounded
            dense
            color="primary"
            class="upward-widget-button"
            icon="arrow_upward"
            @click="$emit('move-widget-up')"
            v-if="moveUpEnabled">
          </q-btn>
          <q-btn
            rounded
            dense
            color="primary"
            class="downward-widget-button"
            icon="arrow_downward"
            @click="$emit('move-widget-down')"
            v-if="moveDownEnabled">
          </q-btn>
          <q-btn
            rounded
            dense
            color="primary"
            class="delete-widget-button"
            icon="delete"
            @click="$emit('delete-widget')">
            <q-tooltip>{{ $t('WidgetTemplate.tooltip.delete') }}</q-tooltip>
          </q-btn>
        </div>

        <slot name="widget-content"></slot>

        <artivact-widget-editor-modal :dialog-model="showDetailsRef">
          <artivact-restrictions-editor
            :in-details-view="false"
            :restrictions="restrictions"
            @delete-restriction="deleteRestriction"
            @add-restriction="addRestriction"
          />

          <template v-slot:editor-preview>
            <slot name="widget-editor-preview"></slot>
          </template>

          <template v-slot:editor-content>
            <slot name="widget-editor-content"></slot>
          </template>

          <template v-slot:approve>
            <q-btn
              :label="$t('Common.save')"
              color="primary"
              @click="showDetailsRef = false"
            />
          </template>
        </artivact-widget-editor-modal>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {PropType, ref, toRef} from 'vue';
import ArtivactRestrictionsEditor from 'components/ArtivactRestrictionsEditor.vue';
import ArtivactWidgetEditorModal from 'components/widgets/ArtivactWidgetEditorModal.vue';

const props = defineProps({
  inEditMode: {
    required: true,
    type: Boolean,
  },
  moveUpEnabled: {
    required: true,
    type: Boolean,
  },
  moveDownEnabled: {
    required: true,
    type: Boolean,
  },
  restrictions: {
    required: true,
    type: Array as PropType<string[]>,
  },
});

defineEmits<{
  (e: 'move-widget-up'): void;
  (e: 'move-widget-down'): void;
  (e: 'delete-widget'): void;
}>();

const restrictionsRef = toRef(props, 'restrictions');

const showDetailsRef = ref(false);

function addRestriction(value: string) {
  if (restrictionsRef.value) {
    restrictionsRef.value.push(value);
  }
}

function deleteRestriction(value: string) {
  if (restrictionsRef.value) {
    restrictionsRef.value.forEach((item, index) => {
      if (item === value && restrictionsRef.value) {
        restrictionsRef.value.splice(index, 1);
      }
    });
  }
}
</script>

<style scoped>
.artivact-widget {
  min-height: 4em;
}

.edit-widget-button-container {
  height: 0;
  max-width: 65rem;
  margin-right: auto;
  margin-left: auto;
  position: relative;
}

.edit-widget-button {
  z-index: 1;
  position: absolute;
  right: -5em;
  top: 1em;
}

.upward-widget-button {
  z-index: 1;
  position: absolute;
  right: -8em;
  top: 1em;
}

.downward-widget-button {
  z-index: 1;
  position: absolute;
  right: -11em;
  top: 1em;
}

.delete-widget-button {
  z-index: 1;
  position: absolute;
  right: -14em;
  top: 1em;
}
</style>
