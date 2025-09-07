export type PlanInfo = {
  attachments: {
    id: string;
    name: string;
    url: string;
  }[];
  auth: boolean;
  autoUpdateResultByExec: boolean;
  caseNum: string;
  casePrefix: string;
  createdBy: string;
  createdByName: string;
  createdDate: string;
  deadlineDate: string;
  description: string;
  evalWorkloadMethod: {
    value: string;
    message: string;
  };
  id: string;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
  name: string;
  ownerAvatar: string;
  ownerId: string;
  ownerName: string;
  progress: {
    completed: string;
    completedRate: string;
    total: string
  };
  projectId: string;
  projectName: string;
  review: boolean;
  startDate: string;
  status: {
    value: 'BLOCKED' | 'COMPLETED' | 'IN_PROGRESS' | 'PENDING';
    message: string;
  };
  tenantId: string;
  tenantName: string;
  members: {
    avatar: string;
    email: string;
    fullName: string;
    id: string;
    mobile: string;
    username: string
  }[];
  showMembers: {
    id: string;
    username: string;
    fullName: string;
    mobile: string;
    email: string;
    avatar: string;
  }[];
  testingObjectives?: string,
  testingScope: string,
  otherInformation:string,
  acceptanceCriteria: string;
  testerResponsibilities: {[id: string]: string};
}
