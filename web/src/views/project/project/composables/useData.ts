import { ref, computed, reactive } from 'vue';
import { debounce } from 'throttle-debounce';
import { PageQuery, SearchCriteria, duration } from '@xcan-angus/infra';
import { project } from '@/api/tester';
import type {
  Project,
  ProjectApiParams
} from '../types';

/**
 * Project data management composable
 * Handles data fetching, pagination, search, and state management for project lists
 *
 * @returns {Object} Project data management utilities and state
 */
export function useData () {
  /** Loading state for API requests */
  const loading = ref(false);

  /** Search keyword for filtering projects */
  const keyword = ref<string>('');

  /** Project list data */
  const data = ref<Project[]>([]);

  /** Project detail data for detail view */
  const detailData = ref<Project>({} as Project);

  /** Current sort field */
  const orderBy = ref<'createdDate' | 'creator'>('createdDate');

  /** Current sort direction */
  const orderSort = ref<PageQuery.OrderSort>(PageQuery.OrderSort.Desc);

  /** Pagination configuration */
  const pagination = reactive({
    pageSize: 4,
    current: 1,
    total: 0,
    pageSizeOptions: [4],
    hideOnSinglePage: true
  });
  /**
   * Check if there are any projects in the current view
   * @returns {boolean} True if no projects are available
   */
  const isEmpty = computed(() => data.value.length === 0);

  /**
   * Check if search is active
   * @returns {boolean} True if user is searching
   */
  const isSearching = computed(() => !!keyword.value?.trim());

  /**
   * Get formatted pagination info for display
   * @returns {string} Pagination summary text
   */
  const paginationInfo = computed(() => {
    const { current, pageSize, total } = pagination;
    const start = (current - 1) * pageSize + 1;
    const end = Math.min(current * pageSize, total);
    return `${start}-${end} of ${total}`;
  });

  /**
   * Build API query parameters from current state
   * @returns {ProjectApiParams} Formatted API parameters
   */
  const buildQueryParams = (): ProjectApiParams => {
    const params: ProjectApiParams = {
      pageNo: pagination.current,
      pageSize: pagination.pageSize
    };

    // Add search filter if keyword exists
    if (keyword.value?.trim()) {
      params.filters = [{
        value: keyword.value.trim(),
        op: SearchCriteria.OpEnum.Match,
        key: 'name'
      }];
    }

    // Add sorting if specified
    if (orderSort.value && orderBy.value) {
      params.orderBy = orderBy.value;
      params.orderSort = orderSort.value;
    }
    return params;
  };

  /**
   * Process raw project data to add computed fields and format member display
   * @param {Project[]} projects - Raw project data from API
   * @returns {Project[]} Processed project data
   */
  const processProjectData = (projects: Project[]): Project[] => {
    return projects.map(item => {
      // Calculate total member count
      const userCount = item.members?.USER?.length || 0;
      const groupCount = item.members?.GROUP?.length || 0;
      const deptCount = item.members?.DEPT?.length || 0;
      item.membersNum = userCount + groupCount + deptCount;

      // Prepare members for display (limited to 10 for performance)
      item.showMembers = {
        USER: [],
        GROUP: [],
        DEPT: []
      };

      // Add users first (up to 10)
      const availableUsers = item.members?.USER || [];
      item.showMembers.USER = availableUsers.slice(0, 10);

      // Add groups if space remaining
      if (item.showMembers.USER.length < 10) {
        const availableGroups = item.members?.GROUP || [];
        const remainingSlots = 10 - item.showMembers.USER.length;
        item.showMembers.GROUP = availableGroups.slice(0, remainingSlots);
      }

      // Add departments if space remaining
      const usedSlots = item.showMembers.USER.length + item.showMembers.GROUP.length;
      if (usedSlots < 10) {
        const availableDepts = item.members?.DEPT || [];
        const remainingSlots = 10 - usedSlots;
        item.showMembers.DEPT = availableDepts.slice(0, remainingSlots);
      }

      return item;
    });
  };

  /**
   * Fetch project list from API
   * @returns {Promise<void>}
   */
  const fetchProjectList = async (): Promise<void> => {
    try {
      loading.value = true;
      const params = buildQueryParams();

      const [error, response] = await project.getProjectList(params);

      if (error) {
        console.error('Failed to fetch project list:', error);
        return;
      }

      // Process and set project data
      const rawData = response.data?.list || [];
      data.value = processProjectData(rawData);
      pagination.total = Number(response.data?.total) || 0;
    } catch (error) {
      console.error('Error fetching project list:', error);
    } finally {
      loading.value = false;
    }
  };

  /**
   * Get detailed project information by ID and update detailData
   * @param {string} projectId - Project ID to fetch
   * @returns {Promise<Project | null>} Project details or null if error
   */
  const fetchProjectDetail = async (projectId: string): Promise<Project | null> => {
    if (!projectId) return null;

    try {
      loading.value = true;
      const [error, response] = await project.getProjectDetail(projectId);

      if (error) {
        console.error('Failed to fetch project detail:', error);
        return null;
      }

      const projectData = response.data || null;
      if (projectData) {
        // Process and normalize project detail data
        detailData.value = {
          ...projectData,
          type: projectData.type || { value: 'AGILE', message: 'Agile' },
          members: projectData.members || { USER: [], DEPT: [], GROUP: [] }
        };
      }

      return projectData;
    } catch (error) {
      console.error('Error fetching project detail:', error);
      return null;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Get detailed project information by ID (without updating detailData)
   * @param {string} projectId - Project ID to fetch
   * @returns {Promise<Project | null>} Project details or null if error
   */
  const getProjectDetail = async (projectId: string): Promise<Project | null> => {
    if (!projectId) return null;

    try {
      const [error, response] = await project.getProjectDetail(projectId);

      if (error) {
        console.error('Failed to fetch project detail:', error);
        return null;
      }

      return response.data || null;
    } catch (error) {
      console.error('Error fetching project detail:', error);
      return null;
    }
  };

  /**
   * Debounced search handler to avoid excessive API calls
   */
  const debouncedSearch = debounce(duration.search, () => {
    pagination.current = 1; // Reset to first page
    fetchProjectList();
  });

  /**
   * Handle search keyword change
   * @param {string} newKeyword - New search keyword
   */
  const handleSearch = (newKeyword: string): void => {
    keyword.value = newKeyword;
    debouncedSearch();
  };

  /**
   * Clear search and reset to show all projects
   */
  const clearSearch = (): void => {
    keyword.value = '';
    pagination.current = 1;
    fetchProjectList();
  };

  /**
   * Update sorting configuration and refresh data
   * @param {Object} sortConfig - Sort configuration
   * @param {string} sortConfig.orderBy - Field to sort by
   * @param {PageQuery.OrderSort} sortConfig.orderSort - Sort direction
   */
  const updateSort = (sortConfig: {
    orderBy: 'createdDate' | 'creator';
    orderSort: PageQuery.OrderSort;
  }): void => {
    orderBy.value = sortConfig.orderBy;
    orderSort.value = sortConfig.orderSort;
    pagination.current = 1; // Reset to first page
    fetchProjectList();
  };

  /**
   * Handle page change
   * @param {number} page - New page number
   */
  const handlePageChange = (page: number): void => {
    pagination.current = page;
    fetchProjectList();
  };

  /**
   * Handle page size change
   * @param {number} current - Current page
   * @param {number} size - New page size
   */
  const handlePageSizeChange = (current: number, size: number): void => {
    pagination.current = current || 1; // Reset to first page
    pagination.pageSize = size;
    fetchProjectList();
  };

  /**
   * Refresh current page data
   */
  const refreshData = (): void => {
    fetchProjectList();
  };

  /**
   * Reset all filters and sorting to default state
   */
  const resetFilters = (): void => {
    keyword.value = '';
    orderBy.value = 'createdDate';
    orderSort.value = PageQuery.OrderSort.Desc;
    pagination.current = 1;
    fetchProjectList();
  };

  /**
   * Find project by ID in current data
   * @param {string} projectId - Project ID to find
   * @returns {Project | undefined} Found project or undefined
   */
  const findProjectById = (projectId: string): Project | undefined => {
    return data.value.find(project => project.id === projectId);
  };

  /**
   * Update a specific project in the current data
   * @param {string} projectId - Project ID to update
   * @param {Partial<Project>} updates - Partial project data to update
   */
  const updateProjectInList = (projectId: string, updates: Partial<Project>): void => {
    const index = data.value.findIndex(project => project.id === projectId);
    if (index !== -1) {
      data.value[index] = { ...data.value[index], ...updates };
    }
  };

  /**
   * Remove a project from the current data
   * @param {string} projectId - Project ID to remove
   */
  const removeProjectFromList = (projectId: string): void => {
    const index = data.value.findIndex(project => project.id === projectId);
    if (index !== -1) {
      data.value.splice(index, 1);
      pagination.total = Math.max(0, pagination.total - 1);
    }
  };

  return {
    // State
    loading,
    keyword,
    data,
    detailData,
    orderBy,
    orderSort,
    pagination,

    // Computed
    isEmpty,
    isSearching,
    paginationInfo,

    // API functions
    fetchProjectList,
    fetchProjectDetail,
    getProjectDetail,

    // Search functions
    handleSearch,
    clearSearch,

    // Sort functions
    updateSort,

    // Pagination functions
    handlePageChange,
    handlePageSizeChange,

    // Utility functions
    refreshData,
    resetFilters,
    findProjectById,
    updateProjectInList,
    removeProjectFromList
  };
}

export type UseDataReturn = ReturnType<typeof useData>;
