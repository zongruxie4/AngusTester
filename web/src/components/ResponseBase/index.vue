<script setup lang="ts">
import { watch, ref, onMounted, nextTick } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';

import { ResponseState } from 'src/utils/apis';
import { baseSource, getStatusText } from './interface';

/**
 * Component props interface
 */
interface Props {
  dataSource: ResponseState;  // HTTP response state data to display
}

/**
 * Request information item interface
 * Represents a collapsible section of request/response information
 */
interface RequestItem {
  name: string;                              // Section display name
  value: Array<Record<string, string>>;      // Array of key-value pairs in this section
  spread: boolean;                           // Whether this section is expanded
  type?: string;                             // Optional type information
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {});

/**
 * Emitted events interface
 */
const emit = defineEmits<{
  (e: 'rendered', value: true): void;  // Emitted when component is fully rendered
}>();

/**
 * Ref: Request data sections to render
 * Defines the structure and initial state of collapsible information sections
 * Each section represents a different aspect of the HTTP request/response
 */
const sourceData = ref<RequestItem[]>([
  { name: 'General', value: [], spread: true },           // Basic request/response info
  { name: 'Query Parameters', value: [], spread: true },  // URL query parameters
  { name: 'Request Headers', value: [], spread: true },   // HTTP request headers
  { name: 'Response Headers', value: [], spread: true, type: '' }, // HTTP response headers
  { name: 'Raw', value: [], spread: true }                // Request body content
]);

/**
 * Watch: Process response data when dataSource changes
 * Parses and organizes HTTP response data into structured sections
 * Handles URL decoding, header processing, and content formatting
 */
watch(() => props.dataSource, (newValue) => {
  // Clear existing data in all sections
  sourceData.value.forEach(i => {
    i.value = [];
  });
  
  const { status, config = {}, headers, requestHeaders } = newValue || {};

  let value = '';
  
  // Process general/basic information
  baseSource.forEach(item => {
    if (item.key === 'statusText') {
      // Get human-readable status text from status code
      value = status ? getStatusText(status) : '';
    } else if (item.key === 'url') {
      // Use URL as-is (no additional encoding needed)
      value = config[item.key];
    } else {
      // Use other config values directly
      value = config[item.key];
    }
    
    // Add to General section
    sourceData.value[0].value.push({
      value,
      name: item.name
    });
  });

  // Process query string parameters
  if (config.queryString) {
    const querys = config.queryString?.split('&');
    for (const query of querys) {
      const [key, value] = query.split('=');
      // URL decode both key and value
      sourceData.value[1].value.push({ 
        name: decodeURIComponent(key), 
        value: decodeURIComponent(value) 
      });
    }
  }

  // Process request headers
  if (requestHeaders) {
    for (const head in requestHeaders) {
      if (requestHeaders[head]) {
        // URL decode header values
        sourceData.value[2].value.push({ 
          name: head || '', 
          value: decodeURIComponent(String(requestHeaders[head])) 
        });
      }
    }
  }

  // Process response headers
  if (headers && Object.keys(headers).length) {
    for (const key in headers) {
      if (Object.prototype.hasOwnProperty.call(headers, key)) {
        if (typeof headers[key] === 'string' || !headers[key]) {
          // Single string value
          sourceData.value[3].value.push({ name: key, value: headers[key] });
        } else {
          // Array of values - create separate entries for each
          sourceData.value[3].value.push(...headers[key].map(i => ({ name: key, value: i })));
        }
      }
    }
  }

  // Process request body content
  if (config.forms) {
    // Form data - change section name and add form fields
    sourceData.value[4].name = 'Form';
    for (const key in config.forms) {
      sourceData.value[4].value.push({ name: key, value: config.forms[key] });
    }
  }
  
  if (config.body) {
    // Raw body content - change section name and add raw content
    sourceData.value[4].name = 'Raw';
    sourceData.value[4].value.push({ name: '', value: config.body });
  }
}, {
  immediate: true,  // Execute immediately on mount
  deep: true        // Watch for deep changes in nested objects
});

/**
 * Toggle section expand/collapse state
 * Switches the spread state of a specific section by index
 * 
 * @param idx - Index of the section to toggle
 */
const handleSpread = (idx: number): void => {
  sourceData.value[idx].spread = !sourceData.value[idx].spread;
};

/**
 * Component mounted lifecycle hook
 * Emits rendered event after component is fully mounted
 */
onMounted(() => {
  nextTick(() => {
    emit('rendered', true);
  });
});
</script>
<template>
  <!-- Response Base Component Container -->
  <div class="h-full py-3 overflow-auto">
    <!-- 
      Information Sections Loop
      - Renders each section of request/response information
      - Only displays sections that have data (item.value.length > 0)
      - Each section is collapsible with expand/collapse functionality
    -->
    <div
      v-for="(item, idx) in sourceData"
      :key="item.name"
      class="mb-1">
      
      <!-- Section Content (only show if has data) -->
      <template v-if="item.value.length">
        <!-- 
          Section Header
          - Clickable header with expand/collapse icon
          - Icon rotates based on spread state
          - Shows section name
        -->
        <div class="py-2 text-theme-title text-3 leading-3.5 flex items-center">
          <span @click="handleSpread(idx)">
            <Icon
              icon="icon-xiangxia"
              class="transition-all mr-2 align-top"
              :class="{'-rotate-90': !item.spread}" />
          </span>
          <span>{{ item.name }}</span>
        </div>
        
        <!-- Optional Type Information -->
        <div v-if="item.type" class="text-theme-title text-3 pl-6.5">{{ item.type }}</div>
        
        <!-- 
          Section Content Items
          - Key-value pairs within each section
          - Only visible when section is expanded (item.spread)
          - Properly formatted with name: value layout
          - Supports line breaks and text wrapping
        -->
        <div
          v-for="(subItem,index) in item.value"
          v-show="item.spread"
          :key="index"
          class="flex items-center flex-wrap whitespace-pre-wrap mb-1.5 leading-4 break-all text-3 last:mb-0 pl-6.5 ">
          
          <!-- Key Name (if exists) -->
          <span v-if="subItem.name" class="inline-block mr-1.5 text-theme-sub-content">{{ subItem.name }}:</span>
          
          <!-- Value Content -->
          <span class="inline-block text-theme-content">{{ subItem.value }}</span>
        </div>
      </template>
    </div>
  </div>
</template>
