import { useI18n } from 'vue-i18n';
import { DatabaseType } from '@xcan-angus/infra';
import { SearchOption } from '../types';

/**
 * <p>Composable for managing search configuration and pagination logic</p>
 * <p>Provides search options, pagination helpers, and search-related utilities</p>
 */
export function useSearchConfig () {
  const { t } = useI18n();

  /**
   * <p>Search options configuration for the search panel</p>
   * <p>Defines all available search fields with their types and constraints</p>
   */
  const searchOptions: SearchOption[] = [
    {
      valueKey: 'name',
      type: 'input',
      placeholder: t('common.placeholders.searchKeyword'),
      allowClear: true,
      maxlength: 100
    },
    {
      valueKey: 'database',
      type: 'select-enum',
      enumKey: DatabaseType,
      placeholder: t('datasource.searchOptions.databasePlaceholder'),
      allowClear: true
    },
    {
      valueKey: 'createdBy',
      type: 'select-user',
      allowClear: true,
      placeholder: t('common.placeholders.selectCreator')
    },
    {
      valueKey: 'createdDate',
      type: 'date-range',
      allowClear: true,
      placeholder: [
        t('common.placeholders.selectCreatedDateRange.0'),
        t('common.placeholders.selectCreatedDateRange.1')
      ]
    }
  ];

  /**
   * <p>Generate pagination total display text</p>
   * <p>Creates a formatted string showing current page and total pages</p>
   */
  const generatePaginationTotalText = (total: number, currentPage: number, pageSize: number): string => {
    const totalPages = Math.ceil(total / pageSize);
    return t('execute.pageShowTotal', {
      total,
      pageNo: currentPage,
      totalPage: totalPages
    });
  };

  /**
   * <p>Check if pagination should be shown</p>
   * <p>Determines whether pagination component should be visible based on data size</p>
   */
  const shouldShowPagination = (total: number, pageSize: number): boolean => {
    return total > pageSize;
  };

  /**
   * <p>Calculate the next page number after deletion</p>
   * <p>Determines the appropriate page to show after removing an item</p>
   */
  const calculateNextPageAfterDeletion = (
    currentPage: number,
    pageSize: number,
    total: number
  ): number => {
    const totalPages = Math.ceil(total / pageSize);
    return currentPage > totalPages ? totalPages : currentPage;
  };

  return {
    searchOptions,
    generatePaginationTotalText,
    shouldShowPagination,
    calculateNextPageAfterDeletion
  };
}
