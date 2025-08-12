<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Image, Table } from '@xcan-angus/vue-ui';
import { Progress } from 'ant-design-vue';
import { analysis } from '@/api/tester';

const { t } = useI18n();

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
  const [error, { data = [] }] = await analysis.getAssigneeProgress(params);
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
    title: t('taskSprint.progress.member'),
    dataIndex: 'assigneeName',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('taskSprint.progress.progress'),
    dataIndex: 'completedRate',
    width: '15%',
    sorter: (a, b) => +a.completedRate - (+b.completedRate),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('taskSprint.progress.totalTaskCount'),
    dataIndex: 'totalTaskNum',
    sorter: (a, b) => +a.totalTaskNum - (+b.totalTaskNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('taskSprint.progress.validTaskCount'),
    dataIndex: 'validTaskNum',
    sorter: (a, b) => +a.validTaskNum - (+b.validTaskNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('taskSprint.progress.completedTaskCount'),
    dataIndex: 'completedNum',
    sorter: (a, b) => +a.validTaskNum - (+b.validTaskNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('taskSprint.progress.estimatedWorkload'),
    dataIndex: 'evalWorkload'
  },
  {
    title: t('taskSprint.progress.completedWorkload'),
    dataIndex: 'completedWorkload'
  },
  {
    title: t('taskSprint.progress.workloadCompletionRate'),
    dataIndex: 'completedWorkloadRate',
    customRender: ({ text }) => text + '%'
  },
  {
    title: t('taskSprint.progress.overdueTaskCount'),
    dataIndex: 'overdueNum',
    sorter: (a, b) => +a.validTaskNum - (+b.validTaskNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('taskSprint.progress.overdueRate'),
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
            {{ t('taskSprint.progress.completed') }}
          </div>
        </div>
      </template>
      <template v-if="column.dataIndex === 'completedRate'">
        <Progress :percent="+record?.completedRate" style="width:100px;" />
      </template>
    </template>
  </Table>
</template>
