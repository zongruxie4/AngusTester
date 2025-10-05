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
    text: t('common.counts.overdueRate'),
    left: '30%',
    bottom: '5%',
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
    orient: 'vertical',
    itemGap: 12,
    textStyle: {
      fontSize: 12,
      color: '#595959'
    }
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['35%', '60%'],
      center: ['30%', '50%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        position: 'center',
        formatter: function () {
          return '{a|' + (props.chart1Value?.title || '0%') + '}';
        },
        rich: {
          a: {
            fontSize: 16,
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
          name: t('status.notOverdue'),
          value: 0,
          itemStyle: {
            color: '#52c41a'
          }
        },
        {
          name: t('status.overdue'),
          value: 0,
          itemStyle: {
            color: '#ff7875'
          }
        }
      ]
    }
  ]
};

const savingWorkloadEChartConfig = {
  title: {
    text: t('common.counts.overdueWorkloadRate'),
    left: '30%',
    bottom: '5%',
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
    orient: 'vertical',
    itemGap: 12,
    textStyle: {
      fontSize: 12,
      color: '#595959'
    }
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['35%', '60%'],
      center: ['30%', '50%'],
      avoidLabelOverlap: true,
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
          name: t('status.notOverdue'),
          value: 0,
          itemStyle: {
            color: '#52c41a'
          }
        },
        {
          name: t('status.overdue'),
          value: 0,
          itemStyle: {
            color: '#ff7875'
          }
        }
      ]
    }
  ]
};

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
    // Title is now static, rate value is shown in center

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
    // Title is now static, rate value is shown in center
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
  <div class="overdue-analysis-container">
    <!-- Key metrics cards section -->
    <div class="metrics-section">
      <div class="metrics-grid">
        <!-- Overdue count -->
        <div class="metric-card overdue-count">
          <div class="metric-value">{{ props.overdueAssessmentData.overdueNum || 0 }}</div>
          <div class="metric-label">{{ t('common.counts.overdueCount') }}</div>
        </div>

        <!-- Risk level -->
        <div class="metric-card risk-level">
          <div
            :class="`risk-indicator risk-${props.overdueAssessmentData?.riskLevel?.value?.toLowerCase()}`"
            class="metric-value">
            {{ overdueAssessmentData?.riskLevel?.message }}
          </div>
          <div class="metric-label">{{ t('issueAnalysis.detail.overdueAssessment.statistics.overdueRisk') }}</div>
        </div>
      </div>

      <div class="metrics-grid secondary">
        <!-- Overdue time -->
        <div class="metric-card">
          <div class="metric-value text-warning">
            {{ props.overdueAssessmentData.overdueTime || 0 }}
            <span class="unit">{{ t('unit.hour') }}</span>
          </div>
          <div class="metric-label">{{ t('issueAnalysis.detail.overdueAssessment.statistics.overdueTime') }}</div>
        </div>

        <!-- Average daily processed workload -->
        <div class="metric-card">
          <div class="metric-value">
            {{ props.overdueAssessmentData.dailyProcessedWorkload || 0 }}
          </div>
          <div class="metric-label">{{ t('issueAnalysis.detail.overdueAssessment.statistics.averageDailyProcessedWorkload') }}</div>
        </div>

        <!-- Estimated processing time -->
        <div class="metric-card">
          <div class="metric-value">
            {{ props.overdueAssessmentData.overdueWorkloadProcessingTime || 0 }}
            <span class="unit">{{ t('unit.hour') }}</span>
          </div>
          <div class="metric-label">{{ t('issueAnalysis.detail.overdueAssessment.statistics.overdueWorkloadProcessingTime') }}</div>
        </div>
      </div>
    </div>

    <!-- Charts section -->
    <div class="charts-section">
      <div ref="completedWorkloadRef" class="chart-container"></div>
      <div ref="savingWorkloadRef" class="chart-container"></div>
    </div>
  </div>
</template>
<style scoped>
.overdue-analysis-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 8px;
}

.metrics-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 8px;
}

.metrics-grid.secondary {
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
}

.metric-card {
  background: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 12px;
  text-align: center;
  transition: all 0.2s ease;
}

.metric-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-color: #d9d9d9;
}

.metric-card.overdue-count {
  background: linear-gradient(135deg, #fff2f0 0%, #ffebe6 100%);
  border-color: #ffccc7;
}

.metric-card.risk-level {
  background: linear-gradient(135deg, #f6ffed 0%, #f0f9ff 100%);
  border-color: #b7eb8f;
}

.metric-value {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 2px;
  line-height: 1.2;
}

.metric-value.text-warning {
  color: #fa8c16;
}

.metric-label {
  font-size: 12px;
  color: #8c8c8c;
  line-height: 1.3;
}

.unit {
  font-size: 14px;
  color: #8c8c8c;
  margin-left: 2px;
}

.risk-indicator {
  font-size: 20px;
  font-weight: 700;
}

.risk-indicator.risk-low {
  color: #52c41a;
}

.risk-indicator.risk-high {
  color: #ff4d4f;
}

.risk-indicator.risk-none {
  color: #1890ff;
}

.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 12px;
  min-height: 200px;
}

.chart-container {
  background: transparent;
  border: none;
  border-radius: 0;
  padding: 8px;
  min-height: 180px;
  width: 100%;
  overflow: hidden;
}

/* Responsive design */
@media (max-width: 768px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .metrics-grid.secondary {
    grid-template-columns: 1fr;
  }

  .charts-section {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .chart-container {
    min-height: 160px;
  }

  .metric-value {
    font-size: 18px;
  }
}

@media (max-width: 480px) {
  .charts-section {
    grid-template-columns: 1fr;
    gap: 6px;
  }

  .chart-container {
    min-height: 140px;
    padding: 4px;
  }
}
</style>
