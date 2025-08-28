import { ref, computed, type Ref } from 'vue';
import { tag } from '@/api/tester';
import type { TagItem, TagApiParams, TagListResponse, RawTagData } from '../types';

/**
 * Composable for managing tag data operations
 * Handles data fetching, loading states, search functionality, and pagination
 *
 * @param projectId - Reactive project ID reference
 * @returns Object containing reactive data and data management functions
 */
export function useData (projectId: Ref<string>) {
  // Reactive state for tag data management
  const dataList = ref<TagItem[]>([]);
  const loading = ref(false);
  const loaded = ref(false);
  const searchedFlag = ref(false);
  const total = ref(0);
  const pageNo = ref(1);
  const pageSize = ref(200);

  /**
   * Computed property to determine if "Load More" button should be shown
   * Returns true when there are more items to load
   */
  const showLoadMore = computed(() => {
    return dataList.value.length < total.value;
  });

  /**
   * Formats raw tag data for display
   * Handles name truncation and permission settings
   *
   * @param data - Raw tag data from API
   * @returns Formatted tag item for display
   */
  const formatTagItem = (data: RawTagData): TagItem => {
    const MAX_LENGTH = 20;
    let showTitle = '';
    let showName = data.name;

    // Truncate long names and provide full name in title
    if (data.name?.length > MAX_LENGTH) {
      showTitle = data.name;
      showName = showName.slice(0, MAX_LENGTH) + '...';
    }

    return {
      ...data,
      showTitle,
      showName,
      hasEditPermission: data.hasEditPermission ?? true
    };
  };

  /**
   * Builds API parameters for tag queries
   * Handles search filters and pagination
   *
   * @param searchValue - Optional search term for filtering tags
   * @param customPageNo - Optional custom page number
   * @param customPageSize - Optional custom page size
   * @returns Formatted parameters object for API calls
   */
  const buildApiParams = (
    searchValue?: string,
    customPageNo?: number,
    customPageSize?: number
  ): TagApiParams => {
    const params: TagApiParams = {
      pageNo: customPageNo ?? pageNo.value,
      pageSize: customPageSize ?? pageSize.value,
      projectId: projectId.value
    };

    // Add search filters if search value is provided
    if (searchValue?.trim()) {
      params.filters = [{
        key: 'name',
        op: 'MATCH_END',
        value: searchValue.trim()
      }];
    }

    return params;
  };

  /**
   * Fetches tag list data from the API
   * Processes the data for display and updates reactive state
   *
   * @param remainder - Number of items to skip (for pagination adjustments)
   * @param searchValue - Optional search term for filtering
   * @param customParams - Additional parameters to merge with default params
   * @returns Promise that resolves when data is loaded
   */
  const fetchTagList = async (
    remainder = 0,
    searchValue?: string,
    customParams?: { pageNo?: number; pageSize?: number; }
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

      // Make API call to fetch tag list
      const [error, response] = await tag.getTagList(params);

      if (error) {
        console.error('Failed to fetch tag list:', error);
        return;
      }

      // Process response data
      const data = response?.data as TagListResponse;
      if (!data) {
        return;
      }

      // Extract and format tag items
      const rawList = (data.list || []).slice(remainder);
      const formattedList = rawList.map(item => formatTagItem(item));

      // Update data list based on pagination context
      if (params.pageNo > 1) {
        // Append to existing list for "Load More" functionality
        dataList.value.push(...formattedList);
      } else {
        // Replace entire list for new searches or refreshes
        dataList.value = formattedList;
      }

      // Update total count and search state
      total.value = Number(data.total);
      searchedFlag.value = Boolean(params.filters?.length);
    } catch (error) {
      console.error('Unexpected error while fetching tag list:', error);
    } finally {
      loading.value = false;
      loaded.value = true;
    }
  };

  /**
   * Loads more tags for pagination
   * Calculates correct page numbers after add/delete operations
   */
  const loadMoreTags = (): Promise<void> => {
    // Calculate remainder and current page after operations
    const remainder = dataList.value.length % pageSize.value;
    const currentPage = Math.floor(dataList.value.length / pageSize.value);

    pageNo.value = currentPage + 1;
    return fetchTagList(remainder);
  };

  /**
   * Refreshes the tag list data
   * Resets to first page and refetches data
   */
  const refreshData = (): Promise<void> => {
    pageNo.value = 1;
    return fetchTagList();
  };

  /**
   * Searches tags by name
   * Resets pagination and applies search filter
   *
   * @param searchTerm - Search term to filter tags
   */
  const searchTags = (searchTerm: string): Promise<void> => {
    pageNo.value = 1;
    return fetchTagList(0, searchTerm);
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
    pageNo.value = 1;
  };

  /**
   * Adds a new tag to the beginning of the list
   * Updates total count and formats the new tag
   *
   * @param newTag - New tag data to add
   */
  const addTagToList = (newTag: { id: string; name: string }): void => {
    const formattedTag = formatTagItem(newTag);
    dataList.value.unshift(formattedTag);
    total.value += 1;
  };

  /**
   * Removes a tag from the list by index
   * Updates total count and loads additional data if needed
   *
   * @param index - Index of the tag to remove
   */
  const removeTagFromList = async (index: number): Promise<void> => {
    dataList.value.splice(index, 1);
    total.value -= 1;

    // Load additional data if there are more items available
    if (index < total.value) {
      const params = {
        pageNo: dataList.value.length + 1,
        pageSize: 1
      };
      await fetchTagList(0, undefined, params);
    }
  };

  /**
   * Updates a tag in the list by index
   * Formats the updated tag data
   *
   * @param index - Index of the tag to update
   * @param updatedData - Updated tag data
   */
  const updateTagInList = (index: number, updatedData: { id: string; name: string; hasEditPermission?: boolean }): void => {
    const existingTag = dataList.value[index];
    const updatedTag = formatTagItem({
      ...updatedData,
      hasEditPermission: updatedData.hasEditPermission ?? existingTag.hasEditPermission
    });
    dataList.value[index] = updatedTag;
  };

  // Return reactive state and methods
  return {
    // Reactive state
    dataList,
    loading,
    loaded,
    searchedFlag,
    total,
    pageNo,
    pageSize,
    showLoadMore,

    // Data management methods
    fetchTagList,
    loadMoreTags,
    refreshData,
    searchTags,
    resetData,
    addTagToList,
    removeTagFromList,
    updateTagInList,
    formatTagItem,
    buildApiParams
  };
}
