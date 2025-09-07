import { computed, inject, ref, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { PageQuery } from '@xcan-angus/infra';
import dayjs from 'dayjs';
import { exec } from '@/api/tester';
import type { ExecutionInfo, ProjectInfo } from '../types';

export type OrderByKey = 'createdDate' | 'createdByName';

/**
 * Composable for managing execution list functionality
 * Handles data loading, pagination, sorting, and CRUD operations
 */
export function useExecutionList () {
  const { t } = useI18n();
  const router = useRouter();
  const route = useRoute();

  // Inject project information
  const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({ id: '', avatar: '', name: '' }));
  const projectId = computed(() => projectInfo.value?.id);

  // State management
  const loaded = ref(false);
  const loading = ref(false);
  const refinishLoading = ref(false);
  const orderBy = ref<OrderByKey>();
  const orderSort = ref<PageQuery.OrderSort>();
  const pagination = ref({
    current: 1,
    pageSize: 5,
    total: 0
  });
  const filters = ref<{ key: string; op: string; value: boolean | string | string[]; }[]>([]);
  const dataList = ref<ExecutionInfo[]>([]);
  const total = ref(0);
  const execIds = ref<string[]>([]);
  const reportInterval = ref(3);

  // Timer for auto-refresh
  let timer: NodeJS.Timeout | null = null;

  // Letter map for time unit conversion
  const letterMap = {
    ms: '毫秒',
    s: '秒',
    min: '分钟',
    h: '小时',
    d: '天'
  };

  // Input refs for inline editing
  const nameInputRefs = ref<{ [key: string]: any }>({});
  const priorityInputRefs = ref<{ [key: string]: any }>({});
  const reportIntervalInputRefs = ref<{ [key: string]: any }>({});
  const durationInputRefs = ref<{ [key: string]: any }>({});
  const threadInputRefs = ref<{ [key: string]: any }>({});
  const iterationsInputRefs = ref<{ [key: string]: any }>({});

  /**
   * Check if execution has a future start date
   */
  const hasStartDate = (startAtDate: string): boolean => {
    if (!startAtDate) {
      return false;
    }
    const givenTime = dayjs(startAtDate);
    const currentTime = dayjs();
    return !(givenTime.isBefore(currentTime) || givenTime.isSame(currentTime));
  };

  /**
   * Split time string into number and unit
   */
  const splitTime = (str: string): [string, string] => {
    const number = str.replace(/\D/g, '');
    const unit = str.replace(/\d/g, '');
    return [number, letterMap[unit]];
  };

  /**
   * Set timeout times based on report interval
   */
  const setTimeoutTimes = (item: ExecutionInfo): void => {
    if (item.reportInterval) {
      const time = +splitTime(item.reportInterval)[0];
      if (reportInterval.value === 3) {
        if (time > 3) {
          reportInterval.value = time;
        }
      } else if (reportInterval.value > 3) {
        if (time < reportInterval.value) {
          reportInterval.value = time;
        }
      }
    }
  };

  /**
   * Get error message from execution item
   */
  const getMessage = (item: ExecutionInfo): { code: string; message: string; codeName: string; messageName: string } | undefined => {
    if (item?.lastSchedulingResult?.length) {
      const foundItem = item.lastSchedulingResult.find(f => !f.success);
      if (foundItem) {
        return {
          code: foundItem.exitCode,
          message: foundItem.message,
          codeName: t('execution.messages.exitCode'),
          messageName: t('execution.messages.failureReason')
        };
      }

      if (item?.meterMessage) {
        return {
          code: item.meterStatus,
          message: item.meterMessage,
          codeName: t('execution.messages.samplingStatus'),
          messageName: t('execution.messages.failureReason')
        };
      }
      return undefined;
    }

    if (item?.meterMessage) {
      return {
        code: item.meterStatus,
        message: item.meterMessage,
        codeName: t('execution.messages.samplingStatus'),
        messageName: t('execution.messages.failureReason')
      };
    }
    return undefined;
  };

  /**
   * Get stroke color for progress bars
   */
  const getStrokeColor = (value: string, status: string): string => {
    const num = Number(value?.slice(-1) === '%' ? value.slice(0, -1) : value);
    if (num === 100) {
      return 'rgba(82, 196, 26, 1)';
    }

    const colorMap: Record<string, string> = {
      CREATED: 'rgba(45, 142, 255, 1)',
      PENDING: 'rgba(45, 142, 255, 1)',
      RUNNING: 'rgba(45, 142, 255, 1)',
      STOPPED: 'rgba(255, 77, 79, 1)',
      FAILED: 'rgba(255, 77, 79, 1)',
      COMPLETED: 'rgba(45, 142, 255, 1)',
      TIMEOUT: 'rgba(255, 77, 79, 1)'
    };

    return colorMap[status] || 'rgba(45, 142, 255, 1)';
  };

  /**
   * Load execution data by IDs for auto-refresh
   */
  const loadDataListByIds = async (isRefinsh: boolean): Promise<void> => {
    if (refinishLoading.value && !isRefinsh) {
      return;
    }

    if (!execIds.value.length && timer) {
      clearTimeout(timer);
      return;
    }

    const params = {
      filters: [{ key: 'id', op: 'IN', value: execIds.value }],
      projectId: projectId.value
    };

    if (isRefinsh) {
      loading.value = true;
    }
    refinishLoading.value = true;

    const [error, { data = { list: [], total: 0 } }] = await exec.getExecList(params);
    refinishLoading.value = false;

    if (isRefinsh) {
      loading.value = false;
    }

    if (error) {
      return;
    }

    if (data.list?.length) {
      execIds.value = [];
      for (let i = 0; i < data.list.length; i++) {
        const dataItem = data.list[i];
        const errMessage = getMessage(dataItem);
        const _hasStartDate = hasStartDate(dataItem?.startAtDate);
        setTimeoutTimes(dataItem);

        const executeItemIndex = dataList.value.findIndex(item => item.id === dataItem.id);
        if (executeItemIndex !== -1) {
          dataList.value[executeItemIndex] = { ...dataItem, errMessage };
        }

        if (['CREATED', 'PENDING', 'RUNNING'].includes(dataItem?.status.value) && !_hasStartDate) {
          execIds.value.push(dataItem.id);
        }
      }
    }

    if (timer) {
      clearTimeout(timer);
    }

    if (execIds.value.length) {
      timer = setTimeout(async () => {
        await loadDataListByIds(false);
      }, reportInterval.value * 1000);
    }
  };

  /**
   * Load execution list data
   */
  const loadDataList = async (): Promise<void> => {
    if (loading.value) {
      return;
    }

    const { current, pageSize } = pagination.value;
    const params: {
      pageNo: number;
      pageSize: number;
      projectId: string;
      filters?: { key: string; op: string; value: boolean | string | string[]; }[];
      orderBy?: OrderByKey;
      orderSort?: PageQuery.OrderSort;
    } = {
      pageNo: current,
      pageSize,
      projectId: projectId.value
    };

    if (filters.value?.length) {
      params.filters = filters.value;
    }

    if (orderSort.value) {
      params.orderBy = orderBy.value;
      params.orderSort = orderSort.value;
    }

    loading.value = true;
    const [error, { data = { list: [], total: 0 } }] = await exec.getExecList(params);
    loading.value = false;
    loaded.value = true;

    if (error) {
      return;
    }

    execIds.value = [];
    dataList.value = data.list.map(item => {
      const _hasStartDate = hasStartDate(item?.startAtDate);
      if (['CREATED', 'PENDING', 'RUNNING'].includes(item?.status.value) && !_hasStartDate) {
        execIds.value.push(item.id);
      }
      setTimeoutTimes(item);
      const errMessage = getMessage(item);

      const others = {
        ...item,
        errMessage,
        editName: false,
        editPriority: false,
        editReportInterval: false,
        editStartDate: false,
        editStartAtDate: false,
        editIterations: false,
        editDuration: false
      };

      const _actionPermission: string[] = [];
      if (item.hasOperationPermission) {
        if (['CREATED', 'PENDING', 'RUNNING'].includes(item?.status.value) && !_hasStartDate) {
          _actionPermission.push('stopNow');
        } else {
          _actionPermission.push('edit');
        }

        if (!['PENDING'].includes(item?.status.value)) {
          _actionPermission.push('delete');
        }
      }

      const durationStrokeColor = getStrokeColor(item.currentDurationProgress, item.status?.value);
      const iterationStrokeColor = getStrokeColor(item.currentIterationsProgress, item.status?.value);

      return {
        ...others,
        actionPermission: _actionPermission,
        durationStrokeColor,
        iterationStrokeColor
      };
    });

    total.value = +data.total;

    if (timer) {
      clearTimeout(timer);
    }

    if (execIds.value.length) {
      timer = setTimeout(async () => {
        await loadDataListByIds(false);
      }, reportInterval.value * 1000);
    }
  };

  /**
   * Handle sorting
   */
  const toSort = (data: { orderBy: OrderByKey; orderSort: PageQuery.OrderSort; }): void => {
    orderBy.value = data.orderBy;
    orderSort.value = data.orderSort;
    pagination.value.current = 1;
    loadDataList();
  };

  /**
   * Handle search panel changes
   */
  const searchPanelChange = (data: { key: string; op: string; value: boolean | string | string[]; }[]): void => {
    filters.value = data;
    pagination.value.current = 1;
    loadDataList();
  };

  /**
   * Show pagination total text
   */
  const showTotal = (total: number): string => {
    const totalPage = Math.ceil(total / pagination.value.pageSize);
    return t('execution.messages.totalRecords', {
      total,
      current: pagination.value.current,
      totalPage
    });
  };

  /**
   * Handle pagination change
   */
  const paginationChange = (pageNo: number, pageSize: number): void => {
    pagination.value.current = pageNo;
    pagination.value.pageSize = pageSize;
    loadDataList();
  };

  /**
   * Initialize component
   */
  const initialize = (): void => {
    const query = route.query;
    if (query) {
      const { pageNo } = query;
      if (pageNo) {
        pagination.value.current = +pageNo as number;
        router.replace('/execution');
      }
    }
    // Load data after initialization
    loadDataList();
  };

  /**
   * Cleanup timer on unmount
   */
  const cleanup = (): void => {
    if (timer) {
      clearTimeout(timer);
      timer = null;
    }
  };

  return {
    // State
    loaded,
    loading,
    refinishLoading,
    orderBy,
    orderSort,
    pagination,
    filters,
    dataList,
    total,
    execIds,
    reportInterval,
    projectId,

    // Input refs
    nameInputRefs,
    priorityInputRefs,
    reportIntervalInputRefs,
    durationInputRefs,
    threadInputRefs,
    iterationsInputRefs,

    // Methods
    toSort,
    searchPanelChange,
    loadDataList,
    loadDataListByIds,
    showTotal,
    paginationChange,
    initialize,
    cleanup,
    hasStartDate,
    splitTime,
    getMessage,
    getStrokeColor
  };
}
