<script lang="ts" setup>
import { computed, defineAsyncComponent, ref, toRef } from 'vue';
import { Button, TabPane, Tabs, Tooltip, Popconfirm, Badge } from 'ant-design-vue';
import { Icon, Input, Spin } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { duration, utils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { useTrashActions } from './composables/useTrashActions';
import type { TrashComponentProps, TrashTargetType, TrashItem } from './types';

/**
 * Component props with default values
 */
const props = withDefaults(defineProps<TrashComponentProps>(), {
  projectId: '',
  userInfo: () => ({ id: '' })
});

// Internationalization
const { t } = useI18n();

// Async component import
const Table = defineAsyncComponent(() => import('./table.vue'));

// Reactive state
const activeKey = ref<TrashTargetType>('SERVICE');
const inputValue = ref<string>();
const notify = ref<string>();
const tableDataMap = ref<Record<TrashTargetType, TrashItem[]>>({} as Record<TrashTargetType, TrashItem[]>);

// Create reactive reference for projectId
const projectIdRef = toRef(props, 'projectId');

// Use trash actions composable
const {
  loading,
  recoverAll,
  deleteAll
} = useTrashActions(projectIdRef);

/**
 * Debounced input change handler
 */
const inputChange = debounce(duration.search, () => {
  notify.value = utils.uuid();
});

/**
 * Computed parameters for SERVICE tab
 */
const serviceParams = computed(() => {
  const params: {
    targetType: 'SERVICE';
    targetName?: string;
  } = {
    targetType: 'SERVICE'
  };

  if (inputValue.value) {
    params.targetName = inputValue.value;
  }

  return params;
});

/**
 * Computed parameters for API tab
 */
const apiParams = computed(() => {
  const params: {
    targetType: 'API';
    targetName?: string;
  } = {
    targetType: 'API'
  };

  if (inputValue.value) {
    params.targetName = inputValue.value;
  }

  return params;
});

/**
 * Computed property for item count
 */
const itemCount = computed(() => {
  const currentData = tableDataMap.value[activeKey.value];
  return currentData?.length || 0;
});

/**
 * Computed property to check if there are items in current tab
 */
const hasItems = computed(() => {
  return itemCount.value > 0;
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
 * @param key - Tab key (SERVICE or API)
 */
const handleChange = (listData: TrashItem[], key: TrashTargetType) => {
  tableDataMap.value[key] = listData;
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
  await deleteAll();
};

/**
 * Handle refresh action
 */
const handleRefresh = () => {
  notify.value = utils.uuid();
};
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
                  {{ $t('apiTrash.title') }}
                </h1>
                <p class="text-xs text-gray-500 mt-0.5">
                  {{ $t('apiTrash.tips.adminOnly') }}
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
                    {{ $t('apiTrash.stats.items') }}
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
                  :placeholder="t('apiTrash.searchPlaceholder')"
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
                <span class="text-xs text-gray-500">{{ $t('apiTrash.filters.title') }}:</span>
                <div class="flex items-center space-x-1">
                  <span
                    class="px-2 py-0.5 text-xs bg-green-100 text-green-700 rounded-full cursor-pointer hover:bg-green-200 transition-colors"
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
                <Tooltip :title="t('apiTrash.actions.recoverAll')">
                  <Button
                    :disabled="!canPerformActions"
                    type="primary"
                    size="small"
                    class="action-button-primary"
                    @click="handleRecoverAll">
                    <Icon icon="icon-zhongzhi" class="mr-1 text-sm" />
                    {{ $t('apiTrash.actions.recoverAll') }}
                  </Button>
                </Tooltip>

                <Popconfirm
                  :title="t('actions.deleteAll')"
                  :okText="t('common.confirm')"
                  :cancelText="t('common.cancel')"
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
              <Tooltip :title="t('common.refresh')">
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
            <!-- SERVICE tab -->
            <TabPane key="SERVICE" :tab="t('common.service')">
              <Table
                v-model:spinning="loading"
                :notify="notify"
                :projectId="props.projectId"
                :userInfo="props.userInfo"
                :params="serviceParams"
                @tableChange="handleChange($event, 'SERVICE')" />
            </TabPane>

            <!-- API tab -->
            <TabPane key="API" :tab="t('common.api')">
              <Table
                v-model:spinning="loading"
                :notify="notify"
                :projectId="props.projectId"
                :userInfo="props.userInfo"
                :params="apiParams"
                @tableChange="handleChange($event, 'API')" />
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
  border-color: #10b981;
  box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.1);
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
  color: #10b981;
}

.enhanced-tabs :deep(.ant-tabs-tab-active) {
  color: #059669;
  font-weight: 600;
}

.enhanced-tabs :deep(.ant-tabs-ink-bar) {
  background-color: #10b981;
  height: 2px;
}

.enhanced-tabs :deep(.ant-tabs-content-holder) {
  background-color: white;
}

/* Trash count badge styling */
.trash-count-badge :deep(.ant-badge-count) {
  background: #10b981;
  box-shadow: 0 1px 2px rgba(16, 185, 129, 0.3);
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
