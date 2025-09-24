<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as echarts from 'echarts/core';
import { LegendComponent, LegendComponentOption, TooltipComponent, TooltipComponentOption } from 'echarts/components';
import { PieChart, PieSeriesOption } from 'echarts/charts';
import { LabelLayout } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';
import { analysis } from '@/api/tester';

type Props = {
  projectId: string;
}

type EChartsOption = echarts.ComposeOption<TooltipComponentOption | LegendComponentOption | PieSeriesOption>;

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  projectId: ''
});

// Inject window resize notification for chart resizing
const windowResizeNotify = inject('windowResizeNotify', ref<string>());

// Chart DOM references
const categoryChartRef = ref<HTMLElement>();
const statusChartRef = ref<HTMLElement>();

// Chart instances
let categoryChartInstance: echarts.ECharts;
let statusChartInstance: echarts.ECharts;

// Data state
const chartDataSource = ref<Record<string, any>>({});
const totalCount = ref<string>('0');

/**
 * Creates a legend formatter function that appends data values to legend names
 * @param seriesData - The series data array containing name and value pairs
 * @returns Formatted legend name with value
 */
const createLegendFormatter = (seriesData: Array<{ name: string; value: number }>) => {
  return function (name: string) {
    if (!seriesData) return name;

    const dataItem = seriesData.find(item => item.name === name);
    return dataItem ? `${name} ${dataItem.value}` : name;
  };
};

/**
 * Configuration for category distribution pie chart
 * Shows distribution of different test categories (Project, Functional, API, etc.)
 */
const categoryChartConfig: EChartsOption = {
  tooltip: {
    trigger: 'item',
    axisPointer: { type: 'shadow' },
    textStyle: {
      fontSize: 12
    }
  },
  legend: {
    top: 'middle',
    right: 10,
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 5,
    formatter: function (name: string) {
      const seriesData = categoryChartConfig?.series?.[0]?.data as Array<{ name: string; value: number }>;
      return createLegendFormatter(seriesData)(name);
    }
  },
  color: ['#52C41A', '#67D7FF', '#FFB925', 'rgba(251, 129, 255, 1)', 'rgba(255, 82, 82, 1)', 'rgba(0,119,255,1)'],
  series: [
    {
      name: '',
      type: 'pie',
      center: ['30%', '50%'],
      radius: ['65%', '90%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        formatter: '{c}'
      },
      labelLine: {
        show: true
      },
      emphasis: {
        label: {
          show: false,
          fontSize: 12,
          fontWeight: 'normal'
        }
      },
      data: [
        {
          name: t('reportHome.chart.categories.project'),
          value: 0
        },
        {
          name: t('reportHome.chart.categories.functional'),
          value: 0
        },
        {
          name: t('reportHome.chart.categories.api'),
          value: 0
        },
        {
          name: t('reportHome.chart.categories.scenario'),
          value: 0
        },
        {
          name: t('reportHome.chart.categories.task'),
          value: 0
        },
        {
          name: t('reportHome.chart.categories.execution'),
          value: 0
        }
      ]
    }
  ]
};

/**
 * Configuration for test status pie chart
 * Shows distribution of test results (Success vs Failure)
 */
const statusChartConfig: EChartsOption = {
  tooltip: {
    trigger: 'item',
    axisPointer: { type: 'shadow' },
    textStyle: {
      fontSize: 12
    }
  },
  legend: {
    top: 'middle',
    right: 10,
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 5,
    formatter: function (name: string) {
      const seriesData = statusChartConfig?.series?.[0]?.data as Array<{ name: string; value: number }>;
      return createLegendFormatter(seriesData)(name);
    }
  },
  color: ['#52C41A', 'rgba(255, 82, 82, 1)'],
  series: [
    {
      name: '',
      type: 'pie',
      center: ['30%', '50%'],
      radius: ['65%', '90%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        formatter: '{c}'
      },
      labelLine: {
        show: true
      },
      emphasis: {
        label: {
          show: false,
          fontSize: 12,
          fontWeight: 'normal'
        }
      },
      data: [
        {
          name: t('status.success'),
          value: 0
        },
        {
          name: t('status.failed'),
          value: 0
        }
      ]
    }
  ]
};

/**
 * Initializes ECharts components and registers required modules
 */
const initializeEChartsComponents = () => {
  echarts.use([TooltipComponent, LegendComponent, PieChart, CanvasRenderer, LabelLayout]);
};

/**
 * Renders both category and status pie charts
 * Initializes chart instances if they don't exist, otherwise updates existing charts
 */
const renderCharts = () => {
  // Render category chart
  if (!categoryChartInstance) {
    initializeEChartsComponents();
    categoryChartInstance = echarts.init(categoryChartRef.value);
    categoryChartInstance.setOption(categoryChartConfig);
  } else {
    categoryChartInstance.setOption(categoryChartConfig);
  }

  // Render status chart
  if (!statusChartInstance) {
    initializeEChartsComponents();
    statusChartInstance = echarts.init(statusChartRef.value);
    statusChartInstance.setOption(statusChartConfig);
  } else {
    statusChartInstance.setOption(statusChartConfig);
  }
};

/**
 * Handles chart resizing when window size changes
 */
const handleChartResize = () => {
  if (categoryChartInstance) {
    categoryChartInstance.resize();
  }
  if (statusChartInstance) {
    statusChartInstance.resize();
  }
};

/**
 * Loads chart data from API and updates chart configurations
 * Fetches test summary data grouped by category and status
 */
const loadChartData = async () => {
  try {
    const [error, { data = {} }] = await analysis.getCustomizationSummary({
      projectId: props.projectId,
      name: 'Report',
      groupBy: 'STATUS',
      groupByColumns: ['category', 'template', 'status'], // Order must not be changed
      'aggregates[0].column': 'id',
      'aggregates[0].function': 'COUNT'
    });

    if (error) {
      console.error('Failed to load chart data:', error);
      return;
    }

    // Store raw data for potential future use
    chartDataSource.value = data;

    // Update category chart data
    updateCategoryChartData(data);

    // Update status chart data
    updateStatusChartData(data);

    // Re-render charts with new data
    renderCharts();
  } catch (error) {
    console.error('Error loading chart data:', error);
  }
};

/**
 * Updates category chart data based on API response
 * @param data - Raw data from API containing category statistics
 */
const updateCategoryChartData = (data: Record<string, any>) => {
  const {
    PROJECT = {},
    FUNCTIONAL = {},
    APIS = {},
    SCENARIO = {},
    TASK = {},
    EXECUTION = {}
  } = data.category || {};

  const categoryData = [
    PROJECT.COUNT_id || 0,
    FUNCTIONAL.COUNT_id || 0,
    APIS.COUNT_id || 0,
    SCENARIO.COUNT_id || 0,
    TASK.COUNT_id || 0,
    EXECUTION.COUNT_id || 0
  ];

  // Update chart data values
  categoryData.forEach((value, index) => {
    if (categoryChartConfig.series?.[0]?.data?.[index]) {
      (categoryChartConfig.series[0].data[index] as { value: number }).value = value;
    }
  });
};

/**
 * Updates status chart data based on API response
 * @param data - Raw data from API containing status statistics
 */
const updateStatusChartData = (data: Record<string, any>) => {
  const { FAILURE = {}, SUCCESS = {} } = data.status || {};

  const statusData = [
    SUCCESS.COUNT_id || 0,
    FAILURE.COUNT_id || 0
  ];

  // Update chart data values
  statusData.forEach((value, index) => {
    if (statusChartConfig.series?.[0]?.data?.[index]) {
      (statusChartConfig.series[0].data[index] as { value: number }).value = value;
    }
  });

  // Calculate and update total count
  const successTotal = +SUCCESS.TOTAL_COUNT_id || 0;
  const failureTotal = +FAILURE.TOTAL_COUNT_id || 0;
  totalCount.value = (successTotal || failureTotal).toFixed(0);
};

onMounted(() => {
  // Watch for project ID changes and reload data
  watch(() => props.projectId, (newProjectId) => {
    if (newProjectId) {
      loadChartData();
    }
  }, { immediate: true });

  // Watch for window resize events and handle chart resizing
  watch(() => windowResizeNotify.value, (resizeEvent) => {
    if (resizeEvent === undefined || resizeEvent === null || resizeEvent === '') {
      return;
    }
    handleChartResize();
  }, { immediate: true });

  // Initial chart rendering
  renderCharts();
});
</script>

<template>
  <div class="flex space-x-2">
    <!-- Category Distribution Chart -->
    <div class="relative leading-5 text-3">
      <div
        ref="categoryChartRef"
        class="w-70 h-34"></div>
      <div class="mark-container">
        <div class="text-center">{{ t('reportHome.chart.labels.category') }}</div>
        <div class="text-3.5 text-center font-semibold">{{ totalCount || 0 }}</div>
      </div>
    </div>

    <!-- Test Status Chart -->
    <div class="relative leading-5 text-3">
      <div
        ref="statusChartRef"
        class="w-70 h-34"></div>
      <div class="mark-container">
        <div class="text-center">{{ t('reportHome.chart.labels.status') }}</div>
        <div class="text-3.5 text-center font-semibold">{{ totalCount || 0 }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.mark-container {
  position: absolute;
  top: 50%;
  left: 30%;
  transform: translate(-50%, -50%);
}
</style>
