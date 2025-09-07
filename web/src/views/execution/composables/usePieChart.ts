import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
import elementResizeDetector from 'element-resize-detector';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
import * as echarts from 'echarts/core';
import {
  LegendComponent,
  LegendComponentOption,
  TitleComponent,
  TitleComponentOption,
  TooltipComponent,
  TooltipComponentOption
} from 'echarts/components';
import { PieChart, PieSeriesOption } from 'echarts/charts';
import { LabelLayout } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';

// Register ECharts components
echarts.use([TitleComponent, TooltipComponent, LegendComponent, PieChart, CanvasRenderer, LabelLayout]);

type EChartsOption = echarts.ComposeOption<TitleComponentOption | TooltipComponentOption | LegendComponentOption | PieSeriesOption>;

export interface PieChartProps {
  color: string[];
  type: string;
  title: string;
  total: number;
  dataSource: { name: string; value: number }[];
}

/**
 * Composable for managing pie chart functionality
 * Handles chart initialization, updates, and resize events
 */
export function usePieChart (props: PieChartProps) {
  const chartsRef = ref<HTMLElement>();
  let myChart: echarts.ECharts | null = null;
  const erd = elementResizeDetector({ strategy: 'scroll' });

  /**
   * Initialize ECharts instance
   */
  const initChart = () => {
    if (!chartsRef.value) {
      return;
    }
    myChart = echarts.init(chartsRef.value, undefined, { renderer: 'canvas' });
    myChart.setOption(createChartOption());
  };

  /**
   * Create ECharts configuration option
   */
  const createChartOption = (): EChartsOption => ({
    color: props.color,
    title: {
      show: false,
      textStyle: { color: '#444C5A', fontSize: '12px', fontWeight: 'normal' },
      left: 38,
      bottom: 0
    },
    tooltip: {
      trigger: 'item',
      confine: false,
      textStyle: {
        fontSize: 12
      }
    },
    series: [
      {
        center: [65, '50%'],
        type: 'pie',
        selectedOffset: 5,
        left: 0,
        top: 0,
        radius: [45, 60],
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: 'center',
          color: '#444C5A'
        },
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        emphasis: {
          scale: true,
          scaleSize: 3,
          label: {
            show: false,
            fontSize: 12,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: props.dataSource
      }
    ]
  });

  /**
   * Update chart data and legend configuration
   */
  const updateChart = () => {
    if (!myChart) return;

    const option = createChartOption();

    // Configure legend based on chart type
    if (props.type === 'script_type') {
      option.legend = [{
        orient: 'vertical',
        right: 10,
        top: 42,
        itemHeight: 12,
        itemWidth: 12,
        itemGap: 5,
        data: props.dataSource
      }];
    } else {
      // Split legend into two columns for status chart
      option.legend = [
        {
          orient: 'vertical',
          right: 80,
          top: 51,
          itemHeight: 12,
          itemWidth: 12,
          itemGap: 5,
          data: props.dataSource.slice(0, 4)
        },
        {
          orient: 'vertical',
          right: 10,
          top: 51,
          itemHeight: 12,
          itemWidth: 12,
          itemGap: 5,
          data: props.dataSource.slice(4, 7)
        }
      ];
    }

    myChart.setOption(option, true);
  };

  /**
   * Handle window resize events
   */
  const resizeHandler = debounce(duration.resize, () => {
    myChart?.resize();
  });

  /**
   * Watch for data source changes and update chart
   */
  watch(() => props.dataSource, updateChart, { deep: true, immediate: true });

  /**
   * Cleanup chart instance
   */
  const destroyChart = () => {
    if (myChart) {
      myChart.dispose();
      myChart = null;
    }
  };

  onMounted(() => {
    initChart();
    erd.listenTo(document.body, resizeHandler);
  });

  onBeforeUnmount(() => {
    erd.removeListener(document.body, resizeHandler);
    destroyChart();
  });

  return {
    chartsRef,
    updateChart
  };
}
