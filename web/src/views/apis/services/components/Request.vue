<script setup lang="ts">
import { ref, watch } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';

import { ResponseState } from '@/views/apis/services/protocol/http/interface';

interface Props {
  dataSource: ResponseState
}

const props = withDefaults(defineProps<Props>(), {});

/**
 * Base source configuration for request information
 */
const baseSource = [
  {
    name: 'Request URL',
    key: 'url',
    value: ''
  },
  {
    name: 'Request Method',
    key: 'method',
    value: ''
  },
  {
    name: 'Status Code',
    key: 'statusText',
    value: ''
  }
];

/**
 * HTTP status code to text mapping
 */
const StatusMap: Record<number, string> = {
  100: 'Continue',
  101: 'Switching Protocols',
  200: 'OK',
  201: 'Created',
  202: 'Accepted',
  203: 'Non-Authoritative info',
  204: 'No content',
  205: 'Reset content',
  206: 'Partial content',
  300: 'Multiple Choices',
  301: 'Moved Permanently',
  302: 'Found',
  303: 'See Other',
  304: 'Not Modified',
  305: 'Use Proxy',
  306: 'Unused',
  307: 'Temporary Redirect',
  400: 'Bad Request',
  401: 'Unauthorized',
  402: 'Payment Required',
  403: 'Forbidden',
  404: 'Not Found',
  405: 'Method Not Allowed',
  406: 'Not Acceptable',
  407: 'Proxy Authentication Required',
  408: 'Request Time-out',
  409: 'Conflict',
  410: 'Gone',
  411: 'Length Required',
  412: 'precondition Failed',
  413: 'Request Entity Too Large',
  414: 'Request-URI Too Large',
  415: 'Unsupported Media Type',
  416: 'Requested range not satisfiable',
  417: 'Expectation Failed',
  500: 'Internal Server error',
  501: 'Not Implemented',
  502: 'Bad Gateway',
  503: 'services Unavailable',
  504: 'Gateway Time-out',
  505: 'http version not supported'
};

/**
 * Get status text for a given HTTP status code
 * @param status - HTTP status code
 * @returns Status text including code and description
 */
const getStatusText = (status: number): string => {
  let statusText = status + '';
  if (StatusMap[status]) {
    statusText += ' ' + StatusMap[status];
  }

  return statusText;
};

interface RequestItem {
  name: string;
  value: Array<Record<string, string>>;
  spread: boolean;
  type?: string
}

// Define request data groups to render
const sourceData = ref<RequestItem[]>([
  { name: 'General', value: [], spread: true },
  { name: 'Query Parameters', value: [], spread: true },
  { name: 'Request Headers', value: [], spread: true },
  { name: 'Response Headers', value: [], spread: true, type: '' },
  { name: 'Raw', value: [], spread: true }]);

/**
 * Toggle the spread state of a request data section
 * @param idx - Index of the section to toggle
 */
const handleSpread = (idx: number) => {
  sourceData.value[idx].spread = !sourceData.value[idx].spread;
};

// Watch for data source changes and update request data
watch(() => props.dataSource, (newValue) => {
  // Clear all existing values
  sourceData.value.forEach(i => {
    i.value = [];
  });

  const { status, config = {}, headers, requestHeaders } = newValue || {};

  let value = '';
  // Basic information
  baseSource.forEach(item => {
    if (item.key === 'statusText') {
      value = getStatusText(status || 0);
    } else if (item.key === 'url') {
      // value = encodeURI(config[item.key]);
      value = config[item.key];
    } else {
      value = config[item.key];
    }
    sourceData.value[0].value.push({
      value,
      name: item.name
    });
  });

  // Query string parameters
  if (config.queryString) {
    const querys = config.queryString?.split('&');
    for (const query of querys) {
      const [key, value] = query.split('=');
      sourceData.value[1].value.push({ name: decodeURIComponent(key), value: decodeURIComponent(value) });
    }
  }

  // Request headers
  if (requestHeaders) {
    for (const head in requestHeaders) {
      if (requestHeaders[head]) {
        sourceData.value[2].value.push({ name: head || '', value: decodeURIComponent(requestHeaders[head] as unknown as string) });
      }
    }
  }

  // Response headers
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

  // Request body
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
</script>

<template>
  <div class="h-full py-3 overflow-auto">
    <div
      v-for="(item, idx) in sourceData"
      :key="item.name"
      class="mb-1">
      <template v-if="item.value.length">
        <div class="py-2 text-text-title text-3 leading-3.5 flex items-center">
          <span @click="handleSpread(idx)">
            <Icon
              icon="icon-xiangxia"
              class="transition-all mr-2 align-top"
              :class="{'-rotate-90': !item.spread}" />
          </span>
          <span>{{ item.name }}</span>
        </div>
        <div v-if="item.type" class="text-text-title text-3 pl-6.5">{{ item.type }}</div>
        <div
          v-for="(subItem,index) in item.value"
          v-show="item.spread"
          :key="index"
          class="flex items-center flex-wrap whitespace-pre-wrap mb-1.5 leading-4 break-all text-3 last:mb-0 pl-6.5 ">
          <span v-if="subItem.name" class="inline-block mr-1.5 text-text-sub-content">{{ subItem.name }}:</span>
          <span class="inline-block text-text-content">{{ subItem.value }}</span>
        </div>
      </template>
    </div>
  </div>
</template>
