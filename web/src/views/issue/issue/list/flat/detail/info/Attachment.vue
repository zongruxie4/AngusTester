<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Button, Upload, UploadFile } from 'ant-design-vue';
import { Icon, notification, Spin, Toggle } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { upload, utils } from '@xcan-angus/infra';
import { issue } from '@/api/tester';
import { TaskDetailProps } from '@/views/issue/issue/list/types';

import { TaskDetail } from '@/views/issue/types';
import { AttachmentInfo } from '@/types/types';

// Component props and emits
const props = withDefaults(defineProps<TaskDetailProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskDetail>): void;
}>();

// Constants
const MAX_FILE_SIZE_MB = 10;

// Component state
const isUploading = ref(false);
const attachmentList = ref<AttachmentInfo[]>([]);

/**
 * <p>Handles file upload change event to process new file uploads.</p>
 * <p>Validates file size, uploads the file, and updates the attachment list.</p>
 * @param param - Upload change event containing the file
 */
const handleFileUpload = async ({ file }: { file: UploadFile }) => {
  if (file.size! > maxFileSizeBytes.value) {
    notification.warning(t('backlog.edit.messages.fileSizeLimit', { size: MAX_FILE_SIZE_MB }));
    return;
  }

  isUploading.value = true;
  const [error, res] = await upload(file.originFileObj!, { bizKey: 'angusTesterTaskAttachments' });
  if (error) {
    isUploading.value = false;
    return;
  }

  const uploadedFile = res?.data?.[0];
  if (!uploadedFile) {
    isUploading.value = false;
    return;
  }

  const updatedAttachmentList = attachmentList.value.filter(item => item.id !== uploadedFile.id).map(item => {
    return {
      name: item.name,
      url: item.url
    };
  });

  updatedAttachmentList.push({
    name: uploadedFile.name,
    url: uploadedFile.url
  });

  await updateTaskAttachments(updatedAttachmentList);
};

/**
 * <p>Handles attachment deletion by removing the specified attachment from the list.</p>
 * @param attachmentToDelete - The attachment item to be deleted
 */
const handleAttachmentDelete = async (attachmentToDelete: AttachmentInfo) => {
  const updatedAttachmentList = attachmentList.value.filter(item => item.id !== attachmentToDelete.id).map(item => {
    return {
      name: item.name,
      url: item.url
    };
  });

  await updateTaskAttachments(updatedAttachmentList);
};

/**
 * <p>Updates task attachments by calling the API and emitting change event.</p>
 * @param attachmentData - Array of attachment data containing name and url
 */
const updateTaskAttachments = async (attachmentData: {name: string; url: string}[]) => {
  const params = {
    attachments: attachmentData
  };
  isUploading.value = true;
  const [error] = await issue.editTaskAttachment(currentTaskId.value, params);
  isUploading.value = false;
  if (error) {
    return;
  }

  emit('change', { id: currentTaskId.value, attachments: params.attachments });
};

/**
 * <p>Custom upload request handler that prevents default upload behavior.</p>
 * <p>Returns false to disable automatic upload, allowing custom handling.</p>
 */
const handleCustomUploadRequest = () => {
  return false;
};

// Computed properties
const currentTaskId = computed(() => props.dataSource?.id);
const isAttachmentListEmpty = computed(() => !attachmentList.value.length);

/**
 * <p>Computes the maximum file size in bytes based on the configured MB limit.</p>
 */
const maxFileSizeBytes = computed(() => {
  return 1024 * 1024 * MAX_FILE_SIZE_MB;
});

/**
 * <p>Initializes component and watches for data source changes.</p>
 * <p>Updates attachment list when task data changes.</p>
 */
onMounted(() => {
  watch(() => props.dataSource, (newTaskData) => {
    if (!newTaskData) {
      return;
    }

    attachmentList.value = (newTaskData.attachments || []).map(item => {
      return {
        id: utils.uuid(),
        name: item.name,
        url: item.url
      };
    });
  }, { immediate: true });
});
</script>
<template>
  <Toggle>
    <template #title>
      <div class="text-3.5">{{ t('common.attachment') }}</div>
    </template>

    <template #default>
      <Spin
        :spinning="isUploading"
        :class="{ empty: isAttachmentListEmpty }"
        class="upload-container w-full ml-5.5 px-3 py-2.5 leading-5 mt-2.5 text-3 rounded border border-dashed">
        <template v-if="!isAttachmentListEmpty">
          <div
            v-for="item in attachmentList"
            :key="item.id"
            class="leading-4 mb-1.5 last:mb-0 flex items-center justify-between overflow-hidden">
            <a
              class="flex-1 flex-nowrap truncate"
              :download="item.name"
              :href="item.url">{{ item.name }}</a>
            <Icon
              icon="icon-qingchu"
              class="text-3.5 flex-shrink-0 cursor-pointer text-theme-text-hover"
              @click="handleAttachmentDelete(item)">
            </icon>
          </div>

          <div v-if="attachmentList.length < 5" class="upload-action flex justify-center h-5">
            <Upload
              :maxCount="5"
              :showUploadList="false"
              :customRequest="handleCustomUploadRequest"
              @change="handleFileUpload">
              <Button
                size="small"
                type="link"
                class="flex items-center h-auto leading-4.5 p-0">
                <Icon icon="icon-shangchuan" class="text-3.5 flex-shrink-0 text-text-link" />
                <div class="flex-shrink-0 text-text-link ml-1">
                  {{ t('backlog.edit.actions.continueUpload') }}
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
            @change="handleFileUpload">
            <Button
              size="small"
              type="link"
              class="flex flex-col items-center justify-center h-auto leading-5 p-0">
              <Icon icon="icon-shangchuan" class="text-5 flex-shrink-0 text-text-link" />
              <div class="flex-shrink-0 text-text-link">
                {{ t('backlog.edit.actions.selectFile') }}
              </div>
            </Button>
          </Upload>
          <div class="text-theme-sub-content mt-1 ml-3 mr-3">
            {{ t('backlog.edit.messages.fileSizeLimit', { size: MAX_FILE_SIZE_MB }) }}
          </div>
        </template>
      </Spin>
    </template>
  </Toggle>
</template>

<style scoped>
.upload-container {
  width: calc(100% - 22px);
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
