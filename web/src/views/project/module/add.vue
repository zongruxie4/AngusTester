<script setup lang="ts">
// Vue composition API imports
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';

// Ant Design components
import { ButtonProps } from 'ant-design-vue';

// Custom UI components
import { Input, Modal, notification } from '@xcan-angus/vue-ui';

// Utilities and API
import { modules } from '@/api/tester';
import {AddModuleProps} from "@/views/project/module/types";

// Initialize i18n
const { t } = useI18n();

// Props and emits
const props = withDefaults(defineProps<AddModuleProps>(), {
  projectId: undefined,
  visible: false,
  pid: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok', value: { id: string; name: string }): void;
}>();

// Reactive data
const inputValue = ref<string>();
const okButtonProps = ref<ButtonProps>({
  loading: false,
  disabled: true
});

// Event handlers
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
  notification.success(t('project.projectEdit.module.addSuccess'));
};

const cancel = () => {
  inputValue.value = undefined;
  emit('update:visible', false);
};

const inputChange = () => {
  okButtonProps.value.disabled = !inputValue.value || !inputValue.value.trim();
};

const handleEnter = () => {
  if (!inputValue.value || !inputValue.value.trim()) {
    return;
  }
  ok();
};
</script>

<template>
  <!-- Modal for adding new module -->
  <Modal
    :title="t('project.projectEdit.module.addModuleTitle')"
    width="500px"
    :visible="props.visible"
    :okButtonProps="okButtonProps"
    @cancel="cancel"
    @ok="ok">
    <!-- Input field for module name -->
    <Input
      v-model:value="inputValue"
      :placeholder="t('project.projectEdit.module.moduleNamePlaceholder')"
      trim
      :allowClear="true"
      :maxlength="50"
      @pressEnter="handleEnter"
      @change="inputChange" />
  </Modal>
</template>
