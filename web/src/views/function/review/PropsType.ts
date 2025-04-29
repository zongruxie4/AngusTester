export type ReviewInfo = {
  attachments: {
    id: string;
    name: string;
    url: string;
  }[];
  authFlag: boolean;
  autoUpdateResultByExec: boolean;
  caseNum: string;
  casePrefix: string;
  createdBy: string;
  createdByName: string;
  createdDate: string;
  deadlineDate: string;
  description: string;
  evalWorkloadMethod: {
    value: string;
    message: string;
  };
  id: string;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
  name: string;
  ownerAvatar: string;
  ownerId: string;
  ownerName: string;
  progress: {
    completed: string;
    completedRate: string;
    total: string
  };
  projectId: string;
  projectName: string;
  reviewFlag: boolean;
  startDate: string;
  status: {
    value: 'BLOCKED' | 'COMPLETED' | 'IN_PROGRESS' | 'PENDING';
    message: string;
  };
  tenantId: string;
  tenantName: string;
  members: {
    avatar: string;
    email: string;
    fullName: string;
    id: string;
    mobile: string;
    username: string
  }[];
  showMembers: {
    id: string;
    username: string;
    fullName: string;
    mobile: string;
    email: string;
    avatar: string;
  }[];
  testingObjectives?: string,
  testingScope: string,
  otherInformation:string,
  acceptanceCriteria: string;
  testerResponsibilities: {[id: string]: string};
}

export type IPane = {
  _id: string;// pane唯一标识
  name: string; // pane的tab文案
  value: string;// pane内部的组件标识
  closable?: boolean;// 是否允许关闭，true - 允许关闭，false - 禁止关闭
  forceRender?: boolean;// 被隐藏时是否渲染 DOM 结构，可选
  icon?: string;// tab文案前面的icon，可选
  active?: boolean; // 是否选中，添加不用设置，缓存时用于记录上次激活的tab pane，可选

  // 组件需要的属性
  data?: { [key: string]: any; };
};
