import { useI18n } from 'vue-i18n';
import { notification, modal as modalV } from '@xcan-angus/vue-ui';
import { cloneDeep } from 'lodash-es';
import { issue } from '@/api/tester';
import { TaskType } from '@/enums/enums';
import { TaskDetail } from '../../types';
import {
  BacklogProps,
  CreateTaskParams,
  MoveTaskParams,
  DragEvent,
  TaskNameEditingState,
  NewTaskState,
  CurrentInfoState,
  BacklogDataState,
  SprintDataState,
  LoadingState,
  ModalState,
  SelectedState,
  UIState,
  DEFAULT_PRIORITY
} from './types';

/**
 * <p>Composable for handling task-related business actions</p>
 * <p>Provides methods for task creation, editing, deletion, and movement operations</p>
 */
export function useTaskActions (
  props: BacklogProps,
  backlogData: BacklogDataState,
  sprintData: SprintDataState,
  currentInfo: CurrentInfoState,
  loading: LoadingState,
  modal: ModalState,
  selected: SelectedState,
  newTask: NewTaskState,
  taskNameEditing: TaskNameEditingState,
  ui: UIState,
  deleteTabPane?: (value: string[]) => void
) {
  const { t } = useI18n();

  /**
   * <p>Create a new task with the provided parameters</p>
   * <p>Handles task creation, updates the backlog list, and shows success notification</p>
   */
  const createNewTask = async () => {
    const value = newTask.newTaskName?.trim();
    if (!value) {
      return;
    }

    const params: CreateTaskParams = {
      projectId: props.projectId!,
      name: value,
      priority: newTask.newTaskPriority,
      taskType: newTask.newTaskType
    };

    loading.isLoading = true;
    const [error, res] = await issue.addTask(params);
    loading.isLoading = false;

    if (error) {
      return;
    }

    notification.success(t('actions.tips.saveSuccess'));

    // Reset form
    newTask.newTaskName = undefined;
    newTask.newTaskPriority = DEFAULT_PRIORITY;

    const id = res?.data?.id;
    if (!id) {
      return;
    }

    const dataInfo = await loadTaskInfoById(id);
    if (!dataInfo) {
      return;
    }

    backlogData.backlogList.push(dataInfo);
    backlogData.backlogTotalCount += 1;
  };

  /**
   * <p>Load task information by ID</p>
   * <p>Fetches detailed task information from the API</p>
   */
  const loadTaskInfoById = async (id: string): Promise<TaskDetail | undefined> => {
    loading.loadingTaskIds.add(id);
    const [error, res] = await issue.getTaskDetail(id);
    loading.loadingTaskIds.delete(id);

    if (error) {
      return;
    }
    return res.data ? { ...res.data } : undefined;
  };

  /**
   * <p>Assign a task to a specific sprint</p>
   * <p>Moves task from backlog to sprint and updates the data structures</p>
   */
  const assignTaskToSprint = async (toId: string, taskData: TaskDetail, index: number) => {
    if (loading.isLoading) {
      return;
    }

    const taskId = taskData.id;
    const hasFlag = sprintData.sprintTasksMap[toId].find((item) => item.id === taskId);
    if (hasFlag) {
      return;
    }

    const params: MoveTaskParams = {
      targetSprintId: toId,
      taskIds: [taskId]
    };

    loading.isLoading = true;
    await issue.moveTask(params);
    loading.isLoading = false;

    sprintData.sprintTasksMap[toId].push(taskData);
    sprintData.sprintTaskCountMap[toId] += 1;

    backlogData.backlogList.splice(index, 1);
    backlogData.backlogTotalCount -= 1;
  };

  /**
   * <p>Move a task between different sprints</p>
   * <p>Handles task movement from one sprint to another</p>
   */
  const moveTaskBetweenSprints = async (fromId: string, toId: string, taskData: TaskDetail, index: number) => {
    if (loading.isLoading) {
      return;
    }

    const taskId = taskData.id;
    const hasFlag = sprintData.sprintTasksMap[toId].find((item) => item.id === taskId);
    if (hasFlag) {
      return;
    }

    const params: MoveTaskParams = {
      targetSprintId: toId,
      taskIds: [taskId]
    };

    loading.isLoading = true;
    await issue.moveTask(params);
    loading.isLoading = false;

    sprintData.sprintTasksMap[toId].push(taskData);
    sprintData.sprintTaskCountMap[toId] += 1;

    sprintData.sprintTasksMap[fromId].splice(index, 1);
    sprintData.sprintTaskCountMap[fromId] -= 1;
  };

  /**
   * <p>Move a task back to the backlog</p>
   * <p>Removes task from sprint and adds it to the backlog</p>
   */
  const moveTaskToBacklog = async (fromId: string, taskData: TaskDetail, index: number) => {
    if (loading.isLoading) {
      return;
    }

    const taskId = taskData.id;
    const hasFlag = backlogData.backlogList.find((item) => item.id === taskId);
    if (hasFlag) {
      return;
    }

    const params: MoveTaskParams = {
      targetSprintId: null,
      taskIds: [taskId]
    };

    loading.isLoading = true;
    await issue.moveTask(params);
    loading.isLoading = false;

    backlogData.backlogTotalCount += 1;
    backlogData.backlogList.push(taskData);
    sprintData.sprintTaskCountMap[fromId] -= 1;
    sprintData.sprintTasksMap[fromId].splice(index, 1);
  };

  /**
   * <p>Handle drag and drop task addition to sprint</p>
   * <p>Processes drag events for moving tasks between containers</p>
   */
  const handleDragAddToSprint = async (event: DragEvent, sprintId: string) => {
    const taskId = event.item.id;
    const params: MoveTaskParams = {
      targetSprintId: sprintId,
      taskIds: [taskId]
    };

    loading.isLoading = true;
    await issue.moveTask(params);
    loading.isLoading = false;

    const fromId = event.from.id;
    sprintData.sprintTaskCountMap[sprintId] += 1;

    if (fromId === 'backlog') {
      backlogData.backlogTotalCount -= 1;
      return;
    }

    sprintData.sprintTaskCountMap[fromId] -= 1;
  };

  /**
   * <p>Handle drag and drop task addition to backlog</p>
   * <p>Processes drag events for moving tasks to the backlog</p>
   */
  const handleDragAddToBacklog = async (event: DragEvent) => {
    const taskId = event.item.id;
    const params: MoveTaskParams = {
      targetSprintId: null,
      taskIds: [taskId]
    };

    loading.isLoading = true;
    await issue.moveTask(params);
    loading.isLoading = false;

    const fromId = event.from.id;
    backlogData.backlogTotalCount += 1;
    sprintData.sprintTaskCountMap[fromId] -= 1;
  };

  /**
   * <p>Confirm and delete a task</p>
   * <p>Shows confirmation modal and handles task deletion</p>
   */
  const confirmDeleteTask = (data: TaskDetail, index: number, sprintId?: string) => {
    modalV.confirm({
      content: t('actions.tips.confirmDelete', { name: data.name }),
      async onOk () {
        const id = data.id;
        const [error] = await issue.deleteTask([id]);
        if (error) {
          return;
        }

        notification.success(t('actions.tips.deleteSuccess'));

        if (typeof deleteTabPane === 'function') {
          deleteTabPane([id]);
        }

        if (sprintId) {
          sprintData.sprintTasksMap[sprintId].splice(index, 1);
        } else {
          backlogData.backlogList.splice(index, 1);
        }
        loading.loadingTaskIds.delete(id);
      }
    });
  };

  /**
   * <p>Handle task name double click for editing</p>
   * <p>Enables inline editing of task names</p>
   */
  const handleTaskNameDoubleClick = (data: TaskDetail) => {
    clearTimeout(ui.nameClickTimeout);
    ui.nameClickTimeout = undefined;

    taskNameEditing.editingTaskNameIds.clear();

    const id = data.id;
    taskNameEditing.editingTaskNameIds.add(id);
    taskNameEditing.editingTaskNameMap[id] = data.name;
  };

  /**
   * <p>Handle task name edit completion</p>
   * <p>Saves the edited task name and updates the UI</p>
   */
  const handleTaskNameEditEnter = async (data: TaskDetail, index: number) => {
    const id = data.id;
    const newName = taskNameEditing.editingTaskNameMap[id];
    // loading.isLoading = true;
    const [error] = await issue.editTaskName(id, newName);
    // loading.isLoading = false;

    if (error) {
      return;
    }

    backlogData.backlogList[index].name = newName;
    if (currentInfo.currentTaskInfo?.id === id) {
      currentInfo.currentTaskInfo.name = newName;
    }

    delete taskNameEditing.editingTaskNameMap[id];
    taskNameEditing.editingTaskNameIds.delete(id);
  };

  /**
   * <p>Cancel task name editing</p>
   * <p>Reverts the editing state without saving changes</p>
   */
  const cancelTaskNameEdit = (id: string) => {
    taskNameEditing.editingTaskNameIds.delete(id);
    delete taskNameEditing.editingTaskNameMap[id];
  };

  /**
   * <p>Handle task name click with timeout</p>
   * <p>Manages single click vs double click detection</p>
   */
  const handleTaskNameClick = () => {
    if (ui.nameClickTimeout) {
      clearTimeout(ui.nameClickTimeout);
      ui.nameClickTimeout = undefined;
      return;
    }

    ui.nameClickTimeout = setTimeout(() => {
      ui.nameClickTimeout = undefined;
    }, 300) as any;
  };

  /**
   * <p>Handle new task name input enter key</p>
   * <p>Creates a new task when enter is pressed in the input field</p>
   */
  const handleNewTaskNameEnter = (event: KeyboardEvent) => {
    const target = event.target as HTMLInputElement;
    const value = target.value.trim();
    if (!value) {
      return;
    }

    createNewTask();
  };

  /**
   * <p>Show the add task form</p>
   * <p>Enables the task creation form and focuses the input</p>
   */
  const showAddTaskForm = () => {
    modal.isAddingNewTask = true;
  };

  /**
   * <p>Cancel adding a new task</p>
   * <p>Resets the form and hides the add task interface</p>
   */
  const cancelAddTask = () => {
    modal.isAddingNewTask = false;
    newTask.newTaskType = TaskType.STORY;
    newTask.newTaskName = undefined;
    newTask.newTaskPriority = DEFAULT_PRIORITY;
  };

  /**
   * <p>Open the create task modal</p>
   * <p>Shows the full task creation modal dialog</p>
   */
  const openCreateTaskModal = () => {
    modal.isEditTaskModalVisible = true;
    selected.selectedSprintId = undefined;
    selected.selectedTaskId = undefined;
  };

  /**
   * <p>Open the task edit modal</p>
   * <p>Shows the task editing modal for a specific task</p>
   */
  const openTaskEditModal = (taskId: string, sprintId?: string) => {
    modal.isEditTaskModalVisible = true;
    selected.selectedSprintId = sprintId;
    selected.selectedTaskId = taskId;
  };

  /**
   * <p>Show the split task modal</p>
   * <p>Opens the modal for splitting a task into multiple subtasks</p>
   */
  const showSplitTaskModal = (data: TaskDetail) => {
    modal.isSplitTaskModalVisible = true;
    selected.selectedTaskInfo = data;
  };

  /**
   * <p>Show the AI generate task modal</p>
   * <p>Opens the modal for AI-powered task generation</p>
   */
  const showAiGenerateTaskModal = () => {
    modal.isAiGenerateTaskModalVisible = true;
  };

  /**
   * <p>Handle task edit success</p>
   * <p>Updates the task data after successful editing</p>
   */
  const handleTaskEditSuccess = async (data: Partial<TaskDetail>) => {
    const taskId = data.id;
    if (!taskId) {
      return;
    }

    const dataInfo = await loadTaskInfoById(taskId);
    if (!dataInfo) {
      return;
    }

    if (selected.selectedTaskId) {
      const _sprintId = selected.selectedSprintId;
      if (_sprintId) {
        const index = sprintData.sprintTasksMap[_sprintId]?.findIndex(item => item.id === taskId);
        if (sprintData.sprintTasksMap[_sprintId][index]) {
          sprintData.sprintTasksMap[_sprintId][index] = cloneDeep(dataInfo);
        }
      } else {
        const index = backlogData.backlogList.findIndex(item => item.id === taskId);
        if (backlogData.backlogList[index]) {
          backlogData.backlogList[index] = cloneDeep(dataInfo);
        }
      }
      return;
    }

    backlogData.backlogList.push(dataInfo);
    newTask.newTaskName = undefined;
    newTask.newTaskPriority = DEFAULT_PRIORITY;
  };

  /**
   * <p>Handle split task success</p>
   * <p>Refreshes data after successful task splitting</p>
   */
  const handleSplitTaskSuccess = async () => {
    selected.selectedTaskInfo = undefined;
    // Note: This would need to be implemented with proper data refresh
  };

  /**
   * <p>Handle split task cancel</p>
   * <p>Cleans up state when split task is cancelled</p>
   */
  const handleSplitTaskCancel = () => {
    selected.selectedTaskInfo = undefined;
  };

  /**
   * <p>Handle AI generate task success</p>
   * <p>Refreshes data after successful AI task generation</p>
   */
  const handleAiGenerateTaskSuccess = async () => {
    selected.selectedTaskInfo = undefined;
    // Note: This would need to be implemented with proper data refresh
  };

  /**
   * <p>Handle AI generate task cancel</p>
   * <p>Cleans up state when AI generation is cancelled</p>
   */
  const handleAiGenerateTaskCancel = () => {
    selected.selectedTaskInfo = undefined;
  };

  return {
    createNewTask,
    loadTaskInfoById,
    assignTaskToSprint,
    moveTaskBetweenSprints,
    moveTaskToBacklog,
    handleDragAddToSprint,
    handleDragAddToBacklog,
    confirmDeleteTask,
    handleTaskNameDoubleClick,
    handleTaskNameEditEnter,
    cancelTaskNameEdit,
    handleTaskNameClick,
    handleNewTaskNameEnter,
    showAddTaskForm,
    cancelAddTask,
    openCreateTaskModal,
    openTaskEditModal,
    showSplitTaskModal,
    showAiGenerateTaskModal,
    handleTaskEditSuccess,
    handleSplitTaskSuccess,
    handleSplitTaskCancel,
    handleAiGenerateTaskSuccess,
    handleAiGenerateTaskCancel
  };
}
