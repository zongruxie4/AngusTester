import { Ref } from 'vue';

/**
 * Node role configuration with value, label, name and disabled state
 */
export interface NodeRole {
  value: string;
  label: string;
  name: string;
  disabled?: boolean;
}

/**
 * Component props interface for NodeItems component
 */
export interface NodeItemsProps {
  /** List of nodes to display and manage */
  nodeList: Array<Record<string, any>>;
  /** Available role options for node assignment */
  roleOptions: NodeRole[];
  /** Whether to automatically refresh node metrics */
  autoRefresh: boolean;
  /** Whether current user has admin privileges */
  isAdmin: boolean;
}

/**
 * Node form parameters for adding/editing nodes
 */
export interface NodeFormParams {
  /** Node name */
  name: string;
  /** Node IP address */
  ip: string;
  /** Public IP address */
  publicIp: string;
  /** Domain name */
  domain: string;
  /** SSH username */
  username: string;
  /** SSH password */
  password: string;
  /** SSH port number */
  sshPort: string;
  /** Assigned node roles */
  roles: string[];
  /** Node ID for editing existing nodes */
  id: string;
}

/**
 * Node monitoring data structure
 */
export interface NodeMonitorData {
  /** CPU usage percentage */
  cpu: string;
  /** Memory usage percentage */
  memory: string;
  /** Disk usage percentage */
  disk: string;
  /** Swap usage percentage */
  swap: string;
  /** Network download rate in MB/s */
  rxBytesRate: number;
  /** Network upload rate in MB/s */
  txBytesRate: number;
}

/**
 * Node data structure with all properties
 */
export interface NodeData extends Record<string, any> {
  /** Node ID */
  id?: string;
  /** Node name */
  name: string;
  /** Whether node is in edit mode */
  editable?: boolean;
  /** Node monitoring data */
  monitorData?: NodeMonitorData;
  /** Whether agent is installed */
  installAgent?: boolean;
  /** Whether node is enabled */
  enabled?: boolean;
  /** Node roles */
  roles: NodeRole[];
  /** Operating system information */
  os?: string[];
  /** Tenant ID */
  tenantId?: string;
  /** Created by user ID */
  createdBy?: string;
  /** Whether node is free tier */
  free?: boolean;
  /** Node source type */
  source?: { value: string };
  /** Whether to show install agent panel */
  showInstallAgent?: boolean;
  /** Linux offline install steps */
  linuxOfflineInstallSteps?: any;
  /** Windows offline install steps */
  windowsOfflineInstallSteps?: any;
  /** Installation configuration */
  installConfig?: any;
}

/**
 * Component emits interface
 */
export interface NodeItemsEmits {
  /** Cancel operation event */
  (e: 'cancel'): void;
  /** Reload node list event */
  (e: 'loadList'): void;
}

/**
 * Loading state maps for various operations
 */
export interface LoadingStateMaps {
  /** Map of nodes currently installing agent */
  installingMap: Record<string, boolean>;
  /** Map of nodes currently enabling/disabling */
  enablingMap: Record<string, boolean>;
  /** Map of nodes currently restarting agent */
  restartingMap: Record<string, boolean>;
}

/**
 * Node name editing state
 */
export interface NodeNameEditState {
  /** Reference to name input element */
  editNameInputRef: Ref<any>;
  /** Current editing name value */
  editNameValue: Ref<string>;
  /** ID of node being edited */
  editNameId: Ref<string | undefined>;
}

/**
 * Connection test state
 */
export interface ConnectionTestState {
  /** Whether connection test has been performed */
  showTested: Ref<boolean>;
  /** Whether connection test was successful */
  testSuccess: Ref<boolean>;
  /** Connection test failure message */
  testFailContent: Ref<string>;
}

/**
 * Form validation state
 */
export interface FormValidationState {
  /** Whether form validation has been triggered */
  validated: Ref<boolean>;
  /** Whether port number is valid */
  showPortTip: Ref<boolean>;
  /** Whether test button should be disabled */
  testBtnDisable: Ref<boolean>;
}
