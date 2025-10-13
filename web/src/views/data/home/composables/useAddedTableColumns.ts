import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import { DataType } from '@/views/data/home/types';

/**
 * Table columns configuration composable
 */
export function useAddedTableColumns (type: DataType) {
  const { t } = useI18n();

  /**
   * Generate table columns configuration based on data type
   */
  const columns = computed(() => {
    const baseColumns = [
      {
        key: 'id',
        title: t('common.id'),
        dataIndex: 'id',
        width: 200
      },
      {
        key: 'name',
        title: t('common.name'),
        dataIndex: 'name',
        ellipsis: true,
        sorter: true
      }
    ];

    // Add type-specific columns
    const typeSpecificColumns: Array<{
      key: string;
      title: string;
      dataIndex: string;
      width?: number | string;
      ellipsis?: boolean;
      sorter?: boolean;
    }> = [];

    if (['space', 'datasource'].includes(type)) {
      typeSpecificColumns.push(
        {
          key: 'createdByName',
          title: t('common.createdBy'),
          dataIndex: 'createdByName',
          width: 200
        },
        {
          key: 'createdDate',
          title: t('common.createdDate'),
          dataIndex: 'createdDate',
          width: 200
        }
      );
    }

    if (['variable', 'dataset'].includes(type)) {
      typeSpecificColumns.push(
        {
          key: 'lastModifiedByName',
          title: t('common.lastModifiedBy'),
          dataIndex: 'lastModifiedByName',
          ellipsis: true,
          width: 200
        },
        {
          key: 'lastModifiedDate',
          title: t('common.lastModifiedDate'),
          dataIndex: 'lastModifiedDate',
          width: 200
        }
      );
    }

    // Add action column
    const actionColumn = {
      key: 'action',
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
