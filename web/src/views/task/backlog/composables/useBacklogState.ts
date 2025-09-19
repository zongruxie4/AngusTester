import { ref, reactive } from 'vue';
import { TaskDetail } from '../../types';
import { SprintInfo } from '@/views/task/sprint/types';
import {
  BacklogDataState,
  SprintDataState,
  LoadingState,
  ModalState,
  SelectedState,
  NewTaskState,
  SearchState,
  TaskNameEditingState,
  SprintExpansionState,
  CurrentInfoState,
  UIState,
  DrawerActiveKey,
  DEFAULT_TASK_TYPE,
  DEFAULT_PRIORITY
} from './types';

/**
 * <p>Composable for managing the overall state of the backlog component</p>
 * <p>Centralizes all reactive state management and provides state initialization</p>
 */
export function useBacklogState () {
  // Backlog data state
  const backlogData: BacklogDataState = reactive({
    backlogList: [],
    isBacklogDataLoaded: false,
    backlogSortParams: undefined,
    backlogTotalCount: 0
  });

  // Sprint data state
  const sprintData: SprintDataState = reactive({
    sprintList: [],
    sprintMembersMap: {},
    sprintMemberProgressMap: {},
    sprintTasksMap: {},
    sprintSortParamsMap: {},
    sprintPermissionsMap: new Map(),
    sprintTaskCountMap: {}
  });

  // Loading state
  const loading: LoadingState = reactive({
    isLoading: false,
    loadingTaskIds: new Set(),
    loadingMemberProgressSprintIds: new Set()
  });

  // Modal state
  const modal: ModalState = reactive({
    isEditTaskModalVisible: false,
    isSplitTaskModalVisible: false,
    isAiGenerateTaskModalVisible: false,
    isAddingNewTask: false
  });

  // Selected state
  const selected: SelectedState = reactive({
    selectedSprintId: undefined,
    selectedTaskId: undefined,
    selectedTaskInfo: undefined
  });

  // New task state
  const newTask: NewTaskState = reactive({
    newTaskType: DEFAULT_TASK_TYPE,
    newTaskName: undefined,
    newTaskPriority: DEFAULT_PRIORITY
  });

  // Search state
  const search: SearchState = reactive({
    searchValue: undefined,
    createdBy: undefined,
    assigneeId: undefined,
    quickDate: undefined
  });

  // Task name editing state
  const taskNameEditing: TaskNameEditingState = reactive({
    editingTaskNameIds: new Set(),
    editingTaskNameMap: {}
  });

  // Sprint expansion state
  const sprintExpansion: SprintExpansionState = reactive({
    expandedSprintIds: new Set()
  });

  // Current info state
  const currentInfo: CurrentInfoState = reactive({
    currentSprintInfo: undefined,
    currentTaskInfo: undefined
  });

  // UI state
  const ui: UIState = reactive({
    popoverId: undefined,
    drawerActiveKey: 'basic' as DrawerActiveKey,
    nameClickTimeout: undefined
  });

  // Template refs
  const sprintContainerRef = ref();
  const newTaskNameInputRef = ref();

  /**
   * <p>Update current task info</p>
   * <p>Updates the current task information with partial data</p>
   */
  const updateCurrentTaskInfo = async (data: Partial<TaskDetail>) => {
    if (currentInfo.currentTaskInfo) {
      currentInfo.currentTaskInfo = { ...currentInfo.currentTaskInfo, ...data };
    }

    const currentIndex = backlogData.backlogList.findIndex(i => i.id === data.id)
    if (currentIndex > -1) {
      backlogData.backlogList[currentIndex] = currentInfo.currentTaskInfo;
    }

    if (currentInfo.currentTaskInfo?.sprintId) {
      const list = sprintData.sprintTasksMap[currentInfo.currentTaskInfo?.sprintId];
      const currentIndex = list.findIndex(i => i.id === data.id)
      if (currentIndex > -1) {
        sprintData.sprintTasksMap[currentInfo.currentTaskInfo?.sprintId][currentIndex] = currentInfo.currentTaskInfo;
      }
    }
  };

  /**
   * <p>Set loading state</p>
   * <p>Updates the global loading state</p>
   */
  const setLoadingState = (value: boolean) => {
    // loading.isLoading = value;
  };

  /**
   * <p>Select a task</p>
   * <p>Sets the current task and sprint information</p>
   */
  const selectTask = async (id: string, data?: SprintInfo, loadTaskInfoById?: (id: string) => Promise<TaskDetail | undefined>) => {
    currentInfo.currentSprintInfo = data;
    if (currentInfo.currentTaskInfo?.id === id) {
      return;
    }
    if (loading.loadingTaskIds.has(id)) {
      return;
    }
    if (loadTaskInfoById) {
      currentInfo.currentTaskInfo = await loadTaskInfoById(id);
    }
  };

  return {
    // State objects
    backlogData,
    sprintData,
    loading,
    modal,
    selected,
    newTask,
    search,
    taskNameEditing,
    sprintExpansion,
    currentInfo,
    ui,

    // Template refs
    sprintContainerRef,
    newTaskNameInputRef,

    // Methods
    updateCurrentTaskInfo,
    setLoadingState,
    selectTask
  };
}
