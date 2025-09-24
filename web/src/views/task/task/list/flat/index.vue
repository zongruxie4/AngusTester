<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { AsyncComponent, modal, notification } from '@xcan-angus/vue-ui';
import { task } from '@/api/tester';
import { TaskStatus } from '@/enums/enums';

import { TaskDetail } from '@/views/task/types';
import { ActionMenuItem } from '@/views/task/task/types';

// Component props interface for task list management
type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  selectedIds: string[];
  dataSource: TaskDetail[];
  editTaskData: TaskDetail;
  pagination: { current: number; pageSize: number; total: number; };
  menuItemsMap: Map<string, ActionMenuItem[]>;
  loading: boolean;
  loaded: boolean;
}

// Component Setup
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  selectedIds: () => [],
  dataSource: () => [],
  editTaskData: undefined,
  pagination: () => ({ current: 1, pageSize: 10, total: 0, index: 0 }),
  menuItemsMap: () => new Map(),
  loading: false,
  loaded: false
});

/**
 * <p>Component event emissions for parent-child communication</p>
 * <p>Handles all user interactions and data changes that need to be communicated to parent components</p>
 */
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'update:loading', value: boolean): void;
  (event: 'update:selectedIds', value: string[]): void;
  (event: 'edit', value: string): void;
  (event: 'move', value: TaskDetail): void;
  (event: 'delete', value:string): void;
  (event: 'dataChange', value: Partial<TaskDetail>): void;
  (event: 'refreshChange'): void;
  (event: 'splitOk'): void;
  (event: 'paginationChange', value: { current: number; pageSize: number; }): void;
  (event: 'batchAction', type: 'cancel' | 'delete' | 'addFollow' | 'cancelFollow' | 'addFavourite' | 'cancelFavourite' | 'move', value: string[]): void;
}>();

// Async Components
const List = defineAsyncComponent(() => import('./List.vue'));
const Details = defineAsyncComponent(() => import('@/views/task/task/list/flat/detail/index.vue'));
const MoveTaskModal = defineAsyncComponent(() => import('@/views/task/task/list/Move.vue'));

// Constants and Configuration
const { t } = useI18n();
const MAX_BATCH_OPERATION_COUNT = 200;

// Batch operation button states
const isBatchCancelDisabled = ref(false);
const isBatchDeleteDisabled = ref(false);
const isBatchMoveDisabled = ref(false);
const isBatchFavouriteDisabled = ref(false);
const isBatchCancelFavouriteDisabled = ref(false);
const isBatchFollowDisabled = ref(false);
const isBatchCancelFollowDisabled = ref(false);

// Currently selected task for detail view
const currentSelectedTask = ref<TaskDetail>();

// Modal visibility states
const isMoveModalVisible = ref(false);

// Map of selected task data for batch operations
const selectedTaskDataMap = ref<{
  [key: string]: {
    id: string;
    status: TaskDetail['status']['value'];
    favourite: boolean;
    follow: boolean;
    sprintId?: string;
  }
}>({});

/**
 * <p>Handles pagination change events from the list component</p>
 * <p>Forwards pagination changes to parent component for data fetching</p>
 */
const handlePaginationChange = (data: { current: number; pageSize: number; }) => {
  emit('paginationChange', data);
};

/**
 * <p>Handles single task selection for detail view display</p>
 * <p>Updates the currently selected task to show its details in the side panel</p>
 */
const handleSingleTaskSelection = (data: TaskDetail) => {
  currentSelectedTask.value = data;
};

/**
 * <p>Handles edit task action</p>
 * <p>Emits edit event with task ID to parent component for opening edit modal</p>
 */
const handleEditTask = (id: string) => {
  emit('edit', id);
};

/**
 * <p>Handles delete task confirmation</p>
 * <p>Emits delete event with task ID to parent component for task deletion</p>
 */
const handleDeleteTask = (id: string) => {
  emit('delete', id);
};

/**
 * <p>Handles move task action</p>
 * <p>Emits move event with task data to parent component for opening move modal</p>
 */
const handleMoveTask = (data: TaskDetail) => {
  emit('move', data);
};

/**
 * <p>Handles task data changes</p>
 * <p>Forwards data changes to parent component for state synchronization</p>
 */
const handleTaskDataChange = (data: Partial<TaskDetail>) => {
  emit('dataChange', data);
};

/**
 * <p>Handles refresh request</p>
 * <p>Emits refresh event to parent component for data reloading</p>
 */
const handleRefreshRequest = () => {
  emit('refreshChange');
};

/**
 * <p>Handles multi-selection of tasks for batch operations</p>
 * <p>Manages the selected task IDs and their associated data for batch processing</p>
 * <p>Validates selection count against maximum allowed batch operations</p>
 */
const handleMultiTaskSelection = (ids: string[]) => {
  // Remove unselected tasks from data map
  const unselectedIds = props.dataSource.reduce((prev, cur) => {
    const id = cur.id;
    if (!ids.includes(id)) {
      prev.push(cur.id);
      delete selectedTaskDataMap.value[id];
    } else {
      // Add selected task data to map
      selectedTaskDataMap.value[id] = {
        id,
        status: cur.status.value,
        favourite: cur.favourite,
        follow: cur.follow
      };
    }
    return prev;
  }, [] as string[]);

  // Update selected IDs, filtering out unselected ones
  const selectedRowKeys = props.selectedIds.filter(item => !unselectedIds.includes(item));

  // Add new selections
  for (let i = 0, len = ids.length; i < len; i++) {
    if (!selectedRowKeys.includes(ids[i])) {
      selectedRowKeys.push(ids[i]);
    }
  }

  // Validate selection count
  const selectedCount = selectedRowKeys.length;
  if (selectedCount > MAX_BATCH_OPERATION_COUNT) {
    notification.info(t('task.detail.messages.maxBatchOperation', {
      maxNum: MAX_BATCH_OPERATION_COUNT,
      selectedNum: selectedCount
    }));
  }

  emit('update:selectedIds', selectedRowKeys);
};

/**
 * <p>Clears all task selections and resets selection state</p>
 * <p>Used when canceling batch operations or resetting selection</p>
 */
const clearAllSelections = () => {
  emit('update:selectedIds', []);
  selectedTaskDataMap.value = {};
};

/**
 * <p>Executes batch cancel operation on selected tasks</p>
 * <p>Shows confirmation dialog and processes all selected tasks for cancellation</p>
 * <p>Handles partial success scenarios and updates UI accordingly</p>
 */
const executeBatchCancel = async () => {
  const selectedCount = props.selectedIds.length;
  modal.confirm({
    content: t('task.detail.batchActions.confirmCancel', { num: selectedCount }),
    async onOk () {
      const taskIds = Object.values(selectedTaskDataMap.value).map(item => item.id);
      const cancelPromises: Promise<any>[] = [];

      // Create cancel promises for all selected tasks
      for (let i = 0, len = taskIds.length; i < len; i++) {
        cancelPromises.push(task.cancelTask(taskIds[i], { silence: true }));
      }

      Promise.all(cancelPromises).then((results: [Error | null, any][]) => {
        const failedTaskIds: string[] = [];

        // Identify failed operations
        for (let i = 0, len = results.length; i < len; i++) {
          if (results[i][0]) {
            failedTaskIds.push(taskIds[i]);
          }
        }

        const failureCount = failedTaskIds.length;

        // Handle complete success
        if (failureCount === 0) {
          emit('refreshChange');
          notification.success(t('task.detail.batchActions.cancelSuccess', { num: selectedCount }));
          emit('batchAction', 'cancel', taskIds);
          emit('update:selectedIds', []);
          selectedTaskDataMap.value = {};
          return;
        }

        // Handle complete failure
        if (failureCount === selectedCount) {
          notification.error(t('task.detail.batchActions.cancelFailed', { num: selectedCount }));
          return;
        }

        // Handle partial success
        const successfulTaskIds = taskIds.filter(item => !failedTaskIds.includes(item));
        notification.warning(t('task.detail.batchActions.cancelPartialSuccess', {
          num: selectedCount - failureCount,
          errorNum: failureCount
        }));

        emit('refreshChange');
        emit('batchAction', 'cancel', successfulTaskIds);

        // Update selection to only include failed tasks
        const remainingIds = props.selectedIds.filter((item) => !successfulTaskIds.includes(item));
        emit('update:selectedIds', remainingIds);

        // Clean up successful tasks from data map
        for (let i = 0, len = successfulTaskIds.length; i < len; i++) {
          delete selectedTaskDataMap.value[successfulTaskIds[i]];
        }
      });
    }
  });
};

/**
 * <p>Executes batch delete operation on selected tasks</p>
 * <p>Shows confirmation dialog and deletes all selected tasks</p>
 * <p>Uses single API call for efficient batch deletion</p>
 */
const executeBatchDelete = async () => {
  const selectedCount = props.selectedIds.length;
  modal.confirm({
    content: t('task.detail.batchActions.confirmDelete', { num: selectedCount }),
    async onOk () {
      const taskIds = Object.values(selectedTaskDataMap.value).map(item => item.id);
      const [error] = await task.deleteTask(taskIds);

      if (error) {
        return;
      }

      emit('refreshChange');
      notification.success(t('task.detail.batchActions.deleteSuccess', { num: selectedCount }));
      emit('batchAction', 'delete', taskIds);
      emit('update:selectedIds', []);
      selectedTaskDataMap.value = {};
    }
  });
};

/**
 * <p>Executes batch favourite operation on selected tasks</p>
 * <p>Shows confirmation dialog and marks all selected tasks as favourites</p>
 * <p>Handles partial success scenarios and updates UI accordingly</p>
 */
const executeBatchFavourite = async () => {
  const selectedCount = props.selectedIds.length;
  modal.confirm({
    content: t('task.detail.batchActions.confirmFavourite', { num: selectedCount }),
    async onOk () {
      const taskIds = Object.values(selectedTaskDataMap.value).map(item => item.id);
      const favouritePromises: Promise<any>[] = [];

      // Create favourite promises for all selected tasks
      for (let i = 0, len = taskIds.length; i < len; i++) {
        favouritePromises.push(task.favouriteTask(taskIds[i], { silence: true }));
      }

      Promise.all(favouritePromises).then((results: [Error | null, any][]) => {
        const failedTaskIds: string[] = [];

        // Identify failed operations
        for (let i = 0, len = results.length; i < len; i++) {
          if (results[i][0]) {
            failedTaskIds.push(taskIds[i]);
          }
        }

        const failureCount = failedTaskIds.length;

        // Handle complete success
        if (failureCount === 0) {
          notification.success(t('task.detail.batchActions.favouriteSuccess', { num: selectedCount }));
          emit('batchAction', 'addFavourite', taskIds);
          emit('update:selectedIds', []);
          selectedTaskDataMap.value = {};
          return;
        }

        // Handle complete failure
        if (failureCount === selectedCount) {
          notification.error(t('task.detail.batchActions.favouriteFailed', { num: selectedCount }));
          return;
        }

        // Handle partial success
        const successfulTaskIds = taskIds.filter(item => !failedTaskIds.includes(item));
        notification.warning(t('task.detail.batchActions.favouritePartialSuccess', {
          num: selectedCount - failureCount,
          errorNum: failureCount
        }));

        emit('batchAction', 'addFavourite', successfulTaskIds);

        // Update selection to only include failed tasks
        const remainingIds = props.selectedIds.filter((item) => !successfulTaskIds.includes(item));
        emit('update:selectedIds', remainingIds);

        // Clean up successful tasks from data map
        for (let i = 0, len = successfulTaskIds.length; i < len; i++) {
          delete selectedTaskDataMap.value[successfulTaskIds[i]];
        }
      });
    }
  });
};

/**
 * <p>Executes batch cancel favourite operation on selected tasks</p>
 * <p>Shows confirmation dialog and removes favourite status from all selected tasks</p>
 * <p>Handles partial success scenarios and updates UI accordingly</p>
 */
const executeBatchCancelFavourite = async () => {
  const selectedCount = props.selectedIds.length;
  modal.confirm({
    content: t('task.detail.batchActions.confirmCancelFavourite', { num: selectedCount }),
    async onOk () {
      const taskIds = Object.values(selectedTaskDataMap.value).map(item => item.id);
      const cancelFavouritePromises: Promise<any>[] = [];

      // Create cancel favourite promises for all selected tasks
      for (let i = 0, len = taskIds.length; i < len; i++) {
        cancelFavouritePromises.push(task.cancelFavouriteTask(taskIds[i], { silence: true }));
      }

      Promise.all(cancelFavouritePromises).then((results: [Error | null, any][]) => {
        const failedTaskIds: string[] = [];

        // Identify failed operations
        for (let i = 0, len = results.length; i < len; i++) {
          if (results[i][0]) {
            failedTaskIds.push(taskIds[i]);
          }
        }

        const failureCount = failedTaskIds.length;

        // Handle complete success
        if (failureCount === 0) {
          notification.success(t('task.detail.batchActions.cancelFavouriteSuccess', { num: selectedCount }));
          emit('batchAction', 'addFavourite', taskIds);
          emit('update:selectedIds', []);
          selectedTaskDataMap.value = {};
          return;
        }

        // Handle complete failure
        if (failureCount === selectedCount) {
          notification.error(t('task.detail.batchActions.cancelFavouriteFailed', { num: selectedCount }));
          return;
        }

        // Handle partial success
        const successfulTaskIds = taskIds.filter(item => !failedTaskIds.includes(item));
        notification.warning(t('task.detail.batchActions.cancelFavouritePartialSuccess', {
          num: selectedCount - failureCount,
          errorNum: failureCount
        }));

        emit('batchAction', 'addFavourite', successfulTaskIds);

        // Update selection to only include failed tasks
        const remainingIds = props.selectedIds.filter((item) => !successfulTaskIds.includes(item));
        emit('update:selectedIds', remainingIds);

        // Clean up successful tasks from data map
        for (let i = 0, len = successfulTaskIds.length; i < len; i++) {
          delete selectedTaskDataMap.value[successfulTaskIds[i]];
        }
      });
    }
  });
};

/**
 * <p>Executes batch follow operation on selected tasks</p>
 * <p>Shows confirmation dialog and follows all selected tasks</p>
 * <p>Handles partial success scenarios and updates UI accordingly</p>
 */
const executeBatchFollow = async () => {
  const selectedCount = props.selectedIds.length;
  modal.confirm({
    content: t('task.detail.batchActions.confirmFollow', { num: selectedCount }),
    async onOk () {
      const taskIds = Object.values(selectedTaskDataMap.value).map(item => item.id);
      const followPromises: Promise<any>[] = [];

      // Create follow promises for all selected tasks
      for (let i = 0, len = taskIds.length; i < len; i++) {
        followPromises.push(task.followTask(taskIds[i], { silence: true }));
      }

      Promise.all(followPromises).then((results: [Error | null, any][]) => {
        const failedTaskIds: string[] = [];

        // Identify failed operations
        for (let i = 0, len = results.length; i < len; i++) {
          if (results[i][0]) {
            failedTaskIds.push(taskIds[i]);
          }
        }

        const failureCount = failedTaskIds.length;

        // Handle complete success
        if (failureCount === 0) {
          notification.success(t('task.detail.batchActions.cancelFollowSuccess', { num: selectedCount }));
          emit('batchAction', 'addFavourite', taskIds);
          emit('update:selectedIds', []);
          selectedTaskDataMap.value = {};
          return;
        }

        // Handle complete failure
        if (failureCount === selectedCount) {
          notification.error(t('task.detail.batchActions.cancelFollowFailed', { num: selectedCount }));
          return;
        }

        // Handle partial success
        const successfulTaskIds = taskIds.filter(item => !failedTaskIds.includes(item));
        notification.warning(t('task.detail.batchActions.cancelFollowPartialSuccess', {
          num: selectedCount - failureCount,
          errorNum: failureCount
        }));

        emit('batchAction', 'addFavourite', successfulTaskIds);

        // Update selection to only include failed tasks
        const remainingIds = props.selectedIds.filter((item) => !successfulTaskIds.includes(item));
        emit('update:selectedIds', remainingIds);

        // Clean up successful tasks from data map
        for (let i = 0, len = successfulTaskIds.length; i < len; i++) {
          delete selectedTaskDataMap.value[successfulTaskIds[i]];
        }
      });
    }
  });
};

/**
 * <p>Executes batch cancel follow operation on selected tasks</p>
 * <p>Shows confirmation dialog and unfollows all selected tasks</p>
 * <p>Handles partial success scenarios and updates UI accordingly</p>
 */
const executeBatchCancelFollow = async () => {
  const selectedCount = props.selectedIds.length;
  modal.confirm({
    content: t('task.detail.batchActions.cancelFollowConfirm', { num: selectedCount }),
    async onOk () {
      const taskIds = Object.values(selectedTaskDataMap.value).map(item => item.id);
      const cancelFollowPromises: Promise<any>[] = [];

      // Create cancel follow promises for all selected tasks
      for (let i = 0, len = taskIds.length; i < len; i++) {
        cancelFollowPromises.push(task.cancelFollowTask(taskIds[i], { silence: true }));
      }

      Promise.all(cancelFollowPromises).then((results: [Error | null, any][]) => {
        const failedTaskIds: string[] = [];

        // Identify failed operations
        for (let i = 0, len = results.length; i < len; i++) {
          if (results[i][0]) {
            failedTaskIds.push(taskIds[i]);
          }
        }

        const failureCount = failedTaskIds.length;

        // Handle complete success
        if (failureCount === 0) {
          notification.success(t('task.detail.batchActions.cancelFollowNumSuccess', { num: selectedCount }));
          emit('batchAction', 'addFavourite', taskIds);
          emit('update:selectedIds', []);
          selectedTaskDataMap.value = {};
          return;
        }

        // Handle complete failure
        if (failureCount === selectedCount) {
          notification.error(t('task.detail.batchActions.cancelFollowFail', { num: selectedCount }));
          return;
        }

        // Handle partial success
        const successfulTaskIds = taskIds.filter(item => !failedTaskIds.includes(item));
        notification.warning(t('task.detail.batchActions.cancelFollowPartialSuccess', {
          num: selectedCount - failureCount,
          errorNum: failureCount
        }));

        emit('batchAction', 'addFavourite', successfulTaskIds);

        // Update selection to only include failed tasks
        const remainingIds = props.selectedIds.filter((item) => !successfulTaskIds.includes(item));
        emit('update:selectedIds', remainingIds);

        // Clean up successful tasks from data map
        for (let i = 0, len = successfulTaskIds.length; i < len; i++) {
          delete selectedTaskDataMap.value[successfulTaskIds[i]];
        }
      });
    }
  });
};

/**
 * <p>Opens the move task modal for batch movement</p>
 * <p>Shows modal to select destination sprint for selected tasks</p>
 */
const openBatchMoveModal = () => {
  isMoveModalVisible.value = true;
};

/**
 * <p>Handles successful batch move operation</p>
 * <p>Clears selections and emits move action to parent component</p>
 */
const handleBatchMoveSuccess = async (_sprintId: string, taskIds: string[]) => {
  emit('batchAction', 'move', taskIds);
  emit('update:selectedIds', []);
  selectedTaskDataMap.value = {};
};

/**
 * <p>Computes menu items for the currently selected task</p>
 * <p>Returns available action menu items based on task permissions and state</p>
 */
const currentTaskMenuItems = computed(() => {
  const taskId = currentSelectedTask.value?.id;
  if (!taskId) {
    return [];
  }

  return props.menuItemsMap.get(taskId);
});

// Lifecycle and Watchers
onMounted(() => {
  /**
   * <p>Watches for changes in data source to update selected task</p>
   * <p>Ensures the currently selected task remains valid when data changes</p>
   */
  watch(() => props.dataSource, (newDataSource) => {
    if (!newDataSource?.length) {
      return;
    }

    // Set first task as selected if none is currently selected
    if (!currentSelectedTask.value) {
      currentSelectedTask.value = newDataSource[0];
      return;
    }

    // Check if currently selected task still exists in new data
    const currentTaskId = currentSelectedTask.value.id;
    const taskStillExists = !!newDataSource.find(item => item.id === currentTaskId);

    if (!taskStillExists) {
      currentSelectedTask.value = newDataSource[0];
    }
  }, { immediate: true, deep: true });

  /**
   * <p>Watches for changes in selected task data to update batch operation button states</p>
   * <p>Enables/disables batch operation buttons based on task properties and permissions</p>
   */
  watch(() => selectedTaskDataMap.value, (newSelectedData) => {
    // Reset all button states
    isBatchCancelDisabled.value = false;
    isBatchDeleteDisabled.value = false;
    isBatchMoveDisabled.value = false;
    isBatchFavouriteDisabled.value = false;
    isBatchCancelFavouriteDisabled.value = false;
    isBatchFollowDisabled.value = false;
    isBatchCancelFollowDisabled.value = false;

    const selectedTasks = (Object.values(newSelectedData) || []) as {
      favourite: boolean;
      follow: boolean;
      id: string;
      status: string;
    }[];

    // Analyze each selected task to determine button states
    for (let i = 0, len = selectedTasks.length; i < len; i++) {
      const { favourite, follow, status, id } = selectedTasks[i];
      const menuItems = props.menuItemsMap.get(id) || [];
      const cancelItem = menuItems.find(item => item.key === 'cancel');
      const deleteItem = menuItems.find(item => item.key === 'delete');
      const moveItem = menuItems.find(item => item.key === 'move');

      // Handle favourite button states
      if (favourite) {
        isBatchFavouriteDisabled.value = true;
      } else {
        isBatchCancelFavouriteDisabled.value = true;
      }

      // Handle follow button states
      if (follow) {
        isBatchFollowDisabled.value = true;
      } else {
        isBatchCancelFollowDisabled.value = true;
      }

      // Handle cancel button state based on task status and permissions
      if ([TaskStatus.CANCELED, TaskStatus.COMPLETED].includes(status as TaskStatus) || cancelItem?.disabled) {
        isBatchCancelDisabled.value = true;
      }

      // Handle delete button state based on permissions
      if (deleteItem?.disabled) {
        isBatchDeleteDisabled.value = true;
      }

      // Handle move button state based on permissions
      if (moveItem?.disabled) {
        isBatchMoveDisabled.value = true;
      }
    }
  }, { immediate: true, deep: true });
});
</script>
<template>
  <div class="flex-1 overflow-y-hidden">
    <div
      :visible="!!selectedIds.length"
      class="btn-group-container flex items-center transition-all duration-300 space-x-2.5">
      <Button
        :disabled="isBatchCancelDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="executeBatchCancel">
        {{ t('actions.cancel') }}
      </Button>

      <Button
        :disabled="isBatchDeleteDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="executeBatchDelete">
        {{ t('actions.delete') }}
      </Button>

      <Button
        :disabled="isBatchFavouriteDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="executeBatchFavourite">
        {{ t('actions.addFavourite') }}
      </Button>

      <Button
        :disabled="isBatchCancelFavouriteDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="executeBatchCancelFavourite">
        {{ t('actions.cancelFavourite') }}
      </Button>

      <Button
        :disabled="isBatchFollowDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="executeBatchFollow">
        {{ t('actions.addFollow') }}
      </Button>

      <Button
        :disabled="isBatchCancelFollowDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="executeBatchCancelFollow">
        {{ t('actions.cancelFollow') }}
      </Button>

      <Button
        :disabled="isBatchMoveDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="openBatchMoveModal">
        {{ t('actions.move') }}
      </Button>

      <Button
        danger
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="clearAllSelections">
        <span>{{ t('actions.cancelBatch') }}</span>
        <span class="ml-1">({{ selectedIds.length }})</span>
      </Button>
    </div>

    <div class="h-full relative flex-1 flex items-start overflow-hidden leading-5 detail-container">
      <List
        :dataSource="props.dataSource"
        :pagination="props.pagination"
        :checkedId="currentSelectedTask?.id"
        :selectedIds="props.selectedIds"
        @paginationChange="handlePaginationChange"
        @checked="handleSingleTaskSelection"
        @select="handleMultiTaskSelection" />

      <AsyncComponent :visible="!!currentSelectedTask">
        <Details
          :id="currentSelectedTask?.id"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :menuItems="currentTaskMenuItems"
          :editTaskData="editTaskData"
          :linkUrl="currentSelectedTask?.linkUrl"
          type="list"
          @edit="handleEditTask"
          @move="handleMoveTask"
          @delete="handleDeleteTask"
          @dataChange="handleTaskDataChange"
          @refreshChange="handleRefreshRequest"
          @splitOk="emit('splitOk')" />
      </AsyncComponent>
    </div>

    <AsyncComponent :visible="isMoveModalVisible">
      <MoveTaskModal
        v-model:visible="isMoveModalVisible"
        :taskIds="props.selectedIds.join(',')"
        :projectId="props.projectId"
        @ok="handleBatchMoveSuccess" />
    </AsyncComponent>
  </div>
</template>

<style scoped>
.detail-container::after {
  content: '';
  display: block;
  position: absolute;
  top: 0;
  left: 0;
  width: calc(100% - 20px);
  height: 1px;
  background-color: var(--border-text-box);
}

.btn-group-container {
  height: 0;
  overflow: hidden;
  opacity: 0;
}

.btn-group-container[visible="true"] {
  height: 28px;
  opacity: 1;
}
</style>
