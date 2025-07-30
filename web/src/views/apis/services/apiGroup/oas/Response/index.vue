<script setup lang="ts">
import { computed, ref } from 'vue';
import { utils } from '@xcan-angus/infra';
import { Select } from '@xcan-angus/vue-ui';

import { PathItemInfo } from '../PropsType';
import { getPropertyExample, getSchemeExample } from '../utils';
import TopTable from './TopTable.vue';

interface Props {
  value: PathItemInfo['responses'];
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

const contentType = ref<string>('');

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

  return {};
});

const responseKeys = computed(() => {
  if (!dataSource.value) {
    return [];
  }

  return Object.keys(dataSource.value);
});

const headersDataMap = computed(() => {
  if (!dataSource.value) {
    return {};
  }

  const map:{[key:string]:{
    name:string;
    type:string;
    format:string;
    description:string;
  }[]} = {};
  const _responsesMap = dataSource.value;
  for (const key in _responsesMap) {
    const headers = _responsesMap[key].headers;
    if (headers) {
      map[key] = [];
      for (const _key in headers) {
        let item = headers[_key];
        if (item.schema?.type === 'object') {
          item['x-xc-children'] = objectToArray(item.schema.properties);
          delete item.schema.properties;
        } else if (item.schema?.type === 'array') {
          item['x-xc-children'] = itemsToArray(item.schema);
          delete item.schema.items;
        }

        item['x-xc-example'] = getSchemeExample(item);
        item = {
          ...item,
          ...item.schema,
          name: _key
        };
        delete item.schema;
        item.id = utils.uuid();
        map[key].push(item);
      }
    }
  }

  return map;
});

const contentMap = computed(() => {
  if (!dataSource.value) {
    return {};
  }

  const map:{[key:string]:{[key:string]:any[]}} = {};
  const data = dataSource.value;
  for (const key in data) {
    const content = data[key].content;
    map[key] = {};
    if (content) {
      for (const _key in content) {
        const schema = content[_key].schema;
        const type = schema?.type;
        if (type === 'object') {
          map[key][_key] = objectToArray(schema.properties, schema.required);
        } else if (type === 'array') {
          map[key][_key] = itemsToArray(schema.items);
        }
      }
    }
  }

  return map;
});

const selectOptionsMap = computed(() => {
  if (!dataSource.value) {
    return {};
  }

  const map:{[key:string]:{label:string;value:string}[]} = {};
  const data = dataSource.value;
  for (const key in data) {
    const content = data[key].content;
    if (content) {
      map[key] = Object.keys(content).map(item => {
        return {
          label: item,
          value: item
        };
      });
    }
  }

  return map;
});

const navsMap = computed(() => {
  return props.navs?.reduce((prev, cur) => {
    prev[cur._key] = cur;
    return prev;
  }, {});
});
</script>
<template>
  <div class="mx-3.25">
    <div
      v-for="item in responseKeys"
      :key="item"
      class="mb-5">
      <div :id="navsMap[item]?._id" class="text-3.5 text-theme-title">{{ item }}</div>
      <div v-if="dataSource[item]?.description" class="text-theme-sub-content break-words whitespace-normal mt-0.5">{{ dataSource[item]?.description }}</div>
      <div class="space-y-5 mt-2">
        <div v-if="dataSource[item]?.headers" class="space-y-2">
          <div class="text-3.5 text-theme-title">响应头</div>
          <TopTable :dataSource="headersDataMap[item]" />
        </div>
        <div v-if="selectOptionsMap[item]?.length" class="space-y-2">
          <div class="flex items-center justify-between">
            <div class="flex items-center text-3.5 text-theme-title">响应体</div>
            <Select
              v-model:value="contentType"
              internal
              class="w-50"
              defaultActiveFirstOption
              :options="selectOptionsMap[item]" />
          </div>
          <TopTable :dataSource="contentMap[item][contentType]||[]" />
        </div>
      </div>
    </div>
  </div>
</template>
