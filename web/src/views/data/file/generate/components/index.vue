<script setup lang="ts">
import { defineAsyncComponent, ref } from 'vue';
import { Hints, Icon, Input, notification, PureCard, Select } from '@xcan-angus/vue-ui';
import { Button, Checkbox, Form, FormItem, Radio, RadioGroup, Switch, Tooltip } from 'ant-design-vue';
import type { Rule } from 'ant-design-vue/es/form';
import { useRouter } from 'vue-router';

import SelectEnum from '@/components/SelectEnum/index.vue'
import { encode } from '@/utils/secure';
import { TESTER } from '@xcan-angus/infra';
import { mock } from 'src/api/tester';
// import { API_EXTENSION_KEY } from '@/views/apis/utils';
// const { valueKey } = API_EXTENSION_KEY;

const router = useRouter();

const FormList = defineAsyncComponent(() => import('./formList.vue'));
const viewType = ref<'form' | 'code'>('form');

const formRef = ref();
const formListRef = ref();
const formState = ref({
  filename: undefined,
  rows: '1000',
  format: 'CSV',
  lineEndingType: 'UNIT_LF',
  threads: '32',
  nodeId: undefined,
  priority: '1000',
  onError: 'CONTINUE',
  location: 'DATASOURCE',
  authentication: false,
  apisSecurity: [{}],
  url: '',
  contentType: 'application/octet-stream',
  datasourceId: undefined,
  batchRows: 200,
  includeHeader: true
});

const bOptions = ref([
  { label: 'CSV', value: 'CSV' },
  { label: 'JSON', value: 'JSON' },
  { label: 'Tab-Delimited', value: 'Tab-Delimited' },
  { label: 'SQL', value: 'SQL' },
  { label: 'Custom', value: 'Custom' },
  { label: 'Excel', value: 'Excel' }
]);

const authType = ref('basic');
const basicUserName = ref();
const basicPassd = ref();
const bearerToken = ref();

const isOpenSurety = ref(false);
const inOptions = ref([{ label: 'header', value: 'header' }, { label: 'query', value: 'query' }]);
const contentTypeOpt = [{ value: 'application/octet-stream', label: 'binary' }, { value: 'multipart/form-data', label: 'multipart/form-data' }];
const formData = ref([{ value: '', type: 'file', name: 'file' }, { type: 'text' }]);

const addFormData = () => {
  formData.value.push({ type: 'text' });
};
const delFormData = (idx) => {
  formData.value.splice(idx, 1);
};

const keNameValidator = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error('请输入参数名'));
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
      return Promise.reject(new Error('keyName存在重复'));
    } else {
      formRef.value.clearValidate();
      return Promise.resolve();
    }
  }
};

const delApisSecurityItem = (index:number) => {
  formState.value.apisSecurity.splice(index, 1);
  if (!formState.value.apisSecurity.length) {
    isOpenSurety.value = false;
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
      in: 'query'
    });
  }).catch(error => {
    const errNames = error.errorFields.map(item => item.name[0]);
    if (errNames.length && errNames.includes('apisSecurity')) {
      return;
    }
    formState.value.apisSecurity.push({
      name: '',
      value: '',
      in: 'query'
    });
  });
};

const onBatchRowsBlur = () => {
  const value = formState.value.batchRows.trim();
  if (!value) {
    formState.value.batchRows = 1;
  }
};

const onThreadsBlur = () => {
  const value = formState.value.threads.trim();
  if (!value) {
    formState.value.threads = 1;
  }
};

const onRowsBlur = () => {
  const value = formState.value.rows.trim();
  if (!value) {
    formState.value.rows = 1;
  }
};

const onFormDataNameBlur = (idx) => {
  if (idx === 0) {
    if (!formData.value[0].name?.trim()) {
      formData.value[0].name = 'file';
    }
  }
};
const nameFormRef = ref();

const generateData = async () => {
  Promise.all([nameFormRef.value.validate(), formRef.value.validate()])
    .then(async () => {
      const configuration = {
        onError: {
          action: formState.value.onError
        },
        nodeSelectors: {
          nodeInfo: {
            nodeId: formState.value.nodeId
          }
        },
        priority: formState.value.priority,
        thread: {
          threads: formState.value.threads
        }
      };

      const mockData = {
        name: formState.value.filename,
        fields: formListRef.value.getData(),
        settings: {
          location: formState.value.location,
          format: formState.value.format,
          rows: formState.value.rows
        }
      };
      if (formState.value.format === 'CSV') {
        mockData.settings.lineEnding = formState.value.lineEndingType;
        mockData.settings.includeHeader = formState.value.includeHeader;
      }
      if (formState.value.location === 'DATASOURCE') {
        mockData.settings.batchRows = formState.value.batchRows;
        mockData.settings.storeDatasource = {
          datasourceId: formState.value.datasourceId
        };
      }

      if (formState.value.location === 'LOCAL') {
        mockData.settings.batchRows = formState.value.batchRows;
      }

      if (['DATASPACE', 'PUSH_THIRD'].includes(formState.value.location)) {
        mockData.settings.storeRequest = {
          parameters: formState.value.apisSecurity.filter(i => !!i.name),
          url: formState.value.url,
          body: {
            contentType: formState.value.contentType
          }
        };
        if (formState.value.contentType === 'multipart/form-data') {
          mockData.settings.storeRequest.body.forms = formData.value.filter(i => !!i.name);
        }
        if (formState.value.authentication) {
          if (authType.value === 'basic') {
            mockData.settings.storeRequest.authentication = {
              type: 'http',
              value: 'basic ' + encode(basicUserName.value, basicPassd.value)
            };
          }
          if (authType.value === 'Bearer') {
            mockData.settings.storeRequest.authentication = {
              type: 'http',
              value: bearerToken.value?.startsWith('Bearer') ? bearerToken.value : 'Bearer ' + bearerToken.value
            };
          }
        }
      }

      const [error] = await mock.mockData({ configuration, mockData });
      if (error) {
        return;
      }
      notification.success('已生成数据');
    });
};

const cancel = () => {
  router.push('/data');
};

</script>
<template>
  <div class="flex flex-col space-y-2 h-full">
    <div class="flex">
      <Form
        ref="nameFormRef"
        :model="formState"
        class="flex-1">
        <FormItem
          name="filename"
          :rules="{required: true, message: '请输入文件名称'}">
          <Input
            v-model:value="formState.filename"
            placeholder="输入生成数据的文件名称，限制20字符内"
            :maxLength="20"
            class="mr-2" />
        </FormItem>
      </Form>
      <Tooltip
        :title="viewType === 'code'?'表单模式' : '代码模式'"
        placement="top"
        arrowPointAtCenter>
        <Button type="primary" class="h-6.75 w-6.75 ml-2 text-center p-0">
          <Icon :icon="viewType === 'code'?'icon-daimashitu':'icon-yemianshitu'" class="text-4" />
        </Button>
      </Tooltip>
    </div>
    <PureCard class="flex p-3.5 !shadow-none flex-1">
      <div class="flex-1 flex flex-col justify-between">
        <FormList ref="formListRef" class="flex-1" />
        <div class="-mb-5">
          <Form
            :model="formState"
            size="small">
            <div class="flex space-x-3.5">
              <FormItem label="行数" class="w-60">
                <div class="flex items-center">
                  <Input
                    v-model:value="formState.rows"
                    dataType="number"
                    :min="1"
                    :max="100000000000"
                    @blur="onRowsBlur" />
                  <Tooltip title="行数最大支持千亿行">
                    <Icon icon="icon-tishi1" class="text-blue-tips text-3.5 ml-1" />
                  </Tooltip>
                </div>
              </FormItem>
              <FormItem label="格式" class="w-60">
                <Select v-model:value="formState.format" :options="bOptions" />
              </FormItem>
              <template v-if="formState.format=== 'CSV'">
                <FormItem label="行尾" class="w-60">
                  <SelectEnum
                    v-model:value="formState.lineEndingType"
                    enumKey="LineEndingType" />
                </FormItem>
                <FormItem>
                  <Checkbox :checkd="formState.includeHeader">Include Header</Checkbox>
                </FormItem>
              </template>
            </div>
          </Form>
        </div>
      </div>
      <div class="w-150 border-l border-border-divider ml-3.5 pl-3.5">
        <Form
          ref="formRef"
          :model="formState"
          layout="vertical"
          size="small">
          <FormItem label="执行线程数">
            <Hints text="默认对应节点核数*2个线程，范围：1-1000。" class="mb-2" />
            <Input
              v-model:value="formState.threads"
              dataType="number"
              :min="1"
              :max="1000"
              @blur="onThreadsBlur" />
          </FormItem>
          <FormItem label="选择Mock节点" :rules="{ required: true, message:'请选择节点' }">
            <Select
              v-model:value="formState.nodeId"
              :action="`${TESTER}/node?fullTextSearch=true`"
              :lazy="false"
              defaultActiveFirstOption
              :fieldNames="{value: 'id', label: 'name'}" />
          </FormItem>
          <FormItem label="执行优先级">
            <Hints text="优先级高的最先被执行，取值范围：0~2147483647,值越大优先级越高。" class="mb-2" />
            <Input
              v-model:value="formState.priority"
              dataType="number"
              :max="2147483647"
              :min="0" />
          </FormItem>
          <FormItem label="遇到错误">
            <RadioGroup v-model:value="formState.onError">
              <Radio value="CONTINUE">继续</Radio>
              <Radio value="STOP">停止</Radio>
              <Radio value="STOP_NOW">立即终止</Radio>
            </RadioGroup>
          </FormItem>
          <FormItem label="存储方式">
            <RadioGroup v-model:value="formState.location">
              <Radio value="DATASPACE">空间</Radio>
              <Radio value="DATASOURCE">数据源</Radio>
              <Radio value="PUSH_THIRD">三方系统</Radio>
              <!-- @TODO 本地只有私有部署才有打包注意区分 -->
              <Radio value="LOCAL">本地</Radio>
            </RadioGroup>
          </FormItem>
          <template v-if="['DATASOURCE', 'LOCAL'].includes(formState.location)">
            <div class="flex items-center mb-2">
              <span class="w-20">批量写入数</span>
              <Input
                v-model:value="formState.batchRows"
                class="w-80"
                dataType="number"
                :max="10000"
                :min="1"
                @blur="onBatchRowsBlur" />
              <Tooltip title="最大支持10000">
                <Icon icon="icon-tishi1" class="text-blue-tips text-3.5 ml-1" />
              </Tooltip>
            </div>
            <template v-if="'DATASOURCE' === formState.location">
              <div class="flex items-center">
                <span class="w-20">选择数据源</span>
                <FormItem
                  name="datasourceId"
                  class="w-80"
                  :rules="{ required: true, message: '请选择数据源' }">
                  <Select
                    v-model:value="formState.datasourceId"
                    defaultActiveFirstOption
                    :lazy="false"
                    :action="`${TESTER}/mock/datasource`"
                    :fieldNames="{value: 'id', label: 'name'}" />
                </FormItem>
              </div>
            </template>
            <template v-if="'LOCAL' === formState.location">
              <div class="flex items-center">
                <span class="w-20">存储路径</span>
                <Input
                  class="w-80"
                  value="${AGENT_HOME}/mock/${TaskId}/data.${format}"
                  disabled />
              </div>
            </template>
          </template>
          <template v-if="['DATASPACE', 'PUSH_THIRD'].includes(formState.location)">
            <div class="flex">
              <Select
                disabled
                value="POST"
                :options="[{value: 'POST', label: 'POST'}]" />
              <FormItem
                class="flex-1"
                name="url"
                :rules="{ required: true, message: '请输入接口地址' }">
                <Input
                  v-model:value="formState.url"
                  :disabled="formState.location === 'DATASPACE'"
                  placeholder="接口地址" />
              </FormItem>
            </div>
            <div class="flex flex-col ">
              <div class="font-semibold mt-2 flex items-center">
                请求参数
                <template v-if="formState.apisSecurity.length <= 9">
                  <Icon
                    icon="icon-tianjia"
                    class="cursor-pointer ml-2"
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
                  <Input v-model:value="item.name" placeholder="参数名" />
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
                <FormItem
                  class="flex items-center text-4"
                  :class="formState.apisSecurity.length-1 === index && formState.apisSecurity.length !== 9?'-right-11':'-right-7'">
                  <Icon
                    icon="icon-jianshao"
                    class="cursor-pointer mr-1"
                    @click="delApisSecurityItem(index)" />
                </FormItem>
              </div>
            </div>
            <div>
              <div class="font-semibold mt-2">请求体</div>
              <div class="flex items-center space-x-2">
                <span>Content-Type</span>
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
                    placeholder="参数名称"
                    class="flex-1"
                    @blur="onFormDataNameBlur(idx)" />
                  <Select
                    v-model:value="form.type"
                    disabled
                    class="w-20" />
                  <Input
                    v-model:value="form.value"
                    :placeholder="idx === 0 ? '文件位置' : '参数值'"
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
                <span class="font-semibold">认证</span>
                <Switch
                  v-model:checked="formState.authentication"
                  class="ml-2"
                  size="small" />
                <template v-if="formState.authentication">
                  <RadioGroup
                    v-model:value="authType"
                    size="small"
                    class="ml-5"
                    :options="[{value: 'basic', label: 'basic Auth'}, {value: 'Bearer', label: 'Bearer Token'}]">
                  </RadioGroup>
                </template>
              </div>
              <template v-if="formState.authentication">
                <template v-if="authType === 'basic'">
                  <div class="flex mt-1"><span class="w-20">用户名</span> <Input v-model:value="basicUserName" class="w-80" /></div>
                  <div class="flex mt-1"><span class="w-20">密码</span> <Input v-model:value="basicPassd" class="w-80" /></div>
                </template>
                <template v-if="authType === 'Bearer'">
                  <div class="flex mt-1"><span class="w-20">令牌</span> <Input v-model:value="bearerToken" class="w-80" /></div>
                </template>
              </template>
            </div>
          </template>
        </Form>
      </div>
    </PureCard>
    <div class="flex space-x-3.5">
      <Button
        type="primary"
        size="small"
        @click="generateData">
        生成数据
      </Button>
      <!-- <Button type="primary" size="small">预览</Button> -->
      <!-- <Button size="small">保存</Button> -->
      <Button size="small" @click="cancel">取消</Button>
    </div>
  </div>
</template>
