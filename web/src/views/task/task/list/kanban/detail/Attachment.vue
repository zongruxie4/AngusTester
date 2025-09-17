<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Button, Upload, UploadFile } from 'ant-design-vue';
import { Icon, notification, Spin } from '@xcan-angus/vue-ui';
import { upload, utils } from '@xcan-angus/infra';
import { task } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { TaskInfo } from '@/views/task/types';
import { TaskInfoProps } from '@/views/task/task/list/types';

// Type definitions
type AttachmentItem = {
  id: string;
  name: string;
  url: string;
}

// Component props and emits
const props = withDefaults(defineProps<TaskInfoProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
}>();

// Constants
const MAX_FILE_SIZE_MB = 10;

// Component state
const isUploading = ref(false);
const attachmentList = ref<AttachmentItem[]>([]);

/**
 * Handle file upload change event and upload file
 * @param param - Upload change event with file
 */
const handleFileUploadChange = async ({ file }: { file: UploadFile }) => {
  if (file.size! > maxFileSizeInBytes.value) {
    notification.warning(t('task.detailInfo.attachment.messages.fileSizeLimit', { size: MAX_FILE_SIZE_MB }));
    return;
  }

  isUploading.value = true;
  const [error, res] = await upload(file.originFileObj!, { bizKey: 'angusTesterTaskAttachments' });
  if (error) {
    isUploading.value = false;
    return;
  }

  const uploadedFileData = res?.data?.[0];
  if (!uploadedFileData) {
    isUploading.value = false;
    return;
  }

  const updatedAttachmentList = attachmentList.value.filter(item => item.id !== uploadedFileData.id).map(item => {
    return {
      name: item.name,
      url: item.url
    };
  });

  updatedAttachmentList.push({
    name: uploadedFileData.name,
    url: uploadedFileData.url
  });

  await updateTaskAttachments(updatedAttachmentList);
};

/**
 * Delete attachment from task
 * @param attachmentToDelete - Attachment item to delete
 */
const deleteAttachment = async (attachmentToDelete: AttachmentItem) => {
  const updatedAttachmentList = attachmentList.value.filter(item => item.id !== attachmentToDelete.id).map(item => {
    return {
      name: item.name,
      url: item.url
    };
  });

  await updateTaskAttachments(updatedAttachmentList);
};

/**
 * Update task attachments via API
 * @param attachmentData - Array of attachment data with name and url
 */
const updateTaskAttachments = async (attachmentData: {name: string; url: string}[]) => {
  const updateParams = {
    attachments: attachmentData
  };
  isUploading.value = true;
  const [error] = await task.editTaskAttachment(currentTaskId.value, updateParams);
  isUploading.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, attachments: updateParams.attachments });
};

/**
 * Custom upload request handler to prevent default upload behavior
 * @returns false to prevent default upload
 */
const handleCustomUploadRequest = () => {
  return false;
};

/**
 * Initialize component and watch for data source changes
 */
onMounted(() => {
  watch(() => props.dataSource, (newDataSource) => {
    if (!newDataSource) {
      return;
    }

    attachmentList.value = (newDataSource.attachments || []).map(item => {
      return {
        id: utils.uuid(),
        name: item.name,
        url: item.url
      };
    });
  }, { immediate: true });
});

// Computed properties
const currentTaskId = computed(() => props.dataSource?.id);
const hasNoAttachments = computed(() => !attachmentList.value.length);

const maxFileSizeInBytes = computed(() => {
  return 1024 * 1024 * MAX_FILE_SIZE_MB;
});
</script>

<template>
  <div class="h-full text-3 leading-5 pl-5 overflow-auto">
    <div class="text-theme-title mb-2.5 font-semibold">{{ t('task.detailInfo.attachment.title') }}</div>
    <Spin
      :spinning="isUploading"
      :class="{ empty: hasNoAttachments }"
      class="upload-container w-full px-3 py-2.5 leading-5 text-3 rounded border border-dashed">
      <template v-if="!hasNoAttachments">
        <div
          v-for="item in attachmentList"
          :key="item.id"
          class="leading-4 mb-2 last:mb-0 flex items-center justify-between overflow-hidden">
          <a
            class="flex-1 flex-nowrap truncate"
            :download="item.name"
            :href="item.url">{{ item.name }}</a>
          <Icon
            icon="icon-qingchu"
            class="text-3.5 flex-shrink-0 cursor-pointer text-theme-text-hover"
            @click="deleteAttachment(item)">
          </icon>
        </div>

        <div v-if="attachmentList.length < 5" class="upload-action flex justify-center h-5">
          <Upload
            :maxCount="5"
            :showUploadList="false"
            :customRequest="handleCustomUploadRequest"
            @change="handleFileUploadChange">
            <Button
              size="small"
              type="link"
              class="flex items-center h-auto leading-4.5 p-0">
              <Icon icon="icon-shangchuan" class="text-3.5 flex-shrink-0 text-text-link" />
              <div class="flex-shrink-0 text-text-link ml-1">
                {{ t('task.detailInfo.attachment.actions.continueUpload') }}
              </div>
            </Button>
          </Upload>
        </div>
      </template>

      <template v-else>
        <Upload
          :maxCount="5"
          :showUploadList="false"
          :customRequest="handleCustomUploadRequest"
          @change="handleFileUploadChange">
          <Button
            size="small"
            type="link"
            class="flex flex-col items-center justify-center h-auto leading-5 p-0">
            <Icon icon="icon-shangchuan" class="text-5 flex-shrink-0 text-text-link" />
            <div class="flex-shrink-0 text-text-link">{{ t('task.detailInfo.attachment.actions.selectFile') }}</div>
          </Button>
        </Upload>
        <div class="text-theme-sub-content mt-1">
          {{ t('task.detailInfo.attachment.messages.uploadLimit', { size: MAX_FILE_SIZE_MB }) }}
        </div>
      </template>
    </Spin>
  </div>
</template>

<style scoped>
.upload-container {
  border-color: var(--border-text-box);
  background-color: #fafafa;
}

.empty.upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 8px;
  padding-bottom: 8px;
}

.upload-action {
  visibility: visible;
}

.upload-container:hover .upload-action {
  visibility: visible;
}

:deep(.ant-upload.ant-upload-select) {
  width: 100%;
}
</style>
