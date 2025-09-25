import { useI18n } from 'vue-i18n';
import type { SortMenuItem, SortType } from '../../components/type';

/**
 * Composable for managing file sorting functionality
 * Provides sort menu items and handles sort selection
 *
 * @returns Object containing sort menu items and handler methods
 */
export function useSort () {
  const { t } = useI18n();

  /**
   * Sort menu items configuration
   */
  const sortMenuItems: SortMenuItem[] = [
    {
      key: 'lastModifiedDate',
      name: t('common.lastModifiedDate'),
      orderSort: 'ASC'
    },
    {
      key: 'type',
      name: t('common.type'),
      orderSort: 'ASC'
    }
  ];

  /**
   * Handle sort selection
   *
   * @param sortData - Selected sort data
   */
  const handleSortSelection = (sortData: { orderBy: string; orderSort: string }): SortType => {
    return {
      orderSort: sortData.orderSort,
      orderBy: sortData.orderBy
    };
  };

  return {
    sortMenuItems,
    handleSortSelection
  };
}
