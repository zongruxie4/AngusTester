/**
 * Mock service object representing a mock service entity
 */
export interface MockServiceObj {
  /** Unique identifier for the mock service */
  id: string;

  /** Name of the mock service */
  name: string;

  /** Source information of the mock service */
  source: {
    /** Source type value */
    value: string;

    /** Display message for the source */
    message: string;
  };

  /** Status information of the mock service */
  status: {
    /** Status type value */
    value: string;

    /** Display message for the status */
    message: string;
  };

  /** Current authorization permissions */
  currentAuths: {
    /** Permission value */
    value: string;

    /** Display message for the permission */
    message: string;
  }[];

  /** Node identifier where the service is deployed */
  nodeId: string;

  /** Name of the node where the service is deployed */
  nodeName: string;

  /** Domain URL of the service */
  serviceDomainUrl: string;

  /** Host URL of the service */
  serviceHostUrl: string;

  /** Authentication flag */
  auth: boolean;

  /** Has authorization flag */
  hasAuth: boolean;

  /** Association with project flag */
  assocProjectFlag: boolean;

  /** Tenant identifier */
  tenantId: string;

  /** Creator user identifier */
  createdBy: string;

  /** Creator user name */
  createdByName: string;

  /** Creation date */
  createdDate: string;

  /** Failure tips information */
  failTips?: {
    /** Exit code */
    exitCode: string | null;

    /** Error message */
    message: string;

    /** Console output */
    console?: string[];

    /** Service identifier */
    serviceId?: string;

    /** Success flag */
    success?: boolean;
  };

  /** Current authorization values */
  currentAuthsValue: string[];

  /** Last modifier user name */
  lastModifiedByName?: string;

  /** Last modification date */
  lastModifiedDate?: string;
}

/**
 * Table selection configuration for row selection in tables
 */
export interface TableSelection {
  /** Selected row keys */
  selectedRowKeys?: string[];

  /** Change event handler for selection */
  onChange?: (selectedRowKeys: string[]) => void;

  /** Function to get checkbox properties for each row */
  getCheckboxProps?: (record: MockServiceObj) => object;
}

/**
 * Filter operation types for search functionality
 */
export type FilterOp =
  | 'EQUAL'
  | 'NOT_EQUAL'
  | 'GREATER_THAN'
  | 'GREATER_THAN_EQUAL'
  | 'LESS_THAN'
  | 'LESS_THAN_EQUAL'
  | 'CONTAIN'
  | 'NOT_CONTAIN'
  | 'MATCH_END'
  | 'MATCH'
  | 'IN'
  | 'NOT_IN';

/**
 * Filter configuration for search parameters
 */
export interface Filter {
  /** Filter key */
  key: string;

  /** Filter value */
  value: string | boolean | string[];

  /** Filter operation */
  op: FilterOp;
}

/**
 * Search parameters for API requests
 */
export interface SearchParam {
  /** Page number */
  pageNo: number;

  /** Page size */
  pageSize: number;

  /** Filter configurations */
  filters?: Filter[];

  /** Order by field */
  orderBy?: string;

  /** Order sort direction */
  orderSort?: 'ASC' | 'DESC';

  /** Additional properties */
  [key: string]: any;
}
