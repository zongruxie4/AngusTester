<script setup lang="ts">
import { ref, onMounted, watch, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { Collapse, CollapsePanel, Tabs, TabPane, Button, Checkbox, Switch, RadioGroup } from 'ant-design-vue';
import { Input, Icon, Tooltip, Hints, Select, IconRequired, ShortDuration } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { PipelineConfig } from '../../PropsType';
import Upload from './Upload.vue';

const { t } = useI18n();

export type ParameterConfig = {
  name: string;
  enabled: boolean;
  disabled: boolean;
  id: string;
  value: string;
  type: 'string';
  in: ParameterIn;
}

export interface Props {
  value: PipelineConfig;
  repeatNames: string[];
  enabledDisabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  repeatNames: () => [],
  enabledDisabled: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'actionClick', value: 'delete' | 'clone'): void;
  (e: 'change', value: Omit<PipelineConfig, 'id'>): void;
  (e: 'nameChange', value: string): void;
  (e: 'enabledChange', value: boolean): void;
  (e: 'renderChange'): void;
}>();

const ActionsGroup = defineAsyncComponent(() => import('../ActionsGroup/index.vue'));

const connectTimeoutInputProps = {
  maxlength: 8,
  dataType: 'integer',
  placeholder: t('smtpPlugin.uiConfig.serverConfig.connectionTimeoutPlaceholder')
};

const readTimeoutInputProps = {
  maxlength: 8,
  dataType: 'integer',
  placeholder: t('smtpPlugin.uiConfig.serverConfig.readTimeoutPlaceholder')
};

const shortTimeSelectProps = {
  excludes: () => { return false; },
  style: 'width:65px;'
};

// const extractNumbers = (str) => {
//   // 使用正则表达式匹配所有数字
//   if (str === undefined) {
//     return '';
//   }
//   const numbers = str.match(/\d+/g);
//   // 输出数字数组或者提示没有找到数字
//   return numbers !== null ? numbers.join('') : '';
// };
let UUID = utils.uuid();
const activeKey = ref<string>();

const emailConfig = ref({ // 邮件设置
  mailFrom: '',
  replyTo: [],
  receiverCC: [],
  receiverBCC: [],
  receiverTo: []
});
const mailContent = ref({ // 邮件内容设置
  subject: '',
  message: '',
  includeTimestamp: false,
  suppressSubject: false,
  sendEmlMessage: false, // 是否使用 EML 文件作为消息内
  plainBody: true,
  messageSizeStatistics: false,
  enableDebugLogging: false,
  localAttachFiles: '',
  localEmlMessageFile: '', // eml文件路径
  localEmlMessageBase64Content: '' // eml文件base64
});

const server = ref({ // server配置
  server: '',
  port: '',
  useAuth: false,
  username: '',
  password: '',
  readTimeout: '60s',
  connectTimeout: '6s'
});

const serverSecurity = ref({ // server 下security 配置
  use: 'NONE',
  trustAllCerts: true,
  enforceStartTLS: false,
  useLocalTrustStore: false,
  trustStorePath: '',
  trustStoreBase64Content: '',
  tlsProtocols: ''
});

const onIgnoreTitleChange = () => {
  if (mailContent.value.suppressSubject) {
    mailContent.value.subject = '';
    mailContent.value.includeTimestamp = false;
  }
};

const securityConfigOpt = [
  {
    label: t('smtpPlugin.uiConfig.securityConfig.none'),
    value: 'NONE'
  },
  {
    label: t('smtpPlugin.uiConfig.securityConfig.useSSL'),
    value: 'USE_SSL'
  },
  {
    label: t('smtpPlugin.uiConfig.securityConfig.useStartTLS'),
    value: 'USE_START_TLS'
  }
];

const emlMsgOpt = [
  {
    value: false,
    label: t('smtpPlugin.uiConfig.emlMsgOpt.n')
  },
  {
    value: true,
    label: t('smtpPlugin.uiConfig.emlMsgOpt.y')
  }
];
const attachmentTypeOpt = [
  {
    value: 'path',
    label: t('smtpPlugin.uiConfig.attachmentTypeOpt.path')
  },
  {
    value: 'file',
    label: t('smtpPlugin.uiConfig.attachmentTypeOpt.file')
  }
];
const emlTypeOpt = [
  {
    value: 'path',
    label: t('smtpPlugin.uiConfig.emlTypeOpt.path')
  },
  {
    value: 'file',
    label: t('smtpPlugin.uiConfig.emlTypeOpt.file')
  }
];

const certificateTypeOpt = [
  {
    value: 'path',
    label: t('smtpPlugin.uiConfig.certificateTypeOpt.path')
  },
  {
    value: 'file',
    label: t('smtpPlugin.uiConfig.certificateTypeOpt.file')
  }
];

const emlUploadRef = ref();
const certUploadRef = ref();
const emlType = ref('path');
const attachmentType = ref('');
const attachmentFiles = ref<{value: string; fileName: string;}[]>([]);

const certificateOpt = [
  {
    label: t('smtpPlugin.uiConfig.certificateOpt.all'),
    value: true
  },
  {
    label: t('smtpPlugin.uiConfig.certificateOpt.local'),
    value: false
  }
];

const localCertType = ref('path');

const onOpenChange = () => {
  server.value.password = '';
  server.value.username = '';
};

const headers = ref<{value: string, name: string}[]>([]);

const description = ref<string>();
const enabled = ref(false);
const name = ref<string>();
const nameRepeatFlag = ref(false);

const nameChange = (event: { target: { value: string } }) => {
  const value = event.target.value;
  name.value = value;
  nameRepeatFlag.value = false;
  emit('nameChange', value);
};

const openChange = (_open: boolean) => {
  if (_open) {
    activeKey.value = UUID;
    return;
  }

  activeKey.value = '';
};

const enabledChange = (_enabled: boolean) => {
  if (props.enabledDisabled) {
    enabled.value = true;
    return;
  }
  enabled.value = _enabled;
  emit('enabledChange', _enabled);
};

const actionClick = (value: 'delete' | 'clone') => {
  emit('actionClick', value);
};

const addHeader = () => {
  headers.value.push({ name: '', value: '' });
};

const delHeader = (idx: number) => {
  headers.value.splice(idx, 1);
};

const initializedData = async () => {
  if (!props.value) {
    return;
  }

  const newValue = JSON.parse(JSON.stringify(props.value));
  const {
    name: _name,
    description: _description,
    enabled: _enabled,
    mail,
    uuid
  } = newValue;

  const { content, replyTo, receiverCC, receiverBCC, receiverTo, ...otherMail } = mail;
  emailConfig.value = {
    ...emailConfig.value,
    ...otherMail,
    replyTo: replyTo ? replyTo.split(';') : [],
    receiverCC: receiverCC ? receiverCC.split(';') : [],
    receiverBCC: receiverBCC ? receiverBCC.split(';') : [],
    receiverTo: receiverTo ? receiverTo.split(';') : []
  };
  const { localAttachBase64Contents, localAttachFiles, headerFields, ...otherMailContent } = content;
  mailContent.value = {
    ...mailContent.value,
    ...otherMailContent
  };
  if (!content.sendEmlMessage) {
    if (localAttachBase64Contents) {
      attachmentType.value = 'file';
      attachmentFiles.value = Object.keys(localAttachBase64Contents).map(key => {
        return {
          value: localAttachBase64Contents[key],
          fileName: key,
          name: key
        };
      });
    }
    if (localAttachFiles) {
      mailContent.value.localAttachFiles = content.localAttachFiles.join(';');
      attachmentType.value = 'path';
    }
  } else {
    if (content.localEmlMessageBase64Content) {
      emlType.value = 'file';
      mailContent.value.localEmlMessageBase64Content = content.localEmlMessageBase64Content;
    }
  }
  if (headerFields) {
    headers.value = Object.keys(content.headerFields).map(key => {
      return {
        value: content.headerFields[key],
        name: key
      };
    });
  }

  const { security, readTimeout, connectTimeout, ...otherServer } = newValue.server;
  server.value = {
    ...server.value,
    ...otherServer,
    readTimeout,
    connectTimeout
  };

  let { use, trustAllCerts = true, enforceStartTLS = false, useLocalTrustStore = false, trustStoreBase64Content = '', tlsProtocols = '' } = security;
  if (typeof use !== 'string') {
    use = use.value;
  }
  serverSecurity.value = {
    ...serverSecurity.value,
    use,
    trustAllCerts,
    enforceStartTLS,
    tlsProtocols,
    useLocalTrustStore,
    trustStoreBase64Content
  };
  if (trustStoreBase64Content) {
    localCertType.value = 'file';
  }

  name.value = _name;
  description.value = _description;
  enabled.value = _enabled;
  if (uuid) {
    UUID = uuid;
    openChange(true);
  }
};

const validated = ref(false);
const isValid = (): boolean => {
  nameRepeatFlag.value = false;
  validated.value = true;

  let errorNum = 0;
  if (!name.value) {
    errorNum++;
  } else {
    if (props.repeatNames.includes(name.value)) {
      nameRepeatFlag.value = true;
    }
  }
  if (!server.value.server || !server.value.port) {
    errorNum++;
  }
  if (server.value.useAuth) {
    if (!server.value.username || !server.value.password) {
      errorNum++;
    }
  }
  if (serverSecurity.value.use === 'USE_SSL' && serverSecurity.value.useLocalTrustStore) {
    if (!serverSecurity.value.trustStorePath && !serverSecurity.value.trustStoreBase64Content) {
      errorNum++;
    }
  }
  if (mailContent.value.sendEmlMessage) {
    if (!mailContent.value.localEmlMessageFile && !mailContent.value.localEmlMessageBase64Content) {
      errorNum++;
    }
  }

  return !errorNum;
};

const onFileChange = (data) => {
  attachmentFiles.value = data.file;
};

const onEmlChange = (data) => {
  mailContent.value.localEmlMessageBase64Content = data.file?.value;
};

const delEmlContent = () => {
  mailContent.value.localEmlMessageBase64Content = '';
  emlUploadRef.value.clear();
};

const onCertFileChange = (data) => {
  serverSecurity.value.trustStoreBase64Content = data.file?.value;
};

const delCertFile = () => {
  serverSecurity.value.trustStoreBase64Content = '';
  certUploadRef.value.clear();
};

const trustCertChange = () => {
  if (serverSecurity.value.trustAllCerts) {
    serverSecurity.value.useLocalTrustStore = false;
  } else {
    serverSecurity.value.useLocalTrustStore = true;
  }
};

onMounted(() => {
  emit('renderChange');
  initializedData();

  watch(() => props.repeatNames, (newValue) => {
    if (newValue.includes(name.value)) {
      nameRepeatFlag.value = true;
      return;
    }

    if (nameRepeatFlag.value) {
      nameRepeatFlag.value = false;
    }
  });
  watch(() => props.value.enabled, newValue => {
    // props.value.enabled
    enabled.value = newValue;
  });
});

const getData = () => {
  const _server = JSON.parse(JSON.stringify(server.value));
  const _serverSecurity = JSON.parse(JSON.stringify(serverSecurity.value));
  const _mail = JSON.parse(JSON.stringify(emailConfig.value));

  const { replyTo, receiverCC, receiverBCC, receiverTo } = _mail;
  if (replyTo.length) {
    _mail.replyTo = replyTo.join(';');
  } else {
    _mail.replyTo = undefined;
  }
  if (receiverCC.length) {
    _mail.receiverCC = receiverCC.join(';');
  } else {
    _mail.receiverCC = undefined;
  }
  if (receiverBCC.length) {
    _mail.receiverBCC = receiverBCC.join(';');
  } else {
    _mail.receiverBCC = undefined;
  }
  if (receiverTo.length) {
    _mail.receiverTo = receiverTo.join(';');
  } else {
    _mail.receiverTo = undefined;
  }
  const _mailContent = JSON.parse(JSON.stringify(mailContent.value));
  if (headers.value.length) {
    _mailContent.headerFields = {};
    headers.value.forEach(item => {
      if (item.name) {
        _mailContent.headerFields[item.name] = item.value;
      }
    });
  }
  if (!_mailContent.sendEmlMessage) {
    delete _mailContent.localEmlMessageFile;
    delete _mailContent.localEmlMessageBase64Content;
    if (attachmentType.value === 'file') {
      _mailContent.localAttachBase64Contents = {};
      attachmentFiles.value.forEach(item => {
        if (item.fileName) {
          _mailContent.localAttachBase64Contents[item.fileName] = item.value;
        }
      });
      delete _mailContent.localAttachFiles;
    } else if (attachmentType.value === 'path') {
      _mailContent.localAttachFiles = _mailContent.localAttachFiles.split(';');
    }
  } else {
    delete _mailContent.localAttachFiles;
    delete _mailContent.localAttachBase64Contents;
    if (emlType.value === 'file') {
      delete _mailContent.localEmlMessageFile;
    } else {
      delete _mailContent.localEmlMessageBase64Content;
    }
  }

  return {
    beforeName: '',
    description: description.value,
    enabled: enabled.value,
    name: name.value,
    target: 'SMTP',
    server: {
      ..._server,
      security: {
        ..._serverSecurity
      }
    },
    mail: {
      ..._mail,
      content: {
        ..._mailContent
      }
    }
  };
};

defineExpose({
  getData,
  isValid,
  getName: (): string => {
    return name.value;
  },
  validateRepeatName: (value: string[]): boolean => {
    if (value.includes(name.value)) {
      nameRepeatFlag.value = true;
      return false;
    }

    return true;
  }
});
</script>

<template>
  <Collapse
    v-model:activeKey="activeKey"
    :class="{ 'opacity-70': !enabled }"
    class="SMTP-collapse-container white-bg-container"
    :bordered="false">
    <CollapsePanel
      :key="UUID"
      :showArrow="false"
      style="background-color: rgba(251, 251, 251, 100%);"
      collapsible="disabled">
      <template #header>
        <div class="flex items-center flex-nowrap w-full whitespace-nowrap">
          <Icon
            class="flex-shrink-0 text-4 mr-3"
            icon="icon-chajianpeizhi" />
          <div class="flex-1 flex items-center space-x-5 mr-5">
            <Tooltip
              :title="t('smtpPlugin.uiConfig.mailContent.nameTip')"
              internal
              placement="right"
              destroyTooltipOnHide
              :visible="nameRepeatFlag">
              <Input
                :value="name"
                :maxlength="400"
                :error="nameRepeatFlag || (validated && !name)"
                :title="name"
                trim
                class="smtp-name-input"
                :placeholder="t('smtpPlugin.uiConfig.mailContent.namePlaceholder')"
                @change="nameChange" />
            </Tooltip>
            <Input
              v-model:value="description"
              :maxlength="800"
              :placeholder="t('smtpPlugin.uiConfig.mailContent.descriptionPlaceholder')" />
          </div>
          <ActionsGroup
            v-model:enabled="enabled"
            :open="activeKey === UUID"
            :arrowVisible="true"
            @openChange="openChange"
            @enabledChange="enabledChange"
            @click="actionClick" />
        </div>
      </template>
      <div class="flex items-center text-theme-title text-3 mb-2">
        <div class="w-1.25 h-3 rounded mr-1.5" style="background-color: #1e88e5;"></div>
        <span>{{ t('smtpPlugin.uiConfig.mailContent.themeConfig') }}</span>
      </div>
      <div class="space-y-3 mb-3">
        <Input
          v-model:value="mailContent.subject"
          class="!w-200"
          size="small"
          :disabled="mailContent.suppressSubject"
          :maxlength="4096"
          :placeholder="t('smtpPlugin.uiConfig.mailContent.themePlaceholder')" />
        <div class="flex items-center">
          <Checkbox v-model:checked="mailContent.includeTimestamp" :disabled="mailContent.suppressSubject">{{ t('smtpPlugin.uiConfig.mailContent.includeTimestamp') }}</Checkbox>
          <Checkbox v-model:checked="mailContent.suppressSubject" @change="onIgnoreTitleChange">{{ t('smtpPlugin.uiConfig.mailContent.disableSubject') }}</Checkbox>
          <Hints :text="t('smtpPlugin.uiConfig.mailContent.subjectHints')"></Hints>
        </div>
        <Button
          size="small"
          type="primary"
          @click="addHeader">
          <Icon icon="icon-jia" />
          {{ t('smtpPlugin.uiConfig.mailContent.addHeader') }}
        </Button>
      </div>
      <div
        v-for="(_item, idx) in headers"
        :key="idx"
        class="flex items-center space-x-2 mb-2 w-200">
        <Input
          v-model:value="headers[idx].name"
          class="flex-1"
          :placeholder="t('smtpPlugin.uiConfig.mailContent.headerNamePlaceholder')"
          :maxlength="400" />
        <Input
          v-model:value="headers[idx].value"
          class="flex-1"
          :placeholder="t('smtpPlugin.uiConfig.mailContent.headerValuePlaceholder')"
          :maxlength="400" />
        <Icon
          icon="icon-jian"
          @click="delHeader(idx)" />
      </div>
      <div class="flex items-center text-theme-title text-3 mt-5 mb-2">
        <div class="w-1.25 h-3 rounded mr-1.5" style="background-color: #1e88e5;"></div>
        <span>{{ t('smtpPlugin.uiConfig.mailContent.contentConfig') }}</span>
      </div>
      <div class="space-y-2 w-200 text-3">
        <RadioGroup v-model:value="mailContent.sendEmlMessage" :options="emlMsgOpt" />
        <div v-if="!mailContent.sendEmlMessage" class="text-3">
          <Input
            v-model:value="mailContent.message"
            type="textarea"
            :disabled="mailContent.sendEmlMessage"
            :maxlength="2097152"
            :placeholder="t('smtpPlugin.uiConfig.mailContent.mailContent')"
            class="flex-1 mb-2" />
          <Checkbox v-model:checked="mailContent.plainBody" class="mb-5">{{ t('smtpPlugin.uiConfig.mailContent.plainBody') }}</Checkbox>
          <div class="flex items-center text-theme-title text-3 mb-2">
            <div class="w-1.25 h-3 rounded mr-1.5" style="background-color: #1e88e5;"></div>
            <span>{{ t('smtpPlugin.uiConfig.mailContent.attachment') }}</span>
          </div>
          <div class="flex items-center mb-2">
            <span>{{ t('smtpPlugin.uiConfig.mailContent.attachment') }}：</span>
            <RadioGroup v-model:value="attachmentType" :options="attachmentTypeOpt" />
          </div>
          <div v-if="attachmentType === 'file'" class="min-h-7 border rounded flex items-center mb-2">
            <Upload
              type="file(array)"
              class="w-full"
              :value="attachmentFiles"
              :disabled="mailContent.sendEmlMessage"
              @change="onFileChange">
              <template #text>
                {{ t('smtpPlugin.uiConfig.mailContent.selectAttachment') }}
              </template>
            </Upload>
          </div>
          <div v-if="attachmentType === 'path'" class="space-y-2">
            <Hints :text="t('smtpPlugin.uiConfig.mailContent.attachmentHints')" />
            <Input v-model:value="mailContent.localAttachFiles" class="flex-1" />
          </div>
        </div>
        <div v-else class="text-3 space-y-2">
          <Checkbox v-model:checked="mailContent.plainBody">{{ t('smtpPlugin.uiConfig.mailContent.plainBody') }}</Checkbox>
          <div class="flex items-center">
            <span>{{ t('smtpPlugin.uiConfig.mailContent.emlFile') }}：</span>
            <RadioGroup v-model:value="emlType" :options="emlTypeOpt" />
            <Upload
              v-show="emlType === 'file' && !mailContent.localEmlMessageBase64Content"
              ref="emlUploadRef"
              type="file"
              accept=".eml"
              :disabled="!mailContent.sendEmlMessage"
              :error="validated"
              @change="onEmlChange">
              <template #text>
                {{ t('smtpPlugin.uiConfig.mailContent.selectEmlFile') }}
              </template>
            </Upload>
          </div>
          <div
            v-if="emlType === 'file'"
            v-show="mailContent.localEmlMessageBase64Content"
            class="p-1 rounded flex items-center flex-1 mt-2">
            <div class="flex w-125 space-x-2">
              <Input
                class="flex-1"
                disabled
                type="textarea"
                :border="false"
                :value="mailContent.localEmlMessageBase64Content" />
              <Icon
                icon="icon-qingchu"
                class="text-4 cursor-pointer hover:text-status-error"
                @click="delEmlContent" />
            </div>
          </div>
          <div v-if="emlType === 'path'" class="space-y-2">
            <Input v-model:value="mailContent.localEmlMessageFile" class="flex-1" />
          </div>
        </div>
      </div>
      <div class="flex items-center text-theme-title text-3 mt-5 mb-2">
        <div class="w-1.25 h-3 rounded mr-1.5" style="background-color: #1e88e5;"></div>
        <span>{{ t('smtpPlugin.uiConfig.emailConfig.sendConfig') }}</span>
      </div>
      <div class="space-y-3.5 w-200 text-3">
        <div class="flex items-center space-x-2">
          <div class="w-33.5">{{ t('smtpPlugin.uiConfig.emailConfig.sendAddress') }}: </div>
          <Input
            v-model:value="emailConfig.mailFrom"
            class="flex-1"
            placeholder="发送地址（Address From）"
            :maxlength="100" />
        </div>
        <div class="flex items-center space-x-2">
          <div class="w-33.5"><IconRequired />{{ t('smtpPlugin.uiConfig.emailConfig.receiverAddress') }}: </div>
          <Select
            v-model:value="emailConfig.receiverTo"
            mode="tags"
            :error="validated && !emailConfig.receiverTo.length"
            class="w-full flex-1"
            :placeholder="t('smtpPlugin.uiConfig.emailConfig.receiverPlaceholder')"
            :maxlength="100" />
        </div>
        <div class="flex items-center space-x-2">
          <div class="w-33.5">{{ t('smtpPlugin.uiConfig.emailConfig.receiverCC') }}: </div>
          <Select
            v-model:value="emailConfig.receiverCC"
            mode="tags"
            class="w-full flex-1"
            :placeholder="t('smtpPlugin.uiConfig.emailConfig.receiverCCPlaceholder')"
            :maxlength="100" />
        </div>
        <div class="flex items-center space-x-2">
          <div class="w-33.5">{{ t('smtpPlugin.uiConfig.emailConfig.receiverBCC') }}: </div>
          <Select
            v-model:value="emailConfig.receiverBCC"
            mode="tags"
            class="w-full flex-1"
            :placeholder="t('smtpPlugin.uiConfig.emailConfig.receiverBCCPlaceholder')"
            :maxlength="100" />
        </div>
        <div class="flex items-center space-x-2">
          <div class="w-33.5">{{ t('smtpPlugin.uiConfig.emailConfig.replyTo') }}: </div>
          <Select
            v-model:value="emailConfig.replyTo"
            mode="tags"
            class="w-full flex-1"
            :placeholder="t('smtpPlugin.uiConfig.emailConfig.replyToPlaceholder')"
            :maxlength="100" />
        </div>
      </div>
      <Tabs
        size="small"
        class="white-bg-container">
        <TabPane
          key="service"
          :tab="t('smtpPlugin.uiConfig.serviceConfig.title')">
          <div class="flex items-center mb-3.5">
            <div class="flex-shrink-0 w-33.5 flex items-center">
              <IconRequired />
              <span>{{ t('smtpPlugin.uiConfig.serviceConfig.hostnameOrIP') }}</span>
              <Colon />
            </div>
            <div class="flex-1 flex items-center space-x-5 max-w-175">
              <Input
                v-model:value="server.server"
                :error="validated && !server.server"
                :maxlength="4096"
                trimAll
                style="flex: 1 1 75%;"
                :placeholder="t('smtpPlugin.uiConfig.serviceConfig.hostnameOrIPPlaceholder')" />
              <div style="flex: 1 1 25%;" class="flex items-center">
                <div class="flex-shrink-0 w-10.5 flex items-center">
                  <IconRequired />
                  <span>{{ t('smtpPlugin.uiConfig.serviceConfig.port') }}</span>
                  <Colon />
                </div>
                <Input
                  v-model:value="server.port"
                  :maxlength="5"
                  :min="1"
                  :error="validated && !server.port"
                  trimALl
                  style="min-width: 75px;max-width: 200px;"
                  dataType="integer"
                  :placeholder="t('smtpPlugin.uiConfig.serviceConfig.portPlaceholder')" />
              </div>
            </div>
          </div>

          <div class="flex items-center mb-3.5">
            <div class="flex-shrink-0 w-33.5 flex items-center">
              <IconRequired />
              <span>{{ t('smtpPlugin.uiConfig.serverConfig.connectionTimeout') }}</span>
              <Colon />
            </div>
            <div class="flex-1 flex items-center space-x-5 max-w-175">
              <ShortDuration
                v-model:value="server.connectTimeout"
                style="flex:none;width:calc((100% - 86px)/2);"
                class="bg-white"
                :max="86400"
                :inputProps="connectTimeoutInputProps"
                :selectProps="shortTimeSelectProps" />
              <div style="flex: 1 1 25%;" class="flex items-center">
                <div class="flex-shrink-0 w-16.5 flex items-center">
                  <IconRequired />
                  <span>{{ t('smtpPlugin.uiConfig.serverConfig.readTimeout') }}</span>
                  <Colon />
                </div>
                <ShortDuration
                  v-model:value="server.readTimeout"
                  :max="86400"
                  class="bg-white"
                  :inputProps="readTimeoutInputProps"
                  :selectProps="shortTimeSelectProps" />
              </div>
            </div>
          </div>

          <div class="flex items-center mb-3.5">
            <span class="w-33.5">{{ t('smtpPlugin.uiConfig.serviceConfig.security') }}:</span>
            <RadioGroup
              v-model:value="serverSecurity.use"
              :options="securityConfigOpt">
            </RadioGroup>
          </div>

          <div v-show="serverSecurity.use !== 'NONE'" class="space-y-3.5">
            <div class="flex items-center">
              <span class="w-33.5">{{ t('smtpPlugin.uiConfig.serviceConfig.trustCertificate') }}:</span>
              <RadioGroup
                v-model:value="serverSecurity.trustAllCerts"
                :options="certificateOpt"
                @change="trustCertChange">
              </RadioGroup>
              <div v-show="serverSecurity.use === 'USE_START_TLS'">
                <Checkbox v-model:checked="serverSecurity.enforceStartTLS" /> <span>{{ t('smtpPlugin.uiConfig.serviceConfig.enforceStartTLS') }}</span>
              </div>
            </div>
            <div v-show="!serverSecurity.trustAllCerts" class="flex">
              <span class="w-33.5">{{ t('smtpPlugin.uiConfig.serviceConfig.localCertificate') }} ：</span>
              <div class="flex-1 space-y-2">
                <div class="flex items-center">
                  <RadioGroup v-model:value="localCertType" :options="certificateTypeOpt">
                  </RadioGroup>
                  <Hints
                    v-if="localCertType==='path'"
                    :text="t('smtpPlugin.uiConfig.serviceConfig.localCertificateHints')"
                    class="inline-flex self-end"></Hints>
                  <Hints
                    v-if="localCertType==='file'"
                    :text="t('smtpPlugin.uiConfig.serviceConfig.localCertificateBase64Content')"
                    class="inline-flex self-end"></Hints>
                </div>
                <Upload
                  v-if="localCertType === 'file' && !serverSecurity.trustStoreBase64Content"
                  ref="certUploadRef"
                  accept=""
                  type="file"
                  :error="validated"
                  @change="onCertFileChange" />
                <div>
                  <Input
                    v-show="localCertType === 'path'"
                    v-model:value="serverSecurity.trustStorePath"
                    :error="validated && !serverSecurity.trustStorePath" />
                </div>
                <div v-show="localCertType === 'file' && serverSecurity.trustStoreBase64Content" class="flex items-center space-x-2">
                  <Input
                    v-model:value="serverSecurity.trustStoreBase64Content"
                    type="textarea"
                    disabled />
                  <Icon
                    icon="icon-qingchu"
                    class="text-4 cursor-pointer hover:text-status-error"
                    @click="delCertFile" />
                </div>
              </div>
            </div>
            <div v-show="!serverSecurity.trustAllCerts" class="flex items-center">
              <span class="w-33.5">{{ serverSecurity.use === 'USE_START_TLS' ? t('smtpPlugin.uiConfig.serviceConfig.tlsProtocols') : t('smtpPlugin.uiConfig.serviceConfig.overrideSystemSSLTLS') }}</span>
              <Input
                v-model:value="serverSecurity.tlsProtocols"
                class="flex-1"
                :maxlength="400" />
            </div>
          </div>
        </TabPane>
        <TabPane key="authentication" :tab="t('smtpPlugin.uiConfig.authenticationConfig.title')">
          <div class="flex items-center space-x-2">
            <Switch
              v-model:checked="server.useAuth"
              size="small"
              @change="onOpenChange" /><span>{{ t('smtpPlugin.uiConfig.authenticationConfig.enableAuthentication') }}</span>
          </div>
          <div class="flex items-center space-x-4 my-2">
            <Input
              v-model:value="server.username"
              :disabled="!server.useAuth"
              :placeholder="t('smtpPlugin.uiConfig.authenticationConfig.username')"
              :maxlength="80"
              :error="validated && server.useAuth && !server.username" />
            <Input
              v-model:value="server.password"
              :disabled="!server.useAuth"
              type="password"
              :placeholder="t('smtpPlugin.uiConfig.authenticationConfig.password')"
              :maxlength="50"
              :error="validated && server.useAuth && !server.username" />
          </div>
        </TabPane>
        <TabPane key="other" :tab="t('smtpPlugin.uiConfig.otherConfig.title')">
          <Checkbox v-model:checked="mailContent.messageSizeStatistics">{{ t('smtpPlugin.uiConfig.otherConfig.messageSizeStatistics') }}</Checkbox>
          <Checkbox v-model:checked="mailContent.enableDebugLogging">{{ t('smtpPlugin.uiConfig.otherConfig.enableDebugLogging') }}</Checkbox>
        </TabPane>
      </Tabs>
    </CollapsePanel>
  </Collapse>
</template>
<style scoped>
.ant-tabs> :deep(.ant-tabs-nav) {
  padding-top: 4px;
}

.smtp-name-input {
  flex: 0 0 calc((100% - (121px))*2/5);
}

.smtp-name-input :deep(.ant-input-disabled) {
  color: var(--content-text-content);
}

.ant-tabs> :deep(.ant-tabs-content-holder) {
  @apply w-200;
}

.child-drag-container .smtp-name-input {
  flex: 0 0 calc((100% - (131px))*2/5);
}

.ant-collapse.SMTP-collapse-container {
  line-height: 20px;
}

.ant-collapse-borderless.SMTP-collapse-container> :deep(.ant-collapse-item) {
  border: none;
  border-radius: 4px;
}

.ant-collapse.SMTP-collapse-container> :deep(.ant-collapse-item)>.ant-collapse-header {
  align-items: center;
  height: 46px;
  padding: 0 12px 0 38px;
  border-radius: 4px;
  background-color: rgba(239, 240, 243, 100%);
  line-height: 20px;
}

.SMTP-collapse-container :deep(.ant-collapse-content)>.ant-collapse-content-box {
  padding: 14px 20px 20px;
}

.ant-input-affix-wrapper:not(.ant-input-affix-wrapper-disabled) {
  background-color: #fff;
}

.http-method-select.ant-select {
  border-radius: 4px;
}

.http-method-select.ant-select :deep(.ant-select-selector) .ant-select-selection-item {
  line-height: 28px;
  text-align: center;
}

em {
  font-style: normal;
}

</style>

<style>
.white-bg-container .ant-input-affix-wrapper:not(.ant-input-affix-wrapper-disabled),
.white-bg-container .ant-input:not(.ant-input-disabled) {
  background-color: #fff;
}

.white-bg-container .ant-select:not(.ant-select-disabled) .ant-select-selector {
  background-color: #fff;
}

.white-bg-container .ant-checkbox-wrapper:not(.ant-checkbox-wrapper-disabled, .ant-checkbox-wrapper-checked) .ant-checkbox-inner {
  background-color: #fff;
}

.white-bg-container .ant-radio-wrapper:not(.ant-radio-wrapper-disabled) .ant-radio-inner {
  background-color: #fff;
}
</style>
