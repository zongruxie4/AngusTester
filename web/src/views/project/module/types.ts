import { SearchCriteria } from '@xcan-angus/infra';

/**
 * Represents a single module item in the project structure
 * Used for displaying modules in tree format and managing their hierarchy
 */
export interface ModuleItem {
  /** Unique identifier for the module */
  id: number;
  /** Display name of the module */
  name: string;
  /** Optional key for tree component compatibility */
  key?: string;
  /** Optional display name for special formatting */
  showName?: string;
  /** Optional display title for tooltips */
  showTitle?: string;
  /** Child modules in hierarchical structure */
  children?: ModuleItem[];
  /** Depth level in the tree (0-based) */
  level?: number;
  /** Index position among siblings */
  index?: number;
  /** Array of parent IDs leading to this module */
  ids?: number[];
  /** Whether this is the last item in its level */
  isLast?: boolean;
  /** Parent module ID (-1 for root level) */
  pid?: number;
  /** Sort sequence number for ordering */
  sequence?: string;
  /** Maximum depth of child levels */
  childLevels?: number;
  /** Whether current user can edit this module */
  hasEditPermission?: boolean;
  /** Whether the module is disabled for selection */
  disabled?: boolean;
}

/**
 * Props interface for the main module management component
 */
export interface ModuleProps {
  /** Project ID to manage modules for */
  projectId: number;
  /** Current user information */
  userInfo: { id: number; };
  /** Application information */
  appInfo: { id: number; };
  /** Notification settings */
  notify: string;
  /** Whether the component is in read-only mode */
  disabled: boolean;
  /** Project name for display purposes */
  projectName: string;
}

/**
 * Props interface for the add module modal component
 */
export interface AddModuleProps {
  /** Project ID where the module will be created */
  projectId: number;
  /** Whether the modal is visible */
  visible: boolean;
  /** Parent module ID (optional for root level modules) */
  pid?: number;
}

/**
 * Props interface for the move module modal component
 */
export interface MoveModuleProps {
  /** Whether the modal is visible */
  visible: boolean;
  /** Project ID containing the module */
  projectId: number;
  /** Project name for display in tree root */
  projectName: string;
  /** Module to be moved */
  module: {
    /** Module ID */
    id: number;
    /** Number of child levels this module contains */
    childLevels: number;
    /** Current parent ID */
    pid: string;
  };
}

/**
 * API parameters for module operations
 */
export interface ModuleApiParams {
  /** Project ID */
  projectId: number;
  /** Optional search filters */
  filters?: SearchCriteria[];
}

/**
 * Parameters for creating new modules
 */
export interface CreateModuleParams {
  /** Array of module names to create */
  names: string[];
  /** Project ID */
  projectId: string;
  /** Parent module ID (optional) */
  pid?: string;
}

/**
 * Parameters for updating existing modules
 */
export interface UpdateModuleParams {
  /** Module ID */
  id: number;
  /** New module name (optional) */
  name?: string;
  /** New parent ID (optional) */
  pid?: number;
  /** New sequence number (optional) */
  sequence?: number;
}

/**
 * Parameters for deleting modules
 */
export interface DeleteModuleParams {
  /** Array of module IDs to delete */
  ids: number[];
}
