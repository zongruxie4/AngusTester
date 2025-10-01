<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Checkbox, Pagination } from 'ant-design-vue';
import { IconTask } from '@xcan-angus/vue-ui';

import { TaskDetail } from '@/views/issue/types';
import TaskStatus from '@/components/TaskStatus/index.vue';

/**
 * Component props interface for task list display
 */
type Props = {
  projectId: string;
  checkedId: string;
  selectedIds: string[];
  dataSource: TaskDetail[];
  pagination: { current: number; pageSize: number; total: number; };
}

// Composables
const { t } = useI18n();

// Props & emits
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  checkedId: undefined,
  selectedIds: () => [],
  dataSource: () => [],
  pagination: () => ({ current: 1, pageSize: 10, total: 0 })
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'paginationChange', value: { current: number; pageSize: number; }): void;
  (e: 'checked', value: TaskDetail): void;
  (e: 'select', value: string[]): void;
}>();

/**
 * Handles pagination change events and emits the new pagination state
 * @param current - The current page number
 * @param pageSize - The number of items per page
 */
const handlePaginationChange = (current: number, pageSize: number) => {
  emit('paginationChange', { current, pageSize });
};

/**
 * Handles task item click events and emits the selected task
 * @param taskData - The task data that was clicked
 */
const handleTaskClick = (taskData: TaskDetail) => {
  emit('checked', taskData);
};

/**
 * Handles checkbox change events for task selection
 * Adds or removes task IDs from the selected list based on checkbox state
 * @param event - The checkbox change event containing checked state and value
 */
const handleCheckboxChange = (event: { target: { checked: boolean; value: string; } }) => {
  const { checked, value } = event.target;
  if (checked) {
    // Add to selection if not already selected
    if (!props.selectedIds.includes(value)) {
      emit('select', [...props.selectedIds, value]);
    }
    return;
  }

  // Remove from selection
  const updatedSelectedIds = props.selectedIds.filter(id => id !== value);
  emit('select', updatedSelectedIds);
};

/**
 * Formats the pagination total display text using i18n
 * @param totalCount - The total number of items
 * @returns The formatted total text
 */
const formatPaginationTotal = (totalCount: number) => {
  return t('chart.total', { total: totalCount });
};
</script>
<template>
  <div class="w-65 h-full pb-1.5 flex flex-col flex-shrink-0 border-r border-solid border-theme-text-box">
    <div style="height:calc(100% - 40px);" class="overflow-auto">
      <div
        v-for="(item) in props.dataSource"
        :key="item.id"
        :class="{ 'checked': props.checkedId === item.id }"
        class="task-item flex items-center px-2.5 py-2 border-b cursor-pointer border-theme-text-box"
        @click="handleTaskClick(item)">
        <Checkbox
          :checked="props.selectedIds.includes(item.id)"
          :value="item.id"
          class="flex-shrink-0 mr-2"
          @change="handleCheckboxChange"
          @click.stop="" />
        <div class="flex-1 truncate">
          <div class="flex items-center">
            <IconTask :value="item.taskType.value" class="flex-shrink-0 mr-1.5" />
            <div :title="item.name" class="flex-1 task-name truncate">{{ item.name }}</div>
          </div>
          <div class="flex items-center pl-5 mt-0.5 space-x-2 overflow-hidden">
            <div class="flex-1 truncate font-medium text-theme-title task-name">{{ item.code }}</div>
            <TaskStatus :value="item.status" />
          </div>
        </div>
      </div>
    </div>

    <Pagination
      v-bind="props.pagination"
      :showTotal="formatPaginationTotal"
      :showSizeChanger="false"
      class="mt-3 pr-1.5"
      size="small"
      showLessItems
      @change="handlePaginationChange" />
  </div>
</template>

<style scoped>
.task-item:hover {
  background-color: rgba(245, 245, 245, 100%);
}

.checked.task-item {
  background-color: rgba(239, 240, 243, 100%);
}

.checked.task-item .task-name {
  color: #1890ff;
}
</style>
