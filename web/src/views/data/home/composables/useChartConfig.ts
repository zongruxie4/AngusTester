import { useI18n } from 'vue-i18n';
import { StatisticsCardConfig, ChartOption } from '../types';

/**
 * <p>Composable for managing chart configurations and statistics display</p>
 * <p>Provides ECharts options and statistics card configurations</p>
 */
export function useChartConfig() {
  const { t } = useI18n();

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
      name: t('dataHome.statistics.totalVariable')
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
      name: t('dataHome.statistics.dataSourceSet')
    }
  ];

  /**
   * <p>Base chart option configuration for pie charts</p>
   * <p>Provides common configuration for all statistics charts</p>
   */
  const createBaseChartOption = (): ChartOption => ({
    title: {
      text: '',
      textStyle: {
        fontSize: 12
      }
    },
    tooltip: {
      show: true
    },
    legend: {
      bottom: '0',
      right: 'center',
      orient: 'horizontal',
      itemHeight: 14,
      itemWidth: 14
    },
    grid: {
      top: '8%',
      left: '3%',
      right: '3%',
      bottom: '3%',
      containLabel: true
    },
    series: [
      {
        name: '',
        type: 'pie',
        radius: ['30%', '40%'],
        center: ['50%', '40%'],
        avoidLabelOverlap: true,
        label: {
          show: true,
          formatter: '{c}'
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
        data: []
      }
    ]
  });

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
          color: 'rgba(250, 84, 28, 1)'
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
    createDatasourceChartOption
  };
}
