export enum ProxyType {
  NO_PROXY= 'NO_PROXY',
  CLIENT_PROXY = 'CLIENT_PROXY',
  CLOUD_PROXY = 'CLOUD_PROXY',
  SERVER_PROXY = 'SERVER_PROXY'
}

/**
 * Proxy configuration data structure
 * @interface ProxyConfig
 */
export interface ProxyConfig {
  /**
   * Proxy URL address
   */
  url: string;

  /**
   * Whether the proxy is enabled
   */
  enabled: boolean;
}

/**
 * Proxy connection status
 * @interface ProxyConnectionStatus
 */
export interface ProxyConnectionStatus {
  /**
   * Whether the connection is successful
   */
  isConnected: boolean;

  /**
   * Whether it's a new connection attempt
   */
  isNewConnection: boolean;
}
