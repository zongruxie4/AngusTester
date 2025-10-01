<script lang="ts" setup>
import { computed, defineAsyncComponent, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { TESTER, download } from '@xcan-angus/infra';
import { Icon } from '@xcan-angus/vue-ui';
import { Button, Table, Tag } from 'ant-design-vue';
import { TemplateIconConfig } from '@/views/test/analysis/list/types';
import { debounce } from 'throttle-debounce';
import { analysis } from '@/api/tester';
import { BasicProps } from '@/types/types';
import { AnalysisCaseTemplate, AnalysisDataSource } from '@/enums/enums';
import { AnalysisInfo } from '../types';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

// Component Props & Configuration
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: () => ({ id: '', fullName: '' }),
  data: undefined,
  onShow: false
});

// Chart Component References
const chartRef = ref();

// Lazy load chart components for better performance
const ProgressChart = defineAsyncComponent(() => import('./progress/index.vue'));
const BurndownChart = defineAsyncComponent(() => import('./burndown/index.vue'));
const WorkloadChart = defineAsyncComponent(() => import('./workload/index.vue'));
const OverdueAssessmentChart = defineAsyncComponent(() => import('./overdue/index.vue'));
const BugsChart = defineAsyncComponent(() => import('./bugs/index.vue'));
const TestingEfficiencyChart = defineAsyncComponent(() => import('./efficiency/index.vue'));
const ReviewEfficiencyChart = defineAsyncComponent(() => import('./reviewEfficiency/index.vue'));
const CoreKpiChart = defineAsyncComponent(() => import('./coreKpi/index.vue'));
const BacklogChart = defineAsyncComponent(() => import('./backlog/index.vue'));
const RecentDeliveryChart = defineAsyncComponent(() => import('./recentDelivery/index.vue'));
const LeadTimeChart = defineAsyncComponent(() => import('./leadTime/index.vue'));
const UnplannedChart = defineAsyncComponent(() => import('./unplanned/index.vue'));
const GrowthTrendChart = defineAsyncComponent(() => import('./growthTread/index.vue'));
const ResourceCreationChart = defineAsyncComponent(() => import('./resourceCreation/index.vue'));

// State Management
const analysisData = ref<AnalysisInfo>({} as AnalysisInfo);
const isExporting = ref(false);
let hasWindowResized = false;

/**
 * Loads analysis detail data from API and updates chart display
 * @param analysisId - The ID of the analysis to load
 */
const loadAnalysisDetail = async (analysisId: string) => {
  const [error, { data }] = await analysis.getAnalysisDetail(analysisId);
  if (error) {
    return;
  }

  analysisData.value = data;
  // Trigger chart resize after data update
  nextTick(() => {
    chartRef.value?.resize();
  });
};

/**
 * Generates table columns configuration from analysis data headers
 */
const tableColumns = computed(() => {
  const headers = analysisData.value.data?.dataDetailTitles;
  if (!headers?.length) return [];

  return headers.map((headerName, index) => ({
    title: headerName,
    dataIndex: index,
    fixed: index === 0 ? 'left' : false,
    ellipsis: index === 0,
    width: 80
  }));
});

/**
 * Transforms raw table data into table-compatible format
 */
const mainTableData = computed(() => {
  const rawData = analysisData.value.data?.tableData;
  if (!rawData?.length) return [];

  // Skip header row and transform data
  const dataRows = rawData.slice(1);
  return dataRows.map((row) => {
    const rowData = {};
    row.forEach((value, index) => {
      rowData[index] = value;
    });
    return rowData;
  });
});

/**
 * Transforms today's delivery data for table display
 */
const todayDeliveryTableData = computed(() => {
  return transformMultiTableData(analysisData.value.data?.multiTableData?.today);
});

/**
 * Transforms last week's delivery data for table display
 */
const last7DaysDeliveryTableData = computed(() => {
  return transformMultiTableData(analysisData.value.data?.multiTableData?.last7Days);
});

/**
 * Transforms last month's delivery data for table display
 */
const lastMonthDeliveryTableData = computed(() => {
  return transformMultiTableData(analysisData.value.data?.multiTableData?.lastMonth);
});

/**
 * Helper function to transform multi-table data into table format
 * @param rawData - Raw data array from API
 * @returns Transformed data array for table display
 */
const transformMultiTableData = (rawData: any[]) => {
  if (!rawData?.length) return [];

  const dataRows = rawData.slice(1);
  return dataRows.map((row) => {
    const rowData = {};
    row.forEach((value, index) => {
      rowData[index] = value;
    });
    return rowData;
  });
};

/**
 * Exports analysis detail data to file
 */
const handleExportDetail = async () => {
  isExporting.value = true;
  try {
    await download(`${TESTER}/analysis/${props.data?.id}/overview/export`);
  } finally {
    isExporting.value = false;
  }
};

/**
 * Debounced resize handler for chart components
 * Only resizes when component is visible to prevent unnecessary operations
 */
const handleWindowResize = debounce(300, () => {
  if (!props.onShow) {
    hasWindowResized = true;
    return;
  }

  hasWindowResized = false;
  chartRef.value?.resize();
});

// Lifecycle Hooks
onMounted(() => {
  // Load analysis data if ID is provided
  if (props.data?.id) {
    loadAnalysisDetail(props.data.id);
  }

  // Watch for component visibility changes
  watch(() => props.onShow, (isVisible) => {
    if (isVisible && hasWindowResized) {
      handleWindowResize();
    }
  });

  // Add window resize listener
  window.addEventListener('resize', handleWindowResize);
});

onBeforeUnmount(() => {
  // Clean up window resize listener
  window.removeEventListener('resize', handleWindowResize);
});
</script>

<template>
  <div class="p-5 h-full overflow-auto">
    <!-- Analysis Header Section -->
    <div class="bg-gray-1 p-2 flex items-center space-x-2">
      <Icon :icon="TemplateIconConfig[analysisData.template]" class="text-20 pl-4 mr-4" />
      <div class="flex-1 min-w-0 flex flex-col justify-around">
        <div>
          <span class="text-4 font-semibold">
            {{ analysisData.name }}
          </span>
          <Tag
            color="geekblue"
            class="ml-5">
            {{ analysisData.datasource?.value === AnalysisDataSource.SNAPSHOT_DATA ? t('functionAnalysis.detail.snapshot') : t('functionAnalysis.detail.realTime') }}
          </Tag>
        </div>
        <div class="mt-2 text-3.5">{{ analysisData.description }}</div>
        <div class="mt-2">
          <span class="text-3.5 font-semibold">
            {{ analysisData.lastModifiedByName }}
          </span>
          <span class="text-3.5 ml-2">
            {{ t('functionAnalysis.detail.lastModifiedAt') }}&nbsp;{{ analysisData.lastModifiedDate }}
          </span>
        </div>
      </div>
    </div>

    <!-- Chart Display Section -->
    <div class="mt-4">
      <div class="detail-title font-semibold pl-2 relative text-3.5 mb-3">
        {{ t('functionAnalysis.detail.charts') }}
      </div>

      <!-- Dynamic Chart Components Based on Template Type -->
      <div v-if="analysisData.template === AnalysisCaseTemplate.PROGRESS" class="w-200">
        <ProgressChart ref="chartRef" :analysisInfo="analysisData" />
      </div>
      <div v-if="analysisData.template === AnalysisCaseTemplate.BURNDOWN" class="w-275">
        <BurndownChart ref="chartRef" :analysisInfo="analysisData" />
      </div>
      <div v-if="analysisData.template === AnalysisCaseTemplate.WORKLOAD" class="max-w-300">
        <WorkloadChart ref="chartRef" :analysisInfo="analysisData" />
      </div>
      <div v-if="analysisData.template === AnalysisCaseTemplate.OVERDUE_ASSESSMENT" class="max-w-275">
        <OverdueAssessmentChart ref="chartRef" :analysisInfo="analysisData" />
      </div>
      <div v-if="analysisData.template === AnalysisCaseTemplate.SUBMITTED_BUGS" class="max-w-400">
        <BugsChart ref="chartRef" :analysisInfo="analysisData" />
      </div>
      <div v-if="analysisData.template === AnalysisCaseTemplate.TESTING_EFFICIENCY" class="max-w-300">
        <TestingEfficiencyChart ref="chartRef" :analysisInfo="analysisData" />
      </div>
      <div v-if="analysisData.template === AnalysisCaseTemplate.REVIEW_EFFICIENCY" class="max-w-300">
        <ReviewEfficiencyChart ref="chartRef" :analysisInfo="analysisData" />
      </div>
      <div v-if="analysisData.template === AnalysisCaseTemplate.CORE_KPI" class="max-w-400">
        <CoreKpiChart ref="chartRef" :analysisInfo="analysisData" />
      </div>
      <div v-if="analysisData.template === AnalysisCaseTemplate.BACKLOG_CASES" class="max-w-400">
        <BacklogChart ref="chartRef" :analysisInfo="analysisData" />
      </div>
      <div v-if="analysisData.template === AnalysisCaseTemplate.RECENT_DELIVERY" class="max-w-300">
        <RecentDeliveryChart ref="chartRef" :analysisInfo="analysisData" />
      </div>
      <div v-if="analysisData.template === AnalysisCaseTemplate.LEAD_TIME" class="max-w-300">
        <LeadTimeChart ref="chartRef" :analysisInfo="analysisData" />
      </div>
      <div v-if="analysisData.template === AnalysisCaseTemplate.UNPLANNED_CASES" class="max-w-300">
        <UnplannedChart ref="chartRef" :analysisInfo="analysisData" />
      </div>
      <div v-if="analysisData.template === AnalysisCaseTemplate.CASE_GROWTH_TREND" class="max-w-300">
        <GrowthTrendChart ref="chartRef" :analysisInfo="analysisData" />
      </div>
      <div v-if="analysisData.template === AnalysisCaseTemplate.RESOURCE_CREATION" class="max-w-300">
        <ResourceCreationChart ref="chartRef" :analysisInfo="analysisData" />
      </div>
    </div>

    <!-- Data Details Section -->
    <div class="mt-4">
      <div class="detail-title font-semibold pl-2 relative text-3.5 flex items-center">
        <span>{{ t('actions.detail') }}</span>
        <Button
          v-show="props.data?.id"
          type="link"
          :loading="isExporting"
          size="small"
          class="ml-3"
          @click="handleExportDetail">
          <Icon icon="icon-daochu1" class="mr-1" />
          {{ t('actions.export') }}
        </Button>
      </div>

      <!-- Special handling for Recent Delivery template with multiple time periods -->
      <template v-if="analysisData.template === AnalysisCaseTemplate.RECENT_DELIVERY">
        <div class="text-center mt-3">
          {{ t('functionAnalysis.today') }}
        </div>
        <Table
          key="today"
          class="w-full mb-5 mt-2"
          size="small"
          :pagination="false"
          :scroll="{x: 1000, y: 300}"
          :columns="tableColumns"
          :dataSource="todayDeliveryTableData" />
        <div class="text-center">
          {{ t('quickSearch.last7Days') }}
        </div>
        <Table
          key="last7Days"
          class="w-full mb-5 mt-2"
          size="small"
          :pagination="false"
          :scroll="{x: 1000, y: 300}"
          :columns="tableColumns"
          :dataSource="last7DaysDeliveryTableData" />
        <div class="text-center">
          {{ t('functionAnalysis.lastMonth') }}
        </div>
        <Table
          key="lastMonth"
          class="w-full mb-5 mt-2"
          size="small"
          :pagination="false"
          :scroll="{x: 1000, y: 300}"
          :columns="tableColumns"
          :dataSource="lastMonthDeliveryTableData" />
      </template>

      <!-- Standard table for other templates -->
      <Table
        v-else
        class="w-full mt-3"
        size="small"
        :pagination="false"
        :scroll="{x: 1000, y: 300}"
        :columns="tableColumns"
        :dataSource="mainTableData">
      </Table>
    </div>
  </div>
</template>
<style scoped>
.detail-title {
  @apply relative;
}

.detail-title::before{
  @apply bg-blue-1;

  content: '';
  display: inline-block;
  position: absolute;
  top: 2px;
  left: 0;
  width: 4px;
  height: 18px;
}
</style>
