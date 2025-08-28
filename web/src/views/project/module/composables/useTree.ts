import { ref, computed, type Ref } from 'vue';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
import type { ModuleItem } from '../types';

/**
 * Composable for managing module tree operations and search functionality
 * Handles tree structure processing, search operations, and UI state
 *
 * @param dataList - Reactive reference to module data list
 * @param onSearch - Callback function for search operations
 * @returns Object containing search state and tree utility functions
 */
export function useTree(
  dataList: Ref<ModuleItem[]>,
  onSearch: (searchTerm: string) => Promise<void>
) {
  // Search and input state management
  const searchValue = ref<string>('');
  const nameInputRef = ref();

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
   * Clears the current search and resets to show all modules
   * Resets search value and triggers a fresh data load
   */
  const clearSearch = async (): Promise<void> => {
    searchValue.value = '';
    await onSearch('');
  };

  /**
   * Computes whether a module can be moved up in the hierarchy
   * Checks if the module has a parent or is not the first in its level
   *
   * @param module - Module item to check
   * @returns Boolean indicating if upward movement is possible
   */
  const canMoveUp = computed(() => (module: ModuleItem): boolean => {
    const index = module.index || 0;
    const pid = module.pid;

    // Can move up if not at first position or has a parent to move up to
    return index > 0 || (pid !== undefined && Number(pid) > 0);
  });

  /**
   * Computes whether a module can be moved down in the hierarchy
   * Checks if the module is not the last in its level
   *
   * @param module - Module item to check
   * @returns Boolean indicating if downward movement is possible
   */
  const canMoveDown = computed(() => (module: ModuleItem): boolean => {
    return !module.isLast;
  });

  /**
   * Computes whether a new sub-module can be added under this module
   * Checks the current level depth to enforce maximum nesting limits
   *
   * @param module - Module item to check
   * @returns Boolean indicating if sub-module addition is allowed
   */
  const canAddSubModule = computed(() => (module: ModuleItem): boolean => {
    const level = module.level || 0;
    // Assuming maximum depth of 4 levels (0-4)
    return level < 4;
  });

  /**
   * Processes tree data to add computed properties for UI display
   * Adds navigation and permission information to each node
   *
   * @param treeData - Raw tree data from API
   * @param userPermissions - Current user's permissions
   * @returns Processed tree data with UI properties
   */
  const processTreeData = (
    treeData: ModuleItem[],
    userPermissions?: { canEdit?: boolean }
  ): ModuleItem[] => {
    const processNode = (node: ModuleItem, index: number, siblings: ModuleItem[]): ModuleItem => {
      const processedNode: ModuleItem = {
        ...node,
        index,
        isLast: index === siblings.length - 1,
        hasEditPermission: userPermissions?.canEdit ?? true
      };

      // Recursively process children
      if (node.children && node.children.length > 0) {
        processedNode.children = node.children.map((child, childIndex) =>
          processNode(child, childIndex, node.children!)
        );
      }

      return processedNode;
    };

    return treeData.map((node, index) => processNode(node, index, treeData));
  };

  /**
   * Finds a module in the tree by its ID
   * Recursively searches through the tree structure
   *
   * @param moduleId - ID of the module to find
   * @param treeData - Tree data to search in (defaults to current dataList)
   * @returns Found module item or undefined
   */
  const findModuleById = (
    moduleId: string,
    treeData: ModuleItem[] = dataList.value
  ): ModuleItem | undefined => {
    for (const node of treeData) {
      if (node.id === moduleId) {
        return node;
      }

      if (node.children) {
        const found = findModuleById(moduleId, node.children);
        if (found) {
          return found;
        }
      }
    }

    return undefined;
  };

  /**
   * Gets the path from root to a specific module
   * Returns array of module IDs representing the path
   *
   * @param moduleId - Target module ID
   * @param treeData - Tree data to search in
   * @returns Array of module IDs from root to target
   */
  const getModulePath = (
    moduleId: string,
    treeData: ModuleItem[] = dataList.value
  ): string[] => {
    const findPath = (nodes: ModuleItem[], targetId: string, path: string[] = []): string[] | null => {
      for (const node of nodes) {
        const currentPath = [...path, node.id];

        if (node.id === targetId) {
          return currentPath;
        }

        if (node.children) {
          const found = findPath(node.children, targetId, currentPath);
          if (found) {
            return found;
          }
        }
      }

      return null;
    };

    return findPath(treeData, moduleId) || [];
  };

  /**
   * Flattens the tree structure into a linear array
   * Useful for operations that need to work with all modules
   *
   * @param treeData - Tree data to flatten
   * @returns Flattened array of all modules
   */
  const flattenTree = (treeData: ModuleItem[] = dataList.value): ModuleItem[] => {
    const result: ModuleItem[] = [];

    const flatten = (nodes: ModuleItem[]): void => {
      for (const node of nodes) {
        result.push(node);
        if (node.children) {
          flatten(node.children);
        }
      }
    };

    flatten(treeData);
    return result;
  };

  /**
   * Counts total modules in the tree
   * Includes all nested modules
   *
   * @param treeData - Tree data to count
   * @returns Total number of modules
   */
  const getTotalModuleCount = computed(() => (): number => {
    return flattenTree().length;
  });

  /**
   * Gets modules at a specific level
   * Returns all modules at the specified depth
   *
   * @param level - Target level (0-based)
   * @param treeData - Tree data to search
   * @returns Array of modules at the specified level
   */
  const getModulesAtLevel = (
    level: number,
    treeData: ModuleItem[] = dataList.value
  ): ModuleItem[] => {
    const result: ModuleItem[] = [];

    const collectAtLevel = (nodes: ModuleItem[], currentLevel: number): void => {
      for (const node of nodes) {
        if (currentLevel === level) {
          result.push(node);
        }

        if (node.children && currentLevel < level) {
          collectAtLevel(node.children, currentLevel + 1);
        }
      }
    };

    collectAtLevel(treeData, 0);
    return result;
  };

  return {
    // Reactive state
    searchValue,
    nameInputRef,

    // Search operations
    handleSearchChange,
    clearSearch,

    // Tree structure utilities
    canMoveUp,
    canMoveDown,
    canAddSubModule,
    processTreeData,
    findModuleById,
    getModulePath,
    flattenTree,
    getTotalModuleCount,
    getModulesAtLevel
  };
}
