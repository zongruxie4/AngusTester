import { CaseDetail, CaseTestStep } from '@/views/test/types';
import { AttachmentInfo } from '@/types/types';
import { Priority } from '@xcan-angus/infra';
import { CaseStepView } from '@/enums/enums';

export type CaseActionAuth =
  'edit' | 'debug' | 'review'
  | 'clone' | 'move' | 'delete' | 'updateTestResult_passed'
  | 'updateTestResult_notPassed' | 'updateTestResult_blocked'
  | 'updateTestResult_canceled' | 'resetTestResult' | 'retestResult'
  | 'resetReviewResult' | 'copy' | 'addFavourite' | 'addFollow' | 'copyUrl'

export type ActionMenuItem = {
  name: string;
  key:'addBug' | 'delete' | 'edit' | 'testPassed' | 'testNotPassed'
    | 'retest' | 'cancel' | 'clone' | 'move' | 'cancelFavourite'
    | 'addFavourite' | 'cancelFollow' | 'addFollow' | 'resetTestResult' | 'block';
  icon: string;
  disabled: boolean;
  hide: boolean;
  tip?: string;
}

export type EnabledModuleGroup = boolean;

export type CaseEditState = {
  name: string;
  planId?: number | undefined;
  moduleId?: number | undefined;
  caseType: 'FUNC' | 'APIS'
  deadlineDate: string;
  description: string;
  evalWorkload: string;
  actualWorkload: string;
  precondition: string;
  priority: Priority;
  testerId?: number;
  developerId?: number,
  stepView: CaseStepView,
  steps: CaseTestStep [],
  tagIds: number[];
  refIdMap: {
    TASK: number[];
    CASE: number[];
  };
  attachments: AttachmentInfo[]
}

export type CaseInfoEditProps = {
  projectId: number;
  userInfo: { id: number; fullName?: string };
  appInfo: { id: number; };
  dataSource: CaseDetail;
  canEdit: boolean;
}

export type GroupCaseList = {
  id: number;
  groupName: any;
  isOpen: boolean;
  checkAll: boolean;
  children: CaseDetail[],
  indeterminate?: boolean;
  method?: { value: string; message: string };
  endpoint?: string;
  selectedRowKeys?: number[]
}
