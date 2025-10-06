<script lang="ts" setup>
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as eCharts from 'echarts';

interface Props {
  chart0Value: {
    yData: number[]
  };
  chart1Value: {
    title: string;
    value: { name: string, value: string | number }[];
  }
  chart2Value: {
    title: string;
    value: { name: string, value: string | number }[];
  }
  chart3Value: {
    title: string;
    value: { name: string, value: string | number }[];
  }
  chart4Value: {
    title: string;
    value: { name: string, value: string | number }[];
  }
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  chart0Value: () => ({
    yData: [0, 0, 0]
  }),
  chart1Value: () => ({
    title: '',
    value: [{ name: '', value: 0 }, { name: '', value: 0 }]
  }),
  chart2Value: () => ({
    title: '',
    value: [{ name: '', value: 0 }, { name: '', value: 0 }]
  }),
  chart3Value: () => ({
    title: '',
    value: [{ name: '', value: 0 }, { name: '', value: 0 }]
  }),
  chart4Value: () => ({
    title: '',
    value: [{ name: '', value: 0 }, { name: '', value: 0 }]
  })
});

const completedRef = ref();
const oneTimePassedRef = ref();
const bugsRef = ref();
const twoTimePassedRef = ref();

let tasksChart;
let completedEChart;
let oneTimePassedEChart;
let twoTimePassedEChart;

// 完成任务
const taskEChartConfig = {
  title: {
    text: t('common.counts.completedCount'),
    left: '50%',
    bottom: '2%',
    textAlign: 'center',
    textStyle: {
      fontSize: 12,
      fontWeight: '600',
      color: '#595959'
    }
  },
  grid: {
    left: '10%',
    right: '10%',
    bottom: '30%',
    top: '15%'
  },
  xAxis: {
    type: 'category',
    data: [
      t('common.counts.completedCount'),
      t('issueAnalysis.detail.handlingEfficiency.chartLabels.oneTimeCompletedCount'),
      t('issueAnalysis.detail.handlingEfficiency.chartLabels.twoTimeCompletedCount')
    ],
    axisLabel: {
      interval: 0,
      overflow: 'break',
      fontSize: 12,
      color: '#666'
    },
    axisLine: {
      lineStyle: {
        color: '#e8e8e8'
      }
    }
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      fontSize: 12,
      color: '#666'
    },
    splitLine: {
      lineStyle: {
        type: 'dashed',
        color: '#f0f0f0'
      }
    }
  },
  tooltip: {
    show: true,
    backgroundColor: 'rgba(0, 0, 0, 0.8)',
    borderColor: 'transparent',
    textStyle: {
      color: '#fff',
      fontSize: 12
    }
  },
  series: [
    {
      itemStyle: {
        color: function (params) {
          const colors = ['#52c41a', '#faad14', '#1890ff'];
          return colors[params.dataIndex] || '#1890ff';
        },
        borderRadius: [4, 4, 0, 0]
      },
      data: [0, 0, 0],
      type: 'bar',
      barMaxWidth: '42%',
      label: {
        show: true,
        position: 'top',
        fontSize: 12,
        color: '#666'
      }
    }
  ]
};
// 完成任务占比
const completedEChartConfig = {
  title: {
    text: t('common.counts.completedRate'),
    left: '35%',
    bottom: '6%',
    textAlign: 'center',
    textStyle: {
      fontSize: 12,
      fontWeight: '600',
      color: '#595959'
    }
  },
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(0, 0, 0, 0.8)',
    borderColor: 'transparent',
    textStyle: {
      color: '#fff',
      fontSize: 12
    },
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    top: 'center',
    right: '2px',
    orient: 'vertical',
    itemGap: 4,
    textStyle: {
      fontSize: 12,
      color: '#595959'
    }
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['30%', '60%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        position: 'center',
        formatter: function () {
          return '{a|' + (props.chart1Value?.title || '0%') + '}';
        },
        rich: {
          a: {
            fontSize: 14,
            fontWeight: 'bold',
            color: '#262626'
          }
        }
      },
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      emphasis: {
        scale: true,
        scaleSize: 5,
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.3)'
        }
      },
      data: [
        {
          name: t('common.counts.uncompletedCount'),
          value: 0,
          itemStyle: {
            color: '#ff7875'
          }
        },
        {
          name: t('common.counts.completedCount'),
          value: 0,
          itemStyle: {
            color: '#52c41a'
          }
        }
      ]
    }
  ]
};

// 一次完成任务占比
const oneTimePassedEChartConfig = JSON.parse(JSON.stringify({
  ...completedEChartConfig,
  title: {
    ...completedEChartConfig.title,
    text: t('issueAnalysis.detail.handlingEfficiency.chartTitles.oneTimeCompletedRate'),
    left: '35%',
    bottom: '6%'
  },
  legend: {
    top: 'center',
    right: '2px',
    orient: 'vertical',
    itemGap: 4,
    textStyle: {
      fontSize: 12,
      color: '#595959'
    }
  },
  series: [{
    ...completedEChartConfig.series[0],
    radius: ['30%', '60%'],
    center: ['35%', '50%'],
    label: {
      show: true,
      position: 'center',
      formatter: function () {
        return '{a|' + (props.chart2Value?.title || '0%') + '}';
      },
      rich: {
        a: {
          fontSize: 14,
          fontWeight: 'bold',
          color: '#262626'
        }
      }
    }
  }]
}));

// 两次完成任务占比
const twoTimePassedEChartConfig = JSON.parse(JSON.stringify({
  ...oneTimePassedEChartConfig,
  title: {
    ...oneTimePassedEChartConfig.title,
    text: t('issueAnalysis.detail.handlingEfficiency.chartTitles.twoTimeCompletedRate'),
    left: '35%',
    bottom: '6%'
  },
  legend: {
    top: 'center',
    right: '2px',
    orient: 'vertical',
    itemGap: 4,
    textStyle: {
      fontSize: 12,
      color: '#595959'
    }
  },
  series: [{
    ...oneTimePassedEChartConfig.series[0],
    radius: ['30%', '60%'],
    center: ['35%', '50%'],
    label: {
      show: true,
      position: 'center',
      formatter: function () {
        return '{a|' + (props.chart3Value?.title || '0%') + '}';
      },
      rich: {
        a: {
          fontSize: 14,
          fontWeight: 'bold',
          color: '#262626'
        }
      }
    }
  }]
}));

onMounted(() => {
  completedEChart = eCharts.init(completedRef.value);
  oneTimePassedEChart = eCharts.init(oneTimePassedRef.value);
  tasksChart = eCharts.init(bugsRef.value);
  twoTimePassedEChart = eCharts.init(twoTimePassedRef.value);

  const handleResize = () => {
    // Update bar width for responsive design
    const isMobile = window.innerWidth < 768;
    taskEChartConfig.series[0].barMaxWidth = isMobile ? '28%' : '42%';

    // Resize all charts
    completedEChart?.resize();
    oneTimePassedEChart?.resize();
    tasksChart?.resize();
    twoTimePassedEChart?.resize();
  };

  window.addEventListener('resize', handleResize);

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
  });

  watch([
    () => props.chart0Value,
    () => props.chart1Value,
    () => props.chart2Value,
    () => props.chart3Value,
    () => props.chart4Value
  ], () => {
    taskEChartConfig.series[0].data = props.chart0Value.yData;
    // Update bar width for responsive design
    const isMobile = window.innerWidth < 768;
    taskEChartConfig.series[0].barMaxWidth = isMobile ? '28%' : '42%';

    completedEChartConfig.series[0].data[0] = {
      ...completedEChartConfig.series[0].data[0],
      ...props.chart1Value.value[0],
      value: Number(props.chart1Value.value[0].value)
    };
    completedEChartConfig.series[0].data[1] = {
      ...completedEChartConfig.series[0].data[1],
      ...props.chart1Value.value[1],
      value: Number(props.chart1Value.value[1].value)
    };
    // Update the center label formatter for the first chart
    completedEChartConfig.series[0].label.formatter = function () {
      return '{a|' + (props.chart1Value?.title || '0%') + '}';
    };
    // Title is now static, rate value is shown in center

    oneTimePassedEChartConfig.series[0].data[0] = {
      ...oneTimePassedEChartConfig.series[0].data[0],
      ...props.chart2Value.value[0],
      value: Number(props.chart2Value.value[0].value)
    };
    oneTimePassedEChartConfig.series[0].data[1] = {
      ...oneTimePassedEChartConfig.series[0].data[1],
      ...props.chart2Value.value[1],
      value: Number(props.chart2Value.value[1].value)
    };
    // Update the center label formatter for the second chart
    oneTimePassedEChartConfig.series[0].label.formatter = function () {
      return '{a|' + (props.chart2Value?.title || '0%') + '}';
    };
    // Title is now static, rate value is shown in center

    twoTimePassedEChartConfig.series[0].data[0] = {
      ...twoTimePassedEChartConfig.series[0].data[0],
      ...props.chart3Value.value[0],
      value: Number(props.chart3Value.value[0].value)
    };
    twoTimePassedEChartConfig.series[0].data[1] = {
      ...twoTimePassedEChartConfig.series[0].data[1],
      ...props.chart3Value.value[1],
      value: Number(props.chart3Value.value[1].value)
    };
    // Update the center label formatter for the third chart
    twoTimePassedEChartConfig.series[0].label.formatter = function () {
      return '{a|' + (props.chart3Value?.title || '0%') + '}';
    };
    // Title is now static, rate value is shown in center

    completedEChart.setOption(completedEChartConfig);
    oneTimePassedEChart.setOption(oneTimePassedEChartConfig);
    tasksChart.setOption(taskEChartConfig);
    twoTimePassedEChart.setOption(twoTimePassedEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    completedEChart?.resize();
    oneTimePassedEChart?.resize();
    tasksChart?.resize();
    twoTimePassedEChart?.resize();
  }
});

</script>
<template>
  <div class="chart-container">
    <div class="main-layout">
      <!-- Right side: Charts area -->
      <div class="right-side">
        <!-- Single row: All charts -->
        <div class="chart-row">
          <div ref="bugsRef" class="chart-item bar-chart"></div>
          <div ref="completedRef" class="chart-item pie-chart"></div>
          <div ref="oneTimePassedRef" class="chart-item pie-chart"></div>
          <div ref="twoTimePassedRef" class="chart-item pie-chart"></div>
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
  min-width: 0;
}

.chart-row {
  display: flex;
  gap: 16px;
  width: 100%;
  height: 160px;
}

.chart-item {
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
}

.bar-chart {
  flex: 1;
  padding-left: 50px;
}

.pie-chart {
  flex: 1;
  display: flex;
  align-items: center;
}

/* Tablet responsive */
@media (max-width: 1200px) {
  .main-layout {
    flex-direction: column;
    gap: 16px;
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
}

/* Mobile responsive */
@media (max-width: 768px) {
  .chart-container {
    padding: 12px 12px 12px 4px;
  }

  .main-layout {
    gap: 12px;
  }

  .chart-row {
    height: 180px;
    flex-direction: column;
  }

  .chart-item {
    flex: 1;
    min-width: 100%;
  }
}

/* Small mobile responsive */
@media (max-width: 480px) {
  .chart-container {
    padding: 8px 8px 8px 2px;
  }

  .chart-row {
    height: 160px;
  }
}
</style>
