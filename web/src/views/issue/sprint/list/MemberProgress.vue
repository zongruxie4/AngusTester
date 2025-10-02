<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Image, Modal, Table } from '@xcan-angus/vue-ui';
import { Progress } from 'ant-design-vue';
import { analysis } from '@/api/tester';

import { MemberProgressData } from '@/views/issue/sprint/types';

const { t } = useI18n();

// Component Props & Emits
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

// Reactive Data
const isLoading = ref(false);
const memberProgressData = ref<MemberProgressData[]>([]);

/**
 * <p>
 * Table column configuration for member progress display.
 * <p>
 * Defines columns for member information, task statistics, workload metrics,
 * and completion rates with appropriate sorting and formatting.
 */
const tableColumns = [
  {
    key: 'assigneeName',
    title: t('common.members'),
    dataIndex: 'assigneeName',
    width: 140,
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

/**
 * <p>
 * Closes the member progress modal.
 * <p>
 * Emits update event to parent component to hide the modal.
 */
const closeModal = () => {
  emit('update:visible', false);
};

/**
 * <p>
 * Loads member progress data from the API.
 * <p>
 * Fetches assignee progress data for the current sprint and project,
 * then updates the table data reactive reference.
 */
const loadMemberProgressData = async () => {
  const requestParams = {
    sprintId: props.sprintId,
    projectId: props.projectId
  };
  const [error, { data = [] }] = await analysis.getAssigneeProgress(requestParams);
  if (error) {
    return;
  }

  memberProgressData.value = data;
};

/**
 * <p>
 * Validates if the modal should load data.
 * <p>
 * Checks if the modal is visible and has valid sprint ID before loading data.
 */
const shouldLoadData = () => {
  return props.visible &&
    props.sprintId !== undefined &&
    props.sprintId !== null &&
    props.sprintId !== '';
};

// Lifecycle Hooks
onMounted(() => {
  watch(() => props.visible, () => {
    if (shouldLoadData()) {
      loadMemberProgressData();
    }
  }, { immediate: true });
});
</script>
<template>
  <Modal
    :title="t('common.memberProgress')"
    :visible="props.visible"
    :width="1200"
    :footer="null"
    @cancel="closeModal">
    <Table
      :columns="tableColumns"
      :dataSource="memberProgressData"
      :loading="isLoading"
      :pagination="false"
      :scroll="{ x: true, y: 600, scrollToFirstRowOnChange: true }"
      :noDataSize="'middle'"
      :noDataText="t('common.noData')"
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
              {{ t('status.completed') }}
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
