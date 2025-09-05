<script setup lang="ts">
import { nextTick, ref, onMounted, watch, computed } from 'vue';
import { Button, Collapse, CollapsePanel, Switch, Checkbox } from 'ant-design-vue';
import Draggable from 'vuedraggable';
import { debounce } from 'throttle-debounce';
import { Icon, NoData, Input, Colon, Arrow, IconRequired, Tooltip, SelectInput, Select, ShortDuration } from '@xcan-angus/vue-ui';
import { utils, duration } from '@xcan-angus/infra';
import { cloneDeep } from 'lodash-es';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

import { PipelineConfig } from '../PropsType';

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

const DATA_MAX_LENGTH = 2097152;

const domId = utils.uuid();
const rendered = ref(false);
const activeKeys = ref<string[]>([]);

const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: PipelineConfig }>({});

const emptyNameSet = ref<Set<string>>(new Set());
const repeatNameIdSet = ref<Set<string>>(new Set());
const serverErrorSet = ref<Set<string>>(new Set());
const portErrorSet = ref<Set<string>>(new Set());
const tcpClientImplClassErrorSet = ref<Set<string>>(new Set());
const dataErrorSet = ref<Set<string>>(new Set());

const insertData = () => {
  const hasEnabled = Object.values(dataMap.value).find(item => item.enabled === true);
  const id = utils.uuid();
  dataMap.value[id] = {
    id,
    target: 'TCP',
    name: '',
    description: '',
    enabled: !hasEnabled,
    beforeName: '',
    data: '',
    dataEncoding: 'none',
    setting: {
      tcpClientImplClass: '',
      reUseConnection: true,
      closeConnection: false,
      soLinger: null,
      tcpNoDelay: false,
      tcpCharset: 'UTF-8',
      eolByte: '',
      eomByte: '',
      binaryPrefixLength: ''
    },
    server: {
      server: '',
      port: '',
      connectTimeout: '6s', // 1s ～ 86400s
      responseTimeout: '60s'// 1s ～ 86400s
    },
    variables: null
  };

  idList.value.push(id);

  if (!hasEnabled) {
    activeKeys.value.push(id);
  }

  scrollToBottom();
};

const checkedRepeatName = (id?: string) => {
  if (id) {
    emptyNameSet.value.delete(id);
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
      const cloneData: PipelineConfig = cloneDeep(dataMap.value[targetId]);
      dataMap.value[cloneId] = cloneData;
      dataMap.value[cloneId].name = cloneData.name ? (cloneData.name + ' copy') : '';
      dataMap.value[cloneId].enabled = false;
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

      idList.value.splice(i, 1);
      delete dataMap.value[id];

      emptyNameSet.value.delete(id);
      repeatNameIdSet.value.delete(id);
      serverErrorSet.value.delete(id);
      portErrorSet.value.delete(id);
      tcpClientImplClassErrorSet.value.delete(id);
      dataErrorSet.value.delete(id);

      break;
    }
  }

  checkedRepeatName(targetId);
};

const serverChange = (id: string) => {
  serverErrorSet.value.delete(id);
};

const portChange = (id: string) => {
  portErrorSet.value.delete(id);
};

const tcpClientImplClassChange = (id: string, value:string) => {
  tcpClientImplClassErrorSet.value.delete(id);
  const text = value.replace(/\(.+\)/g, '').replace(/\s/g, '');
  if (text === 'TcpClientImpl') {
    dataMap.value[id].setting.eolByte = '10';
    dataMap.value[id].setting.eomByte = '';
    dataMap.value[id].setting.binaryPrefixLength = '';
    return;
  }

  if (text === 'BinaryTcpClientImpl') {
    dataMap.value[id].setting.eomByte = '13';
    dataMap.value[id].setting.eolByte = '';
    dataMap.value[id].setting.binaryPrefixLength = '';
    return;
  }

  if (text === 'LengthPrefixedBinaryTcpClientImpl') {
    dataMap.value[id].setting.binaryPrefixLength = '2';
    dataMap.value[id].setting.eomByte = '';
    dataMap.value[id].setting.eolByte = '';
  }
};

const dataChange = debounce(duration.delay, (id: string, event:{target:{value:string}}) => {
  const value = event.target.value;
  if (value.length > DATA_MAX_LENGTH) {
    dataErrorSet.value.add(id);
  } else {
    dataErrorSet.value.delete(id);
  }
});

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

const reset = () => {
  activeKeys.value = [];
  idList.value = [];
  dataMap.value = {};

  emptyNameSet.value.clear();
  repeatNameIdSet.value.clear();
  serverErrorSet.value.clear();
  portErrorSet.value.clear();
  tcpClientImplClassErrorSet.value.clear();
  dataErrorSet.value.clear();
};

onMounted(() => {
  watch(() => props.value, (newValue) => {
    reset();

    if (!newValue?.length) {
      return;
    }

    const list = cloneDeep(newValue) as PipelineConfig[];
    for (let i = 0, len = list.length; i < len; i++) {
      const data = list[i];
      const { id, server, setting, enabled } = data;
      dataMap.value[id] = data;
      if (!server) {
        dataMap.value[id].server = {
          server: '',
          port: '',
          connectTimeout: '6s', // 1s ～ 86400s
          responseTimeout: '60s'// 1s ～ 86400s
        };
      }

      if (!setting) {
        dataMap.value[id].setting = {
          tcpClientImplClass: '',
          reUseConnection: true,
          closeConnection: false,
          soLinger: null,
          tcpNoDelay: false,
          tcpCharset: 'UTF-8',
          eolByte: '',
          eomByte: '',
          binaryPrefixLength: ''
        };
      } else {
        if (setting.tcpClientImplClass === 'TcpClientImpl') {
          setting.tcpClientImplClass = 'TcpClientImpl (EOL)';
        } else if (setting.tcpClientImplClass === 'BinaryTcpClientImpl') {
          setting.tcpClientImplClass = 'BinaryTcpClientImpl (EOM)';
        }
      }

      if (enabled === true) {
        activeKeys.value.push(id);
      }

      idList.value.push(id);
    }
  }, { immediate: true });

  emit('renderChange', true);
  rendered.value = true;
});

const nameErrorSet = computed(() => {
  return new Set<string>([...emptyNameSet.value, ...repeatNameIdSet.value]);
});

const isValid = (): boolean => {
  emptyNameSet.value.clear();
  serverErrorSet.value.clear();
  portErrorSet.value.clear();
  tcpClientImplClassErrorSet.value.clear();
  dataErrorSet.value.clear();

  const ids = idList.value;
  const map = dataMap.value;
  const set = repeatNameIdSet.value;
  let errorNum = 0;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const { name, server, setting, data } = map[id];
    if (!name) {
      emptyNameSet.value.add(id);
      errorNum++;
    } else {
      if (set.has(id)) {
        errorNum++;
      }
    }

    if (server) {
      const { server: _server, port } = server;
      if (!_server) {
        serverErrorSet.value.add(id);
        errorNum++;
      }

      if (!port) {
        portErrorSet.value.add(id);
        errorNum++;
      }
    } else {
      serverErrorSet.value.add(id);
      portErrorSet.value.add(id);
      errorNum++;
    }

    if (!setting || !setting.tcpClientImplClass) {
      tcpClientImplClassErrorSet.value.add(id);
      errorNum++;
    }

    if (data && data.length > DATA_MAX_LENGTH) {
      dataErrorSet.value.add(id);
      errorNum++;
    }
  }

  return !errorNum;
};

const getData = (): PipelineConfig[] => {
  const result: PipelineConfig[] = [];
  const map = dataMap.value;
  const ids = idList.value;
  let beforeName = '';
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const data = cloneDeep(map[id]) as PipelineConfig;
    data.setting.tcpClientImplClass = data.setting.tcpClientImplClass.replace(/\(.+\)/g, '').replace(/\s/g, '');
    result.push({ ...data, beforeName });
    beforeName = data.name;
  }

  return result;
};

defineExpose({
  isValid,
  getData
});

const tcpClientClassOptions = [
  { label: 'TcpClientImpl (EOL)', value: 'TcpClientImpl (EOL)' },
  { label: 'BinaryTcpClientImpl (EOM)', value: 'BinaryTcpClientImpl (EOM)' },
  { label: 'LengthPrefixedBinaryTcpClientImpl', value: 'LengthPrefixedBinaryTcpClientImpl' }
];

const codeOptions = [
  { label: t('tcpPlugin.uiConfig.form.none'), value: 'none' },
  { label: 'base64', value: 'base64' },
  { label: 'gzip_base64', value: 'gzip_base64' }
];

const prefixLengthOptions = [
  { label: '2', value: '2' },
  { label: '4', value: '4' }
];

const autosize = {
  minRows: 6,
  maxRows: 6
};

const connectTimeoutInputProps = {
  maxlength: 8,
  dataType: 'integer',
  placeholder: t('tcpPlugin.uiConfig.serverConfig.connectionTimeoutPlaceholder')
};

const readTimeoutInputProps = {
  maxlength: 8,
  dataType: 'integer',
  placeholder: t('tcpPlugin.uiConfig.serverConfig.responseTimeoutPlaceholder')
};

const selectProps = {
  excludes: () => { return false; },
  style: 'width:65px;'
};
</script>

<template>
  <div class="py-5 h-full text-3 leading-5">
    <div class="flex items-center flex-nowrap whitespace-nowrap space-x-2 px-5">
      <Button
        type="default"
        size="small"
        @click="insertData">
        <div class="flex items-center">
          <Icon icon="icon-chajianpeizhi" class="mr-1" />
          <span>{{ t('tcpPlugin.uiConfig.title') }}</span>
        </div>
      </Button>
      <div class="flex-1 flex items-center overflow-hidden" :title="t('tcpPlugin.uiConfig.description')">
        <Icon
          icon="icon-tishi1"
          class="flex-shrink-0 text-3.5 mr-0.5"
          style="color:#a6ceff;" />
        <span class="text-theme-sub-content truncate">{{ t('tcpPlugin.uiConfig.description') }}</span>
      </div>
    </div>
    <template v-if="props.loaded">
      <Draggable
        v-if="!!idList.length"
        :id="domId"
        :list="idList"
        :animation="300"
        group="scene"
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
                style="background-color: rgba(251, 251, 251, 100%);"
                collapsible="disabled">
                <template #header>
                  <div class="flex items-center flex-nowrap w-full whitespace-nowrap">
                    <Icon class="flex-shrink-0 text-4 mr-3" icon="icon-chajianpeizhi" />
                    <div class="flex-1 flex items-center space-x-2 mr-5">
                      <Tooltip
                        :title="t('tcpPlugin.uiConfig.form.nameDuplicate')"
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
                          :placeholder="t('tcpPlugin.uiConfig.form.namePlaceholder')"
                          @change="nameChange(id)" />
                      </Tooltip>
                      <Input
                        v-model:value="dataMap[id].description"
                        :maxlength="800"
                        :title="dataMap[id].description"
                        trim
                        style="flex:1 1 60%;"
                        :placeholder="t('tcpPlugin.uiConfig.form.descriptionPlaceholder')" />
                    </div>
                    <div class="flex items-center flex-shrink-0 space-x-3">
                      <Switch
                        :checked="dataMap[id].enabled"
                        size="small"
                        @change="enabledChange(id, $event)" />
                      <div class="flex items-center cursor-pointer hover:text-text-link-hover" :title="t('tcpPlugin.uiConfig.actions.clone')">
                        <Icon
                          icon="icon-fuzhi"
                          class="text-3.5"
                          @click="toClone(id)" />
                      </div>
                      <div class="flex items-center cursor-pointer hover:text-text-link-hover" :title="t('tcpPlugin.uiConfig.actions.delete')">
                        <Icon
                          icon="icon-qingchu"
                          class="text-3.5"
                          @click="toDelete(id)" />
                      </div>
                      <Arrow :open="activeKeys.includes(id)" @change="openChange(id, $event)" />
                    </div>
                  </div>
                </template>

                <div class="space-y-3.5">
                  <div class="flex items-center text-theme-title">
                    <div class="w-1.25 h-3 rounded mr-1.5" style="background-color: #1e88e5;"></div>
                    <span>{{ t('tcpPlugin.uiConfig.serverConfig.title') }}</span>
                  </div>
                  <div class="space-y-3.5 ml-2.75">
                    <div class="flex items-center">
                      <div class="flex-shrink-0 w-26.5 flex items-center">
                        <IconRequired />
                        <span>{{ t('tcpPlugin.uiConfig.serverConfig.hostnameOrIP') }}</span>
                        <Colon />
                      </div>
                      <div class="flex-1 flex items-center space-x-5 max-w-175">
                        <Input
                          v-model:value="dataMap[id].server.server"
                          :maxlength="4096"
                          :error="!!serverErrorSet.has(id)"
                          trimAll
                          style="flex: 1 1 75%;"
                          :placeholder="t('tcpPlugin.uiConfig.serverConfig.hostnameOrIPPlaceholder')"
                          @change="serverChange(id)" />

                        <div style="flex: 1 1 25%;" class="flex items-center">
                          <div class="flex-shrink-0 w-11.5 flex items-center">
                            <IconRequired />
                            <span>{{ t('tcpPlugin.uiConfig.serverConfig.port') }}</span>
                            <Colon />
                          </div>
                          <Input
                            v-model:value="dataMap[id].server.port"
                            :maxlength="5"
                            :min="1"
                            :max="65535"
                            :error="!!portErrorSet.has(id)"
                            trimALl
                            dataType="integer"
                            style="min-width: 75px;max-width: 200px;"
                            :placeholder="t('tcpPlugin.uiConfig.serverConfig.portPlaceholder')"
                            @change="portChange(id)" />
                        </div>
                      </div>
                    </div>

                    <div class="flex items-center">
                      <div class="flex-shrink-0 w-26.5 flex items-center">
                        <span>{{ t('tcpPlugin.uiConfig.serverConfig.connectionTimeout') }}</span>
                        <Colon />
                      </div>
                      <div class="flex-1 flex items-center space-x-5 max-w-175">
                        <ShortDuration
                          v-model:value="dataMap[id].server.connectTimeout"
                          :max="86400"
                          :inputProps="connectTimeoutInputProps"
                          :selectProps="selectProps"
                          style="flex:none;width:calc((100% - 82px)/2);background-color: #fff;" />
                        <div class="flex-1 flex items-center">
                          <div class="flex-shrink-0 w-15.5 flex items-center">
                            <span>{{ t('tcpPlugin.uiConfig.serverConfig.responseTimeout') }}</span>
                            <Colon />
                          </div>
                          <ShortDuration
                            v-model:value="dataMap[id].server.responseTimeout"
                            :max="86400"
                            :inputProps="readTimeoutInputProps"
                            :selectProps="selectProps"
                            style="background-color: #fff;" />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="space-y-3.5 mt-5">
                  <div class="flex items-center text-theme-title">
                    <div class="w-1.25 h-3 rounded mr-1.5" style="background-color: #1e88e5;"></div>
                    <span>{{ t('tcpPlugin.uiConfig.requestConfig.title') }}</span>
                  </div>
                  <div class="space-y-3 ml-2.75">
                    <div class="flex items-center">
                      <div class="flex-shrink-0 w-26.5 flex items-center">
                        <IconRequired />
                        <span>{{ t('tcpPlugin.uiConfig.requestConfig.tcpClientClassName') }}</span>
                        <Colon />
                      </div>
                      <SelectInput
                        v-model:value="dataMap[id].setting.tcpClientImplClass"
                        :error="!!tcpClientImplClassErrorSet.has(id)"
                        :maxlength="4096"
                        :options="tcpClientClassOptions"
                        trimAll
                        :placeholder="t('tcpPlugin.uiConfig.requestConfig.tcpClientClassNamePlaceholder')"
                        class="flex-1 max-w-175 bg-white"
                        @change="tcpClientImplClassChange(id,$event)" />
                    </div>

                    <div class="flex items-center">
                      <div class="flex-shrink-0 w-26.5 flex items-center">
                        <span>{{ t('tcpPlugin.uiConfig.requestConfig.socketConnectionConfig') }}</span>
                        <Colon />
                      </div>
                      <div class="flex-1 flex max-w-175 flex-wrap space-x-5">
                        <Checkbox
                          v-model:checked="dataMap[id].setting.reUseConnection"
                          class="flex-shrink-0"
                          style="height:28px;line-height:28px;">
                          {{ t('tcpPlugin.uiConfig.requestConfig.reuseConnection') }}
                        </Checkbox>

                        <Checkbox
                          v-model:checked="dataMap[id].setting.tcpNoDelay"
                          class="flex-shrink-0"
                          style="height:28px;line-height:28px;">
                          {{ t('tcpPlugin.uiConfig.requestConfig.setNoDelay') }}
                        </Checkbox>

                        <div class="flex-shrink-0 flex items-center space-x-1.5">
                          <div class="flex-shrink-0 flex items-center">
                            <span>SO_LINGER</span>
                          </div>
                          <Input
                            v-model:value="dataMap[id].setting.soLinger"
                            :maxlength="8"
                            :min="0"
                            :max="86400000"
                            dataType="integer"
                            suffix="ms"
                            style="width: 120px;"
                            placeholder="1 ~ 86400000" />
                        </div>
                      </div>
                    </div>

                    <div class="flex items-center">
                      <div class="flex-shrink-0 w-26.5 flex items-center">
                        <span>{{ t('tcpPlugin.uiConfig.requestConfig.messageDataEncoding') }}</span>
                        <Colon />
                      </div>
                      <Select
                        v-model:value="dataMap[id].dataEncoding"
                        :options="codeOptions"
                        :placeholder="t('tcpPlugin.uiConfig.requestConfig.messageDataEncodingPlaceholder')"
                        style="width: 162px;"
                        class="bg-white" />
                    </div>

                    <div class="flex items-start">
                      <div class="leading-7 flex-shrink-0 w-26.5 flex items-center">
                        <span>{{ t('tcpPlugin.uiConfig.requestConfig.messageData') }}</span>
                        <Colon />
                      </div>
                      <div class="flex-1 max-w-175">
                        <Input
                          v-model:value="dataMap[id].data"
                          :error="dataErrorSet.has(id)"
                          :autoSize="autosize"
                          trim
                          type="textarea"
                          :placeholder="t('tcpPlugin.uiConfig.requestConfig.messageDataPlaceholder', { maxLength: DATA_MAX_LENGTH })"
                          class="!bg-white"
                          @change="dataChange(id,$event)" />
                        <div v-if="dataErrorSet.has(id)" style="margin-top:3px;color:rgba(245, 34, 45, 100%);">{{ t('tcpPlugin.uiConfig.requestConfig.messageDataError', { maxLength: DATA_MAX_LENGTH, currentLength: dataMap[id].data.length }) }}</div>
                      </div>
                    </div>

                    <div class="flex items-center">
                      <div class="flex-shrink-0 w-26.5"></div>
                      <div class="flex-1 flex max-w-175 flex-wrap">
                        <div class="flex-shrink-0 flex items-center space-x-1.5 mr-5 mb-3">
                          <div class="flex-shrink-0 flex items-center">
                            <span>{{ t('tcpPlugin.uiConfig.requestConfig.charset') }}</span>
                          </div>
                          <Input
                            v-model:value="dataMap[id].setting.tcpCharset"
                            :maxlength="100"
                            trimAll
                            style="width: 120px;" />
                        </div>

                        <div class="flex-shrink-0 flex items-center space-x-1.5 mr-5 mb-3">
                          <div class="flex-shrink-0 flex items-center">
                            <span>{{ t('tcpPlugin.uiConfig.requestConfig.eolByteValue') }}</span>
                          </div>
                          <Input
                            v-model:value="dataMap[id].setting.eolByte"
                            :maxlength="4"
                            :min="-128"
                            :max="127"
                            dataType="integer"
                            style="width: 50px;" />
                        </div>

                        <div class="flex-shrink-0 flex items-center space-x-1.5 mr-5 mb-3">
                          <div class="flex-shrink-0 flex items-center">
                            <span>{{ t('tcpPlugin.uiConfig.requestConfig.eomByteValue') }}</span>
                          </div>
                          <Input
                            v-model:value="dataMap[id].setting.eomByte"
                            :maxlength="4"
                            :min="-128"
                            :max="127"
                            dataType="integer"
                            style="width: 50px;" />
                        </div>

                        <div class="flex-shrink-0 flex items-center space-x-1.5 mb-3">
                          <div class="flex-shrink-0 flex items-center">
                            <span>{{ t('tcpPlugin.uiConfig.requestConfig.binaryDataLengthPrefix') }}</span>
                          </div>
                          <Select
                            v-model:value="dataMap[id].setting.binaryPrefixLength"
                            allowClear
                            :options="prefixLengthOptions"
                            style="width: 50px;"
                            class="flex-1 bg-white" />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </CollapsePanel>
            </Collapse>
          </li>
        </template>
      </Draggable>

      <NoData v-else style="height: calc(100% - 40px);" />
    </template>
  </div>
</template>

<style scoped>
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
  padding: 14px 20px 20px;
  font-size: 12px;
}

:deep(.ant-radio):not(.ant-radio-checked) .ant-radio-inner,
.ant-input-affix-wrapper:not(.ant-input-affix-wrapper-disabled) {
  background-color: #fff;
}

:deep(.ant-input-prefix),
:deep(.ant-input-suffix) {
  color: var(--content-text-sub-content);
}

:deep(.ant-checkbox):not(.ant-checkbox-checked) .ant-checkbox-inner {
  background-color: #fff;
}
</style>
