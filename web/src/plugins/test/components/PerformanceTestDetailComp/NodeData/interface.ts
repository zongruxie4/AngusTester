import { useI18n } from 'vue-i18n';

/**
 * Get table columns with internationalized titles
 * Returns column definitions for the node data table
 * 
 * @returns Array of column definitions with i18n titles
 */
export const getColumns = () => {
  const { t } = useI18n();
  
  return [
    {
      title: t('commonPlugin.node.columns.name'),
      dataIndex: 'name',
      key: 'name'
    },
    {
      title: t('commonPlugin.node.columns.average'),
      dataIndex: 'average',
      key: 'average'
    },
    {
      title: t('commonPlugin.node.columns.high'),
      dataIndex: 'high',
      key: 'high'
    },
    {
      title: t('commonPlugin.node.columns.low'),
      dataIndex: 'low',
      key: 'low'
    },
    {
      title: t('commonPlugin.node.columns.latest'),
      dataIndex: 'latest',
      key: 'latest'
    }
  ];
};
