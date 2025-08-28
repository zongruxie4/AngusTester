/**
 * Target type for task trash items
 */
export type TaskTrashTargetType = 'TASK' | 'TASK_SPRINT';

/**
 * Sorting direction
 */
export type SortOrder = 'ASC' | 'DESC';

/**
 * User information interface
 */
export interface UserInfo {
  id: string;
  name?: string;
  avatar?: string;
}

/**
 * Task trash item interface
 */
export interface TaskTrashItem {
  /** Unique identifier for the trash item */
  id: string;
  /** ID of the user who created the original item */
  createdBy: string;
  /** ID of the user who deleted the item */
  deletedBy: string;
  /** Avatar URL of the creator */
  createdByAvatar: string;
  /** Avatar URL of the deleter */
  deletedByAvatar: string;
  /** Name of the creator */
  createByName: string;
  /** Name of the deleter */
  deletedByName: string;
  /** Name of the deleted target */
  targetName: string;
  /** ID of the deleted target */
  targetId: string;
  /** Creation date of the original item */
  createdDate: string;
  /** Deletion date */
  deletedDate: string;
  /** Whether the current user can perform restore/delete operations */
  disabled?: boolean;
}

/**
 * Legacy TrashItem type for compatibility
 * @deprecated Use TaskTrashItem instead
 */
export type TrashItem = TaskTrashItem;

/**
 * Parameters for task trash data requests
 */
export interface TaskTrashParams {
  /** Target type filter */
  targetType?: TaskTrashTargetType;
  /** Target name filter */
  targetName?: string;
  /** Sorting field */
  orderBy?: string;
  /** Sorting direction */
  orderSort?: SortOrder;
}

/**
 * Pagination configuration for task trash
 */
export interface TaskTrashPagination {
  /** Total number of items */
  total: number;
  /** Current page number */
  current: number;
  /** Items per page */
  pageSize: number;
}

/**
 * Table column configuration for task trash
 */
export interface TaskTableColumn {
  /** Column title */
  title: string;
  /** Data index for the column */
  dataIndex: string;
  /** Unique key for the column */
  key: string;
  /** Column width */
  width?: string | number;
  /** Whether to show ellipsis for overflow text */
  ellipsis?: boolean;
  /** Whether the column is sortable */
  sorter?: boolean;
  /** Column fixed position */
  fixed?: 'left' | 'right';
}

/**
 * Props for the main task trash component
 */
export interface TaskTrashComponentProps {
  /** Current project ID */
  projectId: string;
  /** Current user information */
  userInfo: UserInfo;
  /** Refresh notification token */
  refreshNotify: string;
}

/**
 * Props for the task trash table component
 */
export interface TaskTrashTableProps {
  /** Current project ID */
  projectId: string;
  /** Current user information */
  userInfo: UserInfo;
  /** Filter parameters */
  params: TaskTrashParams;
  /** Refresh notification token */
  notify: string;
  /** Loading state */
  spinning: boolean;
}
