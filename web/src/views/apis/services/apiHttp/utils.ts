import qs from 'qs';
import Ajv from 'ajv';
import addFormats from 'ajv-formats';
import { getBodyDefaultItem, getDefaultParams, ParamsItem } from './interface';
import { mock } from '@/api/tester';
import { API_EXTENSION_KEY } from '@/views/apis/utils';
import { uniq } from 'lodash-es';
import { paramTarget } from '@/api/tester';

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

const getParamsByUri = (endpoint = ''): ParamsItem[] => {
  // 获取path类型参数
  const temp: ParamsItem[] = endpoint.match(/{[^{}]+}/gi)?.map((item): ParamsItem => {
    return getDefaultParams({ name: item.replace(/{(\S*)}/gi, '$1'), in: 'path' });
  }) || [];

  if (/\?/.test(endpoint)) {
    // 获取query类型参数
    new URLSearchParams(endpoint.replace(/\S+\?(\S*)/g, '$1')).forEach((value, key) => {
      temp.push(getDefaultParams({ name: key, value: value as string, in: 'query' }));
    });
  }
  return temp;
};

const getUriByParams = (endpoint: string, paths: ParamsItem[]): string => {
  if (!endpoint) {
    return '';
  }

  // const pathUri = endpoint.split('?')[0] || '/';
  const pathUri = endpoint;

  // 正则变量
  let rexVar = '';
  // path类型参数替换为格式为：{name}的占位符
  const originalPath = pathUri.replace(/(\S+)\?\S*/, '$1');
  let pathname: string;
  // eslint-disable-next-line prefer-regex-literals
  const pathReg = new RegExp(/\{([a-zA-Z0-9!@$%^&*()_\-+=./]+)\}/g);
  const preUrl = decodeURIComponent(originalPath);
  const uriPath = preUrl.match(pathReg); // 拿到 endpoint 上所有 {} 部分
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
  return qs.stringify(queryParameterObj);
};

const getAllFuncNames = async () => {
  const [error, res] = await mock.getAllFunction();
  if (error) {
    return;
  }
  return (res.data || []).map(i => i.name);
};

// 函数、变量替换
const replaceFuncValue = async (param:{parameter?: {name: string, [valueKey]: string}[][], str?: string[], variableStr?: string[] }, allFuncNames: string[] = [], apiId?: string):Promise<any[][]> => {
  // eslint-disable-next-line prefer-regex-literals
  const funcReg = new RegExp('@\\w+\\w*\\([^)]*\\)');
  // eslint-disable-next-line prefer-regex-literals
  // const replaceReg = new RegExp(/\(.*\)/, 'g');
  // eslint-disable-next-line prefer-regex-literals
  // const variableReg = new RegExp(/\$\{([^}]+)\}/g);
  // eslint-disable-next-line prefer-regex-literals
  // const variableReg = new RegExp(/\{([a-zA-Z0-9!@$%^&*()_\-+=./]+)\}/);
  // eslint-disable-next-line prefer-regex-literals
  const variableRegReplace = new RegExp(/\{([a-zA-Z0-9!@$%^&*()_\-+=./]+)\}/g);
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
    const [error, resp] = await mock.generateFunctionValue([...allParams.filter(i => i.func).map(i => ({ text: i[valueKey], iterations: 1 })), ...funcStrs.map(i => ({ text: i, iterations: 1 }))]);
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
  if (variableNames.length) {
    // const [error, resp] = await variable.getVariableValue(uniq(variableNames), apiId);
    const [error, resp] = await paramTarget.getParamsVariableValue(apiId);
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
            //   const samplingSummary = response.find(item => item.name === extractVar(target));
            //   if (samplingSummary) {
            //     return samplingSummary.value;
            //   }
            //   return target;
            // });
            param[valueKey] = param[valueKey].replaceAll(variableRegReplace, (target) => {
              if (variableValues[extractVar(target)]) {
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
            // const samplingSummary = response.find(item => item.name === extractVar(target));
            // if (samplingSummary) {
            //   return samplingSummary.value;
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
// export const getAllVariables = (param:{name: string, [valueKey]: string}[][], strs: string[] = []): string[] => {
//   const samplingSummary: string[] = [];
//   // eslint-disable-next-line prefer-regex-literals
//   const variableReg = new RegExp(/\$\{([^}]+)\}/g);
//   const paramLists = param.map(parameters => parameters.map(param => ({ ...param })));
//   const allParams = Array.prototype.concat.apply([], paramLists);
//   allParams.forEach(param => {
//     if (param[valueKey]) {
//       const matches = param[valueKey].match(variableReg);
//       if (matches) {
//         samplingSummary.push(...matches);
//       }
//     }
//   });
//   strs.forEach(str => {
//     if (str) {
//       const matches = str.match(variableReg);
//       if (matches) {
//         samplingSummary.push(...(matches.map(i => extractVar(i))));
//       }
//     }
//   });
//   return samplingSummary;
// };
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
  // return parameters.filter(i => i[enabledKey] && i.in === 'header' && i.name).map(i => {
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

export const getJsonFromSplitData = (data: {name: string, [valueKey]: string}[]) => {
  const str = data.map((item) => {
    return `${item.name}=${item[valueKey]}`;
  }).join('&');
  return qs.parse(str);
};

export const validateType = (data, schema) => {
  delete schema.exampleSetFlag;
  const validate = ajv.compile(schema);
  const valid = validate(data);
  if (!valid) {
    return validate.errors;
  }
  return [];
  // const errors: string[] = [];
  // if (!data && !schema.required) {
  //   return errors;
  // }
  // if (schema.type === 'string') {
  //   const err = validateString(data);
  //   if (err) {
  //     errors.push(err);
  //   }
  //   if (schema.format) {
  //     const err1 = validateFormat(data, schema.format);
  //     if (err1) {
  //       errors.push(err1);
  //     }
  //   }
  // }
  // if (schema.type === 'boolean') {
  //   const err = validateBoolean(data);
  //   if (err) {
  //     errors.push(err);
  //   }
  // }
  // if (schema.type === 'number') {
  //   const err = validateNumber(data);
  //   if (err) {
  //     errors.push(err);
  //   }
  // }
  // if (schema.type === 'integer') {
  //   const err = validateInteger(data);
  //   if (err) {
  //     errors.push(err);
  //   }
  // }
  // if (schema.type === 'object') {
  //   const err:string[] = [];
  //   Object.keys(data).forEach(name => {
  //     err.push(...validateType(data[name], schema.properties[name]));
  //   });
  //   errors.push(...err);
  // }
  // if (schema.type === 'array') {
  //   const err:string[] = [];
  //   data.forEach(item => {
  //     err.push(...validateType(item, schema.items));
  //   });
  //   errors.push(...err);
  // }
  // return errors;
};

export const deepDelAttrFromObj = (data = {}, keys: string[]) => {
  keys = uniq([...keys, valueKey, 'types', 'exampleSetFlag', 'jsonSchema']);
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
    errors.push(...validateParameter(item[valueKey], deepDelAttrFromObj(schemaObj, [valueKey, 'types', 'exampleSetFlag', 'name', enabledKey, 'key'])));
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
  } else if (Object.prototype.toString.call(data) === '[object Object]') {
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

// @TODO 将响应结果blob转为对应的数据类型
const convertBlob = (blob: Blob) => {
  return new Promise((resolve) => {
    const mimeType = blob.type;
    const reader = new FileReader();

    reader.onload = () => {
      resolve(reader.result);
    };

    reader.onerror = () => {
      resolve(blob);
    };

    if (/^(text)\/{1}.+/gi.test(mimeType) || /^(application\/json).*/gi.test(mimeType)) {
      reader.readAsText(blob, 'utf-8');
    } else {
      resolve(blob);
    }

    // if (/^(text)\/{1}.+/gi.test(mimeType) || /^(application\/json).*/gi.test(mimeType)) {
    //   reader.readAsText(blob, 'utf-8');
    // } else if (/^(images|audio|video)\/{1}.+/gi.test(mimeType)) {
    //   reader.readAsDataURL(blob);
    // } else {
    //   // 如果是不支持的类型，直接返回原始数据
    //   resolve(blob);
    // }
  }).then((res) => {
    try {
      return JSON.parse(res as string);
    } catch (error) {
      return res;
    }
  });
};

export { toUrl, getNewItem, getUriByParams, getParamsByUri, replaceFuncValue, getNameValue, isValidUrl, validateQueryParameter, validateBodyForm, travelXcValueToString, travelEmptyObjToString, convertBlob };
