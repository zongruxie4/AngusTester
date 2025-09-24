<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, onUnmounted, readonly, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Checkbox, TabPane, Tabs, Tag } from 'ant-design-vue';
import {
  AsyncComponent, Icon, Image, Input, modal, notification, ReviewStatus as ReviewStatusView, Spin, Table
} from '@xcan-angus/vue-ui';
import { appContext, download, duration, enumUtils, SearchCriteria, ReviewStatus } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { func, funcPlan } from '@/api/tester';
import { FuncPlanPermission, FuncPlanStatus } from '@/enums/enums';
import { CaseInfo } from '@/views/function/types';

// Type imports
import { BasicProps } from '@/types/types';
import type { ReviewDetail } from '@/views/function/review/types';

// Component imports
import RichEditor from '@/components/richEditor/index.vue';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import TaskPriority from '@/components/TaskPriority/index.vue';

// Props and component definitions
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});


// Async component definitions
const SelectCaseModal = defineAsyncComponent(() => import('@/views/function/review/edit/SelectCaseModal.vue'));
const ReviewForm = defineAsyncComponent(() => import('@/views/function/review/detail/ReviewForm.vue'));
const CaseReviewResult = defineAsyncComponent(() => import('@/views/function/review/detail/case/CaseReviewResult.vue'));
// const CaseStep = defineAsyncComponent(() => import('@/views/function/case/list/CaseSteps.vue'));
const CaseStep = defineAsyncComponent(() => import('@/views/function/review/detail/case/CaseSteps.vue'));
const CaseBasicInfo = defineAsyncComponent(() => import('@/views/function/review/detail/case/CaseBasicInfo.vue'));
const Precondition = defineAsyncComponent(() => import('@/views/function/review/detail/case/Precondition.vue'));
const Members = defineAsyncComponent(() => import('@/views/function/review/detail/case/Member.vue'));
const TestResult = defineAsyncComponent(() => import('@/views/function/review/detail/case/TestResult.vue'));
const Attachment = defineAsyncComponent(() => import('@/views/function/review/detail/case/Attachment.vue'));
const AssocTasks = defineAsyncComponent(() => import('@/views/function/review/detail/case/AssocTask.vue'));
const AssocCases = defineAsyncComponent(() => import('@/views/function/review/detail/case/AssocCase.vue'));
const Description = defineAsyncComponent(() => import('@/views/function/review/detail/case/Description.vue'));

// Composables and injections
const { t } = useI18n();
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const isAdmin = computed(() => appContext.isAdmin());

// Ui state
const selectModalVisible = ref(false);
const loading = ref(false);
const startLoading = ref(false);
const drawerRef = ref();
const isMobile = ref(false);

// Right drawer section expand states
const expand = ref({
  basicInfo: true,
  precondition: true,
  steps: true,
  reviewResult: true,
  description: true,
  members: true,
  testInfo: true,
  assocTasks: true,
  assocCases: true,
  attachments: true
});

const toggleSection = (key: keyof typeof expand.value) => {
  expand.value[key] = !expand.value[key];
};

// Pagination state
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 1
});

// Search and filter state
const keywords = ref();
const reviewStatus = ref();

// Data state
const permissions = ref<string[]>([]);
const reviewDetail = ref<ReviewDetail>();
const reviewCaseList = ref<CaseInfo[]>([]);
const reviewId = ref();

// Selection state
const selectedRowKey = ref<string|undefined>();
const selectReviewCaseInfo = ref();
const selectReviewCaseIds = ref<string[]>([]);
const activeMenuKey = ref();

// Table columns
const columns = [
  {
    title: ' ',
    dataIndex: 'checkbox',
    width: 32
  },
  {
    title: t('caseReview.detail.caseCode'),
    key: 'caseCode',
    dataIndex: 'caseInfo',
    customRender: ({ text }) => {
      return text?.code;
    },
    width: 160
  },
  {
    title: t('caseReview.detail.caseName'),
    dataIndex: 'caseInfo',
    key: 'caseName',
    ellipsis: true,
    customCell: () => {
      return { style: 'white-space: nowrap;' };
    }
  },
  {
    title: t('caseReview.detail.version'),
    dataIndex: 'caseInfo',
    key: 'caseVersion',
    customRender: ({ text }) => {
      return text?.version ? `v${text.version}` : '--';
    },
    width: 100
  },
  {
    title: t('caseReview.detail.priority'),
    dataIndex: 'priority',
    customRender: ({ text }):string => text?.message,
    width: 100,
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('caseReview.detail.reviewStatus'),
    dataIndex: 'reviewStatus',
    customRender: ({ text }):string => text?.message,
    width: 100,
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('caseReview.detail.action'),
    dataIndex: 'action',
    width: 320
  }
];

/**
 * <p>Starts the review process for the current review.</p>
 * <p>Shows loading state and displays success notification upon completion.</p>
 */
const startReview = async () => {
  startLoading.value = true;
  const [error] = await func.startReview(reviewId.value);
  startLoading.value = false;
  if (error) {
    return;
  }
  notification.success(t('caseReview.detail.reviewStartedSuccess'));
  await loadReviewDetail(reviewId.value);
};

/**
 * <p>Loads user permissions for the specified plan.</p>
 * <p>If user is admin, loads all permissions. Otherwise, fetches permissions from API.</p>
 */
const loadPermissions = async (planId: string) => {
  if (isAdmin.value) {
    permissions.value = enumUtils.getEnumValues(FuncPlanPermission);
    return;
  }

  const params = { admin: true };
  loading.value = true;
  const [error, res] = await funcPlan.getCurrentAuthByPlanId(planId, params);
  loading.value = false;
  if (error) {
    return;
  }
  permissions.value = (res?.data?.permissions || []).map(item => item.value);
};

/**
 * <p>Loads detailed information for the specified review.</p>
 * <p>Updates tab pane title and loads associated permissions.</p>
 */
const loadReviewDetail = async (reviewId: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await func.getReviewDetail(reviewId);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data;
  if (!data) {
    return;
  }
  await loadPermissions(data.planId);

  reviewDetail.value = data;
  const name = data.name;
  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: reviewId + '-case' });
  }
};

/**
 * <p>Loads the list of cases for the current review.</p>
 * <p>Applies search filters and pagination parameters.</p>
 */
const loadReviewCaseList = async (reviewId: string) => {
  const { current, pageSize } = pagination.value;
  const [error, { data }] = await func.getReviewCaseList({
    reviewId: reviewId,
    filters: keywords.value ? [{ value: keywords.value, key: 'caseName', op: SearchCriteria.OpEnum.Match }] : [],
    reviewStatus: reviewStatus.value,
    pageNo: current,
    pageSize
  });

  if (error) {
    return;
  }
  reviewCaseList.value = data?.list || [];
  pagination.value.total = +data.total || 0;
  selectReviewCaseIds.value = [];
};

/**
 * <p>Handles pagination changes and reloads case list.</p>
 * <p>Clears current selection when page changes.</p>
 */
const changePage = ({ current, pageSize }) => {
  pagination.value.current = current;
  pagination.value.pageSize = pageSize;
  selectedRowKey.value = undefined;
  loadReviewCaseList(reviewId.value);
};

/**
 * <p>Opens the modal for adding new cases to the review.</p>
 */
const addReviewCase = () => {
  selectModalVisible.value = true;
};

/**
 * <p>Handles row click events for case selection.</p>
 * <p>Toggles selection state and manages drawer visibility.</p>
 */
const customRow = (record) => {
  return {
    onClick: () => {
      if (record.id === selectedRowKey.value) {
        if (activeMenuKey.value) {
          activeMenuKey.value = undefined;
          drawerRef.value.close();
        }
        selectedRowKey.value = '';
      } else {
        selectedRowKey.value = record.id;
      }
    }
  };
};

/**
 * <p>Loads detailed information for the selected case.</p>
 * <p>Determines which case version to display based on review status.</p>
 */
const loadReviewCaseDetail = async () => {
  const [error, { data }] = await func.getReviewCaseDetail(selectedRowKey.value);
  if (error) {
    return;
  }
  selectReviewCaseInfo.value = {
    ...data,
    caseInfo: data.reviewStatus?.value === ReviewStatus.PASSED ? data.reviewedCase : data.latestCase
  };
};

/**
 * <p>Closes the case detail drawer and clears selection.</p>
 */
const onCloseDrawer = () => {
  selectedRowKey.value = '';
};

/**
 * <p>Handles checkbox changes for case selection.</p>
 * <p>Updates the list of selected case IDs for batch operations.</p>
 */
const onCheckedChange = (event, caseId: string) => {
  event.stopPropagation();
  if (event.target.checked) {
    selectReviewCaseIds.value.push(caseId);
  } else {
    selectReviewCaseIds.value = selectReviewCaseIds.value.filter(id => id !== caseId);
  }
};

/**
 * <p>Handles search keyword changes with debouncing.</p>
 * <p>Resets pagination and reloads case list with new search criteria.</p>
 */
const handleKeywordChange = debounce(duration.search, () => {
  pagination.value.current = 1;
  loadReviewCaseList(reviewId.value);
});

/**
 * <p>Handles review status filter changes.</p>
 * <p>Resets pagination and reloads case list with new status filter.</p>
 */
const handleChangeStatus = () => {
  pagination.value.current = 1;
  loadReviewCaseList(reviewId.value);
};

/**
 * <p>Handles successful review completion.</p>
 * <p>Refreshes case content if currently selected case was reviewed.</p>
 */
const handleReviewOk = () => {
  if (selectedRowKey.value && selectReviewCaseIds.value.includes(selectedRowKey.value)) {
    loadReviewCaseDetail();
  }
  selectReviewCaseIds.value = [];
  loadReviewCaseList(reviewId.value);
  loadReviewDetail(reviewId.value);
};

/**
 * <p>Handles cancel action from ReviewForm.</p>
 * <p>Clears selected cases to hide the form.</p>
 */
const handleReviewCancel = () => {
  selectReviewCaseIds.value = [];
};

/**
 * <p>Removes a case from the review with confirmation.</p>
 * <p>Handles pagination adjustment when removing the last item on a page.</p>
 */
const delCase = async (record) => {
  modal.confirm({
    title: t('caseReview.detail.confirmCancelReviewCase', { name: record?.caseInfo?.name || '' }),
    async onOk () {
      const [error] = await func.deleteReviewCase([record.id]);
      if (error) {
        return;
      }
      if (pagination.value.current !== 1 && reviewCaseList.value.length === 1) {
        pagination.value.current -= 1;
      }
      await loadReviewCaseList(reviewId.value);
    }
  });
};

/**
 * <p>Restarts review for a specific case with confirmation.</p>
 * <p>Resets the case to pending status for re-review.</p>
 */
const restart = async (record) => {
  modal.confirm({
    title: t('caseReview.detail.restartReview'),
    content: t('caseReview.detail.confirmRestartReviewCase', { name: record?.caseInfo?.name || '' }),
    async onOk () {
      const [error] = await func.restartReviewCase([record.id]);
      if (error) {
        return;
      }
      await loadReviewCaseList(reviewId.value);
    }
  });
};

/**
 * <p>Resets review results for a specific case with confirmation.</p>
 * <p>Clears previous review decisions and allows re-review.</p>
 */
const reset = async (record) => {
  modal.confirm({
    title: t('caseReview.detail.resetReview'),
    content: t('caseReview.detail.confirmResetReviewCase', { name: record?.caseInfo?.name || '' }),
    async onOk () {
      const [error] = await func.resetReviewCase([record.id]);
      if (error) {
        return;
      }
      await loadReviewCaseList(reviewId.value);
    }
  });
};

/**
 * <p>Adds selected cases to the current review.</p>
 * <p>Closes modal and refreshes both case list and review details.</p>
 */
const handleAddCase = async (caseIds: string[]) => {
  selectModalVisible.value = false;
  const [error] = await func.addReviewCase({
    caseIds: caseIds,
    reviewId: reviewId.value
  });
  if (error) {
    return;
  }
  await loadReviewCaseList(reviewId.value);
  await loadReviewDetail(reviewId.value);
};

// Visibility conditions
const hasReviewInfo = computed(() => {
  const info = selectReviewCaseInfo.value?.caseInfo as any | undefined;
  const reviewNumRaw = info?.reviewNum ?? 0;
  const n = typeof reviewNumRaw === 'string' ? Number(reviewNumRaw) : reviewNumRaw;
  return (n ?? 0) > 0;
});

const hasTestInfo = computed(() => {
  const info = selectReviewCaseInfo.value?.caseInfo as any | undefined;
  const testNumRaw = info?.testNum ?? 0;
  const n = typeof testNumRaw === 'string' ? Number(testNumRaw) : testNumRaw;
  return (n ?? 0) > 0;
});

/**
 * <p>Checks if the current screen size is mobile.</p>
 */
const checkMobile = () => {
  isMobile.value = window.innerWidth < 1024;
};

/**
 * <p>Component mounted lifecycle hook.</p>
 * <p>Watches for changes in props data and loads review information accordingly.</p>
 */
onMounted(() => {
  // Responsive detection
  checkMobile();
  window.addEventListener('resize', checkMobile);

  watch(() => props.data, async (newValue, oldValue) => {
    const id = newValue?.id;
    if (!id) {
      return;
    }

    const oldId = oldValue?.id;
    if (id === oldId) {
      return;
    }
    reviewId.value = id;
    await loadReviewDetail(id);
    await loadReviewCaseList(id);
  }, { immediate: true });
});



/**
 * <p>Watches for changes in selected row key.</p>
 * <p>Loads case content when a new case is selected.</p>
 */
watch(() => selectedRowKey.value, async (newValue) => {
  if (newValue) {
    await loadReviewCaseDetail();
  }
});

const refreshReviewCase = async (refreshList = false) => {
  loadReviewCaseDetail();
  if (refreshList) {
    loadReviewCaseList(reviewId.value);
  }
}

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile);
});
</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5">
    <div class="flex h-full bg-gray-50">
      <!-- Main content area -->
      <div class="flex-1 flex flex-col min-w-0">
        <div class="px-4 mt-4">
          <!-- Page header section -->
          <div class="bg-white border-b border-gray-200 px-4 py-3 shadow-sm">
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <h1 class="text-xl font-semibold text-gray-900">
                  {{ reviewDetail?.name }}
                </h1>
                <Tag
                  class="px-3 py-1 text-sm font-medium rounded-full"
                  :class="reviewDetail?.status?.value">
                  {{ reviewDetail?.status?.message }}
                </Tag>
              </div>

              <Button
                v-if="reviewDetail?.status?.value === ReviewStatus.PENDING"
                size="small"
                type="primary"
                class="shadow-sm"
                :loading="startLoading"
                @click="startReview">
                <Icon icon="icon-kaishi" class="mr-2" />
                {{ t('caseReview.detail.startReview') }}
              </Button>
            </div>
          </div>

          <!-- Progress and summary info -->
          <div class="bg-white border-b border-gray-200 px-4 py-3">
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-8">
                <!-- Progress bar -->
                <div class="flex items-center space-x-4">
                  <div class="text-sm text-gray-600">
                    {{ t('caseReview.detail.progress') }}
                  </div>
                  <div class="flex items-center space-x-2">
                    <div class="w-32 h-2 bg-gray-200 rounded-full overflow-hidden">
                      <div
                        class="h-full bg-gradient-to-r from-blue-500 to-green-500 transition-all duration-300"
                        :style="{width: `${reviewDetail?.progress?.completedRate || 0}%`}">
                      </div>
                    </div>
                    <span class="text-sm font-medium text-gray-700">
                      {{ reviewDetail?.progress?.completed || 0 }} / {{ reviewDetail?.progress?.total || 0 }}
                    </span>
                    <span class="text-sm text-gray-500">
                      ({{ reviewDetail?.progress?.completedRate || 0 }}%)
                    </span>
                  </div>
                </div>
              </div>

              <!-- Summary (only plan) -->
              <div class="flex items-center space-x-6 text-sm">
                <div class="flex items-center space-x-2">
                  <Icon icon="icon-jihua" class="text-gray-400" />
                  <span class="text-gray-600">{{ t('caseReview.detail.testPlan') }}：</span>
                  <span class="font-medium text-gray-900">{{ reviewDetail?.planName }}</span>
                </div>
              </div>
            </div>

            <!-- Participants and attachments -->
            <div class="mt-3 flex items-center justify-between">
              <div class="flex items-center space-x-10">
                <!-- Owner (placed before participants) -->
                <div class="flex items-center space-x-2">
                  <span class="text-gray-600">{{ t('caseReview.detail.owner') }}：</span>
                  <span class="font-medium text-gray-900">{{ reviewDetail?.ownerName }}</span>
                </div>

                <!-- Participants -->
                <div class="flex items-center space-x-2">
                  <Icon icon="icon-tuandui" class="text-gray-400" />
                  <span class="text-gray-600">{{ t('caseReview.detail.participants') }}：</span>
                  <div class="flex items-center space-x-1">
                    <div
                      v-for="person in (reviewDetail?.participants || []).slice(0, 6)"
                      :key="person.id"
                      class="flex items-center space-x-1"
                      :title="person.fullName">
                      <Image
                        type="avatar"
                        :src="person.avatar"
                        class="w-5 h-5 rounded-full border border-white" />
                    </div>
                    <span v-if="(reviewDetail?.participants || []).length > 6" class="text-xs text-gray-500">
                      +{{ (reviewDetail?.participants || []).length - 6 }}
                    </span>
                  </div>
                </div>

                <!-- Attachments -->
                <div class="flex items-center space-x-2">
                  <Icon icon="icon-fujian" class="text-gray-400" />
                  <span class="text-gray-600">{{ t('caseReview.detail.attachments') }}：</span>
                  <div class="flex items-center space-x-2">
                    <a
                      v-for="file in (reviewDetail?.attachments || [])"
                      :key="file.id"
                      class="text-blue-600 hover:text-blue-800 text-sm cursor-pointer"
                      @click="download(file.url)">
                      {{ file.name }}
                    </a>
                    <span v-if="!reviewDetail?.attachments?.length" class="text-sm text-gray-400">
                      {{ t('caseReview.detail.noAttachments') }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Primary content area -->
        <div class="flex-1 px-4 py-3 overflow-auto">
          <!-- Tabs area -->
          <Tabs
            size="small"
            class="bg-white rounded-lg shadow-sm font-medium text-xs"
            :tabBarStyle="{ marginBottom: 0, padding: '0 10px' }">
            <TabPane key="testerResponsibilities" :tab="t('caseReview.detail.testCases')">
              <!-- Search and actions bar -->
              <div class="p-3 border-b border-gray-100 text-xs">
                <div class="flex items-center justify-between" :class="{ 'mobile-search': isMobile }">
                  <div class="flex items-center space-x-2 search-inputs" :class="{ 'w-full': isMobile }">
                    <Input
                      v-model:value="keywords"
                      :placeholder="t('caseReview.detail.enterQueryName')"
                      :class="isMobile ? 'w-full' : 'w-56'"
                      allowClear
                      @change="handleKeywordChange">
                      <template #prefix>
                        <Icon icon="icon-sousuo" class="text-gray-400" />
                      </template>
                    </Input>

                    <SelectEnum
                      v-model:value="reviewStatus"
                      :class="isMobile ? 'w-full' : 'w-40'"
                      :placeholder="t('caseReview.detail.selectReviewStatus')"
                      enumKey="ReviewStatus"
                      allowClear
                      @change="handleChangeStatus">
                    </SelectEnum>
                  </div>

                  <Button
                    size="small"
                    :disabled="!reviewDetail?.planId || !permissions.includes(FuncPlanPermission.REVIEW)
                      || reviewDetail?.status?.value === FuncPlanStatus.COMPLETED"
                    type="primary"
                    class="shadow-sm search-actions"
                    :class="{ 'w-full': isMobile }"
                    @click="addReviewCase">
                    <Icon icon="icon-jia" class="mr-2" />
                    {{ t('caseReview.detail.addReviewCase') }}
                  </Button>
                </div>
              </div>

              <!-- Table area -->
              <div class="p-3 text-xs">
                <Table
                  :columns="columns"
                  :dataSource="reviewCaseList"
                  :customRow="customRow"
                  :rowClassName="(record) => record.id === selectedRowKey ? 'ant-table-row-selected' : ''"
                  :pagination="{
                    ...pagination,
                    showSizeChanger: true,
                    showQuickJumper: true,
                    showTotal: (total, range) => `${range[0]}-${range[1]} / ${total} t('page.items')`
                  }"
                  class="review-case-table"
                  @change="changePage">
                  <template #bodyCell="{record, column}">
                    <template v-if="column.dataIndex === 'checkbox'">
                      <Checkbox
                        :disabled="record.reviewStatus?.value !== ReviewStatus.PENDING
                          || !permissions.includes(FuncPlanPermission.REVIEW)
                          || reviewDetail?.status?.value === ReviewStatus.PENDING"
                        :checked="selectReviewCaseIds.includes(record.id)"
                        @click="onCheckedChange($event, record.id)">
                      </Checkbox>
                    </template>

                    <template v-if="column.key === 'caseName'">
                      <span class="inline-flex items-center">
                        <span class="truncate">{{ record.caseInfo.name }}</span>
                        <span
                          v-if="record?.caseInfo?.overdue"
                          class="ml-3 inline-flex items-center justify-center text-red-700 text-xs font-medium rounded-full border border-red-200 bg-red-100 whitespace-nowrap text-center"
                          style="width: 70px; height: 20px; line-height: 20px;">
                          {{ t('caseReview.comp.caseBasicInfo.overdue') }}
                        </span>
                      </span>
                    </template>

                    <template v-if="column.dataIndex === 'reviewStatus'">
                      <ReviewStatusView :value="record.reviewStatus" />
                    </template>

                    <template v-if="column.dataIndex === 'priority'">
                      <TaskPriority :value="record?.caseInfo?.priority" />
                    </template>

                    <template v-if="column.dataIndex === 'action'">
                      <Button
                        type="text"
                        size="small"
                        :disabled="!permissions.includes(FuncPlanPermission.REVIEW)"
                        @click.stop="delCase(record)">
                        <Icon icon="icon-qingchu" class="mr-1" />
                        {{ t('caseReview.detail.cancel') }}
                      </Button>

                      <Button
                        :disabled="!permissions.includes(FuncPlanPermission.REVIEW)
                          || record.reviewStatus?.value === ReviewStatus.PENDING
                          || reviewDetail?.status?.value === ReviewStatus.PENDING"
                        type="text"
                        size="small"
                        @click.stop="restart(record)">
                        <Icon icon="icon-zhongxinkaishi" class="mr-1" />
                        {{ t('caseReview.detail.restartNewReview') }}
                      </Button>

                      <Button
                        :disabled="!permissions.includes(FuncPlanPermission.REVIEW)
                          || record.reviewStatus?.value === ReviewStatus.PENDING
                          || reviewDetail?.status?.value === ReviewStatus.PENDING"
                        type="text"
                        size="small"
                        @click.stop="reset(record)">
                        <Icon icon="icon-zhongzhipingshenjieguo" class="mr-1" />
                        {{ t('caseReview.detail.resetReview') }}
                      </Button>

                      <RouterLink :to="`/function#cases?id=${record.caseId}`">
                        <Button
                          type="text"
                          size="small"
                          @click.stop>
                          <Icon icon="icon-chakanhuodong" class="mr-1" />
                          {{ t('caseReview.detail.view') }}
                        </Button>
                      </RouterLink>
                    </template>
                  </template>
                </Table>
              </div>

              <template v-if="selectReviewCaseIds.length">
                <div class="ml-10">
                  <div class="font-semibold text-3.5 mt-5">
                    {{ t('caseReview.detail.reviewInfoSelected') }}
                    <span class="text-sub-content">{{
                      t('caseReview.detail.selectedCount', {count: selectReviewCaseIds.length})
                    }}</span>
                  </div>
                  <ReviewForm
                    class="mt-5 w-150"
                    :selectedRowKeys="selectReviewCaseIds"
                    @update="handleReviewOk"
                    @cancel="handleReviewCancel" />
                </div>
              </template>
            </TabPane>
            <TabPane key="description" :tab="t('caseReview.detail.reviewDescription')">
              <div v-if="reviewDetail?.description" class="">
                <RichEditor
                  :value="reviewDetail?.description"
                  mode="view" />
              </div>
              <div v-else class="text-sub-content">
                {{ t('caseReview.detail.noDescription') }}
              </div>
            </TabPane>
          </Tabs>
        </div>
      </div>

      <!-- Right side detail drawer -->
      <div
        class="flex flex-col mt-4 transition-all duration-300 ease-in-out bg-white border border-gray-200 shadow-lg"
        :class="{
          'w-0 opacity-0 overflow-hidden': !selectedRowKey,
          'opacity-100 w-[28.8rem]': !!selectedRowKey && !isMobile,
          'fixed inset-0 z-50 w-full': selectedRowKey && isMobile
        }">
        <!-- Drawer header -->
        <div class="flex items-center justify-between p-4 border-b border-gray-200 bg-gray-50">
          <h3 class="text-lg font-semibold text-gray-900">
            {{ t('caseReview.detail.title') }}
          </h3>
          <Button
            type="text"
            size="small"
            class="text-gray-400 hover:text-gray-600"
            @click="onCloseDrawer">
            <Icon icon="icon-shanchuguanbi" class="text-lg" />
          </Button>
        </div>

        <!-- Drawer content -->
        <div class="flex-1 overflow-auto">
          <!-- Case details content: title outside the card, supports collapse -->
          <div class="p-4 space-y-4 drawer-sections">
            <!-- Basic info -->
            <div class="space-y-2">
              <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('basicInfo')">
                <div class="flex items-center text-gray-800 text-sm font-medium">
                  <Icon icon="icon-jibenxinxi" class="mr-1 text-blue-500" />
                  <span>{{ t('caseReview.comp.caseBasicInfo.title') }}</span>
                </div>
                <Icon :icon="expand.basicInfo ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
              </div>
              <div v-show="expand.basicInfo">
                <CaseBasicInfo
                  :caseInfo="selectReviewCaseInfo?.caseInfo"
                  :projectId="props.projectId"
                  :readonly="!permissions.includes(FuncPlanPermission.MODIFY_CASE)"
                  @change="refreshReviewCase(true)" />
              </div>
            </div>

            <!-- Precondition -->
            <div class="space-y-2">
              <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('precondition')">
                <div class="flex items-center text-gray-800 text-sm font-medium">
                  <Icon icon="icon-shezhi1" class="mr-1 text-orange-500" />
                  <span>{{ t('caseReview.comp.precondition.title') }}</span>
                </div>
                <Icon :icon="expand.precondition ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
              </div>
              <div v-show="expand.precondition">
                <Precondition :caseInfo="selectReviewCaseInfo?.caseInfo"
                :readonly="!permissions.includes(FuncPlanPermission.MODIFY_CASE)"
                @change="refreshReviewCase" />
              </div>
            </div>

            <!-- Test steps -->
            <div class="space-y-2">
              <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('steps')">
                <div class="flex items-center text-gray-800 text-sm font-medium">
                  <Icon icon="icon-jihua1" class="mr-1 text-indigo-500" />
                  <span>{{ t('caseReview.detail.testSteps') }}</span>
                </div>
                <Icon :icon="expand.steps ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
              </div>
              <div v-show="expand.steps">
                <CaseStep :caseInfo="selectReviewCaseInfo?.caseInfo"
                :readonly="!permissions.includes(FuncPlanPermission.MODIFY_CASE)"
                @change="refreshReviewCase" />
              </div>
            </div>

            <!-- Description -->
            <div class="space-y-2">
              <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('description')">
                <div class="flex items-center text-gray-800 text-sm font-medium">
                  <Icon icon="icon-shuoming" class="mr-1 text-purple-500" />
                  <span>{{ t('caseReview.comp.description.title') }}</span>
                </div>
                <Icon :icon="expand.description ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
              </div>
              <div v-show="expand.description">
                <Description
                :caseInfo="selectReviewCaseInfo?.caseInfo"
                :readonly="!permissions.includes(FuncPlanPermission.MODIFY_CASE)"
                @change="refreshReviewCase" />
              </div>
            </div>

            <!-- Review result (visible only when data exists) -->
            <div v-if="hasReviewInfo" class="space-y-2">
              <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('reviewResult')">
                <div class="flex items-center text-gray-800 text-sm font-medium">
                  <Icon icon="icon-pingshen" class="mr-1 text-green-500" />
                  <span>{{ t('caseReview.comp.caseReviewResult.title') }}</span>
                </div>
                <Icon :icon="expand.reviewResult ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
              </div>
              <div v-show="expand.reviewResult">
                <CaseReviewResult :caseInfo="selectReviewCaseInfo" />
              </div>
            </div>

            <!-- Test info (after review result, visible only when data exists) -->
            <div v-if="hasTestInfo" class="space-y-2">
              <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('testInfo')">
                <div class="flex items-center text-gray-800 text-sm font-medium">
                  <Icon icon="icon-ceshixinxi" class="mr-1 text-amber-500" />
                  <span>{{ t('caseReview.comp.testInfo.title') }}</span>
                </div>
                <Icon :icon="expand.testInfo ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
              </div>
              <div v-show="expand.testInfo">
                <TestResult :caseInfo="selectReviewCaseInfo?.caseInfo" />
              </div>
            </div>

            <!-- Members -->
            <div class="space-y-2">
              <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('members')">
                <div class="flex items-center text-gray-800 text-sm font-medium">
                  <Icon icon="icon-chuangjianren" class="mr-1 text-pink-500" />
                  <span>{{ t('caseReview.comp.member.title') }}</span>
                </div>
                <Icon :icon="expand.members ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
              </div>
              <div v-show="expand.members">
                <Members
                :caseInfo="selectReviewCaseInfo?.caseInfo"
                :userInfo="props.userInfo"
                :readonly="!permissions.includes(FuncPlanPermission.MODIFY_CASE)"
                @change="refreshReviewCase" />
              </div>
            </div>

            <!-- Related tasks -->
            <div class="space-y-2 space-x-4">
              <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('assocTasks')">
                <div class="flex items-center text-gray-800 text-sm font-medium">
                  <Icon icon="icon-renwu" class="mr-1 text-emerald-500" />
                  <span>{{ t('caseReview.comp.assocTask.title') }}</span>
                </div>
                <Icon :icon="expand.assocTasks ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
              </div>
              <div v-show="expand.assocTasks">
                <AssocTasks
                  :dataSource="selectReviewCaseInfo?.caseInfo?.refTaskInfos"
                  :projectId="props.projectId"
                  :caseInfo="selectReviewCaseInfo?.caseInfo"
                  :hideTitle="true" />
              </div>
            </div>

            <!-- Related cases -->
            <div class="space-y-2 space-x-4">
              <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('assocCases')">
                <div class="flex items-center text-gray-800 text-sm font-medium">
                  <Icon icon="icon-gongnengyongli" class="mr-1 text-cyan-500" />
                  <span>{{ t('caseReview.comp.assocCase.title') }}</span>
                </div>
                <Icon :icon="expand.assocCases ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
              </div>
              <div v-show="expand.assocCases">
                <AssocCases
                  :dataSource="selectReviewCaseInfo?.caseInfo?.refCaseInfos"
                  :projectId="props.projectId"
                  :caseInfo="selectReviewCaseInfo?.caseInfo"
                  :hideTitle="true" />
              </div>
            </div>

            <!-- Attachments -->
            <div class="space-y-2 space-x-4">
              <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('attachments')">
                <div class="flex items-center text-gray-800 text-sm font-medium">
                  <Icon icon="icon-wenjian" class="mr-1 text-indigo-500" />
                  <span>{{ t('caseReview.comp.attachment.title') }}</span>
                </div>
                <Icon :icon="expand.attachments ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
              </div>
              <div v-show="expand.attachments">
                <Attachment :caseInfo="selectReviewCaseInfo?.caseInfo" :hideTitle="true" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <AsyncComponent :visible="selectModalVisible">
      <SelectCaseModal
        v-model:visible="selectModalVisible"
        :planId="reviewDetail?.planId"
        :reviewId="reviewId"
        :projectId="props.projectId"
        @ok="handleAddCase" />
    </AsyncComponent>
  </Spin>
</template>

<style scoped>
/* Status tag style */
.PENDING {
  @apply bg-blue-100 text-blue-800;
}

.IN_PROGRESS {
  @apply bg-cyan-100 text-cyan-800;
}

.COMPLETED {
  @apply bg-green-100 text-green-800;
}

.BLOCKED {
  @apply bg-orange-100 text-orange-800;
}

/* Table style enhancement */
:deep(.review-case-table) {
  .ant-table-thead > tr > th {
    @apply bg-gray-50 text-gray-700 font-semibold;
  }

  .ant-table-tbody > tr:hover > td {
    @apply bg-blue-50;
  }

  .ant-table-tbody > tr.ant-table-row-selected > td {
    @apply bg-blue-100;
  }
}

/* Drawer animation */
.drawer-enter-active,
.drawer-leave-active {
  transition: all 0.3s ease-in-out;
}

.drawer-enter-from,
.drawer-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

/* Progress bar animation */
.progress-bar {
  transition: width 0.6s ease-in-out;
}

/* Card shadow effect */
.card-shadow {
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
}

/* Responsive design */
@media (max-width: 1024px) {
  .drawer-mobile {
    @apply fixed inset-0 z-50;
  }

  /* Mobile table optimization */
  :deep(.review-case-table) {
    .ant-table-thead > tr > th {
      @apply text-xs px-2;
    }

    .ant-table-tbody > tr > td {
      @apply text-xs px-2 py-2;
    }
  }

  /* Mobile search bar optimization */
  .mobile-search {
    @apply flex-col space-y-3;
  }

  .mobile-search .search-inputs {
    @apply w-full;
  }

  .mobile-search .search-actions {
    @apply w-full justify-center;
  }
}

/* Scrollbar style */
:deep(.ant-table-body) {
  scrollbar-width: thin;
  scrollbar-color: #cbd5e0 #f7fafc;
}

:deep(.ant-table-body)::-webkit-scrollbar {
  height: 6px;
}

:deep(.ant-table-body)::-webkit-scrollbar-track {
  background: #f7fafc;
  border-radius: 3px;
}

:deep(.ant-table-body)::-webkit-scrollbar-thumb {
  background: #cbd5e0;
  border-radius: 3px;
}

:deep(.ant-table-body)::-webkit-scrollbar-thumb:hover {
  background: #a0aec0;
}
</style>
