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
      title: t('activity.list.columns.operator'),
      dataIndex: 'fullName',
      width: '12%',
      ellipsis: true
    },
    {
      key: 'optDate',
      title: t('activity.list.columns.activityTime'),
      dataIndex: 'optDate',
      sorter: true,
      width: '12%',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      key: 'projectName',
      title: t('common.projectName'),
      dataIndex: 'projectName',
      groupName: 'project',
      width: '18%',
      ellipsis: true
    },
    {
      key: 'projectId',
      title: t('common.projectId'),
      dataIndex: 'projectId',
      groupName: 'project',
      hide: true,
      width: '18%'
    },
    {
      key: 'targetType',
      title: t('activity.list.columns.resourceType'),
      dataIndex: 'targetType',
      customRender: ({ text }) => text?.message,
      width: '10%'
    },
    {
      key: 'targetId',
      title: t('activity.list.columns.targetId'),
      dataIndex: 'targetId',
      width: '18%',
      groupName: 'source',
      hide: true,
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      key: 'targetName',
      title: t('activity.list.columns.targetName'),
      dataIndex: 'targetName',
      width: '18%',
      ellipsis: true,
      groupName: 'source'
    },
    {
      key: 'detail',
      title: t('activity.list.columns.activityContent'),
      width: '30%',
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
