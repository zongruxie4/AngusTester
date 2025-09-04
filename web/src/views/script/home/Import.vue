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

// eslint-disable-next-line func-call-spacing
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
    <Form
      ref="formRef"
      class="mr-5"
      :labelCol="{ style: { width: '60px' } }"
      :model="formState"
      :rules="rules">
      <FormItem name="name" :label="t('scriptHome.import.form.name')">
        <Input
          v-model:value="formState.name"
          :maxlength="200"
          trim
          :placeholder="t('scriptHome.import.form.namePlaceholder')" />
      </FormItem>

      <FormItem :label="t('scriptHome.import.form.description')" name="description">
        <Input
          v-model:value="formState.description"
          :autosize="{ minRows: 4, maxRows: 6 }"
          :maxlength="800"
          trim
          :placeholder="t('scriptHome.import.form.descriptionPlaceholder')"
          type="textarea" />
      </FormItem>

      <FormItem :label="t('scriptHome.import.form.file')" required>
        <div
          class="text-3 leading-5 border rounded border-dashed border-border-divider upload-container"
          :error="errorFlag">
          <div
            v-if="fileEmptyFlag"
            class="flex flex-col items-center justify-center p-3.5"
            @paste="handlePaste"
            @drop.prevent="handleDropUpload"
            @dragover="handleDragover">
            <Upload
              :multiple="false"
              :showUploadList="false"
              :customRequest="customRequest"
              accept=".zip,.rar,.7z,.gz,.tar,.bz2,.xz,.lzma,.json,.yaml,.yml">
              <div class="flex flex-col items-center justify-center">
                <Icon icon="icon-shangchuan" class="text-5 leading-5 text-text-link" />
                <div class="text-3 text-text-link text-center">{{ t('scriptHome.import.form.uploadFile') }}</div>
              </div>
            </Upload>

            <div class="text-text-sub-content mt-1">{{ t('scriptHome.import.form.uploadTip') }}</div>
          </div>

          <div v-if="formState.content" class="relative">
            <div class="flex-1 overflow-y-auto px-3.5 py-3 break-all whitespace-pre max-h-100 ">
              {{ formState.content }}
            </div>
            <Button
              class="absolute right-2.5 top-3 px-0 h-5 leading-5 !bg-white"
              type="link"
              size="small"
              @click="clearContent">
              {{ t('scriptHome.import.form.clear') }}
            </Button>
          </div>

          <div v-if="formState.file" class="flex items-center space-x-3.5 px-3.5 py-3 justify-between">
            <div :title="formState.file.name" class="truncate">{{ formState.file.name }}</div>
            <div class="flex items-center space-x-3.5">
              <div class="flex-shrink-0 whitespace-nowrap">{{ formatBytes(Number(formState.file.size)) }}</div>
              <Icon
                icon="icon-qingchu"
                class="flex-shrink-0 cursor-pointer text-3.5 text-theme-text-hover"
                @click="deleteFile" />
            </div>
          </div>
        </div>

        <div v-if="errorFlag" class="text-3 leading-5 text-rule">
          <span v-if="emptyFlag">{{ t('scriptHome.import.messages.uploadFirst') }}</span>
          <span v-if="maximumLimitFlag">{{ t('scriptHome.import.messages.fileTooLarge') }}</span>
        </div>
      </FormItem>
    </Form>
  </Modal>
</template>

<style scoped>
.upload-container[error="true"] {
  border-color: #ff4d4f;
}
</style>
