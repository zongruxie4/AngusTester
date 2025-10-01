<script lang="ts" setup>
import { computed, defineAsyncComponent, ref } from 'vue';
import { AsyncComponent, Hints, Icon, modal, Table } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { Button, Progress } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { funcCase } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { TaskType } from '@/enums/enums';
import { AssocTaskProps } from '@/views/test/case/types';

import TaskPriority from '@/components/TaskPriority/index.vue';
import TaskStatus from '@/components/TaskStatus/index.vue';
const SelectTaskByModuleModal = defineAsyncComponent(() => import('@/components/task/SelectByModuleModal.vue'));

const router = useRouter();
const { t } = useI18n();

const props = withDefaults(defineProps<AssocTaskProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  caseId: undefined,
  dataSource: undefined,
  title: 'Task',
  taskType: TaskType.TASK,
  tip: ''
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'editSuccess'): void;
}>();

const submitLoading = ref(false);
const selectTaskVisible = ref(false);

const tableData = computed(() => {
  return (props.dataSource || []).filter(item => item.taskType.value === props.taskType);
});

// const editRef = ref(false);
const cancelEdit = () => {
  // editRef.value = false;
  selectTaskVisible.value = false;
};
const startEdit = () => {
  // editRef.value = true;
  selectTaskVisible.value = true;
};
// const refTaskIds = ref<string[]>([]);
const handlePut = async (refTaskIds) => {
  selectTaskVisible.value = false;
  if (!refTaskIds.length) {
    cancelEdit();
    return;
  }
  submitLoading.value = true;
  const [error] = await funcCase.putAssociationTask(props.caseId, {
    assocTaskIds: refTaskIds
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
    content: t('functionCase.detail.assocTask.confirmCancelAssocTask', { name: record.name }),
    onOk () {
      return funcCase.cancelAssociationTask(props.caseId, {
        assocTaskIds: [record.id]
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

const openTask = (record) => {
  router.push(`/task#task?taskId=${record.id}`);
};

const columns = [
  {
    key: 'code',
    dataIndex: 'code',
    title: t('common.code')
  },
  {
    key: 'name',
    dataIndex: 'name',
    title: t('common.name')
  },
  {
    key: 'progress',
    dataIndex: 'progress',
    title: t('common.progress')
  },
  {
    key: 'taskType',
    dataIndex: 'taskType',
    title: t('common.type')
  },
  {
    key: 'priority',
    dataIndex: 'priority',
    title: t('common.priority')
  },
  {
    key: 'evalWorkload',
    dataIndex: 'evalWorkload',
    title: t('common.evalWorkload')
  },
  {
    key: 'status',
    dataIndex: 'status',
    title: t('common.status')
  },
  {
    key: 'assigneeName',
    dataIndex: 'assigneeName',
    title: t('common.assignee')
  },
  {
    key: 'deadlineDate',
    dataIndex: 'deadlineDate',
    title: t('common.deadlineDate')
  },
  {
    key: 'action',
    dataIndex: 'action',
    title: t('common.actions')
  }
];
</script>
<template>
  <div>
    <div class="flex mb-2 items-center pr-2">
      <div class="flex-1 ml-1 min-w-0 truncate">
        <Hints v-if="props.tips" :text="props.tips" />
      </div>
      <Button
        :disabled="props.dataSource?.length > 19"
        size="small"
        @click="startEdit">
        <Icon icon="icon-jia" class="mr-1" />
        {{ t('functionCase.detail.assocTask.associate', {name: props.title}) }}
      </Button>
    </div>
    <Table
      :columns="columns"
      :dataSource="tableData || []"
      :pagination="false"
      size="small"
      noDataSize="small">
      <template #bodyCell="{column, record}">
        <template v-if="column.dataIndex === 'name'">
          <Button
            type="link"
            size="small"
            @click="openTask(record)">
            {{ record.name }}
          </Button>
        </template>

        <template v-if="column.dataIndex === 'progress'">
          <Progress
            :percent="+record?.progress?.completedRate"
            style="width: 80px;"
            class="mr-3.5" />
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

        <template v-if="column.dataIndex === 'taskType'">
          {{ record?.taskType?.message }}
        </template>

        <template v-if="column.dataIndex === 'priority'">
          <TaskPriority :value="record?.priority" />
        </template>

        <template v-if="column.dataIndex === 'status'">
          <TaskStatus :value="record?.status" />
        </template>
      </template>
    </Table>

    <AsyncComponent :visible="selectTaskVisible">
      <SelectTaskByModuleModal
        v-model:visible="selectTaskVisible"
        :title="t('functionCase.detail.assocTask.select', {name: props.title})"
        :action="`${TESTER}/func/case/${props.caseId}/task/notAssociated?taskType=${props.taskType}`"
        :projectId="props.projectId"
        @ok="handlePut" />
    </AsyncComponent>
  </div>
</template>
