<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onMounted, Ref, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { AsyncComponent, modal, NoData, notification } from '@xcan-angus/vue-ui';
import {
  appContext, download, enumUtils, http, PageQuery, SearchCriteria, TESTER, toClipboard, ReviewStatus
} from '@xcan-angus/infra';
import { analysis, testCase, testPlan, modules } from '@/api/tester';
import { travelTreeData } from '@/utils/utils';
import { ProjectInfo } from '@/layout/types';

import { CaseActionAuth, EnabledModuleGroup } from './types';
import { CaseTestResult, FuncPlanPermission, TaskType } from '@/enums/enums';
import { CaseCount, CaseViewMode, getActionAuth } from '@/views/test/case/types';
import { CaseDetail } from '@/views/test/types';

// eslint-disable-next-line import/no-absolute-path
import Template from '/file/Import_Case_Template.xlsx?url';

// Async component imports
const FlatView = defineAsyncComponent(() => import('@/views/test/case/list/flat/index.vue'));
const TableView = defineAsyncComponent(() => import('@/views/test/case/list/table/index.vue'));
const KanbanView = defineAsyncComponent(() => import('@/views/test/case/list/kanban/index.vue'));

const ModuleTree = defineAsyncComponent(() => import('@/components/ModuleSelectTree/index.vue'));
const CaseDetailPage = defineAsyncComponent(() => import('@/views/test/case/detail/index.vue'));
const AddModal = defineAsyncComponent(() => import('@/views/test/case/list/Edit.vue'));
const AiAddModal = defineAsyncComponent(() => import('@/views/test/case/list/AiAdd.vue'));
const ReviewModal = defineAsyncComponent(() => import('@/views/test/case/list/Review.vue'));
const MoveModal = defineAsyncComponent(() => import('@/views/test/case/list/Move.vue'));
const UpdateTestResultModal = defineAsyncComponent(() => import('@/views/test/case/list/UpdateResult.vue'));
const UploadCaseModal = defineAsyncComponent(() => import('@/views/test/case/list/Upload.vue'));
const EditTaskModal = defineAsyncComponent(() => import('@/views/issue/issue/list/Edit.vue'));
const SearchPanel = defineAsyncComponent(() => import('@/views/test/case/list/SearchPanel.vue'));

const { t } = useI18n();

// Component props interface
interface Props {
  loading: boolean;
  viewMode: CaseViewMode;
  isOpenCount: boolean;
  count?: CaseCount;
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

// Component emits
// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'update:count', value: CaseCount): void;
  (e: 'openInfo', infoTabParams: any): void;
  (e: 'updateFollowFavourite', type: 'addFollow' | 'addFavourite'): void;
  (e: 'cacheParams', value: any): void;
  (e: 'viewModeChange', value: CaseViewMode): void;
  (e: 'countChange'): void;
}>();

// Global Injects & Basic State
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));
const userInfo = inject<{ id: number, fullName: string }>('userInfo');
const appInfo = inject<{ id: number, name: string }>('appInfo');
const updateLoading = inject<((value: boolean) => void)>('updateLoading', () => undefined);
const isAdmin = computed(() => appContext.isAdmin());

// Loading Flags & Action Types
const firstLoadInfo = ref(true);
const firstLoading = ref(true);
const actionType = ref<'search' | 'del' | undefined>(undefined);
const searchPanelRef = ref();

// Query & Pagination State
const params = ref({
  pageNo: 1,
  pageSize: 10,
  filters: [] as SearchCriteria[],
  orderBy: undefined as 'priority' | 'deadlineDate' | 'createdByName' | 'testerName' | undefined,
  orderSort: undefined as PageQuery.OrderSort | undefined
});
const total = ref(0);

/**
 * Handle loading state change
 * @param value - Loading state
 */
const loadingChange = (value: boolean) => {
  updateLoading(value);
};

/**
 * Refresh data and count
 */
const refreshChange = () => {
  if (props.isOpenCount) {
    loadCaseCount();
  }
  loadData();
};

/**
 * Set parameters and load data
 */
const setParamsAndLoadData = () => {
  params.value.pageNo = 1;
  actionType.value = 'search';
  refreshChange();
};

// Module Grouping
const enabledModuleGroup = ref<EnabledModuleGroup>(true);
const moduleId = ref<number | undefined>();

/**
 * Handle module grouping change
 * @param value - Grouping enabled state
 */
const enabledModuleGroupChange = (value: boolean) => {
  enabledModuleGroup.value = value;
  if (enabledModuleGroup.value) {
    moduleId.value = -1;
  } else {
    moduleId.value = undefined;
  }
};

/**
 * Handle search criteria change
 * @param filters - Search criteria array
 */
const handleSearchChange = (filters: SearchCriteria[]) => {
  params.value.pageNo = 1;
  params.value.filters = filters;
  actionType.value = 'search';
  refreshChange();
};

/**
 * Handle view mode change
 * @param viewMode - New view mode
 */
const handleViewModeChange = (viewMode: CaseViewMode) => {
  emits('viewModeChange', viewMode);
};

/**
 * Handle count panel toggle
 */
const handleCountChange = () => {
  emits('countChange');
};

/**
 * Handle refresh action
 */
const handleRefresh = () => {
  params.value.pageNo = 1;
  refreshChange();
};

/**
 * Handle add case action
 */
const handleAdd = () => {
  editCase.value = undefined;
  addVisible.value = true;
};

/**
 * Handle AI add case action
 */
const handleAiAdd = () => {
  aiAddVisible.value = true;
};

/**
 * Handle case export
 */
const handleExport = async () => {
  if (exportLoading.value) {
    return;
  }

  exportLoading.value = true;
  const _filters = selectedRowKeys.value.length
    ? [...params.value.filters, { key: 'id', value: selectedRowKeys.value, op: SearchCriteria.OpEnum.In }]
    : params.value.filters;
  const _params = http.getURLSearchParams({ filters: _filters }, true);
  await download(`${TESTER}/func/case/export?projectId=${projectInfo.value.id}&${_params}`);
  exportLoading.value = false;
};

/**
 * Handle case import
 */
const handleImport = () => {
  uploadCaseVisible.value = true;
};

let isInit = true;

/**
 * Load data based on view mode
 */
const loadData = () => {
  if (props.viewMode !== CaseViewMode.kanban) {
    loadCaseList();
  }
};

/**
 * Load case count statistics
 */
const loadCaseCount = async (): Promise<void> => {
  const [error, { data }] = await analysis.getFuncCaseCount(
    { ...params.value, projectId: projectInfo.value?.id, moduleId: moduleId.value }
  );
  if (error) {
    return;
  }
  emits('update:count', data);
};

// Case List & Table Actions
const checkedCase = ref<CaseDetail>();
const caseList = ref<CaseDetail[]>([]);

/**
 * Load case list data
 */
const loadCaseList = async (): Promise<void> => {
  if (props.loading) {
    return;
  }

  updateLoading(true);
  const [error, { data = { list: [], total: 0 } }] = await testCase.getCaseList(
    { infoScope: PageQuery.InfoScope.DETAIL, ...params.value, projectId: projectInfo.value?.id, moduleId: moduleId.value }
  );
  firstLoading.value = false;
  if (error) {
    updateLoading(false);
    return;
  }

  if (props.viewMode === CaseViewMode.table && tabViewRef.value) {
    tabViewRef.value.selectedRowKeys = [];
  }

  selectedRowKeys.value = [];
  caseList.value = data.list.map(item => ({ ...item, checked: false }));
  await getPlanAuth();

  total.value = data.total;
  if (!data.list.length) {
    checkedCase.value = undefined;
    updateLoading(false);
    return;
  }

  if (firstLoadInfo.value || actionType.value === 'del') {
    checkedCase.value = data.list[0];
    firstLoadInfo.value = false;
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
  if (props.viewMode === CaseViewMode.flat) {
    if (!checkedCase.value) {
      checkedCase.value = data.list[0];
    }
    if (checkedCase.value?.id) {
      await getCaseInfo(checkedCase.value.id);
    }
  }
  updateLoading(false);
};

const tableAction = computed(() => {
  const action = { auth: {}, actionMenus: {} };
  for (let i = 0; i < caseList.value.length; i++) {
    const _case = caseList.value[i];
    if (userInfo?.id === _case.testerId &&
      (planAuthMap.value[_case.planId]?.permissions || []).includes(FuncPlanPermission.TEST)) {
      if (planAuthMap.value[_case.planId]?.permissions) {
        // eslint-disable-next-line vue/no-side-effects-in-computed-properties
        planAuthMap.value[_case.planId].permissions.push(FuncPlanPermission.TEST);
      }
    }

    action.auth[_case.id] = getActionAuth((planAuthMap.value[_case.planId]?.permissions || []));
    if (action.auth[_case.id].includes('resetTestResult')) {
      if (!planAuthMap.value[_case.planId].funcPlanAuth) {
        action.auth[_case.id] = action.auth[_case.id].filter(i => i !== 'resetTestResult');
      }
    }
    if (action.auth[_case.id].includes('retestResult')) {
      if (!planAuthMap.value[_case.planId].funcPlanAuth && userInfo?.id !== _case.testerId) {
        action.auth[_case.id] = action.auth[_case.id].filter(i => i !== 'retestResult');
      }
    }
    action.actionMenus[_case.id] = [];

    // Allow resetting test result only when test count > 0
    if (+_case.testNum > 0) {
      action.actionMenus[_case.id].push({
        key: 'resetTestResult',
        icon: 'icon-zhongzhiceshijieguo',
        name: t('testCase.actions.resetTestResult'),
        permission: 'resetTestResult'
      });
    }

    if (!_case?.review || (_case?.review && _case?.reviewStatus.value === ReviewStatus.PASSED)) {
      action.actionMenus[_case.id].push({
        key: 'updateTestResult_canceled',
        icon: 'icon-xiugaiceshijieguo',
        name: t('actions.cancel'),
        permission: 'edit'
      });

      if (![CaseTestResult.PASSED, CaseTestResult.NOT_PASSED, CaseTestResult.CANCELED].includes(_case?.testResult?.value)) {
        action.actionMenus[_case.id].push({
          key: 'updateTestResult_passed',
          icon: 'icon-xiugaiceshijieguo',
          name: t('testCase.actions.testPassed'),
          permission: 'updateTestResult'
        }, {
          key: 'updateTestResult_notPassed',
          icon: 'icon-xiugaiceshijieguo',
          name: t('testCase.actions.testNotPassed'),
          permission: 'updateTestResult'
        }, {
          key: 'updateTestResult_blocked',
          icon: 'icon-xiugaiceshijieguo',
          name: t('testCase.actions.setBlocked'),
          permission: 'updateTestResult'
        });
      } else {
        action.actionMenus[_case.id].push({
          key: 'retestResult',
          icon: 'icon-xiugaiceshijieguo',
          name: t('testCase.actions.retest'),
          permission: 'retestResult'
        });
      }
    }

    if (action.auth[_case.id].includes('move')) {
      action.actionMenus[_case.id].push({
        key: 'move',
        icon: 'icon-yidong',
        name: t('actions.move'),
        permission: 'move'
      });
    }

    if (_case?.review && _case?.reviewStatus.value === ReviewStatus.PENDING) {
      action.actionMenus[_case.id].push({
        key: 'review',
        icon: 'icon-pingshen',
        name: t('common.review'),
        noAuth: true
      });
    }

    action.actionMenus[_case.id].push(
      {
        key: 'clone',
        icon: 'icon-fuzhi',
        name: t('actions.clone'),
        permission: 'clone'
      },
      {
        key: 'addFavourite',
        icon: _case.favourite ? 'icon-quxiaoshoucang' : 'icon-yishoucang',
        name: _case.favourite ? t('actions.cancelFavourite') : t('actions.addFavourite'),
        permission: 'edit',
        noAuth: true
      },
      {
        key: 'addFollow',
        icon: _case.follow ? 'icon-quxiaoguanzhu' : 'icon-yiguanzhu',
        name: _case.follow ? t('actions.cancelFollow') : t('actions.addFollow'),
        permission: 'edit',
        noAuth: true
      },
      {
        key: 'copyUrl',
        icon: 'icon-fuzhi',
        name: t('actions.copyLink'),
        noAuth: true
      }
    );
  }
  return action;
});

// Case detail state
const caseInfo = ref<CaseDetail>();
const firstCase = ref<CaseDetail>();
const getCaseInfo = async (id: number) => {
  updateLoading(true);
  const [error, { data }] = await testCase.getCaseDetail(id);
  updateLoading(false);
  if (error) {
    return;
  }
  caseInfo.value = data;
  checkedCase.value = data;
  firstCase.value = data;
};

const addVisible = ref(false);

const aiAddVisible = ref(false);

const planAuthMap = ref<{[id: number]: {
  funcPlanAuth: boolean;
  permissions: string[];
}}>({});

/**
 * Get current user's permissions for each plan within the current case list
 */
const getPlanAuth = async () => {
  if (isAdmin.value) {
    caseList.value.forEach(i => {
      planAuthMap.value[i.planId] = {
        funcPlanAuth: true,
        permissions: enumUtils.getEnumValues(FuncPlanPermission)
      };
    });
    return;
  }
  const planIds = caseList.value.map(i => i.planId).filter(Boolean);
  if (!planIds.length) {
    return;
  }
  const [error, { data }] = await testPlan.getCurrentAuth({
    ids: planIds,
    admin: true
  });
  if (error) {
    return;
  }

  planIds.map(planId => {
    // eslint-disable-next-line no-return-assign
    return planAuthMap.value[planId] = {
      ...data[planId],
      permissions: (data[planId].permissions || []).map(i => i.value)
    };
  });
};

// Module Tree
const moduleTreeData = ref([{ name: t('common.noModule'), id: '-1' }]);
const loadModuleTree = async (keywords?: string) => {
  const [error, { data }] = await modules.getModuleTree({
    projectId: projectInfo.value?.id,
    filters: keywords
      ? [{ value: keywords, op: SearchCriteria.OpEnum.Match, key: 'name' }]
      : []
  });
  if (error) {
    return;
  }
  moduleTreeData.value = [{ name: t('common.noModule'), id: '-1' }, ...travelTreeData(data || [])];
  if (moduleId.value && keywords && !moduleTreeData.value.find(item => item.id === String(moduleId.value))) {
    moduleId.value = undefined;
  }
};

// Selected case IDs
const selectedRowKeys = ref<number[]>([]);

// Operation type: 'batch' for bulk, 'one' for single row action
const btnType = ref<'batch' | 'one'>('one');
const reviewVisible = ref(false);
const selectedCase = ref<CaseDetail>();
const moveVisible = ref(false);
const updateTestResultVisible = ref(false);
const resultPassed = ref(false);

const batchDisabled = ref({
  review: false,
  move: false,
  del: false,
  updateTestResult: false
});

// Move success
const moveSuccess = () => {
  refreshChange();
};

// Update success
const updateSuccess = () => {
  refreshChange();
};

const editSuccess = () => {
  refreshChange();
};

const handleClone = async (rowData: CaseDetail) => {
  updateLoading(true);
  const [error] = await testCase.cloneCase([rowData.id]);
  if (error) {
    updateLoading(false);
    return;
  }
  notification.success(t('actions.tips.cloneSuccess'));
  refreshChange();
};

// eslint-disable-next-line @typescript-eslint/no-unused-vars, @typescript-eslint/no-empty-function
const refreshRecycleBin = inject('refreshRecycleBin', (_key: 'useCase') => { });

// Delete case(s)
const handleDelete = async (rowData?: CaseDetail) => {
  modal.confirm({
    centered: true,
    title: t('actions.delete'),
    content: rowData
      ? t('actions.tips.confirmDelete', { name: rowData.name })
      : t('actions.tips.confirmDataDelete'),
    async onOk () {
      await delCase(rowData);
    }
  });
};

const delCase = async (rowData?: CaseDetail) => {
  updateLoading(true);
  const ids: number[] = rowData ? [rowData.id] : selectedRowKeys.value;
  const [error] = await testCase.deleteCase(ids);
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
  notification.success(t('actions.tips.deleteSuccess'));
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

// Reset test
const handleResetTestResults = async (rowData: CaseDetail) => {
  updateLoading(true);
  const [error] = await testCase.resetCaseResult([rowData.id]);
  if (error) {
    updateLoading(false);
    return;
  }
  notification.success(t('testCase.messages.resetTestResultSuccess'));
  refreshChange();
};

// Reset review
const handleResetReviewResult = async (rowData: CaseDetail) => {
  updateLoading(true);
  const [error] = await testCase.resetReviewCase([rowData.id]);
  if (error) {
    updateLoading(false);
    return;
  }
  notification.success(t('testCase.messages.resetReviewResultSuccess'));
  refreshChange();
};

// Re-test: set case to To Be Tested
const handleReTest = async (rowData: CaseDetail) => {
  updateLoading(true);
  const [error] = await testCase.retestResult([rowData.id]);
  if (error) {
    updateLoading(false);
    return;
  }
  notification.success(t('testCase.messages.resetTestStatusSuccess'));
  refreshChange();
};

// Flat view: click item to load detail
const handleSelectCase = async (_case: CaseDetail) => {
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

// Detail action buttons
const handleDetailAction = (type: CaseActionAuth, value: CaseDetail) => {
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
    case 'updateTestResult_notPassed':
      handleAction('updateTestResult_notPassed', value);
      break;
    case 'updateTestResult_blocked':
      handleSetResultBlocked(value);
      break;
    case 'updateTestResult_canceled':
      handleSetResultCanceled(value);
      break;
    case 'resetTestResult':
      handleResetTestResults(value);
      break;
    case 'retestResult':
      handleReTest(value);
      break;
    case 'resetReviewResult':
      handleResetReviewResult(value);
      break;
    case 'addFavourite':
      handleFavourite(value);
      break;
    case 'addFollow':
      handleFollow(value);
      break;
    case 'copyUrl':
      handleCopy(value);
      break;
  }
};

// Encapsulated actions: review / move / update test result
const handleAction = (
  action: 'review' | 'move' | 'updateTestResult_passed' | 'updateTestResult_notPassed',
  rowData?: CaseDetail
) => {
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
    case 'updateTestResult_notPassed':
      updateTestResultVisible.value = true;
      resultPassed.value = false;
      break;
  }
};

// Set result to BLOCKED
const handleSetResultBlocked = async (value) => {
  const params = [
    {
      id: value.id,
      testResult: CaseTestResult.BLOCKED
    }
  ];
  const [error] = await testCase.updateCaseResult(params);
  if (error) {
    return;
  }
  notification.success(t('testCase.messages.setBlockedSuccess'));
  refreshChange();
};

// Set result to CANCELED
const handleSetResultCanceled = async (value) => {
  const params = [
    { id: value.id, testResult: CaseTestResult.CANCELED }
  ];
  const [error] = await testCase.updateCaseResult(params);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.cancelSuccess'));
  refreshChange();
};

const handleCopy = async (value: CaseDetail) => {
  const idIndex = caseList.value?.findIndex(item => item.id === value.id);
  const _query = {
    ...params.value,
    projectId: projectInfo.value?.id,
    currIndex: idIndex
  };

  const _params = http.getURLSearchParams(_query, true);

  const message = `${window.location.origin}/test#cases?id=${value.id}&name=${value.name}&projectId=${projectInfo.value.id}&${_params}&total=${total.value}`;
  toClipboard(message).then(() => {
    notification.success(t('actions.tips.copySuccess'));
  }).catch(() => {
    notification.error(t('actions.tips.copyFailed'));
  });
};

// Edit case
const editCase = ref<CaseDetail>();
const handleEdit = (rowData: CaseDetail) => {
  editCase.value = rowData;
  addVisible.value = true;
};

// Refresh list after add or edit
const addOrEditSuccess = () => {
  refreshChange();
};

// Table mode: clicking name opens detail tab with prev/next using current search criteria
const handleViewInfo = async (rowData: CaseDetail) => {
  const currIndex = caseList.value.findIndex(item => item.id === rowData.id);
  const tabParams = {
    caseId: rowData.id,
    caseName: rowData.name,
    caseActionAuth: tableAction.value.auth,
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
  // Compute the start index of the current page
  const startIndex = (_pageNo - 1) * _pageSize;

  // Compute the position of n within the current page
  return startIndex + n;
};

// Export cases
const exportLoading = ref(false);

// Export import template
const handleExportTemplate = async () => {
  const a = document.createElement('a');
  a.style.display = 'none';
  a.href = Template;
  a.download = 'Import_Case_Template.xlsx';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
};

// Open upload modal
const uploadCaseVisible = ref(false);

// Cancel uploading case file
const cancelUpload = () => {
  uploadCaseVisible.value = false;
};
// Reload list after successful upload
const handleUploadOk = () => {
  loadCaseList();
};

// Favourite toggle
const handleFavourite = async (rowData: CaseDetail) => {
  updateLoading(true);
  const [error] = rowData.favourite ? await testCase.cancelFavouriteCase(rowData.id) : await testCase.AddFavouriteCase(rowData.id);
  updateLoading(false);
  if (error) {
    return;
  }
  notification.success(rowData.favourite
    ? t('actions.tips.cancelFavouriteSuccess')
    : t('actions.tips.favouriteSuccess'));
  rowData.favourite = !rowData.favourite;
  emits('updateFollowFavourite', 'addFavourite');
};

const handleFollow = async (rowData: CaseDetail) => {
  updateLoading(true);
  const [error] = rowData.follow ? await testCase.cancelFollowCase(rowData.id) : await testCase.addFollowCase(rowData.id);
  updateLoading(false);
  if (error) {
    return;
  }
  notification.success(rowData.follow
    ? t('actions.tips.cancelFollowSuccess')
    : t('actions.tips.followSuccess'));
  rowData.follow = !rowData.follow;
  emits('updateFollowFavourite', 'addFollow');
};

// Create a Bug task
const taskModalVisible = ref(false);
const addBug = (description?: string) => {
  if (selectedCase.value) {
    selectedCase.value.testRemark = description || '';
  }
  taskModalVisible.value = true;
};

const handleAddTask = async () => {
  await loadCaseList();
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
  if (props.viewMode === CaseViewMode.table) {
    tabViewRef.value.onSelectChange([]);
  } else {
    infoViewRef.value.cancelCheckAll([]);
  }
};

const detailQueryParams = computed(() => {
  return {
    ...params.value,
    projectId: projectInfo.value?.id,
    total: +total.value
  };
});

const detailIndex = computed(() => {
  const currIndex = caseList.value.findIndex(item => item.id === caseInfo.value?.id) || 0;
  return calculateDataPosition(+total.value, params.value.pageNo, params.value.pageSize, currIndex + 1);
});

const resetParam = () => {
  params.value.filters = [];
  searchPanelRef.value?.clear(false);
  refreshChange();
};

watch(() => props.notify, () => {
  checkedCase.value = undefined;
  caseInfo.value = undefined;
  refreshChange();
});

watch(() => props.viewMode, (newValue) => {
  selectedRowKeys.value = [];

  if (newValue !== CaseViewMode.kanban) {
    loadCaseList();
  }

  if (newValue === CaseViewMode.flat && caseList.value?.length) {
    if (!caseInfo.value && checkedCase.value) {
      getCaseInfo(checkedCase.value.id);
    }
  }
});

watch(() => selectedRowKeys.value, (newValue) => {
  btnType.value = 'batch';
  batchDisabled.value = {
    review: false,
    move: false,
    del: false,
    updateTestResult: false
  };
  const list = caseList.value;

  const selectList = list.filter(item => newValue.includes(item.id));
  if (selectList.some(_case => {
    if (planAuthMap.value[_case.planId].funcPlanAuth) {
      return !(planAuthMap.value[_case.planId]?.permissions || []).includes(FuncPlanPermission.MODIFY_CASE);
    }
    return false;
  })) {
    batchDisabled.value.move = true;
  }

  if (selectList.some(_case => {
    if (planAuthMap.value[_case.planId].funcPlanAuth) {
      return !planAuthMap.value[_case.planId]?.permissions?.includes(FuncPlanPermission.DELETE_CASE);
    }
    return false;
  })) {
    batchDisabled.value.del = true;
  }

  if (selectList.some(_case => {
    if ((_case?.review && _case?.reviewStatus.value !== ReviewStatus.PASSED) ||
      [CaseTestResult.PASSED, CaseTestResult.NOT_PASSED, CaseTestResult.CANCELED].includes(_case.testResult.value)) {
      return true;
    }
    if (planAuthMap.value[_case.planId].funcPlanAuth) {
      return !planAuthMap.value[_case.planId]?.permissions?.includes(FuncPlanPermission.TEST);
    }
    return false;
  })) {
    batchDisabled.value.updateTestResult = true;
  }
});

onMounted(async () => {
  await loadModuleTree();
  refreshChange();

  watch(() => projectInfo.value?.id, newValue => {
    if (newValue) {
      moduleId.value = undefined;
      loadModuleTree();
    }
  });
  watch(() => moduleId.value, () => {
    setParamsAndLoadData();
  });
});

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
      <div class="flex-shrink-0 h-full overflow-hidden pb-3 bg-gray-1 text-3" :class="{'w-65 mr-2': enabledModuleGroup , 'w-0': !enabledModuleGroup}">
        <ModuleTree
          :moduleId="moduleId as number"
          :projectId="projectInfo?.id"
          :projectName="projectInfo?.name" />
      </div>

      <div class="flex-1 flex flex-col overflow-hidden">
        <SearchPanel
          ref="searchPanelRef"
          :viewMode="props.viewMode"
          :projectId="projectInfo.id"
          :userInfo="userInfo"
          :appInfo="appInfo"
          :notify="String(props.notify)"
          :enabledGroup="enabledModuleGroup"
          :moduleId="moduleId"
          @change="handleSearchChange"
          @viewModeChange="handleViewModeChange"
          @countChange="handleCountChange"
          @refresh="handleRefresh"
          @add="handleAdd"
          @aiAdd="handleAiAdd"
          @export="handleExport"
          @import="handleImport"
          @update:enabledGroup="enabledModuleGroupChange"
          @update:moduleId="(value) => moduleId = value" />

        <div
          :visible="!!selectedRowKeys?.length"
          class="btn-group-container flex-shrink-0 flex items-center transition-all duration-300 space-x-2.5">
          <Button
            :disabled="batchDisabled.move"
            class="flex items-center px-0 h-5 leading-5"
            type="link"
            size="small"
            @click="handleAction('move')">
            {{ t('actions.move ') }}
          </Button>

          <Button
            :disabled="batchDisabled.del"
            class="flex items-center px-0 h-5 leading-5"
            type="link"
            size="small"
            @click="handleDelete()">
            {{ t('actions.delete') }}
          </Button>

          <Button
            :disabled="batchDisabled.updateTestResult"
            class="flex items-center px-0 h-5 leading-5"
            type="link"
            size="small"
            @click="handleAction('updateTestResult_passed')">
            {{ t('testCase.actions.testPassed') }}
          </Button>

          <Button
            :disabled="batchDisabled.updateTestResult"
            class="flex items-center px-0 h-5 leading-5"
            type="link"
            size="small"
            @click="handleAction('updateTestResult_notPassed')">
            {{ t('testCase.actions.testNotPassed') }}
          </Button>

          <Button
            danger
            type="link"
            size="small"
            class="flex items-center px-0 h-5 leading-5"
            @click="cancelSelectedRowKeys">
            <span>{{ t('actions.cancelBatch') }}</span>
            <span class="ml-1">({{ selectedRowKeys?.length }})</span>
          </Button>
        </div>

        <template v-if="props.viewMode === CaseViewMode.kanban">
          <KanbanView
            v-show="props.viewMode === CaseViewMode.kanban"
            v-model:moduleId="moduleId"
            :filters="params.filters as any"
            :projectId="projectInfo?.id"
            :userInfo="userInfo"
            :appInfo="appInfo"
            class="flex-1"
            @refreshChange="refreshChange"
            @loadingChange="loadingChange" />
        </template>

        <template v-else-if="caseList.length">
          <template v-if="props.viewMode === CaseViewMode.table">
            <TableView
              ref="tabViewRef"
              v-model:selectedRowKeys="selectedRowKeys"
              :params="params"
              :total="total"
              :caseActionAuth="tableAction.auth"
              :actionMenus="tableAction.actionMenus"
              :caseList="caseList"
              class="flex-1 pb-3.5"
              @onClick="handleDetailAction"
              @openInfo="handleViewInfo"
              @change="tableChange" />
          </template>

          <template v-else-if="props.viewMode === CaseViewMode.flat">
            <FlatView
              ref="infoViewRef"
              v-model:selectedRowKeys="selectedRowKeys"
              :params="params"
              :total="total"
              :caseList="caseList"
              :checkedCase="checkedCase"
              class="flex-1"
              @select="handleSelectCase"
              @change="listChange">
              <template #info>
                <CaseDetailPage
                  v-if="checkedCase"
                  id="CaseInfoRef"
                  class="h-full flex-1"
                  :caseId="caseInfo?.id"
                  :caseActionAuth="tableAction.auth"
                  :caseInfo="caseInfo"
                  :userInfo="userInfo"
                  :appInfo="appInfo"
                  :scrollNotify="scrollNotify"
                  :queryParams="detailQueryParams"
                  :currIndex="detailIndex"
                  type="info"
                  @onClick="(type: any, value: any) => handleDetailAction(type, value)"
                  @getInfo="getCaseInfo"
                  @editSuccess="editSuccess"
                  @scroll="checkScroll" />
              </template>
            </FlatView>
          </template>
        </template>

        <template v-if="!props.loading && !caseList?.length && !firstLoading">
          <NoData class="mx-auto flex-1 flex-shrink-0" size="small" />
        </template>
      </div>
    </div>
  </div>

  <AsyncComponent :visible="addVisible">
    <AddModal
      v-model:visible="addVisible"
      :editCase="editCase"
      :moduleId="(moduleId && moduleId !== -1) ? moduleId : undefined"
      @update="addOrEditSuccess" />
  </AsyncComponent>

  <AsyncComponent :visible="aiAddVisible">
    <AiAddModal
      v-model:visible="aiAddVisible"
      @update="addOrEditSuccess" />
  </AsyncComponent>

  <AsyncComponent :visible="reviewVisible">
    <ReviewModal
      v-if="reviewVisible"
      v-model:visible="reviewVisible"
      :selectedCase="selectedCase"
      :selectedRowKeys="selectedRowKeys"
      :type="btnType"
      @update="updateSuccess" />
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
      @update="updateSuccess"
      @addBug="addBug" />
  </AsyncComponent>

  <AsyncComponent :visible="uploadCaseVisible">
    <UploadCaseModal
      v-model:visible="uploadCaseVisible"
      :downloadTemplate="handleExportTemplate"
      @cancel="cancelUpload"
      @ok="handleUploadOk" />
  </AsyncComponent>

  <AsyncComponent :visible="taskModalVisible">
    <EditTaskModal
      v-model:visible="taskModalVisible"
      :projectId="projectInfo?.id"
      :appInfo="appInfo"
      :assigneeId="selectedCase?.developerId"
      :userInfo="userInfo"
      :refCaseIds="selectedCase?.id ? [selectedCase.id] : []"
      :description="selectedCase?.testRemark"
      :name="t('testCase.messages.testNotPassedName', { name: selectedCase?.name || '' })"
      :taskType="TaskType.BUG"
      :confirmerId="selectedCase?.testerId"
      :deadlineDate="undefined"
      :priority="undefined as any"
      :severity="undefined as any"
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
