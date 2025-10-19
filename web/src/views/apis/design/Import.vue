<script lang="ts" setup>
import { ref } from 'vue';
import { Modal, Input, notification, Icon, Spin } from '@xcan-angus/vue-ui';
import { Form, FormItem, UploadDragger, Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { apis } from '@/api/tester';

interface Props {
  visible: boolean;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  projectId: ''
});

const emits = defineEmits<{
  (e: 'cancel'):void;
  (e: 'ok'):void;
  (e: 'update:visible', value: boolean):void
}>();

const { t } = useI18n();

const loading = ref(false);
const formRef = ref();
const fileList = ref<any[]>([]);
const formState = ref<{ name: string | undefined; content: string | undefined }>({
  name: undefined,
  content: undefined
});

/**
 * Read selected file content and set into form state.
 */
const handleFile = async (fileObj) => {
  const file = fileObj.file;
  const reader = new FileReader();

  reader.onload = (ev: ProgressEvent<FileReader>) => {
    const result = ev.target?.result as string | null;
    formState.value.content = result || '';
    fileList.value = [{
      name: file.name,
      status: 'done',
      uid: `${Date.now()}`
    }];
    formRef.value.validate(['content']);
  };
  reader.readAsText(file);
};

/**
 * Remove current selected file and clear content.
 */
const delFile = () => {
  fileList.value = [];
  formState.value.content = undefined;
};

/**
 * Close modal without importing.
 */
const cancel = () => {
  emits('update:visible', false);
};

/**
 * Validate fields then submit import request with file content.
 */
const ok = async () => {
  formRef.value.validate().then(async () => {
    loading.value = true;
    const formData = new FormData();
    formData.append('projectId', props.projectId);
    formData.append('name', formState.value.name || '');
    formData.append('content', formState.value.content || '');
    const [error] = await apis.importDesign(formData);
    loading.value = false;
    if (error) {
      return;
    }
    notification.success(t('actions.tips.importSuccess'));
    cancel();
    emits('ok');
  });
};
</script>
<template>
  <Modal
    :title="t('apiDesign.actions.importDesign')"
    :visible="props.visible"
    class="import-modal"
    :width="600"
    :centered="true"
    :okButtonProps="{
      loading
    }"
    @cancel="cancel"
    @ok="ok">
    <div class="import-container">
      <Form
        ref="formRef"
        :model="formState"
        size="small"
        class="import-form"
        :labelCol="{ flex: '80px' }"
        labelAlign="right">
        <FormItem
          name="name"
          :label="t('common.name')"
          required>
          <Input
            v-model:value="formState.name"
            :maxlength="100"
            :placeholder="t('apiDesign.messages.namePlaceholder')" />
        </FormItem>

        <FormItem
          name="content"
          :label="t('common.file')"
          :rules="[{required: true, message: t('apiDesign.importDesignModal.fileRule')}]">
          <Spin :spinning="loading">
            <UploadDragger
              v-show="!formState.content"
              v-model:fileList="fileList"
              accept=".json,.yaml"
              :multiple="false"
              :showUploadList="false"
              :customRequest="handleFile"
              class="enhanced-upload-dragger">
              <div class="upload-content">
                <div class="upload-icon-container">
                  <Icon icon="icon-daoru" class="upload-main-icon" />
                  <div class="upload-icon-bg"></div>
                </div>
                <div class="upload-text">
                  <div class="upload-title">{{ t('apiDesign.importDesignModal.uploadFile') }}</div>
                  <div class="upload-subtitle">{{ t('apiDesign.importDesignModal.uploadTip') }}</div>
                </div>
              </div>
            </UploadDragger>
          </Spin>

          <div
            v-show="!!formState.content"
            class="file-preview-card">
            <div class="file-preview-content">
              <div class="file-icon">
                <Icon icon="icon-json" class="file-type-icon" />
              </div>
              <div class="file-info">
                <div :title="fileList[0]?.name" class="file-name">{{ fileList[0]?.name }}</div>
                <div class="file-meta">
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
                  @click="delFile">
                  <Icon icon="icon-qingchu" class="remove-icon" />
                </Button>
              </div>
            </div>
          </div>
        </FormItem>
      </Form>
    </div>
  </Modal>
</template>
<style scoped>
/* Modal Styles */
.import-modal :deep(.ant-modal-body) {
  padding: 0;
}

.import-modal :deep(.ant-modal-header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-bottom: none;
  border-radius: 8px 8px 0 0;
}

.import-modal :deep(.ant-modal-title) {
  color: white;
  font-weight: 600;
  font-size: 16px;
}

.import-modal :deep(.ant-modal-close) {
  color: white;
}

.import-modal :deep(.ant-modal-close:hover) {
  color: rgba(255, 255, 255, 0.8);
}

/* Container */
.import-container {
  padding: 20px;
  background: #fafbfc;
}

/* Form Styles */
.import-form :deep(.ant-form-item) {
  margin-bottom: 16px;
}

.import-form :deep(.ant-form-item-label) {
  font-weight: 600;
  color: #262626;
  font-size: 14px;
  margin-bottom: 8px;
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
</style>
