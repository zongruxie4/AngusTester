<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { AsyncComponent } from '@xcan-angus/vue-ui';
import { useRoute, useRouter } from 'vue-router';
import { TaskDetail } from '@/views/issue/types';
import { IssueMenuKey } from '@/views/issue/menu';

// ASYNC COMPONENTS
const TaskDetailPage = defineAsyncComponent(() => import('@/views/issue/issue/list/flat/detail/index.vue'));
const EditTaskModal = defineAsyncComponent(() => import('@/views/issue/issue/list/Edit.vue'));
const MoveTaskModal = defineAsyncComponent(() => import('@/views/issue/issue/list/Move.vue'));

/**
 * Component props interface for task details page
 */
type Props = {
  sprintId: string;
  sprintName: string;
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  uiKey: string; // Tab pane UI key
  activeKey: string;
  data: { id: string; query: string; };
}

const props = withDefaults(defineProps<Props>(), {
  sprintId: undefined,
  sprintName: undefined,
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  uiKey: undefined, // Tab pane UI key
  activeKey: undefined,
  data: undefined
});

// COMPOSABLES & INJECTIONS
const route = useRoute();
const router = useRouter();

const getTabPane = inject<(key: string) => ({ data: { query: string; } }[])>('getTabPane', () => []);
const deleteTabPane = inject<(value: string[]) => void>('deleteTabPane');

// REACTIVE STATE
const taskLinkUrl = ref(route.fullPath);
const refreshNotification = ref<string>();

const selectedTaskName = ref<string>();
const selectedTaskSprintId = ref<string>();
const selectedTaskId = ref<string>();
const currentEditTaskData = ref<TaskDetail>();
const isTaskModalVisible = ref(false);
const isMoveModalVisible = ref(false);

/**
 * Gets the current task ID from props data
 * @returns Task ID string or undefined
 */
const currentTaskId = computed(() => {
  return props.data?.id;
});

/**
 * Initializes the task details page by setting up URL and tab data
 */
const initializeTaskDetail = () => {
  router.replace(`/issue#${IssueMenuKey.ISSUE}`);

  const taskId = props.data?.id;
  const tabPaneDataList = getTabPane(taskId);
  if (tabPaneDataList?.length) {
    const tabPaneItem = tabPaneDataList[0];
    const queryString = tabPaneItem.data?.query;
    if (queryString) {
      taskLinkUrl.value = route.path + '?' + queryString;
    }
  }
};

/**
 * Handles task deletion by closing the tab pane
 * @param taskId - ID of the task to delete
 */
const handleTaskDeletion = (taskId: string) => {
  if (typeof deleteTabPane === 'function') {
    deleteTabPane([taskId]);
  }
};

/**
 * Handles task move operation by opening move modal
 * @param taskData - Task data to move
 */
const handleTaskMove = (taskData: TaskDetail) => {
  selectedTaskSprintId.value = taskData.sprintId;
  selectedTaskName.value = taskData.name;
  selectedTaskId.value = taskData.id;
  isMoveModalVisible.value = true;
};

/**
 * Handles task edit operation by opening edit modal
 */
const handleTaskEdit = () => {
  isTaskModalVisible.value = true;
};

/**
 * Handles task edit completion by updating task data
 * @param updatedTaskData - Updated task data from edit modal
 */
const handleTaskEditComplete = (updatedTaskData: Partial<TaskDetail>) => {
  currentEditTaskData.value = updatedTaskData as TaskDetail;
};

/**
 * Component mounted lifecycle hook
 * Initializes task details and sets up notification watcher
 */
onMounted(() => {
  initializeTaskDetail();

  watch(() => props.notify, (newNotificationValue) => {
    if ((newNotificationValue === undefined || newNotificationValue === null || newNotificationValue === '') ||
      (props.activeKey && props.uiKey !== props.activeKey)) {
      return;
    }

    refreshNotification.value = newNotificationValue;
  }, { immediate: true });
});
</script>

<template>
  <TaskDetailPage
    v-bind="props.data"
    :notify="refreshNotification"
    :userInfo="props.userInfo"
    :appInfo="props.appInfo"
    :projectId="props.projectId"
    :sprintId="props.sprintId"
    :sprintName="props.sprintName"
    :editTaskData="currentEditTaskData"
    :linkUrl="taskLinkUrl"
    type="details"
    @edit="handleTaskEdit"
    @move="handleTaskMove"
    @delete="handleTaskDeletion" />

  <AsyncComponent :visible="isTaskModalVisible">
    <EditTaskModal
      v-model:visible="isTaskModalVisible"
      :taskId="currentTaskId"
      :sprintId="props.sprintId"
      :projectId="props.projectId"
      :userInfo="props.userInfo"
      :appInfo="props.appInfo"
      @ok="handleTaskEditComplete" />
  </AsyncComponent>

  <AsyncComponent :visible="isMoveModalVisible">
    <MoveTaskModal
      v-model:visible="isMoveModalVisible"
      :sprintId="selectedTaskSprintId"
      :taskId="selectedTaskId"
      :taskName="selectedTaskName"
      :projectId="props.projectId" />
  </AsyncComponent>
</template>
