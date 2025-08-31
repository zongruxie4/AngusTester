import { NodeRole } from '@xcan-angus/infra';

/**
 * Node specification interface
 */
export interface NodeSpec {
  text?: {
    showLabel: string;
  };
}

/**
 * Node basic information interface
 */
export interface NodeInfo {
  id: string;
  name: string;
  username: string;
  sourceName: string;
  domain?: string;
  enabled: boolean;
  ip: string;
  password?: string;
  roles: NodeRole[];
  installAgent: boolean;
  publicIp?: string;
  spec?: NodeSpec;
  sshPort?: number;
  port?: number;
  agentPort?: number;
  online: boolean;
  createdByName?: string;
  createdDate?: string;
  instanceExpiredDate?: string;
  tenantId?: string;
  createdBy?: string;
  geAgentInstallationCmd?: boolean;
  installNodeAgent?: boolean;
  free?: boolean;
  rolesName?: string;
  rolesValues?: string[];
}

/**
 * Resource usage metrics interface
 */
export interface ResourceUsage {
  cpu: number;
  cpuPercent: number;
  cpuTotal: number;
  memory: string;
  memoryPercent: number;
  memoryTotal: string;
  swap: string;
  swapPercent: number;
  swapTotal: string;
  disk: string;
  diskPercent: number;
  diskTotal: string;
  txBytesRate: number;
  rxBytesRate: number;
  rxBytes: string;
  txBytes: string;
}

/**
 * Network device data interface
 */
export interface NetworkDeviceData {
  deviceName: string;
  networkUsage: {
    cvsValue: string;
    timestamp: string;
  };
}

/**
 * Chart parameters interface
 */
export interface ChartParams {
  orderSort: string;
  filters?: Array<{
    key: string;
    op: string;
    value: string;
  }>;
}

/**
 * Chart server mapping interface
 */
export interface ChartServerMap {
  cpu: string;
  memory: string;
  disk: string;
  network: string;
}

/**
 * Installation configuration interface
 */
export interface InstallConfig {
  tenantId?: string;
  deviceId?: string;
  ctrlAccessToken?: string;
  [key: string]: any;
}

/**
 * Installation steps interface
 */
export interface InstallSteps {
  onlineInstallCmd?: string;
  installScriptUrl?: string;
  installScriptName?: string;
  runInstallCmd?: string;
  [key: string]: any;
}

/**
 * Tab key types
 */
export type ActiveTabKey = 'cpu' | 'memory' | 'disk' | 'network';
export type ActiveKey = 'source' | 'proxy';

/**
 * Chart data options interface
 */
export interface ChartDataOption {
  label: string;
  value: string | boolean;
}

/**
 * Component props interfaces
 */
export interface AgentInfoProps {
  ip: string;
  port: number;
}

export interface AgentChartProps {
  id: string;
}

export interface AgentLogProps {
  ip: string;
  port: number;
}

export interface ExecutionProps {
  id: string;
}

export interface ExecutionPropulsionProps {
  nodeId: string;
  tenantId?: string;
}

export interface MockServiceProps {
  nodeId: string;
}

/**
 * <p>Mock service related types and interfaces</p>
 * <p>Provides type definitions for mock service data structures and API responses</p>
 */
export interface MockService {
  /** Unique identifier for the mock service */
  id: string;
  /** Name of the mock service */
  name: string;
  /** Current status of the mock service */
  status: {
    /** Status message */
    message: string;
    /** Status code */
    code?: string;
  };
  /** Host URL for accessing the mock service */
  serviceHostUrl: string;
  /** Port number for the mock service */
  servicePort: number;
  /** Name of the user who created the service */
  createdByName: string;
  /** Date when the service was created */
  createdDate: string;
}

/**
 * <p>Mock service list API response structure</p>
 * <p>Defines the response format for mock service list API calls</p>
 */
export interface MockServiceListResponse {
  /** List of mock services */
  list: MockService[];
  /** Total count of available services */
  total: number;
}

/**
 * <p>Mock service list request parameters</p>
 * <p>Defines the parameters required for fetching mock service list</p>
 */
export interface MockServiceListParams {
  /** Node identifier */
  nodeId: string;
  /** Project identifier */
  projectId: string;
  /** Page number for pagination */
  pageNo: number;
  /** Number of items per page */
  pageSize: number;
}

/**
 * <p>Table pagination configuration</p>
 * <p>Defines pagination settings for the mock service table</p>
 */
export interface TablePagination {
  /** Current page number */
  current: number;
  /** Number of items per page */
  pageSize: number;
  /** Total number of items */
  total: number;
  /** Available page size options */
  pageSizeOptions: number[];
}

/**
 * <p>Table column definition</p>
 * <p>Defines the structure and configuration for table columns</p>
 */
export interface TableColumn {
  /** Data field identifier */
  dataIndex: string;
  /** Column header title */
  title: string;
  /** Column width (optional) */
  width?: number;
  /** Whether column is sortable (optional) */
  sorter?: boolean;
}
