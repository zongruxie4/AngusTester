import { CaseInfo } from '@/views/test/types';

export type BaselineDetail = {
  id: string;
  name: string;
  planId: string;
  planName: string;
  established: boolean;
  description: string;
  caseIds?: string[];
  tenantId: string;
  createdBy: string;
  creator: string;
  createdDate: string;
  modifiedBy: string;
  modifier: string;
  modifiedDate: string;
}

export type BaselineCaseInfo = {
  baselineId: string;
} & CaseInfo;

export type BaselineEditState = {
  id?: string;
  name: string;
  planId: string;
  description?: string;
  caseIds?: string[],
}
