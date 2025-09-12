<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';
import { AsyncComponent, Hints, Icon, modal, ReviewStatus, Table, TaskPriority, TestResult } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { FuncPlanStatus } from '@/enums/enums';
import { Button } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { task } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { TaskInfoProps } from '@/views/task/task/list/task/types';

const props = withDefaults(defineProps<TaskInfoProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  taskId: undefined,
  dataSource: undefined
});

const router = useRouter();
const { t } = useI18n();
const SelectCaseByModuleModal = defineAsyncComponent(() => import('@/components/function/case/selectByModuleModal/index.vue'));

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'editSuccess'): void;
}>();

// const addTabPane = inject('addTabPane', (value: any) => value);
const submitLoading = ref(false);
const editRef = ref(false);

const selectCaseVisible = ref(false);
const cancelEdit = () => {
  editRef.value = false;
};
const startEdit = () => {
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
  const [error] = await task.associationCase(props.taskId, refCaseIds, {
    paramsType: true
  });
  submitLoading.value = false;
  if (error) {
    return;
  }
  emit('editSuccess');
};

const handleDelCase = (record) => {
  modal.confirm({
    content: t('task.assocCase.messages.confirmCancelCase', { name: record.name }),
    onOk () {
      return task.cancelAssociationCase(props.taskId, [record.id], {
        paramsType: true
      }).then(([error]) => {
        if (error) {
          return;
        }
        // editRef.value = false;
        emit('editSuccess');
      });
    }
  });
};

const openCaseTab = (record) => {
  router.push(`/function#cases?id=${record.id}`);
};

const columns = [
  {
    key: 'code',
    dataIndex: 'code',
    title: t('task.assocCase.columns.code')
  },
  {
    key: 'name',
    dataIndex: 'name',
    title: t('task.assocCase.columns.name')
  },
  {
    key: 'priority',
    dataIndex: 'priority',
    title: t('task.assocCase.columns.priority')
  },
  {
    key: 'evalWorkload',
    dataIndex: 'evalWorkload',
    title: t('task.assocCase.columns.evalWorkload')
  },
  {
    key: 'status',
    dataIndex: 'status',
    title: t('task.assocCase.columns.status')
  },
  {
    key: 'testerName',
    dataIndex: 'testerName',
    title: t('task.assocCase.columns.testerName')
  },
  {
    key: 'deadlineDate',
    dataIndex: 'deadlineDate',
    title: t('task.assocCase.columns.deadlineDate')
  },
  {
    key: 'action',
    dataIndex: 'action',
    title: t('task.assocCase.columns.action')
  }
];
</script>
<template>
  <div>
    <div class="flex mb-2 items-center pr-2">
      <div class="flex-1 min-w-0 truncate px-1">
        <Hints :text="t('task.assocCase.description')" />
      </div>
      <Button
        :disabled="props.dataSource?.length > 19"
        :loading="submitLoading"
        size="small"
        @click="startEdit">
        <Icon icon="icon-jia" class="mr-1" />
        {{ t('task.assocCase.actions.associateCase') }}
      </Button>
    </div>
    <Table
      :columns="columns"
      :dataSource="props.dataSource || []"
      :pagination="false"
      size="small"
      noDataSize="small"
      noDataText="">
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
            @click="handleDelCase(record)">
            <Icon icon="icon-qingchu" class="mr-1" />
            {{ t('task.assocCase.actions.cancel') }}
          </Button>
        </template>
        <template v-if="column.dataIndex === 'priority'">
          <TaskPriority :value="record?.priority" />
        </template>
        <template v-if="column.dataIndex === 'status'">
          <ReviewStatus
            v-if="record?.reviewStatus?.value === FuncPlanStatus.PENDING && record.review"
            :value="record.reviewStatus" />
          <TestResult v-else :value="record.testResult" />
        </template>
      </template>
    </Table>

    <AsyncComponent :visible="selectCaseVisible">
      <SelectCaseByModuleModal
        v-model:visible="selectCaseVisible"
        :projectId="props.projectId"
        :action="`${TESTER}/task/${props.taskId}/case/notAssociated`"
        @ok="handlePut" />
    </AsyncComponent>
  </div>
</template>
