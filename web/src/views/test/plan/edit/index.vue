<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onMounted, ref, watch } from 'vue';
import { Button, Divider, Form, FormItem, Radio, RadioGroup, Switch, TabPane, Tabs, Upload } from 'ant-design-vue';
import {
  AsyncComponent, DatePicker, Icon, Input, modal, notification, SelectUser, Spin, Tooltip
} from '@xcan-angus/vue-ui';
import {
  appContext, EnumMessage, enumUtils, EvalWorkloadMethod, TESTER, toClipboard, upload, utils
} from '@xcan-angus/infra';
import dayjs from 'dayjs';
import { isEqual } from 'lodash-es';
import type { Rule } from 'ant-design-vue/es/form';
import { testPlan, project } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { DATE_TIME_FORMAT, TIME_FORMAT, UPLOAD_TEST_FILE_KEY } from '@/utils/constant';
import { TestMenuKey } from '@/views/test/menu';

import { PlanDetail, PlanEditFormState } from '../types';
import { BasicProps } from '@/types/types';
import { FuncPlanPermission, FuncPlanStatus } from '@/enums/enums';

const { t } = useI18n();

// Props
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

// Async components
const AuthorizeModal = defineAsyncComponent(() => import('@/components/auth/AuthorizeModal.vue'));
const RichEditor = defineAsyncComponent(() => import('@/components/editor/richEditor/index.vue'));
const TesterSelect = defineAsyncComponent(() => import('./TesterSelect.vue'));

// Injected dependencies
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const replaceTabPane = inject<(id: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));

// Computed properties
const isAdmin = computed(() => appContext.isAdmin());
const currentPlanStatus = computed(() => {
  return planDetailData.value?.status?.value;
});
const isEditMode = computed(() => {
  return !!props.data?.id;
});
const isEditDisabled = computed(() => {
  return !!(planDetailData.value && currentPlanStatus.value &&
    [FuncPlanStatus.IN_PROGRESS, FuncPlanStatus.COMPLETED].includes(currentPlanStatus.value));
});

// Reactive data
const formRef = ref();
const loading = ref(false);
const planDetailData = ref<PlanDetail>();
const testerSelectRef = ref();
const activeTabKey = ref('testingObjectives');
const evalWorkloadMethodOptions = ref<EnumMessage<EvalWorkloadMethod>[]>([]);
const reviewFlagVisible = ref(false);
const userPermissions = ref<string[]>([]);
const originalFormState = ref<PlanEditFormState>();
const authorizeModalVisible = ref(false);
const projectMembers = ref([]);

// Form state initialization
const defaultStartDate = dayjs().format(DATE_TIME_FORMAT);
const defaultDeadlineDate = dayjs().add(1, 'month').format(DATE_TIME_FORMAT);
const formState = ref<PlanEditFormState>({
  projectId: props.projectId,
  name: '',
  startDate: defaultStartDate,
  deadlineDate: defaultDeadlineDate,
  ownerId: String(props.userInfo?.id || ''),
  testerResponsibilities: new Map(),
  testingObjectives: '',
  testingScope: '',
  otherInformation: '',
  acceptanceCriteria: '',
  attachments: [],
  review: true,
  casePrefix: '',
  evalWorkloadMethod: EvalWorkloadMethod.STORY_POINT,
  date: [defaultStartDate, defaultDeadlineDate]
});

// Rich editor references
const objectiveRichRef = ref();
const scopeRichRef = ref();
const criteriaRichRef = ref();
const infoRichRef = ref();

/**
 * Validates the date range selection for the plan.
 * Ensures both start and deadline dates are selected.
 */
const validateDateRange = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error(t('common.placeholders.selectPlanTime')));
  } else if (!value[0]) {
    return Promise.reject(new Error(t('common.placeholders.selectStartDate')));
  } else if (!value[1]) {
    return Promise.reject(new Error(t('common.placeholders.selectDeadline')));
  } else {
    return Promise.resolve();
  }
};

/**
 * Validates tester selection and responsibilities.
 * Ensures at least one tester is selected and no duplicate personnel.
 */
const validateTesterSelection = async () => {
  if (Object.keys(formState.value.testerResponsibilities).length === 0) {
    return Promise.reject(new Error(t('common.placeholders.selectTester')));
  }
  if (testerSelectRef.value.validate()) {
    return Promise.resolve();
  } else {
    return Promise.reject(new Error(t('testPlan.messages.duplicatePersonnel')));
  }
};

/**
 * Validates maximum character length for rich text fields.
 * Ensures content does not exceed 2000 characters.
 */
const validateRichTextMaxLength = async (value) => {
  if (value.field === 'testingObjectives') {
    if (objectiveRichRef.value && objectiveRichRef.value.getLength() >= 2000) {
      Promise.reject(new Error(t('testPlan.messages.charLimit2000')));
    }
  }
  if (value.field === 'otherInformation') {
    if (infoRichRef.value && infoRichRef.value.getLength() >= 2000) {
      Promise.reject(new Error(t('testPlan.messages.charLimit2000')));
    }
  }
  if (value.field === 'acceptanceCriteria') {
    if (criteriaRichRef.value && criteriaRichRef.value.getLength() >= 2000) {
      Promise.reject(new Error(t('testPlan.messages.charLimit2000')));
    }
  }
  if (value.field === 'testingScope') {
    if (scopeRichRef.value && scopeRichRef.value.getLength() >= 2000) {
      Promise.reject(new Error(t('testPlan.messages.charLimit2000')));
    }
  }
  return Promise.resolve();
};

/**
 * Handles tester selection change event.
 * Updates the form state with new tester responsibilities.
 */
const handleTesterChange = (data) => {
  formState.value.testerResponsibilities = data;
};

/**
 * Handles file upload for plan attachments.
 * Validates file count limit and uploads to server.
 */
const handleFileUpload = async (file) => {
  if (loading.value) {
    return;
  }

  const attachments = formState.value.attachments || [];
  if (attachments.length >= 10) {
    return;
  }

  loading.value = true;
  const [error, { data = [] }] = await upload(file.file, { bizKey: UPLOAD_TEST_FILE_KEY });
  loading.value = false;
  if (error) {
    return;
  }

  if (data && data.length > 0) {
    const newAttachments = data?.map(item => ({ name: item.name, url: item.url }));
    if (formState.value.attachments) {
      formState.value.attachments.push(...newAttachments);
    } else {
      formState.value.attachments = newAttachments || [];
    }
  }
};

/**
 * Removes an attachment from the form state.
 */
const removeAttachment = (index: number) => {
  formState.value?.attachments?.splice(index, 1);
};

/**
 * Handles review flag change with confirmation dialog.
 * Shows confirmation when disabling review.
 */
const handleReviewFlagChange = (checked) => {
  if (checked) {
    formState.value.review = true;
    return;
  }
  reviewFlagVisible.value = true;
};

/**
 * Confirms disabling the review flag.
 */
const confirmDisableReview = () => {
  formState.value.review = false;
  reviewFlagVisible.value = false;
};

/**
 * Cancels disabling the review flag.
 */
const cancelDisableReview = () => {
  reviewFlagVisible.value = false;
};

/**
 * Prepares form parameters for API submission.
 * Processes form data and removes unnecessary fields.
 */
const prepareFormParams = () => {
  const params: PlanEditFormState = { ...formState.value };
  if (planDetailData.value?.id) {
    params.id = planDetailData.value.id;
  }

  const dateRange = formState.value.date;
  if (dateRange && dateRange[0] && dateRange[1]) {
    params.startDate = dateRange[0];
    params.deadlineDate = dateRange[1];
  }

  if (!params.attachments?.length) {
    params.attachments = [];
  }

  if (!params.casePrefix) {
    delete params.casePrefix;
  }

  delete params.date;

  return params;
};

/**
 * Refreshes the plan list after successful operations.
 */
const refreshPlanList = () => {
  nextTick(() => {
    updateTabPane({ _id: 'planList', notify: utils.uuid() });
  });
};

/**
 * Handles plan update operation.
 * Compares form state changes and updates the plan.
 */
const handlePlanUpdate = async () => {
  const hasChanges = isEqual(originalFormState.value, formState.value);
  if (hasChanges) {
    return;
  }

  const params = prepareFormParams();
  loading.value = true;
  const [error] = await testPlan.putPlan(params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.saveSuccess'));

  const id = params.id;
  const name = params.name;
  updateTabPane({ _id: id, name });
  if (planDetailData.value) {
    planDetailData.value.name = name;
  }
};

/**
 * Handles plan creation operation.
 * Creates a new plan and updates the tab pane.
 */
const handlePlanCreation = async () => {
  const params = prepareFormParams();
  loading.value = true;
  const [error, res] = await testPlan.addPlan(params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.addSuccess'));

  const currentTabId = props.data?._id;
  const newPlanId = res?.data?.id;
  const name = params.name;
  if (newPlanId && currentTabId) {
    replaceTabPane(currentTabId, { _id: newPlanId, uiKey: newPlanId, name, data: { _id: newPlanId, id: newPlanId } });
  }
};

/**
 * Handles form submission with validation.
 * Routes to appropriate create or update operation.
 */
const handleFormSubmit = async () => {
  formRef.value.validate().then(async () => {
    if (!isEditMode.value) {
      await handlePlanCreation();
    } else {
      await handlePlanUpdate();
    }

    refreshPlanList();
  }).catch((error) => {
    if (error.errorFields.length === 1 && error.errorFields[0].name[0] === 'testingScope') {
      activeTabKey.value = 'testingScope';
    }
  });
};

/**
 * Starts the plan execution.
 * Changes plan status to IN_PROGRESS.
 */
const startPlan = async () => {
  const planId = planDetailData.value?.id;
  if (!planId) {
    return;
  }

  loading.value = true;
  const [error] = await testPlan.startPlan(planId!);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.startSuccess'));
  await loadPlanData(planId);
  refreshPlanList();
};

/**
 * Completes the plan execution.
 * Changes plan status to COMPLETED.
 */
const completePlan = async () => {
  const planId = planDetailData.value?.id;
  if (!planId) {
    return;
  }

  loading.value = true;
  const [error] = await testPlan.endPlan(planId!);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.completeSuccess'));
  await loadPlanData(planId);
  refreshPlanList();
};

/**
 * Deletes the plan with confirmation dialog.
 * Removes the plan and updates the tab pane.
 */
const deletePlan = async () => {
  const planData = planDetailData.value;
  if (!planData) {
    return;
  }

  modal.confirm({
    content: t('actions.tips.confirmDelete', { name: planData.name }),
    async onOk () {
      const planId = planData.id;
      loading.value = true;
      const [error] = await testPlan.deletePlan(planId!);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success(t('actions.tips.deleteSuccess'));
      deleteTabPane([planId]);
      refreshPlanList();
    }
  });
};

/**
 * Opens the authorization modal for plan permissions.
 */
const openAuthorizationModal = () => {
  authorizeModalVisible.value = true;
};

/**
 * Handles authorization changes.
 * Reloads plan data and refreshes the list.
 */
const handleAuthorizationChange = async () => {
  const planId = planDetailData.value?.id;
  if (!planId) {
    return;
  }

  await loadPlanData(planId);
  refreshPlanList();
};

/**
 * Clones the current plan.
 * Creates a copy of the plan with a new ID.
 */
const clonePlan = async () => {
  const planId = planDetailData.value?.id;
  if (!planId) {
    return;
  }

  loading.value = true;
  const [error] = await testPlan.clonePlan(planId!);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.cloneSuccess'));
  refreshPlanList();
};

/**
 * Resets test results for the plan.
 * Clears all test execution results.
 */
const resetTestResults = async () => {
  const planId = planDetailData.value?.id;
  if (!planId) {
    return;
  }

  const params = { ids: [planId] };
  loading.value = true;
  const [error] = await testPlan.resetCaseResult(params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('testPlan.messages.planResetTestSuccess'));
  await loadPlanData(planId);
  refreshPlanList();
};

/**
 * Resets review results for the plan.
 * Clears all review results.
 */
const resetReviewResults = async () => {
  const planId = planDetailData.value?.id;
  if (!planId) {
    return;
  }

  const params = { ids: [planId] };
  loading.value = true;
  const [error] = await testPlan.resetCaseReview(params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('testPlan.messages.planResetReviewSuccess'));
  await loadPlanData(planId);
  refreshPlanList();
};

/**
 * Copies the plan link to clipboard.
 * Shows success or error notification.
 */
const copyPlanLink = () => {
  const planId = planDetailData.value?.id;
  if (!planId) {
    return;
  }

  toClipboard(window.location.origin + `/test#${TestMenuKey.PLANS}?id=${planId}`).then(() => {
    notification.success(t('actions.tips.copyLinkSuccess'));
  }).catch(() => {
    notification.error(t('actions.tips.copyLinkFailed'));
  });
};

/**
 * Refreshes the current plan data.
 */
const refreshPlanData = () => {
  const planId = planDetailData.value?.id;
  if (!planId) {
    return;
  }

  loadPlanData(planId);
};

/**
 * Loads plan detail data from the API.
 * Updates form state and tab pane title.
 */
const loadPlanData = async (planId: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await testPlan.getPlanDetail(planId);
  loading.value = false;
  if (error) {
    return;
  }

  const planData = res?.data as PlanDetail;
  if (!planData) {
    return;
  }

  planDetailData.value = planData;
  initializeFormData(planData);

  const name = planData.name;
  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: planId });
  }
};

/**
 * Initializes form data from plan detail or creates default values.
 * Handles both edit and create modes.
 */
const initializeFormData = (planData: PlanDetail) => {
  reviewFlagVisible.value = false;
  if (!planData) {
    const startDate = dayjs().format(DATE_TIME_FORMAT);
    const deadlineDate = dayjs().add(1, 'month').format(DATE_TIME_FORMAT);
    formState.value = {
      projectId: props.projectId,
      name: '',
      startDate,
      deadlineDate,
      ownerId: String(props.userInfo?.id || ''),
      testerResponsibilities: new Map(),
      testingObjectives: '',
      testingScope: '',
      otherInformation: '',
      acceptanceCriteria: '',
      attachments: [],
      review: true,
      casePrefix: '',
      evalWorkloadMethod: EvalWorkloadMethod.STORY_POINT,
      date: [startDate, deadlineDate]
    };
    return;
  }

  const {
    casePrefix = '',
    projectId = '',
    evalWorkloadMethod,
    name = '',
    ownerId = '',
    attachments = '',
    startDate = '',
    deadlineDate = '',
    review = false,
    testingObjectives = '',
    testingScope = '',
    otherInformation = '',
    acceptanceCriteria = '',
    testerResponsibilities = new Map()
  } = planData;

  formState.value.casePrefix = casePrefix;
  formState.value.projectId = projectId;
  formState.value.deadlineDate = deadlineDate;
  formState.value.evalWorkloadMethod = evalWorkloadMethod?.value || '';
  formState.value.name = name;
  formState.value.ownerId = ownerId;
  formState.value.review = review;
  formState.value.startDate = startDate;
  formState.value.attachments = attachments || [];
  formState.value.date = [startDate, deadlineDate];

  formState.value.testingObjectives = parseJsonValue(testingObjectives);
  formState.value.testingScope = parseJsonValue(testingScope);
  formState.value.otherInformation = parseJsonValue(otherInformation);
  formState.value.acceptanceCriteria = parseJsonValue(acceptanceCriteria);
  formState.value.testerResponsibilities = testerResponsibilities || new Map();
  originalFormState.value = JSON.parse(JSON.stringify(formState.value));
};

/**
 * Parses JSON value for rich text fields.
 * Handles both JSON strings and plain text.
 */
const parseJsonValue = (value) => {
  if (!value) {
    return undefined;
  }
  try {
    const result = JSON.parse(value);
    if (typeof result === 'object') {
      return value;
    }
    return JSON.stringify([{ insert: value }]);
  } catch {
    return JSON.stringify([{ insert: value }]);
  }
};

/**
 * Loads evaluation workload method options.
 */
const loadWorkloadMethodOptions = () => {
  evalWorkloadMethodOptions.value = enumUtils.enumToMessages(EvalWorkloadMethod);
};

/**
 * Loads user permissions for the specified plan.
 * For admin users, loads all available permissions.
 */
const loadUserPermissions = async (planId: string) => {
  if (isAdmin.value) {
    userPermissions.value = enumUtils.getEnumValues(FuncPlanPermission);
    return;
  }

  const params = {
    admin: true
  };
  loading.value = true;
  const [error, res] = await testPlan.getCurrentAuthByPlanId(planId, params);
  loading.value = false;
  if (error) {
    return;
  }

  userPermissions.value = (res?.data?.permissions || []).map(item => item.value);
};

/**
 * Loads project members for selection.
 * Transforms member data for dropdown options.
 */
const loadProjectMembers = async () => {
  const [error, res] = await project.getProjectMember(props.projectId);
  if (error) {
    return;
  }

  const memberData = res?.data || [];
  projectMembers.value = (memberData || []).map(member => {
    return {
      ...member,
      label: member.fullName,
      value: member.id
    };
  });
};

/**
 * Cancels the edit operation and closes the tab.
 */
const cancelEdit = async () => {
  if (props.data?._id) {
    deleteTabPane([props.data._id]);
  }
};

// Lifecycle hooks
onMounted(() => {
  loadWorkloadMethodOptions();
  loadProjectMembers();

  watch(() => props.data, async (newValue, oldValue) => {
    const planId = newValue?.id;
    if (!planId) {
      return;
    }

    const previousPlanId = oldValue?.id;
    if (planId === previousPlanId) {
      return;
    }

    await loadUserPermissions(planId);
    await loadPlanData(planId);
  }, { immediate: true });
});
</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="flex items-center space-x-2.5 mb-5">
      <Button
        :disabled="!isAdmin && !userPermissions.includes(FuncPlanPermission.MODIFY_PLAN)"
        type="primary"
        size="small"
        class="flex items-center space-x-1"
        @click="handleFormSubmit">
        <Icon icon="icon-dangqianxuanzhong" class="text-3.5" />
        <span>{{ t('actions.save') }}</span>
      </Button>

      <template v-if="isEditMode">
        <Button
          v-if="currentPlanStatus === FuncPlanStatus.COMPLETED"
          :disabled="!isAdmin && !userPermissions.includes(FuncPlanPermission.MODIFY_PLAN)"
          size="small"
          type="default"
          class="flex items-center space-x-1"
          @click="startPlan">
          <Icon icon="icon-kaishi" class="text-3.5" />
          <span>{{ t('actions.restart') }}</span>
        </Button>

        <Button
          v-else-if="currentPlanStatus && [FuncPlanStatus.PENDING, FuncPlanStatus.BLOCKED].includes(currentPlanStatus)"
          :disabled="!isAdmin && !userPermissions.includes(FuncPlanPermission.MODIFY_PLAN)"
          size="small"
          type="default"
          class="flex items-center space-x-1"
          @click="startPlan">
          <Icon icon="icon-kaishi" class="text-3.5" />
          <span>{{ t('actions.start') }}</span>
        </Button>

        <template v-if="currentPlanStatus === FuncPlanStatus.IN_PROGRESS">
          <Button
            :disabled="!isAdmin && !userPermissions.includes(FuncPlanPermission.MODIFY_PLAN)"
            size="small"
            type="default"
            class="flex items-center space-x-1"
            @click="completePlan">
            <Icon icon="icon-yiwancheng" class="text-3.5" />
            <span>{{ t('actions.complete') }}</span>
          </Button>

          <Button
            :disabled="!isAdmin && !userPermissions.includes(FuncPlanPermission.MODIFY_PLAN)"
            size="small"
            type="default"
            class="flex items-center space-x-1"
            @click="completePlan">
            <Icon icon="icon-zusai" class="text-3.5" />
            <span>{{ t('actions.block') }}</span>
          </Button>
        </template>

        <Button
          :disabled="!isAdmin && !userPermissions.includes(FuncPlanPermission.DELETE_PLAN)"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="deletePlan">
          <Icon icon="icon-qingchu" class="text-3.5" />
          <span>{{ t('actions.delete') }}</span>
        </Button>

        <Button
          :disabled="!isAdmin && !userPermissions.includes(FuncPlanPermission.GRANT)"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="openAuthorizationModal">
          <Icon icon="icon-quanxian1" class="text-3.5" />
          <span>{{ t('actions.permission') }}</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="clonePlan">
          <Icon icon="icon-fuzhizujian2" class="text-3.5" />
          <span>{{ t('actions.clone') }}</span>
        </Button>

        <Button
          :disabled="!isAdmin && !userPermissions.includes(FuncPlanPermission.RESET_TEST_RESULT)"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="resetTestResults">
          <Icon icon="icon-zhongzhiceshijieguo" class="text-3.5" />
          <span>{{ t('testPlan.actions.resetTest') }}</span>
        </Button>

        <Button
          :disabled="!isAdmin && !userPermissions.includes(FuncPlanPermission.RESET_REVIEW_RESULT)"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="resetReviewResults">
          <Icon icon="icon-zhongzhipingshenjieguo" class="text-3.5" />
          <span>{{ t('testPlan.actions.resetReview') }}</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="copyPlanLink">
          <Icon icon="icon-fuzhi" class="text-3.5" />
          <span>{{ t('actions.copyLink') }}</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="refreshPlanData">
          <Icon icon="icon-shuaxin" class="text-3.5" />
          <span>{{ t('actions.refresh') }}</span>
        </Button>
      </template>

      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="cancelEdit">
        <Icon icon="icon-zhongzhi2" class="text-3.5" />
        <span>{{ t('actions.cancel') }}</span>
      </Button>
    </div>

    <Form
      ref="formRef"
      :model="formState"
      :labelCol="{ style: { width: '150px' } }"
      class="max-w-242.5"
      size="small"
      layout="horizontal">
      <FormItem
        :label="t('testPlan.columns.planName')"
        name="name"
        :rules="{ required: true, message: t('testPlan.placeholders.enterPlanName') }">
        <Input
          v-model:value="formState.name"
          size="small"
          :maxlength="200"
          :placeholder="t('testPlan.placeholders.planNamePlaceholder')" />
      </FormItem>

      <FormItem
        :label="t('common.owner')"
        name="ownerId"
        class="relative"
        :rules="{ required: true, message: t('common.placeholders.selectOwner') }">
        <SelectUser
          v-model:value="formState.ownerId"
          size="small"
          :placeholder="t('common.placeholders.selectOwner')"
          :action="`${TESTER}/project/${props.projectId}/member/user`"
          :maxlength="80" />

        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>
              {{ t('testPlan.tips.ownerTooltip') }}
            </div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 z-10 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        :label="t('common.planTime')"
        name="date"
        :rules="{ required: true, validator: validateDateRange, trigger: 'change' }">
        <DatePicker
          v-model:value="formState.date"
          format="YYYY-MM-DD HH:mm:ss"
          :showNow="false"
          :showTime="{ format: TIME_FORMAT }"
          :placeholder="[t('common.startDate'), t('common.deadlineDate')]"
          type="date-range"
          size="small"
          class="w-full" />

        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>{{ t('testPlan.tips.timePlanTooltip') }}</div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        :label="t('common.tester')"
        name="testerResponsibilities"
        class="relative"
        :rules="[{ required: true }, { validator: validateTesterSelection }]">
        <TesterSelect
          ref="testerSelectRef"
          class="inline-block w-full"
          :value="formState.testerResponsibilities"
          :members="planDetailData?.members"
          :membersOptions="projectMembers"
          @change="handleTesterChange" />

        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>
              {{ t('testPlan.tips.testersTooltip') }}
            </div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        :label="t('common.review')"
        required
        class="flex-1"
        name="review">
        <Tooltip
          :visible="reviewFlagVisible"
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '430px' }">
          <template #title>
            <div>
              <span class="mr-2">{{ t('testPlan.messages.reviewCloseConfirm') }}</span>
              <a
                class="text-theme-special"
                @click="confirmDisableReview">{{ t('actions.confirm') }}</a>
              <Divider type="vertical" />
              <a
                class="text-theme-special"
                @click="cancelDisableReview">{{ t('actions.cancel') }}</a>
            </div>
          </template>
          <Switch
            :checked="formState.review"
            :disabled="isEditDisabled"
            size="small"
            @change="handleReviewFlagChange" />
        </Tooltip>

        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>
              {{ t('testPlan.tips.reviewTooltip') }}
            </div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips mt-0.25 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        :label="t('common.evalWorkloadMethod')"
        name="evalWorkloadMethod"
        class="flex-1">
        <RadioGroup
          v-model:value="formState.evalWorkloadMethod"
          :disabled="isEditDisabled"
          class="mt-0.5">
          <Radio
            v-for="item in evalWorkloadMethodOptions"
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
            <div>
              {{ t('testPlan.tips.workloadAssessmentTooltip') }}
            </div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        :label="t('testPlan.columns.casePrefix')"
        name="casePrefix"
        class="relative flex-1">
        <Input
          v-model:value="formState.casePrefix"
          size="small"
          :readonly="!!planDetailData?.id"
          :disabled="!!planDetailData?.id"
          :maxlength="40"
          :placeholder="t('testPlan.placeholders.casePrefixPlaceholder')" />

        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>{{ t('testPlan.tips.casePrefixTooltip') }}</div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem :label="t('common.attachment')">
        <div class="flex items-center mt-0.5">
          <Upload
            :fileList="[]"
            name="file"
            accept=".jpg,.bmp,.png,.gif,.txt,.docx,.jpeg,.rar,.zip,.doc,.xlsx,.xls,.pdf"
            :customRequest="handleFileUpload">
            <a class="text-theme-special text-theme-text-hover text-3 flex items-center leading-5 h-5 mt-0.5">
              <Icon icon="icon-lianjie1" class="mr-1" />
              <span class="whitespace-nowrap">{{ t('actions.upload') }}</span>
            </a>
          </Upload>

          <Tooltip :overlayStyle="{ 'max-width': '400px' }">
            <template #title>
              <div class="text-3 text-theme-sub-content leading-4 break-all">
                {{ t('testPlan.tips.attachmentsTooltip') }}
              </div>
            </template>
            <Icon icon="icon-tishi1" class="text-tips ml-1 -mt-0.25 text-3.5 cursor-pointer" />
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

      <Tabs
        v-model:activeKey="activeTabKey"
        size="small"
        class="pl-5 planEditTab">
        <TabPane key="testingObjectives" forceRender>
          <template #tab>
            <div class="text-right pr-2">
              <span>{{ t('testPlan.columns.testingObjectives') }}</span>
            </div>
          </template>

          <FormItem
            label=""
            class="!mb-5"
            name="testingObjectives"
            :rules="[{validator: validateRichTextMaxLength}]">
            <RichEditor
              ref="objectiveRichRef"
              v-model:value="formState.testingObjectives"
              :options="{placeholder: t('testPlan.placeholders.testingObjectivesPlaceholder')}" />
          </FormItem>
        </TabPane>

        <TabPane key="testingScope" forceRender>
          <template #tab>
            <span>{{ t('testPlan.columns.testingScope') }}</span>
          </template>

          <FormItem
            label=""
            name="testingScope"
            class="!mb-5"
            :rules="[{validator: validateRichTextMaxLength}]">
            <RichEditor
              ref="scopeRichRef"
              v-model:value="formState.testingScope"
              :options="{placeholder: t('testPlan.placeholders.testingScopePlaceholder')}" />
          </FormItem>
        </TabPane>

        <TabPane
          key="acceptanceCriteria"
          :tab="t('testPlan.columns.acceptanceCriteria')"
          forceRender>
          <FormItem
            label=""
            name="acceptanceCriteria"
            :rules="[{validator: validateRichTextMaxLength}]">
            <RichEditor
              ref="criteriaRichRef"
              v-model:value="formState.acceptanceCriteria"
              :options="{placeholder: t('testPlan.placeholders.acceptanceCriteriaPlaceholder')}" />
          </FormItem>
        </TabPane>

        <TabPane
          key="otherInformation"
          :tab="t('testPlan.columns.otherInformation')"
          forceRender>
          <FormItem
            label=""
            name="otherInformation"
            :rules="[{validator: validateRichTextMaxLength}]">
            <RichEditor
              ref="infoRichRef"
              v-model:value="formState.otherInformation"
              :options="{placeholder: t('testPlan.placeholders.otherInformationPlaceholder')}" />
          </FormItem>
        </TabPane>
      </Tabs>
    </Form>

    <AsyncComponent :visible="authorizeModalVisible">
      <AuthorizeModal
        v-model:visible="authorizeModalVisible"
        enumKey="FuncPlanPermission"
        :appId="String(props.appInfo?.id || '')"
        :listUrl="`${TESTER}/func/plan/auth?planId=${planDetailData?.id}`"
        :delUrl="`${TESTER}/func/plan/auth`"
        :addUrl="`${TESTER}/func/plan/${planDetailData?.id}/auth`"
        :updateUrl="`${TESTER}/func/plan/auth`"
        :enabledUrl="`${TESTER}/func/plan/${planDetailData?.id}/auth/enabled`"
        :initStatusUrl="`${TESTER}/func/plan/${planDetailData?.id}/auth/status`"
        :onTips="t('testPlan.editForm.permissionModal.onTips')"
        :offTips="t('testPlan.editForm.permissionModal.offTips')"
        :title="t('testPlan.editForm.permissionModal.title')"
        @change="handleAuthorizationChange" />
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
