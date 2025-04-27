
export interface Analysis {
  id?: string;
  containsDataDetail: boolean;
  object: 'CURRENT_PROJECT'| 'PLAN'| 'TESTER_ORG';
  timeRange: string;
  resource: 'TASK'|'CASE';
  description: string;
  template: string;
  planId: string;
  datasource: 'REAL_TIME_DATA'| 'SNAPSHOT_DATA';
  orgType?: 'DEPT'|'USER'|'GROUP';
  orgId?: string;
  customRange?: [string, string];
  name: string;
}
