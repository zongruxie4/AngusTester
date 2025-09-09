<script lang="ts" setup>
import { reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Modal, notification } from '@xcan-angus/vue-ui';
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
    class="delete-test-modal-container"
    :title="t('commonComp.delTaskTestModal.title')"
    :width="600"
    :confirmLoading="isDeleting"
    :visible="props.visible"
    @cancel="handleModalClose"
    @ok="handleDeleteConfirm">
    <!-- Confirmation information area -->
    <div class="confirm-section">
      <div class="confirm-icon">
        <Icon class="info-icon" icon="icon-tishi1" />
      </div>
      <div class="confirm-content">
        <p class="confirm-message">{{ t('commonComp.delTaskTestModal.confirmMessage') }}</p>
      </div>
    </div>

    <!-- Selection area -->
    <div class="selection-section">
      <h4 class="section-title">{{ t('commonComp.delTaskTestModal.selectDeleteTestType') }}</h4>
      <Form
        ref="deleteFormRef"
        :model="deleteFormData"
        class="selection-form">
        <FormItem
          :rules="{ validator: validateTestTypeSelection, message: t('commonComp.delTaskTestModal.selectTestType'), required: true }"
          name="selectedTestTypes">
          <CheckboxGroup
            v-model:value="deleteFormData.selectedTestTypes"
            :options="testTypeOptions()"
            class="test-type-checkboxes">
          </CheckboxGroup>
        </FormItem>
      </Form>
    </div>
  </Modal>
</template>

<style scoped>
/* Confirmation information area */
.confirm-section {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  margin-bottom: 24px;
  background-color: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 6px;
}

.confirm-icon {
  color: #ff4d4f;
  font-size: 20px;
  margin-right: 12px;
  flex-shrink: 0;
}

.confirm-content {
  flex: 1;
}

.confirm-message {
  font-size: 14px;
  color: #595959;
  line-height: 1.5;
  margin: 0;
}

/* Selection area */
.selection-section {
  margin-top: 24px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.selection-form {
  background-color: #fafafa;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
}

.test-type-checkboxes {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* Deep style overrides */
:deep(.ant-checkbox-wrapper) {
  font-size: 12px;
  color: #262626;
  margin-right: 0;
  margin-bottom: 8px;
  padding: 4px 0;
}

:deep(.ant-checkbox-wrapper:hover) {
  background-color: #f5f5f5;
  border-radius: 4px;
  padding: 4px 8px;
  margin: 0 -8px 8px -8px;
}

:deep(.ant-checkbox) {
  margin-right: 8px;
}

:deep(.ant-checkbox-inner) {
  width: 16px;
  height: 16px;
}

:deep(.ant-checkbox-checked .ant-checkbox-inner) {
  background-color: #ff4d4f;
  border-color: #ff4d4f;
}

:deep(.ant-form-item-label > label) {
  font-size: 12px;
  font-weight: 500;
  color: #262626;
}

:deep(.ant-form-item-label > label.ant-form-item-required:not(.ant-form-item-required-mark-optional)::before) {
  color: #ff4d4f;
}

/* Responsive design */
@media (max-width: 768px) {
  .confirm-section {
    padding: 12px;
  }

  .selection-form {
    padding: 12px;
  }

  .confirm-icon {
    font-size: 18px;
    margin-right: 10px;
  }
}
</style>
