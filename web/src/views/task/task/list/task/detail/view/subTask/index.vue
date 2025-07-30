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
  SelectEnum,
  TaskPriority,
  TaskStatus,
  Table
} from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { task } from 'src/api/tester';

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

  notification.success('关联子任务成功');
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
    content: `确定取消子任务【${data.name}】吗？`,
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

      notification.success('取消子任务成功');
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

  notification.success('任务收藏成功');
  emit('refreshChange');
};

const toDeleteFavourite = async (data: TaskInfo) => {
  const [error] = await task.cancelFavouriteTask(data.id);
  if (error) {
    return;
  }

  notification.success('任务取消收藏成功');
  emit('refreshChange');
};

const toFollow = async (data: TaskInfo) => {
  const [error] = await task.followTask(data.id);
  if (error) {
    return;
  }

  notification.success('任务关注成功');
  emit('refreshChange');
};

const toDeleteFollow = async (data: TaskInfo) => {
  const [error] = await task.cancelFollowTask(data.id);
  if (error) {
    return;
  }

  notification.success('任务取消关注成功');
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
        name: '取消收藏',
        key: 'cancelFavourite',
        icon: 'icon-quxiaoshoucang',
        disabled: false,
        hide: false
      });
    } else {
      items.push({
        name: '收藏',
        key: 'favourite',
        icon: 'icon-yishoucang',
        disabled: false,
        hide: false
      });
    }

    if (followFlag) {
      items.push({
        name: '取消关注',
        key: 'cancelFollow',
        icon: 'icon-quxiaoguanzhu',
        disabled: false,
        hide: false
      });
    } else {
      items.push({
        name: '关注',
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
    dataIndex: 'code',
    title: '编号'
  },
  {
    dataIndex: 'name',
    title: '名称'
  },
  {
    dataIndex: 'progress',
    title: '进度'
  },
  {
    dataIndex: 'taskType',
    title: '类型'
  },
  {
    dataIndex: 'priority',
    title: '优先级',
    groupName: 'task'
  },
  {
    dataIndex: 'evalWorkload',
    title: props.taskInfo?.evalWorkloadMethod?.value === 'STORY_POINT' ? '评估故事点' : '评估工时',
    groupName: 'task',
    hide: true
  },
  {
    dataIndex: 'status',
    title: '状态'
  },
  {
    dataIndex: 'assigneeName',
    title: '经办人'
  },
  {
    dataIndex: 'deadlineDate',
    title: '截止时间'
  },
  {
    dataIndex: 'action',
    title: '操作'
  }
];

</script>

<template>
  <div class="h-full leading-5">
    <div class="flex items-center mb-2.5 pr-5">
      <div class="flex items-center flex-nowrap h-8 px-3.5 rounded" style="background-color:#FAFAFA;">
        <span class="flex-shrink-0 font-semibold text-theme-title">进度</span>
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
      <Hints text="用于将大型复杂任务分解成更小、更易管理的子任务，便于团队成员分工协作。" class="flex-1 min-w-0 truncate ml-1" />
      <div class="flex items-center space-x-2.5">
        <Button
          type="default"
          size="small"
          class="space-x-1"
          @click="addChildTask">
          <Icon icon="icon-jia" />
          <span>添加子任务</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="space-x-1"
          @click="refChildTask">
          <Icon icon="icon-guanlianziyuan" />
          <span>关联子任务</span>
        </Button>
      </div>
    </div>

    <Table
      class="sub_task_table"
      :columns="columns"
      :pagination="false"
      :dataSource="subTaskInfos">
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
              <span>取消</span>
            </Button>

            <Button
              type="text"
              size="small"
              class="flex items-center px-0"
              @click="toEdit(record)">
              <Icon icon="icon-shuxie" class="text-3.5 mr-1" />
              <span>编辑</span>
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
        placeholder="任务类型"
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
        placeholder="优先级"
        class="w-28 mr-2">
        <template #option="record">
          <TaskPriority :value="record" />
        </template>
      </SelectEnum>

      <Input
        ref="taskNameInputRef"
        v-model:value="newTaskName"
        :maxlength="200"
        placeholder="输入任务名称，最大支持200字符，按回车键可自动保存"
        trim
        class="w-200 mr-5"
        @pressEnter="pressEnter" />

      <div class="flex items-center space-x-2.5">
        <Button
          :disabled="!newTaskName"
          type="primary"
          size="small"
          @click="toSave">
          添加
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
