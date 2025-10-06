<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

const { t } = useI18n();

interface Props {
  overdueAssessmentData: Record<string, any>;
  chart0Value: {
    yData: number[]
  },
  chart1Value: {
    yData: number[]
  },
}
const props = withDefaults(defineProps<Props>(), {
  overdueAssessmentData: () => ({}),
  chart0Value: () => ({
    yData: [0, 0]
  }),
  chart1Value: () => ({
    yData: [0, 0]
  })
});

const unplannedTaskRef = ref();
const unplannedWorkloadRef = ref();

let unplannedTaskRefEChart;
let unplannedWorkloadRefEChart;

// 用例数
const unplannedTestEChartConfig = {
  title: {
    text: t('common.count'),
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
      t('common.counts.totalCount'),
      t('testAnalysis.detail.unplanned.chartLabels.unplannedCount'),
      t('testAnalysis.detail.unplanned.chartLabels.unplannedCompletedCount')
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
        color: 'rgb(68,93,179)',
        borderRadius: [5, 5, 0, 0]
      },
      barGap: 0,
      data: [0, 0, 0, 0, 0, 0, 0, 0],
      type: 'bar',
      barMaxWidth: '30',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};

const unplannedWorkloadEChartConfig = JSON.parse(JSON.stringify({
  ...unplannedTestEChartConfig,
  xAxis: {
    type: 'category',
    data: [
      t('testAnalysis.detail.unplanned.chartLabels.totalWorkload'),
      t('testAnalysis.detail.unplanned.chartLabels.unplannedWorkload'),
      t('testAnalysis.detail.unplanned.chartLabels.unplannedCompletedWorkload')
    ],
    axisLabel: {
      interval: 0,
      overflow: 'break'
    }
  },
  title: {
    text: t('common.workload'),
    bottom: 0,
    left: 'center',
    textStyle: {
      fontSize: 12
    }
  }
}));

onMounted(() => {
  unplannedTaskRefEChart = eCharts.init(unplannedTaskRef.value);

  unplannedWorkloadRefEChart = eCharts.init(unplannedWorkloadRef.value);

  watch([() => props.chart0Value, () => props.chart1Value], () => {
    unplannedTestEChartConfig.series[0].data = props.chart0Value.yData;
    unplannedWorkloadEChartConfig.series[0].data = props.chart1Value.yData;

    unplannedTaskRefEChart.setOption(unplannedTestEChartConfig);
    unplannedWorkloadRefEChart.setOption(unplannedWorkloadEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    unplannedTaskRefEChart.resize();
    unplannedWorkloadRefEChart.resize();
  }
});

</script>
<template>
  <div class="flex chart-container">
    <div class="px-3 w-50 flex items-center">
      <div class="text-center flex-1">
        <div class="font-semibold ">
          <span class="text-5 text-status-pending">
            {{ props.overdueAssessmentData.unplannedWorkloadProcessingTime || 0 }}
          </span>{{ t('unit.hour') }}
        </div>
        <div class="metric-subtitle">
          {{ t('testAnalysis.detail.unplanned.metrics.estimatedTime') }}
        </div>
      </div>
    </div>
    <div ref="unplannedTaskRef" class="flex-1 h-40"></div>
    <div ref="unplannedWorkloadRef" class="flex-1 h-40"></div>
  </div>
</template>
<style scoped>
.chart-container {
  padding: 20px;
}

.metric-subtitle {
  color: #9e9c9c;
}
</style>
