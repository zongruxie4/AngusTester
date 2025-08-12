<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Checkbox, Pagination } from 'ant-design-vue';
import { IconTask, TaskStatus } from '@xcan-angus/vue-ui';

import { TaskInfo } from '../../../../PropsType';

type Props = {
  projectId: string;
  checkedId: string;
  selectedIds: string[];
  dataSource: TaskInfo[];
  pagination: { current: number; pageSize: number; total: number; };
}

const { t } = useI18n();
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
  (e: 'checked', value: TaskInfo): void;
  (e: 'select', value: string[]): void;
}>();

const paginationChange = (current: number, pageSize: number) => {
  emit('paginationChange', { current, pageSize });
};

const click = (data: TaskInfo) => {
  emit('checked', data);
};

const checkboxChange = (event:{target:{checked:boolean;value:string;}}) => {
  const { checked, value } = event.target;
  if (checked) {
    if (!props.selectedIds.includes(value)) {
      emit('select', [...props.selectedIds, value]);
    }

    return;
  }

  const ids = props.selectedIds.filter(item => item !== value);
  emit('select', ids);
};

const showTotal = (value: number) => {
  return t('task.detail.pagination.total', { total: value });
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
        @click="click(item)">
        <Checkbox
          :checked="props.selectedIds.includes(item.id)"
          :value="item.id"
          class="flex-shrink-0 mr-2"
          @change="checkboxChange"
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
      :showTotal="showTotal"
      :showSizeChanger="false"
      class="mt-3 pr-1.5"
      size="small"
      showLessItems
      @change="paginationChange" />
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
