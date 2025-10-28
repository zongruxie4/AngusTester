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
  createdByName: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
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
