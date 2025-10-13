import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import { DataType } from '@/views/data/home/types';

/**
 * <p>
 * Table columns configuration composable
 * </p>
 * <p>
 * Dynamically generates table columns based on data type with proper internationalization
 * </p>
 */
export function useAddedTableColumns (type: DataType) {
  const { t } = useI18n();

  /**
   * <p>
   * Generate table columns configuration based on data type
   * </p>
   */
  const columns = computed(() => {
    const baseColumns = [
      {
        title: t('common.id'),
        dataIndex: 'id',
        width: 200
      },
      {
        title: t('common.name'),
        dataIndex: 'name',
        ellipsis: true,
        sorter: true
      }
    ];

    // Add type-specific columns
    const typeSpecificColumns = [];

    if (['space', 'datasource'].includes(type)) {
      typeSpecificColumns.push(
        {
          title: t('common.createdBy'),
          dataIndex: 'createdByName',
          width: 200
        },
        {
          title: t('common.createdDate'),
          dataIndex: 'createdDate',
          width: 200
        }
      );
    }

    if (['variable', 'dataset'].includes(type)) {
      typeSpecificColumns.push(
        {
          title: t('common.lastModifiedBy'),
          dataIndex: 'lastModifiedByName',
          ellipsis: true,
          width: 200
        },
        {
          title: t('common.lastModifiedDate'),
          dataIndex: 'lastModifiedDate',
          width: 200
        }
      );
    }

    // Add action column
    const actionColumn = {
      title: t('common.actions'),
      dataIndex: 'action',
      width: 80
    };

    return [...baseColumns, ...typeSpecificColumns, actionColumn];
  });

  return {
    columns
  };
}
