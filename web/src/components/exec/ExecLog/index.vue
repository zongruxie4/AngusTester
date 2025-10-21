<script setup lang="ts">
// Vue core imports
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Colon, NoData, IconDownload, Spin } from '@xcan-angus/vue-ui';

// Infrastructure imports
import { http, PUB_TESTER } from '@xcan-angus/infra';

const { t } = useI18n();

/**
 * Execution node interface
 */
interface ExecNode {
  id: string;
  name: string;
  ip: string;
  agentPort: string;
  publicIp: string;
}

/**
 * Scheduling result interface
 */
interface SchedulingResult {
  success: boolean;
  exitCode: string;
  console: string[];
}

/**
 * Component props interface for execution log
 */
interface Props {
  execId: string;
  execNode: ExecNode;
  schedulingResult: SchedulingResult;
  sampleLogContent?: string;
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  execId: '',
  execNode: undefined,
  schedulingResult: undefined
});

// Component state
const selectedNodeId = ref<string>();
const selectedNodeIp = ref<string>();
const selectedNodePort = ref<string>('6807');

const executionLogContent = ref();
const executionLogPath = ref('');
const hasExecutionLogError = ref(false);
const errorMessage = ref();

const isLoading = ref(!!props.execNode?.id);
/**
 * Load execution log content from the target node
 */
const loadExecutionLogContent = async () => {
  const logUrl = `${PUB_TESTER}/proxy/actuator/runner/log/${props.execId}?targetAddr=http://${selectedNodeIp.value}:${selectedNodePort.value}`;
  isLoading.value = true;
  const [error, response] = await http.get(logUrl, {}, { timeout: 0 });
  isLoading.value = false;
  if (error) {
    hasExecutionLogError.value = true;
    if (error.response?.data) {
      errorMessage.value = error.response.data;
    } else {
      errorMessage.value = undefined;
    }
    return;
  }

  hasExecutionLogError.value = false;
  executionLogPath.value = response.headers?.['xc-agent-log-path'];
  executionLogContent.value = response?.data || '';
};

// Watchers
watch(() => props.execNode, (newExecNode) => {
  if (newExecNode && newExecNode?.id) {
    selectedNodeId.value = newExecNode.id;
    selectedNodeIp.value = newExecNode?.publicIp || newExecNode.ip;
    selectedNodePort.value = newExecNode.agentPort || '6807';
    loadExecutionLogContent();
  }
}, {
  deep: true,
  immediate: true
});

/**
 * Handle log file download
 */
const handleLogFileDownload = () => {
  const logContent = executionLogContent.value || props.sampleLogContent;
  if (!logContent) {
    return;
  }

  const logBlob = new Blob([logContent], {
    type: 'text/plain'
  });

  const downloadUrl = URL.createObjectURL(logBlob);

  const downloadLink = document.createElement('a');
  downloadLink.style.display = 'none';
  downloadLink.href = downloadUrl;
  downloadLink.download = 'runner.log';
  document.body.appendChild(downloadLink);
  downloadLink.click();
  document.body.removeChild(downloadLink);

  window.URL.revokeObjectURL(downloadUrl);
};

</script>

<template>
  <Spin
    class="flex-1 px-5 py-3 h-full overflow-auto"
    :spinning="isLoading"
    :delay="0">
    <div v-if="!!props.execNode?.id" class="h-full text-3">
      <div class="flex items-center leading-5 mb-2.5">
        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">{{ t('common.node') }}</span>
          <Colon class="mr-2" />
          <span>{{ props.execNode.name }}({{ props.execNode.publicIp || props.execNode.ip }})</span>
        </div>

        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">{{ t('common.scheduleResult') }}</span>
          <Colon class="mr-2" />
          <template v-if="props.schedulingResult?.success">
            <span class="inline-block w-1.5 h-1.5 mr-1 rounded bg-status-success"></span>
            <span>{{ t('status.success') }}</span>
          </template>
          <template v-else>
            <span class="inline-block w-1.5 h-1.5 mr-1 rounded bg-status-error"></span>
            <span>{{ t('status.failed') }}</span>
          </template>
        </div>

        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">{{ t('common.exitCode') }}</span>
          <Colon class="mr-2" />
          <span>{{ props.schedulingResult?.exitCode }}</span>
        </div>

        <IconDownload class="text-4" @click="handleLogFileDownload" />
      </div>
      <div style="height:calc(100% - 30px);" class="px-3.5 py-2.5 space-y-1 rounded border border-solid border-theme-text-box overflow-auto whitespace-pre-wrap">
        {{ executionLogContent }}
      </div>
    </div>
    <div v-else-if="props.schedulingResult && props.sampleLogContent" class="h-full text-3">
      <div class="flex items-center leading-5 mb-2.5">
        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">{{ t('common.scheduleResult') }}</span>
          <Colon class="mr-2" />
          <template v-if="props.schedulingResult?.success">
            <span class="inline-block w-1.5 h-1.5 mr-1 rounded bg-status-success"></span>
            <span>{{ t('status.success') }}</span>
          </template>
          <template v-else>
            <span class="inline-block w-1.5 h-1.5 mr-1 rounded bg-status-error"></span>
            <span>{{ t('status.failed') }}</span>
          </template>
        </div>

        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">{{ t('common.exitCode') }}</span>
          <Colon class="mr-2" />
          <span>{{ props.schedulingResult?.exitCode }}</span>
        </div>

        <IconDownload class="text-4" @click="handleLogFileDownload" />
      </div>

      <div style="height:calc(100% - 30px);" class="px-3.5 py-2.5 space-y-1 rounded border border-solid border-theme-text-box overflow-auto whitespace-pre-wrap">
        {{ props.sampleLogContent }}
      </div>
    </div>
    <NoData v-if="!isLoading && !props.execNode?.id && (!props.schedulingResult || !props.sampleLogContent)" size="small" />
  </Spin>
</template>
