import { useI18n } from 'vue-i18n';

import { SortOption } from '../types';

/**
 * Composable for managing sort configuration
 *
 * @returns Object containing sort configuration functions and state
 */
export function useSortConfig () {
  const { t } = useI18n();

  /**
   * Sort options configuration for the dropdown sort component
   */
  const sortOptions: SortOption[] = [
    {
      name: t('node.nodeItem.interface.sortOptions.byAddTime'),
      key: 'createdDate',
      orderSort: 'DESC'
    },
    {
      name: t('node.nodeItem.interface.sortOptions.byName'),
      key: 'name',
      orderSort: 'ASC'
    }
  ];

  /**
   * Handles sort option selection
   *
   * @param sortEvent - Sort change event with orderBy and orderSort
   * @param onSortChange - Callback function to handle sort changes
   */
  const handleSortChange = (
    sortEvent: { orderBy: string; orderSort: string },
    onSortChange: (sortEvent: { orderBy: string; orderSort: string }) => void
  ) => {
    onSortChange(sortEvent);
  };

  return {
    // Configuration
    sortOptions,

    // Methods
    handleSortChange
  };
}
