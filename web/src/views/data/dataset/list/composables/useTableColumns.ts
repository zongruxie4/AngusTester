import { useI18n } from 'vue-i18n';

/**
 * Table columns configuration composable
 * Defines the structure and rendering of dataset list table columns
 */
export function useTableColumns () {
  const { t } = useI18n();

  const columns = [
    {
      key: 'name',
      title: t('common.name'),
      dataIndex: 'name',
      ellipsis: true,
      width: '30%',
      sorter: true
    },
    {
      key: 'description',
      title: t('common.description'),
      dataIndex: 'description',
      ellipsis: true,
      width: '36%'
    },
    {
      key: 'dataSource',
      title: t('common.source'),
      dataIndex: 'dataSource',
      ellipsis: true,
      width: '14%',
      customRender: ({ text }: { text: any }) => text?.message
    },
    {
      key: 'createdBy',
      title: t('common.createdBy'),
      dataIndex: 'createdBy',
      ellipsis: true,
      width: '15%',
      sorter: true,
      customRender: ({ record }: { record: any }) => record.creator,
      groupName: 'person'
    },
    {
      key: 'modifiedBy',
      title: t('common.modifiedBy'),
      dataIndex: 'modifiedBy',
      ellipsis: true,
      width: '15%',
      sorter: true,
      customRender: ({ record }: { record: any }) => record.modifier,
      groupName: 'person',
      hide: true
    },
    {
      key: 'createdDate',
      title: t('common.createdDate'),
      dataIndex: 'createdDate',
      ellipsis: true,
      width: '15%',
      sorter: true,
      groupName: 'date'
    },
    {
      key: 'modifiedDate',
      title: t('common.modifiedDate'),
      dataIndex: 'modifiedDate',
      ellipsis: true,
      width: '15%',
      sorter: true,
      groupName: 'date',
      hide: true
    },
    {
      key: 'action',
      title: t('common.actions'),
      dataIndex: 'action',
      width: '180px'
    }
  ];

  return {
    columns
  };
}
