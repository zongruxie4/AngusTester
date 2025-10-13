import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import { DataType } from '@/views/data/home/types';

/**
 * Empty state management composable
 */
export function useAddedEmptyState (type: DataType) {
  const { t } = useI18n();

  /**
   * <p>
   * Get empty state configuration based on data type
   * </p>
   */
  const emptyStateConfig = computed(() => {
    const configs = {
      variable: {
        message: t('dataHome.emptyData.noVariable'),
        actionText: t('dataHome.emptyData.addVariable'),
        actionKey: 'variable'
      },
      dataset: {
        message: t('dataHome.emptyData.noDataset'),
        actionText: t('dataHome.emptyData.addDataset'),
        actionKey: 'dataset'
      },
      space: {
        message: t('dataHome.emptyData.noSpace'),
        actionText: '',
        actionKey: null
      },
      dataSource: {
        message: t('dataHome.emptyData.noDataSource'),
        actionText: '',
        actionKey: null
      }
    };

    return configs[type];
  });

  /**
   * Check if the data type supports create action
   */
  const hasCreateAction = computed(() => {
    return ['variable', 'dataset'].includes(type);
  });

  return {
    emptyStateConfig,
    hasCreateAction
  };
}
