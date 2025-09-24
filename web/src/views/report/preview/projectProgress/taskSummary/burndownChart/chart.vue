<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts/core';
import { GridComponent, LegendComponent, TitleComponent, ToolboxComponent, TooltipComponent } from 'echarts/components';
import { LineChart } from 'echarts/charts';
import { UniversalTransition } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '../../PropsType';
import { chartSeriesColorConfig } from '@/views/report/preview/PropsType';

type Props = {
  title:string;
  dataSource: ReportContent['content']['tasks']['burnDownCharts']['NUM'];
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  title: undefined,
  dataSource: undefined
});

echarts.use([
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  LineChart,
  CanvasRenderer,
  UniversalTransition
]);

const chartRef = ref();
let chart: echarts.ECharts;

const echartOption = {
  grid: {
    left: '50',
    right: '50',
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
    type: 'value'
  },
  series: [
    {
      name: t('chart.burndown.remaining'),
      data: [],
      type: 'line',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 1, color: `rgba(${chartSeriesColorConfig[0]}, 0.1)` },
          { offset: 0, color: `rgba(${chartSeriesColorConfig[0]}, 1)` }
        ])
      },
      areaStyle: {}
    },
    {
      name: t('chart.burndown.expected'),
      data: [],
      type: 'line',
      smooth: true,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 1, color: `rgba(${chartSeriesColorConfig[1]}, 0.1)` },
          { offset: 0, color: `rgba(${chartSeriesColorConfig[1]}, 1)` }
        ])
      },
      areaStyle: {}
    }
  ]
};

onMounted(() => {
  chart = echarts.init(chartRef.value);
  chart.setOption(echartOption);

  watch(() => props.dataSource, (newValue) => {
    if (!newValue) {
      echartOption.xAxis.data = [];
      echartOption.series[0].data = [];
      echartOption.series[1].data = [];
      return;
    }

    const expected = newValue?.expected || [];
    const remaining = newValue?.remaining || [];
    const xData = expected.map(item => item.timeSeries);
    const expectedYData = expected.map(item => item.value);
    const remainingYData = remaining.map(item => item.value);
    echartOption.xAxis.data = xData;
    echartOption.series[0].data = remainingYData;
    echartOption.series[1].data = expectedYData;
    chart.setOption(echartOption);
  }, { immediate: true });
});

</script>
<template>
  <div>
    <div class="flex items-center mb-2 text-theme-title">
      <span>{{ props.title }}</span>
    </div>
    <div ref="chartRef" class="border rounded p-2 h-60">
    </div>
  </div>
</template>
