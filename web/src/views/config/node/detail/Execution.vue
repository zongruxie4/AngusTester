<script lang="ts" setup>
import { Table } from '@xcan-angus/vue-ui';
import { useExecution } from './composables/useExecution';
import type { ExecutionProps } from './types';

/**
 * <p>Component props with default values</p>
 * <p>Provides default values for optional props</p>
 */
const props = withDefaults(defineProps<ExecutionProps>(), {
  id: ''
});

/**
 * <p>Execution data management</p>
 * <p>Handles execution data fetching and table configuration</p>
 */
const {
  executionDataSource,
  isLoading,
  error,
  tableColumns
} = useExecution(props.id);
</script>

<template>
  <div class="execution-container">
    <!-- Error Display -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <!-- Execution Table -->
    <Table
      :dataSource="executionDataSource"
      :columns="tableColumns"
      :loading="isLoading"
      :pagination="false"
      size="small"
      noDataSize="small">
      <!-- Custom cell rendering for name column -->
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'name'">
          <a
            class="execution-link"
            :href="`/execution/info/${record.id}`"
            target="_blank">
            {{ record.name || '--' }}
          </a>
        </template>

        <!-- Custom cell rendering for script type column -->
        <template v-if="column.dataIndex === 'scriptType'">
          <span class="script-type">
            {{ record.scriptType?.message }}
          </span>
        </template>
      </template>

      <!-- Empty state when no data is available -->
      <template #empty>
        <div class="empty-state">
          <p>{{ $t('node.message.noExecutionRecords') }}</p>
        </div>
      </template>
    </Table>
  </div>
</template>

<style scoped>
.execution-container {
  padding: 16px;
}

.error-message {
  color: #ff4d4f;
  background-color: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 16px;
  font-size: 14px;
}

.execution-link {
  color: var(--theme-special);
  text-decoration: none;
  transition: color 0.2s ease;
}

.execution-link:hover {
  color: var(--theme-special-hover);
  text-decoration: underline;
}

.script-type {
  font-weight: 500;
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
</style>
