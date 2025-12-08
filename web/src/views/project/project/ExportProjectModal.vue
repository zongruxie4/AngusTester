<script setup lang="ts">
import { ref, watch } from 'vue';
import { Modal, Icon, notification, Spin } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, RadioGroup, Input } from 'ant-design-vue';
import { project } from '@/api/tester';
import type { Project } from './types';

interface Props {
  visible: boolean;
  projectData?: Project;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  projectData: undefined
});

const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void;
}>();

// Export format options
type ExportFormat = 'zip' | 'tar' | 'tar.gz';

const selectedFormat = ref<ExportFormat>('zip');
const projectId = ref<string>('');
const isLoading = ref(false);

const formatOptions = [
  { label: 'ZIP (.zip)', value: 'zip' as ExportFormat },
  { label: 'TAR (.tar)', value: 'tar' as ExportFormat },
  { label: 'TAR.GZ (.tar.gz)', value: 'tar.gz' as ExportFormat }
];

// Close modal
const closeModal = () => {
  emits('update:visible', false);
  selectedFormat.value = 'zip';
  projectId.value = '';
};

// Handle export
const handleExport = async () => {


  isLoading.value = true;

  try {
    const [error, response] = await project.exportProject(
      projectId.value.trim(),
      selectedFormat.value
    );

    if (error) {
      notification.error(error.message || '导出失败');
      isLoading.value = false;
      return;
    }

    // Get blob from response
    let blob: Blob;
    if (response instanceof Blob) {
      blob = response;
    } else if (response?.data instanceof Blob) {
      blob = response.data;
    } else if (response?.data) {
      blob = new Blob([response.data], { type: 'application/octet-stream' });
    } else {
      notification.error('导出数据格式错误');
      isLoading.value = false;
      return;
    }

    // Create download link
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.style.display = 'none';
    link.href = url;
    
    // Generate filename from project name or ID
    const projectName = props.projectData?.name 
      ? props.projectData.name.replace(/[^a-zA-Z0-9\u4e00-\u9fa5]/g, '_')
      : `project_${projectId.value}`;
    const extension = selectedFormat.value === 'tar.gz' ? 'tar.gz' : selectedFormat.value;
    link.download = `${projectName}.${extension}`;
    
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    // Clean up
    URL.revokeObjectURL(url);
    
    notification.success('导出成功');
    closeModal();
  } catch (error: any) {
    notification.error(error.message || '导出失败');
  } finally {
    isLoading.value = false;
  }
};

// Watch visible to initialize project ID
watch(() => props.visible, (newValue) => {
  if (newValue) {
    // Set project ID from projectData if available
    if (props.projectData?.id) {
      projectId.value = String(props.projectData.id);
    } else {
      projectId.value = '';
    }
    selectedFormat.value = 'zip';
  }
});
</script>

<template>
  <Modal
    title="导出项目"
    :visible="props.visible"
    :width="500"
    :centered="true"
    :footer="null"
    @cancel="closeModal">
    <Spin :spinning="isLoading">
      <Form
        layout="vertical"
        class="export-project-form">
        
        <!-- Project Info -->
        <FormItem label="项目信息" v-if="projectData">
          <div class="project-info">
            <div class="project-name">
              <Icon icon="icon-xiangmu" class="project-icon" />
              <span>{{ projectData.name || '-' }}</span>
            </div>
            <div class="project-id" v-if="projectData.id">
              <span class="id-label">ID：</span>
              <span class="id-value">{{ projectData.id }}</span>
            </div>
          </div>
        </FormItem>

        <!-- Export Format Selection -->
        <FormItem
          label="导出格式"
          required>
          <RadioGroup
            v-model:value="selectedFormat"
            :options="formatOptions"
            class="format-radio-group" />
        </FormItem>

        <!-- Format Description -->
        <div class="format-description">
          <div class="description-item">
            <Icon icon="icon-wendang" class="description-icon zip" />
            <div class="description-content">
              <div class="description-title">ZIP (.zip)</div>
              <div class="description-text">最常用的压缩格式，兼容性最好，Windows和Mac系统都支持</div>
            </div>
          </div>
          <div class="description-item">
            <Icon icon="icon-wendang" class="description-icon tar" />
            <div class="description-content">
              <div class="description-title">TAR (.tar)</div>
              <div class="description-text">Unix/Linux系统常用的归档格式，未压缩的打包文件</div>
            </div>
          </div>
          <div class="description-item">
            <Icon icon="icon-wendang" class="description-icon tar-gz" />
            <div class="description-content">
              <div class="description-title">TAR.GZ (.tar.gz)</div>
              <div class="description-text">TAR格式的压缩文件，结合了归档和压缩，文件更小</div>
            </div>
          </div>
        </div>

        <!-- Footer Buttons -->
        <FormItem>
          <div class="form-footer">
            <Button @click="closeModal">取消</Button>
            <Button type="primary" @click="handleExport" :loading="isLoading">
              <Icon icon="icon-daochu1" class="mr-1" />
              开始导出
            </Button>
          </div>
        </FormItem>
      </Form>
    </Spin>
  </Modal>
</template>

<style scoped>
.export-project-form {
  padding: 0;
}

/* Project Info */
.project-info {
  padding: 12px;
  background: #f5f5f5;
  border-radius: 6px;
}

.project-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 8px;
}

.project-icon {
  font-size: 18px;
  color: #1890ff;
}

.project-id {
  font-size: 13px;
  color: #8c8c8c;
}

.id-label {
  margin-right: 4px;
}

.id-value {
  color: #1890ff;
  font-weight: 500;
}

/* Format Radio Group */
.format-radio-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.format-radio-group :deep(.ant-radio-wrapper) {
  margin: 0;
  padding: 12px;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  background: #fff;
  transition: all 0.3s;
  cursor: pointer;
}

.format-radio-group :deep(.ant-radio-wrapper:hover) {
  border-color: #40a9ff;
  background: #f0f9ff;
}

.format-radio-group :deep(.ant-radio-wrapper-checked) {
  border-color: #1890ff;
  background: #e6f7ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

.format-radio-group :deep(.ant-radio) {
  margin-right: 12px;
}

/* Format Description */
.format-description {
  margin-top: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.description-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.description-item:last-child {
  margin-bottom: 0;
}

.description-icon {
  font-size: 20px;
  margin-top: 2px;
  flex-shrink: 0;
}

.description-icon.zip {
  color: #52c41a;
}

.description-icon.tar {
  color: #1890ff;
}

.description-icon.tar-gz {
  color: #fa8c16;
}

.description-content {
  flex: 1;
}

.description-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
}

.description-text {
  font-size: 12px;
  color: #8c8c8c;
  line-height: 1.5;
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

:deep(.ant-form-item-label > label.ant-form-item-required:not(.ant-form-item-required-mark-optional)::before) {
  display: inline-block;
  margin-right: 4px;
  color: #ff4d4f;
  font-size: 14px;
  font-family: SimSun, sans-serif;
  line-height: 1;
  content: '*';
}
</style>
