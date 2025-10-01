<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { Colon, IconTask, Modal, notification, Select } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { task } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { AssocCaseProps } from '@/views/issue/issue/list/types';

const props = withDefaults(defineProps<AssocCaseProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  visible: false,
  taskInfo: undefined
});

// Composables
const { t } = useI18n();

/**
 * Event emitter for component communication
 * <p>
 * Emits events to notify parent components about modal visibility changes
 * and successful operations
 */
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok'): void;
}>();

/**
 * Loading state for API operations
 * <p>
 * Indicates whether a task association operation is currently in progress
 */
const isOperationLoading = ref(false);

/**
 * Array of selected task IDs for association
 * <p>
 * Contains the IDs of tasks that will be associated as sub-tasks
 */
const selectedTaskIds = ref<string[]>([]);

/**
 * Closes the modal and resets form data
 * <p>
 * Emits visibility update event and clears the selected task IDs
 */
const closeModal = () => {
  emit('update:visible', false);
  resetFormData();
};

/**
 * Handles the confirmation of task association
 * <p>
 * Processes the selected task IDs and creates sub-task associations
 * through the API. Shows success notification and closes modal on completion.
 */
const confirmTaskAssociation = async () => {
  const requestParams = {
    subTaskIds: selectedTaskIds.value
  };

  isOperationLoading.value = true;
  const [error] = await task.setSubTask(props.taskInfo?.id, requestParams);
  isOperationLoading.value = false;

  if (error) {
    return;
  }

  notification.success(t('task.subTask.messages.associateSubTaskSuccess'));
  emit('update:visible', false);
  emit('ok');
  resetFormData();
};

/**
 * Resets the form data to initial state
 * <p>
 * Clears all selected task IDs and resets the form
 */
const resetFormData = () => {
  selectedTaskIds.value = [];
};

/**
 * Component mounted lifecycle hook
 * <p>
 * Sets up a watcher to monitor modal visibility changes and populate
 * the selected task IDs when the modal is opened
 */
onMounted(() => {
  watch(() => props.visible, (isModalVisible) => {
    if (!isModalVisible) {
      return;
    }

    selectedTaskIds.value = props.taskInfo?.subTaskInfos?.map(item => item.id) || [];
  }, { immediate: true });
});

/**
 * Determines if a task should be excluded from selection
 * <p>
 * Prevents the current task from being selected as its own sub-task
 *
 * @param taskData - Task data object containing ID
 * @returns True if the task should be excluded from selection
 */
const shouldExcludeTask = (taskData: { id: string }) => {
  return props.taskInfo?.id === taskData.id;
};
</script>
<template>
  <!-- Task reference association modal -->
  <Modal
    :title="t('task.subTask.modal.title')"
    :centered="true"
    :visible="props.visible"
    :width="680"
    @cancel="closeModal"
    @ok="confirmTaskAssociation">
    <div class="flex items-start">
      <!-- Label section -->
      <div class="flex items-center flex-shrink-0 mr-1.5 leading-7">
        <span>{{ t('task.subTask.modal.label') }}</span>
        <Colon />
      </div>

      <!-- Task selection component -->
      <Select
        v-model:value="selectedTaskIds"
        showSearch
        internal
        mode="tags"
        :placeholder="t('task.subTask.modal.placeholder')"
        class="flex-1"
        :excludes="shouldExcludeTask"
        :fieldNames="{ label: 'name', value: 'id' }"
        :action="`${TESTER}/task?projectId=${props.projectId}&fullTextSearch=true`">
        <template #option="record">
          <div class="flex items-center">
            <IconTask :value="record.taskType?.value" class="text-4 flex-shrink-0" />
            <span class="ml-1.5">{{ record.name }}</span>
          </div>
        </template>
      </Select>
    </div>
  </Modal>
</template>
