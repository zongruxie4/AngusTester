import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { modal, notification } from '@xcan-angus/vue-ui';
import YAML from 'yaml';
import { exec } from 'src/api/ctrl';
import { Exception, ExecutionInfo } from '@/views/execution/types';
import { ScriptType, enumUtils } from '@xcan-angus/infra';

/**
 * Composable for managing execution detail data
 * Handles loading execution info, managing state, and execution actions
 *
 * @param props - Component props
 * @param emit - Vue emit function for component events
 * @returns Execution detail management functions and reactive state
 */
export const useExecutionDetail = (props: any, emit: any) => {
  // Vue router hooks
  const route = useRoute();
  const router = useRouter();
  const { t } = useI18n();

  // Component references
  const performanceRef = ref();
  const funcRef = ref();

  // Execution ID from props or route
  const id = props.execId || route.params.id as string;

  // Reactive state
  const topActiveKey = ref(route.query?.tab === 'log' ? '4' : '1');
  const loading = ref(false);
  const detail = ref<ExecutionInfo>();
  const scriptInfo = ref();
  const scriptYamlStr = ref('');
  const exception = ref<Exception>();

  /**
   * Load script content for execution
   * Fetches script content from API and updates scriptYamlStr
   */
  const loadScriptContent = async () => {
    // Fetch script content from API
    const [error, { data }] = await exec.getScriptByExecId(id);
    if (error) {
      // Silently handle error
      return;
    }
    // Update script content
    scriptYamlStr.value = data;
  };

  /**
   * Get execution detail information
   * Fetches execution details from API and updates component state
   */
  const getDetail = async () => {
    // Show loading state
    loading.value = true;

    // Fetch execution details from API
    const [error, { data }] = await exec.getDetail(id);
    loading.value = false;
    // Handle API error
    if (error) {
      loading.value = false;
      return;
    }

    // Update execution details
    detail.value = data;

    // Special handling for MOCK_DATA script type
    if (detail.value?.scriptType.value === ScriptType.MOCK_DATA) {
      detail.value.batchRows = detail.value.task?.mockData?.settings.batchRows || '1';
    }

    // Extract and structure script information
    const { configuration, plugin, task, scriptId, scriptType } = data;
    scriptInfo.value = {
      id: scriptId,
      type: scriptType.value,
      plugin,
      configuration,
      task
    };
  };

  /**
   * Update detail information with new data
   * Merges new data into existing detail object
   *
   * @param data - New data to merge into detail
   */
  const getInfo = (data: any) => {
    // Validate input data
    if (!data) {
      return;
    }

    const keys = Object.keys(data);
    if (!keys.length || !detail.value) {
      return;
    }

    // Merge new data into detail object
    for (const key in data) {
      detail.value[key] = data[key];
    }

    // Update exception information
    setException();
  };

  /**
   * Handle execution restart action
   * Restarts the execution and handles success/error states
   *
   * @param item - Execution item to restart
   */
  const handleRestart = async (item: ExecutionInfo) => {
    // Reset performance data if needed
    if ([ScriptType.TEST_PERFORMANCE, ScriptType.TEST_STABILITY]
      .includes(detail.value?.scriptType.value as ScriptType) && performanceRef.value) {
      performanceRef.value.resetData();
    }

    // Restart mock data if needed
    if (detail.value?.scriptType.value === ScriptType.MOCK_DATA) {
      performanceRef.value.restartMock();
    }

    // Clear previous exceptions
    exception.value = undefined;

    // Prepare restart parameters
    const _params = {
      broadcast: true,
      id: item.id
    };

    // Show loading state and call restart API
    loading.value = true;
    const [error, { data }] = await exec.restart(_params);

    // Handle API error
    if (error) {
      loading.value = false;
      if (error?.message) {
        exception.value = {
          code: (error as any)?.code || '',
          message: error.message,
          codeName: 'Exit Code',
          messageName: 'Failure Reason'
        };
      }
      return;
    }

    // Process restart response data
    const currItemDataList = data.filter((f: any) => f.execId === item.id);
    if (currItemDataList.length) {
      const successFalseItem = currItemDataList.find((f: any) => f.success);
      if (successFalseItem) {
        // Show success notification
        notification.success(t('actions.tips.startSuccess'));
        exception.value = undefined;
      } else {
        // Show error notification
        notification.error(currItemDataList[0].message);
        exception.value = {
          code: currItemDataList[0]?.exitCode || '',
          message: currItemDataList[0]?.message,
          codeName: 'Exit Code',
          messageName: 'Failure Reason'
        };
      }
    }

    // Refresh execution details
    await getDetail();
    setException();
  };

  /**
   * Handle execution stop action
   * Stops the execution and handles success/error states
   *
   * @param item - Execution item to stop
   */
  const handleStop = async (item: ExecutionInfo) => {
    // Clear previous exceptions
    exception.value = undefined;

    // Prepare stop parameters
    const _params = {
      broadcast: true,
      id: item.id
    };

    // Show loading state and call stop API
    loading.value = true;
    const [error, { data }] = await exec.stop(_params);
    loading.value = false;

    // Handle API error
    if (error) {
      if (error?.message) {
        exception.value = {
          code: (error as any)?.code || '',
          message: error.message,
          codeName: 'Exit Code',
          messageName: 'Failure Reason'
        };
      }
      return;
    }

    // Process stop response data
    const currItemDataList = data.filter((f: any) => f.execId === item.id);
    if (currItemDataList.length) {
      const successFalseItem = currItemDataList.find((f: any) => f.success);
      if (successFalseItem) {
        // Show success notification
        notification.success(t('actions.tips.stopSuccess'));
        exception.value = undefined;
      } else {
        // Show error notification
        notification.error(currItemDataList[0].message);
        exception.value = {
          code: currItemDataList[0]?.exitCode || '',
          message: currItemDataList[0]?.message,
          codeName: 'Exit Code',
          messageName: 'Failure Reason'
        };
      }
    }

    // Refresh execution details
    await getDetail();
    setException();
  };

  /**
   * Handle execution delete action
   * Shows confirmation dialog and deletes execution
   *
   * @param item - Execution item to delete
   */
  const handleDelete = async (item: ExecutionInfo) => {
    modal.confirm({
      centered: true,
      content: `Confirm delete execution: ${item.name}`,
      async onOk () {
        // Show loading state and call delete API
        loading.value = true;
        const [error] = await exec.delete([item.id]);
        loading.value = false;
        if (error) {
          return;
        }
        // Show success notification
        notification.success(t('actions.tips.deleteSuccess'));

        // Navigate based on props
        if (props.showBackBtn) {
          router.push('/execution');
        } else {
          emit('del');
        }
      }
    });
  };

  /**
   * Handle tab change events
   * Loads script content when script tab is activated
   *
   * @param value - Active tab key
   */
  const topTabsChange = (value: string) => {
    // Load script content when script tab (key '3') is activated
    if (value === '3' && !scriptYamlStr.value) {
      loadScriptContent();
    }
  };

  /**
   * Handle script language type change
   * Converts script content between JSON and YAML formats
   *
   * @param value - New language type (json or yaml)
   */
  const scriptTypeChange = (value: 'json' | 'yaml') => {
    // Convert script content if available
    if (scriptYamlStr.value) {
      scriptYamlStr.value = value === 'json'
        ? JSON.stringify(YAML.parse(scriptYamlStr.value), null, 4)
        : YAML.stringify(YAML.parse(scriptYamlStr.value));
    }
  };

  /**
   * Set exception information based on execution status
   * Determines exception state from scheduling results and meter data
   */
  const setException = () => {
    const lastSchedulingResult = detail.value?.lastSchedulingResult;
    const meterMessage = detail.value?.meterMessage;

    // Check scheduling results for failures
    if (lastSchedulingResult?.length) {
      const foundItem = lastSchedulingResult.find((f: any) => !f.success);
      if (foundItem) {
        exception.value = {
          code: foundItem.exitCode || '',
          message: foundItem.message,
          codeName: 'Exit Code',
          messageName: 'Failure Reason'
        };
        return;
      }

      // Check meter message if no scheduling failures
      if (meterMessage) {
        exception.value = {
          code: detail.value?.meterStatus || '',
          message: meterMessage,
          codeName: 'Sampling Status',
          messageName: 'Failure Reason'
        };
        return;
      }
    }

    // Check meter message if no scheduling results
    if (meterMessage) {
      exception.value = {
        code: detail.value?.meterStatus || '',
        message: meterMessage,
        codeName: 'Sampling Status',
        messageName: 'Failure Reason'
      };
      return;
    }

    // Clear exception if no issues found
    exception.value = undefined;
  };

  /**
   * Initialize component on mount
   * Loads execution details if ID is available
   */
  onMounted(async () => {
    // Handle case when no execution ID is provided
    if (!id) {
      // Set initial detail state based on props
      if (props.scriptType) {
        detail.value = {
          scriptType: {
            value: props.scriptType,
            message: enumUtils.getEnumDescription(ScriptType, props.scriptType)
          }
        } as ExecutionInfo;

        // Set plugin if provided
        if (props.plugin) {
          detail.value.plugin = props.plugin;
        }
      }
      return;
    }

    // Load execution details and set exception state
    await getDetail();
    setException();
  });

  // Return reactive references directly, not their .value properties
  return {
    loading,
    detail,
    scriptInfo,
    scriptYamlStr,
    topActiveKey,
    exception,
    performanceRef,
    funcRef,
    loadScriptContent,
    getDetail,
    getInfo,
    handleRestart,
    handleStop,
    handleDelete,
    topTabsChange,
    scriptTypeChange,
    setException
  };
};
