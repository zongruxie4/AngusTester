import qs from 'qs';
import Ajv from 'ajv';
import addFormats from 'ajv-formats';
import { uniq } from 'lodash-es';
import { decode as dt, encode as et } from 'js-base64';
import { http, utils, TESTER, codeUtils, AssertionCondition, AssertionType } from '@xcan-angus/infra';
import SwaggerUI from '@xcan-angus/swagger-ui';
import { notification } from '@xcan-angus/vue-ui';

import { Extraction } from '@/components/ApiAssert/utils/extract/PropsType';
import { getExecShowAuthData } from '@/components/ExecAuthencation/interface';
import dayjs from 'dayjs';
import duration from 'dayjs/plugin/duration';
import { services } from '@/api/tester';
import { API_EXTENSION_KEYS } from '@/types/openapi-types';

dayjs.extend(duration);

/**
 * Supported content types for API requests
 */
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

/**
 * CSS class names for different API status states
 */
const API_STATUS_COLOR_CONFIG = {
  IN_DESIGN: 'text-gray-text-light',
  IN_DEV: 'text-execute-yellow',
  DEV_COMPLETED: 'text-blue-1',
  RELEASED: 'text-status-success',
  UNKNOWN: 'text-text-content'
};

/**
 * RGB color values for API status badges
 */
const API_STATUS_BADGE_COLOR_CONFIG = {
  IN_DESIGN: 'rgba(140, 140, 140, 1)',
  IN_DEV: 'rgba(255, 129, 0, 1)',
  DEV_COMPLETED: 'rgba(0,119,255,1)',
  RELEASED: 'rgba(82, 196, 26, 1)',
  UNKNOWN: 'rgba(82,90,101,1)'
};

/**
 * API extension keys for custom properties.
 */
const API_EXTENSION_KEY = API_EXTENSION_KEYS;

// eslint-disable-next-line prefer-regex-literals
const variableNameReg = new RegExp(/^[a-zA-Z0-9!@$%^&*()_\-+=./]+$/);

/**
 * Parameter item interface for API parameters.
 */
export interface ParamsItem {
  name?: string,
  in?: string,
  [key: string]: any,
  description?: string,
  variabled?: boolean,
  enabled?: boolean,
  allowableValues?: string[] | null, // Enum values
  valueType?: string | null,
  key?: symbol
}

/**
 * Create default parameter item with standard configuration.
 * @param config - Additional configuration to merge
 * @returns Default parameter item
 */
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

/**
 * Create default body item with standard configuration.
 * @param config - Additional configuration to merge
 * @returns Default body item
 */
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

/**
 * Deconstruct schema by resolving $ref references.
 * @param data - Data containing model and resolved reference models
 * @returns Deconstructed schema with resolved references
 */
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

/**
 * Convert URL string to URL object or return original if invalid.
 * @param url - URL string to convert
 * @returns URL object or original string if invalid
 */
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

/**
 * Get new item from metadata or create default body item.
 * @param metaData - Array of parameter items
 * @param config - Optional configuration for default item
 * @returns New parameter item or undefined
 */
const getNewItem = (metaData: ParamsItem[], config?: ParamsItem): ParamsItem | undefined => {
  const emptyItem = metaData?.find((item) => !item.name);
  if (!emptyItem) {
    return getBodyDefaultItem(config);
  }

  return undefined;
};

/**
 * Extract parameters from URI string.
 * <p>
 * Extracts both path parameters (wrapped in {}) and query parameters
 * from the URI string.
 * </p>
 * @param uri - URI string to parse
 * @returns Array of parameter items
 */
const getParamsByUri = (uri = ''): ParamsItem[] => {
  // Extract path type parameters
  const parameters: ParamsItem[] = uri.match(/{[^{}]+}/gi)?.map((item): ParamsItem => {
    return getDefaultParams({ name: item.replace(/{(\S*)}/gi, '$1'), in: 'path' });
  }) || [];

  if (/\?/.test(uri)) {
    // Extract query type parameters
    new URLSearchParams(uri.replace(/\S+\?(\S*)/g, '$1')).forEach((value, key) => {
      parameters.push(getDefaultParams({ name: key, value: value as string, in: 'query' }));
    });
  }
  return parameters;
};

/**
 * Build URI from parameters by replacing path placeholders.
 * <p>
 * Replaces path parameters with their values and handles
 * parameter validation and formatting.
 * </p>
 * @param uri - Original URI string
 * @param paths - Array of path parameters
 * @returns Processed URI string
 */
const getUriByParams = (uri: string, paths: ParamsItem[]): string => {
  if (!uri) {
    return '';
  }

  const pathUri = uri;

  // Regular expression variables
  let regexVariable = '';
  // Replace path type parameters with {name} placeholders
  const originalPath = pathUri.replace(/(\S+)\?\S*/, '$1');
  let pathname: string;
  // eslint-disable-next-line prefer-regex-literals
  const pathReg = new RegExp(/\{([a-zA-Z0-9!@$%^&*()_\-+=./]+)\}/g);
  const preUrl = decodeURIComponent(originalPath);
  const uriPath = preUrl.match(pathReg); // Get all {} parts from URI
  if (paths?.length) {
    let tempPath = paths.reduce((prevValue, currentValue) => {
      regexVariable += '(?!' + currentValue.name + ')';
      return prevValue;
    }, decodeURIComponent(originalPath));
    if (paths?.length > (uriPath?.length || 0)) {
      tempPath += `/{${paths?.[paths?.length - 1].name}}`;
    }
    uriPath?.forEach((item, idx) => {
      tempPath = tempPath.replace(item, paths[idx]?.name ? `{${paths[idx].name}}` : '');
    });
    const pattern = new RegExp('{\\b(' + regexVariable + '\\w)+\\b}', 'gi');
    pathname = tempPath.replace(pattern, '');
  } else {
    pathname = originalPath.replace(/{\S+}/g, '');
  }
  pathname = pathname.replace(/\/{2,}/g, '/').replace(/\/$/, '');
  return pathname;
};

/**
 * Validate URL correctness.
 * @param url - URL string to validate
 * @returns True if URL is valid, false otherwise
 */
const isValidUrl = (url: string): boolean => {
  try {
    // eslint-disable-next-line no-new
    new URL(url);
    return true;
  } catch {
    return false;
  }
};

/**
 * Convert parameter array to query string.
 * @param data - Array of parameter objects
 * @returns Query string representation
 */
const getQueryStrFromParameter = (data: ParamsItem[]) => {
  const queryParameters = data.filter(item => item[enabledKey] && (item.name || item[valueKey]));
  const queryParameterObj: Record<string, string> = {};
  queryParameters.forEach(item => {
    queryParameterObj[item.name || ''] = item[valueKey] || '';
  });
  return qs.stringify(queryParameterObj, { allowDots: true, encode: true });
};

/**
 * Get all available function names from the server.
 * @returns Array of function names or undefined if error
 */
const getAllFuncNames = async (): Promise<string[] | undefined> => {
  const [error, res] = await http.get(`${TESTER}/mock/functions`);
  if (error) {
    return;
  }
  return (res.data || []).map((item: { name: string }) => item.name);
};

/**
 * Replace function calls and variables in parameters and strings.
 * <p>
 * Processes function calls (starting with @) and variable references (wrapped in {})
 * by calling the appropriate APIs and replacing them with actual values.
 * </p>
 * @param param - Object containing parameters, strings, and variable strings
 * @param allFuncNames - Array of available function names
 * @param apiId - API identifier for variable resolution
 * @param targetType - Target type for parameter resolution
 * @param options - Options including error handling
 * @returns Processed parameters and strings or false if error
 */
const replaceFuncValue = async (param: {parameter?: {name: string, [valueKey]: string}[][], str?: string[], variableStr?: string[] }, allFuncNames: string[] = [], apiId?: string, targetType: ('API'|'API_CASE'|'SCENARIO') = 'API', options: { ignoreErr: boolean } = { ignoreErr: true }): Promise<any[][] | boolean> => {
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
  const variableNames: string[] = []; // Record all variable names
  const funcStrs: string[] = []; // Record strings with functions

  if (!allFuncNames.length) {
    allFuncNames = await getAllFuncNames() || [];
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
      variableValues = resp.data || {};
      paramLists.forEach(parameters => {
        parameters.forEach((param: any) => {
          if (param.variable) {
            param[valueKey] = param[valueKey].replace(variableRegReplace, (target) => {
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
          const result = i.replace(variableRegReplace, (target) => {
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

/**
 * Get all variable names from parameters and strings.
 * @param param - Array of parameter arrays
 * @param strs - Array of strings to check
 * @returns Array of variable names
 */
export const getAllVariables = (param: {name: string, [valueKey]: string}[][], strs: string[] = []): string[] => {
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

/**
 * Extract variable name from variable reference string.
 * @param str - Variable reference string like "{variableName}"
 * @returns Extracted variable name or original string if no match
 */
const extractVar = (str: string): string => {
  const regex = /{([^}]+)}/;
  const match = str.match(regex);
  return match ? match[1] : str;
};

/**
 * Extract path parameters from API parameters.
 * @param parameters - Array of API parameters
 * @returns Array of path parameters with name and value
 */
const getPathParamsFromApi = (parameters: ParamsItem[]): {name: string, [valueKey]: string}[] => {
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

/**
 * Extract header parameters from API parameters.
 * @param parameters - Array of API parameters
 * @returns Array of header parameters with name and value
 */
const getHeaderParamsFromApi = (parameters: ParamsItem[]): {name: string, [valueKey]: string}[] => {
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

/**
 * Extract query parameters from API parameters.
 * @param parameters - Array of API parameters
 * @returns Array of query parameters with name and value
 */
const getQueryParamFromApi = (parameters: ParamsItem[]): {name: string, [valueKey]: string}[] => {
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

/**
 * Extract form data parameters from API parameters.
 * @param formData - Array of form data parameters
 * @returns Array of form data parameters with name and value
 */
const getFormDataFromApi = (formData: ParamsItem[]): {name: string, [valueKey]: string}[] => {
  const formStr = getQueryStrFromParameter(formData.filter(i => i.format !== 'binary'));
  const formParams = formStr
    ? formStr.split('&').map((str) => {
      const [key, value] = str.split('=');
      return {
        name: key,
        [valueKey]: value || ''
      };
    })
    : [];

  const binaryParams = formData
    .filter(i => i.format === 'binary' && i[enabledKey])
    .map(i => ({
      name: i.name || '',
      [valueKey]: i[valueKey] || ''
    }));

  return [...formParams, ...binaryParams];
};

const getNameValue = {
  getPathParamsFromApi,
  getHeaderParamsFromApi,
  getQueryParamFromApi,
  getFormDataFromApi
};

/**
 * Convert split data array to JSON object.
 * @param data - Array of objects with name and value
 * @returns Parsed JSON object
 */
const getJsonFromSplitData = (data: {name: string, [valueKey]: string}[]) => {
  const str = data.map((item) => {
    return `${item.name}=${item[valueKey]}`;
  }).join('&');
  return qs.parse(str);
};

/**
 * Validate data against JSON schema.
 * @param data - Data to validate
 * @param schema - JSON schema to validate against
 * @returns Array of validation errors or empty array if valid
 */
const validateType = (data: any, schema: any): any[] => {
  delete schema.exampleSet;
  const validate = ajv.compile(schema);
  const valid = validate(data);
  if (!valid) {
    return validate.errors || [];
  }
  return [];
};

/**
 * Deep delete attributes from object recursively.
 * @param data - Object to process
 * @param keys - Array of keys to delete
 * @returns Processed object with specified keys removed
 */
const deepDelAttrFromObj = (data: any = {}, keys: string[]): any => {
  const keysToDelete = uniq([...keys, valueKey, 'types', 'exampleSetFlag', 'jsonSchema']);
  if (typeof data !== 'object') {
    return data;
  }

  const clonedData = JSON.parse(JSON.stringify(data));

  if (Object.prototype.toString.call(clonedData) === '[object Object]') {
    keysToDelete.forEach(key => delete clonedData[key]);
    Object.keys(clonedData).forEach(name => {
      if (name === 'items') {
        clonedData[name] = deepDelAttrFromObj(clonedData[name], keysToDelete);
      }
      if (name === 'properties') {
        Object.keys(clonedData[name]).forEach(key => {
          clonedData[name][key] = deepDelAttrFromObj(clonedData[name][key], keysToDelete);
        });
      }
    });
  }
  return clonedData;
};

/**
 * Validate parameter data against schema.
 * @param data - Data to validate
 * @param schema - Schema to validate against
 * @returns Array of validation errors
 */
const validateParameter = (data: any, schema: any): any[] => {
  const error = validateType(data, schema);
  return error;
};

/**
 * Validate query parameters against their schemas.
 * @param data - Array of query parameter items
 * @returns True if all parameters are valid, false otherwise
 */
const validateQueryParameter = (data: ParamsItem[]): boolean => {
  if (!data.length) {
    return true;
  }
  const errors: any[] = [];
  data.forEach(item => {
    const schemaObj = item;
    const validationErrors = validateParameter(item[valueKey], deepDelAttrFromObj(schemaObj.schema, []));
    if (validationErrors) {
      errors.push(...validationErrors);
    }
  });
  if (errors.length) {
    return false;
  }
  return true;
};

/**
 * Validate body form data against their schemas.
 * @param data - Array of body form items
 * @returns True if all form data is valid, false otherwise
 */
const validateBodyForm = (data: ParamsItem[]): boolean => {
  if (!data.length) {
    return true;
  }
  const errors: any[] = [];
  data.forEach(item => {
    const schemaObj = item;
    const validationErrors = validateParameter(item[valueKey], deepDelAttrFromObj(schemaObj, [valueKey, 'types', 'exampleSet', 'name', enabledKey, 'key']));
    if (validationErrors) {
      errors.push(...validationErrors);
    }
  });
  if (errors.length) {
    return false;
  }
  return true;
};

/**
 * Convert object values to strings recursively for x-xc-value fields.
 * @param data - Data object to process
 * @returns Processed data with object values converted to strings
 */
const travelXcValueToString = (data: any): any => {
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

/**
 * Convert empty objects to empty strings recursively.
 * @param data - Data object to process
 * @returns Processed data with empty objects converted to empty strings
 */
const travelEmptyObjToString = (data: any): any => {
  if (typeof data === 'object') {
    if (JSON.stringify(data) === '{}') {
      data = '';
    }
    if (Object.prototype.toString.call(data) === '[object Object]') {
      Object.keys(data).forEach(key => {
        data[key] = travelEmptyObjToString(data[key]);
      });
    } else if (Object.prototype.toString.call(data) === '[object Array]') {
      data.forEach((item, idx) => {
        data[idx] = travelEmptyObjToString(item);
      });
    }
  }
  return data;
};

const travelDelSchemaRef = (schame) => {
  if (typeof schame === 'object') {
    if (schame.$ref) {
      delete schame.$ref;
    }
    if (Object.prototype.toString.call(schame) === '[object Object]') {
      Object.keys(schame).forEach(key => {
        if (key !== valueKey) {
          schame[key] = travelDelSchemaRef(schame[key]);
        }
      });
    }
    if (Object.prototype.toString.call(schame) === '[object Array]') {
      schame.forEach(item => {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        item = travelDelSchemaRef(item);
      });
    }
  }
  return schame;
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

/**
 * Get data type from format string.
 * @param format - Format string
 * @returns Data type string
 */
const getDataTypeFromFormat = (format: string): string => {
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

/**
 * Convert file to ArrayBuffer.
 * @param file - File object to convert
 * @returns Promise that resolves to ArrayBuffer
 */
const fileToBuffer = (file: File): Promise<ArrayBuffer | null> => {
  const fileReader = new FileReader();
  fileReader.readAsArrayBuffer(file);
  return new Promise((resolve) => {
    fileReader.addEventListener('loadend', (e) => {
      const buffer = e.target?.result as ArrayBuffer | null;
      resolve(buffer);
    });
  });
};

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
    // }
  }).then((res) => {
    try {
      return JSON.parse(res as string);
    } catch (error) {
      return res;
    }
  });
};

/**
 * Decode base64 encoded value.
 * @param value - Base64 encoded string
 * @param native - Whether to return native decoded string
 * @returns Decoded object with name and value or raw string if native
 */
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

/**
 * Encode name and value to base64 string.
 * @param name - Name to encode
 * @param value - Value to encode
 * @returns Base64 encoded string
 */
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
/**
 * Get model data by reference from service.
 * @param serviceId - Service identifier
 * @param ref - Model reference
 * @returns Promise that resolves to model data
 */
const getModelDataByRef = async (serviceId: string, ref: string) => {
  if (refModelsObj[serviceId]?.[ref]) {
    return refModelsObj[serviceId]?.[ref];
  } else {
    const fetcher = services.getComponentRef(serviceId, ref);
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

/**
 * Convert data URL to Blob object.
 * @param dataurl - Data URL string
 * @param type - Optional MIME type
 * @returns Blob object or undefined if conversion fails
 */
const dataURLtoBlob = (dataurl: string, type?: string): Blob | undefined => {
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
/**
 * Get server data with variables replaced.
 * @param dataSource - Server configuration with URL and variables
 * @returns URL with variables replaced
 */
const getServerData = (dataSource: Server): string => {
  const url = dataSource.url;
  const replaced = url.replace(variableReg, match => {
    const key = match.replace('{', '').replace('}', '');
    return dataSource.variables?.[key]?.default || '';
  });
  return replaced;
};

/**
 * Format bytes to human-readable string.
 * @param size - Size in bytes
 * @param decimal - Number of decimal places
 * @returns Formatted string with unit
 */
const formatBytes = (size = 0, decimal = 2): string => {
  if (size === 0) return '0 B';
  const base = 1024;
  const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const magnitudeIndex = Math.floor(Math.log(size) / Math.log(base));
  return parseFloat((size / Math.pow(base, magnitudeIndex)).toFixed(decimal)) + ' ' + units[magnitudeIndex];
};

/**
 * Get CSS class for HTTP status code color.
 * @param status - HTTP status code
 * @returns CSS class name for status color
 */
const getStatusColor = (status: number | string): string => {
  const statusStr = status.toString();
  if (statusStr.startsWith('4') || statusStr.startsWith('5')) {
    return 'text-status-error';
  }
  return 'text-status-success';
};

/**
 * Analyze OpenAPI parameters by resolving references.
 * @param data - Parameter data with schema and $ref
 * @param modelResolves - Model resolution mapping
 * @returns Analyzed parameter data
 */
const analysisParameters = (data: {name: string; schema?: Record<string, any>, $ref?: string}, modelResolves: Record<string, string>) => {
  function analysisObject (obj: { $ref?: string } = {}) {
    const stack: { $ref?: string }[] = [obj]; // Initialize stack with objects to process

    while (stack.length > 0) {
      const current = stack.pop(); // Get current object from stack

      // Process $ref property
      if (current && current.$ref) {
        if (modelResolves[current.$ref]) {
          const model = JSON.parse(modelResolves[current.$ref]);
          if (model) {
            delete current.$ref;
            Object.assign(current, model); // Merge objects
          }
        }
      }

      // Traverse current object properties
      if (current) {
        Object.keys(current).forEach(key => {
          if (Object.prototype.toString.call(current[key]) === '[object Object]') {
            stack.push(current[key] as { $ref?: string }); // Push child objects to stack
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
    if (typeof data[valueKey] === 'string' && data.schema && ['array', 'object'].includes(data.schema.type)) {
      try {
        data[valueKey] = JSON.parse(data[valueKey]);
      } catch {}
    }
  }

  return data;
};

/**
 * Analyze OpenAPI request body by resolving references.
 * @param bodyObj - Body object with content and $ref
 * @param modelResolves - Model resolution mapping
 * @returns Analyzed body object
 */
const analysisBody = (bodyObj: Record<string, any>, modelResolves: Record<string, any>) => {
  function analysisObject (obj: any = {}) {
    const stack: any[] = [obj]; // Initialize stack with objects to process

    while (stack.length > 0) {
      const current = stack.pop(); // Get current object from stack

      // Process $ref property
      if (current && current.$ref) {
        if (modelResolves[current.$ref]) {
          const model = JSON.parse(modelResolves[current.$ref]);
          if (model) {
            delete current.$ref;
            Object.assign(current, model); // Merge objects
          }
        }
      }

      // Traverse current object properties
      if (current) {
        Object.keys(current).forEach(key => {
          if (Object.prototype.toString.call(current[key]) === '[object Object]') {
            stack.push(current[key]); // Push child objects to stack
          }

          // Check and parse values
          if (key === valueKey &&
                typeof current[key] === 'string' &&
                  current.schema && ['array', 'object'].includes(current.schema.type) &&
                  current.type && ['array', 'object'].includes(current.type)) {
            try {
              current[key] = JSON.parse(current[key]);
            } catch {
              // Handle JSON parsing errors
            }
          }
        });
      }
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
      if (!bodyObj.content[type][valueKey] && bodyObj.content[type].schema) {
        bodyObj.content[type][valueKey] = SwaggerUI.extension.sampleFromSchemaGeneric(bodyObj.content[type].schema, { useValue: true });
      }
    });
  }

  return bodyObj;
};

/**
 * Split duration string into value and unit.
 * @param duration - Duration string like "10s", "5min", "2h"
 * @returns Array with [value, unit] or empty array if no match
 */
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

/**
 * Convert JSON object to parameter list.
 * @param data - JSON object or array to convert
 * @returns Array of parameter objects with name, value, and type
 */
const qsJsonToParamList = (data: Record<string, any> | Array<any>) => {
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

/**
 * Get data type of a value.
 * @param data - Value to check
 * @returns Type string ('string', 'number', 'boolean', 'array', 'object', etc.)
 */
const getDataType = (data: any): string => {
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
  return type;
};

/**
 * Transform JSON to list of key-value pairs.
 * @param data - JSON data to transform
 * @param pid - Parent ID
 * @param level - Current level
 * @param defaultData - Default data array
 * @param schema - Schema object
 * @param topSchema - Top-level schema
 * @returns Array of transformed key-value pairs
 */
const transJsonToList = (data: any [] | Record<string, any>, pid: string | number = -1, level = 1, defaultData: any[] = [], schema = {}, topSchema = {}): any[] => {
  const transArr = (data: any[], pid: string | number = -1, level = 1, schema: any) => {
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
        transJsonToList(i as any[], pid, level, result, copySchema.items || {});
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
  const transObject = (data: Record<string, any>, pid: string | number = -1, level = 1, schema: any) => {
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
          transArr(value as any[], id, level + 1, schema.properties?.[key] || {});
        }
        if (type === 'object') {
          if (typeof value === 'string') {
            try {
              value = JSON.parse(value);
            } catch {
              console.log(value + 'id not a json');
            }
          }
          transObject(value as Record<string, any>, id, level + 1, schema.properties?.[key] || {});
        }
      }
    });
  };

  const transNormal = (data: any, pid: string | number = -1, level = 1, _schema: any) => {
    const id = utils.uuid('api');
    const pItem = result.find(item => item.id === pid) || { type: (topSchema as any)?.type };
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
  const type = (schema as any).type || getDataType(data);
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
    transArr(data as any[], pid, level, (schema as any).items || {});
  }
  if (type === 'object') {
    transObject(data as Record<string, any>, pid, level, schema || {});
  }
  return result;
};

/**
 * Check if string contains only ASCII characters.
 * @param str - String to check
 * @returns True if string contains only ASCII characters
 */
function containsAllAscii (str: string): boolean {
  // eslint-disable-next-line no-control-regex
  return /^[\x00-\x7f]*$/.test(str);
}

/**
 * Compress file to gzip and convert to base64.
 * @param file - File to compress
 * @returns Promise that resolves to gzipped base64 string
 */
const gzipFileToBase64 = async (file: File): Promise<string> => {
  return await codeUtils.gzip(file);
};

/**
 * Convert file to base64 string.
 * @param file - File object to convert
 * @returns Promise that resolves to base64 string
 */
const fileToBase64 = (file: File): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const base64ct = (e.target?.result as string || '').split('base64,')[1];
      resolve(base64ct);
    };
    reader.onerror = () => {
      reject(new Error());
    };
    reader.readAsDataURL(file);
  });
};

/**
 * Get the maximum duration between two duration strings.
 * @param a - First duration string
 * @param b - Second duration string
 * @returns The longer duration string
 */
const maxDuration = (a: string, b: string): string => {
  function computedSec (value: string, unit: string): number {
    if (unit === 'h') {
      return +value * 60 * 60;
    }
    if (unit === 'min') {
      return +value * 60;
    }
    if (unit === 's') {
      return +value;
    }
    return 0;
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

/**
 * Format milliseconds to short duration string.
 * @param value - Duration in milliseconds
 * @param format - Target format unit
 * @returns Formatted duration string
 */
const formatMillisecondToShortDuration = (value: number, format: 'h' | 'min' | 's' | 'day'): string => {
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
  return value + 'ms';
};

export {
  CONTENT_TYPE,
  API_STATUS_COLOR_CONFIG,
  API_STATUS_BADGE_COLOR_CONFIG,
  API_EXTENSION_KEY,
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
  travelDelSchemaRef,
  replaceFuncValue,
  dataURLtoBlob,
  getModelDataByRef,
  decode,
  encode,
  variableNameReg,
  fileToBuffer,
  convertBlob,
  getDataTypeFromFormat,
  deepDelAttrFromObj,
  validateType,
  getJsonFromSplitData,
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
  formatMillisecondToShortDuration,
  getExecShowAuthData
};

export default {
  CONTENT_TYPE,
  API_STATUS_COLOR_CONFIG,
  API_STATUS_BADGE_COLOR_CONFIG,
  API_EXTENSION_KEY,
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
  travelDelSchemaRef,
  replaceFuncValue,
  dataURLtoBlob,
  getModelDataByRef,
  decode,
  encode,
  variableNameReg,
  fileToBuffer,
  convertBlob,
  getDataTypeFromFormat,
  deepDelAttrFromObj,
  validateType,
  getJsonFromSplitData,
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
  formatMillisecondToShortDuration,
  getExecShowAuthData
};
