import { nextTick, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { modal, notification } from '@xcan-angus/vue-ui';
import { ScriptType } from '@xcan-angus/infra';
import { exec } from '@/api/tester';
import { getCurrentPage } from '@/utils/utils';
import type { ExecutionInfo } from '../types';

/**
 * Composable for managing execution actions
 * Handles CRUD operations, inline editing, and status changes
 */
export function useExecutionAction () {
  const { t } = useI18n();
  const router = useRouter();

  const loading = ref(false);

  /**
   * Update execution name
   */
  const updateName = (item: ExecutionInfo): void => {
    item.editName = true;
    nextTick(() => {
      // Focus will be handled by parent component
    });
  };

  /**
   * Edit execution name
   */
  const editName = async (name: string, item: ExecutionInfo): Promise<void> => {
    if (name === item.name || loading.value) {
      item.editName = false;
      return;
    }

    loading.value = true;
    const [error] = await exec.putExecConfig(item.id, { name });
    loading.value = false;

    if (error) {
      return;
    }

    item.editName = false;
    item.name = name;
    notification.success(t('actions.tips.modifySuccess'));
  };

  /**
   * Update thread count
   */
  const updateThread = (item: ExecutionInfo): void => {
    item.editThread = true;
    nextTick(() => {
      // Focus will be handled by parent component
    });
  };

  /**
   * Get maximum thread count based on script type
   */
  const threadsMax = (item: ExecutionInfo): number => {
    return [ScriptType.MOCK_DATA, ScriptType.MOCK_APIS].includes(item?.scriptType?.value) ? 1000 : 10000;
  };

  /**
   * Edit thread count
   */
  const editThread = async (value: string, item: ExecutionInfo): Promise<void> => {
    if ((!item?.thread && !value) || item.thread === value || loading.value) {
      item.editThread = false;
      return;
    }

    loading.value = true;
    const [error] = await exec.putExecConfig(item.id, { thread: value });
    loading.value = false;

    if (error) {
      return;
    }

    item.thread = value;
    item.editThread = false;
    notification.success(t('actions.tips.modifySuccess'));
  };

  /**
   * Update duration
   */
  const updateDuration = (item: ExecutionInfo): void => {
    item.editDuration = true;
    nextTick(() => {
      // Focus will be handled by parent component
    });
  };

  /**
   * Edit duration
   */
  const editDuration = async (value: string, item: ExecutionInfo): Promise<void> => {
    if ((!item?.duration && !value) || item.duration === value || loading.value) {
      item.editDuration = false;
      return;
    }

    loading.value = true;
    const [error] = await exec.putExecConfig(item.id, { duration: value });
    loading.value = false;

    if (error) {
      return;
    }

    item.duration = value;
    item.editDuration = false;
    notification.success(t('actions.tips.modifySuccess'));
  };

  /**
   * Update iterations
   */
  const updateIterations = (item: ExecutionInfo): void => {
    item.editIterations = true;
    nextTick(() => {
      // Focus will be handled by parent component
    });
  };

  /**
   * Edit iterations
   */
  const editIterations = async (value: string, item: ExecutionInfo): Promise<void> => {
    if ((!item?.iterations && !value) || item.iterations === value || loading.value) {
      item.editIterations = false;
      return;
    }

    loading.value = true;
    const [error] = await exec.putExecConfig(item.id, { iterations: value });
    loading.value = false;

    if (error) {
      return;
    }

    item.iterations = value;
    item.editIterations = false;
    notification.success(t('actions.tips.modifySuccess'));
  };

  /**
   * Update priority
   */
  const updatePriority = (item: ExecutionInfo): void => {
    item.editPriority = true;
    nextTick(() => {
      // Focus will be handled by parent component
    });
  };

  /**
   * Edit priority
   */
  const editPriority = async (value: string, item: ExecutionInfo): Promise<void> => {
    if ((item?.priority && item.priority === value) || loading.value) {
      item.editPriority = false;
      return;
    }

    loading.value = true;
    const [error] = await exec.putExecConfig(item.id, { priority: value });
    loading.value = false;

    if (error) {
      return;
    }

    item.editPriority = false;
    item.priority = value;
    notification.success(t('actions.tips.modifySuccess'));
  };

  /**
   * Update report interval
   */
  const updateReportInterval = (item: ExecutionInfo): void => {
    item.editReportInterval = true;
    nextTick(() => {
      // Focus will be handled by parent component
    });
  };

  /**
   * Edit report interval
   */
  const editReportInterval = async (value: string, item: ExecutionInfo): Promise<void> => {
    if ((!item?.reportInterval && !value) || item.reportInterval === value || loading.value) {
      item.editReportInterval = false;
      return;
    }

    loading.value = true;
    const [error] = await exec.putExecConfig(item.id, { reportInterval: value });
    loading.value = false;

    if (error) {
      return;
    }

    item.editReportInterval = false;
    item.reportInterval = value;
    notification.success(t('actions.tips.modifySuccess'));
  };

  /**
   * Handle ignore assertions toggle
   */
  const handleIgnoreAssertions = async (value: boolean, item: ExecutionInfo): Promise<void> => {
    if (loading.value) {
      return;
    }

    loading.value = true;
    const [error] = await exec.putExecConfig(item.id, { ignoreAssertions: value });
    loading.value = false;

    if (error) {
      return;
    }

    item.editStartDate = false;
    item.ignoreAssertions = value;
    notification.success(t('actions.tips.modifySuccess'));
  };

  /**
   * Handle update test result toggle
   */
  const handleUpdateTestResult = async (value: boolean, item: ExecutionInfo): Promise<void> => {
    if (loading.value) {
      return;
    }

    loading.value = true;
    const [error] = await exec.putExecConfig(item.id, { updateTestResult: value });
    loading.value = false;

    if (error) {
      return;
    }

    item.editStartDate = false;
    item.updateTestResult = value;
    notification.success(t('actions.tips.modifySuccess'));
  };

  /**
   * Handle restart execution
   */
  // eslint-disable-next-line @typescript-eslint/ban-types
  const handleRestart = async (item: ExecutionInfo, loadDataListByIds: Function): Promise<void> => {
    if (loading.value) {
      return;
    }

    item.errMessage = undefined;
    item.editName = false;
    item.editPriority = false;
    item.editStartDate = false;
    item.editDuration = false;
    item.editReportInterval = false;
    item.editIterations = false;

    const params = {
      broadcast: true,
      id: item.id
    };

    loading.value = true;
    const [error, { data }] = await exec.startExec(params);
    loading.value = false;

    if (error) {
      let errMessage;
      if ((error as any)?.code || error?.message) {
        errMessage = {
          code: (error as any).code,
          message: error.message,
          codeName: t('common.exitCode'),
          messageName: t('common.failureReason')
        };
      }
      item.errMessage = errMessage;
      return;
    }

    const currItemDataList = data.filter((f: any) => f.execId === item.id);
    if (currItemDataList.length) {
      const successFalseItem = currItemDataList.find((f: any) => f.success);
      if (successFalseItem) {
        notification.success(t('actions.tips.startSuccess'));
      } else {
        notification.error(currItemDataList[0].message);
        item.errMessage = {
          code: currItemDataList[0]?.exitCode,
          message: currItemDataList[0]?.message,
          codeName: t('common.exitCode'),
          messageName: t('common.failureReason')
        };
      }
    }

    await loadDataListByIds(true);
  };

  /**
   * Handle stop execution
   */
  // eslint-disable-next-line @typescript-eslint/ban-types
  const handleStop = async (item: ExecutionInfo, loadDataListByIds: Function): Promise<void> => {
    if (loading.value) {
      return;
    }

    item.errMessage = undefined;
    const params = {
      broadcast: true,
      id: item.id
    };

    loading.value = true;
    const [error, { data }] = await exec.stopExec(params);
    loading.value = false;

    if (error) {
      let errMessage;
      if ((error as any)?.code || error?.message) {
        errMessage = {
          code: (error as any).code,
          message: error.message,
          codeName: t('common.exitCode'),
          messageName: t('common.failureReason')
        };
      }
      item.errMessage = errMessage;
      return;
    }

    const currItemDataList = data.filter((f: any) => f.execId === item.id);
    if (currItemDataList.length) {
      const successFalseItem = currItemDataList.find((f: any) => f.success);
      if (successFalseItem) {
        notification.success(t('actions.tips.stopSuccess'));
      } else {
        notification.error(currItemDataList[0].message);
        item.errMessage = {
          code: currItemDataList[0]?.exitCode,
          message: currItemDataList[0]?.message,
          codeName: t('common.exitCode'),
          messageName: t('common.failureReason')
        };
      }
    }

    await loadDataListByIds(true);
  };

  /**
   * Handle delete execution
   */
  // eslint-disable-next-line @typescript-eslint/ban-types
  const handleDelete = async (item: ExecutionInfo, pagination: any, total: any, loadDataList: Function): Promise<void> => {
    if (loading.value) {
      return;
    }

    modal.confirm({
      centered: true,
      content: t('actions.tips.confirmDelete', { name: item.name }),
      async onOk () {
        loading.value = true;
        const [error] = await exec.deleteExec([item.id], { dataType: true });
        loading.value = false;

        if (error) {
          return;
        }

        notification.success(t('actions.tips.deleteSuccess'));
        pagination.value.current = getCurrentPage(pagination.value.current, pagination.value.pageSize, total.value);
        loadDataList();
      }
    });
  };

  /**
   * Handle dropdown menu clicks
   */
  // eslint-disable-next-line @typescript-eslint/ban-types
  const dropdownClick = (key: string, item: ExecutionInfo, pagination: any, loadDataList: Function): void => {
    switch (key) {
      case 'edit':
        router.push(`/execution/edit/${item.id}`);
        break;
      case 'viewLog':
        router.push(`/execution/info/${item.id}?&pageNo=${pagination.value.current}&tab=log`);
        break;
      case 'delete':
        handleDelete(item, pagination, { value: 0 }, loadDataList);
        break;
    }
  };

  /**
   * Get formatted number with fixed decimal places
   */
  const getNumFixed = (str: string): string => {
    return str ? Number(str).toFixed(2) : '0';
  };

  return {
    // State
    loading,

    // Methods
    updateName,
    editName,
    updateThread,
    threadsMax,
    editThread,
    updateDuration,
    editDuration,
    updateIterations,
    editIterations,
    updatePriority,
    editPriority,
    updateReportInterval,
    editReportInterval,
    handleIgnoreAssertions,
    handleUpdateTestResult,
    handleRestart,
    handleStop,
    handleDelete,
    dropdownClick,
    getNumFixed
  };
}
