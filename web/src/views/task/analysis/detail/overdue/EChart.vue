<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

interface Props {
  overdueAssessmentData: Record<string, any>;

  chart1Value: {
    title: string;
    value: {name: string, value: string|number}[];
  }
  chart2Value: {
    title: string;
    value: {name: string, value: string|number}[];
  }
}
const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  overdueAssessmentData: () => ({}),
  chart1Value: () => ({
    title: '',
    value: [{ name: '', value: 0 }, { name: '', value: 0 }]
  }),
  chart2Value: () => ({
    title: '',
    value: [{ name: '', value: 0 }, { name: '', value: 0 }]
  })
});

const completedWorkloadRef = ref();
const savingWorkloadRef = ref();

let completedWorkloadEChart;
let savingWorkloadEChart;

const completedWorkloadEChartConfig = {
  title: {
    text: '0%',
    left: '35%',
    top: '52%',
    padding: 2,
    subtext: t('taskAnalysis.detail.overdueAssessment.chartTitles.completedWorkloadRatio'),
    itemGap: 40,
    textAlign: 'center',
    textStyle: {
      fontSize: 12,
      fontWeight: 'bolder'
    },
    subtextStyle: {
      fontSize: 12,
      color: '#000'
    }
  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    right: '10',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 2
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: '65%',
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      itemStyle: {
        borderRadius: 2,
        borderColor: '#fff',
        borderWidth: 1
      },
      emphasis: {
        label: {
          show: true
        }
      },
      labelLine: {
        show: true,
        length: 5
      },
      data: [
        {
          name: t('status.notCompleted'),
          value: 0,
          itemStyle: {
            color: 'rgb(117,246,42)'
          }
        },
        {
          name: t('status.completed'),
          value: 0,
          itemStyle: {
            color: 'rgb(205,115,120)'
          }
        }
      ]
    }
  ]
};

const savingWorkloadEChartConfig = JSON.parse(JSON.stringify({
  ...completedWorkloadEChartConfig,
  title: {
    ...completedWorkloadEChartConfig.title,
    subtext: t('taskAnalysis.detail.overdueAssessment.chartTitles.savingWorkloadRatio')
  }
}));

onMounted(() => {
  completedWorkloadEChart = eCharts.init(completedWorkloadRef.value);
  savingWorkloadEChart = eCharts.init(savingWorkloadRef.value);

  watch([() => props.chart1Value, () => props.chart2Value], () => {
    completedWorkloadEChartConfig.series[0].data[0] = {
      ...completedWorkloadEChartConfig.series[0].data[0],
      ...props.chart1Value.value[0],
      value: Number(props.chart1Value.value[0].value)
    };
    completedWorkloadEChartConfig.series[0].data[1] = {
      ...completedWorkloadEChartConfig.series[0].data[1],
      ...props.chart1Value.value[1],
      value: Number(props.chart1Value.value[1].value)
    };
    completedWorkloadEChartConfig.title.text = props.chart1Value.title;

    savingWorkloadEChartConfig.series[0].data[0] = {
      ...savingWorkloadEChartConfig.series[0].data[0],
      ...props.chart2Value.value[0],
      value: Number(props.chart2Value.value[0].value)
    };
    savingWorkloadEChartConfig.series[0].data[1] = {
      ...savingWorkloadEChartConfig.series[0].data[1],
      ...props.chart2Value.value[1],
      value: Number(props.chart2Value.value[1].value)
    };
    savingWorkloadEChartConfig.title.text = props.chart2Value.title;
    completedWorkloadEChart.setOption(completedWorkloadEChartConfig);
    savingWorkloadEChart.setOption(savingWorkloadEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    completedWorkloadEChart.resize();
    savingWorkloadEChart.resize();
  }
});
</script>
<template>
  <div class="flex">
    <div class="px-3 w-130">
      <div class="flex justify-around">
        <div class="text-center flex-1">
          <div class="font-semibold text-5 text-status-error">
            {{ props.overdueAssessmentData.overdueNum || 0 }}
          </div>
          <div>
            {{ t('taskAnalysis.detail.overdueAssessment.statistics.overdueCount') }}
          </div>
        </div>
        <div class="text-center flex-1">
          <div
            :class="`risk-level-${props.overdueAssessmentData?.riskLevel?.value}`"
            class="font-semibold text-5">
            {{ overdueAssessmentData?.riskLevel?.message }}
          </div>
          <div>{{ t('taskAnalysis.detail.overdueAssessment.statistics.overdueRisk') }}</div>
        </div>
      </div>
      <div class="flex justify-around mt-3">
        <div class="text-center">
          <div class="font-semibold text-5  text-status-error">
            {{ props.overdueAssessmentData.overdueTime || 0 }}
            {{ t('taskAnalysis.detail.overdueAssessment.statistics.hours') }}
          </div>
          <div>
            {{ t('taskAnalysis.detail.overdueAssessment.statistics.overdueTime') }}
          </div>
        </div>

        <div class="text-center">
          <div class="font-semibold text-5">
            {{ props.overdueAssessmentData.dailyProcessedWorkload || 0 }}
          </div>
          <div>
            {{ t('taskAnalysis.detail.overdueAssessment.statistics.averageDailyProcessedWorkload') }}
          </div>
        </div>

        <div class="text-center">
          <div class="font-semibold text-5">
            {{ props.overdueAssessmentData.overdueWorkloadProcessingTime || 0 }}
            {{ t('taskAnalysis.detail.overdueAssessment.statistics.hours') }}
          </div>
          <div>
            {{ t('taskAnalysis.detail.overdueAssessment.statistics.overdueWorkloadProcessingTime') }}
          </div>
        </div>
      </div>
    </div>
    <div ref="completedWorkloadRef" class="flex-1 h-35"></div>
    <div ref="savingWorkloadRef" class="flex-1 h-35"></div>
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
