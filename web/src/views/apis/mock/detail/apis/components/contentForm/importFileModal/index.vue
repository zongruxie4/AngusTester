<script setup lang="ts">
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Radio, RadioGroup } from 'ant-design-vue';
import { Modal, SingleUpload } from '@xcan-angus/vue-ui';
import { codeUtils } from '@xcan-angus/infra';

import { ContentEncoding } from '../PropsType';

const { t } = useI18n();

interface Props {
  visible:boolean;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok', value:string, file:File, coding:ContentEncoding): void;
  (e: 'cancel'): void;
}>();

const uploadFile = ref<File>();
const contentEncoding = ref<ContentEncoding>('gzip_base64');
const loading = ref(false);

const change = (value:File) => {
  uploadFile.value = value;
};

const ok = async () => {
  const file = uploadFile.value;
  if (!file) {
    return;
  }

  loading.value = true;
  let base64Content:string;
  const coding = contentEncoding.value;
  if (contentEncoding.value === 'gzip_base64') {
    base64Content = await codeUtils.gzip(file);
    emit('ok', base64Content, file, coding);
    close();
    return;
  }

  const reader = new FileReader();
  reader.onload = (event: ProgressEvent<FileReader>) => {
    base64Content = event?.target?.result || '';
    emit('ok', base64Content, file, coding);
    close();
  };
  reader.readAsDataURL(file);
};

const cancel = () => {
  emit('cancel');
  close();
};

const close = () => {
  loading.value = false;
  contentEncoding.value = 'gzip_base64';
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
    :title="t('mock.mockApisComp.contentForm.importFileModal.title')"
    :visible="props.visible"
    :okButtonProps="okButtonProps"
    :bodyStyle="bodyStyle"
    :confirmLoading="loading"
    @ok="ok"
    @cancel="cancel">
    <SingleUpload
      v-if="props.visible"
      :allorPaste="false"
      :maxSize="10485760"
      class="mb-5"
      accept="*"
      :tipText="t('mock.mockApisComp.contentForm.importFileModal.maxFileSize')"
      @change="change" />

    <div class="flex items-center space-x-6">
      <div>{{ t('mock.mockApisComp.contentForm.importFileModal.encoding') }}</div>
      <RadioGroup v-model:value="contentEncoding">
        <Radio value="gzip_base64">gzip_base64</Radio>
        <Radio value="base64">base64</Radio>
      </RadioGroup>
    </div>
  </Modal>
</template>
