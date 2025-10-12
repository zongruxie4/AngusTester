<script lang="ts" setup>
import { Ref, inject, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Modal, Select, Spin } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, RadioGroup, UploadDragger } from 'ant-design-vue';
import { TESTER, enumOptionUtils } from '@xcan-angus/infra';
import { StrategyWhenDuplicated } from '@/enums/enums';
import { formatBytes } from '@/utils/common';
import { testCase } from '@/api/tester';

const { t } = useI18n();

// Component props interface
export interface Props{
  visible: boolean;
  downloadTemplate: () => void;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
});

// Component emits
const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'ok'):void;}>();

// Basic state management
const projectId = inject<Ref<string>>('projectId', ref(''));
const loading = ref(false);
const strategyWhenDuplicatedOpt = ref<{value: string; label: string}[]>([]);

// Form state management
const formRef = ref();
const formData = ref<{
  file: File | undefined;
  strategyWhenDuplicated: StrategyWhenDuplicated;
  planId: string | undefined;
}>({
  file: undefined,
  strategyWhenDuplicated: StrategyWhenDuplicated.COVER,
  planId: undefined
});

/**
 * Load strategy when duplicated enum options
 */
const loadEnums = () => {
  strategyWhenDuplicatedOpt.value = enumOptionUtils.loadEnumAsOptions(StrategyWhenDuplicated);
};

/**
 * Handle file selection
 * @param fileInfo - File information
 */
const handleFile = (fileInfo: any) => {
  formData.value.file = fileInfo.file;
  formRef.value.validateFields(['file']);
};

/**
 * Delete selected file
 */
const deleteFile = () => {
  formData.value.file = undefined;
};

/**
 * Validate file field
 * @returns Validation result
 */
const validateFile = async () => {
  if (formData.value.file) {
    return Promise.resolve();
  }
  return Promise.reject();
};

/**
 * Handle download template
 */
const handleDownloadTemplate = () => {
  if (typeof props.downloadTemplate === 'function') {
    props.downloadTemplate();
  }
};

/**
 * Cancel upload
 */
const cancel = () => {
  emits('update:visible', false);
};

/**
 * Confirm upload
 */
const ok = () => {
  formRef.value.validate()
    .then(async () => {
      const formParams = new FormData();
      formParams.append('planId', formData.value.planId || '');
      formParams.append('strategyWhenDuplicated', formData.value.strategyWhenDuplicated);
      if (formData.value.file) {
        formParams.append('file', formData.value.file);
      }
      loading.value = true;
      const [error] = await testCase.importCase(formParams);
      loading.value = false;
      if (error) {
        return;
      }
      emits('ok');
      cancel();
    });
};

// Watchers
watch(() => props.visible, newValue => {
  if (!newValue) {
    formData.value.file = undefined;
    formData.value.strategyWhenDuplicated = StrategyWhenDuplicated.COVER;
  }
  if (!strategyWhenDuplicatedOpt.value.length) {
    loadEnums();
  }
}, {
  immediate: true
});
</script>
<template>
  <Modal
    :title="t('actions.upload')"
    :visible="props.visible"
    class="upload-modal"
    :width="600"
    :centered="true"
    @cancel="cancel"
    @ok="ok">
    <div class="upload-container">
      <Form
        ref="formRef"
        :model="formData"
        size="small"
        class="upload-form">
        <FormItem
          :label="t('common.plan')"
          name="planId"
          required>
          <Select
            v-model:value="formData.planId"
            :disabled="!projectId"
            :action="`${TESTER}/func/plan?projectId=${projectId}&fullTextSearch=true`"
            :defaultActiveFirstOption="true"
            :lazy="false"
            :placeholder="t('common.placeholders.selectPlan')"
            :fieldNames="{value: 'id', label: 'name'}"
            class="enhanced-select" />
        </FormItem>

        <FormItem
          :rules="{message: t('testCase.messages.pleaseUploadFile'), validator: validateFile}"
          name="file">
          <Spin :spinning="loading">
            <UploadDragger
              v-show="!formData.file"
              accept=".xls,.xlsx"
              :multiple="false"
              :showUploadList="false"
              :customRequest="handleFile"
              class="enhanced-upload-dragger">
              <div class="upload-content">
                <div class="upload-icon-container">
                  <Icon icon="icon-shangchuan" class="upload-main-icon" />
                  <div class="upload-icon-bg"></div>
                </div>
                <div class="upload-text">
                  <div class="upload-title">{{ t('testCase.messages.selectFile') }}</div>
                  <div class="upload-subtitle">{{ t('testCase.messages.uploadDescription') }}</div>
                </div>
              </div>
            </UploadDragger>
          </Spin>

          <div
            v-show="!!formData.file"
            class="file-preview-card">
            <div class="file-preview-content">
              <div class="file-icon">
                <Icon icon="icon-excel" class="file-type-icon" />
              </div>
              <div class="file-info">
                <div :title="formData.file?.name" class="file-name">{{ formData.file?.name }}</div>
                <div class="file-meta">
                  <span class="file-size">{{ formatBytes(Number(formData.file?.size)) }}</span>
                  <span class="file-status">
                    <Icon icon="icon-chenggong" class="status-icon success" />
                    {{ t('common.ready') }}
                  </span>
                </div>
              </div>
              <div class="file-actions">
                <Button
                  type="text"
                  size="small"
                  class="remove-btn"
                  @click="deleteFile">
                  <Icon icon="icon-qingchu" class="remove-icon" />
                </Button>
              </div>
            </div>
          </div>

          <div class="template-download">
            <Button
              type="link"
              size="small"
              class="template-btn"
              @click="handleDownloadTemplate">
              <Icon icon="icon-daochu1" class="template-icon" />
              {{ t('testCase.messages.caseImportTemplate') }}
            </Button>
          </div>
        </FormItem>

        <FormItem
          :label="t('testCase.messages.duplicateStrategy')"
          name="strategyWhenDuplicated"
          required>
          <RadioGroup
            v-model:value="formData.strategyWhenDuplicated"
            :options="strategyWhenDuplicatedOpt"
            class="enhanced-radio-group">
          </RadioGroup>
        </FormItem>
      </Form>
    </div>
  </Modal>
</template>

<style scoped>
/* Modal Styles */
.upload-modal :deep(.ant-modal-body) {
  padding: 0;
}

.upload-modal :deep(.ant-modal-header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-bottom: none;
  border-radius: 8px 8px 0 0;
}

.upload-modal :deep(.ant-modal-title) {
  color: white;
  font-weight: 600;
  font-size: 16px;
}

.upload-modal :deep(.ant-modal-close) {
  color: white;
}

.upload-modal :deep(.ant-modal-close:hover) {
  color: rgba(255, 255, 255, 0.8);
}

/* Container */
.upload-container {
  padding: 20px;
  background: #fafbfc;
}

/* Form Styles */
.upload-form :deep(.ant-form-item) {
  margin-bottom: 16px;
}

.upload-form :deep(.ant-form-item-label) {
  font-weight: 600;
  color: #262626;
  font-size: 14px;
  margin-bottom: 8px;
}

/* Enhanced Select */
.enhanced-select :deep(.ant-select-selector) {
  border-radius: 8px;
  border: 2px solid #f0f0f0;
  transition: all 0.3s ease;
  height: 40px;
}

.enhanced-select :deep(.ant-select-selector:hover) {
  border-color: #40a9ff;
}

.enhanced-select :deep(.ant-select-focused .ant-select-selector) {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

/* Enhanced Upload Dragger */
.enhanced-upload-dragger {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  background: linear-gradient(135deg, #fafbfc 0%, #f8f9fa 100%);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.enhanced-upload-dragger:hover {
  border-color: #40a9ff;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.1);
}

.enhanced-upload-dragger:active {
  transform: translateY(0);
}

.upload-content {
  padding: 24px 16px;
  text-align: center;
  position: relative;
}

.upload-icon-container {
  position: relative;
  display: inline-block;
  margin-bottom: 12px;
}

.upload-main-icon {
  font-size: 40px;
  color: #1890ff;
  position: relative;
  z-index: 2;
  transition: all 0.3s ease;
}

.upload-icon-bg {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border-radius: 50%;
  z-index: 1;
  opacity: 0.6;
}

.enhanced-upload-dragger:hover .upload-main-icon {
  transform: scale(1.1);
  color: #40a9ff;
}

.enhanced-upload-dragger:hover .upload-icon-bg {
  opacity: 0.8;
  transform: translate(-50%, -50%) scale(1.1);
}

.upload-text {
  margin-bottom: 12px;
}

.upload-title {
  font-size: 15px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 6px;
}

.upload-subtitle {
  font-size: 13px;
  color: #8c8c8c;
  line-height: 1.4;
}

/* File Preview Card */
.file-preview-card {
  background: white;
  border: 2px solid #52c41a;
  border-radius: 8px;
  padding: 12px;
  margin-top: 12px;
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.1);
  animation: slideInUp 0.3s ease;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.file-preview-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.file-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.file-type-icon {
  font-size: 20px;
  color: white;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 13px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
}

.file-size {
  color: #8c8c8c;
}

.file-status {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #52c41a;
  font-weight: 500;
}

.status-icon {
  font-size: 14px;
}

.status-icon.success {
  color: #52c41a;
}

.file-actions {
  flex-shrink: 0;
}

.remove-btn {
  color: #ff4d4f;
  border: none;
  background: none;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.remove-btn:hover {
  background: #fff2f0;
  color: #ff7875;
}

.remove-icon {
  font-size: 16px;
}

/* Template Download */
.template-download {
  text-align: right;
  margin-top: 8px;
}

.template-btn {
  color: #1890ff;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.template-btn:hover {
  background: #f0f9ff;
  color: #40a9ff;
}

.template-icon {
  margin-right: 4px;
  font-size: 14px;
}

/* Enhanced Radio Group */
.enhanced-radio-group {
  display: flex;
  flex-direction: row;
  gap: 16px;
  flex-wrap: wrap;
}

.enhanced-radio-group :deep(.ant-radio-wrapper) {
  margin: 0;
  padding: 8px 12px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  background: #fafafa;
  transition: all 0.3s ease;
  cursor: pointer;
}

.enhanced-radio-group :deep(.ant-radio-wrapper:hover) {
  border-color: #40a9ff;
  background: #f0f9ff;
}

.enhanced-radio-group :deep(.ant-radio-wrapper-checked) {
  border-color: #1890ff;
  background: #e6f7ff;
  box-shadow: 0 1px 4px rgba(24, 144, 255, 0.1);
}

.enhanced-radio-group :deep(.ant-radio) {
  margin-right: 8px;
}

.enhanced-radio-group :deep(.ant-radio-inner) {
  width: 16px;
  height: 16px;
}

.enhanced-radio-group :deep(.ant-radio-checked .ant-radio-inner) {
  background-color: #1890ff;
  border-color: #1890ff;
}
</style>
