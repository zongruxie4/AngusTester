export type SummaryInfo = {
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
