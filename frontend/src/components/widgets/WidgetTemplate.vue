<template>
  <div
    class="row"
    :class="
      inEditMode && showDetailsRef ? 'widget-editor-separator' : 'widget-editor'
    "
  >
    <!-- Content -->
    <div class="col-grow" v-if="!inEditMode">
      <slot name="widget-content"></slot>
    </div>

    <!-- Editor -->
    <div v-if="inEditMode" class="col-grow">
      <div class="q-mb-lg">
        <q-btn
          rounded
          dense
          color="primary"
          size="md"
          icon="edit"
          class="widget-editor-edit-button"
          @click="showDetailsRef = true"
          v-if="!showDetailsRef">
          <q-tooltip>{{ $t('WidgetTemplate.tooltip.edit') }}</q-tooltip>
        </q-btn>
        <q-btn
          rounded
          dense
          color="primary"
          size="md"
          icon="expand_less"
          class="widget-editor-edit-button"
          @click="showDetailsRef = false"
          v-if="showDetailsRef">
          <q-tooltip>{{ $t('WidgetTemplate.tooltip.close') }}</q-tooltip>
        </q-btn>

        <slot name="widget-editor-preview"></slot>

        <transition>
          <div v-show="showDetailsRef">
            <artivact-content
              :class="showDetailsRef ? 'widget-editor-preview-separator' : ''"
            >
              <artivact-restrictions-editor
                :in-details-view="false"
                :restrictions="restrictions"
                @delete-restriction="deleteRestriction"
                @add-restriction="addRestriction"
              />

              <div>
                <q-btn
                  rounded
                  dense
                  flat
                  class="widget-editor-content"
                  icon="arrow_upward"
                  @click="$emit('move-widget-up')"
                  v-if="moveUpEnabled">
                  <q-tooltip>{{ $t('WidgetTemplate.tooltip.moveUp') }}</q-tooltip>
                </q-btn>
                <q-btn
                  rounded
                  dense
                  flat
                  class="widget-editor-content"
                  icon="arrow_downward"
                  @click="$emit('move-widget-down')"
                  v-if="moveDownEnabled">
                  <q-tooltip>{{ $t('WidgetTemplate.tooltip.moveDown') }}</q-tooltip>
                </q-btn>
                <q-btn
                  dense
                  flat
                  class="widget-editor-content"
                  icon="delete"
                  @click="$emit('delete-widget')">
                  <q-tooltip>{{ $t('WidgetTemplate.tooltip.delete') }}</q-tooltip>
                </q-btn>
              </div>
            </artivact-content>

            <slot name="widget-editor-content"></slot>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { PropType, ref, toRef } from 'vue';
import ArtivactRestrictionsEditor from 'components/ArtivactRestrictionsEditor.vue';
import ArtivactContent from 'components/ArtivactContent.vue';

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
.widget-editor-preview-separator {
  border-top: 1px solid black;
}

.widget-editor {
  border-top: 1px solid transparent;
  border-bottom: 1px solid transparent;
}

.widget-editor-separator {
  border-top: 1px solid black;
  border-bottom: 1px solid black;
}

.widget-editor-edit-button {
  z-index: 2;
  margin-top: 1em;
  right: 1em;
  position: absolute;
}
</style>
