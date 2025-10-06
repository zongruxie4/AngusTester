<script lang="ts" setup>
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

const { t } = useI18n();

interface Props {
  overdueAssessmentData: Record<string, any>;
  chart0Value: {
    yData0: number[],
    yData1: number[]
  },

  chart1Value: {
    title: string;
    value: { name: string, value: string | number }[];
  }
  chart2Value: {
    title: string;
    value: { name: string, value: string | number }[];
  }
}

const props = withDefaults(defineProps<Props>(), {
  overdueAssessmentData: () => ({}),
  chart0Value: () => ({
    yData0: [0, 0],
    yData1: [0, 0]
  }),
  chart1Value: () => ({
    title: '',
    value: [{ name: '', value: 0 }, { name: '', value: 0 }]
  }),
  chart2Value: () => ({
    title: '',
    value: [{ name: '', value: 0 }, { name: '', value: 0 }]
  })
});

const backlogRef = ref();
const backloggedTaskRef = ref();
const backloggedWorkloadRef = ref();

let backlogRefEChart;
let backloggedTaskEChart;
let backloggedWorkloadEChart;

// 积压量（对齐测试 Backlog 柱图样式）
const backlogEChartConfig = {
  title: {
    text: t('issueAnalysis.detail.backlogIssues.chartLabels.backloggedCount'),
    left: '50%',
    bottom: '2%',
    textAlign: 'center',
    textStyle: { fontSize: 12, fontWeight: '600', color: '#595959' }
  },
  grid: { left: '10%', right: '10%', bottom: '30%', top: '15%' },
  xAxis: {
    type: 'category',
    data: [t('common.count'), t('common.workload')],
    axisLabel: { interval: 0, overflow: 'break', fontSize: 12, color: '#666' },
    axisLine: { lineStyle: { color: '#e8e8e8' } }
  },
  yAxis: [{
    type: 'value',
    axisLabel: { fontSize: 12, color: '#666' },
    splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }
  }],
  tooltip: {
    show: true,
    backgroundColor: 'rgba(0, 0, 0, 0.8)',
    borderColor: 'transparent',
    textStyle: { color: '#fff', fontSize: 12 }
  },
  legend: {
    show: true,
    data: [
      t('issueAnalysis.detail.backlogIssues.chartLabels.backloggedCount'),
      t('issueAnalysis.detail.backlogIssues.chartLabels.totalBackloggedCount')
    ],
    top: 0,
    textStyle: { fontSize: 12, color: '#595959' }
  },
  series: [
    {
      name: t('issueAnalysis.detail.backlogIssues.chartLabels.backloggedCount'),
      itemStyle: { color: '#faad14', borderRadius: [4, 4, 0, 0] },
      barGap: 0,
      data: [0, 0],
      type: 'bar',
      barMaxWidth: '36%',
      label: { show: true, position: 'top', fontSize: 12, color: '#666' }
    },
    {
      name: t('issueAnalysis.detail.backlogIssues.chartLabels.totalBackloggedCount'),
      itemStyle: { color: 'rgb(68,93,179)', borderRadius: [4, 4, 0, 0] },
      data: [0, 0],
      type: 'bar',
      barMaxWidth: '36%',
      label: { show: true, position: 'top', fontSize: 12, color: '#666' }
    }
  ]
};
// 积压任务数（对齐测试 Backlog 饼图样式）
const backloggedTaskEChartConfig = {
  title: {
    text: t('issueAnalysis.detail.backlogIssues.chartLabels.backlogIssueRate'),
    left: '35%',
    bottom: '6%',
    textAlign: 'center',
    textStyle: { fontSize: 12, fontWeight: '600', color: '#595959' }
  },
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(0, 0, 0, 0.8)',
    borderColor: 'transparent',
    textStyle: { color: '#fff', fontSize: 12 },
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    top: 'center',
    right: '2px',
    orient: 'vertical',
    itemGap: 4,
    textStyle: { fontSize: 12, color: '#595959' }
  },
  series: [{
    name: '',
    type: 'pie',
    radius: ['30%', '60%'],
    center: ['35%', '50%'],
    avoidLabelOverlap: true,
    label: {
      show: true,
      position: 'center',
      formatter: function () {
        const title = props.chart1Value?.title || '0.0%';
        return '{a|' + title + '}';
      },
      rich: { a: { fontSize: 14, fontWeight: 'bold', color: '#262626' } }
    },
    itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
    emphasis: {
      scale: true,
      scaleSize: 5,
      itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.3)' }
    },
    data: [
      { name: t('status.notCompleted'), value: 0, itemStyle: { color: '#ff7875' } },
      { name: t('status.completed'), value: 0, itemStyle: { color: '#52C41A' } }
    ]
  }]
};

const backloggedWorkloadEChartConfig = JSON.parse(JSON.stringify({
  ...backloggedTaskEChartConfig,
  title: {
    ...backloggedTaskEChartConfig.title,
    text: t('issueAnalysis.detail.backlogIssues.chartLabels.backlogWorkloadRate'),
    left: '35%',
    bottom: '6%'
  },
  legend: { top: 'center', right: '2px', orient: 'vertical', itemGap: 4, textStyle: { fontSize: 12, color: '#595959' } },
  series: [{
    ...backloggedTaskEChartConfig.series[0],
    radius: ['30%', '60%'],
    center: ['35%', '50%'],
    label: {
      ...backloggedTaskEChartConfig.series[0].label,
      formatter: function () {
        const title = props.chart2Value?.title || '0.0%';
        return '{a|' + title + '}';
      }
    }
  }]
}));

onMounted(() => {
  backlogRefEChart = eCharts.init(backlogRef.value);
  backloggedTaskEChart = eCharts.init(backloggedTaskRef.value);

  backloggedWorkloadEChart = eCharts.init(backloggedWorkloadRef.value);

  const handleResize = () => {
    const isMobile = window.innerWidth < 768;
    backlogEChartConfig.series[0].barMaxWidth = isMobile ? '20%' : '28%';
    backlogEChartConfig.series[1].barMaxWidth = isMobile ? '20%' : '28%';
    backloggedTaskEChart?.resize();
    backloggedWorkloadEChart?.resize();
    backlogRefEChart?.resize();
  };

  window.addEventListener('resize', handleResize);

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
  });

  watch([() => props.chart0Value, () => props.chart1Value, () => props.chart2Value], () => {
    backlogEChartConfig.series[0].data = props.chart0Value.yData0;
    backlogEChartConfig.series[1].data = props.chart0Value.yData1;
    const isMobile = window.innerWidth < 768;
    backlogEChartConfig.series[0].barMaxWidth = isMobile ? '20%' : '28%';
    backlogEChartConfig.series[1].barMaxWidth = isMobile ? '20%' : '28%';

    backloggedTaskEChartConfig.series[0].data[0] = {
      ...backloggedTaskEChartConfig.series[0].data[0],
      ...props.chart1Value.value[0],
      value: Number(props.chart1Value.value[0].value)
    };
    backloggedTaskEChartConfig.series[0].data[1] = {
      ...backloggedTaskEChartConfig.series[0].data[1],
      ...props.chart1Value.value[1],
      value: Number(props.chart1Value.value[1].value)
    };
    backloggedTaskEChartConfig.series[0].label.formatter = function () {
      const title = props.chart1Value?.title || '0.0%';
      return '{a|' + title + '}';
    };

    backloggedWorkloadEChartConfig.series[0].data[0] = {
      ...backloggedWorkloadEChartConfig.series[0].data[0],
      ...props.chart2Value.value[0],
      value: Number(props.chart2Value.value[0].value)
    };
    backloggedWorkloadEChartConfig.series[0].data[1] = {
      ...backloggedWorkloadEChartConfig.series[0].data[1],
      ...props.chart2Value.value[1],
      value: Number(props.chart2Value.value[1].value)
    };
    backloggedWorkloadEChartConfig.series[0].label.formatter = function () {
      const title = props.chart2Value?.title || '0.0%';
      return '{a|' + title + '}';
    };
    backloggedTaskEChart.setOption(backloggedTaskEChartConfig);
    backloggedWorkloadEChart.setOption(backloggedWorkloadEChartConfig);
    backlogRefEChart.setOption(backlogEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    backloggedTaskEChart?.resize();
    backloggedWorkloadEChart?.resize();
    backlogRefEChart?.resize();
  }
});
</script>
<template>
  <div class="chart-container">
    <div class="main-layout">
      <div class="left-side">
        <div class="px-3 w-100">
          <div class="flex justify-around">
            <div class="text-center flex-1">
              <div class="font-semibold ">
                <span class="text-5 text-status-error">
                  {{ props.overdueAssessmentData.backloggedCompletionTime || 0 }}
                </span>
                {{ t('unit.hour') }}
              </div>
              <div class="metric-subtitle">
                {{ t('issueAnalysis.detail.backlogIssues.statistics.backlogWorkloadEstimatedTime') }}
              </div>
            </div>
          </div>
          <div class="flex justify-around mt-3">
            <div class="text-center">
              <div class="font-semibold text-5">{{ props.overdueAssessmentData.dailyProcessedNum || 0 }}</div>
              <div class="metric-subtitle">
                {{ t('issueAnalysis.detail.backlogIssues.statistics.averageDailyProcessedIssues') }}
              </div>
            </div>
            <div class="text-center">
              <div class="font-semibold text-5">{{ props.overdueAssessmentData.dailyProcessedWorkload || 0 }}</div>
              <div class="metric-subtitle">
                {{ t('issueAnalysis.detail.backlogIssues.statistics.averageDailyProcessedWorkload') }}
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="right-side">
        <div class="chart-row">
          <div ref="backlogRef" class="chart-item bar-chart"></div>
          <div ref="backloggedTaskRef" class="chart-item pie-chart"></div>
          <div ref="backloggedWorkloadRef" class="chart-item pie-chart"></div>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.chart-container {
  padding: 20px 20px 0px 0px;
  width: 100%;
  box-sizing: border-box;
}

.main-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
}

.right-side {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-left: 100px;
  min-width: 0;
}

.left-side {
  padding-top: 20px;
  width: 220px;
  box-sizing: border-box;
}

.metric-subtitle {
  color: #9e9c9c;
}

.chart-row {
  display: flex;
  gap: 16px;
  width: 100%;
  height: 200px;
}

.chart-item {
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
}

.bar-chart {
  flex: 1.5;
  padding-left: 50px;
}

.pie-chart {
  flex: 1;
  display: flex;
  align-items: center;
}

@media (max-width: 1200px) {
  .main-layout {
    flex-direction: column;
    gap: 16px;
  }

  .left-side {
    width: 100%;
  }

  .right-side {
    order: 1;
  }

  .chart-row {
    flex-wrap: wrap;
  }

  .chart-item {
    flex: 1 1 calc(50% - 8px);
    min-width: 200px;
  }

  .bar-chart {
    flex: 1 1 calc(100% - 8px);
  }
}

@media (max-width: 768px) {
  .chart-container {
    padding: 12px 12px 12px 4px;
  }

  .main-layout {
    gap: 12px;
  }

  .chart-row {
    height: 200px;
    flex-direction: column;
  }

  .chart-item {
    flex: 1;
    min-width: 100%;
  }
}

@media (max-width: 480px) {
  .chart-container {
    padding: 8px 8px 8px 2px;
  }

  .chart-row {
    height: 200px;
  }
}
</style>
