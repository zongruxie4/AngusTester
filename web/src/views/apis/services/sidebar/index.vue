<script setup lang="ts">
import { computed, defineAsyncComponent, inject, reactive, ref, Ref, watch } from 'vue';
import { AsyncComponent, LeftDrawer, notification, IconText, VuexHelper } from '@xcan-angus/vue-ui';
import { TESTER, localStore, utils, duration, appContext } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

import { services, apis } from '@/api/tester';
import { actions, ModalsConfig, ServiceProject } from './PropsType';

type FoldActionKey = 'creatProejct' | 'creatService' | 'import' | 'export'|'authorization';
type SuffixActionKey = 'creatService' | 'export' | 'import' | 'authorization';


const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));
const Share = defineAsyncComponent(() => import('@/components/share/index.vue'));
const CreateTestTask = defineAsyncComponent(() => import('@/components/task/CreateTestModal.vue'));
const RestartTestTask = defineAsyncComponent(() => import('@/components/task/RestartTestModal.vue'));
const ReopenTestTask = defineAsyncComponent(() => import('@/components/task/ReopenTestModal.vue'));
const DelTestTask = defineAsyncComponent(() => import('@/components/task/DeleteTestModal.vue'));
const SyncConfig = defineAsyncComponent(() => import('@/views/apis/services/components/syncConfig/modal.vue'));
const ServerUrl = defineAsyncComponent(() => import('@/views/apis/services/components/serverConfig/modal.vue'));
const SecurityConfig = defineAsyncComponent(() => import('@/views/apis/services/components/securityConfig/modal.vue'));
const MovePop = defineAsyncComponent(() => import('@/views/apis/services/components/moveModal/index.vue'));
const Status = defineAsyncComponent(() => import('@/views/apis/services/components/statusModal/index.vue'));
const ExportApis = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/exportServices/index.vue'));
const GenTestScript = defineAsyncComponent(() => import('@/components/script/GenTestScriptModal.vue'));
const DelTestScript = defineAsyncComponent(() => import('@/components/script/DeleteScriptModal.vue'));
const EnabledApiTest = defineAsyncComponent(() => import('@/components/apis/enabledTestModal/index.vue'));
const BatchModify = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/batchModifyApi/index.vue'));
const TranslateModal = defineAsyncComponent(() => import('@/views/apis/services/components/translateService/index.vue'));

const CreateServices = defineAsyncComponent(() => import('@/views/apis/services/components/createServicesModal/index.vue'));
const LocalImport = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/localImport/index.vue'));
const ExportServices = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/exportServices/index.vue'));
// const trash = defineAsyncComponent(() => import('./trash/index.vue'));
const Unarchived = defineAsyncComponent(() => import('@/views/apis/services/sidebar/unarchived/index.vue'));
const ExecTestModal = defineAsyncComponent(() => import('@/views/apis/services/sidebar/execTest/index.vue'));

const { t } = useI18n();
const userInfo = ref(appContext.getUser());
const isAdmin = inject('isAdmin', ref(false));
// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));
const appInfo = inject('appInfo') as Ref<Record<string, any>>;
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
  return actions.filter(item => proTypeShowMap.value.showTask || item.key !== 'testTask');
});

const modalsConfig = reactive<ModalsConfig>({
  syncModalVisible: false,
  serverUrlModalVisible: false,
  importModalVisible: false,
  authenticatModalVisible: false,
  exportInterfaceModalVisible: false,
  shareModalVisible: false,
  authModalVisible: false,
  testScriptVisible: false,
  delTestScriptVisible: false,
  enabeldApiTestVisible: false,
  activeId: '',
  auth: false,
  activeName: '',
  type: undefined,
  statusVisible: false,
  selectedNode: undefined
});

const leftDrawerRef = ref();
const unarchivedRef = ref();
const trashRef = ref();

const leftDrawerFoldFlag = ref(true);
const leftDrawerCollapseActiveKey = ref<'trash'|'unarchived'>();
const refreshLeftDrawerRecord = ref(false);// 记录是否要刷新LeftDrawer的目录列表
const refreshUnarchivedRecord = ref(false);// 记录是否要刷新未归档的目录列表
const refreshRecycleBinRecord = ref<{project:boolean;api:boolean}>({ // 记录是否要刷新回收站的列表
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
const exportVisible = ref(false);// 导出弹窗
const importVisible = ref(false);// 导入
const createVisible = ref(false);// 添加项目、服务

// let parentItem;
const createTargetType = ref<'PROJECT'|'SERVICE'>('PROJECT');

const unarchivedKeywords = ref();
const trashKeywords = ref();

const foldChange = (foldFlag:boolean) => {
  // 折叠状态不刷新
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

  collapseChange(leftDrawerCollapseActiveKey.value);
};

const collapseChange = (key:'trash'|'unarchived'|undefined) => {
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
      // 刷新回收站对应列表
        if (typeof trashRef.value?.refresh === 'function') {
          trashRef.value.refresh(_key);
          // 重置已刷新状态
          data[_key] = false;
        }
      }
    }
  }
};

const foldActionClick = (key: FoldActionKey) => {
  switch (key) {
    case 'creatService':
      createTargetType.value = 'SERVICE';
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

const buttonDropdownClick = (item: {key: SuffixActionKey}) => {
  switch (item.key) {
    case 'creatService':
      createServiceOrProject('SERVICE', '-1');
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

// 刷新
const refreshHandler = () => {
  if (typeof leftDrawerRef.value?.refresh === 'function') {
    leftDrawerRef.value.refresh();
  }
};

const importOk = () => {
  refreshHandler();
  updateApiGroup('all');
};

const params = computed(() => {
  return {
    filters: inputValue.value ? [{ key: 'name', op: 'MATCH_END', value: inputValue.value }] : [],
    projectId: projectId.value
  };
});

const allAuths = ['ADD', 'VIEW', 'MODIFY', 'DELETE', 'DEBUG', 'TEST', 'GRANT', 'SHARE', 'RELEASE', 'EXPORT', 'RENAME', 'CLONE'];
const auths = ref<string[]>(['ADD', 'VIEW', 'MODIFY', 'DELETE', 'DEBUG', 'TEST', 'GRANT', 'SHARE', 'RELEASE', 'EXPORT']);
const parentAuths = ref<string[]>([]);

const contextmenuClick = (action: { key: string; }, item: ServiceProject) => {
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
      modalsConfig.authenticatModalVisible = true;
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
        title: t('service.sidebar.deleteService'),
        content: t('service.sidebar.deleteServiceTip')
      });
      break;
    case 'translate':
      setTranslate(item);
      break;
    case 'rename':
      leftDrawerRef.value.edit(item, `${TESTER}/services/${item.id}/name`, {});
      break;
    case 'clone':
      cloneProject(item);
      break;
    case 'setStatus':
      setStatus(item);
      break;
    case 'addApi':
      addApiByProject(item);
      break;
    case 'addSocket':
      addSocketByProject(item);
      break;
    case 'mock':
      addTabPane({ ...item, _id: item.id + 'mock', name: t('service.home.mockTabName'), value: 'mock' });
      break;
    case 'setTest':
      toSetTest(item);
      break;
    case 'reTest':
      restartTestTask(item);
      break;
    case 'reopen':
      reopenTestTask(item);
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
      modalsConfig.enabeldApiTestVisible = true;
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

const infoText = ref<string>();
const toSetTest = (_project) => {
  modalsConfig.activeId = _project.id;
  testVisible.value = true;
  // infoText.value = '当前服务接口测试任务不存在时生成对应任务，如果任务已存在则覆盖当前测试信息。';
};

const restartContent = ref('');
const restartTestTask = (item) => {
  modalsConfig.activeId = item.id;
  restartTestVisible.value = true;
  restartContent.value = t('service.sidebar.restartServiceTip', { name: item.name });
};

// 重新打开测试任务
const reopenTestVisible = ref(false);
const reopenContent = ref('');
const reopenTestTask = (item) => {
  modalsConfig.activeId = item.id;
  reopenTestVisible.value = true;
  reopenContent.value = t('service.sidebar.reopenServiceTip', { name: item.name });
};

const translateVisible = ref(false);
const execTestVisible = ref(false);
const selectedId = ref<string>();
const execTips = ref<string>();
const execModalTitle = ref<string>();
const okAction = ref<string>();
const execTestTipConfig = {
  funcTestExecSmoke: t('service.sidebar.funcTestExecSmokeTip'),
  funcTestExecSecurity: t('service.sidebar.funcTestExecSecurityTip'),
  funcTestExec: t('service.sidebar.funcTestExecTip'),
  perfTestExec: t('service.sidebar.perfTestExecTip'),
  stabilityTestExec: t('service.sidebar.stabilityTestExecTip')
};

const execModalTitleConfig = {
  funcTestExecSmoke: t('service.sidebar.execModalTitle.funcTestExecSmoke'),
  funcTestExecSecurity: t('service.sidebar.execModalTitle.funcTestExecSecurity'),
  funcTestExec: t('service.sidebar.execModalTitle.funcTestExec'),
  perfTestExec: t('service.sidebar.execModalTitle.perfTestExec'),
  stabilityTestExec: t('service.sidebar.execModalTitle.stabilityTestExec')
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

// 执行测试
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

// 添加一条空白项目/服务
const createServiceOrProject = (type = 'SERVICE', pid = '-1') => {
  leftDrawerRef.value.create(pid, `${TESTER}/services`, {
    targetType: type
  });
};

const select = (item) => {
  modalsConfig.activeId = item.id;
  addTabPane({ ...item, _id: item.id + 'group', value: 'group' });
};

const selectedService = ref();
const setTranslate = (item) => {
  translateVisible.value = true;
  selectedService.value = { id: item.id, name: item.name };
};

// 克隆
const cloneProject = async (item: any) => {
  const [error] = await services.cloneServices(item);
  if (error) {
    return;
  }
  refreshHandler();
  notification.success(t('tips.cloneSuccess'));
};

const projectStatus = ref();
const statusItem = ref();
// 打开修改状态弹窗
const setStatus = async (item: any) => {
  statusItem.value = item;
  modalsConfig.statusVisible = true;
  modalsConfig.activeId = item.id;
  modalsConfig.type = 'SERVICE';
  projectStatus.value = item.status?.value || '';
};

// 修改状态值
const changeStatusValue = (status) => {
  statusItem.value.status = status;
};

// 权限表示变化
const authFlagChange = ({ auth }:{auth:boolean}) => {
  if (typeof leftDrawerRef.value?.update === 'function') {
    leftDrawerRef.value.update({ id: modalsConfig.activeId, auth });
  }
};

// 添加http接口
const addApiByProject = async (item) => {
  const _id = utils.uuid('api');
  const param = {
    summary: 'api' + new Date().getTime(),
    ownerId: userInfo.value?.id,
    projectId: item.id,
    assertions: [],
    authentication: null,
    host: '',
    method: 'GET',
    parameters: [],
    requestBody: {},
    secured: false,
    endpoint: '',
    protocol: 'http'
  };

  addTabPane({
    name: param.summary,
    value: 'API',
    _id: _id + 'API',
    pid: _id + 'API',
    unarchived: true,
    projectId: item.id,
    projectName: item.name,
    projectType: item.targetType
  });
};

// 添加soket接口
const addSocketByProject = async (item) => {
  const _id = utils.uuid('socket');
  const params = {
    summary: 'socket' + new Date().getTime(),
    ownerId: userInfo.value?.id,
    projectId: item.id,
    parameters: [],
    protocol: 'wss',
    method: 'GET',
    status: 'UNKNOWN'
  };

  addTabPane({
    name: params.summary,
    value: 'socket',
    _id: _id + 'socket',
    pid: _id + 'socket',
    unarchived: true,
    projectId: item.id,
    projectName: item.name,
    projectType: item.targetType
  });
};

// 删除 callback
const deleteProject = async (id: string) => {
  deleteTabPane([id + 'group', id + 'mock']);
  notification.success(t('service.sidebar.deleteServiceSuccess'));
};

const editProject = (item) => {
  updateTabPane({ pid: item.id + 'group', _id: item.id + 'group', name: item.name });
};

const create = (item) => {
  addTabPane({
    ...item,
    name: item.name,
    _id: item.id + 'group',
    value: 'group'
  });
};

const createProjectOk = () => {
  createVisible.value = false;

  refresh();
};

const moveVisible = ref(false); // 移动弹窗
const movePid = ref<string>();// 当前移动目录id
const moveParentName = ref<string>();// 当前移动目录名称
// // @TODO 缺少项目或服务信息
// const toMove = (_project: ServiceProject) => {
//   modalsConfig.activeId = _project.id;
//   movePid.value = _project.pid;
//   if (movePid.value === '-1') {
//     moveParentName.value = '根目录';
//   } else {
//     moveParentName.value = parentItem.name;
//   }
//   moveVisible.value = true;
// };
const moveHandle = () => {
  refreshHandler();
  moveCancel();
};
// 移动弹窗关闭
const moveCancel = () => {
  moveVisible.value = false;
  movePid.value = undefined;
  moveParentName.value = undefined;
};

const handleExportEnd = () => {
  updateApiGroup(modalsConfig.activeId);
};

// loadActionAuth
const visibleChange = async (visible, item, pItem) => {
  if (!visible) {
    return;
  }
  // parentItem = pItem || undefined;
  listProps.value.dropdownProps.menuItems = showDropActions.value.filter(i => i.key !== 'addServive');
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
    auths.value = allAuths;
  }
  if (item?.status?.value !== 'RELEASED') {
    if (auths.value.includes('MODIFY')) {
      auths.value.push('RENAME');
    }
  } else {
    auths.value = auths.value.filter(permission => !['RENAME', 'DELETE'].includes(permission));
  }
  auths.value.push('CLONE');
  if (item.pid > 0) {
    if (pItem?.auth) {
      const [perror, pResp] = await services.getCurrentAuth(pItem.id);
      if (!perror) {
        parentAuths.value = (pResp.data?.permissions || []).map(i => i.value);
      }
    } else {
      parentAuths.value = allAuths;
    }
    if (!parentAuths.value.includes('ADD')) {
      auths.value = auths.value.filter(permission => !['CLONE'].includes(permission));
    }
  }
  listProps.value.dropdownProps.permissions = auths.value;
};

watch(() => updateProjectInfo, (newValue) => {
  if (typeof leftDrawerRef.value?.update === 'function') {
    leftDrawerRef.value.update(newValue);
  }
}, { deep: true });

const searchChange = debounce(duration.search, (key: 'trash'|'unarchived', value: string) => {
  if (key === 'trash') {
    trashKeywords.value = value;
    return;
  }

  if (key === 'unarchived') {
    unarchivedKeywords.value = value;
  }
});

const loadUnarchivedCount = async () => {
  const [error, res] = await apis.getUnarchivedApiCount({ projectId: projectId.value });
  if (error) {
    return;
  }

  collapseOptions.value[0].total = +res.data;
};

watch(() => projectId.value, newValue => {
  if (newValue) {
    loadUnarchivedCount();
  }
}, {
  immediate: true
});

const refresh = () => {
  if (!leftDrawerFoldFlag.value) {
    refreshLeftDrawerRecord.value = true;
    return;
  }

  if (typeof leftDrawerRef.value?.refresh === 'function') {
    leftDrawerRef.value.refresh();
  }
};

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

  // 刷新未归档列表
  if (typeof unarchivedRef.value?.refresh === 'function') {
    unarchivedRef.value.refresh();
  }
};

defineExpose({
  // 刷新左侧目录列表
  refresh,

  // 刷新左侧未归档列表
  refreshUnarchived
});

const modelTitleMap = {
  SERVICE: t('service.sidebar.authModel.title')
};

const tipMap = {
  SERVICE: {
    on: t('service.sidebar.authModel.onTip'),
    off: t('service.sidebar.authModel.offTip')
  }
};

const scrollProps = computed(() => {
  return {
    lineHeight: 36,
    action: `${TESTER}/services`,
    params: params.value
  };
});

const listProps = ref({
  maxlevel: 2,
  dropdownProps: {
    menuItems: showDropActions.value,
    permissions: ['ADD', 'VIEW', 'MODIFY', 'DELETE', 'DEBUG', 'TEST', 'GRANT', 'SHARE', 'RELEASE', 'EXPORT'],
    trigger: 'contextmenu'
  }
});

// 排序 选项
const sortProps = {
  menuItems: [{
    name: t('service.sidebar.sortMenu.sortByTime'),
    key: 'createdDate',
    orderSort: 'DESC'
  }, {
    name: t('service.sidebar.sortMenu.sortByName'),
    key: 'name',
    orderSort: 'ASC'
  }, {
    name: t('service.sidebar.sortMenu.sortByAdd'),
    key: 'createdBy',
    orderSort: 'ASC'
  }]
};

// 添加项目
const editInputProps = computed(() => ({
  createAction: `${TESTER}/services`, // 添加目录url
  createParams: { projectId: projectId.value }, // 添加目录需要传递的参数
  allowClear: true,
  placeholder: t('service.sidebar.serviceNamePlaceholder'),
  maxlength: 100
}));

const collapseOptions = ref<{ name: string; key: string; icon: string; total: number; }[]>([
  {
    name: t('service.sidebar.unarchivedTitle'),
    key: 'unarchived',
    icon: 'icon-weiguidang',
    total: 0
  }
]);

const foldActions = ref<{ name: string; key: FoldActionKey; icon: string; }[]>([
  {
    name: t('service.sidebar.foldAction.addService'),
    key: 'creatService',
    icon: 'icon-chuangjianfuwu'
  },
  {
    name: t('service.sidebar.foldAction.localImport'),
    key: 'import',
    icon: 'icon-shangchuan'
  },
  {
    name: t('actions.export'),
    key: 'export',
    icon: 'icon-daochu1'
  },
  {
    name: t('actions.authority'),
    key: 'authorization',
    icon: 'icon-quanxian1'
  }
]);

// 搜索 props
const searchInputProps = {
  placeholder: t('service.sidebar.searchServicePlaceholder'),
  allowClear: true
};
const buttonProps = {
  name: t('service.sidebar.topAction.addService'),
  menuItems: [
    {
      name: t('actions.import'),
      key: 'import',
      icon: 'icon-shangchuan'
    },
    {
      name: t('actions.export'),
      key: 'export',
      icon: 'icon-daochu1'
    },
    {
      name: t('actions.authority'),
      key: 'authorization',
      icon: 'icon-quanxian1'
    }
  ]
};

const guideProjectId = ref('');
const getGuideProjectId = (id:string) => {
  if (!id) {
    const cacheGuideKey = `${userInfo.value?.id}_API_GUIDE`;
    localStore.set(cacheGuideKey, true);
    updateGuideStep({ visible: false, key: '' });
  }
  guideProjectId.value = id;
};

const proejctGuideStep = (key:string) => {
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
const handleListChange = ({ ext = { allowImportSamples: false } }) => {
  if (ext?.allowImportSamples) {
    showImportSamples.value = true;
  } else {
    showImportSamples.value = false;
  }
};

const importSamples = async () => {
  importBtnLoading.value = true;
  const [error] = await services.importServicesSamples();
  importBtnLoading.value = false;
  if (error) {
    return;
  }
  refresh();
};

</script>
<template>
  <LeftDrawer
    v-if="projectId"
    ref="leftDrawerRef"
    :key="`api_left_drawer_${projectId}`"
    v-model:fold="leftDrawerFoldFlag"
    v-model:collapseActiveKey="leftDrawerCollapseActiveKey"
    :foldActions="foldActions"
    :collapseOptions="collapseOptions"
    :searchInputProps="searchInputProps"
    :buttonProps="buttonProps"
    :editInputProps="editInputProps"
    :sortProps="sortProps"
    :scrollProps="scrollProps"
    :listProps="listProps"
    :guideType="guideType"
    :stepVisible="stepVisible"
    :stepKey="stepKey"
    :stepContent="stepContent"
    storageKey="left-project"
    @collapseChange="collapseChange"
    @foldChange="foldChange"
    @click="foldActionClick"
    @edit="editProject"
    @del="deleteProject"
    @create="create"
    @select="select"
    @searchChange="searchChange"
    @buttonDropdownClick="buttonDropdownClick"
    @contextmenuClick="contextmenuClick"
    @visibleChange="visibleChange"
    @listChange="handleListChange"
    @updateGuideStep="proejctGuideStep"
    @guideProjectId="getGuideProjectId">
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
      <Unarchived
        ref="unarchivedRef"
        :total="collapseOptions[0].total"
        :keywords="unarchivedKeywords"
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
        @click="importSamples">
        导入示例
      </Button>
    </template>
  </LeftDrawer>
  <!-- 导出 -->
  <AsyncComponent :visible="exportVisible">
    <ExportServices v-model:visible="exportVisible" :selectedNode="undefined" />
  </AsyncComponent>

  <!-- 导入 -->
  <AsyncComponent :visible="importVisible">
    <LocalImport
      v-model:visible="importVisible"
      source="global"
      @ok="importOk" />
  </AsyncComponent>

  <!-- 添加项目、服务 -->
  <AsyncComponent :visible="createVisible">
    <CreateServices
      v-model:visible="createVisible"
      @ok="createProjectOk" />
  </AsyncComponent>

  <!-- 同步 -->
  <AsyncComponent :visible="modalsConfig.syncModalVisible">
    <SyncConfig
      v-if="modalsConfig.syncModalVisible"
      :id="modalsConfig.activeId"
      v-model:visible="modalsConfig.syncModalVisible" />
  </AsyncComponent>

  <!-- 配置服务 -->
  <AsyncComponent :visible="modalsConfig.serverUrlModalVisible">
    <ServerUrl
      v-if="modalsConfig.serverUrlModalVisible"
      :id="modalsConfig.activeId"
      v-model:visible="modalsConfig.serverUrlModalVisible" />
  </AsyncComponent>

  <!-- 导入 -->
  <AsyncComponent :visible="modalsConfig.importModalVisible">
    <LocalImport
      v-model:visible="modalsConfig.importModalVisible"
      :serviceId="modalsConfig.activeId"
      source="projectOrService"
      @ok="handleExportEnd" />
  </AsyncComponent>

  <!-- 认证配置 -->
  <AsyncComponent :visible="modalsConfig.authenticatModalVisible">
    <SecurityConfig :id="modalsConfig.activeId" v-model:visible="modalsConfig.authenticatModalVisible" />
  </AsyncComponent>

  <!-- 导出 -->
  <AsyncComponent :visible="modalsConfig.exportInterfaceModalVisible">
    <ExportApis
      v-model:visible="modalsConfig.exportInterfaceModalVisible"
      :selectedNode="modalsConfig.selectedNode"
      type="APIS" />
  </AsyncComponent>

  <!-- 移动服务 -->
  <AsyncComponent :visible="moveVisible">
    <MovePop
      :id="modalsConfig.activeId"
      :visible="moveVisible"
      :pid="movePid"
      :parentName="moveParentName"
      :projectId="projectId"
      type="service"
      @ok="moveHandle"
      @cancel="moveCancel" />
  </AsyncComponent>

  <!-- 分享 -->
  <AsyncComponent :visible="modalsConfig.shareModalVisible">
    <Share
      v-if="modalsConfig.shareModalVisible"
      :id="modalsConfig.activeId"
      v-model:visible="modalsConfig.shareModalVisible"
      source="all"
      :name="modalsConfig.activeName"
      :type="modalsConfig.type" />
  </AsyncComponent>

  <!-- 权限 -->
  <AsyncComponent :visible="modalsConfig.authModalVisible">
    <AuthorizeModal
      v-model:visible="modalsConfig.authModalVisible"
      enumKey="ServicesPermission"
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
      @change="authFlagChange" />
  </AsyncComponent>

  <!-- 测试 -->
  <AsyncComponent :visible="testVisible">
    <CreateTestTask
      v-model:id="modalsConfig.activeId"
      v-model:visible="testVisible"
      :infoText="infoText"
      type="SERVICE" />
  </AsyncComponent>

  <!-- 重新开始测试 -->
  <AsyncComponent :visible="restartTestVisible">
    <RestartTestTask
      v-model:id="modalsConfig.activeId"
      v-model:visible="restartTestVisible"
      :content="restartContent"
      type="SERVICE" />
  </AsyncComponent>

  <!-- 重新打开测试任务 -->
  <AsyncComponent :visible="reopenTestVisible">
    <ReopenTestTask
      v-model:visible="reopenTestVisible"
      v-model:id="modalsConfig.activeId"
      :content="reopenContent"
      type="SERVICE" />
  </AsyncComponent>

  <!-- 删除测试任务 -->
  <AsyncComponent :visible="delTestVisible">
    <DelTestTask
      :id="modalsConfig.activeId"
      v-model:visible="delTestVisible"
      type="SERVICE" />
  </AsyncComponent>

  <!-- 设置状态 -->
  <AsyncComponent :visible="modalsConfig.statusVisible">
    <Status
      :id="modalsConfig.activeId"
      v-model:visible="modalsConfig.statusVisible"
      :value="projectStatus"
      :type="modalsConfig.type"
      @confirm="changeStatusValue" />
  </AsyncComponent>

  <AsyncComponent :visible="modalsConfig.testScriptVisible">
    <!-- 设置脚本 -->
    <GenTestScript
      :id="modalsConfig.activeId"
      v-model:visible="modalsConfig.testScriptVisible"
      :type="modalsConfig.type"
      :setType="setScript" />
  </AsyncComponent>

  <AsyncComponent :visible="modalsConfig.delTestScriptVisible">
    <!-- 删除测试脚本 -->
    <DelTestScript
      :id="modalsConfig.activeId"
      v-model:visible="modalsConfig.delTestScriptVisible"
      :type="modalsConfig.type" />
  </AsyncComponent>

  <AsyncComponent :visible="modalsConfig.enabeldApiTestVisible">
    <!-- 启用、禁用接口测试 -->
    <EnabledApiTest
      :id="modalsConfig.activeId"
      v-model:visible="modalsConfig.enabeldApiTestVisible" />
  </AsyncComponent>

  <AsyncComponent :visible="batchVisible">
    <BatchModify
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
    <TranslateModal
      v-model:visible="translateVisible"
      :service="selectedService"
      :projectId="projectId" />
  </AsyncComponent>
</template>
