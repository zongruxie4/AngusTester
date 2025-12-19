import { EnumMessage, HttpMethod, SearchCriteria } from '@xcan-angus/infra';
import { MockServicePermission, MockServiceSource } from '@/enums/enums';

export const ANGUS_MOCK_DOMAIN = '.angusmock.cloud';
export const ANGUS_MOCK_DOMAIN_REGEX = new RegExp(`^(?=.{1,253}$)([a-z0-9]|[a-z0-9][a-z0-9\\-]{0,61}[a-z0-9])\\${ANGUS_MOCK_DOMAIN}$`);

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
  creator: string;

  /** Creation date */
  createdDate: string;

  /** Last modifier user name */
  modifier?: string;

  /** Last modification date */
  modifiedDate?: string;

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

export interface MockServiceEditForm {
  name: string;
  serviceDomainUrl: string;
  servicePort: string;
  nodeId: string | undefined;
  projectId?: string;
  serviceId?: string;
  apiIds?: string[];
}

/**
 * Interface for mock service form data
 * Represents the structure of data collected in the mock service creation form
 */
export interface MockServiceForm {
  /** Service name - unique identifier for the mock service */
  name: string;
  /** Domain for the mock service */
  serviceDomain: string;
  /** Port number for the mock service */
  servicePort: string;
  /** Node ID where the service will be deployed */
  nodeId: string | undefined;
  projectId?: string;
  /** Associated service ID (when linking to existing service) */
  serviceId: string;
  /** List of API IDs to import (when linking to existing service) */
  apiIds: string[];
  /** Import type for file-based creation */
  importType: 'OpenAPI' | 'POSTMAN';
  /** Uploaded file for import */
  file: File | undefined;
  /** Text content for direct input */
  text: string;
}

/**
 * Interface for node options in the service deployment selection
 * Represents available nodes for mock service deployment
 */
export interface NodeOption {
  /** Unique node identifier */
  id: string;
  /** Human-readable node name */
  name: string;
  /** Node IP address */
  ip: string;
}

/**
 * Interface for API items in the API selection list
 * Represents individual APIs that can be imported
 */
export interface ApiInfo {
  /** Unique API identifier */
  id: string;
  /** API name/summary */
  name: string;
  /** HTTP method */
  method: HttpMethod;
  /** API endpoint path */
  endpoint: string;
  /** API summary/description */
  summary: string;
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
