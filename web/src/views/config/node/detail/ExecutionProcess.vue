<script lang="ts" setup>
import { Button } from 'ant-design-vue';
import { Table } from '@xcan-angus/vue-ui';
import { useExecutionProcess } from './composables/useExecutionProcess';
import type { ExecutionPropulsionProps } from './types';

/**
 * <p>Component props with default values</p>
 * <p>Provides default values for optional props</p>
 */
const props = withDefaults(defineProps<ExecutionPropulsionProps>(), {
  nodeId: '',
  tenantId: undefined
});

/**
 * <p>Execution propulsion data management</p>
 * <p>Handles process data fetching, process management, and table configuration</p>
 */
const {
  processSummary,
  processDataList,
  isLoading,
  killProcess,
  canKillProcess,
  tableColumns
} = useExecutionProcess(props.nodeId, props.tenantId);
</script>

<template>
  <div class="execution-propulsion-container">
    <!-- Process Summary Statistics -->
    <div class="process-summary">
      <div class="summary-item">
        <div class="summary-value">{{ processSummary.processCount || '--' }}</div>
        <div class="summary-label">{{ $t('node.nodeDetail.execPropulsion.processCount') }}</div>
      </div>
      <div class="summary-item">
        <div class="summary-value">{{ processSummary.threadCount || '--' }}</div>
        <div class="summary-label">{{ $t('node.nodeDetail.execPropulsion.threadCount') }}</div>
      </div>
      <div class="summary-item">
        <div class="summary-value">{{ processSummary.openFiles || '--' }}</div>
        <div class="summary-label">{{ $t('node.nodeDetail.execPropulsion.openFiles') }}</div>
      </div>
      <div class="summary-item">
        <div class="summary-value">{{ processSummary.bytesWritten || '--' }}</div>
        <div class="summary-label">{{ $t('node.nodeDetail.execPropulsion.writeDisk') }}</div>
      </div>
      <div class="summary-item">
        <div class="summary-value">{{ processSummary.bytesRead || '--' }}</div>
        <div class="summary-label">{{ $t('node.nodeDetail.execPropulsion.readDisk') }}</div>
      </div>
    </div>

    <!-- Process Table -->
    <Table
      bordered
      :loading="isLoading"
      :columns="tableColumns"
      :dataSource="processDataList"
      :pagination="false"
      class="process-table"
      size="small"
      noDataSize="small">
      <!-- Custom cell rendering for action column -->
      <template #bodyCell="{ record, column }">
        <template v-if="column.dataIndex === 'action'">
          <Button
            :disabled="!canKillProcess()"
            type="link"
            size="small"
            @click="killProcess(record)">
            {{ $t('node.nodeDetail.execPropulsion.killProcess') }}
          </Button>
        </template>
      </template>

      <!-- Empty state when no data is available -->
      <template #empty>
        <div class="empty-state">
          <p>No process data available for this node</p>
        </div>
      </template>
    </Table>
  </div>
</template>

<style scoped>
.execution-propulsion-container {
  padding: 16px;
}

.process-summary {
  display: flex;
  gap: 32px;
  padding-bottom: 12px;
  margin-bottom: 16px;
}

.summary-item {
  text-align: center;
  padding: 8px;
  border-radius: 6px;
  min-width: 80px;
}

.summary-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-sub-content);
  line-height: 1.2;
}

.summary-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--theme-title);
  margin-top: 4px;
}

.process-table {
  margin-top: 16px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #8c8c8c;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

/* Deep styles for table customization */
:deep(.process-table.table-empty) .ant-table.ant-table-bordered > .ant-table-container {
  border-left: 0 !important;
}
</style>
