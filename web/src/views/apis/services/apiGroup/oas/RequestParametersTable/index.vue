<script setup lang="ts">
import { computed } from 'vue';
import { utils } from '@xcan-angus/infra';

import { getPropertyExample, getSchemeExample } from '../utils';
import TopTable from './TopTable.vue';

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
  navs: {
    _id: string;
    _name: string;
    _key: string;
  }[];
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  navs: () => []
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
  return deepCloneValue.map(item => {
    if (item.schema?.type) {
      if (item.schema?.type === 'object') {
        item['x-xc-children'] = objectToArray(item.schema.properties);
        delete item.schema.properties;
      } else if (item.schema?.type === 'array') {
        item['x-xc-children'] = itemsToArray(item.schema);
        delete item.schema.items;
      }
    } else {
      if (((item.name)?.toLowerCase()) === (('Content-Type').toLowerCase())) {
        if (Object.prototype.hasOwnProperty.call(item, 'schema')) {
          item.schema.type = 'string';
        } else {
          item.schema = { type: 'string' };
        }
      }
    }

    item['x-xc-example'] = getSchemeExample(item);
    item = {
      ...item,
      ...item.schema
    };
    delete item.schema;
    item.id = utils.uuid();

    return item;
  });
});

const domId = computed(() => {
  if (!props.navs?.length) {
    return '';
  }

  return props.navs.find(item => item._key === 'parameter')?._id;
});
</script>

<template>
  <div class="mx-3.25">
    <div :id="domId" class="text-3.5 text-theme-title mb-2">请求参数</div>
    <TopTable :dataSource="dataSource" />
  </div>
</template>
