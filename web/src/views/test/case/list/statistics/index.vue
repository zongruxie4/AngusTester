<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { utils, SearchCriteria } from '@xcan-angus/infra';
import { analysis } from '@/api/tester';

import { CaseCount } from '@/views/test/case/types';

/**
 * API request parameters interface
 */
interface RequestParams {
  projectId: string;
  filters?: SearchCriteria[];
}

/**
 * Component props interface for Statistics component
 */
type Props = {
  collapse: boolean;
  params: RequestParams;
  projectId: string;
  userInfo: { id: string };
  appInfo: { id: string };
  notify: string;
  moduleId?: string;
}

// Props and Emits Definition
const props = withDefaults(defineProps<Props>(), {
  collapse: false,
  params: undefined,
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  moduleId: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:loading', value: boolean): void;
}>();

/**
 * Lazy-loaded Count component for displaying statistics cards
 */
const Count = defineAsyncComponent(() => import('./Count.vue'));
/**
 * Lazy-loaded Chart component for displaying charts
 */
const Chart = defineAsyncComponent(() => import('./Chart.vue'));

// Constants and State
const componentId = utils.uuid();

/**
 * Default statistics data structure with all values initialized to 0
 */
const defaultStatisticsData: CaseCount = {
  actualWorkload: 0,
  canceledTestNum: 0,
  completedWorkload: 0,
  evalWorkload: 0,
  failedReviewNum: 0,
  notPassedTestNum: 0,
  oneTimePassedTestNum: 0,
  oneTimePassedTestRate: 0,
  oneTimePassReviewNum: 0,
  oneTimePassReviewRate: 0,
  overdueNum: 0,
  passedReviewNum: 0,
  passedTestNum: 0,
  pendingReviewNum: 0,
  pendingTestNum: 0,
  progress: '0',
  totalCaseNum: 0,
  totalReviewCaseNum: 0,
  totalReviewedCaseNum: 0,
  totalReviewTimes: 0,
  totalTestFailTimes: 0,
  totalTestTimes: 0,
  totalTestedCaseNum: 0,
  validCaseNum: 0,
  blockedTestNum: 0
};

const statisticsData = ref<CaseCount>({ ...defaultStatisticsData });

/**
 * Determines the active key for the collapse component based on collapse state
 */
const activeKey = computed(() => {
  if (props.collapse) {
    return '';
  }
  return componentId;
});

/**
 * Loads statistics data from the API
 * <p>
 * Constructs request parameters and calls the analysis API to fetch case statistics.
 * Updates the loading state and statistics data based on the response.
 */
const loadStatisticsData = async (): Promise<void> => {
  emit('update:loading', true);

  let requestParams: RequestParams = {
    projectId: props.projectId
  };

  // Merge additional parameters if provided
  if (props.params) {
    requestParams = { ...requestParams, ...props.params };
  }

  const [error, response] = await analysis.getFuncCaseCount(requestParams);
  emit('update:loading', false);

  if (error) {
    return;
  }

  if (response?.data) {
    statisticsData.value = response.data;
  }
};

// Lifecycle Hooks
onMounted(() => {
  // Watch for changes in filter parameters
  watch(() => props.params, (newValue) => {
    if (!newValue) {
      return;
    }
    loadStatisticsData();
  }, { immediate: false });

  // Watch for notification changes to trigger data refresh
  watch(() => props.notify, (newValue) => {
    if (newValue === undefined || newValue === null) {
      return;
    }
    loadStatisticsData();
  }, { immediate: true });
});
</script>
<template>
  <!-- Statistics panel with collapsible functionality -->
  <Collapse
    v-model:activeKey="activeKey"
    ghost
    collapsible="disabled">
    <CollapsePanel :key="componentId" :showArrow="false">
      <!-- Statistics cards section -->
      <Count :dataSource="statisticsData" class="statistics-cards" />
      <!-- ECharts section -->
      <Chart :dataSource="statisticsData" />
    </CollapsePanel>
  </Collapse>
</template>
<style scoped>
.ant-collapse :deep(.ant-collapse-item) .ant-collapse-header {
  display: none;
}

.ant-collapse :deep(.ant-collapse-content) .ant-collapse-content-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0;
}
</style>
