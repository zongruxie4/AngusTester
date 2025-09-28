import { ref, reactive, computed, watch, onBeforeUnmount } from 'vue';
import { notification } from '@xcan-angus/vue-ui';
import axios from 'axios';
import { ApiType, ApiUrlBuilder, routerUtils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import type { AgentLogProps, LogFileOption, LogTextParams } from '../types';

/**
 * <p>Composable for managing agent log data and operations</p>
 * <p>Handles log file listing, log content retrieval, and log download functionality</p>
 *
 * @param props - Component props containing IP and port information
 * @returns Object containing log data and related methods
 */
export function useAgentLog (props: AgentLogProps) {
  const { t } = useI18n();

  /**
   * <p>Log file list options</p>
   * <p>Contains available log files for selection</p>
   */
  const fileList = ref<LogFileOption[]>([]);

  /**
   * <p>Log content text</p>
   * <p>Stores the retrieved log content for display</p>
   */
  const logContent = ref('');

  /**
   * <p>Auto-refresh state</p>
   * <p>Controls whether log content should be automatically refreshed</p>
   */
  const isAutoRefresh = ref(false);

  /**
   * <p>Error display state</p>
   * <p>Controls whether error messages should be shown</p>
   */
  const showError = ref(false);

  /**
   * <p>Error text content</p>
   * <p>Stores error messages for display</p>
   */
  const errorText = ref('');

  /**
   * <p>Full screen state</p>
   * <p>Controls whether the log viewer is in full screen mode</p>
   */
  const isFullScreen = ref(false);

  /**
   * <p>Log text parameters for API calls</p>
   * <p>Contains log file name, tail option, and line count</p>
   */
  const logTextParams = reactive<LogTextParams>({
    logName: '',
    tail: true,
    linesNum: '500'
  });

  /**
   * <p>Log file line count options</p>
   * <p>Provides predefined options for number of lines to retrieve</p>
   */
  const lineCountOptions = computed<LogFileOption[]>(() => [
    {
      value: '500',
      message: t('node.detail.agent.log.options.500')
    },
    {
      value: '1000',
      message: t('node.detail.agent.log.options.1000')
    },
    {
      value: '10000',
      message: t('node.detail.agent.log.options.10000')
    }
  ]);

  /**
   * <p>Full screen icon computed property</p>
   * <p>Returns the appropriate icon based on full screen state</p>
   */
  const fullScreenIcon = computed(() => {
    return isFullScreen.value ? 'icon-tuichuzuida' : 'icon-zuidahua';
  });

  /**
   * <p>Timer reference for auto-refresh</p>
   * <p>Stores the timer ID for cleanup</p>
   */
  let refreshTimer: NodeJS.Timeout | null = null;

  /**
   * <p>Loads the list of available log files</p>
   * <p>Retrieves log file names from the agent API</p>
   *
   * @returns Promise that resolves when file list loading is complete
   */
  const loadFileList = async (): Promise<void> => {
    try {
      // Ensure port has a default value if not provided
      const port = (props.port || '6807') as string;

      const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.PUB_API);
      const host = ApiUrlBuilder.buildApiUrl(routeConfig, '');
      const url = `${host}/proxy/actuator/log/names?filePrefix=agent&targetAddr=http://${props.ip}:${port}`;

      const response = await axios.get(url);
      const { data } = response;

      fileList.value = (data || []).map((fileName: string) => ({
        label: fileName,
        value: fileName
      }));

      if (data.length > 0) {
        logTextParams.logName = data[0];
      }

      showError.value = false;
    } catch (err: any) {
      showError.value = true;
      if (err.response?.data) {
        errorText.value = err.response.data.msg;
      } else if (err.message) {
        errorText.value = err.message;
      } else {
        errorText.value = undefined;
      }
    }
  };

  /**
   * <p>Loads log content from the selected log file</p>
   * <p>Retrieves log content based on current parameters</p>
   *
   * @returns Promise that resolves when log content loading is complete
   */
  const loadLogContent = async (): Promise<void> => {
    const { logName, tail, linesNum } = logTextParams;
    if (!logName) {
      return;
    }

    try {
      // Ensure port has a default value if not provided
      const port = (props.port || '6807') as string;

      const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.PUB_API);
      const host = ApiUrlBuilder.buildApiUrl(routeConfig, '');
      const url = `${host}/proxy/actuator/log/${logName}?tail=${tail}&linesNum=${linesNum}&targetAddr=http://${props.ip}:${port}`;

      const response = await axios.get(url);
      const { data } = response;

      logContent.value = data;
      showError.value = false;

      // Set up auto-refresh timer if enabled
      if (isAutoRefresh.value) {
        refreshTimer = setTimeout(() => {
          loadLogContent();
        }, 3000);
      }
    } catch (err: any) {
      showError.value = true;
      if (err.response?.data) {
        errorText.value = err.response.data.msg;
      } else if (err.message) {
        errorText.value = err.message;
      } else {
        errorText.value = undefined;
      }
    }
  };

  /**
   * <p>Downloads the current log content as a text file</p>
   * <p>Creates a downloadable blob from the log content</p>
   */
  const downloadLog = (): void => {
    if (!logContent.value) {
      notification.warning(t('node.detail.agent.log.noDownloadContent'));
      return;
    }

    const blob = new Blob([logContent.value], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);

    const downloadLink = document.createElement('a');
    downloadLink.style.display = 'none';
    downloadLink.href = url;
    downloadLink.download = 'log.text';
    document.body.appendChild(downloadLink);
    downloadLink.click();
    document.body.removeChild(downloadLink);

    window.URL.revokeObjectURL(url);
  };

  /**
   * <p>Toggles full screen mode</p>
   * <p>Switches between normal and full screen viewing modes</p>
   */
  const toggleFullScreen = (): void => {
    isFullScreen.value = !isFullScreen.value;
  };

  /**
   * <p>Stops auto-refresh</p>
   * <p>Clears the refresh timer and disables auto-refresh</p>
   */
  const stopAutoRefresh = (): void => {
    if (refreshTimer) {
      clearTimeout(refreshTimer);
      refreshTimer = null;
    }
    isAutoRefresh.value = false;
  };

  /**
   * <p>Starts auto-refresh</p>
   * <p>Enables automatic log content refresh every 3 seconds</p>
   */
  const startAutoRefresh = (): void => {
    isAutoRefresh.value = true;
    loadLogContent();
  };

  // Watch for changes in log parameters to reload content
  watch(() => logTextParams, () => {
    if (logTextParams.logName) {
      if (refreshTimer) {
        clearTimeout(refreshTimer);
        refreshTimer = null;
      }
      loadLogContent();
    }
  }, {
    immediate: true,
    deep: true
  });

  // Watch for changes in IP address to reload file list
  watch(() => props.ip, (newValue) => {
    if (newValue) {
      loadFileList();
    }
  }, {
    immediate: true
  });

  // Cleanup timer on component unmount
  onBeforeUnmount(() => {
    if (refreshTimer) {
      clearTimeout(refreshTimer);
      refreshTimer = null;
    }
  });

  return {
    // State
    fileList,
    logContent,
    isAutoRefresh,
    showError,
    errorText,
    isFullScreen,
    logTextParams,

    // Computed
    lineCountOptions,
    fullScreenIcon,

    // Methods
    loadFileList,
    loadLogContent,
    downloadLog,
    toggleFullScreen,
    stopAutoRefresh,
    startAutoRefresh
  };
}
