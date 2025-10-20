<script lang="ts" setup>
import { onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { IconTask, Table } from '@xcan-angus/vue-ui';
import { useTaskData } from './composables/useTaskData';
import type { TaskProps } from './types';

import TaskPriority from '@/components/task/TaskPriority/index.vue';
import TaskStatus from '@/components/task/TaskStatus/index.vue';

const { t } = useI18n();

const props = withDefaults(defineProps<TaskProps>(), {
  scenarioId: '',
  projectId: ''
});

// Use task data composable
const {
  pagination,
  loading,
  taskList,
  columns,
  emptyTextStyle,
  loadTasks,
  handlePaginationChange
} = useTaskData(props.scenarioId, props.projectId);

// Initialize data on component mount
onMounted(() => {
  loadTasks();
});
</script>
<template>
  <div>
    <template v-if="!taskList?.length">
      <div class="flex-1 flex flex-col items-center justify-center">
        <img class="w-27.5" src="../../../../assets/images/nodata.png">
        <div class="flex items-center text-theme-sub-content text-3 leading-5">
          {{ t('common.noData') }}
        </div>
      </div>
    </template>
    <Table
      v-else
      :dataSource="taskList"
      :columns="columns"
      :loading="loading"
      :emptyTextStyle="emptyTextStyle"
      :pagination="pagination"
      rowKey="id"
      size="small"
      noDataSize="small"
      noDataText=""
      @change="handlePaginationChange">
      <template #bodyCell="{ record, column }">
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

        <TaskPriority v-else-if="column.dataIndex === 'priority'" :value="record.priority" />

        <TaskStatus v-else-if="column.dataIndex === 'status'" :value="record.status" />

        <div v-else-if="column.dataIndex === 'scriptType'" class="truncate">
          {{ record.scriptType?.message }}
        </div>
      </template>
    </Table>
  </div>
</template>

<style scoped>
.link {
  color: #1890ff;
  cursor: pointer;
}

:deep(.ant-pagination) {
  margin-bottom: 0;
}
</style>
