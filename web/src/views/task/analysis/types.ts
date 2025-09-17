import { AuthObjectType, EnumMessage } from '@xcan-angus/infra';
import { AnalysisDataSource, AnalysisTaskObject, AnalysisTaskTemplate } from '@/enums/enums';

export interface AnalysisInfo {
  id?: string;
  name: string;
  containsDataDetail: boolean;
  object: EnumMessage<AnalysisTaskObject>;
  timeRange: string;
  resource: 'TASK';
  description: string;
  template: AnalysisTaskTemplate;
  planId: string;
  datasource: EnumMessage<AnalysisDataSource>;
  orgType?: EnumMessage<AuthObjectType>;
  orgId?: string;
  customRange?: [string, string];
  lastModifiedByName?: string;
  lastModifiedDate?: string;
  data?: any;
}

export interface EditAnalysisState {
  id?: string;
  name: string;
  containsUserAnalysis: boolean;
  containsDataDetail: boolean;
  object: AnalysisTaskObject;
  timeRange: string;
  resource: 'TASK';
  description: string;
  template: AnalysisTaskTemplate;
  planId: string;
  datasource: AnalysisDataSource;
  orgType?: AuthObjectType;
  orgId?: string;
  customRange?: [string, string];
}
