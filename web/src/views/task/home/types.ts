export type ResourceInfo = {
  allSprint: string;
  sprintByLastWeek: string;
  sprintByLastMonth: string;
  allTask: string;
  taskByLastWeek: string;
  taskByLastMonth: string;
  taskByOverdue: string;
  allTag: string;
  tagByLastWeek: string;
  tagByLastMonth: string;
  sprintByStatus: {
    PENDING: string;
    IN_PROGRESS: string;
    COMPLETED: string;
    BLOCKED: string;
  };
  taskByType: {
    STORY: string;
    REQUIREMENT: string;
    TASK: string;
    BUG: string;
    API_TEST: string;
    SCENARIO_TEST: string;
  };
  taskByStatus: {
    PENDING: string;
    IN_PROGRESS: string;
    CONFIRMING: string;
    COMPLETED: string;
    CANCELED: string;
  };
  taskByPriority: {
    HIGHEST: string;
    HIGH: string;
    MEDIUM: string;
    LOW: string;
    LOWEST: string;
  };
};

export type DataItem = {
  id: string;
  name: string;
  code: string;
  projectId: string;
  sprintId: string;
  sprintName: string;
  targetId: string;
  targetParentId: string;
  taskType: {
    message: string;
    value: 'API_TEST' | 'BUG' | 'SCENARIO_TEST' | 'STORY' | 'TASK' | 'REQUIREMENT';
  };
  testType: {
    message: string;
    value: 'FUNCTIONAL' | 'PERFORMANCE' | 'STABILITY' | 'CUSTOMIZATION';
  };
  deadlineDate: string;
  assigneeId: string;
  assigneeName: string;
  priority: {
    message: string;
    value: 'HIGH' | 'HIGHEST' | 'LOW' | 'LOWEST' | 'MEDIUM';
  };
  evalWorkloadMethod: {
    message: string;
    value: 'WORKING_HOURS' | 'STORY_POINT';
  };
  status: {
    value: string;
    message: string;
  };
  execResult: {
    value: 'SUCCESS' | 'FAIL',
    message: string;
  };
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
