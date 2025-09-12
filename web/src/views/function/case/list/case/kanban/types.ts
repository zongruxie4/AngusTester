import { CaseTestResult } from '@/enums/enums';

// TODO 使用枚举替代
export type PlanPermissionKey = 'ADD' |
    'VIEW' |
    'MODIFY' |
    'DELETE' |
    'GRANT' |
    'ADD_PLAN' |
    'MODIFY_PLAN' |
    'DELETE_PLAN' |
    'ADD_CASE' |
    'MODIFY_CASE' |
    'DELETE_CASE' |
    'EXPORT_CASE' |
    'REVIEW' |
    'RESET_REVIEW_RESULT' |
    'TEST' | 'RESET_TEST_RESULT';

export type ReviewStatus = 'PENDING' | 'PASSED' | 'FAILED';

export type ActionMenuItem = {
    name: string;
    key:'addBug' | 'delete' | 'edit' | 'testPassed' | 'testNotPassed' | 'retest' | 'cancel' | 'clone' | 'move' | 'cancelFavourite' | 'favourite' | 'cancelFollow' | 'follow' | 'resetTestResult' | 'block';
    icon: string;
    disabled: boolean;
    hide: boolean;
    tip?: string;
}

export type CaseInfo = {
    id: string;
    priority: {
        value: CaseTestResult;
        message: string;
    };
    testResult: {
        value: CaseTestResult;
        message: string;
    };
    planId: string;
    planName: string;
    testerId: string;
    testerName: string;
    testerAvatar: string;
    developerId: string;
    lastModifiedBy: string;
    lastModifiedByName: string;
    createdBy: string;
    createdByName: string;
    developerName: string;
    reviewerName: string;
    name: string;
    overdue: boolean;
    code: string;
    deadlineDate: string;
    review: boolean;
    reviewStatus: {
        value: ReviewStatus;
        message: string;
    };
    testFailNum: string;
    testNum: string;
    description: string;
    precondition: string;
    favourite: boolean;
    follow: boolean;
    refMap:{
        TASK: string[];
        CASE: string[];
    };
    evalWorkloadMethod:{
        value: string;
        message: string;
    };
    steps: {expectedResult:string;step:string;}[]
}
