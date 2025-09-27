<script lang="ts" setup>
import { computed, onMounted, toRef, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { appContext, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { Badge, Button, Tooltip } from 'ant-design-vue';
import { Icon, Image, Input, Spin, Table } from '@xcan-angus/vue-ui';
import { BasicProps } from '@/types/types';

import { useTrashData } from './composables/useTrashData';
import { useTableColumns } from './composables/useTableColumns';
import { useTrashActions } from './composables/useTrashActions';
import { useSearch } from './composables/useSearch';
import type { TrashItem } from './types';

/**
 * Scenario trash component for managing deleted scenario items
 * Provides functionality to view, restore, and permanently delete items
 */
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: '',
  userInfo: undefined
});

// Internationalization
const { t } = useI18n();

// Inject admin status from parent component
const isAdmin = computed(() => appContext.isAdmin());

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
} = useTrashData(toRef(props, 'projectId'), props.userInfo);

const { columns, emptyTextStyle } = useTableColumns();

const { restoreItem, deleteItem, restoreAll, deleteAll } = useTrashActions(toRef(props, 'projectId'));

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
    await loadDataAndRefresh();
  }
};

/**
 * Handle delete all action
 */
const handleDeleteAll = async () => {
  const success = await deleteAll();
  if (success) {
    resetPagination();
    await loadDataAndRefresh();
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
    await loadDataAndRefresh();
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
    await loadDataAndRefresh();
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
  sorter: { orderBy: string; orderSort: PageQuery.OrderSort }
) => {
  handleTableDataChange(paginationInfo, filters, sorter);
  loadDataAndRefresh();
};

/**
 * Load data and handle search parameters
 */
const loadDataAndRefresh = async () => {
  const params = inputValue.value ? { filters: [{ value: inputValue.value, key: 'targetName', op: SearchCriteria.OpEnum.Match }] } : undefined;
  await loadData(params);
};

/**
 * Clear search input and refresh list
 */
const clearSearchAndRefresh = () => {
  inputValue.value = '';
  resetPagination();
  loadDataAndRefresh();
};

// Computed properties for enhanced UI
const hasItems = computed(() => tableData.value && tableData.value.length > 0);
const itemCount = computed(() => pagination.value.total || 0);
const canPerformActions = computed(() => hasItems.value && !loading.value);
const hasSearchValue = computed(() => inputValue.value && inputValue.value.trim() !== '');

/**
 * Check if user can perform actions on item
 * @param item - Trash item to check
 * @returns Whether user has permission
 */
const canUserPerformActions = (item: TrashItem): boolean => {
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
  <!-- Enhanced main container with modern design -->
  <div class="h-full bg-white">
    <Spin :spinning="loading" class="h-full">
      <!-- Header section with enhanced visual hierarchy -->
      <div class="bg-white border-b border-gray-200 shadow-sm">
        <div class="px-4 py-3">
          <!-- Page title and description -->
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center space-x-3">
              <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                <Icon icon="icon-qingchu" class="text-2xl text-blue-500" />
              </div>
              <div class="mt-3">
                <h1 class="text-base font-bold text-gray-900">
                  {{ $t('scenarioTrash.name') }}
                </h1>
                <p class="text-xs text-gray-500 mt-0.5">
                  {{ $t('scenarioTrash.tips.adminOnly') }}
                </p>
              </div>
            </div>

            <!-- Statistics badge -->
            <div class="flex items-center space-x-3">
              <Badge
                :count="itemCount"
                :showZero="true"
                class="trash-count-badge">
                <div class="px-3 py-1.5 bg-gray-100 rounded-md">
                  <span class="text-xs font-medium text-gray-700">
                    {{ $t('stats.items') }}
                  </span>
                </div>
              </Badge>
            </div>
          </div>

          <!-- Enhanced search and actions bar -->
          <div class="flex items-center justify-between">
            <!-- Left side: Enhanced search with filters -->
            <div class="flex items-center space-x-3 flex-1">
              <!-- Search input with enhanced styling -->
              <div class="relative flex-1 max-w-sm">
                <Input
                  v-model:value="inputValue"
                  :allowClear="true"
                  :maxlength="200"
                  trim
                  :placeholder="t('common.placeholders.searchKeyword')"
                  class="search-input-enhanced"
                  size="small"
                  @change="inputChange">
                  <template #prefix>
                    <Icon icon="icon-sousuo" class="text-gray-400 text-sm" />
                  </template>
                </Input>
              </div>

              <!-- Filter chips -->
              <div class="flex items-center space-x-2">
                <span class="text-xs text-gray-500">{{ $t('quickSearch.filters') }}:</span>
                <div class="flex items-center space-x-1">
                  <span
                    class="px-2 py-0.5 text-xs bg-blue-100 text-blue-700 rounded-full cursor-pointer hover:bg-blue-200 transition-colors"
                    @click="clearSearchAndRefresh">
                    {{ $t('common.all') }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Right side: Enhanced action buttons -->
            <div class="flex items-center space-x-2">
              <!-- Bulk actions -->
              <div v-if="hasItems" class="flex items-center space-x-2">
                <Tooltip :title="t('scenarioTrash.actions.restoreAll')">
                  <Button
                    :disabled="!canPerformActions"
                    type="primary"
                    size="small"
                    class="action-button-primary"
                    @click="handleRestoreAll">
                    <Icon icon="icon-zhongzhi" class="mr-1 text-sm" />
                    {{ $t('scenarioTrash.actions.restoreAll') }}
                  </Button>
                </Tooltip>

                <Tooltip :title="t('actions.deleteAll')">
                  <Button
                    :disabled="!canPerformActions"
                    type="primary"
                    danger
                    size="small"
                    class="action-button-danger"
                    @click="handleDeleteAll">
                    <Icon icon="icon-qingchu" class="mr-1 text-sm" />
                    {{ $t('actions.deleteAll') }}
                  </Button>
                </Tooltip>
              </div>

              <!-- Refresh button -->
              <Tooltip :title="t('actions.refresh')">
                <Button
                  :loading="loading"
                  :disabled="!!hasSearchValue"
                  size="small"
                  class="action-button-secondary"
                  @click="handleRefresh">
                  <Icon icon="icon-shuaxin" class="text-sm" />
                </Button>
              </Tooltip>
            </div>
          </div>
        </div>
      </div>

      <!-- Main content area -->
      <div class="flex-1 px-4 py-4 overflow-auto">
        <!-- Enhanced data table -->
        <div v-if="loaded" class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
          <Table
            :dataSource="tableData"
            :columns="columns"
            :pagination="pagination"
            :emptyTextStyle="emptyTextStyle"
            :noDataSize="'small'"
            :noDataText="t('common.noData')"
            rowKey="id"
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
                  <div class="absolute -bottom-0.5 -right-0.5 w-2 h-2 bg-blue-500 rounded-full border border-white"></div>
                </div>
                <div class="flex-1 min-w-0">
                  <div class="font-medium text-gray-900 truncate text-xs">{{ record.createdByName }}</div>
                </div>
              </div>

              <!-- Enhanced action buttons cell -->
              <div v-else-if="column.dataIndex === 'action'" class="flex items-center space-x-1">
                <Tooltip :title="t('scenarioTrash.actions.restore')">
                  <Button
                    :disabled="!canUserPerformActions(record)"
                    type="text"
                    size="small"
                    class="action-icon-button recover-button"
                    @click="handleRestore(record)">
                    <Icon icon="icon-zhongzhi" class="text-sm" />
                  </Button>
                </Tooltip>

                <Tooltip :title="t('actions.delete')">
                  <Button
                    :disabled="!canUserPerformActions(record)"
                    type="text"
                    size="small"
                    class="action-icon-button delete-button"
                    @click="handleDelete(record)">
                    <Icon icon="icon-qingchu" class="text-sm" />
                  </Button>
                </Tooltip>
              </div>

              <!-- Enhanced target name cell -->
              <div
                v-else-if="column.dataIndex === 'targetName'"
                class="flex items-center space-x-2 p-1">
                <div class="w-5 h-5 bg-blue-100 rounded-md flex items-center justify-center">
                  <Icon icon="icon-changjing" class="text-blue-500 text-xs" />
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
                <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-3">
                  <Icon icon="icon-qingchu" class="text-2xl text-blue-400" />
                </div>
                <h3 class="text-sm font-medium text-gray-900 mb-1">
                  {{ $t('common.description') }}
                </h3>
                <p class="text-xs text-gray-500 max-w-sm mx-auto">
                  {{ $t('scenarioTrash.empty.hint') }}
                </p>
              </div>
            </template>
          </Table>
        </div>

        <!-- Loading state -->
        <div v-else-if="!loaded && loading" class="flex items-center justify-center py-16">
          <div class="text-center">
            <div class="w-12 h-12 border-4 border-blue-200 border-t-blue-500 rounded-full animate-spin mx-auto mb-3"></div>
            <p class="text-xs text-gray-500">{{ $t('common.loading') }}</p>
          </div>
        </div>
      </div>
    </Spin>
  </div>
</template>

<style scoped>
/* Enhanced search input styling */
.search-input-enhanced {
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  font-size: 12px;
}

.search-input-enhanced:hover {
  border-color: #d1d5db;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.search-input-enhanced:focus-within {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

/* Enhanced action buttons */
.action-button-primary {
  border-radius: 6px;
  font-weight: 600;
  font-size: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.action-button-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.action-button-danger {
  border-radius: 6px;
  font-weight: 600;
  font-size: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.action-button-danger:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.action-button-secondary {
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  color: #6b7280;
  font-size: 12px;
  transition: all 0.3s ease;
}

.action-button-secondary:hover:not(:disabled) {
  border-color: #d1d5db;
  background-color: #f9fafb;
  transform: translateY(-1px);
}

.action-button-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
  background-color: #dbeafe;
  color: #1d4ed8;
}

.delete-button:hover {
  background-color: #fee2e2;
  color: #dc2626;
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

/* Trash count badge styling */
.trash-count-badge :deep(.ant-badge-count) {
  background: #3b82f6;
  box-shadow: 0 1px 2px rgba(59, 130, 246, 0.3);
  font-size: 10px;
}

/* Responsive design */
@media (max-width: 768px) {
  .px-4 {
    padding-left: 1rem;
    padding-right: 1rem;
  }

  .py-3 {
    padding-top: 0.75rem;
    padding-bottom: 0.75rem;
  }

  .space-x-3 > * + * {
    margin-left: 0.75rem;
  }

  .max-w-sm {
    max-width: 100%;
  }
}
</style>
