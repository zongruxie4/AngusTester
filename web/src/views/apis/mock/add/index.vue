<script setup lang='ts'>
import { Ref, inject, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Input, notification, PureCard, Select, Tooltip } from '@xcan-angus/vue-ui';
import { Button, Card, Form, FormItem, Popover, Upload } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { TESTER, appContext } from '@xcan-angus/infra';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import { formatBytes } from '@/utils/common';
import { mock } from '@/api/tester';
import ApiList from './ApiList.vue';
import { useMockUI } from '../composables/useMockUI';
import { useMockForm, useNodeData, useFileUpload, useTabs } from './composables';

const { t } = useI18n();

const {
  textList
} = useMockUI();

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

const formRef = ref();
const router = useRouter();

const isPrivateEdition = ref(false);

// Use composables
const { activeTab, changeActiveTab } = useTabs();
const { formState, rules, checkFile, resetServiceId, clearTextContent } = useMockForm(isPrivateEdition.value, activeTab.value);
const { nodeState, loadNodes } = useNodeData();
const { sizeErr, fileErr, versionRuleErr, handleFileDrop, preventDefault, handlePaste, customRequest, deleteFile } = useFileUpload();

/**
 * Handle tab change
 * @param tabIndex - Tab index to activate
 */
const handleChangeActiveTab = (tabIndex: number) => {
  changeActiveTab(tabIndex);
  formRef.value?.clearValidate();
  if (tabIndex !== 1) {
    resetServiceId();
  }
};

const selectedProjectId = ref();

/**
 * Handle tree select change for service selection
 * @param value - Selected value
 * @param option - Selected option
 */
const handleTreeSelectChange = (value: any, option: any) => {
  if (option.mockServiceId) {
    selectedProjectId.value = undefined;
  } else {
    formState.value.serviceId = value || '';
  }
};

/**
 * Handle paste event for text content
 * @param event - Clipboard event
 */
const handlePasteEvent = (event: ClipboardEvent) => {
  const text = handlePaste(event);
  if (text) {
    formState.value.text = text;
    formState.value.file = undefined;
    fileErr.value = false;
    sizeErr.value = false;
  }
};

/**
 * Handle custom file request
 * @param file - File to process
 */
const handleCustomRequest = (file: any) => {
  // Handle different file types
  if (file instanceof File) {
    const processedFile = customRequest(file);
    if (processedFile) {
      formState.value.file = processedFile;
      formState.value.text = '';
      sizeErr.value = false;
      fileErr.value = false;
    }
  } else {
    // Handle other file types (string, Blob, etc.)
    formState.value.file = file;
    formState.value.text = '';
    sizeErr.value = false;
    fileErr.value = false;
  }
};

/**
 * Delete uploaded text content
 */
const handleDeleteUploadText = () => {
  clearTextContent();
};

/**
 * Format tree select options
 * @param data - Tree select data
 * @returns Formatted tree select data
 */
const formatTreeSelectOptions = (data: any): any => {
  return { ...data, disabled: !!data.mockServiceId };
};

const isSubmitting = ref(false);

/**
 * Handle form save/submit
 */
const handleSubmit = () => {
  formRef.value?.validate().then(async () => {
    // Prepare base parameters
    let params: any = {
      name: formState.value.name,
      serviceDomain: !isPrivateEdition.value ? formState.value.serviceDomain + '.angusmock.cloud' : formState.value.serviceDomain,
      servicePort: formState.value.servicePort,
      nodeId: formState.value.nodeId,
      projectId: projectId.value
    };

    // Add service association parameters for tab 1
    if (activeTab.value === 1) {
      params = { ...params, serviceId: formState.value.serviceId };
      if (formState.value.apiIds?.length) {
        params = { ...params, apiIds: formState.value.apiIds };
      }
    }

    // Handle file upload for tab 2
    const formData = new FormData();
    if (activeTab.value === 2) {
      const isFileMissing = checkFile();
      if (isFileMissing || fileErr.value || sizeErr.value || versionRuleErr.value) {
        fileErr.value = isFileMissing;
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

    // Submit form based on active tab
    isSubmitting.value = true;
    const [error] = activeTab.value === 0
      ? await mock.addService(params)
      : activeTab.value === 1
        ? await mock.addServiceByAssoc(params)
        : await mock.addServiceFromFile(formData);

    isSubmitting.value = false;

    if (error) {
      return;
    }

    notification.success(t('actions.tips.addSuccess'));
    await router.push('/apis#mock');
  }, () => {
    // Validation failed
  });
};

onMounted(async () => {
  isPrivateEdition.value = appContext.isPrivateEdition();
  await loadNodes();
});
</script>

<template>
  <div class="px-5 py-5 flex h-full space-x-3.5">
    <div class="flex flex-col space-y-3.5 w-80 text-3.5 font-semibold">
      <Card
        v-for="(item,index) in textList"
        :key="index"
        :class="{'selected-card': activeTab === index}"
        hoverable
        class="flex cursor-pointer relative"
        style="min-height: 144px;"
        @click="handleChangeActiveTab(index)">
        <div class="flex items-center mb-2">
          <Icon :icon="item.icon" class="text-7 mr-2 flex-none" />
          <p class="text-3.5 leading-3.5 text-theme-title font-semibold mt-3">{{ item.name }}</p>
        </div>
        <p class="text-3 leading-5 text-theme-sub-content whitespace-break-spaces break-all">{{ item.description }}</p>
        <div class="absolute right-0 bottom-0 corner z-99"></div>
      </Card>
    </div>
    <PureCard class="flex-1 h-full py-8 px-8 overflow-auto font-semibold">
      <Form
        ref="formRef"
        size="small"
        :model="formState"
        :rules="rules">
        <div class="flex">
          <div>
            <FormItem :label="t('mock.addMock.form.name')" required />
            <FormItem
              :label="t('mock.addMock.form.domain')"
              :required="!isPrivateEdition"
              :class="isPrivateEdition?'pl-2.25':''" />
            <FormItem :label="t('mock.addMock.form.port')" required />
            <FormItem :label="t('mock.addMock.form.node')" required />
            <template v-if="activeTab===1">
              <FormItem :label="t('common.service')" required />
              <template v-if="formState.serviceId">
                <FormItem :label="t('common.api')" />
              </template>
            </template>
            <template v-if="activeTab===2">
              <FormItem :label="t('mock.addMock.form.importSource')" required />
            </template>
          </div>
          <div class="w-150">
            <FormItem name="name">
              <Input
                v-model:value="formState.name"
                :placeholder="t('common.placeholders.searchKeyword')"
                :maxlength="100" />
            </FormItem>
            <FormItem name="serviceDomain" class="relative">
              <Input
                v-model:value="formState.serviceDomain"
                :placeholder="t('mock.addMock.form.domainPlaceholder')">
                <template v-if="!isPrivateEdition" #addonAfter>
                  <span>.angusmock.cloud</span>
                </template>
              </Input>
              <template v-if="!isPrivateEdition">
                <Tooltip
                  placement="right"
                  :title="t('mock.addMock.form.domainTooltip')">
                  <Icon class="text-tips absolute -right-5 top-1.75 cursor-pointer text-3.5" icon="icon-tishi1" />
                </Tooltip>
              </template>
            </FormItem>
            <FormItem name="servicePort">
              <Input
                v-model:value="formState.servicePort"
                dataType="number"
                :placeholder="t('mock.addMock.form.portPlaceholder')"
                :min="1"
                :max="65535" />
            </FormItem>
            <FormItem name="nodeId">
              <Select
                v-model:value="formState.nodeId"
                :options="nodeState.nodeOptions"
                :placeholder="t('mock.addMock.form.nodePlaceholder')"
                size="small">
              </Select>
            </FormItem>
            <template v-if="activeTab===1">
              <FormItem name="serviceId">
                <Select
                  :value="selectedProjectId"
                  :action="`${TESTER}/services?projectId=${projectId}&queryHasMockServiceFlag=true&fullTextSearch=true`"
                  :fieldNames="{label:'name',value:'id'}"
                  :format="formatTreeSelectOptions"
                  showSearch
                  allowClear
                  :placeholder="t('mock.addMock.form.servicePlaceholder')"
                  @change="handleTreeSelectChange">
                  <template #title="item">
                    <div class="text-3 leading-3 flex items-center h-6.5 pr-2 space-x-2" :class="item.mockServiceId?'text-text-disabled':'text-text-content'">
                      <label class="w-4 h-4 leading-4 rounded-full text-white text-center mr-1 flex-none" :class="`bg-blue-badge-${item.targetType.value === 'PROJECT'? 'p':'s'}`">{{ item.targetType.value[0] }}</label>
                      <div
                        :title="item.name"
                        class="truncate">
                        {{ item.name }}
                      </div>
                      <template v-if="item.mockServiceId">
                        <div class="flex-none ml">{{ t('mock.addMock.form.associated') }}</div>
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
                  class="border p-3.5 rounded border-dashed text-3"
                  :class="fileErr || sizeErr || versionRuleErr?'border-rule':'border-border-divider '"
                  @paste="handlePasteEvent"
                  @drop="handleFileDrop"
                  @dragover="preventDefault">
                  <template v-if="!formState.text && !formState.file">
                    <div class="flex justify-center text-center">
                      <Upload
                        :multiple="false"
                        :showUploadList="false"
                        :customRequest="({file})=>handleCustomRequest(file)"
                        class="text-3 leading-3"
                        accept=".zip,.rar,.7z,.gz,.tar,.bz2,.xz,.lzma,.json,.yaml,.yml">
                        <Icon icon="icon-shangchuan" class="text-5 leading-5 text-text-link" />
                        <div class="text-3 leading-3 mt-2 text-text-link text-center">{{ t('mock.addMock.upload.uploadFile') }}</div>
                      </Upload>
                    </div>
                    <p class="text-3 leading-3 text-text-sub-content text-center mt-2">{{ t('mock.addMock.upload.uploadDescription') }}</p>
                  </template>
                  <template v-if="formState.text">
                    <div
                      class="overflow-y-auto -mr-3.5 pr-2 mt-1 whitespace-pre -mb-3.5 pb-2"
                      style="max-height: calc(100vh - 480px);">
                      {{ formState.text }}
                    </div>
                    <a
                      class="absolute right-3.5 top-0 text-3 text-text-link"
                      @click="handleDeleteUploadText">{{ t('actions.clear') }}</a>
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
                    {{ t('mock.addMock.upload.fileRequired') }}
                  </template>
                  <template v-if="sizeErr">
                    {{ t('mock.addMock.upload.fileSizeExceeded') }}
                  </template>
                  <template v-if="versionRuleErr">
                    {{ t('mock.addMock.upload.postmanVersionError') }}
                  </template>
                </div>
              </FormItem>
            </template>
            <FormItem>
              <Button
                size="small"
                type="primary"
                class="mr-3"
                :loading="isSubmitting"
                @click="handleSubmit">
                <Icon icon="icon-dangqianxuanzhong" class="mr-1" />
                {{ t('common.confirm') }}
              </Button>
              <RouterLink to="/apis#mock">
                <Button size="small">
                  <Icon icon="icon-zhongzhi2" class="mr-1" />
                  {{ t('actions.cancel') }}
                </Button>
              </RouterLink>
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
