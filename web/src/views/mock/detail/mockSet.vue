<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Divider, Form, FormItem, Radio, RadioGroup, Switch, Tooltip } from 'ant-design-vue';
import { Card, Hints, Icon, IconCopy, Input, notification, Select } from '@xcan-angus/vue-ui';
import { appContext } from '@xcan-angus/infra';
import type { Rule } from 'ant-design-vue/es/form';

import { mock } from '@/api/tester';

interface Props {
  id:string;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

const { t } = useI18n();

const editionType = ref<string>();

const infoFormState = ref({
  serviceDomainUrl: '',
  name: ''
});

const suretyFormState = ref<{apisSecurity:{keyName:string, in:string, value:string}[]}>({
  apisSecurity: []
});

const apisCors = ref({
  allowCorsCredentials: true,
  allowCorsOrigin: '*',
  allowCorsRequestHeaders: '',
  allowCorsRequestMethods: '',
  allowExposeHeaders: '',
  enabled: true
});

const setting = ref({
  useSsl: false,
  workThreadNum: '256',
  enableNettyLog: false,
  sendRequestLog: false,
  logFileLevel: 'BASIC',
  maxContentLength: '1048576000',
  workPushbackThreadNum: '8',
  maxPushbackConnectTimeout: '5000',
  maxPushbackRequestTimeout: '-1'
});

const mockServiceInfo = ref();
const loading = ref(false);
const loadInfo = async () => {
  loading.value = true;
  const [error, { data }] = await mock.getServiceDetail(props.id);
  loading.value = false;
  if (error) { return; }
  mockServiceInfo.value = JSON.parse(JSON.stringify(data));
  infoFormState.value.name = data.name;
  infoFormState.value.serviceDomainUrl = data.serviceDomainUrl;
  suretyFormState.value.apisSecurity = data.apisSecurity?.length ? data.apisSecurity.map(item => ({ ...item, in: item.in.value })) : [];
  isOpenSurety.value = !!data.apisSecurity?.length;
  apisCors.value = JSON.parse(JSON.stringify(data.apisCors));
  const _setting = JSON.parse(JSON.stringify(data.setting));
  setting.value = { ..._setting, logFileLevel: _setting.logFileLevel.value };
};

const corsCookieOptions = [
  {
    label: 'true',
    value: true
  },
  {
    label: 'false',
    value: false
  }
];
const inOptions = ref([{ label: 'header', value: 'header' }, { label: 'query', value: 'query' }]);

const isOpenSurety = ref(false);
const apiSuretyChange = async (value:boolean) => {
  isOpenSurety.value = value;
  if (value) {
    suretyFormState.value.apisSecurity.push({
      keyName: '',
      value: '',
      in: 'header'
    });
  } else {
    suretyFormState.value.apisSecurity = [];
    if (!mockServiceInfo.value?.apisSecurity?.length) {
      return;
    }
    const _params = { id: mockServiceInfo.value.id, apisSecurity: [] };
    loading.value = true;
    const [error] = await mock.patchService(_params);
    loading.value = false;
    if (error) {
      suretyFormState.value.apisSecurity = mockServiceInfo.value.apisSecurity?.length ? mockServiceInfo.value.apisSecurity : [];
      isOpenSurety.value = !!mockServiceInfo.value.apisSecurity?.length;
      return;
    }
    loadInfo();
    notification.success(t('mock.mockDetail.mockSet.notifications.modifySuccess'));
  }
};

const infoFormRef = ref();
const settingFormRef = ref();
const apisCorsFormRef = ref();

const editServiceDomain = ref(false);
const editName = ref(false);
const editCredentials = ref(false);
const editOrigin = ref(false);
const editRequestHeaders = ref(false);
const editRequestMethods = ref(false);
const editExposeHeaders = ref(false);

const editWorkThreadNum = ref(false);
const editMaxContentLength = ref(false);
const editWorkPushbackThreadNum = ref(false);
const editMaxPushbackConnectTimeout = ref(false);
const editMaxPushbackRequestTimeout = ref(false);

const handleEdit = (key:string, type:'open' | 'cancel' | 'save', formType: 'infoForm' | 'settingForm' | 'apisCorsForm') => {
  const editMap = {
    infoForm: {
      serviceDomainUrl: editServiceDomain,
      name: editName
    },
    settingForm: {
      workThreadNum: editWorkThreadNum,
      maxContentLength: editMaxContentLength,
      workPushbackThreadNum: editWorkPushbackThreadNum,
      maxPushbackConnectTimeout: editMaxPushbackConnectTimeout,
      maxPushbackRequestTimeout: editMaxPushbackRequestTimeout
    },
    apisCorsForm: {
      allowCorsCredentials: editCredentials,
      allowCorsOrigin: editOrigin,
      allowCorsRequestHeaders: editRequestHeaders,
      allowCorsRequestMethods: editRequestMethods,
      allowExposeHeaders: editExposeHeaders
    }
  };

  const editValue = editMap[formType][key];

  if (type === 'open') {
    editValue.value = true;
  }

  if (formType === 'infoForm') {
    if (type === 'cancel') {
      infoFormState.value[key] = mockServiceInfo.value[key];
      infoFormRef.value.clearValidate([key]);
      editValue.value = false;
    }
    if (type === 'save') {
      if (infoFormState.value[key] === mockServiceInfo.value[key]) {
        editValue.value = false;
        return;
      }
      infoFormRef.value.validate([key]).then(async () => {
        const _params = key === 'serviceDomainUrl' ? { id: mockServiceInfo.value.id, serviceDomain: infoFormState.value[key] } : { id: mockServiceInfo.value.id, [key]: infoFormState.value[key] };
        loading.value = true;
        const [error] = await mock.patchService(_params);
        loading.value = false;
        if (error) {
          return;
        }
        mockServiceInfo.value[key] = _params[key];
        notification.success(t('mock.mockDetail.mockSet.notifications.modifySuccess'));
        editValue.value = false;
      }).catch();
    }
  }

  if (formType === 'settingForm') {
    if (type === 'cancel') {
      setting.value[key] = mockServiceInfo.value.setting[key];
      settingFormRef.value.clearValidate([key]);
      editValue.value = false;
    }
    if (type === 'save') {
      if (setting.value[key] === mockServiceInfo.value.setting[key]) {
        editValue.value = false;
        return;
      }
      if (setting.value[key] === '' || setting.value[key] === undefined) {
        setting.value[key] = mockServiceInfo.value.setting[key];
        editValue.value = false;
        return;
      }

      settingFormRef.value.validate([key]).then(async () => {
        const _setting = JSON.parse(JSON.stringify(mockServiceInfo.value.setting));
        const _params = { id: mockServiceInfo.value.id, setting: _setting };
        _params.setting[key] = setting.value[key];
        loading.value = true;
        const [error] = await mock.patchService(_params);
        loading.value = false;
        if (error) {
          return;
        }
        mockServiceInfo.value.setting[key] = _params.setting[key];
        notification.success(t('mock.mockDetail.mockSet.notifications.modifySuccess'));
        editValue.value = false;
      }).catch();
    }
  }

  if (formType === 'apisCorsForm') {
    if (type === 'cancel') {
      apisCors.value[key] = mockServiceInfo.value.apisCors[key];
      apisCorsFormRef.value.clearValidate([key]);
      editValue.value = false;
    }
    if (type === 'save') {
      if (apisCors.value[key] === mockServiceInfo.value.apisCors[key]) {
        editValue.value = false;
        return;
      }
      if (apisCors.value[key] === '' || apisCors.value[key] === undefined) {
        apisCors.value[key] = mockServiceInfo.value.apisCors[key];
        editValue.value = false;
        return;
      }

      apisCorsFormRef.value.validate([key]).then(async () => {
        const _apisCors = JSON.parse(JSON.stringify(mockServiceInfo.value.apisCors));
        const _params = { id: mockServiceInfo.value.id, apisCors: _apisCors };
        _params.apisCors[key] = apisCors.value[key];
        loading.value = true;
        const [error] = await mock.patchService(_params);
        loading.value = false;
        if (error) {
          return;
        }
        mockServiceInfo.value.apisCors[key] = _params.apisCors[key];
        notification.success(t('mock.mockDetail.mockSet.notifications.modifySuccess'));
        editValue.value = false;
      }).catch();
    }
  }
};

const settingFlagChange = (key:string, value?:string | boolean) => {
  const _setting = JSON.parse(JSON.stringify(mockServiceInfo.value.setting));
  const _params = { id: mockServiceInfo.value.id, setting: _setting };
  if (['useSsl', 'logFileLevel', 'enableNettyLog', 'sendRequestLog'].includes(key)) {
    _params.setting[key] = value;
  }

  updateSetting(_params, key);
};

const updateSetting = async (_params, key:string) => {
  loading.value = true;
  const [error] = await mock.patchService(_params);
  loading.value = false;
  if (error) {
    return;
  }

  mockServiceInfo.value.setting[key] = _params.setting[key];
  setting.value[key] = _params.setting[key];
  notification.success(t('mock.mockDetail.mockSet.notifications.modifySuccess'));
};

const apisCorsEnabledFlagChange = (value) => {
  const _apisCors = JSON.parse(JSON.stringify(mockServiceInfo.value.apisCors));
  const _params = { id: mockServiceInfo.value.id, apisCors: _apisCors };
  _params.apisCors.enabled = value;
  updateApiCors(_params, 'enabled');
};

const updateApiCors = async (_params, key:string) => {
  loading.value = false;
  const [error] = await mock.patchService(_params);
  loading.value = false;
  if (error) {
    return;
  }

  mockServiceInfo.value.apisCors[key] = _params.apisCors[key];
  apisCors.value[key] = _params.apisCors[key];
  notification.success(t('mock.mockDetail.mockSet.notifications.modifySuccess'));
};

const suretyFormRef = ref();
const saveSureTy = async () => {
  const _params = { id: mockServiceInfo.value.id, apisSecurity: suretyFormState.value.apisSecurity.length ? suretyFormState.value.apisSecurity : [] };
  loading.value = true;
  const [error] = await mock.patchService(_params);
  loading.value = false;
  if (error) {
    suretyFormState.value.apisSecurity = mockServiceInfo.value.apisSecurity?.length ? mockServiceInfo.value.apisSecurity : [];
    isOpenSurety.value = !!mockServiceInfo.value.apisSecurity?.length;
    return;
  }
  notification.success(t('mock.mockDetail.mockSet.notifications.modifySuccess'));
};

const addApisSecurityItem = () => {
  if (suretyFormState.value.apisSecurity.length === 10) {
    return;
  }
  suretyFormRef.value.validate().then(async () => {
    suretyFormState.value.apisSecurity.push({
      keyName: '',
      value: '',
      in: 'header'
    });
  }).catch(error => {
    const errNames = error.errorFields.map(item => item.name[0]);
    if (errNames.length && errNames.includes('apisSecurity')) {
      return;
    }
    suretyFormState.value.apisSecurity.push({
      keyName: '',
      value: '',
      in: 'header'
    });
  });
};

const delApisSecurityItem = (index:number) => {
  suretyFormState.value.apisSecurity.splice(index, 1);
  if (!suretyFormState.value.apisSecurity.length) {
    isOpenSurety.value = false;
  }
};

const keNameValidator = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error(t('mock.mockDetail.mockSet.validation.enterParamName')));
  } else {
    const keyNames = new Set();
    const hasDuplicates = suretyFormState.value.apisSecurity.some(obj => {
      if (keyNames.has(obj.keyName)) {
        return true;
      }
      keyNames.add(obj.keyName);
      return false;
    });
    if (hasDuplicates) {
      return Promise.reject(new Error(t('mock.mockDetail.mockSet.validation.paramNameDuplicate')));
    } else {
      suretyFormRef.value.clearValidate();
      return Promise.resolve();
    }
  }
};

onMounted(async () => {
  editionType.value = appContext.getEditionType();
  loadInfo();
});

const hasEditAuth = computed(() => {
  if (!mockServiceInfo.value) {
    return false;
  }

  if (!mockServiceInfo.value.auth) {
    return true;
  }

  return !!mockServiceInfo.value?.currentAuths?.map(item => item.value).includes('MODIFY');
});
</script>
<template>
  <Card>
    <template #title>
      <span class="text-3 font-medium">{{ t('mock.mockDetail.mockSet.basicInfo.title') }}</span>
    </template>
    <Form
      ref="infoFormRef"
      :model="infoFormState"
      :labelCol="{ style: { width: '140px' } }"
      :colon="false"
      size="small"
      layout="horizontal">
      <FormItem class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>ID</span>
            <div class="w-4.5"></div>
          </div>
        </template>
        <Input :value="mockServiceInfo?.id" disabled />
      </FormItem>
      <FormItem
        :rules="{ required: true, message: t('mock.mockDetail.mockSet.validation.enterName'), trigger: 'change' }"
        name="name"
        class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('mock.mockDetail.mockSet.basicInfo.name') }}</span>
            <Tooltip
              :title="t('mock.mockDetail.mockSet.basicInfo.nameTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon
                v-if="mockServiceInfo"
                icon="icon-tishi1"
                class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input
          v-model:value="infoFormState.name"
          :maxlength="100"
          allowClear
          :disabled="!editName">
          <template v-if="hasEditAuth" #suffix>
            <template v-if="editName">
              <a class="text-text-link text-3 leading-3" @click="handleEdit('name','cancel','infoForm')">{{ t('mock.mockDetail.mockSet.buttons.cancel') }}</a>
              <Divider type="vertical" />
              <a class="text-text-link text-3 leading-3" @click="handleEdit('name','save','infoForm')">{{ t('mock.mockDetail.mockSet.buttons.confirm') }}</a>
            </template>
            <template v-else>
              <Icon
                icon="icon-shuxie"
                class="text-text-link text-3 cursor-pointer"
                @click="handleEdit('name','open','infoForm')" />
            </template>
          </template>
        </Input>
      </FormItem>
      <FormItem
        :rules="{ required: true, message: t('mock.mockDetail.mockSet.validation.enterDomain'), trigger: 'change' }"
        name="serviceDomainUrl"
        class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('mock.mockDetail.mockSet.basicInfo.domain') }}</span>
            <Tooltip
              :title="t('mock.mockDetail.mockSet.basicInfo.domainTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input
          v-model:value="infoFormState.serviceDomainUrl"
          allowClear
          :disabled="!editServiceDomain">
          <template v-if="editionType !== 'CLOUD_SERVICE' && hasEditAuth" #suffix>
            <template v-if="editServiceDomain">
              <a class="text-text-link text-3 leading-3" @click="handleEdit('serviceDomainUrl','cancel','infoForm')">{{ t('mock.mockDetail.mockSet.buttons.cancel') }}</a>
              <Divider type="vertical" />
              <a class="text-text-link text-3 leading-3" @click="handleEdit('serviceDomainUrl','save','infoForm')">{{ t('mock.mockDetail.mockSet.buttons.confirm') }}</a>
            </template>
            <template v-else>
              <Icon
                icon="icon-shuxie"
                class="text-text-link text-3 cursor-pointer"
                @click="handleEdit('serviceDomainUrl','open','infoForm')" />
            </template>
          </template>
        </Input>
        <IconCopy :copyText="infoFormState.serviceDomainUrl" class="absolute -right-5 top-2.25 text-3" />
      </FormItem>
      <FormItem class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('api.mockDetail.mockSet.basicInfo.port') }}</span>
            <Tooltip
              :title="t('api.mockDetail.mockSet.basicInfo.portTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input :value="mockServiceInfo?.servicePort" disabled />
      </FormItem>
      <FormItem class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('api.mockDetail.mockSet.basicInfo.node') }}</span>
            <Tooltip
              :title="t('api.mockDetail.mockSet.basicInfo.nodeTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input :value="`${mockServiceInfo?.nodeName} ( ${mockServiceInfo?.nodeIp} )`" disabled />
      </FormItem>
    </Form>
  </Card>
  <Card class="mt-2">
    <template #title>
      <div class="flex items-start">
        <span class="text-3 flex-none  font-medium">{{ t('api.mockDetail.mockSet.basicInfo.serviceConfig') }}</span>
        <Hints class="mt-2.75 ml-2" :text="t('api.mockDetail.mockSet.basicInfo.serviceConfigTooltip')" />
      </div>
    </template>
    <Form
      ref="settingFormRef"
      :model="setting"
      :labelCol="{ style: { width: '140px' } }"
      :colon="false"
      size="small"
      layout="horizontal">
      <FormItem name="useSsl" class="w-150 hidden">
        <template #label>
          <div class="flex items-center" @click.prevent>
            <span>{{ t('api.mockDetail.mockSet.basicInfo.useSsl') }}</span>
            <Tooltip
              :title="t('api.mockDetail.mockSet.basicInfo.useSslTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Switch
          size="small"
          class="w-8"
          :disabled="!hasEditAuth || loading"
          :checked="setting.useSsl"
          @change="(value)=>settingFlagChange('useSsl',value)" />
      </FormItem>
      <FormItem name="workThreadNum" class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('api.mockDetail.mockSet.basicInfo.workThreadNum') }}</span>
            <Tooltip
              :title="t('api.mockDetail.mockSet.basicInfo.workThreadNumTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input
          v-model:value="setting.workThreadNum"
          dataType="number"
          :min="1"
          :max="10000"
          :disabled="!editWorkThreadNum">
          <template v-if="hasEditAuth" #suffix>
            <template v-if="editWorkThreadNum">
              <a class="text-text-link text-3 leading-3" @click="handleEdit('workThreadNum','cancel','settingForm')">{{ t('actions.cancel') }}</a>
              <Divider type="vertical" />
              <a class="text-text-link text-3 leading-3" @click="handleEdit('workThreadNum','save','settingForm')">{{ t('actions.confirm') }}</a>
            </template>
            <template v-else>
              <Icon
                icon="icon-shuxie"
                class="text-text-link text-3 cursor-pointer"
                @click="handleEdit('workThreadNum','open','settingForm')" />
            </template>
          </template>
        </Input>
      </FormItem>
      <!-- <template v-if="editionType !== 'CLOUD_SERVICE'"> -->
      <!-- <FormItem
        name="enableNettyLog"
        class="w-150">
        <template #label>
          <div class="flex items-center">
                    <span>{{ t('mock.mockDetail.mockSet.settings.enableNettyLog') }}</span>
        <Tooltip
          :title="t('mock.mockDetail.mockSet.settings.enableNettyLogTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Switch
          size="small"
          class="w-8"
           :disabled="!hasEditAuth"
          :checked="setting.enableNettyLog"
          @change="settingFlagChange('enableNettyLog',$event)" />
      </FormItem> -->
      <FormItem name="logFileLevel" class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('api.mockDetail.mockSet.basicInfo.logLevel') }}</span>
            <Tooltip
              :title="t('api.mockDetail.mockSet.basicInfo.logLevelTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <RadioGroup
          :value="setting.logFileLevel"
          class="mt-0.75"
          :disabled="!hasEditAuth"
          @change="settingFlagChange('logFileLevel',$event.target.value)">
          <Radio value="NONE">
            None
            <Tooltip
              :title="t('api.mockDetail.mockSet.basicInfo.logFileLevel.none')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '400px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer -mt-0.5" />
            </Tooltip>
          </Radio>
          <Radio value="BASIC">
            Basic
            <Tooltip
              :title="t('api.mockDetail.mockSet.basicInfo.logFileLevel.basic')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '400px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer -mt-0.5" />
            </Tooltip>
          </Radio>
          <Radio value="HEADERS">
            Headers
            <Tooltip
              :title="t('api.mockDetail.mockSet.basicInfo.logFileLevel.headers')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '400px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer -mt-0.5" />
            </Tooltip>
          </Radio>
          <Radio value="FULL">
            Full
            <Tooltip
              :title="t('api.mockDetail.mockSet.basicInfo.logFileLevel.full')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '400px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer -mt-0.5" />
            </Tooltip>
          </Radio>
        </RadioGroup>
      </FormItem>
      <!-- </template> -->
      <FormItem name="sendRequestLog" class="w-150">
        <template #label>
          <div class="flex items-center" @click.prevent>
            <span>{{ t('api.mockDetail.mockSet.basicInfo.sendRequestLog') }}</span>
            <Tooltip
              :title="t('api.mockDetail.mockSet.basicInfo.sendRequestLogTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Switch
          size="small"
          class="w-8"
          :disabled="!hasEditAuth || loading"
          :checked="setting.sendRequestLog"
          @change="settingFlagChange('sendRequestLog',$event)" />
      </FormItem>
      <FormItem name="maxContentLength" class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('api.mockDetail.mockSet.basicInfo.maxContentLength') }}</span>
            <Tooltip
              :title="t('api.mockDetail.mockSet.basicInfo.maxContentLengthTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input
          v-model:value="setting.maxContentLength"
          :disabled="!editMaxContentLength"
          :max="1000*1024*1024"
          dataType="number">
          <template v-if="hasEditAuth" #suffix>
            <template v-if="editMaxContentLength">
              <a class="text-text-link text-3 leading-3" @click="handleEdit('maxContentLength','cancel','settingForm')">{{ t('actions.cancel') }}</a>
              <Divider type="vertical" />
              <a class="text-text-link text-3 leading-3" @click="handleEdit('maxContentLength','save','settingForm')">{{ t('actions.confirm') }}</a>
            </template>
            <template v-else>
              <Icon
                icon="icon-shuxie"
                class="text-text-link text-3 cursor-pointer"
                @click="handleEdit('maxContentLength','open','settingForm')" />
            </template>
          </template>
        </Input>
      </FormItem>
      <FormItem name="workPushbackThreadNum" class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('api.mockDetail.mockSet.basicInfo.pushbackThreadNum') }}</span>
            <Tooltip
              :title="t('api.mockDetail.mockSet.basicInfo.pushbackThreadNumTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '1000px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input
          v-model:value="setting.workPushbackThreadNum"
          :disabled="!editWorkPushbackThreadNum"
          :max="10000"
          dataType="number">
          <template v-if="hasEditAuth" #suffix>
            <template v-if="editWorkPushbackThreadNum">
              <a class="text-text-link text-3 leading-3" @click="handleEdit('workPushbackThreadNum','cancel','settingForm')">{{ t('actions.cancel') }}</a>
              <Divider type="vertical" />
              <a class="text-text-link text-3 leading-3" @click="handleEdit('workPushbackThreadNum','save','settingForm')">{{ t('actions.confirm') }}</a>
            </template>
            <template v-else>
              <Icon
                icon="icon-shuxie"
                class="text-text-link text-3 cursor-pointer"
                @click="handleEdit('workPushbackThreadNum','open','settingForm')" />
            </template>
          </template>
        </Input>
      </FormItem>
      <FormItem name="maxPushbackConnectTimeout" class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('api.mockDetail.mockSet.basicInfo.pushbackConnectTimeout') }}</span>
            <Tooltip
              :title="t('api.mockDetail.mockSet.basicInfo.pushbackConnectTimeoutTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '1000px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input
          v-model:value="setting.maxPushbackConnectTimeout"
          :disabled="!editMaxPushbackConnectTimeout"
          :max="2147483647"
          dataType="number"
          includes="-">
          <template v-if="hasEditAuth" #suffix>
            <template v-if="editMaxPushbackConnectTimeout">
              <a class="text-text-link text-3 leading-3" @click="handleEdit('maxPushbackConnectTimeout','cancel','settingForm')">{{ t('actions.cancel') }}</a>
              <Divider type="vertical" />
              <a class="text-text-link text-3 leading-3" @click="handleEdit('maxPushbackConnectTimeout','save','settingForm')">{{ t('actions.confirm') }}</a>
            </template>
            <template v-else>
              <Icon
                icon="icon-shuxie"
                class="text-text-link text-3 cursor-pointer"
                @click="handleEdit('maxPushbackConnectTimeout','open','settingForm')" />
            </template>
          </template>
        </Input>
      </FormItem>
      <FormItem name="maxPushbackRequestTimeout" class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('api.mockDetail.mockSet.basicInfo.pushbackRequestTimeout') }}</span>
            <Tooltip
              :title="t('api.mockDetail.mockSet.basicInfo.pushbackRequestTimeoutTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '1000px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input
          v-model:value="setting.maxPushbackRequestTimeout"
          :disabled="!editMaxPushbackRequestTimeout"
          :max="2147483647"
          dataType="number"
          includes="-">
          <template v-if="hasEditAuth" #suffix>
            <template v-if="editMaxPushbackRequestTimeout">
              <a class="text-text-link text-3 leading-3" @click="handleEdit('maxPushbackRequestTimeout','cancel','settingForm')">{{ t('actions.cancel') }}</a>
              <Divider type="vertical" />
              <a class="text-text-link text-3 leading-3" @click="handleEdit('maxPushbackRequestTimeout','save','settingForm')">{{ t('actions.confirm') }}</a>
            </template>
            <template v-else>
              <Icon
                icon="icon-shuxie"
                class="text-text-link text-3 cursor-pointer"
                @click="handleEdit('maxPushbackRequestTimeout','open','settingForm')" />
            </template>
          </template>
        </Input>
      </FormItem>
    </Form>
  </Card>
  <Card class="mt-2">
    <template #title>
      <div class="flex items-start">
        <span class="text-3 flex-none font-medium">{{ t('api.mockDetail.mockSet.basicInfo.surety') }}</span>
        <Hints class="mt-2.75 ml-2" :text="t('api.mockDetail.mockSet.basicInfo.suretyHints')" />
      </div>
    </template>
    <Form
      ref="suretyFormRef"
      :model="suretyFormState"
      :labelCol="{ style: { width: '140px' } }"
      :colon="false"
      size="small"
      layout="horizontal">
      <FormItem class="w-150">
        <template #label>
          <div class="flex items-center" @click.prevent>
            <span>{{ t('api.mockDetail.mockSet.basicInfo.surety') }}</span>
            <Tooltip
              :title="t('api.mockDetail.mockSet.basicInfo.suretyTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '1000px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Switch
          size="small"
          class="w-8"
          :disabled="!hasEditAuth || loading"
          :checked="isOpenSurety"
          @change="apiSuretyChange" />
      </FormItem>
      <div v-if="isOpenSurety" class="flex flex-col items-end -mt-1 w-150">
        <div
          v-for="(item,index) in suretyFormState.apisSecurity"
          :key="index"
          style="width: 460px;"
          class="flex relative space-x-1 flex-1">
          <FormItem
            class="flex-1"
            :name="['apisSecurity', index, 'keyName']"
            :rules="{required: true, validator: keNameValidator}">
            <Input v-model:value="item.keyName" :placeholder="t('api.mockDetail.mockSet.basicInfo.paramsName')" />
          </FormItem>
          <FormItem class="w-20">
            <Select
              v-model:value="item.in"
              :options="inOptions"
              size="small"
              :placeholder="t('api.mockDetail.mockSet.basicInfo.in')"/>
          </FormItem>
          <FormItem
            class="flex-1"
            :name="['apisSecurity', index, 'value']"
            :rules="{required: true, message: t('api.mockDetail.mockSet.basicInfo.paramsValueRule')}">
            <Input v-model:value="item.value" :placeholder="t('api.mockDetail.mockSet.basicInfo.paramsValue')" />
          </FormItem>
          <div
            class="flex items-center text-4 absolute top-1.5"
            :class="suretyFormState.apisSecurity.length-1 === index && suretyFormState.apisSecurity.length !== 10?'-right-11':'-right-7'">
            <Icon
              icon="icon-jianshao"
              class="cursor-pointer mr-1"
              @click="delApisSecurityItem(index)" />
            <template v-if="suretyFormState.apisSecurity.length-1 === index && suretyFormState.apisSecurity.length !== 10">
              <Icon
                icon="icon-tianjia"
                class="cursor-pointer"
                @click="addApisSecurityItem" />
            </template>
          </div>
        </div>
      </div>
      <FormItem v-if="isOpenSurety" label=" ">
        <Button
          size="small"
          :disabled="!hasEditAuth"
          @click="saveSureTy">
          {{ t('actions.save') }}
        </Button>
      </FormItem>
    </Form>
  </Card>
  <Card class="mt-2">
    <template #title>
      <div class="flex items-start">
        <span class="text-3 flex-none font-medium">{{ t('mock.mockDetail.mockSet.cors.title') }}</span>
        <Hints class="mt-2.75 ml-2" :text="t('mock.mockDetail.mockSet.cors.description')" />
      </div>
    </template>
    <Form
      ref="apisCorsFormRef"
      :model="apisCors"
      :labelCol="{ style: { width: '140px' } }"
      :colon="false"
      size="small"
      layout="horizontal">
      <FormItem name="enabled" class="w-150">
        <template #label>
          <div class="flex items-center" @click.prevent>
            <span>{{ t('mock.mockDetail.mockSet.cors.enabled') }}</span>
            <Tooltip
              :title="t('mock.mockDetail.mockSet.cors.enabledTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '1000px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Switch
          size="small"
          class="w-8"
          :disabled="!hasEditAuth || loading"
          :checked="apisCors.enabled"
          @change="apisCorsEnabledFlagChange" />
      </FormItem>
      <div
        :class="apisCors.enabled ? 'open-api-cors' : 'stop-api-cors'"
        class="transition-height overflow-hidden -mt-1.5"
        style="transition-duration: 400ms;">
        <FormItem name="allowCorsOrigin" class="w-150">
          <template #label>
            <div class="flex items-center">
              <span>{{ t('mock.mockDetail.mockSet.cors.allowOrigin') }}</span>
              <Tooltip
                :title="t('mock.mockDetail.mockSet.cors.allowOriginTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{'max-width': '1000px'}">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
              </Tooltip>
            </div>
          </template>
          <Input
            v-model:value="apisCors.allowCorsOrigin"
            :disabled="!editOrigin">
            <template v-if="hasEditAuth" #suffix>
              <template v-if="editOrigin">
                <a class="text-text-link text-3 leading-3" @click="handleEdit('allowCorsOrigin','cancel','apisCorsForm')">{{ t('mock.mockDetail.mockSet.buttons.cancel') }}</a>
                <Divider type="vertical" />
                <a class="text-text-link text-3 leading-3" @click="handleEdit('allowCorsOrigin','save','apisCorsForm')">{{ t('mock.mockDetail.mockSet.buttons.confirm') }}</a>
              </template>
              <template v-else>
                <Icon
                  icon="icon-shuxie"
                  class="text-text-link text-3 cursor-pointer"
                  @click="handleEdit('allowCorsOrigin','open','apisCorsForm')" />
              </template>
            </template>
          </Input>
        </FormItem>
        <FormItem name="allowCorsCredentials" class="relative w-150">
          <template #label>
            <div class="flex items-center">
              <span>{{ t('mock.mockDetail.mockSet.cors.allowCredentials') }}</span>
              <Tooltip
                :title="t('mock.mockDetail.mockSet.cors.allowCredentialsTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{'max-width': '1000px'}">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
              </Tooltip>
            </div>
          </template>
          <Select
            v-model:value="apisCors.allowCorsCredentials"
            :class="editCredentials?'api-cors-allow-cors-credentials-edit':'api-cors-allow-cors-credentials'"
            :options="corsCookieOptions"
            :disabled="!editCredentials" />
          <div class="absolute right-2 top-0.75">
            <template v-if="editCredentials">
                              <a class="text-text-link text-3 leading-3" @click="handleEdit('allowCorsCredentials','cancel','apisCorsForm')">{{ t('mock.mockDetail.mockSet.buttons.cancel') }}</a>
                <Divider type="vertical" />
                <a class="text-text-link text-3 leading-3" @click="handleEdit('allowCorsCredentials','save','apisCorsForm')">{{ t('mock.mockDetail.mockSet.buttons.confirm') }}</a>
            </template>
            <template v-else>
              <Icon
                icon="icon-shuxie"
                class="text-text-link text-3 cursor-pointer"
                @click="handleEdit('allowCorsCredentials','open','apisCorsForm')" />
            </template>
          </div>
        </FormItem>
        <FormItem name="allowCorsRequestMethods" class="relative w-150">
          <template #label>
            <div class="flex items-center">
              <span>{{ t('mock.mockDetail.mockSet.cors.allowRequestMethods') }}</span>
              <Tooltip
                :title="t('mock.mockDetail.mockSet.cors.allowRequestMethodsTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{'max-width': '1000px'}">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
              </Tooltip>
            </div>
          </template>
          <Input
            v-model:value="apisCors.allowCorsRequestMethods"
            enumKey="HttpMethod"
            :disabled="!editRequestMethods"
            :class="editRequestMethods?'api-cors-allow-cors-request-methods-edit':'api-cors-allow-cors-request-methods'">
            <template v-if="hasEditAuth" #suffix>
              <template v-if="editRequestMethods">
                <a class="text-text-link text-3 leading-3" @click="handleEdit('allowCorsRequestMethods','cancel','apisCorsForm')">{{ t('mock.mockDetail.mockSet.buttons.cancel') }}</a>
                <Divider type="vertical" />
                <a class="text-text-link text-3 leading-3" @click="handleEdit('allowCorsRequestMethods','save','apisCorsForm')">{{ t('mock.mockDetail.mockSet.buttons.confirm') }}</a>
              </template>
              <template v-else>
                <Icon
                  icon="icon-shuxie"
                  class="text-text-link text-3 cursor-pointer"
                  @click="handleEdit('allowCorsRequestMethods','open','apisCorsForm')" />
              </template>
            </template>
          </Input>
        </FormItem>
        <FormItem name="allowCorsRequestHeaders" class="relative w-150">
          <template #label>
            <div class="flex items-center">
              <span>{{ t('mock.mockDetail.mockSet.cors.allowRequestHeaders') }}</span>
              <Tooltip
                :title="t('mock.mockDetail.mockSet.cors.allowRequestHeadersTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{'max-width': '1000px'}">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
              </Tooltip>
            </div>
          </template>
          <Input v-model:value="apisCors.allowCorsRequestHeaders" :disabled="!editRequestHeaders">
            <template v-if="hasEditAuth" #suffix>
              <template v-if="editRequestHeaders">
                <a class="text-text-link text-3 leading-3" @click="handleEdit('allowCorsRequestHeaders','cancel','apisCorsForm')">{{ t('mock.mockDetail.mockSet.buttons.cancel') }}</a>
                <Divider type="vertical" />
                <a class="text-text-link text-3 leading-3" @click="handleEdit('allowCorsRequestHeaders','save','apisCorsForm')">{{ t('mock.mockDetail.mockSet.buttons.confirm') }}</a>
              </template>
              <template v-else>
                <Icon
                  icon="icon-shuxie"
                  class="text-text-link text-3 cursor-pointer"
                  @click="handleEdit('allowCorsRequestHeaders','open','apisCorsForm')" />
              </template>
            </template>
          </Input>
        </FormItem>
        <FormItem name="allowExposeHeaders" class="relative w-150">
          <template #label>
            <div class="flex items-center">
              <span>{{ t('mock.mockDetail.mockSet.cors.allowExposeHeaders') }}</span>
              <Tooltip
                :title="t('mock.mockDetail.mockSet.cors.allowExposeHeadersTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{'max-width': '1000px'}">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
              </Tooltip>
            </div>
          </template>
          <Input v-model:value="apisCors.allowExposeHeaders" :disabled="!editExposeHeaders">
            <template v-if="hasEditAuth" #suffix>
              <template v-if="editExposeHeaders">
                <a class="text-text-link text-3 leading-3" @click="handleEdit('allowExposeHeaders','cancel','apisCorsForm')">{{ t('mock.mockDetail.mockSet.buttons.cancel') }}</a>
                <Divider type="vertical" />
                <a class="text-text-link text-3 leading-3" @click="handleEdit('allowExposeHeaders','save','apisCorsForm')">{{ t('mock.mockDetail.mockSet.buttons.confirm') }}</a>
              </template>
              <template v-else>
                <Icon
                  icon="icon-shuxie"
                  class="text-text-link text-3 cursor-pointer"
                  @click="handleEdit('allowExposeHeaders','open','apisCorsForm')" />
              </template>
            </template>
          </Input>
        </FormItem>
      </div>
    </Form>
  </Card>
</template>
<style scoped>
:deep(.ant-form-item-label > label::after) {
  margin: 0 8px 0 4px;
}

:deep(.ant-divider-vertical) {
  top: 0;
  margin: 0 6px 0 4px;
}

.open-api-cors {
  height: 240px;
}

.stop-api-cors {
  height: 0;
}

.api-cors-allow-cors-credentials :deep(.ant-select-arrow) {
  right: 28px;
}

.api-cors-allow-cors-credentials :deep(.ant-select-selector) {
  padding-right: 32px;
}

.api-cors-allow-cors-credentials-edit :deep(.ant-select-arrow) {
  right: 72px;
}

.api-cors-allow-cors-credentials-edit :deep(.ant-select-selector) {
  padding-right: 86px;
}
</style>
