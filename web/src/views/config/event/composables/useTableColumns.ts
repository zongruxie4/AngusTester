import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

/**
 * Composable for managing table column configurations
 * @returns Table column configurations for different tables
 */
export function useTableColumns () {
  const { t } = useI18n();

  /**
   * Column configuration for push configuration table
   */
  const configColumns = computed(() => [
    {
      title: t('notification.columns.eventName'),
      dataIndex: 'eventName'
    },
    {
      title: t('notification.columns.category'),
      dataIndex: 'targetType'
    },
    {
      title: t('notification.columns.noticeType'),
      dataIndex: 'noticeType'
    }
  ]);

  /**
   * Column configuration for push record table
   */
  const recordColumns = computed(() => [
    {
      title: t('notification.columns.eventId'),
      dataIndex: 'id',
      key: 'id',
      width: '12%'
    },
    {
      title: t('notification.columns.eventName'),
      dataIndex: 'name',
      width: '12%',
      ellipsis: true
    },
    {
      title: t('notification.columns.content'),
      dataIndex: 'description',
      ellipsis: true
    },
    {
      title: t('notification.columns.receiver'),
      dataIndex: 'fullName',
      width: '12%'
    },
    {
      title: t('notification.columns.createdDate'),
      key: 'createdDate',
      dataIndex: 'createdDate',
      width: '12%'
    },
    {
      title: t('notification.columns.pushStatus'),
      dataIndex: 'pushStatus',
      key: 'pushStatus',
      width: '12%'
    }
  ]);

  return {
    configColumns,
    recordColumns
  };
}
