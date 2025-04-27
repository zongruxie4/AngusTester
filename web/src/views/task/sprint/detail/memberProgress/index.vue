<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { Image, Table } from '@xcan-angus/vue-ui';
import { Progress } from 'ant-design-vue';
import { http, TESTER } from '@xcan-angus/tools';

import { TableDataObj } from './PropsType';

interface Props {
  sprintId: string;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  sprintId: undefined,
  projectId: undefined
});

const loading = ref(false);
const tableData = ref<TableDataObj[]>([]);

const loadData = async () => {
  const params = {
    sprintId: props.sprintId,
    projectId: props.projectId
  };
  const [error, { data = [] }] = await http.get(`${TESTER}/analysis/task/assignee/progress`, params);
  if (error) {
    return;
  }

  tableData.value = data;
};

onMounted(() => {
  loadData();
});

const tableColumns = [
  {
    title: '成员',
    dataIndex: 'assigneeName',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '进度',
    dataIndex: 'completedRate',
    width: '15%',
    sorter: (a, b) => +a.completedRate - (+b.completedRate),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '总任务数',
    dataIndex: 'totalTaskNum',
    sorter: (a, b) => +a.totalTaskNum - (+b.totalTaskNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '有效任务数',
    dataIndex: 'validTaskNum',
    sorter: (a, b) => +a.validTaskNum - (+b.validTaskNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '完成任务数',
    dataIndex: 'completedNum',
    sorter: (a, b) => +a.validTaskNum - (+b.validTaskNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '评估工作量',
    dataIndex: 'evalWorkload'
  },
  {
    title: '完成工作量',
    dataIndex: 'completedWorkload'
  },
  {
    title: '工作量完成率',
    dataIndex: 'completedWorkloadRate',
    customRender: ({ text }) => text + '%'
  },
  {
    title: '逾期任务数',
    dataIndex: 'overdueNum',
    sorter: (a, b) => +a.validTaskNum - (+b.validTaskNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '逾期率',
    dataIndex: 'overdueRate',
    customRender: ({ text }) => text + '%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  }
];
</script>
<template>
  <Table
    :columns="tableColumns"
    :dataSource="tableData"
    :loading="loading"
    :pagination="false"
    rowKey="testerId"
    size="small">
    <template #bodyCell="{ column, text, record }">
      <template v-if="column.dataIndex === 'assigneeName'">
        <div class="flex items-center">
          <Image
            :src="record?.assigneeAvatar"
            type="avatar"
            class="w-5 h-5 rounded-full mr-1" />
          <div
            :title="text"
            class="truncate"
            style="width: 80px;">
            {{ text }}
          </div>
          <div
            v-if="+record?.completedRate === 100"
            class="text-white rounded-full bg-status-success text-3 px-1"
            style="transform: scale(0.8);">
            已完成
          </div>
        </div>
      </template>
      <template v-if="column.dataIndex === 'completedRate'">
        <Progress :percent="+record?.completedRate" style="width:100px;" />
      </template>
    </template>
  </Table>
</template>
