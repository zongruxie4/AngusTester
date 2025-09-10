<script setup lang="ts">
import { ref, watch, computed, onMounted, nextTick } from 'vue';
import { Upload } from 'ant-design-vue';
import { Icon} from '@xcan-angus/vue-ui';


import apiUtils from '@/utils/ApiUtils/index';

interface File {
formContentType: string;
  'x-xc-contentEncoding': 'base64';
  fileName: string;
  value: string;
}

interface Props {
  value?:File|File[];
  sizes?: number[];
  type: 'file'|'file(array)';
  maxFileSize: number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  maxFileSize: 0,
  type: 'file'
});

let totalSize = 0;

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'change', value?: File|File[]): void
}>();

const fileList = ref<any[]>([]);

const showSelectFile = computed(() => {
  return props.type === 'file(array)' || !fileList.value.length;
});

onMounted(async () => {
  if (props.value?.length || props.value?.value) {
    const files = Array.isArray(props.value) ? props.value : [props.value];
    if (!fileList.value?.length) {
      fileList.value = [];
      for (const i of files) {
        fileList.value.push({
          name: i.fileName,
          status: 'done',
          ...i
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

watch(() => fileList.value.length, async () => {
  nextTick(async () => {
    const data:File[] = [];
    totalSize = 0;
    fileList.value.forEach(item => {
      totalSize += (item.size || item.file.size);
      item.status = 'uploading';
    });
    // if (totalSize > props.maxFileSize) {
    //   fileList.value?.splice(-1);
    //   return;
    // }
    for (const file of fileList.value) {
      if (!file.value) {
        if (file.originFileObj) {
          const fileBase = await apiUtils.fileToBase64(file.originFileObj);
          file.value = fileBase;
        }
      }
      data.push({
        formContentType: file.type,
        'x-xc-contentEncoding': 'base64',
        fileName: file.name,
        value: file.value,
        file: file.originFileObj || undefined
      });
    }
    fileList.value.forEach(item => {
      totalSize += (item.size || item.file.size);
      item.status = 'done';
    });
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
    class="flex items-center flex-wrap">
    <template v-if="showSelectFile">
      <div class="flex items-center h-5 px-1.5 ml-1 my-0.5 select-none rounded text-3 border-none text-text-content bg-gray-bg">
        <Icon icon="icon-xuanze" class="mr-1 text-text-sub-content" />
        <slot name="text">
          选择文件
        </slot>
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
