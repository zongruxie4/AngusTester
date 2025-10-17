import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { setting } from '@/api/gm';
import { ProxyType } from '../types';

export interface ProxyOption {
  label: string;
  value: string;
  enabled: boolean;
  key: string;
  url: string;
  disabled?: boolean;
}

/**
 * Composable for managing proxy options and configuration
 * @param readyState - WebSocket ready state ref
 * @param currentProxyUrl - Current proxy URL ref
 * @param currentProxy - Current proxy type ref
 * @returns Object containing proxy options and related methods
 */
export function useProxyOptions (
  readyState: any,
  currentProxyUrl: any,
  currentProxy: any
) {
  const { t } = useI18n();

  // Reactive state
  const isLoading = ref(false);
  const proxyOptions = ref<ProxyOption[]>([]);
  const selectedProxy = ref<ProxyType>(ProxyType.NO_PROXY);

  // Connection status computed from readyState
  const connectionStatus = computed(() => {
    return readyState.value === 1 ? 'success' : 'fail';
  });

  // Proxy tips mapping
  const proxyTipsMap = computed(() => ({
    NO_PROXY: t('proxy.types.noProxyDescription'),
    CLIENT_PROXY: t('proxy.types.clientProxyDescription'),
    SERVER_PROXY: t('proxy.types.serverProxyDescription'),
    CLOUD_PROXY: t('proxy.types.cloudProxyDescription')
  }));

  /**
   * Initialize proxy options with default values
   */
  const initializeProxyOptions = () => {
    proxyOptions.value = [{
      label: t('service.agent.noProxy.title'),
      value: ProxyType.NO_PROXY,
      enabled: false,
      key: 'noProxy',
      url: '',
      disabled: true
    }, {
      label: t('service.agent.clientProxy.title'),
      value: ProxyType.CLIENT_PROXY,
      enabled: false,
      key: 'clientProxy',
      url: '',
      disabled: true
    }, {
      label: t('service.agent.serverProxy.title'),
      value: ProxyType.SERVER_PROXY,
      enabled: false,
      key: 'serverProxy',
      url: '',
      disabled: true
    }];
  };

  /**
   * Load proxy configuration from server and update local state
   */
  const loadProxyConfiguration = async () => {
    const [error, response = { data: {} }] = await setting.getUserApiProxy();
    if (error) {
      return;
    }

    proxyOptions.value = Object.keys(response.data).map(key => {
      const proxyData = response.data[key];
      return {
        label: proxyData.name?.message || '',
        value: proxyData.name?.value || '',
        enabled: proxyData?.enabled || false,
        key: key,
        url: proxyData.url || '',
        disabled: true
      };
    });

    // Set the currently enabled proxy as selected
    const enabledProxy = proxyOptions.value.find(item => item.enabled);
    selectedProxy.value = enabledProxy?.value as ProxyType || ProxyType.NO_PROXY;
  };

  /**
   * Change the active proxy configuration
   * @param proxyValue - Selected proxy value
   */
  const handleProxyChange = async (proxyValue: string) => {
    const [error] = await setting.enabledUserApiProxy({ name: proxyValue });
    if (error) {
      return;
    }

    const targetProxy = proxyOptions.value.find(item => item.value === proxyValue);
    if (targetProxy) {
      currentProxyUrl.value = targetProxy.url;
      currentProxy.value = targetProxy.value;
    }

    await loadProxyConfiguration();
  };

  /**
   * Enable editing mode for client proxy URL
   * @param proxyOption - The proxy option to edit
   */
  const enableUrlEditing = (proxyOption: ProxyOption) => {
    proxyOption.disabled = false;
  };

  /**
   * Cancel URL editing and restore original value
   * @param proxyOption - The proxy option being edited
   * @param originalUrl - Original URL to restore
   */
  const cancelUrlEditing = (proxyOption: ProxyOption, originalUrl: string) => {
    proxyOption.url = originalUrl;
    proxyOption.disabled = true;
  };

  /**
   * Save the edited URL to server
   * @param proxyOption - The proxy option with updated URL
   * @param originalUrl - Original URL for comparison
   */
  const saveProxyUrl = async (proxyOption: ProxyOption, originalUrl: string) => {
    if (proxyOption.url === originalUrl) {
      cancelUrlEditing(proxyOption, originalUrl);
      return;
    }

    isLoading.value = true;
    const [error] = await setting.patchUserApiProxyUrl({ url: proxyOption.url });
    isLoading.value = false;

    if (error) {
      return;
    }

    // Update current proxy URL if this is the active proxy
    if (proxyOption.enabled) {
      currentProxyUrl.value = proxyOption.url;
    }

    proxyOption.disabled = true;
  };

  // Initialize on first load
  initializeProxyOptions();

  return {
    // Reactive data
    isLoading,
    proxyOptions,
    selectedProxy,
    connectionStatus,
    proxyTipsMap,

    // Methods
    loadProxyConfiguration,
    handleProxyChange,
    enableUrlEditing,
    cancelUrlEditing,
    saveProxyUrl
  };
}
