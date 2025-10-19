
<script lang="ts" setup>
import { ref, watch, computed } from 'vue';
import { Select, Icon, Arrow, Colon, Tooltip } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { getDataByProxy } from '@/api/proxy';

/**
 * Execution node interface
 */
interface ExecNode {
  id: string;     // Node unique identifier
  name: string;   // Node display name
  ip: string;     // Node IP address
  port?: string;  // Agent service port (optional)
}

/**
 * Scheduling result interface
 */
interface SchedulingResult {
  console: string[];   // Console output lines
  deviceId: string;    // Device/node identifier
  success: boolean;    // Whether execution succeeded
  exitCode: string;    // Exit code from execution
}

/**
 * Component props interface
 */
interface Props {
  execId: string;                       // Execution unique identifier
  execNodes: ExecNode[];                // Available execution nodes
  lastSchedulingDate: string;           // Last scheduling timestamp
  lastSchedulingResult: SchedulingResult[];  // Scheduling results from nodes
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  execId: '',
  execNodes: () => [],
  lastSchedulingDate: '',
  lastSchedulingResult: () => []
});

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Event emitters
 */
const emit = defineEmits<{
  (e: 'update:loading', value: boolean): void;  // Emit loading state changes
}>();

/**
 * Node connection state
 */
const nodeId = ref<string>();            // Currently selected node ID
const nodeIp = ref<string>();            // Selected node IP address
const nodePort = ref<string>('6807');    // Agent service port (default: 6807)

/**
 * Execution log state
 */
const execLogContent = ref<string>();    // Raw execution log content
const execLogPath = ref<string>('');     // Path to log file on agent
const execLogErr = ref(false);           // Whether log fetch failed

/**
 * Load execution log from the agent node
 * Fetches log content via proxy API from the selected node
 */
const loadExecLog = async (): Promise<void> => {
  emit('update:loading', true);
  
  // Fetch log via proxy with node agent URL
  const [error, res] = await getDataByProxy(
    `http://${nodeIp.value}:${nodePort.value || '6807'}/actuator/runner/log/${props.execId}`
  );
  
  emit('update:loading', false);
  
  // Handle error
  if (error) {
    execLogErr.value = true;
    return;
  }

  // Extract log path from headers and content from response
  execLogPath.value = res.headers?.['xc-agent-log-path'];
  execLogContent.value = res?.data;
};

/**
 * Get scheduling log item for currently selected node
 * Finds the scheduling result matching the selected node, or returns default item
 * 
 * @returns Scheduling result for selected node or undefined
 */
const schedulingLogItem = computed<SchedulingResult | undefined>(() => {
  if (props.lastSchedulingResult.length) {
    // Try to find result for current node
    const foundItem = props.lastSchedulingResult.find(
      item => item.deviceId === nodeId.value
    );
    
    if (foundItem) {
      return foundItem;
    } else {
      // Fall back to result without device ID (default/total)
      const emptyItem = props.lastSchedulingResult.find(item => !item?.deviceId);
      if (emptyItem) {
        return emptyItem;
      }

      return undefined;
    }
  }

  return undefined;
});

/**
 * Handle node selection change
 * Updates selected node and reloads execution log
 * 
 * @param _nodeId - Selected node ID (from Select component)
 */
const nodeSelectChange = (_nodeId: any): void => {
  if (!_nodeId) return;
  nodeId.value = String(_nodeId);
  loadExecLog();
};

/**
 * Watch for execId changes
 * Initializes node selection and loads log when execution ID changes
 */
watch(() => props.execId, (newValue) => {
  if (newValue) {
    // Select first node by default
    nodeId.value = props.execNodes[0]?.id;
    nodeIp.value = props.execNodes[0]?.ip;
    nodePort.value = props.execNodes[0]?.port || '6807';
    loadExecLog();
  }
}, {
  immediate: true  // Execute immediately on mount
});

/**
 * Log section visibility state
 */
const showSchedulingLog = ref(true);   // Scheduling log section expanded
const showExecLog = ref(false);        // Execution log section collapsed by default

/**
 * Log type for toggle operations
 */
type LogType = 'scheduling' | 'exec';

/**
 * Toggle log section visibility
 * Expands or collapses the specified log section
 * 
 * @param type - Log section type to toggle
 */
const handleDoubleClick = (type: LogType): void => {
  if (type === 'exec') {
    showExecLog.value = !showExecLog.value;
    return;
  }
  showSchedulingLog.value = !showSchedulingLog.value;
};

/**
 * Download log content as a text file
 * Creates a blob and triggers browser download
 * 
 * @param type - Log type to download (scheduling or exec)
 */
const downloadLog = (type: LogType): void => {
  // Get appropriate log content based on type
  const content = type === 'exec' 
    ? execLogContent.value 
    : schedulingLogItem.value?.console.join('\n');
  
  // Early return if no content
  if (!content) {
    return;
  }

  // Create blob from log content
  const blob = new Blob([content], {
    type: 'application/text'
  });

  // Create object URL for the blob
  const url = URL.createObjectURL(blob);

  // Create temporary anchor element for download
  const anchor = document.createElement('a');
  anchor.style.display = 'none';
  anchor.href = url;
  anchor.download = type === 'exec' 
    ? t('ftpPlugin.performanceTestDetail.execLog.execLog.downloadFileName') 
    : t('ftpPlugin.performanceTestDetail.execLog.downloadFileName.scheduling');
  
  // Trigger download
  document.body.appendChild(anchor);
  anchor.click();
  
  // Cleanup: remove anchor and revoke URL
  document.body.removeChild(anchor);
  window.URL.revokeObjectURL(url);
};

/**
 * Refresh execution log content
 * Reloads log from agent node
 * 
 * @param event - Click event
 */
const refreshExecLog = (event: Event): void => {
  event.preventDefault();
  loadExecLog();
};
</script>

<template>
  <!-- Main container for log viewer -->
  <div class="h-full space-y-2">
    <!-- Node selector dropdown -->
    <Select
      :value="nodeId"
      :options="props.execNodes"
      :fieldNames="{ label: 'name', value: 'id' }"
      class="w-80"
      :placeholder="t('ftpPlugin.performanceTestDetail.execLog.selectNode')"
      size="small"
      @change="nodeSelectChange">
      <!-- Custom option template showing name and IP -->
      <template #option="item">
        {{ `${item.name} ( ${item.ip} )` }}
      </template>
    </Select>
    
    <!-- Scheduling log section (collapsible) -->
    <div 
      class="flex flex-col border border-divider rounded transition-height duration-500 overflow-hidden" 
      :class="showSchedulingLog ? 'open-info' : 'stop-info'">
      <!-- Scheduling log header with metadata and actions -->
      <div
        class="h-8.5 flex flex-none justify-between items-center text-3 px-3.5 text-text-content border-b border-theme-divider cursor-pointer"
        style="background-color:rgb(252, 253, 255);"
        @click="handleDoubleClick('scheduling')">
        <!-- Left side: title and metadata -->
        <div class="flex flex-1 items-center space-x-20">
          <!-- Section title -->
          <span class="text-text-title font-medium">
            {{ t('ftpPlugin.performanceTestDetail.execLog.schedulingLog.title') }}
          </span>
          
          <!-- Last scheduling time -->
          <div>
            <span class="mr-2">
              {{ t('ftpPlugin.performanceTestDetail.execLog.schedulingLog.lastSchedulingTime') }}
              <Colon />
            </span>
            <span>{{ props.lastSchedulingDate }}</span>
          </div>
          
          <!-- Execution result indicator -->
          <div class="flex items-center">
            <span class="mr-2">
              {{ t('ftpPlugin.performanceTestDetail.execLog.schedulingLog.result') }}
              <Colon />
            </span>
            <div class="flex items-center whitespace-nowrap">
              <!-- Success/failure status with colored indicator -->
              <template v-if="schedulingLogItem">
                <div 
                  class="w-1.5 h-1.5 mr-1 rounded -mt-0.5" 
                  :class="schedulingLogItem?.success ? 'bg-status-success' : 'bg-status-error'">
                </div>
                {{ schedulingLogItem?.success ? t('status.success') : t('status.failed') }}
              </template>
              <template v-else>
                {{ t('common.noData') }}
              </template>
            </div>
          </div>
          
          <!-- Exit code -->
          <div>
            <span class="mr-2">{{ t('common.exitCode') }}<Colon /></span>
            <span>{{ schedulingLogItem?.exitCode }}</span>
          </div>
        </div>
        
        <!-- Right side: action buttons -->
        <div>
          <!-- Export button -->
          <Tooltip :title="t('actions.export')" placement="top">
            <Icon
              icon="icon-daochu1"
              class="text-4 cursor-pointer hover:text-text-link-hover mr-2"
              @click="downloadLog('scheduling')" />
          </Tooltip>
          
          <!-- Expand/collapse arrow -->
          <Arrow v-model:open="showSchedulingLog" />
        </div>
      </div>
      
      <!-- Scheduling log content area -->
      <div
        class="bg-log-bg whitespace-pre-wrap p-3.5 leading-4 flex-1 overflow-y-auto text-log-text text-3"
        style="scrollbar-gutter: stable;font-family: Monaco, Consolas, monospace !important;">
        {{ schedulingLogItem ? schedulingLogItem.console?.join('\n') : '' }}
      </div>
    </div>
    
    <!-- Execution log section (collapsible) -->
    <div
      class="flex flex-col border border-divider rounded transition-height duration-500 overflow-hidden"
      :class="showExecLog ? 'open-info' : 'stop-info'">
      <!-- Execution log header with file path and actions -->
      <div
        class="h-8.5 flex flex-none justify-between items-center text-3 px-3.5 text-text-content cursor-pointer"
        style="background-color:rgb(252, 253, 255);"
        @click="handleDoubleClick('exec')">
        <!-- Left side: title and file path -->
        <div class="flex flex-1 items-center space-x-20">
          <!-- Section title -->
          <span class="text-text-title font-medium">
            {{ t('ftpPlugin.performanceTestDetail.execLog.execLog.title') }}
          </span>
          
          <!-- Log file path (when successfully loaded) -->
          <template v-if="!execLogErr && execLogPath">
            <div>
              <span class="mr-2">
                {{ t('ftpPlugin.performanceTestDetail.execLog.execLog.file') }}
                <Colon />
              </span>
              <span>{{ execLogPath }}</span>
            </div>
          </template>
          
          <!-- Error message with proxy URL (when failed to load) -->
          <template v-else>
            <span class="text-rule">
              {{ t('ftpPlugin.performanceTestDetail.execLog.execLog.noProxyInfo') }}
              {{ `http://${nodeIp}:6807/actuator/runner/log/${props.execId}` }}
            </span>
          </template>
        </div>
        
        <!-- Right side: action buttons -->
        <div>
          <!-- Export button -->
          <Tooltip :title="t('actions.export')" placement="top">
            <Icon
              icon="icon-daochu1"
              class="text-4 cursor-pointer hover:text-text-link-hover mr-2"
              @click="downloadLog('exec')" />
          </Tooltip>
          
          <!-- Refresh button -->
          <Tooltip :title="t('actions.refresh')" placement="top">
            <Icon
              icon="icon-shuaxin"
              class="text-4 cursor-pointer hover:text-text-link-hover mr-2"
              @click.stop="refreshExecLog" />
          </Tooltip>
          
          <!-- Expand/collapse arrow -->
          <Arrow v-model:open="showExecLog" />
        </div>
      </div>
      
      <!-- Execution log content area -->
      <div
        class="bg-log-bg whitespace-pre-wrap p-3.5 leading-4 flex-1 overflow-y-auto text-log-text text-3"
        style="scrollbar-gutter: stable;font-family: Monaco, Consolas, monospace !important;">
        {{ execLogContent }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.open-info {
  height: calc(100% - 80px);
}

.stop-info {
  height: 34px;
}
</style>
