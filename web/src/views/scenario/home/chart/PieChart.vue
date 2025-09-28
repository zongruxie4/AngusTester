<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { utils, ScriptType, enumUtils } from '@xcan-angus/infra';
import * as echarts from 'echarts/core';
import { LegendComponent, LegendComponentOption, TooltipComponent, TooltipComponentOption } from 'echarts/components';
import { PieChart, PieSeriesOption } from 'echarts/charts';
import { LabelLayout } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';

import { ResourceInfo } from './types';

/**
 * <p>Component props interface for PieChart component.</p>
 * <p>Defines the data source and resize notification properties.</p>
 */
type Props = {
  dataSource: ResourceInfo;
  resizeNotify: string;
}

/**
 * <p>ECharts configuration option type for pie chart.</p>
 * <p>Combines tooltip, legend, and pie series options for type safety.</p>
 */
type EChartsOption = echarts.ComposeOption<TooltipComponentOption | LegendComponentOption | PieSeriesOption>;

/**
 * <p>Style configuration for the center mark container.</p>
 * <p>Controls the positioning of the total count display in the pie center.</p>
 */
type MarkContainerStyle = {
  left: string;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({
    allSce: '0',
    sceByLast7Days: '0',
    sceByLastMonth: '0',
    sceByScriptType: {
      TEST_FUNCTIONALITY: '0',
      TEST_PERFORMANCE: '0',
      TEST_STABILITY: '0',
      TEST_CUSTOMIZATION: '0',
      MOCK_DATA: '0',
      MOCK_APIS: '0'
    },
    sceByPluginName: {}
  }),
  resizeNotify: undefined
});

/**
 * <p>Reference to the chart container element for responsive calculations.</p>
 * <p>Used to determine container width for adaptive chart positioning.</p>
 */
const chartContainerRef = ref<HTMLElement>();

/**
 * <p>Dynamic style configuration for the center mark container.</p>
 * <p>Adjusts left position based on container width for responsive design.</p>
 */
const centerMarkStyle = ref<MarkContainerStyle>({ left: '50%' });

/**
 * <p>Unique DOM element identifier for the chart container.</p>
 * <p>Generated using UUID to ensure uniqueness across component instances.</p>
 */
const chartDomId = utils.uuid('pie');

/**
 * <p>Total number of test scenarios displayed in the center of the pie chart.</p>
 * <p>Calculated from the sum of all test type counts.</p>
 */
const totalTestScenariosCount = ref<number>(0);

/**
 * <p>ECharts instance for managing chart lifecycle and operations.</p>
 * <p>Initialized lazily when first render is called.</p>
 */
let chartInstance: echarts.ECharts;

/**
 * <p>Default pie chart configuration with styling and behavior settings.</p>
 * <p>Includes tooltip, legend, color scheme, and series configuration for test types.</p>
 */
const chartConfiguration: EChartsOption = {
  tooltip: {
    trigger: 'item',
    axisPointer: { type: 'shadow' },
    textStyle: {
      fontSize: 12
    }
  },
  legend: {
    top: 'middle',
    right: 0,
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 10
  },
  color: ['#67D7FF', '#FFB925', '#52C41A', '#2D8EFF'],
  series: [
    {
      name: '',
      type: 'pie',
      center: ['50%', '50%'],
      radius: ['60%', '80%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{c}' // Display count value on each segment
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
      data: []
    }
  ]
};

/**
 * <p>Initializes and renders the ECharts pie chart.</p>
 * <p>Registers required ECharts components and creates chart instance if not exists.</p>
 * <p>Updates chart with current configuration data.</p>
 */
const renderPieChart = (): void => {
  if (!chartInstance) {
    // Register required ECharts components
    echarts.use([TooltipComponent, LegendComponent, PieChart, CanvasRenderer, LabelLayout]);

    // Initialize chart instance with DOM element
    chartInstance = echarts.init(document.getElementById(chartDomId));
    chartInstance.setOption(chartConfiguration);
    return;
  }

  // Update existing chart with new data
  chartInstance.setOption(chartConfiguration);
};

/**
 * <p>Adjusts chart center position and center mark based on container width.</p>
 * <p>Provides responsive layout for different screen sizes.</p>
 * <p>Updates both chart center and center mark positioning.</p>
 */
const adjustChartLayout = (): void => {
  if (!chartContainerRef.value) {
    return;
  }

  const containerWidth = chartContainerRef.value.offsetWidth;

  if (containerWidth > 428) {
    // Large screens: center the chart
    chartConfiguration.series![0].center[0] = '50%';
    centerMarkStyle.value.left = '50%';
  } else if (containerWidth > 348 && containerWidth <= 428) {
    // Medium screens: shift chart slightly left
    chartConfiguration.series![0].center[0] = '40%';
    centerMarkStyle.value.left = '40%';
  } else {
    // Small screens: shift chart more to the left
    chartConfiguration.series![0].center[0] = '35%';
    centerMarkStyle.value.left = '35%';
  }
};

/**
 * <p>Handles chart resize when container dimensions change.</p>
 * <p>Recalculates layout and updates chart positioning and size.</p>
 */
const handleChartResize = (): void => {
  adjustChartLayout();

  if (chartInstance) {
    chartInstance.setOption(chartConfiguration);
    chartInstance.resize();
  }
};

/**
 * <p>Processes test type data and updates chart configuration.</p>
 * <p>Extracts scenario counts by test type and calculates total count.</p>
 * <p>Updates both chart data and center mark display.</p>
 *
 * @param dataSource - Resource information containing scenario counts by test type
 */
const processTestTypeData = (dataSource: ResourceInfo): void => {
  // Initialize chart data with default test types
  chartConfiguration.series![0].data = [
    { name: enumUtils.getEnumDescription(ScriptType, ScriptType.TEST_PERFORMANCE), value: 0 },
    { name: enumUtils.getEnumDescription(ScriptType, ScriptType.TEST_STABILITY), value: 0 },
    { name: enumUtils.getEnumDescription(ScriptType, ScriptType.TEST_FUNCTIONALITY), value: 0 },
    { name: enumUtils.getEnumDescription(ScriptType, ScriptType.TEST_CUSTOMIZATION), value: 0 }
  ];

  if (dataSource?.sceByScriptType) {
    const testTypeData = dataSource.sceByScriptType;

    if (testTypeData) {
      // Calculate total scenarios count
      totalTestScenariosCount.value =
        (+testTypeData.TEST_PERFORMANCE) +
        (+testTypeData.TEST_STABILITY) +
        (+testTypeData.TEST_FUNCTIONALITY) +
        (+testTypeData.TEST_CUSTOMIZATION);

      // Update chart data with actual values
      chartConfiguration.series![0].data[0].value = +testTypeData.TEST_PERFORMANCE;
      chartConfiguration.series![0].data[1].value = +testTypeData.TEST_STABILITY;
      chartConfiguration.series![0].data[2].value = +testTypeData.TEST_FUNCTIONALITY;
      chartConfiguration.series![0].data[3].value = +testTypeData.TEST_CUSTOMIZATION;
    }
  }
};

onMounted(() => {
  // Watch for data source changes and update chart accordingly
  watch(() => props.dataSource, (newDataSource) => {
    processTestTypeData(newDataSource);
    adjustChartLayout();
    renderPieChart();
  }, { immediate: true });

  // Watch for resize notifications and handle chart resizing
  watch(() => props.resizeNotify, (resizeNotification) => {
    if (resizeNotification === undefined || resizeNotification === null || resizeNotification === '') {
      return;
    }

    handleChartResize();
  }, { immediate: true });
});
</script>

<template>
  <div ref="chartContainerRef" class="rounded border border-solid border-theme-text-box px-4 py-3.5">
    <div class="font-semibold">{{ t('common.testType') }}</div>
    <div class="relative">
      <div :id="chartDomId" class="w-full h-50"></div>
      <div :style="centerMarkStyle" class="absolute mark-container">
        <div class="text-theme-sub-content mb-1 text-center">{{ t('chart.total') }}</div>
        <div class="text-3.5 text-center">{{ totalTestScenariosCount }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.mark-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
</style>
