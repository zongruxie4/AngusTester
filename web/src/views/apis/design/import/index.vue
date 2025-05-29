<script lang="ts" setup>
import { ref } from 'vue';
import {Modal, Input, notification, Icon } from '@xcan-angus/vue-ui';
import {  Form, FormItem, Upload  } from 'ant-design-vue';
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
      status: 'done',
    }];
    formRef.value.validate(['content'])
  };
  reader.readAsText(file)
};

const delFile = () => {
  fileList.value = [];
  formState.value.content = undefined;
}

const cancel = () => {
  emits('update:visible', false);
};

const ok = async () => {
  formRef.value.validate().then(async () => {
    const formData = new FormData();
    formData.append('projectId', props.projectId);
    formData.append('name', formState.value.name);
    formData.append('content', formState.value.content);
    const [error] = await apis.importDesign(formData);
    if (error) {
      return;
    }
    notification.success('导入成功');
    cancel();
    emits('ok');
  })
};

</script>
<template>
  <Modal
    title="导入设计"
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
      <FormItem name="name" label="名称" required>
        <Input
          v-model:value="formState.name"
          :maxlength="100"
          placeholder="输入设计名称, 限制100个字符"/>
      </FormItem>
      <FormItem name="content" label="文件" :rules="[{required: true, message: '请上传文件'}]">
        <Upload
          v-model:fileList="fileList"
          class="w-full"
          accept=".json,.yaml"
          :customRequest="handleFile"
          @remove="delFile">
          <div v-show="!formState.content" class="border border-dashed rounded p-4 flex flex-col items-center justify-around space-y-2 text-3 border-blue-1">
            <Icon icon="icon-daoru" class="text-blue-hover" />
            <span class="text-blue-hover" >上传文件</span>
            <div class="text-text-sub-content">
              点击上传Swagger2.0、OpenAPI3.x文件，文件大小不超过20M
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

