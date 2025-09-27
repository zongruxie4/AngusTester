import { VariableItem } from '../types';

/**
 * Table pagination configuration
 */
export interface TablePagination {
  /** Current page number */
  current: number;
  /** Number of items per page */
  pageSize: number;
  /** Total number of items */
  total: number;
}

/**
 * Row selection configuration
 */
export interface RowSelection {
  /** Callback when selection changes */
  onChange: (key: string[]) => void;
  /** Get checkbox properties for a row */
  getCheckboxProps: (data: VariableItem) => { disabled: boolean };
  /** Selected row keys */
  selectedRowKeys: string[];
}

/**
 * Table column configuration
 */
export interface TableColumn {
  /** Column title */
  title: string;
  /** Data field name */
  dataIndex: string;
  /** Whether to show ellipsis */
  ellipsis?: boolean;
  /** Column width */
  width?: string;
  /** Whether column is sortable */
  sorter?: boolean;
  /** Group name for sorting */
  groupName?: string;
  /** Whether column is hidden */
  hide?: boolean;
  /** Custom render function (deprecated) */
  customRender?: (params: { text: any; record: VariableItem }) => any;
  /** Custom cell function */
  customCell?: (record: VariableItem) => any;
}

/**
 * Dropdown menu item configuration
 */
export interface DropdownMenuItem {
  /** Display name */
  name: string;
  /** Unique key */
  key: string;
  /** Icon name */
  icon?: string;
  /** Whether no auth required */
  noAuth?: boolean;
}
