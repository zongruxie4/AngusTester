import { EnumMessage } from '@xcan-angus/infra';
import { ExecStatus } from '@/enums/enums';

export const allCvsKeys = ['duration', 'errors', 'iterations', 'n', 'operations', 'transactions', 'readBytes', 'writeBytes', 'ops', 'tps', 'brps', 'bwps', 'tranMean', 'tranMin', 'tranMax', 'tranP50', 'tranP75', 'tranP90', 'tranP95', 'tranP99', 'tranP999', 'errorRate', 'threadPoolSize', 'threadPoolActiveSize', 'threadMaxPoolSize'];

export interface ServerVariable {
  enum: string[];
  default: string;
}

export interface ServerObject {
  url: string;
  description: string;
  variables: Record<string, ServerVariable>;
}

export interface UseConfigData {
  execSettingFormRef: any; // Reference to the ExecSettingForm component
  saveSetting: () => Promise<void>;
  scrollToErrorElement: (formName: string, errors: any) => void;
}

export interface FunctionalTestProps {
  loading?:boolean;
  plugin?: string;
  execInfo:{
    id:string;
    plugin?: string;
    reportInterval:string;
    status: EnumMessage<ExecStatus>
  };
  execContent:{[key:string]:any}[];
  exception?: { codeName: string; messageName: string; code: string; message: string;};
}

export interface MockDataProps {
  dataSource: Record<string, any>
}
