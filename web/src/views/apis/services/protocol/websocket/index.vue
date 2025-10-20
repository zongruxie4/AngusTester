<script setup lang="ts">
import { computed, defineAsyncComponent, onBeforeUnmount, onMounted, provide, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import dayjs from 'dayjs';
import { Drawer, Icon, Input, notification, Select } from '@xcan-angus/vue-ui';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { utils, duration, enumUtils, HttpMethod } from '@xcan-angus/infra';
import qs from 'qs';
import elementResizeDetector from 'element-resize-detector';
import useClipboard from 'vue-clipboard3';
import ReconnectingWebSocket from 'reconnecting-websocket';
import apiUtils from '@/utils/apis';

import { apis } from '@/api/tester';
import { ApiPermission } from '@/enums/enums';
import { formatBytes } from '@/utils/utils';

import { FormData, Message } from './types';
import { debounce } from 'throttle-debounce';
import { DATE_TIME_FORMAT } from '@/utils/constant';

// Lazy-loaded components for better performance
const ApiServer = defineAsyncComponent(() => import('@/views/apis/services/protocol/websocket/Server.vue'));
const Indicator = defineAsyncComponent(() => import('@/components/Indicator/index.vue'));
const HttpTestInfo = defineAsyncComponent(() => import('@/components/HttpTestInfo/index.vue'));
const SocketForm = defineAsyncComponent(() => import('@/views/apis/services/protocol/websocket/SocketForm.vue'));
const MonacoEditor = defineAsyncComponent(() => import('@/components/MonacoEditor/index.vue'));
const Toolbar = defineAsyncComponent(() => import('@/components/layout/toolbar/index.vue'));
const MessageList = defineAsyncComponent(() => import('@/views/apis/services/protocol/websocket/MessageList.vue'));
const Save = defineAsyncComponent(() => import('@/views/apis/services/protocol/websocket/Save.vue'));
const Config = defineAsyncComponent(() => import('@/views/apis/services/protocol/websocket/Config.vue'));
const RequestProxy = defineAsyncComponent(() => import('@/views/config/proxy/EditableRequestProxy.vue'));
const SaveUnarchived = defineAsyncComponent(() => import('@/views/apis/services/protocol/websocket/SaveUnarchived.vue'));
const ShareListVue = defineAsyncComponent(() => import('@/components/share/list.vue'));

interface Props {
  id: string;
  valueObj: Record<string, any>;
  uuid: string;
  ws: WebSocket;
  response: string;
  pid: string;
  responseCount: number;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  valueObj: undefined,
  uuid: undefined,
  ws: undefined,
  response: undefined,
  pid: undefined,
  responseCount: undefined
});

const { t } = useI18n();

const { API_EXTENSION_KEY } = apiUtils;
const { wsMessageKey, requestSettingKey } = API_EXTENSION_KEY;

/**
 * Element resize detector for responsive layout
 */
const elementResizeDetectorInstance = elementResizeDetector({ strategy: 'scroll' });

/**
 * Clipboard utility for copying content
 */
const { toClipboard } = useClipboard();

/**
 * Component references
 */
const queryParameterFormRef = ref();
const headerParameterFormRef = ref();
const mainWebSocketRef = ref();

/**
 * Supported editor language types
 */
type EditorLanguage = 'json' | 'html' | 'text' | 'yaml' | 'typescript';

/**
 * User permissions for API operations
 */
const userPermissions = ref<string[]>([]);

/**
 * Available language options for the editor
 */
const editorLanguageOptions = ['json', 'html', 'text', 'yaml', 'typescript'].map(lang => ({
  value: lang,
  label: lang
}));

const { valueKey } = API_EXTENSION_KEY;

const currentEditorLanguage = ref<EditorLanguage>('text');
const webSocketConnection = ref();
const messageListRef = ref();
const toolbarRef = ref();
const isWebSocketConnected = ref(false);
const isWebSocketClosing = ref(false);
const isWebSocketConnecting = ref(false);
const webSocketConnectedTimestamp = ref();
const messageSearchKeywords = ref();
const messageTypeFilter = ref();

let currentWebSocketUrl: string;

/**
 * Establishes WebSocket connection
 * <p>
 * Creates a WebSocket connection to the specified server with query parameters
 * </p>
 */
const establishWebSocketConnection = async () => {
  webSocketConnectedTimestamp.value = dayjs().format(DATE_TIME_FORMAT);
  const connectionParams = await packageApiParameters();

  try {
    const baseUrl = new URL(currentServer.value.url + endpoint.value);
    let queryString = baseUrl.search;
    const queryParameters = await buildQueryString();

    if (queryString) {
      queryString = queryString + '&' + queryParameters;
    } else {
      queryString = '?' + queryParameters;
    }
    baseUrl.search = queryString;
    currentWebSocketUrl = baseUrl.toString();
  } catch (error) {
    notification.warning(t('service.apiWebSocket.messages.invalidUrl'));
    console.error('Invalid WebSocket URL:', error);
    return;
  }

  if (WS.value) {
    isWebSocketConnecting.value = true;
    const resolvedReferenceModels = {
      ...headerParameterFormRef.value?.getModelResolve(),
      ...queryParameterFormRef.value?.getModelResolve()
    };

    // Send connection request through proxy
    WS.value.send(JSON.stringify({
      messageType: 'WebsocketRequestProxy',
      type: 'CONNECTION',
      clientId: props.pid,
      parameters: connectionParams.parameters,
      server: connectionParams.currentServer,
      endpoint: connectionParams.endpoint,
      protocol: connectionParams.protocol,
      resolvedRefModels: resolvedReferenceModels
    }));
    return;
  }

  await initializeWebSocketConnection();
};

/**
 * Closes WebSocket connection
 * <p>
 * Closes the current WebSocket connection, optionally changing proxy
 * </p>
 * @param changeProxy - Whether to change proxy after closing
 */
const closeWebSocketConnection = (changeProxy = false) => {
  if (WS.value) {
    isWebSocketClosing.value = true;
    WS.value.send(JSON.stringify({
      messageType: 'WebsocketRequestProxy',
      type: 'DISCONNECTION',
      clientId: props.pid
    }));

    if (changeProxy) {
      isWebSocketClosing.value = false;
      isWebSocketConnected.value = false;
      addMessageToList({
        type: 'close',
        content: 'closed',
        size: calculateStringSize('closed'),
        date: dayjs().format(DATE_TIME_FORMAT),
        showContent: false,
        key: utils.uuid('key')
      });
    }
    return;
  }

  isWebSocketClosing.value = true;
  webSocketConnection.value.close(1000);
  isWebSocketConnected.value = false;
};

/**
 * Computed property to determine if connect button should be disabled
 * <p>
 * Disables connect button when server URL is empty or user lacks debug permission
 * </p>
 */
const isConnectButtonDisabled = computed(() => {
  return !currentServer.value.url || !userPermissions.value.includes(ApiPermission.DEBUG);
});

/**
 * API information and configuration
 */
const apiConfiguration = reactive({
  currentServer: { url: 'ws://' },
  parameters: [],
  status: '',
  method: HttpMethod.GET,
  requestBody: {
    [wsMessageKey]: ''
  },
  endpoint: undefined,
  [requestSettingKey]: {
    maxReconnections: 3,
    reconnectionInterval: 200, // max delay in ms between reconnections
    connectTimeout: 60000
  }
});

const queryParameters = ref<FormData[]>([]);
const headerParameters = ref<FormData[]>([]);

const drawerRef = ref();
const activeDrawerKey = ref();

const endpoint = ref('');
const currentServer = ref({ url: 'ws://' });
const defaultCurrentServer = ref({ url: 'ws://' });

/**
 * Updates query parameters
 * <p>
 * Handles changes to query parameters from the form
 * </p>
 * @param data - Updated query parameters
 */
const updateQueryParameters = (data: FormData[]) => {
  queryParameters.value = data;
};

/**
 * Builds query string from parameters
 * <p>
 * Constructs a query string from the current query parameters
 * </p>
 * @returns Promise resolving to query string
 */
const buildQueryString = async (): Promise<string> => {
  const queryObjectList = (apiUtils as any).getQueryParamFromApi(queryParameters.value);
  const [processedData] = await (apiUtils as any).replaceFuncValue({ parameter: [queryObjectList] });
  return processedData[0].map((item: any) => `${item.name}=${item[valueKey]}`).join('&');
};

/**
 * Updates header parameters
 * <p>
 * Handles changes to header parameters from the form
 * </p>
 * @param data - Updated header parameters
 */
const updateHeaderParameters = (data: FormData[]) => {
  headerParameters.value = data;
};

const activeTabKey = ref('message');
const showParametersPanel = ref(true);
const messageContent = ref();

const WS = ref<WebSocket | undefined>();

const toolbarHeight = ref(30);
const isToolbarMoving = ref(false);
const maxToolbarHeight = ref(500);

/**
 * Toolbar menu options
 */
const toolbarMenuOptions = ref([{
  name: t('service.apiWebSocket.labels.responseMessage'),
  value: 'message'
}]);

/**
 * List of WebSocket messages
 */
const webSocketMessages = ref<Message[]>([]);

/**
 * Initializes WebSocket connection
 * <p>
 * Creates a new ReconnectingWebSocket instance with configuration and event handlers
 * </p>
 */
const initializeWebSocketConnection = async () => {
  isWebSocketConnecting.value = true;
  const requestSettings = apiConfiguration[requestSettingKey] as any;
  const connectionConfig = {
    maxRetries: requestSettings?.maxReconnections || 3,
    maxReconnectionDelay: requestSettings?.reconnectionInterval || 200,
    minReconnectionDelay: requestSettings?.reconnectionInterval || 200,
    connectionTimeout: requestSettings?.connectTimeout || 60000
  };

  webSocketConnection.value = new ReconnectingWebSocket(currentWebSocketUrl, [], connectionConfig);

  webSocketConnection.value.addEventListener('open', () => {
    isWebSocketConnected.value = true;
    isWebSocketConnecting.value = false;
    isWebSocketClosing.value = false;
    const successMessage = 'WebSocket connection success, address:' + currentWebSocketUrl;
    addMessageToList({
      type: 'connect',
      content: successMessage,
      size: calculateStringSize(successMessage),
      date: dayjs().format(DATE_TIME_FORMAT),
      showContent: false,
      key: utils.uuid('key')
    });
  });

  webSocketConnection.value.addEventListener('message', (event) => {
    const receivedData = event.data;
    handleReceivedMessage(receivedData);
  });

  webSocketConnection.value.addEventListener('error', (error) => {
    notification.warning('WebSocket error: ' + error.message);
    isWebSocketClosing.value = true;
    webSocketConnection.value.close(1000);
  });

  webSocketConnection.value.addEventListener('close', (closeEvent) => {
    isWebSocketConnected.value = false;
    isWebSocketClosing.value = false;
    isWebSocketConnecting.value = false;
    const closeMessage = `WebSocket connection closed, code: ${closeEvent.code}, reason: ${closeEvent.reason || 'None'}`;
    addMessageToList({
      type: 'close',
      content: closeMessage,
      size: calculateStringSize(closeMessage),
      date: dayjs().format(DATE_TIME_FORMAT),
      showContent: false,
      key: utils.uuid('key')
    });
  });
};

/**
 * Adds a message to the message list
 * <p>
 * Helper function to add messages to the WebSocket message list
 * </p>
 * @param message - Message object to add
 */
const addMessageToList = (message: Message) => {
  webSocketMessages.value.push(message);
};

/**
 * Calculates the size of a string in bytes
 * <p>
 * Helper function to calculate the byte size of a string for display
 * </p>
 * @param str - String to calculate size for
 * @returns Formatted size string
 */
const calculateStringSize = (str: string): string => {
  const blob = new Blob([str], { type: 'text/plain' });
  return formatBytes(blob.size);
};

/**
 * Packages API parameters for WebSocket connection
 * <p>
 * Prepares all necessary parameters for establishing a WebSocket connection
 * </p>
 * @returns Promise resolving to packaged parameters
 */
const packageApiParameters = async () => {
  let protocol: string;
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

  const allParameters = [
    ...queryParameters.value.filter(param => !!param.name),
    ...headerParameters.value.filter(param => param.name)
  ];

  return {
    serviceId: props.valueObj.serviceId || undefined,
    serviceName: props.valueObj.serviceName || undefined,
    targetType: props.valueObj.targetType || undefined,
    ...apiConfiguration,
    currentServer: currentServer.value,
    endpoint: endpoint.value,
    parameters: allParameters,
    id: props.id || undefined,
    protocol,
    requestBody: {
      [wsMessageKey]: messageContent.value
    }
  };
};

/**
 * Saves the current API configuration
 * <p>
 * Saves the API configuration either to unarchived list or updates existing API
 * </p>
 */
const saveApiConfiguration = async () => {
  const apiParams = await packageApiParameters();
  if (props.valueObj.unarchived) {
    drawerRef.value.open('saveUnarchived');
  } else {
    const [error] = await apis.updateApi([apiParams]);
    if (error) {
      console.error('Failed to update API:', error);
      return;
    }
    notification.success(t('service.apiWebSocket.messages.updateApiSuccess'));
  }
};

/**
 * Opens the archive dialog
 * <p>
 * Opens the drawer to save API to archived services
 * </p>
 */
const openArchiveDialog = () => {
  drawerRef.value.open('save');
};

/**
 * Loads API permissions for the current user
 * <p>
 * Fetches and sets user permissions for API operations
 * </p>
 */
const loadApiPermissions = async () => {
  if (props.valueObj.unarchived) {
    return;
  }

  const [error, response] = await apis.getCurrentAuth(props.id);
  if (error) {
    console.error('Failed to load API permissions:', error);
    return;
  }

  if (!response.data.serviceAuth) {
    userPermissions.value = enumUtils.getEnumValues(ApiPermission);
    if (apiConfiguration.status === ApiPermission.RELEASE) {
      userPermissions.value = userPermissions.value.filter(permission => permission !== ApiPermission.MODIFY);
    }
    return;
  }

  userPermissions.value = (response.data?.permissions || []).map(permission => permission.value);
  if (apiConfiguration.status === ApiPermission.RELEASE) {
    userPermissions.value = userPermissions.value.filter(permission => permission !== ApiPermission.MODIFY);
  }
};

/**
 * Loads API information and configuration
 * <p>
 * Fetches API details and initializes the component with the retrieved data
 * </p>
 */
const loadApiInformation = async () => {
  if (!props.id) {
    return;
  }

  const [error, response] = await (props.valueObj.unarchived
    ? apis.getUnarchivedApiDetail(props.id)
    : apis.getApiDetail(props.id)
  );

  if (error) {
    console.error('Failed to load API information:', error);
    return;
  }

  // Update API configuration with fetched data
  for (const key in response.data) {
    apiConfiguration[key] = response.data[key];
  }

  endpoint.value = apiConfiguration.endpoint || '';
  apiConfiguration.status = response.data.status?.value || '';

  // Filter parameters by type
  const allParameters = (apiConfiguration.parameters as FormData[]) || [];
  queryParameters.value = allParameters.filter(param => param.in === 'query');
  headerParameters.value = allParameters.filter(param => param.in === 'header');

  // Set server configuration
  if (props.valueObj.unarchived) {
    currentServer.value = apiConfiguration.currentServer;
  } else {
    currentServer.value = apiConfiguration.availableServers?.[0] || { url: '' };
  }

  defaultCurrentServer.value = JSON.parse(JSON.stringify(currentServer.value));
  messageContent.value = apiConfiguration.requestBody?.[wsMessageKey] || '';
};

/**
 * Sends a message through WebSocket
 * <p>
 * Sends the current message content through either proxy or direct connection
 * </p>
 */
const sendWebSocketMessage = () => {
  if (WS.value) {
    WS.value.send(JSON.stringify({
      messageType: 'WebsocketRequestProxy',
      type: 'SEND_MESSAGE',
      clientId: props.pid,
      rawContent: messageContent.value
    }));
    return;
  } else {
    webSocketConnection.value.send(messageContent.value);
  }

  toolbarRef.value.handleSelected(toolbarMenuOptions.value[0]);
  addMessageToList({
    type: 'send',
    content: messageContent.value,
    size: calculateStringSize(messageContent.value),
    date: dayjs().format(DATE_TIME_FORMAT),
    showContent: false,
    key: utils.uuid('key')
  });
};

/**
 * Handles received WebSocket message
 * <p>
 * Processes and displays received WebSocket messages
 * </p>
 * @param data - Received message data
 */
const handleReceivedMessage = (data: string) => {
  toolbarRef.value.handleSelected(toolbarMenuOptions.value[0]);
  addMessageToList({
    type: 'receive',
    content: data,
    size: calculateStringSize(data),
    date: dayjs().format(DATE_TIME_FORMAT),
    showContent: false,
    key: utils.uuid('key')
  });
};

/**
 * Handles WebSocket proxy response messages
 * <p>
 * Processes different types of response messages from the WebSocket proxy
 * </p>
 */
const handleWebSocketProxyResponse = async () => {
  const response = JSON.parse(props.response);

  if (response.type === 'CONNECTION_MESSAGE') {
    isWebSocketConnecting.value = false;
    if (response.success === true) {
      isWebSocketConnected.value = true;
      toolbarRef.value.handleSelected(toolbarMenuOptions.value[0]);
      const connectionInfo = JSON.stringify({
        url: currentWebSocketUrl
      });
      addMessageToList({
        type: 'connect',
        content: connectionInfo,
        size: calculateStringSize(connectionInfo),
        date: dayjs().format(DATE_TIME_FORMAT),
        showContent: false,
        key: utils.uuid('key')
      });
    } else {
      notification.warning(t('status.connectionFailed') + ': ' + (response.rawContent || ''));
      addMessageToList({
        type: 'connectErr',
        content: response.rawContent,
        size: calculateStringSize(response.rawContent),
        date: dayjs().format(DATE_TIME_FORMAT),
        showContent: false,
        key: utils.uuid('key')
      });
    }
  }

  if (response.type === 'SEND_RESULT_MESSAGE') {
    if (response.success === true) {
      toolbarRef.value.handleSelected(toolbarMenuOptions.value[0]);
      addMessageToList({
        type: 'send',
        content: messageContent.value,
        size: calculateStringSize(messageContent.value),
        date: dayjs().format(DATE_TIME_FORMAT),
        showContent: false,
        key: utils.uuid('key')
      });
    } else {
      notification.warning(t('service.apiWebSocket.messages.sendFailed') + ' ' + (response.rawContent || ''));
      addMessageToList({
        type: 'sendErr',
        content: response.rawContent,
        size: calculateStringSize(response.rawContent),
        date: dayjs().format(DATE_TIME_FORMAT),
        showContent: false,
        key: utils.uuid('key')
      });
    }
  }

  if (response.type === 'RECEIVED_MESSAGE') {
    handleReceivedMessage(response.rawContent);
  }

  if (response.type === 'CLOSE_MESSAGE') {
    if (response.success) {
      isWebSocketConnected.value = false;
      isWebSocketClosing.value = false;
      isWebSocketConnecting.value = false;
      toolbarRef.value.handleSelected(toolbarMenuOptions.value[0]);
      addMessageToList({
        type: 'close',
        content: response.rawContent,
        size: calculateStringSize(response.rawContent),
        date: dayjs().format(DATE_TIME_FORMAT),
        showContent: false,
        key: utils.uuid('key')
      });
    } else {
      notification.warning(t('service.apiWebSocket.messages.closeFailed') + '：' + (response.rawContent || ''));
      addMessageToList({
        type: 'closeErr',
        content: response.rawContent,
        size: calculateStringSize(response.rawContent),
        date: dayjs().format(DATE_TIME_FORMAT),
        showContent: false,
        key: utils.uuid('key')
      });
    }
  }
};

/**
 * Clears all messages from the message list
 * <p>
 * Removes all messages from the WebSocket message list
 * </p>
 */
const clearAllMessages = () => {
  webSocketMessages.value = [];
};

/**
 * Handles window resize events
 * <p>
 * Updates toolbar maximum height when window is resized
 * </p>
 */
const handleWindowResize = debounce(duration.resize, () => {
  maxToolbarHeight.value = mainWebSocketRef.value.clientHeight;
});

/**
 * Copies the current WebSocket URL to clipboard
 * <p>
 * Constructs and copies the complete WebSocket URL including query parameters
 * </p>
 */
const copyWebSocketUrl = async () => {
  const baseUrl = currentServer.value.url + endpoint.value;
  let queryString = baseUrl.split('?')[1];
  const queryParameters = await buildQueryString();

  if (queryString) {
    queryString = queryString + '&' + queryParameters;
  } else {
    queryString = '?' + queryParameters;
  }

  await toClipboard(baseUrl + queryString);
  notification.success(t('service.apiWebSocket.messages.copyUrlSuccess'));
};

/**
 * Handles endpoint input blur event
 * <p>
 * Parses query parameters from endpoint URL and adds them to query parameters form
 * </p>
 */
const handleEndpointBlur = () => {
  const uriParts = endpoint.value.split('?');
  if (uriParts.length > 1 && !!uriParts[1]) {
    const parsedQuery = qs.parse(uriParts[1]);
    Object.keys(parsedQuery).forEach(key => {
      const value = parsedQuery[key];
      if (typeof value === 'string') {
        queryParameterFormRef.value.addItem({
          name: key,
          [valueKey]: value,
          schema: { type: 'string' }
        });
      } else {
        if (Object.prototype.toString.call(value) === '[object Object]') {
          queryParameterFormRef.value.addItem({
            name: key,
            [valueKey]: value,
            schema: { type: 'object' }
          });
        } else {
          queryParameterFormRef.value.addItem({
            name: key,
            [valueKey]: value,
            schema: { type: 'array' }
          });
        }
      }
    });
    endpoint.value = uriParts[0];
    activeTabKey.value = 'query';
  }
};

/**
 * Watches for response count changes
 * <p>
 * Handles WebSocket proxy responses when response count changes
 * </p>
 */
watch(() => props.responseCount, () => {
  if (props.uuid === props.pid) {
    handleWebSocketProxyResponse();
  }
});

/**
 * Timer for client change operations
 */
let clientChangeTimer: NodeJS.Timeout | undefined;

/**
 * Watches for WebSocket proxy changes
 * <p>
 * Handles WebSocket proxy connection changes and reconnection logic
 * </p>
 */
watch(() => props.ws, newWebSocketProxy => {
  if (isWebSocketConnected.value) {
    closeWebSocketConnection(true);
    clientChangeTimer = setInterval(() => {
      if (isWebSocketConnected.value) {
        return;
      }
      WS.value = newWebSocketProxy;
      establishWebSocketConnection();
      clearInterval(clientChangeTimer);
      clientChangeTimer = undefined;
    }, 500);
    return;
  }
  WS.value = newWebSocketProxy;
}, {
  immediate: true
});

/**
 * Component mounted lifecycle hook
 * <p>
 * Initializes the component by loading API information, permissions, and setting up resize listener
 * </p>
 */
onMounted(async () => {
  await loadApiInformation();
  loadApiPermissions();
  elementResizeDetectorInstance.listenTo(mainWebSocketRef.value, handleWindowResize);
});

/**
 * Component before unmount lifecycle hook
 * <p>
 * Cleans up resize listener when component is destroyed
 * </p>
 */
onBeforeUnmount(() => {
  elementResizeDetectorInstance.removeListener(mainWebSocketRef.value, handleWindowResize);
});

/**
 * Provides close function to child components
 */
provide('close', () => {
  drawerRef.value.close();
});
/**
 * Provides user permissions to child components
 */
provide('auths', computed(() => userPermissions.value));
/**
 * Provides API base information to child components
 */
provide('apiBaseInfo', apiConfiguration);
/**
 * Provides unarchived status to child components
 */
provide('isUnarchived', computed(() => props.valueObj.unarchived));

/**
 * Navigation menu items for the drawer
 * <p>
 * Computed property that generates navigation menu items based on current state
 * </p>
 */
const navigationMenuItems = computed(() => [
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
  {
    icon: 'icon-jiekoudaili',
    name: t('service.apiWebSocket.navigation.agent'),
    key: 'agent'
  }
].filter(Boolean));
</script>
<template>
  <div
    ref="mainWebSocketRef"
    class="h-full flex"
    :class="{'select-none': isToolbarMoving}">
    <div class="flex flex-1 flex-col min-h-0 min-w-0">
      <div class="flex-1 min-h-0 px-4 pt-4 flex flex-col overflow-hidden">
        <div class="flex items-center w-full ">
          <div class="border flex flex-1 rounded border-theme-text-box">
            <ApiServer
              :id="(apiConfiguration as any).id"
              v-model:currentServer="currentServer"
              :availableServers="(apiConfiguration as any).availableServers || []"
              :defaultCurrentServer="defaultCurrentServer"
              :readonly="isWebSocketConnected" />
            <Input
              v-model:value="endpoint"
              :readonly="isWebSocketConnected"
              :placeholder="t('service.apiWebSocket.form.uriPlaceholder')"
              class="border-0 flex-1"
              @blur="handleEndpointBlur" />
          </div>
          <Button
            v-if="isWebSocketConnected"
            :loading="isWebSocketClosing"
            class="ml-2 !bg-orange-500 text-white border-orange-500 hover:text-white hover:bg-orange-500 hover:border-orange-500"
            @click="() => closeWebSocketConnection()">
            <Icon icon="icon-duankai" class="mr-2" />{{ t('service.apiWebSocket.actions.disconnect') }}
          </Button>
          <Button
            v-else
            type="primary"
            class="ml-2"
            :loading="isWebSocketConnecting"
            :disabled="isConnectButtonDisabled"
            @click="establishWebSocketConnection">
            <Icon icon="icon-lianjie2" class="mr-2" />{{ t('service.apiWebSocket.actions.connect') }}
          </Button>
          <template v-if="props.valueObj?.unarchived">
            <Button
              v-if="props.valueObj.id"
              class="ml-2"
              @click="saveApiConfiguration">
              {{ t('actions.save') }}
            </Button>
            <Button
              v-else
              class="ml-2"
              @click="saveApiConfiguration">
              {{ t('service.apiWebSocket.actions.saveToUnarchived') }}
            </Button>
            <Button
              class="ml-2"
              @click="openArchiveDialog">
              {{ t('service.apiWebSocket.actions.archive') }}
            </Button>
            <Button
              class="ml-2"
              @click="copyWebSocketUrl">
              {{ t('service.apiWebSocket.actions.copyUrl') }}
            </Button>
          </template>
          <Button
            v-else
            class="ml-2"
            :disabled="!userPermissions.includes(ApiPermission.MODIFY)"
            @click="saveApiConfiguration">
            <Icon icon="icon-baocun" class="mr-2" />{{ t('actions.save') }}
          </Button>
        </div>
        <div class="flex-1 overflow-auto flex flex-col justify-between">
          <Tabs
            v-model:activeKey="activeTabKey"
            class="overflow-y-auto"
            :class="{'hide-pane': !showParametersPanel}"
            size="small">
            <TabPane key="message" :tab="t('service.apiWebSocket.labels.sendMessage')">
              <MonacoEditor
                v-model:value="messageContent"
                :language="currentEditorLanguage"
                class="w-full h-75 mt-2 border pt-2 rounded" />
              <div class="flex justify-between pt-2">
                <Select
                  v-model:value="currentEditorLanguage"
                  :options="editorLanguageOptions"
                  class="w-40" />
                <Button
                  type="primary"
                  class="h-7 py-0 px-2"
                  :disabled="!isWebSocketConnected"
                  @click="sendWebSocketMessage">
                  <Icon icon="icon-fasong" class="mr-2" />
                  {{ t('actions.send') }}
                </Button>
              </div>
            </TabPane>
            <TabPane
              key="query"
              :tab="t('protocol.queryParameter')"
              :forceRender="true">
              <SocketForm
                ref="queryParameterFormRef"
                key="query"
                in="query"
                :data="queryParameters"
                @change="updateQueryParameters" />
            </TabPane>
            <TabPane
              key="header"
              :tab="t('protocol.requestHeader')"
              :forceRender="true">
              <SocketForm
                ref="headerParameterFormRef"
                key="header"
                :data="headerParameters"
                in="header"
                @change="updateHeaderParameters" />
            </TabPane>
            <TabPane key="config" :tab="t('common.setting')">
              <Config
                :id="(apiConfiguration as any).id"
                v-model:value="(apiConfiguration as any)[requestSettingKey]"
                class="mt-2" />
            </TabPane>
          </Tabs>
        </div>
      </div>
      <Toolbar
        ref="toolbarRef"
        v-model:height="toolbarHeight"
        v-model:moving="isToolbarMoving"
        :max-height="maxToolbarHeight"
        :menus="toolbarMenuOptions">
        <template #actions>
          <div v-show="isWebSocketConnected" class="text-3 text-text-content mr-3 flex items-center">
            <span class="mr-7.5">{{ webSocketConnectedTimestamp }}</span>
            <Icon icon="icon-duihao" class="text-status-success" />
            <span class="ml-1 leading-3">{{ t('status.connected') }}</span>
          </div>
        </template>
        <template #content="{activeMenu}">
          <template v-if="activeMenu === 'message'">
            <div class="px-6 py-2 flex-1 overflow-auto">
              <div class="flex items-center mb-4">
                <Input
                  v-model:value="messageSearchKeywords"
                  class="w-50"
                  :placeholder="t('service.apiWebSocket.form.searchMessagePlaceholder')"
                  size="small">
                  <template #suffix>
                    <Icon icon="icon-sousuo" class="text-4 text-theme-sub-content" />
                  </template>
                </Input>
                <Select
                  v-model:value="messageTypeFilter"
                  class="w-50 ml-2"
                  :placeholder="t('service.apiWebSocket.form.allMessagesPlaceholder')"
                  size="small"
                  :allowClear="true"
                  :options="[{value: 'receive', label: t('service.apiWebSocket.options.receiveMessage')}, {value: 'send', label: t('service.apiWebSocket.options.sendMessage')}]" />
                <span class="flex-1"></span>
                <Button
                  class="h-7 leading-7 py-0 px-3 text-3"
                  @click="clearAllMessages">
                  <Icon icon="icon-qingsao" class="mr-2 text-3 -mt-0.5" />{{ t('actions.clear') }}
                </Button>
              </div>
              <MessageList
                ref="messageListRef"
                :data="webSocketMessages"
                :keyword="messageSearchKeywords"
                :msgType="messageTypeFilter" />
            </div>
          </template>
        </template>
      </Toolbar>
    </div>
    <Drawer
      ref="drawerRef"
      v-model:activeKey="activeDrawerKey"
      :menuItems="navigationMenuItems">
      <template #saveUnarchived>
        <SaveUnarchived
          v-if="activeDrawerKey==='saveUnarchived'"
          class="mt-2 pr-5"
          v-bind="(apiConfiguration as any)"
          :tabKey="props.pid"
          :packageParams="packageApiParameters" />
      </template>
      <template #save>
        <Save
          v-if="activeDrawerKey==='save'"
          class="mt-2 pr-5"
          v-bind="(apiConfiguration as any)"
          :tabKey="props.pid"
          :packageParams="packageApiParameters"
          @ok="loadApiInformation" />
      </template>
      <template #agent>
        <RequestProxy v-if="activeDrawerKey==='agent'" class="pr-5" />
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
          :type="'WEBSOCKET' as any"
          class="pr-5 pt-3" />
      </template>
      <template #share>
        <ShareListVue
          v-if="activeDrawerKey === 'share'"
          :id="props.id"
          :disabled="!userPermissions.includes(ApiPermission.SHARE)"
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
