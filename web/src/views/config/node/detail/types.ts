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
