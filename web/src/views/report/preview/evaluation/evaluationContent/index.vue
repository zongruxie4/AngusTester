<script lang="ts" setup>
import { computed, ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '@/views/report/preview/PropsType';
import eCharts from '@/utils/echarts';

const { t } = useI18n();

type Props = {
  dataSource: ReportContent;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

/**
 * Chart references
 */
const performancePassedRateRef = ref<HTMLElement>();
const functionalPassedRateRef = ref<HTMLElement>();
const stabilityPassedRateRef = ref<HTMLElement>();
const compatibilityScoreRef = ref<HTMLElement>();
const usabilityScoreRef = ref<HTMLElement>();
const maintainabilityScoreRef = ref<HTMLElement>();
const scalabilityScoreRef = ref<HTMLElement>();
const securityScoreRef = ref<HTMLElement>();

/**
 * Chart instances
 */
let performanceChart: any = null;
let functionalChart: any = null;
let stabilityChart: any = null;
let compatibilityChart: any = null;
let usabilityChart: any = null;
let maintainabilityChart: any = null;
let scalabilityChart: any = null;
let securityChart: any = null;

/**
 * Get score color based on score value (0-10 scale)
 */
const getScoreColor = (score: number) => {
  if (score >= 9) return '#52c41a'; // green
  if (score >= 8) return '#1890ff'; // blue
  if (score >= 6) return '#faad14'; // orange
  return '#ff4d4f'; // red
};

/**
 * Get rate color based on rate value (0-100 scale)
 */
const getRateColor = (rate: number) => {
  if (rate >= 90) return '#52c41a'; // green
  if (rate >= 80) return '#1890ff'; // blue
  if (rate >= 60) return '#faad14'; // orange
  return '#ff4d4f'; // red
};

/**
 * Create pie chart for pass rate
 */
const createPassRatePieConfig = (rate: number, numerator: number, denominator: number, title: string) => {
  const color = getRateColor(rate);
  const remaining = denominator - numerator;
  
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    series: [
      {
        name: title,
        type: 'pie',
        radius: ['50%', '70%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{d}%',
          fontSize: 16,
          fontWeight: 'bold',
          color: '#333'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 18,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          {
            value: numerator,
            name: '通过',
            itemStyle: { color: color }
          },
          {
            value: remaining,
            name: '未通过',
            itemStyle: { color: '#E4E7ED' }
          }
        ]
      }
    ],
    graphic: [
      {
        type: 'text',
        left: 'center',
        top: 'center',
        z: 10,
        style: {
          text: `${(+rate).toFixed(1)}%`,
          fontSize: 24,
          fontWeight: 'bold',
          fill: color,
          textAlign: 'center'
        }
      },
      {
        type: 'text',
        left: 'center',
        top: '60%',
        z: 10,
        style: {
          text: `${numerator}/${denominator}`,
          fontSize: 14,
          fill: '#666',
          textAlign: 'center'
        }
      }
    ]
  };
};

/**
 * Create pie chart for score (0-10 scale)
 */
const createScorePieConfig = (score: number, title: string) => {
  const color = getScoreColor(score);
  
  return {
    tooltip: {
      trigger: 'item',
      formatter: `${title}: ${(+score).toFixed(1)}分`
    },
    series: [
      {
        name: title,
        type: 'pie',
        radius: ['60%', '85%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: false
          }
        },
        labelLine: {
          show: false
        },
        data: [
          {
            value: score,
            itemStyle: { color: color }
          },
          {
            value: 10 - score,
            itemStyle: { color: '#E4E7ED' }
          }
        ]
      }
    ],
    graphic: [
      {
        type: 'text',
        left: 'center',
        top: '35%',
        z: 10,
        style: {
          text: `${(+score).toFixed(1)}分`,
          fontSize: 28,
          fontWeight: 'bold',
          fill: color,
          textAlign: 'center'
        }
      }
    ]
  };
};

/**
 * Initialize or update a chart
 */
const initOrUpdateChart = (chartInstance: any, ref: HTMLElement | undefined, config: any) => {
  if (!ref) return null;
  
  if (chartInstance && !chartInstance.isDisposed()) {
    // Update existing chart
    chartInstance.setOption(config, true);
    return chartInstance;
  } else {
    // Create new chart
    const chart = eCharts.init(ref);
    chart.setOption(config);
    return chart;
  }
};

/**
 * Initialize and update charts
 */
const initCharts = () => {
  const metrics = props.dataSource?.content?.metrics;
  if (!metrics) return;

  // Performance Passed Rate Chart
  if (metrics.PERFORMANCE_PASSED_RATE && performancePassedRateRef.value) {
    const data = metrics.PERFORMANCE_PASSED_RATE;
    performanceChart = initOrUpdateChart(
      performanceChart,
      performancePassedRateRef.value,
      createPassRatePieConfig(data.rate, data.numerator, data.denominator, '性能测试通过率')
    );
  }

  // Functional Passed Rate Chart
  if (metrics.FUNCTIONAL_PASSED_RATE && functionalPassedRateRef.value) {
    const data = metrics.FUNCTIONAL_PASSED_RATE;
    functionalChart = initOrUpdateChart(
      functionalChart,
      functionalPassedRateRef.value,
      createPassRatePieConfig(data.rate, data.numerator, data.denominator, '功能测试通过率')
    );
  }

  // Stability Passed Rate Chart
  if (metrics.STABILITY_PASSED_RATE && stabilityPassedRateRef.value) {
    const data = metrics.STABILITY_PASSED_RATE;
    stabilityChart = initOrUpdateChart(
      stabilityChart,
      stabilityPassedRateRef.value,
      createPassRatePieConfig(data.rate, data.numerator, data.denominator, '稳定性测试通过率')
    );
  }

  // Compatibility Score Chart
  if (metrics.COMPATIBILITY_SCORE && compatibilityScoreRef.value) {
    compatibilityChart = initOrUpdateChart(
      compatibilityChart,
      compatibilityScoreRef.value,
      createScorePieConfig(Number(metrics.COMPATIBILITY_SCORE.score), '兼容性评分')
    );
  }

  // Usability Score Chart
  if (metrics.USABILITY_SCORE && usabilityScoreRef.value) {
    usabilityChart = initOrUpdateChart(
      usabilityChart,
      usabilityScoreRef.value,
      createScorePieConfig(Number(metrics.USABILITY_SCORE.score), '易用性评分')
    );
  }

  // Maintainability Score Chart
  if (metrics.MAINTAINABILITY_SCORE && maintainabilityScoreRef.value) {
    maintainabilityChart = initOrUpdateChart(
      maintainabilityChart,
      maintainabilityScoreRef.value,
      createScorePieConfig(Number(metrics.MAINTAINABILITY_SCORE.score), '可维护性评分')
    );
  }

  // Scalability Score Chart
  if (metrics.SCALABILITY_SCORE && scalabilityScoreRef.value) {
    scalabilityChart = initOrUpdateChart(
      scalabilityChart,
      scalabilityScoreRef.value,
      createScorePieConfig(Number(metrics.SCALABILITY_SCORE.score), '可扩展性评分')
    );
  }

  // Security Score Chart
  if (metrics.SECURITY_SCORE && securityScoreRef.value) {
    securityChart = initOrUpdateChart(
      securityChart,
      securityScoreRef.value,
      createScorePieConfig(Number(metrics.SECURITY_SCORE.score), '安全性评分')
    );
  }
};

/**
 * Resize all charts
 */
const resizeCharts = () => {
  const charts = [
    performanceChart,
    functionalChart,
    stabilityChart,
    compatibilityChart,
    usabilityChart,
    maintainabilityChart,
    scalabilityChart,
    securityChart
  ];
  charts.forEach(chart => {
    if (chart) {
      chart.resize();
    }
  });
};

/**
 * Dispose all charts
 */
const disposeCharts = () => {
  const charts = [
    performanceChart,
    functionalChart,
    stabilityChart,
    compatibilityChart,
    usabilityChart,
    maintainabilityChart,
    scalabilityChart,
    securityChart
  ];
  charts.forEach(chart => {
    if (chart) {
      chart.dispose();
    }
  });
};

const basicColumns = computed(() => [
   [
    {
      name: '测评对象类型',
      dataIndex: 'totalCases',
      customRender: () => {
        return props.dataSource?.report?.targetType?.message;
      }
    },
    {
      name: '测评对象名称',
      dataIndex: 'overallScore',
      customRender: () => {
        return props.dataSource?.report?.targetName;
      }
    },
  ],
  [
    {
      name: '总测评数',
      dataIndex: 'totalCases',
      customRender: () => {
        return props.dataSource?.content?.totalCases;
      }
    },
    {
      name: '综合评分',
      dataIndex: 'overallScore',
      customRender: () => {
        return props.dataSource?.content?.overallScore;
      }
    },
  ],

]);

// Watch for data changes
watch(() => props.dataSource, () => {
  nextTick(() => {
    initCharts();
  });
}, {
  immediate: true,
  deep: true
});

onMounted(() => {
  nextTick(() => {
    initCharts();
    window.addEventListener('resize', resizeCharts);
  });
});

onBeforeUnmount(() => {
  disposeCharts();
  window.removeEventListener('resize', resizeCharts);
});

</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a1" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25"></em>测评信息</span>
    </h1>

    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a1.1" class="text-3.5 text-theme-title font-medium">1.1、<em class="inline-block w-0.25"></em>测评概览</span>
    </h1>

    <div class="border-t border-l border-solid border-border-input mb-8">
        <div
            v-for="(column,index) in basicColumns"
            :key="index"
            class="flex border-b border-solid border-border-input">
            <template v-for="item in column" :key="item.dataIndex">
            <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                {{ item.name }}
            </div>
            <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
                {{ item.customRender()}}
            </div>
            </template>
        </div>
    </div>

    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a1.2" class="text-3.5 text-theme-title font-medium">1.2、<em class="inline-block w-0.25"></em>测评信息详情</span>
    </h1>

    <div v-if="props.dataSource?.content?.metrics" class="metrics-charts-container">
      <!-- Performance Passed Rate -->
      <div v-if="props.dataSource?.content?.metrics.PERFORMANCE_PASSED_RATE" class="metric-item mb-6">
        <h2 class="text-3.5 text-theme-title font-medium mb-3">性能测试通过率</h2>
        <div class="metric-chart-wrapper">
          <div ref="performancePassedRateRef" class="metric-chart"></div>
          <div class="metric-info">
            <div class="info-row">
              <span class="info-label">通过率：</span>
              <span class="info-value" :style="{ color: getRateColor(props.dataSource.content.metrics.PERFORMANCE_PASSED_RATE.rate) }">
                {{ (+props.dataSource.content.metrics.PERFORMANCE_PASSED_RATE.rate).toFixed(1) }}%
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">通过数量：</span>
              <span class="info-value">
                {{ props.dataSource.content.metrics.PERFORMANCE_PASSED_RATE.numerator }}/{{ props.dataSource.content.metrics.PERFORMANCE_PASSED_RATE.denominator }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">得分：</span>
              <span class="info-value">
                {{ (+props.dataSource.content.metrics.PERFORMANCE_PASSED_RATE.score).toFixed(1) }} 分
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Functional Passed Rate -->
      <div v-if="props.dataSource?.content?.metrics.FUNCTIONAL_PASSED_RATE" class="metric-item mb-6">
        <h2 class="text-3.5 text-theme-title font-medium mb-3">功能测试通过率</h2>
        <div class="metric-chart-wrapper">
          <div ref="functionalPassedRateRef" class="metric-chart"></div>
          <div class="metric-info">
            <div class="info-row">
              <span class="info-label">通过率：</span>
              <span class="info-value" :style="{ color: getRateColor(props.dataSource.content.metrics.FUNCTIONAL_PASSED_RATE.rate) }">
                {{ (+props.dataSource.content.metrics.FUNCTIONAL_PASSED_RATE.rate).toFixed(1) }}%
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">通过数量：</span>
              <span class="info-value">
                {{ props.dataSource.content.metrics.FUNCTIONAL_PASSED_RATE.numerator }}/{{ props.dataSource.content.metrics.FUNCTIONAL_PASSED_RATE.denominator }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">得分：</span>
              <span class="info-value">
                {{ (+props.dataSource.content.metrics.FUNCTIONAL_PASSED_RATE.score).toFixed(1) }} 分
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Stability Passed Rate -->
      <div v-if="props.dataSource?.content?.metrics.STABILITY_PASSED_RATE" class="metric-item mb-6">
        <h2 class="text-3.5 text-theme-title font-medium mb-3">稳定性测试通过率</h2>
        <div class="metric-chart-wrapper">
          <div ref="stabilityPassedRateRef" class="metric-chart"></div>
          <div class="metric-info">
            <div class="info-row">
              <span class="info-label">通过率：</span>
              <span class="info-value" :style="{ color: getRateColor(props.dataSource.content.metrics.STABILITY_PASSED_RATE.rate) }">
                {{ (+props.dataSource.content.metrics.STABILITY_PASSED_RATE.rate).toFixed(1) }}%
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">通过数量：</span>
              <span class="info-value">
                {{ props.dataSource.content.metrics.STABILITY_PASSED_RATE.numerator }}/{{ props.dataSource.content.metrics.STABILITY_PASSED_RATE.denominator }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">得分：</span>
              <span class="info-value">
                {{ (+props.dataSource.content.metrics.STABILITY_PASSED_RATE.score).toFixed(1) }} 分
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Quality Scores Grid -->
      <div class="quality-scores-grid mb-6">
        <!-- Compatibility Score -->
        <div v-if="props.dataSource?.content?.metrics.COMPATIBILITY_SCORE" class="quality-score-item">
          <h3 class="text-3.5 text-theme-title font-medium mb-3">兼容性评分</h3>
          <div ref="compatibilityScoreRef" class="quality-score-chart"></div>
          <div class="quality-score-info">
            <span class="score-value" :style="{ color: getScoreColor(Number(props.dataSource.content.metrics.COMPATIBILITY_SCORE.score)) }">
              {{ Number(props.dataSource.content.metrics.COMPATIBILITY_SCORE.score).toFixed(1) }} 分
            </span>
          </div>
        </div>

        <!-- Usability Score -->
        <div v-if="props.dataSource?.content?.metrics.USABILITY_SCORE" class="quality-score-item">
          <h3 class="text-3.5 text-theme-title font-medium mb-3">易用性评分</h3>
          <div ref="usabilityScoreRef" class="quality-score-chart"></div>
          <div class="quality-score-info">
            <span class="score-value" :style="{ color: getScoreColor(Number(props.dataSource.content.metrics.USABILITY_SCORE.score)) }">
              {{ Number(props.dataSource.content.metrics.USABILITY_SCORE.score).toFixed(1) }} 分
            </span>
          </div>
        </div>

        <!-- Maintainability Score -->
        <div v-if="props.dataSource?.content?.metrics.MAINTAINABILITY_SCORE" class="quality-score-item">
          <h3 class="text-3.5 text-theme-title font-medium mb-3">可维护性评分</h3>
          <div ref="maintainabilityScoreRef" class="quality-score-chart"></div>
          <div class="quality-score-info">
            <span class="score-value" :style="{ color: getScoreColor(Number(props.dataSource.content.metrics.MAINTAINABILITY_SCORE.score)) }">
              {{ Number(props.dataSource.content.metrics.MAINTAINABILITY_SCORE.score).toFixed(1) }} 分
            </span>
          </div>
        </div>

        <!-- Scalability Score -->
        <div v-if="props.dataSource?.content?.metrics.SCALABILITY_SCORE" class="quality-score-item">
          <h3 class="text-3.5 text-theme-title font-medium mb-3">可扩展性评分</h3>
          <div ref="scalabilityScoreRef" class="quality-score-chart"></div>
          <div class="quality-score-info">
            <span class="score-value" :style="{ color: getScoreColor(Number(props.dataSource.content.metrics.SCALABILITY_SCORE.score)) }">
              {{ Number(props.dataSource.content.metrics.SCALABILITY_SCORE.score).toFixed(1) }} 分
            </span>
          </div>
        </div>

        <!-- Security Score -->
        <div v-if="props.dataSource?.content?.metrics.SECURITY_SCORE" class="quality-score-item">
          <h3 class="text-3.5 text-theme-title font-medium mb-3">安全性评分</h3>
          <div ref="securityScoreRef" class="quality-score-chart"></div>
          <div class="quality-score-info">
            <span class="score-value" :style="{ color: getScoreColor(Number(props.dataSource.content.metrics.SECURITY_SCORE.score)) }">
              {{ Number(props.dataSource.content.metrics.SECURITY_SCORE.score).toFixed(1) }} 分
            </span>
          </div>
        </div>
      </div>
    </div>






  </div>
</template>

<style scoped>
.metrics-charts-container {
  padding: 20px 0;
}

.metric-item {
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 16px;
  background: #fff;
}

.metric-chart-wrapper {
  display: flex;
  align-items: center;
  gap: 30px;
}

.metric-chart {
  width: 300px;
  height: 130px;
  flex-shrink: 0;
}

.metric-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.info-label {
  color: #666;
  width: 100px;
  flex-shrink: 0;
}

.info-value {
  font-weight: 600;
  color: #333;
}

.quality-scores-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.quality-score-item {
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 16px;
  background: #fff;
  text-align: center;
}

.quality-score-chart {
  width: 100%;
  height: 130px;
}

.quality-score-info {
  margin-top: 12px;
}

.score-value {
  font-size: 20px;
  font-weight: bold;
}

@media print {
  .metric-chart,
  .quality-score-chart {
    page-break-inside: avoid;
  }
}
</style>