import { utils } from '@xcan-angus/tools';
import { ParamsItem } from '@/views/apis/services/apiHttp/interface';
import { blobToDataURL } from '@/utils/common';
import { blobTobase64, dataURLtoBlob, getBlobByUrl } from '@/utils/blob';
import { decode } from '@/utils/secure';
import { mock, services } from 'src/api/tester';

const isUrl = (url) => {
  if (!url) {
    return false;
  }
  try {
    // eslint-disable-next-line no-new
    new URL(url);
    return true;
  } catch {
    return false;
  }
};

const getTextValue = (value?: string) => {
  const div = document.createElement('div');
  div.innerHTML = value || '';
  return div.innerText;
};

const getAuthData = (authorizationData, secured) => {
  let stateAuthoriData = JSON.parse(JSON.stringify(authorizationData));
  if (secured) {
    switch (stateAuthoriData.type) {
      case 'BASIC_AUTH':
        // eslint-disable-next-line no-case-declarations
        const decodeMap = decode(stateAuthoriData.value.replace(/Basic\s+/, ''));
        if (!decodeMap.name) {
          stateAuthoriData = null;
        }
        break;
      case 'BEARER_TOKEN':
        if (stateAuthoriData.value === 'Bearer ') {
          stateAuthoriData = null;
        }
        break;
      case 'CUSTOM':
        if (!stateAuthoriData.name) {
          stateAuthoriData = null;
        }
        break;
    }
  } else {
    stateAuthoriData = null;
  }

  return stateAuthoriData;
};

const getFunParamsValue = (param:ParamsItem[][], allFuncNames):Promise<ParamsItem[][]> => {
  // eslint-disable-next-line prefer-regex-literals
  const funcReg = new RegExp(/@[a-zA-Z0-9]+\(.*\)/);
  // eslint-disable-next-line prefer-regex-literals
  const replaceReg = new RegExp(/\(.*\)/, 'g');

  const paramLists = param.map(paramList => paramList.map(param => ({ ...param })));

  const allParams = Array.prototype.concat.apply([], paramLists);
  allParams.forEach((param) => {
    let isFunc = false;
    const matchRes = getTextValue(param.value)?.match(funcReg);
    if (matchRes?.length) {
      isFunc = matchRes.some(i => allFuncNames.includes(i?.replace(replaceReg, '()').replace('@', '')));
    }
    param.func = isFunc;
  });
  return new Promise((resolve) => {
    if (allParams.some(i => i.func)) {
      mock.generateFunctionValue({ iterations: 1, texts: allParams.filter(i => i.func).map(i => getTextValue(i.value)) }).then(resp => {
        const [error, res] = resp;
        if (error) {
          // eslint-disable-next-line prefer-promise-reject-errors
          resolve(allParams);
        } else {
          const response = res.data || [];
          paramLists.forEach(paramList => {
            paramList.forEach((param: any) => {
              if (param.func) {
                param.value = response[0][0];
                response.shift();
              }
              delete param.func;
            });
          });
          resolve(paramLists);
        }
      });
    } else {
      resolve(paramLists);
    }
  });
};

const getRealUri = (params: ParamsItem[], endpoint) => {
  let tempUri = endpoint || '';
  const paths:ParamsItem[] = (params)?.filter(item => item.paramType === 'PATH');
  if (paths?.length) {
    let pattern: RegExp;
    const endpoint = paths?.reduce((prevValue, currentValue) => {
      pattern = new RegExp('{' + currentValue.name + '}', 'gi');
      if (pattern.test(prevValue)) {
        prevValue = prevValue.replace(pattern, currentValue.value);
      }
      return prevValue;
    }, tempUri) || '';
    // path 中的占位符使用参数值替换
    tempUri = '/' + endpoint.replace(/^\/+/, '');
  }
  const querys: ParamsItem[] = (params)?.filter(item => item.paramType === 'QUERY' && !!item.name);
  if (querys?.length) {
    const queryUri = querys.reduce((preValue, currentValue) => {
      preValue = preValue + `${preValue ? '&' : ''}${currentValue.name}=${currentValue.value || ''}`;
      return preValue;
    }, '');
    if (tempUri.includes('?')) {
      tempUri = tempUri.split('?')[0] + '?' + queryUri;
    } else {
      tempUri = tempUri + '?' + queryUri;
    }
  }
  return tempUri;
};

const getRequestParamByApi = async (api, allFuncNames, isLocal = false) => {
  const { requestHeaders = [], requestParams = [], requestBody = { formData: [] as ParamsItem[] }, endpoint, host, method, authentication, secured } = api;
  let formData: ParamsItem[] = [];
  for (const param of (requestBody.formData)) {
    const newParam = { name: '', value: '' } as ParamsItem;
    newParam.name = param.name;
    if (param.paramType === 'FILE') {
      newParam.value = await blobToDataURL(param.value);
      newParam.value = newParam.value.split(',')[1];
    } else {
      newParam.value = param.value;
    }
    formData.push(newParam as ParamsItem);
  }
  const funcValues = await getFunParamsValue([requestParams.filter(i => i.enabled), formData.filter(i => i.enabled), requestHeaders.filter(i => i.enabled)], allFuncNames);

  let apiUri = getRealUri(funcValues[0], endpoint);
  apiUri = (host.startsWith('http') ? host : 'http://' + host) + apiUri;
  formData = funcValues[1];
  const requestBodyTemp = {
    ...requestBody,
    formData,
    contentType: requestBody.contentType?.message,
    binaryContentType: requestBody.binaryContentType?.value
  };

  // 存在文件上传 且为 文件远程地址
  if (requestBodyTemp.contentType === 'application/octet-stream' && requestBodyTemp.binaryContentType === 'URL' && isUrl(requestBodyTemp.binaryContent)) {
    requestBodyTemp.binaryContent = await getBlobByUrl(requestBodyTemp.binaryContent);
    if (!isLocal) { // 有代理, 代理发请求 文件转为 base64 格式
      requestBodyTemp.binaryContent = blobTobase64(requestBodyTemp.binaryContent);
    }
  }

  // 存在文件上传 且为 文件base64 格式
  if (requestBodyTemp.contentType === 'application/octet-stream' && requestBodyTemp.binaryContentType === 'BASE64_TEXT' && requestBodyTemp.binaryContent) {
    if (isLocal) { // 无代理, 本地发请求, 文件转为 blob 格式
      requestBodyTemp.binaryContent = dataURLtoBlob(requestBodyTemp.binaryContent);
    }
  }
  let headers = funcValues[2].filter(i => !!i.value);
  const _contentType = requestBody.contentType && requestBody.contentType !== null
    ? {
        name: 'Content-Type',
        value: requestBody.contentType
      }
    : null;
  const auth = getAuthData(authentication, secured);
  if (auth) {
    headers = headers.filter(head => head.name !== auth.name);
    headers.push(auth);
  }
  if (_contentType) {
    const typeIdx = headers.find(head => head.name === 'Content-Type');
    if (!typeIdx) {
      headers.push(_contentType);
    }
  }

  const uuid = utils.uuid('api');
  if (isLocal) {
    return { url: apiUri, method, data: { ...requestBodyTemp, binary: requestBodyTemp.binaryContent }, headers };
  }
  return { requestId: uuid, url: apiUri, method, requestBody: requestBodyTemp, requestHeaders: headers };
};

export const getDataTypeFromFormat = (format: string) => {
  switch (format) {
    case 'int32':
    case 'int64':
      return 'integer';
    case 'float':
    case 'double':
      return 'number';
    default:
      return 'string';
  }
};
const refModelsObj = {};
export const getModelDataByRef = async (projectId, ref) => {
  if (refModelsObj[projectId]?.[ref]) {
    return refModelsObj[projectId]?.[ref];
  } else {
    const fetcher = services.getRefInfo(projectId, ref);
    refModelsObj[projectId] = {
      [ref]: fetcher
    };
    fetcher.then(resp => {
      refModelsObj[projectId][ref] = resp;
    });
    setTimeout(() => {
      delete refModelsObj[projectId][ref];
    }, 2000);
    return refModelsObj[projectId]?.[ref];
  }
};
export const delRefModalInObj = (projectId, ref) => {
  if (typeof refModelsObj?.[projectId]?.[ref] === 'string') {
    delete refModelsObj?.[projectId]?.[ref];
  }
};

// eslint-disable-next-line prefer-regex-literals
const variableNameReg = new RegExp(/^[a-zA-Z0-9!@$%^&*()_\-+=./]+$/);

const CONTENT_TYPE = [
  'application/x-www-form-urlencoded',
  'multipart/form-data',
  'application/octet-stream',
  'application/json',
  'text/html',
  'application/xml',
  'application/javascript',
  'text/plain',
  '*/*'
];

const API_STATUS_COLOR_CONFIG = {
  IN_DESIGN: 'text-gray-text-light',
  IN_DEV: 'text-execute-yellow',
  DEV_COMPLETED: 'text-blue-1',
  RELEASED: 'text-status-success',
  UNKNOWN: 'text-text-content'
};

const API_STATUS_BADGE_COLOR_CONFIG = {
  IN_DESIGN: 'rgba(140, 140, 140, 1)',
  IN_DEV: 'rgba(255, 129, 0, 1)',
  DEV_COMPLETED: 'rgba(0,119,255,1)',
  RELEASED: 'rgba(82, 196, 26, 1)',
  UNKNOWN: 'rgba(82,90,101,1)'
};

const API_EXTENSION_KEY = {
  perfix: 'x-xc-', // 前缀
  valueKey: 'x-xc-value', // 值
  enabledKey: 'x-xc-enabled', // 启用/禁用
  exportVariableFlagKey: 'x-xc-exportVariableFlag', // 是否设置成变量
  requestSettingKey: 'x-xc-requestSetting', // 接口设置 例如超时时间等设置, object
  serverNameKey: 'x-xc-serverName', // 服务器URL名称
  serverSourceKey: 'x-xc-serverSource', // 服务器来源
  securityApiKeyPerfix: 'x-xc-apiKey', // apiKey 类型扩展
  securitySubTypeKey: 'sx-xc-securitySubType', // 安全方案子类型
  fileNameKey: 'x-xc-fileName', // 文件名称
  newTokenKey: 'x-xc-oauth2-newToken', // 是否使用生成认证令牌
  oAuth2Key: 'x-xc-oauth2-authFlow', // 生成令牌授权类型
  oAuth2Token: 'x-xc-oauth2-token', // 已有令牌 token
  formContentTypeKey: 'x-xc-contentType',
  basicAuthKey: 'x-xc-basicAuth',
  idKey: 'x-xc-id'
} as const;

export {
  getRequestParamByApi,
  API_EXTENSION_KEY,
  API_STATUS_COLOR_CONFIG,
  API_STATUS_BADGE_COLOR_CONFIG,
  CONTENT_TYPE,
  variableNameReg
};
