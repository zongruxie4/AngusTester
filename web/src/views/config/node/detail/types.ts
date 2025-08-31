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
export type ActiveKey = 'source' | 'agent';

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
  port?: string;
}

export interface AgentChartProps {
  id: string;
}

export interface AgentLogProps {
  ip: string;
  port?: string;
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

/**
 * <p>Process information interface for execution propulsion</p>
 * <p>Defines the structure of process data retrieved from the node</p>
 */
export interface ProcessInfo {
  /** Process identifier */
  processID: string;
  /** Process uptime */
  upTime: string;
  /** User running the process */
  user: string;
  /** Virtual memory size */
  virtualSize: string;
  /** Number of threads */
  threadCount: number;
  /** Number of open files */
  openFiles: number;
  /** Bytes written to disk */
  bytesWritten: string;
  /** Bytes read from disk */
  bytesRead: string;
  /** Command line arguments */
  commandLine: string;
}

/**
 * <p>Node process summary interface</p>
 * <p>Contains aggregated process statistics for the node</p>
 */
export interface NodeProcessSummary {
  /** Total number of processes */
  processCount: number;
  /** Total number of threads */
  threadCount: number;
  /** Total number of open files */
  openFiles: number;
  /** Total bytes written to disk */
  bytesWritten: string;
  /** Total bytes read from disk */
  bytesRead: string;
  /** List of individual processes */
  processes: ProcessInfo[];
}

/**
 * <p>Execution propulsion component props interface</p>
 * <p>Defines the properties that can be passed to ExecutionPropulsion component</p>
 */
export interface ExecutionPropulsionProps {
  /** Unique identifier for the node */
  nodeId: string;
  /** Tenant identifier for access control */
  tenantId?: string;
}

/**
 * <p>Table column configuration for execution propulsion</p>
 * <p>Defines the structure and behavior of table columns</p>
 */
export interface ExecutionPropulsionColumn {
  /** Column title */
  title: string;
  /** Data field identifier */
  dataIndex: string;
  /** Unique key for the column */
  key: string;
  /** Custom cell configuration */
  customCell: (record: any, index: number) => { rowSpan?: number; colSpan?: number };
  /** Custom render function for cell content */
  customRender?: ({ record, index }: { record: any; index: number }) => any;
}

/**
 * <p>Execution record interface</p>
 * <p>Defines the structure of execution data</p>
 */
export interface ExecutionRecord {
  /** Execution identifier */
  id: string;
  /** Execution name */
  name: string;
  /** Type of test script */
  scriptType: {
    /** Script type message */
    message: string;
    /** Script type code */
    code?: string;
  };
  /** Plugin information */
  plugin: string;
  /** Name of the executor */
  execByName: string;
  /** Actual start date of execution */
  actualStartDate: string;
}

/**
 * <p>Execution component props interface</p>
 * <p>Defines the properties that can be passed to Execution component</p>
 */
export interface ExecutionProps {
  /** Unique identifier for the node */
  id: string;
}

/**
 * <p>Table column configuration for execution</p>
 * <p>Defines the structure of execution table columns</p>
 */
export interface ExecutionColumn {
  /** Data field identifier */
  dataIndex: string;
  /** Column header title */
  title: string;
  /** Unique key for the column */
  key: string;
  /** Column width (optional) */
  width?: number;
}

/**
 * <p>Agent log component props interface</p>
 * <p>Defines the properties that can be passed to AgentLog component</p>
 */
export interface AgentLogProps {
  /** IP address of the agent */
  ip: string;
  /** Port number for the agent (optional) */
  port?: string;
}

/**
 * <p>Log file option interface</p>
 * <p>Defines the structure of log file selection options</p>
 */
export interface LogFileOption {
  /** Display label for the option */
  label: string;
  /** Value of the option */
  value: string;
}

/**
 * <p>Log text parameters interface</p>
 * <p>Defines the parameters for log content retrieval</p>
 */
export interface LogTextParams {
  /** Name of the log file */
  logName: string;
  /** Whether to tail the log file */
  tail: boolean;
  /** Number of lines to retrieve */
  linesNum: string;
}

/**
 * <p>Agent chart component props interface</p>
 * <p>Defines the properties that can be passed to AgentChart component</p>
 */
export interface AgentChartProps {
  /** Unique identifier for the agent */
  id: string;
}

/**
 * <p>Cache chart data interface</p>
 * <p>Defines the structure of cache chart data</p>
 */
export interface CacheChartData {
  /** X-axis time data */
  xData: string[];
  /** Number of cache entries */
  numData: number[];
  /** Total cache size data */
  totalData: number[];
  /** Used cache size data */
  usedData: number[];
  /** Maximum total cache size */
  maxTotalData: number;
  /** Maximum number of cache entries */
  maxNumData: number;
}

/**
 * <p>RAM chart data interface</p>
 * <p>Defines the structure of RAM chart data</p>
 */
export interface RamChartData {
  /** X-axis time data */
  xData: string[];
  /** Submitted memory data */
  submitData: number[];
  /** Total memory data */
  totalData: number[];
  /** Used memory data */
  usedData: number[];
}

/**
 * <p>CPU chart data interface</p>
 * <p>Defines the structure of CPU chart data</p>
 */
export interface CpuChartData {
  /** X-axis time data */
  xData: string[];
  /** Total CPU data */
  totalData: number[];
  /** System CPU usage data */
  systemCpuUsed: number[];
  /** Process CPU usage data */
  processCpuUsed: number[];
}

/**
 * <p>Chart data parameters interface</p>
 * <p>Defines the parameters for chart data retrieval</p>
 */
export interface ChartDataParams {
  /** Filters for data querying */
  filters: Array<{
    /** Filter key */
    key: string;
    /** Filter operation */
    op: string;
    /** Filter value */
    value: string;
  }>;
}

/**
 * <p>Agent info component props interface</p>
 * <p>Defines the properties that can be passed to AgentInfo component</p>
 */
export interface AgentInfoProps {
  /** IP address of the agent */
  ip: string;
  /** Port number for the agent (optional) */
  port?: string;
}

/**
 * <p>Agent info data interface</p>
 * <p>Defines the structure of agent information data</p>
 */
export interface AgentInfoData {
  /** Agent version */
  version?: string;
  /** Agent home directory */
  home?: string;
  /** Agent uptime */
  uptime?: string;
  /** Agent IP address */
  ip?: string;
  /** Disk space information */
  diskSpace?: {
    /** Used disk space */
    used: number;
    /** Total disk space */
    total: number;
  };
  /** Server port */
  port?: string;
  /** Health status */
  health?: string;
  /** Server information */
  server?: {
    /** Server IP */
    ip: string;
    /** Server port */
    port: string;
  };
  /** Error tip information */
  errorTip?: string;
}

/**
 * <p>Grid column definition interface</p>
 * <p>Defines the structure of grid columns</p>
 */
export interface GridColumn {
  /** Data field identifier */
  dataIndex: string;
  /** Column label */
  label: string;
}
