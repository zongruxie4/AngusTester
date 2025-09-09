import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { ScenarioPermission } from '@/enums/enums';
import { MenuItem } from '../types';

/**
 * <p>
 * Composable for managing scenario dropdown menu items.
 * <p>
 * Provides a centralized way to manage all dropdown menu items for scenario list actions.
 * @returns Object containing dropdown menu items and related utilities
 */
export function useScenarioMenuItems () {
  const { t } = useI18n();

  /**
   * <p>
   * Computed property that returns all dropdown menu items for scenario actions.
   * <p>
   * Includes follow/unfollow, favorite/unfavorite, auth, export, delete, and test task operations.
   * @returns Array of menu items with their configurations
   */
  const dropdownMenuItems = computed<readonly MenuItem[]>(() => [
    {
      key: 'follow',
      name: t('scenario.list.actions.follow'),
      permission: ScenarioPermission.VIEW,
      icon: 'icon-yiguanzhu'
    },
    {
      key: 'cancelFollow',
      name: t('scenario.list.actions.cancelFollow'),
      permission: ScenarioPermission.VIEW,
      icon: 'icon-quxiaoguanzhu'
    },
    {
      key: 'favourite',
      name: t('scenario.list.actions.favourite'),
      permission: ScenarioPermission.VIEW,
      icon: 'icon-yishoucang'
    },
    {
      key: 'cancelFavourite',
      name: t('scenario.list.actions.cancelFavourite'),
      permission: ScenarioPermission.VIEW,
      icon: 'icon-quxiaoshoucang'
    },
    {
      key: 'auth',
      name: t('scenario.list.actions.auth'),
      permission: ScenarioPermission.GRANT,
      icon: 'icon-quanxian1'
    },
    {
      key: 'export',
      name: t('scenario.list.actions.export'),
      permission: ScenarioPermission.EXPORT,
      icon: 'icon-daochu'
    },
    {
      key: 'delete',
      name: t('scenario.list.actions.delete'),
      permission: ScenarioPermission.DELETE,
      icon: 'icon-qingchu'
    },
    {
      key: 'createTestTask',
      name: t('scenario.list.actions.createTestTask'),
      permission: ScenarioPermission.TEST,
      icon: 'icon-shengchengceshirenwu1',
      tip: t('scenario.list.tooltips.createTestTask')
    },
    {
      key: 'restartTestTask',
      name: t('scenario.list.actions.restartTestTask'),
      permission: ScenarioPermission.TEST,
      icon: 'icon-zhongxinkaishiceshi',
      tip: t('scenario.list.tooltips.restartTestTask')
    },
    {
      key: 'reopenTestTask',
      name: t('scenario.list.actions.reopenTestTask'),
      permission: ScenarioPermission.TEST,
      icon: 'icon-zhongxindakaiceshirenwu',
      tip: t('scenario.list.tooltips.reopenTestTask')
    },
    {
      key: 'deleteTestTask',
      name: t('scenario.list.actions.deleteTestTask'),
      permission: ScenarioPermission.TEST,
      icon: 'icon-shanchuceshirenwu1',
      tip: t('scenario.list.tooltips.deleteTestTask')
    }
  ]);

  return {
    dropdownMenuItems
  };
}
