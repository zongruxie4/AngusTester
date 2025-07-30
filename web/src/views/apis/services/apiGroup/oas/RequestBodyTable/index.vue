<script setup lang="ts">
import { computed, ref } from 'vue';
import { Icon, IconRequired, Select, Tooltip } from '@xcan-angus/vue-ui';
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

  return dataMap.value[contentType.value];
});

const required = computed(() => {
  return !!dataSource.value?.required;
});

const description = computed(() => {
  return dataSource.value?.description;
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

const domId = computed(() => {
  if (!props.navs?.length) {
    return '';
  }

  return props.navs.find(item => item._key === 'requestBody')?._id;
});
</script>

<template>
  <div class="mx-3.25">
    <div class="flex items-center justify-between mb-2">
      <div :id="domId" class="flex items-center text-3.5 text-theme-title">
        <IconRequired v-if="required" />
        <span class="mr-1">请求体</span>
        <Tooltip v-if="!!description" :title="description">
          <Icon icon="icon-tishi1" class="text-tips cursor-pointer" />
        </Tooltip>
      </div>
      <Select
        v-model:value="contentType"
        internal
        class="w-50"
        defaultActiveFirstOption
        :options="selectOptions" />
    </div>
    <TopTable v-if="Array.isArray(tableData)" :dataSource="tableData" />
  </div>
</template>
