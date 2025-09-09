<script setup lang="ts">
import { watch, ref, onMounted, nextTick } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';

import { ResponseState } from '@/utils/ApiUtils';
import { baseSource, getStatusText } from './interface';

interface Props {
  dataSource:ResponseState
}

interface RequestItem {
  name: string;
  value: Array<Record<string, string>>;
  spread: boolean;
  type?: string
}

const props = withDefaults(defineProps<Props>(), {});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'rendered', value: true);
}>();

// 定义要渲染的请求数据的分组
const sourceData = ref<RequestItem[]>([
  { name: 'General', value: [], spread: true },
  { name: 'Query Parameters', value: [], spread: true },
  { name: 'Request Headers', value: [], spread: true },
  { name: 'Response Headers', value: [], spread: true, type: '' },
  { name: 'Raw', value: [], spread: true }]);

watch(() => props.dataSource, (newValue) => {
  sourceData.value.forEach(i => {
    i.value = [];
  });
  const { status, config = {}, headers, requestHeaders } = newValue || {};

  let value = '';
  // 基本
  baseSource.forEach(item => {
    if (item.key === 'statusText') {
      value = status ? getStatusText(status) : '';
    } else if (item.key === 'url') {
      // value = config[item.key] ? encodeURI(config[item.key]) : '';
      value = config[item.key];
    } else {
      value = config[item.key];
    }
    sourceData.value[0].value.push({
      value,
      name: item.name
    });
  });

  // queryString
  if (config.queryString) {
    const querys = config.queryString?.split('&');
    for (const query of querys) {
      const [key, value] = query.split('=');
      sourceData.value[1].value.push({ name: decodeURIComponent(key), value: decodeURIComponent(value) });
    }
  }

  // request Header
  if (requestHeaders) {
    for (const head in requestHeaders) {
      if (requestHeaders[head]) {
        sourceData.value[2].value.push({ name: head || '', value: decodeURIComponent(requestHeaders[head]) });
      }
    }
  }

  // response header
  if (headers && Object.keys(headers).length) {
    for (const key in headers) {
      if (Object.prototype.hasOwnProperty.call(headers, key)) {
        if (typeof headers[key] === 'string' || !headers[key]) {
          sourceData.value[3].value.push({ name: key, value: headers[key] });
        } else {
          sourceData.value[3].value.push(...headers[key].map(i => ({ name: key, value: i })));
        }
      }
    }
  }

  // request body
  if (config.forms) {
    sourceData.value[4].name = 'Form';
    for (const key in config.forms) {
      sourceData.value[4].value.push({ name: key, value: config.forms[key] });
    }
  }
  if (config.body) {
    sourceData.value[4].name = 'Raw';
    sourceData.value[4].value.push({ name: '', value: config.body });
  }
}, {
  immediate: true,
  deep: true
});

const handleSpread = (idx) => {
  sourceData.value[idx].spread = !sourceData.value[idx].spread;
};

onMounted(() => {
  nextTick(() => {
    emit('rendered', true);
  });
});
</script>
<template>
  <div class="h-full py-3 overflow-auto">
    <div
      v-for="(item, idx) in sourceData"
      :key="item.name"
      class="mb-1">
      <template v-if="item.value.length">
        <div class="py-2 text-theme-title text-3 leading-3.5 flex items-center">
          <span @click="handleSpread(idx)">
            <Icon
              icon="icon-xiangxia"
              class="transition-all mr-2 align-top"
              :class="{'-rotate-90': !item.spread}" />
          </span>
          <span>{{ item.name }}</span>
        </div>
        <div v-if="item.type" class="text-theme-title text-3 pl-6.5">{{ item.type }}</div>
        <div
          v-for="(subItem,index) in item.value"
          v-show="item.spread"
          :key="index"
          class="flex items-center flex-wrap whitespace-pre-wrap mb-1.5 leading-4 break-all text-3 last:mb-0 pl-6.5 ">
          <span v-if="subItem.name" class="inline-block mr-1.5 text-theme-sub-content">{{ subItem.name }}:</span>
          <span class="inline-block text-theme-content">{{ subItem.value }}</span>
        </div>
      </template>
    </div>
  </div>
</template>
