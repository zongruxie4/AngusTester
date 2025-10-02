import { onMounted, ref, watch } from 'vue';
import * as eCharts from 'echarts';
import { useI18n } from 'vue-i18n';
import { ChartProps } from '../types';

export function useChart (props: ChartProps) {
  const { t } = useI18n();

  // Chart DOM references
  const completedRef = ref<HTMLElement>();
  const completedWorkloadRef = ref<HTMLElement>();

  // Chart instances
  let completedEchart: eCharts.ECharts;
  let completedWorkloadEchart: eCharts.ECharts;

  /**
   * Base chart configuration for completion rate visualization
   * Configures pie chart with center title and legend
   */
  const completedEchartConfig = {
    title: {
      text: '0%',
      left: '35%',
      bottom: '10%',
      padding: 2,
      subtext: t('issueAnalysis.detail.progress.chartTitles.taskProgress'),
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
        radius: ['45%', '60%'],
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
            name: t('status.notCompleted'),
            value: 0,
            itemStyle: {
              color: 'rgb(246,159,42)'
            }
          },
          {
            name: t('status.completed'),
            value: 0,
            itemStyle: {
              color: '#52C41A'
            }
          }
        ]
      }
    ]
  };

  /**
   * Workload chart configuration
   * Deep clone of completion chart with different subtitle
   */
  const completedWorkloadEchartConfig = JSON.parse(JSON.stringify({
    ...completedEchartConfig,
    title: {
      ...completedEchartConfig.title,
      subtext: t('issueAnalysis.detail.progress.chartTitles.workloadProgress')
    }
  }));

  /**
   * Initialize chart instances and set up data watchers
   * Creates ECharts instances and configures reactive data updates
   */
  const initializeCharts = () => {
    if (completedRef.value && completedWorkloadRef.value) {
      completedEchart = eCharts.init(completedRef.value);
      completedWorkloadEchart = eCharts.init(completedWorkloadRef.value);

      // Watch for prop changes and update charts
      watch([() => props.chart1Value, () => props.chart2Value], () => {
        updateChartData();
      }, {
        immediate: true,
        deep: true
      });
    }
  };

  /**
   * Update chart data based on current props
   * Synchronizes chart configurations with incoming data
   */
  const updateChartData = () => {
    // Update completion chart data
    completedEchartConfig.series[0].data[0] = {
      ...completedEchartConfig.series[0].data[0],
      ...props.chart1Value.value[0]
    } as any;
    completedEchartConfig.series[0].data[1] = {
      ...completedEchartConfig.series[0].data[1],
      ...props.chart1Value.value[1]
    } as any;
    completedEchartConfig.title.text = props.chart1Value.title;

    // Update workload chart data
    completedWorkloadEchartConfig.series[0].data[0] = {
      ...completedWorkloadEchartConfig.series[0].data[0],
      ...props.chart2Value.value[0]
    };
    completedWorkloadEchartConfig.series[0].data[1] = {
      ...completedWorkloadEchartConfig.series[0].data[1],
      ...props.chart2Value.value[1]
    };
    completedWorkloadEchartConfig.title.text = props.chart2Value.title;

    // Apply configurations to charts
    completedEchart.setOption(completedEchartConfig);
    completedWorkloadEchart.setOption(completedWorkloadEchartConfig);
  };

  /**
   * Resize chart instances
   * Handles window resize events for responsive charts
   */
  const resizeCharts = () => {
    completedEchart?.resize();
    completedWorkloadEchart?.resize();
  };

  // Initialize charts on mount
  onMounted(() => {
    initializeCharts();
  });

  return {
    completedRef,
    completedWorkloadRef,
    resizeCharts
  };
}
