<script lang="ts" setup>
// Vue core imports
import { ref, watch, onMounted, onBeforeUnmount } from 'vue';
import { useI18n } from 'vue-i18n';

// Third-party library imports
import * as echarts from 'echarts';
import elementResizeDetector from 'element-resize-detector';

const { t } = useI18n();

// Element resize detector for responsive charts
const elementResizeDetectorInstance = elementResizeDetector({ strategy: 'scroll' });

// Component references
const testProgressChartRef = ref();
const testResultChartRef = ref();
const chartContainerRef = ref();

/**
 * Component props interface for service progress data
 */
interface Props {
  value: Record<string, any>;
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  value: () => ({})
});

// ECharts pie chart configuration template
const pieChartConfigurationTemplate = {
  title: {
    text: '',
    textStyle: {
      fontSize: 12
    }
  },
  tooltip: {
    trigger: 'item',
    confine: true,
    textStyle: {
      fontSize: 12
    }
  },
  legend: {
    left: 'center',
    bottom: 0,
    orient: 'horizontal',
    itemHeight: 14,
    itemWidth: 14
  },
  series: [
    {
      name: '',
      type: 'pie',
      startAngle: 90,
      radius: ['35%', '50%'],
      center: ['50%', '40%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      labelLine: {
        length: 5,
        length2: 5
      },
      data: []
    }
  ]
};

// Chart instances
let testProgressChartInstance;
let testResultChartInstance;

/**
 * Resize both chart instances when container size changes
 */
const handleChartResize = () => {
  testProgressChartInstance.resize();
  testResultChartInstance.resize();
};

/**
 * Initialize and update charts when component mounts
 */
onMounted(() => {
  watch(() => props.value, newValue => {
    const testProgressConfig = JSON.parse(JSON.stringify(pieChartConfigurationTemplate));
    testProgressConfig.series[0].data = [
      {
        name: t('xcan_httpTestInfo.tested'),
        value: newValue.testedNum || 0,
        itemStyle: {
          color: '#52C41A'
        }
      },
      {
        name: t('xcan_httpTestInfo.untested'),
        value: newValue.unTestedNum || 0,
        itemStyle: {
          color: 'rgba(200, 202, 208, 1)'
        }
      }
    ];

    const testResultConfig = JSON.parse(JSON.stringify(pieChartConfigurationTemplate));
    testResultConfig.series[0].data = [
      {
        name: t('xcan_httpTestInfo.passed'),
        value: newValue.testPassedNum || 0,
        itemStyle: {
          color: '#52C41A'
        }
      },
      {
        name: t('xcan_httpTestInfo.failed'),
        value: newValue.testUnPassedNum || 0,
        itemStyle: {
          color: 'rgba(200, 202, 208, 1)'
        }
      }
    ];

    testProgressChartInstance = echarts.init(testProgressChartRef.value);
    testResultChartInstance = echarts.init(testResultChartRef.value);
    testProgressChartInstance.setOption(testProgressConfig);
    testResultChartInstance.setOption(testResultConfig);
  }, {
    immediate: true
  });

  elementResizeDetectorInstance.listenTo(chartContainerRef.value, handleChartResize);
});

/**
 * Clean up chart instances and resize listeners when component unmounts
 */
onBeforeUnmount(() => {
  elementResizeDetectorInstance.removeListener(chartContainerRef.value, handleChartResize);
});

</script>
<template>
  <div ref="chartContainerRef" class="flex">
    <div ref="testProgressChartRef" class="flex-1 h-30">
    </div>
    <div ref="testResultChartRef" class="flex-1 h-30">
    </div>
  </div>
</template>
