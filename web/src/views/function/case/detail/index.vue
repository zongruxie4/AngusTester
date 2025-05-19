<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { ActivityInfo, AsyncComponent, Icon, notification, Scroll, SmartComment } from '@xcan-angus/vue-ui';
import { Button, Popover, TabPane, Tabs } from 'ant-design-vue';
import Dexie, { TESTER, duration, clipboard } from '@xcan-angus/tools';
import elementResizeDetector, { Erd } from 'element-resize-detector';
import { debounce } from 'throttle-debounce';

import { CASE_PROJECT_PERMISSIONS, CaseActionAuth, useCaseActionAuth } from './PropsType';
import type { CaseInfoObj, CaseListObj } from '../PropsType';
import { funcCase, funcPlan } from '@/api/tester';

export type TabKey = 'info' | 'activty' | 'comments' | 'asscoTask' | 'asscoCase'
export interface Attachments { id?: string, name: string, url: string }

const CaseDetailTab = defineAsyncComponent(() => import('@/views/function/case/detail/case/index.vue'));
const AddTaskModal = defineAsyncComponent(() => import('@/views/task/task/list/task/edit/index.vue'));
const ReviewTab = defineAsyncComponent(() => import('@/views/function/case/detail/review/index.vue'));
const AssocTaskTab = defineAsyncComponent(() => import('@/views/function/case/detail/assocTask/index.vue'));
const AssocCaseTab = defineAsyncComponent(() => import('@/views/function/case/detail/assocCase/index.vue'));

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
  caseInfo?: CaseInfoObj,
  caseAcitonAuth?: {[key:string]:CaseActionAuth[]},
  queryParams?: any
  scrollNotify?: number;
  notify?: number;
}

const props = withDefaults(defineProps<Props>(), {
  source: undefined,
  caseId: '',
  tabKey: '',
  currIndex: 0,
  userInfo: undefined,
  caseInfo: undefined,
  caseAcitonAuth: undefined,
  queryParams: undefined,
  scrollNotify: 0,
  notify: 0
});

const emits = defineEmits<{(e: 'onClick', type: CaseActionAuth, value: CaseListObj): void;
  (e: 'editSuccess', id:string): void;
  (e: 'getInfo', id:string): void;
  (e: 'updateCaseTab', value): void;
  (e: 'update:loading', value: boolean): void;
}>();

const isAdmin = inject('isAdmin', ref(false));
const projectInfo = inject('projectInfo', ref({ id: '' }));
const userInfo = inject('userInfo');
const appInfo = inject('appInfo');
const projectId = computed(() => {
  return projectInfo.value?.id;
});

const { getActionAuth } = useCaseActionAuth();

const activeKey = ref<TabKey>('info');
const detailRef = ref<HTMLElement | null>(null);

const caseAuth = ref<{ [key: string]: CaseActionAuth[] }>({});
const caseDetail = ref();

const pageNo = ref(1);
const total = ref(1);
const filters = ref([]);

const hasLastData = ref(false);
const hasBeforeData = ref(false);
let isFirstClick = true;

let db: Dexie<IData>;
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
      notification.success('已经是第一条数据啦！');
      hasBeforeData.value = true;
      return;
    } else {
      hasLastData.value = false;
    }
  }

  if (value === 'after') {
    if (pageNo.value >= total.value) {
      notification.success('已经是最后一条数据啦！');
      hasLastData.value = true;
      return;
    } else {
      hasBeforeData.value = false;
    }
  }

  value === 'before' ? pageNo.value-- : pageNo.value++;

  const params = { pageNo: pageNo.value, pageSize: 1, enabledGroup: false, filters: filters.value, projectId: projectInfo.value?.id };
  const [listError, listRes] = await funcCase.loadFuncCase({ infoScope: 'DETAIL', ...params });
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
  // 计算当前页的起始位置
  const startIndex = (_pageNo - 1) * _pageSize;

  // 计算n在当前页的位置
  const positionInPage = startIndex + n;
  // 返回当前数据在所有数据集里的位置
  return positionInPage;
};

// eslint-disable-next-line @typescript-eslint/no-empty-function
const updateTabPane = inject<(data: any) => void>('updateTabPane', () => { });

const getCaseInfo = async (id: string) => {
  destroyInactiveTabPane.value = true;
  emits('update:loading', true);
  const [error, { data }] = await funcCase.getCaseInfo(id);
  emits('update:loading', false);
  if (error) {
    return;
  }
  caseDetail.value = data;

  updateTabPane({ _id: props.tabKey, caseId: data.id, name: data.name, value: 'caseInfo' });
  await getPlanAuth(data.planId);
  caseAuth.value[data.id] = getActionAuth(planAuthList.value);
  if (caseAuth.value[data.id].includes('resetTestResult')) {
    if (!funcPlanAuthFlag.value) {
      caseAuth.value[data.id] = caseAuth.value[data.id].filter(i => i !== 'resetTestResult');
    }
  }
  if (caseAuth.value[data.id].includes('retestResult')) {
    if (!funcPlanAuthFlag.value && userInfo?.id !== data.testerId) {
      caseAuth.value[data.id] = caseAuth.value[data.id].filter(i => i !== 'retestResult');
    }
  }
};

const planAuthList = ref<string[]>([]);
const funcPlanAuthFlag = ref(true);
const getPlanAuth = async (planId) => {
  if (isAdmin.value) {
    funcPlanAuthFlag.value = true;
    planAuthList.value = CASE_PROJECT_PERMISSIONS;
    return;
  }
  const [error, { data }] = await funcPlan.getCurrentAuthByPlanId(planId);
  if (error) {
    return;
  }
  if (data.funcPlanAuthFlag) {
    funcPlanAuthFlag.value = true;
    planAuthList.value = data.permissions.map(item => {
      return item.value;
    });
  } else {
    funcPlanAuthFlag.value = false;
    planAuthList.value = CASE_PROJECT_PERMISSIONS;
  }
  if (props.userInfo?.id === caseDetail.value.testerId && !planAuthList.value.includes('TEST')) {
    planAuthList.value.push('TEST');
  }
};
const destroyInactiveTabPane = ref(true);
watch(() => props.caseInfo, (newValue) => {
  destroyInactiveTabPane.value = true;
  if (newValue) {
    caseDetail.value = newValue;
    caseAuth.value = props.caseAcitonAuth || {};
  }

  if (!newValue && props.tabKey) {
    getCaseInfo(props.caseId);
  }
  // activeKey.value = 'info';
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

const actionAuth = computed(() => {
  return caseAuth.value[caseDetail.value?.id] || [];
});

const handleClick = (type: CaseActionAuth) => {
  emits('onClick', type, caseDetail.value as CaseListObj);
};

// 复制Url
const handleCopy = async () => {
  const message = `${window.location.origin}/function#cases?&id=${caseDetail.value.id}&projectId=${projectInfo.value.id}&name=${caseDetail.value.name}&currIndex=${props.currIndex}&total=${props.queryParams.total}&pageNo=${props.queryParams.pageNo}&pageSize=${props.queryParams.pageSize}`;
  clipboard.toClipboard(message).then(() => {
    notification.success('复制成功');
  }).catch(() => {
    notification.error('复制失败');
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

watch(() => props.notify, () => {
  getCaseInfo(props.caseId);
});

watch(() => activeKey.value, () => {
  destroyInactiveTabPane.value = false;
}, {
  immediate: true
});

const buttonGroupRef = ref<HTMLElement>();
const buttonHeight = ref(0);
let buttonErd:Erd;

const buttonResizeHandler = debounce(duration.resize, (element) => {
  buttonHeight.value = element.offsetHeight;
});

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

const activityLoading = ref(false);
const activityNotify = ref(0);
const activityParams = ref({ filters: [{ key: 'targetType', value: 'FUNC_CASE', op: 'EQUAL' }] });

const refresh = () => {
  if (['info', 'asscoTask', 'asscoCase'].includes(activeKey.value)) {
    getCaseInfo(caseDetail.value.id);
  }

  if (activeKey.value === 'activty') {
    activityNotify.value++;
  }

  if (activeKey.value === 'comments' && smartCommentRef.value) {
    smartCommentRef.value.refresh();
  }

  if (reviewRecordRef.value) {
    reviewRecordRef.value.refresh();
  }
};

const getRefTaskNum = (type = 'TASK') => {
  return (caseDetail.value?.refTaskInfos || []).filter(item => item.taskType.value === type).length || 0;
};

const activityList = ref([]);
const getActivityList = (data) => {
  activityList.value = data;
};

defineExpose({
  activeKey
});
</script>
<template>
  <div class="h-full pl-3 pb-3 pt-1">
    <div class="flex justify-between">
      <div ref="buttonGroupRef" class="flex items-center button-combination flex-wrap">
        <template v-if="!caseDetail?.review || (caseDetail?.review && caseDetail?.reviewStatus.value === 'PASSED')">
          <template v-if="!['PASSED', 'NOT_PASSED', 'CANCELED'].includes(caseDetail?.testResult?.value)">
            <Button
              :disabled="!actionAuth.includes('updateTestResult')"
              type="primary"
              class="mt-2 mr-2"
              size="small"
              @click="handleClick('updateTestResult_passed')">
              <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
              <span>测试通过</span>
            </Button>
            <Button
              :disabled="!actionAuth.includes('updateTestResult')"
              type="primary"
              class="mt-2 mr-2"
              size="small"
              @click="handleClick('updateTestResult_notpassed')">
              <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
              <span>测试未通过</span>
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
              <span>重新测试</span>
            </Button>
          </template>
          <Button
            v-if="+(caseDetail?.testNum || 0) > 0"
            :disabled="!actionAuth.includes('resetTestResult')"
            class="mt-2 mr-2"
            size="small"
            @click="handleClick('resetTestResult')">
            <Icon class="mr-1" icon="icon-zhongzhiceshijieguo" />
            <span>重置测试结果</span>
            <Popover placement="bottom">
              <template #content>
                <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                  将用例更新为`待测试`，相关统计计数和状态会被清除。
                </div>
              </template>
              <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
            </Popover>
          </Button>
        </template>
        <Button
          v-if="(!caseDetail?.review || (caseDetail?.review && caseDetail?.reviewStatus.value === 'PASSED')) && !['PASSED', 'NOT_PASSED','CANCELED'].includes(caseDetail?.testResult?.value)"
          :disabled="!actionAuth.includes('updateTestResult')"
          class="mt-2 mr-2"
          size="small"
          @click="handleClick('updateTestResult_blocked')">
          <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
          设为阻塞中
        </Button>
        <Button
          v-if="!caseDetail?.review || (caseDetail?.review && caseDetail?.reviewStatus.value === 'PASSED') && !['PASSED', 'NOT_PASSED','CANCELED'].includes(caseDetail?.testResult?.value)"
          :disabled="!actionAuth.includes('updateTestResult')"
          class="mt-2 mr-2"
          size="small"
          @click="handleClick('updateTestResult_canceled')">
          <Icon class="mr-1" icon="icon-xiugaiceshijieguo" />
          取消
        </Button>
        <Button
          v-if="caseDetail?.testResult?.value === 'NOT_PASSED'"
          size="small"
          class="mt-2 mr-2"
          :disabled="!actionAuth.includes('edit')"
          @click="addBug">
          <Icon class="mr-1" icon="icon-bianji" />
          <span>提BUG</span>
        </Button>
        <Button
          size="small"
          class="mt-2 mr-2"
          :disabled="!actionAuth.includes('edit')"
          @click="handleClick('edit')">
          <Icon class="mr-1" icon="icon-bianji" />
          <span>编辑</span>
        </Button>
        <Button
          :disabled="!actionAuth.includes('clone')"
          class="mt-2 mr-2"
          size="small"
          @click="handleClick('clone')">
          <Icon class="mr-1" icon="icon-fuzhi" />
          <span>克隆</span>
        </Button>
        <Button
          :disabled="!actionAuth.includes('move')"
          class="mt-2 mr-2"
          size="small"
          @click="handleClick('move')">
          <Icon class="mr-1 text-3" icon="icon-yidong" />
          <span>移动</span>
        </Button>
        <Button
          class="mt-2 mr-2"
          size="small"
          @click="handleClick('favourite')">
          <Icon class="mr-1 text-3" :icon="caseDetail?.favouriteFlag?'icon-quxiaoshoucang':'icon-yishoucang'" />
          <span>{{ caseDetail?.favouriteFlag?'取消收藏':'收藏' }}</span>
        </Button>
        <Button
          class="mt-2 mr-2"
          size="small"
          @click="handleClick('follow')">
          <Icon class="mr-1 text-3" :icon="caseDetail?.followFlag?'icon-quxiaoguanzhu':'icon-yiguanzhu'" />
          <span>{{ caseDetail?.followFlag?'取消关注':'关注' }}</span>
        </Button>
        <Button
          :disabled="!actionAuth.includes('delete')"
          class="mt-2 mr-2"
          size="small"
          @click="handleClick('delete')">
          <Icon class="mr-1" icon="icon-qingchu" />
          <span>删除</span>
        </Button>
        <template v-if="!['share'].includes(props.source)">
          <Button
            class="mt-2 mr-2"
            size="small"
            @click="handleCopy">
            <Icon class="mr-1" icon="icon-fuzhi" />
            <span>复制链接</span>
          </Button>
        </template>
      </div>
      <div v-if="props.tabKey" class="whitespace-nowrap">
        <Button
          size="small"
          class="mt-2 mr-2"
          :disabled="hasBeforeData"
          @click="getData('before')">
          <Icon class="mr-1" icon="icon-chakanshangyitiao" />查看上一条
        </Button>
        <Button
          size="small"
          class="mt-2 mr-2"
          :disabled="hasLastData"
          @click="getData('after')">
          <Icon class="mr-1" icon="icon-chakanxiayitiao" />查看下一条
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
          刷新
        </Button>
      </template>
      <TabPane
        key="info"
        class="h-full"
        tab="详情">
        <CaseDetailTab
          :caseDetail="caseDetail"
          :actionAuth="actionAuth"
          @editSuccess="editSuccess" />
      </TabPane>
      <TabPane
        v-if="!!caseDetail?.review"
        key="reviewRecord"
        tab="评审记录">
        <ReviewTab
          ref="reviewRecordRef"
          :caseDetail="caseDetail" />
      </TabPane>

      <TabPane key="asscoCase">
        <template #tab>
          <div class="inline-flex">
            <span>用例</span>
            <span>({{ caseDetail?.refCaseInfos?.length || 0 }})</span>
          </div>
        </template>
        <AssocCaseTab
          :dataSource="caseDetail?.refCaseInfos"
          :projectId="projectId"
          :caseId="caseDetail?.id"
          @editSuccess="editSuccess" />
      </TabPane>
      <TabPane key="asscoTask">
        <template #tab>
          <div class="inline-flex">
            <span>任务</span>
            <span>({{ getRefTaskNum('TASK') }})</span>
          </div>
        </template>
        <AssocTaskTab
          key="TASK"
          :dataSource="caseDetail?.refTaskInfos"
          :projectId="projectId"
          :caseId="caseDetail?.id"
          title="任务"
          taskType="TASK"
          tips="记录与当前用例相关联的开发或测试任务，帮助团队追踪测试任务。"
          @editSuccess="editSuccess" />
      </TabPane>

      <TabPane key="asscoRequirements">
        <template #tab>
          <div class="inline-flex">
            <span>需求</span>
            <span>({{ getRefTaskNum('REQUIREMENT') }})</span>
          </div>
        </template>
        <AssocTaskTab
          key="REQUIREMENT"
          :projectId="projectId"
          :userInfo="props.userInfo"
          :appInfo="appInfo"
          :dataSource="caseDetail?.refTaskInfos || []"
          :caseId="caseDetail?.id"
          title="需求"
          taskType="REQUIREMENT"
          tips="追溯问题到原始需求，方便理解问题产生的背景和影响范围。"
          @editSuccess="editSuccess" />
      </TabPane>
      <TabPane key="asscoStory">
        <template #tab>
          <div class="inline-flex">
            <span>故事</span>
            <span>({{ getRefTaskNum('STORY') }})</span>
          </div>
        </template>
        <AssocTaskTab
          key="STORY"
          :projectId="projectId"
          :userInfo="props.userInfo"
          :appInfo="appInfo"
          :dataSource="caseDetail?.refTaskInfos || []"
          :caseId="caseDetail?.id"
          title="故事"
          taskType="STORY"
          tips="追溯用户角度描述功能需求，方便理解问题产生的背景和影响范围。"
          @editSuccess="editSuccess" />
      </TabPane>
      <TabPane key="asscoBug">
        <template #tab>
          <div class="inline-flex">
            <span>缺陷</span>
            <span>({{ getRefTaskNum('BUG') }})</span>
          </div>
        </template>
        <AssocTaskTab
          key="BUG"
          :projectId="projectId"
          :userInfo="props.userInfo"
          :appInfo="appInfo"
          :dataSource="caseDetail?.refTaskInfos || []"
          :caseId="caseDetail?.id"
          title="缺陷"
          taskType="BUG"
          tips="追踪软件功能中的错误，确保及时修复，帮助识别和解决系统性问题。"
          @editSuccess="editSuccess" />
      </TabPane>
      <TabPane key="asscoApiTest">
        <template #tab>
          <div class="inline-flex">
            <span>接口测试</span>
            <span>({{ getRefTaskNum('API_TEST') }})</span>
          </div>
        </template>
        <AssocTaskTab
          key="API_TEST"
          :projectId="projectId"
          :userInfo="props.userInfo"
          :appInfo="appInfo"
          :dataSource="caseDetail?.refTaskInfos || []"
          :caseId="caseDetail?.id"
          title="接口测试"
          taskType="API_TEST"
          tips="建立与对应接口测试任务关联关系，方便追溯接口测试进展，确保接口正常工作。"
          @editSuccess="editSuccess" />
      </TabPane>
      <TabPane key="asscoScenTest">
        <template #tab>
          <div class="inline-flex">
            <span>场景测试</span>
            <span>({{ getRefTaskNum('SCENARIO_TEST') }})</span>
          </div>
        </template>
        <AssocTaskTab
          key="SCENARIO_TEST"
          :projectId="projectId"
          :userInfo="props.userInfo"
          :appInfo="appInfo"
          :dataSource="caseDetail?.refTaskInfos || []"
          :caseId="caseDetail?.id"
          title="场景测试"
          taskType="SCENARIO_TEST"
          tips="建立与对应场景测试任务关联关系，方便了解真实场景中功能的有效性，确保解决方案的完整性。"
          @editSuccess="editSuccess" />
      </TabPane>
      <TabPane
        key="comments"
        class="h-full">
        <template #tab>
          <span>评论 ({{ caseDetail?.commentNum || 0 }})</span>
        </template>
        <SmartComment
          ref="smartCommentRef"
          class="!shadow-none h-full overflow-y-auto pr-3.5"
          targetType="FUNC_CASE"
          avatar
          :showPublishTitle="false"
          :bordered="false"
          :public0="false"
          :userId="props.userInfo?.id"
          :targetId="caseDetail?.id"
          :action="`${TESTER}/comment`" />
      </TabPane>

      <TabPane
        key="activty"
        class="h-full">
        <template #tab>
          <span>活动({{ caseDetail?.activityNum || 0 }})</span>
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
        :name="`“${caseDetail.name}”测试不通过`"
        :description="caseDetail.testRemark"
        taskType="BUG"
        :confirmorId="caseDetail?.testerId"
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
