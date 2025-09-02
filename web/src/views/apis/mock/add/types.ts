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
 * Interface for form validation rules
 * Defines the structure for form field validation rules
 */
export interface FormValidationRule {
  /** Rule type */
  type?: string;
  /** Whether the field is required */
  required?: boolean;
  /** Validation message to display on error */
  message?: string;
  /** Validation trigger event */
  trigger?: string;
  /** Custom validator function (optional) */
  validator?: (rule: any, value: any) => Promise<void>;
  /** Regular expression for pattern validation */
  pattern?: RegExp;
  /** Minimum value for number validation */
  min?: number;
  /** Maximum value for number validation */
  max?: number;
  /** Minimum length for string validation */
  len?: number;
  /** Minimum length for string validation */
  minLen?: number;
  /** Maximum length for string validation */
  maxLen?: number;
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
 * Interface for file information display
 * Represents metadata for uploaded files
 */
export interface UploadedFileMetadata {
  /** File name */
  name: string;
  /** File size in bytes */
  size: string;
}

/**
 * Enum for import source types
 * Defines the supported import sources for mock service creation
 */
export enum ImportSourceType {
  /** Angus OpenAPI format */
  ANGUS = 'ANGUS',
  /** Postman collection format */
  POSTMAN = 'POSTMAN',
  /** Swagger/OpenAPI format */
  SWAGGER = 'SWAGGER'
}

/**
 * Enum for HTTP methods
 * Defines the supported HTTP methods for APIs
 */
export enum HttpMethod {
  /** DELETE method */
  DELETE = 'DELETE',
  /** GET method */
  GET = 'GET',
  /** HEAD method */
  HEAD = 'HEAD',
  /** OPTIONS method */
  OPTIONS = 'OPTIONS',
  /** PATCH method */
  PATCH = 'PATCH',
  /** POST method */
  POST = 'POST',
  /** PUT method */
  PUT = 'PUT',
  /** TRACE method */
  TRACE = 'TRACE'
}

/**
 * Interface for service options in the project service selection
 * Represents available services for association
 */
export interface ServiceOption {
  /** Unique service identifier */
  id: string;
  /** Service name */
  name: string;
  /** Service code */
  code: string;
  /** Parent service ID */
  pid: string;
  /** Authentication requirement flag */
  auth: boolean;
  /** Target type classification */
  targetType: 'API' | 'PROJECT' | 'SERVICE';
  /** Child services */
  children: ServiceOption[];
  /** Mock service ID (if associated) */
  mockServiceId?: string;
  /** Whether the option is disabled */
  disabled?: boolean;
}

/**
 * Interface for API items in the API selection list
 * Represents individual APIs that can be imported
 */
export interface ApiItem {
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
