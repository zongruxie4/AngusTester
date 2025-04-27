
export interface Case {
  name: string, code: string, status: string, id: string, priority: any, apisId: string;
  execResult?: {value: 'FAIL'|'SUCCESS', message: string};
  execTestFailureNum?: string;
}
