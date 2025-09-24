import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { TableColumn } from '../types';

/**
 * Composable for managing table column configurations
 * @returns Object containing column definitions and table settings
 */
export function useTableColumns () {
  const { t } = useI18n();

  /**
   * Table column configuration
   */
  const columns = computed<TableColumn[]>(() => [
    {
      title: t('apiTrash.table.columns.name'),
      dataIndex: 'targetName',
      key: 'targetName',
      width: '35%',
      ellipsis: true,
      sorter: false
    },
    {
      title: t('apiTrash.table.columns.createdBy'),
      dataIndex: 'createdByName',
      key: 'createdByName',
      ellipsis: true,
      sorter: false
    },
    {
      title: t('apiTrash.table.columns.deletedBy'),
      dataIndex: 'deletedByName',
      key: 'deletedByName',
      ellipsis: true,
      sorter: false
    },
    {
      title: t('apiTrash.table.columns.deletedDate'),
      dataIndex: 'deletedDate',
      key: 'deletedDate',
      ellipsis: true,
      sorter: true
    },
    {
      title: t('common.actions'),
      dataIndex: 'action',
      key: 'action',
      width: 70,
      fixed: 'right'
    }
  ]);

  /**
   * Empty state styling for table
   */
  const emptyTextStyle = {
    margin: '140px auto',
    height: 'auto'
  };

  /**
   * Default table props
   */
  const defaultTableProps = {
    size: 'small' as const,
    bordered: false,
    showHeader: true,
    tableLayout: 'auto' as const
  };

  return {
    columns,
    emptyTextStyle,
    defaultTableProps
  };
}
