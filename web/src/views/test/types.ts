import { EnumMessage, EvalWorkloadMethod, Priority, ReviewStatus } from '@xcan-angus/infra';
import { CaseStepView, CaseTestResult } from '@/enums/enums';
import { AttachmentInfo, ProgressInfo, TagInfo } from '@/types/types';
import { TaskInfo } from '@/views/issue/types';

export type PlanProps = {
  projectId: string;
  userInfo: { id: string; };
  notify: string;
  planId?: string;
}

export type CaseInfo = {
  id: number;
  name: string;
  code: string;
  description: string;
  projectId: number;
  planId: number;
  planName: string;
  moduleId: number;
  moduleName: string;
  softwareVersion: string;
  priority: EnumMessage<Priority>;
  deadlineDate: string;
  overdue: boolean;
  evalWorkloadMethod: EnumMessage<EvalWorkloadMethod>;
  evalWorkload: number;
  actualWorkload: number;
  review: boolean;
  reviewerId: number;
  reviewerName: string;
  reviewDate: string;
  reviewStatus: EnumMessage<ReviewStatus>;
  reviewRemark: string;
  reviewNum: number;
  reviewFailNum: number;
  testerId: number;
  testerName: string;
  developerId: number;
  developerName: string;
  testNum: number;
  testFailNum: number;
  testResult: EnumMessage<CaseTestResult>;
  testRemark: string;
  testResultHandleDate: string;
  favourite: boolean;
  follow: boolean;
  tenantId?: number;
  createdBy: number;
  createdByName: string;
  avatar: string;
  createdDate: string;
  lastModifiedBy: number;
  lastModifiedDate: string;
  tags: TagInfo[];
}

export type CaseTestStep = {
  step: string;
  expectedResult: string;
}

export type CaseDetail = {
  id: number;
  name: string;
  code: string;
  description: string;
  projectId: number;
  planId: number;
  planName: string;
  moduleId: number;
  moduleName: string;
  softwareVersion: string;
  priority: EnumMessage<Priority>;
  precondition: string;
  stepView: EnumMessage<CaseStepView>;
  steps: CaseTestStep[];
  deadlineDate: string;
  overdue: boolean;
  evalWorkloadMethod: EnumMessage<EvalWorkloadMethod>;
  evalWorkload: number;
  actualWorkload: number;
  review: boolean;
  reviewerId: number;
  reviewerName: string;
  reviewDate: string;
  reviewStatus: EnumMessage<ReviewStatus>;
  reviewRemark: string;
  reviewNum: string;
  reviewFailNum: string;
  testerId: number;
  testerName: string;
  developerId: number;
  developerName: string;
  testNum: number;
  testFailNum: number;
  testResult: EnumMessage<CaseTestResult>;
  testRemark: string;
  testResultHandleDate: string;
  attachments: AttachmentInfo[];
  tags: TagInfo[];
  refTaskInfos: TaskInfo[];
  refCaseInfos: CaseInfo[];
  allVersionCaseVos: Map<string, CaseDetail>;
  progress: ProgressInfo;
  commentNum: number;
  activityNum: number;
  favourite: boolean;
  follow: boolean;
  tenantId: number;
  createdBy: number;
  createdByName: string;
  avatar: string;
  createdDate: string;
  lastModifiedBy: number;
  lastModifiedByName: string;
  lastModifiedDate: string;
  /** Optional reference map used when updating associations */
  refMap?: { TASK: number[]; CASE: number[] };
  checked?: boolean;
}
