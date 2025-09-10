<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { Image, Table } from '@xcan-angus/vue-ui';
import { Progress } from 'ant-design-vue';
import { analysis } from '@/api/tester';
import { useI18n } from 'vue-i18n';


import {MemberProgressData} from "@/views/function/plan/types";

const { t } = useI18n();

interface Props {
  planId: string;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  planId: undefined,
  projectId: undefined
});

const loading = ref(false);
const tableData = ref<MemberProgressData[]>([]);

const loadData = async () => {
  const params = {
    planId: props.planId,
    projectId: props.projectId
  };
  const [error, { data = [] }] = await analysis.getFuncTesterProgress(params);
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
    title: t('functionPlan.planDetail.memberProgress.member'),
    dataIndex: 'testerName',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionPlan.planDetail.memberProgress.progress'),
    dataIndex: 'passedTestRate',
    width: '13%',
    sorter: (a, b) => +a.passedTestRate - (+b.passedTestRate),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionPlan.planDetail.memberProgress.totalCaseNum'),
    dataIndex: 'totalCaseNum',
    sorter: (a, b) => +a.totalCaseNum - (+b.totalCaseNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionPlan.planDetail.memberProgress.validCaseNum'),
    dataIndex: 'validCaseNum',
    sorter: (a, b) => +a.validCaseNum - (+b.validCaseNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionPlan.planDetail.memberProgress.passedTestNum'),
    dataIndex: 'passedTestNum',
    sorter: (a, b) => +a.passedTestNum - (+b.passedTestNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionPlan.planDetail.memberProgress.evalWorkload'),
    dataIndex: 'evalWorkload'
  },
  {
    title: t('functionPlan.planDetail.memberProgress.completedWorkload'),
    dataIndex: 'completedWorkload'
  },
  {
    title: t('functionPlan.planDetail.memberProgress.completedWorkloadRate'),
    dataIndex: 'completedWorkloadRate',
    customRender: ({ text }) => text + '%'
  },
  {
    title: t('functionPlan.planDetail.memberProgress.overdueNum'),
    dataIndex: 'overdueNum',
    sorter: (a, b) => +a.overdueNum - (+b.overdueNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionPlan.planDetail.memberProgress.overdueRate'),
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
            {{ t('functionPlan.planDetail.memberProgress.completed') }}
          </div>
        </div>
      </template>
      <template v-if="column.dataIndex === 'passedTestRate'">
        <Progress :percent="+record?.passedTestRate" style="width:100px;" />
      </template>
    </template>
  </Table>
</template>
