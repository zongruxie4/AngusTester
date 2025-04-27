export interface StatusType {
  message: string,
  value: 'FAIL' | 'NOT_CONNECTED' | 'SUCCESS'
}
export interface SourceType {
  id: string;
  name: string;
  database: string;
  username: string;
  passd: string;
  jdbcUrl: string;
  connSuccessFlag: string;
  lastConnDate: string;
  authFlag: string;
  tenantId: string;
  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;
}

export interface StatusFilter {
  text: string;
  state: string;
}

export interface DataStatus {
  fail: StatusFilter;
  success: StatusFilter;
  default: StatusFilter;
}

export interface PaginationType {
  current: number;
  pageSize: number;
  total: number;
  hideOnSinglePage?: boolean;
}
