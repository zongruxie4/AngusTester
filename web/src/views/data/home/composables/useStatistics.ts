import { onMounted, watch } from 'vue';
import { useData } from './useData';
import { useChartConfig } from './useChartConfig';
import { useECharts } from './useECharts';

/**
 * <p>Main composable for Statistics component business logic</p>
 * <p>Integrates all other composables and provides the main component functionality</p>
 */
export function useStatistics(projectId: string, userId: string) {
  // Initialize all composables
  const data = useData(projectId, userId);
  const chartConfig = useChartConfig();
  const charts = useECharts();

  /**
   * <p>Initialize statistics component</p>
   * <p>Sets up charts and loads initial data</p>
   */
  const initializeStatistics = async (): Promise<void> => {
    // Initialize chart instances
    charts.initializeCharts();
    
    // Set initial chart options
    const variableOption = chartConfig.createVariableChartOption();
    const dataSetOption = chartConfig.createDatasetChartOption();
    const fileOption = chartConfig.createFileChartOption();
    const dataSourceOption = chartConfig.createDatasourceChartOption();
    
    charts.setChartOptions(variableOption, dataSetOption, fileOption, dataSourceOption);
    
    // Load initial data
    await data.loadAllStatistics();
    
    // Update charts with loaded data
    charts.updateChartData(data.projectStatistics);
  };

  /**
   * <p>Refresh statistics data</p>
   * <p>Reloads data and updates charts</p>
   */
  const refreshStatistics = async (): Promise<void> => {
    await data.loadAllStatistics();
    charts.updateChartData(data.projectStatistics);
  };

  // Watch for notification changes and refresh data
  watch(() => data.loading, (newLoading) => {
    if (!newLoading) {
      // Data loading completed, update charts
      charts.updateChartData(data.projectStatistics);
    }
  });

  // Initialize on component mount
  onMounted(() => {
    initializeStatistics();
  });

  return {
    // Data state and methods
    ...data,
    
    // Chart configuration
    ...chartConfig,
    
    // Chart management
    ...charts,
    
    // Business logic methods
    initializeStatistics,
    refreshStatistics
  };
}
