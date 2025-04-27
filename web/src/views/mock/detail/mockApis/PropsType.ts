import {
  ResponseContentConfig,
  ResponseContentInfo,
  ResponsePushbackConfig,
  ResponsePushbackInfo
} from '@/views/mock/detail/mockApis/components/contentForm/PropsType';
import { ResponseMatchConfig, ResponseMatchInfo } from '@/views/mock/detail/mockApis/components/matchForm/PropsType';

export type HttpMethod = 'DELETE' | 'GET' | 'HEAD' | 'OPTIONS' | 'PATCH' | 'POST' | 'PUT' | 'TRACE';

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
  isTempFlag:boolean;// 用于在mock api 为空或者点击添加接口时，标识临时添加的数据
  // 复制接口添加、关联接口添加相关字段
  selectApiId?:string;
  selectType?:'link'|'copy';
}
