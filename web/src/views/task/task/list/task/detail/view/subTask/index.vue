<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref } from 'vue';
import { Button, Progress } from 'ant-design-vue';
import {
  AsyncComponent,
  Colon,
  Dropdown,
  Hints,
  Icon,
  IconTask,
  Input,
  modal,
  notification,
  TaskPriority,
  TaskStatus,
  Table
} from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { task } from 'src/api/tester';
import { useI18n } from 'vue-i18n';

import SelectEnum from '@/components/SelectEnum/index.vue';
import { TaskInfo } from '@/views/task/PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  taskInfo: TaskInfo;
  loading: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  taskInfo: undefined,
  loading: false
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'refreshChange'): void;
  (e: 'update:loading', value:boolean): void;
  (event: 'dataChange', value: Partial<TaskInfo>): void;
}>();

const EditTaskModal = defineAsyncComponent(() => import('@/views/task/task/list/task/edit/index.vue'));
// const refTasks = defineAsyncComponent(() => import('./refTasks/index.vue'));
const SelectTaskByModuleModal = defineAsyncComponent(() => import('@/components/task/selectByModuleModal/index.vue'));

const loading = ref(false);
const selectedTaskId = ref<string>();
const taskModalVisible = ref(false);
const refTaskModalVisible = ref(false);

const newTaskType = ref<TaskInfo['taskType']['value']>();
const newTaskPriority = ref<TaskInfo['priority']['value']>();
const newTaskName = ref<string>();

const addChildTask = () => {
  taskModalVisible.value = true;
};

const addChildTaskOk = () => {
  selectedTaskId.value = undefined;
  emit('refreshChange');
};

const refChildTaskOk = async (subTaskIds) => {
  const params = {
    subTaskIds: subTaskIds
  };
  loading.value = true;
  const [error] = await task.setSubTask(props.taskInfo?.id, params);
  loading.value = false;
  refTaskModalVisible.value = false;
  if (error) {
    return;
  }

  notification.success(t('task.subTask.messages.associateSubTaskSuccess'));
  emit('refreshChange');
};

const refChildTask = () => {
  refTaskModalVisible.value = true;
};

const pressEnter = (event: { target: { value: string } }) => {
  const value = event.target.value.trim();
  if (!value) {
    return;
  }

  toSave();
};

const toSave = async () => {
  const value = newTaskName.value?.trim();
  if (!value) {
    return;
  }

  const params = {
    projectId: props.projectId,
    name: value,
    priority: newTaskPriority.value,
    taskType: newTaskType.value,
    parentTaskId: taskId.value
  };
  if (newTaskType.value === 'BUG') {
    params.bugLevel = 'MINOR';
    params.missingBugFlag = false;
  }
  loading.value = true;
  const [error] = await task.addTask(params);
  loading.value = false;
  if (error) {
    return;
  }

  newTaskName.value = undefined;
  newTaskType.value = props.taskInfo?.taskType?.value;
  newTaskPriority.value = props.taskInfo?.priority?.value;
  emit('refreshChange');
};

const toDelete = (data:TaskInfo['subTaskInfos'][number]) => {
  modal.confirm({
    content: t('task.subTask.messages.confirmCancelSubTask', { name: data.name }),
    async onOk () {
      const params = {
        subTaskIds: [data.id]
      };
      emit('update:loading', true);
      const [error] = await task.cancelSubTask(props.taskInfo?.id, params);
      emit('update:loading', false);
      if (error) {
        return;
      }

      notification.success(t('task.subTask.messages.cancelSubTaskSuccess'));
      emit('refreshChange');
    }
  });
};

const toEdit = (data:TaskInfo['subTaskInfos'][number]) => {
  selectedTaskId.value = data.id;
  taskModalVisible.value = true;
};

const dropdownClick = (menuItem, data: TaskInfo) => {
  const key = menuItem.key;
  if (key === 'favourite') {
    toFavourite(data);
    return;
  }

  if (key === 'cancelFavourite') {
    toDeleteFavourite(data);
    return;
  }

  if (key === 'follow') {
    toFollow(data);
    return;
  }

  if (key === 'cancelFollow') {
    toDeleteFollow(data);
  }
};

const toFavourite = async (data: TaskInfo) => {
  const [error] = await task.favouriteTask(data.id);
  if (error) {
    return;
  }

  notification.success(t('task.subTask.messages.favouriteSuccess'));
  emit('refreshChange');
};

const toDeleteFavourite = async (data: TaskInfo) => {
  const [error] = await task.cancelFavouriteTask(data.id);
  if (error) {
    return;
  }

  notification.success(t('task.subTask.messages.cancelFavouriteSuccess'));
  emit('refreshChange');
};

const toFollow = async (data: TaskInfo) => {
  const [error] = await task.followTask(data.id);
  if (error) {
    return;
  }

  notification.success(t('task.subTask.messages.followSuccess'));
  emit('refreshChange');
};

const toDeleteFollow = async (data: TaskInfo) => {
  const [error] = await task.cancelFollowTask(data.id);
  if (error) {
    return;
  }

  notification.success(t('task.subTask.messages.cancelFollowSuccess'));
  emit('refreshChange');
};

onMounted(() => {
  newTaskType.value = ['API_TEST', 'SCENARIO_TEST'].includes(props.taskInfo?.taskType?.value) ? 'TASK' : props.taskInfo?.taskType?.value;
  newTaskPriority.value = props.taskInfo?.priority?.value;
});

const sprintId = computed(() => {
  return props.taskInfo?.sprintId;
});

const moduleId = computed(() => {
  return props.taskInfo?.moduleId;
});

const taskId = computed(() => {
  return props.taskInfo?.id;
});

const subTaskProgress = computed(() => {
  return props.taskInfo?.subTaskProgress || {
    completed: '0',
    completedRate: '0',
    total: '0'
  };
});

const subTaskInfos = computed(() => {
  return props.taskInfo?.subTaskInfos || [];
});

const menuItemsMap = computed(() => {
  const map = {};
  if (!subTaskInfos.value) {
    return map;
  }

  for (let i = 0, len = subTaskInfos.value.length; i < len; i++) {
    const { favouriteFlag, followFlag, id } = subTaskInfos.value[i];
    const items:any[] = [];
    if (favouriteFlag) {
      items.push({
        name: t('task.subTask.dropdown.cancelFavourite'),
        key: 'cancelFavourite',
        icon: 'icon-quxiaoshoucang',
        disabled: false,
        hide: false
      });
    } else {
      items.push({
        name: t('task.subTask.dropdown.favourite'),
        key: 'favourite',
        icon: 'icon-yishoucang',
        disabled: false,
        hide: false
      });
    }

    if (followFlag) {
      items.push({
        name: t('task.subTask.dropdown.cancelFollow'),
        key: 'cancelFollow',
        icon: 'icon-quxiaoguanzhu',
        disabled: false,
        hide: false
      });
    } else {
      items.push({
        name: t('task.subTask.dropdown.follow'),
        key: 'follow',
        icon: 'icon-yiguanzhu',
        disabled: false,
        hide: false
      });
    }

    map[id] = items;
  }

  return map;
});

const columns = [
  {
    key: 'code',
    dataIndex: 'code',
    title: t('task.subTask.columns.code')
  },
  {
    key: 'name',
    dataIndex: 'name',
    title: t('task.subTask.columns.name')
  },
  {
    key: 'progress',
    dataIndex: 'progress',
    title: t('task.subTask.columns.progress')
  },
  {
    key: 'taskType',
    dataIndex: 'taskType',
    title: t('task.subTask.columns.taskType')
  },
  {
    key: 'priority',
    dataIndex: 'priority',
    title: t('task.subTask.columns.priority'),
    groupName: 'task'
  },
  {
    key: 'evalWorkload',
    dataIndex: 'evalWorkload',
    title: props.taskInfo?.evalWorkloadMethod?.value === 'STORY_POINT' ? t('task.subTask.columns.evalWorkload') : t('task.subTask.columns.evalWorkloadHours'),
    groupName: 'task',
    hide: true
  },
  {
    key: 'status',
    dataIndex: 'status',
    title: t('task.subTask.columns.status')
  },
  {
    key: 'assigneeName',
    dataIndex: 'assigneeName',
    title: t('task.subTask.columns.assigneeName')
  },
  {
    key: 'deadlineDate',
    dataIndex: 'deadlineDate',
    title: t('task.subTask.columns.deadlineDate')
  },
  {
    key: 'action',
    dataIndex: 'action',
    title: t('task.subTask.columns.action')
  }
];

</script>

<template>
  <div class="h-full leading-5">
    <div class="flex items-center mb-2.5 pr-5">
      <div class="flex items-center flex-nowrap h-8 px-3.5 rounded" style="background-color:#FAFAFA;">
        <span class="flex-shrink-0 font-semibold text-theme-title">{{ t('task.subTask.progress') }}</span>
        <Colon class="mr-1.5" />
        <span class="font-semibold text-3.5" style="color: #07F;">{{ subTaskProgress?.completed || 0 }}</span>
        <span class="font-semibold text-3.5 mx-1">/</span>
        <span class="font-semibold text-3.5 mr-3.5">{{ subTaskProgress?.total || 0 }}</span>
        <Progress
          :percent="+subTaskProgress?.completedRate"
          style="width: 120px;"
          class="mr-3.5"
          :showInfo="false" />
        <span class="font-semibold text-3.5">{{ subTaskProgress?.completedRate || 0 }}%</span>
      </div>
      <Hints :text="t('task.subTask.description')" class="flex-1 min-w-0 truncate ml-1" />
      <div class="flex items-center space-x-2.5">
        <Button
          type="default"
          size="small"
          class="space-x-1"
          @click="addChildTask">
          <Icon icon="icon-jia" />
          <span>{{ t('task.subTask.actions.addSubTask') }}</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="space-x-1"
          @click="refChildTask">
          <Icon icon="icon-guanlianziyuan" />
          <span>{{ t('task.subTask.actions.associateSubTask') }}</span>
        </Button>
      </div>
    </div>

    <Table
      class="sub_task_table"
      :columns="columns"
      :pagination="false"
      :dataSource="subTaskInfos"
      noDataSize="small"
      noDataText="">
      <template #bodyCell="{record, column}">
        <template v-if="column.dataIndex === 'name'">
          <RouterLink
            target="_self"
            :title="record.name"
            :to="`/task#task?projectId=${props.projectId}&taskId=${record.id}&total=1`"
            style="color:#40a9ff">
            {{ record.name || '--' }}
          </RouterLink>
        </template>
        <template v-if="column.dataIndex === 'progress'">
          <div style="width: 120px;" class="flex items-center space-x-1">
            <span>{{ `${record?.progress?.completed || 0} / ${record?.progress?.total || 0}` }}</span>
            <Progress
              :percent="+record?.progress?.completedRate"
              style="width: 80px;"
              class="mr-3.5" />
          </div>
        </template>
        <template v-if="column.dataIndex === 'taskType'">
          <div style="width:80px;" class="flex items-center">
            <IconTask :value="record.taskType?.value" class="text-4 flex-shrink-0" />
            <span class="ml-1">{{ record.taskType?.message }}</span>
          </div>
        </template>
        <template v-if="column.dataIndex === 'priority'">
          <TaskPriority :value="record.priority" />
        </template>
        <template v-if="column.dataIndex === 'status'">
          <TaskStatus :value="record.priority" />
        </template>
        <template v-if="column.dataIndex === 'action'">
          <div style="width: 140px;padding: 0;" class="tbody-cell flex items-center space-x-2.5">
            <Button
              type="text"
              size="small"
              class="flex items-center px-0"
              @click="toDelete(record)">
              <Icon icon="icon-qingchu" class="text-3.5 mr-1" />
              <span>{{ t('task.subTask.actions.cancel') }}</span>
            </Button>

            <Button
              type="text"
              size="small"
              class="flex items-center px-0"
              @click="toEdit(record)">
              <Icon icon="icon-shuxie" class="text-3.5 mr-1" />
              <span>{{ t('task.subTask.actions.edit') }}</span>
            </Button>

            <Dropdown :menuItems="menuItemsMap[record.id]" @click="dropdownClick($event,record)">
              <Button
                type="text"
                size="small"
                class="flex items-center px-0">
                <Icon icon="icon-gengduo" />
              </Button>
            </Dropdown>
          </div>
        </template>
      </template>
    </Table>

    <div class="flex items-center pt-2">
      <SelectEnum
        v-model:value="newTaskType"
        :excludes="({value}) => ['API_TEST', 'SCENARIO_TEST'].includes(value)"
        enumKey="TaskType"
        :placeholder="t('task.subTask.form.taskType')"
        class="w-28 mr-2">
        <template #option="record">
          <div class="flex items-center">
            <IconTask :value="record.value" class="text-4 flex-shrink-0" />
            <span class="ml-1">{{ record.message }}</span>
          </div>
        </template>
      </SelectEnum>

      <SelectEnum
        v-model:value="newTaskPriority"
        internal
        enumKey="Priority"
        :placeholder="t('task.subTask.form.priority')"
        class="w-28 mr-2">
        <template #option="record">
          <TaskPriority :value="record" />
        </template>
      </SelectEnum>

      <Input
        ref="taskNameInputRef"
        v-model:value="newTaskName"
        :maxlength="200"
        :placeholder="t('task.subTask.form.taskName')"
        trim
        class="w-200 mr-5"
        @pressEnter="pressEnter" />

      <div class="flex items-center space-x-2.5">
        <Button
          :disabled="!newTaskName"
          type="primary"
          size="small"
          @click="toSave">
          {{ t('task.subTask.actions.add') }}
        </Button>
      </div>
    </div>

    <AsyncComponent :visible="taskModalVisible">
      <EditTaskModal
        v-model:visible="taskModalVisible"
        :sprintId="sprintId"
        :taskId="selectedTaskId"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :moduleId="moduleId === '-1' ? undefined : moduleId"
        :parentTaskId="taskId"
        @ok="addChildTaskOk" />
    </AsyncComponent>

    <AsyncComponent :visible="refTaskModalVisible">
      <!-- <refTasks
        v-model:visible="refTaskModalVisible"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :taskInfo="props.taskInfo"
        @ok="refChildTaskOk" /> -->
      <SelectTaskByModuleModal
        v-model:visible="refTaskModalVisible"
        :projectId="props.projectId"
        :action="`${TESTER}/task/${props.taskInfo?.id}/subtask/notAssociated`"
        @ok="refChildTaskOk" />
    </AsyncComponent>
  </div>
</template>

<style scoped>
.head-container {
  background-color: rgb(246, 248, 251);
  line-height: 36px;
}

.head-item-container {
  display: flex;
  align-items: center;
  padding: 0 8px;
}

.head-item-container:last-child {
  border: 0;
}

.tbody-cell {
  padding: 0 8px;
}

.row-container {
  padding-top: 4px;
  padding-bottom: 4px;
  border-bottom: 1px solid var(--border-divider);
}

.row-container:hover {
 background-color: var(--content-tabs-bg-hover);
}

:deep(.sub_task_table) .ant-table-placeholder {
  @apply hidden;
}
</style>
