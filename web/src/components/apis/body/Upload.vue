<script setup lang="ts">
// Vue core imports
import { ref, watch, computed, onMounted, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import type { UploadProps } from 'ant-design-vue';
import { Upload } from 'ant-design-vue';
import { Icon, notification } from '@xcan-angus/vue-ui';

// Infrastructure imports
import { codeUtils } from '@xcan-angus/infra';

// Utility imports
import { API_EXTENSION_KEY } from '@/utils/apis';

const { t } = useI18n();

// API extension key constants
const { valueKey, fileNameKey, formContentTypeKey } = API_EXTENSION_KEY;
const { gzip, ungzip } = codeUtils;

/**
 * File interface for upload component
 */
interface UploadFile {
  [formContentTypeKey]: string;
  'x-xc-contentEncoding': 'gzip_base64';
  [fileNameKey]: string;
  [valueKey]: string;
}

// Legacy export alias for backward compatibility
export type File = UploadFile;

/**
 * Component props interface
 */
interface Props {
  value?: UploadFile | UploadFile[];
  sizes: number[];
  type: 'file' | 'file(array)';
  maxFileSize: number;
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  maxFileSize: 0
});

// File size tracking
let totalFileSize = 0;

// Component events
const emit = defineEmits<{
  (e: 'change', value?: UploadFile | UploadFile[]): void;
}>();

// Upload file list state
const uploadFileList = ref<any[]>([]);

/**
 * Handle file upload validation before upload
 * @param file - File to be uploaded
 * @returns False to prevent automatic upload
 */
const handleFileUploadValidation: UploadProps['beforeUpload'] = async (file) => {
  if (totalFileSize + file.size > props.maxFileSize) {
    notification.warning(t('xcan_apiBody.totalFileSizeExceeded'));
  }
  return false;
};

/**
 * Load file asynchronously from base64 encoded data
 * @param base64Text - Base64 encoded file data
 * @param name - File name
 * @returns File object
 */
const loadFileFromBase64 = async (base64Text: string, name: string) => {
  const buffer = await ungzip(base64Text);
  return await new File([buffer], name);
};

/**
 * Computed property to determine if file selection should be shown
 */
const shouldShowFileSelection = computed(() => {
  return props.type === 'file(array)' || !uploadFileList.value.length;
});

/**
 * Initialize upload component with existing files
 */
onMounted(async () => {
  if (props.value?.length || props.value?.[valueKey]) {
    const existingFiles = Array.isArray(props.value) ? props.value : [props.value];
    if (!uploadFileList.value?.length) {
      uploadFileList.value = [];
      for (const fileItem of existingFiles) {
        const originFileObject = fileItem.originFileObj ? fileItem.originFileObj : await loadFileFromBase64(fileItem[valueKey], fileItem[fileNameKey]);
        if (!fileItem[formContentTypeKey]) {
          fileItem[formContentTypeKey] = (originFileObject as any).type;
        }
        uploadFileList.value.push({
          name: fileItem[fileNameKey],
          status: 'done',
          ...fileItem,
          originFileObj: originFileObject
        });
      }
    }
  } else {
    uploadFileList.value = [];
  }
});

// Watch for upload type changes and reset file list
watch(() => props.type, () => {
  uploadFileList.value = [];
});

// Watch for file list changes and process files
watch(() => uploadFileList.value, async () => {
  nextTick(async () => {
    const processedFiles: UploadFile[] = [];
    totalFileSize = 0;
    uploadFileList.value.forEach(fileItem => {
      totalFileSize += (fileItem.size || fileItem.file.size);
    });
    if (totalFileSize > props.maxFileSize) {
      uploadFileList.value?.splice(-1);
      return;
    }
    for (const fileItem of uploadFileList.value) {
      if (!fileItem[valueKey]) {
        if (fileItem.originFileObj) {
          const compressedFileData = await gzip(fileItem.originFileObj);
          fileItem[valueKey] = compressedFileData;
        }
      }
      processedFiles.push({
        [formContentTypeKey]: fileItem.type,
        'x-xc-contentEncoding': 'gzip_base64',
        [fileNameKey]: fileItem.name,
        [valueKey]: fileItem[valueKey],
        file: fileItem.originFileObj || undefined
      });
    }
    if (processedFiles.length === 0) {
      emit('change', props.type === 'file(array)' ? [] : undefined);
      return;
    }
    if (props.type === 'file') {
      emit('change', processedFiles[0]);
      return;
    }
    if (props.type === 'file(array)') {
      emit('change', processedFiles);
    }
  });
}, {
  deep: true
});
</script>
<template>
  <Upload
    v-model:fileList="uploadFileList"
    :multiple="props.type === 'file(array)'"
    :beforeUpload="handleFileUploadValidation"
    class="flex items-center flex-wrap">
    <template v-if="shouldShowFileSelection">
      <div class="flex items-center h-5 px-1.5 ml-1 my-0.5 select-none rounded text-3 border-none text-theme-content bg-gray-bg">
        <Icon icon="icon-xuanze" class="mr-1 text-theme-sub-content" />
        {{ t('xcan_apiBody.selectFile') }}
      </div>
    </template>
  </Upload>
</template>
<style scoped>
:deep(.ant-upload-list.ant-upload-list-text) {
  display: flex;
}

:deep(.ant-upload-list.ant-upload-list-text) .ant-upload-list-text-container {
  @apply !my-0.25;

  transition: none !important;
}

:deep(.ant-upload-list.ant-upload-list-text) .ant-upload-list-item-name {
  @apply truncate;

  max-width: 150px;
}

:deep(.ant-upload-list-item-card-actions-btn) {
  opacity: 1;
}

:deep(.ant-upload-list-item) .ant-upload-list-item-info {
  background-color: #f5f5f5;
}

:deep(.ant-upload-list-item) {
  @apply !mt-0 ml-1;
}
</style>
