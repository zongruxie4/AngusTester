<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onMounted, ref, watch } from 'vue';
import { Button, Switch } from 'ant-design-vue';
import {
  AsyncComponent,
  Colon,
  Dropdown,
  DropdownGroup,
  DropdownSort,
  Icon,
  IconCount,
  IconRefresh,
  Input,
  modal,
  NoData,
  notification,
  QuickSelect,
  ReviewStatus,
  SearchPanel,
  Select,
  TaskPriority,
  TestResult,
  Tooltip
} from '@xcan-angus/vue-ui';
import { clipboard, http, duration, download, TESTER, enumLoader, XCanDexie } from '@xcan-angus/tools';
import dayjs, { Dayjs } from 'dayjs';
import { debounce } from 'throttle-debounce';
import { funcCase, modules, funcPlan, analysis } from '@/api/tester';

import { CaseActionAuth, CaseInfoObj, CaseListObj, CountObj, EnabledGroup, travelTreeData } from './PropsType';
import { PlanObj } from '../../PropsType';
import { CASE_PROJECT_PERMISSIONS, useCaseActionAuth } from '../useCaseActionAuth';
// eslint-disable-next-line import/no-absolute-path
import Template from '/file/Import_Case_Template.xlsx?url';

const InfoView = defineAsyncComponent(() => import('./infoView.vue'));
const TableView = defineAsyncComponent(() => import('./tableView.vue'));
const AddModal = defineAsyncComponent(() => import('@/views/function/case/list/case/add/index.vue'));
const AiAddModal = defineAsyncComponent(() => import('@/views/function/case/list/case/add/aiAdd.vue'));
const CaseDetail = defineAsyncComponent(() => import('@/views/function/case/detail/index.vue'));
const ReviewModal = defineAsyncComponent(() => import('@/views/function/case/list/case/review/index.vue'));
const MoveModal = defineAsyncComponent(() => import('@/views/function/case/list/case/move/index.vue'));
const UpdateTestResultModal = defineAsyncComponent(() => import('@/views/function/case/list/case/updateResult/index.vue'));
const TagList = defineAsyncComponent(() => import('@/views/function/case/list/case/tagSelector/index.vue'));
const PlanList = defineAsyncComponent(() => import('@/views/function/case/list/case/planSelector/index.vue'));
const UploadCaseModal = defineAsyncComponent(() => import('@/views/function/case/list/case/upload/index.vue'));
const ModuleTree = defineAsyncComponent(() => import('./moduleTree.vue'));
const KanbanView = defineAsyncComponent(() => import('@/views/function/case/list/case/kanban//index.vue'));
const AddTaskModal = defineAsyncComponent(() => import('@/views/task/task/list/task/edit/index.vue'));

type FilterOp = 'EQUAL' | 'NOT_EQUAL' | 'GREATER_THAN' | 'GREATER_THAN_EQUAL' | 'LESS_THAN' | 'LESS_THAN_EQUAL' | 'CONTAIN' | 'NOT_CONTAIN' | 'MATCH_END' | 'MATCH' | 'IN' | 'NOT_IN';
type Filters = { key: string, value: string | boolean | string[], op: FilterOp }

interface IData {
  id: string;
  data: any
}

interface Props {
  loading: boolean;
  planInfo: PlanObj;
  viewMode: 'table' | 'tile' | 'kanban';
  isOpenCount: boolean;
  count?: CountObj;
  notify?: number;
  planId?: string;
  planName?: string;
  tabInfo?: Record<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
  notify: 0,
  viewMode: undefined,
  count: undefined,
  planId: undefined,
  planName: undefined,
  tabInfo: undefined
});

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'update:count', value: CountObj): void;
  (e: 'openInfo', infoTabParams): void;
  (e: 'updateFollowFavourite', type: 'follow' | 'favourite'): void;
  (e: 'cacheParams', value: any): void;
  (e: 'viewModeChange', value:'table' | 'tile' | 'kanban'): void;
  (e: 'countChange'): void;
}>();

const projectInfo = inject('projectInfo', ref({ id: '' }));
const userInfo = inject<{ id: string, fullName: string }>('userInfo');
const appInfo = inject<{ id: string, name: string }>('appInfo');
const aiEnabled = inject('aiEnabled', ref(false));
const updateLoading = inject<((value: boolean) => void)>('updateLoading', () => undefined);
const isAdmin = inject('isAdmin', ref(false));
const defaultUser = { [userInfo.id]: { fullName: userInfo.fullName, id: userInfo.id } };
const { getActionAuth } = useCaseActionAuth();

const boardsGroupKey = ref<'none' | 'testerName' | 'lastModifiedByName'>('none');
const boardsOrderBy = ref<'priority' | 'deadlineDate' | 'createdByName' | 'testerName'>();
const boardsOrderSort = ref<'DESC' | 'ASC'>();

const firstloadInfo = ref(true); // 是否是第一次加载
const firstloading = ref(true); // 是否是第一次加载
const actionType = ref(undefined);

const searchPanelRef = ref();

const params = ref({
  pageNo: 1,
  pageSize: 10,
  filters: []
});
const total = ref(0);

const loadingChange = (value:boolean) => {
  updateLoading(value);
};

const refreshChange = () => {
  // 刷新统计图表
  if (props.isOpenCount) {
    loadCaseCount();
  }
  loadData();
};

const setParamsAndLoadData = () => {
  params.value.pageNo = 1;
  actionType.value = 'search';
  setParamsFilters();
  setQuickType();
  refreshChange();
};

const enabledGroup = ref<EnabledGroup>(true);
const enabledGroupChange = (value) => {
  enabledGroup.value = value;
  if (enabledGroup.value) {
    moduleId.value = '';
  } else {
    moduleId.value = undefined;
  }
};

const searchData = ref<Filters[]>([]);

const selectedTypes = ref<string[]>([]);

const overdueFlag = ref(false);
const overdueFlagChange = (_val) => {
  overdueFlag.value = _val;
  setParamsAndLoadData();
};

const planId = ref();
const planName = ref();

const tagIds = ref<string[]>([]);

const tagChange = (_tagIds: string[]) => {
  tagIds.value = _tagIds;
  setParamsAndLoadData();
};

const planChange = (_planId: string) => {
  planId.value = _planId;
  setParamsAndLoadData();
};

const testNum = ref(''); // 测试次数
const testNumScope = ref<FilterOp>('EQUAL'); // 测试次数p
const testFailNum = ref(''); // 失败次数
const testFailScope = ref<FilterOp>('EQUAL'); // 失败次数op
const reviewNum = ref(''); // 评审次数
const reviewNumScope = ref<FilterOp>('EQUAL'); // 评审次数op

const handleTimesChange = debounce(duration.resize, (value: string, type: 'testNum' | 'testFailNum' | 'reviewNum') => {
  if (type === 'testNum') {
    testNum.value = value;
  }
  if (type === 'testFailNum') {
    testFailNum.value = value;
  }
  if (type === 'reviewNum') {
    reviewNum.value = value;
  }

  setParamsAndLoadData();
});

const handleScopeChange = (value: FilterOp, type: 'testNum' | 'testFailNum' | 'reviewNum') => {
  if (type === 'testNum') {
    testNumScope.value = value;
    if (!testNum.value) {
      return;
    }
  }
  if (type === 'testFailNum') {
    testFailScope.value = value;
    if (!testFailNum.value) {
      return;
    }
  }
  if (type === 'reviewNum') {
    reviewNumScope.value = value;
    if (!reviewNum.value) {
      return;
    }
  }

  setParamsAndLoadData();
};

const setParamsFilters = () => {
  const othersData: Filters[] = [];
  if (testNum.value) {
    othersData.push({ key: 'testNum', op: testNumScope.value, value: testNum.value });
  }
  if (testFailNum.value) {
    othersData.push({ key: 'testFailNum', op: testFailScope.value, value: testFailNum.value });
  }
  if (testFailNum.value) {
    othersData.push({ key: 'reviewNum', op: reviewNumScope.value, value: reviewNum.value });
  }

  if (planId.value) {
    othersData.push(
      {
        key: 'planId',
        op: 'EQUAL',
        value: planId.value
      }
    );
  }
  if (overdueFlag.value) {
    othersData.push({
      key: 'overdueFlag',
      op: 'EQUAL',
      value: true
    });
  }

  if (tagIds.value?.length) {
    const set = new Set(tagIds.value);
    othersData.push({
      key: 'tagId',
      op: 'IN',
      value: Array.from(set)
    });
  }

  params.value.filters = [...searchData.value, ...othersData];
};

const quickSelectDate = ref<string[]>([]);
const getQuickDate = (type) => {
  let _startDate: Dayjs | undefined;
  let _endDate: Dayjs | undefined;

  if (type === 'lastDay') {
    _startDate = dayjs().startOf('date');
    _endDate = dayjs().endOf('date');
  }

  if (type === 'lastThreeDays') {
    _startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
    _endDate = dayjs();
  }

  if (type === 'lastWeek') {
    _startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
    _endDate = dayjs();
  }

  return [_startDate ? _startDate.format('YYYY-MM-DD HH:mm:ss') : '', _endDate ? _endDate.format('YYYY-MM-DD HH:mm:ss') : ''];
};

// 比较快速选中的时间和搜索框时间是否一致
const getCreatedDateIsQuickDate = (createdDateItems, quickSelectDate) => {
  let hasUpDateTime = false;
  if (createdDateItems.length) {
    for (let i = 0; i < createdDateItems.length; i++) {
      const item = createdDateItems[i];
      if (item.key === 'createdDate' && item.op === 'GREATER_THAN_EQUAL') {
        hasUpDateTime = item.value === quickSelectDate[0];
      }

      if (item.key === 'createdDate' && item.op === 'LESS_THAN_EQUAL') {
        hasUpDateTime = item.value === quickSelectDate[1];
      }
    }
  }
  return hasUpDateTime;
};

const quickSearchChange = (types: string[], allType: boolean) => {
  params.value.pageNo = 1;
  selectedTypes.value = types;
  actionType.value = 'search';
  const _filters = params.value.filters || [];
  const createdByItem = _filters.find(item => item.key === 'createdBy');
  const testerIdItem = _filters.find(item => item.key === 'testerId');
  const testResultItem = _filters.find(item => item.key === 'testResult');
  const createdDateItems = _filters.filter(item => item.key === 'createdDate');
  if (types.includes('all')) {
    // 主动选中全部
    if (allType) {
      testNum.value = '';
      testFailNum.value = '';
      reviewNum.value = '';
      overdueFlag.value = false;
      // planId.value = '';
      // tagIds.value = [];
      if (_filters?.length) {
        const ohtersParams = _filters;
        if (ohtersParams.length) {
          searchPanelRef.value.clear();
        } else {
          refreshChange();
        }
      }
      return;
    }

    if (_filters?.length) {
      const hasUpDateTime = getCreatedDateIsQuickDate(createdDateItems, quickSelectDate.value);

      const configItems = [];
      if (hasUpDateTime) {
        configItems.push({ valueKey: 'createdDate', value: undefined });
      }

      if (createdByItem?.value === userInfo.id) {
        configItems.push({ valueKey: 'createdBy', value: undefined });
      }

      if (testerIdItem?.value === userInfo.id && testResultItem?.value === 'PENDING') {
        configItems.push({ valueKey: 'testerId', value: undefined });
        configItems.push({ valueKey: 'testResult', value: undefined });
      }

      if (configItems.length) {
        searchPanelRef.value.setConfigs(configItems);
      } else {
        selectedTypes.value = [];
      }
    }

    return;
  }

  if (types.includes('lastDay') || types.includes('lastThreeDays') || types.includes('lastWeek')) {
    if (types.includes('lastDay')) {
      const _date = getQuickDate('lastDay');
      quickSelectDate.value = _date;
    }

    if (types.includes('lastThreeDays')) {
      const _date = getQuickDate('lastThreeDays');
      quickSelectDate.value = _date;
    }

    if (types.includes('lastWeek')) {
      const _date = getQuickDate('lastWeek');
      quickSelectDate.value = _date;
    }
  }

  const configItems = [];

  if (types.includes('createdBy')) {
    configItems.push({ valueKey: 'createdBy', value: userInfo.id, defaultOptions: defaultUser });
  } else {
    if (createdByItem?.value === userInfo.id) {
      configItems.push({ valueKey: 'createdBy', value: undefined });
    }
  }

  if (types.includes('testerId')) {
    configItems.push({ valueKey: 'testerId', value: userInfo.id, defaultOptions: defaultUser });
    configItems.push({ valueKey: 'testResult', value: 'PENDING' });
  } else {
    if (testerIdItem?.value === userInfo.id && testResultItem?.value === 'PENDING') {
      configItems.push({ valueKey: 'testerId', value: undefined });
      configItems.push({ valueKey: 'testResult', value: undefined });
    }
  }

  if (types.includes('lastDay') || types.includes('lastThreeDays') || types.includes('lastWeek')) {
    configItems.push({ valueKey: 'createdDate', value: quickSelectDate.value });
  } else {
    const hasUpDateTime = getCreatedDateIsQuickDate(createdDateItems, quickSelectDate.value);
    if (hasUpDateTime) {
      configItems.push({ valueKey: 'createdDate', value: undefined });
    }
  }

  if (configItems.length) {
    searchPanelRef.value.setConfigs(configItems);
  }
};

const cacheParamsKey = computed(() => {
  return `${userInfo?.id}${projectInfo.value.id}_case`;
});

let isInit = true;
let db: Dexie<IData>;
const init = async () => {
  if (!db) {
    db = new XCanDexie<IData>('parameter');
  }

  const [, _data] = await db.get(cacheParamsKey.value);
  if (!_data) {
    selectedTypes.value = ['all'];

    refreshChange();
    return;
  }

  enabledGroup.value = !!_data?.data.enabledGroup;
  if (enabledGroup.value) {
    moduleId.value = '';
  } else {
    moduleId.value = undefined;
  }
  const _cacheFilters = _data?.data.filters;
  if (_cacheFilters.length) {
    params.value.filters = _cacheFilters;
    setQuickType();
    const _otherFilters: Filters[] = [];
    for (let i = 0; i < _cacheFilters.length; i++) {
      const item = _cacheFilters[i] as Filters;
      if (['planId', 'tagId', 'overdueFlag', 'testNum', 'testFailNum', 'reviewNum'].includes(item.key)) {
        if (item.key === 'planId') {
          planId.value = item.value as string;
        }

        if (item.key === 'tagId') {
          const set = new Set(item.value);
          tagIds.value = Array.from(set);
        }

        if (item.key === 'overdueFlag') {
          overdueFlag.value = item.value as boolean;
        }

        if (item.key === 'testNum') {
          testNum.value = item.value as string;
          testNumScope.value = item.op as FilterOp;
        }

        if (item.key === 'testFailNum') {
          testFailNum.value = item.value as string;
          reviewNumScope.value = item.op as FilterOp;
        }

        if (item.key === 'reviewNum') {
          reviewNum.value = item.value as string;
          reviewNumScope.value = item.op as FilterOp;
        }
      } else {
        _otherFilters.push(item);
      }
    }

    if (_otherFilters.length) {
      const defaultParams = [];
      const createdDataValue: string[] = [];
      const deadlineDateValue: string[] = [];
      const reviewDateValue: string[] = [];
      const lastModifiedDateValue: string[] = [];
      const testResultHandleDateValue: string[] = [];
      for (let i = 0; i < _otherFilters.length; i++) {
        const item = _otherFilters[i] as Filters;

        if (item.key === 'createdDate') {
          if (item.op === 'GREATER_THAN_EQUAL') {
            createdDataValue[0] = item.value as string;
          }

          if (item.op === 'LESS_THAN_EQUAL') {
            createdDataValue[1] = item.value as string;
          }
        } else if (item.key === 'deadlineDate') {
          if (item.op === 'GREATER_THAN_EQUAL') {
            deadlineDateValue[0] = item.value as string;
          }

          if (item.op === 'LESS_THAN_EQUAL') {
            deadlineDateValue[1] = item.value as string;
          }
        } else if (item.key === 'reviewDate') {
          if (item.op === 'GREATER_THAN_EQUAL') {
            reviewDateValue[0] = item.value as string;
          }

          if (item.op === 'LESS_THAN_EQUAL') {
            reviewDateValue[1] = item.value as string;
          }
        } else if (item.key === 'lastModifiedDate') {
          if (item.op === 'GREATER_THAN_EQUAL') {
            lastModifiedDateValue[0] = item.value as string;
          }

          if (item.op === 'LESS_THAN_EQUAL') {
            lastModifiedDateValue[1] = item.value as string;
          }
        } else if (item.key === 'testResultHandleDate') {
          if (item.op === 'GREATER_THAN_EQUAL') {
            testResultHandleDateValue[0] = item.value as string;
          }

          if (item.op === 'LESS_THAN_EQUAL') {
            testResultHandleDateValue[1] = item.value as string;
          }
        } else {
          defaultParams.push({ valueKey: item.key, value: item.value });
        }
      }

      if (createdDataValue.length) {
        defaultParams.push({ valueKey: 'createdDate', value: createdDataValue });
      }

      if (deadlineDateValue.length) {
        defaultParams.push({ valueKey: 'deadlineDate', value: deadlineDateValue });
      }

      if (reviewDateValue.length) {
        defaultParams.push({ valueKey: 'reviewDate', value: reviewDateValue });
      }

      if (lastModifiedDateValue.length) {
        defaultParams.push({ valueKey: 'lastModifiedDate', value: lastModifiedDateValue });
      }

      if (testResultHandleDateValue.length) {
        defaultParams.push({ valueKey: 'testResultHandleDate', value: testResultHandleDateValue });
      }

      if (defaultParams.length) {
        searchPanelRef.value.setConfigs(defaultParams);
      }
    }

    refreshChange();
  } else {
    selectedTypes.value = ['all'];
    refreshChange();
  }
};

const loadData = () => {
  if (props.viewMode !== 'kanban') {
    loadCaseList();
  }
};

const loadCaseCount = async (): Promise<void> => {
  const [error, { data }] = await analysis.loadFuncCaseCount({ ...params.value, projectId: projectInfo.value?.id, moduleId: moduleId.value });
  if (error) {
    return;
  }
  emits('update:count', data);
};

// 表格操作选中的当前rowData
const checkedCase = ref<CaseListObj>();

const caseList = ref<CaseListObj[]>([]);

const loadCaseList = async (): Promise<void> => {
  if (props.loading) {
    return;
  }

  updateLoading(true);
  const [error, { data = { list: [], total: 0 } }] = await funcCase.loadFuncCase({infoScope: 'DETAIL', ...params.value, projectId: projectInfo.value?.id, moduleId: moduleId.value });
  firstloading.value = false;
  if (error) {
    updateLoading(false);
    return;
  }
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { pageNo, pageSize, ...otherParam } = params.value;

  db.add(JSON.parse(JSON.stringify({ id: cacheParamsKey.value, data: JSON.parse(JSON.stringify({ ...otherParam, enabledGroup: enabledGroup.value })) })));

  if (props.viewMode === 'table' && tabViewRef.value) {
    tabViewRef.value.selectedRowKeys = [];
  }

  selectedRowKeys.value = [];

  caseList.value = data.list.map(item => ({ ...item, checked: false }));
  getPlanAuth();

  total.value = data.total;
  if (!data.list.length) {
    checkedCase.value = undefined;
    updateLoading(false);
    return;
  }

  if (firstloadInfo.value || actionType.value === 'del') {
    checkedCase.value = data.list[0];
    firstloadInfo.value = false;
    actionType.value = undefined;
  }

  if (actionType.value === 'search') {
    const ids = data.list.map(item => item.id);
    if (!ids.includes(checkedCase.value?.id)) {
      checkedCase.value = data.list[0];
    }
    actionType.value = undefined;
  }
  if (!isInit) {
    await getPlanAuth();
  }
  isInit = false;
  if (props.viewMode === 'tile') {
    if (!checkedCase.value) {
      checkedCase.value = data.list[0];
    }
    await getCaseInfo(checkedCase.value?.id);
  }
  updateLoading(false);
};

const tableAction = computed(() => {
  const action = { auth: {}, menus: {} };
  for (let i = 0; i < caseList.value.length; i++) {
    const _case = caseList.value[i];
    if (userInfo?.id === _case.testerId && (planAuthMap.value[_case.planId]?.permissions || []).includes('TEST')) {
      planAuthMap.value[_case.planId]?.permissions && planAuthMap.value[_case.planId].permissions.push('TEST');
    }
    action.auth[_case.id] = getActionAuth((planAuthMap.value[_case.planId]?.permissions || [])); // getActionAuth(planAuthList.value);
    if (action.auth[_case.id].includes('resetTestResult')) {
      if (!planAuthMap.value[_case.planId].funcPlanAuthFlag) {
        action.auth[_case.id] = action.auth[_case.id].filter(i => i !== 'resetTestResult');
      }
    }
    if (action.auth[_case.id].includes('retestResult')) {
      if (!planAuthMap.value[_case.planId].funcPlanAuthFlag && userInfo?.id !== _case.testerId) {
        action.auth[_case.id] = action.auth[_case.id].filter(i => i !== 'retestResult');
      }
    }
    action.menus[_case.id] = [];

    // 测试次数大于0才允许重置测试结果
    if (+_case.testNum > 0) {
      action.menus[_case.id].push({
        key: 'resetTestResult',
        icon: 'icon-zhongzhiceshijieguo',
        name: '重置测试结果',
        permission: 'resetTestResult'
      });
    }

    if (!_case?.review || (_case?.review && _case?.reviewStatus.value === 'PASSED')) {
      action.menus[_case.id].push({
        key: 'updateTestResult_canceled',
        icon: 'icon-xiugaiceshijieguo',
        name: '取消',
        permission: 'edit'
      });

      if (!['PASSED', 'NOT_PASSED', 'CANCELED'].includes(_case?.testResult?.value)) {
        action.menus[_case.id].push({
          key: 'updateTestResult_passed',
          icon: 'icon-xiugaiceshijieguo',
          name: '测试通过',
          permission: 'updateTestResult'
        }, {
          key: 'updateTestResult_notpassed',
          icon: 'icon-xiugaiceshijieguo',
          name: '测试未通过',
          permission: 'updateTestResult'
        }, {
          key: 'updateTestResult_blocked',
          icon: 'icon-xiugaiceshijieguo',
          name: '设为阻塞中',
          permission: 'updateTestResult'
        });
      } else {
        action.menus[_case.id].push({
          key: 'retestResult',
          icon: 'icon-xiugaiceshijieguo',
          name: '重新测试',
          permission: 'retestResult'
        });
      }
    }

    if (action.auth[_case.id].includes('move')) {
      action.menus[_case.id].push({
        key: 'move',
        icon: 'icon-yidong',
        name: '移动',
        permission: 'move'
      });
    }

    action.menus[_case.id].push(
      {
        key: 'clone',
        icon: 'icon-fuzhi',
        name: '克隆',
        permission: 'clone'
      },
      {
        key: 'favourite',
        icon: _case.favouriteFlag ? 'icon-quxiaoshoucang' : 'icon-yishoucang',
        name: _case.favouriteFlag ? '取消收藏' : '收藏',
        permission: 'edit',
        noAuth: true
      },
      {
        key: 'follow',
        icon: _case.followFlag ? 'icon-quxiaoguanzhu' : 'icon-yiguanzhu',
        name: _case.followFlag ? '取消关注' : '关注',
        permission: 'edit',
        noAuth: true
      },
      {
        key: 'copyUrl',
        icon: 'icon-fuzhi',
        name: '复制连接',
        noAuth: true
      }
    );
  }
  return action;
});

const searchChange = (data: Filters[], _header, valueKey) => {
  params.value.pageNo = 1;
  searchData.value = data;
  actionType.value = 'search';
  setParamsFilters();

  if (valueKey) {
    setQuickType();
  } else {
    if (selectedTypes.value.includes('all')) {
      const _filters = params.value.filters.filter(item => !['planId', 'tagId'].includes(item.key));
      if (_filters.length) {
        selectedTypes.value = [];
      }
    }
  }

  refreshChange();
};

const setQuickType = () => {
  const _filters = params.value.filters || [];
  if (_filters.length === 0) {
    selectedTypes.value = ['all'];
    return;
  }

  const filterKeys = _filters.map(item => item.key);
  if (filterKeys.every(item => ['planId', 'tagId'].includes(item))) {
    selectedTypes.value = ['all'];
    return;
  }

  selectedTypes.value = selectedTypes.value.filter(item => item !== 'all');
  const createdByElement = _filters.find(element => element.key === 'createdBy');
  const testerIdItem = _filters.find(item => item.key === 'testerId');
  const testResultItem = _filters.find(item => item.key === 'testResult');
  const createdDateItems = _filters.filter(item => item.key === 'createdDate');
  if (createdByElement?.value === userInfo.id) {
    if (!selectedTypes.value.includes('createdBy')) {
      selectedTypes.value.push('createdBy');
    }
  } else {
    if (selectedTypes.value.includes('createdBy')) {
      selectedTypes.value = selectedTypes.value.filter(item => item !== 'createdBy');
    }
  }
  if (testerIdItem?.value === userInfo.id && testResultItem?.value === 'PENDING') {
    if (!selectedTypes.value.includes('testerId')) {
      selectedTypes.value.push('testerId');
    }
  } else {
    if (selectedTypes.value.includes('testerId')) {
      selectedTypes.value = selectedTypes.value.filter(item => item !== 'testerId');
    }
  }

  if (createdDateItems.length && (selectedTypes.value.includes('lastDay') || selectedTypes.value.includes('lastThreeDays') || selectedTypes.value.includes('lastWeek'))) {
    let hasUpDateTime = false;
    for (let i = 0; i < createdDateItems.length; i++) {
      const item = createdDateItems[i];
      if (item.key === 'createdDate' && item.op === 'GREATER_THAN_EQUAL') {
        hasUpDateTime = item.value === quickSelectDate.value[0];
      }

      if (item.key === 'createdDate' && item.op === 'LESS_THAN_EQUAL') {
        hasUpDateTime = item.value === quickSelectDate.value[1];
      }
    }

    if (!hasUpDateTime) {
      selectedTypes.value = selectedTypes.value.filter(item => item !== 'lastDay' && item !== 'lastThreeDays' && item !== 'lastWeek');
    }
  } else {
    selectedTypes.value = selectedTypes.value.filter(item => item !== 'lastDay' && item !== 'lastThreeDays' && item !== 'lastWeek');
  }
};

// 用例详情
const caseInfo = ref<CaseInfoObj>();
const firstCase = ref<CaseInfoObj>();
const getCaseInfo = async (id: string) => {
  updateLoading(true);
  const [error, { data }] = await funcCase.getCaseInfo(id);
  updateLoading(false);
  if (error) {
    return;
  }
  caseInfo.value = data;
  checkedCase.value = data;
  firstCase.value = data;
};

const addVisible = ref(false);
const handleAdd = () => {
  editCase.value = undefined;
  addVisible.value = true;
};

const aiAddVisible = ref(false);
const handleAiAdd = () => {
  aiAddVisible.value = true;
};

// 添加接口用例model visible
// const addApiVisible = ref(false);

const planAuthMap = ref<{[id: string]: {
  funcPlanAuthFlag: boolean;
  permissions: string[];
}}>({});
const getPlanAuth = async () => {
  if (isAdmin.value) {
    caseList.value.forEach(i => {
      planAuthMap.value[i.planId] = {
        funcPlanAuthFlag: true,
        permissions: CASE_PROJECT_PERMISSIONS
      };
    });
    return;
  }
  const planIds = caseList.value.map(i => i.planId);
  if (!planIds.length) {
    return;
  }
  const [error, { data }] = await funcPlan.getCurrentAuth({
    ids: planIds,
    admin: true
  });
  if (error) {
    return;
  }

  planIds.map(planId => {
    return planAuthMap.value[planId] = {
      ...data[planId],
      permissions: (data[planId].permissions || []).map(i => i.value)
    };
  });
  // if (data.funcPlanAuthFlag) {
  //   planAuthList.value = data.permissions.map(item => {
  //     return item.value;
  //   });
  // } else {
  //   planAuthList.value = CASE_PROJECT_PERMISSIONS;
  // }
};

const NumberMatchCondition = ref<{ value: string, message: string }[]>([{ value: 'EQUAL', message: '等于' }]);
const loadEnums = async () => {
  const [error, data] = await enumLoader.load('NumberCompareCondition');
  if (error) {
    return;
  }
  NumberMatchCondition.value = data;
};

// 模块相关
const moduleTreeData = ref([{ name: '无模块用例', id: '-1' }]);
const moduleId = ref();
const loadModuleTree = async (keywords?: string) => {
  const [error, { data }] = await modules.searchTree( {
    projectId: projectInfo.value?.id,
    filters: keywords
      ? [{
          value: keywords,
          op: 'MATCH_END',
          key: 'name'
        }]
      : []
  });
  if (error) {
    return;
  }
  moduleTreeData.value = [{ name: '无模块用例', id: '-1' }, ...travelTreeData(data || [])];
  if (moduleId.value && keywords && !moduleTreeData.value.find(item => item.id === moduleId.value)) {
    moduleId.value = '';
  }
};

onMounted(async () => {
  await loadEnums();
  await loadModuleTree();
  await init();

  watch(() => projectInfo.value?.id, newValue => {
    if (newValue) {
      moduleId.value = '';
      loadModuleTree();
    }
  });
  watch(() => moduleId.value, () => {
    setParamsAndLoadData();
  });
});

// 所有所选中的用例的Ids
const selectedRowKeys = ref<string[]>([]);

// 操作类型:batch:批量 one:操作一条数据
const btnType = ref<'batch' | 'one'>('one');
const reviewVisible = ref(false);
const selectedCase = ref<CaseListObj>();
const moveVisible = ref(false);
const updateTestResultVisible = ref(false);
const resultPassed = ref(false);

const batchDisabled = ref({
  review: false,
  move: false,
  del: false,
  updateTestResult: false
});

watch(() => selectedRowKeys.value, (newValue) => {
  btnType.value = 'batch';
  batchDisabled.value = {
    move: false,
    del: false,
    updateTestResult: false
  };
  const list = caseList.value;

  const selectList = list.filter(item => newValue.includes(item.id));
  if (selectList.some(_case => {
    if (planAuthMap.value[_case.planId].funcPlanAuthFlag) {
      return !(planAuthMap.value[_case.planId]?.permissions || []).includes('MODIFY_CASE');
    }
    return false;
  })) {
    batchDisabled.value.move = true;
  }
  if (selectList.some(_case => {
    if (planAuthMap.value[_case.planId].funcPlanAuthFlag) {
      return !planAuthMap.value[_case.planId]?.permissions?.includes('DELETE_CASE');
    }
    return false;
  })) {
    batchDisabled.value.del = true;
  } // TEST

  if (selectList.some(_case => {
    if ((_case?.review && _case?.reviewStatus.value !== 'PASSED') || ['PASSED', 'NOT_PASSED', 'CANCELED'].includes(_case.testResult.value)) {
      return true;
    }
    if (planAuthMap.value[_case.planId].funcPlanAuthFlag) {
      return !planAuthMap.value[_case.planId]?.permissions?.includes('TEST');
    }
    return false;
  })) {
    batchDisabled.value.updateTestResult = true;
  } // TEST
});

// 移动成功
const moveSuccess = () => {
  refreshChange();
};

// 修改成功
const updteSuccess = () => {
  refreshChange();
};

const editSuccess = () => {
  refreshChange();
};

// 回收站数据取消 更新列表
watch(() => props.notify, () => {
  checkedCase.value = undefined;
  caseInfo.value = undefined;
  refreshChange();
});

const handleClone = async (rowData: CaseListObj) => {
  updateLoading(true);
  const [error] = await funcCase.cloneCase([rowData.id]);
  if (error) {
    updateLoading(false);
    return;
  }
  notification.success('克隆成功');
  refreshChange();
};

// eslint-disable-next-line @typescript-eslint/no-unused-vars, @typescript-eslint/no-empty-function
const refreshRecycleBin = inject('refreshRecycleBin', (_key: 'useCase') => { });

// 删除
const handleDelete = async (rowData?: CaseListObj) => {
  modal.confirm({
    centered: true,
    title: '删除用例',
    content: rowData ? `确定删除用例【${rowData.name}】吗？` : '确定删除选中的用例吗？',
    async onOk () {
      await delCase(rowData);
    }
  });
};

const delCase = async (rowData?: CaseListObj) => {
  updateLoading(true);
  const [error] = await funcCase.deleteCase(rowData ? [rowData.id] : selectedRowKeys.value);
  if (error) {
    updateLoading(false);
    return;
  }

  params.value.pageNo = getCurrentPage(params.value.pageNo, params.value.pageSize, total.value);
  caseInfo.value = undefined;
  checkedCase.value = undefined;
  actionType.value = 'del';
  refreshChange();
  refreshRecycleBin('useCase');
  notification.success('删除成功，您可以在回收站查看删除后的用例');
};

const getCurrentPage = (pageNo: number, pageSize: number, total: number): number => {
  if (pageNo === 1 || pageSize >= total) {
    return 1;
  }

  const remainder = total % pageSize;
  if (remainder === 1) {
    const totalPage = Math.ceil(total / pageSize);
    if (totalPage === pageNo) {
      return pageNo - 1;
    }
  }

  return pageNo;
};

// 重置测试
const hanldeResetTestResults = async (rowData: CaseListObj) => {
  updateLoading(true);
  const [error] = await funcCase.resetResult([rowData.id], );
  if (error) {
    updateLoading(false);
    return;
  }
  notification.success('重置测试结果成功');
  refreshChange();
};

// 重置评审
const handleResetReviewResult = async (rowData: CaseListObj) => {
  updateLoading(true);
  const [error] = await funcCase.resetReview([rowData.id]);
  if (error) {
    updateLoading(false);
    return;
  }
  notification.success('重置评审结果成功');
  refreshChange();
};

// 重新测试 将用例改为待测试
const handleReTest = async (rowData: CaseListObj) => {
  updateLoading(true);
  const [error] = await funcCase.retestResult([rowData.id]);
  if (error) {
    updateLoading(false);
    return;
  }
  notification.success('重置测试状态成功');
  refreshChange();
};

// 平铺视图点击数据获取详情
const hanldeSelectCase = async (_case: CaseListObj) => {
  checkedCase.value = _case;
  if (_case.id === caseInfo.value?.id) {
    return;
  }

  await getCaseInfo(_case.id);
  nextTick(() => {
    const el = document.querySelector('CaseInfoRef');
    if (Array.isArray(el)) {
      el?.[0].scrollTo({ top: 0, behavior: 'smooth' });
      el?.[1].scrollTo({ top: 0, behavior: 'smooth' });
    } else {
      el?.scrollTo({ top: 0, behavior: 'smooth' });
    }
  });
};

// 详情操作按钮
const handleDetailAction = (type: CaseActionAuth, value: CaseListObj) => {
  switch (type) {
    case 'edit':
      handleEdit(value);
      break;
    case 'clone':
      handleClone(value);
      break;
    case 'move':
      handleAction('move', value);
      break;
    case 'delete':
      handleDelete(value);
      break;
    case 'review':
      handleAction('review', value);
      break;
    case 'updateTestResult_passed':
      handleAction('updateTestResult_passed', value);
      break;
    case 'updateTestResult_notpassed':
      handleAction('updateTestResult_notpassed', value);
      break;
    case 'updateTestResult_blocked':
      handleSetREsultBlocked(value);
      break;
    case 'updateTestResult_canceled':
      handleSetREsultCanceled(value);
      break;
    case 'resetTestResult':
      hanldeResetTestResults(value);
      break;
    case 'retestResult':
      handleReTest(value);
      break;
    case 'resetReviewResult':
      handleResetReviewResult(value);
      break;
    case 'favourite':
      handleFavourite(value);
      break;
    case 'follow':
      handleFollow(value);
      break;
    case 'copyUrl':
      handleCopy(value);
      break;
  }
};

// 评审 移动 修改测试结果封装逻辑
const handleAction = (action: 'review' | 'move' | 'updateTestResult_passed' | 'updateTestResult_notpassed', rowData?: CaseListObj) => {
  btnType.value = rowData ? 'one' : 'batch';
  selectedCase.value = rowData;
  switch (action) {
    case 'review':
      reviewVisible.value = true;
      break;
    case 'move':
      moveVisible.value = true;
      break;
    case 'updateTestResult_passed':
      updateTestResultVisible.value = true;
      resultPassed.value = true;
      break;
    case 'updateTestResult_notpassed':
      updateTestResultVisible.value = true;
      resultPassed.value = false;
      break;
  }
};

// 设为阻塞中
const handleSetREsultBlocked = async (value) => {
  const params = [
    {
      id: value.id,
      testResult: 'BLOCKED'
    }
  ];
  const [error] = await funcCase.updateResult(params);
  if (error) {
    return;
  }
  notification.success('用例设为阻塞中');
  refreshChange();
};

// 取消case
const handleSetREsultCanceled = async (value) => {
  const params = [
    {
      id: value.id,
      testResult: 'CANCELED'
    }
  ];
  const [error] = await funcCase.updateResult(params);
  if (error) {
    return;
  }
  notification.success('取消成功');
  refreshChange();
};

const handleCopy = async (value) => {
  const idIndex = caseList.value?.findIndex(item => item.id === value.id);
  const _qery = {
    ...params.value,
    projectId: projectInfo.value?.id,
    currIndex: idIndex
  };

  const _params = http.getURLSearchParams(_qery, true);

  const message = `${window.location.origin}/function#cases?&id=${value.id}&name=${value.name}&projectId=${projectInfo.value.id}&${_params}&total=${total.value}`;
  clipboard.toClipboard(message).then(() => {
    notification.success('复制成功');
  }).catch(() => {
    notification.error('复制失败');
  });
};

// 编辑用例
const editCase = ref<CaseListObj>();
const handleEdit = (rowData: CaseListObj) => {
  editCase.value = rowData;
  addVisible.value = true;
};

// 编辑或者添加用例成功更新列表
const addOrEidtSuccess = () => {
  refreshChange();
};

// 表格模式点击名称打开详情tab页 详情包含上一条下一条功能,上一条下一条请求数据使用本tab页数据的search条件
const handleViewInfo = async (rowData: CaseListObj) => {
  const currIndex = caseList.value.findIndex(item => item.id === rowData.id);
  const tabParams = {
    caseId: rowData.id,
    caseName: rowData.name,
    caseAcitonAuth: tableAction.value.auth,
    queryParams: {
      ...params.value,
      projectId: projectInfo.value?.id,
      total: +total.value,
      currIndex: currIndex
    }
  };
  emits('openInfo', tabParams);
};

const calculateDataPosition = (_total, _pageNo, _pageSize, n) => {
  // 计算当前页的起始位置
  const startIndex = (_pageNo - 1) * _pageSize;

  // 计算n在当前页的位置
  const positionInPage = startIndex + n;
  // 返回当前数据在所有数据集里的位置
  return positionInPage;
};

// 导出用例
const exportLoading = ref(false);
const handleExport = async () => {
  if (exportLoading.value) {
    return;
  }

  exportLoading.value = true;
  const _filters = selectedRowKeys.value.length ? [...params.value.filters, { key: 'id', value: selectedRowKeys.value, op: 'IN' }] : params.value.filters;
  const _params = http.getURLSearchParams({ filters: _filters }, true);
  await download(`${TESTER}/func/case/export?projectId=${projectInfo.value.id}&${_params}`);
  exportLoading.value = false;
};

// 导出用例模板
const handdleExportTemplate = async () => {
  const a = document.createElement('a');
  a.style.display = 'none';
  a.href = Template;
  a.download = 'Import_Case_Template.xlsx';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
};

// 打开上传弹窗
const uploadCaseVisible = ref(false);
const handleUploadCase = () => {
  uploadCaseVisible.value = true;
};
// 取消上传用例文件
const cancelUpload = () => {
  uploadCaseVisible.value = false;
};
// 上传成功用例文件
const handleUploadOk = () => {
  loadCaseList();
};

// 收藏
const handleFavourite = async (rowData: CaseListObj) => {
  updateLoading(true);
  const [error] = rowData.favouriteFlag ? await funcCase.cancelFavouriteCase(rowData.id) : await funcCase.favouriteCase(rowData.id);
  updateLoading(false);
  if (error) {
    return;
  }
  notification.success(rowData.favouriteFlag ? '取消收藏成功' : '收藏成功');
  rowData.favouriteFlag = !rowData.favouriteFlag;
  emits('updateFollowFavourite', 'favourite');
};

const handleFollow = async (rowData: CaseListObj) => {
  updateLoading(true);
  const [error] = rowData.followFlag ? await funcCase.cancelFollowCase(rowData.id) : await funcCase.followCase(rowData.id);
  updateLoading(false);
  if (error) {
    return;
  }
  notification.success(rowData.followFlag ? '取消关注成功' : '关注成功');
  rowData.followFlag = !rowData.followFlag;
  emits('updateFollowFavourite', 'follow');
};

// 提Bug
const taskModalVisible = ref(false);
const addBug = (remark = undefined) => {
  if (selectedCase.value) {
    selectedCase.value.testRemark = remark;
  }
  taskModalVisible.value = true;
};

const handleAddTask = async () => {
  loadCaseList();
};

const tableChange = ({ pagination, sorter }) => {
  const { current, pageSize } = pagination;
  params.value.pageNo = current;
  params.value.pageSize = pageSize;
  params.value.orderBy = sorter.orderBy;
  params.value.orderSort = sorter.orderSort;
  actionType.value = 'search';
  loadCaseList();
};

const listChange = ({ current, pageSize }) => {
  params.value.pageNo = current;
  params.value.pageSize = pageSize;
  actionType.value = 'search';
  loadCaseList();
};

const showAnchor = ref(false);

const scrollNotify = ref(0);
const checkScroll = (event) => {
  const container = event.target;
  if (container) {
    if (container.scrollTop === 0) {
      showAnchor.value = false;
      return;
    }
    showAnchor.value = container.scrollHeight > container.clientHeight;

    const isAtBottom = Math.floor(container.scrollHeight - container.scrollTop) === container.clientHeight;
    if (isAtBottom) {
      scrollNotify.value++;
    }
  }
};

const tabViewRef = ref();
const infoViewRef = ref();
const cancelSelectedRowKeys = () => {
  if (props.viewMode === 'table') {
    tabViewRef.value.onSelectChange([]);
  } else {
    infoViewRef.value.cancelCheckAll([]);
  }
};

const buttonDropdownClick = ({ key }: { key: 'import' | 'export' }) => {
  if (key === 'import') {
    handleUploadCase();
    return;
  }

  if (key === 'export') {
    handleExport();
  }
};

// 'table' | 'detail' | 'kanban'
const modeOptions = [
  {
    key: 'tile',
    name: '平铺视图',
    label: ''
  },
  {
    key: 'table',
    name: '列表视图',
    label: ''
  },
  {
    key: 'kanban',
    name: '看板视图',
    label: ''
  }
];

const viewModeChange = (key) => {
  const mode = key;
  emits('viewModeChange', mode);
};

const toRefresh = () => {
  params.value.pageNo = 1;
  setParamsFilters();
  refreshChange();
};

watch(() => props.viewMode, (newValue) => {
  selectedRowKeys.value = [];

  if (newValue !== 'kanban') {
    loadCaseList();
  }

  if (newValue === 'tile' && caseList.value?.length) {
    if (!caseInfo.value && checkedCase.value) {
      getCaseInfo(checkedCase.value.id);
    }
  }
});

const detailQueryParams = computed(() => {
  return {
    ...params.value,
    projectId: projectInfo.value?.id,
    total: +total.value
  };
});

const detaiIndex = computed(() => {
  const currIndex = caseList.value.findIndex(item => item.id === caseInfo.value?.id) || 0;
  const currIndexInAllData = calculateDataPosition(+total.value, params.value.pageNo, params.value.pageSize, currIndex + 1);
  return currIndexInAllData;
});

const qulickList = [
  {
    type: 'all',
    name: '所有',
    selected: false,
    group: 'all'
  },
  {
    type: 'createdBy',
    name: '我添加的',
    selected: false,
    group: 'createdBy'
  },
  {
    type: 'testerId',
    name: '待我测试',
    selected: false,
    group: 'testerId'
  },
  {
    type: 'lastDay',
    name: '近1天',
    selected: false,
    group: 'time'
  },
  {
    type: 'lastThreeDays',
    name: '近3天',
    selected: false,
    group: 'time'
  },
  {
    type: 'lastWeek',
    name: '近7天',
    selected: false,
    group: 'time'
  }
];

const searchOptions = computed(() => [
  {
    placeholder: '查询用例编号、名称',
    valueKey: 'name',
    type: 'input',
    allowClear: true
  },
  // !enabledGroup.value && {
  //   valueKey: 'moduleId',
  //   placeholder: '选择或查询所属模块',
  //   type: 'tree-select',
  //   action: `${TESTER}/module/search?projectId=${projectInfo.value.id}`,
  //   showSearch: true,
  //   allowClear: true,
  //   fieldNames: { value: 'id', label: 'name' }
  // },
  {
    placeholder: '选择添加人',
    valueKey: 'createdBy',
    type: 'select-user',
    allowClear: true
  },
  {
    placeholder: '选择测试人',
    valueKey: 'testerId',
    type: 'select-user',
    allowClear: true
  },
  {
    placeholder: '选择开发人',
    valueKey: 'developerId',
    type: 'select-user',
    allowClear: true
  },
  {
    placeholder: '选择优先级',
    valueKey: 'priority',
    type: 'select-enum',
    enumKey: 'Priority',
    allowClear: true
  },
  {
    placeholder: '选择测试结果',
    valueKey: 'testResult',
    type: 'select-enum',
    enumKey: 'CaseTestResult',
    allowClear: true
  },
  {
    placeholder: '选择评审人',
    valueKey: 'reviewerId',
    type: 'select-user',
    allowClear: true
  },
  {
    placeholder: '选择评审状态',
    valueKey: 'reviewStatus',
    type: 'select-enum',
    enumKey: 'ReviewStatus',
    allowClear: true
  },
  {
    placeholder: ['评审时间从', '评审时间到'],
    valueKey: 'reviewDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    placeholder: ['更新时间从', '更新时间到'],
    valueKey: 'lastModifiedDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    placeholder: ['完成时间从', '完成时间到'],
    valueKey: 'deadlineDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    placeholder: ['添加时间从', '添加时间到'],
    valueKey: 'createdDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    placeholder: ['测试时间从', '测试时间到'],
    valueKey: 'testResultHandleDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    valueKey: 'testNum'
  },
  {
    valueKey: 'testFailNum'
  },
  {
    valueKey: 'reviewNum'
  }
].filter(Boolean));

const buttonDropdownMenuItems = computed(() => [
  {
    name: '导出用例',
    key: 'export',
    icon: 'icon-daochu1',
    noAuth: !caseList.value.length
  },
  {
    name: '导入用例',
    key: 'import',
    icon: 'icon-shangchuan'
  }
]);

const modeTitle = computed(() => {
  if (props.viewMode === 'kanban') {
    return '列表视图';
  }

  if (props.viewMode === 'tile') {
    return '看板视图';
  }

  return '平铺视图';
});

const modeIcon = computed(() => {
  if (props.viewMode === 'kanban') {
    return 'icon-liebiaoshitu';
  }

  if (props.viewMode === 'tile') {
    return 'icon-kanbanshitu';
  }

  return 'icon-pingpushitu';
});

const groupMenuItems = [
  {
    key: 'none',
    name: '不分组'
  },
  {
    key: 'testerName',
    name: '按测试人分组'
  },
  {
    key: 'lastModifiedByName',
    name: '按最后修改人分组'
  }
];

const sortMenuItems = [
  {
    key: 'createdByName',
    name: '按添加人排序',
    orderSort: 'ASC'
  },
  {
    key: 'testerName',
    name: '按测试人排序',
    orderSort: 'ASC'
  },
  {
    key: 'priority',
    name: '按优先级排序',
    orderSort: 'ASC'
  },
  {
    key: 'deadlineDate',
    name: '按截止时间排序',
    orderSort: 'ASC'
  }];

const resetParam = () => {
  searchPanelRef.value.clear(false);
  params.value.filters = [];
  init();
};

defineExpose({
  selectedRowKeys,
  loadData,
  getActionAuth,
  loadCaseCount,
  resetParam
});
</script>
<template>
  <div class="h-full text-3">
    <div class="flex h-full">
      <div class="h-full overflow-hidden bg-gray-1" :class="{'w-70 mr-2': enabledGroup, 'w-0': !enabledGroup}">
        <ModuleTree
          v-model:moduleId="moduleId"
          :projectId="projectInfo.id"
          :projectName="projectInfo?.name"
          :dataList="moduleTreeData"
          @loadData="loadModuleTree" />
      </div>
      <div class="flex-1 flex flex-col overflow-hidden">
        <div class="flex-shrink-0 flex justify-between items-start">
          <div class="flex items-center flex-wrap">
            <QuickSelect
              class="mb-3"
              :list="qulickList"
              :selectedTypes="selectedTypes"
              @change="quickSearchChange" />
            <div class="px-4 h-7 leading-7 mb-3">
              <span>已逾期</span>
              <Colon class="mr-2" />
              <Switch
                :checked="overdueFlag"
                size="small"
                class="w-8"
                @change="overdueFlagChange" />
            </div>
            <div class="h-7 leading-7 mb-3 mr-5">
              <span class="text-3 text-theme-content">
                <span>按模块分组</span>
                <Colon class="mr-2" />
              </span>
              <Switch
                v-model:checked="enabledGroup"
                size="small"
                class="w-8"
                @change="enabledGroupChange" />
            </div>
            <PlanList
              class="mb-3 mr-5"
              :planId="planId"
              :planName="planName"
              @change="planChange" />
            <TagList
              ref="tagListRef"
              class="mb-3 mr-5"
              :tagIds="tagIds"
              @change="tagChange" />
            <template v-if="props.viewMode === 'kanban'">
              <DropdownSort
                v-model:orderBy="boardsOrderBy"
                v-model:orderSort="boardsOrderSort"
                :menuItems="sortMenuItems">
                <Button
                  size="small"
                  type="text"
                  class="flex items-center px-0 h-5 leading-5 border-0 cursor-pointer mr-5 mb-3">
                  <Icon icon="icon-biaotoupaixu" class="text-3.5" />
                  <span class="ml-1">排序</span>
                </Button>
              </DropdownSort>

              <DropdownGroup v-model:activeKey="boardsGroupKey" :menuItems="groupMenuItems">
                <Button
                  size="small"
                  type="text"
                  class="flex items-center px-0 h-5 leading-5 border-0 cursor-pointer mr-5 mb-3">
                  <Icon icon="icon-fenzu" class="text-3.5" />
                  <span class="ml-1">分组</span>
                </Button>
              </DropdownGroup>
            </template>
          </div>
        </div>
        <div class="flex justify-between items-start space-x-5">
          <SearchPanel
            ref="searchPanelRef"
            class="mb-3 flex-shrink-0 flex-1"
            :options="searchOptions"
            :width="296"
            @change="searchChange">
            <template #planId_option="item">
              <div class="flex items-center" :title="item.name">
                <Icon icon="icon-jihua" class="mr-1 text-3.5 flex-none" />
                <div class="truncate">{{ item.name }}</div>
              </div>
            </template>
            <template #moduleId_option="item">
              <div class="flex items-center" :title="item.name">
                <Icon icon="icon-mokuai" class="mr-1 text-3.5 flex-none" />
                <div class="truncate">{{ item.name }}</div>
              </div>
            </template>
            <template #priority_option="item">
              <TaskPriority :value="item" />
            </template>
            <template #testResult_option="item">
              <TestResult :value="item" />
            </template>
            <template #reviewStatus_option="item">
              <ReviewStatus :value="item" />
            </template>
            <template #testNum>
              <Input
                :value="testNum"
                data-type="float"
                size="small"
                allowClear
                placeholder="测试次数"
                style="width: 296px;"
                :min="0"
                :debounce="500"
                @change="(event: ChangeEvent) => handleTimesChange(event.target.value, 'testNum')">
                <template #prefix>
                  <Select
                    :value="testNumScope"
                    size="small"
                    :options="NumberMatchCondition"
                    :fieldNames="{ label: 'message', value: 'value' }"
                    :allowClear="false"
                    :bordered="false"
                    class="w-24"
                    @change="handleScopeChange($event, 'testNum')" />
                </template>
              </Input>
            </template>
            <template #testFailNum>
              <Input
                :value="testFailNum"
                data-type="float"
                size="small"
                allowClear
                placeholder="失败次数"
                style="width: 296px;"
                :min="0"
                :debounce="500"
                @change="handleTimesChange($event.target.value, 'testFailNum')">
                <template #prefix>
                  <Select
                    v-model:value="testFailScope"
                    size="small"
                    :options="NumberMatchCondition"
                    :fieldNames="{ label: 'message', value: 'value' }"
                    :allowClear="false"
                    :bordered="false"
                    class="w-24"
                    @change="handleScopeChange($event, 'testFailNum')" />
                </template>
              </Input>
            </template>
            <template #reviewNum>
              <Input
                :value="reviewNum"
                data-type="float"
                size="small"
                allowClear
                placeholder="评审次数"
                style="width: 296px;"
                :min="0"
                :debounce="500"
                @change="(event: ChangeEvent) => handleTimesChange(event.target.value, 'reviewNum')">
                <template #prefix>
                  <Select
                    :value="reviewNumScope"
                    size="small"
                    :options="NumberMatchCondition"
                    :fieldNames="{ label: 'message', value: 'value' }"
                    :allowClear="false"
                    :bordered="false"
                    class="w-24"
                    @change="handleScopeChange($event, 'reviewNum')" />
                </template>
              </Input>
            </template>
          </SearchPanel>
          <div class="flex items-center space-x-2">
            <Button
              v-if="aiEnabled"
              class="flex-shrink-0 flex items-center"
              size="small"
              ghost
              type="primary"
              @click="handleAiAdd">
              <Icon icon="icon-jia" class="text-3.5" />
              <span class="ml-1">智能添加用例</span>
            </Button>
            <Button
              class="flex-shrink-0 flex items-center pr-0"
              type="primary"
              size="small"
              @click="handleAdd">
              <div class="flex items-center">
                <Icon icon="icon-jia" class="text-3.5" />
                <span class="ml-1">添加用例</span>
              </div>
              <Dropdown :menuItems="buttonDropdownMenuItems" @click="buttonDropdownClick">
                <div class="w-5 h-5 flex items-center justify-center">
                  <Icon icon="icon-more" />
                </div>
              </Dropdown>
            </Button>
            <Tooltip
              arrowPointAtCenter
              placement="topLeft"
              :title="modeTitle">
              <Dropdown
                :value="[props.viewMode]"
                :menuItems="modeOptions"
                @click="viewModeChange($event.key)">
                <div>
                  <Icon
                    :icon="modeIcon"
                    class="text-4 cursor-pointer text-theme-content text-theme-text-hover flex-shrink-0" />
                  <Icon icon="icon-xiajiantou" class="ml-1" />
                </div>
              </Dropdown>
              <!-- <Icon
                :icon="modeIcon"
                class="text-4 cursor-pointer text-theme-content text-theme-text-hover flex-shrink-0"
                @click="viewModeChange" /> -->
            </Tooltip>
            <Tooltip
              arrowPointAtCenter
              placement="topLeft"
              :title="isOpenCount ? '收起统计' : '查看统计'">
              <IconCount
                :value="isOpenCount"
                class="mr-2 text-4.5"
                @click="emits('countChange')" />
            </Tooltip>
            <Tooltip
              arrowPointAtCenter
              placement="topLeft"
              title="刷新">
              <IconRefresh class="text-4 mr-3.5" @click="toRefresh" />
            </Tooltip>
          </div>
        </div>
        <div
          :visible="!!selectedRowKeys?.length"
          class="btn-group-container flex-shrink-0 flex items-center transition-all duration-300 space-x-2.5">
          <!-- <Button
              :disabled="batchDisabled.review"
              class="flex items-center px-0 h-5 leading-5"
              type="link"
              size="small"
              @click="handleAction('review')">
              评审
            </Button> -->
          <Button
            :disabled="batchDisabled.move"
            class="flex items-center px-0 h-5 leading-5"
            type="link"
            size="small"
            @click="handleAction('move')">
            移动
          </Button>
          <Button
            :disabled="batchDisabled.del"
            class="flex items-center px-0 h-5 leading-5"
            type="link"
            size="small"
            @click="handleDelete()">
            删除
          </Button>
          <Button
            :disabled="batchDisabled.updateTestResult"
            class="flex items-center px-0 h-5 leading-5"
            type="link"
            size="small"
            @click="handleAction('updateTestResult_passed')">
            测试通过
          </Button>
          <Button
            :disabled="batchDisabled.updateTestResult"
            class="flex items-center px-0 h-5 leading-5"
            type="link"
            size="small"
            @click="handleAction('updateTestResult_notpassed')">
            测试未通过
          </Button>
          <Button
            danger
            type="link"
            size="small"
            class="flex items-center px-0 h-5 leading-5"
            @click="cancelSelectedRowKeys">
            <span>取消批量操作</span>
            <span class="ml-1">({{ selectedRowKeys?.length }})</span>
          </Button>
        </div>
        <template v-if="props.viewMode === 'kanban'">
          <KanbanView
            v-show="viewMode === 'kanban'"
            v-model:moduleId="moduleId"
            :groupKey="boardsGroupKey"
            :orderBy="boardsOrderBy"
            :orderSort="boardsOrderSort"
            :filters="params?.filters"
            :projectId="projectInfo?.id"
            :userInfo="userInfo"
            :appInfo="appInfo"
            class="flex-1"
            @refreshChange="refreshChange"
            @loadingChange="loadingChange" />
        </template>
        <template v-else-if="caseList.length">
          <template v-if="props.viewMode === 'table'">
            <TableView
              ref="tabViewRef"
              v-model:selectedRowKeys="selectedRowKeys"
              :params="params"
              :total="total"
              :caseAcitonAuth="tableAction.auth"
              :menus="tableAction.menus"
              :caseList="caseList"
              class="flex-1 pb-3.5"
              @onClick="handleDetailAction"
              @openInfo="handleViewInfo"
              @change="tableChange" />
          </template>
          <template v-else-if="props.viewMode === 'tile'">
            <InfoView
              ref="infoViewRef"
              v-model:selectedRowKeys="selectedRowKeys"
              :params="params"
              :total="total"
              :caseList="caseList"
              :checkedCase="checkedCase"
              class="flex-1"
              @select="hanldeSelectCase"
              @change="listChange">
              <template #info>
                <CaseDetail
                  v-if="checkedCase"
                  id="CaseInfoRef"
                  class="h-full flex-1"
                  :caseId="caseInfo?.id"
                  :caseAcitonAuth="tableAction.auth"
                  :caseInfo="caseInfo"
                  :userInfo="userInfo"
                  :appInfo="appInfo"
                  :scrollNotify="scrollNotify"
                  :queryParams="detailQueryParams"
                  :currIndex="detaiIndex"
                  type="info"
                  @onClick="handleDetailAction"
                  @getInfo="getCaseInfo"
                  @editSuccess="editSuccess"
                  @scroll="checkScroll" />
              </template>
            </InfoView>
          </template>
        </template>
        <template v-if="!props.loading && !caseList?.length && !firstloading">
          <NoData class="mx-auto flex-1 flex-shrink-0" size="small" />
        </template>
      </div>
    </div>
  </div>
  <AsyncComponent :visible="addVisible">
    <AddModal
      v-model:visible="addVisible"
      :editCase="editCase"
      :moduleId="(moduleId && moduleId !== '-1') ? moduleId : undefined"
      @update="addOrEidtSuccess" />
  </AsyncComponent>
  <AsyncComponent :visible="aiAddVisible">
    <AiAddModal
      v-model:visible="aiAddVisible"
      @update="addOrEidtSuccess" />
  </AsyncComponent>
  <AsyncComponent :visible="reviewVisible">
    <ReviewModal
      v-if="reviewVisible"
      v-model:visible="reviewVisible"
      :selectedCase="selectedCase"
      :selectedRowKeys="selectedRowKeys"
      :type="btnType"
      @update="updteSuccess" />
  </AsyncComponent>
  <AsyncComponent :visible="moveVisible">
    <MoveModal
      v-if="moveVisible"
      v-model:visible="moveVisible"
      :selectedCase="selectedCase"
      :selectedRowKeys="selectedRowKeys"
      :type="btnType"
      @update="moveSuccess" />
  </AsyncComponent>
  <AsyncComponent :visible="updateTestResultVisible">
    <UpdateTestResultModal
      v-if="updateTestResultVisible"
      v-model:visible="updateTestResultVisible"
      :resultPassed="resultPassed"
      :selectedCase="selectedCase"
      :selectedRowKeys="selectedRowKeys"
      :type="btnType"
      @update="updteSuccess"
      @addBug="addBug" />
  </AsyncComponent>
  <AsyncComponent :visible="uploadCaseVisible">
    <UploadCaseModal
      v-model:visible="uploadCaseVisible"
      :downloadTemplate="handdleExportTemplate"
      @cancel="cancelUpload"
      @ok="handleUploadOk" />
  </AsyncComponent>
  <AsyncComponent :visible="taskModalVisible">
    <AddTaskModal
      v-model:visible="taskModalVisible"
      :projectId="projectInfo?.id"
      :appInfo="appInfo"
      :assigneeId="selectedCase?.developerId"
      :userInfo="userInfo"
      :refCaseIds="[selectedCase?.id]"
      :description="selectedCase?.testRemark"
      :name="`“${selectedCase?.name || ''}” 测试不通过`"
      taskType="BUG"
      :confirmorId="selectedCase?.testerId"
      @ok="handleAddTask" />
  </AsyncComponent>
</template>
<style scoped>
.btn-group-container {
  height: 0;
  overflow: hidden;
  opacity: 0;
}

.btn-group-container[visible="true"] {
  height: 28px;
  opacity: 1;
}
</style>
