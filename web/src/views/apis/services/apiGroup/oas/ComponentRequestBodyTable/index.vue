<script setup lang="ts">
import { computed, ref } from 'vue';
import { Select } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { getPropertyExample } from '../utils';
import TopTable from './TopTable.vue';

type SchemaItem = {
  properties?: {
    [key: string]: SchemaItem;
  };
  type?: string;
  format?: string;
  required?: string[];
}

type ContentType = 'application/xml' | 'application/json' | 'application/x-www-form-urlencoded' | 'multipart/form-data' | 'application/octet-stream';

interface Props {
  value: {
    required?: boolean;
    description?: string;
    content: {
      [key in ContentType]: {
        schema: SchemaItem;
      };
    }
  };
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const contentType = ref<ContentType>();

const objectToArray = (objectData:{[key:string]:any}, requiredList:string[] = []):{[key:string]:any}[] => {
  if (!objectData) {
    return [];
  }

  const data:{[key:string]:any}[] = [];
  const deepCloneValue = JSON.parse(JSON.stringify(objectData));
  if (Object.prototype.toString.call(deepCloneValue) === '[object Object]') {
    for (const key in deepCloneValue) {
      const item = { ...deepCloneValue[key], name: key, id: utils.uuid(), required: requiredList.includes(key) };
      const type = item.type;
      if (type === 'object') {
        item['x-xc-children'] = objectToArray(item.properties, deepCloneValue[key].required);
        delete item.properties;
      } else if (type === 'array') {
        item['x-xc-children'] = itemsToArray(item.items);
        delete item.items;
      }

      item['x-xc-example'] = getPropertyExample(item);
      data.push(item);
    }
  }

  return data;
};

const itemsToArray = (objectData:{[key:string]:any}) => {
  if (!objectData) {
    return [];
  }

  const item = { ...objectData, name: 'Items', id: utils.uuid() };
  const type = item.type;
  if (type === 'object') {
    item['x-xc-children'] = objectToArray(item.properties);
    delete item.items;
    delete item.properties;
  } else if (type === 'array') {
    item['x-xc-children'] = itemsToArray(item.items);
    delete item.items;
  }

  item['x-xc-example'] = getPropertyExample(item);
  return [item];
};

const dataSource = computed(() => {
  if (props.value) {
    return JSON.parse(JSON.stringify(props.value));
  }

  return props.value;
});

const dataMap = computed(() => {
  if (Object.prototype.toString.call(dataSource.value?.content) !== '[object Object]') {
    return {};
  }

  const map = {};
  const content = dataSource.value.content;
  for (const key in content) {
    const schema = content[key].schema;
    const type = schema?.type;
    if (type === 'object') {
      map[key] = objectToArray(schema.properties, schema.required);
    } else if (type === 'array') {
      map[key] = itemsToArray(schema.items);
    }
  }

  return map;
});

const tableData = computed(() => {
  if (!dataSource.value || !contentType.value || !dataMap.value[contentType.value]) {
    return [];
  }

  return dataMap.value[contentType.value] || [];
});

const selectOptions = computed(() => {
  if (!dataSource.value) {
    return;
  }

  if (dataSource.value.content) {
    return Object.keys(dataSource.value.content)?.map(item => {
      return {
        label: item,
        value: item
      };
    });
  }

  return [];
});
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-2 pr-3.25">
      <div class="flex items-center text-3.5 text-theme-title">
        <div class="w-1.25 h-3 rounded mr-2" style="background-color: #1e88e5;"></div>
        <div>属性</div>
      </div>
      <Select
        v-model:value="contentType"
        internal
        class="w-50"
        defaultActiveFirstOption
        :options="selectOptions" />
    </div>
    <TopTable class="mx-3.25" :dataSource="tableData" />
  </div>
</template>
