import { CaseInfo } from '@/views/test/types';

export type BaselineDetail = {
  id: number;
  name: string;
  planId: number;
  planName: string;
  established: boolean;
  description: string;
  caseIds?: number[];
  tenantId: number;
  createdBy: number;
  createdByName: string;
  createdDate: string;
  lastModifiedBy: number;
  lastModifiedByName: string;
  lastModifiedDate: string;
}

export type BaselineCaseInfo = {
  baselineId: number;
} & CaseInfo;

export type BaselineEditState = {
  id?: number;
  name: string;
  planId: string;
  description?: string;
  caseIds?: number[],
}
