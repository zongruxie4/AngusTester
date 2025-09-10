<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Image, Modal, Table } from '@xcan-angus/vue-ui';
import { Progress } from 'ant-design-vue';
import { analysis } from '@/api/tester';

import { MemberProgressData } from '@/views/task/sprint/types';

const { t } = useI18n();

interface Props {
  visible: boolean;
  sprintId: string;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  sprintId: undefined,
  projectId: undefined
});

const emit = defineEmits<{(e: 'update:visible', value: boolean): void; }>();

const loading = ref(false);
const tableData = ref<MemberProgressData[]>([]);

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

const cancel = () => {
  emit('update:visible', false);
};

onMounted(() => {
  watch(() => props.visible, () => {
    if (props.visible === false || props.sprintId === undefined || props.sprintId === null || props.sprintId === '') {
      return;
    }

    loadData();
  }, { immediate: true });
});

const tableColumns = [
  {
    title: t('taskSprint.progress.member'),
    dataIndex: 'assigneeName',
    width: 140,
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
  <Modal
    :title="t('taskSprint.progress.title')"
    :visible="props.visible"
    :width="1200"
    :footer="null"
    @cancel="cancel">
    <Table
      :columns="tableColumns"
      :dataSource="tableData"
      :loading="loading"
      :pagination="false"
      :scroll="{ y: 600 }"
      class="mb-3"
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
              v-if="+record?.completedRate !== 100"
              class="text-white rounded-full bg-status-success text-3 px-1"
              style="transform: scale(0.8);">
              {{ t('taskSprint.progress.completed') }}
            </div>
          </div>
        </template>
        <template v-if="column.dataIndex === 'completedRate'">
          <Progress :percent="+record?.completedRate" style="width:120px;" />
        </template>
      </template>
    </Table>
  </Modal>
</template>
