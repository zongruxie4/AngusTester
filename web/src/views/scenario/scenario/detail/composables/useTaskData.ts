import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { task } from '@/api/tester';
import { TaskType } from '@/enums/enums';
import type { PaginationConfig, TableColumn, TaskItem } from '../types';

/**
 * Composable for managing task data and table configuration
 */
export function useTaskData (scenarioId: string, projectId: string) {
  const { t } = useI18n();

  const pagination = ref<PaginationConfig>({
    current: 1,
    pageSize: 10,
    total: 0
  });

  const loading = ref(false);
  const taskList = ref<TaskItem[]>([]);

  /**
   * Load tasks from API
   */
  const loadTasks = async () => {
    const { current, pageSize } = pagination.value;
    loading.value = true;

    const [error, { data }] = await task.getTaskList({
      projectId,
      taskType: TaskType.SCENARIO_TEST,
      filters: [{ value: scenarioId, op: 'EQUAL', key: 'targetId' }],
      pageNo: current,
      pageSize
    });

    loading.value = false;
    if (error) {
      return;
    }

    taskList.value = data.list || [];
    pagination.value.total = +data.total || 0;
  };

  /**
   * Handle pagination change
   */
  const handlePaginationChange = (page: { current: number; pageSize: number }) => {
    pagination.value.current = page.current;
    pagination.value.pageSize = page.pageSize;
    loadTasks();
  };

  /**
   * Generate table columns configuration
   */
  const columns = computed((): TableColumn[] => [
    {
      key: 'code',
      title: t('common.code'),
      dataIndex: 'code',
      ellipsis: true,
      width: 100
    },
    {
      key: 'name',
      title: t('common.name'),
      dataIndex: 'name',
      ellipsis: true,
      width: '25%'
    },
    {
      key: 'sprintName',
      title: t('common.sprint'),
      dataIndex: 'sprintName',
      ellipsis: true,
      width: '25%'
    },
    {
      key: 'priority',
      title: t('common.priority'),
      dataIndex: 'priority',
      ellipsis: true,
      width: '9%'
    },
    {
      key: 'assigneeName',
      title: t('common.assignee'),
      dataIndex: 'assigneeName',
      width: 120
    },
    {
      key: 'confirmerName',
      title: t('common.confirmer'),
      dataIndex: 'confirmerName',
      width: 120
    },
    {
      key: 'deadlineDate',
      title: t('common.deadlineDate'),
      dataIndex: 'deadlineDate',
      ellipsis: true,
      width: '17%'
    }
  ]);

  /**
   * Empty text style configuration
   */
  const emptyTextStyle = {
    margin: '14px auto',
    height: 'auto'
  };

  return {
    pagination,
    loading,
    taskList,
    columns,
    emptyTextStyle,
    loadTasks,
    handlePaginationChange
  };
}
