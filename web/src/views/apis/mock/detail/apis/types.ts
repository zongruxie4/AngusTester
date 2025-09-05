import { HttpMethod } from '@xcan-angus/infra';

import {
  ResponseContentConfig,
  ResponseContentInfo,
  ResponsePushbackConfig,
  ResponsePushbackInfo
} from '@/views/apis/mock/detail/apis/components/content/types';
import { ResponseMatchConfig, ResponseMatchInfo } from '@/views/apis/mock/detail/apis/components/match/types';

export type ResponseInfo = {
  id:string;
  name:string;
  content: ResponseContentInfo;
  match: ResponseMatchInfo;
  pushback?: ResponsePushbackInfo;
}

export type ResponseConfig = {
  name:string|undefined;
  content: ResponseContentConfig|undefined;
  match: ResponseMatchConfig|undefined;
  pushback?: ResponsePushbackConfig|undefined;
}

export type MockAPIInfo = {
  id: string;
  summary: string;
  method: {value:HttpMethod;message:string};
  description: string;
  endpoint: string;
  mockServiceId: string;
  isTempFlag:boolean;
}

export type MockAPIConfig = {
  id: string;
  summary: string;
  method: HttpMethod,
  description: string;
  endpoint: string;
  mockServiceId: string;
  isTempFlag:boolean;
  selectApiId?:string;
  selectType?:'link'|'copy';
}
