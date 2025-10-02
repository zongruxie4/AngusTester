<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

interface Props {
  overdueAssessmentData: Record<string, any>;
  chart0Value: {
    yData: number[],

  },
  chart1Value: {
    value: {name: string, data: number[]}[],
    xData: string[]
  },
}
const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  overdueAssessmentData: () => ({}),
  chart0Value: () => ({
    yData: [0, 0, 0, 0, 0, 0, 0]
  }),
  chart1Value: () => ({
    value: [],
    xData: []
  })
});

const growthTreadTaskRef = ref();
const growthTreadWorkloadRef = ref();

let growthTreadTaskRefEChart;
let growthTreadWorkloadRefEChart;

// 任务数
const growthTreadTaskEChartConfig = {
  title: {
    text: t('issueAnalysis.detail.growthTread.chartTitles.issueGrowth'),
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
      t('common.counts.requirementCount'),
      t('common.counts.storyCount'),
      t('common.counts.taskCount'),
      t('common.counts.bugCount'),
      t('common.counts.apiTestCount'),
      t('common.counts.scenarioTestCount'),
      t('chart.total')
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
    top: 0
  },
  series: [
    {
      name: '',
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)',
        borderRadius: [5, 5, 0, 0]
      },
      barGap: 0,
      data: [0, 0, 0, 0, 0, 0, 0],
      type: 'bar',
      barMaxWidth: '20',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};

const growthTreadWorkloadEChartConfig: any = {
  title: {
    text: t('issueAnalysis.detail.growthTread.chartTitles.growthTrend'),
    bottom: 0,
    left: 'center',
    textStyle: {
      fontStyle: 12
    }
  },
  grid: {
    left: '5%',
    right: '100',
    bottom: '60',
    top: 20
  },
  legend: {
    show: true,
    right: 0,
    type: 'plain',
    orient: 'vertical'
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
      name: 'api',
      data: [],
      type: 'line',
      smooth: true
    }
  ]
};

onMounted(() => {
  growthTreadTaskRefEChart = eCharts.init(growthTreadTaskRef.value);
  growthTreadWorkloadRefEChart = eCharts.init(growthTreadWorkloadRef.value);

  watch([() => props.chart0Value, () => props.chart1Value], () => {
    growthTreadTaskEChartConfig.series[0].data = props.chart0Value.yData;
    growthTreadWorkloadEChartConfig.series = props.chart1Value.value.map(i => {
      return {
        ...i,
        data: i.data,
        type: 'line',
        smooth: true,
        stack: 'Total'
      };
    });
    growthTreadWorkloadEChartConfig.xAxis.data = props.chart1Value.xData;

    growthTreadTaskRefEChart.setOption(growthTreadTaskEChartConfig);
    growthTreadWorkloadRefEChart.setOption(growthTreadWorkloadEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    growthTreadTaskRefEChart.resize();
    growthTreadWorkloadRefEChart.resize();
  }
});

</script>
<template>
  <div class="flex">
    <div ref="growthTreadTaskRef" class="flex-1 h-45"></div>
    <div ref="growthTreadWorkloadRef" class="flex-1 h-45"></div>
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
