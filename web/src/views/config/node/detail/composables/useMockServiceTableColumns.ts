import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { TableColumn } from '../types';

/**
 * <p>Composable for managing table column configurations</p>
 * <p>Provides reactive table column definitions with internationalization support</p>
 *
 * @returns Object containing table column configuration
 */
export function useMockServiceTableColumns () {
  const { t } = useI18n();

  /**
   * <p>Table column definitions for mock service display</p>
   * <p>Defines the structure and labels for each column in the mock service table</p>
   */
  const columns = computed<TableColumn[]>(() => [
    {
      dataIndex: 'id',
      title: t('node.nodeDetail.mockService.columns.serviceId'),
      width: 120
    },
    {
      dataIndex: 'name',
      title: t('common.name'),
      width: 150
    },
    {
      dataIndex: 'status',
      title: t('common.status'),
      width: 100
    },
    {
      dataIndex: 'serviceHostUrl',
      title: t('node.nodeDetail.mockService.columns.accessUrl'),
      width: 200
    },
    {
      dataIndex: 'servicePort',
      title: t('node.nodeDetail.mockService.columns.accessPort'),
      width: 100
    },
    {
      dataIndex: 'createdByName',
      title: t('common.creator'),
      width: 120
    },
    {
      dataIndex: 'createdDate',
      title: t('common.createdDate'),
      width: 150
    }
  ]);

  return {
    columns
  };
}
