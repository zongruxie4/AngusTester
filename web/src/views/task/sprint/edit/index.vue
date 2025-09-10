<script lang="ts" setup>
import { computed, inject, nextTick, onMounted, ref, watch, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import {
  AsyncComponent,
  DatePicker,
  Icon,
  Input,
  modal,
  notification,
  SelectUser,
  Spin,
  Tooltip
} from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Radio, RadioGroup, TabPane, Tabs, Upload } from 'ant-design-vue';
import {
  EnumMessage,
  EvalWorkloadMethod,
  toClipboard,
  utils,
  TESTER,
  enumUtils,
  upload,
  appContext
} from '@xcan-angus/infra';
import type { Rule } from 'ant-design-vue/es/form';
import dayjs from 'dayjs';
import { TaskSprintPermission, TaskSprintStatus } from '@/enums/enums';
import { task } from '@/api/tester';
import { EditFormState, SprintInfo } from '../types';
import { DATE_TIME_FORMAT } from '@/utils/constant';

/**
 * <p>Component props interface for SprintEdit component</p>
 * <p>Defines the required data structure for sprint editing functionality</p>
 */
type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  data: {
    _id: string;
    id: string | undefined;
  }
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

// Lazy load async components for better performance
const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

// Dependency Injection & Context
const { t } = useI18n();
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const replaceTabPane = inject<(id: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));

/**
 * <p>Computed property to check if current user is admin</p>
 * <p>Used for permission-based UI rendering and action enabling</p>
 */
const isAdmin = computed(() => appContext.isAdmin());

/**
 * <p>Reference to the form instance for validation and submission</p>
 */
const formRef = ref();

/**
 * <p>Available workload evaluation method options</p>
 * <p>Populated from enum values for radio group selection</p>
 */
const workloadMethodOptions = ref<EnumMessage<EvalWorkloadMethod>[]>([]);

/**
 * <p>Current sprint data source</p>
 * <p>Contains the sprint information being edited or viewed</p>
 */
const currentSprintData = ref<SprintInfo>();

/**
 * <p>User permissions for current sprint</p>
 * <p>Determines which actions the user can perform</p>
 */
const userPermissions = ref<string[]>([]);

/**
 * <p>Default date values for new sprints</p>
 * <p>Start date is current time, deadline is one month from now</p>
 */
const defaultStartDate = dayjs().format(DATE_TIME_FORMAT);
const defaultDeadlineDate = dayjs().add(1, 'month').format(DATE_TIME_FORMAT);

/**
 * <p>Form state containing all sprint editing data</p>
 * <p>Includes basic info, dates, owner, and additional details</p>
 */
const formState = ref<EditFormState>({
  taskPrefix: '',
  otherInformation: '',
  acceptanceCriteria: '',
  projectId: props.projectId,
  evalWorkloadMethod: EvalWorkloadMethod.STORY_POINT,
  name: '',
  ownerId: props.userInfo?.id,
  startDate: defaultStartDate,
  deadlineDate: defaultDeadlineDate,
  attachments: [],
  date: [defaultStartDate, defaultDeadlineDate]
});

/**
 * <p>Default owner options for user selection</p>
 * <p>Pre-populated when editing existing sprint</p>
 */
const ownerSelectionOptions = ref<{ [key: string]: { fullName: string; id: string; } }>();

/**
 * <p>Loading state for async operations</p>
 * <p>Controls spinner display during API calls</p>
 */
const isLoading = ref(false);

/**
 * <p>Visibility state for authorization modal</p>
 * <p>Controls when permission management dialog is shown</p>
 */
const isAuthorizeModalVisible = ref(false);

/**
 * <p>Prepares form data for API submission</p>
 * <p>Transforms form state into the format expected by backend APIs</p>
 * <p>Removes empty optional fields and formats date range</p>
 * @returns {EditFormState} Formatted parameters ready for API call
 */
const prepareFormParams = () => {
  const params: EditFormState = { ...formState.value };
  const sprintId = currentSprintData.value?.id;

  // Add sprint ID for edit operations
  if (sprintId) {
    params.id = sprintId;
  }

  // Format date range from picker to individual date fields
  const dateRange = formState.value.date;
  if (dateRange) {
    params.startDate = dateRange[0];
    params.deadlineDate = dateRange[1];
  }

  // Remove empty optional fields to reduce payload size
  if (!params.attachments?.length) {
    delete params.attachments;
  }

  if (!params.otherInformation) {
    delete params.otherInformation;
  }

  if (!params.acceptanceCriteria) {
    delete params.acceptanceCriteria;
  }

  if (!params.taskPrefix) {
    delete params.taskPrefix;
  }

  // Remove temporary date range field
  delete params.date;
  return params;
};

/**
 * <p>Refreshes the sprint list in parent component</p>
 * <p>Triggers a notification to update the sprint list tab</p>
 */
const refreshSprintList = () => {
  nextTick(() => {
    updateTabPane({ _id: 'sprintList', notify: utils.uuid() });
  });
};

/**
 * <p>Handles sprint update operation</p>
 * <p>Validates form, calls update API, and updates UI state</p>
 */
const handleSprintUpdate = async () => {
  const params = prepareFormParams();

  isLoading.value = true;
  const [error] = await task.putSprint(params);
  isLoading.value = false;

  if (error) {
    return;
  }

  notification.success(t('taskSprint.messages.editSuccess'));

  // Update tab title and local data
  const sprintId = params.id;
  const sprintName = params.name;
  updateTabPane({ _id: sprintId, name: sprintName });

  if (currentSprintData.value) {
    currentSprintData.value.name = sprintName;
  }
};

/**
 * <p>Handles sprint creation operation</p>
 * <p>Validates form, calls create API, and navigates to new sprint</p>
 */
const handleSprintCreation = async () => {
  const params = prepareFormParams();

  isLoading.value = true;
  const [error, response] = await task.addSprint(params);
  isLoading.value = false;

  if (error) {
    return;
  }

  notification.success(t('taskSprint.messages.addSuccess'));

  // Navigate to the newly created sprint
  const currentTabId = props.data?._id;
  const newSprintId = response?.data?.id;
  const sprintName = params.name;

  replaceTabPane(currentTabId, {
    _id: newSprintId,
    uiKey: newSprintId,
    name: sprintName,
    data: { _id: newSprintId, id: newSprintId }
  });
};

/**
 * <p>Reference to acceptance criteria rich text editor</p>
 * <p>Used for length validation and content management</p>
 */
const acceptanceCriteriaEditorRef = ref();

/**
 * <p>Reference to other information rich text editor</p>
 * <p>Used for length validation and content management</p>
 */
const otherInformationEditorRef = ref();

/**
 * <p>Validates maximum length for rich text editor fields</p>
 * <p>Ensures content doesn't exceed 2000 characters limit</p>
 * @param {Object} validationRule - Form validation rule object
 * @returns {Promise} Validation result promise
 */
const validateRichTextMaxLength = (validationRule) => {
  if (validationRule.field === 'acceptanceCriteria') {
    if (acceptanceCriteriaEditorRef.value && acceptanceCriteriaEditorRef.value.getLength() > 2000) {
      return Promise.reject(t('taskSprint.messages.maxLengthExceeded'));
    }
  }

  if (validationRule.field === 'otherInformation') {
    if (otherInformationEditorRef.value && otherInformationEditorRef.value.getLength() > 2000) {
      return Promise.reject(t('taskSprint.messages.maxLengthExceeded'));
    }
  }
  return Promise.resolve();
};

/**
 * <p>Main form submission handler</p>
 * <p>Validates form and delegates to appropriate create or update handler</p>
 */
const handleFormSubmission = async () => {
  formRef.value.validate().then(async () => {
    if (!isEditMode.value) {
      await handleSprintCreation();
    } else {
      await handleSprintUpdate();
    }
    refreshSprintList();
  });
};

/**
 * <p>Starts the current sprint</p>
 * <p>Changes sprint status from PENDING/BLOCKED/COMPLETED to IN_PROGRESS</p>
 */
const startSprint = async () => {
  const sprintId = currentSprintData.value?.id;
  if (!sprintId) {
    return;
  }

  isLoading.value = true;
  const [error] = await task.startSprint(sprintId);
  isLoading.value = false;

  if (error) {
    return;
  }

  notification.success(t('taskSprint.messages.startSuccess'));
  await loadSprintData(sprintId);
  refreshSprintList();
};

/**
 * <p>Completes the current sprint</p>
 * <p>Changes sprint status from IN_PROGRESS to COMPLETED</p>
 */
const completeSprint = async () => {
  const sprintId = currentSprintData.value?.id;
  if (!sprintId) {
    return;
  }

  isLoading.value = true;
  const [error] = await task.endSprint(sprintId);
  isLoading.value = false;

  if (error) {
    return;
  }

  notification.success(t('taskSprint.messages.completeSuccess'));
  await loadSprintData(sprintId);
  refreshSprintList();
};

/**
 * <p>Deletes the current sprint with confirmation</p>
 * <p>Shows confirmation dialog before proceeding with deletion</p>
 */
const deleteSprint = async () => {
  const sprintData = currentSprintData.value;
  if (!sprintData) {
    return;
  }

  modal.confirm({
    content: t('taskSprint.messages.confirmDelete', { name: sprintData.name }),
    async onOk () {
      const sprintId = sprintData.id;
      isLoading.value = true;
      const [error] = await task.deleteSprint(sprintId);
      isLoading.value = false;

      if (error) {
        return;
      }

      notification.success(t('taskSprint.messages.deleteSuccess'));
      deleteTabPane([props.data._id]);
      refreshSprintList();
    }
  });
};

/**
 * <p>Opens the authorization modal for permission management</p>
 */
const openPermissionModal = () => {
  isAuthorizeModalVisible.value = true;
};

/**
 * <p>Handles permission changes after authorization modal updates</p>
 * <p>Reloads sprint data and refreshes list to reflect permission changes</p>
 */
const handlePermissionChange = async () => {
  const sprintId = currentSprintData.value?.id;
  if (!sprintId) {
    return;
  }

  await loadSprintData(sprintId);
  refreshSprintList();
};

/**
 * <p>Clones the current sprint</p>
 * <p>Creates a copy of the sprint with same configuration</p>
 */
const cloneSprint = async () => {
  const sprintId = currentSprintData.value?.id;
  if (!sprintId) {
    return;
  }

  isLoading.value = true;
  const [error] = await task.cloneSprint(sprintId);
  isLoading.value = false;

  if (error) {
    return;
  }

  notification.success(t('taskSprint.messages.cloneSuccess'));
  refreshSprintList();
};

/**
 * <p>Copies sprint link to clipboard</p>
 * <p>Generates shareable URL for the current sprint</p>
 */
const copySprintLink = () => {
  const sprintId = currentSprintData.value?.id;
  if (!sprintId) {
    return;
  }

  const sprintUrl = `${window.location.origin}/task#sprint?id=${sprintId}`;
  toClipboard(sprintUrl).then(() => {
    notification.success(t('taskSprint.messages.copyLinkSuccess'));
  }).catch(() => {
    notification.error(t('taskSprint.messages.copyLinkFailed'));
  });
};

/**
 * <p>Refreshes current sprint data</p>
 * <p>Reloads sprint information from server</p>
 */
const refreshSprintData = () => {
  const sprintId = currentSprintData.value?.id;
  if (!sprintId) {
    return;
  }

  loadSprintData(sprintId);
};

/**
 * <p>Closes the current tab and returns to sprint list</p>
 */
const closeCurrentTab = () => {
  deleteTabPane([props.data._id]);
};

/**
 * <p>Loads enum options for workload evaluation methods</p>
 * <p>Populates radio group options from EvalWorkloadMethod enum</p>
 */
const loadWorkloadMethodOptions = () => {
  workloadMethodOptions.value = enumUtils.enumToMessages(EvalWorkloadMethod);
};

/**
 * <p>Loads user permissions for the current sprint</p>
 * <p>Determines which actions the user can perform based on their role and permissions</p>
 * @param {string} sprintId - The ID of the sprint to load permissions for
 */
const loadUserPermissions = async (sprintId: string) => {
  // Admin users have all permissions
  if (isAdmin.value) {
    userPermissions.value = enumUtils.getEnumValues(TaskSprintPermission);
    return;
  }

  const authParams = {
    admin: true
  };

  isLoading.value = true;
  const [error, response] = await task.getCurrentUserSprintAuth(sprintId, authParams);
  isLoading.value = false;
  if (error) {
    return;
  }

  const { taskSprintAuth, permissions: userPermissionList } = response?.data || {
    taskSprintAuth: true,
    permissions: []
  };

  // If no auth control, user has all permissions plus grant if specifically allowed
  if (!taskSprintAuth) {
    userPermissions.value = enumUtils.getEnumValues(TaskSprintPermission);
    if (userPermissionList.includes(TaskSprintPermission.GRANT)) {
      userPermissions.value.push(TaskSprintPermission.GRANT);
    }
  } else {
    // Map permission objects to their values
    userPermissions.value = (userPermissionList || []).map(item => item.value);
  }
};

/**
 * <p>Loads sprint detail data from server</p>
 * <p>Fetches complete sprint information and populates form</p>
 * @param {string} sprintId - The ID of the sprint to load
 */
const loadSprintData = async (sprintId: string) => {
  isLoading.value = true;
  const [error, response] = await task.getSprintDetail(sprintId);
  isLoading.value = false;

  if (error) {
    return;
  }

  const sprintData = response?.data as SprintInfo;
  if (!sprintData) {
    return;
  }

  currentSprintData.value = sprintData;
  populateFormWithSprintData(sprintData);

  // Update tab title with sprint name
  const sprintName = sprintData.name;
  if (sprintName && typeof updateTabPane === 'function') {
    updateTabPane({ name: sprintName, _id: sprintId });
  }
};

/**
 * <p>Populates form with sprint data or sets default values</p>
 * <p>Handles both new sprint creation and existing sprint editing scenarios</p>
 * @param {SprintInfo} sprintData - Sprint data to populate form with, null for new sprint
 */
const populateFormWithSprintData = (sprintData: SprintInfo) => {
  // Set default values for new sprint creation
  if (!sprintData) {
    const startDate = dayjs().format(DATE_TIME_FORMAT);
    const deadlineDate = dayjs().add(1, 'month').format(DATE_TIME_FORMAT);

    formState.value = {
      startDate,
      deadlineDate,
      taskPrefix: '',
      otherInformation: '',
      acceptanceCriteria: '',
      projectId: props.projectId,
      evalWorkloadMethod: EvalWorkloadMethod.STORY_POINT,
      name: '',
      ownerId: props.userInfo?.id,
      attachments: [],
      date: [startDate, deadlineDate]
    };
    return;
  }

  // Extract data with default values
  const {
    taskPrefix = '',
    otherInformation = '',
    acceptanceCriteria = '',
    projectId = '',
    evalWorkloadMethod,
    name = '',
    ownerId = '',
    ownerName = '',
    attachments = '',
    startDate = '',
    deadlineDate = ''
  } = sprintData;

  // Populate form fields
  formState.value.taskPrefix = taskPrefix;
  formState.value.otherInformation = otherInformation;
  formState.value.acceptanceCriteria = acceptanceCriteria;
  formState.value.projectId = projectId;
  formState.value.evalWorkloadMethod = evalWorkloadMethod?.value || '';
  formState.value.name = name;
  formState.value.ownerId = ownerId;

  // Set owner selection options for user picker
  ownerSelectionOptions.value = {
    [ownerId]: { fullName: ownerName, id: ownerId }
  };

  formState.value.startDate = startDate;
  formState.value.deadlineDate = deadlineDate;
  formState.value.attachments = attachments || [];
  formState.value.date = [startDate, deadlineDate];
};

/**
 * <p>Validates date range selection</p>
 * <p>Ensures both start and end dates are selected for sprint planning</p>
 * @param {Rule} _validationRule - Form validation rule object (unused)
 * @param {Array} dateRange - Array containing start and end dates
 * @returns {Promise} Validation result promise
 */
const validateDateRange = async (_validationRule: Rule, dateRange: string[]) => {
  if (!dateRange) {
    return Promise.reject(new Error(t('taskSprint.messages.dateRequired')));
  } else if (!dateRange[0]) {
    return Promise.reject(new Error(t('taskSprint.messages.startDateRequired')));
  } else if (!dateRange[1]) {
    return Promise.reject(new Error(t('taskSprint.messages.endDateRequired')));
  } else {
    return Promise.resolve();
  }
};

/**
 * <p>Handles file upload for sprint attachments</p>
 * <p>Uploads file to server and adds to attachment list with size limit</p>
 * @param {Object} uploadInfo - Upload event containing file information
 */
const handleFileUpload = async (uploadInfo: any) => {
  const file = uploadInfo.file;
  if (!file) {
    return;
  }

  const currentAttachments = formState.value.attachments || [];

  // Enforce maximum attachment limit
  if (currentAttachments.length >= 10) {
    return;
  }

  isLoading.value = true;
  const [error, { data = [] }] = await upload(file, {
    bizKey: 'angusTesterCaseAttachments'
  });
  isLoading.value = false;

  if (error) {
    return;
  }

  // Add uploaded files to attachment list
  if (data && data.length > 0) {
    const uploadedFiles = data?.map(item => ({
      name: item.name,
      url: item.url
    }));

    if (formState.value.attachments) {
      formState.value.attachments.push(...uploadedFiles);
    } else {
      formState.value.attachments = uploadedFiles;
    }
  }
};

/**
 * <p>Removes attachment from the list</p>
 * <p>Deletes file reference from form state by index</p>
 * @param {number} attachmentIndex - Index of attachment to remove
 */
const removeAttachment = (attachmentIndex: number) => {
  formState.value?.attachments?.splice(attachmentIndex, 1);
};

/**
 * <p>Component initialization and data loading</p>
 * <p>Sets up watchers and loads initial data when component mounts</p>
 */
onMounted(() => {
  // Load enum options for form dropdowns
  loadWorkloadMethodOptions();

  // Watch for data changes and load sprint information
  watch(() => props.data, async (newData) => {
    const sprintId = newData?.id;
    if (!sprintId) {
      return;
    }

    // Load permissions and sprint data in parallel
    await Promise.all([
      loadUserPermissions(sprintId),
      loadSprintData(sprintId)
    ]);
  }, { immediate: true });
});

/**
 * <p>Current sprint status value</p>
 * <p>Returns the status enum value for conditional rendering</p>
 */
const currentSprintStatus = computed(() => {
  return currentSprintData.value?.status?.value;
});

/**
 * <p>Determines if component is in edit mode</p>
 * <p>True when editing existing sprint, false when creating new sprint</p>
 */
const isEditMode = computed(() => {
  return !!props.data?.id;
});

/**
 * <p>Determines if form fields should be disabled</p>
 * <p>Disables editing when sprint is in progress or completed</p>
 */
const isFormDisabled = computed(() => {
  const status = currentSprintData.value?.status?.value;
  return !!(currentSprintData.value && status && [
    TaskSprintStatus.IN_PROGRESS,
    TaskSprintStatus.COMPLETED
  ].includes(status as TaskSprintStatus));
});

</script>
<template>
  <Spin :spinning="isLoading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="flex items-center space-x-2.5 mb-5">
      <Button
        :disabled="!isAdmin && !userPermissions.includes(TaskSprintPermission.MODIFY_SPRINT)"
        type="primary"
        size="small"
        class="flex items-center space-x-1"
        @click="handleFormSubmission">
        <Icon icon="icon-dangqianxuanzhong" class="text-3.5" />
        <span>{{ t('taskSprint.actions.save') }}</span>
      </Button>

      <template v-if="isEditMode">
        <Button
          v-if="currentSprintStatus === TaskSprintStatus.COMPLETED"
          :disabled="!isAdmin && !userPermissions.includes(TaskSprintPermission.MODIFY_SPRINT)"
          size="small"
          type="default"
          class="flex items-center space-x-1"
          @click="startSprint">
          <Icon icon="icon-kaishi" class="text-3.5" />
          <span>{{ t('taskSprint.actions.restart') }}</span>
        </Button>

        <Button
          v-else-if="currentSprintStatus && [TaskSprintStatus.PENDING, TaskSprintStatus.BLOCKED].includes(currentSprintStatus)"
          :disabled="!isAdmin && !userPermissions.includes(TaskSprintPermission.MODIFY_SPRINT)"
          size="small"
          type="default"
          class="flex items-center space-x-1"
          @click="startSprint">
          <Icon icon="icon-kaishi" class="text-3.5" />
          <span>{{ t('taskSprint.actions.start') }}</span>
        </Button>

        <template v-if="currentSprintStatus === TaskSprintStatus.IN_PROGRESS">
          <Button
            :disabled="!isAdmin && !userPermissions.includes(TaskSprintPermission.MODIFY_SPRINT)"
            size="small"
            type="default"
            class="flex items-center space-x-1"
            @click="completeSprint">
            <Icon icon="icon-yiwancheng" class="text-3.5" />
            <span>{{ t('taskSprint.actions.complete') }}</span>
          </Button>

          <Button
            :disabled="!isAdmin && !userPermissions.includes(TaskSprintPermission.MODIFY_SPRINT)"
            size="small"
            type="default"
            class="flex items-center space-x-1"
            @click="completeSprint">
            <Icon icon="icon-zusai" class="text-3.5" />
            <span>{{ t('taskSprint.actions.block') }}</span>
          </Button>
        </template>

        <Button
          :disabled="!isAdmin && !userPermissions.includes(TaskSprintPermission.DELETE_SPRINT)"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="deleteSprint">
          <Icon icon="icon-qingchu" class="text-3.5" />
          <span>{{ t('taskSprint.actions.delete') }}</span>
        </Button>

        <Button
          :disabled="!isAdmin && !userPermissions.includes(TaskSprintPermission.GRANT)"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="openPermissionModal">
          <Icon icon="icon-quanxian1" class="text-3.5" />
          <span>{{ t('taskSprint.actions.permission') }}</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="cloneSprint">
          <Icon icon="icon-fuzhizujian2" class="text-3.5" />
          <span>{{ t('taskSprint.actions.clone') }}</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="copySprintLink">
          <Icon icon="icon-fuzhi" class="text-3.5" />
          <span>{{ t('taskSprint.actions.copyLink') }}</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="refreshSprintData">
          <Icon icon="icon-shuaxin" class="text-3.5" />
          <span>{{ t('taskSprint.actions.refresh') }}</span>
        </Button>
      </template>
      <Button
        type="default"
        size="small"
        class="flex items-center"
        @click="closeCurrentTab">
        <span>{{ t('taskSprint.actions.cancel') }}</span>
      </Button>
    </div>

    <Form
      ref="formRef"
      :model="formState"
      size="small"
      :labelCol="{ style: { width: '75px' } }"
      class="max-w-242.5"
      layout="horizontal">
      <FormItem
        :label="t('taskSprint.form.name')"
        name="name"
        :rules="{ required: true, message: t('taskSprint.messages.nameRequired') }">
        <Input
          v-model:value="formState.name"
          size="small"
          :maxlength="100"
          :placeholder="t('taskSprint.placeholder.inputSprintName')" />
      </FormItem>

      <FormItem
        :label="t('taskSprint.form.timePlan')"
        name="date"
        :rules="{ required: true, validator: validateDateRange, trigger: 'change' }">
        <DatePicker
          v-model:value="formState.date"
          format="YYYY-MM-DD HH:mm:ss"
          :showNow="false"
          :showTime="{ format: 'HH:mm:ss' }"
          type="date-range"
          size="small"
          class="w-full" />
      </FormItem>

      <FormItem
        :label="t('taskSprint.form.owner')"
        name="ownerId"
        class="relative"
        :rules="{ required: true, message: t('taskSprint.messages.ownerRequired') }">
        <SelectUser
          v-model:value="formState.ownerId"
          size="small"
          :placeholder="t('taskSprint.placeholder.selectOwner')"
          :defaultOptions="ownerSelectionOptions"
          :action="`${TESTER}/project/${props.projectId}/member/user`"
          :maxlength="80" />
        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>{{ t('taskSprint.tips.ownerTip') }}</div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        :label="t('taskSprint.form.taskPrefix')"
        name="taskPrefix"
        class="relative">
        <Input
          v-model:value="formState.taskPrefix"
          size="small"
          :readonly="!!currentSprintData?.id"
          :disabled="!!currentSprintData?.id"
          :maxlength="40"
          :placeholder="t('taskSprint.placeholder.inputTaskPrefix')" />
        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>{{ t('taskSprint.tips.taskPrefixTip') }}</div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem :label="t('taskSprint.form.workloadAssessment')" name="evalWorkloadMethod">
        <RadioGroup v-model:value="formState.evalWorkloadMethod" :disabled="isFormDisabled">
          <Radio
            v-for="item in workloadMethodOptions"
            :key="item.value"
            :value="item.value">
            {{ item.message }}
          </Radio>
        </RadioGroup>
        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>{{ t('taskSprint.tips.workloadAssessmentTip') }}</div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem :label="t('taskSprint.form.attachments')">
        <div class="flex items-center">
          <Upload
            :fileList="[]"
            name="file"
            :customRequest="() => { }"
            @change="handleFileUpload">
            <a class="text-theme-special text-theme-text-hover text-3 flex items-center leading-5 h-5 mt-0.5">
              <Icon icon="icon-lianjie1" class="mr-1" />
              <span class="whitespace-nowrap">{{ t('taskSprint.form.uploadAttachment') }}</span>
            </a>
          </Upload>
          <Tooltip :overlayStyle="{ 'max-width': '400px' }">
            <template #title>
              <div class="text-3 text-theme-sub-content leading-4 break-all">
                {{ t('taskSprint.tips.attachmentTip') }}
              </div>
            </template>
            <Icon icon="icon-tishi1" class="text-tips ml-2 -mt-0.25 text-3.5 cursor-pointer" />
          </Tooltip>
        </div>
      </FormItem>

      <FormItem
        v-if="formState?.attachments?.length"
        :colon="false"
        label=" "
        class="-mt-5">
        <div
          v-for="(item, index) in formState.attachments"
          :key="index"
          class="flex items-center text-3 leading-5 pb-1 px-3 space-x-5 bg-gray-100 first:rounded-t first:pt-2 last:rounded-b last:pb-2">
          <div class="flex-1 truncate">{{ item.name }}</div>
          <Button
            type="text"
            size="small"
            class="flex-shrink-0 flex items-center justify-center px-0 leading-5 h-5">
            <Icon
              icon="icon-qingchu"
              class="text-3.5"
              @click="removeAttachment(index)" />
          </Button>
        </div>
      </FormItem>
      <Tabs size="small" class="pl-5">
        <TabPane key="acceptanceCriteria" :tab="t('taskSprint.form.acceptanceCriteria')">
          <FormItem
            class="!mb-5"
            name="acceptanceCriteria"
            :rules="[{validator: validateRichTextMaxLength}]">
            <RichEditor
              ref="acceptanceCriteriaEditorRef"
              v-model:value="formState.acceptanceCriteria"
              :options="{placeholder: t('taskSprint.placeholder.inputAcceptanceCriteria')}" />
          </FormItem>
        </TabPane>
        <TabPane key="otherInformation" :tab="t('taskSprint.form.otherInformation')">
          <FormItem
            class="!mb-5"
            name="otherInformation"
            :rules="[{validator: validateRichTextMaxLength}]">
            <RichEditor
              ref="otherInformationEditorRef"
              v-model:value="formState.otherInformation"
              :options="{placeholder: t('taskSprint.placeholder.inputOtherInformation')}" />
          </FormItem>
        </TabPane>
      </Tabs>
    </Form>

    <AsyncComponent :visible="isAuthorizeModalVisible">
      <AuthorizeModal
        v-model:visible="isAuthorizeModalVisible"
        enumKey="TaskSprintPermission"
        :appId="props.appInfo?.id"
        :listUrl="`${TESTER}/task/sprint/auth?sprintId=${currentSprintData?.id}`"
        :delUrl="`${TESTER}/task/sprint/auth`"
        :addUrl="`${TESTER}/task/sprint/${currentSprintData?.id}/auth`"
        :updateUrl="`${TESTER}/task/sprint/auth`"
        :enabledUrl="`${TESTER}/task/sprint/${currentSprintData?.id}/auth/enabled`"
        :initStatusUrl="`${TESTER}/task/sprint/${currentSprintData?.id}/auth/status`"
        onTips="开启&quot;有权限控制&quot;后，需要手动授权服务权限后才会有迭代相应操作权限，默认开启&quot;有权限控制&quot;。注意：如果授权对象没有父级项目权限将自动授权查看权限。"
        offTips="开启&quot;无权限控制&quot;后，将允许所有用户公开查看和操作当前迭代，查看用户同时需要有当前迭代父级项目权限。"
        title="迭代权限"
        @change="handlePermissionChange" />
    </AsyncComponent>
  </Spin>
</template>

<style scoped>
:deep(.ant-form-item-label>label::after) {
  margin-right: 10px;
}

.ant-tabs-small > :deep(.ant-tabs-nav) .ant-tabs-tab {
  padding-top: 0;
}
</style>
