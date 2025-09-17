<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

const { t } = useI18n();

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
const props = withDefaults(defineProps<Props>(), {
  overdueAssessmentData: () => ({}),
  chart1Value: () => ({
    title: '',
    value: [{ name: '', vaue: 0 }, { name: '', vaue: 0 }]
  }),
  chart2Value: () => ({
    title: '',
    value: [{ name: '', vaue: 0 }, { name: '', vaue: 0 }]
  })
});

const completedWorkloadRef = ref();
const savingWorkloadRef = ref();

let completedWorkloadEchart;
let savingWorkloadEchart;

const completedWorkloadEchartConfig = {
  title: {
    text: '0%',
    left: '35%',
    top: '40%',
    padding: 2,
    subtext: t('functionAnalysis.detail.overdueAssessment.completedWorkloadPercentage'),
    itemGap: 50,
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
      // radius: ['50%', '70%'],
      radius: '70%',
      center: ['35%', '45%'],
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
          name: t('functionAnalysis.detail.overdueAssessment.notCompleted'),
          value: 0,
          itemStyle: {
            color: 'rgb(246,159,42)'
          }
        },
        {
          name: t('functionAnalysis.detail.overdueAssessment.completed'),
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 1)'
          }
        }
      ]
    }
  ]
};

const savingWorkloadEchartConfig = JSON.parse(JSON.stringify({
  ...completedWorkloadEchartConfig,
  title: {
    ...completedWorkloadEchartConfig.title,
    subtext: t('functionAnalysis.detail.overdueAssessment.savedWorkloadPercentage')
  }
}));

onMounted(() => {
  completedWorkloadEchart = eCharts.init(completedWorkloadRef.value);

  savingWorkloadEchart = eCharts.init(savingWorkloadRef.value);

  watch([() => props.chart0Value, () => props.chart1Value, () => props.chart2Value], () => {
    completedWorkloadEchartConfig.series[0].data[0] = {
      ...completedWorkloadEchartConfig.series[0].data[0],
      ...props.chart1Value.value[0]
    };
    completedWorkloadEchartConfig.series[0].data[1] = {
      ...completedWorkloadEchartConfig.series[0].data[1],
      ...props.chart1Value.value[1]
    };
    completedWorkloadEchartConfig.title.text = props.chart1Value.title;

    savingWorkloadEchartConfig.series[0].data[0] = {
      ...savingWorkloadEchartConfig.series[0].data[0],
      ...props.chart2Value.value[0]
    };
    savingWorkloadEchartConfig.series[0].data[1] = {
      ...savingWorkloadEchartConfig.series[0].data[1],
      ...props.chart2Value.value[1]
    };
    savingWorkloadEchartConfig.title.text = props.chart2Value.title;
    completedWorkloadEchart.setOption(completedWorkloadEchartConfig);
    savingWorkloadEchart.setOption(savingWorkloadEchartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    completedWorkloadEchart.resize();
    savingWorkloadEchart.resize();
  }
});

</script>
<template>
  <div class="flex">
    <div class="px-3 w-100">
      <div class="flex justify-around">
        <div class="text-center flex-1">
          <div class="font-semibold text-5 text-status-error">
            {{ props.overdueAssessmentData.overdueNum || 0 }}
          </div>
          <div>
            {{ t('functionAnalysis.detail.overdueAssessment.overdueCount') }}
          </div>
        </div>
        <div class="text-center flex-1">
          <div :class="`risk-level-${props.overdueAssessmentData?.riskLevel?.value}`" class="font-semibold text-5">
            {{ overdueAssessmentData?.riskLevel?.message }}
          </div>
          <div>{{ t('functionAnalysis.detail.overdueAssessment.overdueRisk') }}</div>
        </div>
      </div>
      <div class="flex justify-around mt-3">
        <div class="text-center">
          <div class="font-semibold text-5  text-status-error">
            {{ props.overdueAssessmentData.overdueTime || 0 }}{{ t('functionAnalysis.detail.overdueAssessment.hours') }}
          </div>
          <div>
            {{ t('functionAnalysis.detail.overdueAssessment.overdueTime') }}
          </div>
        </div>

        <div class="text-center">
          <div class="font-semibold text-5">{{ props.overdueAssessmentData.dailyProcessedWorkload || 0 }}</div>
          <div>
            {{ t('functionAnalysis.detail.overdueAssessment.dailyAverageProcessedWorkload') }}
          </div>
        </div>

        <div class="text-center">
          <div class="font-semibold text-5">
            {{ props.overdueAssessmentData.overdueWorkloadProcessingTime || 0 }}{{ t('functionAnalysis.detail.overdueAssessment.hours') }}
          </div>
          <div>
            {{ t('functionAnalysis.detail.overdueAssessment.overdueWorkloadEstimatedProcessingTime') }}
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
