<script lang="ts" setup>
import { computed, defineAsyncComponent, ref } from 'vue';
import { AsyncComponent, Hints, Icon, modal, Table, TaskPriority, TaskStatus } from '@xcan-angus/vue-ui';
import { TESTER, EnumMessage } from '@xcan-angus/infra';
import { Button, Progress } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { TaskType } from '@/enums/enums';
import { task } from '@/api/tester';

interface Props {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  taskId: string;
  dataSource: {
    id: string;
    name: string;
    taskType: EnumMessage<TaskType>
  }[];
  caseList:{ id:string; }[];
  title: string;
  taskType: TaskType;
  tips?: string;
}
const router = useRouter();
const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  taskId: undefined,
  dataSource: undefined,
  caseList: undefined,
  title: 'Task',
  taskType: TaskType.TASK,
  tips: ''
});

const SelectTaskByModuleModal = defineAsyncComponent(() => import('@/components/task/SelectByModuleModal.vue'));

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'editSuccess'): void;
}>();
const submitLoading = ref(false);

const tableData = computed(() => {
  return (props.dataSource || []).filter(item => item.taskType.value === props.taskType);
});

const selectTaskVisible = ref(false);
const cancelEdit = () => {
  selectTaskVisible.value = false;
};
const startEdit = () => {
  selectTaskVisible.value = true;
};
const handlePut = async (refTaskIds) => {
  selectTaskVisible.value = false;
  if (!refTaskIds.length) {
    cancelEdit();
    return;
  }
  submitLoading.value = true;
  const [error] = await task.associationTask(props.taskId, refTaskIds, {
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
    content: t('task.assocTask.messages.confirmCancelTask', { name: record.name }),
    onOk () {
      return task.cancelAssociationTask(props.taskId, [record.id], {
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

const openTask = (record) => {
  router.push(`/task#task?taskId=${record.id}`);
};

const columns = [
  {
    key: 'code',
    dataIndex: 'code',
    title: t('task.assocTask.columns.code')
  },
  {
    key: 'name',
    dataIndex: 'name',
    title: t('task.assocTask.columns.name')
  },
  {
    key: 'progress',
    dataIndex: 'progress',
    title: t('task.assocTask.columns.progress')
  },
  {
    key: 'taskType',
    dataIndex: 'taskType',
    title: t('task.assocTask.columns.taskType')
  },
  {
    key: 'priority',
    dataIndex: 'priority',
    title: t('task.assocTask.columns.priority')
  },
  {
    key: 'evalWorkload',
    dataIndex: 'evalWorkload',
    title: t('task.assocTask.columns.evalWorkload')
  },
  {
    key: 'status',
    dataIndex: 'status',
    title: t('task.assocTask.columns.status')
  },
  {
    key: 'assigneeName',
    dataIndex: 'assigneeName',
    title: t('task.assocTask.columns.assigneeName')
  },
  {
    key: 'deadlineDate',
    dataIndex: 'deadlineDate',
    title: t('task.assocTask.columns.deadlineDate')
  },
  {
    key: 'action',
    dataIndex: 'action',
    title: t('task.assocTask.columns.action')
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
        {{ t('task.assocTask.actions.associateTask', { title: props.title }) }}
      </Button>
    </div>
    <Table
      :columns="columns"
      :dataSource="tableData || []"
      :pagination="false"
      size="small"
      noDataSize="small"
      noDataText="">
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
        :title="t('task.assocTask.modal.selectTask', { title: props.title })"
        :projectId="props.projectId"
        :action="`${TESTER}/task/${props.taskId}/task/notAssociated?taskType=${props.taskType}`"
        @ok="handlePut" />
    </AsyncComponent>
  </div>
</template>
