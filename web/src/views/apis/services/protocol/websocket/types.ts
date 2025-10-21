import { API_EXTENSION_KEY } from '@/utils/apis';

const { valueKey, enabledKey } = API_EXTENSION_KEY;

// TODO 替换类型
export interface FormData {
  name?: string;
  [valueKey]?: string;
  description?: string;
  [enabledKey]?: boolean;
  in: string;
  schema: Record<string, any>;
  $ref?: string;
}

export const getDefaultForm = (form = {}) => {
  return {
    name: '',
    [valueKey]: '',
    description: '',
    in: 'query',
    [enabledKey]: true,
    schema: {
      type: 'string'
    },
    ...form
  };
};

export enum MessageType {
  RECEIVE = 'receive',
  SEND = 'send',
  CONNECT = 'connect',
  CLOSE = 'close',
  SEND_ERR = 'sendErr',
  CONNECT_ERR = 'connectErr',
  CLOSE_ERR = 'closeErr'
}

// message payload field: messageType
export enum WebsocketProxyMessageType {
  WEBSOCKET_REQUEST_PROXY = 'WebsocketRequestProxy'
}

export interface Message {
  type: MessageType,
  date: string,
  size: string,
  content?: string,
  showContent?: boolean;
  key: string;
}

export interface Props {
  getParameter: any;
  id: string;
  summary: string;
  operationId: string;
  projectId: string;
  description: string;
  status: string;
  ownerId: string;
  deprecated: boolean;
  tabKey: string;
  packageParams: () => Record<string, any>;
}
