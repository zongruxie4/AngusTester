<script lang="ts" setup>
import { Icon, Select, RadioGroup } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { useAgentLog } from './composables/useAgentLog';
import type { AgentLogProps } from './types';

/**
 * <p>Component props with default values</p>
 * <p>Provides default values for optional props</p>
 */
const props = withDefaults(defineProps<AgentLogProps>(), {
  ip: '',
  port: '6807'
});

/**
 * <p>Agent log data management</p>
 * <p>Handles log file listing, log content retrieval, and log operations</p>
 */
const {
  fileList,
  logContent,
  showError,
  errorText,
  isFullScreen,
  logTextParams,
  lineCountOptions,
  fullScreenIcon,
  loadLogContent,
  downloadLog,
  toggleFullScreen
} = useAgentLog({
  ip: props.ip,
  port: props.port
});
</script>

<template>
  <div class="agent-log-container">
    <!-- Log Controls -->
    <div class="log-controls">
      <div class="control-row">
        <span class="control-label">{{ $t('node.nodeDetail.log.title') }}:</span>
        <Select
          v-model:value="logTextParams.logName"
          class="log-file-select"
          :options="fileList">
        </Select>

        <RadioGroup
          v-model:value="logTextParams.linesNum"
          :options="lineCountOptions"
          size="small" />

        <!-- Error Display -->
        <div v-show="showError" class="error-display">
          {{ errorText || $t('node.nodeDetail.log.errorText', { ip: props.ip, port: props.port }) }}
        </div>

        <!-- Action Buttons -->
        <div class="action-buttons">
          <Button
            class="action-button"
            size="small"
            @click="downloadLog">
            {{ $t('common.download') }}
          </Button>

          <Button
            class="action-button"
            size="small"
            :disabled="!logTextParams.logName"
            @click="loadLogContent">
            {{ $t('actions.refresh') }}
          </Button>

          <!-- Full Screen Toggle -->
          <Icon
            class="fullscreen-icon"
            :icon="fullScreenIcon"
            @click.stop="toggleFullScreen" />
        </div>
      </div>
    </div>

    <!-- Log Content Display -->
    <div
      class="log-content"
      :class="{
        'fullscreen': isFullScreen,
        'normal': !isFullScreen
      }">
      <!-- Full Screen Close Button -->
      <Icon
        v-if="isFullScreen"
        class="fullscreen-close"
        :icon="fullScreenIcon"
        @click.stop="toggleFullScreen" />

      <!-- Log Text Content -->
      <pre class="log-text">{{ logContent }}</pre>
    </div>
  </div>
</template>

<style scoped>
.agent-log-container {
  padding: 16px;
}

.log-controls {
  margin-bottom: 16px;
}

.control-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.control-label {
  font-size: 14px;
  color: var(--text-primary);
}

.log-file-select {
  width: 280px;
}

.error-display {
  color: #ff4d4f;
  font-size: 14px;
  margin-left: 16px;
}

.action-buttons {
  display: inline-flex;
  align-items: center;
  margin-left: auto;
  gap: 8px;
}

.action-button {
  padding: 0;
  height: 20px;
}

.fullscreen-icon {
  font-size: 14px;
  margin-left: 8px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: color 0.2s ease;
}

.fullscreen-icon:hover {
  color: var(--text-primary);
}

.log-content {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  min-height: 200px;
  padding: 12px 16px;
  position: relative;
}

.log-content.normal {
  height: calc(100vh - 450px);
  overflow-y: auto;
}

.log-content.fullscreen {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  height: 100vh;
  z-index: 999;
  border: none;
  border-radius: 0;
}

.fullscreen-close {
  position: fixed;
  right: 28px;
  top: 28px;
  font-size: 14px;
  cursor: pointer;
  color: var(--text-secondary);
  z-index: 1000;
  transition: color 0.2s ease;
}

.fullscreen-close:hover {
  color: var(--text-primary);
}

.log-text {
  font-family: Monaco, Consolas, monospace !important;
  font-size: 12px;
  line-height: 1.5;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-words;
  color: var(--text-primary);
}
</style>
