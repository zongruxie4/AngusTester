<script setup lang="ts">
// Vue core imports
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Colon, NoData, IconDownload } from '@xcan-angus/vue-ui';

// Infrastructure imports
import { utils } from '@xcan-angus/infra';

const { t } = useI18n();

/**
 * Debug log data interface
 */
interface DebugLogData {
  success: boolean;
  exitCode: string;
  console: string[];
}

/**
 * Component props interface for debug log
 */
interface Props {
  value: DebugLogData;
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

// Computed properties
const processedConsoleLogs = computed(() => {
  if (!props.value?.console?.length) {
    return [];
  }

  return props.value.console.map(logItem => {
    return {
      id: utils.uuid(),
      log: logItem
    };
  });
});

/**
 * Handle download log file
 */
const handleDownloadLog = () => {
  const logContent = props.value?.console?.join('\n');
  if (!logContent) {
    return;
  }

  const logFile = new File([logContent], 'scheduling.log', {
    type: 'text/plain'
  });
  const downloadUrl = URL.createObjectURL(logFile);
  const downloadLink = document.createElement('a');
  downloadLink.style.display = 'none';
  downloadLink.href = downloadUrl;
  downloadLink.download = 'scheduling.log';
  document.body.appendChild(downloadLink);
  downloadLink.click();
  document.body.removeChild(downloadLink);
  window.URL.revokeObjectURL(downloadUrl);
};
</script>

<template>
  <div class="h-full px-5 py-3 text-3">
    <template v-if="!!props.value">
      <div class="flex items-center leading-5 mb-2.5">
        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">{{ t('common.scheduleResult') }}</span>
          <Colon class="mr-2" />
          <template v-if="props.value?.success">
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
          <span>{{ props.value?.exitCode }}</span>
        </div>
        <IconDownload class="text-4" @click="handleDownloadLog" />
      </div>
      <div style="height:calc(100% - 30px);" class="px-3.5 py-2.5 space-y-1 rounded border border-solid border-theme-text-box overflow-auto">
        <div
          v-for="logItem in processedConsoleLogs"
          :key="logItem.id"
          class="log-item">
          {{ logItem.log }}
        </div>
      </div>
    </template>
    <NoData v-else size="small" />
  </div>
</template>
