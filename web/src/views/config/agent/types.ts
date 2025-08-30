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
