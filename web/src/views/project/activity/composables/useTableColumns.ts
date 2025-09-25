import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { TableColumn } from '../types';

/**
 * Composable for managing activity table column configurations
 * Provides column definitions and table settings for the activity table
 *
 * @returns Object containing column definitions and table settings
 */
export function useTableColumns () {
  const { t } = useI18n();

  /**
   * Table column configuration for activity list
   */
  const columns = computed<TableColumn[]>(() => [
    {
      key: 'fullName',
      title: t('projectActivity.table.operator'),
      dataIndex: 'fullName',
      width: '10%',
      ellipsis: true
    },
    {
      key: 'optDate',
      title: t('projectActivity.table.activityTime'),
      dataIndex: 'optDate',
      sorter: true,
      width: '10%',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      key: 'projectName',
      title: t('common.projectName'),
      dataIndex: 'projectName',
      groupName: 'project',
      width: '15%',
      ellipsis: true
    },
    {
      key: 'projectId',
      title: t('common.projectId'),
      dataIndex: 'projectId',
      groupName: 'project',
      hide: true,
      width: '15%'
    },
    {
      key: 'targetType',
      title: t('projectActivity.table.resourceType'),
      dataIndex: 'targetType',
      customRender: ({ text }) => text?.message,
      width: '8%'
    },
    {
      key: 'targetId',
      title: t('projectActivity.table.targetId'),
      dataIndex: 'targetId',
      width: '17%',
      groupName: 'source',
      hide: true,
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      key: 'targetName',
      title: t('projectActivity.table.targetName'),
      dataIndex: 'targetName',
      width: '17%',
      ellipsis: true,
      groupName: 'source'
    },
    {
      key: 'detail',
      title: t('projectActivity.table.activityContent'),
      width: '40%',
      dataIndex: 'detail'
    }
  ]);

  /**
   * Default table props for consistent configuration
   */
  const defaultTableProps = {
    rowKey: 'id',
    size: 'small' as const,
    bordered: false,
    showHeader: true,
    tableLayout: 'auto' as const,
    noDataSize: 'small' as const
  };

  return {
    columns,
    defaultTableProps
  };
}
