<script setup lang="ts">
import { ref } from 'vue';
import { ButtonProps } from 'ant-design-vue';
import { Input, Modal, notification } from '@xcan-angus/vue-ui';
import { tagApi } from '@/api/tester';

interface Props {
  projectId: string;
  visible: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  visible: false
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
    projectId: props.projectId
  };
  const [error, res] = await tagApi.addTag(params);
  okButtonProps.value.loading = false;
  if (error) {
    return;
  }

  const id = res?.data?.[0]?.id || '';
  emit('ok', { id, name });

  inputValue.value = undefined;
  emit('update:visible', false);
  notification.success('标签添加成功');
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
    title="添加标签"
    width="500px"
    :visible="props.visible"
    :okButtonProps="okButtonProps"
    @cancel="cancel"
    @ok="ok">
    <Input
      v-model:value="inputValue"
      placeholder="标签名称，最大支持50个字符"
      trim
      :allowClear="true"
      :maxlength="50"
      @pressEnter="hanedleEnterPress"
      @change="inputChange" />
  </Modal>
</template>
