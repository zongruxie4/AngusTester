<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, nextTick, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import {
  AsyncComponent, DatePicker, Icon, Input, modal, notification, SelectUser, Spin, Tooltip
} from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Radio, RadioGroup, TabPane, Tabs, Upload } from 'ant-design-vue';
import {
  appContext, EnumMessage, enumUtils, EvalWorkloadMethod, TESTER, toClipboard, upload, utils
} from '@xcan-angus/infra';
import type { Rule } from 'ant-design-vue/es/form';
import dayjs from 'dayjs';
import { TaskSprintPermission, TaskSprintStatus } from '@/enums/enums';
import { issue } from '@/api/tester';
import { EditFormState, SprintInfo } from '../types';
import { DATE_TIME_FORMAT, UPLOAD_TEST_FILE_KEY } from '@/utils/constant';
import { BasicProps } from '@/types/types';
import { IssueMenuKey } from '@/views/issue/menu';

/**
 * Component props interface for SprintEdit component
 */
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

// Lazy load async components for better performance
const AuthorizeModal = defineAsyncComponent(() => import('@/components/auth/AuthorizeModal.vue'));
const RichEditor = defineAsyncComponent(() => import('@/components/editor/richEditor/index.vue'));

// Dependency Injection & Context
const { t } = useI18n();
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const replaceTabPane = inject<(id: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));

/**
 * Computed property to check if current user is admin
 */
const isAdmin = computed(() => appContext.isAdmin());

/**
 * Reference to the form instance for validation and submission
 */
const formRef = ref();

/**
 * Available workload evaluation method options
 */
const workloadMethodOptions = ref<EnumMessage<EvalWorkloadMethod>[]>([]);

/**
 * Current sprint data source
 */
const currentSprintData = ref<SprintInfo>();

/**
 * User permissions for current sprint
 */
const userPermissions = ref<string[]>([]);

/**
 * Default date values for new sprints
 */
const defaultStartDate = dayjs().format(DATE_TIME_FORMAT);
const defaultDeadlineDate = dayjs().add(1, 'month').format(DATE_TIME_FORMAT);

/**
 * Default owner options for user selection
 */
const ownerSelectionOptions = ref<{ [key: string]: { fullName: string; id: string; } }>();

/**
 * Loading state for async operations
 */
const isLoading = ref(false);

/**
 * Visibility state for authorization modal
 */
const isAuthorizeModalVisible = ref(false);

/**
 * Reference to acceptance criteria rich text editor
 */
const acceptanceCriteriaEditorRef = ref();

/**
 * Reference to other information rich text editor
 */
const otherInformationEditorRef = ref();

/**
 * Form state containing all sprint editing data
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
 * Prepares form data for API submission
 * Transforms form state into the format expected by backend APIs
 * Removes empty optional fields and formats date range
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
 * Refreshes the sprint list in parent component
 */
const refreshSprintList = () => {
  nextTick(() => {
    updateTabPane({ _id: 'sprintList', notify: utils.uuid() });
  });
};

/**
 * Handles sprint update operation
 */
const handleSprintUpdate = async () => {
  const params = prepareFormParams();

  isLoading.value = true;
  const [error] = await issue.putSprint(params);
  isLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.editSuccess'));

  // Update tab title and local data
  const sprintId = params.id;
  const sprintName = params.name;
  updateTabPane({ _id: sprintId, name: sprintName });

  if (currentSprintData.value) {
    currentSprintData.value.name = sprintName;
  }
};

/**
 * Handles sprint creation operation
 */
const handleSprintCreation = async () => {
  const params = prepareFormParams();

  isLoading.value = true;
  const [error, response] = await issue.addSprint(params);
  isLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.addSuccess'));

  // Navigate to the newly created sprint
  const currentTabId = props.data?._id;
  const newSprintId = response?.data?.id;
  const sprintName = params.name;

  if (currentTabId && newSprintId) {
    replaceTabPane(currentTabId, {
      _id: newSprintId,
      uiKey: newSprintId,
      name: sprintName,
      data: { _id: newSprintId, id: newSprintId }
    });
  }
};

/**
 * Validates maximum length for rich text editor fields
 * Ensures content doesn't exceed 2000 characters limit
 * @param {Object} validationRule - Form validation rule object
 * @returns {Promise} Validation result promise
 */
const validateRichTextMaxLength = (validationRule) => {
  if (validationRule.field === 'acceptanceCriteria') {
    if (acceptanceCriteriaEditorRef.value && acceptanceCriteriaEditorRef.value.getLength() > 2000) {
      return Promise.reject(t('sprint.messages.maxLengthExceeded'));
    }
  }

  if (validationRule.field === 'otherInformation') {
    if (otherInformationEditorRef.value && otherInformationEditorRef.value.getLength() > 2000) {
      return Promise.reject(t('sprint.messages.maxLengthExceeded'));
    }
  }
  return Promise.resolve();
};

/**
 * Main form submission handler
 * Validates form and delegates to appropriate create or update handler
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
 * Starts the current sprint
 * Changes sprint status from PENDING/BLOCKED/COMPLETED to IN_PROGRESS
 */
const startSprint = async () => {
  const sprintId = currentSprintData.value?.id;
  if (!sprintId) {
    return;
  }

  isLoading.value = true;
  const [error] = await issue.startSprint(sprintId);
  isLoading.value = false;

  if (error) {
    return;
  }

  notification.success(t('actions.tips.startSuccess'));
  await loadSprintData(sprintId);
  refreshSprintList();
};

/**
 * Completes the current sprint
 * Changes sprint status from IN_PROGRESS to COMPLETED
 */
const completeSprint = async () => {
  const sprintId = currentSprintData.value?.id;
  if (!sprintId) {
    return;
  }

  isLoading.value = true;
  const [error] = await issue.endSprint(sprintId);
  isLoading.value = false;

  if (error) {
    return;
  }

  notification.success(t('actions.tips.completeSuccess'));
  await loadSprintData(sprintId);
  refreshSprintList();
};

/**
 * Deletes the current sprint with confirmation
 * Shows confirmation dialog before proceeding with deletion
 */
const deleteSprint = async () => {
  const sprintData = currentSprintData.value;
  if (!sprintData) {
    return;
  }

  modal.confirm({
    content: t('actions.tips.confirmDelete', { name: sprintData.name }),
    async onOk () {
      const sprintId = sprintData.id;
      isLoading.value = true;
      const [error] = await issue.deleteSprint(sprintId);
      isLoading.value = false;
      if (error) {
        return;
      }

      notification.success(t('actions.tips.deleteSuccess'));
      if (props.data?._id) {
        deleteTabPane([props.data._id]);
      }
      refreshSprintList();
    }
  });
};

/**
 * Opens the authorization modal for permission management
 */
const openPermissionModal = () => {
  isAuthorizeModalVisible.value = true;
};

/**
 * Handles permission changes after authorization modal updates
 * Reloads sprint data and refreshes list to reflect permission changes
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
 * Clones the current sprint
 * Creates a copy of the sprint with same configuration
 */
const cloneSprint = async () => {
  const sprintId = currentSprintData.value?.id;
  if (!sprintId) {
    return;
  }

  isLoading.value = true;
  const [error] = await issue.cloneSprint(sprintId);
  isLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.cloneSuccess'));
  refreshSprintList();
};

/**
 * Copies sprint link to clipboard
 * Generates shareable URL for the current sprint
 */
const copySprintLink = () => {
  const sprintId = currentSprintData.value?.id;
  if (!sprintId) {
    return;
  }

  const sprintUrl = `${window.location.origin}/issue#${IssueMenuKey.SPRINT}?id=${sprintId}`;
  toClipboard(sprintUrl).then(() => {
    notification.success(t('actions.tips.copyLinkSuccess'));
  }).catch(() => {
    notification.error(t('actions.tips.copyLinkFailed'));
  });
};

/**
 * Refreshes current sprint data
 * Reloads sprint information from server
 */
const refreshSprintData = () => {
  const sprintId = currentSprintData.value?.id;
  if (!sprintId) {
    return;
  }
  loadSprintData(sprintId);
};

/**
 * Closes the current tab and returns to sprint list
 */
const closeCurrentTab = () => {
  if (props.data?._id) {
    deleteTabPane([props.data._id]);
  }
};

/**
 * Loads enum options for workload evaluation methods
 * Populates radio group options from EvalWorkloadMethod enum
 */
const loadWorkloadMethodOptions = () => {
  workloadMethodOptions.value = enumUtils.enumToMessages(EvalWorkloadMethod);
};

/**
 * Loads user permissions for the current sprint
 * Determines which actions the user can perform based on their role and permissions
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
  const [error, response] = await issue.getCurrentUserSprintAuth(sprintId, authParams);
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
 * Loads sprint detail data from server
 * Fetches complete sprint information and populates form
 * @param {string} sprintId - The ID of the sprint to load
 */
const loadSprintData = async (sprintId: string) => {
  isLoading.value = true;
  const [error, response] = await issue.getSprintDetail(sprintId);
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
 * Populates form with sprint data or sets default values
 * Handles both new sprint creation and existing sprint editing scenarios
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
 * Validates date range selection
 * Ensures both start and end dates are selected for sprint planning
 * @param {Rule} _validationRule - Form validation rule object (unused)
 * @param {Array} dateRange - Array containing start and end dates
 * @returns {Promise} Validation result promise
 */
const validateDateRange = async (_validationRule: Rule, dateRange: string[]) => {
  if (!dateRange) {
    return Promise.reject(new Error(t('sprint.messages.dateRequired')));
  } else if (!dateRange[0]) {
    return Promise.reject(new Error(t('sprint.messages.startDateRequired')));
  } else if (!dateRange[1]) {
    return Promise.reject(new Error(t('sprint.messages.endDateRequired')));
  } else {
    return Promise.resolve();
  }
};

/**
 * Handles file upload for sprint attachments
 * Uploads file to server and adds to attachment list with size limit
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
    bizKey: UPLOAD_TEST_FILE_KEY
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
 * Removes attachment from the list
 * Deletes file reference from form state by index
 * @param {number} attachmentIndex - Index of attachment to remove
 */
const removeAttachment = (attachmentIndex: number) => {
  formState.value?.attachments?.splice(attachmentIndex, 1);
};

/**
 * Current sprint status value
 * Returns the status enum value for conditional rendering
 */
const currentSprintStatus = computed(() => {
  return currentSprintData.value?.status?.value;
});

/**
 * Determines if component is in edit mode
 * True when editing existing sprint, false when creating new sprint
 */
const isEditMode = computed(() => {
  return !!props.data?.id;
});

/**
 * Determines if form fields should be disabled
 * Disables editing when sprint is in progress or completed
 */
const isFormDisabled = computed(() => {
  const status = currentSprintData.value?.status?.value;
  return !!(currentSprintData.value && status && [
    TaskSprintStatus.IN_PROGRESS,
    TaskSprintStatus.COMPLETED
  ].includes(status as TaskSprintStatus));
});

/**
 * Component initialization and data loading
 * Sets up watchers and loads initial data when component mounts
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
        <span>{{ t('actions.save') }}</span>
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
          <span>{{ t('actions.restart') }}</span>
        </Button>

        <Button
          v-else-if="currentSprintStatus && [TaskSprintStatus.PENDING, TaskSprintStatus.BLOCKED].includes(currentSprintStatus)"
          :disabled="!isAdmin && !userPermissions.includes(TaskSprintPermission.MODIFY_SPRINT)"
          size="small"
          type="default"
          class="flex items-center space-x-1"
          @click="startSprint">
          <Icon icon="icon-kaishi" class="text-3.5" />
          <span>{{ t('actions.start') }}</span>
        </Button>

        <template v-if="currentSprintStatus === TaskSprintStatus.IN_PROGRESS">
          <Button
            :disabled="!isAdmin && !userPermissions.includes(TaskSprintPermission.MODIFY_SPRINT)"
            size="small"
            type="default"
            class="flex items-center space-x-1"
            @click="completeSprint">
            <Icon icon="icon-yiwancheng" class="text-3.5" />
            <span>{{ t('actions.complete') }}</span>
          </Button>

          <Button
            :disabled="!isAdmin && !userPermissions.includes(TaskSprintPermission.MODIFY_SPRINT)"
            size="small"
            type="default"
            class="flex items-center space-x-1"
            @click="completeSprint">
            <Icon icon="icon-zusai" class="text-3.5" />
            <span>{{ t('actions.block') }}</span>
          </Button>
        </template>

        <Button
          :disabled="!isAdmin && !userPermissions.includes(TaskSprintPermission.DELETE_SPRINT)"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="deleteSprint">
          <Icon icon="icon-qingchu" class="text-3.5" />
          <span>{{ t('actions.delete') }}</span>
        </Button>

        <Button
          :disabled="!isAdmin && !userPermissions.includes(TaskSprintPermission.GRANT)"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="openPermissionModal">
          <Icon icon="icon-quanxian1" class="text-3.5" />
          <span>{{ t('actions.permission') }}</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="cloneSprint">
          <Icon icon="icon-fuzhizujian2" class="text-3.5" />
          <span>{{ t('actions.clone') }}</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="copySprintLink">
          <Icon icon="icon-fuzhi" class="text-3.5" />
          <span>{{ t('actions.copyLink') }}</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="refreshSprintData">
          <Icon icon="icon-shuaxin" class="text-3.5" />
          <span>{{ t('actions.refresh') }}</span>
        </Button>
      </template>
      <Button
        type="default"
        size="small"
        class="flex items-center"
        @click="closeCurrentTab">
        <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-zhongzhi2" />
        <span>{{ t('actions.cancel') }}</span>
      </Button>
    </div>

    <Form
      ref="formRef"
      :model="formState"
      size="small"
      :labelCol="{ style: { width: '150px' } }"
      class="max-w-242.5"
      layout="horizontal">
      <FormItem
        :label="t('common.name')"
        name="name"
        :rules="{ required: true, message: t('sprint.messages.nameRequired') }">
        <Input
          v-model:value="formState.name"
          size="small"
          :maxlength="100"
          :placeholder="t('sprint.placeholders.inputSprintName')" />
      </FormItem>

      <FormItem
        :label="t('common.planTime')"
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
        :label="t('common.owner')"
        name="ownerId"
        class="relative"
        :rules="{ required: true, message: t('sprint.messages.ownerRequired') }">
        <SelectUser
          v-model:value="formState.ownerId"
          size="small"
          :placeholder="t('common.placeholders.selectOwner')"
          :defaultOptions="ownerSelectionOptions"
          :action="`${TESTER}/project/${props.projectId}/member/user`"
          :maxlength="80" />
        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>{{ t('sprint.tips.ownerTip') }}</div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        :label="t('sprint.columns.issuePrefix')"
        name="taskPrefix"
        class="relative">
        <Input
          v-model:value="formState.taskPrefix"
          size="small"
          :readonly="!!currentSprintData?.id"
          :disabled="!!currentSprintData?.id"
          :maxlength="40"
          :placeholder="t('sprint.placeholders.inputIssuePrefix')" />
        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>{{ t('sprint.tips.issuePrefixTip') }}</div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem :label="t('common.evalWorkloadMethod')" name="evalWorkloadMethod">
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
            <div>{{ t('sprint.tips.evalWorkloadMethodTip') }}</div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem :label="t('common.attachment')">
        <div class="flex items-center">
          <Upload
            :fileList="[]"
            name="file"
            :customRequest="handleFileUpload">
            <a class="text-theme-special text-theme-text-hover text-3 flex items-center leading-5 h-5 mt-0.5">
              <Icon icon="icon-lianjie1" class="mr-1" />
              <span class="whitespace-nowrap">{{ t('actions.upload') }}</span>
            </a>
          </Upload>
          <Tooltip :overlayStyle="{ 'max-width': '400px' }">
            <template #title>
              <div class="text-3 text-theme-sub-content leading-4 break-all">
                {{ t('sprint.tips.attachmentTip') }}
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
        <TabPane key="acceptanceCriteria" :tab="t('sprint.columns.acceptanceCriteria')">
          <FormItem
            class="!mb-5"
            name="acceptanceCriteria"
            :rules="[{validator: validateRichTextMaxLength}]">
            <RichEditor
              ref="acceptanceCriteriaEditorRef"
              v-model:value="formState.acceptanceCriteria"
              :options="{placeholder: t('sprint.placeholders.inputAcceptanceCriteria')}" />
          </FormItem>
        </TabPane>
        <TabPane key="otherInformation" :tab="t('sprint.columns.otherInformation')">
          <FormItem
            class="!mb-5"
            name="otherInformation"
            :rules="[{validator: validateRichTextMaxLength}]">
            <RichEditor
              ref="otherInformationEditorRef"
              v-model:value="formState.otherInformation"
              :options="{placeholder: t('sprint.placeholders.inputOtherInformation')}" />
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
        :onTips="t('sprint.authorization.onTips')"
        :offTips="t('sprint.authorization.offTips')"
        :title="t('sprint.authorization.title')"
        @change="handlePermissionChange" />
    </AsyncComponent>
  </Spin>
</template>

<style scoped>
:deep(.ant-form-item-label>label::after) {
  margin-right: 10px;
}

:deep(.ant-form-item-label>label) {
  font-weight: 500;
  color: #000;
}

.ant-tabs-small > :deep(.ant-tabs-nav) .ant-tabs-tab {
  padding-top: 0;
}
</style>
