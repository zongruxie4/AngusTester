<script setup lang="ts">
import { computed, ref } from 'vue';
import { Toggle, Icon } from '@xcan-angus/vue-ui';
import { Upload } from 'ant-design-vue';
import { download, upload } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { testCase } from '@/api/tester';
import { CaseDetail } from '@/views/test/types';

interface Props {
  id?: number;
  dataSource?: CaseDetail;
  projectId?: string;
  taskId?: number;
  actionAuth?: {[key: string]: any};
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  dataSource: undefined,
  projectId: undefined,
  taskId: undefined,
  actionAuth: () => ({})
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'change', value: Partial<CaseDetail>): void;
  (event: 'loadingChange', value: boolean): void;
}>();

const { t } = useI18n();

const isUploadControlsVisible = ref(false);
const isUploading = ref<boolean>(false);
const uploadedFileList = ref<{id?:string, name:string, url:string}[]>([]);

/**
 * <p>Gets the current attachments data from the data source.</p>
 * @returns Array of attachment objects
 */
const attachmentsData = computed(() => {
  return props.dataSource?.attachments || [];
});

/**
 * <p>Download a file by URL.</p>
 * <p>Triggers file download for the specified URL.</p>
 * @param url - File URL to download
 */
const handleDownload = (url:string) => {
  download(url);
};

/**
 * <p>Show upload controls on mouse enter.</p>
 * <p>Makes upload controls visible when hovering over upload area.</p>
 */
const handleUploadAreaMouseEnter = () => {
  isUploadControlsVisible.value = true;
};

/**
 * <p>Hide upload controls on mouse leave.</p>
 * <p>Hides upload controls when mouse leaves upload area.</p>
 */
const handleUploadAreaMouseLeave = () => {
  isUploadControlsVisible.value = false;
};

/**
 * <p>Remove attachment by index.</p>
 * <p>Removes the specified attachment from the case.</p>
 * @param attachmentIndex - Attachment index to remove
 */
const handleRemoveAttachment = async (attachmentIndex:number) => {
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
const handleFileUpload = async function (uploadFile) {
  if (uploadedFileList.value.length >= 5 || isUploading.value) {
    return;
  }

  isUploading.value = true;
  const [error, { data = [] }] = await upload(uploadFile.file, { bizKey: 'angusTesterCaseAttachments' });
  isUploading.value = false;
  if (error) {
    return;
  }
  await updateAttachmentList(data);
};

/**
 * <p>Persist new attachments list after upload.</p>
 * <p>Updates the case with the new attachment list.</p>
 * @param uploadedData - Array of uploaded file data
 */
const updateAttachmentList = async (uploadedData) => {
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
  <Toggle
    :title="t('common.attachment')"
    class="mt-3.5">
    <div
      style="height: 108px; border-color: rgba(0, 119, 255);background-color: rgba(0, 119, 255, 4%);"
      class="border border-dashed rounded flex flex-col px-2 py-1 mx-2 mt-3 mb-3"
      :class="attachmentsData.length?'justify-between':'justify-center'"
      @mouseenter="handleUploadAreaMouseEnter"
      @mouseleave="handleUploadAreaMouseLeave">
      <template v-if="attachmentsData.length">
        <div
          style="height: 90px;scrollbar-gutter: stable;"
          class="overflow-hidden hover:overflow-y-auto -mr-2 pr-1">
          <div
            v-for="(item,index) in dataSource?.attachments"
            :key="index"
            :class="{'rounded-b':index===dataSource?.attachments?.length-1}"
            class="flex items-center justify-between text-3 leading-3">
            <div
              :title="item.name"
              class="truncate text-theme-sub-content leading-4 cursor-pointer"
              style="width: 250px;"
              @click="handleDownload(item.url)">
              {{ item.name }}
            </div>
            <Icon
              icon="icon-qingchu"
              class="text-theme-special text-theme-text-hover cursor-pointer flex-shrink-0 leading-4 text-3.5"
              @click="handleRemoveAttachment(index)" />
          </div>
        </div>
      </template>

      <div class="flex justify-around h-6">
        <template v-if="props.actionAuth['edit'] && (dataSource?.attachments?.length || 0) < 5">
          <Upload
            :fileList="[]"
            name="file"
            :customRequest="handleFileUpload">
            <Icon icon="icon-shangchuan" class="text-theme-special mr-1" />
            <span class="text-3 text-theme-text-hover">{{ t('actions.upload') }}</span>
          </Upload>
        </template>
      </div>
    </div>
  </Toggle>
</template>
<style scoped>
:deep(.toggle-title) {
  @apply text-3.5;
}
</style>
