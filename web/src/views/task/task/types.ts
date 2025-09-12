import { EnumMessage, EvalWorkloadMethod, Priority } from '@xcan-angus/infra';
import { ExecResult, TaskStatus, TaskType, TestType } from '@/enums/enums';

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
 * Task information interface containing all task-related data
 */
export type TaskInfo = {
  id: string;
  name: string;
  code: string;
  projectId: string;
  sprintId: string;
  sprintName: string;
  targetId: string;
  targetParentId: string;
  taskType: EnumMessage<TaskType>;
  testType: EnumMessage<TestType>;
  deadlineDate: string;
  assigneeId: string;
  assigneeName: string;
  priority: EnumMessage<Priority>;
  evalWorkloadMethod: EnumMessage<EvalWorkloadMethod>;
  status: EnumMessage<TaskStatus>;
  execResult: EnumMessage<ExecResult>;
  execFailureMessage: string;
  execTestNum: string;
  execTestFailureNum: string;
  execId: string;
  execName: string;
  execBy: string;
  execByName: string;
  execDate: string;
  failNum: string;
  totalNum: string;
  overdue: false;
  createdBy: string;
  createdByName: string;
  createdDate: string;
}

/**
 * Statistics information interface for task metrics and counts
 */
export type StatisticsInfo = {
  actualWorkload: string;
  apiTestNum: string;
  bugNum: string;
  canceledNum: string;
  completedNum: string;
  completedWorkload: string;
  confirmingNum: string;
  evalWorkload: string;
  functionalNum: string;
  inProgressNum: string;
  oneTimePassedNum: string;
  oneTimePassedRate: string;
  overdueNum: string;
  pendingNum: string;
  perfNum: string;
  processFailTimes: string;
  processTimes: string;
  requirementNum: string;
  scenarioTestNum: string;
  stabilityNum: string;
  storyNum: string;
  taskNum: string;
  testFailNum: string;
  testSuccessNum: string;
  totalStatusNum: string;
  totalTaskNum: string;
  totalTaskTypeNum: string;
  totalTestTypeNum: string;
  validTaskNum: string;
  progress: string;
}

/**
 * Action menu item interface for task action buttons
 */
export type ActionMenuItem = {
  name: string;
  key: 'delete' | 'edit' | 'start' | 'processed' | 'uncompleted' | 'completed' | 'reopen' | 'restart' | 'cancel' | 'move' | 'cancelFavourite' | 'favourite' | 'cancelFollow' | 'follow' | 'copyLink';
  icon: string;
  disabled: boolean;
  hide: boolean;
  tip?: string;
}

/**
 * Tree data structure interface for hierarchical task display
 */
export type TreeData = {
  name: string;
  sequence: string;
  level: number;
  children?: TreeData[];
  id: string;
  pid?: string;
  index: number;
  ids: string[];
  isLast: boolean;
  childLevels?: number;
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
  key: 'none' | 'createdBy' | 'assigneeId' | 'progress' | 'confirmerId' | 'lastDay' | 'lastThreeDays' | 'lastWeek' | string;
  name: string;
  groupKey?: 'assigneeId' | 'time';
}

/**
 * Traverses tree data structure and adds metadata for rendering
 * @param treeData - Array of tree data items to process
 * @param callback - Optional callback function to transform each item
 * @returns Processed tree data with added metadata
 */
export const travelTreeData = (treeData: TreeData[], callback = (item: TreeData) => item): TreeData[] => {
  /**
   * Recursive function to traverse tree data and add metadata
   * @param treeData - Current level of tree data
   * @param level - Current depth level
   * @param ids - Array of parent IDs for current path
   */
  function travel (treeData: TreeData[], level = 0, ids: string[] = []) {
    treeData.forEach((item, idx) => {
      item.level = level;
      item.index = idx;
      item.ids = [...ids, item.id];
      item.isLast = idx === (treeData.length - 1);

      // Recursively process children
      travel(item.children || [], level + 1, item.ids);

      // Calculate child levels for rendering purposes
      item.childLevels = (item.children?.length ? Math.max(...item.children.map(i => i.childLevels || 0)) : 0) + 1;
      item = callback(item);
    });
  }

  travel(treeData);
  return treeData;
};
