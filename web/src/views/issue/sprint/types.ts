import { EnumMessage, EvalWorkloadMethod } from '@xcan-angus/infra';
import { TaskMeetingType, TaskSprintStatus } from '@/enums/enums';

export type SprintInfo = {
  id: string;
  projectId: string;
  projectName: string;
  name: string;
  status: EnumMessage<TaskSprintStatus>;
  attachments: {
    id: string;
    name: string;
    url: string;
  }[];
  description: string;
  auth: boolean;
  startDate: string;
  deadlineDate: string;
  ownerId: string;
  ownerName: string;
  ownerAvatar: string;
  acceptanceCriteria: string;
  otherInformation: string;
  autoUpdateResultByExec: boolean;
  evalWorkloadMethod: EnumMessage<EvalWorkloadMethod>;
  tenantId: string;
  tenantName: string;
  createdBy: string;
  creator: string;
  createdDate: string;
  modifiedBy: string;
  modifier: string;
  modifiedDate: string;
  taskNum: string;
  taskPrefix: string;
  progress: {
    total: string;
    completed: string;
    completedRate: string;
  };
  members: {
    id: string;
    username: string;
    fullName: string;
    mobile: string;
    email: string;
    avatar: string;
  }[];
  showMembers: {
    id: string;
    username: string;
    fullName: string;
    mobile: string;
    email: string;
    avatar: string;
  }[];
  meetings?: {
    type: EnumMessage<TaskMeetingType>;
    date: string;
    time: string;
    location: string;
    moderator: { id: string; fullName: string; };
    participants: { id: string; fullName: string; }[];
    content: string;
  }[];
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

export type EditFormState = {
  projectId: string;
  evalWorkloadMethod: string;
  name: string;
  ownerId: string | undefined;
  deadlineDate: string;
  startDate: string;
  taskPrefix?: string;
  otherInformation?: string;
  acceptanceCriteria?: string;
  attachments?: { name: string, url: string }[];
  id?: string;
  meetings?: { [key: string]: string | string[] }[],
  date?: [string, string];
}
