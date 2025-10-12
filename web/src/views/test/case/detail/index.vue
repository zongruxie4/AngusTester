<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onBeforeUnmount, onMounted, ref, Ref, watch } from 'vue';
import { ActivityInfo, AsyncComponent, Icon, notification, Scroll, SmartComment } from '@xcan-angus/vue-ui';
import { Button, Popover, TabPane, Tabs } from 'ant-design-vue';
import {
  appContext, duration, PageQuery, ReviewStatus, SearchCriteria, TESTER, toClipboard, XCanDexie, enumUtils
} from '@xcan-angus/infra';
import elementResizeDetector, { Erd } from 'element-resize-detector';
import { debounce } from 'throttle-debounce';
import { useI18n } from 'vue-i18n';

import { CaseTestResult, CombinedTargetType, FuncPlanPermission, TaskType } from '@/enums/enums';
import { testCase, testPlan } from '@/api/tester';
import { CaseDetail } from '@/views/test/types';
import { CaseActionAuth, getActionAuth } from '@/views/test/case/types';

const CaseDetailTab = defineAsyncComponent(() => import('@/views/test/case/list/flat/detail/index.vue'));
const ReviewRecordTab = defineAsyncComponent(() => import('@/views/test/case/list/flat/detail/ReviewRecord.vue'));
const AssocIssuesTab = defineAsyncComponent(() => import('@/views/test/case/list/flat/detail/AssocIssues.vue'));
const AssocCasesTab = defineAsyncComponent(() => import('@/views/test/case/list/flat/detail/AssocCases.vue'));
const AddIssueModal = defineAsyncComponent(() => import('@/views/issue/issue/list/Edit.vue'));

interface IData {
  id: string;
  data: any
}

export interface Props {
  source:'list' | 'home' | 'share',
  caseId?: number;
  tabKey?:string;
  currIndex?:number;
  userInfo?:{id:number};
  caseInfo?: CaseDetail,
  caseActionAuth?: {[key:number]:CaseActionAuth[]},
  queryParams?: any
  scrollNotify?: number;
  notify?: number;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  source: undefined,
  caseId: -1,
  tabKey: '',
  currIndex: 0,
  userInfo: undefined,
  caseInfo: undefined,
  caseActionAuth: undefined,
  queryParams: undefined,
  scrollNotify: 0,
  notify: 0
});

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'onClick', type: CaseActionAuth, value: CaseDetail): void;
  (e: 'editSuccess', id:number): void;
  (e: 'getInfo', id:number): void;
  (e: 'updateCaseTab', value): void;
  (e: 'update:loading', value: boolean): void;
}>();

const isAdmin = computed(() => appContext.isAdmin());
const projectId = inject<Ref<number>>('projectId', ref(-1));
const userInfo = ref(appContext.getUser());

// UI state
type TabKey = 'info' | 'activity' | 'comments' | 'assocIssues' | 'assocCases';
const activeKey = ref<TabKey>('info');
const detailRef = ref<HTMLElement | null>(null);

// Case data & permissions
const casePermissions = ref<{ [key: number]: CaseActionAuth[] }>({});
const caseDetail = ref<CaseDetail>({} as CaseDetail);

// List navigation cache
const currentPageNumber = ref(1);
const totalCount = ref(1);
const searchFilters = ref([]);

// Navigation guard flags
const hasNextData = ref(false);
const hasPreviousData = ref(false);
let isFirstNavigation = true;

let parameterDatabase;
/**
 * Navigate to adjacent case (previous/next) and refresh detail.
 */
const navigateToAdjacentCase = async (direction: 'before' | 'after') => {
  if (!parameterDatabase) {
    parameterDatabase = new XCanDexie<IData>('parameter');
  }

  const caseListCacheKey = `${props.userInfo?.id}${props.queryParams.projectId}_case`;

  // Initialize from props or cache for first navigation
  if (isFirstNavigation) {
    if (props.queryParams && Object.keys(props.queryParams).length) {
      const queryParameters = props.queryParams;
      searchFilters.value = queryParameters?.filters || [];
      totalCount.value = +queryParameters.total;
      currentPageNumber.value = calculateAbsoluteIndex(
        +queryParameters.total, +queryParameters.pageNo,
        +queryParameters.pageSize, +queryParameters.currIndex + 1
      );
      parameterDatabase.add({ id: caseListCacheKey, data: props.queryParams });
    } else {
      const [, cachedData] = await parameterDatabase.get(caseListCacheKey);
      if (cachedData?.data) {
        const queryParameters = cachedData.data;
        totalCount.value = +queryParameters.total;
        searchFilters.value = queryParameters?.filters || [];
        currentPageNumber.value = calculateAbsoluteIndex(
          +queryParameters.total, +queryParameters.pageNo,
          +queryParameters.pageSize, +queryParameters.currIndex + 1
        );
      }
    }
    isFirstNavigation = false;
  }

  if (direction === 'before') {
    if (currentPageNumber.value <= 1) {
      notification.success(t('testCase.messages.firstDataTip'));
      hasPreviousData.value = true;
      return;
    } else {
      hasNextData.value = false;
    }
  }

  if (direction === 'after') {
    if (currentPageNumber.value >= totalCount.value) {
      notification.success(t('testCase.messages.lastDataTip'));
      hasNextData.value = true;
      return;
    } else {
      hasPreviousData.value = false;
    }
  }

  direction === 'before' ? currentPageNumber.value-- : currentPageNumber.value++;

  const listQueryParams = {
    pageNo: currentPageNumber.value,
    pageSize: 1,
    enabledModuleGroup: false,
    filters: searchFilters.value,
    projectId: projectId.value
  };
  const [listError, listResponse] = await testCase.getCaseList({ infoScope: PageQuery.InfoScope.DETAIL, ...listQueryParams });
  if (listError) {
    return;
  }
  if (!listResponse.data?.list?.length) {
    return;
  }

  totalCount.value = +listResponse.data.total;
  await loadCaseDetail(listResponse.data.list[0].id);
};

/**
 * Calculate absolute index within the dataset for given page and offset.
 */
const calculateAbsoluteIndex = (_totalCount, pageNumber, pageSize, currentIndex) => {
  const startIndex = (pageNumber - 1) * pageSize;
  return startIndex + currentIndex;
};

// eslint-disable-next-line @typescript-eslint/no-empty-function
const updateTabPane = inject<(data: any) => void>('updateTabPane', () => { });

/**
 * Load case detail, compute permissions and update related tab meta.
 */
const loadCaseDetail = async (caseId: number) => {
  destroyInactiveTabPane.value = true;
  emits('update:loading', true);
  const [error, { data }] = await testCase.getCaseDetail(caseId);
  emits('update:loading', false);
  if (error) {
    return;
  }
  caseDetail.value = data;

  updateTabPane({ _id: props.tabKey, caseId: data.id, name: data.name, value: 'caseInfo' });
  await loadPlanPermissions(data.planId);
  casePermissions.value[data.id] = getActionAuth(planPermissionList.value);
  if (casePermissions.value[data.id].includes('resetTestResult')) {
    if (!hasFunctionalPlanPermission.value) {
      casePermissions.value[data.id] = casePermissions.value[data.id]
        .filter(permission => permission !== 'resetTestResult');
    }
  }
  if (casePermissions.value[data.id].includes('retestResult')) {
    if (!hasFunctionalPlanPermission.value && userInfo.value?.id !== data.testerId) {
      casePermissions.value[data.id] = casePermissions.value[data.id]
        .filter(permission => permission !== 'retestResult');
    }
  }
};

const planPermissionList = ref<string[]>([]);
const hasFunctionalPlanPermission = ref(true);

/**
 * Resolve and normalize effective permissions for current plan.
 */
const loadPlanPermissions = async (planId) => {
  if (isAdmin.value) {
    hasFunctionalPlanPermission.value = true;
    planPermissionList.value = enumUtils.getEnumValues(FuncPlanPermission);
    return;
  }
  const [error, { data }] = await testPlan.getCurrentAuthByPlanId(planId);
  if (error) {
    return;
  }
  if (data.funcPlanAuth) {
    hasFunctionalPlanPermission.value = true;
    planPermissionList.value = data.permissions.map(permission => {
      return permission.value;
    });
  } else {
    hasFunctionalPlanPermission.value = false;
    planPermissionList.value = enumUtils.getEnumValues(FuncPlanPermission);
  }
  if (props.userInfo?.id === caseDetail.value?.testerId &&
    !planPermissionList.value.includes(FuncPlanPermission.TEST)) {
    planPermissionList.value.push(FuncPlanPermission.TEST);
  }
};

const destroyInactiveTabPane = ref(true);

// Derived action permissions for current case
const currentCaseActionPermissions = computed(() => {
  return casePermissions.value[caseDetail.value?.id || 0] || [];
});

/**
 * Emit action event for parent to handle.
 */
const handleActionClick = (actionType: any) => {
  emits('onClick', actionType, caseDetail.value as CaseDetail);
};

// Copy shareable link to clipboard
const copyShareableLink = async () => {
  if (!caseDetail.value) {
    return;
  }
  const shareableLink = `${window.location.origin}/test#cases?
  id=${caseDetail.value.id}&projectId=${projectId.value}&
  name=${caseDetail.value.name}&currIndex=${props.currIndex}&
  total=${props.queryParams.total}&pageNo=${props.queryParams.pageNo}&
  pageSize=${props.queryParams.pageSize}`;

  toClipboard(shareableLink).then(() => {
    notification.success(t('actions.tips.copySuccess'));
  }).catch(() => {
    notification.error(t('actions.tips.copyFailed'));
  });
};

// Issue modal visibility
const isAddBugModalVisible = ref(false);
const openAddBugModal = () => {
  isAddBugModalVisible.value = true;
};

const handleAddIssueSuccess = async () => {
  onEditSuccess();
};

const smartCommentComponentRef = ref<any>(null);
const reviewRecordComponentRef = ref();

/**
 * Handle edit completed and refresh detail if in tab mode.
 */
const onEditSuccess = () => {
  if (!caseDetail.value?.id) return;
  emits('editSuccess', caseDetail.value.id);
  if (props.tabKey) {
    loadCaseDetail(caseDetail.value.id);
  }
};

const actionButtonGroupRef = ref<HTMLElement>();
const actionButtonHeight = ref(0);
let buttonResizeDetector:Erd;

// Observe action bar size changes to layout tabs height
const handleActionButtonResize = debounce(duration.resize, (element) => {
  actionButtonHeight.value = element.offsetHeight;
});

const isActivityLoading = ref(false);
const activityRefreshTrigger = ref(0);
const activityQueryParams = ref({
  filters: [{ key: 'targetType', value: CombinedTargetType.FUNC_CASE, op: SearchCriteria.OpEnum.Equal }]
});

/**
 * Refresh content of current active tab or detail.
 */
const refreshActiveTabContent = () => {
  if (['info', 'assocIssues', 'assocCases'].includes(activeKey.value) && caseDetail.value?.id) {
    loadCaseDetail(caseDetail.value.id);
  }

  if (activeKey.value === 'activity') {
    activityRefreshTrigger.value++;
  }

  if (activeKey.value === 'comments' && smartCommentComponentRef.value) {
    smartCommentComponentRef.value.refresh();
  }

  if (reviewRecordComponentRef.value) {
    reviewRecordComponentRef.value.refresh();
  }
};

// Count referenced tasks by type
const countReferencedTasksByType = (taskType: TaskType) => {
  return (caseDetail.value?.refTaskInfos || []).filter(task => task.taskType.value === taskType).length || 0;
};

const activityDataList = ref([]);
const handleActivityListChange = (activityData) => {
  activityDataList.value = activityData;
};

// External notify triggers reload
watch(() => props.notify, () => {
  if (props.caseId) {
    loadCaseDetail(props.caseId);
  }
});

watch(() => activeKey.value, () => {
  destroyInactiveTabPane.value = false;
}, {
  immediate: true
});

watch(() => props.caseInfo, (newCaseInfo) => {
  destroyInactiveTabPane.value = true;
  if (newCaseInfo) {
    caseDetail.value = newCaseInfo;
    casePermissions.value = props.caseActionAuth || {};
  }

  if (!newCaseInfo && props.tabKey && props.caseId) {
    loadCaseDetail(props.caseId);
  }

  nextTick(() => {
    if (!detailRef.value) {
      return;
    }
    detailRef.value.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  });
}, { deep: true, immediate: true });

onMounted(() => {
  if (props.tabKey) {
    if (!props.queryParams?.total || !props.queryParams.pageNo ||
      !props.queryParams.pageSize || props.currIndex === undefined
    ) {
      hasPreviousData.value = true;
      hasNextData.value = true;
    }
  }
  nextTick(() => {
    if (!actionButtonGroupRef.value || buttonResizeDetector) {
      return;
    }

    buttonResizeDetector = elementResizeDetector();
    buttonResizeDetector.listenTo(actionButtonGroupRef.value, handleActionButtonResize);
  });
});

onBeforeUnmount(() => {
  if (buttonResizeDetector) {
    buttonResizeDetector.removeListener(actionButtonGroupRef.value!, handleActionButtonResize);
  }
});

defineExpose({
  activeKey
});
</script>
<template>
  <div class="h-full pl-3 pb-3 pt-1">
    <div class="flex justify-between">
      <div ref="actionButtonGroupRef" class="flex items-center button-combination flex-wrap">
        <template v-if="!caseDetail?.review || (caseDetail?.review && caseDetail?.reviewStatus.value === ReviewStatus.PASSED)">
          <template v-if="![CaseTestResult.PASSED, CaseTestResult.NOT_PASSED, CaseTestResult.CANCELED].includes(caseDetail.testResult?.value)">
            <Button
              :disabled="!currentCaseActionPermissions.includes('updateTestResult')"
              type="primary"
              class="mt-2 mr-2"
              size="small"
              @click="handleActionClick('updateTestResult_passed')">
              <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
              <span>{{ t('testCase.actions.testPassed') }}</span>
            </Button>

            <Button
              :disabled="!currentCaseActionPermissions.includes('updateTestResult')"
              type="primary"
              class="mt-2 mr-2"
              size="small"
              @click="handleActionClick('updateTestResult_notPassed')">
              <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
              <span>{{ t('testCase.actions.testNotPassed') }}</span>
            </Button>
          </template>

          <template v-else>
            <Button
              :disabled="!currentCaseActionPermissions.includes('retestResult')"
              type="primary"
              class="mt-2 mr-2"
              size="small"
              @click="handleActionClick('retestResult')">
              <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
              <span>{{ t('testCase.actions.retest') }}</span>
            </Button>
          </template>

          <Button
            v-if="+(caseDetail?.testNum || 0) > 0"
            :disabled="!currentCaseActionPermissions.includes('resetTestResult')"
            class="mt-2 mr-2"
            size="small"
            @click="handleActionClick('resetTestResult')">
            <Icon class="mr-1" icon="icon-zhongzhiceshijieguo" />
            <span>{{ t('testCase.actions.resetTestResult') }}</span>
            <Popover placement="bottom">
              <template #content>
                <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                  {{ t('testCase.messages.resetTestResultTip') }}
                </div>
              </template>
              <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
            </Popover>
          </Button>
        </template>

        <Button
          v-if="(!caseDetail?.review || (caseDetail?.review && caseDetail?.reviewStatus.value === ReviewStatus.PASSED))
            && ![CaseTestResult.PASSED, CaseTestResult.NOT_PASSED, CaseTestResult.CANCELED].includes(caseDetail.testResult?.value)"
          :disabled="!currentCaseActionPermissions.includes('updateTestResult')"
          class="mt-2 mr-2"
          size="small"
          @click="handleActionClick('updateTestResult_blocked')">
          <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
          {{ t('testCase.actions.setBlocked') }}
        </Button>

        <Button
          v-if="caseDetail?.review && caseDetail?.reviewStatus.value === ReviewStatus.PENDING"
          :disabled="!currentCaseActionPermissions.includes('review')"
          type="primary"
          class="mt-2 mr-2"
          size="small"
          @click="handleActionClick('review')">
          <Icon class="mr-1" icon="icon-pingshen" />
          {{ t('common.review') }}
        </Button>

        <Button
          v-if="!caseDetail?.review || (caseDetail?.review && caseDetail?.reviewStatus.value === ReviewStatus.PASSED)
            && ![CaseTestResult.PASSED, CaseTestResult.NOT_PASSED, CaseTestResult.CANCELED].includes(caseDetail.testResult?.value)"
          :disabled="!currentCaseActionPermissions.includes('updateTestResult')"
          class="mt-2 mr-2"
          size="small"
          @click="handleActionClick('updateTestResult_canceled')">
          <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
          {{ t('actions.cancel') }}
        </Button>

        <Button
          v-if="caseDetail?.testResult?.value === CaseTestResult.NOT_PASSED"
          size="small"
          class="mt-2 mr-2"
          :disabled="!currentCaseActionPermissions.includes('edit')"
          @click="openAddBugModal">
          <Icon class="mr-1" icon="icon-bianji" />
          <span>{{ t('testCase.actions.submitBug') }}</span>
        </Button>

        <Button
          size="small"
          class="mt-2 mr-2"
          :disabled="!currentCaseActionPermissions.includes('edit')"
          @click="handleActionClick('edit')">
          <Icon class="mr-1" icon="icon-bianji" />
          <span>{{ t('actions.edit') }}</span>
        </Button>

        <Button
          :disabled="!currentCaseActionPermissions.includes('clone')"
          class="mt-2 mr-2"
          size="small"
          @click="handleActionClick('clone')">
          <Icon class="mr-1" icon="icon-fuzhi" />
          <span>{{ t('actions.clone') }}</span>
        </Button>

        <Button
          :disabled="!currentCaseActionPermissions.includes('move')"
          class="mt-2 mr-2"
          size="small"
          @click="handleActionClick('move')">
          <Icon class="mr-1 text-3" icon="icon-yidong" />
          <span>{{ t('actions.move') }}</span>
        </Button>

        <Button
          class="mt-2 mr-2"
          size="small"
          @click="handleActionClick('addFavourite')">
          <Icon class="mr-1 text-3" :icon="caseDetail?.favourite?'icon-quxiaoshoucang':'icon-yishoucang'" />
          <span>{{ caseDetail?.favourite ? t('actions.cancelFavourite') : t('actions.addFavourite') }}</span>
        </Button>

        <Button
          class="mt-2 mr-2"
          size="small"
          @click="handleActionClick('addFollow')">
          <Icon class="mr-1 text-3" :icon="caseDetail?.follow?'icon-quxiaoguanzhu':'icon-yiguanzhu'" />
          <span>{{ caseDetail?.follow ? t('actions.cancelFollow') : t('actions.addFollow') }}</span>
        </Button>

        <Button
          :disabled="!currentCaseActionPermissions.includes('delete')"
          class="mt-2 mr-2"
          size="small"
          @click="handleActionClick('delete')">
          <Icon class="mr-1" icon="icon-qingchu" />
          <span>{{ t('actions.delete') }}</span>
        </Button>

        <template v-if="!['share'].includes(props.source)">
          <Button
            class="mt-2 mr-2"
            size="small"
            @click="copyShareableLink">
            <Icon class="mr-1" icon="icon-fuzhi" />
            <span>{{ t('actions.copyLink') }}</span>
          </Button>
        </template>
      </div>

      <div v-if="props.tabKey" class="whitespace-nowrap">
        <Button
          size="small"
          class="mt-2 mr-2"
          :disabled="hasPreviousData"
          @click="navigateToAdjacentCase('before')">
          <Icon class="mr-1" icon="icon-chakanshangyitiao" />
          {{ t('actions.previousItem') }}
        </Button>

        <Button
          size="small"
          class="mt-2 mr-2"
          :disabled="hasNextData"
          @click="navigateToAdjacentCase('after')">
          <Icon class="mr-1" icon="icon-chakanxiayitiao" />
          {{ t('actions.nextItem') }}
        </Button>
      </div>
    </div>

    <Tabs
      v-model:activeKey="activeKey"
      class="mt-1"
      :destroyInactiveTabPane="destroyInactiveTabPane"
      :style="{height: `calc(100% - ${actionButtonHeight}px)`}">
      <template #rightExtra>
        <Button
          type="link"
          size="small"
          class="mr-2"
          @click="refreshActiveTabContent">
          <Icon icon="icon-shuaxin" class="mr-1" />
          {{ t('actions.refresh') }}
        </Button>
      </template>

      <TabPane
        key="info"
        class="h-full"
        :tab="t('common.detail')">
        <CaseDetailTab
          :caseDetail="caseDetail"
          :actionAuth="currentCaseActionPermissions"
          @editSuccess="onEditSuccess" />
      </TabPane>

      <TabPane
        v-if="!!caseDetail?.review"
        key="reviewRecord"
        :tab="t('common.reviewRecord')">
        <ReviewRecordTab
          ref="reviewRecordComponentRef"
          :caseDetail="caseDetail" />
      </TabPane>

      <TabPane key="assocCases">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.assocCases') }}</span>
            <span>({{ caseDetail?.refCaseInfos?.length || 0 }})</span>
          </div>
        </template>

        <AssocCasesTab
          :dataSource="caseDetail?.refCaseInfos"
          :projectId="projectId"
          :caseId="caseDetail?.id"
          @editSuccess="onEditSuccess" />
      </TabPane>

      <TabPane key="assocRequirements">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.requirement') }}</span>
            <span>({{ countReferencedTasksByType(TaskType.REQUIREMENT) }})</span>
          </div>
        </template>
        <AssocIssuesTab
          :projectId="projectId"
          :userInfo="props.userInfo"
          :dataSource="caseDetail?.refTaskInfos || []"
          :caseId="caseDetail?.id"
          :title="t('common.requirement')"
          :taskType="TaskType.REQUIREMENT"
          :tips="t('testCase.messages.assocRequirementTip')"
          @editSuccess="onEditSuccess" />
      </TabPane>

      <TabPane key="assocStory">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.story') }}</span>
            <span>({{ countReferencedTasksByType(TaskType.STORY) }})</span>
          </div>
        </template>
        <AssocIssuesTab
          :projectId="projectId"
          :userInfo="props.userInfo"
          :dataSource="caseDetail?.refTaskInfos || []"
          :caseId="caseDetail?.id"
          :title="t('common.story')"
          :taskType="TaskType.STORY"
          :tips="t('testCase.messages.assocStoryTip')"
          @editSuccess="onEditSuccess" />
      </TabPane>

      <TabPane key="assocTasks">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.task') }}</span>
            <span>({{ countReferencedTasksByType(TaskType.TASK) }})</span>
          </div>
        </template>
        <AssocIssuesTab
          :dataSource="caseDetail?.refTaskInfos"
          :projectId="projectId"
          :caseId="caseDetail?.id"
          :title="t('common.task')"
          :taskType="TaskType.TASK"
          :tips="t('testCase.messages.assocTaskTip')"
          @editSuccess="onEditSuccess" />
      </TabPane>

      <TabPane key="assocBug">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.bug') }}</span>
            <span>({{ countReferencedTasksByType(TaskType.BUG) }})</span>
          </div>
        </template>
        <AssocIssuesTab
          :projectId="projectId"
          :userInfo="props.userInfo"
          :dataSource="caseDetail?.refTaskInfos || []"
          :caseId="caseDetail?.id"
          :title="t('common.bug')"
          :taskType="TaskType.BUG"
          :tips="t('testCase.messages.assocBugTip')"
          @editSuccess="onEditSuccess" />
      </TabPane>

      <TabPane key="assocApiTest">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.apiTest') }}</span>
            <span>({{ countReferencedTasksByType(TaskType.API_TEST) }})</span>
          </div>
        </template>
        <AssocIssuesTab
          :projectId="projectId"
          :userInfo="props.userInfo"
          :dataSource="caseDetail?.refTaskInfos || []"
          :caseId="caseDetail?.id"
          :title="t('common.apiTest')"
          :taskType="TaskType.API_TEST"
          :tips="t('testCase.messages.assocApiTestTip')"
          @editSuccess="onEditSuccess" />
      </TabPane>

      <TabPane key="assocScenarioTest">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.scenarioTest') }}</span>
            <span>({{ countReferencedTasksByType(TaskType.SCENARIO_TEST) }})</span>
          </div>
        </template>
        <AssocIssuesTab
          :projectId="projectId"
          :userInfo="props.userInfo"
          :dataSource="caseDetail?.refTaskInfos || []"
          :caseId="caseDetail?.id"
          :title="t('common.scenarioTest')"
          :taskType="TaskType.SCENARIO_TEST"
          :tips="t('testCase.messages.assocScenarioTestTip')"
          @editSuccess="onEditSuccess" />
      </TabPane>

      <TabPane
        key="comments"
        class="h-full">
        <template #tab>
          <span>{{ t('common.comment') }} ({{ caseDetail?.commentNum || 0 }})</span>
        </template>
        <SmartComment
          ref="smartCommentComponentRef"
          class="!shadow-none h-full overflow-y-auto pr-3.5"
          :targetType="CombinedTargetType.FUNC_CASE"
          avatar
          :showPublishTitle="false"
          :bordered="false"
          :public0="false"
          :userId="props.userInfo?.id"
          :targetId="caseDetail?.id"
          :action="`${TESTER}/comment`" />
      </TabPane>

      <TabPane
        key="activity"
        class="h-full">
        <template #tab>
          <span>{{ t('common.activity') }}({{ caseDetail?.activityNum || 0 }})</span>
        </template>
        <Scroll
          v-model:spinning="isActivityLoading"
          :action="`${TESTER}/activity?mainTargetId=${caseDetail.id}`"
          :hideNoData="!!activityDataList.length"
          :params="activityQueryParams"
          :lineHeight="32"
          :notify="activityRefreshTrigger"
          transition
          class="h-full"
          @change="handleActivityListChange">
          <ActivityInfo :dataSource="activityDataList" infoKey="description" />
        </Scroll>
      </TabPane>
    </Tabs>

    <AsyncComponent :visible="isAddBugModalVisible">
      <AddIssueModal
        v-model:visible="isAddBugModalVisible"
        :projectId="projectId"
        :assigneeId="caseDetail?.developerId"
        :userInfo="userInfo"
        :refCaseIds="[caseDetail?.id]"
        :name="t('testCase.messages.testNotPassedName', {name: caseDetail?.name})"
        :description="caseDetail?.testRemark"
        :taskType="TaskType.BUG"
        :confirmerId="caseDetail?.testerId"
        @ok="handleAddIssueSuccess" />
    </AsyncComponent>
  </div>
</template>
<style scoped>

:deep(.toggle-title) {
  font-size: 12px;
}

:deep(.toggle-main-container) {
  margin-top: 8px;
  padding-left: 14px;
}

:deep(.ant-tabs-nav) {
  margin-bottom: 8px;
}

:deep(.ant-tabs-nav::before) {
  border: 0;
}

:deep(.comment-list-title) {
  margin-top: -20px;
  margin-bottom: 14px;
  font-size: 12px;
}

:deep(.no-comment-img) {
  margin-top: 60px;
}
</style>
