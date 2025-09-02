import { useI18n } from 'vue-i18n';

/**
 * Table columns configuration composable
 * Defines the structure and rendering of dataset list table columns
 */
export function useTableColumns () {
  const { t } = useI18n();

  /**
   * Dataset list table columns configuration
   * Each column definition includes:
   * - key: Unique identifier for the column
   * - title: Display name (internationalized)
   * - dataIndex: Property name from data source
   * - ellipsis: Whether to truncate long text
   * - width: Column width (optional)
   * - customRender: Custom rendering function (optional)
   * - sorter: Whether the column is sortable (optional)
   * - groupName: Group name for column management (optional)
   * - hide: Whether to hide the column by default (optional)
   */
  const columns = [
    {
      key: 'name',
      title: t('dataset.list.columns.name'),
      dataIndex: 'name',
      ellipsis: true,
      width: '20%'
    },
    {
      key: 'description',
      title: t('dataset.list.columns.description'),
      dataIndex: 'description',
      ellipsis: true
    },
    {
      key: 'dataSource',
      title: t('dataset.list.columns.valueSource'),
      dataIndex: 'dataSource',
      ellipsis: true,
      width: '10%',
      customRender: ({ text }: { text: any }) => text?.message
    },
    {
      key: 'lastModifiedBy',
      title: t('dataset.list.columns.lastModifiedBy'),
      dataIndex: 'lastModifiedBy',
      ellipsis: true,
      width: '10%',
      sorter: true,
      customRender: ({ record }: { record: any }) => record.lastModifiedByName,
      groupName: 'person'
    },
    {
      key: 'createdBy',
      title: t('dataset.list.columns.createdBy'),
      dataIndex: 'createdBy',
      ellipsis: true,
      width: '10%',
      sorter: true,
      customRender: ({ record }: { record: any }) => record.createdByName,
      groupName: 'person',
      hide: true
    },
    {
      key: 'lastModifiedDate',
      title: t('dataset.list.columns.lastModifiedDate'),
      dataIndex: 'lastModifiedDate',
      ellipsis: true,
      width: '13%',
      sorter: true,
      groupName: 'date'
    },
    {
      key: 'createdDate',
      title: t('dataset.list.columns.createdDate'),
      dataIndex: 'createdDate',
      ellipsis: true,
      width: '13%',
      sorter: true,
      groupName: 'date',
      hide: true
    },
    {
      key: 'action',
      title: t('dataset.list.columns.action'),
      dataIndex: 'action',
      width: 140
    }
  ];

  return {
    columns
  };
}
