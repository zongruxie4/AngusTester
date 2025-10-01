<script lang="ts" setup>
import { computed, defineAsyncComponent, ref, watch } from 'vue';
import { Button, TabPane, Tabs, Tooltip, Popconfirm, Badge } from 'ant-design-vue';
import { Icon, Input, Spin } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { appContext, utils, SearchCriteria } from '@xcan-angus/infra';

import { FuncTargetType } from '@/enums/enums';
import { useTrashActions } from './composables/useTrashActions';
import { useSearch } from './composables/useSearch';
import type { TrashItem } from './types';
import { BasicProps } from '@/types/types';

/**
 * Function trash component for managing deleted function items
 * Provides functionality to view, recover, and permanently delete items
 */
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: '',
  userInfo: undefined
});

// Async component for better performance
const Table = defineAsyncComponent(() => import('./table.vue'));

// Internationalization
const { t } = useI18n();

// Inject admin status from parent component
const isAdmin = computed(() => appContext.isAdmin());

// State management
const activeKey = ref<FuncTargetType>(FuncTargetType.PLAN);
const refreshNotify = ref<string>();
const tableDataMap = ref<Record<string, TrashItem[]>>({});
const totalItemsMap = ref<Record<string, number>>({});

// Use composables
const { inputValue, createInputChangeHandler } = useSearch();
const { loading, recoverAll, deleteAll } = useTrashActions(props.projectId);

/**
 * Handle input change with debouncing
 */
const inputChange = createInputChangeHandler();

/**
 * Handle recover all action
 */
const handleRecoverAll = async () => {
  const success = await recoverAll();
  if (success) {
    refreshData();
  }
};

/**
 * Handle delete all action
 */
const handleDeleteAll = async () => {
  const success = await deleteAll();
  if (success) {
    refreshData();
  }
};

/**
 * Trigger data refresh
 */
const refreshData = () => {
  refreshNotify.value = utils.uuid();
};

/**
 * Clear search input and refresh list
 */
const clearSearchAndRefresh = () => {
  inputValue.value = '';
  refreshData();
};

/**
 * Handle table data change from child component
 * @param data - Table data and pagination info
 * @param key - Tab key (PLAN or CASE)
 */
const handleTableChange = (data: { list: TrashItem[]; total: number }, key: string) => {
  tableDataMap.value[key] = data.list;
  totalItemsMap.value[key] = data.total;
};

/**
 * Handle refresh action
 */
const handleRefresh = () => {
  refreshData();
};

// Computed properties
const itemCount = computed(() => {
  return totalItemsMap.value[activeKey.value] || 0;
});

const hasItems = computed(() => {
  return itemCount.value > 0;
});

const canPerformActions = computed(() => {
  return hasItems.value && !loading.value;
});

const hasSearchValue = computed(() => {
  return inputValue.value && inputValue.value.trim() !== '';
});

const planParams = computed(() => {
  const params: {
    targetType: FuncTargetType.PLAN;
    filters?: SearchCriteria[];
  } = {
    targetType: FuncTargetType.PLAN
  };

  if (inputValue.value) {
    params.filters = [{ value: inputValue.value, key: 'targetName', op: SearchCriteria.OpEnum.Match }];
  }
  return params;
});

const caseParams = computed(() => {
  const params: {
    targetType: FuncTargetType.CASE;
    filters?: SearchCriteria[];
  } = {
    targetType: FuncTargetType.CASE
  };

  if (inputValue.value) {
    params.filters = [{ value: inputValue.value, key: 'targetName', op: SearchCriteria.OpEnum.Match }];
  }
  return params;
});

// Watch for project changes
watch(
  () => props.projectId,
  () => {
    if (props.projectId) {
      refreshData();
    }
  },
  { immediate: true }
);
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
                  {{ $t('trash.title') }}
                </h1>
                <p class="text-xs text-gray-500 mt-0.5">
                  {{ $t('trash.messages.adminHint') }}
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
                <Tooltip :title="t('actions.recoverAll')">
                  <Button
                    :disabled="!canPerformActions"
                    type="primary"
                    size="small"
                    class="action-button-primary"
                    @click="handleRecoverAll">
                    <Icon icon="icon-zhongzhi" class="mr-1 text-sm" />
                    {{ $t('actions.recoverAll') }}
                  </Button>
                </Tooltip>

                <Popconfirm
                  :title="t('actions.deleteAll')"
                  :okText="t('common.confirm')"
                  :cancelText="t('actions.cancel')"
                  @confirm="handleDeleteAll">
                  <Tooltip :title="t('actions.deleteAll')">
                    <Button
                      :disabled="!canPerformActions"
                      type="primary"
                      danger
                      size="small"
                      class="action-button-danger">
                      <Icon icon="icon-qingchu" class="mr-1 text-sm" />
                      {{ $t('actions.deleteAll') }}
                    </Button>
                  </Tooltip>
                </Popconfirm>
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
      <div v-if="props.projectId" class="flex-1 px-4 py-4 overflow-auto">
        <!-- Enhanced tabs section -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
          <Tabs v-model:activeKey="activeKey" class="enhanced-tabs">
            <!-- Plans tab -->
            <TabPane :key="FuncTargetType.PLAN" :tab="t('common.plan')">
              <Table
                key="plan"
                v-model:spinning="loading"
                :notify="refreshNotify"
                :projectId="props.projectId"
                :userInfo="props.userInfo"
                :params="planParams"
                :isAdmin="isAdmin"
                @tableChange="handleTableChange($event, FuncTargetType.PLAN)" />
            </TabPane>

            <!-- Cases tab -->
            <TabPane :key="FuncTargetType.CASE" :tab="t('common.useCase')">
              <Table
                key="case"
                v-model:spinning="loading"
                :notify="refreshNotify"
                :projectId="props.projectId"
                :userInfo="props.userInfo"
                :params="caseParams"
                :isAdmin="isAdmin"
                @tableChange="handleTableChange($event, FuncTargetType.CASE)" />
            </TabPane>
          </Tabs>
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
  border-color: #8b5cf6;
  box-shadow: 0 0 0 2px rgba(139, 92, 246, 0.1);
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

/* Enhanced tabs styling */
.enhanced-tabs {
  border-radius: 8px;
  overflow: hidden;
}

.enhanced-tabs :deep(.ant-tabs-nav) {
  background-color: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  margin: 0;
}

.enhanced-tabs :deep(.ant-tabs-tab) {
  padding: 8px 16px;
  font-size: 12px;
  font-weight: 500;
  color: #6b7280;
  transition: all 0.2s ease;
}

.enhanced-tabs :deep(.ant-tabs-tab:hover) {
  color: #8b5cf6;
}

.enhanced-tabs :deep(.ant-tabs-tab-active) {
  color: #7c3aed;
  font-weight: 600;
}

.enhanced-tabs :deep(.ant-tabs-ink-bar) {
  background-color: #8b5cf6;
  height: 2px;
}

.enhanced-tabs :deep(.ant-tabs-content-holder) {
  background-color: white;
}

/* Trash count badge styling */
.trash-count-badge :deep(.ant-badge-count) {
  background: #8b5cf6;
  box-shadow: 0 1px 2px rgba(139, 92, 246, 0.3);
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
