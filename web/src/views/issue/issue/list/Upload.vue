<script lang="ts" setup>
import { inject, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Modal, Select, Spin } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, RadioGroup, UploadDragger } from 'ant-design-vue';
import { enumUtils, TESTER } from '@xcan-angus/infra';
import { StrategyWhenDuplicated } from '@/enums/enums';
import { formatBytes } from '@/utils/common';
import { issue } from '@/api/tester';

const { t } = useI18n();

// Props and Emits Definition
export interface Props{
  visible: boolean;
  downloadTemplate: ()=> void;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'ok'):void;}>();

// Injected Data
const projectId = inject<Ref<string>>('projectId', ref(''));
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>(
  'proTypeShowMap', ref({
    showTask: true,
    showBackLog: true,
    showMeeting: true,
    showSprint: true,
    showTasStatistics: true
  }
  ));

// Reactive Data
const isLoading = ref(false);
const strategyOptions = ref<{value: string; label: string}[]>([]);
const formRef = ref();

const formData = ref<{
  file: File|undefined;
  strategyWhenDuplicated: StrategyWhenDuplicated;
  sprintId: string|undefined;
}>({
  file: undefined,
  strategyWhenDuplicated: StrategyWhenDuplicated.COVER,
  sprintId: undefined
});

/**
 * Handle download template button click
 */
const handleDownloadTemplate = () => {
  if (typeof props.downloadTemplate === 'function') {
    props.downloadTemplate();
  }
};

/**
 * Handle file selection
 */
const handleFileSelection = (fileInfo: any) => {
  formData.value.file = fileInfo.file;
  formRef.value.validateFields(['file']);
};

/**
 * Remove selected file
 */
const removeSelectedFile = () => {
  formData.value.file = undefined;
};

/**
 * Validate if file is selected
 */
const validateFileSelection = async () => {
  if (formData.value.file) {
    return Promise.resolve();
  }
  return Promise.reject();
};

/**
 * Load strategy options for duplicate handling
 */
function loadStrategyOptions () {
  const data = enumUtils.enumToMessages(StrategyWhenDuplicated);
  strategyOptions.value = data.map(i => ({ value: i.value, label: i.message }));
}

/**
 * Close the modal dialog
 */
const closeModal = () => {
  emits('update:visible', false);
};

/**
 * Submit the form to import tasks
 */
const submitImportForm = () => {
  formRef.value.validate()
    .then(async () => {
      const formParams = new FormData();
      formData.value.sprintId && formParams.append('sprintId', formData.value.sprintId);
      formParams.append('projectId', projectId.value);
      formParams.append('strategyWhenDuplicated', formData.value.strategyWhenDuplicated);
      formData.value.file && formParams.append('file', formData.value.file);
      isLoading.value = true;
      const [error] = await issue.importTask(formParams);
      isLoading.value = false;
      if (error) {
        return;
      }
      emits('ok');
      closeModal();
    });
};

// Lifecycle Hooks
watch(() => props.visible, newValue => {
  if (!newValue) {
    formData.value.file = undefined;
    formData.value.strategyWhenDuplicated = StrategyWhenDuplicated.COVER;
  }

  if (!strategyOptions.value.length) {
    loadStrategyOptions();
  }
}, {
  immediate: true
});
</script>
<template>
  <Modal
    :title="t('issue.actions.importIssues')"
    :visible="props.visible"
    class="upload-modal"
    @cancel="closeModal"
    @ok="submitImportForm">
    <Form
      ref="formRef"
      :model="formData"
      size="small"
      class="upload-form">
      <FormItem
        v-if="proTypeShowMap.showSprint && props.visible"
        :label="t('common.sprint')"
        name="sprintId">
        <Select
          v-model:value="formData.sprintId"
          allowClear
          :disabled="!projectId"
          :action="`${TESTER}/task/sprint?projectId=${projectId}&fullTextSearch=true`"
          :defaultActiveFirstOption="true"
          :lazy="false"
          :placeholder="t('common.placeholders.selectSprint')"
          :fieldNames="{value: 'id', label: 'name'}"
          class="w-full" />
      </FormItem>

      <FormItem
        :rules="{message: t('issue.actions.upload.uploadFile'), validator: validateFileSelection}"
        name="file">
        <Spin :spinning="isLoading">
          <UploadDragger
            v-show="!formData.file"
            accept=".xls,.xlsx"
            :multiple="false"
            :showUploadList="false"
            :customRequest="handleFileSelection"
            class="text-3 leading-5 upload-dragger">
            <div class="flex flex-col items-center justify-center text-3 leading-5 upload-dragger-inner">
              <Icon icon="icon-shangchuan" class="text-5 text-text-link upload-icon" />
              <div class="mt-1 mb-1.5 text-text-link upload-title">{{ t('issue.actions.upload.selectFile') }}</div>
              <div class="text-theme-sub-content upload-subtitle">{{ t('issue.actions.upload.uploadTip') }}</div>
            </div>
          </UploadDragger>
        </Spin>

        <div
          v-show="!!formData.file"
          class="file-card">
          <div class="file-row">
            <div :title="formData.file?.name" class="file-name truncate">{{ formData.file?.name }}</div>
            <div class="file-size">{{ formatBytes(Number(formData.file?.size)) }}</div>
            <div class="file-actions">
              <Icon
                icon="icon-qingchu"
                class="cursor-pointer ml-2 text-3.5 text-theme-text-hover"
                @click="removeSelectedFile" />
            </div>
          </div>
        </div>

        <div class="text-right mt-2">
          <Button
            type="link"
            size="small"
            @click="handleDownloadTemplate">
            <Icon icon="icon-daochu1" class="text-4 cursor-pointer mr-1" />
            {{ t('issue.actions.upload.importTemplate') }}
          </Button>
        </div>
      </FormItem>

      <FormItem
        :label="t('issue.actions.upload.duplicateStrategy')"
        name="strategyWhenDuplicated"
        required>
        <RadioGroup
          v-model:value="formData.strategyWhenDuplicated"
          :options="strategyOptions"
          class="strategy-group">
        </RadioGroup>
      </FormItem>
    </Form>
  </Modal>
</template>

<style scoped>
.upload-modal :deep(.ant-modal-body) {
  padding: 16px 20px;
}

.upload-form :deep(.ant-form-item-label) {
  font-weight: 600;
}

.upload-form :deep(.ant-form-item) {
  margin-bottom: 12px;
}

.upload-dragger {
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  background: #fafafa;
  transition: all .2s ease;
}

.upload-dragger:hover {
  background: #f5faff;
  border-color: #93c5fd;
}

.upload-dragger-inner { padding: 18px 8px; }
.upload-icon { color: #3b82f6; }
.upload-title { font-weight: 600; }
.upload-subtitle { font-size: 12px; color: #6b7280; }

.file-card {
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
}
.file-row { display: flex; align-items: center; gap: 8px; }
.file-name { flex: 2; }
.file-size { flex: 1; text-align: right; color: #6b7280; }
.file-actions { flex: 1; text-align: right; color: #9ca3af; }

.strategy-group :deep(.ant-radio-wrapper) {
  margin-right: 16px;
}
</style>
