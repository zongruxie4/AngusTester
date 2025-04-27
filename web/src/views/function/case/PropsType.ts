export type FilterOp= 'EQUAL' | 'NOT_EQUAL' | 'GREATER_THAN' | 'GREATER_THAN_EQUAL' | 'LESS_THAN' | 'LESS_THAN_EQUAL' | 'CONTAIN' | 'NOT_CONTAIN' | 'MATCH_END' | 'MATCH' | 'IN' | 'NOT_IN'
export type Filters = {key:string, value:string, op:FilterOp};
export type SearchParams = {
  pageNo:number;
  pageSize:number;
  filters?:Filters[];
  orderBy?:string;
  orderSort?:'ASC' | 'DESC';
}

export type PlanObj = {
  name: string;
  id: string;
  createdBy: string;
  createdDate: string;
  authFlag: boolean;
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
  overdueFlag: boolean;
  evalWorkloadMethod: EnumType;
  evalWorkload: string | null;
  actualWorkload: string | null;
  precondition: string;
  description: string | null;
  reviewFlag: boolean;
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

export type CaseActionAuth = 'edit'| 'debug' | 'review' | 'clone' | 'move' | 'delete' | 'updateTestResult_passed' | 'updateTestResult_notpassed' | 'updateTestResult_blocked' | 'updateTestResult_canceled' | 'resetTestResult' | 'resetReviewResult' | 'retestResult' | 'copy' | 'favourite' | 'follow' | 'copyUrl'

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
  code: any;
  isOpen: boolean;
  checkAll: boolean;
  list: CaseListObj[],
  indeterminate?: boolean;
  method?: { value: string; message: string };
  endpoint?: string;
  selectedRowKeys?: string[]
}
