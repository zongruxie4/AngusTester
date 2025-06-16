<script setup lang="ts">
import { computed, ref } from 'vue';
import { http } from '@xcan-angus/tools';

import { IconTask, Table, TaskPriority, TaskStatus } from '@xcan-angus/vue-ui';

type Props = {
  taskList: any[]
}

const props = withDefaults(defineProps<Props>(), {
  taskList: () => ([])
});

const tableList = computed(() => {
  return (props.taskList || []).map(item => {
    const _params = {
      taskId: item.id,
      pageNo: 1,
      pageSize: 1,
      total: 1
    };

    return {
      ...item,
      linkUrl: '/task#task?' + http.getURLSearchParams(_params, true)
    };
  });
});

const loading = ref(false);

const columns = computed(() => {
  const _columns: {
    title: string;
    dataIndex: string;
    ellipsis?: boolean;
    sorter?: boolean;
    width?: string | number;
    actionKey?: 'createdBy' | 'favouriteBy' | 'followBy';
  }[] = [
    {
      title: '编码',
      dataIndex: 'code',
      ellipsis: true,
      width: 100
    },
    {
      title: '名称',
      dataIndex: 'name',
      ellipsis: true,
      width: '25%'
    },
    {
      title: '所属迭代',
      dataIndex: 'sprintName',
      ellipsis: true,
      width: '25%'
    },
    {
      title: '优先级',
      dataIndex: 'priority',
      ellipsis: true,
      width: '9%'
    },
    {
      title: '经办人',
      dataIndex: 'assigneeName',
      width: 120
    },
    {
      title: '确认人',
      dataIndex: 'confirmorName',
      width: 120
    },
    {
      title: '截止时间',
      dataIndex: 'deadlineDate',
      ellipsis: true,
      width: '17%'
    }
  ];

  return _columns;
});

const emptyTextStyle = {
  margin: '14px auto',
  height: 'auto'
};
</script>

<template>
  <div>
    <template v-if="!props.taskList?.length">
      <div class="flex-1 flex flex-col items-center justify-center">
        <img class="w-27.5" src="../../../../../assets/images/nodata.png">
        <div class="flex items-center text-theme-sub-content text-3 leading-5">
          暂无数据
        </div>
      </div>
    </template>
    <Table
      v-else
      :dataSource="tableList"
      :columns="columns"
      :loading="loading"
      :emptyTextStyle="emptyTextStyle"
      :pagination="false"
      :scroll="{y: 400}"
      rowKey="id"
      size="small">
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
            <span class="inline-block transform-gpu scale-90">已逾期</span>
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
