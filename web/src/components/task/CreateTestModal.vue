<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, reactive, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { CombinedTargetType, EnumMessage, http, Priority, TESTER } from '@xcan-angus/infra';
import { CheckboxGroup, Form, FormItem } from 'ant-design-vue';
import { Icon, Modal, notification, Select, Toggle } from '@xcan-angus/vue-ui';
import { project } from '@/api/tester';
import { ProjectType, TestType } from '@/enums/enums';
import { FormData } from './types';
import { ProjectDisplayInfo } from '@/layout/types';

const { t } = useI18n();

// Lazy load TestForm component to improve performance
const TestForm = defineAsyncComponent(() => import('./TestForm.vue'));

// ===== Component Props and Emits =====
interface Props {
  visible: boolean,
  infoText: string,
  type: CombinedTargetType.API | CombinedTargetType.SERVICE | CombinedTargetType.SCENARIO;
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  infoText: '',
  type: CombinedTargetType.API,
  id: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void,
  (e: 'update:id', val: string | undefined): void,
}>();

// ===== Reactive Data and State =====
// Inject project information from parent component
const projectInfo = inject<Ref<ProjectDisplayInfo>>('projectInfo', ref({ id: '', name: '', type: {} as EnumMessage<ProjectType> }));
const projectMembers = ref([]);

// Form references for validation
const sprintFormRef = ref();
const projectSelectRef = ref();
const performanceFormRef = ref();
const functionalFormRef = ref();
const stabilityFormRef = ref();

// Sprint configuration data
const taskConfiguration = reactive({
  id: undefined,
  activeKey: [TestType.FUNCTIONAL]
});

// Test form data for different test types
const performanceTestForm = ref<FormData>({
  deadlineDate: '',
  priority: Priority.MEDIUM,
  assigneeId: undefined,
  testType: TestType.PERFORMANCE
});

const functionalTestForm = ref<FormData>({
  deadlineDate: '',
  priority: Priority.MEDIUM,
  assigneeId: undefined,
  testType: TestType.FUNCTIONAL
});

const stabilityTestForm = ref<FormData>({
  deadlineDate: '',
  priority: Priority.MEDIUM,
  assigneeId: undefined,
  testType: TestType.STABILITY
});

// Toggle configuration for form sections visibility
const formSectionToggleConfig = reactive({
  functional: true,
  performance: true,
  stability: true
});

// Test type options for checkbox group
const testTypeOptions = [
  { label: t('commonComp.createTaskTestModal.functionalTest'), value: TestType.FUNCTIONAL },
  { label: t('commonComp.createTaskTestModal.performanceTest'), value: TestType.PERFORMANCE },
  { label: t('commonComp.createTaskTestModal.stabilityTest'), value: TestType.STABILITY }
];

const isConfirmLoading = ref<boolean>(false);

// ===== Computed Properties =====
/**
 * <p>
 * Computed property that returns form data based on selected test types.
 * <p>
 * Only includes form data for test types that are currently active.
 * @returns Array of form data objects for active test types
 */
const computedFormData = computed(() => {
  const result: FormData[] = [];
  if (taskConfiguration.activeKey.includes(TestType.PERFORMANCE)) {
    result.push(performanceTestForm.value);
  }

  if (taskConfiguration.activeKey.includes(TestType.FUNCTIONAL)) {
    result.push(functionalTestForm.value);
  }

  if (taskConfiguration.activeKey.includes(TestType.STABILITY)) {
    result.push(stabilityTestForm.value);
  }
  return result;
});

/**
 * <p>
 * Computed property that generates the API endpoint URL based on the target type.
 * <p>
 * Returns the appropriate URL for creating test tasks for API, SERVICE, or SCENARIO.
 * @returns The complete API endpoint URL
 */
const apiEndpointUrl = computed(() => {
  switch (props.type) {
    case CombinedTargetType.API:
      return `${TESTER}/apis/${props.id}/test/task/generate?taskSprintId=${taskConfiguration.id}`;
    case CombinedTargetType.SERVICE:
      return `${TESTER}/services/${props.id}/test/task/generate?taskSprintId=${taskConfiguration.id}`;
    case CombinedTargetType.SCENARIO:
      return `${TESTER}/scenario/${props.id}/test/task/generate?taskSprintId=${taskConfiguration.id}`;
    default:
      return '';
  }
});

// ===== API Methods =====
/**
 * <p>
 * Loads project members from the API.
 * <p>
 * Fetches the list of project members and updates the local state.
 */
const loadProjectMembers = async () => {
  const [error, { data }] = await project.getProjectMember(projectInfo.value.id);
  if (error) {
    return;
  }
  projectMembers.value = data || [];
};

// ===== Validation Methods =====
/**
 * <p>
 * Validates that at least one test type is selected.
 * <p>
 * Returns a rejected promise if no test types are selected.
 * @returns Promise that resolves if validation passes, rejects otherwise
 */
const validateTestTypeSelection = () => {
  if (!taskConfiguration.activeKey.length) {
    // eslint-disable-next-line prefer-promise-reject-errors
    return Promise.reject(t('commonComp.createTaskTestModal.validation.testTypeRequired'));
  }
  return Promise.resolve();
};

/**
 * <p>
 * Validates all active test forms.
 * <p>
 * Checks each active test form and opens the corresponding section if validation fails.
 * @returns True if all validations pass, false otherwise
 */
const validateAllTestForms = () => {
  let isValid = true;

  if (performanceFormRef.value) {
    if (!performanceFormRef.value.validate()) {
      formSectionToggleConfig.performance = true;
      isValid = false;
    }
  }

  if (functionalFormRef.value) {
    if (!functionalFormRef.value.validate()) {
      formSectionToggleConfig.functional = true;
      isValid = false;
    }
  }

  if (stabilityFormRef.value) {
    if (!stabilityFormRef.value.validate()) {
      formSectionToggleConfig.stability = true;
      isValid = false;
    }
  }

  return isValid;
};

// ===== Event Handlers =====
/**
 * <p>
 * Handles the OK button click event.
 * <p>
 * Validates all forms, submits the data to the API, and shows success notification.
 * Resets the form and closes the modal on successful submission.
 */
const handleConfirm = async () => {
  const isFormValid = validateAllTestForms();
  sprintFormRef.value.validate();
  if (!isFormValid) {
    return;
  }

  if (!taskConfiguration.id) {
    return;
  }

  isConfirmLoading.value = true;
  const [error] = await http.put(apiEndpointUrl.value, computedFormData.value);
  isConfirmLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('commonComp.createTaskTestModal.generateSuccess'));
  handleCancel();
};

/**
 * <p>
 * Handles the Cancel button click event.
 * <p>
 * Resets all form data to initial state and closes the modal.
 */
const handleCancel = () => {
  taskConfiguration.id = undefined;
  taskConfiguration.activeKey = [TestType.FUNCTIONAL];
  projectSelectRef.value?.clearAll();
  isConfirmLoading.value = false;

  // Reset all form data to initial state
  performanceTestForm.value = {
    deadlineDate: '',
    priority: Priority.MEDIUM,
    assigneeId: undefined,
    testType: TestType.PERFORMANCE
  };

  functionalTestForm.value = {
    deadlineDate: '',
    priority: Priority.MEDIUM,
    assigneeId: undefined,
    testType: TestType.FUNCTIONAL
  };

  stabilityTestForm.value = {
    deadlineDate: '',
    priority: Priority.MEDIUM,
    assigneeId: undefined,
    testType: TestType.STABILITY
  };

  emit('update:visible', false);
  emit('update:id', undefined);
};

/**
 * <p>
 * Resets form validation state.
 * <p>
 * Clears any validation errors from the sprint form.
 */
const resetFormValidation = () => {
  sprintFormRef.value?.clearValidate();
};

// ===== Lifecycle Hooks =====
onMounted(() => {
  // Watch for modal visibility changes
  watch(() => props.visible, (isVisible) => {
    if (!isVisible) {
      resetFormValidation();
      return;
    }

    // Reset form data when modal opens
    setTimeout(() => {
      functionalFormRef.value?.reset();
      performanceFormRef.value?.reset();
      stabilityFormRef.value?.reset();
    });
  }, { immediate: true });

  // Watch for project ID changes and load members
  watch(() => projectInfo.value, () => {
    if (!projectMembers.value.length && projectInfo.value.id) {
      loadProjectMembers();
    }
  }, { immediate: true });
});
</script>
<template>
  <Modal
    class="create-task-modal-container"
    :title="t('commonComp.createTaskTestModal.title')"
    :width="1200"
    :visible="visible"
    :confirmLoading="isConfirmLoading"
    @ok="handleConfirm"
    @cancel="handleCancel">
    <!-- Information prompt area -->
    <div v-if="!!infoText" class="info-section">
      <Icon class="info-icon" icon="icon-tishi1" />
      <span class="info-text">{{ props.infoText }}</span>
    </div>

    <!-- Basic configuration area -->
    <div class="config-section">
      <h3 class="section-title">{{ t('commonComp.createTaskTestModal.basicConfig') }}</h3>
      <Form
        ref="sprintFormRef"
        :model="taskConfiguration"
        class="config-form">
        <div v-if="projectInfo.type?.value === ProjectType.AGILE" class="form-row">
          <FormItem
            :required="projectInfo.type?.value === ProjectType.AGILE"
            :label="t('commonComp.createTaskTestModal.taskSprint')"
            name="id"
            class="form-item-sprint">
            <Select
              v-model:value="taskConfiguration.id"
              :disabled="!projectInfo.id"
              :action="`${TESTER}/task/sprint?projectId=${projectInfo.id}`"
              :fieldNames="{ value: 'id', label: 'name' }"
              :placeholder="t('commonComp.createTaskTestModal.selectSprint')">
              <template #option="record">
                <div class="sprint-option">
                  <Icon icon="icon-jihua" class="sprint-icon" />
                  <span class="sprint-name">{{ record.name }}</span>
                </div>
              </template>
            </Select>
          </FormItem>
        </div>
        <div class="form-row">
          <FormItem
            :label="t('commonComp.createTaskTestModal.testType')"
            name="activeKey"
            :rules="{ required: true, validator: validateTestTypeSelection, message: t('commonComp.createTaskTestModal.validation.testTypeRequired') }"
            class="form-item-test-type">
            <CheckboxGroup v-model:value="taskConfiguration.activeKey" :options="testTypeOptions" />
          </FormItem>
        </div>
      </Form>
    </div>

    <!-- Test configuration area -->
    <div v-if="taskConfiguration.activeKey.length" class="test-config-section">
      <h3 class="section-title">{{ t('commonComp.createTaskTestModal.testTaskConfig') }}</h3>
      <div class="test-forms-container">
        <Toggle
          v-if="taskConfiguration.activeKey.includes(TestType.FUNCTIONAL)"
          v-model:open="formSectionToggleConfig.functional"
          class="test-form-toggle"
          :title="t('commonComp.createTaskTestModal.functionalTestConfig')">
          <TestForm
            ref="functionalFormRef"
            :key="TestType.FUNCTIONAL"
            v-model:value="functionalTestForm"
            :type="props.type"
            :users="projectMembers"
            class="test-form-content" />
        </Toggle>

        <Toggle
          v-if="taskConfiguration.activeKey.includes(TestType.PERFORMANCE)"
          v-model:open="formSectionToggleConfig.performance"
          class="test-form-toggle"
          :title="t('commonComp.createTaskTestModal.performanceTestConfig')">
          <TestForm
            ref="performanceFormRef"
            :key="TestType.PERFORMANCE"
            v-model:value="performanceTestForm"
            :type="props.type"
            :users="projectMembers"
            class="test-form-content" />
        </Toggle>

        <Toggle
          v-if="taskConfiguration.activeKey.includes(TestType.STABILITY)"
          v-model:open="formSectionToggleConfig.stability"
          class="test-form-toggle"
          :title="t('commonComp.createTaskTestModal.stabilityTestConfig')">
          <TestForm
            ref="stabilityFormRef"
            :key="TestType.STABILITY"
            v-model:value="stabilityTestForm"
            :type="props.type"
            :users="projectMembers"
            class="test-form-content" />
        </Toggle>
      </div>
    </div>
  </Modal>
</template>

<style>
.ant-modal-wrap .ant-modal.create-task-modal-container .ant-modal-content .ant-modal-body {
  max-height: 85vh;
  padding: 24px;
  overflow-y: auto;
}
</style>

<style scoped>
/* Information prompt area */
.info-section {
  display: flex;
  align-items: flex-start;
  padding: 12px 16px;
  margin-bottom: 24px;
  background-color: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 6px;
}

.info-icon {
  color: #fa8c16;
  font-size: 16px;
  margin-right: 8px;
  margin-top: 2px;
  flex-shrink: 0;
}

.info-text {
  font-size: 12px;
  color: #d46b08;
  line-height: 1.5;
}

/* Configuration area */
.config-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.config-form {
  background-color: #fafafa;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
}

.form-row {
  margin-bottom: 16px;
}

.form-row:last-child {
  margin-bottom: 0;
}

.form-item-sprint {
  margin-bottom: 0;
}

.form-item-test-type {
  margin-bottom: 0;
}

.sprint-option {
  display: flex;
  align-items: center;
  padding: 4px 0;
}

.sprint-icon {
  font-size: 14px;
  color: #1890ff;
  margin-right: 8px;
}

.sprint-name {
  font-size: 12px;
  color: #262626;
}

/* Test configuration area */
.test-config-section {
  margin-top: 24px;
}

.test-forms-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.test-form-toggle {
  background-color: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  overflow: hidden;
}

.test-form-content {
  padding: 16px;
  background-color: #fff;
}

/* Deep style overrides */
:deep(.toggle-title) {
  font-size: 12px;
  font-weight: 600;
  color: #262626;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  margin: 0;
}

:deep(.ant-radio-wrapper) {
  margin-right: 16px;
  font-size: 12px;
}

:deep(.ant-checkbox-wrapper) {
  margin-right: 16px;
  font-size: 12px;
}

:deep(.ant-form-item-label > label) {
  font-size: 12px;
  font-weight: 500;
  color: #262626;
}

:deep(.ant-form-item-label > label.ant-form-item-required:not(.ant-form-item-required-mark-optional)::before) {
  color: #ff4d4f;
}

:deep(.ant-select-selector) {
  font-size: 12px;
}

:deep(.ant-select-selection-placeholder) {
  font-size: 12px;
  color: #bfbfbf;
}

:deep(.ant-input) {
  font-size: 12px;
}

:deep(.ant-picker) {
  font-size: 12px;
}

:deep(.ant-select-dropdown) {
  font-size: 12px;
}

/* Responsive design */
@media (max-width: 1200px) {
  .test-forms-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .config-form {
    padding: 12px;
  }

  .test-form-content {
    padding: 12px;
  }

  .info-section {
    padding: 8px 12px;
  }
}
</style>
