<script setup lang="ts">
import { computed, ref } from 'vue';
import { Radio, RadioGroup } from 'ant-design-vue';
import { Hints, Modal, notification, SingleUpload } from '@xcan-angus/vue-ui';
import { http, TESTER } from '@xcan-angus/tools';

interface Props {
  id:string;// mock service Id
  visible:boolean;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  visible: false
});

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
  const [error] = await http.post(`${TESTER}/mock/service/import`, formData);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('导入接口成功');
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
    title="导入接口"
    :visible="props.visible"
    :okButtonProps="okButtonProps"
    :bodyStyle="bodyStyle"
    :confirmLoading="loading"
    @ok="ok"
    @cancel="cancel">
    <Hints text="支持Swagger2.0、OpenAPI3.x、Angus格式接口文件导入。" />
    <SingleUpload
      v-if="props.visible"
      allorPaste
      class="mb-5"
      accept=".zip,.rar,.7z,.gz,.tar,.bz2,.xz,.lzma,.json,.yaml,.yml"
      @change="change" />

    <div class="space-y-0.5 mb-5">
      <div>遇到重复时的处理策略</div>
      <RadioGroup v-model:value="duplicatedValue">
        <Radio value="COVER">覆盖</Radio>
        <Radio value="IGNORE">忽略</Radio>
      </RadioGroup>
    </div>

    <div class="space-y-0.5">
      <div>当前接口在导入数据中不存在时是否删除</div>
      <RadioGroup v-model:value="notExistedValue">
        <Radio value="true">是</Radio>
        <Radio value="false">否</Radio>
      </RadioGroup>
    </div>
  </Modal>
</template>
