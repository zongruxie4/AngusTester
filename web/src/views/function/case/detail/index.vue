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
import { funcCase, funcPlan } from '@/api/tester';
import { CaseDetail } from '@/views/function/types';
import { CaseActionAuth, getActionAuth } from '@/views/function/case/types';

export type TabKey = 'info' | 'activity' | 'comments' | 'assocTask' | 'assocCase'

const CaseDetailTab = defineAsyncComponent(() => import('@/views/function/case/detail/Case.vue'));
const ReviewTab = defineAsyncComponent(() => import('@/views/function/case/detail/Review.vue'));
const AssocTaskTab = defineAsyncComponent(() => import('@/views/function/case/detail/AssocTask.vue'));
const AssocCaseTab = defineAsyncComponent(() => import('@/views/function/case/detail/AssocCase.vue'));
const AddTaskModal = defineAsyncComponent(() => import('@/views/task/task/list/Edit.vue'));

interface IData {
  id: string;
  data: any
}

export interface Props {
  source:'list' | 'home' | 'share',
  caseId?: string;
  tabKey?:string;
  currIndex?:number;
  userInfo?:{id:string};
  caseInfo?: CaseDetail,
  caseActionAuth?: {[key:string]:CaseActionAuth[]},
  queryParams?: any
  scrollNotify?: number;
  notify?: number;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  source: undefined,
  caseId: '',
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
  (e: 'editSuccess', id:string): void;
  (e: 'getInfo', id:string): void;
  (e: 'updateCaseTab', value): void;
  (e: 'update:loading', value: boolean): void;
}>();

const isAdmin = computed(() => appContext.isAdmin());
const projectId = inject<Ref<string>>('projectId', ref(''));
const userInfo = ref(appContext.getUser());
const appInfo = ref(appContext.getAccessApp());

const activeKey = ref<TabKey>('info');
const detailRef = ref<HTMLElement | null>(null);

const caseAuth = ref<{ [key: string]: CaseActionAuth[] }>({});
const caseDetail = ref<CaseDetail>();

const pageNo = ref(1);
const total = ref(1);
const filters = ref([]);

const hasLastData = ref(false);
const hasBeforeData = ref(false);
let isFirstClick = true;

let db;
const getData = async (value: 'before' | 'after') => {
  if (!db) {
    db = new XCanDexie<IData>('parameter');
  }

  const cacheParamsKey = `${props.userInfo?.id}${props.queryParams.projectId}_case`;

  if (isFirstClick) {
    if (props.queryParams && Object.keys(props.queryParams).length) {
      const _params = props.queryParams;
      filters.value = _params?.filters || [];
      total.value = +_params.total;
      pageNo.value = calculateDataPosition(+_params.total, +_params.pageNo, +_params.pageSize, +_params.currIndex + 1);
      db.add({ id: cacheParamsKey, data: props.queryParams });
    } else {
      const [, _data] = await db.get(cacheParamsKey);
      if (_data?.data) {
        const _params = _data.data;
        total.value = +_params.total;
        filters.value = _params?.filters || [];
        pageNo.value = calculateDataPosition(+_params.total, +_params.pageNo, +_params.pageSize, +_params.currIndex + 1);
      }
    }
    isFirstClick = false;
  }

  if (value === 'before') {
    if (pageNo.value <= 1) {
      notification.success(t('functionCase.detail.firstDataTip'));
      hasBeforeData.value = true;
      return;
    } else {
      hasLastData.value = false;
    }
  }

  if (value === 'after') {
    if (pageNo.value >= total.value) {
      notification.success(t('functionCase.detail.lastDataTip'));
      hasLastData.value = true;
      return;
    } else {
      hasBeforeData.value = false;
    }
  }

  value === 'before' ? pageNo.value-- : pageNo.value++;

  const params = { pageNo: pageNo.value, pageSize: 1, enabledGroup: false, filters: filters.value, projectId: projectId.value };
  const [listError, listRes] = await funcCase.getCaseList({ infoScope: PageQuery.InfoScope.DETAIL, ...params });
  if (listError) {
    return;
  }
  if (!listRes.data?.list?.length) {
    return;
  }

  total.value = +listRes.data.total;
  await getCaseInfo(listRes.data.list[0].id);
};

const calculateDataPosition = (_total, _pageNo, _pageSize, n) => {
  const startIndex = (_pageNo - 1) * _pageSize;
  return startIndex + n;
};

// eslint-disable-next-line @typescript-eslint/no-empty-function
const updateTabPane = inject<(data: any) => void>('updateTabPane', () => { });

const getCaseInfo = async (id: string) => {
  destroyInactiveTabPane.value = true;
  emits('update:loading', true);
  const [error, { data }] = await funcCase.getCaseDetail(id);
  emits('update:loading', false);
  if (error) {
    return;
  }
  caseDetail.value = data;

  updateTabPane({ _id: props.tabKey, caseId: data.id, name: data.name, value: 'caseInfo' });
  await getPlanAuth(data.planId);
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
const getPlanAuth = async (planId) => {
  if (isAdmin.value) {
    funcPlanAuth.value = true;
    planAuthList.value = enumUtils.getEnumValues(FuncPlanPermission);
    return;
  }
  const [error, { data }] = await funcPlan.getCurrentAuthByPlanId(planId);
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
  if (props.userInfo?.id === caseDetail.value.testerId && !planAuthList.value.includes(FuncPlanPermission.TEST)) {
    planAuthList.value.push(FuncPlanPermission.TEST);
  }
};
const destroyInactiveTabPane = ref(true);

const actionAuth = computed(() => {
  return caseAuth.value[caseDetail.value?.id] || [];
});

const handleClick = (type: CaseActionAuth) => {
  emits('onClick', type, caseDetail.value);
};

// 复制Url
const handleCopy = async () => {
  const message = `${window.location.origin}/function#cases?
  id=${caseDetail.value.id}&projectId=${projectId.value}&
  name=${caseDetail.value.name}&currIndex=${props.currIndex}&
  total=${props.queryParams.total}&pageNo=${props.queryParams.pageNo}&
  pageSize=${props.queryParams.pageSize}`;

  toClipboard(message).then(() => {
    notification.success(t('functionCase.detail.copySuccess'));
  }).catch(() => {
    notification.error(t('functionCase.detail.copyFailed'));
  });
};

// 提Bug
const taskModalVisible = ref(false);
const addBug = () => {
  taskModalVisible.value = true;
};

const handleAddTask = async () => {
  editSuccess();
};

const smartCommentRef = ref(null);
const reviewRecordRef = ref();

const editSuccess = () => {
  emits('editSuccess', caseDetail.value.id);
  if (props.tabKey) {
    getCaseInfo(caseDetail.value.id);
  }
};

const buttonGroupRef = ref<HTMLElement>();
const buttonHeight = ref(0);
let buttonErd:Erd;

const buttonResizeHandler = debounce(duration.resize, (element) => {
  buttonHeight.value = element.offsetHeight;
});

const activityLoading = ref(false);
const activityNotify = ref(0);
const activityParams = ref({
  filters: [{ key: 'targetType', value: CombinedTargetType.FUNC_CASE, op: SearchCriteria.OpEnum.Equal }]
});

const refresh = () => {
  if (['info', 'assocTask', 'assocCase'].includes(activeKey.value)) {
    getCaseInfo(caseDetail.value.id);
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

const getRefTaskNum = (type: TaskType) => {
  return (caseDetail.value?.refTaskInfos || []).filter(item => item.taskType.value === type).length || 0;
};

const activityList = ref([]);
const getActivityList = (data) => {
  activityList.value = data;
};

watch(() => props.notify, () => {
  getCaseInfo(props.caseId);
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

  if (!newValue && props.tabKey) {
    getCaseInfo(props.caseId);
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
    buttonErd.listenTo(buttonGroupRef.value, buttonResizeHandler);
  });
});

onBeforeUnmount(() => {
  if (buttonErd) {
    buttonErd.removeListener(buttonGroupRef.value!, buttonResizeHandler);
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
          <template v-if="![CaseTestResult.PASSED, CaseTestResult.NOT_PASSED, CaseTestResult.CANCELED].includes(caseDetail?.testResult?.value)">
            <Button
              :disabled="!actionAuth.includes('updateTestResult')"
              type="primary"
              class="mt-2 mr-2"
              size="small"
              @click="handleClick('updateTestResult_passed')">
              <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
              <span>{{ t('functionCase.detail.testPassed') }}</span>
            </Button>

            <Button
              :disabled="!actionAuth.includes('updateTestResult')"
              type="primary"
              class="mt-2 mr-2"
              size="small"
              @click="handleClick('updateTestResult_notPassed')">
              <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
              <span>{{ t('functionCase.detail.testNotPassed') }}</span>
            </Button>
          </template>

          <template v-else>
            <Button
              :disabled="!actionAuth.includes('retestResult')"
              type="primary"
              class="mt-2 mr-2"
              size="small"
              @click="handleClick('retestResult')">
              <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
              <span>{{ t('functionCase.detail.retest') }}</span>
            </Button>
          </template>

          <Button
            v-if="+(caseDetail?.testNum || 0) > 0"
            :disabled="!actionAuth.includes('resetTestResult')"
            class="mt-2 mr-2"
            size="small"
            @click="handleClick('resetTestResult')">
            <Icon class="mr-1" icon="icon-zhongzhiceshijieguo" />
            <span>{{ t('functionCase.detail.resetTestResult') }}</span>
            <Popover placement="bottom">
              <template #content>
                <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                  {{ t('functionCase.detail.resetTestResultTip') }}
                </div>
              </template>
              <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
            </Popover>
          </Button>
        </template>

        <Button
          v-if="(!caseDetail?.review || (caseDetail?.review && caseDetail?.reviewStatus.value === ReviewStatus.PASSED))
            && ![CaseTestResult.PASSED, CaseTestResult.NOT_PASSED, CaseTestResult.CANCELED].includes(caseDetail?.testResult?.value)"
          :disabled="!actionAuth.includes('updateTestResult')"
          class="mt-2 mr-2"
          size="small"
          @click="handleClick('updateTestResult_blocked')">
          <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
          {{ t('functionCase.detail.setBlocked') }}
        </Button>

        <Button
          v-if="!caseDetail?.review || (caseDetail?.review && caseDetail?.reviewStatus.value === ReviewStatus.PASSED)
            && ![CaseTestResult.PASSED, CaseTestResult.NOT_PASSED, CaseTestResult.CANCELED].includes(caseDetail?.testResult?.value)"
          :disabled="!actionAuth.includes('updateTestResult')"
          class="mt-2 mr-2"
          size="small"
          @click="handleClick('updateTestResult_canceled')">
          <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
          {{ t('functionCase.detail.cancel') }}
        </Button>

        <Button
          v-if="caseDetail?.testResult?.value === CaseTestResult.NOT_PASSED"
          size="small"
          class="mt-2 mr-2"
          :disabled="!actionAuth.includes('edit')"
          @click="addBug">
          <Icon class="mr-1" icon="icon-bianji" />
          <span>{{ t('functionCase.detail.submitBug') }}</span>
        </Button>

        <Button
          size="small"
          class="mt-2 mr-2"
          :disabled="!actionAuth.includes('edit')"
          @click="handleClick('edit')">
          <Icon class="mr-1" icon="icon-bianji" />
          <span>{{ t('functionCase.detail.edit') }}</span>
        </Button>

        <Button
          :disabled="!actionAuth.includes('clone')"
          class="mt-2 mr-2"
          size="small"
          @click="handleClick('clone')">
          <Icon class="mr-1" icon="icon-fuzhi" />
          <span>{{ t('functionCase.detail.clone') }}</span>
        </Button>

        <Button
          :disabled="!actionAuth.includes('move')"
          class="mt-2 mr-2"
          size="small"
          @click="handleClick('move')">
          <Icon class="mr-1 text-3" icon="icon-yidong" />
          <span>{{ t('functionCase.detail.move') }}</span>
        </Button>

        <Button
          class="mt-2 mr-2"
          size="small"
          @click="handleClick('favourite')">
          <Icon class="mr-1 text-3" :icon="caseDetail?.favourite?'icon-quxiaoshoucang':'icon-yishoucang'" />
          <span>{{ caseDetail?.favourite ? t('functionCase.detail.unfavourite') : t('functionCase.detail.favourite') }}</span>
        </Button>

        <Button
          class="mt-2 mr-2"
          size="small"
          @click="handleClick('follow')">
          <Icon class="mr-1 text-3" :icon="caseDetail?.follow?'icon-quxiaoguanzhu':'icon-yiguanzhu'" />
          <span>{{ caseDetail?.follow ? t('functionCase.detail.unfollow') : t('functionCase.detail.follow') }}</span>
        </Button>

        <Button
          :disabled="!actionAuth.includes('delete')"
          class="mt-2 mr-2"
          size="small"
          @click="handleClick('delete')">
          <Icon class="mr-1" icon="icon-qingchu" />
          <span>{{ t('functionCase.detail.delete') }}</span>
        </Button>

        <template v-if="!['share'].includes(props.source)">
          <Button
            class="mt-2 mr-2"
            size="small"
            @click="handleCopy">
            <Icon class="mr-1" icon="icon-fuzhi" />
            <span>{{ t('functionCase.detail.copyLink') }}</span>
          </Button>
        </template>
      </div>

      <div v-if="props.tabKey" class="whitespace-nowrap">
        <Button
          size="small"
          class="mt-2 mr-2"
          :disabled="hasBeforeData"
          @click="getData('before')">
          <Icon class="mr-1" icon="icon-chakanshangyitiao" />{{ t('functionCase.detail.viewPrevious') }}
        </Button>

        <Button
          size="small"
          class="mt-2 mr-2"
          :disabled="hasLastData"
          @click="getData('after')">
          <Icon class="mr-1" icon="icon-chakanxiayitiao" />{{ t('functionCase.detail.viewNext') }}
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
          @click="refresh">
          <Icon
            icon="icon-shuaxin"
            class="mr-1" />
          {{ t('functionCase.detail.refresh') }}
        </Button>
      </template>

      <TabPane
        key="info"
        class="h-full"
        :tab="t('functionCase.detail.detail')">
        <CaseDetailTab
          :caseDetail="caseDetail"
          :actionAuth="actionAuth"
          @editSuccess="editSuccess" />
      </TabPane>

      <TabPane
        v-if="!!caseDetail?.review"
        key="reviewRecord"
        :tab="t('functionCase.detail.reviewRecord')">
        <ReviewTab
          ref="reviewRecordRef"
          :caseDetail="caseDetail" />
      </TabPane>

      <TabPane key="assocCase">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('functionCase.detail.case') }}</span>
            <span>({{ caseDetail?.refCaseInfos?.length || 0 }})</span>
          </div>
        </template>

        <AssocCaseTab
          :dataSource="caseDetail?.refCaseInfos"
          :projectId="projectId"
          :caseId="caseDetail?.id"
          @editSuccess="editSuccess" />
      </TabPane>

      <TabPane key="assocTask">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('functionCase.detail.task') }}</span>
            <span>({{ getRefTaskNum(TaskType.TASK) }})</span>
          </div>
        </template>
        <AssocTaskTab
          :key="TaskType.TASK"
          :dataSource="caseDetail?.refTaskInfos"
          :projectId="projectId"
          :caseId="caseDetail?.id"
          :title="t('functionCase.detail.task')"
          :taskType="TaskType.TASK"
          :tips="t('functionCase.detail.taskTip')"
          @editSuccess="editSuccess" />
      </TabPane>

      <TabPane key="assocRequirements">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('functionCase.detail.requirement') }}</span>
            <span>({{ getRefTaskNum(TaskType.REQUIREMENT) }})</span>
          </div>
        </template>
        <AssocTaskTab
          :key="TaskType.REQUIREMENT"
          :projectId="projectId"
          :userInfo="props.userInfo"
          :appInfo="appInfo"
          :dataSource="caseDetail?.refTaskInfos || []"
          :caseId="caseDetail?.id"
          :title="t('functionCase.detail.requirement')"
          :taskType="TaskType.REQUIREMENT"
          :tips="t('functionCase.detail.requirementTip')"
          @editSuccess="editSuccess" />
      </TabPane>

      <TabPane key="assocStory">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('functionCase.detail.story') }}</span>
            <span>({{ getRefTaskNum(TaskType.STORY) }})</span>
          </div>
        </template>
        <AssocTaskTab
          :key="TaskType.STORY"
          :projectId="projectId"
          :userInfo="props.userInfo"
          :appInfo="appInfo"
          :dataSource="caseDetail?.refTaskInfos || []"
          :caseId="caseDetail?.id"
          :title="t('functionCase.detail.story')"
          :taskType="TaskType.STORY"
          :tips="t('functionCase.detail.storyTip')"
          @editSuccess="editSuccess" />
      </TabPane>

      <TabPane key="assocBug">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('functionCase.detail.bug') }}</span>
            <span>({{ getRefTaskNum(TaskType.BUG) }})</span>
          </div>
        </template>
        <AssocTaskTab
          :key="TaskType.BUG"
          :projectId="projectId"
          :userInfo="props.userInfo"
          :appInfo="appInfo"
          :dataSource="caseDetail?.refTaskInfos || []"
          :caseId="caseDetail?.id"
          :title="t('functionCase.detail.bug')"
          :taskType="TaskType.BUG"
          :tips="t('functionCase.detail.bugTip')"
          @editSuccess="editSuccess" />
      </TabPane>

      <TabPane key="assocApiTest">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('functionCase.detail.apiTest') }}</span>
            <span>({{ getRefTaskNum(TaskType.API_TEST) }})</span>
          </div>
        </template>
        <AssocTaskTab
          :key="TaskType.API_TEST"
          :projectId="projectId"
          :userInfo="props.userInfo"
          :appInfo="appInfo"
          :dataSource="caseDetail?.refTaskInfos || []"
          :caseId="caseDetail?.id"
          :title="t('functionCase.detail.apiTest')"
          :taskType="TaskType.API_TEST"
          :tips="t('functionCase.detail.apiTestTip')"
          @editSuccess="editSuccess" />
      </TabPane>

      <TabPane key="assocScenarioTest">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('functionCase.detail.scenarioTest') }}</span>
            <span>({{ getRefTaskNum(TaskType.SCENARIO_TEST) }})</span>
          </div>
        </template>
        <AssocTaskTab
          :key="TaskType.SCENARIO_TEST"
          :projectId="projectId"
          :userInfo="props.userInfo"
          :appInfo="appInfo"
          :dataSource="caseDetail?.refTaskInfos || []"
          :caseId="caseDetail?.id"
          :title="t('functionCase.detail.scenarioTest')"
          :taskType="TaskType.SCENARIO_TEST"
          :tips="t('functionCase.detail.scenarioTestTip')"
          @editSuccess="editSuccess" />
      </TabPane>

      <TabPane
        key="comments"
        class="h-full">
        <template #tab>
          <span>{{ t('functionCase.detail.comments') }} ({{ caseDetail?.commentNum || 0 }})</span>
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
          <span>{{ t('functionCase.detail.activity') }}({{ caseDetail?.activityNum || 0 }})</span>
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
          @change="getActivityList">
          <ActivityInfo :dataSource="activityList" infoKey="description" />
        </Scroll>
      </TabPane>
    </Tabs>

    <AsyncComponent :visible="taskModalVisible">
      <AddTaskModal
        v-model:visible="taskModalVisible"
        :projectId="projectId"
        :appInfo="appInfo"
        :assigneeId="caseDetail.developerId"
        :userInfo="userInfo"
        :refCaseIds="[caseDetail.id]"
        :name="t('functionCase.mainView.testNotPassedName', {name: caseDetail.name})"
        :description="caseDetail.testRemark"
        :taskType="TaskType.BUG"
        :confirmerId="caseDetail?.testerId"
        @ok="handleAddTask" />
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
