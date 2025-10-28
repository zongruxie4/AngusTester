<script setup lang="ts">
import { Progress, Upload } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { useUpload } from './composables/useUpload';

interface Props {
  parentDirectoryId: string
}

const props = withDefaults(defineProps<Props>(), {
  parentDirectoryId: '-1'
});

const emit = defineEmits<{(e:'close'):void, (e: 'success'):void}>();

const { t } = useI18n();

// Use the upload composable
const { state, headers, uploadInput, uploadFileLength, triggerUpload, handleUpload, init } = useUpload(props, emit);

// Initialize the composable
init();

/**
 * Trigger upload again
 */
const uploadAgain = (): void => {
  triggerUpload();
};

/**
 * Close the upload component
 */
const close = (): void => {
  emit('close');
};


// Expose closeUpload method to the composable
// This is a workaround since we can't directly pass the emit function to the composable
</script>
<template>
  <Upload
    v-show="uploadFileLength == 0"
    :showUploadList="false"
    :multiple="true"
    :customRequest="handleUpload"
    :headers="headers">
    <div
      ref="uploadInput"
      class="w-full h-30 border border-dashed border-border-selected leading bg-blue-2 mb-5 text-center pt-5.5 cursor-pointer">
      <Icon icon="icon-shangchuan" class="text-6 text-text-link mb-3 font-medium" />
      <p class="text-theme-title font-medium">{{ t('file.upload.clickToUpload') }}</p>
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
          <Icon icon="icon-duihao" class="text-3 text-status-success mr-1.5" />{{ t('file.upload.uploadComplete') }}
        </div>
        <div v-if="item.status ==1">
          <Icon icon="icon-chahao" class="text-3 text-status-error mr-1.5" />{{ t('file.upload.uploadFailed') }}
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
      <a @click="uploadAgain">{{ t('file.upload.continueUpload') }}</a>
      <a class="ml-3" @click="close">{{ t('actions.cancel') }}</a>
    </div>
  </div>
</template>
<style scoped>
:deep(.ant-upload) {
  @apply w-full;
}
</style>
