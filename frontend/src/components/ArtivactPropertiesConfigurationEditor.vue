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
                    size="lg"
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
                ></q-btn>
              </q-item-section>
            </template>

            <q-card class="q-mb-lg">
              <q-separator />

              <q-card-section>
                <artivact-restricted-translatable-item-editor
                  :translatable-string="element"
                  :restricted-item="element"
                  :locales="locales"
                  label="Category"
                  :show-separator="false"
                />
              </q-card-section>

              <q-separator />

              <q-card-section>
                <h2 class="av-text-h2">Properties</h2>

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
                  ></q-btn>
                  <q-btn
                    round
                    dense
                    flat
                    class="float-right"
                    icon="swap_vertical_circle"
                    size="md"
                  >
                    <q-menu>
                      <q-list>
                        <template
                          v-for="(
                            category, categoryIndex
                          ) in propertiesConfiguration.categories"
                          :key="categoryIndex"
                        >
                          <q-item
                            v-if="category.id !== element.id"
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
                  ></q-btn>
                  <q-btn
                    round
                    dense
                    flat
                    class="float-right q-ml-sm"
                    v-if="index < element.properties.length - 1"
                    icon="arrow_downward"
                    size="md"
                    @click="moveDown(element, index)"
                  ></q-btn>

                  <artivact-restricted-translatable-item-editor
                    :translatable-string="property"
                    :restricted-item="property"
                    :locales="locales"
                    label="Property"
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
                  <q-btn label="Add Property" @click="addProperty(element)" />
                </div>
              </q-card-section>
            </q-card>
          </q-expansion-item>
        </template>
      </draggable>
    </q-list>

    <div class="row">
      <q-space></q-space>
      <q-btn label="Add Category" @click="addCategory" color="primary" />
    </div>
  </div>
</template>

<script setup lang="ts">
// noinspection ES6UnusedImports
import draggable from 'vuedraggable';
import { PropType, toRef } from 'vue';
import {
  PropertiesConfiguration,
  Property,
  PropertyCategory,
} from 'components/models';
import ArtivactRestrictedTranslatableItemEditor from 'components/ArtivactRestrictedTranslatableItemEditor.vue';
import ArtivactPropertyValueRangeEditor from 'components/ArtivactPropertyValueRangeEditor.vue';
import { translate } from './utils';

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
    value: 'New Category',
    translatedValue: 'New Category',
    translations: {},
    restrictions: [],
    properties: [],
  };
  propertiesConfigurationProp.value.categories.push(category);
}

function addProperty(category: PropertyCategory) {
  let property: Property = {
    id: '',
    value: 'New Property',
    translatedValue: 'New Property',
    translations: {},
    restrictions: [],
    valueRange: [],
  };
  category.properties.push(property);
}

function deleteCategory(category: PropertyCategory) {
  propertiesConfigurationProp.value.categories.splice(
    propertiesConfigurationProp.value.categories.indexOf(category),
    1
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
  newCategory: PropertyCategory
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
