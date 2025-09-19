import { EnumMessage, EvalWorkloadMethod, Priority, Result } from '@xcan-angus/infra';
import { BugLevel, ExecResult, TaskStatus, TaskType, TestType } from '@/enums/enums';

export type TaskDetail = {
  id: string;
  code: string;
  name: string;
  description: string;
  taskType: EnumMessage<TaskType>;
  testType: EnumMessage<TestType>;
  projectId: string;
  projectName: string;
  parentTaskId: string;
  parentTaskName: string;
  serviceId: string;
  sprintId: string;
  sprintName: string;
  sprintAuth: any;
  activityNum: string;
  assigneeId: string;
  assigneeName: string;
  assigneeAvatar: string;
  canceledDate: string;
  commentNum: string;
  startDate: string;
  completedDate: string;
  confirmTask: boolean;
  confirmedDate: string;
  deadlineDate: string;
  processedDate: string;
  overdue: boolean;
  priority: EnumMessage<Priority>;
  softwareVersion: string;
  failNum: string;
  totalNum: string;
  status: EnumMessage<TaskStatus>;
  bugLevel: EnumMessage<BugLevel>;
  missingBug: boolean;
  unplanned: boolean;
  favourite: boolean;
  follow: boolean;
  moduleId: string;
  moduleName: string;
  confirmerId: string;
  confirmerName: string;
  currentAssociateType: { message: string; value: string; }[];
  evalWorkloadMethod: EnumMessage<EvalWorkloadMethod>;
  evalWorkload: string;
  actualWorkload: string;
  attachments: {
    name: string;
    url: string;
  }[];
  execBy: string;
  execByName: string;
  execDate: string;
  execFailureMessage: string;
  execId: string;
  execName: string;
  execResult: EnumMessage<Result>;
  execTestFailureNum: string;
  execTestNum: string;
  scriptId: string;
  scriptName: string;
  targetId: string;
  targetName: string;
  targetParentId: string;
  targetParentName: string;
  testerId?: string;
  testerName?: string;
  createdBy: string;
  createdByName: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
  tags: {
    id: string;
    name: string;
  }[];
  refTaskInfos: {
    id: string;
    name: string;
    code: string;
    projectId: string;
    sprintId: string;
    taskType: EnumMessage<TaskType>;
  }[];
  refCaseInfos: {
    id: string;
    name: string;
    code: string;
    projectId: string;
    sprintId: string;
  }[];
  subTaskProgress: {
    completed: string;
    completedRate: string;
    total: string
  };
  subTaskInfos: {
    id: string;
    code: string;
    name: string;
    priority: string;
    projectId: string;
    sprintId: string;
    startDate: string;
    taskType: string;
    testType: string;
    assigneeId: string;
    assigneeName: string;
    confirmerId: string;
    confirmerName: string;
    createdBy: string;
    createdByName: string;
    createdDate: string;
    deadlineDate: string;
    lastModifiedBy: string;
    lastModifiedByName: string;
    lastModifiedDate: string;
  }[];
  linkUrl?: string;
}

/**
 * Task information interface containing all task-related data
 */
export type TaskInfo = {
  id: string;
  name: string;
  code: string;
  projectId: string;
  sprintId: string;
  sprintName: string;
  targetId: string;
  targetParentId: string;
  taskType: EnumMessage<TaskType>;
  testType: EnumMessage<TestType>;
  deadlineDate: string;
  assigneeId: string;
  assigneeName: string;
  priority: EnumMessage<Priority>;
  evalWorkloadMethod: EnumMessage<EvalWorkloadMethod>;
  status: EnumMessage<TaskStatus>;
  execResult: EnumMessage<ExecResult>;
  execFailureMessage: string;
  execTestNum: string;
  execTestFailureNum: string;
  execId: string;
  execName: string;
  execBy: string;
  execByName: string;
  execDate: string;
  failNum: string;
  totalNum: string;
  overdue: false;
  createdBy: string;
  createdByName: string;
  createdDate: string;
}
