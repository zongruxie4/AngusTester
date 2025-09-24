import { Component, defineAsyncComponent } from 'vue';
import { AuthItem } from './authorization/interface';
import { RequestBodyParam } from '@/views/apis/services/apiHttp/requestBody/interface';
import { API_EXTENSION_KEY } from '@/views/apis/utils';
import { i18n } from '@xcan-angus/infra';
import ApiAssertion from '@/components/ApiAssert/index.vue';
import ApiAuthencation from '@/components/Authorize/index.vue';
import ResponseAssert from '@/components/ResponseAssert/index.vue';

const t = i18n.getI18n()?.global?.t || ((value: string) => value);

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
    name: t('service.apis.navs.saveUnarchived'),
    disabled: false,
    value: 'saveUnarchived',
    auth: 'VIEW'
  },
  {
    icon: 'icon-fuwuxinxi',
    name: t('actions.save'),
    value: 'save',
    disabled: false,
    auth: 'MODIFY'
  },
  {
    icon: 'icon-zhibiao',
    name: t('service.apis.navs.indicator'),
    value: 'performance', // 2
    disabled: false,
    auth: 'MODIFY'
  },
  {
    icon: 'icon-zhihangceshi',
    name: t('service.apis.navs.test'),
    value: 'test',
    disabled: false,
    auth: 'QUERY'
  },
  // {
  //   icon: 'icon-yongliku',
  //   name: 'service.apis.navs.case',
  //   value: 'case',
  //   disabled: false,
  //   auth: 'MODIFY'
  // },
  {
    icon: 'icon-lishijilu',
    name: t('service.apis.navs.activity'),
    value: 'activity',
    disabled: false,
    auth: 'QUERY'
  },
  // {
  //   icon: 'icon-bianliang',
  //   name: 'service.apis.navs.variable',
  //   value: 'variable',
  //   disabled: false,
  //   auth: 'MODIFY'
  // },
  // {
  //   icon: 'icon-mockjiedian',
  //   name: 'service.apis.navs.apiMock',
  //   value: 'apiMock',
  //   auth: 'VIEW',
  //   disabled: true
  // },
  {
    icon: 'icon-jiekoudaili',
    name: t('service.apis.navs.agent'),
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
  secured: boolean;
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
    name: t('service.apis.menus.request'),
    value: 'request'
  },
  {
    name: t('service.apis.menus.response'),
    value: 'response'
  },
  {
    name: t('service.apis.menus.time'),
    value: 'time'
  },
  {
    name: t('service.apis.menus.cookie'),
    value: 'cookie'
  },
  {
    name: t('service.apis.menus.assert'),
    value: 'assert'
  },
  // {
  //   name: 'service.apis.menus.define',
  //   value: 'define'
  // },
  {
    name: t('service.apis.menus.generateCode'),
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
    title: t('service.apis.docInfo.pathParams.title'),
    rules: [t('service.apis.docInfo.pathParams.rules.primitive'), t('service.apis.docInfo.pathParams.rules.array'), t('service.apis.docInfo.pathParams.rules.object'), t('service.apis.docInfo.pathParams.rules.limit')]
  },
  {
    title: t('service.apis.docInfo.queryFormParams.title'),
    rules: [t('service.apis.docInfo.queryFormParams.rules.primitive'), t('service.apis.docInfo.queryFormParams.rules.array'), t('service.apis.docInfo.queryFormParams.rules.object'), t('service.apis.docInfo.queryFormParams.rules.support')]
  },
  {
    title: t('service.apis.docInfo.headerParams.title'),
    rules: [t('service.apis.docInfo.headerParams.rules.primitive'), t('service.apis.docInfo.headerParams.rules.array'), t('service.apis.docInfo.headerParams.rules.object'), t('service.apis.docInfo.headerParams.rules.limit')]
  },
  {
    title: t('service.apis.docInfo.cookieParams.title'),
    rules: [t('service.apis.docInfo.cookieParams.rules.primitive'), t('service.apis.docInfo.cookieParams.rules.array'), t('service.apis.docInfo.cookieParams.rules.object'), t('service.apis.docInfo.cookieParams.rules.limit')]
  }
];

export const debugTip = [
  t('service.apis.debugTip.componentSync'),
  t('service.apis.debugTip.variableSupport'),
  t('service.apis.debugTip.functionSupport')
];
