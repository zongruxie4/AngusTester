<script setup lang="ts">
import { ref, watch } from 'vue';
import { Modal, Icon, notification, Spin } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, RadioGroup } from 'ant-design-vue';
import { template } from '@/api/tester';
import { TestTemplateDetail } from '../types';

interface Props {
  visible: boolean;
  templateData?: TestTemplateDetail;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  templateData: undefined
});

const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void;
}>();

// Export format options
type ExportFormat = 'excel' | 'csv' | 'json';

const selectedFormat = ref<ExportFormat>('excel');
const isLoading = ref(false);

const formatOptions = [
  { label: 'Excel (.xlsx)', value: 'excel' as ExportFormat },
  { label: 'CSV (.csv)', value: 'csv' as ExportFormat },
  { label: 'JSON (.json)', value: 'json' as ExportFormat }
];

// Close modal
const closeModal = () => {
  emits('update:visible', false);
  selectedFormat.value = 'excel';
};

// Handle export
const handleExport = async () => {
  if (!props.templateData) {
    notification.error('模板数据不存在');
    return;
  }

  isLoading.value = true;
  
  try {
    const [error, response] = await template.downloadTemplate(
      props.templateData.id,
      selectedFormat.value
    );

    if (error) {
      notification.error(error.message || '导出失败');
      isLoading.value = false;
      return;
    }

    // Get blob from response
    // Response structure: { data: Blob } or just Blob
    let blob: Blob;
    if (response instanceof Blob) {
      blob = response;
    } else if (response?.data instanceof Blob) {
      blob = response.data;
    } else if (response?.data) {
      // If data is not a Blob, try to create one
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
    
    // Generate filename
    const templateName = props.templateData.name.replace(/[^a-zA-Z0-9\u4e00-\u9fa5]/g, '_');
    const extension = selectedFormat.value === 'excel' ? 'xlsx' : selectedFormat.value;
    link.download = `${templateName}.${extension}`;
    
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

// Watch visible to reset form
watch(() => props.visible, (newValue) => {
  if (!newValue) {
    selectedFormat.value = 'excel';
  }
});
</script>

<template>
  <Modal
    title="模板导出"
    :visible="props.visible"
    :width="500"
    :centered="true"
    :footer="null"
    @cancel="closeModal">
    <Spin :spinning="isLoading">
      <Form
        layout="vertical"
        class="export-template-form">
        
        <!-- Template Info -->
        <FormItem label="模板信息">
          <div class="template-info">
            <div class="template-name">
              <Icon icon="icon-wendang" class="template-icon" />
              <span>{{ templateData?.name || '-' }}</span>
            </div>
            <div class="template-type" v-if="templateData">
              <span class="type-label">类型：</span>
              <span class="type-value">{{ templateData.templateType }}</span>
            </div>
          </div>
        </FormItem>

        <!-- Export Format Selection -->
        <FormItem
          label="选择导出格式"
          required>
          <RadioGroup
            v-model:value="selectedFormat"
            :options="formatOptions"
            class="format-radio-group" />
        </FormItem>

        <!-- Format Description -->
        <div class="format-description">
          <div class="description-item">
            <Icon icon="icon-wendang" class="description-icon excel" />
            <div class="description-content">
              <div class="description-title">Excel (.xlsx)</div>
              <div class="description-text">适用于 Microsoft Excel 和其他电子表格软件</div>
            </div>
          </div>
          <div class="description-item">
            <Icon icon="icon-wendang" class="description-icon csv" />
            <div class="description-content">
              <div class="description-title">CSV (.csv)</div>
              <div class="description-text">纯文本格式，兼容性最好，可用任何文本编辑器打开</div>
            </div>
          </div>
          <div class="description-item">
            <Icon icon="icon-wendang" class="description-icon json" />
            <div class="description-content">
              <div class="description-title">JSON (.json)</div>
              <div class="description-text">结构化数据格式，便于程序处理和解析</div>
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
.export-template-form {
  padding: 0;
}

/* Template Info */
.template-info {
  padding: 12px;
  background: #f5f5f5;
  border-radius: 6px;
}

.template-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 8px;
}

.template-icon {
  font-size: 18px;
  color: #1890ff;
}

.template-type {
  font-size: 13px;
  color: #8c8c8c;
}

.type-label {
  margin-right: 4px;
}

.type-value {
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

.description-icon.excel {
  color: #52c41a;
}

.description-icon.csv {
  color: #1890ff;
}

.description-icon.json {
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
