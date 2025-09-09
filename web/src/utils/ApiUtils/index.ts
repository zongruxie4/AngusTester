import qs from 'qs';
import Ajv from 'ajv';
import addFormats from 'ajv-formats';
import { uniq } from 'lodash-es';
import { decode as dt, encode as et } from 'js-base64';
import { http, utils, TESTER, codeUtils, AssertionCondition, AssertionType } from '@xcan-angus/infra';
import SwaggerUI from '@xcan-angus/swagger-ui';
import {notification} from '@xcan-angus/vue-ui';

import { Extraction } from '@/components/ApiAssert/utils/extract/PropsType';
import { getExecShowAuthData } from '@/components/ExecAuthencation/interface';
import dayjs from 'dayjs';
import duration from 'dayjs/plugin/duration';

dayjs.extend(duration);
export interface ParamsItem {
  name?: string,
  in?: string,
  [key: string]: any,
  description?: string,
  variabled?: boolean,
  enabled?: boolean,
  allowableValues?: string[] | null, // 枚举值
  valueType?: string | null,
  key?: symbol
}

export interface HostItem {
  name?: string,
  host: string,
  default0?: boolean,
  protocol: { value: string }
}

const getDefaultParams = (config = {}): ParamsItem => {
  return {
    name: '',
    in: undefined,
    description: '',
    // [exportVariableKey]: false,
    [valueKey]: '',
    [enabledKey]: true,
    schema: { type: 'string' },
    ...config
  };
};

const getBodyDefaultItem = (config = {}) => {
  return {
    name: '',
    in: undefined,
    description: '',
    // [exportVariableKey]: false,
    [valueKey]: '',
    [enabledKey]: true,
    type: 'string',
    ...config
  };
};

const API_EXTENSION_KEY = {
  perfix: 'x-xc-', // 前缀
  valueKey: 'x-xc-value', // 值
  enabledKey: 'x-xc-enabled', // 启用/禁用
  exportVariableKey: 'x-xc-exportVariable', // 是否设置成变量
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
  wsMessageKey: 'x-wsMessage'
};

const deconstruct = (data: Record<string, any>) => {
  const handler = (schema: Record<string, any>) => {
    if (Object.prototype.hasOwnProperty.call(schema, '$ref')) {
      const model = resolvedRefModels[schema.$ref];
      return handler(model);
    } else {
      for (const key in schema) {
        if (Object.prototype.toString.call(schema[key]) === '[object Object]') {
          schema[key] = handler(schema[key]);
        }
      }
    }

    return schema;
  };

  const model = JSON.parse(data.model || 'null');
  let resolvedRefModels = data.resolvedRefModels;
  if (!resolvedRefModels) {
    return model;
  }

  resolvedRefModels = Object.entries(resolvedRefModels).reduce((prev, [key, value]) => {
    const _value = (value || 'null') as string;
    prev[key] = JSON.parse(_value);
    return prev;
  }, {});

  return handler(model);
};

const { valueKey, enabledKey } = API_EXTENSION_KEY;
const ajv = new Ajv();
addFormats(ajv);

const toUrl = (url: string): URL | string => {
  if (!/^https?:\/\//.test(url)) {
    return url;
  }

  try {
    return new URL(url);
  } catch (error) {
    return url;
  }
};

const getNewItem = (metaData:ParamsItem[], config?:ParamsItem):ParamsItem|undefined => {
  const emptyItem = metaData?.find((item) => !item.name);
  if (!emptyItem) {
    return getBodyDefaultItem(config);
  }

  return undefined;
};

const getParamsByUri = (uri = ''): ParamsItem[] => {
  // 获取path类型参数
  const temp: ParamsItem[] = uri.match(/{[^{}]+}/gi)?.map((item): ParamsItem => {
    return getDefaultParams({ name: item.replace(/{(\S*)}/gi, '$1'), in: 'path' });
  }) || [];

  if (/\?/.test(uri)) {
    // 获取query类型参数
    new URLSearchParams(uri.replace(/\S+\?(\S*)/g, '$1')).forEach((value, key) => {
      temp.push(getDefaultParams({ name: key, value: value as string, in: 'query' }));
    });
  }
  return temp;
};

const getUriByParams = (uri: string, paths: ParamsItem[]): string => {
  if (!uri) {
    return '';
  }

  // const pathUri = uri.split('?')[0] || '/';
  const pathUri = uri;

  // 正则变量
  let rexVar = '';
  // path类型参数替换为格式为：{name}的占位符
  const originalPath = pathUri.replace(/(\S+)\?\S*/, '$1');
  let pathname: string;
  // eslint-disable-next-line prefer-regex-literals
  const pathReg = new RegExp(/\{([a-zA-Z0-9!@$%^&*()_\-+=./]+)\}/g);
  const preUrl = decodeURIComponent(originalPath);
  const uriPath = preUrl.match(pathReg); // 拿到 uri 上所有 {} 部分
  if (paths?.length) {
    let tempPath = paths.reduce((prevValue, currentValue) => {
      rexVar += '(?!' + currentValue.name + ')';
      return prevValue;
    }, decodeURIComponent(originalPath));
    if (paths?.length > (uriPath?.length || 0)) {
      tempPath += `/{${paths?.[paths?.length - 1].name}}`;
    }
    uriPath?.forEach((i, idx) => {
      tempPath = tempPath.replace(i, paths[idx]?.name ? `{${paths[idx].name}}` : '');
    });
    const pattern = new RegExp('{\\b(' + rexVar + '\\w)+\\b}', 'gi');
    pathname = tempPath.replace(pattern, '');
  } else {
    pathname = originalPath.replace(/{\S+}/g, '');
  }
  pathname = pathname.replace(/\/{2,}/g, '/').replace(/\/$/, '');
  return pathname;

  // query类型参数替换为name=value格式
  // const searchParams = querys?.reduce((prevValue, currentValue) => {
  //   const { name, value } = currentValue;
  //   if (name) {
  //     prevValue.append(name, value);
  //   }
  //   return prevValue;
  // }, new URLSearchParams());

  // return (pathname + '?' + searchParams?.toString()).replace(/\?$/, '');
};

// 校验 url 正确性
const isValidUrl = (url):boolean => {
  try {
    // eslint-disable-next-line no-new
    new URL(url);
    return true;
  } catch {
    return false;
  }
};

const getQueryStrFromParameter = (data) => {
  const queryParameters = data.filter(i => i[enabledKey] && (i.name || i[valueKey]));
  const queryParameterObj = {};
  queryParameters.forEach(item => {
    queryParameterObj[item.name || ''] = item[valueKey] || '';
  });
  return qs.stringify(queryParameterObj, { allowDots: true, encode: true });
};

const getAllFuncNames = async () => {
  const [error, res] = await http.get(`${TESTER}/mock/functions`);
  if (error) {
    return;
  }
  return (res.data || []).map(i => i.name);
};

// 函数、变量替换
const replaceFuncValue = async (param:{parameter?: {name: string, [valueKey]: string}[][], str?: string[], variableStr?: string[] }, allFuncNames: string[] = [], apiId?: string, targetType: ('API'|'API_CASE'|'SCENARIO') = 'API', options: { ignoreErr: boolean } = { ignoreErr: true }):Promise<any[][] | boolean> => {
  // eslint-disable-next-line prefer-regex-literals
  const funcReg = new RegExp('@\\w+\\w*\\([^)]*\\)');
  // eslint-disable-next-line prefer-regex-literals
  // const replaceReg = new RegExp(/\(.*\)/, 'g');
  // eslint-disable-next-line prefer-regex-literals
  // const variableReg = new RegExp(/\$\{([^}]+)\}/g);
  // eslint-disable-next-line prefer-regex-literals
  // const variableReg = new RegExp(/\{([a-zA-Z0-9!@$%^&*()_\-+=./]+)\}/);
  // eslint-disable-next-line prefer-regex-literals
  const variableRegReplace = new RegExp(/\{([a-zA-Z0-9!$%^&*_\-+=./]+)\}/g);
  const variableNames: string[] = []; // 记录所有变量名
  const funcStrs: string[] = []; // 记录有函数的字符串数组

  if (!allFuncNames.length) {
    allFuncNames = await getAllFuncNames();
  }

  const parameter = param.parameter || [];
  const paramLists = parameter.map(parameters => parameters.map(param => ({ ...param })));

  const allParams = Array.prototype.concat.apply([], paramLists);
  allParams.forEach((param) => {
    let isFunc = false;
    let isVariable = false;
    if (param[valueKey] && param.format !== 'binary') {
      if (typeof param[valueKey] === 'object' && param[valueKey] !== null) {
        param[valueKey] = JSON.stringify(param[valueKey]);
        param.stringed = true;
      }
      try {
        const matchVariable = param[valueKey].match(variableRegReplace);
        if (matchVariable) {
          isVariable = true;
          param.variable = isVariable;
          variableNames.push(...matchVariable.map(i => extractVar(i)));
        }
      } catch {}
      try {
        const matchRes = param[valueKey]?.match(funcReg) || [];
        if (matchRes?.length) {
          isFunc = true;
          param.func = isFunc;
        }
      } catch {}
    }
  });

  let str = param.str || [];
  str.forEach(i => {
    const matchVariable = i.match(variableRegReplace);
    if (matchVariable) {
      variableNames.push(...matchVariable.map(i => extractVar(i)));
    }
    const matchRes = i ? i.match(funcReg) : [];
    if (matchRes?.length) {
      funcStrs.push(i);
    }
  });
  if (allParams.some(i => i.func) || funcStrs.length) {
    const [error, resp] = await http.post(`${TESTER}/mock/text/data/batch`, [...allParams.filter(i => i.func).map(i => ({ text: i[valueKey], iterations: 1 })), ...funcStrs.map(i => ({ text: i, iterations: 1 }))]);
    if (!error) {
      const response = resp.data || [];
      paramLists.forEach(parameters => {
        parameters.forEach((param: any) => {
          if (param.func) {
            param[valueKey] = response[0].values[0];
            response.shift();
          }
          delete param.func;
        });
      });
      str = str.map((i): string => {
        if (i.match(funcReg)) {
          const result = response[0].values[0];
          response.shift();
          return result;
        }
        return i;
      });
    }
  }
  if (param.variableStr?.length) {
    variableNames.push(...param.variableStr);
  }

  let variableValues = [];
  if (variableNames.length && apiId) {
    // const [error, resp] = await variable.getVariableValue(uniq(variableNames), apiId);
    const [error, resp] = await http.get(`${TESTER}/target/${apiId}/${targetType}/parameter/data/value`);
    if (!error) {
      // const response = (resp.data || []).map(i => {
      //   return {
      //     ...i,
      //     targetType: i.targetType?.value,
      //     scope: i.scope?.value
      //   };
      // });
      variableValues = resp.data || {};
      paramLists.forEach(parameters => {
        parameters.forEach((param: any) => {
          if (param.variable) {
            // param[valueKey] = param[valueKey].replaceAll(variableRegReplace, (target) => {
            //   const result = response.find(item => item.name === extractVar(target));
            //   if (result) {
            //     return result.value;
            //   }
            //   return target;
            // });
            param[valueKey] = param[valueKey].replaceAll(variableRegReplace, (target) => {
              if (variableValues.hasOwnProperty(extractVar(target))) {
                return variableValues[extractVar(target)];
              }
              return target;
            });
          }
          delete param.variable;
        });
      });
      str = str.map(i => {
        if (i.match(variableRegReplace)) {
          const result = i.replaceAll(variableRegReplace, (target) => {
            // const result = response.find(item => item.name === extractVar(target));
            // if (result) {
            //   return result.value;
            // }
            // return target;
            if (variableValues[extractVar(target)]) {
              return variableValues[extractVar(target)];
            }
            return target;
          });
          return result;
        }
        return i;
      });
    } else {
      notification.error(error.message);
      if (!options.ignoreErr) {
        return false;
      }
    }
  }
  paramLists.forEach(parameters => {
    parameters.forEach(param => {
      if (param.stringed) {
        param[valueKey] = JSON.parse(param[valueKey]);
        delete param.stringed;
      }
    });
  });
  return [paramLists, str, Object.keys(variableValues).map(i => ({ name: i, value: variableValues[i] }))];
};

// 获取所有变量名称
export const getAllVariables = (param:{name: string, [valueKey]: string}[][], strs: string[] = []): string[] => {
  const result: string[] = [];
  // eslint-disable-next-line prefer-regex-literals
  const variableReg = new RegExp(/\$\{([^}]+)\}/g);
  const paramLists = param.map(parameters => parameters.map(param => ({ ...param })));
  const allParams = Array.prototype.concat.apply([], paramLists);
  allParams.forEach(param => {
    if (param[valueKey]) {
      const matches = param[valueKey].match(variableReg);
      if (matches) {
        result.push(...matches);
      }
    }
  });
  strs.forEach(str => {
    if (str) {
      const matches = str.match(variableReg);
      if (matches) {
        result.push(...(matches.map(i => extractVar(i))));
      }
    }
  });
  return result;
};
// 去掉变量的{}
const extractVar = (str: string): string => {
  const regex = /{([^}]+)}/;
  const match = str.match(regex);
  return match ? match[1] : str;
};

const getPathParamsFromApi = (parameters): {name: string, [valueKey]: string}[] => {
  // return parameters.filter(i => i[enabledKey] && i.in === 'path' && i.name).map(i => {
  //   if (i.schema.type === 'object') {
  //     return {
  //       ...i,
  //       [valueKey]: i[valueKey] ? Object.entries(i[valueKey]).map(([key, value]) => `${key}=${JSON.stringify(value)}`).join(',') : ''
  //     };
  //   }
  //   if (i.schema.type === 'array') {
  //     return {
  //       ...i,
  //       [valueKey]: i[valueKey] ? i[valueKey].map(JSON.stringify).join(',') : ''
  //     };
  //   }
  //   return i;
  // });
  const pathStr = getQueryStrFromParameter(parameters.filter(i => i.in === 'path'));
  return pathStr
    ? pathStr.split('&').map(keyValue => {
      const [key, value] = keyValue.split('=');
      return {
        name: key || '',
        [valueKey]: value
      };
    })
    : [];
};

const getHeaderParamsFromApi = (parameters): {name: string, [valueKey]: string}[] => {
  const headerStr = getQueryStrFromParameter(parameters.filter(i => i[enabledKey] && i.in === 'header' && i.name));
  return headerStr
    ? headerStr.split('&').map(keyValue => {
      const [key, value] = keyValue.split('=');
      return {
        name: key || '',
        [valueKey]: value
      };
    })
    : [];
};

const getQueryParamFromApi = (parameters): {name: string, [valueKey]: string}[] => {
  const queryStr = getQueryStrFromParameter(parameters.filter(i => i.in === 'query' && i[enabledKey] && (i.name || i[valueKey])));
  return queryStr
    ? queryStr.split('&').map(keyValue => {
      const [key, value] = keyValue.split('=');
      return {
        name: key || '',
        [valueKey]: value
      };
    })
    : [];
};

const getFormDataFromApi = (formData): {name: string, [valueKey]: string}[] => {
  const formStr = getQueryStrFromParameter(formData.filter(i => i.format !== 'binary'));
  return (formStr
    ? formStr.split('&').map((str) => {
      const [key, value] = str.split('=');
      return {
        name: key,
        [valueKey]: value || ''
      };
    })
    : []).concat(formData.filter(i => i.format === 'binary' && i[enabledKey]));
};

const getNameValue = {
  getPathParamsFromApi,
  getHeaderParamsFromApi,
  getQueryParamFromApi,
  getFormDataFromApi
};

export const validateMaximum = (val, max) => {
  if (val > max) {
    return `Value must be less than ${max}`;
  }
};

export const validateMinimum = (val, min) => {
  if (val < min) {
    return `Value must be greater than ${min}`;
  }
};

export const validateNumber = (val) => {
  if (!/^-?\d+(\.?\d+)?$/.test(val)) {
    return 'Value must be a number';
  }
};

export const validateInteger = (val) => {
  if (!/^-?\d+$/.test(val)) {
    return 'Value must be an integer';
  }
};

// export const validateFile = (val) => {
//   if (val && !(val instanceof win.File)) {
//     return 'Value must be a file';
//   }
// };

export const validateBoolean = (val) => {
  if (!(val === 'true' || val === 'false' || val === true || val === false)) {
    return 'Value must be a boolean';
  }
};

export const validateString = (val) => {
  if (val && typeof val !== 'string') {
    return 'Value must be a string';
  }
};

export const validateDateTime = (val) => {
  if (isNaN(Date.parse(val))) {
    return 'Value must be a DateTime';
  }
};

export const validateGuid = (val) => {
  val = val.toString().toLowerCase();
  if (!/^[{(]?[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}[)}]?$/.test(val)) {
    return 'Value must be a Guid';
  }
};

export const validateMaxLength = (val, max) => {
  if (val.length > max) {
    return `Value must be no longer than ${max} character${max !== 1 ? 's' : ''}`;
  }
};

// export const validateUniqueItems = (val, uniqueItems) => {
//   if (!val) {
//     return;
//   }
//   if (uniqueItems === 'true' || uniqueItems === true) {
//     const list = fromJS(val);
//     const set = list.toSet();
//     const hasDuplicates = val.length > set.size;
//     if (hasDuplicates) {
//       let errorsPerIndex = Set();
//       list.forEach((item, i) => {
//         if (list.filter(v => isFunc(v.equals) ? v.equals(item) : v === item).size > 1) {
//           errorsPerIndex = errorsPerIndex.add(i);
//         }
//       });
//       if (errorsPerIndex.size !== 0) {
//         return errorsPerIndex.map(i => ({ index: i, error: 'No duplicates allowed.' })).toArray();
//       }
//     }
//   }
// };

export const validateMinItems = (val, min) => {
  if ((!val && min >= 1) || (val && val.length < min)) {
    return `Array must contain at least ${min} item${min === 1 ? '' : 's'}`;
  }
};

export const validateMaxItems = (val, max) => {
  if (val && val.length > max) {
    return `Array must not contain more then ${max} item${max === 1 ? '' : 's'}`;
  }
};

export const validateMinLength = (val, min) => {
  if (val.length < min) {
    return `Value must be at least ${min} character${min !== 1 ? 's' : ''}`;
  }
};

export const validatePattern = (val, rxPattern) => {
  const patt = new RegExp(rxPattern);
  if (!patt.test(val)) {
    return 'Value must follow pattern ' + rxPattern;
  }
};

const getJsonFromSplitData = (data: {name: string, [valueKey]: string}[]) => {
  const str = data.map((item) => {
    return `${item.name}=${item[valueKey]}`;
  }).join('&');
  return qs.parse(str);
};

const validateType = (data, schema) => {
  delete schema.exampleSet;
  const validate = ajv.compile(schema);
  const valid = validate(data);
  if (!valid) {
    return validate.errors;
  }
  return [];
};

const deepDelAttrFromObj = (data = {}, keys: string[]) => {
  keys = uniq([...keys, valueKey, 'types', 'exampleSet', 'jsonSchema']);
  if (typeof data !== 'object') {
    return data;
  }

  const _data = JSON.parse(JSON.stringify(data));

  if (Object.prototype.toString.call(_data) === '[object Object]') {
    keys.forEach(key => delete _data[key]);
    Object.keys(_data).forEach(name => {
      if (name === 'items') {
        _data[name] = deepDelAttrFromObj(_data[name], keys);
      }
      if (name === 'properties') {
        Object.keys(_data[name]).forEach(key => {
          _data[name][key] = deepDelAttrFromObj(_data[name][key], keys);
        });
      }
    });
  }
  return _data;
};

const validateParameter = (data, schema) => {
  // if (schema.type === 'object') {
  //   const list = transJsonToList(data, -1, 1, [], schema.properties);
  //   const json = transListToJson(list, 'object', -1);
  //   const error = validateType(json, schema);
  //   return error;
  // }

  // if (schema.type === 'array') {
  //   const list = transJsonToList(data, -1, 1, [], schema.items);
  //   const json = transListToJson(list, 'array', -1);
  //   const error = validateType(json, schema);
  //   return error;
  // }
  const error = validateType(data, schema);
  return error;
};

const validateQueryParameter = (data) => {
  if (!data.length) {
    return true;
  }
  const errors: any[] = [];
  data.forEach(item => {
    const schemaObj = item;
    errors.push(...validateParameter(item[valueKey], deepDelAttrFromObj(schemaObj.schema, [])));
  });
  if (errors.length) {
    return false;
  }
  return true;
};

const validateBodyForm = (data) => {
  if (!data.length) {
    return true;
  }
  const errors: any[] = [];
  data.forEach(item => {
    const schemaObj = item;
    errors.push(...validateParameter(item[valueKey], deepDelAttrFromObj(schemaObj, [valueKey, 'types', 'exampleSet', 'name', enabledKey, 'key'])));
  });
  if (errors.length) {
    return false;
  }
  return true;
};

// 将x-xc-value 的 object 类型 stringify 为  String 类型
const travelXcValueToString = (data) => {
  if (Object.prototype.toString.call(data) === '[object Array]') {
    data.forEach(item => {
      if (typeof item === 'object') {
        item = travelXcValueToString(item);
      }
    });
  } else {
    Object.keys(data).forEach(key => {
      if (key === valueKey && typeof data[key] === 'object') {
        data[key] = JSON.stringify(data[key]);
      } else if (typeof data[key] === 'object') {
        data[key] = travelXcValueToString(data[key]);
      }
    });
  }
  return data;
};

// 将x-xc-value 的 空object  为  String 类型
const travelEmptyObjToString = (data) => {
  if (typeof data === 'object') {
    if (JSON.stringify(data) === '{}') {
      data = '';
    }
    if (Object.prototype.toString.call(data) === '[object Object]') {
      Object.keys(data).forEach(key => {
        data[key] = travelEmptyObjToString(data[key]);
      });
    } else if (Object.prototype.toString.call(data) === '[object Array]') {
      data.forEach((i, idx) => {
        data[idx] = travelEmptyObjToString(i);
      });
    }
  }
  return data;
};

export const itemTypes = [
  'string',
  'array',
  'boolean',
  'integer',
  'object',
  'number'
].map(i => ({ value: i, label: i }));

export const inOptions = ['path', 'query'].map(i => ({ value: i, label: i }));

export const baseTypes = [
  'string',
  'boolean',
  'integer',
  'number'
].map(i => ({ value: i, label: i }));

export const parameterIn = [
  'query',
  'path'
].map(i => ({ value: i, label: i }));

const getDataTypeFromFormat = (format: string) => {
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

const fileToBuffer = (file) => {
  const fr = new FileReader();
  // const fileName = file.name;
  fr.readAsArrayBuffer(file);
  return new Promise((resolve) => {
    fr.addEventListener('loadend', (e) => {
      const buf = e.target?.result;
      resolve(buf);
    });
  });
};

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
const variableNameReg = new RegExp(/^[a-zA-Z0-9!@$%^&*()_\-+=./]+$/);

// 解密
const decode = (value: string, native = false): { name: string; value: string; } | string => {
  if (native) {
    return dt(value);
  }

  const slices = dt(value).split(':');
  return {
    name: slices[0],
    value: slices[1]
  };
};

// 加密
const encode = (name = '', value = ''): string => {
  if (!value) {
    return et(name);
  }

  if (!name && !value) {
    return '';
  }

  return et(name + ':' + value);
};

export type AssertFormItem = {
  assertionCondition: AssertionCondition|undefined;
  condition: string|undefined;
  description: string|undefined;
  enabled: boolean;
  expected: string|undefined;
  extraction: Extraction;
  headerName: string|undefined;
  name: string|undefined;
  type: AssertionType|undefined;
  result?: {
      failure: boolean;
      message: string;
  };
}

export interface ResponseState {
  config?: Record<string, any>,
  headers?: Record<string, any>,
  data?: any,
  status?: number,
  size?: number,
  assertions?: AssertFormItem[],
  performance?: PerformanceEntry,
  cookie?: string[],
  requestHeaders?: ParamsItem[],
  errorMessage?: string
}

const refModelsObj = {};
const getModelDataByRef = async (serviceId, ref) => {
  if (refModelsObj[serviceId]?.[ref]) {
    return refModelsObj[serviceId]?.[ref];
  } else {
    const fetcher = http.get(`${TESTER}/services/${serviceId}/comp/ref`, { ref });
    refModelsObj[serviceId] = {
      [ref]: fetcher
    };
    fetcher.then(resp => {
      refModelsObj[serviceId][ref] = resp;
    });
    setTimeout(() => {
      delete refModelsObj[serviceId][ref];
    }, 2000);
    return refModelsObj[serviceId]?.[ref];
  }
};

const dataURLtoBlob = (dataurl, type?: string) => {
  try {
    const arr = dataurl.split(',');
    const mime = type || arr[0].match(/:(.*?);/)?.[1];
    const bstr = atob(arr[1] || arr[0]);
    let n = bstr.length;
    const u8arr = new Uint8Array(n);
    while (n--) {
      u8arr[n] = bstr.charCodeAt(n);
    }
    return new Blob([u8arr], {
      type: mime
    });
  } catch {
    return undefined;
  }
};

interface Server {
  url: string;
  variables?: Record<string, any>
}
const variableReg = /\{[a-zA-Z0-9_]+\}/g;
const getServerData = (dataSource: Server): string => {
  const url = dataSource.url;
  const replaced = url.replace(variableReg, match => {
    const key = match.replace('{', '').replace('}', '');
    return dataSource.variables?.[key]?.default || '';
  });
  return replaced;
};

const formatBytes = (size = 0, decimal = 2):string => {
  if (size === 0) return '0 B';
  const c = 1024;
  const e = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const f = Math.floor(Math.log(size) / Math.log(c));
  return parseFloat((size / Math.pow(c, f)).toFixed(decimal)) + ' ' + e[f];
};

const getStatusColor = (status) => {
  const statusStr = status.toString();
  if (statusStr.startsWith('4') || statusStr.startsWith('5')) {
    return 'text-status-error';
  }
  return 'text-status-success';
};

// 解析openapi parameter
const analysisParameters = (data: {name: string; schema?: Record<string, any>, $ref?: string}, modelResolves: Record<string, string>) => {
  // function analysisObject (obj:{$ref?: string} = {}) {
  //   if (obj.$ref) {
  //     if (modelResolves[obj.$ref]) {
  //       const model = JSON.parse(modelResolves[obj.$ref]);
  //       if (model) {
  //         delete obj.$ref;
  //         obj = {
  //           ...obj,
  //           ...model
  //         };
  //       }
  //     }
  //   } else {
  //     Object.keys(obj).forEach(key => {
  //       if (Object.prototype.toString.call(obj[key]) === '[object Object]') {
  //         obj[key] = analysisObject(obj[key]);
  //       }
  //     });
  //   }

  //   return obj;
  // }

  function analysisObject (obj: { $ref?: string } = {}) {
    const stack: { $ref?: string }[] = [obj]; // 初始化栈，包含待处理的对象

    while (stack.length > 0) {
      const current = stack.pop(); // 从栈中取出当前对象

      // 处理 $ref 属性
      if (current && current.$ref) {
        if (modelResolves[current.$ref]) {
          const model = JSON.parse(modelResolves[current.$ref]);
          if (model) {
            delete current.$ref;
            Object.assign(current, model); // 合并对象
          }
        }
      }

      // 遍历当前对象的属性
      if (current) {
        Object.keys(current).forEach(key => {
          if (Object.prototype.toString.call(current[key]) === '[object Object]') {
            stack.push(current[key]); // 将子对象推入栈中
          }
        });
      }
    }

    return obj;
  }

  if (data.$ref) {
    if (modelResolves[data.$ref]) {
      const modalObj = JSON.parse(modelResolves[data.$ref]);
      if (modalObj) {
        delete data.$ref;
        data = {
          ...data,
          ...modalObj
        };
        if (!data[valueKey]) {
          data[valueKey] = SwaggerUI.extension.sampleFromSchemaGeneric(data.schema, { useValue: true });
        }
      }
    }
  }

  if (data.schema) {
    data.schema = analysisObject(data.schema);
    if (typeof data[valueKey] === 'string' && ['array', 'object'].includes(data.schema.type)) {
      try {
        data[valueKey] = JSON.parse(data[valueKey]);
      } catch {}
    }
  }

  return data;
};

const analysisBody = (bodyObj: Record<string, any>, modelResolves: Record<string, any>) => {
  function analysisObject (obj = {}) {
    const stack = [obj]; // 初始化栈，包含待处理的对象

    while (stack.length > 0) {
      const current = stack.pop(); // 从栈中取出当前对象

      // 处理 $ref 属性
      if (current.$ref) {
        if (modelResolves[current.$ref]) {
          const model = JSON.parse(modelResolves[current.$ref]);
          if (model) {
            delete current.$ref;
            Object.assign(current, model); // 合并对象
          }
        }
      }

      // 遍历当前对象的属性
      Object.keys(current).forEach(key => {
        if (Object.prototype.toString.call(current[key]) === '[object Object]') {
          stack.push(current[key]); // 将子对象推入栈中
        }

        // 检查并解析值
        if (key === valueKey &&
                typeof current[key] === 'string' &&
                ['array', 'object'].includes(current.schema?.type) &&
                ['array', 'object'].includes(current?.type)) {
          try {
            current[key] = JSON.parse(current[key]);
          } catch {
            // 处理 JSON 解析错误
          }
        }
      });
    }

    return obj;
  }

  if (bodyObj?.$ref) {
    if (modelResolves[bodyObj.$ref]) {
      const modalObj = JSON.parse(modelResolves[bodyObj.$ref]);
      if (modalObj) {
        delete bodyObj.$ref;
        bodyObj = {
          ...bodyObj,
          ...modalObj
        };
      }
    }
  }

  if (bodyObj?.content) {
    Object.keys(bodyObj.content).forEach(type => {
      bodyObj.content[type] = analysisObject(bodyObj.content[type]);
      if (!bodyObj.content[type][valueKey]) {
        bodyObj.content[type][valueKey] = SwaggerUI.extension.sampleFromSchemaGeneric(bodyObj.content[type].schema, { useValue: true });
      }
    });
  }

  return bodyObj;
};

const splitDuration = (duration: string): string[] => {
  if (duration.includes('h')) {
    duration = duration.replace('h', '');
    return [duration, 'h'];
  } else if (duration.includes('ms')) {
    duration = duration.replace('ms', '');
    return [duration, 'ms'];
  } else if (duration.includes('min')) {
    duration = duration.replace('min', '');
    return [duration, 'min'];
  } else if (duration.includes('s')) {
    duration = duration.replace('s', '');
    return [duration, 's'];
  } else if (duration.includes('d')) {
    duration = duration.replace('d', '');
    return [duration, 'd'];
  }
  return [];
};

const qsJsonToParamList = (data: Record<string, any>|Array<any>) => {
  if (typeof data !== 'object') {
    return [];
  }
  const keyValueStr = qs.stringify(data, { allowDots: true, encode: true });
  if (keyValueStr) {
    const keyValueStrs = keyValueStr.split('&');
    return keyValueStrs.map(str => {
      const [key, value] = str.split('=');
      return { name: key, value, type: 'string' };
    });
  }
  return [];
};

const getDataType = (data) => {
  const type = typeof data;
  if (type !== 'object') {
    return type;
  }
  if (Object.prototype.toString.call(data) === '[object Array]') {
    return 'array';
  }
  if (Object.prototype.toString.call(data) === '[object Object]') {
    return 'object';
  }
};

/**
 *transform JSON to list<key, value>[]
 *
 */
const transJsonToList = (data: any [] | Record<string, any>, pid = -1, level = 1, defaultData: any[] = [], schema = {}, topSchema = {}): any[] => {
  const transArr = (data, pid = -1, level = 1, schema) => {
    data.forEach(i => {
      let copySchema = JSON.parse(JSON.stringify((schema || {})));
      if (['object', 'array'].includes(copySchema.type) || getDataType(i) === 'object' || getDataType(i) === 'array') {
        const id = utils.uuid('api');
        const idLine = [...(result.find(item => item.id === pid)?.idLine || []), id];
        result.push({
          type: getDataType(i),
          ...copySchema,
          name: 'item',
          id,
          pid,
          idLine,
          level: level,
          [valueKey]: undefined,
          checked: true
        });
        level += 1;
        pid = id;
        copySchema = copySchema.items || {};
        transJsonToList(i, pid, level, result, copySchema.items || {});
      } else {
        const id = utils.uuid('api');
        const idLine = [...(result.find(item => item.id === pid)?.idLine || []), id];
        result.push({
          type: getDataType(i),
          ...copySchema,
          name: 'item',
          id,
          pid,
          idLine,
          level: level,
          [valueKey]: i,
          checked: true
        });
      }
    });
  };
  const transObjct = (data, pid = -1, level = 1, schema) => {
    Object.keys(data).forEach(key => {
      let value = data[key];
      let type = schema[key]?.type || getDataType(value);
      if (!schema[key]?.type && schema[key]?.format) {
        type = getDataTypeFromFormat(schema[key].format);
      }
      const id = utils.uuid('api');
      const hasChild = type === 'array' || type === 'object';
      const idLine = [...(result.find(item => item.id === pid)?.idLine || []), id];
      result.push({
        type: type,
        ...schema[key],
        name: key,
        [valueKey]: value,
        pid: pid,
        level,
        idLine,
        id,
        checked: true
      });
      if (hasChild) {
        if (type === 'array') {
          if (typeof value === 'string') {
            try {
              value = JSON.parse(value);
            } catch {
              console.log(value + 'id not a json');
            }
          }
          transArr(value, id, level + 1, schema.properties?.[key] || {});
        }
        if (type === 'object') {
          if (typeof value === 'string') {
            try {
              value = JSON.parse(value);
            } catch {
              console.log(value + 'id not a json');
            }
          }
          transObjct(value, id, level + 1, schema.properties?.[key] || {});
        }
      }
    });
  };

  const transNormal = (data, pid = -1, level = 1, _schema) => {
    const id = utils.uuid('api');
    const pItem = result.find(item => item.id === pid) || { type: topSchema?.type };
    const idLine = [...(pItem?.idLine || []), id];
    let type = _schema.type || getDataType(data);
    if (!_schema.type && _schema.format) {
      type = getDataTypeFromFormat(_schema.format);
    }
    return result.push({
      type,
      name: pItem?.type === 'array' ? 'item' : '',
      ..._schema,
      [valueKey]: data,
      id,
      pid,
      level,
      idLine,
      checked: true
    });
  };
  const result:any[] = defaultData;
  const type = schema.type || getDataType(data);
  if (type !== 'object' && type !== 'array') {
    transNormal(data, pid, level, schema || {});
  }
  if (typeof data === 'string') {
    try {
      data = JSON.parse(data);
    } catch {
      console.log(data + 'id not a json');
    }
  }
  if (type === 'array') {
    transArr(data, pid, level, schema.items || {});
  }
  if (type === 'object') {
    transObjct(data, pid, level, schema || {});
  }
  return result;
};

// 是否符合 assci 编码范围
function containsAllAscii (str) {
  return /^[\000-\177]*$/.test(str);
}

const gzipFileToBase64 = async (file: File) => {
  return await codeUtils.gzip(file);
};

const fileToBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const base64ct = (e.target.result as string || '').split('base64,')[1];
      resolve(base64ct);
    };
    reader.onerror = () => {
      reject(new Error());
    };
    reader.readAsDataURL(file);
  });
};

const maxDuration = (a, b) => { // example: 1min
  function computedSec (value, unit) {
    if (unit === 'h') {
      return +value * 60 * 60;
    }
    if (unit === 'min') {
      return +value * 60;
    }
    if (unit === 's') {
      return +value;
    }
  }
  let [aValue, aUnit] = splitDuration(a);
  if (!aValue) {
    aValue = '0';
  }
  if (!aUnit) {
    aUnit = 's';
  }

  let [bValue, bUnit] = splitDuration(b);
  if (!bValue) {
    bValue = '0';
  }
  if (!bUnit) {
    bUnit = 's';
  }

  const aSec = computedSec(aValue, aUnit);
  const bSec = computedSec(bValue, bUnit);
  if (aSec >= bSec) {
    return a;
  }
  return b;
};

const formatMillisecondToShortDuraiton = (value: number, format: 'h'|'min'|'s'|'day') => {
  if (!value) {
    return '0';
  }
  if (!format) {
    return value + 'ms';
  }
  if (format === 's') {
    return dayjs.duration(value).asSeconds().toFixed(2) + format;
  } else if (format === 'min') {
    return dayjs.duration(value).asMinutes().toFixed(2) + format;
  } else if (format === 'h') {
    return dayjs.duration(value).asHours().toFixed(2) + format;
  } else if (format === 'day') {
    return dayjs.duration(value).asDays().toFixed(2) + format;
  }
};

export {
  toUrl,
  getNewItem,
  getUriByParams,
  getParamsByUri,
  getNameValue,
  isValidUrl,
  validateQueryParameter,
  validateBodyForm,
  travelXcValueToString,
  travelEmptyObjToString,
  replaceFuncValue,
  dataURLtoBlob,
  getModelDataByRef,
  decode,
  encode,
  variableNameReg,
  CONTENT_TYPE,
  fileToBuffer,
  getDataTypeFromFormat,
  deepDelAttrFromObj,
  validateType,
  getJsonFromSplitData,
  API_EXTENSION_KEY,
  getBodyDefaultItem,
  getDefaultParams,
  getServerData,
  formatBytes,
  getStatusColor,
  deconstruct,
  analysisParameters,
  analysisBody,
  splitDuration,
  qsJsonToParamList,
  transJsonToList,
  containsAllAscii,
  gzipFileToBase64,
  fileToBase64,
  maxDuration,
  formatMillisecondToShortDuraiton,
  getExecShowAuthData
};

export default {
  toUrl,
  getNewItem,
  getUriByParams,
  getParamsByUri,
  getNameValue,
  isValidUrl,
  validateQueryParameter,
  validateBodyForm,
  travelXcValueToString,
  travelEmptyObjToString,
  replaceFuncValue,
  dataURLtoBlob,
  getModelDataByRef,
  decode,
  encode,
  variableNameReg,
  CONTENT_TYPE,
  fileToBuffer,
  getDataTypeFromFormat,
  deepDelAttrFromObj,
  validateType,
  getJsonFromSplitData,
  API_EXTENSION_KEY,
  getBodyDefaultItem,
  getDefaultParams,
  getServerData,
  formatBytes,
  getStatusColor,
  deconstruct,
  analysisParameters,
  analysisBody,
  splitDuration,
  qsJsonToParamList,
  transJsonToList,
  containsAllAscii,
  gzipFileToBase64,
  fileToBase64,
  maxDuration,
  formatMillisecondToShortDuraiton,
  getExecShowAuthData
};
