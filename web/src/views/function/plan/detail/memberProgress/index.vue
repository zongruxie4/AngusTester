<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { Image, Table } from '@xcan-angus/vue-ui';
import { Progress } from 'ant-design-vue';
import { http, TESTER } from '@xcan-angus/tools';

import { TableDataObj } from './PropsType';

interface Props {
  planId: string;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  planId: undefined,
  projectId: undefined
});

const loading = ref(false);
const tableData = ref<TableDataObj[]>([]);

const loadData = async () => {
  const params = {
    planId: props.planId,
    projectId: props.projectId
  };
  const [error, { data = [] }] = await http.get(`${TESTER}/analysis/func/tester/progress`, params);
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
    dataIndex: 'testerName',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '进度',
    dataIndex: 'passedTestRate',
    width: '13%',
    sorter: (a, b) => +a.passedTestRate - (+b.passedTestRate),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '总用例数',
    dataIndex: 'totalCaseNum',
    sorter: (a, b) => +a.totalCaseNum - (+b.totalCaseNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '有效用例数',
    dataIndex: 'validCaseNum',
    sorter: (a, b) => +a.validCaseNum - (+b.validCaseNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '通过测试用例数',
    dataIndex: 'passedTestNum',
    sorter: (a, b) => +a.passedTestNum - (+b.passedTestNum),
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
    title: '逾期用例数',
    dataIndex: 'overdueNum',
    sorter: (a, b) => +a.overdueNum - (+b.overdueNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: '逾期率',
    dataIndex: 'overdueRate',
    customRender: ({ text }) => text + '%'
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
      <template v-if="column.dataIndex === 'testerName'">
        <div class="flex items-center">
          <Image
            :src="record?.testerAvatar"
            type="avatar"
            class="w-5 h-5 rounded-full mr-1" />
          <div
            :title="text"
            class="truncate"
            style="width: 60px;">
            {{ text }}
          </div>
          <div
            v-if="+record?.passedTestRate === 100"
            class="text-white rounded-full bg-status-success text-3 px-1"
            style="transform: scale(0.8);">
            已完成
          </div>
        </div>
      </template>
      <template v-if="column.dataIndex === 'passedTestRate'">
        <Progress :percent="+record?.passedTestRate" style="width:100px;" />
      </template>
    </template>
  </Table>
</template>
