import { useI18n } from 'vue-i18n';
import { ReportPermission } from '@/enums/enums';
import type { ActionItem, TableColumn, UseTableColumnsReturn } from '../types';

/**
 * Composable for managing table columns and action configurations
 * Provides column definitions and action items for the report table
 */
export function useTableColumns (): UseTableColumnsReturn {
  const { t } = useI18n();

  // Action items configuration
  const actionItems: ActionItem[] = [
    {
      name: t('reportHome.actions.generateNow'),
      key: 'generate',
      icon: 'icon-guanlijiedian',
      permission: ReportPermission.GENERATE
    },
    {
      name: t('actions.permission'),
      key: 'auth',
      icon: 'icon-quanxian1',
      permission: ReportPermission.GRANT
    },
    {
      name: t('actions.delete'),
      key: 'delete',
      icon: 'icon-qingchu',
      permission: ReportPermission.DELETE
    }
  ];

  // Table columns configuration
  const columns: TableColumn[] = [
    {
      key: 'name',
      dataIndex: 'name',
      title: t('reportHome.table.name'),
      sorter: true
    },
    {
      key: 'version',
      dataIndex: 'version',
      title: t('reportHome.table.version'),
      width: 70,
      sorter: true
    },
    {
      key: 'template',
      dataIndex: 'template',
      title: t('reportHome.table.template'),
      customRender: ({ text }) => {
        return text?.message;
      },
      width: 160
    },
    {
      key: 'status',
      dataIndex: 'status',
      title: t('reportHome.table.status'),
      customRender: ({ text }) => {
        return text?.message;
      },
      width: 80
    },
    {
      key: 'targetName',
      dataIndex: 'targetName',
      title: t('reportHome.table.resourceName'),
      groupName: 'resource',
      hide: false
    },
    {
      key: 'targetType',
      dataIndex: 'targetType',
      title: t('reportHome.table.resourceType'),
      groupName: 'resource',
      hide: true,
      customRender: ({ text }) => {
        return text?.message;
      }
    },
    {
      key: 'targetId',
      dataIndex: 'targetId',
      title: t('reportHome.table.resourceId'),
      groupName: 'resource',
      hide: true
    },
    {
      key: 'description',
      dataIndex: 'description',
      title: t('common.description'),
      ellipsis: true
    },
    {
      key: 'createdBy',
      dataIndex: 'createdBy',
      title: t('reportHome.table.creator'),
      groupName: 'creat',
      hide: false,
      width: 90,
      sorter: true,
      customRender: ({ record }) => record.createdByName
    },
    {
      key: 'createdDate',
      dataIndex: 'createdDate',
      title: t('reportHome.table.createTime'),
      groupName: 'creat',
      hide: true,
      width: 160,
      sorter: true
    },
    {
      key: 'lastModifiedBy',
      dataIndex: 'lastModifiedBy',
      title: t('reportHome.table.lastModifier'),
      groupName: 'creat',
      hide: true,
      width: 130,
      sorter: true,
      customRender: ({ record }) => record.lastModifiedByName
    },
    {
      key: 'lastModifiedDate',
      dataIndex: 'lastModifiedDate',
      title: t('reportHome.table.lastModifyTime'),
      groupName: 'creat',
      hide: true,
      width: 160,
      sorter: true
    },
    {
      key: 'nextGenerationDate',
      dataIndex: 'nextGenerationDate',
      title: t('reportHome.table.nextGenerateTime'),
      groupName: 'creat',
      hide: true,
      width: 160,
      sorter: true
    },
    {
      key: 'action',
      dataIndex: 'action',
      title: t('common.actions'),
      width: 160
    }
  ];

  return {
    columns,
    actionItems
  };
}
