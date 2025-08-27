<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

const { t } = useI18n();

interface Props {
  // title0: string;
  // title1: string;
  // value0: {name: string, value: string|number}[];
  // value1: {name: string, value: string|number}[];

  overdueAssessmentData: Record<string, any>;
  chart0Value: {
    yData: number[]
  },
}
const props = withDefaults(defineProps<Props>(), {
  overdueAssessmentData: () => ({}),
  chart0Value: () => ({
    yData: [0, 0]
  })
});

const backlogRef = ref();

let backlogRefEchart;

// 用例交付周期(小时
const backlogEchartConfig = {
  title: {
    text: t('functionAnalysis.detail.leadTime.caseDeliveryCycle'),
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
    data: [t('functionAnalysis.detail.leadTime.average'), t('functionAnalysis.detail.leadTime.minimum'), t('functionAnalysis.detail.leadTime.maximum'), t('functionAnalysis.detail.leadTime.p50'), t('functionAnalysis.detail.leadTime.p75'), t('functionAnalysis.detail.leadTime.p90'), t('functionAnalysis.detail.leadTime.p95'), t('functionAnalysis.detail.leadTime.p99')],
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
  backlogRefEchart = eCharts.init(backlogRef.value);

  watch([() => props.chart0Value], () => {
    backlogEchartConfig.series[0].data = props.chart0Value.yData;

    backlogRefEchart.setOption(backlogEchartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    backlogRefEchart.resize();
  }
});

</script>
<template>
  <div class="flex">
    <div class="px-3 w-100">
      <div class="flex justify-around">
        <div class="text-center flex-1">
          <div class="font-semibold "><span class="text-5 text-status-warn">{{ props.overdueAssessmentData.totalProcessingTime || 0 }}</span>{{ t('functionAnalysis.detail.leadTime.hours') }}</div>
          <div>
            {{ t('functionAnalysis.detail.leadTime.totalProcessingTime') }}
          </div>
        </div>
      </div>
      <div class="flex justify-around mt-3">
        <div class="text-center">
          <div class="font-semibold text-5">{{ props.overdueAssessmentData.userNum || 0 }}</div>
          <div>
            {{ t('functionAnalysis.detail.leadTime.participants') }}
          </div>
        </div>
        <div class="text-center">
          <div class="">
            <span class="font-semibold text-5">
              {{ props.overdueAssessmentData.userAvgProcessingTime || 0 }}
            </span>{{ t('functionAnalysis.detail.leadTime.perHour') }}
          </div>
          <div>
            {{ t('functionAnalysis.detail.leadTime.dailyAverageProcessingTime') }}
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
