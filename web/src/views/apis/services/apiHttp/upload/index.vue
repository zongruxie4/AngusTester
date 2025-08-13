<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import type { UploadProps } from 'ant-design-vue';
import { Upload } from 'ant-design-vue';
import { Icon, notification } from '@xcan-angus/vue-ui';
import { API_EXTENSION_KEY } from '@/views/apis/utils';
import { codeUtils } from '@xcan-angus/infra';

const { valueKey, fileNameKey, formContentTypeKey } = API_EXTENSION_KEY;
const { gzip, ungzip } = codeUtils;

interface File {
  [formContentTypeKey]: string;
  'x-xc-contentEncoding': 'gzip_base64';
  [fileNameKey]: string;
  [valueKey]: string;
}

interface Props {
  value?:File|File[];
  sizes: number[];
  type: 'file'|'file(array)';
  maxFileSize: number;
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  maxFileSize: 0
});

let totalSize = 0;

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'change', value?: File|File[]): void
}>();

const fileList = ref<any[]>([]);

const beforeUpload: UploadProps['beforeUpload'] = async (file) => {
  if (totalSize + file.size > props.maxFileSize) {
    notification.warning(t('service.apiUpload.messages.fileSizeLimit'));
  }
  return false;
};
const loadAsync = async (base64Text, name) => {
  const buf = await ungzip(base64Text);
  return await new File([buf], name);
};

const showSelectFile = computed(() => {
  return props.type === 'file(array)' || !fileList.value.length;
});

onMounted(async () => {
  if (props.value?.length || props.value?.[valueKey]) {
    const files = Array.isArray(props.value) ? props.value : [props.value];
    if (!fileList.value?.length) {
      fileList.value = [];
      for (const i of files) {
        const originFileObj = i.originFileObj ? i.originFileObj : await loadAsync(i[valueKey], i[fileNameKey]);
        if (!i[formContentTypeKey]) {
          i[formContentTypeKey] = originFileObj.type;
        }
        fileList.value.push({
          name: i[fileNameKey],
          status: 'done',
          ...i,
          originFileObj
        });
      }
    }
  } else {
    fileList.value = [];
  }
});

watch(() => props.type, () => {
  fileList.value = [];
});

watch(() => fileList.value, async () => {
  nextTick(async () => {
    const data:File[] = [];
    totalSize = 0;
    fileList.value.forEach(item => {
      totalSize += (item.size || item.file.size);
    });
    if (totalSize > props.maxFileSize) {
      fileList.value?.splice(-1);
      return;
    }
    for (const file of fileList.value) {
      if (!file[valueKey]) {
        if (file.originFileObj) {
          const fileBase = await gzip(file.originFileObj);
          file[valueKey] = fileBase;
        }
      }
      data.push({
        [formContentTypeKey]: file.type,
        'x-xc-contentEncoding': 'gzip_base64',
        [fileNameKey]: file.name,
        [valueKey]: file[valueKey],
        file: file.originFileObj || undefined
      });
    }
    if (data.length === 0) {
      emits('change', { file: props.type === 'file(array)' ? [] : undefined, size: 0 });
      return;
    }
    if (props.type === 'file') {
      emits('change', { file: data[0], size: totalSize });
      return;
    }
    if (props.type === 'file(array)') {
      emits('change', { file: data, size: totalSize });
    }
  });
}, {
  deep: true
});
</script>
<template>
  <Upload
    v-model:fileList="fileList"
    :multiple="props.type === 'file(array)'"
    :beforeUpload="beforeUpload"
    class="flex items-center flex-wrap">
    <template v-if="showSelectFile">
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
