import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { notification } from '@xcan-angus/vue-ui';
import { ApiType, routerUtils, ApiUrlBuilder } from '@xcan-angus/infra';
import axios from 'axios';

import { mock } from '@/api/tester';

/**
 * Composable for managing mock service log functionality
 * Handles log file loading, content display, and download operations
 */
export function useMockLog (id: string) {
  const { t } = useI18n();

  // Service connection info
  const ip = ref<string>();
  const port = ref('6807');

  // UI state
  const showErr = ref(false);
  const errorInfo = ref('');
  const content = ref('');
  const refresh = ref(false);
  const isFullScreen = ref(false);
  const loadingLog = ref(false);

  // Log configuration
  const logTextParam = reactive({
    logName: '',
    tail: true,
    linesNum: '500'
  });

  // File list for log selection
  const fileList = ref<{ label: string; value: string }[]>([]);

  // Timer for auto-refresh
  let timer: NodeJS.Timeout | null = null;

  /**
   * Line count options for log viewing
   */
  const linesOpt = [
    {
      value: '500',
      label: t('mock.mockDetail.log.lineOptions.last500')
    },
    {
      value: '1000',
      label: t('mock.mockDetail.log.lineOptions.last1000')
    },
    {
      value: '10000',
      label: t('mock.mockDetail.log.lineOptions.last10000')
    }
  ];

  /**
   * Computed property for fullscreen icon
   */
  const fullScreenIcon = computed(() => {
    return isFullScreen.value ? 'icon-tuichuzuida' : 'icon-zuidahua';
  });

  /**
   * Load service IP and port information
   */
  const loadIp = async () => {
    const [error, { data }] = await mock.getServiceDetail(id);
    if (error) return;

    ip.value = data.nodePublicIp || data.nodeIp;
    port.value = data.agentPort || '6807';
  };

  /**
   * Load available log file list from service
   */
  const loadFileList = async () => {
    const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.PUB_API);
    const url = ApiUrlBuilder.buildApiUrl(
      routeConfig,
      `/proxy/actuator/log/names?filePrefix=mockservice&targetAddr=http://${ip.value}:${port.value}`
    );

    try {
      const resp = await axios.get(url, {});
      const { data } = resp;

      fileList.value = (data || []).map(i => ({ label: i, value: i }));
      if (data.length) {
        logTextParam.logName = data[0];
      }
      showErr.value = false;
    } catch (err: any) {
      showErr.value = true;
      if (err.response?.data) {
        errorInfo.value = err.response?.data?.msg;
      } else if (err.message) {
        errorInfo.value = err.message;
      } else {
        errorInfo.value = '';
      }
    }
  };

  /**
   * Load log content from selected file
   */
  const loadLogContent = async () => {
    const { logName, tail, linesNum } = logTextParam;
    if (!logName) return;

    const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.PUB_API);
    const url = ApiUrlBuilder.buildApiUrl(
      routeConfig,
      `/proxy/actuator/log/${logName}?tail=${tail}&linesNum=${linesNum}&targetAddr=http://${ip.value}:${port.value}`
    );

    try {
      const resp = await axios.get(url, {});
      const { data } = resp;

      content.value = data;
      showErr.value = false;

      // Setup auto-refresh timer if enabled
      if (refresh.value) {
        timer = setTimeout(() => {
          loadLogContent();
          timer = null;
        }, 3000);
      }
    } catch (err: any) {
      showErr.value = true;
      if (err.response?.data) {
        errorInfo.value = err?.response?.data?.msg;
      } else if (err.message) {
        errorInfo.value = err.message;
      } else {
        errorInfo.value = '';
      }
    }
  };

  /**
   * Close error information display
   */
  const closeErrInfo = () => {
    showErr.value = false;
  };

  /**
   * Download current log content as text file
   */
  const downloadLog = () => {
    if (!content.value) {
      notification.warning(t('mock.mockDetail.log.notifications.noDownloadContent'));
      return;
    }

    const blob = new Blob([content.value], {
      type: 'text/plain'
    });
    const url = URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.style.display = 'none';
    a.href = url;
    a.download = 'log.text';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);

    window.URL.revokeObjectURL(url);
  };

  /**
   * Toggle fullscreen mode
   */
  const handleZoom = () => {
    isFullScreen.value = !isFullScreen.value;
  };

  // Watch for log parameter changes to reload content
  watch(() => logTextParam, () => {
    if (logTextParam.logName) {
      if (timer) {
        clearTimeout(timer);
        timer = null;
      }
      loadLogContent();
    }
  }, {
    immediate: true,
    deep: true
  });

  // Watch for IP changes to load file list
  watch(() => ip.value, async (newValue) => {
    if (newValue) {
      await loadFileList();
    }
  }, {
    immediate: true
  });

  // Cleanup timer on component unmount
  onBeforeUnmount(() => {
    if (timer) {
      clearTimeout(timer);
      timer = null;
    }
  });

  // Initialize on mount
  onMounted(async () => {
    await loadIp();
  });

  return {
    // State
    ip,
    port,
    showErr,
    errorInfo,
    content,
    refresh,
    isFullScreen,
    loadingLog,
    logTextParam,
    fileList,

    // Options
    linesOpt,

    // Computed
    fullScreenIcon,

    // Methods
    loadIp,
    loadFileList,
    loadLogContent,
    closeErrInfo,
    downloadLog,
    handleZoom
  };
}
