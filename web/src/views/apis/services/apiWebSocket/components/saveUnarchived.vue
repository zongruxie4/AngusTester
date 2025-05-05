<script lang="ts" setup>
import { inject, onMounted, reactive, ref } from 'vue';
import { Button, Form, FormItem } from 'ant-design-vue';
import { Hints, Input, notification } from '@xcan-angus/vue-ui';

import { unarchived } from 'src/api/tester';

interface Props {
  getParameter: any;
  id: string;
  summary: string;
  operationId: string;
  projectId: string;
  description: string;
  status: string;
  ownerId: string;
  deprecated: boolean;
  tabKey: string;
  packageParams: ()=> Record<string, any>;
}

// eslint-disable-next-line @typescript-eslint/no-empty-function
const replaceTabPane = inject<(key:string, data: any) => void>('replaceTabPane', () => { });
const close = inject('close', () => ({}));
// eslint-disable-next-line @typescript-eslint/no-empty-function
const updateTabPane = inject<(data: any) => void>('updateTabPane', () => { });

// 更新左侧未归档列表
// eslint-disable-next-line @typescript-eslint/no-empty-function
const refreshUnarchived = inject('refreshUnarchived', () => {});
const projectInfo = inject('projectInfo', ref({ id: '' }));

const props = withDefaults(defineProps<Props>(), {
  getParameter: () => ({})
});

const emits = defineEmits<{(e: 'ok'): void}>();

const form = reactive({
  summary: '',
  description: ''
});

const formRef = ref();
const rules = {
  summary: [{
    required: true, message: '请输入接口名称，100字符以内', trigger: 'blur'
  }]
};

const isLoading = ref(false);

const save = () => {
  formRef.value.validate().then(async () => {
    const params = form;
    const apiInfo = await props.packageParams();
    if (apiInfo.protocol !== 'ws' && apiInfo.protocol !== 'wss') {
      notification.warning('WebSocket协议必须以ws://或wss://开头');
      return;
    }

    const { currentServer, method, parameters, protocol, requestBody, endpoint } = apiInfo;
    const [error, resp] = props.id
      ? await unarchived.updateApi({ dto: [{ currentServer, method, parameters, protocol, requestBody, endpoint, ...params, id: props.id, projectId: projectInfo.value?.id }] })
      : await unarchived.addApi({ dto: [{ currentServer, method, parameters, protocol, requestBody, endpoint, ...params, projectId: projectInfo.value?.id }] });
    if (error) {
      return;
    }
    if (props.id) {
      notification.success('更新接口成功');
    } else {
      notification.success('添加接口成功');
      refreshUnarchived();
    }
    const id = props.id || resp.data[0]?.id;
    if (props.id) {
      updateTabPane({
        _id: id + 'socket',
        pid: props.tabKey,
        id: id,
        name: params.summary,
        unarchived: true,
        value: 'socket'
      });
    } else {
      replaceTabPane(props.tabKey, {
        _id: id + 'socket',
        pid: id + 'socket',
        name: params.summary,
        id: id,
        unarchived: true,
        value: 'socket'
      });
    }
    handleClose();
    emits('ok');
  });
};

const handleClose = () => {
  close();
};

onMounted(() => {
  form.summary = props.summary;
  form.description = props.description;
});
</script>
<template>
  <Form
    ref="formRef"
    layout="vertical"
    :model="form"
    :rules="rules">
    <Hints
      text="未归档接口为用户临时调试接口，只对添加用户可见"
      class="mb-1.5" />
    <FormItem label="接口名称" name="summary">
      <Input
        v-model:value="form.summary"
        :maxlength="40"
        :allowClear="false"
        class="rounded"
        placeholder="请输入接口名称，40字符以内" />
    </FormItem>
    <FormItem label="描述">
      <Input
        v-model:value="form.description"
        type="textarea"
        showCount
        :rows="4"
        :allowClear="false"
        class="rounded-border"
        placeholder="限制输入200字符以内" />
    </FormItem>
    <FormItem>
      <Button
        type="primary"
        class="mr-2.5 rounded"
        size="small"
        :loading="isLoading"
        @click="save">
        保存
      </Button>
      <Button
        class="rounded"
        size="small"
        @click="handleClose">
        取消
      </Button>
    </FormItem>
  </Form>
</template>
