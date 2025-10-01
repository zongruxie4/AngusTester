<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, reactive, ref, watch, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Form, FormItem, Popover, TreeSelect, Upload } from 'ant-design-vue';
import type { Rule } from 'ant-design-vue/es/form';
import {
  AsyncComponent, DatePicker, Icon, IconTask, IconText, Input,
  Modal, notification, Select, SelectUser, Tooltip
} from '@xcan-angus/vue-ui';
import { EvalWorkloadMethod, localStore, Priority, TESTER, upload } from '@xcan-angus/infra';
import dayjs, { Dayjs } from 'dayjs';
import { cloneDeep, isEqual } from 'lodash-es';
import { modules, task } from '@/api/tester';
import { DATE_TIME_FORMAT, TIME_FORMAT } from '@/utils/constant';
import { BugLevel, SoftwareVersionStatus, TaskType, TestType } from '@/enums/enums';
import { TaskEditState } from '@/views/issue/issue/list/types';

import TaskPriority from '@/components/TaskPriority/index.vue';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import { TaskDetail, getTaskTypeName } from '../types';

// Async Components
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

// Component Props & Emits
const props = withDefaults(defineProps<TaskEditState>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  sprintId: undefined,
  visible: false,
  taskId: undefined,
  taskType: undefined,
  assigneeId: undefined,
  confirmerId: undefined,
  moduleId: undefined,
  parentTaskId: undefined,
  name: undefined,
  description: undefined,
  refCaseIds: () => []
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'update:taskId', value: string | undefined): void;
  (e: 'ok', value?: Partial<TaskDetail>): void;
}>();

const { t } = useI18n();

// Template Refs
const formRef = ref();
const richEditorRef = ref();

// Reactive State Variables
const isLoading = ref<boolean>(false);
const isZoomedIn = ref(false);
const isEditorVisible = ref(false);
const formKey = ref(0); // Force form re-render

const currentEvalWorkloadMethod = ref<EvalWorkloadMethod>(EvalWorkloadMethod.STORY_POINT);
const sprintDeadlineDate = ref<string>();

// Module Management
const moduleTreeData = ref([]);
/**
 * <p>Load module tree data</p>
 * <p>Fetches module tree structure for the current project</p>
 */
const loadModuleTreeData = async () => {
  if (!props.projectId) {
    return;
  }
  const [error, { data }] = await modules.getModuleTree({
    projectId: props.projectId
  });
  if (error) {
    return;
  }
  moduleTreeData.value = data || [];
};

// Form State Management
let previousFormState: TaskEditState | undefined;
const formState = reactive<TaskEditState>({
  projectId: undefined,
  assigneeId: undefined,
  attachments: undefined,
  confirmerId: undefined,
  deadlineDate: undefined,
  description: undefined,
  evalWorkload: undefined,
  actualWorkload: undefined,
  name: undefined,
  priority: Priority.MEDIUM,
  parentTaskId: undefined,
  sprintId: undefined,
  moduleId: undefined,
  tagIds: undefined,
  refTaskIds: undefined,
  refCaseIds: undefined,
  targetId: undefined,
  targetParentId: undefined,
  taskType: TaskType.TASK,
  testType: TestType.FUNCTIONAL,
  bugLevel: BugLevel.MINOR,
  testerId: undefined,
  missingBug: false,
  softwareVersion: undefined
});

/** Task type name mapping for UI display */
const taskTypeName = getTaskTypeName();

// User Selection Default Options
const assigneeDefaultOptions = ref<{[key:string]:{fullName:string;id:string;}}>();
const confirmerDefaultOptions = ref<{[key:string]:{fullName:string;id:string;}}>();

// Computed Properties
const shouldShowContinueButton = computed(() => {
  return !props.taskId && !props.taskType;
});

/**
 * <p>Toggle modal zoom state</p>
 * <p>Switches between normal and full-screen modal view</p>
 */
const toggleModalZoom = () => {
  isZoomedIn.value = !isZoomedIn.value;
  localStore.set(zoomInFlagCacheKey.value, isZoomedIn.value);
};

/**
 * <p>Handle sprint selection change</p>
 * <p>Updates deadline date and evaluation workload method when sprint changes</p>
 */
const handleSprintSelectionChange = (_sprintId: string, sprintInfo: TaskDetail) => {
  formState.deadlineDate = sprintInfo?.deadlineDate || '';
  sprintDeadlineDate.value = formState.deadlineDate;
  currentEvalWorkloadMethod.value = sprintInfo?.evalWorkloadMethod?.value;
};

/**
 * <p>Handle task type change</p>
 * <p>Resets target fields and sets tester for bug tasks</p>
 */
const handleTaskTypeChange = () => {
  formState.targetParentId = undefined;
  formState.targetId = undefined;
  if (!formState.testerId && formState.taskType === TaskType.BUG) {
    formState.testerId = currentUserId.value;
  }
};

/**
 * <p>Assign current user to specified role</p>
 * <p>Sets the current user as assignee, confirmer, or tester</p>
 */
const assignCurrentUserToRole = (roleKey:'assigneeId'|'confirmerId'|'testerId') => {
  if (roleKey === 'assigneeId') {
    formState.assigneeId = currentUserId.value;
    return;
  }
  if (roleKey === 'confirmerId') {
    formState.confirmerId = currentUserId.value;
  }
  if (roleKey === 'testerId') {
    formState.testerId = currentUserId.value;
  }
};

/**
 * <p>Validate evaluation workload field</p>
 * <p>Ensures evaluation workload is provided when actual workload is set</p>
 */
const validateEvaluationWorkload = async (_rule: Rule, value: string) => {
  if (!props.taskId) {
    return;
  }

  if (formState.actualWorkload) {
    if (!value) {
      return Promise.reject(new Error(t('backlog.messages.inputEvalWorkload')));
    }
    return Promise.resolve();
  }
  return Promise.resolve();
};

/**
 * <p>Handle actual workload change</p>
 * <p>Clears evaluation workload validation when actual workload is cleared</p>
 */
const handleActualWorkloadChange = (value: string) => {
  if (!value) {
    formRef.value.clearValidate('evalWorkload');
  }
};

/**
 * <p>Handle evaluation workload change</p>
 * <p>Clears actual workload and validation when evaluation workload is cleared</p>
 */
const handleEvaluationWorkloadChange = (value: string) => {
  if (!value) {
    formState.actualWorkload = undefined;
    formRef.value.clearValidate('evalWorkload');
  }
};

/**
 * <p>Validate deadline date</p>
 * <p>Ensures deadline is in the future and within sprint deadline</p>
 */
const validateDeadlineDate = async (_rule: Rule, value: string) => {
  if (dayjs(value).isBefore(dayjs(), 'minute')) {
    return Promise.reject(new Error(t('backlog.messages.deadlineMustBeFuture')));
  }

  if (sprintDeadlineDate.value) {
    if (dayjs(value).isAfter(dayjs(sprintDeadlineDate.value), 'seconds')) {
      return Promise.reject(new Error(t('backlog.editForm.messages.sprintDeadlineExceeded', { deadline: sprintDeadlineDate.value })));
    }
  }
  return Promise.resolve();
};

/**
 * <p>Disable past dates in date picker</p>
 * <p>Prevents selection of dates before today</p>
 */
const disablePastDates = (current: Dayjs) => {
  const today = dayjs().startOf('day');
  return current.isBefore(today, 'day');
};

/**
 * <p>Handle rich editor content change</p>
 * <p>Updates form state when description content changes</p>
 */
const handleRichEditorChange = (value: string) => {
  formState.description = value;
};

/**
 * <p>Handle rich editor loading state</p>
 * <p>Updates loading state when editor is loading</p>
 */
const handleRichEditorLoading = (isEditorLoading: boolean) => {
  isLoading.value = isEditorLoading;
};

/**
 * <p>Handle file upload</p>
 * <p>Uploads attachment files and adds them to the form state</p>
 */
const handleFileUpload = async function ({ file }: { file: File }) {
  if (!formState.attachments || formState.attachments.length >= 5 || isLoading.value) {
    return;
  }

  isLoading.value = true;
  const [error, { data = [] }] = await upload(file, { bizKey: 'angusTesterTaskAttachments' });
  isLoading.value = false;
  if (error) {
    return;
  }

  if (data && data.length > 0) {
    const attachments = data?.map(item => ({ name: item.name, url: item.url }));
    formState.attachments.push(...attachments);
  }
};

/**
 * <p>Delete attachment file</p>
 * <p>Removes attachment from the form state by index</p>
 */
const deleteAttachmentFile = (index: number) => {
  formState?.attachments?.splice(index, 1);
};

/**
 * <p>Validate form fields</p>
 * <p>Performs validation on required form fields</p>
 */
const validateFormFields = async () => {
  const requiredRuleKeys = [
    'name'
  ];

  if (formState.actualWorkload) {
    requiredRuleKeys.push('evalWorkload');
  }

  return new Promise((resolve) => {
    formRef.value.validate(requiredRuleKeys).then(async () => {
      return resolve(true);
    }).catch((errors:{errorFields:{errors:string[];name:string[];warnings:string;}[]}) => {
      if (errors.errorFields.length === 1) {
        const names = errors.errorFields[0].name;
        if (names.length === 1 && names[0] === 'deadlineDate') {
          return resolve(true);
        }
      }

      return resolve(false);
    });
  });
};

/**
 * <p>Build form parameters for API submission</p>
 * <p>Constructs the parameter object based on form state and task type</p>
 */
const buildFormParameters = () => {
  const params: TaskEditState = {
    projectId: props.projectId,
    sprintId: formState.sprintId,
    name: formState.name,
    taskType: formState.taskType,
    assigneeId: formState.assigneeId,
    deadlineDate: formState.deadlineDate,
    priority: formState.priority,
    parentTaskId: formState.parentTaskId,
    testerId: formState.testerId,
    softwareVersion: formState.softwareVersion
  };

  if (props.parentTaskId) {
    params.parentTaskId = props.parentTaskId;
  }

  if (formState.confirmerId) {
    params.confirmerId = formState.confirmerId;
  }

  if (formState.moduleId && +formState.moduleId > 0) {
    params.moduleId = formState.moduleId;
  }

  if (formState.tagIds?.length) {
    params.tagIds = formState.tagIds;
  }

  if (formState.refTaskIds?.length) {
    params.refTaskIds = formState.refTaskIds;
  }

  if (formState.refCaseIds?.length) {
    params.refCaseIds = formState.refCaseIds;
  }

  if (formState.description) {
    params.description = formState.description;
  }

  if (formState.attachments?.length) {
    params.attachments = formState.attachments;
  }

  if (formState.evalWorkload) {
    params.evalWorkload = formState.evalWorkload;
  }

  if (props.taskId && formState.actualWorkload) {
    params.actualWorkload = formState.actualWorkload;
  }

  if (formState.taskType === TaskType.BUG) {
    params.bugLevel = formState.bugLevel;
    params.missingBug = formState.missingBug;
  }

  if (formState.taskType === TaskType.API_TEST) {
    params.testType = formState.testType;
    params.targetId = formState.targetId;
    params.targetParentId = formState.targetParentId;
  }

  if (formState.taskType === TaskType.SCENARIO_TEST) {
    params.testType = formState.testType;
    params.targetId = formState.targetId;
  }

  return params;
};

/**
 * <p>Submit form data</p>
 * <p>Handles form submission for both create and edit operations</p>
 */
const submitForm = async (shouldContinue: boolean) => {
  if (props.taskId) {
    const hasFormChanged = isEqual(previousFormState, formState);
    if (hasFormChanged) {
      emit('update:visible', false);
      return;
    }
  }

  const isDescriptionValid = !formState.description || formState.description?.length <= 8000;
  const isFormValid = await validateFormFields();
  if (!isFormValid || !isDescriptionValid) {
    return;
  }

  if (!props.taskId) {
    handleTaskCreation(shouldContinue);
    return;
  }

  handleTaskUpdate();
};

/**
 * <p>Handle task creation</p>
 * <p>Creates a new task and handles success/error responses</p>
 */
const handleTaskCreation = async (shouldContinue = false) => {
  isLoading.value = true;
  const params = buildFormParameters();
  const [error, res] = await task.addTask(params);
  isLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('backlog.editForm.messages.taskAddedSuccess'));
  emit('ok', res?.data);

  if (!shouldContinue) {
    cancelModal();
  }
};

/**
 * <p>Handle task update</p>
 * <p>Updates an existing task and handles success/error responses</p>
 */
const handleTaskUpdate = async () => {
  isLoading.value = true;
  const params = buildFormParameters();
  const [error] = await task.putTask(props.taskId!, params);
  isLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('backlog.editForm.messages.taskEditedSuccess'));
  const taskData = await fetchTaskDetails();
  emit('ok', taskData);
  cancelModal();
};

/**
 * <p>Cancel modal and reset state</p>
 * <p>Closes the modal and clears task ID</p>
 */
const cancelModal = () => {
  emit('update:taskId', undefined);
  emit('update:visible', false);
};

/**
 * <p>Load task details</p>
 * <p>Fetches complete task information for editing</p>
 */
const fetchTaskDetails = async (): Promise<Partial<TaskDetail>> => {
  isLoading.value = true;
  console.log('🔍 Fetching task details for taskId:', props.taskId);
  const [error, res] = await task.getTaskDetail(props.taskId!);
  isLoading.value = false;

  if (error) {
    console.error('Error fetching task details:', error);
    return { id: props.taskId! };
  }

  if (!res?.data) {
    console.warn('No data returned from task.getTaskDetail');
    return { id: props.taskId! };
  }

  console.log('Task details loaded successfully:', res.data);
  return res.data;
};

/**
 * <p>Initialize component state</p>
 * <p>Sets up initial component state from local storage</p>
 */
const initializeComponent = () => {
  isZoomedIn.value = !!localStore.get(zoomInFlagCacheKey.value);
};

/**
 * <p>Reset form to default values</p>
 * <p>Initializes form state with default values for new task creation</p>
 */
const resetFormToDefaults = () => {
  isLoading.value = false;
  isEditorVisible.value = false;
  currentEvalWorkloadMethod.value = EvalWorkloadMethod.STORY_POINT;
  sprintDeadlineDate.value = undefined;

  previousFormState = undefined;
  formState.attachments = [];
  formState.deadlineDate = dayjs().add(1, 'day').format(DATE_TIME_FORMAT);
  if (dayjs(formState.deadlineDate).hour() > 19 || dayjs(formState.deadlineDate).hour() < 8) {
    formState.deadlineDate = dayjs(formState.deadlineDate).add(12, 'hour').format(DATE_TIME_FORMAT);
  }
  // Handle description data format for RichEditor
  if (props.description) {
    try {
      // Try to parse as JSON first (for RichEditor format)
      JSON.parse(props.description);
      formState.description = props.description;
    } catch {
      // If not JSON, wrap in RichEditor format
      formState.description = JSON.stringify([{ insert: props.description }]);
    }
  } else {
    formState.description = '';
  }
  formState.evalWorkload = undefined;
  formState.actualWorkload = undefined
  formState.name = props.name || '';
  formState.priority = Priority.MEDIUM;
  formState.bugLevel = BugLevel.MINOR;
  formState.parentTaskId = undefined;
  formState.sprintId = props.sprintId;
  formState.tagIds = [];
  formState.refTaskIds = [];
  formState.refCaseIds = props.refCaseIds || [];
  formState.targetId = undefined;
  formState.targetParentId = undefined;
  formState.taskType = props.taskType || TaskType.TASK;
  formState.testType = TestType.FUNCTIONAL;
  formState.missingBug = false;
  formState.softwareVersion = undefined;
  formState.assigneeId = props.assigneeId || props.userInfo?.id || undefined;
  formState.testerId = props.taskType === TaskType.BUG ? props.userInfo?.id : undefined;
  formState.confirmerId = props.confirmerId || undefined;
  if (props.moduleId && +props.moduleId > 0) {
    formState.moduleId = props.moduleId;
  } else {
    formState.moduleId = undefined;
  }
};

// Lifecycle Hooks
onMounted(() => {
  initializeComponent();

  watch(() => props.visible, async () => {
    if (props.visible) {
      await loadModuleTreeData();

      if (!props.taskId) {
        resetFormToDefaults();
        // Clear validation after resetting form
        if (typeof formRef.value?.clearValidate === 'function') {
          await formRef.value.clearValidate();
        }
        return;
      }

      // Set editor visible before loading data to ensure proper rendering
      isEditorVisible.value = true;

      const taskData = await fetchTaskDetails();
      if (!taskData) {
        resetFormToDefaults();
        return;
      }

      // Set assignee information
      const assigneeId = taskData.assigneeId;
      if (assigneeId) {
        formState.assigneeId = assigneeId;
        assigneeDefaultOptions.value = {
          [assigneeId]: {
            fullName: taskData.assigneeName || '',
            id: assigneeId
          }
        };
      }

      // Set confirmer information
      const confirmerId = taskData.confirmerId;
      if (confirmerId) {
        formState.confirmerId = confirmerId;
        confirmerDefaultOptions.value = {
          [confirmerId]: {
            fullName: taskData.confirmerName || '',
            id: confirmerId
          }
        };
      }

      // Populate form with task data
      console.log('📝 Populating form with task data...');
      formState.attachments = taskData.attachments || [];
      formState.moduleId = taskData.moduleId ? (+taskData.moduleId < 0 ? undefined : taskData.moduleId) : undefined;
      formState.deadlineDate = taskData.deadlineDate;
      // Handle description data format for RichEditor
      if (taskData.description) {
        try {
          // Try to parse as JSON first (for RichEditor format)
          JSON.parse(taskData.description);
          formState.description = taskData.description;
        } catch {
          // If not JSON, wrap in RichEditor format
          formState.description = JSON.stringify([{ insert: taskData.description }]);
        }
      } else {
        formState.description = '';
      }
      formState.evalWorkload = taskData.evalWorkload;
      formState.actualWorkload = taskData.actualWorkload;
      formState.name = taskData.name;
      formState.priority = taskData.priority?.value || Priority.MEDIUM;
      formState.parentTaskId = taskData.parentTaskId;
      formState.sprintId = taskData.sprintId;
      formState.tagIds = taskData.tags?.map(item => item.id);
      formState.refTaskIds = taskData.refTaskInfos?.map(item => item.id);
      formState.refCaseIds = taskData.refCaseInfos?.map(item => item.id);
      formState.targetId = taskData.targetId;
      formState.taskType = taskData.taskType?.value || TaskType.TASK;
      formState.testType = taskData.testType?.value;
      formState.targetParentId = taskData.targetParentId;
      formState.testerId = taskData.testerId;
      formState.missingBug = taskData.missingBug || false;
      formState.bugLevel = taskData.bugLevel?.value || BugLevel.MINOR;
      formState.softwareVersion = taskData.softwareVersion;

      console.log('📋 Form state after population:', {
        name: formState.name,
        description: formState.description,
        taskType: formState.taskType,
        priority: formState.priority,
        assigneeId: formState.assigneeId,
        sprintId: formState.sprintId
      });

      previousFormState = cloneDeep(formState);

      currentEvalWorkloadMethod.value = taskData.evalWorkloadMethod?.value || EvalWorkloadMethod.STORY_POINT;

      // Force form re-render to ensure data binding
      formKey.value++;

      // Wait for DOM to update before clearing validation
      nextTick(() => {
        // Clear validation after DOM update
        if (typeof formRef.value?.clearValidate === 'function') {
          formRef.value.clearValidate();
        }
      });
    } else {
      // Reset editor visibility when modal is closed
      isEditorVisible.value = false;
    }
  }, { immediate: true });

  // Watch formState changes for debugging
  watch(() => formState, (newState) => {
    console.log('🔄 Form state changed:', {
      name: newState.name,
      description: newState.description,
      taskType: newState.taskType,
      priority: newState.priority
    });
  }, { deep: true });
});

/**
 * <p>Exclude task types from selection</p>
 * <p>Filters out certain task types based on current task state</p>
 */
const getExcludedTaskTypes = (data: { value: TaskDetail['taskType']['value']; message: string }) => {
  const value = data.value;
  const currentTaskType = formState.taskType;
  if (props.taskId) {
    if (currentTaskType === TaskType.API_TEST) {
      return value !== TaskType.API_TEST;
    }
    if (currentTaskType === TaskType.SCENARIO_TEST) {
      return value !== TaskType.SCENARIO_TEST;
    }
    return [TaskType.API_TEST, TaskType.SCENARIO_TEST].includes(value);
  }
  return false;
};

/**
 * <p>Exclude current task from parent task selection</p>
 * <p>Prevents selecting the current task as its own parent</p>
 */
const getExcludedTaskIds = (data: { id: string }) => {
  return props.taskId === data.id;
};

// Computed Properties
const currentUserId = computed(() => {
  return props.userInfo?.id;
});

const modalTitle = computed(() => {
  if (props.taskId) {
    return t('actions.edit');
  }
  return t('common.add');
});

const isTaskTypeReadonly = computed(() => {
  return (
    props.taskId && (formState.taskType === TaskType.API_TEST || formState.taskType === TaskType.SCENARIO_TEST)
  ) || !!props.taskType;
});

const shouldShowEditor = computed(() => {
  return props.visible && (isEditorVisible.value || !props.taskId);
});

const shouldShowTestType = computed(() => {
  const taskType = formState.taskType;
  if (!taskType) {
    return false;
  }
  return [TaskType.API_TEST, TaskType.SCENARIO_TEST].includes(taskType);
});

// Description Validation
/**
 * <p>Validate description length</p>
 * <p>Ensures description does not exceed maximum length limit</p>
 */
const validateDescriptionLength = async () => {
  if (richEditorRef.value && richEditorRef.value.getLength() > 6000) {
    return Promise.reject(new Error(t('backlog.editForm.messages.descriptionMaxLength')));
  }
  return Promise.resolve();
};

// Style Computed Properties
const modalStyle = computed(() => {
  if (isZoomedIn.value) {
    return {
      width: '100%'
    };
  }
  return {
    width: '1000px'
  };
});

const formStyle = computed(() => {
  if (isZoomedIn.value) {
    return {
      height: '86vh'
    };
  }
  return {
    height: '75vh',
    minHeight: '665px'
  };
});

const zoomInFlagCacheKey = computed(() => {
  return `${props.userInfo?.id}${props?.projectId}${btoa('modalSize')}`;
});

/**
 * <p>Get popup container for dropdowns</p>
 * <p>Returns document body as popup container for better positioning</p>
 */
const getPopupContainer = () => {
  return document.body;
};
</script>
<template>
  <Modal
    :title="modalTitle"
    :centered="true"
    :style="modalStyle"
    :visible="props.visible"
    class="relative max-w-full"
    @cancel="cancelModal">
    <Tooltip :title="isZoomedIn ? t('backlog.zoomOut') : t('backlog.zoomIn')">
      <Icon
        :icon="isZoomedIn ? 'icon-tuichuzuida' : 'icon-zuidahua'"
        class="absolute right-10 top-3.5 text-3.5 cursor-pointer"
        @click="toggleModalZoom" />
    </Tooltip>

    <Form
      :key="formKey"
      ref="formRef"
      :model="formState"
      layout="vertical"
      class="overflow-y-auto overflow-x-hidden"
      size="small"
      :style="formStyle">
      <div class="flex">
        <div class="flex-1 pr-8">
          <FormItem
            name="name"
            :label="t('common.name')"
            :rules="{ required: true, message: t('backlog.messages.taskNameRequired') }">
            <Input
              v-model:value="formState.name"
              trim
              :maxlength="200"
              :placeholder="t('backlog.placeholder.taskName')" />
          </FormItem>

          <div class="flex space-x-4">
            <FormItem
              v-if="!!formState.taskType"
              name="taskType"
              class="flex-1/2"
              required>
              <template #label>
                {{ t('common.type') }}
                <Popover>
                  <template #content>
                    <div class="flex items-center leading-5">
                      <div class="space-y-2 flex-shrink-0">
                        <div
                          v-for="[taskType, typeName] in taskTypeName"
                          :key="taskType"
                          class="flex items-center">
                          <IconTask :value="taskType as TaskType" class="mr-1 text-4" />
                          <span>{{ typeName }}</span>
                        </div>
                      </div>
                      <div class="ml-3.5 space-y-2">
                        <div
                          v-for="[taskType, typeName] in taskTypeName"
                          :key="taskType">
                          {{ t(`backlog.taskTypeDescriptions.${taskType.toLowerCase()}`) }}
                        </div>
                      </div>
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips ml-1 cursor-pointer text-3.5" />
                </Popover>
              </template>

              <SelectEnum
                v-model:value="formState.taskType"
                :allowClear="false"
                :excludes="getExcludedTaskTypes"
                :readonly="isTaskTypeReadonly"
                internal
                enumKey="TaskType"
                :placeholder="t('backlog.editForm.placeholders.selectTaskType')"
                style="width: 280px;"
                @change="handleTaskTypeChange">
                <template #option="record">
                  <div class="flex items-center">
                    <IconTask :value="record.value" class="text-4 flex-shrink-0" />
                    <span class="ml-1">{{ record.label }}</span>
                  </div>
                </template>
              </SelectEnum>
            </FormItem>

            <FormItem
              name="priority"
              :label="t('common.priority')"
              class="flex-1/2"
              required>
              <SelectEnum
                v-model:value="formState.priority"
                :allowClear="false"
                internal
                enumKey="Priority"
                :placeholder="t('backlog.editForm.placeholders.selectPriority')">
                <template #option="record">
                  <TaskPriority :value="record" />
                </template>
              </SelectEnum>
            </FormItem>
          </div>

          <template v-if="formState.taskType === TaskType.BUG">
            <div class="flex space-x-4">
              <FormItem
                name="bugLevel"
                :label="t('backlog.editForm.labels.bugLevel')"
                class="flex-1/2">
                <SelectEnum
                  v-model:value="formState.bugLevel"
                  enumKey="BugLevel"
                  style="width: 280px;"
                  :allowClear="false"
                  :lazy="false" />
              </FormItem>

              <FormItem
                name="missingBug"
                :label="t('backlog.editForm.labels.missingBug')"
                class="flex-1/2">
                <Select
                  v-model:value="formState.missingBug"
                  :options="[{value: true, label: t('status.yes')}, {value: false, label: t('status.no')}]">
                </Select>
              </FormItem>
            </div>
          </template>

          <div v-if="formState.taskType === TaskType.SCENARIO_TEST" class="flex space-x-4">
            <FormItem
              name="targetId"
              :label="t('common.scenario')"
              class="flex-1 min-w-0"
              :rules="{ required: true, message: t('backlog.editForm.messages.selectScenario') }">
              <Select
                v-model:value="formState.targetId"
                showSearch
                internal
                :placeholder="t('backlog.editForm.placeholders.selectScenario')"
                :fieldNames="{ label: 'name', value: 'id' }"
                :action="`${TESTER}/scenario?projectId=${props.projectId}&fullTextSearch=true`"
                :readonly="!!props.taskId" />
            </FormItem>

            <FormItem
              v-if="shouldShowTestType"
              name="testType"
              :label="t('backlog.editForm.labels.testType')"
              class="flex-1"
              required>
              <SelectEnum
                v-model:value="formState.testType"
                :allowClear="false"
                internal
                enumKey="TestType"
                :placeholder="t('backlog.editForm.placeholders.selectTestType')"
                style="width: 280px;" />
            </FormItem>
          </div>

          <template v-if="formState.taskType === TaskType.API_TEST">
            <FormItem
              name="targetParentId"
              :label="t('common.service')"
              :rules="{ required: true, message: t('backlog.editForm.messages.selectService') }">
              <Select
                v-model:value="formState.targetParentId"
                :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
                :fieldNames="{ value: 'id', label: 'name' }"
                :readonly="!!props.taskId"
                :lazy="false"
                internal
                defaultActiveFirstOption
                showSearch
                :placeholder="t('backlog.editForm.placeholders.selectOrSearchService')">
                <template #option="record">
                  <div class="text-3 leading-3 flex items-center h-6.5">
                    <IconText
                      class="mr-1"
                      style="width:16px;height: 16px;"
                      :text="record.targetType?.value === 'PROJECT' ? 'P' : 'S'"
                      :class="record.targetType?.value === 'PROJECT' ? 'bg-blue-badge-p' : 'bg-blue-badge-s'" />
                    <div :title="record.name" class="truncate">{{ record.name }}</div>
                  </div>
                </template>
              </Select>
            </FormItem>

            <div class="flex space-x-4">
              <FormItem
                :label="t('common.api')"
                name="targetId"
                class="flex-1 min-w-0"
                :rules="{ required: true, message: t('backlog.editForm.messages.selectApi') }">
                <Select
                  v-model:value="formState.targetId"
                  showSearch
                  internal
                  :placeholder="t('backlog.editForm.placeholders.selectApi')"
                  :fieldNames="{ label: 'summary', value: 'id' }"
                  :action="`${TESTER}/apis?projectId=${props.projectId}&serviceId=${formState.targetParentId}&fullTextSearch=true`"
                  :readonly="!!props.taskId || !formState.targetParentId" />
              </FormItem>

              <FormItem
                v-if="shouldShowTestType"
                name="testType"
                :label="t('backlog.editForm.labels.testType')"
                class="flex-1"
                required>
                <SelectEnum
                  v-model:value="formState.testType"
                  :allowClear="false"
                  internal
                  enumKey="TestType"
                  :placeholder="t('backlog.editForm.placeholders.selectTestType')"
                  style="width: 280px;" />
              </FormItem>
            </div>
          </template>

          <div class="flex space-x-4">
            <FormItem
              name="assigneeId"
              class="flex-1/2"
              :rules="{ required: true, message: t('backlog.editForm.messages.selectAssignee') }">
              <template #label>
                {{ t('common.assignee') }}<Popover placement="rightTop">
                  <template #content>
                    <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                      {{ t('common.assignee') }}
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
                </Popover>
              </template>

              <div class="flex items-center ">
                <SelectUser
                  v-model:value="formState.assigneeId"
                  :placeholder="t('backlog.editForm.placeholders.selectAssignee')"
                  internal
                  class="flex-1 min-w-0"
                  :defaultOptions="assigneeDefaultOptions"
                  :action="`${TESTER}/project/${props.projectId}/member/user`"
                  :maxlength="80" />
                <Button
                  size="small"
                  type="link"
                  class="p-0 h-5 leading-5 ml-1"
                  @click="assignCurrentUserToRole('assigneeId')">
                  {{ t('common.assignToMe') }}
                </Button>
              </div>
            </FormItem>

            <FormItem name="confirmerId" class="flex-1/2">
              <template #label>
                {{ t('common.confirmer') }}<Popover placement="rightTop">
                  <template #content>
                    <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                      {{ t('common.confirmer') }}
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
                </Popover>
              </template>

              <div class="flex items-center">
                <SelectUser
                  v-model:value="formState.confirmerId"
                  :placeholder="t('backlog.editForm.placeholders.selectConfirmer')"
                  internal
                  allowClear
                  class="flex-1 min-w-0"
                  :defaultOptions="confirmerDefaultOptions"
                  :action="`${TESTER}/project/${props.projectId}/member/user`"
                  :maxlength="80" />
                <Button
                  size="small"
                  type="link"
                  class="p-0 h-5 leading-5 ml-1"
                  @click="assignCurrentUserToRole('confirmerId')">
                  {{ t('common.assignToMe') }}
                </Button>
              </div>
            </FormItem>
          </div>

          <div class="flex space-x-4">
            <FormItem
              :label="t('common.deadlineDate')"
              name="deadlineDate"
              class="flex-1/2"
              :rules="{ required: true, validator: validateDeadlineDate }">
              <DatePicker
                v-model:value="formState.deadlineDate"
                :showNow="false"
                :disabledDate="disablePastDates"
                :showTime="{ hideDisabledOptions: true, format: TIME_FORMAT }"
                type="date"
                size="small"
                showToday
                class="w-full" />
            </FormItem>

            <FormItem name="confirmerId" class="flex-1/2">
              <template #label>
                {{ t('common.tester') }}<Popover placement="rightTop">
                  <template #content>
                    <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                      {{ t('common.tester') }}
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
                </Popover>
              </template>

              <div class="flex items-center">
                <SelectUser
                  v-model:value="formState.testerId"
                  :placeholder="t('backlog.editForm.placeholders.selectTester')"
                  internal
                  allowClear
                  class="flex-1 min-w-0"
                  :defaultOptions="confirmerDefaultOptions"
                  :action="`${TESTER}/project/${props.projectId}/member/user`"
                  :maxlength="80" />
                <Button
                  size="small"
                  type="link"
                  class="p-0 h-5 leading-5 ml-1"
                  @click="assignCurrentUserToRole('testerId')">
                  {{ t('common.assignToMe') }}
                </Button>
              </div>
            </FormItem>
          </div>

          <FormItem
            name="description"
            :label="t('common.description')"
            :rules="{validator: validateDescriptionLength}">
            <AsyncComponent :visible="shouldShowEditor">
              <RichEditor
                ref="richEditorRef"
                :value="formState.description"
                :options="{placeholder: t('backlog.editForm.placeholders.taskDescription')}"
                :height="340"
                @change="handleRichEditorChange"
                @loadingChange="handleRichEditorLoading" />
            </AsyncComponent>
          </FormItem>
        </div>

        <div class="w-80  pl-2 border-l">
          <FormItem
            :label="t('common.sprint')"
            name="sprintId">
            <Select
              v-model:value="formState.sprintId"
              :action="`${TESTER}/task/sprint?projectId=${props.projectId}&fullTextSearch=true`"
              :fieldNames="{ value: 'id', label: 'name' }"
              :readonly="!!props.taskId"
              showSearch
              internal
              :placeholder="t('backlog.editForm.placeholders.selectOrSearchSprint')"
              @change="handleSprintSelectionChange">
              <template #option="record">
                <div class="flex items-center" :title="record.name">
                  <Icon icon="icon-jihua" class="mr-1 text-4" />
                  <div style="max-width: 220px;" class="truncate">{{ record.name }}</div>
                </div>
              </template>
            </Select>
          </FormItem>

          <FormItem :label="t('common.module')" name="moduleId">
            <TreeSelect
              v-model:value="formState.moduleId"
              :treeData="moduleTreeData"
              :fieldNames="{ value: 'id', label: 'name', children: 'children' }"
              :getPopupContainer="getPopupContainer"
              :virtual="false"
              size="small"
              showSearch
              allowClear
              :placeholder="t('backlog.editForm.placeholders.selectOrSearchModule')">
              <template #title="item">
                <div class="flex items-center" :title="item.name">
                  <Icon icon="icon-mokuai" class="mr-1 text-3.5" />
                  <div style="max-width: 220px;" class="truncate">{{ item.name }}</div>
                </div>
              </template>
            </TreeSelect>
          </FormItem>

          <FormItem :label="t('backlog.editForm.labels.parentTask')" name="parentTaskId">
            <Select
              v-if="!!props.parentTaskId"
              :readonly="true"
              :value="props.parentTaskId"
              :fieldNames="{ label: 'name', value: 'id' }"
              :action="`${TESTER}/task?projectId=${props.projectId}&fullTextSearch=true`">
              <template #option="record">
                <div class="flex items-center">
                  <IconTask :value="record.taskType?.value" class="text-4 flex-shrink-0" />
                  <span class="ml-1.5">{{ record.name }}</span>
                </div>
              </template>
            </Select>

            <Select
              v-else
              v-model:value="formState.parentTaskId"
              showSearch
              internal
              :placeholder="t('backlog.editForm.placeholders.selectParentTask')"
              :excludes="getExcludedTaskIds"
              :fieldNames="{ label: 'name', value: 'id' }"
              :action="`${TESTER}/task?projectId=${props.projectId}&fullTextSearch=true`">
              <template #option="record">
                <div class="flex items-center">
                  <IconTask :value="record.taskType?.value" class="text-4 flex-shrink-0" />
                  <span class="ml-1.5">{{ record.name }}</span>
                </div>
              </template>
            </Select>
          </FormItem>

          <FormItem
            name="evalWorkload"
            :rules="{ required: formState.actualWorkload, validator: validateEvaluationWorkload, trigger: 'change' }">
            <template #label>
              <span>
                {{ t('backlog.editForm.labels.evalWorkload') }}
              </span>
              <Popover placement="rightTop">
                <template #content>
                  <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                    {{ t('backlog.editForm.labels.evalWorkload') }}
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="text-tips ml-1 cursor-pointer text-3.5" />
              </Popover>
            </template>
            <Input
              v-model:value="formState.evalWorkload"
              size="small"
              dataType="float"
              trimAll
              :min="0.1"
              :max="1000"
              :placeholder="t('backlog.editForm.placeholders.workloadRange')"
              @blur="handleEvaluationWorkloadChange($event.target.value)" />
          </FormItem>

          <template v-if="!!props.taskId">
            <FormItem name="actualWorkload">
              <template #label>
                {{ t('backlog.editForm.labels.evalWorkload') }}
                <Popover placement="rightTop">
                  <template #content>
                    <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                      {{ t('backlog.editForm.labels.evalWorkload') }}
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips ml-1 cursor-pointer text-3.5" />
                </Popover>
              </template>
              <Input
                v-model:value="formState.actualWorkload"
                class="w-70"
                size="small"
                dataType="float"
                trimAll
                :placeholder="t('backlog.editForm.placeholders.workloadRange')"
                :min="0.1"
                :max="1000"
                @change="handleActualWorkloadChange($event.target.value)" />
            </FormItem>
          </template>

          <FormItem
            name="softwareVersion"
            :label="t('common.softwareVersion')">
            <Select
              v-model:value="formState.softwareVersion"
              allowClear
              :placeholder="t('backlog.editForm.placeholders.selectSoftwareVersion')"
              :action="`${TESTER}/software/version?projectId=${props.projectId}`"
              :params="{filters: [{value: [SoftwareVersionStatus.NOT_RELEASED, SoftwareVersionStatus.RELEASED], key: 'status', op: 'IN'}]}"
              :fieldNames="{value:'name', label: 'name'}">
            </Select>
          </FormItem>

          <FormItem
            name="tagIds"
            class="relative">
            <template #label>
              {{ t('common.tag') }}<Popover placement="rightTop">
                <template #content>
                  <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                    {{ t('common.tag') }}
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
              </Popover>
            </template>
            <Select
              v-model:value="formState.tagIds"
              showSearch
              internal
              :fieldNames="{ label: 'name', value: 'id' }"
              :maxTagCount="5"
              :maxTagTextLength="15"
              :maxTags="5"
              :allowClear="false"
              :action="`${TESTER}/tag?projectId=${props.projectId}&fullTextSearch=true`"
              :placeholder="t('backlog.editForm.placeholders.maxTags')"
              mode="multiple"
              :notFoundContent="t('backlog.editForm.messages.contactAdminForTags')" />
          </FormItem>

          <FormItem
            name="refTaskIds"
            :label="t('backlog.editForm.labels.refTasks')"
            class="relative">
            <Select
              v-model:value="formState.refTaskIds"
              showSearch
              internal
              :allowClear="false"
              :fieldNames="{ label: 'name', value: 'id' }"
              :maxTagCount="10"
              :maxTagTextLength="15"
              :maxTags="20"
              :action="`${TESTER}/task?projectId=${props.projectId}&fullTextSearch=true`"
              :placeholder="t('backlog.editForm.placeholders.maxRefTasks')"
              mode="multiple">
              <template #option="record">
                <div class="flex items-center leading-4.5 overflow-hidden">
                  <IconTask :value="record.taskType?.value" class="text-4 flex-shrink-0" />
                  <div class="link truncate ml-1" :title="record.name">
                    {{ record.name }}
                  </div>
                  <div
                    v-if="record.overdue"
                    class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2"
                    style="transform: scale(0.9);color: rgba(245, 34, 45, 100%);line-height: 16px;">
                    <span class="inline-block transform-gpu">{{ t('status.overdue') }}</span>
                  </div>
                </div>
              </template>
            </Select>
          </FormItem>

          <FormItem
            name="refCaseIds"
            :label="t('backlog.editForm.labels.refCases')"
            class="relative">
            <Select
              v-model:value="formState.refCaseIds"
              showSearch
              internal
              :allowClear="false"
              :fieldNames="{ label: 'name', value: 'id' }"
              :maxTagCount="10"
              :maxTagTextLength="15"
              :maxTags="20"
              :action="`${TESTER}/func/case?projectId=${props.projectId}&fullTextSearch=true`"
              :placeholder="t('backlog.editForm.placeholders.maxRefCases')"
              mode="multiple">
              <template #option="record">
                <div class="flex items-center leading-4.5 overflow-hidden">
                  <Icon icon="icon-gongnengyongli" class="text-4 flex-shrink-0" />
                  <div class="link truncate ml-1" :title="record.name">
                    {{ record.name }}
                  </div>
                  <div
                    v-if="record.overdue"
                    class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2"
                    style="transform: scale(0.9);color: rgba(245, 34, 45, 100%);line-height: 16px;">
                    <span class="inline-block transform-gpu">{{ t('status.overdue') }}</span>
                  </div>
                </div>
              </template>
            </Select>
          </FormItem>

          <FormItem :label="t('common.attachments')">
            <div
              style="height: 60px; border-color: rgba(0, 119, 255);background-color: rgba(0, 119, 255, 4%);"
              class="border border-dashed rounded flex flex-col px-2 py-1"
              :class="formState?.attachments?.length?'justify-between':'justify-center'">
              <template v-if="formState.attachments?.length">
                <div style="height: 286px;scrollbar-gutter: stable;" class="overflow-hidden hover:overflow-y-auto -mr-2 pr-1">
                  <div
                    v-for="(item,index) in formState.attachments"
                    :key="index"
                    :class="{'rounded-b':index===formState.attachments.length-1}"
                    class="flex items-center justify-between text-3 leading-3">
                    <div
                      :title="item.name"
                      class="truncate text-theme-sub-content leading-4 cursor-pointer"
                      style="width: 250px;">
                      {{ item.name }}
                    </div>
                    <Icon
                      icon="icon-qingchu"
                      class="text-theme-special text-theme-text-hover cursor-pointer flex-shrink-0 leading-4 text-3.5"
                      @click="deleteAttachmentFile(index)" />
                  </div>
                </div>

                <div v-if="formState.attachments.length < 5" class="flex justify-end">
                  <Upload
                    :fileList="[]"
                    name="file"
                    class="-mb-1 mr-1"
                    :customRequest="handleFileUpload">
                    <Icon icon="icon-shangchuan" class="text-theme-special mr-1" />
                    <span class="text-3 leading-3 text-theme-text-hover">
                      {{ t('backlog.editForm.buttons.continueUpload') }}
                    </span>
                  </Upload>
                </div>
              </template>

              <template v-else>
                <div class="flex justify-center">
                  <Upload
                    name="file"
                    :fileList="[]"
                    :customRequest="handleFileUpload">
                    <Icon icon="icon-shangchuan" class="mr-1 text-theme-special" />
                    <span class="text-3 text-theme-text-hover">
                      {{ t('backlog.editForm.buttons.uploadAttachments') }}
                    </span>
                  </Upload>
                </div>
              </template>
            </div>
          </FormItem>
        </div>
      </div>
    </Form>

    <template #footer>
      <Button
        class="text-3 leading-3"
        size="small"
        @click="cancelModal">
        {{ t('actions.cancel') }}
      </Button>
      <Button
        v-if="shouldShowContinueButton"
        type="primary"
        class="text-3 leading-3"
        size="small"
        :disabled="isLoading"
        @click="submitForm(true)">
        {{ t('actions.saveAndContinue') }}
      </Button>
      <Button
        type="primary"
        class="text-3 leading-3"
        size="small"
        :disabled="isLoading"
        @click="submitForm(false)">
        {{ t('common.confirm') }}
      </Button>
    </template>
  </Modal>
</template>

<style scoped>
:deep(.ant-form-item-label) {
  font-size: 12px;
  font-weight: 600;
}

:deep(.ant-form-item:not(.ant-form-item-has-error)) {
  margin-bottom: 15px;
}

:deep(.ant-form-item .ant-form-item-has-error) {
  margin-bottom: 15px;
}

:deep(.ant-form-item-with-help .ant-form-item-explain) {
  min-height: 20px;
}
</style>
