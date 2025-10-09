/**
 * Task view mode
 */
export enum TaskViewMode {
  table = 'table',
  flat = 'flat',
  kanban = 'kanban',
  gantt = 'gantt'
}

/**
 * Remark data structure interface
 * <p>
 * Defines the structure of a task remark object
 * containing content, creator information, and metadata
 */
export type Remark = {
    /** Unique identifier of the remark */
    id: number;
    /** ID of the associated task */
    taskId: number;
  /** Rich text content of the remark */
  content: string;
  /** ID of the user who created the remark */
  createdBy: string;
  /** Name of the user who created the remark */
  createdByName: string;
  /** Creation date of the remark */
  createdDate: string;
}

/**
 * Statistics information interface for task metrics and counts
 */
export type TaskCount = {
  /** Total number of tasks */
  totalTaskNum: number;
  /** Total number of tasks, Ignoring cancel status tasks */
  validTaskNum: number;
  /** The number of tasks in pending */
  pendingNum: number;
  /** The number of tasks in progress */
  inProgressNum: number;
  /** The number of tasks is confirming */
  confirmingNum: number;
  /** The number of completed tasks */
  completedNum: number;
  /** The number of canceled tasks */
  canceledNum: number;
  /** Total number of tasks */
  totalStatusNum: number;
  /** The number of Performance Tasks */
  perfNum: number;
  /** The number of function tasks */
  functionalNum: number;
  /** The number of stabilization tasks */
  stabilityNum: number;
  /** Total number of test type tasks */
  totalTestTypeNum: number;
  /** The number of requirement type tasks */
  requirementNum: number;
  /** The number of story type tasks */
  storyNum: number;
  /** The number of task type tasks */
  taskNum: number;
  /** The number of bug type tasks */
  bugNum: number;
  /** The number of api test type tasks */
  apiTestNum: number;
  /** The number of scenario test type tasks */
  scenarioTestNum: number;
  /** Total number of task type tasks */
  totalTaskTypeNum: number;
  /** The number of successful test tasks */
  testSuccessNum: number;
  /** The number of test failed tasks */
  testFailNum: number;
  /** The number of overdue tasks */
  overdueNum: number;
  /** The number of tasks that the test passed at one time */
  oneTimePassedNum: number;
  /** One-time pass rate */
  oneTimePassedRate: number;
  /** The number of task process times */
  processCount: number;
  /** The number of task process failure times */
  processFailCount: number;
  /** Eval workload */
  evalWorkload: number;
  /** Actual workload */
  actualWorkload: number;
  /** Completed workload */
  completedWorkload: number;
  /** Actual saving workload */
  savingWorkload: number;
  /** Rate of actual saving workload */
  savingWorkloadRate: number;
  /** Calculates progress percentage (0-100) */
  progress: string;
  /** Calculates overdue rate percentage (0-100) */
  overdueRate: number;
}

/**
 * Action menu item interface for task action buttons
 */
export type ActionMenuItem = {
  name: string;
  key: 'delete' | 'edit' | 'start' | 'processed' | 'split' | 'uncompleted' | 'completed' | 'reopen' | 'restart' | 'cancel' | 'move' | 'cancelFavourite' | 'addFavourite' | 'cancelFollow' | 'addFollow' | 'copyLink';
  icon: string;
  disabled: boolean;
  hide: boolean;
  tip?: string;
}

/**
 * Search panel option interface for filter dropdowns
 */
export type SearchPanelOption = {
  id: string;
  name: string;
  showTitle: string;
  showName: string;
}

/**
 * Search panel menu item interface for quick search options
 */
export type SearchPanelMenuItem = {
  key: 'none' | 'createdBy' | 'assigneeId' | 'progress' | 'confirmerId' | 'last1Day' | 'last3Days' | 'last7Days' | string;
  name: string;
  groupKey?: 'assigneeId' | 'time';
}
