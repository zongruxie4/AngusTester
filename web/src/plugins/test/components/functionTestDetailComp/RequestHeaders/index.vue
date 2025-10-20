<script setup lang="ts">
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { utils } from '@xcan-angus/infra';
import { Arrow, Colon, NoData } from '@xcan-angus/vue-ui';

import { ExecContent } from '@/plugins/test/types';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Header item interface
 */
interface HeaderItem {
  id: string;      // Unique identifier
  name: string;    // Header name/key
  value: string;   // Header value
}

/**
 * Panel configuration interface
 */
interface PanelConfig {
  id: string;      // Unique identifier for panel
  name: string;    // Display name (internationalized)
  key: string;     // Panel type: 'general' | 'request' | 'response'
}

/**
 * Component props interface
 */
export interface Props {
  value: ExecContent;  // Execution content containing request/response data
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

/**
 * Computed: Panel configuration array
 * Defines the three collapsible sections:
 * 1. General information (URL, method, status)
 * 2. Request headers
 * 3. Response headers
 * 
 * @returns Array of panel configurations
 */
const panels = computed<PanelConfig[]>(() => [
  {
    id: utils.uuid(),
    name: t('common.general'),
    key: 'general'
  },
  {
    id: utils.uuid(),
    name: t('protocol.requestHeader'),
    key: 'request'
  },
  {
    id: utils.uuid(),
    name: t('protocol.responseHeader'),
    key: 'response'
  }
]);

/**
 * Ref: Active panel keys
 * Tracks which panels are currently expanded
 */
const activeKeys = ref<string[]>([]);

/**
 * Toggle panel expansion state
 * 
 * @param id - Panel ID to toggle
 */
const arrowChange = (id: string): void => {
  if (activeKeys.value.includes(id)) {
    // Panel is open, close it
    activeKeys.value = activeKeys.value.filter(item => item !== id);
  } else {
    // Panel is closed, open it
    activeKeys.value.push(id);
  }
};

/**
 * Computed: Full request URL with query string
 * Constructs complete URL by appending query string if present
 * 
 * @returns Full URL with query parameters
 */
const url = computed(() => {
  const url = props.value?.content?.request0?.url;
  const queryString = props.value?.content?.request0?.queryString;
  
  if (queryString) {
    return url + '?' + queryString;
  }

  return url;
});

/**
 * Computed: HTTP request method
 * Extracts HTTP method (GET, POST, PUT, DELETE, etc.)
 * 
 * @returns HTTP method string
 */
const method = computed(() => {
  return props.value?.content?.request0?.method;
});

/**
 * Computed: HTTP response status code
 * Converts status to number, returns empty string if invalid
 * 
 * @returns Numeric status code or empty string
 */
const statusCode = computed(() => {
  return +props.value?.content?.response?.status || '';
});

/**
 * Computed: Request headers as key-value pairs
 * Transforms flat header array [name1, value1, name2, value2, ...]
 * into structured objects [{name, value}, ...]
 * 
 * Header array format:
 * - Even indices (0, 2, 4...): header names
 * - Odd indices (1, 3, 5...): header values
 * 
 * @returns Array of header objects with id, name, and value
 */
const requestHeaders = computed<HeaderItem[]>(() => {
  const headers = props.value?.content?.request0?.headerArray;
  if (!headers?.length) {
    return [];
  }

  return headers.reduce((prev, cur, index) => {
    if (index % 2 === 0) {
      // Even index: header name
      prev.push({
        id: utils.uuid(),
        name: cur,
        value: ''
      });
    } else {
      // Odd index: header value
      prev[prev.length - 1].value = cur;
    }

    return prev;
  }, [] as HeaderItem[]);
});

/**
 * Computed: Response headers as key-value pairs
 * Transforms flat header array [name1, value1, name2, value2, ...]
 * into structured objects [{name, value}, ...]
 * 
 * Header array format:
 * - Even indices (0, 2, 4...): header names
 * - Odd indices (1, 3, 5...): header values
 * 
 * @returns Array of header objects with id, name, and value
 */
const responseHeaders = computed<HeaderItem[]>(() => {
  const headers = props.value?.content?.response?.headerArray;
  if (!headers?.length) {
    return [];
  }

  return headers.reduce((prev, cur, index) => {
    if (index % 2 === 0) {
      // Even index: header name
      prev.push({
        id: utils.uuid(),
        name: cur,
        value: ''
      });
    } else {
      // Odd index: header value
      prev[prev.length - 1].value = cur;
    }

    return prev;
  }, [] as HeaderItem[]);
});
</script>

<template>
  <!--
    Main collapse component container
    - Manages collapsible header panels
    - White background with 12px font size
    - No border for clean appearance
  -->
  <Collapse
    :activeKey="activeKeys"
    :bordered="false"
    style="background-color: #fff;font-size: 12px;"
    class="tabs-collapse">
    
    <!-- 
      Collapse panel for each section (general, request headers, response headers)
      - Arrow hidden by default, custom arrow in header slot
      - Collapsible disabled to use custom toggle logic
    -->
    <CollapsePanel
      v-for="item in panels"
      :key="item.id"
      :showArrow="false"
      collapsible="disabled">
      
      <!-- Custom panel header with expand/collapse arrow -->
      <template #header>
        <div class="w-full flex items-center">
          <!-- Custom arrow component for expand/collapse -->
          <Arrow 
            :open="activeKeys.includes(item.id)" 
            @change="arrowChange(item.id)" />
          <!-- Panel title -->
          <div class="ml-1 font-bold">{{ item.name }}</div>
        </div>
      </template>
      
      <!-- General information panel: URL, method, and status code -->
      <template v-if="item.key==='general'">
        <div class="pb-2 pl-2 pt-1 space-y-1">
          <!-- Request URL with query string -->
          <div class="flex items-start">
            <div class="w-55">Request URL<Colon /></div>
            <div class="flex-1 whitespace-pre-wrap break-words break-all">{{ url }}</div>
          </div>
          
          <!-- HTTP request method (GET, POST, etc.) -->
          <div class="flex items-start">
            <div class="w-55">Request Method<Colon /></div>
            <div class="flex-1 whitespace-pre-wrap break-words break-all">{{ method }}</div>
          </div>
          
          <!-- HTTP response status code with color indicator -->
          <div class="flex items-start">
            <div class="w-55">Status Code<Colon /></div>
            <div class="flex items-center flex-1 whitespace-pre-wrap break-words break-all">
              <!-- Green indicator for 2xx success status -->
              <span
                v-if="statusCode>=200&&statusCode<=299"
                class="inline-block w-3 h-3 rounded-lg mr-1 transform-gpu -translate-y-0.25"
                style="background-color: rgba(3, 206, 92,100%);"></span>
              <!-- Red indicator for 4xx/5xx error status -->
              <span 
                v-else-if="statusCode>=400&&statusCode<=599" 
                class="inline-block w-3 h-3 rounded-lg mr-1 bg-red-500 transform-gpu -translate-y-0.25"></span>
              <!-- Status code number -->
              <span>{{ statusCode }}</span>
            </div>
          </div>
        </div>
      </template>
      
      <!-- Request headers panel: All HTTP request headers -->
      <template v-else-if="item.key==='request'">
        <!-- Request headers list -->
        <div v-if="requestHeaders.length" class="pb-2 pl-2 pt-1 space-y-1">
          <div
            v-for="header in requestHeaders"
            :key="header.id"
            class="flex items-start">
            <!-- Header name -->
            <div class="w-55">{{ header.name }}<Colon /></div>
            <!-- Header value with word wrapping -->
            <div class="flex-1 whitespace-pre-wrap break-words break-all">{{ header.value }}</div>
          </div>
        </div>
        <!-- Empty state when no request headers -->
        <NoData v-else size="small" />
      </template>
      
      <!-- Response headers panel: All HTTP response headers -->
      <template v-else-if="item.key==='response'">
        <!-- Response headers list -->
        <div v-if="responseHeaders.length" class="pb-2 pl-2 pt-1 space-y-1">
          <div
            v-for="header in responseHeaders"
            :key="header.id"
            class="flex items-start">
            <!-- Header name -->
            <div class="w-55">{{ header.name }}<Colon /></div>
            <!-- Header value with word wrapping -->
            <div class="flex-1 whitespace-pre-wrap break-words break-all">{{ header.value }}</div>
          </div>
        </div>
        <!-- Empty state when no response headers -->
        <NoData v-else size="small" />
      </template>
    </CollapsePanel>
  </Collapse>
</template>

<style scoped>
.ant-collapse.tabs-collapse > :deep(.ant-collapse-item) {
  border: none;
}

.ant-collapse.tabs-collapse > :deep(.ant-collapse-item) .ant-collapse-header{
  padding: 6px 0;
}
</style>
