<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Image, Table } from '@xcan-angus/vue-ui';
import { Progress } from 'ant-design-vue';
import { analysis } from '@/api/tester';

import { MemberProgressData } from '@/views/issue/sprint/types';

const { t } = useI18n();

// Props Definition
interface Props {
  sprintId: string;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  sprintId: undefined,
  projectId: undefined
});

/**
 * Loading state for data operations
 */
const isLoading = ref(false);

/**
 * Member progress data for the table
 */
const memberProgressData = ref<MemberProgressData[]>([]);

/**
 * Loads member progress data from the API
 */
const loadMemberProgressData = async () => {
  const params = {
    sprintId: props.sprintId,
    projectId: props.projectId
  };
  const [error, { data = [] }] = await analysis.getAssigneeProgress(params);
  if (error) {
    return;
  }

  memberProgressData.value = data;
};

// Lifecycle Hooks
onMounted(() => {
  loadMemberProgressData();
});

/**
 * Table column configuration for member progress display
 */
const tableColumns = [
  {
    key: 'assigneeName',
    title: t('common.members'),
    dataIndex: 'assigneeName',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    key: 'completedRate',
    title: t('common.progress'),
    dataIndex: 'completedRate',
    width: '15%',
    sorter: (a, b) => +a.completedRate - (+b.completedRate),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    key: 'totalTaskNum',
    title: t('common.counts.totalCount'),
    dataIndex: 'totalTaskNum',
    sorter: (a, b) => +a.totalTaskNum - (+b.totalTaskNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    key: 'validTaskNum',
    title: t('common.counts.validCount'),
    dataIndex: 'validTaskNum',
    sorter: (a, b) => +a.validTaskNum - (+b.validTaskNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    key: 'completedNum',
    title: t('common.counts.completedCount'),
    dataIndex: 'completedNum',
    sorter: (a, b) => +a.validTaskNum - (+b.validTaskNum),
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
    title: t('common.counts.workloadCompletionRate'),
    dataIndex: 'completedWorkloadRate',
    customRender: ({ text }) => text + '%'
  },
  {
    key: 'overdueNum',
    title: t('common.counts.overdueCount'),
    dataIndex: 'overdueNum',
    sorter: (a, b) => +a.validTaskNum - (+b.validTaskNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    key: 'overdueRate',
    title: t('common.counts.overdueRate'),
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
    :dataSource="memberProgressData"
    :loading="isLoading"
    :pagination="false"
    rowKey="testerId"
    size="small"
    :noDataSize="'small'"
    :noDataText="t('common.noData')">
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
            {{ t('status.completed') }}
          </div>
        </div>
      </template>
      <template v-if="column.dataIndex === 'completedRate'">
        <Progress :percent="+record?.completedRate" style="width:100px;" />
      </template>
    </template>
  </Table>
</template>
