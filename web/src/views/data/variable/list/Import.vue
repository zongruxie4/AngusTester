<script setup lang="ts">
import { computed, ref } from 'vue';
import { Radio, RadioGroup, TypographyParagraph, UploadDragger, UploadFile } from 'ant-design-vue';
import { Icon, Modal, Spin } from '@xcan-angus/vue-ui';
import { variable } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { StrategyWhenDuplicated } from '@/enums/enums';
import { formatBytes } from '@/utils/common';
import { BasicProps } from '@/types/types';

const { t } = useI18n();

// Component props
const props = withDefaults(defineProps<BasicProps>(), {
  visible: false,
  projectId: undefined
});

// Component emits
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'update:visible', value: boolean): void;
  (event: 'ok'): void;
}>();

// Constants
const MAX_FILE_SIZE_MB = 20;

// Reactive state
const loading = ref(false);
const originFile = ref<UploadFile>();
const strategyWhenDuplicated = ref<StrategyWhenDuplicated>(StrategyWhenDuplicated.COVER);
const uploadErrorMsg = ref<string>();

/**
 * Handle file upload change events
 *
 * @param file - Uploaded file information
 */
const handleUploadChange = async ({ file }: { file: UploadFile }) => {
  if (!file) {
    resetFileState();
    return;
  }

  // Validate file size
  if (file?.size && file.size > maxFileSize.value) {
    uploadErrorMsg.value = t('dataVariable.importModal.fileSizeError', { maxSize: MAX_FILE_SIZE_MB });
    originFile.value = undefined;
    return;
  }

  originFile.value = file;
  uploadErrorMsg.value = undefined;
};

/**
 * Custom upload request handler (disabled for manual control)
 *
 * @returns false to prevent automatic upload
 */
const customRequest = () => {
  return false;
};

/**
 * Delete uploaded file
 */
const deleteFile = () => {
  resetFileState();
};

/**
 * Cancel import operation
 */
const cancelImport = () => {
  resetState();
  emit('update:visible', false);
};

/**
 * Execute import operation
 */
const executeImport = async () => {
  if (!originFile.value?.originFileObj) {
    return;
  }

  const formData = new FormData();
  const file = originFile.value.originFileObj as File;

  formData.append('file', file);
  formData.append('strategyWhenDuplicated', strategyWhenDuplicated.value);
  formData.append('projectId', props.projectId);

  loading.value = true;

  try {
    const [error] = await variable.importVariables(formData);

    if (error) {
      uploadErrorMsg.value = error.message;
      return;
    }

    // Import successful
    resetState();
    emit('update:visible', false);
    emit('ok');
  } finally {
    loading.value = false;
  }
};

/**
 * Reset file-related state
 */
const resetFileState = () => {
  originFile.value = undefined;
  uploadErrorMsg.value = undefined;
};

/**
 * Reset all component state
 */
const resetState = () => {
  resetFileState();
  strategyWhenDuplicated.value = StrategyWhenDuplicated.COVER;
};

// Computed properties
const okButtonProps = computed(() => ({
  disabled: !originFile.value
}));

const maxFileSize = computed(() => {
  return 1024 * 1024 * MAX_FILE_SIZE_MB;
});

const ellipsis = computed(() => ({
  rows: 5,
  expandable: false,
  tooltip: uploadErrorMsg.value
}));

// Accepted file types
const acceptedFileTypes = '.zip,.rar,.7z,.gz,.tar,.bz2,.xz,.lzma,.json,.yaml,.yml';
</script>

<template>
  <Modal
    :visible="props.visible"
    :width="600"
    :okButtonProps="okButtonProps"
    :confirmLoading="loading"
    :title="t('dataVariable.importModal.title')"
    @cancel="cancelImport"
    @ok="executeImport">
    <Spin :spinning="loading" class="mb-5">
      <!-- File Upload Area -->
      <UploadDragger
        v-show="!originFile"
        :class="{ 'error-border': !!uploadErrorMsg }"
        :multiple="false"
        :showUploadList="false"
        :customRequest="customRequest"
        :accept="acceptedFileTypes"
        @change="handleUploadChange">
        <div class="flex flex-col items-center justify-center text-3 leading-5">
          <Icon icon="icon-shangchuan" class="text-5 text-text-link" />
          <div class="mt-1 mb-1.5 text-text-link">
            {{ t('dataVariable.importModal.uploadArea.selectFile') }}
          </div>
          <div class="text-theme-sub-content">
            {{ t('dataVariable.importModal.uploadArea.description', { maxSize: MAX_FILE_SIZE_MB }) }}
          </div>
        </div>
      </UploadDragger>

      <!-- File Preview -->
      <div
        v-show="!!originFile"
        :class="{ 'border-status-error': !!uploadErrorMsg }"
        class="px-3.5 border rounded">
        <div class="flex py-2">
          <div :title="originFile?.name" class="flex-2 truncate">
            {{ originFile?.name }}
          </div>
          <div class="flex-1 text-right">
            {{ formatBytes(+(originFile?.size || 0)) }}
          </div>
          <div class="flex-1 text-right">
            <Icon
              icon="icon-qingchu"
              class="cursor-pointer ml-2 text-3.5 text-theme-text-hover"
              @click="deleteFile" />
          </div>
        </div>
      </div>

      <!-- Error Message Display -->
      <TypographyParagraph
        :content="uploadErrorMsg"
        :ellipsis="ellipsis"
        class="text-status-error mt-1" />
    </Spin>

    <!-- Duplicate Strategy Selection -->
    <div class="space-y-0.5 leading-5 text-3">
      <div>{{ t('dataVariable.importModal.duplicateStrategy.title') }}</div>
      <RadioGroup v-model:value="strategyWhenDuplicated">
        <Radio value="COVER">
          {{ t('actions.cover') }}
        </Radio>
        <Radio value="IGNORE">
          {{ t('actions.ignore') }}
        </Radio>
      </RadioGroup>
    </div>
  </Modal>
</template>

<style scoped>
:deep(.ant-upload.ant-upload-drag.error-border) {
  @apply border-border-error;
}

.flex-2 {
  flex: 2 1 0%;
}
</style>
