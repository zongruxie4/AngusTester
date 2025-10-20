<script setup lang="ts">
import {
  computed,
  defineAsyncComponent,
  inject,
  nextTick,
  onBeforeUnmount,
  onMounted,
  provide,
  ref,
  watch
} from 'vue';
import { Badge, Button, Popover, RadioButton, RadioGroup, TabPane, Tabs } from 'ant-design-vue';
import { ActivityTimeline, AsyncComponent, Drawer, Hints, Icon, NoData, notification, Spin } from '@xcan-angus/vue-ui';
import elementResizeDetector from 'element-resize-detector';
import {
  ActionOnEOF,
  duration,
  ParameterIn,
  SharingMode,
  utils
} from '@xcan-angus/infra';
import useClipboard from 'vue-clipboard3';
import { debounce } from 'throttle-debounce';
import { useI18n } from 'vue-i18n';

import { formatBytes } from '@/utils/common';
import { apis } from '@/api/tester';
import { ApiPermission, ApisShareScope } from '@/enums/enums';

// eslint-disable-next-line import/no-duplicates
import {
  API_EXTENSION_KEY,
  API_STATUS_BADGE_COLOR_CONFIG,
  API_STATUS_COLOR_CONFIG,
  travelXcValueToString,
  travelEmptyObjToString
} from '@/utils/apis';

import { getServerData } from '@/views/apis/server/types';
import { getShowAuthData } from '@/components/apis/authencation/interface';
import { getStatusText } from '@/views/apis/services/components/types';
import { RequestSetting } from './types';

// Import composables
import {
  ApiAssert,
  ApiCookie,
  ApiRequest,
  ApiResponse,
  ApiTimeline,
  AssertForm,
  Authorization,
  debugTip,
  docInfo,
  getStatusColor,
  RequestBody,
  RequestCookie,
  RequestHeader,
  RequestParams,
  useApiState,
  useRequestHandler,
  useResponseHandler,
  useAssertionHandler,
  useUIState,
  useParameterManager
} from './composables';

import SelectEnum from '@/components/enum/SelectEnum.vue';

const Indicator = defineAsyncComponent(() => import('@/components/Indicator/index.vue'));
const HttpTestInfo = defineAsyncComponent(() => import('@/components/test/HttpTestInfo/index.vue'));
const FunctionsButton = defineAsyncComponent(() => import('@xcan-angus/vue-ui').then(resp => resp.FunctionsButton));
const APICaseParametric = defineAsyncComponent(() => import('@/components/apis/parameterization/index.vue'));
const ExecDetail = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/exec/index.vue'));

const UnarchivedEdit = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/slider/UnarchivedEdit.vue'));
const InfoEdit = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/slider/InfoEdit.vue'));

const ShareList = defineAsyncComponent(() => import('@/components/share/list.vue'));
const RequestProxy = defineAsyncComponent(() => import('@/views/config/proxy/EditableRequestProxy.vue'));
const CodeSnippet = defineAsyncComponent(() => import('@/views/apis/services/components/CodeSnippet.vue'));

const ApiSetting = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/Setting.vue'));
const ServerPath = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/path/index.vue'));
const Toolbar = defineAsyncComponent(() => import('@/components/layout/toolbar/index.vue'));
const ResponseError = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/ResponseError.vue'));
const ApiDoc = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/Doc.vue'));
const TestCase = defineAsyncComponent(() => import('@/views/apis/services/components/case/tableView.vue'));
const ApiMockVue = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/MockApi.vue'));
const ApiShare = defineAsyncComponent(() => import('@/views/apis/share/Edit.vue'));

interface Props {
  pid: string,
  valueObj: Record<string, any>,
  ws: WebSocket | undefined;
  response: string,
  uuid: string;
  id?: string | undefined;
  serviceId?: string;
  projectId: string;
  appInfo: { [key: string]: string };
  userInfo: { [key: string]: string };
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  pid: undefined,
  serviceId: undefined,
  projectId: ''
});

// Use composables
const {
  saveParams,
  isUnarchivedApi,
  apisStatus,
  loadingApiDetail,
  apiAuths,
  currentServer,
  defaultCurrentServer,
  availableServers,
  apiMethod,
  apiUri,
  state,
  setting,
  defaultAuthentication,
  authInHeader,
  contentType,
  resolvedRefModels,
  datasetActionOnEOF,
  datasetSharingMode,
  parametersNum,
  headerCount,
  cookieCount,
  hasBodyContent,
  handleStatusChange,
  updateApiInfo,
  updateUnarchivedApiInfo
} = useApiState(props);

const {
  loading,
  sendRequest,
  handleAbort,
  getParameter,
  getRealUri
} = useRequestHandler();

const {
  responseState,
  responseError,
  assertResult,
  isResponseEmpty,
  handleHttpResponse,
  onResponse
} = useResponseHandler();

const {
  assertNum,
  validateParam
} = useAssertionHandler();

const {
  activeTabKey,
  activeKey,
  currentTab,
  activeDrawerKey,
  shareVisible,
  height,
  maxHeight,
  moving,
  errorTitle,
  myNavs,
  toolbarMenus,
  handleShare,
  toolbarChange,
  setErrTitle,
  closeDrawer
} = useUIState(props);

const {
  changeParamList,
  changeHeaderList,
  changeCookieList,
  changeRequestBody,
  addQueryParam
} = useParameterManager();

// Other state
const erd = elementResizeDetector({ strategy: 'scroll' });
const { toClipboard } = useClipboard();

const { valueKey } = API_EXTENSION_KEY;

// eslint-disable-next-line @typescript-eslint/no-empty-function
const updateTabPane = inject<(data: any) => void>('updateTabPane', () => {
});
// eslint-disable-next-line @typescript-eslint/no-empty-function
const replaceTabPane = inject<(key: string, data: any) => void>('replaceTabPane', () => {
});
const allFunction = inject('allFunction', ref([]));

let ws: WebSocket | undefined;
const uuid = '';

// Component refs
const requestParamsRef = ref();
const drawerRef = ref();
const toolbarRef = ref();
const mainWrapper = ref();
const requestHeaderRef = ref();
const requestCookieRef = ref();
const authorizationRef = ref();
const requestBodyRef = ref();
const assertFormRef = ref();

let initParams = {};

// Methods
const changeAuthData = (data): void => {
  state.authentication = data;
};

const changeSetting = (data: RequestSetting): void => {
  setting.value = data;
};

const targetInfoChange = (data: { id: string; datasetActionOnEOF: ActionOnEOF; datasetSharingMode: SharingMode; }) => {
  datasetActionOnEOF.value = data.datasetActionOnEOF;
  datasetSharingMode.value = data.datasetSharingMode;
};

const copyUrl = async () => {
  const pathJson = {};
  const pathArr = state.parameters.filter(i => i.in === ParameterIn.path);
  pathArr.forEach(i => {
    pathJson[i.name || ''] = i[valueKey];
  });

  const queryJson = {};
  const queryyArr = state.parameters.filter(i => i.in === ParameterIn.query);
  queryyArr.forEach(i => {
    queryJson[i.name || ''] = travelEmptyObjToString(i[valueKey]);
  });

  const apiPathQuery = getRealUri(pathJson, queryJson, apiUri.value || '');
  const serverUrl = getServerData(currentServer.value || { url: '' });

  let apiHref = '';
  if (!serverUrl.endsWith('/') && apiPathQuery && apiPathQuery.split('?')[0] && !apiPathQuery.startsWith('/')) {
    apiHref = serverUrl + '/' + apiPathQuery;
  } else {
    apiHref = serverUrl + apiPathQuery;
  }

  await toClipboard(apiHref);
  notification.success(t('service.apis.notifications.copyUrlSuccess'));
};

const resizeHandler = debounce(duration.resize, () => {
  nextTick(() => {
    maxHeight.value = mainWrapper.value.clientHeight;
  });
});

// Save-related methods
const save = (): void => {
  if (isUnarchivedApi.value) {
    saveUnarchived();
  } else {
    autoSave();
  }
};

const archivedApi = () => {
  drawerRef.value.open('save');
};

const autoSave = async () => {
  if (state.publishFlag) {
    notification.warning(t('service.apis.notifications.apiPublishedWarning'));
  }
  if (!validateParam(assertFormRef)) {
    return;
  }
  let params = await getParameter(false, state, setting.value,
    currentServer.value || { url: '' },
    apiUri.value || '', apiMethod.value,
    contentType.value, saveParams.value,
    requestBodyRef, authorizationRef, assertFormRef, isUnarchivedApi.value);
  params = JSON.parse(JSON.stringify(params));

  if (utils.deepCompare(initParams, JSON.parse(JSON.stringify(params)))) {
    return;
  }

  if (apiAuths.value.includes(ApiPermission.MODIFY) && !state.publishFlag) {
    if (requestHeaderRef.value) {
      await requestHeaderRef.value.updateComp();
    }
    if (requestParamsRef.value) {
      await requestParamsRef.value.updateComp();
    }
    if (requestBodyRef.value) {
      await requestBodyRef.value.updateComp();
    }
    params.parameters = travelXcValueToString(params.parameters);
    params.requestBody = travelXcValueToString(params.requestBody);
    const [error] = await apis.updateApi([params]);
    initParams = JSON.parse(JSON.stringify(params));
    if (error) {
      return;
    }
    notification.success(t('actions.tips.saveSuccess'));
  }
};

const saveUnarchived = async (): Promise<void> => {
  drawerRef.value.open('saveUnarchived');
};

// Watchers
watch(() => state.authentication, async newValue => {
  const data = await getShowAuthData(newValue);
  authInHeader.value = data?.[0] || {};
}, {
  deep: true
});

watch(() => props.uuid, newValue => {
  if (newValue === uuid) {
    onResponse(props.response);
  } else {
    try {
      const data = JSON.parse(props.response);
      authorizationRef.value.onResponse(data);
    } catch {
    }
  }
});

watch(() => props.ws, (newValue) => {
  ws = newValue;
  setErrTitle(!!newValue);
}, {
  immediate: true
});

// Lifecycle
onMounted(() => {
  resizeHandler();
  erd.listenTo(mainWrapper.value, resizeHandler);
});

onBeforeUnmount(() => {
  erd.removeListener(mainWrapper.value, resizeHandler);
});

// Providers
provide('setApiInfo', async (info) => {
  await updateApiInfo(info);
  if (props.pid === info.id + 'API') {
    updateTabPane({ _id: props.pid, pid: props.pid, id: info.id, name: info.name, unarchived: false });
  } else {
    replaceTabPane(props.pid, {
      _id: info.id + 'API',
      pid: info.id + 'API',
      id: info.id,
      name: info.name,
      unarchived: false,
      value: 'API'
    });
  }
  initParams = await getParameter(false, state, setting.value, currentServer.value || { url: '' }, apiUri.value || '', apiMethod.value, contentType.value, saveParams.value, requestBodyRef, authorizationRef, assertFormRef, isUnarchivedApi.value);
});

provide('setUnarchivedApiInfo', async (info) => {
  await updateUnarchivedApiInfo(info);
  if (props.pid === info.id + 'API') {
    updateTabPane({ _id: props.pid, pid: info.id + 'API', id: info.id, name: info.name, unarchived: true });
  } else {
    replaceTabPane(props.pid, {
      _id: info.id + 'API',
      pid: info.id + 'API',
      id: info.id,
      name: info.name,
      unarchived: true,
      value: 'API'
    });
  }
  initParams = await getParameter(false, state, setting.value, currentServer.value || { url: '' }, apiUri.value || '', apiMethod.value, contentType.value, saveParams.value, requestBodyRef, authorizationRef, assertFormRef, isUnarchivedApi.value);
});

defineExpose({ autoSave, pid: props.pid });
provide('getParameter', () => getParameter(false, state, setting.value, currentServer.value || { url: '' }, apiUri.value || '', apiMethod.value, contentType.value, saveParams.value, requestBodyRef, authorizationRef, assertFormRef, isUnarchivedApi.value));
provide('auths', apiAuths);
provide('id', computed(() => saveParams.value.id));
provide('apiBaseInfo', computed(() => saveParams.value));
provide('isUnarchivedApi', isUnarchivedApi);
provide('resolvedRefModels', computed(() => resolvedRefModels.value));
provide('archivedId', computed(() => {
  return isUnarchivedApi.value ? undefined : saveParams.value.id;
}));
provide('selectHandle', () => closeDrawer(drawerRef));
</script>

<template>
  <div
    class="z-9 h-full"
    :class="{ 'select-none': moving }">
    <div class="flex flex-nowrap h-full w-full">
      <div class="flex flex-col h-full flex-1 min-w-0 relative">
        <div v-if="!isUnarchivedApi" class="mx-5 mt-3 flex justify-between">
          <RadioGroup
            v-model:value="activeTabKey"
            buttonStyle="solid"
            size="small">
            <RadioButton value="debug">{{ t('actions.debug') }}</RadioButton>
            <RadioButton value="case">{{ t('common.useCase') }}</RadioButton>
            <RadioButton value="test">{{ t('service.apis.tabs.test') }}</RadioButton>
            <RadioButton value="mock">{{ t('service.apis.tabs.mock') }}</RadioButton>
            <RadioButton value="doc">{{ t('common.doc') }}</RadioButton>
          </RadioGroup>

          <div class="inline-flex items-center space-x-3">
            <Button
              type="link"
              size="small"
              class="hover:text-status-orange  text-status-orange !bg-board-orange"
              @click="handleShare">
              <Icon icon="icon-fenxiang" class="mr-2" />
              {{ t('actions.share') }}
            </Button>
            <SelectEnum
              v-model:value="apisStatus"
              :disabled="!apiAuths.includes(ApiPermission.MODIFY)"
              :lazy="false"
              class="inline-block"
              enumKey="ApiStatus"
              @change="handleStatusChange">
              <template #option="{label, value}">
                <div :class="[API_STATUS_COLOR_CONFIG[value]]">
                  <Badge :color="API_STATUS_BADGE_COLOR_CONFIG[value]" />
                  {{ label }}
                </div>
              </template>
            </SelectEnum>
          </div>
        </div>

        <div
          v-show="activeTabKey === 'debug'"
          ref="mainWrapper"
          class="flex-1 flex flex-col justify-between min-h-0">
          <div class="flex-1 w-full overflow-y-auto min-h-0">
            <ServerPath
              :id="props.id"
              v-model:loading="loading"
              v-model:endpoint="apiUri"
              v-model:currentServer="currentServer"
              v-model:method="apiMethod"
              class="mx-5 mt-3"
              :isUnarchivedApi="isUnarchivedApi"
              :availableServers="availableServers"
              :defaultCurrentServer="defaultCurrentServer"
              @sendRequest="() => sendRequest(state, setting, currentServer || { url: '' }, apiUri || '', apiMethod, contentType, saveParams, requestBodyRef, authorizationRef, assertFormRef, isUnarchivedApi, ws, allFunction.map(i => i.name), handleHttpResponse, () => {})"
              @save="save"
              @archived="archivedApi"
              @abort="handleAbort"
              @addQuery="(data) => addQueryParam(data, requestParamsRef)"
              @copyUrl="copyUrl" />
            <Tabs
              v-model:active-key="activeKey"
              style="height: calc(100% - 68px);"
              class="inner-container mt-2.5"
              size="small"
              centered>
              <TabPane key="parameters" :tab="`${t('service.apis.requestTabs.parameters')}(${parametersNum})`">
                <div class="flex">
                  <RequestParams
                    ref="requestParamsRef"
                    v-model:apiUri="apiUri"
                    class="flex-1 px-5"
                    :value="state.parameters"
                    @change="changeParamList" />
                </div>
              </TabPane>
              <TabPane
                key="header"
                :tab="`${t('service.apis.requestTabs.header')}(${headerCount || 0})`"
                :forceRender="true">
                <div class="flex">
                  <RequestHeader
                    ref="requestHeaderRef"
                    v-model:contentType="contentType"
                    class="flex-1 px-5"
                    :value="state.headerList"
                    :authData="authInHeader"
                    @change="changeHeaderList" />
                </div>
              </TabPane>
              <TabPane
                key="cookie"
                :tab="`${t('protocol.cookie')}(${cookieCount || 0})`"
                :forceRender="true">
                <div class="flex">
                  <RequestCookie
                    ref="requestCookieRef"
                    class="flex-1 px-5"
                    :value="state.cookieList"
                    @change="changeCookieList" />
                </div>
              </TabPane>
              <TabPane key="authentication" :forceRender="true">
                <template #tab>
                  <div :class="{ 'has-content': state.secured }"></div>
                  Authorization
                </template>
                <Authorization
                  ref="authorizationRef"
                  v-model:auth="state.secured"
                  class="px-5"
                  :ws="props.ws"
                  :defaultValue="defaultAuthentication"
                  @change="changeAuthData" />
              </TabPane>
              <TabPane key="request-body" :forceRender="true">
                <template #tab>
                  <div :class="{ 'has-content': hasBodyContent }"></div>
                  <span>{{ t('protocol.requestBody') }}</span>
                </template>
                <RequestBody
                  ref="requestBodyRef"
                  v-model:contentType="contentType"
                  class="h-full px-5"
                  :defaultValue="state.requestBody"
                  @change="changeRequestBody" />
              </TabPane>
              <TabPane
                v-if="!!props.id"
                key="parametric"
                :tab="t('common.parameterization')">
                <APICaseParametric
                  :datasetActionOnEOF="datasetActionOnEOF"
                  :datasetSharingMode="datasetSharingMode"
                  :targetId="props.id"
                  targetType="API"
                  class="px-5 pb-5 overflow-auto h-full"
                  @targetInfoChange="targetInfoChange" />
              </TabPane>
              <TabPane
                key="assert"
                :tab="`${t('common.assertionResult')}(${assertNum})`"
                :forceRender="true">
                <AssertForm
                  :id="props.id"
                  ref="assertFormRef"
                  v-model:num="assertNum"
                  class="px-5"
                  :value="state.assertions" />
              </TabPane>
              <TabPane key="setting" :tab="t('common.setting')">
                <ApiSetting
                  class="px-5"
                  :value="setting"
                  @change="changeSetting" />
              </TabPane>
              <template #rightExtra>
                <FunctionsButton class="text-3.5 mr-2" />
                <Popover placement="leftTop" :trigger="['click']">
                  <Icon icon="icon-jieshaoshuoming" class="cursor-pointer mr-2 text-4 text-theme-text-hover" />
                  <template #content>
                    <div class="w-100 text-3">
                      <Hints :text="t('service.apis.hints.notes')" class="font-semibold !text-text-title" />
                      <ul class="mt-2 pl-4 list-disc">
                        <li
                          v-for="item in debugTip"
                          :key="item"
                          class="mt-2 text-text-sub-content text-3">
                          {{ item }}
                        </li>
                      </ul>
                    </div>
                  </template>
                </Popover>
                <Popover placement="leftTop" :trigger="['click']">
                  <Icon icon="icon-tiaoshijiaoben" class="cursor-pointer mr-2 text-3.5 text-theme-text-hover" />
                  <template #content>
                    <div class="w-150 text-3">
                      <Hints
                        :text="t('service.apis.hints.parameterSerialization')"
                        class="font-semibold !text-text-title" />
                      <div class="mt-2">
                        {{ t('service.apis.hints.serializationDescription_pre') }}
                        <a
                          class="text-theme-special"
                          href="https://datatracker.ietf.org/doc/html/rfc6570"
                          target="_blank">RFC6570</a>
                        {{ t('service.apis.hints.serializationDescription_later') }}
                      </div>
                      <ul class="pl-4 list-disc">
                        <li
                          v-for="item in docInfo"
                          :key="item.title"
                          class="mt-2">
                          <div class="font-semibold">{{ item.title }}</div>
                          <div
                            v-for="text in item.rules"
                            :key="text"
                            class="text-text-sub-content text-3">
                            {{ text }}
                          </div>
                        </li>
                      </ul>
                    </div>
                  </template>
                </Popover>
              </template>
            </Tabs>
          </div>
          <Toolbar
            ref="toolbarRef"
            v-model:height="height"
            v-model:tab="currentTab"
            v-model:moving="moving"
            :menus="toolbarMenus"
            :maxHeight="maxHeight"
            @change="toolbarChange">
            <template #content="{ activeMenu }">
              <div class="toolbar-main  relative" :class="{ 'select-text': !moving }">
                <template v-if="['request', 'response', 'time', 'cookie', 'assert'].includes(activeMenu || '')">
                  <template v-if="responseError.show">
                    <ResponseError
                      class="h-full overflow-hidden"
                      :errorTitle="errorTitle"
                      :info="responseError.value" />
                  </template>
                  <template v-else-if="!isResponseEmpty">
                    <ApiRequest
                      v-show="activeMenu === 'request'"
                      class="px-5"
                      :dataSource="responseState" />
                    <ApiResponse
                      v-show="activeMenu === 'response'"
                      class="px-5"
                      :dataSource="responseState" />
                    <ApiTimeline
                      v-show="activeMenu === 'time'"
                      class="px-5"
                      :dataSource="responseState?.performance" />
                    <ApiCookie
                      v-show="activeMenu === ParameterIn.cookie"
                      class="px-5"
                      :dataSource="responseState?.cookie"
                      :host="getServerData(currentServer.value || { url: '' })" />
                    <ApiAssert
                      v-show="activeMenu === 'assert'"
                      class="px-5"
                      :value="assertResult" />
                  </template>
                  <template v-else>
                    <NoData
                      v-show="activeMenu !== 'define'"
                      class="overflow-hidden"
                      style="height: calc(100% - 29px);" />
                  </template>
                </template>
                <template v-if="activeMenu === 'generateCode'">
                  <CodeSnippet class="px-5 h-full overflow-auto" />
                </template>
                <Spin
                  :spinning="loading"
                  :class="{ '-z-1': !loading }"
                  mask
                  class="!absolute top-0 bottom-0 left-0 right-0">
                </Spin>
              </div>
            </template>
            <template #actions>
              <template v-if="responseState?.status">
                <div
                  class="flex items-center flex-nowrap whitespace-nowrap mr-7.5 text-3 leading-3 text-text-sub-content">
                  <div class="flex items-center flex-nowrap whitespace-nowrap mr-7.5">
                    <span class="mr-1">{{ t('protocol.statusCode') }}:</span>
                    <span :class="getStatusColor(responseState?.status)">{{
                      getStatusText(responseState?.status)
                    }}</span>
                  </div>
                  <div class="flex items-center flex-nowrap whitespace-nowrap mr-7.5">
                    <span class="mr-1">{{ t('common.duration') }}:</span>
                    <span class>{{
                      responseState?.performance?.duration &&
                        (responseState?.performance?.duration.toFixed(0)
                          + 'ms')
                    }}</span>
                  </div>
                  <div class="flex items-center flex-nowrap whitespace-nowrap">
                    <span class="mr-1">{{ t('common.size') }}:</span>
                    <span class>{{
                      isNaN(Number(responseState?.size)) ? responseState?.size :
                      formatBytes(Number(responseState?.size))
                    }}</span>
                  </div>
                </div>
              </template>
            </template>
          </Toolbar>
          <AsyncComponent :visible="shareVisible">
            <ApiShare
              v-model:visible="shareVisible"
              :projectId="props.projectId"
              :apisIds="[props.id]"
              :servicesId="saveParams.serviceId"
              :shareScope="ApisShareScope.SINGLE_APIS" />
          </AsyncComponent>
        </div>

        <template v-if="activeTabKey === 'case'">
          <TestCase
            :id="props.id"
            :serviceId="saveParams.serviceId"
            layout="inline"
            class="mx-5"
            type="API" />
        </template>

        <template v-if="activeTabKey === 'test'">
          <ExecDetail
            :apisId="props.id"
            :projectId="props.projectId"
            :userInfo="props.userInfo"
            :appInfo="props.appInfo"
            class="mx-5 mt-3 flex-1 overflow-auto mb-3" />
        </template>

        <template v-if="activeTabKey === 'mock'">
          <ApiMockVue
            :id="props.id"
            class="px-5 mt-3 "
            :disabled="!apiAuths.includes(ApiPermission.MODIFY)" />
        </template>

        <template v-if="activeTabKey === 'doc'">
          <ApiDoc
            :id="props.id"
            class="mx-5 mt-3 flex-1 overflow-auto mb-3" />
        </template>

        <Spin
          :spinning="loadingApiDetail"
          :class="{ '-z-1': !loadingApiDetail }"
          :delay="0"
          class="!absolute top-0 bottom-0 left-0 right-0">
        </Spin>
      </div>
      <Drawer
        v-if="activeTabKey === 'debug'"
        ref="drawerRef"
        v-model:activeKey="activeDrawerKey"
        :menuItems="myNavs">
        <template #saveUnarchived>
          <UnarchivedEdit
            v-if="activeDrawerKey === 'saveUnarchived'"
            :id="props.id"
            class="pr-4"
            type="API" />
        </template>
        <template #save>
          <InfoEdit
            v-if="activeDrawerKey === 'save'"
            :id="props.id"
            class="pr-5"
            :disabled="!apiAuths.includes(ApiPermission.MODIFY)"
            type="API" />
        </template>
        <template #performance>
          <Indicator
            v-if="activeDrawerKey === 'performance'"
            :id="props.id"
            :disabled="!apiAuths.includes(ApiPermission.MODIFY)"
            class="pr-5 mt-2"
            type="API" />
        </template>
        <template #activity>
          <ActivityTimeline
            v-if="activeDrawerKey === 'activity'"
            :id="props.id"
            infoKey="description"
            class="pr-5 pt-2"
            type="API" />
        </template>
        <template #test>
          <HttpTestInfo
            :id="props.id"
            class="pr-5 mt-2"
            type="API" />
        </template>
        <template #apiMock>
        </template>
        <template #share>
          <ShareList
            v-if="activeDrawerKey === 'share'"
            :id="props.id"
            class="mt-2 pr-5"
            :disabled="!apiAuths.includes(ApiPermission.SHARE)"
            type="API" />
        </template>
        <template #agent>
          <RequestProxy v-if="activeDrawerKey === 'agent'" class="pr-5 mt-2" />
        </template>
        <template #cases>
        </template>
      </Drawer>
    </div>
  </div>
</template>

<style scoped>
.toolbar-main {
  height: calc(100% - 34px);
}

.inner-container :deep(.ant-tabs-nav-list) {
  width: 100%;
}

.inner-container :deep(.ant-tabs-tabpane) {
  overflow: auto;
}

.inner-container :deep(.ant-tabs-nav-operations) {
  display: none;
}

.inner-container.ant-tabs.ant-tabs-centered :deep(.ant-tabs-nav) {
  @apply mb-3 bg-white;

  margin-right: 20px;
  margin-left: 20px;
}

.ant-select-focused:not(.ant-select-disabled).ant-select:not(.ant-select-customize-input) :deep(.ant-select-selector) {
  @apply shadow-none;
}

.ant-select + .ant-select::after,
:deep(.input-container)::after {
  content: "";

  @apply absolute left-0 top-0 block w-0.25 h-8 border-border-divider;
}

.ant-btn.ant-btn-primary + .ant-btn {
  @apply ml-3 border-none bg-gray-light-b text-text-content;
}

.ant-select:not(.ant-select-customize-input) .ant-select-selector {
  @apply border-none rounded-none;
}

.inner-container.ant-tabs :deep(.ant-tabs-nav-list) .ant-tabs-tab {
  @apply justify-center text-3 leading-3 py-3 text-center min-w-0;
}

.inner-container :deep(.ant-tabs-nav-list) .ant-tabs-tab .ant-tabs-tab-btn {
  @apply text-text-sub-content;
}

.inner-container.ant-tabs-centered.ant-tabs :deep(.ant-tabs-nav-list) .ant-tabs-tab::before {
  @apply hidden;
}

.icon-inner {
  @apply text-3.5 leading-3.5 text-text-sub-content cursor-pointer;
}

.inner-container.ant-tabs-centered.ant-tabs :deep(.ant-tabs-nav-list) .ant-tabs-tab .has-content {
  @apply w-1.5 h-1.5 inline-block bg-status-success rounded-full mr-1;
}
</style>
