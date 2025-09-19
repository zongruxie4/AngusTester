<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Image, Modal, Table } from '@xcan-angus/vue-ui';
import { Progress } from 'ant-design-vue';
import { analysis } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { MemberProgressData } from '@/views/function/plan/types';

// composables
const { t } = useI18n();

// interfaces
interface Props {
  visible: boolean;
  planId: string;
  projectId: string;
}

// props and emits
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  planId: undefined,
  projectId: undefined
});

const emit = defineEmits<{(e: 'update:visible', value: boolean): void; }>();

// reactive data
const loading = ref(false);
const tableData = ref<MemberProgressData[]>([]);

/**
 * Loads member progress data from API based on plan and project IDs
 */
const loadMemberProgressData = async () => {
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

/**
 * Handles modal cancel event and emits visibility update
 */
const handleCancel = () => {
  emit('update:visible', false);
};

// lifecycle hooks
onMounted(() => {
  watch(() => props.visible, () => {
    if (!props.visible || props.planId === undefined || props.planId === null || props.planId === '') {
      return;
    }

    loadMemberProgressData();
  }, { immediate: true });
});

// table configuration
const tableColumns = [
  {
    title: t('functionPlan.comp.progress.member'),
    width: 140,
    dataIndex: 'testerName',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionPlan.comp.progress.progress'),
    dataIndex: 'passedTestRate',
    width: '15%',
    sorter: (a, b) => +a.passedTestRate - (+b.passedTestRate),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionPlan.comp.progress.totalCaseNum'),
    dataIndex: 'totalCaseNum',
    sorter: (a, b) => +a.totalCaseNum - (+b.totalCaseNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionPlan.comp.progress.validCaseNum'),
    dataIndex: 'validCaseNum',
    sorter: (a, b) => +a.validCaseNum - (+b.validCaseNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionPlan.comp.progress.passedTestNum'),
    dataIndex: 'passedTestNum',
    sorter: (a, b) => +a.passedTestNum - (+b.passedTestNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionPlan.comp.progress.evalWorkload'),
    dataIndex: 'evalWorkload'
  },
  {
    title: t('functionPlan.comp.progress.completedWorkload'),
    dataIndex: 'completedWorkload'
  },
  {
    title: t('functionPlan.comp.progress.completedWorkloadRate'),
    dataIndex: 'completedWorkloadRate',
    customRender: ({ text }) => text + '%'
  },
  {
    title: t('functionPlan.comp.progress.overdueNum'),
    dataIndex: 'overdueNum',
    width: '12%',
    sorter: (a, b) => +a.overdueNum - (+b.overdueNum),
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionPlan.comp.progress.overdueRate'),
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
    :title="t('functionPlan.comp.progress.title')"
    :visible="props.visible"
    :width="1200"
    :footer="null"
    @cancel="handleCancel">
    <Table
      :columns="tableColumns"
      :dataSource="tableData"
      :loading="loading"
      :pagination="false"
      :scroll="{ y: 600 }"
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
              style="width: 140px;">
              {{ text }}
            </div>
            <div
              v-if="+record?.passedTestRate === 100"
              class="text-white rounded-full bg-status-success text-3 px-2"
              style="transform: scale(0.8);">
              {{ t('functionPlan.comp.progress.completed') }}
            </div>
          </div>
        </template>
        <template v-if="column.dataIndex === 'passedTestRate'">
          <Progress :percent="+record?.passedTestRate" style="width:120px;" />
        </template>
      </template>
    </Table>
  </Modal>
</template>
