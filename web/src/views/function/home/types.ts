export type ResourceInfo = {
    allPlan: string;
    planByLastWeek: string;
    planByLastMonth: string;
    allCase: string;
    caseByLastWeek: string;
    caseByLastMonth: string;
    caseByOverdue: string;
    allTag: string;
    tagByLastWeek: string;
    tagByLastMonth: string;
    allModule: string;
    moduleByLastWeek: string;
    moduleByLastMonth: string;
    planByStatus: {
        PENDING: string;
        IN_PROGRESS: string;
        COMPLETED: string;
        BLOCKED: string;
    };
    caseByTestResult: {
        PENDING: string;
        PASSED: string;
        NOT_PASSED: string;
        BLOCKED: string;
        CANCELED: string;
    };
    caseByReviewStatus: {
        PENDING: string;
        PASSED: string;
        FAILED: string;
    };
    caseByPriority: {
        HIGHEST: string;
        HIGH: string;
        MEDIUM: string;
        LOW: string;
        LOWEST: string;
    },
    reviewByStatus: {
        PENDING: string;
        IN_PROGRESS: string;
        COMPLETED: string;
    }
};

export type CaseItem = {
    id: string;
    planId: string;
    planName: string;
    name: string;
    code: string;
    priority: {
        value: string;
        message: string;
    };
    deadlineDate: string;
    overdue: boolean;
    evalWorkloadMethod: {
        value: string;
        message: string;
    };
    review: boolean;
    reviewStatus: {
        value: string;
        message: string;
    };
    reviewNum: string;
    reviewFailNum: string;
    testerId: string;
    testerName: string;
    testNum: string;
    testFailNum: string;
    testResult: {
        value: string;
        message: string;
    };
    tags: string[];
    tenantId: string;
    createdBy: string;
    createdByName: string;
    avatar: string;
    createdDate: string;
    lastModifiedBy: string;
    lastModifiedDate: string;
}
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
  testResult: {
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
