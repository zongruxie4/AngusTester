<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch, Ref } from 'vue';
import { Button } from 'ant-design-vue';
import {
  Arrow,
  AsyncComponent,
  Colon,
  Dropdown,
  Icon,
  IconTask,
  Image,
  modal,
  notification,
  TaskPriority,
  Tooltip
} from '@xcan-angus/vue-ui';
import { enumLoader } from '@xcan-angus/tools';
import dayjs from 'dayjs';
import { reverse, sortBy } from 'lodash-es';
import Draggable from 'vuedraggable';
import { task } from 'src/api/tester';

import { TaskInfo } from '../../../../PropsType';
import { ActionMenuItem, SprintPermissionKey } from './PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  filters: { key: string; op: string; value: boolean | string | string[]; }[];
  notify: string;
  moduleId: string;
  loading: boolean;
  groupKey: 'none' | 'assigneeName' | 'lastModifiedByName' | 'taskType';
  orderBy: 'priority' | 'deadlineDate' | 'createdByName' | 'assigneeName';
  orderSort: 'DESC' | 'ASC';
};

const props = withDefaults(defineProps<Props>(), {
  filters: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  moduleId: undefined,
  loading: false,
  groupKey: 'none',
  orderBy: undefined,
  orderSort: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'update:loading', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
  (event: 'refreshChange'): void;
}>();

const MoveTaskModal = defineAsyncComponent(() => import('@/views/task/task/list/task/move/index.vue'));
const EditTaskModal = defineAsyncComponent(() => import('@/views/task/task/list/task/edit/index.vue'));
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

const isAdmin = inject('isAdmin', ref(false));
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));

const drawerActiveKey = ref<'basic' | 'person' | 'date' | 'comment' | 'activity' | 'tasks' | 'cases' | 'attachments' | 'remarks'>('basic');

const checkedTaskInfo = ref<TaskInfo>();
const checkedSprintInfo = ref<{ id: string; name: string; }>();

const moveModalVisible = ref(false);
const selectedTaskSprintId = ref<string>();
const selectedTaskId = ref<string>();
const selectedTaskName = ref<string>();

const selectedIndex = ref<number>();
const selectedStatus = ref<TaskInfo['status']['value']>();
const selectedByGroupKey = ref<string>();
const taskModalVisible = ref(false);
const searchSprintId = ref<string>();

const sprintPermissionsMap = ref<Map<string, SprintPermissionKey[]>>(new Map());

const statusList = ref<{ message: string; value: TaskInfo['status']['value'] }[]>([]);
const taskList = ref<TaskInfo[]>([]);
const groupDataMap = ref<{ [key: string]: { [key in TaskInfo['status']['value']]: TaskInfo[] } }>({});
const taskDataMap = ref<{ [key in TaskInfo['status']['value']]: TaskInfo[] }>({
  CANCELED: [],
  COMPLETED: [],
  CONFIRMING: [],
  IN_PROGRESS: [],
  PENDING: []
});
const numMap = ref<{ [key in TaskInfo['status']['value']]: number }>({
  CANCELED: 0,
  COMPLETED: 0,
  CONFIRMING: 0,
  IN_PROGRESS: 0,
  PENDING: 0
});

const assigneeNameList = ref<{ name: string; value: string }[]>([]);
const lastModifiedByNameList = ref<{ name: string; value: string }[]>([]);
const taskTypeList = ref<{ name: string; value: string }[]>([]);

const openFlag = ref(false);
const arrowOpenSet = ref(new Set<string>());

const isDraggingToColumn = ref<number | null>(null);
const isDraggingToColumnStatus = ref<TaskInfo['status']['value'][]>([]);

const loadEnum = async () => {
  const [error, res] = await enumLoader.load('TaskStatus');
  if (error) {
    return;
  }

  statusList.value = (res || []) as { message: string; value: TaskInfo['status']['value'] }[];
};

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

  // 保存原始数据
  taskList.value = _taskList;

  const newList: TaskInfo[] = [];
  const sprintIdSet = new Set<string>();
  const assigneeNameSet = new Set<string>();
  const lastModifiedByNameSet = new Set<string>();
  const taskTypeSet = new Set<string>();
  for (let i = 0, len = _taskList.length; i < len; i++) {
    const item = _taskList[i];
    const status = item.status?.value;
    numMap.value[status] += 1;
    taskDataMap.value[status].push(item);
    newList.push(item);
    sprintIdSet.add(item.sprintId);

    if (!assigneeNameSet.has(item.assigneeName)) {
      if (!item.assigneeName) {
        assigneeNameList.value.unshift({
          name: '未分组',
          value: '-1'
        });
      } else {
        assigneeNameList.value.push({
          name: item.assigneeName,
          value: item.assigneeId
        });
      }
    }

    if (!lastModifiedByNameSet.has(item.lastModifiedByName)) {
      lastModifiedByNameList.value.push({
        name: item.lastModifiedByName,
        value: item.lastModifiedBy
      });
    }

    if (!taskTypeSet.has(item.taskType.value)) {
      taskTypeList.value.push({
        name: item.taskType.message,
        value: item.taskType.value
      });
    }

    assigneeNameSet.add(item.assigneeName);
    lastModifiedByNameSet.add(item.lastModifiedByName);
    taskTypeSet.add(item.taskType.value);

    if (props.groupKey !== 'none') {
      setGroupData(item);
    }
  }

  if (props.groupKey !== 'none') {
    setDefaultGroupData();
  }

  sprintPermissionsMap.value.clear();
  const sprintIds = Array.from(sprintIdSet);
  // 管理员拥有所有权限，无需加载权限
  if (!isAdmin.value && proTypeShowMap.value.showSprint) {
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

  emit('update:loading', false);
};

const loadPermissions = async (id: string) => {
  const params = {
    admin: true
  };

  return await task.getUserSprintAuth(id, props.userInfo?.id, params);
};

const loadTaskInfoById = async (id: string): Promise<Partial<TaskInfo>> => {
  emit('update:loading', true);
  const [error, res] = await task.loadTaskInfo(id);
  emit('update:loading', false);
  if (error || !res?.data) {
    return { id };
  }
  return { ...res.data };
};

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

const toSort = (data: { orderBy: 'priority' | 'deadlineDate' | 'createdByName' | 'assigneeName'; orderSort: 'DESC' | 'ASC'; }) => {
  sortData(data.orderBy, data.orderSort);
};

const sortData = (orderBy: 'priority' | 'deadlineDate' | 'createdByName' | 'assigneeName', orderSort: 'DESC' | 'ASC') => {
  const map = taskDataMap.value;
  if (orderBy === 'priority') {
    const sortKeys = orderSort === 'DESC' ? ['HIGHEST', 'HIGH', 'MEDIUM', 'LOW', 'LOWEST'] : ['LOWEST', 'LOW', 'MEDIUM', 'HIGH', 'HIGHEST'];
    for (const key in map) {
      map[key].sort((a, b) => {
        const index1 = sortKeys.indexOf(a.priority.value);
        const index2 = sortKeys.indexOf(b.priority.value);
        return index1 - index2;
      });
    }

    return;
  }

  if (orderBy === 'deadlineDate') {
    if (orderSort === 'DESC') {
      for (const key in map) {
        map[key].sort((a, b) => {
          if (dayjs(a.deadlineDate).isBefore(dayjs(b.deadlineDate))) {
            return 1;
          }

          if (dayjs(a.deadlineDate).isAfter(dayjs(b.deadlineDate))) {
            return -1;
          }

          return 0;
        });
      }

      return;
    }

    if (orderSort === 'ASC') {
      for (const key in map) {
        map[key].sort((a, b) => {
          if (dayjs(a.deadlineDate).isBefore(dayjs(b.deadlineDate))) {
            return -1;
          }

          if (dayjs(a.deadlineDate).isAfter(dayjs(b.deadlineDate))) {
            return 1;
          }

          return 0;
        });
      }
    }

    return;
  }

  if (orderBy === 'assigneeName' || orderBy === 'createdByName') {
    if (orderSort === 'DESC') {
      for (const key in map) {
        map[key] = reverse(sortBy(map[key], orderBy));
      }

      return;
    }

    if (orderSort === 'ASC') {
      for (const key in map) {
        map[key] = sortBy(map[key], orderBy);
      }
    }
  }
};

const toGroup = (value: 'none' | 'assigneeName' | 'lastModifiedByName' | 'taskType') => {
  arrowOpenSet.value.clear();

  if (value === 'none') {
    if (props.orderBy) {
      sortData(props.orderBy, props.orderSort);
    }

    return;
  }

  groupDataMap.value = {};
  const list = taskList.value;
  for (let i = 0, len = list.length; i < len; i++) {
    setGroupData(list[i]);
  }

  setDefaultGroupData();
};

const setGroupData = (data: TaskInfo) => {
  const { status: { value: statusValue }, assigneeId = '-1', lastModifiedBy, taskType: { value: taskTypeValue } } = data;
  let key = '';
  if (props.groupKey === 'assigneeName') {
    key = assigneeId;
  } else if (props.groupKey === 'lastModifiedByName') {
    key = lastModifiedBy;
  } else if (props.groupKey === 'taskType') {
    key = taskTypeValue;
  }

  if (groupDataMap.value[key]) {
    if (groupDataMap.value[key][statusValue]) {
      groupDataMap.value[key][statusValue].push(data);
    } else {
      groupDataMap.value[key][statusValue] = [data];
    }
  } else {
    groupDataMap.value[key] = { [statusValue]: [data] };
  }
};

const setDefaultGroupData = () => {
  const _statusList = statusList.value.map(item => item.value);
  for (const key in groupDataMap.value) {
    const keys = Object.keys(groupDataMap.value[key]);
    for (let i = 0, len = _statusList.length; i < len; i++) {
      const _status = _statusList[i];
      if (!keys.includes(_statusList[i])) {
        groupDataMap.value[key][_status] = [];
      }
    }
  }
};

const toChecked = async (data: TaskInfo, index: number, groupByKey: string) => {
  if (data.id === checkedTaskInfo.value?.id) {
    selectedStatus.value = undefined;
    selectedIndex.value = undefined;
    selectedByGroupKey.value = undefined;
    drawerClose();
    return;
  }

  selectedStatus.value = data.status?.value;
  selectedIndex.value = index;
  selectedByGroupKey.value = groupByKey;
  checkedTaskInfo.value = { ...data };
  checkedSprintInfo.value = { id: data.sprintId, name: data.sprintName };
};

const drawerClose = () => {
  checkedTaskInfo.value = undefined;
  checkedSprintInfo.value = undefined;
};

const loadingChange = (value: boolean) => {
  emit('update:loading', value);
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

  if (taskDataMap.value[selectedStatus.value]?.[selectedIndex.value]) {
    taskDataMap.value[selectedStatus.value][selectedIndex.value] = _taskInfo;
  }

  if (groupDataMap.value[selectedByGroupKey.value]?.[selectedStatus.value]?.[selectedIndex.value]) {
    groupDataMap.value[selectedByGroupKey.value][selectedStatus.value][selectedIndex.value] = _taskInfo;
  }
};

const dropdownClick = (menuItem: ActionMenuItem, data: TaskInfo, index: number, status: TaskInfo['status']['value']) => {
  const key = menuItem.key;
  if (key === 'edit') {
    toEdit(data.id, index, status);
    return;
  }

  if (key === 'delete') {
    toDelete(data);
    return;
  }

  if (key === 'favourite') {
    toFavourite(data, index, status);
    return;
  }

  if (key === 'cancelFavourite') {
    toDeleteFavourite(data, index, status);
    return;
  }

  if (key === 'follow') {
    toFollow(data, index, status);
    return;
  }

  if (key === 'cancelFollow') {
    toDeleteFollow(data, index, status);
    return;
  }

  if (key === 'start') {
    toStart(data);
    return;
  }

  if (key === 'processed') {
    toProcessed(data);
    return;
  }

  if (key === 'completed') {
    toCompleted(data);
    return;
  }

  if (key === 'uncompleted') {
    toUncomplete(data);
    return;
  }

  if (key === 'reopen') {
    toReopen(data);
    return;
  }

  if (key === 'restart') {
    toRestart(data);
    return;
  }

  if (key === 'cancel') {
    toCancel(data);
    return;
  }

  if (key === 'move') {
    toMove(data);
  }
};

const toEdit = (id: string, index: number, status: TaskInfo['status']['value']) => {
  selectedTaskId.value = id;
  selectedIndex.value = index;
  selectedStatus.value = status;
  // 设置当前搜索条件中选中的迭代
  const item = props.filters.find(item => item.key === 'sprintId');
  searchSprintId.value = item?.value as string;
  taskModalVisible.value = true;
};

const editOk = async (data: TaskInfo) => {
  const index = selectedIndex.value as number;
  const status = selectedStatus.value as TaskInfo['status']['value'];
  taskDataMap.value[status][index] = data;

  selectedTaskId.value = undefined;
  selectedIndex.value = undefined;
  selectedStatus.value = undefined;
  searchSprintId.value = undefined;
};

const toDelete = (data: TaskInfo) => {
  modal.confirm({
    content: `确定删除任务【${data.name}】吗？`,
    async onOk () {
      emit('update:loading', true);
      const [error] = await task.deleteTask([data.id]);
      if (error) {
        emit('update:loading', false);
        return;
      }

      emit('refreshChange');
      notification.success('任务删除成功，您可以在回收站查看删除后的任务');
      await loadData();
      emit('update:loading', false);
    }
  });
};

const toFavourite = async (data: TaskInfo, index: number, status: TaskInfo['status']['value']) => {
  emit('update:loading', true);
  const [error] = await task.favouriteTask(data.id);
  emit('update:loading', false);
  if (error) {
    return;
  }

  notification.success('任务收藏成功');
  taskDataMap.value[status][index].favouriteFlag = true;
};

const toDeleteFavourite = async (data: TaskInfo, index: number, status: TaskInfo['status']['value']) => {
  emit('update:loading', true);
  const [error] = await task.cancelFavouriteTask(data.id);
  emit('update:loading', false);
  if (error) {
    return;
  }

  notification.success('任务取消收藏成功');
  taskDataMap.value[status][index].favouriteFlag = false;
};

const toFollow = async (data: TaskInfo, index: number, status: TaskInfo['status']['value']) => {
  emit('update:loading', true);
  const [error] = await task.followTask(data.id);
  emit('update:loading', false);
  if (error) {
    return;
  }

  notification.success('任务关注成功');
  taskDataMap.value[status][index].followFlag = true;
};

const toDeleteFollow = async (data: TaskInfo, index: number, status: TaskInfo['status']['value']) => {
  emit('update:loading', true);
  const [error] = await task.cancelFollowTask(data.id);
  emit('update:loading', false);
  if (error) {
    return;
  }

  notification.success('任务取消关注成功');
  taskDataMap.value[status][index].followFlag = false;
};

const toStart = async (data: TaskInfo, notificationFlag = true, errorCallback?: () => void) => {
  const id = data.id;
  emit('update:loading', true);
  const [error] = await task.startProcessing(id);
  emit('update:loading', false);
  if (error) {
    if (typeof errorCallback === 'function') {
      errorCallback();
    }
    return;
  }

  emit('refreshChange');
  if (notificationFlag) {
    notification.success('任务开始处理成功');
  }
  loadData();
};

const toProcessed = async (data: TaskInfo, notificationFlag = true, errorCallback?: () => void) => {
  const id = data.id;
  emit('update:loading', true);
  const [error] = await task.processedTask(id);
  emit('update:loading', false);
  if (error) {
    if (typeof errorCallback === 'function') {
      errorCallback();
    }
    return;
  }

  emit('refreshChange');
  if (notificationFlag) {
    notification.success('任务已处理成功');
  }
  loadData();
};

const toUncomplete = async (data: TaskInfo, notificationFlag = true, errorCallback?: () => void) => {
  const id = data.id;
  emit('update:loading', true);
  const [error] = await task.confirmTask(id, 'FAIL');
  emit('update:loading', false);
  if (error) {
    if (typeof errorCallback === 'function') {
      errorCallback();
    }
    return;
  }

  emit('refreshChange');
  loadData();
};

const toCompleted = async (data: TaskInfo, notificationFlag = true, errorCallback?: () => void) => {
  const id = data.id;
  emit('update:loading', true);
  const [error] = await task.confirmTask(id, 'SUCCESS');
  emit('update:loading', false);
  if (error) {
    if (typeof errorCallback === 'function') {
      errorCallback();
    }
    return;
  }

  emit('refreshChange');
  loadData();
};

const toReopen = async (data: TaskInfo, notificationFlag = true, errorCallback?: () => void) => {
  const id = data.id;
  emit('update:loading', true);
  const [error] = await task.reopenTask(id);
  emit('update:loading', false);
  if (error) {
    if (typeof errorCallback === 'function') {
      errorCallback();
    }
    return;
  }

  emit('refreshChange');
  if (notificationFlag) {
    notification.success('任务重新打开成功');
  }
  loadData();
};

const toRestart = async (data: TaskInfo, notificationFlag = true, errorCallback?: () => void) => {
  const id = data.id;
  emit('update:loading', true);
  const [error] = await task.restartTask(id);
  emit('update:loading', false);
  if (error) {
    if (typeof errorCallback === 'function') {
      errorCallback();
    }
    return;
  }

  emit('refreshChange');
  if (notificationFlag) {
    notification.success('任务重新开始成功');
  }
  loadData();
};

const toMove = (data: TaskInfo) => {
  selectedTaskSprintId.value = data.sprintId;
  selectedTaskName.value = data.name;
  selectedTaskId.value = data.id;
  moveModalVisible.value = true;
};

const moveTaskOk = () => {
  selectedTaskSprintId.value = undefined;
  selectedTaskName.value = undefined;
  selectedTaskId.value = undefined;
  emit('refreshChange');
  loadData();
};

const toCancel = async (data: TaskInfo, notificationFlag = true, errorCallback?: () => void) => {
  const id = data.id;
  const [error] = await task.cancelTask(id);
  if (error) {
    if (typeof errorCallback === 'function') {
      errorCallback();
    }
    return;
  }

  emit('refreshChange');
  if (notificationFlag) {
    notification.success('任务取消成功');
  }
  loadData();
};

const resetDrag = (id: string, index: number, status: TaskInfo['status']['value'], toStatus: TaskInfo['status']['value']) => {
  const _index = taskDataMap.value[toStatus].findIndex(item => item.id === id);
  const dragData = taskDataMap.value[toStatus][_index];
  // 删除已被添加的数据
  taskDataMap.value[toStatus].splice(_index, 1);
  // 移动数据重置设置到原位置
  taskDataMap.value[status].splice(index, 0, dragData);
};

const resetGroupDrag = (id: string, index: number, status: TaskInfo['status']['value'], toStatus: TaskInfo['status']['value'], groupKey: string) => {
  const _index = groupDataMap.value[groupKey][toStatus].findIndex(item => item.id === id);
  const dragData = groupDataMap.value[groupKey][toStatus][_index];
  // 删除已被添加的数据
  groupDataMap.value[groupKey][toStatus].splice(_index, 1);
  // 移动数据重置设置到原位置
  groupDataMap.value[groupKey][status].splice(index, 0, dragData);
};

const dragHandler = async (data: TaskInfo, status: TaskInfo['status']['value'], toStatus: TaskInfo['status']['value'], index: number, groupKey?: 'none' | 'assigneeName' | 'lastModifiedByName' | 'taskType') => {
  const { id, confirmorId } = data;
  if (status === 'PENDING') {
    if (toStatus === 'IN_PROGRESS') {
      const disabled = !!menuItemsMap.value.get(id)?.find(item => item.key === 'start')?.disabled;
      if (disabled) {
        if (groupKey) {
          resetGroupDrag(id, index, status, toStatus, groupKey);
        } else {
          resetDrag(id, index, status, toStatus);
        }
        notification.warning('没有开始处理权限');
        return;
      }
    } else if (toStatus === 'CANCELED') {
      const disabled = !!menuItemsMap.value.get(id)?.find(item => item.key === 'cancel')?.disabled;
      if (disabled) {
        if (groupKey) {
          resetGroupDrag(id, index, status, toStatus, groupKey);
        } else {
          resetDrag(id, index, status, toStatus);
        }
        notification.warning('没有取消任务权限');
        return;
      }
    } else {
      if (groupKey) {
        resetGroupDrag(id, index, status, toStatus, groupKey);
      } else {
        resetDrag(id, index, status, toStatus);
      }
      notification.warning('只能移动到进行中、已取消');
      return;
    }

    if (toStatus === 'IN_PROGRESS') {
      toStart(data, false, () => {
        if (groupKey) {
          resetGroupDrag(id, index, status, toStatus, groupKey);
        } else {
          resetDrag(id, index, status, toStatus);
        }
      });
      return;
    }

    if (toStatus === 'CANCELED') {
      toCancel(data, false, () => {
        if (groupKey) {
          resetGroupDrag(id, index, status, toStatus, groupKey);
        } else {
          resetDrag(id, index, status, toStatus);
        }
      });
      return;
    }

    return;
  }

  if (status === 'IN_PROGRESS') {
    if (toStatus === 'CONFIRMING') {
      if (!confirmorId) {
        if (groupKey) {
          resetGroupDrag(id, index, status, toStatus, groupKey);
        } else {
          resetDrag(id, index, status, toStatus);
        }
        notification.warning('该任务没有确认人，无需确认，只能移动到已完成、已取消');
        return;
      }

      const disabled = !!menuItemsMap.value.get(id)?.find(item => item.key === 'processed')?.disabled;
      if (disabled) {
        if (groupKey) {
          resetGroupDrag(id, index, status, toStatus, groupKey);
        } else {
          resetDrag(id, index, status, toStatus);
        }
        notification.warning('没有已处理权限');
        return;
      }
    } else if (toStatus === 'CANCELED') {
      const disabled = !!menuItemsMap.value.get(id)?.find(item => item.key === 'cancel')?.disabled;
      if (disabled) {
        if (groupKey) {
          resetGroupDrag(id, index, status, toStatus, groupKey);
        } else {
          resetDrag(id, index, status, toStatus);
        }
        notification.warning('没有取消任务权限');
        return;
      }
    } else if (toStatus === 'COMPLETED') {
      const disabled = !!menuItemsMap.value.get(id)?.find(item => item.key === 'completed')?.disabled;
      if (confirmorId) {
        if (groupKey) {
          resetGroupDrag(id, index, status, toStatus, groupKey);
        } else {
          resetDrag(id, index, status, toStatus);
        }
        notification.warning('只能移动到待确认、已取消');
        return;
      }

      if (disabled) {
        if (groupKey) {
          resetGroupDrag(id, index, status, toStatus, groupKey);
        } else {
          resetDrag(id, index, status, toStatus);
        }
        notification.warning('没有确认完成权限');
        return;
      }
    } else {
      if (groupKey) {
        resetGroupDrag(id, index, status, toStatus, groupKey);
      } else {
        resetDrag(id, index, status, toStatus);
      }

      if (confirmorId) {
        notification.warning('只能移动到待确认、已取消');
      } else {
        notification.warning('只能移动到已完成、已取消');
      }

      return;
    }

    if (toStatus === 'CONFIRMING') {
      toProcessed(data, false, () => {
        if (groupKey) {
          resetGroupDrag(id, index, status, toStatus, groupKey);
        } else {
          resetDrag(id, index, status, toStatus);
        }
      });
      return;
    }

    if (toStatus === 'COMPLETED') {
      toCompleted(data, false, () => {
        if (groupKey) {
          resetGroupDrag(id, index, status, toStatus, groupKey);
        } else {
          resetDrag(id, index, status, toStatus);
        }
      });
      return;
    }

    if (toStatus === 'CANCELED') {
      toCancel(data, false, () => {
        if (groupKey) {
          resetGroupDrag(id, index, status, toStatus, groupKey);
        } else {
          resetDrag(id, index, status, toStatus);
        }
      });
      return;
    }

    return;
  }

  if (status === 'CONFIRMING') {
    if (toStatus === 'COMPLETED') {
      const disabled = !!menuItemsMap.value.get(id)?.find(item => item.key === 'completed')?.disabled;
      if (disabled) {
        if (groupKey) {
          resetGroupDrag(id, index, status, toStatus, groupKey);
        } else {
          resetDrag(id, index, status, toStatus);
        }
        notification.warning('没有确认完成权限');
        return;
      }
    } else if (toStatus === 'CANCELED') {
      const disabled = !!menuItemsMap.value.get(id)?.find(item => item.key === 'cancel')?.disabled;
      if (disabled) {
        if (groupKey) {
          resetGroupDrag(id, index, status, toStatus, groupKey);
        } else {
          resetDrag(id, index, status, toStatus);
        }
        notification.warning('没有取消任务权限');
        return;
      }
    } else {
      if (groupKey) {
        resetGroupDrag(id, index, status, toStatus, groupKey);
      } else {
        resetDrag(id, index, status, toStatus);
      }
      notification.warning('只能移动到已完成、已取消');
      return;
    }

    if (toStatus === 'COMPLETED') {
      toCompleted(data, false, () => {
        if (groupKey) {
          resetGroupDrag(id, index, status, toStatus, groupKey);
        } else {
          resetDrag(id, index, status, toStatus);
        }
      });
      return;
    }

    if (toStatus === 'CANCELED') {
      toCancel(data, false, () => {
        if (groupKey) {
          resetGroupDrag(id, index, status, toStatus, groupKey);
        } else {
          resetDrag(id, index, status, toStatus);
        }
      });
      return;
    }

    return;
  }

  if (status === 'COMPLETED' || status === 'CANCELED') {
    if (toStatus === 'PENDING') {
      const disabled = !!menuItemsMap.value.get(id)?.find(item => item.key === 'reopen')?.disabled;
      if (disabled) {
        if (groupKey) {
          resetGroupDrag(id, index, status, toStatus, groupKey);
        } else {
          resetDrag(id, index, status, toStatus);
        }
        notification.warning('没有重新打开权限');
        return;
      }
    } else {
      if (groupKey) {
        resetGroupDrag(id, index, status, toStatus, groupKey);
      } else {
        resetDrag(id, index, status, toStatus);
      }
      notification.warning('只能移动到待处理');
      return;
    }

    toReopen(data, false, () => {
      if (groupKey) {
        resetGroupDrag(id, index, status, toStatus, groupKey);
      } else {
        resetDrag(id, index, status, toStatus);
      }
    });
  }
};

const dragAdd = async (event: { item: { id: string; }; }, toStatus: TaskInfo['status']['value']) => {
  const [status, index, id] = (event.item.id.split('-')) as [TaskInfo['status']['value'], number, string];
  const targetData = taskDataMap.value[toStatus].find(item => item.id === id);
  if (!targetData) {
    return;
  }

  dragHandler(targetData, status, toStatus, index);
};

const groupDragAdd = async (event: { item: { id: string; } }, toStatus: TaskInfo['status']['value']) => {
  const [status, index, id, groupKey] = (event.item.id.split('-')) as [TaskInfo['status']['value'], number, string, string];
  const targetData = groupDataMap.value[groupKey][toStatus].find(item => item.id === id);
  if (!targetData) {
    return;
  }

  dragHandler(targetData, status, toStatus, index, groupKey);
};

const dragMove = (event) => {
  // 设置正在拖动的列索引
  const [, toIndex] = event.to.id.split('-') as [TaskInfo['status']['value'], number];
  const [fromStatus] = event.from.id.split('-') as [TaskInfo['status']['value'], number];
  const [, , draggedId, confirmorId] = event.dragged.id.split('-') as [TaskInfo['status']['value'], number, string, string];
  const cancelDisabled = !!menuItemsMap.value.get(draggedId)?.find(item => item.key === 'cancel')?.disabled;
  if (fromStatus === 'PENDING') {
    isDraggingToColumnStatus.value = ['IN_PROGRESS'];
    if (!cancelDisabled) {
      isDraggingToColumnStatus.value.push('CANCELED');
    }
    isDraggingToColumn.value = +toIndex;
    return;
  }

  if (fromStatus === 'IN_PROGRESS') {
    if (confirmorId === '0') {
      isDraggingToColumnStatus.value = ['COMPLETED'];
    } else {
      isDraggingToColumnStatus.value = ['CONFIRMING'];
    }
    if (!cancelDisabled) {
      isDraggingToColumnStatus.value.push('CANCELED');
    }
    isDraggingToColumn.value = +toIndex;
    return;
  }

  if (fromStatus === 'CONFIRMING') {
    isDraggingToColumnStatus.value = ['COMPLETED'];
    if (!cancelDisabled) {
      isDraggingToColumnStatus.value.push('CANCELED');
    }
    isDraggingToColumn.value = +toIndex;
    return;
  }

  if (fromStatus === 'COMPLETED' || fromStatus === 'CANCELED') {
    isDraggingToColumnStatus.value = ['PENDING'];
    isDraggingToColumn.value = +toIndex;
  }
};

const dragStart = () => {
  // 开始拖动时清空高亮
  isDraggingToColumn.value = null;
  isDraggingToColumnStatus.value = [];
};

const dragEnd = () => {
  // 拖动结束时清空高亮
  isDraggingToColumn.value = null;
  isDraggingToColumnStatus.value = [];
};

const drawerActiveKeyChange = (key: 'basic' | 'person' | 'date' | 'comment' | 'activity' | 'tasks' | 'cases' | 'attachments' | 'remarks') => {
  drawerActiveKey.value = key;
};

const arrowOpenChange = (open: boolean, id: string) => {
  if (open) {
    arrowOpenSet.value.add(id);
    return;
  }

  arrowOpenSet.value.delete(id);
};

const toggleOpen = () => {
  openFlag.value = !openFlag.value;

  if (openFlag.value) {
    let list: string[] = [];
    if (props.groupKey === 'assigneeName') {
      list = assigneeNameList.value.map(item => item.value);
    } else if (props.groupKey === 'lastModifiedByName') {
      list = lastModifiedByNameList.value.map(item => item.value);
    } else if (props.groupKey === 'taskType') {
      list = taskTypeList.value.map(item => item.value);
    }

    for (let i = 0, len = list.length; i < len; i++) {
      arrowOpenSet.value.add(list[i]);
    }

    return;
  }

  arrowOpenSet.value.clear();
};

const resetData = () => {
  taskList.value = [];
  groupDataMap.value = {};
  numMap.value = {
    CANCELED: 0,
    COMPLETED: 0,
    CONFIRMING: 0,
    IN_PROGRESS: 0,
    PENDING: 0
  };
  taskDataMap.value = {
    CANCELED: [],
    COMPLETED: [],
    CONFIRMING: [],
    IN_PROGRESS: [],
    PENDING: []
  };

  assigneeNameList.value = [];
  lastModifiedByNameList.value = [];
  taskTypeList.value = [];
};

onMounted(() => {
  loadEnum();

  watch(() => props.projectId, (newValue) => {
    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: true });

  watch([() => props.filters, () => props.moduleId], () => {
    loadData();
  });

  watch([() => props.orderBy, () => props.orderSort], () => {
    toSort({ orderBy: props.orderBy, orderSort: props.orderSort });
  });

  watch(() => props.groupKey, () => {
    toGroup(props.groupKey);
  });
});

const showGroupData = computed(() => {
  if (props.groupKey === 'assigneeName') {
    return assigneeNameList.value;
  }

  if (props.groupKey === 'lastModifiedByName') {
    return lastModifiedByNameList.value;
  }

  if (props.groupKey === 'taskType') {
    return taskTypeList.value;
  }

  return [];
});

const checkedTaskId = computed(() => {
  return checkedTaskInfo?.value?.id;
});

const checkedTaskType = computed(() => {
  return checkedTaskInfo?.value?.taskType?.value;
});

const menuItemsMap = computed<Map<string, ActionMenuItem[]>>(() => {
  const map = new Map<string, ActionMenuItem[]>();
  for (const key in taskDataMap.value) {
    const list = taskDataMap.value[key] || [];
    for (let i = 0, len = list.length; i < len; i++) {
      const item = list[i];
      const status = item.status?.value;
      const sprintId = item.sprintId;
      const sprintAuthFlag = item.sprintAuthFlag;

      const permissions = sprintPermissionsMap.value.get(sprintId) || [];
      const { currentAssociateType, confirmorId, assigneeId } = item;

      const userId = props.userInfo?.id;
      const isAdministrator = !!currentAssociateType?.map(item => item.value).includes('SYS_ADMIN' || 'APP_ADMIN');
      const isConfirmor = confirmorId === userId;
      const isAssignee = assigneeId === userId;

      const menuItems: ActionMenuItem[] = [
        {
          name: '编辑',
          key: 'edit',
          icon: 'icon-shuxie',
          disabled: !isAdministrator && !permissions.includes('MODIFY_TASK') && sprintAuthFlag,
          hide: false
        },
        {
          name: '删除',
          key: 'delete',
          icon: 'icon-qingchu',
          disabled: !isAdministrator && !permissions.includes('DELETE_TASK') && sprintAuthFlag,
          hide: false
        },
        {
          name: '拆分',
          key: 'split',
          icon: 'icon-guanlianziyuan',
          disabled: !isAdministrator && !permissions.includes('MODIFY_TASK') && sprintAuthFlag,
          hide: true
        }
      ];

      if (status === 'PENDING') {
        menuItems.push({
          name: '开始处理',
          key: 'start',
          icon: 'icon-kaishi',
          disabled: !isAdministrator && !isAssignee,
          hide: false
        });
      }

      if (status === 'IN_PROGRESS') {
        menuItems.push({
          name: '已处理',
          key: 'processed',
          icon: 'icon-yichuli',
          disabled: !isAdministrator && !isAssignee,
          hide: false
        });
      }

      if (confirmorId) {
        if (status === 'CONFIRMING') {
          menuItems.push({
            name: '确认完成',
            key: 'completed',
            icon: 'icon-yiwancheng',
            disabled: !isAdministrator && !isConfirmor,
            hide: false
          });

          menuItems.push({
            name: '确认未完成',
            key: 'uncompleted',
            icon: 'icon-shibaiyuanyin',
            disabled: !isAdministrator && !isConfirmor,
            hide: false
          });
        }
      }

      if (status === 'CANCELED' || status === 'COMPLETED') {
        menuItems.push({
          name: '重新打开',
          key: 'reopen',
          icon: 'icon-zhongxindakaiceshirenwu',
          disabled: !isAdministrator && !permissions.includes('REOPEN_TASK') && !isAssignee,
          hide: false,
          tip: '将任务状态更新为`待处理`、 不清理统计计数和状态。'
        });

        menuItems.push({
          name: '重新开始',
          key: 'restart',
          icon: 'icon-zhongxinkaishiceshi',
          disabled: !isAdministrator && !permissions.includes('RESTART_TASK'),
          hide: false,
          tip: '将任务更新为`待处理`，相关统计计数和状态会被清除。'
        });
      }

      if (status !== 'CANCELED' && status !== 'COMPLETED') {
        menuItems.push({
          name: '取消',
          key: 'cancel',
          icon: 'icon-zhongzhi2',
          disabled: !isAdministrator && !permissions.includes('MODIFY_TASK') && sprintAuthFlag,
          hide: false
        });
      }

      const { favouriteFlag, followFlag } = item;
      if (favouriteFlag) {
        menuItems.push({
          name: '取消收藏',
          key: 'cancelFavourite',
          icon: 'icon-quxiaoshoucang',
          disabled: false,
          hide: false
        });
      } else {
        menuItems.push({
          name: '收藏',
          key: 'favourite',
          icon: 'icon-yishoucang',
          disabled: false,
          hide: false
        });
      }

      if (followFlag) {
        menuItems.push({
          name: '取消关注',
          key: 'cancelFollow',
          icon: 'icon-quxiaoguanzhu',
          disabled: false,
          hide: false
        });
      } else {
        menuItems.push({
          name: '关注',
          key: 'follow',
          icon: 'icon-yiguanzhu',
          disabled: false,
          hide: false
        });
      }

      menuItems.push({
        name: '移动',
        key: 'move',
        icon: 'icon-yidong',
        disabled: !isAdministrator && !permissions.includes('MODIFY_TASK') && sprintAuthFlag,
        hide: false
      });

      map.set(item.id, menuItems);
    }
  }

  return map;
});

</script>
<template>
  <div class="flex-1 leading-5 overflow-hidden">
    <div
      style="height:calc(100% - 14px);"
      class="flex items-start flex-nowrap overflow-hidden border-t border-solid border-theme-text-box">
      <div v-if="props.groupKey === 'none'" class="flex-1 flex flex-nowrap h-full overflow-hidden">
        <div
          v-for="(item, statusIndex) in statusList"
          :key="item.value"
          class="col-item h-full w-1/5 border-r border-solid border-theme-text-box overflow-hidden">
          <div class="flex items-center px-2.5 py-1.5 space-x-1.5 font-semibold head-container">
            <span>{{ item.message }}</span>
            <span>{{ numMap[item.value] }}</span>
          </div>

          <Draggable
            :id="`${item.value}-${statusIndex}`"
            :list="taskDataMap[item.value]"
            :animation="300"
            ghostClass="ghost"
            group="tasks"
            itemKey="id"
            tag="div"
            style="height: calc(100% - 32px);"
            :class="{ 'highlight-enabled': statusIndex === isDraggingToColumn, highlight: isDraggingToColumnStatus.includes(item.value) }"
            class="draggable-container overflow-y-auto scroll-smooth p-2"
            @move="dragMove"
            @start="dragStart"
            @end="dragEnd"
            @add="dragAdd($event, item.value)">
            <template #item="{ element, index }: { element: TaskInfo; index: number; }">
              <div
                :id="`${item.value}-${index}-${element.id}-${(element.confirmorId || '0')}`"
                :class="{ 'active-item': checkedTaskId === element.id }"
                class="task-board-item border border-solid rounded border-theme-text-box p-2 space-y-1.5"
                @click="toChecked(element, index)">
                <div class="flex items-center overflow-hidden">
                  <IconTask :value="element.taskType.value" class="mr-1.5" />
                  <span :title="element.name" class="flex-1 truncate font-semibold">{{ element.name }}</span>
                  <span
                    v-if="element.overdueFlag"
                    class="flex-shrink-0 border border-status-error rounded px-0.5"
                    style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
                    <span class="inline-block transform-gpu scale-90">已逾期</span>
                  </span>
                  <Dropdown
                    :menuItems="menuItemsMap.get(element.id)"
                    @click="dropdownClick($event, element, index, item.value)">
                    <Button
                      type="text"
                      size="small"
                      class="action-btn invisible flex items-center px-0 w-5">
                      <Icon icon="icon-more" />
                    </Button>
                  </Dropdown>
                </div>
                <div class="flex items-center space-x-3.5">
                  <Tooltip trigger="hover">
                    <template #title>
                      <div class="flex items-center overflow-hidden">
                        <span class="flex-shrink-0">经办人</span>
                        <Colon class="mr-1.5" />
                        <span :title="element.assigneeName" class="truncate">{{ element.assigneeName }}</span>
                      </div>
                    </template>
                    <div style="width:20px;" class="flex-shrink-0">
                      <Image
                        :src="element.assigneeAvatar"
                        type="avatar"
                        style="width:100%;border-radius: 50%;" />
                    </div>
                  </Tooltip>
                  <div class="truncate" :title="element.code">{{ element.code }}</div>
                  <TaskPriority class="flex-shrink-0" :value="element.priority" />
                </div>
                <div class="flex items-center overflow-hidden">
                  <span class="flex-shrink-0">截止</span>
                  <Colon class="mr-1.5" />
                  <span :title="element.deadlineDate" class="truncate">{{ element.deadlineDate }}</span>
                </div>
              </div>
            </template>
          </Draggable>
        </div>
      </div>

      <div v-else class="flex-1 h-full overflow-hidden">
        <div class="flex items-center flex-nowrap">
          <div
            class="w-50 flex-shrink-0 col-item border-r border-solid border-theme-text-box flex items-center px-2.5 py-1.5 space-x-1.5 head-container">
            <Tooltip trigger="hover">
              <template #title>
                <span v-if="!openFlag">全部展开</span>
                <span v-else>全部收起</span>
              </template>
              <Icon
                v-if="!openFlag"
                icon="icon-spread"
                class="text-3.5 cursor-pointer"
                @click="toggleOpen" />
              <Icon
                v-else
                icon="icon-shouqi"
                class="text-3.5 cursor-pointer"
                @click="toggleOpen" />
            </Tooltip>
            <span class="font-semibold">泳道</span>
          </div>
          <div
            v-for="_status in statusList"
            :key="_status.value"
            style="width:calc((100% - 200px)/5);"
            class="col-item border-r border-solid border-theme-text-box flex items-center px-2.5 py-1.5 space-x-1.5 font-semibold head-container">
            <span>{{ _status.message }}</span>
            <span>{{ numMap[_status.value] }}</span>
          </div>
        </div>
        <div style="height:calc(100% - 32px);" class="overflow-y-auto">
          <div
            v-for="_createdByName in showGroupData"
            :key="_createdByName.value"
            :class="{ 'h-full': arrowOpenSet.has(_createdByName.value) }"
            class="flex items-start flex-nowrap border-b border-solid border-theme-text-box overflow-x-hidden">
            <div class="w-50 flex-shrink-0 flex items-center justify-between px-2.5 py-3.5">
              <div class="flex items-center overflow-hidden">
                <Arrow
                  :open="arrowOpenSet.has(_createdByName.value)"
                  type="dashed"
                  class="flex-shrink-0 mr-1.5"
                  style="font-size:12px;"
                  @change="arrowOpenChange($event, _createdByName.value)" />
                <IconTask
                  v-if="props.groupKey === 'taskType'"
                  :value="_createdByName.value"
                  class="mr-1.5" />
                <div class="flex-1 truncate font-semibold" :title="_createdByName.name">{{ _createdByName.name }}</div>
                <div class="flex-shrink-0">
                </div>
              </div>
              <div class="flex items-center">
                <span>{{ Object.values(groupDataMap[_createdByName.value] || {}).reduce((prev, cur) => prev +
                  cur.length, 0)
                }}</span>
                <span>个任务</span>
              </div>
            </div>
            <div class="relative h-full flex items-start" style="width: calc(100% - 193px);">
              <template v-if="!arrowOpenSet.has(_createdByName.value)">
                <div
                  v-for="_status in statusList"
                  :key="_status.value"
                  style="width:20%;"
                  class="flex items-center px-2.5 py-3.5 space-x-1.5">
                  <span>{{ _status.message }}</span>
                  <span>{{ groupDataMap[_createdByName.value]?.[_status.value]?.length || 0 }}</span>
                </div>
              </template>
              <AsyncComponent :visible="arrowOpenSet.has(_createdByName.value)">
                <Draggable
                  v-for="(_status, statusIndex) in statusList"
                  v-show="arrowOpenSet.has(_createdByName.value)"
                  :id="`${_status.value}-${statusIndex}`"
                  :key="_status.value"
                  style="width:20%;height: 100%;"
                  :list="(groupDataMap[_createdByName.value]?.[_status.value] || [])"
                  :animation="300"
                  :group="`tasks-${_createdByName.value}`"
                  :class="{ 'highlight-enabled': statusIndex === isDraggingToColumn, highlight: isDraggingToColumnStatus.includes(_status.value) }"
                  class="draggable-container right-border relative overflow-y-auto scroll-smooth space-y-2 px-2 py-2"
                  ghostClass="ghost"
                  itemKey="id"
                  tag="div"
                  @move="dragMove"
                  @start="dragStart"
                  @end="dragEnd"
                  @add="groupDragAdd($event, _status.value)">
                  <template #item="{ element, index }: { element: TaskInfo; index: number; }">
                    <div
                      :id="`${_status.value}-${index}-${element.id}-${(element.confirmorId || '0')}-${_createdByName.value}`"
                      :class="{ 'active-item': checkedTaskId === element.id }"
                      class="task-board-item border border-solid rounded border-theme-text-box p-2 space-y-1.5"
                      @click="toChecked(element, statusIndex, _createdByName.value)">
                      <div class="flex items-center overflow-hidden">
                        <IconTask :value="element.taskType.value" class="mr-1.5" />
                        <span :title="element.name" class="flex-1 truncate font-semibold">{{ element.name }}</span>
                        <span
                          v-if="element.overdueFlag"
                          class="flex-shrink-0 border border-status-error rounded px-0.5"
                          style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
                          <span class="inline-block transform-gpu scale-90">已逾期</span>
                        </span>
                        <Dropdown
                          :menuItems="menuItemsMap.get(element.id)"
                          @click="dropdownClick($event, element, index, _status.value)">
                          <Button
                            type="text"
                            size="small"
                            class="action-btn invisible flex items-center px-0 w-5">
                            <Icon icon="icon-more" />
                          </Button>
                        </Dropdown>
                      </div>
                      <div class="flex items-center space-x-3.5">
                        <Tooltip trigger="hover">
                          <template #title>
                            <div class="flex items-center overflow-hidden">
                              <span class="flex-shrink-0">经办人</span>
                              <Colon class="mr-1.5" />
                              <span :title="element.assigneeName" class="truncate">{{ element.assigneeName }}</span>
                            </div>
                          </template>
                          <div style="width:20px;" class="flex-shrink-0">
                            <Image
                              :src="element.assigneeAvatar"
                              type="avatar"
                              style="width:100%;border-radius: 50%;" />
                          </div>
                        </Tooltip>
                        <div class="truncate" :title="element.code">{{ element.code }}</div>
                        <TaskPriority class="flex-shrink-0" :value="element.priority" />
                      </div>
                      <div class="flex items-center overflow-hidden">
                        <span class="flex-shrink-0">截止</span>
                        <Colon class="mr-1.5" />
                        <span :title="element.deadlineDate" class="truncate">{{ element.deadlineDate }}</span>
                      </div>
                    </div>
                  </template>
                </Draggable>
              </AsyncComponent>
            </div>
          </div>
        </div>
      </div>

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

    <AsyncComponent :visible="moveModalVisible">
      <MoveTaskModal
        v-model:visible="moveModalVisible"
        :sprintId="selectedTaskSprintId"
        :taskIds="selectedTaskId"
        :taskName="selectedTaskName"
        :projectId="props.projectId"
        @ok="moveTaskOk" />
    </AsyncComponent>

    <AsyncComponent :visible="taskModalVisible">
      <EditTaskModal
        v-model:visible="taskModalVisible"
        v-model:taskId="selectedTaskId"
        :sprintId="searchSprintId"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        @ok="editOk" />
    </AsyncComponent>
  </div>
</template>

<style scoped>
.head-container {
  background-color: #f4f5f7;
}

.task-board-item {
  cursor: move
}

.task-board-item:hover {
  box-shadow: 0 3px 6px #0000000a;
}

.task-board-item:hover .action-btn.invisible {
  visibility: visible;
}

.active-item.task-board-item {
  background-color: #f4f5f7;
}

.col-item:last-child {
  border: 0;
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

.draggable-container {
  height: 100%;
}

.draggable-container>*~* {
  margin-top: 8px;
}

.draggable-container.right-border::after {
  /* content: '';
  display:block;
  position: absolute;
  top:0 ;
  right: 0;
  width:1px;
  height: 100%;
  background-color: var(--border-text-box); */
}

.highlight {
  border: 1px dashed rgba(188, 198, 207, 100%);
  background-color: rgba(245, 246, 246, 100%);
}

.highlight.highlight-enabled {
  border: 1px dashed rgba(70, 166, 255, 100%);
  background-color: rgba(70, 166, 255, 10%);
}
</style>
