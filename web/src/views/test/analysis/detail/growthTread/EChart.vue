<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

const { t } = useI18n();

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
const props = withDefaults(defineProps<Props>(), {
  overdueAssessmentData: () => ({}),
  chart0Value: () => ({
    yData: [0, 0, 0, 0, 0, 0, 0]
  }),
  chart1Value: () => ({
    value: []
  })
});

const taskGrowthTreadRef = ref();

let taskGrowthTreadRefEchart;

const taskGrowthTreadEchartConfig = {
  title: {
    text: t('testAnalysis.detail.growthTread.growthTrend'),
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
  taskGrowthTreadRefEchart = eCharts.init(taskGrowthTreadRef.value);

  watch([() => props.chart0Value, () => props.chart1Value], () => {
    taskGrowthTreadEchartConfig.series = props.chart1Value.value.map(i => {
      return {
        ...i,
        stack: 'Total'
      };
    });
    taskGrowthTreadEchartConfig.xAxis.data = props.chart1Value.xData;

    taskGrowthTreadRefEchart.setOption(taskGrowthTreadEchartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    taskGrowthTreadRefEchart.resize();
  }
});

</script>
<template>
  <div class="flex">
    <div ref="taskGrowthTreadRef" class="flex-1 h-40"></div>
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
