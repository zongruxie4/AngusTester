export type FormState = {
  planId: string;
  deadlineDate: string;
  caseIds?: string[],
  participantIds?: string[]
  name: string;
  ownerId: string;
  casePrefix?: string;
  description?: string;
  attachments?: {
    name: string;
    url: string;
  }[];
  id?: string;
  date?: [string, string];// 前端回显startDate、deadlineDate自动添加的字段，保存时需要删除
}

export type BaselineCaseInfo = {
  id: string;
  caseId: string;
  name: string;
  createdByName: string;
  createDate: string;
  status: {
    value: string;
    message: string;
  }
}
