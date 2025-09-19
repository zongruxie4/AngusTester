/**
 * Trash item interface representing deleted scenario items
 */
export interface TrashItem {
  /** Unique identifier for the trash item */
  id: string;
  /** ID of the user who created the original item */
  createdBy: string;
  /** ID of the user who deleted the item */
  deletedBy: string;
  /** Avatar URL of the item creator */
  createdByAvatar: string;
  /** Avatar URL of the item deleter */
  deletedByAvatar: string;
  /** Name of the item creator */
  createdByName: string;
  /** Name of the item deleter */
  deletedByName: string;
  /** Name/title of the deleted scenario */
  targetName: string;
  /** Original ID of the deleted scenario */
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
  /** Optional name filter for search */
  filters?: {value: string, op: string, key: string}[]
}
