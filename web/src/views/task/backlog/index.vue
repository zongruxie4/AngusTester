<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Progress } from 'ant-design-vue';
import {
  Arrow,
  AsyncComponent,
  Colon,
  DropdownSort,
  Icon,
  IconTask,
  Image,
  Input,
  modal,
  notification,
  Popover,
  Spin,
  TaskPriority,
  Tooltip
} from '@xcan-angus/vue-ui';
import {appContext, duration} from '@xcan-angus/infra';
import Draggable from 'vuedraggable';
import { cloneDeep } from 'lodash-es';
import { debounce } from 'throttle-debounce';
import dayjs, { Dayjs } from 'dayjs';
import { task, analysis } from '@/api/tester';
import { DATE_TIME_FORMAT } from '@/utils/constant';

import SelectEnum from '@/components/enum/SelectEnum.vue';
import { MemberCount, SprintInfo } from './types';
import { TaskInfo } from '../types';

type SprintPermissionKey = 'MODIFY_SPRINT' | 'DELETE_SPRINT' | 'ADD_TASK' | 'MODIFY_TASK' | 'DELETE_TASK' | 'EXPORT_TASK' | 'RESTART_TASK' | 'REOPEN_TASK' | 'GRANT'

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

const { t } = useI18n();
const aiEnabled = inject('aiEnabled', ref(false));
const isAdmin = computed(() => appContext.isAdmin());

const Introduce = defineAsyncComponent(() => import('@/views/task/backlog/Introduce.vue'));
const ApiInfo = defineAsyncComponent(() => import('@/views/task/backlog/info/Apis.vue'));
const BasicInfo = defineAsyncComponent(() => import('@/views/task/backlog/info/Basic.vue'));
const ScenarioInfo = defineAsyncComponent(() => import('@/views/task/backlog/info/Scenario.vue'));
const PersonnelInfo = defineAsyncComponent(() => import('@/views/task/backlog/info/Personnel.vue'));
const DateInfo = defineAsyncComponent(() => import('@/views/task/backlog/info/Date.vue'));
const Comment = defineAsyncComponent(() => import('@/views/task/backlog/Comment.vue'));
const Activity = defineAsyncComponent(() => import('@/views/task/backlog/Activity.vue'));
const RefTasks = defineAsyncComponent(() => import('@/views/task/backlog/AssocTask.vue'));
const RefCases = defineAsyncComponent(() => import('@/views/task/backlog/AssocCase.vue'));
const AttachmentInfo = defineAsyncComponent(() => import('@/views/task/backlog/info/Attachment.vue'));
const Remarks = defineAsyncComponent(() => import('@/views/task/backlog/Remark.vue'));
const EditTaskModal = defineAsyncComponent(() => import('@/views/task/backlog/Edit.vue'));
const SplitTask = defineAsyncComponent(() => import('@/views/task/backlog/SplitTask.vue'));
const AIGenerateTask = defineAsyncComponent(() => import('@/views/task/backlog/AiGenerateTask.vue'));

const deleteTabPane = inject<(value: string[]) => void>('deleteTabPane');

const sortOption = [
  {
    name: t('backlog.columns.type'),
    key: 'taskType'
  },
  {
    name: t('backlog.columns.priority'),
    key: 'priority'
  },
  {
    name: t('backlog.columns.code'),
    key: 'code'
  },
  {
    name: t('backlog.columns.name'),
    key: 'name'
  },
  {
    name: t('backlog.columns.assignee'),
    key: 'assigneeId'
  }
];

const backlogSortOption = sortOption.filter(i => !['assigneeId'].includes(i.key));

const PAGE_SIZE = 500;

const sprintRef = ref();
const taskNameInputRef = ref();

const loading = ref(false);

const searchValue = ref<string>();
const createdBy = ref<string>();
const assigneeId = ref<string>();
const quickDate = ref<'1' | '3' | '7'>();

const sprintInfo = ref<SprintInfo>();
const taskInfo = ref<TaskInfo>();
const openSet = ref<Set<string>>(new Set());

const sprintList = ref<SprintInfo[]>([]);
const membersMap = ref<{ [key: string]: SprintInfo['members'] }>({});
const membersCountMap = ref<{
  [key: string]: { [key: string]: MemberCount; }
}>({});
const taskListMap = ref<{ [key: string]: TaskInfo[] }>({});
const sortParamMap = ref<{[key: string]: {orderSort?: 'ASC'|'DESC', orderBy?: string}}>({});
const taskInfoLoadingSet = ref<Set<string>>(new Set());
const membersCountLoadingSet = ref<Set<string>>(new Set());
const sprintPermissionsMap = ref<Map<string, SprintPermissionKey[]>>(new Map());

const backlogList = ref<TaskInfo[]>([]);
const backlogLoaded = ref(false);
const backlogSort = ref<{ orderSort?: 'ASC'|'DESC', orderBy?: string }>();

const backlogTotal = ref(0);
const taskTotalMap = ref<{ [key: string]: number }>({});

const clickTimeout = ref();
const editNameSet = ref<Set<string>>(new Set());
const editNameMap = ref<{[key:string]:string}>({});

const selectedSprintId = ref<string>();
const selectedTaskId = ref<string>();
const selectedTaskInfo = ref<TaskInfo>();
const editTaskModalVisible = ref(false);
const splitTaskVisible = ref(false);
const aiGenerateTaskVisible = ref(false);
const addFlag = ref(false);

const newTaskType = ref<string>('STORY');
const newTaskName = ref<string>();
const newTaskPriority = ref<'HIGH' | 'HIGHEST' | 'LOW' | 'LOWEST' | 'MEDIUM'>('MEDIUM');

const popoverId = ref<string>();
const drawerActiveKey = ref<'basic' | 'person' | 'date' | 'comment' | 'activity' | 'tasks' | 'cases' | 'attachments' | 'remarks'>('basic');

const drawerActiveKeyChange = (key: 'basic' | 'person' | 'date' | 'comment' | 'activity' | 'tasks' | 'cases' | 'attachments' | 'remarks') => {
  drawerActiveKey.value = key;
};

const loadingChange = (value: boolean) => {
  loading.value = value;
};

const visibleChange = (visible: boolean, id: string) => {
  if (visible) {
    popoverId.value = id;
    return;
  }

  popoverId.value = undefined;
};

const taskRefresh = () => {
  loadSprintList();
  loadBacklogList(1);
};

const taskInfoChange = async (data: Partial<TaskInfo>) => {
  if (taskInfo.value) {
    taskInfo.value = { ...taskInfo.value, ...data };
  }
};

const searchChange = debounce(duration.search, () => {
  toQuery();
});

const toQuery = () => {
  const list = sprintList.value;
  for (let i = 0, len = list.length; i < len; i++) {
    loadTaskListById(list[i].id, 1);
  }

  loadBacklogList(1);
};

const queryAll = () => {
  assigneeId.value = undefined;
  createdBy.value = undefined;
  quickDate.value = undefined;

  toQuery();
};

const queryByMe = () => {
  if (!assigneeId.value) {
    assigneeId.value = userId.value;
  } else {
    assigneeId.value = undefined;
  }

  toQuery();
};

const queryByCreatedMe = () => {
  if (!createdBy.value) {
    createdBy.value = userId.value;
  } else {
    createdBy.value = undefined;
  }

  toQuery();
};

const queryByDate = (key: '1' | '3' | '7') => {
  if (quickDate.value === key) {
    quickDate.value = undefined;
  } else {
    quickDate.value = key;
  }

  toQuery();
};

const drawerClose = () => {
  sprintInfo.value = undefined;
  taskInfo.value = undefined;
};

const mouseenter = async (id: string) => {
  if (membersCountMap.value[id] || membersCountLoadingSet.value.has(id)) {
    return;
  }

  const params = {
    sprintId: id,
    projectId: props.projectId
  };
  membersCountLoadingSet.value.add(id);
  const [error, res] = await analysis.getAssigneeProgress(params);
  membersCountLoadingSet.value.delete(id);
  if (error) {
    return;
  }

  const list = res?.data || [];
  membersCountMap.value[id] = list.reduce((prev, cur) => {
    prev[cur.assigneeId] = cur;
    return prev;
  }, {} as MemberCount);
};

const toAddTask = () => {
  addFlag.value = true;

  nextTick(() => {
    setTimeout(() => {
      taskNameInputRef.value.focus();
    }, 100);
  });
};

const handleSortTask = (orderData, sprintId) => {
  sortParamMap.value[sprintId] = orderData;
  if (orderData.orderBy === 'priority') {
    sortTaskByPriority(sprintId);
    return;
  }
  loadTaskListById(sprintId, 1);
};

const handleSortBasklogTask = (orderData) => {
  backlogSort.value = orderData;
  if (orderData.orderBy === 'priority') {
    sortBacklogByPriority();
    return;
  }
  loadBacklogList(1);
};

const aiGenerateTask = () => {
  aiGenerateTaskVisible.value = true;
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
    taskType: newTaskType.value
  };
  loading.value = true;
  const [error, res] = await task.addTask(params);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('backlog.main.messages.saveSuccess'));

  newTaskName.value = undefined;
  newTaskPriority.value = 'MEDIUM';

  const id = res?.data?.id;
  if (!id) {
    return;
  }

  const dataInfo = await loadTaskInfoById(id);
  if (!dataInfo) {
    return;
  }

  backlogList.value.push(dataInfo);
  backlogTotal.value += 1;

  nextTick(() => {
    setTimeout(() => {
      if (sprintRef.value) {
        sprintRef.value.scrollTop = sprintRef.value.scrollHeight;
      }
    }, 16.66);
  });
};

const cancelAdd = () => {
  addFlag.value = false;
  newTaskType.value = 'STORY';
  newTaskName.value = undefined;
  newTaskPriority.value = 'MEDIUM';
};

const openCreateModal = () => {
  editTaskModalVisible.value = true;
  selectedSprintId.value = undefined;
  selectedTaskId.value = undefined;
};

const toAssignment = async (toId: string, taskData: TaskInfo, index: number) => {
  if (loading.value) {
    return;
  }

  const taskId = taskData.id;
  const hasFlag = taskListMap.value[toId].find((item) => item.id === taskId);
  if (hasFlag) {
    return;
  }

  const params = {
    targetSprintId: toId,
    taskIds: [taskId]
  };
  loading.value = true;
  await task.moveTask(params);
  loading.value = false;

  taskListMap.value[toId].push(taskData);
  taskTotalMap.value[toId] += 1;

  backlogList.value.splice(index, 1);
  backlogTotal.value -= 1;
};

const toMove = async (fromId: string, toId: string, taskData: TaskInfo, index: number) => {
  if (loading.value) {
    return;
  }

  const taskId = taskData.id;
  const hasFlag = taskListMap.value[toId].find((item) => item.id === taskId);
  if (hasFlag) {
    return;
  }

  const params = {
    targetSprintId: toId,
    taskIds: [taskId]
  };
  loading.value = true;
  await task.moveTask(params);
  loading.value = false;

  taskListMap.value[toId].push(taskData);
  taskTotalMap.value[toId] += 1;

  taskListMap.value[fromId].splice(index, 1);
  taskTotalMap.value[fromId] -= 1;
};

const moveToBacklog = async (fromId: string, taskData: TaskInfo, index: number) => {
  if (loading.value) {
    return;
  }

  const taskId = taskData.id;
  const hasFlag = backlogList.value.find((item) => item.id === taskId);
  if (hasFlag) {
    return;
  }

  const params = {
    targetSprintId: null,
    taskIds: [taskId]
  };
  loading.value = true;
  await task.moveTask(params);
  loading.value = false;

  backlogTotal.value += 1;
  backlogList.value.push(taskData);
  taskTotalMap.value[fromId] -= 1;
  taskListMap.value[fromId].splice(index, 1);
};

const dragAdd = async (event: { item: { id: string; }; from: { id: string; } }, sprintId: string) => {
  const taskId = event.item.id;
  const params = {
    targetSprintId: sprintId,
    taskIds: [taskId]
  };
  loading.value = true;
  await task.moveTask(params);
  loading.value = false;

  const fromId = event.from.id;
  taskTotalMap.value[sprintId] += 1;

  if (fromId === 'backlog') {
    backlogTotal.value -= 1;
    return;
  }

  taskTotalMap.value[fromId] -= 1;
};

const backlogAdd = async (event: { item: { id: string; }; from: { id: string; }; }) => {
  const taskId = event.item.id;
  const params = {
    targetSprintId: null,
    taskIds: [taskId]
  };
  loading.value = true;
  await task.moveTask(params);
  loading.value = false;

  const fromId = event.from.id;
  backlogTotal.value += 1;
  taskTotalMap.value[fromId] -= 1;
};

const refreshBacklog = () => {
  loadBacklogList(1);
};

const toSplit = (data: TaskInfo) => {
  splitTaskVisible.value = true;
  selectedTaskInfo.value = data;
};

const splitOk = async () => {
  selectedTaskInfo.value = undefined;
  await loadSprintList();
  await loadBacklogList(1);
};

const splitCancel = () => {
  selectedTaskInfo.value = undefined;
};

const generateOk = async () => {
  selectedTaskInfo.value = undefined;
  // await loadSprintList();
  await loadBacklogList(1);
};

const generateCancel = () => {
  selectedTaskInfo.value = undefined;
};

const toDelete = (data: TaskInfo, index: number, sprintId?: string) => {
  modal.confirm({
    content: t('backlog.messages.confirmDelete', { name: data.name }),
    async onOk () {
      const id = data.id;
      const params = { ids: [id] };
      const [error] = await task.deleteTask([id]);
      if (error) {
        return;
      }

      notification.success(t('backlog.messages.deleteSuccess'));

      if (typeof deleteTabPane === 'function') {
        deleteTabPane([id]);
      }

      if (sprintId) {
        taskListMap.value[sprintId].splice(index, 1);
      } else {
        backlogList.value.splice(index, 1);
      }

      taskInfoLoadingSet.value.delete((id));
    }
  });
};

const toEdit = (taskId: string, sprintId?: string) => {
  editTaskModalVisible.value = true;
  selectedSprintId.value = sprintId;
  selectedTaskId.value = taskId;
};

const editOk = async (data: Partial<TaskInfo>) => {
  const taskId = data.id;
  if (!taskId) {
    return;
  }

  const dataInfo = await loadTaskInfoById(taskId);
  if (!dataInfo) {
    return;
  }

  // 编辑
  if (selectedTaskId.value) {
    const _sprintId = selectedSprintId.value;
    if (_sprintId) {
      const index = taskListMap.value[_sprintId]?.findIndex(item => item.id === taskId);
      if (taskListMap.value[_sprintId][index]) {
        taskListMap.value[_sprintId][index] = cloneDeep(dataInfo);
      }
    } else {
      const index = backlogList.value.findIndex(item => item.id === taskId);
      if (backlogList.value[index]) {
        backlogList.value[index] = cloneDeep(dataInfo);
      }
    }

    return;
  }

  // 添加新的任务
  backlogList.value.push(dataInfo);
  newTaskName.value = undefined;
  newTaskPriority.value = 'MEDIUM';
};

const clickName = () => {
  // 如果 clickTimeout 存在，说明是双击事件，不执行单击逻辑
  if (clickTimeout.value) {
    clearTimeout(clickTimeout.value);
    clickTimeout.value = null;
    return;
  }

  // 单击事件逻辑
  clickTimeout.value = setTimeout(() => {
    clickTimeout.value = null; // 清除定时器
  }, 300); // 300 毫秒内如果没有双击，则认为是单击
};

const dblclickName = (data:TaskInfo) => {
  clearTimeout(clickTimeout.value); // 清除单击定时器
  clickTimeout.value = null;

  // 只能同时编辑单个名称
  editNameSet.value.clear();

  const id = data.id;
  editNameSet.value.add(id);
  editNameMap.value[id] = data.name;
};

const namePressEnter = async (data:SprintInfo, index:number) => {
  const id = data.id;
  const newName = editNameMap.value[id];
  loading.value = true;
  const [error] = await task.editTaskName(id, newName);
  loading.value = false;
  if (error) {
    return;
  }

  backlogList.value[index].name = newName;
  // 替换右侧任务详情
  if (taskInfo.value?.id === id) {
    taskInfo.value.name = newName;
  }

  // 清空保存的名称
  delete editNameMap.value[id];
  editNameSet.value.delete(id);
};

const cancelEditName = (id:string) => {
  editNameSet.value.delete(id);
  delete editNameMap.value[id];
};

const openSprint = (id: string) => {
  let open = false;
  if (openSet.value.has(id)) {
    openSet.value.delete(id);
    open = false;
  } else {
    openSet.value.add(id);
    open = true;
  }

  openChange(open, id);
};

const openChange = (open: boolean, id: string) => {
  if (open) {
    openSet.value.add(id);
    return;
  }

  openSet.value.delete(id);
};

const toChecked = async (id: string, data?: SprintInfo) => {
  sprintInfo.value = data;

  if (taskInfo.value?.id === id) {
    return;
  }

  if (taskInfoLoadingSet.value.has(id)) {
    return;
  }

  taskInfo.value = await loadTaskInfoById(id);
};

const loadTaskInfoById = async (id: string): Promise<TaskInfo | undefined> => {
  taskInfoLoadingSet.value.add(id);
  const [error, res] = await task.getTaskDetail(id);
  taskInfoLoadingSet.value.delete(id);
  if (error) {
    return;
  }

  const data = { ...res.data } || { id };
  return data;
};

const getTaskParams = () => {
  const params: {
    projectId: string;
    filters: { key: 'assigneeId' | 'createdBy' | 'name' | 'createdDate', op: 'EQUAL' | 'MATCH_END' | 'GREATER_THAN_EQUAL' | 'LESS_THAN_EQUAL', value: string }[];
    pageSize: number;
    sprintId?: string;
    pageNo?: number;
    backlog?: boolean;
    orderBy?: string;
    orderSort?: 'ASC'|'DESC'
  } = {
    projectId: props.projectId,
    pageSize: PAGE_SIZE,
    filters: []
  };

  if (searchValue.value) {
    params.filters.push({ key: 'name', op: 'MATCH_END', value: searchValue.value });
  }

  if (assigneeId.value) {
    params.filters.push({ key: 'assigneeId', op: 'EQUAL', value: assigneeId.value });
  }

  if (createdBy.value) {
    params.filters.push({ key: 'createdBy', op: 'EQUAL', value: createdBy.value });
  }

  if (quickDate.value) {
    let startDate: Dayjs;
    let endDate: Dayjs;
    if (quickDate.value === '1') {
      startDate = dayjs().startOf('date');
      endDate = dayjs();
    } else if (quickDate.value === '3') {
      startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
      endDate = dayjs();
    } else {
      startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
      endDate = dayjs();
    }

    params.filters.push({ key: 'createdDate', op: 'GREATER_THAN_EQUAL', value: startDate.format(DATE_TIME_FORMAT) });
    params.filters.push({ key: 'createdDate', op: 'LESS_THAN_EQUAL', value: endDate.format(DATE_TIME_FORMAT) });
  }

  return params;
};

const sortTaskByPriority = (sprintId) => {
  const priorityLevelConfig = {
    HIGHEST: 5,
    HIGH: 4,
    MEDIUM: 3,
    LOW: 2,
    LOWEST: 1
  };
  const taskList = taskListMap.value[sprintId];
  taskList.sort((a, b) => {
    if (sortParamMap.value[sprintId].orderSort === 'ASC') {
      return priorityLevelConfig[a.priority?.value] - priorityLevelConfig[b.priority?.value];
    } else if (sortParamMap.value[sprintId].orderSort === 'DESC') {
      return priorityLevelConfig[b.priority?.value] - priorityLevelConfig[a.priority?.value];
    } else {
      return 0;
    }
  });
  taskListMap.value[sprintId] = taskList;
};

const loadTaskListById = async (id: string, pageNo: number) => {
  const params = getTaskParams();
  params.sprintId = id;
  params.pageNo = pageNo;
  Object.assign(params, (sortParamMap.value[id] || {}));

  loading.value = true;
  const [error, res] = await task.getTaskList(params);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data;
  if (!data) {
    return;
  }

  taskTotalMap.value[id] = +data.total;

  const list = data.list || [];
  if (pageNo === 1) {
    taskListMap.value[id] = list;
  } else {
    taskListMap.value[id].push(...list);
  }

  const sprintIdSet = new Set<string>();
  for (let i = 0, len = list.length; i < len; i++) {
    const item = list[i];
    sprintIdSet.add(item.sprintId);
  }

  const sprintIds = Array.from(sprintIdSet);
  // 管理员拥有所有权限，无需加载权限
  if (!isAdmin.value) {
    for (let i = 0, len = sprintIds.length; i < len; i++) {
      const id = sprintIds[i];
      if (id) {
        const [_error, _res] = await loadPermissions(id);
        if (!_error) {
          const _permissions = (_res?.data || []).map(item => item.value);
          sprintPermissionsMap.value.set(id, _permissions);
        }
      }
    }
  } else {
    for (let i = 0, len = sprintIds.length; i < len; i++) {
      const id = sprintIds[i];
      sprintPermissionsMap.value.set(id, [
        'MODIFY_SPRINT',
        'DELETE_SPRINT',
        'ADD_TASK',
        'MODIFY_TASK',
        'DELETE_TASK',
        'EXPORT_TASK',
        'RESTART_TASK',
        'REOPEN_TASK',
        'GRANT'
      ]);
    }
  }

  if (taskListMap.value[id].length < taskTotalMap.value[id]) {
    await loadTaskListById(id, pageNo + 1);
  } else {
    if (sortParamMap.value[id]?.orderBy === 'priority') {
      sortTaskByPriority(id);
    }
  }
};

const loadSprintList = async () => {
  const params = {
    projectId: props.projectId,
    pageSize: PAGE_SIZE
  };
  loading.value = true;
  const [error, res] = await task.getSprintList(params);
  loading.value = false;
  if (error) {
    return;
  }

  const list = (res?.data?.list || []) as SprintInfo[];
  sprintList.value = [];

  for (let i = 0, len = list.length; i < len; i++) {
    const item = list[i];
    const id = item.id;
    membersMap.value[id] = item.members;
    taskListMap.value[id] = [];

    if (item.progress?.completedRate) {
      item.progress.completedRate = item.progress.completedRate.replace(/(\d+\.\d{2})\d+/, '$1');
    }

    sprintList.value.push(item);

    loadTaskListById(id, 1);
  }
};

const sortBacklogByPriority = () => {
  const priorityLevelConfig = {
    HIGHEST: 5,
    HIGH: 4,
    MEDIUM: 3,
    LOW: 2,
    LOWEST: 1
  };
  backlogList.value.sort((a, b) => {
    if (backlogSort.value?.orderSort === 'ASC') {
      return priorityLevelConfig[a.priority?.value] - priorityLevelConfig[b.priority?.value];
    } else if (backlogSort.value?.orderSort === 'DESC') {
      return priorityLevelConfig[b.priority?.value] - priorityLevelConfig[a.priority?.value];
    } else {
      return 0;
    }
  });
};

const loadBacklogList = async (pageNo: number) => {
  const params = getTaskParams();
  params.pageNo = pageNo;
  params.backlog = true;
  Object.assign(params, backlogSort.value || {});
  loading.value = true;
  const [error, res] = await task.getTaskList(params);
  loading.value = false;
  backlogLoaded.value = true;
  if (error) {
    backlogTotal.value = 0;
    backlogList.value = [];
    return;
  }

  const data = res?.data;
  if (!data) {
    backlogTotal.value = 0;
    backlogList.value = [];
    return;
  }

  backlogTotal.value = +data.total;

  const list = data.list || [];
  if (pageNo === 1) {
    backlogList.value = list;
  } else {
    backlogList.value.push(...list);
  }

  if (backlogTotal.value > backlogList.value.length) {
    await loadBacklogList(pageNo + 1);
  } else {
    if (backlogSort.value?.orderBy === 'priority') {
      sortBacklogByPriority();
    }
  }
};

const loadPermissions = async (id: string) => {
  const params = {
    admin: true
  };

  return await task.getUserSprintAuth(id, props.userInfo?.id, params);
};

const hasPermission = (data:TaskInfo, key:'edit'|'delete'|'move'|'split') => {
  const sprintId = data.sprintId;
  const sprintAuth = data.sprintAuth;

  const permissions = sprintPermissionsMap.value.get(sprintId) || [];
  const { currentAssociateType } = data;

  const isAdministrator = !!currentAssociateType?.map(item => item.value).includes('SYS_ADMIN' || 'APP_ADMIN');

  if (key === 'edit' || key === 'move' || key === 'split') {
    return isAdministrator || permissions.includes('MODIFY_TASK') || !sprintAuth;
  }

  if (key === 'delete') {
    return isAdministrator || permissions.includes('DELETE_TASK') || !sprintAuth;
  }
};

onMounted(() => {
  watch(() => props.projectId, async (newValue) => {
    if (!newValue) {
      return;
    }

    loadSprintList();
    loadBacklogList(1);
  }, { immediate: true });
});

const userId = computed(() => {
  return props.userInfo?.id;
});

const totalTaskNum = computed(() => {
  return sprintList.value.reduce((pre, current) => {
    return pre + (taskTotalMap.value[current.id] || 0);
  }, 0);
});

const taskType = computed(() => {
  return taskInfo?.value?.taskType?.value;
});

const taskId = computed(() => {
  return taskInfo?.value?.id;
});

const selectNone = computed(() => {
  return !createdBy.value && !assigneeId.value && !quickDate.value;
});
</script>

<template>
  <div class="flex flex-col h-full py-5 leading-5 text-3">
    <Introduce class="mb-7 mx-5" />
    <Spin :spinning="loading" class="flex-1 px-5 overflow-hidden">
      <div class="flex items-center">
        <Input
          v-model:value="searchValue"
          :maxlength="200"
          allowClear
          class="w-60 mr-5"
          :placeholder="t('backlog.placeholder.searchTaskName')"
          trim
          @change="searchChange" />
        <div class="whitespace-nowrap text-text-sub-content mr-2">
          <span>{{ t('quickSearch') }}</span>
          <Colon />
        </div>
        <div
          :class="{ 'active-key': !!selectNone }"
          class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none mr-2"
          @click="queryAll">
          {{ t('backlog.quickSearch.all') }}
        </div>
        <div
          :class="{ 'active-key': createdBy === userId }"
          class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none mr-2"
          @click="queryByCreatedMe">
          {{ t('backlog.quickSearch.createdByMe') }}
        </div>
        <div
          :class="{ 'active-key': assigneeId === userId }"
          class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none mr-2"
          @click="queryByMe">
          {{ t('backlog.quickSearch.assignedToMe') }}
        </div>
        <div
          :class="{ 'active-key': quickDate === '1' }"
          class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none mr-2"
          @click="queryByDate('1')">
          {{ t('backlog.quickSearch.past1Day') }}
        </div>
        <div
          :class="{ 'active-key': quickDate === '3' }"
          class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none mr-2"
          @click="queryByDate('3')">
          {{ t('backlog.quickSearch.past3Days') }}
        </div>
        <div
          :class="{ 'active-key': quickDate === '7' }"
          class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none"
          @click="queryByDate('7')">
          {{ t('backlog.quickSearch.past7Days') }}
        </div>
      </div>
      <div class="h-0 border-t border-solid border-theme-text-box mt-2.5"></div>
      <div class="flex items-start overflow-hidden" style="height: calc(100% - 39px);">
        <div ref="sprintRef" class="flex-1 h-full pr-2.5 pt-2.5 overflow-auto scroll-smooth">
          <div class="border border-solid border-theme-text-box rounded">
            <div class="header-container border-solid border-b border-theme-text-box">
              <div class="flex items-center px-2.5 py-4 flex-nowrap space-x-5">
                <div class="flex-1 flex items-center truncate">
                  <Arrow
                    :open="openSet.has('sprintBacklog')"
                    type="dashed"
                    class="mr-1.5"
                    style="font-size: 12px;"
                    @change="openChange($event, 'sprintBacklog')" />

                  <div class="font-semibold text-theme-title w-70 mr-5">{{ t('backlog.sprintBacklogTitle') }}</div>
                  <div class="w-25">{{ sprintList.length || 0 }} {{ t('backlog.sprintCount') }}</div>
                  <div class="w-25 flex-shrink-0 flex items-center space-x-1">
                    <span>{{ totalTaskNum || 0 }}</span>
                    <span>{{ t('backlog.taskCount') }}</span>
                  </div>
                </div>
              </div>
            </div>
            <Draggable
              v-for="item in sprintList"
              v-show="openSet.has('sprintBacklog')"
              :id="item.id"
              :key="item.id"
              :list="taskListMap[item.id]"
              :animation="300"
              :class="{ 'draggable-container-open': openSet.has(item.id) }"
              ghostClass="ghost"
              group="tasks"
              itemKey="id"
              tag="div"
              class="draggable-container"
              @add="dragAdd($event, item.id)">
              <template #header>
                <div class="header-container border-solid border-b border-theme-text-box">
                  <div class="flex items-center px-2.5 py-4 flex-nowrap space-x-5">
                    <div class="flex-1 flex items-center truncate pl-5">
                      <Arrow
                        :open="openSet.has(item.id)"
                        type="dashed"
                        class="mr-1.5"
                        style="font-size: 12px;"
                        @change="openChange($event, item.id)" />
                      <div :title="item.name" class="w-65 truncate text-theme-title  mr-5">
                        <span class="cursor-pointer" @click.stop="openSprint(item.id)">{{ item.name }}</span>
                      </div>
                      <div class="w-25 flex-shrink-0 flex items-center space-x-1 text-theme-sub-content">
                        <span>{{ taskTotalMap[item.id] || 0 }}</span>
                        <span>{{ t('backlog.taskCount') }}</span>
                      </div>
                      <div class="flex-shrink-0 flex items-center mr-5">
                        <div
                          :class="item.status?.value"
                          class="text-3 leading-3 px-1 py-1 rounded  text-white flex items-center flex-none whitespace-nowrap mr-3.5">
                          <div class="transform-gpu scale-90">{{ item.status?.message }}</div>
                        </div>
                        <Progress :percent="+item.progress?.completedRate" style="width:150px;" />
                      </div>
                      <div class="flex-shrink-0 text-theme-sub-content">
                        <span>{{ item.startDate }}</span>
                        <span> {{ t('backlog.to') }} </span>
                        <span>{{ item.deadlineDate }}</span>
                      </div>
                    </div>

                    <div class="flex items-center flex-shrink-0 flex-nowrap space-x-2.5">
                      <DropdownSort :menuItems="sortOption" @click="handleSortTask($event, item.id)">
                        <Button
                          size="small"
                          type="text"
                          class="inline-flex space-x-1 items-center"
                          style="height:20px;padding:0;line-height:20px;">
                          <Icon icon="icon-biaotoupaixu" class="text-3.5" />
                          <span>{{ t('backlog.sort') }}</span>
                        </Button>
                      </DropdownSort>

                      <Button
                        :disabled="sprintPermissionsMap[item.id]?.includes('MODIFY_SPRINT')"
                        size="small"
                        type="text"
                        style="height:20px;padding:0;line-height:20px;">
                        <RouterLink class="flex items-center space-x-1" :to="`/task#sprint?id=${item.id}`">
                          <Icon icon="icon-shuxie" class="text-3.5" />
                          <span>{{ t('backlog.edit') }}</span>
                        </RouterLink>
                      </Button>

                      <RouterLink
                        class="flex items-center space-x-1"
                        :to="`/task#task?sprintId=${item.id}&sprintName=${item.name}`">
                        <Icon icon="icon-renwu2" class="text-3.5" />
                        <span>{{ t('backlog.enterSprint') }}</span>
                      </RouterLink>
                    </div>
                  </div>

                  <div
                    v-if="!!membersMap[item.id]?.length"
                    class="flex items-center pr-7.5 pl-12.5 pb-3 space-x-1.5 transform-gpu -translate-y-1.5">
                    <Tooltip v-for="member in membersMap[item.id]" :key="member.id">
                      <template #title>
                        <div class="leading-5 text-theme-content">
                          <div class="mb-1 text-theme-title">{{ member.fullName }}</div>
                          <div class="flex items-center mb-0.5">
                            <div class="flex items-center w-12.25">
                              <span>{{ t('backlog.columns.taskCount') }}</span>
                              <Colon class="w-1" />
                            </div>
                            <span>{{ membersCountMap[item.id]?.[member.id]?.validTaskNum || 0 }}</span>
                          </div>
                          <div class="flex items-center">
                            <div class="flex items-center w-12.25">
                              <span>{{ item.evalWorkloadMethod?.value === 'STORY_POINT' ? t('backlog.columns.storyPoint') : t('backlog.columns.workHours') }}</span>
                              <Colon class="w-1" />
                            </div>
                            <span>{{ membersCountMap[item.id]?.[member.id]?.evalWorkload || 0 }}</span>
                          </div>
                        </div>
                      </template>

                      <div @mouseenter="mouseenter(item.id)">
                        <Image
                          :key="member.id"
                          :alt="member.fullName"
                          :src="member.avatar"
                          type="avatar"
                          class="cursor-pointer"
                          style="width: 20px;height: 20px;border-radius: 20px;" />
                      </div>
                    </Tooltip>
                  </div>
                </div>

                <template v-if="taskTotalMap[item.id] === 0">
                  <div
                    v-show="openSet.has(item.id)"
                    class="empty-draggable mt-4.75 mx-5 h-9.5 flex items-center justify-center rounded text-theme-sub-content">
                    {{ t('backlog.noTasksInSprint') }}
                  </div>
                </template>
              </template>

              <template #item="{ element, index }: { element: TaskInfo; index: number; }">
                <div
                  v-show="openSet.has(item.id)"
                  :id="element.id"
                  :class="{ 'active-item': taskId === element.id, 'draggable-item-hover': popoverId === element.id }"
                  class="draggable-item flex items-center space-x-5 truncate !ml-10"
                  @click="toChecked(element.id, item)">
                  <IconTask :value="element.taskType?.value" />
                  <TaskPriority :value="element.priority" />
                  <span>{{ element.code }}</span>
                  <span class="flex-1 truncate" :title="element.name">{{ element.name }}</span>
                  <div class="flex items-center space-x-2.5 action-container">
                    <Button
                      :disabled="!hasPermission(element,'split')"
                      type="text"
                      size="small"
                      class="px-0 h-5 leading-5 space-x-1 flex items-center"
                      @click.stop="toSplit(element)">
                      <Icon icon="icon-guanlianziyuan" class="text-3.5" />
                      <span>{{ t('backlog.split') }}</span>
                    </Button>

                    <Popover
                      overlayClassName="px-content-1.5"
                      placement="left"
                      :mouseEnterDelay="0.3"
                      @visibleChange="visibleChange($event, element.id)">
                      <template #content>
                        <div class="max-w-100 space-y-1 leading-5 text-3 truncate">
                          <div
                            :title="t('backlog.moveToBacklog')"
                            class="popover-item truncate cursor-pointer px-2"
                            @click="moveToBacklog(item.id, element, index)">
                            {{ t('backlog.moveToBacklog') }}
                          </div>
                          <template v-for="_sprint in sprintList" :key="_sprint.id">
                            <div
                              v-if="_sprint.id !== item.id"
                              :title="_sprint.name"
                              class="popover-item truncate cursor-pointer px-2"
                              @click="toMove(item.id, _sprint.id, element, index)">
                              {{ _sprint.name }}
                            </div>
                          </template>
                        </div>
                      </template>
                      <Button
                        :disabled="!hasPermission(element,'move')"
                        type="text"
                        size="small"
                        class="px-0 h-5 leading-5 space-x-1 flex items-center"
                        @click.stop="">
                        <Icon icon="icon-diedai" class="text-3.5" />
                        <span>{{ t('backlog.moveTo') }}</span>
                      </Button>
                    </Popover>

                    <Button
                      :disabled="!hasPermission(element,'delete')"
                      type="text"
                      size="small"
                      class="px-0 h-5 leading-5 space-x-1 flex items-center"
                      @click.stop="toDelete(element, index, item.id)">
                      <Icon icon="icon-qingchu" class="text-3.5" />
                      <span>{{ t('backlog.delete') }}</span>
                    </Button>

                    <Button
                      :disabled="!hasPermission(element,'edit')"
                      type="text"
                      size="small"
                      class="px-0 h-5 leading-5 space-x-1 flex items-center"
                      @click.stop="toEdit(element.id, item.id)">
                      <Icon icon="icon-shuxie" class="text-3.5" />
                      <span>{{ t('backlog.edit') }}</span>
                    </Button>
                  </div>
                </div>
              </template>
            </Draggable>

            <Draggable
              id="backlog"
              :list="backlogList"
              :animation="300"
              ghostClass="ghost"
              group="tasks"
              itemKey="id"
              tag="div"
              class="draggable-container draggable-container-open"
              @add="backlogAdd">
              <template #header>
                <div class="flex items-center justify-between px-2.5 header-container border-solid border-b border-theme-text-box">
                  <div class="flex items-center py-4 truncate">
                    <Arrow
                      :open="openSet.has('productBacklog')"
                      type="dashed"
                      class="mr-1.5"
                      style="font-size: 12px;"
                      @change="openChange($event, 'productBacklog')" />
                    <div class="truncate text-theme-title font-semibold w-70 mr-5">{{ t('backlog.productBacklogTitle') }}</div>
                    <div class="flex-shrink-0 flex items-center space-x-1 mr-3.5">
                      <span>{{ backlogTotal || 0 }}</span>
                      <span>{{ t('backlog.taskCount') }}</span>
                    </div>
                  </div>

                  <div class="inline-flex space-x-2">
                    <DropdownSort :menuItems="backlogSortOption" @click="handleSortBasklogTask">
                      <Button
                        size="small"
                        type="text"
                        class="inline-flex space-x-1 items-center"
                        style="height:20px;padding:0;line-height:20px;">
                        <Icon icon="icon-biaotoupaixu" class="text-3.5" />
                        <span>{{ t('backlog.sort') }}</span>
                      </Button>
                    </DropdownSort>
                    <Button
                      type="text"
                      size="small"
                      class="px-0 h-5 leading-5 space-x-1 flex items-center"
                      @click.stop="refreshBacklog">
                      <Icon class="text-3.5" icon="icon-shuaxin" />
                      <span>{{ t('backlog.refresh') }}</span>
                    </Button>
                  </div>
                </div>

                <div
                  v-if="backlogLoaded && !backlogTotal"
                  class="empty-draggable mt-4.75 mx-5 h-9.5 flex items-center justify-center rounded text-theme-sub-content">
                  {{ t('backlog.noBacklog') }}
                </div>
              </template>

              <template #footer>
                <div class="px-5 mt-3.5">
                  <div
                    v-if="aiEnabled"
                    v-show="!addFlag"
                    class="flex items-center space-x-2.5">
                    <Button
                      type="primary"
                      size="small"
                      class="flex items-center space-x-1"
                      @click="toAddTask">
                      <Icon icon="icon-jia" class="text-3.5" />
                      <span>{{ t('backlog.main.addBacklog') }}</span>
                    </Button>

                    <Button
                      type="primary"
                      size="small"
                      ghost
                      class="flex items-center space-x-1"
                      @click="aiGenerateTask">
                      <Icon icon="icon-jia" class="text-3.5" />
                      <span>{{ t('backlog.main.aiAddBacklog') }}</span>
                    </Button>
                  </div>

                  <Button
                    v-else
                    v-show="!addFlag"
                    type="default"
                    size="small"
                    style="border-color: #40a9ff;background-color: rgba(244, 2250, 254, 100%);"
                    class="flex items-center space-x-1 border-dashed w-full h-11"
                    @click="toAddTask">
                    <Icon icon="icon-jia" class="text-3.5" />
                    <span>{{ t('backlog.main.addBacklog') }}</span>
                  </Button>

                  <div v-show="addFlag" class="flex items-center">
                    <SelectEnum
                      v-model:value="newTaskType"
                      enumKey="TaskType"
                      :placeholder="t('backlog.main.placeholders.taskType')"
                      :excludes="({value}) => ['API_TEST', 'SCENARIO_TEST'].includes(value)"
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
                      :placeholder="t('backlog.main.placeholders.selectPriority')"
                      class="w-28 mr-2">
                      <template #option="record">
                        <TaskPriority :value="record" />
                      </template>
                    </SelectEnum>

                    <Input
                      ref="taskNameInputRef"
                      v-model:value="newTaskName"
                      :maxlength="200"
                      :placeholder="t('backlog.main.placeholders.taskName')"
                      trim
                      class="w-200 mr-5"
                      @pressEnter="pressEnter" />

                    <div class="flex items-center space-x-2.5">
                      <Button
                        :disabled="!newTaskName"
                        type="primary"
                        size="small"
                        @click="toSave">
                        {{ t('backlog.main.save') }}
                      </Button>

                      <Button
                        type="default"
                        size="small"
                        @click="cancelAdd">
                        {{ t('backlog.main.cancel') }}
                      </Button>

                      <Button
                        type="default"
                        size="small"
                        @click="openCreateModal">
                        {{ t('backlog.main.openAddModal') }}
                      </Button>
                    </div>
                  </div>
                </div>
              </template>

              <template #item="{ element, index }: { element: TaskInfo; index: number; }">
                <div
                  v-show="openSet.has('productBacklog')"
                  :id="element.id"
                  :class="{ 'active-item': taskId === element.id, 'draggable-item-hover': popoverId === element.id }"
                  class="draggable-item flex items-center justify-between space-x-5 truncate"
                  @click="toChecked(element.id)">
                  <div class="flex items-center space-x-5 truncate">
                    <IconTask :value="element.taskType?.value" />
                    <TaskPriority :value="element.priority" />
                    <div>{{ element.code }}</div>
                    <div
                      v-if="editNameSet.has(element.id)"
                      class="flex items-center"
                      @click.stop="">
                      <Input
                        ref="taskNameInputRef"
                        v-model:value="editNameMap[element.id]"
                        :maxlength="200"
                        :placeholder="t('backlog.main.placeholders.taskName')"
                        trim
                        class="w-100 mr-5"
                        @pressEnter="namePressEnter(element,index)" />
                      <Button
                        :disabled="!editNameMap[element.id]"
                        type="primary"
                        size="small"
                        class="mr-2.5"
                        @click="namePressEnter(element,index)">
                        {{ t('backlog.main.save') }}
                      </Button>

                      <Button
                        type="default"
                        size="small"
                        class="bg-white"
                        @click="cancelEditName(element.id)">
                        {{ t('backlog.main.cancel') }}
                      </Button>
                    </div>
                    <div
                      v-else
                      class="flex-1 truncate cursor-default"
                      :title="element.name"
                      @click.stop="clickName"
                      @dblclick.stop="dblclickName(element)">
                      {{ element.name }}
                    </div>
                  </div>
                  <div class="flex items-center space-x-2.5 action-container">
                    <Button
                      type="text"
                      size="small"
                      class="px-0 h-5 leading-5 space-x-1 flex items-center"
                      @click.stop="toSplit(element, index)">
                      <Icon icon="icon-guanlianziyuan" class="text-3.5" />
                      <span>{{ t('backlog.main.split') }}</span>
                    </Button>

                    <Popover
                      overlayClassName="px-content-1.5"
                      placement="left"
                      :mouseEnterDelay="0.3"
                      @visibleChange="visibleChange($event, element.id)">
                      <template #content>
                        <div class="max-w-100 space-y-1 leading-5 text-3 truncate">
                          <div
                            v-for="_sprint in sprintList"
                            :key="_sprint.id"
                            :title="_sprint.name"
                            class="popover-item truncate cursor-pointer px-2"
                            @click="toAssignment(_sprint.id, element, index)">
                            {{ _sprint.name }}
                          </div>
                        </div>
                      </template>
                      <Button
                        type="text"
                        size="small"
                        class="px-0 h-5 leading-5 space-x-1 flex items-center"
                        @click.stop="">
                        <Icon icon="icon-diedai" class="text-3.5" />
                        <span>{{ t('backlog.main.assign') }}</span>
                      </Button>
                    </Popover>

                    <Button
                      type="text"
                      size="small"
                      class="px-0 h-5 leading-5 space-x-1 flex items-center"
                      @click.stop="toDelete(element, index)">
                      <Icon icon="icon-qingchu" class="text-3.5" />
                      <span>{{ t('backlog.main.delete') }}</span>
                    </Button>

                    <Button
                      type="text"
                      size="small"
                      class="px-0 h-5 leading-5 space-x-1 flex items-center"
                      @click.stop="toEdit(element.id)">
                      <Icon icon="icon-shuxie" class="text-3.5" />
                      <span>{{ t('backlog.main.edit') }}</span>
                    </Button>
                  </div>
                </div>
              </template>
            </Draggable>
          </div>
        </div>

        <div class="drawer-container flex items-start" :class="{ 'drawer-open': !!taskId }">
          <div class="flex-shrink-0 h-full w-9 py-1.5 space-y-1 overflow-y-auto drawer-action-container">
            <div
              :class="{ 'drawer-active-item': drawerActiveKey === 'basic' }"
              class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
              :title="t('backlog.main.drawerTitles.basicInfo')"
              @click="drawerActiveKeyChange('basic')">
              <Icon icon="icon-wendangxinxi" class="text-4" />
            </div>

            <div
              :class="{ 'drawer-active-item': drawerActiveKey === 'person' }"
              class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
              :title="t('backlog.main.drawerTitles.personnel')"
              @click="drawerActiveKeyChange('person')">
              <Icon icon="icon-quanburenyuan" class="text-4" />
            </div>

            <div
              :class="{ 'drawer-active-item': drawerActiveKey === 'date' }"
              class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
              :title="t('backlog.main.drawerTitles.date')"
              @click="drawerActiveKeyChange('date')">
              <Icon icon="icon-riqi" class="text-4" />
            </div>

            <div
              :class="{ 'drawer-active-item': drawerActiveKey === 'tasks' }"
              class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
              :title="t('backlog.main.drawerTitles.relatedTasks')"
              @click="drawerActiveKeyChange('tasks')">
              <Icon icon="icon-ceshirenwu" class="text-4" />
            </div>

            <div
              :class="{ 'drawer-active-item': drawerActiveKey === 'cases' }"
              class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
              :title="t('backlog.main.drawerTitles.relatedCases')"
              @click="drawerActiveKeyChange('cases')">
              <Icon icon="icon-ceshiyongli1" class="text-4" />
            </div>

            <div
              :class="{ 'drawer-active-item': drawerActiveKey === 'attachments' }"
              class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
              :title="t('backlog.main.drawerTitles.attachments')"
              @click="drawerActiveKeyChange('attachments')">
              <Icon icon="icon-lianjie1" class="text-4" />
            </div>

            <div
              :class="{ 'drawer-active-item': drawerActiveKey === 'remarks' }"
              class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
              :title="t('backlog.main.drawerTitles.remarks')"
              @click="drawerActiveKeyChange('remarks')">
              <Icon icon="icon-shuxie" class="text-4" />
            </div>

            <div
              :class="{ 'drawer-active-item': drawerActiveKey === 'comment' }"
              class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
              :title="t('backlog.main.drawerTitles.comments')"
              @click="drawerActiveKeyChange('comment')">
              <Icon icon="icon-pinglun" class="text-4" />
            </div>

            <div
              :class="{ 'drawer-active-item': drawerActiveKey === 'activity' }"
              class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
              :title="t('backlog.main.drawerTitles.activity')"
              @click="drawerActiveKeyChange('activity')">
              <Icon icon="icon-chakanhuodong" class="text-4" />
            </div>
          </div>

          <div class="w-full h-full flex-1 overflow-hidden">
            <div class="flex items-center justify-between mt-4 pl-5 space-x-2.5">
              <div class="flex-1 flex items-center truncate">
                <RouterLink
                  v-if="sprintInfo"
                  :to="`/task#sprint?id=${sprintInfo?.id}`"
                  :title="sprintInfo?.name"
                  class="truncate"
                  style="max-width: 50%;">
                  {{ sprintInfo?.name }}
                </RouterLink>
                <div v-else class="flex-shrink-0">Backlog</div>
                <div class="mx-1.5">/</div>
                <RouterLink
                  :to="`/task#task?id=${taskInfo?.id}`"
                  class="truncate flex-1"
                  :title="taskInfo?.name">
                  {{ taskInfo?.name }}
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

            <div style="height: calc(100% - 36px);" class="pt-3.5 pb-3.5 overflow-hidden">
              <AsyncComponent :visible="!!taskId">
                <ApiInfo
                  v-if="taskType === 'API_TEST'"
                  v-show="drawerActiveKey === 'basic'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="taskInfo"
                  @refresh="taskRefresh"
                  @change="taskInfoChange"
                  @loadingChange="loadingChange" />

                <ScenarioInfo
                  v-else-if="taskType === 'SCENARIO_TEST'"
                  v-show="drawerActiveKey === 'basic'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="taskInfo"
                  @refresh="taskRefresh"
                  @change="taskInfoChange"
                  @loadingChange="loadingChange" />

                <BasicInfo
                  v-else
                  v-show="drawerActiveKey === 'basic'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="taskInfo"
                  @refresh="taskRefresh"
                  @change="taskInfoChange"
                  @loadingChange="loadingChange" />

                <PersonnelInfo
                  v-show="drawerActiveKey === 'person'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="taskInfo"
                  @change="taskInfoChange"
                  @loadingChange="loadingChange" />

                <DateInfo
                  v-show="drawerActiveKey === 'date'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="taskInfo"
                  @change="taskInfoChange"
                  @loadingChange="loadingChange" />

                <RefTasks
                  v-show="drawerActiveKey === 'tasks'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="taskInfo"
                  @change="taskInfoChange"
                  @loadingChange="loadingChange" />

                <RefCases
                  v-show="drawerActiveKey === 'cases'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="taskInfo"
                  @change="taskInfoChange"
                  @loadingChange="loadingChange" />

                <AttachmentInfo
                  v-show="drawerActiveKey === 'attachments'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="taskInfo"
                  @change="taskInfoChange"
                  @loadingChange="loadingChange" />

                <Comment
                  v-show="drawerActiveKey === 'comment'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="taskInfo"
                  @change="taskInfoChange"
                  @loadingChange="loadingChange" />

                <Activity
                  v-show="drawerActiveKey === 'activity'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="taskInfo"
                  @change="taskInfoChange"
                  @loadingChange="loadingChange" />

                <Remarks
                  v-show="drawerActiveKey === 'remarks'"
                  :id="taskInfo?.id"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo" />
              </AsyncComponent>
            </div>
          </div>
        </div>
      </div>

      <AsyncComponent :visible="editTaskModalVisible">
        <EditTaskModal
          v-model:visible="editTaskModalVisible"
          :taskId="selectedTaskId"
          :sprintId="selectedSprintId"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          @ok="editOk" />
      </AsyncComponent>
    </Spin>

    <AsyncComponent :visible="splitTaskVisible">
      <SplitTask
        v-model:visible="splitTaskVisible"
        :projectId="props.projectId"
        :taskInfo="selectedTaskInfo"
        @ok="splitOk"
        @cancel="splitCancel" />
    </AsyncComponent>

    <AsyncComponent :visible="aiGenerateTaskVisible">
      <AIGenerateTask
        v-model:visible="aiGenerateTaskVisible"
        :projectId="props.projectId"
        @ok="generateOk"
        @cancel="generateCancel" />
    </AsyncComponent>
  </div>
</template>

<style>
.ant-popover.px-content-1\.5 .ant-popover-inner-content {
  max-height: 500px;
  padding-right: 6px;
  padding-left: 6px;
  overflow: auto;
}
</style>

<style scoped>
.PENDING {
  background-color: rgba(45, 142, 255, 100%);
}

.IN_PROGRESS {
  background-color: rgba(103, 215, 255, 100%);
}

.COMPLETED {
  background-color: rgba(82, 196, 26, 100%);
}

.BLOCKED {
  background-color: rgba(255, 165, 43, 100%);
}

:deep(.ant-progress-outer) {
  width: 100px;
}

.active-key {
  background-color: #4ea0fd;
  color: #fff;
}

.router-link {
  color: #1890ff;
  cursor: pointer;
}

.drawer-action-container {
  background-color: rgba(247, 248, 251, 100%);
  color: var(--content-text-sub-content);
}

.draggable-container {
  position: relative;
}

.draggable-container.draggable-container-open {
  padding-bottom: 20px;
}

.draggable-container::after {
  content: "";
  display: block;
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background-color: var(--border-text-box);
}

.draggable-container:last-child::after {
  display: none;
}

.empty-draggable {
  border: 2px dashed var(--border-text-box);
}

.draggable-item {
  position: relative;
  margin-right: 20px;
  margin-left: 20px;
  padding: 8px;
  border-radius: 4px;
  cursor: move;
}

.draggable-item::after {
  content: "";
  display: block;
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background-color: var(--border-text-box);
}

.draggable-item:last-child::after {
  display: none;
}

.draggable-item:hover,
.draggable-item.active-item {
  background-color: rgba(239, 240, 243, 100%);
}

.draggable-item:last-child {
  border-bottom: none;
}

.action-container {
  visibility: hidden;
}

.draggable-item-hover .action-container,
.draggable-item:hover .action-container {
  visibility: visible;
}

.popover-item {
  padding-top: 4px;
  padding-bottom: 4px;
  border-radius: 4px;
}

.popover-item:hover {
  background-color: var(--border-text-box);
}

.ghost {
  opacity: 0.5;
}

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

.action-item:hover,
.action-item.drawer-active-item {
  background-color: rgba(239, 240, 243, 100%);
}

:deep(.ant-input-affix-wrapper-sm) {
  background-color: #fff;
}
</style>
