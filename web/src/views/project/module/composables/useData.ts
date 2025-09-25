import { ref, type Ref } from 'vue';
import { modules } from '@/api/tester';
import type { ModuleItem, ModuleApiParams } from '../types';
import { travelTreeData } from '@/utils/utils';

/**
 * Composable for managing module data operations
 * Handles data fetching, loading states, and search functionality
 *
 * @param projectId - Reactive project ID reference
 * @returns Object containing reactive data and data management functions
 */
export function useData (projectId: Ref<string>) {
  // Reactive state for module data management
  const dataList = ref<ModuleItem[]>([]);
  const loading = ref(false);
  const loaded = ref(false);
  const searchedFlag = ref(false);
  const total = ref(0);

  /**
   * Builds API parameters for module queries
   * Handles search filters when provided
   *
   * @param searchValue - Optional search term for filtering modules
   * @returns Formatted parameters object for API calls
   */
  const buildApiParams = (searchValue?: string): ModuleApiParams => {
    const params: ModuleApiParams = {
      projectId: projectId.value
    };

    // Add search filters if search value is provided
    if (searchValue?.trim()) {
      params.filters = [{
        key: 'name',
        op: 'MATCH',
        value: searchValue.trim()
      }];
    }

    return params;
  };

  /**
   * Fetches module tree data from the API
   * Processes the data for tree display and updates reactive state
   *
   * @param searchValue - Optional search term for filtering
   * @param customParams - Additional parameters to merge with default params
   * @returns Promise that resolves when data is loaded
   */
  const fetchModuleTree = async (
    searchValue?: string,
    customParams?: Record<string, any>
  ): Promise<void> => {
    // Prevent API calls if no project is selected
    if (!projectId.value) {
      return;
    }

    loading.value = true;

    try {
      // Build API parameters
      let params = buildApiParams(searchValue);

      // Merge with custom parameters if provided
      if (customParams) {
        params = { ...params, ...customParams };
      }

      // Make API call to fetch module tree
      const [error, response] = await modules.getModuleTree(params);

      if (error) {
        console.error('Failed to fetch module tree:', error);
        return;
      }

      // Process and transform the data for tree display
      const rawData = response?.data || [];
      const processedData = rawData as Array<{id: string; name: string}>;

      // Transform data using utility function for tree structure
      dataList.value = travelTreeData(processedData);

      // Update search state based on whether filters were applied
      searchedFlag.value = Boolean(params.filters?.length);

      // Update total count
      total.value = processedData.length;
    } catch (error) {
      console.error('Unexpected error while fetching module tree:', error);
    } finally {
      loading.value = false;
      loaded.value = true;
    }
  };

  /**
   * Refreshes the module tree data
   * Resets to first page and refetches data
   */
  const refreshData = (): Promise<void> => {
    return fetchModuleTree();
  };

  /**
   * Resets all data state to initial values
   * Used when switching projects or clearing data
   */
  const resetData = (): void => {
    dataList.value = [];
    loading.value = false;
    loaded.value = false;
    searchedFlag.value = false;
    total.value = 0;
  };

  /**
   * Searches modules by name
   * Debounced search function to prevent excessive API calls
   *
   * @param searchTerm - Search term to filter modules
   */
  const searchModules = (searchTerm: string): Promise<void> => {
    return fetchModuleTree(searchTerm);
  };

  // Return reactive state and methods
  return {
    // Reactive state
    dataList,
    loading,
    loaded,
    searchedFlag,
    total,

    // Data management methods
    fetchModuleTree,
    refreshData,
    resetData,
    searchModules,
    buildApiParams
  };
}
