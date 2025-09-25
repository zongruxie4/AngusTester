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

const emits = defineEmits<{(e: 'cancel'):void; (e: 'ok'):void; (e: 'update:visible', value: boolean):void}>();
const { t } = useI18n();
const loading = ref(false);
const formRef = ref();
const fileList = ref<{name: string, status: string}[]>([]);
const formState = ref({
  name: undefined,
  content: undefined
});

const handleFile = async (fileObj) => {
  const file = fileObj.file;
  const reader = new FileReader();

  reader.onload = (e: {target: {result: string}}) => {
    formState.value.content = e.target.result;
    fileList.value = [{
      name: file.name,
      status: 'done'
    }];
    formRef.value.validate(['content']);
  };
  reader.readAsText(file);
};

const delFile = () => {
  fileList.value = [];
  formState.value.content = undefined;
};

const cancel = () => {
  emits('update:visible', false);
};

const ok = async () => {
  formRef.value.validate().then(async () => {
    loading.value = true;
    const formData = new FormData();
    formData.append('projectId', props.projectId);
    formData.append('name', formState.value.name);
    formData.append('content', formState.value.content);
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
    :title="t('design.importDesignModal.title')"
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
        :label="t('design.importDesignModal.nameLabel')"
        required>
        <Input
          v-model:value="formState.name"
          :maxlength="100"
          :placeholder="t('design.importDesignModal.namePlaceholder')" />
      </FormItem>
      <FormItem
        name="content"
        :label="t('design.importDesignModal.fileLabel')"
        :rules="[{required: true, message: t('design.importDesignModal.fileRule')}]">
        <Upload
          v-model:fileList="fileList"
          class="w-full"
          accept=".json,.yaml"
          :customRequest="handleFile"
          @remove="delFile">
          <div v-show="!formState.content" class="border border-dashed rounded p-4 flex flex-col items-center justify-around space-y-2 text-3 border-blue-1">
            <Icon icon="icon-daoru" class="text-blue-hover" />
            <span class="text-blue-hover">{{ t('design.importDesignModal.uploadFile') }}</span>
            <div class="text-text-sub-content">
              {{ t('design.importDesignModal.uploadTip') }}
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
