import { ContentEncoding, HttpMethod, ParameterIn, ResponseDelayMode } from '@xcan-angus/infra';
import { CONTENT_TYPE_KEYS, LANGUAGE, RADIO_TYPE_KEYS } from '@/utils/constant';

// ==================== Types ====================
export type RadioType = typeof RADIO_TYPE_KEYS[keyof typeof RADIO_TYPE_KEYS];
export type ContentType = typeof CONTENT_TYPE_KEYS[keyof typeof CONTENT_TYPE_KEYS];
export type Language = typeof LANGUAGE[keyof typeof LANGUAGE];

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
