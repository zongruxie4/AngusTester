<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Badge, Calendar } from 'ant-design-vue';
import { AsyncComponent, Icon, IconTask, Popover, Select, TaskStatus as TaskStatusV } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { Dayjs } from 'dayjs';
import { analysis } from '@/api/tester';
import { TaskStatus } from '@/enums/enums';

import { DATE_TIME_FORMAT } from '@/utils/constant';
import { TaskInfo } from '@/views/task/task/types';

/**
 * Props interface for WorkCalendar component.
 * <p>
 * Defines the required properties for displaying work calendar with task information.
 * </p>
 */
type Props = {
  projectId: string;
  userInfo: { id: string; };
  notify: string;
  sprintId?: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined,
  sprintId: undefined
});

const { t } = useI18n();

// Component state management
const isDataLoaded = ref(false);
const taskDataByDate = ref<{ [key: string]: TaskInfo[] }>({});
const selectedUserId = ref();

/**
 * Loads work summary data from the API.
 * <p>
 * Fetches task work summary data grouped by day for the selected user and project.
 * </p>
 */
const loadWorkSummaryData = async () => {
  const queryParams = {
    projectId: props.projectId,
    userId: selectedUserId.value,
    sprintId: props.sprintId || undefined
  };
  const [error, response] = await analysis.getTaskWorkSummary(queryParams);
  isDataLoaded.value = true;
  if (error) {
    return;
  }

  taskDataByDate.value = response?.data?.groupByDay;
};

/**
 * Gets non-overdue tasks for a specific date.
 * <p>
 * Filters tasks for the given date that are not overdue.
 * </p>
 */
const getNonOverdueTasks = (current: Dayjs) => {
  const dateString = current.format(DATE_TIME_FORMAT).split(' ')[0];
  if (taskDataByDate.value[dateString]) {
    return taskDataByDate.value[dateString].filter(item => !item.overdue);
  }
  return [];
};

/**
 * Gets overdue tasks for a specific date.
 * <p>
 * Filters tasks for the given date that are overdue.
 * </p>
 */
const getOverdueTasks = (current: Dayjs) => {
  const dateString = current.format(DATE_TIME_FORMAT).split(' ')[0];
  if (taskDataByDate.value[dateString]) {
    return taskDataByDate.value[dateString].filter(item => item.overdue);
  }

  return [];
};

/**
 * Calculates total number of active tasks.
 * <p>
 * Counts tasks that are not canceled.
 * </p>
 */
const calculateTotalTaskCount = (taskList: any[]) => {
  return (taskList || []).filter(item => item.status?.value !== TaskStatus.CANCELED).length;
};

/**
 * Calculates number of completed tasks.
 * <p>
 * Counts tasks with completed status.
 * </p>
 */
const calculateCompletedTaskCount = (taskList: any[]) => {
  return (taskList || []).filter(item => item.status?.value === TaskStatus.COMPLETED).length;
};

/**
 * Calculates number of remaining tasks.
 * <p>
 * Calculates the difference between total and completed tasks.
 * </p>
 */
const calculateRemainingTaskCount = (taskList: any[]) => {
  const totalCount = (taskList || []).filter(item => item.status?.value !== TaskStatus.CANCELED).length;
  const completedCount = (taskList || []).filter(item => item.status?.value === TaskStatus.COMPLETED).length;
  return totalCount - completedCount;
};

onMounted(() => {
  // Watch for project and sprint changes
  watch([() => props.projectId, () => props.sprintId], ([newValue]) => {
    if (!newValue) {
      return;
    }
    if (!selectedUserId.value) {
      selectedUserId.value = props.userInfo?.id;
    }

    loadWorkSummaryData();
  }, { immediate: true });

  // Watch for refresh notifications
  watch(() => props.notify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    loadWorkSummaryData();
  }, { immediate: true });

  // Watch for user selection changes
  watch(() => selectedUserId.value, () => {
    loadWorkSummaryData();
  });
});
</script>

<template>
  <div class="relative">
    <!-- User selection dropdown -->
    <div class="absolute top-1.5 left-0 text-3.5 font-semibold">
      <Select
        v-model:value="selectedUserId"
        class="w-30"
        :action="`${TESTER}/project/${props.projectId}/member/user`"
        :fieldNames="{value: 'id', label: 'fullName'}" />
    </div>

    <!-- Calendar component with async loading -->
    <AsyncComponent :visible="isDataLoaded">
      <Calendar size="small">
        <template #dateCellRender="{ current }: { current: Dayjs }">
          <!-- Non-overdue tasks display -->
          <template v-if="getNonOverdueTasks(current).length">
            <div class="flex items-center text-3">
              <Icon icon="icon-xiaoqi" class="text-status-success text-3.5 mr-1" />
              <Popover
                placement="right"
                overlayClassName="calendar-popover-container">
                <Badge
                  class="text-3"
                  :overflowCount="99"
                  :count="getNonOverdueTasks(current).length" />
                <template #content>
                  <!-- Task statistics summary -->
                  <div class="flex items-center flex-nowrap space-x-5 mb-1.5">
                    <div class="flex-shrink-0 space-x-1">
                      <span>{{ t('taskHome.workloadLabel') }}</span>
                      <span>{{ calculateTotalTaskCount(getNonOverdueTasks(current)) }}</span>
                    </div>
                    <div class="flex-shrink-0 space-x-1">
                      <span>{{ t('taskHome.completed') }}</span>
                      <span>{{ calculateCompletedTaskCount(getNonOverdueTasks(current)) }}</span>
                    </div>
                    <div class="flex-shrink-0 space-x-1">
                      <span>{{ t('taskHome.remainingLabel') }}</span>
                      <span>{{ calculateRemainingTaskCount(getNonOverdueTasks(current)) }}</span>
                    </div>
                  </div>
                  <!-- Task list -->
                  <div
                    v-for="item in getNonOverdueTasks(current)"
                    :key="item.id"
                    class="flex items-center mb-1 last:mb-0">
                    <IconTask :value="item.taskType?.value" class="text-4 flex-shrink-0 mr-1.5" />
                    <div class="flex-1 min-w-25 max-w-80 truncate mr-2.5">{{ item.name }}</div>
                    <TaskStatus :value="item.status" />
                  </div>
                </template>
              </Popover>
            </div>
          </template>

          <!-- Overdue tasks display -->
          <template v-if="getOverdueTasks(current).length">
            <div class="flex items-center text-3">
              <Icon icon="icon-xiaoqi" class="text-status-error text-3.5 mr-1" />
              <Popover placement="right" overlayClassName="calendar-popover-container">
                <Badge
                  class="text-3"
                  :overflowCount="99"
                  :count="getOverdueTasks(current).length" />
                <template #content>
                  <!-- Overdue task statistics summary -->
                  <div class="flex items-center flex-nowrap space-x-5 mb-1.5">
                    <div class="flex-shrink-0 space-x-1">
                      <span>{{ t('taskHome.workloadLabel') }}</span>
                      <span>{{ calculateTotalTaskCount(getOverdueTasks(current)) }}</span>
                    </div>
                    <div class="flex-shrink-0 space-x-1">
                      <span>{{ t('taskHome.completed') }}</span>
                      <span>{{ calculateCompletedTaskCount(getOverdueTasks(current)) }}</span>
                    </div>
                    <div class="flex-shrink-0 space-x-1">
                      <span>{{ t('taskHome.remainingLabel') }}</span>
                      <span>{{ calculateRemainingTaskCount(getOverdueTasks(current)) }}</span>
                    </div>
                  </div>

                  <!-- Overdue task list -->
                  <div
                    v-for="item in getOverdueTasks(current)"
                    :key="item.id"
                    class="flex items-center mb-1 last:mb-0">
                    <IconTask :value="item.taskType?.value" class="text-4 flex-shrink-0 mr-1.5" />
                    <div class="flex-1 min-w-25 max-w-80 truncate mr-2.5">{{ item.name }}</div>
                    <div class="flex items-center">
                      <TaskStatusV :value="item.status" />
                      <span
                        class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2"
                        style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
                        <span class="inline-block transform-gpu scale-90">{{ t('taskHome.overdue') }}</span>
                      </span>
                    </div>
                  </div>
                </template>
              </Popover>
            </div>
          </template>
        </template>
      </Calendar>
    </AsyncComponent>
  </div>
</template>
<style>
/* Popover container styling for calendar task details */
.calendar-popover-container .ant-popover-content .ant-popover-inner {
  max-height: 264px;
  padding: 12px 0;
  overflow: hidden;
  line-height: 20px;
}

.calendar-popover-container .ant-popover-content .ant-popover-inner-content {
  max-height: 240px;
  padding: 0 16px;
  overflow: auto;
}
</style>

<style scoped>
/* Badge count styling */
.ant-badge :deep(.ant-badge-count) {
  height: 16px;
  padding: 0 6px;
  border: none;
  background-color: rgba(239, 240, 243, 100%);
  color: var(--content-text-sub-content);
  line-height: 16px;
}

/* Calendar header styling */
:deep(.ant-picker-calendar-full) .ant-picker-calendar-header {
  padding-top: 0;
}

/* Hide calendar mode switch */
:deep(.ant-picker-calendar-full) .ant-picker-calendar-mode-switch {
  display: none;
}

/* Radio button styling */
:deep(.ant-picker-calendar-full) .ant-radio-button-wrapper {
  height: 28px;
  font-size: 12px;
  line-height: 26px;
}

/* Select component styling */
:deep(.ant-picker-calendar-full) .ant-select-single .ant-select-selector {
  height: 28px;
}

:deep(.ant-picker-calendar-full) .ant-select-single .ant-select-selector .ant-select-selection-item {
  line-height: 26px;
}

/* Calendar panel styling */
:deep(.ant-picker-calendar-full) .ant-picker-panel {
  border: 1px solid var(--border-text-box);
  border-radius: 4px;
}

:deep(.ant-picker-calendar-full) .ant-picker-panel .ant-picker-body {
  padding: 0;
}

/* Calendar header row styling */
:deep(.ant-picker-calendar-full) .ant-picker-panel .ant-picker-body .ant-picker-content thead {
  height: 40px;
  background-color: rgba(247, 248, 251, 100%);
}

:deep(.ant-picker-calendar-full) .ant-picker-panel .ant-picker-body .ant-picker-content thead th {
  padding: 0 14px;
  border-right: 1px solid var(--border-text-box);
  line-height: 20px;
}

:deep(.ant-picker-calendar-full) .ant-picker-panel .ant-picker-body .ant-picker-content thead th:last-child {
  border-right: none;
}

/* Calendar cell styling */
:deep(.ant-picker-calendar-full) .ant-picker-cell {
  border-right: 1px solid var(--border-text-box);
}

:deep(.ant-picker-calendar-full) .ant-picker-cell:last-child {
  border-right: none;
}

/* Calendar date styling */
:deep(.ant-picker-calendar-full) .ant-picker-panel .ant-picker-calendar-date {
  margin: 0;
  padding: 0;
  border-top-width: 1px;
}

:deep(.ant-picker-calendar-full) .ant-picker-panel .ant-picker-calendar-date-value {
  position: absolute;
  top: 50%;
  right: 14px;
  transform: translateY(-50%);
  line-height: 20px;
}

:deep(.ant-picker-calendar-full) .ant-picker-panel .ant-picker-calendar-date-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 44px;
  padding-left: 10px;
}

:deep(.ant-picker-calendar-full) .ant-picker-panel .ant-picker-calendar-date-content>div+div {
  margin-top: 2px;
}
</style>
