<script setup lang="ts">
import { computed, defineAsyncComponent, nextTick, onBeforeUnmount, onMounted, provide, ref, watch } from 'vue';
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

import { type AgentValue } from '@/views/apis/services/components/agent/PropsTypes';
import { mock } from '@/api/tester';
import { setting } from 'src/api/gm';
import { HttpMethod, MockAPIConfig, MockAPIInfo, ResponseConfig, ResponseInfo } from './PropsType';

interface Props {
  id: string;// mock service Id
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

const DebugApis = defineAsyncComponent(() => import('./debugApis.vue'));
const SelectApiModal = defineAsyncComponent(() => import('@/views/mock/detail/mockApis/components/selectApiModal/index.vue'));
const MatchForm = defineAsyncComponent(() => import('@/views/mock/detail/mockApis/components/matchForm/index.vue'));
const ContentForm = defineAsyncComponent(() => import('@/views/mock/detail/mockApis/components/contentForm/index.vue'));
const PushBack = defineAsyncComponent(() => import('@/views/mock/detail/mockApis/components/contentForm/pushBack.vue'));
const UrlForm = defineAsyncComponent(() => import('@/views/mock/detail/mockApis/components/urlForm/index.vue'));
const ImportApiModal = defineAsyncComponent(() => import('@/views/mock/detail/mockApis/components/importApiModal/index.vue'));

const router = useRouter();

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
const orderSort = ref<'ASC' | 'DESC'>('DESC');
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
const method = ref<HttpMethod>('GET');
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
    filters?: [{ key: 'summary'; op: 'MATCH_END', value: string }];
    orderBy?: string;
    orderSort?: 'ASC' | 'DESC';
  } = {
    mockServiceId: props.id
  };

  if (inputValue.value) {
    _params.filters = [{ key: 'summary', op: 'MATCH_END', value: inputValue.value }];
  }

  if (orderBy.value) {
    _params.orderBy = orderBy.value;
  }

  if (orderSort.value) {
    _params.orderSort = orderSort.value;
  }

  return _params;
});

const addMockApi = () => {
  if (typeof scrollRef.value?.pureAdd === 'function') {
    const id = utils.uuid();
    const data: MockAPIConfig = {
      id,
      summary: 'MockAPI-' + Date.now(),
      isTempFlag: true,
      mockServiceId: props.id,
      method: 'GET',
      endpoint: '',
      description: ''
    };

    scrollRef.value.pureAdd(data);
    apiIds.value.unshift(id);
    apiDataMap.value[id] = data;
    permissionMap.value.set(id, ['DELETE']);
    setApiForm(data);
  }
};

const scrollChange = (dataList: MockAPIInfo[]) => {
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
    permissionMap.value.set(id, ['CLONE', 'DELETE', 'EXPORT']);
  }

  if (!dataList.length) {
    mockAPIId.value = undefined;
    mockAPIConfig.value = undefined;
    resetMockApi();
    resetMockApiResponse();
    addMockApi();
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

const select = (id: string) => {
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
          mode: 'NONE',
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

const dropdownSortChange = (data: { orderBy: 'createdDate' | 'id', orderSort: 'ASC' | 'DESC' }) => {
  orderBy.value = data.orderBy;
  orderSort.value = data.orderSort;
};

const toRefresh = () => {
  notify.value++;
  loading.value = true;
};

const dropdownClick = async ({ key }: { key: 'clone' | 'delete' | 'export' }, id: string) => {
  if (key === 'clone') {
    clone(id);
    return;
  }

  if (key === 'delete') {
    del(id);
    return;
  }

  if (key === 'export') {
    toExport(id);
  }
};

const buttonDropdownClick = ({ key }: { key: 'copyApi' | 'linkApi' | 'import' | 'importDemo' }) => {
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
  if (key === 'importDemo') {
    importDemo();
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
  notification.success('复制接口添加成功');
  toRefresh();
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
  notification.success('关联接口添加成功');
  toRefresh();
  nextTick(() => {
    scrollToTop();
  });
};

const importApiOk = () => {
  toRefresh();
  nextTick(() => {
    scrollToTop();
  });
};

// 导入示例
const importDemo = async () => {
  if (loading.value) {
    return;
  }
  loading.value = true;
  const [error] = await mock.importDemoMockApi(props.id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('导入示例成功');
  toRefresh();
  nextTick(() => {
    scrollToTop();
  });
};

const clone = async (id: string) => {
  loading.value = true;
  const [error] = await mock.cloneMockApi(id);
  if (error) {
    loading.value = false;
    return;
  }

  toRefresh();
  notification.success('克隆成功');
};

const toExport = (id: string) => {
  const url = `${TESTER}/mock/service/export?mockApiIds=${id}&mockServiceId=${props.id}`;
  download(url);
};

const del = async (id: string) => {
  const { summary: _summary, isTempFlag } = apiDataMap.value[id];
  modal.confirm({
    centered: true,
    content: `确认要删除接口【${_summary}】吗？`,
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

        notification.success('删除成功');

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
    content: '刷新实例接口会强制同步当前Mock接口信息到运行的服务实例。',
    async onOk () {
      loading.value = true;
      const [error] = await mock.syncApiInstanceConfig(mockAPIId.value!);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success('刷新实例接口成功');
    }
  });
};

const inputChange = debounce(duration.search, (event: { target: { value: string } }) => {
  const value = event.target.value.trim();
  inputValue.value = value;
});

const create = () => {
  readonly.value = false;
  storeTempMockApiData();
  resetMockApi();
  addMockApi();
  resetMockApiResponse();
  addResponse();
  nextTick(() => {
    scrollToTop();
  });
};

// ---- ScrollList end ----

// ---- CreateForm start ----
const enableChange = (checked: boolean, id: string) => {
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

const summaryChange = (event: { target: { value: string } }) => {
  summary.value = event.target.value;
  summaryError.value = false;
};

const descriptionChange = (event: { target: { value: string } }) => {
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
    notification.error('数据有误，请检查并更正后再保存。');
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
      permissionMap.value.set(targetId, ['CLONE', 'DELETE', 'EXPORT']);
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

  notification.success('保存成功');
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
        nameErrorMessage.value[id] = '名称重复';
      } else {
        nameErrorSet.value.add(key);
        nameErrorMessage.value[key] = '名称重复';
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

const resetMockApi = () => {
  mockAPIConfig.value = undefined;
  mockAPIId.value = '';
  readonly.value = false;
  summary.value = '';
  summaryError.value = false;
  method.value = 'GET';
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
    name: '克隆',
    permission: 'ADD'
  },
  {
    key: 'delete',
    icon: 'icon-fz',
    name: '删除',
    permission: 'DELETE'
  },
  {
    key: 'export',
    icon: 'icon-daochu1',
    name: '导出',
    permission: 'EXPORT'
  }
]);
const dropdownMenuItems = ref([
  {
    key: 'copyApi',
    icon: 'icon-fuzhizujian2',
    name: '复制接口添加',
    noAuth: true
  },
  {
    key: 'linkApi',
    icon: 'icon-yinyong',
    name: '关联接口添加',
    noAuth: true
  },
  {
    key: 'import',
    icon: 'icon-daoru',
    name: '导入接口',
    noAuth: true
  },
  {
    key: 'importDemo',
    icon: 'icon-daoru',
    name: '导入示例',
    noAuth: true
  }
]);

const sortMenuItems = [{
  name: '按添加时间',
  key: 'createdDate',
  orderSort: 'DESC'
}, {
  name: '按ID',
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
  <Hints class="mb-1.5" text="当某个接口尚未实现或无法访问时，可以通过Mock接口模仿真实接口的行为和数据返回，以便进行开发和测试工作，从而提高开发和测试效率。注意：Mock接口保存后才生效。" />
  <PureCard style="height: calc(100% - 24px);" class="flex-1 flex flex-nowrap pl-3.5 py-3.5 relative">
    <Spin
      :delay="0"
      :spinning="loading"
      class="flex flex-col w-80 h-full">
      <div class="flex flex-nowrap space-x-2 mb-2">
        <Input
          placeholder="查询名称"
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
          <span>添加接口</span>
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
            <span>保存</span>
          </div>
        </Button>
        <Button
          v-if="isSavedMockApi"
          type="default"
          size="small"
          @click="clone(mockAPIId!)">
          <div class="flex items-center space-x-1">
            <Icon icon="icon-fuzhizujian2" />
            <span>克隆</span>
          </div>
        </Button>
        <Button
          v-if="isSavedMockApi"
          type="default"
          size="small"
          @click="toExport(mockAPIId!)">
          <div class="flex items-center space-x-1">
            <Icon icon="icon-fuzhizujian2" />
            <span>导出</span>
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
            <span>删除</span>
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
            <span>刷新实例接口</span>
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
            <span>刷新</span>
          </div>
        </Button>
        <Button
          type="default"
          size="small"
          @click="goback">
          <div class="flex items-center space-x-1">
            <Icon icon="icon-fanhui" />
            <span>返回</span>
          </div>
        </Button>
      </div>
      <div :id="domId" class="w-full flex-1 overflow-auto space-y-6 scroll-smooth">
        <div class="space-y-2">
          <div class="flex items-center text-3.5 space-x-1 text-text-title">
            <div>请求</div>
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
                  <span>名称</span>
                </div>
              </div>
              <Input
                :maxlength="400"
                :value="summary"
                :error="summaryError"
                placeholder="最大支持400个字符"
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
              <div class="mb-0.5">描述</div>
              <Input
                :maxlength="20000"
                :value="description"
                :autoSize="{ minRows: 5, maxRows: 5 }"
                showCount
                type="textarea"
                placeholder="最大支持20000个字符"
                trim
                @change="descriptionChange" />
            </div>
          </div>
        </div>
        <div class="pr-5.25 space-y-2">
          <div class="flex items-center justify-between leading-5 pr-3.25 text-3.5 text-text-title">
            <div>
              <span>响应</span>
              <Tooltip>
                <template #title>最大支持配置50个响应</template>
                <Icon icon="icon-shuoming" class="text-tips cursor-pointer text-3.5 ml-1" />
              </Tooltip>
            </div>
            <Button
              size="small"
              type="primary"
              @click="addResponse">
              <div class="flex items-center space-x-1">
                <Icon icon="icon-jia" />
                <span>添加响应</span>
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
                        <span>名称</span>
                      </div>

                      <Validate
                        class="flex-1"
                        :text="nameErrorMessage[item]"
                        :fixed="true">
                        <Input
                          :maxlength="200"
                          :value="nameMap[item]"
                          :error="nameErrorSet.has(item)"
                          placeholder="最大支持200个字符"
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
                        <span>删除响应</span>
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
                        <span>匹配</span>
                        <Tooltip>
                          <template #title>
                            指定返回当前响应所需满足的匹配请求条件。注意：当存在满足匹配的多个响应时，将返回优先级最高的响应，如果没有配置匹配条件或优先级相同，则返回第一个条件或最先配置的条件。
                          </template>
                          <Icon icon="icon-shuoming" class="text-tips cursor-pointer text-3.5 ml-1" />
                        </Tooltip>
                      </div>
                    </div>
                  </template>

                  <div class="flex items-center leading-5 mb-5 space-x-2">
                    <div class="flex items-center">
                      <IconRequired />
                      <span>优先级</span>
                      <Tooltip>
                        <template #title>值越大优先级越高，值范围：0~2147483647。注意：仅当存在满足匹配的多个响应时，才会返回优先级最高的响应。</template>
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
                        <span>内容</span>
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
                        <span>回推</span>
                        <Tooltip>
                          <template #title>回推可以用于在接收到 Mock 请求后，自动向指定地址推送一个 Http 请求，如：支付回调。</template>
                          <Icon icon="icon-shuoming" class="text-tips cursor-pointer text-3.5 ml-1" />
                        </Tooltip>
                      </div>

                      <Switch
                        :checked="enablePushbackSet.has(item)"
                        size="small"
                        class="ml-1.5"
                        checkedChildren="启用"
                        unCheckedChildren="关闭"
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
