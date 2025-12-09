<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, reactive, ref, Ref, watch } from 'vue';
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
import { modules, issue } from '@/api/tester';
import { DATE_TIME_FORMAT, TIME_FORMAT, MAX_FILE_SIZE_MB, UPLOAD_ISSUE_FILE_KEY } from '@/utils/constant';
import { BugLevel, SoftwareVersionStatus, TaskType } from '@/enums/enums';

import { TaskEditState } from './types';
import { TaskDetail, getTaskTypeName } from '../../types';

import SelectEnum from '@/components/form/enum/SelectEnum.vue';
import TaskPriority from '@/components/task/TaskPriority.vue';

// Component props & emits
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap',
  ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true })
);
const RichEditor = defineAsyncComponent(() => import('@/components/editor/richEditor/index.vue'));
const DropdownTemplateSelect = defineAsyncComponent(() => import('@/components/test/DropdownTemplateSelect.vue'));

const props = withDefaults(defineProps<TaskEditState>(), {
  projectId: undefined,
  userInfo: undefined,
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

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'update:taskId', value: string | undefined): void;
  (e: 'ok', value: Partial<TaskDetail>, addFlag?: boolean): void;
}>();

// Composables
const { t } = useI18n();

// Reactive state
const formRef = ref();
const loading = ref<boolean>(false);
const zoomInFlag = ref(false);
const showEditorFlag = ref(false);
const evalWorkloadMethod = ref<EvalWorkloadMethod>(EvalWorkloadMethod.STORY_POINT);
const sprintDeadlineDate = ref<string>();
const moduleTreeData = ref([]);

// Default options for user selection components
const assigneeDefaultOptions = ref<{[key:string]:{fullName:string;id:number;}}>();
const confirmerDefaultOptions = ref<{[key:string]:{fullName:string;id:number;}}>();

// Store original form state for comparison during edit
let originalFormState: TaskEditState | undefined;

// Main form state containing all task data
const formState = reactive<TaskEditState>({
  projectId: props.projectId,
  assigneeId: undefined,
  attachments: [],
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
  tagIds: [],
  refTaskIds: [],
  refCaseIds: [],
  targetParentId: undefined,
  taskType: TaskType.TASK,
  bugLevel: BugLevel.MINOR,
  testerId: undefined,
  missingBug: false,
  softwareVersion: undefined
});

/** Task type name mapping for UI display */
const taskTypeName = getTaskTypeName();

/**
 * Determine if the "Save and Continue" button should be shown
 * Only shown when creating a new task without a specific task type
 */
const showContinue = computed(() => {
  return !props.taskId && !props.taskType;
});

/**
 * Get the current user's ID from props
 */
const currentUserId = computed(() => {
  return props.userInfo?.id;
});

/**
 * Get the modal title based on whether we're editing or creating a task
 */
const modalTitle = computed(() => {
  if (props.taskId) {
    return t('actions.edit');
  }
  return t('actions.add');
});

/**
 * Determine if the task type field should be readonly
 * Readonly when taskType is provided as prop
 */
const isTaskTypeReadonly = computed(() => {
  return !!props.taskType;
});

/**
 * Determine if the rich text editor should be shown
 * Shown when modal is visible and either editor flag is set or creating a new task
 */
const shouldShowEditor = computed(() => {
  return props.visible && (showEditorFlag.value || !props.taskId);
});


/**
 * Get modal style based on zoom state
 * Full width when zoomed in, fixed width when normal
 */
const modalStyle = computed(() => {
  if (zoomInFlag.value) {
    return {
      width: '100%'
    };
  }
  return {
    width: '1200px'
  };
});

/**
 * Get form style based on zoom state
 * Larger height when zoomed in, standard height when normal
 */
const formStyle = computed(() => {
  if (zoomInFlag.value) {
    return {
      height: '86vh'
    };
  }
  return {
    height: '76vh',
    minHeight: '670px'
  };
});

const softwareVersionParams = {
  filters: [{ value: [SoftwareVersionStatus.NOT_RELEASED, SoftwareVersionStatus.RELEASED], key: 'status', op: 'IN' }]
};

/**
 * Generate cache key for storing zoom state in local storage
 * Unique per user and project combination
 */
const zoomInFlagCacheKey = computed(() => {
  return `${props.userInfo?.id}${props?.projectId}${btoa('modalSize')}`;
});

/**
 * Toggle modal zoom state and persist to local storage
 */
const toggleModalZoom = () => {
  zoomInFlag.value = !zoomInFlag.value;
  localStore.set(zoomInFlagCacheKey.value, zoomInFlag.value);
};

/**
 * Handle sprint selection change and update deadline accordingly
 * @param _id - Sprint ID (unused)
 * @param option - Selected sprint option containing deadline and workload method
 */
const handleSprintChange = (_id: any, option: any) => {
  if (!props.taskId) {
    // For new tasks, set deadline based on sprint deadline
    if (option?.deadlineDate) {
      if (dayjs(option?.deadlineDate).isAfter(dayjs())) {
        formState.deadlineDate = option.deadlineDate;
      } else {
        formState.deadlineDate = dayjs().add(2, 'hour').format(DATE_TIME_FORMAT);
      }
    }
  } else {
    // For existing tasks, use sprint deadline directly
    formState.deadlineDate = option?.deadlineDate || '';
  }

  // Adjust deadline to business hours (8 AM - 7 PM)
  if (dayjs(formState.deadlineDate).hour() > 19 || dayjs(formState.deadlineDate).hour() < 8) {
    formState.deadlineDate = dayjs(formState.deadlineDate).add(12, 'hour').format(DATE_TIME_FORMAT);
  }

  sprintDeadlineDate.value = formState.deadlineDate;
  evalWorkloadMethod.value = option?.evalWorkloadMethod?.value;
};

/**
 * Handle task type change and reset related fields
 */
const handleTaskTypeChange = () => {
  // Auto-assign current user as tester for bug tasks
  if (!formState.testerId && formState.taskType === TaskType.BUG) {
    formState.testerId = currentUserId.value;
  }
};

/**
 * Assign current user to specified field
 * @param fieldKey - Field to assign current user to
 */
const assignCurrentUserToField = (fieldKey:'assigneeId'|'confirmerId'|'testerId') => {
  if (fieldKey === 'assigneeId') {
    formState.assigneeId = currentUserId.value;
    return;
  }
  if (fieldKey === 'confirmerId') {
    formState.confirmerId = currentUserId.value;
  }
  if (fieldKey === 'testerId') {
    formState.testerId = currentUserId.value;
  }
};

/**
 * Validate evaluation workload when actual workload is provided
 * @param _rule - Validation rule (unused)
 * @param value - Evaluation workload value
 * @returns Promise resolving to validation result
 */
const validateEvaluationWorkload = async (_rule: Rule, value: string) => {
  if (!props.taskId) {
    return;
  }
  if (formState.actualWorkload) {
    if (!value) {
      return Promise.reject(new Error(t('common.placeholders.inputEvalWorkload')));
    }
    return Promise.resolve();
  }
  return Promise.resolve();
};

/**
 * Handle actual workload change and clear evaluation workload validation if empty
 * @param value - Actual workload value
 */
const handleActualWorkloadChange = (value: string) => {
  if (!value) {
    formRef.value.clearValidate('evalWorkload');
  }
};

/**
 * Handle evaluation workload change and clear actual workload if empty
 * @param value - Evaluation workload value
 */
const handleEvaluationWorkloadChange = (value: string) => {
  if (!value) {
    formState.actualWorkload = undefined;
    formRef.value.clearValidate('evalWorkload');
  }
};

/**
 * Validate deadline date against business rules
 * @param _rule - Validation rule (unused)
 * @param value - Deadline date value
 * @returns Promise resolving to validation result
 */
const validateDeadlineDate = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error(t('common.placeholders.deadlineMustBeFuture')));
  }

  if (dayjs(value).isBefore(dayjs(), 'minute')) {
    return Promise.reject(new Error(t('common.placeholders.deadlineMustBeFuture')));
  }

  if (sprintDeadlineDate.value) {
    if (dayjs(value).isAfter(dayjs(sprintDeadlineDate.value), 'seconds')) {
      return Promise.reject(new Error(t('backlog.edit.messages.sprintDeadlineExceeded',
        { deadline: sprintDeadlineDate.value })));
    }
  }
  return Promise.resolve();
};

/**
 * Disable past dates in date picker
 * @param current - Current date being evaluated
 * @returns True if date should be disabled
 */
const isDateDisabled = (current: Dayjs) => {
  const today = dayjs().startOf('day');
  return current.isBefore(today, 'day');
};

/**
 * Handle rich text editor content change
 * @param value - New editor content
 */
const handleEditorContentChange = (value: string) => {
  formState.description = value;
};

/**
 * Handle rich text editor loading state change
 * @param value - Loading state
 */
const handleEditorLoadingChange = (value: boolean) => {
  loading.value = value;
};

/**
 * Handle file upload for task attachments
 * @param info - Upload change info
 */
const handleFileUpload = async function (info: any) {
  const file = info.file;
  if (!formState.attachments || formState.attachments.length >= 5 || loading.value) {
    return;
  }

  if (file.size! > 1024 * 1024 * MAX_FILE_SIZE_MB) {
    notification.warning(t('backlog.edit.messages.fileSizeLimit', { size: MAX_FILE_SIZE_MB }));
    return;
  }

  loading.value = true;
  const [error, { data = [] }] = await upload(file, { bizKey: UPLOAD_ISSUE_FILE_KEY });
  loading.value = false;
  if (error) {
    return;
  }

  if (data && data.length > 0) {
    const attachments = data?.map(item => ({ name: item.name, url: item.url }));
    formState.attachments.push(...attachments);
  }
};

/**
 * Remove file from attachments list
 * @param index - Index of file to remove
 */
const removeAttachmentFile = (index: number) => {
  formState?.attachments?.splice(index, 1);
};

// Description validation
const descriptionEditorRef = ref();

/**
 * Validate description length
 * @returns Promise resolving to validation result
 */
const validateDescriptionLength = async () => {
  if (descriptionEditorRef.value && descriptionEditorRef.value.getLength() > 6000) {
    Promise.reject(new Error(t('common.placeholders.inputDescription30')));
  }
  return Promise.resolve();
};

/**
 * Validate form fields and return validation result
 * @returns Promise resolving to validation success status
 */
const isFormValid = async () => {
  const requiredRuleKeys = [
    'name',
    'sprintId',
    'assigneeId',
    'deadlineDate',
    'description'
  ];

  if (formState.actualWorkload) {
    requiredRuleKeys.push('evalWorkload');
  }

  return new Promise((resolve) => {
    formRef.value.validate(requiredRuleKeys).then(async () => {
      return resolve(true);
    }).catch((errors:{errorFields:{errors:string[];name:string[];warnings:string;}[]}) => {
      // Allow deadline date validation to pass for special cases
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
 * Build task parameters object from form state
 * @returns Task parameters object for API calls
 */
const buildTaskParameters = () => {
  const params: TaskEditState = {
    projectId: props.projectId,
    sprintId: formState.sprintId,
    name: formState.name,
    taskType: formState.taskType,
    assigneeId: formState.assigneeId,
    deadlineDate: formState.deadlineDate,
    priority: formState.priority,
    parentTaskId: formState.parentTaskId
  };

  // Add parent task ID if provided
  if (props.parentTaskId) {
    params.parentTaskId = props.parentTaskId;
  }

  // Add optional fields if they have values
  if (formState.confirmerId) {
    params.confirmerId = formState.confirmerId;
  }

  if (formState.testerId) {
    params.testerId = formState.testerId;
  }

  if (formState.softwareVersion) {
    params.softwareVersion = formState.softwareVersion;
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

  // Add actual workload only for existing tasks
  if (props.taskId && formState.actualWorkload) {
    params.actualWorkload = formState.actualWorkload;
  }

  // Add bug-specific fields
  if (formState.taskType === TaskType.BUG) {
    params.bugLevel = formState.bugLevel;
    params.missingBug = formState.missingBug;
  }
  return params;
};

/**
 * Handle form submission for both create and edit operations
 * @param shouldContinue - Whether to continue after successful creation
 */
const handleFormSubmit = async (shouldContinue: boolean) => {
  // For existing tasks, check if form has changes
  if (props.taskId) {
    // Create normalized objects for comparison
    const normalizeForComparison = (obj: any) => {
      const normalized = { ...obj };
      // Ensure all array fields are arrays
      normalized.tagIds = normalized.tagIds || [];
      normalized.refTaskIds = normalized.refTaskIds || [];
      normalized.refCaseIds = normalized.refCaseIds || [];
      normalized.attachments = normalized.attachments || [];
      return normalized;
    };

    const normalizedOriginal = normalizeForComparison(originalFormState);
    const normalizedCurrent = normalizeForComparison(formState);

    const hasChanges = !isEqual(normalizedOriginal, normalizedCurrent);
    if (!hasChanges) {
      emit('update:visible', false);
      return;
    }
  }

  // Validate form and description length
  const isDescriptionValid = !formState.description || formState.description?.length <= 8000;
  const isFormValidResult = await isFormValid();
  if (!isFormValidResult || !isDescriptionValid) {
    return;
  }

  // Route to appropriate handler
  if (!props.taskId) {
    await handleTaskCreation(shouldContinue);
    return;
  }
  await handleTaskUpdate();
};

/**
 * Handle task creation
 * @param shouldContinue - Whether to continue after successful creation
 */
const handleTaskCreation = async (shouldContinue = false) => {
  loading.value = true;
  const params = buildTaskParameters();
  const [error, res] = await issue.addTask({ ...params, projectId: props.projectId });
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.addSuccess'));
  emit('ok', res?.data, true);

  if (!shouldContinue) {
    closeModal();
  }
};

/**
 * Handle task update
 */
const handleTaskUpdate = async () => {
  loading.value = true;
  const params = buildTaskParameters();
  const [error] = await issue.putTask(props.taskId as number, params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.editSuccess'));
  const updatedTaskData = await loadTaskData();
  emit('ok', updatedTaskData);
  closeModal();
};

/**
 * Close modal and reset state
 */
const closeModal = () => {
  emit('update:taskId', undefined);
  emit('update:visible', false);
};

/**
 * Load module tree data for the project
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

/**
 * Load task data for editing
 * @returns Promise resolving to task data
 */
const loadTaskData = async (): Promise<Partial<TaskDetail>> => {
  loading.value = true;
  const [error, res] = await issue.getTaskDetail(props.taskId as number);
  loading.value = false;
  if (error || !res?.data) {
    return { id: props.taskId! };
  }
  return res.data;
};

/**
 * Initialize component state from local storage
 */
const initializeComponent = () => {
  zoomInFlag.value = !!localStore.get(zoomInFlagCacheKey.value);
};

/**
 * Reset form to default values for new task creation
 */
const resetFormToDefaults = () => {
  loading.value = false;
  showEditorFlag.value = false;
  evalWorkloadMethod.value = EvalWorkloadMethod.STORY_POINT;
  sprintDeadlineDate.value = undefined;

  originalFormState = undefined;
  formState.attachments = [];

  // Set default deadline to tomorrow, adjusted for business hours
  formState.deadlineDate = dayjs().add(1, 'day').format(DATE_TIME_FORMAT);
  if (dayjs(formState.deadlineDate).hour() > 19 || dayjs(formState.deadlineDate).hour() < 8) {
    formState.deadlineDate = dayjs(formState.deadlineDate).add(12, 'hour').format(DATE_TIME_FORMAT);
  }

  // Reset form fields to default values
  formState.description = props.description || '';
  formState.evalWorkload = undefined;
  formState.actualWorkload = undefined;
  formState.name = props.name || '';
  formState.priority = Priority.MEDIUM;
  formState.bugLevel = BugLevel.MINOR;
  formState.parentTaskId = undefined;
  formState.sprintId = props.sprintId;
  formState.tagIds = [];
  formState.refTaskIds = [];
  formState.refCaseIds = props.refCaseIds || [];
  formState.taskType = props.taskType || TaskType.TASK;
  formState.missingBug = false;
  formState.assigneeId = props.assigneeId || props.userInfo?.id || undefined;
  formState.testerId = props.taskType === TaskType.BUG ? props.userInfo?.id : undefined;
  formState.confirmerId = props.confirmerId || undefined;
  formState.softwareVersion = undefined;

  // Set module ID if provided and valid
  if (props.moduleId && +props.moduleId > 0) {
    formState.moduleId = props.moduleId;
  } else {
    formState.moduleId = undefined;
  }
};

/**
 * Populate form state with loaded task data
 * @param data - Task data from API
 */
const populateFormWithTaskData = (data: Partial<TaskDetail>) => {
  // Set assignee data
  const assigneeId = data.assigneeId;
  if (assigneeId) {
    formState.assigneeId = assigneeId;
    assigneeDefaultOptions.value = {
      [assigneeId]: {
        fullName: data.assigneeName || '',
        id: assigneeId
      }
    };
  }

  // Set confirmer data
  const confirmerId = data.confirmerId;
  if (confirmerId) {
    formState.confirmerId = confirmerId;
    confirmerDefaultOptions.value = {
      [confirmerId]: {
        fullName: data.confirmerName || '',
        id: confirmerId
      }
    };
  }

  // Populate all form fields
  formState.attachments = data.attachments || [];
  formState.moduleId = data.moduleId ? (+data.moduleId < 0 ? undefined : data.moduleId) : undefined;
  formState.deadlineDate = data.deadlineDate;
  formState.description = data.description;
  formState.evalWorkload = data.evalWorkload;
  formState.actualWorkload = data.actualWorkload;
  formState.name = data.name;
  formState.priority = data.priority?.value || Priority.MEDIUM;
  formState.parentTaskId = data.parentTaskId;
  formState.sprintId = data.sprintId;
  formState.tagIds = data.tags?.map(item => item.id) || [];
  formState.refTaskIds = data.refTaskInfos?.map(item => item.id) || [];
  formState.refCaseIds = data.refCaseInfos?.map(item => item.id) || [];
  formState.taskType = data.taskType?.value || TaskType.TASK;
  formState.testerId = data.testerId;
  formState.missingBug = data.missingBug || false;
  formState.bugLevel = data.bugLevel?.value || BugLevel.MINOR;
  formState.softwareVersion = data.softwareVersion;

  // Store original state for change detection
  originalFormState = cloneDeep(formState);

  evalWorkloadMethod.value = data.evalWorkloadMethod?.value || EvalWorkloadMethod.STORY_POINT;
  showEditorFlag.value = true;
};

/**
 * Determine if task ID should be excluded from parent task selection
 * @param data - Task data
 * @returns True if task ID should be excluded
 */
const shouldExcludeTaskId = (data: any) => {
  return props.taskId === data.id;
};

/**
 * Get popup container for dropdown components
 * @param node - DOM node
 * @returns Container element for popup
 */
const getDropdownContainer = (node: HTMLElement): HTMLElement => {
  if (node) {
    return node.parentNode as HTMLElement;
  }
  return document.body;
};

const handleSelectTemplate = async (template: any) => {
  if (template?.templateContent) {
    formState.name = template.templateContent.name;
    formState.taskType = template.templateContent.taskType?.value || template.templateContent.taskType;
    formState.bugLevel = template.templateContent.bugLevel?.value || template.templateContent.bugLevel;
    formState.priority = template.templateContent.priority?.value || template.templateContent.priority;
    formState.missingBug = template.templateContent.missingBug || false;
    formState.evalWorkloadMethod = template.templateContent.evalWorkloadMethod?.value || EvalWorkloadMethod.WORKING_HOURS;
    formState.evalWorkload = template.templateContent.evalWorkload;
    formState.actualWorkload = template.templateContent.actualWorkload ;
    formState.description = template.templateContent.description;
  }
};

// Lifecycle hooks
onMounted(() => {
  initializeComponent();

  watch(() => props.visible, async () => {
    if (props.visible) {
      await loadModuleTreeData();
      if (typeof formRef.value?.clearValidate === 'function') {
        await formRef.value.clearValidate();
      }

      if (!props.taskId) {
        resetFormToDefaults();
        return;
      }

      const taskData = await loadTaskData();
      if (!taskData) {
        resetFormToDefaults();
        return;
      }

      // Populate form with loaded task data
      populateFormWithTaskData(taskData);
    }
  }, { immediate: true });
});
</script>
<template>
  <Modal
    :title="modalTitle"
    :centered="true"
    :style="modalStyle"
    :visible="props.visible"
    class="relative max-w-full"
    @cancel="closeModal">
    <Tooltip :title="zoomInFlag ? t('actions.exitFullScreen') : t('actions.fullScreen')">
      <Icon
        :icon="zoomInFlag ? 'icon-tuichuzuida' : 'icon-zuidahua'"
        class="absolute right-10 top-3.5 text-3.5 cursor-pointer"
        @click="toggleModalZoom" />
    </Tooltip>
    <Form
      ref="formRef"
      :model="formState"
      :labelCol="{}"
      layout="vertical"
      class="overflow-y-auto overflow-x-hidden"
      size="small"
      :style="formStyle">
      <div class="flex">
        <div class="flex-1 pr-8">
          <div class="flex space-x-2">
            <FormItem
              name="name"
              class="flex-1"
              :label="t('common.name')"
              :rules="{ required: true, message: t('common.placeholders.inputName2') }">
                <Input
                  v-model:value="formState.name"
                  trim
                  :maxlength="200"
                  :placeholder="t('common.placeholders.inputName2')" />
            </FormItem>
            <FormItem label="&nbsp;">
              <DropdownTemplateSelect templateType="ISSUE" @change="handleSelectTemplate" />
            </FormItem>
          </div>
          

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
                          {{ t(`backlog.typeDescriptions.${taskType.toLowerCase()}`) }}
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
                :readonly="isTaskTypeReadonly"
                internal
                enumKey="TaskType"
                :placeholder="t('common.placeholders.selectIssueType')"
                @change="handleTaskTypeChange">
                <template #option="record">
                  <div class="flex items-center">
                    <IconTask :value="record.value as TaskType" class="text-4 flex-shrink-0" />
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
                :placeholder="t('common.placeholders.selectPriority')">
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
                :label="t('common.bugLevel')"
                class="flex-1/2">
                <SelectEnum
                  v-model:value="formState.bugLevel"
                  enumKey="BugLevel"
                  :allowClear="false"
                  :lazy="false" />
              </FormItem>

              <FormItem
                name="missingBug"
                :label="t('common.escapedBug')"
                class="flex-1/2">
                <Select
                  :value="(formState.missingBug as any)"
                  :options="[
                    { value: (true as any), label: t('status.yes') },
                    { value: (false as any), label: t('status.no') }
                  ]">
                </Select>
              </FormItem>
            </div>
          </template>

          <div class="flex space-x-4">
            <FormItem
              name="assigneeId"
              class="flex-1/2"
              :rules="{ required: true, message: t('common.placeholders.selectAssignee') }">
              <template #label>
                {{ t('common.assignee') }}
                <Popover placement="rightTop">
                  <template #content>
                    <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                      {{ t('backlog.edit.descriptions.assignee') }}
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
                </Popover>
              </template>

              <div class="flex items-center ">
                <SelectUser
                  v-model:value="formState.assigneeId"
                  :placeholder="t('common.placeholders.selectAssignee')"
                  internal
                  class="flex-1 min-w-0"
                  :defaultOptions="assigneeDefaultOptions"
                  :action="`${TESTER}/project/${props.projectId}/member/user`"
                  :maxlength="80" />

                <Button
                  size="small"
                  type="link"
                  class="p-0 h-5 leading-5 ml-1"
                  @click="assignCurrentUserToField('assigneeId')">
                  {{ t('actions.assignToMe') }}
                </Button>
              </div>
            </FormItem>

            <FormItem name="confirmerId" class="flex-1/2">
              <template #label>
                {{ t('common.confirmer') }}<Popover placement="rightTop">
                  <template #content>
                    <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                      {{ t('backlog.edit.descriptions.confirmer') }}
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
                </Popover>
              </template>

              <div class="flex items-center">
                <SelectUser
                  v-model:value="formState.confirmerId"
                  :placeholder="t('common.placeholders.selectConfirmer')"
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
                  @click="assignCurrentUserToField('confirmerId')">
                  {{ t('actions.assignToMe') }}
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
                :disabledDate="isDateDisabled"
                :showTime="{ hideDisabledOptions: true, format: TIME_FORMAT }"
                :disabled="(proTypeShowMap.showSprint && !formState.sprintId)"
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
                      {{ t('backlog.edit.descriptions.tester') }}
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
                </Popover>
              </template>

              <div class="flex items-center">
                <SelectUser
                  v-model:value="formState.testerId"
                  :placeholder="t('common.placeholders.selectTester')"
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
                  @click="assignCurrentUserToField('testerId')">
                  {{ t('actions.assignToMe') }}
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
                ref="descriptionEditorRef"
                v-model:value="formState.description"
                :options="{placeholder: t('common.placeholders.inputDescription30')}"
                :height="280"
                @change="handleEditorContentChange"
                @loadingChange="handleEditorLoadingChange" />
            </AsyncComponent>
          </FormItem>
        </div>

        <div class="w-80 pl-2 border-l">
          <FormItem
            v-if="proTypeShowMap.showSprint"
            :label="t('common.sprint')"
            name="sprintId"
            :rules="{ required: true, message: t('common.placeholders.selectSprint') }">
            <Select
              v-model:value="formState.sprintId"
              :action="`${TESTER}/task/sprint?projectId=${props.projectId}&fullTextSearch=true`"
              :fieldNames="{ value: 'id', label: 'name' }"
              :readonly="!!props.taskId"
              showSearch
              internal
              :placeholder="t('common.placeholders.selectOrSearchSprint')"
              @change="(value: any, option: any) => handleSprintChange(value, option)">
              <template #option="record">
                <div class="flex items-center" :title="record.name">
                  <Icon icon="icon-jihua" class="mr-1 text-4" />
                  <div style="max-width: 220px;" class="truncate">{{ record.name }}</div>
                </div>
              </template>
            </Select>
          </FormItem>

          <FormItem
            :label="t('common.module')"
            name="moduleId">
            <TreeSelect
              v-model:value="formState.moduleId"
              :treeData="moduleTreeData"
              :fieldNames="{ value: 'id', label: 'name', children: 'children' }"
              :getPopupContainer="getDropdownContainer"
              :virtual="false"
              size="small"
              showSearch
              allowClear
              :placeholder="t('common.placeholders.selectOrSearchModule')">
              <template #title="item">
                <div class="flex items-center" :title="item.name">
                  <Icon icon="icon-mokuai" class="mr-1 text-3.5" />
                  <div style="max-width: 220px;" class="truncate">{{ item.name }}</div>
                </div>
              </template>
            </TreeSelect>
          </FormItem>

          <FormItem
            :label="t('common.parentIssue')"
            name="parentTaskId">
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
              :placeholder="t('common.placeholders.selectParentIssue')"
              :excludes="shouldExcludeTaskId"
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
            :rules="{ required: !!formState.actualWorkload, validator: validateEvaluationWorkload, trigger: 'change' }">
            <template #label>
              {{ t('common.evalWorkload') }}
            </template>

            <Input
              v-model:value="formState.evalWorkload"
              size="small"
              dataType="float"
              trimAll
              :min="0.1"
              :max="1000"
              :placeholder="t('common.placeholders.workloadRange')"
              @blur="handleEvaluationWorkloadChange($event.target.value)" />
          </FormItem>

          <template v-if="!!props.taskId">
            <FormItem name="actualWorkload">
              <template #label>
                <span class="w-70">{{ t('common.actualWorkload') }}</span>
                <Popover placement="rightTop">
                  <template #content>
                    <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                      {{ t('backlog.edit.descriptions.evalWorkload') }}
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
                :placeholder="t('common.placeholders.workloadRange')"
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
              :placeholder="t('common.placeholders.selectSoftwareVersion')"
              :action="`${TESTER}/software/version?projectId=${props.projectId}`"
              :params="softwareVersionParams"
              :fieldNames="{ value:'name', label: 'name'}">
            </Select>
          </FormItem>

          <FormItem
            name="tagIds"
            class="relative">
            <template #label>
              {{ t('common.tag') }}
              <Popover placement="rightTop">
                <template #content>
                  <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                    {{ t('backlog.edit.descriptions.tags') }}
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
              :placeholder="t('backlog.edit.placeholders.maxTags')"
              mode="multiple"
              :notFoundContent="t('backlog.edit.messages.contactAdminForTags')" />
          </FormItem>

          <FormItem
            name="refTaskIds"
            :label="t('common.assocIssues')"
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
              :placeholder="t('backlog.edit.placeholders.maxAssocIssues')"
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
            :label="t('common.assocCases')"
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
              :placeholder="t('backlog.edit.placeholders.maxAssocCases')"
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

          <FormItem
            name="attachments"
            class="relative">
            <template #label>
              {{ t('common.attachment') }}
              <Popover placement="rightTop">
                <template #content>
                  <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                    {{ t('backlog.edit.actions.uploadAttachments') }}
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
              </Popover>
            </template>
            <div
              style="height: 60px; border-color: rgba(0, 119, 255);background-color: rgba(0, 119, 255, 4%);"
              class="border border-dashed rounded flex flex-col px-2 py-1"
              :class="formState?.attachments?.length?'justify-between':'justify-center'">
              <template v-if="formState.attachments?.length">
                <div
                  style="height: 286px;scrollbar-gutter: stable;"
                  class="overflow-hidden hover:overflow-y-auto -mr-2 pr-1">
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
                      @click="removeAttachmentFile(index)" />
                  </div>
                </div>

                <div v-if="formState.attachments.length < 5" class="flex justify-end">
                  <Upload
                    :fileList="[]"
                    name="file"
                    class="-mb-1 mr-1"
                    :customRequest="handleFileUpload">
                    <Icon icon="icon-shangchuan" class="text-theme-special mr-1" />
                    <span class="text-3 leading-3 text-theme-text-hover">{{ t('backlog.edit.actions.continueUpload') }}</span>
                  </Upload>
                </div>
              </template>
              <template v-else>
                <div class="flex justify-center text-center">
                  <Upload
                    name="file"
                    :fileList="[]"
                    :customRequest="handleFileUpload">
                    <Icon icon="icon-shangchuan" class="mr-1 text-theme-special" />
                    <span class="text-3 text-theme-text-hover">{{ t('actions.upload') }}</span>
                    <span class="text-3 block">
                      {{ t('backlog.edit.messages.fileSizeLimit', { size: MAX_FILE_SIZE_MB }) }}
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
        @click="closeModal">
        {{ t('actions.cancel') }}
      </Button>
      <Button
        v-if="showContinue"
        type="primary"
        class="text-3 leading-3"
        size="small"
        :disabled="loading"
        @click="handleFormSubmit(true)">
        {{ t('actions.saveAndContinue') }}
      </Button>
      <Button
        type="primary"
        class="text-3 leading-3"
        size="small"
        :disabled="loading"
        @click="handleFormSubmit(false)">
        {{ t('actions.confirm') }}
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
