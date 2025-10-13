import { uniq } from 'lodash-es';
import qs from 'qs';
import { Authentication, OASServer, RequestBody, RequestConfig } from './types';
import { variable } from '@/api/tester';

import angusUtils from '@/utils/apis/index';

export const password = [
  'tokenUrl',
  'refreshUrl',
  'clientId',
  'clientSecret',
  'username',
  'password',
  'scopes'
];

export const clientCredentials = [
  'tokenUrl',
  'refreshUrl',
  'clientId',
  'clientSecret',
  'scopes'
];

export const flowAuthKeys = {
  password,
  clientCredentials
};

const { analysisParameters, analysisBody, API_EXTENSION_KEY } = angusUtils;
const { valueKey, securityApiKeyPrefix, oAuth2Key, newTokenKey, basicAuthKey, oAuth2Token } = API_EXTENSION_KEY;
const getServerUrl = (data: RequestConfig['server']): string => {
  const { url, variables } = data;
  const variableReg = /\{[a-zA-Z0-9_]+\}/g;
  const replaced = url?.replace(variableReg, match => {
    const key = match.replace('{', '').replace('}', '');
    return variables?.[key]?.defaultValue || '';
  });
  return replaced;
};

const extractVar = (str: string): string => {
  const regex = /{([^}]+)}/;
  const match = str.match(regex);
  return match ? match[1] : str;
};

const replaceApiVariable = async (
  id: string,
  requestBody: RequestBody,
  parameters: RequestConfig['parameters'],
  authentication: Authentication
) => {
  const variableNames = [];
  const variableRegReplace = /\{([a-zA-Z0-9!@$%^&*()_\-+=./]+)\}/g;

  let matchVariable = JSON.stringify(parameters).match(variableRegReplace);
  variableNames.push(...(matchVariable || []).map(item => extractVar(item)));

  matchVariable = JSON.stringify(requestBody || {}).match(variableRegReplace);
  variableNames.push(...(matchVariable || []).map(item => extractVar(item)));

  matchVariable = JSON.stringify(authentication || {}).match(variableRegReplace);
  variableNames.push(...(matchVariable || []).map(item => extractVar(item)));

  if (!variableNames.length) {
    return;
  }

  const [error, { data: _data = [] }] = await variable.getVariableValue({
    names: uniq(variableNames),
    targetId: id,
    targetType: 'API'
  });
  if (error || !_data) {
    return;
  }

  const response = _data.map(item => {
    return {
      ...item,
      targetType: item.targetType?.value,
      scope: item.scope?.value
    };
  });
  parameters = JSON.parse(JSON.stringify(parameters).replace(variableRegReplace, target => {
    const result = response.find(item => item.name === extractVar(target));
    if (result) {
      return result.value;
    }
    return target;
  }));

  requestBody = JSON.parse(JSON.stringify(requestBody).replace(variableRegReplace, target => {
    const result = response.find(item => item.name === extractVar(target));
    if (result) {
      return result.value;
    }
    return target;
  }));

  authentication = JSON.parse(JSON.stringify(authentication).replace(variableRegReplace, target => {
    const result = response.find(item => item.name === extractVar(target));
    if (result) {
      return result.value;
    }
    return target;
  }));
};

const openapiRequestBodyToBody = (data: {
  [key: string]: {
    schema: {
      format: 'binary';
      type: string;
      'x-xc-contentEncoding': string;
      'x-xc-fileName': string;
      'x-xc-value': string;
      properties: {
        [key: string]: {
          format:'binary';
          type: string;
          'x-xc-enabled': boolean;
          'x-xc-value': string;
        }
      }
    };
    'x-xc-value': string;
  }
}, contentType: string) => {
  if (!data) {
    return undefined;
  }

  let itemContent = data[contentType];
  if (contentType === 'application/x-www-form-urlencoded' || contentType === 'multipart/form-data') {
    const forms = [];
    const properties = itemContent?.schema?.properties;
    if (properties) {
      for (const key in properties) {
        const { 'x-xc-value': value, 'x-xc-enabled': enabled, type, format } = properties[key];
        const tempData = {
          name: key,
          value,
          enabled: enabled !== false,
          format,
          type,
          fileName: null,
          contentType: null,
          contentEncoding: null
        };

        if (typeof value === 'object') {
          tempData.value = JSON.stringify(value);
        }

        if (contentType === 'application/x-www-form-urlencoded') {
          if (!['string', 'integer', 'boolean', 'number'].includes(type)) {
            tempData.type = 'string';
          }
        } else {
          if (format === 'binary') {
            if (!['string', 'array'].includes(type)) {
              tempData.type = 'string';
            }

            if (value) {
              let valueJson = {};
              if (typeof value === 'string') {
                valueJson = JSON.parse(value);
              } else if (typeof value === 'object') {
                valueJson = value;
              }

              if (Array.isArray(valueJson)) {
                const items = valueJson.map(item => {
                  return {
                    fileName: item['x-xc-fileName'],
                    contentType: item['x-xc-contentType'],
                    contentEncoding: item['x-xc-contentEncoding'],
                    value: item['x-xc-value']
                  };
                });
                tempData.value = JSON.stringify(items);
              } else {
                tempData.fileName = valueJson['x-xc-fileName'];
                tempData.contentType = valueJson['x-xc-contentType'];
                tempData.contentEncoding = valueJson['x-xc-contentEncoding'];
                tempData.value = valueJson['x-xc-value'];
              }
            } else {
              tempData.value = '';
            }
          } else if (!['string', 'integer', 'boolean', 'number'].includes(type)) {
            tempData.type = 'string';
          }
        }

        forms.push(tempData);
      }
    }
    return {
      forms
    };
  }

  let isBinary = false;
  let mimeType: string;
  if (itemContent && itemContent.schema?.format === 'binary') {
    isBinary = true;
  } else if (contentType === 'application/octet-stream') {
    isBinary = true;
    if (!itemContent) {
      for (const key in data) {
        if (data[key]?.schema?.format === 'binary') {
          itemContent = data[key];
          mimeType = key;
        }
      }
    }
  }

  if (isBinary) {
    const { 'x-xc-contentEncoding': contentEncoding, 'x-xc-fileName': fileName, 'x-xc-value': value, type, format } = itemContent.schema || {};
    return {
      fileName,
      contentEncoding,
      type,
      format,
      rawContent: value,
      contentType: mimeType
    };
  }

  let rawContent = itemContent?.['x-xc-value'] || '';
  if (rawContent) {
    if (typeof rawContent === 'object') {
      rawContent = JSON.stringify(rawContent, null, 2);
    } else if (typeof rawContent !== 'string') {
      rawContent = rawContent + '';
    }
  }

  return {
    rawContent,
    type: 'string'
  };
};

const formApiAuthToScenarioAuth = (authentication) => {
  if (authentication?.type) {
    if (authentication.type === 'http') {
      if (authentication.scheme === 'basic') {
        const value = angusUtils.encode(authentication[basicAuthKey]?.name || '', authentication[basicAuthKey]?.value || '');
        return {
          type: 'http',
          value: value
        };
      }
      if (authentication.scheme === 'bearer') {
        return {
          type: 'http',
          value: authentication[valueKey] || 'Bearer '
        };
      }
    }
    if (authentication.type === 'apiKey') {
      const first = { name: authentication.name, in: authentication.in || 'header', value: authentication[valueKey] };
      const others = (authentication[securityApiKeyPrefix] || []).map(i => {
        return {
          name: i.name,
          in: i.in,
          value: i[valueKey]
        };
      });
      return {
        type: 'apiKey',
        apiKeys: [first, ...others]
      };
    }
    if (authentication.type === 'oauth2') {
      const flowData:{[key: string]: {[key: string]: string|string[]}} = {};
      if (authentication[oAuth2Key] === 'clientCredentials') {
        flowData.clientCredentials = {};
        flowData.clientCredentials.clientIn = authentication.flows.clientCredentials?.['x-xc-oauth2-clientAuthType'] || 'REQUEST_BODY';
        if (authentication.flows) {
          flowAuthKeys.clientCredentials.forEach(i => {
            if (i === 'scopes') {
              const scopesObj = authentication.flows.clientCredentials?.[i] || {};
              flowData.clientCredentials[i] = Object.keys(scopesObj);
            } else {
              flowData.clientCredentials[i] = authentication.flows.clientCredentials?.[i] || authentication.flows.clientCredentials?.[`x-xc-oauth2-${i}`] || '';
            }
          });
        }
      }
      if (authentication[oAuth2Key] === 'password') {
        flowData.password = {};
        flowData.password.clientIn = authentication.flows.password?.['x-xc-oauth2-clientAuthType'] || 'REQUEST_BODY';
        if (authentication.flows) {
          flowAuthKeys.password.forEach(i => {
            if (i === 'scopes') {
              const scopesObj = authentication.flows.password?.[i] || {};
              flowData.password[i] = Object.keys(scopesObj);
            } else {
              flowData.password[i] = authentication.flows.password?.[i] || authentication.flows.password?.[`x-xc-${i}`] || '';
            }
          });
        }
      }
      return {
        type: 'oauth2',
        value: authentication[oAuth2Token],
        oauth2: {
          newToken: authentication[newTokenKey],
          ...flowData
        }
      };
    }
  }
  return authentication;
};

/**
 * @param data 接口详情
 * @returns 变量里的request配置
 */
export const requestConfigs = async (data) => {
  const {
    id,
    projectId,
    uri,
    endpoint,
    availableServers,
    method,
    parameters: _parameters = [],
    requestBody: _requestBody,
    resolvedRefModels = {},
    authentication: _authentication
  } = data;

  let url = '';
  let server: RequestConfig['server'] = { url: '', variables: {} };
  let availableServer: OASServer;
  const _servers:OASServer[] = availableServers;
  if (_servers?.length) {
    availableServer = _servers.find(item => item['x-xc-serverSource'] === 'CURRENT_REQUEST') || _servers[0];
  }
  server = { ...availableServer, variables: {} };
  if (availableServer?.variables) {
    const variables = availableServer.variables;
    for (const key in variables) {
      server.variables[key] = {
        // default: variables[key].default,
        // enum: variables[key].enum
        defaultValue: variables[key].default,
        allowableValues: variables[key].enum
      };
    }
  }
  url = getServerUrl(server) + (uri || '');

  const requestBody = analysisBody(_requestBody, resolvedRefModels);
  let authentication = _authentication;
  if (authentication?.$ref) {
    const [error, { data: _data }] = await angusUtils.getModelDataByRef(projectId, authentication.$ref);
    if (!error) {
      authentication = JSON.parse(_data.model);
    }
  }

  const parameters: RequestConfig['parameters'] = [];
  for (let i = 0, len = _parameters.length; i < len; i++) {
    const item = _parameters[i];
    const _parameter: RequestConfig['parameters'][number] = analysisParameters(item, resolvedRefModels || {});
    const { in: _in, name } = _parameter;
    const _value = _parameter['x-xc-value'];
    const _enabled = item['x-xc-enabled'] !== false;
    if (_in === 'query') {
      const itemJSon = {
        [name]: _parameter['x-xc-value'] || ''
      };
      const itemStrs = qs.stringify(itemJSon, { allowDots: true, encode: false, encodeValuesOnly: true }).split('&');
      itemStrs.every(str => {
        const [key, value] = str.split('=');
        parameters.push({ name: key, value, in: _in, enabled: _enabled, type: 'string' });
        return true;
      });

      continue;
    }

    if (['path', 'header'].includes(_in)) {
      if (typeof _value === 'object') {
        if (Object.prototype.toString.call(_value) === '[object Object]') {
          const value = Object.keys(_parameter['x-xc-value']).map(key => {
            return `${key}=${JSON.stringify(_value[key])}`;
          }).join(',');
          parameters.push({ name, value, in: _in, enabled: _enabled, type: 'string' });
          continue;
        }

        if (Object.prototype.toString.call(_value) === '[object Array]') {
          const value = _value.map(item => {
            return JSON.stringify(item);
          }).join(',');
          parameters.push({ name, value, in: _in, enabled: _enabled, type: 'string' });
          continue;
        }

        continue;
      }

      parameters.push({ name, value: _value, in: _in, enabled: _enabled, type: 'string' });
      continue;
    }

    if (_in === 'cookie') {
      if (typeof _value === 'object') {
        if (Object.prototype.toString.call(_value) === '[object Object]') {
          const value = Object.keys(_value).map(key => {
            return `${key},${JSON.stringify(_value[key])}`;
          }).join(',');
          parameters.push({ name, value, in: _in, enabled: _enabled, type: 'string' });
          continue;
        }

        if (Object.prototype.toString.call(_value) === '[object Array]') {
          const value = _value.map(item => {
            return JSON.stringify(item);
          }).join(',');
          parameters.push({ name, value, in: _in, enabled: _enabled, type: 'string' });
          continue;
        }

        continue;
      }

      parameters.push({ name, value: _value, in: _in, enabled: _enabled, type: 'string' });
      continue;
    }
  }

  await replaceApiVariable(id, requestBody, parameters, authentication);

  authentication = formApiAuthToScenarioAuth(authentication);

  // 找到Content-Type
  const contentType = parameters?.find(item => item.in === 'header' && item.name === 'Content-Type')?.value;

  // 把openapi规范的requestBody转为body
  const body = openapiRequestBodyToBody(requestBody?.content, contentType);

  return {
    endpoint,
    method: method.value,
    server,
    url,
    parameters,
    authentication,
    body
  };
};
