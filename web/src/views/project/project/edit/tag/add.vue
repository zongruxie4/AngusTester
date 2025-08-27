<script setup lang="ts">
// Vue composition API imports
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';

// Ant Design components
import { ButtonProps } from 'ant-design-vue';

// Custom UI components
import { Input, Modal, notification } from '@xcan-angus/vue-ui';

// API imports and utils
import { tag } from '@/api/tester';
import { AddTagProps } from '@/views/project/project/types';

// Initialize i18n
const { t } = useI18n();

// Props and emits
const props = withDefaults(defineProps<AddTagProps>(), {
  projectId: undefined,
  visible: false
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
    projectId: props.projectId
  };
  const [error, res] = await tag.addTag(params);
  okButtonProps.value.loading = false;
  if (error) {
    return;
  }

  const id = res?.data?.[0]?.id || '';
  emit('ok', { id, name });

  inputValue.value = undefined;
  emit('update:visible', false);
  notification.success(t('project.projectEdit.tag.addSuccess'));
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
  <!-- Modal for adding new tag -->
  <Modal
    :title="t('project.projectEdit.tag.addTagTitle')"
    width="500px"
    :visible="props.visible"
    :okButtonProps="okButtonProps"
    @cancel="cancel"
    @ok="ok">
    <!-- Input field for tag name -->
    <Input
      v-model:value="inputValue"
      :placeholder="t('project.projectEdit.tag.tagNamePlaceholder')"
      trim
      :allowClear="true"
      :maxlength="50"
      @pressEnter="hanedleEnterPress"
      @change="inputChange" />
  </Modal>
</template>
