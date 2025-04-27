import { Component, defineAsyncComponent } from 'vue';
import { AuthItem } from './authorization/interface';
import { RequestBodyParam } from '@/views/apis/services/apiHttp/requestBody/interface';
import { API_EXTENSION_KEY } from '@/views/apis/utils';
import { ApiAssert as ApiAssertion, ApiAuthencation, ResponseAssert } from 'angus-design';
import 'angus-design/style.css';

const { valueKey, enabledKey } = API_EXTENSION_KEY;

export interface ParamsItem {
  name?: string,
  in?: string,
  [key: string]: any,
  description?: string,
  variabledFlag?: boolean,
  enabled?: boolean,
  allowableValues?: string[] | null, // 枚举值
  valueType?: string | null,
  key?: symbol
}

export interface HostItem {
  name?: string;
  host: string;
  defaultFlag?: boolean;
  protocol: { value: string };
}

export const getDefaultParams = (config = {}): ParamsItem => {
  return {
    name: '',
    in: undefined,
    description: '',
    // [exportVariableFlagKey]: false,
    [valueKey]: '',
    [enabledKey]: true,
    schema: { type: 'string' },
    ...config
  };
};

export const getBodyDefaultItem = (config = {}) => {
  return {
    name: '',
    in: undefined,
    description: '',
    [valueKey]: '',
    [enabledKey]: true,
    type: 'string',
    ...config
  };
};

export const RESP_FILE_TYPE = [
  'application/octet-stream',
  'application/force-download',
  'images/',
  'application/msword',
  'application/vnd',
  'video/',
  'audio/',
  'application/x-',
  'text/x-'
];

export const enum AuthEnum {
  NO_AUTH = 'NO_AUTH',
  BASIC_AUTH = 'BASIC_AUTH',
  BEARER_TOKEN = 'BEARER_TOKEN',
  CUSTOM = 'CUSTOM',
}
export type AuthType = 'NO_AUTH' | 'BASIC_AUTH' | 'BEARER_TOKEN' | 'CUSTOM'
export type Method = 'GET' | 'POST' | 'DELETE' | 'PUT' | 'PATCH' | 'HEAD' | 'OPTIONS' | 'TRACE';

export const navs = [
  {
    icon: 'icon-baocundaoweiguidang',
    name: '保存到未归档',
    disabled: false,
    value: 'saveUnarchived',
    auth: 'VIEW'
  },
  {
    icon: 'icon-fuwuxinxi',
    name: '归档到服务',
    value: 'save',
    disabled: false,
    auth: 'MODIFY'
  },
  {
    icon: 'icon-zhibiao',
    name: '接口指标',
    value: 'performance', // 2
    disabled: false,
    auth: 'MODIFY'
  },
  {
    icon: 'icon-zhihangceshi',
    name: '测试结果',
    value: 'test',
    disabled: false,
    auth: 'QUERY'
  },
  // {
  //   icon: 'icon-yongliku',
  //   name: '接口用例',
  //   value: 'case',
  //   disabled: false,
  //   auth: 'MODIFY'
  // },
  {
    icon: 'icon-lishijilu',
    name: '活动',
    value: 'activity',
    disabled: false,
    auth: 'QUERY'
  },
  // {
  //   icon: 'icon-bianliang',
  //   name: '变量',
  //   value: 'variable',
  //   disabled: false,
  //   auth: 'MODIFY'
  // },
  // {
  //   icon: 'icon-mockjiedian',
  //   name: 'Mock接口',
  //   value: 'apiMock',
  //   auth: 'VIEW',
  //   disabled: true
  // },
  {
    icon: 'icon-jiekoudaili',
    name: '接口代理',
    value: 'agent',
    auth: 'MODIFY',
    disabled: false
  }
];

export interface OptionItem {
  label: string | null,
  value: string
}

export interface Setting {
  enableParamValidation: boolean;
  connectTimeout: number;
  readTimeout: number;
  maxRedirects: number;
  retryNum: number;
}

export interface State {
  parameter: Record<string, unknown>;
  assertTypeOptions: OptionItem[];
  assertConditionOptions: OptionItem[];
  paramTypeOptions: OptionItem[];
  bodyParamTypeOptions: OptionItem[];
  contentTypeOptions: OptionItem[];
  formDataTypeOptions: OptionItem[];
  methodOptions: OptionItem[];
  authTypeOptions: OptionItem[];
  parameters: ParamsItem[];
  requestBody: RequestBodyParam;
  headerList: ParamsItem[];
  cookieList: ParamsItem[];
  assertions: any[];
  authentication: AuthItem;
  authDefaultValue?: AuthItem;
  securityFlag: boolean;
  publishFlag?: boolean;
}

export interface ResponseState {
  config?: Record<string, any>;
  headers?: Record<string, any>;
  data?: any;
  status?: number;
  size?: number;
  assertions?: any[];
  performance?: PerformanceEntry;
  cookie?: string[];
  requestHeaders?: ParamsItem[];
  errorMessage?: string;
  contentEncoding?: string;
}

export const RequestParams: Component = defineAsyncComponent(() => import('@/views/apis/services/apiHttp/requestParam/index.vue'));
export const RequestBody: Component = defineAsyncComponent(() => import('@/views/apis/services/apiHttp/requestBody/index.vue'));
export const RequestHeader: Component = defineAsyncComponent(() => import('@/views/apis/services/apiHttp/requestHeader/index.vue'));
export const RequestCookie: Component = defineAsyncComponent(() => import('@/views/apis/services/apiHttp/requestCookie/index.vue'));
// export const Authorization: Component = defineAsyncComponent(() => import('./authorization/index.vue'));
export const Authorization: Component = ApiAuthencation;
// @TODO 临时调试，需要删除
// export const AssertForm: Component = defineAsyncComponent(() => import('@/views/apis/services/components/AssertForm/index.vue'));
export const AssertForm: Component = ApiAssertion;

export const ApiRequest: Component = defineAsyncComponent(() => import('@/views/apis/services/components/request/index.vue'));
export const ApiResponse: Component = defineAsyncComponent(() => import('@/views/apis/services/components/response/index.vue'));
export const ApiTimeline: Component = defineAsyncComponent(() => import('@/views/apis/services/components/timeline/index.vue'));
export const ApiCookie: Component = defineAsyncComponent(() => import('@/views/apis/services/components/cookie/index.vue'));
// @TODO 临时调试，需要删除
// export const ApiAssert: Component = defineAsyncComponent(() => import('@/views/apis/services/components/ApiAssert/index.vue'));
export const ApiAssert: Component = ResponseAssert;
// export const ApiDefine: Component = defineAsyncComponent(() => import('@/views/apis/services/components/apiDefinition/index.vue'));

export interface Menu {
  name: string,
  value: string
}

export const menus: Menu[] = [
  {
    name: '基本',
    value: 'request'
  },
  {
    name: '响应',
    value: 'response'
  },
  {
    name: '耗时分析',
    value: 'time'
  },
  {
    name: 'Cookie',
    value: 'cookie'
  },
  {
    name: '断言结果',
    value: 'assert'
  },
  // {
  //   name: '定义',
  //   value: 'define'
  // },
  {
    name: '代码',
    value: 'generateCode'
  }
];
export const getStatusColor = (status) => {
  const statusStr = status.toString();
  if (statusStr.startsWith('4') || statusStr.startsWith('5')) {
    return 'text-status-error';
  }
  return 'text-status-success';
};

export const docInfo = [
  {
    title: 'Path参数',
    rules: ['Primitive id = 5 -> /users/5', 'Array id = [3, 4, 5] -> /users/3,4,5', 'Object id = {"role": "admin", "firstName": "Alex"} -> /users/role=admin,firstName=Alex', '> 只支持基本类型、基本类型数组、对象类型，其他复杂类型数据模型不解析。']
  },
  {
    title: 'Query 和 Form 参数',
    rules: ['Primitive id = 5 -> id=5', 'Array id = [3, 4, 5] -> id[0]=3&id[1]=4&id[2]=5', 'Object id = {"role": "admin", "firstName": "Alex"} -> id.role=admin&id.firstName=Alex', '> 支持所有类型，及对象和数组类型嵌套。']
  },
  {
    title: 'header 参数',
    rules: ['Primitive X-MyHeader = 5 ->  X-MyHeader: 5', 'Array X-MyHeader = [3, 4, 5] -> X-MyHeader: 3,4,5', 'Object X-MyHeader = {"role": "admin", "firstName": "Alex"} -> X-MyHeader: role=admin,firstName=Alex', '> 只支持基本类型、基本类型数组、对象类型，其他复杂类型数据模型不解析。']
  },
  {
    title: 'Cookie 参数',
    rules: ['Primitive id = 5 ->  Cookie: id=5', 'Array id = [3, 4, 5] -> Cookie: id=3,4,5', 'Object id = {"role": "admin", "firstName": "Alex"} -> Cookie: id=role,admin,firstName,Alex', '> 只支持基本类型、基本类型数组、对象类型，其他复杂类型数据模型不解析。']
  }
];

export const debugTip = [
  '组件调试值修改与同步：修改Query、Path、Header和Form类型参数引用组件的调试值会同步修改到组件，同时引用组件的其他接口也会被同步修改；而Raw请求体内容引用组件时修改调试值不会同步到组件，即Raw请求体引用组件调试值修改不会影响其他接口。',
  '接口中支持变量的位置：安全方案(Authorization)、Path参数、Query参数、Header参数、请求体Form参数、请求体Raw、断言条件表达式和断言期望值。',
  '接口中支持函数的位置：Path参数、Query参数、Header参数、请求体Form参数和请求体Raw。'
];
