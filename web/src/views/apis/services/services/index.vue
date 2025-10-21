<script setup lang="ts">
import { computed, defineAsyncComponent, inject, reactive, ref, Ref, watch } from 'vue';
import { AsyncComponent, LeftDrawer, notification, IconText, VuexHelper } from '@xcan-angus/vue-ui';
import {
  TESTER, localStore, utils, duration, appContext, enumUtils, PageQuery, HttpMethod, SearchCriteria
} from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { ServicesPermission, ApiStatus, ApisProtocol } from '@/enums/enums';
import { services, apis } from '@/api/tester';

import { FoldActionKey, foldGlobalActions, globalActions, menuActions, ModalConfig, ServicesInfo } from './types';

// Service left sliders
const UnarchivedApiList = defineAsyncComponent(() => import('@/views/apis/services/services/UnarchivedApiList.vue'));

// // Service global action modals
const CreateServices = defineAsyncComponent(() => import('@/views/apis/services/components/CreateServiceModal.vue'));
const ExportService = defineAsyncComponent(() => import('@/views/apis/services/components/ExportOptionalModal.vue'));
const LocalImport = defineAsyncComponent(() => import('@/views/apis/services/services/LocalImportModal.vue'));

// Service menu action modals
const SyncConfig = defineAsyncComponent(() => import('@/views/apis/services/services/SyncConfigModal.vue'));
const SecurityConfig = defineAsyncComponent(() => import('@/views/apis/services/services/SecurityConfigModal.vue'));
const ServerConfig = defineAsyncComponent(() => import('@/views/apis/services/services/ServerConfigModal.vue'));
const ExportApis = defineAsyncComponent(() => import('@/views/apis/services/components/ExportOptionalModal.vue'));
const ShareService = defineAsyncComponent(() => import('@/components/share/index.vue'));
const TranslateService = defineAsyncComponent(() => import('@/views/apis/services/components/TranslateService.vue'));
const ModifyStatus = defineAsyncComponent(() => import('@/views/apis/services/components/ModifyStatus.vue'));
const Authorize = defineAsyncComponent(() => import('@/components/auth/AuthorizeModal.vue'));
const BatchModifyApiParams = defineAsyncComponent(() => import('@/views/apis/services/services/modifyApiParams/index.vue'));
const GenTestScript = defineAsyncComponent(() => import('@/components/script/GenTestScriptModal.vue'));
const DelTestScript = defineAsyncComponent(() => import('@/components/script/DeleteScriptModal.vue'));
const EnabledApiTest = defineAsyncComponent(() => import('@/components/test/EnabledTestModal.vue'));
const ExecTestModal = defineAsyncComponent(() => import('@/views/apis/services/test/ExecServiceTestModal.vue'));
const CreateTestTask = defineAsyncComponent(() => import('@/components/task/CreateTestTaskModal.vue'));
const RestartTestTask = defineAsyncComponent(() => import('@/components/task/RestartTestTaskModal.vue'));
const ReopenTestTask = defineAsyncComponent(() => import('@/components/task/ReopenTestTaskModal.vue'));
const DelTestTask = defineAsyncComponent(() => import('@/components/task/DeleteTestTaskModal.vue'));

const { t } = useI18n();
const userInfo = ref(appContext.getUser());
const isAdmin = computed(() => appContext.isAdmin());
// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));
const appInfo = inject('appInfo') as Ref<Record<string, any>>;

// TODO proTypeShowMap logic seems incorrect
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));
// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });
// eslint-disable-next-line @typescript-eslint/no-empty-function
const deleteTabPane = inject<(data: any) => void>('deleteTabPane', () => { });
// eslint-disable-next-line @typescript-eslint/no-empty-function
const updateTabPane = inject<(data: any) => void>('updateTabPane', () => { });

// eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/no-unused-vars
const updateApiGroup = inject('updateApiGroup', (_id) => undefined);
const updateProjectInfo = inject('updateProjectInfo', reactive({ id: '', name: '' }));

const { useMutations, useState } = VuexHelper;
const { guideType, stepVisible, stepKey, stepContent } = useState(['guideType', 'stepVisible', 'stepKey', 'stepContent'], 'guideStore');
const { updateGuideType, updateGuideStep } = useMutations(['updateGuideType', 'updateGuideStep'], 'guideStore');

const showDropActions = computed(() => {
  return menuActions.filter(item => proTypeShowMap.value.showTask || item.key !== 'testTask');
});

const modalsConfig = reactive<ModalConfig>({
  syncModalVisible: false,
  serverUrlModalVisible: false,
  importModalVisible: false,
  authenticateModalVisible: false,
  exportInterfaceModalVisible: false,
  shareModalVisible: false,
  authModalVisible: false,
  testScriptVisible: false,
  delTestScriptVisible: false,
  enabledApiTestVisible: false,
  activeId: '',
  auth: false,
  activeName: '',
  type: '',
  statusVisible: false,
  selectedNode: undefined
});

const leftDrawerRef = ref();
const unarchivedRef = ref();
const trashRef = ref();

const leftDrawerFoldFlag = ref(true);
const leftDrawerCollapseActiveKey = ref<'trash'|'unarchived'>();
const refreshLeftDrawerRecord = ref(false); // Track whether to refresh LeftDrawer directory list
const refreshUnarchivedRecord = ref(false); // Track whether to refresh unarchived directory list
const refreshRecycleBinRecord = ref<{project:boolean;api:boolean}>({ // Track whether to refresh recycle bin lists
  project: false,
  api: false
});
const trashActiveKey = ref<string>();

const setScript = ref();
const testVisible = ref(false);
const restartTestVisible = ref(false);
const delTestVisible = ref(false);

const authTabId = 'api-auth-1001';
const inputValue = ref<string>();
const exportVisible = ref(false);
const importVisible = ref(false);
const createVisible = ref(false);

const unarchivedKeywords = ref();
const trashKeywords = ref();

/**
 * Handle fold state changes of the left drawer and trigger deferred refreshes.
 */
const onFoldChange = (foldFlag:boolean) => {
  // Don't refresh when collapsed
  if (!foldFlag) {
    return;
  }

  if (refreshLeftDrawerRecord.value) {
    if (typeof leftDrawerRef.value?.refresh === 'function') {
      leftDrawerRef.value.refresh();
      refreshLeftDrawerRecord.value = false;
    }
  }

  const refreshFlag = Object.values(refreshRecycleBinRecord.value).find(item => item);
  if (refreshFlag) {
    // loadRecycleBinCount();
  }

  onCollapseChange(leftDrawerCollapseActiveKey.value);
};

/**
 * React to collapse panel changes and conditionally refresh related lists.
 */
const onCollapseChange = (key:'trash'|'unarchived'|undefined) => {
  if (key === 'unarchived') {
    if (refreshUnarchivedRecord.value) {
      if (typeof unarchivedRef.value?.refresh === 'function') {
        unarchivedRef.value.refresh();
        refreshUnarchivedRecord.value = false;
        loadUnarchivedCount();
      }
    }

    return;
  }

  if (key === 'trash') {
    const data = refreshRecycleBinRecord.value;
    for (const _key in data) {
      if (data[_key] && trashActiveKey.value === _key) {
      // Refresh corresponding recycle bin list
        if (typeof trashRef.value?.refresh === 'function') {
          trashRef.value.refresh(_key);
          // Reset refresh status
          data[_key] = false;
        }
      }
    }
  }
};

/**
 * Handle quick action clicks from the fold area toolbar.
 */
const onFoldActionClick = (key: FoldActionKey) => {
  switch (key) {
    case 'creatService':
      createVisible.value = true;
      break;
    case 'import':
      importVisible.value = true;
      break;
    case 'export':
      exportVisible.value = true;
      break;
    case 'authorization':
      addTabPane({ name: t('service.home.authTabName'), value: 'auth', id: authTabId, _id: authTabId + 'auth' });
      break;
  }
};

/**
 * Handle dropdown button clicks from the global actions menu.
 */
const onToolbarDropdownClick = (item: {key: FoldActionKey}) => {
  switch (item.key) {
    case 'creatService':
      createServiceOrProject('SERVICE');
      break;
    case 'import':
      importVisible.value = true;
      break;
    case 'export':
      exportVisible.value = true;
      break;
    case 'authorization':
      addTabPane({ name: t('service.home.authTabName'), value: 'auth', id: authTabId, _id: authTabId + 'auth' });
      break;
  }
};

/**
 * Refresh the left drawer list immediately if available.
 */
const refreshLeftList = () => {
  if (typeof leftDrawerRef.value?.refresh === 'function') {
    leftDrawerRef.value.refresh();
  }
};

/**
 * Callback after a global import completes to refresh lists and groups.
 */
const onImportOk = () => {
  refreshLeftList();
  updateApiGroup('all');
};

const params = computed(() => {
  return {
    filters: inputValue.value ? [{ key: 'name', op: SearchCriteria.OpEnum.Match, value: inputValue.value }] : [],
    projectId: projectId.value
  };
});

const auths = ref<string[]>([]);

/**
 * Handle context menu actions on a service item.
 */
const onContextMenuClick = (action: { key: string; }, item: ServicesInfo) => {
  switch (action.key) {
    case 'sync-config':
      modalsConfig.syncModalVisible = true;
      modalsConfig.activeId = item.id;
      break;
    case 'server-config':
      modalsConfig.serverUrlModalVisible = true;
      modalsConfig.activeId = item.id;
      break;
    case 'local-import':
      modalsConfig.importModalVisible = true;
      modalsConfig.activeId = item.id;
      break;
    case 'authentication-config':
      modalsConfig.authenticateModalVisible = true;
      modalsConfig.activeId = item.id;
      modalsConfig.type = 'SERVICE';
      break;
    case 'export-apis':
      modalsConfig.exportInterfaceModalVisible = true;
      modalsConfig.activeId = item.id;
      modalsConfig.selectedNode = item;
      break;
    case 'share':
      modalsConfig.shareModalVisible = true;
      modalsConfig.activeId = item.id;
      modalsConfig.activeName = item.name;
      modalsConfig.type = 'SERVICE';
      break;
    case 'auth':
      modalsConfig.authModalVisible = true;
      modalsConfig.activeId = item.id;
      modalsConfig.auth = item.auth;
      modalsConfig.type = 'SERVICE';
      break;
    case 'delete':
      leftDrawerRef.value.del(item.id, `${TESTER}/services/${item.id}`, {
        title: t('service.service.deleteService'),
        content: t('service.service.deleteServiceTip')
      });
      break;
    case 'translate':
      openTranslateService(item);
      break;
    case 'MODIFY':
      leftDrawerRef.value.edit(item, `${TESTER}/services/${item.id}/name`, {});
      break;
    case 'clone':
      cloneService(item);
      break;
    case 'setStatus':
      openModifyStatus(item);
      break;
    case 'addApi':
      addHttpApiForService(item);
      break;
    case 'addSocket':
      addSocketApiForService(item);
      break;
    case 'mock':
      addTabPane({ ...item, _id: item.id + 'mock', name: t('service.home.mockTabName'), value: 'mock' });
      break;
    case 'setTest':
      openCreateTestTask(item);
      break;
    case 'reTest':
      openRestartTestTask(item);
      break;
    case 'reopen':
      openReopenTestTask(item);
      break;
    case 'deleteTask':
      delTestVisible.value = true;
      modalsConfig.activeId = item.id;
      break;
    case 'setTestScript':
      modalsConfig.testScriptVisible = true;
      modalsConfig.activeId = item.id;
      modalsConfig.type = 'SERVICE';
      setScript.value = 'create';
      break;
    case 'updateTestScript':
      modalsConfig.testScriptVisible = true;
      modalsConfig.activeId = item.id;
      modalsConfig.type = 'SERVICE';
      setScript.value = 'update';
      break;
    case 'delTestScript':
      modalsConfig.delTestScriptVisible = true;
      modalsConfig.type = 'SERVICE';
      modalsConfig.activeId = item.id;
      break;
    case 'enabledTest':
      modalsConfig.enabledApiTestVisible = true;
      modalsConfig.activeId = item.id;
      break;
    case 'funcTestExec':
    case 'perfTestExec':
    case 'stabilityTestExec':
    case 'funcTestExecSmoke':
    case 'funcTestExecSecurity':
      handleExecTest(action.key, item.id);
      break;
    case 'batchAddParams':
    case 'batchModifyParams':
    case 'batchDelParams':
    case 'batchEnabledParams':
    case 'batchDisabledParams':
    case 'batchModifyServer':
    case 'batchLinkVariable':
    case 'batchDelVariable':
    case 'batchLinkDataSet':
    case 'batchDelDataSet':
    case 'batchModifyAuth':
      handleBatch(action, item.id);
      break;
  }
};

const guideProjectId = ref('');
/**
 * Set the project id for the guide and manage guide visibility cache.
 */
const setGuideProjectId = (id:string) => {
  if (!id) {
    const cacheGuideKey = `${userInfo.value?.id}_API_GUIDE`;
    localStore.set(cacheGuideKey, true);
    updateGuideStep({ visible: false, key: '' });
  }
  guideProjectId.value = id;
};

/**
 * Update the guide step visibility and cache when user interacts with guide.
 */
const updateProjectGuideStep = (key:string) => {
  if (key === 'hideDrawer') {
    updateGuideStep({ visible: false, key: '' });
    updateGuideType('');
    const cacheGuideKey = `${userInfo.value?.id}_API_GUIDE`;
    localStore.set(cacheGuideKey, true);
    return;
  }
  updateGuideStep({ visible: true, key });
};

const showImportSamples = ref(false);
const importBtnLoading = ref(false);
/**
 * Respond to list meta changes from LeftDrawer to show sample import entry.
 */
const onListChange = ({ ext = { allowImportSamples: false } }) => {
  showImportSamples.value = !!ext?.allowImportSamples;
};

/**
 * Import service samples and refresh view after completion.
 */
const importServiceSamples = async () => {
  importBtnLoading.value = true;
  const [error] = await services.importServicesSamples();
  importBtnLoading.value = false;
  if (error) {
    return;
  }
  refresh();
};

const infoText = ref<string>();
/**
 * Open create test task modal for a service.
 */
const openCreateTestTask = (_project) => {
  modalsConfig.activeId = _project.id;
  testVisible.value = true;
};

const restartContent = ref('');
/**
 * Open restart test task modal with contextual content.
 */
const openRestartTestTask = (item) => {
  modalsConfig.activeId = item.id;
  restartTestVisible.value = true;
  restartContent.value = t('service.service.restartServiceTip', { name: item.name });
};

// Reopen test task
const reopenTestVisible = ref(false);
const reopenContent = ref('');
/**
 * Open reopen test task modal with contextual content.
 */
const openReopenTestTask = (item) => {
  modalsConfig.activeId = item.id;
  reopenTestVisible.value = true;
  reopenContent.value = t('service.service.reopenServiceTip', { name: item.name });
};

const translateVisible = ref(false);
const execTestVisible = ref(false);
const selectedId = ref<string>();
const execTips = ref<string>();
const execModalTitle = ref<string>();
const okAction = ref<string>();
const execTestTipConfig = {
  funcTestExecSmoke: t('service.service.funcTestExecSmokeTip'),
  funcTestExecSecurity: t('service.service.funcTestExecSecurityTip'),
  funcTestExec: t('service.service.funcTestExecTip'),
  perfTestExec: t('service.service.perfTestExecTip'),
  stabilityTestExec: t('service.service.stabilityTestExecTip')
};

const execModalTitleConfig = {
  funcTestExecSmoke: t('service.service.execModalTitle.funcTestExecSmoke'),
  funcTestExecSecurity: t('service.service.execModalTitle.funcTestExecSecurity'),
  funcTestExec: t('service.service.execModalTitle.funcTestExec'),
  perfTestExec: t('service.service.execModalTitle.perfTestExec'),
  stabilityTestExec: t('service.service.execModalTitle.stabilityTestExec')
};

const getOkAction = (type:'funcTestExecSmoke'|'funcTestExecSecurity'|'funcTestExec'|'perfTestExec'|'stabilityTestExec', id:string) => {
  switch (type) {
    case 'funcTestExecSmoke':
      return `${TESTER}/services/${id}/smoke/exec`;
    case 'funcTestExecSecurity':
      return `${TESTER}/services/${id}/security/exec`;
    case 'funcTestExec':
      return `${TESTER}/services/${id}/exec?testTypes=FUNCTIONAL`;
    case 'perfTestExec':
      return `${TESTER}/services/${id}/exec?testTypes=PERFORMANCE`;
    case 'stabilityTestExec':
      return `${TESTER}/services/${id}/exec?testTypes=STABILITY`;
  }
};

// Execute test
const handleExecTest = async (type:'funcTestExecSmoke'|'funcTestExecSecurity'|'funcTestExec'|'perfTestExec'|'stabilityTestExec', id: string) => {
  selectedId.value = id;
  execTestVisible.value = true;
  execTips.value = execTestTipConfig[type];
  execModalTitle.value = execModalTitleConfig[type];
  okAction.value = getOkAction(type, id);
};

const batchVisible = ref(false);
const activeAction = ref({});
const handleBatch = (action, id: string) => {
  modalsConfig.activeId = id;
  activeAction.value = {
    title: action.name,
    type: action.key
  };

  batchVisible.value = true;
};

// Add a blank project/service
const createServiceOrProject = (type = 'SERVICE') => {
  leftDrawerRef.value.create('-1', `${TESTER}/services`, {
    targetType: type
  });
};

const select = (item) => {
  modalsConfig.activeId = item.id;
  addTabPane({ ...item, _id: item.id + 'group', value: 'group' });
};

const selectedService = ref();
/**
 * Open translate modal for selected service.
 */
const openTranslateService = (item) => {
  translateVisible.value = true;
  selectedService.value = { id: item.id, name: item.name };
};

/**
 * Clone a service and refresh the left drawer on success.
 */
const cloneService = async (item: any) => {
  const [error] = await services.cloneServices(item);
  if (error) {
    return;
  }
  refreshLeftList();
  notification.success(t('actions.tips.cloneSuccess'));
};

const projectStatus = ref();
const statusItem = ref();

/**
 * Open modify status modal and initialize modal context.
 */
const openModifyStatus = async (item: any) => {
  statusItem.value = item;
  modalsConfig.statusVisible = true;
  modalsConfig.activeId = item.id;
  modalsConfig.type = 'SERVICE';
  projectStatus.value = item.status?.value || '';
};

/**
 * Apply selected status value to the cached item.
 */
const applyStatusChange = (status) => {
  statusItem.value.status = status;
};

/**
 * Reflect authentication flag changes to the left drawer list item.
 */
const onAuthFlagChange = ({ auth }:{auth:boolean}) => {
  if (typeof leftDrawerRef.value?.update === 'function') {
    leftDrawerRef.value.update({ id: modalsConfig.activeId, auth });
  }
};

/**
 * Add a temporary HTTP API tab under a service for quick creation.
 */
const addHttpApiForService = async (item) => {
  const _id = utils.uuid('api');
  const param = {
    summary: 'api' + new Date().getTime(),
    ownerId: userInfo.value?.id,
    projectId: item.id, // TODO Is this project ID or service ID?
    assertions: [],
    authentication: null,
    host: '',
    protocol: ApisProtocol.http,
    method: HttpMethod.GET,
    status: ApiStatus.UNKNOWN,
    parameters: [],
    requestBody: {},
    secured: false,
    endpoint: ''
  };

  addTabPane({
    name: param.summary,
    value: 'API',
    _id: _id + 'API',
    pid: _id + 'API',
    unarchived: true,
    projectId: item.id, // TODO Is this project ID or service ID?
    projectName: item.name,
    projectType: item.targetType
  });
};

/**
 * Add a temporary Socket API tab under a service for quick creation.
 */
const addSocketApiForService = async (item) => {
  const _id = utils.uuid('socket');
  const params = {
    summary: 'socket' + new Date().getTime(),
    ownerId: userInfo.value?.id,
    projectId: item.id, // TODO Is this project ID or service ID?
    parameters: [],
    protocol: ApisProtocol.wss,
    method: HttpMethod.GET,
    status: ApiStatus.UNKNOWN
  };

  addTabPane({
    name: params.summary,
    value: 'socket',
    _id: _id + 'socket',
    pid: _id + 'socket',
    unarchived: true,
    projectId: item.id, // TODO Is this project ID or service ID?
    projectName: item.name,
    projectType: item.targetType
  });
};

/**
 * Handle delete service callback to clean related tabs and notify.
 */
const onDeleteService = async (id: string) => {
  deleteTabPane([id + 'group', id + 'mock']);
  notification.success(t('service.service.deleteServiceSuccess'));
};

/**
 * Update the tab title when a service is renamed inline.
 */
const onEditService = (item) => {
  updateTabPane({ pid: item.id + 'group', _id: item.id + 'group', name: item.name });
};

/**
 * Open the service group tab after a new service is created.
 */
const onCreateService = (item) => {
  addTabPane({
    ...item,
    name: item.name,
    _id: item.id + 'group',
    value: 'group'
  });
};

/**
 * Close create modal and refresh the left drawer after creation.
 */
const onCreateServiceOk = () => {
  createVisible.value = false;
  refresh();
};

/**
 * Callback after import into a service finishes to update api group.
 */
const onImportToServiceOk = () => {
  updateApiGroup(modalsConfig.activeId);
};

/**
 * Load current permissions for the dropdown when the list becomes visible.
 */
const onLeftListVisibleChange = async (visible, item) => {
  if (!visible) {
    return;
  }

  listProps.value.dropdownProps.menuItems = showDropActions.value.filter(i => i.key !== 'addService');
  if (isAdmin.value) {
    return;
  }

  if (item.auth) {
    const [error, res] = await services.getCurrentAuth(item.id);
    if (error) {
      return;
    }
    auths.value = (res.data?.permissions || [])?.map(i => i.value);
  } else {
    auths.value = enumUtils.getEnumValues(ServicesPermission);
  }

  if (item?.status?.value === ApiStatus.RELEASED) {
    auths.value = auths.value.filter(permission => ![ServicesPermission.MODIFY, ServicesPermission.DELETE].includes(permission as ServicesPermission));
  }

  listProps.value.dropdownProps.permissions = auths.value;
};

/**
 * Debounced search handler for different left drawer sections.
 */
const onSearchChange = debounce(duration.search, (key: 'trash'|'unarchived', value: string) => {
  if (key === 'trash') {
    trashKeywords.value = value;
    return;
  }

  if (key === 'unarchived') {
    unarchivedKeywords.value = value;
  }
});

/**
 * Load unarchived api count for the current project and update badge.
 */
const loadUnarchivedCount = async () => {
  const [error, res] = await apis.getUnarchivedApiCount({ projectId: projectId.value });
  if (error) {
    return;
  }

  collapseOptions.value[0].total = +res.data;
};

/**
 * Refresh the left drawer or mark a deferred refresh when folded.
 */
const refresh = () => {
  if (!leftDrawerFoldFlag.value) {
    refreshLeftDrawerRecord.value = true;
    return;
  }

  if (typeof leftDrawerRef.value?.refresh === 'function') {
    leftDrawerRef.value.refresh();
  }
};

/**
 * Refresh the unarchived list or defer until the panel is active.
 */
const refreshUnarchived = () => {
  if (!leftDrawerFoldFlag.value) {
    refreshUnarchivedRecord.value = true;
    return;
  }

  loadUnarchivedCount();

  if (leftDrawerCollapseActiveKey.value !== 'unarchived') {
    refreshUnarchivedRecord.value = true;
    return;
  }

  // Refresh unarchived list
  if (typeof unarchivedRef.value?.refresh === 'function') {
    unarchivedRef.value.refresh();
  }
};

// Add service
const editInputProps = computed(() => ({
  createAction: `${TESTER}/services`, // Add directory URL
  createParams: { projectId: projectId.value }, // Parameters needed for adding directory
  allowClear: true,
  placeholder: t('service.service.serviceNamePlaceholder'),
  maxlength: 100
}));

const scrollProps = computed(() => {
  return {
    lineHeight: 36,
    action: `${TESTER}/services`,
    params: params.value
  };
});

watch(() => updateProjectInfo, (newValue) => {
  if (typeof leftDrawerRef.value?.update === 'function') {
    leftDrawerRef.value.update(newValue);
  }
}, { deep: true });

watch(() => projectId.value, newValue => {
  if (newValue) {
    loadUnarchivedCount();
  }
}, {
  immediate: true
});

defineExpose({
  refresh,
  refreshUnarchived
});

const modelTitleMap = {
  SERVICE: t('service.service.authModel.title')
};

const tipMap = {
  SERVICE: {
    on: t('service.service.authModel.onTip'),
    off: t('service.service.authModel.offTip')
  }
};

const searchInputProps = {
  placeholder: t('service.service.searchServicePlaceholder'),
  allowClear: true
};

const listProps = ref({
  maxlevel: 2,
  dropdownProps: {
    menuItems: showDropActions.value,
    permissions: enumUtils.getEnumValues(ServicesPermission),
    trigger: 'contextmenu'
  }
});

const sortProps = {
  menuItems: [{
    name: t('common.createdDate'),
    key: 'createdDate',
    orderSort: PageQuery.OrderSort.Desc
  }, {
    name: t('common.name'),
    key: 'name',
    orderSort: PageQuery.OrderSort.Asc
  }, {
    name: t('common.creator'),
    key: 'createdBy',
    orderSort: PageQuery.OrderSort.Asc
  }]
};

const collapseOptions = ref<{ name: string; key: string; icon: string; total: number; }[]>([
  {
    name: t('service.service.unarchivedTitle'),
    key: 'unarchived',
    icon: 'icon-weiguidang',
    total: 0
  }
]);
</script>
<template>
  <LeftDrawer
    v-if="projectId"
    ref="leftDrawerRef"
    :key="`api_left_drawer_${projectId}`"
    v-model:fold="leftDrawerFoldFlag"
    v-model:collapseActiveKey="leftDrawerCollapseActiveKey"
    :foldActions="foldGlobalActions"
    :collapseOptions="collapseOptions"
    :searchInputProps="searchInputProps"
    :buttonProps="globalActions"
    :editInputProps="editInputProps"
    :sortProps="sortProps"
    :scrollProps="scrollProps"
    :listProps="listProps"
    :guideType="guideType"
    :stepVisible="stepVisible"
    :stepKey="stepKey"
    :stepContent="stepContent"
    storageKey="left-project"
    @collapseChange="onCollapseChange"
    @foldChange="onFoldChange"
    @click="onFoldActionClick"
    @edit="onEditService"
    @del="onDeleteService"
    @create="onCreateService"
    @select="select"
    @searchChange="onSearchChange"
    @buttonDropdownClick="onToolbarDropdownClick"
    @contextmenuClick="onContextMenuClick"
    @visibleChange="onLeftListVisibleChange"
    @listChange="onListChange"
    @updateGuideStep="updateProjectGuideStep"
    @guideProjectId="setGuideProjectId">
    <template #iconText>
      <IconText text="S" class="bg-blue-badge-s" />
    </template>
    <template #name="{name, apisNum}">
      <div class="flex-1 flex leading-8 min-w-0" :title="name">
        <div class="truncate">{{ name }}</div>
        <div class="flex-1 text-text-placeholder ml-0.5">({{ apisNum || 0 }})</div>
      </div>
    </template>
    <template #unarchived>
      <UnarchivedApiList
        ref="unarchivedRef"
        :total="collapseOptions[0].total"
        :searchKeyword="unarchivedKeywords"
        @delete="loadUnarchivedCount"
        @deleteAll="loadUnarchivedCount"
        @refresh="loadUnarchivedCount" />
    </template>
    <template v-if="showImportSamples && !stepVisible" #noData>
      <Button
        szie="small"
        type="link"
        class="text-3"
        :loading="importBtnLoading"
        @click="importServiceSamples">
        Import samples
      </Button>
    </template>
  </LeftDrawer>

  <!-- Export -->
  <AsyncComponent :visible="exportVisible">
    <ExportService
      v-model:visible="exportVisible"
      :selectedNode="undefined" />
  </AsyncComponent>

  <!-- Import -->
  <AsyncComponent :visible="importVisible">
    <LocalImport
      v-model:visible="importVisible"
      source="global"
      @ok="onImportOk" />
  </AsyncComponent>

  <!-- Add project/service -->
  <AsyncComponent :visible="createVisible">
    <CreateServices
      v-model:visible="createVisible"
      @ok="onCreateServiceOk" />
  </AsyncComponent>

  <!-- Sync -->
  <AsyncComponent :visible="modalsConfig.syncModalVisible">
    <SyncConfig
      v-if="modalsConfig.syncModalVisible"
      :id="modalsConfig.activeId"
      v-model:visible="modalsConfig.syncModalVisible" />
  </AsyncComponent>

  <!-- Configure service -->
  <AsyncComponent :visible="modalsConfig.serverUrlModalVisible">
    <ServerConfig
      v-if="modalsConfig.serverUrlModalVisible"
      :id="modalsConfig.activeId"
      v-model:visible="modalsConfig.serverUrlModalVisible" />
  </AsyncComponent>

  <!-- Import -->
  <AsyncComponent :visible="modalsConfig.importModalVisible">
    <LocalImport
      v-model:visible="modalsConfig.importModalVisible"
      :serviceId="modalsConfig.activeId"
      source="services"
      @ok="onImportToServiceOk" />
  </AsyncComponent>

  <!-- Authentication config -->
  <AsyncComponent :visible="modalsConfig.authenticateModalVisible">
    <SecurityConfig :id="modalsConfig.activeId" v-model:visible="modalsConfig.authenticateModalVisible" />
  </AsyncComponent>

  <!-- Export -->
  <AsyncComponent :visible="modalsConfig.exportInterfaceModalVisible">
    <ExportApis
      v-model:visible="modalsConfig.exportInterfaceModalVisible"
      :selectedNode="modalsConfig.selectedNode"
      type="APIS" />
  </AsyncComponent>

  <!-- Share -->
  <AsyncComponent :visible="modalsConfig.shareModalVisible">
    <ShareService
      v-if="modalsConfig.shareModalVisible"
      :id="modalsConfig.activeId"
      v-model:visible="modalsConfig.shareModalVisible"
      source="all"
      :name="modalsConfig.activeName"
      :type="modalsConfig.type" />
  </AsyncComponent>

  <!-- Authorization -->
  <AsyncComponent :visible="modalsConfig.authModalVisible">
    <Authorize
      v-model:visible="modalsConfig.authModalVisible"
      :enumKey="ServicesPermission"
      :appId="appInfo?.id"
      :listUrl="`${TESTER}/services/auth?serviceId=${modalsConfig.activeId}`"
      :delUrl="`${TESTER}/services/auth`"
      :addUrl="`${TESTER}/services/${modalsConfig.activeId}/auth`"
      :updateUrl="`${TESTER}/services/auth`"
      :enabledUrl="`${TESTER}/services/${modalsConfig.activeId}/auth/enabled`"
      :initStatusUrl="`${TESTER}/services/${modalsConfig.activeId}/auth/status`"
      :onTips="tipMap[modalsConfig.type!].on"
      :offTips="tipMap[modalsConfig.type!].off"
      :title="modelTitleMap[modalsConfig.type!]"
      @change="onAuthFlagChange" />
  </AsyncComponent>

  <!-- Test -->
  <AsyncComponent :visible="testVisible">
    <CreateTestTask
      v-model:id="modalsConfig.activeId"
      v-model:visible="testVisible"
      :infoText="infoText"
      type="SERVICE" />
  </AsyncComponent>

  <!-- Restart test -->
  <AsyncComponent :visible="restartTestVisible">
    <RestartTestTask
      v-model:id="modalsConfig.activeId"
      v-model:visible="restartTestVisible"
      :content="restartContent"
      type="SERVICE" />
  </AsyncComponent>

  <!-- Reopen test task -->
  <AsyncComponent :visible="reopenTestVisible">
    <ReopenTestTask
      v-model:visible="reopenTestVisible"
      v-model:id="modalsConfig.activeId"
      :content="reopenContent"
      type="SERVICE" />
  </AsyncComponent>

  <!-- Delete test task -->
  <AsyncComponent :visible="delTestVisible">
    <DelTestTask
      :id="modalsConfig.activeId"
      v-model:visible="delTestVisible"
      type="SERVICE" />
  </AsyncComponent>

  <!-- Set status -->
  <AsyncComponent :visible="modalsConfig.statusVisible">
    <ModifyStatus
      :id="modalsConfig.activeId"
      v-model:visible="modalsConfig.statusVisible"
      :value="projectStatus"
      :type="modalsConfig.type"
      @confirm="applyStatusChange" />
  </AsyncComponent>

  <AsyncComponent :visible="modalsConfig.testScriptVisible">
    <!-- Set script -->
    <GenTestScript
      :id="modalsConfig.activeId"
      v-model:visible="modalsConfig.testScriptVisible"
      :type="modalsConfig.type"
      :setType="setScript" />
  </AsyncComponent>

  <AsyncComponent :visible="modalsConfig.delTestScriptVisible">
    <!-- Delete test script -->
    <DelTestScript
      :id="modalsConfig.activeId"
      v-model:visible="modalsConfig.delTestScriptVisible"
      :type="modalsConfig.type" />
  </AsyncComponent>

  <AsyncComponent :visible="modalsConfig.enabledApiTestVisible">
    <!-- Enable/disable API test -->
    <EnabledApiTest
      :id="modalsConfig.activeId"
      v-model:visible="modalsConfig.enabledApiTestVisible" />
  </AsyncComponent>

  <AsyncComponent :visible="batchVisible">
    <BatchModifyApiParams
      v-model:visible="batchVisible"
      v-bind="activeAction"
      :serviceId="modalsConfig.activeId"
      :projectId="projectId" />
  </AsyncComponent>

  <AsyncComponent :visible="execTestVisible">
    <ExecTestModal
      v-model:serviceId="selectedId"
      v-model:visible="execTestVisible"
      :title="execModalTitle"
      :tips="execTips"
      :okAction="okAction" />
  </AsyncComponent>

  <AsyncComponent :visible="translateVisible">
    <TranslateService
      v-model:visible="translateVisible"
      :service="selectedService"
      :projectId="projectId" />
  </AsyncComponent>
</template>
