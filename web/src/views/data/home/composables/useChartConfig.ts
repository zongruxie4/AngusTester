import { useI18n } from 'vue-i18n';
import { StatisticsCardConfig, ChartOption } from '../types';

/**
 * <p>Composable for managing chart configurations and statistics display</p>
 * <p>Provides ECharts options and statistics card configurations</p>
 */
export function useChartConfig () {
  const { t } = useI18n();

  /**
   * <p>Check if screen is small (mobile/tablet)</p>
   * <p>Determines responsive behavior for chart layouts</p>
   */
  const isSmallScreen = (): boolean => {
    return window.innerWidth < 1524; // lg breakpoint for better responsive behavior
  };

  /**
   * <p>Get responsive legend configuration</p>
   * <p>Returns legend config based on screen size</p>
   */
  const getResponsiveLegendConfig = () => {
    if (isSmallScreen()) {
      // Small screen: legend at bottom, horizontal layout
      return {
        bottom: '0%',
        left: 'center',
        orient: 'horizontal',
        itemHeight: 10,
        itemWidth: 10,
        itemGap: 4,
        textStyle: {
          fontSize: 9
        }
      };
    } else {
      // Large screen: legend at right, vertical layout
      return {
        top: 'center',
        right: '5%',
        orient: 'vertical',
        itemHeight: 14,
        itemWidth: 14,
        itemGap: 8,
        textStyle: {
          fontSize: 12
        }
      };
    }
  };

  /**
   * <p>Get responsive grid configuration</p>
   * <p>Returns grid config based on screen size</p>
   */
  const getResponsiveGridConfig = () => {
    if (isSmallScreen()) {
      // Small screen: full width, more bottom space for legend
      return {
        top: '5%',
        left: '5%',
        right: '5%',
        bottom: '25%',
        containLabel: true
      };
    } else {
      // Large screen: left space for legend
      return {
        top: '8%',
        left: '3%',
        right: '25%',
        bottom: '3%',
        containLabel: true
      };
    }
  };

  /**
   * <p>Get responsive series configuration</p>
   * <p>Returns series config based on screen size</p>
   */
  const getResponsiveSeriesConfig = () => {
    if (isSmallScreen()) {
      // Small screen: centered pie chart, smaller radius
      return {
        radius: ['20%', '35%'],
        center: ['50%', '35%']
      };
    } else {
      // Large screen: left-aligned pie chart
      return {
        radius: ['30%', '50%'],
        center: ['35%', '50%']
      };
    }
  };

  /**
   * <p>Statistics card configuration for display</p>
   * <p>Defines the appearance and data mapping for statistics cards</p>
   */
  const statisticsCardConfig: StatisticsCardConfig[] = [
    {
      topClass: 'huang-top',
      bottomClass: 'huang-bottom',
      total: 'allVariable',
      week: 'variableByLastWeek',
      month: 'variableByLastMonth',
      name: t('dataHome.statistics.variable')
    },
    {
      topClass: 'hong-top',
      bottomClass: 'hong-bottom',
      total: 'allDataset',
      week: 'datasetByLastWeek',
      month: 'datasetByLastMonth',
      name: t('dataHome.statistics.dataset')
    },
    {
      topClass: 'lan-top',
      bottomClass: 'lan-bottom',
      total: 'allFile',
      week: 'fileByLastWeek',
      month: 'fileByLastMonth',
      name: t('dataHome.statistics.file')
    },
    {
      topClass: 'qin-top',
      bottomClass: 'qin-bottom',
      total: 'allDatasource',
      week: 'datasourceByLastWeek',
      month: 'datasourceByLastMonth',
      name: t('dataHome.statistics.datasource')
    }
  ];

  /**
   * <p>Base chart option configuration for pie charts</p>
   * <p>Provides common configuration for all statistics charts</p>
   */
  const createBaseChartOption = (): ChartOption => {
    const legendConfig = getResponsiveLegendConfig();
    const gridConfig = getResponsiveGridConfig();
    const seriesConfig = getResponsiveSeriesConfig();

    return {
      title: {
        text: '',
        textStyle: {
          fontSize: 12
        }
      },
      tooltip: {
        show: true
      },
      legend: legendConfig,
      grid: gridConfig,
      series: [
        {
          name: '',
          type: 'pie',
          radius: seriesConfig.radius,
          center: seriesConfig.center,
          avoidLabelOverlap: true,
          label: {
            show: true,
            formatter: '{c}',
            fontSize: 12
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: true,
            length: 10,
            length2: 5
          },
          data: []
        }
      ]
    };
  };

  /**
   * <p>Variable usage chart configuration</p>
   * <p>Chart showing used vs unused variables</p>
   */
  const createVariableChartOption = (): ChartOption => {
    const option = createBaseChartOption();
    option.series[0].data = [
      {
        name: t('dataHome.statistics.usedCount'),
        value: 0,
        itemStyle: {
          color: 'rgba(82, 196, 26, 1)'
        }
      },
      {
        name: t('dataHome.statistics.unusedCount'),
        value: 0,
        itemStyle: {
          color: 'rgba(255, 77, 79, 1)'
        }
      }
    ];
    return option;
  };

  /**
   * <p>Dataset usage chart configuration</p>
   * <p>Chart showing used vs unused datasets</p>
   */
  const createDatasetChartOption = (): ChartOption => {
    const option = createBaseChartOption();
    option.series[0].data = [
      {
        name: t('dataHome.statistics.usedCount'),
        value: 0,
        itemStyle: {
          color: 'rgba(82, 196, 26, 1)'
        }
      },
      {
        name: t('dataHome.statistics.unusedCount'),
        value: 0,
        itemStyle: {
          color: 'rgba(255, 77, 79, 1)'
        }
      }
    ];
    return option;
  };

  /**
   * <p>File resource type chart configuration</p>
   * <p>Chart showing space, directory, and file counts</p>
   */
  const createFileChartOption = (): ChartOption => {
    const option = createBaseChartOption();
    option.series[0].data = [
      {
        name: t('dataHome.statistics.space'),
        value: 0,
        itemStyle: {
          color: 'rgba(24, 144, 255, 1)'
        }
      },
      {
        name: t('dataHome.statistics.directory'),
        value: 0,
        itemStyle: {
          color: 'rgba(250, 173, 20, 1)'
        }
      },
      {
        name: t('dataHome.statistics.file'),
        value: 0,
        itemStyle: {
          color: 'rgba(114, 46, 209, 1)'
        }
      }
    ];
    return option;
  };

  /**
   * <p>Datasource database type chart configuration</p>
   * <p>Chart showing counts for different database types</p>
   */
  const createDatasourceChartOption = (): ChartOption => {
    const option = createBaseChartOption();

    // 为数据源图表调整特殊配置
    if (isSmallScreen()) {
      // 小屏幕：图例在底部，水平布局
      option.legend = {
        bottom: '0%',
        left: 'center',
        orient: 'horizontal',
        itemHeight: 8,
        itemWidth: 8,
        itemGap: 2,
        textStyle: {
          fontSize: 8
        }
      };
      option.grid = {
        top: '5%',
        left: '5%',
        right: '5%',
        bottom: '30%',
        containLabel: true
      };
      option.series[0].radius = ['15%', '30%'];
      option.series[0].center = ['50%', '30%'];
    } else {
      // 大屏幕：图例在右侧，垂直布局
      option.legend = {
        top: '5%',
        right: '5%',
        orient: 'vertical',
        itemHeight: 12,
        itemWidth: 12,
        itemGap: 4,
        textStyle: {
          fontSize: 10
        }
      };
      option.grid = {
        top: '8%',
        left: '3%',
        right: '30%',
        bottom: '3%',
        containLabel: true
      };
      option.series[0].radius = ['25%', '45%'];
      option.series[0].center = ['30%', '50%'];
    }

    option.series[0].label.fontSize = isSmallScreen() ? 8 : 10;
    option.series[0].emphasis.label.fontSize = isSmallScreen() ? 10 : 12;

    option.series[0].data = [
      {
        name: 'H2',
        value: 0,
        itemStyle: {
          color: 'rgba(82, 196, 26, 1)'
        }
      },
      {
        name: 'HSQLDB',
        value: 0,
        itemStyle: {
          color: 'rgba(24, 144, 255, 1)'
        }
      },
      {
        name: 'SQLITE',
        value: 0,
        itemStyle: {
          color: 'rgba(250, 173, 20, 1)'
        }
      },
      {
        name: 'POSTGRES',
        value: 0,
        itemStyle: {
          color: 'rgba(114, 46, 209, 1)'
        }
      },
      {
        name: 'MARIADB',
        value: 0,
        itemStyle: {
          color: 'rgba(245, 34, 45, 1)'
        }
      },
      {
        name: 'MYSQL',
        value: 0,
        itemStyle: {
          color: 'rgb(35, 148, 69)'
        }
      },
      {
        name: 'ORACLE',
        value: 0,
        itemStyle: {
          color: 'rgba(250, 173, 20, 1)'
        }
      },
      {
        name: 'SQLSERVER',
        value: 0,
        itemStyle: {
          color: 'rgba(82, 196, 26, 1)'
        }
      }
    ];
    return option;
  };

  return {
    statisticsCardConfig,
    createBaseChartOption,
    createVariableChartOption,
    createDatasetChartOption,
    createFileChartOption,
    createDatasourceChartOption,
    isSmallScreen,
    getResponsiveLegendConfig,
    getResponsiveGridConfig,
    getResponsiveSeriesConfig
  };
}
