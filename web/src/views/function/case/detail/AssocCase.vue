<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, ref } from 'vue';
import { AsyncComponent, Hints, Icon, modal, ReviewStatus, Table } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { Button } from 'ant-design-vue';
import { funcCase } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import TaskPriority from '@/components/TaskPriority/index.vue';
import TestResult from '@/components/TestResult/index.vue';

interface Props {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  caseId: string;
  dataSource: {
    id: string;
    name: string;
  }[];
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  caseId: undefined,
  dataSource: undefined
});

const SelectCaseByModuleModal = defineAsyncComponent(() => import('@/components/function/case/selectByModuleModal/index.vue'));

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'editSuccess'): void;
}>();

const addTabPane = inject('addTabPane', (value: any) => value);

const caseProgress = computed(() => {
  const completed = (props.dataSource || []).filter(item => ['PASSED'].includes(item?.testResult?.value)).length;
  const total = (props.dataSource || []).filter(item => !['CANCELED'].includes(item?.testResult?.value)).length;
  const completedRate = total > 0 ? (completed / total * 100).toFixed(2) : 0;
  return {
    completed,
    completedRate,
    total
  };
});

const submitLoading = ref(false);
const editRef = ref(false);

const selectCaseVisible = ref(false);

const cancelEdit = () => {
  editRef.value = false;
  // refCaseIds.value = [];
};
const startEdit = () => {
  // editRef.value = true;
  selectCaseVisible.value = true;
};
// const refCaseIds = ref<string[]>([]);
const handlePut = async (refCaseIds) => {
  selectCaseVisible.value = false;
  if (!refCaseIds.length) {
    cancelEdit();
    return;
  }
  submitLoading.value = true;
  const [error] = await funcCase.putAssociationCase(props.caseId, {
    assocCaseIds: refCaseIds
  }, {
    paramsType: true
  });
  submitLoading.value = false;
  if (error) {
    return;
  }
  emit('editSuccess');
};

const handleDelTask = (record) => {
  modal.confirm({
    content: t('functionCase.kanbanView.assocCase.confirmCancelAssocCase', { name: record.name }),
    onOk () {
      return funcCase.cancelAssociationCase(props.caseId, {
        assocCaseIds: [record.id]
      }, {
        paramsType: true
      }).then(([error]) => {
        if (error) {
          return;
        }
        emit('editSuccess');
      });
    }
  });
};

const openCaseTab = (record) => {
  addTabPane({
    _id: record.id,
    name: record.caseName,
    type: 'caseInfo',
    closable: true,
    caseId: record.id
  });
};
// 编号、名称、优先级、评估故事点、状态、测试人、截止时间、操作
const columns = [
  {
    key: 'code',
    dataIndex: 'code',
    title: t('functionCase.kanbanView.assocCase.code')
  },
  {
    key: 'name',
    dataIndex: 'name',
    title: t('functionCase.kanbanView.assocCase.name')
  },
  {
    key: 'priority',
    dataIndex: 'priority',
    title: t('functionCase.kanbanView.assocCase.priority')
  },
  {
    key: 'evalWorkload',
    dataIndex: 'evalWorkload',
    title: t('functionCase.kanbanView.assocCase.evalStoryPoint')
  },
  {
    key: 'status',
    dataIndex: 'status',
    title: t('functionCase.kanbanView.assocCase.status')
  },
  {
    key: 'testerName',
    dataIndex: 'testerName',
    title: t('functionCase.kanbanView.assocCase.tester')
  },
  {
    key: 'deadlineDate',
    dataIndex: 'deadlineDate',
    title: t('functionCase.kanbanView.assocCase.deadline')
  },
  {
    key: 'action',
    dataIndex: 'action',
    title: t('functionCase.kanbanView.assocCase.actions')
  }
];

</script>
<template>
  <div>
    <div class="flex mb-2 items-center">
      <!-- <div class="flex items-center flex-nowrap h-8 px-3.5 rounded" style="background-color:#FAFAFA;">
        <span class="flex-shrink-0 font-semibold text-theme-title">进度</span>
        <Colon class="mr-1.5" />
        <span class="font-semibold text-3.5" style="color: #07F;">{{ caseProgress?.completed || 0 }}</span>
        <span class="font-semibold text-3.5 mx-1">/</span>
        <span class="font-semibold text-3.5 mr-3.5">{{ caseProgress?.total || 0 }}</span>
        <Progress
          :percent="+caseProgress?.completedRate"
          style="width: 120px;"
          class="mr-3.5"
          :showInfo="false" />
        <span class="font-semibold text-3.5">{{ caseProgress?.completedRate || 0 }}%</span>
      </div> -->
      <div class="flex-1 truncate min-w-0 px-1">
        <Hints :text="t('functionCase.kanbanView.assocCase.associateCaseTip')" />
      </div>
      <Button
        :disabled="props.dataSource?.length > 19"
        :loading="submitLoading"
        size="small"
        @click="startEdit">
        <Icon icon="icon-jia" class="mr-1" />
        {{ t('functionCase.kanbanView.assocCase.associateCase') }}
      </Button>
    </div>
    <Table
      :columns="columns"
      :dataSource="props.dataSource || []"
      :pagination="false"
      size="small"
      noDataSize="small">
      <template #bodyCell="{column, record}">
        <template v-if="column.dataIndex === 'name'">
          <Button
            type="link"
            size="small"
            @click="openCaseTab(record)">
            {{ record.name }}
          </Button>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <Button
            size="small"
            type="text"
            @click="handleDelTask(record)">
            <Icon icon="icon-qingchu" class="mr-1" />
            {{ t('actions.cancel') }}
          </Button>
        </template>
        <template v-if="column.dataIndex === 'priority'">
          <TaskPriority :value="record?.priority" />
        </template>
        <template v-if="column.dataIndex === 'status'">
          <!-- <TaskStatus :value="record?.status" /> -->
          <ReviewStatus v-if="record?.reviewStatus?.value !== 'PASSED' && record.review" :value="record.reviewStatus" />
          <TestResult v-else :value="record.testResult" />
        </template>
      </template>
    </Table>

    <AsyncComponent :visible="selectCaseVisible">
      <SelectCaseByModuleModal
        v-model:visible="selectCaseVisible"
        :projectId="props.projectId"
        :action="`${TESTER}/func/case/${props.caseId}/case/notAssociated`"
        @ok="handlePut" />
    </AsyncComponent>
  </div>
</template>
