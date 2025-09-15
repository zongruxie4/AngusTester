import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { appContext, enumUtils, PageQuery } from '@xcan-angus/infra';
import { analysis, task } from '@/api/tester';
import { TaskSprintPermission } from '@/enums/enums';
import { SprintInfo } from '@/views/task/sprint/types';
import { MemberCount } from '../types';
import {
  BacklogProps,
  SprintDataState,
  LoadingState,
  SearchState,
  TaskQueryParams,
  SortParams,
  PRIORITY_LEVEL_CONFIG,
  PAGE_SIZE
} from './types';

/**
 * <p>Composable for managing sprint data operations</p>
 * <p>Handles sprint data loading, task management within sprints, and member progress tracking</p>
 */
export function useSprintData (
  props: BacklogProps,
  sprintData: SprintDataState,
  loading: LoadingState,
  search: SearchState,
  getTaskParams: () => TaskQueryParams
) {
  const { t } = useI18n();
  const isAdmin = computed(() => appContext.isAdmin());

  /**
   * <p>Sort tasks by priority within a sprint</p>
   * <p>Sorts the task list for a specific sprint based on priority level</p>
   */
  const sortTaskByPriority = (sprintId: string) => {
    const taskList = sprintData.sprintTasksMap[sprintId];
    if (!taskList || !Array.isArray(taskList)) {
      return;
    }
    taskList.sort((a, b) => {
      if (sprintData.sprintSortParamsMap[sprintId].orderSort === PageQuery.OrderSort.Asc) {
        return PRIORITY_LEVEL_CONFIG[a.priority?.value] - PRIORITY_LEVEL_CONFIG[b.priority?.value];
      } else if (sprintData.sprintSortParamsMap[sprintId].orderSort === PageQuery.OrderSort.Desc) {
        return PRIORITY_LEVEL_CONFIG[b.priority?.value] - PRIORITY_LEVEL_CONFIG[a.priority?.value];
      } else {
        return 0;
      }
    });
    sprintData.sprintTasksMap[sprintId] = [...taskList];
  };

  /**
   * <p>Load task list for a specific sprint</p>
   * <p>Fetches tasks belonging to a sprint with pagination and sorting</p>
   */
  const loadTaskListById = async (id: string, pageNo: number) => {
    const params = getTaskParams();
    params.sprintId = id;
    params.pageNo = pageNo;
    Object.assign(params, (sprintData.sprintSortParamsMap[id] || {}));

    try {
      const [error, res] = await task.getTaskList(params);

      if (error) {
        return;
      }

      const data = res?.data;
      if (!data) {
        return;
      }

      sprintData.sprintTaskCountMap[id] = +data.total;

      const list = data.list || [];
      if (pageNo === 1) {
        sprintData.sprintTasksMap[id] = [...list];
      } else {
        if (sprintData.sprintTasksMap[id]) {
          sprintData.sprintTasksMap[id].push(...list);
        } else {
          sprintData.sprintTasksMap[id] = [...list];
        }
      }

      const sprintIdSet = new Set<string>();
      for (let i = 0, len = list.length; i < len; i++) {
        const item = list[i];
        sprintIdSet.add(item.sprintId);
      }

      const sprintIds = Array.from(sprintIdSet);

      if (!isAdmin.value) {
        for (let i = 0, len = sprintIds.length; i < len; i++) {
          const id = sprintIds[i];
          if (id) {
            try {
              const [_error, _res] = await loadPermissions(id);
              if (!_error) {
                const _permissions = (_res?.data || []).map(item => item.value);
                sprintData.sprintPermissionsMap.set(id, _permissions);
              }
            } catch (err) {
              console.error(`Failed to load permissions for sprint ${id}:`, err);
            }
          }
        }
      } else {
        for (let i = 0, len = sprintIds.length; i < len; i++) {
          const id = sprintIds[i];
          sprintData.sprintPermissionsMap.set(id, enumUtils.getEnumValues(TaskSprintPermission));
        }
      }

      if (sprintData.sprintTasksMap[id] && sprintData.sprintTasksMap[id].length < sprintData.sprintTaskCountMap[id]) {
        await loadTaskListById(id, pageNo + 1);
      } else {
        if (sprintData.sprintSortParamsMap[id]?.orderBy === 'priority') {
          sortTaskByPriority(id);
        }
      }
    } catch (err) {
      console.error(`Failed to load task list for sprint ${id}:`, err);
    }
  };

  /**
   * <p>Load sprint list</p>
   * <p>Fetches all sprints for the current project and loads their associated tasks</p>
   */
  const loadSprintList = async () => {
    const params = {
      projectId: props.projectId,
      pageSize: PAGE_SIZE
    };

    loading.isLoading = true;
    try {
      const [error, res] = await task.getSprintList(params);

      if (error) {
        loading.isLoading = false;
        return;
      }

      const list = (res?.data?.list || []) as SprintInfo[];
      sprintData.sprintList = [];

      for (let i = 0, len = list.length; i < len; i++) {
        const item = list[i];
        const id = item.id;
        sprintData.sprintMembersMap[id] = item.members;
        sprintData.sprintTasksMap[id] = [];

        if (item.progress?.completedRate) {
          item.progress.completedRate = item.progress.completedRate.replace(/(\d+\.\d{2})\d+/, '$1');
        }

        sprintData.sprintList.push(item);

        try {
          await loadTaskListById(id, 1);
        } catch (err) {
          console.error(`Failed to load tasks for sprint ${id}:`, err);
        }
      }
    } catch (err) {
      console.error('Failed to load sprint list:', err);
    } finally {
      loading.isLoading = false;
    }
  };

  /**
   * <p>Load user permissions for a sprint</p>
   * <p>Fetches user permissions for a specific sprint</p>
   */
  const loadPermissions = async (id: string) => {
    const params = {
      admin: true
    };

    return await task.getUserSprintAuth(id, props.userInfo?.id || '', params);
  };

  /**
   * <p>Handle sprint member hover</p>
   * <p>Loads member progress data when hovering over sprint members</p>
   */
  const handleSprintMemberHover = async (id: string) => {
    if (sprintData.sprintMemberProgressMap[id] || loading.loadingMemberProgressSprintIds.has(id)) {
      return;
    }

    const params = {
      sprintId: id,
      projectId: props.projectId
    };

    loading.loadingMemberProgressSprintIds.add(id);
    const [error, res] = await analysis.getAssigneeProgress(params);
    loading.loadingMemberProgressSprintIds.delete(id);

    if (error) {
      return;
    }

    const list = res?.data || [];
    sprintData.sprintMemberProgressMap[id] = list.reduce((prev, cur) => {
      prev[cur.assigneeId] = cur;
      return prev;
    }, {} as MemberCount);
  };

  /**
   * <p>Handle sprint task sorting</p>
   * <p>Processes sort requests for tasks within a sprint</p>
   */
  const handleSprintTaskSort = (orderData: SortParams, sprintId: string) => {
    sprintData.sprintSortParamsMap[sprintId] = orderData;
    if (orderData.orderBy === 'priority') {
      sortTaskByPriority(sprintId);
      return;
    }
    loadTaskListById(sprintId, 1);
  };

  /**
   * <p>Refresh all task data</p>
   * <p>Reloads both sprint and backlog data</p>
   */
  const refreshAllTaskData = () => {
    loadSprintList();
    // Note: This would need to be coordinated with useTaskData
  };

  /**
   * <p>Refresh all data</p>
   * <p>Reloads all sprint task lists and backlog data</p>
   */
  const refreshAllData = () => {
    const list = sprintData.sprintList;
    for (let i = 0, len = list.length; i < len; i++) {
      loadTaskListById(list[i].id, 1);
    }
    // Note: This would need to be coordinated with useTaskData
  };

  /**
   * <p>Get total task number across all sprints</p>
   * <p>Calculates the sum of tasks in all sprint containers</p>
   */
  const totalTaskNum = computed(() => {
    return sprintData.sprintList.reduce((pre, current) => {
      return pre + (sprintData.sprintTaskCountMap[current.id] || 0);
    }, 0);
  });

  return {
    sortTaskByPriority,
    loadTaskListById,
    loadSprintList,
    loadPermissions,
    handleSprintMemberHover,
    handleSprintTaskSort,
    refreshAllTaskData,
    refreshAllData,
    totalTaskNum
  };
}
