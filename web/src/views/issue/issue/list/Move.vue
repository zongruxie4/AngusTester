<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Modal, notification, Select } from '@xcan-angus/vue-ui';
import { RadioGroup, Radio } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import { task } from '@/api/tester';

const { t } = useI18n();

// Move scope enum for clarity and type safety
enum MoveScope {
  BACKLOG = 'BACKLOG',
  SPRINT = 'SPRINT'
}

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
const moveScope = ref<MoveScope>(MoveScope.SPRINT);

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
  // Guard: when choosing sprint, require a target sprint selection
  if (moveScope.value === MoveScope.SPRINT && !selectedSprintId.value) {
    return;
  }

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
    // When moving to backlog, send undefined
    targetSprintId: moveScope.value === MoveScope.BACKLOG ? undefined : (targetSprintId || undefined)
  };

  const [error] = await task.moveTask(moveParams);
  confirmLoading.value = false;

  if (error) {
    return;
  }

  emit('ok', targetSprintId || '', moveParams.taskIds);
  closeModal();

  // Show success notification based on single or batch move
  notification.success(t('actions.tips.moveSuccess'));
};

// Lifecycle Hooks
onMounted(() => {
  watch(() => props.sprintId, (newValue) => {
    selectedSprintId.value = newValue;
    moveScope.value = newValue ? MoveScope.SPRINT : MoveScope.BACKLOG;
  }, { immediate: true });

  // Clear selected sprint when switching to BACKLOG
  watch(() => moveScope.value, (newValue) => {
    if (newValue === MoveScope.BACKLOG) {
      selectedSprintId.value = undefined;
    }
  }, { immediate: false });
});
</script>
<template>
  <Modal
    :title="t('actions.move')"
    :visible="props.visible"
    :width="520"
    :confirmLoading="confirmLoading"
    @cancel="closeModal"
    @ok="moveTasksToSprint">
    <div class="space-y-3 pt-1">
      <div class="flex items-center px-3 py-2">
        <div class="mr-3 min-w-18 text-theme-sub-content">{{ t('actions.moveTo') }}</div>
        <RadioGroup v-model:value="moveScope" size="small">
          <Radio :value="MoveScope.BACKLOG">{{ t('issue.actions.move.options.productBacklog') }}</Radio>
          <Radio :value="MoveScope.SPRINT">{{ t('issue.actions.move.options.productSprint') }}</Radio>
        </RadioGroup>
      </div>

      <div v-if="moveScope === MoveScope.SPRINT" class="flex items-center px-3 py-2">
        <div class="mr-3 min-w-18 text-theme-sub-content">{{ t('common.placeholders.selectSprint') }}</div>
        <Select
          v-model:value="selectedSprintId"
          :action="`${TESTER}/task/sprint?projectId=${props.projectId}&fullTextSearch=true`"
          :fieldNames="{ value: 'id', label: 'name' }"
          :format="formatSprintOption"
          showSearch
          :placeholder="t('common.placeholders.selectSprint')"
          class="flex-1" />
      </div>
    </div>
  </Modal>
</template>
