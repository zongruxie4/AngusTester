import { EnumMessage, EvalWorkloadMethod } from '@xcan-angus/infra';
import { FuncPlanStatus } from '@/enums/enums';
import { AttachmentInfo, ProgressInfo, UserInfo } from '@/types/types';

export type PlanDetail = {
  id: string;
  projectId: string;
  name: string;
  status: EnumMessage<FuncPlanStatus>;
  startDate: string;
  deadlineDate: string;
  ownerId: string;
  ownerName: string;
  ownerAvatar: string;
  testerResponsibilities: Map<string, string>;
  members: UserInfo[];
  testingScope: string;
  testingObjectives: string;
  acceptanceCriteria: string;
  otherInformation: string;
  attachments: AttachmentInfo[];
  casePrefix: string;
  review: boolean;
  evalWorkloadMethod: EnumMessage<EvalWorkloadMethod>;
  auth: boolean;
  tenantId: string;
  createdBy: string;
  createdByName: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
  caseNum: number;
  validCaseNum: number;
  progress: ProgressInfo;
}

export type PlanEditFormState = {
  id?: string;
  projectId: string;
  name: string;
  startDate: string;
  deadlineDate: string;
  ownerId: string;
  testerResponsibilities: Map<string, string>;
  testingObjectives?: string,
  testingScope: string,
  otherInformation: string,
  acceptanceCriteria: string;
  attachments: AttachmentInfo[];
  review: boolean;
  casePrefix?: string;
  evalWorkloadMethod: EvalWorkloadMethod;
  // The front-end echoes the automatically added fields of startDate and deadlineDate, which need to be deleted when saving
  date?: [string, string];
}

export type MemberProgressData = {
  testerId: string;
  testerName: string;
  testerAvatar: string;
  totalCaseNum: string;
  evalWorkload: string;
  actualWorkload: string;
  passedTestNum: string;
  passedTestRate: string;
  overdueNum: string;
  overdueRate: string;
}
