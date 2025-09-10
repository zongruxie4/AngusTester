
export type CaseActionAuth = 'edit' | 'review' | 'clone' | 'move' | 'delete' | 'updateTestResult' | 'resetTestResult' | 'retestResult' | 'resetReviewResult';

export const CASE_PROJECT_PERMISSIONS = [
  'ADD',
  'VIEW',
  'MODIFY',
  'DELETE',
  'GRANT',
  'ADD_PLAN',
  'MODIFY_PLAN',
  'DELETE_PLAN',
  'ADD_CASE',
  'MODIFY_CASE',
  'DELETE_CASE',
  'EXPORT_CASE',
  'REVIEW',
  'RESET_REVIEW_RESULT',
  'TEST',
  'RESET_TEST_RESULT'
];

export const useCaseActionAuth = () => {
  const getActionAuth = (_authList: string[]) => {
    const actionAuth: CaseActionAuth[] = [];
    if (_authList.includes('MODIFY_CASE')) {
      actionAuth.push('edit');
      actionAuth.push('move');
    }

    if (_authList.includes('ADD_CASE')) {
      actionAuth.push('clone');
    }

    if (_authList.includes('DELETE_CASE')) {
      actionAuth.push('delete');
    }

    if (_authList.includes('TEST')) {
      actionAuth.push('updateTestResult');
    }

    if (_authList.includes('RESET_TEST_RESULT')) {
      actionAuth.push('resetTestResult');
    }

    if (_authList.includes('RESET_TEST_RESULT')) {
      actionAuth.push('retestResult');
    }

    if (_authList.includes('REVIEW')) {
      actionAuth.push('review');
    }

    if (_authList.includes('RESET_REVIEW_RESULT')) {
      actionAuth.push('resetReviewResult');
    }

    return actionAuth;
  };

  return {
    getActionAuth
  };
};

export type CaseTypeObj = 'FUNC' | 'APIS'

export type EnabledGroup = boolean

// 优先级
export type Priority = 'HIGH' | 'HIGHEST' | 'LOW' | 'LOWEST' | 'MEDIUM'

export type CurrentServerObj = {
  url: string;
  description: string;
  variables:
  {
    [key: string]: {
      default: string;
      enum: string[]
      description?: string;
    }
  }
}

export type CountObj = {
  apisCaseNum: string;
  blockedTestNum: string;
  failedReviewNum: string;
  funcCaseNum: string;
  canceledTestNum: string;
  notPassedTestNum: string;
  oneTimePassReviewNum: string;
  oneTimePassReviewRate: string;
  oneTimePassedTestNum: string;
  oneTimePassedTestRate: string;
  overdueNum: string;
  passedReviewNum: string;
  passedTestNum: string;
  pendingReviewNum: string;
  pendingTestNum: string;
  totalCaseNum: string;
  totalReviewCaseNum: string;
  totalReviewNum: string;
  totalTestCaseNum: string;
  totalTestFailNum: string;
  totalTestNum: string;
  totalUsedPlan: string;
  totalPlan: string;
}
