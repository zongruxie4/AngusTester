export type MemberCount = {
    assigneeId: string;
    assigneeName: string;
    assigneeAvatar: string;
    totalTaskNum: string;
    validTaskNum: string;
    evalWorkload: string;
    actualWorkload: string;
    completedNum: string;
    completedRate: string;
    overdueNum: string;
    overdueRate: string;
    progress: string;
}

// TODO 重复代码
export type SprintInfo = {
    id: string;
    projectId: string;
    projectName: string;
    name: string;
    status: {
        value: string;
        message: string;
    };
    auth: false;
    startDate: string;
    deadlineDate: string;
    ownerId: string;
    ownerName: string;
    ownerAvatar: string;
    taskPrefix: string;
    priority: {
        message: string;
        value: 'HIGH' | 'HIGHEST' | 'LOW' | 'LOWEST' | 'MEDIUM';
    };
    evalWorkloadMethod: {
        message: string;
        value: 'WORKING_HOURS' | 'STORY_POINT';
    };
    tenantId: string;
    tenantName: string;
    createdBy: string;
    createdByName: string;
    createdDate: string;
    lastModifiedBy: string;
    lastModifiedByName: string;
    lastModifiedDate: string;
    taskNum: string;
    validNum: string;
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
}
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

export type EditFormState = {
  projectId: string | undefined;
  assigneeId: string | undefined;
  deadlineDate: string | undefined;
  name: string | undefined;
  priority: 'HIGH' | 'HIGHEST' | 'LOW' | 'LOWEST' | 'MEDIUM' | undefined;
  sprintId: string | undefined;
  taskType: 'API_TEST' | 'BUG' | 'SCENARIO_TEST' | 'STORY' | 'TASK' | 'REQUIREMENT' | undefined;
  moduleId?: string | undefined;
  confirmorId?: string | undefined;
  attachments?: {
    name: string;
    url: string;
  }[] | undefined;
  targetParentId?: string | undefined;
  targetId?: string | undefined;
  tagIds?: string[] | undefined;
  refTaskIds?: string[] | undefined;
  refCaseIds?: string[] | undefined;
  description?: string | undefined;
  evalWorkload?: string | undefined;
  actualWorkload?: string | undefined;
  testType?: 'FUNCTIONAL' | 'PERFORMANCE' | 'STABILITY' | 'CUSTOMIZATION' | undefined;
  bugLevel?: 'CRITICAL' | 'MAJOR' | 'MINOR' | 'TRIVIAL';
  testerId?: string;
  missingBugFlag?: boolean;
  softwareVersion?: string;
}
