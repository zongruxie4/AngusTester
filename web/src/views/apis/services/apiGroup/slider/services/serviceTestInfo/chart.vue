<script lang="ts" setup>
import { ref, watch, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import * as echarts from 'echarts';

interface Props {
  progressValues: number[];
  typeValues: number[];
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  progressValues: () => ([0, 0, 0, 0]),
  typeValues: () => ([0, 0, 0, 0])
});

const testProgressRef = ref();
let testProgressChart;
const testProgressChartConfig = {
  title: {
    text: t('service.serviceTestDetail.chart.testApiStats'),
    bottom: 5,
    left: 'center',
    textStyle: {
      fontSize: 12
    }
  },
  grid: {
    left: '60',
    right: '20',
    bottom: '50',
    top: '20'
  },
  xAxis: {
    type: 'category',
    data: [t('service.serviceTestDetail.chart.tested'), t('service.serviceTestDetail.chart.unTested'), t('service.serviceTestDetail.chart.testPassed'), t('service.serviceTestDetail.chart.unPassed')]
  },
  yAxis: {
    type: 'value'
  },
  tooltip: {
    show: true
  },
  series: [
    {
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)',
        borderRadius: [5, 5, 0, 0]
      },
      barMaxWidth: '20',
      data: [] as number[],
      type: 'bar',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};

const testTypeRef = ref();
let testTypeChart;
const testTypeChartConfig = {
  title: {
    text: t('service.serviceTestDetail.chart.testTypeStats'),
    bottom: 5,
    left: 'center',
    textStyle: {
      fontSize: 12
    }
  },
  grid: {
    left: '60',
    right: '20',
    bottom: '50',
    top: '20'
  },
  xAxis: {
    type: 'category',
    data: [t('service.serviceTestDetail.chart.totalTestNum'), t('service.serviceTestDetail.chart.functionalTest'), t('service.serviceTestDetail.chart.performanceTest'), t('service.serviceTestDetail.chart.stabilityTest')]
  },
  yAxis: {
    type: 'value'
  },
  tooltip: {
    show: true
  },
  series: [
    {
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)',
        borderRadius: [5, 5, 0, 0]
      },
      barMaxWidth: '20',
      data: [] as number[],
      type: 'bar',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};

onMounted(() => {
  testProgressChart = echarts.init(testProgressRef.value);
  testTypeChart = echarts.init(testTypeRef.value);
  testProgressChart.setOption(testProgressChartConfig);
  testTypeChart.setOption(testTypeChartConfig);
  watch([() => props.progressValues, () => props.typeValues], () => {
    testProgressChartConfig.series[0].data = props.progressValues;
    testTypeChartConfig.series[0].data = props.typeValues;

    testProgressChart.setOption(testProgressChartConfig);
    testTypeChart.setOption(testTypeChartConfig);
  }, {
    immediate: true
  });
});

</script>
<template>
  <div class="flex-1 ml-4 space-x-4 min-w-100 inline-flex">
    <div ref="testProgressRef" class="h-40  flex-1"></div>
    <div ref="testTypeRef" class="h-40 flex-1"></div>
  </div>
</template>
