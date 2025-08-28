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
      title: t('projectTrash.table.name'),
      dataIndex: 'targetName',
      width: '35%',
      ellipsis: true,
      sorter: false
    },
    {
      key: 'createdByName',
      title: t('projectTrash.table.creator'),
      dataIndex: 'createdByName',
      ellipsis: true,
      sorter: false
    },
    {
      key: 'deletedByName',
      title: t('projectTrash.table.deleter'),
      dataIndex: 'deletedByName',
      ellipsis: true,
      sorter: false
    },
    {
      key: 'deletedDate',
      title: t('projectTrash.table.deleteTime'),
      dataIndex: 'deletedDate',
      ellipsis: true,
      sorter: true
    },
    {
      key: 'action',
      title: t('projectTrash.table.actions'),
      dataIndex: 'action',
      width: 70
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
