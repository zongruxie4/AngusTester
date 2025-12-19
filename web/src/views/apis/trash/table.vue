<script setup lang="ts">
import { onMounted, watch, toRef } from 'vue';
import { Button, Tooltip } from 'ant-design-vue';
import { Icon, Image, Table } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { useTrashData } from './composables/useTrashData';
import { useTableColumns } from './composables/useTableColumns';
import { useTrashActions } from './composables/useTrashActions';
import type { TrashTableProps, TrashItem } from './types';

/**
 * Component props with default values
 */
const props = withDefaults(defineProps<TrashTableProps>(), {
  projectId: '',
  userInfo: () => ({ id: '' }),
  params: () => ({ targetType: 'API' as const }),
  notify: '',
  spinning: false
});

/**
 * Component emits
 */
const emit = defineEmits<{
  'update:spinning': [value: boolean];
  'tableChange': [value: TrashItem[]];
}>();

// Internationalization
const { t } = useI18n();

// Create reactive references for props
const projectIdRef = toRef(props, 'projectId');

// Use composables
const {
  tableData,
  loading,
  loaded,
  pagination,
  loadData,
  handleTableChange: handleTableDataChange,
  resetPagination,
  updateCurrentPage
} = useTrashData(projectIdRef, props.userInfo);

const { columns, emptyTextStyle } = useTableColumns();

const { recoverItem, deleteItem } = useTrashActions(projectIdRef);

/**
 * Handle recover action for a single item
 * @param data - Trash item to recover
 */
const recoverHandler = async (data: TrashItem) => {
  emit('update:spinning', true);
  const success = await recoverItem(data);
  if (success) {
    updateCurrentPage();
    await loadData(props.params);
  }
  emit('update:spinning', false);
};

/**
 * Handle delete action for a single item
 * @param data - Trash item to delete
 */
const deleteHandler = async (data: TrashItem) => {
  emit('update:spinning', true);
  const success = await deleteItem(data);
  if (success) {
    updateCurrentPage();
    await loadData(props.params);
  }
  emit('update:spinning', false);
};

/**
 * Handle table change events (pagination, sorting)
 * @param paginationInfo - Pagination information
 * @param filters - Table filters
 * @param sorter - Sorting information
 */
const tableChange = (
  paginationInfo: { current?: number; pageSize?: number },
  filters: any,
  sorter: { orderBy?: string; orderSort?: 'ASC' | 'DESC' }
) => {
  handleTableDataChange(paginationInfo, filters, sorter);
  loadData(props.params);
};

/**
 * Watch for prop changes and reload data
 */
watch(
  [() => props.projectId, () => props.params],
  () => {
    resetPagination();
    loadData(props.params);
  },
  { immediate: true }
);

/**
 * Watch for notify changes and reload data
 */
watch(
  () => props.notify,
  (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }
    resetPagination();
    loadData(props.params);
  },
  { immediate: true }
);

/**
 * Watch for loading state changes and emit to parent
 */
watch(
  loading,
  (newLoading) => {
    emit('update:spinning', newLoading);
  }
);

/**
 * Watch for table data changes and emit to parent
 */
watch(
  tableData,
  (newData) => {
    emit('tableChange', newData);
  },
  { deep: true }
);

// Lifecycle hook
onMounted(() => {
  // Initial data loading is handled by watchers
});
</script>

<template>
  <!-- Enhanced data table -->
  <div v-if="loaded" class="enhanced-table-container">
    <Table
      :dataSource="tableData"
      :columns="columns"
      :pagination="pagination"
      :emptyTextStyle="emptyTextStyle"
      rowKey="id"
      noDataSize="small"
      :noDataText="t('common.noData')"
      size="small"
      class="enhanced-table"
      @change="tableChange">
      <!-- Custom cell rendering for enhanced visual appeal -->
      <template #bodyCell="{ record, column }">
        <!-- Enhanced deleter avatar and name cell -->
        <div
          v-if="column.dataIndex === 'deletedByName'"
          :title="record.deletedByName"
          class="flex items-center space-x-2 p-1 rounded-md hover:bg-gray-50 transition-colors">
          <div class="relative">
            <Image
              :src="record.deletedByAvatar"
              type="avatar"
              class="w-6 h-6 rounded-full border border-gray-200" />
            <div class="absolute -bottom-0.5 -right-0.5 w-2 h-2 bg-red-500 rounded-full border border-white"></div>
          </div>
          <div class="flex-1 min-w-0">
            <div class="font-medium text-gray-900 truncate text-xs">{{ record.deletedByName }}</div>
          </div>
        </div>

        <!-- Enhanced creator avatar and name cell -->
        <div
          v-else-if="column.dataIndex === 'creator'"
          :title="record.creator"
          class="flex items-center space-x-2 p-1 rounded-md hover:bg-gray-50 transition-colors">
          <div class="relative">
            <Image
              :src="record.creatorAvatar"
              type="avatar"
              class="w-6 h-6 rounded-full border border-gray-200" />
            <div class="absolute -bottom-0.5 -right-0.5 w-2 h-2 bg-green-500 rounded-full border border-white"></div>
          </div>
          <div class="flex-1 min-w-0">
            <div class="font-medium text-gray-900 truncate text-xs">{{ record.creator }}</div>
          </div>
        </div>

        <!-- Enhanced action buttons cell -->
        <div v-else-if="column.dataIndex === 'action'" class="flex items-center space-x-1">
          <Tooltip :title="t('actions.recover')">
            <Button
              :disabled="record.disabled"
              type="text"
              size="small"
              class="action-icon-button recover-button"
              @click="recoverHandler(record)">
              <Icon icon="icon-zhongzhi" class="text-sm" />
            </Button>
          </Tooltip>

          <Tooltip :title="t('actions.delete')">
            <Button
              :disabled="record.disabled"
              type="text"
              size="small"
              class="action-icon-button delete-button"
              @click="deleteHandler(record)">
              <Icon icon="icon-qingchu" class="text-sm" />
            </Button>
          </Tooltip>
        </div>

        <!-- Enhanced target name cell -->
        <div
          v-else-if="column.dataIndex === 'targetName'"
          class="flex items-center space-x-2 p-1">
          <div class="w-5 h-5 bg-gray-100 rounded-md flex items-center justify-center">
            <Icon icon="icon-jiekou" class="text-gray-500 text-xs" />
          </div>
          <div class="flex-1 min-w-0">
            <div class="font-medium text-gray-900 truncate text-xs">{{ record.targetName }}</div>
          </div>
        </div>

        <!-- Enhanced date cells -->
        <div
          v-else-if="column.dataIndex === 'deletedDate'"
          class="text-xs text-gray-600">
          <div class="font-medium">{{ record.deletedDate }}</div>
        </div>
      </template>

      <!-- Enhanced empty state -->
      <template #emptyText>
        <div class="py-8 text-center">
          <div class="w-12 h-12 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-3">
            <Icon icon="icon-qingchu" class="text-2xl text-gray-400" />
          </div>
          <h3 class="text-3.5 font-medium text-gray-900 mb-1">
            {{ $t('trash.messages.emptyDescription') }}
          </h3>
          <p class="text-3.5 text-gray-500">
            {{ $t('trash.messages.emptyHint') }}
          </p>
        </div>
      </template>
    </Table>
  </div>

  <!-- Loading state -->
  <div v-else-if="!loaded && loading" class="flex items-center justify-center py-16">
    <div class="text-center">
      <div class="w-12 h-12 border-4 border-green-200 border-t-green-500 rounded-full animate-spin mx-auto mb-3"></div>
      <p class="text-xs text-gray-500">{{ $t('common.loading') }}</p>
    </div>
  </div>
</template>

<style scoped>
/* Enhanced table container */
.enhanced-table-container {
  border-radius: 8px;
  overflow: hidden;
}

/* Enhanced table styling */
.enhanced-table {
  border-radius: 8px;
  overflow: hidden;
}

.enhanced-table :deep(.ant-table-thead > tr > th) {
  background-color: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  font-weight: 600;
  color: #374151;
  padding: 8px 12px;
  font-size: 12px;
}

.enhanced-table :deep(.ant-table-tbody > tr > td) {
  padding: 8px 12px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 12px;
}

.enhanced-table :deep(.ant-table-tbody > tr:hover > td) {
  background-color: #f8fafc;
}

.enhanced-table :deep(.ant-table-tbody > tr.disabled-row > td) {
  background-color: #f9fafb;
  opacity: 0.6;
}

.enhanced-table :deep(.ant-table-tbody > tr.disabled-row:hover > td) {
  background-color: #f3f4f6;
}

/* Enhanced action icon buttons */
.action-icon-button {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  transition: all 0.2s ease;
  font-size: 12px;
}

.action-icon-button:hover {
  transform: scale(1.05);
}

.recover-button:hover {
  background-color: #d1fae5;
  color: #059669;
}

.delete-button:hover {
  background-color: #fee2e2;
  color: #dc2626;
}

/* Responsive design */
@media (max-width: 768px) {
  .enhanced-table :deep(.ant-table) {
    font-size: 11px;
  }

  .w-6 {
    width: 1.25rem;
    height: 1.25rem;
  }
}
</style>
