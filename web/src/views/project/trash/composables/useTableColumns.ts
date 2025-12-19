import { useI18n } from 'vue-i18n';

/**
 * <p>Composable for managing table column configurations and styling.</p>
 * <p>Provides consistent column definitions for the trash table.</p>
 */
export function useTableColumns () {
  const { t } = useI18n();

  /**
   * <p>Table column definitions with sorting, width, and display properties.</p>
   * <p>Each column is configured with appropriate data mapping and styling.</p>
   */
  const columns = [
    {
      key: 'targetId',
      title: 'ID',
      dataIndex: 'targetId',
      ellipsis: true,
      sorter: false
    },
    {
      key: 'targetName',
      title: t('common.name'),
      dataIndex: 'targetName',
      width: '35%',
      ellipsis: true,
      sorter: false
    },
    {
      key: 'creator',
      title: t('common.creator'),
      dataIndex: 'creator',
      ellipsis: true,
      sorter: false
    },
    {
      key: 'deletedByName',
      title: t('common.deletedBy'),
      dataIndex: 'deletedByName',
      ellipsis: true,
      sorter: false
    },
    {
      key: 'deletedDate',
      title: t('common.deletedDate'),
      dataIndex: 'deletedDate',
      ellipsis: true,
      sorter: true
    },
    {
      key: 'action',
      title: t('common.actions'),
      dataIndex: 'action',
      width: 140
    }
  ];

  /**
   * <p>Empty state styling for when no data is available.</p>
   * <p>Provides consistent visual appearance for empty tables.</p>
   */
  const emptyTextStyle = {
    margin: '140px auto',
    height: 'auto'
  };

  return {
    columns,
    emptyTextStyle
  };
}
