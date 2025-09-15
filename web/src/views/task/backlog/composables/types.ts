import { PageQuery, Priority, SearchCriteria } from '@xcan-angus/infra';
import { TaskSprintPermission, TaskType } from '@/enums/enums';
import { TaskInfo } from '../../types';
import { SprintInfo } from '@/views/task/sprint/types';
import { MemberCount } from '../types';

/**
 * <p>Component props interface for backlog management</p>
 */
export interface BacklogProps {
  projectId?: string;
  userInfo?: any;
  appInfo?: any;
}

/**
 * <p>Sort option configuration for task lists</p>
 */
export interface SortOption {
  name: string;
  key: string;
}

/**
 * <p>Sort parameters for task ordering</p>
 */
export interface SortParams {
  orderSort?: PageQuery.OrderSort;
  orderBy?: string;
}

/**
 * <p>Task creation parameters</p>
 */
export interface CreateTaskParams {
  projectId: string;
  name: string;
  priority: Priority;
  taskType: string;
}

/**
 * <p>Task move parameters</p>
 */
export interface MoveTaskParams {
  targetSprintId: string | null;
  taskIds: string[];
}

/**
 * <p>Task query parameters</p>
 */
export interface TaskQueryParams {
  projectId: string;
  filters: SearchCriteria[];
  pageSize: number;
  sprintId?: string;
  pageNo?: number;
  backlog?: boolean;
  orderBy?: string;
  orderSort?: PageQuery.OrderSort;
}

/**
 * <p>Drawer active key type</p>
 */
export type DrawerActiveKey = 'basic' | 'person' | 'date' | 'comment' | 'activity' | 'tasks' | 'cases' | 'attachments' | 'remarks';

/**
 * <p>Quick date filter options</p>
 */
export type QuickDateFilter = '1' | '3' | '7';

/**
 * <p>Drag event interface for task movement</p>
 */
export interface DragEvent {
  item: { id: string };
  from: { id: string };
}

/**
 * <p>State interface for task name editing</p>
 */
export interface TaskNameEditingState {
  editingTaskNameIds: Set<string>;
  editingTaskNameMap: { [key: string]: string };
}

/**
 * <p>State interface for sprint expansion</p>
 */
export interface SprintExpansionState {
  expandedSprintIds: Set<string>;
}

/**
 * <p>State interface for loading states</p>
 */
export interface LoadingState {
  isLoading: boolean;
  loadingTaskIds: Set<string>;
  loadingMemberProgressSprintIds: Set<string>;
}

/**
 * <p>State interface for modal visibility</p>
 */
export interface ModalState {
  isEditTaskModalVisible: boolean;
  isSplitTaskModalVisible: boolean;
  isAiGenerateTaskModalVisible: boolean;
  isAddingNewTask: boolean;
}

/**
 * <p>State interface for selected items</p>
 */
export interface SelectedState {
  selectedSprintId?: string;
  selectedTaskId?: string;
  selectedTaskInfo?: TaskInfo;
}

/**
 * <p>State interface for new task creation</p>
 */
export interface NewTaskState {
  newTaskType: string;
  newTaskName?: string;
  newTaskPriority: Priority;
}

/**
 * <p>State interface for search and filters</p>
 */
export interface SearchState {
  searchValue?: string;
  createdBy?: string;
  assigneeId?: string;
  quickDate?: QuickDateFilter;
}

/**
 * <p>State interface for sprint data management</p>
 */
export interface SprintDataState {
  sprintList: SprintInfo[];
  sprintMembersMap: { [key: string]: SprintInfo['members'] };
  sprintMemberProgressMap: { [key: string]: { [key: string]: MemberCount } };
  sprintTasksMap: { [key: string]: TaskInfo[] };
  sprintSortParamsMap: { [key: string]: SortParams };
  sprintPermissionsMap: Map<string, TaskSprintPermission[]>;
  sprintTaskCountMap: { [key: string]: number };
}

/**
 * <p>State interface for backlog data management</p>
 */
export interface BacklogDataState {
  backlogList: TaskInfo[];
  isBacklogDataLoaded: boolean;
  backlogSortParams?: SortParams;
  backlogTotalCount: number;
}

/**
 * <p>State interface for current task and sprint info</p>
 */
export interface CurrentInfoState {
  currentSprintInfo?: SprintInfo;
  currentTaskInfo?: TaskInfo;
}

/**
 * <p>State interface for UI interactions</p>
 */
export interface UIState {
  popoverId?: string;
  drawerActiveKey: DrawerActiveKey;
  nameClickTimeout?: number;
}

/**
 * <p>Priority level configuration for sorting</p>
 */
export const PRIORITY_LEVEL_CONFIG = {
  HIGHEST: 5,
  HIGH: 4,
  MEDIUM: 3,
  LOW: 2,
  LOWEST: 1
} as const;

/**
 * <p>Default page size for task queries</p>
 */
export const PAGE_SIZE = 500;

/**
 * <p>Default task type for new tasks</p>
 */
export const DEFAULT_TASK_TYPE = TaskType.TASK;

/**
 * <p>Default priority for new tasks</p>
 */
export const DEFAULT_PRIORITY = Priority.MEDIUM;
