<script setup lang="ts">
import { computed, defineAsyncComponent, onBeforeUnmount, onMounted, provide, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import dayjs from 'dayjs';
import { Drawer, Icon, Input, notification, Select } from '@xcan-angus/vue-ui';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { utils, duration, enumUtils } from '@xcan-angus/infra';
import qs from 'qs';
import elementResizeDetector from 'element-resize-detector';
import useClipboard from 'vue-clipboard3';
import ReconnectingWebSocket from 'reconnecting-websocket';
import apiUtils from '@/utils/apis/index';

import { apis } from '@/api/tester';
import { ApiPermission } from '@/enums/enums';
import { formatBytes } from '@/utils/utils';
import { getNameValue } from '@/views/apis/services/apiHttp/utils';
import { FormData, Message } from './PropsType';
import { debounce } from 'throttle-debounce';
import { DATE_TIME_FORMAT } from '@/utils/constant';

interface Props {
  id: string;
  valueObj: Record<string, any>;
  uuid: string;
  ws: WebSocket;
  response: string;
  pid: string;
  responseCount: number;
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  valueObj: undefined,
  uuid: undefined,
  ws: undefined,
  response: undefined,
  pid: undefined,
  responseCount: undefined
});

const ApiServer = defineAsyncComponent(() => import('@/views/apis/services/apiWebSocket/server/index.vue'));
const Indicator = defineAsyncComponent(() => import('@/components/Indicator/index.vue'));
const HttpTestInfo = defineAsyncComponent(() => import('@/components/HttpTestInfo/index.vue'));
const SocketForm = defineAsyncComponent(() => import('./components/socketForm.vue'));
const MonacoEditor = defineAsyncComponent(() => import('@/components/MonacoEditor/index.vue'));
const Toolbar = defineAsyncComponent(() => import('@/components/layout/toolbar/index.vue'));
const MessageList = defineAsyncComponent(() => import('./components/messageList.vue'));
const Save = defineAsyncComponent(() => import('./components/save.vue'));
const Config = defineAsyncComponent(() => import('./components/config.vue'));
const Agent = defineAsyncComponent(() => import('@/views/apis/services/components/agent/index.vue'));
const saveUnarchived = defineAsyncComponent(() => import('./components/saveUnarchived.vue'));
const ShareListVue = defineAsyncComponent(() => import('@/components/share/list.vue'));

const { API_EXTENSION_KEY } = apiUtils;
const { wsMessageKey, requestSettingKey } = API_EXTENSION_KEY;

const erd = elementResizeDetector({ strategy: 'scroll' });
const { toClipboard } = useClipboard();

const queryParamRef = ref();
const headerParamRef = ref();
const mainSocketRef = ref();

const auths = ref<string[]>([]);
const languageOpt = ['json', 'html', 'text', 'yaml', 'typescript'].map(i => ({ value: i, label: i }));
const { valueKey } = API_EXTENSION_KEY;
type lge = 'json'|'html'|'text'|'yaml'|'typescript';
const language = ref<lge>('text');
const socketTarget = ref();
const msgListRef = ref();
const toolbarRef = ref();
const isConnected = ref(false);
const isClosing = ref(false);
const connecting = ref(false);
const connectedDate = ref();
const msgKeyWords = ref();
const msgType = ref();

let currentConnectUrl;
const connect = async () => {
  connectedDate.value = dayjs().format(DATE_TIME_FORMAT);
  const params = await packageParams();
  try {
    // eslint-disable-next-line no-new
    // new URL(currentServer.value.url);
    const url = new URL(currentServer.value.url + endpoint.value);
    let search = url.search;
    const queryStr = await getQueryStr();
    if (search) {
      search = search + '&' + queryStr;
    } else {
      search = '?' + queryStr;
    }
    url.search = search;
    currentConnectUrl = url.toString();
  } catch {
    notification.warning(t('service.apiWebSocket.messages.invalidUrl'));
    return;
  }
  if (WS.value) {
    connecting.value = true;
    const resolvedRefModels = {
      ...headerParamRef.value?.getModelResolve(),
      ...queryParamRef.value?.getModelResolve()
    };
    // clientId 用于鉴别是否为当前接口所产生的代理连接
    WS.value.send(JSON.stringify({ messageType: 'WebsocketRequestProxy', type: 'CONNECTION', clientId: props.pid, parameters: params.parameters, server: params.currentServer, endpoint: params.endpoint, protocol: params.protocol, resolvedRefModels }));
    return;
  }
  loadWs();
};

const closeConnect = (changeProxy = false) => {
  if (WS.value) {
    isClosing.value = true;
    WS.value.send(JSON.stringify({ messageType: 'WebsocketRequestProxy', type: 'DISCONNECTION', clientId: props.pid }));
    if (changeProxy) {
      isClosing.value = false;
      isConnected.value = false;
      msgList.value.push({ type: 'close', content: 'closed', size: calculateStringSize('closed'), date: dayjs().format(DATE_TIME_FORMAT), showContent: false, key: utils.uuid('key') });
    }
    return;
  }
  isClosing.value = true;
  socketTarget.value.close(1000);
  isConnected.value = false;
};

const disableConnect = computed(() => {
  return !currentServer.value.url || !auths.value.includes(ApiPermission.DEBUG);
});

const apiInfo = reactive({
  currentServer: { url: 'ws://' },
  parameters: [],
  status: '',
  method: 'GET',
  [requestSettingKey]: {
    maxReconnections: 3,
    reconnectionInterval: 200, // max delay in ms between reconnections
    connectTimeout: 60000
  },
  requestBody: {
    [wsMessageKey]: ''
  },
  endpoint: undefined
});
const queryParams = ref<FormData[]>([]);
const header = ref<FormData[]>([]);

const drawerRef = ref();
const activeDrawerKey = ref();
const endpoint = ref('');
const currentServer = ref({ url: 'ws://' });
const defaultCurrentServer = ref({ url: 'ws://' });
const changeQuery = (data) => {
  queryParams.value = data;
};

const getQueryStr = async () => {
  const queryObjList = getNameValue.getQueryParamFromApi(queryParams.value);
  const [datas] = await apiUtils.replaceFuncValue({ parameter: [queryObjList] });
  return datas[0].map(item => `${item.name}=${item[valueKey]}`).join('&');
};

const changeHeader = (data) => {
  header.value = data;
};

const activeKey = ref('message');
const showParams = ref(true);
const content = ref();
const WS = ref<WebSocket|undefined>();

const navs = computed(() => [
  props.valueObj.unarchived && {
    icon: 'icon-baocundaoweiguidang',
    name: t('service.apiWebSocket.navigation.saveToUnarchived'),
    key: 'saveUnarchived'
  },
  {
    icon: 'icon-baocun',
    name: props.valueObj?.unarchived ? t('actions.archiveToService') : t('actions.save'),
    key: 'save'
  },
  !props.valueObj.unarchived && {
    icon: 'icon-bianliang',
    name: t('common.variables'),
    key: 'variable'
  },
  // !props.valueObj.unarchived && {
  //   icon: 'icon-fenxiang',
  //   name: '分享',
  //   key: 'share'
  // },
  {
    icon: 'icon-jiekoudaili',
    name: t('service.apiWebSocket.navigation.agent'),
    key: 'agent'
  }
].filter(Boolean));
const barHeight = ref(30);
const moving = ref(false);
const barMaxHeight = ref(500);
const barMenus = ref([{
  name: t('service.apiWebSocket.labels.responseMessage'),
  value: 'message'
}]);

const msgList = ref<Message[]>([]);

const loadWs = async () => {
  connecting.value = true;
  const config = {
    maxRetries: apiInfo[requestSettingKey].maxReconnections,
    maxReconnectionDelay: apiInfo[requestSettingKey].reconnectionInterval,
    minReconnectionDelay: apiInfo[requestSettingKey].reconnectionInterval,
    connectionTimeout: apiInfo[requestSettingKey].connectTimeout
  };
  socketTarget.value = new ReconnectingWebSocket(currentConnectUrl, [], config);
  socketTarget.value.addEventListener('open', () => {
    isConnected.value = true;
    connecting.value = false;
    isClosing.value = false;
    const content = 'WebSocket connection success, address:' + currentConnectUrl;
    msgList.value.push({ type: 'connect', content: content, size: calculateStringSize(content), date: dayjs().format(DATE_TIME_FORMAT), showContent: false, key: utils.uuid('key') });
  });
  socketTarget.value.addEventListener('message', (evt) => {
    const response = evt.data;
    receiveMessage(response);
  });
  socketTarget.value.addEventListener('error', (error) => {
    // readyState.value = socketTarget.value?.readyState || 3;
    notification.warning('WebSocket error: ' + error.message);
    isClosing.value = true;
    socketTarget.value.close(1000);
  });
  socketTarget.value.addEventListener('close', (data) => {
    isConnected.value = false;
    isClosing.value = false;
    connecting.value = false;
    const content = `WebSocket connection closed, code: ${data.code}, reason: ${data.reason || 'None'}`;
    msgList.value.push({ type: 'close', content: content, size: calculateStringSize(content), date: dayjs().format(DATE_TIME_FORMAT), showContent: false, key: utils.uuid('key') });
  });
};

const packageParams = async () => {
  let protocol;
  if (currentServer.value.url.startsWith('ws://')) {
    protocol = 'ws';
  } else if (currentServer.value.url.startsWith('wss://')) {
    protocol = 'wss';
  } else {
    try {
      const url = new URL(currentServer.value.url);
      protocol = url.protocol.replace(':', '');
    } catch {
      protocol = '';
    }
  }
  const parameters = [...queryParams.value.filter(i => !!i.name), ...header.value.filter(i => i.name)];
  const params = {
    serviceId: props.valueObj.serviceId || undefined,
    serviceName: props.valueObj.serviceName || undefined,
    targetType: props.valueObj.targetType || undefined,
    ...apiInfo,
    currentServer: currentServer.value,
    endpoint: endpoint.value,
    parameters,
    id: props.id || undefined,
    protocol,
    requestBody: {
      [wsMessageKey]: content.value
    }
  };
  return params;
};

const save = async () => {
  const params = await packageParams();
  if (props.valueObj.unarchived) {
    drawerRef.value.open('saveUnarchived');
  } else {
    const [error] = await apis.updateApi([params]);
    if (error) {
      return;
    }
    notification.success(t('service.apiWebSocket.messages.updateApiSuccess'));
  }
};

const archived = () => {
  drawerRef.value.open('save');
};

const loadApiAuth = async () => {
  if (props.valueObj.unarchived) {
    return;
  }
  const [error, resp] = await apis.getCurrentAuth(props.id);
  if (error) {
    return;
  }
  if (!resp.data.serviceAuth) {
    auths.value = enumUtils.getEnumValues(ApiPermission);
    if (apiInfo.status === ApiPermission.RELEASE) {
      auths.value = auths.value.filter(i => i !== ApiPermission.MODIFY);
    }
    return;
  }
  auths.value = (resp.data?.permissions || []).map(i => i.value);
  if (apiInfo.status === ApiPermission.RELEASE) {
    auths.value = auths.value.filter(i => i !== ApiPermission.MODIFY);
  }
};

const loadApiInfo = async () => {
  if (!props.id) {
    return;
  }
  const [error, resp] = await (props.valueObj.unarchived ? apis.getUnarchivedApiDetail(props.id) : apis.getApiDetail(props.id));
  if (error) {
    return;
  }
  for (const i in resp.data) {
    apiInfo[i] = resp.data[i];
  }
  endpoint.value = apiInfo.endpoint;
  apiInfo.status = resp.data.status?.value;
  queryParams.value = (apiInfo.parameters || []).filter(i => i.in === 'query');
  header.value = (apiInfo.parameters || []).filter(i => i.in === 'header');
  if (props.valueObj.unarchived) {
    currentServer.value = apiInfo.currentServer;
  } else {
    currentServer.value = apiInfo.availableServers?.[0] || { url: '' };
  }
  defaultCurrentServer.value = JSON.parse(JSON.stringify(currentServer.value));
  content.value = apiInfo.requestBody?.[wsMessageKey] || '';
};

const calculateStringSize = (str: string): string => {
  const blob = new Blob([str], { type: 'text/plain' });
  return formatBytes(blob.size);
};

const postMessage = () => {
  if (WS.value) {
    WS.value.send(JSON.stringify({ messageType: 'WebsocketRequestProxy', type: 'SEND_MESSAGE', clientId: props.pid, rawContent: content.value }));
    return;
  } else {
    socketTarget.value.send(content.value);
  }
  toolbarRef.value.handleSelected(barMenus.value[0]);
  msgList.value.push({ type: 'send', content: content.value, size: calculateStringSize(content.value), date: dayjs().format(DATE_TIME_FORMAT), showContent: false, key: utils.uuid('key') });
};

const receiveMessage = (data: string) => {
  toolbarRef.value.handleSelected(barMenus.value[0]);
  msgList.value.push({ type: 'receive', content: data, size: calculateStringSize(data), date: dayjs().format(DATE_TIME_FORMAT), showContent: false, key: utils.uuid('key') });
};

const onResponse = async () => {
  const response = JSON.parse(props.response);
  if (response.type === 'CONNECTION_MESSAGE') { // 链接结果
    connecting.value = false;
    if (response.success === true) {
      isConnected.value = true;
      toolbarRef.value.handleSelected(barMenus.value[0]);
      const content = JSON.stringify({
        url: currentConnectUrl
      });
      msgList.value.push({ type: 'connect', content: content, size: calculateStringSize(content), date: dayjs().format(DATE_TIME_FORMAT), showContent: false, key: utils.uuid('key') });
    } else {
      notification.warning(t('service.apiWebSocket.messages.connectionFailed') + ': ' + (response.rawContent || ''));
      msgList.value.push({ type: 'connectErr', content: response.rawContent, size: calculateStringSize(response.rawContent), date: dayjs().format(DATE_TIME_FORMAT), showContent: false, key: utils.uuid('key') });
    }
  }

  if (response.type === 'SEND_RESULT_MESSAGE') { // 发送结果
    // 发送结果
    if (response.success === true) {
      toolbarRef.value.handleSelected(barMenus.value[0]);
      msgList.value.push({ type: 'send', content: content.value, size: calculateStringSize(content.value), date: dayjs().format(DATE_TIME_FORMAT), showContent: false, key: utils.uuid('key') });
    } else {
      notification.warning(t('service.apiWebSocket.messages.sendFailed') + ' ' + (response.rawContent || ''));
      msgList.value.push({ type: 'sendErr', content: response.rawContent, size: calculateStringSize(response.rawContent), date: dayjs().format(DATE_TIME_FORMAT), showContent: false, key: utils.uuid('key') });
    }
  }

  if (response.type === 'RECEIVED_MESSAGE') { // 接收消息成功
    receiveMessage(response.rawContent);
  }

  if (response.type === 'CLOSE_MESSAGE') { // 关闭 ws 链接
    if (response.success) {
      isConnected.value = false;
      isClosing.value = false;
      connecting.value = false;
      toolbarRef.value.handleSelected(barMenus.value[0]);
      msgList.value.push({ type: 'close', content: response.rawContent, size: calculateStringSize(response.rawContent), date: dayjs().format(DATE_TIME_FORMAT), showContent: false, key: utils.uuid('key') });
    } else {
      notification.warning(t('service.apiWebSocket.messages.closeFailed') + '：' + (response.rawContent || ''));
      msgList.value.push({ type: 'closeErr', content: response.rawContent, size: calculateStringSize(response.rawContent), date: dayjs().format(DATE_TIME_FORMAT), showContent: false, key: utils.uuid('key') });
    }
  }
};

const delAllMsg = () => {
  msgList.value = [];
};

const resizeHandler = debounce(duration.resize, () => {
  barMaxHeight.value = mainSocketRef.value.clientHeight;
});

const copyUrl = async () => {
  const url = currentServer.value.url + endpoint.value;
  let search = url.split('?')[1];
  const queryStr = await getQueryStr();
  if (search) {
    search = search + '&' + queryStr;
  } else {
    search = '?' + queryStr;
  }
  await toClipboard(url + search);
  notification.success(t('service.apiWebSocket.messages.copyUrlSuccess'));
};

watch(() => props.responseCount, () => {
  if (props.uuid === props.pid) {
    onResponse();
  }
});

let changeClientTimer;

watch(() => props.ws, newValue => {
  if (isConnected.value) {
    closeConnect(true);
    changeClientTimer = setInterval(() => {
      if (isConnected.value) {
        return;
      }
      WS.value = newValue;
      connect();
      clearInterval(changeClientTimer);
      changeClientTimer = undefined;
    }, 500);
    return;
  }
  WS.value = newValue;
}, {
  immediate: true
});

const onUriBlur = () => {
  const uriStrs = endpoint.value.split('?');
  if (uriStrs.length > 1 && !!uriStrs[1]) {
    const jsonQuery = qs.parse(uriStrs[1]);
    Object.keys(jsonQuery).forEach(key => {
      if (typeof jsonQuery[key] === 'string') {
        queryParamRef.value.addItem({ name: key, [valueKey]: jsonQuery[key], schema: { type: 'string' } });
      } else {
        if (Object.prototype.toString.call(jsonQuery[key]) === '[object Object]') {
          queryParamRef.value.addItem({ name: key, [valueKey]: jsonQuery[key], schema: { type: 'object' } });
        } else {
          queryParamRef.value.addItem({ name: key, [valueKey]: jsonQuery[key], schema: { type: 'array' } });
        }
      }
    });
    endpoint.value = uriStrs[0];
    activeKey.value = 'query';
  }
};

onMounted(async () => {
  await loadApiInfo();
  loadApiAuth();
  erd.listenTo(mainSocketRef.value, resizeHandler);
});

onBeforeUnmount(() => {
  erd.removeListener(mainSocketRef.value, resizeHandler);
});

provide('close', () => {
  drawerRef.value.close();
});
provide('auths', computed(() => auths.value));
provide('apiBaseInfo', apiInfo);
provide('isUnarchived', computed(() => props.valueObj.unarchived));
</script>
<template>
  <div
    ref="mainSocketRef"
    class="h-full flex"
    :class="{'select-none': moving}">
    <div class="flex flex-1 flex-col min-h-0 min-w-0">
      <div class="flex-1 min-h-0 px-4 pt-4 flex flex-col overflow-hidden">
        <div class="flex items-center w-full ">
          <div class="border flex flex-1 rounded border-theme-text-box">
            <ApiServer
              :id="apiInfo.id"
              v-model:currentServer="currentServer"
              :availableServers="apiInfo.availableServers || []"
              :defaultCurrentServer="defaultCurrentServer"
              :readonly="isConnected" />
            <Input
              v-model:value="endpoint"
              :readonly="isConnected"
              :placeholder="t('service.apiWebSocket.form.uriPlaceholder')"
              class="border-0 flex-1"
              @blur="onUriBlur" />
          </div>
          <Button
            v-if="isConnected"
            :loading="isClosing"
            class="ml-2 !bg-orange-500 text-white border-orange-500 hover:text-white hover:bg-orange-500 hover:border-orange-500"
            @click="closeConnect">
            <Icon icon="icon-duankai" class="mr-2" />{{ t('service.apiWebSocket.actions.disconnect') }}
          </Button>
          <Button
            v-else
            type="primary"
            class="ml-2"
            :loading="connecting"
            :disabled="disableConnect"
            @click="connect">
            <Icon icon="icon-lianjie2" class="mr-2" />{{ t('service.apiWebSocket.actions.connect') }}
          </Button>
          <template v-if="props.valueObj?.unarchived">
            <Button
              v-if="props.valueObj.id"
              class="ml-2"
              @click="save">
              {{ t('actions.save') }}
            </Button>
            <Button
              v-else
              class="ml-2"
              @click="save">
              {{ t('service.apiWebSocket.actions.saveToUnarchived') }}
            </Button>
            <Button
              class="ml-2"
              @click="archived">
              {{ t('service.apiWebSocket.actions.archive') }}
            </Button>
            <Button
              class="ml-2"
              @click="copyUrl">
              {{ t('service.apiWebSocket.actions.copyUrl') }}
            </Button>
          </template>
          <Button
            v-else
            class="ml-2"
            :disabled="!auths.includes(ApiPermission.MODIFY)"
            @click="save">
            <Icon icon="icon-baocun" class="mr-2" />{{ t('actions.save') }}
          </Button>
        </div>
        <div class="flex-1 overflow-auto flex flex-col justify-between">
          <Tabs
            v-model:activeKey="activeKey"
            class="overflow-y-auto"
            :class="{'hide-pane': !showParams}"
            size="small">
            <TabPane key="message" :tab="t('service.apiWebSocket.labels.sendMessage')">
              <MonacoEditor
                v-model:value="content"
                :language="language"
                class="w-full h-75 mt-2 border pt-2 rounded" />
              <div class="flex justify-between pt-2">
                <Select
                  v-model:value="language"
                  :options="languageOpt"
                  class="w-40" />
                <Button
                  type="primary"
                  class="h-7 py-0 px-2"
                  :disabled="!isConnected"
                  @click="postMessage">
                  <Icon icon="icon-fasong" class="mr-2" />
                  {{ t('actions.send') }}
                </Button>
              </div>
            </TabPane>
            <TabPane
              key="query"
              :tab="t('service.apiWebSocket.labels.queryParams')"
              :forceRender="true">
              <SocketForm
                ref="queryParamRef"
                key="query"
                in="query"
                :data="queryParams"
                @change="changeQuery" />
            </TabPane>
            <TabPane
              key="header"
              :tab="t('service.apiWebSocket.labels.requestHeader')"
              :forceRender="true">
              <SocketForm
                ref="headerParamRef"
                key="header"
                :data="header"
                in="header"
                @change="changeHeader" />
            </TabPane>
            <TabPane key="config" :tab="t('service.apiWebSocket.labels.settings')">
              <Config
                :id="apiInfo.id"
                v-model:value="apiInfo[requestSettingKey]"
                class="mt-2" />
            </TabPane>
          </Tabs>
        </div>
      </div>
      <Toolbar
        ref="toolbarRef"
        v-model:height="barHeight"
        v-model:moving="moving"
        :max-height="barMaxHeight"
        :menus="barMenus">
        <template #actions>
          <div v-show="isConnected" class="text-3 text-text-content mr-3 flex items-center">
            <span class="mr-7.5">{{ connectedDate }}</span>
            <Icon icon="icon-duihao" class="text-status-success" />
            <span class="ml-1 leading-3">{{ t('service.apiWebSocket.status.connected') }}</span>
          </div>
        </template>
        <template #content="{activeMenu}">
          <template v-if="activeMenu === 'message'">
            <div class="px-6 py-2 flex-1 overflow-auto">
              <div class="flex items-center mb-4">
                <Input
                  v-model:value="msgKeyWords"
                  class="w-50"
                  :placeholder="t('service.apiWebSocket.form.searchMessagePlaceholder')"
                  size="small">
                  <template #suffix>
                    <Icon icon="icon-sousuo" class="text-4 text-theme-sub-content" />
                  </template>
                </Input>
                <Select
                  v-model:value="msgType"
                  class="w-50 ml-2"
                  :placeholder="t('service.apiWebSocket.form.allMessagesPlaceholder')"
                  size="small"
                  :allowClear="true"
                  :options="[{value: 'receive', label: t('service.apiWebSocket.options.receiveMessage')}, {value: 'send', label: t('service.apiWebSocket.options.sendMessage')}]" />
                <span class="flex-1"></span>
                <Button
                  class="h-7 leading-7 py-0 px-3 text-3"
                  @click="delAllMsg">
                  <Icon icon="icon-qingsao" class="mr-2 text-3 -mt-0.5" />{{ t('actions.clear') }}
                </Button>
              </div>
              <MessageList
                ref="msgListRef"
                :data="msgList"
                :keyword="msgKeyWords"
                :msgType="msgType" />
            </div>
          </template>
        </template>
      </Toolbar>
    </div>
    <Drawer
      ref="drawerRef"
      v-model:activeKey="activeDrawerKey"
      :menuItems="navs">
      <template #saveUnarchived>
        <saveUnarchived
          v-if="activeDrawerKey==='saveUnarchived'"
          class="mt-2 pr-5"
          v-bind="apiInfo"
          :tabKey="props.pid"
          :packageParams="packageParams" />
      </template>
      <template #save>
        <Save
          v-if="activeDrawerKey==='save'"
          class="mt-2 pr-5"
          v-bind="apiInfo"
          :tabKey="props.pid"
          :packageParams="packageParams"
          @ok="loadApiInfo" />
      </template>
      <template #agent>
        <Agent v-if="activeDrawerKey==='agent'" class="pr-5" />
      </template>
      <template #indicator>
        <Indicator
          v-if="activeDrawerKey==='indicator'"
          :id="props.id"
          type="API"
          class="pr-5 pt-2" />
      </template>
      <template #testInfo>
        <HttpTestInfo
          :id="props.id"
          type="WEBSOCKET"
          class="pr-5 pt-3" />
      </template>
      <template #share>
        <ShareListVue
          v-if="activeDrawerKey === 'share'"
          :id="props.id"
          :disabled="!auths.includes(ApiPermission.SHARE)"
          class="pr-5 pt-2"
          type="API" />
      </template>
    </Drawer>
  </div>
</template>
<style scoped>
.hide-pane :deep(.ant-tabs-content-holder) {
  height: 0;
  overflow: hidden;
}
</style>
