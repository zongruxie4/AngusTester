/**
 * Type definitions for project tag management components
 * These types ensure type safety across the tag management system
 */

/**
 * Represents a single tag item in the project
 * Used for displaying tags in the interface and managing their state
 */
export interface TagItem {
  /** Unique identifier for the tag */
  id: string;
  /** Full name of the tag */
  name: string;
  /** Display name (may be truncated) */
  showName: string;
  /** Full title for tooltips when name is truncated */
  showTitle: string;
  /** Whether current user can edit this tag */
  hasEditPermission: boolean;
}

/**
 * Props interface for the main tag management component
 */
export interface TagProps {
  /** Project ID to manage tags for */
  projectId: string;
  /** Current user information */
  userInfo: { id: string; };
  /** Application information */
  appInfo: { id: string; };
  /** Notification settings */
  notify: string;
  /** Whether the component is in read-only mode */
  disabled: boolean;
}

/**
 * Props interface for the add tag modal component
 */
export interface AddTagProps {
  /** Project ID where the tag will be created */
  projectId: string;
  /** Whether the modal is visible */
  visible: boolean;
}

/**
 * API parameters for tag operations
 */
export interface TagApiParams {
  /** Page number for pagination */
  pageNo: number;
  /** Number of items per page */
  pageSize: number;
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
 * Parameters for creating new tags
 */
export interface CreateTagParams {
  /** Array of tag names to create */
  names: string[];
  /** Project ID */
  projectId: string;
}

/**
 * Parameters for updating existing tags
 */
export interface UpdateTagParams {
  /** Tag ID */
  id: string;
  /** New tag name */
  name: string;
}

/**
 * API response structure for tag list
 */
export interface TagListResponse {
  /** List of tags */
  list: Array<{
    id: string;
    name: string;
    hasEditPermission?: boolean;
  }>;
  /** Total number of tags */
  total: number;
}

/**
 * Raw tag data from API before formatting
 */
export interface RawTagData {
  /** Tag ID */
  id: string;
  /** Tag name */
  name: string;
  /** Whether user can edit this tag (optional) */
  hasEditPermission?: boolean;
}
