/**
 * Represents a single activity item in the project
 * Used for displaying activities in the interface and managing their state
 */
export interface Activity {
  /** Unique identifier for the activity */
  id: string
  /** Activity timestamp */
  optDate: string
  /** Target resource ID */
  targetId: string
  /** Target resource type */
  targetType: {
    code: string
    message: string
    [key: string]: any
  }
  /** Activity title */
  title: string
  /** Full name of the operator */
  fullName: string
  /** Activity description */
  description: string
  /** Activity detail content */
  detail: string
  /** Array of activity details */
  details: string[]
  /** Operator avatar URL */
  avatar?: string
}

/**
 * API parameters for activity operations
 */
export interface ActivityApiParams {
  /** Page number for pagination */
  pageNo: number
  /** Number of items per page */
  pageSize: number
  /** Optional search filters */
  filters?: Array<{
    key: string
    op: string
    value: string | string[]
  }>
  /** Field to order by */
  orderBy?: string
  /** Sort order (ASC or DESC) */
  orderSort?: 'ASC' | 'DESC'
}

/**
 * Response structure for activity list
 */
export interface ActivityListResponse {
  /** List of activities */
  list: Activity[]
  /** Total number of activities */
  total: number
}

/**
 * Search panel filter item
 */
export interface SearchFilter {
  /** Filter key */
  key: string
  /** Filter operation */
  op: string
  /** Filter value */
  value: string | string[]
}

/**
 * Search panel change event data
 */
export interface SearchPanelChangeData {
  /** Field to order by */
  orderBy?: string
  /** Sort order */
  orderSort?: 'ASC' | 'DESC'
  /** Array of filters */
  filters: SearchFilter[]
}

/**
 * Quick search menu item
 */
export interface QuickSearchItem {
  /** Unique key for the menu item */
  key: string
  /** Display name */
  name: string
}

/**
 * Table column configuration
 */
export interface TableColumn {
  /** Unique key for the column */
  key: string
  /** Column title */
  title: string
  /** Data index for the column */
  dataIndex: string
  /** Column width */
  width?: string | number
  /** Whether the column is sortable */
  sorter?: boolean
  /** Whether to ellipsis long text */
  ellipsis?: boolean
  /** Custom cell rendering function */
  customCell?: () => any
  /** Custom render function */
  customRender?: (data: { text: any; record: any; index: number }) => any
  /** Group name for the column */
  groupName?: string
  /** Whether to hide the column */
  hide?: boolean
}
