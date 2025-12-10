<script setup lang="ts">
import { computed } from 'vue';
import { NoData } from '@xcan-angus/vue-ui';
// @ts-ignore - FormatHighlight is available but type definition may be missing
import { FormatHighlight } from '@xcan-angus/vue-ui';

import { ExecContent } from '@/plugins/test/types';

/**
 * Component props interface
 */
export interface Props {
  value: ExecContent;  // Execution content containing request data
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

/**
 * Computed: Form data from request
 * Extracts form data from the first request (request0)
 * Used for form submissions and multipart data
 * 
 * @returns Form data object or undefined
 */
const forms = computed(() => {
  return props.value?.content?.request0?.forms;
});

/**
 * Computed: Raw body content from request
 * Extracts raw body content from the first request (request0)
 * Used for plain text, JSON, XML, or binary data
 * 
 * @returns Raw body content string or undefined
 */
const rawContent = computed(() => {
  return props.value?.content?.request0?.body;
});
</script>

<template>
  <!-- Empty state: no request body content -->
  <template v-if="!forms && !rawContent">
    <NoData size="small" class="my-10" />
  </template>
  
  <!-- Form data: display as formatted JSON -->
  <template v-else-if="forms">
    <div class="pt-4.75 pb-4 pl-0.5">
      <FormatHighlight
        :dataSource="value?.content?.request0?.forms"
        dataType="json"
        format="preview" />
    </div>
  </template>
  
  <!-- Raw content: display as plain text -->
  <template v-else-if="rawContent">
    <div class="pt-4.75 pb-4 pl-0.5">
      <FormatHighlight
        :dataSource="rawContent"
        dataType="plain"
        format="preview" />
    </div>
  </template>
</template>
