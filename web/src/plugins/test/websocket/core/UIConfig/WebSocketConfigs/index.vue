<script setup lang="ts">
import { computed, defineAsyncComponent, ref, onMounted, watch, watchEffect } from 'vue';
import { useI18n } from 'vue-i18n';
import { Badge, Collapse, CollapsePanel, Tabs, TabPane, Switch } from 'ant-design-vue';
import { Icon, Input, SelectEnum, Tooltip, Validate, Select, Arrow } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { ParameterConfig, WebsocketConfig, WebSocketMode } from './PropsType';
import { AssertionConfig } from '@/plugins/test/components/UIConfigComp/AssertionForm/PropsType';

const { t } = useI18n();

export interface Props {
  value: WebsocketConfig;
  enabled: boolean;
  repeatNames: string[];
  errorNum:number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  enabled: false,
  repeatNames: () => [],
  errorNum: 0
});


const emit = defineEmits<{
  (e: 'click', value: 'delete' | 'clone'): void;
  (e: 'enabledChange', value: boolean): void;
  (e: 'nameChange', value: string): void;
  (e: 'update:errorNum', value: number): void;
}>();

const ParameterInput = defineAsyncComponent(() => import('./ParameterInput.vue'));
const AssertionForm = defineAsyncComponent(() => import('@/plugins/test/components/UIConfigComp/AssertionForm/index.vue'));
const ServerUri = defineAsyncComponent(() => import('./ServerUri/index.vue'));

const queryRef = ref();
const headerRef = ref();
const assertRef = ref();

const UUID = utils.uuid();
const activeKey = ref<string>(UUID);

const enabled = ref(false);
const apisId = ref<string>();
const name = ref<string>();
const description = ref<string>();
const url = ref<string>();
const server = ref<WebsocketConfig['server']>();
const endpoint = ref<string>();
const mode = ref<WebSocketMode>('ONLY_SEND');
const message = ref<string>();
const messageEncoding = ref<WebsocketConfig['messageEncoding']>('none');
const queryParameters = ref<ParameterConfig[]>([]);
const headerParameters = ref<ParameterConfig[]>([]);
const assertionList = ref<AssertionConfig[]>([]);

const errorNum = ref(0);
const nameError = ref(false);
const urlError = ref(false);
const urlErrorMessage = ref<string>();
const modeError = ref(false);
const messageError = ref(false);
const queryErrorNum = ref(0);
const headerErrorNum = ref(0);
const nameRepeatFlag = ref(false);

const nameChange = (event: { target: { value: string } }) => {
  if (nameError.value) {
    errorNum.value--;
    nameError.value = false;
  }
  const value = event.target.value;
  name.value = value;
  nameRepeatFlag.value = false;
  emit('nameChange', value);
};

const serverChange = (data:WebsocketConfig['server']) => {
  server.value = data;
};

const urlChange = (event: { target: { value: string } }) => {
  if (urlError.value) {
    errorNum.value--;
    urlError.value = false;
    urlErrorMessage.value = undefined;
  }
  url.value = event.target.value;
};

const modeChange = (value: WebSocketMode) => {
  if (modeError.value) {
    errorNum.value--;
    modeError.value = false;
  }

  if (value === 'ONLY_RECEIVE') {
    message.value = '';
  }
};

const messageChange = (event: { target: { value: string } }) => {
  if (messageError.value) {
    errorNum.value--;
    messageError.value = false;
  }
  message.value = event.target.value;
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

const reset = () => {
  apisId.value = undefined;
  name.value = undefined;
  description.value = undefined;
  url.value = undefined;
  server.value = undefined;
  endpoint.value = undefined;
  mode.value = 'ONLY_SEND';
  message.value = '';
  messageEncoding.value = 'none';
  queryParameters.value = [];
  headerParameters.value = [];
  assertionList.value = [];

  errorNum.value = 0;
  nameError.value = false;
  urlError.value = false;
  urlErrorMessage.value = undefined;
  modeError.value = false;
  messageError.value = false;
  queryErrorNum.value = 0;
  headerErrorNum.value = 0;
  nameRepeatFlag.value = false;
};

onMounted(() => {
  watch(() => props.enabled, (newValue) => {
    enabled.value = newValue;
  }, { immediate: true });

  watch(() => props.value, (newValue) => {
    reset();
    if (!newValue) {
      return;
    }

    const {
      assertions = [],
      apisId: _apisId,
      name: _name,
      description: _description,
      url: _url,
      mode: _mode,
      message: _message,
      messageEncoding: _messageEncoding,
      parameters,
      server: _server,
      endpoint: _endpoint
    } = JSON.parse(JSON.stringify(newValue));
    name.value = _name;
    description.value = _description;
    apisId.value = _apisId;
    url.value = _url;
    mode.value = _mode;
    message.value = _message;
    messageEncoding.value = _messageEncoding;
    server.value = _server;
    endpoint.value = _endpoint;
    assertionList.value = assertions;
    queryParameters.value = parameters?.filter(item => item.in === 'query') || [];
    headerParameters.value = parameters?.filter(item => item.in === 'header') || [];
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
    emit('update:errorNum', errorNum.value);
  });
});

const getInvalidNum = (): number => {
  errorNum.value = 0;
  nameError.value = false;
  urlError.value = false;
  urlErrorMessage.value = undefined;
  modeError.value = false;
  queryErrorNum.value = 0;
  headerErrorNum.value = 0;
  nameRepeatFlag.value = false;

  if (!name.value) {
    errorNum.value++;
    nameError.value = true;
  } else {
    if (props.repeatNames.includes(name.value)) {
      nameError.value = true;
      nameRepeatFlag.value = true;
    }
  }

  if (!server.value) {
    if (!url.value) {
      errorNum.value++;
      urlError.value = true;
    } else {
      if (!/^ws:\/\/|wss:\/\//.test(url.value)) {
        errorNum.value++;
        urlError.value = true;
        urlErrorMessage.value = t('websocketPlugin.uiConfig.websocketConfigs.urlFormatError');
      }
    }
  }

  if (!mode.value) {
    errorNum.value++;
    modeError.value = true;
  }

  if (showSendMessage.value && !message.value) {
    errorNum.value++;
    messageError.value = true;
  }

  if (typeof queryRef.value?.getInvalidNum === 'function') {
    queryErrorNum.value = queryRef.value.getInvalidNum();
    if (queryErrorNum.value) {
      errorNum.value++;
    }
  }

  if (typeof headerRef.value?.getInvalidNum === 'function') {
    headerErrorNum.value = headerRef.value.getInvalidNum();
    if (headerErrorNum.value) {
      errorNum.value++;
    }
  }

  if (typeof assertRef.value?.isValid === 'function') {
    const validFlag = assertRef.value.isValid();
    if (validFlag) {
      errorNum.value++;
    }
  }

  return errorNum.value;
};

const getData = (): Omit<WebsocketConfig, 'id'> => {
  let assertions = JSON.parse(JSON.stringify(assertionList.value));
  if (typeof assertRef.value?.getData === 'function') {
    assertions = assertRef.value.getData();
  }

  let _queryParameters = JSON.parse(JSON.stringify(queryParameters.value));
  if (typeof queryRef.value?.getData === 'function') {
    _queryParameters = queryRef.value.getData();
  }

  let _headerParameters = JSON.parse(JSON.stringify(headerParameters.value));
  if (typeof headerRef.value?.getData === 'function') {
    _headerParameters = headerRef.value.getData();
  }

  const parameters = JSON.parse(JSON.stringify([..._queryParameters, ..._headerParameters]));
  return {
    beforeName: undefined,
    assertions,
    parameters,
    target: 'WEBSOCKET',
    apisId: apisId.value,
    description: description.value,
    enabled: enabled.value,
    name: name.value,
    mode: mode.value,
    url: url.value,
    messageEncoding: messageEncoding.value,
    server: server.value,
    endpoint: endpoint.value,
    message: message.value
  };
};

defineExpose({
  getData,
  getInvalidNum,
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

const showSendMessage = computed(() => {
  if (!mode.value) {
    return false;
  }

  return ['ONLY_SEND', 'SEND_AND_RECEIVE', 'SEND_TO_RECEIVE', 'RECEIVE_TO_SEND'].includes(mode.value);
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
  <Collapse
    :activeKey="activeKey"
    :class="{'opacity-70':!enabled}"
    class="websocket-collapse-container"
    :bordered="false">
    <CollapsePanel
      :key="UUID"
      :showArrow="false"
      style="background-color: rgba(247, 248, 251, 100%);"
      collapsible="disabled">
      <template #header>
        <div class="flex items-center flex-nowrap w-full whitespace-nowrap">
          <Icon
            v-if="apisId"
            icon="icon-yinyongWS"
            class="flex-shrink-0 text-4 mr-3" />
          <Icon
            v-else
            icon="icon-WS"
            class="flex-shrink-0 text-4 mr-3" />
          <div class="flex-1 flex items-center space-x-2 mr-3">
            <Tooltip
              :title="t('websocketPlugin.uiConfig.websocketConfigs.form.nameDuplicate')"
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
                class="websocket-name-input"
                :placeholder="t('common.placeholders.searchKeyword')"
                @change="nameChange" />
            </Tooltip>
            <Input
              v-model:value="description"
              :maxlength="800"
              :title="description"
              trim
              style="flex: 1 1 calc((100% - (8px))*3/5);"
              :placeholder="t('websocketPlugin.uiConfig.websocketConfigs.form.descriptionPlaceholder')" />
          </div>
          <div class="flex items-center flex-shrink-0 space-x-3">
            <Switch
              :checked="enabled"
              size="small"
              @change="enabledChange" />
            <div class="flex items-center cursor-pointer hover:text-text-link-hover" :title="t('actions.clone')">
              <Icon
                icon="icon-fuzhi"
                class="text-3.5"
                @click="actionClick('clone')" />
            </div>
            <div class="flex items-center cursor-pointer hover:text-text-link-hover" :title="t('actions.delete')">
              <Icon
                icon="icon-qingchu"
                class="text-3.5"
                @click="actionClick('delete')" />
            </div>
            <Arrow :open="activeKey === UUID" @change="openChange" />
          </div>
        </div>
      </template>
      <div class="flex flex-col items-end leading-5 space-y-8 text-3">
        <div class="w-full flex items-start space-x-2.5">
          <div class="w-12 transform-gpu translate-y-1 flex-shrink-0 text-theme-title">{{ t('websocketPlugin.uiConfig.websocketConfigs.connectionInfo.title') }}</div>
          <div class="flex-1">
            <div class="flex items-start space-x-2 flex-1">
              <Validate class="flex-1" :text="urlErrorMessage">
                <ServerUri
                  v-if="apisId"
                  :server="server"
                  :uri="endpoint"
                  @change="serverChange" />
                <Input
                  v-else
                  v-model:value="url"
                  trimAll
                  :placeholder="t('websocketPlugin.uiConfig.websocketConfigs.connectionInfo.urlPlaceholder')"
                  :error="urlError"
                  @change="urlChange" />
              </Validate>
              <SelectEnum
                v-model:value="mode"
                :error="modeError"
                :placeholder="t('websocketPlugin.uiConfig.websocketConfigs.connectionInfo.messageModePlaceholder')"
                class="w-30 flex-shrink-0"
                enumKey="WebSocketMessageMode"
                @change="modeChange" />
            </div>
            <Tabs
              size="small"
              class="w-full mt-2">
              <TabPane key="query">
                <template #tab>
                  <Badge size="small" :count="queryErrorNum">
                    <div>{{ t('protocol.requestParameter') }}</div>
                  </Badge>
                </template>
                <ParameterInput
                  ref="queryRef"
                  v-model:errorNum="queryErrorNum"
                  type="query"
                  :value="queryParameters" />
              </TabPane>
              <TabPane key="header">
                <template #tab>
                  <Badge size="small" :count="headerErrorNum">
                    <div>{{ t('protocol.requestHeader') }}</div>
                  </Badge>
                </template>
                <ParameterInput
                  ref="headerRef"
                  v-model:errorNum="headerErrorNum"
                  type="header"
                  :value="headerParameters" />
              </TabPane>
            </Tabs>
          </div>
        </div>
        <template v-if="showSendMessage">
          <div class="w-full flex items-start space-x-2.5">
            <div class="w-12 transform-gpu translate-y-1 flex-shrink-0 text-theme-title">{{ t('websocketPlugin.uiConfig.websocketConfigs.sendInfo.title') }}</div>
            <Input
              v-model:value="message"
              type="textarea"
              trim
              class="flex-1"
              :error="messageError"
              :autoSize="autoSize"
              @change="messageChange" />
            <Select
              v-model:value="messageEncoding"
              :options="codeOptions"
              :placeholder="t('websocketPlugin.uiConfig.websocketConfigs.sendInfo.encodingPlaceholder')"
              style="width: 120px;"
              class="bg-white flex-shrink-0" />
          </div>
        </template>
        <div class="w-full flex items-start space-x-2.5">
          <div
            class="w-12 flex items-center justify-start transform-gpu translate-y-1 space-x-1 flex-shrink-0 text-theme-title">
            <span>{{ t('common.assertion') }}</span>
            <Tooltip :title="t('websocketPlugin.uiConfig.websocketConfigs.assertion.tooltip')">
              <Icon icon="icon-tishi1" class="tip-icon text-3.5 cursor-pointer" />
            </Tooltip>
          </div>
          <div class="flex-1 space-y-2.5">
            <AssertionForm ref="assertRef" :value="assertionList" />
          </div>
        </div>
      </div>
    </CollapsePanel>
  </Collapse>
</template>
<style scoped>
.tip-icon {
  color: #b3d7ff;
}

.websocket-name-input {
  flex: 1 1 calc((100% - 8px)*2/5 + 72px);
}

.websocket-name-input :deep(.ant-input-disabled) {
  color: var(--content-text-content);
}

.ant-collapse.websocket-collapse-container {
  line-height: 20px;
}

.ant-collapse-borderless.websocket-collapse-container> :deep(.ant-collapse-item) {
  border: none;
  border-radius: 4px;
}

.ant-collapse.websocket-collapse-container> :deep(.ant-collapse-item)>.ant-collapse-header {
  align-items: center;
  height: 46px;
  padding: 0 12px 0 38px;
  border-radius: 4px;
  background-color: rgba(239, 240, 243, 100%);
  line-height: 20px;
}

.websocket-collapse-container :deep(.ant-collapse-content)>.ant-collapse-content-box {
  padding: 20px;
}

.ant-badge {
  color: inherit;
}

:deep(.ant-badge-count) {
  top: -2px;
  right: -5px;
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
