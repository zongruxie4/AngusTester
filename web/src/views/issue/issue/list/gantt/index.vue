<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import Gantt from '@xcan-angus/frappe-gantt';
import dayjs from 'dayjs';
import { SearchCriteria } from '@xcan-angus/infra';
import { AsyncComponent, Icon } from '@xcan-angus/vue-ui';
import { issue } from '@/api/tester';
import { DATE_TIME_FORMAT } from '@/utils/constant';
import { TaskType } from '@/enums/enums';
import { IssueMenuKey } from '@/views/issue/menu';
import { TaskDetail } from '@/views/issue/types';

/**
 * <p>Component props interface for Gantt chart task management</p>
 * <p>Defines all required properties for the Gantt chart component including project context, filters, and UI state</p>
 */
type GanttChartProps = {
  projectId: number;
  userInfo: { id: number; fullName: string; };
  appInfo: { id: number; };
  filters: SearchCriteria[];
  notify: string;
  moduleId: number;
  loading: boolean;
};

type DrawerTabType = 'basic' | 'person' | 'date' | 'comment' | 'activity' | 'tasks' | 'cases' | 'attachments' | 'remarks';

type TaskListRequestParams = {
  backlog: false;
  projectId: number;
  pageNo: number;
  pageSize: number;
  moduleId?: number;
  filters?: SearchCriteria[];
};

// COMPONENT SETUP
const { t } = useI18n();

const props = withDefaults(defineProps<GanttChartProps>(), {
  filters: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  moduleId: undefined,
  loading: false
});

/**
 * <p>Component event emissions for parent-child communication</p>
 * <p>Handles loading state updates, task changes, and refresh requests</p>
 */
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'update:loading', value: boolean): void;
  (event: 'change', value: Partial<TaskDetail>): void;
  (event: 'refreshChange'): void;
}>();

// ASYNC COMPONENTS
const BasicInfo = defineAsyncComponent(() => import('@/views/issue/issue/list/kanban/detail/Basic.vue'));
const ScenarioInfo = defineAsyncComponent(() => import('@/views/issue/issue/list/kanban/detail/Scenario.vue'));
const PersonnelInfo = defineAsyncComponent(() => import('@/views/issue/issue/list/kanban/detail/Personnel.vue'));
const DateInfo = defineAsyncComponent(() => import('@/views/issue/issue/list/kanban/detail/Date.vue'));
const Comment = defineAsyncComponent(() => import('@/views/issue/issue/list/kanban/detail/Comment.vue'));
const Activity = defineAsyncComponent(() => import('@/views/issue/issue/list/kanban/detail/Activity.vue'));
const AssocIssues = defineAsyncComponent(() => import('@/views/issue/issue/list/kanban/detail/AssocIssues.vue'));
const AssocCases = defineAsyncComponent(() => import('@/views/issue/issue/list/kanban/detail/AssocCases.vue'));
const AttachmentInfo = defineAsyncComponent(() => import('@/views/issue/issue/list/kanban/detail/Attachment.vue'));
const Remarks = defineAsyncComponent(() => import('@/views/issue/issue/list/kanban/detail/Remark.vue'));
const APIInfo = defineAsyncComponent(() => import('@/views/issue/issue/list/kanban/detail/Apis.vue'));

// Task data and Gantt chart references
const taskList = ref<TaskDetail[]>([]);
const ganttChartRef = ref<HTMLElement>();
const ganttChartInstance = ref<Gantt>();

// Selected task and drawer state
const selectedTaskInfo = ref<TaskDetail>();
const selectedSprintInfo = ref<{ id: string; name: string; }>();
const activeDrawerTab = ref<DrawerTabType>('basic');

/**
 * <p>Gets the ID of the currently selected task</p>
 * <p>Returns the task ID for conditional rendering and data binding</p>
 */
const selectedTaskId = computed(() => {
  return selectedTaskInfo?.value?.id;
});

/**
 * <p>Gets the type of the currently selected task</p>
 * <p>Returns the task type for determining which detail component to render</p>
 */
const selectedTaskType = computed(() => {
  return selectedTaskInfo?.value?.taskType?.value;
});

/**
 * <p>Builds API request parameters for task list fetching</p>
 * <p>Constructs query parameters including project ID, pagination, filters, and module ID</p>
 */
const buildTaskListRequestParams = (): TaskListRequestParams => {
  const params: TaskListRequestParams = {
    backlog: false,
    projectId: props.projectId,
    pageNo: 1,
    pageSize: 500
  };

  if (props.filters?.length) {
    params.filters = props.filters;
  }
  if (props.moduleId) {
    params.moduleId = props.moduleId;
  }
  return params;
};

/**
 * <p>Loads detailed task information by ID</p>
 * <p>Fetches complete task details from the API for a specific task</p>
 */
const fetchTaskDetailsById = async (taskId: number): Promise<Partial<TaskDetail>> => {
  emit('update:loading', true);
  const [error, res] = await issue.getTaskDetail(taskId);
  emit('update:loading', false);

  if (error || !res?.data) {
    return { id: taskId };
  }
  return { ...res.data };
};

/**
 * <p>Resets all component data and closes drawer</p>
 * <p>Clears task list and closes the detail drawer panel</p>
 */
const resetComponentData = () => {
  taskList.value = [];
  closeDetailDrawer();
};

/**
 * <p>Loads all task pages with pagination</p>
 * <p>Fetches all available tasks across multiple pages to ensure complete data set</p>
 */
const loadAllTaskPages = async (baseParams: TaskListRequestParams): Promise<TaskDetail[]> => {
  const [error, res] = await issue.getTaskList(baseParams);

  if (error) {
    throw error;
  }

  const { list, total } = (res?.data || { total: 0, list: [] });
  const firstPageCount = list.length;
  const allTasks: TaskDetail[] = [];
  allTasks.push(...list);

  // Load remaining pages if needed
  if (firstPageCount < +total) {
    const remainingPages = Math.ceil((total - firstPageCount) / baseParams.pageSize);
    for (let i = 0, len = remainingPages; i < len; i++) {
      const pageNo = i + 2;
      const pageParams = { ...baseParams, pageNo };
      const [_error, _res] = await issue.getTaskList(pageParams);

      if (_error) {
        throw _error;
      }

      const { list: pageList } = (_res?.data || { total: 0, list: [] });
      allTasks.push(...pageList);
    }
  }

  return allTasks;
};

/**
 * <p>Transforms task data for Gantt chart format</p>
 * <p>Converts task data to the format required by the Gantt chart library</p>
 */
const transformTasksForGanttChart = (tasks: TaskDetail[]): TaskDetail[] => {
  return tasks.map(task => {
    return {
      ...task,
      start: task.startDate || task.createdDate,
      end: task.completedDate || dayjs().format(DATE_TIME_FORMAT),
      progress: task.progress || task.completedDate ? 100 : (+(task as any).completedRate || 0),
      description: '',
      dependencies: task.parentTaskId ? [task.parentTaskId] : []
    };
  });
};

/**
 * <p>Loads task data and initializes/updates the Gantt chart</p>
 * <p>Fetches all tasks with pagination, transforms data for Gantt chart format, and renders the chart</p>
 */
const loadTaskData = async () => {
  try {
    const requestParams = buildTaskListRequestParams();
    emit('update:loading', true);

    const allTasks = await loadAllTaskPages(requestParams);

    resetComponentData();

    // Transform task data for Gantt chart format
    const transformedTasks = transformTasksForGanttChart(allTasks);
    taskList.value = transformedTasks;

    // Initialize or refresh Gantt chart
    if (!ganttChartInstance.value) {
      initializeGanttChart(transformedTasks, allTasks);
    } else {
      refreshGanttChart(transformedTasks);
    }
  } catch (error) {
    resetComponentData();
  } finally {
    emit('update:loading', false);
  }
};

/**
 * <p>Initializes the Gantt chart with task data</p>
 * <p>Creates a new Gantt chart instance with the provided task data</p>
 */
const initializeGanttChart = (transformedTasks: TaskDetail[], originalTasks: TaskDetail[]) => {
  ganttChartInstance.value = new Gantt(ganttChartRef.value, transformedTasks as any, {
    language: 'zh', // TODO: Add internationalization support
    view_mode: 'Day',
    view_mode_select: true,
    on_click: (ganttTask) => {
      selectedTaskInfo.value = originalTasks.find(task => task.id === ganttTask.id);
    }
  });
};

/**
 * <p>Refreshes the existing Gantt chart with new data</p>
 * <p>Updates the Gantt chart instance with new task data</p>
 */
const refreshGanttChart = (transformedTasks: TaskDetail[]) => {
  ganttChartInstance.value?.refresh(transformedTasks as any);
};

/**
 * <p>Changes the active drawer tab</p>
 * <p>Updates the currently displayed tab in the detail drawer panel</p>
 */
const changeActiveDrawerTab = (tabKey: DrawerTabType) => {
  activeDrawerTab.value = tabKey;
};

/**
 * <p>Closes the detail drawer and clears selected data</p>
 * <p>Resets selected task and sprint information</p>
 */
const closeDetailDrawer = () => {
  selectedTaskInfo.value = undefined;
  selectedSprintInfo.value = undefined;
};

/**
 * <p>Handles task information changes and updates local state</p>
 * <p>Refreshes task details and updates both selected task and task list</p>
 */
const handleTaskInfoChange = async (data: Partial<TaskDetail>) => {
  const taskId = data.id;
  if (!taskId) {
    return;
  }

  const updatedTaskInfo = await fetchTaskDetailsById(taskId);

  // Update selected task if it matches
  if (selectedTaskInfo.value) {
    selectedTaskInfo.value = { ...selectedTaskInfo.value, ...updatedTaskInfo };
  }

  // Update task in the list
  const currentTaskList = taskList.value;
  for (let i = 0, len = currentTaskList.length; i < len; i++) {
    if (currentTaskList[i].id === taskId) {
      currentTaskList[i] = { ...currentTaskList[i], ...updatedTaskInfo } as TaskDetail;
      break;
    }
  }
};

/**
 * <p>Handles loading state changes</p>
 * <p>Forwards loading state updates to parent component</p>
 */
const handleLoadingStateChange = (isLoading: boolean) => {
  emit('update:loading', isLoading);
};

// LIFECYCLE AND WATCHERS
onMounted(() => {
  /**
   * <p>Watches for project ID changes to reload data</p>
   * <p>Triggers data loading when project context changes</p>
   */
  watch(() => props.projectId, (newProjectId) => {
    if (!newProjectId) {
      return;
    }

    loadTaskData();
  }, { immediate: true });

  /**
   * <p>Watches for filter and module changes to reload data</p>
   * <p>Refreshes task data when search criteria or module context changes</p>
   */
  watch([() => props.filters, () => props.moduleId], () => {
    loadTaskData();
  });
});
</script>
<template>
  <div class="flex border-t flex-1 h-150">
    <div ref="ganttChartRef" class="flex-1 min-w-0 h-full overflow-auto"></div>
    <div class="drawer-container flex items-start" :class="{ 'drawer-open': !!selectedTaskId }">
      <div class="flex-shrink-0 h-full w-9 space-y-1 overflow-y-auto scroll-smooth drawer-action-container">
        <div
          :class="{ 'drawer-active-item': activeDrawerTab === 'basic' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.basicInfo')"
          @click="changeActiveDrawerTab('basic')">
          <Icon icon="icon-wendangxinxi" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': activeDrawerTab === 'person' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.personnel')"
          @click="changeActiveDrawerTab('person')">
          <Icon icon="icon-quanburenyuan" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': activeDrawerTab === 'date' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.date')"
          @click="changeActiveDrawerTab('date')">
          <Icon icon="icon-riqi" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': activeDrawerTab === 'tasks' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.assocIssues')"
          @click="changeActiveDrawerTab('tasks')">
          <Icon icon="icon-ceshirenwu" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': activeDrawerTab === 'cases' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.assocCases')"
          @click="changeActiveDrawerTab('cases')">
          <Icon icon="icon-ceshiyongli1" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': activeDrawerTab === 'attachments' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.attachment')"
          @click="changeActiveDrawerTab('attachments')">
          <Icon icon="icon-lianjie1" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': activeDrawerTab === 'remarks' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.remark')"
          @click="changeActiveDrawerTab('remarks')">
          <Icon icon="icon-shuxie" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': activeDrawerTab === 'comment' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.comment')"
          @click="changeActiveDrawerTab('comment')">
          <Icon icon="icon-pinglun" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': activeDrawerTab === 'activity' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.activity')"
          @click="changeActiveDrawerTab('activity')">
          <Icon icon="icon-chakanhuodong" class="text-4" />
        </div>
      </div>

      <div class="w-full h-full flex-1 overflow-hidden">
        <div class="flex items-center justify-between mt-4 pl-5 space-x-2.5">
          <div class="flex-1 flex items-center truncate">
            <RouterLink
              :to="`/issue#${IssueMenuKey.SPRINT}?id=${selectedSprintInfo?.id}`"
              :title="selectedSprintInfo?.name"
              class="truncate"
              style="max-width: 50%;">
              {{ selectedSprintInfo?.name }}
            </RouterLink>
            <div class="mx-1.5">/</div>
            <RouterLink
              :to="`/issue#${IssueMenuKey.ISSUE}?id=${selectedTaskInfo?.id}`"
              class="truncate flex-1"
              :title="selectedTaskInfo?.name">
              {{ selectedTaskInfo?.name }}
            </RouterLink>
          </div>
          <Button
            type="default"
            size="small"
            class="p-0 h-3.5 leading-3.5 border-none"
            @click="closeDetailDrawer">
            <Icon icon="icon-shanchuguanbi" class="text-3.5 cursor-pointer" />
          </Button>
        </div>

        <div style="height: calc(100% - 36px);" class="pt-3.5 overflow-hidden">
          <AsyncComponent :visible="!!selectedTaskId">
            <APIInfo
              v-if="selectedTaskType === TaskType.API_TEST"
              v-show="activeDrawerTab === 'basic'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="selectedTaskInfo"
              @change="handleTaskInfoChange as any"
              @loadingChange="handleLoadingStateChange" />

            <ScenarioInfo
              v-else-if="selectedTaskType === TaskType.SCENARIO_TEST"
              v-show="activeDrawerTab === 'basic'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="selectedTaskInfo"
              @change="handleTaskInfoChange as any"
              @loadingChange="handleLoadingStateChange" />

            <BasicInfo
              v-else
              v-show="activeDrawerTab === 'basic'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="selectedTaskInfo"
              @change="handleTaskInfoChange as any"
              @loadingChange="handleLoadingStateChange" />

            <PersonnelInfo
              v-show="activeDrawerTab === 'person'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="selectedTaskInfo"
              @change="handleTaskInfoChange as any"
              @loadingChange="handleLoadingStateChange" />

            <DateInfo
              v-show="activeDrawerTab === 'date'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="selectedTaskInfo"
              @change="handleTaskInfoChange as any"
              @loadingChange="handleLoadingStateChange" />

            <AssocIssues
              v-show="activeDrawerTab === 'tasks'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="selectedTaskInfo"
              @change="handleTaskInfoChange as any"
              @loadingChange="handleLoadingStateChange" />

            <AssocCases
              v-show="activeDrawerTab === 'cases'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="selectedTaskInfo"
              @change="handleTaskInfoChange as any"
              @loadingChange="handleLoadingStateChange" />

            <AttachmentInfo
              v-show="activeDrawerTab === 'attachments'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="selectedTaskInfo"
              @change="handleTaskInfoChange as any"
              @loadingChange="handleLoadingStateChange" />

            <Comment
              v-show="activeDrawerTab === 'comment'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="selectedTaskInfo"
              @change="handleTaskInfoChange as any"
              @loadingChange="handleLoadingStateChange" />

            <Activity
              v-show="activeDrawerTab === 'activity'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="selectedTaskInfo"
              @change="handleTaskInfoChange as any"
              @loadingChange="handleLoadingStateChange" />

            <Remarks
              v-show="activeDrawerTab === 'remarks'"
              :id="selectedTaskInfo?.id || ''"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo" />
          </AsyncComponent>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>

.drawer-container {
  width: 0;
  overflow: hidden;
  transition: all 150ms linear 0ms;
  border-left: 1px solid transparent;
  opacity: 0;
}

.drawer-container.drawer-open {
  width: 400px;
  height: 100%;
  border-left: 1px solid var(--border-text-box);
  opacity: 1;
}

.drawer-action-container {
  background-color: rgba(247, 248, 251, 100%);
  color: var(--content-text-sub-content);
}

.action-item:hover,
.action-item.drawer-active-item {
  background-color: rgba(239, 240, 243, 100%);
}

:deep(.ant-input-affix-wrapper-sm) {
  background-color: #fff;
}
</style>
