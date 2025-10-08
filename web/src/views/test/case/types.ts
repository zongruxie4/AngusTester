import { CaseInfo } from '@/views/test/types';
import { TaskInfo } from '@/views/issue/types';
import { FuncPlanPermission, TaskType } from '@/enums/enums';

/**
 * Case view mode
 */
export enum CaseViewMode {
  table = 'table',
  flat = 'flat',
  kanban = 'kanban'
}

export type CaseActionAuth =
  'edit'
  | 'review'
  | 'clone'
  | 'move'
  | 'delete'
  | 'addFollow'
  | 'cancelFollow'
  | 'addFavourite'
  | 'cancelFavourite'
  | 'updateTestResult'
  | 'resetTestResult'
  | 'retestResult'
  | 'resetReviewResult';

export type CaseCount = {
  /** The total number of cases */
  totalCaseNum: number;
  /** The total number of tested cases, Ignoring cancel status cases */
  validCaseNum: number;
  /** The total number of already tested cases */
  totalTestedCaseNum: number;
  /** The number of overdue cases */
  overdueNum: number;
  /** The number of pending review cases */
  pendingReviewNum: number;
  /** The number of passed review cases */
  passedReviewNum: number;
  /** The number of failed review cases */
  failedReviewNum: number;
  /** The total number of review cases */
  totalReviewCaseNum: number;
  /** The total number of already reviewed cases */
  totalReviewedCaseNum: number;
  /** The total number(times) of review */
  totalReviewTimes: number;
  /** The number of cases that the review passed at one time */
  oneTimePassReviewNum: number;
  /** One-time pass review rate */
  oneTimePassReviewRate: number;
  /** The number of pending test cases */
  pendingTestNum: number;
  /** The number of passed test cases */
  passedTestNum: number;
  /** The number of not passed test cases */
  notPassedTestNum: number;
  /** The number of blocked test cases */
  blockedTestNum: number;
  /** The number of canceled test cases */
  canceledTestNum: number;
  /** The number of cases that the test passed at one time */
  oneTimePassedTestNum: number;
  /** One-time pass test rate */
  oneTimePassedTestRate: number;
  /** The total times of test */
  totalTestTimes: number;
  /** The total times of test failure */
  totalTestFailTimes: number;
  /** Eval workload */
  evalWorkload: number;
  /** Actual workload */
  actualWorkload: number;
  /** Completed workload */
  completedWorkload: number;
  /** progress = passedTestNum / validCaseNum * 100 **/
  progress: string;
}

export interface AssocCaseProps {
  projectId: number;
  userInfo: { id: number; };
  appInfo: { id: number; };
  caseId: number;
  dataSource: CaseInfo[];
}

export interface AssocTaskProps {
  projectId: number;
  userInfo: { id: number; };
  appInfo: { id: number; };
  caseId: number;
  dataSource: TaskInfo[];
  title: string;
  taskType: TaskType;
  tips?: string;
}

export const getActionAuth = (_authList: string[]) => {
  const actionAuth: CaseActionAuth[] = [];
  if (_authList.includes(FuncPlanPermission.MODIFY_CASE)) {
    actionAuth.push('edit');
    actionAuth.push('move');
  }

  if (_authList.includes(FuncPlanPermission.ADD_CASE)) {
    actionAuth.push('clone');
  }

  if (_authList.includes(FuncPlanPermission.DELETE_CASE)) {
    actionAuth.push('delete');
  }

  if (_authList.includes(FuncPlanPermission.TEST)) {
    actionAuth.push('updateTestResult');
  }

  if (_authList.includes(FuncPlanPermission.RESET_TEST_RESULT)) {
    actionAuth.push('resetTestResult');
  }

  if (_authList.includes(FuncPlanPermission.RESET_TEST_RESULT)) {
    actionAuth.push('retestResult');
  }

  if (_authList.includes(FuncPlanPermission.REVIEW)) {
    actionAuth.push('review');
  }

  if (_authList.includes(FuncPlanPermission.RESET_REVIEW_RESULT)) {
    actionAuth.push('resetReviewResult');
  }
  return actionAuth;
};
