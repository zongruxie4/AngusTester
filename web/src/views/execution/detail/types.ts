import { Exception, ExecutionInfo } from '@/views/execution/types';

export interface ServerVariable {
  enum: string[];
  default: string;
}

export interface ServerObject {
  url: string;
  description: string;
  variables: Record<string, ServerVariable>;
}

export interface UseServerData {
  serverList: ServerObject[];
  loadServers: () => Promise<void>;
  hasVariable: (variables?: Record<string, ServerVariable>) => boolean;
}

export interface UseConfigData {
  execSettingFormRef: any; // Reference to the ExecSettingForm component
  saveSetting: () => Promise<void>;
  scrollToErrorElement: (formName: string, errors: any) => void;
}

export interface UseLogData {
  nodeId: string | undefined;
  nodeIp: string | undefined;
  nodePort: string;
  execLogContent: string | undefined;
  execLogPath: string | undefined;
  execLogErr: boolean;
  errorText: string | undefined;
  showSchedulingLog: string;
  showExecLog: string;
  schedulingLogItem: any; // Scheduling log item type - could be improved with specific interface
  loadExecLog: () => Promise<void>;
  nodeSelectChange: (nodeId: string, options: any) => void;
  handleDoubleClick: (type: 'scheduling' | 'exec') => void;
  openSchedulingLog: () => void;
  openExecLog: () => void;
  downloadLog: (type: 'scheduling' | 'exec') => void;
  refreshExecLog: (event: Event) => void;
}

export interface UseExecutionDetail {
  loading: boolean;
  detail: ExecutionInfo | undefined;
  scriptInfo: any; // Script information object - could be improved with specific interface
  scriptYamlStr: string;
  topActiveKey: string;
  exception: Exception | undefined;
  performanceRef: any; // Reference to Performance component
  funcRef: any; // Reference to FuncTest component
  loadscriptContent: () => Promise<void>;
  getDetail: () => Promise<void>;
  getInfo: (data: any) => void;
  handleRestart: (item: ExecutionInfo) => Promise<void>;
  handleStop: (item: ExecutionInfo) => Promise<void>;
  handleDelete: (item: ExecutionInfo) => Promise<void>;
  topTabsChange: (value: string) => void;
  scriptTypeChange: (value: 'json' | 'yaml') => void;
  setException: () => void;
}
