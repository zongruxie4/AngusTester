import { Priority } from '@xcan-angus/infra';
import { TaskType, BugLevel, TestType } from '@/enums/enums';
import { TaskDetail, TaskInfo } from '@/views/issue/types';
import { AttachmentInfo } from '@/types/types';

export type TaskEditState = {
  /** Project identifier for context */
  projectId?: number | undefined;
  /** Sprint Id */
  sprintId: number | undefined;
  /** Function case module identifier for task categorization */
  moduleId?: number | undefined;
  /** Task display name for identification and organization */
  name: string | undefined;
  /** Software version identifier associated with the task */
  softwareVersion?: string | undefined;
  /** Task classification type for workflow management. Note: Test task type cannot be modified after creation */
  taskType: TaskType;
  /** Bug severity level for defect classification */
  bugLevel?: BugLevel | undefined;
  /** Primary assignee identifier for task responsibility assignment */
  assigneeId: number | undefined;
  /** Reviewer identifier for task validation and approval */
  confirmerId?: number | undefined;
  /** Test execution identifier for task testing responsibility */
  testerId?: number | undefined;
  /** Flag indicating if this is a missing bug report */
  escapedBug?: boolean | undefined;
  /** Tag identifiers for task categorization and filtering */
  tagIds?: number[] | undefined;
  /** Task priority level for scheduling and resource allocation */
  priority: Priority;
  /** Task completion deadline for timeline management */
  deadlineDate: string | undefined;
  /** Task-related file attachments for documentation and reference */
  attachments?: AttachmentInfo[] | undefined;
  /** Detailed task description for requirements and context */
  description?: string | undefined;
  /** Estimated workload for task planning and resource allocation */
  evalWorkload?: number | undefined;
  /** Actual workload for performance tracking and analysis */
  actualWorkload?: number | undefined;
  /** Parent task identifier for hierarchical task relationships */
  parentTaskId?: number | undefined;
  /** Referenced task identifiers for dependency management */
  refTaskIds?: number[] | undefined;
  /** Referenced test case identifiers for test coverage linkage */
  refCaseIds?: number[] | undefined;
  /** Test type for API and scenario testing */
  testType?: TestType | undefined;
  /** Target identifier for API and scenario testing */
  targetId?: string | undefined;
  /** Target parent identifier for API testing */
  targetParentId?: string | undefined;

  // Temp for edit
  userInfo?: { id: number; fullName?: string};
  appInfo?: { id: number; };
  visible?: boolean;
  taskId?: number;
}

/**
 * Props interface for AssocTask component
 */
export interface AssocTaskProps {
  /** Project identifier for context */
  projectId: number;
  /** User information object */
  userInfo: { id: number; fullName?: string};
  /** Application information object */
  appInfo: { id: number; };
  /** Current task identifier */
  taskId: number;
  /** List of associated task data */
  dataSource: TaskInfo[];
  /** Display title for the component */
  title: string;
  /** Type of tasks to filter and display */
  taskType: TaskType;
  /** Optional tips or description text */
  tips?: string;
}

export type TaskDetailProps = {
  projectId: number;
  userInfo: { id: number; fullName: string};
  appInfo: { id: number; };
  dataSource: TaskDetail;
  largePageLayout?: boolean;
  taskId?: number;
  loading?: boolean;
  notify?: string;
  id?: number;
  tips?: string;
  taskInfo?: any;
}
