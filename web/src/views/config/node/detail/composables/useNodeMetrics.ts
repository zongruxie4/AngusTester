import { reactive, ref } from 'vue';
import dayjs from 'dayjs';
import { nodeCtrl } from '@/api/ctrl';
import { formatBytes } from '@/utils/common';
import { NetworkDeviceData, ResourceUsage } from '../types';
import { i18n } from '@xcan-angus/infra';

const I18nInstance = i18n.getI18n();
const t = I18nInstance?.global?.t || ((value: string): string => value);

// Resource usage progress configuration for monitoring display
export const nodeUseProgresses = [
  {
    label: t('node.nodeItem.interface.nodeUseProgresses.cpu'),
    value: 90,
    valueKey: 'cpu',
    totalKey: 'cpuTotal',
    percentValue: 'cpuPercent',
    unit: '%'
  },
  {
    label: t('node.nodeItem.interface.nodeUseProgresses.memory'),
    value: 60,
    valueKey: 'memory',
    totalKey: 'memoryTotal',
    percentValue: 'memoryPercent',
    unit: ''
  },
  {
    label: t('node.nodeItem.interface.nodeUseProgresses.fileSystem'),
    value: 45,
    valueKey: 'disk',
    totalKey: 'diskTotal',
    percentValue: 'diskPercent',
    unit: ''
  },
  {
    label: t('node.nodeItem.interface.nodeUseProgresses.swapArea'),
    value: 45,
    valueKey: 'swap',
    totalKey: 'swapTotal',
    percentValue: 'swapPercent',
    unit: ''
  },
  {
    label: t('node.nodeItem.interface.nodeUseProgresses.network'),
    value: 29,
    valueKey: 'network',
    totalKey: 'network',
    percentValue: 'networkPercent'
  }
];

// Network information display configuration
export const internetInfo = [
  {
    label: t('actions.upload') + ': ',
    valueKey: 'txBytesRate',
    unit: 'MB/s'
  },
  {
    label: ' ' + t('common.download') + ': ',
    valueKey: 'rxBytesRate',
    unit: 'MB/s'
  },
  {
    label: t('node.nodeItem.interface.nodeUseProgresses.totalUpload') + ': ',
    valueKey: 'txBytes',
    unit: ''
  },
  {
    label: t('node.nodeItem.interface.nodeUseProgresses.totalDownload') + ': ',
    valueKey: 'rxBytes',
    unit: ''
  }
];

/**
 * Get stroke color based on percentage value
 * <p>
 * Returns appropriate color for progress bars based on percentage thresholds.
 * </p>
 */
export const getStrokeColor = (percent: number) => {
  if (percent > 85) {
    return '#F5222D'; // Red for high usage
  }
  if (percent >= 65) {
    return '#FFB925'; // Yellow for medium usage
  }
  if (percent > 0) {
    return '#52C41A'; // Green for low usage
  }
  return '#D9D9D9'; // Default gray
};

/**
 * Composable for managing node metrics and resource monitoring
 * <p>
 * Provides reactive state management for resource usage metrics,
 * network device data, and real-time monitoring functionality.
 * </p>
 */
export function useNodeMetrics () {
  // Network device data state
  const networkDeviceData = ref<NetworkDeviceData[]>([]);
  const currentDeviceName = ref<string>();
  let networkDatatime = '';

  // Resource usage state
  const sourceUse = reactive<ResourceUsage>({
    cpu: 0,
    cpuPercent: 0,
    cpuTotal: 0,
    memory: '0',
    memoryPercent: 0,
    memoryTotal: '0',
    swap: '0',
    swapPercent: 0,
    swapTotal: '0',
    disk: '0',
    diskPercent: 0,
    diskTotal: '0',
    txBytesRate: 0,
    rxBytesRate: 0,
    rxBytes: '0',
    txBytes: '0'
  });

  /**
   * Load latest resource metrics from API
   * <p>
   * Fetches CPU, memory, and filesystem metrics and updates the resource usage state.
   * Handles offline detection based on timestamp comparison.
   * </p>
   */
  const loadMetrics = async (nodeId: string) => {
    try {
      const [error, res] = await nodeCtrl.getLatestMetrics(nodeId);
      if (error) {
        return;
      }

      const { cvsCpu, cvsFilesystem, cvsMemory } = res.data || {};

      // Process CPU metrics: cvsCpu => idle,sys,user,wait,other,total
      if (cvsCpu) {
        const cpu = +(+cvsCpu.split(',')[5]).toFixed(2);
        const cpuPercent = cpu;
        const cpuTotal = +(100 - cpu).toFixed(2);
        sourceUse.cpu = cpu;
        sourceUse.cpuPercent = cpuPercent;
        sourceUse.cpuTotal = cpuTotal;
      }

      // Process memory metrics: cvsMemory => free,used,freePercent,usedPercent,actualFree,actualUsed,actualFreePercent,actualUsedPercent,swapFree,swapUsed
      if (cvsMemory) {
        const cvsMemoryValues = cvsMemory.split(',');
        const memoryPercent = +(+cvsMemoryValues[7]).toFixed(2);
        const memory = +(+cvsMemoryValues[5]).toFixed(2);
        const memoryTotal = +(+cvsMemoryValues[4] + +cvsMemoryValues[5]).toFixed(2);

        sourceUse.memory = formatBytes(memory, 2);
        sourceUse.memoryPercent = memoryPercent;
        sourceUse.memoryTotal = formatBytes(memoryTotal, 2);

        // Process swap area metrics
        const swapTotal = (+cvsMemoryValues[9] || 0) + (+cvsMemoryValues[8] || 0);
        const swapPercent = +((+cvsMemoryValues[9] || 0) / (+swapTotal || 1) * 100).toFixed(2);
        const swap = (+cvsMemoryValues[9] || 0);

        sourceUse.swapTotal = formatBytes(swapTotal, 2);
        sourceUse.swapPercent = swapPercent;
        sourceUse.swap = formatBytes(swap, 2);
      }

      // Process filesystem metrics: cvsFilesystem => free,used,avail,usePercent
      if (cvsFilesystem) {
        const cvsFilesystems = cvsFilesystem.split(',');
        const disk = +(+cvsFilesystems[1]).toFixed(2);
        const diskPercent = +(+cvsFilesystems[3]).toFixed(2);
        const diskTotal = +(Number(cvsFilesystems[0]) + Number(cvsFilesystems[1])).toFixed(2);

        sourceUse.disk = formatBytes(disk, 2);
        sourceUse.diskPercent = diskPercent;
        sourceUse.diskTotal = formatBytes(diskTotal, 2);
      }

      // Check if node is offline based on timestamp
      if (res.data) {
        const timestamp = res.data?.timestamp;
        const datetime = res.datetime;

        if (dayjs(timestamp).add(30, 'second') < dayjs(datetime)) {
          // Reset metrics if node is offline
          sourceUse.cpu = 0;
          sourceUse.cpuPercent = 0;
          sourceUse.cpuTotal = 0;
          sourceUse.memory = '0';
          sourceUse.memoryPercent = 0;
          sourceUse.memoryTotal = '0';
          sourceUse.disk = '0';
          sourceUse.diskPercent = 0;
          sourceUse.diskTotal = '0';
        }
      }
    } catch (error) {
      console.error('Failed to load metrics:', error);
    }
  };

  /**
   * Load network information metrics from API
   * <p>
   * Fetches network device data and sets the current device for monitoring.
   * </p>
   */
  const loadNetwork = async (nodeId: string) => {
    try {
      const [error, res] = await nodeCtrl.getNetworkInfoMetrics(nodeId);
      if (error) {
        return;
      }

      networkDeviceData.value = res.data || [];
      networkDatatime = res.datetime;

      // Set default device if none selected or current device not found
      if (!currentDeviceName.value || !networkDeviceData.value.find(item => item.deviceName === currentDeviceName.value)) {
        currentDeviceName.value = networkDeviceData.value[0]?.deviceName;
      }

      if (!currentDeviceName.value) {
        return;
      }

      setNetworkData();
    } catch (error) {
      console.error('Failed to load network metrics:', error);
    }
  };

  /**
   * Set network data for current device
   * <p>
   * Processes network usage data for the selected network device and updates
   * the resource usage state with network metrics.
   * </p>
   */
  const setNetworkData = () => {
    const currentNetworkUsage = networkDeviceData.value.find(item => item.deviceName === currentDeviceName.value);
    const { cvsValue } = currentNetworkUsage?.networkUsage || {};

    if (!cvsValue) {
      return;
    }

    const cvsValues = cvsValue.split(',');

    // Update network metrics: rxBytesRate (download), txBytesRate (upload)
    sourceUse.rxBytesRate = +(+cvsValues[1]);
    sourceUse.txBytesRate = +(+cvsValues[4]);
    sourceUse.rxBytes = formatBytes(+cvsValues[0], 2);
    sourceUse.txBytes = formatBytes(+cvsValues[3], 2);

    // Check if network device is offline
    if (currentNetworkUsage?.networkUsage) {
      const timestamp = currentNetworkUsage?.networkUsage?.timestamp;
      const datetime = networkDatatime;

      if (dayjs(timestamp).add(30, 'second') < dayjs(datetime)) {
        sourceUse.rxBytesRate = 0;
        sourceUse.txBytesRate = 0;
        sourceUse.rxBytes = '0';
        sourceUse.txBytes = '0';
      }
    }
  };

  /**
   * Handle network device name change
   * <p>
   * Updates the current network device and refreshes network data display.
   * </p>
   */
  const onDeviceNameChange = (value: string) => {
    currentDeviceName.value = value;
    setNetworkData();
  };

  /**
   * Get disk names for chart filtering
   * <p>
   * Fetches available disk devices for the node to enable disk-specific monitoring.
   * </p>
   */
  const getDiskNames = async (nodeId: string) => {
    try {
      const [error, res] = await nodeCtrl.getDiskInfoMetrics(nodeId);
      if (error) {
        return [];
      }

      const diskNames: { label: string; value: string }[] = [];
      const deviceNameMap: Record<string, boolean> = {};

      for (const item of (res.data || [])) {
        if (!deviceNameMap[item.deviceName]) {
          diskNames.push({ label: item.deviceName, value: item.deviceName });
          deviceNameMap[item.deviceName] = true;
        }
      }

      return diskNames;
    } catch (error) {
      console.error('Failed to get disk names:', error);
      return [];
    }
  };

  /**
   * Get network names for chart filtering
   * <p>
   * Fetches available network devices for the node to enable network-specific monitoring.
   * </p>
   */
  const getNetworkNames = async (nodeId: string) => {
    try {
      const [error, res] = await nodeCtrl.getNetworkInfoMetrics(nodeId);
      if (error) {
        return [];
      }

      const networkNames: { label: string; value: string }[] = [];
      const networkNamesMap: Record<string, boolean> = {};

      for (const item of (res.data || [])) {
        if (!networkNamesMap[item.deviceName]) {
          networkNames.push({ label: item.deviceName, value: item.deviceName });
          networkNamesMap[item.deviceName] = true;
        }
      }

      return networkNames;
    } catch (error) {
      console.error('Failed to get network names:', error);
      return [];
    }
  };

  return {
    // State
    sourceUse,
    networkDeviceData,
    currentDeviceName,

    // Methods
    loadMetrics,
    loadNetwork,
    setNetworkData,
    onDeviceNameChange,
    getDiskNames,
    getNetworkNames
  };
}
