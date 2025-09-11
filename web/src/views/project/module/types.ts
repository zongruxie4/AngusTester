/**
 * Type definitions for project module management components
 * These types ensure type safety across the module management system
 */

/**
 * Represents a single module item in the project structure
 * Used for displaying modules in tree format and managing their hierarchy
 */
export interface ModuleItem {
  /** Unique identifier for the module */
  id: string;
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
  ids?: string[];
  /** Whether this is the last item in its level */
  isLast?: boolean;
  /** Parent module ID (-1 for root level) */
  pid?: string;
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
  projectId: string;
  /** Current user information */
  userInfo: { id: string; };
  /** Application information */
  appInfo: { id: string; };
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
  projectId: string;
  /** Whether the modal is visible */
  visible: boolean;
  /** Parent module ID (optional for root level modules) */
  pid?: string;
}

/**
 * Props interface for the move module modal component
 */
export interface MoveModuleProps {
  /** Whether the modal is visible */
  visible: boolean;
  /** Project ID containing the module */
  projectId: string;
  /** Project name for display in tree root */
  projectName: string;
  /** Module to be moved */
  module: {
    /** Module ID */
    id: string;
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
  projectId: string;
  /** Optional search filters */
  filters?: Array<{
    key: 'name';
    op: 'MATCH';
    value: string;
  }>;
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
  id: string;
  /** New module name (optional) */
  name?: string;
  /** New parent ID (optional) */
  pid?: string;
  /** New sequence number (optional) */
  sequence?: number;
}

/**
 * Parameters for deleting modules
 */
export interface DeleteModuleParams {
  /** Array of module IDs to delete */
  ids: string[];
}
