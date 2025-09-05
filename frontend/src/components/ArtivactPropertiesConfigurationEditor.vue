<template>
  <div v-if="propertiesConfigurationProp">
    <q-list bordered class="rounded-borders q-mb-lg">
      <draggable
        :list="propertiesConfigurationProp.categories"
        item-key="id"
        group="categories"
        handle=".category-move-icon"
      >
        <template #item="{ element }">
          <q-expansion-item
            data-test="category-expansion-item"
            :label="element.value"
            group="categories"
            header-class="bg-primary text-white"
            class="category"
            expand-separator
            expand-icon-class="text-white"
          >
            <template v-slot:header>
              <q-item-section avatar>
                <div class="text-white q-gutter-md">
                  <q-icon
                    name="drag_indicator"
                    class="category-move-icon"
                    size="md"
                  ></q-icon>
                </div>
              </q-item-section>

              <q-item-section class="category-label">
                {{ translate(element) }}
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
                  @click="deleteCategory(element)"
                >
                  <q-tooltip>{{
                    $t('ArtivactPropertiesConfigurationEditor.tooltip.delete')
                  }}</q-tooltip>
                </q-btn>
              </q-item-section>
            </template>

            <q-card class="q-mb-lg">
              <q-separator />

              <q-card-section>
                <artivact-restricted-translatable-item-editor
                  data-test="category-name-input"
                  :translatable-string="element"
                  :restricted-item="element"
                  :locales="locales"
                  :label="$t('Common.items.category')"
                  :show-separator="false"
                />
              </q-card-section>

              <q-separator />

              <q-card-section>
                <h2 class="av-text-h2">{{ $t('Common.items.properties') }}</h2>

                <div
                  v-for="(property, index) in element.properties"
                  :key="index"
                >
                  <q-btn
                    round
                    dense
                    flat
                    class="float-right"
                    icon="delete"
                    size="md"
                    @click="deleteProperty(element, index)"
                  >
                    <q-tooltip>{{
                      $t(
                        'ArtivactPropertiesConfigurationEditor.tooltip.deleteProperty',
                      )
                    }}</q-tooltip>
                  </q-btn>
                  <q-btn
                    round
                    dense
                    flat
                    class="float-right"
                    icon="swap_vertical_circle"
                    size="md"
                    v-if="propertiesConfigurationProp.categories.length > 1"
                  >
                    <q-tooltip>{{
                      $t(
                        'ArtivactPropertiesConfigurationEditor.tooltip.switchCategory',
                      )
                    }}</q-tooltip>
                    <q-menu>
                      <q-list>
                        <template
                          v-for="(
                            category, categoryIndex
                          ) in propertiesConfiguration.categories"
                          :key="categoryIndex"
                        >
                          <q-item
                            clickable
                            v-close-popup
                            @click="
                              switchCategory(property, index, element, category)
                            "
                          >
                            {{ category.value }}
                          </q-item>
                        </template>
                      </q-list>
                    </q-menu>
                  </q-btn>
                  <q-btn
                    round
                    dense
                    flat
                    class="float-right"
                    v-if="index > 0"
                    icon="arrow_upward"
                    size="md"
                    @click="moveUp(element, index)"
                  >
                    <q-tooltip>{{
                      $t('ArtivactPropertiesConfigurationEditor.tooltip.up')
                    }}</q-tooltip>
                  </q-btn>
                  <q-btn
                    round
                    dense
                    flat
                    class="float-right q-ml-sm"
                    v-if="index < element.properties.length - 1"
                    icon="arrow_downward"
                    size="md"
                    @click="moveDown(element, index)"
                  >
                    <q-tooltip>{{
                      $t('ArtivactPropertiesConfigurationEditor.tooltip.down')
                    }}</q-tooltip>
                  </q-btn>

                  <artivact-restricted-translatable-item-editor
                    data-test="property-name-input"
                    :translatable-string="property"
                    :restricted-item="property"
                    :locales="locales"
                    :label="$t('Common.items.property')"
                    :show-separator="false"
                  >
                    <artivact-property-value-range-editor
                      :value-range="property.valueRange"
                      :locales="locales"
                    />
                  </artivact-restricted-translatable-item-editor>

                  <q-separator class="q-mt-sm q-mb-lg" />
                </div>
              </q-card-section>

              <q-card-section>
                <div class="row">
                  <q-space></q-space>
                  <q-btn
                    data-test="add-property-button"
                    :label="
                      $t(
                        'ArtivactPropertiesConfigurationEditor.button.addProperty',
                      )
                    "
                    @click="addProperty(element)"
                  />
                </div>
              </q-card-section>
            </q-card>
          </q-expansion-item>
        </template>
      </draggable>
    </q-list>

    <div class="row">
      <q-space></q-space>
      <q-btn
        data-test="add-category-button"
        :label="$t('ArtivactPropertiesConfigurationEditor.button.addCategory')"
        @click="addCategory"
        color="primary"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import draggable from 'vuedraggable';
import { PropType, toRef } from 'vue';
import {
  PropertiesConfiguration,
  Property,
  PropertyCategory,
} from './artivact-models';
import ArtivactRestrictedTranslatableItemEditor from '../components/ArtivactRestrictedTranslatableItemEditor.vue';
import ArtivactPropertyValueRangeEditor from '../components/ArtivactPropertyValueRangeEditor.vue';
import { translate } from './artivact-utils';
import { useI18n } from 'vue-i18n';

const i18n = useI18n();

const props = defineProps({
  propertiesConfiguration: {
    required: true,
    type: Object as PropType<PropertiesConfiguration>,
  },
  locales: {
    required: true,
    type: Object as PropType<string[]>,
  },
});
const propertiesConfigurationProp = toRef(props, 'propertiesConfiguration');

function addCategory() {
  let category: PropertyCategory = {
    id: '',
    value: i18n.t('ArtivactPropertiesConfigurationEditor.newCategory'),
    translatedValue: '',
    translations: {},
    restrictions: [],
    properties: [],
  };
  propertiesConfigurationProp.value.categories.push(category);
}

function addProperty(category: PropertyCategory) {
  let property: Property = {
    id: '',
    value: i18n.t('ArtivactPropertiesConfigurationEditor.newProperty'),
    translatedValue: '',
    translations: {},
    restrictions: [],
    valueRange: [],
  };
  category.properties.push(property);
}

function deleteCategory(category: PropertyCategory) {
  propertiesConfigurationProp.value.categories.splice(
    propertiesConfigurationProp.value.categories.indexOf(category),
    1,
  );
}

function deleteProperty(category: PropertyCategory, index: string | number) {
  let idx: number = +index;
  category.properties.splice(idx, 1);
}

function moveUp(category: PropertyCategory, index: string | number) {
  let idx: number = +index;
  if (idx > 0) {
    let el = category.properties[idx];
    category.properties[idx] = category.properties[idx - 1];
    category.properties[idx - 1] = el;
  }
}

function moveDown(category: PropertyCategory, index: string | number) {
  let idx: number = +index;
  if (idx !== -1 && idx < category.properties.length - 1) {
    let el = category.properties[idx];
    category.properties[idx] = category.properties[idx + 1];
    category.properties[idx + 1] = el;
  }
}

function switchCategory(
  property: Property,
  propertyIndex: string | number,
  oldCategory: PropertyCategory,
  newCategory: PropertyCategory,
) {
  newCategory.properties.push(property);
  deleteProperty(oldCategory, propertyIndex);
}
</script>

<style scoped>
.category {
  border-bottom: 1px solid white;
}

.category-label {
  font-size: large;
}
</style>
