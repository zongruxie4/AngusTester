<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Form, FormItem, TabPane, Tabs, Upload } from 'ant-design-vue';
import {
  AsyncComponent, Icon, Input, modal, notification, ReviewStatus, Select, Spin, Table, Tooltip, SelectUser
} from '@xcan-angus/vue-ui';
import {
  EnumMessage, EvalWorkloadMethod, utils, TESTER, enumUtils, upload, duration, appContext
} from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { debounce } from 'throttle-debounce';

// API and type imports
import { func, funcPlan, project } from '@/api/tester';
import { FuncPlanStatus, FuncPlanPermission } from '@/enums/enums';
import { BasicProps } from '@/types/types';
import { ReviewEditState, ReviewCaseInfo, ReviewDetail } from '../types';

// Component imports
import RichEditor from '@/components/richEditor/index.vue';
const SelectCaseModal = defineAsyncComponent(() => import('./SelectCaseModal.vue'));

// Props and Context
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

const { t } = useI18n();

// Dependency Injection
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const replaceTabPane = inject<(id: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));

// Reactive State
const isAdmin = computed(() => appContext.isAdmin());
const reviewId = ref<string>();
const formRef = ref();
const selectModalVisible = ref(false);
const searchKeywords = ref<string>();
const loading = ref(false);
const dataSource = ref<ReviewDetail>();
const activeTabKey = ref('funcCase');
const evalWorkloadMethodOptions = ref<EnumMessage<EvalWorkloadMethod>[]>([]);
const reviewFlagVisible = ref(false);
const permissions = ref<string[]>([]);
const oldFormState = ref<ReviewEditState>();

// Form State
const formState = ref<ReviewEditState>({
  name: '',
  planId: undefined,
  description: '',
  ownerId: props.userInfo?.id,
  attachments: [],
  caseIds: [],
  participantIds: []
});

/**
 * Handles file upload for review attachments
 * <p>
 * Validates file count limit and uploads file to server
 * <p>
 * Updates form state with uploaded file information
 */
const handleFileUpload = async (file) => {
  if (loading.value) {
    return;
  }

  const currentAttachments = formState.value.attachments || [];
  if (currentAttachments.length >= 10) {
    return;
  }

  loading.value = true;
  const [error, { data = [] }] = await upload(file.file, { bizKey: 'angusTesterCaseAttachments' });
  loading.value = false;
  if (error) {
    return;
  }

  if (data && data.length > 0) {
    const uploadedFiles = data?.map(item => ({ name: item.name, url: item.url }));
    if (formState.value.attachments) {
      formState.value.attachments.push(...uploadedFiles);
    } else {
      formState.value.attachments = uploadedFiles;
    }
  }
};

/**
 * Removes attachment file from form state
 * <p>
 * Deletes file at specified index from attachments array
 */
const removeAttachmentFile = (index: number) => {
  formState.value?.attachments?.splice(index, 1);
};

/**
 * Prepares form parameters for API submission
 * <p>
 * Maps case list to case IDs and cleans up empty optional fields
 * <p>
 * Returns sanitized parameters ready for API calls
 */
const prepareFormParameters = () => {
  const params: ReviewEditState = {
    ...formState.value,
    caseIds: caseList.value.map(item => item.id)
  };

  if (dataSource.value?.id) {
    params.id = dataSource.value.id;
  }

  if (!params.attachments?.length) {
    delete params.attachments;
  }

  if (!params.description) {
    delete params.description;
  }

  delete params.date;

  return params;
};

/**
 * Refreshes the review list in parent component
 * <p>
 * Triggers tab pane update with unique notification ID
 */
const refreshReviewList = () => {
  nextTick(() => {
    updateTabPane({ _id: 'reviewList', notify: utils.uuid() });
  });
};

/**
 * Handles review update operation
 * <p>
 * Validates form changes and submits update request
 * <p>
 * Updates UI state and shows success notification
 */
const handleReviewUpdate = async () => {
  const hasChanges = isEqual(oldFormState.value, formState.value);
  if (hasChanges) {
    return;
  }

  const params = prepareFormParameters();
  delete params.planId;
  loading.value = true;
  const [error] = await func.putReview(params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.modifySuccess'));

  const reviewId = params.id;
  const reviewName = params.name;
  updateTabPane({ _id: reviewId, name: reviewName });
  if (dataSource.value) {
    dataSource.value.name = reviewName;
  }
};

/**
 * Handles review creation operation
 * <p>
 * Submits new review data and updates tab navigation
 * <p>
 * Shows success notification and replaces current tab
 */
const handleReviewCreation = async () => {
  const params = prepareFormParameters();
  loading.value = true;
  const [error, response] = await func.addReview(params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.addSuccess'));

  const currentTabId = props.data?._id;
  const newReviewId = response?.data?.id;
  const reviewName = params.name;
  replaceTabPane(currentTabId, {
    _id: newReviewId,
    uiKey: newReviewId,
    name: reviewName,
    data: { _id: newReviewId, id: newReviewId }
  });
};

// Form Validation
const descRichRef = ref();

/**
 * Validates description field character limit
 * <p>
 * Checks if description exceeds maximum allowed characters
 * <p>
 * Returns promise rejection if limit exceeded
 */
const validateDescription = async () => {
  if (descRichRef.value && descRichRef.value.getLength() > 2000) {
    return Promise.reject(new Error(t('caseReview.editForm.charLimitExceeded')));
  }
  return Promise.resolve();
};

/**
 * Handles form submission for both create and update operations
 * <p>
 * Validates form data and calls appropriate handler based on edit mode
 * <p>
 * Refreshes review list after successful operation
 */
const handleFormSubmit = async () => {
  formRef.value.validate().then(async () => {
    if (!isEditMode.value) {
      await handleReviewCreation();
    } else {
      await handleReviewUpdate();
    }
    refreshReviewList();
  });
};

/**
 * Handles review deletion with confirmation
 * <p>
 * Shows confirmation modal and deletes review if confirmed
 * <p>
 * Updates UI state and shows success notification
 */
const handleReviewDeletion = async () => {
  const reviewData = dataSource.value;
  if (!reviewData) {
    return;
  }

  modal.confirm({
    content: t('caseReview.editForm.confirmDeleteReview', { name: reviewData.name }),
    async onOk () {
      const reviewId = reviewData.id;
      loading.value = true;
      const [error] = await func.deleteReview(reviewId);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success(t('caseReview.editForm.reviewDeletedSuccess'));
      deleteTabPane([reviewId]);
      refreshReviewList();
    }
  });
};

/**
 * Handles form cancellation
 * <p>
 * Closes current tab and returns to previous view
 */
const handleFormCancel = () => {
  deleteTabPane([props.data._id]);
};

/**
 * Loads review detail data from server
 * <p>
 * Fetches review information and populates form state
 * <p>
 * Updates tab pane with review name
 */
const loadReviewDetail = async (reviewId: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, response] = await func.getReviewDetail(reviewId);
  loading.value = false;
  if (error) {
    return;
  }

  const reviewData = response?.data as ReviewDetail;
  if (!reviewData) {
    return;
  }
  await loadPermissions(reviewData.planId);

  dataSource.value = reviewData;
  populateFormWithData(reviewData);

  const reviewName = reviewData.name;
  if (reviewName && typeof updateTabPane === 'function') {
    updateTabPane({ name: reviewName, _id: reviewId });
  }
};

/**
 * Populates form state with review data
 * <p>
 * Maps review data to form fields and initializes state
 * <p>
 * Handles participants and attachments data mapping
 */
const populateFormWithData = (reviewData: ReviewDetail) => {
  reviewFlagVisible.value = false;
  if (!reviewData) {
    formState.value = {
      name: '',
      planId: '',
      description: '',
      ownerId: props.userInfo?.id,
      attachments: [],
      caseIds: [],
      participantIds: []
    };
    return;
  }

  const {
    name = '',
    description = '',
    ownerId = '',
    attachments = '',
    planId,
    caseIds,
    participants = []
  } = reviewData;

  formState.value.description = description;
  formState.value.name = name;
  formState.value.ownerId = ownerId;
  formState.value.caseIds = caseIds;
  formState.value.planId = planId;
  formState.value.participantIds = participants.map(participant => participant.id);

  participants.forEach(participant => {
    if (!members.value.find(member => member.id === participant.id)) {
      members.value.push({
        ...participant,
        value: participant.id,
        label: participant.fullName
      });
    }
  });

  formState.value.attachments = attachments || [];
  oldFormState.value = JSON.parse(JSON.stringify(formState.value));
};

/**
 * Loads enumeration options for form dropdowns
 * <p>
 * Initializes workload method options from enum definitions
 */
const loadEnumerationOptions = () => {
  evalWorkloadMethodOptions.value = enumUtils.enumToMessages(EvalWorkloadMethod);
};

/**
 * Loads user permissions for current plan
 * <p>
 * Fetches permissions based on admin status or plan-specific auth
 * <p>
 * Updates permissions array for UI control
 */
const loadPermissions = async (planId: string) => {
  if (isAdmin.value) {
    permissions.value = enumUtils.getEnumValues(FuncPlanPermission);
    return;
  }

  const params = {
    admin: true
  };
  loading.value = true;
  const [error, response] = await funcPlan.getCurrentAuthByPlanId(planId, params);
  loading.value = false;
  if (error) {
    return;
  }

  permissions.value = (response?.data?.permissions || []).map(permission => permission.value);
};

// Member Management
const members = ref<{id: string; fullName: string; value: string; label: string}[]>([]);

/**
 * Loads project members for form selection
 * <p>
 * Fetches project members and formats for dropdown options
 * <p>
 * Maps member data to required format for select components
 */
const loadProjectMembers = async () => {
  const [error, response] = await project.getProjectMember(props.projectId);
  if (error) {
    return;
  }

  const memberData = response?.data || [];
  members.value = (memberData || []).map(member => {
    return {
      ...member,
      label: member.fullName,
      value: member.id
    };
  });
};

/**
 * Handles plan ID change event
 * <p>
 * Clears case list when plan selection changes
 */
const handlePlanIdChange = () => {
  caseList.value = [];
};

/**
 * Loads review case list from server
 * <p>
 * Fetches cases with optional search filters and pagination
 * <p>
 * Updates case list and pagination state
 */
const loadReviewCaseList = async () => {
  const [error, { data }] = await func.getReviewCaseList({
    reviewId: reviewId.value,
    filters: searchKeywords.value ? [{ value: searchKeywords.value, key: 'caseName', op: 'MATCH' }] : [],
    pageNo: pagination.value.current,
    pageSize: pagination.value.pageSize
  });

  if (error) {
    return;
  }
  caseList.value = data?.list || [];
  pagination.value.total = +data.total || 0;
};

/**
 * Handles search keyword change with debounce
 * <p>
 * Resets pagination and reloads case list when keywords change
 */
const handleSearchKeywordChange = debounce(duration.search, () => {
  if (!reviewId.value) {
    return;
  }
  pagination.value.current = 1;
  loadReviewCaseList();
});

/**
 * Opens case selection modal
 * <p>
 * Shows modal for adding cases to review
 */
const openCaseSelectionModal = () => {
  selectModalVisible.value = true;
};

/**
 * Handles adding cases to review
 * <p>
 * Adds cases to existing review or local case list
 * <p>
 * Closes modal and refreshes case list
 */
const handleAddCasesToReview = async (caseIds: string[], cases: ReviewCaseInfo[]) => {
  if (reviewId.value) {
    const [error] = await func.addReviewCase({
      caseIds: caseIds,
      reviewId: reviewId.value
    });
    if (error) {
      return;
    }
    selectModalVisible.value = false;
    await loadReviewCaseList();
  } else {
    selectModalVisible.value = false;
    caseList.value.push(...cases);
  }
};

// Pagination and Table Configuration
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
});

const isEditMode = computed(() => {
  return !!props.data?.id;
});

const caseList = ref<ReviewCaseInfo[]>([]);

/**
 * Table column configuration for case list
 * <p>
 * Defines display columns and custom renderers for case data
 */
const tableColumns = [
  {
    title: t('common.id'),
    dataIndex: 'caseId',
    customRender: ({ record }) => {
      if (reviewId.value) {
        return record.caseId;
      }
      return record.id;
    }
  },
  {
    title: t('common.name'),
    dataIndex: 'caseName',
    customRender: ({ record }) => {
      if (reviewId.value) {
        return record.caseInfo.name;
      }
      return record.name;
    }
  },
  {
    title: t('common.reviewStatus'),
    dataIndex: 'reviewStatus'
  },
  {
    title: t('common.actions'),
    dataIndex: 'action'
  }
];

/**
 * Handles case deletion with confirmation
 * <p>
 * Shows confirmation modal and removes case from review
 * <p>
 * Handles pagination adjustment for server-side deletion
 */
const handleCaseDeletion = async (caseRecord: ReviewCaseInfo) => {
  if (reviewId.value) {
    modal.confirm({
      title: t('caseReview.editForm.confirmDeleteCase', { name: caseRecord.name }),
      async onOk () {
        const [error] = await func.deleteReviewCase([caseRecord.id]);
        if (error) {
          return;
        }
        if (pagination.value.current !== 1 && caseList.value.length === 1) {
          pagination.value.current -= 1;
        }
        await loadReviewCaseList();
      }
    });
  } else {
    caseList.value = caseList.value.filter(caseItem => caseItem.id !== caseRecord.id);
  }
};

/**
 * Handles pagination change event
 * <p>
 * Updates pagination state and reloads case list
 */
const handlePaginationChange = ({ current, pageSize }) => {
  pagination.value.current = current;
  pagination.value.pageSize = pageSize;
  loadReviewCaseList();
};

/**
 * Component initialization and data loading
 * <p>
 * Loads enumeration options and project members
 * <p>
 * Sets up watchers for data changes
 */
onMounted(async () => {
  loadEnumerationOptions();
  await loadProjectMembers();

  watch(() => props.data, async (newData, oldData) => {
    const newReviewId = newData?.id;
    if (!newReviewId) {
      return;
    }

    const previousReviewId = oldData?.id;
    if (newReviewId === previousReviewId) {
      return;
    }

    reviewId.value = newReviewId;

    await loadReviewDetail(newReviewId);
    await loadReviewCaseList();
  }, { immediate: true });
});
</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="flex items-center space-x-2.5 mb-5">
      <Button
        :disabled="!isAdmin && !permissions.includes(FuncPlanPermission.REVIEW)"
        type="primary"
        size="small"
        class="flex items-center space-x-1"
        @click="handleFormSubmit">
        <Icon icon="icon-dangqianxuanzhong" class="text-3.5" />
        <span>{{ t('actions.save') }}</span>
      </Button>

      <template v-if="isEditMode">
        <Button
          :disabled="!isAdmin && !permissions.includes(FuncPlanPermission.REVIEW)"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="handleReviewDeletion">
          <Icon icon="icon-qingchu" class="text-3.5" />
          <span>{{ t('actions.delete') }}</span>
        </Button>

        <Button
          :disabled="!isAdmin && !permissions.includes(FuncPlanPermission.REVIEW)"
          type="default"
          size="small"
          :href="`/function#reviews?id=${reviewId}`"
          class="flex items-center space-x-1">
          <Icon icon="icon-pingshen" class="text-3.5" />
          <span>{{ t('caseReview.editForm.reviewNow') }}</span>
        </Button>
      </template>

      <Button
        size="small"
        class="flex items-center space-x-1"
        @click="handleFormCancel">
        <Icon icon="icon-zhongzhi2" class="text-3.5" />
        <span>{{ t('common.cancel') }}</span>
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
        :label="t('caseReview.editForm.name')"
        name="name"
        :rules="{ required: true, message: t('caseReview.editForm.enterReviewName') }">
        <Input
          v-model:value="formState.name"
          size="small"
          :maxlength="200"
          :placeholder="t('caseReview.editForm.reviewBriefOverview')" />
      </FormItem>

      <FormItem
        :label="t('caseReview.editForm.testPlan')"
        name="planId"
        :rules="{ required: true, message: t('caseReview.editForm.selectTestPlan') }">
        <Select
          v-model:value="formState.planId"
          size="small"
          :disabled="!!reviewId"
          :action="`${TESTER}/func/plan?projectId=${props.projectId}&review=true&fullTextSearch=true`"
          :fieldNames="{value: 'id', label: 'name'}"
          :placeholder="t('caseReview.editForm.selectTestPlanPlaceholder')"
          @change="handlePlanIdChange" />
      </FormItem>

      <FormItem
        :label="t('common.owner')"
        name="ownerId"
        class="relative"
        :rules="{ required: true, message: t('caseReview.editForm.selectOwner') }">
        <SelectUser
          v-model:value="formState.ownerId"
          size="small"
          :placeholder="t('caseReview.editForm.selectOwnerPlaceholder')"
          :action="`${TESTER}/project/${props.projectId}/member/user`"
          :maxlength="80" />
      </FormItem>

      <FormItem
        :label="t('caseReview.editForm.participants')"
        name="participantIds"
        class="relative"
        :rules="{ required: true, message: t('caseReview.editForm.selectParticipants') }">
        <Select
          v-model:value="formState.participantIds"
          :options="members"
          mode="multiple"
          size="small"
          :placeholder="t('caseReview.editForm.selectParticipantsPlaceholder')" />
      </FormItem>

      <FormItem :label="t('common.attachments')">
        <div class="flex items-center mt-0.5">
          <Upload
            v-if="!formState?.attachments || formState?.attachments?.length < 10"
            :fileList="[]"
            name="file"
            accept=".jpg,.bmp,.png,.gif,.txt,.docx,.jpeg,.rar,.zip,.doc,.xlsx,.xls,.pdf"
            :customRequest="handleFileUpload">
            <a class="text-theme-special text-theme-text-hover text-3 flex items-center leading-5 h-5 mt-0.5">
              <Icon icon="icon-lianjie1" class="mr-1" />
              <span class="whitespace-nowrap">{{ t('caseReview.editForm.uploadAttachments') }}</span>
            </a>
          </Upload>

          <Tooltip :overlayStyle="{ 'max-width': '400px' }">
            <template #title>
              <div class="text-3 text-theme-sub-content leading-4 break-all">
                {{ t('caseReview.editForm.attachmentsDescription') }}
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
              @click="removeAttachmentFile(index)" />
          </Button>
        </div>
      </FormItem>

      <Tabs
        v-model:activeKey="activeTabKey"
        size="small"
        class="pl-5 reviewEditTab">
        <TabPane
          key="funcCase"
          forceRender
          :tab="t('caseReview.editForm.reviewCases')">
          <div class="flex justify-between mb-3">
            <Input
              v-model:value="searchKeywords"
              :disabled="!reviewId"
              :placeholder="t('caseReview.editForm.enterQueryName')"
              class="w-50"
              @change="handleSearchKeywordChange" />

            <Button
              :disabled="!formState.planId || (reviewId && !permissions.includes(FuncPlanPermission.REVIEW))
                || dataSource?.status?.value === FuncPlanStatus.COMPLETED"
              size="small"
              type="primary"
              @click="openCaseSelectionModal">
              <Icon icon="icon-jia" class="mr-1" />
              {{ t('caseReview.editForm.addReviewCase') }}
            </Button>
          </div>

          <Table
            :columns="tableColumns"
            :dataSource="caseList"
            :pagination="reviewId ? pagination : false"
            rowKey="id"
            size="small"
            noDataSize="small"
            @change="handlePaginationChange">
            <template #bodyCell="{record, column}">
              <template v-if="column.dataIndex === 'action'">
                <Button
                  type="text"
                  size="small"
                  @click="handleCaseDeletion(record)">
                  <Icon icon="icon-qingchu" />
                  {{ t('actions.delete') }}
                </Button>
              </template>

              <template v-if="column.dataIndex === 'reviewStatus'">
                <ReviewStatus :value="record.reviewStatus" />
              </template>
            </template>
          </Table>
        </TabPane>

        <TabPane key="description" :tab="t('caseReview.editForm.reviewDescription')">
          <FormItem name="description" :rules="[{validator: validateDescription}]">
            <RichEditor
              ref="descRichRef"
              v-model:value="formState.description"
              class="review-description" />
          </FormItem>
        </TabPane>
      </Tabs>
    </Form>

    <AsyncComponent :visible="selectModalVisible">
      <SelectCaseModal
        v-model:visible="selectModalVisible"
        :projectId="props.projectId"
        :planId="formState.planId"
        :reviewId="reviewId"
        @ok="handleAddCasesToReview" />
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
