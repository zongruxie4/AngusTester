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
      key: 'addFollow',
      name: t('actions.addFollow'),
      permission: ScenarioPermission.VIEW,
      icon: 'icon-yiguanzhu'
    },
    {
      key: 'cancelFollow',
      name: t('actions.cancelFollow'),
      permission: ScenarioPermission.VIEW,
      icon: 'icon-quxiaoguanzhu'
    },
    {
      key: 'addFavourite',
      name: t('actions.addFavourite'),
      permission: ScenarioPermission.VIEW,
      icon: 'icon-yishoucang'
    },
    {
      key: 'cancelFavourite',
      name: t('actions.cancelFavourite'),
      permission: ScenarioPermission.VIEW,
      icon: 'icon-quxiaoshoucang'
    },
    {
      key: 'auth',
      name: t('actions.permission'),
      permission: ScenarioPermission.GRANT,
      icon: 'icon-quanxian1'
    },
    {
      key: 'export',
      name: t('actions.export'),
      permission: ScenarioPermission.EXPORT,
      icon: 'icon-daochu'
    },
    {
      key: 'delete',
      name: t('actions.delete'),
      permission: ScenarioPermission.DELETE,
      icon: 'icon-qingchu'
    }
  ]);

  return {
    dropdownMenuItems
  };
}
