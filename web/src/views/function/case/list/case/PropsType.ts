
export type CaseTypeObj = 'FUNC' | 'APIS'

export type CaseActionAuth = 'edit'| 'debug' | 'review' | 'clone' | 'move' | 'delete' | 'updateTestResult_passed' | 'updateTestResult_notpassed' | 'updateTestResult_blocked' | 'updateTestResult_canceled' | 'resetTestResult' | 'retestResult' | 'resetReviewResult' | 'copy' | 'favourite' | 'follow' | 'copyUrl'
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

type EnumType = {
  value: string;
  message: string;
};

type TagType = {
  id: string;
  name: string;
}

export type CaseInfoObj = {
  id: string;
  planId: string;
  planName: string;
  moduleId: string;
  moduleName: string;
  caseType: EnumType;
  apisId: string | null;
  apisSummary: string | null;
  apisProjectId: string | null;
  apisProjectName: string | null;
  name: string;
  code: string;
  priority: EnumType;
  deadlineDate: string;
  overdue: boolean;
  evalWorkloadMethod: EnumType;
  evalWorkload: string | null;
  actualWorkload: string | null;
  precondition: string;
  description: string | null;
  review: boolean;
  reviewerId: string | null;
  reviewerName: string | null;
  reviewDate: string | null;
  reviewStatus: EnumType;
  reviewRemark: string | null;
  reviewNum: string;
  testerId: string;
  testerName: string;
  testNum: string;
  testFailNum: string;
  testResult: EnumType;
  testResultHandleDate: string | null;
  attachments: any;
  steps: any[];
  protocol: EnumType | null;
  method: EnumType | null;
  endpoint: string | null;
  currentServer: any;
  parameters: any;
  requestBody: any;
  authentication: any;
  assertions: any;
  tags: TagType[];
  refMap: any;
  favouriteFlag: boolean;
  followFlag: boolean;
  apisDeletedFlag: boolean;
  commentNum: string;
  tenantId: string;
  createdBy: string;
  createdByName: string;
  avatar: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
};

export type CaseListObj = CaseInfoObj & {
  actualWorkload: string | null;
  apisId: string | null;
  avatar: string;
  favouriteFlag: boolean | null;
  testFailNum: string;
  followFlag: boolean | null;
  checked: boolean;
  id: string
};

export type GroupCaseListObj = {
  id: string;
  groupName: any;
  isOpen: boolean;
  checkAll: boolean;
  children: CaseListObj[],
  indeterminate?: boolean;
  method?: { value: string; message: string };
  endpoint?: string;
  selectedRowKeys?: string[]
}

type TreeData = {
  name: string;
  sequece: string;
  level: number;
  children?: TreeData[];
  id: string;
  pid?: string;
  index: number;
  ids: string[];
  isLast: boolean;
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
