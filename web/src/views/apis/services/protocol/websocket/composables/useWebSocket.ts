import { computed, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import dayjs from 'dayjs';
import ReconnectingWebSocket from 'reconnecting-websocket';
import qs from 'qs';
import { utils, HttpMethod, enumUtils } from '@xcan-angus/infra';
import { notification } from '@xcan-angus/vue-ui';
import useClipboard from 'vue-clipboard3';
import apiUtils from '@/utils/apis';
import { apis } from '@/api/tester';
import { ApiPermission } from '@/enums/enums';
import { formatBytes } from '@/utils/utils';
import { DATE_TIME_FORMAT } from '@/utils/constant';
import { SchemaType } from '@/types/openapi-types';
import { FormData, Message, MessageType, WebsocketProxyMessageType } from '../types';

type EditorLanguage = 'json' | 'html' | 'text' | 'yaml' | 'typescript';

export enum WebsocketProxyResponseType {
  CONNECTION_MESSAGE = 'CONNECTION_MESSAGE',
  SEND_RESULT_MESSAGE = 'SEND_RESULT_MESSAGE',
  RECEIVED_MESSAGE = 'RECEIVED_MESSAGE',
  CLOSE_MESSAGE = 'CLOSE_MESSAGE'
}

export interface WebsocketProxyResponse {
  type: WebsocketProxyResponseType;
  success?: boolean;
  rawContent?: string;
}

interface UseWebSocketDeps {
  queryParameterFormRef: any;
  headerParameterFormRef: any;
  toolbarRef: any;
}

/**
 * Composable for WebSocket testing workflow
 * <p>
 * Manages connection lifecycle, proxy-mode integration, messaging, filtering, and UI bindings.
 * </p>
 * @param props - External props including identifiers and proxy channel
 * @param deps - References to parameter forms and toolbar
 * @returns State and actions for WebSocket interactions
 */
export function useWebSocket (props: any, deps: UseWebSocketDeps) {
  const { t } = useI18n();
  const { toClipboard } = useClipboard();

  const { API_EXTENSION_KEY } = apiUtils as any;
  const { wsMessageKey, requestSettingKey, valueKey } = API_EXTENSION_KEY;

  const currentEditorLanguage = ref<EditorLanguage>('text');
  const editorLanguageOptions = ['json', 'html', 'text', 'yaml', 'typescript'].map(lang => ({ value: lang, label: lang }));

  const userPermissions = ref<string[]>([]);

  const isWebSocketConnected = ref(false);
  const isWebSocketClosing = ref(false);
  const isWebSocketConnecting = ref(false);
  const webSocketConnectedTimestamp = ref<string>('');

  const messageSearchKeywords = ref<string>('');
  const messageTypeFilter = ref<string>('');

  let currentWebSocketUrl = '';

  const apiConfiguration = reactive<any>({
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
      reconnectionInterval: 200,
      connectTimeout: 60000
    }
  });

  const queryParameters = ref<FormData[]>([]);
  const headerParameters = ref<FormData[]>([]);

  const endpoint = ref('');
  const currentServer = ref({ url: 'ws://' });
  const defaultCurrentServer = ref({ url: 'ws://' });

  const messageContent = ref<string>('');
  const WS = ref<WebSocket | undefined>();

  const toolbarMenuOptions = ref([{ name: t('service.apiWebSocket.labels.responseMessage'), value: 'message' }]);
  const webSocketMessages = ref<Message[]>([]);

  const webSocketConnection = ref<any>();

  const isConnectButtonDisabled = computed(() => {
    return !currentServer.value.url || !userPermissions.value.includes(ApiPermission.DEBUG);
  });

  const addMessageToList = (message: Message) => {
    webSocketMessages.value.push(message);
  };

  const calculateStringSize = (str: string): string => {
    const blob = new Blob([str], { type: 'text/plain' });
    return formatBytes(blob.size);
  };

  const updateQueryParameters = (data: FormData[]) => {
    queryParameters.value = data;
  };

  const updateHeaderParameters = (data: FormData[]) => {
    headerParameters.value = data;
  };

  const buildQueryString = async (): Promise<string> => {
    const queryObjectList = (apiUtils as any).getQueryParamFromApi(queryParameters.value);
    const [processedData] = await (apiUtils as any).replaceFuncValue({ parameter: [queryObjectList] });
    return processedData[0].map((item: any) => `${item.name}=${item[valueKey]}`).join('&');
  };

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
   * Initialize a raw WebSocket connection (non-proxy mode)
   * <p>
   * Uses ReconnectingWebSocket with configurable retry/timeout settings.
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
        type: MessageType.CONNECT,
        content: successMessage,
        size: calculateStringSize(successMessage),
        date: dayjs().format(DATE_TIME_FORMAT),
        showContent: false,
        key: utils.uuid('key')
      });
    });

    webSocketConnection.value.addEventListener('message', (event: any) => {
      const receivedData = event.data;
      handleReceivedMessage(receivedData);
    });

    webSocketConnection.value.addEventListener('error', (error: any) => {
      notification.warning('WebSocket error: ' + error.message);
      isWebSocketClosing.value = true;
      webSocketConnection.value.close(1000);
    });

    webSocketConnection.value.addEventListener('close', (closeEvent: any) => {
      isWebSocketConnected.value = false;
      isWebSocketClosing.value = false;
      isWebSocketConnecting.value = false;
      const closeMessage = `WebSocket connection closed, code: ${closeEvent.code}, reason: ${closeEvent.reason || 'None'}`;
      addMessageToList({
        type: MessageType.CLOSE,
        content: closeMessage,
        size: calculateStringSize(closeMessage),
        date: dayjs().format(DATE_TIME_FORMAT),
        showContent: false,
        key: utils.uuid('key')
      });
    });
  };

  /**
   * Establish a WebSocket connection
   * <p>
   * Builds the final URL with query parameters, then connects via proxy channel if available,
   * otherwise opens a direct WebSocket connection.
   * </p>
   */
  const establishWebSocketConnection = async () => {
    webSocketConnectedTimestamp.value = dayjs().format(DATE_TIME_FORMAT);
    const connectionParams = await packageApiParameters();

    try {
      const baseUrl = new URL(currentServer.value.url + endpoint.value);
      let queryString = baseUrl.search;
      const queryParametersStr = await buildQueryString();
      if (queryString) {
        queryString = queryString + '&' + queryParametersStr;
      } else {
        queryString = '?' + queryParametersStr;
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
        ...deps.headerParameterFormRef.value?.getModelResolve(),
        ...deps.queryParameterFormRef.value?.getModelResolve()
      };

      WS.value.send(JSON.stringify({
        messageType: WebsocketProxyMessageType.WEBSOCKET_REQUEST_PROXY,
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
   * Close the WebSocket connection
   * <p>
   * If running in proxy mode, notifies proxy server; otherwise closes the local connection.
   * </p>
   */
  const closeWebSocketConnection = (changeProxy = false) => {
    if (WS.value) {
      isWebSocketClosing.value = true;
      WS.value.send(JSON.stringify({
        messageType: WebsocketProxyMessageType.WEBSOCKET_REQUEST_PROXY,
        type: 'DISCONNECTION',
        clientId: props.pid
      }));

      if (changeProxy) {
        isWebSocketClosing.value = false;
        isWebSocketConnected.value = false;
        addMessageToList({
          type: MessageType.CLOSE,
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
   * Send a WebSocket message
   * <p>
   * Sends via proxy channel when present; otherwise sends through the active local socket.
   * Also records the message to the response list for display.
   * </p>
   */
  const sendWebSocketMessage = () => {
    if (WS.value) {
      WS.value.send(JSON.stringify({
        messageType: WebsocketProxyMessageType.WEBSOCKET_REQUEST_PROXY,
        type: 'SEND_MESSAGE',
        clientId: props.pid,
        rawContent: messageContent.value
      }));
      return;
    } else {
      webSocketConnection.value.send(messageContent.value);
    }

    deps.toolbarRef.value.handleSelected(toolbarMenuOptions.value[0]);
    addMessageToList({
      type: MessageType.SEND,
      content: messageContent.value,
      size: calculateStringSize(messageContent.value),
      date: dayjs().format(DATE_TIME_FORMAT),
      showContent: false,
      key: utils.uuid('key')
    });
  };

  /**
   * Handle an incoming WebSocket message
   * <p>
   * Pushes the received message into the message list and focuses the response panel.
   * </p>
   */
  const handleReceivedMessage = (data: string) => {
    deps.toolbarRef.value.handleSelected(toolbarMenuOptions.value[0]);
    addMessageToList({
      type: MessageType.RECEIVE,
      content: data,
      size: calculateStringSize(data),
      date: dayjs().format(DATE_TIME_FORMAT),
      showContent: false,
      key: utils.uuid('key')
    });
  };

  /**
   * Handle responses returned from WebSocket proxy channel
   */
  const handleWebSocketProxyResponse = async () => {
    const response = JSON.parse(props.response) as WebsocketProxyResponse;

    if (response.type === WebsocketProxyResponseType.CONNECTION_MESSAGE) {
      isWebSocketConnecting.value = false;
      if (response.success === true) {
        isWebSocketConnected.value = true;
        deps.toolbarRef.value.handleSelected(toolbarMenuOptions.value[0]);
        const connectionInfo = JSON.stringify({ url: currentWebSocketUrl });
        addMessageToList({
          type: MessageType.CONNECT,
          content: connectionInfo,
          size: calculateStringSize(connectionInfo),
          date: dayjs().format(DATE_TIME_FORMAT),
          showContent: false,
          key: utils.uuid('key')
        });
      } else {
        notification.warning(t('status.connectionFailed') + ': ' + (response.rawContent || ''));
        addMessageToList({
          type: MessageType.CONNECT_ERR,
          content: response.rawContent ?? '',
          size: calculateStringSize(response.rawContent ?? ''),
          date: dayjs().format(DATE_TIME_FORMAT),
          showContent: false,
          key: utils.uuid('key')
        });
      }
    }

    if (response.type === WebsocketProxyResponseType.SEND_RESULT_MESSAGE) {
      if (response.success === true) {
        deps.toolbarRef.value.handleSelected(toolbarMenuOptions.value[0]);
        addMessageToList({
          type: MessageType.SEND,
          content: messageContent.value,
          size: calculateStringSize(messageContent.value),
          date: dayjs().format(DATE_TIME_FORMAT),
          showContent: false,
          key: utils.uuid('key')
        });
      } else {
        notification.warning(t('service.apiWebSocket.messages.sendFailed') + ' ' + (response.rawContent || ''));
        addMessageToList({
          type: MessageType.SEND_ERR,
          content: response.rawContent ?? '',
          size: calculateStringSize(response.rawContent ?? ''),
          date: dayjs().format(DATE_TIME_FORMAT),
          showContent: false,
          key: utils.uuid('key')
        });
      }
    }

    if (response.type === WebsocketProxyResponseType.RECEIVED_MESSAGE) {
      handleReceivedMessage(response.rawContent ?? '');
    }

    if (response.type === WebsocketProxyResponseType.CLOSE_MESSAGE) {
      if (response.success) {
        isWebSocketConnected.value = false;
        isWebSocketClosing.value = false;
        isWebSocketConnecting.value = false;
        deps.toolbarRef.value.handleSelected(toolbarMenuOptions.value[0]);
        addMessageToList({
          type: MessageType.CLOSE,
          content: response.rawContent ?? '',
          size: calculateStringSize(response.rawContent ?? ''),
          date: dayjs().format(DATE_TIME_FORMAT),
          showContent: false,
          key: utils.uuid('key')
        });
      } else {
        notification.warning(t('service.apiWebSocket.messages.closeFailed') + '：' + (response.rawContent || ''));
        addMessageToList({
          type: MessageType.CLOSE_ERR,
          content: response.rawContent ?? '',
          size: calculateStringSize(response.rawContent ?? ''),
          date: dayjs().format(DATE_TIME_FORMAT),
          showContent: false,
          key: utils.uuid('key')
        });
      }
    }
  };

  const clearAllMessages = () => {
    webSocketMessages.value = [];
  };

  /**
   * Copy the final WebSocket URL (with query parameters) to clipboard
   */
  const copyWebSocketUrl = async () => {
    const baseUrl = currentServer.value.url + endpoint.value;
    let queryString = baseUrl.split('?')[1];
    const queryParametersStr = await buildQueryString();
    if (queryString) {
      queryString = queryString + '&' + queryParametersStr;
    } else {
      queryString = '?' + queryParametersStr;
    }
    await toClipboard(baseUrl + queryString);
    notification.success(t('service.apiWebSocket.messages.copyUrlSuccess'));
  };

  /**
   * Parse query string from endpoint input and populate query parameters list
   */
  const handleEndpointBlur = () => {
    const uriParts = endpoint.value.split('?');
    if (uriParts.length > 1 && !!uriParts[1]) {
      const parsedQuery: any = qs.parse(uriParts[1]);
      Object.keys(parsedQuery).forEach(key => {
        const value = (parsedQuery as any)[key];
        if (typeof value === 'string') {
          deps.queryParameterFormRef.value.addItem({
            name: key,
            [valueKey]: value,
            schema: { type: SchemaType.string }
          });
        } else {
          if (Object.prototype.toString.call(value) === '[object Object]') {
            deps.queryParameterFormRef.value.addItem({
              name: key,
              [valueKey]: value,
              schema: { type: SchemaType.object }
            });
          } else {
            deps.queryParameterFormRef.value.addItem({
              name: key,
              [valueKey]: value,
              schema: { type: SchemaType.array }
            });
          }
        }
      });
      endpoint.value = uriParts[0];
    }
  };

  /**
   * Load current user permissions for the API and adjust allowed operations accordingly
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
    userPermissions.value = (response.data?.permissions || []).map((permission: any) => permission.value);
    if (apiConfiguration.status === ApiPermission.RELEASE) {
      userPermissions.value = userPermissions.value.filter(permission => permission !== ApiPermission.MODIFY);
    }
  };

  /**
   * Load API base information and initialize local state
   */
  const loadApiInformation = async () => {
    if (!props.id) {
      return;
    }
    const [error, response] = await (props.valueObj.unarchived
      ? apis.getUnarchivedApiDetail(props.id)
      : apis.getApiDetail(props.id));
    if (error) {
      console.error('Failed to load API information:', error);
      return;
    }
    for (const key in response.data) {
      (apiConfiguration as any)[key] = response.data[key];
    }
    endpoint.value = apiConfiguration.endpoint || '';
    apiConfiguration.status = response.data.status?.value || '';
    const allParameters = (apiConfiguration.parameters as FormData[]) || [];
    queryParameters.value = allParameters.filter(param => (param as any).in === 'query');
    headerParameters.value = allParameters.filter(param => (param as any).in === 'header');
    if (props.valueObj.unarchived) {
      currentServer.value = apiConfiguration.currentServer;
    } else {
      currentServer.value = apiConfiguration.availableServers?.[0] || { url: '' };
    }
    defaultCurrentServer.value = JSON.parse(JSON.stringify(currentServer.value));
    messageContent.value = apiConfiguration.requestBody?.[wsMessageKey] || '';
  };

  /**
   * Save API configuration
   * <p>
   * If unarchived, returns packaged params for upper-layer drawer handling; otherwise updates the API directly.
   * </p>
   */
  const saveApiConfiguration = async () => {
    const apiParams = await packageApiParameters();
    if (props.valueObj.unarchived) {
      // Let the upper layer open the drawer
      return apiParams;
    } else {
      const [error] = await apis.updateApi([apiParams]);
      if (error) {
        console.error('Failed to update API:', error);
        return;
      }
      notification.success(t('service.apiWebSocket.messages.updateApiSuccess'));
    }
  };

  watch(() => props.responseCount, () => {
    if (props.uuid === props.pid) {
      handleWebSocketProxyResponse();
    }
  });

  let clientChangeTimer: any;
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
  }, { immediate: true });

  return {
    // state
    API_EXTENSION_KEY,
    requestSettingKey,
    valueKey,
    userPermissions,
    editorLanguageOptions,
    currentEditorLanguage,
    isWebSocketConnected,
    isWebSocketClosing,
    isWebSocketConnecting,
    webSocketConnectedTimestamp,
    messageSearchKeywords,
    messageTypeFilter,
    apiConfiguration,
    queryParameters,
    headerParameters,
    endpoint,
    currentServer,
    defaultCurrentServer,
    messageContent,
    toolbarMenuOptions,
    webSocketMessages,
    isConnectButtonDisabled,

    // methods
    updateQueryParameters,
    updateHeaderParameters,
    buildQueryString,
    packageApiParameters,
    initializeWebSocketConnection,
    establishWebSocketConnection,
    closeWebSocketConnection,
    sendWebSocketMessage,
    handleReceivedMessage,
    handleWebSocketProxyResponse,
    clearAllMessages,
    copyWebSocketUrl,
    handleEndpointBlur,
    loadApiPermissions,
    loadApiInformation,
    saveApiConfiguration,

    // expose for tests
    _internals: { addMessageToList, calculateStringSize }
  };
}
