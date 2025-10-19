
<script setup lang="ts">
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, NoData, IconDownload, Spin } from '@xcan-angus/vue-ui';
import { getDataByProxy } from '@/api/proxy';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Execution node information interface
 */
interface ExecNode {
  id: string;         // Node unique identifier
  name: string;       // Node display name
  ip: string;         // Node IP address
  agentPort: string;  // Agent service port
  publicIp: string;   // Public IP address (if available)
}

/**
 * Scheduling result interface
 */
interface SchedulingResult {
  success: boolean;   // Whether execution succeeded
  exitCode: string;   // Exit code from execution
  console: string[];  // Console output lines
}

/**
 * Component props interface
 */
interface Props {
  execId: string;                        // Execution unique identifier
  execNode?: ExecNode;                   // Node where execution ran
  schedulingResult?: SchedulingResult;   // Execution scheduling result
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  execId: '',
  execNode: undefined,
  schedulingResult: undefined
});

/**
 * Node connection reactive references
 */
const nodeId = ref<string>();                    // Current node ID
const nodeIp = ref<string>();                    // Node IP address (public or private)
const nodePort = ref<string>('6807');            // Agent service port (default: 6807)

/**
 * Execution log state
 */
const execLogContent = ref<string>();            // Raw log content from agent
const execLogPath = ref<string>('');             // Path to log file on agent
const execLogErr = ref<boolean>(false);          // Whether log fetch failed
const errorText = ref<string>();                 // Error message if fetch failed

/**
 * Loading state (initially true if execNode is provided)
 */
const loading = ref<boolean>(!!props.execNode?.id);

/**
 * Fetch execution log from the agent node
 * Makes a proxy request to the agent's actuator endpoint
 * Handles both success and error cases
 */
const loadExecLog = async (): Promise<void> => {
  loading.value = true;
  
  // Fetch log content via proxy with no timeout
  const [error, res] = await getDataByProxy(
    `http://${nodeIp.value}:${nodePort.value}/actuator/runner/log/${props.execId}`,
    {},
    { timeout: 0 }
  );
  
  loading.value = false;
  
  // Handle error response
  if (error) {
    execLogErr.value = true;
    // Type assertion for axios-like error with response property
    const axiosError = error as any;
    if (axiosError.response?.data) {
      errorText.value = axiosError.response.data;
    } else {
      errorText.value = undefined;
    }
    return;
  }

  // Handle success response
  execLogErr.value = false;
  execLogPath.value = res.headers?.['xc-agent-log-path'];
  execLogContent.value = res?.data || '';
};

/**
 * Watch for execNode prop changes
 * When a new node is provided, extract connection details and load the log
 */
watch(() => props.execNode, (newValue) => {
  if (newValue && newValue?.id) {
    nodeId.value = newValue.id;
    // Prefer public IP over private IP
    nodeIp.value = newValue?.publicIp || newValue.ip;
    // Use provided port or default to 6807
    nodePort.value = newValue.agentPort || '6807';
    // Fetch log content from the node
    loadExecLog();
  }
}, {
  deep: true,      // Watch nested properties
  immediate: true  // Execute immediately on mount
});

/**
 * Download execution log as a text file
 * Creates a blob from log content and triggers browser download
 */
const downloadLog = (): void => {
  const content = execLogContent.value;
  
  // Early return if no content to download
  if (!content) {
    return;
  }

  // Create blob from log content
  const blob = new Blob([content], {
    type: 'text/plain'
  });

  // Create object URL for the blob
  const url = URL.createObjectURL(blob);

  // Create temporary anchor element for download
  const anchor = document.createElement('a');
  anchor.style.display = 'none';
  anchor.href = url;
  anchor.download = t('commonPlugin.execLog.downloadFileName');
  
  // Trigger download
  document.body.appendChild(anchor);
  anchor.click();
  
  // Cleanup: remove anchor and revoke URL
  document.body.removeChild(anchor);
  window.URL.revokeObjectURL(url);
};

</script>

<template>
  <!-- Spinning wrapper for loading state -->
  <Spin
    class="flex-1 px-5 py-3 h-full overflow-auto"
    :spinning="loading"
    :delay="0">
    
    <!-- Main log content (displayed when node exists) -->
    <div v-if="!!props.execNode?.id" class="h-full text-3">
      <!-- Header section: node info, status, exit code, and download button -->
      <div class="flex items-center leading-5 mb-2.5">
        <!-- Node information -->
        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">{{ t('common.node') }}</span>
          <Colon class="mr-2" />
          <span>{{ props.execNode.name }}({{ props.execNode.publicIp || props.execNode.ip }})</span>
        </div>
        
        <!-- Scheduling result status indicator -->
        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">{{ t('common.scheduleResult') }}</span>
          <Colon class="mr-2" />
          
          <!-- Success status -->
          <template v-if="props.schedulingResult?.success">
            <span class="inline-block w-1.5 h-1.5 mr-1 rounded bg-status-success"></span>
            <span>{{ t('status.success') }}</span>
          </template>
          
          <!-- Failure status -->
          <template v-else>
            <span class="inline-block w-1.5 h-1.5 mr-1 rounded bg-status-error"></span>
            <span>{{ t('status.failed') }}</span>
          </template>
        </div>
        
        <!-- Exit code display -->
        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">{{ t('common.exitCode') }}</span>
          <Colon class="mr-2" />
          <span>{{ props.schedulingResult?.exitCode }}</span>
        </div>
        
        <!-- Download log button -->
        <IconDownload class="text-4 cursor-pointer" @click="downloadLog" />
      </div>
      
      <!-- Log content display area -->
      <div 
        style="height:calc(100% - 30px);" 
        class="px-3.5 py-2.5 space-y-1 rounded border border-solid border-theme-text-box overflow-auto whitespace-pre-wrap">
        {{ execLogContent }}
      </div>
    </div>
    
    <!-- No data placeholder (when no node is selected) -->
    <NoData v-if="!loading && !props.execNode?.id" size="small" />
  </Spin>
</template>
