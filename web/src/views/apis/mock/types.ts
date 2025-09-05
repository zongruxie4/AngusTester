import { EnumMessage, SearchCriteria } from '@xcan-angus/infra';
import { MockServiceSource, MockServicePermission } from '@/enums/enums';

/**
 * Mock service object representing a mock service entity
 */
export interface MockService {
  /** Unique identifier for the mock service */
  id: string;

  /** Name of the mock service */
  name: string;

  /** Source information of the mock service */
  source: EnumMessage<MockServiceSource>;

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
    value: MockServicePermission;

    /** Display message for the permission */
    message: string;
  }[];

  /** Node identifier where the service is deployed */
  nodeId: string;

  /** Name of the node where the service is deployed */
  nodeName: string;

  servicePort: string;

  nodeIp: string;

  nodePublicIp?: string;

  /** Domain URL of the service */
  serviceDomainUrl: string;

  /** Host URL of the service */
  serviceHostUrl: string;

  /** Authentication flag */
  auth: boolean;

  /** Has authorization flag */
  hasAuth: boolean;

  /** Association with project flag */
  assocProject: boolean;

  /** Tenant identifier */
  tenantId: string;

  /** Creator user identifier */
  createdBy: string;

  /** Creator user name */
  createdByName: string;

  /** Creation date */
  createdDate: string;

  /** Last modifier user name */
  lastModifiedByName?: string;

  /** Last modification date */
  lastModifiedDate?: string;

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
  currentAuthsValue: MockServicePermission[];
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
    filters: SearchCriteria[];
  } | undefined;
}
