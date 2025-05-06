<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
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
import Draggable from 'vuedraggable';
import dayjs from 'dayjs';
import { reverse, sortBy } from 'lodash-es';
import { funcCase, funcPlan } from '@/api/tester';

import { ActionMenuItem, CaseInfo, PlanPermissionKey, TestResult } from './PropsType';
import { userInfo } from 'os';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  filters: { key: string; op: string; value: boolean | string | string[]; }[];
  notify: string;
  moduleId: string;
  groupKey: 'none' | 'testerName' | 'lastModifiedByName';
  orderBy: 'priority' | 'deadlineDate' | 'createdByName' | 'testerName';
  orderSort: 'DESC' | 'ASC';
};

const props = withDefaults(defineProps<Props>(), {
  filters: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  moduleId: undefined,
  groupKey: 'none',
  orderBy: undefined,
  orderSort: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'refreshChange'): void;
}>();

const EditTaskModal = defineAsyncComponent(() => import('@/views/function/case/list/case/add/index.vue'));
const MoveModal = defineAsyncComponent(() => import('@/views/function/case/list/case/move/index.vue'));
const UpdateResultModal = defineAsyncComponent(() => import('@/views/function/case/list/case/updateResult/index.vue'));
const AddTaskModal = defineAsyncComponent(() => import('@/views/task/task/list/task/edit/index.vue'));
const BasicInfo = defineAsyncComponent(() => import('@/views/function/case/list/case/kanban/info/basic/index.vue'));
const TestSteps = defineAsyncComponent(() => import('@/views/function/case/list/case/kanban/info/testSteps/index.vue'));
const PersonnelInfo = defineAsyncComponent(() => import('@/views/function/case/list/case/kanban/info/personnel/index.vue'));
const DateInfo = defineAsyncComponent(() => import('@/views/function/case/list/case/kanban/info/date/index.vue'));
const ReviewInfo = defineAsyncComponent(() => import('@/views/function/case/list/case/kanban/reviewInfo/index.vue'));
const TestInfo = defineAsyncComponent(() => import('@/views/function/case/list/case/kanban/info/testInfo/index.vue'));
const AssocTask = defineAsyncComponent(() => import('@/views/function/case/list/case/kanban/assocTask/index.vue'));
const AssocCase = defineAsyncComponent(() => import('@/views/function/case/list/case/kanban/assocCase/index.vue'));
const AttachmentInfo = defineAsyncComponent(() => import('@/views/function/case/list/case/kanban/info/attachment/index.vue'));
const ReviewRecord = defineAsyncComponent(() => import('@/views/function/case/list/case/kanban/reviewRecord/index.vue'));
const Comment = defineAsyncComponent(() => import('@/views/function/case/list/case/kanban/comment/index.vue'));
const Activity = defineAsyncComponent(() => import('@/views/function/case/list/case/kanban/activity/index.vue'));

const isAdmin = inject('isAdmin', ref(false));

const drawerActiveKey = ref<'basic' | 'testStep' | 'person' | 'date' | 'comment' | 'activity' | 'refTasks' | 'refCases' | 'attachments' | 'remarks'>('basic');

const planPermissionsMap = ref<Map<string, PlanPermissionKey[]>>(new Map());
const planAuthFlagMap = ref({});

const testResultList = ref<{ message: string; value: TestResult }[]>([]);
const caseList = ref<CaseInfo[]>([]);
const caseDataMap = ref<{ [key in TestResult]: CaseInfo[] }>({
  PENDING: [],
  PASSED: [],
  NOT_PASSED: [],
  BLOCKED: [],
  CANCELED: []
});
const numMap = ref<{ [key in TestResult]: number }>({
  PENDING: 0,
  PASSED: 0,
  NOT_PASSED: 0,
  BLOCKED: 0,
  CANCELED: 0
});

const testerNameList = ref<{ name: string; value: string }[]>([]);
const lastModifiedByNameList = ref<{ name: string; value: string }[]>([]);
const groupDataMap = ref<{ [key: string]: { [key in TestResult]: CaseInfo[] } }>({});

const isDraggingToColumn = ref<number|null>(null);
const isDraggingToColumnTestResult = ref<TestResult[]>([]);

const openFlag = ref(false);
const arrowOpenSet = ref(new Set<string>());

const checkedCaseInfo = ref<CaseInfo>();
const checkedPlanInfo = ref<{ id: string; name: string; }>();
const checkedGroupKey = ref<string>();

const selectedIndex = ref<number>();
const selectedTestResult = ref<TestResult>();
const selectedCaseInfo = ref<CaseInfo>();

// 拖动到确认完成、确认未完成相关数据，用于取消确认完成、取消确认未完成时重置数据
const selectedToTestResult = ref<TestResult>();
const selectedGroupKey = ref<'none' | 'testerName' | 'lastModifiedByName'>();

const caseModalVisible = ref(false);
const moveModalVisible = ref(false);
const updateTestResultVisible = ref(false);
const taskModalVisible = ref(false);

const resultPassed = ref(false);

const loadEnum = async () => {
  const [error, res] = await enumLoader.load('CaseTestResult');
  if (error) {
    return;
  }

  testResultList.value = (res || []) as { message: string; value: TestResult }[];
};

const loadData = async () => {
  const params = getParams();
  emit('loadingChange', true);
  const [error, res] = await funcCase.loadFuncCase({ infoScope: 'DETAIL', ...params });
  if (error) {
    resetData();
    emit('loadingChange', false);
    return;
  }

  resetData();

  const { list, total } = (res?.data || { total: 0, list: [] });
  const len = list.length;
  const _caseList: CaseInfo[] = [];
  _caseList.push(...list);
  if (len < +total) {
    const pages = Math.ceil((total - len) / params.pageSize);
    for (let i = 0, len = pages; i < len; i++) {
      const pageNo = i + 2;
      const _params = { ...params, pageNo };
      const [_error, _res] = await funcCase.loadFuncCase({infoScope: 'DETAIL', ..._params});
      if (_error) {
        emit('loadingChange', false);
        return;
      }

      const { list: _list } = (_res?.data || { total: 0, list: [] });
      _caseList.push(..._list);
    }
  }

  // 保存原始数据
  caseList.value = _caseList;

  const newList: CaseInfo[] = [];
  const planIdSet = new Set<string>();
  const testerNameSet = new Set<string>();
  const lastModifiedByNameSet = new Set<string>();
  for (let i = 0, len = _caseList.length; i < len; i++) {
    const item = _caseList[i];
    const testResult = item.testResult?.value;
    numMap.value[testResult] += 1;
    caseDataMap.value[testResult].push(item);
    newList.push(item);
    planIdSet.add(item.planId);

    if (!testerNameSet.has(item.testerName)) {
      if (!item.testerName) {
        testerNameList.value.unshift({
          name: '未分组',
          value: '-1'
        });
      } else {
        testerNameList.value.push({
          name: item.testerName,
          value: item.testerId
        });
      }
    }

    if (!lastModifiedByNameSet.has(item.lastModifiedByName)) {
      lastModifiedByNameList.value.push({
        name: item.lastModifiedByName,
        value: item.lastModifiedBy
      });
    }

    testerNameSet.add(item.testerName);
    lastModifiedByNameSet.add(item.lastModifiedByName);

    if (props.groupKey !== 'none') {
      setGroupData(item);
    }
  }

  if (props.groupKey !== 'none') {
    setDefaultGroupData();
  }

  planPermissionsMap.value.clear();
  const sprintIds = Array.from(planIdSet);
  // 管理员拥有所有权限，无需加载权限
  if (!isAdmin.value) {
    const [error, { data }] = await funcPlan.getCurrentAuth({
      ids: sprintIds,
      adminFlag: true
    });
    if (error) {
      return;
    }
    for (let i = 0, len = sprintIds.length; i < len; i++) {
      const id = sprintIds[i];
      planPermissionsMap.value.set(id, (data[id]?.permissions || []).map(i => i.value));
      planAuthFlagMap.value[id] = data[id].funcPlanAuthFlag;
    }
  } else {
    for (let i = 0, len = sprintIds.length; i < len; i++) {
      const id = sprintIds[i];
      planAuthFlagMap.value[id] = true;
      planPermissionsMap.value.set(id, [
        'ADD',
        'VIEW',
        'MODIFY',
        'DELETE',
        'GRANT',
        'ADD_PLAN',
        'MODIFY_PLAN',
        'DELETE_PLAN',
        'ADD_CASE',
        'MODIFY_CASE',
        'DELETE_CASE',
        'EXPORT_CASE',
        'REVIEW',
        'RESET_REVIEW_RESULT',
        'TEST',
        'RESET_TEST_RESULT'
      ]);
    }
  }

  emit('loadingChange', false);
};

const getParams = () => {
  const params: {
    projectId: string;
    pageNo: number;
    pageSize: number;
    infoScope: 'DETAIL';
    moduleId?: string;
    filters?: { key: string; op: string; value: boolean | string | string[]; }[];
  } = {
    projectId: props.projectId,
    pageNo: 1,
    pageSize: 500,
    infoScope: 'DETAIL'
  };

  if (props.filters?.length) {
    params.filters = props.filters;
  }

  if (props.moduleId) {
    params.moduleId = props.moduleId;
  }

  return params;
};

const setGroupData = (data:CaseInfo) => {
  const { testResult: { value: testResultValue }, testerId = '-1', lastModifiedBy } = data;
  let key = '';
  if (props.groupKey === 'testerName') {
    key = testerId;
  } else if (props.groupKey === 'lastModifiedByName') {
    key = lastModifiedBy;
  }

  if (groupDataMap.value[key]) {
    if (groupDataMap.value[key][testResultValue]) {
      groupDataMap.value[key][testResultValue].push(data);
    } else {
      groupDataMap.value[key][testResultValue] = [data];
    }
  } else {
    groupDataMap.value[key] = { [testResultValue]: [data] };
  }
};

const setDefaultGroupData = () => {
  const _testResultList = testResultList.value.map(item => item.value);
  for (const key in groupDataMap.value) {
    const keys = Object.keys(groupDataMap.value[key]);
    for (let i = 0, len = _testResultList.length; i < len; i++) {
      const _testResult = _testResultList[i];
      if (!keys.includes(_testResultList[i])) {
        groupDataMap.value[key][_testResult] = [];
      }
    }
  }
};

const dragMove = (event) => {
  // 设置正在拖动的列索引
  const [, toIndex] = event.to.id.split('-') as [TestResult, number];
  const [fromResult] = event.from.id.split('-') as [TestResult, number];
  const [, , draggedId] = event.dragged.id.split('-') as [TestResult, number, string];
  let cancelDisabled = false;
  const cancelItem = menuItemsMap.value.get(draggedId)?.find(item => item.key === 'cancel');
  if (!cancelItem) {
    cancelDisabled = true;
  } else {
    cancelDisabled = !!cancelItem?.disabled;
  }

  if (fromResult === 'PENDING') {
    isDraggingToColumnTestResult.value = ['PASSED', 'NOT_PASSED', 'BLOCKED'];
    if (!cancelDisabled) {
      isDraggingToColumnTestResult.value.push('CANCELED');
    }
    isDraggingToColumn.value = +toIndex;
    return;
  }

  if (fromResult === 'PASSED' || fromResult === 'NOT_PASSED') {
    isDraggingToColumnTestResult.value = ['PENDING'];
    if (!cancelDisabled) {
      isDraggingToColumnTestResult.value.push('CANCELED');
    }
    isDraggingToColumn.value = +toIndex;
    return;
  }

  if (fromResult === 'BLOCKED') {
    isDraggingToColumnTestResult.value = ['PASSED', 'NOT_PASSED'];
    if (!cancelDisabled) {
      isDraggingToColumnTestResult.value.push('CANCELED');
    }
    isDraggingToColumn.value = +toIndex;
    return;
  }

  if (fromResult === 'CANCELED') {
    isDraggingToColumnTestResult.value = ['PENDING'];
    isDraggingToColumn.value = +toIndex;
  }
};

const dragStart = () => {
  // 开始拖动时清空高亮
  isDraggingToColumn.value = null;
  isDraggingToColumnTestResult.value = [];
};

const dragEnd = () => {
  // 拖动结束时清空高亮
  isDraggingToColumn.value = null;
  isDraggingToColumnTestResult.value = [];
};

const dragHandler = (data:CaseInfo, testResult:TestResult, toTestResult:TestResult, index:number, groupKey?:'none' | 'testerName' | 'lastModifiedByName') => {
  const { reviewFlag, reviewStatus: { value: reviewStatus }, planId, id } = data;
  const permissions = planPermissionsMap.value.get(planId) || [];
  if (testResult === 'PENDING') {
    if (reviewFlag) {
      if (reviewStatus === 'PENDING') {
        if (groupKey) {
          resetGroupDrag(id, index, testResult, toTestResult, groupKey);
        } else {
          resetDrag(id, index, testResult, toTestResult);
        }
        notification.warning('用例未评审，不能修改测试结果');
        return;
      }

      if (reviewStatus === 'FAILED') {
        if (groupKey) {
          resetGroupDrag(id, index, testResult, toTestResult, groupKey);
        } else {
          resetDrag(id, index, testResult, toTestResult);
        }
        notification.warning('用例评审未通过，不能修改测试结果');
        return;
      }

      if (!isAdmin && !permissions.includes('TEST')) {
        notification.warning('没有测试权限');
        return;
      }
    }

    if (toTestResult === 'PASSED') {
      selectedCaseInfo.value = data;
      selectedIndex.value = index;
      selectedGroupKey.value = groupKey;
      selectedTestResult.value = testResult;
      selectedToTestResult.value = toTestResult;
      toPassed(data);
      return;
    }

    if (toTestResult === 'NOT_PASSED') {
      selectedCaseInfo.value = data;
      selectedIndex.value = index;
      selectedGroupKey.value = groupKey;
      selectedTestResult.value = testResult;
      selectedToTestResult.value = toTestResult;
      toNotPassed(data);
      return;
    }

    if (toTestResult === 'BLOCKED') {
      toBlock(data, false, () => {
        if (groupKey) {
          resetGroupDrag(id, index, testResult, toTestResult, groupKey);
        } else {
          resetDrag(id, index, testResult, toTestResult);
        }
      });
      return;
    }

    if (toTestResult === 'CANCELED') {
      toCancel(data, false, () => {
        if (groupKey) {
          resetGroupDrag(id, index, testResult, toTestResult, groupKey);
        } else {
          resetDrag(id, index, testResult, toTestResult);
        }
      });
      return;
    }

    return;
  }

  if (testResult === 'PASSED' || testResult === 'NOT_PASSED' || testResult === 'CANCELED') {
    if (toTestResult !== 'PENDING') {
      if (groupKey) {
        resetGroupDrag(id, index, testResult, toTestResult, groupKey);
      } else {
        resetDrag(id, index, testResult, toTestResult);
      }
      notification.warning('只能移动到待测试');
      return;
    } else {
      if (!isAdmin && !permissions.includes('TEST')) {
        if (groupKey) {
          resetGroupDrag(id, index, testResult, toTestResult, groupKey);
        } else {
          resetDrag(id, index, testResult, toTestResult);
        }
        notification.warning('没有测试权限');
        return;
      }
    }

    toRetest(data, false, () => {
      if (groupKey) {
        resetGroupDrag(id, index, testResult, toTestResult, groupKey);
      } else {
        resetDrag(id, index, testResult, toTestResult);
      }
    });
    return;
  }

  if (testResult === 'BLOCKED') {
    if (toTestResult !== 'PASSED' && toTestResult !== 'NOT_PASSED' && toTestResult !== 'CANCELED') {
      if (groupKey) {
        resetGroupDrag(id, index, testResult, toTestResult, groupKey);
      } else {
        resetDrag(id, index, testResult, toTestResult);
      }
      notification.warning('只能移动到测试通过、测试未通过、已取消');
      return;
    } else {
      if (!isAdmin && !permissions.includes('TEST')) {
        if (groupKey) {
          resetGroupDrag(id, index, testResult, toTestResult, groupKey);
        } else {
          resetDrag(id, index, testResult, toTestResult);
        }
        notification.warning('没有测试权限');
        return;
      }
    }

    if (toTestResult === 'PASSED') {
      selectedCaseInfo.value = data;
      selectedIndex.value = index;
      selectedGroupKey.value = groupKey;
      selectedTestResult.value = testResult;
      selectedToTestResult.value = toTestResult;
      toPassed(data);
      return;
    }

    if (toTestResult === 'NOT_PASSED') {
      selectedCaseInfo.value = data;
      selectedIndex.value = index;
      selectedGroupKey.value = groupKey;
      selectedTestResult.value = testResult;
      selectedToTestResult.value = toTestResult;
      toNotPassed(data);
      return;
    }

    if (toTestResult === 'CANCELED') {
      toCancel(data, false, () => {
        if (groupKey) {
          resetGroupDrag(id, index, testResult, toTestResult, groupKey);
        } else {
          resetDrag(id, index, testResult, toTestResult);
        }
      });
    }
  }
};

const dragAdd = async (event: { item: { id: string; }; }, toTestResult: TestResult) => {
  const [testResult, index, id] = (event.item.id.split('-')) as [TestResult, number, string];
  const targetData = caseDataMap.value[toTestResult].find(item => item.id === id);
  if (!targetData) {
    return;
  }

  dragHandler(targetData, testResult, toTestResult, index);
};

const groupDragAdd = async (event: { item: { id: string; } }, toTestResult: TestResult) => {
  const [testResult, index, id, groupKey] = (event.item.id.split('-')) as [TestResult, number, string, string];
  const targetData = groupDataMap.value[groupKey][toTestResult].find(item => item.id === id);
  if (!targetData) {
    return;
  }

  dragHandler(targetData, testResult, toTestResult, index, groupKey);
};

const resetDrag = (id: string, index: number, testResult: TestResult, toTestResult: TestResult) => {
  const _index = caseDataMap.value[toTestResult].findIndex(item => item.id === id);
  const dragData = caseDataMap.value[toTestResult][_index];
  // 删除已被添加的数据
  caseDataMap.value[toTestResult].splice(_index, 1);
  // 移动数据重置设置到原位置
  caseDataMap.value[testResult].splice(index, 0, dragData);
};

const resetGroupDrag = (id: string, index: number, testResult: TestResult, toTestResult: TestResult, groupKey: string) => {
  const _index = groupDataMap.value[groupKey][toTestResult].findIndex(item => item.id === id);
  const dragData = groupDataMap.value[groupKey][toTestResult][_index];
  // 删除已被添加的数据
  groupDataMap.value[groupKey][toTestResult].splice(_index, 1);
  // 移动数据重置设置到原位置
  groupDataMap.value[groupKey][testResult].splice(index, 0, dragData);
};

const toSort = (data: { orderBy: 'priority' | 'deadlineDate' | 'createdByName' | 'testerName'; orderSort: 'DESC' | 'ASC'; }) => {
  sortData(data.orderBy, data.orderSort);
};

const sortData = (orderBy: 'priority' | 'deadlineDate' | 'createdByName' | 'testerName', orderSort: 'DESC' | 'ASC') => {
  const map = caseDataMap.value;
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

  if (orderBy === 'testerName' || orderBy === 'createdByName') {
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

const toGroup = (value: 'none' | 'testerName' | 'lastModifiedByName') => {
  arrowOpenSet.value.clear();

  if (value === 'none') {
    if (props.orderBy) {
      sortData(props.orderBy, props.orderSort);
    }

    return;
  }

  groupDataMap.value = {};
  const list = caseList.value;
  for (let i = 0, len = list.length; i < len; i++) {
    setGroupData(list[i]);
  }

  setDefaultGroupData();
};

const toChecked = async (caseInfo: CaseInfo, index:number, groupKey:string) => {
  const id = caseInfo.id;
  if (id === checkedCaseInfo.value?.id) {
    drawerClose();
    return;
  }

  if (!id) {
    return;
  }

  loadingChange(true);
  const [error, res] = await funcCase.getCaseInfo(id);
  loadingChange(false);
  if (error) {
    return;
  }

  const data = res?.data || {};
  checkedCaseInfo.value = data;
  checkedPlanInfo.value = { id: data.planId, name: data.planName };
  checkedGroupKey.value = groupKey;
  selectedIndex.value = index;
  selectedTestResult.value = data.testResult?.value;
};

const drawerClose = () => {
  checkedCaseInfo.value = undefined;
  checkedPlanInfo.value = undefined;
  checkedGroupKey.value = undefined;
  selectedTestResult.value = undefined;
  selectedIndex.value = undefined;
};

const dropdownClick = (menuItem: ActionMenuItem, data: CaseInfo, index: number, testResult: TestResult) => {
  const key = menuItem.key;
  if (key === 'edit') {
    toEdit(data, index, testResult);
    return;
  }

  if (key === 'delete') {
    toDelete(data);
    return;
  }

  if (key === 'favourite') {
    toFavourite(data, index, testResult);
    return;
  }

  if (key === 'cancelFavourite') {
    toDeleteFavourite(data, index, testResult);
    return;
  }

  if (key === 'follow') {
    toFollow(data, index, testResult);
    return;
  }

  if (key === 'cancelFollow') {
    toDeleteFollow(data, index, testResult);
    return;
  }

  if (key === 'clone') {
    toClone(data);
    return;
  }

  if (key === 'move') {
    toMove(data);
    return;
  }

  if (key === 'testPassed') {
    toPassed(data);
    return;
  }

  if (key === 'testNotPassed') {
    toNotPassed(data);
    return;
  }

  if (key === 'retest') {
    toRetest(data);
    return;
  }

  if (key === 'resetTestResult') {
    toResetTestResult(data);
    return;
  }

  if (key === 'block') {
    toBlock(data);
    return;
  }

  if (key === 'cancel') {
    toCancel(data);
    return;
  }

  if (key === 'addBug') {
    toAddBug(data, index, testResult);
  }
};

const toEdit = (data: CaseInfo, index: number, testResult: TestResult) => {
  selectedCaseInfo.value = data;
  selectedIndex.value = index;
  selectedTestResult.value = testResult;
  caseModalVisible.value = true;
};

const editOk = async (id:string) => {
  const index = selectedIndex.value as number;
  const testResult = selectedTestResult.value as TestResult;

  emit('loadingChange', true);
  const [, res] = await funcCase.getCaseInfo(id);
  emit('loadingChange', false);
  if (res) {
    caseDataMap.value[testResult][index] = res.data;
  }

  selectedCaseInfo.value = undefined;
  selectedIndex.value = undefined;
  selectedTestResult.value = undefined;
};

const toDelete = (data: CaseInfo) => {
  modal.confirm({
    content: `确定删除用例【${data.name}】吗？`,
    async onOk () {
      emit('loadingChange', true);
      const [error] = await funcCase.deleteCase( [data.id]);
      if (error) {
        emit('loadingChange', false);
        return;
      }

      emit('refreshChange');
      notification.success('用例删除成功，您可以在回收站查看删除后的用例');
      await loadData();
      emit('loadingChange', false);
    }
  });
};

const toFavourite = async (data: CaseInfo, index: number, testResult: TestResult) => {
  emit('loadingChange', true);
  const [error] = await funcCase.favouriteCase(data.id);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  notification.success('用例收藏成功');
  caseDataMap.value[testResult][index].favouriteFlag = true;
};

const toDeleteFavourite = async (data: CaseInfo, index: number, testResult: TestResult) => {
  emit('loadingChange', true);
  const [error] = await funcCase.cancelFavouriteCase(data.id);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  notification.success('用例取消收藏成功');
  caseDataMap.value[testResult][index].favouriteFlag = false;
};

const toFollow = async (data: CaseInfo, index: number, testResult: TestResult) => {
  emit('loadingChange', true);
  const [error] = await funcCase.followCase(data.id);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  notification.success('用例关注成功');
  caseDataMap.value[testResult][index].followFlag = true;
};

const toDeleteFollow = async (data: CaseInfo, index: number, testResult: TestResult) => {
  emit('loadingChange', true);
  const [error] = await funcCase.cancelFollowCase(data.id);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  notification.success('用例取消关注成功');
  caseDataMap.value[testResult][index].followFlag = false;
};

const toClone = async (data: CaseInfo) => {
  const id = data.id;
  emit('loadingChange', true);
  const [error] = await funcCase.cloneCase([id]);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success('用例克隆成功');
  loadData();
};

const toMove = (data: CaseInfo) => {
  selectedCaseInfo.value = data;
  moveModalVisible.value = true;
};

const moveOk = () => {
  selectedCaseInfo.value = undefined;
  emit('refreshChange');
  loadData();
};

const toPassed = async (data: CaseInfo) => {
  selectedCaseInfo.value = data;
  updateTestResultVisible.value = true;
  resultPassed.value = true;
};

const toNotPassed = async (data: CaseInfo) => {
  selectedCaseInfo.value = data;
  updateTestResultVisible.value = true;
  resultPassed.value = false;
};

const testOk = async () => {
  selectedCaseInfo.value = undefined;
  resultPassed.value = false;
  emit('refreshChange');
  notification.success('测试结果修改成功');
  loadData();
};

const updateTestResultCancel = () => {
  const id = selectedCaseInfo.value?.id;
  const index = selectedIndex.value;
  const testResult = selectedTestResult.value;
  const toTestResult = selectedToTestResult.value;
  if (!id || (!index && index !== 0) || !testResult || !toTestResult) {
    return;
  }

  if (selectedGroupKey.value) {
    resetGroupDrag(id, index, testResult, toTestResult, selectedGroupKey.value);
  } else {
    resetDrag(id, index, testResult, toTestResult);
  }

  resultPassed.value = false;
  selectedCaseInfo.value = undefined;
  selectedIndex.value = undefined;
  selectedGroupKey.value = undefined;
  selectedTestResult.value = undefined;
  selectedToTestResult.value = undefined;
};

const toAddBug = (data: CaseInfo, index: number, testResult: TestResult) => {
  selectedCaseInfo.value = data;
  selectedIndex.value = index;
  selectedTestResult.value = testResult;
  taskModalVisible.value = true;
};

const addTaskOk = async (data) => {
  if (!data?.id) {
    return;
  }

  const refMap = selectedCaseInfo.value?.refMap || {
    TASK: [],
    CASE: []
  };
  refMap.TASK.push(data.id);
  const [error] = await funcCase.updateCase([{
    id: selectedCaseInfo.value?.id,
    refMap
  }]);
  if (error) {
    return;
  }

  notification.success('已关联该Bug任务');
  // 更新该条数据
  const [_error, _res] = await funcCase.getCaseInfo(selectedCaseInfo.value?.id);
  if (_error) {
    return;
  }

  if (selectedTestResult.value && selectedIndex.value) {
    caseDataMap.value[selectedTestResult.value][selectedIndex.value] = _res?.data || {};
  }

  selectedCaseInfo.value = undefined;
  selectedIndex.value = undefined;
  selectedTestResult.value = undefined;
};

const toRetest = async (data: CaseInfo, notificationFlag = true, errorCallback?:()=>void) => {
  const id = data.id;
  emit('loadingChange', true);
  const [error] = await funcCase.retestResult([id]);
  emit('loadingChange', false);
  if (error) {
    if (typeof errorCallback === 'function') {
      errorCallback();
    }
    return;
  }

  emit('refreshChange');
  if (notificationFlag) {
    notification.success('用例重新测试成功');
  }
  loadData();
};

const toResetTestResult = async (data: CaseInfo) => {
  const id = data.id;
  emit('loadingChange', true);
  const [error] = await funcCase.resetResult([id]);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success('用例重置测试结果成功');
  loadData();
};

const toBlock = async (data: CaseInfo, notificationFlag = true, errorCallback?:()=>void) => {
  const id = data.id;
  emit('loadingChange', true);
  const [error] = await funcCase.updateResult([{ id, testResult: 'BLOCKED' }]);
  emit('loadingChange', false);
  if (error) {
    if (typeof errorCallback === 'function') {
      errorCallback();
    }
    return;
  }

  emit('refreshChange');
  if (notificationFlag) {
    notification.success('用例设为阻塞中成功');
  }
  loadData();
};

const toCancel = async (data: CaseInfo, notificationFlag = true, errorCallback?:()=>void) => {
  const id = data.id;
  emit('loadingChange', true);
  const [error] = await funcCase.updateResult([{ id, testResult: 'CANCELED' }], { dataType: true });
  emit('loadingChange', false);
  if (error) {
    if (typeof errorCallback === 'function') {
      errorCallback();
    }
    return;
  }

  emit('refreshChange');
  if (notificationFlag) {
    notification.success('用例取消成功');
  }
  loadData();
};

const drawerActiveKeyChange = (key: 'basic' | 'testStep' | 'person' | 'date' | 'comment' | 'activity' | 'refTasks' | 'refCases' | 'attachments' | 'remarks') => {
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
    if (props.groupKey === 'testerName') {
      list = testerNameList.value.map(item => item.value);
    } else if (props.groupKey === 'lastModifiedByName') {
      list = lastModifiedByNameList.value.map(item => item.value);
    }

    for (let i = 0, len = list.length; i < len; i++) {
      arrowOpenSet.value.add(list[i]);
    }

    return;
  }

  arrowOpenSet.value.clear();
};

const loadingChange = (value:boolean) => {
  emit('loadingChange', value);
};

const caseInfoChange = (data: Partial<CaseInfo>) => {
  if (checkedCaseInfo.value) {
    checkedCaseInfo.value = { ...checkedCaseInfo.value, ...data };
  }

  const id = data.id;
  const list = caseList.value;
  for (let i = 0, len = list.length; i < len; i++) {
    if (list[i].id === id) {
      list[i] = data;
      break;
    }
  }

  if (caseDataMap.value[selectedTestResult.value]?.[selectedIndex.value]) {
    caseDataMap.value[selectedTestResult.value][selectedIndex.value] = data;
  }

  if (groupDataMap.value[checkedGroupKey.value]?.[selectedTestResult.value]?.[selectedIndex.value]) {
    groupDataMap.value[checkedGroupKey.value][selectedTestResult.value][selectedIndex.value] = data;
  }

  // const id = checkedCaseInfo.value?.id;
  // const testResult = checkedCaseInfo.value?.testResult?.value || 'none';
  // let list:case [] = [];
  // if (props.groupKey === 'none') {
  //   list = caseDataMap.value[testResult];
  // } else {
  //   list = groupDataMap.value[_createdByName.value][testResult];
  // }

  // for (let i = 0, len = list.length; i < len; i++) {
  //   if (list[i].id === id) {
  //     list[i] = data;
  //     return;
  //   }
  // }
};

const resetData = () => {
  caseList.value = [];
  groupDataMap.value = {};
  numMap.value = {
    PENDING: 0,
    PASSED: 0,
    NOT_PASSED: 0,
    BLOCKED: 0,
    CANCELED: 0
  };
  caseDataMap.value = {
    PENDING: [],
    PASSED: [],
    NOT_PASSED: [],
    BLOCKED: [],
    CANCELED: []
  };

  testerNameList.value = [];
  lastModifiedByNameList.value = [];
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

const menuItemsMap = computed<Map<string, ActionMenuItem[]>>(() => {
  const map = new Map<string, ActionMenuItem[]>();
  for (const key in caseDataMap.value) {
    const list = (caseDataMap.value[key] || []) as CaseInfo[];
    for (let i = 0, len = list.length; i < len; i++) {
      const item = list[i];
      const planId = item.planId;

      const permissions = planPermissionsMap.value.get(planId) || [];
      if (props.userInfo?.id === item.testerId && !permissions.includes('TEST')) {
        permissions.push('TEST');
      }
      const { favouriteFlag, followFlag, testNum, reviewFlag, reviewStatus: { value: reviewStatus }, testResult: { value: testResult } } = item;

      const menuItems: ActionMenuItem[] = [
        {
          name: '编辑',
          key: 'edit',
          icon: 'icon-shuxie',
          disabled: !isAdmin && !permissions.includes('MODIFY_CASE'),
          hide: false
        },
        {
          name: '删除',
          key: 'delete',
          icon: 'icon-qingchu',
          disabled: !isAdmin && !permissions.includes('DELETE_CASE'),
          hide: false
        }
      ];

      if (!reviewFlag || (reviewFlag && reviewStatus === 'PASSED')) {
        if (testResult === 'PENDING' || testResult === 'BLOCKED') {
          menuItems.push({
            name: '测试通过',
            key: 'testPassed',
            icon: 'icon-xiugaiceshijieguo',
            disabled: !isAdmin && !permissions.includes('TEST'),
            hide: false
          });

          menuItems.push({
            name: '测试未通过',
            key: 'testNotPassed',
            icon: 'icon-xiugaiceshijieguo',
            disabled: !isAdmin && !permissions.includes('TEST'),
            hide: false
          });

          if (testResult === 'PENDING') {
            menuItems.push({
              name: '设为阻塞中',
              key: 'block',
              icon: 'icon-xiugaiceshijieguo',
              disabled: !isAdmin && !permissions.includes('TEST'),
              hide: false
            });
          }
        } else if (testResult === 'PASSED' || testResult === 'NOT_PASSED' || testResult === 'CANCELED') {
          menuItems.push({
            name: '重新测试',
            key: 'retest',
            icon: 'icon-xiugaiceshijieguo',
            disabled: !isAdmin && (!permissions.includes('RESET_TEST_RESULT') || planAuthFlagMap.value[item.planId]) && item.testerId !== userInfo?.id,
            hide: false
          });

          if (testResult === 'NOT_PASSED') {
            menuItems.push({
              name: '提BUG',
              key: 'addBug',
              icon: 'icon-bianji',
              disabled: !isAdmin && !permissions.includes('MODIFY_CASE'),
              hide: false
            });
          }
        }

        // 测试次数大于0才允许重置测试结果
        if (+testNum > 0) {
          menuItems.push({
            name: '重置测试结果',
            key: 'resetTestResult',
            icon: 'icon-zhongzhiceshijieguo',
            disabled: !isAdmin && (!permissions.includes('RESET_TEST_RESULT') || planAuthFlagMap.value[item.planId]),
            hide: false,
            tip: '将用例更新为`待测试`，相关统计计数和状态会被清除。'
          });
        }

        if (testResult === 'PENDING' || testResult === 'BLOCKED') {
          menuItems.push({
            name: '取消',
            key: 'cancel',
            icon: 'icon-qingchu',
            disabled: !isAdmin && !permissions.includes('TEST'),
            hide: false
          });
        }
      }

      menuItems.push({
        name: '克隆',
        key: 'clone',
        icon: 'icon-fuzhi',
        disabled: !isAdmin && !permissions.includes('ADD_CASE'),
        hide: false
      });

      menuItems.push({
        name: '移动',
        key: 'move',
        icon: 'icon-yidong',
        disabled: !isAdmin && !permissions.includes('MODIFY_CASE'),
        hide: false
      });

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

      map.set(item.id, menuItems);
    }
  }

  return map;
});

const showGroupData = computed(() => {
  if (props.groupKey === 'testerName') {
    return testerNameList.value;
  }

  if (props.groupKey === 'lastModifiedByName') {
    return lastModifiedByNameList.value;
  }

  return [];
});

const checkedCaseId = computed(() => {
  return checkedCaseInfo?.value?.id;
});
</script>

<template>
  <div class="flex-1 leading-5 flex items-start flex-nowrap overflow-hidden border-t border-solid border-theme-text-box">
    <div v-if="props.groupKey === 'none'" class="flex-1 flex flex-nowrap h-full overflow-hidden">
      <div
        v-for="(item,testResultIndex) in testResultList"
        :key="item.value"
        class="col-item h-full w-1/5 border-r border-solid border-theme-text-box overflow-hidden">
        <div class="flex items-center px-2.5 py-1.5 space-x-1.5 font-semibold head-container">
          <span>{{ item.message }}</span>
          <span>{{ numMap[item.value] }}</span>
        </div>
        <Draggable
          :id="`${item.value}-${testResultIndex}`"
          :list="caseDataMap[item.value]"
          :animation="300"
          ghostClass="ghost"
          group="cases"
          itemKey="id"
          tag="div"
          style="height: calc(100% - 32px);"
          :class="{'highlight-enabled':testResultIndex===isDraggingToColumn,highlight:isDraggingToColumnTestResult.includes(item.value)}"
          class="draggable-container overflow-y-auto scroll-smooth p-2"
          @move="dragMove"
          @start="dragStart"
          @end="dragEnd"
          @add="dragAdd($event, item.value)">
          <template #item="{ element, index }: { element: CaseInfo; index: number; }">
            <div
              :id="`${item.value}-${index}-${element.id}`"
              :class="{ 'active-item': checkedCaseId === element.id }"
              class="case-board-item border border-solid rounded border-theme-text-box p-2 space-y-1.5"
              @click="toChecked(element,index)">
              <div class="flex items-center overflow-hidden">
                <IconTask :value="element.testResult.value" class="mr-1.5" />
                <span :title="element.name" class="flex-1 truncate font-semibold">{{ element.name }}</span>
                <span
                  v-if="element.overdueFlag"
                  class="flex-shrink-0 border border-testResult-error rounded px-0.5"
                  style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
                  <span class="inline-block transform-gpu scale-90">已逾期</span>
                </span>
                <Dropdown
                  :menuItems="menuItemsMap.get(element.id)"
                  :overlayStyle="{'min-width': '100px'}"
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
                      <span class="flex-shrink-0">测试人</span>
                      <Colon class="mr-1.5" />
                      <span :title="element.testerName" class="truncate">{{ element.testerName }}</span>
                    </div>
                  </template>
                  <div style="width:20px;" class="flex-shrink-0">
                    <Image
                      :src="element.testerAvatar"
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
          v-for="_testResult in testResultList"
          :key="_testResult.value"
          style="width:calc((100% - 200px)/5);"
          class="col-item border-r border-solid border-theme-text-box flex items-center px-2.5 py-1.5 space-x-1.5 font-semibold head-container">
          <span>{{ _testResult.message }}</span>
          <span>{{ numMap[_testResult.value] }}</span>
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
              <div class="flex-1 truncate font-semibold" :title="_createdByName.name">{{ _createdByName.name }}</div>
              <div class="flex-shrink-0">
              </div>
            </div>
            <div class="flex items-center">
              <span>{{ Object.values(groupDataMap[_createdByName.value] || {}).reduce((prev, cur) => prev + cur.length,0) }}</span>
              <span>个用例</span>
            </div>
          </div>
          <div class="relative h-full flex items-start" style="width: calc(100% - 193px);">
            <template v-if="!arrowOpenSet.has(_createdByName.value)">
              <div
                v-for="_testResult in testResultList"
                :key="_testResult.value"
                style="width:20%;"
                class="flex items-center px-2.5 py-3.5 space-x-1.5">
                <span>{{ _testResult.message }}</span>
                <span>{{ groupDataMap[_createdByName.value]?.[_testResult.value]?.length || 0 }}</span>
              </div>
            </template>
            <AsyncComponent :visible="arrowOpenSet.has(_createdByName.value)">
              <Draggable
                v-for="(_testResult,testResultIndex) in testResultList"
                v-show="arrowOpenSet.has(_createdByName.value)"
                :id="`${_testResult.value}-${testResultIndex}`"
                :key="_testResult.value"
                style="width:20%;height: 100%;"
                :list="(groupDataMap[_createdByName.value]?.[_testResult.value] || [])"
                :animation="300"
                :group="`cases-${_createdByName.value}`"
                :class="{'highlight-enabled':testResultIndex===isDraggingToColumn,highlight:isDraggingToColumnTestResult.includes(_testResult.value)}"
                class="draggable-container right-border relative overflow-y-auto scroll-smooth space-y-2 px-2 py-2"
                ghostClass="ghost"
                itemKey="id"
                tag="div"
                @move="dragMove"
                @start="dragStart"
                @end="dragEnd"
                @add="groupDragAdd($event, _testResult.value)">
                <template #item="{ element, index }: { element: CaseInfo; index: number; }">
                  <div
                    :id="`${_testResult.value}-${index}-${element.id}-${_createdByName.value}`"
                    :class="{ 'active-item': checkedCaseId === element.id }"
                    class="case-board-item border border-solid rounded border-theme-text-box p-2 space-y-1.5"
                    @click="toChecked(element,index,_createdByName.value)">
                    <div class="flex items-center overflow-hidden">
                      <IconTask :value="element.testResult.value" class="mr-1.5" />
                      <span :title="element.name" class="flex-1 truncate font-semibold">{{ element.name }}</span>
                      <span
                        v-if="element.overdueFlag"
                        class="flex-shrink-0 border border-testResult-error rounded px-0.5"
                        style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
                        <span class="inline-block transform-gpu scale-90">已逾期</span>
                      </span>
                      <Dropdown
                        :menuItems="menuItemsMap.get(element.id)"
                        :overlayStyle="{'min-width': '100px'}"
                        @click="dropdownClick($event, element, index, _testResult.value)">
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
                            <span class="flex-shrink-0">测试人</span>
                            <Colon class="mr-1.5" />
                            <span :title="element.testerName" class="truncate">{{ element.testerName }}</span>
                          </div>
                        </template>
                        <div style="width:20px;" class="flex-shrink-0">
                          <Image
                            :src="element.testerAvatar"
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

    <div class="drawer-container flex items-start" :class="{ 'drawer-open': !!checkedCaseId }">
      <div class="flex-shrink-0 h-full w-9 space-y-1 overflow-y-auto scroll-smooth drawer-action-container">
        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'basic' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          title="基本信息"
          @click="drawerActiveKeyChange('basic')">
          <Icon icon="icon-wendangxinxi" class="text-4" />
        </div>

        <div
          v-if="false"
          :class="{ 'drawer-active-item': drawerActiveKey === 'testStep' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          title="测试步骤"
          @click="drawerActiveKeyChange('testStep')">
          <Icon icon="icon-renwu2" class="text-4" />
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
          :class="{ 'drawer-active-item': drawerActiveKey === 'reviewInfo' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          title="评审信息"
          @click="drawerActiveKeyChange('reviewInfo')">
          <Icon icon="icon-pingtaimorenzhibiao1" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'testInfo' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          title="测试信息"
          @click="drawerActiveKeyChange('testInfo')">
          <Icon icon="icon-renwuceshibaogao" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'refTasks' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          title="关联任务"
          @click="drawerActiveKeyChange('refTasks')">
          <Icon icon="icon-ceshirenwu" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'refCases' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          title="关联用例"
          @click="drawerActiveKeyChange('refCases')">
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
          :class="{ 'drawer-active-item': drawerActiveKey === 'reviewRecord' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          title="评审记录"
          @click="drawerActiveKeyChange('reviewRecord')">
          <Icon icon="icon-pingshen" class="text-4" />
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
              :to="`/function#plans?id=${checkedPlanInfo?.id}`"
              :title="checkedPlanInfo?.name"
              class="truncate"
              style="max-width: 50%;">
              {{ checkedPlanInfo?.name }}
            </RouterLink>
            <div class="mx-1.5">/</div>
            <RouterLink
              :to="`/function#cases?id=${checkedCaseInfo?.id}`"
              class="truncate flex-1"
              :title="checkedCaseInfo?.name">
              {{ checkedCaseInfo?.name }}
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
          <AsyncComponent :visible="!!checkedCaseId">
            <BasicInfo
              v-show="drawerActiveKey === 'basic'"
              v-model:dataSource="checkedCaseInfo"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :canEdit="!!menuItemsMap.get(checkedCaseId)?.find(item=>item.key==='edit')"
              @change="caseInfoChange"
              @loadingChange="loadingChange" />

            <TestSteps
              v-show="drawerActiveKey === 'testStep'"
              v-model:dataSource="checkedCaseInfo"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :canEdit="!!menuItemsMap.get(checkedCaseId)?.find(item=>item.key==='edit')"
              @change="caseInfoChange"
              @loadingChange="loadingChange" />

            <PersonnelInfo
              v-show="drawerActiveKey === 'person'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedCaseInfo"
              :canEdit="!!menuItemsMap.get(checkedCaseId)?.find(item=>item.key==='edit')"
              @change="caseInfoChange"
              @loadingChange="loadingChange" />

            <DateInfo
              v-show="drawerActiveKey === 'date'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedCaseInfo"
              :canEdit="!!menuItemsMap.get(checkedCaseId)?.find(item=>item.key==='edit')"
              @change="caseInfoChange"
              @loadingChange="loadingChange" />

            <ReviewInfo
              v-show="drawerActiveKey === 'reviewInfo'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedCaseInfo"
              :canEdit="!!menuItemsMap.get(checkedCaseId)?.find(item=>item.key==='edit')"
              @change="caseInfoChange"
              @loadingChange="loadingChange" />

            <TestInfo
              v-show="drawerActiveKey === 'testInfo'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedCaseInfo"
              :canEdit="!!menuItemsMap.get(checkedCaseId)?.find(item=>item.key==='edit')"
              @change="caseInfoChange"
              @loadingChange="loadingChange" />

            <AssocTask
              v-show="drawerActiveKey === 'refTasks'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedCaseInfo"
              :canEdit="!!menuItemsMap.get(checkedCaseId)?.find(item=>item.key==='edit')"
              @change="caseInfoChange"
              @loadingChange="loadingChange" />

            <AssocCase
              v-show="drawerActiveKey === 'refCases'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedCaseInfo"
              :canEdit="!!menuItemsMap.get(checkedCaseId)?.find(item=>item.key==='edit')"
              @change="caseInfoChange"
              @loadingChange="loadingChange" />

            <AttachmentInfo
              v-show="drawerActiveKey === 'attachments'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedCaseInfo"
              :canEdit="!!menuItemsMap.get(checkedCaseId)?.find(item=>item.key==='edit')"
              @change="caseInfoChange"
              @loadingChange="loadingChange" />

            <ReviewRecord
              v-show="drawerActiveKey === 'reviewRecord'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedCaseInfo"
              :canEdit="!!menuItemsMap.get(checkedCaseId)?.find(item=>item.key==='edit')"
              @change="caseInfoChange"
              @loadingChange="loadingChange" />

            <Activity
              v-show="drawerActiveKey === 'activity'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedCaseInfo"
              :canEdit="!!menuItemsMap.get(checkedCaseId)?.find(item=>item.key==='edit')"
              @change="caseInfoChange"
              @loadingChange="loadingChange" />

            <Comment
              v-show="drawerActiveKey === 'comment'"
              :id="checkedCaseInfo?.id"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedCaseInfo"
              :canEdit="!!menuItemsMap.get(checkedCaseId)?.find(item=>item.key==='edit')" />
          </AsyncComponent>
        </div>
      </div>
    </div>
  </div>

  <AsyncComponent :visible="moveModalVisible">
    <MoveModal
      v-if="moveModalVisible"
      v-model:visible="moveModalVisible"
      :selectedCase="selectedCaseInfo"
      :selectedRowKeys="selectedCaseInfo?.id?[selectedCaseInfo.id]:[]"
      type="one"
      @update="moveOk" />
  </AsyncComponent>

  <AsyncComponent :visible="caseModalVisible">
    <EditTaskModal
      v-model:visible="caseModalVisible"
      :editCase="selectedCaseInfo"
      @update="editOk" />
  </AsyncComponent>

  <AsyncComponent :visible="updateTestResultVisible">
    <UpdateResultModal
      v-if="updateTestResultVisible"
      v-model:visible="updateTestResultVisible"
      :resultPassed="resultPassed"
      :selectedCase="selectedCaseInfo"
      :selectedRowKeys="selectedCaseInfo?.id?[selectedCaseInfo.id]:[]"
      type="one"
      @update="testOk"
      @cancel="updateTestResultCancel" />
  </AsyncComponent>

  <AsyncComponent :visible="taskModalVisible">
    <AddTaskModal
      v-model:visible="taskModalVisible"
      :projectId="props.projectId"
      :appInfo="props.appInfo"
      :assigneeId="selectedCaseInfo?.developerId"
      :userInfo="userInfo"
      taskType="BUG"
      :confirmorId="selectedCaseInfo?.testerId"
      @ok="addTaskOk" />
  </AsyncComponent>
</template>

<style scoped>
.head-container {
  background-color: #f4f5f7;
}

.case-board-item {
  cursor: move
}

.case-board-item:hover {
  box-shadow: 0 3px 6px #0000000a;
}

.case-board-item:hover .action-btn.invisible {
  visibility: visible;
}

.active-item.case-board-item {
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
  border: 1px dashed rgba(188,198,207,100%);
  background-color: rgba(245,246,246,100%);
}

.highlight.highlight-enabled {
  border: 1px dashed rgba(70,166,255,100%);
  background-color: rgba(70,166,255,10%);
}
</style>
