<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts/core';
import { LegendComponent, LegendComponentOption, TooltipComponent, TooltipComponentOption } from 'echarts/components';
import { PieChart, PieSeriesOption } from 'echarts/charts';
import { LabelLayout } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';
import { analysis } from '@/api/tester';

type Props = {
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: ''
});

type EChartsOption = echarts.ComposeOption<TooltipComponentOption | LegendComponentOption | PieSeriesOption>;

const windowResizeNotify = inject('windowResizeNotify', ref<string>());

const typeChartRef = ref();
const statusChartRef = ref();

let typeEchart: echarts.ECharts;
let statusEchart: echarts.ECharts;
const echartOption: EChartsOption = {
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
    formatter: function (name) {
      const data = echartOption?.series?.[0].data;
      for (let i = 0; i < data.length; i++) {
        if (data[i].name === name) {
          name += ' ' + data[i].value;
          break;
        }
      }
      return name;
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
          name: '项目',
          value: 0
        },
        {
          name: '功能',
          value: 0
        },
        {
          name: '接口',
          value: 0
        },
        {
          name: '场景',
          value: 0
        },
        {
          name: '任务',
          value: 0
        },
        {
          name: '执行',
          value: 0
        }
      ]
    }
  ]
};

const statusEchartOption: EChartsOption = {
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
    formatter: function (name) {
      const data = statusEchartOption?.series?.[0].data;
      for (let i = 0; i < data.length; i++) {
        if (data[i].name === name) {
          name += ' ' + data[i].value;
          break;
        }
      }
      return name;
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
          name: '成功',
          value: 0
        },
        {
          name: '失败',
          value: 0
        }
      ]
    }
  ]
};

const renderChart = () => {
  if (!typeEchart) {
    echarts.use([TooltipComponent, LegendComponent, PieChart, CanvasRenderer, LabelLayout]);
    typeEchart = echarts.init(typeChartRef.value);
    typeEchart.setOption(echartOption);
  } else {
    typeEchart.setOption(echartOption);
  }
  if (!statusEchart) {
    echarts.use([TooltipComponent, LegendComponent, PieChart, CanvasRenderer, LabelLayout]);
    statusEchart = echarts.init(statusChartRef.value);
    statusEchart.setOption(statusEchartOption);
  } else {
    statusEchart.setOption(statusEchartOption);
  }
};

const resizeHandler = () => {
  if (typeEchart) {
    typeEchart.resize();
  }
};

const dataSource = ref({});
const total = ref(0);
const loadChartData = async () => {
  const [error, { data = {} }] = await analysis.getCustomizationSummary({
    projectId: props.projectId,
    name: 'Report',
    groupBy: 'STATUS',
    groupByColumns: ['category', 'template', 'status'], // 顺序不能改
    'aggregates[0].column': 'id',
    'aggregates[0].function': 'COUNT'
  });
  if (error) {
    return;
  }
  dataSource.value = data;
  const { PROJECT = {}, FUNCTIONAL = {}, APIS = {}, SCENARIO = {}, TASK = {}, EXECUTION = {} } = data.category || {};
  const chartData = [PROJECT.COUNT_id || 0, FUNCTIONAL.COUNT_id || 0, APIS.COUNT_id || 0, SCENARIO.COUNT_id || 0, TASK.COUNT_id || 0, EXECUTION.COUNT_id || 0];
  chartData.forEach((value, idx) => {
    echartOption.series[0].data[idx].value = value;
  });

  const { FAILURE = {}, SUCCESS = {} } = data.status || {};
  const statusChartData = [SUCCESS.COUNT_id || 0, FAILURE.COUNT_id || 0];
  statusChartData.forEach((value, idx) => {
    statusEchartOption.series[0].data[idx].value = value;
  });
  total.value = (+SUCCESS.TOTAL_COUNT_id || +FAILURE.TOTAL_COUNT_id || 0).toFixed(0);
  renderChart();
};

onMounted(() => {
  watch(() => props.projectId, (newValue) => {
    if (newValue) {
      loadChartData();
    }
  }, { immediate: true });
  watch(() => windowResizeNotify.value, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    resizeHandler();
  }, { immediate: true });

  renderChart();
});
</script>

<template>
  <div class="flex space-x-2">
    <div class="relative leading-5 text-3">
      <div
        ref="typeChartRef"
        class="w-70 h-34"></div>
      <div class="mark-container">
        <div class="text-center">分类</div>
        <div class="text-3.5 text-center font-semibold">{{ total || 0 }}</div>
      </div>
    </div>
    <div class="relative leading-5 text-3">
      <div
        ref="statusChartRef"
        class="w-70 h-34"></div>
      <div class="mark-container">
        <div class="text-center">状态</div>
        <div class="text-3.5 text-center font-semibold">{{ total || 0 }}</div>
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
