import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { enumUtils, ScriptType } from '@xcan-angus/infra';
import { ExecStatus } from '@/enums/enums';
import { analysis } from '@/api/tester';
import type { PieData, PieSetting } from '../types';

// TODO 替换成Dashboard组件

/**
 * Composable for managing chart data and statistics
 * Handles data fetching, processing, and chart configuration
 */
export function useChartData (projectId: string) {
  const { t } = useI18n();

  // Chart configuration for different grouping types
  const chartGroupSettings = ref<PieSetting[]>([
    { key: 'script_type', value: t('execution.chartInfo.scriptType'), type: [] },
    { key: 'status', value: t('execution.chartInfo.executionStatus'), type: [] }
  ]);

  // Chart data and colors
  const scriptTypeData = ref<{ name: string; value: number }[]>([]);
  const scriptTypeColors = ref<string[]>([]);
  const statusData = ref<{ name: string; value: number }[]>([]);
  const statusColors = ref<string[]>([]);

  // Chart loading state
  const isChartLoading = ref(true);
  const pieChartData = ref<PieData[]>([]);

  // Initialize enum data
  const scriptTypeEnums = enumUtils.enumToMessages(ScriptType).filter(item => item.value !== ScriptType.MOCK_APIS);
  const execStatusEnums = enumUtils.enumToMessages(ExecStatus);

  // Initialize chart group settings
  chartGroupSettings.value[0].type = scriptTypeEnums;
  chartGroupSettings.value[1].type = execStatusEnums;

  /**
   * Initialize script type data with colors and default values
   */
  const initializeScriptTypeData = () => {
    scriptTypeData.value = scriptTypeEnums.map(item => {
      const color = getScriptTypeColor(item.value);
      scriptTypeColors.value.push(color);
      return { name: item.message, value: 0 };
    });
  };

  /**
   * Initialize execution status data with colors and default values
   */
  const initializeStatusData = () => {
    statusData.value = execStatusEnums.map(item => {
      const color = getStatusColor(item.value);
      statusColors.value.push(color);
      return { name: item.message, value: 0 };
    });
  };

  /**
   * Get color for script type based on enum value
   */
  const getScriptTypeColor = (type: string): string => {
    const colorMap: Record<string, string> = {
      TEST_PERFORMANCE: 'rgba(45,142,255, 1)',
      TEST_FUNCTIONALITY: 'rgba(82,196,26, 1)',
      TEST_STABILITY: 'rgba(255, 185, 37, 1)',
      TEST_CUSTOMIZATION: 'rgba(251, 129, 255, 1)',
      MOCK_DATA: 'rgba(191, 199, 255, 1)'
    };
    return colorMap[type] || 'rgba(128, 128, 128, 1)';
  };

  /**
   * Get color for execution status based on enum value
   */
  const getStatusColor = (status: string): string => {
    const colorMap: Record<string, string> = {
      CREATED: 'rgba(45,142,255, 1)',
      PENDING: 'rgba(255,165,43, 1)',
      RUNNING: 'rgba(103,215,255, 1)',
      STOPPED: 'rgba(217, 217, 217, 1)',
      FAILED: 'rgba(245,34,45, 1)',
      COMPLETED: 'rgba(82,196,26, 1)',
      TIMEOUT: 'rgba(201,119,255, 1)'
    };
    return colorMap[status] || 'rgba(128, 128, 128, 1)';
  };

  /**
   * Load chart data from API
   */
  const loadChartData = async () => {
    const params = {
      'aggregates[0].column': 'id',
      'aggregates[0].function': 'COUNT',
      groupBy: 'STATUS',
      name: 'Exec',
      groupByColumns: chartGroupSettings.value.map(item => item.key),
      projectId
    };

    const [error, { data }] = await analysis.getCustomizationSummary(params);
    isChartLoading.value = false;

    if (error) {
      return;
    }

    pieChartData.value = processChartData(chartGroupSettings.value, data);
    updateChartData();
  };

  /**
   * Process raw API data into chart format
   */
  const processChartData = (groupSettings: PieSetting[], data: any): PieData[] => {
    const dataSource: PieData[] = [];

    for (let i = 0; i < groupSettings.length; i++) {
      const column = groupSettings[i];
      const res = data[column.key];

      if (!res || !Object.keys(res).length) {
        dataSource.push(createEmptyDataSource(column));
        continue;
      }

      if (['script_type', 'status'].includes(column.key)) {
        setEnumDataSource(column, res, dataSource);
      }
    }

    return dataSource;
  };

  /**
   * Create empty data source for missing data
   */
  const createEmptyDataSource = (column: PieSetting): PieData => ({
    key: column.key,
    title: column.value,
    total: 0,
    color: [],
    legend: column.type,
    data: []
  });

  /**
   * Set enum data source with proper formatting
   */
  const setEnumDataSource = (column: PieSetting, res: any, dataSource: PieData[]) => {
    const data: { name: string; value: number; codes?: number }[] = [];
    let total = 0;

    for (let j = 0; j < column.type.length; j++) {
      const key = column.type[j].value;
      if (res[key]) {
        const item = {
          name: column.type[j]?.message,
          value: +res[key]?.COUNT_id
        };
        data.push(item);
        total = +res[key]?.TOTAL_COUNT_id;
      } else {
        data.push({ name: column.type[j]?.message, value: 0 });
      }
    }

    dataSource.push({
      key: column.key,
      title: column.value,
      total,
      color: [],
      legend: column.type,
      data: data.length ? data : []
    });
  };

  /**
   * Update chart data with processed values
   */
  const updateChartData = () => {
    const scriptTypeChartData = pieChartData.value[0]?.data;
    if (scriptTypeChartData?.length) {
      for (let i = 0; i < scriptTypeData.value.length; i++) {
        scriptTypeData.value[i].value = scriptTypeChartData[i]?.value || 0;
      }
    }

    const statusChartData = pieChartData.value[1]?.data;
    if (statusChartData?.length) {
      for (let i = 0; i < statusData.value.length; i++) {
        statusData.value[i].value = statusChartData[i]?.value || 0;
      }
    }
  };

  // Initialize data on setup
  initializeScriptTypeData();
  initializeStatusData();

  return {
    // State
    isChartLoading,
    pieChartData,
    scriptTypeData,
    scriptTypeColors,
    statusData,
    statusColors,

    // Methods
    loadChartData
  };
}
