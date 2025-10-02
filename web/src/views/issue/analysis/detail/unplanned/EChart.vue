<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

interface Props {
  overdueAssessmentData: Record<string, any>;
  chart0Value: {
    yData: number[]
  },
  chart1Value: {
    yData: number[]
  },
}
const { t } = useI18n();

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

// 任务数
const unplannedTaskEChartConfig = {
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
      t('taskAnalysis.detail.unplannedTasks.chartLabels.totalCount'),
      t('taskAnalysis.detail.unplannedTasks.chartLabels.unplannedCount'),
      t('taskAnalysis.detail.unplannedTasks.chartLabels.unplannedCompletedCount')
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
      data: [0, 0, 0, 0, 0, 0, 0, 0],
      type: 'bar',
      barMaxWidth: '20',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};

const unplannedWorkloadEChartConfig = JSON.parse(JSON.stringify({
  ...unplannedTaskEChartConfig,
  xAxis: {
    type: 'category',
    data: [
      t('taskAnalysis.detail.unplannedTasks.chartLabels.totalWorkload'),
      t('taskAnalysis.detail.unplannedTasks.chartLabels.unplannedWorkload'),
      t('taskAnalysis.detail.unplannedTasks.chartLabels.unplannedCompletedWorkload')
    ],
    axisLabel: {
      interval: 0,
      overflow: 'break'
    }
  },
  title: {
    text: t('taskAnalysis.detail.unplannedTasks.chartTitles.workload'),
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
    unplannedTaskEChartConfig.series[0].data = props.chart0Value.yData;
    unplannedWorkloadEChartConfig.series[0].data = props.chart1Value.yData;

    unplannedTaskRefEChart.setOption(unplannedTaskEChartConfig);
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
  <div class="flex">
    <div class="px-3 w-50 flex items-center">
      <div class="text-center flex-1">
        <div class="font-semibold ">
          <span class="text-5 text-status-pending">
            {{ props.overdueAssessmentData.unplannedWorkloadProcessingTime || 0 }}
          </span>{{ t('taskAnalysis.detail.unplannedTasks.metrics.hours') }}
        </div>
        <div>
          {{ t('taskAnalysis.detail.unplannedTasks.metrics.estimatedTime') }}
        </div>
      </div>
    </div>
    <div ref="unplannedTaskRef" class="flex-1 h-40"></div>
    <div ref="unplannedWorkloadRef" class="flex-1 h-40"></div>
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
