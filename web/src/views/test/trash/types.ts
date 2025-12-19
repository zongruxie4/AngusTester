import { SearchCriteria } from '@xcan-angus/infra';
import { FuncTargetType } from '@/enums/enums';

/**
 * Trash item interface representing deleted items in function module
 */
export interface TrashItem {
  /** Unique identifier for the trash item */
  id: string;
  /** ID of the user who created the original item */
  createdBy: string;
  /** ID of the user who deleted the item */
  deletedBy: string;
  /** Avatar URL of the item creator */
  creatorAvatar: string;
  /** Avatar URL of the item deleter */
  deletedByAvatar: string;
  /** Name of the item creator */
  creator: string;
  /** Name of the item deleter */
  deletedByName: string;
  /** Name/title of the deleted item */
  targetName: string;
  /** Original ID of the deleted item */
  targetId: string;
  /** Creation date of the original item */
  createdDate: string;
  /** Deletion date timestamp */
  deletedDate: string;
  /** Whether actions are disabled for current user */
  disabled?: boolean;
}

/**
 * Table column interface for trash table configuration
 */
export interface TableColumn {
  /** Column title displayed in header */
  title: string;
  /** Data field name to display */
  dataIndex: string;
  /** Unique key for the column */
  key: string;
  /** Column width (optional) */
  width?: string | number;
  /** Whether to show ellipsis for overflow text */
  ellipsis?: boolean;
  /** Whether column supports sorting */
  sorter?: boolean;
  /** Fixed column position */
  fixed?: 'left' | 'right';
}

/**
 * Search and filter parameters for trash data
 */
export interface TrashParams {
  /** Type of target item (CASE or PLAN) */
  targetType: FuncTargetType.CASE | FuncTargetType.PLAN;
  /** Optional name filter for search */
  filters: SearchCriteria[]
}

/**
 * Base props for trash components
 */
export interface TrashProps {
  /** Project identifier */
  projectId: string;
  /** User information */
  userInfo: { id: string };
}

/**
 * Props for the trash table component
 */
export interface TrashTableProps extends TrashProps {
  /** Search and filter parameters */
  params: TrashParams;
  /** Notification trigger for data refresh */
  notify: string;
  /** Loading state indicator */
  spinning: boolean;
}

/**
 * Pagination configuration interface
 */
export interface PaginationConfig {
  /** Total number of items */
  total: number;
  /** Current page number */
  current: number;
  /** Items per page */
  pageSize: number;
}
