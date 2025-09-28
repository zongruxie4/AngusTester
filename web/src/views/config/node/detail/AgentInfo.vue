<script lang="ts" setup>
import { Grid, Icon, Tooltip } from '@xcan-angus/vue-ui';
import { useAgentInfo } from './composables/useAgentInfo';
import { formatBytes } from '@/utils';
import type { AgentInfoProps } from './types';

/**
 * <p>Component props with default values</p>
 * <p>Provides default values for optional props</p>
 */
const props = withDefaults(defineProps<AgentInfoProps>(), {
  ip: '',
  port: '6807'
});

/**
 * <p>Agent info data management</p>
 * <p>Handles agent information retrieval, data processing, and grid configuration</p>
 */
const {
  agentInfoData,
  showError,
  errorInfo,
  gridColumns,
  closeErrorInfo
} = useAgentInfo({
  ip: props.ip,
  port: props.port
});
</script>

<template>
  <div class="agent-info-container">
    <!-- Error Display -->
    <div v-if="showError" class="error-banner">
      <Icon icon="icon-tishi1" class="error-icon" />
      <span class="error-text">
        {{ errorInfo || $t('node.detail.agent.log.errorText', { ip: props.ip, port: props.port }) }}
      </span>
      <Icon
        icon="icon-cuowu"
        class="close-icon"
        @click="closeErrorInfo" />
    </div>

    <!-- Agent Info Title -->
    <div class="info-title">
      {{ $t('node.detail.agent.title') }}
    </div>

    <!-- Agent Information Grid -->
    <Grid
      class="info-grid"
      :columns="gridColumns"
      :dataSource="agentInfoData">
      <!-- Custom cell rendering for health status -->
      <template #health="text">
        <template v-if="text.text && text.text === 'UP'">
          {{ $t('node.detail.agent.health.normal') }}
        </template>
        <template v-else-if="text.text">
          <Tooltip>
            <template #title>{{ agentInfoData.errorTip }}</template>
            <span class="health-warning">
              {{ $t('node.detail.agent.health.abnormal') }}
              <Icon class="warning-icon" icon="icon-jinggao" />
            </span>
          </Tooltip>
        </template>
      </template>

      <!-- Custom cell rendering for disk space -->
      <template #diskSpace="record">
        <template v-if="record.text">
          <span
            class="disk-space"
            :class="{ 'disk-space-warning': record.text.used / record.text.total > 0.8 }">
            {{ formatBytes(record.text.used) }} / {{ formatBytes(record.text.total) }}
          </span>
        </template>
      </template>
    </Grid>
  </div>
</template>

<style scoped>
.agent-info-container {
  padding: 16px;
}

.error-banner {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  margin: 16px 0;
  background-color: var(--bg-red);
  border: 1px solid var(--border-error);
  border-radius: 12px;
  font-size: 14px;
}

.error-icon {
  font-size: 14px;
  color: var(--blue-icon);
  flex-shrink: 0;
}

.error-text {
  color: var(--text-rule);
  flex: 1;
}

.close-icon {
  font-size: 14px;
  cursor: pointer;
  color: var(--text-rule);
  transition: color 0.2s ease;
  flex-shrink: 0;
}

.close-icon:hover {
  color: var(--text-primary);
}

.info-title {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
  margin: 10px 0;
}

.info-grid {
  margin-left: 24px;
}

.health-warning {
  color: var(--text-rule);
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.warning-icon {
  color: var(--text-rule);
}

.disk-space {
  color: var(--text-primary);
}

.disk-space-warning {
  color: var(--text-rule);
}
</style>
