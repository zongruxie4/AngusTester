<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Modal, notification, Select } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { task } from '@/api/tester';

const { t } = useI18n();

// Props and Emits Definition
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

// Reactive data
const confirmLoading = ref(false);
const selectedSprintId = ref<string>();
const defaultSprintOption = {
  name: t('task.moveModal.defaultOptions.productBacklog'),
  id: ''
};

// Lifecycle Hooks
onMounted(() => {
  watch(() => props.sprintId, (newValue) => {
    selectedSprintId.value = newValue;
  }, { immediate: true });
});

/**
 * Close the modal dialog
 */
const closeModal = () => {
  emit('update:visible', false);
};

/**
 * Format sprint data to disable current sprint in selection
 */
const formatSprintOption = (data: any) => {
  return { ...data, disabled: data.id === props.sprintId };
};

/**
 * Move tasks to selected sprint
 */
const moveTasksToSprint = async () => {
  const targetSprintId = selectedSprintId.value;
  const sourceSprintId = props.sprintId;

  // If target sprint is the same as source sprint, just close the modal
  if (targetSprintId === sourceSprintId) {
    closeModal();
    return;
  }

  confirmLoading.value = true;
  const taskIds = Array.isArray(props.taskIds) ? props.taskIds : [props.taskIds];
  const moveParams = {
    taskIds,
    targetSprintId: targetSprintId || undefined
  };

  const [error] = await task.moveTask(moveParams);
  confirmLoading.value = false;

  if (error) {
    return;
  }

  emit('ok', targetSprintId || '', moveParams.taskIds);
  closeModal();

  // Show success notification based on single or batch move
  if (props.taskName) {
    notification.success(t('task.moveModal.messages.moveSuccess'));
  } else {
    notification.success(t('task.moveModal.messages.batchMoveSuccess', { num: props.taskIds?.length }));
  }
};
</script>
<template>
  <Modal
    :title="t('task.moveModal.title')"
    :visible="props.visible"
    :width="500"
    :confirmLoading="confirmLoading"
    @cancel="closeModal"
    @ok="moveTasksToSprint">
    <div class="flex items-center">
      <div class="mr-2">{{ t('task.moveModal.form.selectSprint') }}</div>
      <Select
        v-model:value="selectedSprintId"
        :action="`${TESTER}/task/sprint?projectId=${props.projectId}&fullTextSearch=true`"
        :fieldNames="{ value: 'id', label: 'name' }"
        :format="formatSprintOption"
        :additionalOption="defaultSprintOption"
        showSearch
        :placeholder="t('task.moveModal.form.selectSprintPlaceholder')"
        class="flex-1" />
    </div>
  </Modal>
</template>
