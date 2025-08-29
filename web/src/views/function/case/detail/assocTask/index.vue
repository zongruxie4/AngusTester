<script lang="ts" setup>
import { computed, defineAsyncComponent, ref } from 'vue';
import { AsyncComponent, Hints, Icon, modal, Table, TaskPriority, TaskStatus } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { Button, Progress } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { funcCase } from '@/api/tester';
import { useI18n } from 'vue-i18n';

interface Props {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  caseId: string;
  dataSource: {
    id: string;
    name: string;
    taskType: {
      value: 'TASK' | 'API_TEST' | 'BUG' | 'SCENARIO_TEST' | 'STORY' | 'REQUIREMENT' | undefined;
      message: string;
    }
  }[];
  caseList:{ id:string; }[];
  title: string;
  taskType: 'TASK' | 'API_TEST' | 'BUG' | 'SCENARIO_TEST' | 'STORY' | 'REQUIREMENT';
  tips?: string;
}
const router = useRouter();
const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  caseId: undefined,
  dataSource: undefined,
  caseList: undefined,
  title: '任务',
  taskType: 'TASK',
  tip: ''
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'editSuccess'): void;
}>();

const SelectTaskByModuleModal = defineAsyncComponent(() => import('@/components/task/selectByModuleModal/index.vue'));

const submitLoading = ref(false);
const selectTaskVisible = ref(false);

const tableData = computed(() => {
  return (props.dataSource || []).filter(item => item.taskType.value === props.taskType);
});

const taskProgress = computed(() => {
  const completed = tableData.value.filter(item => item?.status?.value === 'COMPLETED').length;
  const total = tableData.value.filter(item => !['CANCELED'].includes(item?.testResult?.value)).length;
  const completedRate = total > 0 ? (completed / total * 100).toFixed(2) : 0;
  return {
    completed,
    completedRate,
    total
  };
});

// const editRef = ref(false);
const cancelEdit = () => {
  // editRef.value = false;
  selectTaskVisible.value = false;
  // refTaskIds.value = [];

  // refTaskIds.value = (props.dataSource || []).map(item => item.id);
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

// 编号、名称、类型、优先级、评估故事点、状态、经办人、截止时间、操作

const columns = [
  {
    key: 'code',
    dataIndex: 'code',
    title: t('functionCase.detail.assocTask.code')
  },
  {
    key: 'name',
    dataIndex: 'name',
    title: t('functionCase.detail.assocTask.name')
  },
  {
    key: 'progress',
    dataIndex: 'progress',
    title: t('functionCase.detail.assocTask.progress')
  },
  {
    key: 'taskType',
    dataIndex: 'taskType',
    title: t('functionCase.detail.assocTask.type')
  },
  {
    key: 'priority',
    dataIndex: 'priority',
    title: t('functionCase.detail.assocTask.priority')
  },
  {
    key: 'evalWorkload',
    dataIndex: 'evalWorkload',
    title: t('functionCase.detail.assocTask.evalStoryPoint')
  },
  {
    key: 'status',
    dataIndex: 'status',
    title: t('functionCase.detail.assocTask.status')
  },
  {
    key: 'assigneeName',
    dataIndex: 'assigneeName',
    title: t('functionCase.detail.assocTask.assignee')
  },
  {
    key: 'deadlineDate',
    dataIndex: 'deadlineDate',
    title: t('functionCase.detail.assocTask.deadline')
  },
  {
    key: 'action',
    dataIndex: 'action',
    title: t('functionCase.detail.assocTask.actions')
  }
];

</script>
<template>
  <div>
    <div class="flex mb-2 items-center pr-2">
      <!-- <div class="flex items-center flex-nowrap h-8 px-3.5 rounded" style="background-color:#FAFAFA;">
        <span class="flex-shrink-0 font-semibold text-theme-title">进度</span>
        <Colon class="mr-1.5" />
        <span class="font-semibold text-3.5" style="color: #07F;">{{ taskProgress?.completed || 0 }}</span>
        <span class="font-semibold text-3.5 mx-1">/</span>
        <span class="font-semibold text-3.5 mr-3.5">{{ taskProgress?.total || 0 }}</span>
        <Progress
          :percent="+taskProgress?.completedRate"
          style="width: 120px;"
          class="mr-3.5"
          :showInfo="false" />
        <span class="font-semibold text-3.5">{{ taskProgress?.completedRate || 0 }}%</span>
      </div> -->
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
            {{ t('functionCase.detail.assocTask.cancel') }}
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
