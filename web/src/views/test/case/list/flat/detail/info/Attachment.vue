<script setup lang="ts">
import { computed, ref } from 'vue';
import { Toggle, Icon, Spin } from '@xcan-angus/vue-ui';
import { Button, Upload } from 'ant-design-vue';
import { download, upload } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { testCase } from '@/api/tester';
import { CaseDetail } from '@/views/test/types';
import { CaseActionAuth } from '@/views/test/case/types';
import { MAX_FILE_SIZE_MB, UPLOAD_TEST_FILE_KEY } from '@/utils/constant';

interface Props {
  id?: number;
  dataSource?: CaseDetail;
  projectId?: string;
  actionAuth?: CaseActionAuth[];
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  dataSource: undefined,
  projectId: undefined,
  actionAuth: () => ([])
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'change', value: Partial<CaseDetail>): void;
  (event: 'loadingChange', value: boolean): void;
}>();

const { t } = useI18n();

// Component state
const isUploading = ref<boolean>(false);

/**
 * <p>Gets the current attachments data from the data source.</p>
 * @returns Array of attachment objects
 */
const attachmentsData = computed(() => {
  return props.dataSource?.attachments || [];
});

/**
 * <p>Checks if attachment list is empty.</p>
 * @returns True if no attachments exist
 */
const isAttachmentListEmpty = computed(() => !attachmentsData.value.length);

/**
 * <p>Download a file by URL.</p>
 * <p>Triggers file download for the specified URL.</p>
 * @param url - File URL to download
 */
const handleDownload = (url: string) => {
  download(url);
};

/**
 * <p>Remove attachment by index.</p>
 * <p>Removes the specified attachment from the case.</p>
 * @param attachmentIndex - Attachment index to remove
 */
const handleRemoveAttachment = async (attachmentIndex: number) => {
  if (!props.dataSource || !attachmentsData.value.length) {
    return;
  }
  const remainingAttachments = attachmentsData.value?.filter((_item, index) => index !== attachmentIndex);
  isUploading.value = true;
  const [error] = await testCase.putAttachment(props.dataSource.id, { attachments: remainingAttachments });
  isUploading.value = false;
  if (error) {
    return;
  }
  emit('change', { attachments: remainingAttachments });
};

/**
 * <p>Upload attachment via custom request.</p>
 * <p>Handles file upload and adds to attachment list.</p>
 * @param uploadFile - File upload object
 */
const handleFileUpload = async ({ file }: { file: any }) => {
  if (file.size > 1024 * 1024 * MAX_FILE_SIZE_MB) {
    return;
  }

  if (attachmentsData.value.length >= 5 || isUploading.value) {
    return;
  }

  isUploading.value = true;
  const [error, { data = [] }] = await upload(file, { bizKey: UPLOAD_TEST_FILE_KEY });
  isUploading.value = false;
  if (error) {
    return;
  }
  await updateAttachmentList(data);
};

/**
 * <p>Custom upload request handler that prevents default upload behavior.</p>
 * <p>Returns false to disable automatic upload, allowing custom handling.</p>
 */
const handleCustomUploadRequest = () => {
  return false;
};

/**
 * <p>Persist new attachments list after upload.</p>
 * <p>Updates the case with the new attachment list.</p>
 * @param uploadedData - Array of uploaded file data
 */
const updateAttachmentList = async (uploadedData: any[]) => {
  if (!props.dataSource || isUploading.value) {
    return;
  }
  const newAttachments = uploadedData?.map(fileItem => ({ name: fileItem.name, url: fileItem.url }));
  if (attachmentsData.value.length) {
    newAttachments.push(...attachmentsData.value);
  }
  isUploading.value = true;
  const [error] = await testCase.putAttachment(props.dataSource.id, { attachments: newAttachments });
  isUploading.value = false;

  if (error) {
    return;
  }
  emit('change', { attachments: newAttachments });
};
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
            v-for="(item, index) in attachmentsData"
            :key="index"
            class="leading-4 mb-1.5 last:mb-0 flex items-center justify-between overflow-hidden">
            <a
              class="flex-1 flex-nowrap truncate"
              :download="item.name"
              :href="item.url"
              @click="handleDownload(item.url)">{{ item.name }}</a>
            <Icon
              icon="icon-qingchu"
              class="text-3.5 flex-shrink-0 cursor-pointer text-theme-text-hover"
              @click="handleRemoveAttachment(index)">
            </Icon>
          </div>

          <div
            v-if="props.actionAuth.includes('edit') && attachmentsData.length < 5"
            class="upload-action flex justify-center h-5">
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
                  {{ t('actions.upload') }}
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
                {{ t('actions.upload') }}
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
