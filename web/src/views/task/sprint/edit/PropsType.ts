export type FormState = {
  projectId: string;
  evalWorkloadMethod: string;
  name: string;
  ownerId: string | undefined;
  deadlineDate: string;
  startDate: string;
  taskPrefix?: string;
  otherInformation?: string;
  acceptanceCriteria?: string;
  attachments?: { name: string, url: string }[];
  id?: string;
  meetings?: {[key: string]: string|string[]}[],
  date?: [string, string];// 前端回显startDate、deadlineDate自动添加的字段，保存时需要删除
}
