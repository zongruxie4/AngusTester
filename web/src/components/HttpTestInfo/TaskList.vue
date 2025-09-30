<script lang="ts" setup>
// Vue core imports
import { inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Icon, Tooltip, Grid } from '@xcan-angus/vue-ui';

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
  taskType: 'API_TEST' | 'BUG' | 'SCENARIO_TEST' | 'STORY' | 'TASK';
  targetId: string;
}

/**
 * Component props interface for task list display
 */
interface Props {
  dataSource: TaskItem[];
}

// Injected project information
const projectInfo = inject('projectInfo', ref({ id: '' }));

/**
 * Get task type icon based on task type
 * @param taskType - The type of task
 * @returns Icon name for the task type
 */
const getTaskTypeIcon = (taskType: string) => {
  switch (taskType) {
    case 'API_TEST':
      return 'icon-jiekouceshi';
    case 'SCENARIO_TEST':
      return 'icon-changjingceshi';
    case 'BUG':
      return 'icon-quexian';
    case 'STORY':
      return 'icon-gushi';
    case 'TASK':
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
      label: t('xcan_httpTestInfo.sprintName')
    },
    {
      dataIndex: 'assigneeName',
      label: t('xcan_httpTestInfo.assigneeName')
    },
    {
      dataIndex: 'confirmerName',
      label: t('xcan_httpTestInfo.confirmerName')
    },
    { dataIndex: 'createdByName', label: t('common.createdBy') },
    { dataIndex: 'createdDate', label: t('common.createdDate') }
  ]
];

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ([])
});

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
          <span class="min-w-0 truncate flex-1" :title="taskItem.name"><a target="_blank" :href="`/tasks#tasks?projectId=${projectInfo.id}&taskId=${taskItem.targetId}&taskName=${taskItem.name}`">{{ taskItem.name }}</a></span>
        </div>
      </Tooltip>
    </div>
  </div>
</template>
