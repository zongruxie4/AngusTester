<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

const { t } = useI18n();

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
const deliverygedWorkloadRef = ref();

let deliveryRefEchart;
let deliveryWorkloadEchart;
let deliverygedWorkloadEchart;

// 交付数（完成数）
const deliveryEchartConfig = {
  title: {
    text: t('functionAnalysis.detail.recentDelivery.deliveryCount'),
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
      t('functionAnalysis.detail.recentDelivery.today'),
      t('quickSearch.last7Days'),
      t('functionAnalysis.detail.recentDelivery.lastMonth')
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
      t('functionAnalysis.detail.recentDelivery.deliveryCount2'),
      t('functionAnalysis.detail.recentDelivery.deliveryOverdueCount'),
      t('functionAnalysis.detail.recentDelivery.totalCount')
    ],
    top: 0
  },
  series: [
    {
      name: t('functionAnalysis.detail.recentDelivery.deliveryCount2'),
      itemStyle: {
        color: 'blue',
        borderRadius: [5, 5, 0, 0]
      },
      barGap: 0,
      data: [0, 0],
      type: 'bar',
      barMaxWidth: '20',
      label: {
        show: true,
        position: 'top'
      }
    },
    {
      name: t('functionAnalysis.detail.recentDelivery.deliveryOverdueCount'),
      itemStyle: {
        color: 'orange',
        borderRadius: [5, 5, 0, 0]
      },
      data: [0, 0],
      type: 'bar',
      barMaxWidth: '20',
      label: {
        show: true,
        position: 'top'
      }
    },
    {
      name: t('functionAnalysis.detail.recentDelivery.totalCount'),
      itemStyle: {
        color: 'green',
        borderRadius: [5, 5, 0, 0]
      },
      data: [0, 0],
      type: 'bar',
      barMaxWidth: '20',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};

// 交付工作量（完成量）
const deliveryWorkloadEchartConfig = JSON.parse(JSON.stringify({
  ...deliveryEchartConfig,
  title: {
    text: '0%',
    left: '35%',
    top: '45%',
    padding: 2,
    subtext: t('functionAnalysis.detail.recentDelivery.deliveryWorkload'),
    // left: '25%',
    // top: '40%',
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
  deliveryRefEchart = eCharts.init(deliveryRef.value);
  deliveryWorkloadEchart = eCharts.init(deliveryWorkloadRef.value);

  watch([() => props.chart0Value, () => props.chart1Value], () => {
    deliveryEchartConfig.series[0].data = props.chart0Value.yData0;
    deliveryEchartConfig.series[1].data = props.chart0Value.yData1;
    deliveryEchartConfig.series[2].data = props.chart0Value.yData2;

    deliveryWorkloadEchartConfig.series[0].data = props.chart1Value.yData0;
    deliveryWorkloadEchartConfig.series[1].data = props.chart1Value.yData1;
    deliveryWorkloadEchartConfig.series[2].data = props.chart1Value.yData2;

    deliveryWorkloadEchart.setOption(deliveryWorkloadEchartConfig);
    deliveryRefEchart.setOption(deliveryEchartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    deliveryWorkloadEchart.resize();
    deliveryRefEchart.resize();
  }
});

</script>
<template>
  <div class="flex">
    <div ref="deliveryRef" class="flex-1 h-40"></div>
    <div ref="deliveryWorkloadRef" class="flex-1 h-40"></div>
  </div>
</template>
<style scoped>
.risk-level-LOW {
  color: 'gold'
}

.risk-level-HIGH {
  color: 'red'
}

.risk-level-NONE {
  color: '#52C41A'
}
</style>
