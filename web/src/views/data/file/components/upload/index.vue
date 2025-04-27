<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { upload, cookie } from '@xcan-angus/tools';
import { Progress, Upload } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';

import { formatBytes } from '@/utils/common';

interface SourceType {
  name: string,
  size: string,
  progress: number,
  status:number
}

interface Props {
  parentDirectoryId: string
}

const props = withDefaults(defineProps<Props>(), {
  parentDirectoryId: '-1'
});

const route = useRoute();

let spaceId = '';

const state = reactive<{
  fileList: {
    [key:string]: SourceType
  },
  fileId:string
}>({
  fileList: {},
  fileId: '-1'
});

const emit = defineEmits<{(e:'close'):void, (e: 'success'):void}>();

const headers = {
  authorization: `Bearer ${cookie.get('access_token')}`
};

const uploadInput = ref();
const uploadAgain = () => {
  uploadInput.value.click();
};

const close = () => {
  emit('close');
};

const toUpload = async ({ file }) => {
  const options = {
    bizKey: 'angusTesterDataFiles',
    parentDirectoryId: +props.parentDirectoryId > -1 ? props.parentDirectoryId : undefined,
    spaceId: spaceId
  };
  const size = formatBytes(file.size);
  state.fileList[file.uid] = {
    name: file.name, size, progress: 0, status: 0
  };

  const config = {
    onUploadProgress: (progressEvent: { loaded: number, total: number }) => {
      const { loaded, total } = progressEvent;
      const progress = Math.round((loaded * 100) / total);
      state.fileList[file.uid].progress = progress;
    }
  };

  const [error] = await upload(file, options, config);
  if (error) {
    state.fileList[file.uid].status = 1;
    return;
  }
  state.fileList[file.uid].status = 2;
  emit('success');
};

const uploadFileLength = computed(() => {
  return Object.keys(state.fileList).length;
});

watch(() => route.params.id, (val) => {
  if (val) {
    spaceId = val as string;
  }
}, {
  immediate: true
});

</script>
<template>
  <Upload
    v-show="uploadFileLength == 0"
    :showUploadList="false"
    :multiple="true"
    :customRequest="toUpload"
    :headers="headers">
    <div
      ref="uploadInput"
      class="w-full h-30 border border-dashed border-border-selected leading bg-blue-2 mb-5 text-center pt-5.5 cursor-pointer">
      <Icon icon="icon-shangchuan" class="text-6 text-text-link mb-3 font-medium" />
      <p class="text-theme-title font-medium">点击此处上传文件</p>
    </div>
  </Upload>
  <div v-show="uploadFileLength !== 0" class="text-3 mb-5 relative">
    <div
      class="border border-dashed border-gray-9 px-5 pt-5 pb-1 max-h-40 overflow-scroll">
      <div
        v-for="(item,key) in state.fileList"
        :key="key"
        class="flex items-center mb-3">
        <div class="text-theme-title w-40 overflow-hidden whitespace-nowrap text-ellipsis">{{ item.name }}</div>
        <div class="text-theme-sub-content mx-20 w-15">{{ item.size }}</div>
        <div v-if="item.status ==2">
          <Icon icon="icon-duihao" class="text-3 text-status-success mr-1.5" />上传完成
        </div>
        <div v-if="item.status ==1">
          <Icon icon="icon-chahao" class="text-3 text-status-error mr-1.5" />上传失败
        </div>
        <Progress
          v-if="item.status==0"
          :stroke-width="6"
          class="w-30"
          :percent="item.progress"
          :showInfo="false" />
      </div>
    </div>
    <div class="absolute right-5 bottom-2 text-theme-special text-theme-text-hover:hove">
      <a @click="uploadAgain">继续上传</a>
      <a class="ml-3" @click="close">取消</a>
    </div>
  </div>
</template>
<style scoped>
:deep(.ant-upload) {
  @apply w-full;
}
</style>
