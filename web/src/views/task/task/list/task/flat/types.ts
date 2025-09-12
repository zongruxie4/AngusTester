export type SubTaskInfos = {
    assigneeId: string;
    assigneeName: string;
    code: string;
    confirmerId: string;
    confirmerName: string;
    createdBy: string;
    createdByName: string;
    createdDate: string;
    deadlineDate: string;
    id: string;
    lastModifiedBy: string;
    lastModifiedByName: string;
    lastModifiedDate: string;
    name: string;
    priority: string;
    projectId: string;
    sprintId: string;
    startDate: string;
    taskType: string;
    testType: string;
}

// TODO 考虑复用
export type ActivityItem = {
  id: string;
  projectId: string;
  userId: string;
  fullName: string;
  avatar: string;
  targetId: string;
  parentTargetId: string;
  targetType: {
    value: string;
    message: string
  };
  targetName: string;
  optDate: string;
  description: string;
  detail: string
}
