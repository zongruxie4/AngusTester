<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Divider, Form, FormItem, Radio, RadioGroup, Switch, Tooltip } from 'ant-design-vue';
import { Card, Hints, Icon, IconCopy, Input, notification, Select } from '@xcan-angus/vue-ui';
import { site } from '@xcan-angus/tools';
import type { Rule } from 'ant-design-vue/es/form';

import { mock } from 'src/api/tester';

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
  const [error, { data }] = await mock.loadServiceInfo(props.id);
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
    notification.success('修改成功');
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
        notification.success('修改成功');
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
        notification.success('修改成功');
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
        notification.success('修改成功');
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
  notification.success('修改成功');
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
  notification.success('修改成功');
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
  notification.success('修改成功');
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
    return Promise.reject(new Error('请输入参数名'));
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
      return Promise.reject(new Error('参数名称重复'));
    } else {
      suretyFormRef.value.clearValidate();
      return Promise.resolve();
    }
  }
};

onMounted(async () => {
  const envContent = await site.getEnvContent();
  editionType.value = envContent?.VITE_EDITION_TYPE;
  loadInfo();
});

const hasEditAuth = computed(() => {
  if (!mockServiceInfo.value) {
    return false;
  }

  if (!mockServiceInfo.value.authFlag) {
    return true;
  }

  return !!mockServiceInfo.value?.currentAuths?.map(item => item.value).includes('MODIFY');
});
</script>
<template>
  <Card>
    <template #title>
      <span class="text-3 font-medium">基本信息</span>
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
        :rules="{ required: true, message: '请输入名称', trigger: 'change' }"
        name="name"
        class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('名称') }}</span>
            <Tooltip
              title="服务标识命名信息，最多允许100个字符。"
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
              <a class="text-text-link text-3 leading-3" @click="handleEdit('name','cancel','infoForm')">取消</a>
              <Divider type="vertical" />
              <a class="text-text-link text-3 leading-3" @click="handleEdit('name','save','infoForm')">确定</a>
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
        :rules="{ required: true, message: '请输入域名', trigger: 'change' }"
        name="serviceDomainUrl"
        class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('域名') }}</span>
            <Tooltip
              title="为服务设置域名后，可以通过域名访问Mock接口，设置的域名需要被解析到所在节点的IP。"
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
              <a class="text-text-link text-3 leading-3" @click="handleEdit('serviceDomainUrl','cancel','infoForm')">取消</a>
              <Divider type="vertical" />
              <a class="text-text-link text-3 leading-3" @click="handleEdit('serviceDomainUrl','save','infoForm')">确定</a>
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
            <span>{{ t('端口') }}</span>
            <Tooltip
              title="服务所监听的端口，服务添加后不允许修改。"
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
            <span>{{ t('节点') }}</span>
            <Tooltip
              title="服务所运行的节点，服务添加后不允许修改。"
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
        <span class="text-3 flex-none  font-medium">服务配置</span>
        <Hints class="mt-2.75 ml-2" text="服务运行时主要参数配置，更多参数配置请查看官网相关文档。修改后需要重启服务生效。" />
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
            <span>{{ t('开启SSL') }}</span>
            <Tooltip
              title="在Netty Http服务器上启用SSL选项，默认为不开启。"
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
            <span>{{ t('线程数') }}</span>
            <Tooltip
              title="处理请求的线程数，最大10000，默认256。"
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
              <a class="text-text-link text-3 leading-3" @click="handleEdit('workThreadNum','cancel','settingForm')">取消</a>
              <Divider type="vertical" />
              <a class="text-text-link text-3 leading-3" @click="handleEdit('workThreadNum','save','settingForm')">确定</a>
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
            <span>{{ t('开启Netty日志') }}</span>
            <Tooltip
              title="建议仅在调试模式下打开，默认为不开启。"
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
            <span>{{ t('文件日志级别') }}</span>
            <Tooltip
              title="配置请求日志信息级别，包括四个选项：NONE、BASIC、HEADERS和FULL。"
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
              title="无日志记录。"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '400px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer -mt-0.5" />
            </Tooltip>
          </Radio>
          <Radio value="BASIC">
            Basic
            <Tooltip
              title="只记录请求方法和URL以及响应状态代码和执行时间，默认。"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '400px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer -mt-0.5" />
            </Tooltip>
          </Radio>
          <Radio value="HEADERS">
            Headers
            <Tooltip
              title="记录基本信息以及请求和响应头。"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '400px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer -mt-0.5" />
            </Tooltip>
          </Radio>
          <Radio value="FULL">
            Full
            <Tooltip
              title="记录请求和响应头、正文和元数据。"
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
            <span>{{ t('记录请求日志') }}</span>
            <Tooltip
              title="是否发送Mock请求日志到服务端。"
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
            <span>{{ t('最大请求大小') }}</span>
            <Tooltip
              title="允许的最大请求大小，默认为1000*1024*1024（1000MB）。"
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
              <a class="text-text-link text-3 leading-3" @click="handleEdit('maxContentLength','cancel','settingForm')">取消</a>
              <Divider type="vertical" />
              <a class="text-text-link text-3 leading-3" @click="handleEdit('maxContentLength','save','settingForm')">确定</a>
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
            <span>{{ t('回推线程数') }}</span>
            <Tooltip
              title="处理回推请求的线程数，默认为8。"
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
              <a class="text-text-link text-3 leading-3" @click="handleEdit('workPushbackThreadNum','cancel','settingForm')">取消</a>
              <Divider type="vertical" />
              <a class="text-text-link text-3 leading-3" @click="handleEdit('workPushbackThreadNum','save','settingForm')">确定</a>
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
            <span>{{ t('回推连接超时') }}</span>
            <Tooltip
              title="回推时最大连接超时，单位毫秒，默认5000。"
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
              <a class="text-text-link text-3 leading-3" @click="handleEdit('maxPushbackConnectTimeout','cancel','settingForm')">取消</a>
              <Divider type="vertical" />
              <a class="text-text-link text-3 leading-3" @click="handleEdit('maxPushbackConnectTimeout','save','settingForm')">确定</a>
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
            <span>{{ t('回推请求超时') }}</span>
            <Tooltip
              title="回推时最大请求超时，单位毫秒，默认-1不超时。"
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
              <a class="text-text-link text-3 leading-3" @click="handleEdit('maxPushbackRequestTimeout','cancel','settingForm')">取消</a>
              <Divider type="vertical" />
              <a class="text-text-link text-3 leading-3" @click="handleEdit('maxPushbackRequestTimeout','save','settingForm')">确定</a>
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
        <span class="text-3 flex-none font-medium">接口安全</span>
        <Hints class="mt-2.75 ml-2" text="修改后需要重启服务或刷新实例后生效。" />
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
            <span>{{ t('接口安全') }}</span>
            <Tooltip
              title="用于配置访问接口所需的授权信息，开启配置后访问接口必须携带授权参数才可以访问接口。最多允许添加10个。"
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
            <Input v-model:value="item.keyName" placeholder="参数名" />
          </FormItem>
          <FormItem class="w-20">
            <Select
              v-model:value="item.in"
              :options="inOptions"
              size="small"
              placeholder="位置" />
          </FormItem>
          <FormItem
            class="flex-1"
            :name="['apisSecurity', index, 'value']"
            :rules="{required: true, message: '请输入value'}">
            <Input v-model:value="item.value" placeholder="参数值" />
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
          保存
        </Button>
      </FormItem>
    </Form>
  </Card>
  <Card class="mt-2">
    <template #title>
      <div class="flex items-start">
        <span class="text-3 flex-none font-medium">跨域设置</span>
        <Hints class="mt-2.75 ml-2" text="当通过浏览器访问Mock接口站点域名和访问Mock接口地址不一致时，会触发浏览器同源访问策略（CORS）限制导致接口无法访问，通过当前“跨域配置”可以自由制定跨域访问策略。修改后需要重启服务或刷新实例后生效。" />
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
            <span>{{ t('是否开启') }}</span>
            <Tooltip
              title="启用跨域访问限制配置，默认为不启用。"
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
              <span>{{ t('允许的域') }}</span>
              <Tooltip
                title="指定哪些网站可以参与跨来源资源共享，默认设置*，通过响应头Access-Control-Allow-Origin返回。"
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
                <a class="text-text-link text-3 leading-3" @click="handleEdit('allowCorsOrigin','cancel','apisCorsForm')">取消</a>
                <Divider type="vertical" />
                <a class="text-text-link text-3 leading-3" @click="handleEdit('allowCorsOrigin','save','apisCorsForm')">确定</a>
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
              <span>{{ t('允许访问凭证') }}</span>
              <Tooltip
                title="指定第三方站点可能能够执行特权操作并检索敏感信息，默认为true，通过响应头Access-Control-Allow-Credentials返回。"
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
              <a class="text-text-link text-3 leading-3" @click="handleEdit('allowCorsCredentials','cancel','apisCorsForm')">取消</a>
              <Divider type="vertical" />
              <a class="text-text-link text-3 leading-3" @click="handleEdit('allowCorsCredentials','save','apisCorsForm')">确定</a>
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
              <span>{{ t('允许的请求方法') }}</span>
              <Tooltip
                title="指定允许的HTTP方法，默认为GET、POST、PUT、PATCH、DELETE、OPTIONS、HEAD，通过响应头Access-Control-Allow-Methods返回。"
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
                <a class="text-text-link text-3 leading-3" @click="handleEdit('allowCorsRequestMethods','cancel','apisCorsForm')">取消</a>
                <Divider type="vertical" />
                <a class="text-text-link text-3 leading-3" @click="handleEdit('allowCorsRequestMethods','save','apisCorsForm')">确定</a>
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
              <span>{{ t('允许的请求头') }}</span>
              <Tooltip
                title="指定允许客户端使用的HTTP请求标头，默认不指定，通过响应头Access-Control-Allow-Headers返回。"
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
                <a class="text-text-link text-3 leading-3" @click="handleEdit('allowCorsRequestHeaders','cancel','apisCorsForm')">取消</a>
                <Divider type="vertical" />
                <a class="text-text-link text-3 leading-3" @click="handleEdit('allowCorsRequestHeaders','save','apisCorsForm')">确定</a>
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
              <span>{{ t('可访问的响应头') }}</span>
              <Tooltip
                title="指定允许访问响应中的那些标头字段，这些字段不包括浏览器可以访问的默认头，默认不指定，通过响应头Access-Control-Expose-Headers返回。"
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
                <a class="text-text-link text-3 leading-3" @click="handleEdit('allowExposeHeaders','cancel','apisCorsForm')">取消</a>
                <Divider type="vertical" />
                <a class="text-text-link text-3 leading-3" @click="handleEdit('allowExposeHeaders','save','apisCorsForm')">确定</a>
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
