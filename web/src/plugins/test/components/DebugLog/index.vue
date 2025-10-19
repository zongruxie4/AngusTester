
<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, NoData, IconDownload } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Log item interface for rendering individual console entries
 */
interface LogItem {
  id: string;
  log: string;
}

/**
 * Debug log data structure
 */
interface DebugLogValue {
  success: boolean;    // Execution success status
  exitCode: string;    // Exit code from the execution
  console: string[];   // Array of console log messages
}

/**
 * Component props interface
 */
interface Props {
  value?: DebugLogValue; // Debug log data (optional)
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

/**
 * Transforms console array into renderable log items with unique IDs
 * Returns empty array if no console logs are available
 */
const showConsole = computed<LogItem[]>(() => {
  if (!props.value?.console?.length) {
    return [];
  }

  return props.value.console.map(item => ({
    id: utils.uuid(),
    log: item
  }));
});


/**
 * Downloads the console log as a text file
 * Creates a temporary anchor element to trigger download
 */
const downloadLog = (): void => {
  const content = props.value?.console?.join('\n');
  
  // Early return if no content to download
  if (!content) {
    return;
  }

  const fileName = t('commonPlugin.debugLog.downloadFileName');
  
  // Create a blob file with log content
  const file = new File([content], fileName, {
    type: 'text/plain'
  });
  
  // Create object URL for the file
  const url = URL.createObjectURL(file);
  
  // Create temporary anchor element for download
  const anchor = document.createElement('a');
  anchor.style.display = 'none';
  anchor.href = url;
  anchor.download = fileName;
  
  // Trigger download
  document.body.appendChild(anchor);
  anchor.click();
  
  // Cleanup: remove anchor and revoke URL
  document.body.removeChild(anchor);
  window.URL.revokeObjectURL(url);
};
</script>

<template>
  <!-- Main container for debug log display -->
  <div class="h-full px-5 py-3 text-3">
    <!-- Debug log content (when data is available) -->
    <template v-if="!!props.value">
      <!-- Header section: execution status, exit code, and download button -->
      <div class="flex items-center leading-5 mb-2.5">
        <!-- Execution result status indicator -->
        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">{{ t('common.scheduleResult') }}</span>
          <Colon class="mr-2" />
          
          <!-- Success status indicator -->
          <template v-if="props.value?.success">
            <span class="inline-block w-1.5 h-1.5 mr-1 rounded bg-status-success"></span>
            <span>{{ t('status.success') }}</span>
          </template>
          
          <!-- Failure status indicator -->
          <template v-else>
            <span class="inline-block w-1.5 h-1.5 mr-1 rounded bg-status-error"></span>
            <span>{{ t('status.failed') }}</span>
          </template>
        </div>
        
        <!-- Exit code display -->
        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">{{ t('common.exitCode') }}</span>
          <Colon class="mr-2" />
          <span>{{ props.value?.exitCode }}</span>
        </div>
        
        <!-- Download log button -->
        <IconDownload 
          class="text-4 cursor-pointer" 
          @click="downloadLog" 
        />
      </div>
      
      <!-- Console log content area -->
      <div 
        class="px-3.5 py-2.5 space-y-1 rounded border border-solid border-theme-text-box overflow-auto"
        style="height: calc(100% - 30px);">
        <div
          v-for="item in showConsole"
          :key="item.id"
          class="log-item">
          {{ item.log }}
        </div>
      </div>
    </template>
    
    <!-- No data placeholder (when no debug log data) -->
    <NoData v-else size="small" />
  </div>
</template>

<style scoped>
.log-item {
  word-break: break-all;
  white-space: pre-wrap;
  transition: background-color 0.2s ease;
}

.log-item:hover {
  background-color: rgba(0, 0, 0, 0.02);
}
</style>
