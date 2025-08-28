<script lang="ts" setup>
import { computed, defineAsyncComponent, ref, inject, Ref, onMounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { Icon, Input, Spin } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { duration, utils } from '@xcan-angus/infra';
import { useTrashActions } from './composables/useTrashActions';
import type { TaskTrashComponentProps, TaskTrashTargetType, TaskTrashItem } from './types';

/**
 * Component props with default values
 */
const props = withDefaults(defineProps<TaskTrashComponentProps>(), {
  projectId: '',
  userInfo: () => ({ id: '' }),
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
const activeKey = ref<TaskTrashTargetType>('TASK_SPRINT');
const inputValue = ref<string>();
const notify = ref<string>();
const tableDataMap = ref<Record<TaskTrashTargetType, TaskTrashItem[]>>({} as Record<TaskTrashTargetType, TaskTrashItem[]>);

// Use task trash actions composable
const {
  loading,
  recoverAll,
  deleteAll
} = useTrashActions(props.projectId);

/**
 * Debounced input change handler
 */
const inputChange = debounce(duration.search, () => {
  notify.value = utils.uuid();
});

/**
 * Computed parameters for TASK_SPRINT tab
 */
const serviceParams = computed(() => {
  const params: {
    targetType: 'TASK_SPRINT';
    targetName?: string;
  } = {
    targetType: 'TASK_SPRINT'
  };
  if (inputValue.value) {
    params.targetName = inputValue.value;
  }
  return params;
});

/**
 * Computed parameters for TASK tab
 */
const apiParams = computed(() => {
  const params: {
    targetType: 'TASK';
    targetName?: string;
  } = {
    targetType: 'TASK'
  };
  if (inputValue.value) {
    params.targetName = inputValue.value;
  }
  return params;
});

/**
 * Computed property to check if bulk action buttons should be disabled
 */
const buttonDisabled = computed(() => {
  return !tableDataMap.value[activeKey.value]?.length;
});

/**
 * Handle table data change events
 * @param listData - Updated table data
 * @param key - Tab key (TASK_SPRINT or TASK)
 */
const handleChange = (listData: TaskTrashItem[], key: TaskTrashTargetType) => {
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
      activeKey.value = 'TASK';
    }
  }, { immediate: true });
});
</script>
<template>
  <Spin :spinning="loading" class="h-full px-5 py-5 overflow-auto text-3">
    <!-- Header section with search and action buttons -->
    <div class="flex items-center justify-between mb-1">
      <!-- Search section -->
      <div class="flex items-center">
        <Input
          v-model:value="inputValue"
          :allowClear="true"
          :maxlength="200"
          trim
          :placeholder="t('taskTrash.placeholder.searchKeyword')"
          class="w-75"
          @change="inputChange">
          <template #suffix>
            <Icon class="text-3.5 cursor-pointer text-theme-content" icon="icon-sousuo" />
          </template>
        </Input>
        <div class="flex-1 truncate text-theme-sub-content space-x-1 ml-2">
          <Icon icon="icon-tishi1" class="text-3.5 text-tips" />
          <span>{{ t('taskTrash.tips.adminOnly') }}</span>
        </div>
      </div>

      <!-- Action buttons -->
      <div class="space-x-2.5">
        <Button
          :disabled="buttonDisabled"
          size="small"
          type="primary"
          @click="handleRecoverAll">
          <Icon icon="icon-zhongzhi" class="text-3.5 mr-1" />
          <span>{{ t('taskTrash.actions.recoverAll') }}</span>
        </Button>
        <Button
          :disabled="buttonDisabled"
          size="small"
          type="primary"
          danger
          @click="handleDeleteAll">
          <Icon icon="icon-qingchu" class="text-3.5 mr-1" />
          <span>{{ t('taskTrash.actions.deleteAll') }}</span>
        </Button>
        <Button
          size="small"
          type="default"
          @click="handleRefresh">
          <Icon icon="icon-shuaxin" class="text-3.5 mr-1" />
          <span>{{ t('taskTrash.actions.refresh') }}</span>
        </Button>
      </div>
    </div>

    <!-- Tabs section -->
    <Tabs v-model:activeKey="activeKey">
      <!-- TASK_SPRINT tab -->
      <TabPane
        v-if="proTypeShowMap.showSprint"
        key="TASK_SPRINT"
        :tab="t('taskTrash.tabs.sprint')">
        <Table
          v-model:spinning="loading"
          :notify="notify"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :params="serviceParams"
          @listChange="handleChange($event, 'TASK_SPRINT')" />
      </TabPane>

      <!-- TASK tab -->
      <TabPane key="TASK" :tab="t('taskTrash.tabs.task')">
        <Table
          v-model:spinning="loading"
          :notify="notify"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :params="apiParams"
          @listChange="handleChange($event, 'TASK')" />
      </TabPane>
    </Tabs>
  </Spin>
</template>
