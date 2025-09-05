<script setup lang="ts">
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Radio, RadioGroup } from 'ant-design-vue';
import { Hints, Modal, notification, SingleUpload } from '@xcan-angus/vue-ui';
import { mock } from '@/api/tester';

interface Props {
  id:string;// mock service Id
  visible:boolean;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  visible: false
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok'): void;
  (e: 'cancel'): void;
}>();

const uploadFile = ref<File|string>();
const duplicatedValue = ref<'COVER'|'IGNORE'>('COVER');
const notExistedValue = ref<'true'|'false'>('false');

const loading = ref(false);

const change = (value:File|string|undefined) => {
  uploadFile.value = value;
};

const ok = async () => {
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

  notification.success(t('mock.detail.apis.components.importApiModal.success'));
  emit('ok');
  close();
};

const cancel = () => {
  emit('cancel');
  close();
};

const close = () => {
  uploadFile.value = undefined;
  emit('update:visible', false);
};

const okButtonProps = computed(() => {
  return {
    disabled: !uploadFile.value
  };
});

const bodyStyle = {
  lineHeight: '20px'
};
</script>
<template>
  <Modal
    :title="t('mock.detail.apis.components.importApiModal.title')"
    :visible="props.visible"
    :okButtonProps="okButtonProps"
    :bodyStyle="bodyStyle"
    :confirmLoading="loading"
    @ok="ok"
    @cancel="cancel">
    <Hints :text="t('mock.detail.apis.components.importApiModal.hints')" />
    <SingleUpload
      v-if="props.visible"
      allorPaste
      class="mb-5"
      accept=".zip,.rar,.7z,.gz,.tar,.bz2,.xz,.lzma,.json,.yaml,.yml"
      @change="change" />

    <div class="space-y-0.5 mb-5">
      <div>{{ t('mock.detail.apis.components.importApiModal.duplicateStrategy') }}</div>
      <RadioGroup v-model:value="duplicatedValue">
        <Radio value="COVER">{{ t('mock.detail.apis.components.importApiModal.cover') }}</Radio>
        <Radio value="IGNORE">{{ t('mock.detail.apis.components.importApiModal.ignore') }}</Radio>
      </RadioGroup>
    </div>

    <div class="space-y-0.5">
      <div>{{ t('mock.detail.apis.components.importApiModal.deleteNotExisted') }}</div>
      <RadioGroup v-model:value="notExistedValue">
        <Radio value="true">{{ t('mock.detail.apis.components.importApiModal.yes') }}</Radio>
        <Radio value="false">{{ t('mock.detail.apis.components.importApiModal.no') }}</Radio>
      </RadioGroup>
    </div>
  </Modal>
</template>
