<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, onMounted, Ref, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Badge, Button, Popconfirm, TabPane, Tabs, Tooltip } from 'ant-design-vue';
import { Icon, Input, Spin } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { duration, utils, SearchCriteria } from '@xcan-angus/infra';

import { useTrashActions } from './composables/useTrashActions';
import { CombinedTargetType } from '@/enums/enums';
import type { TaskTrashItem, TaskTrashTargetType } from './types';
import { BasicProps } from '@/types/types';

/**
 * Component props with default values
 */
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: '',
  userInfo: undefined,
  refreshNotify: ''
});

// Internationalization
const { t } = useI18n();

// Project type show map injection
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>(
  'proTypeShowMap',
  ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTaskStatistics: true })
);

// Async component import
const Table = defineAsyncComponent(() => import('./table.vue'));

// Reactive state
const activeKey = ref<TaskTrashTargetType>(CombinedTargetType.TASK_SPRINT);
const inputValue = ref<string>();
const notify = ref<string>();
const tableDataMap = ref<Record<TaskTrashTargetType, TaskTrashItem[]>>({} as Record<TaskTrashTargetType, TaskTrashItem[]>);
const totalItemsMap = ref<Record<TaskTrashTargetType, number>>({} as Record<TaskTrashTargetType, number>);

// Use task trash actions composable
const {
  loading,
  recoverAll,
  deleteAll
} = useTrashActions(props.projectId);

// Get current active tab data
const currentTableData = computed(() => tableDataMap.value[activeKey.value] || []);
const currentTotalItems = computed(() => totalItemsMap.value[activeKey.value] || 0);

const hasItems = computed(() => currentTableData.value && currentTableData.value.length > 0);
const itemCount = computed(() => currentTotalItems.value);

/**
 * Debounced input change handler
 */
const inputChange = debounce(duration.search, () => {
  notify.value = utils.uuid();
});

/**
 * Computed parameters for TASK_SPRINT tab
 */
const sprintParams = computed(() => {
  const params: {
    targetType: CombinedTargetType.TASK_SPRINT;
    filters?: SearchCriteria[];
  } = {
    targetType: CombinedTargetType.TASK_SPRINT
  };
  if (inputValue.value) {
    params.filters = [{ value: inputValue.value, op: SearchCriteria.OpEnum.Match, key: 'targetName' }];
  }
  return params;
});

/**
 * Computed parameters for TASK tab
 */
const taskParams = computed(() => {
  const params: {
    targetType: CombinedTargetType.TASK;
    filters?: SearchCriteria[];
  } = {
    targetType: CombinedTargetType.TASK
  };
  if (inputValue.value) {
    params.filters = [{ value: inputValue.value, op: SearchCriteria.OpEnum.Match, key: 'targetName' }];
  }
  return params;
});

/**
 * Computed property to check if actions can be performed
 */
const canPerformActions = computed(() => {
  return hasItems.value && !loading.value;
});

/**
 * Computed property to check if search input has value
 */
const hasSearchValue = computed(() => {
  return inputValue.value && inputValue.value.trim() !== '';
});

/**
 * Clear search input and refresh list
 */
const clearSearchAndRefresh = () => {
  inputValue.value = '';
  notify.value = utils.uuid();
};

/**
 * Handle table data change events
 * @param listData - Updated table data
 * @param key - Tab key (TASK_SPRINT or TASK)
 */
const handleChange = (listData: TaskTrashItem[], key: TaskTrashTargetType) => {
  tableDataMap.value[key] = listData;
};

/**
 * Handle pagination data change events
 * @param total - Total items count
 * @param key - Tab key (TASK_SPRINT or TASK)
 */
const handlePaginationChange = (total: number, key: TaskTrashTargetType) => {
  totalItemsMap.value[key] = total;
};

/**
 * Handle recover all action
 */
const handleRecoverAll = async () => {
  await recoverAll();
};

/**
 * Handle delete all action
 */
const handleDeleteAll = async () => {
  const success = await deleteAll();
  if (success) {
    notify.value = utils.uuid();
  }
};

/**
 * Handle refresh action
 */
const handleRefresh = () => {
  notify.value = utils.uuid();
};

// Lifecycle hooks
onMounted(() => {
  watch(() => proTypeShowMap.value.showSprint, (newValue) => {
    if (!newValue) {
      activeKey.value = CombinedTargetType.TASK;
    }
  }, { immediate: true });
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
                  {{ $t('taskTrash.name') }}
                </h1>
                <p class="text-xs text-gray-500 mt-0.5">
                  {{ $t('taskTrash.tips.adminOnly') }}
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
                    {{ $t('taskTrash.stats.items') }}
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
                  :placeholder="t('taskTrash.placeholder.searchKeyword')"
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
                <span class="text-xs text-gray-500">{{ $t('taskTrash.filters.title') }}:</span>
                <div class="flex items-center space-x-1">
                  <span
                    class="px-2 py-0.5 text-xs bg-blue-100 text-blue-700 rounded-full cursor-pointer hover:bg-blue-200 transition-colors"
                    @click="clearSearchAndRefresh">
                    {{ $t('taskTrash.filters.all') }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Right side: Enhanced action buttons -->
            <div class="flex items-center space-x-2">
              <!-- Bulk actions -->
              <div v-if="hasItems" class="flex items-center space-x-2">
                <Tooltip :title="t('taskTrash.actions.recoverAll')">
                  <Button
                    :disabled="!canPerformActions"
                    type="primary"
                    size="small"
                    class="action-button-primary"
                    @click="handleRecoverAll">
                    <Icon icon="icon-zhongzhi" class="mr-1 text-sm" />
                    {{ $t('taskTrash.actions.recoverAll') }}
                  </Button>
                </Tooltip>

                <Popconfirm
                  :title="t('actions.deleteAll')"
                  :okText="t('actions.confirm')"
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
      <div class="flex-1 px-4 py-4 overflow-auto">
        <!-- Enhanced tabs section -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
          <Tabs v-model:activeKey="activeKey" class="enhanced-tabs">
            <!-- TASK_SPRINT tab -->
            <TabPane
              v-if="proTypeShowMap.showSprint"
              :key="CombinedTargetType.TASK_SPRINT"
              :tab="t('taskTrash.tabs.sprint')">
              <Table
                v-model:spinning="loading"
                :notify="notify"
                :projectId="props.projectId"
                :userInfo="props.userInfo"
                :params="sprintParams"
                @listChange="handleChange($event, CombinedTargetType.TASK_SPRINT)"
                @paginationChange="handlePaginationChange($event, CombinedTargetType.TASK_SPRINT)" />
            </TabPane>

            <!-- TASK tab -->
            <TabPane :key="CombinedTargetType.TASK" :tab="t('taskTrash.tabs.task')">
              <Table
                v-model:spinning="loading"
                :notify="notify"
                :projectId="props.projectId"
                :userInfo="props.userInfo"
                :params="taskParams"
                @listChange="handleChange($event, CombinedTargetType.TASK)"
                @paginationChange="handlePaginationChange($event, CombinedTargetType.TASK)" />
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
  color: #3b82f6;
}

.enhanced-tabs :deep(.ant-tabs-tab-active) {
  color: #1d4ed8;
  font-weight: 600;
}

.enhanced-tabs :deep(.ant-tabs-ink-bar) {
  background-color: #3b82f6;
  height: 2px;
}

.enhanced-tabs :deep(.ant-tabs-content-holder) {
  background-color: white;
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
