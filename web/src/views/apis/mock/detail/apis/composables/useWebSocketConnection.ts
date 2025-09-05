import { ref, watch, onMounted, onBeforeUnmount } from 'vue';
import ReconnectingWebSocket from 'reconnecting-websocket';
import { type AgentValue } from '@/views/apis/services/components/agent/PropsTypes';
import { setting } from '@/api/gm';

/**
 * Composable for managing WebSocket connection and proxy configuration
 * Handles WebSocket lifecycle, proxy settings, and connection state
 */
export function useWebSocketConnection () {
  // WebSocket state
  const WS = ref<ReconnectingWebSocket>();
  const uuid = ref<string>();
  const wsResponse = ref<string>();
  const readyState = ref(-1);

  // Proxy configuration
  const currentProxy = ref<AgentValue>();
  const currentProxyUrl = ref<string>();
  const proxyOptObj = ref<any>();

  /**
   * Load proxy URL configuration from server
   */
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

  /**
   * Create WebSocket connection with reconnection logic
   */
  const createWS = () => {
    if (!currentProxyUrl.value) {
      return;
    }

    WS.value = new ReconnectingWebSocket(currentProxyUrl.value, [], {
      maxRetries: 3,
      maxReconnectionDelay: 30000,
      minReconnectionDelay: 30000,
      connectionTimeout: 60000
    });

    WS.value.addEventListener('open', () => {
      readyState.value = WS.value?.readyState || 1;
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
      } catch (error) {
        // Handle parsing error silently
      }
    });

    WS.value.addEventListener('error', () => {
      readyState.value = WS.value?.readyState || 3;
    });

    WS.value.addEventListener('close', () => {
      readyState.value = WS.value?.readyState || 3;
    });
  };

  /**
   * Update WebSocket connection based on network and proxy changes
   */
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

  /**
   * Close WebSocket connection
   */
  const closeWebSocket = () => {
    if (WS.value?.close) {
      WS.value.close(1000);
    }
  };

  // Watch for proxy changes and update connection
  watch([() => currentProxyUrl.value, () => currentProxy.value], () => {
    closeWebSocket();
    WS.value = undefined;

    if (currentProxy.value === 'NO_PROXY') {
      readyState.value = -1;
    } else if (currentProxyUrl.value) {
      createWS();
    }
  }, { immediate: true });

  // Network connection change handler
  const handleNetworkChange = () => {
    updateWs();
  };

  onMounted(() => {
    loadProxyUrl();

    // Add network change listener
    if (navigator.connection) {
      navigator.connection.addEventListener('change', handleNetworkChange);
    }
  });

  onBeforeUnmount(() => {
    closeWebSocket();

    // Remove network change listener
    if (navigator.connection) {
      navigator.connection.removeEventListener('change', handleNetworkChange);
    }
  });

  return {
    // State
    WS,
    uuid,
    wsResponse,
    readyState,
    currentProxy,
    currentProxyUrl,
    proxyOptObj,

    // Methods
    loadProxyUrl,
    createWS,
    updateWs,
    closeWebSocket
  };
}
