import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { TaskTableColumn } from '../types';

/**
 * Composable for managing task trash table column configurations
 * @returns Object containing column definitions and table settings
 */
export function useTableColumns () {
  const { t } = useI18n();

  /**
   * Table column configuration for task trash
   */
  const columns = computed<TaskTableColumn[]>(() => [
    {
      title: t('taskTrash.columns.name'),
      dataIndex: 'targetName',
      key: 'targetName',
      width: '35%',
      ellipsis: true,
      sorter: false
    },
    {
      title: t('common.creator'),
      dataIndex: 'createdByName',
      key: 'createdByName',
      ellipsis: true,
      sorter: false
    },
    {
      title: t('taskTrash.columns.deleter'),
      dataIndex: 'deletedByName',
      key: 'deletedByName',
      ellipsis: true,
      sorter: false
    },
    {
      title: t('taskTrash.columns.deleteTime'),
      dataIndex: 'deletedDate',
      key: 'deletedDate',
      ellipsis: true,
      sorter: true
    },
    {
      title: t('common.actions'),
      dataIndex: 'action',
      key: 'action',
      width: 100,
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
