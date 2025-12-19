/**
 * Target type for trash items
 */
export type TrashTargetType = 'API' | 'SERVICE';

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
 * Trash item interface
 */
export interface TrashItem {
  /** Unique identifier for the trash item */
  id: string;
  /** ID of the user who created the original item */
  createdBy: string;
  /** ID of the user who deleted the item */
  deletedBy: string;
  /** Avatar URL of the creator */
  creatorAvatar: string;
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
 * Parameters for trash data requests
 */
export interface TrashParams {
  /** Target type filter */
  targetType?: TrashTargetType;
  /** Target name filter */
  targetName?: string;
  /** Sorting field */
  orderBy?: string;
  /** Sorting direction */
  orderSort?: SortOrder;
}

/**
 * Pagination configuration
 */
export interface TrashPagination {
  /** Total number of items */
  total: number;
  /** Current page number */
  current: number;
  /** Items per page */
  pageSize: number;
}

/**
 * Table column configuration
 */
export interface TableColumn {
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
 * Props for the main trash component
 */
export interface TrashComponentProps {
  /** Current project ID */
  projectId: string;
  /** Current user information */
  userInfo: UserInfo;
}

/**
 * Props for the trash table component
 */
export interface TrashTableProps {
  /** Current project ID */
  projectId: string;
  /** Current user information */
  userInfo: UserInfo;
  /** Filter parameters */
  params: TrashParams;
  /** Refresh notification token */
  notify: string;
  /** Loading state */
  spinning: boolean;
}
