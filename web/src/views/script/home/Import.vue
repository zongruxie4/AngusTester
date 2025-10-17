<script setup lang="ts">
import { watch } from 'vue';
import { Button, Form, FormItem, Upload } from 'ant-design-vue';
import { Icon, Input, Modal } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { useScriptImport } from './composables/useScriptImport';

/**
 * Format bytes to human readable format
 */
const formatBytes = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

interface Props {
  projectId: string;
  visible: boolean;
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  visible: true
});


const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok'): void;
}>();

// Use the import composable
const {
  formState,
  emptyFlag,
  maximumLimitFlag,
  loading,
  formRef,
  handleCancel,
  handleDropUpload,
  customRequest,
  handleDragover,
  handlePaste,
  clearContent,
  deleteFile,
  handleImport,
  fileEmptyFlag,
  errorFlag,
  rules
} = useScriptImport();

/**
 * Handle OK action
 */
const handleOk = async () => {
  await handleImport(() => {
    emit('ok');
  });
};

/**
 * Handle cancel action
 */
const handleCancelClick = () => {
  handleCancel();
  emit('update:visible', false);
};

/**
 * Watch for visibility changes to reset form
 */
watch(() => props.visible, (newVisible) => {
  if (!newVisible) {
    handleCancel();
  }
});
</script>
<template>
  <Modal
    :visible="props.visible"
    :confirmLoading="loading"
    :width="800"
    :title="t('scriptHome.import.title')"
    @cancel="handleCancelClick"
    @ok="handleOk">
    <!--  Script name  -->
    <Form
      ref="formRef"
      class="space-y-6 font-semibold"
      :labelCol="{ style: { width: '80px' } }"
      :model="formState"
      :rules="rules">
      <FormItem
        name="name"
        :label="t('common.name')">
        <Input
          v-model:value="formState.name"
          :maxlength="200"
          trim
          class="rounded-lg"
          :placeholder="t('scriptHome.messages.namePlaceholder')" />
      </FormItem>

      <!--  Script file  -->
      <FormItem
        :label="t('scriptHome.import.form.file')"
        required>
        <div
          class="text-3 leading-5 border-2 rounded-lg border-dashed border-border-divider upload-container transition-all duration-300 hover:border-blue-300 hover:bg-blue-50/30"
          :error="errorFlag">
          <div
            v-if="fileEmptyFlag"
            class="flex flex-col items-center justify-center p-8 cursor-pointer group"
            @paste="handlePaste"
            @drop.prevent="handleDropUpload"
            @dragover="handleDragover">
            <Upload
              :multiple="false"
              :showUploadList="false"
              :customRequest="customRequest"
              accept=".zip,.rar,.7z,.gz,.tar,.bz2,.xz,.lzma,.json,.yaml,.yml">
              <div class="flex flex-col items-center justify-center">
                <div class="w-16 h-16 rounded-full bg-blue-100 flex items-center justify-center mb-4 group-hover:bg-blue-200 transition-colors duration-300">
                  <Icon icon="icon-shangchuan" class="text-2xl text-blue-600" />
                </div>
                <div class="text-lg font-medium text-gray-700 mb-2">{{ t('scriptHome.import.form.uploadFile') }}</div>
                <div class="text-sm text-gray-500 text-center max-w-xs leading-relaxed">{{ t('scriptHome.import.form.uploadTip') }}</div>
              </div>
            </Upload>
          </div>

          <div v-if="formState.content" class="relative bg-gray-50 rounded-lg border border-gray-200">
            <div class="flex-1 overflow-y-auto px-4 py-4 break-all whitespace-pre max-h-100 text-sm font-mono leading-relaxed">
              {{ formState.content }}
            </div>
            <Button
              class="absolute right-3 top-3 px-2 py-1 h-7 leading-5 bg-white border border-gray-200 rounded-md shadow-sm hover:bg-gray-50 transition-colors duration-200"
              type="text"
              size="small"
              @click="clearContent">
              <Icon icon="icon-qingchu" class="mr-1" />
              {{ t('actions.clear') }}
            </Button>
          </div>

          <div v-if="formState.file" class="flex items-center space-x-4 px-4 py-4 justify-between bg-green-50 border border-green-200 rounded-lg">
            <div class="flex items-center space-x-3">
              <div class="w-10 h-10 rounded-lg bg-green-100 flex items-center justify-center">
                <Icon icon="icon-wenjian" class="text-green-600 text-lg" />
              </div>
              <div class="flex-1 min-w-0">
                <div :title="formState.file.name" class="truncate font-medium text-gray-800">{{ formState.file.name }}</div>
                <div class="text-sm text-gray-500">{{ formatBytes(Number(formState.file.size)) }}</div>
              </div>
            </div>
            <Button
              class="flex-shrink-0 w-8 h-8 rounded-full bg-white border border-gray-200 shadow-sm hover:bg-gray-50 hover:border-red-300 transition-colors duration-200"
              type="text"
              size="small"
              @click="deleteFile">
              <Icon icon="icon-qingchu" class="text-gray-500 hover:text-red-500" />
            </Button>
          </div>
        </div>

        <div v-if="errorFlag" class="mt-3 p-3 bg-red-50 border border-red-200 rounded-lg">
          <div class="flex items-center space-x-2">
            <Icon icon="icon-jinggao" class="text-red-500 text-sm" />
            <span class="text-sm text-red-700">
              <span v-if="emptyFlag">{{ t('scriptHome.import.messages.uploadFirst') }}</span>
              <span v-if="maximumLimitFlag">{{ t('scriptHome.import.messages.fileTooLarge') }}</span>
            </span>
          </div>
        </div>
      </FormItem>

      <!--  Script description  -->
      <FormItem
        :label="t('common.description')"
        name="description">
        <Input
          v-model:value="formState.description"
          :autosize="{ minRows: 6, maxRows: 8 }"
          :maxlength="800"
          trim
          class="rounded-lg"
          :placeholder="t('scriptHome.messages.descriptionPlaceholder')"
          type="textarea" />
      </FormItem>
    </Form>
  </Modal>
</template>

<style scoped>
/* Upload container error state */
.upload-container[error="true"] {
  border-color: #ff4d4f;
  background-color: rgba(255, 77, 79, 0.05);
}

/* Upload area drag and drop effects */
.upload-container:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* File upload icon animation */
.group:hover .w-16 {
  transform: scale(1.05);
}

/* Form item spacing */
:deep(.ant-form-item) {
  margin-bottom: 24px;
}

/* Form label styling */
:deep(.ant-form-item-label) {
  font-weight: 500;
  color: #374151;
}

/* Input focus effects */
:deep(.ant-input:focus),
:deep(.ant-input-focused) {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

/* Textarea styling */
:deep(.ant-input) {
  border-radius: 8px;
  border: 1px solid #d1d5db;
  transition: all 0.2s ease;
}

:deep(.ant-input:hover) {
  border-color: #9ca3af;
}

/* Modal content styling */
:deep(.ant-modal-body) {
  padding: 24px;
}

/* Button styling */
:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border: none;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s ease;
}

:deep(.ant-btn-primary:hover) {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

/* Cancel button styling */
:deep(.ant-btn-default) {
  border-radius: 8px;
  border: 1px solid #d1d5db;
  color: #6b7280;
  transition: all 0.2s ease;
}

:deep(.ant-btn-default:hover) {
  border-color: #9ca3af;
  color: #374151;
  transform: translateY(-1px);
}
</style>
