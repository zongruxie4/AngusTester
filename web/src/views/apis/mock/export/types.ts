/**
 * Interface for mock service object
 * Represents the structure of a mock service
 */
export interface MockService {
  id: string;
  name: string;
  source: {
    value: string;
    message: string;
  };
  status: {
    value: string;
    message: string;
  };
  currentAuths: {
    value: string;
    message: string;
  }[];
  nodeId: string;
  nodeName: string;
  serviceDomainUrl: string;
  serviceHostUrl: string;
  auth: boolean;
  hasAuth: boolean;
  assocProjectFlag: boolean;
  tenantId: string;
  createdBy: string;
  createdByName: string;
  createdDate: string;
  failTips?: string[];
  currentAuthsValue: string[];
}

/**
 * Interface for export format options
 * Represents the available export format options
 */
export interface ExportFormatOption {
  /** Display label for the format */
  label: string;
  /** Value of the format */
  value: 'json' | 'yaml';
}

/**
 * Interface for API selection data
 * Represents the data structure when APIs are selected
 */
export interface ApiSelectionData {
  /** Project ID */
  projectId: string;
  /** Selected API IDs */
  apiIds: string[];
  /** API options */
  _apiOptions: any;
  /** Whether all APIs are checked */
  checkedAll: boolean;
}

/**
 * Interface for tree properties
 * Represents the configuration for the service tree selection
 */
export interface TreeProps {
  /** API endpoint for fetching services */
  action: string;
  /** Whether the selection is disabled */
  disabled: boolean;
  /** Additional parameters for the API request */
  params: {
    /** Admin flag */
    admin: boolean;
    /** Project ID */
    projectId: string;
  };
  /** Default selected value */
  defaultValue?: {
    /** Service name */
    name: string;
    /** Service ID */
    id: string;
  };
}

/**
 * Interface for scroll properties
 * Represents the configuration for the API scroll list
 */
export interface ScrollProps {
  /** API endpoint for fetching APIs */
  action: string;
  /** Additional parameters for the API request */
  params: {
    filters: {
      key: string;
      op: string;
      value: string[];
    }[];
  } | undefined;
}
