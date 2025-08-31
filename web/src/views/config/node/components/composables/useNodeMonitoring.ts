import { watch, onBeforeUnmount } from 'vue';
import dayjs from 'dayjs';

import { NodeData, NodeItemsProps } from '../types';
import { nodeCtrl } from '@/api/ctrl';

/**
 * Composable for managing node monitoring data
 *
 * @param props - Component props containing auto-refresh configuration
 * @param nodeList - Reactive node list reference
 * @returns Object containing monitoring functions and state
 */
export function useNodeMonitoring (
  props: NodeItemsProps,
  nodeList: any
) {
  // Timer reference for interval management
  let timer: NodeJS.Timeout | undefined;

  /**
   * Loads network information metrics for a specific node
   *
   * @param id - Node ID to load network metrics for
   * @returns Promise resolving to network metrics response
   */
  const loadNodeNetworkMetrics = (id: string) => {
    return nodeCtrl.getNetworkInfoMetrics(id);
  };

  /**
   * Loads resource usage metrics for a specific node
   *
   * @param id - Node ID to load resource metrics for
   * @returns Promise resolving to resource metrics response
   */
  const loadNodeResourceMetrics = (id: string) => {
    return nodeCtrl.getLatestMetrics(id);
  };

  /**
   * Processes network metrics data and updates node monitoring data
   *
   * <p>Extracts network usage information including upload/download rates
   * and validates data freshness based on timestamps.</p>
   */
  const loadNodeMetricsNetwork = async () => {
    const nodesWithId = nodeList.value.filter((node: NodeData) => node.id);

    if (nodesWithId.length === 0) return;

    try {
      const responses = await Promise.all(
        nodesWithId.map((node: NodeData) => loadNodeNetworkMetrics(node.id!))
      );

      nodeList.value.forEach((node: NodeData) => {
        if (!node.id) return;

        const [error, res] = responses.shift() || [];
        if (error) return;

        const { cvsValue = '' } = res.data?.[0]?.networkUsage || {};
        const cvsValues = cvsValue.split(',');

        if (cvsValues.length > 1) {
          // Extract network rates from CSV data
          const rxBytesRate = (+cvsValues[1] || 0); // Download rate
          const txBytesRate = (+cvsValues[4] || 0); // Upload rate

          node.monitorData = {
            ...node.monitorData,
            rxBytesRate,
            txBytesRate
          } as any;
        } else {
          // Set default values if no data available
          node.monitorData = {
            ...node.monitorData,
            rxBytesRate: 0,
            txBytesRate: 0
          } as any;
        }

        // Validate data freshness (30 second threshold)
        if (res.data?.[0]?.networkUsage) {
          const timestamp = res.data[0].networkUsage.timestamp;
          const datetime = res.datetime;

          if (dayjs(timestamp).add(30, 'second') < dayjs(datetime)) {
            node.monitorData = {
              ...node.monitorData,
              rxBytesRate: 0,
              txBytesRate: 0
            } as any;
          }
        }
      });
    } catch (error) {
      console.error('Failed to load network metrics:', error);
    }
  };

  /**
   * Processes resource metrics data and updates node monitoring data
   *
   * <p>Extracts CPU, memory, disk, and swap usage information
   * and validates data freshness based on timestamps.</p>
   */
  const loadNodeListMetrics = async () => {
    const nodesWithId = nodeList.value.filter((node: NodeData) => node.id);

    if (nodesWithId.length === 0) return;

    try {
      const responses = await Promise.all(
        nodesWithId.map((node: NodeData) => loadNodeResourceMetrics(node.id!))
      );

      nodeList.value.forEach((node: NodeData) => {
        if (!node.id) return;

        const [error, res] = responses.shift() || [];
        if (error) return;

        const { cvsCpu = '', cvsFilesystem = '', cvsMemory = '' } = res.data || {};

        // Process CPU usage data
        // Format: idle,sys,user,wait,other,total - extract total
        const cpu = (+cvsCpu.split(',')[5] || 0).toFixed(2);

        // Process memory usage data
        // Format: free,used,freePercent,usedPercent,actualFree,actualUsed,actualFreePercent,actualUsedPercent,swapFree,swapUsed
        // Extract actualUsedPercent for memory usage
        const memory = (+cvsMemory.split(',')[7] || 0).toFixed(2);

        // Process disk usage data
        // Format: free,used,avail,usePercent - extract usePercent
        const disk = (+cvsFilesystem.split(',')[3] || 0).toFixed(2);

        // Process swap usage data
        const cvsMemories = cvsMemory.split(',');
        const swapTotal = (+cvsMemories[9] || 0) + (+cvsMemories[8] || 0);
        const swap = ((+cvsMemories[9] || 0) / (+swapTotal || 1) * 100).toFixed(2);

        // Update node monitoring data
        node.monitorData = {
          ...node.monitorData,
          cpu,
          memory,
          disk,
          swap
        } as any;

        // Validate data freshness (30 second threshold)
        if (res.data) {
          const timestamp = res.data?.timestamp;
          const datetime = res.datetime;

          if (dayjs(timestamp).add(30, 'second') < dayjs(datetime)) {
            node.monitorData = {
              ...node.monitorData,
              cpu: '0',
              memory: '0',
              disk: '0',
              swap: '0'
            } as any;
          }
        }
      });
    } catch (error) {
      console.error('Failed to load resource metrics:', error);
    }
  };

  /**
   * Starts the monitoring data collection interval
   *
   * <p>Immediately loads current data and sets up interval if auto-refresh is enabled.
   * Clears any existing timer before starting new one.</p>
   */
  const startMonitoringInterval = () => {
    // Load initial data
    loadNodeListMetrics();
    loadNodeMetricsNetwork();

    // Clear existing timer
    if (timer) {
      clearInterval(timer);
    }

    // Set up new interval if auto-refresh is enabled
    if (props.autoRefresh) {
      timer = setInterval(() => {
        loadNodeListMetrics();
        loadNodeMetricsNetwork();
      }, 5000); // 5 second interval
    }
  };

  /**
   * Stops the monitoring data collection interval
   */
  const stopMonitoringInterval = () => {
    if (timer) {
      clearInterval(timer);
      timer = undefined;
    }
  };

  // Watch for changes in auto-refresh setting
  watch(() => props.autoRefresh, (newValue) => {
    if (newValue) {
      startMonitoringInterval();
    } else {
      stopMonitoringInterval();
    }
  });

  // Clean up timer on component unmount
  onBeforeUnmount(() => {
    stopMonitoringInterval();
  });

  return {
    // Methods
    loadNodeMetricsNetwork,
    loadNodeListMetrics,
    startMonitoringInterval,
    stopMonitoringInterval
  };
}
