<script lang="ts" setup>
import { computed, inject, onBeforeUnmount, onMounted, ref, watch, Ref } from 'vue';
import * as echarts from 'echarts';
import { RadioGroup } from 'ant-design-vue';
import elementResizeDetector from 'element-resize-detector';
import { getDateArr } from '@/utils/utils';
import { analysis } from '@/api/tester';
import { useI18n } from 'vue-i18n';

// Type Definitions
interface UserInfo {
  [key: string]: string;
}

interface ChartProps {
  userInfo?: UserInfo;
}

type ChartTarget = 'NUM' | 'WORKLOAD';

// Props and Composables
const props = withDefaults(defineProps<ChartProps>(), {
  userInfo: undefined
});

const { t } = useI18n();

// Injected Dependencies
const projectId = inject<Ref<string>>('projectId', ref(''));

// Chart Configuration
const resizeDetector = elementResizeDetector({ strategy: 'scroll' });

// Reactive State
const chartData = ref();
const selectedChartTarget = ref<ChartTarget>('NUM');
const chartContainerRef = ref();
let chartInstance: echarts.ECharts;

// Computed Properties
const chartTargetOptions = computed(() => [
  {
    value: 'NUM',
    label: t('common.count')
  },
  {
    value: 'WORKLOAD',
    label: t('common.workload')
  }
]);

// Chart Configuration Object
const chartConfiguration = {
  grid: {
    left: '30',
    right: '20',
    bottom: '60',
    top: 20
  },
  legend: {
    show: true,
    bottom: 0,
    type: 'plain'
  },
  tooltip: {
    show: true,
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value',
    min: function (value) {
      if (value.max < 1) {
        return 10;
      } else {
        return undefined;
      }
    }
  },
  series: [
    {
      name: t('chart.burndown.remaining'),
      data: [],
      type: 'line'
    },
    {
      name: t('chart.burndown.expected'),
      data: [],
      type: 'line',
      smooth: true
    }
  ]
};

/**
 * <p>Loads burndown chart data from the API.</p>
 * <p>Fetches data based on current project and user context.</p>
 */
const loadChartData = async () => {
  const [error, { data }] = await analysis.getFuncTesterBurndown({
    projectId: projectId.value,
    userId: props.userInfo?.id
  });
  if (error) {
    return;
  }
  chartData.value = data;
};

/**
 * <p>Handles chart resize events.</p>
 * <p>Updates chart configuration and triggers resize to maintain responsiveness.</p>
 */
const handleChartResize = () => {
  chartInstance.setOption(chartConfiguration);
  chartInstance.resize();
};

/**
 * <p>Updates chart data based on selected target and available data.</p>
 * <p>Processes data arrays and updates chart configuration accordingly.</p>
 */
const updateChartData = () => {
  if (chartData.value) {
    const targetData = chartData.value[selectedChartTarget.value];
    const xAxisData = (targetData?.expected || []).map(item => item.timeSeries);
    const expectedData = (targetData?.expected || []).map(item => item.value);
    const remainingData = (targetData?.remaining || []).map(item => item.value);

    (chartConfiguration.xAxis as any).data = xAxisData;
    (chartConfiguration.series[0] as any).data = remainingData;
    (chartConfiguration.series[1] as any).data = expectedData;
  } else {
    (chartConfiguration.xAxis as any).data = [];
    (chartConfiguration.series[0] as any).data = [];
    (chartConfiguration.series[1] as any).data = [];
  }

  if (chartConfiguration.xAxis.data.length === 0) {
    (chartConfiguration.xAxis as any).data = getDateArr();
    (chartConfiguration.series[0] as any).data = [0, 0, 0, 0, 0, 0, 0];
  }

  chartInstance.setOption(chartConfiguration);
};

// Lifecycle
onMounted(() => {
  chartInstance = echarts.init(chartContainerRef.value);
  chartInstance.setOption(chartConfiguration);

  watch(() => projectId.value, (newValue) => {
    if (newValue) {
      loadChartData();
    }
  }, {
    immediate: true
  });

  watch([() => selectedChartTarget.value, () => chartData.value], () => {
    updateChartData();
  });

  resizeDetector.listenTo(chartContainerRef.value, handleChartResize);
});

onBeforeUnmount(() => {
  resizeDetector.removeListener(chartContainerRef.value, handleChartResize);
});
</script>
<template>
  <div class="pt-1.5 flex flex-col">
    <div class="text-3.5 font-semibold flex justify-between">
      {{ t('chart.burndown.title') }}
      <RadioGroup v-model:value="selectedChartTarget" :options="chartTargetOptions">
      </RadioGroup>
    </div>
    <div ref="chartContainerRef" class="border rounded p-2 flex-1 mt-3">
    </div>
  </div>
</template>
