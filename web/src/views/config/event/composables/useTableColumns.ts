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
      title: t('event.columns.eventName'),
      dataIndex: 'eventName'
    },
    {
      title: t('common.category'),
      dataIndex: 'targetType'
    },
    {
      title: t('event.columns.noticeType'),
      dataIndex: 'noticeType'
    }
  ]);

  /**
   * Column configuration for push record table
   */
  const recordColumns = computed(() => [
    {
      title: t('event.columns.eventId'),
      dataIndex: 'id',
      key: 'id',
      width: '10%'
    },
    {
      title: t('event.columns.eventName'),
      dataIndex: 'name',
      width: '15%',
      ellipsis: true
    },
    {
      title: t('event.columns.content'),
      dataIndex: 'description',
      ellipsis: true
    },
    {
      title: t('event.columns.receiver'),
      dataIndex: 'fullName',
      width: '12%'
    },
    {
      title: t('common.createdDate'),
      key: 'createdDate',
      dataIndex: 'createdDate',
      width: '12%'
    },
    {
      title: t('event.columns.pushStatus'),
      dataIndex: 'pushStatus',
      key: 'pushStatus',
      width: '10%'
    }
  ]);

  return {
    configColumns,
    recordColumns
  };
}
