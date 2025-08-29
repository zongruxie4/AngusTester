<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import { Button, Tooltip } from 'ant-design-vue';
import { Icon, Image, Table } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { useTrashData } from './composables/useTrashData';
import { useTableColumns } from './composables/useTableColumns';
import { useTrashActions } from './composables/useTrashActions';
import type { TrashTableProps, TrashItem } from './types';

/**
 * Enhanced trash table component with composables and improved UX
 * Provides data management, sorting, and action capabilities
 */
const props = withDefaults(defineProps<TrashTableProps & { isAdmin?: boolean }>(), {
  projectId: '',
  userInfo: () => ({ id: '' }),
  params: () => ({ targetType: 'CASE' as const }),
  notify: '',
  spinning: false,
  isAdmin: false
});

/**
 * Component emits for parent communication
 */
const emit = defineEmits<{
  'update:spinning': [value: boolean];
  'tableChange': [value: { list: TrashItem[]; total: number }];
}>();

// Internationalization
const { t } = useI18n();

// Inject admin status if not provided as prop
const injectedIsAdmin = inject('isAdmin', ref(false));
const isAdmin = props.isAdmin || injectedIsAdmin.value;

// Use composables for separation of concerns
const {
  tableData,
  loaded,
  loading,
  pagination,
  loadData,
  handleTableChange: handleTableDataChange,
  resetPagination,
  updateCurrentPage
} = useTrashData(props.projectId, props.userInfo);

const { columns, emptyTextStyle } = useTableColumns();

const { recoverItem, deleteItem } = useTrashActions(props.projectId);

/**
 * Handle recover action for a single item
 * @param data - Trash item to recover
 */
const recoverHandler = async (data: TrashItem) => {
  emit('update:spinning', true);

  const success = await recoverItem(data.id);

  if (success) {
    updateCurrentPage();
    await loadDataAndEmit();
  }

  emit('update:spinning', false);
};

/**
 * Handle delete action for a single item
 * @param data - Trash item to delete permanently
 */
const deleteHandler = async (data: TrashItem) => {
  emit('update:spinning', true);

  const success = await deleteItem(data.id);

  if (success) {
    updateCurrentPage();
    await loadDataAndEmit();
  }

  emit('update:spinning', false);
};

/**
 * Handle table change events (pagination, sorting)
 * @param paginationInfo - Pagination data
 * @param filters - Filter data
 * @param sorter - Sort configuration
 */
const tableChange = (
  paginationInfo: { current?: number; pageSize?: number },
  filters: any,
  sorter: { orderBy: string; orderSort: 'ASC' | 'DESC' }
) => {
  handleTableDataChange(paginationInfo, filters, sorter);
  loadDataAndEmit();
};

/**
 * Load data and emit table change event
 */
const loadDataAndEmit = async () => {
  await loadData(props.params);
  emit('tableChange', { list: tableData.value, total: pagination.value.total });
};

/**
 * Check if user can perform actions on item
 * @param item - Trash item to check
 * @returns Whether user has permission
 */
const canPerformActions = (item: TrashItem): boolean => {
  if (isAdmin) return true;
  return props.userInfo?.id === item.createdBy || props.userInfo?.id === item.deletedBy;
};

// Lifecycle and watchers
onMounted(() => {
  // Watch for prop changes and reload data
  watch(
    [() => props.projectId, () => props.params],
    () => {
      if (props.projectId) {
        resetPagination();
        loadDataAndEmit();
      }
    },
    { immediate: true }
  );

  // Watch for notification changes to refresh data
  watch(
    () => props.notify,
    (newValue) => {
      if (newValue) {
        resetPagination();
        loadDataAndEmit();
      }
    }
  );
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
          v-else-if="column.dataIndex === 'createdByName'"
          :title="record.createdByName"
          class="flex items-center space-x-2 p-1 rounded-md hover:bg-gray-50 transition-colors">
          <div class="relative">
            <Image
              :src="record.createdByAvatar"
              type="avatar"
              class="w-6 h-6 rounded-full border border-gray-200" />
            <div class="absolute -bottom-0.5 -right-0.5 w-2 h-2 bg-green-500 rounded-full border border-white"></div>
          </div>
          <div class="flex-1 min-w-0">
            <div class="font-medium text-gray-900 truncate text-xs">{{ record.createdByName }}</div>
          </div>
        </div>

        <!-- Enhanced action buttons cell -->
        <div v-else-if="column.dataIndex === 'action'" class="flex items-center space-x-1">
          <Tooltip :title="t('functionTrash.actions.recover')">
            <Button
              :disabled="!canPerformActions(record)"
              type="text"
              size="small"
              class="action-icon-button recover-button"
              @click="recoverHandler(record)">
              <Icon icon="icon-zhongzhi" class="text-sm" />
            </Button>
          </Tooltip>

          <Tooltip :title="t('functionTrash.actions.delete')">
            <Button
              :disabled="!canPerformActions(record)"
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
          <div class="w-5 h-5 bg-purple-100 rounded-md flex items-center justify-center">
            <Icon icon="icon-gongneng" class="text-purple-500 text-xs" />
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
          <div class="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-3">
            <Icon icon="icon-qingchu" class="text-2xl text-purple-400" />
          </div>
          <h3 class="text-sm font-medium text-gray-900 mb-1">
            {{ $t('functionTrash.empty.description') }}
          </h3>
          <p class="text-xs text-gray-500 max-w-sm mx-auto">
            {{ $t('functionTrash.empty.hint') }}
          </p>
        </div>
      </template>
    </Table>
  </div>

  <!-- Loading state -->
  <div v-else-if="!loaded && loading" class="flex items-center justify-center py-16">
    <div class="text-center">
      <div class="w-12 h-12 border-4 border-purple-200 border-t-purple-500 rounded-full animate-spin mx-auto mb-3"></div>
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
  background-color: #dcfce7;
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
