<script lang="ts" setup>
import { reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Modal, notification } from '@xcan-angus/vue-ui';
import { CheckboxGroup, Form, FormItem } from 'ant-design-vue';
import { TestType } from '@/enums/enums';

import { apis, scenario, services } from '@/api/tester';
import { CombinedTargetType, enumOptionUtils, enumUtils } from '@xcan-angus/infra';

const { t } = useI18n();

// ===== Component Props and Emits =====
interface Props {
  visible: boolean;
  id?: string;
  type: CombinedTargetType.API | CombinedTargetType.SERVICE | CombinedTargetType.SCENARIO;
}

const props = withDefaults(defineProps<Props>(), {
  id: '',
  visible: false,
  type: undefined
});

const emits = defineEmits<{(e: 'update:visible', value: boolean) }>();

// ===== API Configuration =====
/**
 * <p>
 * Maps target types to their corresponding delete API methods.
 * <p>
 * Provides a centralized way to handle different API calls based on the target type.
 */
const DELETE_API_MAP = {
  SERVICE: services.deleteTest,
  API: apis.deleteTestTask,
  SCENARIO: scenario.deleteTestTask
};

// ===== Reactive Data and State =====
const isFormValidated = ref(false);
const isDeleting = ref(false);

// Form reference for validation
const deleteFormRef = ref();

// Form data for test type selection
const deleteFormData = reactive({
  selectedTestTypes: enumUtils.getEnumValues(TestType, [TestType.CUSTOMIZATION])
});

// ===== Computed Properties =====
/**
 * <p>
 * Generates test type options for the checkbox group.
 * <p>
 * Excludes the CUSTOMIZATION test type from the available options.
 * @returns Array of test type options for the checkbox group
 */
const testTypeOptions = () => {
  return enumOptionUtils.loadEnumAsOptions(TestType, {
    excludeValues: [TestType.CUSTOMIZATION]
  });
};

// ===== Validation Methods =====
/**
 * <p>
 * Validates that at least one test type is selected for deletion.
 * <p>
 * Returns a resolved promise if test types are selected, rejected otherwise.
 * @returns Promise that resolves if validation passes, rejects otherwise
 */
const validateTestTypeSelection = async () => {
  if (deleteFormData.selectedTestTypes.length) {
    return Promise.resolve();
  }
  // eslint-disable-next-line prefer-promise-reject-errors
  return Promise.reject();
};

// ===== Form Management Methods =====
/**
 * <p>
 * Resets the form data to initial state.
 * <p>
 * Clears validation errors and resets selected test types to default values.
 */
const resetFormData = () => {
  if (typeof deleteFormRef.value?.clearValidate === 'function') {
    deleteFormRef.value.clearValidate();
  }
  deleteFormData.selectedTestTypes = enumUtils.getEnumValues(TestType, [TestType.CUSTOMIZATION]);
};

// ===== Event Handlers =====
/**
 * <p>
 * Handles the delete confirmation action.
 * <p>
 * Validates the form, calls the appropriate delete API, and shows success notification.
 * Closes the modal on successful deletion.
 */
const handleDeleteConfirm = async () => {
  deleteFormRef.value.validate()
    .then(async () => {
      isDeleting.value = true;
      const [error] = await DELETE_API_MAP[props.type](props.id, deleteFormData.selectedTestTypes);
      isDeleting.value = false;
      if (error) {
        return;
      }

      notification.success(t('commonComp.delTaskTestModal.deleteSuccess'));
      emits('update:visible', false);
    });
};

/**
 * <p>
 * Handles the modal close action.
 * <p>
 * Emits the update:visible event to close the modal.
 */
const handleModalClose = () => {
  emits('update:visible', false);
};

// ===== Lifecycle Hooks =====
watch(() => props.visible, () => {
  isFormValidated.value = false;
  resetFormData();
}, {
  immediate: true
});

</script>
<template>
  <Modal
    :title="t('commonComp.delTaskTestModal.title')"
    :width="580"
    :confirmLoading="isDeleting"
    :visible="props.visible"
    @cancel="handleModalClose"
    @ok="handleDeleteConfirm">
    <div class="mb-2">
      {{ t('commonComp.delTaskTestModal.confirmMessage') }}
    </div>
    <Form ref="deleteFormRef" :model="deleteFormData">
      <FormItem
        :rules="{ validator: validateTestTypeSelection, message: t('commonComp.delTaskTestModal.selectTestType'), required: true }"
        :label="t('commonComp.delTaskTestModal.selectDeleteTestType')"
        class="w-full"
        name="selectedTestTypes">
        <CheckboxGroup v-model:value="deleteFormData.selectedTestTypes" :options="testTypeOptions()"></CheckboxGroup>
      </FormItem>
    </Form>
  </Modal>
</template>
