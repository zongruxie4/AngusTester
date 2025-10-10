<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, TableColumnProps } from 'ant-design-vue';
import {
  AsyncComponent, Dropdown, Icon, IconTask, modal, notification, Table
} from '@xcan-angus/vue-ui';
import { toClipboard, PageQuery } from '@xcan-angus/infra';
import { issue } from '@/api/tester';
import { TaskStatus as TaskStatusType } from '@/enums/enums';
import TaskPriority from '@/components/TaskPriority/index.vue';

import TaskStatus from '@/components/TaskStatus/index.vue';
import { TaskDetail } from '@/views/issue/types';
import { ActionMenuItem } from '@/views/issue/issue/types';

const { t } = useI18n();

const MoveTaskModal = defineAsyncComponent(() => import('@/views/issue/issue/list/Move.vue'));

// Type Definitions
type Props = {
  projectId: number;
  selectedIds: number[];
  dataSource: TaskDetail[];
  pagination: { current: number; pageSize: number; total: number; };
  menuItemsMap: Map<number, ActionMenuItem[]>;
  loading: boolean;
}

// Component Props & Emit
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  selectedIds: () => [],
  dataSource: () => [],
  pagination: () => ({ current: 1, pageSize: 10, total: 0 }),
  menuItemsMap: () => new Map(),
  loading: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'update:selectedIds', value: number[]): void;
  (event: 'update:loading', value: boolean): void;
  (event: 'tableChange', pagination: { current: number; pageSize: number }, sorter: {
    orderBy: string;
    orderSort: PageQuery.OrderSort
  }): void;
  (event: 'edit', value: number): void;
  (event: 'move', value: TaskDetail): void;
  (event: 'delete', value: number): void;
  (event: 'dataChange', value: Partial<TaskDetail>): void;
  (event: 'refreshChange'): void;
  (event: 'batchAction', type: 'cancel' | 'delete' | 'addFollow' | 'cancelFollow' | 'addFavourite' | 'cancelFavourite' | 'move', value: number[]): void;
}>();

// Constants
const MAX_BATCH_SELECTION_LIMIT = 200;

// Reactive State
const moveModalVisible = ref(false);
const selectedTaskDataMap = ref<{
  [key: string]: {
    id: number;
    status: TaskDetail['status']['value'];
    favourite: boolean;
    follow: boolean;
    sprintId: number;
  }
}>({});

// Batch operation button disabled states
const isBatchCancelDisabled = ref(false);
const isBatchDeleteDisabled = ref(false);
const isBatchMoveDisabled = ref(false);
const isBatchFavouriteDisabled = ref(false);
const isBatchCancelFavouriteDisabled = ref(false);
const isBatchFollowDisabled = ref(false);
const isBatchCancelFollowDisabled = ref(false);

/**
 * Handles table row selection changes
 * <p>Updates the selected task data map and validates selection limits
 * @param selectedKeys - Array of selected task IDs
 */
const handleTableSelection = (selectedKeys: number[]) => {
  // Remove deselected tasks from the data map
  const deselectedTaskIds = props.dataSource.reduce((prev, current) => {
    const taskId = current.id;
    if (!selectedKeys.includes(taskId)) {
      prev.push(current.id);
      delete selectedTaskDataMap.value[taskId];
    } else {
      // Add or update selected task data
      selectedTaskDataMap.value[taskId] = {
        id: taskId,
        status: current.status.value,
        favourite: current.favourite,
        follow: current.follow,
        sprintId: current.sprintId
      };
    }
    return prev;
  }, [] as number[]);

  // Filter out deselected tasks from current selection
  const currentSelectedKeys = rowSelection.value.selectedRowKeys.filter(item => !deselectedTaskIds.includes(item));

  // Add new selections to current selection
  for (let i = 0, len = selectedKeys.length; i < len; i++) {
    if (!currentSelectedKeys.includes(selectedKeys[i])) {
      currentSelectedKeys.push(selectedKeys[i]);
    }
  }

  const selectedCount = currentSelectedKeys.length;
  // Validate selection limit
  if (selectedCount > MAX_BATCH_SELECTION_LIMIT) {
    notification.info(t('issue.detail.messages.maxBatchLimit', { maxNum: MAX_BATCH_SELECTION_LIMIT, selectedNum: selectedCount }));
  }

  rowSelection.value.selectedRowKeys = currentSelectedKeys;
};

// Table Configuration
const rowSelection = ref<{
  onChange:(key: number[]) => void;
  getCheckboxProps: (data: TaskDetail) => ({ disabled: boolean; });
  selectedRowKeys: number[];
    }>({
      onChange: handleTableSelection,
      getCheckboxProps: () => {
        return {
          disabled: false
        };
      },
      selectedRowKeys: []
    });

/**
 * Handles table pagination and sorting changes
 * @param pagination - Pagination configuration
 * @param _filters - Table filters (unused)
 * @param sorter - Sorting configuration
 */
const handleTableChange = (
  pagination: { current: number; pageSize: number; },
  _filters: { [key: string]: any }[],
  sorter: {
  orderBy: string;
  orderSort: PageQuery.OrderSort
}) => {
  emit('tableChange', pagination, sorter);
};

/**
 * Clears all batch selections and resets the selection state
 */
const clearBatchSelection = () => {
  rowSelection.value.selectedRowKeys = [];
  selectedTaskDataMap.value = {};
};

/**
 * Performs batch cancel operation on selected tasks
 * <p>Shows confirmation dialog and processes all selected tasks
 */
const executeBatchCancel = async () => {
  const selectedCount = rowSelection.value.selectedRowKeys.length;
  modal.confirm({
    content: t('actions.tips.confirmCancel', { num: selectedCount }),
    async onOk () {
      const taskIds = Object.values(selectedTaskDataMap.value).map(item => item.id);
      const cancelPromises: Promise<any>[] = [];

      // Create cancel task promises
      for (let i = 0, len = taskIds.length; i < len; i++) {
        cancelPromises.push(issue.cancelTask(taskIds[i], { silence: true }));
      }

      Promise.all(cancelPromises).then((results: [Error | null, any][]) => {
        const failedTaskIds: number[] = [];

        // Collect failed task IDs
        for (let i = 0, len = results.length; i < len; i++) {
          if (results[i][0]) {
            failedTaskIds.push(taskIds[i]);
          }
        }

        const failedCount = failedTaskIds.length;

        // All tasks cancelled successfully
        if (failedCount === 0) {
          emit('refreshChange');
          notification.success(t('issue.detail.messages.cancelSuccess', { num: selectedCount }));
          emit('batchAction', 'cancel', taskIds);
          rowSelection.value.selectedRowKeys = [];
          selectedTaskDataMap.value = {};
          return;
        }

        // All tasks failed
        if (failedCount === selectedCount) {
          notification.error(t('issue.detail.messages.cancelFailed', { num: selectedCount }));
          return;
        }

        // Partial success - handle mixed results
        const successTaskIds = taskIds.filter(item => !failedTaskIds.includes(item));
        notification.warning(t('issue.detail.messages.cancelPartialSuccess', { num: selectedCount - failedCount, errorNum: failedCount }));

        emit('refreshChange');
        emit('batchAction', 'cancel', successTaskIds);

        // Remove successful tasks from selection
        rowSelection.value.selectedRowKeys = rowSelection.value.selectedRowKeys.filter((item) => !successTaskIds.includes(item));
        for (let i = 0, len = successTaskIds.length; i < len; i++) {
          delete selectedTaskDataMap.value[successTaskIds[i]];
        }
      });
    }
  });
};

/**
 * Performs batch delete operation on selected tasks
 * <p>Shows confirmation dialog and deletes all selected tasks
 */
const executeBatchDelete = async () => {
  const selectedCount = rowSelection.value.selectedRowKeys.length;
  modal.confirm({
    content: t('actions.tips.confirmCountDelete', { num: selectedCount }),
    async onOk () {
      const taskIds = Object.values(selectedTaskDataMap.value).map(item => item.id);
      const [error] = await issue.deleteTask(taskIds);
      if (error) {
        return;
      }

      emit('refreshChange');
      notification.success(t('actions.tips.deleteSuccess'));
      emit('batchAction', 'delete', taskIds);
      rowSelection.value.selectedRowKeys = [];
      selectedTaskDataMap.value = {};
    }
  });
};

/**
 * Performs batch favourite operation on selected tasks
 * <p>Shows confirmation dialog and adds all selected tasks to favourites
 */
const executeBatchFavourite = async () => {
  const selectedCount = rowSelection.value.selectedRowKeys.length;
  modal.confirm({
    content: t('issue.detail.messages.confirmFavourite', { num: selectedCount }),
    async onOk () {
      const taskIds = Object.values(selectedTaskDataMap.value).map(item => item.id);
      const favouritePromises: Promise<any>[] = [];

      // Create favourite task promises
      for (let i = 0, len = taskIds.length; i < len; i++) {
        favouritePromises.push(issue.favouriteTask(taskIds[i], { silence: true }));
      }

      Promise.all(favouritePromises).then((results: [Error | null, any][]) => {
        const failedTaskIds: number[] = [];

        // Collect failed task IDs
        for (let i = 0, len = results.length; i < len; i++) {
          if (results[i][0]) {
            failedTaskIds.push(taskIds[i]);
          }
        }

        const failedCount = failedTaskIds.length;

        // All tasks favourited successfully
        if (failedCount === 0) {
          notification.success(t('issue.detail.messages.favouriteSuccess', { num: selectedCount }));
          emit('batchAction', 'addFavourite', taskIds);
          rowSelection.value.selectedRowKeys = [];
          selectedTaskDataMap.value = {};
          return;
        }

        // All tasks failed
        if (failedCount === selectedCount) {
          notification.error(t('issue.detail.messages.favouriteFailed', { num: selectedCount }));
          return;
        }

        // Partial success - handle mixed results
        const successTaskIds = taskIds.filter(item => !failedTaskIds.includes(item));
        notification.warning(t('issue.detail.messages.favouritePartialSuccess', { num: selectedCount - failedCount, errorNum: failedCount }));

        emit('batchAction', 'addFavourite', successTaskIds);

        // Remove successful tasks from selection
        rowSelection.value.selectedRowKeys = rowSelection.value.selectedRowKeys.filter((item) => !successTaskIds.includes(item));
        for (let i = 0, len = successTaskIds.length; i < len; i++) {
          delete selectedTaskDataMap.value[successTaskIds[i]];
        }
      });
    }
  });
};

/**
 * Performs batch cancel favourite operation on selected tasks
 * <p>Shows confirmation dialog and removes all selected tasks from favourites
 */
const executeBatchCancelFavourite = async () => {
  const selectedCount = rowSelection.value.selectedRowKeys.length;
  modal.confirm({
    content: t('issue.detail.messages.confirmCancelFavourite', { num: selectedCount }),
    async onOk () {
      const taskIds = Object.values(selectedTaskDataMap.value).map(item => item.id);
      const cancelFavouritePromises: Promise<any>[] = [];

      // Create cancel favourite task promises
      for (let i = 0, len = taskIds.length; i < len; i++) {
        cancelFavouritePromises.push(issue.cancelFavouriteTask(taskIds[i], { silence: true }));
      }

      Promise.all(cancelFavouritePromises).then((results: [Error | null, any][]) => {
        const failedTaskIds: number[] = [];

        // Collect failed task IDs
        for (let i = 0, len = results.length; i < len; i++) {
          if (results[i][0]) {
            failedTaskIds.push(taskIds[i]);
          }
        }

        const failedCount = failedTaskIds.length;

        // All tasks unfavourited successfully
        if (failedCount === 0) {
          notification.success(t('issue.detail.messages.cancelFavouriteSuccess', { num: selectedCount }));
          emit('batchAction', 'addFavourite', taskIds);
          rowSelection.value.selectedRowKeys = [];
          selectedTaskDataMap.value = {};
          return;
        }

        // All tasks failed
        if (failedCount === selectedCount) {
          notification.error(t('issue.detail.messages.cancelFavouriteFailed', { num: selectedCount }));
          return;
        }

        // Partial success - handle mixed results
        const successTaskIds = taskIds.filter(item => !failedTaskIds.includes(item));
        notification.warning(t('issue.detail.messages.cancelFavouritePartialSuccess', { num: selectedCount - failedCount, errorNum: failedCount }));

        emit('batchAction', 'addFavourite', successTaskIds);

        // Remove successful tasks from selection
        rowSelection.value.selectedRowKeys = rowSelection.value.selectedRowKeys.filter((item) => !successTaskIds.includes(item));
        for (let i = 0, len = successTaskIds.length; i < len; i++) {
          delete selectedTaskDataMap.value[successTaskIds[i]];
        }
      });
    }
  });
};

/**
 * Performs batch follow operation on selected tasks
 * <p>Shows confirmation dialog and follows all selected tasks
 */
const executeBatchFollow = async () => {
  const selectedCount = rowSelection.value.selectedRowKeys.length;
  modal.confirm({
    content: t('issue.detail.messages.confirmFollow', { num: selectedCount }),
    async onOk () {
      const taskIds = Object.values(selectedTaskDataMap.value).map(item => item.id);
      const followPromises: Promise<any>[] = [];

      // Create follow task promises
      for (let i = 0, len = taskIds.length; i < len; i++) {
        followPromises.push(issue.followTask(taskIds[i], { silence: true }));
      }

      Promise.all(followPromises).then((results: [Error | null, any][]) => {
        const failedTaskIds: number[] = [];

        // Collect failed task IDs
        for (let i = 0, len = results.length; i < len; i++) {
          if (results[i][0]) {
            failedTaskIds.push(taskIds[i]);
          }
        }

        const failedCount = failedTaskIds.length;

        // All tasks followed successfully
        if (failedCount === 0) {
          notification.success(t('issue.detail.messages.followSuccess', { num: selectedCount }));
          emit('batchAction', 'addFollow', taskIds);
          rowSelection.value.selectedRowKeys = [];
          selectedTaskDataMap.value = {};
          return;
        }

        // All tasks failed
        if (failedCount === selectedCount) {
          notification.error(t('issue.detail.messages.followFailed', { num: selectedCount }));
          return;
        }

        // Partial success - handle mixed results
        const successTaskIds = taskIds.filter(item => !failedTaskIds.includes(item));
        notification.warning(t('issue.detail.messages.followPartialSuccess', { successNum: selectedCount - failedCount, errorNum: failedCount }));

        emit('batchAction', 'addFollow', successTaskIds);

        // Remove successful tasks from selection
        rowSelection.value.selectedRowKeys = rowSelection.value.selectedRowKeys.filter((item) => !successTaskIds.includes(item));
        for (let i = 0, len = successTaskIds.length; i < len; i++) {
          delete selectedTaskDataMap.value[successTaskIds[i]];
        }
      });
    }
  });
};

/**
 * Performs batch cancel follow operation on selected tasks
 * <p>Shows confirmation dialog and unfollows all selected tasks
 */
const executeBatchCancelFollow = async () => {
  const selectedCount = rowSelection.value.selectedRowKeys.length;
  modal.confirm({
    content: t('issue.detail.messages.confirmCancelFollow', { num: selectedCount }),
    async onOk () {
      const taskIds = Object.values(selectedTaskDataMap.value).map(item => item.id);
      const cancelFollowPromises: Promise<any>[] = [];

      // Create cancel follow task promises
      for (let i = 0, len = taskIds.length; i < len; i++) {
        cancelFollowPromises.push(issue.cancelFollowTask(taskIds[i], { silence: true }));
      }

      Promise.all(cancelFollowPromises).then((results: [Error | null, any][]) => {
        const failedTaskIds: number[] = [];

        // Collect failed task IDs
        for (let i = 0, len = results.length; i < len; i++) {
          if (results[i][0]) {
            failedTaskIds.push(taskIds[i]);
          }
        }

        const failedCount = failedTaskIds.length;

        // All tasks unfollowed successfully
        if (failedCount === 0) {
          notification.success(t('issue.detail.messages.cancelFollowSuccess', { num: selectedCount }));
          emit('batchAction', 'cancelFollow', taskIds);
          rowSelection.value.selectedRowKeys = [];
          selectedTaskDataMap.value = {};
          return;
        }

        // All tasks failed
        if (failedCount === selectedCount) {
          notification.error(t('issue.detail.messages.cancelFollowFailed', { num: selectedCount }));
          return;
        }

        // Partial success - handle mixed results
        const successTaskIds = taskIds.filter(item => !failedTaskIds.includes(item));
        notification.warning(t('issue.detail.messages.cancelFollowPartialSuccess', { num: selectedCount - failedCount, errorNum: failedCount }));

        emit('batchAction', 'cancelFollow', successTaskIds);

        // Remove successful tasks from selection
        rowSelection.value.selectedRowKeys = rowSelection.value.selectedRowKeys.filter((item) => !successTaskIds.includes(item));
        for (let i = 0, len = successTaskIds.length; i < len; i++) {
          delete selectedTaskDataMap.value[successTaskIds[i]];
        }
      });
    }
  });
};

/**
 * Opens the batch move modal for selected tasks
 */
const openBatchMoveModal = () => {
  moveModalVisible.value = true;
};

/**
 * Handles successful batch move operation
 * @param _sprintId - Target sprint ID (unused)
 * @param taskIds - Array of moved task IDs
 */
const handleBatchMoveSuccess = async (_sprintId: number, taskIds: number[]) => {
  emit('batchAction', 'move', taskIds);
  rowSelection.value.selectedRowKeys = [];
  selectedTaskDataMap.value = {};
};

/**
 * Navigates to task edit page
 * @param taskId - ID of the task to edit
 */
const navigateToEdit = (taskId: number) => {
  emit('edit', taskId);
};

/**
 * Deletes a single task with confirmation
 * @param taskData - Task information to delete
 */
const deleteSingleTask = (taskData: TaskDetail) => {
  modal.confirm({
    content: t('actions.tips.confirmDelete', { name: taskData.name }),
    async onOk () {
      const [error] = await issue.deleteTask([taskData.id]);
      if (error) {
        return;
      }

      emit('refreshChange');
      emit('delete', taskData.id);
      notification.success(t('actions.tips.deleteSuccess'));
    }
  });
};

/**
 * Handles dropdown menu item clicks for task actions
 * @param menuItem - The clicked menu item
 * @param taskData - The task data associated with the action
 */
const handleDropdownAction = (menuItem: ActionMenuItem, taskData: TaskDetail) => {
  const actionKey = menuItem.key;

  switch (actionKey) {
    case 'addFavourite':
      addToFavourites(taskData);
      break;
    case 'cancelFavourite':
      removeFromFavourites(taskData);
      break;
    case 'addFollow':
      followTask(taskData);
      break;
    case 'cancelFollow':
      unfollowTask(taskData);
      break;
    case 'start':
      startTask(taskData);
      break;
    case 'processed':
      markAsProcessed(taskData);
      break;
    case 'completed':
      completeTask(taskData);
      break;
    case 'uncompleted':
      markAsUncompleted(taskData);
      break;
    case 'reopen':
      reopenTask(taskData);
      break;
    case 'restart':
      restartTask(taskData);
      break;
    case 'cancel':
      cancelTask(taskData);
      break;
    case 'move':
      moveTask(taskData);
      break;
    case 'copyLink':
      copyTaskLink(taskData);
      break;
  }
};

/**
 * Adds a task to favourites
 * @param taskData - Task to add to favourites
 */
const addToFavourites = async (taskData: TaskDetail) => {
  const [error] = await issue.favouriteTask(taskData.id);
  if (error) {
    return;
  }

  notification.success(t('actions.tips.favouriteSuccess'));
  emit('dataChange', { id: taskData.id, favourite: true });
};

/**
 * Removes a task from favourites
 * @param taskData - Task to remove from favourites
 */
const removeFromFavourites = async (taskData: TaskDetail) => {
  const [error] = await issue.cancelFavouriteTask(taskData.id);
  if (error) {
    return;
  }

  notification.success(t('actions.tips.cancelFavouriteSuccess'));
  emit('dataChange', { id: taskData.id, favourite: false });
};

/**
 * Follows a task
 * @param taskData - Task to follow
 */
const followTask = async (taskData: TaskDetail) => {
  const [error] = await issue.followTask(taskData.id);
  if (error) {
    return;
  }

  notification.success(t('actions.tips.followSuccess'));
  emit('dataChange', { id: taskData.id, follow: true });
};

/**
 * Unfollows a task
 * @param taskData - Task to unfollow
 */
const unfollowTask = async (taskData: TaskDetail) => {
  const [error] = await issue.cancelFollowTask(taskData.id);
  if (error) {
    return;
  }

  notification.success(t('actions.tips.cancelFollowSuccess'));
  emit('dataChange', { id: taskData.id, follow: false });
};

/**
 * Starts a task
 * @param taskData - Task to start
 */
const startTask = async (taskData: TaskDetail) => {
  const taskId = taskData.id;
  const [error] = await issue.startTask(taskId);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('actions.tips.startSuccess'));
  const updatedTaskData = await loadTaskDetail(taskId);
  emit('dataChange', updatedTaskData);
};

/**
 * Marks a task as processed
 * @param taskData - Task to mark as processed
 */
const markAsProcessed = async (taskData: TaskDetail) => {
  const taskId = taskData.id;
  const [error] = await issue.processedTask(taskId);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('actions.tips.processSuccess'));
  const updatedTaskData = await loadTaskDetail(taskId);
  emit('dataChange', updatedTaskData);
};

/**
 * Marks a task as uncompleted
 * @param taskData - Task to mark as uncompleted
 */
const markAsUncompleted = async (taskData: TaskDetail) => {
  const taskId = taskData.id;
  const [error] = await issue.confirmTask(taskId, 'FAIL');
  if (error) {
    return;
  }

  emit('refreshChange');
  const updatedTaskData = await loadTaskDetail(taskId);
  emit('dataChange', updatedTaskData);
};

/**
 * Completes a task
 * @param taskData - Task to complete
 */
const completeTask = async (taskData: TaskDetail) => {
  const taskId = taskData.id;
  const [error] = await issue.confirmTask(taskId, 'SUCCESS');
  if (error) {
    return;
  }

  emit('refreshChange');
  const updatedTaskData = await loadTaskDetail(taskId);
  emit('dataChange', updatedTaskData);
};

/**
 * Reopens a task
 * @param taskData - Task to reopen
 */
const reopenTask = async (taskData: TaskDetail) => {
  const taskId = taskData.id;
  const [error] = await issue.reopenTask(taskId);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('actions.tips.reopenSuccess'));
  const updatedTaskData = await loadTaskDetail(taskId);
  emit('dataChange', updatedTaskData);
};

/**
 * Restarts a task
 * @param taskData - Task to restart
 */
const restartTask = async (taskData: TaskDetail) => {
  const taskId = taskData.id;
  const [error] = await issue.restartTask(taskId);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('actions.tips.restartSuccess'));
  const updatedTaskData = await loadTaskDetail(taskId);
  emit('dataChange', updatedTaskData);
};

/**
 * Moves a task to another sprint
 * @param taskData - Task to move
 */
const moveTask = (taskData: TaskDetail) => {
  emit('move', taskData);
};

/**
 * Cancels a task
 * @param taskData - Task to cancel
 */
const cancelTask = async (taskData: TaskDetail) => {
  const taskId = taskData.id;
  const [error] = await issue.cancelTask(taskId);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('actions.tips.cancelSuccess'));
  const updatedTaskData = await loadTaskDetail(taskId);
  emit('dataChange', updatedTaskData);
};

/**
 * Copies task link to clipboard
 * @param taskData - Task to copy link for
 */
const copyTaskLink = (taskData: TaskDetail) => {
  const taskUrl = window.location.origin + (taskData.linkUrl || '');
  toClipboard(taskUrl).then(() => {
    notification.success(t('actions.tips.copySuccess'));
  }).catch(() => {
    notification.error(t('actions.tips.copyFailed'));
  });
};

/**
 * Loads task detail data from API
 * @param taskId - ID of the task to load
 * @returns Promise resolving to partial task data
 */
const loadTaskDetail = async (taskId: number): Promise<Partial<TaskDetail>> => {
  emit('update:loading', true);
  const [error, response] = await issue.getTaskDetail(taskId);
  emit('update:loading', false);
  if (error || !response?.data) {
    return { id: taskId };
  }

  return response.data;
};

// Lifecycle & Watchers
onMounted(() => {
  // Watch for external selectedIds changes and sync with internal state
  watch(() => props.selectedIds, (newSelectedIds) => {
    rowSelection.value.selectedRowKeys = newSelectedIds;
  }, { immediate: true });

  // Watch for internal selection changes and emit to parent
  watch(() => rowSelection.value.selectedRowKeys, (newSelectedKeys) => {
    emit('update:selectedIds', newSelectedKeys);
  });

  // Watch for selected task data changes and update batch operation button states
  watch(() => selectedTaskDataMap.value, (newSelectedData) => {
    // Reset all batch operation disabled states
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
      id: number;
      status: string;
    }[];

    // Update button states based on selected task properties
    for (let i = 0, len = selectedTasks.length; i < len; i++) {
      const { favourite, follow, status, id } = selectedTasks[i];
      const taskMenuItems = props.menuItemsMap.get(id) || [];
      const cancelMenuItem = taskMenuItems.find(item => item.key === 'cancel');
      const deleteMenuItem = taskMenuItems.find(item => item.key === 'delete');
      const moveMenuItem = taskMenuItems.find(item => item.key === 'move');

      // Update favourite button states
      if (favourite) {
        isBatchFavouriteDisabled.value = true;
      } else {
        isBatchCancelFavouriteDisabled.value = true;
      }

      // Update follow button states
      if (follow) {
        isBatchFollowDisabled.value = true;
      } else {
        isBatchCancelFollowDisabled.value = true;
      }

      // Update cancel button state based on task status or menu item disabled state
      if ([TaskStatusType.CANCELED, TaskStatusType.COMPLETED].includes(status as TaskStatusType) || cancelMenuItem?.disabled) {
        isBatchCancelDisabled.value = true;
      }

      // Update delete button state based on menu item disabled state
      if (deleteMenuItem?.disabled) {
        isBatchDeleteDisabled.value = true;
      }

      // Update move button state based on menu item disabled state
      if (moveMenuItem?.disabled) {
        isBatchMoveDisabled.value = true;
      }
    }
  }, { immediate: true, deep: true });
});

// Table Configuration
const tableColumns: ({
  hide?: boolean,
  groupName?: string
} & TableColumnProps)[] = [
  {
    title: t('common.code'),
    dataIndex: 'code',
    width: 110,
    sorter: true,
    ellipsis: true
  },
  {
    title: t('common.name'),
    dataIndex: 'name',
    width: '45%',
    sorter: true,
    ellipsis: true
  },
  {
    title: t('common.sprint'),
    dataIndex: 'sprintName',
    groupName: 'target',
    width: '25%',
    ellipsis: true,
    customRender: ({ text }) => text || '--'
  },
  {
    title: t('common.resource'),
    hide: true,
    dataIndex: 'targetName',
    groupName: 'target',
    width: '25%',
    ellipsis: true,
    customRender: ({ text }) => text || '--'
  },
  {
    title: t('common.service'),
    hide: true,
    dataIndex: 'targetParentName',
    groupName: 'target',
    width: '25%',
    ellipsis: true,
    customRender: ({ text }) => text || '--'
  },
  {
    title: t('common.priority'),
    dataIndex: 'priority',
    width: 100,
    sorter: true,
    ellipsis: true
  },
  {
    title: t('common.status'),
    dataIndex: 'status',
    width: 120,
    groupName: 'status',
    sorter: true,
    ellipsis: true
  },
  {
    title: t('common.execResult'),
    dataIndex: 'execResult',
    width: 120,
    groupName: 'status',
    hide: true,
    sorter: true,
    ellipsis: true,
    customRender: ({ text }) => text || '--'
  },
  {
    title: t('common.assignee'),
    dataIndex: 'assigneeName',
    width: 110,
    groupName: 'personType',
    ellipsis: true,
    customRender: ({ text }) => text || '--'
  },
  {
    title: t('common.creator'),
    dataIndex: 'createdByName',
    width: 110,
    hide: true,
    groupName: 'personType',
    ellipsis: true
  },
  {
    title: t('common.confirmer'),
    dataIndex: 'confirmerName',
    width: 110,
    hide: true,
    groupName: 'personType',
    ellipsis: true,
    customRender: ({ text }) => text || '--'
  },
  {
    title: t('common.lastModifiedBy'),
    dataIndex: 'lastModifiedByName',
    width: 110,
    hide: true,
    groupName: 'personType',
    ellipsis: true
  },
  {
    title: t('common.executor'),
    dataIndex: 'execUserName',
    width: 110,
    hide: true,
    groupName: 'personType',
    ellipsis: true,
    customRender: ({ text }) => text || '--'
  },
  {
    title: t('common.workload'),
    dataIndex: 'evalWorkload',
    width: 100,
    sorter: true,
    ellipsis: true
  },
  {
    title: t('common.counts.processFailCount'),
    dataIndex: 'failNum',
    width: 130,
    hide: true,
    sorter: true,
    groupName: 'num',
    ellipsis: true,
    customRender: ({ text }) => text || '--'
  },
  {
    title: t('common.counts.processCount'),
    dataIndex: 'totalNum',
    width: 130,
    sorter: true,
    groupName: 'num',
    ellipsis: true,
    customRender: ({ text }) => text || '--'
  },
  {
    title: t('common.createdDate'),
    dataIndex: 'createdDate',
    width: 160,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: t('common.deadlineDate'),
    dataIndex: 'deadlineDate',
    width: 160,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true,
    customRender: ({ text }) => text || '--'
  },
  {
    title: t('common.startDate'),
    dataIndex: 'startDate',
    width: 160,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true,
    customRender: ({ text }) => text || '--'
  },
  {
    title: t('common.processedDate'),
    dataIndex: 'processedDate',
    width: 160,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true,
    customRender: ({ text }) => text || '--'
  },
  {
    title: t('common.confirmedDate'),
    dataIndex: 'confirmedDate',
    width: 160,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true,
    customRender: ({ text }) => text || '--'
  },
  {
    title: t('common.completedDate'),
    dataIndex: 'completedDate',
    width: 160,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true,
    customRender: ({ text }) => text || '--'
  },
  {
    title: t('common.canceledDate'),
    dataIndex: 'canceledDate',
    width: 160,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true,
    customRender: ({ text }) => text || '--'
  },
  {
    title: t('common.execCompletedDate'),
    dataIndex: 'execCompletedDate',
    width: 160,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true,
    customRender: ({ text }) => text || '--'
  },
  {
    title: t('common.lastModifiedDate'),
    dataIndex: 'lastModifiedDate',
    width: 160,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: t('common.actions'),
    dataIndex: 'action',
    align: 'center',
    width: 160
  }
];

const EXEC_RESULT_COLOR = {
  FAIL: '#F5222D',
  SUCCESS: '#52C41A'
};
</script>
<template>
  <div>
    <div
      :visible="!!props.selectedIds.length"
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
        @click="clearBatchSelection">
        <span>{{ t('actions.cancelBatch') }}</span>
        <span class="ml-1">({{ props.selectedIds.length }})</span>
      </Button>
    </div>

    <Table
      :columns="tableColumns"
      :dataSource="props.dataSource"
      :pagination="props.pagination"
      :rowSelection="rowSelection"
      rowKey="id"
      @change="handleTableChange">
      <template #bodyCell="{ text, record, column }">
        <div v-if="column.dataIndex === 'name'" class="flex items-center">
          <IconTask :value="record.taskType?.value" class="text-4 flex-shrink-0" />
          <RouterLink
            class="link truncate ml-1"
            :title="record.name"
            :to="record.linkUrl">
            {{ record.name }}
          </RouterLink>
          <span
            v-if="record.overdue"
            class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2 mr-2"
            style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
            <span class="inline-block transform-gpu scale-90">{{ t('status.overdue') }}</span>
          </span>
        </div>

        <div v-else-if="column.dataIndex === 'evalWorkload'">
          {{ text || '--' }}
        </div>

        <TaskPriority v-else-if="column.dataIndex === 'priority'" :value="text" />

        <TaskStatus v-else-if="column.dataIndex === 'status'" :value="text" />

        <div v-else-if="column.dataIndex === 'execResult'" :style="'color:' + EXEC_RESULT_COLOR[text?.value]">
          {{ text?.message || '--' }}
        </div>

        <div v-else-if="column.dataIndex === 'action'" class="flex items-center">
          <Button
            :disabled="!!props.menuItemsMap.get(record.id)?.find(item => item.key === 'edit')?.disabled"
            type="text"
            size="small"
            class="flex items-center px-0 mr-1"
            @click="navigateToEdit(record.id)">
            <Icon icon="icon-shuxie" class="mr-1 text-3.5" />
            <span>{{ t('actions.edit') }}</span>
          </Button>

          <Button
            :disabled="!!props.menuItemsMap.get(record.id)?.find(item => item.key === 'delete')?.disabled"
            type="text"
            size="small"
            class="flex items-center px-0 mr-1"
            @click="deleteSingleTask(record)">
            <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
            <span>{{ t('actions.delete') }}</span>
          </Button>

          <Dropdown :menuItems="props.menuItemsMap.get(record.id)" @click="handleDropdownAction($event, record)">
            <Button
              type="text"
              size="small"
              class="flex items-center px-0">
              <Icon icon="icon-gengduo" />
            </Button>
          </Dropdown>
        </div>
      </template>
    </Table>

    <AsyncComponent :visible="moveModalVisible">
      <MoveTaskModal
        v-model:visible="moveModalVisible"
        :taskIds="rowSelection.selectedRowKeys"
        :projectId="props.projectId"
        @ok="handleBatchMoveSuccess" />
    </AsyncComponent>
  </div>
</template>
<style scoped>
.link {
  color: #1890ff;
  cursor: pointer;
}

.type-icon {
  position: absolute;
  top: -2px;
  left: 0;
  font-size: 18px;
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
