<script lang="ts" setup>
import { ref } from 'vue';
import { Modal, Input, notification, Icon } from '@xcan-angus/vue-ui';
import { Form, FormItem, Upload } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { apis } from '@/api/tester';

interface Props {
  visible: boolean;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  projectId: ''
});

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'cancel'):void;
  (e: 'ok'):void;
  (e: 'update:visible', value: boolean):void
}>();

const { t } = useI18n();

const loading = ref(false);
const formRef = ref();
const fileList = ref<any[]>([]);
const formState = ref<{ name: string | undefined; content: string | undefined }>({
  name: undefined,
  content: undefined
});

/**
 * Read selected file content and set into form state.
 */
const handleFile = async (fileObj) => {
  const file = fileObj.file;
  const reader = new FileReader();

  reader.onload = (ev: ProgressEvent<FileReader>) => {
    const result = ev.target?.result as string | null;
    formState.value.content = result || '';
    fileList.value = [{
      name: file.name,
      status: 'done',
      uid: `${Date.now()}`
    }];
    formRef.value.validate(['content']);
  };
  reader.readAsText(file);
};

/**
 * Remove current selected file and clear content.
 */
const delFile = () => {
  fileList.value = [];
  formState.value.content = undefined;
};

/**
 * Close modal without importing.
 */
const cancel = () => {
  emits('update:visible', false);
};

/**
 * Validate fields then submit import request with file content.
 */
const ok = async () => {
  formRef.value.validate().then(async () => {
    loading.value = true;
    const formData = new FormData();
    formData.append('projectId', props.projectId);
    formData.append('name', formState.value.name || '');
    formData.append('content', formState.value.content || '');
    const [error] = await apis.importDesign(formData);
    loading.value = false;
    if (error) {
      return;
    }
    notification.success(t('actions.tips.importSuccess'));
    cancel();
    emits('ok');
  });
};
</script>
<template>
  <Modal
    :title="t('actions.import')"
    :visible="props.visible"
    :width="500"
    :okButtonProps="{
      loading
    }"
    @cancel="cancel"
    @ok="ok">
    <Form
      ref="formRef"
      :model="formState">
      <FormItem
        name="name"
        :label="t('common.name')"
        required>
        <Input
          v-model:value="formState.name"
          :maxlength="100"
          :placeholder="t('common.placeholders.searchKeyword')" />
      </FormItem>

      <FormItem
        name="content"
        :label="t('common.file')"
        :rules="[{required: true, message: t('apiDesign.importDesignModal.fileRule')}]">
        <Upload
          v-model:fileList="fileList"
          class="w-full"
          accept=".json,.yaml"
          :customRequest="handleFile"
          @remove="delFile">
          <div
            v-show="!formState.content"
            class="border border-dashed rounded p-4 flex flex-col items-center justify-around space-y-2 text-3 border-blue-1">
            <Icon icon="icon-daoru" class="text-blue-hover" />
            <span class="text-blue-hover">{{ t('apiDesign.importDesignModal.uploadFile') }}</span>
            <div class="text-text-sub-content">
              {{ t('apiDesign.importDesignModal.uploadTip') }}
            </div>
          </div>
        </Upload>
      </FormItem>
    </Form>
  </Modal>
</template>
<style scoped>
:deep(.ant-upload.ant-upload-select) {
  @apply block;
}
</style>
