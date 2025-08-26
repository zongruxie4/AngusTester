<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { utils } from '@xcan-angus/infra';
import * as echarts from 'echarts/core';
import { LegendComponent, LegendComponentOption, TooltipComponent, TooltipComponentOption } from 'echarts/components';
import { PieChart, PieSeriesOption } from 'echarts/charts';
import { LabelLayout } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';

import { ResourceInfo } from './PropsType';

const { t } = useI18n();

type Props = {
  dataSource: ResourceInfo;
  resizeNotify: string;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined,
  resizeNotify: undefined
});

type EChartsOption = echarts.ComposeOption<TooltipComponentOption | LegendComponentOption | PieSeriesOption>;

const containerRef = ref<HTMLElement>();
const markContainerStyle = ref<{ left: string; }>({ left: '50%' });

const domId = utils.uuid('pie');
const total = ref<number>(0);

let echartInstance: echarts.ECharts;
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
      data: []
    }
  ]
};

const renderChart = () => {
  if (!echartInstance) {
    echarts.use([TooltipComponent, LegendComponent, PieChart, CanvasRenderer, LabelLayout]);
    echartInstance = echarts.init(document.getElementById(domId));
    echartInstance.setOption(echartOption);
    return;
  }

  // 重新绘制图表
  echartInstance.setOption(echartOption);
};

const setEchartOption = () => {
  if (containerRef.value) {
    const width = containerRef.value.offsetWidth;
    if (width > 428) {
      echartOption.series![0].center[0] = '50%';
      markContainerStyle.value.left = '50%';
    } else if (width > 348 && width <= 428) {
      echartOption.series![0].center[0] = '40%';
      markContainerStyle.value.left = '40%';
    } else {
      echartOption.series![0].center[0] = '35%';
      markContainerStyle.value.left = '35%';
    }
  }
};

const resizeHandler = () => {
  setEchartOption();

  if (echartInstance) {
    echartInstance.setOption(echartOption);
    echartInstance.resize();
  }
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    echartOption.series![0].data = [
      { name: t('scenarioHome.chart.testTypes.perfTest'), value: 0 },
      { name: t('scenarioHome.chart.testTypes.stabilityTest'), value: 0 },
      { name: t('scenarioHome.chart.testTypes.funcTest'), value: 0 },
      { name: t('scenarioHome.chart.testTypes.customTest'), value: 0 }
    ];
    if (newValue?.sceByScriptType) {
      const _data = newValue.sceByScriptType;
      if (_data) {
        total.value = (+_data.TEST_PERFORMANCE) + (+_data.TEST_STABILITY) + (+_data.TEST_FUNCTIONALITY) + (+_data.TEST_CUSTOMIZATION);
        echartOption.series![0].data[0].value = +_data.TEST_PERFORMANCE;
        echartOption.series![0].data[1].value = +_data.TEST_STABILITY;
        echartOption.series![0].data[2].value = +_data.TEST_FUNCTIONALITY;
        echartOption.series![0].data[3].value = +_data.TEST_CUSTOMIZATION;
      }
    }

    setEchartOption();
    renderChart();
  }, { immediate: true });

  watch(() => props.resizeNotify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    resizeHandler();
  }, { immediate: true });
});
</script>

<template>
  <div ref="containerRef" class="rounded border border-solid border-theme-text-box px-4 py-3.5">
    <div class="font-semibold">{{ t('scenarioHome.chart.testType') }}</div>
    <div class="relative">
      <div :id="domId" class="w-full h-50"></div>
      <div :style="markContainerStyle" class="absolute mark-container">
        <div class="text-theme-sub-content mb-1 text-center">{{ t('scenarioHome.chart.total') }}</div>
        <div class="text-3.5 text-center">{{ total }}</div>
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
