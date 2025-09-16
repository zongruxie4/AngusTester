import { computed } from 'vue';
import { debounce } from 'throttle-debounce';
import { duration, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import dayjs, { Dayjs } from 'dayjs';
import { task } from '@/api/tester';
import { DATE_TIME_FORMAT } from '@/utils/constant';
import {
  BacklogProps,
  BacklogDataState,
  LoadingState,
  SearchState,
  TaskQueryParams,
  SortParams,
  PRIORITY_LEVEL_CONFIG,
  PAGE_SIZE
} from './types';

/**
 * <p>Composable for managing task data operations</p>
 * <p>Handles task data loading, filtering, sorting, and search functionality</p>
 */
export function useTaskData (
  props: BacklogProps,
  backlogData: BacklogDataState,
  loading: LoadingState,
  search: SearchState
) {
  /**
   * <p>Build task query parameters</p>
   * <p>Constructs the query parameters based on current search and filter state</p>
   */
  const getTaskParams = (): TaskQueryParams => {
    const params: TaskQueryParams = {
      projectId: props.projectId!,
      pageSize: PAGE_SIZE,
      filters: []
    };

    if (search.searchValue) {
      params.filters.push({
        key: 'name',
        op: SearchCriteria.OpEnum.Equal,
        value: search.searchValue
      });
    }

    if (search.assigneeId) {
      params.filters.push({
        key: 'assigneeId',
        op: SearchCriteria.OpEnum.Equal,
        value: search.assigneeId
      });
    }

    if (search.createdBy) {
      params.filters.push({
        key: 'createdBy',
        op: SearchCriteria.OpEnum.Equal,
        value: search.createdBy
      });
    }

    if (search.quickDate) {
      let startDate: Dayjs;
      let endDate: Dayjs;

      if (search.quickDate === '1') {
        startDate = dayjs().startOf('date');
        endDate = dayjs();
      } else if (search.quickDate === '3') {
        startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
        endDate = dayjs();
      } else {
        startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
        endDate = dayjs();
      }

      params.filters.push(
        {
          key: 'createdDate',
          op: SearchCriteria.OpEnum.GreaterThanEqual,
          value: startDate.format(DATE_TIME_FORMAT)
        }
      );
      params.filters.push(
        {
          key: 'createdDate',
          op: SearchCriteria.OpEnum.LessThanEqual,
          value: endDate.format(DATE_TIME_FORMAT)
        }
      );
    }
    return params;
  };

  /**
   * <p>Sort backlog tasks by priority</p>
   * <p>Sorts the backlog list based on priority level configuration</p>
   */
  const sortBacklogByPriority = () => {
    backlogData.backlogList.sort((a, b) => {
      if (backlogData.backlogSortParams?.orderSort === PageQuery.OrderSort.Asc) {
        return PRIORITY_LEVEL_CONFIG[a.priority?.value] - PRIORITY_LEVEL_CONFIG[b.priority?.value];
      } else if (backlogData.backlogSortParams?.orderSort === PageQuery.OrderSort.Desc) {
        return PRIORITY_LEVEL_CONFIG[b.priority?.value] - PRIORITY_LEVEL_CONFIG[a.priority?.value];
      } else {
        return 0;
      }
    });
  };

  /**
   * <p>Load backlog task list</p>
   * <p>Fetches tasks from the backlog with pagination and sorting</p>
   */
  const loadBacklogList = async (pageNo: number) => {
    const params = getTaskParams();
    params.pageNo = pageNo;
    params.backlog = true;
    Object.assign(params, backlogData.backlogSortParams || {});

    loading.isLoading = true;
    const [error, res] = await task.getTaskList(params);
    loading.isLoading = false;
    backlogData.isBacklogDataLoaded = true;

    if (error) {
      backlogData.backlogTotalCount = 0;
      backlogData.backlogList = [];
      return;
    }

    const data = res?.data;
    if (!data) {
      backlogData.backlogTotalCount = 0;
      backlogData.backlogList = [];
      return;
    }

    backlogData.backlogTotalCount = +data.total;

    const list = data.list || [];
    if (pageNo === 1) {
      backlogData.backlogList = list;
    } else {
      backlogData.backlogList.push(...list);
    }

    if (backlogData.backlogTotalCount > backlogData.backlogList.length) {
      await loadBacklogList(pageNo + 1);
    } else {
      if (backlogData.backlogSortParams?.orderBy === 'priority') {
        sortBacklogByPriority();
      }
    }
  };

  /**
   * <p>Handle backlog task sorting</p>
   * <p>Processes sort requests for backlog tasks</p>
   */
  const handleBacklogTaskSort = (orderData: SortParams) => {
    backlogData.backlogSortParams = orderData;
    if (orderData.orderBy === 'priority') {
      sortBacklogByPriority();
      return;
    }
    loadBacklogList(1);
  };

  /**
   * <p>Refresh backlog data</p>
   * <p>Reloads the backlog task list</p>
   */
  const refreshBacklogData = () => {
    loadBacklogList(1);
  };

  /**
   * <p>Handle search change with debounce</p>
   * <p>Debounces search input changes to avoid excessive API calls</p>
   */
  const handleSearchChange = (searchValue) => {
    search.searchValue = searchValue;
    refreshBacklogData();
  };

  /**
   * <p>Clear all search filters</p>
   * <p>Resets all search and filter criteria</p>
   */
  const clearAllFilters = () => {
    search.assigneeId = undefined;
    search.createdBy = undefined;
    search.quickDate = undefined;
    refreshBacklogData();
  };

  /**
   * <p>Toggle assigned to me filter</p>
   * <p>Switches the assigned to me filter on/off</p>
   */
  const toggleAssignedToMeFilter = (userId: string) => {
    if (!search.assigneeId) {
      search.assigneeId = userId;
    } else {
      search.assigneeId = undefined;
    }
    refreshBacklogData();
  };

  /**
   * <p>Toggle created by me filter</p>
   * <p>Switches the created by me filter on/off</p>
   */
  const toggleCreatedByMeFilter = (userId: string) => {
    if (!search.createdBy) {
      search.createdBy = userId;
    } else {
      search.createdBy = undefined;
    }
    refreshBacklogData();
  };

  /**
   * <p>Toggle date filter</p>
   * <p>Switches date-based filtering on/off</p>
   */
  const toggleDateFilter = (key: '1' | '3' | '7') => {
    if (search.quickDate === key) {
      search.quickDate = undefined;
    } else {
      search.quickDate = key;
    }
    refreshBacklogData();
  };

  /**
   * <p>Get user ID from props</p>
   * <p>Extracts user ID from component props</p>
   */
  const userId = computed(() => {
    return props.userInfo?.id;
  });

  /**
   * Determine whether there is a selection query condition
   */
  const selectNone = computed(() => {
    return !search.createdBy && !search.assigneeId && !search.quickDate;
  });

  return {
    getTaskParams,
    sortBacklogByPriority,
    loadBacklogList,
    handleBacklogTaskSort,
    refreshBacklogData,
    handleSearchChange,
    clearAllFilters,
    toggleAssignedToMeFilter,
    toggleCreatedByMeFilter,
    toggleDateFilter,
    userId,
    selectNone
  };
}
