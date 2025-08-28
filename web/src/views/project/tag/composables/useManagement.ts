import { ref, type Ref } from 'vue';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
import type { TagItem } from '../types';

/**
 * Composable for managing tag display and interaction logic
 * Handles edit states, search functionality, and UI interactions
 *
 * @param dataList - Reactive reference to tag data list
 * @param onSearch - Callback function for search operations
 * @param onTagUpdate - Callback function for tag updates
 * @returns Object containing management state and utility functions
 */
export function useManagement (
  dataList: Ref<TagItem[]>,
  onSearch: (searchTerm: string) => Promise<void>,
  onTagUpdate: (index: number, tagId: string, newName: string) => Promise<boolean>
) {
  // Search and input state management
  const searchValue = ref<string>('');
  const editId = ref<string>();

  /**
   * Debounced search handler to prevent excessive API calls
   * Uses configurable debounce duration from infra utilities
   */
  const debouncedSearch = debounce(duration.search, async (event: Event) => {
    const target = event.target as HTMLInputElement;
    const value = target.value?.trim() || '';

    searchValue.value = value;
    await onSearch(value);
  });

  /**
   * Handles search input changes with debouncing
   * Prevents API calls on every keystroke
   *
   * @param event - Input change event
   */
  const handleSearchChange = (event: Event): void => {
    debouncedSearch(event);
  };

  /**
   * Clears the current search and resets to show all tags
   * Resets search value and triggers a fresh data load
   */
  const clearSearch = async (): Promise<void> => {
    searchValue.value = '';
    await onSearch('');
  };

  /**
   * Starts editing mode for a tag
   * Sets the edit ID to enable inline editing
   *
   * @param tag - The tag to edit
   * @param disabled - Whether editing is disabled
   */
  const startEdit = (tag: TagItem, disabled = false): void => {
    if (disabled || !tag.hasEditPermission) {
      return;
    }
    editId.value = tag.id;
  };

  /**
   * Cancels the current edit operation
   * Clears the edit ID to exit inline editing mode
   */
  const cancelEdit = (): void => {
    editId.value = undefined;
  };

  /**
   * Handles Enter key press during inline editing
   * Saves the edited tag name if valid
   *
   * @param tagId - ID of the tag being edited
   * @param index - Index of the tag in the data list
   * @param event - Input event containing the new value
   */
  const handleEditEnter = async (
    tagId: string,
    index: number,
    event: { target: { value: string } }
  ): Promise<void> => {
    const newName = event.target.value?.trim();
    if (!newName) {
      return;
    }

    // Check if the name has actually changed
    const currentTag = dataList.value[index];
    if (newName === currentTag.name) {
      editId.value = undefined;
      return;
    }

    // Attempt to update the tag
    const success = await onTagUpdate(index, tagId, newName);
    if (success) {
      editId.value = undefined;
    }
  };

  /**
   * Handles blur event during inline editing
   * Auto-saves the changes after a short delay
   *
   * @param tagId - ID of the tag being edited
   * @param index - Index of the tag in the data list
   * @param event - Input blur event
   */
  const handleEditBlur = (
    tagId: string,
    index: number,
    event: { target: { value: string } }
  ): void => {
    setTimeout(() => {
      if (editId.value === tagId) {
        handleEditEnter(tagId, index, event);
      }
    }, 300);
  };

  /**
   * Finds a tag in the list by its ID
   * Returns the tag and its index if found
   *
   * @param tagId - ID of the tag to find
   * @returns Object containing the tag and its index, or null if not found
   */
  const findTagById = (tagId: string): { tag: TagItem; index: number } | null => {
    const index = dataList.value.findIndex(tag => tag.id === tagId);
    if (index === -1) {
      return null;
    }
    return {
      tag: dataList.value[index],
      index
    };
  };

  /**
   * Checks if a tag is currently being edited
   * Returns true if the tag is in edit mode
   *
   * @param tagId - ID of the tag to check
   * @returns Boolean indicating if the tag is being edited
   */
  const isTagBeingEdited = (tagId: string): boolean => {
    return editId.value === tagId;
  };

  /**
   * Gets tags that match a search term
   * Returns filtered list of tags based on name matching
   *
   * @param searchTerm - Term to search for
   * @param caseSensitive - Whether the search should be case sensitive
   * @returns Array of matching tags with their indices
   */
  const getMatchingTags = (
    searchTerm: string,
    caseSensitive = false
  ): Array<{ tag: TagItem; index: number }> => {
    if (!searchTerm.trim()) {
      return dataList.value.map((tag, index) => ({ tag, index }));
    }

    const term = caseSensitive ? searchTerm : searchTerm.toLowerCase();

    return dataList.value
      .map((tag, index) => ({ tag, index }))
      .filter(({ tag }) => {
        const tagName = caseSensitive ? tag.name : tag.name.toLowerCase();
        return tagName.includes(term);
      });
  };

  /**
   * Validates tag name input
   * Checks for various validation rules
   *
   * @param name - Tag name to validate
   * @returns Object containing validation result and error message
   */
  const validateTagName = (name: string): { isValid: boolean; errorMessage?: string } => {
    const trimmedName = name?.trim();

    if (!trimmedName) {
      return {
        isValid: false,
        errorMessage: 'Tag name cannot be empty'
      };
    }

    if (trimmedName.length > 50) {
      return {
        isValid: false,
        errorMessage: 'Tag name cannot exceed 50 characters'
      };
    }

    // Check for duplicate names (case-insensitive)
    const isDuplicate = dataList.value.some(tag =>
      tag.name.toLowerCase() === trimmedName.toLowerCase() &&
      tag.id !== editId.value
    );

    if (isDuplicate) {
      return {
        isValid: false,
        errorMessage: 'Tag name already exists'
      };
    }

    return { isValid: true };
  };

  /**
   * Formats tag display name with truncation
   * Handles long tag names for consistent UI display
   *
   * @param name - Original tag name
   * @param maxLength - Maximum length before truncation
   * @returns Object with display name and title for tooltips
   */
  const formatDisplayName = (
    name: string,
    maxLength = 20
  ): { displayName: string; title: string } => {
    if (name.length <= maxLength) {
      return {
        displayName: name,
        title: ''
      };
    }

    return {
      displayName: name.slice(0, maxLength) + '...',
      title: name
    };
  };

  /**
   * Sorts tags by different criteria
   * Provides various sorting options for tag display
   *
   * @param sortBy - Criteria to sort by ('name', 'created', 'usage')
   * @param ascending - Whether to sort in ascending order
   * @returns Sorted array of tags with indices
   */
  const sortTags = (
    sortBy: 'name' | 'created' | 'usage' = 'name',
    ascending = true
  ): Array<{ tag: TagItem; index: number }> => {
    const tagWithIndices = dataList.value.map((tag, index) => ({ tag, index }));

    return tagWithIndices.sort((a, b) => {
      let comparison = 0;

      switch (sortBy) {
        case 'name':
          comparison = a.tag.name.localeCompare(b.tag.name);
          break;
        case 'created':
          // Assuming newer tags have higher IDs (simple heuristic)
          comparison = Number(a.tag.id) - Number(b.tag.id);
          break;
        case 'usage':
          // Would need additional data for actual usage sorting
          comparison = a.tag.name.localeCompare(b.tag.name);
          break;
        default:
          comparison = 0;
      }

      return ascending ? comparison : -comparison;
    });
  };

  /**
   * Resets all management state
   * Clears search, edit states, and any temporary data
   */
  const resetState = (): void => {
    searchValue.value = '';
    editId.value = undefined;
  };

  return {
    // Reactive state
    searchValue,
    editId,

    // Search operations
    handleSearchChange,
    clearSearch,

    // Edit operations
    startEdit,
    cancelEdit,
    handleEditEnter,
    handleEditBlur,
    isTagBeingEdited,

    // Utility functions
    findTagById,
    getMatchingTags,
    validateTagName,
    formatDisplayName,
    sortTags,
    resetState
  };
}
