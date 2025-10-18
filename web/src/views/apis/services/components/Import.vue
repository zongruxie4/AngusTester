<script setup lang="ts">
import { inject, onMounted, ref, Ref } from 'vue';
import { Button, Popover, Radio, RadioGroup, UploadDragger } from 'ant-design-vue';
import { Icon, Input, notification, Spin, Validate } from '@xcan-angus/vue-ui';
import postmanToOpenApi from '@xcan-angus/postman-to-openapi';
import { useI18n } from 'vue-i18n';
import { services } from '@/api/tester';
import { formatBytes } from '@/utils/common';
import { StrategyWhenDuplicated, ApiImportSource } from '@/enums/enums';
import { UploadRequestOption } from 'ant-design-vue/lib/vc-upload/interface';

import SelectEnum from '@/components/enum/SelectEnum.vue';

interface Props {
  serviceId?: string;
  serviceName?: string;
  source?:'introduce' | 'global' | 'services';
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  serviceId: undefined,
  serviceName: undefined,
  source: 'introduce'
});
const { t } = useI18n();

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void,
  (e: 'ok', value: {id: string, key: string}): void,
  (e: 'close'): void
}>();

// Reactive references for import form state
const isLoading = ref<boolean>(false);
const strategyWhenDuplicated = ref<StrategyWhenDuplicated>(StrategyWhenDuplicated.COVER);
const deleteWhenNotExisted = ref<boolean>(false);
const projectServiceName = ref<string | undefined>();
const nameErrorMsg = ref<string | undefined>();
const importSource = ref<ApiImportSource>(ApiImportSource.OPENAPI);
const uploadErrorMsg = ref<string | undefined>();
const isProcessing = ref<boolean>(false);
const convertedFile = ref<File | undefined>();
const originalFile = ref<File | undefined>();

/**
 * Handle import source change event
 * @param value - New import source value
 * @param option - Selected option object
 */
const handleImportSourceChange = (value: string) => {
  // Convert string value to ApiImportSource enum
  const typedValue = value as ApiImportSource;

  if (typedValue === ApiImportSource.POSTMAN && originalFile.value && originalFile.value.name === convertedFile.value?.name) {
    convertToOpenapi(originalFile.value);
  } else {
    convertedFile.value = originalFile.value;
    uploadErrorMsg.value = undefined;
  }
};

/**
 * Save import form data and submit API request
 * @returns Promise that resolves when save operation completes
 */
const saveImportData = async () => {
  // Prevent multiple submissions
  if (isLoading.value) {
    return;
  }

  // Validate import source
  if (!importSource.value) {
    notification.warning(t('service.importForm.sourceRule'));
    return;
  }

  // Validate file selection
  if (!convertedFile.value) {
    uploadErrorMsg.value = t('service.importForm.fileRule_required');
    return;
  }

  // Validate service name for new services
  if (!props.serviceId && !projectServiceName.value) {
    nameErrorMsg.value = t('service.importForm.nameRule');
    return;
  }

  // Prepare form data for API request
  const formData = new FormData();

  formData.append('file', convertedFile.value);
  formData.append('strategyWhenDuplicated', strategyWhenDuplicated.value);
  formData.append('deleteWhenNotExisted', deleteWhenNotExisted.value.toString());
  formData.append('importSource', importSource.value);
  formData.append('projectId', projectId.value);

  if (props.serviceId) {
    formData.append('serviceId', props.serviceId);
  } else {
    formData.append('serviceName', projectServiceName.value || '');
  }

  // Submit API request
  isLoading.value = true;
  const [error, response] = await services.localImport(formData);
  isLoading.value = false;

  if (error) {
    return;
  }

  // Reset form and notify success
  resetImportForm();
  emits('ok', response.data || {});
  notification.success(t('actions.tips.importSuccess'));
};

/**
 * Handle custom file upload request
 * @param options - Upload request options
 */
const handleCustomRequest = (options: UploadRequestOption) => {
  uploadErrorMsg.value = undefined;

  // Type guard to ensure file is of type File
  if (!(options.file instanceof File)) {
    uploadErrorMsg.value = t('service.importForm.fileRule_required');
    return;
  }

  const uploadedFile = options.file;

  // Automatically convert Postman collections to OpenAPI format
  if (uploadedFile.type === 'application/json' && importSource.value === ApiImportSource.POSTMAN) {
    convertToOpenapi(uploadedFile);
    return;
  }

  convertedFile.value = uploadedFile;
  originalFile.value = uploadedFile;
  isProcessing.value = false;
};

/**
 * Convert Postman collection to OpenAPI format
 * @param file - Postman collection file to convert
 */
const convertToOpenapi = (file: File) => {
  isProcessing.value = true;
  const reader = new FileReader();

  reader.onload = () => {
    try {
      const fileContent = reader.result as string;
      const openapiDoc = postmanToOpenApi(fileContent);
      const yamlFileName = file.name.replace(/\.json$/, '') + '.yml';
      convertedFile.value = new File([openapiDoc], yamlFileName, { type: 'text/yaml' });
      originalFile.value = file;
      isProcessing.value = false;
    } catch (error) {
      // Handle conversion error
      isProcessing.value = false;
      convertedFile.value = file;
      originalFile.value = file;
      uploadErrorMsg.value = t('service.importForm.fileRule');
    }
  };

  reader.readAsText(file);
};

/**
 * Remove selected file
 */
const removeFile = () => {
  convertedFile.value = undefined;
  originalFile.value = undefined;
  uploadErrorMsg.value = undefined;
};

/**
 * Reset import form to initial state
 */
const resetImportForm = () => {
  strategyWhenDuplicated.value = StrategyWhenDuplicated.COVER;
  deleteWhenNotExisted.value = false;
  projectServiceName.value = undefined;
  nameErrorMsg.value = undefined;
  convertedFile.value = undefined;
  originalFile.value = undefined;
  uploadErrorMsg.value = undefined;
  importSource.value = ApiImportSource.OPENAPI;
};

/**
 * Close modal and reset form
 */
const closeModal = () => {
  resetImportForm();
  emits('close');
};

/**
 * Handle service name change event
 */
const handleServiceNameChange = () => {
  nameErrorMsg.value = undefined;
};

/**
 * Exclude certain import sources from selection
 * @param option - Option object to check
 * @returns Boolean indicating if option should be excluded
 */
const excludeImportSources = (option: { message: string; value: string }) => {
  // Convert string value to ApiImportSource enum for comparison
  const typedValue = option.value as ApiImportSource;
  return [ApiImportSource.ANGUS].includes(typedValue);
};

// Initialize component with service name if provided
onMounted(() => {
  projectServiceName.value = props.serviceName;
});
</script>
<template>
  <div class="text-text-content text-3 flex flex-col w-full">
    <Spin :spinning="isLoading">
      <div v-if="!props.serviceId && props.source !== 'introduce'" class="mb-5 text-3">
        <div class="mb-2 text-black-active leading-3">{{ t('service.importForm.nameLabel') }}</div>
        <Validate :text="nameErrorMsg">
          <Input
            v-model:value="projectServiceName"
            allowClear
            :error="!!nameErrorMsg"
            :maxlength="100"
            :placeholder="t('common.placeholders.searchKeyword')"
            @change="handleServiceNameChange" />
        </Validate>
      </div>
      <div class="leading-3">{{ t('service.importForm.sourceLabel') }}</div>
      <SelectEnum
        v-model:value="importSource"
        class="w-full mt-2 mb-5"
        size="small"
        :excludes="excludeImportSources"
        enumKey="ApiImportSource"
        defaultActiveFirstOption
        @change="handleImportSourceChange" />
      <div :class="{'mb-5':props.source == 'services'}">
        <Spin :spinning="isProcessing">
          <UploadDragger
            v-show="!convertedFile"
            :class="{'error-border':!!uploadErrorMsg}"
            :multiple="false"
            :showUploadList="false"
            :customRequest="handleCustomRequest"
            class="text-3 leading-5"
            accept=".zip,.rar,.7z,.gz,.tar,.bz2,.xz,.lzma,.json,.yaml,.yml">
            <div class="flex flex-col items-center justify-center text-3 leading-5">
              <Icon icon="icon-shangchuan" class="text-5 text-text-link" />
              <div class="mt-1 mb-1.5 text-text-link">{{ t('service.importForm.fileLabel') }}</div>
              <div class="text-theme-sub-content">{{ t('service.importForm.fileTip') }}</div>
            </div>
          </UploadDragger>
        </Spin>
        <Validate :text="uploadErrorMsg">
          <div
            v-show="!!originalFile"
            :class="{'border-status-error':!!uploadErrorMsg}"
            class="px-3.5 border rounded">
            <div class="flex py-2 text-3 leading-3">
              <Popover placement="top">
                <template #content>
                  {{ originalFile?.name }}
                </template>
                <div class="flex-2 truncate">{{ originalFile?.name }}</div>
              </Popover>
              <div class="flex-1 text-right">{{ formatBytes(Number(originalFile?.size)) }}</div>
              <div class="flex-1 text-right text-gray-500">
                <Icon
                  icon="icon-qingchu"
                  class="cursor-pointer ml-2 text-3.5 text-theme-text-hover"
                  @click="removeFile" />
              </div>
            </div>
          </div>
        </Validate>
      </div>

      <template v-if="props.source == 'services'">
        <div class="space-y-0.5 mb-5 leading-5">
          <div>{{ t('service.importForm.strategyWhenDuplicated') }}</div>
          <RadioGroup v-model:value="strategyWhenDuplicated">
            <Radio value="COVER">{{ t('service.importForm.strategy_cover') }}</Radio>
            <Radio value="IGNORE">{{ t('service.importForm.strategy_ignore') }}</Radio>
          </RadioGroup>
        </div>

        <div class="space-y-0.5 leading-5">
          <div>{{ t('service.importForm.deleteWhenNotExisted') }}</div>
          <RadioGroup v-model:value="deleteWhenNotExisted">
            <Radio :value="true">{{ t('service.importForm.delete_y') }}</Radio>
            <Radio :value="false">{{ t('service.importForm.delete_n') }}</Radio>
          </RadioGroup>
        </div>
      </template>
    </Spin>
    <div class="flex mt-5" :class="{'justify-end':props.source !== 'introduce'}">
      <template v-if="['global','services'].includes(props.source)">
        <Button
          size="small"
          class="mr-5"
          @click="closeModal">
          {{ t('actions.cancel') }}
        </Button>
      </template>
      <Button
        type="primary"
        size="small"
        :loading="isLoading || isProcessing"
        @click="saveImportData">
        {{ t('actions.confirm') }}
      </Button>
    </div>
  </div>
</template>
<style scoped>
:deep(.ant-upload.ant-upload-drag.error-border) {
  @apply border-border-error;
}
</style>
