import ReconnectingWebSocket from 'reconnecting-websocket';
import { ProxyType } from '@/views/config/proxy/types';

/**
 * WebSocket connection configuration
 */
export const WS_CONFIG = {
  maxRetries: 3,
  maxReconnectionDelay: 30000,
  minReconnectionDelay: 30000,
  connectionTimeout: 60000
} as const;

/**
 * WebSocket ready states
 */
export const WS_READY_STATES = {
  CONNECTING: 0,
  OPEN: 1,
  CLOSING: 2,
  CLOSED: 3,
  DISCONNECTED: 4
} as const;

/**
 * WebSocket message response interface
 */
export interface WebSocketResponse {
  requestId?: string;
  clientId?: string;
  [key: string]: any;
}

/**
 * WebSocket event handlers interface
 */
export interface WebSocketEventHandlers {
  onOpen?: () => void;
  onMessage?: (data: string, response: WebSocketResponse) => void;
  onError?: () => void;
  onClose?: () => void;
}

/**
 * Angus WebSocket Proxy utility class
 * Manages WebSocket connections with reconnection capabilities
 */
export class AngusWebSocketProxy {
  private ws: ReconnectingWebSocket | undefined;
  private handlers: WebSocketEventHandlers = {};
  private readyState: number = WS_READY_STATES.CLOSED;
  private responseCount = 0;
  private currentUuid = '';

  /**
   * Creates a new WebSocket connection
   * @param url WebSocket URL
   * @param handlers Event handlers
   */
  public connect (url: string, handlers: WebSocketEventHandlers = {}): void {
    if (!url) {
      console.warn('WebSocket URL is required');
      return;
    }

    this.handlers = handlers;

    // Close existing connection
    this.disconnect();

    this.ws = new ReconnectingWebSocket(url, [], WS_CONFIG);

    this.ws.addEventListener('open', () => {
      this.readyState = this.ws?.readyState ?? WS_READY_STATES.OPEN;
      this.handlers.onOpen?.();
    });

    this.ws.addEventListener('message', (event: MessageEvent) => {
      try {
        const response: WebSocketResponse = JSON.parse(event.data);
        // Extract request ID from different possible fields
        this.currentUuid = response?.requestId || response?.clientId || '';
        this.responseCount += 1;
        this.handlers.onMessage?.(event.data, response);
      } catch (error) {
        console.warn('Failed to parse WebSocket message:', error);
      }
    });

    this.ws.addEventListener('error', () => {
      this.readyState = this.ws?.readyState ?? WS_READY_STATES.CLOSED;
      this.handlers.onError?.();
    });

    this.ws.addEventListener('close', () => {
      this.readyState = this.ws?.readyState ?? WS_READY_STATES.CLOSED;
      this.handlers.onClose?.();
    });
  }

  /**
   * Disconnects the WebSocket connection
   */
  public disconnect (): void {
    if (this.ws?.close) {
      this.ws.close(1000);
    }
    this.ws = undefined;
    this.readyState = WS_READY_STATES.CLOSED;
  }

  /**
   * Updates WebSocket connection based on network status and proxy configuration
   * @param isOnline Network online status
   * @param proxyUrl Proxy URL (optional)
   * @param proxyName Proxy name (optional)
   */
  public updateConnection (isOnline: boolean, proxyUrl?: string, proxyName?: string): void {
    // Close existing connection if online
    if (isOnline && this.ws?.close) {
      this.ws.close(1000);
    }

    if (isOnline) {
      if (proxyUrl) {
        this.ws = undefined;
        this.connect(proxyUrl, this.handlers);
      } else if (proxyName === ProxyType.NO_PROXY) {
        this.ws = undefined;
      } else {
        this.ws = { readyState: WS_READY_STATES.DISCONNECTED } as any;
        this.readyState = WS_READY_STATES.DISCONNECTED;
      }
    } else {
      // Offline state
      if (proxyUrl) {
        this.ws = { readyState: WS_READY_STATES.DISCONNECTED } as any;
        this.readyState = WS_READY_STATES.DISCONNECTED;
      } else {
        this.ws = undefined;
        this.readyState = WS_READY_STATES.CLOSED;
      }
    }
  }

  /**
   * Sends a message through the WebSocket connection
   * @param message Message to send
   */
  public send (message: string): void {
    if (this.ws && this.readyState === WS_READY_STATES.OPEN) {
      this.ws.send(message);
    } else {
      console.warn('WebSocket is not connected');
    }
  }

  /**
   * Gets the current WebSocket instance
   */
  public getWebSocket (): ReconnectingWebSocket | undefined {
    return this.ws;
  }

  /**
   * Gets the current ready state
   */
  public getReadyState (): number {
    return this.readyState;
  }

  /**
   * Gets the current request UUID
   */
  public getCurrentUuid (): string {
    return this.currentUuid;
  }

  /**
   * Gets the response count
   */
  public getResponseCount (): number {
    return this.responseCount;
  }

  /**
   * Resets the response count
   */
  public resetResponseCount (): void {
    this.responseCount = 0;
  }

  /**
   * Checks if the WebSocket is connected
   */
  public isConnected (): boolean {
    return this.readyState === WS_READY_STATES.OPEN;
  }

  /**
   * Checks if the WebSocket is connecting
   */
  public isConnecting (): boolean {
    return this.readyState === WS_READY_STATES.CONNECTING;
  }

  /**
   * Checks if the WebSocket is disconnected
   */
  public isDisconnected (): boolean {
    return this.readyState === WS_READY_STATES.CLOSED ||
           this.readyState === WS_READY_STATES.DISCONNECTED;
  }
}

/**
 * Creates a new Angus WebSocket Proxy instance
 */
export const createAngusWebSocketProxy = (): AngusWebSocketProxy => {
  return new AngusWebSocketProxy();
};
