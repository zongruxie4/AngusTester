<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, onMounted, ref, watch, Ref } from 'vue';
import { Button } from 'ant-design-vue';
import Gantt from '@xcan-angus/frappe-gantt';
import dayjs from 'dayjs';
import { AsyncComponent, Icon } from '@xcan-angus/vue-ui';
import { task } from 'src/api/tester';

import { TaskInfo } from '../../../../PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  filters: { key: string; op: string; value: boolean | string | string[]; }[];
  notify: string;
  moduleId: string;
  loading: boolean;
};

const props = withDefaults(defineProps<Props>(), {
  filters: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  moduleId: undefined,
  loading: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'update:loading', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
  (event: 'refreshChange'): void;
}>();

const APIInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/kanban/view/info/apis/index.vue'));
const BasicInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/kanban/view/info/basic/index.vue'));
const ScenarioInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/kanban/view/info/scenario/index.vue'));
const PersonnelInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/kanban/view/info/personnel/index.vue'));
const DateInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/kanban/view/info/date/index.vue'));
const Comment = defineAsyncComponent(() => import('@/views/task/task/list/task/kanban/view/comment/index.vue'));
const Activity = defineAsyncComponent(() => import('@/views/task/task/list/task/kanban/view/activity/index.vue'));
const AssocTasks = defineAsyncComponent(() => import('@/views/task/task/list/task/kanban/view/assocTask/index.vue'));
const AssocCases = defineAsyncComponent(() => import('@/views/task/task/list/task/kanban/view/assocCase/index.vue'));
const AttachmentInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/kanban/view/info/attachment/index.vue'));
const Remarks = defineAsyncComponent(() => import('@/views/task/task/list/task/kanban/view/remark/index.vue'));

const getParams = () => {
  const params: {
    backlog: false,
    projectId: string;
    pageNo: number;
    pageSize: number;
    moduleId?: string;
    filters?: { key: string; op: string; value: boolean | string | string[]; }[];
  } = {
    backlog: false,
    projectId: props.projectId,
    pageNo: 1,
    pageSize: 500
  };

  if (props.filters?.length) {
    params.filters = props.filters;
  }

  if (props.moduleId) {
    params.moduleId = props.moduleId;
  }

  return params;
};

const taskList = ref<TaskInfo[]>([]);
const ganttRef = ref();

const resetData = () => {
  taskList.value = [];
  drawerClose();
};

const ganttView = ref();

const loadData = async () => {
  const params = getParams();
  emit('update:loading', true);
  const [error, res] = await task.loadTaskList(params);
  if (error) {
    resetData();
    emit('update:loading', false);
    return;
  }
  resetData();

  const { list, total } = (res?.data || { total: 0, list: [] });
  const len = list.length;
  const _taskList: TaskInfo[] = [];
  _taskList.push(...list);
  if (len < +total) {
    const pages = Math.ceil((total - len) / params.pageSize);
    for (let i = 0, len = pages; i < len; i++) {
      const pageNo = i + 2;
      const _params = { ...params, pageNo };
      const [_error, _res] = await task.loadTaskList(_params);
      if (_error) {
        emit('update:loading', false);
        return;
      }

      const { list: _list } = (_res?.data || { total: 0, list: [] });
      _taskList.push(..._list);
    }
  }
  emit('update:loading', false);

  // 保存原始数据
  taskList.value = _taskList.map(i => {
    return {
      ...i,
      start: i.startDate || i.createdDate,
      end: i.completedDate || dayjs().format('YYYY-MM-DD HH:mm:ss'),
      progress: i.completedDate ? 100 : (+i.progress?.completedRate || 0),
      description: '',
      dependencies: i.parentTaskId ? [i.parentTaskId] : []
    };
  });

  if (!ganttView.value) {
    ganttView.value = new Gantt(ganttRef.value, taskList.value, {
      language: 'zh',
      view_mode: 'Day',
      view_mode_select: true,
      on_click: (task) => {
        checkedTaskInfo.value = _taskList.find(i => i.id === task.id);
      }
    });
  } else {
    ganttView.value.refresh(taskList.value);
  }
};

const checkedTaskInfo = ref<TaskInfo>();
const checkedSprintInfo = ref<{ id: string; name: string; }>();
const drawerActiveKey = ref<'basic' | 'person' | 'date' | 'comment' | 'activity' | 'tasks' | 'cases' | 'attachments' | 'remarks'>('basic');

const drawerActiveKeyChange = (key: 'basic' | 'person' | 'date' | 'comment' | 'activity' | 'tasks' | 'cases' | 'attachments' | 'remarks') => {
  drawerActiveKey.value = key;
};

const drawerClose = () => {
  checkedTaskInfo.value = undefined;
  checkedSprintInfo.value = undefined;
};
const checkedTaskId = computed(() => {
  return checkedTaskInfo?.value?.id;
});
const checkedTaskType = computed(() => {
  return checkedTaskInfo?.value?.taskType?.value;
});

const loadTaskInfoById = async (id: string): Promise<Partial<TaskInfo>> => {
  emit('update:loading', true);
  const [error, res] = await task.loadTaskInfo(id);
  emit('update:loading', false);
  if (error || !res?.data) {
    return { id };
  }
  return { ...res.data };
};

const taskInfoChange = async (data: Partial<TaskInfo>) => {
  const id = data.id;
  if (!id) {
    return;
  }

  const _taskInfo = await loadTaskInfoById(id);
  if (checkedTaskInfo.value) {
    checkedTaskInfo.value = { ...checkedTaskInfo.value, ..._taskInfo };
  }

  const list = taskList.value;
  for (let i = 0, len = list.length; i < len; i++) {
    if (list[i].id === id) {
      list[i] = _taskInfo;
      break;
    }
  }
};

const loadingChange = (value: boolean) => {
  emit('update:loading', value);
};

onMounted(() => {
  watch(() => props.projectId, (newValue) => {
    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: true });

  watch([() => props.filters, () => props.moduleId], () => {
    loadData();
  });
});

</script>
<template>
  <div class="flex border-t flex-1 h-150">
    <div ref="ganttRef" class="flex-1 min-w-0 h-full overflow-auto"></div>
    <div class="drawer-container flex items-start" :class="{ 'drawer-open': !!checkedTaskId }">
      <div class="flex-shrink-0 h-full w-9 space-y-1 overflow-y-auto scroll-smooth drawer-action-container">
        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'basic' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          title="基本信息"
          @click="drawerActiveKeyChange('basic')">
          <Icon icon="icon-wendangxinxi" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'person' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          title="人员"
          @click="drawerActiveKeyChange('person')">
          <Icon icon="icon-quanburenyuan" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'date' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          title="日期"
          @click="drawerActiveKeyChange('date')">
          <Icon icon="icon-riqi" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'tasks' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          title="关联任务"
          @click="drawerActiveKeyChange('tasks')">
          <Icon icon="icon-ceshirenwu" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'cases' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          title="关联用例"
          @click="drawerActiveKeyChange('cases')">
          <Icon icon="icon-ceshiyongli1" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'attachments' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          title="附件"
          @click="drawerActiveKeyChange('attachments')">
          <Icon icon="icon-lianjie1" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'remarks' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          title="备注"
          @click="drawerActiveKeyChange('remarks')">
          <Icon icon="icon-shuxie" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'comment' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          title="评论"
          @click="drawerActiveKeyChange('comment')">
          <Icon icon="icon-pinglun" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'activity' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          title="活动"
          @click="drawerActiveKeyChange('activity')">
          <Icon icon="icon-chakanhuodong" class="text-4" />
        </div>
      </div>

      <div class="w-full h-full flex-1 overflow-hidden">
        <div class="flex items-center justify-between mt-4 pl-5 space-x-2.5">
          <div class="flex-1 flex items-center truncate">
            <RouterLink
              :to="`/task#sprint?id=${checkedSprintInfo?.id}`"
              :title="checkedSprintInfo?.name"
              class="truncate"
              style="max-width: 50%;">
              {{ checkedSprintInfo?.name }}
            </RouterLink>
            <div class="mx-1.5">/</div>
            <RouterLink
              :to="`/task#task?id=${checkedTaskInfo?.id}`"
              class="truncate flex-1"
              :title="checkedTaskInfo?.name">
              {{ checkedTaskInfo?.name }}
            </RouterLink>
          </div>
          <Button
            type="default"
            size="small"
            class="p-0 h-3.5 leading-3.5 border-none"
            @click="drawerClose">
            <Icon icon="icon-shanchuguanbi" class="text-3.5 cursor-pointer" />
          </Button>
        </div>

        <div style="height: calc(100% - 36px);" class="pt-3.5 overflow-hidden">
          <AsyncComponent :visible="!!checkedTaskId">
            <APIInfo
              v-if="checkedTaskType === 'API_TEST'"
              v-show="drawerActiveKey === 'basic'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedTaskInfo"
              @change="taskInfoChange"
              @loadingChange="loadingChange" />

            <ScenarioInfo
              v-else-if="checkedTaskType === 'SCENARIO_TEST'"
              v-show="drawerActiveKey === 'basic'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedTaskInfo"
              @change="taskInfoChange"
              @loadingChange="loadingChange" />

            <BasicInfo
              v-else
              v-show="drawerActiveKey === 'basic'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedTaskInfo"
              @change="taskInfoChange"
              @loadingChange="loadingChange" />

            <PersonnelInfo
              v-show="drawerActiveKey === 'person'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedTaskInfo"
              @change="taskInfoChange"
              @loadingChange="loadingChange" />

            <DateInfo
              v-show="drawerActiveKey === 'date'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedTaskInfo"
              @change="taskInfoChange"
              @loadingChange="loadingChange" />

            <AssocTasks
              v-show="drawerActiveKey === 'tasks'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedTaskInfo"
              @change="taskInfoChange"
              @loadingChange="loadingChange" />

            <AssocCases
              v-show="drawerActiveKey === 'cases'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedTaskInfo"
              @change="taskInfoChange"
              @loadingChange="loadingChange" />

            <AttachmentInfo
              v-show="drawerActiveKey === 'attachments'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedTaskInfo"
              @change="taskInfoChange"
              @loadingChange="loadingChange" />

            <Comment
              v-show="drawerActiveKey === 'comment'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedTaskInfo"
              @change="taskInfoChange"
              @loadingChange="loadingChange" />

            <Activity
              v-show="drawerActiveKey === 'activity'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedTaskInfo"
              @change="taskInfoChange"
              @loadingChange="loadingChange" />

            <Remarks
              v-show="drawerActiveKey === 'remarks'"
              :id="checkedTaskInfo?.id"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo" />
          </AsyncComponent>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>

.drawer-container {
  width: 0;
  overflow: hidden;
  transition: all 150ms linear 0ms;
  border-left: 1px solid transparent;
  opacity: 0;
}

.drawer-container.drawer-open {
  width: 400px;
  height: 100%;
  border-left: 1px solid var(--border-text-box);
  opacity: 1;
}

.drawer-action-container {
  background-color: rgba(247, 248, 251, 100%);
  color: var(--content-text-sub-content);
}

.action-item:hover,
.action-item.drawer-active-item {
  background-color: rgba(239, 240, 243, 100%);
}

:deep(.ant-input-affix-wrapper-sm) {
  background-color: #fff;
}
</style>
