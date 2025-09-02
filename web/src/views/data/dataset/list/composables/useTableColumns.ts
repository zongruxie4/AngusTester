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
      title: t('dataset.list.columns.name'),
      dataIndex: 'name',
      ellipsis: true,
      width: '30%',
      sorter: true
    },
    {
      key: 'description',
      title: t('dataset.list.columns.description'),
      dataIndex: 'description',
      ellipsis: true,
      width: '36%'
    },
    {
      key: 'dataSource',
      title: t('dataset.list.columns.valueSource'),
      dataIndex: 'dataSource',
      ellipsis: true,
      width: '14%',
      customRender: ({ text }: { text: any }) => text?.message
    },
    {
      key: 'createdBy',
      title: t('dataset.list.columns.createdBy'),
      dataIndex: 'createdBy',
      ellipsis: true,
      width: '15%',
      sorter: true,
      customRender: ({ record }: { record: any }) => record.createdByName,
      groupName: 'person'
    },
    {
      key: 'lastModifiedBy',
      title: t('dataset.list.columns.lastModifiedBy'),
      dataIndex: 'lastModifiedBy',
      ellipsis: true,
      width: '15%',
      sorter: true,
      customRender: ({ record }: { record: any }) => record.lastModifiedByName,
      groupName: 'person',
      hide: true
    },
    {
      key: 'createdDate',
      title: t('dataset.list.columns.createdDate'),
      dataIndex: 'createdDate',
      ellipsis: true,
      width: '15%',
      sorter: true,
      groupName: 'date'
    },
    {
      key: 'lastModifiedDate',
      title: t('dataset.list.columns.lastModifiedDate'),
      dataIndex: 'lastModifiedDate',
      ellipsis: true,
      width: '15%',
      sorter: true,
      groupName: 'date',
      hide: true
    },
    {
      key: 'action',
      title: t('dataset.list.columns.action'),
      dataIndex: 'action',
      width: '180px'
    }
  ];

  return {
    columns
  };
}
