import { ref, onMounted, onBeforeUnmount } from 'vue';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
import { setting } from '@/api/gm';
import { useI18n } from 'vue-i18n';
import { notification } from '@xcan-angus/vue-ui';
import { ProxyConfig, ProxyConnectionStatus } from '../types';

/**
 * Composable for managing proxy configuration data and operations
 * @returns Object containing reactive data and methods for proxy configuration
 */
export function useProxyData () {
  const { t } = useI18n();

  // Reactive state for proxy configuration
  const loading = ref(false);
  const proxyConfig = ref<ProxyConfig>({ url: '', enabled: false });
  const connectionStatus = ref<ProxyConnectionStatus>({ isConnected: false, isNewConnection: false });
  const isEditMode = ref(true);
  const addressValidationError = ref(false);

  // WebSocket connection reference
  let webSocketConnection: WebSocket | null = null;
  let isInitializing = false;

  /**
   * Toggle edit mode for proxy address
   */
  const toggleEditMode = () => {
    isEditMode.value = !isEditMode.value;
  };

  /**
   * Test WebSocket connection to proxy address
   */
  const testProxyConnection = () => {
    connectionStatus.value.isNewConnection = true;

    // Close existing connection if present
    if (webSocketConnection) {
      webSocketConnection.close(1000);
    }

    // Create new WebSocket connection
    webSocketConnection = new WebSocket(proxyConfig.value.url);

    if (!webSocketConnection) {
      connectionStatus.value.isConnected = false;
      return;
    }

    // Handle successful connection
    webSocketConnection.onopen = () => {
      connectionStatus.value.isConnected = true;
      isEditMode.value = true;

      // Update proxy settings if not initializing
      if (!isInitializing) {
        const params = {
          enabled: proxyConfig.value.enabled,
          url: proxyConfig.value.url
        };
        updateProxyConfiguration(params);
      }
    };

    // Reset connection status
    connectionStatus.value.isConnected = false;
  };

  /**
   * Load proxy configuration data from API
   */
  const loadProxyConfiguration = async () => {
    loading.value = true;
    const [error, { data = {} }] = await setting.getTenantApiProxy();
    loading.value = false;

    if (error || !data) {
      return;
    }

    // Update local state with API response
    proxyConfig.value = data;

    // Test connection if URL is present
    if (data.url) {
      isInitializing = true;
      testProxyConnection();
    }
  };

  /**
   * Update proxy configuration via API
   * @param params - Proxy configuration parameters
   */
  const updateProxyConfiguration = async (params: ProxyConfig) => {
    // Skip update if address validation fails
    if (addressValidationError.value) {
      return;
    }

    // Skip update if not connected
    if (!connectionStatus.value.isConnected) {
      return;
    }
    loading.value = true;
    const [error] = await setting.updateTenantApiProxy(params);
    loading.value = false;
    if (error) {
      return;
    }

    // Show success notification and reload data
    notification.success(t('proxy.messages.proxyConfigModifiedSuccess'));
    await loadProxyConfiguration();
  };

  /**
   * Handle proxy enable/disable toggle
   */
  const handleProxyToggle = () => {
    const params = {
      enabled: proxyConfig.value.enabled,
      url: proxyConfig.value.url
    };
    updateProxyConfiguration(params);
  };

  /**
   * Validate proxy address and test connection
   */
  const validateProxyAddress = debounce(duration.search, () => {
    // Validate address format
    if (!proxyConfig.value.url) {
      addressValidationError.value = true;
      return;
    } else {
      addressValidationError.value = false;
    }

    // Test connection with new address
    isInitializing = false;
    testProxyConnection();

    // Update configuration
    const params = {
      enabled: proxyConfig.value.enabled,
      url: proxyConfig.value.url
    };
    updateProxyConfiguration(params);
  });

  /**
   * Initialize component data
   */
  const initializeComponent = () => {
    loadProxyConfiguration();
  };

  /**
   * Clean up resources before component unmount
   */
  const cleanupResources = () => {
    if (webSocketConnection) {
      webSocketConnection.close(1000);
    }
  };

  // Lifecycle hooks
  onMounted(() => {
    initializeComponent();
  });

  onBeforeUnmount(() => {
    cleanupResources();
  });

  return {
    // Reactive data
    loading,
    proxyConfig,
    connectionStatus,
    isEditMode,
    addressValidationError,

    // Methods
    toggleEditMode,
    testProxyConnection,
    loadProxyConfiguration,
    updateProxyConfiguration,
    handleProxyToggle,
    validateProxyAddress,
    initializeComponent,
    cleanupResources
  };
}
