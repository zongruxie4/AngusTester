<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Popover, TabPane, Tabs } from 'ant-design-vue';
import { Icon, modal, notification, Spin } from '@xcan-angus/vue-ui';
import { appContext, duration, enumUtils, http, toClipboard, utils } from '@xcan-angus/infra';
import { TaskSprintPermission, TaskStatus, TaskType } from '@/enums/enums';
import { debounce } from 'throttle-debounce';
import { cloneDeep } from 'lodash-es';
import { issue } from '@/api/tester';

import { TaskDetail } from '@/views/issue/types';
import { ActionMenuItem } from '@/views/issue/issue/types';

// Types
type TabPaneKey = 'basicInfo' | 'remark' | 'testInfo' | 'comments' | 'activity';

type Props = {
  editTaskData: TaskDetail;
  projectId: number;
  userInfo: { id: number; fullName: string };
  appInfo: { id: number; };
  id: number;
  menuItems: ActionMenuItem[];
  type: 'details' | 'list';
  linkUrl: string;
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  editTaskData: undefined,
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  id: undefined,
  menuItems: () => [],
  type: 'list',
  linkUrl: '',
  notify: undefined
});

// Component Events
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'edit', value: number): void;
  (event: 'move', value: TaskDetail): void;
  (event: 'delete', value: number): void;
  (event: 'dataChange', value: Partial<TaskDetail>): void;
  (event: 'refreshChange'): void;
  (event: 'splitOk'): void;
}>();

// Async Components
const BasicInfo = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/info/index.vue'));
const TestInfo = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/testing/index.vue'));
const Remarks = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/Remark.vue'));
const Activities = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/Activity.vue'));
const Comments = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/Comment.vue'));
const SubIssues = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/SubIssues.vue'));
const SplitIssue = defineAsyncComponent(() => import('@/views/issue/backlog/SplitIssue.vue'));
const AssocCases = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/AssocCases.vue'));
const AssocIssues = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/AssocIssues.vue'));

// Composables
const { t } = useI18n();
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const replaceTabPane = inject<(id: number, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));
const deleteTabPane = inject<(value: number[]) => void>('deleteTabPane');
const windowResizeNotify = inject('windowResizeNotify', ref<string>());

// Computed Properties
const isAdmin = computed(() => appContext.isAdmin());

// DOM References
const domId = utils.uuid('a');
let containerElement: HTMLElement | null;

// Layout State
const isLargePageLayout = ref<boolean>();

// Notification State
const refreshNotificationId = ref<string>();
const remarkNotificationId = ref<string>();
const activityNotificationId = ref<string>();
const commentNotificationId = ref<string>();

// Data State
const isLoading = ref(false);
const currentTaskInfo = ref<TaskDetail>();
const sprintPermissions = ref<TaskSprintPermission[]>([]);

// UI State
const activeTabKey = ref<TabPaneKey>('basicInfo');
const isFullScreen = ref(false);

// Split Task State
const isSplitTaskVisible = ref(false);
const selectedTaskForSplit = ref<TaskDetail>();

/**
 * Toggles full screen mode for the task detail view
 */
const toggleFullScreenMode = () => {
  isFullScreen.value = !isFullScreen.value;
};

/**
 * Navigates to task edit mode
 */
const navigateToEdit = () => {
  emit('edit', props.id);
};

/**
 * Opens the split task dialog
 */
const openSplitTaskDialog = () => {
  selectedTaskForSplit.value = cloneDeep(currentTaskInfo.value);
  isSplitTaskVisible.value = true;
};

/**
 * Cancels the split task operation
 */
const cancelSplitTask = () => {
  selectedTaskForSplit.value = undefined;
};

/**
 * Confirms the split task operation
 */
const confirmSplitTask = async () => {
  selectedTaskForSplit.value = undefined;
  emit('splitOk');
  await loadTaskData();
};

/**
 * Opens the move task dialog
 */
const openMoveTaskDialog = () => {
  if (!currentTaskInfo.value) {
    return;
  }
  emit('move', currentTaskInfo.value);
};

/**
 * Deletes the current task with confirmation
 */
const deleteCurrentTask = () => {
  if (!currentTaskInfo.value) {
    return;
  }

  const { name, id } = currentTaskInfo.value;
  modal.confirm({
    content: t('actions.tips.confirmDelete', { name }),
    async onOk () {
      const [error] = await issue.deleteTask([id]);
      if (error) {
        return;
      }

      emit('refreshChange');
      emit('delete', id);
      notification.success(t('actions.tips.deleteSuccess'));
    }
  });
};

/**
 * Adds the current task to favorites
 */
const addToFavorites = async () => {
  if (!currentTaskInfo.value) {
    return;
  }

  const { id } = currentTaskInfo.value;
  const [error] = await issue.favouriteTask(id);
  if (error) {
    return;
  }

  notification.success(t('actions.tips.favouriteSuccess'));
  updateTaskData({ id: id, favourite: true });
};

/**
 * Removes the current task from favorites
 */
const removeFromFavorites = async () => {
  if (!currentTaskInfo.value) {
    return;
  }

  const { id } = currentTaskInfo.value;
  const [error] = await issue.cancelFavouriteTask(id);
  if (error) {
    return;
  }

  notification.success(t('actions.tips.cancelFavouriteSuccess'));
  updateTaskData({ id: id, favourite: false });
};

/**
 * Follows the current task
 */
const followCurrentTask = async () => {
  if (!currentTaskInfo.value) {
    return;
  }

  const { id } = currentTaskInfo.value;
  const [error] = await issue.followTask(id);
  if (error) {
    return;
  }

  notification.success(t('actions.tips.followSuccess'));
  updateTaskData({ id: id, follow: true });
};

/**
 * Unfollows the current task
 */
const unfollowCurrentTask = async () => {
  if (!currentTaskInfo.value) {
    return;
  }

  const { id } = currentTaskInfo.value;
  const [error] = await issue.cancelFollowTask(id);
  if (error) {
    return;
  }

  notification.success(t('actions.tips.cancelFollowSuccess'));
  updateTaskData({ id: id, follow: false });
};

/**
 * Starts the current task
 */
const startCurrentTask = async () => {
  if (!currentTaskInfo.value) {
    return;
  }

  const { id } = currentTaskInfo.value;
  const [error] = await issue.startTask(id);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('actions.tips.startSuccess'));
  const taskData = await loadTaskData();
  if (taskData) {
    updateTaskData(taskData);
  }
};

/**
 * Marks the current task as processed
 */
const markTaskAsProcessed = async () => {
  if (!currentTaskInfo.value) {
    return;
  }

  const { id } = currentTaskInfo.value;
  const [error] = await issue.processedTask(id);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('actions.tips.processSuccess'));
  const taskData = await loadTaskData();
  if (taskData) {
    updateTaskData(taskData);
  }
};

/**
 * Confirms the task as incomplete
 */
const confirmTaskAsIncomplete = async () => {
  if (!currentTaskInfo.value) {
    return;
  }

  const { id } = currentTaskInfo.value;
  const [error] = await issue.confirmTask(id, 'FAIL');
  if (error) {
    return;
  }

  emit('refreshChange');
  const taskData = await loadTaskData();
  if (taskData) {
    updateTaskData(taskData);
  }
};

/**
 * Confirms the task as completed
 */
const confirmTaskAsCompleted = async () => {
  if (!currentTaskInfo.value) {
    return;
  }

  const { id } = currentTaskInfo.value;
  const [error] = await issue.confirmTask(id, 'SUCCESS');
  if (error) {
    return;
  }

  emit('refreshChange');
  const taskData = await loadTaskData();
  if (taskData) {
    updateTaskData(taskData);
  }
};

/**
 * Reopens the current task
 */
const reopenCurrentTask = async () => {
  if (!currentTaskInfo.value) {
    return;
  }

  const { id } = currentTaskInfo.value;
  const [error] = await issue.reopenTask(id);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('actions.tips.reopenSuccess'));
  const taskData = await loadTaskData();
  if (taskData) {
    updateTaskData(taskData);
  }
};

/**
 * Restarts the current task
 */
const restartCurrentTask = async () => {
  if (!currentTaskInfo.value) {
    return;
  }

  const { id } = currentTaskInfo.value;
  const [error] = await issue.restartTask(id);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('actions.tips.restartSuccess'));
  const taskData = await loadTaskData();
  if (taskData) {
    updateTaskData(taskData);
  }
};

/**
 * Cancels the current task
 */
const cancelCurrentTask = async () => {
  if (!currentTaskInfo.value) {
    return;
  }

  const { id } = currentTaskInfo.value;
  const [error] = await issue.cancelTask(id);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('actions.tips.cancelSuccess'));
  const taskData = await loadTaskData();
  if (taskData) {
    updateTaskData(taskData);
  }
};

/**
 * Copies the task link to clipboard
 */
const copyTaskLinkToClipboard = () => {
  const message = window.location.origin + props.linkUrl;
  toClipboard(message).then(() => {
    notification.success(t('actions.tips.copyLinkSuccess'));
  }).catch(() => {
    notification.error(t('actions.tips.copyLinkFailed'));
  });
};

/**
 * Handles basic info changes and updates task data
 * <p>
 * Merges the new data with existing task info and emits change events
 */
const handleBasicInfoChange = (data: Partial<TaskDetail>) => {
  if (currentTaskInfo.value) {
    currentTaskInfo.value = { ...currentTaskInfo.value, ...data };
  }

  updateTaskData(data);
  updateTabPaneData(data);
};

/**
 * Updates task data and emits change event
 * <p>
 * Merges new data with current task info and notifies parent components
 */
const updateTaskData = (data: Partial<TaskDetail>) => {
  if (currentTaskInfo.value) {
    currentTaskInfo.value = { ...currentTaskInfo.value, ...data };
  }
  emit('dataChange', data);
};

/**
 * Updates tab pane with new task data
 * <p>
 * Updates the tab pane title and ID when task data changes
 */
const updateTabPaneData = (data: Partial<TaskDetail>) => {
  const { id, name } = data;
  if (id && name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: id });
  }
};

/**
 * Navigates to the previous task in the list
 */
const navigateToPreviousTask = async () => {
  await loadTaskListData(+queryParameterMap.value.pageNo - 1);
};

/**
 * Navigates to the next task in the list
 */
const navigateToNextTask = async () => {
  await loadTaskListData(+queryParameterMap.value.pageNo + 1);
};

/**
 * Loads task list data for navigation
 * <p>
 * Fetches task list data for the specified page number and updates the tab pane
 */
const loadTaskListData = async (pageNo: number) => {
  const params: { [key: string]: any; } = Object.entries(queryParameterMap.value).reduce((prev, cur) => {
    if (!['total'].includes(cur[0])) {
      prev[cur[0]] = cur[1];
    }
    return prev;
  }, {});

  params.pageNo = pageNo;
  const queryStr = http.getURLSearchParams(params, true);
  const [error, res] = await issue.getTaskList(params);
  if (error) {
    return;
  }

  const data = res?.data;
  if (data) {
    const total = data.total;
    const item = data?.list?.[0];
    if (item) {
      const { id, name } = item;
      replaceTabPane(props.id, { _id: id, uiKey: id, name, data: { id, query: queryStr + '&total=' + total } });
    }
  }
};

/**
 * Handles tab key changes and triggers appropriate refresh actions
 * <p>
 * Refreshes specific tab content based on the selected tab
 */
const handleTabKeyChange = (key: string | number) => {
  if (key === 'remark') {
    refreshRemarkTab();
    return;
  }
  if (key === 'activity') {
    refreshActivityTab();
    return;
  }
  if (key === 'comments') {
    refreshCommentTab();
  }
};

/**
 * Refreshes all tab content and data
 * <p>
 * Reloads task data and refreshes the currently active tab
 */
const refreshAllContent = () => {
  loadTaskData();
  refreshNotificationId.value = utils.uuid();
  if (activeTabKey.value === 'remark') {
    refreshRemarkTab();
    return;
  }
  if (activeTabKey.value === 'activity') {
    refreshActivityTab();
    return;
  }
  if (activeTabKey.value === 'comments') {
    refreshCommentTab();
  }
};

/**
 * Triggers remark tab refresh
 */
const refreshRemarkTab = () => {
  remarkNotificationId.value = refreshNotificationId.value;
};

/**
 * Triggers activity tab refresh
 */
const refreshActivityTab = () => {
  activityNotificationId.value = refreshNotificationId.value;
};

/**
 * Triggers comment tab refresh
 */
const refreshCommentTab = () => {
  commentNotificationId.value = refreshNotificationId.value;
};

/**
 * Loads task detail data from the API
 * <p>
 * Fetches task details and handles error cases including resource not found
 * <p>
 * Returns partial task info for error cases
 */
const loadTaskData = async (): Promise<Partial<TaskDetail>> => {
  const id = props.id;
  isLoading.value = true;
  const [error, res] = await issue.getTaskDetail(id);
  isLoading.value = false;
  if (error) {
    if ((error as any)?.ext?.eKey === 'resource_not_found') {
      if (typeof deleteTabPane === 'function') {
        deleteTabPane([id]);
      }
      return { id };
    }
    return { id };
  }

  if (!res?.data) {
    return { id };
  }
  const data = (res?.data || { id }) as TaskDetail;
  currentTaskInfo.value = {
    ...data
  };

  updateTabPaneData(data);
  return data;
};

/**
 * Loads sprint permissions for the current user
 * <p>
 * Fetches user-specific permissions or uses admin permissions
 */
const loadSprintPermissions = async (id: number | undefined) => {
  if (!isAdmin.value && id) {
    const params = {
      admin: true
    };
    const [error, res] = await issue.getUserSprintAuth(id, props.userInfo?.id, params);
    if (error) {
      return;
    }
    sprintPermissions.value = (res?.data || []).map(item => item.value);
  } else {
    sprintPermissions.value = enumUtils.getEnumValues(TaskSprintPermission);
  }
};

/**
 * Refreshes task data and permissions
 * <p>
 * Reloads task data and updates permissions if needed
 */
const refreshTaskData = async () => {
  const data = await loadTaskData();
  if (!props.menuItems?.length) {
    await loadSprintPermissions(data.sprintId);
  }
};

/**
 * Handles window resize events to determine layout mode
 * <p>
 * Debounced resize handler that updates layout based on container width
 */
const handleWindowResize = debounce(duration.resize, () => {
  if (!containerElement) {
    containerElement = document.getElementById(domId);
  }

  if (!containerElement) {
    return;
  }
  isLargePageLayout.value = containerElement.offsetWidth >= 960;
});

// Lifecycle Hooks
onMounted(() => {
  handleWindowResize();

  /**
   * Watches for task ID changes and loads corresponding data
   * <p>
   * Resets active tab and loads task data when ID changes
   */
  watch(() => props.id, async (newTaskId) => {
    if (!newTaskId) {
      return;
    }

    activeTabKey.value = 'basicInfo';

    const data = await loadTaskData();
    if (!props.menuItems?.length) {
      await loadSprintPermissions(data.sprintId);
    }
  }, { immediate: true });

  /**
   * Watches for edit task data changes
   * <p>
   * Updates current task info when edit data is provided
   */
  watch(() => props.editTaskData, async (newEditData) => {
    if (!newEditData || props.id !== newEditData.id) {
      return;
    }
    currentTaskInfo.value = newEditData;
  }, { immediate: true });

  /**
   * Watches for window resize notifications
   * <p>
   * Triggers layout recalculation when resize is detected
   */
  watch(() => windowResizeNotify.value, (newResizeNotify) => {
    if (newResizeNotify === undefined || newResizeNotify === null || newResizeNotify === '') {
      return;
    }
    handleWindowResize();
  }, { immediate: true });

  /**
   * Watches for general notifications
   * <p>
   * Refreshes task data when notification is received
   */
  watch(() => props.notify, async (newNotify) => {
    if (newNotify === undefined || newNotify === null || newNotify === '') {
      return;
    }

    const data = await loadTaskData();
    if (!props.menuItems?.length) {
      await loadSprintPermissions(data.sprintId);
    }
  }, { immediate: true });
});

/**
 * Parses query parameters from the link URL
 * <p>
 * Extracts and parses URL query parameters for navigation
 */
const queryParameterMap = computed(() => {
  if (!props.linkUrl) {
    return {};
  }

  const queryStr = props.linkUrl.split('?')[1];
  if (queryStr) {
    return queryStr.split('&').reduce((prev, cur) => {
      const [key, value] = cur.split('=');
      prev[key] = value;
      return prev;
    }, {} as { [key: string]: any; });
  }
  return {};
});

/**
 * Determines if the previous button should be disabled
 * <p>
 * Returns true if on the first page or page number is invalid
 */
const isPreviousButtonDisabled = computed(() => {
  const { pageNo } = queryParameterMap.value;
  if (pageNo === undefined || pageNo === null || pageNo === '') {
    return true;
  }
  return +pageNo === 1;
});

/**
 * Determines if the next button should be disabled
 * <p>
 * Returns true if on the last page or page data is invalid
 */
const isNextButtonDisabled = computed(() => {
  const { pageNo, total } = queryParameterMap.value;
  if (pageNo === undefined || pageNo === null || pageNo === '' ||
    total === undefined || total === null || total === ''
  ) {
    return true;
  }
  return +pageNo === +total;
});

/**
 * Determines if test info tab should be shown
 * <p>
 * Returns true for API test and scenario test task types
 */
const shouldShowTestInfo = computed(() => {
  const taskType = currentTaskInfo.value?.taskType?.value;
  if (!taskType) {
    return false;
  }
  return [TaskType.API_TEST, TaskType.SCENARIO_TEST].includes(taskType);
});

/**
 * Generates menu items map based on task status and user permissions
 * <p>
 * Creates a map of available action menu items with proper permissions and visibility
 */
const actionMenuItemsMap = computed(() => {
  const menuMap: { [key: string]: ActionMenuItem } = {};
  const taskData = currentTaskInfo.value;
  if (taskData) {
    const status = taskData.status?.value;
    const { currentAssociateType, confirmerId, assigneeId, favourite, follow, sprintAuth } = taskData;

    const userId = props.userInfo?.id;
    const isAdmin = !!(currentAssociateType?.map(item => item.value).includes('SYS_ADMIN') ||
                               currentAssociateType?.map(item => item.value).includes('APP_ADMIN'));
    const isConfirmer = confirmerId === userId;
    const isAssignee = assigneeId === userId;

    let userPermissions: any[] = props.menuItems || [];
    if (sprintPermissions.value?.length) {
      userPermissions = sprintPermissions.value as any[];
    }

    const menuItems: ActionMenuItem[] = [
      {
        name: t('actions.edit'),
        key: 'edit',
        icon: 'icon-shuxie',
        disabled: !isAdmin && !userPermissions.includes(TaskSprintPermission.MODIFY_TASK as any) && sprintAuth,
        hide: true
      },
      {
        name: t('actions.delete'),
        key: 'delete',
        icon: 'icon-qingchu',
        disabled: !isAdmin && !userPermissions.includes(TaskSprintPermission.DELETE_TASK as any) && sprintAuth,
        hide: true
      },
      {
        name: t('actions.split'),
        key: 'split' as any,
        icon: 'icon-guanlianziyuan',
        disabled: !isAdmin && !userPermissions.includes(TaskSprintPermission.MODIFY_TASK as any) && sprintAuth,
        hide: true
      }
    ];

    // Add status-specific menu items
    if (status === TaskStatus.PENDING) {
      menuItems.push({
        name: t('actions.start'),
        key: 'start',
        icon: 'icon-kaishi',
        disabled: !isAdmin && !isAssignee,
        hide: false
      });
    }

    if (status === TaskStatus.IN_PROGRESS) {
      menuItems.push({
        name: t('actions.complete'),
        key: 'processed',
        icon: 'icon-yichuli',
        disabled: !isAdmin && !isAssignee,
        hide: false
      });
    }

    if (status === TaskStatus.CONFIRMING) {
      menuItems.push({
        name: t('issue.actions.confirmComplete'),
        key: 'completed',
        icon: 'icon-yiwancheng',
        disabled: !isAdmin && !isConfirmer,
        hide: false
      });

      menuItems.push({
        name: t('issue.actions.confirmIncomplete'),
        key: 'uncompleted',
        icon: 'icon-shibaiyuanyin',
        disabled: !isAdmin && !isConfirmer,
        hide: false
      });
    }

    if (status === TaskStatus.CANCELED || status === TaskStatus.COMPLETED) {
      menuItems.push({
        name: t('actions.reopen'),
        key: 'reopen',
        icon: 'icon-zhongxindakaiceshirenwu',
        disabled: !isAdmin && !userPermissions.includes(TaskSprintPermission.REOPEN_TASK as any) && !isAssignee,
        hide: false,
        tip: t('issue.messages.reopenTip')
      });

      menuItems.push({
        name: t('actions.restart'),
        key: 'restart',
        icon: 'icon-zhongxinkaishiceshi',
        disabled: !isAdmin && !userPermissions.includes(TaskSprintPermission.MODIFY_TASK as any),
        hide: false,
        tip: t('issue.messages.restartTip')
      });
    }

    if (status !== TaskStatus.CANCELED && status !== TaskStatus.COMPLETED) {
      menuItems.push({
        name: t('actions.cancel'),
        key: 'cancel',
        icon: 'icon-zhongzhi2',
        disabled: !isAdmin && !userPermissions.includes(TaskSprintPermission.MODIFY_TASK as any) && sprintAuth,
        hide: false
      });
    }

    // Add favorite/follow menu items
    if (favourite) {
      menuItems.push({
        name: t('actions.cancelFavourite'),
        key: 'cancelFavourite',
        icon: 'icon-quxiaoshoucang',
        disabled: false,
        hide: false
      });
    } else {
      menuItems.push({
        name: t('actions.addFavourite'),
        key: 'addFavourite',
        icon: 'icon-yishoucang',
        disabled: false,
        hide: false
      });
    }

    if (follow) {
      menuItems.push({
        name: t('actions.cancelFollow'),
        key: 'cancelFollow',
        icon: 'icon-quxiaoguanzhu',
        disabled: false,
        hide: false
      });
    } else {
      menuItems.push({
        name: t('actions.addFollow'),
        key: 'addFollow',
        icon: 'icon-yiguanzhu',
        disabled: false,
        hide: false
      });
    }

    // Add common menu items
    menuItems.push({
      name: t('actions.move'),
      key: 'move',
      icon: 'icon-yidong',
      disabled: !isAdmin && !userPermissions.includes(TaskSprintPermission.MODIFY_TASK) && sprintAuth,
      hide: false
    });

    menuItems.push({
      name: t('actions.copyLink'),
      key: 'copyLink',
      icon: 'icon-fuzhi',
      disabled: false,
      hide: false
    });

    // Convert array to map
    for (let i = 0, len = menuItems.length; i < len; i++) {
      const item = menuItems[i];
      menuMap[item.key] = item;
    }
  }
  return menuMap;
});

/**
 * Generates CSS class name based on full screen state
 * <p>
 * Returns 'fixed-full' class when in full screen mode
 */
const containerClassName = computed(() => {
  return isFullScreen.value ? 'fixed-full' : '';
});

/**
 * Gets the count of referenced tasks by type
 * <p>
 * Returns the number of referenced tasks matching the specified type
 */
const getReferencedTaskCount = (type = 'TASK') => {
  return (currentTaskInfo.value?.refTaskInfos || []).filter(item => item.taskType.value === type).length || 0;
};
</script>

<template>
  <!-- Main container with loading spinner -->
  <Spin
    :id="domId"
    :spinning="isLoading"
    :class="containerClassName"
    class="flex-1 h-full pt-3.5 pl-3.5 pb-3 overflow-hidden">
    <!-- Action buttons and navigation -->
    <div class="flex items-start justify-between pr-5">
      <div class="flex items-start flex-wrap space-y-b-2 space-x-r-2.5">
        <!-- Start Task Button -->
        <Button
          v-if="actionMenuItemsMap.start"
          :disabled="!!actionMenuItemsMap.start?.disabled"
          type="primary"
          size="small"
          class="flex items-center"
          @click="startCurrentTask">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-kaishi" />
          <span>{{ t('actions.start') }}</span>
        </Button>

        <!-- Mark as Processed Button -->
        <Button
          v-if="actionMenuItemsMap.processed"
          :disabled="!!actionMenuItemsMap.processed?.disabled"
          type="primary"
          size="small"
          class="flex items-center"
          @click="markTaskAsProcessed">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-kaishi" />
          <span>{{ t('actions.complete') }}</span>
        </Button>

        <!-- Confirm Complete Button -->
        <Button
          v-if="actionMenuItemsMap.completed"
          :disabled="!!actionMenuItemsMap.completed?.disabled"
          type="primary"
          size="small"
          class="flex items-center"
          @click="confirmTaskAsCompleted">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-kaishi" />
          <span>{{ t('issue.actions.confirmComplete') }}</span>
        </Button>

        <!-- Confirm Incomplete Button -->
        <Button
          v-if="actionMenuItemsMap.uncompleted"
          :disabled="!!actionMenuItemsMap.uncompleted?.disabled"
          type="primary"
          size="small"
          class="flex items-center"
          @click="confirmTaskAsIncomplete">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-kaishi" />
          <span>{{ t('issue.actions.confirmIncomplete') }}</span>
        </Button>

        <!-- Reopen Task Button -->
        <Button
          v-if="actionMenuItemsMap.reopen"
          :disabled="!!actionMenuItemsMap.reopen?.disabled"
          type="primary"
          size="small"
          class="flex items-center"
          @click="reopenCurrentTask">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-kaishi" />
          <span>{{ t('actions.reopen') }}</span>
          <Popover placement="bottom">
            <template #content>
              <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                {{ t('issue.messages.reopenTip') }}
              </div>
            </template>
            <Icon icon="icon-tishi1" class="text-tips ml-1" />
          </Popover>
        </Button>

        <!-- Restart Task Button -->
        <Button
          v-if="actionMenuItemsMap.restart"
          :disabled="!!actionMenuItemsMap.restart?.disabled"
          type="primary"
          size="small"
          class="flex items-center"
          @click="restartCurrentTask">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-kaishi" />
          <span>{{ t('actions.restart') }}</span>
          <Popover placement="bottom">
            <template #content>
              <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                {{ t('issue.messages.restartTip') }}
              </div>
            </template>
            <Icon icon="icon-tishi1" class="text-tips ml-1" />
          </Popover>
        </Button>

        <!-- Edit Task Button -->
        <Button
          v-if="actionMenuItemsMap.edit"
          :disabled="!!actionMenuItemsMap.edit?.disabled"
          size="small"
          class="flex items-center"
          @click="navigateToEdit">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-shuxie" />
          <span>{{ t('actions.edit') }}</span>
        </Button>

        <!-- Split Task Button -->
        <Button
          v-if="actionMenuItemsMap.split"
          :disabled="!!actionMenuItemsMap.split?.disabled"
          size="small"
          class="flex items-center"
          @click="openSplitTaskDialog">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-guanlianziyuan" />
          <span>{{ t('actions.split') }}</span>
        </Button>

        <!-- Cancel Task Button -->
        <Button
          v-if="actionMenuItemsMap.cancel"
          :disabled="!!actionMenuItemsMap.cancel?.disabled"
          size="small"
          class="flex items-center"
          @click="cancelCurrentTask">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-zhongzhi2" />
          <span>{{ t('actions.cancel') }}</span>
        </Button>

        <!-- Delete Task Button -->
        <Button
          v-if="actionMenuItemsMap.delete"
          :disabled="!!actionMenuItemsMap.delete?.disabled"
          size="small"
          class="flex items-center"
          @click="deleteCurrentTask">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-qingchu" />
          <span>{{ t('actions.delete') }}</span>
        </Button>

        <!-- Add to Favorites Button -->
        <Button
          v-if="actionMenuItemsMap.favourite"
          :disabled="!!actionMenuItemsMap.favourite?.disabled"
          size="small"
          class="flex items-center"
          @click="addToFavorites">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-yishoucang" />
          <span>{{ t('actions.addFavourite') }}</span>
        </Button>

        <!-- Remove from Favorites Button -->
        <Button
          v-if="actionMenuItemsMap.cancelFavourite"
          :disabled="!!actionMenuItemsMap.cancelFavourite?.disabled"
          size="small"
          class="flex items-center"
          @click="removeFromFavorites">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-quxiaoshoucang" />
          <span>{{ t('actions.cancelFavourite') }}</span>
        </Button>

        <!-- Follow Task Button -->
        <Button
          v-if="actionMenuItemsMap.follow"
          :disabled="!!actionMenuItemsMap.follow?.disabled"
          size="small"
          class="flex items-center"
          @click="followCurrentTask">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-yiguanzhu" />
          <span>{{ t('actions.addFollow') }}</span>
        </Button>

        <!-- Unfollow Task Button -->
        <Button
          v-if="actionMenuItemsMap.cancelFollow"
          :disabled="!!actionMenuItemsMap.cancelFollow?.disabled"
          size="small"
          class="flex items-center"
          @click="unfollowCurrentTask">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-quxiaoguanzhu" />
          <span>{{ t('actions.cancelFollow') }}</span>
        </Button>

        <!-- Move Task Button -->
        <Button
          v-if="actionMenuItemsMap.move"
          :disabled="!!actionMenuItemsMap.move?.disabled"
          size="small"
          class="flex items-center"
          @click="openMoveTaskDialog">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-yidong" />
          <span>{{ t('actions.move') }}</span>
        </Button>

        <!-- Copy Link Button -->
        <Button
          size="small"
          class="flex items-center"
          @click="copyTaskLinkToClipboard">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-fuzhi" />
          <span>{{ t('actions.copyLink') }}</span>
        </Button>

        <!-- Full Screen Toggle Button -->
        <Button
          v-if="props.type === 'list'"
          type="default"
          size="small"
          class="flex items-center"
          @click="toggleFullScreenMode">
          <template v-if="isFullScreen">
            <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-tuichuzuida" />
            <span>{{ t('actions.exitFullScreen') }}</span>
          </template>

          <template v-else>
            <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-zuidahua" />
            <span>{{ t('actions.fullScreen') }}</span>
          </template>
        </Button>
      </div>

      <!-- Navigation buttons for details view -->
      <div v-if="props.type === 'details' && !(isPreviousButtonDisabled && isNextButtonDisabled)" class="flex items-start">
        <Button
          size="small"
          class="flex items-center mr-2.5"
          :disabled="isPreviousButtonDisabled"
          @click="navigateToPreviousTask">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-chakanshangyitiao" />
          <span>{{ isPreviousButtonDisabled ? t('actions.noMore') : t('actions.previousItem') }}</span>
        </Button>

        <Button
          size="small"
          class="flex items-center"
          :disabled="isNextButtonDisabled"
          @click="navigateToNextTask">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-chakanxiayitiao" />
          <span>{{ isNextButtonDisabled ? t('actions.noMore') : t('actions.nextItem') }}</span>
        </Button>
      </div>
    </div>

    <!-- Main tabs container -->
    <Tabs
      v-model:activeKey="activeTabKey"
      size="small"
      style="height: calc(100% - 36px);"
      @change="handleTabKeyChange">
      <template #rightExtra>
        <Button
          type="link"
          size="small"
          class="flex items-center mr-4"
          @click="refreshAllContent">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-shuaxin" />
          <span>{{ t('actions.refresh') }}</span>
        </Button>
      </template>

      <!-- Basic Info Tab -->
      <TabPane key="basicInfo" :tab="t('common.basicInfo')">
        <BasicInfo
          v-if="isLargePageLayout === false || isLargePageLayout === true"
          v-model:loading="isLoading"
          :projectId="props.projectId"
          :userInfo="{ ...props.userInfo, fullName: '' }"
          :appInfo="{ ...props.appInfo }"
          :dataSource="currentTaskInfo"
          :largePageLayout="isLargePageLayout"
          @change="handleBasicInfoChange" />
      </TabPane>

      <!-- Sub Task Tab -->
      <TabPane key="taskChild">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.subIssues') }}</span>
            <span>({{ currentTaskInfo?.subTaskInfos?.length || 0 }})</span>
          </div>
        </template>
        <SubIssues
          v-model:loading="isLoading"
          :projectId="props.projectId"
          :userInfo="{ ...props.userInfo, fullName: '' }"
          :appInfo="{ ...props.appInfo }"
          :taskInfo="currentTaskInfo"
          :tips="t('issue.detail.tabs.subIssueTip')"
          @refreshChange="refreshTaskData" />
      </TabPane>

      <!-- Associated Requirements Tab -->
      <TabPane key="assocRequirements">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.requirement') }}</span>
            <span>({{ getReferencedTaskCount(TaskType.REQUIREMENT) }})</span>
          </div>
        </template>
        <AssocIssues
          :key="TaskType.REQUIREMENT"
          :projectId="props.projectId"
          :userInfo="{ ...props.userInfo, fullName: '' }"
          :appInfo="{ ...props.appInfo }"
          :dataSource="currentTaskInfo?.refTaskInfos || []"
          :taskId="props.id"
          :title="t('common.requirement')"
          :tips="t('issue.detail.tabs.assocRequirementTip')"
          :taskType="TaskType.REQUIREMENT"
          @editSuccess="loadTaskData" />
      </TabPane>

      <!-- Associated Story Tab -->
      <TabPane key="assocStory">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.story') }}</span>
            <span>({{ getReferencedTaskCount(TaskType.STORY) }})</span>
          </div>
        </template>
        <AssocIssues
          :key="TaskType.STORY"
          :projectId="props.projectId"
          :userInfo="{ ...props.userInfo, fullName: '' }"
          :appInfo="{ ...props.appInfo }"
          :dataSource="currentTaskInfo?.refTaskInfos || []"
          :taskId="props.id"
          :title="t('common.story')"
          :tips="t('issue.detail.tabs.assocStoryTip')"
          :taskType="TaskType.STORY"
          @editSuccess="loadTaskData" />
      </TabPane>

      <!-- Associated Task Tab -->
      <TabPane key="assocTasks">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.task') }}</span>
            <span>({{ getReferencedTaskCount(TaskType.TASK) }})</span>
          </div>
        </template>

        <AssocIssues
          :key="TaskType.TASK"
          :projectId="props.projectId"
          :userInfo="{ ...props.userInfo, fullName: '' }"
          :appInfo="{ ...props.appInfo }"
          :dataSource="currentTaskInfo?.refTaskInfos || []"
          :taskId="props.id"
          :title="t('common.task')"
          :tips="t('issue.detail.tabs.assocTaskTip')"
          :taskType="TaskType.TASK"
          @editSuccess="loadTaskData" />
      </TabPane>

      <!-- Associated Bug Tab -->
      <TabPane key="assocBug">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.bug') }}</span>
            <span>({{ getReferencedTaskCount(TaskType.BUG) }})</span>
          </div>
        </template>
        <AssocIssues
          :key="TaskType.BUG"
          :projectId="props.projectId"
          :userInfo="{ ...props.userInfo, fullName: '' }"
          :appInfo="{ ...props.appInfo }"
          :dataSource="currentTaskInfo?.refTaskInfos || []"
          :taskId="props.id"
          :title="t('common.bug')"
          :tips="t('issue.detail.tabs.assocBugTip')"
          :taskType="TaskType.BUG"
          @editSuccess="loadTaskData" />
      </TabPane>

      <!-- Associated Case Tab -->
      <TabPane key="assocCase">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('issue.detail.tabs.assocCaseTitle') }}</span>
            <span>({{ currentTaskInfo?.refCaseInfos?.length || 0 }})</span>
          </div>
        </template>
        <AssocCases
          :projectId="props.projectId"
          :userInfo="{ ...props.userInfo, fullName: '' }"
          :appInfo="{ ...props.appInfo }"
          :dataSource="(currentTaskInfo?.refCaseInfos || []) as any"
          :taskId="props.id"
          :tips="t('issue.detail.tabs.assocCaseTip')"
          @editSuccess="loadTaskData" />
      </TabPane>

      <!-- Associated API Test Tab -->
      <TabPane key="assocApiTest">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('issue.detail.tabs.assocApiTestTitle') }}</span>
            <span>({{ getReferencedTaskCount(TaskType.API_TEST) }})</span>
          </div>
        </template>
        <AssocIssues
          :key="TaskType.API_TEST"
          :projectId="props.projectId"
          :userInfo="{ ...props.userInfo, fullName: '' }"
          :appInfo="{ ...props.appInfo }"
          :dataSource="currentTaskInfo?.refTaskInfos || []"
          :taskId="props.id"
          :title="t('issue.detail.tabs.assocApiTestTitle')"
          :tips="t('issue.detail.tabs.assocApiTestTip')"
          :taskType="TaskType.API_TEST"
          @editSuccess="loadTaskData" />
      </TabPane>

      <!-- Associated Scenario Test Tab -->
      <TabPane key="assocScenarioTest">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('issue.detail.tabs.assocScenarioTestTitle') }}</span>
            <span>({{ getReferencedTaskCount(TaskType.SCENARIO_TEST) }})</span>
          </div>
        </template>
        <AssocIssues
          :key="TaskType.SCENARIO_TEST"
          :projectId="props.projectId"
          :userInfo="{ ...props.userInfo, fullName: '' }"
          :appInfo="{ ...props.appInfo }"
          :dataSource="currentTaskInfo?.refTaskInfos || []"
          :taskId="props.id"
          :title="t('issue.detail.tabs.assocScenarioTestTitle')"
          :tips="t('issue.detail.tabs.assocScenarioTestTip')"
          :taskType="TaskType.SCENARIO_TEST"
          @editSuccess="loadTaskData" />
      </TabPane>

      <!-- Test Info Tab (conditional) -->
      <TabPane
        v-if="shouldShowTestInfo"
        key="testInfo"
        :tab="t('issue.detail.tabs.testInfoTitle')">
        <TestInfo
          :projectId="props.projectId"
          :userInfo="{ ...props.userInfo }"
          :appInfo="{ ...props.appInfo }"
          :dataSource="currentTaskInfo"
          :largePageLayout="isLargePageLayout" />
      </TabPane>

      <!-- Remark Tab -->
      <TabPane key="remark">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.remark') }}</span>
            <span>({{ (currentTaskInfo as any)?.remarkNum || 0 }})</span>
          </div>
        </template>
        <Remarks
          :id="props.id"
          v-model:notify="remarkNotificationId"
          :projectId="props.projectId"
          :userInfo="{ ...props.userInfo, fullName: '' }"
          :appInfo="{ ...props.appInfo }" />
      </TabPane>

      <!-- Comments Tab -->
      <TabPane key="comments">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.comment') }}</span>
            <span>({{ (currentTaskInfo as any)?.commentNum || 0 }})</span>
          </div>
        </template>
        <Comments
          :id="props.id"
          :notify="commentNotificationId"
          :projectId="props.projectId"
          :userInfo="{ ...props.userInfo }"
          :appInfo="{ ...props.appInfo }" />
      </TabPane>

      <!-- Activity Tab -->
      <TabPane key="activity">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.activity') }}</span>
            <span>({{ (currentTaskInfo as any)?.activityNum || 0 }})</span>
          </div>
        </template>
        <Activities
          :id="props.id"
          :notify="activityNotificationId"
          :projectId="props.projectId"
          :userInfo="{ ...props.userInfo, fullName: '' }"
          :appInfo="{ ...props.appInfo }" />
      </TabPane>
    </Tabs>

    <!-- Split Task Dialog -->
    <AsyncComponent :visible="isSplitTaskVisible">
      <SplitIssue
        v-model:visible="isSplitTaskVisible"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :taskInfo="selectedTaskForSplit"
        @ok="confirmSplitTask"
        @cancel="cancelSplitTask" />
    </AsyncComponent>
  </Spin>
</template>
<style scoped>
/* Full screen mode styles */
.fixed-full {
  position: fixed;
  inset: 0;
  z-index: 999;
  padding: 20px 0 20px 20px;
  background-color: #fff;
}

/* Tab navigation styles */
:deep(.ant-tabs-nav::before) {
  border: 0;
}

:deep(.ant-tabs-top>.ant-tabs-nav::before) {
  right: 20px;
}

/* Spacing utilities for button layout */
.space-x-r-2\.5>*+* {
  margin-right: 10px;
}

.space-y-b-2>*+* {
  margin-bottom: 10px;
}

.space-x-r-2\.5>*:first-child {
  margin-right: 10px;
}

.space-y-b-2>*:first-child {
  margin-bottom: 10px;
}
</style>
