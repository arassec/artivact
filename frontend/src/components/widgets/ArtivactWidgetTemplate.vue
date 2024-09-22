<template>
  <div class="row artivact-widget">

    <!-- Content -->
    <div class="col-grow" v-if="!inEditMode">
      <slot name="widget-content"></slot>
    </div>

    <!-- Editor -->
    <div v-if="inEditMode" class="col-grow widget-editor">
      <q-tooltip>{{ $t('WidgetTemplate.tooltip.edit') }}</q-tooltip>
      <div class="q-pb-lg">

        <q-menu :context-menu="true">
          <q-list data-test="widget-context-menu">
            <q-item
              data-test="widget-context-menu-edit-button"
              clickable
              v-close-popup
              @click="showDetailsRef = true"
            >
              <q-item-section>
                <label>
                  <q-icon
                    name="edit"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('WidgetTemplate.label.edit') }}</label
                >
              </q-item-section>
            </q-item>
            <q-item
              data-test="widget-context-menu-up"
              clickable
              v-close-popup
              @click="$emit('move-widget-up')"
            >
              <q-item-section>
                <label>
                  <q-icon
                    name="arrow_upward"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('WidgetTemplate.label.moveUp') }}</label
                >
              </q-item-section>
            </q-item>
            <q-item
              data-test="widget-context-menu-down"
              clickable
              v-close-popup
              @click="$emit('move-widget-down')"
            >
              <q-item-section>
                <label>
                  <q-icon
                    name="arrow_downward"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('WidgetTemplate.label.moveDown') }}</label
                >
              </q-item-section>
            </q-item>
            <q-item
              data-test="widget-context-menu-add-above"
              clickable
              v-close-popup
              @click="$emit('add-widget-above')"
            >
              <q-item-section>
                <label>
                  <q-icon
                    name="vertical_align_top"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('WidgetTemplate.label.addAbove') }}</label
                >
              </q-item-section>
            </q-item>
            <q-item
              data-test="widget-context-menu-add-below"
              clickable
              v-close-popup
              @click="$emit('add-widget-below')"
            >
              <q-item-section>
                <label>
                  <q-icon
                    name="vertical_align_bottom"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('WidgetTemplate.label.addBelow') }}</label
                >
              </q-item-section>
            </q-item>
            <q-item
              data-test="widget-context-menu-delete"
              clickable
              v-close-popup
              @click="$emit('delete-widget')"
            >
              <q-item-section>
                <label>
                  <q-icon
                    name="delete"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  {{ $t('WidgetTemplate.label.delete') }}</label
                >
              </q-item-section>
            </q-item>
          </q-list>
        </q-menu>

        <slot name="widget-content"></slot>

        <artivact-widget-editor-modal :dialog-model="showDetailsRef">

          <template v-slot:editor-preview>
            <slot name="widget-editor-preview"></slot>
          </template>

          <template v-slot:editor-content>
            <artivact-content>
              <artivact-restrictions-editor
                :in-details-view="false"
                :restrictions="restrictions"
                @delete-restriction="deleteRestriction"
                @add-restriction="addRestriction"
              />
              <div class="full-width q-mt-md">
                <artivact-restricted-translatable-item-editor
                  :locales="localStore.locales"
                  :label="$t('Widget.label.navigationTitle')"
                  :translatable-string="navigationTitle"
                  :show-separator="false"/>
              </div>
            </artivact-content>
            <slot name="widget-editor-content"></slot>
          </template>

          <template v-slot:approve>
            <q-btn
              data-test="widget-editor-modal-approve"
              :label="$t('Common.apply')"
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
import ArtivactContent from 'components/ArtivactContent.vue';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';
import {TranslatableString} from 'components/artivact-models';
import {useLocaleStore} from 'stores/locale';

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
  navigationTitle: {
    required: true,
    type: Object as PropType<TranslatableString>
  },
});

defineEmits<{
  (e: 'move-widget-up'): void;
  (e: 'move-widget-down'): void;
  (e: 'add-widget-above'): void;
  (e: 'add-widget-below'): void;
  (e: 'delete-widget'): void;
}>();

const localStore = useLocaleStore();

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

.widget-editor:hover {
  cursor: pointer;
}

</style>
