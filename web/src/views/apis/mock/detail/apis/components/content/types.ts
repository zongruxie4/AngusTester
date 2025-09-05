import { HttpMethod, ResponseDelayMode, ParameterIn, ContentEncoding } from '@xcan-angus/infra';

export type RadioType = 'none' | 'application/x-www-form-urlencode' | 'multipart/form-data' | 'raw'
export type ContentType =
  'application/x-www-form-urlencode'
  | 'multipart/form-data'
  | 'application/json'
  | 'text/html'
  | 'application/xml'
  | 'application/javascript'
  | 'text/plain'
  | '*/*'
export type Language = 'json' | 'html' | 'typescript' | 'text'

export type DelayData = {
  fixedTime?: string;
  maxRandomTime?: string;
  minRandomTime?: string;
  mode: ResponseDelayMode;
}

export type PushbackBody = {
  forms: {
    name: string;
    value: string;
  }[];
  rawContent: string;
  contentType?: string;
};

export type ResponseHeader = { name: string; value: string; disabled: boolean; }

export type ResponseContentInfo = {
  content: string | undefined;
  contentEncoding: ContentEncoding | undefined;
  delay: {
    fixedTime?: string;
    maxRandomTime?: string;
    minRandomTime?: string;
    mode: { value: ResponseDelayMode; message: string; };
  } | undefined;
  headers: ResponseHeader[] | undefined;
  status: string;
}

export type ResponseContentConfig = {
  content: string | undefined;
  contentEncoding: ContentEncoding | undefined;
  delay: DelayData | undefined;
  headers: ResponseHeader[] | undefined;
  status: string;
}

export type ParametersType = {
  in: ParameterIn;
  name: string;
  value: string;
  disabled: boolean;
}

export type ResponsePushbackInfo = {
  autoPush: boolean;
  body: PushbackBody;
  delay: {
    fixedTime?: string;
    maxRandomTime?: string;
    minRandomTime?: string;
    mode: { value: ResponseDelayMode; message: string; };
  };
  method: { value: HttpMethod; message: string; };
  parameters: ParametersType[];
  url: string;
}

export type ResponsePushbackConfig = {
  autoPush: boolean;
  body: PushbackBody;
  delay: DelayData | undefined;
  method: HttpMethod;
  parameters: ParametersType[];
  url: string;
}
