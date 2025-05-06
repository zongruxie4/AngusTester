<script setup lang="ts">
import { ref } from 'vue';
import { ButtonProps } from 'ant-design-vue';
import { Input, Modal, notification } from '@xcan-angus/vue-ui';
import { modules } from '@/api/tester';

interface Props {
  projectId: string;
  visible: boolean;
  pid?: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  visible: false,
  pid: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok', value: { id: string; name: string }): void;
}>();

const inputValue = ref<string>();
const okButtonProps = ref<ButtonProps>({
  loading: false,
  disabled: true
});

const ok = async () => {
  okButtonProps.value.loading = true;
  const name = inputValue.value as string;
  const params = {
    names: [name],
    projectId: props.projectId,
    pid: props.pid
  };
  const [error, res] = await modules.addModule(params);
  okButtonProps.value.loading = false;
  if (error) {
    return;
  }

  const id = res?.data?.[0]?.id || '';
  emit('ok', { id, name });

  inputValue.value = undefined;
  emit('update:visible', false);
  notification.success('模块添加成功');
};

const cancel = () => {
  inputValue.value = undefined;
  emit('update:visible', false);
};

const inputChange = () => {
  okButtonProps.value.disabled = !inputValue.value || !inputValue.value.trim();
};

const hanedleEnterPress = () => {
  if (!inputValue.value || !inputValue.value.trim()) {
    return;
  }
  ok();
};
</script>

<template>
  <Modal
    title="添加模块"
    width="500px"
    :visible="props.visible"
    :okButtonProps="okButtonProps"
    @cancel="cancel"
    @ok="ok">
    <Input
      v-model:value="inputValue"
      placeholder="模块名称，最大支持50个字符"
      trim
      :allowClear="true"
      :maxlength="50"
      @pressEnter="hanedleEnterPress"
      @change="inputChange" />
  </Modal>
</template>
