import { ref, watch } from 'vue';
import axios from 'axios';
import dayjs from 'dayjs';
import duration from 'dayjs/plugin/duration';
import { ApiType, ApiUrlBuilder, routerUtils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import type { AgentInfoProps, AgentInfoData, GridColumn } from '../types';

// Extend dayjs with duration plugin
dayjs.extend(duration);

/**
 * <p>Composable for managing agent info data and operations</p>
 * <p>Handles agent information retrieval, data processing, and grid configuration</p>
 *
 * @param props - Component props containing IP and port information
 * @returns Object containing agent info data and related methods
 */
export function useAgentInfo (props: AgentInfoProps) {
  const { t } = useI18n();

  /**
   * <p>Agent information data source</p>
   * <p>Contains all agent-related information for display</p>
   */
  const agentInfoData = ref<AgentInfoData>({});

  /**
   * <p>Error display state</p>
   * <p>Controls whether error messages should be shown</p>
   */
  const showError = ref(false);

  /**
   * <p>Error information text</p>
   * <p>Stores error details for display</p>
   */
  const errorInfo = ref('');

  /**
   * <p>Grid column configuration</p>
   * <p>Defines the structure and layout of the information grid</p>
   */
  const gridColumns: GridColumn[][] = [
    [
      {
        dataIndex: 'version',
        label: t('common.version')
      },
      {
        dataIndex: 'home',
        label: t('node.nodeDetail.agentInfo.columns.home')
      }
    ],
    [
      {
        dataIndex: 'uptime',
        label: t('node.nodeDetail.agentInfo.columns.uptime')
      },
      {
        dataIndex: 'diskSpace',
        label: t('node.nodeDetail.agentInfo.columns.diskSpace')
      }
    ],
    [
      {
        dataIndex: 'port',
        label: t('node.nodeDetail.agentInfo.columns.port')
      },
      {
        dataIndex: 'health',
        label: t('node.nodeDetail.agentInfo.columns.health')
      }
    ]
  ];

  /**
   * <p>Closes error information display</p>
   * <p>Hides error messages from the user interface</p>
   */
  const closeErrorInfo = (): void => {
    showError.value = false;
  };

  /**
   * <p>Formats uptime duration into human-readable format</p>
   * <p>Converts milliseconds to hours, minutes, and seconds with localization</p>
   *
   * @param uptimeMs - Uptime in milliseconds
   * @returns Formatted uptime string
   */
  const formatUptime = (uptimeMs: number): string => {
    const durationObj = dayjs.duration(uptimeMs);
    const hours = durationObj.hours();
    const minutes = durationObj.minutes();
    const seconds = durationObj.seconds();

    let result = '';
    if (hours) {
      result += `${hours}${t('node.nodeDetail.agentInfo.timeUnits.hour')}`;
    }
    if (minutes) {
      result += `${minutes}${t('node.nodeDetail.agentInfo.timeUnits.minute')}`;
    }
    if (seconds) {
      result += `${seconds}${t('node.nodeDetail.agentInfo.timeUnits.second')}`;
    }

    return result;
  };

  /**
   * <p>Loads agent information from the API</p>
   * <p>Retrieves agent details including version, uptime, disk space, and health status</p>
   *
   * @returns Promise that resolves when agent info loading is complete
   */
  const loadAgentInfo = async (): Promise<void> => {
    if (!props.ip) {
      return;
    }

    try {
      // Ensure port has a default value if not provided
      const port = props.port || '6807';

      const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.PUB_API);
      const url = ApiUrlBuilder.buildApiUrl(routeConfig, `/proxy?targetAddr=http://${props.ip}:${port}`);

      const response = await axios.get(url, { timeout: 2000 });
      const data = response.data;

      // Process and store agent information
      agentInfoData.value = {
        version: data.version,
        home: data.home,
        uptime: formatUptime(+data.uptime),
        diskSpace: data.diskSpace,
        ip: data.server?.ip,
        port: data.server?.port,
        health: data.health?.status?.status || data.health // 'UP' | ''
      };

      // Handle health status and error details
      if (agentInfoData.value.health !== 'UP') {
        const errorObj = data.health?.detail || {};
        agentInfoData.value.errorTip = Object.values(errorObj).join(',');
      }

      showError.value = false;
    } catch (err: any) {
      showError.value = true;

      // Extract error information from response
      if (err.response && err.response.data) {
        errorInfo.value = err.response.data;
      } else if (err.message) {
        errorInfo.value = err.message;
      } else {
        errorInfo.value = '';
      }
    }
  };

  /**
   * <p>Refreshes agent information</p>
   * <p>Reloads agent data from the API</p>
   */
  const refreshAgentInfo = (): void => {
    loadAgentInfo();
  };

  // Watch for changes in IP address to reload agent info
  watch(() => props.ip, async (newValue) => {
    if (newValue) {
      await loadAgentInfo();
    }
  }, {
    immediate: true
  });

  return {
    // State
    agentInfoData,
    showError,
    errorInfo,

    // Configuration
    gridColumns,

    // Methods
    loadAgentInfo,
    refreshAgentInfo,
    closeErrorInfo
  };
}
