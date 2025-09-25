<script setup lang="ts">
import { nextTick, ref, onMounted, watch, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Alert, Button, Collapse, CollapsePanel, RadioGroup, Radio, Upload, Switch } from 'ant-design-vue';
import { UploadRequestOption } from 'ant-design-vue/lib/vc-upload/interface';
import Draggable from 'vuedraggable';
import { debounce } from 'throttle-debounce';
import { Icon, NoData, Input, Tooltip, Colon, Arrow, Select, IconRequired, Spin, ShortDuration } from '@xcan-angus/vue-ui';
import { utils, duration } from '@xcan-angus/infra';
import { cloneDeep } from 'lodash-es';

import { PipelineConfig } from '../PropsType';
import { gzipBase64ToArrayBuffer, base64ToArrayBuffer, stringToArrayBuffer, arrayBufferToString, arrayBufferToGzipBase64, fileToArrayBuffer, arrayBufferToBase64 } from './utils';

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

const domId = utils.uuid();
const rendered = ref(false);
const activeKeys = ref<string[]>([]);

const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: PipelineConfig }>({});
const fileMap = ref<{
  [key: string]: {
    arrayBuffer: ArrayBuffer;
    limitFlag: boolean;
    loading: boolean;
  }
}>({});

const localFileTypeMap = ref<{ [key: string]: 'url' | 'file' }>({});
const emptyNameSet = ref<Set<string>>(new Set());
const repeatNameIdSet = ref<Set<string>>(new Set());
const serverErrorSet = ref<Set<string>>(new Set());
const portErrorSet = ref<Set<string>>(new Set());
const remoteFileNameErrorSet = ref<Set<string>>(new Set());
const remoteFileUrlErrorSet = ref<Set<string>>(new Set());
const localFileNameErrorSet = ref<Set<string>>(new Set());
const localFileContentErrorSet = ref<Set<string>>(new Set());

const insertData = () => {
  const hasEnabled = Object.values(dataMap.value).find(item => item.enabled === true);
  const id = utils.uuid();
  dataMap.value[id] = {
    id,
    target: 'FTP',
    name: '',
    description: '',
    enabled: !hasEnabled, // {{ t('ftpPlugin.uiConfig.comments.onlyOneEnabled') }}
    server: {
      server: '',
      port: '',
      username: '', // length - 400
      password: '', // length - 4096
      connectTimeout: '6s', // 1s ～ 86400s
      readTimeout: '60s'// 1s ～ 86400s
    },
    uploadFile: true,
    uploadFileSource: 'LOCAL_FILE',
    remoteFileName: '',
    remoteFileUrl: '',
    localFileName: '',
    localFileContent: '',
    localFileContentEncoding: 'none',
    binaryMode: true
  };

  idList.value.push(id);

  if (!hasEnabled) {
    activeKeys.value.push(id);
  }

  localFileTypeMap.value[id] = 'file';

  scrollToBottom();
};

const checkedRepeatName = () => {
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

      localFileTypeMap.value[cloneId] = localFileTypeMap.value[targetId];

      // 克隆本地文件
      if (fileMap.value[targetId]?.arrayBuffer) {
        fileMap.value[cloneId] = { ...fileMap.value[targetId] };
        fileMap.value[cloneId].limitFlag = false;
      }

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
      delete fileMap.value[id];
      delete localFileTypeMap.value[id];

      emptyNameSet.value.delete(id);
      repeatNameIdSet.value.delete(id);
      serverErrorSet.value.delete(id);
      portErrorSet.value.delete(id);
      remoteFileNameErrorSet.value.delete(id);
      remoteFileUrlErrorSet.value.delete(id);
      localFileNameErrorSet.value.delete(id);
      localFileContentErrorSet.value.delete(id);

      break;
    }
  }

  checkedRepeatName();
};

const serverChange = (id: string) => {
  serverErrorSet.value.delete(id);
};

const portChange = (id: string) => {
  portErrorSet.value.delete(id);
};

const uploadDownloadChange = (id: string) => {
  dataMap.value[id].uploadFileSource = 'LOCAL_FILE';
  dataMap.value[id].remoteFileName = '';
  dataMap.value[id].remoteFileUrl = '';
  dataMap.value[id].localFileName = '';
  dataMap.value[id].localFileContent = '';
  dataMap.value[id].localFileContentEncoding = 'none';

  localFileTypeMap.value[id] = 'file';
  fileMap.value[id] = null;

  remoteFileNameErrorSet.value.delete(id);
  remoteFileUrlErrorSet.value.delete(id);
  localFileNameErrorSet.value.delete(id);
  localFileContentErrorSet.value.delete(id);
};

const uploadFileSourceChange = (id: string) => {
  dataMap.value[id].remoteFileName = '';
  dataMap.value[id].remoteFileUrl = '';
  dataMap.value[id].localFileName = '';
  dataMap.value[id].localFileContent = '';
  dataMap.value[id].localFileContentEncoding = 'none';

  localFileTypeMap.value[id] = 'file';
  fileMap.value[id] = null;

  remoteFileNameErrorSet.value.delete(id);
  remoteFileUrlErrorSet.value.delete(id);
  localFileNameErrorSet.value.delete(id);
  localFileContentErrorSet.value.delete(id);
};

const localFileTypeChange = (id: string) => {
  dataMap.value[id].localFileName = '';
  dataMap.value[id].localFileContent = '';
  dataMap.value[id].localFileContentEncoding = 'none';
  dataMap.value[id].binaryMode = true;

  fileMap.value[id] = null;

  localFileNameErrorSet.value.delete(id);
  localFileContentErrorSet.value.delete(id);
};

const remoteFileNameChange = (id: string) => {
  remoteFileNameErrorSet.value.delete(id);
};

const remoteFileUrlChange = (id: string) => {
  remoteFileUrlErrorSet.value.delete(id);
};

const localFileNameChange = (id: string) => {
  localFileNameErrorSet.value.delete(id);
};

const isBinary = (mimeType: string): boolean => {
  if (!mimeType) {
    return false;
  }

  if (mimeType.startsWith('text/') || ['application/json'].includes(mimeType)) {
    return false;
  }

  return true;
};

const customRequest = async (requestOption: UploadRequestOption, id: string) => {
  localFileContentErrorSet.value.delete(id);

  fileMap.value[id] = {
    arrayBuffer: null,
    limitFlag: false,
    loading: true
  };

  const file = requestOption.file as File;
  if (file.size > 1048576) {
    fileMap.value[id].limitFlag = true;
    return;
  }

  fileMap.value[id].arrayBuffer = await fileToArrayBuffer(file);
  dataMap.value[id].binaryMode = isBinary(file.type);
  dataMap.value[id].localFileName = file.name;

  fileReadAsText(id, dataMap.value[id].localFileContentEncoding);
};

const encodingChange = (id: string, value: 'none' | 'base64' | 'gzip_base64') => {
  fileReadAsText(id, value);
};

const fileReadAsText = (id: string, code: 'none' | 'base64' | 'gzip_base64' = 'none') => {
  if (!fileMap.value[id]) {
    return;
  }

  fileMap.value[id].loading = true;
  const arrayBuffer = fileMap.value[id].arrayBuffer;
  switch (code) {
    case 'base64':
      dataMap.value[id].localFileContent = arrayBufferToBase64(arrayBuffer);
      break;
    case 'gzip_base64':
      dataMap.value[id].localFileContent = arrayBufferToGzipBase64(arrayBuffer);
      break;
    case 'none':
      dataMap.value[id].localFileContent = arrayBufferToString(arrayBuffer);
      break;
  }

  fileMap.value[id].loading = false;
};

const deleteFile = (id: string) => {
  if (fileMap.value[id]) {
    fileMap.value[id] = null;
  }

  dataMap.value[id].localFileContentEncoding = 'none';
  dataMap.value[id].localFileName = '';
  dataMap.value[id].localFileContent = '';
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
  idList.value = [];
  dataMap.value = {};
  fileMap.value = {};
  localFileTypeMap.value = {};
  emptyNameSet.value.clear();
  repeatNameIdSet.value.clear();
  serverErrorSet.value.clear();
  portErrorSet.value.clear();
  remoteFileNameErrorSet.value.clear();
  remoteFileUrlErrorSet.value.clear();
  localFileNameErrorSet.value.clear();
  localFileContentErrorSet.value.clear();
};

onMounted(() => {
  watch(() => props.value, (newValue) => {
    reset();

    if (!newValue?.length) {
      insertData();
      return;
    }

    const list = cloneDeep(newValue) as PipelineConfig[];
    for (let i = 0, len = list.length; i < len; i++) {
      const data = list[i];
      const { id, server } = data;
      dataMap.value[id] = data;

      if (!server) {
        dataMap.value[id].server = {
          server: '',
          port: '',
          username: '', // length - 400
          password: '', // length - 4096
          connectTimeout: '6s', // 1s ～ 86400s
          readTimeout: '60s'// 1s ～ 86400s
        };
      }

      const localFileContent = data.localFileContent;
      if (localFileContent) {
        localFileTypeMap.value[id] = 'file';
        const encoding = data.localFileContentEncoding;
        if (encoding === 'none') {
          fileMap.value[id] = {
            arrayBuffer: stringToArrayBuffer(localFileContent),
            limitFlag: false,
            loading: false
          };
        } else if (encoding === 'base64') {
          fileMap.value[id] = {
            arrayBuffer: base64ToArrayBuffer(localFileContent),
            limitFlag: false,
            loading: false
          };
        } else if (encoding === 'gzip_base64') {
          fileMap.value[id] = {
            arrayBuffer: gzipBase64ToArrayBuffer(localFileContent),
            limitFlag: false,
            loading: false
          };
        }
      } else {
        localFileTypeMap.value[id] = 'url';
      }

      idList.value.push(id);
      activeKeys.value.push(id);
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
  remoteFileNameErrorSet.value.clear();
  remoteFileUrlErrorSet.value.clear();
  localFileNameErrorSet.value.clear();
  localFileContentErrorSet.value.clear();

  const ids = idList.value;
  const map = dataMap.value;
  const set = repeatNameIdSet.value;
  let errorNum = 0;
  const typeMap = localFileTypeMap.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const { name, server, remoteFileName, uploadFileSource, uploadFile, remoteFileUrl, localFileName, localFileContent } = map[id];
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

    if (uploadFile === false || uploadFileSource === 'REMOTE_FILE') {
      if (!remoteFileName) {
        remoteFileNameErrorSet.value.add(id);
        errorNum++;
      }
    }

    if (uploadFileSource === 'REMOTE_URL') {
      if (!remoteFileUrl) {
        remoteFileUrlErrorSet.value.add(id);
        errorNum++;
      }
    }

    if (uploadFileSource === 'LOCAL_FILE') {
      if (typeMap[id] === 'url') {
        if (!localFileName) {
          localFileNameErrorSet.value.add(id);
          errorNum++;
        }
      } else {
        if (!localFileContent) {
          localFileContentErrorSet.value.add(id);
          errorNum++;
        }
      }
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
    const data = cloneDeep(map[id]);
    result.push({ ...data, beforeName });
    beforeName = data.name;
  }

  return result;
};

defineExpose({
  isValid,
  getData
});

const codeOptions = [
  {
    label: '无',
    value: 'none'
  },
  {
    label: 'base64',
    value: 'base64'
  },
  {
    label: 'gzip_base64',
    value: 'gzip_base64'
  }
];

const connectTimeoutInputProps = {
  maxlength: 8,
  dataType: 'integer',
  placeholder: t('ftpPlugin.uiConfig.serverConfig.connectionTimeoutPlaceholder')
};

const readTimeoutInputProps = {
  maxlength: 8,
  dataType: 'integer',
  placeholder: t('ftpPlugin.uiConfig.serverConfig.readTimeoutPlaceholder')
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
          <span>{{ t('ftpPlugin.uiConfig.title') }}</span>
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
                    <div class="flex-1 flex items-center space-x-2 mr-5">
                      <Tooltip
                        :title="t('ftpPlugin.uiConfig.form.nameDuplicate')"
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
                          :placeholder="t('ftpPlugin.uiConfig.form.namePlaceholder')"
                          @change="nameChange" />
                      </Tooltip>
                      <Input
                        v-model:value="dataMap[id].description"
                        :maxlength="800"
                        :title="dataMap[id].description"
                        trim
                        style="flex:1 1 60%;"
                        :placeholder="t('ftpPlugin.uiConfig.form.descriptionPlaceholder')" />
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

                <div class="space-y-3.5">
                  <div class="flex items-center text-theme-title">
                    <div class="w-1.25 h-3 rounded mr-1.5" style="background-color: #1e88e5;"></div>
                    <span>{{ t('ftpPlugin.uiConfig.serverConfig.title') }}</span>
                  </div>
                  <div class="space-y-3.5 ml-2.75">
                    <div class="flex items-center">
                      <div class="flex-shrink-0 w-20.5 flex items-center">
                        <IconRequired />
                        <span>{{ t('ftpPlugin.uiConfig.serverConfig.hostnameOrIP') }}</span>
                        <Colon />
                      </div>
                      <div class="flex-1 flex items-center space-x-5 max-w-175">
                        <Input
                          v-model:value="dataMap[id].server.server"
                          :maxlength="4096"
                          :error="!!serverErrorSet.has(id)"
                          trimAll
                          style="flex: 1 1 75%;"
                          :placeholder="t('ftpPlugin.uiConfig.serverConfig.hostnameOrIPPlaceholder')"
                          @change="serverChange(id)" />

                        <div style="flex: 1 1 25%;" class="flex items-center">
                          <div class="flex-shrink-0 w-11.5 flex items-center">
                            <IconRequired />
                            <span>{{ t('ftpPlugin.uiConfig.serverConfig.port') }}</span>
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
                            :placeholder="t('ftpPlugin.uiConfig.serverConfig.portPlaceholder')"
                            @change="portChange(id)" />
                        </div>
                      </div>
                    </div>

                    <div class="flex items-center">
                      <div class="flex-shrink-0 w-20.5 flex items-center">
                        <span>{{ t('ftpPlugin.uiConfig.serverConfig.username') }}</span>
                        <Colon />
                      </div>
                      <div class="flex-1 flex items-center space-x-5 max-w-175">
                        <Input
                          v-model:value="dataMap[id].server.username"
                          :maxlength="400"
                          trimAll
                          :placeholder="t('ftpPlugin.uiConfig.serverConfig.usernamePlaceholder')"
                          style="width:calc((100% - 82px)/2);" />
                        <div class="flex-1 flex items-center">
                          <div class="flex-shrink-0 w-15.5 flex items-center">
                            <span>{{ t('ftpPlugin.uiConfig.serverConfig.password') }}</span>
                            <Colon />
                          </div>
                          <Input
                            v-model:value="dataMap[id].server.password"
                            :maxlength="4096"
                            trimAll
                            :placeholder="t('ftpPlugin.uiConfig.serverConfig.passwordPlaceholder')"
                            type="password" />
                        </div>
                      </div>
                    </div>

                    <div class="flex items-center">
                      <div class="flex-shrink-0 w-20.5 flex items-center">
                        <span>{{ t('ftpPlugin.uiConfig.serverConfig.connectionTimeout') }}</span>
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
                            <span>{{ t('ftpPlugin.uiConfig.serverConfig.readTimeout') }}</span>
                            <Colon />
                          </div>
                          <ShortDuration
                            v-model:value="dataMap[id].server.readTimeout"
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
                    <span>{{ t('ftpPlugin.uiConfig.requestConfig.title') }}</span>
                  </div>
                  <div class="space-y-3 ml-2.75">
                    <div class="flex items-center">
                      <div class="flex-shrink-0 w-24 flex items-center">
                        <IconRequired />
                        <span>{{ t('ftpPlugin.uiConfig.requestConfig.uploadDownload') }}</span>
                        <Colon />
                      </div>
                      <RadioGroup
                        v-model:value="dataMap[id].uploadFile"
                        :name="id + 'uploadFile'"
                        @change="uploadDownloadChange(id)">
                        <Radio :value="true">{{ t('actions.upload') }}</Radio>
                        <Radio :value="false">{{ t('actions.download') }}</Radio>
                      </RadioGroup>
                    </div>

                    <template v-if="dataMap[id].uploadFile === true">
                      <div class="flex items-center">
                        <div class="flex-shrink-0 w-24 flex items-center">
                          <IconRequired />
                          <span>{{ t('ftpPlugin.uiConfig.requestConfig.uploadFileSource') }}</span>
                          <Colon />
                        </div>
                        <RadioGroup
                          v-model:value="dataMap[id].uploadFileSource"
                          :name="id + 'uploadFileSource'"
                          @change="uploadFileSourceChange(id)">
                          <Radio value="LOCAL_FILE">
                            <span>{{ t('ftpPlugin.uiConfig.requestConfig.localFile') }}</span>
                          </Radio>
                          <Radio value="REMOTE_FILE">
                            <div class="flex items-center space-x-1">
                              <span>{{ t('ftpPlugin.uiConfig.requestConfig.remoteFile') }}</span>
                              <Tooltip
                                :title="t('ftpPlugin.uiConfig.requestConfig.remoteFileTooltip')"
                                internal
                                destroyTooltipOnHide>
                                <Icon icon="icon-tishi1" class="flex-shrink-0 text-3.5 text-text-tip cursor-pointer" />
                              </Tooltip>
                            </div>
                          </Radio>
                          <Radio value="REMOTE_URL">
                            <div class="flex items-center space-x-1">
                              <span>{{ t('ftpPlugin.uiConfig.requestConfig.remoteUrl') }}</span>
                              <Tooltip
                                :title="t('ftpPlugin.uiConfig.requestConfig.remoteFileUrlTooltip')"
                                internal
                                destroyTooltipOnHide>
                                <Icon icon="icon-tishi1" class="flex-shrink-0 text-3.5 text-text-tip cursor-pointer" />
                              </Tooltip>
                            </div>
                          </Radio>
                        </RadioGroup>
                      </div>

                      <template v-if="dataMap[id].uploadFileSource === 'REMOTE_URL'">
                        <div class="flex items-center">
                          <div class="flex-shrink-0 w-24 flex items-center">
                            <IconRequired />
                            <span>{{ t('ftpPlugin.uiConfig.requestConfig.remoteFileUrl') }}</span>
                            <Colon />
                          </div>
                          <div class="flex-1 flex items-center space-x-1 max-w-175">
                            <Input
                              v-model:value="dataMap[id].remoteFileUrl"
                              :error="!!remoteFileUrlErrorSet.has(id)"
                              :maxlength="4096"
                              trim
                              :placeholder="t('ftpPlugin.uiConfig.requestConfig.remoteFileUrlPlaceholder')"
                              class="flex-1"
                              @change="remoteFileUrlChange(id)" />
                          </div>
                        </div>
                      </template>

                      <template v-if="dataMap[id].uploadFileSource === 'REMOTE_FILE'">
                        <div class="flex items-center">
                          <div class="flex-shrink-0 w-24 flex items-center">
                            <IconRequired />
                            <span>{{ t('ftpPlugin.uiConfig.requestConfig.remoteFileName') }}</span>
                            <Colon />
                          </div>
                          <div class="flex-1 flex items-center space-x-1 max-w-175">
                            <Input
                              v-model:value="dataMap[id].remoteFileName"
                              :error="!!remoteFileNameErrorSet.has(id)"
                              :maxlength="4096"
                              trim
                              :placeholder="t('ftpPlugin.uiConfig.requestConfig.remoteFileNamePlaceholder')"
                              class="flex-1"
                              @change="remoteFileNameChange(id)" />
                          </div>
                        </div>
                      </template>

                      <template v-else>
                        <div class="flex items-center">
                          <div class="flex-shrink-0 w-24 flex items-center">
                            <span>{{ t('ftpPlugin.uiConfig.requestConfig.remoteFileName') }}</span>
                            <Colon />
                          </div>
                          <div class="flex-1 flex items-center space-x-1 max-w-175">
                            <Input
                              v-model:value="dataMap[id].remoteFileName"
                              :maxlength="4096"
                              trim
                              :placeholder="t('ftpPlugin.uiConfig.requestConfig.remoteFileUrlHints')"
                              class="flex-1" />
                          </div>
                        </div>
                      </template>

                      <template v-if="dataMap[id].uploadFileSource === 'LOCAL_FILE'">
                        <div class="flex items-center">
                          <div class="flex-shrink-0 w-24 flex items-center">
                            <IconRequired />
                            <span>{{ t('ftpPlugin.uiConfig.fileConfig.localFilePath') }}</span>
                            <Colon />
                          </div>
                          <RadioGroup
                            v-model:value="localFileTypeMap[id]"
                            :name="id + 'localFile'"
                            @change="localFileTypeChange(id)">
                            <Radio value="file">
                              <span>{{ t('ftpPlugin.uiConfig.requestConfig.localFileContent') }}</span><span class="text-theme-sub-content">（{{ t('ftpPlugin.uiConfig.requestConfig.localFileContentTooltip') }}）</span>
                            </Radio>
                            <Radio value="url">
                              <div class="flex items-center space-x-1">
                                <span>{{ t('ftpPlugin.uiConfig.fileConfig.localFilePath') }}</span>
                                <Tooltip
                                  :title="t('ftpPlugin.uiConfig.fileConfig.localFileNameTooltip')"
                                  internal
                                  destroyTooltipOnHide>
                                  <Icon
                                    icon="icon-tishi1"
                                    class="flex-shrink-0 text-3.5 text-text-tip cursor-pointer" />
                                </Tooltip>
                              </div>
                            </Radio>
                          </RadioGroup>
                        </div>
                      </template>

                      <template v-if="dataMap[id].uploadFileSource === 'LOCAL_FILE'">
                        <template v-if="localFileTypeMap[id] === 'file'">
                          <div class="flex items-center">
                            <div class="flex-shrink-0 w-24 flex items-center"></div>
                            <div class="flex-1 flex items-center justify-between space-x-3 max-w-175 overflow-hidden">
                              <div class="flex-shrink-0 flex items-center">
                                <Upload
                                  :maxCount="1"
                                  :multiple="false"
                                  :showUploadList="false"
                                  :customRequest="(requestOption) => customRequest(requestOption, id)">
                                  <Button
                                    :danger="localFileContentErrorSet.has(id)"
                                    class="flex-shrink-0"
                                    size="small">
                                    <Icon icon="icon-xuanze" class="mr-1" />
                                    <span>{{ t('ftpPlugin.uiConfig.upload.selectFile') }}</span>
                                  </Button>
                                </Upload>
                                <Alert
                                  v-if="fileMap[id]?.limitFlag"
                                  size="small"
                                  :message="t('ftpPlugin.uiConfig.upload.fileSizeLimit')"
                                  style="margin-left:10px;padding:0;border:none;background-color: transparent;font-size:12px;"
                                  type="error"
                                  closable />
                                <Alert
                                  v-if="localFileContentErrorSet.has(id)"
                                  size="small"
                                  :message="t('ftpPlugin.uiConfig.upload.selectFileTooltip')"
                                  style="margin-left:10px;padding:0;border:none;background-color: transparent;font-size:12px;"
                                  type="error" />
                              </div>

                              <div v-if="dataMap[id].localFileName" class="flex-1 flex items-center overflow-hidden">
                                <span class="truncate">{{ dataMap[id].localFileName }}</span>
                                <Icon
                                  icon="icon-qingchu"
                                  class="flex-shrink-0 text-3.5 ml-2 cursor-pointer hover:text-status-error"
                                  @click="deleteFile(id)" />
                              </div>

                              <div class="flex-shrink-0 flex items-center space-x-2 whitespace-nowrap">
                                <div>
                                  <span>{{ t('ftpPlugin.uiConfig.upload.code') }}</span>
                                  <Colon />
                                </div>
                                <Select
                                  v-model:value="dataMap[id].localFileContentEncoding"
                                  :options="codeOptions"
                                  :placeholder="t('ftpPlugin.uiConfig.upload.fileEncoding')"
                                  class="w-27"
                                  @change="encodingChange(id, $event)" />
                              </div>
                            </div>
                          </div>

                          <template v-if="dataMap[id].localFileContent">
                            <div class="flex items-center">
                              <div class="flex-shrink-0 w-24 flex items-center">
                              </div>
                              <Spin :spinning="fileMap[id]?.loading" class="flex-1 max-w-175">
                                <div
                                  class="leading-4.5 px-2.5 py-2 min-h-9 max-h-50 overflow-auto break-all rounded border border-solid border-theme-text-box whitespace-break-spaces">
                                  {{ dataMap[id].localFileContent }}
                                </div>
                              </Spin>
                            </div>
                          </template>
                        </template>

                        <template v-else-if="localFileTypeMap[id] === 'url'">
                          <div class="flex items-center">
                            <div class="flex-shrink-0 w-24 flex items-center"></div>
                            <div class="flex-1 flex items-center space-x-1 max-w-175">
                              <Input
                                v-model:value="dataMap[id].localFileName"
                                :error="!!localFileNameErrorSet.has(id)"
                                :maxlength="4096"
                                trim
                                :placeholder="t('ftpPlugin.uiConfig.fileConfig.localFilePathPlaceholder')"
                                class="flex-1"
                                @change="localFileNameChange(id)" />
                            </div>
                          </div>
                        </template>
                      </template>

                      <div class="flex items-center">
                        <div class="flex-shrink-0 w-24 flex items-center">
                          <span>{{ t('ftpPlugin.uiConfig.fileConfig.binaryFile') }}</span>
                          <Tooltip
                            :title="t('ftpPlugin.uiConfig.fileConfig.binaryFileTooltip')"
                            internal
                            destroyTooltipOnHide>
                            <Icon icon="icon-tishi1" class="flex-shrink-0 text-3.5 text-text-tip cursor-pointer" />
                          </Tooltip>
                          <Colon />
                        </div>
                        <RadioGroup v-model:value="dataMap[id].binaryMode" :name="id + 'binaryMode'">
                          <Radio :value="true">{{ t('status.yes') }}</Radio>
                          <Radio :value="false">{{ t('status.no') }}</Radio>
                        </RadioGroup>
                      </div>
                    </template>

                    <template v-else>
                      <div class="flex items-center">
                        <div class="flex-shrink-0 w-24 flex items-center">
                          <IconRequired />
                          <span>{{ t('ftpPlugin.uiConfig.fileConfig.remoteFileName') }}</span>
                          <Colon />
                        </div>
                        <div class="flex-1 flex items-center space-x-1 max-w-175">
                          <Input
                            v-model:value="dataMap[id].remoteFileName"
                            :error="!!remoteFileNameErrorSet.has(id)"
                            :maxlength="4096"
                            trim
                            :placeholder="t('ftpPlugin.uiConfig.fileConfig.remoteFileNamePlaceholder')"
                            class="flex-1"
                            @change="remoteFileNameChange(id)" />
                        </div>
                      </div>
                    </template>
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

.ant-alert-error :deep(.ant-alert-message) {
  color: rgba(245, 34, 45, 100%);
}

.ant-btn.error {
  border-color: rgba(245, 34, 45, 100%);
}
</style>
