import { Priority } from '@xcan-angus/infra';
import { TaskType, BugLevel } from '@/enums/enums';
import { TaskDetail, TaskInfo } from '@/views/issue/types';
import { AttachmentInfo } from '@/types/types';

export type TaskEditState = {
  /** Project identifier for context */
  projectId?: string | undefined;
  /** Sprint Id */
  sprintId: string | undefined;
  /** Function case module identifier for task categorization */
  moduleId?: string | undefined;
  /** Task display name for identification and organization */
  name: string | undefined;
  /** Software version identifier associated with the task */
  softwareVersion?: string | undefined;
  /** Task classification type for workflow management. Note: Test task type cannot be modified after creation */
  taskType: TaskType;
  /** Bug severity level for defect classification */
  bugLevel?: BugLevel | undefined;
  /** Primary assignee identifier for task responsibility assignment */
  assigneeId: string | undefined;
  /** Reviewer identifier for task validation and approval */
  confirmerId?: string | undefined;
  /** Test execution identifier for task testing responsibility */
  testerId?: string | undefined;
  /** Flag indicating if this is a missing bug report */
  escapedBug?: boolean | undefined;
  /** Tag identifiers for task categorization and filtering */
  tagIds?: string[] | undefined;
  /** Task priority level for scheduling and resource allocation */
  priority: Priority;
  /** Task completion deadline for timeline management */
  deadlineDate: string | undefined;
  /** Task-related file attachments for documentation and reference */
  attachments?: AttachmentInfo[] | undefined;
  /** Detailed task description for requirements and context */
  description?: string | undefined;
  /** Estimated workload for task planning and resource allocation */
  evalWorkload?: string | undefined;
  /** Actual workload for performance tracking and analysis */
  actualWorkload?: string | undefined;
  /** Parent task identifier for hierarchical task relationships */
  parentTaskId?: string | undefined;
  /** Referenced task identifiers for dependency management */
  refTaskIds?: string[] | undefined;
  /** Referenced test case identifiers for test coverage linkage */
  refCaseIds?: string[] | undefined;

  // Temp for edit
  userInfo?: { id: string; fullName?: string};
  appInfo?: { id: string; };
  visible?: boolean;
  taskId?: string;
}

/**
 * Props interface for AssocTask component
 */
export interface AssocTaskProps {
  /** Project identifier for context */
  projectId: string;
  /** User information object */
  userInfo: { id: string; fullName?: string};
  /** Application information object */
  appInfo: { id: string; };
  /** Current task identifier */
  taskId: string;
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
  projectId: string;
  userInfo: { id: string; fullName: string};
  appInfo: { id: string; };
  dataSource: TaskDetail;
  largePageLayout?: boolean;
  taskId?: string;
  loading?: boolean;
  notify?: string;
  id?: string;
  tips?: string;
  taskInfo?: any;
}
