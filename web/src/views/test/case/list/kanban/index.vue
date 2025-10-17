<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import {
  Arrow, AsyncComponent, Colon, Dropdown, Icon, IconTask, Image, modal, notification, Tooltip
} from '@xcan-angus/vue-ui';
import { appContext, enumUtils, PageQuery, SearchCriteria, ProjectPageQuery, ReviewStatus } from '@xcan-angus/infra';
import Draggable from 'vuedraggable';
import dayjs from 'dayjs';
import { reverse, sortBy } from 'lodash-es';
import { useI18n } from 'vue-i18n';
import { testCase, testPlan } from '@/api/tester';
import { CaseTestResult, FuncPlanPermission, TaskType } from '@/enums/enums';
import { CaseDetail } from '@/views/test/types';
import { ActionMenuItem } from '@/views/test/case/list/types';
import { TestMenuKey } from '@/views/test/menu';

import TaskPriority from '@/components/TaskPriority/index.vue';

const EditTaskModal = defineAsyncComponent(() => import('@/views/test/case/list/Edit.vue'));
const MoveModal = defineAsyncComponent(() => import('@/views/test/case/list/Move.vue'));
const UpdateResultModal = defineAsyncComponent(() => import('@/views/test/case/list/UpdateResult.vue'));
const AddTaskModal = defineAsyncComponent(() => import('@/views/issue/issue/list/Edit.vue'));

const BasicInfo = defineAsyncComponent(() => import('@/views/test/case/list/kanban/detail/Basic.vue'));
const TestSteps = defineAsyncComponent(() => import('@/views/test/case/list/kanban/detail/TestSteps.vue'));
const PersonnelInfo = defineAsyncComponent(() => import('@/views/test/case/list/kanban/detail/Personnel.vue'));
const DateInfo = defineAsyncComponent(() => import('@/views/test/case/list/kanban/detail/Date.vue'));
const ReviewInfo = defineAsyncComponent(() => import('@/views/test/case/list/kanban/detail/ReviewInfo.vue'));
const TestInfo = defineAsyncComponent(() => import('@/views/test/case/list/kanban/detail/TestInfo.vue'));
const AssocIssues = defineAsyncComponent(() => import('@/views/test/case/list/kanban/detail/AssocIssues.vue'));
const AssocCases = defineAsyncComponent(() => import('@/views/test/case/list/kanban/detail/AssocCases.vue'));
const AttachmentInfo = defineAsyncComponent(() => import('@/views/test/case/list/kanban/detail/Attachment.vue'));
const ReviewRecord = defineAsyncComponent(() => import('@/views/test/case/list/kanban/detail/ReviewRecord.vue'));
const Comment = defineAsyncComponent(() => import('@/views/test/case/list/kanban/detail/Comment.vue'));
const Activity = defineAsyncComponent(() => import('@/views/test/case/list/kanban/detail/Activity.vue'));

type Props = {
  projectId: number;
  userInfo: { id: number; };
  appInfo: { id: number; };
  filters: SearchCriteria[];
  notify: string;
  moduleId: number;
  groupKey: 'none' | 'testerName' | 'lastModifiedByName';
  orderBy: 'priority' | 'deadlineDate' | 'createdByName' | 'testerName';
  orderSort: PageQuery.OrderSort;
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


const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'refreshChange'): void;
}>();

const { t } = useI18n();

const isAdmin = computed(() => appContext.isAdmin());

const drawerActiveKey = ref<'basic' | 'testStep' | 'person' | 'date' | 'comment' | 'activity' | 'assocIssues' | 'assocCases' | 'attachments' | 'remarks' | 'reviewInfo' | 'testInfo' | 'reviewRecord'>('basic');

const planPermissionsMap = ref<Map<number, FuncPlanPermission[]>>(new Map());
const planAuthMap = ref({});

const testResultList = ref<{ message: string; value: CaseTestResult }[]>([]);
const caseList = ref<CaseDetail[]>([]);
const caseDataMap = ref<{ [key in CaseTestResult]: CaseDetail[] }>({
  PENDING: [],
  PASSED: [],
  NOT_PASSED: [],
  BLOCKED: [],
  CANCELED: []
});
const numMap = ref<{ [key in CaseTestResult]: number }>({
  PENDING: 0,
  PASSED: 0,
  NOT_PASSED: 0,
  BLOCKED: 0,
  CANCELED: 0
});

const testerNameList = ref<{ name: string; value: number }[]>([]);
const lastModifiedByNameList = ref<{ name: string; value: number }[]>([]);
const groupDataMap = ref<{ [key: number]: { [key in CaseTestResult]: CaseDetail[] } }>({});

const isDraggingToColumn = ref<number|null>(null);
const isDraggingToColumnTestResult = ref<CaseTestResult[]>([]);

const openFlag = ref(false);
const arrowOpenSet = ref(new Set<number>());

const checkedCaseInfo = ref<CaseDetail>();
const checkedPlanInfo = ref<{ id: string; name: string; }>();
const checkedGroupKey = ref<string>();

const selectedIndex = ref<number>();
const selectedTestResult = ref<CaseTestResult>();
const selectedCaseInfo = ref<CaseDetail>();

const selectedToTestResult = ref<CaseTestResult>();
const selectedGroupKey = ref<string|undefined>();

const caseModalVisible = ref(false);
const moveModalVisible = ref(false);
const updateTestResultVisible = ref(false);
const taskModalVisible = ref(false);

const resultPassed = ref(false);

/**
 * Load enums for test results used to render columns and labels
 */
const loadEnums = async () => {
  testResultList.value = enumUtils.enumToMessages(CaseTestResult);
};

/**
 * Load cases for current filters and group settings, including permissions
 */
const loadCases = async () => {
  const params = getParams();
  emit('loadingChange', true);
  const [error, res] = await testCase.getCaseList({ infoScope: 'DETAIL', ...params });
  if (error) {
    resetData();
    emit('loadingChange', false);
    return;
  }

  resetData();

  const { list, total } = (res?.data || { total: 0, list: [] });
  const len = list.length;
  const _caseList: CaseDetail[] = [];
  _caseList.push(...list);
  if (len < +total) {
    const pages = Math.ceil((total - len) / (params.pageSize || 10));
    for (let i = 0, len = pages; i < len; i++) {
      const pageNo = i + 2;
      const _params = { ...params, pageNo };
      const [_error, _res] = await testCase.getCaseList({ infoScope: PageQuery.InfoScope.DETAIL, ..._params });
      if (_error) {
        emit('loadingChange', false);
        return;
      }

      const { list: _list } = (_res?.data || { total: 0, list: [] });
      _caseList.push(..._list);
    }
  }

  caseList.value = _caseList;

  const planIdSet = new Set<number>();
  const testerNameSet = new Set<string>();
  const lastModifiedByNameSet = new Set<string>();
  for (let i = 0, len = _caseList.length; i < len; i++) {
    const item = _caseList[i];
    const testResult = item.testResult?.value;
    numMap.value[testResult] += 1;
    caseDataMap.value[testResult].push(item);
    planIdSet.add(item.planId);

    if (!testerNameSet.has(item.testerName)) {
      if (!item.testerName) {
        testerNameList.value.unshift({
          name: t('status.ungrouped'),
          value: -1
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
  if (!isAdmin.value) {
    const [error, { data }] = await testPlan.getCurrentAuth({
      ids: sprintIds,
      admin: true
    });
    if (error) {
      return;
    }
    for (let i = 0, len = sprintIds.length; i < len; i++) {
      const id = sprintIds[i];
      planPermissionsMap.value.set(id, (data[id]?.permissions || []).map(i => i.value));
      planAuthMap.value[id] = data[id].funcPlanAuth;
    }
  } else {
    for (let i = 0, len = sprintIds.length; i < len; i++) {
      const id = sprintIds[i];
      planAuthMap.value[id] = true;
      planPermissionsMap.value.set(id, enumUtils.getEnumValues(FuncPlanPermission));
    }
  }

  emit('loadingChange', false);
};

/**
 * Build list query params from component props
 */
const getParams = () => {
  const params: ProjectPageQuery & {
    moduleId?: number;
  } = {
    projectId: props.projectId,
    pageNo: 1,
    pageSize: 500,
    infoScope: PageQuery.InfoScope.DETAIL
  };

  if (props.filters?.length) {
    params.filters = props.filters;
  }

  if (props.moduleId) {
    params.moduleId = props.moduleId;
  }
  return params;
};

/**
 * Accumulate a single case into grouped data map by selected group key
 */
const setGroupData = (data:CaseDetail) => {
  const { testResult: { value: testResultValue }, testerId = -1, lastModifiedBy } = data;
  let key = -1;
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

/**
 * Ensure each group has buckets for all test results
 */
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

/**
 * Visual highlight rules while dragging across columns
 */
const handleDragMove = (event) => {
  const [, toIndex] = event.to.id.split('-') as [CaseTestResult, number];
  const [fromResult] = event.from.id.split('-') as [CaseTestResult, number];
  const [, , draggedId] = event.dragged.id.split('-') as [CaseTestResult, number, number];
  let cancelDisabled = false;
  const cancelItem = menuItemsMap.value.get(draggedId)?.find(item => item.key === 'cancel');
  if (!cancelItem) {
    cancelDisabled = true;
  } else {
    cancelDisabled = !!cancelItem?.disabled;
  }

  if (fromResult === CaseTestResult.PENDING) {
    isDraggingToColumnTestResult.value = [CaseTestResult.PASSED, CaseTestResult.NOT_PASSED, CaseTestResult.BLOCKED];
    if (!cancelDisabled) {
      isDraggingToColumnTestResult.value.push(CaseTestResult.CANCELED);
    }
    isDraggingToColumn.value = +toIndex;
    return;
  }

  if (fromResult === CaseTestResult.PASSED || fromResult === CaseTestResult.NOT_PASSED) {
    isDraggingToColumnTestResult.value = [CaseTestResult.PENDING];
    if (!cancelDisabled) {
      isDraggingToColumnTestResult.value.push(CaseTestResult.CANCELED);
    }
    isDraggingToColumn.value = +toIndex;
    return;
  }

  if (fromResult === CaseTestResult.BLOCKED) {
    isDraggingToColumnTestResult.value = [CaseTestResult.PASSED, CaseTestResult.NOT_PASSED];
    if (!cancelDisabled) {
      isDraggingToColumnTestResult.value.push(CaseTestResult.CANCELED);
    }
    isDraggingToColumn.value = +toIndex;
    return;
  }

  if (fromResult === CaseTestResult.CANCELED) {
    isDraggingToColumnTestResult.value = [CaseTestResult.PENDING];
    isDraggingToColumn.value = +toIndex;
  }
};

/**
 * Reset highlight state on drag start
 */
const handleDragStart = () => {
  isDraggingToColumn.value = null;
  isDraggingToColumnTestResult.value = [];
};

/**
 * Clear highlight state on drag end
 */
const handleDragEnd = () => {
  isDraggingToColumn.value = null;
  isDraggingToColumnTestResult.value = [];
};

/**
 * Apply business rules for dropping a card between columns
 */
const handleDropBusinessRules = (
  data:CaseDetail,
  testResult:CaseTestResult,
  toTestResult:CaseTestResult,
  index:number,
  groupKey?:string
) => {
  const { review, reviewStatus: { value: reviewStatus }, planId, id } = data;
  const permissions = planPermissionsMap.value.get(planId) || [];
  if (testResult === CaseTestResult.PENDING) {
    if (review) {
      if (reviewStatus === ReviewStatus.PENDING) {
        if (groupKey) {
          revertDragInGroup(id, index, testResult, toTestResult, groupKey);
        } else {
          revertDragInFlat(id, index, testResult, toTestResult);
        }
        notification.warning(t('testCase.messages.caseNotReviewed'));
        return;
      }

      if (reviewStatus === ReviewStatus.FAILED) {
        if (groupKey) {
          revertDragInGroup(id, index, testResult, toTestResult, groupKey);
        } else {
          revertDragInFlat(id, index, testResult, toTestResult);
        }
        notification.warning(t('testCase.messages.caseReviewFailed'));
        return;
      }

      if (!isAdmin.value && !permissions.includes(FuncPlanPermission.TEST)) {
        notification.warning(t('testCase.messages.noTestPermission'));
        return;
      }
    }

    if (toTestResult === CaseTestResult.PASSED) {
      selectedCaseInfo.value = data;
      selectedIndex.value = index;
      selectedGroupKey.value = groupKey;
      selectedTestResult.value = testResult;
      selectedToTestResult.value = toTestResult;
      toPassed(data);
      return;
    }

    if (toTestResult === CaseTestResult.NOT_PASSED) {
      selectedCaseInfo.value = data;
      selectedIndex.value = index;
      selectedGroupKey.value = groupKey;
      selectedTestResult.value = testResult;
      selectedToTestResult.value = toTestResult;
      toNotPassed(data);
      return;
    }

    if (toTestResult === CaseTestResult.BLOCKED) {
      toBlock(data, false, () => {
        if (groupKey) {
          revertDragInGroup(id, index, testResult, toTestResult, groupKey);
        } else {
          revertDragInFlat(id, index, testResult, toTestResult);
        }
      });
      return;
    }

    if (toTestResult === CaseTestResult.CANCELED) {
      toCancel(data, false, () => {
        if (groupKey) {
          revertDragInGroup(id, index, testResult, toTestResult, groupKey);
        } else {
          revertDragInFlat(id, index, testResult, toTestResult);
        }
      });
      return;
    }

    return;
  }

  if (testResult === CaseTestResult.PASSED || testResult === CaseTestResult.NOT_PASSED || testResult === CaseTestResult.CANCELED) {
    if (toTestResult !== CaseTestResult.PENDING) {
      if (groupKey) {
        revertDragInGroup(id, index, testResult, toTestResult, groupKey);
      } else {
        revertDragInFlat(id, index, testResult, toTestResult);
      }
      notification.warning(t('testCase.messages.canOnlyMoveToPending'));
      return;
    } else {
      if (!isAdmin.value && !permissions.includes(FuncPlanPermission.TEST)) {
        if (groupKey) {
          revertDragInGroup(id, index, testResult, toTestResult, groupKey);
        } else {
          revertDragInFlat(id, index, testResult, toTestResult);
        }
        notification.warning('No test permission');
        return;
      }
    }

    toRetest(data, false, () => {
      if (groupKey) {
        revertDragInGroup(id, index, testResult, toTestResult, groupKey);
      } else {
        revertDragInFlat(id, index, testResult, toTestResult);
      }
    });
    return;
  }

  if (testResult === CaseTestResult.BLOCKED) {
    if (toTestResult !== CaseTestResult.PASSED && toTestResult !== CaseTestResult.NOT_PASSED && toTestResult !== CaseTestResult.CANCELED) {
      if (groupKey) {
        revertDragInGroup(id, index, testResult, toTestResult, groupKey);
      } else {
        revertDragInFlat(id, index, testResult, toTestResult);
      }
      notification.warning(t('testCase.messages.canOnlyMoveToTestResults'));
      return;
    } else {
      if (!isAdmin.value && !permissions.includes(FuncPlanPermission.TEST)) {
        if (groupKey) {
          revertDragInGroup(id, index, testResult, toTestResult, groupKey);
        } else {
          revertDragInFlat(id, index, testResult, toTestResult);
        }
        notification.warning('No test permission');
        return;
      }
    }

    if (toTestResult === CaseTestResult.PASSED) {
      selectedCaseInfo.value = data;
      selectedIndex.value = index;
      selectedGroupKey.value = groupKey;
      selectedTestResult.value = testResult;
      selectedToTestResult.value = toTestResult;
      toPassed(data);
      return;
    }

    if (toTestResult === CaseTestResult.NOT_PASSED) {
      selectedCaseInfo.value = data;
      selectedIndex.value = index;
      selectedGroupKey.value = groupKey;
      selectedTestResult.value = testResult;
      selectedToTestResult.value = toTestResult;
      toNotPassed(data);
      return;
    }

    if (toTestResult === CaseTestResult.CANCELED) {
      toCancel(data, false, () => {
        if (groupKey) {
          revertDragInGroup(id, index, testResult, toTestResult, groupKey);
        } else {
          revertDragInFlat(id, index, testResult, toTestResult);
        }
      });
    }
  }
};

/**
 * Handle item added into a flat column by drag-and-drop
 */
const handleDragAdd = async (event: { item: { id: string; }; }, toTestResult: CaseTestResult) => {
  const [testResult, index, id] = (event.item.id.split('-')) as [CaseTestResult, number, number];
  const targetData = caseDataMap.value[toTestResult].find(item => item.id === id);
  if (!targetData) {
    return;
  }

  handleDropBusinessRules(targetData, testResult, toTestResult, index);
};

/**
 * Handle item added into a grouped column by drag-and-drop
 */
const handleGroupDragAdd = async (event: { item: { id: string; } }, toTestResult: CaseTestResult) => {
  const [testResult, index, id, groupKey] = (event.item.id.split('-')) as [CaseTestResult, number, number, string];
  const targetData = groupDataMap.value[groupKey][toTestResult].find(item => item.id === id);
  if (!targetData) {
    return;
  }

  handleDropBusinessRules(targetData, testResult, toTestResult, index, groupKey);
};

/**
 * Revert drag in flat view by moving card back to original position
 */
const revertDragInFlat = (id: number, index: number, testResult: CaseTestResult, toTestResult: CaseTestResult) => {
  const _index = caseDataMap.value[toTestResult].findIndex(item => item.id === id);
  const dragData = caseDataMap.value[toTestResult][_index];
  caseDataMap.value[toTestResult].splice(_index, 1);
  caseDataMap.value[testResult].splice(index, 0, dragData);
};

/**
 * Revert drag in grouped view by moving card back to original position
 */
const revertDragInGroup = (id: number, index: number, testResult: CaseTestResult, toTestResult: CaseTestResult, groupKey: string) => {
  const _index = groupDataMap.value[groupKey][toTestResult].findIndex(item => item.id === id);
  const dragData = groupDataMap.value[groupKey][toTestResult][_index];
  groupDataMap.value[groupKey][toTestResult].splice(_index, 1);
  groupDataMap.value[groupKey][testResult].splice(index, 0, dragData);
};

/**
 * Entry point for applying sort to all columns
 */
const handleSortRequest = (data: { orderBy: 'priority' | 'deadlineDate' | 'createdByName' | 'testerName'; orderSort: PageQuery.OrderSort; }) => {
  sortData(data.orderBy, data.orderSort);
};

const sortData = (orderBy: 'priority' | 'deadlineDate' | 'createdByName' | 'testerName', orderSort: PageQuery.OrderSort) => {
  const map = caseDataMap.value;
  if (orderBy === 'priority') {
    const sortKeys = orderSort === PageQuery.OrderSort.Desc ? ['HIGHEST', 'HIGH', 'MEDIUM', 'LOW', 'LOWEST'] : ['LOWEST', 'LOW', 'MEDIUM', 'HIGH', 'HIGHEST'];
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
    if (orderSort === PageQuery.OrderSort.Desc) {
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

    if (orderSort === PageQuery.OrderSort.Asc) {
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
    if (orderSort === PageQuery.OrderSort.Desc) {
      for (const key in map) {
        map[key] = reverse(sortBy(map[key], orderBy));
      }
      return;
    }

    if (orderSort === PageQuery.OrderSort.Asc) {
      for (const key in map) {
        map[key] = sortBy(map[key], orderBy);
      }
    }
  }
};

/**
 * Apply grouping and rebuild grouped data map
 */
const handleGroupChange = (value: 'none' | 'testerName' | 'lastModifiedByName') => {
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

/**
 * Load and open drawer for selected case card
 */
const handleSelectCard = async (caseInfo: CaseDetail, index:number, groupKey?:string) => {
  const id = caseInfo.id;
  if (id === checkedCaseInfo.value?.id) {
    drawerClose();
    return;
  }

  if (!id) {
    return;
  }

  loadingChange(true);
  const [error, res] = await testCase.getCaseDetail(id);
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

/**
 * Close the right drawer and clear selection state
 */
const drawerClose = () => {
  checkedCaseInfo.value = undefined;
  checkedPlanInfo.value = undefined;
  checkedGroupKey.value = undefined;
  selectedTestResult.value = undefined;
  selectedIndex.value = undefined;
};

/**
 * Handle actions from card context menu
 */
const dropdownClick = (
  menuItem: ActionMenuItem,
  data: CaseDetail,
  index: number,
  testResult: CaseTestResult
) => {
  const key = menuItem.key;
  if (key === 'edit') {
    toEdit(data, index, testResult);
    return;
  }

  if (key === 'delete') {
    toDelete(data);
    return;
  }

  if (key === 'addFavourite') {
    toFavourite(data, index, testResult);
    return;
  }

  if (key === 'cancelFavourite') {
    toDeleteFavourite(data, index, testResult);
    return;
  }

  if (key === 'addFollow') {
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

/**
 * Open edit modal for a specific case
 */
const toEdit = (data: CaseDetail, index: number, testResult: CaseTestResult) => {
  selectedCaseInfo.value = data;
  selectedIndex.value = index;
  selectedTestResult.value = testResult;
  caseModalVisible.value = true;
};

/**
 * Handle edit modal success and refresh the edited row
 */
const editOk = async (id:number) => {
  const index = selectedIndex.value as number;
  const testResult = selectedTestResult.value as CaseTestResult;

  emit('loadingChange', true);
  const [, res] = await testCase.getCaseDetail(id);
  emit('loadingChange', false);
  if (res) {
    caseDataMap.value[testResult][index] = res.data;
  }

  selectedCaseInfo.value = undefined;
  selectedIndex.value = undefined;
  selectedTestResult.value = undefined;
};

/**
 * Confirm and delete a case, then reload list
 */
const toDelete = (data: CaseDetail) => {
  modal.confirm({
    content: t('actions.tips.confirmDelete', { name: data.name }),
    async onOk () {
      emit('loadingChange', true);
      const [error] = await testCase.deleteCase([data.id]);
      if (error) {
        emit('loadingChange', false);
        return;
      }

      emit('refreshChange');
      notification.success(t('actions.tips.deleteSuccess'));
      await loadCases();
      emit('loadingChange', false);
    }
  });
};

/**
 * Add to favourites and update local state
 */
const toFavourite = async (data: CaseDetail, index: number, testResult: CaseTestResult) => {
  emit('loadingChange', true);
  const [error] = await testCase.AddFavouriteCase(data.id);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  notification.success(t('actions.tips.favouriteSuccess'));
  caseDataMap.value[testResult][index].favourite = true;
};

/**
 * Remove from favourites and update local state
 */
const toDeleteFavourite = async (data: CaseDetail, index: number, testResult: CaseTestResult) => {
  emit('loadingChange', true);
  const [error] = await testCase.cancelFavouriteCase(data.id);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  notification.success(t('actions.tips.cancelFavouriteSuccess'));
  caseDataMap.value[testResult][index].favourite = false;
};

/**
 * Start following a case and update local state
 */
const toFollow = async (data: CaseDetail, index: number, testResult: CaseTestResult) => {
  emit('loadingChange', true);
  const [error] = await testCase.addFollowCase(data.id);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  notification.success(t('actions.tips'));
  caseDataMap.value[testResult][index].follow = true;
};

/**
 * Stop following a case and update local state
 */
const toDeleteFollow = async (data: CaseDetail, index: number, testResult: CaseTestResult) => {
  emit('loadingChange', true);
  const [error] = await testCase.cancelFollowCase(data.id);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  notification.success(t('actions.tips.cancelFollowSuccess'));
  caseDataMap.value[testResult][index].follow = false;
};

/**
 * Clone a case and reload list
 */
const toClone = async (data: CaseDetail) => {
  const id = data.id;
  emit('loadingChange', true);
  const [error] = await testCase.cloneCase([id]);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('actions.tips.cloneSuccess'));
  await loadCases();
};

/**
 * Open move modal for a case
 */
const toMove = (data: CaseDetail) => {
  selectedCaseInfo.value = data;
  moveModalVisible.value = true;
};

/**
 * Handle move modal success and reload list
 */
const moveOk = () => {
  selectedCaseInfo.value = undefined;
  emit('refreshChange');
  loadCases();
};

/**
 * Open update test result modal with Passed preset
 */
const toPassed = async (data: CaseDetail) => {
  selectedCaseInfo.value = data;
  updateTestResultVisible.value = true;
  resultPassed.value = true;
};

/**
 * Open update test result modal with Not Passed preset
 */
const toNotPassed = async (data: CaseDetail) => {
  selectedCaseInfo.value = data;
  updateTestResultVisible.value = true;
  resultPassed.value = false;
};

/**
 * Handle update test result success and reload list
 */
const testOk = async () => {
  selectedCaseInfo.value = undefined;
  resultPassed.value = false;
  emit('refreshChange');
  notification.success(t('testCase.messages.testResultUpdateSuccess'));
  await loadCases();
};

/**
 * Cancel update test result and revert drag preview if necessary
 */
const updateTestResultCancel = () => {
  const id = selectedCaseInfo.value?.id;
  const index = selectedIndex.value;
  const testResult = selectedTestResult.value;
  const toTestResult = selectedToTestResult.value;
  if (!id || (!index && index !== 0) || !testResult || !toTestResult) {
    return;
  }

  if (selectedGroupKey.value) {
    revertDragInGroup(id, index, testResult, toTestResult, selectedGroupKey.value);
  } else {
    revertDragInFlat(id, index, testResult, toTestResult);
  }

  resultPassed.value = false;
  selectedCaseInfo.value = undefined;
  selectedIndex.value = undefined;
  selectedGroupKey.value = undefined;
  selectedTestResult.value = undefined;
  selectedToTestResult.value = undefined;
};

/**
 * Open create bug modal with linked tester/developer defaults
 */
const toAddBug = (data: CaseDetail, index: number, testResult: CaseTestResult) => {
  selectedCaseInfo.value = data;
  selectedIndex.value = index;
  selectedTestResult.value = testResult;
  taskModalVisible.value = true;
};

/**
 * Handle bug creation success and update case references
 */
const addTaskOk = async (data) => {
  if (!data?.id) {
    return;
  }

  const refMap = selectedCaseInfo.value?.refMap || { TASK: [], CASE: [] };
  refMap.TASK.push(data.id);
  const [error] = await testCase.updateCase([{
    id: selectedCaseInfo.value?.id,
    refMap
  }]);
  if (error) {
    return;
  }

  notification.success(t('testCase.messages.bugIssueAssociated'));
  const [_error, _res] = await testCase.getCaseDetail(selectedCaseInfo?.value?.id || -1);
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

/**
 * Trigger retest action on a case and optionally notify
 */
const toRetest = async (data: CaseDetail, notificationFlag = true, errorCallback?:()=>void) => {
  const id = data.id;
  emit('loadingChange', true);
  const [error] = await testCase.retestResult([id]);
  emit('loadingChange', false);
  if (error) {
    if (typeof errorCallback === 'function') {
      errorCallback();
    }
    return;
  }

  emit('refreshChange');
  if (notificationFlag) {
    notification.success(t('testCase.messages.caseRetestSuccess'));
  }
  await loadCases();
};

/**
 * Reset test result to Pending and reload list
 */
const toResetTestResult = async (data: CaseDetail) => {
  const id = data.id;
  emit('loadingChange', true);
  const [error] = await testCase.resetCaseResult([id]);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('testCase.messages.resetTestResultSuccess'));
  await loadCases();
};

/**
 * Set case to Blocked and reload list
 */
const toBlock = async (data: CaseDetail, notificationFlag = true, errorCallback?:()=>void) => {
  const id = data.id;
  emit('loadingChange', true);
  const [error] = await testCase.updateCaseResult([{ id, testResult: CaseTestResult.BLOCKED }]);
  emit('loadingChange', false);
  if (error) {
    if (typeof errorCallback === 'function') {
      errorCallback();
    }
    return;
  }

  emit('refreshChange');
  if (notificationFlag) {
    notification.success(t('testCase.messages.caseBlockedSuccess'));
  }
  await loadCases();
};

/**
 * Cancel case and reload list
 */
const toCancel = async (data: CaseDetail, notificationFlag = true, errorCallback?:()=>void) => {
  const id = data.id;
  emit('loadingChange', true);
  const [error] = await testCase.updateCaseResult([{ id, testResult: CaseTestResult.CANCELED }]);
  emit('loadingChange', false);
  if (error) {
    if (typeof errorCallback === 'function') {
      errorCallback();
    }
    return;
  }

  emit('refreshChange');
  if (notificationFlag) {
    notification.success(t('actions.tips.cancelSuccess'));
  }
  await loadCases();
};

/**
 * Set drawer active tab
 */
const drawerActiveKeyChange = (key: 'basic' | 'testStep' | 'reviewInfo' | 'person' | 'date' | 'comment' | 'activity' | 'testInfo' | 'assocIssues' | 'assocCases' | 'attachments' | 'remarks') => {
  drawerActiveKey.value = key;
};

/**
 * Toggle single group open/close state
 */
const arrowOpenChange = (open: boolean, id: number) => {
  if (open) {
    arrowOpenSet.value.add(id);
    return;
  }
  arrowOpenSet.value.delete(id);
};

/**
 * Expand or collapse all groups
 */
const toggleOpen = () => {
  openFlag.value = !openFlag.value;

  if (openFlag.value) {
    let list: number[] = [];
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

/**
 * Emit loading state to parent
 */
const loadingChange = (value:boolean) => {
  emit('loadingChange', value);
};

/**
 * Sync case info changes into local maps and drawer data
 */
const caseInfoChange = (data: Partial<CaseDetail>) => {
  if (checkedCaseInfo.value) {
    checkedCaseInfo.value = { ...checkedCaseInfo.value, ...data };
  }

  const id = data.id;
  const list = caseList.value;
  for (let i = 0, len = list.length; i < len; i++) {
    if (list[i].id === id) {
      list[i] = data as CaseDetail;
      break;
    }
  }

  const selectedTestResultKey = selectedTestResult.value as string;
  if (caseDataMap.value[selectedTestResultKey]?.[selectedIndex.value]) {
    caseDataMap.value[selectedTestResultKey][selectedIndex.value] = data;
  }

  if (groupDataMap.value[selectedTestResultKey]?.[selectedTestResultKey]?.[selectedIndex.value]) {
    groupDataMap.value[selectedTestResultKey][selectedTestResultKey][selectedIndex.value] = data;
  }
};

/**
 * Reset all local state for list and grouping
 */
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
  loadEnums();

  watch(() => props.projectId, (newValue) => {
    if (!newValue) {
      return;
    }

    loadCases();
  }, { immediate: true });

  watch([() => props.filters, () => props.moduleId], () => {
    loadCases();
  });

  watch([() => props.orderBy, () => props.orderSort], () => {
    handleSortRequest({ orderBy: props.orderBy, orderSort: props.orderSort });
  });

  watch(() => props.groupKey, () => {
    handleGroupChange(props.groupKey);
  });
});

const menuItemsMap = computed<Map<number, ActionMenuItem[]>>(() => {
  const map = new Map<number, ActionMenuItem[]>();
  for (const key in caseDataMap.value) {
    const list = (caseDataMap.value[key] || []) as CaseDetail[];
    for (let i = 0, len = list.length; i < len; i++) {
      const item = list[i];
      const planId = item.planId;

      const permissions = planPermissionsMap.value.get(planId) || [];
      if (props.userInfo?.id === item.testerId && !permissions.includes(FuncPlanPermission.TEST)) {
        permissions.push(FuncPlanPermission.TEST);
      }
      const { favourite, follow, testNum, review, reviewStatus: { value: reviewStatus }, testResult: { value: testResult } } = item;

      const menuItems: ActionMenuItem[] = [
        {
          name: t('actions.edit'),
          key: 'edit',
          icon: 'icon-shuxie',
          disabled: !isAdmin.value && !permissions.includes(FuncPlanPermission.MODIFY_CASE),
          hide: false
        },
        {
          name: t('actions.delete'),
          key: 'delete',
          icon: 'icon-qingchu',
          disabled: !isAdmin.value && !permissions.includes(FuncPlanPermission.DELETE_CASE),
          hide: false
        }
      ];

      if (!review || (review && reviewStatus === ReviewStatus.PASSED)) {
        if (testResult === CaseTestResult.PENDING || testResult === CaseTestResult.BLOCKED) {
          menuItems.push({
            name: t('testCase.actions.testPassed'),
            key: 'testPassed',
            icon: 'icon-xiugaiceshijieguo',
            disabled: !isAdmin.value && !permissions.includes(FuncPlanPermission.TEST),
            hide: false
          });

          menuItems.push({
            name: t('testCase.actions.testNotPassed'),
            key: 'testNotPassed',
            icon: 'icon-xiugaiceshijieguo',
            disabled: !isAdmin.value && !permissions.includes(FuncPlanPermission.TEST),
            hide: false
          });

          if (testResult === CaseTestResult.PENDING) {
            menuItems.push({
              name: t('testCase.actions.setBlocked'),
              key: 'block',
              icon: 'icon-xiugaiceshijieguo',
              disabled: !isAdmin.value && !permissions.includes(FuncPlanPermission.TEST),
              hide: false
            });
          }
        } else if (testResult === CaseTestResult.PASSED || testResult === CaseTestResult.NOT_PASSED || testResult === CaseTestResult.CANCELED) {
          menuItems.push({
            name: t('testCase.actions.retest'),
            key: 'retest',
            icon: 'icon-xiugaiceshijieguo',
            disabled: !isAdmin.value && (!permissions.includes(FuncPlanPermission.RESET_TEST_RESULT) || planAuthMap.value[item.planId]) && item.testerId !== props.userInfo?.id,
            hide: false
          });

          if (testResult === CaseTestResult.NOT_PASSED) {
            menuItems.push({
              name: t('testCase.actions.submitBug'),
              key: 'addBug',
              icon: 'icon-bianji',
              disabled: !isAdmin.value && !permissions.includes(FuncPlanPermission.MODIFY_CASE),
              hide: false
            });
          }
        }

        // 测试次数大于0才允许重置测试结果
        if (+testNum > 0) {
          menuItems.push({
            name: t('testCase.actions.resetTestResult'),
            key: 'resetTestResult',
            icon: 'icon-zhongzhiceshijieguo',
            disabled: !isAdmin.value && (!permissions.includes(FuncPlanPermission.RESET_TEST_RESULT) || planAuthMap.value[item.planId]),
            hide: false,
            tip: t('testCase.messages.resetTestResultTip')
          });
        }

        if (testResult === CaseTestResult.PENDING || testResult === CaseTestResult.BLOCKED) {
          menuItems.push({
            name: t('actions.cancel'),
            key: 'cancel',
            icon: 'icon-qingchu',
            disabled: !isAdmin.value && !permissions.includes(FuncPlanPermission.TEST),
            hide: false
          });
        }
      }

      menuItems.push({
        name: t('actions.clone'),
        key: 'clone',
        icon: 'icon-fuzhi',
        disabled: !isAdmin.value && !permissions.includes(FuncPlanPermission.ADD_CASE),
        hide: false
      });

      menuItems.push({
        name: t('actions.move'),
        key: 'move',
        icon: 'icon-yidong',
        disabled: !isAdmin.value && !permissions.includes(FuncPlanPermission.MODIFY_CASE),
        hide: false
      });

      if (favourite) {
        menuItems.push({
          name: t('actions.cancelFavourite'),
          key: 'cancelFavourite',
          icon: 'icon-quxiaoshoucang',
          disabled: false,
          hide: false
        });
      } else {
        menuItems.push({
          name: t('actions.addFavourite'),
          key: 'addFavourite',
          icon: 'icon-yishoucang',
          disabled: false,
          hide: false
        });
      }

      if (follow) {
        menuItems.push({
          name: t('actions.cancelFollow'),
          key: 'cancelFollow',
          icon: 'icon-quxiaoguanzhu',
          disabled: false,
          hide: false
        });
      } else {
        menuItems.push({
          name: t('actions.addFollow'),
          key: 'addFollow',
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
  return checkedCaseInfo?.value?.id ?? 0;
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
          @move="handleDragMove"
          @start="handleDragStart"
          @end="handleDragEnd"
          @add="handleDragAdd($event, item.value)">
          <template #item="{ element, index }: { element: CaseDetail; index: number; }">
            <div
              :id="`${item.value}-${index}-${element.id}`"
              :class="{ 'active-item': checkedCaseId === element.id }"
              class="case-board-item border border-solid rounded border-theme-text-box p-2 space-y-1.5"
              @click="handleSelectCard(element,index)">
              <div class="flex items-center overflow-hidden">
                <IconTask :value="element.testResult.value" class="mr-1.5" />
                <span :title="element.name" class="flex-1 truncate font-semibold">{{ element.name }}</span>
                <span
                  v-if="element.overdue"
                  class="flex-shrink-0 border border-testResult-error rounded px-0.5"
                  style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
                  <span class="inline-block transform-gpu scale-90">{{ t('status.overdue') }}</span>
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
                      <span class="flex-shrink-0">{{ t('common.tester') }}</span>
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
                <span class="flex-shrink-0">{{ t('common.deadlineDate') }}</span>
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
              <span v-if="!openFlag">{{ t('common.expandAll') }}</span>
              <span v-else>{{ t('common.collapseAll') }}</span>
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
          <span class="font-semibold">{{ t('common.swimLane') }}</span>
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
              <span>{{ t('common.useCase') }}</span>
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
                @move="handleDragMove"
                @start="handleDragStart"
                @end="handleDragEnd"
                @add="handleGroupDragAdd($event, _testResult.value)">
                <template #item="{ element, index }: { element: CaseDetail; index: number; }">
                  <div
                    :id="`${_testResult.value}-${index}-${element.id}-${_createdByName.value}`"
                    :class="{ 'active-item': checkedCaseId === element.id }"
                    class="case-board-item border border-solid rounded border-theme-text-box p-2 space-y-1.5"
                    @click="handleSelectCard(element,index,String(_createdByName.value))">
                    <div class="flex items-center overflow-hidden">
                      <IconTask :value="element.testResult.value" class="mr-1.5" />
                      <span :title="element.name" class="flex-1 truncate font-semibold">{{ element.name }}</span>
                      <span
                        v-if="element.overdue"
                        class="flex-shrink-0 border border-testResult-error rounded px-0.5"
                        style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
                        <span class="inline-block transform-gpu scale-90">{{ t('status.overdue') }}</span>
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
                            <span class="flex-shrink-0">{{ t('common.tester') }}</span>
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
                      <span class="flex-shrink-0">{{ t('common.deadlineDate') }}</span>
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
          :title="t('common.basicInfo')"
          @click="drawerActiveKeyChange('basic')">
          <Icon icon="icon-wendangxinxi" class="text-4" />
        </div>

        <div
          v-if="false"
          :class="{ 'drawer-active-item': drawerActiveKey === 'testStep' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.testSteps')"
          @click="drawerActiveKeyChange('testStep')">
          <Icon icon="icon-renwu2" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'person' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.personnel')"
          @click="drawerActiveKeyChange('person')">
          <Icon icon="icon-quanburenyuan" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'date' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.date')"
          @click="drawerActiveKeyChange('date')">
          <Icon icon="icon-riqi" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'reviewInfo' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.reviewInfo')"
          @click="drawerActiveKeyChange('reviewInfo')">
          <Icon icon="icon-pingtaimorenzhibiao1" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'testInfo' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.testInfo')"
          @click="drawerActiveKeyChange('testInfo')">
          <Icon icon="icon-renwuceshibaogao" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'assocIssues' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.assocIssues')"
          @click="drawerActiveKeyChange('assocIssues')">
          <Icon icon="icon-ceshirenwu" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'assocCases' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.assocCases')"
          @click="drawerActiveKeyChange('assocCases')">
          <Icon icon="icon-ceshiyongli1" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'attachments' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.attachment')"
          @click="drawerActiveKeyChange('attachments')">
          <Icon icon="icon-lianjie1" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'reviewRecord' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.reviewRecord')"
          @click="drawerActiveKeyChange('reviewRecord')">
          <Icon icon="icon-pingshen" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'comment' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.comment')"
          @click="drawerActiveKeyChange('comment')">
          <Icon icon="icon-pinglun" class="text-4" />
        </div>

        <div
          :class="{ 'drawer-active-item': drawerActiveKey === 'activity' }"
          class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
          :title="t('common.activity')"
          @click="drawerActiveKeyChange('activity')">
          <Icon icon="icon-chakanhuodong" class="text-4" />
        </div>
      </div>

      <div class="w-full h-full flex-1 overflow-hidden">
        <div class="flex items-center justify-between mt-4 pl-5 space-x-2.5">
          <div class="flex-1 flex items-center truncate">
            <RouterLink
              :to="`/test#${TestMenuKey.CASES}?id=${checkedPlanInfo?.id}`"
              :title="checkedPlanInfo?.name"
              class="truncate"
              style="max-width: 50%;">
              {{ checkedPlanInfo?.name }}
            </RouterLink>
            <div class="mx-1.5">/</div>
            <RouterLink
              :to="`/test#${TestMenuKey.CASES}?id=${checkedCaseInfo?.id}`"
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

            <AssocIssues
              v-show="drawerActiveKey === 'assocIssues'"
              :projectId="props.projectId"
              :appInfo="props.appInfo"
              :userInfo="props.userInfo"
              :dataSource="checkedCaseInfo"
              :canEdit="!!menuItemsMap.get(checkedCaseId)?.find(item=>item.key==='edit')"
              @change="caseInfoChange"
              @loadingChange="loadingChange" />

            <AssocCases
              v-show="drawerActiveKey === 'assocCases'"
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
      :taskType="TaskType.BUG"
      :confirmerId="selectedCaseInfo?.testerId"
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
