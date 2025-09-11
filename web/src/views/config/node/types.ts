/**
 * Search option configuration for node filtering
 */
export interface SearchOption {
  /** Input type for the search field */
  type: 'input' | 'select-enum' | 'select-user' | 'select-enum';
  /** Whether to allow clearing the input */
  allowClear: boolean;
  /** Key for the search parameter */
  valueKey: string;
  /** Display label for the search field */
  label: string;
  /** Placeholder text for the input */
  placeholder: string;
  /** Maximum length for input fields */
  maxlength?: number;
  /** Operation type for filtering */
  op?: 'EQUAL' | 'GREATER_THAN' | 'GREATER_THAN_EQUAL' | 'IN' | 'LESS_THAN' | 'LESS_THAN_EQUAL' | 'MATCH' | 'MATCH' | 'NOT_EQUAL' | 'NOT_IN';
  /** Enum key for select-enum type */
  enumKey?: any;
}

/**
 * Pagination configuration
 */
export interface PaginationConfig {
  /** Current page number */
  current: number;
  /** Number of items per page */
  pageSize: number;
  /** Function to display total count */
  showTotal: (total: number) => string;
  /** Whether to show size changer */
  showSizeChanger: boolean;
  /** Total number of items */
  total: number;
}

/**
 * Sort option configuration
 */
export interface SortOption {
  /** Display name for the sort option */
  name: string;
  /** Field key to sort by */
  key: string;
  /** Sort direction (ASC/DESC) */
  orderSort: 'ASC' | 'DESC';
  /** Additional properties for component compatibility */
  [key: string]: any;
}

/**
 * Node specification data
 */
export interface NodeSpec {
  /** CPU specification */
  cpu?: string;
  /** Memory specification */
  memory?: string;
  /** Disk specification */
  disk?: string;
  /** Network specification */
  network?: string;
  /** Display label for specifications */
  showLabel?: string;
}

/**
 * Node data structure
 */
export interface NodeData {
  /** Node ID */
  id: string;
  /** Node name */
  name: string;
  /** Node IP address */
  ip: string;
  /** Node specifications */
  spec: NodeSpec;
  /** Node source information */
  source: {
    /** Source message */
    message: string;
  };
  /** Whether node is editable */
  editable: boolean;
  /** Source name for display */
  sourceName: string;
  /** Standard specification display */
  standard: string;
  /** Monitoring data */
  monitorData: Record<string, any>;
  /** Node roles */
  roles?: any[];
  /** Created by user */
  createdBy?: string;
}

/**
 * Role option for node assignment
 */
export interface RoleOption {
  /** Role name */
  name: string;
  /** Display label */
  label: string;
  /** Role value */
  value: string;
  /** Role message */
  message: string;
  /** Whether role is disabled */
  disabled?: boolean;
}

/**
 * Node list response data
 */
export interface NodeListResponse {
  /** List of nodes */
  list: NodeData[];
  /** Total count of nodes */
  total: number;
}
