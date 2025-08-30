import { ref, computed } from 'vue';
import { activity } from '@/api/tester';
import { setting } from '@/api/gm';
import { SETTING_KEYS } from '@/utils/constant';
import DOMPurify from 'dompurify';
import type { Activity, ActivityApiParams, ActivityListResponse } from '../types';

/**
 * Composable for managing activity data operations
 * Handles data fetching, loading states, pagination, and settings
 *
 * @returns Object containing reactive data and data management functions
 */
export function useActivityData () {
  // Reactive state for activity data management
  const tableData = ref<Activity[]>([]);
  const loading = ref(false);
  const loaded = ref(false);
  const total = ref(0);
  const maxResource = ref('0');

  // Pagination state
  const params = ref<ActivityApiParams>({
    pageNo: 1,
    pageSize: 10,
    filters: []
  });

  /**
   * Load activity data from API
   * Processes the data for display and updates reactive state
   *
   * @returns Promise that resolves when data is loaded
   */
  const loadActivityList = async (): Promise<void> => {
    if (loading.value) {
      return;
    }

    loading.value = true;

    try {
      const [error, response] = await activity.getActivityList(params.value);

      if (error) {
        console.error('Failed to fetch activity list:', error);
        return;
      }

      const data = response?.data as ActivityListResponse || { list: [], total: 0 };

      // Sanitize activity details for security
      tableData.value = data.list?.map(item => {
        return {
          ...item,
          detail: DOMPurify.sanitize(item.detail)
        };
      }) || [];

      total.value = Number(data.total || 0);
      loaded.value = true;
    } catch (error) {
      console.error('Unexpected error while fetching activity list:', error);
    } finally {
      loading.value = false;
    }
  };

  /**
   * Load maximum resource activities setting
   * Fetches the system setting for maximum resource activities
   *
   * @returns Promise that resolves when setting is loaded
   */
  const loadMaxResourceSetting = async (): Promise<void> => {
    const [error, response] = await setting.getSettingByKey(SETTING_KEYS.MAX_RESOURCE_ACTIVITIES);

    if (error) {
      console.error('Failed to fetch max resource setting:', error);
      return;
    }

    maxResource.value = response?.data?.maxResourceActivities || '0';
  };

  /**
   * Handle table change events (pagination, sorting)
   * Updates pagination and sorting parameters and reloads data
   *
   * @param paginationInfo - Pagination information
   * @param _filters - Table filters (not used)
   * @param sorter - Sort configuration
   */
  const handleTableChange = (
    paginationInfo: { current?: number; pageSize?: number },
    _filters: any,
    sorter: { orderBy?: string; orderSort?: 'ASC' | 'DESC' }
  ): void => {
    params.value.pageNo = paginationInfo.current || 1;
    params.value.pageSize = paginationInfo.pageSize || 10;
    params.value.orderBy = sorter.orderBy;
    params.value.orderSort = sorter.orderSort;

    loadActivityList();
  };

  /**
   * Reset pagination to first page
   */
  const resetPagination = (): void => {
    params.value.pageNo = 1;
  };

  /**
   * Update activity list with new search parameters
   * Resets pagination and applies new filters
   *
   * @param searchParams - New search parameters
   */
  const updateSearchParams = (searchParams: {
    orderBy?: string;
    orderSort?: 'ASC' | 'DESC';
    filters: Array<{key: string; op: string; value: string|string[]}>
  }): void => {
    params.value.pageNo = 1;
    params.value.filters = searchParams.filters;
    params.value.orderBy = searchParams.orderBy;
    params.value.orderSort = searchParams.orderSort;

    loadActivityList();
  };

  /**
   * Refresh all data including activities and settings
   */
  const refreshAllData = (): void => {
    loadActivityList();
  };

  // Computed properties
  const pagination = computed(() => ({
    current: params.value.pageNo,
    pageSize: params.value.pageSize,
    total: total.value
  }));

  const hasData = computed(() => tableData.value.length > 0);
  const isEmpty = computed(() => loaded.value && !hasData.value);

  // Return reactive state and methods
  return {
    // Reactive state
    tableData,
    loading,
    loaded,
    total,
    maxResource,
    params,
    pagination,

    // Computed properties
    hasData,
    isEmpty,

    // Data management methods
    loadActivityList,
    loadMaxResourceSetting,
    handleTableChange,
    resetPagination,
    updateSearchParams,
    refreshAllData
  };
}
