<script setup lang="ts">
import { computed, ref } from 'vue';
import { Button, Form, FormItem, Upload } from 'ant-design-vue';
import { Icon, Input, Modal, notification } from '@xcan-angus/vue-ui';
import { script } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { formatBytes } from '@/utils/common';

interface Props {
  projectId: string;
  visible: boolean;
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  visible: true
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok'): void;
}>();

const formRef = ref();
const emptyFlag = ref(false);
const maximumLimitFlag = ref(false);

const loading = ref(false);

const formState = ref<{
  name: string;
  content?: string;
  file?: File | undefined;
  description?: string;
}>({
  name: '',
  content: undefined,
  file: undefined,
  description: ''
});

const cancelHandler = () => {
  emptyFlag.value = false;
  maximumLimitFlag.value = false;
  formState.value = {
    name: '',
    content: undefined,
    file: undefined,
    description: ''
  };
  formRef.value.clearValidate();
  emit('update:visible', false);
};

const dropUploadHandler = (event) => {
  event.preventDefault();
  const file = event.dataTransfer.files[0];
  customRequest({ file });
};

const customRequest = ({ file }) => {
  if (file.size > 20 * 1024 * 1024) {
    maximumLimitFlag.value = true;
    emptyFlag.value = false;

    return;
  }

  formState.value.file = file;
  formState.value.content = undefined;
  maximumLimitFlag.value = false;
  emptyFlag.value = false;
};

const dragoverHandler = (event) => {
  event.preventDefault();
};

const pasteHandler = (event) => {
  const _text = event.clipboardData.getData('text');
  const encoder = new TextEncoder();
  const encodedString = encoder.encode(_text);
  const byteSize = encodedString.byteLength;
  const maxSizeInMB = 20;
  const maxSizeInBytes = maxSizeInMB * 1024 * 1024;

  if (byteSize > maxSizeInBytes) {
    maximumLimitFlag.value = true;
    emptyFlag.value = false;
  } else {
    formState.value.content = _text;
    formState.value.file = undefined;
    emptyFlag.value = false;
    maximumLimitFlag.value = false;
  }
};

const clearContent = () => {
  formState.value.content = '';
  emptyFlag.value = true;
};

const deleteFile = () => {
  formState.value.file = undefined;
  emptyFlag.value = true;
};

const handleOk = async () => {
  formRef.value.validate().then(async () => {
    checkFile();
    if (emptyFlag.value || maximumLimitFlag.value) {
      return;
    }

    const formData = new FormData();
    formData.append('name', formState.value.name);
    formData.append('projectId', props.projectId);

    if (formState.value.file) {
      formData.append('file', formState.value.file);
    }

    if (formState.value.content) {
      formData.append('content', formState.value.content.toString());
    }

    if (formState.value.description) {
      formData.append('description', formState.value.description);
    }

    loading.value = true;
    const [error] = await script.importScript(formData);
    loading.value = false;
    if (error) {
      return;
    }

    notification.success(t('tips.importSuccess'));
    cancelHandler();
    emit('ok');
  }, () => {
    checkFile();
  });
};

const checkFile = () => {
  emptyFlag.value = !formState.value.file && !formState.value.content;
};

const fileEmptyFlag = computed(() => {
  return !formState.value.file && !formState.value.content;
});

const errorFlag = computed(() => {
  return emptyFlag.value || maximumLimitFlag.value;
});

const rules = {
  name: [
    { required: true, message: t('scriptHome.import.messages.nameRequired'), trigger: 'change' }
  ]
};
</script>
<template>
  <Modal
    :visible="props.visible"
    :confirmLoading="loading"
    :width="800"
    :title="t('scriptHome.import.title')"
    @cancel="cancelHandler"
    @ok="handleOk">
    <Form
      ref="formRef"
      class="mr-5"
      :labelCol="{ style: { width: '60px' } }"
      :model="formState"
      :rules="rules">
      <FormItem name="name" :label="t('scriptHome.import.form.name')">
        <Input
          v-model:value="formState.name"
          :maxlength="200"
          trim
          :placeholder="t('scriptHome.import.form.namePlaceholder')" />
      </FormItem>

      <FormItem :label="t('scriptHome.import.form.description')" name="description">
        <Input
          v-model:value="formState.description"
          :autosize="{ minRows: 4, maxRows: 6 }"
          :maxlength="800"
          trim
          :placeholder="t('scriptHome.import.form.descriptionPlaceholder')"
          type="textarea" />
      </FormItem>

      <FormItem :label="t('scriptHome.import.form.file')" required>
        <div
          class="text-3 leading-5 border rounded border-dashed border-border-divider upload-container"
          :error="errorFlag">
          <div
            v-if="fileEmptyFlag"
            class="flex flex-col items-center justify-center p-3.5"
            @paste="pasteHandler"
            @drop.prevent="dropUploadHandler"
            @dragover="dragoverHandler">
            <Upload
              :multiple="false"
              :showUploadList="false"
              :customRequest="customRequest"
              accept=".zip,.rar,.7z,.gz,.tar,.bz2,.xz,.lzma,.json,.yaml,.yml">
              <div class="flex flex-col items-center justify-center">
                <Icon icon="icon-shangchuan" class="text-5 leading-5 text-text-link" />
                <div class="text-3 text-text-link text-center">{{ t('scriptHome.import.form.uploadFile') }}</div>
              </div>
            </Upload>

              <div class="text-text-sub-content mt-1">{{ t('scriptHome.import.form.uploadTip') }}</div>
          </div>

          <div v-if="formState.content" class="relative">
            <div class="flex-1 overflow-y-auto px-3.5 py-3 break-all whitespace-pre max-h-100 ">
              {{ formState.content }}
            </div>
            <Button
              class="absolute right-2.5 top-3 px-0 h-5 leading-5 !bg-white"
              type="link"
              size="small"
              @click="clearContent">
              {{ t('scriptHome.import.form.clear') }}
            </Button>
          </div>

          <div v-if="formState.file" class="flex items-center space-x-3.5 px-3.5 py-3 justify-between">
            <div :title="formState.file.name" class="truncate">{{ formState.file.name }}</div>
            <div class="flex items-center space-x-3.5">
              <div class="flex-shrink-0 whitespace-nowrap">{{ formatBytes(Number(formState.file.size)) }}</div>
              <Icon
                icon="icon-qingchu"
                class="flex-shrink-0 cursor-pointer text-3.5 text-theme-text-hover"
                @click="deleteFile" />
            </div>
          </div>
        </div>

        <div v-if="errorFlag" class="text-3 leading-5 text-rule">
          <span v-if="emptyFlag">{{ t('scriptHome.import.messages.uploadFirst') }}</span>
          <span v-if="maximumLimitFlag">{{ t('scriptHome.import.messages.fileTooLarge') }}</span>
        </div>
      </FormItem>
    </Form>
  </Modal>
</template>

<style scoped>
.upload-container[error="true"] {
  border-color: #ff4d4f;
}
</style>
