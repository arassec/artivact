<template>
  <!--suppress VueUnrecognizedDirective -->
  <artivact-content
    :class="inEditMode ? 'widget-editor' : ''"
    @click="inEditMode && $emit('start-editing')"
    v-click-outside="handleClickOutside"
  >
    <slot v-if="!editingRef" name="widget-content"></slot>

    <template v-if="editingRef">
      <div class="full-width with-background-color q-pa-sm">
      <artivact-restrictions-editor
        v-if="userDataStore.isAdmin"
        :in-details-view="false"
        :restrictions="restrictions"
        @delete-restriction="deleteRestriction"
        @add-restriction="addRestriction"
      />
      <div class="full-width">
        <artivact-restricted-translatable-item-editor
          :locales="localStore.locales"
          :label="$t('Widget.label.navigationTitle')"
          :translatable-string="navigationTitle"
          :show-separator="false"
        />
      </div>
      <slot name="widget-editor"></slot>
      </div>
    </template>

    <q-btn
      v-if="inEditMode && !editingRef"
      rounded
      dense
      icon="drag_indicator"
      class="widget-button move-widget-button"
      color="secondary"
      @click.stop
    />

    <q-btn
      v-if="inEditMode && !editingRef"
      rounded
      dense
      icon="delete"
      class="widget-button delete-widget-button"
      @click.stop="$emit('delete-widget')"
      color="secondary"
    />

    <q-btn
      v-if="inEditMode && !editingRef"
      rounded
      dense
      icon="add"
      class="widget-button add-widget-button"
      @click.stop="$emit('add-widget-below')"
      color="secondary"
    />
  </artivact-content>
</template>

<script setup lang="ts">
import {PropType, toRef} from 'vue';
import ArtivactContent from '../../components/ArtivactContent.vue';
import {TranslatableString} from '../artivact-models';
import {useLocaleStore} from '../../stores/locale';
import ArtivactRestrictionsEditor from '../ArtivactRestrictionsEditor.vue';
import ArtivactRestrictedTranslatableItemEditor from '../ArtivactRestrictedTranslatableItemEditor.vue';
import {useUserdataStore} from '../../stores/userdata';

const props = defineProps({
  inEditMode: {
    required: true,
    type: Boolean,
  },
  editing: {
    required: true,
    type: Boolean,
  },
  restrictions: {
    required: true,
    type: Array as PropType<string[]>,
  },
  navigationTitle: {
    required: true,
    type: Object as PropType<TranslatableString>,
  },
});

const emit = defineEmits<{
  (e: 'add-widget-below'): void;
  (e: 'delete-widget'): void;
  (e: 'start-editing'): void;
  (e: 'stop-editing'): void;
}>();

const localStore = useLocaleStore();
const userDataStore = useUserdataStore();

const restrictionsRef = toRef(props, 'restrictions');
const inEditModeRef = toRef(props, 'inEditMode');
const editingRef = toRef(props, 'editing');

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

function handleClickOutside() {
  if (inEditModeRef.value && editingRef.value) {
    emit('stop-editing');
  }
}
</script>

<style scoped>
.widget-editor {
  position: relative;
  min-height: 4em;
}

.widget-editor:hover {
  cursor: text;
}

.widget-button {
  position: absolute;
  opacity: 0;
  transition: opacity 0.5s ease;
  z-index: 2;
}

.widget-editor:hover .widget-button {
  opacity: 1;
}

.move-widget-button {
  top: 10px;
  left: -15px;
}

.delete-widget-button {
  top: 60px;
  left: -15px;
}

.add-widget-button {
  left: 50%;
  bottom: -15px;
  transform: translateX(-50%);
}

.with-background-color {
  background-color: #eaeaea;
}
</style>
