// Type Definitions
export type Remark = {
  content: string;
  createdBy: string;
  createdByName: string;
  createdDate: string;
  id: string;
  taskId: string;
}

export type MemberCount = {
    assigneeId: string;
    assigneeName: string;
    assigneeAvatar: string;
    totalTaskNum: string;
    validTaskNum: string;
    evalWorkload: string;
    actualWorkload: string;
    completedNum: string;
    completedRate: string;
    overdueNum: string;
    overdueRate: string;
    progress: string;
}
