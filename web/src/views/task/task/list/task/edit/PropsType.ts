export type FormState = {
  assigneeId: string | undefined;
  deadlineDate: string | undefined;
  name: string | undefined;
  priority: 'HIGH' | 'HIGHEST' | 'LOW' | 'LOWEST' | 'MEDIUM' | undefined;
  sprintId: string | undefined;
  taskType: 'API_TEST' | 'BUG' | 'SCENARIO_TEST' | 'STORY' | 'TASK' | 'REQUIREMENT' | undefined;
  moduleId?: string | undefined;
  confirmorId?: string | undefined;
  attachments?: {
    name: string;
    url: string;
  }[] | undefined;
  targetParentId?: string | undefined;
  targetId?: string | undefined;
  parentTaskId?: string | undefined;
  tagIds?: string[] | undefined;
  refTaskIds?: string[] | undefined;
  refCaseIds?: string[] | undefined;
  description?: string | undefined;
  evalWorkload?: string | undefined;
  actualWorkload?: string | undefined;
  testType?: 'FUNCTIONAL' | 'PERFORMANCE' | 'STABILITY' | 'CUSTOMIZATION' | undefined;
  bugLevel?: 'CRITICAL' | 'MAJOR'| 'MINOR' | 'TRIVIAL';
  testerId?: string;
  missingBugFlag?: boolean;
  softwareVersion?: string;
}
