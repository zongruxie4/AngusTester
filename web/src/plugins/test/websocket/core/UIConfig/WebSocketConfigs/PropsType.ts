import { AssertionConfig, AssertionInfo } from '@/plugins/test/components/UIConfigComp/AssertionForm/PropsType';

export type TargetKey = 'WEBSOCKET';
export type WebSocketMode = 'ONLY_SEND' | 'ONLY_RECEIVE' | 'SEND_AND_RECEIVE' | 'SEND_TO_RECEIVE' | 'RECEIVE_TO_SEND';

export type ParameterConfig = {
  name: string;
  in: 'query'|'header';
  value: string;
  enabled:boolean;
}

export type WebsocketConfig = {
  beforeName: string;
  target: 'WEBSOCKET';
  name: string;
  description: string;
  enabled: boolean;
  parameters: ParameterConfig[];
  mode: WebSocketMode;
  assertions: AssertionConfig[];
  message: string;
  url:string;
  messageEncoding:'none'|'base64'|'gzip_base64';
  server: {
    url: string;
    variables: {
      [key:string]: {
        defaultValue: string;
        allowableValues: string[];
        description?: string;
      }
    };
    description?:string;
  };
  endpoint: string;
  apisId: string;// 关联的api id
  id: string;// 用来标记，使用uuid生成
}

export type WebsocketInfo = {
  beforeName: string;
  target: 'WEBSOCKET';
  name: string;
  description: string;
  enabled: boolean;
  parameters: ParameterConfig[];
  mode: {value:WebSocketMode;message:string;};
  assertions: AssertionInfo[];
  message: string;
  url:string;
  server: {
    url: string;
    variables: {
      [key:string]: {
        defaultValue: string;
        allowableValues: string[];
        description?: string;
      }
    };
    description?:string;
  };
  endpoint: string;
  apisId: string;// 关联的api id
  id: string;// 用来标记，使用uuid生成
}
