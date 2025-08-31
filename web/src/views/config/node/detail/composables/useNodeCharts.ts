import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { TESTER } from '@xcan-angus/infra';
import { nodeCtrl } from '@/api/ctrl';
import { EChartsManager } from '../echartsManager';
import { ActiveTabKey, ChartDataOption, ChartParams, ChartServerMap } from '../types';

// Chart tabs configuration for resource monitoring
export const nodeEchartsTabs = [
  {
    label: 'CPU',
    value: 90,
    valueKey: 'cpu',
    totalKey: 'cpuTotal',
    percentValue: 'cpuPercent',
    unit: '%'
  },
  {
    label: '内存',
    value: 60,
    valueKey: 'memory',
    totalKey: 'memoryTotal',
    percentValue: 'memoryPercent',
    unit: ''
  },
  {
    label: '磁盘',
    value: 45,
    valueKey: 'disk',
    totalKey: 'diskTotal',
    percentValue: 'diskPercent',
    unit: ''
  },
  {
    label: '网络',
    value: 29,
    valueKey: 'network',
    totalKey: 'network',
    percentValue: 'networkPercent'
  }
];

// Time options for chart data selection
export const timeOpt = [
  {
    value: '3-minute',
    label: '最近3分钟'
  },
  {
    value: '5-minute',
    label: '最近5分钟'
  },
  {
    value: '10-minute',
    label: '最近10分钟'
  },
  {
    value: '1-hour',
    label: '最近1小时'
  },
  {
    value: '3-hour',
    label: '最近3小时'
  }
];

// Chart data columns configuration
export const columns = [
  {
    title: '名称',
    dataIndex: 'name',
    key: 'name'
  },
  {
    title: '平均值',
    dataIndex: 'average',
    key: 'average'
  },
  {
    title: '最高',
    dataIndex: 'high',
    key: 'high'
  },
  {
    title: '最低',
    dataIndex: 'low',
    key: 'low'
  },
  {
    title: '最新值',
    dataIndex: 'latest',
    key: 'latest'
  }
];

/**
 * Composable for managing node charts and monitoring displays
 * <p>
 * Provides reactive state management for chart configurations, data loading,
 * and chart display options for different resource types.
 * </p>
 */
export function useNodeCharts (nodeId: string, echartsManager: EChartsManager) {
  const { t } = useI18n();

  // Chart configuration state
  const loadingChart = ref(false);
  const currentSourceServer = ref<string>();
  const chartParams = ref<ChartParams>({ orderSort: 'ASC' });
  const activeTab = ref<ActiveTabKey>('cpu');
  const notMerge = ref(true);
  const showNoData = ref(false);

  // Chart data options
  const memoryDataOptions: ChartDataOption[] = [
    { label: t('node.nodeDetail.chartOptions.memory.usage'), value: false },
    { label: t('node.nodeDetail.chartOptions.memory.usageRate'), value: true }
  ];

  const diskDataOptions: ChartDataOption[] = [
    { label: t('node.nodeDetail.chartOptions.disk.iops'), value: 'rate' },
    { label: t('node.nodeDetail.chartOptions.disk.mbPerSecond'), value: 'bytesRate' }
  ];

  const networkDataOptions: ChartDataOption[] = [
    { label: t('node.nodeDetail.chartOptions.network.mbPerSecond'), value: 'network' },
    { label: t('node.nodeDetail.chartOptions.network.bytes'), value: 'bytes' },
    { label: t('node.nodeDetail.chartOptions.network.errorPackets'), value: 'packet' }
  ];

  // Chart display options
  const showMemoryPercentChart = ref(false);
  const diskChartKey = ref('rate');
  const networkChartKey = ref('network');

  // Device selection state
  const diskNames = ref<{ label: string; value: string }[]>([]);
  const activeDisk = ref<string>();
  const networkNames = ref<{ label: string; value: string }[]>([]);
  const activeNetwork = ref<string>();

  // Chart server mapping
  const chartServersMap: ChartServerMap = {
    cpu: `${TESTER}/node/${nodeId}/metrics/cpu`,
    memory: `${TESTER}/node/${nodeId}/metrics/memory`,
    disk: `${TESTER}/node/${nodeId}/metrics/disk`,
    network: `${TESTER}/node/${nodeId}/metrics/network`
  };

  /**
   * Handle chart data change events
   * <p>
   * Processes incoming chart data and updates the appropriate chart display
   * based on the currently active tab and chart options.
   * </p>
   */
  const onChartDataChange = (data: any) => {
    // Handle different data formats that might be passed from IntervalTimestamp
    let chartData: any[] = [];

    if (data && typeof data === 'object') {
      // If data is ChartDataChangeEvent format
      if ('data' in data && Array.isArray(data.data)) {
        chartData = data.data;
      }
      // If data is directly an array
      else if (Array.isArray(data)) {
        chartData = data;
      }
    }

    // Set chart data in manager
    echartsManager.setChartsData(chartData);

    // Update no data flag
    if (!chartData || chartData.length === 0) {
      showNoData.value = true;
    } else {
      showNoData.value = false;
    }

    // Update chart based on active tab
    switch (activeTab.value) {
      case 'cpu':
        echartsManager.loadCpuEchartData(chartData, t, notMerge.value);
        break;
      case 'memory':
        echartsManager.loadMemoryEchartData(chartData, t, showMemoryPercentChart.value, notMerge.value);
        break;
      case 'disk':
        echartsManager.loadDiskEchartData(chartData, t, diskChartKey.value, notMerge.value);
        break;
      case 'network':
        echartsManager.loadNetworkEchartData(chartData, t, networkChartKey.value, notMerge.value);
        break;
    }
  };

  /**
   * Load chart data based on active tab and selected devices
   * <p>
   * Configures chart parameters and server endpoints based on the selected
   * resource type and device filters.
   * </p>
   */
  const loadEchartData = async () => {
    try {
      switch (activeTab.value) {
        case 'cpu':
          currentSourceServer.value = chartServersMap.cpu;
          chartParams.value = { orderSort: 'ASC' };
          break;

        case 'memory':
          currentSourceServer.value = chartServersMap.memory;
          chartParams.value = { orderSort: 'ASC' };
          break;

        case 'disk':
          if (!diskNames.value.length) {
            const diskNamesData = await getDiskNames();
            diskNames.value = diskNamesData;
            activeDisk.value = diskNames.value?.[0]?.value;
          }

          chartParams.value = {
            orderSort: 'ASC',
            filters: [{ key: 'deviceName', op: 'EQUAL', value: activeDisk.value || '' }]
          };
          currentSourceServer.value = chartServersMap.disk;
          break;

        case 'network':
          if (!networkNames.value.length) {
            const networkNamesData = await getNetworkNames();
            networkNames.value = networkNamesData;
            activeNetwork.value = networkNames.value?.[0]?.value;
          }

          chartParams.value = {
            orderSort: 'ASC',
            filters: [{ key: 'deviceName', op: 'EQUAL', value: activeNetwork.value || '' }]
          };
          currentSourceServer.value = chartServersMap.network;
          break;
      }
    } catch (error) {
      console.error('Failed to load chart data:', error);
    }
  };

  /**
   * Get disk names for chart filtering
   * <p>
   * Fetches available disk devices and updates the disk names state.
   * </p>
   */
  const getDiskNames = async (): Promise<{ label: string; value: string }[]> => {
    try {
      const [error, res] = await nodeCtrl.getDiskInfoMetrics(nodeId);
      if (error) {
        return [];
      }

      const diskNamesData: { label: string; value: string }[] = [];
      const deviceNameMap: Record<string, boolean> = {};

      for (const item of (res.data || [])) {
        if (!deviceNameMap[item.deviceName]) {
          diskNamesData.push({ label: item.deviceName, value: item.deviceName });
          deviceNameMap[item.deviceName] = true;
        }
      }

      return diskNamesData;
    } catch (error) {
      console.error('Failed to get disk names:', error);
      return [];
    }
  };

  /**
   * Get network names for chart filtering
   * <p>
   * Fetches available network devices and updates the network names state.
   * </p>
   */
  const getNetworkNames = async (): Promise<{ label: string; value: string }[]> => {
    try {
      const [error, res] = await nodeCtrl.getNetworkInfoMetrics(nodeId);
      if (error) {
        return [];
      }

      const networkNamesData: { label: string; value: string }[] = [];
      const networkNamesMap: Record<string, boolean> = {};

      for (const item of (res.data || [])) {
        if (!networkNamesMap[item.deviceName]) {
          networkNamesData.push({ label: item.deviceName, value: item.deviceName });
          networkNamesMap[item.deviceName] = true;
        }
      }

      return networkNamesData;
    } catch (error) {
      console.error('Failed to get network names:', error);
      return [];
    }
  };

  /**
   * Update chart display when options change
   * <p>
   * Refreshes chart display when memory, disk, or network chart options are modified.
   * </p>
   */
  const updateChartDisplay = () => {
    echartsManager.updateChartDisplay(
      activeTab.value,
      showMemoryPercentChart.value,
      diskChartKey.value,
      networkChartKey.value
    );
  };

  // Watch for chart option changes
  watch(() => showMemoryPercentChart.value, updateChartDisplay);
  watch(() => diskChartKey.value, updateChartDisplay);
  watch(() => networkChartKey.value, updateChartDisplay);

  // Watch for tab and device changes
  watch([() => activeTab.value, () => activeDisk.value, () => activeNetwork.value], () => {
    // Only load chart data if we have a valid currentSourceServer
    if (currentSourceServer.value) {
      notMerge.value = true;
      loadEchartData();
    }
  });

  return {
    // State
    loadingChart,
    currentSourceServer,
    chartParams,
    activeTab,
    notMerge,
    showNoData,
    memoryDataOptions,
    diskDataOptions,
    networkDataOptions,
    showMemoryPercentChart,
    diskChartKey,
    networkChartKey,
    diskNames,
    activeDisk,
    networkNames,
    activeNetwork,
    chartServersMap,

    // Methods
    onChartDataChange,
    loadEchartData,
    getDiskNames,
    getNetworkNames,
    updateChartDisplay
  };
}
