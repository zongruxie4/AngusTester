<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import type { UploadProps } from 'ant-design-vue';
import { Upload } from 'ant-design-vue';
import { Icon, notification } from '@xcan-angus/vue-ui';
import { API_EXTENSION_KEY, SchemaWideType } from '@/utils/apis';
import { codeUtils } from '@xcan-angus/infra';

const { valueKey, fileNameKey, formContentTypeKey, contentEncoding } = API_EXTENSION_KEY;
const { gzip, ungzip } = codeUtils;

/**
 * File interface for upload
 */
interface UploadFileItem {
  /** Form content type */
  [formContentTypeKey]: string;
  /** Content encoding */
  [contentEncoding]: 'gzip_base64';
  /** File name */
  [fileNameKey]: string;
  /** File value */
  [valueKey]: string;
  /** Origin file object */
  originFileObj?: File;
  /** File type */
  type?: string;
  /** File status */
  status?: string;
  /** File name */
  name?: string;
}

/**
 * Component props interface
 */
interface Props {
  value?: UploadFileItem | UploadFileItem[];
  sizes: number[];
  type: SchemaWideType.file | SchemaWideType.file_array;
  maxFileSize: number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  maxFileSize: 0
});

const emits = defineEmits<{
  (e: 'change', value?: { file?: File | File[]; size: number }): void;
}>();

const { t } = useI18n();

let totalFileSize = 0;

const fileList = ref<any[]>([]);

/**
 * Handles file before upload
 * <p>
 * Validates file size before upload
 * </p>
 * @param file - File to upload
 * @returns False to prevent automatic upload
 */
const handleBeforeUpload: UploadProps['beforeUpload'] = async (file) => {
  if (totalFileSize + file.size > props.maxFileSize) {
    notification.warning(t('service.apiUpload.messages.fileSizeLimit'));
  }
  return false;
};

/**
 * Loads file asynchronously from base64
 * <p>
 * Decompresses and creates file from base64 text
 * </p>
 * @param base64Text - Base64 encoded file content
 * @param fileName - Name of the file
 * @returns Promise resolving to File object
 */
const loadFileAsynchronously = async (base64Text: string, fileName: string) => {
  const buffer = await ungzip(base64Text);
  return await new File([buffer], fileName);
};

/**
 * Computed property to show file selection
 * <p>
 * Determines whether to show file selection button
 * </p>
 */
const shouldShowFileSelection = computed(() => {
  return props.type === SchemaWideType.file_array || !fileList.value.length;
});

/**
 * Component mounted hook
 * <p>
 * Initializes file list from props value
 * </p>
 */
onMounted(async () => {
  if (props.value?.length || props.value?.[valueKey]) {
    const files = Array.isArray(props.value) ? props.value : [props.value];
    if (!fileList.value?.length) {
      fileList.value = [];
      for (const fileItem of files) {
        const originFileObject = fileItem.originFileObj
          ? fileItem.originFileObj
          : (typeof fileItem[valueKey] === 'string' && typeof fileItem[fileNameKey] === 'string'
              ? await loadFileAsynchronously(fileItem[valueKey], fileItem[fileNameKey])
              : new File([], ''));
        if (!fileItem[formContentTypeKey]) {
          fileItem[formContentTypeKey] = originFileObject.type;
        }
        fileList.value.push({
          name: fileItem[fileNameKey],
          status: 'done',
          ...fileItem,
          originFileObj: originFileObject
        });
      }
    }
  } else {
    fileList.value = [];
  }
});

/**
 * Watches for changes in upload type
 * <p>
 * Clears file list when upload type changes
 * </p>
 */
watch(() => props.type, () => {
  fileList.value = [];
});

/**
 * Watches for changes in file list
 * <p>
 * Processes files and emits change event
 * </p>
 */
watch(() => fileList.value, async () => {
  nextTick(async () => {
    const processedFiles: UploadFileItem[] = [];
    totalFileSize = 0;
    fileList.value.forEach(item => {
      totalFileSize += (item.size || item.file.size);
    });
    if (totalFileSize > props.maxFileSize) {
      fileList.value?.splice(-1);
      return;
    }
    for (const fileItem of fileList.value) {
      if (!fileItem[valueKey]) {
        if (fileItem.originFileObj) {
          fileItem[valueKey] = await gzip(fileItem.originFileObj);
        }
      }
      processedFiles.push({
        [formContentTypeKey]: fileItem.type,
        [contentEncoding]: 'gzip_base64',
        [fileNameKey]: fileItem.name,
        [valueKey]: fileItem[valueKey],
        originFileObj: fileItem.originFileObj || undefined
      } as UploadFileItem);
    }
    if (processedFiles.length === 0) {
      emits('change', { file: props.type === SchemaWideType.file_array ? [] : undefined, size: 0 });
      return;
    }
    if (props.type === SchemaWideType.file) {
      // Extract the actual File object from UploadFileItem
      const fileObj = processedFiles[0].originFileObj || new File([], '');
      emits('change', { file: fileObj, size: totalFileSize });
      return;
    }
    if (props.type === SchemaWideType.file_array) {
      // Extract the actual File objects from UploadFileItem array
      const fileObjs = processedFiles.map(item => item.originFileObj || new File([], ''));
      emits('change', { file: fileObjs, size: totalFileSize });
    }
  });
}, {
  deep: true
});
</script>
<template>
  <Upload
    v-model:fileList="fileList"
    :multiple="props.type === SchemaWideType.file_array"
    :beforeUpload="handleBeforeUpload"
    class="flex items-center flex-wrap">
    <template v-if="shouldShowFileSelection">
      <div class="flex items-center h-5 px-1.5 ml-1 my-0.5 select-none rounded text-3 border-none text-text-content bg-gray-bg">
        <Icon icon="icon-xuanze" class="mr-1 text-text-sub-content" />
        {{ t('service.apiUpload.actions.selectFile') }}
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
