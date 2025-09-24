import { computed, onMounted, ref, type Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import * as echarts from 'echarts';
import elementResizeDetectorMaker from 'element-resize-detector';
import type { ChartConfig, ConfigInfoItem, ScenarioResult, TestData } from '../types';

/**
 * Composable for managing test summary data and chart configuration
 */
export function useTestSummary (dataSource: Ref<ScenarioResult>) {
  const { t } = useI18n();
  const resizeDetector = elementResizeDetectorMaker();
  let testChart: echarts.ECharts;
  const testChartRef = ref<HTMLElement>();

  /**
   * Get result summary from data source
   */
  const resultSummary = computed(() => {
    return dataSource.value?.resultSummary;
  });

  /**
   * <p>Get performance test data from the scenario result.</p>
   * <p>Contains metrics like TPS, response time, and error rate for performance testing.</p>
   */
  const performanceTestData = computed(() => {
    return dataSource.value?.resultDetailVoMap?.TEST_PERFORMANCE;
  });

  /**
   * <p>Get custom test data from the scenario result.</p>
   * <p>Contains metrics for non-standard custom testing scenarios.</p>
   */
  const customTestData = computed(() => {
    return dataSource.value?.resultDetailVoMap?.TEST_CUSTOMIZATION;
  });

  /**
   * <p>Get functionality test data from the scenario result.</p>
   * <p>Contains test case execution results and configuration information.</p>
   */
  const functionalityTestData = computed(() => {
    return dataSource.value?.resultDetailVoMap?.TEST_FUNCTIONALITY;
  });

  /**
   * <p>Get stability test data from the scenario result.</p>
   * <p>Contains metrics for long-running stability testing scenarios.</p>
   */
  const stabilityTestData = computed(() => {
    return dataSource.value?.resultDetailVoMap?.TEST_STABILITY;
  });

  /**
   * <p>Get TPS trend icon name based on performance comparison.</p>
   * <p>Returns up arrow for better performance, down arrow for worse performance, or undefined if no comparison data.</p>
   */
  const getTpsTrendIcon = (testData: TestData) => {
    const indicatorTps = testData?.indicatorPerf?.tps || testData?.indicatorStability?.tps;
    if (!indicatorTps || !testData?.sampleSummary?.tps) {
      return undefined;
    }
    const sampleTps = +testData.sampleSummary.tps;
    if (sampleTps > +indicatorTps) {
      return 'icon-shangjiantou1';
    }
    if (sampleTps < +indicatorTps) {
      return 'icon-xiajiantou1';
    }
    return undefined;
  };

  /**
   * <p>Get response time trend icon name based on performance comparison.</p>
   * <p>Returns up arrow for worse response time, down arrow for better response time, or undefined if no comparison data.</p>
   */
  const getResponseTimeTrendIcon = (testData: TestData) => {
    const indicatorArt = testData?.indicatorPerf?.art || testData?.indicatorStability?.art;
    if (!indicatorArt || !testData?.sampleSummary?.tranP90) {
      return undefined;
    }
    const tranP90 = +testData.sampleSummary.tranP90;
    if (tranP90 > +indicatorArt) {
      return 'icon-shangjiantou';
    }
    if (tranP90 < +indicatorArt) {
      return 'icon-xiajiantou2';
    }
    return undefined;
  };

  /**
   * <p>Get error rate trend icon name based on performance comparison.</p>
   * <p>Returns up arrow for higher error rate, down arrow for lower error rate, or undefined if no comparison data.</p>
   */
  const getErrorRateTrendIcon = (testData: TestData) => {
    const indicatorErrorRate = testData?.indicatorPerf?.errorRate || testData?.indicatorStability?.errorRate;
    if (!indicatorErrorRate || !testData?.sampleSummary?.errorRate) {
      return undefined;
    }
    const sampleErrorRate = +testData.sampleSummary.errorRate;
    if (sampleErrorRate > +indicatorErrorRate) {
      return 'icon-shangjiantou';
    }
    if (sampleErrorRate < +indicatorErrorRate) {
      return 'icon-xiajiantou2';
    }
    return undefined;
  };

  /**
   * Configuration info for test summary table
   */
  const configInfo: ConfigInfoItem[][] = [
    [
      { label: t('chart.total'), dataIndex: 'totalNum', bgColor: 'bg-blue-1' },
      { label: t('status.success'), dataIndex: 'successNum', bgColor: 'bg-status-success' }
    ],
    [
      { label: t('status.failed'), dataIndex: 'failNum', bgColor: 'bg-status-error' },
      { label: t('status.disabled'), dataIndex: 'disabledNum', bgColor: 'bg-gray-icon' }
    ]
  ];

  /**
   * ECharts configuration for test summary chart
   */
  const echartConfig: ChartConfig = {
    title: {
      text: '0%',
      left: '30%',
      bottom: '0%',
      padding: 2,
      subtext: t('scenario.detail.testSummary.successRate'),
      itemGap: 35,
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
      right: '10%',
      orient: 'vertical',
      itemHeight: 14,
      itemWidth: 14,
      itemGap: 2
    },
    series: [
      {
        name: '',
        type: 'pie',
        radius: ['45%', '65%'],
        center: ['30%', '40%'],
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
            name: t('scenario.detail.testSummary.successCount'),
            value: 0,
            itemStyle: {
              color: '#52C41A'
            }
          },
          {
            name: t('scenario.detail.testSummary.failCount'),
            value: 0,
            itemStyle: {
              color: 'rgba(245, 34, 45, 0.7)'
            }
          }
        ]
      }
    ]
  };

  /**
   * Resize chart when container size changes
   */
  const resizeChart = () => {
    testChart.resize();
  };

  /**
   * Initialize chart with data
   */
  const initializeChart = () => {
    if (!testChartRef.value) return;

    echartConfig.series[0].data[0].value = +resultSummary.value?.testSuccessNum || 0;
    echartConfig.series[0].data[1].value = +resultSummary.value?.testFailureNum || 0;
    echartConfig.title.text = (resultSummary.value?.testSuccessRate || '--') + '%';
    testChart = echarts.init(testChartRef.value);
    testChart.setOption(echartConfig as any);
    resizeDetector.listenTo(testChartRef.value as HTMLElement, resizeChart);
  };

  onMounted(() => {
    initializeChart();
  });

  return {
    testChartRef,
    resultSummary,
    performanceTestData,
    customTestData,
    functionalityTestData,
    stabilityTestData,
    configInfo,
    getTpsTrendIcon,
    getResponseTimeTrendIcon,
    getErrorRateTrendIcon
  };
}
