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

export type TabKey = 'info' | 'activity' | 'comments' | 'assocIssues' | 'assocCases'

const CaseDetailTab = defineAsyncComponent(() => import('@/views/test/case/list/flat/detail/index.vue'));
const ReviewTab = defineAsyncComponent(() => import('@/views/test/case/list/flat/detail/Review.vue'));
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
const appInfo = ref(appContext.getAccessApp());

// UI state
const activeKey = ref<TabKey>('info');
const detailRef = ref<HTMLElement | null>(null);

// Case data & permissions
const caseAuth = ref<{ [key: number]: CaseActionAuth[] }>({});
const caseDetail = ref<CaseDetail>({} as CaseDetail);

// List navigation cache
const pageNo = ref(1);
const total = ref(1);
const filters = ref([]);

// Navigation guard flags
const hasLastData = ref(false);
const hasBeforeData = ref(false);
let isFirstNavigation = true;

let db;
/**
 * Navigate to adjacent case (previous/next) and refresh detail.
 */
const fetchAdjacentCase = async (direction: 'before' | 'after') => {
  if (!db) {
    db = new XCanDexie<IData>('parameter');
  }

  const cacheParamsKey = `${props.userInfo?.id}${props.queryParams.projectId}_case`;

  // Initialize from props or cache for first navigation
  if (isFirstNavigation) {
    if (props.queryParams && Object.keys(props.queryParams).length) {
      const _params = props.queryParams;
      filters.value = _params?.filters || [];
      total.value = +_params.total;
      pageNo.value = calculateAbsoluteIndex(+_params.total, +_params.pageNo, +_params.pageSize, +_params.currIndex + 1);
      db.add({ id: cacheParamsKey, data: props.queryParams });
    } else {
      const [, _data] = await db.get(cacheParamsKey);
      if (_data?.data) {
        const _params = _data.data;
        total.value = +_params.total;
        filters.value = _params?.filters || [];
        pageNo.value = calculateAbsoluteIndex(+_params.total, +_params.pageNo, +_params.pageSize, +_params.currIndex + 1);
      }
    }
    isFirstNavigation = false;
  }

  if (direction === 'before') {
    if (pageNo.value <= 1) {
      notification.success(t('testCase.messages.firstDataTip'));
      hasBeforeData.value = true;
      return;
    } else {
      hasLastData.value = false;
    }
  }

  if (direction === 'after') {
    if (pageNo.value >= total.value) {
      notification.success(t('testCase.messages.lastDataTip'));
      hasLastData.value = true;
      return;
    } else {
      hasBeforeData.value = false;
    }
  }

  direction === 'before' ? pageNo.value-- : pageNo.value++;

  const params = { pageNo: pageNo.value, pageSize: 1, enabledModuleGroup: false, filters: filters.value, projectId: projectId.value };
  const [listError, listRes] = await testCase.getCaseList({ infoScope: PageQuery.InfoScope.DETAIL, ...params });
  if (listError) {
    return;
  }
  if (!listRes.data?.list?.length) {
    return;
  }

  total.value = +listRes.data.total;
  await fetchCaseDetail(listRes.data.list[0].id);
};

/**
 * Calculate absolute index within the dataset for given page and offset.
 */
const calculateAbsoluteIndex = (_total, _pageNo, _pageSize, n) => {
  const startIndex = (_pageNo - 1) * _pageSize;
  return startIndex + n;
};

// eslint-disable-next-line @typescript-eslint/no-empty-function
const updateTabPane = inject<(data: any) => void>('updateTabPane', () => { });

/**
 * Load case detail, compute permissions and update related tab meta.
 */
const fetchCaseDetail = async (id: number) => {
  destroyInactiveTabPane.value = true;
  emits('update:loading', true);
  const [error, { data }] = await testCase.getCaseDetail(id);
  emits('update:loading', false);
  if (error) {
    return;
  }
  caseDetail.value = data;

  updateTabPane({ _id: props.tabKey, caseId: data.id, name: data.name, value: 'caseInfo' });
  await fetchPlanPermissions(data.planId);
  caseAuth.value[data.id] = getActionAuth(planAuthList.value);
  if (caseAuth.value[data.id].includes('resetTestResult')) {
    if (!funcPlanAuth.value) {
      caseAuth.value[data.id] = caseAuth.value[data.id].filter(i => i !== 'resetTestResult');
    }
  }
  if (caseAuth.value[data.id].includes('retestResult')) {
    if (!funcPlanAuth.value && userInfo.value?.id !== data.testerId) {
      caseAuth.value[data.id] = caseAuth.value[data.id].filter(i => i !== 'retestResult');
    }
  }
};

const planAuthList = ref<string[]>([]);
const funcPlanAuth = ref(true);
/**
 * Resolve and normalize effective permissions for current plan.
 */
const fetchPlanPermissions = async (planId) => {
  if (isAdmin.value) {
    funcPlanAuth.value = true;
    planAuthList.value = enumUtils.getEnumValues(FuncPlanPermission);
    return;
  }
  const [error, { data }] = await testPlan.getCurrentAuthByPlanId(planId);
  if (error) {
    return;
  }
  if (data.funcPlanAuth) {
    funcPlanAuth.value = true;
    planAuthList.value = data.permissions.map(item => {
      return item.value;
    });
  } else {
    funcPlanAuth.value = false;
    planAuthList.value = enumUtils.getEnumValues(FuncPlanPermission);
  }
  if (props.userInfo?.id === caseDetail.value?.testerId && !planAuthList.value.includes(FuncPlanPermission.TEST)) {
    planAuthList.value.push(FuncPlanPermission.TEST);
  }
};
const destroyInactiveTabPane = ref(true);

// Derived action permissions for current case
const actionAuth = computed(() => {
  return caseAuth.value[caseDetail.value?.id || 0] || [];
});

/**
 * Emit action event for parent to handle.
 */
const emitActionClick = (type: any) => {
  emits('onClick', type, caseDetail.value as CaseDetail);
};

// Copy shareable link to clipboard
const copyShareLink = async () => {
  if (!caseDetail.value) {
    return;
  }
  const message = `${window.location.origin}/test#cases?
  id=${caseDetail.value.id}&projectId=${projectId.value}&
  name=${caseDetail.value.name}&currIndex=${props.currIndex}&
  total=${props.queryParams.total}&pageNo=${props.queryParams.pageNo}&
  pageSize=${props.queryParams.pageSize}`;

  toClipboard(message).then(() => {
    notification.success(t('actions.tips.copySuccess'));
  }).catch(() => {
    notification.error(t('actions.tips.copyFailed'));
  });
};

// Issue modal visibility
const taskModalVisible = ref(false);
const openAddBugModal = () => {
  taskModalVisible.value = true;
};

const handleAddIssueOk = async () => {
  onEditSuccess();
};

const smartCommentRef = ref<any>(null);
const reviewRecordRef = ref();

/**
 * Handle edit completed and refresh detail if in tab mode.
 */
const onEditSuccess = () => {
  if (!caseDetail.value?.id) return;
  emits('editSuccess', caseDetail.value.id);
  if (props.tabKey) {
    fetchCaseDetail(caseDetail.value.id);
  }
};

const buttonGroupRef = ref<HTMLElement>();
const buttonHeight = ref(0);
let buttonErd:Erd;

// Observe action bar size changes to layout tabs height
const onButtonResize = debounce(duration.resize, (element) => {
  buttonHeight.value = element.offsetHeight;
});

const activityLoading = ref(false);
const activityNotify = ref(0);
const activityParams = ref({
  filters: [{ key: 'targetType', value: CombinedTargetType.FUNC_CASE, op: SearchCriteria.OpEnum.Equal }]
});

/**
 * Refresh content of current active tab or detail.
 */
const refreshActiveTabContent = () => {
  if (['info', 'assocIssues', 'assocCases'].includes(activeKey.value) && caseDetail.value?.id) {
    fetchCaseDetail(caseDetail.value.id);
  }

  if (activeKey.value === 'activity') {
    activityNotify.value++;
  }

  if (activeKey.value === 'comments' && smartCommentRef.value) {
    smartCommentRef.value.refresh();
  }

  if (reviewRecordRef.value) {
    reviewRecordRef.value.refresh();
  }
};

// Count referenced tasks by type
const countRefTasksByType = (type: TaskType) => {
  return (caseDetail.value?.refTaskInfos || []).filter(item => item.taskType.value === type).length || 0;
};

const activityList = ref([]);
const onActivityListChange = (data) => {
  activityList.value = data;
};

// External notify triggers reload
watch(() => props.notify, () => {
  if (props.caseId) {
    fetchCaseDetail(props.caseId);
  }
});

watch(() => activeKey.value, () => {
  destroyInactiveTabPane.value = false;
}, {
  immediate: true
});

watch(() => props.caseInfo, (newValue) => {
  destroyInactiveTabPane.value = true;
  if (newValue) {
    caseDetail.value = newValue;
    caseAuth.value = props.caseActionAuth || {};
  }

  if (!newValue && props.tabKey && props.caseId) {
    fetchCaseDetail(props.caseId);
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
    if (!props.queryParams?.total || !props.queryParams.pageNo || !props.queryParams.pageSize || props.currIndex === undefined) {
      hasBeforeData.value = true;
      hasLastData.value = true;
    }
  }
  nextTick(() => {
    if (!buttonGroupRef.value || buttonErd) {
      return;
    }

    buttonErd = elementResizeDetector();
    buttonErd.listenTo(buttonGroupRef.value, onButtonResize);
  });
});

onBeforeUnmount(() => {
  if (buttonErd) {
    buttonErd.removeListener(buttonGroupRef.value!, onButtonResize);
  }
});

defineExpose({
  activeKey
});
</script>
<template>
  <div class="h-full pl-3 pb-3 pt-1">
    <div class="flex justify-between">
      <div ref="buttonGroupRef" class="flex items-center button-combination flex-wrap">
        <template v-if="!caseDetail?.review || (caseDetail?.review && caseDetail?.reviewStatus.value === ReviewStatus.PASSED)">
          <template v-if="![CaseTestResult.PASSED, CaseTestResult.NOT_PASSED, CaseTestResult.CANCELED].includes(caseDetail.testResult?.value)">
            <Button
              :disabled="!actionAuth.includes('updateTestResult')"
              type="primary"
              class="mt-2 mr-2"
              size="small"
              @click="emitActionClick('updateTestResult_passed')">
              <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
              <span>{{ t('testCase.actions.testPassed') }}</span>
            </Button>

            <Button
              :disabled="!actionAuth.includes('updateTestResult')"
              type="primary"
              class="mt-2 mr-2"
              size="small"
              @click="emitActionClick('updateTestResult_notPassed')">
              <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
              <span>{{ t('testCase.actions.testNotPassed') }}</span>
            </Button>
          </template>

          <template v-else>
            <Button
              :disabled="!actionAuth.includes('retestResult')"
              type="primary"
              class="mt-2 mr-2"
              size="small"
              @click="emitActionClick('retestResult')">
              <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
              <span>{{ t('testCase.actions.retest') }}</span>
            </Button>
          </template>

          <Button
            v-if="+(caseDetail?.testNum || 0) > 0"
            :disabled="!actionAuth.includes('resetTestResult')"
            class="mt-2 mr-2"
            size="small"
            @click="emitActionClick('resetTestResult')">
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
          :disabled="!actionAuth.includes('updateTestResult')"
          class="mt-2 mr-2"
          size="small"
          @click="emitActionClick('updateTestResult_blocked')">
          <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
          {{ t('testCase.actions.setBlocked') }}
        </Button>

        <Button
          v-if="caseDetail?.review && caseDetail?.reviewStatus.value === ReviewStatus.PENDING"
          :disabled="!actionAuth.includes('review')"
          type="primary"
          class="mt-2 mr-2"
          size="small"
          @click="emitActionClick('review')">
          <Icon class="mr-1" icon="icon-pingshen" />
          {{ t('common.review') }}
        </Button>

        <Button
          v-if="!caseDetail?.review || (caseDetail?.review && caseDetail?.reviewStatus.value === ReviewStatus.PASSED)
            && ![CaseTestResult.PASSED, CaseTestResult.NOT_PASSED, CaseTestResult.CANCELED].includes(caseDetail.testResult?.value)"
          :disabled="!actionAuth.includes('updateTestResult')"
          class="mt-2 mr-2"
          size="small"
          @click="emitActionClick('updateTestResult_canceled')">
          <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
          {{ t('actions.cancel') }}
        </Button>

        <Button
          v-if="caseDetail?.testResult?.value === CaseTestResult.NOT_PASSED"
          size="small"
          class="mt-2 mr-2"
          :disabled="!actionAuth.includes('edit')"
          @click="openAddBugModal">
          <Icon class="mr-1" icon="icon-bianji" />
          <span>{{ t('testCase.actions.submitBug') }}</span>
        </Button>

        <Button
          size="small"
          class="mt-2 mr-2"
          :disabled="!actionAuth.includes('edit')"
          @click="emitActionClick('edit')">
          <Icon class="mr-1" icon="icon-bianji" />
          <span>{{ t('actions.edit') }}</span>
        </Button>

        <Button
          :disabled="!actionAuth.includes('clone')"
          class="mt-2 mr-2"
          size="small"
          @click="emitActionClick('clone')">
          <Icon class="mr-1" icon="icon-fuzhi" />
          <span>{{ t('actions.clone') }}</span>
        </Button>

        <Button
          :disabled="!actionAuth.includes('move')"
          class="mt-2 mr-2"
          size="small"
          @click="emitActionClick('move')">
          <Icon class="mr-1 text-3" icon="icon-yidong" />
          <span>{{ t('actions.move') }}</span>
        </Button>

        <Button
          class="mt-2 mr-2"
          size="small"
          @click="emitActionClick('addFavourite')">
          <Icon class="mr-1 text-3" :icon="caseDetail?.favourite?'icon-quxiaoshoucang':'icon-yishoucang'" />
          <span>{{ caseDetail?.favourite ? t('actions.cancelFavourite') : t('actions.addFavourite') }}</span>
        </Button>

        <Button
          class="mt-2 mr-2"
          size="small"
          @click="emitActionClick('addFollow')">
          <Icon class="mr-1 text-3" :icon="caseDetail?.follow?'icon-quxiaoguanzhu':'icon-yiguanzhu'" />
          <span>{{ caseDetail?.follow ? t('actions.cancelFollow') : t('actions.addFollow') }}</span>
        </Button>

        <Button
          :disabled="!actionAuth.includes('delete')"
          class="mt-2 mr-2"
          size="small"
          @click="emitActionClick('delete')">
          <Icon class="mr-1" icon="icon-qingchu" />
          <span>{{ t('actions.delete') }}</span>
        </Button>

        <template v-if="!['share'].includes(props.source)">
          <Button
            class="mt-2 mr-2"
            size="small"
            @click="copyShareLink">
            <Icon class="mr-1" icon="icon-fuzhi" />
            <span>{{ t('actions.copyLink') }}</span>
          </Button>
        </template>
      </div>

      <div v-if="props.tabKey" class="whitespace-nowrap">
        <Button
          size="small"
          class="mt-2 mr-2"
          :disabled="hasBeforeData"
          @click="fetchAdjacentCase('before')">
          <Icon class="mr-1" icon="icon-chakanshangyitiao" />{{ t('actions.previousItem') }}
        </Button>

        <Button
          size="small"
          class="mt-2 mr-2"
          :disabled="hasLastData"
          @click="fetchAdjacentCase('after')">
          <Icon class="mr-1" icon="icon-chakanxiayitiao" />{{ t('actions.nextItem') }}
        </Button>
      </div>
    </div>

    <Tabs
      v-model:activeKey="activeKey"
      class="mt-1"
      :destroyInactiveTabPane="destroyInactiveTabPane"
      :style="{height: `calc(100% - ${buttonHeight}px)`}">
      <template #rightExtra>
        <Button
          type="link"
          size="small"
          class="mr-2"
          @click="refreshActiveTabContent">
          <Icon
            icon="icon-shuaxin"
            class="mr-1" />
          {{ t('actions.refresh') }}
        </Button>
      </template>

      <TabPane
        key="info"
        class="h-full"
        :tab="t('common.basicInfo')">
        <CaseDetailTab
          :caseDetail="caseDetail"
          :actionAuth="actionAuth"
          @editSuccess="onEditSuccess" />
      </TabPane>

      <TabPane
        v-if="!!caseDetail?.review"
        key="reviewRecord"
        :tab="t('common.reviewRecord')">
        <ReviewTab
          ref="reviewRecordRef"
          :caseDetail="caseDetail" />
      </TabPane>

      <TabPane key="assocCases">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.useCase') }}</span>
            <span>({{ caseDetail?.refCaseInfos?.length || 0 }})</span>
          </div>
        </template>

        <AssocCasesTab
          :dataSource="caseDetail?.refCaseInfos"
          :projectId="projectId"
          :caseId="caseDetail?.id"
          @editSuccess="onEditSuccess" />
      </TabPane>

      <TabPane key="assocIssues">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.task') }}</span>
            <span>({{ countRefTasksByType(TaskType.TASK) }})</span>
          </div>
        </template>
        <AssocIssuesTab
          :key="TaskType.TASK"
          :dataSource="caseDetail?.refTaskInfos"
          :projectId="projectId"
          :caseId="caseDetail?.id"
          :title="t('common.task')"
          :taskType="TaskType.TASK"
          :tips="t('testCase.messages.assocTaskTip')"
          @editSuccess="onEditSuccess" />
      </TabPane>

      <TabPane key="assocRequirements">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.requirement') }}</span>
            <span>({{ countRefTasksByType(TaskType.REQUIREMENT) }})</span>
          </div>
        </template>
        <AssocIssuesTab
          :key="TaskType.REQUIREMENT"
          :projectId="projectId"
          :userInfo="props.userInfo"
          :appInfo="appInfo"
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
            <span>({{ countRefTasksByType(TaskType.STORY) }})</span>
          </div>
        </template>
        <AssocIssuesTab
          :key="TaskType.STORY"
          :projectId="projectId"
          :userInfo="props.userInfo"
          :appInfo="appInfo"
          :dataSource="caseDetail?.refTaskInfos || []"
          :caseId="caseDetail?.id"
          :title="t('common.story')"
          :taskType="TaskType.STORY"
          :tips="t('testCase.messages.assocStoryTip')"
          @editSuccess="onEditSuccess" />
      </TabPane>

      <TabPane key="assocBug">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('common.bug') }}</span>
            <span>({{ countRefTasksByType(TaskType.BUG) }})</span>
          </div>
        </template>
        <AssocIssuesTab
          :key="TaskType.BUG"
          :projectId="projectId"
          :userInfo="props.userInfo"
          :appInfo="appInfo"
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
            <span>({{ countRefTasksByType(TaskType.API_TEST) }})</span>
          </div>
        </template>
        <AssocIssuesTab
          :key="TaskType.API_TEST"
          :projectId="projectId"
          :userInfo="props.userInfo"
          :appInfo="appInfo"
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
            <span>({{ countRefTasksByType(TaskType.SCENARIO_TEST) }})</span>
          </div>
        </template>
        <AssocIssuesTab
          :key="TaskType.SCENARIO_TEST"
          :projectId="projectId"
          :userInfo="props.userInfo"
          :appInfo="appInfo"
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
          ref="smartCommentRef"
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
          v-model:spinning="activityLoading"
          :action="`${TESTER}/activity?mainTargetId=${caseDetail.id}`"
          :hideNoData="!!activityList.length"
          :params="activityParams"
          :lineHeight="32"
          :notify="activityNotify"
          transition
          class="h-full"
          @change="onActivityListChange">
          <ActivityInfo :dataSource="activityList" infoKey="description" />
        </Scroll>
      </TabPane>
    </Tabs>

    <AsyncComponent :visible="taskModalVisible">
      <AddIssueModal
        v-model:visible="taskModalVisible"
        :projectId="projectId"
        :appInfo="appInfo"
        :assigneeId="caseDetail?.developerId"
        :userInfo="userInfo"
        :refCaseIds="[caseDetail?.id]"
        :name="t('testCase.messages.testNotPassedName', {name: caseDetail?.name})"
        :description="caseDetail?.testRemark"
        :taskType="TaskType.BUG"
        :confirmerId="caseDetail?.testerId"
        @ok="handleAddIssueOk" />
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
