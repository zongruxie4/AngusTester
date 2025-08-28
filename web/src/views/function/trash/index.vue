<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, ref, watch } from 'vue';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { Icon, Input, Spin } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { utils } from '@xcan-angus/infra';
import { useTrashActions } from './composables/useTrashActions';
import { useSearch } from './composables/useSearch';
import type { TrashProps, TrashItem } from './types';

/**
 * Function trash component for managing deleted function items
 * Provides functionality to view, recover, and permanently delete items
 */
const props = withDefaults(defineProps<TrashProps>(), {
  projectId: '',
  userInfo: () => ({ id: '' })
});

// Async component for better performance
const Table = defineAsyncComponent(() => import('./table.vue'));

// Internationalization
const { t } = useI18n();

// Inject admin status from parent component
const isAdmin = inject('isAdmin', ref(false));

// State management
const activeKey = ref<'PLAN' | 'CASE'>('PLAN');
const refreshNotify = ref<string>();
const tableDataMap = ref<Record<string, TrashItem[]>>({});

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
 * Handle table data change from child component
 * @param listData - Updated table data
 * @param key - Tab key (PLAN or CASE)
 */
const handleTableChange = (listData: TrashItem[], key: string) => {
  tableDataMap.value[key] = listData;
};

// Computed properties
const serviceParams = computed(() => {
  const params: {
    targetType: 'PLAN';
    targetName?: string;
  } = {
    targetType: 'PLAN'
  };

  if (inputValue.value) {
    params.targetName = inputValue.value;
  }

  return params;
});

const caseParams = computed(() => {
  const params: {
    targetType: 'CASE';
    targetName?: string;
  } = {
    targetType: 'CASE'
  };

  if (inputValue.value) {
    params.targetName = inputValue.value;
  }

  return params;
});

const buttonDisabled = computed(() => {
  return !tableDataMap.value[activeKey.value]?.length;
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
  <Spin
    :spinning="loading"
    class="h-full px-5 py-5 overflow-auto text-3">
    <!-- Header section with search and actions -->
    <div class="flex items-center justify-between mb-4">
      <!-- Search section -->
      <div class="flex items-center flex-1 mr-4">
        <Input
          :value="inputValue"
          :allowClear="true"
          :maxlength="200"
          :placeholder="t('functionTrash.placeholder.searchKeyword')"
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
          <span>{{ t('functionTrash.tips.adminOnly') }}</span>
        </div>
      </div>

      <!-- Action buttons -->
      <div class="space-x-2.5 flex-shrink-0">
        <Button
          :disabled="buttonDisabled"
          :loading="loading"
          size="small"
          type="primary"
          @click="handleRecoverAll">
          <Icon
            icon="icon-zhongzhi"
            class="text-3.5 mr-1" />
          <span>{{ t('functionTrash.actions.recoverAll') }}</span>
        </Button>

        <Button
          :disabled="buttonDisabled"
          :loading="loading"
          size="small"
          type="primary"
          danger
          @click="handleDeleteAll">
          <Icon
            icon="icon-qingchu"
            class="text-3.5 mr-1" />
          <span>{{ t('functionTrash.actions.deleteAll') }}</span>
        </Button>

        <Button
          size="small"
          type="default"
          @click="refreshData">
          <Icon
            icon="icon-shuaxin"
            class="text-3.5 mr-1" />
          <span>{{ t('functionTrash.actions.refresh') }}</span>
        </Button>
      </div>
    </div>

    <!-- Tabs section -->
    <Tabs
      v-model:activeKey="activeKey"
      class="h-full flex flex-col">
      <!-- Plans tab -->
      <TabPane
        key="PLAN"
        :tab="t('functionTrash.tabs.plan')">
        <Table
          v-model:spinning="loading"
          :notify="refreshNotify"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :params="serviceParams"
          :isAdmin="isAdmin"
          @tableChange="handleTableChange($event, 'PLAN')" />
      </TabPane>

      <!-- Cases tab -->
      <TabPane
        key="CASE"
        :tab="t('functionTrash.tabs.case')">
        <Table
          v-model:spinning="loading"
          :notify="refreshNotify"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :params="caseParams"
          :isAdmin="isAdmin"
          @tableChange="handleTableChange($event, 'CASE')" />
      </TabPane>
    </Tabs>
  </Spin>
</template>

<style scoped>
/**
 * Enhanced styling for trash component
 */
.enhanced-search {
  @apply transition-all duration-200 focus-within:shadow-md;
}

.action-buttons {
  @apply flex items-center space-x-2;
}

/* Responsive design */
@media (max-width: 768px) {
  .flex {
    @apply flex-col space-y-2;
  }

  .w-75 {
    @apply w-full;
  }
}
</style>
