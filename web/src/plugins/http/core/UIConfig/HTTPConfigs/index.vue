<script setup lang="ts">
import { ref, onMounted, watch, computed, defineAsyncComponent, watchEffect, onBeforeUnmount } from 'vue';
import { Collapse, CollapsePanel, Tabs, TabPane, Badge, Radio } from 'ant-design-vue';
import { Composite, Input, SelectEnum, Icon, Tooltip, Popover, ExecAuthencation, FunctionsButton } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import { cloneDeep } from 'lodash-es';

import { HTTPConfig, HttpMethod } from './PropsType';

const docInfo = [
  {
    title: 'Path参数',
    rules: ['Primitive id = 5 -> /users/5', 'Array id = [3, 4, 5] -> /users/3,4,5', 'Object id = {"role": "admin", "firstName": "Alex"} -> /users/role=admin,firstName=Alex', '> 只支持基本类型、基本类型数组、对象类型，其他复杂类型数据模型不解析。']
  },
  {
    title: 'Query 和 Form 参数',
    rules: ['Primitive id = 5 -> id=5', 'Array id = [3, 4, 5] -> id[0]=3&id[1]=4&id[2]=5', 'Object id = {"role": "admin", "firstName": "Alex"} -> id.role=admin&id.firstName=Alex', '> 支持所有类型，及对象和数组类型嵌套。']
  },
  {
    title: 'Header 参数',
    rules: ['Primitive X-MyHeader = 5 ->  X-MyHeader: 5', 'Array X-MyHeader = [3, 4, 5] -> X-MyHeader: 3,4,5', 'Object X-MyHeader = {"role": "admin", "firstName": "Alex"} -> X-MyHeader: role=admin,firstName=Alex', '> 只支持基本类型、基本类型数组、对象类型，其他复杂类型数据模型不解析。']
  },
  {
    title: 'Cookie 参数',
    rules: ['Primitive id = 5 ->  Cookie: id=5', 'Array id = [3, 4, 5] -> Cookie: id=3,4,5', 'Object id = {"role": "admin", "firstName": "Alex"} -> Cookie: id=role,admin,firstName,Alex', '> 只支持基本类型、基本类型数组、对象类型，其他复杂类型数据模型不解析。']
  }
];

const debugTip = [
  '组件调试值修改与同步：修改Query、Path、Header和Form类型参数引用组件的调试值会同步修改到组件，同时引用组件的其他接口也会被同步修改；而Raw请求体内容引用组件时修改调试值不会同步到组件，即Raw请求体引用组件调试值修改不会影响其他接口。',
  '接口中支持变量的位置：安全方案(Authorization)、Path参数、Query参数、Header参数、请求体Form参数、请求体Raw、断言条件表达式和断言期望值。',
  '接口中支持函数的位置：Path参数、Query参数、Header参数、请求体Form参数和请求体Raw。'
];

export type ParameterConfig = {
  name: string;
  enabled: boolean;
  disabled: boolean;
  id: string;
  value: string;
  type: 'string';
  in: 'query' | 'path' | 'header' | 'cookie';
}

export interface Props {
  value: HTTPConfig;
  repeatNames: string[];
  enabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  repeatNames: () => [],
  enabled: true
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'click', value: 'delete' | 'clone'): void;
  (e: 'change', value: Omit<HTTPConfig, 'id'>): void;
  (e: 'nameChange', value: string): void;
  (e: 'enabledChange', value: boolean): void;
  (e: 'renderChange'): void;
}>();

const ServerEndpoint = defineAsyncComponent(() => import('./ServerEndpoint/index.vue'));
const ActionsGroup = defineAsyncComponent(() => import('../ActionsGroup/index.vue'));
const AssertionForm = defineAsyncComponent(() => import('./AssertionForm/index.vue'));
const RequestBody = defineAsyncComponent(() => import('./RequestBody/index.vue'));
const ParameterInput = defineAsyncComponent(() => import('./ParameterInput.vue'));
const ParameterPure = defineAsyncComponent(() => import('./ParameterPure.vue'));
const Parametric = defineAsyncComponent(() => import('./Parametric/index.vue'));

const NO_BINARY_TYPES = [
  'application/x-www-form-urlencoded',
  'multipart/form-data',
  'application/json',
  'text/html',
  'application/xml',
  'application/javascript',
  'text/plain',
  '*/*'
];
const STREAM_TYPE = 'application/octet-stream';
const ENCODED_TYPE = 'application/x-www-form-urlencoded';

const overlayClassName = utils.uuid();
const UUID = utils.uuid();
const activeKey = ref<string>(UUID);

const queryPathRef = ref();
const headerRef = ref();
const cookieRef = ref();
const assertRef = ref();
const bodyRef = ref();
const parametricRef = ref();

const popoverDom = ref<HTMLElement>();

const serverUrlEnterFlag = ref(false);
let timer: NodeJS.Timeout;

const apisId = ref<string>();
const caseId = ref<string>();

const contentType = ref<string>();
const requestBody = ref<HTTPConfig['request']['body']>();

const authentication = ref<HTTPConfig['request']['authentication']>();
const defaultAuthentication = ref<HTTPConfig['request']['authentication']>();
const authInHeader = ref<{name:string;value:string;}[]>([]);

const cookieParams = ref<ParameterConfig[]>([]);
const defaultCookieParams = ref<ParameterConfig[]>([]);
const headerParams = ref<ParameterConfig[]>([]);
const defaultHeaderParams = ref<ParameterConfig[]>([]);
const queryPathParams = ref<ParameterConfig[]>([]);
const defaultQueryPathParams = ref<ParameterConfig[]>([]);

const assertions = ref<HTTPConfig['assertions']>([]);
const defaultAssertions = ref<HTTPConfig['assertions']>([]);

const variables = ref<HTTPConfig['variables']>([]);
const defaultVariables = ref<HTTPConfig['variables']>([]);

const datasets = ref<HTTPConfig['datasets']>([]);
const defaultDatasets = ref<HTTPConfig['datasets']>([]);

const actionOnEOF = ref<'RECYCLE' | 'STOP_THREAD'>('RECYCLE');
const sharingMode = ref<'ALL_THREAD' | 'CURRENT_THREAD'>('ALL_THREAD');

const description = ref<string>();
const enabled = ref(false);
const name = ref<string>();
const nameRepeatFlag = ref(false);
const nameError = ref(false);

const httpMethod = ref<HttpMethod>('GET');

const server = ref<HTTPConfig['request']['server']>({
  url: null,
  description: null,
  variables: null
});
const serverUrlError = ref(false);
const serverUrlFormatError = ref(false);

const endpoint = ref<string>();
const endpointError = ref(false);
const endpointFormatError = ref(false);

const assertionErrorNum = ref(0);
const parametricErrorNum = ref(0);
const queryPathErrorNum = ref(0);
const headerErrorNum = ref(0);
const cookieErrorNum = ref(0);
const bodyErrorNum = ref(0);

const nameChange = (event: { target: { value: string } }) => {
  const value = event.target.value;
  name.value = value;
  nameError.value = false;
  nameRepeatFlag.value = false;
  emit('nameChange', value);
};

const serverChange = (data: HTTPConfig['request']['server']) => {
  server.value = data;
};

const serverUrlChange = () => {
  serverUrlFormatError.value = false;
  serverUrlError.value = false;
};

const serverUrlBlur = (event: { target: { value: string } }) => {
  const value = event.target.value;
  if (!value) {
    return;
  }

  // 校验URL是否合法
  if (!isValidServerUrl(value)) {
    return;
  }

  const [href, search] = value.split('?');
  if (search) {
    const parameters: ParameterConfig[] = search.split('&').map(item => {
      const [name, value] = item.split('=');
      return {
        name,
        value,
        in: 'query',
        type: 'string',
        enabled: true,
        id: utils.uuid(),
        disabled: false
      };
    });

    addQueryParameters(parameters);
    server.value.url = href;
  }
};

const serverUrlMouseenter = () => {
  clearTimeout(timer);
  timer = setTimeout(() => {
    serverUrlEnterFlag.value = true;
  }, 600);
};

const serverUrlMouseleave = () => {
  clearTimeout(timer);
  timer = setTimeout(() => {
    serverUrlEnterFlag.value = false;
  }, 600);
};

const popoverMouseenter = () => {
  serverUrlEnterFlag.value = true;
  clearTimeout(timer);
};

const popoverMouseleave = () => {
  serverUrlEnterFlag.value = false;
  clearTimeout(timer);
};

const endpointChange = () => {
  endpointFormatError.value = false;
  endpointError.value = false;
};

const endpointBlur = (event: { target: { value: string } }) => {
  const value = event.target.value;
  if (!value) {
    return;
  }

  // 校验URL是否合法
  if (!isValidEndpoint(value)) {
    return;
  }

  const [pathname, search] = value.split('?');
  if (search) {
    const parameters: ParameterConfig[] = search.split('&').map(item => {
      const [name, value] = item.split('=');
      return {
        name,
        value,
        in: 'query',
        type: 'string',
        enabled: true,
        id: utils.uuid(),
        disabled: false
      };
    });

    addQueryParameters(parameters);
    endpoint.value = pathname;
  }

  addPathParameters(pathname);
};

const addQueryParameters = (data: ParameterConfig[]) => {
  let parameterMap: { [key: string]: ParameterConfig };
  let defaultQueryNames: { [key: string]: ParameterConfig };
  const queryNames = queryPathParams.value.filter(item => item.in === 'query').map(item => item.name);
  for (let i = 0, len = data.length; i < len; i++) {
    const item = data[i];
    if (queryNames.includes(item.name)) {
      if (!parameterMap) {
        parameterMap = queryPathParams.value.filter(item => item.in === 'query').reduce((prev, cur) => {
          prev[cur.name] = cur;
          return prev;
        }, {});
      }

      if (!defaultQueryNames) {
        defaultQueryNames = defaultQueryPathParams.value.filter(item => item.in === 'query').reduce((prev, cur) => {
          prev[cur.name] = cur;
          return prev;
        }, {});
      }

      parameterMap[item.name].enabled = true;
      parameterMap[item.name].value = item.value;
      defaultQueryNames[item.name].enabled = true;
      defaultQueryNames[item.name].value = item.value;
    } else {
      queryPathParams.value.push({ ...item });
      defaultQueryPathParams.value.push({ ...item });
    }
  }
};

const addPathParameters = (pathname = '') => {
  const paths = pathname.match(/{[^{}]+}/gi)?.map(item => {
    return item?.replace(/{(\S*)}/gi, '$1') || '';
  }) || [];
  if (paths?.length) {
    const defaultPathMap = defaultQueryPathParams.value.filter(item => item.in === 'path').reduce((prev, cur) => {
      prev[cur.name] = cur;
      return prev;
    }, {});

    const pathMap = queryPathParams.value.filter(item => item.in === 'path').reduce((prev, cur) => {
      prev[cur.name] = cur;
      return prev;
    }, {});

    const pathNames = queryPathParams.value.filter(item => item.in === 'path').map(item => item.name);
    for (let i = 0, len = paths.length; i < len; i++) {
      const name = paths[i];
      if (!pathNames.includes(name)) {
        const parameterItem = {
          name,
          type: 'string',
          in: 'path',
          enabled: true,
          value: '',
          disabled: false,
          id: utils.uuid()
        };
        defaultQueryPathParams.value.push(parameterItem);
        queryPathParams.value.push(parameterItem);
      } else {
        defaultPathMap[name].enabled = true;
        pathMap[name].enabled = true;
      }
    }
  }

  // 过滤不在path中的变量
  defaultQueryPathParams.value = defaultQueryPathParams.value.map(item => {
    if (item.in === 'path') {
      return {
        ...item,
        enabled: paths.includes(item.name)
      };
    }

    return item;
  });
  queryPathParams.value = queryPathParams.value.map(item => {
    if (item.in === 'path') {
      return {
        ...item,
        enabled: paths.includes(item.name)
      };
    }

    return item;
  });
};

const openChange = (_open: boolean) => {
  if (_open) {
    activeKey.value = UUID;
    return;
  }

  activeKey.value = '';
};

const enabledChange = (_enabled: boolean) => {
  enabled.value = _enabled;
  emit('enabledChange', _enabled);
};

const actionClick = (value: 'delete' | 'clone') => {
  emit('click', value);
};

const authenticationToHeader = (data: {
  type: 'http' | 'apiKey' | 'oauth2';
  value: string;
  apiKeys: {
    name: string;
    in: 'header' | 'query';
    value: string;
  }[];
  oauth2: {
    clientCredentials: {
      clientIn: string;
      tokenUrl: string;
      refreshUrl: string;
      clientId: string;
      clientSecret: string;
      scopes: string[];
    };
    password: {
      clientIn: string;
      tokenUrl: string;
      refreshUrl: string;
      clientId: string;
      clientSecret: string;
      username: string;
      password: string;
      scopes: string[];
    };
    newToken: boolean;
  }
}) => {
  if (!data) {
    return [];
  }

  const type = data.type;
  const value = data.value;
  const apiKeys = data.apiKeys || [];
  const result:{name:string;value:string}[] = [];
  if (type === 'http') {
    result.push({
      name: 'Authorization',
      value: value
    });
  } else if (type === 'apiKey') {
    if (apiKeys.length) {
      for (let i = 0, len = apiKeys.length; i < len; i++) {
        const item = apiKeys[i];
        if (item.in === 'header') {
          result.push({
            name: item.name,
            value: item.value
          });
        }
      }
    }
  } else if (type === 'oauth2') {
    if (data.oauth2?.newToken !== true) {
      result.push({
        name: 'access_token',
        value
      });
    }
  }

  return result;
};

const chanegAuthentication = async (data: any) => {
  authentication.value = data;
  authInHeader.value = authenticationToHeader(data);
};

const requestBodyChange = (data: HTTPConfig['request']['body']) => {
  if (!requestBody.value) {
    requestBody.value = cloneDeep(data);
    return;
  }

  for (const key in requestBody.value) {
    delete requestBody.value[key];
  }

  for (const key in data) {
    requestBody.value[key] = data[key];
  }
};

const contentTypeChange = (value: string) => {
  contentType.value = value;
};

const assertionChange = (data: HTTPConfig['assertions']) => {
  assertions.value = data;
};

const variablesChange = (data: HTTPConfig['variables']) => {
  variables.value = data;
};

const datasetsChange = (data: HTTPConfig['datasets']) => {
  datasets.value = data;
};

const parametersPathChange = (pathList) => {
  const pathUri = endpoint.value;
  // 正则变量
  let rexVar = '';
  // path类型参数替换为格式为：{name}的占位符
  const originalPath = pathUri.replace(/(\S+)\?\S*/, '$1');
  let pathname: string;
  // eslint-disable-next-line prefer-regex-literals
  const pathReg = new RegExp(/\{([a-zA-Z0-9!@$%^&*()_\-+=./]+)\}/g);
  const preUrl = originalPath;
  const uriPath = preUrl.match(pathReg); // 拿到 uri 上所有 {} 部分
  if (pathList?.length) {
    let tempPath = pathList.reduce((prevValue, currentValue) => {
      rexVar += '(?!' + currentValue.name + ')';
      return prevValue;
    }, originalPath);
    if (pathList?.length > (uriPath?.length || 0)) {
      tempPath += `/{${pathList?.[pathList?.length - 1].name}}`;
    }
    uriPath?.forEach((i, idx) => {
      tempPath = tempPath.replace(i, pathList[idx]?.name ? `{${pathList[idx].name}}` : '');
    });
    const pattern = new RegExp('{\\b(' + rexVar + '\\w)+\\b}', 'gi');
    pathname = tempPath.replace(pattern, '');
  } else {
    pathname = originalPath.replace(/{\S+}/g, '');
  }
  pathname = pathname.replace(/\/{2,}/g, '/').replace(/\/$/, '');
  endpoint.value = pathname;
};

const parametersCookieChange = (data: ParameterConfig[]) => {
  cookieParams.value = data;
};

const parametersHeaderChange = (data: ParameterConfig[]) => {
  headerParams.value = data;
};

const parametersQueryChange = (data: ParameterConfig[]) => {
  queryPathParams.value = data;
};

const serverDefaultValueChange = (key: string, value: string) => {
  if (server.value.variables?.[key]) {
    server.value.variables[key].defaultValue = value;
  }
};

const initializedData = async () => {
  if (!props.value) {
    return;
  }

  const newValue = cloneDeep(props.value);
  const {
    request,
    apisId: _apisId,
    caseId: _caseId,
    name: _name,
    assertions: _assertions,
    variables: _variables,
    datasets: _datasets,
    actionOnEOF: _actionOnEOF,
    sharingMode: _sharingMode,
    description: _description,
    enabled: _enabled
  } = newValue;

  name.value = _name;
  description.value = _description;
  enabled.value = _enabled;
  apisId.value = _apisId;
  caseId.value = _caseId;

  actionOnEOF.value = _actionOnEOF?.value || _actionOnEOF || 'RECYCLE';
  sharingMode.value = _sharingMode?.value || _sharingMode || 'ALL_THREAD';

  assertions.value = _assertions || [];
  defaultAssertions.value = cloneDeep(_assertions);

  variables.value = _variables || [];
  defaultVariables.value = cloneDeep(_variables);

  datasets.value = _datasets || [];
  defaultDatasets.value = cloneDeep(_datasets);

  if (request) {
    httpMethod.value = request.method;
    requestBody.value = request.body || {};

    authentication.value = request.authentication ? { ...request.authentication, ...(request.authentication.extensions || {}) } : { type: null };
    if (authentication.value) {
      defaultAuthentication.value = JSON.parse(JSON.stringify(authentication.value));
      authInHeader.value = authenticationToHeader(authentication.value);
    }

    let _contentType: string;
    const parameters = request.parameters || [];
    for (let i = 0, len = parameters.length; i < len; i++) {
      const data: ParameterConfig = {
        ...parameters[i],
        id: utils.uuid(),
        type: 'string',
        disabled: false
      };
      const _in = data.in;
      if (_in === 'query' || _in === 'path') {
        defaultQueryPathParams.value.push({ ...data });
        queryPathParams.value.push({ ...data });
      } else if (_in === 'cookie') {
        defaultCookieParams.value.push({ ...data });
        cookieParams.value.push({ ...data });
      } else {
        if (data.name === 'Content-Type') {
          _contentType = data.value;
        } else {
          defaultHeaderParams.value.push({ ...data });
          headerParams.value.push({ ...data });
        }
      }
    }

    const _server = request.server;
    if (_server) {
      server.value.url = _server.url;
      server.value.description = _server.description;
      server.value.variables = _server.variables;
    }

    if (!_contentType) {
      contentType.value = null;
    } else {
      contentType.value = _contentType;
      if (NO_BINARY_TYPES.includes(_contentType)) {
        contentType.value = _contentType;
      } else {
        contentType.value = STREAM_TYPE;
      }
    }

    if (requestBody.value) {
      const { forms, rawContent, fileName } = requestBody.value;
      // 修复Content-Type为空时，自动设置Content-Type
      if (!contentType.value) {
        if (forms?.length) {
          contentType.value = ENCODED_TYPE;
        } else {
          if (rawContent?.length) {
            if (fileName) {
              contentType.value = STREAM_TYPE;
            } else {
              contentType.value = '*/*';
            }
          }
        }
      }
    }

    endpoint.value = request.endpoint;
    addPathParameters(endpoint.value);
  }
};

const isValidServerUrl = (urlStr: string): boolean => {
  // 不能包含多个: 和多个//
  if (/[^:]*:[^:]*:[^:]*:[^:]*/.test(urlStr) || /.*\/\/.*\/\/.*/.test(urlStr)) {
    serverUrlError.value = true;
    serverUrlFormatError.value = true;
    return false;
  }

  // url中包含变量
  if (/[{|}]/g.test(urlStr)) {
    // 不能出现{{、}}
    if (/\{{2,}|\}{2,}/g.test(urlStr)) {
      serverUrlError.value = true;
      serverUrlFormatError.value = true;
      return false;
    }

    // 不能出现大括号中字符为空
    if (/\{\}/g.test(urlStr)) {
      serverUrlError.value = true;
      serverUrlFormatError.value = true;
      return false;
    }

    // 不能出现}{
    if (/\}\{/g.test(urlStr)) {
      serverUrlError.value = true;
      serverUrlFormatError.value = true;
      return false;
    }

    // 大括号必须闭合
    if (!checkBrackets(urlStr)) {
      serverUrlError.value = true;
      serverUrlFormatError.value = true;
      return false;
    }

    // 大括号中的内容不能包含/
    if (!checkBracketsContent(urlStr)) {
      serverUrlError.value = true;
      serverUrlFormatError.value = true;
      return false;
    }

    // 必须以http、https或者变量开始
    if (!/^https?:\/\/[^/]/.test(urlStr) && !/^\{[^{}]+\}/.test(urlStr)) {
      serverUrlError.value = true;
      serverUrlFormatError.value = true;
      return false;
    }

    return true;
  }

  if (!/^https?:\/\/[^/]/.test(urlStr)) {
    serverUrlError.value = true;
    serverUrlFormatError.value = true;
    return false;
  }

  try {
    // eslint-disable-next-line no-new
    new URL(urlStr);
    return true;
  } catch (error) {
    serverUrlError.value = true;
    serverUrlFormatError.value = true;
    return false;
  }
};

const isValidEndpoint = (pathStr: string): boolean => {
  // 不能包含多个连续的/
  if (/\/{2}/g.test(pathStr)) {
    endpointError.value = true;
    endpointFormatError.value = true;
    return false;
  }

  // 不能出现{{、}}
  if (/\{{2,}|\}{2,}/g.test(pathStr)) {
    endpointError.value = true;
    endpointFormatError.value = true;
    return false;
  }

  // 不能出现大括号中字符为空
  if (/\{\}/g.test(pathStr)) {
    endpointError.value = true;
    endpointFormatError.value = true;
    return false;
  }

  // 不能出现}{
  if (/\}\{/g.test(pathStr)) {
    endpointError.value = true;
    endpointFormatError.value = true;
    return false;
  }

  // 大括号必须闭合
  if (!checkBrackets(pathStr)) {
    endpointError.value = true;
    endpointFormatError.value = true;
    return false;
  }

  // 大括号中的内容不能包含/
  if (!checkBracketsContent(pathStr)) {
    endpointError.value = true;
    endpointFormatError.value = true;
    return false;
  }

  // 必须以/开头或者变量开头
  if (!/^\//.test(pathStr) && !/^\{[^{}]+\}/.test(pathStr)) {
    endpointError.value = true;
    endpointFormatError.value = true;
    return false;
  }

  return true;
};

// 检测字符串的所有大括号是否闭合
const checkBrackets = (str: string): boolean => {
  const stack = [];
  const openingBrackets = ['{'];
  const closingBrackets = ['}'];

  for (const char of str) {
    if (openingBrackets.includes(char)) {
      stack.push(char);
    } else if (closingBrackets.includes(char)) {
      if (stack.length === 0) {
        return false; // 出现了多余的闭合括号
      }
      stack.pop();
    }
  }

  return stack.length === 0; // 如果栈为空，表示所有括号都闭合
};

// 检测大括号的内容是否包含/
const checkBracketsContent = (str: string): boolean => {
  // eslint-disable-next-line no-useless-escape
  const regex = /\{([^\{\}]+)\}/g;
  const matches = str.match(regex);

  if (!matches) {
    return true; // 没有大括号或大括号内容为空，符合条件
  }

  for (const match of matches) {
    const content = match.slice(1, -1); // 提取大括号内的内容
    if (content.match(/[/]/)) {
      return false; // 大括号内容包含指定字符，不符合条件
    }
  }

  return true; // 所有大括号的内容均符合条件
};

const isValid = (): boolean => {
  nameError.value = false;
  serverUrlError.value = false;
  serverUrlFormatError.value = false;
  endpointError.value = false;
  endpointFormatError.value = false;
  nameRepeatFlag.value = false;

  let errorNum = 0;
  if (!name.value) {
    errorNum++;
    nameError.value = true;
  } else {
    if (props.repeatNames.includes(name.value)) {
      nameError.value = true;
      nameRepeatFlag.value = true;
    }
  }

  if (!readonly.value) {
    if (!server.value.url) {
      errorNum++;
      serverUrlError.value = true;
    } else {
      if (!isValidServerUrl(server.value.url)) {
        errorNum++;
      }
    }

    if (endpoint.value) {
      if (!isValidEndpoint(endpoint.value)) {
        errorNum++;
      }
    }
  }

  if (typeof queryPathRef.value?.isValid === 'function') {
    if (!queryPathRef.value.isValid()) {
      errorNum++;
    }
  }

  if (typeof headerRef.value?.isValid === 'function') {
    if (!headerRef.value.isValid()) {
      errorNum++;
    }
  }

  if (typeof cookieRef.value?.isValid === 'function') {
    if (!cookieRef.value.isValid()) {
      errorNum++;
    }
  }

  if (typeof bodyRef.value?.isValid === 'function') {
    if (!bodyRef.value.isValid()) {
      errorNum++;
    }
  }

  if (typeof assertRef.value?.isValid === 'function') {
    if (!assertRef.value.isValid()) {
      errorNum++;
    }
  }

  if (typeof parametricRef.value?.isValid === 'function') {
    if (!parametricRef.value.isValid()) {
      errorNum++;
    }
  }

  return !errorNum;
};

const tabActiveKey = ref<string>();
onMounted(() => {
  emit('renderChange');
  initializedData();

  watch(() => popoverVisible.value, (newValue) => {
    if (!newValue || popoverDom.value) {
      return;
    }

    setTimeout(() => {
      popoverDom.value = document.getElementsByClassName(overlayClassName)?.[0];
      if (popoverDom.value) {
        popoverDom.value.removeEventListener('mouseenter', popoverMouseenter);
        popoverDom.value.addEventListener('mouseenter', popoverMouseenter);

        popoverDom.value.removeEventListener('mouseleave', popoverMouseleave);
        popoverDom.value.addEventListener('mouseleave', popoverMouseleave);
      }
    }, 100);
  }, { immediate: true });

  watch(() => props.repeatNames, (newValue) => {
    if (newValue.includes(name.value)) {
      nameError.value = true;
      nameRepeatFlag.value = true;
      return;
    }

    if (nameRepeatFlag.value) {
      nameError.value = false;
      nameRepeatFlag.value = false;
    }
  });

  watchEffect(() => {
    const data = getData();
    emit('change', data);
  });
});

onBeforeUnmount(() => {
  if (popoverDom.value) {
    popoverDom.value.removeEventListener('mouseenter', popoverMouseenter);
    popoverDom.value.removeEventListener('mouseleave', popoverMouseleave);
  }
});

const hasRequestBody = computed(() => {
  const body = requestBody.value;
  if (body && (body.forms?.length || body.rawContent?.length)) {
    return true;
  }

  if (contentType.value) {
    return true;
  }

  return false;
});

const pureHeaderParams = computed(() => {
  const data: ParameterConfig[] = [];
  if (authInHeader.value?.length) {
    const _data: ParameterConfig[] = authInHeader.value.map(({ name, value }) => {
      return {
        name,
        value,
        enabled: true,
        disabled: true,
        id: utils.uuid(),
        in: 'header',
        type: 'string'
      };
    });

    data.push(..._data);
  }

  if (contentType.value) {
    data.push({
      name: 'Content-Type',
      value: contentType.value,
      enabled: true,
      disabled: true,
      id: utils.uuid(),
      in: 'header',
      type: 'string'
    });
  }

  return data;
});

const assertionNum = computed(() => {
  return assertions.value.length;
});

const headerNum = computed(() => {
  return headerParams.value.length + pureHeaderParams.value.length;
});

const queryPathNum = computed(() => {
  return queryPathParams.value.length;
});

const cookieNum = computed(() => {
  return cookieParams.value.length;
});

const readonly = computed(() => {
  return !!apisId.value || !!caseId.value;
});

const serverVariables = computed(() => {
  const variables = server.value.variables;
  if (!variables) {
    return [];
  }

  return Object.entries(variables).map(([_name, value]) => {
    return {
      _name,
      ...value
    };
  });
});

const popoverVisible = computed(() => {
  if (serverUrlEnterFlag.value) {
    return true;
  }

  return false;
});

const getData = (): Omit<HTTPConfig, 'id'> => {
  const parameters: HTTPConfig['request']['parameters'] = [];
  if (queryPathParams.value.length) {
    const list = queryPathParams.value;
    for (let i = 0, len = list.length; i < len; i++) {
      const { enabled, type, in: _in, value, name } = list[i];
      parameters.push({
        enabled,
        type,
        in: _in,
        value,
        name
      });
    }
  }

  if (headerParams.value.length) {
    const list = headerParams.value;
    for (let i = 0, len = list.length; i < len; i++) {
      const { enabled, type, in: _in, value, name } = list[i];
      parameters.push({
        enabled,
        type,
        in: _in,
        value,
        name
      });
    }
  }

  if (contentType.value) {
    parameters.push({
      name: 'Content-Type',
      value: contentType.value,
      enabled: true,
      in: 'header',
      type: 'string'
    });
  }

  if (cookieParams.value.length) {
    const list = cookieParams.value;
    for (let i = 0, len = list.length; i < len; i++) {
      const { enabled, type, in: _in, value, name } = list[i];
      parameters.push({
        enabled,
        type,
        in: _in,
        value,
        name
      });
    }
  }

  return {
    apisId: apisId.value,
    caseId: caseId.value,
    beforeName: '',
    transactionName: '',
    description: description.value,
    enabled: enabled.value,
    name: name.value,
    assertions: assertions.value,
    variables: variables.value,
    datasets: datasets.value,
    actionOnEOF: actionOnEOF.value,
    sharingMode: sharingMode.value,
    request: {
      method: httpMethod.value,
      server: server.value,
      endpoint: endpoint.value,
      body: requestBody.value,
      authentication: authentication.value,
      parameters
    },
    target: 'HTTP'
  };
};

defineExpose({
  getData,
  isValid,
  getName: (): string => {
    return name.value;
  },
  validateRepeatName: (value: string[]): boolean => {
    if (value.includes(name.value)) {
      nameError.value = true;
      nameRepeatFlag.value = true;
      return false;
    }

    return true;
  }
});

const optionStyle = {
  GET: 'color:rgba(30, 136, 229, 1);',
  HEAD: 'color:rgba(255, 82, 82, 1);',
  POST: 'color:rgba(51, 183, 130, 1);',
  PUT: 'color:rgba(255, 167, 38, 1);',
  PATCH: 'color:rgba(171, 71, 188, 1);',
  DELETE: 'color:rgba(255, 82, 82, 1);',
  OPTIONS: 'color:rgba(255, 82, 82, 1);',
  TRACE: 'color:rgba(255, 82, 82, 1);'
};

const overlayStyle = {
  minWidth: '300px',
  maxWidth: '450px'
};
</script>

<template>
  <Collapse
    :activeKey="activeKey"
    :class="{ 'opacity-70': !enabled && props.enabled }"
    class="HTTP-collapse-container"
    :bordered="false">
    <CollapsePanel
      :key="UUID"
      :showArrow="false"
      style="background-color: rgba(251, 251, 251, 100%);"
      collapsible="disabled">
      <template #header>
        <div class="flex items-center flex-nowrap w-full whitespace-nowrap">
          <template v-if="caseId">
            <Icon class="flex-shrink-0 text-4 mr-3" icon="icon-cese" />
          </template>
          <template v-else>
            <Icon
              v-if="apisId"
              class="flex-shrink-0 text-4 mr-3"
              icon="icon-yinyonghttp" />
            <Icon
              v-else
              class="flex-shrink-0 text-4 mr-3"
              icon="icon-httpcanshu" />
          </template>
          <div class="flex-1 flex items-center space-x-5 mr-5">
            <Tooltip
              title="名称重复"
              internal
              placement="right"
              destroyTooltipOnHide
              :visible="nameRepeatFlag">
              <Input
                :value="name"
                :maxlength="400"
                :error="nameError"
                :title="name"
                trim
                class="http-name-input"
                placeholder="名称，最长400个字符"
                @change="nameChange" />
            </Tooltip>
            <SelectEnum
              v-model:value="httpMethod"
              :border="false"
              :showArrow="false"
              :optionStyle="optionStyle"
              :class="httpMethod"
              :disabled="readonly"
              optionLabelProp="label"
              class="w-19 flex-shrink-0 http-method-select"
              enumKey="HttpMethod"
              placeholder="请求方法" />
            <ServerEndpoint
              v-if="readonly"
              :server="server"
              :endpoint="endpoint"
              @change="serverChange" />
            <Composite v-else class="bg-white">
              <Popover
                v-model:visible="popoverVisible"
                :overlayStyle="overlayStyle"
                :overlayClassName="overlayClassName"
                placement="bottomLeft"
                @mouseenter="serverUrlMouseenter"
                @mouseleave="serverUrlMouseleave">
                <template #content>
                  <template v-if="serverVariables">
                    <div
                      v-for="_variable in serverVariables"
                      :key="_variable._name"
                      class="leading-5 text-3 space-y-1 mb-3">
                      <div class="flex items-center space-x-2">
                        <div class="w-1 h-1 rounded-lg bg-slate-600"></div>
                        <div class="text-theme-title font-bold">{{ _variable._name }}</div>
                      </div>
                      <div class="text-theme-content space-y-0.5">
                        <div
                          v-for="_value in _variable.allowableValues"
                          :key="_value"
                          class="flex items-center pl-7">
                          <div class="flex-1 truncate">{{ _value }}</div>
                          <div class="flex items-center flex-shrink-0 space-x-1">
                            <div
                              v-if="server.variables?.[_variable._name]?.defaultValue === _value"
                              class="text-theme-sub-content">
                              <span>默认</span>
                            </div>
                            <Radio
                              :checked="server.variables?.[_variable._name]?.defaultValue === _value"
                              @change="serverDefaultValueChange(_variable._name, _value)" />
                          </div>
                        </div>
                      </div>
                    </div>
                  </template>
                </template>
                <template #title>
                  <div class="leading-4 py-2 space-y-2">
                    <div class="text-theme-title font-bold break-all">{{ server.url }}</div>
                    <div class="text-theme-content break-all">{{ server.description }}</div>
                  </div>
                </template>
                <Tooltip
                  title="server格式错误"
                  internal
                  placement="topLeft"
                  destroyTooltipOnHide
                  :visible="serverUrlFormatError">
                  <Input
                    v-model:value="server.url"
                    :error="serverUrlError"
                    :maxlength="400"
                    :title="server.url"
                    trimAll
                    style="flex:1 1 40%;"
                    placeholder="server，最长400个字符"
                    @change="serverUrlChange"
                    @blur="serverUrlBlur" />
                </Tooltip>
              </Popover>
              <Tooltip
                title="path格式错误"
                internal
                placement="topLeft"
                destroyTooltipOnHide
                :visible="endpointFormatError">
                <Input
                  v-model:value="endpoint"
                  :error="endpointError"
                  :maxlength="800"
                  :title="endpoint"
                  trimAll
                  style="flex:1 1 60%;"
                  placeholder="path，最长800个字符"
                  @change="endpointChange"
                  @blur="endpointBlur" />
              </Tooltip>
            </Composite>
          </div>
          <ActionsGroup
            v-model:enabled="enabled"
            :open="activeKey === UUID"
            :arrowVisible="true"
            @openChange="openChange"
            @enabledChange="enabledChange"
            @click="actionClick" />
        </div>
      </template>

      <Tabs
        v-model:activeKey="tabActiveKey"
        size="small"
        class="white-bg-container">
        <TabPane key="query">
          <template #tab>
            <Badge
              class="count-Badge-container"
              size="small"
              :count="queryPathErrorNum">
              <div class="flex items-center space-x-0.5">
                <div>请求参数</div>
                <div class="flex items-center space-x-0.5">
                  <em>(</em>
                  <span>{{ queryPathNum }}</span>
                  <em>)</em>
                </div>
              </div>
            </Badge>
          </template>
          <ParameterInput
            ref="queryPathRef"
            v-model:errorNum="queryPathErrorNum"
            defaultIn="query"
            :readonly="readonly"
            :enabled="props.enabled"
            :defaultValue="defaultQueryPathParams"
            :showInType="true"
            @changePath="parametersPathChange"
            @change="parametersQueryChange" />
        </TabPane>
        <TabPane key="header">
          <template #tab>
            <Badge
              class="count-Badge-container"
              size="small"
              :count="headerErrorNum">
              <div class="flex items-center space-x-0.5">
                <div>请求头</div>
                <div class="flex items-center space-x-0.5">
                  <em>(</em>
                  <span>{{ headerNum }}</span>
                  <em>)</em>
                </div>
              </div>
            </Badge>
          </template>
          <ParameterPure :value="pureHeaderParams" />
          <ParameterInput
            ref="headerRef"
            v-model:errorNum="headerErrorNum"
            :defaultValue="defaultHeaderParams"
            :enabled="props.enabled"
            defaultIn="header"
            @change="parametersHeaderChange" />
        </TabPane>
        <TabPane key="Cookie">
          <template #tab>
            <Badge
              class="count-Badge-container"
              size="small"
              :count="cookieErrorNum">
              <div class="flex items-center space-x-0.5">
                <div>Cookie</div>
                <div class="flex items-center space-x-0.5">
                  <em>(</em>
                  <span>{{ cookieNum }}</span>
                  <em>)</em>
                </div>
              </div>
            </Badge>
          </template>
          <ParameterInput
            ref="cookieRef"
            v-model:errorNum="cookieErrorNum"
            :enabled="props.enabled"
            :defaultValue="defaultCookieParams"
            defaultIn="cookie"
            @change="parametersCookieChange" />
        </TabPane>
        <TabPane key="Authorization">
          <template #tab>
            <Badge v-if="!!authentication?.type" color="green" />
            <span>Authorization</span>
          </template>
          <ExecAuthencation :defaultValue="defaultAuthentication" @change="chanegAuthentication" />
        </TabPane>
        <TabPane key="body">
          <template #tab>
            <Badge v-if="hasRequestBody" color="green" />
            <Badge size="small" :count="bodyErrorNum">请求体</Badge>
          </template>
          <RequestBody
            ref="bodyRef"
            v-model:errorNum="bodyErrorNum"
            :contentType="contentType"
            :value="requestBody"
            @change="requestBodyChange"
            @contentTypeChange="contentTypeChange" />
        </TabPane>
        <TabPane key="parametric">
          <template #tab>
            <Badge
              class="count-Badge-container"
              size="small"
              :count="parametricErrorNum">
              <div class="flex items-center space-x-0.5">
                <div>参数化</div>
              </div>
            </Badge>
          </template>
          <Parametric
            ref="parametricRef"
            v-model:errorNum="parametricErrorNum"
            v-model:actionOnEOF="actionOnEOF"
            v-model:sharingMode="sharingMode"
            :variables="defaultVariables"
            :datasets="defaultDatasets"
            @variablesChange="variablesChange"
            @datasetsChange="datasetsChange" />
        </TabPane>
        <TabPane key="assert">
          <template #tab>
            <Badge
              class="count-Badge-container"
              size="small"
              :count="assertionErrorNum">
              <div class="flex items-center space-x-0.5">
                <div>断言</div>
                <div class="flex items-center space-x-0.5">
                  <em>(</em>
                  <span>{{ assertionNum }}</span>
                  <em>)</em>
                </div>
              </div>
            </Badge>
          </template>
          <AssertionForm
            ref="assertRef"
            v-model:errorNum="assertionErrorNum"
            :defaultValue="defaultAssertions"
            :enabled="props.enabled"
            @change="assertionChange" />
        </TabPane>
        <template #rightExtra>
          <FunctionsButton class="text-3.5 mr-2" />
          <Popover placement="leftTop" :trigger="['click']">
            <Icon icon="icon-jieshaoshuoming" class="cursor-pointer mr-2 text-4 text-theme-text-hover" />
            <template #content>
              <div class="w-100 text-3">
                <Hints text="注意事项" class="font-semibold !text-text-title" />
                <ul class="mt-2 pl-4 list-disc">
                  <li
                    v-for="item in debugTip"
                    :key="item"
                    class="mt-2 text-text-sub-content text-3">
                    {{ item }}
                  </li>
                </ul>
              </div>
            </template>
          </Popover>
          <Popover placement="leftTop" :trigger="['click']">
            <Icon icon="icon-tiaoshijiaoben" class="cursor-pointer mr-2 text-3.5 text-theme-text-hover" />
            <template #content>
              <div class="w-150 text-3">
                <Hints text="参数序列化" class="font-semibold !text-text-title" />
                <div class="mt-2">
                  AngusTester 序列化规则基于
                  <a
                    class="text-theme-special"
                    href="https://datatracker.ietf.org/doc/html/rfc6570"
                    target="_blank">RFC6570</a>
                  定义的UR模板的子集。支持OAS3 Schema数据类型包括：string、boolean、integer、number、object、array。
                </div>
                <ul class="pl-4 list-disc">
                  <li
                    v-for="item in docInfo"
                    :key="item.title"
                    class="mt-2">
                    <div class="font-semibold">{{ item.title }}</div>
                    <div
                      v-for="text in item.rules"
                      :key="text"
                      class="text-text-sub-content text-3">
                      {{ text }}
                    </div>
                  </li>
                </ul>
              </div>
            </template>
          </Popover>
        </template>
      </Tabs>
    </CollapsePanel>
  </Collapse>
</template>
<style scoped>
.ant-tabs> :deep(.ant-tabs-nav) {
  padding-top: 4px;
}

.http-name-input {
  flex: 0 0 calc((100% - (121px))*2/5);
}

.http-name-input :deep(.ant-input-disabled) {
  color: var(--content-text-content);
}

.child-drag-container .http-name-input {
  flex: 0 0 calc((100% - (131px))*2/5);
}

.ant-collapse.HTTP-collapse-container {
  line-height: 20px;
}

.ant-collapse-borderless.HTTP-collapse-container> :deep(.ant-collapse-item) {
  border: none;
  border-radius: 4px;
}

.ant-collapse.HTTP-collapse-container> :deep(.ant-collapse-item)>.ant-collapse-header {
  align-items: center;
  height: 46px;
  padding: 0 12px 0 38px;
  border-radius: 4px;
  background-color: rgba(239, 240, 243, 100%);
  line-height: 20px;
}

.HTTP-collapse-container :deep(.ant-collapse-content)>.ant-collapse-content-box {
  padding: 0 14px 14px;
}

.ant-input-affix-wrapper:not(.ant-input-affix-wrapper-disabled) {
  background-color: #fff;
}

.http-method-select.ant-select {
  border-radius: 4px;
}

.http-method-select.ant-select :deep(.ant-select-selector) {
  border: none !important;
  background-color: transparent !important;
  color: #fff;
}

.http-method-select.ant-select :deep(.ant-select-selector) .ant-select-selection-item {
  line-height: 28px;
  text-align: center;
}

.http-method-select.ant-select.GET {
  background-color: rgba(30, 136, 229, 100%);
}

.http-method-select.ant-select.HEAD {
  background-color: rgba(255, 82, 82, 100%);
}

.http-method-select.ant-select.POST {
  background-color: rgba(51, 183, 130, 100%);
}

.http-method-select.ant-select.PUT {
  background-color: rgba(255, 167, 38, 100%);
}

.http-method-select.ant-select.PATCH {
  background-color: rgba(171, 71, 188, 100%);
}

.http-method-select.ant-select.DELETE {
  background-color: rgba(255, 82, 82, 100%);
}

.http-method-select.ant-select.OPTIONS {
  background-color: rgba(255, 82, 82, 100%);
}

.http-method-select.ant-select.TRACE {
  background-color: rgba(255, 82, 82, 100%);
}

em {
  font-style: normal;
}

.ant-badge {
  color: inherit;
}

.count-Badge-container :deep(.ant-badge-count) {
  top: -2px;
  right: -5px;
}
</style>

<style>
.white-bg-container .ant-input-affix-wrapper:not(.ant-input-affix-wrapper-disabled),
.white-bg-container .ant-input:not(.ant-input-disabled) {
  background-color: #fff;
}

.white-bg-container .ant-select:not(.ant-select-disabled) .ant-select-selector {
  background-color: #fff;
}

.white-bg-container .ant-checkbox-wrapper:not(.ant-checkbox-wrapper-disabled, .ant-checkbox-wrapper-checked) .ant-checkbox-inner {
  background-color: #fff;
}

.white-bg-container .ant-radio-wrapper:not(.ant-radio-wrapper-disabled) .ant-radio-inner {
  background-color: #fff;
}
</style>
