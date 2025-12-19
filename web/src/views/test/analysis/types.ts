import { AuthObjectType, EnumMessage } from '@xcan-angus/infra';
import { AnalysisDataSource, AnalysisCaseObject, AnalysisCaseTemplate } from '@/enums/enums';

export interface AnalysisInfo {
  id?: string;
  name: string;
  containsDataDetail: boolean;
  object: EnumMessage<AnalysisCaseObject>;
  timeRange: string;
  resource: 'CASE';
  description: string;
  template: AnalysisCaseTemplate;
  planId: string;
  datasource: EnumMessage<AnalysisDataSource>;
  orgType?: EnumMessage<AuthObjectType>;
  orgId?: string;
  customRange?: [string, string];
  modifier?: string;
  modifiedDate?: string;
  data?: any;
}

export interface EditAnalysisState {
  id?: string;
  name: string;
  containsUserAnalysis: boolean;
  containsDataDetail: boolean;
  object: AnalysisCaseObject;
  timeRange: string;
  resource: 'CASE';
  description: string;
  template: AnalysisCaseTemplate;
  planId: string;
  datasource: AnalysisDataSource;
  orgType?: AuthObjectType;
  orgId?: string;
  customRange?: [string, string];
}
