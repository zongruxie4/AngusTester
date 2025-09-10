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
    value: {name: string, value: number[]}[],
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
    value: []
  })
});

const unplannedTaskRef = ref();
const unplannedWorkloadRef = ref();

let unplannedTaskRefEchart;
let unplannedWorkloadRefEchart;

// 任务数
const unplannedTaskEchartConfig = {
  title: {
    text: t('taskAnalysis.detail.taskGrowthTread.chartTitles.taskGrowth'),
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
    data: [t('taskAnalysis.detail.taskGrowthTread.chartLabels.requirement'), t('taskAnalysis.detail.taskGrowthTread.chartLabels.story'), t('taskAnalysis.detail.taskGrowthTread.chartLabels.task'), t('taskAnalysis.detail.taskGrowthTread.chartLabels.bug'), t('taskAnalysis.detail.taskGrowthTread.chartLabels.apiTest'), t('taskAnalysis.detail.taskGrowthTread.chartLabels.scenarioTest'), t('taskAnalysis.detail.taskGrowthTread.chartLabels.total')],
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

const unplannedWorkloadEchartConfig = {
  title: {
    text: t('taskAnalysis.detail.taskGrowthTread.chartTitles.growthTrend'),
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
  unplannedTaskRefEchart = eCharts.init(unplannedTaskRef.value);

  unplannedWorkloadRefEchart = eCharts.init(unplannedWorkloadRef.value);

  watch([() => props.chart0Value, () => props.chart1Value], () => {
    unplannedTaskEchartConfig.series[0].data = props.chart0Value.yData;
    unplannedWorkloadEchartConfig.series = props.chart1Value.value.map(i => {
      return {
        ...i,
        stack: 'Total'
      };
    });
    unplannedWorkloadEchartConfig.xAxis.data = props.chart1Value.xData;

    unplannedTaskRefEchart.setOption(unplannedTaskEchartConfig);
    unplannedWorkloadRefEchart.setOption(unplannedWorkloadEchartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    unplannedTaskRefEchart.resize();
    unplannedWorkloadRefEchart.resize();
  }
});

</script>
<template>
  <div class="flex">
    <div ref="unplannedTaskRef" class="flex-1 h-45"></div>
    <div ref="unplannedWorkloadRef" class="flex-1 h-45"></div>
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
