<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Checkbox, Popover, TabPane, Tabs, Tag } from 'ant-design-vue';
import { AsyncComponent, Icon, Image, Input, modal, notification, ReviewStatus, Spin, Table } from '@xcan-angus/vue-ui';
import { appContext, download, duration, enumUtils } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { func, funcPlan } from '@/api/tester';
import { FuncPlanPermission, FuncPlanStatus } from '@/enums/enums';

// Type imports
import { BasicProps } from '@/types/types';
import { ReviewDetail } from '@/views/function/review/types';

// Component imports
import RichEditor from '@/components/richEditor/index.vue';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import TaskPriority from '@/components/TaskPriority/index.vue';

// PROPS AND COMPONENT DEFINITIONS
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

// Async component definitions
const SelectCaseModal = defineAsyncComponent(() => import('@/views/function/review/edit/SelectCaseModal.vue'));
const ReviewForm = defineAsyncComponent(() => import('@/views/function/review/detail/ReviewForm.vue'));
const CaseReviewResult = defineAsyncComponent(() => import('@/views/function/review/case/CaseReviewResult.vue'));
const CaseStep = defineAsyncComponent(() => import('@/views/function/case/list/CaseSteps.vue'));
const CaseBasicInfo = defineAsyncComponent(() => import('@/views/function/review/case/CaseBasicInfo.vue'));
const Precondition = defineAsyncComponent(() => import('@/views/function/review/case/Precondition.vue'));
const Members = defineAsyncComponent(() => import('@/views/function/review/case/Member.vue'));
const TestInfo = defineAsyncComponent(() => import('@/views/function/review/case/TestInfo.vue'));
const Attachment = defineAsyncComponent(() => import('@/views/function/review/case/Attachment.vue'));
const AssocTasks = defineAsyncComponent(() => import('@/views/function/review/case/AssocTask.vue'));
const AssocCases = defineAsyncComponent(() => import('@/views/function/review/case/AssocCase.vue'));
const Description = defineAsyncComponent(() => import('@/views/function/review/case/Description.vue'));

// COMPOSABLES AND INJECTIONS
const { t } = useI18n();
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const isAdmin = computed(() => appContext.isAdmin());

// UI STATE
const selectModalVisible = ref(false);
const loading = ref(false);
const startLoading = ref(false);
const drawerRef = ref();

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
const reviewDetail = ref(ReviewDetail);
const caseList = ref([]);
const reviewId = ref();

// Selection state
const selectedRowKey = ref<string|undefined>();
const selectCaseInfo = ref();
const selectCaseIds = ref<string[]>([]);
const activeMenuKey = ref();

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

// Table columns
const columns = [
  {
    title: ' ',
    dataIndex: 'checkbox',
    width: 32
  },
  {
    title: t('caseReview.detail.caseCode'),
    dataIndex: 'caseInfo',
    customRender: ({ text }) => {
      return text?.code;
    },
    width: 160
  },
  {
    title: t('caseReview.detail.caseName'),
    dataIndex: 'caseInfo',
    ellipsis: true,
    customRender: ({ text }) => {
      return text?.name;
    },
    customCell: () => {
      return { style: 'white-space: nowrap;' };
    }
  },
  {
    title: t('caseReview.detail.version'),
    dataIndex: 'caseInfo',
    customRender: ({ text }) => {
      return text?.version ? `v${text.version}` : '--';
    },
    width: 90
  },
  {
    title: t('caseReview.detail.priority'),
    dataIndex: 'priority',
    customRender: ({ text }):string => text?.message,
    width: 80,
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('caseReview.detail.reviewStatus'),
    dataIndex: 'reviewStatus',
    customRender: ({ text }):string => text?.message,
    width: '10%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('caseReview.detail.creator'),
    dataIndex: 'createdByName',
    width: '10%',
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
 * <p>Loads user permissions for the specified plan.</p>
 * <p>If user is admin, loads all permissions. Otherwise, fetches permissions from API.</p>
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
    filters: keywords.value ? [{ value: keywords.value, key: 'caseName', op: 'MATCH' }] : [],
    reviewStatus: reviewStatus.value,
    pageNo: current,
    pageSize
  });

  if (error) {
    return;
  }
  caseList.value = data?.list || [];
  pagination.value.total = +data.total || 0;
  selectCaseIds.value = [];
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
const loadCaseContentInfo = async () => {
  const [error, { data }] = await func.getReviewCaseDetail(selectedRowKey.value);
  if (error) {
    return;
  }
  selectCaseInfo.value = {
    ...data,
    caseInfo: data.reviewStatus?.value === ReviewStatus.PENDING ? data.latestCase : data.reviewedCase
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
    selectCaseIds.value.push(caseId);
  } else {
    selectCaseIds.value = selectCaseIds.value.filter(id => id !== caseId);
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
  if (selectedRowKey.value && selectCaseIds.value.includes(selectedRowKey.value)) {
    loadCaseContentInfo();
  }
  selectCaseIds.value = [];
  loadReviewCaseList(reviewId.value);
  loadReviewDetail(reviewId.value);
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
      if (pagination.value.current !== 1 && caseList.value.length === 1) {
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

/**
 * <p>Watches for changes in selected row key.</p>
 * <p>Loads case content when a new case is selected.</p>
 */
watch(() => selectedRowKey.value, newValue => {
  if (newValue) {
    loadCaseContentInfo();
  }
});

/**
 * <p>Component mounted lifecycle hook.</p>
 * <p>Watches for changes in props data and loads review information accordingly.</p>
 */
onMounted(() => {
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
</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5">
    <div class="flex h-full">
      <div class="flex-1 px-5 py-5 overflow-auto">
        <div class="font-semibold text-4 mb-3">
          {{ reviewDetail?.name }}
          <Tag class="ml-2 text-white" :class="reviewDetail?.status?.value">
            {{ reviewDetail?.status?.message }}
          </Tag>

          <Button
            v-if="reviewDetail?.status?.value === ReviewStatus.PENDING"
            size="small"
            type="primary"
            ghost
            class="ml-3"
            @click="startReview">
            <Icon icon="icon-kaishi" class="mr-1" />
            {{ t('caseReview.detail.startReview') }}
          </Button>
        </div>
        <div class="flex space-x-12">
          <div class="space-y-1">
            <div class="text-center h-6 leading-6 space-x-3">
              <span>{{ t('caseReview.detail.progress') }}</span>
              <span class="font-semibold text-3.5">{{
                reviewDetail?.progress?.completed || 0
              }} / {{ reviewDetail?.progress?.total || 0 }}</span>
            </div>

            <div class="w-30 flex h-2 rounded bg-gray-3">
              <div class="text-center bg-status-success rounded" :style="{width: `${reviewDetail?.progress?.completedRate || 0}%`}">
              </div>
            </div>

            <div class="text-center text-4 font-medium">
              {{ reviewDetail?.progress?.completedRate || 0 }} %
            </div>
          </div>

          <div class="flex-1">
            <div class="flex leading-8 h-8">
              <div class="inline-flex flex-1 pr-2">
                <label class="w-16">{{ t('caseReview.detail.testPlan') }}：</label>
                <div>{{ reviewDetail?.planName }}</div>
              </div>
              <div class="inline-flex flex-1 pr-2">
                <label class="w-16">{{ t('caseReview.detail.owner') }}：</label>
                <div>{{ reviewDetail?.ownerName }}</div>
              </div>
            </div>

            <div class="flex leading-8 h-8">
              <div class="inline-flex flex-1 pr-2">
                <label class="w-16">{{ t('caseReview.detail.attachments') }}：</label>
                <div class="flex space-x-2 flex-1">
                  <a
                    v-for="file in (reviewDetail?.attachments || [])"
                    class="text-blue-hover"
                    @click="download(file.url)">
                    {{ file.name }}
                  </a>
                  <div v-if="!reviewDetail?.attachments?.length">{{ t('caseReview.detail.noAttachments') }}</div>
                </div>
              </div>

              <div class="inline-flex flex-1 pr-2">
                <label class="w-16">{{ t('caseReview.detail.participants') }}：</label>
                <div class="flex space-x-2 flex-wrap flex-1 items-center">
                  <div
                    v-for="person in (reviewDetail?.participants || []).slice(0, 5)"
                    :key="person.id"
                    class="inline-flex space-x-1 items-center">
                    <Image
                      type="avatar"
                      :src="person.avatar"
                      class="w-4 h-4 rounded-full" />
                    <span>{{ person.fullName }}</span>
                  </div>

                  <Popover placement="bottom">
                    <template #content>
                      <div class="max-w-100">
                        <div
                          v-for="person in (reviewDetail?.participants || []).slice(5)"
                          :key="person.id"
                          class="inline-flex space-x-1 items-center">
                          <Image
                            type="avatar"
                            :src="person.avatar"
                            class="w-4 h-4 rounded-full" />
                          <span>{{ person.fullName }}</span>
                        </div>
                      </div>
                    </template>
                    <Icon
                      v-if="(reviewDetail?.participants || []).length > 5"
                      icon="icon-gengduo"
                      class="text-blue-1" />
                  </Popover>
                </div>
              </div>
            </div>
          </div>
        </div>

        <Tabs size="small" class="mt-5">
          <TabPane key="testerResponsibilities" :tab="t('caseReview.detail.testCases')">
            <div class="flex mb-3">
              <Input
                v-model:value="keywords"
                :placeholder="t('caseReview.detail.enterQueryName')"
                class="w-50"
                @change="handleKeywordChange" />

              <SelectEnum
                v-model:value="reviewStatus"
                class="w-50 ml-2"
                :placeholder="t('caseReview.detail.selectReviewStatus')"
                enumKey="ReviewStatus"
                :allowClear="true"
                @change="handleChangeStatus">
                <template #option="item">
                  <ReviewStatus :value="item" />
                </template>
              </SelectEnum>

              <div class="flex-1"></div>

              <Button
                :disabled="!reviewDetail?.planId || !permissions.includes(FuncPlanPermission.REVIEW) || reviewDetail?.status?.value === FuncPlanStatus.COMPLETED"
                size="small"
                type="primary"
                @click="addReviewCase">
                <Icon icon="icon-jia" class="mr-1" />
                {{ t('caseReview.detail.addReviewCase') }}
              </Button>
            </div>

            <Table
              size="small"
              :columns="columns"
              :dataSource="caseList"
              :customRow="customRow"
              :rowClassName="(record) => record.id === selectedRowKey ? 'ant-table-row-selected' : ''"
              :pagination="pagination"
              noDataSize="small"
              @change="changePage">
              <template #bodyCell="{record, column}">
                <template v-if="column.dataIndex === 'checkbox'">
                  <Checkbox
                    :disabled="record.reviewStatus?.value !== ReviewStatus.PENDING
                      || !permissions.includes(FuncPlanPermission.REVIEW)
                      || reviewDetail?.status?.value === ReviewStatus.PENDING"
                    :checked="selectCaseIds.includes(record.id)"
                    @click="onCheckedChange($event, record.id)">
                  </Checkbox>
                </template>

                <template v-if="column.dataIndex === 'reviewStatus'">
                  <ReviewStatus :value="record.reviewStatus" />
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

            <template v-if="selectCaseIds.length">
              <div class="font-semibold text-3.5 mt-5">
                {{ t('caseReview.detail.reviewInfoSelected') }}
                <span class="text-sub-content">{{ t('caseReview.detail.selectedCount', { count: selectCaseIds.length }) }}</span>
              </div>
              <ReviewForm
                class="mt-5 w-100"
                :selectedRowKeys="selectCaseIds"
                @update="handleReviewOk" />
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
      <div class="flex flex-col transition-all" :class="{'w-0': !selectedRowKey, 'w-100 border-l p-2': !!selectedRowKey}">
        <div>
          <Icon
            icon="icon-shanchuguanbi"
            class="text-5 cursor-pointer"
            @click="onCloseDrawer" />
        </div>

        <div class="flex-1 overflow-auto p-4">
          <CaseBasicInfo
            class="pb-5"
            :caseInfo="selectCaseInfo?.caseInfo" />

          <Precondition
            class="py-5"
            :caseInfo="selectCaseInfo?.caseInfo" />

          <div class="font-semibold text-3.5">
            {{ t('caseReview.detail.testSteps') }}
          </div>

          <CaseStep
            class="pb-5 pt-3"
            :defaultValue="selectCaseInfo?.caseInfo?.steps || []"
            :stepView="selectCaseInfo?.caseInfo?.stepView?.value"
            :readonly="true" />

          <CaseReviewResult
            class="py-5"
            :caseInfo="selectCaseInfo" />

          <Description
            class="py-5"
            :caseInfo="selectCaseInfo?.caseInfo" />

          <Members
            class="py-5"
            :caseInfo="selectCaseInfo?.caseInfo" />

          <TestInfo
            class="py-5"
            :caseInfo="selectCaseInfo?.caseInfo" />

          <AssocTasks
            class="py-5"
            :dataSource="selectCaseInfo?.caseInfo?.refTaskInfos"
            :projectId="props.projectId"
            :caseInfo="selectCaseInfo?.caseInfo" />

          <AssocCases
            class="py-5"
            :dataSource="selectCaseInfo?.caseInfo?.refCaseInfos"
            :projectId="props.projectId"
            :caseInfo="selectCaseInfo?.caseInfo" />

          <Attachment
            class="py-5"
            :caseInfo="selectCaseInfo?.caseInfo" />
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
.PENDING {
  background-color: rgba(45, 142, 255, 100%);
}

.IN_PROGRESS {
  background-color: rgba(103, 215, 255, 100%);
}

.COMPLETED {
  background-color: rgba(82, 196, 26, 100%);
}

.BLOCKED {
  background-color: rgba(255, 165, 43, 100%);
}

.meeting-container {
  border: 1px solid var(--border-text-box);
  border-radius: 4px;
  background-color: #fafafa;
}
</style>
