import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { TaskSprintPermission } from '@/enums/enums';
import { SortOption, DrawerActiveKey, UIState, SprintExpansionState, CurrentInfoState, SprintDataState } from './types';

/**
 * <p>Composable for managing UI-related options and configurations</p>
 * <p>Provides sort options, drawer management, and UI state handling</p>
 */
export function useUIOptions (
  ui: UIState,
  sprintExpansion: SprintExpansionState,
  currentInfo: CurrentInfoState,
  sprintData: SprintDataState
) {
  const { t } = useI18n();

  /**
   * <p>Sort options for task lists</p>
   * <p>Defines available sorting criteria for tasks</p>
   */
  const sortOption: SortOption[] = [
    {
      name: t('common.type'),
      key: 'taskType'
    },
    {
      name: t('common.priority'),
      key: 'priority'
    },
    {
      name: t('common.code'),
      key: 'code'
    },
    {
      name: t('common.name'),
      key: 'name'
    },
    {
      name: t('common.assignee'),
      key: 'assigneeId'
    }
  ];

  /**
   * <p>Backlog sort options (excluding assignee)</p>
   * <p>Filtered sort options specifically for backlog tasks</p>
   */
  const backlogSortOption = sortOption.filter(i => !['assigneeId'].includes(i.key));

  /**
   * <p>Handle drawer active key change</p>
   * <p>Updates the active drawer tab when user clicks on different tabs</p>
   */
  const drawerActiveKeyChange = (key: DrawerActiveKey) => {
    ui.drawerActiveKey = key;
  };

  /**
   * <p>Handle popover visibility change</p>
   * <p>Manages popover state for task action menus</p>
   */
  const handlePopoverVisibilityChange = (visible: boolean, id: string) => {
    if (visible) {
      ui.popoverId = id;
      return;
    }
    ui.popoverId = undefined;
  };

  /**
   * <p>Toggle sprint expansion state</p>
   * <p>Handles expanding/collapsing of sprint containers</p>
   */
  const toggleSprintExpansion = (id: string) => {
    let isOpen = false;
    if (sprintExpansion.expandedSprintIds.has(id)) {
      sprintExpansion.expandedSprintIds.delete(id);
      isOpen = false;
    } else {
      sprintExpansion.expandedSprintIds.add(id);
      isOpen = true;
    }

    handleSprintExpansionChange(isOpen, id);
  };

  /**
   * <p>Handle sprint expansion change</p>
   * <p>Updates sprint expansion state based on user interaction</p>
   */
  const handleSprintExpansionChange = (isOpen: boolean, id: string) => {
    if (isOpen) {
      sprintExpansion.expandedSprintIds.add(id);
      return;
    }

    sprintExpansion.expandedSprintIds.delete(id);
  };

  /**
   * <p>Close task drawer</p>
   * <p>Clears current task and sprint info when drawer is closed</p>
   */
  const closeTaskDrawer = () => {
    currentInfo.currentSprintInfo = undefined;
    currentInfo.currentTaskInfo = undefined;
  };

  /**
   * <p>Get current task type</p>
   * <p>Returns the task type of the currently selected task</p>
   */
  const taskType = computed(() => {
    return currentInfo.currentTaskInfo?.taskType?.value;
  });

  /**
   * <p>Get current task ID</p>
   * <p>Returns the ID of the currently selected task</p>
   */
  const taskId = computed(() => {
    return currentInfo.currentTaskInfo?.id;
  });

  /**
   * <p>Check if user has permission for specific task action</p>
   * <p>Validates user permissions for task operations</p>
   */
  const hasPermission = (data: any, key: 'edit' | 'delete' | 'move' | 'split') => {
    const sprintId = data.sprintId;
    const sprintAuth = data.sprintAuth;

    const permissions = sprintData.sprintPermissionsMap.get(sprintId) || [];
    const { currentAssociateType } = data;

    const isAdministrator = !!currentAssociateType?.map(item => item.value).includes('SYS_ADMIN') ||
      !!currentAssociateType?.map(item => item.value).includes('APP_ADMIN');

    if (key === 'edit' || key === 'move' || key === 'split') {
      return isAdministrator || permissions.includes(TaskSprintPermission.MODIFY_TASK) || !sprintAuth;
    }

    if (key === 'delete') {
      return isAdministrator || permissions.includes(TaskSprintPermission.DELETE_TASK) || !sprintAuth;
    }
  };

  /**
   * <p>Get drawer action items configuration</p>
   * <p>Defines the available drawer action items with their properties</p>
   */
  const drawerActionItems = computed(() => [
    {
      key: 'basic' as DrawerActiveKey,
      icon: 'icon-wendangxinxi',
      title: t('common.basicInfo')
    },
    {
      key: 'person' as DrawerActiveKey,
      icon: 'icon-quanburenyuan',
      title: t('backlog.main.drawerTitles.personnel')
    },
    {
      key: 'date' as DrawerActiveKey,
      icon: 'icon-riqi',
      title: t('backlog.main.drawerTitles.date')
    },
    {
      key: 'tasks' as DrawerActiveKey,
      icon: 'icon-ceshirenwu',
      title: t('backlog.main.drawerTitles.relatedTasks')
    },
    {
      key: 'cases' as DrawerActiveKey,
      icon: 'icon-ceshiyongli1',
      title: t('backlog.main.drawerTitles.relatedCases')
    },
    {
      key: 'attachments' as DrawerActiveKey,
      icon: 'icon-lianjie1',
      title: t('common.attachments')
    },
    {
      key: 'remarks' as DrawerActiveKey,
      icon: 'icon-shuxie',
      title: t('backlog.main.drawerTitles.remarks')
    },
    {
      key: 'comment' as DrawerActiveKey,
      icon: 'icon-pinglun',
      title: t('common.comments')
    },
    {
      key: 'activity' as DrawerActiveKey,
      icon: 'icon-chakanhuodong',
      title: t('backlog.main.drawerTitles.activity')
    }
  ]);

  /**
   * <p>Get quick search filter options</p>
   * <p>Defines the available quick search filter buttons</p>
   */
  const quickSearchFilters = computed(() => [
    {
      key: 'all',
      label: t('common.all'),
      isActive: false
    },
    {
      key: 'createdByMe',
      label: t('quickSearch.createdByMe'),
      isActive: false
    },
    {
      key: 'assignedToMe',
      label: t('backlog.quickSearch.assignedToMe'),
      isActive: false
    },
    {
      key: 'lastDay',
      label: t('quickSearch.last1Day'),
      isActive: false
    },
    {
      key: 'last3Day',
      label: t('backlog.quickSearch.last3Day'),
      isActive: false
    },
    {
      key: 'last7Day',
      label: t('backlog.quickSearch.last7Day'),
      isActive: false
    }
  ]);

  return {
    sortOption,
    backlogSortOption,
    drawerActiveKeyChange,
    handlePopoverVisibilityChange,
    toggleSprintExpansion,
    handleSprintExpansionChange,
    closeTaskDrawer,
    taskType,
    taskId,
    hasPermission,
    drawerActionItems,
    quickSearchFilters
  };
}
