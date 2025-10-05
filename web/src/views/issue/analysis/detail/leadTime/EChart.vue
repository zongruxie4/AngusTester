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

const leadTimeRef = ref();

let leadTimeRefEChart;

// 任务交付周期(小时
const leadTimeEChartConfig = {
  title: {
    text: t('issueAnalysis.detail.leadTime.chartTitles.deliveryCycle'),
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
      t('chart.average'),
      t('chart.min'),
      t('chart.max'),
      t('chart.p50'),
      t('chart.p75'),
      t('chart.p90'),
      t('chart.p95'),
      t('chart.p99')
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

onMounted(() => {
  leadTimeRefEChart = eCharts.init(leadTimeRef.value);

  watch([() => props.chart0Value], () => {
    leadTimeEChartConfig.series[0].data = props.chart0Value.yData;

    leadTimeRefEChart.setOption(leadTimeEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    leadTimeRefEChart.resize();
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
            {{ t('unit.hour') }}
          </div>
          <div>
            {{ t('issueAnalysis.detail.leadTime.statistics.totalProcessingTime') }}
          </div>
        </div>
      </div>
      <div class="flex justify-around mt-3">
        <div class="text-center">
          <div class="font-semibold text-5">{{ props.overdueAssessmentData.userNum || 0 }}</div>
          <div>
            {{ t('issueAnalysis.detail.leadTime.statistics.participants') }}
          </div>
        </div>
        <div class="text-center">
          <div class="">
            <span class="font-semibold text-5">
              {{ props.overdueAssessmentData.userAvgProcessingTime || 0 }}
            </span>{{ t('issueAnalysis.detail.leadTime.statistics.perHour') }}
          </div>
          <div>
            {{ t('issueAnalysis.detail.leadTime.statistics.averageDailyProcessingTime') }}
          </div>
        </div>
      </div>
    </div>
    <div ref="leadTimeRef" class="flex-1 h-40"></div>
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
