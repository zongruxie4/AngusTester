<script setup lang="ts">
import { computed, defineAsyncComponent, nextTick, onBeforeUnmount, onMounted, provide, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import {
  Arrow,
  AsyncComponent,
  Colon,
  Dropdown,
  DropdownSort,
  Hints,
  HttpMethodText,
  Icon,
  IconCopy,
  IconRefresh,
  IconRequired,
  Input,
  modal,
  notification,
  PureCard,
  Scroll,
  Spin,
  Tooltip,
  Validate
} from '@xcan-angus/vue-ui';
import { useRouter } from 'vue-router';
import { SearchCriteria, PageQuery, HttpMethod, ResponseDelayMode, utils, TESTER, duration, download } from '@xcan-angus/infra';
import ReconnectingWebSocket from 'reconnecting-websocket';
import { Button, Collapse, CollapsePanel, Switch } from 'ant-design-vue';
import { debounce } from 'throttle-debounce';

import { MockServicePermission } from '@/enums/enums';
import { type AgentValue } from '@/views/apis/services/components/agent/PropsTypes';
import { mock } from 'src/api/tester';
import { setting } from 'src/api/gm';
import { MockAPIConfig, MockAPIInfo, ResponseConfig, ResponseInfo } from './types';

interface Props {
  id: string;// mock service Id
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

const DebugApis = defineAsyncComponent(() => import('./DebugApis.vue'));
const SelectApiModal = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/SelectApiModal.vue'));
const MatchForm = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/match/index.vue'));
const ContentForm = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/content/index.vue'));
const PushBack = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/content/PushBack.vue'));
const UrlForm = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/UrlForm.vue'));
const ImportApiModal = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/ImportApiModal.vue'));

const router = useRouter();
const { t } = useI18n();

const WS = ref();
const uuid = ref();
const wsResponse = ref();
const currentProxy = ref<AgentValue>(); // CLIENT_PROXY|NO_PROXY|SERVER_PROXY|CLOUD_PROXY
const currentProxyUrl = ref();
const proxyOptObj = ref();

const serviceUrlOptions = ref<{ label: string; value: string; }[]>([]);

// ---- ScrollList start ----

const scrollRef = ref();
const urlFormRef = ref();
const matchFormRefs = ref<any[]>([]);
const contentFormRefs = ref<any[]>([]);
const pushbackFormRefs = ref<any[]>([]);

const domId = utils.uuid();
const notify = ref(0);
const responseNotify = ref(0);
const loading = ref(true);
const apiCopyModalVisible = ref(false);
const apiLinkModalVisible = ref(false);
const apiCopyConfirmLoading = ref(false);
const apiLinkConfirmLoading = ref(false);
const importApiModalVisible = ref(false);

const inputValue = ref<string>();
const orderBy = ref<'createdDate' | 'id'>('createdDate');
const orderSort = ref<PageQuery.OrderSort>(PageQuery.OrderSort.Desc);
const action = `${TESTER}/mock/apis`;

const apiIds = ref<string[]>([]);
const apiDataMap = ref<{ [key: string]: MockAPIConfig }>({});
const permissionMap = ref<Map<string, string[]>>(new Map());

const tempMockApiMap = ref<{
  [key: string]: {
    api: {
      isTempFlag: boolean;
      summary: string;
      method: HttpMethod;
      endpoint: string;
      description: string;
    };
    response: {
      responseIdList: string[];
      responseMap: { [key: string]: ResponseConfig };
      nameMap: { [key: string]: string | undefined };
      openKeys: string[];
      priorityMap: { [key: string]: string };
      enablePushbackSet: Set<string>;
    }
  }
}>({});
const mockAPIConfig = ref<MockAPIConfig>();
const mockAPIId = ref<string>();
const readonly = ref(false);

const summary = ref<string>('');
const summaryError = ref(false);
const method = ref<HttpMethod>(HttpMethod.GET);
const endpoint = ref<string>('');
const description = ref<string>('');

const responseIdList = ref<string[]>([]);// 响应id集合
const responseMap = ref<{ [key: string]: ResponseConfig }>({});
const nameMap = ref<{ [key: string]: string | undefined }>({});
const nameErrorSet = ref<Set<string>>(new Set());
const nameErrorMessage = ref<{ [key: string]: string | undefined }>({});
const openKeys = ref<string[]>([]);// 打开的折叠面板
const priorityMap = ref<{ [key: string]: string }>({});// 优先级
const priorityErrorSet = ref<Set<string>>(new Set());
const enablePushbackSet = ref<Set<string>>(new Set());

const isSavedMockApi = computed(() => {
  return mockAPIConfig.value?.id && !mockAPIConfig.value?.isTempFlag;
});

const params = computed(() => {
  const _params: {
    mockServiceId: string;
    filters?: [{ key: 'summary'; op: SearchCriteria.OpEnum.Match, value: string }];
    orderBy?: string;
    orderSort?: PageQuery.OrderSort;
  } = {
    mockServiceId: props.id
  };

  if (inputValue.value) {
    _params.filters = [{ key: 'summary', op: SearchCriteria.OpEnum.Match as any, value: inputValue.value }];
  }

  if (orderBy.value) {
    _params.orderBy = orderBy.value;
  }

  if (orderSort.value) {
    _params.orderSort = orderSort.value;
  }

  return _params;
});

/**
 * Add a new mock API
 */
const handleAddMockApi = () => {
  if (typeof scrollRef.value?.pureAdd === 'function') {
    const id = utils.uuid();
    const data: MockAPIConfig = {
      id,
      summary: 'MockAPI-' + Date.now(),
      isTempFlag: true,
      mockServiceId: props.id,
      method: HttpMethod.GET,
      endpoint: '',
      description: ''
    };

    scrollRef.value.pureAdd(data);
    apiIds.value.unshift(id);
    apiDataMap.value[id] = data;
    permissionMap.value.set(id, [MockServicePermission.DELETE]);
    setApiForm(data);
  }
};

/**
 * Handle scroll list data change
 * @param dataList - List of mock API info
 */
const handleScrollChange = (dataList: MockAPIInfo[]) => {
  apiIds.value = [];
  apiDataMap.value = {};
  for (let i = 0, len = dataList.length; i < len; i++) {
    const data = dataList[i];
    const { id, method, isTempFlag } = data;
    apiIds.value.push(id);
    apiDataMap.value[id] = {
      ...data,
      isTempFlag,
      method: method.value
    };
    permissionMap.value.set(id, [MockServicePermission.DELETE, MockServicePermission.EXPORT]);
  }

  if (!dataList.length) {
    mockAPIId.value = undefined;
    mockAPIConfig.value = undefined;
    resetMockApi();
    resetMockApiResponse();
    handleAddMockApi();
    addResponse();
    return;
  }

  if (!mockAPIId.value || !apiIds.value.includes(mockAPIId.value)) {
    selectHandler(dataList[0].id);
    return;
  }

  if (!mockAPIConfig.value) {
    mockAPIConfig.value = apiDataMap.value[mockAPIId.value];
    selectHandler(mockAPIId.value);
  }
};

/**
 * Set API form data
 * @param data - Mock API configuration data
 */
const setApiForm = (data: MockAPIConfig) => {
  const {
    id,
    summary: _summary,
    method: _method,
    endpoint: _endpoint,
    description: _description
  } = data;
  summary.value = _summary;
  method.value = _method;
  endpoint.value = _endpoint;
  description.value = _description;
  mockAPIId.value = id;
  mockAPIConfig.value = data;
  summaryError.value = false;
};

/**
 * Select a mock API
 * @param id - API ID to select
 */
const handleSelect = (id: string) => {
  if (id === mockAPIId.value) {
    return;
  }

  storeTempMockApiData();
  selectHandler(id);
};

const selectHandler = (id: string) => {
  const prevMockApiData = tempMockApiMap.value[id];
  if (prevMockApiData) {
    resetMockApi();
    resetMockApiResponse();

    const { api, response } = tempMockApiMap.value[id];
    summary.value = api.summary;
    method.value = api.method;
    endpoint.value = api.endpoint;
    description.value = api.description;
    enablePushbackSet.value = response.enablePushbackSet;
    nameMap.value = response.nameMap;
    openKeys.value = response.openKeys;
    priorityMap.value = response.priorityMap;
    responseIdList.value = response.responseIdList;
    responseMap.value = response.responseMap;
    mockAPIId.value = id;
    mockAPIConfig.value = {
      id,
      isTempFlag: api.isTempFlag,
      summary: api.summary,
      method: method.value,
      endpoint: api.endpoint,
      description: api.description,
      mockServiceId: props.id
    };

    loading.value = false;
    nextTick(() => {
      scrollToTop();
    });
    return;
  }

  const data = apiDataMap.value[id];
  setApiForm(data);
  loadApiResponse();
};

const storeTempMockApiData = () => {
  const prevApiId = mockAPIId.value;
  if (!prevApiId || !apiIds.value.includes(prevApiId)) {
    return;
  }

  const responseMap: { [key: string]: ResponseConfig } = {};
  const ids = responseIdList.value;
  const matchRefs = matchFormRefs.value;
  const contentRefs = contentFormRefs.value;
  const pushbackRefs = pushbackFormRefs.value;
  const enableSet = enablePushbackSet.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    responseMap[id] = {
      name: '',
      pushback: undefined,
      match: {
        body: undefined,
        parameters: undefined,
        path: undefined,
        priority: '1000'
      },
      content: {
        content: '',
        headers: [],
        contentEncoding: undefined,
        delay: {
          mode: ResponseDelayMode.NONE,
          fixedTime: undefined,
          maxRandomTime: undefined,
          minRandomTime: undefined
        },
        status: '200'
      }
    };

    responseMap[id].match = { priority: priorityMap.value[id], body: undefined, parameters: undefined, path: undefined };
    if (typeof matchRefs[i]?.getData === 'function') {
      const tempData = matchRefs[i].getData();
      responseMap[id].match = tempData;
    }

    if (typeof contentRefs[i]?.getData === 'function') {
      responseMap[id].content = contentRefs[i].getData();
    }

    if (enableSet.has(id)) {
      if (typeof pushbackRefs[i]?.getData === 'function') {
        responseMap[id].pushback = pushbackRefs[i].getData();
      }
    }
  }

  tempMockApiMap.value[prevApiId] =
  {
    api: {
      isTempFlag: !!apiDataMap.value[prevApiId]?.isTempFlag,
      summary: summary.value,
      method: method.value,
      endpoint: endpoint.value,
      description: description.value
    },
    response: {
      responseMap,
      enablePushbackSet: new Set(enablePushbackSet.value),
      nameMap: JSON.parse(JSON.stringify(nameMap.value)),
      openKeys: JSON.parse(JSON.stringify(openKeys.value)),
      priorityMap: JSON.parse(JSON.stringify(priorityMap.value)),
      responseIdList: JSON.parse(JSON.stringify(responseIdList.value))
    }
  };
};

/**
 * Handle dropdown sort change
 * @param data - Sort configuration data
 */
const handleDropdownSortChange = (data: { orderBy: 'createdDate' | 'id', orderSort: 'ASC' | 'DESC' }) => {
  orderBy.value = data.orderBy;
  orderSort.value = data.orderSort as any;
};

/**
 * Trigger refresh
 */
const handleRefresh = () => {
  notify.value++;
  loading.value = true;
};

/**
 * Handle dropdown menu click
 * @param param0 - Click event data
 * @param id - API ID
 */
const handleDropdownClick = async ({ key }: { key: 'clone' | 'delete' | 'export' }, id: string) => {
  if (key === 'clone') {
    handleClone(id);
    return;
  }

  if (key === 'delete') {
    handleDelete(id);
    return;
  }

  if (key === 'export') {
    handleExport(id);
  }
};

const buttonDropdownClick = ({ key }: { key: 'copyApi' | 'linkApi' | 'import' | 'importExamples' }) => {
  // 选择接口添加
  if (key === 'copyApi') {
    apiCopyModalVisible.value = true;
    return;
  }

  // 关联接口添加
  if (key === 'linkApi') {
    apiLinkModalVisible.value = true;
    return;
  }

  // 导入接口
  if (key === 'import') {
    importApiModalVisible.value = true;
    return;
  }

  // 导入示例
  if (key === 'importExamples') {
    importExamples();
  }
};

const copyApiOk = async ({ id }) => {
  apiCopyConfirmLoading.value = true;
  const [error, { data }] = await mock.copyApiToMock(props.id, id);
  apiCopyConfirmLoading.value = false;
  if (error) {
    return;
  }

  apiCopyModalVisible.value = false;
  mockAPIId.value = data.id;
  mockAPIConfig.value = undefined;
  notification.success(t('mock.detail.apis.notifications.copyApiSuccess'));
  handleRefresh();
  nextTick(() => {
    scrollToTop();
  });
};

const linkApiOk = async ({ id }) => {
  apiLinkConfirmLoading.value = true;
  const [error, { data }] = await mock.assocApiToMock(props.id, id);
  apiLinkConfirmLoading.value = false;
  if (error) {
    return;
  }

  apiLinkModalVisible.value = false;
  mockAPIId.value = data.id;
  mockAPIConfig.value = undefined;
  notification.success(t('mock.detail.apis.notifications.linkApiSuccess'));
  handleRefresh();
  nextTick(() => {
    scrollToTop();
  });
};

const importApiOk = () => {
  handleRefresh();
  nextTick(() => {
    scrollToTop();
  });
};

// 导入示例
const importExamples = async () => {
  if (loading.value) {
    return;
  }
  loading.value = true;
  const [error] = await mock.importDemoMockApi(props.id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('mock.detail.apis.notifications.importDemoSuccess'));
  handleRefresh();
  nextTick(() => {
    scrollToTop();
  });
};

/**
 * Clone a mock API
 * @param id - API ID to clone
 */
const handleClone = async (id: string) => {
  loading.value = true;
  const [error] = await mock.cloneMockApi(id);
  if (error) {
    loading.value = false;
    return;
  }

  handleRefresh();
  notification.success(t('actions.tips.cloneSuccess'));
};

/**
 * Export a mock API
 * @param id - API ID to export
 */
const handleExport = (id: string) => {
  const url = `${TESTER}/mock/service/export?mockApiIds=${id}&mockServiceId=${props.id}`;
  download(url);
};

/**
 * Delete a mock API
 * @param id - API ID to delete
 */
const handleDelete = async (id: string) => {
  const { summary: _summary, isTempFlag } = apiDataMap.value[id];
  modal.confirm({
    centered: true,
    content: t('mock.detail.apis.confirmations.deleteApi', { summary: _summary }),
    async onOk () {
      const checkedId = mockAPIConfig.value?.id;
      const checkedIndex = apiIds.value.findIndex(item => item === checkedId);
      if (!isTempFlag) {
        loading.value = true;
        const [error] = await mock.deleteMockApi({ ids: [id] });
        if (error) {
          loading.value = false;
          return;
        }

        notification.success(t('actions.tips.deleteSuccess'));

        if (typeof scrollRef.value?.delete === 'function') {
          if (apiIds.value.length === 1) {
            scrollRef.value.pureDel(id);
          } else {
            scrollRef.value.del(id);
          }
        }
      } else {
        if (typeof scrollRef.value?.delete === 'function') {
          scrollRef.value.pureDel(id);
        }
      }

      const index = apiIds.value.findIndex(item => item === id);
      if (index > -1) {
        apiIds.value.splice(index, 1);
      }
      delete apiDataMap.value[id];
      delete tempMockApiMap.value[id];
      permissionMap.value.delete(id);
      if (apiIds.value?.length) {
        if (checkedId === id) {
          let selectId: string;
          if (checkedIndex === 0) {
            selectId = apiIds.value[0];
          } else {
            selectId = apiIds.value[checkedIndex] || apiIds.value[checkedIndex - 1];
          }
          selectHandler(selectId);
        } else {
          loading.value = false;
        }
      } else {
        loading.value = false;
        resetMockApi();
        resetMockApiResponse();
      }
    }
  });
};

const refreshInfo = () => {
  loadApiInfo();
  loadApiResponse();
};

const refreshInstance = () => {
  modal.confirm({
    content: t('mock.detail.apis.confirmations.refreshInstance'),
    async onOk () {
      loading.value = true;
      const [error] = await mock.syncApiInstanceConfig(mockAPIId.value!);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success(t('mock.detail.apis.notifications.refreshInstanceSuccess'));
    }
  });
};

const inputChange = debounce(duration.search, (event: any) => {
  const value = event.target.value.trim();
  inputValue.value = value;
});

const create = () => {
  readonly.value = false;
  storeTempMockApiData();
  resetMockApi();
  handleAddMockApi();
  resetMockApiResponse();
  addResponse();
  nextTick(() => {
    scrollToTop();
  });
};

// ---- ScrollList end ----

// ---- CreateForm start ----
const enableChange = (checked: any, id: string) => {
  const key = id + '-3';

  if (checked) {
    openKeys.value.push(key);
    enablePushbackSet.value.add(id);
    return;
  }

  openKeys.value = openKeys.value.filter(item => item !== key);
  enablePushbackSet.value.delete(id);

  // @TODO 禁用后是否清空回推数据？？？
  if (responseMap.value[id]) {
    responseMap.value[id]!.pushback = undefined;
  }
};

const summaryChange = (event: any) => {
  summary.value = event.target.value;
  summaryError.value = false;
};

const descriptionChange = (event: any) => {
  description.value = event.target.value;
};

const isValid = (): boolean => {
  let errorNum = 0;

  if (!summary.value) {
    summaryError.value = true;
    errorNum++;
  }

  if (typeof urlFormRef.value?.isValid === 'function') {
    if (!urlFormRef.value.isValid()) {
      errorNum++;
    }
  }

  return !errorNum;
};

const validateResponse = (): boolean => {
  if (!responseIdList.value?.length) {
    return false;
  }

  priorityErrorSet.value.clear();
  let errorNum = 0;
  const matchRefs = matchFormRefs.value;
  const contentRefs = contentFormRefs.value;
  const pushbackRefs = pushbackFormRefs.value;
  const enableSet = enablePushbackSet.value;
  if (!validateRepeatName()) {
    errorNum++;
  }

  const ids = responseIdList.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];

    if (utils.isEmpty(nameMap.value[id])) {
      nameErrorSet.value.add(id);
      errorNum++;
    }

    if (utils.isEmpty(priorityMap.value[id])) {
      priorityErrorSet.value.add(id);
      errorNum++;
    }

    if (typeof matchRefs[i]?.isValid === 'function') {
      const validFlag = matchRefs[i].isValid();
      if (!validFlag) {
        errorNum++;
      }
    }

    if (typeof contentRefs[i]?.isValid === 'function') {
      const validFlag = contentRefs[i].isValid();
      if (!validFlag) {
        errorNum++;
      }
    }

    // 启用回推
    if (enableSet.has(id)) {
      if (typeof pushbackRefs[i]?.isValid === 'function') {
        const validFlag = pushbackRefs[i].isValid();
        if (!validFlag) {
          errorNum++;
        }
      }
    }
  }

  return !errorNum;
};

const getAPIParams = () => {
  const _params: {
    description: string;
    endpoint: string;
    method: HttpMethod;
    mockServiceId: string;
    summary: string;
    id?: string;
  } = {
    description: description.value,
    endpoint: endpoint.value,
    method: method.value,
    mockServiceId: props.id,
    summary: summary.value
  };

  if (mockAPIId.value) {
    _params.id = mockAPIId.value;
  }

  return [_params];
};

const getResponseParams = (): ResponseConfig[] => {
  const params: ResponseConfig[] = [];
  const ids = responseIdList.value;
  const matchRefs = matchFormRefs.value;
  const contentRefs = contentFormRefs.value;
  const pushbackRefs = pushbackFormRefs.value;
  const enableSet = enablePushbackSet.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    params[i] = {
      name: nameMap.value[id],
      match: {
        priority: priorityMap.value[id],
        body: undefined,
        parameters: undefined,
        path: undefined
      },
      content: undefined,
      pushback: undefined
    };
    if (typeof matchRefs[i]?.getData === 'function') {
      params[i].match = matchRefs[i].getData();
    }

    if (typeof contentRefs[i]?.getData === 'function') {
      params[i].content = contentRefs[i].getData();
    }

    if (enableSet.has(id)) {
      if (typeof pushbackRefs[i]?.getData === 'function') {
        params[i].pushback = pushbackRefs[i].getData();
      }
    }
  }

  return params;
};

const save = async (): Promise<void> => {
  let errorNum = 0;
  if (!isValid()) {
    errorNum++;
  }

  if (!validateResponse()) {
    errorNum++;
  }

  if (errorNum) {
    notification.error(t('mock.detail.apis.notifications.dataError'));
    return;
  }

  const isUpdateFlag = !!isSavedMockApi.value;
  const params = getAPIParams();
  loading.value = true;
  const prevTargetId = mockAPIId.value;
  let targetId: string;
  // 编辑更新接口
  if (isUpdateFlag) {
    const [error] = await mock.updateMockApi(params);
    if (error) {
      loading.value = false;
      return;
    }

    targetId = mockAPIId.value!;
  } else {
    // 添加接口
    const [error, { data }] = await mock.addMockApi(params);
    if (error) {
      loading.value = false;
      return;
    }

    targetId = data[0].id;
  }

  mockAPIId.value = targetId;
  saveResponse(targetId, isUpdateFlag);

  const ids = apiIds.value;
  const { description, endpoint, method, summary } = params[0];
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    if (id === prevTargetId) {
      apiIds.value.splice(i, 1, targetId);
      delete apiDataMap.value[prevTargetId];
      const newData: MockAPIConfig = {
        ...apiDataMap.value[prevTargetId],
        isTempFlag: false,
        id: targetId,
        description,
        endpoint,
        method,
        summary
      };
      apiDataMap.value[targetId] = newData;
      mockAPIConfig.value = newData;
      permissionMap.value.set(targetId, [MockServicePermission.DELETE, MockServicePermission.EXPORT]);
      if (!isUpdateFlag) {
        if (typeof scrollRef.value?.pureDel === 'function') {
          scrollRef.value.pureDel(prevTargetId);
        }

        if (typeof scrollRef.value?.pureAdd === 'function') {
          scrollRef.value.pureAdd(newData);
        }
      }

      // 删除缓存
      delete tempMockApiMap.value[prevTargetId];
      break;
    }
  }
};

const saveResponse = async (id: string, isUpdateFlag: boolean) => {
  const params: ResponseConfig[] = getResponseParams();
  const [error] = await (isUpdateFlag ? mock.updateMockApiResponse(id, params) : mock.addMockApiResponse(id, params));
  if (error) {
    loading.value = false;
    return;
  }

  notification.success(t('actions.tips.saveSuccess'));
  loading.value = false;
};

const goback = () => {
  router.push('/apis#mock');
};

const deleteResponse = (index: number, id: string) => {
  responseIdList.value.splice(index, 1);
  delete priorityMap.value[id];
  delete nameMap.value[id];
  delete responseMap.value[id];
  priorityErrorSet.value.delete(id);
  nameErrorSet.value.delete(id);
  delete nameErrorMessage.value[id];
  const _keys = [id + '-1', id + '-2', id + '-3'];
  openKeys.value = openKeys.value.filter(item => !_keys.includes(item));
  // 如果响应为空，自动添加一条空响应
  if (!responseIdList.value.length) {
    addResponse();
  }
};

const nameChange = (event: { target: { value: string; } }, id: string) => {
  const value = event.target.value;
  nameMap.value[id] = value;
  if (!value) {
    nameErrorSet.value.add(id);
    return;
  }

  nameErrorSet.value.delete(id);
  delete nameErrorMessage.value[id];
  validateRepeatName(id);
};

const validateRepeatName = (id?: string): boolean => {
  const tempSet = new Set();
  const repeatNameSet = Object.values(nameMap.value).reduce((prev, cur) => {
    if (tempSet.has(cur)) {
      prev.add(cur);
    }
    tempSet.add(cur);
    return prev;
  }, new Set());

  let errorNum = 0;
  const data = nameMap.value;
  for (const key in data) {
    if (repeatNameSet.has(data[key])) {
      if (id) {
        nameErrorSet.value.add(id);
        nameErrorMessage.value[id] = t('mock.detail.apis.notifications.nameDuplicate');
      } else {
        nameErrorSet.value.add(key);
        nameErrorMessage.value[key] = t('mock.detail.apis.notifications.nameDuplicate');
      }

      errorNum++;
    } else {
      nameErrorMessage.value[key] = undefined;
      nameErrorSet.value.delete(key);
    }
  }

  return !errorNum;
};

const collapseChange = (keys: any) => {
  openKeys.value = keys;
};

const arrowChange = (key: string) => {
  if (!openKeys.value.includes(key)) {
    openKeys.value.push(key);
    return;
  }

  openKeys.value = openKeys.value.filter(_key => _key !== key);
};

const priorityChange = (event: { target: { value: string } }, id: string) => {
  priorityMap.value[id] = event.target.value;

  priorityErrorSet.value.delete(id);
};

const priorityBlur = (event: { target: { value: string } }, id: string) => {
  const value = event.target.value.trim();
  priorityMap.value[id] = value;
  if (utils.isEmpty(value)) {
    priorityErrorSet.value.add(id);
    return;
  }

  priorityErrorSet.value.delete(id);
};

const loadApiInfo = async () => {
  const id = mockAPIId.value;
  if (!id) {
    return;
  }

  loading.value = true;
  const [error, { data }] = await mock.getMockApiDetail(id);
  if (error) {
    loading.value = false;
    return;
  }

  apiDataMap.value[id] = {
    ...data,
    method: data.method.value
  };
  setApiForm(apiDataMap.value[id]);
};

const loadApiResponse = async () => {
  const id = mockAPIId.value;
  if (!id) {
    loading.value = false;
    return;
  }

  loading.value = true;
  // 修复响应数据量大渲染慢而loading不显示问题
  setTimeout(async () => {
    const [error, { data }]: [Error | null, { data: ResponseInfo[] }] = await mock.getMockApiResponse(id);
    if (!error) {
      resetMockApiResponse();
      if (!data?.length) {
        addResponse();
      } else {
        setResponseData(data);
      }
    }
    nextTick(() => {
      loading.value = false;
      scrollToTop();
    });
  }, 16.66);
};

const scrollToTop = () => {
  const dom = document.getElementById(domId);
  if (dom) {
    dom.scrollTop = 0;
  }
};

const addResponse = () => {
  const id = utils.uuid();
  responseIdList.value.push(id);
  nameMap.value[id] = undefined;
  openKeys.value.push(...[id + '-1', id + '-2']);
  priorityMap.value[id] = '1000';
  responseMap.value[id] = {
    name: '',
    pushback: undefined,
    match: {
      body: undefined,
      parameters: undefined,
      path: undefined,
      priority: '1000'
    },
    content: {
      content: '',
      headers: [],
      delay: { mode: ResponseDelayMode.NONE },
      status: '200',
      contentEncoding: undefined
    }
  };
};

const setResponseData = (data: ResponseInfo[]) => {
  for (let i = 0, len = data.length; i < len; i++) {
    const _data = data[i];
    const { id, match, pushback, name, content } = _data;
    const tempData: ResponseConfig = {
      content: undefined,
      match: undefined,
      name: '',
      pushback: undefined
    };

    if (match) {
      const { body, parameters, path, priority } = match;
      tempData.match = {
        priority,
        body: undefined,
        parameters: undefined,
        path: undefined
      };

      if (body) {
        tempData.match.body = {
          ...body,
          condition: body.condition.value
        };
      }

      if (path) {
        tempData.match.path = {
          ...path,
          condition: path.condition.value
        };
      }

      if (parameters) {
        tempData.match.parameters = parameters.map(item => {
          return {
            ...item,
            condition: item.condition.value
          };
        });
      }
    }

    if (content) {
      const { content: _content, contentEncoding, delay, headers, status } = content;
      tempData.content = {
        content: _content,
        contentEncoding,
        delay: undefined,
        headers,
        status
      };

      if (delay) {
        tempData.content.delay = { ...delay, mode: delay.mode.value };
      }
    }

    if (pushback) {
      const { autoPush, body, delay, method, parameters, url } = pushback;
      tempData.pushback = {
        autoPush,
        body,
        delay: undefined,
        method: method.value || HttpMethod.GET,
        parameters,
        url
      };

      if (delay) {
        tempData.pushback.delay = { ...delay, mode: delay.mode.value };
      }
    }

    responseMap.value[id] = tempData;
    responseIdList.value.push(id);
    priorityMap.value[id] = match?.priority as string;
    nameMap.value[id] = name;
    openKeys.value.push(id + '-1');
    openKeys.value.push(id + '-2');
    if (!utils.isEmpty(pushback)) {
      openKeys.value.push(id + '-3');
      enablePushbackSet.value.add(id);
    }
  }
};

// ---- CreateForm end ----
const loadServiceInfo = async () => {
  const [error, res] = await mock.getServiceDetail(props.id);
  if (error) {
    return;
  }

  if (!res?.data) {
    return;
  }

  const { serviceDomainUrl, serviceHostUrl } = res.data;
  const options: { label: string; value: string; }[] = [];
  if (serviceDomainUrl) {
    options.push({ label: serviceDomainUrl, value: serviceDomainUrl });
  }

  if (serviceHostUrl) {
    options.push({ label: serviceHostUrl, value: serviceHostUrl });
  }

  serviceUrlOptions.value = options;
};

const readyState = ref(-1);
const loadProxyUrl = async () => {
  const [error, resp] = await setting.getUserApiProxy();
  if (error) {
    return;
  }
  const { data } = resp;
  proxyOptObj.value = data;
  Object.keys(data).forEach(key => {
    if (data[key].enabled) {
      currentProxyUrl.value = data[key].url;
      currentProxy.value = data[key].name.value;
    }
  });
};

const resetMockApi = () => {
  mockAPIConfig.value = undefined;
  mockAPIId.value = '';
  readonly.value = false;
  summary.value = '';
  summaryError.value = false;
  method.value = HttpMethod.GET;
  endpoint.value = '';
  description.value = '';
};

const resetMockApiResponse = () => {
  responseIdList.value = [];
  responseMap.value = {};
  nameMap.value = {};
  nameErrorSet.value.clear();
  nameErrorMessage.value = {};
  openKeys.value = [];
  priorityMap.value = {};
  priorityErrorSet.value.clear();
  enablePushbackSet.value.clear();

  responseNotify.value++;
};

const updateWs = () => {
  if (navigator.onLine) {
    if (WS.value?.close) {
      WS.value?.close(1000);
    }
    WS.value = undefined;
    if (currentProxyUrl.value) {
      createWS();
    } else if (currentProxy.value === 'NO_PROXY') {
      readyState.value = -1;
    }
  } else {
    WS.value = undefined;
    if (currentProxyUrl.value) {
      readyState.value = -1;
    }
  }
};

const createWS = () => {
  WS.value = new ReconnectingWebSocket(currentProxyUrl.value, [],
    {
      maxRetries: 3,
      maxReconnectionDelay: 30000, // max delay in ms between reconnections
      minReconnectionDelay: 30000,
      connectionTimeout: 60000
    });
  WS.value.addEventListener('open', () => {
    readyState.value = WS.value.readyState || 1;
  });
  readyState.value = WS.value.readyState;
  WS.value.addEventListener('message', (event) => {
    try {
      const response = JSON.parse(event.data);
      if (response?.requestId) {
        uuid.value = response?.requestId;
      } else {
        uuid.value = '';
      }
      wsResponse.value = event.data;
    } catch (error) { }
  });
  WS.value.addEventListener('error', () => {
    readyState.value = WS.value?.readyState || 3;
  });
  WS.value.addEventListener('close', () => {
    readyState.value = WS.value?.readyState || 3;
  });
};

onMounted(() => {
  loadServiceInfo();
  loadProxyUrl();

  watch([() => currentProxyUrl.value, () => currentProxy.value], () => {
    WS.value && WS.value.close(1000);
    WS.value = undefined;
    if (currentProxy.value === 'NO_PROXY') {
      readyState.value = -1;
    } else if (currentProxyUrl.value) {
      createWS();
    }
  }, { immediate: true });

  if ((navigator as any).connection) {
    (navigator as any).connection.addEventListener('change', updateWs);
  }
});

onBeforeUnmount(() => {
  WS.value && WS.value.close(1000);
  if ((navigator as any).connection) {
    (navigator as any).connection.removeEventListener('change', updateWs);
  }
});

const trigger = ['contextmenu'] as ('contextmenu' | 'click' | 'hover')[];
const menuItems = ref([
  {
    key: 'clone',
    icon: 'icon-fuzhizujian2',
    name: t('actions.clone'),
    permission: MockServicePermission.ADD
  },
  {
    key: 'delete',
    icon: 'icon-fz',
    name: t('actions.delete'),
    permission: MockServicePermission.DELETE
  },
  {
    key: 'export',
    icon: 'icon-daochu1',
    name: t('actions.export'),
    permission: MockServicePermission.EXPORT
  }
]);
const dropdownMenuItems = ref([
  {
    key: 'copyApi',
    icon: 'icon-fuzhizujian2',
    name: t('mock.detail.apis.menuItems.copyApi'),
    noAuth: true
  },
  {
    key: 'linkApi',
    icon: 'icon-yinyong',
    name: t('mock.detail.apis.menuItems.linkApi'),
    noAuth: true
  },
  {
    key: 'import',
    icon: 'icon-daoru',
    name: t('actions.import'),
    noAuth: true
  },
  {
    key: 'importExamples',
    icon: 'icon-daoru',
    name: t('actions.importExamples'),
    noAuth: true
  }
]);

const sortMenuItems = [{
  name: t('mock.detail.apis.sortOptions.byCreatedDate'),
  key: 'createdDate',
  orderSort: 'DESC' as any
}, {
  name: t('mock.detail.apis.sortOptions.byId'),
  key: 'id',
  orderSort: 'ASC' as any
}];

provide('WS', WS);
provide('uuid', uuid);
provide('wsResponse', wsResponse);
provide('readyState', readyState);
provide('currentProxyUrl', currentProxyUrl);
provide('currentProxy', currentProxy);
provide('proxyOptObj', proxyOptObj);
</script>
<template>
  <Hints class="mb-1.5" :text="t('mock.detail.apis.hints')" />
  <PureCard style="height: calc(100% - 24px);" class="flex-1 flex flex-nowrap pl-3.5 py-3.5 relative">
    <Spin
      :delay="0"
      :spinning="loading"
      class="flex flex-col w-80 h-full">
      <div class="flex flex-nowrap space-x-2 mb-2">
        <Input
          :placeholder="t('mock.detail.apis.searchPlaceholder')"
          class="flex-1"
          :allowClear="true"
          :value="inputValue"
          @change="inputChange">
          <template #suffix>
            <Icon icon="icon-sousuo" class="text-3.5 text-theme-sub-content" />
          </template>
        </Input>

        <Button
          class="flex-shrink-0 flex items-center"
          type="primary"
          size="small"
          style="padding-right: 0;"
          @click="create">
          <span>{{ t('mock.detail.apis.addApi') }}</span>
          <Dropdown :menuItems="dropdownMenuItems" @click="buttonDropdownClick">
            <span class="inline-block w-5 h-5">
              <Icon class="text-white" icon="icon-more" />
            </span>
          </Dropdown>
        </Button>

        <DropdownSort
          v-model:orderBy="orderBy"
          v-model:orderSort="orderSort"
          class="flex-shrink-0"
          :menuItems="sortMenuItems"
          @click="handleDropdownSortChange" />

        <div
          class="flex-shrink-0 flex-grow-0 leading-7 text-3.75 cursor-pointer text-theme-text-hover flex items-center"
          @click="handleRefresh">
          <IconRefresh />
        </div>
      </div>

      <Scroll
        ref="scrollRef"
        v-model:spinning="loading"
        :action="action"
        :lineHeight="66"
        :params="params"
        :notify="notify"
        :transition="false"
        :delay="0"
        class="flex-1"
        @change="handleScrollChange">
        <div>
          <Dropdown
            v-for="item in apiIds"
            :key="item"
            :trigger="trigger"
            :menuItems="menuItems"
            :permissions="permissionMap.get(item)"
            @click="handleDropdownClick($event, item)">
            <div class="list-item-container">
              <div
                :key="item"
                :class="{ 'active-item': mockAPIId === item }"
                class="relative cursor-pointer rounded px-2 pt-2 pb-3 space-y-2 hover:bg-bg-hover text-theme-text-hover"
                @click="handleSelect(item)">
                <div :title="apiDataMap[item].summary" class="truncate font-bold">
                  {{ apiDataMap[item].summary }}
                </div>
                <div class="flex items-center space-x-3">
                  <HttpMethodText style="min-width: auto;" :value="apiDataMap[item].method" />
                  <div :title="apiDataMap[item].endpoint" class="text-text-content truncate">
                    {{ apiDataMap[item].endpoint }}
                  </div>
                </div>
              </div>

              <div class="list-item-divider"></div>
            </div>
          </Dropdown>
        </div>
      </Scroll>
    </Spin>
    <div class="w-0 h-full mx-3.5 border-r border-solid border-theme-text-box"></div>
    <Spin
      :delay="0"
      :spinning="loading"
      class="flex-1 flex flex-col h-full text-3 leading-5 space-y-4 mr-10 text-text-content">
      <div class="flex justify-end items-center w-full space-x-2 pr-5.25">
        <Button
          type="primary"
          size="small"
          @click="save">
          <div class="flex items-center space-x-1">
            <Icon icon="icon-baocun" />
            <span>{{ t('actions.save') }}</span>
          </div>
        </Button>
        <Button
          v-if="isSavedMockApi"
          type="default"
          size="small"
          @click="handleClone(mockAPIId!)">
          <div class="flex items-center space-x-1">
            <Icon icon="icon-fuzhizujian2" />
            <span>{{ t('actions.clone') }}</span>
          </div>
        </Button>
        <Button
          v-if="isSavedMockApi"
          type="default"
          size="small"
          @click="handleExport(mockAPIId!)">
          <div class="flex items-center space-x-1">
            <Icon icon="icon-fuzhizujian2" />
            <span>{{ t('actions.export') }}</span>
          </div>
        </Button>
        <Button
          v-if="isSavedMockApi"
          type="default"
          size="small"
          :danger="false"
          @click="handleDelete(mockAPIId!)">
          <div class="flex items-center space-x-1">
            <Icon icon="icon-qingchu" />
            <span>{{ t('actions.delete') }}</span>
          </div>
        </Button>
        <Button
          v-if="isSavedMockApi"
          type="default"
          size="small"
          :danger="false"
          @click="refreshInstance">
          <div class="flex items-center space-x-1">
            <Icon icon="icon-peizhifuwutongbu" />
            <span>{{ t('mock.detail.apis.refreshInstance') }}</span>
          </div>
        </Button>
        <Button
          v-if="isSavedMockApi"
          type="default"
          size="small"
          :danger="false"
          @click="refreshInfo">
          <div class="flex items-center space-x-1">
            <IconRefresh />
            <span>{{ t('actions.refresh') }}</span>
          </div>
        </Button>
        <Button
          type="default"
          size="small"
          @click="goback">
          <div class="flex items-center space-x-1">
            <Icon icon="icon-fanhui" />
            <span>{{ t('actions.back') }}</span>
          </div>
        </Button>
      </div>
      <div :id="domId" class="w-full flex-1 overflow-auto space-y-6 scroll-smooth">
        <div class="space-y-2">
          <div class="flex items-center text-3.5 space-x-1 text-text-title">
            <div>{{ t('mock.detail.apis.request') }}</div>
            <div v-if="isSavedMockApi" class="flex items-center text-3 text-theme-sub-content">
              <span class="mx-0.5">(</span>
              <div class="flex items-center translate-y-0.25">
                <span class="mr-1">ID
                  <Colon />
                </span>
                <div class="mr-1">{{ mockAPIId }}</div>
                <IconCopy :copyText="mockAPIId" />
              </div>
              <span class="mx-0.5">)</span>
            </div>
          </div>

          <div class="space-y-4">
            <div class="pr-5.25">
              <div class="flex items-center justify-between">
                <div class="flex items-center mb-0.5">
                  <IconRequired />
                  <span>{{ t('mock.detail.apis.name') }}</span>
                </div>
              </div>
              <Input
                :maxlength="400"
                :value="summary"
                :error="summaryError"
                :placeholder="t('mock.detail.apis.namePlaceholder')"
                trim
                @change="summaryChange" />
            </div>

            <UrlForm
              ref="urlFormRef"
              v-model:method="method"
              v-model:endpoint="endpoint"
              class="mt-4"
              :readonly="readonly"
              :options="serviceUrlOptions" />

            <div class="pr-5.25">
              <div class="mb-0.5">{{ t('common.description') }}</div>
              <Input
                :maxlength="20000"
                :value="description"
                :autoSize="{ minRows: 5, maxRows: 5 }"
                showCount
                type="textarea"
                :placeholder="t('mock.detail.apis.descriptionPlaceholder')"
                trim
                @change="descriptionChange" />
            </div>
          </div>
        </div>
        <div class="pr-5.25 space-y-2">
          <div class="flex items-center justify-between leading-5 pr-3.25 text-3.5 text-text-title">
            <div>
              <span>{{ t('mock.detail.apis.response') }}</span>
              <Tooltip>
                <template #title>{{ t('mock.detail.apis.maxResponseTip') }}</template>
                <Icon icon="icon-shuoming" class="text-tips cursor-pointer text-3.5 ml-1" />
              </Tooltip>
            </div>
            <Button
              size="small"
              type="primary"
              @click="addResponse">
              <div class="flex items-center space-x-1">
                <Icon icon="icon-jia" />
                <span>{{ t('mock.detail.apis.addResponse') }}</span>
              </div>
            </Button>
          </div>

          <div class="space-y-4">
            <div
              v-for="(item, index) in responseIdList"
              :key="item"
              class="px-3 rounded border border-solid border-theme-text-box">
              <Collapse
                :activeKey="openKeys"
                :bordered="false"
                style="background-color: #fff;font-size: 12px;"
                @change="collapseChange">
                <CollapsePanel
                  collapsible="disabled"
                  :showArrow="false"
                  style="border-top: none;border-right: none;border-left: none;line-height: 20px;">
                  <template #header>
                    <div class="flex items-center flex-1 space-x-2">
                      <div class="flex items-center flex-shrink-0 flex-nowrap whitespace-nowrap mb-0.5">
                        <IconRequired />
                        <span>{{ t('mock.detail.apis.name') }}</span>
                      </div>

                      <Validate
                        class="flex-1"
                        :text="nameErrorMessage[item]"
                        :fixed="true">
                        <Input
                          :maxlength="200"
                          :value="nameMap[item]"
                          :error="nameErrorSet.has(item)"
                          :placeholder="t('mock.detail.apis.namePlaceholder')"
                          trim
                          @change="nameChange($event, item)" />
                      </Validate>
                    </div>
                  </template>

                  <template #extra>
                    <Button
                      type="primary"
                      size="small"
                      class="ml-5"
                      danger
                      @click="deleteResponse(index, item)">
                      <div class="flex items-center space-x-1">
                        <Icon icon="icon-qingchu" />
                        <span>{{ t('mock.detail.apis.deleteResponse') }}</span>
                      </div>
                    </Button>
                  </template>
                </CollapsePanel>

                <CollapsePanel
                  :key="item + '-1'"
                  collapsible="disabled"
                  :showArrow="false"
                  style="border-top: none;border-right: none;border-left: none;line-height: 20px;">
                  <template #header>
                    <div class="flex items-center py-3">
                      <Arrow :open="openKeys.includes(item + '-1')" @change="arrowChange(item + '-1')" />
                      <div class="flex items-center ml-1">
                        <span>{{ t('mock.detail.apis.match') }}</span>
                        <Tooltip>
                          <template #title>
                            {{ t('mock.detail.apis.matchTooltip') }}
                          </template>
                          <Icon icon="icon-shuoming" class="text-tips cursor-pointer text-3.5 ml-1" />
                        </Tooltip>
                      </div>
                    </div>
                  </template>

                  <div class="flex items-center leading-5 mb-5 space-x-2">
                    <div class="flex items-center">
                      <IconRequired />
                      <span>{{ t('mock.detail.apis.priority') }}</span>
                      <Tooltip>
                        <template #title>{{ t('mock.detail.apis.priorityTooltip') }}</template>
                        <Icon icon="icon-shuoming" class="text-tips cursor-pointer text-3.5 ml-0.75" />
                      </Tooltip>
                    </div>
                    <Input
                      :value="priorityMap[item]"
                      :max="2147483647"
                      :min="0"
                      :maxlength="10"
                      :error="priorityErrorSet.has(item)"
                      dataType="number"
                      trimAll
                      placeholder="0~2147483647"
                      class="w-30"
                      @blur="priorityBlur($event, item)"
                      @change="priorityChange($event, item)" />
                  </div>

                  <MatchForm
                    :ref="el => { matchFormRefs[index] = el }"
                    :value="responseMap[item]?.match"
                    :notify="responseNotify" />
                </CollapsePanel>

                <CollapsePanel
                  :key="item + '-2'"
                  collapsible="disabled"
                  :showArrow="false"
                  style="border-top: none;border-right: none;border-left: none;">
                  <template #header>
                    <div class="flex items-center leading-5 py-3">
                      <Arrow :open="openKeys.includes(item + '-2')" @change="arrowChange(item + '-2')" />
                      <div class="flex items-center ml-1">
                        <IconRequired />
                        <span>{{ t('mock.detail.apis.content') }}</span>
                      </div>
                    </div>
                  </template>

                  <ContentForm
                    :ref="el => { contentFormRefs[index] = el }"
                    :value="responseMap[item]?.content"
                    :notify="responseNotify" />
                </CollapsePanel>

                <CollapsePanel
                  :key="item + '-3'"
                  :showArrow="false"
                  collapsible="disabled"
                  style="border: none;">
                  <template #header>
                    <div class="flex items-center leading-5 py-3">
                      <Arrow
                        v-if="enablePushbackSet.has(item)"
                        :open="openKeys.includes(item + '-3')"
                        @change="arrowChange(item + '-3')" />
                      <div class="flex items-center ml-1">
                        <span>{{ t('mock.detail.apis.pushback') }}</span>
                        <Tooltip>
                          <template #title>{{ t('mock.detail.apis.pushbackTooltip') }}</template>
                          <Icon icon="icon-shuoming" class="text-tips cursor-pointer text-3.5 ml-1" />
                        </Tooltip>
                      </div>

                      <Switch
                        :checked="enablePushbackSet.has(item)"
                        size="small"
                        class="ml-1.5"
                        :checkedChildren="t('mock.detail.apis.enable')"
                        :unCheckedChildren="t('mock.detail.apis.disable')"
                        @change="enableChange($event, item)" />
                    </div>
                  </template>

                  <PushBack
                    :ref="el => { pushbackFormRefs[index] = el }"
                    :value="responseMap[item]?.pushback"
                    :notify="responseNotify" />
                </CollapsePanel>
              </Collapse>
            </div>
          </div>
        </div>
      </div>
    </Spin>
    <DebugApis :serviceOptions="serviceUrlOptions" :mockAPIInfo="mockAPIConfig" />
  </PureCard>
  <AsyncComponent :visible="apiCopyModalVisible">
    <SelectApiModal
      v-model:visible="apiCopyModalVisible"
      v-model:confirmLoading="apiCopyConfirmLoading"
      :title="t('mock.detail.apis.copyApiTitle')"
      type="copy"
      @ok="copyApiOk" />
  </AsyncComponent>
  <AsyncComponent :visible="apiLinkModalVisible">
    <SelectApiModal
      v-model:visible="apiLinkModalVisible"
      v-model:confirmLoading="apiLinkConfirmLoading"
      :title="t('mock.detail.apis.linkApiTitle')"
      type="link"
      @ok="linkApiOk" />
  </AsyncComponent>
  <AsyncComponent :visible="importApiModalVisible">
    <ImportApiModal
      :id="props.id"
      v-model:visible="importApiModalVisible"
      @ok="importApiOk" />
  </AsyncComponent>
</template>

<style scoped>
.active-item {
  background-color: rgba(239, 240, 243, 100%);
}

:deep(.ant-ribbon.ant-ribbon-placement-end) {
  right: 6px;
}

.list-item-divider {
  display: block;
  height: 1px;
  background-color: var(--border-divider);
}

.list-item-container:last-child .list-item-divider {
  display: none;
}

.error.ant-input-affix-wrapper {
  border-width: 1px;
  border-style: solid;
}

.error-border {
  border-color: rgba(245, 34, 45, 100%);
}

.ant-collapse> :deep(.ant-collapse-item) {
  border-style: dashed;
}

.ant-collapse> :deep(.ant-collapse-item):first-child {
  border: none;
}

.ant-collapse> :deep(.ant-collapse-item)>.ant-collapse-header {
  display: flex;
  position: relative;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
}

.ant-collapse> :deep(.ant-collapse-item):first-child>.ant-collapse-header {
  padding-top: 20px;
  padding-bottom: 0;
}

.ant-collapse :deep(.ant-collapse-content)>.ant-collapse-content-box {
  padding: 0 20px 30px;
  line-height: 20px;
}

:deep(.ant-collapse-header-text) {
  flex: 1;
}
</style>
