<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { Icon, Image, notification, Spin, Table } from '@xcan-angus/vue-ui';
import { appContext, download, TESTER, toClipboard, enumUtils } from '@xcan-angus/infra';
import { funcPlan } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { FuncPlanPermission } from '@/enums/enums';
import { PlanDetail } from '../types';
import { BasicProps } from '@/types/types';

const { t } = useI18n();

// Props
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

// Async components
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));
const BurnDownChart = defineAsyncComponent(() => import('@/views/test/plan/detail/BurndownChart.vue'));
const MemberProgress = defineAsyncComponent(() => import('@/views/test/plan/detail/MemberProgress.vue'));
const WorkCalendar = defineAsyncComponent(() => import('@/views/test/home/WorkCalendar.vue'));
const BasicInfo = defineAsyncComponent(() => import('./BasicInfo.vue'));

// Injected dependencies
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const setCaseListPlanParam = inject<(value: any) => void>('setCaseListPlanParam');

// Computed properties
const isAdmin = computed(() => appContext.isAdmin());
const currentPlanId = computed(() => {
  return planDetailData.value?.id;
});

// Reactive data
const loading = ref(false);
const exportLoading = ref(false);
const userPermissions = ref<string[]>([]);
const planDetailData = ref<PlanDetail>();
const testerResponsibilities = ref<{id:string;name:string;content:string;}[]>([]);
const completedRate = ref(0);

/**
 * Navigates to the case list view with the current plan parameters.
 * <p>
 * Passes plan details including ID and name to the case list component.
 * </p>
 */
const navigateToCaseList = () => {
  setCaseListPlanParam?.({
    ...planDetailData.value,
    planId: planDetailData.value?.id,
    planName: planDetailData.value?.name
  });
};

/**
 * Loads user permissions for the specified plan ID.
 * <p>
 * For admin users, loads all available permissions. For regular users,
 * fetches specific permissions from the API.
 * </p>
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
  const [error, res] = await funcPlan.getCurrentAuthByPlanId(planId, params);
  loading.value = false;
  if (error) {
    return;
  }
  userPermissions.value = (res?.data?.permissions || []).map(item => item.value);
};

/**
 * Loads detailed plan information from the API.
 * <p>
 * Processes the plan data, calculates completion rate, builds tester responsibilities
 * list, and updates the tab pane title.
 * </p>
 */
const loadPlanDetailData = async (planId: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await funcPlan.getPlanDetail(planId);
  loading.value = false;
  if (error) {
    return;
  }

  const planData = res?.data as PlanDetail;
  if (!planData) {
    return;
  }

  planDetailData.value = planData;
  if (planDetailData.value.progress?.completedRate) {
    completedRate.value = +String(planDetailData.value.progress.completedRate).replace(/(\d+\.\d{2})\d+/, '$1');
  }

  buildTesterResponsibilitiesList(planData);
  updateTabPaneTitle(planData.name, planId);
};

/**
 * Builds the tester responsibilities list from plan members data.
 * <p>
 * Maps member information with their assigned responsibilities.
 * </p>
 */
const buildTesterResponsibilitiesList = (planData: PlanDetail) => {
  testerResponsibilities.value = [];
  const members = planData.members || [];
  for (let i = 0, len = members.length; i < len; i++) {
    const { id, fullName } = members[i];
    testerResponsibilities.value.push({
      name: fullName,
      ...members[i],
      content: planDetailData.value?.testerResponsibilities?.[id]
    });
  }
};

/**
 * Updates the tab pane title with the plan name.
 * <p>
 * Generates a unique tab ID for case-related navigation.
 * </p>
 */
const updateTabPaneTitle = (planName: string, planId: string) => {
  if (planName && typeof updateTabPane === 'function') {
    updateTabPane({ name: planName, _id: planId + '-case' });
  }
};

/**
 * Exports test cases for the current plan.
 * <p>
 * Downloads a file containing all test cases associated with the plan.
 * </p>
 */
const exportTestCases = async () => {
  if (!planDetailData.value) {
    return;
  }

  const { id, projectId } = planDetailData.value;
  if (exportLoading.value) {
    return;
  }

  exportLoading.value = true;
  await download(`${TESTER}/func/case/export?projectId=${projectId}&planId=${id}`);
  exportLoading.value = false;
};

/**
 * Copies the current plan URL to the clipboard.
 * <p>
 * Shows success or error notifications based on the operation result.
 * </p>
 */
const copyPlanUrl = () => {
  const planUrl = window.location.origin + '/test#plans?id=' + currentPlanId.value;
  toClipboard(planUrl).then(() => {
    notification.success(t('actions.tips.copySuccess'));
  }).catch(() => {
    notification.error(t('actions.tips.copyFailed'));
  });
};

/**
 * Refreshes the plan detail data and permissions.
 * <p>
 * Reloads both user permissions and plan detail information.
 * </p>
 */
const refreshPlanData = async () => {
  const planId = currentPlanId.value;
  if (!planId) {
    return;
  }

  await loadUserPermissions(planId);
  await loadPlanDetailData(planId);
};

// Table configurations
const testerTableColumns = [
  {
    key: 'name',
    dataIndex: 'name',
    title: t('common.name'),
    width: '25%',
    ellipsis: true
  },
  {
    key: 'content',
    dataIndex: 'content',
    title: t('testPlan.planDetail.table.workResponsibilities'),
    ellipsis: true
  }
];

// Lifecycle hooks
onMounted(() => {
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
    await loadPlanDetailData(planId);
  }, { immediate: true });
});
</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="flex items-start flex-wrap space-y-b-2 space-x-2.5 mb-3.5">
      <Button
        type="default"
        size="small"
        class="p-0">
        <RouterLink
          class="flex items-center space-x-1 leading-6.5 px-1.75"
          :to="`/test#plans?id=${currentPlanId}&type=edit`">
          <Icon icon="icon-shuxie" class="text-3.5" />
          <span>{{ t('actions.edit') }}</span>
        </RouterLink>
      </Button>

      <Button
        :disabled="!isAdmin && userPermissions.length === 0"
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="navigateToCaseList">
        <Icon icon="icon-ceshiyongli1" class="text-3.5" />
        <span>{{ t('testPlan.planDetail.buttons.viewCases') }}</span>
      </Button>

      <Button
        :disabled="!isAdmin && !userPermissions.includes(FuncPlanPermission.EXPORT_CASE)"
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="exportTestCases">
        <Icon icon="icon-daochu" class="text-3.5" />
        <span>{{ t('testPlan.planDetail.buttons.exportCases') }}</span>
      </Button>

      <Button
        size="small"
        class="flex items-center"
        @click="copyPlanUrl">
        <Icon class="mr-1 flex-shrink-0" icon="icon-fuzhi" />
        <span>{{ t('actions.copyLink') }}</span>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center"
        @click="refreshPlanData">
        <Icon class="mr-1 flex-shrink-0" icon="icon-shuaxin" />
        <span>{{ t('actions.refresh') }}</span>
      </Button>
    </div>

    <BasicInfo :planData="planDetailData" :completionRate="completedRate" />

    <Tabs size="small">
      <TabPane key="testerResponsibilities" :tab="t('common.tester')">
        <Table
          :columns="testerTableColumns"
          :dataSource="testerResponsibilities"
          :pagination="false"
          :noDataSize="'small'"
          :noDataText="t('common.noData')">
          <template #bodyCell="{record, column}">
            <template v-if="column.dataIndex === 'name'">
              <div class="flex items-center">
                <Image
                  class="w-5 h-5 mr-1"
                  type="avatar"
                  :src="record.avatar" />
                {{ record.name }}
              </div>
            </template>
          </template>
        </Table>
      </TabPane>
      <TabPane key="testingObjectives" :tab="t('testPlan.columns.testingObjectives')">
        <div class="space-y-1 whitespace-pre-wrap break-words break-all">
          <RichEditor
            v-if="planDetailData?.otherInformation"
            :value="planDetailData.testingObjectives"
            mode="view" />
        </div>
      </TabPane>
      <TabPane key="testingScope" :tab="t('testPlan.columns.testingScope')">
        <div class="space-y-1 whitespace-pre-wrap break-words break-all">
          <RichEditor
            v-if="planDetailData?.otherInformation"
            :value="planDetailData.testingScope"
            mode="view" />
        </div>
      </TabPane>
      <TabPane key="acceptanceCriteria" :tab="t('testPlan.columns.acceptanceCriteria')">
        <div class="space-y-1 whitespace-pre-wrap break-words break-all">
          <RichEditor
            v-if="planDetailData?.acceptanceCriteria"
            :value="planDetailData.acceptanceCriteria"
            mode="view" />
        </div>
      </TabPane>
      <TabPane key="otherInformation" :tab="t('testPlan.columns.otherInformation')">
        <div class="space-y-1 whitespace-pre-wrap break-words break-all">
          <RichEditor
            v-if="planDetailData?.otherInformation"
            :value="planDetailData.otherInformation"
            mode="view" />
        </div>
      </TabPane>
      <TabPane key="chart" :tab="t('chart.burndown.title')">
        <BurnDownChart :planId="currentPlanId" />
      </TabPane>
      <TabPane key="progress" :tab="t('common.memberProgress')">
        <MemberProgress :planId="currentPlanId" :projectId="props.projectId" />
      </TabPane>
      <TabPane key="workCalendar" :tab="t('common.workCalendar')">
        <WorkCalendar
          :projectId="props.projectId"
          :userInfo="props.userInfo as any"
          :planId="currentPlanId" />
      </TabPane>
    </Tabs>
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
