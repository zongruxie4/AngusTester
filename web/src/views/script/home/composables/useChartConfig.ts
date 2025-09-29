import { useI18n } from 'vue-i18n';
import { ScriptType, enumUtils } from '@xcan-angus/infra';

import { ChartDataItem, PieChartOption } from '@/views/script/types';

/**
 * Composable for managing chart configuration and data transformation
 * Provides reusable chart options and data formatting utilities
 */
export function useChartConfig () {
  const { t } = useI18n();

  /**
   * Default color palette for charts
   */
  const defaultColors = ['#52C41A', '#2D8EFF', '#FFB925', 'rgba(251, 129, 255, 1)', 'rgba(201, 119, 255, 1)'];

  /**
   * Create pie chart configuration with default settings
   * @param chartData - Optional chart data for legend formatter
   * @returns PieChartOption configuration object
   */
  const createPieChartOption = (chartData?: ChartDataItem[]): PieChartOption => ({
    tooltip: {
      trigger: 'item',
      axisPointer: { type: 'shadow' },
      textStyle: {
        fontSize: 12
      }
    },
    legend: {
      top: 'middle',
      right: 10,
      orient: 'vertical',
      itemHeight: 14,
      itemWidth: 14,
      itemGap: 5,
      formatter: function (name: string) {
        // Use provided chart data or fallback to name only
        if (!chartData) return name;

        for (let i = 0; i < chartData.length; i++) {
          if (chartData[i].name === name) {
            return name + ' ' + chartData[i].value;
          }
        }
        return name;
      }
    },
    color: defaultColors,
    series: [
      {
        name: '',
        type: 'pie',
        center: ['30%', '50%'],
        radius: ['65%', '90%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          formatter: '{c}'
        },
        labelLine: {
          show: true
        },
        emphasis: {
          label: {
            show: false,
            fontSize: 12,
            fontWeight: 'normal'
          }
        },
        data: []
      }
    ]
  });

  /**
   * Transform resource info to chart data items
   * @param resourceInfo - Resource information containing script counts
   * @returns Array of chart data items
   */
  const transformResourceToChartData = (resourceInfo: any): ChartDataItem[] => {
    if (!resourceInfo) return [];

    const chartData = [
      {
        name: enumUtils.getEnumDescription(ScriptType, ScriptType.TEST_FUNCTIONALITY),
        value: +resourceInfo.functionalScriptNum
      },
      {
        name: enumUtils.getEnumDescription(ScriptType, ScriptType.TEST_PERFORMANCE),
        value: +resourceInfo.perfScriptNum
      },
      {
        name: enumUtils.getEnumDescription(ScriptType, ScriptType.TEST_STABILITY),
        value: +resourceInfo.stabilityScriptNum
      },
      {
        name: enumUtils.getEnumDescription(ScriptType, ScriptType.TEST_CUSTOMIZATION),
        value: +resourceInfo.customizationScriptNum
      },
      {
        name: enumUtils.getEnumDescription(ScriptType, ScriptType.MOCK_DATA),
        value: +resourceInfo.mockDataScriptNum
      }
    ];
    return chartData;
  };

  return {
    defaultColors,
    createPieChartOption,
    transformResourceToChartData
  };
}
