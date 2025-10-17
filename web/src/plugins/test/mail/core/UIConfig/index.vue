<script setup lang="ts">
import { nextTick, ref, onMounted, watch, computed } from 'vue';
import { Badge, Button, Collapse, CollapsePanel, Switch, Tabs, TabPane, RadioGroup, Radio, Checkbox } from 'ant-design-vue';
import Draggable from 'vuedraggable';
import { debounce } from 'throttle-debounce';
import { Icon, NoData, Input, Tooltip, Colon, Arrow, IconRequired, Hints, ShortDuration } from '@xcan-angus/vue-ui';
import { utils, duration } from '@xcan-angus/infra';

import { PipelineConfig } from '../PropsType';
import Upload from './Upload.vue';

export interface Props {
  value: PipelineConfig[];
  loaded: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  loaded: false
});


const emit = defineEmits<{
  (e: 'errorNumChange', value: number): void;
  (e: 'renderChange', value: boolean): void;
}>();

// const extractNumbers = (str) => {
//   // 使用正则表达式匹配所有数字
//   if (str === undefined) {
//     return '';
//   }
//   const numbers = str.match(/\d+/g);
//   // 输出数字数组或者提示没有找到数字
//   return numbers !== null ? numbers.join('') : '';
// };

const connectTimeoutInputProps = (id) => {
  return {
    maxlength: 8,
    dataType: 'integer',
    placeholder: '连接超时，最大24小时',
    error: validated.value && !dataMap.value[id].server.readTimeout
  };
};

const readTimeoutInputProps = (id) => {
  return {
    maxlength: 8,
    dataType: 'integer',
    placeholder: '读取超时，最大24小时',
    error: validated.value && !dataMap[id].server.connectTimeout
  };
};

const shortTimeSelectProps = {
  excludes: () => { return false; },
  style: 'width:65px;'
};

const certificateOpt = [
  {
    label: '信任所有证书',
    value: true
  },
  {
    label: '使用本地信任库',
    value: false
  }
];

const domId = utils.uuid();
const rendered = ref(false);

const activeKeys = ref<string[]>([]);
const emptyNameSet = ref<Set<string>>(new Set());

const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: PipelineConfig }>({});
const repeatNameSet = ref<Set<string>>(new Set());
const fileMap = ref<{ [key: string]: { url: string; file: File } }>({});

const tabActiveKeyMap = ref<{[key:string]:'mail'|'security'|'other'}>({});
const mailSetErrorNumMap = ref<Map<string, number>>(new Map());

const generateEmptSmtpData = ():PipelineConfig => {
  return {
    target: 'MAIL',
    name: '',
    description: '',
    enabled: !idList.value.length,
    beforeName: '',
    server: {
      server: '',
      port: '',
      readTimeout: '60s',
      connectTimeout: '6s',
      useAuth: false
    },
    username: '',
    password: '',
    use: 'NONE',
    localCertType: 'file',
    // transmissionMode: 'all',
    trustAllCerts: true,
    useLocalTrustStore: false,
    enforceStartTLS: false,
    messageType: 'all',
    numMessages: undefined,
    trustStoreBase64Content: '',
    protocol: 'POP3',
    tlsProtocols: '',
    trustStorePath: '',
    deleteMessage: false,
    storeMimeMessage: false,
    folder: 'INBOX'
  };
};

const insertData = () => {
  validated.value = false;
  const id = utils.uuid();
  dataMap.value[id] = {
    id,
    ...generateEmptSmtpData()
  };
  idList.value.push(id);
  activeKeys.value.push(id);

  scrollToBottom();
};

const nameChange = debounce(duration.search, (id: string, event: { target: { value: string } }) => {
  const value = event.target.value;
  dataMap.value[id].name = value;

  repeatNameSet.value.clear();
  const uniqueNames = new Set();
  const map = dataMap.value;
  for (const key in map) {
    const name = map[key].name;
    if (name) {
      if (uniqueNames.has(name)) {
        repeatNameSet.value.add(name);
      } else {
        uniqueNames.add(name);
      }
    }
  }
});

const openChange = (id: string, _open: boolean) => {
  if (_open) {
    activeKeys.value.push(id);
    return;
  }

  activeKeys.value = activeKeys.value.filter(item => item !== id);
};

const enabledChange = (id: string, enabled: boolean) => {
  if (idList.value.length < 2) {
    return;
  }
  if (enabled) {
    for (const i of idList.value) {
      dataMap.value[i].enabled = false;
    }
    dataMap.value[id].enabled = enabled;
  } else {
    const TargetIdx = idList.value.findIndex(i => i === id);
    if (idList.value[TargetIdx - 1]) {
      dataMap.value[idList.value[TargetIdx - 1]].enabled = true;
    } else if (idList.value[TargetIdx + 1]) {
      dataMap.value[idList.value[TargetIdx + 1]].enabled = true;
    }
  }
};

const actionClick = (id: string, type: 'delete' | 'clone') => {
  validated.value = false;
  switch (type) {
    case 'clone':
      toClone(id);
      break;
    case 'delete':
      toDelete(id);
      break;
  }
};

const toClone = (targetId: string) => {
  const ids = idList.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    if (id === targetId) {
      const cloneId = utils.uuid();
      const sourceData: PipelineConfig = JSON.parse(JSON.stringify(dataMap.value[targetId]));
      dataMap.value[cloneId] = sourceData;
      dataMap.value[cloneId].name = sourceData.name ? (sourceData.name + ' copy') : '';
      dataMap.value[cloneId].enabled = false;
      idList.value.splice(i + 1, 0, cloneId);
      if (activeKeys.value.includes(id)) {
        activeKeys.value.push(cloneId);
      }

      return;
    }
  }
};

const toDelete = (targetId: string) => {
  const ids = idList.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    if (id === targetId) {
      const name = dataMap.value[targetId].name;
      if (dataMap.value[targetId].enabled && ids.length > 1) {
        const enabledTargetId = idList.value[i - 1] || idList.value[i + 1];
        dataMap.value[enabledTargetId].enabled = true;
      }
      repeatNameSet.value.delete(name);
      delete dataMap.value[targetId];
      idList.value.splice(i, 1);
      break;
    }
  }
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

const reset = () => {
  activeKeys.value = [];
  emptyNameSet.value.clear();
  idList.value = [];
  dataMap.value = {};
  repeatNameSet.value.clear();
  fileMap.value = {};
};

onMounted(() => {
  watch(() => props.value, (newValue) => {
    reset();

    if (!newValue?.length) {
      insertData();
      return;
    }

    const list = JSON.parse(JSON.stringify(newValue));
    for (let i = 0, len = list.length; i < len; i++) {
      const data = list[i];
      const { id } = data;
      // 自动补全enabled，默认值为true
      if (data.enabled !== false) {
        data.enabled = true;
        activeKeys.value.push(id);
      }

      dataMap.value[id] = analysisData(data);
      idList.value.push(id);
    }
  }, { immediate: true });

  emit('renderChange', true);
  rendered.value = true;
});

const repeatNameIdSet = computed(() => {
  const set = new Set<string>();
  const ids = idList.value;
  const _repeatNameSet = repeatNameSet.value;
  const map = dataMap.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    if (_repeatNameSet.has(map[id].name)) {
      set.add(id);
    }
  }

  return set;
});

const nameErrorSet = computed(() => {
  return new Set<string>([...emptyNameSet.value, ...repeatNameIdSet.value]);
});

const validated = ref(false);
const isValid = (): boolean => {
  emptyNameSet.value.clear();
  const ids = idList.value;
  validated.value = true;
  const map = dataMap.value;
  const set = repeatNameIdSet.value;
  let errorNum = 0;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const { name, use, transmissionMode, trustStorePath, trustStoreBase64Content, server, useAuth, password, username } = map[id];
    if (!name) {
      emptyNameSet.value.add(id);
      errorNum++;
    } else {
      if (set.has(id)) {
        emptyNameSet.value.add(id);
        errorNum++;
      }
    }
    if (!server.server || !server.port) {
      errorNum++;
    }
    if (useAuth && (!password || !username)) {
      errorNum++;
    }
    if (use !== 'NONE') {
      if (transmissionMode === 'local' && !trustStorePath) {
        errorNum++;
      }
      if (transmissionMode === 'content' && !trustStoreBase64Content) {
        errorNum++;
      }
    }
  }

  return !errorNum;
};

const onfileChange = (data, id) => {
  dataMap.value[id].trustStoreBase64Content = data.file?.value;
};

const onUseAuthChange = (id) => {
  dataMap.value[id].username = '';
  dataMap.value[id].password = '';
};

const onMsgTypeChange = (id) => {
  dataMap.value[id].numMessages = undefined;
};

const delTrustContent = (id) => {
  dataMap.value[id].trustStoreBase64Content = undefined;
};

const analysisData = (data): PipelineConfig => {
  const { server, mail, protocol, ...other } = data;
  const { password = '', username = '', security, readTimeout, connectTimeout, ...othetServer } = server;
  let { use, tlsProtocols, trustAllCerts, enforceStartTLS, useLocalTrustStore, trustStorePath = '', trustStoreBase64Content = '' } = security;
  if (typeof use !== 'string') {
    use = use.value;
  }
  return {
    ...other,
    ...mail,
    protocol: protocol.value || protocol,
    password,
    username,
    server: {
      ...othetServer,
      readTimeout,
      connectTimeout
    },
    use,
    tlsProtocols,
    trustAllCerts,
    useLocalTrustStore,
    localCertType: useLocalTrustStore && trustStorePath ? 'path' : 'file',
    trustStorePath,
    trustStoreBase64Content,
    enforceStartTLS,
    messageType: mail.numMessages ? 'other' : 'all'
  };
};

const getMailData = (data): PipelineConfig => {
  const {
    target,
    name,
    description,
    enabled,
    beforeName,
    server,
    username,
    password,
    use,
    trustAllCerts,
    localCertType,
    trustStorePath,
    tlsProtocols,
    trustStoreBase64Content,
    enforceStartTLS,
    folder,
    deleteMessage,
    storeMimeMessage,
    numMessages,
    protocol
  } = data;
  const result: {[key: string]: any} = { target, name, description, enabled, beforeName, server, protocol };
  if (server.useAuth) {
    result.server.password = password;
    result.server.username = username;
  }
  const security: {[key: string]: string|boolean} = {
    use
  };
  if (use !== 'NONE') {
    security.trustAllCerts = trustAllCerts;
    if (trustAllCerts) {
      security.useLocalTrustStore = false;
    } else {
      security.useLocalTrustStore = true;
      security.tlsProtocols = tlsProtocols;
      if (localCertType === 'path') {
        security.trustStorePath = trustStorePath;
      }
      if (localCertType === 'file') {
        security.trustStoreBase64Content = trustStoreBase64Content;
      }
      if (use === 'USE_START_TLS') {
        security.enforceStartTLS = enforceStartTLS;
      }
    }
  }

  const mail = {
    folder,
    deleteMessage,
    storeMimeMessage,
    numMessages
  };
  result.server.security = security;
  result.mail = mail;
  return result;
};

const getData = (): PipelineConfig[] => {
  const result: PipelineConfig[] = [];
  const map = dataMap.value;
  const ids = idList.value;
  let beforeName = '';
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const data = JSON.parse(JSON.stringify(map[id]));
    result.push({ ...getMailData(data), beforeName });
    beforeName = data.name;
  }

  return result;
};

defineExpose({
  isValid,
  getData,
  generateEmptSmtpData
});

const selectOptions = [
  // { label: 'SMTP', value: 'SMTP' },
  { label: 'POP3', value: 'POP3' },
  { label: 'IMAP', value: 'IMAP' }
  // { label: 'HTTP', value: 'HTTP' },
  // { label: 'HTTPS', value: 'HTTPS' }
];

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
          <span>插入MAIL请求</span>
        </div>
      </Button>
      <Hints text="支持同时编排多个 Smtp 接口，但每次只允许启用一个 Smtp 进行测试。"></Hints>
    </div>
    <template v-if="props.loaded">
      <Draggable
        v-if="!!idList.length"
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
                style="background-color: rgba(251, 251, 251, 100%);"
                collapsible="disabled">
                <template #header>
                  <div class="flex items-center flex-nowrap w-full whitespace-nowrap">
                    <Icon class="flex-shrink-0 text-4 mr-3" icon="icon-chajianpeizhi" />
                    <div class="flex-1 flex items-center space-x-3 mr-5">
                      <Tooltip
                        title="名称重复"
                        internal
                        placement="right"
                        destroyTooltipOnHide
                        :visible="repeatNameIdSet.has(id)">
                        <Input
                          :value="dataMap[id].name"
                          :maxlength="400"
                          :error="nameErrorSet.has(id)"
                          :title="dataMap[id].name"
                          style="flex:1 1 40%;"
                          trim
                          placeholder="名称，最大支持400个字符"
                          @change="nameChange(id, $event)" />
                      </Tooltip>
                      <Input
                        v-model:value="dataMap[id].description"
                        :maxlength="800"
                        :title="dataMap[id].description"
                        trim
                        style="flex:1 1 60%;"
                        placeholder="描述，最大支持800个字符" />
                    </div>
                    <div class="flex items-center flex-shrink-0 space-x-3">
                      <Switch
                        :checked="dataMap[id].enabled"
                        size="small"
                        @change="enabledChange(id, $event)" />
                      <div class="flex items-center cursor-pointer hover:text-text-link-hover" title="克隆">
                        <Icon
                          icon="icon-fuzhi"
                          class="text-3.5"
                          @click="actionClick(id, 'clone')" />
                      </div>
                      <div class="flex items-center cursor-pointer hover:text-text-link-hover" title="删除">
                        <Icon
                          icon="icon-qingchu"
                          class="text-3.5"
                          @click="actionClick(id, 'delete')" />
                      </div>
                      <Arrow :open="activeKeys.includes(id)" @change="openChange(id, $event)" />
                    </div>
                  </div>
                </template>
                <div class="space-y-3 max-w-200 mb-2">
                  <div class="flex items-center">
                    <div class="flex-shrink-0 w-33.5">
                      <IconRequired />
                      <span>协议</span>
                      <Colon />
                    </div>
                    <div class="flex-1 flex items-center space-x-2.5 max-w-">
                      <!-- <SelectEnum
                        v-model:value="dataMap[id].protocol"
                        placeholder="协议"
                        :options="selectOptions" /> -->
                      <RadioGroup
                        v-model:value="dataMap[id].protocol"
                        :options="selectOptions" />
                    </div>
                  </div>
                  <div class="flex items-center">
                    <div class="flex-shrink-0 w-33.5">
                      <IconRequired />
                      <span>收件箱文件夹</span>
                      <Colon />
                    </div>
                    <Input
                      v-model:value="dataMap[id].folder"
                      :maxlength="100"
                      :error="validated && !dataMap[id].folder"
                      title="收件箱文件夹"
                      placeholder="收件箱文件夹"
                      class="max-w-" />
                  </div>
                  <div class="flex items-center">
                    <div class="flex-shrink-0 w-33.5">
                      <span></span>
                    </div>
                    <div class="flex items-center">
                      <Checkbox v-model:checked="dataMap[id].deleteMessage">从服务器删除消息</Checkbox>
                      <Checkbox v-model:checked="dataMap[id].storeMimeMessage">存储原始邮件(.eml)到执行路径</Checkbox>
                    </div>
                  </div>
                  <div class="flex items-center">
                    <div class="flex-shrink-0 w-33.5">
                      <span>检索邮件数</span>
                      <Colon />
                    </div>

                    <div class="flex items-center flex-1 max-w- space-x-2">
                      <RadioGroup
                        v-model:value="dataMap[id].messageType"
                        class="flex-shrink-0"
                        :name="id"
                        @change="onMsgTypeChange(id)">
                        <Radio value="all">所有</Radio>
                        <Radio value="other"></Radio>
                      </RadioGroup>
                      <Input
                        v-model:value="dataMap[id].numMessages"
                        :maxlength="15"
                        :min="1"
                        :disabled="dataMap[id].messageType === 'all'"
                        trimAll
                        dataType="number"
                        title="消息数"
                        placeholder="消息数"
                        class="max-w-35" />
                    </div>
                  </div>
                </div>

                <Tabs v-model:activeKey="tabActiveKeyMap[id]" size="small">
                  <TabPane key="mail">
                    <template #tab>
                      <Badge
                        class="count-Badge-container"
                        size="small"
                        :count="mailSetErrorNumMap.get(id)">
                        <div>邮件设置</div>
                      </Badge>
                    </template>
                    <div class="space-y-4">
                      <div class="flex items-center">
                        <div class="flex-shrink-0 w-33.5 flex items-center">
                          <IconRequired />
                          <span>主机名或IP</span>
                          <Colon />
                        </div>
                        <div class="flex-1 flex items-center space-x-5 max-w-175">
                          <Input
                            v-model:value="dataMap[id].server.server"
                            :maxlength="4096"
                            :error="validated && !dataMap[id].server.server"
                            trimAll
                            style="flex: 1 1 75%;"
                            placeholder="主机名或IP，最大支持4096个字符" />
                          <div style="flex: 1 1 25%;" class="flex items-center">
                            <div class="flex-shrink-0 w-11.5 flex items-center">
                              <IconRequired />
                              <span>端口</span>
                              <Colon />
                            </div>
                            <Input
                              v-model:value="dataMap[id].server.port"
                              :maxlength="5"
                              :min="1"
                              :error="validated && !dataMap[id].server.port"
                              trimALl
                              style="min-width: 75px;max-width: 200px;"
                              dataType="integer"
                              placeholder="端口（1~65535）" />
                          </div>
                        </div>
                      </div>

                      <div class="flex items-center">
                        <div class="flex-shrink-0 w-33.5 flex items-center">
                          <IconRequired />
                          <span>连接超时</span>
                          <Colon />
                        </div>
                        <div class="flex-1 flex items-center space-x-5 max-w-175">
                          <ShortDuration
                            v-model:value="dataMap[id].server.connectTimeout"
                            style="flex:none;width:calc((100% - 90px)/2);"
                            class="bg-white"
                            :max="86400"
                            :inputProps="connectTimeoutInputProps(id)"
                            :selectProps="shortTimeSelectProps" />
                          <div style="flex: 1 1 25%;" class="flex items-center">
                            <div class="flex-shrink-0 w-17.5 flex items-center">
                              <IconRequired />
                              <span>读取超时</span>
                              <Colon />
                            </div>
                            <ShortDuration
                              v-model:value="dataMap[id].server.readTimeout"
                              :max="86400"
                              class="bg-white"
                              :inputProps="readTimeoutInputProps(id)"
                              :selectProps="shortTimeSelectProps" />
                          </div>
                        </div>
                      </div>
                    </div>
                    <div class="mt-4 mb-2">
                      <div class="flex items-center mb-3">
                        <div class="flex-shrink-0 w-33.5">
                          <span>安全功能</span>
                          <Colon />
                        </div>
                        <RadioGroup v-model:value="dataMap[id].use" :name="id">
                          <Radio class="w-26" value="NONE">不使用</Radio>
                          <Radio class="w-29" value="USE_SSL">SSL</Radio>
                          <Radio value="USE_START_TLS">StartTLS</Radio>
                        </RadioGroup>
                      </div>

                      <template v-if="dataMap[id].use !== 'NONE'">
                        <div class="flex items-center mb-3">
                          <div class="flex-shrink-0 w-33.5">
                            <span>信任证书</span>
                            <Colon />
                          </div>
                          <RadioGroup
                            v-model:value="dataMap[id].trustAllCerts"
                            :options="certificateOpt"
                            :name="id">
                            <!-- <Radio class="w-26" value="all">信任所有证书</Radio>
                            <Radio class="w-29" value="local">使用本地信任库</Radio>
                            <Radio class="w-29" value="content">上传证书</Radio> -->
                          </RadioGroup>
                          <Checkbox
                            v-show="dataMap[id].use==='USE_START_TLS'"
                            v-model:checked="dataMap[id].enforceStartTLS"
                            class="!ml-2">
                            执行StartTLS
                          </Checkbox>
                        </div>

                        <div v-show="dataMap[id].trustAllCerts === false" class="space-y-2 mb-4">
                          <div class="inline-flex items-center">
                            <div class="flex-shrink-0 w-33.5">
                              <span>本地受信证书</span>
                              <Colon />
                            </div>
                            <RadioGroup
                              v-model:value="dataMap[id].localCertType"
                              :options="[{value: 'path', label: '存储路径'}, {value: 'file', label: '上传证书'}]"
                              :name="id">
                              <!-- <Radio class="w-26" value="all">信任所有证书</Radio>
                              <Radio class="w-29" value="local">使用本地信任库</Radio>
                              <Radio class="w-29" value="content">上传证书</Radio> -->
                            </RadioGroup>
                            <Hints
                              v-if="dataMap[id].localCertType==='path'"
                              text="需要确保证书文件已上传到对应执行节点路径。"
                              class="inline-flex self-end"></Hints>
                            <Hints
                              v-if="dataMap[id].localCertType==='file'"
                              text="在脚本中存储本地证书 Base64 编码后内容。"
                              class="inline-flex self-end"></Hints>
                          </div>
                          <div v-show="dataMap[id].localCertType === 'file'" class="flex">
                            <div class="flex-shrink-0 w-33.5">
                              <span></span>
                            </div>
                            <div v-if="dataMap[id].trustStoreBase64Content" class="flex space-x-2 items-center flex-1">
                              <Input
                                type="textarea"
                                disabled
                                class="flex-1 !max-w-"
                                :value="dataMap[id].trustStoreBase64Content" />
                              <Icon
                                icon="icon-qingchu"
                                class="text-4 cursor-pointer hover:text-status-error"
                                @click="delTrustContent(id)" />
                            </div>
                            <Upload
                              v-else
                              class="flex-1"
                              :multiple="false"
                              @change="onfileChange($event, id)">
                            </Upload>
                          </div>
                          <div v-show="dataMap[id].localCertType === 'path'" class="flex items-center">
                            <div class="flex-shrink-0 w-33.5">
                              <span></span>
                            </div>
                            <Input
                              v-model:value="dataMap[id].trustStorePath"
                              :maxlength="200"
                              :error="validated && !dataMap[id].trustStorePath"
                              trimAll
                              placeholder="本地信任存储库路径"
                              class="flex-1 max-w-" />
                          </div>
                        </div>

                        <div v-show="dataMap[id].trustAllCerts === false" class="flex items-center">
                          <div class="flex-shrink-0 w-33.5">
                            <span>{{ dataMap[id].use === 'USE_START_TLS' ? '重写的SSL/TLS协议' : '覆盖系统SSL/TLS协议' }}</span>
                            <Colon />
                          </div>
                          <Input
                            v-model:value="dataMap[id].tlsProtocols"
                            :maxlength="80"
                            trimAll
                            placeholder="覆盖系统SSL/TLS协议"
                            class="flex-1 max-w-" />
                        </div>
                      </template>
                    </div>
                  </TabPane>
                  <TabPane key="other">
                    <template #tab>
                      <Badge
                        class="count-Badge-container"
                        size="small">
                        <div>身份验证设置</div>
                      </Badge>
                    </template>
                    <div class="space-y-4">
                      <div class="flex items-center">
                        <div class="flex-shrink-0 w-33.5">
                          <span>开启服务器认证</span>
                          <Colon />
                        </div>
                        <div class="flex-1 flex items-center space-x-2.5 max-w-">
                          <Switch
                            v-model:checked="dataMap[id].server.useAuth"
                            size="small"
                            @change="onUseAuthChange(id)" />
                        </div>
                      </div>
                      <div class="flex items-center">
                        <div class="flex-shrink-0 w-33.5">
                          <IconRequired v-show="dataMap[id].server.useAuth" />
                          <span>用户名/密码</span>
                          <Colon />
                        </div>
                        <div class="flex-1 flex items-center space-x-2.5 max-w-">
                          <Input
                            v-model:value="dataMap[id].username"
                            :maxlength="400"
                            :disabled="!dataMap[id].server.useAuth"
                            :error="dataMap[id].server.useAuth && validated && !dataMap[id].username"
                            trimAll
                            title="用户名"
                            placeholder="用户名" />
                          <Input
                            v-model:value="dataMap[id].password"
                            :maxlength="4096"
                            :disabled="!dataMap[id].server.useAuth"
                            :error="dataMap[id].server.useAuth && validated && !dataMap[id].password"
                            trimAll
                            title="密码"
                            placeholder="密码"
                            type="password" />
                        </div>
                      </div>
                    </div>
                  </TabPane>
                </Tabs>
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
em {
  font-style: normal
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
  padding: 14px 20px 20px;
  font-size: 12px;
}

:deep(.ant-radio):not(.ant-radio-checked) .ant-radio-inner,
.ant-input-affix-wrapper:not(.ant-input-affix-wrapper-disabled),
.ant-select:not(.ant-select-disabled) :deep(.ant-select-selector) {
  background-color: #fff;
}

.ant-tabs {
  line-height: 20px;
}

.count-Badge-container :deep(.ant-badge-count) {
  top: -2px;
  right: -5px;
}

.ant-tabs> :deep(.ant-tabs-content-holder) {
  @apply w-200;
}

</style>
