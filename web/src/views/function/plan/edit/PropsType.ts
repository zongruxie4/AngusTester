export type FormState = {
  deadlineDate: string;
  evalWorkloadMethod: string;
  name: string;
  ownerId: string;
  projectId: string;
  reviewFlag: boolean;
  startDate: string;
  casePrefix?: string;
  description?: string;
  attachments?: {
    name: string;
    url: string;
  }[];
  id?: string;
  testingObjectives?: string,
  testingScope: string,
  otherInformation:string,
  acceptanceCriteria: string;
  testerResponsibilities: {[id: string]: string};
  date?: [string, string];// 前端回显startDate、deadlineDate自动添加的字段，保存时需要删除
}
