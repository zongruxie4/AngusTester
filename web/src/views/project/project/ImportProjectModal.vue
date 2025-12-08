<script setup lang="ts">
import { ref, watch } from 'vue';
import { Modal, Icon, notification, Spin } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Input, UploadDragger, RadioGroup } from 'ant-design-vue';
import { ProjectType } from '@/enums/enums';
import { enumUtils } from '@xcan-angus/infra';
import { project } from '@/api/tester';
import { formatBytes } from '@/utils/common';

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

// Form state
const formRef = ref();
const isLoading = ref(false);
const selectedProjectType = ref<ProjectType>(ProjectType.AGILE);
const uploadedFile = ref<File | undefined>(undefined);
const projectName = ref<string>('');

// Project type options
const projectTypeOptions = enumUtils.enumToMessages(ProjectType).map(item => ({
  label: item.message,
  value: item.value as ProjectType
}));

// Handle project type selection
const handleProjectTypeChange = (e: any) => {
  selectedProjectType.value = e.target.value;
};

// Handle file selection
const handleFileSelection = (fileInfo: any) => {
  const file = fileInfo.file as File;
  
  // Validate file format
  const fileName = file.name.toLowerCase();
  const validExtensions = ['.zip', '.tar', '.tar.gz', '.tgz'];
  const isValidFormat = validExtensions.some(ext => fileName.endsWith(ext));
  
  if (!isValidFormat) {
    notification.error('不支持的文件格式，仅支持 ZIP (.zip)、TAR (.tar, .tar.gz, .tgz) 格式');
    return;
  }
  
  uploadedFile.value = file;
  
  // Set default project name from file name (without extension)
  if (!projectName.value) {
    const nameWithoutExt = file.name.replace(/\.(zip|tar|tar\.gz|tgz)$/i, '');
    projectName.value = nameWithoutExt;
  }
  
  formRef.value?.validateFields(['file']);
};

// Remove selected file
const removeFile = () => {
  uploadedFile.value = undefined;
  projectName.value = '';
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
  return Promise.reject(new Error('请上传项目文件'));
};

// Close modal
const closeModal = () => {
  emits('update:visible', false);
  // Reset form
  selectedProjectType.value = ProjectType.AGILE;
  uploadedFile.value = undefined;
  projectName.value = '';
  formRef.value?.resetFields();
};

// Submit import
const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    
    if (!uploadedFile.value) {
      notification.error('请上传项目文件');
      return;
    }
    
    if (!projectName.value.trim()) {
      notification.error('请输入项目名称');
      return;
    }
    
    isLoading.value = true;
    
    const formData = new FormData();
    formData.append('projectType', selectedProjectType.value);
    formData.append('name', projectName.value.trim());
    formData.append('file', uploadedFile.value);
    
    const [error] = await project.importProject(formData);
    
    isLoading.value = false;
    
    if (error) {
      notification.error(error.message || '导入失败');
      return;
    }
    
    notification.success('导入成功');
    emits('ok');
    closeModal();
  } catch (error) {
    isLoading.value = false;
    // Validation error, do nothing
  }
};

// Watch visible to reset form
watch(() => props.visible, (newValue) => {
  if (!newValue) {
    selectedProjectType.value = ProjectType.AGILE;
    uploadedFile.value = undefined;
    projectName.value = '';
    formRef.value?.resetFields();
  }
});
</script>

<template>
  <Modal
    title="导入项目"
    :visible="props.visible"
    :width="600"
    :centered="true"
    :footer="null"
    @cancel="closeModal">
    <Spin :spinning="isLoading">
      <Form
        ref="formRef"
        :model="{ projectType: selectedProjectType, file: uploadedFile, name: projectName }"
        layout="vertical"
        class="import-project-form">
        
        <!-- Project Type Selection -->
        <FormItem
          label="项目类型"
          name="projectType"
          :rules="[{ required: true, message: '请选择项目类型' }]"
          required>
          <RadioGroup
            v-model:value="selectedProjectType"
            :options="projectTypeOptions"
            @change="handleProjectTypeChange" />
        </FormItem>

        <!-- File Upload -->
        <FormItem
          label="导入文件"
          name="file"
          :rules="{ validator: validateFileSelection }"
          required>
          <div v-if="!uploadedFile" class="upload-area">
            <UploadDragger
              accept=".zip,.tar,.tar.gz,.tgz"
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
                  <div class="upload-title">拖拽文件到此处或点击上传</div>
                  <div class="upload-subtitle">支持格式: ZIP (.zip, 默认), TAR (.tar, .tar.gz, .tgz)</div>
                </div>
                <Button type="primary" class="upload-button">选择文件</Button>
              </div>
            </UploadDragger>
          </div>
          
          <div v-else class="file-preview">
            <div class="file-info">
              <Icon icon="icon-wendang" class="file-icon" />
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

        <!-- Project Name -->
        <FormItem
          label="项目名称"
          name="name"
          :rules="[{ required: true, message: '请输入项目名称' }]"
          required>
          <Input
            v-model:value="projectName"
            placeholder="请输入项目名称"
            :maxlength="100" />
        </FormItem>

        <!-- Footer Buttons -->
        <FormItem>
          <div class="form-footer">
            <Button @click="closeModal">取消</Button>
            <Button type="primary" @click="handleSubmit" :loading="isLoading">
              <Icon icon="icon-shangchuan" class="mr-1" />
              开始导入
            </Button>
          </div>
        </FormItem>
      </Form>
    </Spin>
  </Modal>
</template>

<style scoped>
.import-project-form {
  padding: 0;
}

/* Upload Area */
.upload-area {
  margin-bottom: 0;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 20px;
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

/* Radio Group Styles */
:deep(.ant-radio-group) {
  display: flex;
  gap: 12px;
}

:deep(.ant-radio-wrapper) {
  margin: 0;
  padding: 12px;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  background: #fff;
  transition: all 0.3s;
  cursor: pointer;
}

:deep(.ant-radio-wrapper:hover) {
  border-color: #40a9ff;
  background: #f0f9ff;
}

:deep(.ant-radio-wrapper-checked) {
  border-color: #1890ff;
  background: #e6f7ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

:deep(.ant-radio) {
  margin-right: 12px;
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
