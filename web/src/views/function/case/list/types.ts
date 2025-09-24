import { CaseDetail, CaseTestStep } from '@/views/function/types';
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

export type EnabledGroup = boolean;

export type CaseEditState = {
  name: string;
  planId: number;
  moduleId: number;
  caseType: 'FUNC' | 'APIS'
  deadlineDate: string;
  description: string;
  evalWorkload: string;
  actualWorkload: string;
  precondition: string;
  priority: Priority;
  testerId: number;
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
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: CaseDetail;
  canEdit: boolean;
}

export type CaseDetailChecked = CaseDetail & {
  checked: boolean;
};

export type GroupCaseList = {
  id: string;
  groupName: any;
  isOpen: boolean;
  checkAll: boolean;
  children: CaseDetailChecked[],
  indeterminate?: boolean;
  method?: { value: string; message: string };
  endpoint?: string;
  selectedRowKeys?: string[]
}

export const travelTreeData = (treeData, callback = (item) => item) => {
  function travel (treeData, level = 0, ids: string[] = []) {
    treeData.forEach((item, idx) => {
      item.level = level;
      item.index = idx;
      item.ids = [...ids, item.id];
      item.isLast = idx === (treeData.length - 1);
      travel(item.children || [], level + 1, item.ids),
      item.childLevels = (item.children?.length ? Math.max(...item.children.map(i => i.childLevels)) : 0) + 1;
      item = callback(item);
    });
  }

  travel(treeData);
  return treeData;
};
