<script setup lang="ts">
import {computed, defineAsyncComponent, inject, onMounted, reactive, ref, Ref, watch} from 'vue';
import {useI18n} from 'vue-i18n';
import {CombinedTargetType, http, Priority, TESTER} from '@xcan-angus/infra';
import {CheckboxGroup, Form, FormItem} from 'ant-design-vue';
import {Icon, Modal, notification, Select, Toggle} from '@xcan-angus/vue-ui';
import {project} from '@/api/tester';
import {TestType} from '@/enums/enums';
import {FormData} from './types';

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
const projectId = inject<Ref<string>>('projectId', ref(''));
const projectMembers = ref([]);

// Form references for validation
const sprintFormRef = ref();
const projectSelectRef = ref();
const performanceFormRef = ref();
const functionalFormRef = ref();
const stabilityFormRef = ref();

// Sprint configuration data
const sprintConfiguration = reactive({
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
  if (sprintConfiguration.activeKey.includes(TestType.PERFORMANCE)) {
    result.push(performanceTestForm.value);
  }

  if (sprintConfiguration.activeKey.includes(TestType.FUNCTIONAL)) {
    result.push(functionalTestForm.value);
  }

  if (sprintConfiguration.activeKey.includes(TestType.STABILITY)) {
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
      return `${TESTER}/apis/${props.id}/test/task/generate?taskSprintId=${sprintConfiguration.id}`;
    case CombinedTargetType.SERVICE:
      return `${TESTER}/services/${props.id}/test/task/generate?taskSprintId=${sprintConfiguration.id}`;
    case CombinedTargetType.SCENARIO:
      return `${TESTER}/scenario/${props.id}/test/task/generate?taskSprintId=${sprintConfiguration.id}`;
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
  const [error, { data }] = await project.getProjectMember(projectId.value);
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
  if (!sprintConfiguration.activeKey.length) {
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

  if (!sprintConfiguration.id) {
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
  sprintConfiguration.id = undefined;
  sprintConfiguration.activeKey = [TestType.FUNCTIONAL];
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
  watch(() => projectId.value, () => {
    if (!projectMembers.value.length && projectId.value) {
      loadProjectMembers();
    }
  }, { immediate: true });
});
</script>
<template>
  <Modal
    class="create-task-modal-container"
    :title="t('commonComp.createTaskTestModal.title')"
    :width="1000"
    :visible="visible"
    :confirmLoading="isConfirmLoading"
    @ok="handleConfirm"
    @cancel="handleCancel">
    <div v-if="!!infoText" class="mt-3 mb-5 text-3 text-theme-content leading-5">
      <Icon class="text-status-warn text-3.5 -mt-1 mr-1" icon="icon-tishi1" />
      <span>{{ props.infoText }}</span>
    </div>

    <Form
      ref="sprintFormRef"
      :model="sprintConfiguration"
      class="my-5">
      <FormItem
        required
        :label="t('commonComp.createTaskTestModal.taskSprint')"
        name="id"
        :labelCol="{ style: { lineHeight: '28px' } }"
        class="flex-1">
        <Select
          v-model:value="sprintConfiguration.id"
          :disabled="!projectId"
          :action="`${TESTER}/task/sprint?projectId=${projectId}`"
          :fieldNames="{ value: 'id', label: 'name' }"
          :placeholder="t('commonComp.createTaskTestModal.selectSprint')">
          <template #option="record">
            <div class="flex items-center truncate">
              <Icon icon="icon-jihua" class="text-4" />
              <span class="ml-1">{{ record.name }}</span>
            </div>
          </template>
        </Select>
      </FormItem>
      <FormItem
        :label="t('commonComp.createTaskTestModal.testType')"
        name="activeKey"
        :rules="{ required: true, validator: validateTestTypeSelection, message: t('commonComp.createTaskTestModal.validation.testTypeRequired') }">
        <CheckboxGroup v-model:value="sprintConfiguration.activeKey" :options="testTypeOptions" />
      </FormItem>
    </Form>

    <div class="space-x-5 flex">
      <Toggle
        v-if="sprintConfiguration.activeKey.includes(TestType.FUNCTIONAL)"
        v-model:open="formSectionToggleConfig.functional"
        class="w-1/3"
        :title="t('commonComp.createTaskTestModal.functionalTestConfig')">
        <TestForm
          ref="functionalFormRef"
          :key="TestType.FUNCTIONAL"
          v-model:value="functionalTestForm"
          :type="props.type"
          :users="projectMembers"
          class="flex-1 mt-2" />
      </Toggle>

      <Toggle
        v-if="sprintConfiguration.activeKey.includes(TestType.PERFORMANCE)"
        v-model:open="formSectionToggleConfig.performance"
        class="w-1/3"
        :title="t('commonComp.createTaskTestModal.performanceTestConfig')">
        <TestForm
          ref="performanceFormRef"
          :key="TestType.PERFORMANCE"
          v-model:value="performanceTestForm"
          :type="props.type"
          :users="projectMembers"
          class="flex-1 mt-2" />
      </Toggle>

      <Toggle
        v-if="sprintConfiguration.activeKey.includes(TestType.STABILITY)"
        v-model:open="formSectionToggleConfig.stability"
        class="w-1/3"
        :title="t('commonComp.createTaskTestModal.stabilityTestConfig')">
        <TestForm
          ref="stabilityFormRef"
          :key="TestType.STABILITY"
          v-model:value="stabilityTestForm"
          :type="props.type"
          :users="projectMembers"
          class="flex-1 mt-2" />
      </Toggle>
    </div>
  </Modal>
</template>

<style>
.ant-modal-wrap .ant-modal.create-task-modal-container .ant-modal-content .ant-modal-body {
  max-height: 80vh;
  padding-top: 0;
  padding-bottom: 20px;
  overflow-y: auto;
}
</style>

<style scoped>
:deep(.toggle-title) {
  font-size: 12px;
}

:deep(.ant-radio-wrapper) {
  margin-right: 0;
}

:deep(span.ant-radio + *) {
  padding-right: 0;
}
</style>
