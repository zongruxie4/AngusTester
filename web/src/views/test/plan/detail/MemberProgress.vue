<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { Image, Table } from '@xcan-angus/vue-ui';
import { Progress } from 'ant-design-vue';
import { analysis } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { MemberProgressData } from '@/views/test/plan/types';

const { t } = useI18n();

// Interfaces
interface Props {
  planId: string;
  projectId: string;
}

// Props
const props = withDefaults(defineProps<Props>(), {
  planId: undefined,
  projectId: undefined
});

// Reactive data
const loading = ref(false);
const memberProgressData = ref<MemberProgressData[]>([]);

/**
 * <p>
 * Loads member progress data from the API for the specified plan and project.
 * </p>
 * <p>
 * Fetches tester progress information including completion rates and workload statistics.
 * </p>
 */
const loadMemberProgressData = async () => {
  const requestParams = {
    planId: props.planId,
    projectId: props.projectId
  };
  const [error, { data = [] }] = await analysis.getFuncTesterProgress(requestParams);
  if (error) {
    return;
  }
  memberProgressData.value = data;
};

// Table configuration
const progressTableColumns = [
  {
    key: 'testerName',
    title: t('testPlan.planDetail.memberProgress.member'),
    dataIndex: 'testerName',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    key: 'passedTestRate',
    title: t('common.progress'),
    dataIndex: 'passedTestRate',
    width: '13%',
    sorter: (a, b) => +a.passedTestRate - (+b.passedTestRate),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    key: 'totalCaseNum',
    title: t('testPlan.planDetail.memberProgress.totalCaseNum'),
    dataIndex: 'totalCaseNum',
    sorter: (a, b) => +a.totalCaseNum - (+b.totalCaseNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    key: 'validCaseNum',
    title: t('testPlan.planDetail.memberProgress.validCaseNum'),
    dataIndex: 'validCaseNum',
    sorter: (a, b) => +a.validCaseNum - (+b.validCaseNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    key: 'passedTestNum',
    title: t('testPlan.planDetail.memberProgress.passedTestNum'),
    dataIndex: 'passedTestNum',
    sorter: (a, b) => +a.passedTestNum - (+b.passedTestNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    key: 'evalWorkload',
    title: t('common.evalWorkload'),
    dataIndex: 'evalWorkload'
  },
  {
    key: 'completedWorkload',
    title: t('common.completedWorkload'),
    dataIndex: 'completedWorkload'
  },
  {
    key: 'completedWorkloadRate',
    title: t('common.counts.completedWorkloadRate'),
    dataIndex: 'completedWorkloadRate',
    customRender: ({ text }) => text + '%'
  },
  {
    key: 'overdueNum',
    title: t('testPlan.planDetail.memberProgress.overdueCount'),
    dataIndex: 'overdueNum',
    sorter: (a, b) => +a.overdueNum - (+b.overdueNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    key: 'overdueRate',
    title: t('testPlan.planDetail.memberProgress.overdueRate'),
    dataIndex: 'overdueRate',
    customRender: ({ text }) => text + '%'
  }
];

// Lifecycle hooks
onMounted(() => {
  loadMemberProgressData();
});
</script>
<template>
  <Table
    :columns="progressTableColumns"
    :dataSource="memberProgressData"
    :loading="loading"
    :pagination="false"
    :noDataSize="'small'"
    :noDataText="t('common.noData')"
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
            {{ t('status.completed') }}
          </div>
        </div>
      </template>
      <template v-if="column.dataIndex === 'passedTestRate'">
        <Progress :percent="+record?.passedTestRate" style="width:100px;" />
      </template>
    </template>
  </Table>
</template>
