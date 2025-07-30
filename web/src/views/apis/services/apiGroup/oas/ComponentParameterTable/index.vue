<script setup lang="ts">
import { computed } from 'vue';
import { utils } from '@xcan-angus/infra';

import { getPropertyExample, getSchemeExample } from '../utils';
import TopTable from './TopTable.vue';
import IntegerProperty from '../IntegerProperty.vue';
import StringProperty from '../StringProperty.vue';
import BooleanProperty from '../BooleanProperty.vue';

type SchemaItem = {
  properties?: {
    [key: string]: SchemaItem;
  };
  type?: string;
  $ref?: string;
  format?: string;
  required?: string[];
}

interface Props {
  value: {
    [key: string]: {
      schema: SchemaItem;
    };
  }[];
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

// @TODO required字段
const objectToArray = (objectData:{[key:string]:any}):{[key:string]:any}[] => {
  if (!objectData) {
    return [];
  }

  const deepCloneValue = JSON.parse(JSON.stringify(objectData));
  const data:{[key:string]:any}[] = [];
  for (const key in deepCloneValue) {
    const item = deepCloneValue[key];
    const type = item.type;
    item.id = utils.uuid();
    item.name = key;
    if (type === 'object') {
      item['x-xc-children'] = objectToArray(item.properties);
    } else if (type === 'array') {
      item['x-xc-children'] = itemsToArray(item);
    }

    item['x-xc-example'] = getSchemeExample(item);
    delete item.properties;
    delete item.items;

    data.push(item);
  }

  return data;
};

const itemsToArray = (objectData:{[key:string]:any}) => {
  if (!objectData) {
    return [];
  }

  if (Object.prototype.toString.call(objectData.items) !== '[object Object]') {
    objectData.items = {};
  }

  const data:{[key:string]:any}[] = [];
  const item = { ...objectData, ...objectData.items, id: utils.uuid(), name: 'Items' };
  const type = item.type;
  if (type === 'object') {
    item['x-xc-children'] = objectToArray(item.properties);
    delete item.properties;
  } else if (type === 'array') {
    item['x-xc-children'] = itemsToArray(item);
  }

  item['x-xc-example'] = getPropertyExample(item);
  delete item.items;
  data.push(item);

  return data;
};

const dataSource = computed(() => {
  if (!props.value) {
    return [];
  }

  const deepCloneValue = JSON.parse(JSON.stringify(props.value));
  const schema = deepCloneValue.schema;
  if (schema) {
    const type = schema.type;
    if (type === 'object') {
      return objectToArray(schema.properties);
    }

    if (type === 'array') {
      return itemsToArray(schema);
    }

    const example = getPropertyExample(deepCloneValue);
    return { 'x-xc-example': example, ...deepCloneValue, ...schema };
  }

  return [];
});
</script>

<template>
  <div class="mt-3">
    <div class="flex items-center text-3.5 text-theme-title mb-1.5">
      <div class="w-1.25 h-3 rounded mr-2" style="background-color: #1e88e5;"></div>
      <div>属性</div>
    </div>
    <TopTable
      v-if="Array.isArray(dataSource)"
      class="mx-3.25"
      :dataSource="dataSource" />
    <div
      v-else
      class="border border-solid rounded border-theme-text-box mx-3.25 space-y-2.5"
      style="background-color: rgb(251, 251, 251);">
      <IntegerProperty
        v-if="dataSource.type === 'integer' || dataSource.type === 'number'"
        :value="dataSource" />
      <StringProperty
        v-else-if="dataSource.type === 'string'"
        :value="dataSource" />
      <BooleanProperty
        v-else-if="dataSource.type === 'boolean'"
        :value="dataSource" />
    </div>
  </div>
</template>
