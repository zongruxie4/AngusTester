<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

interface Props {
  overdueAssessmentData: Record<string, any>;
  chart0Value: {
    yData: number[]
  },
}
const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  overdueAssessmentData: () => ({}),
  chart0Value: () => ({
    yData: [0, 0, 0, 0, 0, 0, 0, 0]
  })
});

const backlogRef = ref();

let backlogRefEChart;

// 任务交付周期(小时
const backlogEChartConfig = {
  title: {
    text: t('taskAnalysis.detail.leadTime.chartTitles.taskDeliveryCycle'),
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
    data: [t('taskAnalysis.detail.leadTime.chartLabels.average'),
      t('taskAnalysis.detail.leadTime.chartLabels.min'),
      t('taskAnalysis.detail.leadTime.chartLabels.max'),
      t('taskAnalysis.detail.leadTime.chartLabels.p50'),
      t('taskAnalysis.detail.leadTime.chartLabels.p75'),
      t('taskAnalysis.detail.leadTime.chartLabels.p90'),
      t('taskAnalysis.detail.leadTime.chartLabels.p95'),
      t('taskAnalysis.detail.leadTime.chartLabels.p99')],
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

onMounted(() => {
  backlogRefEChart = eCharts.init(backlogRef.value);

  watch([() => props.chart0Value], () => {
    backlogEChartConfig.series[0].data = props.chart0Value.yData;

    backlogRefEChart.setOption(backlogEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    backlogRefEChart.resize();
  }
});

</script>
<template>
  <div class="flex">
    <div class="px-3 w-100">
      <div class="flex justify-around">
        <div class="text-center flex-1">
          <div class="font-semibold ">
            <span class="text-5 text-status-warn">
              {{ props.overdueAssessmentData.totalProcessingTime || 0 }}
            </span>
            {{ t('taskAnalysis.detail.leadTime.statistics.hours') }}
          </div>
          <div>
            {{ t('taskAnalysis.detail.leadTime.statistics.totalProcessingTime') }}
          </div>
        </div>
      </div>
      <div class="flex justify-around mt-3">
        <div class="text-center">
          <div class="font-semibold text-5">{{ props.overdueAssessmentData.userNum || 0 }}</div>
          <div>
            {{ t('taskAnalysis.detail.leadTime.statistics.participants') }}
          </div>
        </div>
        <div class="text-center">
          <div class="">
            <span class="font-semibold text-5">
              {{ props.overdueAssessmentData.userAvgProcessingTime || 0 }}
            </span>{{ t('taskAnalysis.detail.leadTime.statistics.perHour') }}
          </div>
          <div>
            {{ t('taskAnalysis.detail.leadTime.statistics.averageDailyProcessingTime') }}
          </div>
        </div>
      </div>
    </div>
    <div ref="backlogRef" class="flex-1 h-40"></div>
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
