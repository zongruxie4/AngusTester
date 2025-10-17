import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { ProxyType } from '../types';

/**
 * Composable for managing proxy connection status and UI state
 * @param readyState - WebSocket ready state ref
 * @param currentProxy - Current proxy type ref
 * @param currentProxyUrl - Current proxy URL ref
 * @returns Object containing status-related data and methods
 */
export function useProxyStatus (
  readyState: any,
  currentProxy: any,
  currentProxyUrl: any
) {
  const { t } = useI18n();

  // Connection status computed from readyState
  const connectionStatus = computed(() => {
    return readyState.value === 1 ? 'success' : 'fail';
  });

  // Status messages for different scenarios
  const statusMessages = computed(() => ({
    notConnected: t('service.agent.unconnect'),
    connected: t('service.agent.connectSuccess'),
    serverProxyNotConfigured: t('service.agent.configFailTip'),
    proxyConnectionFailed: t('service.agent.connectFailTip')
  }));

  // Status icons mapping
  const statusIcons = computed(() => ({
    fail: 'icon-zhongzhi1',
    success: 'icon-duihao'
  }));

  // Status classes mapping
  const statusClasses = computed(() => ({
    fail: 'text-execute-yellow',
    success: 'text-execute-res4',
    warning: 'text-status-orange'
  }));

  /**
   * Check if proxy is configured but not connected
   */
  const isProxyConfiguredButNotConnected = computed(() => {
    return currentProxy.value !== ProxyType.NO_PROXY &&
           !currentProxyUrl.value &&
           connectionStatus.value === 'fail';
  });

  /**
   * Check if proxy is configured and connection failed
   */
  const isProxyConfiguredAndConnectionFailed = computed(() => {
    return currentProxy.value !== ProxyType.NO_PROXY &&
           currentProxyUrl.value &&
           connectionStatus.value === 'fail';
  });

  /**
   * Check if proxy is not configured
   */
  const isProxyNotConfigured = computed(() => {
    return currentProxy.value !== ProxyType.NO_PROXY &&
           !currentProxyUrl.value;
  });

  return {
    // Computed properties
    connectionStatus,
    statusMessages,
    statusIcons,
    statusClasses,
    isProxyConfiguredButNotConnected,
    isProxyConfiguredAndConnectionFailed,
    isProxyNotConfigured
  };
}
