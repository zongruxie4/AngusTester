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
import { utils, TESTER, duration, download } from '@xcan-angus/infra';
import ReconnectingWebSocket from 'reconnecting-websocket';
import { Button, Collapse, CollapsePanel, Switch } from 'ant-design-vue';
import { debounce } from 'throttle-debounce';

// Import composables
import { useMockApiData } from './composables/useMockApiData';
import { useMockApiForm } from './composables/useMockApiForm';
import { useMockApiResponse } from './composables/useMockApiResponse';
import { useWebSocketConnection } from './composables/useWebSocketConnection';
import { useMockApiOperations } from './composables/useMockApiOperations';
import { useMockServiceInfo } from './composables/useMockServiceInfo';
import { useMockApiUI } from './composables/useMockApiUI';
import { useMockApiMenus } from './composables/useMockApiMenus';

import { mock } from '@/api/tester';
import { setting } from '@/api/gm';
import { HttpMethod, MockAPIConfig, MockAPIInfo, ResponseConfig, ResponseInfo } from './types';

const { t } = useI18n();

interface Props {
  id: string;// mock service Id
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

// Initialize composables
const {
  apiIds,
  apiDataMap,
  permissionMap,
  inputValue,
  orderBy,
  orderSort,
  getParams,
  addMockApi,
  updateApiList,
  removeApi,
  updateSearchValue,
  updateSorting
} = useMockApiData();

// Computed params for API requests
const params = computed(() => getParams(props.id));

const {
  mockAPIConfig,
  mockAPIId,
  readonly,
  summary,
  summaryError,
  method,
  endpoint,
  description,
  tempMockApiMap,
  isSavedMockApi,
  setApiForm,
  storeTempMockApiData,
  restoreTempMockApiData,
  resetMockApi,
  isValid,
  getAPIParams,
  summaryChange,
  descriptionChange
} = useMockApiForm();

const {
  responseIdList,
  responseMap,
  nameMap,
  nameErrorSet,
  nameErrorMessage,
  openKeys,
  priorityMap,
  priorityErrorSet,
  enablePushbackSet,
  responseNotify,
  addResponse,
  deleteResponse,
  resetMockApiResponse,
  validateRepeatName,
  validateResponse,
  getResponseParams,
  nameChange,
  priorityChange,
  priorityBlur,
  enableChange,
  collapseChange,
  arrowChange
} = useMockApiResponse();

const {
  WS,
  uuid,
  wsResponse,
  readyState,
  currentProxy,
  currentProxyUrl,
  proxyOptObj,
  loadProxyUrl,
  createWS,
  updateWs,
  closeWebSocket
} = useWebSocketConnection();

const {
  loading,
  createMockApi,
  updateMockApi,
  deleteMockApi,
  cloneMockApi,
  copyApiToMock,
  associateApiToMock,
  importDemoMockApi,
  getMockApiDetail,
  getMockApiResponse,
  addMockApiResponse,
  updateMockApiResponse,
  syncApiInstanceConfig
} = useMockApiOperations();

const {
  serviceUrlOptions,
  loadServiceInfo
} = useMockServiceInfo();

const {
  apiCopyModalVisible,
  apiLinkModalVisible,
  importApiModalVisible,
  apiCopyConfirmLoading,
  apiLinkConfirmLoading,
  goback,
  showDeleteConfirmation,
  showRefreshInstanceConfirmation,
  exportMockApi,
  showErrorNotification,
  showSuccessNotification
} = useMockApiUI();

const {
  trigger,
  menuItems,
  dropdownMenuItems,
  sortMenuItems
} = useMockApiMenus();

// Component imports
const DebugApis = defineAsyncComponent(() => import('./DebugApis.vue'));
const SelectApiModal = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/selectApiModal/index.vue'));
const MatchForm = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/matchForm/index.vue'));
const ContentForm = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/contentForm/index.vue'));
const PushBack = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/contentForm/pushBack.vue'));
const UrlForm = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/urlForm/index.vue'));
const ImportApiModal = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/importApiModal/index.vue'));

// Component references
const scrollRef = ref();
const urlFormRef = ref();
const matchFormRefs = ref<any[]>([]);
const contentFormRefs = ref<any[]>([]);
const pushbackFormRefs = ref<any[]>([]);

// UI state
const domId = utils.uuid();
const notify = ref(0);
const action = `${TESTER}/mock/apis`;

/**
 * Add a new temporary Mock API to the list
 */
const addMockApiHandler = () => {
  if (typeof scrollRef.value?.pureAdd === 'function') {
    const id = addMockApi(props.id);
    scrollRef.value.pureAdd(apiDataMap.value[id]);
    setApiForm(apiDataMap.value[id]);
  }
};

/**
 * Handle scroll list data change
 * @param dataList - Array of API info from server
 */
const scrollChange = (dataList: MockAPIInfo[]) => {
  updateApiList(dataList);

  if (!dataList.length) {
    mockAPIId.value = undefined;
    mockAPIConfig.value = undefined;
    resetMockApi();
    resetMockApiResponse();
    addMockApiHandler();
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
 * Select API by ID with temporary data storage
 * @param id - API ID to select
 */
const select = (id: string) => {
  if (id === mockAPIId.value) {
    return;
  }

  storeTempMockApiData({
    responseIdList: responseIdList.value,
    responseMap: responseMap.value,
    nameMap: nameMap.value,
    openKeys: openKeys.value,
    priorityMap: priorityMap.value,
    enablePushbackSet: enablePushbackSet.value
  });
  selectHandler(id);
};

/**
 * Handle API selection with data restoration
 * @param id - API ID to select
 */
const selectHandler = (id: string) => {
  const restoredData = restoreTempMockApiData(id);
  if (restoredData) {
    const { response } = restoredData;
    enablePushbackSet.value = response.enablePushbackSet;
    nameMap.value = response.nameMap;
    openKeys.value = response.openKeys;
    priorityMap.value = response.priorityMap;
    responseIdList.value = response.responseIdList;
    responseMap.value = response.responseMap;

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

/**
 * Handle dropdown sort change
 * @param data - Sort configuration data
 */
const dropdownSortChange = (data: { orderBy: 'createdDate' | 'id', orderSort: 'ASC' | 'DESC' }) => {
  updateSorting(data.orderBy, data.orderSort);
};

/**
 * Trigger refresh of API list
 */
const toRefresh = () => {
  notify.value++;
  loading.value = true;
};

/**
 * Handle dropdown menu click for API operations
 * @param param - Menu click event data
 * @param id - API ID for the operation
 */
const dropdownClick = async ({ key }: { key: 'clone' | 'delete' | 'export' }, id: string) => {
  if (key === 'clone') {
    const { error } = await cloneMockApi(id);
    if (!error) {
      toRefresh();
    }
    return;
  }

  if (key === 'delete') {
    const { summary: apiName } = apiDataMap.value[id];
    showDeleteConfirmation(apiName, async () => {
      await handleDelete(id);
    });
    return;
  }

  if (key === 'export') {
    exportMockApi(id, props.id);
  }
};

/**
 * Handle button dropdown click for main actions
 * @param param - Menu click event data
 */
const buttonDropdownClick = ({ key }: { key: 'copyApi' | 'linkApi' | 'import' | 'importDemo' }) => {
  if (key === 'copyApi') {
    apiCopyModalVisible.value = true;
    return;
  }

  if (key === 'linkApi') {
    apiLinkModalVisible.value = true;
    return;
  }

  if (key === 'import') {
    importApiModalVisible.value = true;
    return;
  }

  if (key === 'importDemo') {
    handleImportDemo();
  }
};

/**
 * Handle copy API confirmation
 * @param param - API selection data
 */
const copyApiOk = async ({ id }: { id: string }) => {
  apiCopyConfirmLoading.value = true;
  const { error, data } = await copyApiToMock(props.id, id);
  apiCopyConfirmLoading.value = false;

  if (error) {
    return;
  }

  apiCopyModalVisible.value = false;
  mockAPIId.value = data.id;
  mockAPIConfig.value = undefined;
  toRefresh();
  nextTick(() => {
    scrollToTop();
  });
};

/**
 * Handle link API confirmation
 * @param param - API selection data
 */
const linkApiOk = async ({ id }: { id: string }) => {
  apiLinkConfirmLoading.value = true;
  const { error, data } = await associateApiToMock(props.id, id);
  apiLinkConfirmLoading.value = false;

  if (error) {
    return;
  }

  apiLinkModalVisible.value = false;
  mockAPIId.value = data.id;
  mockAPIConfig.value = undefined;
  notification.success(t('mock.mockApis.notifications.linkApiSuccess'));
  toRefresh();
  nextTick(() => {
    scrollToTop();
  });
};

/**
 * Handle import API completion
 */
const importApiOk = () => {
  toRefresh();
  nextTick(() => {
    scrollToTop();
  });
};

/**
 * Handle import demo APIs
 */
const handleImportDemo = async () => {
  if (loading.value) {
    return;
  }

  const { error } = await importDemoMockApi(props.id);
  if (!error) {
    toRefresh();
    nextTick(() => {
      scrollToTop();
    });
  }
};

const clone = async (id: string) => {
  loading.value = true;
  const [error] = await mock.cloneMockApi(id);
  if (error) {
    loading.value = false;
    return;
  }

  toRefresh();
  notification.success(t('mock.mockApis.notifications.cloneSuccess'));
};

const toExport = (id: string) => {
  const url = `${TESTER}/mock/service/export?mockApiIds=${id}&mockServiceId=${props.id}`;
  download(url);
};

const del = async (id: string) => {
  const { summary: _summary, isTempFlag } = apiDataMap.value[id];
  modal.confirm({
    centered: true,
    content: t('mock.mockApis.confirmations.deleteApi', { name: _summary }),
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

        notification.success(t('mock.mockApis.notifications.deleteSuccess'));

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

/**
 * Refresh API information and response data
 */
const refreshInfo = () => {
  loadApiInfo();
  loadApiResponse();
};

/**
 * Refresh API instance configuration with confirmation
 */
const refreshInstance = () => {
  showRefreshInstanceConfirmation(async () => {
    const { error } = await syncApiInstanceConfig(mockAPIId.value!);
    if (!error) {
      // Success notification is handled in the composable
    }
  });
};

/**
 * Handle input change with debounce
 * @param event - Input change event
 */
const inputChange = debounce(duration.search, (event: { target: { value: string } }) => {
  updateSearchValue(event.target.value);
});

/**
 * Create new Mock API
 */
const create = () => {
  readonly.value = false;
  storeTempMockApiData({
    responseIdList: responseIdList.value,
    responseMap: responseMap.value,
    nameMap: nameMap.value,
    openKeys: openKeys.value,
    priorityMap: priorityMap.value,
    enablePushbackSet: enablePushbackSet.value
  });
  resetMockApi();
  addMockApiHandler();
  resetMockApiResponse();
  addResponse();
  nextTick(() => {
    scrollToTop();
  });
};

// ---- ScrollList end ----
const save = async (): Promise<void> => {
  let errorNum = 0;
  if (!isValid()) {
    errorNum++;
  }

  if (!validateResponse()) {
    errorNum++;
  }

  if (errorNum) {
    notification.error(t('mock.mockApis.notifications.dataError'));
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
      permissionMap.value.set(targetId, ['CLONE', MockServicePermission.DELETE, MockServicePermission.EXPORT]);
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

  notification.success(t('mock.mockApis.notifications.saveSuccess'));
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
        nameErrorMessage.value[id] = t('mock.mockApis.validation.nameDuplicate');
      } else {
        nameErrorSet.value.add(key);
        nameErrorMessage.value[key] = t('mock.mockApis.validation.nameDuplicate');
      }

      errorNum++;
    } else {
      nameErrorMessage.value[key] = undefined;
      nameErrorSet.value.delete(key);
    }
  }

  return !errorNum;
};

const collapseChange = (keys: string[]) => {
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
      delay: { mode: 'NONE' },
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
        method: method.value || 'GET',
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

  if (navigator.connection) {
    navigator.connection.addEventListener('change', updateWs);
  }
});

onBeforeUnmount(() => {
  WS.value && WS.value.close(1000);
  if (navigator.connection) {
    navigator.connection.removeEventListener('change', updateWs);
  }
});

const trigger = ['contextmenu'];
const menuItems = ref([
  {
    key: 'clone',
    icon: 'icon-fuzhizujian2',
    name: t('mock.mockApis.menuItems.clone'),
    permission: 'ADD'
  },
  {
    key: 'delete',
    icon: 'icon-fz',
    name: t('mock.mockApis.menuItems.delete'),
    permission: MockServicePermission.DELETE
  },
  {
    key: 'export',
    icon: 'icon-daochu1',
    name: t('mock.mockApis.menuItems.export'),
    permission: MockServicePermission.EXPORT
  }
]);
const dropdownMenuItems = ref([
  {
    key: 'copyApi',
    icon: 'icon-fuzhizujian2',
    name: t('mock.mockApis.menuItems.copyApi'),
    noAuth: true
  },
  {
    key: 'linkApi',
    icon: 'icon-yinyong',
    name: t('mock.mockApis.menuItems.linkApi'),
    noAuth: true
  },
  {
    key: 'import',
    icon: 'icon-daoru',
    name: t('mock.mockApis.menuItems.import'),
    noAuth: true
  },
  {
    key: 'importDemo',
    icon: 'icon-daoru',
    name: t('mock.mockApis.menuItems.importDemo'),
    noAuth: true
  }
]);

const sortMenuItems = [{
  name: t('mock.mockApis.sortMenuItems.createdDate'),
  key: 'createdDate',
  orderSort: 'DESC'
}, {
  name: t('mock.mockApis.sortMenuItems.id'),
  key: 'id',
  orderSort: 'ASC'
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
  <Hints class="mb-1.5" :text="t('mock.mockApis.hints.mockDescription')" />
  <PureCard style="height: calc(100% - 24px);" class="flex-1 flex flex-nowrap pl-3.5 py-3.5 relative">
    <Spin
      :delay="0"
      :spinning="loading"
      class="flex flex-col w-80 h-full">
      <div class="flex flex-nowrap space-x-2 mb-2">
        <Input
          :placeholder="t('mock.mockApis.search.placeholder')"
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
          <span>{{ t('mock.mockApis.buttons.addApi') }}</span>
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
          @click="dropdownSortChange" />

        <div
          class="flex-shrink-0 flex-grow-0 leading-7 text-3.75 cursor-pointer text-theme-text-hover flex items-center"
          @click="toRefresh">
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
        @change="scrollChange">
        <div>
          <Dropdown
            v-for="item in apiIds"
            :key="item"
            :trigger="trigger"
            :menuItems="menuItems"
            :permissions="permissionMap.get(item)"
            @click="dropdownClick($event, item)">
            <div class="list-item-container">
              <div
                :key="item"
                :class="{ 'active-item': mockAPIId === item }"
                class="relative cursor-pointer rounded px-2 pt-2 pb-3 space-y-2 hover:bg-bg-hover text-theme-text-hover"
                @click="select(item)">
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
          @click="clone(mockAPIId!)">
          <div class="flex items-center space-x-1">
            <Icon icon="icon-fuzhizujian2" />
            <span>{{ t('actions.clone') }}</span>
          </div>
        </Button>
        <Button
          v-if="isSavedMockApi"
          type="default"
          size="small"
          @click="toExport(mockAPIId!)">
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
          @click="del(mockAPIId!)">
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
            <span>{{ t('mock.mockApis.buttons.refreshInstance') }}</span>
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
            <span>{{ t('mock.mockApis.buttons.back') }}</span>
          </div>
        </Button>
      </div>
      <div :id="domId" class="w-full flex-1 overflow-auto space-y-6 scroll-smooth">
        <div class="space-y-2">
          <div class="flex items-center text-3.5 space-x-1 text-text-title">
            <div>{{ t('mock.mockApis.form.request') }}</div>
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
                  <span>{{ t('mock.mockApis.form.name') }}</span>
                </div>
              </div>
              <Input
                :maxlength="400"
                :value="summary"
                :error="summaryError"
                :placeholder="t('mock.mockApis.form.namePlaceholder')"
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
              <div class="mb-0.5">{{ t('mock.mockApis.form.description') }}</div>
              <Input
                :maxlength="20000"
                :value="description"
                :autoSize="{ minRows: 5, maxRows: 5 }"
                showCount
                type="textarea"
                :placeholder="t('mock.mockApis.form.descriptionPlaceholder')"
                trim
                @change="descriptionChange" />
            </div>
          </div>
        </div>
        <div class="pr-5.25 space-y-2">
          <div class="flex items-center justify-between leading-5 pr-3.25 text-3.5 text-text-title">
            <div>
              <span>{{ t('mock.mockApis.form.response') }}</span>
              <Tooltip>
                <template #title>{{ t('mock.mockApis.form.responseTooltip') }}</template>
                <Icon icon="icon-shuoming" class="text-tips cursor-pointer text-3.5 ml-1" />
              </Tooltip>
            </div>
            <Button
              size="small"
              type="primary"
              @click="addResponse">
              <div class="flex items-center space-x-1">
                <Icon icon="icon-jia" />
                <span>{{ t('mock.mockApis.buttons.addResponse') }}</span>
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
                        <span>{{ t('mock.mockApis.form.name') }}</span>
                      </div>

                      <Validate
                        class="flex-1"
                        :text="nameErrorMessage[item]"
                        :fixed="true">
                        <Input
                          :maxlength="200"
                          :value="nameMap[item]"
                          :error="nameErrorSet.has(item)"
                          :placeholder="t('mock.mockApis.form.namePlaceholder')"
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
                        <span>{{ t('mock.mockApis.buttons.deleteResponse') }}</span>
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
                        <span>{{ t('mock.mockApis.response.match') }}</span>
                        <Tooltip>
                          <template #title>
                            {{ t('mock.mockApis.response.matchTooltip') }}
                          </template>
                          <Icon icon="icon-shuoming" class="text-tips cursor-pointer text-3.5 ml-1" />
                        </Tooltip>
                      </div>
                    </div>
                  </template>

                  <div class="flex items-center leading-5 mb-5 space-x-2">
                    <div class="flex items-center">
                      <IconRequired />
                      <span>{{ t('mock.mockApis.response.priority') }}</span>
                      <Tooltip>
                        <template #title>{{ t('mock.mockApis.response.priorityTooltip') }}</template>
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
                        <span>{{ t('mock.mockApis.response.content') }}</span>
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
                        <span>{{ t('mock.mockApis.response.pushback') }}</span>
                        <Tooltip>
                          <template #title>{{ t('mock.mockApis.response.pushbackTooltip') }}</template>
                          <Icon icon="icon-shuoming" class="text-tips cursor-pointer text-3.5 ml-1" />
                        </Tooltip>
                      </div>

                      <Switch
                        :checked="enablePushbackSet.has(item)"
                        size="small"
                        class="ml-1.5"
                        checkedChildren="t('mock.mockApis.response.enable')"
                        unCheckedChildren="t('mock.mockApis.response.disable')"
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
      title="复制接口添加"
      type="copy"
      @ok="copyApiOk" />
  </AsyncComponent>
  <AsyncComponent :visible="apiLinkModalVisible">
    <SelectApiModal
      v-model:visible="apiLinkModalVisible"
      v-model:confirmLoading="apiLinkConfirmLoading"
      title="关联接口添加"
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
@/views/apis/Service/commonComp/Agent/PropsTypes
