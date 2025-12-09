<script setup lang="ts">
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Modal, Icon, notification, Spin } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Input, UploadDragger } from 'ant-design-vue';
import { template } from '@/api/tester';
import { formatBytes } from '@/utils/common';

const { t } = useI18n();

// eslint-disable-next-line import/no-absolute-path
import CaseTemplateCsv from '/file/Case_Template.csv?url';
import CaseTemplateExcel from '/file/Case_Template.xlsx?url';
import CaseTemplateJson from '/file/Case_Template.json?url';

import IssueTemplateCsv from '/file/Issue_Template.csv?url';
import IssueTemplateExcel from '/file/Issue_Template.xlsx?url';
import IssueTemplateJson from '/file/Issue_Template.json?url';

import TestPlanTemplateCsv from '/file/Plan_Template.csv?url';
import TestPlanTemplateExcel from '/file/Plan_Template.xlsx?url';
import TestPlanTemplateJson from '/file/Plan_Template.json?url';

interface Props {
  visible: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
});

const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok'): void;
}>();

const templateExampleFile = {
    'ISSUE': {
        'csv': IssueTemplateCsv,
        'excel': IssueTemplateExcel,
        'json': IssueTemplateJson
    },
    'TEST_PLAN': {
        'csv': TestPlanTemplateCsv,
        'excel': TestPlanTemplateExcel,
        'json': TestPlanTemplateJson
    },
    'TEST_CASE': {
        'csv': CaseTemplateCsv,
        'excel': CaseTemplateExcel,
        'json': CaseTemplateJson
    }
}

// Template types
type TemplateType = 'ISSUE' | 'TEST_PLAN' | 'TEST_CASE';

// Form state
const formRef = ref();
const isLoading = ref(false);
const selectedTemplateType = ref<TemplateType>('ISSUE');
const uploadedFile = ref<File | undefined>(undefined);
const templateName = ref<string>('');

// Template type options
const templateTypeOptions = [
  {
    value: 'ISSUE' as TemplateType,
    label: t('testTemplate.importTemplate.downloadTemplate.types.issueTemplate'),
    description: t('testTemplate.importTemplate.downloadTemplate.types.issueTemplateDesc'),
    icon: 'icon-jinggao',
    color: '#ff4d4f'
  },
  {
    value: 'TEST_PLAN' as TemplateType,
    label: t('testTemplate.importTemplate.downloadTemplate.types.testPlanTemplate'),
    description: t('testTemplate.importTemplate.downloadTemplate.types.testPlanTemplateDesc'),
    icon: 'icon-wendang',
    color: '#1890ff'
  },
  {
    value: 'TEST_CASE' as TemplateType,
    label: t('testTemplate.importTemplate.downloadTemplate.types.testCaseTemplate'),
    description: t('testTemplate.importTemplate.downloadTemplate.types.testCaseTemplateDesc'),
    icon: 'icon-bianji',
    color: '#52c41a'
  }
];

// Get template type label
const getTemplateTypeLabel = (type: TemplateType) => {
  const option = templateTypeOptions.find(opt => opt.value === type);
  return option?.label || type;
};

// Handle template type selection
const handleTemplateTypeSelect = (type: TemplateType) => {
  selectedTemplateType.value = type;
  // Reset file when type changes
  uploadedFile.value = undefined;
  templateName.value = '';
};

// Handle file selection
const handleFileSelection = (fileInfo: any) => {
  const file = fileInfo.file as File;
  
  // Validate file format
  const fileName = file.name.toLowerCase();
  const validExtensions = ['.xlsx', '.xls', '.csv', '.json'];
  const isValidFormat = validExtensions.some(ext => fileName.endsWith(ext));
  
  if (!isValidFormat) {
    notification.error(t('testTemplate.importTemplate.messages.invalidFileFormat'));
    return;
  }
  
  uploadedFile.value = file;
  
  // Set default template name from file name (without extension)
  if (!templateName.value) {
    const nameWithoutExt = file.name.replace(/\.(xlsx|xls|csv|json)$/i, '');
    templateName.value = nameWithoutExt;
  }
  
  formRef.value?.validateFields(['file']);
};

// Remove selected file
const removeFile = () => {
  uploadedFile.value = undefined;
  templateName.value = '';
};

// Handle file drop
const handleFileDrop = (event: DragEvent) => {
  event.preventDefault();
  const files = event.dataTransfer?.files;
  if (files && files[0]) {
    handleFileSelection({ file: files[0] });
  }
};

// Handle drag over
const handleDragOver = (event: DragEvent) => {
  event.preventDefault();
};

// Validate file selection
const validateFileSelection = async () => {
  if (uploadedFile.value) {
    return Promise.resolve();
  }
  return Promise.reject(new Error(t('testTemplate.importTemplate.messages.uploadFileRequired')));
};

// Download template file
const downloadTemplate = (format: 'excel'|'csv'|'json') => {
  const templateType = selectedTemplateType.value;
  handleImportTemplate(templateType, format);
};

// Close modal
const closeModal = () => {
  emits('update:visible', false);
  // Reset form
  selectedTemplateType.value = 'ISSUE';
  uploadedFile.value = undefined;
  templateName.value = '';
  formRef.value?.resetFields();
};

// Submit import
const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    
    if (!uploadedFile.value) {
      notification.error(t('testTemplate.importTemplate.messages.uploadFileRequired'));
      return;
    }
    
    if (!templateName.value.trim()) {
      notification.error(t('testTemplate.importTemplate.messages.templateNameRequired'));
      return;
    }
    
    isLoading.value = true;
    
    const formData = new FormData();
    formData.append('templateType', selectedTemplateType.value);
    formData.append('name', templateName.value.trim());
    formData.append('file', uploadedFile.value);
    
    const [error] = await template.importTemplate(formData);
    
    isLoading.value = false;
    
    if (error) {
      notification.error(error.message || t('testTemplate.importTemplate.messages.importFailed'));
      return;
    }
    
    notification.success(t('common.tips.importSuccess'));
    emits('ok');
    closeModal();
  } catch (error) {
    isLoading.value = false;
    // Validation error, do nothing
  }
};

// Export import template
const handleImportTemplate = async (templateType: TemplateType, format: 'excel'|'csv'|'json') => {
  const downloadLink = document.createElement('a');
  downloadLink.style.display = 'none';
  downloadLink.href = templateExampleFile[templateType][format];
  downloadLink.download = `Import_${templateType}_Template.${format}`;
  document.body.appendChild(downloadLink);
  downloadLink.click();
  document.body.removeChild(downloadLink);
};

// Watch visible to reset form
watch(() => props.visible, (newValue) => {
  if (!newValue) {
    selectedTemplateType.value = 'ISSUE';
    uploadedFile.value = undefined;
    templateName.value = '';
    formRef.value?.resetFields();
  }
});
</script>

<template>
  <Modal
    :title="t('testTemplate.importTemplate.title')"
    :visible="props.visible"
    :width="700"
    :centered="true"
    :footer="null"
    @cancel="closeModal">
    <Spin :spinning="isLoading">
      <Form
        ref="formRef"
        :model="{ templateType: selectedTemplateType, file: uploadedFile, name: templateName }"
        layout="vertical"
        class="import-template-form">
        
        <!-- Template Type Selection -->
        <FormItem
          :label="t('testTemplate.importTemplate.labels.selectTemplateType')"
          required>
          <div class="template-type-cards">
            <div
              v-for="option in templateTypeOptions"
              :key="option.value"
              class="template-type-card"
              :class="{ active: selectedTemplateType === option.value }"
              @click="handleTemplateTypeSelect(option.value)">
              <div class="template-type-icon" :style="{ color: option.color }">
                <Icon :icon="option.icon" />
              </div>
              <div class="template-type-content">
                <div class="template-type-label">{{ option.label }}</div>
                <div class="template-type-desc">{{ option.description }}</div>
              </div>
            </div>
          </div>
        </FormItem>

        <!-- File Upload -->
        <FormItem
          :label="t('testTemplate.importTemplate.labels.uploadTemplateFile')"
          name="file"
          :rules="{ validator: validateFileSelection }"
          required>
          <div v-if="!uploadedFile" class="upload-area">
            <UploadDragger
              accept=".xlsx,.xls,.csv,.json"
              :multiple="false"
              :showUploadList="false"
              :customRequest="handleFileSelection"
              @drop="handleFileDrop"
              @dragover="handleDragOver">
              <div class="upload-content">
                <div class="upload-icon">
                  <Icon icon="icon-shangchuan" />
                </div>
                <div class="upload-text">
                  <div class="upload-title">{{ t('testTemplate.importTemplate.upload.title') }}</div>
                  <div class="upload-subtitle">{{ t('testTemplate.importTemplate.upload.subtitle') }}</div>
                </div>
                <Button type="primary" class="upload-button">{{ t('testTemplate.importTemplate.buttons.selectFile') }}</Button>
              </div>
            </UploadDragger>
          </div>
          
          <div v-else class="file-preview">
            <div class="file-info">
              <Icon icon="icon-excel" class="file-icon" />
              <div class="file-details">
                <div class="file-name">{{ uploadedFile.name }}</div>
                <div class="file-size">{{ formatBytes(uploadedFile.size) }}</div>
              </div>
            </div>
            <Button type="text" @click="removeFile">
              <Icon icon="icon-qingchu" />
            </Button>
          </div>
        </FormItem>

        <!-- Template Name -->
        <FormItem
          :label="t('testTemplate.importTemplate.labels.templateName')"
          name="name"
          :rules="[{ required: true, message: t('testTemplate.importTemplate.messages.templateNameRequired') }]"
          required>
          <Input
            v-model:value="templateName"
            :placeholder="t('testTemplate.importTemplate.placeholders.templateName')"
            :maxlength="100" />
          <div class="form-hint">{{ t('testTemplate.importTemplate.formHint') }}</div>
        </FormItem>

        <div class="rounded bg-gray-100 p-4 space-y-3">
            <div class="font-semibold"><Icon icon="icon-daochu1" class="download-icon mr-1" />{{ t('testTemplate.importTemplate.downloadTemplate.title') }}</div>
            <div class="download-section">
                <div class="download-buttons">
                    <Button
                    @click="downloadTemplate('excel')">
                        <Icon icon="icon-wendang" class="mr-1" />
                        {{ getTemplateTypeLabel(selectedTemplateType) }} (.xlsx)
                    </Button>
                    <Button
                    @click="downloadTemplate('csv')">
                        <Icon icon="icon-wendang" class="mr-1" />
                        {{ getTemplateTypeLabel(selectedTemplateType) }} (.csv)
                    </Button>
                    <Button
                    @click="downloadTemplate('json')">
                        <Icon icon="icon-wendang" class="mr-1" />
                        {{ getTemplateTypeLabel(selectedTemplateType) }} (.json)
                    </Button>
                </div>
            </div>
        </div>

        <!-- Footer Buttons -->
        <FormItem>
          <div class="form-footer">
            <Button @click="closeModal">{{ t('actions.cancel') }}</Button>
            <Button type="primary" @click="handleSubmit">{{ t('testTemplate.importTemplate.buttons.startImport') }}</Button>
          </div>
        </FormItem>
      </Form>
    </Spin>
  </Modal>
</template>

<style scoped>
.import-template-form {
  padding: 0;
}

/* Template Type Cards */
.template-type-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.template-type-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.template-type-card:hover {
  border-color: #40a9ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

.template-type-card.active {
  border-color: #1890ff;
  background: #f0f9ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.template-type-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.template-type-content {
  text-align: center;
}

.template-type-label {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
}

.template-type-desc {
  font-size: 12px;
  color: #8c8c8c;
}

/* Upload Area */
.upload-area {
  margin-bottom: 0;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 10px;
  text-align: center;
}

.upload-icon {
  font-size: 48px;
  color: #1890ff;
  margin-bottom: 16px;
}

.upload-text {
  margin-bottom: 16px;
}

.upload-title {
  font-size: 15px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 8px;
}

.upload-subtitle {
  font-size: 13px;
  color: #8c8c8c;
  line-height: 1.5;
}

.upload-button {
  margin-top: 8px;
}

/* File Preview */
.file-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 6px;
}

.file-info {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.file-icon {
  font-size: 24px;
  color: #52c41a;
  margin-right: 12px;
}

.file-details {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 12px;
  color: #8c8c8c;
}

/* Form Hint */
.form-hint {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

/* Download Section */
.download-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.download-icon {
  font-size: 18px;
  color: #1890ff;
}

.download-buttons {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.download-buttons :deep(.ant-btn-link) {
  padding: 0;
  height: auto;
  color: #1890ff;
}

.download-buttons :deep(.ant-btn-link:hover) {
  color: #40a9ff;
}

/* Form Footer */
.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

/* Form Item Styles */
:deep(.ant-form-item-label > label) {
  font-weight: 600;
  color: #262626;
}

:deep(.ant-form-item-label > label::before) {
  content: '*';
  color: #ff4d4f;
  margin-right: 4px;
}

:deep(.ant-form-item-label > label.ant-form-item-required:not(.ant-form-item-required-mark-optional)::before) {
  display: inline-block;
}

/* Upload Dragger Styles */
:deep(.ant-upload-drag) {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.3s;
}

:deep(.ant-upload-drag:hover) {
  border-color: #40a9ff;
  background: #f0f9ff;
}

:deep(.ant-upload-drag.ant-upload-drag-hover) {
  border-color: #1890ff;
  background: #e6f7ff;
}
</style>
