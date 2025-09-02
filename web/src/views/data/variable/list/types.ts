/**
 * Types for variable list page components
 */

import { VariableItem } from '../types';

/**
 * Search filter configuration
 */
export interface SearchFilter {
  /** Filter key */
  key: string;
  /** Filter operation */
  op: string;
  /** Filter value */
  value: string | string[];
}

/**
 * Props for the main variable list component
 */
export interface VariableListProps {
  /** Project identifier */
  projectId: string;
  /** User information */
  userInfo: { id: string };
  /** Application information */
  appInfo: { id: string };
  /** Notification message */
  notify: string;
}

/**
 * Props for the import modal component
 */
export interface ImportModalProps {
  /** Whether the modal is visible */
  visible: boolean;
  /** Project identifier */
  projectId: string;
}

/**
 * Props for the search panel component
 */
export interface SearchPanelProps {
  /** Loading state */
  loading: boolean;
  /** Number of selected items */
  selectedNum?: number;
}

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
 * Search panel parameters
 */
export interface SearchPanelParams {
  /** Field to order by */
  orderBy?: string;
  /** Sort direction */
  orderSort?: 'ASC' | 'DESC';
  /** Search filters */
  filters: SearchFilter[];
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

/**
 * Sort configuration
 */
export interface SortConfig {
  /** Field to order by */
  orderBy: string;
  /** Sort direction */
  orderSort: 'ASC' | 'DESC';
}

/**
 * Search panel option configuration
 */
export interface SearchPanelOption {
  /** Value key */
  valueKey: string;
  /** Input type */
  type: string;
  /** Placeholder text */
  placeholder?: string;
  /** Whether to allow clear */
  allowClear?: boolean;
  /** Maximum length */
  maxlength?: number;
}

/**
 * File upload configuration
 */
export interface FileUploadConfig {
  /** Maximum file size in MB */
  maxSize: number;
  /** Accepted file types */
  acceptTypes: string[];
  /** Whether to allow multiple files */
  multiple: boolean;
}

/**
 * Import strategy options
 */
export type ImportStrategy = 'COVER' | 'IGNORE';

/**
 * Error message mapping
 */
export type ErrorMessageMap = Map<string, string>;

/**
 * Visibility state mapping
 */
export type VisibilityIdSet = Set<string>;
