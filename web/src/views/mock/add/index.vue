<script setup lang='ts'>
import { computed, inject, onMounted, reactive, ref } from 'vue';
import { Icon, Input, notification, PureCard, Select, SelectEnum, Tooltip } from '@xcan-angus/vue-ui';
import { Button, Card, Form, FormItem, Popover, Upload } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import type { Rule } from 'ant-design-vue/es/form';
import { TESTER, site } from '@xcan-angus/tools';
import postmanToOpenApi from '@xcan-angus/postman-to-openapi';

import { formatBytes } from '@/utils/common';
import { mock, node } from 'src/api/tester';
import store from '@/store';

import ApiList from './apiList.vue';
import { textList } from '../data';
import { ImportExistForm } from './PropsType';

const projectInfo = inject('projectInfo', ref({ id: '' }));
const projectId = computed(() => {
  return projectInfo?.value?.id;
});

const formRef = ref();
const router = useRouter();

const isPrivate = ref(false);

const activeTab = ref<number>(0);
const changeActiveTab = (num: number) => {
  activeTab.value = num;
  formRef.value.clearValidate();
  formState.value.serviceId = '';
};

const state = reactive<{
  saving: boolean,
  nodeOptions: {id: string, name: string, ip: string}[],

}>({
  saving: false,
  nodeOptions: []
});

const formState = ref<ImportExistForm>({
  name: '',
  serviceDomain: '',
  servicePort: '',
  nodeId: undefined,
  serviceId: '',
  apiIds: [],
  importType: 'OpenAPI',
  file: undefined,
  text: ''
});

const domainRegex = /^(?=.{1,253}$)([a-z0-9]|[a-z0-9][a-z0-9\\-]{0,61}[a-z0-9])\.angusmock\.cloud$/;
const serviceDomainValidate = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error('请输入域名'));
  } else if (!domainRegex.test(value + '.angusmock.cloud')) {
    return Promise.reject(new Error('请输入正确的域名'));
  } else {
    return Promise.resolve();
  }
};

const rules = computed(() => {
  const baseRule = {
    name: [{ required: true, message: '请输入名称', trigger: 'change' }],
    servicePort: [{ required: true, message: '请输入端口（1~65535）', trigger: 'change' }],
    nodeId: [{ required: true, message: '请选择节点', trigger: 'change' }]
  };

  const privateRule = {
    ...baseRule
  };

  let publicRule:Record<string, any> = {
    ...baseRule,
    serviceDomain: [{ required: true, validator: serviceDomainValidate, trigger: 'change' }]
  };

  if (activeTab.value === 1) {
    publicRule = {
      ...publicRule,
      serviceId: [{ required: true, message: '请选择项目服务', trigger: 'change' }]
    };
  }

  return isPrivate.value ? privateRule : publicRule;
});

const loadNodes = async () => {
  const params = {
    pageNo: 1,
    pageSize: store.state.maxPageSize,
    filters: [{ key: 'role', op: 'EQUAL', value: 'MOCK_SERVICE' }]
  };
  const [error, res] = await node.loadNodes(params);
  if (error) { return; }
  state.nodeOptions = res.data.list.map(item => ({ value: item.id, label: `${item.name}(${item.ip})` }));
};

const selectProjectId = ref();
const treeSelectChange = (id: string, item) => {
  if (item.mockServiceId) {
    selectProjectId.value = undefined;
  } else {
    formState.value.serviceId = id;
  }
};

const sizeErr = ref(false);
const fileErr = ref(false);
const checkFile = () => {
  fileErr.value = !formState.value.file && !formState.value.text;
};

const uploadFileDrop = (event) => {
  event.preventDefault();
  const files = event.dataTransfer.files;
  customRequest(files[0]);
};

const preventDefault = (event) => {
  event.preventDefault();
};

const handlePaste = (event) => {
  const _text = event.clipboardData.getData('text');
  const encoder = new TextEncoder();
  const encodedString = encoder.encode(_text);
  const byteSize = encodedString.byteLength;
  const maxSizeInMB = 20;
  const maxSizeInBytes = maxSizeInMB * 1024 * 1024;

  if (byteSize > maxSizeInBytes) {
    sizeErr.value = true;
    fileErr.value = false;
  } else {
    formState.value.text = _text;
    formState.value.file = undefined;
    fileErr.value = false;
    sizeErr.value = false;
  }
};

const delUploadText = () => {
  formState.value.text = '';
};

const customRequest = (_file) => {
  if (_file.size > 20 * 1024 * 1024) {
    sizeErr.value = true;
    fileErr.value = false;
  } else {
    formState.value.file = _file;
    formState.value.text = '';
    sizeErr.value = false;
    fileErr.value = false;
    if (_file.type === 'application/json' && formState.value.importType === 'POSTMAN') {
      toOpenapi(_file);
    }
  }
};

const versionRuleErr = ref(false);
const toOpenapi = (_file:File) => {
  versionRuleErr.value = false;
  const reader = new FileReader();
  reader.onload = () => {
    try {
      const fileContent = reader.result as string;
      const openapiDoc = postmanToOpenApi(fileContent);
      const yamlFileName = _file.name.replace(/\.json$/, '') + '.yml';
      const yamlfile = new File([openapiDoc], yamlFileName, { type: 'text/yaml' });
      formState.value.file = yamlfile;
      formState.value.text = '';
    } catch (error) {
      versionRuleErr.value = true;
      sizeErr.value = false;
      fileErr.value = false;
    }
  };
  reader.readAsText(_file);
};

const deleteFile = () => {
  formState.value.file = undefined;
  fileErr.value = true;
};

const loading = ref(false);
const handleSave = () => {
  formRef.value.validate().then(async () => {
    let params:any = {
      name: formState.value.name,
      serviceDomain: !isPrivate.value ? formState.value.serviceDomain + '.angusmock.cloud' : formState.value.serviceDomain,
      servicePort: formState.value.servicePort,
      nodeId: formState.value.nodeId,
      projectId: projectId.value
    };
    if (activeTab.value === 1) {
      params = { ...params, serviceId: formState.value.serviceId };
      if (formState.value.apiIds?.length) {
        params = { ...params, apiIds: formState.value.apiIds };
      }
    }

    const formData = new FormData();
    if (activeTab.value === 2) {
      checkFile();
      if (fileErr.value || sizeErr.value || versionRuleErr.value) {
        return;
      }
      formData.append('name', params.name);
      formData.append('serviceDomain', params.serviceDomain);
      formData.append('servicePort', params.servicePort);
      formData.append('nodeId', params.nodeId);
      formData.append('projectId', params.projectId);
      formData.append('importType', formState.value.importType);
      if (formState.value.file) {
        formData.append('file', formState.value.file);
      }
      if (formState.value.text) {
        formData.append('text', formState.value.text.toString());
      }
    }
    loading.value = true;
    const [error] = activeTab.value === 0 ? await mock.addService(params) : activeTab.value === 1 ? await mock.addAngusService(params) : await mock.addFileService(formData);
    loading.value = false;
    if (error) { return; }
    notification.success('添加成功');
    router.push('/mockservice');
  }, () => { /** */ });
};

const treeSelectFormat = (data) => {
  return { ...data, disabled: !!data.mockServiceId };
};

onMounted(async () => {
  isPrivate.value = await site.isPrivate();
  loadNodes();
});
</script>

<template>
  <div class="px-5 py-5 flex h-full space-x-3.5">
    <div class="flex flex-col space-y-3.5 w-80">
      <Card
        v-for="(item,index) in textList"
        :key="index"
        :class="{'selected-card': activeTab === index}"
        hoverable
        class="flex cursor-pointer relative"
        style="min-height: 144px;"
        @click="changeActiveTab(index)">
        <div class="flex items-center mb-2">
          <Icon :icon="item.icon" class="text-7 mr-2 flex-none" />
          <p class="text-3.5 leading-3.5 text-theme-title font-semibold">{{ item.name }}</p>
        </div>
        <p class="text-3 leading-5 text-theme-sub-content whitespace-break-spaces break-all">{{ item.description }}</p>
        <div class="absolute right-0 bottom-0 corner z-99"></div>
      </Card>
    </div>
    <PureCard class="flex-1 h-full py-8 px-8 overflow-auto">
      <Form
        ref="formRef"
        size="small"
        :model="formState"
        :rules="rules">
        <div class="flex">
          <div>
            <FormItem label="名称" required />
            <FormItem
              label="域名"
              :required="!isPrivate"
              :class="isPrivate?'pl-2.25':''" />
            <FormItem label="端口" required />
            <FormItem label="节点" required />
            <template v-if="activeTab===1">
              <FormItem label="服务" required />
              <template v-if="formState.serviceId">
                <FormItem label="接口" />
              </template>
            </template>
            <template v-if="activeTab===2">
              <FormItem label="导入来源" required />
            </template>
          </div>
          <div class="w-150">
            <FormItem name="name">
              <Input
                v-model:value="formState.name"
                placeholder="服务标识命名信息，最多允许100个字符"
                :maxlength="100" />
            </FormItem>
            <FormItem name="serviceDomain" class="relative">
              <Input
                v-model:value="formState.serviceDomain"
                placeholder="为服务设置域名后，可以通过域名访问Mock接口">
                <template v-if="!isPrivate" #addonAfter>
                  <span>.angusmock.cloud</span>
                </template>
              </Input>
              <template v-if="!isPrivate">
                <Tooltip
                  placement="right"
                  title="域名解析生效预计需要60秒以内的延迟。">
                  <Icon class="text-tips absolute -right-5 top-1.75 cursor-pointer text-3.5" icon="icon-tishi1" />
                </Tooltip>
              </template>
            </FormItem>
            <FormItem name="servicePort">
              <Input
                v-model:value="formState.servicePort"
                dataType="number"
                placeholder="服务所监听的端口，服务添加后不允许修改(1~65535)"
                :min="1"
                :max="65535" />
            </FormItem>
            <FormItem name="nodeId">
              <Select
                v-model:value="formState.nodeId"
                :options="state.nodeOptions"
                placeholder="服务所运行的节点，服务添加后不允许修改"
                size="small">
              </Select>
            </FormItem>
            <template v-if="activeTab===1">
              <FormItem name="serviceId">
                <Select
                  :value="selectProjectId"
                  :action="`${TESTER}/services/search?projectId=${projectId}&queryHasMockServiceFlag=true`"
                  :fieldNames="{label:'name',value:'id'}"
                  :format="treeSelectFormat"
                  showSearch
                  allowClear
                  placeholder="选择或查询服务"
                  @change="treeSelectChange">
                  <template #title="item">
                    <div class="text-3 leading-3 flex items-center h-6.5 pr-2 space-x-2" :class="item.mockServiceId?'text-text-disabled':'text-text-content'">
                      <label class="w-4 h-4 leading-4 rounded-full text-white text-center mr-1 flex-none" :class="`bg-blue-badge-${item.targetType.value === 'PROJECT'? 'p':'s'}`">{{ item.targetType.value[0] }}</label>
                      <div
                        :title="item.name"
                        class="truncate">
                        {{ item.name }}
                      </div>
                      <template v-if="item.mockServiceId">
                        <div clas="flex-none ml">(已关联)</div>
                      </template>
                    </div>
                  </template>
                </Select>
              </FormItem>
              <template v-if="formState.serviceId">
                <FormItem name="apiIds">
                  <ApiList v-model:apiIds="formState.apiIds" :serviceId="formState.serviceId" />
                </FormItem>
              </template>
            </template>
            <template v-if="activeTab===2">
              <FormItem name="importType">
                <SelectEnum
                  v-model:value="formState.importType"
                  size="small"
                  enumKey="ApiImportSource" />
              </FormItem>
              <FormItem name="file">
                <div
                  style="min-height: 90px;border-color: rgba(0, 119, 255);background-color: rgba(0, 119, 255, 4%);"
                  class="border p-3.5 rounded border-dashed  text-3"
                  :class="fileErr || sizeErr || versionRuleErr?'border-rule':'border-border-divider '"
                  @paste="handlePaste"
                  @drop.prevent="uploadFileDrop"
                  @dragover="preventDefault">
                  <template v-if="!formState.text && !formState.file">
                    <div class="flex justify-center text-center">
                      <Upload
                        :multiple="false"
                        :showUploadList="false"
                        :customRequest="({file})=>customRequest(file)"
                        class="text-3 leading-3"
                        accept=".zip,.rar,.7z,.gz,.tar,.bz2,.xz,.lzma,.json,.yaml,.yml">
                        <Icon icon="icon-shangchuan" class="text-5 leading-5 text-text-link" />
                        <div class="text-3 leading-3 mt-2 text-text-link text-center">上传文件</div>
                      </Upload>
                    </div>
                    <p class="text-3 leading-3 text-text-sub-content text-center mt-2">直接粘贴内容或拖动文件或点击上传文件，文件大小不超过20M</p>
                  </template>
                  <template v-if="formState.text">
                    <div
                      class="overflow-y-auto -mr-3.5 pr-2 mt-1 whitespace-pre -mb-3.5 pb-2"
                      style="max-height: calc(100vh - 480px);">
                      {{ formState.text }}
                    </div>
                    <a
                      class="absolute right-3.5 top-0 text-3 text-text-link"
                      @click="delUploadText">清空</a>
                  </template>
                  <div
                    v-if="formState.file"
                    class="flex items-center space-x-3.5 justify-between">
                    <Popover placement="top">
                      <template #content>
                        {{ formState.file?.name }}
                      </template>
                      <div class="min-w-1/2 truncate">{{ formState.file?.name }}</div>
                    </Popover>
                    <div class="max-w-1/2 whitespace-nowrap">{{ formatBytes(Number(formState.file?.size)) }}</div>
                    <div class="flex-none w-4">
                      <Icon
                        icon="icon-qingchu"
                        class="cursor-pointer ml-2 text-3.5"
                        @click="deleteFile" />
                    </div>
                  </div>
                </div>
                <div class="text-3 text-rule h-4">
                  <template v-if="fileErr">
                    请先上传文件
                  </template>
                  <template v-if="sizeErr">
                    上传文件或文本大小超过20M，请重新上传
                  </template>
                  <template v-if="versionRuleErr">
                    请检查文件是否是Postman Collection V2/V2.1 版本JSON文件
                  </template>
                </div>
              </FormItem>
            </template>
            <FormItem>
              <RouterLink to="/mockservice">
                <Button size="small">取消</Button>
              </RouterLink>
              <Button
                size="small"
                type="primary"
                class="ml-3"
                :loading="loading"
                @click="handleSave">
                确定
              </Button>
            </FormItem>
          </div>
        </div>
      </Form>
    </PureCard>
  </div>
</template>

<style scoped>
.corner {
  display: none;
  border-width: 12px;
  border-style: solid;
  border-color: transparent #07f #07f transparent;
}

.corner::after {
  content: "✓";
  position: absolute;
  top: 50%;
  left: 50%;
  color: #fff;
  font-size: 12px;
  line-height: 1;
}

.selected-card .corner {
  display: block;
}

:deep(.ant-upload.ant-upload-drag.error-border) {
  @apply border-border-error;
}

.upload-bg {
  background-color: rgba(0, 0, 0, 2%);
}
</style>
