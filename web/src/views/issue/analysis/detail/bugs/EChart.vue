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
    yData: [0, 0, 0, 0]
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

const bugLevelRef = ref();
const validBugRef = ref();
const bugsRef = ref();
const escapedBugRef = ref();
const bugWorkloadRef = ref();

let bugsChart;
let bugLevelEChart;
let validBugEChart;
let escapedBugEChart;
let bugWorkloadEChart;

// 缺陷
const bugsEChartConfig = {
  title: {
    text: t('common.counts.bugCount'),
    left: '50%',
    bottom: '12%',
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
    bottom: '20%',
    top: '15%'
  },
  xAxis: {
    type: 'category',
    data: [
      t('issueAnalysis.detail.bugs.chartLabels.totalBugs'),
      t('issueAnalysis.detail.bugs.chartLabels.validBugs'),
      t('issueAnalysis.detail.bugs.chartLabels.invalidBugs'),
      t('issueAnalysis.detail.bugs.chartLabels.escapedBugs')
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
          const colors = ['#ff4d4f', '#52c41a', '#faad14', '#1890ff'];
          return colors[params.dataIndex] || '#1890ff';
        },
        borderRadius: [4, 4, 0, 0]
      },
      data: [0, 0, 0, 0],
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
// 缺陷等级
const bugLevelEChartConfig = {
  title: {
    text: t('common.bugLevel'),
    left: '35%',
    bottom: '12%',
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
      radius: ['0%', '50%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      label: {
        show: false
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
          name: t('issueAnalysis.detail.bugs.pieChartLabels.criticalCount'),
          value: 0,
          itemStyle: {
            color: '#ff4d4f'
          }
        },
        {
          name: t('issueAnalysis.detail.bugs.pieChartLabels.majorCount'),
          value: 0,
          itemStyle: {
            color: '#faad14'
          }
        },
        {
          name: t('issueAnalysis.detail.bugs.pieChartLabels.minorCount'),
          value: 0,
          itemStyle: {
            color: '#666cd1'
          }
        },
        {
          name: t('issueAnalysis.detail.bugs.pieChartLabels.trivialCount'),
          value: 0,
          itemStyle: {
            color: '#83a871'
          }
        }
      ]
    }
  ]
};

// 有效缺陷
const validBugEChartConfig = JSON.parse(JSON.stringify({
  ...bugLevelEChartConfig,
  title: {
    ...bugLevelEChartConfig.title,
    text: t('issueAnalysis.detail.bugs.chartTitles.validBugRate'),
    left: '35%',
    bottom: '12%'
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
    ...bugLevelEChartConfig.series[0],
    radius: ['21%', '50%'],
    center: ['35%', '50%'],
    label: {
      show: true,
      position: 'center',
      formatter: function () {
        return '{a|' + (props.chart2Value?.title || '0%') + '}';
      },
      rich: {
        a: {
          fontSize: 16,
          fontWeight: 'bold',
          color: '#262626'
        }
      }
    },
    data: [
      {
        name: t('status.notCompleted'),
        value: 0,
        itemStyle: {
          color: '#ff7875'
        }
      },
      {
        name: t('status.completed'),
        value: 0,
        itemStyle: {
          color: '#52c41a'
        }
      }
    ]
  }]
}));

// 漏测缺陷
const escapedBugEChartConfig = JSON.parse(JSON.stringify({
  ...validBugEChartConfig,
  title: {
    ...validBugEChartConfig.title,
    text: t('issueAnalysis.detail.bugs.chartTitles.escapedBugRate'),
    left: '35%',
    bottom: '12%'
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
    ...validBugEChartConfig.series[0],
    radius: ['21%', '50%'],
    center: ['35%', '50%'],
    label: {
      show: true,
      position: 'center',
      formatter: function () {
        return '{a|' + (props.chart3Value?.title || '0%') + '}';
      },
      rich: {
        a: {
          fontSize: 16,
          fontWeight: 'bold',
          color: '#262626'
        }
      }
    }
  }]
}));

// 缺陷工作量
const bugWorkloadEChartConfig = JSON.parse(JSON.stringify({
  ...validBugEChartConfig,
  title: {
    ...validBugEChartConfig.title,
    text: t('issueAnalysis.detail.bugs.chartTitles.bugWorkloadRate'),
    left: '35%',
    bottom: '12%'
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
    ...validBugEChartConfig.series[0],
    radius: ['21%', '50%'],
    center: ['35%', '50%'],
    label: {
      show: true,
      position: 'center',
      formatter: function () {
        return '{a|' + (props.chart4Value?.title || '0%') + '}';
      },
      rich: {
        a: {
          fontSize: 16,
          fontWeight: 'bold',
          color: '#262626'
        }
      }
    }
  }]
}));

onMounted(() => {
  bugLevelEChart = eCharts.init(bugLevelRef.value);
  validBugEChart = eCharts.init(validBugRef.value);
  bugsChart = eCharts.init(bugsRef.value);
  escapedBugEChart = eCharts.init(escapedBugRef.value);
  bugWorkloadEChart = eCharts.init(bugWorkloadRef.value);

  const handleResize = () => {
    // Update bar width for responsive design
    const isMobile = window.innerWidth < 768;
    bugsEChartConfig.series[0].barMaxWidth = isMobile ? '28%' : '42%';

    // Resize all charts
    bugLevelEChart?.resize();
    validBugEChart?.resize();
    bugsChart?.resize();
    escapedBugEChart?.resize();
    bugWorkloadEChart?.resize();
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
    () => props.chart4Value],
  () => {
    bugsEChartConfig.series[0].data = props.chart0Value.yData;
    // Update bar width for responsive design
    const isMobile = window.innerWidth < 768;
    bugsEChartConfig.series[0].barMaxWidth = isMobile ? '28%' : '42%';

    bugLevelEChartConfig.series[0].data[0] = {
      ...bugLevelEChartConfig.series[0].data[0],
      ...props.chart1Value.value[0],
      value: Number(props.chart1Value.value[0].value)
    };
    bugLevelEChartConfig.series[0].data[1] = {
      ...bugLevelEChartConfig.series[0].data[1],
      ...props.chart1Value.value[1],
      value: Number(props.chart1Value.value[1].value)
    };
    bugLevelEChartConfig.series[0].data[2] = {
      ...bugLevelEChartConfig.series[0].data[2],
      ...props.chart1Value.value[2],
      value: Number(props.chart1Value.value[2].value)
    };
    bugLevelEChartConfig.series[0].data[3] = {
      ...bugLevelEChartConfig.series[0].data[3],
      ...props.chart1Value.value[3],
      value: Number(props.chart1Value.value[3].value)
    };
    // Title is now static, no center label for bug level chart

    validBugEChartConfig.series[0].data[0] = {
      ...validBugEChartConfig.series[0].data[0],
      ...props.chart2Value.value[0],
      value: Number(props.chart2Value.value[0].value)
    };
    validBugEChartConfig.series[0].data[1] = {
      ...validBugEChartConfig.series[0].data[1],
      ...props.chart2Value.value[1],
      value: Number(props.chart2Value.value[1].value)
    };
    // Update the center label formatter for the second chart
    validBugEChartConfig.series[0].label.formatter = function () {
      return '{a|' + (props.chart2Value?.title || '0%') + '}';
    };
    // Title is now static, rate value is shown in center

    escapedBugEChartConfig.series[0].data[0] = {
      ...escapedBugEChartConfig.series[0].data[0],
      ...props.chart3Value.value[0],
      value: Number(props.chart3Value.value[0].value)
    };
    escapedBugEChartConfig.series[0].data[1] = {
      ...escapedBugEChartConfig.series[0].data[1],
      ...props.chart3Value.value[1],
      value: Number(props.chart3Value.value[1].value)
    };
    // Update the center label formatter for the third chart
    escapedBugEChartConfig.series[0].label.formatter = function () {
      return '{a|' + (props.chart3Value?.title || '0%') + '}';
    };
    // Title is now static, rate value is shown in center

    bugWorkloadEChartConfig.series[0].data[0] = {
      ...bugWorkloadEChartConfig.series[0].data[0],
      ...props.chart4Value.value[0],
      value: Number(props.chart4Value.value[0].value)
    };
    bugWorkloadEChartConfig.series[0].data[1] = {
      ...bugWorkloadEChartConfig.series[0].data[1],
      ...props.chart4Value.value[1],
      value: Number(props.chart4Value.value[1].value)
    };
    // Update the center label formatter for the fourth chart
    bugWorkloadEChartConfig.series[0].label.formatter = function () {
      return '{a|' + (props.chart4Value?.title || '0%') + '}';
    };
    // Title is now static, rate value is shown in center

    bugLevelEChart.setOption(bugLevelEChartConfig);
    validBugEChart.setOption(validBugEChartConfig);
    bugsChart.setOption(bugsEChartConfig);
    escapedBugEChart.setOption(escapedBugEChartConfig);
    bugWorkloadEChart.setOption(bugWorkloadEChartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    bugLevelEChart?.resize();
    validBugEChart?.resize();
    escapedBugEChart?.resize();
    bugsChart?.resize();
    bugWorkloadEChart?.resize();
  }
});

</script>
<template>
  <div class="chart-container">
    <div class="main-layout">
      <!-- Right side: Charts area -->
      <div class="right-side">
        <!-- First row: Bar chart and first pie chart -->
        <div class="chart-row first-row">
          <div ref="bugsRef" class="chart-item bar-chart"></div>
          <div ref="bugLevelRef" class="chart-item pie-chart"></div>
        </div>

        <!-- Second row: Last three pie charts -->
        <div class="chart-row second-row">
          <div ref="validBugRef" class="chart-item pie-chart"></div>
          <div ref="escapedBugRef" class="chart-item pie-chart"></div>
          <div ref="bugWorkloadRef" class="chart-item pie-chart"></div>
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

.first-row {
  display: flex;
  gap: 16px;
}

.second-row {
  display: flex;
  gap: 16px;
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

  .second-row {
    flex-wrap: wrap;
  }

  .second-row .chart-item:last-child {
    flex: 1 1 100%;
    max-width: calc(50% - 8px);
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

  .first-row {
    flex-direction: column;
  }

  .second-row {
    flex-direction: column;
  }

  .second-row .chart-item:last-child {
    flex: 1;
    max-width: 100%;
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
