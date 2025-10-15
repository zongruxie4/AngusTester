<script setup lang="ts">
import { defineAsyncComponent, nextTick, ref, onMounted, watch, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Checkbox, Button, Badge, Collapse, CollapsePanel, Tabs, TabPane, Switch } from 'ant-design-vue';
import { Icon, AsyncComponent, NoData, Input, SelectEnum, Tooltip, Validate, Select, Arrow, IconRequired, Colon, FunctionsButton } from '@xcan-angus/vue-ui';
import { utils, duration } from '@xcan-angus/infra';
import { uniq } from 'lodash-es';
import qs from 'qs';
import Draggable from 'vuedraggable';
import { debounce } from 'throttle-debounce';
import { variable } from '@/api/tester';

import angusUtils from 'src/utils/apis';
import { ParameterConfig, PipelineConfig } from '../PropsType';
import { ApiInfo } from './SelectApiModal/PropsType';

const { t } = useI18n();

export interface Props {
  value: PipelineConfig[];
  loaded: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  loaded: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'errorNumChange', value: number): void;
  (e: 'renderChange', value: boolean): void;
}>();

const ParamInput = defineAsyncComponent(() => import('@/components/ParamInput/index.vue'));
const SelectApiModal = defineAsyncComponent(() => import('./SelectApiModal/index.vue'));
const AssertionForm = defineAsyncComponent(() => import('./AssertionForm/index.vue'));
const Parametric = defineAsyncComponent(() => import('./Parametric/index.vue'));

const assertRefsMap = ref<{[key:string]:any}>({});
const parametricRefsMap = ref<{[key:string]:any}>({});
const apiModalVisible = ref(false);

const domId = utils.uuid();
const rendered = ref(false);
const activeKeys = ref<string[]>([]);

const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: PipelineConfig }>({});
const linkApiIds = ref<Set<string>>(new Set());

const queryParametersMap = ref<{ [key: string]: PipelineConfig['parameters'] }>({});
const headerParametersMap = ref<{ [key: string]: PipelineConfig['parameters'] }>({});

const queryParametersCheckedIdSet = ref<Set<string>>(new Set());
const headerParametersCheckedIdSet = ref<Set<string>>(new Set());

const emptyNameIdSet = ref<Set<string>>(new Set());
const repeatNameIdSet = ref<Set<string>>(new Set());
const emptyUrlIdSet = ref<Set<string>>(new Set());
const urlFormatErrorIdSet = ref<Set<string>>(new Set());
const modeErrorIdSet = ref<Set<string>>(new Set());
const messageErrorIdSet = ref<Set<string>>(new Set());
const queryParameterNameErrorSet = ref<Set<string>>(new Set());
const headerParameterNameErrorSet = ref<Set<string>>(new Set());

const queryErrorNumMap = ref<Map<string, number>>(new Map());
const headerErrorNumMap = ref<Map<string, number>>(new Map());

const insertWebSocket = () => {
  const hasEnabled = Object.values(dataMap.value).find(item => item.enabled === true);
  const id = utils.uuid();
  dataMap.value[id] = {
    beforeName: '',
    apisId: '',
    id: utils.uuid(),
    target: 'WEBSOCKET',
    name: '',
    description: '',
    enabled: !hasEnabled,
    assertions: [],
    datasets: [],
    actionOnEOF: 'RECYCLE',
    sharingMode: 'ALL_THREAD',
    mode: 'ONLY_SEND',
    parameters: [],
    url: '',
    server: undefined,
    endpoint: '',
    message: '',
    messageEncoding: 'none'
  };

  if (!hasEnabled) {
    activeKeys.value.push(id);
  }

  // 插入请求参数
  queryParametersMap.value[id] = [generateParameter('query')];
  headerParametersMap.value[id] = [generateParameter('header')];

  idList.value.push(id);

  scrollToBottom();
};

const selectWebSocket = () => {
  apiModalVisible.value = true;
};

const getServerUrl = (data: PipelineConfig['server']): string => {
  if (!data) {
    return '';
  }

  const { url, variables } = data;
  const variableReg = /\{[a-zA-Z0-9_]+\}/g;
  const replaced = url?.replace(variableReg, match => {
    const key = match.replace('{', '').replace('}', '');
    return variables?.[key]?.defaultValue || '';
  });

  return replaced || '';
};

const extractVar = (str: string): string => {
  const regex = /{([^}]+)}/;
  const match = str.match(regex);
  return match ? match[1] : str;
};

const replaceApiVariable = async (
  id: string,
  requestBody: ApiInfo['requestBody'],
  parameters: PipelineConfig['parameters']) => {
  const variableNames = [];
  const variableRegReplace = /\{([a-zA-Z0-9!@$%^&*()_\-+=./]+)\}/g;

  let matchVariable = JSON.stringify(parameters).match(variableRegReplace);
  variableNames.push(...(matchVariable || []).map(item => extractVar(item)));

  matchVariable = JSON.stringify(requestBody || {}).match(variableRegReplace);
  variableNames.push(...(matchVariable || []).map(item => extractVar(item)));

  if (!variableNames.length) {
    return;
  }

  const [error, { data = [] }] = await variable.getVariableValue({
    names: uniq(variableNames),
    targetId: id,
    targetType: 'API'
  });

  if (error) {
    return;
  }

  const response = data.map(item => {
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
};

const selectApiOk = async (data: ApiInfo) => {
  const newId = utils.uuid();
  const {
    id: apiId,
    apisId,
    description,
    requestBody,
    parameters: _parameters = [],
    summary,
    endpoint = '',
    resolvedRefModels,
    availableServers
  } = data;

  let availableServer: ApiInfo['availableServers'][number];
  if (availableServers?.length) {
    availableServer = availableServers.find(i => i['x-xc-serverSource'] === 'CURRENT_REQUEST') || availableServers[0];
  }

  const server: PipelineConfig['server'] = { ...availableServer, variables: {} };
  if (availableServer?.variables) {
    const variables = availableServer.variables;
    for (const key in variables) {
      server.variables[key] = {
        defaultValue: variables[key].default,
        allowableValues: variables[key].enum
      };
    }
  }

  queryParametersMap.value[newId] = [];
  headerParametersMap.value[newId] = [];

  for (let i = 0, len = _parameters.length; i < len; i++) {
    const item = _parameters[i];
    const _parameter: PipelineConfig['parameters'][number] = angusUtils.analysisParameters(item, resolvedRefModels || {});
    const { in: _in, name } = _parameter;
    const _value = _parameter['x-xc-value'];
    const _enabled = item['x-xc-enabled'] !== false;
    if (_in === 'query') {
      const itemJSon = {
        [name]: _parameter['x-xc-value'] || ''
      };
      const itemStrs = qs.stringify(itemJSon, { allowDots: true, encode: false, encodeValuesOnly: true }).split('&');
      itemStrs.forEach(str => {
        const [key, value] = str.split('=');
        queryParametersMap.value[newId].push({ id: utils.uuid(), type: 'string', name: key, value, in: _in, enabled: _enabled });
      });

      continue;
    }

    if (_in === 'header') {
      if (typeof _value === 'object') {
        if (Object.prototype.toString.call(_value) === '[object Object]') {
          const value = Object.keys(_parameter['x-xc-value']).map(key => {
            return `${key}=${JSON.stringify(_value[key])}`;
          }).join(',');
          headerParametersMap.value[newId].push({ id: utils.uuid(), type: 'string', name, value, in: _in, enabled: _enabled });

          continue;
        }

        if (Object.prototype.toString.call(_value) === '[object Array]') {
          const value = _value.map(item => {
            return JSON.stringify(item);
          }).join(',');
          headerParametersMap.value[newId].push({ id: utils.uuid(), type: 'string', name, value, in: _in, enabled: _enabled });

          continue;
        }

        continue;
      }

      headerParametersMap.value[newId].push({ id: utils.uuid(), type: 'string', name, value: _value, in: _in, enabled: _enabled });
      continue;
    }
  }

  const parameters = [...queryParametersMap.value[newId], ...headerParametersMap.value[newId]];
  // 添加空数据
  queryParametersMap.value[newId].push(generateParameter('query'));
  headerParametersMap.value[newId].push(generateParameter('header'));

  await replaceApiVariable(apiId, requestBody, parameters);

  const hasEnabled = Object.values(dataMap.value).find(item => item.enabled === true);
  const url = getServerUrl(server) + endpoint;
  dataMap.value[newId] = {
    id: newId,
    apisId,
    beforeName: '',
    assertions: [],
    datasets: [],
    actionOnEOF: 'RECYCLE',
    sharingMode: 'ALL_THREAD',
    description,
    parameters,
    name: summary,
    enabled: !hasEnabled,
    target: 'WEBSOCKET',
    mode: 'ONLY_SEND',
    message: data.requestBody['x-wsMessage'],
    messageEncoding: 'none',
    server,
    url,
    endpoint
  };

  if (apisId) {
    linkApiIds.value.add(apisId);
  }

  if (!hasEnabled) {
    activeKeys.value.push(newId);
  }

  idList.value.push(newId);

  scrollToBottom();
};

const checkedRepeatName = (id?: string) => {
  if (id) {
    emptyNameIdSet.value.delete(id);
  }

  repeatNameIdSet.value.clear();
  const uniqueNames = new Set();
  const repeatNameSet = new Set();
  const map = dataMap.value;
  for (const key in map) {
    const name = map[key].name;
    if (name) {
      if (uniqueNames.has(name)) {
        repeatNameSet.add(name);
      } else {
        uniqueNames.add(name);
      }
    }
  }

  for (const key in map) {
    if (repeatNameSet.has(map[key].name)) {
      repeatNameIdSet.value.add(key);
    }
  }
};

const nameChange = debounce(duration.search, checkedRepeatName);

const openChange = (id: string, _open: boolean) => {
  if (_open) {
    activeKeys.value.push(id);
    return;
  }

  activeKeys.value = activeKeys.value.filter(item => item !== id);
};

// 只能启用一个
const enabledChange = (id: string, enabled: boolean) => {
  const ids = idList.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    dataMap.value[ids[i]].enabled = false;
  }

  dataMap.value[id].enabled = enabled;
};

const toClone = (targetId: string) => {
  const ids = idList.value;
  const cloneId = utils.uuid();
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    if (id === targetId) {
      const cloneData: PipelineConfig = JSON.parse(JSON.stringify(dataMap.value[targetId]));
      dataMap.value[cloneId] = cloneData;
      dataMap.value[cloneId].name = cloneData.name ? (cloneData.name + ' copy') : '';
      dataMap.value[cloneId].enabled = false;

      // 克隆请求参数
      queryParametersMap.value[cloneId] = [...queryParametersMap.value[targetId]];
      headerParametersMap.value[cloneId] = [...headerParametersMap.value[targetId]];

      // 克隆断言内容
      if (typeof assertRefsMap.value?.[id]?.getData === 'function') {
        dataMap.value[cloneId].assertions = assertRefsMap.value[id].getData();
      }

      // 克隆断言内容
      if (typeof parametricRefsMap.value?.[id]?.getData === 'function') {
        dataMap.value[cloneId].datasets = parametricRefsMap.value[id].getData();
      }

      idList.value.splice(i + 1, 0, cloneId);

      break;
    }
  }

  // 判断克隆的名称是否已经存在
  if (dataMap.value[cloneId].name) {
    checkedRepeatName();
  }
};

const toDelete = (targetId: string) => {
  const ids = idList.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    if (id === targetId) {
      activeKeys.value = activeKeys.value.filter(item => item !== id);

      if (dataMap.value[id].apisId) {
        linkApiIds.value.delete(dataMap.value[id].apisId);
      }

      const queryIds = queryParametersMap.value[id].map(item => item.id);
      for (let i = 0, len = queryIds.length; i < len; i++) {
        queryParametersCheckedIdSet.value.delete(queryIds[i]);
        queryParameterNameErrorSet.value.delete(queryIds[i]);
      }

      const headerIds = headerParametersMap.value[id].map(item => item.id);
      for (let i = 0, len = headerIds.length; i < len; i++) {
        headerParametersCheckedIdSet.value.delete(headerIds[i]);
        headerParameterNameErrorSet.value.delete(headerIds[i]);
      }

      delete queryParametersMap.value[id];
      delete headerParametersMap.value[id];
      queryErrorNumMap.value.delete(id);
      headerErrorNumMap.value.delete(id);

      emptyNameIdSet.value.delete(id);
      repeatNameIdSet.value.delete(id);
      emptyUrlIdSet.value.delete(id);
      urlFormatErrorIdSet.value.delete(id);
      modeErrorIdSet.value.delete(id);
      messageErrorIdSet.value.delete(id);

      delete dataMap.value[id];
      idList.value.splice(i, 1);

      break;
    }
  }

  checkedRepeatName(targetId);
};

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

const urlChange = (id: string) => {
  emptyNameIdSet.value.delete(id);
  urlFormatErrorIdSet.value.delete(id);
};

const urlBlur = (id: string, event: { target: { value: string } }) => {
  const value = event.target.value;
  if (value) {
    if (!/^ws:\/\/|wss:\/\//.test(value)) {
      urlFormatErrorIdSet.value.add(id);
      return;
    }

    urlFormatErrorIdSet.value.delete(id);
    return;
  }

  urlFormatErrorIdSet.value.delete(id);
};

const generateParameter = (_in:'header'|'query'):ParameterConfig => {
  return {
    id: utils.uuid(),
    in: _in,
    enabled: false,
    type: 'string',
    name: '',
    value: ''
  };
};

const queryParameterNameChange = debounce(duration.search, (pid: string, id: string, index: number, event: { target: { value: string } }) => {
  queryParameterNameErrorSet.value.delete(id);
  const value = event.target.value;
  if (!queryParametersCheckedIdSet.value.has(id)) {
    if (value) {
      queryParametersCheckedIdSet.value.add(id);
      queryParametersMap.value[pid][index].enabled = true;
    }
  }

  // 保证最后一条是空的
  if (index === queryParametersMap.value[pid].length - 1) {
    if (value) {
      queryParametersMap.value[pid].push(generateParameter('query'));
    }
  }
});

const queryParameterValueChange = (pid: string, _id: string, index: number, target: Element) => {
  const value = target?.innerText?.trim()?.replaceAll('\n', '');
  queryParametersMap.value[pid][index].value = value;
};

const deleteQueryParameter = (pid: string, id: string, index: number) => {
  const length = queryParametersMap.value[pid].length - 1;
  queryParametersMap.value[pid].splice(index, 1);
  queryParametersCheckedIdSet.value.delete(id);
  queryParameterNameErrorSet.value.delete(id);
  queryErrorNumMap.value.delete(pid);
  if (index === length) {
    queryParametersMap.value[pid].push(generateParameter('query'));
  }
};

const headerParameterNameChange = debounce(duration.search, (pid: string, id: string, index: number, event: { target: { value: string } }) => {
  headerParameterNameErrorSet.value.delete(id);
  const value = event.target.value;
  if (!headerParametersCheckedIdSet.value.has(id)) {
    if (value) {
      headerParametersCheckedIdSet.value.add(id);
      headerParametersMap.value[pid][index].enabled = true;
    }
  }

  // 保证最后一条是空的
  if (index === headerParametersMap.value[pid].length - 1) {
    if (value) {
      headerParametersMap.value[pid].push(generateParameter('header'));
    }
  }
});

const headerParameterValueChange = (pid: string, _id: string, index: number, target: Element) => {
  const value = target?.innerText?.trim()?.replaceAll('\n', '');
  headerParametersMap.value[pid][index].value = value;
};

const deleteHeaderParameter = (pid: string, id: string, index: number) => {
  const length = headerParametersMap.value[pid].length - 1;
  headerParametersMap.value[pid].splice(index, 1);
  headerParametersCheckedIdSet.value.delete(id);
  headerParameterNameErrorSet.value.delete(id);
  headerErrorNumMap.value.delete(pid);
  if (index === length) {
    headerParametersMap.value[pid].push(generateParameter('header'));
  }
};

const modeChange = (id: string, value: PipelineConfig['mode']) => {
  modeErrorIdSet.value.delete(id);
  if (value === 'ONLY_RECEIVE') {
    dataMap.value[id].message = '';
  }
};

const messageChange = (id: string) => {
  messageErrorIdSet.value.delete(id);
};

const reset = () => {
  assertRefsMap.value = {};
  parametricRefsMap.value = {};
  activeKeys.value = [];

  idList.value = [];
  dataMap.value = {};
  linkApiIds.value.clear();

  queryParametersMap.value = {};
  headerParametersMap.value = {};

  queryParametersCheckedIdSet.value.clear();
  headerParametersCheckedIdSet.value.clear();

  emptyNameIdSet.value.clear();
  repeatNameIdSet.value.clear();
  emptyUrlIdSet.value.clear();
  urlFormatErrorIdSet.value.clear();
  modeErrorIdSet.value.clear();
  messageErrorIdSet.value.clear();
  queryParameterNameErrorSet.value.clear();
  headerParameterNameErrorSet.value.clear();

  queryErrorNumMap.value.clear();
  headerErrorNumMap.value.clear();
};

onMounted(() => {
  watch(() => props.value, (newValue) => {
    reset();
    if (!newValue?.length) {
      return;
    }

    const list = JSON.parse(JSON.stringify(newValue)) as PipelineConfig[];
    for (let i = 0, len = list.length; i < len; i++) {
      const data = list[i];
      const { id, apisId, enabled, parameters } = data;
      dataMap.value[id] = data;

      if (enabled === true) {
        activeKeys.value.push(id);
      }

      if (apisId) {
        linkApiIds.value.add(apisId);
      }

      const _parameters = parameters || [];
      const queryParameters = _parameters.filter(item => item.in === 'query').map(item => {
        return {
          ...item,
          id: utils.uuid()
        };
      });
      if (queryParameters.length) {
        queryParametersMap.value[id] = [...queryParameters, generateParameter('query')];
      } else {
        queryParametersMap.value[id] = [generateParameter('query')];
      }

      const headerParameters = _parameters.filter(item => item.in === 'header').map(item => {
        return {
          ...item,
          id: utils.uuid()
        };
      });
      if (headerParameters.length) {
        headerParametersMap.value[id] = [...headerParameters, generateParameter('header')];
      } else {
        headerParametersMap.value[id] = [generateParameter('header')];
      }

      idList.value.push(id);
    }
  }, { immediate: true });

  emit('renderChange', true);
  rendered.value = true;
});

const nameErrorSet = computed(() => {
  return new Set<string>([...emptyNameIdSet.value, ...repeatNameIdSet.value]);
});

const urlErrorSet = computed(() => {
  return new Set<string>([...emptyUrlIdSet.value, ...urlFormatErrorIdSet.value]);
});

const getData = (): Omit<PipelineConfig, 'id'>[] => {
  const result: Omit<PipelineConfig, 'id'>[] = [];
  const ids = idList.value;
  const _dataMap = dataMap.value;
  let beforeName = '';
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const {
      apisId,
      assertions,
      datasets,
      actionOnEOF = 'RECYCLE',
      sharingMode = 'ALL_THREAD',
      description,
      enabled,
      endpoint,
      message,
      messageEncoding,
      mode,
      name,
      server,
      target,
      url
    } = _dataMap[id];

    const queryParameters = queryParametersMap.value[id].slice(0, -1);
    const headerParameters = headerParametersMap.value[id].slice(0, -1);
    const parameters = [...queryParameters, ...headerParameters];
    let _assertions = assertions;
    if (typeof assertRefsMap.value?.[id]?.getData === 'function') {
      _assertions = assertRefsMap.value[id].getData();
    }

    let _datasets = datasets;
    if (typeof parametricRefsMap.value?.[id]?.getData === 'function') {
      _datasets = parametricRefsMap.value[id].getData();
    }

    result.push({
      apisId,
      description,
      enabled,
      endpoint,
      message,
      messageEncoding,
      mode,
      name,
      parameters,
      server,
      target,
      url,
      beforeName,
      assertions: _assertions,
      datasets: _datasets,
      actionOnEOF,
      sharingMode
    });

    beforeName = name;
  }

  return result;
};

const isValid = (): boolean => {
  emptyNameIdSet.value.clear();
  emptyUrlIdSet.value.clear();
  modeErrorIdSet.value.clear();
  messageErrorIdSet.value.clear();
  queryParameterNameErrorSet.value.clear();
  headerParameterNameErrorSet.value.clear();
  queryErrorNumMap.value.clear();
  headerErrorNumMap.value.clear();

  const ids = idList.value;
  const _dataMap = dataMap.value;
  const set = repeatNameIdSet.value;
  let errorNum = 0;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const { name, url, mode, message } = _dataMap[id];
    if (!name) {
      emptyNameIdSet.value.add(id);
      errorNum++;
    } else {
      if (set.has(id)) {
        errorNum++;
      }
    }

    if (!mode) {
      modeErrorIdSet.value.add(id);
      errorNum++;
    } else if (['ONLY_SEND', 'SEND_AND_RECEIVE'].includes(mode)) {
      if (!message) {
        messageErrorIdSet.value.add(id);
        errorNum++;
      }
    }

    if (!url) {
      emptyUrlIdSet.value.add(id);
      errorNum++;
    } else {
      if (urlFormatErrorIdSet.value.has(id)) {
        errorNum++;
      } else if (!/^ws:\/\/|wss:\/\//.test(url)) {
        errorNum++;
        urlFormatErrorIdSet.value.add(id);
      }
    }

    const queryParameters = queryParametersMap.value[id] || [];
    let queryErrorNum = 0;
    for (let j = 0, len = queryParameters.length - 1; j < len; j++) {
      if (!queryParameters[j].name) {
        const _id = queryParameters[j].id;
        queryParameterNameErrorSet.value.add(_id);
        queryErrorNum++;
        errorNum++;
      }
    }
    queryErrorNumMap.value.set(id, queryErrorNum);

    const headerParameters = headerParametersMap.value[id] || [];
    let headerErrorNum = 0;
    for (let j = 0, len = headerParameters.length - 1; j < len; j++) {
      if (!headerParameters[j].name) {
        const _id = headerParameters[j].id;
        headerParameterNameErrorSet.value.add(_id);
        headerErrorNum++;
        errorNum++;
      }
    }
    headerErrorNumMap.value.set(id, headerErrorNum);

    if (typeof assertRefsMap.value[id]?.isValid === 'function') {
      const validFlag = assertRefsMap.value[id].isValid();
      if (validFlag) {
        errorNum++;
      }
    }
  }

  return !errorNum;
};

defineExpose({
  isValid,
  getData
});

const codeOptions = [
  { label: t('common.none'), value: 'none' },
  { label: 'base64', value: 'base64' },
  { label: 'gzip_base64', value: 'gzip_base64' }
];

const autoSize = {
  minRows: 5,
  maxRows: 10
};
</script>

<template>
  <div class="py-5 h-full text-3 leading-5">
    <div class="flex items-center flex-nowrap whitespace-nowrap space-x-2 px-5">
      <Button
        type="default"
        size="small"
        @click="insertWebSocket">
        <div class="flex items-center">
          <Icon icon="icon-WS" class="mr-1" />
          <span>{{ t('websocketPlugin.uiConfig.title') }}</span>
        </div>
      </Button>
      <Button
        type="default"
        size="small"
        @click="selectWebSocket">
        <div class="flex items-center">
          <Icon icon="icon-xuanzejiekou" class="mr-1" />
          <span>{{ t('websocketPlugin.uiConfig.selectTitle') }}</span>
        </div>
      </Button>
      <div class="flex-1 flex items-center overflow-hidden" :title="t('common.description')">
        <Icon
          icon="icon-tishi1"
          class="flex-shrink-0 text-3.5 mr-0.5"
          style="color:#a6ceff;" />
        <span class="text-theme-sub-content truncate">{{ t('common.description') }}</span>
      </div>
    </div>

    <template v-if="props.loaded && !idList.length">
      <NoData style="height: calc(100% - 40px);" />
    </template>
    <template v-else>
      <Draggable
        :id="domId"
        :list="idList"
        :animation="300"
        group="scenario"
        itemKey="id"
        tag="ul"
        handle=".drag-handle"
        style="height: calc(100% - 40px);"
        class="my-4 pl-5 pr-4 space-y-3 overflow-y-auto overflow-x-hidden scroll-smooth">
        <template #item="{ element: id }">
          <li
            :id="id"
            :key="id"
            class="drag-item relative">
            <Icon
              :class="{ invisible: !rendered }"
              icon="icon-yidong"
              class="drag-handle absolute top-3.75 left-3 z-10 text-4 cursor-move text-theme-sub-content" />
            <Collapse
              :activeKey="activeKeys"
              :class="{ 'opacity-70': !dataMap[id].enabled }"
              :bordered="false">
              <CollapsePanel
                :key="id"
                :showArrow="false"
                style="background-color: rgba(247, 248, 251, 100%);"
                collapsible="disabled">
                <template #header>
                  <div class="flex items-center flex-nowrap w-full whitespace-nowrap">
                    <Icon
                      v-if="dataMap[id].apisId"
                      icon="icon-yinyongWS"
                      class="flex-shrink-0 text-4 mr-3" />
                    <Icon
                      v-else
                      icon="icon-WS"
                      class="flex-shrink-0 text-4 mr-3" />
                    <div class="flex-1 flex items-center space-x-2 mr-3">
                      <Tooltip
                        :title="t('websocketPlugin.uiConfig.form.nameDuplicate')"
                        internal
                        placement="right"
                        destroyTooltipOnHide
                        :visible="!!repeatNameIdSet.has(id)">
                        <Input
                          v-model:value="dataMap[id].name"
                          :maxlength="400"
                          :error="!!nameErrorSet.has(id)"
                          :title="dataMap[id].name"
                          style="flex:1 1 40%;"
                          trim
                          :placeholder="t('common.placeholders.searchKeyword')"
                          @change="nameChange(id)" />
                      </Tooltip>
                      <Input
                        v-model:value="dataMap[id].description"
                        :maxlength="800"
                        :title="dataMap[id].description"
                        trim
                        style="flex:1 1 60%;"
                        :placeholder="t('websocketPlugin.form.descriptionPlaceholder')" />
                    </div>
                    <div class="flex items-center flex-shrink-0 space-x-3">
                      <Switch
                        :checked="dataMap[id].enabled"
                        size="small"
                        @change="enabledChange(id, $event)" />
                      <div class="flex items-center cursor-pointer hover:text-text-link-hover" :title="t('actions.clone')">
                        <Icon
                          icon="icon-fuzhi"
                          class="text-3.5"
                          @click="toClone(id)" />
                      </div>
                      <div class="flex items-center cursor-pointer hover:text-text-link-hover" :title="t('actions.delete')">
                        <Icon
                          icon="icon-qingchu"
                          class="text-3.5"
                          @click="toDelete(id)" />
                      </div>
                      <Arrow :open="activeKeys.includes(id)" @change="openChange(id, $event)" />
                    </div>
                  </div>
                </template>
                <div class="flex flex-col items-end leading-5 space-y-3.5 text-3">
                  <div class="w-full flex items-start">
                    <div class="w-21.5 transform-gpu translate-y-1 flex-shrink-0 text-theme-title">
                      <IconRequired />
                      <span>{{ t('websocketPlugin.connectionInfo.title') }}</span>
                      <Colon />
                    </div>
                    <div class="flex-1">
                      <Validate
                        class="flex-1"
                        :text="t('websocketPlugin.connectionInfo.urlFormatError')"
                        mode="error"
                        :error="urlFormatErrorIdSet.has(id)">
                        <Input
                          v-model:value="dataMap[id].url"
                          trimAll
                          :placeholder="t('websocketPlugin.connectionInfo.urlPlaceholder')"
                          :error="urlErrorSet.has(id)"
                          @change="urlChange(id)"
                          @blur="urlBlur(id, $event)" />
                      </Validate>
                      <Tabs size="small" class="w-full mt-2">
                        <template #rightExtra>
                          <FunctionsButton class="text-3.5" />
                        </template>
                        <TabPane key="query">
                          <template #tab>
                            <Badge size="small" :count="queryErrorNumMap.get(id)">
                              <div>{{ t('protocol.requestParameter') }}</div>
                            </Badge>
                          </template>
                          <div class="space-y-2.5">
                            <div
                              v-for="(item, index) in queryParametersMap[id]"
                              :key="item.id"
                              :class="{ 'opacity-70': !item.enabled }"
                              class="flex items-center">
                              <Checkbox v-model:checked="item.enabled" class="flex-shrink-0" />
                              <div class="flex-1 flex items-center space-x-2 mr-3 ml-2">
                                <Input
                                  v-model:value="item.name"
                                  :maxlength="400"
                                  :error="queryParameterNameErrorSet.has(item.id)"
                                  trimAll
                                  style="flex: 1 1 40%;"
                                  :placeholder="t('common.placeholders.enterParameterName')"
                                  @change="queryParameterNameChange(id, item.id, index, $event)" />
                                <ParamInput
                                  :value="item.value"
                                  :maxLength="4096"
                                  class="bg-white"
                                  :placeholder="t('common.placeholders.enterParameterValue')"
                                  style="flex: 1 1 60%"
                                  @blur="queryParameterValueChange(id, item.id, index, $event)" />
                              </div>
                              <div class="w-9 h-7 flex items-center justify-start">
                                <Icon
                                  class="flex-shrink-0 cursor-pointer text-theme-sub-content hover:text-text-link-hover"
                                  icon="icon-shanchuguanbi"
                                  @click="deleteQueryParameter(id, item.id, index)" />
                              </div>
                            </div>
                          </div>
                        </TabPane>
                        <TabPane key="header">
                          <template #tab>
                            <Badge size="small" :count="headerErrorNumMap.get(id)">
                              <div>{{ t('protocol.requestHeader') }}</div>
                            </Badge>
                          </template>
                          <div class="space-y-2.5">
                            <div
                              v-for="(item, index) in headerParametersMap[id]"
                              :key="item.id"
                              :class="{ 'opacity-70': !item.enabled }"
                              class="flex items-center">
                              <Checkbox v-model:checked="item.enabled" class="flex-shrink-0" />
                              <div class="flex-1 flex items-center space-x-2 mr-3 ml-2">
                                <Input
                                  v-model:value="item.name"
                                  :maxlength="400"
                                  :error="headerParameterNameErrorSet.has(item.id)"
                                  trimAll
                                  style="flex: 1 1 40%;"
                                  placeholder="参数名称，最大支持400个字符"
                                  @change="headerParameterNameChange(id, item.id, index, $event)" />
                                <ParamInput
                                  :value="item.value"
                                  :maxLength="4096"
                                  class="bg-white"
                                  placeholder="参数值，最大支持4096个字符"
                                  style="flex: 1 1 60%"
                                  @blur="headerParameterValueChange(id, item.id, index, $event)" />
                                <!-- <Input
                                  v-model:value="item.value"
                                  :maxlength="4096"
                                  trim
                                  placeholder="参数值，最大支持4096个字符"
                                  style="flex: 1 1 60%" /> -->
                              </div>
                              <div class="w-9 h-7 flex items-center justify-start">
                                <Icon
                                  class="flex-shrink-0 cursor-pointer text-theme-sub-content hover:text-text-link-hover"
                                  icon="icon-shanchuguanbi"
                                  @click="deleteHeaderParameter(id, item.id, index)" />
                              </div>
                            </div>
                          </div>
                        </TabPane>
                      </Tabs>
                    </div>
                  </div>

                  <div class="w-full flex items-start">
                    <div class="w-21.5 transform-gpu translate-y-1 flex-shrink-0 text-theme-title">
                      <IconRequired />
                      <span>{{ t('websocketPlugin.messageMode.title') }}</span>
                      <Colon />
                    </div>
                    <SelectEnum
                      v-model:value="dataMap[id].mode"
                      :error="modeErrorIdSet.has(id)"
                      :placeholder="t('websocketPlugin.messageMode.placeholder')"
                      class="w-40 flex-shrink-0"
                      enumKey="WebSocketMessageMode"
                      @change="modeChange(id, $event)" />
                  </div>

                  <template v-if="['ONLY_SEND', 'SEND_AND_RECEIVE'].includes(dataMap[id].mode)">
                    <div class="w-full flex items-start">
                      <div class="w-21.5 transform-gpu translate-y-1 flex-shrink-0 text-theme-title">
                        <span>{{ t('websocketPlugin.sendData.encodingTitle') }}</span>
                        <Colon />
                      </div>
                      <Select
                        v-model:value="dataMap[id].messageEncoding"
                        :options="codeOptions"
                        :placeholder="t('websocketPlugin.sendData.encodingPlaceholder')"
                        class="w-40 flex-shrink-0" />
                    </div>

                    <div class="w-full flex items-start">
                      <div class="w-21.5 transform-gpu translate-y-1 flex-shrink-0 text-theme-title">
                        <IconRequired />
                        <span>{{ t('websocketPlugin.sendData.title') }}</span>
                        <Colon />
                      </div>
                      <Input
                        v-model:value="dataMap[id].message"
                        type="textarea"
                        trim
                        class="flex-1"
                        :error="messageErrorIdSet.has(id)"
                        :autoSize="autoSize"
                        @change="messageChange(id)" />
                    </div>
                  </template>

                  <div class="w-full flex items-start">
                    <div
                      class="w-21.5 flex items-center justify-start space-x-1 flex-shrink-0 text-theme-title">
                      <span>{{ t('websocketPlugin.parametric.title') }}</span>
                      <Colon />
                    </div>
                    <div class="flex-1 space-y-2.5">
                      <Parametric
                        :ref="el => parametricRefsMap[id] = el"
                        v-model:actionOnEOF="dataMap[id].actionOnEOF"
                        v-model:sharingMode="dataMap[id].sharingMode"
                        v-model:datasets="dataMap[id].datasets" />
                    </div>
                  </div>

                  <div class="w-full flex items-start">
                    <div
                      class="w-21.5 flex items-center justify-start transform-gpu translate-y-1 space-x-1 flex-shrink-0 text-theme-title">
                      <span>
                        <span>{{ t('websocketPlugin.assertion.title') }}</span>
                        <Colon />
                      </span>
                      <Tooltip :title="t('websocketPlugin.assertion.tooltip')">
                        <Icon icon="icon-tishi1" class="tip-icon text-3.5 cursor-pointer" />
                      </Tooltip>
                    </div>
                    <div class="flex-1 space-y-2.5">
                      <AssertionForm :ref="el => assertRefsMap[id] = el" :value="dataMap[id].assertions" />
                    </div>
                  </div>
                </div>
              </CollapsePanel>
            </Collapse>
          </li>
        </template>
      </Draggable>
    </template>
    <AsyncComponent :visible="apiModalVisible">
      <SelectApiModal
        v-model:visible="apiModalVisible"
        :linkIds="linkApiIds"
        @ok="selectApiOk" />
    </AsyncComponent>
  </div>
</template>

<style scoped>
.tip-icon {
  color: #b3d7ff;
}

.ant-collapse {
  line-height: 20px;
}

.ant-collapse-borderless> :deep(.ant-collapse-item) {
  border: none;
  border-radius: 4px;
}

.ant-collapse> :deep(.ant-collapse-item)>.ant-collapse-header {
  align-items: center;
  height: 46px;
  padding: 0 12px 0 38px;
  border-radius: 4px;
  background-color: rgba(239, 240, 243, 100%);
  line-height: 20px;
}

.ant-collapse :deep(.ant-collapse-content)>.ant-collapse-content-box {
  padding: 20px;
}

.ant-badge {
  color: inherit;
}

:deep(.ant-badge-count) {
  top: -2px;
  right: -5px;
}

:deep(.ant-checkbox:not(.ant-checkbox-disabled)) {
  background-color: #fff;
}

:deep(.ant-input-affix-wrapper:not(.ant-input-affix-wrapper-disabled)),
:deep(.ant-input),
.ant-select:not(.ant-select-disabled) :deep(.ant-select-selector) {
  background-color: #fff;
}

:deep(.ant-tabs-tab) .ant-tabs-tab-btn {
  line-height: 20px;
}

:deep(.ant-tabs-top)>.ant-tabs-nav {
  margin-bottom: 14px;
}
</style>
