<script lang="ts" setup>
import { Table } from '@xcan-angus/vue-ui';
import { useProjectInfo } from './composables/useProjectInfo';
import { useMockServiceData } from './composables/useMockServiceData';
import { useMockServiceTableColumns } from './composables/useMockServiceTableColumns';
import { MockServiceProps } from '@/views/config/node/detail/types';

/**
 * <p>Component props with default values</p>
 * <p>Provides default values for optional props</p>
 */
const props = withDefaults(defineProps<MockServiceProps>(), {
  nodeId: ''
});

/**
 * <p>Project information management</p>
 * <p>Provides access to current project details</p>
 */
const { projectId } = useProjectInfo();

/**
 * <p>Mock service data management</p>
 * <p>Handles data fetching, pagination, and state management</p>
 */
const {
  mockServiceData,
  pagination,
  isLoading,
  error,
  handlePaginationChange
} = useMockServiceData(props.nodeId, projectId.value);

/**
 * <p>Table column configuration</p>
 * <p>Provides reactive column definitions with internationalization</p>
 */
const { columns } = useMockServiceTableColumns();
</script>

<template>
  <div class="mock-service-container">
    <!-- Error Display -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <!-- Mock Service Table -->
    <Table
      :dataSource="mockServiceData"
      :columns="columns"
      :pagination="pagination"
      :loading="isLoading"
      size="small"
      noDataSize="small"
      @change="handlePaginationChange">
      <!-- Custom cell rendering for status column -->
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <span class="status-cell">
            {{ record.status?.message }}
          </span>
        </template>
      </template>

      <!-- Empty state when no data is available -->
      <template #empty>
        <div class="empty-state">
          <p>No mock services found for this node</p>
        </div>
      </template>
    </Table>
  </div>
</template>

<style scoped>
.mock-service-container {
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

.status-cell {
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
