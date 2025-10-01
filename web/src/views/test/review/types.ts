import { EnumMessage, ReviewStatus } from '@xcan-angus/infra';
import { FuncPlanStatus } from '@/enums/enums';
import { AttachmentInfo, ProgressInfo, UserInfo } from '@/types/types';
import { CaseDetail, CaseInfo } from '@/views/test/types';

export type ReviewDetail = {
  id: number;
  name: string;
  projectId: number;
  planId: number;
  planName: string;
  status: EnumMessage<FuncPlanStatus>;
  ownerId: number;
  ownerName: string;
  ownerAvatar: string;
  participants: UserInfo[];
  attachments: AttachmentInfo[];
  description: string;
  tenantId: number;
  createdBy: number;
  createdByName: string;
  createdDate: string;
  lastModifiedBy: number;
  lastModifiedByName: string;
  lastModifiedDate: string;
  caseNum: number;
  progress: ProgressInfo;
}

export type ReviewEditState = {
  id?: number;
  planId?: number;
  name: string;
  ownerId: number;
  participantIds: number[];
  attachments?: Attachment[];
  description?: string;
}

export type ReviewCaseInfo = {
  id: number;
  name: string;
  projectId: number;
  planId: number;
  planName: string;
  reviewId: number;
  caseId: number;
  caseInfo: CaseInfo;
  reviewerId: number;
  reviewerName: string;
  reviewDate: string;
  reviewStatus: EnumMessage<ReviewStatus>;
  reviewRemark: string;
  createdBy: number;
  createdByName: string;
  createdDate: string;
}

export type ReviewCaseDetail = {
  id: number;
  planId: number;
  planName: string;
  reviewId: number;
  reviewName: string;
  caseId: number;
  reviewedCase: CaseDetail;
  latestCase: CaseDetail;
  reviewerId: number;
  reviewerName: string;
  reviewDate: string;
  reviewStatus: EnumMessage<ReviewStatus>;
  reviewRemark: string;
  createdBy: number;
  createdByName: string;
  createdDate: string;
}
