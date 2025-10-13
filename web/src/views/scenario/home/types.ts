import { PageQuery } from '@xcan-angus/infra';

// User information interface
export interface UserInfo {
  id: string;
}

// Scenario query parameters
export interface ScenarioQueryParams {
  projectId?: string;
  createdBy?: string;
  favouriteBy?: string;
  followBy?: string;
}

// Scenario item data structure
export interface ScenarioItem {
  id: string;
  name: string;
  plugin: string;
  scriptType: {
    message: string;
  };
  createdDate: string;
}

// Table pagination configuration
export interface TablePagination {
  total: number;
  current: number;
  pageSize: number;
  showSizeChanger: boolean;
  size: 'small';
  showTotal: (value: number) => string;
}

// Table column configuration
export interface TableColumn {
  key: string;
  title: string;
  dataIndex: string;
  ellipsis?: boolean;
  sorter?: boolean;
  width?: string | number;
  actionKey?: 'createdBy' | 'favouriteBy' | 'followBy';
}

// Table change event parameters
export interface TableChangeParams {
  current?: number;
  pageSize?: number;
  filters?: any;
  sorter?: {
    orderBy: string;
    orderSort: PageQuery.OrderSort;
  };
}

// API request parameters for scenario list
export interface ScenarioListParams {
  projectId: string;
  pageNo: number;
  pageSize: number;
  createdBy?: string;
  favouriteBy?: string;
  followBy?: string;
  orderBy?: string;
  orderSort?: string;
}
