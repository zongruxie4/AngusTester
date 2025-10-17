<script setup lang="ts">
import { defineAsyncComponent, nextTick, ref, onMounted, watch, inject, computed } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, AsyncComponent } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import qs from 'qs';
import { paramTarget } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import angusUtils from 'src/utils/apis';
import { PipelineConfig, TargetKey } from './PropsType';
import { HTTPConfig } from './HTTPConfigs/PropsType';
import { WaitingTimeConfig } from '@/plugins/test/components/UIConfigComp/WaitingTime/PropsType';
import { RendezvousConfig } from '@/plugins/test/components/UIConfigComp/Rendezvous/PropsType';
import { ThroughputConfig } from './Throughput/PropsType';
import { TransEndConfig } from '@/plugins/test/components/UIConfigComp/TransEnd/PropsType';
import { TransStartConfig } from '@/plugins/test/components/UIConfigComp/TransStart/PropsType';
import { ApiInfo } from '@/plugins/test/types';

export interface Props {
  value: PipelineConfig[];
  loaded: boolean;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  loaded: false
});

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

const password = [
  'tokenUrl',
  'refreshUrl',
  'clientId',
  'clientSecret',
  'username',
  'password',
  'scopes'
];

const clientCredentials = [
  'tokenUrl',
  'refreshUrl',
  'clientId',
  'clientSecret',
  'scopes'
];

const flowAuthKeys = {
  password,
  clientCredentials
};

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'errorNumChange', value: number): void;
  (e: 'renderChange', value: boolean): void;
  (e: 'variableChange', value): void;
}>();

const { API_EXTENSION_KEY, analysisParameters, analysisBody } = angusUtils;
const { serverSourceKey, valueKey, enabledKey, securityApiKeyPrefix, oAuth2Key, newTokenKey, basicAuthKey, oAuth2Token } = API_EXTENSION_KEY;

const Draggable = defineAsyncComponent(() => import('./Draggable/index.vue'));
const SelectApiModal = defineAsyncComponent(() => import('./SelectApiModal/index.vue'));
const SelectUseCaseModal = defineAsyncComponent(() => import('./SelectUseCaseModal/index.vue'));

const dragRef = ref();
const domId = utils.uuid();
const isTransEnd = ref(false);
const apiModalVisible = ref(false);
const useCaseModalVisible = ref(false);

const linkApiIds = ref(new Set<string>());
const linkCaseIds = ref(new Set<string>());

const scrollToBottom = () => {
  const dom = document.getElementById(domId);
  if (!dom) {
    return;
  }

  nextTick(() => {
    setTimeout(() => {
      dom.scrollTop = dom.scrollHeight;
    }, 16.66);
  });
};

const renderChange = (value: boolean) => {
  emit('renderChange', value);
};

const insertHTTP = () => {
  const data: HTTPConfig = {
    id: utils.uuid(),
    target: 'HTTP',
    name: '',
    description: '',
    enabled: true,
    beforeName: '',
    transactionName: '',
    assertions: [],
    variables: [],
    request: {
      endpoint: '',
      method: 'GET',
      server: undefined,
      url: '',
      parameters: [],
      authentication: undefined,
      body: undefined
    },
    apisId: undefined,
    caseId: undefined
  };
  if (typeof dragRef.value?.add === 'function') {
    dragRef.value.add(data);
  }

  scrollToBottom();
};

const selectHTTP = () => {
  apiModalVisible.value = true;
};

const extractVar = (str: string): string => {
  const regex = /{([^}]+)}/;
  const match = str.match(regex);
  return match ? match[1] : str;
};

const replaceApiVariable = async (
  id: string,
  requestBody: ApiInfo['requestBody'],
  parameters: HTTPConfig['request']['parameters'],
  authentication: ApiInfo['authentication'],
  apisId: string
) => {
  const variableNames = [];
  const variableRegReplace = /\{([a-zA-Z0-9!@$%^&*()_\-+=./]+)\}/g;

  let matchVariable = JSON.stringify(parameters || []).match(variableRegReplace);
  variableNames.push(...(matchVariable || []).map(item => extractVar(item)));

  matchVariable = JSON.stringify(requestBody || {}).match(variableRegReplace);
  variableNames.push(...(matchVariable || []).map(item => extractVar(item)));

  matchVariable = JSON.stringify(authentication || {}).match(variableRegReplace);
  variableNames.push(...(matchVariable || []).map(item => extractVar(item)));

  if (!variableNames.length) {
    return;
  }

  // const [error, { data: _data = [] }] = await http.get(`${TESTER}/variable/value/all`, {
  //   names: uniq(variableNames),
  //   targetId: id,
  //   targetType: 'API'
  // });
  const [_error, { data: _data = {} }] = await paramTarget.getParamsVariableValue(apisId);
  const variableValues = _data || {};

  parameters = JSON.parse(JSON.stringify(parameters || []).replace(variableRegReplace, target => {
    if (variableValues.hasOwnProperty(extractVar(target))) {
      return variableValues[extractVar(target)];
    }
    return target;
  }));

  requestBody = JSON.parse(JSON.stringify(requestBody || {}).replace(variableRegReplace, target => {
    if (variableValues.hasOwnProperty(extractVar(target))) {
      return variableValues[extractVar(target)];
    }
    return target;
  }));

  authentication = JSON.parse(JSON.stringify(authentication || {}).replace(variableRegReplace, target => {
    if (variableValues.hasOwnProperty(extractVar(target))) {
      return variableValues[extractVar(target)];
    }
    return target;
  }));

  // const response = _data.map(item => {
  //   return {
  //     ...item,
  //     targetType: item.targetType?.value || item.targetType,
  //     scope: item.scope?.value || item.scope
  //   };
  // });
  // parameters = JSON.parse(JSON.stringify(parameters || []).replace(variableRegReplace, target => {
  //   const result = response.find(item => item.name === extractVar(target));
  //   if (result) {
  //     return result.value;
  //   }
  //   return target;
  // }));

  // requestBody = JSON.parse(JSON.stringify(requestBody || {}).replace(variableRegReplace, target => {
  //   const result = response.find(item => item.name === extractVar(target));
  //   if (result) {
  //     return result.value;
  //   }
  //   return target;
  // }));

  // authentication = JSON.parse(JSON.stringify(authentication || {}).replace(variableRegReplace, target => {
  //   const result = response.find(item => item.name === extractVar(target));
  //   if (result) {
  //     return result.value;
  //   }
  //   return target;
  // }));
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
          format: 'binary';
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

const transformOpenApiTo = async (data: ApiInfo, caseFlag?: true): Promise<HTTPConfig> => {
  const {
    id,
    apisId,
    caseId,
    name,
    serviceId,
    endpoint,
    availableServers,
    currentServer,
    summary,
    method,
    assertions: _assertions = [],
    _variables,
    datasets,
    parameters: _parameters = [],
    requestBody: _requestBody,
    resolvedRefModels = {},
    authentication: _authentication
  } = data;
  // let url = '';
  const server: HTTPConfig['request']['server'] = { url: '', variables: {} };
  if (caseFlag) {
    if (currentServer) {
      server.url = currentServer.url;
      if (currentServer.description) {
        server.description = currentServer.description;
      }
      if (currentServer.variables) {
        const variables = currentServer.variables;
        for (const key in variables) {
          server.variables[key] = {
            defaultValue: variables[key].default,
            allowableValues: variables[key].enum
          };
        }
      }
    }
  } else {
    let availableServer: ApiInfo['availableServers'][number];
    if (availableServers?.length) {
      availableServer = availableServers.find(i => i[serverSourceKey] === 'CURRENT_REQUEST') || availableServers[0];
      if (availableServer) {
        server.url = availableServer.url;
        if (availableServer.description) {
          server.description = availableServer.description;
        }
        if (availableServer?.variables) {
          const variables = availableServer.variables;
          for (const key in variables) {
            server.variables[key] = {
              defaultValue: variables[key].default,
              allowableValues: variables[key].enum
            };
          }
        }
      }
    }
  }

  let assertions: HTTPConfig['assertions'] = [];
  if (_assertions?.length) {
    assertions = _assertions.map(item => {
      let extraction: HTTPConfig['assertions'][number]['extraction'];
      const _extraction = item.extraction;
      if (_extraction) {
        extraction = {
          ..._extraction,
          method: _extraction.method?.value || _extraction.method
        };
      }
      return {
        ...item,
        assertionCondition: item.assertionCondition?.value || item.assertionCondition,
        type: item.type?.value || item.type,
        extraction
      };
    });
  }

  const requestBody = analysisBody(_requestBody, resolvedRefModels);
  let authentication = _authentication;
  if (authentication?.$ref) {
    const [error, { data: _data }] = await angusUtils.getModelDataByRef(serviceId, authentication.$ref);
    if (!error) {
      authentication = JSON.parse(_data.model);
    }
  }

  const parameters: HTTPConfig['request']['parameters'] = [];
  for (let i = 0, len = _parameters.length; i < len; i++) {
    const item = _parameters[i];
    const _parameter: HTTPConfig['request']['parameters'][number] = analysisParameters(item, resolvedRefModels || {});
    const { in: _in, name } = _parameter;
    const _value = _parameter[valueKey];
    const _enabled = item[enabledKey] !== false;
    if (_in === 'query') {
      const itemJSon = {
        [name]: _parameter[valueKey] || ''
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
          const value = Object.keys(_parameter[valueKey]).map(key => {
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

  await replaceApiVariable(id, requestBody, parameters, authentication, caseFlag ? apisId : id);

  authentication = formApiAuthToScenarioAuth(authentication);

  // 找到Content-Type
  const contentType = parameters?.find(item => item.in === 'header' && item.name === 'Content-Type')?.value;

  // 把openapi规范的requestBody转为body
  const body = openapiRequestBodyToBody(requestBody?.content, contentType);

  const httpData: HTTPConfig = {
    id: utils.uuid(),
    target: 'HTTP',
    name: caseFlag ? name : summary,
    description: '',
    enabled: true,
    beforeName: '',
    transactionName: '',
    assertions,
    _variables,
    datasets,
    request: {
      endpoint,
      method: method.value,
      server,
      parameters,
      authentication,
      body
    },
    apisId,
    caseId
  };

  return httpData;
};

const selectApiOk = async (data: any[]) => {
  apiModalVisible.value = false;
  for (let i = 0, len = data.length; i < len; i++) {
    const item = data[i];
    const httpData = await transformOpenApiTo(item);
    emit('variableChange', httpData?._variables);
    if (typeof dragRef.value?.add === 'function') {
      dragRef.value.add(httpData);
      if (item.apisId) {
        linkApiIds.value.add(item.apisId);
      }
    }
  }

  scrollToBottom();
};

const selectUseCase = () => {
  useCaseModalVisible.value = true;
};

const selectUseCaseOk = async (data: ApiInfo[]) => {
  for (let i = 0, len = data.length; i < len; i++) {
    const item = data[i];
    const httpData = await transformOpenApiTo(item, true);
    delete item.apisId;
    emit('variableChange', httpData?._variables);
    if (typeof dragRef.value?.add === 'function') {
      dragRef.value.add(httpData);
      if (item.caseId) {
        linkCaseIds.value.add(item.caseId);
      }
    }
  }

  scrollToBottom();
};

const insertTransStart = () => {
  isTransEnd.value = true;
  const data: TransStartConfig = {
    id: utils.uuid(),
    target: 'TRANS_START',
    name: '',
    description: '',
    enabled: true,
    beforeName: '',
    transactionName: ''
  };

  if (typeof dragRef.value?.add === 'function') {
    dragRef.value.add(data);
  }

  scrollToBottom();
};

const insertTransEnd = () => {
  isTransEnd.value = false;
  const data: TransEndConfig = {
    id: utils.uuid(),
    target: 'TRANS_END',
    name: '',
    description: '',
    beforeName: '',
    enabled: true,
    transactionName: ''
  };

  if (typeof dragRef.value?.add === 'function') {
    dragRef.value.add(data);
  }

  scrollToBottom();
};

let waitingTimeNum = 0;
const insertTime = () => {
  const data: WaitingTimeConfig = {
    id: utils.uuid(),
    beforeName: '',
    target: 'WAITING_TIME',
    name: t('httpPlugin.uiConfig.defaultNames.waitingTime') + '-' + waitingTimeNum++,
    description: '',
    enabled: true,
    minWaitTimeInMs: '',
    maxWaitTimeInMs: '',
    transactionName: ''
  };

  if (typeof dragRef.value?.add === 'function') {
    dragRef.value.add(data);
  }

  scrollToBottom();
};

let qbsNum = 0;
const insertQBS = () => {
  const data: ThroughputConfig = {
    id: utils.uuid(),
    target: 'THROUGHPUT',
    name: t('httpPlugin.uiConfig.defaultNames.throughputLimiter') + '-' + qbsNum++,
    description: '',
    enabled: true,
    beforeName: '',
    timeoutInMs: '',
    permitsPerSecond: '',
    transactionName: ''
  };

  if (typeof dragRef.value?.add === 'function') {
    dragRef.value.add(data);
  }

  scrollToBottom();
};

let rendezvousNum = 0;
const insertRendezvous = () => {
  const data: RendezvousConfig = {
    id: utils.uuid(),
    target: 'RENDEZVOUS',
    name: t('httpPlugin.uiConfig.defaultNames.rendezvous') + '-' + rendezvousNum++,
    description: '',
    enabled: true,
    beforeName: '',
    timeoutInMs: '',
    threads: '',
    transactionName: ''
  };

  if (typeof dragRef.value?.add === 'function') {
    dragRef.value.add(data);
  }

  scrollToBottom();
};

const deleteHandler = (deletedConfigs: PipelineConfig[], existData: {
  id: string;
  target: TargetKey;
  children?: {
    id: string;
    target: TargetKey;
  }[];
}[]) => {
  if (deletedConfigs?.length) {
    for (let i = 0, len = deletedConfigs.length; i < len; i++) {
      const data = deletedConfigs[i];
      if (data.target === 'HTTP') {
        const { apisId, caseId } = data;
        if (apisId) {
          linkApiIds.value.delete(apisId);
        }

        if (caseId) {
          linkCaseIds.value.delete(caseId);
        }
      }
    }
  }

  if (!existData.length) {
    isTransEnd.value = false;
    return;
  }

  let transNum = 0;
  let hasUncloseTrans = false;
  for (let i = 0, len = existData.length; i < len; i++) {
    const { target, children } = existData[i];
    if (target === 'TRANS_START') {
      transNum++;
      if (!children?.length) {
        hasUncloseTrans = true;
        break;
      } else {
        const hasTransEnd = children.find(item => item.target === 'TRANS_END');
        if (!hasTransEnd) {
          hasUncloseTrans = true;
          break;
        }
      }
    }
  }

  if (transNum) {
    isTransEnd.value = hasUncloseTrans;
  } else {
    isTransEnd.value = false;
  }
};

const errorNumChange = (value: number) => {
  emit('errorNumChange', value);
};

onMounted(() => {
  watch(() => props.value, (newValue) => {
    if (!newValue?.length) {
      return;
    }

    for (let i = 0, len = newValue.length; i < len; i++) {
      const data = newValue[i];
      if (data.target === 'HTTP') {
        if (data.apisId) {
          linkApiIds.value.add(data.apisId);
        }

        if (data.caseId) {
          linkCaseIds.value.add(data.caseId);
        }
      }
    }
  }, { immediate: true });
});

const getData = () => {
  if (typeof dragRef.value?.getData === 'function') {
    return dragRef.value.getData();
  }

  return [];
};

defineExpose({
  isValid: (): boolean => {
    if (typeof dragRef.value?.isValid === 'function') {
      return dragRef.value.isValid();
    }

    return true;
  },
  getData
});

</script>

<template>
  <div class="py-5 h-full text-3 leading-5">
    <div class="flex items-center flex-nowrap whitespace-nowrap space-x-2 px-5">
      <Button
        type="default"
        size="small"
        @click="insertHTTP">
        <div class="flex items-center">
          <Icon icon="icon-httpcanshu" class="mr-1" />
          <span>{{ t('httpPlugin.uiConfig.buttons.insertApi') }}</span>
        </div>
      </Button>
      <Button
        type="default"
        size="small"
        @click="selectHTTP">
        <div class="flex items-center">
          <Icon icon="icon-xuanzejiekou" class="mr-1" />
          <span>{{ t('httpPlugin.uiConfig.buttons.selectApi') }}</span>
        </div>
      </Button>
      <Button
        type="default"
        size="small"
        @click="selectUseCase">
        <div class="flex items-center">
          <Icon icon="icon-xuanzeyongli" class="mr-1" />
          <span>{{ t('httpPlugin.uiConfig.buttons.selectUseCase') }}</span>
        </div>
      </Button>
      <Button
        v-if="!isTransEnd"
        type="default"
        size="small"
        @click="insertTransStart">
        <div class="flex items-center">
          <Icon icon="icon-shiwu" class="mr-1" />
          <span>{{ t('httpPlugin.uiConfig.buttons.insertTransStart') }}</span>
        </div>
      </Button>
      <Button
        v-else
        type="default"
        size="small"
        @click="insertTransEnd">
        <div class="flex items-center">
          <Icon icon="icon-shiwu" class="mr-1" />
          <span>{{ t('httpPlugin.uiConfig.buttons.insertTransEnd') }}</span>
        </div>
      </Button>
      <Button
        type="default"
        size="small"
        @click="insertTime">
        <div class="flex items-center">
          <Icon icon="icon-dengdaishijian" class="mr-1" />
          <span>{{ t('httpPlugin.uiConfig.buttons.insertWaitingTime') }}</span>
        </div>
      </Button>
      <Button
        type="default"
        size="small"
        @click="insertQBS">
        <div class="flex items-center">
          <Icon icon="icon-zusai" class="mr-1" />
          <span>{{ t('httpPlugin.uiConfig.buttons.insertThroughputLimiter') }}</span>
        </div>
      </Button>
      <Button
        type="default"
        size="small"
        @click="insertRendezvous">
        <div class="flex items-center">
          <Icon icon="icon-jihedian1" class="mr-1" />
          <span>{{ t('httpPlugin.uiConfig.buttons.insertRendezvous') }}</span>
        </div>
      </Button>
    </div>

    <Draggable
      :id="domId"
      ref="dragRef"
      :value="props.value"
      :loaded="props.loaded"
      style="height: calc(100% - 40px);"
      class="my-4 pl-5 pr-4 space-y-3 overflow-y-auto overflow-x-hidden scroll-smooth"
      @delete="deleteHandler"
      @errorNumChange="errorNumChange"
      @renderChange="renderChange" />
    <AsyncComponent :visible="apiModalVisible">
      <SelectApiModal
        v-model:visible="apiModalVisible"
        :projectId="projectId"
        :linkIds="linkApiIds"
        @ok="selectApiOk" />
        <!-- <SelectApisByService
          v-model:visible="apiModalVisible"
          :projectId="projectId"
          @ok="selectApiOk" /> -->
    </AsyncComponent>

    <AsyncComponent :visible="useCaseModalVisible">
      <SelectUseCaseModal
        v-model:visible="useCaseModalVisible"
        :linkIds="linkCaseIds"
        @ok="selectUseCaseOk" />
    </AsyncComponent>
  </div>
</template>
