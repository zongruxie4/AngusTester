<script setup lang="ts">
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Radio, RadioGroup } from 'ant-design-vue';
import { StrategyWhenDuplicated } from '@xcan-angus/infra';
import { Hints, Modal, notification, SingleUpload } from '@xcan-angus/vue-ui';
import { mock } from '@/api/tester';

// Props & Emits
interface Props {
  id:string;// mock service Id
  visible:boolean;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  visible: false
});

const { t } = useI18n();


const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok'): void;
  (e: 'cancel'): void;
}>();

// ==================== Reactive State ====================
const uploadFile = ref<File|string>();
const duplicatedValue = ref<StrategyWhenDuplicated>(StrategyWhenDuplicated.COVER);
const notExistedValue = ref<'true'|'false'>('false');
const loading = ref(false);

// ==================== Computed Properties ====================
/**
 * OK button properties based on upload file state
 */
const okButtonProps = computed(() => {
  return {
    disabled: !uploadFile.value
  };
});

/**
 * Modal body style configuration
 */
const bodyStyle = {
  lineHeight: '20px'
};

// ==================== Methods ====================
/**
 * Handle file upload change event
 * @param value - Uploaded file or string content
 */
const handleFileChange = (value:File|string|undefined) => {
  uploadFile.value = value;
};

/**
 * Handle import confirmation
 */
const handleImportConfirm = async () => {
  if (loading.value || !uploadFile.value) {
    return;
  }

  const formData = new FormData();

  formData.append('mockServiceId', props.id);
  formData.append('strategyWhenDuplicated', duplicatedValue.value);
  formData.append('deleteWhenNotExisted', notExistedValue.value);

  if (uploadFile.value instanceof File) {
    formData.append('file', uploadFile.value);
  } else {
    formData.append('content', uploadFile.value);
  }

  loading.value = true;
  const [error] = await mock.importService(formData);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('status.success'));
  emit('ok');
  handleModalClose();
};

/**
 * Handle import cancellation
 */
const handleImportCancel = () => {
  emit('cancel');
  handleModalClose();
};

/**
 * Close the modal and reset state
 */
const handleModalClose = () => {
  uploadFile.value = undefined;
  emit('update:visible', false);
};
</script>
<template>
  <Modal
    :title="t('mock.detail.apis.components.importApiModal.title')"
    :visible="props.visible"
    :okButtonProps="okButtonProps"
    :bodyStyle="bodyStyle"
    :confirmLoading="loading"
    @ok="handleImportConfirm"
    @cancel="handleImportCancel">
    <Hints :text="t('mock.detail.apis.components.importApiModal.hints')" />
    <SingleUpload
      v-if="props.visible"
      allorPaste
      class="mb-5"
      accept=".zip,.rar,.7z,.gz,.tar,.bz2,.xz,.lzma,.json,.yaml,.yml"
      @change="handleFileChange" />

    <div class="space-y-0.5 mb-5">
      <div>{{ t('mock.detail.apis.components.importApiModal.duplicateStrategy') }}</div>
      <RadioGroup v-model:value="duplicatedValue">
        <Radio value="COVER">{{ t('actions.cover') }}</Radio>
        <Radio value="IGNORE">{{ t('actions.ignore') }}</Radio>
      </RadioGroup>
    </div>

    <div class="space-y-0.5">
      <div>{{ t('mock.detail.apis.components.importApiModal.deleteNotExisted') }}</div>
      <RadioGroup v-model:value="notExistedValue">
        <Radio value="true">{{ t('status.yes') }}</Radio>
        <Radio value="false">{{ t('status.no') }}</Radio>
      </RadioGroup>
    </div>
  </Modal>
</template>
