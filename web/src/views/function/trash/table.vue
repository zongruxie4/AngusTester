<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
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
  'tableChange': [value: TrashItem[]];
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
  pagination,
  loadData,
  handleTableChange: handleTableDataChange,
  resetPagination,
  updateCurrentPage
} = useTrashData(props.projectId, props.userInfo);

const { columns, emptyTextStyle, defaultTableProps } = useTableColumns();

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
  emit('tableChange', tableData.value);
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
  <!-- Enhanced table with improved accessibility and UX -->
  <Table
    v-if="loaded"
    :dataSource="tableData"
    :columns="columns"
    :pagination="pagination"
    :emptyTextStyle="emptyTextStyle"
    :noDataSize="'small'"
    :noDataText="t('common.noData')"
    v-bind="defaultTableProps"
    rowKey="id"
    class="enhanced-trash-table"
    @change="tableChange">

    <!-- Custom cell rendering for enhanced UX -->
    <template #bodyCell="{ record, column }">
      <!-- Deleter column with enhanced avatar display -->
      <div
        v-if="column.dataIndex === 'deletedByName'"
        :title="record.deletedByName"
        class="flex items-center overflow-hidden group">
        <div class="relative flex-shrink-0 mr-2">
          <div class="w-6 h-6 rounded-full overflow-hidden border-2 border-gray-200 group-hover:border-blue-300 transition-colors">
            <Image
              :src="record.deletedByAvatar"
              type="avatar"
              class="w-full h-full object-cover" />
          </div>
          <!-- Status indicator for deleter -->
          <div class="absolute -bottom-0.5 -right-0.5 w-2 h-2 bg-red-500 rounded-full border border-white"></div>
        </div>
        <div class="flex-1 truncate">
          <div class="text-sm font-medium text-gray-900 group-hover:text-blue-600 transition-colors">
            {{ record.deletedByName }}
          </div>
        </div>
      </div>

      <!-- Creator column with enhanced avatar display -->
      <div
        v-else-if="column.dataIndex === 'createdByName'"
        :title="record.createdByName"
        class="flex items-center overflow-hidden group">
        <div class="relative flex-shrink-0 mr-2">
          <div class="w-6 h-6 rounded-full overflow-hidden border-2 border-gray-200 group-hover:border-green-300 transition-colors">
            <Image
              :src="record.createdByAvatar"
              type="avatar"
              class="w-full h-full object-cover" />
          </div>
          <!-- Status indicator for creator -->
          <div class="absolute -bottom-0.5 -right-0.5 w-2 h-2 bg-green-500 rounded-full border border-white"></div>
        </div>
        <div class="flex-1 truncate">
          <div class="text-sm font-medium text-gray-900 group-hover:text-green-600 transition-colors">
            {{ record.createdByName }}
          </div>
        </div>
      </div>

      <!-- Action column with enhanced buttons -->
      <div
        v-else-if="column.dataIndex === 'action'"
        class="flex items-center justify-center space-x-2">
        <!-- Recover button -->
        <Button
          :disabled="!canPerformActions(record)"
          :title="t('functionTrash.actions.recover')"
          size="small"
          type="text"
          class="flex items-center p-1 hover:bg-green-50 rounded transition-colors"
          @click="recoverHandler(record)">
          <Icon
            icon="icon-zhongzhi"
            :class="[
              'text-3.5',
              canPerformActions(record)
                ? 'text-green-600 hover:text-green-700'
                : 'text-gray-400'
            ]" />
        </Button>

        <!-- Delete button -->
        <Button
          :disabled="!canPerformActions(record)"
          :title="t('functionTrash.actions.delete')"
          size="small"
          type="text"
          class="flex items-center p-1 hover:bg-red-50 rounded transition-colors"
          @click="deleteHandler(record)">
          <Icon
            icon="icon-qingchu"
            :class="[
              'text-3.5',
              canPerformActions(record)
                ? 'text-red-600 hover:text-red-700'
                : 'text-gray-400'
            ]" />
        </Button>
      </div>
    </template>
  </Table>
</template>

<style scoped>
/**
 * Enhanced styling for trash table
 */
.enhanced-trash-table {
  @apply bg-white rounded-lg shadow-sm;
}

.enhanced-trash-table :deep(.ant-table-thead > tr > th) {
  @apply bg-gray-50 font-semibold text-gray-700;
}

.enhanced-trash-table :deep(.ant-table-tbody > tr:hover > td) {
  @apply bg-blue-50;
}

.enhanced-trash-table :deep(.ant-table-tbody > tr > td) {
  @apply border-b border-gray-100;
}

/* Action button hover effects */
.group:hover .transition-colors {
  @apply transform scale-105;
}

/* Responsive design */
@media (max-width: 768px) {
  .enhanced-trash-table :deep(.ant-table) {
    @apply text-xs;
  }

  .w-6 {
    @apply w-5 h-5;
  }
}

/* Empty state styling */
.enhanced-trash-table :deep(.ant-empty) {
  @apply py-16;
}
</style>
