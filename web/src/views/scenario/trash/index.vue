<script lang="ts" setup>
import { inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Icon, Image, Input, Spin, Table } from '@xcan-angus/vue-ui';
import { useTrashData } from './composables/useTrashData';
import { useTableColumns } from './composables/useTableColumns';
import { useTrashActions } from './composables/useTrashActions';
import { useSearch } from './composables/useSearch';
import type { TrashProps, TrashItem } from './types';

/**
 * Scenario trash component for managing deleted scenario items
 * Provides functionality to view, restore, and permanently delete items
 */
const props = withDefaults(defineProps<TrashProps>(), {
  projectId: '',
  userInfo: () => ({ id: '' })
});

// Internationalization
const { t } = useI18n();

// Inject admin status from parent component
const isAdmin = inject('isAdmin', ref(false));

// Use composables for separation of concerns
const {
  tableData,
  loading,
  loaded,
  pagination,
  loadData,
  handleTableChange: handleTableDataChange,
  resetPagination,
  updateCurrentPage
} = useTrashData(props.projectId, props.userInfo);

const { columns, emptyTextStyle, defaultTableProps } = useTableColumns();

const { restoreItem, deleteItem, restoreAll, deleteAll } = useTrashActions(props.projectId);

const { inputValue, createInputChangeHandler } = useSearch();

/**
 * Handle input change with debouncing
 */
const inputChange = createInputChangeHandler(() => {
  resetPagination();
  loadDataAndRefresh();
});

/**
 * Handle restore all action
 */
const handleRestoreAll = async () => {
  const success = await restoreAll();
  if (success) {
    resetPagination();
    loadDataAndRefresh();
  }
};

/**
 * Handle delete all action
 */
const handleDeleteAll = async () => {
  const success = await deleteAll();
  if (success) {
    resetPagination();
    loadDataAndRefresh();
  }
};

/**
 * Handle refresh action
 */
const handleRefresh = () => {
  resetPagination();
  loadDataAndRefresh();
};

/**
 * Handle restore action for a single item
 * @param data - Trash item to restore
 */
const handleRestore = async (data: TrashItem) => {
  const success = await restoreItem(data.id);
  if (success) {
    updateCurrentPage();
    loadDataAndRefresh();
  }
};

/**
 * Handle delete action for a single item
 * @param data - Trash item to delete permanently
 */
const handleDelete = async (data: TrashItem) => {
  const success = await deleteItem(data.id);
  if (success) {
    updateCurrentPage();
    loadDataAndRefresh();
  }
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
  loadDataAndRefresh();
};

/**
 * Load data and handle search parameters
 */
const loadDataAndRefresh = async () => {
  const params = inputValue.value ? { targetName: inputValue.value } : undefined;
  await loadData(params);
};

/**
 * Check if user can perform actions on item
 * @param item - Trash item to check
 * @returns Whether user has permission
 */
const canPerformActions = (item: TrashItem): boolean => {
  if (isAdmin?.value) return true;
  return props.userInfo?.id === item.createdBy || props.userInfo?.id === item.deletedBy;
};

// Lifecycle and watchers
onMounted(() => {
  watch(
    () => props.projectId,
    (newValue) => {
      if (newValue) {
        resetPagination();
        loadDataAndRefresh();
      }
    },
    { immediate: true }
  );
});
</script>

<template>
  <Spin
    :spinning="loading"
    class="h-full px-5 py-5 overflow-auto text-3">
    <!-- Header section with search and actions -->
    <div class="flex items-center justify-between mb-3.5">
      <!-- Search section -->
      <div class="flex items-center">
        <Input
          v-model:value="inputValue"
          :allowClear="true"
          :maxlength="200"
          :placeholder="t('scenarioTrash.table.searchPlaceholder')"
          trim
          class="w-75"
          @change="inputChange">
          <template #suffix>
            <Icon
              class="text-3.5 cursor-pointer text-theme-content"
              icon="icon-sousuo" />
          </template>
        </Input>

        <!-- Admin tip -->
        <div class="flex-1 truncate text-theme-sub-content space-x-1 ml-2">
          <Icon
            icon="icon-tishi1"
            class="text-3.5 text-tips" />
          <span>{{ t('scenarioTrash.description') }}</span>
        </div>
      </div>

      <!-- Action buttons -->
      <div class="space-x-2.5">
        <Button
          :disabled="!tableData?.length"
          :loading="loading"
          size="small"
          type="primary"
          @click="handleRestoreAll">
          <Icon
            icon="icon-zhongzhi"
            class="text-3.5 mr-1" />
          <span>{{ t('scenarioTrash.actions.restoreAll') }}</span>
        </Button>

        <Button
          :disabled="!tableData?.length"
          :loading="loading"
          size="small"
          type="primary"
          danger
          @click="handleDeleteAll">
          <Icon
            icon="icon-qingchu"
            class="text-3.5 mr-1" />
          <span>{{ t('scenarioTrash.actions.deleteAll') }}</span>
        </Button>

        <Button
          size="small"
          type="default"
          @click="handleRefresh">
          <Icon
            icon="icon-shuaxin"
            class="text-3.5 mr-1" />
          <span>{{ t('scenarioTrash.actions.refresh') }}</span>
        </Button>
      </div>
    </div>

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
            <div class="w-6 h-6 rounded-full overflow-hidden border-2 border-gray-200 group-hover:border-red-300 transition-colors">
              <Image
                :src="record.deletedByAvatar"
                type="avatar"
                class="w-full h-full object-cover" />
            </div>
            <!-- Status indicator for deleter -->
            <div class="absolute -bottom-0.5 -right-0.5 w-2 h-2 bg-red-500 rounded-full border border-white"></div>
          </div>
          <div class="flex-1 truncate">
            <div class="text-sm font-medium text-gray-900 group-hover:text-red-600 transition-colors">
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
            <div class="w-6 h-6 rounded-full overflow-hidden border-2 border-gray-200 group-hover:border-blue-300 transition-colors">
              <Image
                :src="record.createdByAvatar"
                type="avatar"
                class="w-full h-full object-cover" />
            </div>
            <!-- Status indicator for creator -->
            <div class="absolute -bottom-0.5 -right-0.5 w-2 h-2 bg-blue-500 rounded-full border border-white"></div>
          </div>
          <div class="flex-1 truncate">
            <div class="text-sm font-medium text-gray-900 group-hover:text-blue-600 transition-colors">
              {{ record.createdByName }}
            </div>
          </div>
        </div>

        <!-- Action column with enhanced buttons -->
        <div
          v-else-if="column.dataIndex === 'action'"
          class="flex items-center justify-center space-x-2">
          <!-- Restore button -->
          <Button
            :disabled="!canPerformActions(record)"
            :title="t('scenarioTrash.actions.restore')"
            size="small"
            type="text"
            class="flex items-center p-1 hover:bg-green-50 rounded transition-colors"
            @click="handleRestore(record)">
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
            :title="t('scenarioTrash.actions.delete')"
            size="small"
            type="text"
            class="flex items-center p-1 hover:bg-red-50 rounded transition-colors"
            @click="handleDelete(record)">
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
  </Spin>
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
