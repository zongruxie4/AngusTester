<script setup lang="ts">
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Radio, RadioGroup } from 'ant-design-vue';
import { Modal, SingleUpload } from '@xcan-angus/vue-ui';
import { ContentEncoding, codeUtils } from '@xcan-angus/infra';

/**
 * <p>Props interface for ImportFileModal component</p>
 * <p>Defines the structure of props passed to the component</p>
 */
interface Props {
  visible: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
});

const { t } = useI18n();

/**
 * <p>Component events interface</p>
 * <p>Defines the events that this component can emit</p>
 */
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok', value: string, file: File, coding: ContentEncoding): void;
  (e: 'cancel'): void;
}>();

/**
 * <p>Currently selected file for upload</p>
 * <p>Stores the file object selected by the user</p>
 */
const uploadFile = ref<File>();

/**
 * <p>Content encoding method for file processing</p>
 * <p>Determines how the file content will be encoded</p>
 */
const contentEncoding = ref<ContentEncoding>(ContentEncoding.gzip_base64);

/**
 * <p>Loading state for file processing</p>
 * <p>Indicates if file processing is in progress</p>
 */
const loading = ref(false);

/**
 * <p>Handles file selection change event</p>
 * <p>Updates the selected file when user chooses a file</p>
 *
 * @param value - The selected file object
 */
const handleFileChange = (value: File) => {
  uploadFile.value = value;
};

/**
 * <p>Handles modal confirmation event</p>
 * <p>Processes the selected file and emits the result</p>
 */
const handleOk = async () => {
  const file = uploadFile.value;
  if (!file) {
    return;
  }

  loading.value = true;
  let base64Content: string;
  const coding = contentEncoding.value;

  if (contentEncoding.value === ContentEncoding.gzip_base64) {
    base64Content = await codeUtils.gzip(file);
    emit('ok', base64Content, file, coding);
    closeModal();
    return;
  }

  const reader = new FileReader();
  reader.onload = (event: ProgressEvent<FileReader>) => {
    base64Content = event?.target?.result || '';
    emit('ok', base64Content, file, coding);
    closeModal();
  };
  reader.readAsDataURL(file);
};

/**
 * <p>Handles modal cancellation event</p>
 * <p>Emits cancel event and closes the modal</p>
 */
const handleCancel = () => {
  emit('cancel');
  closeModal();
};

/**
 * <p>Closes the modal and resets form state</p>
 * <p>Clears all form data and emits visibility update</p>
 */
const closeModal = () => {
  loading.value = false;
  contentEncoding.value = ContentEncoding.gzip_base64;
  uploadFile.value = undefined;
  emit('update:visible', false);
};

/**
 * <p>OK button properties configuration</p>
 * <p>Disables button when no file is selected</p>
 */
const okButtonProps = computed(() => {
  return {
    disabled: !uploadFile.value
  };
});

/**
 * <p>Modal body style configuration</p>
 * <p>Sets consistent line height for modal content</p>
 */
const bodyStyle = {
  lineHeight: '20px'
};
</script>
<template>
  <Modal
    :title="t('mock.detail.apis.components.importFileModal.title')"
    :visible="props.visible"
    :okButtonProps="okButtonProps"
    :bodyStyle="bodyStyle"
    :confirmLoading="loading"
    @ok="handleOk"
    @cancel="handleCancel">
    <SingleUpload
      v-if="props.visible"
      :allorPaste="false"
      :maxSize="10485760"
      class="mb-5"
      accept="*"
      :tipText="t('mock.detail.apis.components.importFileModal.maxFileSize')"
      @change="handleFileChange" />

    <div class="flex items-center space-x-6">
      <div>{{ t('mock.detail.apis.components.importFileModal.encoding') }}</div>
      <RadioGroup v-model:value="contentEncoding">
        <Radio value="gzip_base64">gzip_base64</Radio>
        <Radio value="base64">base64</Radio>
      </RadioGroup>
    </div>
  </Modal>
</template>
