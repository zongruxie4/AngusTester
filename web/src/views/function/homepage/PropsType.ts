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
    overdueFlag: boolean;
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
