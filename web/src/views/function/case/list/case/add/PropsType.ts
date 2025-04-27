export type FormState = {
  attachments: {
    name: string;
    url: string;
  }[],
  caseType: 'FUNC' | 'APIS'
  deadlineDate: string;
  description: string;
  evalWorkload: string;
  actualWorkload: string;
  moduleId: string;
  name: string;
  planId: string;
  precondition: string;
  priority: string;
  steps: {
    expectedResult: string;
    step: string;
  } [],
  tagIds: string[];
  testerId: string;
  refIdMap:{
    TASK:string[];
    CASE:string[];
  };
  developerId?: string
}
