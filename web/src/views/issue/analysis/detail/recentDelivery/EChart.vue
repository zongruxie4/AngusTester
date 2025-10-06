<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

interface Props {
  chart0Value: {
    yData0: number[],
    yData1: number[],
    yData2: number[]
  },

  chart1Value: {
    yData0: number[],
    yData1: number[],
    yData2: number[]
  },
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  chart0Value: () => ({
    yData0: [0, 0, 0],
    yData1: [0, 0, 0],
    yData2: [0, 0, 0]
  }),
  chart1Value: () => ({
    yData0: [0, 0, 0],
    yData1: [0, 0, 0],
    yData2: [0, 0, 0]
  })
});

const deliveryRef = ref();
const deliveryWorkloadRef = ref();

let deliveryRefEChart;
let deliveryWorkloadEChart;

// 交付数（完成数）
const deliveryEChartConfig = {
  title: {
    text: t('issueAnalysis.detail.recentDelivery.chartTitles.deliveryCount'),
    bottom: 0,
    left: 'center',
    textStyle: {
      fontSize: 12
    }
  },
  grid: {
    left: '40',
    right: '30',
    bottom: '50',
    top: '40'
  },
  xAxis: {
    type: 'category',
    data: [
      t('chart.today'),
      t('chart.last7Days'),
      t('chart.lastMonth')
    ],
    axisLabel: {
      interval: 0,
      overflow: 'break'
    }
  },
  yAxis: [{
    type: 'value'
  }],
  tooltip: {
    show: true
  },
  legend: {
    show: true,
    data: [
      t('issueAnalysis.detail.recentDelivery.chartLabels.deliveryCount'),
      t('issueAnalysis.detail.recentDelivery.chartLabels.deliveryOverdueCount'),
      t('common.counts.totalCount')
    ],
    top: 0
  },
  series: [
    {
      name: t('issueAnalysis.detail.recentDelivery.chartLabels.deliveryCount'),
      itemStyle: {
        color: '#52C41A',
        borderRadius: [5, 5, 0, 0]
      },
      barGap: 0,
      data: [0, 0],
      type: 'bar',
      barMaxWidth: '30',
      label: {
        show: true,
        position: 'top'
      }
    },
    {
      name: t('issueAnalysis.detail.recentDelivery.chartLabels.deliveryOverdueCount'),
      itemStyle: {
        color: 'orange',
        borderRadius: [5, 5, 0, 0]
      },
      data: [0, 0],
      type: 'bar',
      barMaxWidth: '30',
      label: {
        show: true,
        position: 'top'
      }
    },
    {
      name: t('common.counts.totalCount'),
      itemStyle: {
        color: 'rgb(68,93,179)',
        borderRadius: [5, 5, 0, 0]
      },
      data: [0, 0],
      type: 'bar',
      barMaxWidth: '30',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};

// 交付工作量（完成量）
const deliveryWorkloadEChartConfig = JSON.parse(JSON.stringify({
  ...deliveryEChartConfig,
  title: {
    text: '0%',
    left: '35%',
    top: '45%',
    padding: 2,
    subtext: t('issueAnalysis.detail.recentDelivery.chartTitles.deliveryWorkload'),
    itemGap: 55,
    textAlign: 'center',
    textStyle: {
      fontSize: 12,
      fontWeight: 'bolder'
    },
    subtextStyle: {
      fontSize: 12,
      color: '#000'
    }
  }
}));

onMounted(() => {
  deliveryRefEChart = eCharts.init(deliveryRef.value);
  deliveryWorkloadEChart = eCharts.init(deliveryWorkloadRef.value);

  watch([() => props.chart0Value, () => props.chart1Value], () => {
    deliveryEChartConfig.series[0].data = props.chart0Value.yData0;
    deliveryEChartConfig.series[1].data = props.chart0Value.yData1;
    deliveryEChartConfig.series[2].data = props.chart0Value.yData2;

    deliveryWorkloadEChartConfig.series[0].data = props.chart1Value.yData0;
    deliveryWorkloadEChartConfig.series[1].data = props.chart1Value.yData1;
    deliveryWorkloadEChartConfig.series[2].data = props.chart1Value.yData2;

    deliveryWorkloadEChart.setOption(deliveryWorkloadEChartConfig);
    deliveryRefEChart.setOption(deliveryEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    deliveryWorkloadEChart.resize();
    deliveryRefEChart.resize();
  }
});

</script>
<template>
  <div class="flex chart-container">
    <div ref="deliveryRef" class="flex-1 h-40"></div>
    <div ref="deliveryWorkloadRef" class="flex-1 h-40"></div>
  </div>
</template>
<style scoped>
.chart-container {
  padding: 20px;
}
</style>
