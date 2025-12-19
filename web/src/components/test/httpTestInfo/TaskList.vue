<script lang="ts" setup>
import { inject, Ref, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Tooltip, Grid } from '@xcan-angus/vue-ui';
import { ProjectInfo } from '@/layout/types';
import { TaskType } from '@/enums/enums';
import { IssueMenuKey } from '@/views/issue/menu';

const { t } = useI18n();

/**
 * Task item interface for task list display
 */
interface TaskItem {
  name: string;
  id: string;
  code: string;
  serviceId: string;
  serviceName: string;
  taskType: TaskType;
  targetId: string;
}

interface Props {
  dataSource: TaskItem[];
}
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ([])
});

// Injected project information
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));

/**
 * Get task type icon based on task type
 * @param taskType - The type of task
 * @returns Icon name for the task type
 */
const getTaskTypeIcon = (taskType: string) => {
  switch (taskType) {
    case TaskType.DESIGN:
      return 'icon-jiekouceshi';
    case TaskType.BUG:
      return 'icon-quexian';
    case TaskType.STORY:
      return 'icon-gushi';
    case TaskType.TASK:
      return 'icon-renwu1';
    default:
      return 'icon-renwu1';
  }
};

// Task tooltip table columns configuration
const taskTooltipTableColumns = [
  [
    {
      dataIndex: 'sprintName',
      label: t('common.sprint')
    },
    {
      dataIndex: 'assigneeName',
      label: t('common.assignee')
    },
    {
      dataIndex: 'confirmerName',
      label: t('common.confirmer')
    },
    {
      dataIndex: 'creator',
      label: t('common.createdBy')
    },
    {
      dataIndex: 'createdDate',
      label: t('common.createdDate')
    }
  ]
];
</script>
<template>
  <div class="text-3">
    <div
      v-for="taskItem in props.dataSource"
      :key="taskItem.id"
      class="border border-gray-light rounded bg-gray-light">
      <Tooltip placement="rightTop">
        <template #title>
          <div class="max-h-100 overflow-y-auto">
            <Grid
              :dataSource="taskItem || {}"
              :columns="taskTooltipTableColumns" />
          </div>
        </template>
        <div class="px-1 flex h-6 items-center">
          <Icon :icon="getTaskTypeIcon(taskItem.taskType)" />
          <span class="min-w-0 truncate flex-1" :title="taskItem.name">
            <a
              target="_blank"
              :href="`/issue#${IssueMenuKey.ISSUE}?projectId=${projectInfo.id}&taskId=${taskItem.targetId}&taskName=${taskItem.name}`">
              {{ taskItem.name }}
            </a>
          </span>
        </div>
      </Tooltip>
    </div>
  </div>
</template>
