<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Modal, notification, Select } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { task } from '@/api/tester';

const { t } = useI18n();

interface Props {
  projectId: string;
  visible: boolean;
  taskIds: string;
  sprintId?: string;
  taskName?: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  visible: false,
  taskIds: undefined,
  sprintId: undefined,
  taskName: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok', value: string, ids:string[]): void;
}>();

const confirmLoading = ref(false);
const selectedId = ref<string>();
const defaultOptions = {
  name: t('task.moveModal.defaultOptions.productBacklog'),
  id: ''
};

const cancel = () => {
  emit('update:visible', false);
};

const confirm = async () => {
  const currentSprintId = selectedId.value;

  const prevSprintId = props.sprintId;
  if (currentSprintId === prevSprintId) {
    cancel();
    return;
  }

  confirmLoading.value = true;
  const taskIds = Array.isArray(props.taskIds) ? props.taskIds : [props.taskIds];
  const params = {
    taskIds,
    targetSprintId: currentSprintId || undefined
  };
  const [error] = await task.moveTask(params);
  confirmLoading.value = false;
  if (error) {
    return;
  }

  emit('ok', currentSprintId, params.taskIds);
  cancel();

  if (props.taskName) {
    notification.success(t('task.moveModal.messages.moveSuccess'));
  } else {
    notification.success(t('task.moveModal.messages.batchMoveSuccess', { num: props.taskIds?.length }));
  }
};

onMounted(() => {
  watch(() => props.sprintId, (newValue) => {
    selectedId.value = newValue;
  }, { immediate: true });
});

const format = (data) => {
  return { ...data, disabled: data.id === props.sprintId };
};
</script>
<template>
  <Modal
    :title="t('task.moveModal.title')"
    :visible="props.visible"
    :width="500"
    :confirmLoading="confirmLoading"
    @cancel="cancel"
    @ok="confirm">
    <div class="flex items-center">
      <div class="mr-2">{{ t('task.moveModal.form.selectIteration') }}</div>
      <Select
        v-model:value="selectedId"
        :action="`${TESTER}/task/sprint?projectId=${props.projectId}&fullTextSearch=true`"
        :fieldNames="{ value: 'id', label: 'name' }"
        :format="format"
        :additionalOption="defaultOptions"
        showSearch
        :placeholder="t('task.moveModal.form.selectIterationPlaceholder')"
        class="flex-1" />
    </div>
  </Modal>
</template>
