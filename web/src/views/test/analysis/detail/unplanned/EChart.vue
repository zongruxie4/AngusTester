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

let unplannedTaskRefEchart;
let unplannedWorkloadRefEchart;

// 用例数
const unplannedTestEchartConfig = {
  title: {
    text: t('common.counts.caseCount'),
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
      t('testAnalysis.detail.unplanned.totalCount'),
      t('testAnalysis.detail.unplanned.unplannedCount'),
      t('testAnalysis.detail.unplanned.unplannedCompletedCount')
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

const unplannedWorkloadEchartConfig = JSON.parse(JSON.stringify({
  ...unplannedTestEchartConfig,
  xAxis: {
    type: 'category',
    data: [
      t('testAnalysis.detail.unplanned.totalWorkload'),
      t('testAnalysis.detail.unplanned.unplannedWorkload'),
      t('testAnalysis.detail.unplanned.unplannedCompletedWorkload')
    ],
    axisLabel: {
      interval: 0,
      overflow: 'break'
    }
  },
  title: {
    text: t('testAnalysis.detail.unplanned.workload'),
    bottom: 0,
    left: 'center',
    textStyle: {
      fontSize: 12
    }
  }
}));

onMounted(() => {
  unplannedTaskRefEchart = eCharts.init(unplannedTaskRef.value);

  unplannedWorkloadRefEchart = eCharts.init(unplannedWorkloadRef.value);

  watch([() => props.chart0Value, () => props.chart1Value], () => {
    unplannedTestEchartConfig.series[0].data = props.chart0Value.yData;
    unplannedWorkloadEchartConfig.series[0].data = props.chart1Value.yData;

    unplannedTaskRefEchart.setOption(unplannedTestEchartConfig);
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
    <div class="px-3 w-50 flex items-center">
      <div class="text-center flex-1">
        <div class="font-semibold ">
          <span class="text-5 text-status-pending">
            {{ props.overdueAssessmentData.unplannedWorkloadProcessingTime || 0 }}
          </span>
          {{ t('testAnalysis.detail.unplanned.hours') }}
        </div>
        <div>
          {{ t('testAnalysis.detail.unplanned.unplannedCaseEstimatedTime') }}
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
