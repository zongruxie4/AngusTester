<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { AsyncComponent, NoData, notification, Spin } from '@xcan-angus/vue-ui';
import { appContext, download, enumUtils, http, PageQuery, SearchCriteria, utils, routerUtils } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { issue } from '@/api/tester';
import { TaskSprintPermission, TaskStatus } from '@/enums/enums';
import { getCurrentPage } from '@/utils/utils';
import { TaskDetail } from '../../types';
import { ActionMenuItem, TaskViewMode } from '../types';
import { IssueMenuKey } from '@/views/issue/menu';

// eslint-disable-next-line import/no-absolute-path
import Template from '/file/Import_Task_Template.xlsx?url';

// Component props interface for task list page
type Props = {
  sprintId: number;
  sprintName: string;
  projectId: number;
  projectName: string;
  userInfo: { id: number; fullName: string; };
  appInfo: { id: number; };
  notify: string;
}

// Component props
const props = withDefaults(defineProps<Props>(), {
  sprintId: undefined,
  sprintName: undefined,
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

// Async components
const StatisticsPanel = defineAsyncComponent(() => import('@/views/issue/issue/list/statistics/index.vue'));
const SearchPanel = defineAsyncComponent(() => import('@/views/issue/issue/list/SearchPanel.vue'));
const Edit = defineAsyncComponent(() => import('@/views/issue/issue/list/Edit.vue'));
const Move = defineAsyncComponent(() => import('@/views/issue/issue/list/Move.vue'));
const Upload = defineAsyncComponent(() => import('@/views/issue/issue/list/Upload.vue'));
const FlowChart = defineAsyncComponent(() => import('@/views/issue/issue/list/FlowChart.vue'));

const ModuleTree = defineAsyncComponent(() => import('@/components/module/ModuleTree.vue'));
const KanbanView = defineAsyncComponent(() => import('@/views/issue/issue/list/kanban/index.vue'));
const GanttView = defineAsyncComponent(() => import('@/views/issue/issue/list/gantt/index.vue'));
const TableView = defineAsyncComponent(() => import('@/views/issue/issue/list/table/index.vue'));
const FlatView = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/index.vue'));

// Composables & injections
const { t } = useI18n();
const router = useRouter();

const deleteTabPane = inject<(value: number[]) => void>('deleteTabPane');
const isAdmin = computed(() => appContext.isAdmin());
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));

// Reactive state
const statisticsRefreshNotify = ref<string>(); // Statistics panel refresh notification
const listRefreshNotify = ref<string>(); // Task list refresh notification

const kanbanGroupKey = ref<'none' | 'assigneeName' | 'lastModifiedByName' | 'taskType'>('none');
const kanbanOrderBy = ref<'priority' | 'deadlineDate' | 'createdByName' | 'assigneeName'>();
const kanbanOrderSort = ref<PageQuery.OrderSort>();

const isStatisticsCollapsed = ref(true); // Initially collapsed, will auto-expand after initialization if not manually collapsed
const currentViewMode = ref<TaskViewMode>(TaskViewMode.flat);
const isModuleGroupEnabled = ref(false);
const isFlowChartVisible = ref(false);
const isExporting = ref(false);

const isDataLoaded = ref(false);
const isLoading = ref(false);
const currentOrderBy = ref<string>();
const currentOrderSort = ref<PageQuery.OrderSort>();
const searchFilters = ref<SearchCriteria[]>([]);
const paginationInfo = ref<{ current: number; pageSize: number; total: number; }>({ current: 1, pageSize: 10, total: 0 });
const taskListData = ref<TaskDetail[]>([]);
const sprintPermissionsCache = ref<Map<number, TaskSprintPermission[]>>(new Map());

const selectedTaskName = ref<string>();
const selectedTaskSprintId = ref<number>();
const selectedTaskId = ref<number>();
const isTaskModalVisible = ref(false);
const isMoveModalVisible = ref(false);
const isUploadModalVisible = ref(false);

const searchSprintId = ref<number>();
const currentEditTaskData = ref<TaskDetail>(); // Task data for edit modal

const selectedTaskIds = ref<number[]>([]); // Batch selected task IDs

const previousSearchParams = ref<{[key:string]:any}>();

/**
 * Builds API parameters for task list requests
 * @returns Formatted parameters for API calls
 */
const buildApiParameters = () => {
  const params: {
    backlog?: boolean,
    projectId: number;
    pageNo: number;
    pageSize: number;
    filters?: SearchCriteria[];
    orderBy?: string;
    orderSort?: PageQuery.OrderSort;
  } = {
    backlog: proTypeShowMap.value.showBackLog ? false : undefined,
    projectId: props.projectId,
    pageNo: paginationInfo.value.current,
    pageSize: paginationInfo.value.pageSize
  };

  if (searchFilters.value.length) {
    params.filters = searchFilters.value;
  }

  if (currentOrderSort.value) {
    params.orderBy = currentOrderBy.value;
    params.orderSort = currentOrderSort.value;
  }
  return params;
};

/**
 * Loads task list data from API
 */
const loadTaskListData = async () => {
  if (!currentViewMode.value || ![TaskViewMode.flat, TaskViewMode.table].includes(currentViewMode.value)) {
    return;
  }

  isLoading.value = false;
  const params = buildApiParameters();
  const [error, response] = await issue.getTaskList({ ...params, moduleId: currentModuleId.value });
  isLoading.value = false;
  isDataLoaded.value = true;
  if (error) {
    return false;
  }

  const data = (response?.data || { total: '0', list: [] }) as { total: string; list: TaskDetail[] };
  const totalCount = +data.total;
  paginationInfo.value.total = totalCount;

  const pageNumber = +params.pageNo;
  const pageSize = +params.pageSize;
  const taskListItems = (data.list || []);
  const processedTaskList: TaskDetail[] = [];
  const sprintIdSet = new Set<number>();

  for (let i = 0, len = taskListItems.length; i < len; i++) {
    const taskItem = taskListItems[i];
    const taskParams = {
      ...params,
      taskId: taskItem.id,
      pageNo: (pageNumber - 1) * pageSize + i + 1,
      pageSize: 1,
      total: totalCount
    };

    processedTaskList.push({
      ...taskItem,
      linkUrl: `/issue#${IssueMenuKey.ISSUE}?` + http.getURLSearchParams(taskParams, true)
    });

    sprintIdSet.add(taskItem.sprintId);
  }

  taskListData.value = processedTaskList;

  // Reset selected tasks if search conditions changed (excluding page number)
  if (previousSearchParams.value) {
    const { pageNo: _prevPageNo, ...prevRest } = previousSearchParams.value;
    const { pageNo: _currPageNo, ...currRest } = params;
    if (!isEqual(prevRest, currRest)) {
      selectedTaskIds.value = [];
    }
  }

  previousSearchParams.value = params;

  // Load sprint permissions
  sprintPermissionsCache.value.clear();
  const sprintIds = Array.from(sprintIdSet);
  // Admin users have all permissions, no need to load permissions
  if (!isAdmin.value && proTypeShowMap.value.showSprint) {
    for (let i = 0, len = sprintIds.length; i < len; i++) {
      const sprintId = sprintIds[i];
      if (sprintId) {
        const [permissionError, permissionResponse] = await loadSprintPermissions(sprintId);
        if (!permissionError) {
          const permissions = (permissionResponse?.data || []).map(item => item.value);
          sprintPermissionsCache.value.set(sprintId, permissions);
        }
      }
    }
  } else {
    for (let i = 0, len = sprintIds.length; i < len; i++) {
      const sprintId = sprintIds[i];
      sprintPermissionsCache.value.set(sprintId, enumUtils.getEnumValues(TaskSprintPermission));
    }
  }
};

/**
 * Loads sprint permissions for a specific sprint
 * @param sprintId - Sprint ID to load permissions for
 * @returns Promise with permission data
 */
const loadSprintPermissions = async (sprintId: number) => {
  const params = {
    admin: true
  };
  return await issue.getUserSprintAuth(sprintId, props.userInfo?.id, params);
};

/**
 * Handles search panel filter changes
 * @param filterData - New filter data from search panel
 */
const handleSearchPanelChange = (filterData: SearchCriteria[]) => {
  searchFilters.value = filterData;
  paginationInfo.value.current = 1;
  loadTaskListData();
};

/**
 * Handles table sorting and pagination changes
 * @param paginationData - Pagination data
 * @param sorter - Sorting configuration
 */
const handleTableChange = ({ current, pageSize }: { current: number; pageSize: number; },
  sorter: { orderBy: string; orderSort: PageQuery.OrderSort }) => {
  paginationInfo.value.current = current;
  paginationInfo.value.pageSize = pageSize;

  currentOrderBy.value = sorter.orderBy;
  currentOrderSort.value = sorter.orderSort;

  loadTaskListData();
};

/**
 * Handles pagination changes
 * @param paginationData - New pagination data
 */
const handlePaginationChange = (paginationData: { current: number; pageSize: number; }) => {
  paginationInfo.value.current = paginationData.current;
  paginationInfo.value.pageSize = paginationData.pageSize;
  loadTaskListData();
};

/**
 * Handles task creation by opening create modal
 */
const handleTaskCreation = () => {
  // Set current sprint from search filters
  const sprintFilter = searchFilters.value.find(filter => filter.key === 'sprintId');
  searchSprintId.value = sprintFilter?.value;
  isTaskModalVisible.value = true;
};

/**
 * Opens upload task modal
 */
const openUploadTaskModal = () => {
  isUploadModalVisible.value = true;
};

/**
 * Handles upload task modal cancellation
 */
const handleUploadTaskCancel = () => {
  isUploadModalVisible.value = false;
};

/**
 * Handles upload task completion
 */
const handleUploadTaskComplete = () => {
  loadTaskListData();
};

/**
 * Handles template export for task import
 */
const handleImportTemplate = async () => {
  const downloadLink = document.createElement('a');
  downloadLink.style.display = 'none';
  downloadLink.href = Template;
  downloadLink.download = 'Import_Task_Template.xlsx';
  document.body.appendChild(downloadLink);
  downloadLink.click();
  document.body.removeChild(downloadLink);
};

/**
 * Handles task list export
 */
const handleIssueExport = async () => {
  if (isExporting.value) {
    notification.info(t('actions.tips.exportingInProgress'));
    return;
  }

  isExporting.value = true;
  const exportUrl = routerUtils.getTesterApiUrl('/task/export') +
    '?' + http.getURLSearchParams({ projectId: props.projectId, filters: searchFilters.value }, true);
  await download(exportUrl);
  isExporting.value = false;
};

/**
 * Handles refresh notifications
 */
const handleRefreshChange = () => {
  statisticsRefreshNotify.value = utils.uuid();
};

/**
 * Handles task split completion
 */
const handleTaskSplitComplete = () => {
  statisticsRefreshNotify.value = utils.uuid();
  loadTaskListData();
};

/**
 * Handles view mode changes
 * @param newViewMode - New view mode to switch to
 */
const handleViewModeChange = (newViewMode: TaskViewMode) => {
  if ([TaskViewMode.kanban, TaskViewMode.gantt].includes(currentViewMode.value) &&
    [TaskViewMode.table, TaskViewMode.flat].includes(newViewMode)) {
    currentViewMode.value = newViewMode;
    paginationInfo.value.current = 1;
    loadTaskListData();
  }

  currentViewMode.value = newViewMode;
};

/**
 * Handles task edit by opening edit modal
 * @param taskId - ID of task to edit
 */
const handleTaskEdit = (taskId: number) => {
  selectedTaskId.value = taskId;
  handleTaskCreation();
};

/**
 * Handles task edit completion
 * @param taskData - Updated task data
 * @param isNewTask - Whether this is a new task being added
 */
const handleTaskEditComplete = (taskData: TaskDetail, isNewTask = false) => {
  if (isNewTask) {
    loadTaskListData();
    return;
  }

  if (taskData) {
    updateTaskListData(taskData);
  }
};

/**
 * Handles task deletion
 * @param taskId - ID of task to delete
 */
const handleTaskDeletion = (taskId: number) => {
  paginationInfo.value.current = getCurrentPage(paginationInfo.value.current, paginationInfo.value.pageSize, paginationInfo.value.total);
  loadTaskListData();
  if (typeof deleteTabPane === 'function') {
    deleteTabPane([taskId]);
  }
};

/**
 * Handles task move by opening move modal
 * @param taskData - Task data to move
 */
const handleTaskMove = (taskData: TaskDetail) => {
  selectedTaskSprintId.value = taskData.sprintId;
  selectedTaskName.value = taskData.name;
  selectedTaskId.value = taskData.id;
  isMoveModalVisible.value = true;
};

/**
 * Handles task move completion
 */
const handleTaskMoveComplete = () => {
  selectedTaskSprintId.value = undefined;
  selectedTaskName.value = undefined;
  selectedTaskId.value = undefined;
  paginationInfo.value.current = getCurrentPage(paginationInfo.value.current, paginationInfo.value.pageSize, paginationInfo.value.total);
  loadTaskListData();
};

/**
 * Handles batch actions on selected tasks
 * @param actionKey - Action to perform
 * @param taskIds - Array of task IDs to perform action on
 */
const handleBatchAction = (actionKey: 'cancel' | 'delete' | 'addFollow' | 'addFavourite' | 'move', taskIds: string[]) => {
  switch (actionKey) {
    case 'cancel':
      handleBatchCancel(taskIds);
      break;
    case 'delete':
      handleBatchDelete(taskIds);
      break;
    case 'addFavourite':
      handleBatchFavourite();
      break;
    case 'move':
      handleBatchMove(taskIds);
      break;
    default:
      break;
  }
};

/**
 * Handles batch task cancellation
 * @param taskIds - Array of task IDs to cancel
 */
const handleBatchCancel = async (taskIds: string[]) => {
  // If no status filter, refresh list directly
  const hasStatusCondition = searchFilters.value.find(filter => filter.key === 'status');
  if (hasStatusCondition) {
    handleBatchActionComplete(taskIds);
    return;
  }

  await loadTaskListData();
};

/**
 * Handles batch task deletion
 * @param taskIds - Array of task IDs to delete
 */
const handleBatchDelete = async (taskIds: string[]) => {
  handleBatchActionComplete(taskIds);
};

/**
 * Handles batch task favourite toggle
 */
const handleBatchFavourite = async () => {
  // Update current list data
  await loadTaskListData();
};

/**
 * Handles batch task move
 * @param taskIds - Array of task IDs to move
 */
const handleBatchMove = (taskIds: string[]) => {
  // If no sprint filter, refresh list directly
  const hasSprintCondition = searchFilters.value.find(filter => filter.key === 'sprintId');
  if (hasSprintCondition) {
    handleBatchActionComplete(taskIds);
    return;
  }

  loadTaskListData();
};

/**
 * Handles batch action completion with pagination adjustment
 * @param taskIds - Array of task IDs that were processed
 */
const handleBatchActionComplete = (taskIds: string[]) => {
  const { current, pageSize, total } = paginationInfo.value;
  const totalPages = Math.ceil(total / pageSize);
  const remainder = total % pageSize;

  const deleteCount = taskIds.length;
  const deletePages = Math.floor(deleteCount / pageSize);
  const deleteRemainder = deleteCount % pageSize;

  if ((deleteRemainder === 0 || remainder === 0) || (deleteRemainder < remainder)) {
    if (current + deletePages <= totalPages) {
      // Notify list refresh
      listRefreshNotify.value = utils.uuid();
      // Notify statistics panel refresh
      statisticsRefreshNotify.value = utils.uuid();
      return;
    }

    paginationInfo.value.current = current - (current + deletePages - totalPages) || 1;
    // Notify list refresh
    listRefreshNotify.value = utils.uuid();
    // Notify statistics panel refresh
    statisticsRefreshNotify.value = utils.uuid();
    return;
  }

  if (deleteRemainder >= remainder) {
    if (current + deletePages + 1 <= totalPages) {
      // Notify list refresh
      listRefreshNotify.value = utils.uuid();
      // Notify statistics panel refresh
      statisticsRefreshNotify.value = utils.uuid();
      return;
    }

    // Notify list refresh
    listRefreshNotify.value = utils.uuid();
    // Notify statistics panel refresh
    statisticsRefreshNotify.value = utils.uuid();
  }
};

/**
 * Updates task data in the list
 * @param taskData - Updated task data
 */
const updateTaskListData = (taskData: Partial<TaskDetail>) => {
  if (taskData) {
    const taskIndex = taskListData.value.findIndex(item => item.id === taskData.id);
    if (taskListData.value[taskIndex]) {
      taskListData.value[taskIndex] = { ...taskListData.value[taskIndex], ...taskData };
      currentEditTaskData.value = taskListData.value[taskIndex];
    }
  }
};

// MODULE MANAGEMENT
const currentModuleId = ref<number>();

/**
 * Component mounted lifecycle hook
 * Initializes module tree and sets up watchers
 */
onMounted(async () => {
  await router.replace(`/issue#${IssueMenuKey.ISSUE}`);

  watch(() => props.projectId, () => {
    currentModuleId.value = undefined;
  });

  watch(() => listRefreshNotify.value, (newNotificationValue) => {
    if (newNotificationValue === undefined || newNotificationValue === null || newNotificationValue === '') {
      return;
    }

    loadTaskListData();
  }, { immediate: true });

  watch(() => isModuleGroupEnabled.value, () => {
    if (isModuleGroupEnabled.value) {
      currentModuleId.value = undefined;
    } else {
      currentModuleId.value = undefined;
    }
  });

  watch(() => currentModuleId.value, () => {
    // Notify list refresh
    listRefreshNotify.value = utils.uuid();
    // Notify statistics panel refresh
    statisticsRefreshNotify.value = utils.uuid();
  });
});

/**
 * Computed property for task action menu items
 * Generates context menu items based on task status and user permissions
 */
const taskActionMenuItems = computed<Map<number, ActionMenuItem[]>>(() => {
  const menuItemsMap = new Map<number, ActionMenuItem[]>();
  const taskList = taskListData.value || [];

  for (let i = 0, len = taskList.length; i < len; i++) {
    const taskItem = taskList[i];
    const taskStatus = taskItem.status?.value;
    const sprintId = taskItem.sprintId;
    const sprintAuth = taskItem.sprintAuth;

    const permissions = sprintPermissionsCache.value.get(sprintId) || [];
    const { currentAssociateType, confirmerId, assigneeId } = taskItem;

    const currentUserId = props.userInfo?.id;
    const isCurrentUserAdmin = !!currentAssociateType?.map(associateType => associateType.value).includes('SYS_ADMIN' || 'APP_ADMIN');
    const isCurrentUserConfirmer = confirmerId === currentUserId;
    const isCurrentUserAssignee = assigneeId === currentUserId;

    const menuItems: ActionMenuItem[] = [
      {
        name: t('actions.edit'),
        key: 'edit',
        icon: 'icon-shuxie',
        disabled: !isCurrentUserAdmin && !permissions.includes(TaskSprintPermission.MODIFY_TASK) && sprintAuth,
        hide: true
      },
      {
        name: t('actions.delete'),
        key: 'delete',
        icon: 'icon-qingchu',
        disabled: !isCurrentUserAdmin && !permissions.includes(TaskSprintPermission.DELETE_TASK) && sprintAuth,
        hide: true
      },
      {
        name: t('actions.split'),
        key: 'split',
        icon: 'icon-guanlianziyuan',
        disabled: !isCurrentUserAdmin && !permissions.includes(TaskSprintPermission.MODIFY_TASK) && sprintAuth,
        hide: true
      }
    ];

    if (taskStatus === TaskStatus.PENDING) {
      menuItems.push({
        name: t('actions.start'),
        key: 'start',
        icon: 'icon-kaishi',
        disabled: !isCurrentUserAdmin && !isCurrentUserAssignee,
        hide: false
      });
    }

    if (taskStatus === TaskStatus.IN_PROGRESS) {
      menuItems.push({
        name: t('actions.complete'),
        key: 'processed',
        icon: 'icon-yichuli',
        disabled: !isCurrentUserAdmin && !isCurrentUserAssignee,
        hide: false
      });
    }

    if (taskStatus === TaskStatus.CONFIRMING) {
      menuItems.push({
        name: t('issue.actions.confirmComplete'),
        key: 'completed',
        icon: 'icon-yiwancheng',
        disabled: !isCurrentUserAdmin && !isCurrentUserConfirmer,
        hide: false
      });

      menuItems.push({
        name: t('issue.actions.confirmIncomplete'),
        key: 'uncompleted',
        icon: 'icon-shibaiyuanyin',
        disabled: !isCurrentUserAdmin && !isCurrentUserConfirmer,
        hide: false
      });
    }

    if (taskStatus === TaskStatus.CANCELED || taskStatus === TaskStatus.COMPLETED) {
      menuItems.push({
        name: t('actions.reopen'),
        key: 'reopen',
        icon: 'icon-zhongxindakaiceshirenwu',
        disabled: !isCurrentUserAdmin && !permissions.includes(TaskSprintPermission.REOPEN_TASK) && !isCurrentUserAssignee,
        hide: false,
        tip: t('issue.messages.reopenTip')
      });

      menuItems.push({
        name: t('actions.restart'),
        key: 'restart',
        icon: 'icon-zhongxinkaishiceshi',
        disabled: !isCurrentUserAdmin && !permissions.includes(TaskSprintPermission.RESTART_TASK),
        hide: false,
        tip: t('issue.messages.restartTip')
      });
    }

    if (taskStatus !== TaskStatus.CANCELED && taskStatus !== TaskStatus.COMPLETED) {
      menuItems.push({
        name: t('actions.cancel'),
        key: 'cancel',
        icon: 'icon-zhongzhi2',
        disabled: !isCurrentUserAdmin && !permissions.includes(TaskSprintPermission.MODIFY_TASK) && sprintAuth,
        hide: false
      });
    }

    const { favourite, follow } = taskItem;
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

    menuItems.push({
      name: t('actions.move'),
      key: 'move',
      icon: 'icon-yidong',
      disabled: !isCurrentUserAdmin && !permissions.includes(TaskSprintPermission.MODIFY_TASK) && sprintAuth,
      hide: false
    });

    menuItems.push({
      name: t('actions.copyLink'),
      key: 'copyLink',
      icon: 'icon-fuzhi',
      disabled: false,
      hide: false
    });
    menuItemsMap.set(taskItem.id, menuItems);
  }
  return menuItemsMap;
});

/**
 * Computed property for statistics panel parameters
 * Builds parameters for statistics API calls
 */
const statisticsParameters = computed(() => {
  const parameters: any = { };
  if (proTypeShowMap.value.showBackLog) {
    parameters.backlog = false;
  }
  if (searchFilters.value.length) {
    parameters.filters = searchFilters.value;
  }
  if (currentModuleId.value) {
    parameters.moduleId = currentModuleId.value;
  }
  return parameters;
});
</script>

<template>
  <Spin :spinning="isLoading" class="flex flex-col pl-3.5 pt-3.5 h-full overflow-y-auto overflow-x-hidden">
    <StatisticsPanel
      :collapse="isStatisticsCollapsed"
      :params="statisticsParameters"
      :notify="statisticsRefreshNotify"
      :userInfo="props.userInfo"
      :appInfo="props.appInfo"
      :projectId="props.projectId"
      :class="{ 'mb-3': !isStatisticsCollapsed }"
      class="pr-5" />

    <div class="flex h-full">
      <div class="flex-shrink-0 h-full overflow-hidden pb-3 bg-gray-1 text-3" :class="{'w-65 mr-2': isModuleGroupEnabled , 'w-0': !isModuleGroupEnabled}">
        <ModuleTree
          v-model:moduleId="currentModuleId"
          :projectId="props.projectId"
          :projectName="props.projectName" />
      </div>
      <div class="flex-1 flex flex-col overflow-x-hidden">
        <SearchPanel
          v-model:collapse="isStatisticsCollapsed"
          v-model:visible="isFlowChartVisible"
          v-model:moduleFlag="isModuleGroupEnabled"
          v-model:groupKey="kanbanGroupKey"
          v-model:orderBy="kanbanOrderBy"
          v-model:orderSort="kanbanOrderSort"
          :viewMode="currentViewMode"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId"
          :sprintId="props.sprintId"
          :sprintName="props.sprintName"
          class="mb-1.5 pr-5"
          @change="handleSearchPanelChange"
          @add="handleTaskCreation"
          @export="handleIssueExport"
          @uploadTask="openUploadTaskModal"
          @exportTemplate="handleImportTemplate"
          @viewModeChange="handleViewModeChange" />

        <KanbanView
          v-if="currentViewMode === TaskViewMode.kanban"
          v-model:loading="isLoading"
          v-model:moduleId="currentModuleId"
          :groupKey="kanbanGroupKey"
          :orderBy="kanbanOrderBy"
          :orderSort="kanbanOrderSort"
          :filters="searchFilters"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          class="pr-5"
          @refreshChange="handleRefreshChange" />

        <GanttView
          v-else-if="currentViewMode === TaskViewMode.gantt"
          v-model:loading="isLoading"
          v-model:moduleId="currentModuleId"
          :filters="searchFilters"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          class="pr-5" />

        <template v-else-if="isDataLoaded">
          <div v-if="!taskListData.length" class="flex-1 pr-5 h-full flex items-center justify-center">
            <NoData />
          </div>

          <TableView
            v-else-if="currentViewMode === TaskViewMode.table"
            v-model:loading="isLoading"
            v-model:selectedIds="selectedTaskIds"
            :projectId="props.projectId"
            :dataSource="taskListData"
            :pagination="paginationInfo"
            :menuItemsMap="taskActionMenuItems"
            class="pr-5"
            @tableChange="handleTableChange"
            @edit="handleTaskEdit"
            @move="handleTaskMove"
            @delete="handleTaskDeletion"
            @dataChange="updateTaskListData"
            @batchAction="handleBatchAction"
            @refreshChange="handleRefreshChange" />

          <FlatView
            v-else-if="currentViewMode === TaskViewMode.flat"
            v-model:loading="isLoading"
            v-model:selectedIds="selectedTaskIds"
            :projectId="props.projectId"
            :userInfo="props.userInfo"
            :appInfo="props.appInfo"
            :dataSource="taskListData"
            :editTaskData="currentEditTaskData"
            :pagination="paginationInfo"
            :menuItemsMap="taskActionMenuItems"
            :loaded="isDataLoaded"
            @edit="handleTaskEdit"
            @move="handleTaskMove"
            @delete="handleTaskDeletion"
            @dataChange="updateTaskListData"
            @paginationChange="handlePaginationChange"
            @batchAction="handleBatchAction"
            @refreshChange="handleRefreshChange"
            @splitOk="handleTaskSplitComplete" />
        </template>
      </div>
    </div>

    <AsyncComponent :visible="isTaskModalVisible">
      <Edit
        v-model:visible="isTaskModalVisible"
        v-model:taskId="selectedTaskId"
        :sprintId="searchSprintId"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :moduleId="currentModuleId === -1 ? undefined : currentModuleId"
        @ok="handleTaskEditComplete" />
    </AsyncComponent>

    <AsyncComponent :visible="isFlowChartVisible">
      <FlowChart v-model:visible="isFlowChartVisible" />
    </AsyncComponent>

    <AsyncComponent :visible="isMoveModalVisible">
      <Move
        v-model:visible="isMoveModalVisible"
        :sprintId="selectedTaskSprintId"
        :taskIds="selectedTaskId ? [selectedTaskId] : selectedTaskIds"
        :taskName="selectedTaskName"
        :projectId="props.projectId"
        @ok="handleTaskMoveComplete" />
    </AsyncComponent>
    <AsyncComponent :visible="isUploadModalVisible">
      <Upload
        v-model:visible="isUploadModalVisible"
        :downloadTemplate="handleImportTemplate"
        @cancel="handleUploadTaskCancel"
        @ok="handleUploadTaskComplete" />
    </AsyncComponent>
  </Spin>
</template>
