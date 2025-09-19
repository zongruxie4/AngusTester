<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Button, Upload, UploadFile } from 'ant-design-vue';
import { Icon, notification, Spin } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { upload, utils } from '@xcan-angus/infra';
import { task } from '@/api/tester';

import { TaskDetail } from '../../types';
import { TaskInfoProps } from '@/views/task/task/list/types';

const { t } = useI18n();

/**
 * <p>Attachment item interface</p>
 * <p>Defines the structure of a single attachment file</p>
 */
type AttachmentItem = {
  id: string;
  name: string;
  url: string;
}

// Component Props & Emits
const props = withDefaults(defineProps<TaskInfoProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskDetail>): void;
}>();

/**
 * <p>Maximum file size in MB for uploads</p>
 */
const MAX_FILE_SIZE_MB = 10;

// Reactive State Variables
const isUploading = ref(false);
const attachmentList = ref<AttachmentItem[]>([]);

/**
 * <p>Handle file upload change event</p>
 * <p>Validates file size, uploads file, and updates attachment list</p>
 */
const handleFileUploadChange = async ({ file }: { file: UploadFile }) => {
  if (file.size! > maxFileSizeInBytes.value) {
    notification.warning(t('backlog.info.attachment.messages.fileSizeExceeded', { maxSize: MAX_FILE_SIZE_MB }));
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
 * <p>Handle attachment deletion</p>
 * <p>Removes the specified attachment from the list and updates the task</p>
 */
const handleAttachmentDeletion = async (attachmentToDelete: AttachmentItem) => {
  const updatedAttachmentList = attachmentList.value.filter(item => item.id !== attachmentToDelete.id).map(item => {
    return {
      name: item.name,
      url: item.url
    };
  });

  await updateTaskAttachments(updatedAttachmentList);
};

/**
 * <p>Update task attachments via API</p>
 * <p>Sends the updated attachment list to the server and emits change event</p>
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
 * <p>Custom upload request handler</p>
 * <p>Prevents default upload behavior to handle uploads manually</p>
 */
const handleCustomUploadRequest = () => {
  return false;
};

/**
 * <p>Get current task ID from props</p>
 */
const currentTaskId = computed(() => props.dataSource?.id);

/**
 * <p>Check if attachment list is empty</p>
 * <p>Used to determine whether to show upload area or attachment list</p>
 */
const isAttachmentListEmpty = computed(() => !attachmentList.value.length);

/**
 * <p>Calculate maximum file size in bytes</p>
 * <p>Converts MB to bytes for file size validation</p>
 */
const maxFileSizeInBytes = computed(() => {
  return 1024 * 1024 * MAX_FILE_SIZE_MB;
});

/**
 * <p>Initialize component and watch for data source changes</p>
 * <p>Sets up reactive attachment list based on task data</p>
 */
onMounted(() => {
  watch(() => props.dataSource, (newTaskData) => {
    if (!newTaskData) {
      return;
    }

    attachmentList.value = (newTaskData.attachments || []).map(attachment => {
      return {
        id: utils.uuid(),
        name: attachment.name,
        url: attachment.url
      };
    });
  }, { immediate: true });
});
</script>

<template>
  <div class="basic-info-drawer">
    <div class="basic-info-header">
      <h3 class="basic-info-title">{{ t('backlog.info.attachment.title') }}</h3>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <Spin
          :spinning="isUploading"
          :class="{ empty: isAttachmentListEmpty }"
          class="upload-container w-full px-3 py-2.5 leading-5 text-3 rounded border border-dashed">
          <template v-if="!isAttachmentListEmpty">
            <div
              v-for="attachment in attachmentList"
              :key="attachment.id"
              class="leading-4 mb-2 last:mb-0 flex items-center justify-between overflow-hidden">
              <a
                class="flex-1 flex-nowrap truncate"
                :download="attachment.name"
                :href="attachment.url">{{ attachment.name }}</a>
              <Icon
                icon="icon-qingchu"
                class="text-3.5 flex-shrink-0 cursor-pointer text-theme-text-hover"
                @click="handleAttachmentDeletion(attachment)">
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
                  <div class="flex-shrink-0 text-text-link ml-1">{{ t('backlog.info.attachment.continueUpload') }}</div>
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
                <div class="flex-shrink-0 text-text-link">
                  {{ t('backlog.info.attachment.selectFile') }}
                </div>
              </Button>
            </Upload>
            <div class="text-theme-sub-content mt-1">
              {{ t('backlog.info.attachment.fileSizeLimit', { maxSize: MAX_FILE_SIZE_MB }) }}
            </div>
          </template>
        </Spin>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Main container styles */
.basic-info-drawer {
  width: 370px;
  height: 100%;
  background: #ffffff;
  font-size: 12px;
  line-height: 1.4;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* Header styles */
.basic-info-header {
  padding: 12px 20px 8px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.basic-info-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0;
  line-height: 1.2;
}

/* Scrollable content area */
.scrollable-content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* Content area styles */
.basic-info-content {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

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
