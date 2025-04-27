<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { IconCopy, Select } from '@xcan-angus/vue-ui';
import qs from 'qs';

import { ArrayItems, DataType, ObjectProperties, PathItemInfo, RequestBodyContent } from './PropsType';
import { encode, stringify } from './utils';

type ContentType = 'application/xml' | 'application/json' | 'application/x-www-form-urlencoded' | 'multipart/form-data' | 'application/octet-stream';

interface Props {
  serverUrl: string;
  value: PathItemInfo;
  navs: {
    _id: string;
    _name: string;
    _key: string;
  }[];
}

const props = withDefaults(defineProps<Props>(), {
  serverUrl: undefined,
  value: undefined,
  navs: () => []
});

const contentType = ref<ContentType>();
const dataSource = ref<PathItemInfo>();

onMounted(() => {
  watch(() => props.value, (newValue) => {
    if (newValue) {
      dataSource.value = JSON.parse(JSON.stringify(newValue));
      return;
    }

    dataSource.value = undefined;
  }, { immediate: true });
});

const parameters = computed(() => {
  return dataSource.value?.parameters || [];
});

const parametersO2O = (data: {
  type: DataType;
  default: any;
  enum?: string[];
  examples?: any[];
  example?: any;
  items?: {
    type: DataType;
    default: any;
    enum?: string[];
    examples?: any[];
    example?: any;
    properties: {
      [key: string]: {
        type: DataType;
        default: any;
        enum?: string[];
        examples?: any[];
        example?: any;
      }
    }
  };
  properties: {
    [key: string]: {
      type: DataType;
      default: any;
      enum?: string[];
      examples?: any[];
      example?: any;
    }
  }
}) => {
  const type = data.type;
  if (type === 'object') {
    if (Object.prototype.hasOwnProperty.call(data, 'examples')) {
      return data.examples?.[0];
    } else if (Object.prototype.hasOwnProperty.call(data, 'example')) {
      return data.example;
    } else if (Object.prototype.hasOwnProperty.call(data, 'default')) {
      return data.default;
    }

    const properties = data.properties;
    if (properties) {
      const map: { [key: string]: any } = {};
      for (const key in properties) {
        const value = parametersO2O(properties[key]);
        if (value !== null && value !== undefined && value !== '') {
          map[key] = value;
        }
      }

      if (Object.keys(map).length) {
        return map;
      }
    }

    return null;
  }

  if (type === 'array') {
    const items = data.items;
    if (items) {
      const value = parametersO2O(items);
      if (value !== null && value !== undefined && value !== '') {
        return [value];
      }
    }

    return null;
  }

  if (Object.prototype.hasOwnProperty.call(data, 'examples')) {
    return data.examples?.[0];
  } else if (Object.prototype.hasOwnProperty.call(data, 'example')) {
    return data.example;
  } else if (data.type === 'string' && data.enum?.length) {
    return data.enum[0];
  } else if (Object.prototype.hasOwnProperty.call(data, 'default')) {
    return data.default;
  }

  return null;
};

const covertParameters = (data, allowEmpty = false) => {
  const list: { [key: string]: any }[] = [];
  if (data.length) {
    for (let i = 0, len = data.length; i < len; i++) {
      const item = data[i];
      const name = item.name;
      if (Object.prototype.hasOwnProperty.call(item, 'examples')) {
        if (Object.prototype.toString.call(item.examples) === '[object Object]') {
          for (const key in item.examples) {
            const value = item.examples[key]?.value;
            if (!allowEmpty) {
              if (value !== null && value !== undefined && value !== '') {
                list.push({ name, value });
              }
            } else {
              list.push({ name, value });
            }

            break;
          }
        }
      } else if (Object.prototype.hasOwnProperty.call(item, 'example')) {
        const value = item.example;
        if (!allowEmpty) {
          if (value !== null && value !== undefined && value !== '') {
            list.push({ name, value });
          }
        } else {
          list.push({ name, value });
        }
      } else if (Object.prototype.hasOwnProperty.call(item, 'x-xc-value')) {
        const value = item['x-xc-value'];
        if (!allowEmpty) {
          if (value !== null && value !== undefined && value !== '') {
            list.push({ name, value });
          }
        } else {
          list.push({ name, value });
        }
      } else {
        const schema = item.schema;
        if (schema) {
          const type = schema.type;
          if (Object.prototype.hasOwnProperty.call(schema, 'examples')) {
            const value = schema.examples?.[0];
            if (!allowEmpty) {
              if (value !== null && value !== undefined && value !== '') {
                list.push({ name, value });
              }
            } else {
              list.push({ name, value });
            }
          } else if (Object.prototype.hasOwnProperty.call(schema, 'example')) {
            const value = schema.example;
            if (!allowEmpty) {
              if (value !== null && value !== undefined && value !== '') {
                list.push({ name, value });
              }
            } else {
              list.push({ name, value });
            }
          } else if (Object.prototype.hasOwnProperty.call(schema, 'default')) {
            const value = schema.default;
            if (!allowEmpty) {
              if (value !== null && value !== undefined && value !== '') {
                list.push({ name, value });
              }
            } else {
              list.push({ name, value });
            }
          } else if (type === 'string' && schema.enum?.length) {
            const value = schema.enum[0];
            if (!allowEmpty) {
              if (value !== null && value !== undefined && value !== '') {
                list.push({ name, value });
              }
            } else {
              list.push({ name, value });
            }
          } else if (type === 'array' || type === 'object') {
            const value = parametersO2O(schema);
            if (!allowEmpty) {
              if (value !== null && value !== undefined && value !== '') {
                list.push({ name, value });
              }
            } else {
              list.push({ name, value });
            }
          }
        }
      }
    }
  }

  return list;
};

const pathParametersMap = computed(() => {
  const _parameters = parameters.value?.filter(item => item.in === 'path');
  const list = covertParameters(_parameters);
  const map: { [key: string]: any } = {};
  if (list.length) {
    for (let i = 0, len = list.length; i < len; i++) {
      const { name, value } = list[i];
      map[name] = value;
    }
  }

  return map;
});

const cookieParametersMap = computed(() => {
  const map: { [key: string]: string } = {};
  const _parameters = parameters.value?.filter(item => item.in === 'cookie');
  const list = covertParameters(_parameters, true);
  if (list.length) {
    for (let i = 0, len = list.length; i < len; i++) {
      const { name, value } = list[i];
      if (value !== null && value !== undefined && value !== '') {
        if (map[name]) {
          map[name] = map[name] + ',' + encode(value, false);
        } else {
          map[name] = encode(value, false);
        }
      }
    }
  }

  return map;
});

const headerParametersMap = computed(() => {
  const map:{[key:string]:string} = {};
  if (contentType.value) {
    map['Content-Type'] = contentType.value;
  }

  const _parameters = parameters.value?.filter(item => item.in === 'header');
  const list = covertParameters(_parameters, true);
  if (list.length) {
    for (let i = 0, len = list.length; i < len; i++) {
      const { name, value } = list[i];
      if (value !== null && value !== undefined && value !== '') {
        if (map[name]) {
          map[name] = map[name] + ',' + encode(value, false);
        } else {
          map[name] = encode(value, false);
        }
      }
    }
  }

  return map;
});

const queryParametersMap = computed(() => {
  const map: { [key: string]: string } = {};
  const _parameters = parameters.value?.filter(item => item.in === 'query');
  const list = covertParameters(_parameters, true);
  if (list.length) {
    for (let i = 0, len = list.length; i < len; i++) {
      const { name, value } = list[i];
      if (value !== null && value !== undefined && value !== '') {
        if (map[name]) {
          map[name] = map[name] + ',' + encode(value, false);
        } else {
          map[name] = encode(value);
        }
      }
    }
  }

  return map;
});

const getExampleValue = (data: ObjectProperties[string]) => {
  if (Object.prototype.hasOwnProperty.call(data, 'examples')) {
    if (Array.isArray(data.examples)) {
      return stringify(data.examples[0]);
    }

    return stringify(data.examples);
  }

  if (Object.prototype.hasOwnProperty.call(data, 'example')) {
    return stringify(data.example);
  }

  if (data.type === 'string' && data.enum?.length) {
    return data.enum[0];
  }

  return data.type;
};

const getArray = (data: ArrayItems | undefined): any[] => {
  if (!data) {
    return [];
  }

  const arr: any[] = [];
  const { type, properties, items } = data;
  if (['string', 'integer', 'number', 'boolean'].includes(type)) {
    arr.push(getExampleValue(data));
  } else if (type === 'array') {
    arr.push(...getArray(items));
  } else if (type === 'object') {
    arr.push(getObject(properties));
  }

  return arr;
};

const getObject = (data: ObjectProperties | undefined): { [key: string]: any } => {
  if (!data) {
    return {};
  }

  const obj: { [key: string]: any } = {};
  for (const key in data) {
    const { type } = data[key];
    if (['string', 'integer', 'number', 'boolean'].includes(type)) {
      obj[key] = getExampleValue(data[key]);
    } else if (type === 'array') {
      obj[key] = getArray(data[key]?.items);
    } else if (type === 'object') {
      obj[key] = getObject(data[key]?.properties);
    }
  }

  return obj;
};

const getRequestBody = (data: RequestBodyContent) => {
  const type = data.schema?.type;
  if (['string', 'integer', 'number', 'boolean'].includes(type)) {
    return getExampleValue(data.schema);
  }

  if (type === 'object') {
    return getObject(data.schema?.properties);
  }

  if (type === 'array') {
    return getArray(data.schema?.items);
  }
};

const requestBodyMap = computed(() => {
  if (!dataSource.value?.requestBody) {
    return {};
  }

  const content = dataSource.value.requestBody.content;
  if (!content) {
    return {};
  }

  const map: { [key: string]: any } = {};
  const entries = Object.entries(content);
  return entries.reduce((prev, [key, data]) => {
    prev[key] = getRequestBody(data);
    return prev;
  }, map);
});

const requestBody = computed(() => {
  if (!requestBodyMap.value || !contentType.value) {
    return '';
  }

  return requestBodyMap.value[contentType.value] || '';
});

const method = computed(() => {
  return dataSource.value?._method || '';
});

const queryString = computed(() => {
  // 拼接URL和参数
  if (queryParametersMap.value && Object.keys(queryParametersMap.value).length) {
    return qs.stringify(queryParametersMap.value, { allowDots: true, encode: true });
  }

  return '';
});

const href = computed(() => {
  if (!props.serverUrl || !dataSource.value?._uri) {
    return '';
  }

  let uri = dataSource.value._uri;
  const map = pathParametersMap.value;
  if (map) {
    for (const key in map) {
      const value = map[key];
      if (value) {
        const rex = new RegExp('{' + key + '}');
        uri = uri.replace(rex, encode(value));
      }
    }
  }

  let _href = props.serverUrl + uri;
  // 拼接URL和参数
  if (queryString.value) {
    _href += '?' + queryString.value;
  }

  return _href;
});

const curlCommand = computed(() => {
  let curlCommand = 'curl -X ' + '\'' + method.value?.toUpperCase() + '\'' + ' ' + '\\\n\t\'' + href.value + '\' \\\n';
  // 添加headers
  const headerMap = headerParametersMap.value;
  if (headerMap) {
    for (const key in headerMap) {
      curlCommand += '\t-H \'' + key + ': ' + headerMap[key] + '\' \\\n';
    }
  }

  const cookieMap = cookieParametersMap.value;
  if (cookieMap) {
    let str = '';
    for (const key in cookieMap) {
      str += key + '=' + cookieMap[key] + ';';
    }

    str = str.replace(/;$/, '');
    if (str) {
      curlCommand += '\t-H \'' + 'Cookie: ' + str + '\' \\\n';
    }
  }

  // 添加data
  if (requestBody.value) {
    const _data = JSON.stringify(requestBody.value, null, 0);
    curlCommand += '\t-d \'' + _data + '\'';
  }

  return curlCommand;
});

const selectOptions = computed(() => {
  if (!dataSource.value?.requestBody?.content) {
    return;
  }

  return Object.keys(dataSource.value.requestBody.content)?.map(item => {
    return {
      label: item,
      value: item
    };
  });
});

const domId = computed(() => {
  if (!props.navs?.length) {
    return '';
  }

  return props.navs.find(item => item._key === 'requestDemo')?._id;
});
</script>

<template>
  <div class="mx-3.25">
    <div :id="domId" class="text-3.5 text-theme-title mb-2">
      <div class="flex items-center justify-between mb-2">
        <div :id="domId" class="flex items-center text-3.5 text-theme-title">
          请求示例
        </div>
        <Select
          v-if="contentType"
          v-model:value="contentType"
          internal
          class="w-50"
          defaultActiveFirstOption
          :options="selectOptions" />
      </div>
    </div>
    <div class="border border-solid rounded border-theme-text-box" style="background-color:rgba(251, 251, 251, 100%);">
      <div class="flex items-end justify-between py-2 mx-3">
        <div :title="href" class="truncate mr-5">{{ href }}</div>
        <IconCopy :copyText="href" class="flex-shrink-0 transform-gpu -translate-y-1" />
      </div>
      <div class="w-full h-0.25 mx-3" style="width:calc(100% - 24px);background-color: var(--border-text-box);"></div>
      <template v-if="requestBody">
        <div class="relative py-2">
          <div class="pl-3 pr-5 max-h-70 overflow-x-hidden overflow-y-auto">
            <pre class="break-all whitespace-break-spaces"><code>{{ requestBody }}</code></pre>
          </div>
          <IconCopy :copyText="requestBody" class="absolute bottom-3 right-3 flex-shrink-0" />
        </div>
        <div class="w-full h-0.25 mx-3" style="width:calc(100% - 24px);background-color: var(--border-text-box);"></div>
      </template>
      <div class="relative py-2">
        <div class="pl-3 pr-5 max-h-70 overflow-x-hidden overflow-y-auto">
          <pre class="break-all whitespace-break-spaces"><code>{{ curlCommand }}</code></pre>
        </div>
        <IconCopy :copyText="curlCommand" class="absolute bottom-3 right-3 flex-shrink-0" />
      </div>
    </div>
  </div>
</template>
