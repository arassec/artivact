<template>
  <artivact-widget-template
    v-if="widgetDataRef"
    :editing="editingRef"
    :in-edit-mode="inEditMode"
    :restrictions="widgetDataRef.restrictions"
    :navigation-title="widgetDataRef.navigationTitle"
    @start-editing="editingRef = true"
    @stop-editing="editingRef = false"
  >
    <template v-slot:widget-content>
      <q-space/>
      <div class="row full-width gt-sm">
        <div
          v-for="(buttonConfig, index) in widgetDataRef.buttonConfigs"
          :key="index"
          :class="'col-' + 12 / widgetDataRef.columns"
          class="flex justify-center"
          :style="{ marginTop: index >= widgetDataRef.columns ? '16px' : '0' }"
        >
          <artivact-button :config="buttonConfig" :disabled="inEditMode"/>
        </div>
      </div>
      <div class="row full-width lt-sm">
        <div
          v-for="(buttonConfig, index) in widgetDataRef.buttonConfigs"
          :key="index"
          class="col-12 flex justify-center q-mb-sm"
        >
          <artivact-button :config="buttonConfig" :disabled="inEditMode"/>
        </div>
      </div>
      <q-space/>
    </template>

    <template v-slot:widget-editor>
      <div class="column full-width">
        <q-input
          type="number"
          outlined
          v-model="widgetDataRef.columns"
          class="q-mb-xl full-width"
          :label="$t('ButtonsWidget.label.columns')"
        />

        <q-list bordered class="rounded-borders q-mb-lg">
          <q-expansion-item
            header-class="bg-primary text-white"
            class="list-entry"
            expand-separator
            expand-icon-class="text-white"
            v-for="(buttonConfig, index) in widgetDataRef.buttonConfigs"
            :key="index"
          >
            <template v-slot:header>
              <q-item-section class="list-entry-label">
                {{ translate(buttonConfig.label) }}
              </q-item-section>

              <q-item-section side>
                <q-btn
                  round
                  dense
                  flat
                  class="float-right"
                  color="white"
                  icon="delete"
                  size="md"
                  @click.stop="deleteButton(index)"
                >
                </q-btn>
              </q-item-section>
            </template>

            <artivact-button-editor :config="buttonConfig" class="q-mt-md"/>
          </q-expansion-item>
        </q-list>

        <div>
          <q-btn
            class="float-right"
            rounded
            dense
            color="primary"
            icon="add"
            @click="addButtonConfig()"
          />
        </div>
      </div>
    </template>
  </artivact-widget-template>
</template>

<script setup lang="ts">
import {PropType, ref, toRef} from 'vue';
import {ButtonsWidgetData} from './artivact-widget-models';
import ArtivactWidgetTemplate from './ArtivactWidgetTemplate.vue';
import ArtivactButton from '../ArtivactButton.vue';
import ArtivactButtonEditor from '../ArtivactButtonEditor.vue';
import {ButtonConfig, TranslatableString} from '../artivact-models';
import {translate} from '../artivact-utils';

const props = defineProps({
  inEditMode: {
    required: true,
    type: Boolean,
  },
  widgetData: {
    required: true,
    type: Object as PropType<ButtonsWidgetData>,
  },
});

const editingRef = ref(false);

const widgetDataRef = toRef(props, 'widgetData');

function addButtonConfig() {
  widgetDataRef.value.buttonConfigs.push({
    targetUrl: '',
    iconLeft: '',
    label: {
      value: 'Button',
    } as TranslatableString,
    iconRight: '',
    size: 1,
    buttonColor: 'primary',
    textColor: 'white',
  } as ButtonConfig);
}

function deleteButton(index) {
  widgetDataRef.value.buttonConfigs.splice(index, 1);
}
</script>

<style scoped>
.list-entry {
  border-bottom: 1px solid white;
}

.list-entry-label {
  font-size: large;
}
</style>
