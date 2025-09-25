<script setup lang="ts">
import { ref } from 'vue';
import { Input, Select, Icon, Composite, Hints } from '@xcan-angus/vue-ui';
import { Form, FormItem, Switch, RadioGroup } from 'ant-design-vue';
import type { Rule } from 'ant-design-vue/es/form';
import { encode } from '../PropsType.ts';
import { useI18n } from 'vue-i18n';

const formState = ref({
  spaceId: undefined,
  url: '',
  contentType: 'application/octet-stream',
  apisSecurity: [],
  authentication: false,
  batchRows: '1'
});

const { t } = useI18n();
const formRef = ref();
const authType = ref('Basic');
const basicUserName = ref();
const basicPassd = ref();
const bearerToken = ref();

const isOpenSurety = ref(false);
const inOptions = ref([{ label: 'header', value: 'header' }, { label: 'query', value: 'query' }]);
const contentTypeOpt = [
  { value: 'application/octet-stream', label: 'binary' },
  { value: 'multipart/form-data', label: 'multipart/form-data' },
  { value: 'text/plain', label: 'text/plain' }
];
const formData = ref([{ value: '', format: 'binary', name: 'file', enabled: true, type: 'string' }, { format: 'string', enabled: true, type: 'string' }]);

const keNameValidator = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error(t('genDataPlugin.pushThirdStoreConfig.validation.enterParamName')));
  } else {
    const keyNames = new Set();
    const hasDuplicates = formState.value.apisSecurity.some(obj => {
      if (keyNames.has(obj.name)) {
        return true;
      }
      keyNames.add(obj.name);
      return false;
    });
    if (hasDuplicates) {
      return Promise.reject(new Error(t('genDataPlugin.pushThirdStoreConfig.validation.paramNameDuplicate')));
    } else {
      formRef.value.clearValidate();
      return Promise.resolve();
    }
  }
};

const onBatchRowsBlur = () => {
  const value = formState.value.batchRows.trim();
  if (!value) {
    formState.value.batchRows = '1';
  }
};

const addFormData = () => {
  formData.value.push({ format: 'string', enabled: true, type: 'string' });
};

const delFormData = (idx) => {
  formData.value.splice(idx, 1);
};

const onFormDataNameBlur = (idx) => {
  if (idx === 0) {
    if (!formData.value[0].name?.trim()) {
      formData.value[0].name = 'file';
    }
  }
};

const addApisSecurityItem = () => {
  if (formState.value.apisSecurity.length === 10) {
    return;
  }
  formRef.value.validate().then(async () => {
    formState.value.apisSecurity.push({
      name: '',
      value: '',
      in: 'query',
      enabled: true
    });
  }).catch(error => {
    const errNames = error.errorFields.map(item => item.name[0]);
    if (errNames.length && errNames.includes('apisSecurity')) {
      return;
    }
    formState.value.apisSecurity.push({
      name: '',
      value: '',
      in: 'query',
      enabled: true
    });
  });
};

const delApisSecurityItem = (index:number) => {
  formState.value.apisSecurity.splice(index, 1);
  if (!formState.value.apisSecurity.length) {
    isOpenSurety.value = false;
  }
};

const validate = () => {
  return formRef.value.validate();
};

const getAuthenticationData = () => {
  if (authType.value === 'Basic') {
    return {
      type: 'HTTP',
      value: 'Basic ' + encode(basicUserName.value, basicPassd.value)
    };
  }
  if (authType.value === 'Bearer') {
    return {
      type: 'HTTP',
      value: bearerToken.value?.startsWith('Bearer') ? bearerToken.value : 'Bearer ' + bearerToken.value
    };
  }
};

defineExpose({
  getData: () => {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const { apisSecurity, url, contentType, authentication, batchRows } = formState.value;
    const authHeader = [];
    if (authentication) {
      const authValue = getAuthenticationData().value;
      authHeader.push({ name: 'Authorization', in: 'header', type: 'string', value: authValue });
    }
    return {
      batchRows,
      storeRequest: {
        parameters: [...apisSecurity, ...authHeader, { name: 'Content-Type', in: 'header', value: contentType, enabled: true }],
        url,
        body: {
          forms: contentType === 'multipart/form-data' ? formData.value : undefined
        }
      }
    };
  },
  getCurrentData: () => {
    return formState.value;
  },
  validate,
  setData: (data) => {
    if (data) {
      formState.value = data;
    }
  }
});
</script>
<template>
  <Form
    ref="formRef"
    class="text-3"
    layout="vertical"
    :model="formState">
    <FormItem :label="t('genDataPlugin.pushThirdStoreConfig.batchRows')">
      <Hints :text="t('genDataPlugin.pushThirdStoreConfig.hints.batchRows')" class="mb-2" />
      <div class="flex items-center">
        <Input
          v-model:value="formState.batchRows"
          dataType="number"
          :max="10000"
          :min="1"
          @blur="onBatchRowsBlur" />
        <!-- <Tooltip title="最大支持10000" placement="topLeft">
          <Icon icon="icon-tishi1" class="text-blue-tips text-3.5 ml-1" />
        </Tooltip> -->
      </div>
    </FormItem>
    <div class="flex">
      <FormItem
        class="flex-1"
        name="url"
        :rules="{ required: true, message: t('genDataPlugin.pushThirdStoreConfig.validation.enterApiAddress') }">
        <Composite>
          <Select
            value="POST"
            class="h-7 !w-20"
            :options="[{value: 'POST', label: 'POST'}]" />
          <Input
            v-model:value="formState.url"
            :placeholder="t('genDataPlugin.pushThirdStoreConfig.placeholders.apiAddress')" />
        </Composite>
      </FormItem>
    </div>
    <div class="flex flex-col ">
      <div class="mt-2 flex items-center">
        {{ t('genDataPlugin.pushThirdStoreConfig.requestParams') }}
        <template v-if="formState.apisSecurity.length <= 9">
          <Icon
            icon="icon-tianjia"
            class="cursor-pointer text-4 ml-2"
            @click="addApisSecurityItem" />
        </template>
      </div>
      <div
        v-for="(item,index) in formState.apisSecurity"
        :key="index"
        class="flex space-x-1 flex-1">
        <FormItem
          class="flex-1"
          :name="['apisSecurity', index, 'name']"
          :rules="{required: true, validator: keNameValidator}">
          <Input v-model:value="item.name" :placeholder="t('genDataPlugin.pushThirdStoreConfig.form.paramName')" />
        </FormItem>
        <FormItem class="w-20">
          <Select
            v-model:value="item.in"
            :options="inOptions"
            size="small"
            :placeholder="t('common.position')" />
        </FormItem>
        <FormItem
          class="flex-1"
          :name="['apisSecurity', index, 'value']"
          :rules="{required: true, message: t('genDataPlugin.pushThirdStoreConfig.validation.enterParamValue')}">
          <Input v-model:value="item.value" :placeholder="t('genDataPlugin.pushThirdStoreConfig.form.paramValue')" />
        </FormItem>
        <FormItem
          class="flex items-center text-4 h-7 leading-7"
          :class="formState.apisSecurity.length-1 === index && formState.apisSecurity.length !== 9?'-right-11':'-right-7'">
          <Icon
            icon="icon-jianshao"
            class="cursor-pointer mr-1"
            @click="delApisSecurityItem(index)" />
        </FormItem>
      </div>
    </div>
    <div>
      <div class="mt-2">{{ t('genDataPlugin.pushThirdStoreConfig.requestBody') }}</div>
      <div class="flex items-center space-x-2">
        <span>{{ t('genDataPlugin.pushThirdStoreConfig.contentType') }}</span>
        <RadioGroup
          v-model:value="formState.contentType"
          size="small"
          :options="contentTypeOpt">
        </RadioGroup>
        <template v-if="formState.contentType==='multipart/form-data' && formData.length < 10">
          <Icon
            icon="icon-tianjia"
            class="cursor-pointer text-3.5"
            @click="addFormData" />
        </template>
      </div>
      <div v-if="formState.contentType==='multipart/form-data'" class="mt-2 space-y-2">
        <div
          v-for="(form, idx) in formData"
          :key="form.name"
          class="flex items-center space-x-2">
          <Input
            v-model:value="form.name"
            :placeholder="t('genDataPlugin.pushThirdStoreConfig.form.paramNamePlaceholder')"
            class="flex-1"
            @blur="onFormDataNameBlur(idx)" />
          <Select
            v-model:value="form.format"
            disabled
            class="w-20" />
          <Input
            v-model:value="form.value"
            :placeholder="idx === 0 ? t('genDataPlugin.pushThirdStoreConfig.form.filePosition') : t('genDataPlugin.pushThirdStoreConfig.form.paramValue')"
            :disabled="idx === 0"
            class="flex-1" />
          <Icon
            icon="icon-jianshao"
            :class="{'invisible': idx === 0}"
            class="cursor-pointer mr-1 text-4"
            @click="delFormData(idx)" />
        </div>
      </div>
    </div>
    <div class="mt-3">
      <div class="mt-2 flex items-center">
        <span class="">{{ t('genDataPlugin.pushThirdStoreConfig.authentication') }}</span>
        <Switch
          v-model:checked="formState.authentication"
          class="ml-2"
          size="small" />
        <template v-if="formState.authentication">
          <RadioGroup
            v-model:value="authType"
            size="small"
            class="ml-5"
            :options="[{value: 'Basic', label: t('genDataPlugin.pushThirdStoreConfig.basicAuth')}, {value: 'Bearer', label: t('genDataPlugin.pushThirdStoreConfig.bearerToken')}]">
          </RadioGroup>
        </template>
      </div>
      <template v-if="formState.authentication">
        <template v-if="authType === 'Basic'">
          <div class="flex mt-1"><span class="w-20">{{ t('common.username') }}</span> <Input v-model:value="basicUserName" class="w-80" /></div>
          <div class="flex mt-1"><span class="w-20">{{ t('common.password') }}</span> <Input v-model:value="basicPassd" class="w-80" /></div>
        </template>
        <template v-if="authType === 'Bearer'">
          <div class="flex mt-1"><span class="w-20">{{ t('genDataPlugin.pushThirdStoreConfig.token') }}</span> <Input v-model:value="bearerToken" class="w-80" /></div>
        </template>
      </template>
    </div>
  </Form>
</template>
